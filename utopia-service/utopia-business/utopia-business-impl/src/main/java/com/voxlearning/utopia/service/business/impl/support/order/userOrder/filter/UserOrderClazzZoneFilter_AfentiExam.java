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

package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by Summer on 2017/1/6.
 */
@Named
public class UserOrderClazzZoneFilter_AfentiExam extends UserOrderFilter {

    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        if (context.getOrder().getPaymentStatus() == PaymentStatus.Paid
                && context.getOrder().getOrderType() == OrderType.app
                && context.getOrder().getOrderProductServiceType() != null
                && OrderProductServiceType.safeParse(context.getOrder().getOrderProductServiceType()) == OrderProductServiceType.AfentiExam) {

            User user = userLoaderClient.loadUser(context.getOrder().getUserId());

            //2013-11-14 新添加购买阿分题英语发班级动态消息
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(context.getOrder().getUserId());
            if (clazz != null && user != null) {
                sendMsg(context, user, clazz);
            }
        }
        //继续处理别的filter
        chain.doFilter(context);
    }

    private void sendMsg(UserOrderFilterContext context, User user, Clazz clazz) {
        String content = StringUtils.formatMessage("{}同学开通了{}，快来和他一起做学霸！",
                user.getProfile().getRealname(), context.getOrder().getProductName());
        if (!clazz.isSystemClazz()) {// 非系统自建班级
            zoneQueueServiceClient.createClazzJournal(clazz.getId())
                    .withUser(user.getId())
                    .withUser(user.fetchUserType())
                    .withClazzJournalType(ClazzJournalType.STUDENT_BUY_AFENTI)
                    .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
                    .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                    .commit();
        } else {// 系统自建班级
            List<GroupMapper> groups = groupLoaderClient.loadStudentGroups(user.getId(), false);
            if (groups.size() > 0) {
                zoneQueueServiceClient.createClazzJournal(clazz.getId())
                        .withUser(user.getId())
                        .withUser(user.fetchUserType())
                        .withClazzJournalType(ClazzJournalType.STUDENT_BUY_AFENTI)
                        .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
                        .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                        .withGroup(groups.get(0).getId())
                        .commit();
            }
        }
    }
}
