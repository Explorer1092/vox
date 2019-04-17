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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.RealnameRule;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.core.ObjectIdEntity;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.coupon.api.constants.CouponType;
import com.voxlearning.utopia.service.coupon.api.constants.CouponUserStatus;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.*;
import com.voxlearning.utopia.service.order.api.entity.*;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.api.mapper.OrderShowMapper;
import com.voxlearning.utopia.service.order.api.mapper.OrderTabMapper;
import com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil;
import com.voxlearning.utopia.service.order.api.util.UserOrderUtil;
import com.voxlearning.utopia.service.parent.api.PalaceMuseumLoader;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.PalaceGift;
import com.voxlearning.utopia.service.parent.api.support.PalaceMuseumProductSupport;
import com.voxlearning.utopia.service.region.api.constant.RegionConstants;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.region.util.RegionGrayUtils;
import com.voxlearning.utopia.service.reminder.api.ReminderService;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.utopia.service.userlevel.api.entity.UserActivationHomeLevel;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.VendorAppsServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.FairylandLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.temp.ApplePayParent;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.support.ParentCrosHeaderSupport;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.AppSystemType.ANDROID;
import static com.voxlearning.utopia.api.constant.AppSystemType.IOS;
import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform.PARENT_APP;
import static com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType.APPS;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Hailong Yang
 * @since 2015/09/14
 */
