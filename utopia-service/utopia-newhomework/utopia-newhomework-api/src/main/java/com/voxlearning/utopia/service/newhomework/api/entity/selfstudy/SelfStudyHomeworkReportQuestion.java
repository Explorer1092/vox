package com.voxlearning.utopia.service.newhomework.api.entity.selfstudy;

import com.voxlearning.utopia.service.newhomework.api.mapper.QuestionWrongReason;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xuesong.zhang
 * @since 2017/3/22
 */
@Getter
@Setter
public class SelfStudyHomeworkReportQuestion implements Serializable {

    private static final long serialVersionUID = 2376920404500028424L;

    private String processId;                       // 对应的processId
    private Boolean grasp;                          // 掌握
    private String similarQuestionId;               // 类题id
    private String sourceQuestionId;                // 原题id
    private QuestionWrongReason wrongReason;        // 错题原因
    private String courseId;                        // 课程ID
}
