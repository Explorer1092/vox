package com.voxlearning.utopia.agent.view.permission;

import com.voxlearning.utopia.agent.persist.entity.permission.SystemOperation;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * SystemOperationView
 *
 * @author song.wang
 * @date 2018/6/11
 */
@Getter
@Setter
public class SystemOperationView extends SystemOperation {
    private boolean selected;
    private List<AgentRoleType> roleTypeList;
}
