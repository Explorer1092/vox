package com.voxlearning.utopia.service.vendor.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.vendor.api.entity.VendorUserRef;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
@ServiceVersion(version = "20190201")
public interface VendorUserLoader {

    Long queryEffectiveUser(String appKey, String date);

    /**
     * 分页查询用户id大于minUserId的用户
     * @param appkey
     * @param minUserId
     * @param limit:最大不能超过1000
     * @return
     */
    List<VendorUserRef> countEffectiveUser(String appkey, Long minUserId, int limit);
}
