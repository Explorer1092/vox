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

package com.voxlearning.utopia.service.piclisten.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.api.constant.UnisoundScoreLevel;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.CommonConfig;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.piclisten.cache.PiclistenCache;
import com.voxlearning.utopia.service.piclisten.consumer.cache.manager.FollowReadShareLikeRankCacheManager;
import com.voxlearning.utopia.service.piclisten.consumer.cache.manager.PicListenReportCacheManager;
import com.voxlearning.utopia.service.piclisten.impl.dao.*;
import com.voxlearning.utopia.service.piclisten.impl.handler.StudentFollowReadReportGenerator;
import com.voxlearning.utopia.service.piclisten.impl.loader.TextBookManagementLoaderImpl;
import com.voxlearning.utopia.service.piclisten.impl.queue.PicListenCollectDataQueueProducer;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.FollowReadLikeRangeType;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import org.springframework.core.convert.converter.Converter;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-03-02 下午3:25
 **/
@Named
@Service(interfaceClass = ParentSelfStudyService.class)
@ExposeServices({
        @ExposeService(interfaceClass = ParentSelfStudyService.class, version = @ServiceVersion(version = "ALPS")),
        @ExposeService(interfaceClass = ParentSelfStudyService.class, version = @ServiceVersion(version = "20171013"))
})
public class ParentSelfStudyServiceImpl extends SpringContainerSupport implements ParentSelfStudyService {

    private static final int MAX_TRY_COUNT_GENERATE_FLTRP_MOBILE = 5;
    private static final String FLTRP_MOBILE_HIT_COUNT = "FLTRP_MOBILE_HIT_COUNT";

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private PicListenBookShelfDao picListenBookShelfDao;
    @Inject
    private FltrpMobileDao fltrpMobileDao;

    @Inject
    private AsyncPiclistenCacheServiceImpl asyncVendorCacheService;

    @Inject
    private FollowReadSentenceResultDao followReadSentenceResultDao;

    @Inject
    private FollowReadCollectionDao followReadCollectionDao;

    @Inject
    private PicListenCollectDataQueueProducer picListenCollectDataQueueProducer;

    private PicListenReportCacheManager picListenReportCacheManager;
    private FollowReadShareLikeRankCacheManager followReadShareLikeRankCacheManager;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Inject
    private StudentFollowReadReportDao studentFollowReadReportDao;

