package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Set;

/**
 * @author majianxin
 */
@Setter
@Getter
public class JztHomeworkReport implements Serializable {
    private static final long serialVersionUID = 2979406343335499606L;

    private String homeworkName;                                            // 作业名称
    private String subject;                                                 // 学科
    private Set<String> unitNameSet;                                        // 单元名
    private String planDuration;                                            // 预计时长
    private String endDate;                                                 // 作业截止时间 mm月dd日 mm:ss
    private ScoreStatus scoreStatus;                                        // 分数的显示状态

    /**
     * 家长通作业报告->作业状态枚举
     */
    public enum ScoreStatus {
        SCORE,          // 分数
        LEVEL,          // 等级
        UN_FINISHED,    // 未完成
        NOT_SCORE;       // 不判分


        public static JztHomeworkReport.ScoreStatus process(@NotNull Boolean scoreRegionFlag, @NotNull Boolean finished, @NotNull Boolean notShowScore) {
            if (!finished) {
                return UN_FINISHED;
            }
            if (notShowScore) {
                return NOT_SCORE;
            }
            if (scoreRegionFlag) {
                return LEVEL;
            } else {
               return SCORE;
            }
        }
    }
}
