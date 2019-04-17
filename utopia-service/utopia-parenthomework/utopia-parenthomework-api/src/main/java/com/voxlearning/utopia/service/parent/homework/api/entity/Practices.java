package com.voxlearning.utopia.service.parent.homework.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 作业形式
 */
@Getter
@Setter
public class Practices implements Serializable {

    private static final long serialVersionUID = -3775866081796336377L;

    private String type; //作业形式，参照ObjectiveConfigType

    private int timeLimit;//参考 MentalArithmeticTimeLimit

    private List<Questions> questions; //题信息
}
