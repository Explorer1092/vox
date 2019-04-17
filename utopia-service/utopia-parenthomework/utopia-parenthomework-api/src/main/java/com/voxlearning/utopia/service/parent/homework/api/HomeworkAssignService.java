package com.voxlearning.utopia.service.parent.homework.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;

import java.util.concurrent.TimeUnit;

/**
 * 作业服务接口：提供布置作业功能
 *
 * @author Wenlong Meng
 * @version 20181111
 */
@ServiceVersion(version = "20181111")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
public interface HomeworkAssignService {

    /**
     * 布置作业
     *
     * @param homeworkParam
     * @return
     */
    MapMessage assignHomework(HomeworkParam homeworkParam);

}
