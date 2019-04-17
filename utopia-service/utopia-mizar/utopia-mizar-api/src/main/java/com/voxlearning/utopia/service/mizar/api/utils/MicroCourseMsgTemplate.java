package com.voxlearning.utopia.service.mizar.api.utils;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * 用来维护微课堂消息的各种模板
 */
public class MicroCourseMsgTemplate {

    // 短信消息模板
    private static final String SMS_CONTENT = "您在家长通预约的直播课程{}将于{}开始，点击进入{}";

    // 微课堂公众号ID
    private static final Long ACCOUNT_ID = OfficialAccounts.SpecialAccount.MICRO_COURSE.getId();
    // 公众号预约成功推送消息模板
    private static final String MSG_RESERVE_TITLE = "您已成功订阅{}课程";
    private static final String MSG_RESERVE_CONTENT = "您已订阅了{}{}直播课程{}，点击查看详情。";
    // 公众号开课前推送消息模板
    private static final String MSG_TITLE = "您预约的{}课程即将开始";
    private static final String MSG_CONTENT = "您预约的直播课程{}将于{}开始，点击查看课程。";
    // 模板消息公共部分
    private static final String LINK_URL = "/mizar/course/courseperiod.vpage?period={}&track={}";
    private static final String DEFAULT_LINK_URL = "/mizar/course/courseperiod.vpage?period={}&track=weiketang";
    private static final String EXT_INFO = StringUtils.formatMessage("{\"accountsKey\":\"{}\"}", OfficialAccounts.SpecialAccount.MICRO_COURSE.getKey());
    // 微信跳转链接
    private static final String WECHAT_LINK_TEST = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx13914a7ef721ab49&redirect_uri=http%3a%2f%2fwechat.test.17zuoye.net%2fparent_auth.vpage%3fperiod%3d{}%26track%3d{}&response_type=code&scope=snsapi_base&state=micro_course_curriculum#wechat_redirect";
    private static final String WECHAT_LINK_STAGING = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx40f737e021a97870&redirect_uri=http%3a%2f%2fwechat.staging.17zuoye.net%2fparent_auth.vpage%3fperiod%3d{}%26track%3d{}&response_type=code&scope=snsapi_base&state=micro_course_curriculum#wechat_redirect";
    private static final String WECHAT_LINK_PRODUCTION = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx3c30705f9f1d82d1&redirect_uri=https%3a%2f%2fwechat.17zuoye.com%2fparent_auth.vpage%3fperiod%3d{}%26track%3d{}&response_type=code&scope=snsapi_base&state=micro_course_curriculum#wechat_redirect";
    public static final String APP_DEFAULT_TRACK = "17Parent";
    public static final String WECHAT_DEFAULT_TRACK = "tixing";

    public static String sms(String content, Date time,String url) {
        return genMessage(SMS_CONTENT, content, DateUtils.dateToString(time, "HH:mm"), url);
    }

    public static Long accountId() {
        return ACCOUNT_ID;
    }

    public static String reserveTitle(String title) {
        return genMessage(MSG_RESERVE_TITLE, title);
    }

    public static String reserveContent(Date time, String title) {
        if (time == null) {
            return genMessage(MSG_RESERVE_CONTENT, "", "", title);
        }
        return genMessage(MSG_RESERVE_CONTENT, DateUtils.dateToString(time, "MM-dd HH:mm"), "开始的", title);
    }

    public static String title(String title) {
        return genMessage(MSG_TITLE, title);
    }

    public static String content(String name, Date time) {
        return genMessage(MSG_CONTENT, name, DateUtils.dateToString(time, "HH:mm"));
    }

    public static String appLinkUrl(String id) {
        return genMessage(LINK_URL, id, APP_DEFAULT_TRACK);
    }

    public static String wechatLinkUrl(Mode runtime, String id) {
        return genMessage(wechatTemplate(runtime), id, WECHAT_DEFAULT_TRACK);
    }

    public static String linkUrl(String track, String id) {
        if (StringUtils.isBlank(track)) return genMessage(DEFAULT_LINK_URL, id);
        return genMessage(LINK_URL, id, track);
    }

    public static String extInfo() {
        return EXT_INFO;
    }

    private static String genMessage(String template, Object... params) {
        return StringUtils.formatMessage(template, params);
    }

    private static String wechatTemplate(Mode runtime) {
        if (runtime == null) {
            throw new RuntimeException("Cannot fetch current Mode!");
        }
        switch (runtime) {
            case PRODUCTION:
                return WECHAT_LINK_PRODUCTION;
            case STAGING:
                return WECHAT_LINK_STAGING;
            default:
                return WECHAT_LINK_TEST;
        }
    }


    private static void debugInput() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
//        map.put("sms()", sms("[XXXX]", new Date()));
        map.put("accountId()", accountId());
        map.put("title()", title("[XXXX]"));
        map.put("content()", content("[XXXX]", new Date()));
        map.put("reserveTitle()", reserveTitle("[XXXX]"));
        map.put("reserveContent()", reserveContent(null, "[XXXX]"));
        map.put("extInfo()", extInfo());
        map.put("wechatLinkUrl()[Mode.TEST]", wechatLinkUrl(Mode.TEST, "58c2663012a0cb8e74ba2cad"));
        map.put("wechatLinkUrl()[Mode.STAGING]", wechatLinkUrl(Mode.STAGING, "58c2663012a0cb8e74ba2cad"));
        map.put("wechatLinkUrl()[Mode.PRODUCTION]", wechatLinkUrl(Mode.PRODUCTION, "58c2663012a0cb8e74ba2cad"));
        System.out.println(String.format(" %16s | %20s  ", "Method", "Value"));
        System.out.println("      ------------+--------------------------------------");
        map.entrySet().forEach(e -> System.out.println(String.format(" %16s | %2s ", e.getKey(), e.getValue())));
    }

    public static void main(String[] args) {
        debugInput();
    }
}
