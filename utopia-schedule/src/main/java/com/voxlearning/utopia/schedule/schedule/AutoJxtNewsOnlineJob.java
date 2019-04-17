package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.ThreadUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.vendor.api.CRMVendorService;
import com.voxlearning.utopia.service.vendor.api.JxtNewsLoader;
import com.voxlearning.utopia.service.vendor.api.JxtNewsService;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;

import javax.inject.Named;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2016/10/24.
 */
@Named
@ScheduledJobDefinition(
        jobName = "家长通资讯定时发布资讯",
        jobDescription = "每5分钟运行一次",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 0/5 * * * ?"
)
@ProgressTotalWork(100)
public class AutoJxtNewsOnlineJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = CRMVendorService.class)
    private CRMVendorService crmVendorService;

    @ImportService(interfaceClass = JxtNewsLoader.class)
    private JxtNewsLoader jxtNewsLoader;


    @ImportService(interfaceClass = JxtNewsService.class)
    private JxtNewsService jxtNewsService;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<JxtNews> offlineNewsList = jxtNewsLoader.getAllOfflineJxtNews();
        if (CollectionUtils.isNotEmpty(offlineNewsList)) {
            offlineNewsList = offlineNewsList.stream().filter(jxtNews -> jxtNews.getPublishTime() != null && jxtNews.getPublishTime() > 0)
                    .filter(jxtNews -> Instant.ofEpochMilli(jxtNews.getPublishTime()).isBefore(Instant.now()) && Instant.ofEpochMilli(jxtNews.getPublishTime()).isAfter(Instant.now().minusSeconds(300)))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(offlineNewsList)) {
                return;
            }
            progressMonitor.worked(5);
            progressMonitor.subTask(95, offlineNewsList.size());
            offlineNewsList.forEach(jxtNews -> {
                jxtNews.setOnline(true);
                jxtNews.setPushTime(new Date());
                crmVendorService.$upsertJxtNews(jxtNews);
                progressMonitor.worked(1);
            });
//            List<String> albumIds = offlineNewsList.stream().filter(e -> StringUtils.isNotBlank(e.getAlbumId())).map(JxtNews::getAlbumId).collect(Collectors.toList());
//            for (String albumId : albumIds) {
//                //jxtNewsService.sendUpdateAlbumMessageToSubUser(albumId);
//                AlpsThreadPool.getInstance().submit(() -> sendAlbumReminder(albumId));
//
//            }
            progressMonitor.done();
        }
    }

    private void sendAlbumReminder(String albumId) {
        if (StringUtils.isBlank(albumId)) {
            return;
        }
        Pageable pageable = new PageRequest(0, 1000);
        Page<Long> userIdsByPage = jxtNewsLoader.getAlbumSubUserIdsByAlbumId(albumId, pageable);
        Set<Long> userIds = new HashSet<>();
        if (userIdsByPage.getTotalElements() == 0) {
            return;
        }
        //FIXME:这里处理第一次
        userIds.addAll(userIdsByPage.getContent());
        while (userIdsByPage.hasNext()) {
            //FIXME:这里处理第二至第N次
            pageable = userIdsByPage.nextPageable();
            userIdsByPage = jxtNewsLoader.getAlbumSubUserIdsByAlbumId(albumId, pageable);
            userIds.addAll(userIdsByPage.getContent());

        }
        //FIXME:查出订阅用户ID,然后增加更新数
        List<List<Long>> userSplitIds = CollectionUtils.splitList(new ArrayList<>(userIds), userIds.size() / 5000 + 1);
        for (List<Long> userIdList : userSplitIds) {
            jxtNewsService.sendMessageToReminder(userIdList);
            ThreadUtils.sleepCurrentThread(5, TimeUnit.SECONDS);
        }
    }

}
