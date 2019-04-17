package com.voxlearning.utopia.service.rstaff.api.constans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-07-18 16:57
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ChineseSkill {
    PINGYZSYY("拼音知识运用"),
    ZHICJLYYY("字词积累与运用"),
    JUZJLYYY("句子积累与运用"),
    PIANZYWCJL("篇章与文常积累"),
    YUEDNL("阅读能力"),
    XIZNL("习作（写话）能力"),
    SHIJTJNL("实践探究能力"),
    KOUYJJNL("口语交际能力")
    ;

    public String desc;

    private static List<String> chineseSkillNames;

    static {
        chineseSkillNames = new LinkedList<>();
        for(ChineseSkill skill : values()){
            chineseSkillNames.add(skill.desc);
        }
    }

    public static List<String> getAllChineseSkillNames(){
        return chineseSkillNames;
    }
}
