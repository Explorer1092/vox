package com.voxlearning.utopia.agent.persist.entity.exam;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * 大考合同分成设置类
 *
 * @author deliang.che
 * @date 2018-05-02
 **/

@Getter
@Setter
public class AgentExamContractSplitSetting implements Serializable {
    private static final long serialVersionUID = -412364721393404081L;
    public static final Integer  MAIN_CONTRACTOR = 1;   //主签约人标识
    private Long contractorId;      //签约人ID
    private String contractorName;  //签约人姓名
    private Double splitProportion; //分成比例
    private Integer contractorFlag; //签约人标识(1:主  0：合作)
}
