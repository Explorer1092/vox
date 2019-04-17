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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.athena.api.search.entity.PictureBookRecomBean;
import com.voxlearning.athena.api.search.entity.PictureBookTagType;
import com.voxlearning.athena.api.search.loader.RecommendPictureBookLoader;
import com.voxlearning.galaxy.service.picturebook.api.cache.PictureBookCacheManager;
import com.voxlearning.galaxy.service.picturebook.api.consumer.PictureBookCardClient;
import com.voxlearning.galaxy.service.picturebook.api.entity.PictureBookCard;
import com.voxlearning.galaxy.service.picturebook.api.entity.UserPictureBookCardRef;
import com.voxlearning.galaxy.service.picturebook.api.mapper.PictrueBookCardMapper;
import com.voxlearning.galaxy.service.picturebook.api.service.PictureBookCardLoader;
import com.voxlearning.galaxy.service.picturebook.api.service.PictureBookCardService;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningService;
import com.voxlearning.galaxy.service.studyplanning.api.constant.StudyPlanningType;
import com.voxlearning.galaxy.service.studyplanning.api.data.StudyPlanningItemMapper;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookClazzLevel;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.parent.api.consumer.PictureBookConfigLoaderClient;
import com.voxlearning.utopia.service.parent.api.entity.picturebook.PictureBookConfig;
import com.voxlearning.utopia.service.parent.api.entity.picturebook.PictureBookConfigInfo;
import com.voxlearning.utopia.service.parent.api.entity.picturebook.PictureBookConfigList;
import com.voxlearning.utopia.service.piclisten.api.JztReadingLoader;
import com.voxlearning.utopia.service.piclisten.api.JztReadingService;
import com.voxlearning.utopia.service.question.api.constant.PictureBookApply;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.mapper.PictureBookQuery;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.question.consumer.TeachingObjectiveLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.entity.UserReadingRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.washington.mapper.picturebook.*;
import com.voxlearning.washington.service.picturebook.MobileJztReadingHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author malong
 * @since 2016/12/21
 */
