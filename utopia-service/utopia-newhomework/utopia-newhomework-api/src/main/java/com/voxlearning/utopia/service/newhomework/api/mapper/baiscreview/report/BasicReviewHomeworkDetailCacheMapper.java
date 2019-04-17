package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class BasicReviewHomeworkDetailCacheMapper implements Serializable{
    private static final long serialVersionUID = -6746312390029896353L;
    private Long duration;      // 本次作业用时（来源于BaseHomeworkResult::processDuration）
    private Integer avgScore;    // 本次作业平均分（来源于BaseHomeworkResult::processScore）
    private Date finishAt;      // 本次作业完成时间
    private Integer stageId;        // 关卡id
    private String stageName;       // 关卡名
    private String homeworkId;      // 关联的作业id
    private Boolean begin;

    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }
}
