package com.voxlearning.utopia.service.piclisten.impl.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramReadService;
import com.voxlearning.utopia.service.piclisten.consumer.cache.manager.PicListenReportCacheManager;
import com.voxlearning.utopia.service.piclisten.impl.service.AsyncPiclistenCacheServiceImpl;
import com.voxlearning.utopia.service.piclisten.impl.service.GrindEarServiceImpl;
import com.voxlearning.utopia.service.piclisten.impl.service.MySelfStudyQueueService;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenCollectData;
import com.voxlearning.utopia.temp.GrindEarActivity;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-03-15 下午7:46
 **/
@Named
public class PicListenCollectDataHandler extends SpringContainerSupport {


    private PicListenReportCacheManager picListenReportCacheManager;

    @Inject
    private AsyncPiclistenCacheServiceImpl asyncVendorCacheService;

    @Inject
    private MySelfStudyQueueService mySelfStudyService;  //改成广播方式，myselfstudy服务还在 vendor 里

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Inject
    private GrindEarServiceImpl grindEarService;

    @Inject
    private MiniProgramReadService miniProgramReadService;


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        picListenReportCacheManager = asyncVendorCacheService.getPicListenReportCacheManager();
    }

    public void handleData(PicListenCollectData data) {
        if (data == null)
            return;
        List<PicListenCollectData.SentenceResult> sentenceResultList = data.getSentenceResultList();
        if (CollectionUtils.isEmpty(sentenceResultList))
            return;
        Set<String> sentenceIdSet = sentenceResultList.stream().map(PicListenCollectData.SentenceResult::getSentenceId).collect(Collectors.toSet());
        Map<String, Future<Boolean>> sentencesIsReadFutureMap = picListenReportCacheManager.asyncSentencesIsRead(data.getStudentId(), data.getDayRange(), sentenceIdSet, "picListen");

        Long sumLearnTime = sentenceResultList.stream().mapToLong(PicListenCollectData.SentenceResult::getTime).sum();
        Long picListenSentenceCount = 0L;
        for (PicListenCollectData.SentenceResult sentenceResult : sentenceResultList) {
            Future<Boolean> future = sentencesIsReadFutureMap.get(sentenceResult.getSentenceId());
            if (future == null)
                continue;
            Boolean isRead = getFutureValue(future, false);
            if (!isRead)
                picListenSentenceCount++;
            String progress;
            String bookId = sentenceResult.getBookId();
            String unitId = sentenceResult.getUnitId();
            if (StringUtils.isNotBlank(unitId)) {
                progress = generatePicListenProgress(unitId);
            } else if (StringUtils.isNotBlank(bookId)) {
                progress = generatePicListenProgress(bookId);
            } else
                progress = "";
            if (StringUtils.isNotBlank(progress))
                mySelfStudyService.updateSelfStudyProgress(data.getStudentId(), SelfStudyType.PICLISTEN_ENGLISH, progress);
        }
        Long learnTimeAfter = picListenReportCacheManager.asyncUpdateScoreResult(data.getStudentId(), data.getDayRange(), sumLearnTime, picListenSentenceCount,
                null, null, null);
        if (GrindEarActivity.isInActivityPeriod()) {
            if (grindEarService.timeStandard(learnTimeAfter)) {
                grindEarService.pushTodayRecord(data.getStudentId(), DayRange.parse(data.getDayRange()).getStartDate(), false);
            }
        }
        picListenReportCacheManager.asyncAddListenedSentence(data.getStudentId(), data.getDayRange(), sentenceIdSet, "picListen");

        //MiniProgram read feature
        miniProgramReadService.incrReadData(data.getStudentId(), sumLearnTime, picListenSentenceCount.intValue());
        logger.debug("User: {} read time: {}", data.getStudentId(), sumLearnTime);


    }

    private String generatePicListenProgress(String progressId) {

        NewBookCatalog bookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(progressId);
        if (bookCatalog == null)
            return null;
        return bookCatalog.getName();
    }


    private <T> T getFutureValue(Future<T> future, T defaultValue) {
        try {
            return future.get();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
