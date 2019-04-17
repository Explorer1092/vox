package com.voxlearning.utopia.service.campaign.impl.support;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.WarmHeartPlanConstant;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

@Named
public class CacheExistsUtils {

    @Inject
    private CampaignCacheSystem campaignCacheSystem;

    public boolean exists(CacheExistsEnum existsEnum, Object key) {
        return campaignCacheSystem.CBS.storage.get(existsEnum.getKey() + SafeConverter.toString(key)).containsValue();
    }

    public boolean noExists(CacheExistsEnum existsEnum, Object key) {
        return !exists(existsEnum, key);
    }

    public void set(CacheExistsEnum existsEnum, Object key) {
        campaignCacheSystem.CBS.storage.set(existsEnum.getKey() + SafeConverter.toString(key), calcExpire(), 0);
    }

    private int calcExpire() {
        long expire = WarmHeartPlanConstant.WARM_HEART_PLAN_END_TIME.getTime() - new Date().getTime();
        return (int) (expire / 1000);
    }
}
