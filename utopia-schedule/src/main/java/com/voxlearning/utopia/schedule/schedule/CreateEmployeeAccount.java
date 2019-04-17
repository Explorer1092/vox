/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.user.consumer.ClazzServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * redmine 25983
 * 1.建立［一起作业培训学校］，性质：培训学校，前台不可见
 * 2.根据工号建立相关帐号，例如工号为00024，建立老师帐号为100024，建立一个班级包含2个相邻id的老师，学生帐号为300024，使其进入其班级。
 * （其中奇数是英语老师，偶数为数学老师，满足逻辑300024的学生，对应英语老师为100023，对应数学老师为100024（此步骤要求学生能否不走强行绑定手机流程）
 * 生成2000组帐号。老师100001-102000，学生300024-302000
 * 3.家长号逻辑可以自行绑定手机。（应该有不少员工的手机绑定了，可以发邮件给qa解绑）
 *
 * @author changyuan
 * @since 2016/6/28
 */
@Named
@ScheduledJobDefinition(
        jobName = "建立员工体验账号体系",
        jobDescription = "建立员工体验账号体系，脚本任务，只执行一次",
        disabled = {Mode.UNIT_TEST},
        cronExpression = "0 0 3 1 1 ?",
        ENABLED = true
)
@ProgressTotalWork(100)
public class CreateEmployeeAccount extends ScheduledJobWithJournalSupport {

    @Inject private ClazzServiceClient clazzServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        clazzServiceClient.createEmployeeAccount();
    }
}