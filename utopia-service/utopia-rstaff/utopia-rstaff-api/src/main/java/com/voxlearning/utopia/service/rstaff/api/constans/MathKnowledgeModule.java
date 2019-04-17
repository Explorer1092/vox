package com.voxlearning.utopia.service.rstaff.api.constans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MathKnowledgeModule {

    SHUYDS("01","数与代数"),
    TUXYJH("02","图形与几何"),
    TONGJYGL("03","统计与概率"),
    ZONGHYSJ("04","综合与实践")
    ;

    public String code;
    public String desc;

    public static List<String> mathKnowledgeModuleNames;

    static {
        mathKnowledgeModuleNames = new LinkedList<>();
        for (MathKnowledgeModule knowledgeModule : values()){
            mathKnowledgeModuleNames.add(knowledgeModule.desc);
        }
    }

    public static List<String> getAllMathKnowledgeModule(){
        return mathKnowledgeModuleNames;
    }

}
