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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.xml.XmlUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;
import com.voxlearning.utopia.schedule.cache.ScheduleCacheSystem;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.finance.client.WirelessChargingServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Created by Alex on 14-11-12.
 */
@Named
@ScheduledJobDefinition(
        jobName = "畅天游无线充值任务",
        jobDescription = "畅天游无线充值任务,每10分钟运行一次",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.TEST, Mode.STAGING},
        cronExpression = "0 */10 7,8,9,10,11,12,13,14,15,16,17,18,19 * * ?",
        ENABLED = true
)
@ProgressTotalWork(100)
public class AutoWirelessChargingJob extends ScheduledJobWithJournalSupport {

    private static final Map<String, String> SYS_ERROR_CODE_MAP = new HashMap<>();

    static {
        SYS_ERROR_CODE_MAP.put("1001", "参数不完整");
        SYS_ERROR_CODE_MAP.put("1004", "用户不存在");
        SYS_ERROR_CODE_MAP.put("1005", "密码不正确");
        SYS_ERROR_CODE_MAP.put("2001", "用户暂停");
        SYS_ERROR_CODE_MAP.put("1006", "IP鉴权失败");
        SYS_ERROR_CODE_MAP.put("1007", "md5 key验证不正确");
        SYS_ERROR_CODE_MAP.put("2002", "账户余额异常 ");
        SYS_ERROR_CODE_MAP.put("2005", "余额不足");
        SYS_ERROR_CODE_MAP.put("9999", "系统错误");
    }

    private static final Map<String, String> USER_ERROR_CODE_MAP = new HashMap<>();

    static {
        USER_ERROR_CODE_MAP.put("1002", "手机号不正确");
        USER_ERROR_CODE_MAP.put("1003", "金额不正确");
        USER_ERROR_CODE_MAP.put("2003", "手机号是黑名单");
        USER_ERROR_CODE_MAP.put("2004", "订单是重复");
        // 目前联通个别地区产品未开通，所以本错误作为用户级错误处理
        USER_ERROR_CODE_MAP.put("2006", "该产品未开通");
    }

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private WirelessChargingServiceClient wirelessChargingServiceClient;

