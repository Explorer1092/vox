package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishUserService;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsProductUserCountCacheManager;
import com.voxlearning.utopia.service.ai.data.ChipsUserOrderBO;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishClassPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsGroupShoppingPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsUserOrderExtDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsVideoBlackListDao;
import com.voxlearning.utopia.service.ai.util.CollectionExtUtil;
import com.voxlearning.utopia.service.ai.util.StringExtUntil;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = ChipsEnglishUserService.class)
public class ChipsEnglishUserServiceImpl extends AbstractAiSupport implements ChipsEnglishUserService {

    private static String GROUP_SHOPPING_SPONSOR_SIGN = "TEMP";
    private final static String Chips_Super_User = "Chips_Super_User";

    @Inject
    private ChipsUserOrderExtDao chipsUserOrderExtDao;

    @Inject
    private UserOrderServiceClient userOrderServiceClient;

    @Inject
    private ChipsProductUserCountCacheManager chipsProductUserCountCacheManager;

    @Inject
    private ChipsEnglishClassPersistence chipsEnglishClassPersistence;

    @Inject
    private ChipsGroupShoppingPersistence chipsGroupShoppingPersistence;
    @Inject
    private AiChipsEnglishConfigServiceImpl chipsEnglishConfigService;
    @Inject
    private ChipsVideoBlackListDao videoBlackListDao;

    @Override
    public MapMessage createOrder(ChipsUserOrderBO chipsOrder) {
        if (chipsOrder == null || !chipsOrder.parameterCheck()) {
            return MapMessage.errorMessage("参数为空");
        }
        Long userId = chipsOrder.getUserId();
        List<String> productIds = chipsOrder.getProductIds();
        Set<Long> blackSet = videoBlackListDao.loadAll().stream().map(e -> e.getId()).filter(e -> e.equals(userId)).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(blackSet)) {
            return MapMessage.errorMessage("由于您提交的学习视频违规，目前账号处于异常状态，请稍后再试。");
        }

        Map<String, OrderProduct> orderProductMap = userOrderLoaderClient.loadOrderProducts(productIds);
        if (MapUtils.isEmpty(orderProductMap) || orderProductMap.size() != productIds.size()) {
            return MapMessage.errorMessage("未查询到产品信息");
        }

        OrderProduct notChipsProduct = orderProductMap.values().stream().filter(e -> !OrderProductServiceType.ChipsEnglish.name().equals(e.getProductType())).findFirst().orElse(null);
        if (notChipsProduct != null) {
            return MapMessage.errorMessage("不是薯条英语的产品");
        }

        if (productIds.size() == 1) {
            OrderProduct product = orderProductMap.get(productIds.get(0));
            boolean shortProduct = Optional.ofNullable(product)
                    .map(OrderProduct::getAttributes)
                    .filter(StringUtils::isNotBlank)
                    .map(JsonUtils::fromJson)
                    .map(e -> SafeConverter.toBoolean(e.get("short")))
                    .orElse(false);
            if (shortProduct) {
                int rank = Optional.ofNullable(product)
                        .map(OrderProduct::getAttributes)
                        .filter(StringUtils::isNotBlank)
                        .map(JsonUtils::fromJson)
                        .map(e -> SafeConverter.toInt(e.get("rank")))
                        .orElse(0);
                if (rank == 10 || rank == 9) {
                    int number = chipsContentService.loadShortProductRank8And9SurplusNumber();
                    if (number <= 0) {
                        return MapMessage.errorMessage("产品已经售罄");
                    }

                    Long count = chipsProductUserCountCacheManager.getCount(productIds.get(0));
                    List<ChipsEnglishClass> clazzList = chipsEnglishClassPersistence.loadByProductId(productIds.get(0));
                    int total = clazzList.stream().filter(e -> e.getUserLimit() != null).mapToInt(ChipsEnglishClass::getUserLimit).sum();
                    if (count.intValue() > total) {
                        return MapMessage.errorMessage("正在处理中，请稍后");
                    }
                }
            }
        }

        Map<String, List<OrderProductItem>> orderProductItemMap = userOrderLoaderClient.loadProductItemsByProductIds(productIds);
        if (MapUtils.isEmpty(orderProductItemMap)) {
            return MapMessage.errorMessage("未查询到产品信息");
        }

        List<OrderProductItem> orderProductItem = new ArrayList<>();
        orderProductItemMap.values().forEach(orderProductItem::addAll);
        if (CollectionUtils.isEmpty(orderProductItem)) {
            return MapMessage.errorMessage("未查询到产品信息");
        }

