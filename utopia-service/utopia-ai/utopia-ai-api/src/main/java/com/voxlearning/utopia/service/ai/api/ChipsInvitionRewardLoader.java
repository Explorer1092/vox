package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190328")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsInvitionRewardLoader extends IPingable {

    MapMessage loadInvitionConfig();

    MapMessage loadInvitionConfigDetail(Long userId);

    MapMessage loadInvitionIndexData(Long userId);

    /**
     * 查询邀请活动成功人数排名前列的用户
     * @return
     */
    MapMessage loadInvitionActivityTopUser(String activityType);

    /**
     *
     * @param userId
     * @return
     */
    MapMessage loadMyReward(Long userId);


    /**
     * @param userId
     * @param type 已浏览:0,下单未支付:1,成功购买: 2,退款:3
     * @return
     */
    MapMessage loadInvitionDetail(Long userId, Integer type);
}
