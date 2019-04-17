package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoBulkWriteException;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.OutsideReadingBookType;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingCollection;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingMission;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingPractice;
import com.voxlearning.utopia.service.newhomework.api.mapper.OutsideReadingDynamicCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.service.OutsideReadingService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.cache.OutsideReadingDynamicCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.TeacherAssignmentRecordDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.TotalAssignmentRecordDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.outside.OutsideReadingAchievementDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.outside.OutsideReadingCollectionDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.outside.OutsideReadingDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.OutsideReadingLoadermpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.TeacherAssignmentRecordLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.TotalAssignmentRecordLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside.OutsideReadingResultProcessor;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.stone.data.*;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = OutsideReadingService.class)
@ExposeService(interfaceClass = OutsideReadingService.class)
public class OutsideReadingServiceImpl extends SpringContainerSupport implements OutsideReadingService {

    @Inject private RaikouSDK raikouSDK;

    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private StoneDataLoaderClient stoneDataLoaderClient;
    @Inject private OutsideReadingDao outsideReadingDao;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private OutsideReadingResultProcessor outsideReadingResultProcessor;
    @Inject private OutsideReadingLoadermpl outsideReadingLoader;
    @Inject private OutsideReadingCollectionDao outsideReadingCollectionDao;
    @Inject private OutsideReadingAchievementDao outsideReadingAchievementDao;
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private TeacherAssignmentRecordLoaderImpl teacherAssignmentRecordLoader;
    @Inject private TotalAssignmentRecordLoaderImpl totalAssignmentRecordLoader;
    @Inject private TeacherAssignmentRecordDao teacherAssignmentRecordDao;
    @Inject private TotalAssignmentRecordDao totalAssignmentRecordDao;
    @Inject private AsyncAvengerHomeworkServiceImpl asyncAvengerHomeworkService;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;


