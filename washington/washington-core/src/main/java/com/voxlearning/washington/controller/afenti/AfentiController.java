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

package com.voxlearning.washington.controller.afenti;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.Password;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.payment.WechatAbstractPaymentGateway;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.afenti.api.AfentiSocialService;
import com.voxlearning.utopia.service.business.consumer.BusinessUserOrderServiceClient;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowRefer;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.FinanceFlowContext;
import com.voxlearning.utopia.service.user.api.service.financial.Finance;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil.testAmount;

@Controller
@RequestMapping("/apps/afenti")
//注意！这个controller不要在外面做权限控制，因为涉及到多浏览器不同登录用户的情况下，生成订单付款的问题。具体参见 confirm.vpage 的逻辑
public class AfentiController extends AfentiBaseController {

    // 目前线上支持购买的产品列表
    private static final Map<String, String> PRODUCT_PURCHASE_URL = new HashMap<>();

    static {
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.AfentiExam.name(), "exam-cart.vpage");             // 阿分题
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.TravelAmerica.name(), "travel-cart.vpage");        // 走遍美国
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.SanguoDmz.name(), "sanguodmz-cart.vpage");         // 进击的三国
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.Walker.name(), "walker-cart.vpage");               // 沃克大冒险合集
        PRODUCT_PURCHASE_URL.put(A17ZYSPG.name(), "spg-cart.vpage");                // 诺亚传说
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.PetsWar.name(), "petswar-cart.vpage");             // 宠物大乱斗
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.WalkerElf.name(), "walkerelf-cart.vpage");         // 拯救精灵王
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.Stem101.name(), "stem-cart.vpage");                // 趣味数学
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.WukongShizi.name(), "wukongshizi-cart.vpage");     // 悟空识字
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.WukongPinyin.name(), "wukongpinyin-cart.vpage");    // 悟空拼音
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.AfentiMath.name(), "afentimath-cart.vpage");       // 阿分题数学
        PRODUCT_PURCHASE_URL.put(OrderProductServiceType.AfentiChinese.name(), "afentichinese-cart.vpage"); // 阿分题语文
    }

    // 根据cart获取OrderProductServiceType
    private static final Map<String, OrderProductServiceType> PRODUCT_MAP = new HashMap<>();

    static {
        PRODUCT_MAP.put("exam", AfentiExam);
        PRODUCT_MAP.put("travel", TravelAmerica);
        PRODUCT_MAP.put("sanguodmz", SanguoDmz);
        PRODUCT_MAP.put("walker", Walker);
        PRODUCT_MAP.put("spg", A17ZYSPG);
        PRODUCT_MAP.put("petswar", PetsWar);
        PRODUCT_MAP.put("walkerelf", WalkerElf);
        PRODUCT_MAP.put("stem", Stem101);
        PRODUCT_MAP.put("wukongshizi", WukongShizi);
        PRODUCT_MAP.put("wukongpinyin", WukongPinyin);
        PRODUCT_MAP.put("afentimath", AfentiMath);
        PRODUCT_MAP.put("afentichinese", AfentiChinese);
    }


    @ImportService(interfaceClass = AfentiSocialService.class)
    private AfentiSocialService afentiSocialService;

    @Inject private BusinessUserOrderServiceClient businessUserOrderServiceClient;
    @Inject private FinanceServiceClient financeServiceClient;

    @RequestMapping(value = "/products.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage products() {
        //这个controller没有权限控制,在这里做下检查
        User student = currentStudent();
        if (null == student) return MapMessage.errorMessage("请使用学生帐号登录");

        try {
            StudentDetail studentDetail = currentStudentDetail();
            List<OrderProduct> productInfos = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(studentDetail);
            return MapMessage.successMessage().add("products", productInfos);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("查询产品信息失败");
        }
    }

    /**
     * 阿分题专题
     */
    @RequestMapping(value = "exam.vpage", method = RequestMethod.GET)
    public String exam(Model model) {
        return "redirect:/";

//        if (currentUserId() == null || !getWebRequestContext().isCurrentUserStudent())
//            return "redirect:/";
//
//        Integer level = currentStudentDetail().getClazzLevelAsInteger();
//
//        if (level == null) {
//            return "redirect:/";
//        } else {
//            model.addAttribute("level", level);
//            return "/apps/afenti/exam";
//        }
    }

    /**
     * 走遍美国专题
     */
    @RequestMapping(value = "travel.vpage", method = RequestMethod.GET)
    public String travel() {
        return "redirect:/";
        //return "/apps/afenti/travel";
    }

    // 学生pc端课外乐园，点击产品进入产品列表页面
    @RequestMapping(value = "order/{productType}-cart.vpage", method = RequestMethod.GET)
    public String cart(@PathVariable("productType") String productType, Model model,
                       @RequestParam(value = "vip", required = false, defaultValue = "0") int vip) {

        return "redirect:/";
//        if (currentUserId() == null || !getWebRequestContext().isCurrentUserStudent())
//            return "redirect:/";
//
//        StudentDetail student = currentStudentDetail();
//        if (student == null)
//            return "redirect:/";
//
//        if (StringUtils.isBlank(productType) || !PRODUCT_MAP.containsKey(productType.toLowerCase())) {
//            model.addAttribute("message", "小朋友，你选择的应用不存在！");
//            return "common/message";
//        }
//
//        OrderProductServiceType orderProductServiceType = PRODUCT_MAP.get(productType.toLowerCase());
//        // 关闭付费的产品直接去下线公告页
//        if (orderProductServiceType.isOrderClosed()) {
//            model.addAttribute("type", orderProductServiceType.name());
//            return "/apps/afenti/order/offlineinfo";
//        }
//
//        // 通过年级获取可以购买的产品
//        Integer classLevel = student.getClazzLevelAsInteger();
//        if (student.getClazz() != null && student.getClazz().isTerminalClazz()) {
//            model.addAttribute("error", "本产品不提供毕业班购买");
//            return "common/message";
//        }
//        switch (productType) {
//            case "exam":
//                // 黑名单规则
//                if (student.isInPaymentBlackListRegion()) {
//                    AppPayMapper mapper = userOrderLoaderClient.getUserAppPaidStatus(AfentiExam.name(), student.getId());
//                    if (mapper == null || mapper.unpaid()) {
//                        model.addAttribute("message", "您所在区域暂时不能使用 阿分题 服务，敬请期待！");
//                        return "common/message";
//                    }
//                }
//
//                // pk大礼包弹窗
//                model.addAttribute("pkGiftPacks", false);
//                break;
//
//            case "walkerelf":
//                AppPayMapper walkerelfMapper = userOrderLoaderClient.getUserAppPaidStatus(orderProductServiceType.name(), currentUserId(), true);
//                if (walkerelfMapper != null && CollectionUtils.isNotEmpty(walkerelfMapper.getValidProducts())) {
//                    model.addAttribute("validItems", StringUtils.join(walkerelfMapper.getValidProducts(), ","));
//                }
//                break;
//
//            case "stem":
//                AppPayMapper stemMapper = userOrderLoaderClient.getUserAppPaidStatus(orderProductServiceType.name(), currentUserId(), true);
//                if (stemMapper != null && CollectionUtils.isNotEmpty(stemMapper.getValidProducts())) {
//                    model.addAttribute("validItems", StringUtils.join(stemMapper.getValidProducts(), ","));
//                }
//                break;
//        }
//
//        List<OrderProduct> availableProductList = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(student);
//        availableProductList = availableProductList.stream()
//                .filter(o -> OrderProductServiceType.safeParse(o.getProductType()) == orderProductServiceType)
//                .collect(Collectors.toList());
//
//        model.addAttribute("productServiceType", orderProductServiceType);
//        model.addAttribute("productType", productType);
//        model.addAttribute("level", classLevel);
//        model.addAttribute("availableProducts", availableProductList);
//        model.addAttribute("availableProductsJson", JsonUtils.toJson(availableProductList));
//        model.addAttribute("vip", vip);
//
//        // 跳转到订单页面的来源， 传给页面， 在生成订单时存到 ORDER_REFERER 字段
//        String refer = getRequestParameter("refer", "");
//        if (StringUtils.isNotBlank(refer))
//            model.addAttribute("refer", refer.trim());
//
//        // 购买不同周期的阿分题（30天 90天）
//        String afentiCycle = getRequestParameter("afentiCycle", "");
//        if (StringUtils.isNotBlank(afentiCycle)) {
//            model.addAttribute("afentiCycle", afentiCycle.trim());
//        }
//
//        //该用户在该产品内的VIP有效期等于大于365天，则提示不可购买
//        MapMessage checkMsg = userOrderServiceClient.checkDaysToExpireForCreateNewOrder(currentUserId(), orderProductServiceType);
//        if (!checkMsg.isSuccess()) {
//            model.addAttribute("dayToExpireBiggerThan365", true);
//            model.addAttribute("dayToExpireErrorInfo", "您已经开通了" + availableProductList.get(0).getName() + "，可以直接学习使用哦！");
//        } else {
//            model.addAttribute("dayToExpireBiggerThan365", false);
//        }
//
//        return "/apps/afenti/order/product-cart";
    }


    @RequestMapping(value = "order/{productType}-cart.vpage", method = RequestMethod.POST)
    public String cartPost(@PathVariable("productType") String productType, Model model) {
        return "redirect:/";

//        if (currentUserId() == null || !getWebRequestContext().isCurrentUserStudent())
//            return "redirect:/";
//        StudentDetail student = currentStudentDetail();
//        if (student == null)
//            return "redirect:/";
//
//        String productId = getRequestString("productId");
//        if (StringUtils.isBlank(productId)) {
//            logger.error("productId not found in request params, productType:{}", productType);
//            return "redirect:/apps/afenti/order/" + productType + "-cart.vpage";
//        }
//
//        // 这里要查灰度价格
//        List<OrderProduct> productList = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(student);
//        OrderProduct product = productList.stream().filter(p -> Objects.equals(p.getId(), productId))
//                .findFirst().orElse(null);
//        if (product == null)
//            return "redirect:/apps/afenti/order/" + productType + "-cart.vpage";
//
//        // 校验用户支付
//        MapMessage validateResult = validateStudentPay(student.getId(), product);
//        if (!validateResult.isSuccess()) {
//            model.addAttribute("message", validateResult.getInfo());
//            return "common/message";
//        }
//
//        // new order
//        UserOrder order = UserOrder.newOrder(OrderType.app, student.getId());
//        order.setProductId(product.getId());
//        order.setProductName(product.getName());
//
//        order.setOrderProductServiceType(product.getProductType());
//        order.setProductAttributes(product.getAttributes());
//        order.setOrderPrice(product.getPrice());
//
//        order.setUserId(student.getId());
//        order.setUserName(student.getProfile().getRealname());
//        String refer = getRequestParameter("refer", "");
//        refer = StringUtils.isBlank(refer) ? STUDENT_PC_FAIRYLAND_DEFAULT.type : refer.trim();
//        order.setOrderReferer(refer);
//        MapMessage message = userOrderServiceClient.saveUserOrder(order);
//        if (!message.isSuccess()) {
//            model.addAttribute("message", StringUtils.defaultIfBlank(message.getInfo(), "下单失败，请重试"));
//            return "common/message";
//        }
//        //订单生成后，进入确认付款页面
//        model.addAttribute("orderId", order.genUserOrderId());
//        return "redirect:/apps/afenti/order/confirm.vpage";
    }

    @RequestMapping(value = "order/confirm.vpage", method = RequestMethod.GET)
    public String confirm(@RequestParam(defaultValue = "") String orderId,
                          @RequestParam(defaultValue = "") String payment, Model model) {
        return "redirect:/";
//        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
//        // 订单号不正确，或者订单不能被付款，则跳入订单列表页面
//        if (order == null) {
//            return "redirect:/apps/afenti/order/basic-cart.vpage";
//        }
//
//        // 洛亚传说 三国 关付费 挂公告
//        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == A17ZYSPG || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == SanguoDmz ||
//                OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == TravelAmerica || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == PetsWar) {
//            model.addAttribute("type", order.getOrderProductServiceType());
//            return "/apps/afenti/order/offlineinfo";
//        }
//
//        //用户在此产品下的vip时长>=365天时。不能再支付此订单。直接跳转到用户的订单列表页面
//        MapMessage checkMsg = userOrderServiceClient.checkDaysToExpireForCreateNewOrder(order.getUserId(),
//                OrderProductServiceType.safeParse(order.getOrderProductServiceType()));
//        if (!checkMsg.isSuccess()) {
//            model.addAttribute("dayToExpireBiggerThan365", true);
//            model.addAttribute("dayToExpireErrorInfo", "您已经开通了" + order.getProductName() + "，可以直接学习使用哦！");
//        } else {
//            model.addAttribute("dayToExpireBiggerThan365", false);
//        }
//
//        // 拯救精灵王和趣味数学不允许重复购买
//        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == WalkerElf || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == Stem101) {
//            List<UserOrder> orders = userOrderLoaderClient.loadUserPaidOrders(order.getOrderProductServiceType(), currentUserId());
//            for (UserOrder oldOrder : orders) {
//                if (oldOrder.getProductId().equals(order.getProductId())) {
//                    String purchaseUrl = PRODUCT_PURCHASE_URL.get(order.getOrderProductServiceType());
//                    if (StringUtils.isNoneBlank(purchaseUrl)) {
//                        return "redirect:/apps/afenti/order/" + purchaseUrl;
//                    } else {
//                        return "redirect:/";
//                    }
//                }
//            }
//        }
//
//        // 被取消的订单也允许付款
//        if (!order.canBePaidOrCanceled()) {
//            return "redirect:/student/center/order.vpage";
//        }
//
//        model.addAttribute("orderId", orderId);
//        model.addAttribute("payment", payment);
//        model.addAttribute("captchaToken", RandomUtils.randomString(24));
//
//        if (!order.getUserId().equals(currentUserId())) {
//            //FIXME: 用户当前登录的id和要付款的id不一样，暂时还是允许付款。最好是顺便把用户当前登录状态也退了
//            getWebRequestContext().cleanupAuthenticationStates();
//
//            //FIXME: 用户当前登录的id和要付款的id不一样的情况下，直接展示其他用户的订单，其实不太安全（信息泄漏）。暂时没有这方面隐患，先允许这种行为。
//            //不要在页面中展示泄漏用户信息的东西。因为这里展示的可能是别人的订单
//            model.addAttribute("modifyOrder", false);
//        } else {
//            model.addAttribute("modifyOrder", true);
//        }
//
//        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(order.getUserId());
//
//        // 通过支付网关，获取可用的银行和直连的支付方法
//        PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(PaymentConstants.PaymentGatewayName_Alipay);
//        List<Bank> banks = paymentGateway.getBanks();
//
//        Double totalPriceGeneric = order.getOrderPrice().doubleValue();
//
//        model.addAttribute("totalPriceGeneric", totalPriceGeneric);
//        model.addAttribute("payMethodAlipay", paymentGateway.getPayMethodForDirect());
//        model.addAttribute("banks", banks);
//
//        model.addAttribute("afentiOrder", order);
//        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == AfentiExam
//                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == KaplanPicaro
//                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == Walker
//                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == iandyou100
//                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == WalkerElf
//                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == Stem101
//                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == SanguoDmz
//                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == A17ZYSPG
//                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == PetsWar
//                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == WukongShizi
//                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == WukongPinyin) {
//            Map<String, Date> timeMap = userOrderServiceClient.getOrderValidityPeriodIfNecessary(order.getUserId(), order.getProductId());
//            if (MapUtils.isNotEmpty(timeMap)) {
//                Date start = timeMap.get("serviceStartTime");
//                Date end = timeMap.get("serviceEndTime");
//                if (start != null && end != null) {
//                    model.addAttribute("validityPeriod", DateUtils.dateToString(start, DateUtils.FORMAT_SQL_DATE) + "至" + DateUtils.dateToString(end, DateUtils.FORMAT_SQL_DATE));
//                }
//            }
//
//        }
//
//        model.addAttribute("supportedMobilePaymentMobileRegEx", PaymentConstants.PaymentMobile_ChinaMobile_RegEx);
//
//        Finance finance = financeServiceClient.getFinanceService()
//                .loadUserFinance(currentUserId())
//                .getUninterruptibly();
//        if (null != finance) {
//            model.addAttribute("balance", finance.getBalance());
//        }
//        User user = currentUser();
//        if (null != user && user.fetchUserType() == UserType.STUDENT) {  // 代付没有用户信息
//            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
//            model.addAttribute("haspaypwd", !StringUtils.isEmpty(ua.getPaymentPassword()));
//        } else {
//            model.addAttribute("haspaypwd", false);
//        }
//        // 学生是否开启支付权限
//        StudentExtAttribute attribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
//        if (attribute == null || attribute.fetchPayFreeStatus()) {
//            return "/apps/afenti/order/confirm";
//        } else {
//            // 获取家长列表
//            List<StudentParent> parents = parentLoaderClient.loadStudentParents(studentDetail.getId());
//            if (CollectionUtils.isNotEmpty(parents)) {
//                List<Map<String, Object>> parentMaps = new ArrayList<>();
//                for (StudentParent parent : parents) {
//                    Map<String, Object> p = new HashMap<>();
//                    p.put("parentId", parent.getParentUser().getId());
//                    p.put("callName", parent.getCallName());
//                    parentMaps.add(p);
//                }
//                model.addAttribute("parentList", parentMaps);
//                return "/apps/afenti/order/authority";
//            } else {
//                return "/apps/afenti/order/confirm";
//            }
//        }
    }

    @RequestMapping(value = "order/confirm.vpage", method = RequestMethod.POST)
    public String confirmPost(@RequestParam String orderId, @RequestParam String payMethod, Model model) {
        return "redirect:/";
        // // FIXME: 2017/3/17 没有代付逻辑了
//        if (payMethod.equals("vox_amount") && null != currentStudent()) { //代付是没有用户信息的
//            //余额支付
//            Password password = Password.of(currentStudent().getPaymentPassword());
//            String paymentPassword = Password.obscurePassword(getRequest().getParameter("paymentPwd"), password.getSalt());
//            if (!paymentPassword.equals(currentStudent().getPaymentPassword())) {
//                return "redirect:/student/center/order.vpage";
//            }
//            return "redirect:/finance/payment/amount_payment.vpage?orderId=" + orderId;
//        }
//        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
//        if (order == null)
//            return "redirect:/";
//
//
//        //被取消的订单也允许付款，因为代付流程中可能会触发多次下单流程
//        if (!order.canBePaidOrCanceled()) {
//            return "redirect:/student/center/order.vpage";
//        }
//
//        PaymentRequest paymentRequest = new PaymentRequest();
//        PaymentGateway paymentGateway = fillPaymentRequestCreatePaymentGateway(paymentRequest, payMethod, order);
//
//        if (paymentGateway == null) {
//            logger.error("failed to fillPaymentRequestCreatePaymentGateway for orderId {},payMethod {}", order.genUserOrderId(), payMethod);
//            return "redirect:/apps/afenti/order/basic-cart.vpage";
//        }
//
//        PaymentRequestForm paymentRequestForm = paymentGateway.getPaymentRequestForm(paymentRequest);
//
//        model.addAttribute("paymentRequest", paymentRequest);
//        model.addAttribute("paymentRequestForm", paymentRequestForm);
//
//        //生成跳转表单，进入支付网关
//        return "/apps/afenti/order/pay_submit";
    }

    @RequestMapping(value = "order/finished.vpage", method = RequestMethod.GET)
    public String finished(@RequestParam(required = false) String orderId, Model model) {

        //注意！这里面没有做权限检查，只是做了简单的结果显示。不能信任外部传入的 orderId —— 可能是被恶意伪造的。
        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        model.addAttribute("orderId", orderId);

        if (order != null && order.getPaymentStatus() == PaymentStatus.Paid) {
            return "/apps/afenti/order/success";
        } else {
            return "/apps/afenti/order/fail";
        }

    }

    //余额支付
    @RequestMapping(value = "amount_payment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage amountPayment(@RequestParam String paymentPwd, @RequestParam String orderId, Model model) {
        if (null == currentUserId()) {
            return MapMessage.errorMessage("登录后才能支付");
        }
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(currentUserId());
        Password password = Password.of(ua.getPaymentPassword());
        if (password == null) {
            return MapMessage.errorMessage("支付密码错误");
        }
        String paymentPassword = Password.obscurePassword(paymentPwd, password.getSalt());
        if (!paymentPassword.equals(password.getPassword())) {
            return MapMessage.errorMessage("支付密码错误");
        }
        Finance finance = financeServiceClient.getFinanceService()
                .createUserFinanceIfAbsent(currentUserId())
                .getUninterruptibly();
        if (finance == null) {
            return MapMessage.errorMessage("余额不足");
        }
        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        if (order == null) {
            return MapMessage.errorMessage("未知的订单");
        }
        // 判断重复支付的问题
        if (!order.canBePaid()) {
            return MapMessage.errorMessage("请不要重复支付");
        }
        BigDecimal price = userOrderServiceClient.getOrderCouponDiscountPrice(order);
        try {
            String memo = StringUtils.isBlank(order.getUserName()) ? "" : order.getUserName() + "购买" + order.getProductName();

            String userOrderId = order.genUserOrderId();
            MapMessage message = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("Finance:debit")
                    .keys(currentUserId())
                    .expirationInSeconds(30)
                    .callback(() -> debitFinance(price, finance, memo, userOrderId, PaymentConstants.PaymentGatewayName_17Zuoye))
                    .build()
                    .execute();

            if (message.isSuccess()) {
                PaymentCallbackContext context = new PaymentCallbackContext(PaymentConstants.PaymentGatewayName_17Zuoye, "");
                context.setTradeNumber(orderId);
                PaymentVerifiedData paymentVerifiedData = new PaymentVerifiedData();
                paymentVerifiedData.setTradeNumber(orderId);
                paymentVerifiedData.setPayAmount(price);
                context.setVerifiedPaymentData(paymentVerifiedData);
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
                    currentUserId(), orderId, finance.getUserId(), ex);
            return MapMessage.errorMessage("支付失败");
        }
    }


    /**
     * 生成微信支付二维码链接,格式如: weixin://********
     */
    @RequestMapping(value = "/paymentqrcode/link.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage paymentQrCodeLink() {
        String orderId = getRequestString("oid");
        if (StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
            if (null == order) {
                return MapMessage.errorMessage("订单不存在");
            }
            if (!order.canBePaid()) {
                return MapMessage.errorMessage("订单不可支付");
            }

            //下订单时check过了,这里再check一次,过滤从未支付订单页面过来的请求
            MapMessage checkMsg = userOrderServiceClient.checkDaysToExpireForCreateNewOrder(order.getUserId(), OrderProductServiceType.safeParse(order.getOrderProductServiceType()));
            if (!checkMsg.isSuccess()) {
                return MapMessage.errorMessage("您已经开通了" + order.getProductName() + "，可以直接学习使用哦！", "/parent/ucenter/orderlist.vpage");
            }

            WechatAbstractPaymentGateway paymentGateway = (WechatAbstractPaymentGateway) paymentGatewayManager.getPaymentGateway(PaymentConstants.PaymentGatewayName_Wechat_PcNative);
            String qrCodeUrl = paymentGateway.getCodeUrl(generatePaymentRequest(order));
            if (StringUtils.isBlank(qrCodeUrl)) {
                return MapMessage.errorMessage("生成支付二维码失败");
            }

            return MapMessage.successMessage().add("qrcode_url", qrCodeUrl);
        } catch (Exception ex) {
            logger.error("Create payment qrcode error,oid:{}", orderId, ex);
            return MapMessage.errorMessage("生成支付二维码错误");
        }
    }

    @RequestMapping(value = "/paymentqrcode/result.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage paymentQrCodeResult() {
        String orderId = getRequestString("oid");
        if (StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
            if (null == order) {
                return MapMessage.errorMessage("无效订单");
            }
            return MapMessage.successMessage().add("paid", order.getPaymentStatus() == PaymentStatus.Paid);
        } catch (Exception ex) {
            logger.error("Get order pay status for error,oid:{}", orderId, ex);
            return MapMessage.errorMessage("查询订单支付状态失败");
        }
    }

    private PaymentRequest generatePaymentRequest(UserOrder order) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setTradeNumber(order.genUserOrderId());
        paymentRequest.setProductName(order.getProductName());
        paymentRequest.setPayMethod(PaymentConstants.PaymentGatewayName_Wechat);
        //春龙的测试帐号,支付1分钱
        if (PaymentGateway.getUsersForPaymentTest(order.getUserId())) {
            paymentRequest.setPayAmount(testAmount);
        } else {
            paymentRequest.setPayAmount(userOrderServiceClient.getOrderCouponDiscountPrice(order));
        }
        paymentRequest.setSpbillCreateIp(getWebRequestContext().getRealRemoteAddress());
        paymentRequest.setCallbackBaseUrl(ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order/");
        return paymentRequest;
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
