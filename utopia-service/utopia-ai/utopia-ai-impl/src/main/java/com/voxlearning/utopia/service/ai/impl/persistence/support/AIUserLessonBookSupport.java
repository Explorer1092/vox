package com.voxlearning.utopia.service.ai.impl.persistence.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonBookRef;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserLessonBookRefPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class AIUserLessonBookSupport {

    @Inject
    private AIUserLessonBookRefPersistence aiUserLessonBookRefPersistence;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    public AIUserLessonBookRef fetchUserCurrentBook(Long userId) {
        return aiUserLessonBookRefPersistence.loadByUserId(userId).stream()
                .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                .findFirst().orElse(null);
    }

    public AIUserLessonBookRef initNewUserBook(AppPayMapper appPayMapper, List<OrderProduct> orderProductList, Map<String, List<OrderProductItem>> orderProductItemMap, Long userId) {
        Date now = new Date();
        Set<String> productIds = orderProductList.stream().map(OrderProduct::getId).collect(Collectors.toSet());
        Map<String, ChipsEnglishProductTimetable> productTimetableMap = chipsEnglishProductTimetableDao.loads(productIds);
        orderProductList.sort((e1, e2) -> {
            Date beginDate1 = Optional.ofNullable(productTimetableMap.get(e1.getId())).map(ChipsEnglishProductTimetable::getBeginDate).orElse(now);
            Date beginDate2 = Optional.ofNullable(productTimetableMap.get(e2.getId())).map(ChipsEnglishProductTimetable::getBeginDate).orElse(now);
            return beginDate1.compareTo(beginDate2);
        });

        //先推荐在学的
        OrderProduct recomment = orderProductList.stream()
                .filter(e -> {
                    ChipsEnglishProductTimetable config = productTimetableMap.get(e.getId());
                    if (config == null || config.getBeginDate() == null || config.getEndDate() == null) {
                        return false;
                    }
                    return config.getBeginDate().before(now) && config.getEndDate().after(now);
                }).findFirst().orElse(null);
        OrderProductItem recommentItem = Optional.ofNullable(recomment)
                .map(e -> orderProductItemMap.get(e.getId()))
                .map(e -> e.stream().filter(e1 -> appPayMapper.getValidItems().contains(e1.getId())).filter(e1 -> StringUtils.isNotBlank(e1.getAppItemId()))
                        .sorted(Comparator.comparing(OrderProductItem::getUpdateDatetime).reversed())
                        .findFirst().orElse(null))
                .orElse(null);

        //再推荐靠前的没有开课的
        if (recomment == null || recommentItem == null) {
            recomment = orderProductList.stream()
                    .filter(e -> {
                        ChipsEnglishProductTimetable config = productTimetableMap.get(e.getId());
                        if (config == null || config.getBeginDate() == null || config.getEndDate() == null) {
                            return false;
                        }
                        return config.getBeginDate().after(now);
                    }).findFirst().orElse(null);
           recommentItem = Optional.ofNullable(recomment)
                    .map(e -> orderProductItemMap.get(e.getId()))
                    .map(e -> e.stream().filter(e1 -> appPayMapper.getValidItems().contains(e1.getId()))
                            .filter(e1 -> StringUtils.isNotBlank(e1.getAppItemId()))
                            .sorted(Comparator.comparing(OrderProductItem::getUpdateDatetime).reversed()).findFirst().orElse(null))
                    .orElse(null);
        }
        // 最后根据时间排序取靠前的
        if (recomment == null || recommentItem == null) {
            orderProductList.sort((e1, e2) -> {
                Date beginDate1 = Optional.ofNullable(productTimetableMap.get(e1.getId())).map(ChipsEnglishProductTimetable::getBeginDate).orElse(now);
                Date beginDate2 = Optional.ofNullable(productTimetableMap.get(e2.getId())).map(ChipsEnglishProductTimetable::getBeginDate).orElse(now);
                return beginDate2.compareTo(beginDate1);
            });
            for(OrderProduct orderProduct : orderProductList) {
                List<OrderProductItem> orderProductItemList = orderProductItemMap.get(orderProduct.getId());
                if (CollectionUtils.isEmpty(orderProductItemList)) {
                    continue;
                }
                OrderProductItem item = orderProductItemList.stream().filter(e -> StringUtils.isNotBlank(e.getAppItemId()))
                        .sorted(Comparator.comparing(OrderProductItem::getUpdateDatetime).reversed())
                        .findFirst().orElse(null);
                if (item == null) {
                    continue;
                }
                if (appPayMapper.getValidItems().contains(item.getId())) {
                    recomment = orderProduct;
                    recommentItem = item;
                    break;
                }
            }
        }
        aiUserLessonBookRefPersistence.deleteByUser(userId);
        AIUserLessonBookRef userLessonBookRef = new AIUserLessonBookRef();
        userLessonBookRef.setUserId(userId);
        userLessonBookRef.setDisabled(false);
        userLessonBookRef.setUpdateTime(now);
        userLessonBookRef.setCreateTime(now);
        if (recomment != null) {
            userLessonBookRef.setProductId(recomment.getId());
            userLessonBookRef.setBookName(recommentItem.getName());
            userLessonBookRef.setBookId(recommentItem.getAppItemId());
            aiUserLessonBookRefPersistence.insertOrUpdate(userLessonBookRef);
        }
        return userLessonBookRef;
    }

    public AIUserLessonBookRef initNewUserBookV2(Set<String> productIds, Map<String, List<OrderProductItem>> orderProductItemMap, Long userId) {
        Date now = new Date();
        Map<String, ChipsEnglishProductTimetable> productTimetableMap = chipsEnglishProductTimetableDao.loads(productIds);
        List<String> orderProductList = new ArrayList<>(productIds);
        orderProductList.sort((e1, e2) -> {
            Date beginDate1 = Optional.ofNullable(productTimetableMap.get(e1)).map(ChipsEnglishProductTimetable::getBeginDate).orElse(now);
            Date beginDate2 = Optional.ofNullable(productTimetableMap.get(e2)).map(ChipsEnglishProductTimetable::getBeginDate).orElse(now);
            return beginDate1.compareTo(beginDate2);
        });

        //先推荐在学的
        String recomment = orderProductList.stream()
                .filter(e -> {
                    ChipsEnglishProductTimetable config = productTimetableMap.get(e);
                    if (config == null || config.getBeginDate() == null || config.getEndDate() == null) {
                        return false;
                    }
                    return config.getBeginDate().before(now) && config.getEndDate().after(now);
                }).findFirst().orElse(null);
        OrderProductItem recommentItem = Optional.ofNullable(recomment)
                .map(e -> orderProductItemMap.get(e))
                .map(e -> e.stream().filter(e1 -> StringUtils.isNotBlank(e1.getAppItemId()))
                        .sorted(Comparator.comparing(OrderProductItem::getUpdateDatetime).reversed())
                        .findFirst().orElse(null))
                .orElse(null);

        //再推荐靠前的没有开课的
        if (recomment == null || recommentItem == null) {
            recomment = orderProductList.stream()
                    .filter(e -> {
                        ChipsEnglishProductTimetable config = productTimetableMap.get(e);
                        if (config == null || config.getBeginDate() == null || config.getEndDate() == null) {
                            return false;
                        }
                        return config.getBeginDate().after(now);
                    }).findFirst().orElse(null);
            recommentItem = Optional.ofNullable(recomment)
                    .map(e -> orderProductItemMap.get(e))
                    .map(e -> e.stream().filter(e1 -> StringUtils.isNotBlank(e1.getAppItemId()))
                            .sorted(Comparator.comparing(OrderProductItem::getUpdateDatetime).reversed()).findFirst().orElse(null))
                    .orElse(null);
        }
        // 最后根据时间排序取靠前的
        if (recomment == null || recommentItem == null) {
            orderProductList.sort((e1, e2) -> {
                Date beginDate1 = Optional.ofNullable(productTimetableMap.get(e1)).map(ChipsEnglishProductTimetable::getBeginDate).orElse(now);
                Date beginDate2 = Optional.ofNullable(productTimetableMap.get(e2)).map(ChipsEnglishProductTimetable::getBeginDate).orElse(now);
                return beginDate2.compareTo(beginDate1);
            });
            for(String orderProduct : orderProductList) {
                List<OrderProductItem> orderProductItemList = orderProductItemMap.get(orderProduct);
                if (CollectionUtils.isEmpty(orderProductItemList)) {
                    continue;
                }
                OrderProductItem item = orderProductItemList.stream().filter(e -> StringUtils.isNotBlank(e.getAppItemId()))
                        .sorted(Comparator.comparing(OrderProductItem::getUpdateDatetime).reversed())
                        .findFirst().orElse(null);
                if (item != null) {
                    recomment = orderProduct;
                    recommentItem = item;
                    break;
                }
            }
        }

        aiUserLessonBookRefPersistence.deleteByUser(userId);
        AIUserLessonBookRef userLessonBookRef = new AIUserLessonBookRef();
        userLessonBookRef.setUserId(userId);
        userLessonBookRef.setDisabled(false);
        userLessonBookRef.setUpdateTime(now);
        userLessonBookRef.setCreateTime(now);
        if (StringUtils.isNotBlank(recomment)) {
            userLessonBookRef.setProductId(recomment);
            userLessonBookRef.setBookName(recommentItem.getName());
            userLessonBookRef.setBookId(recommentItem.getAppItemId());
            aiUserLessonBookRefPersistence.insertOrUpdate(userLessonBookRef);
        }
        return userLessonBookRef;
    }
}
