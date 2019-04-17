package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContract;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContractExtend;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContractSplitSetting;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author chunlin.yu
 * @create 2018-03-13 13:33
 **/
@Setter
@Getter
public class AgentExamContractVO extends AgentExamContract {

    private static final long serialVersionUID = -2357827957731660033L;

    private String schoolName;

    private String contractorName;

    private String cmainName;               //学校主干名

    private String schoolPopularityType;    //学校等级

    private String userName;                //专员姓名

    private List<AgentExamContractSplitSetting> splitSettingList;//分成设置list

    private List<String> imageUrlList;                                  //合同照片URL

    public String getContractTypeDesc(){
        if (null != getContractType()){
            return getContractType().getDesc();
        }
        return null;
    }

    public String getContractNumber(){
        if (super.getId() != null){
            return  String.format("%05d", super.getId());
        }
        return null;
    }


    public AgentExamContract toAgentLargeExamContract(){
        AgentExamContract contract = new AgentExamContractVO();
        try {
            BeanUtils.copyProperties(contract,this);
        } catch (Exception e) {
            return null;
        }
        return contract;
    }
}
