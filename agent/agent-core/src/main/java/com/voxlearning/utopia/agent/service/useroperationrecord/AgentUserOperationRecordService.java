package com.voxlearning.utopia.agent.service.useroperationrecord;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.agent.constants.AgentUserOperationType;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserOperationRecordDao;
import com.voxlearning.utopia.agent.persist.entity.AgentUserOperationRecord;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class AgentUserOperationRecordService extends AbstractAgentService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private AgentUserOperationRecordDao agentUserOperationRecordDao;

    /**
     * 添加操作记录
     * @param dataId
     * @param agentUserOperationType
     * @param operationContent
     */
    public void addOperationRecord(String dataId, AgentUserOperationType agentUserOperationType, String operationContent){
        if (StringUtils.isNotBlank(operationContent)){
            AgentUserOperationRecord operationRecord = new AgentUserOperationRecord();
            operationRecord.setDataId(dataId);
            operationRecord.setOperationType(agentUserOperationType);
            operationRecord.setOperatorId(getCurrentUserId());
            operationRecord.setOperatorName(getCurrentUser().getRealName());
            operationRecord.setNote(operationContent);
            agentUserOperationRecordDao.insert(operationRecord);
        }
    }
}
