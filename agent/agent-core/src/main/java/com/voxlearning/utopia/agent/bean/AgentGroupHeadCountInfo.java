package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门编制情况
 *
 * @author chunlin.yu
 * @create 2018-04-26 12:16
 **/
@Getter
@Setter
public class AgentGroupHeadCountInfo {

    private Long groupId;

    private String groupName;

    private AgentGroupRoleType roleType;

    private Long parentGroupId;

    private String parentGroupName;

    private Integer headCount;

    private Integer actuallyCount;

    public Integer getWaitingCount(){
        if (null != getActuallyCount() && getHeadCount()!=null){
            return getHeadCount() - getActuallyCount();
        }
        return 0;
    }

    public double getActuallyRate(){
        if (null != getActuallyCount() && getHeadCount()!=null){
            return MathUtils.doubleDivide(getActuallyCount()*100,getHeadCount());
        }
        return 0d;
    }

    public String getRoleTypeName(){
        if (getRoleType() != null){
            return getRoleType().getRoleName();
        }
        return null;
    }
}
