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

package com.voxlearning.utopia.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.TeacherSummaryEsInfo;
import com.voxlearning.utopia.service.user.api.mappers.TeacherSummaryQuery;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherSummaryEsServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 每天中午12点给满足人工认证条件的老师，发送系统消息+模板消息提示
 *
 * @author changyuan
 * @since 2016/6/23
 */
@Named
@ScheduledJobDefinition(
        jobName = "提醒老师认证任务",
        jobDescription = "每天中午12点给满足人工认证条件的老师，发送系统消息+模板消息提示",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 12 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoSendNoticeForTeacherAuth extends ScheduledJobWithJournalSupport {

    static private final String NOTICE_TEXT = "亲爱的老师，您已满足认证条件，请注意接听010开头的电话，一起作业客服人员会与您电话沟通完成认证。";
    static private final int PAGE_SIZE = 100;

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private WechatLoaderClient wechatLoaderClient;
    @Inject private WechatServiceClient wechatServiceClient;
    @Inject private TeacherSummaryEsServiceClient teacherSummaryEsServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        int page = 1;
        int handledCount = 0;
        int totalCount = 0;

        do {
            TeacherSummaryQuery query = new TeacherSummaryQuery();
            query.setAuthCond1reached(true);
            query.setAuthCond2reached(true);
            query.setAuthCond3reached(true);
            query.setAuthStatus("WAITING");
            query.setPage(page);
            query.setLimit(PAGE_SIZE);

            // es查询
            Page<TeacherSummaryEsInfo> pageInfo = teacherSummaryEsServiceClient.getTeacherSummaryEsService().query(query);
            Set<Long> teacherIds = pageInfo.getContent().stream().map(TeacherSummaryEsInfo::getTeacherId).collect(Collectors.toSet());
            totalCount = (int) pageInfo.getTotalElements();

            for (Long tid : teacherIds) {
                // 过滤学校未认证的老师
                School school = asyncTeacherServiceClient.getAsyncTeacherService()
                        .loadTeacherSchool(tid)
                        .getUninterruptibly();
                if (null == school || school.getSchoolAuthenticationState() != AuthenticationState.SUCCESS || school.isTraingingSchool()) {
                    continue;
                }
                // 发送系统消息
                teacherLoaderClient.sendTeacherMessage(tid, NOTICE_TEXT);
                // 右下角弹窗
                userPopupServiceClient.createPopup(tid)
                        .content(NOTICE_TEXT)
                        .type(PopupType.TEACHER_ALTERATION_FOR_RESPONDENT)
                        .category(PopupCategory.LOWER_RIGHT)
                        .create();
                // 发送微信模板消息
                Map<String, Object> extensionInfo = MiscUtils.m("first", "认证进度通知",
                        "keyword1", "一起作业",
                        "keyword2", "您已满足认证条件，请注意接听010开头的客服电话",
                        "url", ProductConfig.get("wechat.url") + "/teacher/message/list.vpage?_from=wechatnotice");
                Map<Long, List<UserWechatRef>> userWechatRefs = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(tid), WechatType.TEACHER);
                List<UserWechatRef> refs = userWechatRefs.get(tid);
                if (CollectionUtils.isNotEmpty(refs)) {
                    wechatServiceClient.processWechatNotice(
                            WechatNoticeProcessorType.TeacherOperationNotice, tid, extensionInfo, WechatType.TEACHER);
                }
            }
            page++;
            handledCount += teacherIds.size();
        } while (handledCount < totalCount);
    }
}