@Slf4j
@Controller
@RequestMapping(value = "/parentMobile/ucenter")
public class MobileParentUcenterController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private VendorAppsServiceClient vendorAppsServiceClient;

    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @ImportService(interfaceClass = ReminderService.class)
    private ReminderService reminderService;

    @AlpsQueueProducer(queue = "parent.queue.changeName", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer changeNameMessageProducer;

    //只显示在pc上可使用的app的用户
    private static final Set<Long> onlyShowPcVendorAppUser = new HashSet<>(Collections.singletonList(20001L));
    //需要屏蔽的app
    private static final Set<String> notShowAppKeyForBlackUser = new HashSet<>(Arrays.asList("UsaAdventure", "AfentiMath", "AfentiExam", "GreatAdventure"));
    private static Integer maxGlory = 100;

    static {
        if (RuntimeMode.current() == Mode.TEST || RuntimeMode.current() == Mode.DEVELOPMENT)
            maxGlory = 10;
    }

    @Inject
    VendorLoaderClient vendorLoaderClient;
    @Inject
    FairylandLoaderClient fairylandLoaderClient;

    @Inject
    CouponLoaderClient couponLoaderClient;
    @Inject
    private UserBlacklistServiceClient userBlacklistServiceClient;

    @ImportService(interfaceClass = PalaceMuseumLoader.class)
    private PalaceMuseumLoader palaceMuseumLoader;

    /**
     * about us
     */
    @RequestMapping(value = "about_us.vpage", method = RequestMethod.GET)
    public String about_us(Model model) {
        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE));
        }
        return "parentmobile/person/aboutUs";
    }

    /**
     * AbstractMobileParentController中针对低版本的升级跳转路由
     * app升级
     */
    @RequestMapping(value = "upgrade.vpage", method = RequestMethod.GET)
    public String trustUpgrade() {
        return "parentmobile/person/upgrade_app";
    }

    /**
     * 如何成为vip跳转路由
     */
    @RequestMapping(value = "vip.vpage", method = RequestMethod.GET)
    public String vipHelp(Model model) {
        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE));
        }
        return "parentmobile/person/vipHelp";
    }

    /**
     * 产品购买协议
     */
    @RequestMapping(value = "shopagreement.vpage", method = RequestMethod.GET)
    public String shopagreement() {
        return "parentmobile/person/shopagreement";
    }

    /**
     * 意见反馈
     */
    @RequestMapping(value = "feedback.vpage", method = RequestMethod.GET)
    public String feedback(Model model) {
        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE));
        }
        setRouteParameter(model);
        return "parentmobile/person/feedback";
    }

    /**
     * 常见问题 详情页
     */
    @RequestMapping(value = "questionDetail.vpage", method = RequestMethod.GET)
    public String questionDetail(Model model) {
        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE));
        }
        setRouteParameter(model);
        return "parentmobile/person/questionDetail";
    }

    /**
     * 常见问题
     */
    @RequestMapping(value = "questions.vpage", method = RequestMethod.GET)
    public void questions(Model model) {
        try {
            getResponse().sendRedirect("/view/mobile/parent/question");
            getResponse().setStatus(200);
        } catch (IOException e) {
        }
    }

    /**
     * 检查是否绑定班级
     */
    @RequestMapping(value = "isBindClazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage isBindClazz() {
        Long studentId = getRequestLong("sid");

        try {
            if (studentId <= 0) {
                return MapMessage.errorMessage("invalid studentid").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
            }

            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
            }

            if (!studentIsParentChildren(currentUserId(), studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
            }
            return MapMessage.successMessage().add("isBindClazz", isBindClazz(studentId));
        } catch (Exception ex) {
            return MapMessage.errorMessage(ex.getMessage()).setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    @RequestMapping(value = "orderlistForInterest.vpage", method = RequestMethod.GET)
    public void orderListForInterest(Model model, HttpServletResponse response) {
        try {
            response.sendRedirect(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/order/list");
        } catch (Exception e) {
            logger.error("orderlistForInterest redirect error", e);
        }
//        if (currentParent() == null) {
//            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE));
//        }
//
//        setRouteParameter(model);
//
//        return "parentmobile/person/orderListForInterest";
    }

    @RequestMapping(value = "orderlist.vpage", method = RequestMethod.GET)
    public String orderList(Model model) {
        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE));
        }

        setRouteParameter(model);

        String version = getRequestString("app_version");
        model.addAttribute("isNotSupportTrust", StringUtils.isNotBlank(version) && VersionUtil.compareVersion(version, "1.3.6") < 0);

        return "parentmobile/person/orderList";
    }

    /**
     * 我的订单获取tab
     */
    @RequestMapping(value = "gettablist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTabList() {
        List<OrderTabMapper> types = new ArrayList<>();
        for (ParentOrderShowTypes type : ParentOrderShowTypes.values()) {
            OrderTabMapper mapper = new OrderTabMapper();
            mapper.setName(type.name());
            mapper.setShowName(type.showName);
            mapper.setType(type.type);
            types.add(mapper);
        }
        List<OrderTabMapper> statusList = new ArrayList<>();
        for (ParentOrderShowStatus status : ParentOrderShowStatus.values()) {
            OrderTabMapper mapper = new OrderTabMapper();
            mapper.setName(status.name());
            mapper.setShowName(status.showName);
            mapper.setType(status.type);
            statusList.add(mapper);
        }
        return MapMessage.successMessage()
                .add("status", statusList)
                .add("types", types);
    }

    /**
     * 获取订单详情
     */
    @RequestMapping(value = "orderdetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage orderDetail() {
        String orderId = getRequestString("oid");
        Long userId = getRequestLong("uid");
        UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
        if (order == null) {
            return MapMessage.errorMessage("无效的订单");
        }
        // 7天未支付的单子直接返回错误
        Date before7 = DateUtils.nextDay(new Date(), -7);
        if (order.getCreateDatetime().before(before7) && order.canBePaidOrCanceled()) {
            return MapMessage.errorMessage("订单已过期不能支付");
        }
        List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userId);
        List<FairylandProduct> products = new ArrayList<>();
        String iconOrderServiceType = transFormAfentiImproved(order.getOrderProductServiceType());
        FairylandProduct product = fairylandLoaderClient.loadFairylandProduct(PARENT_APP, null, iconOrderServiceType);
        if (product != null) {
            products.add(product);
        }
        OrderShowMapper mapper = convertShowMapper(order, paymentHistories, products);
        boolean canBuyAgain = studentCanBuyAgain();
        if (order.getPaymentStatus() == PaymentStatus.Refund
                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == YiQiXue
                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()).isOrderClosed()) {
            canBuyAgain = false;
        }
        Map<String, Object> expressMap = new HashMap<>();
        Map<String, Object> extAttrMap = JsonUtils.fromJson(order.getExtAttributes());
        if (MapUtils.isNotEmpty(extAttrMap) && extAttrMap.get("logisticsCompany") != null
                && extAttrMap.get("logisticsNum") != null) {
            expressMap.put("name", extAttrMap.get("logisticsCompany"));
            expressMap.put("code", extAttrMap.get("logisticsNum"));
        }
        return MapMessage.successMessage()
                .add("canBuyAgain", canBuyAgain)
                .add("orderDetail", mapper)
                .add("express", MapUtils.isNotEmpty(expressMap) ? expressMap : null);
    }

    /**
     * 用户删除订单
     */
    @RequestMapping(value = "deleteorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteOrder() {
        String orderId = getRequestString("oid");
        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        if (order == null) {
            return MapMessage.errorMessage("无效的订单");
        }
        if (!order.canBePaid()) {
            return MapMessage.errorMessage("此订单不支持删除");
        }
        // 已支付的订单不允许取消
        if (order.getPaymentStatus() == PaymentStatus.Paid) {
            return MapMessage.errorMessage("对不起，已支付的订单不能取消");
        }
        return userOrderServiceClient.cancelOrder(orderId);
    }

    @Data
    private static class OrderShowMapperPlus extends OrderShowMapper {
        private static final long serialVersionUID = 1136626015764149783L;
        private Boolean isPalaceGift;
        private Boolean beReceived;
        private String giftReceiver;
        private String giftReceiverPhone;
    }

    private String transFormAfentiImproved(String orderProductServiceType) {
        String orderServiceType = "";
        if (Objects.equals(orderProductServiceType, OrderProductServiceType.AfentiChineseImproved.name())) {
            orderServiceType = OrderProductServiceType.AfentiChinese.name();
        } else if (Objects.equals(orderProductServiceType, OrderProductServiceType.AfentiMathImproved.name())) {
            orderServiceType = OrderProductServiceType.AfentiMath.name();
        } else if (Objects.equals(orderProductServiceType, OrderProductServiceType.AfentiExamImproved.name())) {
            orderServiceType = OrderProductServiceType.AfentiExam.name();
        } else {
            orderServiceType = orderProductServiceType;
        }
        return orderServiceType;
    }


    private OrderShowMapper convertShowMapper(UserOrder order,
                                              List<UserOrderPaymentHistory> paymentHistories,
                                              List<FairylandProduct> fairylandProducts) {
        OrderShowMapperPlus mapper = new OrderShowMapperPlus();
        mapper.setOrderId(order.genUserOrderId());
        mapper.setOrderProductServiceType(order.getOrderProductServiceType());
        mapper.setCreateDatetime(DateUtils.dateToString(order.getCreateDatetime(), "yyyy-MM-dd HH:mm"));
        mapper.setDayStr(DateUtils.dateToString(order.getCreateDatetime(), "yyyy-MM-dd"));
        mapper.setUserId(order.getUserId());
        mapper.setProductId(order.getProductId());
        String iconOrderServiceType = transFormAfentiImproved(order.getOrderProductServiceType());
        FairylandProduct product = fairylandProducts.stream().filter(p -> Objects.equals(p.getAppKey(), iconOrderServiceType))
                .findFirst().orElse(null);
        if (product != null && StringUtils.isNotBlank(product.getProductIcon())) {
            mapper.setIcon(product.getProductIcon());
        }
        if (StringUtils.isNotBlank(order.getUserName())) {
            mapper.setUserName(order.getUserName());
        }

        String extAttributes = order.getExtAttributes();
        Map<String, Object> extAttrMap = JsonUtils.fromJson(extAttributes);

        if (order.getPaymentStatus() == PaymentStatus.Unpaid) {
            mapper.setStatus("待支付");
        } else if (order.getPaymentStatus() == PaymentStatus.Paid) {
            mapper.setStatus("已支付");
            if (MapUtils.isNotEmpty(extAttrMap) && extAttrMap.get("logisticsCompany") != null && extAttrMap.get("logisticsNum") != null) {
                mapper.setStatus("已发货");
            }
        } else if (order.getPaymentStatus() == PaymentStatus.Refund) {
            mapper.setStatus("已退款");
        }
        mapper.setProductName(order.getProductName());
        // 小U绘本单独处理
        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == ELevelReading) {
            List<UserOrderProductRef> orderProductRefs = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
            if (CollectionUtils.isNotEmpty(orderProductRefs)) {
                List<String> productNames = orderProductRefs.stream().map(UserOrderProductRef::getProductName).collect(Collectors.toList());
                List<String> showNames = new ArrayList<>();
                for (String n : productNames) {
                    showNames.add("小U绘本-" + n);
                }
                mapper.setProductNames(showNames);
            } else {
                OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(order.getProductId());
                if (orderProduct != null) {
                    List<String> showNames = new ArrayList<>();
                    showNames.add("小U绘本-" + orderProduct.getName());
                    mapper.setProductNames(showNames);
                }
            }
        } else {
            List<UserOrderProductRef> orderProductRefs = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
            if (CollectionUtils.isNotEmpty(orderProductRefs)) {
                List<String> productNames = orderProductRefs.stream().map(UserOrderProductRef::getProductName).collect(Collectors.toList());
                mapper.setProductNames(productNames);
            }
        }

        mapper.setOrderPrice(order.getOrderPrice().doubleValue());
        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == PicListen) {
            Map<String, Object> attrMap = JsonUtils.fromJson(order.getProductAttributes());
            String albumId = SafeConverter.toString(attrMap.get("albumId"));
            if (StringUtils.isNotBlank(albumId)) {
                mapper.setAlbumId(albumId);
            }
        }
        UserOrderPaymentHistory history = paymentHistories.stream().filter(p -> Objects.equals(p.getOrderId(), order.getId()))
                .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid).findFirst().orElse(null);
        if (history != null) {
            mapper.setPayAmount(history.getPayAmount().doubleValue());
            mapper.setPayDatetime(DateUtils.dateToString(history.getPayDatetime(), "yyyy-MM-dd HH:mm"));
            if (history.getServiceStartTime() != null
                    && history.getServiceEndTime() != null
                    && !OrderProductServiceType.getHideValidatePeriodTypes().contains(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))) {
                mapper.setValidatePeriod(DateUtils.dateToString(history.getServiceStartTime(), "yyyy-MM-dd") + "至" + DateUtils.dateToString(history.getServiceEndTime(), "yyyy-MM-dd"));
            }
            if (StringUtils.isNotBlank(history.getPayMethod())) {
                if (history.getPayMethod().contains("wechatpay")) {
                    mapper.setPayMethod("微信支付");
                } else if (history.getPayMethod().contains("alipay")) {
                    mapper.setPayMethod("支付宝支付");
                } else if (history.getPayMethod().contains("voxpay")) {
                    mapper.setPayMethod("学贝支付");
                }
            }
        }

        // 是否是过期的小U订单
        if (AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType())) && UserOrderUtil.isUnpaidAfentiImpovedPeriodOrder(order)) {
            mapper.setExpiredAfentiOrder(true);
        } else {
            mapper.setExpiredAfentiOrder(false);
        }
        //故宫的买赠商品
        if (PalaceMuseumProductSupport.GIFT_PRODUCT_ID.equals(order.getProductId())) {
            mapper.setIsPalaceGift(true);
            PalaceGift palaceGift = palaceMuseumLoader.loadPalaceGiftByBuyerIdOrderId(currentUserId(), order.genUserOrderId());
            if (palaceGift == null) {
                mapper.setBeReceived(false);
            } else {
                mapper.setBeReceived(palaceGift.getGiftStatus() == PalaceGift.GiftStatus.RECEIVED);
                if (palaceGift.getGiftStatus() == PalaceGift.GiftStatus.RECEIVED) {
                    Long receiverId = palaceGift.getReceiverId();
                    User user = raikouSystem.loadUser(receiverId);
                    if (user != null) {
                        mapper.setGiftReceiver(fetchShowName(user));
                        String s = sensitiveUserDataServiceClient.loadUserMobileObscured(user.getId());
                        mapper.setGiftReceiverPhone(s);
                    }
                }
            }
        } else {
            mapper.setIsPalaceGift(false);
        }
        return mapper;
    }

    private String fetchShowName(User parent) {
        if (parent == null) {
            return "您的亲友";
        }
        String realname = parent.fetchRealname();
        if (StringUtils.isNotBlank(realname)) {
            return realname;
        }
        List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
        if (CollectionUtils.isEmpty(students)) {
            return "您的亲友";
        }
        String s = students.get(0).fetchRealname();
        if ("未命名".equals(s)) {
            return "您的亲友";
        }
        if (StringUtils.isNotBlank(s)) {
            return s + "小朋友的家长";
        }
        return "您的亲友";
    }


    /**
     * 订单列表
     *
     * @deprecated 这个接口已经迁移到galaxy-webapp了
     */
    @RequestMapping(value = "doOrderlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public MapMessage doOrderList() {
        Integer type = getRequestInt("t");
        Integer status = getRequestInt("s");
        Integer currentPage = getRequestInt("currentPage");
        Long parentId = currentUserId();
        currentPage = currentPage <= 0 ? 1 : currentPage;
        Integer pageSize = 10;

        try {
            if (null == parentId) {
                return MapMessage.errorMessage("invalid parameters").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
            }

            User parent = currentParent();
            if (parent == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
            }

            // 家长属于个人黑名单 直接返回空列表
            boolean hitBlack = userBlacklistServiceClient.isInUserBlackList(parent);
            if (hitBlack) {
                return MapMessage.successMessage().add("pageOrder", new PageImpl<>(Collections.emptyList()));
            }

            //查出没有班级或未毕业的孩子
            List<User> children = getChildrenNotInClazzOrNotGraduated(parentId);

            List<UserOrder> orders = new ArrayList<>();
            List<UserOrderPaymentHistory> historyList = new ArrayList<>();

            if (CollectionUtils.isNotEmpty(children)) {
                Map<String, OrderProduct> timeBaseAvailableProductMap = userOrderLoaderClient.loadAllTimeBaseAvailableProduct()
                        .stream()
                        .collect(Collectors.toMap(ObjectIdEntity::getId, s -> s));

                for (User user : children) {
                    List<UserOrder> childOrders = getChildOrderNeedDisplay(timeBaseAvailableProductMap, user);
                    orders.addAll(childOrders);

                    // 付费历史
                    List<UserOrderPaymentHistory> childPaymentHistories = getUserOrderPaymentHistories(user.getId());
                    historyList.addAll(childPaymentHistories);
                }
            }
            // 加入家长的订单
            List<UserOrder> parentOrders = getParentOrderNeedDisplay(parentId);
            orders.addAll(parentOrders);

            // 付费历史
            List<UserOrderPaymentHistory> parentPaymentHistories = getUserOrderPaymentHistories(parentId);
            historyList.addAll(parentPaymentHistories);

            // 过滤出家长app支持的应用列表的订单
            orders = filterParentAppSupportedVendorAppOrder(orders);

            // 7天之内未支付的单子自动取消
            orders = cancelNewOrdersUnpaidOver7Days(orders);

            // 过滤180天之内的订单
            orders = filterOrdersIn180Days(orders);

            ParentOrderShowStatus showStatus = getParentOrderShowStatus(status);
            orders = filterOrderShowStatus(orders, showStatus);

            ParentOrderShowTypes showTypes = getParentOrderShowTypes(type);
            orders = filterOrderShowTypes(orders, showTypes);

            List<OrderShowMapper> mapperList = getOrderListShowMapper(orders, historyList);

            boolean canBuyAgain = studentCanBuyAgain();
            List<String> canNotBuyAgainOrderId = getOrdersCanNotBuyAgain(mapperList);

            Pageable pageable = new PageRequest(currentPage - 1, pageSize);
            Page<OrderShowMapper> pageData = PageableUtils.listToPage(mapperList, pageable);
            return MapMessage.successMessage()
                    .add("canBuyAgain", canBuyAgain)
                    .add("orderCanNotBuyAgain", canNotBuyAgainOrderId)
                    .add("pageOrder", pageData);
        } catch (Exception ex) {
            logger.error("load order list failed. parentId:{},type:{},status:{},currentPage:{},pageSize:{}", parentId, type, status, currentPage, pageSize, ex);
            return MapMessage.errorMessage("查询订单列表失败").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    private boolean studentCanBuyAgain() {
        boolean canBuyAgain = true;
        long studentId = getRequestLong("sid");
        if (VersionUtil.compareVersion(getAppVersion(), "2.3.5") >= 0 && 0L != studentId) {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            if (null == clazz || clazz.isTerminalClazz() || clazz.getEduSystem().getKtwelve() != Ktwelve.PRIMARY_SCHOOL) {
                canBuyAgain = false;
            }
        }
        return canBuyAgain;
    }

    /**
     * 过滤出家长APP支持的订单
     */
    @NotNull
    private List<UserOrder> filterParentAppSupportedVendorAppOrder(List<UserOrder> orders) {
        Map<String, VendorApps> vam = vendorAppsServiceClient.getVendorAppsBuffer().loadVendorAppsList().stream()
                .filter(v -> v.isVisible(RuntimeMode.current().getLevel()))
                .filter(VendorApps::getWechatBuyFlag)
                .collect(Collectors.toMap(VendorApps::getAppKey, Function.identity()));
        orders = orders.stream().filter(o -> vam.containsKey(o.getOrderProductServiceType())).collect(Collectors.toList());
        return orders;
    }

    /**
     * 过滤出家长需要显示的订单
     */
    @NotNull
    private List<UserOrder> getParentOrderNeedDisplay(Long parentId) {
        return userOrderLoaderClient.loadUserOrderList(parentId).stream()
                .filter(order -> order.getOrderProductServiceType() != null)
                // 过滤一起学翻转课堂的单子
                .filter(order -> order.getOrderType() != null && order.getOrderType() != OrderType.yi_qi_xue_fz)
                // 洛亚传说 三国订单不显示
                .filter(o -> (OrderProductServiceType.safeParse(o.getOrderProductServiceType()) != A17ZYSPG && OrderProductServiceType.safeParse(o.getOrderProductServiceType()) != SanguoDmz) ||
                        (OrderProductServiceType.safeParse(o.getOrderProductServiceType()) == A17ZYSPG && o.getPaymentStatus() == PaymentStatus.Paid) ||
                        (OrderProductServiceType.safeParse(o.getOrderProductServiceType()) == SanguoDmz && o.getPaymentStatus() == PaymentStatus.Paid))
                .collect(Collectors.toList());
    }

    /**
     * 过滤出孩子需要显示的订单
     */
    @NotNull
    private List<UserOrder> getChildOrderNeedDisplay(Map<String, OrderProduct> timeBaseAvailableProductMap, User user) {
        List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderListIncludedCanceled(user.getId());
        userOrders = userOrders.stream()
                // 过滤取消的未支付的单子
                .filter(order -> !(order.getOrderStatus() == OrderStatus.Canceled && order.getPaymentStatus() == PaymentStatus.Unpaid))
                // 过滤一起学翻转课堂的单子
                .filter(order -> order.getOrderType() != null && order.getOrderType() != OrderType.yi_qi_xue_fz)
                .filter(order -> timeBaseAvailableProductMap.containsKey(order.getProductId()))
                // 过滤类型不为空的单子
                .filter(order -> order.getOrderProductServiceType() != null)
                // 未支付的订单价格与产品不相等直接跳过
                .filter(order -> OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == PicListen || order.getPaymentStatus() != PaymentStatus.Unpaid ||
                        (order.getPaymentStatus() == PaymentStatus.Unpaid && matchOrderPrice(order, timeBaseAvailableProductMap)))
                // 洛亚传说 三国订单不显示
                .filter(o -> (OrderProductServiceType.safeParse(o.getOrderProductServiceType()) != A17ZYSPG && OrderProductServiceType.safeParse(o.getOrderProductServiceType()) != SanguoDmz) ||
                        (OrderProductServiceType.safeParse(o.getOrderProductServiceType()) == A17ZYSPG && o.getPaymentStatus() == PaymentStatus.Paid) ||
                        (OrderProductServiceType.safeParse(o.getOrderProductServiceType()) == SanguoDmz && o.getPaymentStatus() == PaymentStatus.Paid))
                .collect(Collectors.toList());
        return userOrders;
    }

    @NotNull
    private List<UserOrderPaymentHistory> getUserOrderPaymentHistories(Long userId) {
        return userOrderLoaderClient.loadUserOrderPaymentHistoryList(userId)
                .stream().filter(p -> p.getPaymentStatus() == PaymentStatus.Paid)
                .collect(Collectors.toList());
    }

    /**
     * 查出没有班级的孩子和未毕业的孩子
     */
    private List<User> getChildrenNotInClazzOrNotGraduated(Long parentId) {
        List<User> children = studentLoaderClient.loadParentStudents(parentId);
        if (CollectionUtils.isEmpty(children)) {
            return new ArrayList<>();
        }

        Set<Long> studentIds = children.stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds);
        if (MapUtils.isNotEmpty(studentDetails)) {
            children = studentDetails.values().stream()
                    .filter(e -> e.getClazz() == null || !e.getClazz().isTerminalClazz())
                    .collect(Collectors.toList());
        } else {
            children = new ArrayList<>();
        }
        return children;
    }

    @Nullable
    private ParentOrderShowStatus getParentOrderShowStatus(Integer status) {
        if (null != status && status != 0) {
            return ParentOrderShowStatus.of(status);
        }
        return null;
    }

    @Nullable
    private ParentOrderShowTypes getParentOrderShowTypes(Integer type) {
        if (null != type && type != 0) {
            return ParentOrderShowTypes.of(type);
        }
        return null;
    }

    //获取订单列表中不能显示再次购买按钮的订单ID集合
    private List<String> getOrdersCanNotBuyAgain(List<OrderShowMapper> mapperList) {
        List<String> canNotBuyAgainOrderId = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mapperList) && VersionUtil.compareVersion(getAppVersion(), "2.3.5") >= 0) {
            for (OrderShowMapper mapper : mapperList) {
                if (OrderProductServiceType.safeParse(mapper.getOrderProductServiceType()) == YiQiXue
                        || OrderProductServiceType.safeParse(mapper.getOrderProductServiceType()).isOrderClosed()) {
                    canNotBuyAgainOrderId.add(mapper.getOrderId());
                }
            }
        }
        return canNotBuyAgainOrderId;
    }

    private boolean matchOrderPrice(UserOrder order, Map<String, OrderProduct> productInfoMap) {
        List<UserOrderProductRef> uoprList = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
        if (CollectionUtils.isNotEmpty(uoprList)) {
            BigDecimal totalPrice = new BigDecimal(0);
            for (UserOrderProductRef uopr : uoprList) {
                OrderProduct product = productInfoMap.get(uopr.getProductId());
                if (product != null) {
                    totalPrice = totalPrice.add(product.getPrice());
                }
            }

            return Math.abs(totalPrice.doubleValue() - order.getOrderPrice().doubleValue()) <= 1e-6;

        } else {
            OrderProduct product = productInfoMap.get(order.getProductId());
            return product != null && (Math.abs(product.getPrice().doubleValue() - order.getOrderPrice().doubleValue()) <= 1e-6);
        }
    }

    /**
     * 7天之内未支付的单子自动取消
     */
    private List<UserOrder> cancelNewOrdersUnpaidOver7Days(List<UserOrder> orders) {
        Date date7 = DateUtils.nextDay(new Date(), -7);
        List<UserOrder> cancelOrders7 = orders.stream().filter(o -> o.getCreateDatetime() != null && date7.after(o.getCreateDatetime()))
                .filter(o -> o.getOrderStatus() == OrderStatus.New && o.getPaymentStatus() == PaymentStatus.Unpaid)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(cancelOrders7)) {
            userOrderServiceClient.cancelOrders(cancelOrders7);
            Set<String> cancelOrderIds = cancelOrders7.stream().map(UserOrder::getId).collect(Collectors.toSet());
            orders = orders.stream()
                    .filter(p -> !cancelOrderIds.contains(p.getId()))
                    .collect(Collectors.toList());
        }
        return orders;
    }

    /**
     * 过滤出180天之内的订单
     */
    private List<UserOrder> filterOrdersIn180Days(List<UserOrder> orders) {
        orders = orders.stream()
                .filter(p -> DateUtils.dayDiff(new Date(), new Date(p.getCreateDatetime().getTime())) < 180)
                .collect(Collectors.toList());
        return orders;
    }

    private List<UserOrder> filterOrderShowStatus(List<UserOrder> orders, ParentOrderShowStatus status) {
        if (null == status) {
            return orders;
        }

        switch (status) {
            case Paid:
                orders = orders.stream().filter(source -> source.getPaymentStatus() == PaymentStatus.Paid)
                        .collect(Collectors.toList());
                break;
            case UnPaid:
                orders = orders.stream().filter(source ->
                        source.getPaymentStatus() == PaymentStatus.Unpaid && source.getOrderStatus() == OrderStatus.New)
                        .collect(Collectors.toList());
                break;
            default:
                break;
        }
        return orders;
    }

    private List<UserOrder> filterOrderShowTypes(List<UserOrder> orders, ParentOrderShowTypes types) {
        if (null == types) {
            return orders;
        }

        switch (types) {
            case App:
                orders = orders.stream().filter(o -> o.getOrderType() == OrderType.app)
                        .collect(Collectors.toList());
                break;
            case Course:
                orders = orders.stream().filter(o -> o.getOrderType() == OrderType.micro_course)
                        .collect(Collectors.toList());
                break;
            case PicListen:
                orders = orders.stream().filter(o -> OrderProductServiceType.safeParse(o.getOrderProductServiceType()) == PicListen)
                        .collect(Collectors.toList());
                break;
            case PicListenBook:
                orders = orders.stream().filter(o -> OrderProductServiceType.safeParse(o.getOrderProductServiceType()) == PicListenBook)
                        .collect(Collectors.toList());
                break;
            default:
                break;
        }
        return orders;
    }

    private List<OrderShowMapper> getOrderListShowMapper(List<UserOrder> orders, List<UserOrderPaymentHistory> paymentHistories) {
        if (CollectionUtils.isEmpty(orders)) {
            return Collections.emptyList();
        }

        orders.sort((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime()));

        // 获取ICON
        List<FairylandProduct> fairylandProducts = fairylandLoaderClient.loadFairylandProducts(PARENT_APP, null);
        // 转换成showMappers
        List<OrderShowMapper> mapperList = new ArrayList<>();
        for (UserOrder order : orders) {
            mapperList.add(convertShowMapper(order, paymentHistories, fairylandProducts));
        }

        return mapperList;
    }

    @RequestMapping(value = "shoppinginfo/productlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage shoppingInfoProductList() {
        String tag = getRequestString("tag");
        String version = getRequestString("app_version");
        User parent = currentParent();
        Long parentId = currentUserId();
        Long childrenId = currentChildrenId();

        if (null == parentId || childrenId == null) {
            return MapMessage.errorMessage("请先去查看孩子最新作业情况吧~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(childrenId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("你选择的孩子不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        FairylandProductType fairylandProductType = FairylandProductType.of(tag);

        List<FairylandProduct> fairylandProducts = businessVendorServiceClient.getParentAvailableFairylandProducts(
                parent, studentDetail, PARENT_APP, fairylandProductType)
                .stream()
                .filter(p -> !Objects.equals(p.getAppKey(), OrderProductServiceType.FollowRead.name()))
                .collect(Collectors.toList());

        Map<String, VendorApps> vendorAppsMap = null;
        Map<String, AppPayMapper> appPaidStatus = null;
        //产品列表获取包含app类型，针对app类型，排除不在VendorApps里面的fairylandProduct，并获取使用中的人数
        if (CollectionUtils.isNotEmpty(fairylandProducts)) {
            List<String> availableServiceTypes = fairylandProducts.stream()
                    .filter((p) -> (APPS.name().equals(p.getProductType())))
                    .map(FairylandProduct::getAppKey)
                    .collect(Collectors.toList());
            vendorAppsMap = vendorLoaderClient.loadVendorAppsIncludeDisabled()
                    .values()
                    .stream()
                    .collect(Collectors.toMap(VendorApps::getAppKey, e -> e));
            //获取用户使用产品状态
            appPaidStatus = userOrderLoaderClient.getUserAppPaidStatus(availableServiceTypes, childrenId, false);
        }

        List<Map<String, Object>> products = new ArrayList<>();
        for (FairylandProduct fairylandProduct : fairylandProducts) {
            // 洛亚传说 三国 关付费 挂公告
            if (StringUtils.equals(fairylandProduct.getAppKey(), OrderProductServiceType.A17ZYSPG.name())) continue;
            if (StringUtils.equals(fairylandProduct.getAppKey(), OrderProductServiceType.SanguoDmz.name())) continue;

            //特殊账号屏蔽掉某些付费产品
            if (onlyShowPcVendorAppUser.contains(parentId)) {
                continue;
            }
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("appKey", fairylandProduct.getAppKey());
            appInfo.put("productName", fairylandProduct.getProductName());
            appInfo.put("productDesc", fairylandProduct.getProductDesc());
            appInfo.put("productIcon", fairylandProduct.getProductIcon());
            appInfo.put("productRectIcon", fairylandProduct.getProductRectIcon());
            appInfo.put("backgroundImage", fairylandProduct.getBackgroundImage());
            appInfo.put("operationMessage", fairylandProduct.getOperationMessage());
            appInfo.put("usePlatformDesc", fairylandProduct.getUsePlatformDesc());
            appInfo.put("rank", fairylandProduct.getRank());
            appInfo.put("catalogDesc", fairylandProduct.getCatalogDesc());
            appInfo.put("hotFlag", fairylandProduct.getHotFlag());
            appInfo.put("newFlag", fairylandProduct.getNewFlag());
            appInfo.put("recommendFlag", fairylandProduct.getRecommendFlag());

            //增加当前孩子是否有支付的订单 0未购买，１已经过期　2有正在使用的订单
            if (MapUtils.isNotEmpty(appPaidStatus) && appPaidStatus.containsKey(fairylandProduct.getAppKey())) {
                appInfo.put("status", appPaidStatus.get(fairylandProduct.getAppKey()).getAppStatus());
            }

            //家长端增加点击进入游戏功能
            if (APPS.name().equals(fairylandProduct.getProductType())
                    && MapUtils.isNotEmpty(vendorAppsMap)
                    && vendorAppsMap.containsKey(fairylandProduct.getAppKey())
                    && StringUtils.isNotEmpty(fairylandProduct.fetchRedirectUrl(RuntimeMode.current()))
                    && StringUtils.isNotEmpty(version)) {

                VendorApps vendorApps = vendorAppsMap.get(fairylandProduct.getAppKey());
                boolean useAppFlag = false;
                String apVersion = vendorApps.getAndroidParentVersion();
                String iosVersion = vendorApps.getIosParentVersion();
                if (getAppSystemType() == ANDROID && (StringUtils.isEmpty(apVersion) || VersionUtil.compareVersion(version, apVersion) >= 0)) {
                    useAppFlag = true;
                }
                if (getAppSystemType() == IOS && (StringUtils.isEmpty(iosVersion) || VersionUtil.compareVersion(version, iosVersion) >= 0)) {
                    useAppFlag = true;
                }
                if (useAppFlag) {
                    String url = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
                    appInfo.put("launchUrl", url);
                    appInfo.put("orientation", vendorApps.getOrientation());
                    appInfo.put("browser", vendorApps.getBrowser());
                }
            }
            products.add(appInfo);
        }
        return MapMessage.successMessage()
                .add("products", products)
                .add("isGraduate", studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz());
    }

    /**
     * 课外乐园 -- 列表页首页
     */
    @RequestMapping(value = "shoppinginfolist.vpage", method = RequestMethod.GET)
    public String shoppingInfoList(Model model) {
        setRouteParameter(model);
        Long studentId = getRequestLong("sid");
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        model.addAttribute("isGraduate", clazz != null && clazz.isTerminalClazz());

        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE));
        } else {
            model.addAttribute("result", MapMessage.successMessage());
        }
        return "parentmobile/fairylandList";

    }

    @RequestMapping(value = "shoppinginfo/availablestudentlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getStudentList() {
        User user = currentUser();
        try {
            OrderProductServiceType orderProductServiceType = OrderProductServiceType.safeParse(getRequestString("productType"));
            if (user == null || Unknown == orderProductServiceType) {
                return MapMessage.errorMessage("获取孩子列表错误").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            Set<Long> userIds = studentLoaderClient.loadParentStudents(user.getId()).stream().map(User::getId).collect(Collectors.toSet());
            Map<Long, StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(userIds);
            if (studentDetails.isEmpty()) {
                return MapMessage.successMessage().add("students", new LinkedList<>());
            }

            List<Map<String, Object>> studentDetailsMap = new ArrayList<>();
            for (StudentDetail studentDetail : studentDetails.values()) {
                if (userBlacklistServiceClient.isInBlackListByParent(user, studentDetail)) {
                    continue;
                }
                FairylandProduct fairylandProduct = businessVendorServiceClient.getParentAvailableFairylandProducts(user, studentDetail, PARENT_APP, APPS)
                        .stream()
                        .filter(p -> orderProductServiceType.name().equals(p.getAppKey()))
                        .findFirst()
                        .orElse(null);
                if (fairylandProduct != null) {
                    Map<String, Object> infoMap = new HashMap<>();
                    infoMap.put("id", studentDetail.getId());
                    infoMap.put("img", getUserAvatarImgUrl(studentDetail.getProfile().getImgUrl()));
                    infoMap.put("name", studentDetail.fetchRealname());
                    studentDetailsMap.add(infoMap);
                }
            }
            return MapMessage.successMessage().add("students", studentDetailsMap);
        } catch (Exception ex) {
            return MapMessage.errorMessage("获取孩子列表错误").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }

    /**
     * 推荐产品
     */
    @RequestMapping(value = "/recommend.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage recommend() {
        ParentCrosHeaderSupport.setCrosHeader(getWebRequestContext());
        Long studentId = getRequestLong("sid");
        String productIdStr = getRequestString("productId");
        if (StringUtils.isBlank(productIdStr)) {
            return MapMessage.errorMessage("参数错误");
        }
        if (null == currentUserId()) {
            return MapMessage.errorMessage("请登录后操作");
        }

        try {
            List<String> productIds = Arrays.asList(productIdStr.trim().split(","));
            Map<String, OrderProduct> orderProductMap = userOrderLoaderClient.loadOrderProducts(productIds);
            if (MapUtils.isEmpty(orderProductMap)) {
                return MapMessage.errorMessage("未知的产品");
            }
            Set<Long> userIdsToRecommend = new HashSet<>();
            for (OrderProduct product : orderProductMap.values()) {
                if (gonnaCreateOrderForParent(OrderProductServiceType.safeParse(product.getProductType()))) {
                    userIdsToRecommend.add(currentUserId());
                } else {
                    if (0 == studentId) {
                        return MapMessage.errorMessage("无效的学生id");
                    }
                    userIdsToRecommend.add(studentId);
                }
            }

            final Set<OrderProductServiceType> productTypes = orderProductMap.values().stream().map(o -> OrderProductServiceType.safeParse(o.getProductType())).collect(Collectors.toSet());
            List<OrderProduct> orderProducts = userOrderLoaderClient.loadAllOrderProductIncludeOffline();
            if (CollectionUtils.isEmpty(orderProducts)) {
                return MapMessage.successMessage();
            }
            List<String> productIdsOnline = orderProducts.stream()
                    .filter(p -> productTypes.contains(OrderProductServiceType.safeParse(p.getProductType())))
                    .filter(p -> p.getSalesType() == OrderProductSalesType.TIME_BASED)
                    .filter(p -> p.getStatus().equals("ONLINE"))
                    .map(OrderProduct::getId).collect(Collectors.toList());
            Map<String, List<OrderProductItem>> productItemsMap = userOrderLoaderClient.loadProductItemsByProductIds(productIdsOnline);
            if (MapUtils.isEmpty(productItemsMap)) {
                return MapMessage.successMessage();
            }
            //打包产品不做推荐 todo 这里的逻辑应该可以干掉了
            productIds = productIds.stream().filter(pid -> productItemsMap.containsKey(pid) && productItemsMap.get(pid).size() == 1).collect(Collectors.toList());
            if (productIds.size() == 0) {
                return MapMessage.successMessage();
            }
            //查出当前产品有关联的产品
            Set<String> productIdsRelated = getReleatedProductList(productIds, productItemsMap);
            //在关联产品中查出需要推荐的产品和不需要推荐的产品
            Set<String> productIdsToRecommend = getRecommendProductList(productIds, userIdsToRecommend, productItemsMap, productIdsRelated);

            Set<UserOrder> userOrders = new HashSet<>();
            for (Long uid : userIdsToRecommend) {
                List<UserOrder> orders = userOrderLoaderClient.loadUserOrderList(uid);
                if (CollectionUtils.isNotEmpty(orders)) {
                    userOrders.addAll(orders);
                }
            }
            Set<String> productIdsAlreadyBuy = userOrders.stream().map(UserOrder::getProductId).collect(Collectors.toSet());
            List<Map<String, Object>> ret = new ArrayList<>();
            for (String pid : productIdsRelated) {
                //如果产品不被推荐，并且用户没有购买过这个产品，则过滤掉
                if (!productIdsToRecommend.contains(pid) && !productIdsAlreadyBuy.contains(pid)) {
                    continue;
                }
                OrderProduct product = orderProducts.stream().filter(p -> p.getId().equals(pid)).findFirst().orElse(null);
                if (null == product) {
                    continue;
                }

                Map<String, Object> info = new HashMap<>();
                info.put("id", product.getId());
                info.put("name", product.getName());
                info.put("desc", product.getDesc());
                info.put("price", product.getPrice());
                info.put("originalPrice", product.getOriginalPrice());
                info.put("recommend", productIdsToRecommend.contains(pid));
                List<OrderProductItem> productItems = productItemsMap.get(pid);
                List<Map<String, Object>> items = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(productItems)) {
                    for (OrderProductItem item : productItems) {
                        Map<String, Object> itemInfo = new HashMap<>();
                        itemInfo.put("id", item.getId());
                        itemInfo.put("name", item.getName());
                        itemInfo.put("period", item.getPeriod());
                        if (OrderProductServiceType.safeParse(item.getProductType()) == PicListenBook && StringUtils.isNotBlank(item.getAppItemId())) {
                            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(item.getAppItemId());
                            if (null == newBookProfile) {
                                continue;
                            }
                            itemInfo.put("bookName", newBookProfile.getName());
                            itemInfo.put("bookImgUrl", StringUtils.isBlank(newBookProfile.getImgUrl()) ? "" : getCdnBaseUrlStaticSharedWithSep() + newBookProfile.getImgUrl());
                            itemInfo.put("publisher", newBookProfile.getShortPublisher());
                            TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(newBookProfile.getId());
                            if (null != sdkInfo) {
                                itemInfo.put("sdk", sdkInfo.getSdkType().name());
                            }
                        }
                        items.add(itemInfo);
                    }
                }
                info.put("items", items);
                ret.add(info);
            }
            return MapMessage.successMessage().add("products", ret);
        } catch (Exception ex) {
            logger.error("sid:{},productId:{},pid:{}", studentId, productIdStr, currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }


    private Set<String> getRecommendProductList(Collection<String> productIds, Collection<Long> userIdsToRecommend, Map<String, List<OrderProductItem>> productItemsMap, Set<String> productIdsRelated) {
        Set<String> activatedItemIds = new HashSet<>();
        Set<String> productIdsToRecommend = new HashSet<>();
        List<UserActivatedProduct> userActivatedProducts = new ArrayList<>();
        for (Long userId : userIdsToRecommend) {
            List<UserActivatedProduct> uaps = userOrderLoaderClient.loadUserActivatedProductList(userId);
            if (CollectionUtils.isNotEmpty(uaps)) {
                userActivatedProducts.addAll(uaps);
            }
        }
        if (CollectionUtils.isNotEmpty(userActivatedProducts)) {
            userActivatedProducts = userActivatedProducts.stream().filter(uap -> uap.getServiceEndTime().after(new Date())).collect(Collectors.toList());
            activatedItemIds = userActivatedProducts.stream().map(UserActivatedProduct::getProductItemId).collect(Collectors.toSet());
        }

        Map<String, OrderProductItem> orderProductItemMap = userOrderLoaderClient.loadOrderProductItems(activatedItemIds);
        Set<String> appItemIds = new HashSet<>();
        if (MapUtils.isNotEmpty(orderProductItemMap)) {
            appItemIds = orderProductItemMap.values().stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
        }
        for (String pid : productIdsRelated) {
            if (productIds.contains(pid)) continue;

            List<OrderProductItem> items = productItemsMap.get(pid);
            if (CollectionUtils.isEmpty(items)) continue;
            Set<String> ids = items.stream().map(OrderProductItem::getId).collect(Collectors.toSet());
            Set<String> appIds = items.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
            boolean recommend = true;
            for (String id : ids) {
                if (activatedItemIds.contains(id)) {
                    recommend = false;  //子产品还在服务期，则产品不被推荐
                    break;
                }
            }
            if (OrderProductServiceType.safeParse(items.get(0).getProductType()) == PicListenBook && recommend) {
                //用教材id过滤一下关联产品
                for (String id : appIds) {
                    if (appItemIds.contains(id)) {
                        recommend = false;
                        break;
                    }
                }
            }
            if (recommend) {
                productIdsToRecommend.add(pid);
            }
        }
        return productIdsToRecommend;
    }

    private Set<String> getReleatedProductList(Collection<String> productIds, Map<String, List<OrderProductItem>> productItemsMap) {
        Set<String> itemIds = new HashSet<>();
        Set<String> appItemIds = new HashSet<>();
        for (String productId : productIds) {
            if (!productItemsMap.containsKey(productId)) continue;
            itemIds.addAll(productItemsMap.get(productId).stream().map(OrderProductItem::getId).collect(Collectors.toSet()));
            appItemIds.addAll(productItemsMap.get(productId).stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet()));
        }

        Set<String> productIdsRelated = new HashSet<>();
        for (String pid : productItemsMap.keySet()) {
            if (productIds.contains(pid)) continue;

            List<OrderProductItem> items = productItemsMap.get(pid);
            if (CollectionUtils.isEmpty(items)) continue;
            List<String> ids = items.stream().map(OrderProductItem::getId).collect(Collectors.toList());
            for (String id : itemIds) {
                if (ids.contains(id)) { //如果item有重叠，则认为此产品与当前产品相关
                    productIdsRelated.add(pid);
                    break;
                }
            }
            if (OrderProductServiceType.safeParse(items.get(0).getProductType()) == PicListenBook && !productIdsRelated.contains(pid)) {
                //根据教材id再去找一下关联产品
                Set<String> appIds = items.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
                for (String id : appItemIds) {
                    if (appIds.contains(id)) {
                        productIdsRelated.add(pid);
                        break;
                    }
                }
            }
        }
        return productIdsRelated;
    }

    @RequestMapping(value = "/app_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage productInfo() {
        Long studentId = getRequestLong("sid");
        String productId = getRequestString("productId");
        String bookId = getRequestString("book_id");
        OrderProductServiceType orderProductServiceType = OrderProductServiceType.safeParse(getRequestString("productType"));
        if (orderProductServiceType == Unknown) {
            return MapMessage.errorMessage("参数错误");
        }
        //如果是点读机产品，则productId和bookId至少要传一个
        if (orderProductServiceType == PicListenBook && StringUtils.isBlank(productId) && StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            User student = null;
            if (0 != studentId) {
                student = raikouSystem.loadUser(studentId);
            }
            //如果订单要挂在孩子身上，则需要检查
            if (!gonnaCreateOrderForParent(orderProductServiceType)) {
                if (null == student) {
                    return MapMessage.errorMessage("学生帐号错误");
                }

                List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
                if (CollectionUtils.isEmpty(studentParentRefs)) {
                    return MapMessage.errorMessage("孩子与当前家长没有关联关系");
                }
                Set<Long> parentIds = studentParentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet());
                if (!parentIds.contains(currentUserId())) {
                    return MapMessage.errorMessage("孩子与当前家长没有关联关系");
                }
            }

            List<OrderProduct> products = getProductsToDisplay(productId, bookId, orderProductServiceType);
            if (CollectionUtils.isEmpty(products)) {
                return MapMessage.errorMessage("未查询到产品信息");
            }
            List<Map<String, Object>> ret = generateProductInfo(products, gonnaCreateOrderForParent(orderProductServiceType) ? currentUserId() : studentId);

            MapMessage message = MapMessage.successMessage().add("products", ret)
                    .add("studentAvatar", null == student ? "" : getUserAvatarImgUrl(student))
                    .add("studentName", null == student ? "" : student.fetchRealname());

            //如果有bookId，返回的时候带上学科，供前端根据不同的学科选择默认选中的点读机教材
            Subject subject = getBookSubject(bookId);
            if (null != subject) {
                message.add("subject", subject.name());
                //查出小U产品购买情况
                OrderProductServiceType improvedType = getAfentiImprovedTypeBySubject(subject);
                if (null != improvedType && 0 != studentId) {
                    StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                    if (null == studentDetail) {
                        return MapMessage.errorMessage("未查询到孩子信息");
                    }

                    //查出小U显示用的icon
                    List<FairylandProduct> parentAvailableFairylandProducts =
                            businessVendorServiceClient.getParentAvailableFairylandProducts(currentParent(), studentDetail, FairyLandPlatform.PARENT_APP, FairylandProductType.APPS);
                    OrderProductServiceType baseOrderProductServiceType = AfentiOrderUtil.getAfentiBaseOrderTypeByImprovedType(improvedType);
                    FairylandProduct fairylandProduct = parentAvailableFairylandProducts.stream().filter(p -> p.getAppKey().equals(baseOrderProductServiceType.name())).findFirst().orElse(null);
                    String icon = "";
                    if (null != fairylandProduct) {
                        icon = getCdnBaseUrlAvatarWithSep() + "gridfs/" + fairylandProduct.getProductIcon();
                    }

                    List<OrderProduct> improvedProducts = userOrderLoader.loadAllOrderProduct().stream().filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == improvedType).collect(Collectors.toList());
                    AppPayMapper userAppPaidStatus = userOrderLoader.getUserAppPaidStatus(improvedType.name(), studentId, true);
                    List<Map<String, Object>> improvedInfos = new ArrayList<>();
                    for (OrderProduct product : improvedProducts) {
                        Map<String, Object> info = new HashMap<>();
                        info.put("id", product.getId());
                        info.put("name", product.getName());
                        boolean paid = CollectionUtils.isNotEmpty(userAppPaidStatus.getValidProducts()) && userAppPaidStatus.getValidProducts().contains(product.getId());
                        info.put("hasPaid", paid);
                        info.put("price", product.getPrice());
                        info.put("originalPrice", product.getOriginalPrice());
                        if (StringUtils.isNotBlank(product.getAttributes())) {
                            Map<String, Object> attr = JsonUtils.fromJson(product.getAttributes());
                            if (MapUtils.isNotEmpty(attr) && attr.containsKey("startDate")) {
                                Date startDate = DateUtils.stringToDate(attr.get("startDate").toString(), "yyyy-MM-dd HH:mm:ss");
                                if (null != startDate) {
                                    info.put("startDate", DateUtils.dateToString(startDate, "yyyy.MM.dd"));
                                }
                            }
                            if (MapUtils.isNotEmpty(attr) && attr.containsKey("endDate")) {
                                if (attr.get("endDate").toString().compareTo("2018-08-31 23:59:59") > 0) {
                                    continue;   //只要春季版和暑假版
                                }
                                Date endDate = DateUtils.stringToDate(attr.get("endDate").toString(), "yyyy-MM-dd HH:mm:ss");

                                if (null != endDate) {
                                    info.put("endDate", DateUtils.dateToString(endDate, "yyyy.MM.dd"));
                                }
                            }
                        }
                        info.put("icon", icon);

                        improvedInfos.add(info);
                    }
                    message.add("improvedProducts", improvedInfos);
                }
            }
            return message;
        } catch (Exception ex) {
            logger.error("sid:{},productId:{},bookId:{},type:{},pid:{}", studentId, productId, bookId, orderProductServiceType.name(), currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }

    }

    private OrderProductServiceType getAfentiImprovedTypeBySubject(Subject subject) {
        switch (subject) {
            case ENGLISH:
                return AfentiExamImproved;
            case MATH:
                return AfentiMathImproved;
            case CHINESE:
                return AfentiChineseImproved;
            default:
                return null;
        }
    }

    private Subject getBookSubject(String bookId) {
        Subject subject = null;
        if (StringUtils.isNotBlank(bookId)) {
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
            if (null != newBookProfile) {
                subject = Subject.fromSubjectId(newBookProfile.getSubjectId());
            }
        }
        return subject;
    }

    private List<OrderProduct> getProductsToDisplay(String productId, String bookId, OrderProductServiceType orderProductServiceType) {
        List<OrderProduct> products = new ArrayList<>();
        OrderProduct product = null;
        if (StringUtils.isNotBlank(productId)) {
            product = userOrderLoaderClient.loadOrderProductById(productId);
        } else if (StringUtils.isNotBlank(bookId) && orderProductServiceType == PicListenBook) {
            product = findPicListenBookOrderProductByBookId(bookId);
        } else if (orderProductServiceType == FollowRead) {
            product = userOrderLoaderClient.loadAvailableProduct().stream().filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == orderProductServiceType).findFirst().orElse(null);
        } else if (orderProductServiceType == WalkerMan) {
            product = findWalkManBookOrderProductByBookId(bookId);
        }
        if (null != product) {
            products.add(product);
        } else {
            List<OrderProduct> pts = userOrderLoaderClient.loadAvailableProduct().stream()
                    .filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == orderProductServiceType)
                    .filter(p -> p.getSalesType() == OrderProductSalesType.TIME_BASED)
                    .collect(Collectors.toList());
            products.addAll(pts);
        }
        return products;
    }

    private List<Map<String, Object>> generateProductInfo(List<OrderProduct> orderProducts, Long userId) {
        List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(userId);

        List<Map<String, Object>> productInfo = new ArrayList<>();
        for (OrderProduct product : orderProducts) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", product.getId());
            info.put("name", product.getName());
            info.put("desc", product.getDesc());
            info.put("price", product.getPrice());
            info.put("originalPrice", product.getOriginalPrice());

            //补充productItem信息
            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
            List<Map<String, Object>> items = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(orderProductItems)) {
                for (OrderProductItem item : orderProductItems) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("id", item.getId());
                    itemInfo.put("period", item.getPeriod());
                    itemInfo.put("name", item.getName());
                    itemInfo.put("price", item.getOriginalPrice());
                    itemInfo.put("desc", item.getDesc());
                    itemInfo.put("appItemId", item.getAppItemId());
                    if (CollectionUtils.isEmpty(userActivatedProducts)) {
                        itemInfo.put("status", 0);   //未购买此子产品
                    } else {
                        UserActivatedProduct userActivatedProduct = userActivatedProducts.stream().filter(uap -> StringUtils.isNotBlank(uap.getProductItemId()) && uap.getProductItemId().equals(item.getId())).findFirst().orElse(null);
                        if (null == userActivatedProduct) {
                            itemInfo.put("status", 0);
                        } else if (userActivatedProduct.getServiceEndTime().after(new Date())) {
                            itemInfo.put("status", 1);   //已购买未过期
                            itemInfo.put("expire", userActivatedProduct.getServiceEndTime());
                        } else {
                            itemInfo.put("status", 2);   //已购买，已过期
                            itemInfo.put("expire", userActivatedProduct.getServiceEndTime());
                        }
                    }
                    if (OrderProductServiceType.safeParse(product.getProductType()) == PicListenBook || OrderProductServiceType.safeParse(product.getProductType()) == WalkerMan) {
                        //补充教材信息
                        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(item.getAppItemId());
                        if (null != newBookProfile) {
                            itemInfo.put("bookName", newBookProfile.getName());
                            itemInfo.put("bookImg", StringUtils.isBlank(newBookProfile.getImgUrl()) ? "" : getCdnBaseUrlStaticSharedWithSep() + newBookProfile.getImgUrl());
                        }
                    }
                    items.add(itemInfo);
                }
            }
            info.put("items", items);

            //是否买过这个产品
            List<UserOrder> userOrders = userOrderLoaderClient.loadUserPaidOrders(product.getProductType(), userId);
            boolean hasPaid = false;
            if (CollectionUtils.isNotEmpty(userOrders)) {
                long count = userOrders.stream().filter(order -> order.getProductId().equals(product.getId())).count();
                hasPaid = count > 0;
            }
            info.put("hasPaid", hasPaid);
            productInfo.add(info);
        }
        return productInfo;
    }

    /**
     * 课外乐园 -- 应用详情页
     */
    @RequestMapping(value = "shoppinginfo.vpage", method = RequestMethod.GET)
    public String buyList(Model model, HttpServletResponse response) {
        setRouteParameter(model);

        String version = getRequestString("app_version");
        OrderProductServiceType productType = OrderProductServiceType.safeParse(getRequestString("productType"));
        User parent = currentParent();
        Long studentId = getRequestLong("sid");
        String bookId = getRequestString("book_id"); //此参数可选
        String key = getRequestString("key");   //产品的唯一标识，可以定位到具体的一个产品配置，存储在产品配置的attr里
        String productId = getRequestString("productId");

        // 未来将shopinginfo转到/view/mobile/parent/learning_app/detail.vpage TODO 先拿点读机,阿分题开搞
        if (StringUtils.equals(productType.name(), "PicListenBook")
                || StringUtils.equals(productType.name(), "AfentiExam")
                || StringUtils.equals(productType.name(), "AfentiMath")
                || StringUtils.equals(productType.name(), "AfentiChinese")
        ) {
            return "redirect:/view/mobile/parent/learning_app/detail.vpage";
        }

        if (0 != studentId) {
            model.addAttribute("sid", studentId);  // 默认选中的学生
        }

        if (parent == null) {
            try {
                response.sendRedirect("https://wx.17zuoye.com/download/17parentapp?cid=202023");
            } catch (IOException e) {

            }
            model.addAttribute("result", MapMessage.errorMessage("请登录并绑定孩子的账号后再来继续使用").setErrorCode(RES_RESULT_BAD_REQUEST_CODE));
            return "parentmobile/shopDetail";
        }
        if (StringUtils.equals(productType.name(), OrderProductServiceType.Unknown.name())) {
            model.addAttribute("result", MapMessage.errorMessage("错误的产品类型").setErrorCode(RES_RESULT_BAD_REQUEST_CODE));
            return "parentmobile/shopDetail";
        }
        try {
            //挂在家长身上的订单不做与孩子相关的检查
            if (!gonnaCreateOrderForParent(productType)) {
                List<User> children = studentLoaderClient.loadParentStudents(parent.getId());
                if (CollectionUtils.isEmpty(children)) {
                    model.addAttribute("result", MapMessage.errorMessage("请先添加孩子后再来继续使用").setErrorCode(RES_RESULT_BAD_REQUEST_CODE));
                    return "parentmobile/shopDetail";
                }

                if (studentId != 0 && !studentIsParentChildren(parent.getId(), studentId)) {
                    model.addAttribute("result", MapMessage.errorMessage("此学生和家长无关联").setErrorCode(RES_RESULT_BAD_REQUEST_CODE));
                    return "parentmobile/shopDetail";
                }

                //没传sid  获取的是所有孩子列表，需要验证看是否存在有效可以购买的孩子
                if (studentId == 0) {
                    StudentDetail availableStu = studentLoaderClient.loadStudentDetails(children.stream().map(LongIdEntity::getId).collect(Collectors.toList()))
                            .values()
                            .stream()
                            .filter(p -> p.getClazz() != null && p.getClazz().isPrimaryClazz() && !p.getClazz().isTerminalClazz())
                            .findFirst()
                            .orElse(null);
                    if (availableStu == null) {
                        model.addAttribute("result", MapMessage.errorMessage("请先添加孩子后再来继续使用").setErrorCode(RES_RESULT_BAD_REQUEST_CODE));
                        return "parentmobile/shopDetail";
                    }
                }
            }

            //如果是点读机付费教材，需要传进来一个bookId
            //根据bookId去找对应的product信息，h5直接显示查到的product信息
            OrderProduct product = null;
            if (productType == OrderProductServiceType.PicListenBook) {
                if (StringUtils.isNotBlank(key)) {
                    product = findPicListenBookOrderProductByKey(key);
                } else if (StringUtils.isNotBlank(bookId)) {
                    product = findPicListenBookOrderProductByBookId(bookId);
                } else if (StringUtils.isNotBlank(productId)) {
                    product = userOrderLoaderClient.loadOrderProductById(productId);
                }
                if (null != product) {
                    TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(bookId);
                    if (null != sdkInfo) {
                        model.addAttribute(FIELD_SDK_NAME, sdkInfo.getSdkType().name());
                        model.addAttribute(FIELD_BOOK_ID, sdkInfo.getSdkBookIdV2(getRequestString(ApiConstants.REQ_APP_NATIVE_VERSION)));
                    }
                }
            }
            if (OrderProductServiceType.FollowRead == productType) {
                //点读跟读只有一个产品配置
                product = userOrderLoaderClient.loadAvailableProduct().stream().filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == productType).findFirst().orElse(null);
            }

            List<Map<String, Object>> infos;
            if (null != product) {
                infos = businessVendorServiceClient.getShoppingInfo(parent.getId(), product, getAppSystemType(), version);
            } else {
                infos = businessVendorServiceClient
                        .getShoppingInfo(studentId, parent.getId(), productType.name(), getAppSystemType(), version);
            }
            Student student = studentLoaderClient.loadStudent(studentId);
            FairylandProduct fairylandProduct = fairylandLoaderClient.loadFairylandProducts(FairyLandPlatform.PARENT_APP, null)
                    .stream().filter(p -> productType.name().equals(p.getAppKey()))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("result", MapMessage.successMessage());
            model.addAttribute("infos", infos);
            model.addAttribute("imgUrl", student != null ? getUserAvatarImgUrl(student.fetchImageUrl()) : null);
            if (fairylandProduct != null) {
                model.addAttribute("bannerImage", StringUtils.isBlank(fairylandProduct.getBannerImage()) ? "" : getUserAvatarImgUrl(fairylandProduct.getBannerImage()));
                model.addAttribute("descImage", StringUtils.isBlank(fairylandProduct.getDescImage()) ? "" : getUserAvatarImgUrl(fairylandProduct.getDescImage()));
                model.addAttribute("promptMessage", fairylandProduct.getPromptMessage());
            }

            //// FIXME: 2017/2/17 applePay UserCounts
            if (ApplePayParent.getParentIds().contains(parent.getId()) && productType == AfentiExam
                    && getAppSystemType() == IOS && VersionUtil.compareVersion(version, "1.9.5") >= 0) {
                model.addAttribute("applePay", true);
            }
        } catch (Exception ex) {
            logger.error("get shopping product detail failed. parentId:{}", parent.getId(), ex);
            model.addAttribute("result", MapMessage.errorMessage("查询购买信息失败").setErrorCode(RES_RESULT_BAD_REQUEST_CODE));
        }

        return "parentmobile/shopDetail";
    }

    //-----------------神秘分割线,哦耶,1.7.5开始有的,个人信息接口-------------------//

    //************家长开始******************

    /**
     * 是否显示重做按钮
     */
    @RequestMapping(value = "isShowRedo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage isShowRedo() {
        Long studentId = getRequestLong("sid");
        User parent = currentParent();

        boolean isShowRedo = false;
        try {
            if (null == studentId) {
                return MapMessage.errorMessage("invalid parameters").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
            }

            if (parent == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
            }
            if (!studentIsParentChildren(parent.getId(), studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            boolean isBlack = userBlacklistServiceClient.isInBlackListByParent(parent, studentDetail);
            if (!isBlack) {
                Integer level = studentDetail.getClazzLevelAsInteger();
                if (level != null && level >= 1 && level <= 6) {
                    List<OrderProduct> informations = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(studentDetail)
                            .stream()
                            .filter(a -> OrderProductServiceType.safeParse(a.getProductType()) == AfentiExam)
                            .collect(Collectors.toList());
                    for (OrderProduct information : informations) {
                        String grayTagName = information.getProductType() + RegionConstants.TAG_GRAY_REGION_SUFFIX;
                        if (RegionGrayUtils.checkRegionGrayStatus(studentDetail.getStudentSchoolRegionCode(),
                                grayTagName,
                                new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()))) {
                            isShowRedo = true;
                        }
                    }
                }
            }

            return MapMessage.successMessage().add("isShowRedo", isShowRedo);

        } catch (Exception ex) {
            logger.error("get shopping product failed. parentId:{}", parent.getId(), ex);
            return MapMessage.errorMessage("获取是否显示重做按钮失败").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }

    }

    /**
     * 获取当前家长的所有孩子信息
     */
    @RequestMapping(value = "getKids.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getKids() {
        Long parentId = currentUserId();
        Long studentId = getRequestLong("sid");
        if (parentId == null)
            return MapMessage.errorMessage("invalid parameters").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        if (studentId > 0 && !studentIsParentChildren(parentId, studentId)) {
            return MapMessage.errorMessage("此孩子和家长无关联").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail != null) {
                Map<String, Object> studentInfoMap = new HashMap<>();
                studentInfoMap.put(RES_STUDENT_ID, studentDetail.getId());
                studentInfoMap.put(RES_REAL_NAME, studentDetail.fetchRealname());
                studentInfoMap.put(RES_USER_IMG_URL, getUserAvatarImgUrl(studentDetail.fetchImageUrl()));
                resultList.add(studentInfoMap);

            } else {
                List<User> childList = studentLoaderClient.loadParentStudents(parentId);
                if (childList == null || childList.isEmpty())
                    return MapMessage.successMessage().add("childList", Collections.EMPTY_LIST);

                childList.stream().forEach(p -> {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put(ApiConstants.RES_STUDENT_ID, p.getId());
                    resultMap.put(ApiConstants.RES_REAL_NAME, p.fetchRealname());
                    resultMap.put(ApiConstants.RES_USER_IMG_URL, getUserAvatarImgUrl(p.fetchImageUrl()));
                    resultList.add(resultMap);
                });
            }

            return MapMessage.successMessage().add(ApiConstants.RES_USER_LIST, resultList);

        } catch (Exception ex) {
            logger.error("getKids. parentId:{}", parentId, ex);
            return MapMessage.errorMessage("get parent Kids fail").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }

    }

    /**
     * 获取当前家长信息
     */
    @RequestMapping(value = "/parent/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage parentInfo() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Map<String, Object> userInfoMap = new LinkedHashMap<>();
        userInfoMap.put("real_name", parent.fetchRealname());
        userInfoMap.put("avatar_url", getUserAvatarImgUrl(parent));

        String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(parent.getId());
        userInfoMap.put("mobile", authenticatedMobile);

        UserProfile profile = parent.getProfile();
        if (profile != null) {
            Integer year = profile.getYear();
            Integer month = profile.getMonth();
            Integer day = profile.getDay();
            if (year != null && month != null && day != null) {
                Map<String, Object> birthdayMap = new LinkedHashMap<>();
                birthdayMap.put("year", year);
                birthdayMap.put("month", month);
                birthdayMap.put("day", day);
                userInfoMap.put("birthday", birthdayMap);
            }
        }
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parent.getId());
        List<Long> childIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toList());
        Map<Long, Student> childMap = studentLoaderClient.loadStudents(childIds);
        List<String> identityList = new ArrayList<>();
        studentParentRefs.forEach(childRef -> {
            String callName = childRef.getCallName();
            Student student = childMap.get(childRef.getStudentId());
            if (StringUtils.isNotBlank(callName) && student != null)
                identityList.add(student.fetchRealname() + "的" + callName);
        });
        userInfoMap.put("identity_list", identityList);

        ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(parent.getId());
        if (parentExtAttribute != null) {
            if (StringUtils.isNotBlank(parentExtAttribute.getProfession()))
                userInfoMap.put("profession", parentExtAttribute.getProfession());
            else
                userInfoMap.put("profession", "");
            if (StringUtils.isNotBlank(parentExtAttribute.getEducationDegree()))
                userInfoMap.put("education", parentExtAttribute.getEducationDegree());
            else
                userInfoMap.put("education", "");

            List<ParentExtAttribute.Address> addressList = parentExtAttribute.getAddress();
            if (addressList != null) {
                ParentExtAttribute.Address defaultAddress = addressList.stream().filter(ParentExtAttribute.Address::getDefaultAddress).findFirst().orElse(null);
                if (defaultAddress != null) {
                    Integer countyCode = defaultAddress.getCountyCode();
                    ExRegion exRegion = raikouSystem.loadRegion(countyCode);
                    if (exRegion != null) {
                        String countyName = exRegion.getCountyName();
                        String cityName = exRegion.getCityName();
                        String provinceName = exRegion.getProvinceName();
                        userInfoMap.put("address", provinceName + cityName + countyName);
                    } else
                        userInfoMap.put("address", "");
                } else
                    userInfoMap.put("address", "");

            } else
                userInfoMap.put("address", "");
        } else {
            userInfoMap.put("profession", "");
            userInfoMap.put("education", "");
            userInfoMap.put("address", "");
        }
        List<UserWechatRef> wechatRefList = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(parent.getId()), WechatType.PARENT_APP).getOrDefault(parent.getId(), Collections.emptyList());
        userInfoMap.put("wechat_app_bind", CollectionUtils.isNotEmpty(wechatRefList));
        return MapMessage.successMessage().add("user_info", userInfoMap);
    }

    /**
     * 修改家长姓名
     */
    @RequestMapping(value = "/parent/name/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentNameUpdate() {
        // Feature #54929
//        if (ForbidModifyNameAndPortrait.check()) {
//            return ForbidModifyNameAndPortrait.errorMessage;
//        }
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        String name = getRequestString("name");
        if (StringUtils.isBlank(name))
            return MapMessage.errorMessage("name 必须");
        boolean validRealName = RealnameRule.isValidRealName(name);
        if (!validRealName)
            return MapMessage.errorMessage("请填写真实姓名");
        if (badWordCheckerClient.containsUserNameBadWord(name)) {
            return MapMessage.errorMessage("输入的姓名信息不合适哦\n有疑问请联系客服：\n400-160-1717");
        }

        return userServiceClient.changeName(parent.getId(), name);
    }

    /**
     * 获取 家长地址列表
     *
     * @deprecated 这个接口已经迁移到galaxy-webapp了
     */
    @RequestMapping(value = "/parent/address/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public MapMessage parentAddressList() {
        setCorsHeadersForParentOnlyForParent();
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        List<Map<String, Object>> addressMapList = new ArrayList<>();
        ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(parent.getId());
        Map<String, Object> defaultMap = null;
        if (parentExtAttribute != null) {
            List<ParentExtAttribute.Address> addressList = parentExtAttribute.getAddress();
            if (addressList != null) {
                for (ParentExtAttribute.Address address : addressList) {
                    Map<String, Object> addressMap = addressMap(address);
                    if (address != null) {
                        if (address.getDefaultAddress())
                            defaultMap = addressMap;
                        else
                            addressMapList.add(addressMap);
                    }
                }

            }
        }
        List<Map<String, Object>> sortedMapList = new ArrayList<>();
        if (defaultMap != null)
            sortedMapList.add(defaultMap);

        Collections.reverse(addressMapList);
        sortedMapList.addAll(addressMapList);
        return MapMessage.successMessage().add("address_list", sortedMapList).add("could_add", sortedMapList.size() < 3);
    }

    /**
     * 获取 家长地址列表
     * @deprecated 这个接口已经迁移到了galaxy-webapp
     */
    @Deprecated
    @RequestMapping(value = "/parent/address/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage parentAddressDetail() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        String id = getRequestString("id");
        if (StringUtils.isBlank(id))
            return MapMessage.errorMessage("地址id必需");
        ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(parent.getId());
        if (parentExtAttribute != null) {
            List<ParentExtAttribute.Address> addressList = parentExtAttribute.getAddress();
            if (addressList != null) {
                ParentExtAttribute.Address address = addressList.stream().filter(t -> StringUtils.equals(t.getAddressFlag(), id)).findFirst().orElse(null);
                if (address == null)
                    return MapMessage.errorMessage("没有此地址");
                Map<String, Object> map = addressMap(address);
                if (map != null) {
                    MapMessage success = MapMessage.successMessage();
                    success.putAll(map);
                    return success;
                }
            }
        }
        return MapMessage.errorMessage("没有此地址");
    }

    /**
     * 更新 新增地址
     * @deprecated 此接口已经迁移到galaxy-webapp
     */
    @RequestMapping(value = "/parent/address/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public MapMessage parentAddressUpdate() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        String id = getRequestString("id");
        String name = getRequestString("consignee_name");
        String phone = getRequestString("phone_number");
        Integer countyCode = getRequestInt("county_code");
        String addressDetail = getRequestString("address_detail");
        Boolean isDefault = getRequestBool("is_default");
        if (StringUtils.isBlank(name) || StringUtils.isBlank(phone) || StringUtils.isBlank(addressDetail) || countyCode == 0 || isDefault == null)
            return MapMessage.errorMessage("请填写完整");
        if (addressDetail.length() < 5)
            return MapMessage.errorMessage("详细地址少于5个字");
        if (addressDetail.length() > 60)
            return MapMessage.errorMessage("详细地址最多60个字");
        ParentExtAttribute.Address address = new ParentExtAttribute.Address(name, phone, countyCode, addressDetail, isDefault);
        if (StringUtils.isNotBlank(id)) {
            address.setAddressFlag(id);
            parentServiceClient.updateParentExtAttributeAddress(parent.getId(), address);
        } else
            parentServiceClient.addParentExtAttributeAddress(parent.getId(), address);

        return MapMessage.successMessage();
    }

    /**
     * 删除地址
     * @deprecated 此接口已经迁移到galaxy-webapp
     */
    @RequestMapping(value = "/parent/address/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public MapMessage parentAddressDelete() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        String id = getRequestString("id");
        if (StringUtils.isBlank(id))
            return MapMessage.errorMessage("id 必需");
        return parentServiceClient.deleteParentExtAttributeAddress(parent.getId(), id);
    }

    /**
     * 更新生日
     */
    @RequestMapping(value = "/parent/birthday/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentBirthdayUpdate() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Integer year = getRequestInt("year");
        Integer month = getRequestInt("month");
        Integer day = getRequestInt("day");
        if (year == 0 || month == 0 || day == 0)
            return MapMessage.errorMessage("参数错误");

        return userServiceClient.changeUserBirthday(parent.getId(), year, month, day);
    }

    /**
     * 更新职业
     */
    @RequestMapping(value = "/parent/profession/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentUpdateProfession() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        String profession = getRequestString("profession");
        if (StringUtils.isBlank(profession))
            return MapMessage.errorMessage("参数错误");

        ParentExtAttribute parentExtAttribute = new ParentExtAttribute(parent.getId());
        parentExtAttribute.setProfession(profession);

        return parentServiceClient.updateParentExtAttributeFields(parent.getId(), parentExtAttribute);
    }

    //************家长结束******************

    //************学生开始******************

    /**
     * 更新职业
     */
    @RequestMapping(value = "/parent/education/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentUpdateEducation() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        String education = getRequestString("education");
        if (StringUtils.isBlank(education))
            return MapMessage.errorMessage("参数错误");

        ParentExtAttribute parentExtAttribute = new ParentExtAttribute(parent.getId());
        parentExtAttribute.setEducationDegree(education);

        return parentServiceClient.updateParentExtAttributeFields(parent.getId(), parentExtAttribute);
    }

    private Map<String, Object> addressMap(ParentExtAttribute.Address address) {
        Integer countyCode = address.getCountyCode();
        ExRegion exRegion = raikouSystem.loadRegion(countyCode);
        if (exRegion == null)
            return null;

        String countyName = exRegion.getCountyName();
        String cityName = exRegion.getCityName();
        String provinceName = exRegion.getProvinceName();
        Map<String, Object> addressMap = new LinkedHashMap<>();
        addressMap.put("area", provinceName + " " + cityName + " " + countyName);
        addressMap.put("id", address.getAddressFlag());
        addressMap.put("consignee_name", address.getConsigneeName());
        addressMap.put("phone_number", address.getPhoneNumber());
        addressMap.put("address_detail", address.getAddressDetail());
        addressMap.put("is_default", address.getDefaultAddress());
        addressMap.put("county_code", address.getCountyCode());
        addressMap.put("province_code", exRegion.getProvinceCode());
        addressMap.put("city_code", exRegion.getCityCode());
        return addressMap;
    }

    /**
     * 更新职业
     *
     * @deprecated 这个接口已经迁移到galaxy-webapp了
     */
    @Deprecated
    @RequestMapping(value = "/child/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage childInfo() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return noLoginResult;
        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
        if (student == null)
            return noLoginResult;
        Map<String, Object> userInfoMap = new LinkedHashMap<>();
        userInfoMap.put("real_name", student.fetchRealname());
        userInfoMap.put("sid", student.getId());
        userInfoMap.put("avatar_url", getUserAvatarImgUrl(student));
        String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(student.getId());
        if (StringUtils.isNotBlank(authenticatedMobile)) {
            userInfoMap.put("mobile", authenticatedMobile);
        }

        userInfoMap.put("use_day_count", DateUtils.dayDiff(new Date(), student.getCreateTime()));
        Boolean isChannelCStudent;
        //B端孩子（就是有平台班级的孩子）
        if (student.getClazz() != null) {
            isChannelCStudent = false;
            userInfoMap.put("school_clazz", student.getStudentSchoolName() + student.getClazz().formalizeClazzName());
            userInfoMap.put("grade_c", student.getClazz().getClazzLevel().getDescription());
        } else {
            ChannelCUserAttribute channelCUserAttribute = studentLoaderClient.loadStudentChannelCAttribute(studentId);
            if (channelCUserAttribute != null) {
                if (channelCUserAttribute.getJoinClazzTime() != null) {
                    isChannelCStudent = false;
                    userInfoMap.put("school_clazz", "");
                } else {
                    isChannelCStudent = true;
                    String schoolName = channelCUserAttribute.getSchoolName();
                    if (channelCUserAttribute.getSchoolId() != null && channelCUserAttribute.getSchoolId() != 0) {
                        School school = schoolLoaderClient.getSchoolLoader()
                                .loadSchool(channelCUserAttribute.getSchoolId())
                                .getUninterruptibly();
                        if (school != null)
                            schoolName = school.getCname();
                    }
                    userInfoMap.put("school_name_c", schoolName);
                    ChannelCUserAttribute.ClazzCLevel clazzCLevelByClazzJie = ChannelCUserAttribute.getClazzCLevelByClazzJie(channelCUserAttribute.getClazzJie());
                    if (clazzCLevelByClazzJie == null)
                        userInfoMap.put("grade_c", "毕业班");
                    else
                        userInfoMap.put("grade_c", clazzCLevelByClazzJie.getDescription());
                }
            } else
                isChannelCStudent = false;
        }
        userInfoMap.put("is_channal_c_student", isChannelCStudent);

        UserProfile profile = student.getProfile();
        if (profile != null) {
            userInfoMap.put("gender", profile.getGender());
            if (profile.getYear() != null) {
                Map<String, Object> birthdayMap = new LinkedHashMap<>();
                birthdayMap.put("year", profile.getYear());
                birthdayMap.put("month", profile.getMonth());
                birthdayMap.put("day", profile.getDay());
                userInfoMap.put("birthday", birthdayMap);
            }
        } else {
            userInfoMap.put("gender", "");
            userInfoMap.put("birthday", new Object());
        }

        StudentExtAttribute extAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        if (extAttribute != null) {
            List<String> interestSpecialityList = extAttribute.getInterestSpeciality();
            if (CollectionUtils.isNotEmpty(interestSpecialityList)) {
                userInfoMap.put("interest_list", interestSpecialityList);
            } else
                userInfoMap.put("interest_list", new ArrayList<>());
            List<StudentExtAttribute.AwardsHonor> awardsHonor = extAttribute.getAwardsHonor();
            if (CollectionUtils.isNotEmpty(awardsHonor)) {
                userInfoMap.put("glory", awardsHonor.size() + "个荣誉");
            } else
                userInfoMap.put("glory", "");
        }

        UserActivationHomeLevel userHomeLevel = userLevelLoader.getUserHomeLevel(studentId);
        if (null != userHomeLevel) {
            userInfoMap.put("homeLevel", userHomeLevel.getLevel());
        }
        return MapMessage.successMessage().add("child_info", userInfoMap);
    }

    /**
     * 兴趣特长列表
     */
    @RequestMapping(value = "/child/interest/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage childInterestList() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return noLoginResult;
        StudentExtAttribute extAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        if (extAttribute != null) {
            List<String> interestSpeciality = extAttribute.getInterestSpeciality();
            if (CollectionUtils.isNotEmpty(interestSpeciality)) {
                return MapMessage.successMessage().add("interest_list", interestSpeciality);
            }
        }
        return MapMessage.successMessage().add("interest_list", Collections.emptyList());

    }

    /**
     * 修改性别
     *
     * @deprecated 这个接口已经迁移到galaxy-webapp了
     */
    @Deprecated
    @RequestMapping(value = "/child/gender/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateChildGender() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return noLoginResult;
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null)
            return noLoginResult;
        UserProfile profile = student.getProfile();
        Gender gender = Gender.fromCode(profile.getGender());
        if (Gender.NOT_SURE != gender)
            return MapMessage.errorMessage("性别无法修改哦!");
        gender = Gender.fromCode(getRequestString("gender"));
        if (gender == Gender.NOT_SURE)
            return MapMessage.errorMessage("请选择性别");
        return userServiceClient.changeGender(studentId, gender.getCode());
    }

    /**
     * 修改C端学生学校
     */
    @RequestMapping(value = "/child/school/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateChildCSchool() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return noLoginResult;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return noLoginResult;

        if (studentDetail.getClazz() != null)
            return MapMessage.errorMessage("B端孩子禁止修改学校");
        ChannelCUserAttribute channelCUserAttribute = studentLoaderClient.loadStudentChannelCAttribute(studentId);
        if (channelCUserAttribute != null && channelCUserAttribute.getJoinClazzTime() != null) {
            return MapMessage.errorMessage("B端孩子禁止修改学校");
        }
        Long schoolId = getRequestLong("school_id");
        String schoolName = getRequestString("school_name");
        Integer regionCode = getRequestInt("region_code");
        if (schoolId == 0 && StringUtils.isBlank(schoolName) && regionCode == 0)
            return MapMessage.errorMessage("参数错误");
        return studentServiceClient.updateChannelCStudentSchoolIdOrName(studentId, schoolId, schoolName, regionCode);
    }

    /**
     * 修改C端学生年级
     */
    @RequestMapping(value = "/child/grade/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateChildCGrade() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return noLoginResult;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return noLoginResult;

        if (studentDetail.getClazz() != null)
            return MapMessage.errorMessage("B端孩子禁止修改年级");
        ChannelCUserAttribute channelCUserAttribute = studentLoaderClient.loadStudentChannelCAttribute(studentId);
        if (channelCUserAttribute != null && channelCUserAttribute.getJoinClazzTime() != null) {
            return MapMessage.errorMessage("B端孩子禁止修改年级");
        }
        Integer grade = getRequestInt("grade", -1);
        if (grade == -1)
            return MapMessage.errorMessage("参数错误");

        if (grade != 0) {
            ClazzLevel level = ClazzLevel.parse(grade);
            if (level == null || level.getLevel() > 9) {
                return MapMessage.errorMessage(RES_RESULT_CLAZZ_LEVEL_ERROR).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }
        }
        MapMessage mapMessage = studentServiceClient.updateChannelCStudentClazzLevel(studentId, grade);
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage("更新学生年级失败");
        }
        return MapMessage.successMessage();
    }

    /**
     * 更新生日
     */
    @RequestMapping(value = "/child/birthday/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage childBirthdayUpdate() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return noLoginResult;
        Integer year = getRequestInt("year");
        Integer month = getRequestInt("month");
        Integer day = getRequestInt("day");
        if (year == 0 || month == 0 || day == 0)
            return MapMessage.errorMessage("参数错误");
        return userServiceClient.changeUserBirthday(studentId, year, month, day);
    }

    /**
     * 更新兴趣
     */
    @RequestMapping(value = "/child/interest/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage childInterestUpdate() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return noLoginResult;
        String interestStr = getRequestString("interest");
        if (StringUtils.isBlank(interestStr))
            return MapMessage.errorMessage("请选择一个兴趣特长");
        String[] interestArray = interestStr.split(",");
        if (interestArray.length == 0)
            return MapMessage.errorMessage("请选择一个兴趣特长");
        List<String> interestList = Arrays.asList(interestArray);
        StudentExtAttribute
                studentExtAttribute = new StudentExtAttribute(studentId);
        studentExtAttribute.setInterestSpeciality(interestList);
        return studentServiceClient.updateStudentExtAttributeFields(studentId, studentExtAttribute);
    }

    @RequestMapping(value = "/child/glory/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage childGloryList() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return noLoginResult;
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        if (studentExtAttribute == null || CollectionUtils.isEmpty(studentExtAttribute.getAwardsHonor()))
            return MapMessage.successMessage().add("glory_list", Collections.emptyList()).add("could_add", true);
        List<Map<String, Object>> gloryMapList = new ArrayList<>();
        studentExtAttribute.getAwardsHonor().stream().sorted(new StudentExtAttribute.AwardsHonorComparator()).forEach(awardsHonor -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", awardsHonor.getAwardsHonorFlag());
            map.put("year", awardsHonor.getYear());
            if (awardsHonor.getMonth() != null)
                map.put("month", awardsHonor.getMonth());
            map.put("content", awardsHonor.getAwardName());
            gloryMapList.add(map);
        });


        return MapMessage.successMessage().add("glory_list", gloryMapList).add("could_add", gloryMapList.size() < maxGlory);
    }


    @RequestMapping(value = "/child/glory/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage childGloryAddOrUpdate() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return noLoginResult;
        String id = getRequestString("id");
        Integer year = getRequestInt("year");
        Integer month = getRequestInt("month");
        String content = getRequestString("content");

        if (year == 0)
            return MapMessage.errorMessage("获奖时间年份必填");
        if (content.length() < 2)
            return MapMessage.errorMessage("奖励荣誉名称少于2个字");
        if (content.length() > 60)
            return MapMessage.errorMessage("奖励荣誉名称最多60个字");
        StudentExtAttribute.AwardsHonor awardsHonor = new StudentExtAttribute.AwardsHonor(year, month, content);
        if (StringUtils.isNotBlank(id)) {
            awardsHonor.setAwardsHonorFlag(id);
            return studentServiceClient.updateStudentExtAttributeAwardsHonor(studentId, awardsHonor);
        } else {
            StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
            if (studentExtAttribute != null && CollectionUtils.isNotEmpty(studentExtAttribute.getAwardsHonor())) {
                if (studentExtAttribute.getAwardsHonor().size() >= maxGlory)
                    return MapMessage.errorMessage("你已经有足够多奖励荣誉啦");
            }
            return studentServiceClient.addStudentExtAttributeAwardsHonor(studentId, awardsHonor);
        }
    }

    @RequestMapping(value = "/child/glory/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage childGloryDelete() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return noLoginResult;
        String id = getRequestString("id");
        if (StringUtils.isBlank(id))
            return MapMessage.errorMessage("ID必需");
        return studentServiceClient.deleteStudentExtAttributeAwardsHonor(studentId, id);
    }

    @RequestMapping(value = "/child/name/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage childNameUpdate() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        Long studentId = getRequestLong("sid");
        String userName = getRequestString("user_name");
        if (studentId == 0) {
            return noLoginResult;
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail.getClazz() != null) {
            return MapMessage.errorMessage("B端孩子禁止修改姓名");
        }
        ChannelCUserAttribute channelCUserAttribute = studentLoaderClient.loadStudentChannelCAttribute(studentId);
        if (channelCUserAttribute != null && channelCUserAttribute.getJoinClazzTime() != null) {
            return MapMessage.errorMessage("B端孩子禁止修改姓名");
        }

        MapMessage message = userServiceClient.changeName(studentId, userName);
        if (message.isSuccess()) {
            Map<String, Object> info = new HashMap<>();
            info.put("studentId", studentId);
            info.put("name", userName);

            changeNameMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(info)));
        }
        return message;
    }

    /**
     * 家长通优惠劵列表
     */
    @RequestMapping(value = "/usercouponlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userCouponList() {
        User user = currentParent();
        if (user == null) return noLoginResult;
        CouponUserStatus couponUserStatus = CouponUserStatus.safeParse(getRequestString("status"));
        List<CouponShowMapper> userCoupons = couponLoaderClient.loadUserCoupons(user.getId())
                .stream()
                .filter(p -> p.getCouponUserStatus() == couponUserStatus)
                .collect(Collectors.toList());
        if (couponUserStatus == CouponUserStatus.NotUsed) {
            userCoupons = userCoupons.stream().sorted((p, q) -> q.getCreateDate().compareTo(p.getCreateDate()))
                    .collect(Collectors.toList());
        } else if (couponUserStatus == CouponUserStatus.Expired) {
            userCoupons = userCoupons.stream().sorted((p, q) -> q.getEffectiveEndTime().compareTo(p.getEffectiveEndTime()))
                    .collect(Collectors.toList());
        } else if (couponUserStatus == CouponUserStatus.Used) {
            userCoupons = userCoupons.stream().sorted((p, q) -> q.getUpdateDate().compareTo(p.getUpdateDate()))
                    .collect(Collectors.toList());
        }
        // 获取appKey
        List<CouponShowMapper> mappers = userOrderLoaderClient.loadCouponUsableAppKeys(userCoupons);
        MapMessage message = MapMessage.successMessage().set("userCoupons", mappers);

        return message;
    }

    @RequestMapping(value = "/coupon/activate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage activateCoupon() {
        String couponId = getRequestString("couponId");
        Long studentId = getRequestLong("sid");
        if (StringUtils.isBlank(couponId) || 0 == studentId) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            List<CouponShowMapper> couponShowMappers = couponLoaderClient.loadUserCoupons(currentUserId());
            if (CollectionUtils.isEmpty(couponShowMappers)) {
                return MapMessage.errorMessage("未查询到有效的优惠券");
            }
            CouponShowMapper couponShowMapper = couponShowMappers.stream().filter(mapper -> mapper.getCouponId().equals(couponId)).findFirst().orElse(null);
            if (null == couponShowMapper) {
                return MapMessage.errorMessage("未查询到要使用的优惠券");
            }
            if (couponShowMapper.getCouponType() != CouponType.Trial) {
                return MapMessage.errorMessage("不支持此类型优惠券激活");
            }

            MapMessage ret = userOrderServiceClient.getUserOrderService().useTrialCoupon(couponShowMapper.getCouponUserRefId(), studentId);
            if (ret.isSuccess()) {
                return MapMessage.successMessage();
            } else {
                return ret;
            }
        } catch (Exception ex) {
            log.error("couponId:{}", couponId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private Long currentChildrenId() {
        long studentId = getRequestLong("sid");
        if (studentId == 0) {
            studentId = Long.parseLong(getCookieManager().getCookie("sid", "0"));
        }
        if (studentId == 0) {
            return null;
        }
        return studentId;
    }


    /**
     * 根据点读机教材id查找对应的产品配置
     *
     * @param bookId 点读机教材id
     * @return 产品配置 {@link OrderProduct}
     */
    private OrderProduct findPicListenBookOrderProductByBookId(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return null;
        }

        Map<String, List<OrderProduct>> orderProductMap = userOrderLoaderClient.loadOrderProductByAppItemIds(Collections.singletonList(bookId));
        if (MapUtils.isNotEmpty(orderProductMap)) {
            List<OrderProduct> products = orderProductMap.get(bookId);
            if (CollectionUtils.isNotEmpty(products)) {
                return products.stream()
                        .filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == PicListenBook)
                        .filter(p -> {
                            //用book_id来找产品，只能找有1个item的
                            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(p.getId());
                            return CollectionUtils.isNotEmpty(orderProductItems) && orderProductItems.size() == 1;
                        })
                        .findFirst().orElse(null);
            }
        }
        return null;
    }


    /**
     * 根据随声听教材id查找对应的产品配置
     */
    private OrderProduct findWalkManBookOrderProductByBookId(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return null;
        }

        Map<String, List<OrderProduct>> orderProductMap = userOrderLoaderClient.loadOrderProductByAppItemIds(Collections.singletonList(bookId));
        if (MapUtils.isNotEmpty(orderProductMap)) {
            List<OrderProduct> products = orderProductMap.get(bookId);
            if (CollectionUtils.isNotEmpty(products)) {
                return products.stream()
                        .filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == WalkerMan)
                        .filter(p -> {
                            //用book_id来找产品，只能找有1个item的
                            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(p.getId());
                            return CollectionUtils.isNotEmpty(orderProductItems) && orderProductItems.size() == 1;
                        })
                        .findFirst().orElse(null);
            }
        }
        return null;
    }

    private OrderProduct findPicListenBookOrderProductByKey(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        List<OrderProduct> orderProducts = userOrderLoader.loadAllOrderProduct();
        if (CollectionUtils.isEmpty(orderProducts)) {
            return null;
        }

        for (OrderProduct orderProduct : orderProducts) {
            if (StringUtils.isBlank(orderProduct.getAttributes())) continue;
            Map<String, Object> attr = JsonUtils.fromJson(orderProduct.getAttributes());
            if (attr.containsKey(FIELD_PICLISTEN_PACKAGE_ID) && key.equals(attr.get(FIELD_PICLISTEN_PACKAGE_ID))) {
                return orderProduct;
            }
        }
        return null;
    }

    /**
     * 学习成长tab跳转标志，为true表示跳转到C端H5页面，否则跳转到“自学乐园”H5页面
     */
    @RequestMapping(value = "/tab/grow_up/change.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage growUpChange() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        boolean channelCFlag = true;
        User parent = currentParent();
        MapMessage result = MapMessage.successMessage();
        try {
            if (parent != null) {
                if (studentId != 0L) {
                    StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                    //没有孩子或者没有班级去C端页面
                    channelCFlag = studentDetail == null || studentDetail.getClazz() == null;
                }
            }
            return result.add("channelC", channelCFlag).add("displayUcourse", false);
        } catch (Exception e) {
            log.error("grow up link status error. parentId:{}", e);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "/remind/clean.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage remindClean() {
        ParentCrosHeaderSupport.setCrosHeader(getWebRequestContext());
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        ReminderPosition reminderPosition = ReminderPosition.of(getRequestString(REQ_REMIND_POSITION));
        if (reminderPosition != null) {
            reminderService.clearUserReminder(parent.getId(), reminderPosition);
        }
        return MapMessage.successMessage();
    }

    /**
     * 更新用户头像。可能是家长、学生
     */
    @RequestMapping(value = "update_user_avatar.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateUserAvatar() {
        Long userId = getRequestLong(REQ_USER_ID);
        String fileName = getRequestString(REQ_USER_IMG_URL);
        String gfsId = getRequestString(REQ_AVATAR_GFS_ID);
        if (StringUtils.isBlank(fileName) || StringUtils.isBlank(gfsId)) {
            return MapMessage.errorMessage("头像信息不能为空");
        }
        User user = raikouSystem.loadUser(userId);
        if (user == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR);
        }
        MapMessage message = onlyUpdateUserAvatar(user, fileName, gfsId);
        if (!message.isSuccess()) {
            return message;
        }
        return MapMessage.successMessage();
    }

}
