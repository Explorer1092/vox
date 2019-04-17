package com.voxlearning.utopia.service.crm.api.loader.agent.evaluate;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.evaluate.EvaluationRecord;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author deliang.che
 * @since 2018/12/17
 */
@ServiceVersion(version = "2018.12.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface EvaluationRecordLoader extends IPingable{

    EvaluationRecord load(String id);

    Map<String, EvaluationRecord> loads(Collection<String> ids);
}
