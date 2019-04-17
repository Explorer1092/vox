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

package com.voxlearning.washington.controller.finance;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.coupon.api.constants.CouponUserStatus;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowRefer;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowType;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.FinanceFlowContext;
import com.voxlearning.utopia.service.user.api.service.financial.Finance;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xin
 * @since 14-7-23 下午4:47
 */
@RequestMapping(value = "/finance/recharge")
@Controller
public class RechargeController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private FinanceServiceClient financeServiceClient;
    @Inject
    private CouponLoaderClient couponLoaderClient;

    private static final Map<String, String> SKU_DEF = new HashMap<>();
    static {
        SKU_DEF.put("200001", "6");
        SKU_DEF.put("200002", "30");
        SKU_DEF.put("200003", "68");
        SKU_DEF.put("200004", "158");
        SKU_DEF.put("200005", "258");
        SKU_DEF.put("200006", "588");
        SKU_DEF.put("200010", "1");
        SKU_DEF.put("200011", "8");
        SKU_DEF.put("200012", "138");
    }

    private static final Map<String, String> YUNKETANG_SKU_DEF = new HashMap<>();
    static {
        YUNKETANG_SKU_DEF.put("300001", "6");
        YUNKETANG_SKU_DEF.put("300002", "30");
        YUNKETANG_SKU_DEF.put("300003", "68");
        YUNKETANG_SKU_DEF.put("300004", "158");
        YUNKETANG_SKU_DEF.put("300005", "258");
        YUNKETANG_SKU_DEF.put("300006", "588");
        YUNKETANG_SKU_DEF.put("300010", "1");
        YUNKETANG_SKU_DEF.put("300011", "8");
        YUNKETANG_SKU_DEF.put("300012", "138");
    }

    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        if (null == currentUserId()) {
            return "redirect:/";
        }

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(currentStudent().getId());
        model.addAttribute("haspaypwd", StringUtils.isNotBlank(ua.getPaymentPassword()));
        Finance finance = financeServiceClient.getFinanceService()
                .loadUserFinance(currentUserId())
                .getUninterruptibly();
        model.addAttribute("balance", finance == null ? 0 : finance.getBalance());
        List<FinanceFlow> flows = financeServiceClient.getFinanceService()
                .findUserFinanceFlows(currentUserId())
                .getUninterruptibly()
                .stream()
                .sorted((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime()))
                .collect(Collectors.toList());
        model.addAttribute("flows", flows);
        return "studentv3/finance/index";
    }

    // 获取我的作业币余额
    @RequestMapping(value = "/loadfinance.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadFinance() {
        User user = currentUser();
        if (null == user) {
            return MapMessage.errorMessage("用户不存在");
        }
        MapMessage result = MapMessage.successMessage();
        Long sid = getRequestLong("studentId");
        StudentDetail sd = studentLoaderClient.loadStudentDetail(sid);
        //IPA学贝充值标志
        boolean IOSFinanceRecharge = true;

        if (sd != null) {
            result.add("financeShow", true);
            IOSFinanceRecharge = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(sd, "Order", "FinanceRecharge");
        }

        Finance finance = financeServiceClient.getFinanceService()
                .loadUserFinance(user.getId())
                .getUninterruptibly();

        String paymentResultUrl = getGalaxySiteUrl() + "/api/v3/finance/recharge/result.vpage";
        return result.add("balance", finance == null ? 0 : finance.getBalance())
                .add("payment_result_url", paymentResultUrl)
                .add("IOSFinanceRecharge",IOSFinanceRecharge);
    }

    // 获取充值支付方式列表
    @RequestMapping(value = "/loadFinancePayWayList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadFinancePayWayList() {
        String appVer = getRequestString("app_version");
        String appType = getRequestString("appType");
        List<Map<String,Object>> payWayList = new LinkedList<>();
        if(isIOSRequest(getRequest())){
            if((Objects.equals("17parent",appType) && VersionUtil.compareVersion(appVer, "2.6.2") < 0)
                    || (Objects.equals("17yunketang",appType) && VersionUtil.compareVersion(appVer, "1.6.1") < 0) ){
                Map<String,Object> wechatPay = new LinkedHashMap<>();
                wechatPay.put("way","微信支付");
                wechatPay.put("className","wx");
                wechatPay.put("payment","wechatpay_h5_parentapp");
                wechatPay.put("type",1);
                payWayList.add(wechatPay);

                Map<String,Object> aliPay = new LinkedHashMap<>();
                aliPay.put("way","支付宝支付");
                aliPay.put("className","zfb");
                aliPay.put("payment","alipay_wap_parentapp");
                aliPay.put("type",2);
                payWayList.add(aliPay);
            }
        }else{
            Map<String,Object> wechatPay = new LinkedHashMap<>();
            wechatPay.put("way","微信支付");
            wechatPay.put("className","wx");
            wechatPay.put("payment","wechatpay_parent");
            wechatPay.put("type",1);
            payWayList.add(wechatPay);

            Map<String,Object> aliPay = new LinkedHashMap<>();
            aliPay.put("way","支付宝支付");
            aliPay.put("className","zfb");
            aliPay.put("payment","alipay_parentapp");
            aliPay.put("type",2);
            payWayList.add(aliPay);
        }
        MapMessage result = MapMessage.successMessage();
        return result.add("payWayList",payWayList);
    }


    private String getGalaxySiteUrl() {
        String domain = ProductConfig.get("galaxy.domain");
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "https://" + domain;
        }
        return domain;
    }

    // 获取我的作业币流水
    @RequestMapping(value = "/loadfinanceflow.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadFinanceFlow() {
        User user = currentUser();
        if (null == user) {
            return MapMessage.errorMessage("用户不存在");
        }
        List<FinanceFlow> flowList = financeServiceClient.getFinanceService()
                .findUserFinanceFlows(user.getId())
                .getUninterruptibly()
                .stream()
                .sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime()))
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("flowList", flowList);
    }

    // 执行充值前 获取流水ID
    @RequestMapping(value = "/prerecharge.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage preRecharge() {
        User user = currentUser();
        if (null == user) {
            return MapMessage.errorMessage("用户不存在");
        }
        // 充值金额
        String payAmount = getRequestString("payAmount");
        // 支付方式
        String payType = getRequestString("payType");
        // 交易方式 区分H5和原生
        String tradeType = getRequestString("tradeType");
        if (StringUtils.isBlank(payAmount) || StringUtils.isBlank(payType)) {
            return MapMessage.errorMessage("参数错误");
        }
        // payType 定义的传给客户端的是 1 微信  2 支付宝
        String payMethod = "";
        Map<String, String> attrs = new HashMap<>();
        if ("1".equals(payType)) {
            if("h5".equals(tradeType)){
                payMethod = PaymentConstants.PaymentGatewayName_Wechat_H5_ParentApp;
            }else{
                payMethod = PaymentConstants.PaymentGatewayName_Wechat_ParentApp;
            }
        }
        if ("2".equals(payType)) {
            if("h5".equals(tradeType)){
                payMethod = PaymentConstants.PaymentGatewayName_Alipay_Wap_ParentApp;
            }else{
                payMethod = PaymentConstants.PaymentGatewayname_Alipay_ParentApp;
            }
        }
        if ("4".equals(payType)) {
            payMethod = PaymentConstants.PaymentGatewayName_ApplePay_ParentApp;
            String skuId = getRequestString("skuId");
            if (StringUtils.isBlank(skuId)) {
                return MapMessage.errorMessage("请求参数错误");
            }
            attrs.put("skuId", skuId);
            String appType = getRequestString("appType");
            if(Objects.equals("17yunketang",appType)){
                if (!Objects.equals(YUNKETANG_SKU_DEF.get(skuId), payAmount)) {
                    return MapMessage.errorMessage("请求参数错误");
                }
            }else{
                if (!Objects.equals(SKU_DEF.get(skuId), payAmount)) {
                    return MapMessage.errorMessage("请求参数错误");
                }
            }
        }

        if (StringUtils.isBlank(payMethod)) {
            return MapMessage.errorMessage("未知的支付方式");
        }

        // 先生成一条流水
        FinanceFlowContext context = FinanceFlowContext.instance()
                .userId(currentUserId())
                .payAmount(new BigDecimal(payAmount))
                .amount(new BigDecimal(0)) //这里先设置成0,充值成功后会更新
                .type(FinanceFlowType.Deposit)
                .state(FinanceFlowState.WAITING)
                .payMethod(payMethod)
                .refer(FinanceFlowRefer.Deposit)
                .attribute(JsonUtils.toJson(attrs))
                .memo("学贝充值");
        String flowId = financeServiceClient.getFinanceService().precharge(context).getUninterruptibly();
        if (flowId == null) {
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage().add("flowId", flowId);
    }

    // 学生app支付页，获取家长的作业币余额
    @RequestMapping(value = "/loadparentfinance.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadParentFinance() {
        // 获取学生ID
        Long studentId = getRequestLong("studentId");
        User user = raikouSystem.loadUser(studentId);
        if (null == user) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (user.fetchUserType() != UserType.STUDENT) {
            return MapMessage.errorMessage("用户类型错误");
        }
        MapMessage result = MapMessage.successMessage();
        result.add("financeShow", true);
        // 获取绑定的家长
        List<StudentParent> studentParentList = parentLoaderClient.loadStudentParents(user.getId());
        List<Map<String, Object>> financeList = new ArrayList<>();
        for (StudentParent parent : studentParentList) {
            Finance finance = financeServiceClient.getFinanceService()
                    .createUserFinanceIfAbsent(parent.getParentUser().getId())
                    .getUninterruptibly();
            if (finance != null) {
                Map<String, Object> financeMap = new MapMessage();
                financeMap.put("userId", parent.getParentUser().getId());
                financeMap.put("callName", parent.getCallName());
                financeMap.put("finance", finance.getBalance().doubleValue());
                financeList.add(financeMap);
            }
        }
        // 加入自己的账户
        Finance finance = financeServiceClient.getFinanceService()
                .createUserFinanceIfAbsent(studentId)
                .getUninterruptibly();
        Map<String, Object> financeMap = new MapMessage();
        financeMap.put("userId", studentId);
        financeMap.put("callName", "我的");
        financeMap.put("finance", finance.getBalance().doubleValue());
        financeList.add(financeMap);
        result.add("financeList", financeList);
        return result;
    }

    // 作业币支付
    @RequestMapping(value = "financepay.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage financePay() {
        User currentUser = currentUser();
        if (null == currentUser) {
            return MapMessage.errorMessage("登录后才能支付");
        }
        String orderId = getRequestString("orderId"); // 订单ID
        Long financeUserId = getRequestLong("userId");       // 消耗的作业币账户ID
        if (Objects.equals(financeUserId, 0L)) {
            financeUserId = currentUser.getId();
        }

        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        if (order == null) {
            LoggerUtils.error("orderIsNull", null,orderId);
            return MapMessage.errorMessage("订单不存在");
        }
        // 判断重复支付的问题
        if (!order.canBePaid()) {
            return MapMessage.errorMessage("请不要重复支付");
        }

        if(StringUtils.isNotBlank(order.getCouponRefId())){
            CouponUserRef couponUserRef = couponLoaderClient.loadCouponUserRefById(order.getCouponRefId());
            if(couponUserRef != null && couponUserRef.getStatus() == CouponUserStatus.Used){
                return MapMessage.errorMessage("优惠券已被使用");
            }
        }

        Finance finance = financeServiceClient.getFinanceService()
                .createUserFinanceIfAbsent(financeUserId)
                .getUninterruptibly();
        if (finance == null) {
            return MapMessage.errorMessage("账户不存在");
        }
        // 判断账户权限
        User user = raikouSystem.loadUser(financeUserId);
        if (!Objects.equals(currentUser.getId(), user.getId())) {
            if (currentUser.fetchUserType() == UserType.STUDENT) {
                // 获取学生家长
                List<StudentParentRef> refList = studentLoaderClient.loadStudentParentRefs(currentUser.getId());
                if (CollectionUtils.isEmpty(refList)) {
                    return MapMessage.errorMessage("账户权限错误");
                }
                List<Long> parentIds = refList.stream().map(StudentParentRef::getParentId).collect(Collectors.toList());
                if (!parentIds.contains(financeUserId)) {
                    return MapMessage.errorMessage("账户权限错误");
                }
            } else {
                // 获取家长孩子
                List<User> refList = studentLoaderClient.loadParentStudents(currentUser.getId());
                if (CollectionUtils.isEmpty(refList)) {
                    return MapMessage.errorMessage("账户权限错误");
                }
                List<Long> child = refList.stream().map(User::getId).collect(Collectors.toList());
                if (!child.contains(financeUserId)) {
                    return MapMessage.errorMessage("账户权限错误");
                }
            }

        }
        BigDecimal price = userOrderServiceClient.getOrderCouponDiscountPrice(order);
        //是否包含奖学金，如果包含，扣除奖学金，得到最终的
        if(Objects.nonNull(order.getGiveBalance()) && order.getGiveBalance().compareTo(BigDecimal.ZERO) > 0){
            price = price.subtract(order.getGiveBalance());
        }
        try {
            String memo = (StringUtils.isBlank(order.getUserName()) ? "" : order.getUserName()) + "购买" + order.getProductName();
            String userOrderId = order.genUserOrderId();
            BigDecimal finalPrice = price;
            MapMessage message = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .expirationInSeconds(30)
                    .keyPrefix("Finance:debit")
                    .keys(financeUserId)
                    .callback(() -> debitFinance(finalPrice, finance, memo, userOrderId, PaymentConstants.PaymentGatewayName_17Zuoye))
                    .build()
                    .execute();

            if (message.isSuccess() || price.compareTo(BigDecimal.ZERO) == 0) {
                PaymentCallbackContext context = new PaymentCallbackContext(PaymentConstants.PaymentGatewayName_17Zuoye, "");
                context.setTradeNumber(orderId);
                PaymentVerifiedData paymentVerifiedData = new PaymentVerifiedData();
                paymentVerifiedData.setTradeNumber(orderId);
                paymentVerifiedData.setPayAmount(price);
                context.setVerifiedPaymentData(paymentVerifiedData);
                context.setFinanceUserId(financeUserId);
                order = businessUserOrderServiceClient.processUserOrderPayment(context);
                if (null != order) {
                    return MapMessage.successMessage("支付成功");
                } else {
                    return MapMessage.successMessage("支付失败");
                }
            } else {
                return message;
            }
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("对不起，您点击太快了");
        } catch (Exception ex) {
            logger.error("Failed to pay finance order (user={},order={},financeUserId={})",
                    user.getId(), orderId, financeUserId, ex);
            return MapMessage.errorMessage("支付失败");
        }
    }

    //获得奖学金,
    @RequestMapping(value = "/loadParentGiveBalance.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadParentGiveBalance() {
        MapMessage result = MapMessage.successMessage();
        User parent = currentParent();
        if (null == parent) {
            return MapMessage.errorMessage("登录后才能支付");
        }
        String orderId = getRequestString("orderId");
        UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
        if (order != null && order.getOrderStatus() == OrderStatus.Canceled) {
            LoggerUtils.error("orderIsCanceled", null,orderId);
        }
        if(order == null){
            LoggerUtils.error("orderIsNull", null,orderId);
            return MapMessage.errorMessage("无效的订单，请重新创建订单");
        }
        String couponRefId = getRequestString("couponRefId");
        if(StringUtils.isNotBlank(couponRefId)){
            order.setCouponRefId(couponRefId);
        }
        //是否已经选择了使用奖学金了
        if(Objects.nonNull(order.getGiveBalance()) && order.getGiveBalance().compareTo(BigDecimal.ZERO) > 0){
            result.add("selected",true);
        }else{
            result.add("selected",false);
        }
        //实际支付金额
        BigDecimal reallyPayAmount = userOrderServiceClient.getOrderCouponDiscountPrice(order);

        //奖学金
        BigDecimal giveBalance = financeServiceClient.getFinanceService()
                .loadUserGiveBalance(parent.getId());
        //奖学金总金额
        result.add("totalGiveBalance",giveBalance);
        //实际支付金额大于5元才能使用奖学金
        if(reallyPayAmount.compareTo(new BigDecimal(5)) >= 0){
            result.add("supportDeduction",true);
            //奖学金是否大于5元
            if(giveBalance.compareTo(new BigDecimal(5)) >= 0){
                //抵扣金额
                if(reallyPayAmount.compareTo(giveBalance) > 0){
                    result.add("deductionAmount",giveBalance.setScale(0, BigDecimal.ROUND_DOWN));
                }else{
                    result.add("deductionAmount",reallyPayAmount.setScale(0,BigDecimal.ROUND_DOWN));
                }
                result.add("enoughBalance",true);

            }else{
                //奖学金余额不足
                result.add("enoughBalance",false);
            }
        }else{
            result.add("supportDeduction",false);
        }
        return result;
    }

    // 支付之前绑定奖学金
    @RequestMapping(value = "relatedGiveBalance.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage relatedCouponOrder() {
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        String orderId = getRequestString("orderId");
        Integer giveBalance = getRequestInt("giveBalance");
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        if (userOrder == null) {
            return MapMessage.errorMessage("无效的订单，请重新创建订单");
        }
        if (giveBalance <= 0) {
            return MapMessage.errorMessage("奖学金错误");
        }
        // 如果是已经关联的 直接返回成功
        if (Objects.nonNull(userOrder.getGiveBalance()) && userOrder.getGiveBalance().compareTo(new BigDecimal(0)) > 0) {
            return MapMessage.successMessage();
        }
        //设置之前校验一下余额是否充足
        BigDecimal userGiveBalance = financeServiceClient.getFinanceService().loadUserGiveBalance(user.getId());

        if(userGiveBalance.compareTo(new BigDecimal(giveBalance)) < 0){
            return MapMessage.errorMessage("奖学金余额不足");
        }
        MapMessage mapMessage = userOrderServiceClient.getUserOrderService().relatedGiveBalance(userOrder, user.getId(), BigDecimal.valueOf(giveBalance));
        return mapMessage;
    }

    // 用户消费学贝
    public MapMessage debitFinance(BigDecimal payAmount, Finance finance, String memo, String orderId, String payMethod) {
        if (payAmount == null || finance == null) {
            return MapMessage.errorMessage("支付失败");
        }
        // 校验价格
        if (payAmount.compareTo(finance.getBalance()) > 0) {
            return MapMessage.errorMessage("余额不足");
        }
        FinanceFlowContext ctx = FinanceFlowContext.instance()
                // 判断是否是使用的家长作业币账户购买
                .userId(finance.getUserId())
                .amount(payAmount)
                .payAmount(payAmount)
                .state(FinanceFlowState.SUCCESS)
                .type(FinanceFlowType.Debit)
                .orderId(orderId) // 这里存带下划线的ID
                .memo(memo)
                .refer(FinanceFlowRefer.UserOrder)
                .payMethod(payMethod);

        boolean result = financeServiceClient.getFinanceService().debit(ctx).getUninterruptibly();
        if (result) {
            return MapMessage.successMessage("支付成功");
        } else {
            return MapMessage.errorMessage("支付失败，请检查余额是否充足");
        }
    }
}

