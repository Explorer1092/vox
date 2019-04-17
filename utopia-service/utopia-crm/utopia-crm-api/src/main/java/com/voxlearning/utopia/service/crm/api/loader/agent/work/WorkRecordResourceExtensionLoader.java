package com.voxlearning.utopia.service.crm.api.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordResourceExtension;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WorkRecordResourceExtensionLoader
 *
 * @author deliang.che
 * @since 2019/1/18
 */
@ServiceVersion(version = "2018.12.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface WorkRecordResourceExtensionLoader extends IPingable{

    WorkRecordResourceExtension load(String id);

    Map<String,WorkRecordResourceExtension> loads(Collection<String> ids);

    List<WorkRecordResourceExtension> loadByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate);
}
