package com.voxlearning.utopia.service.newhomework.api.mapper.avenger;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/6/20
 */
@Setter
@Getter
public class AvengerHomeworkPracticeContent implements Serializable {

    private static final long serialVersionUID = -1169824210064531585L;

    private List<AvengerHomeworkQuestion> questions;

    // 新口算特有属性
    private Integer timeLimit;      // 口算训练限定时间
    private Boolean mentalAward;    // 口算训练是否有奖励
    private Boolean recommend;      // 口算训练是否推荐题目

    // 纸质口算特有属性
    private String workBookId;      // 纸质口算练习册id
    private String workBookName;    // 纸质口偶算练习册名称
    private String homeworkDetail;  // 纸质口算作业详情(页码)

    private List<AvengerHomeworkApp> apps;     // 应用类型(目前口语交际，字词讲练用到)
}
