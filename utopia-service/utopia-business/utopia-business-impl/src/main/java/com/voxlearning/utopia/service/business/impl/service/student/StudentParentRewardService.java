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

package com.voxlearning.utopia.service.business.impl.service.student;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.MissionType;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.entity.mission.MissionProgress;
import com.voxlearning.utopia.service.business.impl.service.AsyncBusinessCacheServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.student.spr.*;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.campaign.client.MissionServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.WishType.INTEGRAL;
import static com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType.RemindRewardNotice;
import static com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType.RemindUpdateProgressNotice;
import static com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType.TEMPLATE_REMIND_REWARD;
import static com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType.TEMPLATE_REMIND_UPDATE_PROGRESS;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/9/2015
 */
@Named
@Slf4j
public class StudentParentRewardService extends BusinessServiceSpringBean {

    @Inject private AsyncBusinessCacheServiceImpl asyncBusinessCacheService;
    @Inject private MissionServiceClient missionServiceClient;

    @Inject private MissionCreationTemplateManager missionCreationTemplateManager;
    @Inject private WishCreationTemplateManager wishCreationTemplateManager;

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    public MapMessage studentMakeWish(Long studentId, WishType wishType, String wish) {
        WishCreationContext context = WishCreationContext.of(wishType).withUserId(studentId).withIntegral(10).withWish(wish);
        WishCreationTemplate template = wishCreationTemplateManager.get(context.getType());
        if (null == template) {
            logger.error("CANNOT RECOGNIZE WISHTYPE {}, STUDENTID {}", wishType.name(), studentId);
            return MapMessage.errorMessage("提交失败");
        }
        return template.create(context);
    }

    public MapMessage studentSendWechatNotice(Long studentId, Long missionId, String template) {
        if (null == studentId || null == missionId) {
            return MapMessage.errorMessage("操作失败");
        }
        User student = userLoaderClient.loadUser(studentId);
        if (null == student) {
            return MapMessage.errorMessage("请先登录");
        }
        Mission mission = missionServiceClient.getMissionService().loadMission(missionId).getUninterruptibly();
        if (null == mission) {
            return MapMessage.errorMessage("请先许愿");
        }
        WechatNoticeType type = WechatNoticeType.of(template);
        if (type != TEMPLATE_REMIND_UPDATE_PROGRESS && type != TEMPLATE_REMIND_REWARD) {
            return MapMessage.errorMessage("发送失败");
        }
        // 判断是否有绑定微信的家长
        List<StudentParentRef> parents = studentLoaderClient.loadStudentParentRefs(studentId);
        if (CollectionUtils.isEmpty(parents)) {
            return MapMessage.errorMessage("请先绑定家长");
        }
        List<Long> parentIds = parents.stream().map(StudentParentRef::getParentId).collect(Collectors.toList());
        Map<Long, List<UserWechatRef>> parentsWechat = wechatLoaderClient.loadUserWechatRefs(parentIds, WechatType.PARENT);
        String month = DateUtils.dateToString(new Date(), DateUtils.FORMAT_YEAR_MONTH);
        boolean integralUsed = missionServiceClient.getMissionService()
                .findMissionIntegralLogs(student.getId())
                .getUninterruptibly()
                .stream()
                .filter(t -> StringUtils.equals(month, t.getMonth()))
                .count() > 0;
        switch (type) {
            case TEMPLATE_REMIND_UPDATE_PROGRESS: {
                Map<String, Object> extensionInfo = new HashMap<>();
                extensionInfo.put("studentName", student.fetchRealname());
                extensionInfo.put("mission", mission.formalizeMissionContent());
                extensionInfo.put("integralUsed", integralUsed);
                extensionInfo.put("studentId", studentId);
                extensionInfo.put("missionId", missionId);
                for (Long parentId : parentsWechat.keySet()) {
                    for (UserWechatRef ref : parentsWechat.get(parentId)) {
                        wechatServiceClient.processWechatNotice(RemindUpdateProgressNotice,
                                parentId, ref.getOpenId(), extensionInfo);
                    }
                }
                break;
            }
            case TEMPLATE_REMIND_REWARD: {
                Map<String, Object> extensionInfo = new HashMap<>();
                extensionInfo.put("studentName", student.fetchRealname());
                extensionInfo.put("wish", mission.formalizeWishContent());
                extensionInfo.put("mission", mission.formalizeMissionContent());
                extensionInfo.put("integralUsed", integralUsed);
                extensionInfo.put("studentId", studentId);
                extensionInfo.put("missionId", missionId);
                for (Long parentId : parentsWechat.keySet()) {
                    for (UserWechatRef ref : parentsWechat.get(parentId)) {
                        wechatServiceClient.processWechatNotice(RemindRewardNotice,
                                parentId, ref.getOpenId(), extensionInfo);
                    }
                }
                break;
            }
            default:
                return MapMessage.errorMessage("发送失败");
        }
        // 一天一次
        asyncBusinessCacheService.StudentMissionNoticeCacheManager_record(studentId, missionId, type.name())
                .awaitUninterruptibly();
        return MapMessage.successMessage();
    }

