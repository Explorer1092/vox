package com.voxlearning.utopia.service.business.impl.utils;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.user.api.entities.BusinessActivity;

import java.util.Map;

/**
 * 用来维护通用支付微信模板消息
 */
public class SeattleWechatMsgUtils {

    // 公众号预约成功推送消息模板
    private static final String MSG_TYPE = "您已成功购买了课程{}";

    // 微信跳转链接
    private static final String WECHAT_LINK_TEST = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx13914a7ef721ab49&redirect_uri=http%3a%2f%2fwechat.test.17zuoye.net%2fparent_auth.vpage%3fid%3d{}%26track%3dafterbuy&response_type=code&scope=snsapi_base&state=nt_seattle#wechat_redirect";
    private static final String WECHAT_LINK_STAGING = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx40f737e021a97870&redirect_uri=http%3a%2f%2fwechat.staging.17zuoye.net%2fparent_auth.vpage%3fid%3d{}%26track%3dafterbuy&response_type=code&scope=snsapi_base&state=nt_seattle#wechat_redirect";
    private static final String WECHAT_LINK_PRODUCTION = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx3c30705f9f1d82d1&redirect_uri=https%3a%2f%2fwechat.17zuoye.com%2fparent_auth.vpage%3fid%3d{}%26track%3dafterbuy&response_type=code&scope=snsapi_base&state=nt_seattle#wechat_redirect";

    public static Map<String, Object> extensionInfo(BusinessActivity activity, Mode runtime) {
        return MapUtils.m(
                "first", "",
                "keyword1", "您已成功购买了课程" + activity.getProductName(),
                "keyword2", activity.getWechatContent(),
                "remark", "点击查看",
                "url", wechatLinkUrl(runtime, activity.getId())
        );
    }

    private static String wechatLinkUrl(Mode runtime, Long id) {
        if (runtime == null) {
            throw new RuntimeException("Cannot fetch current Mode!");
        }
        String wechatLink;
        switch (runtime) {
            case PRODUCTION:
                wechatLink = WECHAT_LINK_PRODUCTION;
                break;
            case STAGING:
                wechatLink = WECHAT_LINK_STAGING;
                break;
            default:
                wechatLink = WECHAT_LINK_TEST;
                break;
        }
        return StringUtils.formatMessage(wechatLink, id);
    }

}
