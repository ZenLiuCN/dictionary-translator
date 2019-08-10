package cn.zenliu.kotlin.translator.py

import cn.zenliu.kotlin.translator.*
import cn.zenliu.kotlin.translator.EMIT_COMPARATOR
import cn.zenliu.kotlin.translator.ForwardLongestSelector
import cn.zenliu.kotlin.translator.data.BIT_MASKS
import cn.zenliu.kotlin.translator.data.CHAR_12295
import cn.zenliu.kotlin.translator.data.CODE_TAB_ONE
import cn.zenliu.kotlin.translator.data.CODE_TAB_THREE
import cn.zenliu.kotlin.translator.data.CODE_TAB_TWO
import cn.zenliu.kotlin.translator.data.MAX_VALUE
import cn.zenliu.kotlin.translator.data.MIN_VALUE
import cn.zenliu.kotlin.translator.data.OFFSET_TAB_ONE
import cn.zenliu.kotlin.translator.data.OFFSET_TAB_TWO
import cn.zenliu.kotlin.translator.data.PADDING_MASK
import cn.zenliu.kotlin.translator.data.PADDING_TAB_ONE
import cn.zenliu.kotlin.translator.data.PADDING_TAB_THREE
import cn.zenliu.kotlin.translator.data.PADDING_TAB_TWO
import cn.zenliu.kotlin.translator.data.PINYIN_12295
import cn.zenliu.kotlin.translator.data.PYTABLE
import org.ahocorasick.trie.*


object Translator {
	internal var trie: Trie? = null
	internal var selector: SegmentationSelector? = ForwardLongestSelector()
	internal val dicts: MutableSet<Dictionary> = mutableSetOf()
	private fun parseDict() {
		trie = dicts.map { it.words() }
			.flatten()
			.takeIf { it.isNotEmpty() }
			?.let {
				Trie.builder().apply {
					it.forEach {
						this.addKeyword(it)
					}
				}.build()
			}
	}

	fun reset() {
		dicts.clear()
		trie = null
		selector = null
	}

	fun setSelector(selector: SegmentationSelector) {
		Translator.selector = selector
	}

	fun add(dict: Dictionary) {
		dict.takeIf { it.words().isNotEmpty() }?.let {
			dicts.add(dict)
		}

	}

	fun translate(str: String, separator: String = " "): String? {
		return find(str, trie, dicts, separator, selector)
	}

	fun translate(c: Char) = when {
		c == CHAR_12295 -> PINYIN_12295
		isChinese(c) -> PYTABLE[index(c)]
		else -> c.toString()
	}

	fun isChinese(c: Char) = CHAR_12295 == c ||
		(MIN_VALUE <= c
			&& c <= MAX_VALUE
			&& index(c) > 0)

	private fun index(c: Char) = (c - MIN_VALUE).let {
		when {
			0 <= it && it < OFFSET_TAB_ONE -> decode(
				PADDING_TAB_ONE,
				CODE_TAB_ONE,
				it).toInt()
			OFFSET_TAB_ONE <= it && it < OFFSET_TAB_TWO -> decode(
				PADDING_TAB_TWO,
				CODE_TAB_TWO,
				it - OFFSET_TAB_ONE
			).toInt()
			else -> decode(
				PADDING_TAB_THREE,
				CODE_TAB_THREE,
				it - OFFSET_TAB_TWO
			).toInt()
		}
	}

	private fun decode(
		paddings: ByteArray,
		indexes: ByteArray,
		offset: Int
	) = ((offset / 8) to (offset % 8))
		.let { (paddingIndex, maskIndex) ->
			(indexes[offset].toInt() and 0xff).toShort().let {
				if (paddings[paddingIndex].toInt() and BIT_MASKS[maskIndex] != 0) {
					(it.toInt() or PADDING_MASK).toShort()
				} else it
			}
		}



	private fun find(
		inputStr: String?,
		trie: Trie? = null,
		pinyinDictList: Set<Dictionary>? = null,
		separator: String,
		selector: SegmentationSelector? = null
	): String? {
		when {
			inputStr.isNullOrBlank() -> return inputStr
			trie == null || selector == null -> {
				// 没有提供字典或选择器，按单字符转换输出
				return buildString {
					inputStr.forEachIndexed { idx, c ->
						append(translate(c))
						if (idx != inputStr.length - 1) {
							append(separator)
						}
					}
				}
			}
			else -> {
				val selectedEmits = selector.select(trie.parseText(inputStr))
				selectedEmits.sortedWith(EMIT_COMPARATOR)
				val resultPinyinStrBuf = StringBuffer()
				var nextHitIndex = 0
				var i = 0
				while (i < inputStr.length) {
					// 首先确认是否有以第i个字符作为begin的hit
					if (nextHitIndex < selectedEmits.size && i == selectedEmits.get(nextHitIndex).getStart()) {
						// 有以第i个字符作为begin的hit
						val fromDicts = pinyinFromDict(selectedEmits.get(nextHitIndex).getKeyword(), pinyinDictList)
						for (j in fromDicts.indices) {
							resultPinyinStrBuf.append(fromDicts[j].toUpperCase())
							if (j != fromDicts.size - 1) {
								resultPinyinStrBuf.append(separator)
							}
						}

						i = i + selectedEmits.get(nextHitIndex).size()
						nextHitIndex++
					} else {
						// 将第i个字符转为拼音
						resultPinyinStrBuf.append(translate(inputStr[i]))
						i++
					}

					if (i != inputStr.length) {
						resultPinyinStrBuf.append(separator)
					}
				}

				return resultPinyinStrBuf.toString()
			}
		}

	}

	private fun pinyinFromDict(word: String, pinyinDictSet: Set<Dictionary>?) = pinyinDictSet
		?.find { it.words().contains(word) }
		?.find(word)
		?: throw IllegalArgumentException("Not find dictionary contains word: $word")


}
