package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.dao.mongo.AgentRegionMessageDao;
import com.voxlearning.utopia.agent.persist.entity.AgentRegionMessage;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * AgentRegionMessageService
 *
 * @author song.wang
 * @date 2016/7/28
 */
@Named
public class AgentRegionMessageService extends AbstractAgentService {

    @Inject
    private AgentRegionMessageDao agentRegionMessageDao;
    @Inject
    private BaseOrgService baseOrgService;

    public void saveRegionMessage(Long groupId, String message){
        AgentRegionMessage regionMessage = new AgentRegionMessage();
        regionMessage.setGroupId(groupId);
        regionMessage.setMessage(message);
        agentRegionMessageDao.insert(regionMessage);
    }

    public AgentRegionMessage findRegionMessageForUser(Long userId){
        List<Long> groupIdList = baseOrgService.getGroupListByRole(userId, AgentGroupRoleType.Region);
        if(CollectionUtils.isEmpty(groupIdList)){
            return null;
        }
        List<AgentRegionMessage> regionMessageList = agentRegionMessageDao.findByGroupId(groupIdList.get(0));
        AgentRegionMessage regionMessage;
        if(CollectionUtils.isEmpty(regionMessageList)){
            regionMessage = new AgentRegionMessage();
            regionMessage.setGroupId(groupIdList.get(0));
            regionMessage.setMessage("");
        }else {
            Collections.sort(regionMessageList, (o1, o2) -> {
                if(o1.getCreateTime() == null){
                    return 1;
                }
                if(o2.getCreateTime() == null){
                    return -1;
                }
                if(o1.getCreateTime().after(o2.getCreateTime())){
                    return -1;
                }else if(o1.getCreateTime().before(o2.getCreateTime())){
                    return 1;
                }else {
                    return 0;
                }
            });

            regionMessage = regionMessageList.get(0);
        }
        return regionMessage;
    }





}
