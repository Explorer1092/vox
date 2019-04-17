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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.report;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.HomeworkSelfStudyRef;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.TeacherReportParameter;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.HomeworkSelfStudyRefDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkReportDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.OfflineHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.newhomework.impl.template.report.factory.ObjectiveConfigTypeProcessorFactory;
import com.voxlearning.utopia.service.newhomework.impl.template.report.template.ObjectiveConfigTypeProcessorTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.GenerateSelfStudyHomeworkConfigTypes;
import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.NeedSelfStudyHomeworkSubjects;

/**
 * @author tanguohong
 * @since 2016/1/20.
 */
@Named
public class HomeworkReportProcessor extends NewHomeworkSpringBean {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    @Inject
    private ObjectiveConfigTypeProcessorFactory objectiveConfigTypeProcessorFactory;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject
    private VoiceRecommendProcessor voiceRecommendProcessor;
    @Inject
    private OfflineHomeworkLoaderImpl offlineHomeworkLoader;
    @Inject
    private HomeworkSelfStudyRefDao homeworkSelfStudyRefDao;
    @Inject
    private SelfStudyHomeworkReportDao selfStudyHomeworkReportDao;
    @Inject
    private NewHomeworkResultServiceImpl newHomeworkResultService;

    @Inject private RaikouSDK raikouSDK;

    /**
     * 学生作业历史列表
     */
    public Page<DisplayStudentHomeWorkHistoryMapper> loadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate, Pageable pageable) {

        if (student == null || student.getClazz() == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        Long studentId = student.getId();
        List<Group> groupMappers = raikouSystem.loadStudentGroups(studentId);
        Set<Long> groupIds = new HashSet<>();
        Map<Long, Group> groupMapperMap = new HashMap<>();
        // 学生所在分组
        for (Group group : groupMappers) {
            groupIds.add(group.getId());
            groupMapperMap.put(group.getId(), group);

        }

        Page<NewHomework.Location> newHomeworkPage = newHomeworkLoader.loadGroupNewHomeworks(groupIds, startDate, endDate, pageable);
        if (newHomeworkPage == null || CollectionUtils.isEmpty(newHomeworkPage.getContent())) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<DisplayStudentHomeWorkHistoryMapper> mappers = buildDisplayStudentHomeWorkHistoryMapper(student, newHomeworkPage.getContent(), groupMapperMap, startDate, endDate);
        return new PageImpl<>(mappers, pageable, newHomeworkPage.getTotalElements());

    }

    private List<DisplayStudentHomeWorkHistoryMapper> buildDisplayStudentHomeWorkHistoryMapper(StudentDetail student,
                                                                                               List<NewHomework.Location> groupHomeworks,
                                                                                               Map<Long, Group> groupMapperMap,
                                                                                               Date startDate,
                                                                                               Date endDate) {
        List<NewHomework.Location> locations = new ArrayList<>();
        // 读取作业
        Map<String, String> homeworkResultIdMap = new HashMap<>();
        for (NewHomework.Location location : groupHomeworks) {
            if (location.getCreateTime() > startDate.getTime() && location.getCreateTime() <= endDate.getTime()) {
                String day = DayRange.newInstance(location.getCreateTime()).toString();
                Subject subject = location.getSubject();
                Long groupId = location.getClazzGroupId();
                NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, subject, location.getId(), student.getId().toString());
                homeworkResultIdMap.put(location.getId(), id.toString());
                Group group = groupMapperMap.get(groupId);
                //当组学科和作业学科不匹配则过滤掉（因为老师换科会导致当前组学科和之前布置作业学科不一致）
                if (group != null && !group.getSubject().equals(subject)) {
                    continue;
                }
                locations.add(location);
            }
        }

        locations.sort((h1, h2) -> Long.compare(h2.getCreateTime(), h1.getCreateTime()));

        //作业课本单元信息
        Map<String, NewHomeworkBook> newHomeworkBookInfoMap = newHomeworkLoader.loadNewHomeworkBooks(homeworkResultIdMap.keySet());
        // 纸质作业，英语单词听写，需要展示Lesson的中文名
        Map<String, Set<String>> newHomeworkLessonIdMap = new HashMap<>();
        Set<String> allLessonIds = new HashSet<>();
        for (NewHomeworkBook newHomeworkBook : newHomeworkBookInfoMap.values()) {
            LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practices = newHomeworkBook.getPractices();
            if (MapUtils.isEmpty(practices)) {
                continue;
            }
            Set<String> lessonIds = new HashSet<>();
            for (ObjectiveConfigType objectiveConfigType : practices.keySet()) {
                if (!ObjectiveConfigType.OCR_DICTATION.equals(objectiveConfigType)) {
                    continue;
                }
                for (NewHomeworkBookInfo bookInfo : practices.get(objectiveConfigType)) {
                    lessonIds.add(bookInfo.getLessonId());
                }
            }
            newHomeworkLessonIdMap.put(newHomeworkBook.getId(), lessonIds);
            allLessonIds.addAll(lessonIds);
        }
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(allLessonIds);

        Map<String, List<NewBookCatalog>> newHomeworkBookCatalogMap = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : newHomeworkLessonIdMap.entrySet()) {
            Set<String> lessonIds = entry.getValue();
            List<NewBookCatalog> newBookCatalogs = new LinkedList<>();
            for (String lessonId : lessonIds) {
                if (newBookCatalogMap.get(lessonId) == null) {
                    continue;
                }
                newBookCatalogs.add(newBookCatalogMap.get(lessonId));
            }
            newHomeworkBookCatalogMap.put(entry.getKey(), newBookCatalogs);
        }

        //读取作业结果
        Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loads(homeworkResultIdMap.values(), false);

