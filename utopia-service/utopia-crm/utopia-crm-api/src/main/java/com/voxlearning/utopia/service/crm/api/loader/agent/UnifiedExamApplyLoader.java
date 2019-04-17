package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.UnifiedExamApply;

import java.util.concurrent.TimeUnit;

/**
 * Created by dell on 2017/4/17.
 */
@ServiceVersion(version = "20170417")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface UnifiedExamApplyLoader extends IPingable {
    @Idempotent
    UnifiedExamApply findByWorkflowId(Long workflowId);

    @Idempotent
    UnifiedExamApply load(Long id);
}
