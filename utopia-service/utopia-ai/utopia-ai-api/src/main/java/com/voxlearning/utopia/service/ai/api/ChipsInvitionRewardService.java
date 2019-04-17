package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.concurrent.TimeUnit;

/**
 *  邀请有奖接口
 * @author guangqing
 * @since 2019/3/7
 */
@ServiceVersion(version = "20190307")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsInvitionRewardService extends IPingable {

    /**
     *
     * 返回每个手机号已经邀请多少人
     * 邀请人数大于10人的手机号
     * 授权id
     * @see  ChipsInvitionRewardLoader.loadInvitionActivityTopUser
     * @param activityType  默认值 productId;
     * @return
     */

    @Deprecated
    MapMessage loadInvitionAwardHome(String activityType);


    MapMessage updateInvitionRank(String activityType, Long userId, int num);

    /**
     *
     * @param activityType
     * @param userId
     * @return
     */
    @Deprecated
    MapMessage loadMyReward(String activityType, Long userId);

    /**
     * 已浏览:0,下单未支付:1,成功购买: 2,退款:3
     * @param activityType
     * @param userId
     * @param type
     * @return
     */
    @Deprecated
    MapMessage loadInvitionDetail(String activityType, Long userId, Integer type);

    MapMessage processInvitionPageVisit(String openId, Long inviter, String productId);

}
