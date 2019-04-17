package com.voxlearning.utopia.service.parent.homework.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 试题
 * @author chongfeng.qi
 * @date 2018-11-07
 */
@Getter
@Setter
public class Questions implements Serializable {
    private static final long serialVersionUID = -3775876192256336377L;

    private String questionId; //题id

    private String docId; //docId

    private Integer questionVersion; //题版本

    private Double score; //分数

    private Integer seconds; //预计耗时，单位秒

    private String questionBoxId; //题包id

    private List<List<Integer>> submitWay; //提交方式，0：直接作答；1：拍照；2：录音；3：口语；
}
