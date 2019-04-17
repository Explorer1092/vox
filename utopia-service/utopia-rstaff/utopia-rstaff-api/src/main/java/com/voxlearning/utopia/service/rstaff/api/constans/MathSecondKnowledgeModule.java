package com.voxlearning.utopia.service.rstaff.api.constans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MathSecondKnowledgeModule {

    SHUDRS("0101","数的认识"),
    SHUDYS("0102","数的运算"),
    LIANGYDW("0103","量与单位"),
    SHIYFC("0104","式与方程"),
    BIYBL("0105","比与比例"),
    TANSGL("0106","探索规律"),

    TUXRS("0201","图形认识"),
    CENGL("0202","测量"),
    TUXYWZ("0203","图形与位置"),
    TUXDYD("0204","图形的运动"),

    SHUJTS("0301","数据统计"),
    KENX("0302","可能性"),

    CELYFF("0401","策略与方法"),
    SHUXMX("0402","数学模型"),
    SHENGHYY("0403","生活应用"),

    ;

    public String code;
    public String desc;

    public static List<String> mathSecondKnowledgeModuleNames;

    static {
        mathSecondKnowledgeModuleNames = new LinkedList<>();
        for (MathSecondKnowledgeModule secondKnowledgeModule : values()){
            mathSecondKnowledgeModuleNames.add(secondKnowledgeModule.desc);
        }
    }

    public static List<String> getAllMathSecondKnowledgeModule(){
        return mathSecondKnowledgeModuleNames;
    }
}
