package com.voxlearning.utopia.service.crm.api.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkSupporter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WorkSupporterLoader
 *
 * @author deliang.che
 * @since 2018/12/17
 */
@ServiceVersion(version = "2018.12.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface WorkSupporterLoader extends IPingable{

    WorkSupporter load(String id);

    Map<String,WorkSupporter> loads(Collection<String> ids);
}
