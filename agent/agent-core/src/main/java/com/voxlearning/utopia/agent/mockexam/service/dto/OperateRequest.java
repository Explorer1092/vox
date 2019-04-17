package com.voxlearning.utopia.agent.mockexam.service.dto;

import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 基本操作请求
 *
 * @author xiaolei.li
 * @version 2018/8/7
 */
@Data
public class OperateRequest implements Serializable {

    /**
     * 业务主键
     */
    protected Long id;

    /**
     * 操作人id
     */
    protected Long operatorId;

    /**
     * 操作人名称
     */
    protected String operatorName;

    /**
     * 操作人角色列表
     */
    protected List<Integer> operatorRoles;

    /**
     * 判断是否为管理员
     *
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        if (null == operatorRoles)
            return false;
        else
            return operatorRoles.contains(AgentRoleType.Admin.getId())
                    || operatorRoles.contains(AgentRoleType.Country.getId())
                    || operatorRoles.contains(AgentRoleType.MOCK_EXAM_MANAGER.getId());
    }
}