//        Map<String, CorrectQuestionResult> correctQuestionResultMap = correctQuestionResultDao.loads(homeworkResultIdMap.values());
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loads(homeworkResultIdMap.keySet());

        //查询订正状态
        final Map<String, HomeworkCorrectStatus> homeworkCorrectStatusMap = newHomeworkResultService.fetchHomeworkCorrectStatus(new ArrayList<>(homeworkResultIdMap.values()));
        // 生成结果
        List<DisplayStudentHomeWorkHistoryMapper> mappers = new LinkedList<>();
        for (NewHomework.Location location : locations) {
            String homeworkId = location.getId();
            NewHomework newHomework = newHomeworkMap.get(homeworkId);
            if (newHomework == null) continue;
            NewHomeworkResult homeworkResult = newHomeworkResultMap.get(homeworkResultIdMap.get(homeworkId));
            DisplayStudentHomeWorkHistoryMapper mapper = new DisplayStudentHomeWorkHistoryMapper();
            mapper.setHomeworkId(newHomework.getId());
            mapper.setCreateTime(newHomework.getCreateAt().getTime());
            mapper.setStartDate(DateUtils.dateToString(newHomework.getStartTime(), "MM月dd日"));
            mapper.setEndDate(DateUtils.dateToString(newHomework.getEndTime(), "MM月dd日"));
            mapper.setChecked(newHomework.isHomeworkChecked());
            mapper.setHomeworkTerminated(newHomework.isHomeworkTerminated());
            mapper.setSubject(newHomework.getSubject());
            mapper.setHomeworkType(newHomework.getType() != null ? newHomework.getType().name() : NewHomeworkType.Normal.name());
            List<String> types = newHomework.getPractices().stream().map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name()).collect(Collectors.toList());
            mapper.setTypes(types);
            Set<String> bookNames = new HashSet<>();
            Set<String> unitNames = new HashSet<>();
            NewHomeworkBook newHomeworkBook = newHomeworkBookInfoMap.get(homeworkId);
            if (newHomeworkBook != null) {
                bookNames = newHomeworkBook.processBookNameList();
                unitNames = newHomeworkBook.processUnitNameList();
            }
            mapper.setBookName(StringUtils.join(bookNames, ","));
            mapper.setUnitNames(StringUtils.join(unitNames, ","));
            if (NewHomeworkType.OCR.equals(location.getType())) {
                if (Subject.MATH.equals(location.getSubject())) {
                    NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
                    String workBookName = newHomeworkPracticeContent.getWorkBookName();
                    String homeworkDetail = newHomeworkPracticeContent.getHomeworkDetail();
                    List<String> bookNameList = Arrays.asList(StringUtils.split(workBookName, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                    List<String> homeworkDetailList = Arrays.asList(StringUtils.split(homeworkDetail, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                    int length = Integer.max(bookNameList.size(), homeworkDetailList.size());
                    List<String> contentList = new ArrayList<>();
                    for (int i = 0; i < length; i++) {
                        String content = bookNameList.get(i) + ":" + homeworkDetailList.get(i);
                        contentList.add(content);
                    }
                    mapper.setUnitNames(StringUtils.join(contentList, ";"));
                } else if (Subject.ENGLISH.equals(location.getSubject())) {
                    List<NewBookCatalog> newBookCatalogs = newHomeworkBookCatalogMap.get(homeworkId);
                    if (CollectionUtils.isNotEmpty(newBookCatalogs)) {
                        unitNames = newBookCatalogs.stream().map(NewBookCatalog::getAlias).collect(Collectors.toSet());
                    }
                    mapper.setUnitNames(StringUtils.join(unitNames, ";"));
                }
            }

            Integer avgScore = null;
            boolean homeworkFinished = false;
            if (homeworkResult != null) {
                mapper.setNote(homeworkResult.getComment());
                if (homeworkResult.isFinished()) {
                    mapper.setSubmitTime(DateUtils.dateToString(homeworkResult.getFinishAt()));
                }
                homeworkFinished = homeworkResult.getFinishAt() != null;
                boolean showScore = newHomework.getPractices()
                        .stream()
                        .map(NewHomeworkPracticeContent::getType)
                        .anyMatch(o -> !NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(o));
                if (homeworkFinished && showScore) avgScore = homeworkResult.processScore();
            }
            if (homeworkFinished && avgScore == null) {
                if (homeworkResult.isCorrected()) {
                    mapper.setCorrectedType("已完成");
                } else {
                    mapper.setCorrectedType("未批改");
                }
            }
            mapper.setFinished(homeworkFinished);
            // 1.开始作业2，学生并没有做完 && 未检查 && 在可做范围内
            // 2.补做作业1，学生并没有做完 && (作业检查完||作业过期)
            // 3.查看作业0，学生做完
            // 4.禁止作业3，未完成 && 创建时间早于ALLOW_UPDATE_HOMEWORK_START_TIME
            if (!homeworkFinished) {
                if (newHomework.getCreateAt().before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
                    mapper.setState("3");
                } else if (newHomework.isHomeworkTerminated()) {
                    mapper.setState("1");
                } else {
                    mapper.setState("2");
                }
            } else {
                mapper.setState("0");
            }
            mapper.setHomeworkScore(avgScore);
            // 是否显示口算训练榜单
            List<NewHomeworkPracticeContent> practices = newHomework.getPractices();
            if (CollectionUtils.isNotEmpty(practices)) {
                for (NewHomeworkPracticeContent practiceContent : practices) {
                    if (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(practiceContent.getType())
                            && practiceContent.getTimeLimit() != null
                            && practiceContent.getTimeLimit().getTime() != 0) {
                        mapper.setTimeLimit(true);
                        break;
                    }
                }
            }

            HomeworkCorrectStatus homeworkCorrectStatus = HomeworkCorrectStatus.WITHOUT_CORRECT;
            if (homeworkResult != null) {
                homeworkCorrectStatus = homeworkCorrectStatusMap.get(homeworkResult.getId()) == null ? homeworkCorrectStatus : homeworkCorrectStatusMap.get(homeworkResult.getId());
            }
            mapper.setHCorrectStatus(homeworkCorrectStatus);
            mappers.add(mapper);
        }
        return mappers;
    }

    /**
     * 学生作业历史列表
     */
    public List<DisplayStudentHomeWorkHistoryMapper> loadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate) {
        if (student == null || student.getClazz() == null) {
            return Collections.emptyList();
        }
        Long studentId = student.getId();
        List<Group> groupMappers = raikouSystem.loadStudentGroups(studentId);
        Set<Long> groupIds = new HashSet<>();
        Map<Long, Group> groupMapperMap = new HashMap<>();
        // 学生所在分组
        for (Group group : groupMappers) {
            groupIds.add(group.getId());
            groupMapperMap.put(group.getId(), group);
        }

        List<NewHomework.Location> groupHomeworks = newHomeworkLoader.loadNewHomeworksByClazzGroupIds(groupIds)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return buildDisplayStudentHomeWorkHistoryMapper(student, groupHomeworks, groupMapperMap, startDate, endDate);
    }


    /**
     * 学生作业历史详情
     *
     * @param homeworkId 作业ID
     * @param studentId  学生Id
     * @return 学生作业历史详情
     */
    public MapMessage loadStudentNewHomeworkHistoryDetail(String homeworkId, Long studentId) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在").add("result", Collections.emptyMap());
        }
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, newHomework.getSubject(), newHomework.getId(), studentId.toString());
        NewHomeworkResult nr = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());

        //取学生答题过程数据
        List<String> newHomeworkProcessResultIds = nr != null && nr.getPractices() != null ? nr.findAllHomeworkProcessIds(false) : new ArrayList<>();

        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(homeworkId, newHomeworkProcessResultIds);
        Map<String, Object> obj = new HashMap<>();
        obj.put("teacherId", newHomework.getTeacherId());
        obj.put("createDate", DateUtils.dateToString(newHomework.getStartTime(), DateUtils.FORMAT_SQL_DATETIME));
        obj.put("userId", studentId);
        obj.put("comment", nr != null ? nr.getComment() : "");
        List<Map> objectiveConfigTypes = new ArrayList<>();
        for (NewHomeworkPracticeContent newHomeworkPracticeContent : newHomework.getPractices()) {
            ObjectiveConfigType objectiveConfigType = newHomeworkPracticeContent.getType();
            NewHomeworkResultAnswer nra = (nr != null && nr.getPractices() != null ? nr.getPractices().get(objectiveConfigType) : null);
            if (nra != null) {

                List<String> corrections = new ArrayList<>();
                if (ObjectiveConfigType.getSubjectives().contains(objectiveConfigType)) {
                    for (String resultId : nra.processAnswers().values()) {
                        NewHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(resultId);
                        if (newHomeworkProcessResult != null && newHomeworkProcessResult.getCorrection() != null) {
                            corrections.add(newHomeworkProcessResult.getCorrection().getDescription());
                        } else {
                            corrections.add(newHomeworkProcessResult == null || newHomeworkProcessResult.getReview() != null ? "阅" : "未批改");
                        }
                    }
                }
                objectiveConfigTypes.add(
                        MapUtils.m(
                                "objectiveConfigType", objectiveConfigType,
                                "isSubjective", objectiveConfigType.isSubjective(),
                                "objectiveConfigTypeName", objectiveConfigType.getValue(),
                                "score", nra.processScore(objectiveConfigType),
                                "finished", nra.isFinished(),
                                "corrected", nra.isCorrected(),
                                "corrections", StringUtils.join(corrections, ",")
                        ));

            } else {
                objectiveConfigTypes.add(
                        MapUtils.m(
                                "objectiveConfigType", objectiveConfigType,
                                "isSubjective", objectiveConfigType.isSubjective(),
                                "objectiveConfigTypeName", objectiveConfigType.getValue(),
                                "score", null,
                                "finished", false,
                                "corrected", false,
                                "corrections", null

                        ));
            }
        }

        Integer avgScore = null;
        if (nr != null && nr.isFinished()) {
            avgScore = nr.processScore();
        }
        obj.put("avgScore", avgScore);
        obj.put("objectiveConfigTypes", objectiveConfigTypes);
        return MapMessage.successMessage().add("result", obj);
    }

    /**
     * 获取老师未检查作业列表
     */
    public List<Map<String, Object>> loadTeacherUncheckedHomeworkList(Teacher teacher) {
        try {
            // 主副账号支持
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());

            // 老师分班
            Map<Long, List<Clazz>> teacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(relTeacherIds);
            List<Long> isTerminalClazzIds = new ArrayList<>();
            teacherClazzs.forEach((tid, clazzs) -> clazzs.forEach(clazz -> {
                if (clazz.isTerminalClazz()) {
                    isTerminalClazzIds.add(clazz.getId());
                }
            }));

            // 老师分组
            Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(relTeacherIds, false);

            // group id list
            Set<Long> groupIds = new HashSet<>();
            // subject -> group id list
            Map<Subject, Set<Long>> subjectGroupIds = new HashMap<>();

            teacherGroups.forEach((tid, groups) ->
                    groups.forEach(group -> {
                        //过滤毕业班
                        //过滤不教了的组
                        if (!isTerminalClazzIds.contains(group.getClazzId()) &&
                                group.isTeacherGroupRefStatusValid(tid)) {
                            groupIds.add(group.getId());
                            Set<Long> gids = subjectGroupIds.computeIfAbsent(group.getSubject(), k -> new HashSet<>());
                            gids.add(group.getId());
                        }
                    }));

            // 读取分组信息，包括分组学生信息
            Map<Long, GroupMapper> groupMappers = groupLoaderClient.loadGroups(groupIds, true);

            // 按分组读取所有未检查作业，并按时间排序
//            SortedSet<NewHomework> homeworkList = new TreeSet<>((o1, o2) -> Long.compare(o2.getCreateAt().getTime(), o1.getCreateAt().getTime()));
            List<NewHomework> homeworkList = new ArrayList<>();
            subjectGroupIds.forEach((subject, gids) -> homeworkList.addAll(newHomeworkLoader.loadGroupHomeworks(gids, subject).unchecked().toList()));
            homeworkList.sort((o1, o2) -> Long.compare(o2.getCreateAt().getTime(), o1.getCreateAt().getTime()));
            return buildHomeworkReportMapper(homeworkList, groupMappers);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    /**
     * PC端作业列表
     *
     * @param groupIds 班组ID
     * @param pageable page
     * @param subject  subject
     * @return 作业列表
     */
    public Page<Map<String, Object>> pageHomeworkReportListByGroupIds(Collection<Long> groupIds, Pageable pageable, Subject subject) {
        try {
            if (CollectionUtils.isEmpty(groupIds) || pageable == null || subject == null) {
                return new PageImpl<>(Collections.emptyList());
            }
            // 按分组读取所有作业
            Map<Long, GroupMapper> groupMappers = groupLoaderClient.loadGroups(groupIds, true);
            Page<NewHomework> homeworkPage = newHomeworkLoader.loadGroupHomeworks(groupIds, subject)
                    .filter(h -> NewHomeworkType.OCR != h.getType())
                    .sorted((o1, o2) -> {
                        boolean checked1 = o1.isChecked();
                        boolean checked2 = o2.isChecked();
                        int ret = Boolean.compare(checked1, checked2);
                        if (ret != 0) return ret;
                        long createAt1 = o1.getCreateTime();
                        long createAt2 = o2.getCreateTime();
                        return Long.compare(createAt2, createAt1);
                    })
                    .toPage(pageable);
            List<NewHomework> homeworkList = homeworkPage.getContent();
            List<Map<String, Object>> results = buildHomeworkReportMapper(homeworkList, groupMappers);
            return new PageImpl<>(results, pageable, homeworkPage.getTotalElements());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new PageImpl<>(Collections.emptyList());
        }

    }

    /**
     * APP端作业列表
     *
     * @param groupIds 组Id
     * @param pageable page
     * @param subject  作业
     * @return 作业详情
     */
    public Page<Map<String, Object>> pageHomeworkReportListByGroupIdsAndHomeworkStatus(Collection<Long> groupIds, Pageable pageable, Subject subject, HomeworkStatus homeworkStatus) {
        return pageHomeworkReportListByGroupIdsAndHomeworkStatus(groupIds, pageable, Collections.singleton(subject), homeworkStatus);
    }

    public Page<Map<String, Object>> pageHomeworkReportListByGroupIdsAndHomeworkStatus(Collection<Long> groupIds, Pageable pageable, Collection<Subject> subjects, HomeworkStatus homeworkStatus) {
        try {
            if (CollectionUtils.isEmpty(groupIds) || pageable == null || CollectionUtils.isEmpty(subjects)) {
                return new PageImpl<>(Collections.emptyList());
            }
            // 按分组读取所有作业
            Map<Long, GroupMapper> groupMappers = groupLoaderClient.loadGroups(groupIds, true);
            Page<NewHomework> homeworkPage;
            if (homeworkStatus == null) {
                homeworkPage = newHomeworkLoader
                        .loadGroupHomeworks(groupIds, subjects)
                        .sorted((o1, o2) -> {
                            int ret = Boolean.compare(o1.isChecked(), o2.isChecked());
                            if (ret != 0) return ret;
                            return Long.compare(o2.getCreateTime(), o1.getCreateTime());
                        })
                        .toPage(pageable);
            } else {
                homeworkPage = newHomeworkLoader
                        .loadGroupHomeworks(groupIds, subjects)
                        .filter(homework -> (homeworkStatus == HomeworkStatus.Checked && homework.isChecked())
                                || (homeworkStatus == HomeworkStatus.UnChecked && !homework.isChecked()))
                        .sorted((o1, o2) -> {
                            int ret = Boolean.compare(o1.isChecked(), o2.isChecked());
                            if (ret != 0) return ret;
                            return Long.compare(o2.getCreateTime(), o1.getCreateTime());
                        })
                        .toPage(pageable);
            }
            List<NewHomework> newHomeworkList = homeworkPage.getContent();
            List<Map<String, Object>> results = buildHomeworkReportMapper(newHomeworkList, groupMappers);
            return new PageImpl<>(results, pageable, homeworkPage.getTotalElements());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new PageImpl<>(Collections.emptyList());
        }

    }

    // <HomeworkId, List<NewHomeworkResult>>
    private Map<String, List<NewHomeworkResult>> findNewHomeworkResults(Collection<NewHomework> newHomeworkList, Map<Long, GroupMapper> groupMappers) {
        Map<String, List<NewHomeworkResult>> resultMap = new HashMap<>();
        Map<String, Set<NewHomeworkResult>> newHomeworkResultMap = newHomeworkResultLoader.findByHomeworksForReport(newHomeworkList);
        for (NewHomework newHomework : newHomeworkList) {
            GroupMapper groupMapper = groupMappers.getOrDefault(newHomework.getClazzGroupId(), null);
            if (groupMapper != null) {
                Set<Long> studentIds = groupMapper
                        .getStudents()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(GroupMapper.GroupUser::getId)
                        .collect(Collectors.toSet());

                Set<NewHomeworkResult> newHomeworkResultSet = newHomeworkResultMap.getOrDefault(newHomework.getId(), Collections.emptySet())
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(o -> studentIds.contains(o.getUserId()))
                        .collect(Collectors.toSet());

                resultMap.put(newHomework.getId(), new ArrayList<>(newHomeworkResultSet));
            }
        }
        return resultMap;
    }

    /**
     * 注意：这里的groupMappers需要带有学生信息
     */
    private List<Map<String, Object>> buildHomeworkReportMapper(Collection<NewHomework> newHomeworkList, Map<Long, GroupMapper> groupMappers) {
        if (CollectionUtils.isEmpty(newHomeworkList)) {
            return Collections.emptyList();
        }
        List<String> newHomeworkIds = newHomeworkList
                .stream()
                .map(NewHomework::getId)
                .collect(Collectors.toList());
        List<Long> clazzIds = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, List<NewHomeworkResult>> newHomeworkResultMap = findNewHomeworkResults(newHomeworkList, groupMappers);
        Map<String, OfflineHomework> offlineHomeworkMap = offlineHomeworkLoader.loadByNewHomeworkIds(newHomeworkIds);
        for (NewHomework newHomework : newHomeworkList) {
            int finishedCount = 0;
            int correctedCount = 0;
            if (newHomeworkResultMap.getOrDefault(newHomework.getId(), null) != null) {
                for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.get(newHomework.getId())) {
                    if (newHomeworkResult.getFinishAt() != null) {
                        finishedCount++;
                        if (newHomeworkResult.isCorrected()) {
                            correctedCount++;
                        }
                    }
                }
            }
            GroupMapper groupMapper = groupMappers.get(newHomework.getClazzGroupId());
            int userCount = groupMapper != null ? groupMapper.getStudents().size() : 0;
            Long clazzId = groupMapper == null ? null : groupMapper.getClazzId();
            OfflineHomework offlineHomework = offlineHomeworkMap.get(newHomework.getId());
            String homeworkName = DateUtils.dateToString(newHomework.getCreateAt(), "MM.dd");
            boolean isTermReview = false;
            if (Objects.equals(newHomework.getType(), NewHomeworkType.TermReview)) {
                isTermReview = true;
            }
            boolean hasRecommend = false;
            if (newHomework.getRemindCorrection() != null) {
                hasRecommend = SafeConverter.toBoolean(newHomework.getRemindCorrection());
            }
            boolean includeIntelligentTeaching = false;
            if (newHomework.getIncludeIntelligentTeaching() != null) {
                includeIntelligentTeaching = newHomework.getIncludeIntelligentTeaching();
            }
            String recommendUrl = includeIntelligentTeaching && newHomework.isHomeworkChecked() && newHomework.getCreateAt().after(NewHomeworkConstants.REMIND_CORRECTION_START_DATE) ?
                    UrlUtils.buildUrlQuery("/view/mobile/teacher/junior/similarrecommend/individualization_consolidate.vpage", MapUtils.m("homeworkIds", newHomework.getId(), "hasRecommend", hasRecommend)) : null;
            String correctUrl = newHomework.getIncludeSubjective() && correctedCount < finishedCount ? UrlUtils.buildUrlQuery("/view/report/correct",
                    MapUtils.m(
                            "homeworkId", newHomework.getId(),
                            "subject", newHomework.getSubject(),
                            "homeworkType", newHomework.getNewHomeworkType()
                    )) : null;
            //纸质作业查看作业单
            String ocrHomeworkDetailUrl = NewHomeworkType.OCR.equals(newHomework.getNewHomeworkType()) ? UrlUtils.buildUrlQuery("/view/mobile/teacher/junior/ocrhomework/homework_card.vpage",
                    MapUtils.m("homeworkIds", newHomework.getId(),
                            "from", "report"
                    )) : null;
            //作业单ID
            String offlineHomeworkId = offlineHomework != null ? offlineHomework.getId() : null;
            if (NewHomeworkType.OCR.equals(newHomework.getNewHomeworkType()) && StringUtils.isBlank(offlineHomeworkId)) {
                offlineHomeworkId = NewHomeworkConstants.OCR_HOMEWORK_DETAIL_DEFAULT_ID;
            }

            results.add(MapUtils.m(
                    "isTermReview", isTermReview,
                    "homeworkId", newHomework.getId(),
                    "homeworkName", homeworkName,
                    "homeworkType", newHomework.getNewHomeworkType(),
                    "homeworkTag", newHomework.getHomeworkTag(),
                    "clazzId", clazzId,
                    "groupId", newHomework.getClazzGroupId(),
                    "endTime", DateUtils.dateToString(newHomework.getEndTime(), DateUtils.FORMAT_SQL_DATETIME),
                    "startTime", DateUtils.dateToString(newHomework.getStartTime(), DateUtils.FORMAT_SQL_DATETIME),
                    "checked", newHomework.isHomeworkChecked(),
                    "createAt", newHomework.getCreateAt(),
                    "includeSubjective", newHomework.getIncludeSubjective(),
                    "showCheck", !newHomework.isHomeworkChecked()
                            && (DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE).equals(DateUtils.dateToString(newHomework.getEndTime(), DateUtils.FORMAT_SQL_DATE))
                            || System.currentTimeMillis() > newHomework.getEndTime().getTime()
                            || finishedCount >= userCount),//显示检查按钮的条件
                    "showDelete", !newHomework.isHomeworkTerminated() && finishedCount < userCount, // 显示删除按钮的条件
                    "showAdjust", !newHomework.isHomeworkTerminated() && finishedCount < userCount, // 显示调整按钮的条件
                    "finishedCount", finishedCount,
                    "correctUrl", correctUrl,
                    "correctedCount", correctedCount,
                    "terminated", System.currentTimeMillis() > newHomework.getEndTime().getTime(),
                    "userCount", userCount,
                    "subject", newHomework.getSubject(),
                    "recommendUrl", recommendUrl,
                    "recommendTitle", hasRecommend ? "已推荐巩固" : "推荐巩固",
                    "offlineHomeworkId", offlineHomeworkId,
                    "ocrHomeworkDetailUrl", ocrHomeworkDetailUrl,
                    // 学前|纸质 不显示发送作业单
                    "showAssignOffline", SchoolLevel.INFANT != newHomework.getSchoolLevel()
                            && NewHomeworkType.MothersDay != newHomework.getType()
                            && NewHomeworkType.Activity != newHomework.getType()
                            && NewHomeworkType.OCR != newHomework.getType()
                            && offlineHomework == null
                            && !newHomework.isHomeworkTerminated()
            ));
            clazzIds.add(clazzId);
        }

        Map<String, NewHomeworkBook> newHomeworkBookMap = newHomeworkLoader.loadNewHomeworkBooks(newHomeworkIds);
        Map<String, NewHomework> newHomeworkMap = newHomeworkList.stream().collect(Collectors.toMap(NewHomework::getId, Function.identity()));
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        // 纸质作业，英语单词听写，需要展示Lesson的中文名
        Map<String, Set<String>> newHomeworkLessonIdMap = new HashMap<>();
        Set<String> allLessonIds = new HashSet<>();
        for (NewHomeworkBook newHomeworkBook : newHomeworkBookMap.values()) {
            LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practices = newHomeworkBook.getPractices();
            if (MapUtils.isEmpty(practices)) {
                continue;
            }
            Set<String> lessonIds = new HashSet<>();
            for (ObjectiveConfigType objectiveConfigType : practices.keySet()) {
                if (!ObjectiveConfigType.OCR_DICTATION.equals(objectiveConfigType)) {
                    continue;
                }
                for (NewHomeworkBookInfo bookInfo : practices.get(objectiveConfigType)) {
                    lessonIds.add(bookInfo.getLessonId());
                }
            }
            newHomeworkLessonIdMap.put(newHomeworkBook.getId(), lessonIds);
            allLessonIds.addAll(lessonIds);
        }
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(allLessonIds);
        Map<String, List<NewBookCatalog>> newHomeworkBookCatalogMap = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : newHomeworkLessonIdMap.entrySet()) {
            Set<String> lessonIds = entry.getValue();
            List<NewBookCatalog> newBookCatalogs = new LinkedList<>();
            for (String lessonId : lessonIds) {
                if (newBookCatalogMap.get(lessonId) == null) {
                    continue;
                }
                newBookCatalogs.add(newBookCatalogMap.get(lessonId));
            }
            newHomeworkBookCatalogMap.put(entry.getKey(), newBookCatalogs);
        }


        for (Map<String, Object> result : results) {
            String homeworkId = SafeConverter.toString(result.get("homeworkId"));
            NewHomeworkBook newHomeworkBook = newHomeworkBookMap.get(homeworkId);
            NewHomework newHomework = newHomeworkMap.get(homeworkId);
            Set<String> unitNames = new LinkedHashSet<>();
            if (newHomeworkBook != null) {
                unitNames = newHomeworkBook.processUnitNameList();
            }
            Clazz clazz = clazzMap.get(SafeConverter.toLong(result.get("clazzId")));
            result.put("clazzName", clazz != null ? clazz.formalizeClazzName() : "");
            String content = StringUtils.join(unitNames, ",");
            if (StringUtils.isBlank(content) && newHomework != null && NewHomeworkType.Activity == newHomework.getType() && HomeworkTag.KidsDay == newHomework.getHomeworkTag()) {
                content = "儿童节趣味配音";
            }
            if (NewHomeworkType.OCR.equals(newHomework.getType())) {
                if (Subject.MATH.equals(newHomework.getSubject())) {
                    NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
                    String workBookName = newHomeworkPracticeContent.getWorkBookName();
                    String homeworkDetail = newHomeworkPracticeContent.getHomeworkDetail();
                    List<String> bookNameList = Arrays.asList(StringUtils.split(workBookName, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                    List<String> homeworkDetailList = Arrays.asList(StringUtils.split(homeworkDetail, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                    int length = Integer.max(bookNameList.size(), homeworkDetailList.size());
                    List<String> contentList = new ArrayList<>();
                    for (int i = 0; i < length; i++) {
                        String contentDetail = bookNameList.get(i) + ":" + homeworkDetailList.get(i);
                        contentList.add(contentDetail);
                    }
                    content = StringUtils.join(contentList, ";");
                } else if (Subject.ENGLISH.equals(newHomework.getSubject())) {
                    List<NewBookCatalog> newBookCatalogs = newHomeworkBookCatalogMap.get(homeworkId);
                    if (CollectionUtils.isNotEmpty(newBookCatalogs)) {
                        unitNames = newBookCatalogs
                                .stream()
                                .filter(b -> StringUtils.isNotBlank(b.getAlias()))
                                .map(NewBookCatalog::getAlias)
                                .collect(Collectors.toSet());
                    }
                    content = StringUtils.join(unitNames, ";");
                }
            }
            result.put("content", content);
        }
        return results;
    }

    /**
     * 作业详情首页
     *
     * @param teacher    老师账号
     * @param homeworkId 作业ID
     * @return 作业详情
     */
    public MapMessage reportDetailIndex(Teacher teacher, String homeworkId) {
        try {
            NewHomework newHomework = newHomeworkLoader.loadNewHomework(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }

            // 权限检查
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有查看权限");
            }

            MapMessage mapMessage = MapMessage.successMessage();
            GroupMapper group = groupLoaderClient.loadGroup(newHomework.getClazzGroupId(), true);
            return mapMessage.add("homeworkInfo",
                    MapUtils.m(
                            "homeworkType", newHomework.getNewHomeworkType(),
                            "clazzId", group == null ? null : group.getClazzId(),
                            "includeSubjective", newHomework.getIncludeSubjective(),
                            "subject", newHomework.getSubject()
                    ));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage().setInfo(ex.getMessage());
        }
    }

    public MapMessage loadEnglishHomeworkVoiceList(String homeworkId) {
        NewHomework newHomework = newHomeworkLoader.loadNewHomework(homeworkId);
        if (newHomework == null || Subject.ENGLISH != newHomework.getSubject()) {
            return MapMessage.errorMessage("无效的作业id");
        }
        if (!newHomework.isHomeworkChecked()) {
            return MapMessage.errorMessage("作业未检查，无法推荐录音");
        }

        Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.findByHomeworkForReport(newHomework);

        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(User::getId, Function.identity()));
        return voiceRecommendProcessor.loadAllVoices(userMap, newHomework, newHomeworkResultMap.values());
    }

    public MapMessage loadDubbingWithScoreVoiceList(String homeworkId) {
        NewHomework newHomework = newHomeworkLoader.loadNewHomework(homeworkId);
        if (newHomework == null || Subject.ENGLISH != newHomework.getSubject()) {
            return MapMessage.errorMessage("无效的作业id");
        }
        if (!newHomework.isHomeworkChecked()) {
            return MapMessage.errorMessage("作业未检查，无法推荐录音");
        }

        Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.findByHomeworkForReport(newHomework);

        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(User::getId, Function.identity()));
        List<BaseVoiceRecommend.DubbingWithScore> excellentDubbingStu = dubbingWithScoreRecommendProcessor.loadAllDubbing(userMap, newHomework, newHomeworkResultMap.values());
        return MapMessage.successMessage().add("excellentDubbingStu", excellentDubbingStu);
    }

    public List<Map<String, Object>> homeworkReportForStudentInfo(String homeworkId) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        Subject subject = newHomework.getSubject();
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();

        Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.findByHomeworkForReport(newHomework);
        List<User> userList = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId());

        return userList.stream()
                .map(o -> {
                    Long userId = o.getId();
                    NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, subject, homeworkId, userId.toString());
                    NewHomeworkResult n = newHomeworkResultMap.get(id.toString());
                    return MapUtils.m(
                            "userId", o.getId(),
                            "userName", o.fetchRealname(),
                            "finished", n != null && n.isFinished()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 学生成绩进行排序
     */
    private void sortStudentReportList(List<Map<String, Object>> studentReportList) {
        if (CollectionUtils.isNotEmpty(studentReportList)) {
            studentReportList.sort((o1, o2) -> {
                Object o1Score = o1.get("avgScore");
                Object o2Score = o2.get("avgScore");
                long o1ScoreInt = SafeConverter.toLong(o1Score);
                long o2ScoreInt = SafeConverter.toLong(o2Score);
                if (o1ScoreInt != o2ScoreInt) {
                    return Long.compare(o2ScoreInt, o1ScoreInt);
                } else {
                    long createTime1 = o1.get("duration") != null ? SafeConverter.toLong(o1.get("duration")) : Long.MAX_VALUE;
                    long createTime2 = o2.get("duration") != null ? SafeConverter.toLong(o2.get("duration")) : Long.MAX_VALUE;
                    int ret = Long.compare(createTime1, createTime2);
                    if (ret != 0) return ret;
                    createTime1 = o1.get("finishTime") != null ? SafeConverter.toLong(o1.get("finishTime")) : Long.MAX_VALUE;
                    createTime2 = o2.get("finishTime") != null ? SafeConverter.toLong(o2.get("finishTime")) : Long.MAX_VALUE;
                    return Long.compare(createTime1, createTime2);
                }
            });
        }
    }


    /**
     * 学生完成情况
     *
     * @param homeworkId 作业Id
     * @return 学生完成情况
     */
    public MapMessage newHomeworkReportForStudent(Teacher teacher, String homeworkId, boolean isPcWay) {

        try {

            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST);
            }
            if (CollectionUtils.isEmpty(newHomework.getPractices())) {
                return MapMessage.errorMessage("这份作业里面不存在习题").setErrorCode(ErrorCodeConstants.ERROR_CODE_MISS_HOMEWORK_PRACTICE);
            }
            // 权限检查
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            Subject subject = newHomework.getSubject();
            //初始化作业类型
            List<ObjectiveConfigType> homeworkTypes = newHomework.getPractices()
                    .stream()
                    .map(NewHomeworkPracticeContent::getType)
                    .collect(Collectors.toList());
            String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();

            FlightRecorder.dot("before_fetch_newHomeworkResultMap" + homeworkId);
            Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.findByHomework(newHomework);
            FlightRecorder.dot("after_fetch_newHomeworkResultMap" + homeworkId);


            FlightRecorder.dot("before_fetch_newHomeworkProcessResultMap" + homeworkId);
            //取学生答题process result 不包含base_app 和 reading
            List<String> newHomeworkProcessResultIds = newHomeworkResultMap
                    .values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(o -> o.findAllHomeworkProcessIds(true))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            //newHomeworkProcessResultMap 主要用于主观作业的数据统计
            Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(homeworkId, newHomeworkProcessResultIds);
            FlightRecorder.dot("after_fetch_newHomeworkProcessResultMap" + homeworkId);
            /*开始统计学生完成情况*/
            List<User> userList = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId());

            TeacherReportParameter teacherReportParameter = new TeacherReportParameter();

            //订正信息
            teacherReportParameter.setNewHomeworkResultMap(newHomeworkResultMap);

            teacherReportParameter.setNewHomework(newHomework);

            teacherReportParameter.setHomeworkTypes(homeworkTypes);

            teacherReportParameter.setNewHomeworkProcessResultMap(newHomeworkProcessResultMap);
            intCorrectInfo(teacherReportParameter, newHomeworkResultMap);
            List<Map<String, Object>> studentReportList = new ArrayList<>();
            FlightRecorder.dot("before_handler_studentReport" + homeworkId);
            for (User user : userList) {
                Long userId = user.getId();
                NewHomeworkResult.ID newHomeworkResultId = new NewHomeworkResult.ID(day, subject, homeworkId, userId.toString());
                Map<String, Object> studentReport = new HashMap<>();
                NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(newHomeworkResultId.toString());
                if (newHomeworkResult != null) {
                    teacherReportParameter.setUser(user);
                    teacherReportParameter.setNewHomeworkResult(newHomeworkResult);
                    teacherReportParameter.setStudentReport(studentReport);
                    teacherReportParameter.setSelfStudyHomeworkReport(null);
                    teacherReportParameter.setHomeworkSelfStudyRef(null);

                    //学生作业开始练习
                    newHandleBegin(teacherReportParameter);
                } else {
                    //学生作业没有开始
                    handleUnBegin(studentReport, user, homeworkTypes);
                }
                studentReportList.add(studentReport);
            }
            sortStudentReportList(studentReportList);
            FlightRecorder.dot("after_handler_studentReport" + homeworkId);
            int avgScore = teacherReportParameter.handleNewTotalScore();
            int avgFinishTIme = teacherReportParameter.handleNewTotalFinishTime();
            GroupMapper group = groupLoaderClient.loadGroup(newHomework.getClazzGroupId(), true);
            Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(group == null ? null : group.getClazzId());

            List<Map<String, Object>> subjectTypeList = ObjectiveConfigType
                    .getSubjectTypes(newHomework.getSubject())
                    .stream()
                    .map(objectiveConfigType ->
                            MapUtils.m(
                                    "type", objectiveConfigType.name(),
                                    "typeId", objectiveConfigType.getKey(),
                                    "name", objectiveConfigType.getValue()))
                    .collect(Collectors.toList());
            Date d = DateUtils.stringToDate("2016-08-31 14:30:00", "yyyy-MM-dd HH:mm:ss");
            boolean isOldHomework = newHomework.getCreateAt().getTime() < d.getTime();
            //口算知识点报告统计
            teacherReportParameter.initKnowledgePointIdToQIds();
            //各类型每题的统计
            teacherReportParameter.initTypeQuestionsInfo(); // Statistics of every answering question 每一个题的答题统计初始值

            teacherReportParameter.setPcWay(isPcWay);
            FlightRecorder.dot("before_handler_objectiveConfig" + homeworkId);

            for (NewHomeworkPracticeContent newHomeworkPracticeContent : newHomework.getPractices()) {
                if (newHomeworkPracticeContent.getType() != null) {
                    //模板处理各个类型的数据
                    //类型数据统计，类型每题信息统计
                    ObjectiveConfigTypeProcessorTemplate template = objectiveConfigTypeProcessorFactory.getTemplate(newHomeworkPracticeContent.getType());
                    teacherReportParameter.setType(newHomeworkPracticeContent.getType());
                    if (template != null) {
                        template.processor(teacherReportParameter);
                    }
                }
            }
            FlightRecorder.dot("after_handler_objectiveConfig" + homeworkId);
            //知识点统计数据处理
            if (MapUtils.isNotEmpty(teacherReportParameter.getKnowledgePointIdToQIds())) {
                Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(teacherReportParameter.getKnowledgePointIdToQIds().keySet());
                teacherReportParameter.finishHandleKnowledgePointResult(newKnowledgePointMap);
            }

            //处理typeQuestionsInfo数据
            teacherReportParameter.finishTypeQuestionsInfoResult();

            FlightRecorder.dot("newHomeworkReportForStudent" + homeworkId);
            List<String> practices = homeworkTypes
                    .stream()
                    .map(ObjectiveConfigType::getValue)
                    .collect(Collectors.toList());
            MapMessage mapMessage = MapMessage.successMessage()
                    .add("isOldHomework", isOldHomework)
                    .add("showCorrect", teacherReportParameter.isShowCorrect())
                    .add("totalNeedCorrectedNum", teacherReportParameter.getNeedCorrectNum())
                    .add("finishedCorrectedCount", teacherReportParameter.getFinishedCorrectedNum())
                    .add("wrongReasonInformation", teacherReportParameter.handleSelfStudyHomeworkReport())
                    .add("studentReportList", studentReportList)
                    .add("objectiveConfigTypes", practices)
                    .add("includeSubjective", newHomework.getIncludeSubjective())
                    .add("subject", newHomework.getSubject())
                    .add("corrected", teacherReportParameter.getNewCorrectedCount() >= teacherReportParameter.getNewNeedCorrectCount())
                    .add("subjectId", newHomework.getSubject().getId())
                    .add("userCount", userList.size())
                    .add("joinCount", teacherReportParameter.getNewJoinCount())
                    .add("finishCount", teacherReportParameter.getNewFinishCount())
                    .add("unfinishCount", teacherReportParameter.getNewJoinCount() - teacherReportParameter.getNewFinishCount())
                    .add("unDoCount", userList.size() - teacherReportParameter.getNewJoinCount())
                    .add("avgScore", avgScore)
                    .add("homeworkType", newHomework.getNewHomeworkType())
                    .add("avgFinishTime", avgFinishTIme)
                    .add("typeReportList", teacherReportParameter.processTypeReport())
                    .add("clazzId", clazz == null ? null : clazz.getId())
                    .add("clazzName", clazz == null ? "" : clazz.formalizeClazzName())
                    .add("groupId", group == null ? null : group.getId())
                    .add("createAt", DateUtils.dateToString(newHomework.getCreateAt(), "yyyy年MM月dd日") + (newHomework.isTermEnd() ? "期末复习" : ""))
                    .add("currentDateTime", DateUtils.dateToString(DayRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATETIME))
                    .add("subjectTypeList", subjectTypeList);
            return mapMessage;
        } catch (Exception e) {
            logger.error("load Homework Report For Student tid {},hid {}", teacher.getId(), homeworkId, e);
            return MapMessage.errorMessage();
        }
    }


    private void intCorrectInfo(TeacherReportParameter teacherReportParameter, Map<String, NewHomeworkResult> newHomeworkResultMap) {
        NewHomework newHomework = teacherReportParameter.getNewHomework();
        boolean showCorrect = false;

        boolean isConfigTrue = true;
        try {
            String config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "GET_CORRECT_FUN_IS_SHOW");
            isConfigTrue = ConversionUtils.toBool(config);
        } catch (IllegalArgumentException e) {
            logger.info("CommonConfigLoaderClient GET_CORRECT_FUN_IS_SHOW : e{}", e.getMessage());
        }


        if (CollectionUtils.containsAny(GenerateSelfStudyHomeworkConfigTypes, teacherReportParameter.getHomeworkTypes())
                && NewHomeworkConstants.showWrongQuestionInfo(newHomework.getCreateAt(), RuntimeMode.getCurrentStage())
                && NeedSelfStudyHomeworkSubjects.contains(newHomework.getSubject())
                && isConfigTrue
        ) {
            showCorrect = true;
        }

        if (showCorrect) {
            List<String> homeworkToSelfStudyIds = newHomeworkResultMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(o -> newHomework.getId() + "_" + o.getUserId()).collect(Collectors.toList());

            teacherReportParameter.setRefMap(homeworkSelfStudyRefDao.loads(homeworkToSelfStudyIds));

            // 获取订正作业id，用于拿到订正报告
            List<String> selfStudyIds = teacherReportParameter.getRefMap().values()
                    .stream()
                    .filter(o -> StringUtils.isNotBlank(o.getSelfStudyId()))
                    .map(HomeworkSelfStudyRef::getSelfStudyId)
                    .collect(Collectors.toList());

            teacherReportParameter.setSelfStudyHomeworkReportMap(selfStudyHomeworkReportDao.loads(selfStudyIds));
            teacherReportParameter.setNeedCorrectNum(teacherReportParameter.getNeedCorrectNum() + teacherReportParameter.getRefMap().size());
            teacherReportParameter.setFinishedCorrectedNum(teacherReportParameter.getFinishedCorrectedNum() + teacherReportParameter.getSelfStudyHomeworkReportMap().size());
        }
        teacherReportParameter.setShowCorrect(showCorrect);
    }

    /**
     * 学生作业开始练习
     */
    private void newHandleBegin(TeacherReportParameter teacherReportParameter) {

        User user = teacherReportParameter.getUser();
        NewHomeworkResult newHomeworkResult = teacherReportParameter.getNewHomeworkResult();
        NewHomework newHomework = teacherReportParameter.getNewHomework();
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = teacherReportParameter.getNewHomeworkProcessResultMap();
        Map<String, Object> studentReport = teacherReportParameter.getStudentReport();
        List<Object> typeInformation = new ArrayList<>();
        Date finishAt = newHomeworkResult.getFinishAt();
        studentReport.put("userId", user.getId());
        studentReport.put("userName", user.fetchRealname());
        studentReport.put("finishAt", finishAt != null ? DateUtils.dateToString(newHomeworkResult.getFinishAt(), "MM月dd日 HH:mm") : null);
        studentReport.put("finishTime", finishAt != null ? finishAt.getTime() : Long.MAX_VALUE);
        studentReport.put("comment", newHomeworkResult.getComment());
        studentReport.put("audioComment", newHomeworkResult.getAudioComment());
        studentReport.put("rewardIntegral", newHomeworkResult.getRewardIntegral());
        studentReport.put("typeInfos", typeInformation);
        //处理批改
        teacherReportParameter.handleNewCorrectedCount();
        teacherReportParameter.handleNewNeedCorrectCount();

        //处理订正信息
        teacherReportParameter.handleCorrect();


        long duration = 0L;
        for (ObjectiveConfigType type : teacherReportParameter.getHomeworkTypes()) {
            if (MapUtils.isEmpty(newHomeworkResult.getPractices()) || Objects.isNull(newHomeworkResult.getPractices().get(type)) || !newHomeworkResult.getPractices().get(type).isFinished()) {
                typeInformation.add(null);
                continue;
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            Integer typeDuration = newHomeworkResultAnswer.processDuration();
            typeDuration = Objects.nonNull(typeDuration) ? typeDuration : 0;
            typeDuration = new BigDecimal(typeDuration).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue();
            duration += typeDuration;
            if (!type.isSubjective()) {
                int score = SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
                if (type == ObjectiveConfigType.MENTAL || type == ObjectiveConfigType.MENTAL_ARITHMETIC) {
                    Integer mentalDuration = newHomeworkResultAnswer.processDuration();
                    if (mentalDuration != null) {
                        int minutes = mentalDuration / 60;
                        int second = mentalDuration % 60;
                        if (minutes == 0) {
                            typeInformation.add(score + "分" + " (" + second + "\"" + ")");
                        } else {
                            typeInformation.add(score + "分" + " (" + minutes + "'" + second + "\"" + ")");
                        }
                    } else {
                        typeInformation.add(score + "分");
                    }
                } else {
                    typeInformation.add(score + "分");
                }
            } else {
                if (type == ObjectiveConfigType.NEW_READ_RECITE) {
                    List<String> readCorrections = new ArrayList<>();
                    List<String> reciteCorrections = new ArrayList<>();

                    boolean isAllUncorrected = true;
                    for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                        String correction;
                        if (appAnswer.getCorrection() != null) {
                            correction = appAnswer.getCorrection().getDescription();
                            isAllUncorrected = false;
                        } else if (appAnswer.getReview() != null) {
                            correction = "阅";
                            isAllUncorrected = false;
                        } else {
                            correction = "未批改";
                        }
                        if (appAnswer.getQuestionBoxType() == QuestionBoxType.READ) {
                            readCorrections.add(correction);
                        } else {
                            reciteCorrections.add(correction);
                        }
                    }
                    readCorrections.addAll(reciteCorrections);
                    if (isAllUncorrected) {
                        typeInformation.add("未批改");
                    } else {
                        typeInformation.add(StringUtils.join(readCorrections, ","));
                    }
                } else if (ObjectiveConfigType.DUBBING.equals(type)) {
                    // 趣味配音类型结果显示已完成或未完成
                    boolean isAllUnfinished = true;
                    if (MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                        for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                            if (appAnswer.isFinished()) {
                                isAllUnfinished = false;
                            }
                        }
                    }
                    if (isAllUnfinished) {
                        typeInformation.add("未完成");
                    } else {
                        typeInformation.add("已完成");
                    }
                } else {
                    //没分数则显示已阅或优良中差等
                    List<String> corrections = new ArrayList<>();
                    boolean isAllUncorrected = true;
                    for (String resultId : newHomeworkResultAnswer.getAnswers().values()) {
                        NewHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(resultId);
                        if (newHomeworkProcessResult == null) {
                            continue;
                        }
                        if (newHomeworkProcessResult.getCorrection() != null) {
                            corrections.add(newHomeworkProcessResult.getCorrection().getDescription());
                            isAllUncorrected = false;
                        } else if (newHomeworkProcessResult.getReview() != null) {
                            corrections.add("阅");
                            isAllUncorrected = false;
                        } else {
                            corrections.add("未批改");
                        }
                    }
                    if (isAllUncorrected) {
                        typeInformation.add("未批改");
                    } else {
                        typeInformation.add(StringUtils.join(corrections, ","));
                    }
                }
            }
        }
        int avgScore = 0;
        Integer processScore = newHomeworkResult.processScore();
        if (processScore == null) {
            studentReport.put("avgScore", null);
        } else {
            avgScore = processScore;
            studentReport.put("avgScore", avgScore);
        }
        if (finishAt != null) {
            teacherReportParameter.setNewTotalScore(teacherReportParameter.getNewTotalScore() + avgScore);
            teacherReportParameter.setNewFinishCount(1 + teacherReportParameter.getNewFinishCount());
            studentReport.put("repair", (finishAt.getTime() > newHomework.getEndTime().getTime() || (newHomework.getCheckedAt() != null && finishAt.getTime() > newHomework.getCheckedAt().getTime())));
            studentReport.put("duration", duration);
        } else {
            studentReport.put("duration", null);
        }
        teacherReportParameter.setNewJoinCount(teacherReportParameter.getNewJoinCount() + 1);

    }


    /**
     * 学生作业没有开始
     */

    private void handleUnBegin(Map<String, Object> studentReport, User user, List<ObjectiveConfigType> homeworkTypes) {
        List typeInformation = homeworkTypes
                .stream()
                .map(type -> null)
                .collect(Collectors.toList());
        studentReport.putAll(MapUtils.m(
                "userId", user.getId(),
                "avgCorrectedScoreInfo", "无需",
                "userName", user.fetchRealname(),
                "duration", null,
                "finishTime", null,
                "finishAt", null,
                "comment", null,
                "audioComment", null,
                "avgScore", null,
                "rewardIntegral", null,
                "typeInfos", typeInformation
        ));
    }

    public Page<Map<String, Object>> pageHomeworkReportListByGroupIds(Collection<Long> groupIds, Pageable pageable, Subject subject, Date begin, Date end) {
        try {
            if (CollectionUtils.isEmpty(groupIds) || pageable == null || subject == null) {
                return new PageImpl<>(Collections.emptyList());
            }
            // 按分组读取所有作业
            Map<Long, GroupMapper> groupMappers = groupLoaderClient.loadGroups(groupIds, true);
            Page<NewHomework> homeworkPage = newHomeworkLoader.loadGroupHomeworks(groupIds, subject, begin, end)
                    .sorted((o1, o2) -> {
                        boolean checked1 = o1.isChecked();
                        boolean checked2 = o2.isChecked();
                        int ret = Boolean.compare(checked1, checked2);
                        if (ret != 0) return ret;
                        long createAt1 = o1.getCreateTime();
                        long createAt2 = o2.getCreateTime();
                        return Long.compare(createAt2, createAt1);
                    }).toPage(pageable);
            List<NewHomework> homeworkList = homeworkPage.getContent();
            List<Map<String, Object>> results = buildHomeworkReportMapper(homeworkList, groupMappers);
            return new PageImpl<>(results, pageable, homeworkPage.getTotalElements());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new PageImpl<>(Collections.emptyList());
        }
    }
}
