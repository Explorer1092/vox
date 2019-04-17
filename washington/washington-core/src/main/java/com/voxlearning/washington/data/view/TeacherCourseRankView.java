package com.voxlearning.washington.data.view;

import lombok.Data;

/**
 * 课件人气榜视图
 *
 * @Author: peng.zhang
 * @Date: 2018/10/26
 */
@Data
public class TeacherCourseRankView {

    private String courseId;

    /**
     * 人气值
     */
    private Long popularityScore;
}