        List<ChipsUserCourse> userCourseList = chipsUserService.loadUserEffectiveCourseIncludeUnactive(userId);
        if (CollectionUtils.isNotEmpty(userCourseList)) {
            Set<String> productSet = productIds.stream().collect(Collectors.toSet());
            ChipsUserCourse ext = userCourseList.stream().filter(e -> productSet.contains(e.getProductId())).findFirst().orElse(null);
            if (ext != null) {
                return MapMessage.errorMessage("已经购买相同产品，请勿重复购买").setErrorCode("100010").set("oid", ext.genUserOrderId());
            }

            Set<String> itemSet = orderProductItem.stream().map(OrderProductItem::getId).collect(Collectors.toSet());
            ext = userCourseList.stream().filter(e -> itemSet.contains(e.getProductItemId())).findFirst().orElse(null);
            if (ext != null) {
                return MapMessage.errorMessage("已经购买相同产品，请勿重复购买").setErrorCode("100010").set("oid", ext.genUserOrderId());
            }

            List<String> bookList = orderProductItem.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList());
            Map<String, OrderProductItem> userItemMap = userOrderLoaderClient.loadOrderProductItems(userCourseList.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet()));
            if (MapUtils.isNotEmpty(userItemMap)) {
                Map<String, List<String>> mutexMap = chipsContentService.loadBookMutexMap();
                for (ChipsUserCourse userCourse : userCourseList) {
                    OrderProductItem item = userItemMap.get(userCourse.getProductItemId());
                    if (item == null) {
                        continue;
                    }
                    List<String> books = mutexMap.get(item.getAppItemId());
                    if (CollectionUtils.isEmpty(books)) {
                        continue;
                    }
                    boolean hasInter = CollectionExtUtil.hasIntersection(books, bookList);
                    if (hasInter) {
                        ext = userCourse;
                        break;
                    }
                }
            }

            if (ext != null) {
                return MapMessage.errorMessage("已经购买相同产品，请勿重复购买").setErrorCode("100010").set("oid", ext.genUserOrderId());
            }
        }

        if (StringUtils.isNotBlank(chipsOrder.getGroupCode()) && !GROUP_SHOPPING_SPONSOR_SIGN.equals(chipsOrder.getGroupCode())) {
            ChipsGroupShopping chipsGroupShopping = chipsGroupShoppingPersistence.loadByCode(chipsOrder.getGroupCode());
            if (chipsGroupShopping == null) {
                return MapMessage.errorMessage("拼团码不存在").setErrorCode("100010");
            }

            if (chipsGroupShopping.getSponsor().equals(chipsOrder.getUserId())) {
                return MapMessage.errorMessage("不能拼自己的团").setErrorCode("100011");
            }
        }

        BigDecimal decu = BigDecimal.ZERO;
        if (StringUtils.isNotBlank(chipsOrder.getGroupCode())) {
            decu = new BigDecimal(300).multiply(new BigDecimal(productIds.size()));
            double productPrice = orderProductMap.values().stream().mapToDouble(e -> e.getPrice().doubleValue()).sum();
            decu = decu.doubleValue() < productPrice ? decu : BigDecimal.ZERO;
        }

        MapMessage message = userOrderServiceClient.getUserOrderService().createAppOrderByReduceAmount(userId,
                OrderProductServiceType.ChipsEnglish.name(), productIds, chipsOrder.getProductName(), StringUtils.isBlank(chipsOrder.getRefer()) ? "" : chipsOrder.getRefer(),
                chipsOrder.getChannel(), OrderType.chips_english.name(), decu);
        if (message.isSuccess() && (chipsOrder.getInviter() != null && chipsOrder.getInviter().compareTo(0L) > 0 || StringUtils.isNotBlank(chipsOrder.getSaleStaffId())) || StringUtils.isNotBlank(chipsOrder.getGroupCode())) {
            String orderId = SafeConverter.toString(message.get("orderId"));
            try {
                ChipsUserOrderExt chipsUserOrderExt = new ChipsUserOrderExt(orderId, userId);
                chipsUserOrderExt.setInviter(chipsOrder.getInviter());
                chipsUserOrderExt.setSaleStaffId(chipsOrder.getSaleStaffId());
                if (StringUtils.isNotBlank(chipsOrder.getGroupCode())) {
                    chipsUserOrderExt.setSponsor(false);
                    String groupCode = chipsOrder.getGroupCode();
                    if (GROUP_SHOPPING_SPONSOR_SIGN.equals(groupCode)) {
                        chipsUserOrderExt.setSponsor(true);
                        String order = orderId.split(UserOrder.SEP)[0];
                        groupCode = StringExtUntil.md5(order + userId.toString());
                        chipsGroupShoppingPersistence.insertOrUpdate(userId, order, groupCode);
                    }
                    chipsUserOrderExt.setGroupShoppingCode(groupCode);
                }
                if (chipsOrder.getInviter() != null) {
                    for (String product : productIds) {
                        chipsActivityInvitationPersistence.inserOrUpdate(chipsOrder.getInviter(), chipsOrder.getUserId(), product, 1);
                    }
                }
                chipsUserOrderExtDao.upsert(chipsUserOrderExt);
            } catch (Exception e) {
                logger.error("order update ext error. orderId:{}", orderId, e);
            }
        }
        return message;
    }

    @Override
    public MapMessage openSuperUser(Set<String> mobileSet, Set<String> productSet) {
        if (CollectionUtils.isEmpty(mobileSet) || CollectionUtils.isEmpty(productSet)) {
            return MapMessage.errorMessage().add("info", "mobile is null or product is null");
        }
        MapMessage message = MapMessage.successMessage();
        //如果value为0 表示没有根据该手机号查询到用户
        Map<String, Long> mobileToUserIdMap = mobileSet.stream().collect(Collectors.toMap(Function.identity(),
                m -> Optional.ofNullable(userLoaderClient.loadMobileAuthentication(m, UserType.PARENT)).map(UserAuthentication::getId).orElse(0l)));
        //没有查询到用户的手机号
        List<String> noUserMobileList = mobileSet.stream().filter(m -> mobileToUserIdMap.get(m) == null || mobileToUserIdMap.get(m) == 0l).collect(Collectors.toList());
        Set<Long> mobileUserList = mobileToUserIdMap.values().stream().filter(u -> u != null && u != 0l).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(noUserMobileList)) {
            message.add("noUserMobileList", noUserMobileList);
        }
        //插入ChipsUserCourse
        updateChipsUserCourse(productSet, mobileUserList);
        //添加到白名单
        updateChipsSuperUser(mobileUserList);
        return message;
    }

    private void updateChipsUserCourse(Set<String> productSet, Set<Long> mobileUserList) {
        Map<String, List<OrderProductItem>> productToItemMap = userOrderLoaderClient.loadProductItemsByProductIds(productSet);
        productToItemMap.forEach((k, v) ->
                v.forEach(item -> {
                    mobileUserList.forEach(u -> {
                        ChipsUserCourse userCourse = new ChipsUserCourse();
                        userCourse.setUserId(u);
                        userCourse.setProductId(k);
                        userCourse.setProductItemId(item.getId());
                        userCourse.setOrderId("crm");
                        userCourse.setOriginalProductId(k);
                        userCourse.setOriginalProductItemId(item.getId());
                        userCourse.setServiceBeginDate(new Date());
                        userCourse.setServiceEndDate(DateUtils.addYears(new Date(), 3));
                        userCourse.setOperation(ChipsUserCourse.Operation.CREATE);
                        userCourse.setDisabled(false);
                        userCourse.setCreateTime(new Date());
                        userCourse.setUpdateTime(new Date());
                        chipsUserCoursePersistence.insertOrUpdate(userCourse);
                    });
                })
        );
    }

    /**
     * 更新白名单
     *
     * @param mobileUserList
     */
    private void updateChipsSuperUser(Set<Long> mobileUserList) {
        ChipsEnglishPageContentConfig config = chipsEnglishConfigService.loadChipsConfigByName(Chips_Super_User);
        if (config == null) {
            return;
        }
        String value = config.getValue();
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotBlank(value)) {
            String[] split = value.split(",");
            Set<String> valueSet = Arrays.stream(split).collect(Collectors.toSet());
            if (valueSet.size() != split.length) {//之前有重复的，去除掉重复的
                sb.append(StringUtils.join(valueSet, ","));
            } else {
                sb.append(value);
            }
            mobileUserList.stream().filter(u -> !valueSet.contains(u.toString())).forEach(u -> sb.append(",").append(u));
            config.setValue(sb.toString());
        } else {
            config.setValue(StringUtils.join(mobileUserList, ","));
        }
        chipsEnglishConfigService.updateChipsEnglishPageContentConfig(config.getId(), config.getName(), config.getValue(), config.getMemo());
    }


}

