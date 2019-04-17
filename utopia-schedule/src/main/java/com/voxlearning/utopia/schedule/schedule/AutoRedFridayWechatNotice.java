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
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2015/10/27.
 */
@Named
@ScheduledJobDefinition(
        jobName = "最红星期五微信通知",
        jobDescription = "最红星期五微信通知，每周五2：00运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 2 ? 3,4,5,6,9,10,11,12 FRI",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoRedFridayWechatNotice extends ScheduledJobWithJournalSupport {

    @Inject private WechatServiceClient wechatServiceClient;
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
        //查询所有绑定了微信的认证老师
//        String authSql = "SELECT r.OPEN_ID, r.USER_ID " +
//                "FROM UCT_USER_WECHAT_REF r, UCT_USER u WHERE r.USER_ID = u.ID " +
//                "AND u.USER_TYPE = 1 AND u.AUTHENTICATION_STATE = 1 AND u.DISABLED = FALSE AND r.DISABLED = FALSE AND r.TYPE = 1;";
//        List<Map<String, Object>> teachers = utopiaSql.withSql(authSql).queryAll();
//        for (Map<String, Object> teacherMap : teachers) {
//            Long userId = ConversionUtils.toLong(teacherMap.get("USER_ID"));
//            String openId = teacherMap.get("OPEN_ID").toString();
//            wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.TeacherRedFridayNotice,
//                    userId, openId, MiscUtils.m("content", "最红星期五"));
//        }
//        progressMonitor.done();
    }
}
