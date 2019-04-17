package com.voxlearning.utopia.service.newhomework.api.entity.poetry;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */
@Getter
@Setter
public class AncientPoetryProcessResult implements Serializable {
    private static final long serialVersionUID = -5832832409605839096L;

    private String id;                                      // activityId-missionId-docId-studentId
    private String questionId;
    private String docId;
    private QuestionProcessResult processResult;            // 原题processResult
    private QuestionProcessResult correctProcessResult;     // 订正processResult

    @Getter
    @Setter
    public static class QuestionProcessResult implements Serializable {
        private static final long serialVersionUID = -642534808968341035L;

        private List<List<String>> answers;
        private List<List<Boolean>> subGrasp;
        private Boolean grasp;
        private Date createAt;
        private Long duration;                           // 完成时长（单位：毫秒）
        private String clientType;                       // 客户端类型:pc,mobile
        private String clientName;                       // 客户端名称:***app
        private Long parentId;                           // 订正家长ID
    }

    public static String generateId(String activityId, String missionId, String docId, Long studentId) {
        return StringUtils.join(activityId, "-", missionId, "-", docId, "-", studentId);
    }
}
