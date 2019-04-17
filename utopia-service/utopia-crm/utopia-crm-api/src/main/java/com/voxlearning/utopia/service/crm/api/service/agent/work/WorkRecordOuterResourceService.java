package com.voxlearning.utopia.service.crm.api.service.agent.work;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordOuterResource;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * WorkRecordOuterResourceService
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@ServiceVersion(version = "2018.12.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface WorkRecordOuterResourceService extends IPingable {

    String insert(WorkRecordOuterResource workRecordOuterResource);

    List<String> inserts(Collection<WorkRecordOuterResource> workRecordOuterResourceList);

    void updateWorkRecordIdAndType(Collection<String> ids, String workRecordId, AgentWorkRecordType workRecordType);
}
