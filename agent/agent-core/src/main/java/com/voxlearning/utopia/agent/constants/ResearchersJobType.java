package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教研员职务
 * Created by yaguang.wang on 2016/10/19.
 */
@Getter
@RequiredArgsConstructor
public enum ResearchersJobType {
    RESEARCHER(1, "教研员",1),
    OTHER(2, "其他",1),
    DIRECTOR_GENERAL(3, "局长",1),
    DEPUTY_DIRECTOR_GENERAL(4, "副局长",1),
    CURATOR(5, "馆长",1),
    DEPUTY_CURATOR(6, "副馆长",1),
    DEAN(7, "院长",1),
    DEPUTY_DEAN(8, "副院长",1),
    SECRETARY(9, "书记",1),
    DEPUTY_SECRETARY(10, "副书记",1),
    HEAD(11, "主任",1),
    DEPUTY_HEAD(12, "副主任",1),
    DIVISION_HEAD(13, "处长",1),
    DEPUTY_DIVISION_HEAD(14, "副处长",1),
    SECTION_CHIEF(15, "科长",1),
    DEPUTY_SECTION_CHIEF(16, "副科长",1),
    SECRETARY_GENERAL(17, "秘书长",1),
    DEPUTY_SECRETARY_GENERAL(18, "副秘书长",1),
    CLERK(19, "科员",1),
    HEADMASTER(20, "校长",2),
    DEPUTY_HEADMASTER(21, "副校长",2),
    ACADEMIC_DIRECTOR(22, "教务主任",2),
    TEACHING_DIRECTOR(23, "教学主任",2),
    SUBJECT_LEADER(24, "学科组长",2),
    TEAM_LEADER(25, "教研组长",2),
    GRADE_LEADER(26, "年级组长",2),
    LESSON_PREPARATION_LEADER(27, "备课组长",2),
    UNREGISTERED_TEACHER(28, "未注册老师",2),
    SCHOOL_OTHER(29, "其他",1),
    ;

    private final int jobId;
    private final String jobName;
    private final int category;
    private static final Map<Integer, ResearchersJobType> jobMap;
    private static List<ResearchersJobType> schoolResourceJobList;
    private static List<ResearchersJobType> grResourceJobList;
    static {
        jobMap = new HashMap<>();
        schoolResourceJobList = new ArrayList<>();
        grResourceJobList = new ArrayList<>();
        for (ResearchersJobType type : ResearchersJobType.values()) {
            jobMap.put(type.getJobId(), type);
            if(type.getCategory()==1){
                grResourceJobList.add(type);
            }else{
                schoolResourceJobList.add(type);
            }
        }
    }

    public static ResearchersJobType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return ResearchersJobType.jobMap.get(id);
    }

    public static boolean schoolResourceJobType(ResearchersJobType jobType){
        return schoolResourceJobList.contains(jobType);
    }

    public static boolean sgrResourceJobType(ResearchersJobType jobType){
        return grResourceJobList.contains(jobType);
    }
}
