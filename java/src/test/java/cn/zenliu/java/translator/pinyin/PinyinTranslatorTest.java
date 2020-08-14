package cn.zenliu.java.translator.pinyin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PinyinTranslatorTest {
    final PinyinTranslator translator=PinyinTranslator.getNewInstance();
    @Test
    void py(){
        System.out.println(translator.translate("β-地中海贫血", null));
    }
}