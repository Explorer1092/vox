package com.voxlearning.utopia.service.parent.homework.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.model.CorrectParam;

import java.util.concurrent.TimeUnit;

/**
 * 订正作业服务接口：提供查作业、查题id、上报结果&判题、查结果等订正作业相关功能
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@ServiceVersion(version = "20190322")
@ServiceTimeout(timeout = 7, unit = TimeUnit.SECONDS)
public interface CorrectHomeworkService {

    /**
     * 订正作业
     *
     * @param param
     * @return
     */
    MapMessage dos(CorrectParam param);

}
