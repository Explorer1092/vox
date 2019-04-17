package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
@ServiceVersion(version = "20190201")
public interface GansuTelecomService {

    /**
     * 创建账号，开通服务
     *
     * @param appKey
     * @param mobileList
     * @return
     */
    Map<String, String> openService(String appKey, List<String> mobileList);

    /**
     * 关闭服务
     *
     * @param mobileList
     * @return
     */
    Map<String, String> closeService(String appKey, List<String> mobileList);

    /**
     * 查询有效人数
     *
     * @param appKey
     * @param date
     * @return
     */
    Long loadEffectiveUser(String appKey, String date);
}
