package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsInvitionRewardService;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsInvitionRankCacheManager;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.ai.data.ChipsRank;
import com.voxlearning.utopia.service.ai.entity.ChipsActivityInvitation;
import com.voxlearning.utopia.service.ai.entity.ChipsActivityInvitationVisit;
import com.voxlearning.utopia.service.ai.entity.ChipsActivityTransactionFlow;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserEntity;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsActivityInvitationPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsActivityInvitationVisitPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsActivityTransactionFlowPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserPersistence;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2019/3/7
 */
@Named
@ExposeService(interfaceClass = ChipsInvitionRewardService.class)
public class ChipsInvitionRewardServiceImpl implements ChipsInvitionRewardService {
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
    private ChipsContentService chipsContentService;

    @Override
    public MapMessage loadInvitionAwardHome(String activityType) {
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

    private String getPhone(Long userId) {
        String phone = "";
        if (userId != null) {
            User user = userLoaderClient.loadUserIncludeDisabled(userId);
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

    @Override
    public MapMessage updateInvitionRank(String activityType, Long userId, int num) {
        if (StringUtils.isBlank(activityType) || userId == null) {
            return MapMessage.errorMessage("参数为空");
        }
        Long count = chipsInvitionRankCacheManager.updateRank(activityType, userId, num);
        return MapMessage.successMessage().add("num", count);
    }

    @Override
    public MapMessage loadMyReward(String activityType, Long userId) {
        if (StringUtils.isBlank(activityType) || userId == null) {
            return MapMessage.errorMessage("参数为空");
        }

        MapMessage message = MapMessage.successMessage();
        List<ChipsActivityInvitationVisit> visitList = chipsActivityInvitationVisitPersistence.loadByActivityTypeAndUserId(activityType, userId);
        Map<Long, List<ChipsActivityInvitationVisit>> visitMap = visitList.stream().collect(Collectors.groupingBy(ChipsActivityInvitationVisit::getAuthorizationId));
        List<ChipsActivityInvitation> invitationList = chipsActivityInvitationPersistence.loadByActivityTypeAndInviter(activityType, userId);
        List<ChipsActivityInvitation> paidList = invitationList.stream().filter(e -> e != null && e.getStatus() != null && e.getStatus() == 2).collect(Collectors.toList());
        List<ChipsActivityInvitation> todayPaidList = paidList.stream().filter(e -> e.getCreateTime().after(DayRange.current().getStartDate())).collect(Collectors.toList());
        List<ChipsActivityInvitation> noPaidList = invitationList.stream().filter(e -> e != null && e.getStatus() != null && e.getStatus() == 1).collect(Collectors.toList());
        List<ChipsActivityTransactionFlow> flowList = chipsActivityTransactionFlowPersistence.loadByActivityTypeAndUserId(activityType, userId);
        Double drawableAcount = flowList.stream().filter(e -> e.getOperation() == 0).map(e -> e.getAmount()).reduce((x, y) -> x + y).orElse(0.0);

        message.add("todayAcount", todayPaidList.size() * 2.97);//今日总分红
        message.add("totalAcount", paidList.size() * 2.97);//累计分红
        message.add("drawableAcount", drawableAcount);//可提现金额
        message.add("invitNum", visitMap.size());//已浏览好友
        message.add("noPaidNum", noPaidList.size());//未付款好友
        message.add("paidNum", paidList.size());//邀请成功购买好友

        String image = Optional.ofNullable(wechatUserPersistence.loadByUserId(userId)).map(list -> list.stream().findFirst().orElse(null))
                .map(ChipsWechatUserEntity::getAvatar).orElse("");
        message.add("image",image);
        message.add("flag", CollectionUtils.isNotEmpty(paidList));
        return message;
    }

    /**
     * 已浏览:0,下单未支付:1,成功购买: 2,退款:3
     *
     * @param activityType
     * @param userId
     * @param type
     * @return
     */
    @Override
    public MapMessage loadInvitionDetail(String activityType, Long userId, Integer type) {
        if (type == null) {
            return MapMessage.errorMessage().add("info", "不支持的类型");
        }
        if (type == 0) {
            return handleVisit(activityType, userId);
        } else if (type == 1 || type == 2) {
            return handlePaid(activityType, userId, type);
        }
        return MapMessage.errorMessage().add("info", "不支持的类型");
    }

    @Override
    public MapMessage processInvitionPageVisit(String openId, Long inviter, String productId) {
        if (StringUtils.isAnyBlank(openId, productId) || inviter == null) {
            return MapMessage.errorMessage("参数为空");
        }

        ChipsWechatUserEntity userEntity = wechatUserPersistence.loadByOpenIdAndType(openId, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.getCode());
        if (userEntity == null) {
            return MapMessage.errorMessage("用户为空");
        }
        chipsActivityInvitationVisitPersistence.insert(productId, inviter, userEntity.getId());
        return MapMessage.successMessage();
    }

    private MapMessage handleVisit(String activityType, Long userId) {
        List<ChipsActivityInvitationVisit> visitList = chipsActivityInvitationVisitPersistence.loadByActivityTypeAndUserId(activityType, userId);
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

    private MapMessage handlePaid(String activityType, Long userId, int type) {
        List<ChipsActivityInvitation> invitationList = chipsActivityInvitationPersistence.loadByActivityTypeAndInviter(activityType, userId);
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
}
