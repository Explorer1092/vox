package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AgentTag
 *
 * @author song.wang
 * @date 2017/5/23
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentTag {

    REJECT(1, "驳回"),
    PRESIDENT(2, "校长"),
    VICE_PRESIDENT(3, "副校长"),
    EDUCATION_DIRECTOR(4, "教务主任"),
    TEACHING_DIRECTOR(5, "教学主任"),
    SUBJECT_LEADER(6, "学科组长"),
    RESEARCH_LEADER(7, "教研组长"),
    GRADE_LEADER(8, "年级组长"),
    LESSON_PREPARATION_LEADER(9, "备课组长")
    ;
    public final int code;
    public final String desc;

    private static final Map<Integer, AgentTag> CODE_MAPPING;

    static {
        CODE_MAPPING = new HashMap<>();
        for (AgentTag e : values()) {
            CODE_MAPPING.put(e.code, e);
        }
    }

    public static AgentTag codeOf(Integer code){
        return CODE_MAPPING.get(code);
    }

    public static AgentTag nameOf(String name){
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<AgentTag> fetchTeacherPositionTags(){
        List<AgentTag> list = new ArrayList<>();
        list.add(AgentTag.PRESIDENT);
        list.add(AgentTag.VICE_PRESIDENT);
        list.add(AgentTag.EDUCATION_DIRECTOR);
        list.add(AgentTag.TEACHING_DIRECTOR);
        list.add(AgentTag.SUBJECT_LEADER);
        list.add(AgentTag.RESEARCH_LEADER);
        list.add(AgentTag.GRADE_LEADER);
        list.add(AgentTag.LESSON_PREPARATION_LEADER);
        return list;
    }

}
