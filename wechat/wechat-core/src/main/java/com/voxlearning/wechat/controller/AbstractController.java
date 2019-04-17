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

package com.voxlearning.wechat.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.XmlConvertUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.service.business.consumer.BusinessStudentServiceClient;
import com.voxlearning.utopia.service.business.consumer.MissionLoaderClient;
import com.voxlearning.utopia.service.config.client.BusinessActivityManagerClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.HomeworkCommentLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.BusinessActivity;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import com.voxlearning.wechat.cache.WechatWebCacheSystem;
import com.voxlearning.wechat.constants.WechatInfoCode;
import com.voxlearning.wechat.context.PayConfig;
import com.voxlearning.wechat.context.WechatRequestContext;
import com.voxlearning.wechat.service.OrderService;
import com.voxlearning.wechat.service.UserService;
import com.voxlearning.wechat.support.PageBlockContentGenerator;
import com.voxlearning.wechat.support.WechatMessageHelper;
import com.voxlearning.wechat.support.WechatPictureUploader;
import com.voxlearning.wechat.support.utils.TokenHelper;
import com.voxlearning.wechat.support.utils.WechatSignUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.dom4j.DocumentException;
import org.springframework.ui.Model;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.voxlearning.wechat.constants.WechatInfoCode.PARENT_SEATTLE_ACTIVITY_NOT_EXIST;

/**
 * @author Xin Xin
 * @since 10/15/15
 */
@Slf4j
public class AbstractController extends SpringContainerSupport {
    public static final String WECHAT_LOG_COLLECTION_NAME = "wechat_logs";
    public static final String WECHAT_LOG_SYS = "wechat";
    public static final String WECHAT_LOG_CODE = "1";
    public static final String WECHAT_LOG_SOURCE_PARENT = "wechat_parent";
    public static final String WECHAT_LOG_APP_PARENT = "parent";
    private static final String PARENT_MCH_ID = "1219984501";
    private static final String CHIPS_MCH_ID = "1503058101";
    private static final String WECHAT_UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    @Getter
    @Inject protected AsyncFootprintServiceClient asyncFootprintServiceClient;

    @Inject protected PageBlockContentServiceClient pageBlockContentServiceClient;
    @Inject protected AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject
    protected UserService userService;
    @Inject
    protected OrderService orderService;
    @Inject
    protected WechatWebCacheSystem wechatWebCacheSystem;
    @Inject
    protected WechatLoaderClient wechatLoaderClient;
    @Inject
    @Getter
    protected UserLoaderClient userLoaderClient;
    @Inject
    protected StudentLoaderClient studentLoaderClient;
    @Inject
    @Getter
    protected UserServiceClient userServiceClient;
    @Inject
    protected ParentServiceClient parentServiceClient;
    @Inject
    protected ParentLoaderClient parentLoaderClient;
    @Inject
    protected HomeworkCommentLoaderClient homeworkCommentLoaderClient;
    @Inject
    protected SmsServiceClient smsServiceClient;

    @Inject
    protected TokenHelper tokenHelper;
    @Inject
    protected UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject
    protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    protected QuestionLoaderClient questionLoaderClient;
    @Inject
    protected MissionLoaderClient missionLoaderClient;
    @Inject
    protected BusinessStudentServiceClient businessStudentServiceClient;
    @Inject
    protected AtomicLockManager atomicLockManager;
    @Inject
    protected WechatPictureUploader wechatPictureUploader;
    @Inject
    @Getter
    protected TeacherLoaderClient teacherLoaderClient;
    @Inject
    protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    protected WechatServiceClient wechatServiceClient;
    @Inject
    protected VendorLoaderClient vendorLoaderClient;
    @Inject
    protected VendorServiceClient vendorServiceClient;
    @Inject
    protected PaperLoaderClient paperLoaderClient;
    @Inject
    protected UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    protected UserOrderServiceClient userOrderServiceClient;
    @Inject
    protected NewContentLoaderClient newContentLoaderClient;

    @Inject
    protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject private BusinessActivityManagerClient businessActivityManagerClient;

    @Inject
    protected WechatMessageHelper messageHelper;

    private static int TIME = 60 * 60;//一个小时

    protected void persistenceCache(String key, String value) {
        wechatWebCacheSystem.CBS.unflushable.set(key, TIME, value);
    }

