package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsInvitionRewardLoader;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsInvitionRankCacheManager;
import com.voxlearning.utopia.service.ai.data.ChipsRank;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.ai.impl.support.UserInfoSupport;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.ai.util.CollectionExtUtil;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2019/3/7
 */
@Named
@ExposeService(interfaceClass = ChipsInvitionRewardLoader.class)
public class ChipsInvitionRewardLoaderImpl implements ChipsInvitionRewardLoader {
    @Inject
    private ChipsActivityInvitationPersistence chipsActivityInvitationPersistence;
    @Inject
    private ChipsActivityInvitationVisitPersistence chipsActivityInvitationVisitPersistence;
    @Inject
    private ChipsActivityTransactionFlowPersistence chipsActivityTransactionFlowPersistence;
    @Inject
    private ChipsInvitionRankCacheManager chipsInvitionRankCacheManager;

    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private ChipsWechatUserPersistence wechatUserPersistence;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private ChipsContentService chipsContentService;

    @Inject
    private ChipsUserService chipsUserService;

    @Inject
    private ChipsEnglishProductTimetableDao timetableDao;

    @Override
    public MapMessage loadInvitionConfig() {
        Map<String, Object> map = chipsContentService.loadActivityConfig("invite");
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.putAll(map);
        return mapMessage;
    }

