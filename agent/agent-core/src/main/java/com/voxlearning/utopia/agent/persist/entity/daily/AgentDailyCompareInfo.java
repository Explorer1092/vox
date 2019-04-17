package com.voxlearning.utopia.agent.persist.entity.daily;

import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *  日报对比实体
 *
 * @author deliang.che
 * @since  2018/12/5
 */

@Getter
@Setter
public class AgentDailyCompareInfo implements Serializable {

    private static final long serialVersionUID = 5946302036885957859L;

    private CrmWorkRecordType workType; //工作记录类型
    private String workRecordId;        //工作记录ID
    private String content;             //内容
    private String status;              //状态  （plan：计划内，noPlan：未计划，unFinish：未完成）
    private Boolean frequentlyVisit;    //是否频繁拜访（进校）
}

