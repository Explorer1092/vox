/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.equator.common.api.constants.EquatorPalaceMuseumConstants;
import com.voxlearning.equator.common.api.constants.EquatorPromoteProductsConstants;
import com.voxlearning.equator.service.rubik.api.client.PalaceMuseumServiceClient;
import com.voxlearning.equator.service.rubik.api.client.PromoteParentProductsServiceClient;
import com.voxlearning.galaxy.service.groupon.api.GrouponOrderService;
import com.voxlearning.galaxy.service.groupon.api.constant.lightcourse.LightCourseActivity;
import com.voxlearning.galaxy.service.groupon.api.constant.lightcourse.LightCourseConfig;
import com.voxlearning.galaxy.service.partner.api.constant.ThirdPartyType;
import com.voxlearning.galaxy.service.partner.api.service.ThirdPartyService;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.galaxy.service.tobbit.api.support.TobbitProductSupport;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.order.api.constants.OrderReferType;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.*;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.api.service.DPUserOrderService;
import com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil;
import com.voxlearning.utopia.service.order.consumer.OrderShippingAddressServiceClient;
import com.voxlearning.utopia.service.parent.api.PalaceMuseumLoader;
import com.voxlearning.utopia.service.parent.api.PalaceMuseumService;
import com.voxlearning.utopia.service.parent.api.StudyTogetherJoinGroupV2Service;
import com.voxlearning.utopia.service.parent.api.UserOrderAddressService;
import com.voxlearning.utopia.service.parent.api.cache.ParentCache;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.common.UserOrderAddress;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.PalaceMuseumUserPurchaseData;
import com.voxlearning.utopia.service.parent.api.support.PalaceMuseumProductSupport;
import com.voxlearning.utopia.service.parent.constant.NewYearGiftConstant;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.DPParentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.support.ParentCrosHeaderSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil.isAfentiCommonOrder;
import static com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil.isAfentiImpovedOrder;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_BAD_REQUEST_CODE;

/**
 * @author Hailong Yang
 * @since 15/09/14
 */
