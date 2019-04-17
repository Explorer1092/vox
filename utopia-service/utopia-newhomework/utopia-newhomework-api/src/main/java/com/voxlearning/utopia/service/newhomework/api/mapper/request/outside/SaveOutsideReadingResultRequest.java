package com.voxlearning.utopia.service.newhomework.api.mapper.request.outside;

import com.voxlearning.utopia.service.newhomework.api.mapper.request.base.BaseReq;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaveOutsideReadingResultRequest extends BaseReq {

    private static final long serialVersionUID = -170341265895161109L;

    private String outsideReadingId; // 课外阅读ID
    private String missionId;       // 关卡ID
    private String questionId;      // 题ID
    private String clientType;      // 客户端类型:pc,mobile
    private String clientName;      // 客户端名称:***app
    private List<List<String>> answer;  // 用户答案
    private Long duration;  // 完成时长

    private List<List<String>> fileUrls;    // 文件地址 用于有作答过程的试题
}