    @Inject
    private StudentFollowReadReportGenerator studentFollowReadReportGenerator;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Inject
    private TextBookManagementLoaderImpl textBookManagementLoader;

    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Inject
    private StudyToolShelfDao studyToolShelfDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        picListenReportCacheManager = asyncVendorCacheService.getPicListenReportCacheManager();
        followReadShareLikeRankCacheManager = asyncVendorCacheService.getFollowReadShareLikeRankCacheManager();
    }

    @Override
    public StudyToolShelf loadStudentStudyToolShelf(Long studentId) {
        return studyToolShelfDao.load(studentId);
    }

    @Override
    public void updateStudentStudyToolShelf(Long studentId, List<SelfStudyType> selfStudyTypeList) {
        studyToolShelfDao.updateList(studentId, selfStudyTypeList);
    }


    @Override
    public Map<Long, List<PicListenBookShelf>> loadParentPicListenBookShelves(Collection<Long> parentIds) {
        if (CollectionUtils.isEmpty(parentIds))
            return Collections.emptyMap();
        return picListenBookShelfDao.loadParentPicListenShelves(parentIds);
    }

    @Override
    public MapMessage deleteBookFromPicListenShelf(Long parentId, String bookId) {
        picListenBookShelfDao.deletePicListenBookShelf(parentId, bookId);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteBooksFromPicListenShelf(Long parentId, List<String> bookIds) {
        if (bookIds.size() > 0) {
            List<String> ids = bookIds.stream().filter(x -> x.length() > 0).map(String::trim).collect(Collectors.toList());
            picListenBookShelfDao.deletePicListenBooksShelf(parentId, ids);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage addBook2PicListenShelf(Long parentId, String bookId) {
        List<PicListenBookShelf> picListenBookShelfs = loadParentPicListenBookShelf(parentId);
        if (CollectionUtils.isNotEmpty(picListenBookShelfs)
                && picListenBookShelfs.stream().anyMatch(t -> bookId.equals(t.getBookId())))
            return MapMessage.successMessage();
        picListenBookShelfDao.addPicListenBookShelf(parentId, bookId);
        return MapMessage.successMessage();
    }

    @Override
    public Long parentPicListenBookShelfCountIncludeDisabled(Long parentId) {
        if (parentId == null || parentId == 0)
            return 0L;
        return picListenBookShelfDao.countParentShelfBook(parentId);
    }

    @Override
    public void initParentPicListenBookShelfBooks(Long parentId, Set<String> bookIds) {
        if (parentId == null || parentId == 0 || CollectionUtils.isEmpty(bookIds))
            return;
        String lock = "initParentPicListenBookShelfBooks_" + parentId;
        try {
            AtomicLockManager.instance().acquireLock(lock);
        } catch (CannotAcquireLockException e) {
            return;
        }
        try {
            Long bookCount = parentPicListenBookShelfCountIncludeDisabled(parentId);
            if (bookCount > 0)
                return;
            List<PicListenBookShelf> picListenBookShelfs = bookIds.stream().map(bookId -> new PicListenBookShelf(parentId, bookId)).collect(Collectors.toList());
            picListenBookShelfDao.inserts(picListenBookShelfs);
        } finally {
            AtomicLockManager.instance().releaseLock(lock);
        }
    }

    @Override
    public List<PicListenBookShelf> mergePicListenBookShelf(Long parentId, List<String> bookIdList) {
        if (parentId == null || parentId == 0 || CollectionUtils.isEmpty(bookIdList))
            return Collections.emptyList();
        Set<String> alreadyInShelfBookIdSet = loadParentPicListenBookShelf(parentId).stream()
                .map(PicListenBookShelf::getBookId).collect(Collectors.toSet());
        List<PicListenBookShelf> picListenBookShelfs = bookIdList.stream().filter(t -> !alreadyInShelfBookIdSet.contains(t))
                .map(bookId -> new PicListenBookShelf(parentId, bookId)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(picListenBookShelfs))
            picListenBookShelfDao.inserts(picListenBookShelfs);
        return loadParentPicListenBookShelf(parentId);
    }

    @Override
    public AlpsFuture<Boolean> loadIsFollowReadOverLimit(Long userId, String picListenId, Long sentenceId) {
        Boolean aBoolean = asyncVendorCacheService.getFollowReadDayReadCacheManager().todayIsOverLimit_sentence(userId, picListenId, sentenceId);
        return new ValueWrapperFuture<>(aBoolean);
    }

    @Override
    public MapMessage processFollowResult(FollowReadSentenceResult followReadSentenceResult) {
        Boolean todayIsOverLimit = asyncVendorCacheService.getFollowReadDayReadCacheManager().todayIsOverLimit_sentence(followReadSentenceResult.getStudentId(),
                followReadSentenceResult.getPicListenId(), followReadSentenceResult.getSentenceId());
        if (todayIsOverLimit)
            return MapMessage.errorMessage("over limit");
        Future<Boolean> sentencesIsFollowedFuture = picListenReportCacheManager.asyncSentencesIsRead(followReadSentenceResult.getStudentId(), DayRange.current().toString(),
                Collections.singleton(followReadSentenceResult.getSentenceId().toString()), "follow").get(followReadSentenceResult.getSentenceId().toString());
        followReadSentenceResult.generateId();
        followReadSentenceResultDao.insert(followReadSentenceResult);
        asyncVendorCacheService.getFollowReadDayReadCacheManager().todayReadOne_sentence(followReadSentenceResult.getStudentId(),
                followReadSentenceResult.getPicListenId(), followReadSentenceResult.getSentenceId());
        UnisoundScoreLevel unisoundScoreLevel = UnisoundScoreLevel.processLevel(followReadSentenceResult.fetchSentenceScore());
        //收集点读报告数据
        Boolean isFollowed = getFutureValue(sentencesIsFollowedFuture, false);
        picListenReportCacheManager.asyncUpdateScoreResult(followReadSentenceResult.getStudentId(), DayRange.current().toString(),
                (long) followReadSentenceResult.getRecordTime(), null, isFollowed ? 0L : 1L, null, null);
        picListenReportCacheManager.studentDayActiveFollowRead(DayRange.current(), followReadSentenceResult.getStudentId());
        return MapMessage.successMessage().add("result_id", followReadSentenceResult.getId()).add("level", unisoundScoreLevel.name());
    }

    @Override
    public Map<String, FollowReadSentenceResult> loadFollowReadSentenceResults(Collection<String> ids) {
        return followReadSentenceResultDao.loads(ids);
    }

    @Override
    public MapMessage generateFollowReadCollection(Long studentId, String unitId, List<String> scoreIdList) {
        if (studentId == null || StringUtils.isBlank(unitId) || CollectionUtils.isEmpty(scoreIdList))
            return MapMessage.errorMessage();
        Boolean isOverLimit = asyncVendorCacheService.getFollowReadDayReadCacheManager().todayIsOVerLimit_collection(studentId);
        if (isOverLimit)
            return MapMessage.successMessage().add("is_over_limit", true);
        Map<String, FollowReadSentenceResult> followReadSentenceResultMap = followReadSentenceResultDao.loads(scoreIdList);
        if (MapUtils.isEmpty(followReadSentenceResultMap))
            return MapMessage.errorMessage();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return MapMessage.errorMessage();

        FollowReadCollection collection = new FollowReadCollection();
        collection.setStudentId(studentId);
        collection.setUnitId(unitId);
        Clazz clazz = studentDetail.getClazz();
        if (clazz != null) {
            collection.setSchoolId(clazz.getSchoolId());
            collection.setCityId(studentDetail.getCityCode());
        }
        collection.setResultIdList(followReadSentenceResultMap.values().stream().map(FollowReadSentenceResult::getId).collect(Collectors.toList()));
        collection.generateId();
        followReadCollectionDao.insert(collection);
        asyncVendorCacheService.getFollowReadDayReadCacheManager().todayCreateOne_collection(studentId);

        //榜单初始化，设置score为创建时间，用于对点赞数相同的做排序
        DayRange currentDay = DayRange.current();
        followReadShareLikeRankCacheManager.globalRangeSetLike(currentDay, collection.getId(), 0, new Date(), false);
        if (collection.getCityId() != null) {
            followReadShareLikeRankCacheManager.cityRangeSetLike(currentDay, collection.getId(), collection.getCityId(), 0, new Date(), false);
        }
        if (collection.getSchoolId() != null) {
            followReadShareLikeRankCacheManager.schoolRangeSetLike(currentDay, collection.getId(), collection.getSchoolId(), 0, new Date(), false);
        }

        //周榜的初始化
        List<DayRange> dayRangeList = generateDayRangeList(true);
        for (DayRange dayRange : dayRangeList) {
            followReadShareLikeRankCacheManager.globalRangeSetLike(dayRange, collection.getId(), 0, new Date(), true);
            if (collection.getCityId() != null) {
                followReadShareLikeRankCacheManager.cityRangeSetLike(dayRange, collection.getId(), collection.getCityId(), 0, new Date(), true);
            }
            if (collection.getSchoolId() != null) {
                followReadShareLikeRankCacheManager.schoolRangeSetLike(dayRange, collection.getId(), collection.getSchoolId(), 0, new Date(), true);
            }
        }

        return MapMessage.successMessage().add("collection_id", collection.getId()).add("is_over_limit", false);
    }

    @Override
    public FollowReadCollection loadFollowReadCollection(String id) {
        return followReadCollectionDao.load(id);
    }

    @Override
    public void someoneLikeCollection(String id) {
        FollowReadCollection readCollection = followReadCollectionDao.load(id);
        if (readCollection == null || Objects.equals(readCollection.getSchoolId(), 353246L))
            return;
        Date createTime = readCollection.getCreateTime();
        asyncVendorCacheService.getFollowReadDayReadCacheManager().someOneLikeColletion(id);
        // 暂时先不写排行榜,还没测完   已开
        Boolean writeRange = true;
        // 来一个赞就要在 6个排行榜上加1。。nnd,6个排行榜我也是醉了
        if (writeRange) {
            DayRange currentDay = DayRange.current();
            boolean sameDay = currentDay.contains(createTime);
            //日排行榜，只有当天创建当天点赞时才会记录点赞信息
            if (sameDay) {
                //全国
                followReadShareLikeRankCacheManager.globalRangeIncrLike(currentDay, id, 1L, false);
                if (readCollection.getCityId() != null) {
                    //同市
                    followReadShareLikeRankCacheManager.cityRangeIncrLike(currentDay, id, readCollection.getCityId(), 1L, false);
                }
                if (readCollection.getSchoolId() != null) {
                    //同校
                    followReadShareLikeRankCacheManager.schoolRangeIncrLike(currentDay, id, readCollection.getSchoolId(), 1L, false);
                }
            }

            List<DayRange> dayRangeList = generateDayRangeList(false);
            for (DayRange dayRange : dayRangeList) {
                if (needRank(readCollection, dayRange)) {
                    followReadShareLikeRankCacheManager.globalRangeIncrLike(dayRange, id, 1L, true);
                    if (readCollection.getCityId() != null) {
                        followReadShareLikeRankCacheManager.cityRangeIncrLike(dayRange, id, readCollection.getCityId(), 1L, true);
                    }
                    if (readCollection.getSchoolId() != null) {
                        followReadShareLikeRankCacheManager.schoolRangeIncrLike(dayRange, id, readCollection.getSchoolId(), 1L, true);
                    }
                }
            }
        }
    }


    @Override
    public AlpsFuture<Long> loadFollowReadCollectionLikeCount(String id) {
        Long collectionLikeCount = asyncVendorCacheService.getFollowReadDayReadCacheManager().collectionLikeCount(id);
        return new ValueWrapperFuture<>(collectionLikeCount);
    }

    @Override
    public List<FollowReadCollection> loadStudentFollowReadCollections(Long studentId) {
        return followReadCollectionDao.findByUserId(studentId);
    }

    @Override
    public void processPicListenCollectData(PicListenCollectData picListenCollectData) {
        if (picListenCollectData == null ||
                picListenCollectData.getStudentId() == null || picListenCollectData.getStudentId() == 0
                || CollectionUtils.isEmpty(picListenCollectData.getSentenceResultList()))
            return;
        picListenCollectDataQueueProducer.processCollect(picListenCollectData);
    }

    @Override
    public AlpsFuture<Set<String>> supportFollowReadBookIdSet() {
        Set<String> followReadBookIdSet = innerFollowReadBookIdSet();
        return CollectionUtils.isEmpty(followReadBookIdSet) ? ValueWrapperFuture.emptySet() : new ValueWrapperFuture<>(followReadBookIdSet);
    }

    @Override
    public AlpsFuture<PicListenReportDayResult> loadReportDayResult(Long studentId, DayRange dayRange) {
        PicListenReportDayResult reportDayResult = innerLoadReportDayResult(studentId, dayRange);
        return new ValueWrapperFuture<>(reportDayResult);
    }

    private PicListenReportDayResult innerLoadReportDayResult(Long studentId, DayRange dayRange) {
        if (studentId == null || studentId == 0 || dayRange == null)
            return null;
        PicListenReportDayResult reportDayResult = picListenReportCacheManager.getReportDayResult(studentId, dayRange.toString());
        if (reportDayResult == null)
            return PicListenReportDayResult.emptyResult();
        if (reportDayResult.getReportScore() < 0) {
            Map<String, Object> map = calculateStudentReportScore(studentId, reportDayResult);
            Long reportScore = SafeConverter.toLong(map.get("score"));
            Boolean hasFollowRead = SafeConverter.toBoolean(map.get("hasFollowRead"));
            if (!dayRange.equals(DayRange.current()))
                picListenReportCacheManager.asyncUpdateScoreResult(studentId, dayRange.toString(), null, null, null, reportScore, hasFollowRead);
            reportDayResult.setReportScore(reportScore);
            reportDayResult.setHasFollowRead(hasFollowRead);
        }
        return reportDayResult;
    }

    /**
     * 包括当前的天, 前7天的报告分数
     * 缓存只记前6天的,当天的直接放无数据进去。。。
     * 当天的分数不计算,因为一旦计算了无法更新了新的分数。
     *
     * @param studentId
     * @param currentDayRange 当前的天
     * @return
     */
    @Override
    public AlpsFuture<List<PicListenReportDayResult.DayScoreMapper>> loadSevenDayScore(Long studentId, DayRange currentDayRange) {
        if (studentId == null || studentId == 0)
            return ValueWrapperFuture.emptyList();
        List<PicListenReportDayResult.DayScoreMapper> sevenDayScoreList = picListenReportCacheManager.getSevenDayScoreList(studentId);
        List<DayRange> deleteDayRanges = new ArrayList<>();
        List<PicListenReportDayResult.DayScoreMapper> resultList = new ArrayList<>();
        Set<DayRange> lastSixDayRangeSet = lastSixDayRangeSet(currentDayRange);
        Set<DayRange> existentDayRangeSet = new HashSet<>();
        for (PicListenReportDayResult.DayScoreMapper scoreMapper : sevenDayScoreList) {
            if (!lastSixDayRangeSet.contains(scoreMapper.getDay())) {
                deleteDayRanges.add(scoreMapper.getDay());
                continue;
            }
            existentDayRangeSet.add(scoreMapper.getDay());
            resultList.add(scoreMapper);
        }
        List<DayRange> missingDayRangeList = lastSixDayRangeSet.stream().filter(t -> !existentDayRangeSet.contains(t)).collect(Collectors.toList());
        for (DayRange dayRange : missingDayRangeList) {
            PicListenReportDayResult reportDayResult = innerLoadReportDayResult(studentId, dayRange);
            if (reportDayResult != null) {
                picListenReportCacheManager.addOneDayNewScore(studentId, dayRange.toString(), reportDayResult.getReportScore());
                PicListenReportDayResult.DayScoreMapper dayScoreMapper = new PicListenReportDayResult.DayScoreMapper();
                dayScoreMapper.setScore(reportDayResult.getReportScore());
                dayScoreMapper.setDay(dayRange);
                resultList.add(dayScoreMapper);
            }
        }
        if (CollectionUtils.isNotEmpty(deleteDayRanges))
            picListenReportCacheManager.deleteSomeDayScore(studentId, deleteDayRanges);
        resultList.sort(new PicListenReportDayResult.DayScoreMapper.DayRangeCompartor());
        return new ValueWrapperFuture<>(resultList);
    }

    @Override
    public AlpsFuture<PicListenReportConfig> loadPicListenReportConfig() {
        return new ValueWrapperFuture<>(innerLoadPicListenReportConfig());
    }


    @Override
    public StudentFollowReadReport loadStudentFollowReadReportData(Long studentId) {
        return studentFollowReadReportDao.load(studentId);
    }

    @Override
    public MapMessage updateStudentFollowSentenceResult(Long studentId) {
        studentFollowReadReportGenerator.generateStudentFollowReadReport(studentId);
        return MapMessage.successMessage();
    }

    @Override
    public Set<Long> dayFollowReadActiveStudentIdSet(DayRange dayRange) {
        return picListenReportCacheManager.dayActiveFollowReadStudentIdSet(dayRange);
    }

    @Override
    public AlpsFuture<Boolean> reportHasFollowRead(Long studentId) {
        return new ValueWrapperFuture<>(innerReportHasFollowRead(studentId));
    }

    @Override
    public Page<FollowReadRangeWrapper> loadLikeRangeList(StudentDetail studentDetail, FollowReadLikeRangeType followReadLikeRangeType, Pageable page) {
        if (studentDetail == null || followReadLikeRangeType == null || page == null)
            return new PageImpl<>(Collections.emptyList());
        if (studentDetail.getClazz() == null && followReadLikeRangeType.isNeedClazz())
            return new PageImpl<>(Collections.emptyList());

        List<FollowReadLikeCountScoreMapper> scoreMapperList = loadRangeScoreMapper(studentDetail, followReadLikeRangeType);
        Page<FollowReadLikeCountScoreMapper> scoreMapperPage = PageableUtils.listToPage(scoreMapperList, page);
        List<FollowReadLikeCountScoreMapper> mapperList = scoreMapperPage.getContent();
        Set<String> collectionIds = mapperList.stream().map(FollowReadLikeCountScoreMapper::getCollectionId).collect(Collectors.toSet());
        Map<String, FollowReadCollection> collectionMap = followReadCollectionDao.loads(collectionIds);
        if (MapUtils.isEmpty(collectionMap))
            return new PageImpl<>(Collections.emptyList());
        Set<String> unitIdSet = collectionMap.values().stream().map(FollowReadCollection::getUnitId).collect(Collectors.toSet());
        Set<Long> studentIdSet = collectionMap.values().stream().map(FollowReadCollection::getStudentId).collect(Collectors.toSet());
        Map<String, NewBookCatalog> unitMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIdSet);
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIdSet);
        if (MapUtils.isEmpty(unitMap))
            return new PageImpl<>(Collections.emptyList());

        return scoreMapperPage.map(new WrapperConverter(studentDetailMap, unitMap, collectionMap));
    }

    private class WrapperConverter implements Converter<FollowReadLikeCountScoreMapper, FollowReadRangeWrapper> {

        private Map<Long, StudentDetail> studentDetailMap;

        private Map<String, NewBookCatalog> unitMap;

        private Map<String, FollowReadCollection> collectionMap;

        @Override
        public FollowReadRangeWrapper convert(FollowReadLikeCountScoreMapper source) {
            FollowReadCollection collection = collectionMap.get(source.getCollectionId());
            if (collection == null)
                return null;
            StudentDetail studentDetail = studentDetailMap.get(collection.getStudentId());
            if (studentDetail == null)
                return null;
            NewBookCatalog unit = unitMap.get(collection.getUnitId());
            if (unit == null)
                return null;
//            String clazzName = "";
//            Clazz clazz = studentDetail.getClazz();
//            if (clazz != null)
//                clazzName = clazz.formalizeClazzName();
            FollowReadRangeWrapper wrapper = new FollowReadRangeWrapper();
            wrapper.setCollectionId(source.getCollectionId());
            wrapper.setStudentId(studentDetail.getId());
            wrapper.setCreateTime(source.getCreateTime());
            wrapper.setLikeCount(source.getLikeCount());
            wrapper.setDateRange(source.getDateRange());
            wrapper.setAvatar(studentDetail.fetchImageUrl());
//            wrapper.setClazzName(clazzName);
//            wrapper.setSchoolName(studentDetail.getStudentSchoolName() == null ? "" : studentDetail.getStudentSchoolName());
            StringBuilder regionString = new StringBuilder();
            if (studentDetail.getRootRegionCode() != null) {
                regionString = regionString.append(raikouSystem.loadRegion(studentDetail.getRootRegionCode()).getName());
            }
            if (studentDetail.getStudentSchoolRegionCode() != null) {
                regionString = regionString.append(raikouSystem.loadRegion(studentDetail.getStudentSchoolRegionCode()).getName());
            }
            wrapper.setSchoolName(regionString.toString());
            wrapper.setUnitName(unit.getName());
            wrapper.setStudentName(studentDetail.fetchRealname());
            wrapper.setUnitId(unit.getId());
            wrapper.setBookId(unit.getAncestors().stream().filter(t -> BookCatalogType.BOOK.name().equals(t.getNodeType()))
                    .map(NewBookCatalogAncestor::getId).findFirst().orElse(""));
            return wrapper;
        }

        public WrapperConverter(Map<Long, StudentDetail> studentDetailMap, Map<String, NewBookCatalog> unitMap, Map<String, FollowReadCollection> collectionMap) {
            this.studentDetailMap = studentDetailMap;
            this.unitMap = unitMap;
            this.collectionMap = collectionMap;
        }
    }


    private List<FollowReadLikeCountScoreMapper> loadRangeScoreMapper(StudentDetail studentDetail, FollowReadLikeRangeType followReadLikeRangeType) {
        DayRange yesterday = DayRange.current().previous();
        DayRange currentDay = DayRange.current();
        Integer topNum = 500;
        List<FollowReadLikeCountScoreMapper> mapperList;
        switch (followReadLikeRangeType) {
            case YESTERDAY_GLOBAL:
                mapperList = followReadShareLikeRankCacheManager.getGlobalLikeRangeList(yesterday, topNum, false);
                followReadShareLikeRankCacheManager.deleteGlobalLikeRangeMore(yesterday, topNum, false);
                break;
            case YESTERDAY_CITY:
                mapperList = followReadShareLikeRankCacheManager.getCityLikeRangeList(yesterday, studentDetail.getCityCode(), topNum, false);
                followReadShareLikeRankCacheManager.deleteCityLikeRangeMore(yesterday, studentDetail.getCityCode(), topNum, false);
                break;
            case YESTERDAY_SCHOOL:
                mapperList = followReadShareLikeRankCacheManager.getSchoolLikeRangeList(yesterday, studentDetail.getClazz().getSchoolId(), topNum, false);
                followReadShareLikeRankCacheManager.deleteSchoolLikeRangeMore(yesterday, studentDetail.getClazz().getSchoolId(), topNum, false);
                break;
            case LAST_WEEK_GLOBAL:
                mapperList = followReadShareLikeRankCacheManager.getGlobalLikeRangeList(currentDay, topNum, true);
                followReadShareLikeRankCacheManager.deleteGlobalLikeRangeMore(currentDay, topNum, true);
                break;
            case LAST_WEEK_CITY:
                mapperList = followReadShareLikeRankCacheManager.getCityLikeRangeList(currentDay, studentDetail.getCityCode(), topNum, true);
                followReadShareLikeRankCacheManager.deleteCityLikeRangeMore(currentDay, studentDetail.getCityCode(), topNum, true);
                break;
            case LAST_WEEK_SCHOOL:
                mapperList = followReadShareLikeRankCacheManager.getSchoolLikeRangeList(currentDay, studentDetail.getClazz().getSchoolId(), topNum, true);
                followReadShareLikeRankCacheManager.deleteSchoolLikeRangeMore(currentDay, studentDetail.getClazz().getSchoolId(), topNum, true);
                break;
            default:
                mapperList = Collections.emptyList();
                break;
        }
        return mapperList;
    }

    //如果传入创建时间，则表示是在创建作品的时候计算需要初始化的周榜，否者表示是在点赞的时候计算需要增加点赞数的周榜
    private List<DayRange> generateDayRangeList(boolean init) {
        List<DayRange> dayRangeList = new ArrayList<>();
        int dayOfWeek;
        Calendar calendar = Calendar.getInstance();
        if (init) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        } else {
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        }

        //周排行榜需要的日排行榜的个数
        int totalSets;
        //星期天
        if (dayOfWeek == 1) {
            totalSets = 1;
        } else {
            totalSets = 9 - dayOfWeek;
        }
        for (int i = 1; i <= totalSets; i++) {
            DayRange dayRange = DayRange.newInstance(DateUtils.addDays(new Date(), i).getTime());
            dayRangeList.add(dayRange);
        }
        return dayRangeList;
    }

    //判断一个作品的点赞是否需要记录在周排行榜中
    private boolean needRank(FollowReadCollection followReadCollection, DayRange dayRange) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dayRange.getEndTime());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Date createTime = followReadCollection.getCreateTime();
        //本周一的周排行榜，参加排名的作品的创建时间必须在上一周之内
        if (dayOfWeek == 2) {
            return createTime.after(DateUtils.addDays(new Date(dayRange.getStartTime()), -7));
        } else if (dayOfWeek == 1) {//星期天参与排名的作品创建时间是本周前六天
            return createTime.after(DateUtils.addDays(new Date(dayRange.getStartTime()), -6));
        } else {
            return createTime.after(DateUtils.addDays(new Date(dayRange.getStartTime()), 2 - dayOfWeek));
        }
    }

    private Map<String, Object> calculateStudentReportScore(Long studentId, PicListenReportDayResult reportDayResult) {
        Map<String, Object> scoreMap = new HashMap<>();
        Boolean hasFollowRead = innerReportHasFollowRead(studentId);
        scoreMap.put("score", calculateReportScore(reportDayResult, hasFollowRead));
        scoreMap.put("hasFollowRead", hasFollowRead);
        return scoreMap;
    }


    private Boolean innerReportHasFollowRead(Long studentId) {
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        Boolean hasFollowRead = false;
        if (CollectionUtils.isNotEmpty(studentParents)) {
            Set<Long> parentIds = studentParents.stream().map(t -> t.getParentUser().getId()).collect(Collectors.toSet());
            Map<Long, List<PicListenBookShelf>> parentPicListenBookShelves = loadParentPicListenBookShelves(parentIds);
            List<String> allShelfBookId = parentPicListenBookShelves.values().stream().flatMap(Collection::stream).map(PicListenBookShelf::getBookId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(allShelfBookId)) {
                hasFollowRead = allShelfBookId.stream().anyMatch(t -> textBookManagementLoader.followReadBookSupport(t));
            }
        }
        return hasFollowRead;
    }

    /**
     * 前6天,不包括当前这天
     *
     * @param currentDayRage
     * @return
     */
    private static Set<DayRange> lastSixDayRangeSet(DayRange currentDayRage) {
        Set<DayRange> result = new HashSet<>();
        DayRange cursor = currentDayRage.previous();
        for (int i = 0; i <= 5; i++) {
            result.add(cursor);
            cursor = cursor.previous();
        }
        return result;
    }


    private Long calculateReportScore(PicListenReportDayResult dayResult, Boolean hasFollowRead) {
        PicListenReportConfig config = innerLoadPicListenReportConfig();
        if (config == null)
            return 0L;
        long learnTimeScore = calculateSingleScore(dayResult.getLearnTime(), config.getLearnTimeScoreParam());
        long playSentenceCountScore = calculateSingleScore(dayResult.getPlaySentenceCount(), config.getPlaySentenceCountScoreParam());
        if (hasFollowRead) {
            long followReadCountScore = calculateSingleScore(dayResult.getFollowReadSentenceCount(), config.getFollowReadSentenceCountScoreParam());
            BigDecimal learnTimeScoreB = new BigDecimal(learnTimeScore).divide(new BigDecimal(3), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal playSentenceCountScoreB = new BigDecimal(playSentenceCountScore).divide(new BigDecimal(3), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal followReadCountScoreB = new BigDecimal(followReadCountScore).divide(new BigDecimal(3), 2, BigDecimal.ROUND_HALF_UP);
            return learnTimeScoreB.add(playSentenceCountScoreB).add(followReadCountScoreB).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        } else {
            BigDecimal learnTimeScoreB = new BigDecimal(learnTimeScore).divide(new BigDecimal(2), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal playSentenceCountScoreB = new BigDecimal(playSentenceCountScore).divide(new BigDecimal(2), 2, BigDecimal.ROUND_HALF_UP);
            return learnTimeScoreB.add(playSentenceCountScoreB).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        }
    }

    private long calculateSingleScore(Long value, PicListenReportConfig.ScoreParam scoreParam) {
        if (value == null || value == 0L)
            return 0L;
        if (scoreParam == null)
            return 0L;
        if (value >= scoreParam.getMax())
            return scoreParam.getTotalScore();
        if (scoreParam.getStandard() == 0)
            return 0L;
        if (value <= scoreParam.getStandard()) {
            BigDecimal divide = new BigDecimal(value).multiply(new BigDecimal(scoreParam.getStandardScore())).divide(new BigDecimal(scoreParam.getStandard()), 0, RoundingMode.HALF_UP);
            return divide.longValue();
        } else {
            if (scoreParam.getMax().equals(scoreParam.getStandard()))
                return 0L;
            return new BigDecimal(value - scoreParam.getStandard()).multiply(new BigDecimal(scoreParam.getTotalScore() - scoreParam.getStandardScore()))
                    .divide(new BigDecimal(scoreParam.getMax() - scoreParam.getStandard()), 0, RoundingMode.HALF_UP).longValue()
                    + scoreParam.getStandardScore();
        }
    }


    @Deprecated
    private Set<String> innerFollowReadBookIdSet() {
        String configStr = innerGetPicListenPageBlockConfig("followReadBook");
        if (StringUtils.isBlank(configStr))
            return Collections.emptySet();
        List<String> bookIdList = JsonUtils.fromJsonToList(configStr, String.class);
        return CollectionUtils.isEmpty(bookIdList) ? Collections.emptySet() : new HashSet<>(bookIdList);
    }

    private PicListenReportConfig innerLoadPicListenReportConfig() {
        String reportParam = innerGetPicListenPageBlockConfig("reportParam");
        if (StringUtils.isBlank(reportParam))
            return null;
        return JsonUtils.fromJson(reportParam, PicListenReportConfig.class);
    }

    private String innerGetPicListenPageBlockConfig(String key) {
        if (StringUtils.isBlank(key))
            return null;
        List<PageBlockContent> selfStudyAdConfigPageContentList = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("picListenConfig");
        if (CollectionUtils.isEmpty(selfStudyAdConfigPageContentList))
            return null;
        PageBlockContent configPageBlockContent = selfStudyAdConfigPageContentList.stream().filter(p ->
                key.equals(p.getBlockName())
        ).findFirst().orElse(null);
        return configPageBlockContent == null ? null : configPageBlockContent.getContent();
    }

    private <T> T getFutureValue(Future<T> future, T defaultValue) {
        try {
            return future.get();
        } catch (Exception e) {
            return defaultValue;
        }
    }


    @Override
    public MapMessage getFltrpMobile(Long userId) {
        if (null == userId || 0 == userId) {
            return MapMessage.errorMessage("参数错误");
        }
        FltrpMobile fltrpMobile = fltrpMobileDao.getByUserId(userId);
        if (null != fltrpMobile) {
            return MapMessage.successMessage().add("mobile", fltrpMobile.getMobile());
        }

        if (useRealMobileForFltrp()) {
            String mobile = sensitiveUserDataServiceClient.showUserMobile(userId, "外研社注册", "9999");
            if (StringUtils.isBlank(mobile)) {
                return MapMessage.errorMessage("获取用户手机号失败");
            }

            saveFltrMobile(userId, mobile, true);
            return MapMessage.successMessage().add("mobile", mobile);
        }

        //get user mobile
        String mobile = sensitiveUserDataServiceClient.showUserMobile(userId, "生成外研社注册手机号", "9999");
        if (StringUtils.isBlank(mobile) || !MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("用户没有绑定手机号");
        }

        for (int i = 0; i < MAX_TRY_COUNT_GENERATE_FLTRP_MOBILE; i++) {
            String fMobile = generateFltrpMobile(mobile);

            //check no body use this mobile
            List<UserAuthentication> userAuthentications = userLoaderClient.loadMobileAuthentications(fMobile);
            if (CollectionUtils.isNotEmpty(userAuthentications)) {
                continue;
            }

            //check fltrp never used this mobile
            List<FltrpMobile> fltrpMobiles = fltrpMobileDao.getByMobile(fMobile);
            if (CollectionUtils.isNotEmpty(fltrpMobiles)) {
                PiclistenCache.getPiclistenCache().incr(FLTRP_MOBILE_HIT_COUNT, 1, 1, DateUtils.getCurrentToDayEndSecond());
                continue;
            }

            saveFltrMobile(userId, fMobile, false);

            return MapMessage.successMessage().add("mobile", fMobile);
        }
        return MapMessage.errorMessage("获取手机号失败");
    }

    @Override
    public MapMessage checkFltrpMobile(Long userId, String fltrpMobile) {
        if (null == userId || 0 == userId || StringUtils.isBlank(fltrpMobile)) {
            return MapMessage.errorMessage("参数错误");
        }

        FltrpMobile fmobile = fltrpMobileDao.getByUserId(userId);
        if (null == fmobile) {
            return MapMessage.errorMessage("未查询到记录");
        }
        if (!fmobile.getMobile().equalsIgnoreCase(fltrpMobile)) {
            fmobile.setMobile(fltrpMobile);
            fmobile.setReal(true);
        }
        fmobile.setChecked(true);
        boolean success = fltrpMobileDao.setMobileChecked(fmobile.getUserId(), fmobile.getMobile(), fmobile.getReal());
        if (success) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("更新失败");
        }
    }


    /**
     * 是否要使用真实的手机号传给外研社
     *
     * @return
     */
    private boolean useRealMobileForFltrp() {
        CommonConfig commonConfig = commonConfigServiceClient.getCommonConfigBuffer()
                .findByCategoryName(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType())
                .stream()
                .filter(e -> "fltrp_use_real_mobile".equals(e.getConfigKeyName()))
                .filter(e -> SafeConverter.toInt(e.getConfigRegionCode()) == 0)
                .findFirst()
                .orElse(null);
        return null != commonConfig && commonConfig.getConfigKeyValue().equalsIgnoreCase("true");
    }

    private String generateFltrpMobile(String mobile) {
        String mobilePrefix = mobile.substring(0, 3);
        String mobileSuffix = mobile.substring(7, 11);

        StringBuilder sb = new StringBuilder();
        sb.append(mobilePrefix);
        for (int i = 0; i < 4; i++) {
            sb.append(RandomUtils.nextInt(0, 9));
        }
        sb.append(mobileSuffix);

        return sb.toString();
    }

    private void saveFltrMobile(Long userId, String mobile, boolean real) {
        FltrpMobile fltrpMobile = new FltrpMobile();
        fltrpMobile.setUserId(userId);
        fltrpMobile.setMobile(mobile);
        fltrpMobile.setChecked(false);
        fltrpMobile.setReal(real);
        fltrpMobile.setCreateTime(new Date());
        fltrpMobile.setUpdateTime(new Date());

        fltrpMobileDao.insert(fltrpMobile);
    }
}