@Controller
@Slf4j
@RequestMapping("parentMobile/jzt/reading")
public class MobileJztReadingController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;
    @Inject
    private UserAdvertisementServiceClient userAdvertisementServiceClient;
    @Inject
    private PictureBookLoaderClient pictureBookLoaderClient;
    @Inject
    private PictureBookPlusServiceClient pictureBookPlusServiceClient;
    @Inject
    private TeachingObjectiveLoaderClient teachingObjectiveLoaderClient;
    @Inject
    private PictureBookConfigLoaderClient pictureBookConfigLoaderClient;
    @Inject
    private PictureBookCardClient pictureBookCardClient;

    @ImportService(interfaceClass = JztReadingLoader.class)
    private JztReadingLoader jztReadingLoader;
    @ImportService(interfaceClass = JztReadingService.class)
    private JztReadingService jztReadingService;
    @ImportService(interfaceClass = StudyPlanningService.class)
    private StudyPlanningService studyPlanningService;
    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;
    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;
    @ImportService(interfaceClass = RecommendPictureBookLoader.class)
    private RecommendPictureBookLoader recommendPictureBookLoader;
    @ImportService(interfaceClass = PictureBookCardService.class)
    private PictureBookCardService pictureBookCardService;
    @ImportService(interfaceClass = PictureBookCardLoader.class)
    private PictureBookCardLoader pictureBookCardLoader;

    private static final String HOME_PAGE_PICBOOK_RECOMMEND = "picBookRecommend";
    private static final String PICBOOK_SUBJECTS = "picBookSubjects";
    private static final String PERSISTENCE = "persistence";
    private static final int PICBOOK_RECOMMEND_DEFAULT_NUM = 3;
    private static final String PICBOOK_RECOMMEND_CACHE_PREFIX = "picBook_recommend_";

    private static final int DEFAULT_CLAZZ_LEVEL = 3;
    private static final List<Integer> NEED_DEAL_CLAZZ_LEVEL_ARRAY = new ArrayList<>(Arrays.asList(51, 52, 53, 54));
    private static final String USER_READ_COUNT_CACHE_PRE = "JZT_USER_READ_PICTURE_BOOK_COPUNT_";
    private static final String USER_CURRENT_PICTURE_BOOK_CARD_PRE = "USER_CURRENT_PICTURE_BOOK_CARD_";
    private static final String USER_CURRENT_PICTURE_BOOK_CARD_POP_PRE = "USER_CURRENT_PICTURE_BOOK_CARD_IS_POP_";
    private static Integer USER_READ_COUNT_CACHE_EXPIRATION = DateUtils.getCurrentToDayEndSecond() + 30 * 24 * 60 * 60;

    static {
        if (RuntimeMode.isUsingTestData() || RuntimeMode.isStaging()) {
            USER_READ_COUNT_CACHE_EXPIRATION = 60;
        }
    }

    /**
     * 绘本-本周热榜
     */
    @RequestMapping(value = "/recommend.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getRecommendList() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentParent();
        MapMessage mapMessage = MapMessage.successMessage();
        try {
            if (parent == null) {
                return noLoginResult;
            }
            if (studentId <= 0L) {
                return MapMessage.successMessage();
            }
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                return MapMessage.errorMessage("student error");
            }
            if (!studentIsParentChildren(parent.getId(), studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联");
            }

            Clazz clazz = studentDetail.getClazz();
            if (clazz != null && clazz.isTerminalClazz()) {
                mapMessage.put("isGraduated", Boolean.TRUE);
                return mapMessage;
            }

            List<PictureBook> pictureBookList = new ArrayList<>();
            int clazzLevel = studentDetail.getClazzLevel() == null ? 1 : SafeConverter.toInt(studentDetail.getClazzLevel().getLevel());
            int termType = SchoolYear.newInstance().currentTerm().getKey();
            String description = "";

            //热门推荐
            List<Map<String, Object>> readingContent = teachingObjectiveLoaderClient.loadReadingContentByBook("", clazzLevel, termType);
            if (CollectionUtils.isNotEmpty(readingContent)) {
                Map<String, Object> readingMap = MiscUtils.firstElement(readingContent);
                @SuppressWarnings("unchecked ")
                List<String> pictureBookIds = (List<String>) readingMap.get("picture_book_ids");
                //副标题
                description = SafeConverter.toString(readingMap.get("description"));
                Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBookByDocIds(pictureBookIds);
                if (MapUtils.isNotEmpty(pictureBookMap)) {
                    pictureBookList.addAll(pictureBookMap.values());
                }
            }

            Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false)
                    .stream()
                    .filter(e -> e.getSubject() == Subject.ENGLISH)
                    .map(GroupMapper::getId)
                    .collect(Collectors.toSet());
            NewClazzBookRef clazzBookRef = newClazzBookLoaderClient.loadGroupBookRefs(groupIds).toList()
                    .stream()
                    .sorted((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime()))
                    .filter(e -> StringUtils.isNotBlank(e.getUnitId()))
                    .findFirst()
                    .orElse(null);
            if (clazzBookRef != null) {
                //课堂同步拓展
                List<PictureBook> synchronousList = pictureBookLoaderClient.loadPictureBookByUnitId(clazzBookRef.getUnitId());
                pictureBookList.addAll(synchronousList);
            }
            if (CollectionUtils.isEmpty(pictureBookList)) {
                //如果热门推荐和课堂拓展都没有，则取对应年级的前四个绘本，没有年级，默认为三年级
                clazzLevel = studentDetail.getClazzLevel() == null ? 3 : SafeConverter.toInt(studentDetail.getClazzLevel().getLevel());
                Pageable pageable = new PageRequest(0, 4);
                Page<PictureBook> pictureBookPage = pictureBookLoaderClient.loadPictureBookByPictureBookQuery(new PictureBookQuery(), clazzLevel, pageable);
                pictureBookList.addAll(pictureBookPage.getContent());
            }
            mapMessage = generateReadingMap(pictureBookList, false);
            mapMessage.put("description", description);
            return mapMessage;

        } catch (Exception e) {
            logger.error("get reading recommend list error.", e);
            return MapMessage.errorMessage("获取本周热榜失败");
        }
    }

    /**
     * 绘本-所有绘本（搜索）
     */
    @RequestMapping(value = "/search.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage readingSearch() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String readingName = getRequestString("readingName");
        String clazzLevels = getRequestString("clazzLevels");
        String topicIds = getRequestString("topicIds");
        String seriesIds = getRequestString("seriesIds");
        int pageNum = getRequestInt("currentPage", 1);
        int pageSize = getRequestInt("pageSize", 10);

        try {
            List<String> clazzLevelStringList = Arrays.asList(clazzLevels.trim().split(","));
            List<Integer> clazzLevelList = clazzLevelStringList.stream()
                    .filter(e -> PictureBookClazzLevel.of(e) != null)
                    .map(e -> PictureBookClazzLevel.valueOf(e).getClazzLevel())
                    .collect(Collectors.toList());

            List<String> topicIdList = Arrays.stream(topicIds.trim().split(","))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            List<String> seriesIdList = Arrays.stream(seriesIds.trim().split(","))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            PictureBookQuery pictureBookQuery = new PictureBookQuery();
            pictureBookQuery.setName(readingName);
            pictureBookQuery.setClazzLevels(clazzLevelList);
            pictureBookQuery.setTopicIds(topicIdList);
            pictureBookQuery.setSeriesIds(seriesIdList);

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            Integer clazzLevel = studentDetail != null && studentDetail.getClazzLevel() != null ? studentDetail.getClazzLevel().getLevel() : null;

            Pageable pageable = new PageRequest(pageNum - 1, pageSize);

            Page<PictureBook> pictureBooks = pictureBookLoaderClient.loadPictureBookByPictureBookQuery(pictureBookQuery, clazzLevel, pageable);
            List<PictureBook> pictureBookList = pictureBooks.getContent();
            MapMessage mapMessage = generateReadingMap(pictureBookList, true);
            mapMessage.put("totalPage", pictureBooks.getTotalPages());
            return mapMessage;
        } catch (Exception e) {
            logger.error("search reading error:{}", e);
            return MapMessage.errorMessage("查询绘本失败");
        }

    }

    /**
     * 绘本-活动
     */
    @RequestMapping(value = "/activity.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getActivityInfo() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentParent();
        try {
            if (parent == null) {
                return noLoginResult;
            }
            String slotId = "221101";
            List<NewAdMapper> data = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadNewAdvertisementData(parent.getId(), slotId, getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));
            List<Map<String, Object>> activityList = generateActivityList(data, studentId, parent.getId(), slotId);
            return MapMessage.successMessage()
                    .add("activityList", activityList);
        } catch (Exception e) {
            logger.error("load jzt reading activities error.", e);
            return MapMessage.errorMessage("获取绘本活动失败");
        }
    }

    /**
     * 绘本-我的绘本
     */
    @RequestMapping(value = "/mine.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMyReadings() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentParent();
        try {
            if (parent == null) {
                return noLoginResult;
            }
            Long userId = Objects.equals(studentId, 0L) ? parent.getId() : studentId;
            //所有我在读的绘本(包括家长和学生的)
            Map<Long, List<UserReadingRef>> refListMap = jztReadingLoader
                    .getUserReadingRefsByUserIds(Arrays.asList(studentId, parent.getId()));
            List<UserReadingRef> refList = refListMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(ref -> ref.getSelfStudyType() == SelfStudyType.READING_ENGLISH_PLUS)
                    .collect(Collectors.toList());
            Map<String, UserReadingRef> refMap = refList
                    .stream()
                    .collect(Collectors.toMap(
                            UserReadingRef::getPictureBookId,
                            Function.identity(),
                            (u, v) -> v
                    ));

            Set<String> pictureBookIds = refList.stream().map(UserReadingRef::getPictureBookId).collect(Collectors.toSet());
            List<PictureBookPlus> pictureBookList = pictureBookPlusServiceClient
                    .loadByIds(pictureBookIds).values().stream().collect(Collectors.toList());

            //绘本列表按开始时间倒序排序
            pictureBookList = pictureBookList
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(e -> refMap.get(e.getId()) != null)
                    .sorted((o1, o2) -> refMap.get(o2.getId()).getUpdateDatetime().compareTo(refMap.get(o1.getId()).getUpdateDatetime()))
                    .collect(Collectors.toList());

            MapMessage message = MapMessage.successMessage();
            message.put("reading", generatePicBookPlusMap(pictureBookList));
            message.put("current", new Date());

            long finishCount = PictureBookCacheManager.INSTANCE
                    .getReadFinishCount(Objects.equals(studentId, 0L) ? parent.getId() : studentId);
            if (finishCount == 0L) {
                finishCount = refList.stream().filter(u -> Objects.equals(u.getFinishStatus(), 1)).count();
                PictureBookCacheManager.INSTANCE.initReadFinishCount(userId, SafeConverter.toInt(finishCount));
            }
            message.put("finish_count", finishCount);
            int readDays = PictureBookCacheManager.INSTANCE.getReadDayCount(userId);
            if (readDays == 0) {
                List<String> readDateList = refList.stream()
                        .map(u -> DateUtils.dateToString(u.getUpdateDatetime(), DateUtils.FORMAT_SQL_DATE))
                        .distinct()
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(readDateList)) {
                    readDays = readDateList.size();
                    PictureBookCacheManager.INSTANCE.initReadDayCount(userId, readDays);
                    for (String d : readDateList) {
                        PictureBookCacheManager.INSTANCE.intiReadMonthData(userId, d);
                    }
                }
            }
            message.put("read_days", readDays);
            message.put("calendar_list", getPictureBookReadCalendarData(userId));
            return message;
        } catch (Exception e) {
            logger.error("get my pictureBooks error.", e);
            return MapMessage.errorMessage("获取我的绘本失败");
        }
    }

    private List<Map<String, Object>> getPictureBookReadCalendarData(Long userId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Date weekEndDay = WeekRange.current().getEndDate();
        for (int i = 1; i <= 28; i++) {
            Map<String, Object> readMap = new HashMap<>();
            Date tmpDate = DateUtils.calculateDateDay(weekEndDay, -i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tmpDate);
            readMap.put("year", calendar.get(Calendar.YEAR));
            readMap.put("month", calendar.get(Calendar.MONTH) + 1);
            readMap.put("day", calendar.get(Calendar.DAY_OF_MONTH));
            readMap.put("week_day", calendar.get(Calendar.DAY_OF_WEEK) - 1);
            boolean monthData = PictureBookCacheManager.INSTANCE.getReadMonthData(userId, tmpDate);
            if (monthData) {
                readMap.put("is_read", true);
            } else {
                readMap.put("is_read", false);
            }
            resultList.add(readMap);
        }
        Collections.reverse(resultList);
        return resultList;
    }

    /**
     * 绘本-开始绘本:保存在读绘本记录
     */
    @RequestMapping(value = "start.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage startReading() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String pictureBookId = getRequestString(REQ_PICTURE_BOOK_ID);
        User parent = currentParent();
        try {
            if (parent == null) {
                return noLoginResult;
            }
            if (StringUtils.isEmpty(pictureBookId)) {
                return MapMessage.errorMessage("pictureBookId error");
            }
            UserReadingRef readingRef = new UserReadingRef();
            int clazzLevel = 3;
            //这里有个逻辑：1.如果没有学生，关系表中记录的就是家长id
            //            2.家长后面添加了孩子，如果家长读过这个绘本，就更新时间就行了，否则，记录关系表中就记录孩子id
            if (studentId != 0L) {
                List<UserReadingRef> userReadingRefList = jztReadingLoader.getUserReadingRefsByUserId(parent.getId());
                UserReadingRef userReadingRef = userReadingRefList.stream()
                        .filter(e -> pictureBookId.equals(e.getPictureBookId()))
                        .findFirst()
                        .orElse(null);

                if (userReadingRef != null) {
                    readingRef.setUserId(parent.getId());
                } else {
                    readingRef.setUserId(studentId);
                }
                //查找学生年级
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                clazzLevel = (studentDetail == null || studentDetail.getClazzLevel() == null) ? DEFAULT_CLAZZ_LEVEL :
                        NEED_DEAL_CLAZZ_LEVEL_ARRAY.contains(SafeConverter.toInt(studentDetail.getClazzLevel().getLevel()))
                                ? 3 : SafeConverter.toInt(studentDetail.getClazzLevel().getLevel());
            } else {
                readingRef.setUserId(parent.getId());
            }

            readingRef.setPictureBookId(pictureBookId);
            readingRef.setSelfStudyType(SelfStudyType.READING_ENGLISH_PLUS);
            readingRef.setFinishStatus(0);
            readingRef.setReadSeconds(0L);
            //更新绘本阅读信息
            MapMessage mapMessage = jztReadingService.upsertUserReadingRef(readingRef);

            if (mapMessage.isSuccess()) {
                StudyPlanningItemMapper itemMapper = new StudyPlanningItemMapper();
                itemMapper.setType(StudyPlanningType.READING_ENGLISH.name());
                studyPlanningService.finishPlanning(studentId, currentParent().getId(), itemMapper);
                Map<String, PictureBookPlus> pictureBookMap = pictureBookPlusServiceClient.loadByIds(Collections.singleton(pictureBookId));
                if (MapUtils.isNotEmpty(pictureBookMap) && pictureBookMap.get(pictureBookId) != null) {
                    //记录自学进度
                    mySelfStudyService.updateSelfStudyProgress(studentId, SelfStudyType.READING_ENGLISH_PLUS, pictureBookMap.get(pictureBookId).getEname());
                }

                //为用户发放绘本馆卡片
                MapMessage message = pictureBookCardService.sendCard(parent.getId());
                if (message.isSuccess() && message.get("cardId") != null) {
                    //写入缓存
                    CacheSystem.CBS.getCache(PERSISTENCE).set(USER_CURRENT_PICTURE_BOOK_CARD_PRE + parent.getId(), DateUtils.getCurrentToWeekEndSecond(), message.get("cardId"));
                }

                //记录用户阅读数据
                boolean monthData = PictureBookCacheManager.INSTANCE.getReadMonthData(readingRef.getUserId(), new Date());
                if (!monthData) {
                    PictureBookCacheManager.INSTANCE.recordReadMonthData(readingRef.getUserId());
                    PictureBookCacheManager.INSTANCE.recordReadDayCount(readingRef.getUserId());
                }

                //用户绘本等级
                if (clazzLevel <= 3) {
                    mapMessage.add("pbVersion", "l");
                } else {
                    mapMessage.add("pbVersion", "h");
                }
            }
            return mapMessage;
        } catch (Exception e) {
            logger.error("save userReadingRef error.", e);
            return MapMessage.errorMessage("保存在读绘本失败");
        }
    }

    /**
     * 绘本-我的绘本：删除在读绘本记录
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteReading() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String pictureBookIdStr = getRequestString(REQ_PICTURE_BOOK_ID);
        User parent = currentParent();
        try {
            if (parent == null) {
                return noLoginResult;
            }
            if (StringUtils.isEmpty(pictureBookIdStr)) {
                return MapMessage.errorMessage("pictureBookId error");
            }
            Long userId = studentId == 0L ? parent.getId() : studentId;
            String[] pictureBookIdSplit = pictureBookIdStr.split(",");
            Set<String> pictureBookIds = new HashSet<>();
            Collections.addAll(pictureBookIds, pictureBookIdSplit);

            return jztReadingService.deleteUserReadingRefs(userId, pictureBookIds);

        } catch (Exception e) {
            logger.error("delete userReadingRef error.", e);
            return MapMessage.errorMessage("删除在读绘本失败");
        }
    }

    /**
     * 生成绘本信息列表
     */
    private MapMessage generateReadingMap(List<PictureBook> pictureBookList, boolean isSearch) {
        MapMessage message = MapMessage.successMessage();

        if (CollectionUtils.isEmpty(pictureBookList)) {
            return message;
        }
        //绘本所有系列
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));

        //绘本所有主题
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));

        List<Map<String, Object>> readingMapperList = pictureBookList
                .stream()
                .filter(e -> e.getDeletedAt() == null)
                .filter(Objects::nonNull)
                .map(pictureBook -> NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, null, null))
                .collect(Collectors.toList());

        message.put("reading", readingMapperList);

        if (isSearch) {
            //生成查询条件（年级、主题、系列）列表
            List<Map<String, Object>> pictureBookTopics = pictureBookTopicList
                    .stream()
                    .filter(e -> e.getSubjectId() == 103)
                    .sorted(Comparator.comparingInt(PictureBookTopic::getRank))
                    .map(topic -> MiscUtils.m("topicId", topic.getId(), "topicName", topic.getName()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> pictureBookSeries = pictureBookSeriesList
                    .stream()
                    .map(series -> MiscUtils.m("seriesId", series.getId(), "seriesName", series.fetchName()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> pictureBookClazzLevels = Arrays.stream(PictureBookClazzLevel.values())
                    .map(pictureBookClazzLevel -> MiscUtils.m("clazzLevel", pictureBookClazzLevel, "name", pictureBookClazzLevel.getShowName()))
                    .collect(Collectors.toList());

            message.put("topics", pictureBookTopics);
            message.put("series", pictureBookSeries);
            message.put("clazzLevels", pictureBookClazzLevels);
        }

        return message;
    }

    /**
     * 生成活动信息
     */
    private List<Map<String, Object>> generateActivityList(List<NewAdMapper> data, Long studentId, Long userId, String slotId) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(data)) {
            return list;
        }
        int index = 0;
        for (NewAdMapper newAdMapper : data) {
            Map<String, Object> map = new HashMap<>();
            map.put("imgUrl", combineCdbUrl(newAdMapper.getImg()));

            String ver = getRequestString("app_version");
            String sys = getRequestString(REQ_SYS);
            String url = "";
            if (newAdMapper.getHasUrl()) {
                url = AdvertiseRedirectUtils.redirectUrl(newAdMapper.getId(), index, ver, sys, "", studentId);
            }
            map.put("url", url);
            map.put("name", SafeConverter.toString(newAdMapper.getName(), ""));
            map.put("aid", newAdMapper.getId());
            list.add(map);

            if (Boolean.TRUE.equals(newAdMapper.getLogCollected())) {
                LogCollector.info("sys_new_ad_show_logs",
                        MiscUtils.map("user_id", userId,
                                "env", RuntimeMode.getCurrentStage(),
                                "version", ver,
                                "aid", newAdMapper.getId(),
                                "acode", newAdMapper.getCode(),
                                "index", index,
                                "slotId", slotId,
                                "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                "time", DateUtils.dateToString(new Date()),
                                "agent", getRequest().getHeader("User-Agent"),
                                "uuid", UUID.randomUUID().toString(),
                                "system", sys
                        ));
            }

            index++;
        }
        return list;
    }

    @RequestMapping(value = "/recommend_new.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homePagePicBookRecommend() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentParent();
        Long parentId = parent == null ? null : parent.getId();
        MapMessage message = MapMessage.successMessage();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        try {
            int clazzLevel = (studentDetail == null || studentDetail.getClazzLevel() == null) ? DEFAULT_CLAZZ_LEVEL :
                    NEED_DEAL_CLAZZ_LEVEL_ARRAY.contains(SafeConverter.toInt(studentDetail.getClazzLevel().getLevel()))
                            ? 0 : SafeConverter.toInt(studentDetail.getClazzLevel().getLevel());
            List<PicBookRecommendMapper> picBookRecommendList = new ArrayList<>();
            List<PictureBookPlus> pictureBookList = new ArrayList<>();
            int singlePicBookNum;
            //获取CRM配置的绘本合集
            List<PictureBookConfigList> configList = new ArrayList<>(3);
            try {
                configList = pictureBookConfigLoaderClient.getConfig().getConfigLists()
                        .stream()
                        .filter(pc -> pc.getShowType() == 3)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                logger.error("获取绘本配置集合出现异常:" + e.getMessage());
                e.printStackTrace();
            }
            if (CollectionUtils.isEmpty(configList)) {
                singlePicBookNum = PICBOOK_RECOMMEND_DEFAULT_NUM;
            } else {
                singlePicBookNum = PICBOOK_RECOMMEND_DEFAULT_NUM - configList.size();
                picBookRecommendList.addAll(configList
                        .stream()
                        .sorted(Comparator.comparing(PictureBookConfigList::getUpdateDate).reversed())
                        .map(sub -> {
                            PicBookRecommendMapper mapper = new PicBookRecommendMapper();
                            mapper.setId(sub.getId());
                            mapper.setName(sub.getTitle());
                            mapper.setCoverUrl(sub.getAdImgUrl());
                            mapper.setThumbnail(sub.getAdImgUrl());
                            mapper.setType(0);
                            return mapper;
                        }).collect(Collectors.toList()));
            }
            if (singlePicBookNum < 0) {
                singlePicBookNum = 0;
            }
            if (studentId > 0L) {
                String studentPicBookRecommendCacheKey = PICBOOK_RECOMMEND_CACHE_PREFIX + studentId;
                List<String> studentPicBookRecommendCacheList = JsonUtils.fromJsonToList(CacheSystem.CBS.getCache(PERSISTENCE).load(studentPicBookRecommendCacheKey), String.class);
                if (CollectionUtils.isEmpty(studentPicBookRecommendCacheList)) {
                    //目前暂无绘本推荐缓存或缓存已过期
                    /**
                     * 从recommendPicBook获取，并写入缓存
                     */
                    pictureBookList = recommendPicBook(clazzLevel, singlePicBookNum, studentId, parentId);
                    List<String> picBookIds = pictureBookList.stream().map(PictureBookPlus::getId).distinct().collect(Collectors.toList());
                    CacheSystem.CBS.getCache(PERSISTENCE).set(studentPicBookRecommendCacheKey, DateUtils.getCurrentToDayEndSecond() + 24 * 60 * 60, JsonUtils.toJson(picBookIds));
                } else {
                    //获取对应singlePicBookNum数量的绘本
                    /**
                     * 如果当前缓存数量大于或等于singlePicBookNum，取对应前singlePicBookNum个
                     * 否则执行recommendPicBook,取singlePicBookNum-studentPicBookRecommendCacheNum个PictureBook
                     * 并且更新缓存
                     */
                    int studentPicBookRecommendCacheNum = studentPicBookRecommendCacheList.size();
                    //最终写回缓存的数据
                    List<String> pictureBookIds = new ArrayList<>(studentPicBookRecommendCacheNum);
                    if (studentPicBookRecommendCacheNum >= singlePicBookNum && singlePicBookNum >= 0) {
                        pictureBookIds = studentPicBookRecommendCacheList.stream().limit(singlePicBookNum).collect(Collectors.toList());
                    } else {
                        /**1.把缓存的数据写入最终集合
                         * 2.获取新的绘本写入最终集合和写回缓存集合
                         * */
                        List<PictureBookPlus> recommendPicBook = recommendPicBook(clazzLevel, singlePicBookNum - studentPicBookRecommendCacheNum, studentId, parentId);
                        studentPicBookRecommendCacheList.addAll(recommendPicBook.stream().map(PictureBookPlus::getId).collect(Collectors.toList()));
                        pictureBookIds.addAll(studentPicBookRecommendCacheList);
                    }
                    pictureBookIds = pictureBookIds.stream().distinct().limit(3).collect(Collectors.toList());
                    //更新缓存
                    CacheSystem.CBS.getCache(PERSISTENCE).replace(studentPicBookRecommendCacheKey,
                            DateUtils.getCurrentToDayEndSecond() + 24 * 60 * 60, JsonUtils.toJson(pictureBookIds));
                    //根据得到的绘本ID集合获取绘本信息，并放入最终绘本集合
                    Map<String, PictureBookPlus> pictureBookMap = pictureBookPlusServiceClient.loadByIds(pictureBookIds);
                    if (MapUtils.isNotEmpty(pictureBookMap)) {
                        pictureBookList.addAll(pictureBookMap.values());
                    }
                }
            } else {
                pictureBookList.addAll(recommendPicBook(clazzLevel, singlePicBookNum, studentId, parentId));
            }
            //处理绘本数据
            picBookRecommendList.addAll(generatePicBookPlusMap(pictureBookList));
            message.put("reading", picBookRecommendList);
            return message;
        } catch (Exception e) {
            logger.error("get recommend picture book list error.", e);
            return MapMessage.errorMessage("获取推荐绘本失败");
        }
    }

    private List<PictureBookPlus> recommendPicBook(int clazzLevel, int singlePicBookNum, Long studentId, Long parentId) {
        /**
         * 1.根据用户所在年级，随机推荐用户未读过的绘本。
         * 2.如果用户无年级信息，默认推荐三年级。
         * 3.如果当前年年级的绘本已经推荐完毕，推荐高一年级的未读过的绘本。
         * 4.如果当前已经没有符合条件的绘本推荐，推荐当前年级的，不考虑是否读过
         */
        List<PictureBookPlus> pictureBookResult = new ArrayList<>();
        //根据获取绘本的结果与用户在读绘本取差集
        // 查询所有绘本
        List<PictureBookPlus> allPictureBookList = pictureBookPlusServiceClient.loadAllOnline()
                .stream()
                // 过滤非作业端的绘本
                .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                .filter(pictureBookPlus -> Objects.equals(pictureBookPlus.getSubjectId(), Subject.ENGLISH.getId()))
                .filter(pictureBookPlus -> !pictureBookPlus.isDeleted())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(allPictureBookList)) {
            return pictureBookResult;
        }

        List<PictureBookPlus> unreadPicBookList = new ArrayList<>(allPictureBookList.size());
        unreadPicBookList.addAll(allPictureBookList);
        List<Long> userList = new ArrayList<>();
        if (studentId != null) {
            userList.add(studentId);
        }
        if (parentId != null) {
            userList.add(parentId);
        }
        if (CollectionUtils.isNotEmpty(userList)) {
            List<String> pictureBookLoaderByUserReadingList = pictureBookLoaderByUserReading(userList)
                    .stream()
                    .map(PictureBookPlus::getId)
                    .distinct()
                    .collect(Collectors.toList());
            //过滤已经读过的绘本
            unreadPicBookList = allPictureBookList
                    .stream()
                    .filter(a -> !pictureBookLoaderByUserReadingList.contains(a.getId()))
                    .collect(Collectors.toList());
        }
        //获取当前年级未读过的绘本Id集合
        List<String> resultPicBookIds = new ArrayList<>();
        if (clazzLevel > 6) {
            int newClazzLevel = clazzLevel - 6;
            resultPicBookIds = unreadPicBookList
                    .stream()
                    .filter(p -> CollectionUtils.isNotEmpty(p.getNewClazzLevels()))
                    .filter(p -> Objects.equals(p.getNewClazzLevels().get(0).getLevelType(), 3))
                    .filter(p -> Objects.equals(p.getNewClazzLevels().get(0).getLevelNum(), newClazzLevel))
                    .map(PictureBookPlus::getId)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            resultPicBookIds = unreadPicBookList
                    .stream()
                    .filter(p -> CollectionUtils.isNotEmpty(p.getNewClazzLevels()))
                    .filter(p -> Objects.equals(p.getNewClazzLevels().get(0).getLevelNum(), clazzLevel))
                    .map(PictureBookPlus::getId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(resultPicBookIds)) {
            //当前年级的所有绘本都已经被阅读，获取高一年级的绘本
            int newClazzLevel = clazzLevel + 1;
            if (clazzLevel > 6) {
                resultPicBookIds = unreadPicBookList
                        .stream()
                        .filter(p -> CollectionUtils.isNotEmpty(p.getNewClazzLevels()))
                        .filter(p -> Objects.equals(p.getNewClazzLevels().get(0).getLevelType(), 3))
                        .filter(p -> Objects.equals(p.getNewClazzLevels().get(0).getLevelNum(), (clazzLevel - 6)))
                        .map(PictureBookPlus::getId)
                        .distinct()
                        .collect(Collectors.toList());
            } else {
                resultPicBookIds = unreadPicBookList
                        .stream()
                        .filter(p -> CollectionUtils.isNotEmpty(p.getNewClazzLevels()))
                        .filter(p -> Objects.equals(p.getNewClazzLevels().get(0).getLevelNum(), newClazzLevel))
                        .map(PictureBookPlus::getId)
                        .distinct()
                        .collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isEmpty(resultPicBookIds)) {
            //当前已经没有符合条件的绘本推荐，推荐当前年级的，不考虑是否读过
            if (clazzLevel > 6) {
                int newClazzLevel = clazzLevel - 6;
                resultPicBookIds = allPictureBookList
                        .stream()
                        .filter(p -> CollectionUtils.isNotEmpty(p.getNewClazzLevels()))
                        .filter(p -> Objects.equals(p.getNewClazzLevels().get(0).getLevelType(), 3))
                        .filter(a -> Objects.equals(a.getNewClazzLevels().get(0).getLevelNum(), newClazzLevel))
                        .map(PictureBookPlus::getId)
                        .distinct()
                        .collect(Collectors.toList());
            } else {
                resultPicBookIds = allPictureBookList
                        .stream()
                        .filter(p -> CollectionUtils.isNotEmpty(p.getNewClazzLevels()))
                        .filter(a -> Objects.equals(a.getNewClazzLevels().get(0).getLevelNum(), clazzLevel))
                        .map(PictureBookPlus::getId)
                        .distinct()
                        .collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isEmpty(resultPicBookIds)) {
            //当前学生为毕业学生或其他异常情况，推荐默认年级，不考虑是否读过
            resultPicBookIds = allPictureBookList
                    .stream()
                    .filter(p -> CollectionUtils.isNotEmpty(p.getNewClazzLevels()))
                    .filter(a -> Objects.equals(a.getNewClazzLevels().get(0).getLevelNum(), DEFAULT_CLAZZ_LEVEL))
                    .map(PictureBookPlus::getId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        //随机取值返回
        return pictureBookRandomHandler(resultPicBookIds, singlePicBookNum);
    }

    private List<PictureBookPlus> pictureBookRandomHandler(List<String> picBookIds, int singlePicBookNum) {
        //随机获取singlePicBookNum个结果集合中的绘本Id,并返回绘本集合
        if (CollectionUtils.isEmpty(picBookIds)) {
            return new ArrayList<>();
        }
        Collections.shuffle(picBookIds);
        if (picBookIds.size() > singlePicBookNum && singlePicBookNum >= 0) {
            picBookIds = picBookIds.subList(0, singlePicBookNum);
        }
        Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(picBookIds);
        if (MapUtils.isEmpty(pictureBookPlusMap)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(pictureBookPlusMap.values());
    }

    private List<PictureBookPlus> pictureBookLoaderByUserReading(List<Long> userList) {
        //所有我在读的绘本（包括家长的和孩子的）
        List<UserReadingRef> refList = selectUserReadingRef(userList);
        Map<String, UserReadingRef> refMap = refList
                .stream()
                .collect(Collectors.toMap(
                        UserReadingRef::getPictureBookId,
                        Function.identity(),
                        (u, v) -> v
                ));
        Set<String> pictureBookReadingIds = refList.stream().map(UserReadingRef::getPictureBookId).collect(Collectors.toSet());
        //绘本列表按开始时间倒序排序
        return pictureBookPlusServiceClient.loadByIds(pictureBookReadingIds).values()
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> refMap.get(e.getId()) != null)
                .sorted((o1, o2) -> refMap.get(o2.getId()).getUpdateDatetime().compareTo(refMap.get(o1.getId()).getUpdateDatetime()))
                .collect(Collectors.toList());
    }

    private List<UserReadingRef> selectUserReadingRef(List<Long> userList) {
        //所有我在读的绘本（包括家长的和孩子的）
        return jztReadingLoader.getUserReadingRefsByUserIds(userList).values()
                .stream()
                .flatMap(Collection::stream)
                .filter(ref -> ref.getSelfStudyType() == SelfStudyType.READING_ENGLISH_PLUS)
                .collect(Collectors.toList());
    }

    private List<PicBookRecommendMapper> generatePicBookPlusMap(List<PictureBookPlus> pictureBookPlusList) {
        if (CollectionUtils.isEmpty(pictureBookPlusList)) {
            return new ArrayList<>();
        }
        return pictureBookPlusList
                .stream()
                .filter(e -> e.getDeletedAt() == null)
                .map(pictureBookPlus -> {
                    PicBookRecommendMapper recommendMapper = new PicBookRecommendMapper();
                    recommendMapper.setId(pictureBookPlus.getId());
                    recommendMapper.setName(pictureBookPlus.getEname());
                    recommendMapper.setCoverUrl(pictureBookPlus.getCoverUrl());
                    recommendMapper.setThumbnail(pictureBookPlus.getCoverThumbnailUrl());
                    recommendMapper.setType(1);
                    recommendMapper.setScreenMode(pictureBookPlus.getScreenMode());
                    recommendMapper.setWordsLength(pictureBookPlus.getWordsLength());
                    recommendMapper.setNameZH(pictureBookPlus.getCname());
                    return recommendMapper;
                }).collect(Collectors.toList());
    }

    @RequestMapping(value = "/crmseries.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage picBookSeriesDetail() {
        MapMessage message = MapMessage.successMessage();
        String subjectsId = getRequestString("subId");
        try {
            if (StringUtils.isBlank(subjectsId)) {
                return MapMessage.errorMessage("推荐系列参数异常");
            }
            List<PicBookSubjects> subjects = pageBlockContentServiceClient.loadConfigList(HOME_PAGE_PICBOOK_RECOMMEND, PICBOOK_SUBJECTS, PicBookSubjects.class);
            if (CollectionUtils.isEmpty(subjects)) {
                logger.error("CRM select PicBookSubjects is null");
                return MapMessage.errorMessage("推荐系列查询异常");
            }
            PicBookSubjects currentSubjects = subjects
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(s -> subjectsId.contains(s.getId()))
                    .findFirst()
                    .orElse(null);
            if (currentSubjects == null) {
                logger.error("CRM select PicBookSubjects size is " + subjects.size() + ",but current param subjectsId contains current PicBookSubjects is null");
                return MapMessage.errorMessage("推荐系列查询异常");
            }
            message.put("head_url", currentSubjects.getHeadImg());
            message.put("sub_name", currentSubjects.getSubName());
            List<String> picBookIds = currentSubjects.getIds();
            if (CollectionUtils.isEmpty(picBookIds)) {
                message.put("pic_book", new ArrayList<>());
                return message;
            }
            Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(picBookIds);
            if (MapUtils.isEmpty(pictureBookPlusMap)) {
                message.put("pic_book", new ArrayList<>());
                return message;
            }
            List<PicBookRecommendMapper> picBookList = pictureBookPlusMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(p -> {
                        PicBookRecommendMapper mapper = new PicBookRecommendMapper();
                        mapper.setId(p.getId());
                        mapper.setName(p.getEname());
                        mapper.setType(1);
                        mapper.setCoverUrl(p.getCoverUrl());
                        mapper.setThumbnail(p.getCoverThumbnailUrl());
                        mapper.setScreenMode(p.getScreenMode());
                        return mapper;
                    }).collect(Collectors.toList());

            message.put("pic_book", picBookList);
            return message;
        } catch (Exception e) {
            logger.error("get crm series picture book list error.", e);
            return MapMessage.errorMessage("获取推荐绘本系列详情失败");
        }
    }

    @RequestMapping(value = "/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getPicBookDetail() {
        MapMessage message = MapMessage.successMessage();
        String picBookId = getRequestString("pbid");
        try {
            if (StringUtils.isBlank(picBookId)) {
                return MapMessage.errorMessage("查询绘本详情参数异常");
            }
            PictureBookPlus picBook = pictureBookPlusServiceClient.loadById(picBookId);
            if (picBook == null) {
                return MapMessage.errorMessage("查询绘本详情出现异常");
            }

            //绘本所有系列
            List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                    .filter(s -> s.getId().equals(picBook.getSeriesId()))
                    .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));

            //绘本所有主题
            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                    .filter(t -> picBook.getTopicIds().contains(t.getId()))
                    .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
            List<PictureBookTag> tags = pageBlockContentServiceClient.loadConfigList("PICBOOK_TAB_PAGE", "PICBOOK_TAG", PictureBookTag.class);
            Map<String, Object> bookPlusMap = NewHomeworkContentDecorator.decoratePictureBookPlus(picBook,
                    pictureBookSeriesMap, pictureBookTopicMap, null, null, null, null);
            List<String> bookTopicIds = (List<String>) bookPlusMap.get("pictureBookTopicIds");
            PictureBookTag tag = tags.stream().filter(Objects::nonNull)
                    .filter(t -> t.getTopics().containsAll(bookTopicIds))
                    .findFirst()
                    .orElse(null);
            if (tag != null) {
                List<String> bookTopicNames = Collections.singletonList(tag.getTagName());
                bookPlusMap.put("pictureBookTopicNameList", bookTopicNames);
            }
            message.put("detail", bookPlusMap);
            return message;
        } catch (Exception e) {
            logger.error("get picture book detail error.", e);
            return MapMessage.errorMessage("获取绘本详情失败");
        }
    }

    @RequestMapping(value = "/gallery.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage pictureBookGallery() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Long parentId = currentUserId();
        MapMessage message = MapMessage.successMessage();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(Objects.equals(studentId, 0L) ? null : studentId);
        try {
            int clazzLevel = (studentDetail == null || studentDetail.getClazzLevel() == null ||
                    NEED_DEAL_CLAZZ_LEVEL_ARRAY.contains(SafeConverter.toInt(studentDetail.getClazzLevel().getLevel()))) ?
                    DEFAULT_CLAZZ_LEVEL : SafeConverter.toInt(studentDetail.getClazzLevel().getLevel());
            //精选系列
            List<PictureBookSeriesMapper> recommendPicBookSeries = getRecommendPicBookSeries();
            message.put("recommend_series", recommendPicBookSeries);
            //绘本推荐(大数据)
            List<PicBookRecommendMapper> mappers =
                    bigDataRecommendPicBook(clazzLevel, Objects.equals(studentId, 0L) ? null : studentId, Objects.equals(parentId, 0L) ? null : parentId);
            message.put("recommend_pic_count", mappers.size());
            message.put("recommend_pic", mappers);
            //获取tag集合
            List<PictureBookTag> tags = pageBlockContentServiceClient.loadConfigList("PICBOOK_TAB_PAGE", "PICBOOK_TAG", PictureBookTag.class);
            message.put("tags", getPictureBookTagMapperList(tags).stream()
                    .filter(t -> Objects.equals(t.getIsHomeShow(), 1)).collect(Collectors.toList()));
            //展示卡片
            Map<String, Object> cardMap = pictureBookCardShowHandler(studentId, parentId);
            message.put("card", cardMap);
            return message;
        } catch (Exception e) {
            logger.error("get picture book gallery error.", e);
            return MapMessage.errorMessage("获取绘本馆数据失败");
        }
    }

    private List<PicBookRecommendMapper> bigDataRecommendPicBook(int clazzLevel, Long studentId, Long parentId) {
        List<PicBookRecommendMapper> resultList = new ArrayList<>();
        //每天更新一次数据
        String recommendPBObj = PictureBookCacheManager.INSTANCE.getUserBigDataRecommendPB(studentId);
        if (StringUtils.isBlank(recommendPBObj)) {
            List<PictureBookRecomBean> booksRecommend = new ArrayList<>(20);
            List<PictureBookPlus> bookList = new ArrayList<>(20);
            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
            List<PictureBookTag> tags = pageBlockContentServiceClient.loadConfigList("PICBOOK_TAB_PAGE",
                    "PICBOOK_TAG", PictureBookTag.class);
            try {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                ExRegion exRegion = raikouSystem.loadRegion(studentDetail == null ? null : studentDetail.getCityCode());
                booksRecommend = recommendPictureBookLoader.primaryEnglishPictureBookRecommend(
                        parentId, studentId, clazzLevel,
                        exRegion == null ? null : SafeConverter.toLong(exRegion.getProvinceCode()),
                        exRegion == null ? null : SafeConverter.toLong(exRegion.getCityCode()),
                        exRegion == null ? null : SafeConverter.toLong(exRegion.getCountyCode()));
            } catch (Exception e) {
                logger.error("recommendPictureBookLoader primaryEnglishPictureBooksRecommend get picture book has exception.", e);
            }

            if (CollectionUtils.isEmpty(booksRecommend)) {//防止大数据服务不可用状态，走recommendPicBook逻辑
                logger.warn("big data recommendPictureBookLoader primaryEnglishPictureBooksRecommend return NULL");
                bookList = recommendPicBook(clazzLevel, 20, studentId, parentId);
            } else {
                List<String> recommendPbIds = booksRecommend.stream().map(PictureBookRecomBean::getPictureBookId)
                        .collect(Collectors.toList());
                bookList = pictureBookPlusServiceClient.loadByIds(recommendPbIds).values()
                        .stream().filter(Objects::nonNull).collect(Collectors.toList());
            }

            for (PictureBookPlus p : bookList) {
                PicBookRecommendMapper book = new PicBookRecommendMapper();
                book.setCoverUrl(p.getCoverUrl());
                book.setThumbnail(p.getCoverThumbnailUrl());
                book.setId(p.getId());
                book.setName(p.getEname());
                book.setNameZH(p.getCname());
                book.setType(1);
                PictureBookTopic topic = pictureBookTopicList.stream()
                        .filter(i -> p.getTopicIds().contains(i.getId()))
                        .findFirst()
                        .orElse(null);
                PictureBookTag tag = tags.stream().filter(Objects::nonNull)
                        .filter(t -> t.getTopics().containsAll(p.getTopicIds()))
                        .findFirst()
                        .orElse(null);
                book.setTopicText(tag == null ? topic == null ? "" : topic.getName() : tag.getTagName());
                PictureBookRecomBean bean = booksRecommend
                        .stream()
                        .filter(r -> p.getId().equals(r.getPictureBookId()))
                        .findFirst()
                        .orElse(null);
                if (bean != null) {
                    List<Integer> tagIdList = bean.getTagIdList();
                    if (CollectionUtils.isNotEmpty(tagIdList)) {
                        PictureBookTagType[] values = PictureBookTagType.values();
                        PictureBookTagType tagType = Arrays.stream(values).filter(v -> Objects.equals(tagIdList.get(0), v.getId()))
                                .findFirst().orElse(null);
                        if (tagType != null) {
                            book.setTags(Collections.singletonList(tagType.getValue()));
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(book.getTags())) {
                    resultList.add(0, book);
                } else {
                    resultList.add(book);
                }
            }
            if (studentId != null) {
                //更新缓存
                PictureBookCacheManager.INSTANCE.recordUserBigDataRecommendPB(studentId, JsonUtils.toJson(resultList));
            }
        } else {
            resultList = JsonUtils.fromJsonToList(recommendPBObj, PicBookRecommendMapper.class);
        }
        return resultList;
    }

    private Map<String, Object> pictureBookCardShowHandler(Long studentId, Long parentId) {
        if (parentId == null) {
            //未登录
            return noUserCardRecordHandler();
        } else {
            List<UserReadingRef> readRefList = jztReadingLoader
                    .getUserReadingRefsByUserIds(Arrays.asList(studentId, parentId)).values()
                    .stream().flatMap(Collection::stream)
                    .filter(r -> DayRange.current().contains(r.getUpdateDatetime()))
                    .collect(Collectors.toList());

            Map<String, Object> resultMap = new HashMap<>();
            List<UserPictureBookCardRef> cardRefs = pictureBookCardLoader.userCardRef(parentId);
            if (CollectionUtils.isEmpty(cardRefs)) {
                return noUserCardRecordHandler();
            }
            UserPictureBookCardRef lastCardRef = cardRefs.get(0);
            if (lastCardRef == null) {
                return noUserCardRecordHandler();
            }
            List<PictureBookCard> cardList = pictureBookCardClient
                    .loadCardByIds(Collections.singletonList(lastCardRef.getCardId()));
            PictureBookCard card = null;
            if (CollectionUtils.isNotEmpty(cardList)) {
                card = cardList.get(0);
            }
            //是否本周获取
            if (WeekRange.current().contains(lastCardRef.getUpdateTime())) {
                if (card != null) {
                    resultMap.put("card_id", card.getId());
                    resultMap.put("card_name", card.getName());
                    resultMap.put("is_finish", CollectionUtils.isNotEmpty(readRefList));
                    resultMap.put("has_color", !Objects.equals(lastCardRef.getCardType(), 2) && checkColorCard());
                    resultMap.put("img_url", card.getImgUrl());
                    resultMap.put("has_num", lastCardRef.getObtained());
                    resultMap.put("card_num", card.getFragmentNum());
                    resultMap.put("card_type", card.getCardType());
                    resultMap.put("des", card.getDescription());
                    return resultMap;
                }
                return resultMap;
            } else {
                UserPictureBookCardRef lastCommonCardRef = cardRefs.stream()
                        .filter(u -> Objects.equals(u.getCardType(), 1))
                        .min(Comparator.comparing(UserPictureBookCardRef::getUpdateTime).reversed())
                        .orElse(null);
                if (lastCommonCardRef == null) {
                    return null;
                }
                List<PictureBookCard> lastCommonCardList = pictureBookCardClient
                        .loadCardByIds(Collections.singletonList(lastCommonCardRef.getCardId()));
                if (CollectionUtils.isEmpty(lastCommonCardList)) {
                    return null;
                }
                PictureBookCard bookCard = checkAndSendCommonCard(lastCommonCardList.get(0), cardRefs);
                if (bookCard == null) {
                    return noUserCardRecordHandler();
                }
                resultMap.put("card_id", bookCard.getId());
                resultMap.put("card_name", bookCard.getName());
                resultMap.put("is_finish", CollectionUtils.isNotEmpty(readRefList));
                resultMap.put("has_color", checkColorCard());
                resultMap.put("img_url", bookCard.getImgUrl());
                resultMap.put("has_num", 0);
                resultMap.put("card_num", bookCard.getFragmentNum());
                resultMap.put("card_type", bookCard.getCardType());
                return resultMap;
            }
        }
    }

    private PictureBookCard checkAndSendCommonCard(PictureBookCard bookCard, List<UserPictureBookCardRef> userCardRefList) {
        if (CollectionUtils.isEmpty(userCardRefList)) {
            return null;
        }
        UserPictureBookCardRef cardRef = userCardRefList.get(0);
        if (cardRef == null) {
            return null;
        }
        String userCardId = cardRef.getCardId();
        if (StringUtils.isBlank(userCardId)) {
            return null;
        }
        if (bookCard == null) {
            return null;
        }
        /**
         * 1.检查当前套系是否有可发放卡片
         * 2.是，发放本套系卡片
         * 3.否，检查是否有其他套系可发放
         * 4.是，发放
         */
        List<PictrueBookCardMapper> cardMappers = pictureBookCardLoader.loadAllOnlineCard();
        if (CollectionUtils.isEmpty(cardMappers)) {
            return null;
        }
        List<String> userCardIdList = CollectionUtils.isEmpty(userCardRefList) ? Collections.emptyList() :
                userCardRefList.stream().map(UserPictureBookCardRef::getCardId).collect(Collectors.toList());
        String subjectId = bookCard.getSubjectId();
        if (StringUtils.isNotBlank(subjectId)) {
            PictrueBookCardMapper cardMapper = cardMappers.stream()
                    .filter(s -> subjectId.equals(s.getSubjectId())).findFirst().orElse(null);
            if (cardMapper == null) {
                return null;
            }
            List<PictureBookCard> availableCardList = cardMapper.getCardList();
            if (CollectionUtils.isNotEmpty(availableCardList) && CollectionUtils.isNotEmpty(userCardIdList)) {
                availableCardList = availableCardList.stream()
                        .filter(card -> !userCardIdList.contains(card.getId()))
                        .collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(availableCardList)) {
                PictureBookCard card = availableCardList.stream()
                        .min(Comparator.comparing(PictureBookCard::getOnLineTime))
                        .orElse(null);
                if (card != null) {
                    return card;
                }
            } else {//取其他可用套系
                return getAvailableCommonCard(userCardIdList, cardMappers);
            }
        }
        return null;
    }

    private PictureBookCard getAvailableCommonCard(List<String> userCardIdList, List<PictrueBookCardMapper> cardMappers) {
        List<String> userSubjectList = pictureBookCardClient.loadCardByIds(userCardIdList)
                .stream()
                .filter(c -> Objects.equals(c.getCardType(), 1))
                .map(PictureBookCard::getSubjectId).distinct().collect(Collectors.toList());

        PictrueBookCardMapper mapper = cardMappers.stream().filter(c -> !userSubjectList.contains(c.getSubjectId()))
                .min(Comparator.comparing(PictrueBookCardMapper::getOnLineTime))
                .orElse(null);
        if (mapper == null) {
            return null;
        }
        List<PictureBookCard> cardList = mapper.getCardList();
        if (CollectionUtils.isEmpty(cardList)) {
            return null;
        }
        return cardList.stream()
                .min(Comparator.comparing(PictureBookCard::getOnLineTime))
                .orElse(null);
    }

    private Map<String, Object> noUserCardRecordHandler() {
        List<PictrueBookCardMapper> cardMappers = pictureBookCardClient.loadAllOnlineCard();
        if (CollectionUtils.isEmpty(cardMappers)) {
            return null;
        }
        PictrueBookCardMapper mapper = cardMappers.get(0);
        if (mapper == null) {
            return null;
        }
        PictureBookCard card = mapper.getCardList()
                .stream()
                .min(Comparator.comparing(PictureBookCard::getOnLineTime))
                .orElse(null);
        if (card == null) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("card_id", card.getId());
        resultMap.put("card_name", card.getName());
        resultMap.put("is_finish", false);
        resultMap.put("has_color", checkColorCard());
        resultMap.put("img_url", card.getImgUrl());
        resultMap.put("has_num", 0);
        resultMap.put("card_num", card.getFragmentNum());
        resultMap.put("card_type", card.getCardType());
        return resultMap;
    }

    private boolean checkColorCard() {
        PictrueBookCardMapper mapper = pictureBookCardClient.loadAllOnlineCard().stream()
                .filter(s -> PictureBookCard.CardType.COLOR.name().equals(s.getSubjectName()))
                .findFirst().orElse(null);
        if (mapper == null) {
            return false;
        }
        List<PictureBookCard> cardList = mapper.getCardList();
        if (CollectionUtils.isEmpty(cardList)) {
            return false;
        }
        PictureBookCard currentWeekCard = cardList.stream()
                .filter(c -> {
                    Date startDate = c.getStartDate();
                    Date endDate = WeekRange.current().getEndDate();
                    int weekNum = c.getContinuedWeekNum();
                    while (weekNum > 1) {
                        endDate = WeekRange.newInstance(endDate.getTime()).getEndDate();
                        --weekNum;
                    }
                    if (WeekRange.current().contains(startDate) || WeekRange.current().contains(endDate)) {
                        return true;
                    }
                    return false;
                }).findFirst().orElse(null);
        if (currentWeekCard == null) {
            return false;
        }
        return true;
    }

    private Map<String, Object> getAllPicBookClassifyByLevel(int clazzLevel, String lastReadPictureBook) {
        Map<String, Object> resultMap = new HashMap<>(2);
        //确定当前要显示的年级
        /**除第一次外需要根据用户上一次读的绘本所在的年级选中对应年级
         * */
        String selectedClazz = "L" + clazzLevel + "A";
        if (StringUtils.isNotBlank(lastReadPictureBook)) {
            PictureBookPlus pictureBookPlus = pictureBookPlusServiceClient.loadById(lastReadPictureBook);
            List<PictureBookNewClazzLevel> clazzLevels = pictureBookPlus != null ? pictureBookPlus.getNewClazzLevels() : Collections.EMPTY_LIST;
            if (!CollectionUtils.isEmpty(clazzLevels)) {
                selectedClazz = clazzLevels.get(0).toString();
            }
        }
        resultMap.put("selected_clazz", selectedClazz);
        resultMap.put("clazz", pageBlockContentServiceClient.loadConfigList("PICBOOK_CLAZZ_PAGE", "PICBOOK_CLAZZ", PictureBookClazzLevelMapper.class));

        //获取tag集合(默认加载ALL tag)
        List<PictureBookTag> tags = pageBlockContentServiceClient.loadConfigList("PICBOOK_TAB_PAGE", "PICBOOK_TAG", PictureBookTag.class);
        resultMap.put("tags", getPictureBookTagMapperList(tags));
        //series
        resultMap.put("series", pageBlockContentServiceClient.loadConfigList("PICBOOK_SERIES_PAGE", "PICBOOK_SERIES", PictureBookSeriesOptionMapper.class));
        return resultMap;
    }

    private List<PictureBookTagMapper> getPictureBookTagMapperList(List<PictureBookTag> tags) {
        //获取tag集合(默认加载ALL tag)
        return CollectionUtils.isNotEmpty(tags) ? tags.stream()
                .map(t -> {
                    PictureBookTagMapper mapper = new PictureBookTagMapper();
                    mapper.setTagId(t.getTagId());
                    mapper.setTagName(t.getTagName());
                    mapper.setIsHomeShow(t.getIsHomeShow());
                    return mapper;
                }).collect(Collectors.toList()) : Collections.EMPTY_LIST;
    }

    private Page<PictureBookPlus> loadPictureBookPlusByPictureBookQuery(PictureBookQuery pictureBookQuery, Pageable pageable, NewBookProfile newBookProfile, List<String> readPicBookIds, String lexiler) {

        if (pictureBookQuery == null) {
            return PageableUtils.listToPage(Collections.emptyList(), pageable);
        }
        Stream<PictureBookPlus> pictureBookPlusStream = pictureBookPlusServiceClient.loadAllOnline().stream();
        if (CollectionUtils.isNotEmpty(readPicBookIds)) {
            pictureBookPlusStream = pictureBookPlusStream.filter(pb -> !readPicBookIds.contains(pb.getId()));
        }
        if (StringUtils.isNotBlank(lexiler) && lexiler.contains("-")) {
            lexiler = lexiler.replace("L", "");
            if (lexiler.contains("BR")) {
                lexiler = lexiler.replace("BR", "-");
            }
            String[] lexArray = lexiler.split("-");
            EmbedLexile lexile = new EmbedLexile();
            lexile.setPriceStart(SafeConverter.toInt(lexArray[0]));
            lexile.setPriceEnd(SafeConverter.toInt(lexArray[1]));
            pictureBookPlusStream = pictureBookPlusStream.filter(Objects::nonNull).filter(pb -> pb.getLexiler() != null)
                    .filter(pb -> pb.getLexiler().getPriceStart() >= lexile.getPriceStart() && pb.getLexiler().getPriceEnd() <= lexile.getPriceEnd());
        }
        return PageableUtils.listToPage(MobileJztReadingHelper.filterPictureBooks(pictureBookPlusStream, pictureBookQuery, newBookProfile).collect(Collectors.toList()), pageable);
    }

    private List<PictureBookSeriesMapper> getRecommendPicBookSeries() {
        List<PictureBookSeriesMapper> seriesMappers = new ArrayList<>();
        /**
         * 通过查询CRM配置，查找相应精选系列
         */
        List<PictureBookConfigList> configList = pictureBookConfigLoaderClient.getConfig().getConfigLists()
                .stream()
                .filter(pc -> pc.getShowType() == 1)
                .sorted(Comparator.comparing(PictureBookConfigList::getUpdateDate).reversed())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(configList)) {
            return Collections.emptyList();
        }
        for (PictureBookConfigList mapper : configList) {
            if (Objects.equals(mapper.getShowType(), 1)) {
                PictureBookSeriesMapper seriesMapper = new PictureBookSeriesMapper();
                seriesMapper.setSeriesId(mapper.getId());
                seriesMapper.setSeriesName(mapper.getTitle());
                seriesMapper.setSeriesContent(mapper.getIntroduction());
                seriesMapper.setSeriesIcon(Collections.singletonList(mapper.getAdImgUrl()));
                seriesMappers.add(seriesMapper);
            }
        }
        return seriesMappers;
    }


    @RequestMapping(value = "/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage pictureBookList() {
        MapMessage message = MapMessage.successMessage();
        String series = getRequestString("series");
        String tagId = getRequestString("tag_id");
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Integer unread = getRequestInt("un_read");
        String lexiler = getRequestString("lex");
        User parent = currentParent();
        try {
            if (StringUtils.isBlank(lexiler)) {
                return MapMessage.errorMessage("参数异常");
            }
            message.putAll(picBookTopicMapperHandler(tagId, unread, studentId, parent != null ? parent.getId() : null, series, lexiler));
            message.put("selected_lexiler", lexiler);
            message.put("selected_tag", tagId);
            return message;
        } catch (Exception e) {
            logger.error("get picture book gallery tab data error.", e);
            return MapMessage.errorMessage("获取绘本馆tab数据失败");
        }
    }

    @RequestMapping(value = "/all.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage allPictureBook() {
        MapMessage message = MapMessage.successMessage();
        try {
            //获取tag集合
            List<PictureBookTag> tags = pageBlockContentServiceClient.loadConfigList("PICBOOK_TAB_PAGE", "PICBOOK_TAG", PictureBookTag.class);
            message.put("tags", getPictureBookTagMapperList(tags));
            message.put("lexiler", pageBlockContentServiceClient.loadConfigList("PICBOOK_CLAZZ_PAGE", "PICBOOK_CLAZZ", PictureBookClazzLevelMapper.class));
            return message;
        } catch (Exception e) {
            logger.error("get picture book gallery error.", e);
            return MapMessage.errorMessage("获取绘本馆数据失败");
        }
    }

    private Map<String, Object> picBookTopicMapperHandler(String tagId, Integer unread, Long studentId, Long parentId, String series, String lexiler) {
        Map<String, Object> result = new HashMap<>(7);
        //获取tag集合(默认加载ALL tag)
        List<PictureBookTag> tags = pageBlockContentServiceClient.loadConfigList("PICBOOK_TAB_PAGE", "PICBOOK_TAG", PictureBookTag.class);
        List<String> selectTopic = new ArrayList<>();
        if (StringUtils.isNotBlank(tagId)) {
            result.put("selected_tag", tagId);
            selectTopic = tags.stream().filter(t -> tagId.equals(t.getTagId())).map(PictureBookTag::getTopics).findFirst().orElse(null);
        }
        List<String> readPicBookIds = new ArrayList<>();
        //留下此段代码，以防再次启用
        /*if (Objects.equals(unread, 0)) {
            List<Long> userList = new ArrayList<>();
            if (studentId != null) {
                userList.add(studentId);
            }
            if (parentId != null) {
                userList.add(parentId);
            }
            readPicBookIds = pictureBookLoaderByUserReading(userList).stream().map(PictureBookPlus::getId).collect(Collectors.toList());
        }*/

        int pageSize = SafeConverter.toInt(getRequestString("page_size"), 15);
        int currentPage = SafeConverter.toInt(getRequestString("current_page"), 1);
        result.put("page_size", pageSize);
        result.put("current_page", currentPage);

        Pageable pageable = new PageRequest(currentPage - 1, pageSize);
        PictureBookQuery pictureBookQuery = new PictureBookQuery();
        if (CollectionUtils.isNotEmpty(selectTopic)) {
            pictureBookQuery.setTopicIds(selectTopic);
        }
        if (StringUtils.isNotBlank(series)) {
            pictureBookQuery.setSeriesIds(Collections.singletonList(series));
        }
        NewBookProfile newBookProfile = new NewBookProfile();
        newBookProfile.setSubjectId(Subject.ENGLISH.getId());
        Page<PictureBookPlus> page = loadPictureBookPlusByPictureBookQuery(pictureBookQuery, pageable, newBookProfile, readPicBookIds, lexiler);

        result.put("total_page", page.getTotalPages());
        result.put("total", page.getTotalElements());

        List<PictureBookPlus> selectLevelPicBookList = page.getContent();
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
        result.put("data", selectLevelPicBookList.stream().map(p -> {
            List<String> topicIds = p.getTopicIds();
            PictureBookTag tag = tags.stream().filter(Objects::nonNull)
                    .filter(t -> t.getTopics().containsAll(topicIds)).findFirst().orElse(null);
            PictureBookTopic topic = pictureBookTopicList.stream()
                    .filter(i -> p.getTopicIds().contains(i.getId()))
                    .findFirst()
                    .orElse(null);
            PicBookRecommendMapper book = new PicBookRecommendMapper();
            book.setCoverUrl(p.getCoverUrl());
            book.setThumbnail(p.getCoverThumbnailUrl());
            book.setId(p.getId());
            book.setName(p.getEname());
            book.setType(1);
            book.setWordsLength(p.getWordsLength());
            book.setNameZH(p.getCname());
            book.setTopicText(tag == null ? topic == null ? "" : topic.getName() : tag.getTagName());
            return book;
        }).collect(Collectors.toList()));
        return result;
    }

    @RequestMapping(value = "/series.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage seriesDetail() {
        String series = getRequestString("series");
        MapMessage message = MapMessage.successMessage();
        try {
            if (StringUtils.isBlank(series)) {
                return MapMessage.errorMessage("参数异常");
            }
            PictureBookConfig config = pictureBookConfigLoaderClient.getConfig();

            List<PictureBookConfigList> configLists = config.getConfigLists();
            if (CollectionUtils.isEmpty(configLists)) {
                return MapMessage.errorMessage("参数异常");
            }
            PictureBookConfigList configList = configLists.stream().filter(c -> c.getId().equals(series)).findFirst().orElse(null);
            if (configList == null) {
                return MapMessage.errorMessage("参数异常");
            }
            List<PictureBookConfigInfo> infoList = config.getInfoList();
            if (CollectionUtils.isEmpty(infoList)) {
                return MapMessage.errorMessage("参数异常");
            }
            infoList = infoList.stream().filter(i -> i.getConfigListId().equals(configList.getId())).collect(Collectors.toList());
            message.put("data", CollectionUtils.isNotEmpty(infoList) ? dealSeriesDetailPicBook(infoList) : Collections.EMPTY_LIST);
            message.put("series_name", configList.getTitle());
            message.put("banner_img", configList.getMainImgUrl());
            message.put("introduction", configList.getIntroduction());
            message.put("series_id", series);
            return message;
        } catch (Exception e) {
            logger.error("get picture book series data error.", e);
            return MapMessage.errorMessage("获取绘本馆系列详情数据失败");
        }
    }

    private List<PicBookInSeriesMapper> dealSeriesDetailPicBook(List<PictureBookConfigInfo> infoList) {
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
        List<PicBookInSeriesMapper> mappers = new ArrayList<>();

        List<String> picBookIds = infoList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(PictureBookConfigInfo::getUpdateDate))
                .map(PictureBookConfigInfo::getPictureBookId)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(picBookIds)) {
            picBookIds.forEach(p -> {
                PictureBookPlus bookPlus = pictureBookPlusServiceClient.loadById(p);
                if (bookPlus != null) {
                    PictureBookConfigInfo vo = infoList.stream()
                            .filter(i -> i.getPictureBookId().equals(bookPlus.getId()))
                            .findFirst()
                            .orElse(null);
                    PicBookInSeriesMapper mapper = new PicBookInSeriesMapper();
                    mapper.setPictureBookId(bookPlus.getId());
                    mapper.setCnName(bookPlus.getCname());
                    mapper.setEnName(bookPlus.getEname());

                    PictureBookTopic topic = pictureBookTopicList.stream()
                            .filter(i -> bookPlus.getTopicIds().contains(i.getId()))
                            .findFirst()
                            .orElse(null);

                    mapper.setTopicText(topic == null ? "" : topic.getName());
                    mapper.setRecommendWord(vo == null ? "" : vo.getRecommendWords());
                    mapper.setRecommendWordSecond(vo == null ? "" : vo.getRecommendWordsSecond());
                    mapper.setThumbnail(bookPlus.getCoverThumbnailUrl());
                    mapper.setConfigWords(vo == null ? "" : vo.getConfigWords());
                    mappers.add(mapper);
                }
            });
        }
        return mappers;
    }

    @RequestMapping(value = "/read/finish.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readFinish() {
        String picBookId = getRequestString("pb_id");
        Long readSeconds = getRequestLong("read_time");
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentUser();
        if (parent == null) {
            return go2LoginPageResult;
        }
        if (StringUtils.isBlank(picBookId)) {
            return MapMessage.errorMessage("pb_id error");
        }
        Long userId = studentId;
        UserReadingRef readingRef = new UserReadingRef();
        if (studentId != 0L) {
            List<UserReadingRef> userReadingRefList = jztReadingLoader.getUserReadingRefsByUserId(parent.getId());
            UserReadingRef userReadingRef = userReadingRefList.stream()
                    .filter(e -> picBookId.equals(e.getPictureBookId()))
                    .findFirst()
                    .orElse(null);

            if (userReadingRef != null) {
                readingRef.setUserId(parent.getId());
            } else {
                readingRef.setUserId(studentId);
            }
        } else {
            userId = parent.getId();
            readingRef.setUserId(parent.getId());
        }
        readingRef.setPictureBookId(picBookId);
        readingRef.setSelfStudyType(SelfStudyType.READING_ENGLISH_PLUS);
        readingRef.setFinishStatus(1);
        readingRef.setReadFinishTime(new Date());
        readingRef.setReadSeconds(readSeconds);
        //更新绘本阅读信息
        MapMessage mapMessage = jztReadingService.upsertUserReadingRef(readingRef);
        if (!mapMessage.isSuccess()) {
            logger.error("当前绘本:{},sid:{},pid{},完成状态更新失败", picBookId, studentId, currentUserId());
            return MapMessage.errorMessage("完成状态更新失败");
        }
        //更新当前用户阅读完成绘本数量缓存
        long finishCount = jztReadingLoader.getUserReadingRefsByUserIds(Arrays.asList(studentId, parent.getId()))
                .values()
                .stream().flatMap(Collection::stream)
                .filter(u -> Objects.equals(u.getFinishStatus(), 1)).count();
        PictureBookCacheManager.INSTANCE.initReadFinishCount(userId, SafeConverter.toInt(finishCount));
        //本周第一次阅读完成一本发放学豆(B端孩子)
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail != null) {
            if (studentDetail.getClazz() != null) {
                boolean integralRewardStatus = PictureBookCacheManager.INSTANCE.getReadIntegralRewardStatus(studentId);
                if (!integralRewardStatus) {
                    pictureBookCardService.sendIntegralReward(studentId);
                    PictureBookCacheManager.INSTANCE.recordReadIntegralRewardStatus(studentId);
                }
            }
        }
        return mapMessage;
    }

    @RequestMapping(value = "/finish/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage readFinishDetail() {
        String picBookId = getRequestString("pb_id");
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentUser();
        if (parent == null) {
            return go2LoginPageResult;
        }
        if (StringUtils.isBlank(picBookId)) {
            return MapMessage.errorMessage("pb_id error");
        }
        Long userId = studentId != 0L ? studentId : parent.getId();
        int readCount = PictureBookCacheManager.INSTANCE
                .getReadFinishCount(userId);
        List<UserReadingRef> readingRefs = jztReadingLoader
                .getUserReadingRefsByUserIds(Arrays.asList(studentId, parent.getId()))
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        if (readCount == 0) {
            readCount = SafeConverter.toInt(readingRefs.stream().filter(u -> Objects.equals(u.getFinishStatus(), 1))
                    .count());
            PictureBookCacheManager.INSTANCE.initReadFinishCount(userId, readCount);
        }

        int readSeconds = 0;
        if (CollectionUtils.isNotEmpty(readingRefs)) {
            UserReadingRef userReadingRef = readingRefs.stream().filter(Objects::nonNull)
                    .filter(u -> picBookId.equals(u.getPictureBookId())
                            && Objects.equals(u.getFinishStatus(), 1)).findFirst().orElse(null);
            if (userReadingRef != null) {
                readSeconds = SafeConverter.toInt(userReadingRef.getReadSeconds());
            }
        }
        PictureBookPlus bookPlus = pictureBookPlusServiceClient.loadById(picBookId);
        //更多推荐
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(Objects.equals(studentId, 0L) ? null : studentId);
        int clazzLevel = (studentDetail == null || studentDetail.getClazzLevel() == null ||
                NEED_DEAL_CLAZZ_LEVEL_ARRAY.contains(SafeConverter.toInt(studentDetail.getClazzLevel().getLevel()))) ?
                DEFAULT_CLAZZ_LEVEL : SafeConverter.toInt(studentDetail.getClazzLevel().getLevel());
        List<PicBookRecommendMapper> bookList = recommendPicBook(clazzLevel, 3, studentId, parent.getId())
                .stream().map(p -> {
                    PicBookRecommendMapper book = new PicBookRecommendMapper();
                    book.setCoverUrl(p.getCoverUrl());
                    book.setThumbnail(p.getCoverThumbnailUrl());
                    book.setId(p.getId());
                    book.setName(p.getEname());
                    book.setNameZH(p.getCname());
                    book.setType(1);
                    return book;
                }).collect(Collectors.toList());

        String userDisplayName = "";
        if (studentDetail != null && studentDetail.getProfile() != null) {
            String realName = studentDetail.getProfile().getRealname();
            if (StringUtils.isNotBlank(realName)) {
                userDisplayName = realName.substring(0, 1);
            }
        }
        return MapMessage.successMessage().add("read_count", readCount).add("seconds", readSeconds)
                .add("words", bookPlus != null ? bookPlus.getWordsLength() : 0)
                .add("en_name", bookPlus != null ? bookPlus.getEname() : "")
                .add("cover_url", bookPlus != null ? bookPlus.getCoverThumbnailUrl() : "")
                .add("recommend_list", bookList)
                .add("cn_name", bookPlus != null ? bookPlus.getCname() : "")
                .add("gender", studentDetail != null ? studentDetail.getProfile().getGender() : "")
                .add("user_name", userDisplayName + "同学");
    }

    @RequestMapping(value = "/card/mine.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage mineCard() {
        User parent = currentParent();
        if (parent == null) {
            return go2LoginPageResult;
        }
        List<UserPictureBookCardRef> userCardRefList = pictureBookCardLoader.userCardRef(parent.getId());
        List<PictrueBookCardMapper> cardMappers = pictureBookCardClient.loadAllOnlineCard();
        List<Map<String, Object>> commonCardMapList = new ArrayList<>();
        boolean currentCardFlag = true;
        for (PictrueBookCardMapper mapper : cardMappers) {
            if (mapper != null && StringUtils.isNotBlank(mapper.getSubjectId())) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("sub_id", mapper.getSubjectId());
                resultMap.put("sub_name", mapper.getSubjectName());
                List<PictureBookCard> cardList = mapper.getCardList().stream()
                        .sorted(Comparator.comparing(PictureBookCard::getOnLineTime))
                        .collect(Collectors.toList());
                List<String> currentSubCardIdList = cardList.stream()
                        .map(PictureBookCard::getId)
                        .collect(Collectors.toList());
                List<UserPictureBookCardRef> currentUserCardRefList = userCardRefList.stream()
                        .filter(s -> currentSubCardIdList.contains(s.getCardId()))
                        .sorted(Comparator.comparing(UserPictureBookCardRef::getUpdateTime).reversed())
                        .collect(Collectors.toList());
                if (currentCardFlag) {
                    if (CollectionUtils.isNotEmpty(currentUserCardRefList)) {
                        UserPictureBookCardRef cardRef = currentUserCardRefList.get(0);
                        if (cardRef != null) {
                            if (!WeekRange.current().contains(cardRef.getUpdateTime())) {
                                int cardIndex = currentSubCardIdList.indexOf(cardRef.getCardId()) + 1;
                                if (cardList.size() > cardIndex) {
                                    PictureBookCard card = cardList.get(cardIndex);
                                    if (card != null) {
                                        resultMap.put("current_card", card.getId());
                                        currentCardFlag = false;
                                    }
                                }
                            } else {
                                resultMap.put("current_card", cardRef.getCardId());
                                currentCardFlag = false;
                            }
                        } else {
                            resultMap.put("current_card", cardList.get(0) != null ? cardList.get(0).getId() : "");
                            currentCardFlag = false;
                        }
                    } else {
                        resultMap.put("current_card", cardList.get(0) != null ? cardList.get(0).getId() : "");
                        currentCardFlag = false;
                    }
                }
                List<Map<String, Object>> cardMapList = new ArrayList<>(cardList.size());
                for (PictureBookCard card : cardList) {
                    Map<String, Object> cardListMap = new HashMap<>();
                    cardListMap.put("card_id", card.getId());
                    cardListMap.put("card_name", card.getName());
                    cardListMap.put("img_url", card.getImgUrl());
                    UserPictureBookCardRef cardRef = userCardRefList.stream()
                            .filter(c -> card.getId().equals(c.getCardId())).findFirst().orElse(null);
                    cardListMap.put("has_num", cardRef != null ? cardRef.getObtained() : 0);
                    cardListMap.put("card_num", card.getFragmentNum());
                    cardMapList.add(cardListMap);
                }
                resultMap.put("card_list", cardMapList);
                commonCardMapList.add(resultMap);
            }
        }
        //彩蛋卡
        List<UserPictureBookCardRef> colorRef = userCardRefList
                .stream().filter(c -> Objects.equals(c.getCardType(), 2)).collect(Collectors.toList());
        List<Map<String, Object>> colorMapList = new ArrayList<>(colorRef.size());
        if (CollectionUtils.isNotEmpty(colorRef)) {
            List<PictureBookCard> colorCardList = pictureBookCardClient.loadCardByIds(colorRef.stream()
                    .map(UserPictureBookCardRef::getCardId).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(colorCardList)) {
                for (PictureBookCard card : colorCardList) {
                    Map<String, Object> colorMap = new HashMap<>();
                    colorMap.put("card_id", card.getId());
                    colorMap.put("card_name", card.getName());
                    colorMap.put("img_url", card.getImgUrl());
                    colorMapList.add(colorMap);
                }
            }
        }
        return MapMessage.successMessage().add("list", commonCardMapList).add("color_list", colorMapList);
    }

    @RequestMapping(value = "/card/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage cardDetail() {
        User parent = currentUser();
        if (parent == null) {
            return go2LoginPageResult;
        }
        Integer type = getRequestInt("type");
        if (Objects.equals(type, 0)) {
            return MapMessage.errorMessage("参数type异常");
        }
        try {
            List<UserPictureBookCardRef> userCardRefList = pictureBookCardLoader.userCardRef(parent.getId());
            List<PictrueBookCardMapper> cardMappers = pictureBookCardClient.loadAllOnlineCard();
            if (Objects.equals(type, 1)) {
                String subjectId = getRequestString("subjectId");
                if (StringUtils.isBlank(subjectId)) {
                    return MapMessage.errorMessage("参数subjectId异常");
                }
                MapMessage message = MapMessage.successMessage();
                PictrueBookCardMapper mapper = cardMappers.stream().filter(c -> subjectId.equals(c.getSubjectId()))
                        .findFirst().orElse(null);
                if (mapper == null) {
                    return message;
                }
                List<String> mapperCardList = mapper.getCardList()
                        .stream().map(PictureBookCard::getId).collect(Collectors.toList());
                int cardListLimit = 1;
                if (CollectionUtils.isNotEmpty(userCardRefList)) {
                    List<UserPictureBookCardRef> userCommonCadRefList = userCardRefList.stream()
                            .filter(u -> Objects.equals(u.getCardType(), 1) && mapperCardList.contains(u.getCardId()))
                            .sorted(Comparator.comparing(UserPictureBookCardRef::getUpdateTime).reversed())
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(userCommonCadRefList)) {
                        cardListLimit = userCommonCadRefList.size();
                        UserPictureBookCardRef cardRef = userCommonCadRefList.get(0);
                        if (cardRef != null) {
                            if (!WeekRange.current().contains(cardRef.getUpdateTime())) {
                                cardListLimit += 1;
                            }
                        }
                    }
                }
                List<PictureBookCard> cardList = mapper.getCardList().stream()
                        .sorted(Comparator.comparing(PictureBookCard::getOnLineTime))
                        .limit(cardListLimit)
                        .collect(Collectors.toList());
                List<Map<String, Object>> cardMapList = new ArrayList<>(cardList.size());
                for (PictureBookCard card : cardList) {
                    Map<String, Object> cardListMap = new HashMap<>();
                    cardListMap.put("card_id", card.getId());
                    cardListMap.put("card_name", card.getName());
                    cardListMap.put("img_url", card.getImgUrl());
                    UserPictureBookCardRef cardRef = userCardRefList.stream()
                            .filter(c -> card.getId().equals(c.getCardId())).findFirst().orElse(null);
                    cardListMap.put("has_num", cardRef != null ? cardRef.getObtained() : 0);
                    cardListMap.put("card_num", card.getFragmentNum());
                    cardListMap.put("des", card.getDescription());
                    cardMapList.add(cardListMap);
                }
                return message.add("sub_id", mapper.getSubjectId())
                        .add("sub_name", mapper.getSubjectName())
                        .add("card_list", cardMapList);
            } else if (Objects.equals(type, 2)) {
                List<Map<String, Object>> colorMapList = new ArrayList<>();
                List<UserPictureBookCardRef> colorRef = userCardRefList
                        .stream().filter(c -> Objects.equals(c.getCardType(), 2)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(colorRef)) {
                    List<PictureBookCard> colorCardList = pictureBookCardClient.loadCardByIds(colorRef.stream()
                            .map(UserPictureBookCardRef::getCardId).collect(Collectors.toList()));
                    if (CollectionUtils.isNotEmpty(colorCardList)) {
                        for (PictureBookCard card : colorCardList) {
                            Map<String, Object> colorMap = new HashMap<>();
                            colorMap.put("card_id", card.getId());
                            colorMap.put("card_name", card.getName());
                            colorMap.put("img_url", card.getImgUrl());
                            UserPictureBookCardRef cardRef = colorRef.stream()
                                    .filter(c -> card.getId().equals(c.getCardId())).findFirst().orElse(null);
                            colorMap.put("has_num", cardRef != null ? 1 : 0);
                            colorMap.put("card_num", 1);
                            colorMap.put("des", card.getDescription());
                            colorMapList.add(colorMap);
                        }
                    }
                }
                return MapMessage.successMessage().add("card_list", colorMapList);
            }
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage("获取数据异常，参数为subjectId:{},type{},userId{}", getRequestString("subjectId"), type, parent.getId());
        }
    }

    @RequestMapping(value = "/card/get.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCard() {
        Long sid = getRequestLong(REQ_STUDENT_ID);
        User parent = currentUser();
        if (parent == null) {
            return go2LoginPageResult;
        }
        Long userId = parent.getId();
        boolean isCardPop = PictureBookCacheManager.INSTANCE.getReadReadCardPopStatus(userId);
        boolean isIntegralPop = false;
        boolean sendRewardStatus = PictureBookCacheManager.INSTANCE.getReadIntegralRewardStatus(sid);
        if (sendRewardStatus) {
            boolean integralPop = PictureBookCacheManager.INSTANCE.getReadFinishSendIntegralPopStatus(sid);
            if (!integralPop) {
                isIntegralPop = true;
                //已经发放学豆成功，则记录下已经弹窗
                PictureBookCacheManager.INSTANCE.recordReadFinishSendIntegralPopStatus(sid);
            }
        }
        if (isCardPop) {
            return MapMessage.successMessage().add("is_integral_pop", isIntegralPop)
                    .add("is_card_pop", false);
        }
        String sendCardId = SafeConverter.toString(CacheSystem.CBS.getCache(PERSISTENCE)
                .load(USER_CURRENT_PICTURE_BOOK_CARD_PRE + userId));
        if (StringUtils.isBlank(sendCardId)) {
            return MapMessage.successMessage("暂无卡片").add("is_integral_pop", isIntegralPop)
                    .add("is_card_pop", false);
        }
        List<PictureBookCard> cardList = pictureBookCardClient.loadCardByIds(Collections.singletonList(sendCardId));
        if (CollectionUtils.isEmpty(cardList)) {
            return MapMessage.errorMessage("获取卡片异常");
        }
        PictureBookCard card = cardList.get(0);
        int hasNum = 0;
        List<UserPictureBookCardRef> userCardRefs = pictureBookCardLoader.userCardRef(userId);
        if (CollectionUtils.isNotEmpty(userCardRefs)) {
            UserPictureBookCardRef cardRef = userCardRefs.stream()
                    .filter(c -> card.getId().equals(c.getCardId())).findFirst().orElse(null);
            if (cardRef != null) {
                hasNum = cardRef.getObtained();
            }
        }
        List<UserReadingRef> readRefList = jztReadingLoader
                .getUserReadingRefsByUserIds(Arrays.asList(sid, userId)).values()
                .stream().flatMap(Collection::stream)
                .filter(r -> DayRange.current().contains(r.getUpdateDatetime()))
                .collect(Collectors.toList());

        //记录用户卡片已经弹窗
        PictureBookCacheManager.INSTANCE.recordReadCardPopStatus(userId);
        return MapMessage.successMessage().add("card_id", card.getId())
                .add("img_url", card.getImgUrl())
                .add("card_num", card.getFragmentNum())
                .add("card_name", card.getName())
                .add("des", card.getDescription())
                .add("has_num", hasNum)
                .add("is_integral_pop", isIntegralPop)
                .add("is_card_pop", true)
                .add("has_color", checkColorCard())
                .add("is_finish", CollectionUtils.isNotEmpty(readRefList))
                .add("card_type", card.getCardType());
    }

}