    @Override
    public MapMessage loadInvitionConfigDetail(Long userId) {
        Map<String, Object> map = chipsContentService.loadActivityConfig("invite");
        String productId = SafeConverter.toString(map.get("productId"));
        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
        if (orderProduct == null) {
            return MapMessage.errorMessage("产品不存在");
        }

        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.putAll(map);
        mapMessage.set("productName", orderProduct.getName());
        mapMessage.set("price", orderProduct.getPrice());
        mapMessage.set("originalPrice", orderProduct.getOriginalPrice());
        ChipsEnglishProductTimetable timetable = timetableDao.load(productId);
        mapMessage.put("beginDate",  DateUtils.dateToString(timetable.getBeginDate(), "yyyy年MM月dd日"));
        mapMessage.set("endDate", DateUtils.dateToString(timetable.getEndDate(), "yyyy年MM月dd日"));
        Date sellOutDate = Optional.ofNullable(map)
                .map(ma -> SafeConverter.toString(ma.get("sellOutDate")))
                .map(DateUtils::stringToDate)
                .orElse(DateUtils.addDays(timetable.getBeginDate(), -2));
        mapMessage.set("sellOut", sellOutDate.before(new Date()));
        mapMessage.set("sellOutDate",  DateUtils.dateToString(sellOutDate, "yyyy年MM月dd日"));
        boolean payed = false;
        if (userId != null) {
            List<ChipsUserCourse> chipsUserCourseList = chipsUserService.loadUserEffectiveCourse(userId);
            payed = Optional.ofNullable(chipsUserCourseList)
                    .filter(CollectionUtils::isNotEmpty)
                    .map(list -> {
                        ChipsUserCourse ext = list.stream().filter(e -> productId.contains(e.getProductId())).findFirst().orElse(null);
                        if (ext != null) {
                            return true;
                        }
                        Set<String> myItems = list.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet());
                        List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
                        OrderProductItem item = itemList.stream().filter(it -> myItems.contains(it)).findFirst().orElse(null);
                        if (item != null) {
                            return true;
                        }
                        Map<String, List<String>> mutexMap = chipsContentService.loadBookMutexMap();
                        Set<String> mybooks = userOrderLoaderClient.loadAllOrderProductItems().stream().filter(e -> myItems.contains(e)).map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
                        List<String> books = itemList.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList());
                        for (String book : mybooks) {
                            List<String> mutexBooks = mutexMap.get(book);
                            if (CollectionUtils.isEmpty(mutexBooks)) {
                                continue;
                            }
                            boolean hasInter = CollectionExtUtil.hasIntersection(mutexBooks, books);
                            if (hasInter) {
                                return true;
                            }
                        }

                        return false;
                    })
                    .orElse(false);
        }
        return mapMessage.set("payed", payed);
    }

    @Override
    public MapMessage loadInvitionIndexData(Long userId) {
        Map<String, Object> map = chipsContentService.loadActivityConfig("invite");
        MapMessage mapMessage = MapMessage.successMessage();
        if (MapUtils.isNotEmpty(map)) {
            String productId = SafeConverter.toString(map.get("productId"));
            mapMessage.putAll(loadInvitionActivityTopUser(productId));
        }
        String url = Optional.ofNullable(map)
                .map(ma -> SafeConverter.toString(ma.get("linkUrl")))
                .map(ur -> ur + "?inviter=" + userId)
                .orElse("");
        User user = userLoaderClient.loadUser(userId, UserType.PARENT);
        String avatar = Optional.ofNullable(user).map(e -> UserInfoSupport.getUserRoleImage(e, "http://cdn.17zuoye.com/fs-resource/5c8f6e809831c741caaea393.png")).orElse("");
        mapMessage.put("avatar", avatar);
        mapMessage.put("linkUrl", url);
        Long beginDate = Optional.ofNullable(map)
                .map(e -> e.get("acBeginDate"))
                .map(SafeConverter::toString)
                .map(DateUtils::stringToDate)
                .map(Date::getTime)
                .orElse(0L);
        Long endDate = Optional.ofNullable(map)
                .map(e -> e.get("acEndDate"))
                .map(SafeConverter::toString)
                .map(DateUtils::stringToDate)
                .map(Date::getTime)
                .orElse(0L);
        mapMessage.set("acBeginDate", beginDate);
        mapMessage.set("acEndDate", endDate);
        return mapMessage;
    }

    @Override
    public MapMessage loadInvitionActivityTopUser(String activityType) {
        if (StringUtils.isBlank(activityType)) {
            return MapMessage.errorMessage("参数为空");
        }
        List<ChipsRank> rankList = chipsInvitionRankCacheManager.getRankList(activityType, 20);
        List<Map<String, Object>> list = rankList.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", e.getUserId());
            map.put("rolling", getPhone(e.getUserId()));
            map.put("count", e.getNumber());
            return map;
        }).collect(Collectors.toList());
        MapMessage message = MapMessage.successMessage();
        Map<String, Object> cfg = chipsContentService.loadActivityConfig("invite");
        message.putAll(cfg);
        message.set("data", list);
        return message;
    }

    @Override
    public MapMessage loadMyReward(Long userId) {
        if(userId == null) {
            return MapMessage.errorMessage("参数为空");
        }

        MapMessage message = MapMessage.successMessage();
        Map<Long, List<ChipsActivityInvitationVisit>> visitMap = chipsActivityInvitationVisitPersistence.loadByUserId(userId).stream().collect(Collectors.groupingBy(ChipsActivityInvitationVisit::getAuthorizationId));
        message.add("invitNum", visitMap.size());//已浏览好友

        List<ChipsActivityInvitation> invitationList = chipsActivityInvitationPersistence.loadByInviter(userId);
        List<ChipsActivityInvitation> paidList = invitationList.stream().filter(e -> e != null && e.getStatus() != null && e.getStatus() == 2).collect(Collectors.toList());
        message.add("totalAcount", paidList.size() * 2.97);//累计分红
        message.add("paidNum", paidList.size());//邀请成功购买好友

        List<ChipsActivityInvitation> todayPaidList = paidList.stream().filter(e -> e.getCreateTime().after(DayRange.current().getStartDate())).collect(Collectors.toList());
        message.add("todayAcount", todayPaidList.size() * 2.97);//今日总分红

        List<ChipsActivityInvitation> noPaidList = invitationList.stream().filter(e -> e != null && e.getStatus() != null && e.getStatus() == 1).collect(Collectors.toList());
        message.add("noPaidNum", noPaidList.size());//未付款好友

        List<ChipsActivityTransactionFlow> flowList = chipsActivityTransactionFlowPersistence.loadByUserId(userId);
        Double drawableAcount = flowList.stream().filter(e -> e.getOperation() == 0).map(e -> e.getAmount()).reduce((x, y) -> x + y).orElse(0.0);
        message.add("drawableAcount", drawableAcount);//可提现金额


        String image = Optional.ofNullable(wechatUserPersistence.loadByUserId(userId)).map(list -> list.stream().findFirst().orElse(null))
                .map(ChipsWechatUserEntity::getAvatar).orElse("");
        message.add("image",image);
        message.add("flag", CollectionUtils.isNotEmpty(paidList));
        return message;
    }

    @Override
    public MapMessage loadInvitionDetail(Long userId, Integer type) {
        if (type == null) {
            return MapMessage.errorMessage().add("info", "不支持的类型");
        }
        if (type == 0) {
            return handleVisit(userId);
        } else if (type == 1 || type == 2) {
            return handlePaid(userId, type);
        }
        return MapMessage.errorMessage().add("info", "不支持的类型");
    }

    private MapMessage handleVisit(Long userId) {
        List<ChipsActivityInvitationVisit> visitList = chipsActivityInvitationVisitPersistence.loadByUserId(userId);
        Set<Long> list = visitList.stream().map(e -> e.getAuthorizationId()).collect(Collectors.toSet());
        Map<Long, ChipsWechatUserEntity> wechatUserEntityMap = wechatUserPersistence.loads(list);
        MapMessage message = MapMessage.successMessage();
        message.add("count", list.size());
        if (CollectionUtils.isEmpty(list) || MapUtils.isEmpty(wechatUserEntityMap)) {
            message.add("data", Collections.emptyList());
            return message;
        }
        List<Map<String, Object>> mapList = buildDetailMap(list, wechatUserEntityMap);
        message.add("data", mapList);
        return message;
    }

    private MapMessage handlePaid(Long userId, int type) {
        List<ChipsActivityInvitation> invitationList = chipsActivityInvitationPersistence.loadByInviter(userId);
        List<Long> list = invitationList.stream().filter(e -> e.getStatus() == type).map(e -> e.getInvitee()).collect(Collectors.toList());
        Map<Long, List<ChipsWechatUserEntity>> wechatUserEntityMap = wechatUserPersistence.loadByUserIds(list);
        MapMessage message = MapMessage.successMessage();
        message.add("count", list.size());
        if (CollectionUtils.isEmpty(list) || MapUtils.isEmpty(wechatUserEntityMap)) {
            message.add("data", Collections.emptyList());
            return message;
        }
        List<Map<String, Object>> mapList = buildDetailMap(wechatUserEntityMap, list);
        message.add("data", mapList);
        return message;
    }

    private List<Map<String, Object>> buildDetailMap(Map<Long, List<ChipsWechatUserEntity>> wechatUserEntityMap, List<Long> userList) {
        if (CollectionUtils.isEmpty(userList) || MapUtils.isEmpty(wechatUserEntityMap)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for(Long u : userList) {
            Map<String, Object> map = new HashMap<>();
            ChipsWechatUserEntity userEntity = Optional.ofNullable(wechatUserEntityMap.get(u)).map(list -> list.stream().findFirst().orElse(null)).orElse(null);
            if (userEntity == null) {
                continue;
            }
            map.put("image", userEntity.getAvatar());
            map.put("nickName", userEntity.getNickName());
            mapList.add(map);
        }
        return mapList;
    }


    private List<Map<String, Object>> buildDetailMap(Set<Long> wechatUserIds, Map<Long, ChipsWechatUserEntity> wechatUserEntityMap) {
        if (CollectionUtils.isEmpty(wechatUserIds) || MapUtils.isEmpty(wechatUserEntityMap)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Long u: wechatUserIds) {
            Map<String, Object> map = new HashMap<>();
            ChipsWechatUserEntity userEntity = wechatUserEntityMap.get(u);
            if (userEntity == null) {
                continue;
            }
            map.put("image", userEntity.getAvatar());
            map.put("nickName", userEntity.getNickName());
            mapList.add(map);
        }
        return mapList;
    }

    private String getPhone(Long userId) {
        String phone = "";
        if (userId != null) {
            User user = userLoaderClient.loadUser(userId, UserType.PARENT);
            if (user != null) {
                phone = sensitiveUserDataServiceClient.loadUserMobile(userId);
            }
        }
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotBlank(phone) && phone.length() == 11) {
            for (int i = 0; i < 11; i++) {
                if (i > 2 && i < 7) {
                    sb.append("*");
                } else {
                    sb.append(phone.charAt(i));
                }
            }
            return sb.toString();
        }
        return "";
    }


}
