package com.voxlearning.utopia.agent.bean.exam;


import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContractPayback;
import lombok.Getter;
import lombok.Setter;


/**
 * @author deliang.che
 * @data 2018-05-03
 **/
@Setter
@Getter
public class AgentExamContractPaybackVO extends AgentExamContractPayback {

    private static final long serialVersionUID = -1097268518204201779L;
    private String operatorName;    //操作人姓名

    public String getPaybackNumber(){
        if (super.getId() != null){
            return  String.format("%06d", super.getId());
        }
        return null;
    }

}
