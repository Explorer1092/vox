package com.voxlearning.utopia.service.crm.impl.loader.agent.evaluate;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.evaluate.EvaluationRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.evaluate.EvaluationRecordLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.evaluate.EvaluationRecordDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

/**
 *
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = EvaluationRecordLoader.class)
@ExposeService(interfaceClass = EvaluationRecordLoader.class)
public class EvaluationRecordLoaderImpl extends SpringContainerSupport implements EvaluationRecordLoader {

    @Inject
    EvaluationRecordDao evaluationRecordDao;

    @Override
    public EvaluationRecord load(String id){
        return evaluationRecordDao.load(id);
    }

    @Override
    public Map<String, EvaluationRecord> loads(Collection<String> ids){
        return evaluationRecordDao.loads(ids);
    }

}