    protected String getPersistenceStringValue(String key) {
        Object obj = wechatWebCacheSystem.CBS.unflushable.load(key);
        return obj == null ? wechatWebCacheSystem.CBS.persistence.load(key) : SafeConverter.toString(obj);
    }

    private PageBlockContentGenerator pageBlockContentGenerator;

    public boolean onBeforeControllerMethod() throws IOException {
        //如果是家长端过来的get请求，判断一下家长是否绑了手机
//        String servletPath = getRequestContext().getRequest().getServletPath();
//        if (getRequestContext().getRequest().getMethod().equals("GET")
//                && servletPath.startsWith("/parent/") && !servletPath.endsWith("bindmobile.vpage")) {
//            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(getRequestContext().getUserId());
//            if (null == userAuthentication || StringUtils.isBlank(userAuthentication.getSensitiveMobile())) {
//                getRequestContext().getResponse().sendRedirect("redirect:/parent/ucenter/bindmobile.vpage?returnUrl=" + servletPath);
//                return false;
//            }
//        }

        return true;
    }

    protected HttpServletResponse getResponse() {
        return HttpRequestContextUtils.currentRequestContext().getResponse();
    }

    protected void removeUserAndOpenIdFromCookie() {
        Cookie cookieOpenId = new Cookie("openId", "");
        cookieOpenId.setPath("/");
        cookieOpenId.setMaxAge(0);
        getResponse().addCookie(cookieOpenId);
        getRequestContext().cleanupAuthenticationStates();
    }

