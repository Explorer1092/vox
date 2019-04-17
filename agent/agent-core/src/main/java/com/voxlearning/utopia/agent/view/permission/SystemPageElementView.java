package com.voxlearning.utopia.agent.view.permission;

import com.voxlearning.utopia.agent.persist.entity.permission.SystemPageElement;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2018/5/22
 */
@Getter
@Setter
public class SystemPageElementView extends SystemPageElement {
    private boolean selected;
    private List<AgentRoleType> roleTypeList;
}
