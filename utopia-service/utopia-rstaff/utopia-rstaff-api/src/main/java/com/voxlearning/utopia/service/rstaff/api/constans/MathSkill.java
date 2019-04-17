package com.voxlearning.utopia.service.rstaff.api.constans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-07-18 16:41
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MathSkill {
    SHUG("数感"),
    FUHYS("符号意识"),
    KONGJGL("空间观念"),
    JIHEZHIGUAN("几何直观"),
    SHUJFX("数据分析观念"),
    YUSNL("运算能力"),
    TUILNL("推理能力"),
    SIXMX("模型思想");

    public String desc;

    private static List<String> mathSkillNames;

    static {
        mathSkillNames = new LinkedList<>();
        for(MathSkill skill : values()){
            mathSkillNames.add(skill.desc);
        }
    }

    public static List<String> getAllMathSkillNames(){
        return mathSkillNames;
    }




}
