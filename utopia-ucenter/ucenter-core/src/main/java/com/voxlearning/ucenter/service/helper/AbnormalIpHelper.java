package com.voxlearning.ucenter.service.helper;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.utopia.service.config.api.util.BadIpChecker;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhouwei on 2018/11/8
 **/
@Slf4j
public class AbnormalIpHelper {

    private static final String VOICE_VERIFY_CODE_IP = "voice_verify_code_ip";

    private static final String VOICE_VERIFY_CODE_ = "voice_verify_code_";

    private static Cache cache = CacheSystem.CBS.getCache("storage");

    /**
     * 校验获取语音短信验证码的功能是否异常
     *
     * 发现语音验证码有被攻击或者被运营商恶意调用的嫌疑，这里做一个数据统计
     * 处理：如果同一个号码，当天内，被5个不同的IP调用过语音短信的功能，则将这5个IP设置为黑名单，直接返回错误，请联系客服人员
     *
     * @param ip        调用IP
     * @param mobile    手机号码
     * @return
     */
    public static MapMessage checkVoiceVerifyCodeIP(String ip, String mobile) {
        BadIpChecker badIpChecker = BadIpChecker.getVoiceVerifyCodeIps();
        if (badIpChecker.contains(ip) || checkCacheVoiceVerifyCodeIp(ip)) {//如果判断为恶意IP，则直接返回错误
            return MapMessage.errorMessage("获取语音验证码异常，请联系客服400-160-1717");
        }
        if (MobileRule.isMobile(mobile) && !IpHelper.internalIp(ip)) {//语音验证码，并且手机号码合法，并且不是内网IP
            int expiration = DateUtils.getCurrentToDayEndSecond();
            String key = VOICE_VERIFY_CODE_ + mobile;
            Set<String> ips = cache.load(key);
            if (ips == null) {
                ips = new HashSet<>();
            }
            ips.add(ip);
            cache.set(key, expiration, ips);
            if (ips.size() >= 5) {
                log.info("tmsignsvc voice error ips: {}", ips);//需要定期将IP维护至BadIpChecker
                saveCacheVoiceVerifyCodeIp(ips);//将IP存入缓存
                return MapMessage.errorMessage("获取语音验证码异常，请联系客服400-160-1717");
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 将语音验证码的嫌疑IP先存入公用的缓存中，用于判断
     * @param ipsNew
     */
    private static void saveCacheVoiceVerifyCodeIp(Set<String> ipsNew) {
        Set<String> ips = cache.load(VOICE_VERIFY_CODE_IP);
        if (ips == null) {
            ips = new HashSet<>();
        }
        ips.addAll(ipsNew);
        //因为IP收集到文本中需要时间，无法实时收集，现将IP存入缓存中，用于过滤，过期时间为14天
        cache.set(VOICE_VERIFY_CODE_IP, 14 * 24 * 60 * 60, ips);
    }

    /**
     * 检查一下IP是否在缓存中的语音验证的嫌疑IP中
     * @param ip
     * @return
     */
    private static boolean checkCacheVoiceVerifyCodeIp(String ip) {
        Set<String> ips = cache.load(VOICE_VERIFY_CODE_IP);
        if (ips == null) {
            return false;
        }
        return ips.contains(ip);
    }

}
