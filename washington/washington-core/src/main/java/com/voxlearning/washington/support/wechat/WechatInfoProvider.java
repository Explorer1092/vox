package com.voxlearning.washington.support.wechat;


import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.washington.cache.WechatCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WechatInfoProvider {

    private static final Logger log = LoggerFactory.getLogger(WechatInfoProvider.class);

    private WechatInfoProvider() {
    }

    public static final WechatInfoProvider INSTANCE = new WechatInfoProvider();

    public String appId() {
        String appid = ProductConfig.get("wechat.teacher.appid");
        if (StringUtils.isEmpty(appid)) {
            log.error("wechat.teacher.appid isEmpty");
        }
        return appid;
    }

    public String secret() {
        String appsecret = ProductConfig.get("wechat.teacher.appsecret");
        if (StringUtils.isEmpty(appsecret)) {
            log.error("wechat.teacher.appsecret isEmpty");
        }
        return appsecret;
    }

    public UtopiaCache cache() {
        return WechatCacheManager.INSTANCE.getCache();
    }

}