package com.voxlearning.utopia.agent.bean.vacationhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 假期作业View
 *
 * @author deliang.che
 * @since  2019/1/3
 **/
@Getter
@Setter
public class AgentVacationHwSumView implements Serializable{

    private static final long serialVersionUID = 6382004729779826321L;
    private Long id;
    private Integer idType;                       // 1:部门   2：user
    private String name;                          // 名称

    private Integer vacationHwTeaNum;   //布置老师
    private Integer teaScale;           //老师基数
    private Integer settleStuNum;       //结算学生
    private Double vacationHwRate;      //布置率

    private Boolean clickable;
    private Boolean self;
    private Integer serviceType;        //1：小学  2：中学
}
