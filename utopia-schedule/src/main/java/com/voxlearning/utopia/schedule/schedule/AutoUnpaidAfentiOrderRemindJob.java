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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.OfficialAccountsServiceClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.FairylandLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.ParentMessageServiceClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 学生未支付的订单发通知
 * Created by Shuai Huan on 2014/11/10.
 */
@Named
@ScheduledJobDefinition(
        jobName = "学生未支付的订单发通知",
        jobDescription = "学生未支付的订单发通知，每天13:00运行一次",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 0 13 * * ?"
)
@ProgressTotalWork(100)
public class AutoUnpaidAfentiOrderRemindJob extends ScheduledJobWithJournalSupport {


    @Inject private EmailServiceClient emailServiceClient;
    @Inject private GlobalTagServiceClient globalTagServiceClient;
    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private ParentMessageServiceClient parentMessageServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private WechatServiceClient wechatServiceClient;
    @Inject private OfficialAccountsServiceClient officialAccountsServiceClient;
    @Inject private FairylandLoaderClient fairylandLoaderClient;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        /*
            推送订单类型：VIP购买（道具购买不推送）
            包含产品：购买过未支付的订单
            推送机制：每天定时推送一次（暂定13点推送），上线时间点前24小时的订单开始推送
            推单规则：如订单超过1条时，推单价最高的
         */
        AtomicLong validOrderSize = new AtomicLong(0);
        AtomicLong pushParentSize = new AtomicLong(0);

        Map<String, String> productNames = new HashMap<>();
        Set<String> validProducts;
        List<OrderProduct> productInfos;

