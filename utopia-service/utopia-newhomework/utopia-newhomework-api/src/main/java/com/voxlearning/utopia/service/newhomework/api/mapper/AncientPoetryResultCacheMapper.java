package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * 亲子古诗活动活动结果缓存
 * @author majianxin
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"studentId", "activityId"})
@RequiredArgsConstructor
public class AncientPoetryResultCacheMapper implements Serializable {
    private static final long serialVersionUID = -381163002637070688L;

    private Long studentId;                                                     // 学生ID
    private String activityId;                                                  // 活动ID
    private Date finishAt;                                                      // 完成时间
    private Date updateAt;                                                      // 更新时间
    private Integer finishMissionCount = 0;                                     // 完成关卡数量

    private Double star = 0D;                                                   // 总星星数
    private Long duration = 0L;                                                 // 关卡用时
    private Integer wrongNum = 0;                                               // 错题数(原错题)
    private Integer correctTrueNum = 0;                                         // 订正正确数
    private Integer correctFalseNum = 0;                                        // 订正错误数

    // 格式<missionId, detail> 家长助力关卡missionId为 -1
    private LinkedHashMap<String, PoetryMissionCacheMapper> missionCache = new LinkedHashMap<>();

    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }

    /**
     * 获取未订正题目数
     * @return 未订正题数
     */
    @JsonIgnore
    public Integer getNoCorrectNum() {
        return wrongNum - correctTrueNum - correctFalseNum;
    }

    /**
     * 获取订正状态
     * @return 未订正题数
     */
    @JsonIgnore
    public String getCorrectStatus() {
        return wrongNum == 0 ? "WITHOUT_CORRECT" : getNoCorrectNum() == 0 ? "FINISH" : "TODO";
    }


    @Getter
    @Setter
    public static class PoetryMissionCacheMapper implements Serializable{
        private static final long serialVersionUID = -1838181498606811989L;

        private String missionId;           // 关卡ID
        private Double star = 0D;           // 关卡星星数
        private Date finishAt;              // 关卡完成时间

        @JsonIgnore
        public boolean isFinished() {
            return finishAt != null;
        }
    }
}
