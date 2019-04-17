package com.voxlearning.utopia.service.crm.api.service.agent.work;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordSchool;

import java.util.concurrent.TimeUnit;

/**
 * WorkRecordSchoolService
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@ServiceVersion(version = "2018.12.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface WorkRecordSchoolService extends IPingable {
    String insert(WorkRecordSchool workRecordSchool);
}
