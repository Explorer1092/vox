package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/4/19
 */
@Getter
@Setter
public class BaseHomeworkReport implements Serializable {

    private static final long serialVersionUID = -3339512089827702867L;

    // Map<作业形式, Map<做题id，做题的简要信息>>
    public Map<ObjectiveConfigType, Map<String, BaseHomeworkReportQuestion>> practices;

    // 以下为冗余属性
    private String homeworkId;      // 原作业id
    private Subject subject;        // 学科
    private Long groupId;           // 班组id
    private Long studentId;         // 学生id
}
