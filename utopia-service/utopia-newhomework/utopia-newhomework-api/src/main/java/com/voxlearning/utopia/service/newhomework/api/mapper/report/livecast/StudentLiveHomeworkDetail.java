package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class StudentLiveHomeworkDetail implements Serializable {
    private static final long serialVersionUID = 5304234613174349475L;
    private Long userId;// 学生ID
    private String userName;//学生名字
    private String homeworkId;//作业ID
    private Boolean finished;//是否完成
    private Integer liveHomeworkType;//作业
    private Boolean success;//是否成功返回
    private String failedInfo;// 发送错误的时候错误信息
}
