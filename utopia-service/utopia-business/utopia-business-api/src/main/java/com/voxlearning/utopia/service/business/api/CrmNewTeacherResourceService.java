package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.api.entity.NewTeacherResource;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceRetries
@ServiceVersion(version = "20190315")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface CrmNewTeacherResourceService {

    List<NewTeacherResource> loadAll();

    NewTeacherResource load(String id);

    MapMessage upsert(NewTeacherResource newTeacherResource);

    NewTeacherResource disabled(String id);

    NewTeacherResource onlineOffline(String id);

    MapMessage syncTeacherCoursewareData();

    MapMessage fixBookName();

}