        List<FairylandProduct> fairylandProducts = fairylandLoaderClient.loadFairylandProducts(FairyLandPlatform.PARENT_APP, FairylandProductType.APPS)
                .stream()
                .collect(Collectors.toList());
        validProducts = fairylandProducts
                .stream()
                .map(FairylandProduct::getAppKey)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(validProducts)) {
            logger.error("loadFairylandProducts error");
            return;
        }
        fairylandProducts.forEach(p -> productNames.put(p.getAppKey(), p.getProductName()));
        productInfos = userOrderLoaderClient.loadAvailableProduct();

        int taskCount = RuntimeMode.current().le(Mode.TEST) ? 2 : 10;
        Date end = new Date();
        Date start = DateUtils.calculateDateDay(end, -1);
        List<UserOrder> userOrderList = new ArrayList<>();
        // 循环取表的数据
        for (int i = 0; i < 100; i++) {
            List<UserOrder> orderList = userOrderLoaderClient.loadUnPaidOrderByTime(start, end, (long) i);
            if (CollectionUtils.isNotEmpty(orderList)) {
                userOrderList.addAll(orderList);
            }
        }
        //取每个用户单价最高的未支付订单，排除家长通不支持的订单列表
        List<UserOrder> maxOrders = new LinkedList<>();
        Map<Long, List<UserOrder>> userIdMap = userOrderList.stream()
                .filter(e -> e != null && e.getUserId() != null)
                .collect(Collectors.groupingBy(UserOrder::getUserId));
        for (Long userId : userIdMap.keySet()) {
            UserOrder maxOrder = null;
            Double totalPrice = 0d;
            for (UserOrder order : userIdMap.get(userId)) {
                if (!validProducts.contains(order.getOrderProductServiceType())) {
                    continue;
                }
                if (order.getOrderPrice().doubleValue() > totalPrice) {
                    totalPrice = order.getOrderPrice().doubleValue();
                    maxOrder = order;
                }
            }
            if (maxOrder != null) {
                maxOrders.add(maxOrder);
            }
        }
        if (maxOrders.isEmpty()) {
            return;
        }
        progressMonitor.worked(20);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(80, maxOrders.size());
        List<List<UserOrder>> sources = CollectionUtils.splitList(maxOrders, taskCount);
        final CountDownLatch latch = new CountDownLatch(taskCount);
        for (final List<UserOrder> source : sources) {
            Runnable task = () -> {
                try {
                    handle(source, productNames, productInfos, validOrderSize, pushParentSize, monitor);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            };
            AlpsThreadPool.getInstance().submit(task);
        }
        latch.await();

        String title = "给家长通发送未支付订单提醒";
        String content = "有效订单数量：" + validOrderSize.intValue() + ",推送家长数量:" + pushParentSize.intValue();
        sendEmail(title, content);
        progressMonitor.done();
    }


    private void handle(List<UserOrder> source, Map<String, String> productNames, List<OrderProduct> productInfos,
                        AtomicLong validOrderSize, AtomicLong pushParentSize, ISimpleProgressMonitor monitor) {
        Set<String> noRemindUsers = globalTagServiceClient.getGlobalTagBuffer()
                .findByName(GlobalTagName.NoAfentiRemindUsers.name())
                .stream()
                .filter(t -> t.getTagValue() != null)
                .map(GlobalTag::getTagValue)
                .collect(Collectors.toSet());

        int vos = 0;
        int pps = 0;

        for (UserOrder data : source) {
            try {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(data.getUserId());
                if (studentDetail == null || studentDetail.getClazz() == null
                        || studentDetail.getClazz().isTerminalClazz()
                        || studentDetail.isInPaymentBlackListRegion()) {
                    continue;
                }

                //不收提醒的用户
                if (CollectionUtils.isNotEmpty(noRemindUsers) && noRemindUsers.contains(studentDetail.getId().toString())) {
                    continue;
                }
                //价格不一样的用户订单不推送
                //没有产品id的不推送
                //产品id不在productInfos中的表示道具订单，不进行推送
                OrderProduct productInfo = productInfos.stream()
                        .filter(p -> Objects.equals(p.getProductType(), data.getOrderProductServiceType()))
                        .filter(p -> data.getProductId() != null)
                        .filter(p -> Objects.equals(p.getId(), data.getProductId()))
                        .filter(p -> Math.abs(p.getPrice().doubleValue() - data.getOrderPrice().doubleValue()) <= 1e-6)
                        .findFirst()
                        .orElse(null);

                if (productInfo == null) {
                    continue;
                }

//                Map<String, Object> extensionInfo = new HashMap<>();
//                extensionInfo.put("productName", productNames.getOrDefault(data.getOrderProductServiceType().name(), ""));
//                extensionInfo.put("orderCreateTime", data.getCreateDatetime());
//                extensionInfo.put("orderId", data.genUserOrderId());
//                try {
//                    //此类消息teacher和clazzId都不需要
//                    wechatServiceClient.processWechatNotice(
//                            WechatNoticeProcessorType.UnpaidAfentiOrderRemindNotice,
//                            Collections.singletonList(studentDetail),
//                            new Teacher(),
//                            0L,
//                            extensionInfo,
//                            WechatType.PARENT);
//                } catch (Exception ex) {
//                    logger.warn(ex.getMessage(), ex);
//                }

                List<Long> parentIds = parentLoaderClient.loadStudentParents(studentDetail.getId()).stream().map(StudentParent::getParentUser).map(User::getId).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(parentIds)) {
                    continue;
                }
                String productName = productNames.getOrDefault(data.getOrderProductServiceType(), "");
                String pattern = "您的孩子{0}想要开通【{1}】，点击查看>>";
                String content = MessageFormat.format(pattern, studentDetail.fetchRealname(), productName);
                String linkUrl = "/view/mobile/parent/17my_shell/particulars.vpage?oid=" + data.genUserOrderId() + "&uid=" + data.getUserId();

                // 发送家长消息
//                ParentMessageTag tag = ParentMessageTag.订单;
//                ParentMessageType type = ParentMessageType.REMINDER;
//                parentMessageServiceClient.postParentMessage(parentIds, studentDetail.getId(), content, "", linkUrl, "", tag, type);

                List<AppMessage> messageList = new ArrayList<>();
                Map<String, Object> extInfo = new HashMap<>();
                extInfo.put("studentId", studentDetail.getId());
                extInfo.put("tag", ParentMessageTag.订单.name());
                extInfo.put("type", ParentMessageType.REMINDER.name());
                extInfo.put("senderName", "");
                for (Long parentId : parentIds) {
                    //新消息中心
                    AppMessage message = new AppMessage();
                    message.setUserId(parentId);
                    message.setContent(content);
                    message.setLinkType(1);
                    message.setLinkUrl(linkUrl);
                    message.setImageUrl("");
                    message.setExtInfo(extInfo);
                    message.setMessageType(ParentMessageType.REMINDER.getType());
                    messageList.add(message);
                    messageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
                }
                //发送jpush
                Map<String, Object> extras = new HashMap<>();
                extras.put("studentId", studentDetail.getId());
                extras.put("url", linkUrl);
                extras.put("tag", ParentMessageTag.订单.name());
                extras.put("s", ParentAppPushType.ORDER_CENTER.name());
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, parentIds, extras);



//                // 发送公众号消息
//                String title = "未支付订单";
//                Map<String, Object> extInfo = new HashMap<>();
//                linkUrl = ProductConfig.getMainSiteBaseUrl() + linkUrl;
//                extInfo.put("accountsKey", "fairyland");
//                officialAccountsServiceClient.sendMessage(parentIds, title, content, linkUrl, JsonUtils.toJson(extInfo), false);

                vos++;
                pps += parentIds.size();
            } finally {
                monitor.worked(1);
            }
        }
        validOrderSize.addAndGet(vos);
        pushParentSize.addAndGet(pps);
        logger.info("AutoUnpaidAfentiOrderRemindJob success validOrderSize={},pushParentSize={}", validOrderSize, pushParentSize);
    }

    private void sendEmail(String title, String content) {
        String date = DateUtils.getNowSqlDatetime();
        content += "\n发送时间:" + date;
        try {
            emailServiceClient.createPlainEmail()
                    .to("peng.zhang.a@17zuoye.com;ronghua.lv@17zuoye.com")
                    .subject(title + "发送报告(来自：" + RuntimeMode.current().name() + "环境)")
                    .body(content)
                    .send();

        } catch (Exception e) {
            logger.error("sendEmail failed");
        }
    }
}
