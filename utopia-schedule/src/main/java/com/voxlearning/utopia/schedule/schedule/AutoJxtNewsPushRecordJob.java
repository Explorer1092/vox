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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.api.entity.JxtExtTab;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsPushRecord;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.JxtLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.JxtServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2016/11/30
 */
@Named
@ScheduledJobDefinition(
        jobName = "资讯精选推送jPush消息",
        jobDescription = "资讯精选推送jPush消息，每5分钟运行一次",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 */5 * * * ?"
)
@ProgressTotalWork(100)
public class AutoJxtNewsPushRecordJob extends ScheduledJobWithJournalSupport {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private JxtNewsLoaderClient jxtNewsLoaderClient;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private JxtServiceClient jxtServiceClient;
    @Inject
    private JxtLoaderClient jxtLoaderClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<JxtNewsPushRecord> jxtNewsPushRecordList = jxtNewsLoaderClient.getAllOnlineJxtNewsPushRecord();

        jxtNewsPushRecordList = jxtNewsPushRecordList.stream()
                .filter(e -> e.getStartTime().after(DateUtils.addMinutes(new Date(), -5))
                        && e.getStartTime().before(new Date()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(jxtNewsPushRecordList)) {
            return;
        }

        progressMonitor.worked(10);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, jxtNewsPushRecordList.size());

        for (JxtNewsPushRecord pushRecord : jxtNewsPushRecordList) {
            try {
                if (!pushRecord.getIsSendPush()) {
                    continue;
                }

                //获取通用参数
                String notifyContent = pushRecord.getPushContent().trim();
                //持续时间60分钟
                Integer durationTime = SafeConverter.toInt(pushRecord.getDuration()) < 30 ? 30 : SafeConverter.toInt(pushRecord.getDuration());
                //学段默认为小学
                AppMessageSource source = AppMessageSource.PARENT;
                String allMessageTag = JpushUserTag.USER_ALL_ONLY_FOR_PARENT.tag;

                Map<String, Object> jpushExtInfo = new HashMap<>();
                //这个字段给客户端点击消息进入列表后返回首页清除首页-系统消息 tab的红点使用
                jpushExtInfo.put("ext_tab_message_type", ParentAppJxtExtTabTypeToNative.USER_MESSAGE.getType());
                jpushExtInfo.put("studentId", "");
                if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                    jpushExtInfo.put("url", "https://www.test.17zuoye.net/view/mobile/parent/album/handpick.vpage?rel=push");
                } else if (RuntimeMode.isStaging()) {
                    jpushExtInfo.put("url", "https://www.staging.17zuoye.net/view/mobile/parent/album/handpick.vpage?rel=push");
                } else {
                    jpushExtInfo.put("url", "https://www.17zuoye.com/view/mobile/parent/album/handpick.vpage?rel=push");
                }
                jpushExtInfo.put("tag", ParentMessageTag.资讯.name());
                jpushExtInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW.name());
                jpushExtInfo.put("shareContent", "");
                jpushExtInfo.put("shareUrl", "");
                jpushExtInfo.put("s", ParentAppPushType.JXT_NEWS.name());

                // jpush发送tag ||
                List<String> orTags = new ArrayList<>();
                // jpush发送tag &&
                List<String> andTags = new ArrayList<>();

                int pushType = pushRecord.generatePushType();
                //指定用户
                if (pushType == 1) {
                    List<Long> userIdList = new ArrayList<>();
                    userIdList.add(pushRecord.getAvailableUserId());
                    appMessageServiceClient.sendAppJpushMessageByIds(notifyContent, source, userIdList, jpushExtInfo);
                } else if (pushType == 2) { //全部用户
                    orTags.add(allMessageTag);
                    //默认学段是小学
                    andTags.add(JpushUserTag.PRIMARY_SCHOOL.tag);
                    appMessageServiceClient.sendAppJpushMessageByTags(notifyContent, source, orTags, andTags, jpushExtInfo, durationTime);

                    //tab副标题 只有推送给全部用户是才改tab副标题
                    JxtExtTab jxtExtTab = jxtLoaderClient.getAllOnlineJxtExtTabList()
                            .stream()
                            .filter(e -> Long.valueOf(10001L).equals(e.getTabType()))
                            .findFirst()
                            .orElse(null);
                    if (jxtExtTab != null) {
                        jxtExtTab.setDesc(pushRecord.getSubHeading());
                        jxtServiceClient.saveJxtExtTab(jxtExtTab);
                    }
                } else if (pushType == 3) {
                    List<Integer> regionList = pushRecord.getRegionCodeList();
                    raikouSystem.getRegionBuffer().loadRegions(regionList).values().forEach(exRegion -> {
                        if (exRegion != null) {
                            if (exRegion.fetchRegionType() == RegionType.PROVINCE) {
                                orTags.add(JpushUserTag.PROVINCE.generateTag(exRegion.getId().toString()));
                            } else if (exRegion.fetchRegionType() == RegionType.CITY) {
                                orTags.add(JpushUserTag.CITY.generateTag(exRegion.getId().toString()));
                            } else if (exRegion.fetchRegionType() == RegionType.COUNTY) {
                                orTags.add(JpushUserTag.COUNTY.generateTag(exRegion.getId().toString()));
                            }
                        }
                    });

                    andTags.add(JpushUserTag.PRIMARY_SCHOOL.tag);

                    appMessageServiceClient.sendAppJpushMessageByTags(notifyContent, source, orTags, andTags, jpushExtInfo, durationTime);
                }


            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }
}
