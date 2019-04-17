package com.voxlearning.utopia.service.crm.api.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordSchool;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WorkRecordSchoolLoader
 *
 * @author deliang.che
 * @since 2018/12/17
 */
@ServiceVersion(version = "2018.12.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface WorkRecordSchoolLoader extends IPingable{

    WorkRecordSchool load(String id);

    Map<String,WorkRecordSchool> loads(Collection<String> ids);

    List<WorkRecordSchool> findByWorkersAndTime(Collection<Long> userIds, Date startDate,Date endDate);

    List<WorkRecordSchool> findBySchoolId(Long schoolId);
}
