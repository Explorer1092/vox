package com.voxlearning.utopia.service.vendor.api.constant;

/**
 * @author shiwe.liao
 * @since 2015/12/21
 */
public enum JpushUserTag {
    USER_ALL_ONLY_FOR_PARENT("user_all"),
    //下面这3个tag.由于客户端一直用的Ktwelve的name来注册的。jpush那边区分大小写。所以遗留下来成大写了
    //2.4版本改成服务器返回了。所以2.4成主流之后就可以都改成小写了
    INFANT_SCHOOL("INFANT_SCHOOL"),
    PRIMARY_SCHOOL("PRIMARY_SCHOOL"),
    JUNIOR_SCHOOL("JUNIOR_SCHOOL"),
    SENIOR_SCHOOL("SENIOR_SCHOOL"),
    PROVINCE("province_"),
    CITY("city_"),
    COUNTY("county_"),
    SCHOOL("school_"),
    CLAZZ_LEVEL("clazz_level_"),
    CLAZZ("clazz_"),
    ClAZZ_GROUP("group_"),
    VERSION("version_"),
    SUBJECT("subject_"),
    AUTH("auth_"),//老师用的是否已认证,auth_AuthenticationState.name().   eg:auth_SUCCESS auth_WATIING
    PAYMENT_BLACK_LIST("va_payment_blacklist"),//StudentDetail.inPaymentBlackListRegion = true
    NON_ANY_BLACK_LIST("va_none_blacklist"),//StudentDetail.inPaymentBlackListRegion = false
    OFFICIAL_ACCOUNT_FOLLOW("oa_follow_"), // 公众号关注tag 家长通 后面拼接公众号ID或者key
    @Deprecated
    REFACTOR_PUSH_VERSION("refactor_push_version"),//去掉了环信的版本tag
    CLAZZ_GROUP_REFACTOR("group_refactor_"),//去掉了环信的版本tag
    ;
    public String tag;

    JpushUserTag(String tag) {
        this.tag = tag;
    }

    public String generateTag(String tagValue) {
        return this.tag + tagValue;
    }

}
