package com.voxlearning.utopia.agent.bean.performance.school;

import lombok.Getter;
import lombok.Setter;

/**
 * 学校家长指标数据
 *
 * @author deliang.che
 * @since  2019/2/21
 **/
@Setter
@Getter
public class SchoolParentIndicatorData {
    private Long schoolId;

    private Double parentPermeateRate;              //家长渗透率
    private Integer bindParentStuNum;               //绑定家长学生
    private Integer tmBindParentStuNum;             //本月绑定家长学生
    private Integer parentMauc;                     //家长月活
    private Integer parentStuActiveSettlementNum;   //家长学生双活
    private Integer newParentActiveSettlementNum;   //新家长双活
    private	Integer	tmRegisterBindParentStuNum;	        //本月新注册学生且已经绑定家长的学生数
    private	Integer	tmRegisterUnBindParentStuNum;       //本月新注册学生未绑定家长的学生数
    private Integer historyRegisterBindParentStuNum;        //历史注册学生绑定家长学生
    private Integer historyRegisterUnBindParentStuNum;      //历史注册学生未绑定家长学生
    private Integer tmLoginEq1BindStuParentNum;     //本月仅登录一次家长
    private Integer tmLoginEq2BindStuParentNum;     //本月仅登录两次家长

}