@Controller
@RequestMapping(value = "/parentMobile/order")
@Slf4j
public class MobileParentOrderController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @AlpsQueueProducer(queue = "galaxy.mission.invite.register.queue", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer missionInviteProducer;
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @ImportService(interfaceClass = ThirdPartyService.class)
    private ThirdPartyService thirdPartyService;

    @ImportService(interfaceClass = StudyTogetherJoinGroupV2Service.class)
    private StudyTogetherJoinGroupV2Service studyTogetherJoinGroupV2Service;

    @ImportService(interfaceClass = DPUserOrderService.class)
    private DPUserOrderService dpUserOrderService;

    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    @ImportService(interfaceClass = UserOrderAddressService.class)
    private UserOrderAddressService userOrderAddressService;

    @ImportService(interfaceClass = GrouponOrderService.class)
    private GrouponOrderService grouponOrderService;

    @Inject
    private PromoteParentProductsServiceClient promoteParentProductsServiceClient;
    @Inject
    private PalaceMuseumServiceClient palaceMuseumServiceClient;

    @ImportService(interfaceClass = PalaceMuseumLoader.class)
    private PalaceMuseumLoader palaceMuseumLoader;

    @ImportService(interfaceClass = PalaceMuseumService.class)
    private PalaceMuseumService palaceMuseumService;
    @Inject
    private DPParentLoaderClient dpParentLoaderClient;
    @Inject
    private OrderShippingAddressServiceClient orderShippingAddressServiceClient;

    private StudyLesson getStudyLesson(String lessonId) {
        return studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(lessonId));
    }

    /**
     * 查看指定订单               *
     *
     * @return
     */
    @RequestMapping(value = "loadorder.vpage", method = RequestMethod.GET)
    public String loadOrder(Model model) {
        String orderId = getRequestString("oid");
        setRouteParameter(model);
        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
            return "parentmobile/person/orderDetail";
        }
        if (null == orderId) {
            model.addAttribute("result", MapMessage.errorMessage("无效的参数").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "parentmobile/person/orderDetail";
        }
        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        if (null == order || null == order.getUserId()) {
            model.addAttribute("result", MapMessage.errorMessage("无效的订单号").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "parentmobile/person/orderDetail";
        }
        model.addAttribute("result", MapMessage.successMessage().add("order", order));
        return "parentmobile/person/orderDetail";
    }

    // view order_detail 调用，商品详情页点击立即购买获取订单展示信息
    @RequestMapping(value = "loadorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadOrder() {
        String orderId = getRequestString("oid");
        if (null == orderId) {
            return MapMessage.errorMessage("无效的参数").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        if (currentParent() == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        // cancel的单子允许继续支付
        UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
        if (null == order || null == order.getUserId()) {
            return MapMessage.errorMessage("无效的订单号").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        User user = raikouSystem.loadUser(order.getUserId());
        String paymentResultUrl = getGalaxySiteUrl() + "/api/v3/order/payment/result.vpage";
        String icon = "";
        if (VersionUtil.compareVersion(getAppVersion(), "2.3.5") >= 0) {
            List<FairylandProduct> fairylandProducts = fairylandLoaderClient.getFairylandLoader().loadFairylandProducts(FairyLandPlatform.PARENT_APP, null);
            if (CollectionUtils.isNotEmpty(fairylandProducts)) {
                FairylandProduct fairylandProduct = fairylandProducts.stream().filter(p -> p.getAppKey().equals(order.getOrderProductServiceType())).findFirst().orElse(null);
                if (null != fairylandProduct) {
                    icon = fairylandProduct.getProductIcon();
                }
            }
        }
        Long sid = getRequestLong("studentId");
        StudentDetail sd = studentLoaderClient.loadStudentDetail(sid);
        boolean isOpenH5payment = true;
        if (sd != null) {
            isOpenH5payment = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(sd, "Order", "H5Pay");
        }

        return MapMessage.successMessage()
                .add("orderId", order.genUserOrderId())
                .add("productName", order.getProductName())
                .add("amount", order.getOrderPrice())
                .add("use", user.getUserType() == UserType.PARENT.getType() ? "不限" : user.fetchRealname())
                .add("icon", icon)
                .add("payment_result_url", paymentResultUrl)
                .add("isOpenH5payment", isOpenH5payment);
    }

    @RequestMapping(value = "loadOrderPayWayList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadOrderPayWayList() {
        List payWayList = new LinkedList();
        if (currentParent() == null) {
            return MapMessage.errorMessage("请登录家长号");
        }
        String orderId = getRequestString("oid");
        if (Objects.isNull(orderId)) {
            return MapMessage.errorMessage("参数异常");
        }

        UserOrder userOrder = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
        if (Objects.isNull(userOrder)) {
            return MapMessage.errorMessage("参数异常");
        }
        List<String> productIds = new LinkedList<>();
        List<UserOrderProductRef> orderProductRefList = userOrderLoaderClient.loadOrderProducts(userOrder.getUserId(), userOrder.getId());
        if (CollectionUtils.isNotEmpty(orderProductRefList)) {
            productIds = orderProductRefList.stream().map(UserOrderProductRef::getProductId).collect(Collectors.toList());
        } else {
            productIds.add(userOrder.getProductId());
        }
        Collection<OrderProduct> orderProductList = userOrderLoaderClient.loadOrderProducts(productIds).values();
        //判断该订单下是否有实物商品
        Boolean actualGoods = false;
        if (CollectionUtils.isNotEmpty(orderProductList)) {
            for (OrderProduct orderProduct : orderProductList) {
                if (Objects.nonNull(orderProduct) && SafeConverter.toBoolean(orderProduct.getActualGoods())) {
                    actualGoods = true;
                    break;
                }
            }
        }

        String appKey = getRequestString("app_key");
        String appVersion = getAppVersion();
        if (isIOSRequest(getRequest())) {
            if (VersionUtil.compareVersion(appVersion, "2.6.2") >= 0 && !actualGoods) {
                //版本大于等于2.6.2，非实物，只有学贝支付
                Map<String, Object> voxPay = new LinkedHashMap<>();
                voxPay.put("way", "学贝支付");
                voxPay.put("className", "xb");
                voxPay.put("type", "recharge");
                voxPay.put("type_index", 3);
                payWayList.add(voxPay);
            } else {
                //版本小于2.6.2或者是实物商品，走H5的支付宝支付和微信支付
                Map<String, Object> wechatPay = new LinkedHashMap<>();
                wechatPay.put("way", "微信支付");
                wechatPay.put("className", "wx");
                wechatPay.put("type", Objects.equals(appKey, "17parent") ? "wechatpay_h5_parentapp" : "wechatpay_h5_studentapp");
                wechatPay.put("type_index", 6);
                payWayList.add(wechatPay);

                Map<String, Object> aliPay = new LinkedHashMap<>();
                aliPay.put("way", "支付宝支付");
                aliPay.put("className", "zfb");
                aliPay.put("type", Objects.equals(appKey, "17parent") ? "alipay_wap_parentapp" : "alipay_wap_studentapp");
                aliPay.put("type_index", 5);
                payWayList.add(aliPay);
            }
        } else {
            //android,不包含学贝支付，走原生的微信和支付宝支付
            Map<String, Object> wechatPay = new LinkedHashMap<>();
            wechatPay.put("way", "微信支付");
            wechatPay.put("className", "wx");
            wechatPay.put("type", Objects.equals(appKey, "17parent") ? "wechatpay_parent" : "wechatpay_studentapp");
            wechatPay.put("type_index", 1);
            payWayList.add(wechatPay);

            Map<String, Object> aliPay = new LinkedHashMap<>();
            aliPay.put("way", "支付宝支付");
            aliPay.put("className", "zfb");
            aliPay.put("type", Objects.equals(appKey, "17parent") ? "alipay_parentapp" : "alipay_studentapp");
            aliPay.put("type_index", 2);
            payWayList.add(aliPay);
        }
        return MapMessage.successMessage().add("payWayList", payWayList).add("actualGoods", actualGoods);
    }

    private String getGalaxySiteUrl() {
        String domain = ProductConfig.get("galaxy.domain");
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "https://" + domain;
        }
        return domain;
    }

    /**
     * 创建订单         *
     */
    @RequestMapping(value = "createorder.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage createOrder() {
        ParentCrosHeaderSupport.setCrosHeader(getWebRequestContext());
        Long studentId = getRequestLong("sid");
        String productId = getRequestString("productId");
        String picListenBookProductId = getRequestString("picProductId");
        String refer = getRequestParameter("refer", OrderReferType.PARENT_APP_SHOPDETAIL_DEFAULT.type);
        User parent = currentParent();
        String channel = getRequestString("channel");
        Long inviter = getRequestLong("inviter");//故宫立春产品运营使用
        String bookId = getRequestString("bookId"); //点读机Id
        if (null == parent) {
            //fixme 暂时加个code666，一起学下单未登录需要返回666去登录。
            return MapMessage.errorMessage("请登录后购买").setErrorCode("666");
        }

        try {
            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
            if (null == orderProduct) {
                return MapMessage.errorMessage("未查询到产品信息");
            }

            String studyTogetherLessonId = null;
            OrderProductServiceType productType = OrderProductServiceType.safeParse(orderProduct.getProductType());
            if (productType == OrderProductServiceType.StudyMates && !Objects.equals(NewYearGiftConstant.STUDY_TOGETHER_GIFT_PID, orderProduct.getId())
                    && !Objects.equals(NewYearGiftConstant.STUDY_TOGETHER_LESSONS_PRODUCT_ID, orderProduct.getId())
                    && !Objects.equals(NewYearGiftConstant.STUDY_TOGETHER_LESSIONS_PRODUCT_ID_1, orderProduct.getId())
                    && !Objects.equals(NewYearGiftConstant.STUDY_TOGETHER_LESSONS_GIFT_PID, orderProduct.getId())) {
                List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId());
                if (CollectionUtils.isEmpty(orderProductItems)) {
                    return MapMessage.errorMessage("商品数量错误！");
                }
                String lessonId = orderProductItems.get(0).getAppItemId();
                if(StringUtils.isNotBlank(lessonId) && lessonId.contains("_")){
                    String[] split = lessonId.split("_");
                    lessonId = split[1];
                }
                studyTogetherLessonId = lessonId;
                MapMessage mapMessage = studyTogetherServiceClient.checkParentJoinLimit(parent.getId(), lessonId);
                if (!mapMessage.isSuccess()) {
                    return mapMessage;
                }
                StudyLesson studyLesson = getStudyLesson(lessonId);
                if (studyLesson == null) {
                    return MapMessage.errorMessage("课程错误");
                }
                if (studyLesson.safeGetJoinWay() == 3) {
//                    String twoMemberProductId = studyLesson.secondProductId();
                    ////如果这个产品是2个人的拼团，则需要看是否有资格下单
//                    if (orderProduct.getId().equals(twoMemberProductId)) {
//                        ParentJoinGroup parentJoinGroup = studyTogetherJoinGroupV2Service.loadParentJoinGroup(lessonId, parent.getId());
//                        if (parentJoinGroup == null || (!parentJoinGroup.isExpire(studyLesson) && parentJoinGroup.memberCount() != 1)){
//                            return MapMessage.errorMessage("你的参团状态不符合购买此商品的条件");
//                    }
                }
            }

            if (validateRepeatCreate(orderProduct, parent.getId())) {
                return MapMessage.errorMessage("您已经购买此产品或者购买了相关产品，请勿重复购买");
            }

            MapMessage message;
            if (gonnaCreateOrderForParent(OrderProductServiceType.safeParse(orderProduct.getProductType())) || ThirdPartyType.getOrderProductServiceTypes().contains(productType.name())) {
                if (!availableForRenew(orderProduct, parent.getId())) {
                    return MapMessage.errorMessage("下单失败，当前产品（或当前产品的部分内容）仍在服务期，不支持续费");
                }
                if (productType == OrderProductServiceType.PalaceMuseum && !PalaceMuseumProductSupport.GIFT_PRODUCT_ID.equals(productId) && studentId == 0L) {
                    return MapMessage.errorMessage("故宫课程下单失败！学生ID不能为空");
                }
                message = createUserOrderForParent(orderProduct,
                        refer,
                        OrderProductServiceType.safeParse(orderProduct.getProductType()) == OrderProductServiceType.ChipsEnglish ? OrderType.chips_english : OrderType.pic_listen,
                        channel, studyTogetherLessonId, studentId, inviter);
            } else {
                if (AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(orderProduct.getProductType())) && StringUtils.isNotBlank(picListenBookProductId)) {
                    //是小U产品下单，并且带着点读机
                    OrderProduct picListenBookProduct = userOrderLoader.loadOrderProductById(picListenBookProductId);
                    if (null == picListenBookProduct) {
                        return MapMessage.errorMessage("未查询到点读机产品信息");
                    }
                    message = userOrderService.createAfentiOrder(studentId, orderProduct.getProductType(), productId, refer, picListenBookProductId, parent.getId(), channel);
                } else {
                    message = createUserOrderForStudent(studentId, orderProduct, bookId, refer, parent, channel);
                }
            }

            if (message.isSuccess() && StringUtils.isNotBlank(getRequestString("rel")) && getRequestString("rel").equals("fengchao")) {
                String orderId = message.get("orderId").toString();
                ParentExtAttribute parentExtAttribute = dpParentLoaderClient.loadParentExtAttribute(currentUserId());
                if (null != parentExtAttribute) {
                    List<ParentExtAttribute.Address> addrs = parentExtAttribute.getAddress();
                    if (CollectionUtils.isNotEmpty(addrs)) {
                        ParentExtAttribute.Address address = addrs.stream().filter(ParentExtAttribute.Address::getDefaultAddress).findFirst().orElse(null);
                        if (null != address) {
                            String activity = getRequestString("activity");
                            orderShippingAddressServiceClient.upsertOrderShippingInfo(orderId, currentUserId(), address.getConsigneeName(), address.getPhoneNumber(), address.getAddressDetail(), null, address.getCountyCode(), activity);
                        }
                    }
                }
            }

            //家长任务处理:邀请任务开始登记
            if (message.isSuccess() && StringUtils.isNotBlank(getRequestString("msinviter")) && StringUtils.isNotBlank(getRequestString("msidy"))) {
                Long missionInviter = SafeConverter.toLong(getRequestString("msinviter"));
                String missionIdentification = getRequestString("msidy");
                Map<String, Object> info = new HashMap<>();
                info.put("inviter", missionInviter);
                info.put("invitee", currentUserId());
                info.put("identification", missionIdentification);
                missionInviteProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(info)));
            }
            return message;
        } catch (Exception ex) {
            log.error("create order failed.studentId:{},productId:{},parentId:{}", studentId, productId, currentUserId(), ex);
            return MapMessage.errorMessage("系统错误");
        }
    }

    private boolean availableForRenew(OrderProduct orderProduct, Long userId) {
        if (OrderProductServiceType.safeParse(orderProduct.getProductType()) == OrderProductServiceType.PicListenBook || OrderProductServiceType.safeParse(orderProduct.getProductType()) == OrderProductServiceType.WalkerMan) {
            List<OrderProductItem> productItems = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId());
            if (CollectionUtils.isEmpty(productItems)) {
                return false;
            }
            List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(userId);
            if (CollectionUtils.isEmpty(userActivatedProducts)) {
                return true;
            }
            Set<String> productItemIds = productItems.stream().map(OrderProductItem::getId).collect(Collectors.toSet());
            final Date currentDay = new Date();
            long count = userActivatedProducts.stream()
                    .filter(uap -> productItemIds.contains(uap.getProductItemId()))
                    .filter(uap -> uap.getServiceEndTime().after(currentDay))
                    .count();
            return count == 0;
        }
        return true;
    }

    //校验薯条英语重复购买的情况
    private boolean validateRepeatCreate(OrderProduct orderProduct, Long userId) {
        if (OrderProductServiceType.safeParse(orderProduct.getProductType()) == OrderProductServiceType.ChipsEnglish) {
            List<UserOrder> userOrderList = userOrderLoaderClient.loadUserPaidOrders(orderProduct.getProductType(), userId);
            if (CollectionUtils.isEmpty(userOrderList)) {
                return false;
            }
            AppPayMapper appPayMapper = userOrderLoaderClient.getUserAppPaidStatus(orderProduct.getProductType(), userId, true);
            List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId());
            //检查购买情况
            if (appPayMapper != null && CollectionUtils.isNotEmpty(orderProductItemList) &&
                    CollectionUtils.isNotEmpty(appPayMapper.getValidItems()) && orderProductItemList
                    .stream()
                    .filter(e -> appPayMapper.getValidItems().contains(e.getId()))
                    .findFirst().orElse(null) != null) {
                return true;
            }
        }
        return false;
    }

    private MapMessage createUserOrderForParent(OrderProduct product, String refer, OrderType orderType, String channel, String studyTogetherLessonId, long sid, Long inviter) {
        Long parentId = currentUserId();
        String productId = product.getId();
        MapMessage message = createStudyTogetherGiftOrderForParent(product, parentId, refer, channel, orderType);
        if (message.isSuccess()) {
            return message;
        }

        message = createStudyTogetherOrderForParent(product, studyTogetherLessonId, parentId, sid, refer, channel, orderType);
        if (message.isSuccess()) {
            return message;
        }

        //立春0元指定refer
        if (StringUtils.equals(PalaceMuseumProductSupport.BEGIN_SPRING_PRODUCT_ID, productId)) {
            String orderRefer = palaceMuseumLoader.getUserZeroOrderRefer(parentId);
            if (StringUtils.isNotBlank(orderRefer)) {
                refer = orderRefer;
            }
        }
        //买故宫全年减去立春的逻辑
        if (StringUtils.equals(PalaceMuseumProductSupport.ALL_PRODUCT_ID, productId)) {
            List<PalaceMuseumUserPurchaseData> palaceMuseumUserPurchaseData = palaceMuseumLoader.getUserPurchaseDataByStudentId(sid);
            PalaceMuseumUserPurchaseData purchaseData = palaceMuseumUserPurchaseData.stream().filter(e -> StringUtils.equals(PalaceMuseumProductSupport.BEGIN_SPRING_PRODUCT_ID, e.getProductId())).findFirst().orElse(null);
            if (purchaseData != null) {
                OrderProduct beginSpringProduct = userOrderLoaderClient.loadOrderProductById(PalaceMuseumProductSupport.BEGIN_SPRING_PRODUCT_ID);
                message = userOrderService.createAppOrderByReduceAmount(parentId, product.getProductType(), Collections.singletonList(productId), null, refer, channel, orderType.name(), beginSpringProduct.getPrice());
            } else {
                message = userOrderService.createAppOrder(parentId, product.getProductType(), Collections.singletonList(productId), null, refer, channel, orderType.name(), null);
            }
        } else {
            message = userOrderService.createAppOrder(parentId, product.getProductType(), Collections.singletonList(productId), null, refer, channel, orderType.name(), null);
            if (message.isSuccess()) {
                StudyLesson studyLesson = getStudyLesson(studyTogetherLessonId);
                if(studyLesson!=null && studyLesson.joinGroupon() && !productId.equals(studyLesson.getProductId())){
                    String orderId = message.get("orderId").toString();
                    //训练营课程拼团，下单后回调
                    grouponOrderCallback(productId, parentId, orderId, sid, refer, channel,studyLesson);
                }
            }
        }
        if (message.isSuccess()) {
            String orderId = message.get("orderId").toString();
            boolean studyTogetherOrder = product.getProductType().equals(OrderProductServiceType.StudyMates.name());
            boolean palaceMuseumOrder = product.getProductType().equals(OrderProductServiceType.PalaceMuseum.name());
            if ((studyTogetherOrder || palaceMuseumOrder) && sid != 0) {
                studyTogetherServiceClient.getStudyTogetherHulkService().studyTogetherJoinActiveOrderSID(orderId, sid);
            }
            if (palaceMuseumOrder) {
                if (PalaceMuseumProductSupport.ALL_PRODUCT_ID.equals(productId) || PalaceMuseumProductSupport.ALL_CUT_PRODUCT_ID.equals(productId) || PalaceMuseumProductSupport.PICBOOK_PRODUCT_ID.equals(productId)) {
                    userOrderAddressService.createOrderRecordAddress(parentId, orderId, UserOrderAddress.AddressType.PalaceMuseum);
                }

                //故宫立春产品运营活动时,下单后的回调
                try {
                    String springBeginProductId = RuntimeMode.current().le(Mode.TEST) ? EquatorPalaceMuseumConstants.springBegin_test_productId : EquatorPalaceMuseumConstants.springBegin_product_productId;
                    if (StringUtils.equals(productId, springBeginProductId)) {
                        palaceMuseumServiceClient.getRemoteReference().orderCallBack(inviter, parentId, orderId);
                    }
                } catch (Exception ex) {
                    logger.error("order call back fail ,ex={}", ex);
                }
                //全年级轻课拼团活动，下单后回调
                grouponOrderCallback(productId, parentId, orderId, sid, refer, channel, null);

                //平台的0元立春,下单成功之后要把orderRefer清掉
                if (StringUtils.equals(PalaceMuseumProductSupport.BEGIN_SPRING_PRODUCT_ID, productId)) {
                    palaceMuseumService.delUserOrderRefer(parentId);
                }
            }

            if (palaceMuseumOrder || studyTogetherOrder) {
                //家长通29元多产品0元推广活动--下单后的回调
                try {
                    Set<String> tempProductIds = new HashSet<>();
                    for (EquatorPromoteProductsConstants.ProductType productType : EquatorPromoteProductsConstants.ProductType.values()) {
                        tempProductIds.addAll(productType.productInfos.stream().map(EquatorPromoteProductsConstants.ProductInfo::fetchProductId).collect(Collectors.toSet()));
                    }

                    if (tempProductIds.contains(productId)) {
                        Long marketId = getRequestLong("marketId", 0);
                        Integer layer = getRequestInt("layer", 0);
                        long lastInviter = getRequestLong("lastInviter", 0);//inviter的上家
                        long initSource = getRequestLong("initSource", 0);//最初发起人来源
                        promoteParentProductsServiceClient.getRemoteReference().placeAnOrder(parentId, orderId, productId, inviter, marketId, layer, lastInviter, initSource);
                    }
                } catch (Exception ex) {
                    logger.error("order call back fail ,ex={}", ex);
                }
            }

            return MapMessage.successMessage().add("orderId", orderId);
        } else {
            return MapMessage.errorMessage(StringUtils.defaultIfBlank(message.getInfo(), "生成订单失败"))
                    .setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    private void grouponOrderCallback(String productId, Long parentId,String orderId, Long sid, String refer,
                                      String channel, StudyLesson studyLesson){
        try {
            String grouponId = getRequestString("grouponId");
            String grouponGroupId = getRequestString("grouponGroupId");
            LightCourseActivity lightCourseActivity = LightCourseActivity.getLightCourseActivity(grouponId);
            LightCourseConfig lightCourseConfig = null;
            if(studyLesson == null && lightCourseActivity != null){
                lightCourseConfig = lightCourseActivity.getCourseConfig();
            }
            if(studyLesson != null){
                grouponOrderService.lcagOrderCreated(parentId, orderId, productId, studyLesson.getGrouponId(), grouponGroupId, sid, refer, channel);
                return;
            }

            if (lightCourseConfig != null && StringUtils.equals(grouponId, lightCourseConfig.fetchGrouponId())) {
                grouponOrderService.lcagOrderCreated(parentId, orderId, productId, grouponId, grouponGroupId, sid, refer, channel);
            }
        } catch (Exception ex) {
            logger.error(" groupon order call back fail ,ex={}", ex);
        }
    }

    private MapMessage createStudyTogetherGiftOrderForParent(OrderProduct product, Long parentId, String refer, String channel, OrderType orderType) {
        if (Objects.equals(NewYearGiftConstant.STUDY_TOGETHER_GIFT_PID, product.getId())) {
            //获取用户是否分享过
            boolean isShared = SafeConverter.toBoolean(ParentCache.getParentPersistenceCache().get(NewYearGiftConstant.STUDY_TOGETHER_GIFT + currentUserId()).getValue());
            double rate = 1.0;
            if (isShared) {
                rate = SafeConverter.toDouble(commonConfigServiceClient.getCommonConfigBuffer()
                        .loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "STUDY_TOGETHER_GIFT_RATE"), 1.0);
            }
            MapMessage message = dpUserOrderService.createAppOrderByDiscount(parentId, OrderProductServiceType.StudyMates.name(),
                    Collections.singletonList(product.getId()), null, refer, channel, orderType.name(), new BigDecimal(rate));
            if (message.isSuccess()) {
                String orderId = SafeConverter.toString(message.get("orderId"));
                userOrderAddressService.createOrderRecordAddress(parentId, orderId, UserOrderAddress.AddressType.Gift59Package);
            }
            return message;
        }

        return MapMessage.errorMessage();
    }

    private MapMessage createStudyTogetherOrderForParent(OrderProduct product, String studyTogetherLessonId, Long parentId, Long sid, String refer, String channel, OrderType orderType) {
        boolean studyTogetherOrder = product.getProductType().equals(OrderProductServiceType.StudyMates.name());
        if (studyTogetherOrder) {
            if (studyTogetherLessonId != null) {
                StudyLesson studyLesson = getStudyLesson(studyTogetherLessonId);
                if (studyLesson != null && studyLesson.safeGetJoinWay() == 9) {
                    Boolean shared = studyTogetherServiceClient.getStudyTogetherShareDiscountService().parentSkuIsShared(parentId, studyTogetherLessonId);
                    if (shared) {
                        MapMessage mapMessage = dpUserOrderService.createAppOrderByDiscount(parentId, OrderProductServiceType.StudyMates.name(),
                                Collections.singletonList(product.getId()), null, refer, channel, orderType.name(), new BigDecimal("0.8"));
                        if (mapMessage.isSuccess()) {
                            String orderId = SafeConverter.toString(mapMessage.get("orderId"));
                            if (sid != 0) {
                                studyTogetherServiceClient.getStudyTogetherHulkService().studyTogetherJoinActiveOrderSID(orderId, sid);
                            }
                        }
                        return mapMessage;
                    }
                }
            }
        }
        return MapMessage.errorMessage();
    }

    private MapMessage createUserOrderForStudent(Long studentId, OrderProduct product, String bookId, String refer, User parent, String channel) {
        if (studentId <= 0 || null == product || StringUtils.isBlank(refer)) {
            return MapMessage.errorMessage("无效的参数").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }

        if (!studentIsParentChildren(currentUserId(), studentId)) {
            return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage(ApiConstants.RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        if (studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz()) {
            return MapMessage.errorMessage("本产品不提供毕业班购买").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }

        //没有订单的黑名单用户不让创建订单
        boolean isBlack = userBlacklistServiceClient.isInUserBlackList(parent);
        if (isBlack) {
            Date startDate = MonthRange.current().getStartDate();
            List<UserOrder> orders = userOrderLoaderClient.loadUserOrderList(studentId)
                    .stream()
                    .filter(t -> t.getOrderType() == OrderType.app)
                    .filter(t -> t.getPaymentStatus() == PaymentStatus.Paid)
                    .filter(t -> t.getUpdateDatetime() != null && t.getUpdateDatetime().after(startDate))
                    .collect(Collectors.toList());
            List<UserOrder> payTypeOrders = orders.stream()
                    .filter(p -> Objects.equals(p.getOrderProductServiceType(), product.getProductType())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(payTypeOrders)) {
                return MapMessage.errorMessage("您所在的地区暂时不能使用，敬请期待。").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
            }
        }

        // 是否已经购买超过455天的产品
        MapMessage checkMsg = userOrderServiceClient
                .checkDaysToExpireForCreateNewOrder(studentId, OrderProductServiceType.safeParse(product.getProductType()));
        if (!checkMsg.isSuccess()) {
            return MapMessage.errorMessage("您已经开通了 " + product.getName() + "，可以直接学习使用哦！");
        }

        String picListenProductId = null;
        if (AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(product.getProductType()))) {
            String client_type = getRequestString("client_type");
            Boolean parentAuth = picListenCommonService.userIsAuthForPicListen(currentParent());

            Subject subject = null;
            if (OrderProductServiceType.safeParse(product.getProductType()) == OrderProductServiceType.AfentiExamImproved) {
                subject = Subject.ENGLISH;
            } else if (OrderProductServiceType.safeParse(product.getProductType()) == OrderProductServiceType.AfentiMathImproved) {
                subject = Subject.MATH;
            } else if (OrderProductServiceType.safeParse(product.getProductType()) == OrderProductServiceType.AfentiChineseImproved) {
                subject = Subject.CHINESE;
            }
            //带上教材id，根据教材找商品
            if (StringUtils.isNotBlank(bookId)) {
                if (textBookManagementLoaderClient.picListenShow(bookId, client_type, parentAuth)) {
                    picListenProductId = calculatePicListenProductId(bookId);
                }
            } else {
                //找到用户默认教材，
                List<String> studentDefaultSubjectBook = parentSelfStudyPublicHelper.getStudentDefaultSubjectBook(studentDetail, client_type, parentAuth, null, subject);
                if (CollectionUtils.isNotEmpty(studentDefaultSubjectBook)) {
                    //拿到bookId，去查对应的产品是否已经购买过（包括打包产品和非打包产品）
                    bookId = studentDefaultSubjectBook.get(0);
                    picListenProductId = calculatePicListenProductId(bookId);
                }
            }

        }

        if (isAfentiCommonOrder(OrderProductServiceType.safeParse(product.getProductType())) || isAfentiImpovedOrder(OrderProductServiceType.safeParse(product.getProductType()))) {
            return userOrderService.createAfentiOrder(studentId, product.getProductType(), product.getId(), refer, picListenProductId, currentUserId(), channel);
        } else {

            /**tobbit 分享后下单处理*/
            if(TobbitProductSupport.isTobbit(product.getId())){

                AppPayMapper paidStatus = userOrderLoader.getUserAppPaidStatus(OrderProductServiceType.Synchronousclassroom.name(), studentId);
                /**首单零元购买*/
                if(paidStatus == null || (paidStatus!=null && paidStatus.getAppStatus() <= 0)){
                    return userOrderService.createAppOrderByReduceAmount(studentId,product.getProductType(),Collections.singletonList(product.getId()),null,refer,channel,OrderType.app.name(),product.getPrice());
                }

                /**内容分享标识 目前只用于tobbit*/
                String activityShare = getRequestString("activityShare");

                /**分享立减6元*/
                if(TobbitProductSupport.TOBBIT_SHARE.equals(activityShare)){
                    return userOrderService.createAppOrderByReduceAmount(studentId,product.getProductType(),Collections.singletonList(product.getId()),null,refer,channel,OrderType.app.name(),BigDecimal.valueOf(TobbitProductSupport.REDUCEMONEY));
                }

            }

            return userOrderService.createAppOrder(studentId, product.getProductType(), Collections.singletonList(product.getId()), null, refer, channel, OrderType.app.name(), null);
        }
    }

    // 获取教材对应的商品
    private String calculatePicListenProductId(String bookId) {
        String picListenProductId = null;

        Map<String, List<OrderProduct>> productMap = userOrderLoaderClient.loadOrderProductByAppItemIds(Collections.singleton(bookId));
        if (MapUtils.isNotEmpty(productMap)) {
            List<OrderProduct> orderProducts = productMap.get(bookId);
            if (CollectionUtils.isNotEmpty(orderProducts)) {
                for (OrderProduct p : orderProducts) {
                    //只取点读机的商品
                    if (!p.getProductType().equals(OrderProductServiceType.PicListenBook.name())) {
                        continue;
                    }
                    List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(p.getId());
                    if (CollectionUtils.isEmpty(orderProductItems)) {
                        continue;
                    }

                    OrderProductItem orderProductItem = orderProductItems.stream().filter(item -> item.getAppItemId().equals(bookId)).findFirst().orElse(null);
                    if (null == orderProductItem) {
                        continue;
                    }
                    AppPayMapper paidStatus = userOrderLoader.getUserAppPaidStatus(OrderProductServiceType.PicListenBook.name(), currentUserId(), true);
                    if (paidStatus != null && CollectionUtils.isNotEmpty(paidStatus.getValidItems()) && paidStatus.getValidItems().contains(orderProductItem.getId())) {
                        picListenProductId = null;
                        //购买过了，无论是打包产品还是非打包产品都一样，订单里不需要加点读机了
                        break;
                    }

                    //教材对应的点读机没买过，则创建订单时把点读机产品id带过去
                    //如果有打包产品和非打包产品，优先考虑非打包产品，如果多个产品都是非打包产品（理论上不应该出现），取最后一个产品
                    if (StringUtils.isBlank(picListenProductId)) {
                        picListenProductId = p.getId();
                    } else if (orderProductItems.size() == 1) {
                        picListenProductId = p.getId();
                    }
                }
            }
        }

        return picListenProductId;
    }

    //////////////////////////////////////
    /////////  已下线接口      /////////////
    //////////////////////////////////////

    /**
     * 生成Trustee order
     */
    @RequestMapping(value = "createtrusteeorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createTrusteeOrder() {
        return MapMessage.errorMessage("托管班类型订单已下线");
    }


    /**
     * 生成家长买学豆订单
     */
    @RequestMapping(value = "createintegralorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createIntegralOrder() {
        return MapMessage.errorMessage("购买学豆功能已下线");
    }

    /**
     * 查询学豆订单
     */
    @RequestMapping(value = "loadintegralorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadIntegralOrder() {
        return MapMessage.errorMessage("购买学豆功能已下线");
    }

}
