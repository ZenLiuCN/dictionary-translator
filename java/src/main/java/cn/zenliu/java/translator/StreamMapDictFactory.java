package cn.zenliu.java.translator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class StreamMapDictFactory {
    StreamMapDictFactory() {
    }

    static final char KEY_VALUE_SPLICER = ' ';
    static final char VALUES_SPLICER = '\'';

    static class StreamDict extends MapDictionary {
        StreamDict(final InputStream txtStream) {
            this.map = new BufferedReader(new InputStreamReader(txtStream))
                    .lines()
                    .parallel()
                    .map(l -> {
                        final String[] split = l.split(String.valueOf(KEY_VALUE_SPLICER));
                        if (split.length != 2)
                            return null;
                        else
                            return split;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            s -> s[0],
                            s -> Arrays.stream(s[1].split(String.valueOf(VALUES_SPLICER)))
                                    .parallel()
                                    .filter(it -> !it.isEmpty())
                                    .collect(Collectors.toList()).toArray(new String[0])
                            )
                    );

        }

        final Map<String, String[]> map;

        Map<String, String[]> getMapping() {
            return map;
        }
    }

    public static Dictionary build(InputStream textStream) {
        return new StreamDict(textStream);
    }
}
