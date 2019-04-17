package com.voxlearning.utopia.service.campaign.api.constant;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数
 *
 * @Author: peng.zhang
 * @Date: 2018/9/7
 */
@Data
public class TeacherCoursewarePageInfo implements Serializable {

    private Integer pageNum;

    private Integer pageSize;

    private Long teacherId;

    private String status;
}
