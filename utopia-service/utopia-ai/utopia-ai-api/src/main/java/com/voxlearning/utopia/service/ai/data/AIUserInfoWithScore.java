package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.utopia.service.ai.constant.ChipsEnglishLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuan.zhu
 * @date 2018/8/24 11:13
 * description,change me
 */
@Setter
@Getter
public class AIUserInfoWithScore implements Serializable{

    private static final long serialVersionUID = 2789951437458722042L;

    private Long id;                 //用户id
    private String name;             //姓名
    private String productName;      //产品名
    private String productItemName;  //课程名
    private Double finishRate;       //完成率
    private ChipsEnglishLevel level; //定级
    private Boolean showPlay;        //发放电子教材

    private Long finishedNum;        //单元完成总数
    private Long totalNum;           //单元总数
    private String bookId;

    private List<AIUserUnitScore> scoreLis; //成绩列表
}
