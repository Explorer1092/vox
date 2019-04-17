package com.voxlearning.utopia.service.campaign.api.constant;

import lombok.Data;

import java.io.Serializable;

/**
 * 课件查询条件
 *
 * @Author: peng.zhang
 * @Date: 2018/9/10
 */
@Data
public class TeacherCoursewareParam implements Serializable {

    /**
     * 老师 ID
     */
    private Long teacherId;

    /**
     * 状态
     */
    private String status;
}
