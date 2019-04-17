package com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class LearningHabitsPart implements Serializable {
    private static final long serialVersionUID = 3189623602805710554L;

    private int clazzFinishRate;
    private int cityFinishRate;
    //市区前10完成lv
    private int cityTopTenFinishRate;

}
