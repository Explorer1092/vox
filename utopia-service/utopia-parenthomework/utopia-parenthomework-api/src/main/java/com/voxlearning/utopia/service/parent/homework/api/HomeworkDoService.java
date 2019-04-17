package com.voxlearning.utopia.service.parent.homework.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;

import java.util.concurrent.TimeUnit;

/**
 * 做作业服务接口：提供查作业、查题id、上报结果&判题、查结果等做作业相关功能
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-21
 */
@ServiceVersion(version = "20181111")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
public interface HomeworkDoService {

    /**
     * 做作业
     *
     * @param param
     * @return
     */
    MapMessage dos(HomeworkParam param);
}
