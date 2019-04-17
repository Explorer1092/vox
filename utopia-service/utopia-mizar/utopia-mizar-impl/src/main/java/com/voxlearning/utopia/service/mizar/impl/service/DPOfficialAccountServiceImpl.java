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

package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.oa.UserOfficialAccountsRef;
import com.voxlearning.utopia.service.mizar.api.service.DPOfficialAccountService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Sms dubbo proxy service implementation of {@link DPOfficialAccountService}.
 *
 * @author yuechen.wang
 * @since 2017-06-13
 */
@Named
@Service(interfaceClass = DPOfficialAccountService.class)
@ExposeService(interfaceClass = DPOfficialAccountService.class)
public class DPOfficialAccountServiceImpl implements DPOfficialAccountService {

    @Inject private OfficialAccountsServiceImpl officialAccountsService;

    @Override
    public MapMessage isFollow(Long accountId, Long userId) {
        // 检查参数
        if (accountId == null || userId == null) {
            return MapMessage.errorMessage("参数错误");
        }

        boolean follow = officialAccountsService.isFollow(accountId, userId);

        return MapMessage.successMessage().add("isFollow", follow);
    }

    @Override
    public MapMessage updateFollowStatus(Long accountId, Long userId, String status) {
        if (userId == null || accountId == null || StringUtils.isBlank(status)) {
            return MapMessage.errorMessage("参数错误");
        }

        UserOfficialAccountsRef.Status refStatus;
        try {
            refStatus = UserOfficialAccountsRef.Status.valueOf(status);
        } catch (Exception ignored) {
            return MapMessage.errorMessage("无效的关注状态");
        }
        return officialAccountsService.updateFollowStatus(userId, accountId, refStatus);
    }


    @Override
    public MapMessage sendMessage(Collection<Long> userIds, String title, String content, String linkUrl, String accountKey, Boolean sendPush) {
        if (CollectionUtils.isEmpty(userIds) || StringUtils.isAnyBlank(title, content, accountKey)) {
            return MapMessage.errorMessage("参数错误");
        }
        List<Long> parentIds = userIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<String, Object> extInfo = MapUtils.m("accountsKey", accountKey);
        return officialAccountsService.sendMessage(parentIds, title, content, linkUrl, JsonUtils.toJson(extInfo), sendPush);
    }

}
