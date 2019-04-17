package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.FollowReadLikeRangeType;
import com.voxlearning.utopia.service.vendor.api.entity.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangpeng
 * @since 2017-03-02 下午1:34
 **/
@ServiceVersion(version = "20171013")
@ServiceRetries(retries = 1)
public interface ParentSelfStudyService extends IPingable{




    @CacheMethod(
            writeCache = false
    )
    StudyToolShelf loadStudentStudyToolShelf(@CacheParameter Long studentId);

    void updateStudentStudyToolShelf(Long studentId, List<SelfStudyType> selfStudyTypeList);

    /**
     * 获取要传给外研社的手机号，如果没有就生成一个新的
     *
     * @param userId 假手机号对应的用户id
     * @return
     */
    @Idempotent
    MapMessage getFltrpMobile(Long userId);

    /**
     * 将外研社手机号与生成的手机号做同步
     * 优先以外研社手机号为准
     *
     * @param userId      要check手机号的userid
     * @param fltrpMobile 外研社记录的手机号
     * @return
     */
    @Idempotent
    MapMessage checkFltrpMobile(Long userId, String fltrpMobile);

    @CacheMethod(
            type = PicListenBookShelf.class,
            writeCache = false
    )
    Map<Long, List<PicListenBookShelf>> loadParentPicListenBookShelves(
            @CacheParameter(multiple = true, value = "PID") Collection<Long> parentIds);

    @Idempotent
    MapMessage deleteBookFromPicListenShelf(Long parentId, String bookId);

    MapMessage deleteBooksFromPicListenShelf(Long parentId, List<String> bookIds);

    @Idempotent
    MapMessage addBook2PicListenShelf(Long parentId, String bookId);

    @CacheMethod(
            type = Long.class,
            writeCache = false
    )
    Long parentPicListenBookShelfCountIncludeDisabled(@CacheParameter("PIDWD") Long parentId);


    @NoResponseWait
    void initParentPicListenBookShelfBooks(Long parentId, Set<String> bookIds);


    default List<PicListenBookShelf> loadParentPicListenBookShelf(Long parentId) {
        Map<Long, List<PicListenBookShelf>> map = loadParentPicListenBookShelves(Collections.singleton(parentId));
        List<PicListenBookShelf> list = map == null ? Collections.emptyList() : map.get(parentId);
        return list == null ? Collections.emptyList() : list;
    }

    List<PicListenBookShelf> mergePicListenBookShelf(Long parentId, List<String> bookIdList);

    /**
     * instead of TextBookManagement
     * @return
     */
    @Async
    @Deprecated
    AlpsFuture<Set<String>> supportFollowReadBookIdSet();


    //跟读相关
    @Async
    AlpsFuture<Boolean> loadIsFollowReadOverLimit(Long userId, String picListenId, Long sentenceId);

    MapMessage processFollowResult(FollowReadSentenceResult followReadSentenceResult);


    @CacheMethod(
            type = FollowReadSentenceResult.class,
            writeCache = false
    )
    Map<String, FollowReadSentenceResult> loadFollowReadSentenceResults(@CacheParameter(multiple = true) Collection<String> ids);


    MapMessage generateFollowReadCollection(Long studentId, String unitId, List<String> scoreIdList);

    @CacheMethod(
            type = FollowReadCollection.class,
            writeCache = false
    )
    FollowReadCollection loadFollowReadCollection(@CacheParameter String id);

    @NoResponseWait
    void someoneLikeCollection(String id);

    @Async
    AlpsFuture<Long> loadFollowReadCollectionLikeCount(String id);

    @CacheMethod(
            type = FollowReadCollection.class,
            writeCache = false
    )
    List<FollowReadCollection> loadStudentFollowReadCollections(@CacheParameter("SID") Long studentId);


    //点读报告

    @NoResponseWait
    void processPicListenCollectData(PicListenCollectData picListenCollectData);

    @Async
    AlpsFuture<PicListenReportDayResult> loadReportDayResult(Long studentId, DayRange dayRange);

    @Async
    AlpsFuture<List<PicListenReportDayResult.DayScoreMapper>> loadSevenDayScore(Long studentId, DayRange currentDayRange);

    @Async
    AlpsFuture<PicListenReportConfig> loadPicListenReportConfig();

    @CacheMethod(
            type = StudentFollowReadReport.class,
            writeCache = false
    )
    StudentFollowReadReport loadStudentFollowReadReportData(@CacheParameter Long studentId);

    MapMessage updateStudentFollowSentenceResult(Long studentId);

    @ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
    Set<Long> dayFollowReadActiveStudentIdSet(DayRange dayRange);

    @Async
    AlpsFuture<Boolean> reportHasFollowRead(Long studentId);

    Page<FollowReadRangeWrapper> loadLikeRangeList(StudentDetail studentDetail, FollowReadLikeRangeType followReadLikeRangeType, Pageable page);
}
