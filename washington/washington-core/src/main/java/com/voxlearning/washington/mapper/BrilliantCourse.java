package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class BrilliantCourse implements Serializable {
    private static final long serialVersionUID = 1831487901643673167L;

    private String studentName;//学生名字
    private Set<String> knowledgePoints = new HashSet<>();
    private String bookName;//课程名字
    private List<String> courseBrief;//课程简介
    private String coverUrl;//课程封面

    //推荐的课时
    private String courseName;//课时名字
    private String courseCoverUrl;//课程封面

    //免费的课时
    private String freeCourseName;//课时名字
    private String freeCourseCoverUrl;//课程封面


    private boolean bought;//是否购买

    private String unlockCourseUrl; //解锁推荐
    private String goInRecommendCourseUrl;//进入推荐课程

    private String goInFreeCourseUrl;//进入免费课程

    private String courseListUrl;//课时列表




}
