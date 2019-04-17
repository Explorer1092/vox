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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.galaxy.service.partner.api.service.ThirdPartyService;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.payment.PaymentRequestForm;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.config.client.BusinessActivityManagerClient;
import com.voxlearning.utopia.service.coupon.api.constants.CouponUserStatus;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.email.api.EmailService;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MicroCourseLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.parent.api.support.PalaceMuseumProductSupport;
import com.voxlearning.utopia.service.sms.api.SmsService;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import com.voxlearning.utopia.service.vendor.api.constant.OrderResult;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.temp.ApplePayErrorMessage;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

import static com.voxlearning.utopia.api.constant.OperationSourceType.*;
import static com.voxlearning.washington.controller.open.ApiConstants.*;


/**
 * @author Hailong Yang
 * @version 0.1
 * @since 2015/10/13
 */
@Controller
@RequestMapping(value = "/v1/parent/order")
@Slf4j
public class ParentOrderApiController extends AbstractParentApiController {

    // 新的订单中心使用的类型 By Wyc 2016-12-21
    private static final String USER_ORDER_PRODUCT_TYPE = "order";

    private static final String FINANCE_RECHARGE = "recharge";

    private static final String url_sandbox = "https://sandbox.itunes.apple.com/verifyReceipt";
    private static final String url_verify = "https://buy.itunes.apple.com/verifyReceipt";

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private FinanceServiceClient financeServiceClient;
    @Inject
    private MicroCourseLoaderClient microCourseLoaderClient;
    @Inject
    private BusinessActivityManagerClient businessActivityManagerClient;
    @Inject
    private CouponLoaderClient couponLoaderClient;

    @ImportService(interfaceClass = EmailService.class)
    private EmailService emailService;
    @ImportService(interfaceClass = ThirdPartyService.class)
    private ThirdPartyService thirdPartyService;
    @ImportService(interfaceClass = SmsService.class)
    private SmsService smsService;

    private static final List<Long> APPLE_PAY_SANDBOX_USERS = new ArrayList<>();

    static {
        APPLE_PAY_SANDBOX_USERS.add(211410507L);
        APPLE_PAY_SANDBOX_USERS.add(215917181L);
        APPLE_PAY_SANDBOX_USERS.add(2356054L);
        APPLE_PAY_SANDBOX_USERS.add(229234732L);
        APPLE_PAY_SANDBOX_USERS.add(20001L);
    }

