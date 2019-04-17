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

package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.Password;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.payment.PaymentRequestForm;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.coupon.api.constants.CouponUserStatus;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.OrderProductSalesType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.service.financial.Finance;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil.testAmount;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Shuai Huan
 * @version 0.1
 * @since 2014/6/9
 */
@Controller
@RequestMapping(value = "/apps/order")
@Slf4j
public class AppOrderController extends AbstractApiController {

    @Inject private AsyncVendorServiceClient asyncVendorServiceClient;
    @Inject private IntegralLoaderClient integralLoaderClient;
    @Inject private FinanceServiceClient financeServiceClient;
    @Inject private CouponLoaderClient couponLoaderClient;

//    public static void main(String[] args) {
//        String appKey = "TravelAmerica";
//        String appSecret = "Tzpw9PIj8zM";
//        String sessionKey = "7fbe8773e13e0c1a362590339276b37c";
//        String orderId = "0166ab526f96448d8faf38a1468ac53e";
//        String orderToken = "1882e07c497845e897426f005f3c81875fa95d1d31559d99b61d2d62cf01ec0b31824af9d1bdfe7870f7401a0a85c7cc";
//
//        Map<String, String> paramMap = new HashMap<>();
//        paramMap.put("order_id", orderId);
//        paramMap.put("order_token", orderToken);
//        paramMap.put("app_key", appKey);
//        paramMap.put("session_key", sessionKey);
//
//        String sig = DigestSignUtils.signMd5(paramMap, appSecret);
//        paramMap.put("sig", sig);
//
//        String url = UrlUtils.buildUrlQuery("http://127.0.0.1:8081/apps/order/mobile/confirm.vpage", paramMap);
//        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
//
//        System.out.println(response.getResponseString());
//    }