    @Inject private ScheduleCacheSystem scheduleCacheSystem;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        List<WirelessCharging> chargingList = wirelessChargingServiceClient.getWirelessChargingService()
                .findTobeChargingList()
                .getUninterruptibly();
        if (CollectionUtils.isEmpty(chargingList)) {
            return;
        }
        progressMonitor.worked(10);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, chargingList.size());
        for (WirelessCharging chargingItem : chargingList) {
            try {
                if (!submitChargingRequest(chargingItem)) {
                    return;
                }
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }

    // SP畅天游那边的注意事项
    // 一：以下事项请知晓：我们代充的话费用户需要拨打运营商电话才能查询。
    // 二：日常情况下只要号码正常98%的订单都会在10分钟之内充值成功。月初月末运营商维护订单处理的稍微慢一些。
    // 三：十分钟之内未充值成功的用户，我们订单处理的机制是每6小时自动重提一次处理，最晚不会超过72小时。建议在页面提示用户72小时内到账。
    // 四：针对联通无法充值地区的号码建议用户更换移动或者电信的手机号码，或者赠送其他礼品。
    // !!!!!!!!!!!!!!!!! 下面才是重点 !!!!!!!!!!!!!!!!!
    // 移动电信支持全国，联通1~4元不支持地区为四川、陕西、广东，5~9元不支持地区为广东地区，10元为广东智能卡用户
    private boolean submitChargingRequest(WirelessCharging chargingItem) {
        // FIXME 老师任务防作弊处理，同一个手机号如果已经发过奖励了，那么就不再发了
        Integer chargeType = chargingItem.getChargeType();
        String chargeDesc = chargingItem.getChargeDesc();
        if (Objects.equals(chargeType, ChargeType.TEACHER_TASK.getType()) && !Objects.equals(chargeDesc, ChargeType.TEACHER_TASK.getDescription())) { // 新的任务请求才进入
            WirelessCharging chargingHistory = wirelessChargingServiceClient.getWirelessChargingService()
                    .findChargingSuccessList(chargingItem.getTargetSensitiveMobile())
                    .getUninterruptibly().stream()
                    .filter(p -> Objects.equals(p.getStatus(), 1) || Objects.equals(p.getStatus(), 2))  // 已经请求 或者 发放成功
                    .filter(p -> !Objects.equals(p.getId(), chargingItem.getId()))  // 不是同一条
                    .filter(p -> Objects.equals(p.getChargeType(), chargeType))     // 类型一样
                    .filter(p -> Objects.equals(p.getChargeDesc(), chargeDesc))     // 描述一样
                    .filter(p -> Objects.equals(SafeConverter.toString(p.getNotifySmsMessage(), ""), SafeConverter.toString(chargingItem.getNotifySmsMessage(), "")))   // 提示消息一样
                    .findAny()
                    .orElse(null);
            if (chargingHistory != null) { // 如果发过，那么就不在重复发了，标记成失败
                wirelessChargingServiceClient.getWirelessChargingService().updateChargingFailed(chargingItem.getId(), "9999", "Dup");
                return true;
            }
        }

        CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();
        String companyId = commonConfiguration.getWirelessChargingCompanyId();
        String interfacePwd = commonConfiguration.getWirelessChargingInterfacePwd();

        String mobile = sensitiveUserDataServiceClient.loadWirelessChargingTargetMobile(chargingItem.getId());
        String amount = String.valueOf(chargingItem.getAmount());
        String orderId = chargingItem.getId();
        String orderSource = "1";
        String submitKey = commonConfiguration.getWirelessChargingSubmitKey();

        // 检查用户当日充值是否超过500元
        int userDailyAmount = wirelessChargingServiceClient.getWirelessChargingService()
                .findUserDailyChargingAmount(chargingItem.getUserId(), chargingItem.getCreateDatetime())
                .getUninterruptibly();
        userDailyAmount = userDailyAmount / 100;
        if (userDailyAmount > 500) {

            // 判断是否已经发过报警邮件了,如果已经发过就不发了,防止骚扰
            String emailKey = CacheKeyGenerator.generateCacheKey(AutoWirelessChargingJob.class,
                    new String[]{"userId", "date", "type"},
                    new Object[]{chargingItem.getUserId(), DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE), "over"});

            if (scheduleCacheSystem.CBS.flushable.get(emailKey) == null) {
                Map<String, Object> content = new HashMap<>();
                content.put("info", "用户:" + chargingItem.getUserId() + "今天充值金额" + userDailyAmount + "超过上限500元");
                emailServiceClient.createTemplateEmail(EmailTemplate.office)
                        .to("zhilong.hu@17zuoye.com")
                        .cc("yizhou.zhang@17zuoye.com")
                        .subject(RuntimeMode.getCurrentStage() + "环境畅天游无线充值系统错误")
                        .content(content)
                        .send();
                scheduleCacheSystem.CBS.flushable.set(emailKey, DateUtils.getCurrentToDayEndSecond(), "1");
            }

            return true;
        }

        // 计算Key
        String key = DigestUtils.md5Hex(companyId + interfacePwd + mobile + amount + orderId + submitKey);

        // 拼成请求的URL
        Map<String, String> params = new HashMap<>();
        params.put("CompanyId", companyId);
        params.put("InterfacePwd", interfacePwd);
        params.put("Mobile", mobile);
        params.put("Amount", amount);
        params.put("OrderID", orderId);
        params.put("OrderSource", orderSource);
        params.put("Key", key);

        String submitUrl = commonConfiguration.getWirelessChargingSubmitUrl();

        try {
            // logger.info("Send wireless charging request to {} with params {}", submitUrl, params);
            String URL = UrlUtils.buildUrlQuery(submitUrl, params);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
//            if (response != null && response.getResponseString() != null) {
//                logger.info("Wireless charging response {}", response.getResponseString().replaceAll("\n", "").replaceAll("\r", ""));
//            }

            if (response != null && response.getStatusCode() == 200) {
                Document document = XmlUtils.parseDocument(new ByteArrayInputStream(response.getResponseString().getBytes()));
                String resultCode = XmlUtils.getChildElementText(document.getDocumentElement(), "result");
                if ("0000".equals(resultCode)) {
                    wirelessChargingServiceClient.getWirelessChargingService()
                            .updateChargingSubmitSuccess(orderId)
                            .awaitUninterruptibly();
                } else if (USER_ERROR_CODE_MAP.containsKey(resultCode)) {
                    // 用户错误，更新成充值错误
                    wirelessChargingServiceClient.getWirelessChargingService()
                            .updateChargingSubmitFailed(orderId, resultCode)
                            .awaitUninterruptibly();
                } else {
                    // 系统错误，发送提示邮件，状态不变
                    String emailKey = CacheKeyGenerator.generateCacheKey(AutoWirelessChargingJob.class,
                            new String[]{"userId", "date", "type"},
                            new Object[]{chargingItem.getUserId(), DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE), "syserr"});

                    if (scheduleCacheSystem.CBS.flushable.get(emailKey) == null) {
                        Map<String, Object> content = new HashMap<>();
                        content.put("info", "CODE:" + resultCode + " CONTENT:" + SYS_ERROR_CODE_MAP.get(resultCode) + " RESPONSE:" + response.getResponseString());
                        emailServiceClient.createTemplateEmail(EmailTemplate.office)
                                .to("zhilong.hu@17zuoye.com")
                                .cc("yizhou.zhang@17zuoye.com")
                                .subject(RuntimeMode.getCurrentStage() + "环境畅天游无线充值系统错误")
                                .content(content)
                                .send();

                        scheduleCacheSystem.CBS.flushable.set(emailKey, DateUtils.getCurrentToDayEndSecond(), "1");
                    }
                    // 坑比较多，将本条数据后移确保其他充值可以执行
                    wirelessChargingServiceClient.getWirelessChargingService()
                            .updateProcessTime(orderId)
                            .awaitUninterruptibly();
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warn("Wireless charging submit request failed.", e);
        }

        return true;
    }
}
