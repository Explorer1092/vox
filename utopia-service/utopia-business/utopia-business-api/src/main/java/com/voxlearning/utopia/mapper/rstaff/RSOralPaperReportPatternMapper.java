package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author changyuan.liu
 * @since 2015/5/21
 */
@Data
public class RSOralPaperReportPatternMapper implements Serializable {

    private static final long serialVersionUID = -3230497492858591817L;

    private String pattern;         // 题型
    private Double avgMScore;    // 机器打分平均分
    private Double avgTScore;    // 老师打分平均分
}
