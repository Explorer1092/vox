package com.voxlearning.utopia.service.rstaff.api.constans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EnglishKnowledgeModule {
    ZIM(1,"字母"),
    YUY(2,"语音"),
    DANCDY(3,"单词短语"),
    JUSYF(4,"句式语法"),
    HUATGN(5,"话题功能");

    public int code;
    public String desc;

    public static List<String> englishKnowledgeModuleNames;

    static {
        englishKnowledgeModuleNames = new LinkedList<>();
        for (EnglishKnowledgeModule knowledgeModule : values()){
            englishKnowledgeModuleNames.add(knowledgeModule.desc);
        }
    }

    public static List<String> getAllEnglishKnowledgeModule(){
        return englishKnowledgeModuleNames;
    }

}
