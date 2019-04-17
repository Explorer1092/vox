package com.voxlearning.utopia.service.crm.consumer.service.agent.evaluate;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.evaluate.EvaluationRecord;
import com.voxlearning.utopia.service.crm.api.service.agent.evaluate.EvaluationRecordService;

import java.util.Collection;
import java.util.List;

/**
 *
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class EvaluationRecordServiceClient implements EvaluationRecordService {

    @ImportService(interfaceClass = EvaluationRecordService.class)
    private EvaluationRecordService remoteReference;

    @Override
    public String insert(EvaluationRecord evaluateRecord){
        return remoteReference.insert(evaluateRecord);
    }

    @Override
    public List<String> inserts(Collection<EvaluationRecord> evaluationList) {
        return remoteReference.inserts(evaluationList);
    }

}