    /* ======================================================================================
       以下代码负责 Request & Response
       ====================================================================================== */
    protected HttpServletRequest getRequest() {
        return getRequestContext().getRequest();
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

    public WechatRequestContext getRequestContext() {
        return (WechatRequestContext) DefaultContext.get();
    }

    protected synchronized PageBlockContentGenerator getPageBlockContentGenerator() {
        if (pageBlockContentGenerator == null) {
            pageBlockContentGenerator = new PageBlockContentGenerator(pageBlockContentServiceClient);
        }
        return pageBlockContentGenerator;
    }

    protected String getOpenId() {
        return getRequestContext().getAuthenticatedOpenId();
    }

    ////////////////////////////
    //阿分题名字个性化
    ///////////////////////////
    protected boolean version_ZiBo(Map<Long, Integer> regionMap) {
        List<Integer> regions = Arrays.asList(370303, 370302, 370306);
        for (Long id : regionMap.keySet()) {
            if (!regions.contains(regionMap.get(id))) {
                return false;
            }
        }
        return true;
    }

    protected boolean version_XuZhou(Map<Long, Integer> regionMap) {
        List<Integer> regions = Arrays.asList(320311, 320303, 320322, 320383, 320324);
        for (Long id : regionMap.keySet()) {
            if (!regions.contains(regionMap.get(id))) {
                return false;
            }
        }
        return true;
    }

    /////////////////////////
    //错误页
    protected String redirectWithMsg(String msg, Model model) {
        String url = getRequestContext().getFullRequestUrl();

        model.addAttribute("errmsg", msg);
        model.addAttribute("reffer", url);

        return "/parent/block/error";
    }

    /////////////////////////
    //提示页
    protected String infoPage(WechatInfoCode code, Model model) {
        model.addAttribute("info", code.getDesc());
        model.addAttribute("code", code.getCode());
        model.addAttribute("url", code.getReturnUrl());
        return "/parent/block/info";
    }

    protected String infoPage(String info, String returnUrl, Model model) {
        model.addAttribute("info", info);
        if (!StringUtils.isBlank(returnUrl)) {
            model.addAttribute("url", returnUrl);
        }
        return "/parent/block/info";
    }


    ///////////////////////////////
    //Account
    ///////////////////////////////
    //格式  studentId:parentId:callNameKey
    protected void saveCookie(Long studentId, Long parentId, Integer callNameKey) {
        getRequestContext().getCookieManager().setCookieEncrypt("psd", studentId + ":" + (null == parentId ? "" : parentId) + ":" + (null == callNameKey ? "" : callNameKey), 10 * 60);
    }

    protected Optional<Long> getStudentIdFromCookie() {
        String value = getRequestContext().getCookieManager().getCookieDecrypt("psd", null);
        if (StringUtils.isBlank(value)) {
            return Optional.empty();
        }

        String[] vs = value.split(":");
        if (vs.length > 0) {
            return Optional.of(Long.valueOf(vs[0]));
        }
        return Optional.empty();
    }

    protected Optional<Long> getParentIdFromCookie(Integer callNameKey) {
        String value = getRequestContext().getCookieManager().getCookieDecrypt("psd", null);
        if (StringUtils.isBlank(value)) {
            return Optional.empty();
        }

        String[] vs = value.split(":");
        if (vs.length == 3 && Objects.equals(Integer.valueOf(vs[2]), callNameKey)) {
            return Optional.of(Long.valueOf(vs[1]));
        }
        return Optional.empty();
    }

    protected void log(Map<String, String> map) {
        map.put("sys", WECHAT_LOG_SYS);
        map.put("code", WECHAT_LOG_CODE);

        if (StringUtils.isNotBlank(getRequestContext().getAuthenticatedOpenId())) {
            map.put("openId", getRequestContext().getAuthenticatedOpenId());
        }

        if (null != getRequestContext().getUserId()) {
            map.put("userId", getRequestContext().getUserId().toString());
        }


        LogCollector.instance().info(WECHAT_LOG_COLLECTION_NAME, map);
    }

    protected String fetchMainsiteUrlByCurrentSchema() {
        if (getRequestContext().isHttpsRequest()) {
            return "https://www." + TopLevelDomain.getTopLevelDomain();
        }
        return "http://www." + TopLevelDomain.getTopLevelDomain();
    }

    protected MapMessage getPrepayIdForUserOrder(String orderId) throws DocumentException {
        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        if (null == order) {
            return MapMessage.errorMessage().add("err", WechatInfoCode.PARENT_TRUSTEE_ORDER_NOT_EXIST);
        }
        if (!order.canBePaid()) {
            return MapMessage.errorMessage().add("err", order.getOrderType() == OrderType.chips_english ? WechatInfoCode.CHIPS_ORDER_ALREADY_PAYD : WechatInfoCode.PARENT_ORDER_ALREADY_PAYD);
        }
        String returnUrl = "/parent/ucenter/orderlist.vpage?_to=paid";
        String mchId = PARENT_MCH_ID;
        // 应用类订单
        if (order.getOrderType() == OrderType.app) {
            //下订单时check过了,这里再check一次,过滤从未支付订单页面过来的请求
            MapMessage checkMsg = userOrderServiceClient.checkDaysToExpireForCreateNewOrder(order.getUserId(),
                    OrderProductServiceType.safeParse(order.getOrderProductServiceType()));
            if (!checkMsg.isSuccess()) {
                return MapMessage.errorMessage("您已经开通了" + order.getProductName() + "，可以直接学习使用哦！").add("url", "/parent/ucenter/orderlist.vpage");
            }
            // 通用支付类订单
        } else if (order.getOrderType() == OrderType.seattle) {
            BusinessActivity activity = businessActivityManagerClient.getBusinessActivityBuffer()
                    .load(SafeConverter.toLong(order.getProductId()));
            if (activity == null) {
                return MapMessage.errorMessage().add("err", PARENT_SEATTLE_ACTIVITY_NOT_EXIST);
            }
            returnUrl = activity.getReturnUrl();
        } else if (OrderType.micro_course == order.getOrderType()) { // 微课堂订单，支付成功跳转到详情页
            // 这个controller在华盛顿上，所以需要跳到华盛顿
            returnUrl = ProductConfig.getMainSiteBaseUrl() + "/mizar/course/courseperiod.vpage?track=WechatParent&period=" + order.getProductId();
        } else if (OrderType.chips_english == order.getOrderType()) {
            returnUrl = "/chips/order/"+ orderId + "/paymentsuccess.vpage";
            mchId = CHIPS_MCH_ID;
        }
        BigDecimal price = order.getOrderPrice();
        if (PaymentGateway.getUsersForPaymentTest(getRequestContext().getUserId())) {
            price = BigDecimal.valueOf(0.01);
        }

        String prepayId = generatePrepayId(order.getProductName(), orderId, price, getRequestContext().getWebAppBaseUrl() + "/notify-order.vpage", mchId);
        if (StringUtils.isEmpty(prepayId)) {
            return MapMessage.errorMessage().add("err", order.getOrderType() == OrderType.chips_english ? WechatInfoCode.CHIPS_ORDER_ALREADY_PAYD : WechatInfoCode.PARENT_ORDER_ALREADY_PAYD);
        }
        return MapMessage.successMessage(prepayId).add("backUrl", returnUrl).add("orderType", order.getOrderType().name());
    }

    protected void initModel(Model model, PayConfig payConfig, WechatType wechatType) {
        model.addAttribute("config_signature", payConfig.getWxConfig().sha1Sign());
        model.addAttribute("appid", ProductConfig.get(wechatType.getAppId()));
        model.addAttribute("config_timestamp", payConfig.getWxConfig().getTimestamp());
        model.addAttribute("config_nonceStr", payConfig.getWxConfig().getNonce());
        model.addAttribute("pay_timestamp", payConfig.getWxConfig().getTimestamp());
        model.addAttribute("pay_nonceStr", payConfig.getWxConfig().getNonce());
        model.addAttribute("pay_package", payConfig.getPayPackage());
        model.addAttribute("pay_signType", payConfig.getSignType());
        model.addAttribute("pay_paySign", payConfig.md5Sign(wechatType));
        model.addAttribute("pay_backUrl", payConfig.getBackUrl());
    }

    protected String generatePrepayId(String productName, String orderId, BigDecimal price, String notifyUrl, String mchID) throws DocumentException {
        //非线上环境生成假的
        if (RuntimeMode.le(Mode.STAGING)) {
            return String.valueOf(RandomUtils.nextInt(10000, 100000));
        }

        if (!notifyUrl.startsWith("http://") && !notifyUrl.startsWith("https://")) {
            notifyUrl = "https://" + notifyUrl;
        }

        // 统一为https 开头的。
        if (!notifyUrl.startsWith("https")) {
            notifyUrl = StringUtils.replace(notifyUrl, "http", "https");
        }

        String openId = getRequestContext().getAuthenticatedOpenId();
        if (StringUtils.isBlank(openId)) {
            logger.warn("No openid found from cookie,orderid:{}", orderId);
            return null;
        }

        Map<String, Object> params = new TreeMap<>();
        String appId = ProductConfig.get(WechatType.PARENT.getAppId());
        if (StringUtils.equals(CHIPS_MCH_ID, mchID)) {
            appId = ProductConfig.get(WechatType.CHIPS.getAppId());
        }
        params.put("appid", appId);
        params.put("mch_id", mchID);
        params.put("nonce_str", UUID.randomUUID().toString().replaceAll("-", ""));
        params.put("body", productName);
        // 处理订单ID 加上时分秒
        params.put("out_trade_no", orderId);
        params.put("total_fee", price.multiply(BigDecimal.valueOf(100)).intValue());
        params.put("spbill_create_ip", getRequestContext().getRealRemoteAddr());
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        params.put("openid", getRequestContext().getAuthenticatedOpenId());
        params.put("sign", WechatSignUtils.md5Sign(params));

        String content = XmlConvertUtils.toXml(params);
        StringEntity entity = new StringEntity(content, ContentType.create("application/xml", Consts.UTF_8));
        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).post(WECHAT_UNIFIED_ORDER_URL).entity(entity).execute();
        if (StringUtils.isEmpty(response.getResponseString())) {
            logger.error("Request for prepay_id failed, params:[{}],response:{}", JsonUtils.toJson(params), JsonUtils.toJson(response));
        } else {
            Map<String, String> result = XmlConvertUtils.toMap(response.getResponseString());
            if ("SUCCESS".equals(result.get("return_code"))) {
                if ("SUCCESS".equals(result.get("result_code"))) {
                    // wechatWebCacheSystem.CBS.flushable.add(key, 2 * 60 * 60, prepayId); //prepayId有效期2个小时
                    return result.get("prepay_id");
                } else {
                    logger.error("Prepay order {} failed,err_code_des:{},err_code:{},param:{}", orderId, result.get("err_code_des"), result.get("err_code"), JsonUtils.toJson(params));
                }
            } else {
                logger.error("Prepay order {} failed,return_msg:{},params:{}", orderId, result.get("return_msg"), JsonUtils.toJson(params));
            }
        }
        return null;
    }

    protected boolean isPayForTest() {
        return RuntimeMode.le(Mode.STAGING);
    }
}
