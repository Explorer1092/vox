package com.voxlearning.utopia.service.newhomework.api.mapper.request;

import com.voxlearning.utopia.service.newhomework.api.mapper.request.base.BaseReq;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaveAncientPoetryResultRequest extends BaseReq {
    private static final long serialVersionUID = -6096856016481817099L;

    private Long studentId;         // 学生ID
    private String activityId;      // 活动ID
    private String missionId;       // 关卡ID
    private String modelType;       // 模块类型
    private String questionId;      // 题ID
    private String clientType;      // 客户端类型:pc,mobile
    private String clientName;      // 客户端名称:***app
    private List<List<String>> answer;  // 用户答案
    private Long duration;              // 完成时长
    private boolean parentMission;   // 是否是亲子助力关卡
    private boolean correct;         // 是否订正错题

    private List<String> studentAudioUrls;// 学生录音地址
    private List<String> parentAudioUrls; // 家长录音地址
}
