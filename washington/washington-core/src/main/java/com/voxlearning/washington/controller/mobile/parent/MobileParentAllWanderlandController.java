package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.coupon.api.entities.Coupon;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentSelfStudyTypeH5Mapper;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.washington.support.ParentCrosHeaderSupport;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-08-09 下午4:55
 **/

@Controller
@RequestMapping(value = "/parentMobile/allwanderland")
public class MobileParentAllWanderlandController extends AbstractMobileParentSelfStudyController {
    @Inject
    private CouponLoaderClient couponLoaderClient;
    @Inject
    private CouponServiceClient couponServiceClient;

    @RequestMapping(value = "coupon.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendCoupon() {
        String couponId = getRequestString("couponId");
        if (StringUtils.isBlank(couponId)) {
            return MapMessage.errorMessage("couponId错误");
        }
        User parent = currentParent();
        if (parent == null || !parent.isParent()) {
            return noLoginResult;
        }
        Coupon coupon = couponLoaderClient.loadCouponById(couponId);
        if (coupon != null) {
            return couponServiceClient.sendCoupon(couponId, parent.getId());
        }
        return MapMessage.errorMessage("发放优惠券失败");
    }

    @RequestMapping(value = "/classmate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getClassMate() {
        User parent = currentParent();
        if (parent == null || !parent.isParent())
            return noLoginResult;

        long sid = getRequestLong("sid");
        if (sid == 0)
            return noLoginResult;
        String appKeysStr = getRequestString("app_keys");
        String[] split = appKeysStr.split(",");
        if (StringUtils.isBlank(appKeysStr) || split.length == 0)
            return MapMessage.errorMessage("参数错误");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        if (studentDetail == null)
            return noLoginResult;
        List<String> appKeys = Arrays.asList(split);
        Map<String, Object> appMap = new HashMap<>();
        List<FairylandProduct> parentAvailableFairylandProducts =
                businessVendorServiceClient.getParentAvailableFairylandProducts(parent, studentDetail, FairyLandPlatform.PARENT_APP, FairylandProductType.APPS);
        Map<String, FairylandProduct> fairylandProductMap = parentAvailableFairylandProducts.stream().collect(Collectors.toMap(FairylandProduct::getAppKey, Function.identity()));
        if (studentDetail.getClazz() == null) {
            for (String appKey : appKeys) {
                FairylandProduct fairylandProduct = fairylandProductMap.get(appKey);
                if (fairylandProduct == null)
                    continue;
                Map<String, Object> map = new HashMap<>();
                map.put("user_text", "暂无同班同学在学");
                map.put("icon", getCdnBaseUrlStaticSharedWithSep() + fairylandProduct.getProductIcon());
                appMap.put(appKey, map);
            }
        } else {
            Map<String, String> userTextMap = businessVendorServiceClient.fetchUserUseNumDesc(appKeys, studentDetail);
            for (String appKey : appKeys) {
                FairylandProduct fairylandProduct = fairylandProductMap.get(appKey);
                if (fairylandProduct == null) {
                    if (appKey.equals(OrderProductServiceType.PicListenBook.name())) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("use_text", userTextMap.get(appKey));
                        map.put("icon", getCdnBaseUrlStaticSharedWithSep() + SelfStudyType.PICLISTEN_ENGLISH.getIconUrl());
                        appMap.put(appKey, map);
                    } else
                        continue;
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("use_text", userTextMap.get(appKey));
                    map.put("icon", getCdnBaseUrlAvatarWithSep() + "gridfs/" + fairylandProduct.getProductIcon());
                    appMap.put(appKey, map);
                }
            }
        }

