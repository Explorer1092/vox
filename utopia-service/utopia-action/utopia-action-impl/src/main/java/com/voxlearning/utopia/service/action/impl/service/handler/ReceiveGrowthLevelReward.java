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

package com.voxlearning.utopia.service.action.impl.service.handler;

import com.voxlearning.alps.dao.mongo.support.MongoExceptionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.action.api.document.UserGrowthRewardLog;
import com.voxlearning.utopia.service.action.api.document.UserPrivilege;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.api.support.PrivilegeType;
import com.voxlearning.utopia.service.action.api.support.UserGrowthReward;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xinxin
 * @since 10/8/2016
 */
@Named("actionEventHandler.receiveGrowthLevelReward")
public class ReceiveGrowthLevelReward extends AbstractActionEventHandler {

    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;
    @Inject private PrivilegeServiceClient privilegeServiceClient;
    @Inject private UserIntegralServiceClient userIntegralServiceClient;

    @Override
    public ActionEventType getEventType() {
        return ActionEventType.ReceiveGrowthLevelReward;
    }

    @Override
    public void handle(ActionEvent event) {
        int level = SafeConverter.toInt(event.getAttributes().get("level"), 0);
        if (0 == level) {
            return;
        }

        if (!UserGrowthReward.canReceive(level)) {
            return;
        }

        //记录领取日志
        UserGrowthRewardLog log = new UserGrowthRewardLog();
        log.setId(event.getUserId() + "-" + level);     // userId-growthLevel 作为唯一主键
        log.setUserId(event.getUserId());
        log.setGrowthLevel(level);
        log.setIntegralCount(UserGrowthReward.getIntegral(level));

        // 通过等级对应的奖励头饰code，获得头饰数据
        Privilege headWear = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(UserGrowthReward.getHeadWearCode(level));

        // FIXME: headWare non-null checking missed

        log.setHeadWearId(headWear.getId());

        try {
            userGrowthRewardLogDao.insert(log);
        } catch (Exception ex) {
            if (MongoExceptionUtils.isDuplicateKeyError(ex)) {
                // 已经领取过这个等级的奖励了
                return;
            }
            throw ex;
        }

        //加学豆
        int integralCount = UserGrowthReward.getIntegral(level);
        if (integralCount > 0) {
            IntegralHistory history = new IntegralHistory(event.getUserId(), IntegralType.STUDENT_APP_GROWTH_REWARD, integralCount);
            history.setComment("成长等级" + level + "升级奖励");
            userIntegralServiceClient.getUserIntegralService().changeIntegral(history);
        }

        //给用户增加头饰特权

        String type = PrivilegeType.Head_Wear.name();
        // userId-type-privilegeId 作为唯一主键
        String id = event.getUserId() + "-" + type + "-" + headWear.getId();

        UserPrivilege privilege = new UserPrivilege();
        privilege.setId(id);
        privilege.setUserId(event.getUserId());
        privilege.setType(type);
        privilege.setPrivilegeId(headWear.getId());
        privilegeServiceClient.getPrivilegeService().insertUserPrivilegeWithoutResponse(privilege);
    }
}
