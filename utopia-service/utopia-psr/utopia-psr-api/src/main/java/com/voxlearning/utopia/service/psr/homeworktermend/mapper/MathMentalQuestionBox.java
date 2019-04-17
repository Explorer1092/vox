package com.voxlearning.utopia.service.psr.homeworktermend.mapper;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

/**
 * 数学口算题包
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/5/13
 * Time: 11:19
 * To change this template use File | Settings | File Templates.
 */
@Getter
@Setter
public class MathMentalQuestionBox implements Serializable {

    private static final long serialVersionUID = -5576909290159471102L;

    //required fields
    private String unitId;
    private int totalQuestionCnt;
    private List<MathMentalKpQuestion> mathMentalKpQuestions;


}
