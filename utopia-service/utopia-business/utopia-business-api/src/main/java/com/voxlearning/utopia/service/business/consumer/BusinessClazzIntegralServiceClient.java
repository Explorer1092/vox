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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.SmartClazzRewardItem;
import com.voxlearning.utopia.business.api.BusinessClazzIntegralService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.Getter;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Summer Yang
 * @since 2016/1/8
 */
public class BusinessClazzIntegralServiceClient {

    @Getter
    @ImportService(interfaceClass = BusinessClazzIntegralService.class)
    private BusinessClazzIntegralService remoteReference;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private UserLoaderClient userLoaderClient;

    public MapMessage rewardSmartClazzStudent(Long teacherId, Clazz clazz, Map<String, Object> jsonMap) {
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            return MapMessage.errorMessage("老师信息为空");
        }
        if (teacherDetail.fetchCertificationState() != AuthenticationState.SUCCESS) {//老师未认证，不可发奖励
            return MapMessage.errorMessage("非认证用户，不可奖励学生");
        }
        if (jsonMap == null || jsonMap.isEmpty()) {
            return MapMessage.errorMessage("所传参数为空");
        }
        if (!jsonMap.containsKey("userIds")) {
            return MapMessage.errorMessage("学生列表不能为空");
        }
        String idListText = SafeConverter.toString(jsonMap.get("userIds"));
        List<Long> userIds = StringUtils.toLongList(idListText);
        List<User> userList = new LinkedList<>(userLoaderClient.loadUsers(userIds).values());
        if (CollectionUtils.isEmpty(userList)) {
            return MapMessage.errorMessage("学生列表不能为空");
        }
        if (clazz == null) {
            return MapMessage.errorMessage("班级信息不能为空");
        }
        int rewardIntegralCnt = SafeConverter.toInt(jsonMap.get("integralCnt"));
        if (rewardIntegralCnt <= 0) {
            return MapMessage.errorMessage("奖励的学豆数量必须大于零");
        }
        String rewardItem = SafeConverter.toString(jsonMap.get("rewardItem"));
        SmartClazzRewardItem item = SmartClazzRewardItem.of(rewardItem);
        if (item == null) {
            return MapMessage.errorMessage("选择的奖项未知");
        }
        String customContent = StringUtils.filterEmojiForMysql(SafeConverter.toString(jsonMap.get("customContent")));
        if (item == SmartClazzRewardItem.CUSTOM_TAG && (StringUtils.isBlank(customContent) || customContent.length() > 20)) {
            return MapMessage.errorMessage("自定义内容不能为空且不能超过20个字符");
        }
        return remoteReference.rewardSmartClazzStudent(teacherDetail, clazz, userList, rewardIntegralCnt, item, customContent);
    }
}
