package com.voxlearning.luffy.controller.piclisten;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.payment.PaymentRequestForm;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramBookService;
import com.voxlearning.utopia.service.piclisten.client.MiniProgramBookServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenCollectData;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-09-14 上午10:49
 **/
@Controller
@RequestMapping(value = "/xcx/piclisten")
public class ParentXcxPicListenController extends AbstractXcxPicListenController {

    public static final String FIELD_PICLISTEN_PACKAGE_ID = "piclisten_package_id";   //套餐id

    @Inject
    private MiniProgramBookServiceClient miniProgramBookServiceClient;


    /**
     * 书架教材列表
     *
     * @return
     */
    @RequestMapping(value = "/book_shelf.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage bookShelf(HttpServletRequest request) {
        String sys = sys();
        Long pid = pid();

        return wrapper((mm) -> {
            mm.putAll(bookService().bookSelf(pid, sys, getCdnBaseUrlStaticSharedWithSep(request)));
        });

    }

    @RequestMapping(value = "/book_shelf/add.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addBook2Shelf() {

        Long pid = pid();
        String bookId = getRequestString("book_id");


        if (StringUtils.isBlank(bookId))
            return MapMessage.errorMessage("请选择一本教材!");

        try {
            return AtomicLockManager.instance().wrapAtomic(parentSelfStudyService).keyPrefix("addBook").keys(pid)
                    .proxy().addBook2PicListenShelf(pid, bookId);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    @RequestMapping(value = "/book_shelf/delete.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteBook2Shelf() {
        Long pid = pid();
        String bookId = getRequestString("book_ids");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("请选择一本教材!");
        }
        List<String> bookIds = Arrays.asList(bookId.split(","));
        try {
            return AtomicLockManager.instance().wrapAtomic(parentSelfStudyService).keyPrefix("deleteBook").keys(pid)
                    .proxy().deleteBooksFromPicListenShelf(pid, bookIds);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }


    @RequestMapping(value = "/book/product_info.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage productInfo(HttpServletRequest request) {
        Long pid = pid();
        Long uid = uid();
        String bookId = getRequestString("book_id");

        return wrapper((mm) -> {
            mm.putAll(bookService().productInfo(uid, pid, bookId, getCdnBaseUrlStaticSharedWithSep(request), getCdnBaseUrlAvatarWithSep(request)));
        });

    }


    @RequestMapping(value = "/book/recommend.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage recommend(HttpServletRequest request) {
        Long pid = pid();
        Long uid = uid();
        String productIds = getRequestString("product_ids");
        return wrapper((mm) -> {
            mm.putAll(bookService().recommend(uid, pid, productIds, getCdnBaseUrlStaticSharedWithSep(request)));
        });

    }


    @RequestMapping(value = "/book/createorder.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage createOrder() {
        Long parentId = pid();
        String productId = getRequestString("product_id");
        String refer = getRequestString("refer");

        if (StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("参数错误");
        }

        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        if (product == null || (OrderProductServiceType.safeParse(product.getProductType()) != OrderProductServiceType.PicListenBook
                && OrderProductServiceType.safeParse(product.getProductType()) != OrderProductServiceType.WalkerMan)) {
            return MapMessage.errorMessage("不支持的产品类型");
        }

        if (!availableForRenew(product, parentId)) {
            return MapMessage.errorMessage("下单失败，当前产品（或当前产品的部分内容）仍在服务期，不支持续费");
        }

        UserOrder order = UserOrder.newOrder(OrderType.pic_listen, parentId);
        order.setUserId(parentId);
        order.setProductAttributes(product.getAttributes());
        order.setOrderPrice(product.getPrice());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setOrderProductServiceType(product.getProductType());
        order.setOrderReferer(refer);
        order.setUserReferer(SafeConverter.toString(parentId));

        if (OrderProductServiceType.PicListenBook == OrderProductServiceType.safeParse(product.getProductType())) {
            OrderSynchronizeContext context = generateAttrForPicListenBook(product);
            order.setExtAttributes(JsonUtils.toJson(context));
        }
        MapMessage message = userOrderServiceClient.saveUserOrder(order);
        if (message.isSuccess()) {

            MapMessage mapMessage = MapMessage.successMessage()
                    .add("orderId", order.genUserOrderId())
                    .add("productName", order.getProductName())
                    .add("price", order.getOrderPrice())
                    .add("createDate", DateUtils.dateToString(order.getCreateDatetime(), "yyyy.MM.dd"));

            List<CouponShowMapper> mappers = userOrderLoaderClient.loadOrderUsableCoupons(order, parentId);
            if (CollectionUtils.isNotEmpty(mappers)) {
                mappers.sort(Comparator.comparing(CouponShowMapper::getTypeValue).reversed());
                mapMessage.add("discountPrice", mappers.get(0).getDiscountPrice());
                mapMessage.add("coupons", mappers);
            } else {
                mapMessage.add("discountPrice", order.getOrderPrice());
                mapMessage.add("coupons", Collections.emptyList());
            }
            return mapMessage;


        } else {
            return MapMessage.errorMessage("生成订单失败");
        }
    }


    // 关联订单与使用的优惠劵
    @RequestMapping(value = "/book/relatedcouponorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage relatedCouponOrder() {

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


    @RequestMapping(value = "/book/pay.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage pay() {
        Long parentId = pid();
        String orderId = getRequestString("orderId");

        if (StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage("参数错误");
        }

        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);


        if (order == null || order.getPaymentStatus() != PaymentStatus.Unpaid || order.getOrderStatus() != OrderStatus.New) {
            return MapMessage.errorMessage("订单不存在或者已经失效");
        }


        try {
            PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(PaymentConstants.PaymentGatewayName_Wechat_Piclisten);
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setTradeNumber(order.genUserOrderId());
            paymentRequest.setProductName(order.getProductName());
            paymentRequest.setPayMethod(PaymentConstants.PaymentGatewayName_Wechat_Piclisten);
            paymentRequest.setSpbillCreateIp(getRequestContext().getRealRemoteAddress());
            BigDecimal amount = userOrderServiceClient.getOrderCouponDiscountPrice(order);
            if (PaymentGateway.getUsersForPaymentTest(order.getUserId())) {
                amount = new BigDecimal(0.01);
            }
            paymentRequest.setPayAmount(amount);
            paymentRequest.setPayUser(parentId);
            paymentRequest.setCallbackBaseUrl(ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order");
            paymentRequest.setOpenid(getOpenId());

            PaymentRequestForm paymentRequestForm = paymentGateway.getPaymentRequestForm(paymentRequest);
            if (MapUtils.isNotEmpty(paymentRequestForm.getFormFields())) {
                MapMessage result = MapMessage.successMessage();
                result.putAll(paymentRequestForm.getFormFields());
                return result;
            } else {
                return MapMessage.errorMessage("调用第三方支付失败");
            }
        } catch (UtopiaRuntimeException ex) {
            return MapMessage.errorMessage("调用第三方支付失败").add("ex", ex.getMessage());
        } catch (Exception ex) {
            logger.error("xcx pay order failed. orderId:{}",
                    order.genUserOrderId(), ex);
            return MapMessage.errorMessage("调用第三方支付失败").add("ex", ex.getMessage());
        }
    }


    /**
     * 为点读报告收集数据
     */
    @RequestMapping(value = "/book/data/collect.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage dataCollect() {
        MapMessage resultMap = new MapMessage();
        String data = getRequestString("data");
        Long sid = uid();
        if (StringUtils.isBlank(data) || sid == null || sid == 0L) {
            return MapMessage.errorMessage("数据错误");
        }

        List<PicListenCollectData.SentenceResult> sentenceResultList =
                JsonUtils.fromJsonToList(data, PicListenCollectData.SentenceResult.class);
        if (CollectionUtils.isEmpty(sentenceResultList))
            return MapMessage.errorMessage("数据格式错误");

        PicListenCollectData picListenCollectData = new PicListenCollectData(sid, DayRange.current());
        picListenCollectData.setSentenceResultList(sentenceResultList);
        parentSelfStudyService.processPicListenCollectData(picListenCollectData.union());
        return MapMessage.successMessage();
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


    private OrderSynchronizeContext generateAttrForPicListenBook(OrderProduct product) {
        OrderSynchronizeContext context = new OrderSynchronizeContext();

        if (StringUtils.isNotBlank(product.getAttributes())) {
            Map<String, Object> productAttr = JsonUtils.fromJson(product.getAttributes());
            if (MapUtils.isNotEmpty(productAttr) && productAttr.containsKey(FIELD_PICLISTEN_PACKAGE_ID)) {
                context.setPackageId(productAttr.get(FIELD_PICLISTEN_PACKAGE_ID).toString());
            }
        }

        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
        if (CollectionUtils.isNotEmpty(orderProductItems)) {
            for (OrderProductItem item : orderProductItems) {
                if (OrderProductServiceType.safeParse(item.getProductType()) != OrderProductServiceType.PicListenBook) continue;
                TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(item.getAppItemId());
                if (null == sdkInfo || StringUtils.isBlank(sdkInfo.getSdkBookIdV2())) continue;

                context.addBook(sdkInfo.getSdkType().name(), sdkInfo.getSdkBookIdV2(), item.getPeriod(), BigDecimal.valueOf(item.getOriginalPrice().longValue()).multiply(BigDecimal.valueOf(100)).intValue());
            }
        }
        return context;
    }


    private MiniProgramBookService bookService() {
        return miniProgramBookServiceClient.getRemoteReference();
    }


    private String getCdnBaseUrlStaticSharedWithSep(HttpServletRequest request) {
        return cdnResourceUrlGenerator.getCdnBaseUrlStaticSharedWithSep(request);
    }


    private String getCdnBaseUrlAvatarWithSep(HttpServletRequest request) {
        return cdnResourceUrlGenerator.getCdnBaseUrlAvatarWithSep(request);
    }

}