    public MapMessage studentCheckDetail(Long studentId, Long missionId) {
        if (null == studentId || null == missionId) {
            return MapMessage.errorMessage("查询失败");
        }
        List<MissionProgress> mps = missionServiceClient.getMissionService()
                .findMissionProgressList(missionId)
                .getUninterruptibly();
        List<Map<String, Object>> detail = new ArrayList<>();
        for (int i = 0; i < mps.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            MissionProgress mp = mps.get(i);
            map.put("rank", mps.size() - i);
            map.put("date", DateUtils.dateToString(mp.getCreateDatetime(), "MM月dd日 HH点mm分"));
            detail.add(map);
        }
        return MapMessage.successMessage().add("detail", detail);
    }

    public MapMessage parentSetMission(Long parentId, Long studentId, WishType wishType, String wish,
                                       Integer totalCount, String mission, MissionType missionType, Long missionId) {
        MissionCreationContext context = MissionCreationContext.of(wishType, missionType)
                .withStudentId(studentId).withParentId(parentId).withTotalCount(totalCount)
                .withMission(mission).withIntegral(10).withWish(wish).withMissionId(missionId);
        MissionCreationTemplate template = missionCreationTemplateManager.get(context.getWishType());
        if (null == template) {
            logger.error("CANNOT RECOGNIZE WISHTYPE {}, STUDENTID {}, PARENTID {}", wishType.name(), studentId, parentId);
            return MapMessage.errorMessage("设置任务失败");
        }
        return template.create(context);
    }

