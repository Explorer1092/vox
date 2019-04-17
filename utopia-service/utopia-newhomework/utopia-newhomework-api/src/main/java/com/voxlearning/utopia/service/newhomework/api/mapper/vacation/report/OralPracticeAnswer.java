package com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public  class OralPracticeAnswer implements Serializable {
    private static final long serialVersionUID = 2042670830925446629L;
    private String qid; //题ID
    private double store; // 题得分
    private String contentType;
    private Integer difficulty;
    private int showType;
    private List<OralDetailBranchInformation> answerList = new LinkedList<>(); // 每个整个题目的学生成绩
}