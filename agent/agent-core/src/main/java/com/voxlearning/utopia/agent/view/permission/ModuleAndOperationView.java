package com.voxlearning.utopia.agent.view.permission;

import com.voxlearning.utopia.agent.bean.permission.ModuleAndOperation;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2018/5/17
 */
@Getter
@Setter
public class ModuleAndOperationView extends ModuleAndOperation {

    private boolean selected;
    private boolean systemNotExist;
    private List<AgentRoleType> roleTypeList;

}
