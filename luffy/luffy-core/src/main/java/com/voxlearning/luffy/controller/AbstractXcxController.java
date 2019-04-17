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

package com.voxlearning.luffy.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.Base64Utils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
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
import com.voxlearning.alps.spi.exception.CryptoException;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.luffy.cache.LuffyWebCacheSystem;
import com.voxlearning.luffy.context.LuffyRequestContext;
import com.voxlearning.luffy.exception.MiniProgramErrorException;
import com.voxlearning.luffy.support.utils.TokenHelper;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.payment.PaymentGatewayManager;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.piclisten.api.PicListenCommonService;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.api.constant.TextBookSdkType;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;

import static com.voxlearning.luffy.controller.ApiConstants.RES_CLAZZ_LEVEL;
import static com.voxlearning.luffy.controller.ApiConstants.RES_CLAZZ_LEVEL_NAME;
import static com.voxlearning.luffy.controller.ContentApiConstants.*;

/**
 * @author Xin Xin
 * @since 10/15/15
 */
@Slf4j
public class AbstractXcxController extends SpringContainerSupport {

    protected static String bookImgUrlPrefix = "http://cdn-cnc.17zuoye.cn/resources/app/jzt/res/{0}.png";


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
    protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject
    protected QuestionLoaderClient questionLoaderClient;
    @Inject
    protected AtomicLockManager atomicLockManager;
    @Inject
    @Getter
    protected TeacherLoaderClient teacherLoaderClient;
    @Inject
    protected WechatServiceClient wechatServiceClient;
    @Inject
    protected ParentLoaderClient parentLoaderClient;
    @Inject
    protected StudentLoaderClient studentLoaderClient;
    @Inject
    protected UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    protected UserOrderServiceClient userOrderServiceClient;
    @Inject
    protected TextBookManagementLoaderClient textBookManagementLoaderClient;
    @Inject
    protected PaymentGatewayManager paymentGatewayManager;
    @Inject
    protected NewContentLoaderClient newContentLoaderClient;
    @ImportService(interfaceClass = PicListenCommonService.class)
    protected PicListenCommonService picListenCommonService;


    @Inject
    protected TokenHelper tokenHelper;

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

