# translator
this project use  Aho-Corasick Algorithm to do fast translate by dictionaries. 

## dictionary structure
`v'v'v k`

## current build in data
1. chinese to pinyin
    ```java
     PinyinTranslator translator= PinyinTranslator.getNewInstance();// can be hold
     translator.loadChinaCity();
     String py=translator.translate("汉字",null);
    ```


