package com.voxlearning.utopia.service.newhomework.api.constant;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum NatureSpellingType {


    ALPHABETIC_PRACTICE("字母学习", 10325),
    REPEAT_WORD("单词拼读", 10326),
    FUNNY_SPELLING("趣味拼写", 10327),
    PRONUNCIATION_CLASSIFICATION("读音归类", 10328),
    TONGUE_TWISTER("绕口令", 10329),
    LISTENING_CHOICE("听音选择", 10330),
    COMMON("普通", -1);


    @Getter
    public final String description;
    @Getter
    public final int categoryId;

    public static NatureSpellingType of(Integer key) {
        if (key == null) {
            return null;
        }
        for (NatureSpellingType t : values()) {
            if (t.getCategoryId() == key) {
                return t;
            }
        }
        return null;
    }
}
