package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 对话场景，
 * @author songtao
 * @since 2018/5/23
 */
@Getter
@Setter
public class AITalkScene implements Serializable {
    private static final long serialVersionUID = 398454060489704044L;
    private String roleImage;
    private RoleType roleType;
    private String media;
    private String original;
    private String description;
    private List<Map<String, Object>> suggestion;

    public AITalkScene() {
        this.setSuggestion(Collections.emptyList());
    }
    public enum RoleType {
        AITeacher,//老师
        Student, //学生
        K //占位符
    }
}
