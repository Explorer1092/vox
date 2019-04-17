package com.voxlearning.wechat.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.payment.PaymentRequestForm;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.ai.api.*;
import com.voxlearning.utopia.service.ai.constant.ChipsEnglishTeacher;
import com.voxlearning.utopia.service.ai.data.ChipsOrderExtBO;
import com.voxlearning.utopia.service.ai.data.ChipsUserOrderBO;
import com.voxlearning.utopia.service.ai.entity.AiChipsEnglishTeacher;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.wechat.anotation.CorsHeader;
import com.voxlearning.wechat.constants.AuthType;
import com.voxlearning.wechat.context.WxConfig;
import com.voxlearning.wechat.controller.AbstractChipsController;
import com.voxlearning.wechat.support.utils.OAuthUrlGenerator;
import com.voxlearning.wechat.support.utils.StringExtUntil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping(value = "/chips/order")
@CorsHeader
public class ChipsOrderController extends AbstractChipsController {

    @ImportService(interfaceClass = ChipsOrderProductLoader.class)
    private ChipsOrderProductLoader chipsOrderProductLoader;

    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @ImportService(interfaceClass = ChipsEnglishUserService.class)
    private ChipsEnglishUserService chipsEnglishUserService;

    @ImportService(interfaceClass = ChipsOrderLoader.class)
    private ChipsOrderLoader chipsOrderLoader;

    @ImportService(interfaceClass = ChipsEnglishClazzLoader.class)
    private ChipsEnglishClazzLoader chipsEnglishClazzLoader;

    @RequestMapping(value = "confirmorder.vpage", method = RequestMethod.GET)
    public String confirm(Model model) {
        String orderId = getRequestString("orderId");
        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        if (order == null || order.getPaymentStatus() != PaymentStatus.Unpaid || order.getOrderStatus() != OrderStatus.New) {
            return "redirect:/chips/center/hope.vpage";
        }
        model.addAttribute("orderId", orderId);
        return "/parent/chips/confirmorder";
    }

    @RequestMapping(value = "create.vpage", method = RequestMethod.GET)
    public String create(Model model) {
        String productId = getRequestString("productId");
        String duration = getRequestString("duration");
        if (StringUtils.isBlank(productId)) {
            return redirectWithMsg("活动不存在", model);
        }
        String productName = getRequestString("productName");

        String refer = getRequestString("refer");
        if (StringUtils.isBlank(refer)) {
            refer = "330223";
        }
        String channel = getRequestParameter("channel", "wechat");

        List<String> productList = Arrays.stream(productId.split(",")).collect(toList());
        if (CollectionUtils.isEmpty(productList)) {
            return redirectWithMsg("活动不存在", model);
        }

        Map<String, OrderProduct> orderProductMap = userOrderLoaderClient.loadOrderProducts(productList);
        OrderProduct product = orderProductMap.get(productList.get(0));
        if (product == null || product.isDisabledTrue() || !product.isOnline()
                || OrderProductServiceType.safeParse(product.getProductType()) != OrderProductServiceType.ChipsEnglish) {
            return redirectWithMsg("活动不存在或已经下线", model);
        }

        long inviter = getRequestLong("inviter");
        String staffId = getRequestString("si");
        User user = currentChipsUser();
        String groupCode = getRequestString("group");
        if (user == null || !user.isParent()) {
            if (StringUtils.isNotBlank(groupCode)) {
                String stateSur = productName + "_" + productId;
                String key = StringExtUntil.md5(stateSur);
                persistenceCache(key, stateSur);
                return "redirect:" + OAuthUrlGenerator.generatorForChips(AuthType.CHIPS_GROUP_SHOPPING.getType()+  "_" + groupCode  + "_" + key);
            }

            StringBuffer param = new StringBuffer();
            param.append("refer=" + refer).append("&channel=" + channel).append("&productId=" + productId).append("&productName=" + productName);
            if (inviter > 0L) {
                param.append("&inviter=" + inviter);
            }
            String key = StringExtUntil.md5(param.toString());
            persistenceCache(key, param.toString());
            return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChipsLogin(AuthType.CHIPS_CRATE_ORDERV2, key);
        }

        if (Long.compare(inviter, 0L) > 0 && Long.compare(inviter, user.getId()) == 0) {
            return redirectWithMsg("不能接受自己的邀请哦", model);
        }

        try {
            ChipsUserOrderBO chipsOrder = new ChipsUserOrderBO(user.getId(), productList);
            if (Long.compare(inviter, 0L) > 0) {
                chipsOrder.setInviter(inviter);
            }
            chipsOrder.setRefer(refer);
            chipsOrder.setChannel(channel);
            chipsOrder.setSaleStaffId(staffId);
            chipsOrder.setProductName(productName);
            chipsOrder.setGroupCode(groupCode);
            MapMessage messa = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("chipsEnglishUserService.createOrder")
                    .keys(user.getId())
                    .callback(() -> chipsEnglishUserService.createOrder(chipsOrder))
                    .build()
                    .execute();
            if (!messa.isSuccess()) {
                if (StringUtils.isNotBlank(messa.getErrorCode()) && messa.getErrorCode().equals("100010")) {
                    return "redirect:/chips/center/hope.vpage";
                }
                return redirectWithMsg(StringUtils.isBlank(messa.getInfo()) ? "生成订单失败" : messa.getInfo(), model);
            }
            String orderId = SafeConverter.toString(messa.get("orderId"));
            return "redirect:/chips/order/confirmorder.vpage?orderId=" + orderId + "&duration=" + duration + "&inviter=" + inviter;
        } catch (CannotAcquireLockException e) {
            return redirectWithMsg("正在处理中", model);
        } catch (DuplicatedOperationException e) {
            return redirectWithMsg("您点击太快了，请重试", model);
        } catch (Exception e) {
            return redirectWithMsg("生成订单失败", model);
        }
    }

