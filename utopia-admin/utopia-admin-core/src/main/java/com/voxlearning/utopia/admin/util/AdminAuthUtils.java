package com.voxlearning.utopia.admin.util;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.admin.cache.AdminCacheSystem;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * Admin Auth Utils Class
 * Created by alex on 2016/10/24.
 */
@Named
public class AdminAuthUtils {

    public static final String CACHE_KEY_ENC = "admin_cache_enc";
    public static final String DEFAULT_ENC_CODE = "Admin_ENC_20151202";
    public static final String CacheKey_AuthCurrentUser = "Admin_User_Cache_";

    @Inject private AdminCacheSystem adminCacheSystem;

    public String getUserSign(Long userId) {
        String encryptionCode = getEncryptionCode();
        return generateSign(userId, encryptionCode);
    }

    public boolean isSignValid(String sign, String userId) {
        int index = sign.indexOf(".");
        if (index < 0) {
            return false;
        }
        String cookieSign = sign.substring(0, index);
        String signTime = sign.substring(index + 1);
        String encryptionCode = getEncryptionCode();
        String newSign = DigestUtils.sha1Hex(userId + "." + signTime + encryptionCode);
        //校验签名
        if (!cookieSign.equals(newSign)) {
            return false;
        }
        //校验时间
        Date signDate = new Date(SafeConverter.toLong(signTime));
        if (DateUtils.dayDiff(signDate, new Date()) != 0) {
            return false;
        }
        return true;
    }

    public String getPreCacheKey_AuthCurrentUser(){
        return  CacheKey_AuthCurrentUser;
    }

    private String generateSign(Long userId, String code) {
        long time = System.currentTimeMillis();
        String userString = userId + "." + time + code;
        String sign = DigestUtils.sha1Hex(userString) + "." + time;
        return sign;
    }

    private String getEncryptionCode() {
        String cacheCode = SafeConverter.toString(adminCacheSystem.CBS.storage.get(CACHE_KEY_ENC).getValue());
        if (StringUtils.isNotBlank(cacheCode)) {
            return cacheCode;
        } else {
            //cache取不到的时候尝试写一次缓存。再读一次来判断是缓存宕掉了还是仅仅只是初始化
            //1、缓存宕掉了用default_Pre_EncryptionCode
            //2、缓存初始化则用随机的code
            String newCacheCode = RandomUtils.randomString(6);
            adminCacheSystem.CBS.storage.add(CACHE_KEY_ENC, DateUtils.getCurrentToDayEndSecond(), newCacheCode);
            String tmpCode = SafeConverter.toString(adminCacheSystem.CBS.storage.get(CACHE_KEY_ENC).getValue());
            if (StringUtils.isNotBlank(tmpCode)) {
                return newCacheCode;
            } else {
                return DEFAULT_ENC_CODE;
            }
        }
    }

}
