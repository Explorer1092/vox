package com.voxlearning.utopia.service.psr.homeworktermend.mapper;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

/**
 * 数学口算知识点下面题目信息
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/5/13
 * Time: 11:20
 * To change this template use File | Settings | File Templates.
 */
@Getter
@Setter
@Deprecated
public class MathMentalKpQuestion implements Serializable {

    private static final long serialVersionUID = -5576909290159471112L;


    //required fields
    private String id;
    private int type;
    private String kpName;
    private String kpId;
    private int kpQuestionCnt;
    private long contentTypeId;
    private long contentType2Id;
    private List<Integer> difficulties;


}
