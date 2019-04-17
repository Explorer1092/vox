package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BasicAppCategoryType {

    PARAGRAPH("课文读背", Arrays.asList(10305, 10303, 10304), "全文跟读 | 朗读 | 背诵"), // 全文跟读，全文朗读，全文背诵
    WORD("词汇识记", Arrays.asList(10310, 10313, 10314, 10311, 10307, 10306), "单词跟读 | 听音选词 | 看图识词等"), // 单词跟读，听音选词，看图识词，单词辨识，单词排序，单词拼写
    SENTENCE("句型巩固", Arrays.asList(10312, 10322), "句子听力 | 连词成句"); // 句子听力，连词成句

    @Getter private final String name;
    @Getter private final List<Integer> categoryIds;
    @Getter private final String description;

    public static BasicAppCategoryType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ignore) {
            return null;
        }
    }
}
