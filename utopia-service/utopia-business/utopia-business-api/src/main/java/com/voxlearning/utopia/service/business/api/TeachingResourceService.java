package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;

import java.util.concurrent.TimeUnit;

/**
 * Created by haitian.gan on 2017/8/3.
 */
@ServiceVersion(version = "1.7")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface TeachingResourceService {

    MapMessage upsertTeachingResource(TeachingResource resource);

    MapMessage receiveTask(Long userId, String resourceId, String type);

    MapMessage checkTask(TeacherResourceTask task);

    MapMessage finishTask(String taskId);

    MapMessage finishUserTask(Long userId, String taskType);

    MapMessage supplyUserTask(Long userId);

    MapMessage addReadCount(String id);

    MapMessage addCollectCount(String id);

    MapMessage addCollect(Long userId, String categorie, String resourceId);

    MapMessage disableCollect(Long userId, String recourceId, String categorie, String collectId);

    MapMessage addHotSearch(String Word);

    /**
     * 把教学资源的作业进度数据从Redis挪到Couchbase
     * @return
     */
    MapMessage moveDataForBackDoor();

    MapMessage fixExpiryData();
}
