package com.voxlearning.utopia.service.crm.consumer.loader.agent.evaluate;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.evaluate.EvaluationRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.evaluate.EvaluationRecordLoader;

import java.util.Collection;
import java.util.Map;

/**
 *
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class EvaluationRecordLoaderClient implements EvaluationRecordLoader {

    @ImportService(interfaceClass = EvaluationRecordLoader.class)
    private EvaluationRecordLoader remoteReference;

    @Override
    public EvaluationRecord load(String id){
        return remoteReference.load(id);
    }

    @Override
    public Map<String, EvaluationRecord> loads(Collection<String> ids){
        return remoteReference.loads(ids);
    }

}
