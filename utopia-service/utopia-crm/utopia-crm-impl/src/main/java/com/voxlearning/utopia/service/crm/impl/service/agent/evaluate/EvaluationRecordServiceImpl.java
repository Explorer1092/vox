package com.voxlearning.utopia.service.crm.impl.service.agent.evaluate;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.evaluate.EvaluationRecord;
import com.voxlearning.utopia.service.crm.api.service.agent.evaluate.EvaluationRecordService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.evaluate.EvaluationRecordDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = EvaluationRecordService.class)
@ExposeService(interfaceClass = EvaluationRecordService.class)
public class EvaluationRecordServiceImpl extends SpringContainerSupport implements EvaluationRecordService {
    @Inject
    EvaluationRecordDao evaluationRecordDao;

    @Override
    public String insert(EvaluationRecord evaluateRecord){
        evaluationRecordDao.insert(evaluateRecord);
        return evaluateRecord.getId();
    }

    @Override
    public List<String> inserts(Collection<EvaluationRecord> evaluationList) {
        if(CollectionUtils.isEmpty(evaluationList)){
            return Collections.emptyList();
        }
        evaluationRecordDao.inserts(evaluationList);
        return evaluationList.stream().map(EvaluationRecord::getId).collect(Collectors.toList());
    }

}
