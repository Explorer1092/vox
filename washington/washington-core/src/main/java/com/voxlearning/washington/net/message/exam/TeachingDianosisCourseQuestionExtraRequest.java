package com.voxlearning.washington.net.message.exam;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeachingDianosisCourseQuestionExtraRequest implements Serializable {

    private static final long serialVersionUID = 8388236104563275811L;
    private String courseId;         //课程ID
    private String taskId;           //任务ID

    private Long createTime;           //创建时间
}