    @Override
    public MapMessage loadTeacherClazzList(Long teacherId) {
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId).stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        Map<Long, List<GroupMapper>> groupMaps = groupLoaderClient.loadClazzGroups(clazzs.stream().map(Clazz::getId).collect(Collectors.toList()));
        List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByTeacherId(teacherId)
                .stream()
                .map(GroupTeacherTuple::getGroupId)
                .collect(Collectors.toList());
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(teacherGroupIds);
        Map<Long, Long> clazzIdGroupIdMap = new LinkedHashMap<>();
        for (Clazz clazz : clazzs) {
            List<GroupMapper> groupMapperList = groupMaps.get(clazz.getId());
            if (CollectionUtils.isNotEmpty(groupMapperList)) {
                groupMapperList.forEach(groupMapper -> {
                    if (teacherGroupIds.contains(groupMapper.getId())) {
                        clazzIdGroupIdMap.put(clazz.getId(), groupMapper.getId());
                    }
                });
            }
        }
        Map<Integer, List<Map>> clazzMap = clazzs.stream()
                .filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .map(clazz -> {
                    Long groupId = clazzIdGroupIdMap.get(clazz.getId());
                    return MapUtils.m("clazzId", clazz.getId(),
                            "clazzName", clazz.getClassName(),
                            "fullName", clazz.formalizeClazzName(),
                            "clazzLevel", clazz.getClazzLevel().getLevel(),
                            "groupId", groupId,
                            "emptyClazz", CollectionUtils.isEmpty(groupStudentIds.get(groupId)));
                })
                .collect(Collectors.groupingBy(clazz -> SafeConverter.toInt(clazz.get("clazzLevel")), LinkedHashMap::new, Collectors.mapping(p -> p, Collectors.toList())));
        List<Map<String, Object>> clazzList = new ArrayList<>();
        clazzMap.forEach((k, v) -> clazzList.add(MapUtils.m(
                "clazzLevel", k,
                "clazzLevelName", ClazzLevel.parse(k).getDescription(),
                "clazzs", v)));
        return MapMessage.successMessage().add("clazzList", clazzList);
    }

    @Override
    public MapMessage loadReportClazzList(Long teacherId) {
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId).stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        Map<Long, List<GroupMapper>> groupMaps = groupLoaderClient.loadClazzGroups(clazzs.stream().map(Clazz::getId).collect(Collectors.toList()));
        List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByTeacherId(teacherId)
                .stream()
                .map(GroupTeacherTuple::getGroupId)
                .collect(Collectors.toList());
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(teacherGroupIds);
        Map<Long, Long> clazzIdGroupIdMap = new LinkedHashMap<>();
        for (Clazz clazz : clazzs) {
            List<GroupMapper> groupMapperList = groupMaps.get(clazz.getId());
            if (CollectionUtils.isNotEmpty(groupMapperList)) {
                groupMapperList.forEach(groupMapper -> {
                    if (teacherGroupIds.contains(groupMapper.getId())) {
                        clazzIdGroupIdMap.put(clazz.getId(), groupMapper.getId());
                    }
                });
            }
        }
        List<Map<String, Object>> clazzList = clazzs.stream()
                .filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .map(clazz -> {
                    Long groupId = clazzIdGroupIdMap.get(clazz.getId());
                    return MapUtils.m("clazzId", clazz.getId(),
                            "clazzName", clazz.getClassName(),
                            "fullName", clazz.formalizeClazzName(),
                            "clazzLevel", clazz.getClazzLevel().getLevel(),
                            "groupId", groupId,
                            "emptyClazz", CollectionUtils.isEmpty(groupStudentIds.get(groupId)));
                }).collect(Collectors.toList());
        return MapMessage.successMessage().add("clazzList", clazzList);
    }

    @Override
    public MapMessage loadBookTypeList() {
        List<Map<String, Object>> bookTypeList = new ArrayList<>();
        bookTypeList.add(MapUtils.m("typeId", "", "typeName", "全部类型"));
        for (OutsideReadingBookType bookType : OutsideReadingBookType.values()) {
            bookTypeList.add(MapUtils.m("typeId", bookType.name(), "typeName", bookType.getName()));
        }
        return MapMessage.successMessage().add("bookTypeList", bookTypeList);
    }

    @Override
    public MapMessage loadBookList(Long teacherId, Long groupId, Integer clazzLevel, String bookType, Integer pageNumber, Integer pageSize) {
        Map<String, StoneBufferedData> stoneBufferedDataMap = stoneDataLoaderClient.getStoneBufferedDataLinkedHashMap();
        List<StoneBufferedData> bookList = new ArrayList<>();
        for (StoneBufferedData stoneBufferedData : stoneBufferedDataMap.values()) {
            if (stoneBufferedData.getReadingOTOBook() != null && StringUtils.equals("online", stoneBufferedData.getReadingOTOBook().getOlStatus()) && stoneBufferedData.getDeletedAt() == null) {
                bookList.add(stoneBufferedData);
            }
        }
        OutsideReadingBookType outsideReadingBookType = OutsideReadingBookType.of(bookType);
        List<StoneBufferedData> filteredBookList = bookList.stream()
                .filter(book -> outsideReadingBookType == null || outsideReadingBookType.getGenre().equals(book.getReadingOTOBook().getGenre()))
                .filter(book -> clazzLevel == null || clazzLevel == 0 || (CollectionUtils.isNotEmpty(book.getReadingOTOBook().getForGrades()) && book.getReadingOTOBook().getForGrades().contains(clazzLevel)))
                .collect(Collectors.toList());

        Set<String> bookIdSet = filteredBookList.stream().map(book -> SafeConverter.toString(book.getId())).collect(Collectors.toSet());
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(Subject.CHINESE, teacherId, NewHomeworkConstants.OUT_SIDE_READING_DEFAULT_BOOK_ID);
        Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(Subject.CHINESE, bookIdSet, HomeworkContentType.PACKAGE);

        filteredBookList.sort(
                (a, b) -> {
                    int assignTimesA = (MapUtils.isNotEmpty(totalAssignmentRecordMap) && totalAssignmentRecordMap.containsKey(a.getId())) ? SafeConverter.toInt(totalAssignmentRecordMap.get(a.getId()).getAssignTimes()) : 0;
                    int assignTimesB = (MapUtils.isNotEmpty(totalAssignmentRecordMap) && totalAssignmentRecordMap.containsKey(b.getId())) ? SafeConverter.toInt(totalAssignmentRecordMap.get(b.getId()).getAssignTimes()) : 0;
                    return Integer.compare(assignTimesB, assignTimesA);
                }
        );

        filteredBookList.sort(
                (a, b) -> {
                    int assignTimesA = teacherAssignmentRecord != null && MapUtils.isNotEmpty(teacherAssignmentRecord.getAppInfo()) ? SafeConverter.toInt(teacherAssignmentRecord.getAppInfo().get(groupId + "-" + a.getId())) : 0;
                    int assignTimesB = teacherAssignmentRecord != null && MapUtils.isNotEmpty(teacherAssignmentRecord.getAppInfo()) ? SafeConverter.toInt(teacherAssignmentRecord.getAppInfo().get(groupId + "-" + b.getId())) : 0;
                    return Integer.compare(assignTimesA, assignTimesB);
                }
        );

        Pageable pageable = new PageRequest(pageNumber - 1, pageSize);
        Page<StoneBufferedData> bookPage = PageableUtils.listToPage(filteredBookList, pageable);
        List<OutsideReading> outsideReadingList = outsideReadingLoader.loadOutsideReadingByGroupId(groupId);
        List<Map<String, Object>> bookMapperList = bookPage.getContent()
                .stream()
                .map(book -> MapUtils.m(
                        "bookId", SafeConverter.toString(book.getId()),
                        "bookName", SafeConverter.toString(book.getReadingOTOBook().getBookName(), ""),
                        "wordsCount", processWordsCount(SafeConverter.toDouble(book.getReadingOTOBook().getTotalNum())),
                        "tags", book.getReadingOTOBook().getTags(),
                        "time", SafeConverter.toInt(book.getReadingOTOBook().getTimeUse()),
                        "missionCount", SafeConverter.toInt(book.getReadingOTOBook().getMissionNum()),
                        "coverImage", SafeConverter.toString(book.getReadingOTOBook().getCoverPic()),
                        "recommendDays", SafeConverter.toInt(book.getReadingOTOBook().getDayUse()),
                        "guidanceCount", SafeConverter.toInt(book.getReadingOTOBook().getMissionNum()),
                        "ongoing", outsideReadingList.stream().anyMatch(outsideReading -> !outsideReading.isTerminated() && StringUtils.equals(outsideReading.getPractices().getBookId(), book.getId())),
                        "showAssigned", teacherAssignmentRecord != null && MapUtils.isNotEmpty(teacherAssignmentRecord.getAppInfo()) && SafeConverter.toInt(teacherAssignmentRecord.getAppInfo().get(groupId + "-" + book.getId())) > 0,
                        "assignTimes", (MapUtils.isNotEmpty(totalAssignmentRecordMap) && totalAssignmentRecordMap.containsKey(book.getId())) ? SafeConverter.toInt(totalAssignmentRecordMap.get(book.getId()).getAssignTimes()) : 0
                ))
                .collect(Collectors.toList());
        return MapMessage.successMessage()
                .add("bookList", bookMapperList)
                .add("pageSize", bookPage.getTotalPages())
                .add("elementSize", bookPage.getTotalElements());
    }

    @Override
    public MapMessage loadBookDetail(Long teacherId, Long groupId, String bookId) {
        ReadingOTOBook readingOTOBook = stoneDataLoaderClient.loadReadingOTOBookByIds(Collections.singleton(bookId)).get(bookId);
        if (readingOTOBook == null) {
            return MapMessage.errorMessage("课本不存在");
        }
        List<OutsideReading> outsideReadingList = outsideReadingLoader.loadOutsideReadingByGroupId(groupId);
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(Subject.CHINESE, teacherId, NewHomeworkConstants.OUT_SIDE_READING_DEFAULT_BOOK_ID);
        Map<String, Object> readingOTOBookDetailMapper = MapUtils.m(
                "bookId", bookId,
                "bookName", SafeConverter.toString(readingOTOBook.getBookName(), ""),
                "coverImage", SafeConverter.toString(readingOTOBook.getCoverPic(), ""),
                "ongoing", outsideReadingList.stream().anyMatch(outsideReading -> !outsideReading.isTerminated() && StringUtils.equals(outsideReading.getPractices().getBookId(), bookId)),
                "showAssigned", teacherAssignmentRecord != null && MapUtils.isNotEmpty(teacherAssignmentRecord.getAppInfo()) && SafeConverter.toInt(teacherAssignmentRecord.getAppInfo().get(groupId + "-" + bookId)) > 0,
                "author", SafeConverter.toString(readingOTOBook.getAuthor(), ""),
                "wordsCount", processWordsCount(SafeConverter.toDouble(readingOTOBook.getTotalNum())),
                "time", SafeConverter.toInt(readingOTOBook.getTimeUse()),
                "recommendDays", SafeConverter.toInt(readingOTOBook.getDayUse()),
                "description", SafeConverter.toString(readingOTOBook.getDescription(), "")
        );
        List<String> missionIds = readingOTOBook.getMissionIds();
        if (CollectionUtils.isNotEmpty(missionIds)) {
            Map<String, ReadingOTOMission> missionMap = stoneDataLoaderClient.loadReadingOTOMissionByIds(missionIds);
            missionMap = MapUtils.resort(missionMap, missionIds);
            if (MapUtils.isNotEmpty(missionMap)) {
                List<Map<String, Object>> guidanceMapperList = new ArrayList<>();
                List<Map<String, Object>> missionMapperList = new ArrayList<>();
                Set<String> niceExpressionSet = new LinkedHashSet<>();
                Set<String> allQuestionDocIds = missionMap.values()
                        .stream()
                        .map(ReadingOTOMission::getQuestionIds)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());
                Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionByDocIds0(allQuestionDocIds);
                for (Map.Entry<String, ReadingOTOMission> entry : missionMap.entrySet()) {
                    String missionId = entry.getKey();
                    ReadingOTOMission mission = entry.getValue();
                    guidanceMapperList.add(
                            MapUtils.m(
                                    "audio", SafeConverter.toString(mission.getLeadinAudio()),
                                    "title", SafeConverter.toString(mission.getLeadinAudioTitle()),
                                    "duration", SafeConverter.toInt(mission.getLeadinAudioDuration()))
                    );

                    List<String> questionIds = mission.getQuestionIds()
                            .stream()
                            .filter(questionMap::containsKey)
                            .map(questionMap::get)
                            .map(NewQuestion::getId)
                            .collect(Collectors.toList());
                    missionMapperList.add(
                            MapUtils.m(
                                    "missionId", missionId,
                                    "name", SafeConverter.toString(mission.getMissionName()),
                                    "description", SafeConverter.toString(mission.getDescription()),
                                    "questionIds", questionIds
                            )
                    );

                    if (mission.getNiceExpressions() != null && CollectionUtils.isNotEmpty(mission.getNiceExpressions().getExpressions())) {
                        for (Expression expression : mission.getNiceExpressions().getExpressions()) {
                            niceExpressionSet.add(expression.getExpression());
                        }
                    }
                }
                readingOTOBookDetailMapper.put("guidanceList", guidanceMapperList);
                readingOTOBookDetailMapper.put("missionList", missionMapperList);
                readingOTOBookDetailMapper.put("niceExpressionList", niceExpressionSet);
            }
        }
        return MapMessage.successMessage().add("bookDetail", readingOTOBookDetailMapper);
    }

    @Override
    public MapMessage confirm(Long teacherId, Long groupId) {
        List<OutsideReading> outsideReadingList = outsideReadingLoader.loadOutsideReadingByGroupId(groupId);
        long ongoingCount = outsideReadingList.stream()
                .filter(reading -> !reading.isTerminated())
                .count();
        return MapMessage.successMessage()
                .add("showToast", ongoingCount > 0)
                .add("ongoingCount", ongoingCount);
    }

    @Override
    public MapMessage assign(Long teacherId, Long groupId, String bookIds, Integer planDays, String endDate) {
        if (groupId == null || 0L == groupId) {
            return MapMessage.errorMessage("班组错误");
        }
        List<String> bookIdList = StringUtils.toList(bookIds, String.class);
        Map<String, ReadingOTOBook> bookMap = stoneDataLoaderClient.loadReadingOTOBookByIds(bookIdList);
        if (MapUtils.isEmpty(bookMap)) {
            return MapMessage.errorMessage("课本错误");
        }
        if (planDays == null || 0L == planDays) {
            return MapMessage.errorMessage("计划天数错误");
        }
        Date currentDate = new Date();
        Date endTime = DateUtils.stringToDate(endDate);
        if (endTime == null || endTime.before(currentDate)) {
            return MapMessage.errorMessage("结束时间错误");
        }
        List<OutsideReading> assignedOutsideReadingList = outsideReadingLoader.loadOutsideReadingByGroupId(groupId);
        if (CollectionUtils.isNotEmpty(assignedOutsideReadingList)) {
            Set<String> ongoingBookIdSet = assignedOutsideReadingList
                    .stream()
                    .filter(outsideReading -> !outsideReading.isTerminated())
                    .map(OutsideReading::findBookId)
                    .collect(Collectors.toSet());
            for (String bookId : bookIdList) {
                if (ongoingBookIdSet.contains(bookId)) {
                    return MapMessage.errorMessage("已有进行中的练习，请不要重复推荐");
                }
            }
        }
        String actionId = StringUtils.join(Arrays.asList(teacherId, currentDate.getTime()), "_");
        List<OutsideReading> outsideReadingList = new ArrayList<>();
        String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
        for (String bookId : bookIdList) {
            OutsideReading outsideReading = new OutsideReading();
            outsideReading.setId(month + "_" + RandomUtils.nextObjectId());
            outsideReading.setTeacherId(teacherId);
            outsideReading.setClazzGroupId(groupId);
            outsideReading.setSubject(Subject.CHINESE); //fixme 暂时只有语文
            outsideReading.setActionId(actionId);
            outsideReading.setEndTime(endTime);
            outsideReading.setPlanDays(planDays);
            outsideReading.setDisabled(false);
            outsideReading.setCreateAt(currentDate);
            outsideReading.setUpdateAt(currentDate);

            OutsideReadingPractice practice = new OutsideReadingPractice();
            ReadingOTOBook readingOTOBook = bookMap.get(bookId);
            if (readingOTOBook != null) {
                practice.setBookId(bookId);
                Map<String, ReadingOTOMission> missionMap = stoneDataLoaderClient.loadReadingOTOMissionByIds(readingOTOBook.getMissionIds());
                if (MapUtils.isNotEmpty(missionMap)) {
                    List<OutsideReadingMission> missionList = new ArrayList<>();
                    for (String missionId : readingOTOBook.getMissionIds()) {
                        ReadingOTOMission mission = missionMap.get(missionId);
                        if (mission != null) {
                            OutsideReadingMission outsideReadingMission = new OutsideReadingMission();
                            outsideReadingMission.setMissionId(missionId);
                            outsideReadingMission.setMissionName(mission.getMissionName());
                            List<String> questionIds = new ArrayList<>();
                            List<String> subjectiveQuestionIds = new ArrayList<>();
                            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionByDocIds0(mission.getQuestionIds());
                            if (MapUtils.isNotEmpty(questionMap)) {
                                for (NewQuestion newQuestion : questionMap.values()) {
                                    if (newQuestion.isSubjective()) {
                                        subjectiveQuestionIds.add(newQuestion.getId());
                                    } else {
                                        questionIds.add(newQuestion.getId());
                                    }
                                }
                            }
                            outsideReadingMission.setQuestionIds(questionIds);
                            outsideReadingMission.setSubjectiveQuestionIds(subjectiveQuestionIds);
                            missionList.add(outsideReadingMission);
                        }
                    }
                    practice.setMissions(missionList);
                    outsideReading.setPractices(practice);
                    outsideReadingList.add(outsideReading);
                } else {
                    return MapMessage.errorMessage("书本<<" + SafeConverter.toString(readingOTOBook.getBookName(), "") + ">>内容错误");
                }
            }
        }
        outsideReadingDao.inserts(outsideReadingList);
        if (CollectionUtils.isNotEmpty(outsideReadingList)) {
            updateAssignmentRecord(teacherId, outsideReadingList, groupId);
            toAvenger(outsideReadingList);
            assignOutsideReadingSendMobileNotification(teacherId, groupId);
            assignOutsideReadingPublishMessage(outsideReadingList, groupId, endTime);
        }
        changeIntegral(teacherId); // 增加园丁豆
        return MapMessage.successMessage("推荐成功");
    }

    /**
     * 增加园丁豆
     */
    private void changeIntegral(Long teacherId) {
        String comment = "老师布置语文课外阅读获得奖励";
        IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.PRIMARY_TEACHER_CHINESE_OTO, 50);
        String uniqueKey = StringUtils.join(Arrays.asList(IntegralType.PRIMARY_TEACHER_CHINESE_OTO.name(), teacherId), "-");
        integralHistory.setComment(comment);
        integralHistory.setUniqueKey(uniqueKey);
        userIntegralService.changeIntegral(integralHistory);
    }

    private void toAvenger(List<OutsideReading> outsideReadingList) {
        for (OutsideReading outsideReading : outsideReadingList) {
            asyncAvengerHomeworkService.informOutsideReadingToBigData(outsideReading);
        }
    }

    private void updateAssignmentRecord(Long userId, List<OutsideReading> outsideReadingList, Long groupId) {
        List<String> bookIds = outsideReadingList.stream().map(OutsideReading::findBookId).collect(Collectors.toList());
        List<String> teacherAssignmentBookIds = bookIds.stream().map(id -> groupId + "-" + id).collect(Collectors.toList());
        teacherAssignmentRecordDao.updateOutsideReadingTeacherAssignmentRecord(Subject.CHINESE, userId, teacherAssignmentBookIds);

        Set<String> packageSet = new HashSet<>(bookIds);
        totalAssignmentRecordDao.updateTotalAssignmentRecord(Subject.CHINESE, 1, null, packageSet, null);
    }

    private void assignOutsideReadingSendMobileNotification(Long teacherId, Long groupId) {
        AlpsThreadPool.getInstance().submit(() -> {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            String t = "h5";
            String content = teacher.fetchRealname() + "老师推荐了图书阅读，快去看看吧~";
            List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(groupId);
            appMessageServiceClient.sendAppJpushMessageByIds(
                    content,
                    AppMessageSource.STUDENT,
                    studentIds,
                    MiscUtils.m("s", StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getType(), "link", NewHomeworkConstants.STUDENT_OUTSIDE_READING_BOOKSHELF_URL, "t", t, "key", "j",
                            "title", StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getDescription()));
        });
    }

    private void assignOutsideReadingPublishMessage(List<OutsideReading> outsideReadings, Long groupId, Date endTime) {
        AlpsThreadPool.getInstance().submit(() -> {
            Set<String> bookIds = outsideReadings.stream().map(OutsideReading::findBookId).collect(Collectors.toSet());
            LinkedHashMap<String, ReadingOTOBook> bookMap = stoneDataLoaderClient.loadReadingOTOBookByIds(bookIds);
            List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(groupId);
            String title = "新阅读任务";
            String summary = "阅读图书\n";
            int i = 0;
            for (OutsideReading outsideReading : outsideReadings) {
                ReadingOTOBook readingOTOBook = bookMap.get(outsideReading.findBookId());
                if (readingOTOBook != null) {
                    summary = StringUtils.join(summary, "《", readingOTOBook.getBookName(), "》");
                    if (++i < 3) {
                        summary = StringUtils.join(summary, "\n");
                    } else {
                        summary = StringUtils.join(summary, "等\n");
                        break;
                    }
                }
            }
            String endDate = DateUtils.getDateTimeStrC(endTime);
            summary = StringUtils.join(summary, "截止时间:", endDate, "\n");
            List<AppMessage> result = new ArrayList<>();
            for (Long studentId : studentIds) {
                AppMessage appUserMessage = new AppMessage();
                appUserMessage.setUserId(studentId);
                appUserMessage.setTitle(title);
                appUserMessage.setContent(summary);
                appUserMessage.setLinkUrl(NewHomeworkConstants.STUDENT_OUTSIDE_READING_BOOKSHELF_URL);
                appUserMessage.setMessageType(StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getType());
                appUserMessage.setLinkType(1);
                result.add(appUserMessage);
            }
            result.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        });
    }


    @Override
    public MapMessage processResult(OutsideReadingContext outsideReadingContext) {
        try {
            OutsideReadingContext context = outsideReadingResultProcessor.process(outsideReadingContext);
            return context.transform().add("result", context.getResult());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @Override
    public MapMessage saveGoldenWords(Long userId, String outsideReadingId, String missionId, List<String> missionIndexes) {
        OutsideReading outsideReading = outsideReadingDao.load(outsideReadingId);
        if (outsideReading == null || outsideReading.findBookId() == null) {
            return MapMessage.errorMessage("阅读任务不存在");
        }
        NiceExpression niceExpression = outsideReadingLoader.fetchNiceExpressionByMissionId(missionId);
        if (niceExpression == null || CollectionUtils.isEmpty(niceExpression.getExpressions())) {
            return MapMessage.errorMessage("没有找到关卡对应的好词好句");
        }

        Map<String, Expression> expressionMap = new HashMap<>();
        List<Expression> expressions = niceExpression.getExpressions();
        for (int i = 0; i < expressions.size(); i++) {
            Expression expression = expressions.get(i);
            OutsideReadingCollection.ID id = new OutsideReadingCollection.ID(userId, outsideReading.findBookId(), missionId, i);
            expressionMap.put(id.toString(), expression);
        }

        Map<String, OutsideReadingCollection> missionAllCollectionsMap = outsideReadingCollectionDao.loads(expressionMap.keySet());

        Date currentTime = new Date();
        List<OutsideReadingCollection> insertCollections = new LinkedList<>();
        List<OutsideReadingCollection> updateCollections = new LinkedList<>();
        for (String missionIndex : missionIndexes) {
            if ("".equals(missionIndex)) {
                break;// 进这里说明没有提交任何一个好词好句
            }
            String collectionId = new OutsideReadingCollection.ID(userId, outsideReading.findBookId(), missionId, SafeConverter.toInt(missionIndex)).toString();
            OutsideReadingCollection readingCollection = new OutsideReadingCollection();
            readingCollection.setId(collectionId);
            readingCollection.setStudentId(userId);
            readingCollection.setBookId(outsideReading.findBookId());
            readingCollection.setMissionId(missionId);
            readingCollection.setUpdateAt(currentTime);
            readingCollection.setDisabled(false);
            Expression expression = expressionMap.get(collectionId);
            if (expression != null) {
                readingCollection.setLabels(expression.getTags());
                readingCollection.setGoldenWordsContent(expression.getExpression());

                OutsideReadingCollection collection = missionAllCollectionsMap.get(collectionId);
                // remove剩余为取消收藏的
                missionAllCollectionsMap.remove(collectionId);
                // 新添加收藏
                if (collection == null) {
                    readingCollection.setCreateAt(currentTime);
                    insertCollections.add(readingCollection);
                    continue;
                }
                // 之前收藏,本次不动的
                if (!collection.getDisabled()) {
                    continue;
                }
                // 取消收藏重新收藏的
                if (collection.getDisabled()) {
                    updateCollections.add(readingCollection);
                }
            }
        }
        // 之前收藏, 本次取消收藏的
        Map<String, OutsideReadingCollection> cancelReadingCollectionMap = Maps.filterValues(missionAllCollectionsMap, o -> o != null && !o.getDisabled());
        Integer addGoldenWordsCount = insertCollections.size() + updateCollections.size() - cancelReadingCollectionMap.size();
        cancelReadingCollectionMap.values().forEach(o -> {
            o.setDisabled(true);
            o.setUpdateAt(currentTime);
            updateCollections.add(o);
        });

        try {
            outsideReadingCollectionDao.inserts(insertCollections);
        } catch (MongoBulkWriteException | DuplicateKeyException ignored) {
        }
        updateCollections.forEach(o -> outsideReadingCollectionDao.upsert(o));
        outsideReadingAchievementDao.addGoldenWordsCount(userId, addGoldenWordsCount);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage modifyEndTime(String readingId, Date endTime) {
        OutsideReading outsideReading = outsideReadingDao.load(readingId);
        if (outsideReading == null) {
            return MapMessage.errorMessage("阅读任务不存在");
        }
        outsideReading.setEndTime(endTime);
        outsideReadingDao.upsert(outsideReading);
        return MapMessage.successMessage("修改成功");
    }

    @Override
    public MapMessage loadReadingReport(TeacherDetail teacherDetail, Map<Long, String> groupIdNameMap, String cdnUrl) {
        Map<Long, List<OutsideReading>> outsideReadingMap = outsideReadingDao.loadOutsideReadingByClazzGroupIds(groupIdNameMap.keySet());
        //获取每个班级一本未截止的图书, 没有未截止的则获取一个截止的
        List<OutsideReading> outsideReadings = new LinkedList<>();
        outsideReadingMap.forEach((groupId, readings) -> {
            if (CollectionUtils.isNotEmpty(readings)) {
                List<OutsideReading> readingList = readings;
                if (readings.stream().anyMatch(o -> !o.isTerminated())) {
                    readingList = readings.stream().filter(o -> !o.isTerminated()).collect(Collectors.toList());
                }
                outsideReadings.add(readingList.get(getIndexByDay(readingList.size())));
            }
        });

        List<String> bookIds = Lists.transform(outsideReadings, OutsideReading::findBookId);
        LinkedHashMap<String, ReadingOTOBook> bookMap = stoneDataLoaderClient.loadReadingOTOBookByIds(bookIds);
        Map<String, Double> readingFinishRateMap = outsideReadingLoader.fetchOutsideReadingFinishRate(outsideReadings, groupIdNameMap.keySet());

        List<Map<String, Object>> readingReportList = new ArrayList<>();
        for (OutsideReading outsideReading : outsideReadings) {
            String readingId = outsideReading.getId();
            OutsideReadingDynamicCacheManager cacheManager = newHomeworkCacheService.getOutsideReadingDynamicCacheManager();
            List<OutsideReadingDynamicCacheMapper> groupThisReadingDynamics = cacheManager.load(cacheManager.getCacheKey(readingId));
            List<Map<String, Object>> trends = new ArrayList<>(20);

            // 学生打卡动态(最近7天)
            if (CollectionUtils.isNotEmpty(groupThisReadingDynamics)) {
                List<OutsideReadingDynamicCacheMapper> weeksReadingDynamics = groupThisReadingDynamics.stream()
                        .filter(dynamic -> System.currentTimeMillis() - dynamic.getFinishAt().getTime() < DateUtils.WEEK_TIME_LENGTH_IN_MILLIS)
                        .collect(Collectors.toList());
                int toIndex = weeksReadingDynamics.size() > 20 ? 20 : weeksReadingDynamics.size();
                List<OutsideReadingDynamicCacheMapper> dynamicCacheMappers = weeksReadingDynamics.subList(0, toIndex);
                for (OutsideReadingDynamicCacheMapper dynamic : dynamicCacheMappers) {
                    String trend = StringUtils.join(dynamic.getStudentName(), "完成了", dynamic.getMissionName(), "的打卡");
                    if (dynamic.getAddReadingCount() > 0) {
                        trend = StringUtils.join(trend, "+", dynamic.getAddReadingCount(), "万字");
                    }
                    trends.add(MapUtils.m("icon", NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, dynamic.getStudentImage()),
                            "trend", trend));
                }
            }

            ReadingOTOBook readingOTOBook = bookMap.get(outsideReading.findBookId());
            readingReportList.add(MapUtils.map(
                    "groupId", outsideReading.getClazzGroupId(),
                    "readingId", readingId,
                    "progress", readingFinishRateMap.get(readingId),
                    "isEnd", outsideReading.isTerminated(),
                    "groupName", groupIdNameMap.get(outsideReading.getClazzGroupId()),
                    "coverImg", readingOTOBook != null ? readingOTOBook.getCoverPic() : "",
                    "trends", trends,
                    "bookId", outsideReading.findBookId()
            ));
        }
        return MapMessage.successMessage().add("readingReportList", readingReportList);
    }

    @Override
    public MapMessage crmDeleteOutsideReading(String readingId) {
        OutsideReading outsideReading = outsideReadingDao.load(readingId);
        if (outsideReading == null) {
            return MapMessage.errorMessage("阅读任务不存在");
        }
        outsideReading.setDisabled(true);
        outsideReadingDao.upsert(outsideReading);
        return MapMessage.successMessage("修改成功");
    }

    /**
     * 根据当前时间是一年中的第几天, 顺序获取0-size的整数
     *
     * @param size
     * @return
     */
    private int getIndexByDay(int size) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        return dayOfYear % size;
    }

    private String processWordsCount(double wordsCount) {
        if (wordsCount < 1) {
            return new BigDecimal(SafeConverter.toDouble(wordsCount * 10000)).intValue() + "";
        } else {
            return new BigDecimal(SafeConverter.toDouble(wordsCount)).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "万";
        }
    }
}
