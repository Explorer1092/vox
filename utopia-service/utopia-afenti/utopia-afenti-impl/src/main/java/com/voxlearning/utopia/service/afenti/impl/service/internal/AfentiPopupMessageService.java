package com.voxlearning.utopia.service.afenti.impl.service.internal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.afenti.api.constant.UseAppStatus;
import com.voxlearning.utopia.service.afenti.api.context.LoginContext;
import com.voxlearning.utopia.service.afenti.api.context.PopupTextContext;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardBufferLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardStatus;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardType;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardItem;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardLog;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType.school;

/**
 * 用户弹框相关服务
 *
 * @author peng.zhang.a
 * @since 16-7-29
 */
@Named
public class AfentiPopupMessageService extends UtopiaAfentiSpringBean {

    @Inject
    private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject
    AfentiLearningRankService afentiLearningRankService;
    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @Inject
    private ParentRewardBufferLoaderClient parentRewardBufferLoaderClient;

    public MapMessage fetchPopupMessage(StudentDetail studentDetail, Subject subject) {
        if (studentDetail == null || subject == null) return MapMessage.errorMessage("参数缺失");

        OrderProductServiceType orderProductServiceType = AfentiUtils.getOrderProductServiceType(subject);
        if (orderProductServiceType == null) return MapMessage.errorMessage("参数缺失");

        // 加载popup消息
        // 有限排行榜总结，其次邀请成功通知
        if (asyncAfentiCacheService.AfentiLastWeekUsedCacheManager_fetch(studentDetail.getId(), subject).take()) {
            Map<String, Object> rankPopupInfo = new HashMap<>();

            Long schoolId = studentDetail.getClazz() == null ? 0 : studentDetail.getClazz().getSchoolId();
            Date calculateDate = asyncAfentiCacheService.UserLearningRankCacheManager_lastWeekCalculateDate().take();
            Map<AfentiRankType, Map<Long, Integer>> userRankFlag = afentiLearningRankService.getUserRankFlag(subject, calculateDate, schoolId);
            Map<AfentiRankType, List<Map<String, Object>>> rankList = afentiLearningRankService.getRank(subject, calculateDate, schoolId);
            Integer nationRank = afentiLearningRankService.getUserNationRank(rankList, userRankFlag, studentDetail.getId());
            rankPopupInfo.put("schoolRank", userRankFlag.getOrDefault(school, Collections.emptyMap()).getOrDefault(studentDetail.getId(), 0));
            rankPopupInfo.put("nationalRank", nationRank);
            UserActivatedProduct history = userActivatedProductPersistence
                    .loadByUserIds(Collections.singleton(studentDetail.getId()))
                    .getOrDefault(studentDetail.getId(), Collections.emptyList())
                    .stream()
                    .filter(t -> OrderProductServiceType.safeParse(t.getProductServiceType()) == orderProductServiceType)
                    .findFirst()
                    .orElse(null);
            UseAppStatus useAppStatus;
            if (history == null) {
                useAppStatus = UseAppStatus.NotBuy;
            } else if (history.getServiceEndTime().after(new Date())) {
                useAppStatus = UseAppStatus.Using;
            } else {
                useAppStatus = UseAppStatus.Expired;
            }
            rankPopupInfo.put("useAppStatus", useAppStatus);
            return MapMessage.successMessage()
                    .set("rankPopupInfo", rankPopupInfo);
        } else {
            String msg = loadUserInvitationSuccessPopupMsg(studentDetail.getId(), subject);
            if (StringUtils.isNotEmpty(msg)) {
                return MapMessage.successMessage().set("invitationPopupInfo", msg);
            }
            return MapMessage.successMessage();
        }
    }

    private String loadUserInvitationSuccessPopupMsg(Long userId, Subject subject) {
        Set<Long> userIds = asyncAfentiCacheService.AfentiSuccessInviteRecordCacheManager_loadAndReset(userId, subject)
                .take();
        if (CollectionUtils.isEmpty(userIds)) return null;

        List<Long> list = userIds.stream().limit(3).collect(Collectors.toList());
        List<String> names = userLoaderClient.loadUsers(list).values().stream().map(User::fetchRealname).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(names)) {
            String name = StringUtils.join(names, ",");
            String nameMsg = names.size() > 3 ? name + "等" + userIds.size() + "位" : name;
            return StringUtils.formatMessage(PopupTextContext.INVITATION_SUCCESS_POPUP_MSG.desc, nameMsg, subject.getValue());
        }
        return null;
    }

    public MapMessage fetchParentRewardPopupMessage(LoginContext context) {
        if (context.getStudent() == null || context.getSubject() == null) return MapMessage.errorMessage("参数缺失");

        List<ParentRewardType> rewardTypes = AfentiUtils.getParentRewardType(context.getSubject());
        if (rewardTypes == null) return MapMessage.errorMessage("参数缺失");
        // 家长奖励获取popup消息
        List<ParentRewardLog> logList = parentRewardLoader.getParentRewardList(context.getStudent().getId(), ParentRewardStatus.SEND.getType());
        if (CollectionUtils.isEmpty(logList)) {
            return MapMessage.errorMessage();
        }
        Set<String> keys = rewardTypes.stream().map(ParentRewardType::name).collect(Collectors.toSet());
        logList = logList.stream()
                .filter(log -> keys.contains(log.getKey()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(logList)) {
            return MapMessage.errorMessage();
        }
        // 拼装返回的数据
        int totalPoints = 0;
        Set<ParentRewardItem> rewardTypeSet = new HashSet<>();
        for (ParentRewardLog log : logList) {
            if (StringUtils.isBlank(log.getKey())) continue;

            totalPoints = totalPoints + log.getCount();
            ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(log.getKey());
            if (item != null) {
                rewardTypeSet.add(item);
            }
        }
        List<Map<String, Object>> typeMapList = new ArrayList<>();
        for (ParentRewardItem type : rewardTypeSet) {
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put("code", type.getKey());
            typeMap.put("desc", type.getTitle());
            typeMapList.add(typeMap);
        }
        Map<String, Object> popupInfo = new HashMap<>();
        popupInfo.put("totalPoints", totalPoints);
        popupInfo.put("rewardTypeList", typeMapList);
        return MapMessage.successMessage().add("popupInfo", popupInfo);
    }
}
