package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuan.zhu
 * @date 2018/8/24 11:56
 * 学生单元成绩
 */

@Getter
@Setter
public class AIUserUnitScore implements Serializable {

    private static final long serialVersionUID = -2592774122466695895L;

    private String name;        //单元名称
    private Integer score;      //分数
    private Integer rank;       //排序
    private Boolean finished;   //是否完成

    private Date openDate;      //单元开课日期

    private String unitId;      //单元ID
    private String operationLog;//运营日志

}