    // 订单详情
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage orderDetail() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("用户不存在");
        }

        String orderId = getRequestString("orderId");
        if (StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage("参数为空");
        }

        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        if (order == null || order.getPaymentStatus() != PaymentStatus.Unpaid || order.getOrderStatus() != OrderStatus.New) {
            return MapMessage.errorMessage("订单不存在或者已经失效");
        }

        MapMessage mapMessage = MapMessage.successMessage()
                .add("productName", order.getProductName())
                .add("price", order.getOrderPrice())
                .add("createDate", DateUtils.dateToString(order.getCreateDatetime(), "yyyy.MM.dd"));

        List<CouponShowMapper> mappers = userOrderLoaderClient.loadOrderUsableCoupons(order, user.getId());
        if (CollectionUtils.isNotEmpty(mappers)) {
            mappers.sort(Comparator.comparing(CouponShowMapper::getTypeValue).reversed());
            mapMessage.add("discountPrice", mappers.get(0).getDiscountPrice());
            mapMessage.add("coupons", mappers);
        } else {
            mapMessage.add("discountPrice", order.getOrderPrice());
            mapMessage.add("coupons", Collections.emptyList());
        }
        return mapMessage;
    }

    // 关联订单与使用的优惠劵
    @RequestMapping(value = "relatedcouponorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage relatedCouponOrder() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        String orderId = getRequestString("orderId");
        String refId = getRequestString("refId");
        String couponId = getRequestString("couponId");
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        if (userOrder == null || userOrder.getPaymentStatus() != PaymentStatus.Unpaid || userOrder.getOrderStatus() != OrderStatus.New) {
            return MapMessage.errorMessage("订单不存在或者已经失效");
        }
        if (StringUtils.isBlank(refId) || StringUtils.isBlank(couponId)) {
            return MapMessage.errorMessage("请选择优惠劵");
        }
        // 如果是已经关联的 直接返回成功
        if (StringUtils.isNotBlank(userOrder.getCouponRefId()) && Objects.equals(userOrder.getCouponRefId(), refId)) {
            return MapMessage.successMessage();
        }

        try {
            return atomicLockManager.wrapAtomic(userOrderServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("ORDER_COUPON_RELATED")
                    .keys(orderId)
                    .proxy()
                    .relatedCouponOrder(userOrder, refId, couponId);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 确认支付
    @RequestMapping(value = "confirm.vpage", method = RequestMethod.GET)
    public String confirmOrder(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }
        String orderId = getRequestString("orderId");

        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        if (order == null || order.getPaymentStatus() != PaymentStatus.Unpaid || order.getOrderStatus() != OrderStatus.New) {
            return "redirect:/chips/center/hope.vpage";
        }

        if (!isPayForTest()) {
            PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(PaymentConstants.PaymentGatewayName_Wechat_Chips);
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setTradeNumber(order.genUserOrderId());
            paymentRequest.setProductName(order.getProductName());
            paymentRequest.setPayMethod(PaymentConstants.PaymentGatewayName_Wechat_Chips);
            paymentRequest.setSpbillCreateIp(getRequestContext().getRealRemoteAddress());
            BigDecimal amount = userOrderServiceClient.getOrderCouponDiscountPrice(order);
            if (PaymentGateway.getUsersForPaymentTest(order.getUserId()) || ChipsTestUser.contains(order.getUserId())) {
                amount = new BigDecimal(0.01);
            }
            paymentRequest.setPayAmount(amount);
            paymentRequest.setPayUser(user.getId());
            paymentRequest.setCallbackBaseUrl(ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order");
            // 这里的openId可能会不一样，因为是公用cookie，从这里查询一下当前用户绑定的openId
            paymentRequest.setOpenid(getChipsOpenId(user));
            PaymentRequestForm paymentRequestForm = paymentGateway.getPaymentRequestForm(paymentRequest);
            if (MapUtils.isNotEmpty(paymentRequestForm.getFormFields())) {
                Map<String, Object> payMap = paymentRequestForm.getFormFields();
                String returnUrl = "/chips/order/" + orderId + "/paymentsuccess.vpage";
                WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(WechatType.CHIPS));
                initChipsPayModel(model, wxConfig, WechatType.CHIPS,
                        SafeConverter.toLong(payMap.get("timeStamp")),
                        SafeConverter.toString(payMap.get("nonceStr")),
                        SafeConverter.toString(payMap.get("package")),
                        SafeConverter.toString(payMap.get("sign")),
                        returnUrl);
            } else {
                return redirectWithMsg("调用第三方支付失败", model);
            }
        } else {
            model.addAttribute("oid", order.genUserOrderId());
        }

        return "/parent/wxpay/pay";
    }

    private String getChipsOpenId(User user) {
        String openid = Optional.ofNullable(wechatLoaderClient.getWechatLoader().loadUserWechatRefs(Collections.singleton(user.getId()), WechatType.CHIPS))
                .map(ma -> ma.get(user.getId()))
                .filter(CollectionUtils::isNotEmpty)
                .map(userWechatRefs -> {
                    userWechatRefs.sort(Comparator.comparing(UserWechatRef::getUpdateDatetime).reversed());
                    String openId = getOpenId();
                    for (UserWechatRef wechatRef : userWechatRefs) {
                        if (wechatRef.getOpenId().equals(openId)) {
                            return wechatRef;
                        }
                    }
                    return userWechatRefs.get(0);
                })
                .map(UserWechatRef::getOpenId)
                .orElse("");
        return openid;
    }

    // 支付成功
    @RequestMapping(value = "{order}/paymentsuccess.vpage", method = RequestMethod.GET)
    public String paymentsuccess(@PathVariable("order") String order, Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }

        ChipsOrderExtBO chipsOrderExtBO = chipsOrderLoader.loadOrderExtInfo(order);
        if (chipsOrderExtBO != null && StringUtils.isNotBlank(chipsOrderExtBO.getGroupCode()) && Boolean.FALSE.equals(chipsOrderExtBO.getGroupSuccess())) {
            long surTime = DateUtils.addDays(chipsOrderExtBO.getCreateDate(), 1).getTime() - System.currentTimeMillis();
            surTime = surTime > 0L ?  surTime / 1000L : 0;
            ParentExtAttribute parentExt = parentLoaderClient.loadParentExtAttribute(chipsOrderExtBO.getUserId());
            String wechatName =  Optional.ofNullable(parentExt)
                    .filter(e -> StringUtils.isNotBlank(e.getWechatNick()))
                    .map(ParentExtAttribute::getWechatNick)
                    .orElse("薯条学员");
            String image =  Optional.ofNullable(parentExt)
                    .filter(e -> StringUtils.isNotBlank(e.getWechatImage()))
                    .map(ParentExtAttribute::getWechatImage)
                    .orElse("http://cdn.17zuoye.com/fs-resource/5c6a53f0b43327e1ce9af229.png");
            model.addAttribute("userName", wechatName);
            model.addAttribute("image", image);
            model.addAttribute("type", "prod7");
            model.addAttribute("surplusTime", surTime);
            model.addAttribute("code", chipsOrderExtBO.getGroupCode());
            WechatType wechatType = WechatType.CHIPS;
            WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
            initWechatConfigModel(model, wxConfig, wechatType);
            return "parent/chips/formal_group_message";
        }

        Optional<ChipsEnglishTeacher> chipsEnglishTeacherOptional = Optional.ofNullable(userOrderLoaderClient.loadUserOrder(order))
                .map(UserOrder::getProductId)
                .map(e -> chipsEnglishClazzService.loadClazzIdByUserAndProduct(user.getId(), e))
                .map(ChipsEnglishClass::getTeacherInfo);
        String wxCode = chipsEnglishTeacherOptional.map(ChipsEnglishTeacher::getWxCode).orElse(null);
        String qrCode = chipsEnglishTeacherOptional.map(ChipsEnglishTeacher::getQrImage).orElse(null);
        String companyQR = chipsEnglishTeacherOptional.map(ChipsEnglishTeacher::getCompanyQrImage).orElse(null);
        model.addAttribute("wxCode", wxCode);
        model.addAttribute("qrCode", qrCode);
        model.addAttribute("companyQrCode", companyQR);
        model.addAttribute("teacherName", chipsEnglishTeacherOptional.map(ChipsEnglishTeacher::name).orElse(""));
        String techerImage = chipsEnglishTeacherOptional
                .map(te -> chipsEnglishClazzLoader.loadChipsEnglishTeacherByName(te.name()).stream().findFirst().orElse(null))
                .map(AiChipsEnglishTeacher::getHeadPortrait)
                .orElse("");
        model.addAttribute("teacherAvatar", techerImage);
        return "/parent/chips/paymentsuccess";
    }

    @RequestMapping(value = "officialProduct/load.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loadOfficialProduct() {
        User user = currentChipsUser();
        String typeName = getRequestString("type");
        if (StringUtils.isBlank(typeName)) {
            return MapMessage.errorMessage("类型为空");
        }

        return chipsOrderProductLoader.loadOfficialProductInfoByType(typeName, user != null && user.isParent() ? user.getId() : null);
    }
}
