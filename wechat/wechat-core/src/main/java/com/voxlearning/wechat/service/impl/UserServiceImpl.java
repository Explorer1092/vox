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

package com.voxlearning.wechat.service.impl;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.random.RandomGenerator;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.api.constant.NewUserTask;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.WechatUserCampaignServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import com.voxlearning.wechat.service.UserService;
import lombok.SneakyThrows;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Xin Xin
 * @since 10/23/15
 */
@Named
public class UserServiceImpl implements UserService {

    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Inject private WechatUserCampaignServiceClient wechatUserCampaignServiceClient;

    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private ParentServiceClient parentServiceClient;
    @Inject
    private UserServiceClient userServiceClient;
    @Inject
    private WechatServiceClient wechatServiceClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private SmsServiceClient smsServiceClient;

    @Override
    public MapMessage verifySmsCode(String mobile, String code, WechatType type) {
        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(code) || null == type) {
            return MapMessage.errorMessage("参数错误");
        }

        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效手机号");
        }

        SmsType smsType;
        if (WechatType.TEACHER == type) {
            smsType = SmsType.TEACHER_VERIFY_MOBILE_WEIXIN_REGISTER;
        } else if (WechatType.CHIPS == type) {
            smsType = SmsType.PARENT_VERIFY_MOBILE_CHIPS_WEIXIN_REGISTER;
        } else {
            smsType = SmsType.PARENT_VERIFY_MOBILE_WEIXIN_REGISTER;
        }

        return smsServiceClient.getSmsService().verifyValidateCode(mobile, code, smsType.name());
    }

    @Override
    public MapMessage bindStudentToParent(Long studentId, Long parentId, String callName) {
        if (null == studentId || null == parentId) { //callName可以传null
            return MapMessage.errorMessage("参数错误");
        }
        //检查家长关联的孩子数量是否超限
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
        if (!CollectionUtils.isEmpty(studentParentRefs) && studentParentRefs.size() >= 3) {
            return MapMessage.errorMessage("现在系统只支持最多绑定3个孩子，如果需要绑定更多的孩子，请联系客服");
        }

        //检查是否已经有关联关系了
        List<StudentParent> parents = parentLoaderClient.loadStudentParents(studentId);
        if (!CollectionUtils.isEmpty(parents) && parents.size() > 0) {
            parents = parents.stream().filter(p -> p.getParentUser().getId().longValue() == parentId.longValue()).collect(Collectors.toList());
            if (parents.size() > 0) {
                return MapMessage.successMessage();
            }
        }

        boolean keyParent = false;
        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(parentId);
        if ((userAuthentication != null && userAuthentication.isMobileAuthenticated())
                && parentLoaderClient.loadStudentKeyParent(studentId) == null) {
            // parent has mobile and student has no key parent
            keyParent = true;
        }

        MapMessage message = parentServiceClient.bindExistingParent(studentId, parentId, keyParent, callName);
        if (!message.isSuccess()) {
            return message;
        } else {
            // 完成学生新手任务
            User student = userLoaderClient.loadUser(studentId);
            asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .NewUserTaskCacheManager_completeStudentNewUserTask(student, NewUserTask.parentWechatBinded)
                    .awaitUninterruptibly();
            return MapMessage.successMessage();
        }
    }

    @Override
    public MapMessage registParentByMobile(String mobile) {
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(RoleType.ROLE_PARENT);
        neonatalUser.setUserType(UserType.PARENT);
        neonatalUser.setMobile(mobile);
        neonatalUser.setPassword(RandomGenerator.generatePlainPassword());
        MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
        if (!message.isSuccess()) {
            return message;
        }

        User parent = (User) message.get("user");
        if (!StringUtils.isBlank(mobile)) {
            MapMessage msg = userServiceClient.activateUserMobile(parent.getId(), mobile);
            if (!msg.isSuccess()) {
                return msg; //绑手机失败
            }
        }
        return message;
    }

    //绑定家长号并给学生发奖励
    @Override
    @SneakyThrows(InterruptedException.class)
    public void bindParentWithReward(Long parentId, Long studentId, String openId, String source) {
        CampaignType campaignType = wechatUserCampaignServiceClient.getWechatUserCampaignService().loadUserCampaign(studentId).get();
        if (null != campaignType) {
            source += "_" + campaignType;
        }

        //绑定家长号与微信号
        wechatServiceClient.bindUserAndWechat(parentId, openId, source, WechatType.PARENT.getType(), id -> completeStudentNewUserTask(parentId));
    }

    @Override
    public void unbindParent(String openId) {
        if (StringUtils.isBlank(openId)) return;

        wechatServiceClient.unbindUserAndWechat(openId, id -> cleanUpStudentNewUserTask(id));
    }


    @Override
    public Optional<Long> getParentByCallName(Long studentId, String callName) {
        if (null == studentId || StringUtils.isBlank(callName)) {
            return Optional.empty();
        }

        Map<Long, List<StudentParentRef>> map = studentLoaderClient.loadStudentParentRefs(Collections.singletonList(studentId));
        if (MapUtils.isEmpty(map)) {
            return Optional.empty();
        }
        List<StudentParentRef> refs = map.get(studentId);
        if (CollectionUtils.isEmpty(refs)) {
            return Optional.empty();
        }
        Optional<StudentParentRef> ref = refs.stream().filter(r -> r.getCallName().equals(callName)).findFirst();
        if (ref.isPresent()) {
            return Optional.of(ref.get().getParentId());
        }
        return Optional.empty();
    }

    @Override
    /**
     * 逻辑描述：
     *   1. 根据家长ID parentId 查出名下所有的孩子ID childrenIds;
     *   2. 根据 childrenIds 查出所有ID对应的孩子与家长的关系 studentParentRefs
     *   3. 对 studentParentRefs 遍历，将 parentId 相同的的 CallName 放入 集合 callNameSet
     *   4. 匹配 callNameSet 中 性别与当前性别比较
     */
    public MapMessage checkCallNameGender(Long parentId, CallName callName) {
        if (null == parentId || null == callName) {
            return MapMessage.errorMessage("参数错误");
        }

        List<User> children = studentLoaderClient.loadParentStudents(parentId);
        if (!CollectionUtils.isEmpty(children)) {
            List<Long> childrenIds = children.stream().map(child -> child.getId()).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(childrenIds)) {
                Map<Long, List<StudentParentRef>> studentParentRefs = studentLoaderClient.loadStudentParentRefs(childrenIds);
                Set<CallName> callNameSet = new HashSet<>();
                studentParentRefs.entrySet().forEach(entry -> callNameSet.addAll(
                        entry.getValue()
                                .stream()
                                .filter(ref -> parentId.equals(ref.getParentId()) && !StringUtils.isBlank(ref.getCallName()))
                                .map(m -> CallName.of(m.getCallName()))
                                .collect(Collectors.toSet()))
                );

                if (callNameSet.stream().anyMatch(call -> call != null && CallName.isGenderDiff(call, callName))) {
                    return MapMessage.errorMessage("当前选择身份({})与您曾经用过的身份性别不一致", callName.getValue());
                }
            }
        }
        return MapMessage.successMessage();
    }

    private void completeStudentNewUserTask(Long parentId) {
        List<User> children = studentLoaderClient.loadParentStudents(parentId);
        for (User child : children) {
            asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .NewUserTaskCacheManager_completeStudentNewUserTask(child, NewUserTask.parentWechatBinded)
                    .awaitUninterruptibly();
        }
    }

    private void cleanUpStudentNewUserTask(Long parentId) {
        List<User> children = studentLoaderClient.loadParentStudents(parentId);
        asyncUserCacheServiceClient.getAsyncUserCacheService()
                .NewUserTaskCacheManager_cleanupStudentNewUserTaskCache(children.stream().map(User::getId).collect(Collectors.toList()))
                .awaitUninterruptibly();
    }
}
