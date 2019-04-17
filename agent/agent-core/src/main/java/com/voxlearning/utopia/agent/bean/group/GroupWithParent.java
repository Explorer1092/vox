package com.voxlearning.utopia.agent.bean.group;

import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 *
 *
 * @author song.wang
 * @date 2018/9/18
 */
@Getter
@Setter
public class GroupWithParent extends AgentGroup {

    private GroupWithParent parent;

    public static class Builder{
        public static GroupWithParent build(AgentGroup group){
            GroupWithParent groupWithParent = new GroupWithParent();
            BeanUtils.copyProperties(group, groupWithParent);
            return groupWithParent;
        }
    }
}
