package com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 绘本全部练习题的报告信息
 */
@Setter
@Getter
public class ExercisesData implements Serializable {
    private static final long serialVersionUID = 8921773231492956656L;
    private int rightNum;                         //正确数目
    private int totalExercises;                   //一样练习的数目
    private List<ExercisesQuestionData> exercisesQuestionInfo = new LinkedList<>();//每题的信息

}