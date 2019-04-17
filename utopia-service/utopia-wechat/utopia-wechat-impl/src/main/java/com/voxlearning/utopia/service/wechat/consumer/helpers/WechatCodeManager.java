/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.wechat.consumer.helpers;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatLoader;
import com.voxlearning.galaxy.service.wechat.api.util.ParentWechatInfoProvider;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.*;

/**
 * Wechat code manager helper.
 *
 * @author Xin Xin
 * @author Shuai Huan
 * @author Zhilong Hu
 * @author Yongji Yin
 * @author Xiaohai Zhang
 * @since Apr 29, 2014 7:11pm
 */
@Slf4j
@Named("com.voxlearning.utopia.service.wechat.consumer.helpers.WechatCodeManager")
public class WechatCodeManager implements InitializingBean {

    @ImportService(interfaceClass = DPWechatLoader.class)
    private DPWechatLoader dpWechatLoader;

    private UtopiaCache flushable;
    private UtopiaCache persistence;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.flushable = CacheSystem.CBS.getCache("flushable");
        this.persistence = CacheSystem.CBS.getCache("persistence");
    }

    public String generateAccessToken(WechatType wechatType) {
        if (wechatType == null) {
            throw new UtopiaRuntimeException("微信类型为空");
        }
        if (wechatType == WechatType.PARENT) {
            return dpWechatLoader.getAccessToken(ParentWechatInfoProvider.INSTANCE.wechatInfoContext());
        }

        String key = wechatType.getAccessTokenCacheKey() + "_" + ProductConfig.get(wechatType.getAppId());
        String lockKey = "LOCK_" + key;

        CacheObject<String> accessTokenCache = flushable.get(key);
        if (null != accessTokenCache && null != accessTokenCache.getValue()) {
            return accessTokenCache.getValue();
        }

        try {
            AtomicLockManager.instance().acquireLock(lockKey);
        } catch (CannotAcquireLockException ex) {
            return null;
        }

        try {
            accessTokenCache = flushable.get(key);
            if (null != accessTokenCache && null != accessTokenCache.getValue()) {
                return accessTokenCache.getValue();
            }

            String accessToken = generateAccessToken_impl(wechatType);
            if (StringUtils.isBlank(accessToken)) return null;

            flushable.set(key, 6000, accessToken);
            return accessToken;
        } finally {
            AtomicLockManager.instance().releaseLock(lockKey);
        }
    }

    public String generateAccessToken_impl(WechatType wechatType) {
        String access_token;

        //生成新token
        Map<String, String> param = new HashMap<>();
        param.put("grant_type", "client_credential");
        param.put("appid", ProductConfig.get(wechatType.getAppId()));
        param.put("secret", ProductConfig.get(wechatType.getAppSecret()));

        String URL = UrlUtils.buildUrlQuery(ProductConfig.get("wechat.access_token_url"), param);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();

        if (response.hasHttpClientException()) {
            log.error("generateAccessToken_impl failed, url:{}, wechat type:{}", URL, wechatType);
            //noinspection ThrowableResultOfMethodCallIgnored
            throw new UtopiaRuntimeException("生成access_token请求失败，" + response.getHttpClientExceptionMessage(), response.getHttpClientException());
        }
        Map<String, String> map = JsonUtils.fromJsonToMapStringString(response.getResponseString());
        if (map != null && map.containsKey("access_token")) {
            access_token = map.get("access_token");
        } else {
            log.error("generateAccessToken_impl failed, url:{}, wechat type:{}", URL, wechatType);
            // 这里需要看看map是null的情况下，responseString是什么值
            throw new UtopiaRuntimeException("获取access_token失败," + response.getResponseString());
        }
        return access_token;
    }

    private String generateTicket(String eventKey, WechatType wechatType) {
        Integer r = flushable.load(wechatType.getTicketExeedQuotaCacheKey());
        if (Objects.equals(r, 1)) {
            return null;
        }

        String accessToken = generateAccessToken(wechatType);
        return flushable.wrapCache(this)
                .expiration(20 * 60)
                .keyPrefix("WECHAT_QRCODE_TICKET_CACHE")
                .keys(eventKey, wechatType)
                .proxy()
                .generateTicket_impl(eventKey, accessToken, wechatType);
    }

    private String generateF2FTicket(String eventKey, WechatType wechatType) {
        String accessToken = generateAccessToken(wechatType);
        String data = "{\"action_name\": \"QR_LIMIT_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \"" + eventKey + "\"}}}";
        String url = ProductConfig.get("wechat.qrcode_ticket_url") + "?access_token=" + accessToken;
        String r = HttpRequestExecutor.defaultInstance().post(url)
                .json(data).execute().getResponseString();
        Map<String, String> mapTicket = JsonUtils.fromJsonToMapStringString(r);
        if (null == mapTicket) {
            throw new UtopiaRuntimeException("生成ticket请求微信未响应");
        }
        if (mapTicket.containsKey("ticket")) {
            return mapTicket.get("ticket");
        } else {
            if (mapTicket.containsKey("errcode") && (mapTicket.get("errcode").equals("40001") || mapTicket.get("errcode").equals("40014") || mapTicket.get("errcode").equals("42001"))) {
                String key = CacheKeyGenerator.generateCacheKey(wechatType.getAccessTokenCacheKey(), null, new Object[]{});
                flushable.delete(key);
            }
            if (mapTicket.containsKey("errcode") && mapTicket.get("errcode").equals("45009")) { //接口调用超过限制
                flushable.add(wechatType.getTicketExeedQuotaCacheKey(), DateUtils.getCurrentToDayEndSecond(), 1);
            }
            throw new UtopiaRuntimeException("生成ticket失败，" + r);
        }
    }

    private String generateWechatDynamicQRCodeTicket(int sceneId, long expire, WechatType wechatType) {
        // 生成微信动态二维码的ticket
        String accessToken = generateAccessToken(wechatType);
        String data = "{\"expire_seconds\": " + expire + ", \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": " + sceneId + "}}}";
        String url = ProductConfig.get("wechat.qrcode_ticket_url") + "?access_token=" + accessToken;
        String r = HttpRequestExecutor.defaultInstance().post(url)
                .json(data).execute().getResponseString();
        Map<String, String> mapTicket = JsonUtils.fromJsonToMapStringString(r);
        if (null == mapTicket) {
            throw new UtopiaRuntimeException("生成ticket请求微信未响应");
        }
        if (mapTicket.containsKey("ticket")) {
            return mapTicket.get("ticket");
        } else {
            if (mapTicket.containsKey("errcode") && (mapTicket.get("errcode").equals("40001") || mapTicket.get("errcode").equals("40014") || mapTicket.get("errcode").equals("42001"))) {
                String key = CacheKeyGenerator.generateCacheKey(wechatType.getAccessTokenCacheKey(), null, new Object[]{});
                flushable.delete(key);
            }
            if (mapTicket.containsKey("errcode") && mapTicket.get("errcode").equals("45009")) { //接口调用超过限制
                flushable.add(wechatType.getTicketExeedQuotaCacheKey(), DateUtils.getCurrentToDayEndSecond(), 1);
            }
            throw new UtopiaRuntimeException("生成ticket失败，" + r);
        }
    }

    private int generateSceneId(String scene, WechatType wechatType) {
        // 根据scene生成sceneId
        // 由于微信限制了sceneId为32位非0整型，而我们的应用场景可能要超过这个量，切不存在完美的对应关系，所以需要一个可逆映射机制
        // 暂设计为，通过两条缓存来实现，scene->sceneId和sceneId->scene。
        // scene如果缓存中有，则用之，否则hash生成sceneId，获取ticket，添加双向缓存
        // 得到sceneId后，去缓存中查找scene，无则无效
        Random random = new Random();
        int sceneHashCode = random.nextInt(2147483647);
        // 缓存中读取是否能用sceneHashCode
        while (true) {
            String sceneIdCacheKey = wechatType.toString() + "-DYNAMIC-SCENE-ID-" + sceneHashCode;
            CacheObject<String> cacheObject = persistence.get(sceneIdCacheKey);
            if (cacheObject != null && StringUtils.isNotBlank(cacheObject.getValue())) {
                //啊，有冲突，需要重新找一个能用的，加上一个随机数
                sceneHashCode = (sceneHashCode + random.nextInt(2147483647)) % 2147483647;
            } else {
                // 终于找到了
                // 将这个sceneId存入cache
                boolean sucess = persistence.add(sceneIdCacheKey, (int) (DateUtils.calculateDateDay(new Date(), 30).getTime() / 1000), scene);
                break;
            }
        }
        return sceneHashCode;
    }


    public String generateTicket_impl(String eventKey, String accessToken, WechatType wechatType) {
        String data = "{\"expire_seconds\": 1800, \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": " + eventKey + "}}}";
        String url = ProductConfig.get("wechat.qrcode_ticket_url") + "?access_token=" + accessToken;
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).json(data).execute();

        if (response.hasHttpClientException()) {
            //noinspection ThrowableResultOfMethodCallIgnored
            throw new UtopiaRuntimeException("生成ticket请求失败，" + response.getHttpClientException().getMessage(), response.getHttpClientException());
        }

        Map<String, String> mapTicket = JsonUtils.fromJsonToMapStringString(response.getResponseString());
        if (null == mapTicket) {
            throw new UtopiaRuntimeException("生成ticket请求微信未响应");
        }

        if (mapTicket.containsKey("ticket")) {
            return mapTicket.get("ticket");
        } else {
            if (mapTicket.containsKey("errcode") && (mapTicket.get("errcode").equals("40001") || mapTicket.get("errcode").equals("40014") || mapTicket.get("errcode").equals("42001"))) {
                //由于access_token被生成后，之前生成的access_token就失效，所以access_token可能被过期
                String key = CacheKeyGenerator.generateCacheKey(wechatType.getAccessTokenCacheKey(), null, new Object[]{});
                flushable.delete(key);
            }
            if (mapTicket.containsKey("errcode") && mapTicket.get("errcode").equals("45009")) { //接口调用超过限制
                flushable.add(wechatType.getTicketExeedQuotaCacheKey(), DateUtils.getCurrentToDayEndSecond(), 1);
            }
            throw new UtopiaRuntimeException("生成ticket失败，" + response.getResponseString());
        }
    }

    public String generateQRCode(String eventKey, WechatType wechatType) {
        if (wechatType == null) {
            throw new UtopiaRuntimeException("微信类型为空");
        }
        String ticket = generateTicket(eventKey, wechatType);
        if (StringUtils.isBlank(ticket)) {
            return wechatType.getStaticQrcode();
        }
        return ProductConfig.get("wechat.qrcode_url") + "?ticket=" + ticket;
    }

    public String generateF2FQrcode(String scene, WechatType wechatType) {
        if (wechatType == null) {
            throw new UtopiaRuntimeException("微信类型为空");
        }
        int sceneId = generateSceneId(scene, wechatType);
        String ticket = generateWechatDynamicQRCodeTicket(sceneId, 2592000, wechatType);
        if (StringUtils.isBlank(ticket)) {
            return wechatType.getStaticQrcode();
        }
        return ProductConfig.get("wechat.qrcode_url") + "?ticket=" + ticket;
    }

    public String generateJsApiTicket(WechatType wechatType) throws CannotAcquireLockException {
        String key = "WECHAT_CACHE_KEY_JSAPI_" + wechatType.name() + "_" + ProductConfig.get(wechatType.getAppId());
        CacheObject<String> ticket = flushable.get(key);

        if (null == ticket || null == ticket.getValue()) {
            //重新向微信服务器请求新的ticket,需要加锁,加锁失败会抛CannotAcquireLockException异常
            String ticketStr = "";
            try {
                ticketStr = AtomicLockManager.instance()
                        .wrapAtomic(this)
                        .keys(wechatType.getAppId(), wechatType.getAppSecret())
                        .proxy()
                        .generateJsApiTicket_impl(wechatType);
                flushable.set(key, 7198, ticketStr); //ticket的有效期7200秒,这里缓存7198秒
            } catch (UtopiaRuntimeException e) {
                // retry control
                String retryKey = key + "_retry_count";
                Long retryCount = flushable.incr(retryKey, 1, 1, 30);
                if (retryCount != null && retryCount > 2) {
                    throw e;
                }

                return generateJsApiTicket(wechatType);
            }

            return ticketStr;
        }

        return ticket.getValue();
    }

    public String generateJsApiTicket_impl(WechatType wechatType) {
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={}&type=jsapi";
        String accessToken = generateAccessToken(wechatType);
        url = StringUtils.formatMessage(url, accessToken);

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
        if (response.hasHttpClientException()) {
            // 因为缓存时间的问题，这里会出现accessToken失效的错误，需要清一下缓存
            String exceptionMessage = response.getHttpClientExceptionMessage();
            if (exceptionMessage != null && exceptionMessage.contains("invalid credential")) {
                String cacheKey = wechatType.getAccessTokenCacheKey() + "_" + ProductConfig.get(wechatType.getAppId());
                flushable.delete(cacheKey);
            }

            throw new UtopiaRuntimeException("生成jsapi ticket请求失败," + response.getHttpClientExceptionMessage(),
                    response.getHttpClientException());
        }
        if (null == response.getResponseString()) {
            throw new UtopiaRuntimeException("生成jsapi ticket请求失败,response nothing.");
        }


        Map<String, String> map = JsonUtils.fromJsonToMapStringString(response.getResponseString());
        if (null != map && map.containsKey("ticket")) {
            return map.get("ticket");
        } else {
            String responseMessage = response.getResponseString();
            if (responseMessage != null && responseMessage.contains("invalid credential")) {
                String cacheKey = wechatType.getAccessTokenCacheKey() + "_" + ProductConfig.get(wechatType.getAppId());
                flushable.delete(cacheKey);
            }

            throw new UtopiaRuntimeException("生成jsapi ticket失败," + response.getResponseString());
        }
    }
}
