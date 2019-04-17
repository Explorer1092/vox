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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.xml.XmlUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.mutable.MutableBoolean;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.finance.client.WirelessChargingServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex on 14-11-12.
 */
@Named
@ScheduledJobDefinition(
        jobName = "畅天游无线充值任务",
        jobDescription = "畅天游无线充值任务,每2小时运行一次",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 */2 * * ?"
        //ENABLED = false
)
@ProgressTotalWork(100)
public class AutoWirelessChargingQueryJob extends ScheduledJobWithJournalSupport {

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
        USER_ERROR_CODE_MAP.put("2006", "该产品未开通");
    }

    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private EmailServiceClient emailServiceClient;
    @Inject private WirelessChargingServiceClient wirelessChargingServiceClient;

    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private WechatLoaderClient wechatLoaderClient;
    @Inject private WechatServiceClient wechatServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        List<WirelessCharging> chargingList = wirelessChargingServiceClient.getWirelessChargingService()
                .findTobeCheckResultList()
                .getUninterruptibly();
        if (CollectionUtils.isEmpty(chargingList)) {
            return;
        }
        progressMonitor.worked(10);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, chargingList.size());
        for (WirelessCharging chargingItem : chargingList) {
            try {
                if (!checkChargingResult(chargingItem)) {
                    return;
                }
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }

    private boolean checkChargingResult(WirelessCharging chargingItem) {
        CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();

        String companyId = commonConfiguration.getWirelessChargingCompanyId();
        String interfacePwd = commonConfiguration.getWirelessChargingInterfacePwd();
        String mobile = sensitiveUserDataServiceClient.loadWirelessChargingTargetMobile(chargingItem.getId());
        String amount = String.valueOf(chargingItem.getAmount());
        String orderId = chargingItem.getId();
        String orderSource = "1";
        String submitKey = commonConfiguration.getWirelessChargingSubmitKey();
        Long userId = chargingItem.getUserId();

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

        String queryUrl = commonConfiguration.getWirelessChargingQueryUrl();

        try {
            //logger.info("Send wireless charging check result request to {} with params {}", queryUrl, params);
            String URL = UrlUtils.buildUrlQuery(queryUrl, params);
            //logger.info("Send wireless charging check result request to {} ", queryUrl);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
            // logger.info("Wireless charging check result response {}", response.getResponseString());
            if (response.getStatusCode() == 200) {
                Document document = XmlUtils.parseDocument(new ByteArrayInputStream(response.getResponseString().getBytes()));
                String resultCode = XmlUtils.getChildElementText(document.getDocumentElement(), "result");
                String resultStatus = XmlUtils.getChildElementText(document.getDocumentElement(), "status");
                if ("0000".equals(resultCode)) {
                    if ("1000".equals(resultStatus)) {
                        // 订单支付成功
                        boolean ret = wirelessChargingServiceClient.getWirelessChargingService()
                                .updateChargingSuccess(orderId)
                                .getUninterruptibly();
                        // 充值成功发送提醒短信，服务周到哇，微信优先~~~
                        if (ret && StringUtils.isNotBlank(chargingItem.getNotifySmsMessage())) {
                            final MutableBoolean wechatNoticeSended = new MutableBoolean(false);
                            Map<Long, List<UserWechatRef>> map = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(userId), WechatType.TEACHER);
                            List<UserWechatRef> refs = map.get(userId);
                            if (CollectionUtils.isNotEmpty(refs)) {
                                Map<String, Object> extensionInfo = MiscUtils.m("content", chargingItem.getNotifySmsMessage(), "mobile", StringUtils.mobileObscure(mobile));
                                refs.stream()
                                        .filter(source -> !source.isDisabledTrue())
                                        .forEach(e -> {
                                            wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.WirelessChargingNotice,
                                                    userId, e.getOpenId(), extensionInfo);
                                            wechatNoticeSended.setTrue();
                                        });
                            }
                            if (wechatNoticeSended.isFalse()) {
                                userSmsServiceClient.buildSms().to(chargingItem)
                                        .content(chargingItem.getNotifySmsMessage())
                                        .type(SmsType.WIRELESS_CHARGING_SUCCESS_SMS)
                                        .send();
                            }
                        }
                    } else if ("1002".equals(resultStatus)) {
                        // 订单支付中
                        // do nothing when the order is in charging process
                    } else if ("1004".equals(resultStatus)) {
                        // 订单不存在
                        wirelessChargingServiceClient.getWirelessChargingService()
                                .updateUnsubmit(orderId)
                                .awaitUninterruptibly();
                    } else {
                        wirelessChargingServiceClient.getWirelessChargingService()
                                .updateChargingFailed(orderId, resultCode, resultStatus)
                                .awaitUninterruptibly();
                    }
                } else {
                    // 系统错误，发送提示邮件，状态不变
                    Map<String, Object> content = new HashMap<>();
                    content.put("info", "CODE:" + resultCode + " status:" + resultStatus + " msg:" + SYS_ERROR_CODE_MAP.get(resultCode) + " RESPONSE:" + response.getResponseString());
                    emailServiceClient.createTemplateEmail(EmailTemplate.office)
                            .to("zhilong.hu@17zuoye.com")
                            .cc("yizhou.zhang@17zuoye.com")
                            .subject(RuntimeMode.getCurrentStage() + "环境畅天游无线充值系统错误")
                            .content(content)
                            .send();
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
