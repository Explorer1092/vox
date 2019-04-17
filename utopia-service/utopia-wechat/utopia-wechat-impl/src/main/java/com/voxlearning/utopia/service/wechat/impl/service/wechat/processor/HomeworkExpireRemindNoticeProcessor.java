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

package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.utopia.service.wechat.impl.dao.UserWechatRefPersistence;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatNoticePersistence;
import com.voxlearning.utopia.service.wechat.impl.support.WechatNoticeTemplateIds;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 到期未检查的作业给老师发微信消息提醒
 * Created by Shuai Huan on 2014/12/19.
 */
@Named
@Lazy(false)
@NoArgsConstructor
public class HomeworkExpireRemindNoticeProcessor extends AbstractNoticeProcessor {

    @Inject private WechatNoticePersistence wechatNoticePersistence;
    @Inject private UserWechatRefPersistence userWechatRefPersistence;

    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Override
    public WechatNoticeProcessorType type() {
        return WechatNoticeProcessorType.HomeworkExpireRemindNotice;
    }

    @Override
    public void process(Clazz clazz, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void process(Long clazzId, Long groupId, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processWithSpecificUsers(List<User> students, Teacher teacher, Long clazzId, Map<String, Object> extensionInfo, WechatType wechatType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processSingleUser(Long userId, Map<String, Object> extensionInfo, WechatType wechatType) {
        if (userId == null || wechatType == null) {
            return;
        }
        if (wechatType == WechatType.TEACHER) {// 包班制支持,需要发给主账号
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(userId);
            if (mainTeacherId != null) {
                userId = mainTeacherId;
            }
        }
        List<UserWechatRef> refs = userWechatRefPersistence.findByUserId(userId, wechatType.getType());
        if (CollectionUtils.isEmpty(refs)) {
            return;
        }
        for (UserWechatRef ref : refs) {
            processNotice(ref.getUserId(), ref.getOpenId(), extensionInfo);
        }
    }

    @Override
    protected void processWechat(Long userId, String openId, Map extensionInfo) {
        throw new UnsupportedOperationException();
    }

    private void processNotice(Long userId, String openId, Map<String, Object> extensionInfo) {
        WechatNotice notice = new WechatNotice();
        notice.setUserId(userId);
        notice.setOpenId(openId);
        notice.setMessageType(WechatNoticeType.TEACHER_TEMPLATE_HOMEWORK_EXPIRE_REMIND.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setDisabled(false);
        notice.setSendTime(new Date());
        notice.setExpireTime(getDefaultExpireTime());
        Map<String, Object> map = new HashMap<>();
        map.put("template_id", WechatNoticeTemplateIds.templateId6);
        map.put("messageInfo", extensionInfo);
        notice.setMessage(JsonUtils.toJson(map));

        wechatNoticePersistence.persist(notice);
    }

}
