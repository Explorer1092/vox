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

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductRedirectType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Summer
 * @since 2017/1/6
 */
@Slf4j
@Named
public class AppOrderRemindFilter extends UserOrderFilter {
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder order = context.getOrder();
        if (order.getOrderType() == OrderType.app) {
            // 发送支付成功公众号消息
            String[] parts = order.getProductName().split(" ");
            String content;
            String url;
            if (OrderProductServiceType.isJztOpenApp(order.getOrderProductServiceType())) {
                content = "您的孩子" + order.getUserName() + "已成功开通【自学乐园-" + parts[0] + "】，马上去学习吧！";
                url = ProductConfig.getMainSiteBaseUrl() + FairylandProductRedirectType.buildMidUrl(order.getOrderProductServiceType(), FairyLandPlatform.PARENT_APP);
            } else {
                content = "您的孩子" + order.getUserName() + "已成功开通【自学乐园-" + parts[0] + "】，马上到学生app或者学生电脑端使用吧！";
                url = ProductConfig.getMainSiteBaseUrl() + "/parentMobile/ucenter/orderlistForInterest.vpage?isPaid=1";
            }
            String title = "开通成功";
            Map<String, Object> extInfo = new HashMap<>();
            extInfo.put("accountsKey", "fairyland");
            List<StudentParent> parentList = parentLoaderClient.loadStudentParents(order.getUserId());
            if (CollectionUtils.isNotEmpty(parentList)) {
                List<Long> parentIds = parentList.stream().map(StudentParent::getParentUser).map(User::getId).collect(Collectors.toList());
                officialAccountsServiceClient.sendMessage(parentIds, title, content, url, JsonUtils.toJson(extInfo), false);
                // 灰度发送家长通系统消息
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(order.getUserId());
                if (studentDetail != null) {
                    content = "您的孩子" + order.getUserName() + "已成功开通【" + parts[0] + "】。";
                    // 获取孩子的关键家长
                    List<StudentParent> keyParents = parentList.stream().filter(StudentParent::isKeyParent).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(keyParents)) {
                        for (StudentParent parent : keyParents) {
                            AppMessage message = new AppMessage();
                            message.setUserId(parent.getParentUser().getId());
                            message.setMessageType(ParentMessageType.REMINDER.type);
                            message.setTitle("通知");
                            message.setContent(content);
                            Map<String, Object> m = new HashMap<>();
                            m.put("tag", ParentMessageTag.订单.name());
                            message.setExtInfo(m);
                            messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
                        }
                    }
                }
            }
        }
        chain.doFilter(context); //可以继续后面的处理
    }
}
