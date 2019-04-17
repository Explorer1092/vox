package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.AIUserBookResult;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 薯条英语内容V2版接口
 */
@ServiceVersion(version = "20190107")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsEnglishContentLoader extends IPingable {
    /**
     * 获取每日首页课程信息
     */
    MapMessage loadDailyClassInfo(User user, String bookId, String unitId);

    /**
     * 获取单元详情
     */
    MapMessage loadUnitDetail(Long userId, String bookId, String unitId);

    /**
     * 获取lesson详情
     */
    MapMessage loadLessonDetail(Long userId, String bookId, String unitId, String lessonId, String appVersion);

    /**
     * 获取单元结果
     */
    MapMessage loadUnitResult(User user, String bookId, String unitId);

    /**
     * 获取任务地图
     * @param appVersion app版本号
     */
    MapMessage loadUnitMap(Long userId, String appVersion);


    /**
     * 根据 用户id和bookId获取结果列表
     */
    MapMessage loadUnitMapByBook(Long userId, String bookId);

    /**
     * 获取任务地图
     */
    MapMessage loadUnitShareInfo(Long userId, String unitId, String bookId);

    String loadCurrentUnitId(Long clazzId);

    List<StoneUnitData> fetchUnitListExcludeTrialV2(String bookId);

    ChipsEnglishProductTimetable loadChipsEnglishProductTimetableById(String productId);

    Map<String, ChipsEnglishProductTimetable> loadChipsEnglishProductTimetableByIds(Collection<String> productIds);

    /**
     * 查询所有的有效的并且已经开课的unit(排除Trial_Unit)
     */
    List<StoneUnitData> loadValidBeginUnitByBookIdSortWithRank(OrderProduct orderProduct, String bookId);

    StoneUnitData loadTodayStudyUnit(String productId);

    MapMessage loadTalkRoleQuestionList(Long userId, String userCode, String roleName, String lessonId, String unitId, String bookId);

    Set<ChipsUnitType> getAllChipsUnitType(String bookId);

    List<StoneUnitData> getUnitByChipsUnitType(String bookId, ChipsUnitType chipsUnitType);

    List<StoneBookData> loadAllChipsEnglishBooks();

    List<StoneData> loadLessonByUnitId(String bookId, String unitId);

    StoneLessonData loadLessonById(String id);

    MapMessage loadCourseStudyPlanInfo(Long userId, String unitId, String bookId);

    MapMessage loadBookResultInfo(Long userId, String bookId);

    Map<Long, AIUserBookResult> loadPreviewUserBookResult(Collection<Long> userIdList, String book);

    MapMessage loadLessonPlay(Long userId, String bookId, String unitId);

    Map<String, StoneData> loadQuestionStoneData(List<String> qidList);
}
