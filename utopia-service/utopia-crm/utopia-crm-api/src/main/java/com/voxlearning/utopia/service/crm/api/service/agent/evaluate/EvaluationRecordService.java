package com.voxlearning.utopia.service.crm.api.service.agent.evaluate;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.evaluate.EvaluationRecord;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@ServiceVersion(version = "2018.12.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface EvaluationRecordService extends IPingable {

    String insert(EvaluationRecord evaluateRecord);

    List<String> inserts(Collection<EvaluationRecord> evaluationList);
}
