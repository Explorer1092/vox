package com.voxlearning.utopia.service.vendor.api.constant;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author peng.zhang.a
 * @since 16-7-14
 */
@Getter
public enum FairylandProductRedirectType {

    THIRD_APP("第三方app(需要sessionKey)", "/app/redirect/thirdApp.vpage"),
    SELF_APP("自家app(直接跳转)", "/app/redirect/selfApp.vpage");

    static final public Map<String, String> map = new HashMap();
    static final public String MID_PAGE_URL = "/app/redirect/openapp.vpage";
    static final public String JUMP_URL = "/app/redirect/jump.vpage";

    static {
        for (FairylandProductRedirectType type : FairylandProductRedirectType.values()) {
            map.put(type.name(), type.desc);
        }
    }

    private String desc;
    private String url;

    FairylandProductRedirectType(String desc, String url) {
        this.desc = desc;
        this.url = url;
    }

    public static FairylandProductRedirectType of(String redirectTypeStr) {
        try {
            return FairylandProductRedirectType.valueOf(redirectTypeStr);
        } catch (Exception e) {
            return FairylandProductRedirectType.SELF_APP;
        }
    }

    public static String buildMidUrl(String appKey, FairyLandPlatform platform) {
        return MID_PAGE_URL + "?appKey=" + appKey + "&platform=" + platform.name();
    }

    /**
     * 应用的跳转地址
     */
    public static String fetchAppRedirectUrl(String appKey, String platform, String productType) {
        return JUMP_URL + "?appKey=" + appKey + "&platform=" + platform + "&productType=" + productType;
    }

    @Deprecated
    public String buildUrl(String appKey, String version, String platform, String productType) {
        String retUrl = JUMP_URL + "?appKey=" + appKey + "&platform=" + platform + "&productType=" + productType;
        if (StringUtils.isNotBlank(version)) {
            retUrl += "&version=" + version;
        }
        return retUrl;
    }
}

