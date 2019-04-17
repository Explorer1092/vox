package com.voxlearning.utopia.agent.service.sysconfig;

import com.voxlearning.utopia.agent.dao.mongo.AgentProductInventoryRecordDao;
import com.voxlearning.utopia.agent.persist.entity.AgentProductInventoryRecord;
import com.voxlearning.utopia.agent.service.AbstractAgentService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * AgentProductInventoryRecordService
 *
 * @author song.wang
 * @date 2016/11/22
 */
@Named
public class AgentProductInventoryRecordService extends AbstractAgentService {
    @Inject
    private AgentProductInventoryRecordDao agentProductInventoryRecordDao;

    public List<AgentProductInventoryRecord> findByProductId(Long productId){
        return agentProductInventoryRecordDao.findByProductId(productId);
    }


}
