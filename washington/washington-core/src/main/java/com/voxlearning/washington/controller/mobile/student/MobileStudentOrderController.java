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

package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.mizar.api.entity.oa.UserOfficialAccountsRef;
import com.voxlearning.utopia.service.order.api.constants.OrderReferType;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.loader.UserOrderLoader;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeContext;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.mobile.parent.AbstractMobileParentController;
import com.voxlearning.washington.controller.mobile.parent.MobileParentOrderController;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.mapper.StudentFairylandProductItemMapper;
import com.voxlearning.washington.mapper.StudentFairylandProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 2/24/17.
 */
@Controller
@RequestMapping(value = "/studentMobile/order")
@Slf4j
public class MobileStudentOrderController extends AbstractMobileController {
    @ImportService(interfaceClass = UserOrderLoader.class)
    private UserOrderLoader userOrderLoader;

    /**
     * 查询产品详情
     * 参数：
     * type 必填  产品类型，see {@link com.voxlearning.utopia.api.constant.OrderProductServiceType}
     * id   选填  产品id，点读机产品传bookId
     */
    @RequestMapping(value = "/productinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage productInfo() {
        String type = getRequestString("type");
        OrderProductServiceType productType = OrderProductServiceType.safeParse(type);
        if (OrderProductServiceType.Unknown == productType) {
            return MapMessage.errorMessage("产品类型未知");
        }

        String productId = getRequestString("id");
        String key = getRequestString("key");   //这是产品唯一标识，可以定位到具体的一个产品，存储在产品的attr里

        try {
            OrderProduct orderProduct;
            //如果是点读机教材，传进来的是bookId，要去查一下对应的产品id
            if (OrderProductServiceType.PicListenBook == productType) {
                if (StringUtils.isNotBlank(key)) {
                    orderProduct = findPicListenBookOrderProductByKey(key);
                } else {
                    //历史原因吧，productId有可能是bookId,也有可能是产品id
                    orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
                    if (null == orderProduct) {
                        orderProduct = findPicListenBookOrderProductByBookId(productId);
                    }
                }
            } else {
                orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
            }

            if (null == orderProduct) {
                return MapMessage.errorMessage("未查询到产品");
            }

            //取出项部和底部banner
            List<FairylandProduct> fairylandProducts = fairylandLoaderClient.loadFairylandProducts(FairyLandPlatform.STUDENT_APP, null);
            FairylandProduct fairylandProduct = fairylandProducts.stream().filter(p -> p.getAppKey().equals(productType.name())).findFirst().orElse(null);

            //取产品信息
            List<StudentFairylandProductMapper> productMappers = new ArrayList<>();
            //限定了只显示某一个产品
            StudentFairylandProductMapper mapper = new StudentFairylandProductMapper();
            mapper.setId(orderProduct.getId());
            mapper.setName(orderProduct.getName());
            mapper.setPrice(orderProduct.getPrice());
            mapper.setOriginalPrice(orderProduct.getOriginalPrice());

            productMappers.add(mapper);

            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId());
            if (CollectionUtils.isNotEmpty(orderProductItems)) {
                List<StudentFairylandProductItemMapper> itemMappers = new ArrayList<>();
                for (OrderProductItem item : orderProductItems) {
                    StudentFairylandProductItemMapper itemMapper = new StudentFairylandProductItemMapper();
                    itemMapper.setId(item.getId());
                    itemMapper.setName(item.getName());
                    itemMapper.setPeriod(item.getPeriod());
                    itemMappers.add(itemMapper);
                }

                mapper.setItems(itemMappers);
            }

            boolean useBanner = null != fairylandProduct && StringUtils.isNotBlank(fairylandProduct.getBannerImage());

            MapMessage message = MapMessage.successMessage().add("info", productMappers);
            if (useBanner) {
                message.add("bannerImg", getUserAvatarImgUrl(fairylandProduct.getBannerImage()));
                message.add("descImg", getUserAvatarImgUrl(fairylandProduct.getDescImage()));
            }
            return message;
        } catch (Exception ex) {
            logger.error("get product info fail,type:{},pid:{},msg:{}", type, productId, ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 检查学生支付是否已被关闭
     */
    @RequestMapping(value = "/permission.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage permission() {
        String productId = getRequestString("id");
        if (StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("参数错误");
        }
        Long studentId = currentUserId();

        try {
            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
            if (null == orderProduct) {
                return MapMessage.errorMessage("未知的产品");
            }

            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                return MapMessage.errorMessage("没有绑定家长号，请前往一起作业家长端APP注册家长号后购买").add("toParent", true);
            }

            if (OrderProductServiceType.safeParse(orderProduct.getProductType()) == OrderProductServiceType.PicListenBook) {
                Set<Long> parentIds = studentParentRefs.stream()
                        .map(StudentParentRef::getParentId)
                        .collect(Collectors.toSet());
                Map<Long, UserAuthentication> parentAuthenticationMap = userLoaderClient.loadUserAuthentications(parentIds);
                if (MapUtils.isEmpty(parentAuthenticationMap)) {
                    return MapMessage.errorMessage("家长号没有绑定手机，请前往一起作业家长APP为家长号绑定手机号后购买").add("toParent", true);
                }
                Set<Long> parentIdsMobileAuthenticated = parentAuthenticationMap.values().stream()
                        .filter(UserAuthentication::isMobileAuthenticated)
                        .map(UserAuthentication::getId)
                        .collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(parentIdsMobileAuthenticated)) {
                    return MapMessage.errorMessage("家长号没有绑定手机，请前往一起作业家长APP为家长号绑定手机号后购买").add("toParent", true);
                }
                studentParentRefs = studentParentRefs.stream().filter(p -> parentIdsMobileAuthenticated.contains(p.getParentId())).collect(Collectors.toList());

                //查出能下单的家长列表,如果学生没有支付权限，可以给家长发支付请求，如果学生有支付权限，则把订单下到选择的家长身上
                List<Map<String, Object>> parentInfos = new ArrayList<>();
                studentParentRefs.forEach(ref -> {
                    UserAuthentication userAuthentication = parentAuthenticationMap.get(ref.getParentId());
                    if (null != userAuthentication) {
                        Map<String, Object> info = new HashMap<>();
                        info.put("name", ref.getCallName());
                        info.put("id", ref.getParentId());
                        String mobileObscured = sensitiveUserDataServiceClient.loadUserMobileObscured(ref.getParentId());
                        info.put("mobile", mobileObscured);
                        parentInfos.add(info);
                    }
                });

                StudentExtAttribute attribute = studentLoaderClient.loadStudentExtAttribute(currentUserId());
                boolean studentPayPermit = null == attribute || attribute.fetchPayFreeStatus();

                return MapMessage.successMessage()
                        .add("permission", studentPayPermit)
                        .add("parents", parentInfos);
            } else {
                return MapMessage.errorMessage("不支持的产品类型");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 生成订单
     */
    @RequestMapping(value = "/create.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage create() {
        String productId = getRequestString("id");
        Long parentId = getRequestLong("pid");  //如果订单需要下到家长身上需要此参数
        String rel = getRequestParameter("rel", OrderReferType.STUDENT_APP_FAIRYLAND_DEFAULT.type);

        if (StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
            if (null == orderProduct) {
                return MapMessage.errorMessage("未知的产品");
            }
            MapMessage validateResult = validateStudentPay(currentUserId(), orderProduct);
            if (!validateResult.isSuccess()) {
                return validateResult;
            }

            UserOrder userOrder;
            if (OrderProductServiceType.PicListenBook == OrderProductServiceType.safeParse(orderProduct.getProductType())) {
                MapMessage message = parentServiceClient.loadOrRegisteDefaultParentUserByStudentId(currentUserId());
                if (message.isSuccess()) {
                    Object parentObj = message.get("parent");
                    if (parentObj instanceof User) {
                        User parent = User.class.cast(parentObj);
                        parentId = parent.getId();
                    }
                }

                if (0 == parentId) {
                    return MapMessage.errorMessage("无效的家长ID");
                }
                if (!availableForRenew(orderProduct, parentId)) {
                    return MapMessage.errorMessage("下单失败，当前产品（或当前产品的部分内容）仍在服务期，不支持续费");
                }

                userOrder = createOrderForParent(parentId, orderProduct, rel);
            } else {
                return MapMessage.errorMessage("不支持的产品类型");
            }

            if (null == userOrder) {
                return MapMessage.errorMessage("生成订单失败");
            }

            //生成跳转支付页需根的参数
            Map<String, String> payParams = generatePayParams(userOrder, getRequestString("return_url"));

            return MapMessage.successMessage().add("id", userOrder.genUserOrderId())
                    .add("payParams", payParams);
        } catch (Exception ex) {
            logger.error("create order fail,pid:{},msg:{}", productId, ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private boolean availableForRenew(OrderProduct orderProduct, Long userId) {
        if (OrderProductServiceType.safeParse(orderProduct.getProductType()) == OrderProductServiceType.PicListenBook) {
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

    private Map<String, String> generatePayParams(UserOrder userOrder, String returnUrl) {
        Map<String, String> params = new HashMap<>();

        params.put(ApiConstants.REQ_APP_KEY, "17Student");
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", currentUserId());
        if (null != vendorAppsUserRef) {
            params.put(ApiConstants.REQ_SESSION_KEY, vendorAppsUserRef.getSessionKey());
        }

        params.put(ApiConstants.REQ_ORDER_ID, userOrder.genUserOrderId());
        params.put(ApiConstants.REQ_ORDER_TOKEN, userOrder.getOrderToken());
        if (StringUtils.isBlank(returnUrl)) {
            params.put(ApiConstants.REQ_RETURN_URL, "");
        } else {
            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(userOrder.getProductId());
//            if (CollectionUtils.isEmpty(orderProductItems) || orderProductItems.size() > 1) {
//                throw new IllegalStateException("Illegal item count for product " + userOrder.getProductId());
//            }
            String obscuredMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(userOrder.getUserId());
            params.put(ApiConstants.REQ_RETURN_URL, returnUrl + (returnUrl.indexOf("?") > 0 ? "&" : "?") + "mobile=" + obscuredMobile + "&book_id=" + orderProductItems.get(0).getAppItemId());
        }

        VendorApps vendorApps = vendorLoaderClient.loadVendor("17Student");
        if (null == vendorApps) {
            throw new IllegalStateException("No vendorApp found for 17Student");
        }

        params.put(ApiConstants.REQ_SIG, DigestSignUtils.signMd5(params, vendorApps.getSecretKey()));

        return params;
    }

    private UserOrder createOrderForParent(Long parentId, OrderProduct product, String refer) {
        UserOrder order = UserOrder.newOrder(OrderType.pic_listen, parentId);
        order.setUserId(parentId);
        order.setProductAttributes(product.getAttributes());
        order.setOrderPrice(product.getPrice());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setOrderProductServiceType(product.getProductType());
        order.setOrderReferer(refer);
        order.setUserReferer(SafeConverter.toString(currentUserId()));

        String orderToken = StringUtils.join(System.currentTimeMillis(), RandomUtils.randomNumeric(10));
        order.setOrderToken(orderToken);
        order.setOrderSeq(Instant.now().toEpochMilli()); //仅用于生成orderToken，别无他用

        OrderSynchronizeContext context = new OrderSynchronizeContext();
        if (StringUtils.isNotBlank(product.getAttributes())) {
            Map<String, Object> productAttr = JsonUtils.fromJson(product.getAttributes());
            if (MapUtils.isNotEmpty(productAttr) && productAttr.containsKey(MobileParentOrderController.FIELD_PICLISTEN_PACKAGE_ID)) {
                context.setPackageId(productAttr.get(MobileParentOrderController.FIELD_PICLISTEN_PACKAGE_ID).toString());
            }
        }
        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
        if (CollectionUtils.isNotEmpty(orderProductItems)) {
            for (OrderProductItem item : orderProductItems) {
                if (OrderProductServiceType.safeParse(item.getProductType()) != OrderProductServiceType.PicListenBook) continue;
                TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(item.getAppItemId());

                context.addBook(sdkInfo.getSdkType().name(), sdkInfo.getSdkBookIdV2(), item.getPeriod(), item.getOriginalPrice().multiply(BigDecimal.valueOf(100)).intValue());
            }
        }

        order.setExtAttributes(JsonUtils.toJson(context));
        MapMessage message = userOrderServiceClient.saveUserOrder(order);
        if (!message.isSuccess()) {
            return null;
        }

        //默认关注磨耳朵公众号
        OfficialAccounts officialAccounts = officialAccountsServiceClient
                .loadAccountsByKeyIncludeOffline(OfficialAccounts.SpecialAccount.GRIND_EAR_SERVICE.getKey());
        if (officialAccounts != null && !officialAccounts.getPaymentBlackLimit()) {
            officialAccountsServiceClient.updateFollowStatus(
                    parentId,
                    OfficialAccounts.SpecialAccount.GRIND_EAR_SERVICE.getId(),
                    UserOfficialAccountsRef.Status.Follow);
        }

        return order;
    }

    private OrderProduct findPicListenBookOrderProductByBookId(String productId) {
        Map<String, List<OrderProduct>> orderProductMap = userOrderLoaderClient.loadOrderProductByAppItemIds(Collections.singletonList(productId));
        if (MapUtils.isEmpty(orderProductMap)) {
            return null;
        }

        List<OrderProduct> orderProducts = orderProductMap.get(productId)
                .stream()
                .filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == OrderProductServiceType.PicListenBook)
                .filter(p -> {
                    //用bookId来找产品，只能打有1个item的
                    List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(p.getId());
                    return CollectionUtils.isNotEmpty(orderProductItems) && orderProductItems.size() == 1;
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orderProducts)) {
            return null;
        }

        if (orderProducts.size() > 1) {
            return null;
        }
        return orderProducts.get(0);
    }


    private OrderProduct findPicListenBookOrderProductByKey(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        List<OrderProduct> orderProducts = userOrderLoader.loadAllOrderProduct();
        return orderProducts.stream()
                .filter(op -> StringUtils.isNotBlank(op.getAttributes()))
                .filter(op -> {
                    Map<String, Object> attr = JsonUtils.fromJson(op.getAttributes());
                    return attr.containsKey(AbstractMobileParentController.FIELD_PICLISTEN_PACKAGE_ID) && key.equals(attr.get(AbstractMobileParentController.FIELD_PICLISTEN_PACKAGE_ID));
                }).findFirst().orElse(null);
    }

}
