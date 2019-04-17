package com.voxlearning.utopia.service.rstaff.api.constans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-07-18 16:56
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EnglishSkill {
    LINTENING("听"),
    SPEAKING("说"),
    READING("读"),
    WRITING("写");

    public String desc;

    public static List<String> englishSkillNames;

    static {
        englishSkillNames = new LinkedList<>();
        for (EnglishSkill skill : values()){
            englishSkillNames.add(skill.desc);
        }
    }

    public static List<String> getAllEnglishSkill(){
        return englishSkillNames;
    }
}
