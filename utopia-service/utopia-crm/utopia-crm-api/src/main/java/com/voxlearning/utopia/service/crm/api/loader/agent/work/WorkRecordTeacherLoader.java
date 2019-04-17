package com.voxlearning.utopia.service.crm.api.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordTeacher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WorkRecordTeacherLoader
 *
 * @author deliang.che
 * @since 2018/12/17
 */
@ServiceVersion(version = "2018.12.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface WorkRecordTeacherLoader extends IPingable{

    WorkRecordTeacher load(String id);

    Map<String,WorkRecordTeacher> loads(Collection<String> ids);

    List<WorkRecordTeacher> loadByTeacherId(Long teacherId);
}
