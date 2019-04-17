package com.voxlearning.luffy.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.Base64Utils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.cipher.CommonCipherUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.hydra.exception.ServerExecutionErrorException;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.spi.exception.CryptoException;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.luffy.cache.LuffyWebCacheSystem;
import com.voxlearning.luffy.context.LuffyRequestContext;
import com.voxlearning.luffy.exception.MiniProgramErrorException;
import com.voxlearning.luffy.support.utils.TokenHelper;
import com.voxlearning.utopia.api.enums.MiniProgramApi;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import com.voxlearning.utopia.service.wechat.client.MiniProgramServiceClient;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


@Slf4j
public abstract class MiniProgramAbstractController extends SpringContainerSupport {


    private final static Integer SESSIONKEY_EXPIRATION_IN_SECONDS = 86400 * 25;

    @Inject
    protected CdnResourceUrlGenerator cdnResourceUrlGenerator;

    @Getter
    @Inject
    protected AsyncFootprintServiceClient asyncFootprintServiceClient;

    @Inject
    protected LuffyWebCacheSystem luffyWebCacheSystem;
    @Inject
    protected WechatLoaderClient wechatLoaderClient;
    @Inject
    @Getter
    protected UserLoaderClient userLoaderClient;
    @Inject
    @Getter
    protected UserServiceClient userServiceClient;
    @Inject
    @Getter
    protected StudentServiceClient studentServiceClient;
    @Inject
    protected ParentServiceClient parentServiceClient;
    @Inject
    protected SmsServiceClient smsServiceClient;

    @Inject
    @Getter
    protected TeacherLoaderClient teacherLoaderClient;
    @Inject
    protected WechatServiceClient wechatServiceClient;
    @Inject
    protected ParentLoaderClient parentLoaderClient;

    @Inject
    protected MiniProgramServiceClient miniProgramServiceClient;


    @Inject
    protected TokenHelper tokenHelper;


    protected static final HttpRequestExecutor httpRequestExecutor = HttpRequestExecutor.instance(HttpClientType.POOLING);

    protected abstract MiniProgramType type();

