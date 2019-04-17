package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.data.ChipsUserOrderBO;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190306")
@ServiceTimeout(timeout = 120, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsEnglishUserService extends IPingable {
    /**
     *
     * @param chipsOrder
     * @return MapMessage :if mapMessage is success, then contains orderId, orderToken
     *
     */
    MapMessage createOrder(ChipsUserOrderBO chipsOrder);

    /**
     *  开通超级用户和白名单
     * @param mobileSet
     * @param productSet
     * @return
     */
    MapMessage openSuperUser(Set<String> mobileSet, Set<String> productSet);
}