    protected String getSessionKeyByOpenId(String openId) {
        CacheObject<String> cacheObject = luffyWebCacheSystem.CBS.persistence.get(openId);
        if (cacheObject != null && StringUtils.isNotBlank(cacheObject.getValue())) {
            return cacheObject.getValue();
        }
        return null;
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

    protected User currentUser() {
        return getRequestContext().getCurrentUser();
    }

    protected String getCdnBaseUrlStaticSharedWithSep() {
        return cdnResourceUrlGenerator.getCdnBaseUrlStaticSharedWithSep(getRequest());
    }


    protected MapMessage wrapper(Consumer<MapMessage> wrapper) {
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
            mm = MapMessage.errorMessage(e.getExecutionExceptionMessage());
        } catch (Exception e) {

            boolean mode = RuntimeMode.current().lt(Mode.STAGING);
            if (mode) {
                mm = MapMessage.errorMessage(e.getMessage());
            } else {
                mm = MapMessage.errorMessage("System error.");
            }
            log.error(e.getMessage());

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

    /**
     * 对于点读机,需要返回付费状态  免费 已购买 为购买
     * 需要sdk的教材加标识,返回对应的sdk的教材id
     *
     * @param bookProfile
     * @param series       教材系列node
     * @param isPicListen
     * @param lastDayRange
     * @return
     */
    protected Map<String, Object> convert2BookMap(NewBookProfile bookProfile, NewBookCatalog series, Boolean isPicListen, DayRange lastDayRange) {
        Map<String, Object> map = convert2BookMap(bookProfile, series);

        // 付费支持
        if (isPicListen) {
            //教材状态 已购买  免费  未购买
            Boolean bookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(bookProfile);

            Map<String, String> statusMap = purchaseStatus(bookNeedPay, lastDayRange);
            map.put(RES_STATUS, statusMap.get("status"));
            String expire = statusMap.get("expire");
            String expireStr = StringUtils.isNotBlank(expire) ? ("有效期至：" + expire) : "";
            map.put(RES_EXPIRE_DATE, expireStr);//到期时间

            //是否需要sdk,以及对应的sdk的教材id
            TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(bookProfile.getId());
            addIntoMapSdk(map, sdkInfo);
        }
        return map;
    }

    protected Map<String, Object> convert2BookMap(NewBookProfile bookProfile, NewBookCatalog series) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(RES_BOOK_ID, bookProfile.getId());
        addIntoMap(map, RES_BOOK_NAME, bookProfile.getShortName());
        addIntoMap(map, RES_BOOK_CNAME, bookProfile.getName());
        addIntoMap(map, RES_BOOK_ENAME, bookProfile.getAlias());
        addIntoMap(map, RES_SUBJECT, Subject.fromSubjectId(bookProfile.getSubjectId()).name());
        addIntoMap(map, RES_SUBJECT_NAME, Subject.fromSubjectId(bookProfile.getSubjectId()).getValue());
        addIntoMap(map, RES_CLAZZ_LEVEL, bookProfile.getClazzLevel());
        addIntoMap(map, RES_CLAZZ_LEVEL_NAME, ClazzLevel.parse(bookProfile.getClazzLevel()).getDescription());
        addIntoMap(map, RES_BOOK_TERM, Term.of(bookProfile.getTermType()).name());
        addIntoMap(map, RES_BOOK_COVER_URL, StringUtils.isBlank(bookProfile.getImgUrl()) ? "" : getCdnBaseUrlStaticSharedWithSep() + bookProfile.getImgUrl());

        if (series != null) {
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(bookProfile.getSubjectId()), series.getName());
            if (bookPress != null) {
                addIntoMap(map, RES_BOOK_VIEW_CONTENT, bookPress.getViewContent());
                addIntoMap(map, RES_BOOK_COLOR, bookPress.getColor());
                addIntoMap(map, RES_BOOK_IMAGE, MessageFormat.format(bookImgUrlPrefix, bookPress.getColor()));
            }
        }
        return map;
    }

    protected String getClientVersion() {
        return getRequestString(ApiConstants.REQ_APP_NATIVE_VERSION);
    }
    protected void addIntoMapSdk(Map<String, Object> map, TextBookManagement.SdkInfo sdkInfo) {
        if (sdkInfo.getSdkType() == TextBookSdkType.renjiao || sdkInfo.getSdkType() == TextBookSdkType.hujiao) { //人教 sdk
            addIntoMap(map, RES_SDK, sdkInfo.getSdkType().name());
            addIntoMap(map, RES_SDK_BOOK_ID, sdkInfo.getSdkBookIdV2(getClientVersion()));
        } else {
            addIntoMap(map, RES_SDK, sdkInfo.getSdkType().name());
            if (sdkInfo.getSdkType().hasSdk()) {
                addIntoMap(map, RES_SDK_BOOK_ID, sdkInfo.getSdkBookIdV2(getClientVersion()));
            }
        }
    }

    protected Map<String, String> purchaseStatus(Boolean bookNeedPay, DayRange lastDayRange) {
        Map<String, String> statusMap = new HashMap<>();
        String bookStatus;
        String expireDateStr = "";
        if (!bookNeedPay) {
            bookStatus = "free";
        } else {
            if (lastDayRange == null || lastDayRange.getEndDate().before(new Date()))
                bookStatus = "not_purchased";
            else {
                bookStatus = "purchased";
                expireDateStr = DateUtils.dateToString(lastDayRange.getEndDate(), DateUtils.FORMAT_SQL_DATE); //到期时间
            }
        }
        statusMap.put("status", bookStatus);
        statusMap.put("expire", expireDateStr);
        return statusMap;
    }

    protected void addIntoMap(Map<String, Object> dataMap, String key, Object value) {
        if (value == null) {
            dataMap.put(key, "");
        } else {
            dataMap.put(key, value);
        }
    }

}
