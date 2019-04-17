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

package com.voxlearning.utopia.service.business.impl.service.student.spr;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.business.api.constant.IdentificationWishType;
import com.voxlearning.utopia.service.business.impl.service.AsyncBusinessCacheServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.campaign.client.MissionServiceClient;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.WishType.INTEGRAL;

/**
 * Student parent reward -- wish creation template
 *
 * @author RuiBao
 * @version 0.1
 * @since 1/12/2015
 */
@Slf4j
public abstract class WishCreationTemplate extends BusinessServiceSpringBean {

    @Inject private MissionServiceClient missionServiceClient;
    @Inject private AsyncBusinessCacheServiceImpl asyncBusinessCacheService;
    @Inject private WishCreationTemplateManager wishCreationTemplateManager;

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Getter
    private final WishType type;

    protected WishCreationTemplate() {
        IdentificationWishType annotation = getClass().getAnnotation(IdentificationWishType.class);
        type = annotation.value();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        wishCreationTemplateManager.register(this);
    }

    final public MapMessage create(WishCreationContext context) {
        if (null == context) {
            return MapMessage.errorMessage("提交失败");
        }
        User student = userLoaderClient.loadUser(context.getUserId());
        if (null == student) {
            return MapMessage.errorMessage("请先登录");
        }
        // 判断是否有绑定微信的家长
        List<StudentParentRef> parents = studentLoaderClient.loadStudentParentRefs(context.getUserId());
        if (CollectionUtils.isEmpty(parents)) {
            return MapMessage.errorMessage("请先绑定家长");
        }
        List<Long> parentIds = parents.stream().map(StudentParentRef::getParentId).collect(Collectors.toList());
        String month = DateUtils.dateToString(new Date(), DateUtils.FORMAT_YEAR_MONTH);
        // 判断是否可以许愿
        MapMessage mesg = canMakeWish(context);
        if (mesg.isSuccess()) {
            // 许愿
            Long missionId = save(context);
            // 记录本周已经许过愿望
            asyncBusinessCacheService.StudentWishCreationCacheManager_record(student.getId()).awaitUninterruptibly();
            // 发微信
            Map<String, Object> extensionInfo = new HashMap<>();
            extensionInfo.put("studentName", student.fetchRealname());
            String wish = context.getType() == INTEGRAL ? context.getIntegral() + "学豆" : context.getWish();
            extensionInfo.put("wish", wish);
            boolean integralUsed = missionServiceClient.getMissionService()
                    .findMissionIntegralLogs(student.getId())
                    .getUninterruptibly()
                    .stream()
                    .filter(t -> StringUtils.equals(month, t.getMonth()))
                    .count() > 0;
            extensionInfo.put("integralUsed", integralUsed);
            extensionInfo.put("studentId", student.getId());
            extensionInfo.put("missionId", missionId);
            Map<Long, List<UserWechatRef>> parentsWechat = wechatLoaderClient.loadUserWechatRefs(parentIds, WechatType.PARENT);
            for (Long parentId : parentsWechat.keySet()) {
                for (UserWechatRef ref : parentsWechat.get(parentId)) {
                    wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.MakeWishNotice,
                            parentId, ref.getOpenId(), extensionInfo);
                }
            }
            return MapMessage.successMessage();
        }
        return mesg;
    }

    abstract protected MapMessage canMakeWish(WishCreationContext context);

    abstract protected Long save(WishCreationContext context);

}
