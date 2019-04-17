package com.voxlearning.utopia.agent.bean.indicator;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * ParentIndicator
 *
 * @author deliang.che
 * @since  2019/2/21
 */
@Getter
@Setter
public class ParentIndicator implements Serializable {

    private static final long serialVersionUID = 5642158991304553100L;

    private	Integer	bindParentStuNum;	                //绑定家长的学生数
    private	Integer	bindStuParentNum;	                //绑定学生的家长数
    private	Double	parentPermeateRate;	                //家长渗透率
    private	Integer	tmRegisterBindParentStuNum;	        //本月新注册学生且已经绑定家长的学生数
    private	Integer tmRegisterUnbindParentStuNum;       //本月新注册学生未绑定家长的学生数
    private	Integer	tmLoginGte1BindStuParentNum;        //本月登录1次及以上且绑定学生的家长数（MAU）
    private	Integer	tmLoginGte2BindStuParentNum;        //本月登录2次及以上且绑定学生的家长数
    private	Integer	tmLoginGte3BindStuParentNum;        //本月登录3次及以上且绑定学生的家长数
    private	Integer	parentStuActiveSettlementNum;	    //家长学生双活结算
    private	Integer	newParentOldStuActiveSettlementNum;	//老学生新家长双活结算
    private	Integer	newParentNewStuActiveSettlementNum;	//新学生新家长双活结算
    private	Integer	newParentActiveSettlementNum;	    //新家长双活结算
    private	Integer	backFlowParentNum;	                //回流家长数
}
