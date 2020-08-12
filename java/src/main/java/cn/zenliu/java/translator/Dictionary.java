package cn.zenliu.java.translator;

import java.util.Map;
import java.util.Set;

public interface Dictionary {
    Set<String> words();

    String[] find(String word);
}


