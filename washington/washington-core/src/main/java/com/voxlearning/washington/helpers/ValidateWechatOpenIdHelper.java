package com.voxlearning.washington.helpers;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.washington.cache.WashingtonCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_LOAD_USER_ERROR;

/**
 * @author shiwei.liao
 * @since 2018-6-11
 */
@Named
public class ValidateWechatOpenIdHelper {

    private static final String parentMobileWechatBindPrefix = "parent_wechat_login_union_id_";

    @Inject
    private WashingtonCacheSystem washingtonCacheSystem;

    public MapMessage validateOpenIdAndUnionId(String requestMobile, String requestOpenId, String requestUnionId) {
        CacheObject<Object> cacheObject = washingtonCacheSystem.CBS.persistence.get(parentMobileWechatBindPrefix + requestMobile);
        if (cacheObject == null || cacheObject.getValue() == null || !(requestUnionId + "_" + requestOpenId).equals(cacheObject.getValue())) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR);
        }
        return MapMessage.successMessage();
    }

    public void storeOpenIdAndUnionId(String requestMobile, String requestOpenId, String requestUnionId) {
        washingtonCacheSystem.CBS.persistence.set(parentMobileWechatBindPrefix + requestMobile, DateUtils.getCurrentToDayEndSecond(), requestUnionId + "_" + requestOpenId);
    }

}
