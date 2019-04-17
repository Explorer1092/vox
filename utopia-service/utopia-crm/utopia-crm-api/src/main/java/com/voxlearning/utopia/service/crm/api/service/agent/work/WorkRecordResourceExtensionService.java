package com.voxlearning.utopia.service.crm.api.service.agent.work;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordResourceExtension;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * WorkRecordResourceExtensionService
 *
 * @author deliang.che
 * @since  2019/1/19
 */
@ServiceVersion(version = "2019.1.19")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface WorkRecordResourceExtensionService extends IPingable {

    String insert(WorkRecordResourceExtension workRecordResourceExtension);

    List<String> inserts(Collection<WorkRecordResourceExtension> workRecordResourceExtensionList);
}
