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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.connection.DataSourceConnectionBuilder;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.SmsTaskStatus;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.config.consumer.BadWordCheckerClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.client.SmsTaskServiceClient;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.RowMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yuechen.wang
 * @since 2016/04/20
 */

@Named
@ScheduledJobDefinition(
        jobName = "自动发送短信管理平台审核通过的短信",
        jobDescription = "自动发送CRM短信管理平台下审核通过的短信，8-22点 每5分钟运行一次",
        disabled = {Mode.DEVELOPMENT, Mode.TEST, Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 */5 8-21 * * ? "
)
@ProgressTotalWork(100)
public class AutoSendCrmApprovedSmsJob extends ScheduledJobWithJournalSupport {

    @Inject private SmsServiceClient smsServiceClient;
    @Inject private SmsTaskServiceClient smsTaskServiceClient;
    @Inject private BadWordCheckerClient badWordCheckerClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private DataSourceConnectionBuilder dataSourceConnectionBuilder;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        logger.info("查询符合条件的短信任务...");


        // 一次任务不执行完所有的发送，每次取6000条
        String sql = "SELECT REF.ID AS refId, REF.SMS_TASK_ID AS taskId, T.SMS_TEXT content, T.SMS_TYPE smsType, SMS_RECEIVER as userToken, bin(REF.MOBILE_TOKEN) as isMobile " +
                " FROM VOX_SMS_TASK T, VOX_SMS_TASK_RECEIVER_REF REF " +
                " WHERE T.ID=REF.SMS_TASK_ID " +
                " AND T.STATUS=21 AND T.SMS_SENDTIME<=? AND T.DISABLED=FALSE " +
                " AND REF.STATUS=0 AND REF.DISABLED=FALSE " +
                " ORDER BY T.PRIORITY DESC LIMIT 6000 ";

        List<SmsReceiverMapper> results = dataSourceConnectionBuilder.getDataSourceConnection("hs_misc")
                .getJdbcTemplate()
                .query(sql, ROW_MAPPER, new Date(startTimestamp))
                .stream()
                .filter(t -> t.getTaskId() != null && t.getTaskId() > 0L)    // 任务ID不能为空
                .filter(t -> t.getRefId() != null && t.getRefId() > 0L)      // REF_ID不能为空
                .filter(t -> StringUtils.isNotBlank(t.getContent()))         // 短信文本不能为空
                .filter(t -> t.getSmsType() != null && t.getSmsType() != SmsType.NO_CATEGORY) // 短信类型
                .filter(t -> StringUtils.isNotBlank(t.getUserToken()))       // 用户信息不能为空
                .collect(Collectors.toList());                               // 这样一来就能保证接下来数据的准确性

        progressMonitor.worked(10);
        logger.info("查询到[{}]条待发的短信任务...", results.size());
        if (CollectionUtils.isEmpty(results)) {
            progressMonitor.done();
            return;
        }

        // 按照文案分组, 这样在一批任务中，同一个文案同一个手机号就不会收到多次了
        Map<String, List<SmsReceiverMapper>> smsSendMap = new LinkedHashMap<>();

        for (SmsReceiverMapper mapper : results) {
            // 解析手机号码
            String mobile = parseMobile(mapper);
            if (StringUtils.isNotBlank(mobile)) {
                mapper.setMobile(mobile);
                distinctAdd(smsSendMap, mapper);
            }
        }

        // 开始发送
        ISimpleProgressMonitor monitor = progressMonitor.subTask(80, smsSendMap.size());
        for (Map.Entry<String, List<SmsReceiverMapper>> entry : smsSendMap.entrySet()) {
            String content = entry.getKey();
            List<SmsReceiverMapper> receivers = entry.getValue();
            try {
                // 发送次数
                int times = receivers.size() / 30 + 1;
                // 分组发送
                for (List<SmsReceiverMapper> batch : CollectionUtils.splitList(receivers, times)) {
                    SmsType smsType = batch.get(0).getSmsType();
                    // 批量发送短信
                    List<String> mobiles = batch.stream().map(SmsReceiverMapper::getMobile).collect(Collectors.toList());
                    smsServiceClient.createSmsMessage(StringUtils.join(mobiles, ",")).content(content).type(smsType.name()).send();

                    // 批量更新状态
                    List<Long> refs = batch.stream().map(SmsReceiverMapper::getRefId).collect(Collectors.toList());
                    smsServiceClient.getSmsService()
                            .batchUpdateReceiverRefStatus(refs, 1, RandomUtils.randomString(8)) // 备注为批次号，表示同一批发送出去的
                            .awaitUninterruptibly();

                    // 休息一会
                    try {
                        Thread.sleep(100);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ex) {
                logger.error("An error occurred when running SmsTask Job...", ex);
            } finally {
                monitor.worked(1);
            }
        }

        // 更新任务状态
        Set<Long> taskIds = results.stream().map(SmsReceiverMapper::getTaskId).collect(Collectors.toSet());
        for (Long taskId : taskIds) {
            if (SafeConverter.toInt(smsServiceClient.getSmsService().getSmsTaskLeftRefCount(taskId).getUninterruptibly()) == 0) {
                // 表示该短信任务下没有可以发送短信的用户，更新短信任务状态为成功
                smsTaskServiceClient.getSmsTaskService()
                        .updateSmsTaskStatus(taskId, SmsTaskStatus.SEND_SUCCESS)
                        .awaitUninterruptibly();
            }
        }
        progressMonitor.done();
    }

    private String parseMobile(SmsReceiverMapper smsTask) {
        if (smsTask == null || smsTask.getRefId() == null) {
            return null;
        }
        String validMobile;
        Long refId = smsTask.getRefId();
        // 获取用户的手机号
        if (Boolean.TRUE.equals(smsTask.getIsMobile())) {
            validMobile = smsTask.getUserToken();
        } else {
            Long userId = SafeConverter.toLong(smsTask.getUserToken());
            validMobile = sensitiveUserDataServiceClient.showUserMobile(userId, "AdminSmsTask", SafeConverter.toString(userId));
            if (StringUtils.isBlank(validMobile)) {
                //logger.info("user does not exist or user's mobile is null , user:{}", userId);
                smsServiceClient.getSmsService()
                        .updateReceiverRefStatus(refId, 9, "用户未绑定手机号")
                        .awaitUninterruptibly();
                return null;
            }
        }
        // 检查手机号
        if (!MobileRule.isMobile(validMobile)) {
            //logger.info("receiver's mobile is incorrect, mobile: {}", validMobile);
            smsServiceClient.getSmsService()
                    .updateReceiverRefStatus(refId, 9, "用户手机号码无效")
                    .awaitUninterruptibly();
            return null;
        }
        if (badWordCheckerClient.containsMobileNumBadWord(validMobile)) {
            //logger.info("receiver's mobile is in bad mobile number list, mobile:{} ", validMobile);
            smsServiceClient.getSmsService()
                    .updateReceiverRefStatus(refId, 9, "用户设置了短信屏蔽")
                    .awaitUninterruptibly();
            return null;
        }
        return validMobile;
    }

    private void distinctAdd(Map<String, List<SmsReceiverMapper>> smsMap, SmsReceiverMapper mapper) {
        String content = mapper.getContent();
        if (!smsMap.containsKey(content)) {
            smsMap.put(content, new LinkedList<>());
        }

        List<SmsReceiverMapper> mapperList = smsMap.get(content);
        // 同样的文本有相同的手机号，假设已经发送成功，先把状态更新了
        if (mapperList.stream().anyMatch(t -> Objects.equals(t.getMobile(), mapper.getMobile()))) {
            smsServiceClient.getSmsService()
                    .updateReceiverRefStatus(mapper.getRefId(), 1, "有其他相同内容的短信")
                    .awaitUninterruptibly();
            return;
        }
        mapperList.add(mapper);
    }

    private final RowMapper<SmsReceiverMapper> ROW_MAPPER = (rs, rowNum) -> {
        SmsReceiverMapper mapper = new SmsReceiverMapper();
        mapper.setTaskId(rs.getLong("taskId"));
        mapper.setRefId(rs.getLong("refId"));
        mapper.setContent(rs.getString("content"));
        mapper.setSmsType(SmsType.of(rs.getString("smsType")));
        mapper.setUserToken(rs.getString("userToken"));
        mapper.setIsMobile(rs.getBoolean("isMobile"));
        return mapper;
    };

    @Setter
    @Getter
    private class SmsReceiverMapper {
        private Long taskId;       // 短信任务ID
        private Long refId;        // 发送人ID
        private String content;    // 短信文案
        private SmsType smsType;   // 发送短信类型
        private String userToken;  // 发送用户
        private Boolean isMobile;  // 是否是手机号

        private String mobile;     // 用户手机号
    }

}