    @RequestMapping(value = "confirm/{paymentGatewayName}.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage confirm(@PathVariable final String paymentGatewayName) {
        String orderProductType = getRequestString(ORDER_PRODUCT_TYPE);
        String orderId = getRequestString(RES_PARENT_APP_ORDER_ID);
        String ip = getRequestString(RES_IP);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        MapMessage mapMessage = new MapMessage();

        try {
            validateRequired(RES_PARENT_APP_ORDER_ID, "订单号");
            validateRequired(RES_IP, "IP");
            validateRequired(ORDER_PRODUCT_TYPE, "订单产品类型");
            validateRequest(RES_PARENT_APP_ORDER_ID, RES_IP, ORDER_PRODUCT_TYPE);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        try {
            UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
            if (userOrder != null) {

                if (!userOrder.canBePaid()) {
                    return MapMessage.errorMessage("订单不可支付");
                }

                if (StringUtils.isNotBlank(userOrder.getCouponRefId())) {
                    CouponUserRef couponUserRef = couponLoaderClient.loadCouponUserRefById(userOrder.getCouponRefId());
                    if (couponUserRef != null && couponUserRef.getStatus() == CouponUserStatus.Used) {
                        return MapMessage.errorMessage("优惠券已被使用");
                    }
                }
            }

            String gatewayName = getPaymentGatewayName(paymentGatewayName);
            PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(gatewayName);

            PaymentRequest paymentRequest;
            User user = getApiRequestUser();
            switch (orderProductType) {
                case USER_ORDER_PRODUCT_TYPE:
                    paymentRequest = getPaymentRequestForUserOrder(user, orderId, ip, gatewayName);
                    break;
                case FINANCE_RECHARGE:
                    paymentRequest = getPaymentRequestForRecharge(user, orderId, ip, gatewayName);
                    break;
                default:
                    paymentRequest = null;
                    break;
            }

            if (null == paymentRequest) {
                throw new UtopiaRuntimeException(RES_RESULT_UNKNOW_PAYMENT_TYPE);
            }

            PaymentRequestForm paymentRequestForm = paymentGateway.getPaymentRequestForm(paymentRequest);
            if (MapUtils.isNotEmpty(paymentRequestForm.getFormFields())) {
                Map<String, Object> result = new HashMap<>();
                result.put("signMap", paymentRequestForm.getFormFields());
                result.put("result_code", "SUCCESS");
                result.put("type", gatewayName);
                mapMessage.putAll(result);
                mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
                return mapMessage;
            } else {
                mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                mapMessage.add(RES_MESSAGE, "调用第三方支付失败");
                return mapMessage;
            }
        } catch (UtopiaRuntimeException ex) {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, ex.getMessage());
            return mapMessage;
        } catch (Exception ex) {
            log.error("create order failed. orderId:{},orderProductType:{}, ip:{}, ver:{}",
                    orderId, orderProductType, ip, ver, ex);
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, RES_RESULT_UNKNOW_PAYMENT_TYPE);
            return mapMessage;
        }
    }

    /**
     * 查询订单结果订单
     */
    @RequestMapping(value = "orderTail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage orderTail() {
        String orderType = getRequestString(ORDER_PRODUCT_TYPE);
        String orderId = getRequestString(RES_PARENT_APP_ORDER_ID);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);

        String h5Request = getRequestString("from");
        boolean isAppRequest = !(StringUtils.isNotEmpty(h5Request) && "web".equals(h5Request) && RuntimeMode.lt(Mode.STAGING));

        MapMessage mapMessage = new MapMessage();

        try {
            validateRequired(RES_PARENT_APP_ORDER_ID, "订单号");
            validateRequired(ORDER_PRODUCT_TYPE, "订单产品类型");
            if (isAppRequest) {
                validateRequest(RES_PARENT_APP_ORDER_ID, ORDER_PRODUCT_TYPE);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        try {
            switch (orderType) {
                // 作业币充值
                case FINANCE_RECHARGE:
                    FinanceFlow flow = financeServiceClient.getFinanceService()
                            .loadFinanceFlow(orderId)
                            .getUninterruptibly();
                    if (flow == null) {
                        throw new UtopiaRuntimeException(RES_RESULT_ORDER_NOT_EXIST);
                    }
                    //支付结果页的文案
                    mapMessage.put("content", "恭喜您成功购买学贝，您可以在一起作业学生App和家长通使用学贝购买一起作业的自学产品");
                    mapMessage.put("url", "/view/mobile/common/workcoin/paysuccess");
                    mapMessage.put("type", "NONE_VIEW");
                    if (Objects.equals(FinanceFlowState.SUCCESS.name(), flow.getState())) {
                        mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
                        mapMessage.add(RES_IS_PAY, true);
                        return mapMessage;
                    }
                    break;
                case USER_ORDER_PRODUCT_TYPE:
                    // 具体逻辑往下看 processUserOrder
                    mapMessage = processUserOrder(orderId);
                    if (StringUtils.equals(SafeConverter.toString(mapMessage.get(RES_RESULT)), RES_RESULT_SUCCESS) && Boolean.TRUE.equals(mapMessage.get(RES_IS_PAY))) {
                        return mapMessage;
                    }
                    break;
                default:
                    throw new UtopiaRuntimeException(RES_RESULT_UNKNOW_PAYMENT_TYPE);
            }
            mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
            mapMessage.add(RES_IS_PAY, false);
            return mapMessage;
        } catch (UtopiaRuntimeException ex) {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, ex.getMessage());
            return mapMessage;
        } catch (Exception ex) {
            log.error("check order result error. orderId:{}, orderType:{}, ver:{}",
                    orderId, orderType, ver, ex);
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, "查询订单结果失败");
            return mapMessage;
        }
    }


    // applePay check
    @RequestMapping(value = "applepayverify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage applePayVerify() {
        String orderId = getRequestString(RES_PARENT_APP_ORDER_ID);
        String receipt = getRequestString(REQ_RECEIPT);
        String transactionId = getRequestString(REQ_TRANSACTION_ID);

        LogCollector.info("backend-general",
                MiscUtils.map(
                        "op", "applepayverify",
                        "model", "applepayverify-requestparam",
                        "env", RuntimeMode.getCurrentStage(),
                        "version", getRequestString("version"),
                        "oid", orderId,
                        "transactionId", transactionId,
                        "receipt", receipt,
                        "agent", getRequest().getHeader("User-Agent")
                ));

        MapMessage mapMessage = new MapMessage();

        try {
            validateRequired(RES_PARENT_APP_ORDER_ID, "订单号");
            validateRequired(REQ_RECEIPT, "凭据");
            if (StringUtils.isNoneBlank(getRequestString(REQ_SIG))) {
                validateRequest(RES_PARENT_APP_ORDER_ID, REQ_RECEIPT, REQ_TRANSACTION_ID);
            }

        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        // FIXME 这里做个兼容，如果oid==0，那么只存数据，不做任何处理
        if (Objects.equals("0", orderId) && StringUtils.isNoneBlank(getRequestString(REQ_SESSION_KEY))) {
            User apiUser = getApiRequestUser();
            if (apiUser != null) {
                userOrderServiceClient.getUserOrderService().saveAppleReceipt("DATASAVE", SafeConverter.toString(apiUser.getId()), receipt);
            }

            mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
            return mapMessage;
        }

        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        FinanceFlow flow = financeServiceClient.getFinanceService().loadFinanceFlow(orderId).take();
        if (userOrder == null && flow == null) {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, "未知的订单号");
            return mapMessage;
        }

        Long uid = 0L;
        if (userOrder != null) {
            uid = userOrder.getUserId();
        }

        if (flow != null) {
            uid = flow.getUserId();
        }

        // 进行校验
        String url = url_sandbox;
        if (RuntimeMode.current().ge(Mode.PRODUCTION) && !APPLE_PAY_SANDBOX_USERS.contains(uid)) {
            url = url_verify;
        }

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url)
                .json(MiscUtils.map("receipt-data", receipt))
                .execute();
        if (response.hasHttpClientException()) {
            // fixme 现在发现有 Connect to buy.itunes.apple.com:443 [buy.itunes.apple.com/17.154.66.75] failed: Read timed out 错误
            // fixme 这里做1次重试, 如果依然有失败，交给客户端处理，客户端在app重新启动的时候或者尝试购买的时候会再次调用这个接口进行处理
            response = HttpRequestExecutor.defaultInstance().post(url)
                    .json(MiscUtils.map("receipt-data", receipt))
                    .execute();
            if (response.hasHttpClientException()) {
                logger.error("apple Pay verify error {}", response.getHttpClientExceptionMessage());
                mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                mapMessage.add(RES_MESSAGE, "凭据验证失败");
                return mapMessage;
            }
        }

        Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());

        LogCollector.info("backend-general",
                MiscUtils.map(
                        "op", "applepayverify",
                        "model", "applepayverify-validateresult",
                        "env", RuntimeMode.getCurrentStage(),
                        "version", getRequestString("version"),
                        "uid", uid,
                        "oid", orderId,
                        "result", JsonUtils.toJson(resultMap)
                ));

        String status = SafeConverter.toString(resultMap.get("status"));
        if (Objects.equals(status, "0")) {
            if (userOrder != null) {

                // save receipt data
                MapMessage receiptResult = userOrderServiceClient.getUserOrderService().saveAppleReceipt("ORDER", userOrder.genUserOrderId(), receipt, transactionId);
                if (!receiptResult.isSuccess()) {
                    mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
                    mapMessage.add(RES_MESSAGE, "保存凭据失败");
                    return mapMessage;
                }

                // 验证通过， 进行订单支付后处理
                PaymentCallbackContext paymentCallbackContext = new PaymentCallbackContext(PaymentConstants.PaymentGatewayName_ApplePay_ParentApp,
                        PaymentGateway.CallbackAction_Notify);
                paymentCallbackContext.setVerifiedPaymentData(new PaymentVerifiedData());
                paymentCallbackContext.getVerifiedPaymentData().setExternalTradeNumber("");
                paymentCallbackContext.getVerifiedPaymentData().setExternalUserId("");
                paymentCallbackContext.getVerifiedPaymentData().setPayAmount(userOrder.getOrderPrice());
                paymentCallbackContext.getVerifiedPaymentData().setTradeNumber(orderId);
                businessUserOrderServiceClient.processUserOrderPayment(paymentCallbackContext);
                mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
            }

            if (flow != null) {

                MapMessage verifyMap = verifyApplePayFinanceReceipt(flow, resultMap, transactionId);
                if (!verifyMap.isSuccess()) {
                    mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    mapMessage.add(RES_MESSAGE, "凭据验证失败");
                    return mapMessage;
                }

                // overwrite transaction id
                transactionId = SafeConverter.toString(verifyMap.get("transaction_id"));

                // save receipt data
                MapMessage receiptResult = userOrderServiceClient.getUserOrderService().saveAppleReceipt("FINANCE", flow.getId(), receipt, transactionId);
                if (!receiptResult.isSuccess()) {
                    mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
                    mapMessage.add(RES_MESSAGE, "保存凭据失败");
                    return mapMessage;
                }

                // 充值
                boolean rechargeResult = financeServiceClient.getFinanceService()
                        .recharge(orderId, flow.getPaymentAmount(), transactionId)
                        .getUninterruptibly();
                if (rechargeResult) {
                    User user = raikouSystem.loadUser(flow.getUserId());
                    if (null != user && user.fetchUserType() == UserType.PARENT) {
                        userLevelService.parentRecharge(user.getId(), flow.getAmount());
                    }
                    mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
                }
            }

            return mapMessage;
        } else {
            // 验证失败 获取信息
            String errorMessage = ApplePayErrorMessage.getErrorMap().get(status);
            logger.error("applePay verify error, msg : {}", errorMessage);
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, "凭据验证失败");
            return mapMessage;
        }
    }

    private MapMessage verifyApplePayFinanceReceipt(FinanceFlow flow, Map<String, Object> receiptResult, String transactionid) {
        try {
            // 如果有 transactionid，直接根据 transactionid 进行验证
            if (StringUtils.isNoneBlank(transactionid)) {
                Map<String, Object> receipt = (Map<String, Object>) receiptResult.get("receipt");
                List<Map<String, Object>> inappList = (List<Map<String, Object>>) receipt.get("in_app");

                if (CollectionUtils.isEmpty(inappList)) {
                    logger.error("verifyApplePayFinanceReceipt failed. flowId:{}, receipt result:{}", flow.getId(), JsonUtils.toJson(receiptResult));
                    return MapMessage.errorMessage();
                }

                for (Map<String, Object> inapp : inappList) {
                    String inappTransId = SafeConverter.toString(inapp.get("transaction_id"));
                    if (Objects.equals(inappTransId, transactionid)) {
                        return MapMessage.successMessage().add("transaction_id", transactionid);
                    }
                }

                logger.error("verifyApplePayFinanceReceipt failed. flowId:{}, transId:{}, receipt result:{}", flow.getId(), transactionid, JsonUtils.toJson(receiptResult));
                return MapMessage.errorMessage();

            } else {
                if (StringUtils.isBlank(flow.getAttributes())) {
                    return MapMessage.successMessage();
                }

                Map<String, Object> attrs = JsonUtils.fromJson(flow.getAttributes());
                String skuId = SafeConverter.toString(attrs.get("skuId"));
                if (StringUtils.isBlank(skuId)) {
                    return MapMessage.successMessage();
                }

                Map<String, Object> receipt = (Map<String, Object>) receiptResult.get("receipt");
                List<Map<String, Object>> inappList = (List<Map<String, Object>>) receipt.get("in_app");

                if (CollectionUtils.isEmpty(inappList)) {
                    logger.error("verifyApplePayFinanceReceipt failed. flowId:{}, receipt result:{}", flow.getId(), JsonUtils.toJson(receiptResult));
                    return MapMessage.errorMessage();
                }

                for (Map<String, Object> inapp : inappList) {
                    String productId = SafeConverter.toString(inapp.get("product_id"));
                    if (StringUtils.equals(productId, skuId)) {
                        return MapMessage.successMessage().add("transaction_id", SafeConverter.toString(inapp.get("transaction_id")));
                    }
                }

                logger.error("verifyApplePayFinanceReceipt failed. flowId:{}, receipt result:{}", flow.getId(), JsonUtils.toJson(receiptResult));
                return MapMessage.errorMessage();
            }
        } catch (Exception e) {
            logger.error("verifyApplePayFinanceReceipt failed. flowId:{}, receipt result:{}", flow.getId(), JsonUtils.toJson(receiptResult), e);
            return MapMessage.errorMessage();
        }
    }

    private String getPaymentGatewayName(String paymentGatewayName) {
        if ("wechat".equals(paymentGatewayName)) {
            //兼容旧版本
            return PaymentConstants.PaymentGatewayName_Wechat_ParentApp;
        }

        return paymentGatewayName;
    }

    private PaymentRequest getPaymentRequestForRecharge(User payUser, String orderId, String ip, String paymentGatewayName) {
        FinanceFlow flow = financeServiceClient.getFinanceService()
                .loadFinanceFlow(orderId)
                .getUninterruptibly();
        if (flow == null) {
            throw new UtopiaRuntimeException(RES_RESULT_ORDER_UNKNOW_MSG);
        }
        if (Objects.equals(flow.getState(), FinanceFlowState.SUCCESS.name())) {
            throw new UtopiaRuntimeException(RES_RESULT_ORDER_HAD_PAID);
        }
        BigDecimal amount = flow.getPaymentAmount();
        if (PaymentGateway.getUsersForPaymentTest(flow.getUserId())) {
            amount = new BigDecimal(0.01);
        }
        return generatePaymentRequest(payUser, orderId, "学贝充值", paymentGatewayName, amount, ip, "/payment/notify/recharge/");
    }

    private PaymentRequest generatePaymentRequest(User payUser, String orderId, String productName, String payMethod, BigDecimal amount, String ip, String callbackUrl) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setTradeNumber(orderId);
        paymentRequest.setProductName(productName);
        paymentRequest.setPayMethod(payMethod);
        paymentRequest.setSpbillCreateIp(ip);
        paymentRequest.setPayAmount(amount);
        if (payMethod.startsWith("alipay")) {
            paymentRequest.setPayUser(payUser.getId());  //微信原生支付总提示 商户订单号重复，将报文attach属性设值成0就可以
        }
        paymentRequest.setCallbackBaseUrl(ProductConfig.getMainSiteBaseUrl() + callbackUrl);
        return paymentRequest;
    }

    //--------------------------------------------------------------------
    //------------------            新订单系统           -----------------
    //--------------------------------------------------------------------

    private PaymentRequest getPaymentRequestForUserOrder(User payUser, String orderId, String ip, String paymentGatewayName) {
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        if (userOrder == null) {
            throw new UtopiaRuntimeException(RES_RESULT_ORDER_UNKNOW_MSG);
        }
        if (!userOrder.canBePaid()) {
            throw new UtopiaRuntimeException(RES_RESULT_ORDER_HAD_PAID);
        }
        // app type order check
        if (userOrder.getOrderType() == OrderType.app) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userOrder.getUserId());
            if (studentDetail == null) {
                throw new UtopiaRuntimeException("此订单不属于这个学生");
            } else if (studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz()) {
                throw new UtopiaRuntimeException("本产品不提供毕业班购买");
            }
            MapMessage checkMsg = userOrderServiceClient.checkDaysToExpireForCreateNewOrder(userOrder.getUserId(),
                    OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()));
            if (!checkMsg.isSuccess()) {
                throw new UtopiaRuntimeException("您已经开通了 " + userOrder.getProductName() + "，可以直接学习使用哦！");
            }
        }
        String productName = userOrder.getProductName();
        BigDecimal amount = userOrderServiceClient.getOrderCouponDiscountPrice(userOrder);
        if (PaymentGateway.getUsersForPaymentTest(userOrder.getUserId())) {
            amount = new BigDecimal(0.01);
        }
        return generatePaymentRequest(payUser, orderId, productName, paymentGatewayName, amount, ip, "/payment/notify/order");
    }

    private MapMessage processUserOrder(String orderId) {
        MapMessage mapMessage = new MapMessage();
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        if (userOrder == null || userOrder.getOrderType() == null) {
            throw new UtopiaRuntimeException(RES_RESULT_ORDER_NOT_EXIST);
        }
        OrderType type = userOrder.getOrderType();
        // 往后的校验在这里
        switch (type) {
            case pic_listen:
                MapMessage picMessage;
                if (OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook) {
                    picMessage = generateResultMessage(userOrder, OrderResult.PICLISTENBOOK_ORDER_RESULT);
                } else {
                    picMessage = generateResultMessage(userOrder, OrderResult.PICLISTEN_ORDER_RESULT);
                }
                if (null != picMessage) {
                    return picMessage;
                }
            case app:
                MapMessage message = generateResultMessage(userOrder, OrderResult.AFENTI_ORDER_RESULT);
                if (null != message) {
                    return message;
                }
                break;
            case yi_qi_xue:
                MapMessage resultMessage = generateResultMessage(userOrder, OrderResult.YIQIXUE_ORDER_RESULT);
                if (resultMessage != null) {
                    return resultMessage;
                }
                break;
            case chips_english:
                mapMessage.put("url", "/view/mobile/parent/parent_ai/payment_success?orderId=" + orderId);
                mapMessage.put("type", "WEB_VIEW");
                if (PaymentStatus.Paid == userOrder.getPaymentStatus()) {
                    //支付结果页的文案
                    mapMessage.put("content", "您已购买成功");
                    mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
                    mapMessage.add(RES_IS_PAY, true);
                } else {
                    mapMessage.put("content", "");
                }
                break;
            default:
                throw new UtopiaRuntimeException(RES_RESULT_UNKNOW_PAYMENT_TYPE);
        }
        return mapMessage;
    }

    private MapMessage generateResultMessage(UserOrder userOrder, OrderResult orderResult) {
        MapMessage mapMessage = new MapMessage();

        //支付应用可以在那个地方使用
        VendorApps afentiVendor = vendorLoaderClient.loadVendor(userOrder.getOrderProductServiceType());
        String platform = "";
        if (afentiVendor != null && afentiVendor.getPlaySources() != null) {
            if (afentiVendor.matchPlaySources(app)) {
                platform += "一起作业学生APP、";
            }
            if (afentiVendor.matchPlaySources(wechat) && VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "1.6.5") > 0) {
                platform += "一起作业家长通、";
            }
            if (afentiVendor.matchPlaySources(pc)) {
                platform += "一起作业网、";
            }
        }
        if (platform.endsWith("、")) {
            platform = platform.substring(0, platform.length() - 1);
        }

        //支付结果页的文案
        mapMessage.put("content", StringUtils.formatMessage(orderResult.content,
                userOrder.getProductName(),
                platform));
        String url = orderResult.getUrl();
        //如果是一起学订单，跳转到一起学订单支付结果页
        if (OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) == OrderProductServiceType.YiQiXue) {
            url = ProductConfig.get17XueParentUrl() + orderResult.url + userOrder.getProductId();
        }
        if (OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) == OrderProductServiceType.PalaceMuseum) {
            url = getGalaxySiteUrl() + "/karp/gugong/index/payresult";
        }
        if (userOrder.getProductId().equals(PalaceMuseumProductSupport.GIFT_PRODUCT_ID)) {
            url = getGalaxySiteUrl() + "/karp/gugong/index/giftPayResult?oid=" + userOrder.genUserOrderId();
        }
        mapMessage.put("url", url);
        mapMessage.put("type", orderResult.type);

        if (PaymentStatus.Paid == userOrder.getPaymentStatus()) {
            mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
            mapMessage.add(RES_IS_PAY, true);
            return mapMessage;
        }

        return null;
    }
}

