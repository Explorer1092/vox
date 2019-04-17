package com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report;

import com.voxlearning.utopia.service.newexam.api.constant.SkillType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SkillPart implements Serializable {
    private static final long serialVersionUID = 4284601452760566613L;

    //技能列表
    private List<Skill> skills = new LinkedList<>();

    //技能名字
    private List<String> skillName = new LinkedList<>();


    private List<StudentSkillRecord> studentSkillRecords = new LinkedList<>();

    @Getter
    @Setter
    public static class Skill implements Serializable {
        private static final long serialVersionUID = -2860343397551599305L;
        private String skillId;
        private String skillName;
        private int clazzSkillRate;
        //市技能正确率，可以为空，表示不存在
        private int citySkillRate;
    }

    @Getter
    @Setter
    public static class StudentSkillRecord implements Serializable {
        private static final long serialVersionUID = 3866783143459346674L;
        private Long sid;
        private String sName;
        private boolean finished;
        private boolean begin;
        private List<String> skillValue = new LinkedList<>();
        private List<String> skillLevelValue = new LinkedList<>();
    }


    @Getter
    @Setter
    public static class SkillBO implements Serializable {
        private static final long serialVersionUID = -373312709778481908L;
        private String skillId;
        private String skillName;
        private int totalNum;
        private int rightNum;
    }

    @Getter
    @Setter
    public static class StudentSkillRecordBO implements Serializable {
        private Long sid;
        private String sName;
        private boolean finished;
        private boolean begin;
        private Map<SkillType,Integer> totalNumMap = new LinkedHashMap<>();
        private Map<SkillType,Integer> rightNumMap = new LinkedHashMap<>();
    }

}
