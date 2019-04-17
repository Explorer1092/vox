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
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.afenti.consumer.AfentiServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/8/4
 */
@Named
@ScheduledJobDefinition(
        jobName = "给没有阿分题关卡的书生成关卡数据",
        jobDescription = "提供BOOK手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 40 4 ? * MON",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoGenerateAfentiRankManagerJob extends ScheduledJobWithJournalSupport {
    @Inject private AfentiServiceClient afentiServiceClient;
    @Inject private NewContentLoaderClient newContentLoaderClient;

    public static final List<String> CANDIDATE_PUBLISHER = Collections.unmodifiableList(
            // 人教版，苏教版，北京版，北师大版
            Arrays.asList("BKC_10100000002119", "BKC_10100001245546", "BKC_10100000618021", "BKC_10100078307663")
    );

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        //有参数时优先运行参数
        if (MapUtils.isNotEmpty(parameters)) {
            boolean dryRun = SafeConverter.toBoolean(parameters.get("dryRun"));
            String subjectStr = SafeConverter.toString(parameters.get("subject"));
            Subject subject = Subject.safeParse(subjectStr);
            List<String> bookIds = (List<String>) parameters.get("bookIds");
            if (subject != null && CollectionUtils.isNotEmpty(bookIds) ) {
                 if (dryRun) {
                      logger.info("auto generate afenti rank bookIds:{} , subject:{}", bookIds, subject);
                 } else {
                     switch (subject) {
                         case ENGLISH:
                             afentiServiceClient.generateAfentiRank(bookIds, Subject.ENGLISH);
                             break;
                         case CHINESE:
                             afentiServiceClient.generateAfentiRankForChinese(bookIds);
                             break;
                         case MATH:
                             afentiServiceClient.generateAfentiRankForMath(bookIds);
                             break;
                         default:
                             break;
                     }
                 }
            }
            return;
        }
        // 获取备选英语教材
        Set<String> ebids = newContentLoaderClient.loadBooks(Subject.ENGLISH).stream()
                .filter(NewBookProfile::isOnline)
                .filter(b -> StringUtils.equals(b.getBookType(), "TEXTBOOK"))
                .filter(b -> b.getClazzLevel() != null && Arrays.asList(1, 2, 3, 4, 5, 6).contains(b.getClazzLevel()))
                .filter(b -> b.getLatestVersion() == 1)
                .map(NewBookProfile::getId)
                .collect(Collectors.toSet());
        logger.info("There are total {} english books found.", ebids.size());
        progressMonitor.worked(2); // 完成3%工作量

        // 获取备选数学教材
        Set<String> mbids = newContentLoaderClient.loadBooks(Subject.MATH).stream()
                .filter(NewBookProfile::isOnline)
                .filter(b -> b.getTermType() == Term.上学期.getKey())
                .filter(b -> StringUtils.equals(b.getBookType(), "TEXTBOOK"))
                .filter(b -> b.getClazzLevel() != null && Arrays.asList(1, 2, 3, 4, 5, 6).contains(b.getClazzLevel()))
                .filter(b -> b.getLatestVersion() == 1)
                .map(NewBookProfile::getId)
                .collect(Collectors.toSet());
        logger.info("There are total {} math books found.", mbids.size());
        progressMonitor.worked(2); // 完成3%工作量

        // 获取备选语文教材
        Set<String> cbids = newContentLoaderClient.loadBooks(Subject.CHINESE).stream()
                .filter(NewBookProfile::isOnline)
                .filter(b -> StringUtils.equals(b.getBookType(), "TEXTBOOK"))
                .filter(b -> b.getClazzLevel() != null && Arrays.asList(1, 2, 3, 4, 5, 6).contains(b.getClazzLevel()))
                .filter(b -> CANDIDATE_PUBLISHER.contains(b.getSeriesId()))
                .filter(b -> b.getLatestVersion() == 1)
                .map(NewBookProfile::getId)
                .collect(Collectors.toSet());
        logger.info("There are total {} chinese books found.", cbids.size());

        afentiServiceClient.generateAfentiRank(ebids, Subject.ENGLISH);
        progressMonitor.worked(40);
        afentiServiceClient.generateAfentiRankForMath(mbids);
        progressMonitor.worked(33);
        afentiServiceClient.generateAfentiRankForChinese(cbids);
        progressMonitor.worked(23);
    }
}
