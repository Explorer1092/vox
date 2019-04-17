package com.voxlearning.washington.controller.open.v2.order;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.payment.PaymentRequestForm;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.service.financial.Finance;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

import static com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil.testAmount;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Created by Summer on 2016/12/14.
 * 第三方支付确认
 */
@Controller
@RequestMapping(value = "/apps/order/v2")
@Slf4j
public class AppOrderV2Controller extends AbstractApiController {

    @Inject
    private FinanceServiceClient financeServiceClient;

    /**
     * 移动端订单支付---进入支付确认页
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
            User curUser = getApiRequestUser();
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(curUser.getId());
            if (studentDetail != null && studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz()) {
                model.addAttribute("error", "本产品不提供毕业班购买");
                return "/paymentmobile/confirm";
            }

            //以下为confirm页面必选参数
            model.addAttribute("orderId", getRequestString(REQ_ORDER_ID));
            model.addAttribute("showOrderId", order.getId());// 展示的orderId
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

            boolean IOSFinanceRecharge = true;
            if (studentDetail != null) {
                IOSFinanceRecharge = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "Order", "FinanceRecharge");
            }
            model.addAttribute("IOSFinanceRecharge",IOSFinanceRecharge);

            //H5支付灰度变量
            boolean isOpenH5payment = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "Order", "H5Pay");
            model.addAttribute("isOpenH5payment",isOpenH5payment);

            //学生端学贝支付灰度测试
            String userAgent = getRequest().getHeader("User-Agent").toLowerCase();
            boolean isFinancePayment = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "Order", "FinancePay");
            if(isFinancePayment){
                List<CouponShowMapper> mappers = userOrderLoaderClient.loadOrderUsableCoupons(order, curUser.getId());
                model.addAttribute("isFinancePayment",userAgent.contains("17student"));
                //查询学贝够不够支付
                List<StudentParent> studentParentList = parentLoaderClient.loadStudentParents(curUser.getId());
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
                Finance stuFinance = financeServiceClient.getFinanceService().createUserFinanceIfAbsent(curUser.getId())
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

            if (MapUtils.isNotEmpty(JsonUtils.fromJson(order.getProductAttributes()))) {
                model.addAttribute("productAttributes", JsonUtils.fromJson(order.getProductAttributes()));
            }

            // 学生是否开启支付权限
            StudentExtAttribute attribute = studentLoaderClient.loadStudentExtAttribute(curUser.getId());
            if ((currentUser() != null && currentUser().fetchUserType() == UserType.PARENT) || attribute == null || attribute.fetchPayFreeStatus()) {
                return "/paymentmobile/confirm";
            } else {
                // 获取家长列表
                List<StudentParent> parents = parentLoaderClient.loadStudentParents(curUser.getId());
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
            String fixId = getRequestString(REQ_ORDER_ID);
            UserOrder order = userOrderLoaderClient.loadUserOrder(fixId);
            if (null == order || !order.getOrderToken().equals(getRequestString(RES_ORDER_TOKEN))
                    || !order.getUserId().equals(user.getId())) {
                result.add(RES_RESULT, false);
                result.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
                return result;
            }

            if (order.getPaymentStatus() == PaymentStatus.Paid) {
                result.add(RES_RESULT, false);
                result.add(RES_MESSAGE, RES_RESULT_ORDER_HAD_PAID);
                return result;
            }

            String productId = order.getProductId();
            OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
            if (product != null) {
                MapMessage checkMsg = userOrderServiceClient.checkDaysToExpireForCreateNewOrder(order.getUserId(),
                        OrderProductServiceType.safeParse(order.getOrderProductServiceType()));
                if (!checkMsg.isSuccess()) {
                    result.add(RES_RESULT, false);
                    result.add(RES_MESSAGE, "您已经开通了 " + order.getProductName() + "，可以直接学习使用哦！");
                    return result;
                }
            }
            //移动端的支付,目前只有微信,不同的APP微信商户号不同,这里需要区分一下
            String gatewayName = getRequestString(REQ_PAYMENT_GATEWAY);  //gatewayName的值取自PaymentConstants
            PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(gatewayName);

            if (null != paymentGateway) {
                PaymentRequestForm paymentRequestForm = paymentGateway.getPaymentRequestForm(generatePaymentRequest(order, gatewayName));
                result.add(RES_RESULT, true);
                result.add("payParams", paymentRequestForm.getFormFields());
                return result;
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

    @RequestMapping(value = "/mobile/confirmH5.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mobileConfirmH5() {
        MapMessage result = new MapMessage();
        try {
            validateRequired(REQ_ORDER_ID);
            validateRequired(REQ_PAYMENT_GATEWAY);

            User user = getApiRequestUser();
            String fixId = getRequestString(REQ_ORDER_ID);
            UserOrder order = userOrderLoaderClient.loadUserOrder(fixId);
            if (null == order || !order.getOrderToken().equals(getRequestString(RES_ORDER_TOKEN))
                    || !order.getUserId().equals(user.getId())) {
                result.add(RES_RESULT, false);
                result.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
                return result;
            }

            if (order.getPaymentStatus() == PaymentStatus.Paid) {
                result.add(RES_RESULT, false);
                result.add(RES_MESSAGE, RES_RESULT_ORDER_HAD_PAID);
                return result;
            }

            String productId = order.getProductId();
            OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
            if (product != null) {
                MapMessage checkMsg = userOrderServiceClient.checkDaysToExpireForCreateNewOrder(order.getUserId(),
                        OrderProductServiceType.safeParse(order.getOrderProductServiceType()));
                if (!checkMsg.isSuccess()) {
                    result.add(RES_RESULT, false);
                    result.add(RES_MESSAGE, "您已经开通了 " + order.getProductName() + "，可以直接学习使用哦！");
                    return result;
                }
            }
            //移动端的支付,目前只有微信,不同的APP微信商户号不同,这里需要区分一下
            String gatewayName = getRequestString(REQ_PAYMENT_GATEWAY);  //gatewayName的值取自PaymentConstants
            PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(gatewayName);

            if (null != paymentGateway) {
                PaymentRequestForm paymentRequestForm = paymentGateway.getPaymentRequestForm(generatePaymentRequest(order, gatewayName));
                result.add(RES_RESULT, true);
                result.add("payParams", paymentRequestForm.getFormFields());
                result.add("tradeType",paymentRequestForm.getFormFields().get("tradeType"));
                result.add("mwebUrl",paymentRequestForm.getFormFields().get("mwebUrl"));
                return result;
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

    private PaymentRequest generatePaymentRequest(UserOrder order, String paymentGateway) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setTradeNumber(order.genUserOrderId());
        paymentRequest.setProductName(order.getProductName());
        paymentRequest.setPayMethod(paymentGateway);
        if (PaymentGateway.getUsersForPaymentTest(order.getUserId())) {
            paymentRequest.setPayAmount(testAmount);
        } else {
            paymentRequest.setPayAmount(userOrderServiceClient.getOrderCouponDiscountPrice(order));
        }
        paymentRequest.setSpbillCreateIp(getWebRequestContext().getRealRemoteAddress());
        paymentRequest.setCallbackBaseUrl(ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order/");
        return paymentRequest;
    }
}