    protected MapMessage getNoLoginResult() {
        String cid = tokenHelper.generateContextId(getRequestContext());
        return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE).add("cid", cid);
    }

    public boolean onBeforeControllerMethod() {
        return true;
    }

    /* ======================================================================================
       以下代码负责 Request & Response
       ====================================================================================== */
    protected HttpServletRequest getRequest() {
        return getRequestContext().getRequest();
    }

    protected HttpServletResponse getResponse() {
        return getRequestContext().getResponse();
    }

    protected String getRequestParameter(String key, String def) {
        //then URLEncodedUtils can be used to parse query-string ?
        String v = getRequest().getParameter(key);
        return v == null ? def : v;
    }

    protected String getRequestString(String key) {
        //then URLEncodedUtils can be used to parse query-string ?
        String v = getRequest().getParameter(key);
        return v == null ? "" : v;
    }

    protected boolean getRequestBool(String name) {
        return ConversionUtils.toBool(getRequest().getParameter(name));
    }

    protected boolean getRequestBool(String name, boolean def) {
        return SafeConverter.toBoolean(getRequest().getParameter(name), def);
    }

    protected long getRequestLong(String name, long def) {
        return ConversionUtils.toLong(getRequest().getParameter(name), def);
    }

    protected long getRequestLong(String name) {
        return ConversionUtils.toLong(getRequest().getParameter(name));
    }

    protected int getRequestInt(String name, int def) {
        return ConversionUtils.toInt(getRequest().getParameter(name), def);
    }

    protected int getRequestInt(String name) {
        return ConversionUtils.toInt(getRequest().getParameter(name));
    }

    public LuffyRequestContext getRequestContext() {
        return (LuffyRequestContext) DefaultContext.get();
    }

    protected String getOpenId() {
        return getRequestContext().getAuthenticatedOpenId();
    }

    protected String fetchMainsiteUrlByCurrentSchema() {
        if (getRequestContext().isHttpsRequest()) {
            return "https://www." + TopLevelDomain.getTopLevelDomain();
        }
        return "http://www." + TopLevelDomain.getTopLevelDomain();
    }

    protected String getSessionKeyByOpenId(String openId, MiniProgramType type) {
        String key = String.format("%s-%s", type, openId);
        CacheObject<String> cacheObject = luffyWebCacheSystem.CBS.persistence.get(key);
        if (cacheObject != null && StringUtils.isNotBlank(cacheObject.getValue())) {
            return cacheObject.getValue();
        }
        return null;
    }

    protected void setSessionKeyByOpenId(String openId, String sessionKey, MiniProgramType type) {
        // 更新sessionKey  暂定25天(有资料说微信有效期是30天)
        String key = String.format("%s-%s", type, openId);
        luffyWebCacheSystem.CBS.persistence.set(key, SESSIONKEY_EXPIRATION_IN_SECONDS, sessionKey);
    }


    protected User currentUserByUserType(UserType... optionUserTypes) {
        User user = getRequestContext().getCurrentUser();
        if (user == null)
            return null;
        if (Arrays.asList(optionUserTypes).contains(user.fetchUserType()))
            return user;
        else
            return null;
    }

    protected MapMessage wrapper(Consumer<MapMessage> wrapper) {
        boolean mode = RuntimeMode.current().lt(Mode.STAGING);
        MapMessage mm = MapMessage.successMessage();
        try {
            wrapper.accept(mm);
        } catch (MiniProgramErrorException e) {
            mm = MapMessage.errorMessage(e.getMessage()).setErrorCode(e.getCode());
            log.error(e.getMessage());
        } catch (CryptoException e) {
            mm = MapMessage.errorMessage("信息解析失败，请重新试下吧").setErrorCode(ApiConstants.RES_RESULT_DECODE_FAILED_CODE);
            log.error(e.getMessage());
        } catch (ServerExecutionErrorException e) {
            if (mode) {
                mm = MapMessage.errorMessage(e.getExecutionExceptionMessage());
            } else {
                mm = MapMessage.errorMessage("系统服务异常，请稍后重试");
            }
            log.error(e.getMessage(), e);

        } catch (Exception e) {
            if (mode) {
                mm = MapMessage.errorMessage(e.getMessage());
            } else {
                mm = MapMessage.errorMessage("系统异常，请稍候重试");
            }
            log.error(e.getMessage(), e);

        }

        return mm;
    }

    protected boolean nb(CharSequence src) {
        return StringUtils.isNotBlank(src);
    }


    private Map<String, Object> decryptData(String iv, String encryptedData, String sessionKey) {
        CommonCipherUtils commonCipherUtils = new CommonCipherUtils("AES/CBC/PKCS5Padding", "AES");
        byte[] ivBytes = Base64Utils.decodeBase64(iv);
        byte[] keyBytes = Base64Utils.decodeBase64(sessionKey);
        byte[] encodeBytes = Base64Utils.decodeBase64(encryptedData);
        byte[] decodeBytes = commonCipherUtils.decrypt(keyBytes, encodeBytes, ivBytes);
        if (decodeBytes != null && decodeBytes.length > 0) {
            return JsonUtils.fromJson(new String(decodeBytes));
        }
        return new HashMap<>();
    }


    protected String getDecryptData(String iv, String encryptedData, String sessionKey, String param) {
        if (nb(iv) && nb(encryptedData) && nb(sessionKey) && nb(param)) {
            return SafeConverter.toString(decryptData(iv, encryptedData, sessionKey).get(param));
        }
        return "";
    }


    protected String getOpenIdByCode(String code) {
        Map<String, String> data = getOpenIdAndSessionKeyByCode(code);
        return data.get("openId");
    }

    protected Map<String, String> getOpenIdAndSessionKeyByCode(String code) {
        Map<String, String> data = new HashMap<>();

        if (StringUtils.isBlank(code)) {
            return data;
        }
        String url = MiniProgramApi.JSCODE2SESSION.url(ProductConfig.get(type().getAppId()), ProductConfig.get(type().getAppSecret()), code);

        AlpsHttpResponse response = httpRequestExecutor.get(url).socketTimeout(10000).execute();
        if (null == response.getResponseString()) {
            logger.warn("Get openId by code from weixin failed, response nothing.");
            return data;
        }

        Map<String, Object> result = JsonUtils.fromJson(response.getResponseString());

        if (!MapUtils.isEmpty(result) && result.get("errcode") == null) {
            String openId = SafeConverter.toString(result.get("openid"));
            String sessionKey = SafeConverter.toString(result.get("session_key"));
            data.put("openId", openId);
            data.put("sessionKey", sessionKey);
            // Update session key
            if (StringUtils.isNoneBlank(openId, sessionKey)) {
                setSessionKeyByOpenId(openId, sessionKey, type());
            }
            return data;
        } else if (!MapUtils.isEmpty(result) && "40029".equals(SafeConverter.toString(result.get("errcode")))) {
            //微信有bug,菜单点击后会发出两次请求,第一次请求会被微信终止进程,导致第二次接收到的是已经接收过一次的,此时code已经失效
            //do nothing
        } else {
            logger.error("Get openId by code from oauth failed,code:{},response:{}", code, response.getResponseString());
        }
        return data;
    }

}