    public MapMessage parentUpdateProgress(Long parentId, Long missionId) {
        if (null == parentId || null == missionId) {
            return MapMessage.errorMessage("操作失败");
        }
        User parent = userLoaderClient.loadUser(parentId);
        if (null == parent) {
            return MapMessage.errorMessage("操作失败");
        }
        Mission mission = missionServiceClient.getMissionService().loadMission(missionId).getUninterruptibly();
        if (null == mission) {
            return MapMessage.errorMessage("操作失败");
        }
        // 判断家长和学生关系的正确性
        List<StudentParentRef> parents = studentLoaderClient.loadStudentParentRefs(mission.getStudentId());
        Set<Long> parentIds = parents.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet());
        if (!parentIds.contains(parentId)) {
            return MapMessage.errorMessage("您不是这个孩子的家长");
        }
        if (mission.getFinishCount() >= mission.getTotalCount()) {
            return MapMessage.errorMessage("任务已经完成，请给学生发奖励");
        }
        String pattern;
        if (mission.getTotalCount() - mission.getFinishCount() > 1) {
            pattern = "任务：{0} {1}次，已经完成了{2}次，继续努力就可以获得{3}哟";
        } else {
            pattern = "任务：{0} {1}次，已经完成了{2}次，让家长兑现{3}吧";
        }
        if (missionServiceClient.getMissionService().increaseMissionFinishCount(missionId, 1).getUninterruptibly()) {
            // 暂不开放完成任务的时间详情功能
            // missionProgressPersistence.persist(MissionProgress.newInstance(missionId));
            messageCommandServiceClient.getMessageCommandService().sendUserMessage(mission.getStudentId(), MessageFormat.format(pattern,
                    mission.getMission(), mission.getTotalCount(), mission.getFinishCount() + 1, mission.formalizeWishContent()));
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }

    }

    public MapMessage parentUpdateComplete(Long parentId, Long missionId) {
        if (null == parentId || null == missionId) {
            return MapMessage.errorMessage("操作失败");
        }
        User parent = userLoaderClient.loadUser(parentId);
        if (null == parent) {
            return MapMessage.errorMessage("操作失败");
        }
        Mission mission = missionServiceClient.getMissionService().loadMission(missionId).getUninterruptibly();
        if (null == mission) {
            return MapMessage.errorMessage("操作失败");
        }
        // 判断家长和学生关系的正确性
        List<StudentParentRef> parents = studentLoaderClient.loadStudentParentRefs(mission.getStudentId());
        Set<Long> parentIds = new HashSet<>();
        for (StudentParentRef ref : parents) {
            parentIds.add(ref.getParentId());
        }
        if (!parentIds.contains(parentId)) {
            return MapMessage.errorMessage("您不是这个孩子的家长");
        }
        if (mission.getFinishCount() < mission.getTotalCount()) {
            return MapMessage.errorMessage("任务还未完成");
        }
        if (missionServiceClient.getMissionService().updateMissionComplete(missionId).getUninterruptibly()) {
            // 发通知
            String pattern = "恭喜你！家长已经兑现了承诺：{0}";
            messageCommandServiceClient.getMessageCommandService().sendUserMessage(mission.getStudentId(), MessageFormat.format(pattern, mission.formalizeWishContent()));
            // 如果奖励是学豆，发学豆
            if (mission.getWishType() == INTEGRAL) {
                IntegralHistory integralHistory = new IntegralHistory(mission.getStudentId(), IntegralType.家长奖励, mission.getIntegral());
                integralHistory.setComment("家长奖励学豆");
                integralHistory.setRelationUserIdUniqueKey(parentId);
                userIntegralService.changeIntegral(integralHistory);
            }
        }
        // 删除详情暂不开放
        // missionProgressPersistence.deleteByMissionId(missionId);
        return MapMessage.successMessage();
    }

    public boolean isCurrentMonthIntegralMissionArranged(Long studentId) {
        if (studentId == null) return true;
        String month = DateUtils.dateToString(new Date(), DateUtils.FORMAT_YEAR_MONTH);
        return missionServiceClient.getMissionService()
                .findMissionIntegralLogs(studentId)
                .getUninterruptibly()
                .stream()
                .filter(t -> StringUtils.equals(month, t.getMonth()))
                .count() > 0;
    }

    // private methods

    public void postParentMessage(Long studentId, Long missionId, WechatNoticeType wechatNoticeType) {
//        AlpsThreadPool.getInstance().submit(() -> {
//            if (null == studentId || null == missionId) {
//                return;
//            }
//            User student = userLoaderClient.loadUser(studentId);
//            if (null == student) {
//                return;
//            }
//            Mission mission = missionServiceClient.getMissionService().loadMission(missionId).getUninterruptibly();
//            if (null == mission) {
//                return;
//            }
//            if (wechatNoticeType != TEMPLATE_REMIND_UPDATE_PROGRESS && wechatNoticeType != TEMPLATE_REMIND_REWARD) {
//                return;
//            }
//            String month = DateUtils.dateToString(new Date(), DateUtils.FORMAT_YEAR_MONTH);
//            boolean integralUsed = missionServiceClient.getMissionService()
//                    .findMissionIntegralLogs(student.getId())
//                    .getUninterruptibly()
//                    .stream()
//                    .filter(t -> StringUtils.equals(month, t.getMonth()))
//                    .count() > 0;
//            switch (wechatNoticeType) {
//                case TEMPLATE_REMIND_UPDATE_PROGRESS:
////                    parentMessageServiceClient.updateProgressRemind(student, mission.formalizeMissionContent(), parentLoaderClient);
//                    break;
//                case TEMPLATE_REMIND_REWARD:
////                    parentMessageServiceClient.rewardRemind(student, mission.formalizeWishContent(), integralUsed, parentLoaderClient);
//                    break;
//                default:
//                    break;
//            }
//        });

    }
}
