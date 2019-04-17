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

package com.voxlearning.wechat.handler;

import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.service.ai.client.AiLoaderClient;
import com.voxlearning.utopia.service.ai.client.AiOrderProductServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.service.integral.ClazzIntegralService;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.context.WechatRequestContext;
import com.voxlearning.wechat.message.MessageSender;
import com.voxlearning.wechat.service.UserService;
import com.voxlearning.wechat.support.WechatMessageHelper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @author Xin Xin
 * @since 10/20/15
 */
public abstract class AbstractHandler implements IMessageHandler {
    protected ApplicationContext applicationContext;
    protected MessageSender messageSender;
    protected UserService userService;

    protected WechatLoaderClient wechatLoaderClient;
    protected UserLoaderClient userLoaderClient;
    protected StudentLoaderClient studentLoaderClient;
    protected ParentLoaderClient parentLoaderClient;
    protected ParentServiceClient parentServiceClient;
    protected WechatServiceClient wechatServiceClient;
    protected TeacherLoaderClient teacherLoaderClient;
    protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    protected DeprecatedGroupLoaderClient groupLoaderClient;
    protected CdnResourceUrlGenerator cdnResourceUrlGenerator;

    protected UserIntegralService userIntegralService;
    protected ClazzIntegralService clazzIntegralService;
    protected UserOrderLoaderClient userOrderLoaderClient;
    protected AiLoaderClient aiLoaderClient;
    protected AiOrderProductServiceClient aiOrderProductServiceClient;
    protected WechatMessageHelper wechatMessageHelper;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        userService = applicationContext.getBean(UserService.class);

        wechatLoaderClient = applicationContext.getBean(WechatLoaderClient.class);
        userLoaderClient = applicationContext.getBean(UserLoaderClient.class);
        studentLoaderClient = applicationContext.getBean(StudentLoaderClient.class);
        parentLoaderClient = applicationContext.getBean(ParentLoaderClient.class);
        parentServiceClient = applicationContext.getBean(ParentServiceClient.class);
        wechatServiceClient = applicationContext.getBean(WechatServiceClient.class);
        messageSender = applicationContext.getBean(MessageSender.class);
        teacherLoaderClient = applicationContext.getBean(TeacherLoaderClient.class);
        deprecatedClazzLoaderClient = applicationContext.getBean(DeprecatedClazzLoaderClient.class);
        groupLoaderClient = applicationContext.getBean(DeprecatedGroupLoaderClient.class);
        cdnResourceUrlGenerator = applicationContext.getBean(CdnResourceUrlGenerator.class);
        userOrderLoaderClient = applicationContext.getBean(UserOrderLoaderClient.class);
        aiLoaderClient = applicationContext.getBean(AiLoaderClient.class);
        aiOrderProductServiceClient = applicationContext.getBean(AiOrderProductServiceClient.class);
        wechatMessageHelper = applicationContext.getBean(WechatMessageHelper.class);
    }

    @Override
    public void setExtInstances(Map<String, Object> extInstances) {
        userIntegralService = (UserIntegralService) extInstances.get(UserIntegralService.class.getName());
        clazzIntegralService = (ClazzIntegralService) extInstances.get(ClazzIntegralService.class.getName());
    }

    //发送关注后的消息
    protected String sendMsgForSubscribe_Parent(MessageContext context) {
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.NEWS);
        String cover = "http://mmbiz.qpic.cn/mmbiz/ziadDDQxbCJELdlzbmab0YEQuDSJq55apiaHHnJAT4jtRN1RsQlp5b0pmibEX3KiariagnUgDdHoxIZsbYd0Zib8U9ag/0.jpeg?tp=webp&wxfrom=5&wx_lazy=1";
        rb.buildArticle("请点击查收\"我\"的【介绍信】", "", cover, "http://mp.weixin.qq.com/s/LebQgsu30Q3COqX75tHeGQ");
        return rb.toString();
    }

    protected String defaultReplyTextMsg(MessageContext context) {
        ReplyMessageBuilder rmb = new ReplyMessageBuilder(context);
        rmb.buildMsgType(MessageType.TEXT);
        rmb.buildContent("尊敬的用户，非常感谢您对一起作业的信任和支持，如有任何疑问请在APP学生端里【个人】里面联系【帮助与客服】，祝您生活愉快，家庭和睦");
        return rmb.toString();
    }

    protected WechatRequestContext getRequestContext() {
        return (WechatRequestContext) DefaultContext.get();
    }

}
