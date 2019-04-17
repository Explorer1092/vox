package com.voxlearning.utopia.service.wechat.api.constants;

import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-09-15 下午3:02
 **/
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MiniProgramType {

     PICLISTEN(
            0,
            "xcx.piclisten.appid",
            "xcx.piclisten.appsecret",
            "MINI_PROGRAM_PICLISTEN_API_ACCESS_TOKEN",
            "WX_TICKET_EXCEED_QUOTA__PICLISTEN",
            "http://cdn.17zuoye.com/static/project/app/publiccode.jpg",
            "微信点读机小程序",
             UserWebSource.miniProgramPicListen
    ),
    TOBBIT(
            1,
            "xcx.oral.arithmetic.appid",
            "xcx.oral.arithmetic.appsecret",
            "MINI_PROGRAM_TOBBIT_API_ACCESS_TOKEN",
            "WX_TICKET_EXCEED_QUOTA_TOBBIT",
            "http://cdn.17zuoye.com/static/project/app/publiccode.jpg",
            "托比口算小程序",
            UserWebSource.miniprogram_tobbit
    ),

    ;

    MiniProgramType(int type, String appId, String appSecret, String accessTokenCacheKey,
                    String ticketExeedQuotaCacheKey, String staticQrcode, String desc, UserWebSource webSource) {
        this.type = type;
        this.appId = appId;
        this.appSecret = appSecret;
        this.accessTokenCacheKey = accessTokenCacheKey;
        this.ticketExeedQuotaCacheKey = ticketExeedQuotaCacheKey;
        this.staticQrcode = staticQrcode;
        this.desc = desc;
        this.webSource = webSource;
    }

    private static Map<Integer, MiniProgramType> types;

    static {
        types = new HashMap<>();
        for (MiniProgramType type : values()) {
            types.put(type.getType(), type);
        }
    }

    public static MiniProgramType of(Integer type) {
        return types.get(type);
    }

    @Getter
    private int type;
    @Getter
    private String appId;
    @Getter
    private String appSecret;
    @Getter
    private String accessTokenCacheKey;
    @Getter
    private String ticketExeedQuotaCacheKey;
    @Getter
    private String staticQrcode;
    @Getter
    private String desc;
    @Getter
    private UserWebSource webSource;
}
