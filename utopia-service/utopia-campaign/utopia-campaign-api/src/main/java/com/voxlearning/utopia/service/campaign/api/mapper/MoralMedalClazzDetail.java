package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Data;

import java.util.List;

@Data
public class MoralMedalClazzDetail implements java.io.Serializable {

    private Long clazzId;
    private String clazzName;
    private Long groupId;
    private String groupName;

    private Integer studentSize;
    private List<Student> studentList;

    private Long todayCount;        // 老师今天发布的个数
    private Integer semesterCount;  // 本学期发布的个数

    @Data
    public static class Student implements java.io.Serializable {
        private Long id;
        private String name;
        private String img;
        private Integer moralCount;
    }

}
