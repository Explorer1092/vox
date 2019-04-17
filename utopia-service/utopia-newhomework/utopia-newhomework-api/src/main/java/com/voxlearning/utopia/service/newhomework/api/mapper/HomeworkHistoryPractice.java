package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 学生历史详情页-作业形式下的数据
 *
 * @author xuesong.zhang
 * @since 2016-03-22
 */
@Getter
@Setter
public class HomeworkHistoryPractice implements Serializable {

    private static final long serialVersionUID = -3113163013638941442L;
    private Integer rightCount; // 正确题数
    private Integer wrongCount; // 错误题数
    private Integer rate;       // 正确率
    private Integer completePracticeCount; //完成应用数
    private Integer score;      // 分数
    private Long duration;     // 耗时（单位：分钟）
    private String correctedType;  // 作业订正类型
    private Integer totalNeedCorrectedNum;                   // 需要订正数目
    private Integer finishCorrectedCount;            //已经订正的数目
    private String state;
}
