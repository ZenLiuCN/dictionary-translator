package cn.zenliu.java.translator.pinyin;

import cn.zenliu.java.translator.Dictionary;
import cn.zenliu.java.translator.InternalSelector;
import cn.zenliu.java.translator.SegmentationSelector;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.zenliu.java.translator.InternalSelector.FORWARD_LONGEST_SELECTOR;


public interface PinyinTranslator {
    void reset();

    void setSelector(SegmentationSelector segmentationSelector);

    void add(@NotNull Dictionary dict);

    void loadChinaCity();

    String translate(String str, String separator);

    String translate(char c);

    @Contract(value = " -> new", pure = true)
    static @NotNull PinyinTranslator getNewInstance() {
        return new Pinyin();
    }

    class Pinyin implements data, data_tab1, data_tab2, data_tab3, PinyinTranslator {
        private Trie trie = null;
        private SegmentationSelector selector = FORWARD_LONGEST_SELECTOR;
        private final Set<Dictionary> dicts = new HashSet<>();

        void parseDict() {
            final Set<String> strings = dicts.parallelStream()
                    .map(Dictionary::words)
                    .reduce(new HashSet<>(), (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
            if (!strings.isEmpty()) {
                final Trie.TrieBuilder builder = Trie.builder();
                strings.forEach(builder::addKeyword);
                trie = builder.build();
            }
        }

        public void reset() {
            dicts.clear();
            trie = null;
            selector = null;
        }

        public void setSelector(SegmentationSelector segmentationSelector) {
            selector = segmentationSelector;
        }

        public void add(@NotNull Dictionary dict) {
            if (!dict.words().isEmpty()) {
                dicts.add(dict);
            }
        }

        @Override
        public void loadChinaCity() {
            add(data.chinaCityDict());
        }

        static final String SEPARATOR = " ";

        public String translate(String str, @Nullable String separator) {
            return find(str, trie, dicts, separator == null ? SEPARATOR : separator, selector);
        }

        public String translate(char c) {
            if (c == CHAR_12295) return PINYIN_12295;
            if (isChinese(c)) return PYTABLE[index(c)];
            return String.valueOf(c);
        }

        boolean isChinese(char c) {
            return CHAR_12295 == c ||
                    (MIN_VALUE <= c
                            && c <= MAX_VALUE
                            && index(c) > 0);
        }

        private int index(char c) {
            final int it = c - MIN_VALUE;
            if (it >= 0 && it < OFFSET_TAB_ONE) {
                return decode(PADDING_TAB_ONE, CODE_TAB_ONE, it);
            } else if (OFFSET_TAB_ONE <= it && it < OFFSET_TAB_TWO) {
                return decode(
                        PADDING_TAB_TWO,
                        CODE_TAB_TWO,
                        it - OFFSET_TAB_ONE
                );
            } else {
                return decode(
                        PADDING_TAB_THREE,
                        CODE_TAB_THREE,
                        it - OFFSET_TAB_TWO
                );
            }
        }

        private @NotNull String find(
                @NotNull String input,
                Trie trie,
                Set<Dictionary> dicts,
                String separator,
                SegmentationSelector segmentationSelector
        ) {
            if (input.isEmpty() || input.trim().isEmpty()) return input;
            if (trie == null || segmentationSelector == null) {
                final StringBuilder sb = new StringBuilder();
                final AtomicInteger pos = new AtomicInteger(-1);
                input.chars().forEach(c -> {
                    sb.append(translate((char) c));
                    final int p = pos.addAndGet(1);
                    if (p != input.length() - 1) {
                        sb.append(separator);
                    }
                });
                return sb.toString();
            }
            final List<Emit> selectedEmits = segmentationSelector.select(trie.parseText(input));
            selectedEmits.sort(InternalSelector.EMIT_COMPARATOR);
            final StringBuilder resultPinyinStrBuf = new StringBuilder();
            int nextHitIndex = 0;
            int i = 0;
            while (i < input.length()) {
                if (nextHitIndex < selectedEmits.size() && i == selectedEmits.get(nextHitIndex).getStart()) {
                    // 有以第i个字符作为begin的hit
                    final String[] fromDicts = pinyinFromDict(selectedEmits.get(nextHitIndex).getKeyword(), dicts);
                    for (int j = 0; j < fromDicts.length; j++) {
                        resultPinyinStrBuf.append(fromDicts[j].toUpperCase());
                        if (j != fromDicts.length - 1) {
                            resultPinyinStrBuf.append(separator);
                        }
                    }
                    i = i + selectedEmits.get(nextHitIndex).size();
                    nextHitIndex++;
                } else {
                    // 将第i个字符转为拼音
                    resultPinyinStrBuf.append(translate(input.charAt(i)));
                    i++;
                }

                if (i != input.length()) {
                    resultPinyinStrBuf.append(separator);
                }
            }
            return resultPinyinStrBuf.toString();
        }

        @Contract(pure = true)
        private int decode(
                @NotNull byte[] paddings,
                @NotNull byte[] indexes,
                int offset
        ) {
            final int paddingIndex = offset / 8;
            final int maskIndex = offset % 8;
            final int i = indexes[offset] & 0xff;
            if ((paddings[paddingIndex] & BIT_MASKS[maskIndex]) != 0) {
                return (i | PADDING_MASK);
            } else return i;

        }

        private String[] pinyinFromDict(String word, @NotNull Set<Dictionary> pinyinDictSet) {
            for (Dictionary dictionary : pinyinDictSet) {
                if (dictionary.words().contains(word)) {
                    return dictionary.find(word);
                }
            }
            throw new IllegalArgumentException("Not find dictionary contains word: $word");
        }
    }
}
