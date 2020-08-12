package cn.zenliu.java.translator;

import java.util.Map;
import java.util.Set;

abstract class MapDictionary implements Dictionary {
    abstract Map<String, String[]> getMapping();

    public Set<String> words() {
        return getMapping().keySet();
    }

    public String[] find(final String word) {
        return getMapping().get(word);
    }
}