    @RequestMapping(value = "/submit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String submitOrder(Model model) {

        try {
            validateRequired(REQ_ORDER_ID);
            validateRequest(REQ_ORDER_ID, REQ_ORDER_TOKEN);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "open/apporder";
        }
        StudentDetail student = currentStudentDetail();
        if (student == null) {
            model.addAttribute("error", "请重新登陆!");
            return "open/apporder";
        }

        if (student.getClazz() != null && student.getClazz().isTerminalClazz()) {
            model.addAttribute("error", "本产品不提供毕业班购买");
            return "open/apporder";
        }

        UserOrder order = userOrderLoaderClient.loadUserOrder(getRequestString(REQ_ORDER_ID));
        if (order == null || !order.getOrderToken().equals(getRequestString(REQ_ORDER_TOKEN))) {
            model.addAttribute("error", "订单数据错误!");
            return "open/apporder";
        }

        if (order.getPaymentStatus() == PaymentStatus.Paid) {
            model.addAttribute("error", "这个订单已经被支付过了");
            return "open/apporder";
        }
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(order.getProductId());
        if (product == null) {
            model.addAttribute("error", "购买的产品不存在");
            return "open/apporder";
        }
        // 校验用户支付限额
        MapMessage validateResult = validateStudentPay(student.getId(), product);
        if (!validateResult.isSuccess()) {
            model.addAttribute("error", validateResult.getInfo());
            return "open/apporder";
        }

        // 已经关闭付费的产品去公告说明页
        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()).isOrderClosed()) {
            model.addAttribute("type", order.getOrderProductServiceType());
            return "/apps/afenti/order/offlineinfo";
        }

        UserIntegral userIntegral = integralLoaderClient.getIntegralLoader().loadUserIntegral(student.toSimpleUser());
        Finance userFiance = financeServiceClient.getFinanceService()
                .loadUserFinance(student.getId())
                .getUninterruptibly();
        if (userFiance == null) {
            userFiance = new Finance();
            userFiance.setBalance(new BigDecimal(0));
            userFiance.setUserId(student.getId());
        }
        if (order.getOrderPrice().compareTo(userFiance.getBalance()) == 1) {
            // 存在多个浏览器间登录用户不一致的情况，如果发现登录用户的ID和订单用户ID不一样，
            // 那么删除当前登录用户的Cookie，要求用户重新登录后再进行支付处理
            if (currentUserId() != null && !currentUserId().equals(student.getId())) {
                getWebRequestContext().cleanupAuthenticationStates();
            }
            return "redirect:/student/center/recharging.vpage?types=recharging-go&vendorOrderId=" + order.getId();
        }
        model.addAttribute("userIntegral", userIntegral);
        model.addAttribute("userFinance", userFiance);
        model.addAttribute("appOrder", order);
        model.addAttribute("appName", getApiRequestApp().getCname());
        model.addAttribute("user", student);
        model.addAttribute("orderId", order.genUserOrderId());
        model.addAttribute("captchaToken", RandomUtils.randomString(24));
        return "open/apporder";
    }

    @RequestMapping(value = "/confirm.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage confirmOrder() {
        MapMessage resultMap = new MapMessage();

        String captchaToken = getRequestString("captchaToken");
        String captchaCode = getRequestString("captchaCode");

        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            resultMap.setSuccess(false);
            resultMap.add("message", "验证码输入错误，请重新输入。");
            return resultMap;
        }
        StudentDetail user = currentStudentDetail();
        if (user == null) {
            resultMap.setSuccess(false);
            resultMap.add("message", "获取登陆用户信息错误。");
            return resultMap;
        }

        //毕业学生不能购买产品
        if (user.getClazz() != null && user.getClazz().isTerminalClazz()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "本产品不提供毕业班购买");
            return resultMap;
        }

        VendorApps vendorApps = getApiRequestApp();
        String orderId = getRequestString(ApiConstants.RES_ORDER_ID);

        if (StringUtils.equals(vendorApps.getAppKey(), SanguoDmz.name())
                || StringUtils.equals(vendorApps.getAppKey(), A17ZYSPG.name())
                || StringUtils.equals(vendorApps.getAppKey(), TravelAmerica.name())
                || StringUtils.equals(vendorApps.getAppKey(), PetsWar.name())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "即日起，本产品的开通功能将永久性关闭。在包月有效期限内，您仍可继续使用。");
            return resultMap;
        }

        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        if (order == null || !order.getOrderToken().equals(getRequestString(ApiConstants.RES_ORDER_TOKEN))
                || !order.getUserId().equals(user.getId())) {
            resultMap.setSuccess(false);
            resultMap.add("message", "系统错误，请联系管理员进行处理！");
            return resultMap;
        }

        // validate the payment password
        String payPassword = getRequestParameter("paymentPassword", "");
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        Password password = Password.of(ua.getPaymentPassword());

        if (StringUtils.isNotEmpty(ua.getPaymentPassword())) {//有支付密码必须验证
            if (StringUtils.isEmpty(payPassword) || !StringUtils.equals(Password.obscurePassword(payPassword, password.getSalt()), password.getPassword())) {
                resultMap.setSuccess(false);
                resultMap.add("message", "支付密码错误！");
                return resultMap;
            }
        }

        if (order.getPaymentStatus() == PaymentStatus.Paid) {
            resultMap.setSuccess(false);
            resultMap.add("message", "这个订单已经被支付过了！");
            return resultMap;
        }
        Map<String, Object> m = MiscUtils.m(
                "vendorApps", vendorApps,
                "order", order,
                "user", user);
        MapMessage paymentResult;
        try {
            paymentResult = AtomicLockManager.instance().wrapAtomic(businessFinanceServiceClient)
                    .keyPrefix("HWCOIN_PAY")
                    .keys(order.getId())
                    .proxy()
                    .doHwcoinPaymnet(m);
        } catch (CannotAcquireLockException e) {
            resultMap.setSuccess(false);
            resultMap.add("message", "正在处理，请不要重复提交！");
            return resultMap;
        }

        if (paymentResult == null || !paymentResult.isSuccess()) {
            resultMap.setSuccess(false);
            resultMap.add("message", paymentResult == null ? "" : paymentResult.getInfo());
            return resultMap;
        }
        order = userOrderLoaderClient.loadUserOrder(orderId);

        // 获取appId
        asyncVendorServiceClient.getAsyncVendorService()
                .sendVendorPaymentCallBackNotify(vendorApps,
                        order,
                        SafeConverter.toLong(order.getProductId()),
                        null, "", "")
                .awaitUninterruptibly();

        resultMap.setSuccess(true);
        resultMap.add(RES_RESULT, "success");
        return resultMap;
    }

    /**
     * 第三方应用（除了阿分题）订单支付---进入支付确认页
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/mobile/confirm.vpage", method = RequestMethod.GET)
    public String mobileConfirm(Model model) {
        try {
            validateRequired(REQ_ORDER_ID, "订单号");
            validateRequest(REQ_ORDER_ID, REQ_ORDER_TOKEN, REQ_RETURN_URL);

            UserOrder order = userOrderLoaderClient.loadUserOrder(getRequestString(REQ_ORDER_ID));
            if (null == order || !order.getOrderToken().equals(getRequestString(REQ_ORDER_TOKEN))) {
                model.addAttribute("error", "订单数据错误");
                return "/paymentmobile/confirm";
            }

            if (order.getPaymentStatus() == PaymentStatus.Paid) {
                model.addAttribute("error", "这个订单已经被支付过了");
                return "/paymentmobile/confirm";
            }

            //毕业学生不能购买产品
            User apiRequestUser = getApiRequestUser();
            if (apiRequestUser == null) {
                model.addAttribute("error", "请重新登录");
                return "/paymentmobile/confirm";
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(apiRequestUser.getId());
            if (studentDetail != null && studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz()) {
                model.addAttribute("error", "本产品不提供毕业班购买");
                return "/paymentmobile/confirm";
            }

            boolean IOSFinanceRecharge = true;
            if (studentDetail != null) {
                IOSFinanceRecharge = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "Order", "FinanceRecharge");
            }
            model.addAttribute("IOSFinanceRecharge",IOSFinanceRecharge);

            //H5支付灰度变量
            boolean isOpenH5payment = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "Order", "H5Pay");
            model.addAttribute("isOpenH5payment",isOpenH5payment);

            String userAgent = getRequest().getHeader("User-Agent").toLowerCase();
            //学生端学贝支付灰度测试
            boolean isFinancePayment = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "Order", "FinancePay");
            if(isFinancePayment){
                List<CouponShowMapper> mappers = userOrderLoaderClient.loadOrderUsableCoupons(order, apiRequestUser.getId());
                model.addAttribute("isFinancePayment",userAgent.contains("17student"));
                //查询学贝够不够支付
                List<StudentParent> studentParentList = parentLoaderClient.loadStudentParents(apiRequestUser.getId());
                boolean isFinanceEnougthPay = false;
                List<Finance> finances = new ArrayList<>();
                for (StudentParent parent : studentParentList) {
                    Finance parFinance = financeServiceClient.getFinanceService()
                            .createUserFinanceIfAbsent(parent.getParentUser().getId())
                            .getUninterruptibly();
                    if (Objects.nonNull(parFinance)) {
                        finances.add(parFinance);
                    }
                }
                //查询自己的学贝
                Finance stuFinance = financeServiceClient.getFinanceService().createUserFinanceIfAbsent(apiRequestUser.getId())
                        .getUninterruptibly();
                if (Objects.nonNull(stuFinance)){
                    finances.add(stuFinance);
                }
                if(CollectionUtils.isNotEmpty(finances)){
                    for(Finance finance : finances){
                        if(CollectionUtils.isNotEmpty(mappers)){
                            for(CouponShowMapper mapper : mappers){
                                BigDecimal newOrderPrice = new BigDecimal(mapper.getDiscountPrice());
                                if(finance.getBalance().compareTo(newOrderPrice) >= 0){
                                    isFinanceEnougthPay = true;
                                    break;
                                }
                            }
                        }else{
                            if(finance.getBalance().compareTo(order.getOrderPrice()) >= 0){
                                isFinanceEnougthPay = true;
                                break;
                            }
                        }
                    }
                }

                if(!isFinanceEnougthPay && userAgent.contains("17student")){
                    if(CollectionUtils.isNotEmpty(studentParentList)){
                        model.addAttribute("isBindParent",true);
                        sendPushMsgAndSms(studentDetail,studentParentList,order.getProductName());
                    }else{
                        model.addAttribute("isBindParent",false);
                        List<String> mobiles = new ArrayList<>();
                        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(studentDetail.getId());
                        if(StringUtils.isNotBlank(authentication.getSensitiveMobile())){
                            mobiles.add(SensitiveLib.decodeMobile(authentication.getSensitiveMobile()));
                        }
                        sendMessage(studentDetail.fetchRealname(),order.getProductName(),mobiles);
                    }
                    return "/paymentmobile/finance";
                }
            }

            //以下为confirm页面必选参数
            model.addAttribute("orderId", order.genUserOrderId());
            model.addAttribute("productName", order.getProductName());
            model.addAttribute("amount", order.getOrderPrice());
            //以下为confirm页面可选参数,只在apps订单才有
            model.addAttribute("orderSeq", order.getOrderSeq());
            model.addAttribute("orderToken", order.getOrderToken());
            model.addAttribute("returnUrl", getRequestString(REQ_RETURN_URL));
            model.addAttribute("appKey", getRequestString(REQ_APP_KEY));
            model.addAttribute("sessionKey", getRequestString(REQ_SESSION_KEY));
            model.addAttribute("hideTopTitle",getRequestBool("hideTopTitle",false));
            model.addAttribute("hideAppTitle",getRequestBool("hideAppTitle",false));
            model.addAttribute("appType",getRequestString("appType"));

            if (MapUtils.isNotEmpty(JsonUtils.fromJson(order.getProductAttributes()))) {
                model.addAttribute("productAttributes", JsonUtils.fromJson(order.getProductAttributes()));
            }
            // 学生是否开启支付权限
            StudentExtAttribute attribute = studentLoaderClient.loadStudentExtAttribute(apiRequestUser.getId());
            if (userAgent.contains("17parent") || attribute == null || attribute.fetchPayFreeStatus()) {
                return "/paymentmobile/confirm";
            } else {
                // 获取家长列表
                List<StudentParent> parents = parentLoaderClient.loadStudentParents(apiRequestUser.getId());
                if (CollectionUtils.isNotEmpty(parents)) {
                    List<Map<String, Object>> parentMaps = new ArrayList<>();
                    for (StudentParent parent : parents) {
                        Map<String, Object> p = new HashMap<>();
                        p.put("parentId", parent.getParentUser().getId());
                        p.put("callName", parent.getCallName());
                        parentMaps.add(p);
                    }
                    model.addAttribute("parentList", parentMaps);
                    return "/paymentmobile/authority";
                } else {
                    return "/paymentmobile/confirm";
                }
            }
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            model.addAttribute("error", "系统异常");
        }
        return "/paymentmobile/confirm";
    }



    @RequestMapping(value = "/mobile/confirm.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mobileConfirm() {
        MapMessage result = new MapMessage();
        try {
            validateRequired(REQ_ORDER_ID);
            validateRequired(REQ_PAYMENT_GATEWAY);

            User user = getApiRequestUser();
            if (user == null) {
                result.add(RES_RESULT, false);
                result.add(RES_MESSAGE, RES_RESULT_RELOGIN);
                return result;
            }

            String orderId = getRequestString(REQ_ORDER_ID);

            UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
            if (null == order || !order.getOrderToken().equals(getRequestString(RES_ORDER_TOKEN))
                    || (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) != ChipsEnglish
                    && OrderProductServiceType.safeParse(order.getOrderProductServiceType()) != PicListenBook && !order.getUserId().equals(user.getId()))) {
                result.add(RES_RESULT, false);
                result.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
                return result;
            }

            if (order.getPaymentStatus() == PaymentStatus.Paid) {
                result.add(RES_RESULT, false);
                result.add(RES_MESSAGE, RES_RESULT_ORDER_HAD_PAID);
                return result;
            }

            if(StringUtils.isNotBlank(order.getCouponRefId())){
                CouponUserRef couponUserRef = couponLoaderClient.loadCouponUserRefById(order.getCouponRefId());
                if(couponUserRef != null && couponUserRef.getStatus() == CouponUserStatus.Used){
                    result.add(RES_RESULT, false);
                    result.add(RES_MESSAGE, "优惠券已被使用");
                    return result;
                }
            }

            OrderProduct product = userOrderLoaderClient.loadOrderProductById(order.getProductId());
            if (product != null && product.getSalesType() != null && product.getSalesType() != OrderProductSalesType.ITEM_BASED) {
                MapMessage checkMsg = userOrderServiceClient.checkDaysToExpireForCreateNewOrder(order.getUserId(),
                        OrderProductServiceType.safeParse(order.getOrderProductServiceType()));
                if (!checkMsg.isSuccess()) {
                    result.add(RES_RESULT, false);
                    result.add(RES_MESSAGE, "您已经开通了 " + order.getProductName() + "，可以直接学习使用哦！");
                    return result;
                }
            }
            //添加支付日志
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(order.getUserId());
            userServiceRecord.setOperatorId(user.getId().toString());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.订单支付.name());
            userServiceRecord.setOperationContent(order.getId());
            String sys = getRequestString("mobile_sys");
            String model = getRequestString("mobile_model");
            userServiceRecord.setComments(StringUtils.formatMessage("手机系统:{}, 手机型号:{}", sys, model));
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            String gatewayName = getRequestString(REQ_PAYMENT_GATEWAY);  //gatewayName的值取自PaymentConstants
            PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(gatewayName);

            if (null != paymentGateway) {
                PaymentRequest paymentRequest = generatePaymentRequest(order,gatewayName);
                if(paymentRequest.getPayAmount().compareTo(BigDecimal.ZERO) == 0){
                    //如果支付金额为零的时候的特殊处理
                    PaymentCallbackContext context = buildPaymentCallbackContext(paymentRequest);
                    UserOrder zeroOrder = businessUserOrderServiceClient.processUserOrderPayment(context);
                    result.add(RES_RESULT, true);
                    if (zeroOrder.getPaymentStatus() == PaymentStatus.Paid) {
                        result.add(RES_ORDER_STATUS, PaymentStatus.Paid);
                    } else {
                        result.add(RES_ORDER_STATUS,PaymentStatus.Unpaid);
                    }
                    return result;
                }else{
                    PaymentRequestForm paymentRequestForm = paymentGateway.getPaymentRequestForm(paymentRequest);
                    result.add(RES_RESULT, true);
                    result.add("payParams", paymentRequestForm.getFormFields());
                    if(PaymentConstants.PaymentGatewayName_Alipay_Wap_StudentApp.equals(gatewayName)||
                            PaymentConstants.PaymentGatewayName_Alipay_Wap_ParentApp.equals(gatewayName) ){
                        result.add("wapForm",paymentRequestForm.getWapForm());
                    }else if(PaymentConstants.PaymentGatewayName_Wechat_H5_StudentApp.equals(gatewayName)||
                            PaymentConstants.PaymentGatewayName_Wechat_H5_ParentApp.equals(gatewayName)){
                        result.add("mwebUrl",paymentRequestForm.getFormFields().get("mwebUrl"));
                    }
                    return result;
                }
            } else {
                //目前只支持微信支付
                result.add(RES_RESULT, false);
                result.add(RES_MESSAGE, RES_RESULT_PAYMENT_NOT_SUPPORT);
                return result;
            }
        } catch (IllegalArgumentException ex) {
            logger.error(ex.getMessage(), ex);
            result.add(RES_RESULT, false);
            result.add(RES_MESSAGE, ex.getMessage());
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result.add(RES_RESULT, false);
            result.add(RES_MESSAGE, "系统异常");
            return result;
        }
    }

    @RequestMapping(value = "/mobile/wappaysuccess.vpage", method = RequestMethod.GET)
    public String wapPaySucess(Model model) {
        try{
            String returnUrl = getRequestString("game_url");
            String orderId = getRequestString(REQ_ORDER_ID);
            String gatewayName = getRequestString(REQ_PAYMENT_GATEWAY);
            String frontType = getRequestString("frontType");
            String appKey = getRequestString(REQ_APP_KEY);
            String appType = getRequestString("appType");
            if(gatewayName.startsWith("alipay")){
                if(StringUtils.isNotBlank(returnUrl)){
                    returnUrl = returnUrl.replaceAll("@","&");
                    if("Arithmetic".equals(appKey)) {
                        returnUrl += "#PaySuccess";
                    }
                }
            }else if(gatewayName.startsWith("wechatpay")){
                if("Arithmetic".equals(appKey)) {
                    returnUrl += "#PaySuccess";
                }
            }

            model.addAttribute("returnUrl",returnUrl);
            model.addAttribute("order_id",orderId);
            model.addAttribute("payment_gateway",gatewayName);
            model.addAttribute("appKey", getRequestString(REQ_APP_KEY));
            model.addAttribute("isSendAppInfo",getRequestString("isSendAppInfo"));
            model.addAttribute("frontType",frontType);
            model.addAttribute("appType",appType);
        }catch(Exception e){
            logger.error("支付返回异常",e);
        }
        return "/paymentmobile/paysuccess";
    }

    @RequestMapping(value = "/mobile/wechat_h5.vpage", method = RequestMethod.GET)
    public void wechatH5pay(Model model) {
        String mwebUrl = getRequestString("mwebUrl");
        getResponse().setHeader("Content-Type", "text/html;charset=UTF-8");
        try {
            getResponse().sendRedirect(mwebUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/mobile/orderquery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage orderQuery() {
        MapMessage result = new MapMessage();
        result.setSuccess(true);
        String orderId = getRequestString(REQ_ORDER_ID);
        String gatewayName = getRequestString(REQ_PAYMENT_GATEWAY);
        try{
            UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
            if(Objects.nonNull(order) && order.getPaymentStatus() == PaymentStatus.Paid){
                result.set("tradeCode","1");     //支付成功
                //增加其他返回值，产品名称, 实际价格，一个是应付价格，一个是优惠券id
                result.set("productName",order.getProductName());
                result.set("couponRefId",StringUtils.isNotBlank(order.getCouponRefId())?order.getCouponRefId():"");
                result.set("orderPrice",order.getOrderPrice());
                UserOrderPaymentHistory orderPaymentHistory = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId())
                        .stream()
                        .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                        .filter(o -> o.getOrderId().equals(order.getId()))
                        .findFirst()
                        .orElse(null);
                result.set("payAmount",Objects.nonNull(orderPaymentHistory)?orderPaymentHistory.getPayAmount():BigDecimal.ZERO);
            }else{
                result.set("tradeCode","2");       //支付失败
            }
        }catch (Exception e){
            logger.error("支付结果查询异常",e);
            result.set("tradeCode","2");
        }
        return result;
    }

    /**
     * 学贝充值H5支付，支付结果查询
     * @return
     */
    @RequestMapping(value = "/mobile/financeQuery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage financeQuery() {
        MapMessage result = new MapMessage();
        result.setSuccess(true);
        String flowId = getRequestString(REQ_ORDER_ID);
        String gatewayName = getRequestString(REQ_PAYMENT_GATEWAY);
        try {
            FinanceFlow financeFlow = financeServiceClient.getFinanceService().loadFinanceFlow(flowId).getUninterruptibly();
            if(financeFlow != null){
                String state = financeFlow.getState();
                if(Objects.equals(state,FinanceFlowState.SUCCESS.name())){
                    result.add("tradeCode","1");
                }else{
                    result.add("tradeCode","2");
                }
            }else{
                result.add("tradeCode","2");
            }
        } catch (Exception e) {
            logger.error("充值支付结果查询异常", e);
            result.add("tradeCode","2");
        }
        result.add("gatewayName",gatewayName);
        return result;
    }

    private PaymentRequest generatePaymentRequest(UserOrder order, String paymentGateway) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setTradeNumber(order.genUserOrderId());
        paymentRequest.setProductName(order.getProductName());
        paymentRequest.setPayMethod(paymentGateway);
        BigDecimal reallyPayAmount = userOrderServiceClient.getOrderCouponDiscountPrice(order);
        //考虑奖学金的情况
        if(Objects.nonNull(order.getGiveBalance())){
            reallyPayAmount = reallyPayAmount.subtract(order.getGiveBalance());
        }
        if (PaymentGateway.getUsersForPaymentTest(order.getUserId())) {
            if(reallyPayAmount.compareTo(BigDecimal.ZERO) == 0){
                paymentRequest.setPayAmount(BigDecimal.ZERO);
            }else{
                paymentRequest.setPayAmount(testAmount);
            }
        } else {
            paymentRequest.setPayAmount(reallyPayAmount);
        }
        if(PaymentConstants.PaymentGatewayName_Wechat_H5_StudentApp.equals(paymentGateway)
                ||PaymentConstants.PaymentGatewayName_Wechat_H5_ParentApp.equals(paymentGateway)){
            if(RuntimeMode.isTest()){
                paymentRequest.setSpbillCreateIp("43.227.252.50");
            }else{
                paymentRequest.setSpbillCreateIp(getWebRequestContext().getRealRemoteAddress());
            }
        }else{
            paymentRequest.setSpbillCreateIp(getWebRequestContext().getRealRemoteAddress());
        }
        paymentRequest.setCallbackBaseUrl(ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order/");
        if("Arithmetic".equals(getRequestString(REQ_APP_KEY))){
            paymentRequest.getExtParams().put("return_url",getRequestString("arithmeticUrl"));
        }else{
            paymentRequest.getExtParams().put("return_url",getRequestString(REQ_RETURN_URL));
        }
        paymentRequest.getExtParams().put("order_token",order.getOrderToken());
        paymentRequest.getExtParams().put("app_key",getRequestString(REQ_APP_KEY));
        paymentRequest.getExtParams().put("session_key", getRequestString(REQ_SESSION_KEY));
        String userAgent = getRequest().getHeader("User-Agent");
        if(userAgent.contains("iPhone")){
            userAgent = "IOS";
        }else{
            userAgent = "Android";
        }
        paymentRequest.getExtParams().put("scenceType",userAgent);
        paymentRequest.getExtParams().put("frontType",getRequestString("frontType"));
        paymentRequest.getExtParams().put("isSendAppInfo",getRequestString("isSendAppInfo"));
        paymentRequest.getExtParams().put("appType",getRequestString("appType"));
        return paymentRequest;
    }

    //////////////////////////
    ///  PAY FOR TEST ////////
    //////////////////////////

    @RequestMapping(value = "payfortest-fail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage payForTestFail(@RequestParam String oid) {
        if (RuntimeMode.gt(Mode.TEST)) return MapMessage.errorMessage("老实做人");

        String xml = generateWechatNotifyXml(oid, false);
        String notifyUrl = ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order/wechatpay_studentapp-notify.vpage";

        MapMessage message = notify(xml, notifyUrl);

        // 较验1,返回结果得是false
        if (message.isSuccess()) return MapMessage.errorMessage();

        // 较验2,检查订单状态
        UserOrder order = userOrderLoaderClient.loadUserOrder(oid);
        if (order == null || order.getPaymentStatus() != PaymentStatus.Unpaid)
            return MapMessage.errorMessage();

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "payfortest-success.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage payForTestSuccess(@RequestParam String oid) {
        String xml = generateWechatNotifyXml(oid, true);
        String notifyUrl = ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order/wechatpay_studentapp-notify.vpage";

        MapMessage message = notify(xml, notifyUrl);

        // 较验1,返回结果得是true
        if (!message.isSuccess()) return MapMessage.errorMessage();

        // 较验2,检查订单状态
        UserOrder order = userOrderLoaderClient.loadUserOrder(oid);
        if (order == null || order.getPaymentStatus() != PaymentStatus.Paid)
            return MapMessage.errorMessage();

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "payfortest-repeat.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage payForTestRepeat(@RequestParam String oid) {
        String xml = generateWechatNotifyXml(oid, true);
        String notifyUrl = ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order/wechatpay_studentapp-notify.vpage";

        // 订单应该是已经支付过了的
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(oid);
        if (userOrder == null || userOrder.getPaymentStatus() != PaymentStatus.Paid)
            return MapMessage.errorMessage();

        MapMessage message = notify(xml, notifyUrl);

        // 较验1,返回结果得是true
        if (!message.isSuccess()) return MapMessage.errorMessage();


        // 较验3,订单没有被更新
        UserOrder userOrder1 = userOrderLoaderClient.loadUserOrder(oid);
        if (!userOrder.getUpdateDatetime().equals(userOrder1.getUpdateDatetime())) return MapMessage.errorMessage();

        return MapMessage.successMessage();
    }

    private String generateWechatNotifyXml(String orderId, boolean success) {
        Map<String, Object> params = new TreeMap<>();
        params.put("appid", ProductConfig.get(WechatType.PARENT.getAppId()));
        params.put("attach", "支付测试");
        params.put("bank_type", "CFT");
        params.put("fee_type", "CNY");
        params.put("is_subscribe", "Y");
        params.put("mch_id", "1219984501");
        params.put("nonce_str", "5d2b6c2a8db53831f7eda20af46e531c");
        params.put("openid", "oUpF8uMEb4qRXf22hE3X68TekukE");
        params.put("out_trade_no", orderId);
        params.put("result_code", success ? "SUCCESS" : "FAIL");
        params.put("return_code", success ? "SUCCESS" : "FAIL");
        params.put("sub_mch_id", "1219984501");

        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddhhmmss");
        params.put("time_end", formatter.format(new Date()));
        params.put("total_fee", "1");
        params.put("trade_type", "JSAPI");
        params.put("transaction_id", "1004400740201409030005092168" + RandomUtils.nextInt(1000, 9999));

        params.put("sign", "test");

        return "<xml>"
                + "<appid>" + params.get("appid") + "</appid>"
                + "<attach>" + params.get("attach") + "</attach>"
                + "<bank_type>" + params.get("bank_type") + "</bank_type>"
                + "<fee_type>" + params.get("fee_type") + "</fee_type>"
                + "<is_subscribe>" + params.get("is_subscribe") + "</is_subscribe>"
                + "<mch_id>" + params.get("mch_id") + "</mch_id>"
                + "<nonce_str>" + params.get("nonce_str") + "</nonce_str>"
                + "<openid>" + params.get("openid") + "</openid>"
                + "<out_trade_no>" + params.get("out_trade_no") + "</out_trade_no>"
                + "<result_code>" + params.get("result_code") + "</result_code>"
                + "<return_code>" + params.get("return_code") + "</return_code>"
                + "<sign>" + params.get("sign") + "</sign>"
                + "<sub_mch_id>" + params.get("sub_mch_id") + "</sub_mch_id>"
                + "<time_end>" + params.get("time_end") + "</time_end>"
                + "<total_fee>" + params.get("total_fee") + "</total_fee>"
                + "<trade_type>" + params.get("trade_type") + "</trade_type>"
                + "<transaction_id>" + params.get("transaction_id") + "</transaction_id>"
                + "</xml>";
    }

    private MapMessage notify(String xml, String notifyUrl) {
        StringEntity entity = new StringEntity(xml, ContentType.create("application/xml", Consts.UTF_8));
        notifyUrl = notifyUrl.replace("https://", "http://");
        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).post(notifyUrl).entity(entity).execute();
        if (null != response.getResponseString()) {
            if (response.getResponseString().contains("SUCCESS")) {
                return MapMessage.successMessage();
            }
        }

        return MapMessage.errorMessage();
    }
}
