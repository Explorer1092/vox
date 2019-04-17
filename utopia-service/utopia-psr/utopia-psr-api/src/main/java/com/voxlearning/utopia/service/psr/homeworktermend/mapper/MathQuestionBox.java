package com.voxlearning.utopia.service.psr.homeworktermend.mapper;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

/**
 * 数学应试题包
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/5/13
 * Time: 11:11
 * To change this template use File | Settings | File Templates.
 */
@Getter
@Setter
@Deprecated
public class MathQuestionBox implements Serializable {

    private static final long serialVersionUID = -5576909790159471102L;

    private String boxId;
    private String unitId;
    private int difficulty;
    private String boxName;
    private List<String> questionIds;
    private int type;

}