        return MapMessage.successMessage().add("same_clazz_student_count_map", appMap);
    }

    @RequestMapping(value = "/infos.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage infos() {
        ParentCrosHeaderSupport.setCrosHeader(getWebRequestContext());
        User parent = currentParent();
        if (parent == null || !parent.isParent())
            return noLoginResult;

        long sid = getRequestLong("sid");
        if (sid == 0)
            return noLoginResult;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        if (studentDetail == null)
            return noLoginResult;
        String appKey = getRequestString("app_key");
        if (StringUtils.isBlank(appKey))
            return MapMessage.errorMessage("没有 appKey");

        OrderProductServiceType orderProductServiceType = OrderProductServiceType.safeParse(appKey);
        if (orderProductServiceType == OrderProductServiceType.Unknown)
            return MapMessage.errorMessage("错误的appKey");
        MapMessage mapMessage = MapMessage.successMessage();
        if (appKey.equals(OrderProductServiceType.PicListenBook.name()) || appKey.equals(OrderProductServiceType.WalkerMan.name())) {
            Map<String, DayRange> picListenLastDayMap = picListenCommonService.parentBuyBookPicListenLastDayMap(parent.getId(), false);
            List<Map<String, Object>> picInfoList = new ArrayList<>();
            picListenLastDayMap.forEach((key, value) -> {
                TextBookManagement textBook = textBookManagementLoaderClient.getTextBook(key);
                if (textBook == null)
                    return;
                NewBookProfile newBookProfile = newContentLoaderClient.loadBook(textBook.getBookId());
                if (newBookProfile == null)
                    return;
                Map<String, Object> map = new HashMap<>();
                map.put("book_name", newBookProfile.getShortName());
                map.put("subject_name", Subject.fromSubjectId(newBookProfile.getSubjectId()).getValue());
                map.put("days_reminding", DateUtils.dayDiff(value.getEndDate(), new Date()));
                picInfoList.add(map);
            });
            mapMessage.add("piclisten_infos", picInfoList);
        } else {

            OrderProductServiceType type = orderProductServiceType;
            if (type == null || type == OrderProductServiceType.Unknown)
                return MapMessage.errorMessage("error appKey");

            OrderProductServiceType newType = old2ImprovedType(type);
            AppPayMapper improvedMapper = null;
            if (newType != null) {
                improvedMapper = userOrderLoaderClient.getUserAppPaidStatus(newType.name(), studentDetail.getId());
            }
            AppPayMapper oldMapper = userOrderLoaderClient.getUserAppPaidStatus(type.name(), studentDetail.getId());

            AppPayMapper userAppMapper;
            if (improvedMapper == null) {
                userAppMapper = oldMapper;
            } else {
                if (safeIsInActive(improvedMapper)) {
                    userAppMapper = improvedMapper;
                } else if (safeIsInActive(oldMapper)) {
                    userAppMapper = oldMapper;
                } else
                    userAppMapper = null;
            }


            String status = convert2Status(userAppMapper);
            Long daysReminding = userAppMapper == null ? 0 : SafeConverter.toLong(userAppMapper.getDayToExpire());
            mapMessage.add("days_reminding", daysReminding);
            mapMessage.add("status", status);
            SelfStudyType selfStudyType = SelfStudyType.fromOrderType(orderProductServiceType);
            if (selfStudyType != null) {
                ParentSelfStudyTypeH5Mapper parentSelfStudyTypeH5Mapper = loadEntryMapper(Collections.singleton(selfStudyType), studentDetail, studentDetail, false, "??????", true).get(selfStudyType);
                if (parentSelfStudyTypeH5Mapper == null)
                    return MapMessage.errorMessage("错误的用户状态");

                mapMessage.add("entry", parentSelfStudyTypeH5Mapper);
            }
        }
        mapMessage.add("avatar_url", getUserAvatarImgUrl(studentDetail));
        mapMessage.add("student_name", studentDetail.fetchRealname());

        return mapMessage;


    }

    private boolean safeIsInActive(AppPayMapper appPayMapper) {
        return appPayMapper != null && appPayMapper.isActive();
    }


    private OrderProductServiceType old2ImprovedType(OrderProductServiceType appKey) {
        switch (appKey) {
            case AfentiExam:
                return OrderProductServiceType.AfentiExamImproved;
            case AfentiChinese:
                return OrderProductServiceType.AfentiChineseImproved;
            case AfentiMath:
                return OrderProductServiceType.AfentiMathImproved;
            default:
                return null;
        }
    }

    private Long getDaysReminding(UserActivatedProduct userActivatedProduct) {
        if (userActivatedProduct == null)
            return 0L;
        if (userActivatedProduct.getServiceEndTime().before(new Date()))
            return 0L;
        return DateUtils.dayDiff(userActivatedProduct.getServiceEndTime(), new Date()) + 1;
    }

    private String convert2Status(AppPayMapper appPayMapper) {
        if (appPayMapper == null)
            return "not_purchased";
        if (appPayMapper.isActive())
            return "not_expire";
        else return "expire";
    }


    @RequestMapping(value = "/piclisten_products.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage piclistenProdunctInfos() {
        User parent = currentParent();
        if (parent == null || !parent.isParent())
            return noLoginResult;

        long sid = getRequestLong("sid");
        if (sid == 0)
            return noLoginResult;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        if (studentDetail == null)
            return noLoginResult;
        List<Map<String, Object>> recommendPicListenBook = parentSelfStudyPublicHelper.recommendPicListenBook(studentDetail, parent, getRequestString("app_version"));

        List<String> bookIdList = recommendPicListenBook.stream().map(t -> SafeConverter.toString(t.get("book_id"))).collect(Collectors.toList());
        Map<String, PiclistenProductWrapper> wrapperMap = findOrderProductByBookIds(bookIdList);
        Map<String, OrderProduct> productMap = new HashMap<>();
        Map<String, OrderProductItem> productItemMap = new HashMap<>();
        for (String bookId : bookIdList) {
            PiclistenProductWrapper wrapper = wrapperMap.get(bookId);
            if (wrapper == null)
                continue;
            OrderProduct orderProduct = wrapper.getOrderProduct();
            if (orderProduct != null) {
                productMap.put(bookId, orderProduct);
                OrderProductItem orderProductItem = wrapper.getOrderProductItem();
                if (orderProductItem != null)
                    productItemMap.put(bookId, orderProductItem);
            }
        }
        String cdnBaseUrlStaticSharedWithSep = getCdnBaseUrlStaticSharedWithSep();
        recommendPicListenBook.forEach(t -> {
            t.put("img", cdnBaseUrlStaticSharedWithSep + t.get("img"));
            String bookId = SafeConverter.toString(t.get("book_id"));
            OrderProduct orderProduct = productMap.get(bookId);
            if (orderProduct == null)
                return;
            OrderProductItem orderProductItem = productItemMap.get(bookId);
            if (orderProductItem != null) {
                t.put("product_id", orderProduct.getId());
                t.put("price", orderProduct.getPrice());
                t.put("period", orderProductItem.getPeriod());
            } else {
                t.put("product_id", "");
                t.put("price", 0);
                t.put("period", 0);
            }
        });
        return MapMessage.successMessage().add("book_product_map", recommendPicListenBook.stream().
                collect(Collectors.groupingBy(t -> Subject.valueOf(SafeConverter.toString(t.get("subject"))))));

    }

    @Data
    private static class PiclistenProductWrapper {
        private OrderProduct orderProduct;
        private OrderProductItem orderProductItem;

        public static PiclistenProductWrapper newInstance() {
            return new PiclistenProductWrapper();
        }

    }


    // FIXME: 2017/8/9 这里在上了打包购买之后，需要抽象一个逻辑出来，因为一个 item可能对应多个 product了
    private Map<String, PiclistenProductWrapper> findOrderProductByBookIds(Collection<String> bookIds) {
        if (CollectionUtils.isEmpty(bookIds))
            return Collections.emptyMap();

        Map<String, List<OrderProduct>> orderProductMap = userOrderLoaderClient.loadOrderProductByAppItemIds(bookIds);
        Set<String> productIdSet = orderProductMap.values().stream().flatMap(Collection::stream).map(OrderProduct::getId).collect(Collectors.toSet());
        Map<String, List<OrderProductItem>> itemsByProductIds = userOrderLoaderClient.loadProductItemsByProductIds(productIdSet);
        Map<String, PiclistenProductWrapper> wrapperMap = new HashMap<>();
        for (String bookId : bookIds) {
            List<OrderProduct> products = orderProductMap.get(bookId);
            if (CollectionUtils.isNotEmpty(products)) {
                PiclistenProductWrapper wrapper = PiclistenProductWrapper.newInstance();
                OrderProduct product = products.stream()
                        .filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == OrderProductServiceType.PicListenBook && (itemsByProductIds.get(p.getId()) != null && itemsByProductIds.get(p.getId()).size() == 1))
                        .findFirst().orElse(null);
                if (product == null)
                    continue;
                List<OrderProductItem> orderProductItems = itemsByProductIds.get(product.getId());
                wrapper.setOrderProduct(product);
                wrapper.setOrderProductItem(orderProductItems.get(0));
                wrapperMap.put(bookId, wrapper);
            }
        }
        return wrapperMap;
    }


}
