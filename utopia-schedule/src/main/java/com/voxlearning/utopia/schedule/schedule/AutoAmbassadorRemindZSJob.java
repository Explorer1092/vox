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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/3/30.
 */
@Named
@ScheduledJobDefinition(
        jobName = "正式大使观察期提醒任务",
        jobDescription = "每天00:10执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 10 0 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoAmbassadorRemindZSJob extends ScheduledJobWithJournalSupport {

    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        // 1 2月不执行考核
        int month = MonthRange.current().getMonth();
        if (month == 1 || month == 2 || month == 7 || month == 8) {
            jobJournalLogger.log("寒暑假期间不执行。");
            return;
        }

        // 获取所有观察期大使
        String sxsql = "SELECT AMBASSADOR_ID FROM VOX_AMBASSADOR_LEVEL_DETAIL WHERE DISABLED=FALSE AND IS_OBSERVATION=TRUE";
        List<Long> ambassadorIds = utopiaSql.withSql(sxsql).queryColumnValues(Long.class);
        if (CollectionUtils.isEmpty(ambassadorIds)) {
            jobJournalLogger.log("没有需要提醒的大使！");
            return;
        }
        progressMonitor.worked(5);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(95, ambassadorIds.size());
        for (Long ambassadorId : ambassadorIds) {
            try {
                // 离月底还有7天 14天 分别提醒
                long diff = DateUtils.dayDiff(MonthRange.current().getEndDate(), new Date());
                if (diff == 7 || diff == 14) {
                    String pcMsg = "距离大使观察期结束还有" + diff + "天，您尚未达到最低经验值；如无法达成，将变回普通老师哦。（请登录电脑端-『校园大使』页面查看规则）";
                    // 发送通知
                    String msg = "距离大使观察期结束还有" + diff + "天，您尚未达到最低经验值；如无法达成，将变回普通老师哦。<a href='/ambassador/center.vpage'>查看规则</a>";
                    sendRemind(ambassadorId, msg, pcMsg);
                }
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }

    private void sendRemind(Long ambassadorId, String msg, String pcMsg) {
        // 发送微信模板消息 以及短信
        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(ambassadorId);
        if (authentication != null && authentication.isMobileAuthenticated()) {
            // 发短信
            userSmsServiceClient.buildSms().to(authentication)
                    .content(msg)
                    .type(SmsType.AMBASSADOR_REMIND_SMS)
                    .send();
        }
        // 发模板消息  本期不做了。 等待微信号功能开通后加上
        userPopupServiceClient.createPopup(ambassadorId).content(pcMsg)
                .type(PopupType.AMBASSADOR_NOTICE).category(PopupCategory.LOWER_RIGHT).create();
    }
}
