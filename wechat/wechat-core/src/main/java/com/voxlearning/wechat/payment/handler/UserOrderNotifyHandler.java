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

package com.voxlearning.wechat.payment.handler;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.service.business.consumer.BusinessUserOrderServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.builder.ServiceMessageBuilder;
import com.voxlearning.wechat.message.MessageSender;
import com.voxlearning.wechat.payment.NotifyHandlerFactory;
import com.voxlearning.wechat.payment.PaymentResultContext;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by xinxin on 2/2/2016.
 */
@Named
public class UserOrderNotifyHandler extends NotifyHandler {
    public static final String HANDLER_TYPE = "order";

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    private BusinessUserOrderServiceClient businessUserOrderServiceClient;
    private UserOrderLoaderClient userOrderLoaderClient;

    @Override
    public String getType() {
        return HANDLER_TYPE;
    }

    @Override
    protected void preHandle(PaymentResultContext context) {
    }

    @Override
    protected boolean doHandle(PaymentResultContext context) {
        try {
            PaymentCallbackContext cxt = parse(context);
            UserOrder order = AtomicLockManager.instance().wrapAtomic(businessUserOrderServiceClient)
                    .keys(context.getOrderId(), context.getTransactionId())
                    .proxy()
                    .processUserOrderPayment(cxt);
            return order != null;
        } catch (CannotAcquireLockException ex) {
            return false;
        }
    }

    @Override
    protected void postHandle(PaymentResultContext context) {
        AlpsThreadPool.getInstance().submit(() -> {
            UserOrder order = userOrderLoaderClient.loadUserOrder(context.getOrderId());
            String activityIds = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "SEATTLE_CLOSE_WECHAT_NOTICE");
            if (null != order) {
                if (order.getOrderType() != OrderType.seattle || StringUtils.isBlank(activityIds) || !activityIds.contains("," + order.getProductId() + ",")) {
                    String msg = StringUtils.formatMessage("{}的{}已购买成功,请让孩子登录一起作业家长通进行学习,点击链接去家长通：http://www.17zyw.cn/3BmAufam,如有任何问题请咨询400-160-1717",
                            StringUtils.isBlank(order.getUserName()) ? "您" : order.getUserName(), order.getProductName());
                    ServiceMessageBuilder rmb = new ServiceMessageBuilder(context.getOpenId());
                    rmb.buildTxtMsg(msg);

                    MessageSender messageSender = this.applicationContext.getBean(MessageSender.class);
                    messageSender.sendServiceMsg(rmb.toString(), WechatType.PARENT);
                }
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        NotifyHandlerFactory.register(this);
        businessUserOrderServiceClient = this.applicationContext.getBean(BusinessUserOrderServiceClient.class);
        userOrderLoaderClient = this.applicationContext.getBean(UserOrderLoaderClient.class);
    }
}
