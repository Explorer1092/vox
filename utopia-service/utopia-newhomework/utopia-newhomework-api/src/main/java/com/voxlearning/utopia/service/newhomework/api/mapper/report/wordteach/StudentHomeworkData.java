package com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Description: 学生作业答题信息
 * @author: Mr_VanGogh
 * @date: 2018/12/19 下午4:44
 */
@Getter
@Setter
public class StudentHomeworkData implements Serializable {
    private static final long serialVersionUID = 7437449953816045341L;

    private Long studentId;       //学生ID
    private String studentName;     //学生姓名
    private Integer score;           //分数(星级):这里跟前端之前约定为星级
    private Integer star;           //星级
    private String flashvarsUrl;      //预览地址
    private Boolean finished;       //完成状态
}
