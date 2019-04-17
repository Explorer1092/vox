package com.voxlearning.utopia.agent.service.mobile.workrecord;

import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.service.crm.api.constants.agent.EvaluationBusinessType;
import com.voxlearning.utopia.service.crm.api.constants.agent.EvaluationIndicator;
import com.voxlearning.utopia.service.crm.api.entities.agent.evaluate.EvaluationRecord;
import com.voxlearning.utopia.service.crm.consumer.service.agent.evaluate.EvaluationRecordServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

@Named
public class EvaluateRecordService extends AbstractAgentService {
    @Inject
    private EvaluationRecordServiceClient evaluateRecordServiceClient;

    /**
     * 添加评价记录
     * @param recordId
     * @param businessType
     * @param workerId
     * @param workerName
     * @param evaluateIndicator
     * @param score
     */
    public void addEvaluateRecord(String recordId, String businessType, Long workerId, String workerName, EvaluationIndicator evaluateIndicator, Integer score){
        EvaluationRecord evaluateRecord = new EvaluationRecord();
        evaluateRecord.setBusinessRecordId(recordId);
        evaluateRecord.setBusinessType(EvaluationBusinessType.valueOf(businessType));
        evaluateRecord.setTargetUserId(workerId);
        evaluateRecord.setTargetUserName(workerName);
        evaluateRecord.setEvaluateTime(new Date());

        evaluateRecord.setIndicator(evaluateIndicator);
        evaluateRecord.setResult(score);
        evaluateRecordServiceClient.insert(evaluateRecord);
    }
}
