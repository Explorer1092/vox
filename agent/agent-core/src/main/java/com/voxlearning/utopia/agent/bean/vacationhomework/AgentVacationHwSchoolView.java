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
public class AgentVacationHwSchoolView implements Serializable{

    private static final long serialVersionUID = 3538763769342684321L;
    private Long id;
    private String name;                // 名称

    private Integer vacationHwTeaNum;   //布置老师
    private Integer teaScale;           //老师基数
    private Integer settleStuNum;       //结算学生
    private Integer authUnAssignNum;    //认证未布置
}
