package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.vendor.api.AsyncNewsCacheService;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsCacheType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsAlbum;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2017/5/9.
 */
@Named
@ScheduledJobDefinition(
        jobName = "专辑每周排序",
        jobDescription = "专辑排序，每周的执行一次",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 0 2 ? * MON "
)
@ProgressTotalWork(100)
public class AutoRankAlbum extends ScheduledJobWithJournalSupport {

    @Inject
    private JxtNewsLoaderClient jxtNewsLoaderClient;

    @Inject
    private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;
    @ImportService(interfaceClass = AsyncNewsCacheService.class)
    private AsyncNewsCacheService asyncNewsCacheService;


    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<JxtNewsAlbum> allOnlineJxtNewsAlbum = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum();
        if (CollectionUtils.isNotEmpty(allOnlineJxtNewsAlbum)) {
            List<String> albumIds = allOnlineJxtNewsAlbum.stream().map(JxtNewsAlbum::getId).collect(Collectors.toList());
            Map<String, Long> subCountMap = asyncNewsCacheService.JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_SUB_COUNT, albumIds).take();
            Comparator<Map.Entry<String, Long>> rankComparator = (a, b) -> Long.compare(SafeConverter.toLong(b.getValue()), SafeConverter.toInt(a.getValue()));
            List<String> albumRankIds = subCountMap.entrySet().stream().sorted(rankComparator).map(Map.Entry::getKey).collect(Collectors.toList());
            asyncNewsCacheService.JxtNewsCacheManager_saveAlbumSubCountRank(albumRankIds);
        }
    }
}
