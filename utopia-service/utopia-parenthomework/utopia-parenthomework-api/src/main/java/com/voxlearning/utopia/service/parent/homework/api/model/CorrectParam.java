package com.voxlearning.utopia.service.parent.homework.api.model;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.util.Map;

/**
 * 作业参数
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-13
 */
@Getter
@Setter
public class CorrectParam implements Serializable {

    private static final long serialVersionUID = 0L;
    private String homeworkId; //作业id
    private Long studentId;//学生id
    private Long currentUserId;//当前用户id
    private String courseId;//课程id
    private String source; //来源
    private Command command;
    private ObjectiveConfigType objectiveConfigType;
    private Subject subject;
    private BizType bizType;
    private Map<String, Object> data;
    private String homeworkResultId;
}
