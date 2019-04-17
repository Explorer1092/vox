package com.voxlearning.utopia.service.newhomework.api.mapper.vacation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2016/12/1
 */
@Getter
@Setter
public class VacationHomeworkDetailCacheMapper implements Serializable {

    private static final long serialVersionUID = -8922394957045046588L;

    private Long duration;      // 本次作业用时（来源于BaseHomeworkResult::processDuration）
    private Integer avgScore;    // 本次作业平均分（来源于BaseHomeworkResult::processScore）
    private Date finishAt;      // 本次作业完成时间
    private Integer weekRank;
    private Integer dayRank;

    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }
}
