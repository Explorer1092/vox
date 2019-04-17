package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生历史列表页-评论列表
 *
 * @author xuesong.zhang
 * @since 2016-03-21
 */
@Getter
@Setter
public class HomeworkCommentMapper implements Serializable {

    private static final long serialVersionUID = -917342566903956248L;

    private String homeworkId;
    private Long teacherId;
    private String teacherName;
    private Long studentId;
    private String studentName;
    private String comment;
    private Date createDate;
}
