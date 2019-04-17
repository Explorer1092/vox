package com.voxlearning.utopia.service.newhomework.api.mapper.report.pc;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class QuestionReportDetail implements Serializable {
    private static final long serialVersionUID = -1567036424341489805L;
    private String questionId;
    private int totalNum;
    private double totalScore;
    private int rightNum;
    private int interventionRightNum;   //订正后的正确数
    private int proportion;         //正确率(没有干预&有干预,最终的结果)
    private int firstProportion;    //初始正确率(有干预情况下,存在)(字词讲练)
    private boolean hasIntervention;    //是否干预
}
