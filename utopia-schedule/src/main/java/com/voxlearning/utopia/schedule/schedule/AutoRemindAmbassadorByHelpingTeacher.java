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
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by XiaoPeng.Yang on 15-4-23.
 */
@Named
@ScheduledJobDefinition(
        jobName = "每天统计校园大使上一天帮助老师认证成功提醒",
        jobDescription = "每天10:00执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 10 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoRemindAmbassadorByHelpingTeacher extends ScheduledJobWithJournalSupport {

    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private WechatServiceClient wechatServiceClient;

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
        Date start = DayRange.current().previous().getStartDate();
        Date end = DayRange.current().previous().getEndDate();
//        Date start = DateUtils.stringToDate("2015-04-24 00:00:00");
//        Date end = DateUtils.stringToDate("2015-04-24 23:59:59");
        String sql = "SELECT AMBASSADOR_ID,COUNT(1) AS COUNT FROM VOX_AMBASSADOR_AUTHENTICATION_TEACHER " +
                "WHERE DISABLED=1 AND UPDATE_DATETIME>=? AND UPDATE_DATETIME<=? GROUP BY AMBASSADOR_ID";
        List<Map<String, Object>> dataList = utopiaSql.withSql(sql).useParamsArgs(start, end).queryAll();
        if (dataList.isEmpty()) {
            return;
        }
        progressMonitor.worked(20);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(80, dataList.size());
        for (Map<String, Object> data : dataList) {
            try {
                //微信信息
                Long ambassadorId = ConversionUtils.toLong(data.get("AMBASSADOR_ID"));
                int count = ConversionUtils.toInt(data.get("COUNT"));
                User ambassador = userLoaderClient.loadUser(ambassadorId);
                Map<String, Object> extensions = new HashMap<>();
                extensions.put("teacherName", ambassador.fetchRealname());
                extensions.put("datetime", DateUtils.dateToString(new Date()));
                extensions.put("type", "帮助老师认证得园丁豆");
                extensions.put("amount", 100 * count + "园丁豆");
                extensions.put("count", count);
                extensions.put("balance", teacherLoaderClient.loadMainSubTeacherUserIntegral(ambassador.getId(), null).getUsable());
                wechatServiceClient.processWechatNotice(
                        WechatNoticeProcessorType.TeacherIntegralRemindNotice,
                        ambassadorId,
                        extensions,
                        WechatType.TEACHER
                );
                //站内信
                teacherLoaderClient.sendTeacherMessage(ambassador.getId(), ambassador.fetchRealname() + "老师您好，您帮助的" + count + "名老师已认证，获得" + count * 100 + "园丁豆！");
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }
}