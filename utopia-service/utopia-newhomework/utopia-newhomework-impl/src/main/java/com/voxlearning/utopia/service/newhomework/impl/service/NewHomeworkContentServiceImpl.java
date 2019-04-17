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

package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolYearPhase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.athena.api.cuotizhenduan.entity.IntellDiagSectionPak;
import com.voxlearning.athena.api.cuotizhenduan.entity.IntellVariantPak;
import com.voxlearning.athena.api.cuotizhenduan.entity.IntelligentDiagnosisPak;
import com.voxlearning.athena.api.recom.entity.paks.AthenaReviewPackageType;
import com.voxlearning.athena.api.recom.entity.paks.NewRecommendPackage;
import com.voxlearning.athena.api.recom.entity.paks.RecommendPointQuestionInfo;
import com.voxlearning.athena.api.recom.entity.wrapper.NewRecommendPackageWrapper;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.core.helper.ObjectCopyUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.NewKnowledgePointLoader;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.constant.NewBookType;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.consumer.ChineseContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.entity.*;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.AssignableValidationResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.WorkBookConfig;
import com.voxlearning.utopia.service.newhomework.api.mapper.wordteach.WordTeachHomeworkMapper;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkContentService;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.consumer.cache.RecommendContentCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.athena.*;
import com.voxlearning.utopia.service.newhomework.impl.dao.*;
import com.voxlearning.utopia.service.newhomework.impl.loader.*;
import com.voxlearning.utopia.service.newhomework.impl.recommendation.RecommendedHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.*;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.AssignBasicReviewHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.template.*;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.content.*;
import com.voxlearning.utopia.service.question.api.TeacherCoursewareLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.constant.PictureBookApply;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisVariant;
import com.voxlearning.utopia.service.question.api.entity.stone.data.StoneBufferedData;
import com.voxlearning.utopia.service.question.api.entity.stone.data.oralpractice.BaseOralPractice;
import com.voxlearning.utopia.service.question.api.mapper.PictureBookQuery;
import com.voxlearning.utopia.service.question.api.mapper.review.ChineseBasicReview;
import com.voxlearning.utopia.service.question.api.mapper.review.EnglishReview;
import com.voxlearning.utopia.service.question.api.mapper.review.MathReview;
import com.voxlearning.utopia.service.question.consumer.*;
import com.voxlearning.utopia.service.question.consumer.TermReviewLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserExtensionAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.*;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @author guoqiang.li
 * @since 2016/1/19
 */

@Named
@Service(interfaceClass = NewHomeworkContentService.class)
@ExposeService(interfaceClass = NewHomeworkContentService.class)
public class NewHomeworkContentServiceImpl extends SpringContainerSupport implements NewHomeworkContentService {

    @Inject
    private PictureBookPlusServiceClient pictureBookPlusServiceClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;
    @Inject
    private AthenaHomeworkLoaderClient athenaHomeworkLoaderClient;
    @Inject
    private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private NewClazzBookLoaderClient newClazzBookLoaderClient;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private NewHomeworkContentLoaderFactory newHomeworkContentLoaderFactory;
    @Inject
    private TermReviewContentLoaderFactory termReviewContentLoaderFactory;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private PictureBookLoaderClient pictureBookLoaderClient;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private TeacherAssignmentRecordLoaderImpl teacherAssignmentRecordLoader;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private TeachingObjectiveLoaderClient teachingObjectiveLoaderClient;
    @Inject
    private UserAttributeLoaderClient userAttributeLoaderClient;
    @Inject
    private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;
    @Inject
    private TotalAssignmentRecordLoaderImpl totalAssignmentRecordLoader;
    @Inject
    private NewHomeworkBasicAppContentLoader newHomeworkBasicAppContentLoader;
    @Inject
    private NewHomeworkReadingContentLoader newHomeworkReadingContentLoader;
    @Inject
    private VideoLoaderClient videoLoaderClient;
    @Inject
    private UniSoundScoreLevelHelper uniSoundScoreLevelHelper;
    @Inject
    private PracticeLoaderClient practiceLoaderClient;
    @Inject
    private NewHomeworkNaturalSpellingContentLoader newHomeworkNaturalSpellingContentLoader;
    @Inject
    private AthenaReviewLoaderClient athenaReviewLoaderClient;
    @Inject
    private TermReviewLoaderClient termReviewLoaderClient;
    @Inject
    private DubbingLoaderClient dubbingLoaderClient;
    @Inject
    private BasicReviewHomeworkLoaderImpl basicReviewHomeworkLoader;
    @Inject
    private AssignBasicReviewHomeworkProcessor assignBasicReviewHomeworkProcessor;
    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject
    private PictureBookPlusHistoryDao pictureBookPlusHistoryDao;
    @Inject
    private PictureBookSearchLoaderClient pictureBookSearchLoaderClient;
    @Inject
    private DubbingSearchLoaderClient dubbingSearchLoaderClient;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    @Inject
    private PictureBookPlusRecommendRecordLoaderImpl pictureBookPlusRecommendRecordLoader;
    @Inject
    private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject
    private DubbingRecommendRecordDao dubbingRecommendRecordDao;
    @Inject
    private NewKnowledgePointLoader newKnowledgePointLoader;
    @Inject
    private XxWorkbookLoaderClient xxWorkbookLoaderClient;
    @Inject
    private OcrMentalWorkBookConfigHelper ocrMentalWorkBookConfigHelper;
    @Inject
    private WrongQuestionDiagnosisLoaderClient wrongQuestionDiagnosisLoaderClient;
    @Inject
    private IntelDiagnosisClient intelDiagnosisClient;
    @Inject
    private DubbingCollectionRecordDao dubbingCollectionRecordDao;
    @Inject
    private VoxScoreLevelHelper voxScoreLevelHelper;
    @Inject
    private VoiceEngineConfigHelper voiceEngineConfigHelper;
    @Inject
    private ImageQualityStrHelper imageQualityStrHelper;
    @Inject
    protected ChineseContentLoaderClient chineseContentLoaderClient;
    @Inject
    private OutsideReadingServiceImpl outsideReadingServiceImpls;
    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;
    @Inject
    private OralCommunicationSearchClient oralCommunicationSearchClient;
    @Inject
    private OralCommunicationRecommendRecordLoaderImpl oralCommunicationRecommendRecordLoader;
    @Inject
    private RecommendOralCommunicationLoaderClient oralCommunicationLoaderClient;
    @Inject
    private NewHomeworkLevelReadingsContentLoader newHomeworkLevelReadingsContentLoader;
    @Inject
    private NewHomeworkIntelligentTeachingContentLoader newHomeworkIntelligentTeachingContentLoader;
    @Inject
    private TeacherCoursewareLoader teacherCoursewareLoader;
    @Inject
    private HomeworkReportShareChannelHelper homeworkReportShareChannelHelper;
    @Inject
    private OcrMentalBookDao ocrMentalBookDao;
    @Inject
    private NewHomeworkDictationContentLoader newHomeworkDictationContentLoader;
    @Inject
    private DaiTeTypeFactory daiTeTypeFactory;
    @Inject
    private TeachingResourceBookPersistence teachingResourceBookPersistence;
    @Inject
    private OralCommunicationRecommendRecordDao oralCommunicationRecommendRecordDao;
    @Inject
    private RecommendedHomeworkLoaderClient recommendedHomeworkLoaderClient;

    @Inject private RaikouSDK raikouSDK;

    private static List<Integer> FILTER_CATEGORY_IDS = Arrays.asList(10310, 10305, 10303, 10304);

    private static Map<String, Integer> PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP = new LinkedHashMap<>();

    static {
        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000002435", 1);
        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000080777", 1);
        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000075123", 1);

        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000001576", 2);
        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000076294", 2);
        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000004813", 2);

        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000010606", 3);
        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000001892", 3);
        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000007919", 3);

        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000079028", 4);
        PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.put("PBS_10300000081780", 4);
    }

    @Override
    public List<ExClazz> findTeacherClazzsCanBeAssignedHomework(Teacher teacher) {
        if (teacher == null || teacher.getId() == null || teacher.getSubject() == null) {
            return Collections.emptyList();
        }
        Subject subject = teacher.getSubject();
        List<ExClazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherExClazzsWithSpecifiedSubject(teacher.getId(), subject, false, false);
        if (CollectionUtils.isEmpty(clazzs)) {
            logger.warn("Teacher '{}' has no non-terminal clazzs, ignore", teacher.getId());
            return Collections.emptyList();
        }
        Map<Long, ExClazz> clazzCandidate = getCandidateSystemClazz(subject, clazzs);

        return clazzCandidate.values().stream()
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
    }

    @Override
    public MapMessage loadTeacherClazzList(Teacher teacher, Set<NewHomeworkType> newHomeworkTypes, Boolean filterEmptyClazz) {
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId()).stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        // 学前按照年级倒序重新排序
        if (teacher.isInfantTeacher()) {
            clazzs.sort((o1, o2) -> Integer.compare(o2.getClazzLevel().getLevel(), o1.getClazzLevel().getLevel()));
        }
        Map<Long, List<GroupMapper>> groupMaps = groupLoaderClient.loadClazzGroups(clazzs.stream().map(Clazz::getId).collect(Collectors.toList()));
        List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByTeacherId(teacher.getId())
                .stream()
                .map(GroupTeacherTuple::getGroupId)
                .collect(Collectors.toList());
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
        Collection<Long> groupIds = clazzIdGroupIdMap.values();
        // 验证每个分组是否有学生存在
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIds);
        Map<Long, List<NewHomework.Location>> groupLocationMap = newHomeworkLoader.loadGroupHomeworks(groupIds, teacher.getSubject())
                .originalLocationsAsList()
                .stream()
                .filter(e -> !e.isChecked())
                .collect(Collectors.groupingBy(NewHomework.Location::getClazzGroupId));

        Map<Integer, List<Map>> clazzMap = clazzs.stream()
                .filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .filter(clazz -> Objects.equals(false, filterEmptyClazz) || CollectionUtils.isNotEmpty(groupStudentIds.get(clazzIdGroupIdMap.get(clazz.getId()))))
                .map(clazz -> MapUtils.m("clazzId", clazz.getId(),
                        "clazzName", clazz.getClassName(),
                        "fullName", clazz.formalizeClazzName(),
                        "clazzLevel", clazz.getClazzLevel().getLevel(),
                        "groupId", clazzIdGroupIdMap.get(clazz.getId()),
                        "hasUncheckedHomework", CollectionUtils.isNotEmpty(groupLocationMap.get(clazzIdGroupIdMap.get(clazz.getId())))
                                && groupLocationMap.get(clazzIdGroupIdMap.get(clazz.getId())).stream().anyMatch(location -> newHomeworkTypes.contains(location.getType()))))
                .collect(Collectors.groupingBy(clazz -> SafeConverter.toInt(clazz.get("clazzLevel")), LinkedHashMap::new, Collectors.mapping(p -> p, Collectors.toList())));
        Map<Integer, Boolean> clazzCanAssigned = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Map>> entry : clazzMap.entrySet()) {
            Integer clazzLevel = entry.getKey();
            List<Map> clazzList = entry.getValue();
            Boolean canBeAssigned = false;
            if (CollectionUtils.isNotEmpty(clazzList)) {
                for (Map map : clazzList) {
                    Boolean hasUncheckedHomework = (Boolean) map.get("hasUncheckedHomework");
                    if (!hasUncheckedHomework) {
                        canBeAssigned = true;
                        break;
                    }
                }
            }
            clazzCanAssigned.put(clazzLevel, canBeAssigned);
        }
        List<Map<String, Object>> clazzList = new ArrayList<>();
        clazzMap.forEach((k, v) -> clazzList.add(MapUtils.m(
                "clazzLevel", k,
                "clazzLevelName", ClazzLevel.parse(k).getDescription(),
                "canBeAssigned", clazzCanAssigned.get(k),
                "clazzs", v)));
        return MapMessage.successMessage().add("clazzList", clazzList);
    }

    @Override
    public MapMessage loadNewTeacherClazzList(Teacher teacher, Set<NewHomeworkType> newHomeworkTypes, Set<HomeworkTag> HomeworkTags, Boolean filterEmptyClazz) {
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId()).stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        // 学前按照年级倒序重新排序
        if (teacher.isInfantTeacher()) {
            clazzs.sort((o1, o2) -> Integer.compare(o2.getClazzLevel().getLevel(), o1.getClazzLevel().getLevel()));
        }
        Map<Long, List<GroupMapper>> groupMaps = groupLoaderClient.loadClazzGroups(clazzs.stream().map(Clazz::getId).collect(Collectors.toList()));
        List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByTeacherId(teacher.getId())
                .stream()
                .map(GroupTeacherTuple::getGroupId)
                .collect(Collectors.toList());
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
        Collection<Long> groupIds = clazzIdGroupIdMap.values();
        // 验证每个分组是否有学生存在
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIds);
        Map<Long, List<NewHomework.Location>> groupLocationMap = newHomeworkLoader.loadGroupHomeworks(groupIds, teacher.getSubject())
                .originalLocationsAsList()
                .stream()
                .filter(e -> !e.isChecked())
                .collect(Collectors.groupingBy(NewHomework.Location::getClazzGroupId));

        Map<Integer, List<Map>> clazzMap = clazzs.stream()
                .filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .filter(clazz -> Objects.equals(false, filterEmptyClazz) || CollectionUtils.isNotEmpty(groupStudentIds.get(clazzIdGroupIdMap.get(clazz.getId()))))
                .map(clazz -> MapUtils.m("clazzId", clazz.getId(),
                        "clazzName", clazz.getClassName(),
                        "fullName", clazz.formalizeClazzName(),
                        "clazzLevel", clazz.getClazzLevel().getLevel(),
                        "groupId", clazzIdGroupIdMap.get(clazz.getId()),
                        "hasUncheckedHomework", CollectionUtils.isNotEmpty(groupLocationMap.get(clazzIdGroupIdMap.get(clazz.getId())))
                                && groupLocationMap.get(clazzIdGroupIdMap.get(clazz.getId())).stream().anyMatch(location -> newHomeworkTypes.contains(location.getType()) && HomeworkTags.contains(location.getHomeworkTag()))))
                .collect(Collectors.groupingBy(clazz -> SafeConverter.toInt(clazz.get("clazzLevel")), LinkedHashMap::new, Collectors.mapping(p -> p, Collectors.toList())));
        Map<Integer, Boolean> clazzCanAssigned = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Map>> entry : clazzMap.entrySet()) {
            Integer clazzLevel = entry.getKey();
            List<Map> clazzList = entry.getValue();
            Boolean canBeAssigned = false;
            if (CollectionUtils.isNotEmpty(clazzList)) {
                for (Map map : clazzList) {
                    Boolean hasUncheckedHomework = (Boolean) map.get("hasUncheckedHomework");
                    if (!hasUncheckedHomework) {
                        canBeAssigned = true;
                        break;
                    }
                }
            }
            clazzCanAssigned.put(clazzLevel, canBeAssigned);
        }
        List<Map<String, Object>> clazzList = new ArrayList<>();
        clazzMap.forEach((k, v) -> clazzList.add(MapUtils.m(
                "clazzLevel", k,
                "clazzLevelName", ClazzLevel.parse(k).getDescription(),
                "canBeAssigned", clazzCanAssigned.get(k),
                "clazzs", v)));
        return MapMessage.successMessage().add("clazzList", clazzList);
    }

    @Override
    public MapMessage loadTeachersClazzList(Collection<Long> teacherIds, Set<NewHomeworkType> newHomeworkTypes, Boolean filterEmptyClazz) {
        // 老师分组
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, false);
        // 分组id
        List<Long> groupIds = new LinkedList<>();
        // 班级id
        List<Long> clazzIds = new LinkedList<>();
        // clazz id -> group id map
        Map<Long, List<Long>> clazzIdGroupIdMap = new HashMap<>();
        // 学科 -> 分组id
        Map<Subject, List<Long>> subjectGroupIdsMap = new HashMap<>();
        // 分组id -> 学科
        Map<Long, Subject> groupIdSubjectMap = new HashMap<>();

        teacherGroups.forEach((teacherId, groups) -> groups.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(teacherId)) {
                // 分组id
                groupIds.add(group.getId());
                // 班级id
                clazzIds.add(group.getClazzId());
                // 学科 -> 分组id
                subjectGroupIdsMap.computeIfAbsent(group.getSubject(), k -> new ArrayList<>())
                        .add(group.getId());
                // clazz id -> group id map
                clazzIdGroupIdMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>())
                        .add(group.getId());
                groupIdSubjectMap.put(group.getId(), group.getSubject());
            }
        }));

        // 分组学生
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIds);
        // 分组作业
        Map<Long, List<NewHomework.Location>> groupLocationMap = new HashMap<>();
        subjectGroupIdsMap.forEach((subject, gids) -> groupLocationMap.putAll(newHomeworkLoader.loadGroupHomeworks(gids, subject)
                .originalLocationsAsList()
                .stream()
                .filter(e -> !e.isChecked())
                .collect(Collectors.groupingBy(NewHomework.Location::getClazzGroupId))));
        // 班级
        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        // 学前按照年级倒序重新排序
        if (CollectionUtils.isNotEmpty(clazzs)) {
            Clazz clazz = clazzs.get(0);
            if (clazz.isInfantClazz()) {
                clazzs.sort(((o1, o2) -> Integer.compare(o2.getClazzLevel().getLevel(), o1.getClazzLevel().getLevel())));
            }
        }

        Set<Subject> canBeAssignedSubjects = new LinkedHashSet<>();
        List<Map<String, Object>> clazzMap = new ArrayList<>();
        clazzs.stream()
                .filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .forEach(c -> {
                    List<Long> gids = clazzIdGroupIdMap.get(c.getId());
                    gids.forEach(gid -> {
                        if (Objects.equals(false, filterEmptyClazz) || CollectionUtils.isNotEmpty(groupStudentIds.get(gid))) {
                            Subject subject = groupIdSubjectMap.get(gid);
                            boolean hasUncheckedHomework = CollectionUtils.isNotEmpty(groupLocationMap.get(gid))
                                    && groupLocationMap.get(gid).stream().anyMatch(location -> newHomeworkTypes.contains(location.getType()));
                            Map<String, Object> m = MapUtils.m("clazzId", c.getId(),
                                    "clazzName", c.getClassName(),
                                    "clazzLevel", c.getClazzLevel().getLevel(),
                                    "groupId", gid,
                                    "subject", subject,
                                    "subjectName", subject.getValue(),
                                    "hasUncheckedHomework", hasUncheckedHomework);
                            clazzMap.add(m);
                            if (!hasUncheckedHomework) {
                                canBeAssignedSubjects.add(subject);
                            }
                        }
                    });
                });
        List<Map<String, Object>> clazzList = new ArrayList<>();
        clazzMap.stream()
                .collect(Collectors.groupingBy(clazz -> SafeConverter.toInt(clazz.get("clazzLevel")), LinkedHashMap::new, Collectors.mapping(p -> p, Collectors.toList())))
                .forEach((k, v) -> clazzList.add(MapUtils.m("clazzLevel", k, "clazzLevelName", ClazzLevel.parse(k).getDescription(), "clazzs", v)));
        return MapMessage.successMessage().add("clazzList", clazzList).add("canBeAssignedSubjects", canBeAssignedSubjects);
    }

    @Override
    public MapMessage loadClazzBook(Teacher teacher, Map<Long, Long> clazzGroupMap, Boolean fromVacation) {
        Collection<Long> clazzIds = clazzGroupMap.keySet();
        Collection<Long> groupIds = clazzGroupMap.values();
        if (CollectionUtils.isEmpty(clazzIds)) {
            return MapMessage.errorMessage("信息不全");
        }
        Set<Long> teacherClazzIds = new HashSet<>(teacherLoaderClient.loadTeacherClazzIds(teacher.getId()));
        for (Long clazzId : clazzIds) {
            if (!teacherClazzIds.contains(clazzId)) {
                return MapMessage.errorMessage("您没有该权限");
            }
        }
        // 所有班级的最新教材
        NewClazzBookRef newClazzBookRef = newClazzBookLoaderClient.loadGroupBookRefs(groupIds)
                .subject(teacher.getSubject())
                .toList()
                .stream()
                .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                .findFirst()
                .orElse(null);
        NewBookProfile book = null;
        if (newClazzBookRef != null) {
            String bookId = newClazzBookRef.getBookId();
            Map<String, NewBookProfile> bookMap = newContentLoaderClient.loadBooks(Collections.singleton(bookId));
            if (MapUtils.isNotEmpty(bookMap)) {
                book = bookMap.get(bookId);
            }
        }

        if (book != null) {
            int latestVersion = SafeConverter.toInt(book.getLatestVersion());
            int subjectId = SafeConverter.toInt(book.getSubjectId());
            // 填坑。
            // 小学数学 latestVersion=0 重新推一本默认教材
            if (subjectId == 102 && latestVersion == 0) {
                book = null;
            }
        }

        // 学前老师如果不是使用的学前教材，重新推默认教材
        if (teacher.isInfantTeacher() && book != null && !Objects.equals(QuestionConstants.SUBJECT_PRESCHOOL_ENGLISH, book.getSubjectId())) {
            book = null;
        }

        // 找不到班级已使用教材,推送默认教材
        // 推默认教材，按第一个班级推
        Long clazzId = clazzIds.iterator().next();
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId);
        if (book == null) {
            if (clazz != null) {
                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(clazz.getSchoolId())
                        .getUninterruptibly();
                if (school != null) {
                    int subjectId = SafeConverter.toInt(teacher.getSubject().getId());
                    if (teacher.isInfantTeacher()) {
                        subjectId += 400;
                    }
                    String bookId = newContentLoaderClient.initializeClazzBook(subjectId, clazz.getClazzLevel(), school.getRegionCode());
                    if (bookId != null) {
                        book = newContentLoaderClient.loadBooks(Collections.singleton(bookId)).get(bookId);
                    }
                }
            }

            if (book == null) {
                return MapMessage.errorMessage("未找到合适的教材");
            }
        }

//        // 假期作业强制使用上册教材的地区特殊处理默认教材
//        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
//        if (fromVacation && Objects.equals(book.getTermType(), Term.下学期.getKey())
//                && grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "VacationHW", "LastTermBook")) {
//            List<NewBookProfile> books = newContentLoaderClient
//                    .loadBooksByClassLevelAndTermAndSeriesIdAndBookType(Subject.fromSubjectId(book.getSubjectId()), ClazzLevel.parse(book.getClazzLevel()), Term.上学期, book.getSeriesId(), NewBookType.TEXTBOOK.name())
//                    .stream()
//                    .sorted((o1, o2) -> Integer.compare(o2.getLatestVersion(), o1.getLatestVersion()))
//                    .collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(books)) {
//                book = books.get(0);
//            }
//        }

        Map<String, Object> bookMap = buildBookMapper(book, newClazzBookRef);
        // 提示更换学期教材 如果课本与当前学期年级不符，提示更换
        bookMap.put("remindBookFlag", false);
        SchoolYear currentSchoolYear = SchoolYear.newInstance(new Date());
        SchoolYearPhase phase = currentSchoolYear.currentPhase();
        if (phase == SchoolYearPhase.LAST_TERM || phase == SchoolYearPhase.NEXT_TERM) {
            int currentTerm = currentSchoolYear.currentTerm().getKey();
            int clazzLevel = clazz.getClazzLevel().getLevel();
            if (clazzLevel != book.getClazzLevel() || currentTerm != book.getTermType()) {
                UserExtensionAttribute attribute = userAttributeLoaderClient.loadUserExtensionAttributes(teacher.getId())
                        .key(UserExtensionAttributeKeyType.REMIND_TEACHER_CHANGE_BOOK.name() + "_" + clazzLevel)
                        .findFirst();
                if (attribute == null || !currentSchoolYear.currentPhaseDateRange().contains(DateUtils.stringToDate(attribute.getExtensionAttributeValue()))) {
                    String seriesId = book.getSeriesId();
                    List<NewBookProfile> bookList = newContentLoaderClient.loadBooks(teacher.getSubject()).stream()
                            .filter(NewBookProfile::isOnline)
                            .filter(e -> Objects.equals(e.getClazzLevel(), clazzLevel))
                            .filter(e -> Objects.equals(e.getTermType(), currentTerm))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(bookList)) {
                        bookList = bookList.stream().filter(bookProfile -> StringUtils.equals(seriesId, bookProfile.getSeriesId()))
                                .sorted((o1, o2) -> Integer.compare(o2.getLatestVersion(), o1.getLatestVersion())).collect(Collectors.toList());
                        bookMap.put("remindBookFlag", true);
                        bookMap.put("remindBook", MiscUtils.firstElement(bookList));
                    }
                }
            }
        }

        return MapMessage.successMessage().add("clazzBook", bookMap);
    }

    @Override
    public MapMessage load17XueBook(Teacher teacher, Collection<Long> groupIds, NewBookType newBookType) {

        if (CollectionUtils.isEmpty(groupIds)) {
            return MapMessage.errorMessage("信息不全");
        }

        // 所有班级的最新教材
        NewClazzBookRef newClazzBookRef = newClazzBookLoaderClient.loadGroupBookRefs(groupIds)
                .subject(teacher.getSubject())
                .toList()
                .stream()
                .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                .findFirst()
                .orElse(null);
        NewBookProfile book = null;
        if (newClazzBookRef != null) {
            String bookId = newClazzBookRef.getBookId();
            Map<String, NewBookProfile> bookMap = newContentLoaderClient.loadBooks(Collections.singleton(bookId));
            if (MapUtils.isNotEmpty(bookMap)) {
                book = bookMap.get(bookId);
            }
        }

        // 找不到已使用教材,推送默认教材
        // 推默认教材，取教材列表第一个
        if (book == null) {
            List<NewBookProfile> newBookProfiles = newContentLoaderClient.loadBookBySubjectAndNewBookType(teacher.getSubject(), newBookType);
            if (CollectionUtils.isNotEmpty(newBookProfiles)) {
                book = newBookProfiles.get(0);
            }
            if (book == null) {
                return MapMessage.errorMessage("未找到合适的教材");
            }
        }
        Map<String, Object> bookMap = buildBookMapper(book, newClazzBookRef);
        // 提示更换学期教材 如果课本与当前学期年级不符，提示更换
        bookMap.put("remindBookFlag", false);
        return MapMessage.successMessage().add("clazzBook", bookMap);
    }

    @Override
    public List<NewBookProfile> loadBooks(Teacher teacher, Integer clazzLevel, Integer term) {
        Subject subject = teacher.getSubject();
        if (subject == null) {
            return Collections.emptyList();
        }
        int subjectId = SafeConverter.toInt(subject.getId());
        if (teacher.isInfantTeacher()) {
            subjectId += 400;
        }
        // 学前特殊处理 统一使用 学前班 上册 取教材
        int usingClazzLevel = teacher.isInfantTeacher() ? ClazzLevel.INFANT_FOURTH.getLevel() : clazzLevel;
        int usingTerm = teacher.isInfantTeacher() ? Term.上学期.getKey() : term;
        List<NewBookProfile> books = newContentLoaderClient.loadBooksBySubjectId(subjectId)
                .stream()
                .filter(NewBookProfile::isOnline)
                .filter(e -> Objects.equals(e.getClazzLevel(), usingClazzLevel))
                .filter(e -> Objects.equals(e.getTermType(), usingTerm))
                // 作业接口：小学数学 latestVersion=1
                .filter(e -> (subject != Subject.MATH)
                        || (SafeConverter.toInt(e.getLatestVersion(), -1) == 1))
                .collect(Collectors.toList());
        // 中文排序
        Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
        Comparator<NewBookProfile> comparator = (o2, o1) -> com.compare(o2.getName(), o1.getName());
        books.sort(comparator);
        return books;
    }

    @Override
    public MapMessage loadBookUnitList(String bookId) {
        NewBookProfile book = newContentLoaderClient.loadBook(bookId);
        if (book == null) {
            return MapMessage.errorMessage("教材不存在或已下线");
        }
        Map<String, Object> bookMap = buildBookMapper(book, null);
        return MapMessage.successMessage().add("book", bookMap);
    }

    /**
     * 获取内容库配置的作业形式
     */
    @Override
    public MapMessage getHomeworkType(TeacherDetail teacher, List<String> sectionIds, String unitId, String bookId, String sys, String appVersion, String cdnUrl) {
        return MapMessage.successMessage()
                .add("homeworkType", Collections.emptyList())
                .add("subjectType", Collections.emptyList())
                .add("groupList", Collections.emptyList());
    }

    /**
     * 获取内容库配置的作业内容
     * 注意：这个接口平台布置新结构已经不支持了，暂时兼容USTALK、智慧课堂课堂资源 基础练习和绘本内容的获取
     */
    @Override
    public MapMessage getHomeworkContent(TeacherDetail teacher, Set<Long> groupIds, List<String> sectionIds, String unitId, String bookId,
                                         ObjectiveConfigType objectiveConfigType, Integer currentPageNum) {
        List<Map<String, Object>> content = Collections.emptyList();
        if (objectiveConfigType == ObjectiveConfigType.READING) {
            content = newHomeworkReadingContentLoader.getHomeworkContent(teacher, groupIds, sectionIds, unitId, bookId, currentPageNum);
        } else if (objectiveConfigType == ObjectiveConfigType.BASIC_APP) {
            content = newHomeworkBasicAppContentLoader.getHomeworkContent(teacher, unitId, bookId);
        }
        return MapMessage.successMessage().add("content", content);
    }

    /**
     * 口算取题
     */
    @Override
    public MapMessage getMentalQuestion(String knowledgePoint, Integer contentTypeId, List<String> chosenQuestionIds, Integer newQuestionCount) {
        MapMessage mapMessage = MapMessage.successMessage();
        List<Map<String, Object>> questions = new ArrayList<>();
        List<String> questionIds = null;
        if (newQuestionCount > 0 && (contentTypeId == 0 || QuestionConstants.mentalIncludeContentTypeIds.contains(contentTypeId))) {
            List<Integer> contentTypeIdList = contentTypeId == 0 ? QuestionConstants.mentalIncludeContentTypeIds : Collections.singletonList(contentTypeId);
            questionIds = questionLoaderClient.loadRandomQuestionIdsByNewKnowledgePointId(Collections.singleton(knowledgePoint), contentTypeIdList, chosenQuestionIds, newQuestionCount, true, true, true, true);
        }
        if (CollectionUtils.isNotEmpty(questionIds)) {
            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            questionIds.forEach(questionId -> {
                NewQuestion newQuestion = newQuestionMap.get(questionId);
                if (newQuestion != null) {
                    String questionContent = null;
                    if (newQuestion.getContent() != null && CollectionUtils.isNotEmpty(newQuestion.getContent().getSubContents())) {
                        questionContent = newQuestion.getContent().getSubContents().get(0).getContent();
                    }
                    Map<String, Object> questionMap = new LinkedHashMap<>();
                    questionMap.put("contentTypeId", contentTypeId);
                    questionMap.put("questionId", questionId);
                    questionMap.put("questionContent", questionContent);
                    questionMap.put("knowledgePoint", knowledgePoint);
                    questionMap.put("seconds", SafeConverter.toInt(newQuestion.getSeconds()));
                    questions.add(questionMap);
                }
            });
        }
        mapMessage.put("questions", questions);
        return mapMessage;
    }

    @Override
    public MapMessage loadOcrMentalWorkBookList(TeacherDetail teacherDetail, String bookId) {
        List<WorkBookConfig> configs = ocrMentalWorkBookConfigHelper.loadConfig();
        Set<String> configWorkBookIds = configs.stream()
                .map(WorkBookConfig::getWorkBookId)
                .collect(Collectors.toSet());
        Map<String, XxWorkbook> configWorkBookMap = xxWorkbookLoaderClient.getRemoteReference().loadXxWorkbooksByDocids(configWorkBookIds);
        // 根据教材ID进行筛选
        List<XxWorkbook> filterWorkBooks = configWorkBookMap.values().stream()
                .filter(workBook -> workBook.getBook_new() != null && workBook.getBook_new().getBookId() != null)
                .filter(workBook -> StringUtils.equalsIgnoreCase(workBook.getBook_new().getBookId(), bookId))
                .collect(Collectors.toList());

        List<XxWorkbook> workbookList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filterWorkBooks)) {
            //根据地区刷选
            Map<String, List<WorkBookConfig>> workBookConfigMap = new HashMap<>();
            for (WorkBookConfig workBookConfig : configs) {
                workBookConfigMap.computeIfAbsent(workBookConfig.getWorkBookId(), k -> new ArrayList<>()).add(workBookConfig);
            }
            for (XxWorkbook workbook : filterWorkBooks) {
                List<WorkBookConfig> workBookConfigList = workBookConfigMap.get(workbook.getDoc_id());
                // TODO: 2018/8/9 排序有待商榷
                workBookConfigList.sort(Comparator.comparing(WorkBookConfig::getAcode).thenComparing(WorkBookConfig::getCcode).thenComparing(WorkBookConfig::getPriority));
                if (CollectionUtils.isNotEmpty(workBookConfigList)) {
                    for (WorkBookConfig workBookConfig : workBookConfigList) {
                        Integer regionId = workBookConfig.getAcode();
                        Integer cityId = workBookConfig.getCcode();
                        Integer provinceId = workBookConfig.getPcode();

                        if (regionId != null && regionId.equals(teacherDetail.getRegionCode())) {
                            workbookList.add(workbook);
                        } else if (regionId == null && cityId != null && cityId.equals(teacherDetail.getCityCode())) {
                            workbookList.add(workbook);
                        } else if (regionId == null && provinceId != null && provinceId.equals(teacherDetail.getRootRegionCode())) {
                            workbookList.add(workbook);
                        } else if (provinceId == null) {
                            workbookList.add(workbook);
                        }
                    }
                }
            }
        }

        // 如果没有教辅的话，显示这些教辅
        // W_10200001431557 一年级
        // W_10200001432827 二年级
        // W_10200001433845 三年级
        // W_10200001434288 四年级
        // W_10200001435616 五年级
        // W_10200001436452 六年级
        // W_10200001461282 一年级
        // W_10200001462413 二年级
        // W_10200001463332 三年级
        // W_10200001464461 四年级
        // W_10200001465527 五年级
        if (CollectionUtils.isEmpty(workbookList)) {
            List<String> workBookIds = Arrays.asList("W_10200001431557", "W_10200001432827", "W_10200001433845", "W_10200001434288", "W_10200001435616",
                    "W_10200001436452", "W_10200001461282", "W_10200001462413", "W_10200001463332", "W_10200001464461", "W_10200001465527");
            Map<String, XxWorkbook> workbookMap = xxWorkbookLoaderClient.getRemoteReference().loadXxWorkbooksByDocids(workBookIds);
            NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
            // 根据教材ID进行筛选
            workbookList = workbookMap.values().stream()
                    .filter(workBook -> workBook.getBook_new() != null && workBook.getBook_new().getClassLevel() != null)
                    .filter(workBook -> workBook.getBook_new().getClassLevel().equals(newBookProfile.getClazzLevel()))
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> workBookMapperList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(workbookList)) {
            workBookMapperList = workbookList
                    .stream()
                    .map(workBook -> MapUtils.m(
                            "workBookId", workBook.getId(),
                            "workBookName", workBook.getAlias(),
                            "coverImageUrl", workBook.getCover() != null ? workBook.getCover().getOri_url() : ""
                    ))
                    .collect(Collectors.toList());
        }

        return MapMessage.successMessage().add("workBooks", workBookMapperList);
    }

    @Override
    public MapMessage searchReading(PictureBookQuery pictureBookQuery, Pageable pageable, String bookId, String unitId, Teacher teacher) {
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("信息不全");
        }
        Subject subject = teacher.getSubject();
        if (subject == null) {
            return MapMessage.errorMessage("请先设置学科");
        }
        // 学前学科id为 501,502,503
//        int subjectId = teacher.isInfantTeacher() ? subject.getId() + 400 : subject.getId();
//        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
//        Integer bookClazzLevel = newBookProfile != null ? newBookProfile.getClazzLevel() : null;
//        Page<PictureBook> pictureBooks = pictureBookLoaderClient.loadPictureBookByPictureBookQueryAndSubjectId(pictureBookQuery, bookClazzLevel, pageable, subjectId);
        Page<PictureBook> pictureBooks = pictureBookLoaderClient.loadPictureBookByPictureBookQueryAndBookId(pictureBookQuery, bookId, pageable);
        MapMessage message = MapMessage.successMessage();
        List<Map<String, Object>> readingMapperList = Collections.emptyList();
        List<PictureBook> pictureBookList = pictureBooks.getContent();
        EmbedBook book = new EmbedBook();
        book.setBookId(bookId);
        book.setUnitId(unitId);
        if (CollectionUtils.isNotEmpty(pictureBookList)) {
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
            List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                    .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                    .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
            readingMapperList = pictureBookList
                    .stream()
                    .filter(Objects::nonNull)
                    .map(pictureBook -> NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord))
                    .collect(Collectors.toList());
        }
        message.put("readings", readingMapperList);
        message.put("totalSize", pictureBooks.getTotalElements());
        message.put("pageCount", pictureBooks.getTotalPages());
        message.put("pageNum", pictureBooks.getNumber() + 1);
        return message;
    }

    @Override
    public MapMessage loadDubbingDetail(TeacherDetail teacherDetail, String bookId, String unitId, String dubbingId, ObjectiveConfigType objectiveConfigType) {
        Long teacherId = teacherDetail.getId();
        Subject subject = teacherDetail.getSubject();
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(subject, teacherId, bookId);
        Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(Collections.singleton(dubbingId)).get(dubbingId);
        DubbingCollectionRecord dubbingCollectionRecord = dubbingCollectionRecordDao.loadDubbingCollectionRecord(teacherId, subject);
        DubbingCategory album = dubbingLoaderClient.loadDubbingCategoriesByIds(Collections.singleton(dubbing.getCategoryId())).get(dubbing.getCategoryId());
        Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                .stream()
                .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
        Map<String, Object> map = new HashMap<>();
        if (dubbing.getCategoryId() != null) {
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            Date newDate = DateUtils.addDays(new Date(), -7);

            map = NewHomeworkContentDecorator.decorateDubbing(dubbing, album, teacherAssignmentRecord, book, newDate, objectiveConfigType, dubbingThemeMap);
            map.put("keyGrammars", dubbing.getKeyGrammars());
            map.put("teacherAssignTimes", teacherAssignmentRecord != null ? teacherAssignmentRecord.getAppInfo().getOrDefault(dubbing.getDocId(), 1) : 1);
            map.put("isCollection", dubbingCollectionRecord.getDubbingCollectionInfo().get(dubbingId) != null);
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("dubbingDetail", map);
        return mapMessage;
    }

    /**
     * 趣味配音：我的收藏
     */
    @Override
    public MapMessage loadDubbingCollectionRecord(TeacherDetail teacherDetail, String bookId, String unitId, Pageable pageable, String sys, String appVersion) {
        DubbingCollectionRecord dubbingCollectionRecord = dubbingCollectionRecordDao.loadDubbingCollectionRecord(teacherDetail.getId(), teacherDetail.getSubject());
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacherDetail.getSubject(), teacherDetail.getId(), bookId);
        List<Map<String, Object>> dubbingMapperList = new ArrayList<>();
        Page<Dubbing> dubbingPage;
        MapMessage result = MapMessage.successMessage()
                .add("dubbingList", Collections.emptyList())
                .add("totalSize", 0)
                .add("pageCount", 0)
                .add("pageNum", 0);
        if (dubbingCollectionRecord == null || MapUtils.isEmpty(dubbingCollectionRecord.getDubbingCollectionInfo())) {
            return result;
        }

        Map<String, Date> dubbingCollectionRecordMap = new LinkedHashMap<>();
        dubbingCollectionRecord.getDubbingCollectionInfo()
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .sorted(Map.Entry.<String, Date>comparingByValue().reversed())
                .forEach(e -> dubbingCollectionRecordMap.put(e.getKey(), e.getValue()));

        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIds(dubbingCollectionRecordMap.keySet());
        Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                .stream()
                .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
        if (MapUtils.isEmpty(dubbingMap)) {
            return result;
        }
        Map<String, Dubbing> sortedDubbingMap = new LinkedHashMap<>();
        dubbingCollectionRecordMap.keySet()
                .stream()
                .filter(d -> dubbingMap.get(d) != null)
                .forEach(d -> sortedDubbingMap.put(d, dubbingMap.get(d)));

        List<Dubbing> allCollectionDubbingList = new ArrayList<>(sortedDubbingMap.values());
        dubbingPage = PageableUtils.listToPage(allCollectionDubbingList, pageable);
        List<Dubbing> collectionDubbingList = dubbingPage.getContent();
        if (CollectionUtils.isNotEmpty(collectionDubbingList)) {
            Set<String> albumIds = collectionDubbingList.stream().map(Dubbing::getCategoryId).collect(Collectors.toSet());
            Map<String, DubbingCategory> albums = dubbingLoaderClient.loadDubbingCategoriesByIds(albumIds);
            Date newDate = DateUtils.addDays(new Date(), -7);
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            dubbingMapperList = collectionDubbingList
                    .stream()
                    .map(dubbing -> NewHomeworkContentDecorator.decorateDubbing(dubbing, albums.get(dubbing.getCategoryId()), teacherAssignmentRecord, book, newDate, ObjectiveConfigType.DUBBING_WITH_SCORE, dubbingThemeMap))
                    .collect(Collectors.toList());
        }

        return MapMessage.successMessage()
                .add("dubbingList", dubbingMapperList)
                .add("totalSize", dubbingPage.getTotalElements())
                .add("pageCount", dubbingPage.getTotalPages())
                .add("pageNum", dubbingPage.getNumber() + 1);
    }

    /**
     * 作业预览
     */
    @Override
    public MapMessage previewContent(Teacher teacher, String bookId, Map<String, List> contentMap) {
        if (MapUtils.isEmpty(contentMap)) {
            return MapMessage.errorMessage("预览内容不能为空");
        }
        Map<ObjectiveConfigType, Map<String, Object>> resultMap = new HashMap<>();
        contentMap.forEach((key, value) -> {
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(key);
            if (objectiveConfigType != null) {
                NewHomeworkContentLoaderTemplate template = newHomeworkContentLoaderFactory.getTemplate(objectiveConfigType);
                if (template != null && CollectionUtils.isNotEmpty(value)) {
                    List<String> contentIdList = new ArrayList<>();
                    for (Object o : value) {
                        contentIdList.add(SafeConverter.toString(o));
                    }
                    resultMap.put(objectiveConfigType, template.previewContent(teacher, bookId, contentIdList));
                }
            }
        });
        Set<ObjectiveConfigType> resultSet = resultMap.keySet();
        Subject matchSubject = ObjectiveConfigType.matchSubject(resultSet);
        List<ObjectiveConfigType> types = ObjectiveConfigType.getSubjectTypes(matchSubject);
        List<Map<String, Object>> contents = types.stream()
                .filter(resultSet::contains)
                .map(resultMap::get)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(contents)) {
            return MapMessage.errorMessage("未查询到作业内容");
        }
        return MapMessage.successMessage().add("contents", contents);
    }

    @Override
    public MapMessage loadTermReviewContentTypeList(Subject subject, String bookId, List<Long> groupIds, Boolean fromPC, String cdnUrl, Teacher teacher, String sys, String appVersion) {
        NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (bookProfile != null && NewHomeworkConstants.TERM_REVIEW_NOT_SUPPORTED_BOOK_SERIES.contains(bookProfile.getSeriesId())) {
            if (fromPC) {
                return MapMessage.errorMessage("此教材没有配套复习内容，请换本教材吧！");
            } else {
                return MapMessage.successMessage().add("contentTypes", Collections.emptyList());
            }
        }

        List<Map<String, Object>> results = new LinkedList<>();
        Set<BasicReviewContentType> contentTypes = new HashSet<>();
        boolean allGroupAssigned = true;
        // 处理基础必过相关数据
        Map<Long, List<BasicReviewHomeworkPackage>> packageMap = basicReviewHomeworkLoader.loadBasicReviewHomeworkPackageByClazzGroupIds(groupIds);
        for (Long groupId : groupIds) {
            List<BasicReviewHomeworkPackage> packageList = packageMap.get(groupId);
            if (CollectionUtils.isNotEmpty(packageList)) {
                for (BasicReviewHomeworkPackage basicReviewHomeworkPackage : packageList) {
                    contentTypes.addAll(basicReviewHomeworkPackage.getContentTypes());
                }
            } else {
                allGroupAssigned = false;
            }
        }

        List<TermReviewContentType> termReviewContentTypeList = loadSubjectTermReviewContentTypes(subject, bookId, teacher.getId());
        List<Map<String, Object>> contentTypeList = termReviewContentTypeList
                .stream()
                .filter(Objects::nonNull)
                .map(type -> buildTermReviewContentTypeMapper(type, cdnUrl))
                .collect(Collectors.toList());


        if (fromPC) {
            if (!allGroupAssigned) {
                if (Subject.ENGLISH == subject) {
                    // 判断有没有内容数据，有数据的教材才显示这个模块
                    List<EnglishReview> englishReviews = termReviewLoaderClient.getTermReviewLoader().loadEnglishReviews(bookId, TermReview.EnglishType.WORD_AND_SENTENCE);
                    if (CollectionUtils.isNotEmpty(englishReviews)) {
                        Map<String, Object> basicMapper = buildTermReviewContentTypeMapper(TermReviewContentType.BASIC_WORD, cdnUrl);
                        basicMapper.put("typeName", "基础必过");
                        results.add(basicMapper);
                    }
                } else if (Subject.MATH == subject) {
                    List<MathReview> mathReviews = termReviewLoaderClient.getTermReviewLoader().loadMathReviews(bookId);
                    if (CollectionUtils.isNotEmpty(mathReviews)) {
                        Map<String, Object> basicMapper = buildTermReviewContentTypeMapper(TermReviewContentType.BASIC_CALCULATION, cdnUrl);
                        basicMapper.put("typeName", "基础必过");
                        results.add(basicMapper);
                    }
                } else {
                    List<ChineseBasicReview> chineseBasicReviews = termReviewLoaderClient.getTermReviewLoader().loadChineseBasicReviews(bookId);
                    if (CollectionUtils.isNotEmpty(chineseBasicReviews)) {
                        Map<String, Object> basicMapper = buildTermReviewContentTypeMapper(TermReviewContentType.BASIC_READ_RECITE_WITH_SCORE, cdnUrl);
                        basicMapper.put("typeName", "基础必过");
                        results.add(basicMapper);
                    }
                }
            }
            results.addAll(contentTypeList);
        } else {
            boolean hasAssignedWord = contentTypes.contains(BasicReviewContentType.WORD);
            boolean hasAssignedSentence = contentTypes.contains(BasicReviewContentType.SENTENCE);
            if (Subject.ENGLISH == subject) {
                Map<String, Object> wordMapper = buildTermReviewContentTypeMapper(TermReviewContentType.BASIC_WORD, cdnUrl);
                Map<String, Object> sentenceMapper = buildTermReviewContentTypeMapper(TermReviewContentType.BASIC_SENTENCE, cdnUrl);
                // 所有班都布置过
                if (allGroupAssigned) {
                    if (hasAssignedWord) {
                        wordMapper.put("hasAssigned", true);
                        contentTypeList.add(wordMapper);
                    }
                    if (hasAssignedSentence) {
                        sentenceMapper.put("hasAssigned", true);
                        contentTypeList.add(sentenceMapper);
                    }
                } else {
                    // 有未布置的班级
                    // 判断有没有单词内容
                    List<EnglishReview> englishWordReviews = termReviewLoaderClient.getTermReviewLoader().loadEnglishReviews(bookId, TermReview.EnglishType.WORD_ONLY);
                    if (CollectionUtils.isNotEmpty(englishWordReviews)) {
                        wordMapper.put("hasAssigned", false);
                        contentTypeList.add(wordMapper);
                    }
                    // 判断有没有重点句内容
                    List<EnglishReview> englishSentenceReviews = termReviewLoaderClient.getTermReviewLoader().loadEnglishReviews(bookId, TermReview.EnglishType.SENTENCE_ONLY);
                    boolean hasSentenceContent = CollectionUtils.isNotEmpty(englishSentenceReviews);
                    if (hasSentenceContent) {
                        sentenceMapper.put("hasAssigned", false);
                        contentTypeList.add(sentenceMapper);
                    }
                }
            } else if (Subject.MATH == subject) {
                Map<String, Object> calculationMapper = buildTermReviewContentTypeMapper(TermReviewContentType.BASIC_CALCULATION, cdnUrl);
                // 所有班都布置过
                if (allGroupAssigned) {
                    calculationMapper.put("hasAssigned", true);
                    contentTypeList.add(calculationMapper);
                } else {
                    // 判断有没有基础必过内容，没有不显示这个模块
                    List<MathReview> mathReviews = termReviewLoaderClient.getTermReviewLoader().loadMathReviews(bookId);
                    if (CollectionUtils.isNotEmpty(mathReviews)) {
                        calculationMapper.put("hasAssigned", false);
                        contentTypeList.add(calculationMapper);
                    }
                }
            } else if (Subject.CHINESE == subject && VersionUtil.compareVersion(appVersion, "1.8.6") >= 0) {
                Map<String, Object> calculationMapper = buildTermReviewContentTypeMapper(TermReviewContentType.BASIC_READ_RECITE_WITH_SCORE, cdnUrl);
                // 所有班都布置过
                if (allGroupAssigned) {
                    calculationMapper.put("hasAssigned", true);
                    contentTypeList.add(calculationMapper);
                } else {
                    // 判断有没有基础必过内容，没有不显示这个模块
                    List<ChineseBasicReview> chineseBasicReviews = termReviewLoaderClient.getTermReviewLoader().loadChineseBasicReviews(bookId);
                    if (CollectionUtils.isNotEmpty(chineseBasicReviews)) {
                        calculationMapper.put("hasAssigned", false);
                        contentTypeList.add(calculationMapper);
                    }
                }
            }

            // 对二级模块分类
            if (CollectionUtils.isNotEmpty(contentTypeList)) {
                Set<TermReviewType> termReviewTypeSet = new HashSet<>();
                contentTypeList.forEach(e -> termReviewTypeSet.add((TermReviewType) e.get("termReviewType")));
                List<TermReviewType> termReviewTypes = termReviewTypeSet.stream()
                        .filter(e -> e.getRank() != null)
                        .sorted(Comparator.comparing(TermReviewType::getRank))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(termReviewTypes)) {
                    for (TermReviewType termReviewType : termReviewTypes) {
                        Map<String, Object> termReviewTypeMap = new LinkedHashMap<>();
                        termReviewTypeMap.put("termReviewType", termReviewType);
                        termReviewTypeMap.put("termReviewTypeName", termReviewType.getName());
                        List<Map<String, Object>> modules = new ArrayList<>();
                        for (Map<String, Object> map : contentTypeList) {
                            if (termReviewType.equals(map.get("termReviewType"))) {
                                modules.add(map);
                            }
                        }
                        termReviewTypeMap.put("modules", modules);
                        results.add(termReviewTypeMap);
                    }
                }
            }
        }
        return MapMessage.successMessage().add("contentTypes", results);
    }

    private Map<String, Object> buildTermReviewContentTypeMapper(TermReviewContentType type, String cdnUrl) {
        return MapUtils.m(
                "type", type,
                "typeName", type.getValue(),
                "typeDescription", type.getDescription(),
                "termReviewType", type.getTermReviewType(),
                "iconUrl", cdnUrl + type.getIconUrl(),
                "objectiveConfigType", type.getObjectiveConfigType(),
                "objectiveConfigTypeName", type.getObjectiveConfigType().getValue(),
                "keyName", type.getKeyName()
        );
    }

    private List<TermReviewContentType> loadSubjectTermReviewContentTypes(Subject subject, String bookId, Long teacherId) {
        List<TermReviewContentType> subjectTypeList = TermReviewContentType.getSubjectTypes(subject);
        List<TermReviewContentType> termReviewContentTypeList = new ArrayList<>();
        if (!subject.equals(Subject.CHINESE)) {
            List<AthenaReviewPackageType> athenaReviewPackageTypeList = Collections.emptyList();
            try {
                athenaReviewPackageTypeList = athenaReviewLoaderClient.getAthenaReviewLoader().getAvailablePackageTypes(bookId, teacherId);
            } catch (Exception e) {
                logger.error("NewHomeworkContentServiceImpl call athena error:", e);
            }
            if (CollectionUtils.isNotEmpty(athenaReviewPackageTypeList)) {
                for (AthenaReviewPackageType athenaReviewPackageType : athenaReviewPackageTypeList) {
                    for (TermReviewContentType termReviewContentType : subjectTypeList) {
                        String athenaName = termReviewContentType.getAthenaName();
                        AthenaReviewPackageType type = null;
                        try {
                            type = AthenaReviewPackageType.valueOf(athenaName);
                        } catch (Exception ignore) {
                        }
                        if (athenaReviewPackageType.equals(type)) {
                            termReviewContentTypeList.add(termReviewContentType);
                            break;
                        }
                    }
                }
            }
            // 数学单元重点讲练测排到前面来
            if (!termReviewContentTypeList.isEmpty() && termReviewContentTypeList.contains(TermReviewContentType.UNIT_DIAGNOSIS)) {
                termReviewContentTypeList.remove(TermReviewContentType.UNIT_DIAGNOSIS);
                termReviewContentTypeList.add(0, TermReviewContentType.UNIT_DIAGNOSIS);
            }
        } else {
            List<TermReview.ChineseModule> chineseModuleList = termReviewLoaderClient.loadChineseModules(bookId);
            if (CollectionUtils.isNotEmpty(chineseModuleList)) {
                for (TermReview.ChineseModule chineseModule : chineseModuleList) {
                    for (TermReviewContentType termReviewContentType : subjectTypeList) {
                        String athenaName = termReviewContentType.getAthenaName();
                        TermReview.ChineseModule module = null;
                        try {
                            module = TermReview.ChineseModule.valueOf(athenaName);
                        } catch (Exception ignore) {
                        }
                        if (chineseModule.equals(module)) {
                            termReviewContentTypeList.add(termReviewContentType);
                            break;
                        }
                    }
                }
            }
        }
        return termReviewContentTypeList;
    }

    @Override
    public MapMessage loadTermReviewContent(Teacher teacher, List<Long> groupIds, String bookId, TermReviewContentType termReviewContentType) {
        TermReviewContentLoaderTemplate template = termReviewContentLoaderFactory.getTemplate(termReviewContentType);
        if (template == null) {
            return MapMessage.errorMessage("unknown contentType");
        }
        return template.loadNewContent(teacher, groupIds, bookId, termReviewContentType);
    }

    @Override
    public MapMessage previewBasicReviewContent(String bookId, List<String> contentTypes, String cdnUrl) {
        return assignBasicReviewHomeworkProcessor.preview(bookId, contentTypes, cdnUrl);
    }

    @Override
    public MapMessage loadUnitProgress(Teacher teacher, Map<Long, Long> groupIdClazzIdMap, String unitId, String bookId) {
        return MapMessage.errorMessage("功能已下线");
    }

    @Override
    public MapMessage loadIntelligenceQuestion(TeacherDetail teacher, Collection<Long> groupIds, List<String> sectionIds, String bookId, String unitId,
                                               String algoType, Integer difficulty, Integer questionCount, Collection<String> kpIds, Collection<Integer> contentTypeIds,
                                               String objectiveConfigId, String type) {
        Map<Long, Collection<String>> groupBookCatalogIds = new LinkedHashMap<>();
        kpIds = kpIds.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        IntelligenceExamSceneType intelligenceExamSceneType = IntelligenceExamSceneType.of(algoType);
        if (intelligenceExamSceneType == null) {
            return MapMessage.errorMessage("未知的场景");
        }
        Subject subject = teacher.getSubject();
        if (subject == null) {
            return MapMessage.errorMessage("学科信息错误");
        }
        // 老师使用次数
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(subject, teacher.getId(), bookId);
        NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        Integer clazzLevel = bookProfile != null ? bookProfile.getClazzLevel() : 1;
        String packageId = null;
        List<RecommendPointQuestionInfo> recommendPointQuestionInfoList = null;
        //教师学校ID
        Map<String, String> teacherSchoolMap = new HashMap<>();
        teacherSchoolMap.put("SCHOOL_ID", SafeConverter.toString(teacher.getTeacherSchoolId()));
        try {
            switch (subject) {
                case ENGLISH:
                    if (CollectionUtils.isNotEmpty(groupIds)) {
                        for (Long groupId : groupIds) {
                            groupBookCatalogIds.put(groupId, Collections.singleton(unitId));
                        }
                    }
                    com.voxlearning.recom.homework.api.entity.NewRecommendPackageWrapper englishRecommendPackage = recommendedHomeworkLoaderClient.getHomeworkRecomLoader()
                            .loadEnglishRecommendedPackagesV2(bookId, groupBookCatalogIds, teacher.getId(),
                            SafeConverter.toLong(teacher.getCityCode()), algoType, 0, questionCount, kpIds, contentTypeIds, teacherSchoolMap);

                    NewRecommendPackageWrapper englishRecommendPackageWrapper = ObjectCopyUtils.copyPropertiesByJson(NewRecommendPackageWrapper.class, englishRecommendPackage);
                    if (englishRecommendPackageWrapper != null && CollectionUtils.isNotEmpty(englishRecommendPackageWrapper.getRecommendPackages())) {
                        NewRecommendPackage recommendPackage = englishRecommendPackageWrapper.getRecommendPackages().get(0);
                        packageId = recommendPackage.getId();
                        recommendPointQuestionInfoList = recommendPackage.getRecommendPointQuestionInfoList();
                    }
                    break;
                case MATH:
                    if (CollectionUtils.isNotEmpty(groupIds)) {
                        for (Long groupId : groupIds) {
                            groupBookCatalogIds.put(groupId, sectionIds);
                        }
                    }
                    NewRecommendPackageWrapper mathRecommendPackageWrapper = athenaHomeworkLoaderClient.getAthenaHomeworkLoader()
                            .loadPrimaryMathRecommendedPackagesV2(bookId, groupBookCatalogIds, teacher.getId(), algoType, difficulty, questionCount, kpIds, teacherSchoolMap);
                    if (mathRecommendPackageWrapper != null && CollectionUtils.isNotEmpty(mathRecommendPackageWrapper.getRecommendPackages())) {
                        NewRecommendPackage recommendPackage = mathRecommendPackageWrapper.getRecommendPackages().get(0);
                        packageId = recommendPackage.getId();
                        recommendPointQuestionInfoList = recommendPackage.getRecommendPointQuestionInfoList();
                    }
                    break;
                case CHINESE:
                    if (CollectionUtils.isNotEmpty(groupIds)) {
                        for (Long groupId : groupIds) {
                            groupBookCatalogIds.put(groupId, sectionIds);
                        }
                    }
                    NewRecommendPackageWrapper chineseRecommendPackageWrapper = athenaHomeworkLoaderClient.getAthenaHomeworkLoader()
                            .loadPrimaryChineseNewRecommendedPackages(bookId, clazzLevel, groupBookCatalogIds, teacher.getId(), SafeConverter.toLong(teacher.getCityCode()), algoType, questionCount, kpIds, contentTypeIds);
                    if (chineseRecommendPackageWrapper != null && CollectionUtils.isNotEmpty(chineseRecommendPackageWrapper.getRecommendPackages())) {
                        NewRecommendPackage recommendPackage = chineseRecommendPackageWrapper.getRecommendPackages().get(0);
                        packageId = recommendPackage.getId();
                        recommendPointQuestionInfoList = recommendPackage.getRecommendPointQuestionInfoList();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error("loadIntelligenceQuestion call athena error:", e);
        }
        List<Map<String, Object>> questionMapperList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(recommendPointQuestionInfoList)) {
            // 题型
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            Set<String> allQuestionDocIds = recommendPointQuestionInfoList
                    .stream()
                    .map(RecommendPointQuestionInfo::getDocId)
                    .collect(Collectors.toSet());
            Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionDocIds)
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
            Map<String, NewQuestion> docIdQuestionMap = allQuestionMap.values()
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
            if (MapUtils.isNotEmpty(allQuestionMap)) {
                EmbedBook book = new EmbedBook();
                book.setBookId(bookId);
                book.setUnitId(unitId);
                // 总的使用次数
                Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader
                        .loadTotalAssignmentRecordByContentType(subject, allQuestionMap.keySet(), HomeworkContentType.QUESTION);
                // 保证推出来的题包没有重题
                Set<String> pushQuestionDocIdSet = new HashSet<>();
                if (CollectionUtils.isNotEmpty(recommendPointQuestionInfoList)) {
                    for (RecommendPointQuestionInfo questionInfo : recommendPointQuestionInfoList) {
                        String docId = questionInfo.getDocId();
                        if (docIdQuestionMap.containsKey(docId) && pushQuestionDocIdSet.add(docId)) {
                            NewQuestion question = docIdQuestionMap.get(docId);
                            // 支持在线作答或者新口语题
                            // 支持移动端展示
                            if ((question.supportOnlineAnswer()
                                    || (intelligenceExamSceneType == IntelligenceExamSceneType.ENGLISH_ORAL_PRACTICE && questionContentTypeLoaderClient.isNewOral(question.findSubContentTypeIds())))
                                    && !Objects.equals(question.getNotFitMobile(), 1)) {
                                if (subject == Subject.MATH || subject == Subject.CHINESE) {
                                    book.setSectionId(questionInfo.getCatalogId());
                                }
                                Map<String, Object> questionMapper = NewHomeworkContentDecorator
                                        .decorateNewQuestion(question, contentTypeMap, totalAssignmentRecordMap, teacherAssignmentRecord, book);
                                questionMapperList.add(questionMapper);
                            }
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(questionMapperList)) {
                    int seconds = questionMapperList.stream()
                            .mapToInt(questionMapper -> SafeConverter.toInt(questionMapper.get("seconds")))
                            .sum();
                    if (StringUtils.isBlank(packageId)) {
                        packageId = RandomUtils.randomNumeric(5);
                    }
                    return MapMessage.successMessage()
                            .add("id", packageId)
                            .add("questions", questionMapperList)
                            .add("seconds", seconds)
                            .add("objectiveConfigId", objectiveConfigId)
                            .add("type", type);
                }
            }
        }
        return MapMessage.errorMessage("暂无合适的试题");
    }

    @Override
    public MapMessage loadObjectiveList(TeacherDetail teacher, List<String> sectionIds, String unitId, String bookId, HomeworkSourceType homeworkSourceType, String appVersion) {
        String configJson = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "ah_integral_reward");
        Map<String, Object> objMap = new HashMap<>();
        if (StringUtils.isNotBlank(configJson)) {
            objMap = JsonUtils.fromJson(configJson);
        }
        Date startAt = SafeConverter.toDate(objMap.get("startAt"));
        Date endAt = SafeConverter.toDate(objMap.get("endAt"));
        Date currentDate = new Date();

        Subject subject = teacher.getSubject();
        List<Map<String, Object>> objectiveList = new ArrayList<>();
        List<ObjectiveConfigType> assignTypes = ObjectiveConfigType.getAssignSubjectTypes(teacher.getSubject());
        NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        boolean isYiQiXue = bookProfile != null && StringUtils.equalsIgnoreCase(NewBookType.YIQIXUE.name(), bookProfile.getBookType());
        // 非17XUE教材过滤 动手做一做，概念说一说
        if (!isYiQiXue) {
            assignTypes = assignTypes.stream()
                    .filter(e -> e != ObjectiveConfigType.PHOTO_OBJECTIVE)
                    .filter(e -> e != ObjectiveConfigType.VOICE_OBJECTIVE)
                    .collect(Collectors.toList());
        }

        List<Long> planASchoolIds = loadABTestPlanASchoolIds();
        Set<Long> planASchoolIdSet = Collections.emptySet();
        if (CollectionUtils.isNotEmpty(planASchoolIds)) {
            planASchoolIdSet = new LinkedHashSet<>(planASchoolIds);
        }

        List<Long> waterfallPlanASchoolIds = loadWaterfallPlanASchoolIds();
        Set<Long> waterfallPlanASchoolIdSet = Collections.emptySet();
        if (CollectionUtils.isNotEmpty(waterfallPlanASchoolIds) && Subject.MATH == subject) {
            waterfallPlanASchoolIdSet = new LinkedHashSet<>(waterfallPlanASchoolIds);
        }

        Long schoolId = teacher.getTeacherSchoolId();
        List<TeachingObjective> teachingObjectiveList = teachingObjectiveLoaderClient
                .loadLocalTeachingObjectiveByRegionAndUnit(teacher.getRootRegionCode(), teacher.getCityCode(), teacher.getRegionCode(), schoolId, unitId);
        List<String> teachingObjectiveIdList = teachingObjectiveList
                .stream()
                .map(TeachingObjective::getId)
                .collect(Collectors.toList());
        List<AppObjectiveConfigShowConfig> configShowConfigList = getAppObjConfig();
        Map<ObjectiveConfigType, AppObjectiveConfigShowConfig> configShowConfigMap = configShowConfigList
                .stream().collect(Collectors.toMap(c -> ObjectiveConfigType.of(c.getObjectiveConfigTypeName()), Function.identity()));
        Map<String, List<ObjectiveConfig>> objectiveConfigsMap = teachingObjectiveLoaderClient.loadObjectiveConfigByTeachingObjectiveIds(teachingObjectiveIdList);
        for (TeachingObjective teachingObjective : teachingObjectiveList) {
            // ABTest
            if (MapUtils.isNotEmpty(teachingObjective.getExtras())) {
                String abTestConfigValue = SafeConverter.toString(teachingObjective.getExtras().get("abtest"));
                // 值为空，所有学校都显示
                // 值为"A"，配置A的学校显示
                // 值为"B"，配置B的学校显示
                // 值为其它，配置错误，所有学校都不显示
                if (StringUtils.isNotBlank(abTestConfigValue)) {
                    String abTestSchoolValue = planASchoolIdSet.contains(schoolId) ? "A" : "B";
                    if (!abTestSchoolValue.equals(abTestConfigValue)) {
                        continue;
                    }
                }
            }
            List<ObjectiveConfig> objectiveConfigList = objectiveConfigsMap.get(teachingObjective.getId());
            if (CollectionUtils.isNotEmpty(objectiveConfigList)) {
                List<Map<String, Object>> typeList = new ArrayList<>();
                List<ObjectiveConfigType> objectiveConfigTypes = new ArrayList<>();
                boolean waterfall = false;
                if (teachingObjective.getIsHorizontal() != null) {
                    waterfall = !teachingObjective.getIsHorizontal() && !waterfallPlanASchoolIdSet.contains(schoolId);
                }
                boolean quickEntry = SafeConverter.toBoolean(teachingObjective.getIsFast());
                for (ObjectiveConfig objectiveConfig : objectiveConfigList) {
                    if (objectiveConfig.getConfigType() == null) {
                        continue;
                    }
                    ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfig.getConfigType());
                    if (objectiveConfigType == null) {
                        continue;
                    }
                    // 过滤一起学不支持的作业形式
                    if (isYiQiXue && !NewHomeworkConstants.LIVE_CAST_HOMEWORK_SUPPORTED_TYPES.contains(objectiveConfigType)) {
                        continue;
                    }
                    //根据版本号和灰度过滤作业形式
                    if (!showObjectiveConfigType(teacher, configShowConfigMap, objectiveConfigType, appVersion)) {
                        continue;
                    }

                    // 数学和语文，根据配置包关联的sections来确认该配置包是否和当前选择的sections匹配
                    boolean related = true;
                    if ((subject == Subject.MATH || subject == Subject.CHINESE) && CollectionUtils.isNotEmpty(objectiveConfig.getRelatedCatalogs())) {
                        related = false;
                        for (EmbedBook book : objectiveConfig.getRelatedCatalogs()) {
                            // 没有关联section或者关联的section在已选的里面，则认为关联
                            if (book == null || book.getSectionId() == null || sectionIds.contains(book.getSectionId())) {
                                related = true;
                                break;
                            }
                        }
                    }
                    if (related && objectiveConfigType != null && assignTypes.contains(objectiveConfigType)) {
                        // 老版本加上一个子目标下面不能有相同作业形式的校验
                        if (HomeworkSourceType.App == homeworkSourceType && VersionUtil.compareVersion(appVersion, "1.8.0") < 0 && objectiveConfigTypes.contains(objectiveConfigType)) {
                            break;
                        }
                        objectiveConfigTypes.add(objectiveConfigType);
                        Map<String, Object> objectiveConfigMapper = MapUtils.m(
                                "objectiveConfigId", objectiveConfig.getId(),
                                "type", objectiveConfigType.name(),
                                "typeName", objectiveConfigType.getValue(),
                                "name", objectiveConfig.getName(),
                                "useNative", !waterfall && NewHomeworkConstants.ASSIGN_HOMEWORK_USE_NATIVE_TYPES.contains(objectiveConfigType));
                        if (objectiveConfigType.equals(ObjectiveConfigType.ORAL_COMMUNICATION)) {
                            objectiveConfigMapper.put("floatText", "NEW");
                        }
                        if (quickEntry) {
                            String icon = "http://cdn.17zuoye.com/gridfs/59704e30c3666e472423ac41.png";
                            String backgroundColor = "#ffc22e";
                            String textColor = "#ff6802";
                            Map<String, Object> extras = objectiveConfig.getExtras();
                            if (MapUtils.isNotEmpty(extras)) {
                                icon = SafeConverter.toString(extras.get("icon"), icon);
                                backgroundColor = SafeConverter.toString(extras.get("backgroundColor"), backgroundColor);
                                textColor = SafeConverter.toString(extras.get("textColor"), textColor);
                            }
                            objectiveConfigMapper.put("icon", icon);
                            objectiveConfigMapper.put("backgroundColor", backgroundColor);
                            objectiveConfigMapper.put("textColor", textColor);
                        }
                        typeList.add(objectiveConfigMapper);
                    }
                }
                if (CollectionUtils.isNotEmpty(typeList)) {
                    String objectiveIcon = "";
                    String activeObjectiveIcon = "";
                    if (MapUtils.isNotEmpty(teachingObjective.getExtras())) {
                        if (homeworkSourceType == HomeworkSourceType.Web) {
                            objectiveIcon = SafeConverter.toString(teachingObjective.getExtras().get("pc_unselected"));
                        } else if (homeworkSourceType == HomeworkSourceType.App) {
                            objectiveIcon = SafeConverter.toString(teachingObjective.getExtras().get("app"));
                        }
                        activeObjectiveIcon = SafeConverter.toString(teachingObjective.getExtras().get("pc_selected"));
                    }
                    Map<String, Object> objectiveMapper = MapUtils.m(
                            "objectiveId", teachingObjective.getId(),
                            "objectiveName", teachingObjective.getName(),
                            "objectiveDescription", teachingObjective.getDescription(),
                            "objectiveIcon", objectiveIcon,
                            "activeObjectiveIcon", activeObjectiveIcon,
                            "waterfall", waterfall,
                            "quickEntry", quickEntry,
                            "typeList", typeList,
                            "homeworkUrl", waterfall ? "/view/mobile/teacher/junior/homework2018/vertical_objective_config_type.vpage" : "/view/mobile/teacher/junior/homework2018/horizontal_type_switch.vpage");
                    if (HomeworkSourceType.App == homeworkSourceType && VersionUtil.compareVersion(appVersion, "1.8.0") < 0) {
                        // 作业形式页面是否由原生打开，默认false（1.8.0以前的老版本才返回这个字段）
                        objectiveMapper.put("useNative", objectiveConfigTypes.size() == 1 && NewHomeworkConstants.ASSIGN_HOMEWORK_USE_NATIVE_TYPES.contains(objectiveConfigTypes.get(0)));
                    }
                    String floatText = "";
                    if (objectiveConfigTypes.contains(ObjectiveConfigType.LEVEL_READINGS)) {
                        if (Subject.CHINESE != subject && startAt != null && endAt != null && currentDate.after(startAt) && currentDate.before(endAt)) {
                            floatText = "布置有奖";
                        }
                    } else if (objectiveConfigTypes.contains(ObjectiveConfigType.MENTAL_ARITHMETIC) && objectiveConfigTypes.size() == 1) {
                        if (startAt != null && endAt != null && currentDate.after(startAt) && currentDate.before(endAt)) {
                            floatText = "布置有奖";
                        }
                    } else if (objectiveConfigTypes.contains(ObjectiveConfigType.READ_RECITE_WITH_SCORE)
                            || objectiveConfigTypes.contains(ObjectiveConfigType.WORD_RECOGNITION_AND_READING)) {
                        floatText = "智能评分";
                    } else if (objectiveConfigTypes.contains(ObjectiveConfigType.INTELLIGENT_TEACHING)
                            || objectiveConfigTypes.contains(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)
                            || objectiveConfigTypes.contains(ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING)
                            || objectiveConfigTypes.contains(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE)
                            || objectiveConfigTypes.contains(ObjectiveConfigType.ORAL_COMMUNICATION)) {
                        floatText = "NEW";
                    }
                    if (teacher.isPrimarySchool() && StringUtils.isNotBlank(floatText)) {
                        objectiveMapper.put("floatText", floatText);
                    }
                    objectiveList.add(objectiveMapper);
                }
            }
        }
        return MapMessage.successMessage().add("objectiveList", objectiveList);
    }

    @Override
    public MapMessage loadObjectiveContent(TeacherDetail teacher, Set<Long> groupIds, List<String> sectionIds, String unitId, String bookId, ObjectiveConfigType objectiveConfigType,
                                           String objectiveConfigId, Integer currentPageNum, HomeworkSourceType homeworkSourceType, String sys, String appVersion) {
        ObjectiveConfig objectiveConfig = teachingObjectiveLoaderClient
                .loadObjectiveConfigByIds(Collections.singleton(objectiveConfigId)).get(objectiveConfigId);
        if (objectiveConfig == null || !Objects.equals(objectiveConfig.getConfigType(), objectiveConfigType.getKey())) {
            return MapMessage.errorMessage("配置包错误");
        }
        // 校验配置包对应的父目标unitId与前端传入的unitId是否一致
        TeachingObjective subTeachingObjective = teachingObjectiveLoaderClient.loadTeachingObjectById(objectiveConfig.getObjectiveId());
        if (subTeachingObjective != null) {
            TeachingObjective rootTeachingObjective = teachingObjectiveLoaderClient.loadTeachingObjectById(subTeachingObjective.getParentId());
            if (rootTeachingObjective != null) {
                EmbedBook book = rootTeachingObjective.getBook();
                if (book != null && !StringUtils.equals(book.getUnitId(), unitId)) {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", teacher.getId(),
                            "mod2", "unitId error",
                            "mod3", JsonUtils.toJson(MapUtils.m(
                                    "groupIds", groupIds,
                                    "sectionIds", sectionIds,
                                    "unitId", unitId,
                                    "bookId", bookId,
                                    "objectiveConfigUnitId", book.getUnitId(),
                                    "objectiveConfigId", objectiveConfigId,
                                    "objectiveConfigType", objectiveConfigType
                            )),
                            "op", "loadObjectiveContent"
                    ));
                    return MapMessage.errorMessage("单元错误，请返回重新选择单元!");
                }
            }
        }
        NewHomeworkContentLoaderTemplate template = newHomeworkContentLoaderFactory.getTemplate(objectiveConfigType);
        if (template == null) {
            return MapMessage.errorMessage("unsupported ObjectiveConfigType");
        }

        NewHomeworkContentLoaderMapper mapper = new NewHomeworkContentLoaderMapper();
        mapper.setTeacher(teacher);
        mapper.setGroupIds(groupIds);
        mapper.setObjectiveConfig(objectiveConfig);
        mapper.setSectionIds(sectionIds);
        mapper.setUnitId(unitId);
        mapper.setBookId(bookId);
        mapper.setCurrentPageNum(currentPageNum);
        mapper.setHomeworkSourceType(homeworkSourceType);
        mapper.setSys(sys);
        mapper.setAppVersion(appVersion);
        mapper.setWaterfall(false);

        List<Map<String, Object>> content = template.loadContent(mapper);
        return MapMessage.successMessage().add("content", content);
    }

    @Override
    public MapMessage loadExpandIndexData(TeacherDetail teacherDetail) {
        Subject subject = teacherDetail.getSubject();
        if (subject != Subject.ENGLISH) {
            return MapMessage.errorMessage("暂只支持英语学科");
        }
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(Collections.singleton(teacherDetail.getId()), true);
        Map<Long, List<Long>> clazzIdGroupIdsMap = new LinkedHashMap<>();
        teacherGroups.forEach((tId, groups) -> groups.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(tId) && CollectionUtils.isNotEmpty(group.getStudents())) {
                clazzIdGroupIdsMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>()).add(group.getId());
            }
        }));
        List<Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdGroupIdsMap.keySet())
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        Map<Integer, Set<Long>> clazzLevelGroupIdMap = new LinkedHashMap<>();
        Map<Long, Clazz> groupIdClazzMap = new HashMap<>();
        clazzList.forEach(clazz -> {
            int clazzLevel = clazz.getClazzLevel().getLevel();
            List<Long> groupIds = clazzIdGroupIdsMap.get(clazz.getId());
            if (CollectionUtils.isNotEmpty(groupIds)) {
                clazzLevelGroupIdMap.computeIfAbsent(clazzLevel, e -> new LinkedHashSet<>()).addAll(groupIds);
                groupIds.forEach(groupId -> groupIdClazzMap.put(groupId, clazz));
            }
        });
        List<Map<String, Object>> bookList = new ArrayList<>();
        if (MapUtils.isNotEmpty(clazzIdGroupIdsMap)) {
            clazzLevelGroupIdMap.forEach((k, v) -> {
                // 所有班级的最新教材
                NewClazzBookRef newClazzBookRef = newClazzBookLoaderClient.loadGroupBookRefs(v)
                        .subject(subject)
                        .toList()
                        .stream()
                        .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                        .findFirst()
                        .orElse(null);
                NewBookProfile book = null;
                if (newClazzBookRef != null) {
                    String bookId = newClazzBookRef.getBookId();
                    book = newContentLoaderClient.loadBook(bookId);
                }
                ClazzLevel clazzLevel = ClazzLevel.parse(k);
                if (book == null) {
                    String bookId = newContentLoaderClient.initializeClazzBook(subject.getId(), clazzLevel, teacherDetail.getRegionCode());
                    book = newContentLoaderClient.loadBook(bookId);
                }
                if (book != null) {
                    String clazzLevelName = clazzLevel != null ? clazzLevel.getDescription() : "";
                    List<Map<String, Object>> clazzMapperList = v
                            .stream()
                            .filter(groupIdClazzMap::containsKey)
                            .map(groupId -> {
                                Clazz clazz = groupIdClazzMap.get(groupId);
                                return MapUtils.m(
                                        "groupId", groupId,
                                        "clazzId", clazz.getId(),
                                        "clazzName", clazz.formalizeClazzName()
                                );
                            })
                            .collect(Collectors.toList());
                    bookList.add(MapUtils.m(
                            "clazzLevel", k,
                            "clazzLevelName", clazzLevelName,
                            "bookId", book.getId(),
                            "bookName", book.getName(),
                            "clazzList", clazzMapperList)
                    );
                }
            });
        }
        return MapMessage.successMessage().add("bookList", bookList);
    }

    @Override
    public MapMessage loadExpandObjectiveList(TeacherDetail teacherDetail, String bookId) {
        List<Map<String, Object>> objectiveList = new ArrayList<>();
        // 获取父目标
        List<TeachingObjective> rootTeachingObjectiveList = teachingObjectiveLoaderClient.loadRootTeachingObjectiveByBookIdAndTeachingTypes(
                teacherDetail.getRootRegionCode(), teacherDetail.getRegionCode(), teacherDetail.getCityCode(), bookId, Collections.singleton(TeachingObjective.EXTRACURRICULAR));
        // 如果配了多个，只取第一个
        if (CollectionUtils.isNotEmpty(rootTeachingObjectiveList)) {
            TeachingObjective rootTeachingObjective = rootTeachingObjectiveList.get(0);
            List<TeachingObjective> subTeachingObjectiveList = teachingObjectiveLoaderClient.loadTeachingObjectiveByParentId(rootTeachingObjective.getId());
            if (CollectionUtils.isNotEmpty(subTeachingObjectiveList)) {
                List<String> teachingObjectiveIdList = subTeachingObjectiveList.stream()
                        .map(TeachingObjective::getId)
                        .collect(Collectors.toList());

                Map<String, List<ObjectiveConfig>> objectiveConfigsMap = teachingObjectiveLoaderClient.loadObjectiveConfigByTeachingObjectiveIds(teachingObjectiveIdList);
                for (TeachingObjective teachingObjective : subTeachingObjectiveList) {
                    List<ObjectiveConfig> objectiveConfigList = objectiveConfigsMap.get(teachingObjective.getId());
                    if (CollectionUtils.isNotEmpty(objectiveConfigList)) {
                        List<Map<String, Object>> typeList = new ArrayList<>();
                        Set<ObjectiveConfigType> objectiveConfigTypes = new HashSet<>();
                        for (ObjectiveConfig objectiveConfig : objectiveConfigList) {
                            if (objectiveConfig.getConfigType() == null) {
                                continue;
                            }
                            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfig.getConfigType());
                            if (objectiveConfigType != null && objectiveConfigTypes.add(objectiveConfigType)) {
                                typeList.add(MapUtils.m(
                                        "objectiveConfigId", objectiveConfig.getId(),
                                        "type", objectiveConfigType.name(),
                                        "typeName", objectiveConfigType.getValue(),
                                        "descriptionList", objectiveConfig.getDescriptionList()));
                            }
                        }
                        if (CollectionUtils.isNotEmpty(typeList)) {
                            objectiveList.add(MapUtils.m(
                                    "objectiveId", teachingObjective.getId(),
                                    "objectiveName", teachingObjective.getName(),
                                    "typeList", typeList));
                        }
                    }
                }
            }
        }
        return MapMessage.successMessage().add("objectiveList", objectiveList);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapMessage loadExpandObjectiveContent(TeacherDetail teacherDetail, String bookId, ObjectiveConfigType objectiveConfigType, String objectiveConfigId) {
        ObjectiveConfig objectiveConfig = teachingObjectiveLoaderClient.loadObjectiveConfigByIds(Collections.singleton(objectiveConfigId)).get(objectiveConfigId);
        if (objectiveConfig == null || !Objects.equals(objectiveConfig.getConfigType(), objectiveConfigType.getKey())) {
            return MapMessage.errorMessage("配置包错误");
        }
        List<Map<String, Object>> content = new ArrayList<>();
        List<Map<String, Object>> contentList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(objectiveConfig.getContents())) {
            for (Map<String, Object> configContent : objectiveConfig.getContents()) {
                int configType = SafeConverter.toInt(configContent.get("type"));
                if (configType == ObjectiveConfig.VIDEO_QUESTION) {
                    contentList.add(configContent);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(contentList)) {
            Set<String> allVideoDocIds = new HashSet<>();
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacherDetail.getSubject(), teacherDetail.getId(), bookId);
            // 获取所有的videoDocId
            for (Map<String, Object> configContent : contentList) {
                List<Map<String, Object>> detailList = (List<Map<String, Object>>) configContent.get("detail");
                if (CollectionUtils.isNotEmpty(detailList)) {
                    for (Map<String, Object> detail : detailList) {
                        String contentId = SafeConverter.toString(detail.get("content_id"));
                        if (StringUtils.isNotBlank(contentId)) {
                            allVideoDocIds.add(contentId);
                        }
                    }
                }
            }
            Map<String, Video> videoMap = videoLoaderClient.loadVideoByDocIds(allVideoDocIds);
            if (MapUtils.isNotEmpty(videoMap)) {
                EmbedBook book = new EmbedBook();
                book.setBookId(bookId);
                for (Map<String, Object> configContent : contentList) {
                    List<Map<String, Object>> detailList = (List<Map<String, Object>>) configContent.get("detail");
                    if (CollectionUtils.isNotEmpty(detailList)) {
                        List<Map<String, Object>> videoList = new ArrayList<>();
                        for (Map<String, Object> detail : detailList) {
                            String contentId = SafeConverter.toString(detail.get("content_id"));
                            Video video = videoMap.get(contentId);
                            if (StringUtils.isNotBlank(contentId) && video != null && CollectionUtils.isNotEmpty(video.getExtracurricularTasks())) {
                                String videoName = SafeConverter.toString(detail.get("name"));
                                Map<String, Object> videoMapper = buildExpandVideoMapper(video, videoName, teacherAssignmentRecord, book);
                                if (MapUtils.isNotEmpty(videoMapper)) {
                                    videoList.add(videoMapper);
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(videoList)) {
                            content.add(MapUtils.m(
                                    "groupName", SafeConverter.toString(configContent.get("name")),
                                    "videoList", videoList)
                            );
                        }
                    }
                }
            }
        }
        return MapMessage.successMessage().add("content", content);
    }

    @Override
    public MapMessage loadExpandVideoDetail(String videoId) {
        Video video = videoLoaderClient.loadVideoIncludeDisabled(Collections.singleton(videoId)).get(videoId);
        if (video == null) {
            return MapMessage.errorMessage("视频不存在");
        }
        Map<String, Object> videoMapper = buildExpandVideoMapper(video, video.getVideoName(), null, null);
        if (MapUtils.isEmpty(videoMapper)) {
            return MapMessage.errorMessage("视频信息错误");
        }
        return MapMessage.successMessage().add("videoInfo", videoMapper);
    }

    @Override
    public List<Map<String, Object>> loadUniSoundWordScoreLevels(StudentDetail studentDetail) {
        return uniSoundScoreLevelHelper.loadWordScoreLevels(studentDetail);
    }

    @Override
    public List<Map<String, Object>> loadUniSoundSentenceScoreLevels(StudentDetail studentDetail) {
        return uniSoundScoreLevelHelper.loadSentenceScoreLevels(studentDetail);
    }

    @Override
    public List<Map<String, Object>> loadUniSoundWordTeachSentenceScoreLevels(StudentDetail studentDetail) {
        return uniSoundScoreLevelHelper.loadUniSoundWordTeachSentenceScoreLevels(studentDetail);
    }

    @Override
    public String loadImageQualityStr(StudentDetail studentDetail) {
        return imageQualityStrHelper.getImageQualityStr(studentDetail);
    }

    @Override
    public List<Map<String, Object>> loadVoxSentenceScoreLevels(StudentDetail studentDetail) {
        return voxScoreLevelHelper.loadSentenceScoreLevels(studentDetail);
    }

    @Override
    public List<Map<String, Object>> loadVoxSongScoreLevels(StudentDetail studentDetail) {
        return voxScoreLevelHelper.loadSongScoreLevels(studentDetail);
    }

    @Override
    public List<Map<String, Object>> loadVoxOralCommunicationSingleLevel(StudentDetail studentDetail) {
        return voxScoreLevelHelper.loadVoxOralCommunicationSingleLevel(studentDetail);
    }

    @Override
    public List<Map<String, Object>> loadVoxOralCommunicationTotalLevel(StudentDetail studentDetail) {
        return voxScoreLevelHelper.loadVoxOralCommunicationTotalLevel(studentDetail);
    }

    @Override
    public MapMessage loadVoiceEngineConfig(StudentDetail studentDetail, ObjectiveConfigType objectiveConfigType) {
        return voiceEngineConfigHelper.loadConfig(studentDetail, objectiveConfigType);
    }

    @Override
    public MapMessage loadPracticeDetail(Teacher teacher, String homeworkId) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
            return MapMessage.errorMessage("没有查看权限");
        }
        List<NewHomeworkPracticeContent> practices = newHomework.getPractices();
        if (CollectionUtils.isEmpty(practices)) {
            return MapMessage.errorMessage("作业内容错误");
        }
        List<Map<String, Object>> contentList = new ArrayList<>();
        int questionCount = 0;
        for (NewHomeworkPracticeContent practiceContent : practices) {
            questionCount += processContent(practiceContent, contentList);
        }
        return MapMessage.successMessage()
                .add("practices", contentList)
                .add("copiable", DateUtils.dayDiff(new Date(), newHomework.getCreateAt()) < 30)
                .add("questionCount", questionCount)
                .add("duration", newHomework.getDuration());
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapMessage loadSameLevelClazzList(Teacher teacher, String homeworkId) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (teacher.getSubject() != newHomework.getSubject()) {
            return MapMessage.errorMessage("学科错误");
        }
        if (newHomework.getType() == null) {
            return MapMessage.errorMessage("作业类型错误");
        }
        Long groupId = newHomework.getClazzGroupId();
        Group group = raikouSDK.getClazzClient()
                .getGroupLoaderClient()
                ._loadGroup(groupId).firstOrNull();
        if (group == null) {
            return MapMessage.errorMessage("作业班组不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                ._loadClazz(group.getClazzId())
                .firstOrNull();
        if (clazz == null) {
            return MapMessage.errorMessage("作业班级不存在");
        }
        int clazzLevel = clazz.getClazzLevel().getLevel();
        List<Map<String, Object>> sameLevelClazzList = Collections.emptyList();
        MapMessage message = loadTeacherClazzList(teacher, Collections.singleton(newHomework.getType()), true);
        if (message.isSuccess()) {
            List<Map<String, Object>> clazzList = (List<Map<String, Object>>) message.get("clazzList");
            for (Map<String, Object> clazzListMapper : clazzList) {
                int level = SafeConverter.toInt(clazzListMapper.get("clazzLevel"));
                if (clazzLevel == level) {
                    sameLevelClazzList = (List<Map<String, Object>>) clazzListMapper.get("clazzs");
                    if (CollectionUtils.isNotEmpty(sameLevelClazzList)) {
                        sameLevelClazzList = sameLevelClazzList.stream()
                                .map(mapper -> MapUtils.m(
                                        "clazzId", SafeConverter.toLong(mapper.get("clazzId")),
                                        "clazzName", SafeConverter.toString(mapper.get("fullName")),
                                        "groupId", SafeConverter.toLong(mapper.get("groupId")),
                                        "hasUncheckedHomework", SafeConverter.toBoolean(mapper.get("hasUncheckedHomework"))
                                ))
                                .collect(Collectors.toList());
                    }
                    break;
                }
            }
        }
        return MapMessage.successMessage().add("clazzList", sameLevelClazzList);
    }

    @Override
    public MapMessage loadNaturalSpellingContent(TeacherDetail teacherDetail, String bookId, String unitId, String objectiveConfigId, Integer level) {
        return MapMessage.successMessage().add("content", newHomeworkNaturalSpellingContentLoader.loadContentByLevel(teacherDetail, unitId, bookId, level));
    }

    @Override
    public MapMessage loadDubbingAlbumList(Integer clazzLevel) {
        // 获取所有的频道
        List<DubbingCategory> channelList = dubbingLoaderClient.loadAllChannels();
        List<Map<String, Object>> channelMapperList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(channelList)) {
            Set<String> channelIds = channelList.stream().map(DubbingCategory::getId).collect(Collectors.toCollection(LinkedHashSet::new));
            Map<String, List<DubbingCategory>> channelAlbumsMap = dubbingLoaderClient.loadDubbingCategoriesByParentIds(channelIds);
            List<Map<String, Object>> allAlbumMapperList = new ArrayList<>();
            allAlbumMapperList.add(MapUtils.m("albumId", "", "albumName", "全部专辑"));
            for (DubbingCategory channel : channelList) {
                List<DubbingCategory> albumList = channelAlbumsMap.get(channel.getId());
                if (CollectionUtils.isNotEmpty(albumList)) {
                    List<Map<String, Object>> albumMapperList = new ArrayList<>();
                    albumMapperList.add(MapUtils.m("albumId", "", "albumName", "全部专辑"));
                    albumList.stream()
                            .filter(album -> Objects.equals(album.getDifficult(), clazzLevel))
                            .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                            .forEach(album -> {
                                Map<String, Object> albumMapper = MapUtils.m(
                                        "albumId", album.getId(),
                                        "albumName", album.getName());
                                albumMapperList.add(albumMapper);
                                allAlbumMapperList.add(albumMapper);
                            });
                    if (CollectionUtils.isNotEmpty(albumMapperList)) {
                        channelMapperList.add(MapUtils.m("channelId", channel.getId(), "channelName", channel.getName(), "albumList", albumMapperList));
                    }
                }
            }
            List<Map<String, Object>> allChannelMapperList = new ArrayList<>();
            allChannelMapperList.add(MapUtils.m("channelId", "", "channelName", "全部类型", "albumList", allAlbumMapperList));
            allChannelMapperList.addAll(channelMapperList);
            channelMapperList = allChannelMapperList;
        }
        return MapMessage.successMessage().add("channelList", channelMapperList);
    }

    @Override
    public MapMessage loadDubbingRecommendSearchWords() {
        List<String> recommendSearchWords = Collections.emptyList();
        try {
            recommendSearchWords = dubbingSearchLoaderClient.getDubbingSearchLoader().getRecommendedQueryWords();
        } catch (Exception e) {
            logger.error("NewHomeworkContentServiceImpl call athena error:", e);
        }
        return MapMessage.successMessage().add("words", recommendSearchWords);
    }

    @Override
    public MapMessage searchDubbing(TeacherDetail teacherDetail, Integer clazzLevel, String searchWord, List<String> channelIds, List<String> albumIds, List<String> themeIds, String bookId, String unitId, Pageable pageable, ObjectiveConfigType objectiveConfigType) {
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("教材信息错误");
        }
        Subject subject = teacherDetail.getSubject();
        if (subject == null) {
            return MapMessage.errorMessage("请先设置学科");
        }
        Page<Dubbing> dubbingPage;
        // 判断有没有搜索词，有搜索词则调用大数据接口
        if (StringUtils.isNotEmpty(searchWord)) {
            if (StringUtils.isBlank(searchWord)) {
                return MapMessage.errorMessage("请输入要搜索的内容");
            }
            List<String> dubbingDocIds = Collections.emptyList();
            try {
                dubbingDocIds = dubbingSearchLoaderClient.getDubbingSearchLoader().queryDubbings(clazzLevel, searchWord);
            } catch (Exception e) {
                logger.error("NewHomeworkContentServiceImpl call athena error:", e);
            }
            // 保持大数据的配音顺序
            List<Dubbing> dubbingList = new ArrayList<>();
            Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByDocIds(dubbingDocIds);
            dubbingDocIds.forEach(d -> {
                if (dubbingMap.get(d) != null) {
                    dubbingList.add(dubbingMap.get(d));
                }
            });
            dubbingPage = PageableUtils.listToPage(dubbingList, pageable);
        } else {
            dubbingPage = dubbingLoaderClient.loadDubbingByPage(clazzLevel, channelIds, albumIds, themeIds, pageable);
        }
        Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                .stream()
                .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
        List<Dubbing> dubbingList = dubbingPage.getContent();
        List<Map<String, Object>> dubbingMapperList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(dubbingList)) {
            Set<String> albumIdSet = dubbingList.stream().map(Dubbing::getCategoryId).collect(Collectors.toSet());
            Map<String, DubbingCategory> albums = dubbingLoaderClient.loadDubbingCategoriesByIds(albumIdSet);
            Date newDate = DateUtils.addDays(new Date(), -7);
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacherDetail.getSubject(), teacherDetail.getId(), bookId);
            dubbingMapperList = dubbingList.stream()
                    .map(dubbing -> NewHomeworkContentDecorator.decorateDubbing(dubbing, albums.get(dubbing.getCategoryId()), teacherAssignmentRecord, book, newDate, objectiveConfigType, dubbingThemeMap))
                    .collect(Collectors.toList());
        }
        return MapMessage.successMessage()
                .add("dubbingList", dubbingMapperList)
                .add("totalSize", dubbingPage.getTotalElements())
                .add("pageCount", dubbingPage.getTotalPages())
                .add("pageNum", dubbingPage.getNumber() + 1);
    }

    @Override
    public MapMessage loadPictureBookPlusTopicList() {
        // 查询所有的主题
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookLoaderClient.loadAllPictureBookTopics()
                .stream()
                .sorted(Comparator.comparingInt(PictureBookTopic::getRank))
                .collect(Collectors.toMap(PictureBookTopic::getId, v -> v, (v1, v2) -> v2, LinkedHashMap::new));
        // 查询所有绘本
        List<PictureBookPlus> allPictureBookPlus = pictureBookPlusServiceClient.loadAllOnline()
                .stream()
                // 过滤非作业端的绘本
                .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                .collect(Collectors.toList());
        Map<String, List<PictureBookPlus>> topicPictureBookPlusMap = new HashMap<>();
        for (PictureBookPlus pictureBookPlus : allPictureBookPlus) {
            List<String> topicIds = pictureBookPlus.getTopicIds();
            if (CollectionUtils.isNotEmpty(topicIds)) {
                for (String topicId : topicIds) {
                    topicPictureBookPlusMap.computeIfAbsent(topicId, k -> new ArrayList<>()).add(pictureBookPlus);
                }
            }
        }
        List<Map<String, Object>> topicMapperList = new ArrayList<>();
        for (String topicId : pictureBookTopicMap.keySet()) {
            List<PictureBookPlus> pictureBookPlusList = topicPictureBookPlusMap.get(topicId);
            if (CollectionUtils.isNotEmpty(pictureBookPlusList)) {
                PictureBookPlus pictureBookPlus = pictureBookPlusList.get(0);
                topicMapperList.add(MapUtils.m(
                        "topicId", topicId,
                        "topicName", pictureBookTopicMap.get(topicId).getName(),
                        "pictureBookCount", pictureBookPlusList.size(),
                        "imgUrl", pictureBookPlus.getCoverThumbnailUrl()
                ));
            }
        }
        return MapMessage.successMessage().add("topicList", topicMapperList);
    }

    @Override
    public MapMessage loadPictureBookPlusSeriesList() {
        // 查询所有的系列
        Collator collator = Collator.getInstance(java.util.Locale.CHINA);
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookLoaderClient.loadAllPictureBookSeries()
                .stream()
                .sorted((a, b) -> collator.compare(a.fetchName(), b.fetchName()))
                .collect(Collectors.toMap(PictureBookSeries::getId, v -> v, (v1, v2) -> v2, LinkedHashMap::new));
        // 查询所有绘本
        List<PictureBookPlus> allPictureBookPlus = pictureBookPlusServiceClient.loadAllOnline()
                .stream()
                // 过滤非作业端的绘本
                .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                .collect(Collectors.toList());
        Map<String, List<PictureBookPlus>> seriesPictureBookPlusMap = new HashMap<>();
        for (PictureBookPlus pictureBookPlus : allPictureBookPlus) {
            String seriesId = pictureBookPlus.getSeriesId();
            seriesPictureBookPlusMap.computeIfAbsent(seriesId, k -> new ArrayList<>()).add(pictureBookPlus);
        }
        List<Map<String, Object>> seriesMapperList = new ArrayList<>();

        for (String seriesId : pictureBookSeriesMap.keySet()) {
            List<PictureBookPlus> pictureBookPlusList = seriesPictureBookPlusMap.get(seriesId);
            if (CollectionUtils.isNotEmpty(pictureBookPlusList)) {
                PictureBookPlus pictureBookPlus = pictureBookPlusList.get(0);
                seriesMapperList.add(MapUtils.m(
                        "seriesId", seriesId,
                        "seriesName", pictureBookSeriesMap.get(seriesId).fetchName(),
                        "pictureBookCount", pictureBookPlusList.size(),
                        "imgUrl", pictureBookPlus.getCoverThumbnailUrl()
                ));
            }
        }
        return MapMessage.successMessage().add("seriesList", seriesMapperList);
    }

    @Override
    public MapMessage loadPictureBookPlusRecommendSearchWords() {
        List<String> recommendSearchWords = Collections.emptyList();
        try {
            recommendSearchWords = pictureBookSearchLoaderClient.getPictureBookSearchLoader().getRecommendedQueryWords();
        } catch (Exception e) {
            logger.error("NewHomeworkContentServiceImpl call athena error:", e);
        }
        return MapMessage.successMessage().add("words", recommendSearchWords);
    }

    @Override
    public MapMessage searchPictureBookPlus(TeacherDetail teacherDetail, String clazzLevel, List<String> topicIds, List<String> seriesIds, String searchWord, String bookId, String unitId, Pageable pageable, String sys, String appVersion) {
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("教材信息错误");
        }
        NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        Subject subject = bookProfile != null ? Subject.fromSubjectId(bookProfile.getSubjectId()) : teacherDetail.getSubject();
        if (subject == null) {
            return MapMessage.errorMessage("请先设置学科");
        }
        Page<PictureBookPlus> page;
        // 判断有没有搜索词，有搜索词则调用大数据接口
        if (StringUtils.isNotEmpty(searchWord)) {
            if (StringUtils.isBlank(searchWord)) {
                return MapMessage.errorMessage("请输入要搜索的内容");
            }
            List<String> clazzLevels = new ArrayList<>();
            if (StringUtils.isNotBlank(clazzLevel)) {
                clazzLevels.add(clazzLevel);
            }
            List<String> pictureBookPlusIds = Collections.emptyList();
            try {
                pictureBookPlusIds = pictureBookSearchLoaderClient.getPictureBookSearchLoader()
                        .queryPictureBooks(clazzLevels, searchWord);
            } catch (Exception e) {
                logger.error("NewHomeworkContentServiceImpl call athena error:", e);
            }
            List<PictureBookPlus> pictureBookPluses = pictureBookPlusServiceClient.loadByIds(pictureBookPlusIds)
                    .values()
                    .stream()
                    // 过滤未发布的绘本
                    .filter(PictureBookPlus::isOnline)
                    // 过滤非作业端的绘本
                    .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                    // 过滤学科
                    .filter(pictureBookPlus -> Objects.equals(pictureBookPlus.getSubjectId(), subject.getId()))
                    .collect(Collectors.toList());
            // 过滤主题，系列
            if (CollectionUtils.isNotEmpty(topicIds)) {
                pictureBookPluses = pictureBookPluses.stream()
                        .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getTopicIds()))
                        .filter(pictureBookPlus -> pictureBookPlus.getTopicIds().stream().anyMatch(topicIds::contains))
                        .collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(seriesIds)) {
                pictureBookPluses = pictureBookPluses.stream()
                        .filter(pictureBookPlus -> StringUtils.isNotBlank(pictureBookPlus.getSeriesId()))
                        .filter(pictureBookPlus -> seriesIds.contains(pictureBookPlus.getSeriesId()))
                        .collect(Collectors.toList());
            }

            Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPluses
                    .stream()
                    .collect(Collectors.toMap(PictureBookPlus::getId, Function.identity()));

            // 保持大数据的绘本顺序
            List<PictureBookPlus> pictureBookPlusResult = new ArrayList<>();
            pictureBookPlusIds.forEach(d -> {
                if (pictureBookPlusMap.get(d) != null) {
                    pictureBookPlusResult.add(pictureBookPlusMap.get(d));
                }
            });

            page = PageableUtils.listToPage(pictureBookPlusResult, pageable);
        } else {
            // 没有搜索词调用内容接口
            PictureBookQuery pictureBookQuery = new PictureBookQuery();
            if (StringUtils.isNotBlank(clazzLevel)) {
                pictureBookQuery.setNewClazzLevels(Collections.singletonList(clazzLevel));
            }
            if (CollectionUtils.isNotEmpty(topicIds)) {
                pictureBookQuery.setTopicIds(topicIds);
            }
            if (CollectionUtils.isNotEmpty(seriesIds)) {
                pictureBookQuery.setSeriesIds(seriesIds);
            }
            page = pictureBookLoaderClient.loadPictureBookPlusByPictureBookQueryAndBookId(pictureBookQuery, bookId, pageable);
        }
        List<Map<String, Object>> pictureBookPlusMapperList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(page.getContent())) {
            List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries()
                    .stream()
                    .filter(pictureBookSeries -> Objects.equals(pictureBookSeries.getSubjectId(), subject.getId()))
                    .collect(Collectors.toList());
            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                    .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics()
                    .stream()
                    .filter(pictureBookTopic -> Objects.equals(pictureBookTopic.getSubjectId(), subject.getId()))
                    .collect(Collectors.toList());
            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                    .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(subject, teacherDetail.getId(), bookId);
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            pictureBookPlusMapperList = page.getContent()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(pictureBookPlus -> NewHomeworkContentDecorator.decoratePictureBookPlus(pictureBookPlus, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, sys, appVersion))
                    .collect(Collectors.toList());
        }
        return MapMessage.successMessage()
                .add("pictureBookList", pictureBookPlusMapperList)
                .add("totalSize", page.getTotalElements())
                .add("pageCount", page.getTotalPages())
                .add("pageNum", page.getNumber() + 1);
    }

    @Override
    public MapMessage loadPictureBookPlusHistory(TeacherDetail teacherDetail, String bookId, String unitId, Pageable pageable, String sys, String appVersion) {
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("教材信息错误");
        }
        NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        Subject subject = bookProfile != null ? Subject.fromSubjectId(bookProfile.getSubjectId()) : teacherDetail.getSubject();
        if (subject == null) {
            return MapMessage.errorMessage("请先设置学科");
        }
        Long teacherId = teacherDetail.getId();
        if (subject != teacherDetail.getSubject()) {
            Long relTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacherId, subject);
            if (relTeacherId != null) {
                teacherId = relTeacherId;
            }
        }
        PictureBookPlusHistory history = pictureBookPlusHistoryDao.load(teacherId, subject);
        List<String> pictureBookPlusIds = new ArrayList<>();
        if (history != null && MapUtils.isNotEmpty(history.getPictureBookInfo())) {
            pictureBookPlusIds = new ArrayList<>(history.getPictureBookInfo().keySet());
        }
        Page<String> page = PageableUtils.listToPage(pictureBookPlusIds, pageable);
        List<Map<String, Object>> pictureBookPlusMapperList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(page.getContent())) {
            Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(page.getContent())
                    .values()
                    .stream()
                    // 过滤未发布的绘本，担心有问题
                    .filter(PictureBookPlus::isOnline)
                    .collect(Collectors.toMap(PictureBookPlus::getId, Function.identity()));
            List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries()
                    .stream()
                    .filter(pictureBookSeries -> Objects.equals(pictureBookSeries.getSubjectId(), subject.getId()))
                    .collect(Collectors.toList());
            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                    .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics()
                    .stream()
                    .filter(pictureBookTopic -> Objects.equals(pictureBookTopic.getSubjectId(), subject.getId()))
                    .collect(Collectors.toList());
            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                    .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(subject, teacherId, bookId);
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            pictureBookPlusMapperList = pictureBookPlusIds
                    .stream()
                    .filter(pictureBookPlusMap::containsKey)
                    .map(pictureBookPlusMap::get)
                    .map(pictureBookPlus -> NewHomeworkContentDecorator.decoratePictureBookPlus(pictureBookPlus, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, sys, appVersion))
                    .collect(Collectors.toList());
        }
        return MapMessage.successMessage()
                .add("pictureBookList", pictureBookPlusMapperList)
                .add("totalSize", page.getTotalElements())
                .add("pageCount", page.getTotalPages())
                .add("pageNum", page.getNumber() + 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapMessage loadIndexRecommendContent(TeacherDetail teacher, String appVersion) {
        Subject subject = teacher.getSubject();
        // 获取老师的年级
        MapMessage clazzMessage = loadTeacherClazzList(teacher, Collections.singleton(NewHomeworkType.Normal), true);
        List<Map<String, Object>> clazzList = (List<Map<String, Object>>) clazzMessage.get("clazzList");
        for (Map<String, Object> clazzListMapper : clazzList) {
            // 获取各年级下的默认教材
            List<Map<String, Object>> clazzs = (List<Map<String, Object>>) clazzListMapper.get("clazzs");
            Map<Long, Long> clazzIdGroupIdMap = new HashMap<>();
            for (Map<String, Object> clazz : clazzs) {
                Long clazzId = SafeConverter.toLong(clazz.get("clazzId"));
                Long groupId = SafeConverter.toLong(clazz.get("groupId"));
                clazzIdGroupIdMap.put(clazzId, groupId);
            }
            MapMessage bookMessage = loadClazzBook(teacher, clazzIdGroupIdMap, false);
            String bookId = "";
            String unitId = "";
            String sectionId = "";
            if (bookMessage.isSuccess()) {
                Map<String, Object> clazzBook = (Map<String, Object>) bookMessage.get("clazzBook");
                bookId = SafeConverter.toString(clazzBook.get("bookId"));
                List<Map<String, Object>> unitList = (List<Map<String, Object>>) clazzBook.get("unitList");
                for (Map<String, Object> unit : unitList) {
                    if (SafeConverter.toBoolean(unit.get("defaultUnit"))) {
                        unitId = SafeConverter.toString(unit.get("unitId"));
                        if (subject == Subject.MATH) {
                            List<Map<String, Object>> sectionList = (List<Map<String, Object>>) unit.get("sections");
                            for (Map<String, Object> section : sectionList) {
                                if (SafeConverter.toBoolean(section.get("defaultSection"))) {
                                    sectionId = SafeConverter.toString(section.get("sectionId"));
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
            // 根据默认教材和单元获取子目标列表
            MapMessage objectiveListMassage = loadObjectiveList(teacher, Collections.singletonList(sectionId), unitId, bookId, HomeworkSourceType.App, appVersion);
            boolean includeLevelReadings = false;
            boolean includeMentalArithmetic = false;
            Map<String, Object> objective = null;
            if (objectiveListMassage.isSuccess()) {
                List<Map<String, Object>> objectiveList = (List<Map<String, Object>>) objectiveListMassage.get("objectiveList");
                if (CollectionUtils.isNotEmpty(objectiveList)) {
                    for (Map<String, Object> objectiveMapper : objectiveList) {
                        List<Map<String, Object>> typeList = (List<Map<String, Object>>) objectiveMapper.get("typeList");
                        if (CollectionUtils.isNotEmpty(typeList)) {
                            for (Map<String, Object> typeMapper : typeList) {
                                String type = SafeConverter.toString(typeMapper.get("type"));
                                if (subject == Subject.ENGLISH && StringUtils.equals(ObjectiveConfigType.LEVEL_READINGS.name(), type)) {
                                    includeLevelReadings = true;
                                    objective = objectiveMapper;
                                    break;
                                }
                                if (subject == Subject.MATH && StringUtils.equals(ObjectiveConfigType.MENTAL_ARITHMETIC.name(), type)) {
                                    includeMentalArithmetic = true;
                                    objective = objectiveMapper;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            clazzListMapper.put("bookId", bookId);
            clazzListMapper.put("unitId", unitId);
            if (subject == Subject.MATH) {
                clazzListMapper.put("sectionId", sectionId);
                clazzListMapper.put("includeMentalArithmetic", includeMentalArithmetic);
                clazzListMapper.put("desc", "口算天天练，限时有奖励；布置更方便，内容更贴心");
            } else if (subject == Subject.ENGLISH) {
                clazzListMapper.put("includeLevelReadings", includeLevelReadings);
                if (includeLevelReadings) {
                    clazzListMapper.put("recommendPictureBooks", loadRecommendPictureBooks(teacher, bookId, unitId));
                }
            }
            if (objective != null) {
                clazzListMapper.put("objective", objective);
            }
        }
        return MapMessage.successMessage().add("clazzList", clazzList);
    }

    /**
     * 获取教师班级默认教材
     *
     * @param teacher
     * @param newHomeworkTypes
     * @param filterEmptyClazz
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public MapMessage loadTeacherClazzListForRecommend(TeacherDetail teacher, Set<NewHomeworkType> newHomeworkTypes, Boolean filterEmptyClazz) {
        // 获取老师的年级
        MapMessage clazzMessage = loadTeacherClazzList(teacher, newHomeworkTypes, filterEmptyClazz);
        List<Map<String, Object>> clazzList = (List<Map<String, Object>>) clazzMessage.get("clazzList");
        for (Map<String, Object> clazzListMapper : clazzList) {
            // 获取各年级下的默认教材
            List<Map<String, Object>> clazzs = (List<Map<String, Object>>) clazzListMapper.get("clazzs");
            Map<Long, Long> clazzIdGroupIdMap = new HashMap<>();
            for (Map<String, Object> clazz : clazzs) {
                Long clazzId = SafeConverter.toLong(clazz.get("clazzId"));
                Long groupId = SafeConverter.toLong(clazz.get("groupId"));
                clazzIdGroupIdMap.put(clazzId, groupId);
            }
            MapMessage bookMessage = loadClazzBook(teacher, clazzIdGroupIdMap, false);
            String bookId = "";
            String unitId = "";
            if (bookMessage.isSuccess()) {
                Map<String, Object> clazzBook = (Map<String, Object>) bookMessage.get("clazzBook");
                bookId = SafeConverter.toString(clazzBook.get("bookId"));
                List<Map<String, Object>> unitList = (List<Map<String, Object>>) clazzBook.get("unitList");
                for (Map<String, Object> unit : unitList) {
                    if (SafeConverter.toBoolean(unit.get("defaultUnit"))) {
                        unitId = SafeConverter.toString(unit.get("unitId"));
                        break;
                    }
                }
            }
            clazzListMapper.put("bookId", bookId);
            clazzListMapper.put("unitId", unitId);
        }
        return MapMessage.successMessage().add("clazzList", clazzList);
    }

    /**
     * 教师首页推荐
     *
     * @param teacherDetail
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public MapMessage loadNewIndexRecommendContent(TeacherDetail teacherDetail, String sys, String appVersion, String cdnUrl) {
        MapMessage mapMessage = loadTeacherClazzListForRecommend(teacherDetail, Collections.singleton(NewHomeworkType.Normal), true);
        if (mapMessage.isSuccess()) {
            List<Map<String, Object>> clazzList = (List<Map<String, Object>>) mapMessage.get("clazzList");
            List<Map<String, Object>> newClazzList = new ArrayList<>();
            List<Map<String, Object>> recommendModules = new ArrayList<>();
            Map<Integer, EmbedBook> clazzLevelBookInfoMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(clazzList)) {
                for (Map<String, Object> clazz : clazzList) {
                    Integer clazzLevel = SafeConverter.toInt(clazz.get("clazzLevel"));
                    String bookId = SafeConverter.toString(clazz.get("bookId"));
                    String unitId = SafeConverter.toString(clazz.get("unitId"));
                    NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
                    int startGrade = 1;
                    int grade = 1;
                    if (bookProfile != null) {
                        startGrade = SafeConverter.toInt(bookProfile.getStartClazzLevel(), 1);
                        grade = SafeConverter.toInt(bookProfile.getClazzLevel(), 1);
                    }
                    newClazzList.add(MapUtils.m("clazzLevel", clazzLevel, "bookId", bookId, "unitId", unitId));
                    clazz.putAll(MapUtils.m("startGrade", startGrade, "grade", grade));
                    EmbedBook embedBook = new EmbedBook();
                    embedBook.setBookId(bookId);
                    embedBook.setUnitId(unitId);
                    clazzLevelBookInfoMap.put(clazzLevel, embedBook);
                }
            }
            if (CollectionUtils.isNotEmpty(newClazzList)) {
                Subject subject = teacherDetail.getSubject();
                if (Subject.ENGLISH == subject) {
                    processEnglishIndexRecommendContent(recommendModules, teacherDetail, clazzLevelBookInfoMap, appVersion, clazzList);
                } else if (Subject.MATH == subject) {
                    processMathIndexRecommendContent(recommendModules, teacherDetail, clazzLevelBookInfoMap, appVersion);
                } else {
                    processChineseIndexRecommendContent(teacherDetail, recommendModules, clazzList, cdnUrl);
                }
            }
            return MapMessage.successMessage()
                    .add("recommendModules", recommendModules)
                    .add("previewUrl", "/view/homeworkv5/previewhomework")
                    .add("objectiveId", NewHomeworkConstants.TEACHER_HOME_INDEX_RECOMMEND_OBJECTIVE_ID);
        }
        return MapMessage.errorMessage("获取班级信息失败");
    }

    /**
     * 教师端：拼装首页推荐(语文)
     */
    @SuppressWarnings("unchecked")
    private void processChineseIndexRecommendContent(TeacherDetail teacherDetail, List<Map<String, Object>> recommendModules, List<Map<String, Object>> clazzList, String cdnUrl) {
        Map<Long, String> groupIdNameMap = new LinkedHashMap<>();
        for (Map<String, Object> clazz : clazzList) {
            List<Map<String, Object>> clazzs = (List<Map<String, Object>>) clazz.get("clazzs");
            if (CollectionUtils.isNotEmpty(clazzs)) {
                for (Map<String, Object> clazzMapper : clazzs) {
                    Long groupId = SafeConverter.toLong(clazzMapper.get("groupId"));
                    String clazzName = SafeConverter.toString(clazzMapper.get("fullName"));
                    groupIdNameMap.put(groupId, clazzName);
                }
            }
        }
        MapMessage message = outsideReadingServiceImpls.loadReadingReport(teacherDetail, groupIdNameMap, cdnUrl);
        if (message.isSuccess()) {
            List<Map<String, Object>> readingReportList = (List<Map<String, Object>>) message.get("readingReportList");
            if (CollectionUtils.isNotEmpty(readingReportList)) {
                boolean useNewCore = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "TeacherApp", "wk");
                // 阅读报告模块
                recommendModules.add(MapUtils.map(
                        "module", "OutsideReadingReport",
                        "moduleName", "阅读报告",
                        "objectiveConfigType", "OutsideReadingReport",
                        "reportList", readingReportList,
                        "typeName", "阅读报告",
                        "useNewCore", useNewCore ? "wk" : false
                ));
            }
        }
    }

    /**
     * 教师端：拼装首页推荐(数学)
     */
    private void processMathIndexRecommendContent(List<Map<String, Object>> recommendModules, TeacherDetail teacherDetail, Map<Integer, EmbedBook> clazzLevelBookInfoMap, String appVersion) {
        if (StringUtils.isNotEmpty(appVersion) && VersionUtil.compareVersion(appVersion, "1.8.1") >= 0) {
            // 重点讲练测
            Map<String, Object> intelligentTeachingModule = processMathIntelligentTeachingRecommendModule(teacherDetail, clazzLevelBookInfoMap);
            if (MapUtils.isNotEmpty(intelligentTeachingModule)) {
                recommendModules.add(intelligentTeachingModule);
            }
        }
    }

    /**
     * 教师端：拼装首页推荐(数学)
     * 重点讲练测
     */
    private Map<String, Object> processMathIntelligentTeachingRecommendModule(TeacherDetail teacherDetail, Map<Integer, EmbedBook> clazzLevelBookInfoMap) {
        // 获取该教师的班级 默认教材&默认单元
        List<String> unitIds = new ArrayList<>();
        List<String> bookIds = new ArrayList<>();
        Map<String, String> unitBookMap = new HashMap<>();
        for (EmbedBook embedBook : clazzLevelBookInfoMap.values()) {
            String bookId = embedBook.getBookId();
            String unitId = embedBook.getUnitId();
            if (StringUtils.isNotBlank(bookId) && !unitIds.contains(bookId)) {
                bookIds.add(bookId);
            }
            if (StringUtils.isNotBlank(unitId) && !unitIds.contains(unitId)) {
                unitIds.add(unitId);
            }
            if (StringUtils.isNotBlank(bookId) && StringUtils.isNotBlank(unitId)) {
                unitBookMap.put(unitId, bookId);
            }
        }
        // 教师布置默认教材默认单元的题包次数
        Map<String, Integer> teacherAssignmentRecordMap = new HashMap<>();
        bookIds.forEach(b -> {
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(Subject.MATH, teacherDetail.getId(), b);
            if (teacherAssignmentRecord != null) {
                teacherAssignmentRecordMap.putAll(teacherAssignmentRecord.getPackageInfo());
            }
        });

        boolean openIntelligentTeaching = grayFunctionManagerClient.getTeacherGrayFunctionManager()
                .isWebGrayFunctionAvailable(teacherDetail, "IntelligentTeaching", "WhiteList");

        // 获取题包
        List<IntelligentDiagnosisPak> intelligentDiagnosisPaks = new ArrayList<>();
        if (openIntelligentTeaching) {
            try {
                unitIds.forEach(u -> {
                    IntelligentDiagnosisPak intelligentDiagnosisPak = wrongQuestionDiagnosisLoaderClient.getCuotizhenduanLoader().loadIntelligentDiagnosisQuestionPaks(u);
                    if (intelligentDiagnosisPak != null) {
                        intelligentDiagnosisPaks.add(intelligentDiagnosisPak);
                    }
                });
            } catch (Exception e) {
                logger.error("teacherNewHomeworkIndexRecommend call athena error:", e);
            }
        }
        List<Map<String, Object>> intelligentTeachingList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(intelligentDiagnosisPaks)) {
            intelligentTeachingList = processMathIntelligentTeaching(unitBookMap, teacherAssignmentRecordMap, intelligentDiagnosisPaks);
            // 排序(0—>……),取前4个
            intelligentTeachingList = intelligentTeachingList.stream().sorted(new AssignTimesCompartor()).limit(4).collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(intelligentTeachingList)) {
            return MapUtils.m(
                    "module", "IntelligentTeaching",
                    "moduleName", "重点讲练测",
                    "objectiveConfigType", "INTELLIGENT",
                    "typeName", ObjectiveConfigType.INTELLIGENT_TEACHING.getValue(),
                    "intelligentTeachingList", intelligentTeachingList
            );
        }
        return new HashMap<>();
    }

    private List<Map<String, Object>> processMathIntelligentTeaching(Map<String, String> unitBookMap, Map<String, Integer> teacherAssignmentRecordMap, List<IntelligentDiagnosisPak> intelligentDiagnosisPaks) {
        List<Map<String, Object>> content = new ArrayList<>();
        for (IntelligentDiagnosisPak intelligentDiagnosisPak : intelligentDiagnosisPaks) {
            String unitId = intelligentDiagnosisPak.getUnitId();
            String bookId = unitBookMap.get(unitId);
            List<IntellDiagSectionPak> sectionPaks = intelligentDiagnosisPak.getSectionPaks();
            if (CollectionUtils.isNotEmpty(sectionPaks)) {
                //布置题目信息
                Set<String> questionIds = sectionPaks
                        .stream()
                        .filter(sectionPak -> CollectionUtils.isNotEmpty(sectionPak.getVariantPaks()))
                        .map(IntellDiagSectionPak::getVariantPaks)
                        .flatMap(Collection::stream)
                        .map(IntellVariantPak::getQuestionId)
                        .collect(Collectors.toSet());
                Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(questionIds)
                        .stream()
                        .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
                //课程信息
                Set<String> courseIds = sectionPaks
                        .stream()
                        .filter(sectionPak -> CollectionUtils.isNotEmpty(sectionPak.getVariantPaks()))
                        .map(IntellDiagSectionPak::getVariantPaks)
                        .flatMap(Collection::stream)
                        .map(IntellVariantPak::getCourseId)
                        .collect(Collectors.toSet());
                Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);
                if (MapUtils.isNotEmpty(allQuestionMap) && MapUtils.isNotEmpty(intelDiagnosisCourseMap)) {
                    // 所有题型
                    Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
                    //变式信息
                    Set<String> variantIds = sectionPaks
                            .stream()
                            .filter(sectionPak -> CollectionUtils.isNotEmpty(sectionPak.getVariantPaks()))
                            .map(IntellDiagSectionPak::getVariantPaks)
                            .flatMap(Collection::stream)
                            .map(IntellVariantPak::getVariantId)
                            .collect(Collectors.toSet());
                    Map<String, IntelDiagnosisVariant> intelDiagnosisVariantMap = intelDiagnosisClient.loadIntelDiagnosisVariantByIdIncludeDisabled(variantIds);
                    //后测题信息
                    List<String> postQuestionIds = sectionPaks
                            .stream()
                            .filter(sectionPak -> CollectionUtils.isNotEmpty(sectionPak.getVariantPaks()))
                            .map(IntellDiagSectionPak::getVariantPaks)
                            .flatMap(Collection::stream)
                            .map(IntellVariantPak::getPostQuestionId)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    Map<String, NewQuestion> postQuestionMap = questionLoaderClient.loadQuestionByDocIds(postQuestionIds)
                            .stream()
                            .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
                    //教材信息
                    EmbedBook book = new EmbedBook();
                    book.setBookId(bookId);
                    book.setUnitId(unitId);

                    for (IntellDiagSectionPak sectionPak : sectionPaks) {
                        if (CollectionUtils.isNotEmpty(sectionPak.getVariantPaks())) {
                            //题包信息
                            Map<String, Object> sectionPakMap = new LinkedHashMap<>();
                            sectionPakMap.put("id", sectionPak.getSectionId());
                            sectionPakMap.put("title", sectionPak.getPackageName());
                            sectionPakMap.put("packageAssignTimes", teacherAssignmentRecordMap.get(sectionPak.getSectionId()));
                            sectionPakMap.put("objectiveConfigType", ObjectiveConfigType.INTELLIGENT_TEACHING.name());
                            sectionPakMap.put("typeName", ObjectiveConfigType.INTELLIGENT_TEACHING.getValue());
                            //此题包下的题目信息
                            Map<String, NewQuestion> questionMap = new HashMap<>();
                            sectionPak.getVariantPaks()
                                    .forEach(o -> {
                                        if (allQuestionMap.get(o.getQuestionId()) != null) {
                                            questionMap.put(o.getQuestionId(), allQuestionMap.get(o.getQuestionId()));
                                        }
                                    });
                            //变式信息 questionId + 变式信息
                            Map<String, IntelDiagnosisVariant> variantMap = new HashMap<>();
                            sectionPak.getVariantPaks()
                                    .forEach(o -> {
                                        if (intelDiagnosisVariantMap.get(o.getVariantId()) != null) {
                                            variantMap.put(o.getQuestionId(), intelDiagnosisVariantMap.get(o.getVariantId()));
                                        }
                                    });
                            //对应的课程信息 questionId + course信息
                            Map<String, IntelDiagnosisCourse> courseMap = new HashMap<>();
                            sectionPak.getVariantPaks()
                                    .forEach(o -> {
                                        if (intelDiagnosisCourseMap.get(o.getCourseId()) != null) {
                                            courseMap.put(o.getQuestionId(), intelDiagnosisCourseMap.get(o.getCourseId()));
                                        }
                                    });
                            //对应后测题信息 questionId + List<Map<String, Object>>
                            Map<String, List<Map<String, Object>>> postQuestionInfoMap = new HashMap<>();
                            sectionPak.getVariantPaks()
                                    .forEach(o -> {
                                        List<Map<String, Object>> newQuestions = new ArrayList<>();
                                        o.getPostQuestionId().forEach(q -> {
                                            if (postQuestionMap.get(q) != null) {
                                                NewQuestion newQuestion = postQuestionMap.get(q);
                                                Map<String, Object> postMap = NewHomeworkContentDecorator.decorateNewQuestion(newQuestion, new HashMap<>(), new HashMap<>(), null, book);
                                                newQuestions.add(postMap);
                                            }
                                        });
                                        postQuestionInfoMap.put(o.getQuestionId(), newQuestions);
                                    });
                            //组装Question信息
                            List<Map<String, Object>> questionList = questionMap.values().stream()
                                    .map(q -> {
                                        Map<String, Object> map = NewHomeworkContentDecorator.decorateNewQuestion(q, contentTypeMap, new HashMap<>(), null, book);
                                        map.put("variantId", variantMap.get(q.getId()).getId());
                                        map.put("variantName", variantMap.get(q.getId()).getCoreMission());
                                        map.put("courseId", courseMap.get(q.getId()).getId());
                                        map.put("courseName", courseMap.get(q.getId()).getName());
                                        map.put("postQuestions", postQuestionInfoMap.get(q.getId()));
                                        return map;
                                    })
                                    .collect(Collectors.toList());
                            if (CollectionUtils.isEmpty(questionList)) {
                                continue;
                            }
                            sectionPakMap.put("questions", questionList);
                            sectionPakMap.put("seconds", questionMap.values().stream().mapToInt(q -> SafeConverter.toInt(q.getSeconds())).sum());

                            content.add(sectionPakMap);
                        }
                    }
                }
            }
        }
        return content;
    }

    /**
     * 排序：按照题包布置次数
     */
    public static class AssignTimesCompartor implements Comparator<Map<String, Object>> {
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            Integer o1Times = SafeConverter.toInt(o1.get("packageAssignTimes"), 0);
            Integer o2Times = SafeConverter.toInt(o2.get("packageAssignTimes"), 0);
            return o1Times.compareTo(o2Times);
        }
    }

    /**
     * 教师端：拼装首页推荐(英语)
     */
    private void processEnglishIndexRecommendContent(List<Map<String, Object>> recommendModules, TeacherDetail teacherDetail, Map<Integer, EmbedBook> clazzLevelBookInfoMap, String appVersion, List<Map<String, Object>> clazzList) {

        RecommendContentCacheManager manager = newHomeworkCacheService.getRecommendContentCacheManager();

        // 获取模式
        boolean isPlanB = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "TeacherIndexRecommend", "PlanB");
        boolean isPlanC = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "TeacherIndexRecommend", "PlanC");
        // 默认年级
        int defaultClazzLevel = clazzLevelBookInfoMap.keySet().iterator().next();
        // 默认教材
        EmbedBook defaultBook = clazzLevelBookInfoMap.values().iterator().next();
        // 口语交际灰度
        boolean matchOralCommunicationConfig = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "OralCommunication", "WhiteList");
        if (StringUtils.isNotEmpty(appVersion) && VersionUtil.compareVersion(appVersion, "1.9.1") >= 0 && matchOralCommunicationConfig) {
            List<Map<String, Object>> oralCommunicationTypeMapperList = new ArrayList<>();
            List<Map<String, Object>> oralCommunicationClazzLevelMapperList = new ArrayList<>();
            List<Map<String, Object>> oralCommunicationMapperList = new ArrayList<>();
            Map<Integer, List<String>> clazzLevelStoneData = Maps.newLinkedHashMap();
            for (Map<String, Object> clazz : clazzList) {
                List clazzs = (ArrayList) clazz.get("clazzs");
                List<Long> clazzGroupIds = new ArrayList<>();
                clazzs.forEach(c -> {
                    Map<String, Object> cMap = (Map) c;
                    clazzGroupIds.add(SafeConverter.toLong(cMap.get("groupId")));
                });
                // 口语交际推荐模块
                Map<String, List<String>> recommendOralCommunicationIdsMap;
                try {
                    recommendOralCommunicationIdsMap = oralCommunicationLoaderClient.getRecommendOralCommuncationLoader()
                            .recommendOralCommunication(SafeConverter.toString(clazz.get("unitId")), SafeConverter.toString(clazz.get("bookId")),
                                    SafeConverter.toInt(clazz.get("startGrade")), SafeConverter.toInt(clazz.get("grade")), clazzGroupIds);
                } catch (Exception e) {
                    logger.error("processEnglishIndexRecommendContent call athena error: ", e);
                    return;
                }
                List<String> oralCommunicationIds = recommendOralCommunicationIdsMap.values()
                        .stream()
                        .flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList());
                clazzLevelStoneData.put(SafeConverter.toInt(clazz.get("clazzLevel")), oralCommunicationIds);
            }
            List<StoneBufferedData> stoneBufferedDataList = recommendOralCommunicationList(clazzLevelStoneData, new ArrayList<>(clazzLevelBookInfoMap.keySet()), teacherDetail);
            if (CollectionUtils.isNotEmpty(stoneBufferedDataList)) {
                oralCommunicationMapperList = stoneBufferedDataList.stream()
                        .filter(Objects::nonNull)
                        .map(oralCommunication -> NewHomeworkContentDecorator.decorateOralCommunicationSummary(oralCommunication, defaultBook, null))
                        .collect(Collectors.toList());

                oralCommunicationClazzLevelMapperList.add(MapUtils.m("levelId", "", "levelName", "全部年级"));
                for (OralCommunicationClazzLevel oralCommunicationClazzLevel : OralCommunicationClazzLevel.values()) {
                    oralCommunicationClazzLevelMapperList.add(MapUtils.m("levelId", oralCommunicationClazzLevel.name(), "levelName", oralCommunicationClazzLevel.getName()));
                }

                oralCommunicationTypeMapperList.add(MapUtils.m("typeId", "", "typeName", "全部类型"));
                for (OralCommunicationContentType oralCommunicationContentType : OralCommunicationContentType.values()) {
                    oralCommunicationTypeMapperList.add(MapUtils.m("typeId", oralCommunicationContentType.name(), "typeName", oralCommunicationContentType.getName()));
                }
            }

            Map<String, Object> oralCommunicationModule = MapUtils.m(
                    "module", "OralCommunication",
                    "moduleName", "口语交际",
                    "objectiveConfigType", ObjectiveConfigType.ORAL_COMMUNICATION.name(),
                    "typeName", ObjectiveConfigType.ORAL_COMMUNICATION.getValue(),
                    "oralCommunicationList", oralCommunicationMapperList,
                    "defaultBookId", defaultBook.getBookId(),
                    "defaultUnitId", defaultBook.getUnitId(),
                    "clazzLevelList", oralCommunicationClazzLevelMapperList,
                    "oralTypeList", oralCommunicationTypeMapperList
            );
            if (CollectionUtils.isNotEmpty(oralCommunicationMapperList)) {
                recommendModules.add(oralCommunicationModule);
            }
        }

        // 获取所有绘本系列（按照名称字典序）
        Collator collator = Collator.getInstance(Locale.CHINA);
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries()
                .stream()
                .filter(pictureBookSeries -> Objects.equals(pictureBookSeries.getSubjectId(), teacherDetail.getSubject().getId()))
                .sorted((a, b) -> collator.compare(a.fetchName(), b.fetchName()))
                .collect(Collectors.toList());
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
        // 获取所有主题（按照rank排序）
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics()
                .stream()
                .filter(pictureBookTopic -> Objects.equals(pictureBookTopic.getSubjectId(), teacherDetail.getSubject().getId()))
                .sorted(Comparator.comparingInt(PictureBookTopic::getRank))
                .collect(Collectors.toList());
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
        // 绘本年级列表
        List<PictureBookNewClazzLevel> pictureBookNewClazzLevels = PictureBookNewClazzLevel.primarySchoolLevels();
        List<Map<String, Object>> clazzLevelMapperList = new ArrayList<>();
        clazzLevelMapperList.add(MapUtils.m("levelId", "", "levelName", "全部年级"));
        pictureBookNewClazzLevels.forEach(level -> clazzLevelMapperList.add(MapUtils.m("levelId", level.name(), "levelName", level.getLevelName())));
        // 绘本系列列表
        List<Map<String, Object>> seriesMapperList = new ArrayList<>();
        seriesMapperList.add(MapUtils.m("seriesId", "", "seriesName", "全部系列"));
        pictureBookSeriesList.forEach(series -> seriesMapperList.add(MapUtils.m("seriesId", series.getId(), "seriesName", series.fetchName())));
        // 绘本主题列表
        List<Map<String, Object>> topicMapperList = new ArrayList<>();
        topicMapperList.add(MapUtils.m("topicId", "", "topicName", "全部主题"));
        pictureBookTopicList.forEach(topic -> topicMapperList.add(MapUtils.m("topicId", topic.getId(), "topicName", topic.getName())));

        PictureBookPlusHistory pictureBookPlusHistory = pictureBookPlusHistoryDao.load(teacherDetail.getId(), teacherDetail.getSubject());
        if (isPlanB || isPlanC) {
            // 主题绘本推荐模块
            List<PictureBookPlus> topicRecommendPictureBookPlusList = recommendTopicPictureBookPlusList(clazzLevelBookInfoMap, pictureBookPlusHistory);
            if (CollectionUtils.isNotEmpty(topicRecommendPictureBookPlusList)) {
                Map<String, Object> topicPictureBookModule = processPictureBookRecommendModule("TopicPictureBook", "节日主题阅读",
                        topicRecommendPictureBookPlusList, clazzLevelBookInfoMap, defaultBook, pictureBookSeriesMap, pictureBookTopicMap, pictureBookPlusHistory, clazzLevelMapperList, seriesMapperList, topicMapperList);
                recommendModules.add(topicPictureBookModule);
            }
        }

        if (isPlanB) {
            // 知名绘本推荐模块
            List<String> pictureBookPlusIds = manager.loadRecommendPictureIds(teacherDetail.getId());
            if (CollectionUtils.isEmpty(pictureBookPlusIds)) {
                List<PictureBookPlus> allPictureBookPlus = pictureBookPlusServiceClient.toMap()
                        .values()
                        .stream()
                        .filter(PictureBookPlus::isOnline)
                        .filter(pictureBookPlus -> pictureBookPlus.getSubjectId() == teacherDetail.getSubject().getId())
                        // 过滤非作业端的绘本
                        .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK)).collect(Collectors.toList());
                List<PictureBookPlus> averagedPictureBookPlus = recommendFamousPictureBookPlusList(allPictureBookPlus, new ArrayList<>(clazzLevelBookInfoMap.keySet()), teacherDetail);
                pictureBookPlusIds = averagedPictureBookPlus.stream().map(BasePictureBook::getId).collect(Collectors.toList());
                manager.saveRecommendPictureIds(pictureBookPlusIds, teacherDetail.getId());
            }
            Map<String, PictureBookPlus> famousRecommendPbMap = pictureBookPlusServiceClient.loadByIds(pictureBookPlusIds);
            if (MapUtils.isNotEmpty(famousRecommendPbMap)) {
                Map<String, Object> famousPictureBookModule = processPictureBookRecommendModule("FamousPictureBook", "知名绘本",
                        famousRecommendPbMap.values(), clazzLevelBookInfoMap, defaultBook, pictureBookSeriesMap, pictureBookTopicMap, pictureBookPlusHistory, clazzLevelMapperList, seriesMapperList, topicMapperList);
                recommendModules.add(famousPictureBookModule);
            }
        }

        if (isPlanB || isPlanC) {
            // 趣配音推荐模块
            processDubbingRecommendModule(recommendModules, teacherDetail, clazzLevelBookInfoMap, appVersion, manager, defaultClazzLevel, defaultBook);
        }

        if (isPlanB || isPlanC) {
            // 自然拼读推荐模块
            List<Map<String, Object>> levelList = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                levelList.add(MapUtils.m("level", i, "levelName", "Level " + i));
            }
            Map<String, Object> naturalSpellingModule = MapUtils.m(
                    "module", "NaturalSpelling",
                    "moduleName", "自然拼读",
                    "objectiveConfigType", ObjectiveConfigType.NATURAL_SPELLING.name(),
                    "typeName", ObjectiveConfigType.NATURAL_SPELLING.getValue(),
                    "levelList", levelList
            );
            recommendModules.add(naturalSpellingModule);
        }
    }

    /**
     * 趣味配音推荐模块
     */
    private void processDubbingRecommendModule(List<Map<String, Object>> recommendModules, TeacherDetail teacherDetail, Map<Integer, EmbedBook> clazzLevelBookInfoMap, String appVersion, RecommendContentCacheManager manager, int defaultClazzLevel, EmbedBook defaultBook) {
        // 获取所有的主题
        List<DubbingTheme> dubbingThemeList = dubbingLoaderClient.loadAllDubbingThemes();
        List<Map<String, Object>> dubbingThemeMapperList = new ArrayList<>();
        dubbingThemeMapperList.add(MapUtils.m("themeId", "", "themeName", "全部主题"));
        dubbingThemeList.forEach(dubbingTheme -> dubbingThemeMapperList.add(MapUtils.m("themeId", dubbingTheme.getId(), "themeName", dubbingTheme.getName())));
        List<Dubbing> recommendDubbingList;
        List<String> recommendDubbingIds = manager.loadRecommendDubbingIds(teacherDetail.getId());
        if (CollectionUtils.isEmpty(recommendDubbingIds)) {
            recommendDubbingList = recommendDubbingList(teacherDetail, clazzLevelBookInfoMap);
            recommendDubbingIds = recommendDubbingList.stream().map(Dubbing::getDocId).collect(Collectors.toList());
            manager.saveRecommendDubbingIds(recommendDubbingIds, teacherDetail.getId());
        } else {
            Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByDocIds(recommendDubbingIds);
            recommendDubbingList = new ArrayList<>(dubbingMap.values());
        }
        if (CollectionUtils.isNotEmpty(recommendDubbingList)) {
            Set<String> albumIds = recommendDubbingList.stream().map(Dubbing::getCategoryId).collect(Collectors.toSet());
            Map<String, DubbingCategory> albums = dubbingLoaderClient.loadDubbingCategoriesByIds(albumIds);
            Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                    .stream()
                    .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
            ObjectiveConfigType objectiveConfigType;
            boolean matchDubbingWithScoreConfig = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "DubbingWithScore", "WhiteList");
            if (VersionUtil.compareVersion(appVersion, "1.8.4") >= 0 && matchDubbingWithScoreConfig) {
                objectiveConfigType = ObjectiveConfigType.DUBBING_WITH_SCORE;
            } else {
                objectiveConfigType = ObjectiveConfigType.DUBBING;
            }
            List<Map<String, Object>> dubbingList = recommendDubbingList.stream()
                    .map(dubbing -> {
                        int difficult = SafeConverter.toInt(dubbing.getDifficult(), 1);
                        EmbedBook book = clazzLevelBookInfoMap.getOrDefault(difficult, defaultBook);
                        return NewHomeworkContentDecorator.decorateDubbing(dubbing, albums.get(dubbing.getCategoryId()), null, book, null, objectiveConfigType, dubbingThemeMap);
                    })
                    .collect(Collectors.toList());
            Map<String, Object> dubbingModule = MapUtils.m(
                    "module", "Dubbing",
                    "moduleName", "趣味配音",
                    "objectiveConfigType", objectiveConfigType.name(),
                    "typeName", objectiveConfigType.getValue(),
                    "dubbingList", dubbingList,
                    "dubbingThemeList", dubbingThemeMapperList,
                    "defaultClazzLevel", defaultClazzLevel,
                    "defaultBookId", defaultBook.getBookId(),
                    "defaultUnitId", defaultBook.getUnitId()
            );
            recommendModules.add(dubbingModule);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapMessage loadRecommendJumpParams(TeacherDetail teacherDetail, ObjectiveConfigType objectiveConfigType, String sys, String appVersion, String id) {
        MapMessage mapMessage = loadTeacherClazzListForRecommend(teacherDetail, Collections.singleton(NewHomeworkType.Normal), true);
        if (mapMessage.isSuccess()) {
            Map<String, Object> paramsMap = new LinkedHashMap<>();
            List<Map<String, Object>> clazzList = (List<Map<String, Object>>) mapMessage.get("clazzList");
            List<Map<String, Object>> newClazzList = new ArrayList<>();
            Map<Integer, EmbedBook> clazzLevelBookInfoMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(clazzList)) {
                for (Map<String, Object> clazz : clazzList) {
                    Integer clazzLevel = SafeConverter.toInt(clazz.get("clazzLevel"));
                    String bookId = SafeConverter.toString(clazz.get("bookId"));
                    String unitId = SafeConverter.toString(clazz.get("unitId"));
                    newClazzList.add(MapUtils.m("clazzLevel", clazzLevel, "bookId", bookId, "unitId", unitId));
                    EmbedBook embedBook = new EmbedBook();
                    embedBook.setBookId(bookId);
                    embedBook.setUnitId(unitId);
                    clazzLevelBookInfoMap.put(clazzLevel, embedBook);
                }
            }
            if (CollectionUtils.isNotEmpty(newClazzList)) {
                // 默认年级
                int defaultClazzLevel = clazzLevelBookInfoMap.keySet().iterator().next();
                // 默认教材
                EmbedBook defaultBook = clazzLevelBookInfoMap.values().iterator().next();

                if (objectiveConfigType == null || ObjectiveConfigType.LEVEL_READINGS == objectiveConfigType) {
                    // 获取所有绘本系列（按照名称字典序）
                    Collator collator = Collator.getInstance(java.util.Locale.CHINA);
                    List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries()
                            .stream()
                            .filter(pictureBookSeries -> Objects.equals(pictureBookSeries.getSubjectId(), teacherDetail.getSubject().getId()))
                            .sorted((a, b) -> collator.compare(a.fetchName(), b.fetchName()))
                            .collect(Collectors.toList());
                    // 获取所有主题（按照rank排序）
                    List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics()
                            .stream()
                            .filter(pictureBookTopic -> Objects.equals(pictureBookTopic.getSubjectId(), teacherDetail.getSubject().getId()))
                            .sorted(Comparator.comparingInt(PictureBookTopic::getRank))
                            .collect(Collectors.toList());
                    // 绘本年级列表
                    List<PictureBookNewClazzLevel> pictureBookNewClazzLevels = PictureBookNewClazzLevel.primarySchoolLevels();
                    List<Map<String, Object>> clazzLevelMapperList = new ArrayList<>();
                    clazzLevelMapperList.add(MapUtils.m("levelId", "", "levelName", "全部年级"));
                    pictureBookNewClazzLevels.forEach(level -> clazzLevelMapperList.add(MapUtils.m("levelId", level.name(), "levelName", level.getLevelName())));
                    // 绘本系列列表
                    List<Map<String, Object>> seriesMapperList = new ArrayList<>();
                    seriesMapperList.add(MapUtils.m("seriesId", "", "seriesName", "全部系列"));
                    pictureBookSeriesList.forEach(series -> seriesMapperList.add(MapUtils.m("seriesId", series.getId(), "seriesName", series.fetchName())));
                    // 绘本主题列表
                    List<Map<String, Object>> topicMapperList = new ArrayList<>();
                    topicMapperList.add(MapUtils.m("topicId", "", "topicName", "全部主题"));
                    pictureBookTopicList.forEach(topic -> topicMapperList.add(MapUtils.m("topicId", topic.getId(), "topicName", topic.getName())));

                    Map<String, Object> levelReadingsMapper = MapUtils.m(
                            "clazzLevelList", clazzLevelMapperList,
                            "seriesList", seriesMapperList,
                            "topicList", topicMapperList,
                            "previewUrl", UrlUtils.buildUrlQuery(TopLevelDomain.getHttpsMainSiteBaseUrl() + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html", MapUtils.m("from", "preview")),
                            "defaultBookId", defaultBook.getBookId(),
                            "defaultUnitId", defaultBook.getUnitId(),
                            "objectiveConfigType", ObjectiveConfigType.LEVEL_READINGS.name(),
                            "typeName", ObjectiveConfigType.LEVEL_READINGS.getValue()
                    );
                    if (ObjectiveConfigType.LEVEL_READINGS == objectiveConfigType) {
                        PictureBookPlus pictureBookPlus = pictureBookPlusServiceClient.loadById(id);
                        if (pictureBookPlus != null) {
                            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                                    .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
                            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                                    .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
                            Map<String, Object> pictureBookPlusMapper = NewHomeworkContentDecorator
                                    .decoratePictureBookPlus(pictureBookPlus, pictureBookSeriesMap, pictureBookTopicMap, defaultBook, null, null, null);
                            levelReadingsMapper.put("pictureBook", pictureBookPlusMapper);
                        }
                    }
                    paramsMap.put(ObjectiveConfigType.LEVEL_READINGS.name(), levelReadingsMapper);
                }

                if (objectiveConfigType == null || ObjectiveConfigType.DUBBING == objectiveConfigType) {
                    // 配音跳转参数
                    List<DubbingTheme> dubbingThemeList = dubbingLoaderClient.loadAllDubbingThemes();
                    List<Map<String, Object>> dubbingThemeMapperList = new ArrayList<>();
                    dubbingThemeMapperList.add(MapUtils.m("themeId", "", "themeName", "全部"));
                    dubbingThemeList.forEach(dubbingTheme -> dubbingThemeMapperList.add(MapUtils.m("themeId", dubbingTheme.getId(), "themeName", dubbingTheme.getName())));
                    boolean matchDubbingWithScoreConfig = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "DubbingWithScore", "WhiteList");
                    if (VersionUtil.compareVersion(appVersion, "1.8.4") >= 0 && matchDubbingWithScoreConfig) {
                        Map<String, Object> dubbingMapper = MapUtils.m(
                                "dubbingThemeList", dubbingThemeMapperList,
                                "defaultClazzLevel", defaultClazzLevel,
                                "defaultBookId", defaultBook.getBookId(),
                                "defaultUnitId", defaultBook.getUnitId(),
                                "objectiveConfigType", ObjectiveConfigType.DUBBING_WITH_SCORE.name(),
                                "typeName", ObjectiveConfigType.DUBBING_WITH_SCORE.getValue()
                        );
                        paramsMap.put(ObjectiveConfigType.DUBBING.name(), dubbingMapper);
                    } else {
                        Map<String, Object> dubbingMapper = MapUtils.m(
                                "dubbingThemeList", dubbingThemeMapperList,
                                "defaultClazzLevel", defaultClazzLevel,
                                "defaultBookId", defaultBook.getBookId(),
                                "defaultUnitId", defaultBook.getUnitId(),
                                "objectiveConfigType", ObjectiveConfigType.DUBBING.name(),
                                "typeName", ObjectiveConfigType.DUBBING.getValue()
                        );
                        paramsMap.put(ObjectiveConfigType.DUBBING.name(), dubbingMapper);
                    }
                }

                if (objectiveConfigType == null || ObjectiveConfigType.NATURAL_SPELLING == objectiveConfigType) {
                    // 自然拼读跳转参数
                    List<Map<String, Object>> levelList = new ArrayList<>();
                    for (int i = 1; i <= 3; i++) {
                        levelList.add(MapUtils.m("level", i, "levelName", "Level " + i));
                    }
                    Map<String, Object> naturalSpellingMapper = MapUtils.m(
                            "levelList", levelList,
                            "objectiveConfigType", ObjectiveConfigType.NATURAL_SPELLING.name(),
                            "typeName", ObjectiveConfigType.NATURAL_SPELLING.getValue()
                    );
                    paramsMap.put(ObjectiveConfigType.NATURAL_SPELLING.name(), naturalSpellingMapper);
                }

                if (StringUtils.isNotEmpty(appVersion) && VersionUtil.compareVersion(appVersion, "1.9.1") >= 0 && (objectiveConfigType == null || ObjectiveConfigType.ORAL_COMMUNICATION == objectiveConfigType)) {
                    // 口语交际跳转参数
                    List<Map<String, Object>> clazzLevelMapperList = new ArrayList<>();
                    clazzLevelMapperList.add(MapUtils.m("levelId", "", "levelName", "全部年级"));
                    for (OralCommunicationClazzLevel oralCommunicationClazzLevel : OralCommunicationClazzLevel.values()) {
                        clazzLevelMapperList.add(MapUtils.m("levelId", oralCommunicationClazzLevel.name(), "levelName", oralCommunicationClazzLevel.getName()));
                    }
                    List<Map<String, Object>> contentTypeMapperList = new ArrayList<>();
                    contentTypeMapperList.add(MapUtils.m("typeId", "", "typeName", "全部类型"));
                    for (OralCommunicationContentType oralCommunicationContentType : OralCommunicationContentType.values()) {
                        contentTypeMapperList.add(MapUtils.m("typeId", oralCommunicationContentType.name(), "typeName", oralCommunicationContentType.getName()));
                    }
                    Map<String, Object> oralCommunicationMapper = MapUtils.m(
                            "clazzLevelList", clazzLevelMapperList,
                            "oralTypeList", contentTypeMapperList,
                            "defaultBookId", defaultBook.getBookId(),
                            "defaultUnitId", defaultBook.getUnitId(),
                            "objectiveConfigType", ObjectiveConfigType.ORAL_COMMUNICATION.name(),
                            "typeName", ObjectiveConfigType.ORAL_COMMUNICATION.getValue()
                    );
                    paramsMap.put(ObjectiveConfigType.ORAL_COMMUNICATION.name(), oralCommunicationMapper);
                }
                return MapMessage.successMessage()
                        .add("params", paramsMap)
                        .add("previewUrl", "/view/homeworkv5/previewhomework")
                        .add("objectiveId", NewHomeworkConstants.TEACHER_HOME_INDEX_RECOMMEND_OBJECTIVE_ID);
            } else {
                return MapMessage.errorMessage("请添加学生才可以布置口语交际哦~");
            }
        }
        return MapMessage.errorMessage("获取班级信息失败");
    }

    private Map<String, Object> processPictureBookRecommendModule(String module, String moduleName, Collection<PictureBookPlus> pictureBookPluses, Map<Integer, EmbedBook> clazzLevelBookMap, EmbedBook defaultBook,
                                                                  Map<String, PictureBookSeries> pictureBookSeriesMap, Map<String, PictureBookTopic> pictureBookTopicMap, PictureBookPlusHistory pictureBookPlusHistory,
                                                                  List<Map<String, Object>> clazzLevelMapperList, List<Map<String, Object>> seriesMapperList, List<Map<String, Object>> topicMapperList) {
        Map<String, Date> pictureBookInfo = pictureBookPlusHistory != null && MapUtils.isNotEmpty(pictureBookPlusHistory.getPictureBookInfo()) ? pictureBookPlusHistory.getPictureBookInfo() : new HashMap<>();
        Comparator<PictureBookPlus> comparator = (p1, p2) -> {
            int assigned1 = pictureBookInfo.containsKey(p1.getId()) ? 1 : 0;
            int assigned2 = pictureBookInfo.containsKey(p2.getId()) ? 1 : 0;
            if (assigned1 == assigned2) {
                PictureBookNewClazzLevel clazzLevel1 = null;
                if (CollectionUtils.isNotEmpty(p1.getNewClazzLevels())) {
                    clazzLevel1 = p1.getNewClazzLevels().get(0);
                }
                PictureBookNewClazzLevel clazzLevel2 = null;
                if (CollectionUtils.isNotEmpty(p2.getNewClazzLevels())) {
                    clazzLevel2 = p2.getNewClazzLevels().get(0);
                }
                int id1 = clazzLevel1 != null ? clazzLevel1.getLevelNum() : 1;
                int id2 = clazzLevel2 != null ? clazzLevel2.getLevelNum() : 1;
                return Integer.compare(id1, id2);
            }
            return Integer.compare(assigned1, assigned2);
        };
        List<Map<String, Object>> pbList = pictureBookPluses.stream()
                .sorted(comparator)
                .map(pictureBookPlus -> {
                    PictureBookNewClazzLevel clazzLevel = null;
                    if (CollectionUtils.isNotEmpty(pictureBookPlus.getNewClazzLevels())) {
                        clazzLevel = pictureBookPlus.getNewClazzLevels().get(0);
                    }
                    EmbedBook book = clazzLevel != null ? clazzLevelBookMap.getOrDefault(clazzLevel.getLevelNum(), defaultBook) : defaultBook;
                    return NewHomeworkContentDecorator.decoratePictureBookPlus(pictureBookPlus, pictureBookSeriesMap, pictureBookTopicMap, book, null, null, null);
                })
                .collect(Collectors.toList());
        return MapUtils.m(
                "module", module,
                "moduleName", moduleName,
                "objectiveConfigType", ObjectiveConfigType.LEVEL_READINGS.name(),
                "typeName", ObjectiveConfigType.LEVEL_READINGS.getValue(),
                "pictureBookList", pbList,
                "clazzLevelList", clazzLevelMapperList,
                "seriesList", seriesMapperList,
                "topicList", topicMapperList,
                "previewUrl", UrlUtils.buildUrlQuery(TopLevelDomain.getHttpsMainSiteBaseUrl() + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html", MapUtils.m("from", "preview")),
                "defaultBookId", defaultBook.getBookId(),
                "defaultUnitId", defaultBook.getUnitId()
        );
    }


    private List<PictureBookPlus> recommendTopicPictureBookPlusList(Map<Integer, EmbedBook> clazzLevelBookInfoMap, PictureBookPlusHistory pictureBookPlusHistory) {
        List<PictureBookPlus> recommendPictureBookPlusList = new ArrayList<>();
        Set<String> recommendIds = new HashSet<>();
        List<String> unitIds = new ArrayList<>();
        for (EmbedBook embedBook : clazzLevelBookInfoMap.values()) {
            String unitId = embedBook.getUnitId();
            if (StringUtils.isNotBlank(unitId) && !unitIds.contains(unitId)) {
                unitIds.add(unitId);
            }
        }
        Map<String, Date> pictureBookInfo = pictureBookPlusHistory != null && MapUtils.isNotEmpty(pictureBookPlusHistory.getPictureBookInfo()) ? pictureBookPlusHistory.getPictureBookInfo() : new HashMap<>();
        Comparator<PictureBookPlus> assignedComparator = (p1, p2) -> {
            int assigned1 = pictureBookInfo.containsKey(p1.getId()) ? 1 : 0;
            int assigned2 = pictureBookInfo.containsKey(p2.getId()) ? 1 : 0;
            return Integer.compare(assigned1, assigned2);
        };
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyList();
        }
        int unitIdSize = unitIds.size();
        int averageSize = 6 / unitIdSize;
        int mod = 6 % unitIdSize;
        for (int i = 1; i <= unitIdSize; i++) {
            int recommendSize = averageSize;
            if (i <= mod) {
                recommendSize += 1;
            }
            String unitId = unitIds.get(i - 1);
            List<String> pictureBookPlusIds = pictureBookLoaderClient.loadRecommendPictureBookPlusIdsForTopic(unitId);
            List<PictureBookPlus> pictureBookPlusList = new ArrayList<>(pictureBookPlusServiceClient.loadByIds(pictureBookPlusIds).values());
            if (CollectionUtils.isNotEmpty(pictureBookPlusList)) {
                pictureBookPlusList = pictureBookPlusList.stream()
                        .sorted(assignedComparator)
                        .collect(Collectors.toList());
                int recommendedSize = 0;
                for (PictureBookPlus pictureBookPlus : pictureBookPlusList) {
                    if (recommendedSize < recommendSize) {
                        String id = pictureBookPlus.getId();
                        if (!recommendIds.contains(id)) {
                            recommendedSize++;
                            recommendIds.add(id);
                            recommendPictureBookPlusList.add(pictureBookPlus);
                        }
                    }
                }
            }
        }
        return recommendPictureBookPlusList;
    }

    private List<StoneBufferedData> recommendOralCommunicationList(Map<Integer, List<String>> clazzLevelStoneData, List<Integer> clazzLevels, TeacherDetail teacherDetail) {
        List<StoneBufferedData> recommendList = Lists.newArrayList();
        OralCommunicationRecommendRecord oralCommunicationRecommendRecord = oralCommunicationRecommendRecordLoader.loadOralCommunicationRecommendRecord(teacherDetail.getSubject(), teacherDetail.getId());
        List<String> stoneIds = clazzLevelStoneData.values().stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(stoneIds)) {
            return recommendList;
        }
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(stoneIds);
        Map<String, StoneBufferedData> stoneBufferedDataMap = stoneBufferedDataList.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getDeletedAt() == null)
                .filter(data -> {
                    BaseOralPractice baseOralPractice = null;
                    if (data.getOralPracticeConversion() != null) {
                        baseOralPractice = data.getOralPracticeConversion();
                    }
                    if (data.getInteractivePictureBook() != null) {
                        baseOralPractice = data.getInteractivePictureBook();
                    }
                    if (data.getInteractiveVideo() != null) {
                        baseOralPractice = data.getInteractiveVideo();
                    }
                    return baseOralPractice != null && "online".equals(baseOralPractice.getOlStatus());
                })
                .collect(Collectors.toMap(StoneBufferedData::getId, Function.identity()));

        if (MapUtils.isNotEmpty(stoneBufferedDataMap) && stoneBufferedDataMap.values().size() <= 6) {
            updateRecommendInfo(teacherDetail, stoneBufferedDataMap.values().stream().collect(Collectors.toList()));
            return stoneBufferedDataMap.values().stream().collect(Collectors.toList());
        }
        Map<String, String> recommendInfo = oralCommunicationRecommendRecord.getOralCommunicationRecommendInfo();
        //计算推介个数
        String day = DayRange.current().toString();
        long clazzLevelSize = clazzLevelStoneData.values().stream().filter(CollectionUtils::isNotEmpty).count();
        if (clazzLevelSize == 0L) {
            return recommendList;
        }
        int clazzSize = SafeConverter.toInt(clazzLevelSize);
        int size = 6 / clazzSize;
        int mod = 6 % clazzSize;
        int[] numbers = new int[clazzSize];

        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = size;
            if (mod > 0) {
                numbers[i] = size + 1;
                mod--;
            }
        }
        //--之前没推介过，直接取
        if (MapUtils.isEmpty(recommendInfo)) {
            int i = 0;
            for (List<String> levelIds : clazzLevelStoneData.values()) {
                if (CollectionUtils.isEmpty(levelIds)) {
                    continue;
                }
                List<String> addIds = CollectionUtils.isNotEmpty(recommendList)
                        ? recommendList.stream().map(StoneBufferedData::getId).collect(Collectors.toList())
                        : new ArrayList<>();
                List<String> chooseableId = levelIds.stream().filter(id -> !addIds.contains(id)).collect(Collectors.toList());
                List<String> ids;
                if (CollectionUtils.isNotEmpty(chooseableId) && chooseableId.size() >= numbers[i]) {
                    ids = chooseableId.subList(0, numbers[i]);
                } else {
                    ids = chooseableId;
                }
                if (CollectionUtils.isEmpty(ids)) {
                    i++;
                    continue;
                }
                i++;
                ids.forEach(id -> {
                    if (stoneBufferedDataMap.get(id) != null) {
                        recommendList.add(stoneBufferedDataMap.get(id));
                    }
                });
            }
            updateRecommendInfo(teacherDetail, recommendList);
            return recommendList;
        }
        //之前推介过
        List<String> beforeAlreadyRecommend = Lists.newArrayList();
        recommendInfo.forEach((k, v) -> {
            if (!v.equals(day)) {
                beforeAlreadyRecommend.add(k);
            }
        });
        int i = 0;
        for (List<String> levelIds : clazzLevelStoneData.values()) {
            if (CollectionUtils.isEmpty(levelIds)) {
                continue;
            }
            List<String> addIds;
            if (CollectionUtils.isEmpty(recommendList)) {
                addIds = Lists.newArrayList();
            } else {
                addIds = recommendList.stream().map(StoneBufferedData::getId).collect(Collectors.toList());
            }
            List<String> ids = levelIds.stream()
                    .filter(id -> !addIds.contains(id))
                    .filter(id -> !beforeAlreadyRecommend.contains(id)).collect(Collectors.toList());
            //去掉已推介过的如果能满足推介数量
            if (CollectionUtils.isNotEmpty(ids) && ids.size() >= numbers[i]) {
                ids = ids.subList(0, numbers[i++]);
                ids.forEach(id -> {
                            if (stoneBufferedDataMap.get(id) != null) {
                                recommendList.add(stoneBufferedDataMap.get(id));
                            }
                        }
                );
                continue;
            }
            List<String> chooseableIds = levelIds.stream()
                    .filter(id -> !addIds.contains(id)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(chooseableIds)) {
                i++;
                continue;
            }
            ids = chooseableIds.size() >= numbers[i] ? chooseableIds.subList(0, numbers[i]) : chooseableIds;
            ids.forEach(id -> {
                if (stoneBufferedDataMap.get(id) != null) {
                    recommendList.add(stoneBufferedDataMap.get(id));
                }
            });
            i++;
        }
        updateRecommendInfo(teacherDetail, recommendList);
        return recommendList;
    }

    private void updateRecommendInfo(TeacherDetail teacherDetail, List<StoneBufferedData> recommendList) {
        if (CollectionUtils.isEmpty(recommendList)) {
            return;
        }
        List<String> stoneIds = recommendList.stream().map(StoneBufferedData::getId).collect(Collectors.toList());
        oralCommunicationRecommendRecordDao.updateOralCommunicationHistory(teacherDetail.getId(), teacherDetail.getSubject(), stoneIds);
    }

    private List<PictureBookPlus> recommendFamousPictureBookPlusList(List<PictureBookPlus> picturesBookPlusList, List<Integer> clazzLevels, TeacherDetail teacherDetail) {
        PictureBookPlusRecommendRecord pictureBookPlusRecommendRecord = pictureBookPlusRecommendRecordLoader.loadPictureBookRecommendRecord(teacherDetail.getSubject(), teacherDetail.getId()); // 绘本推荐记录
        List<List<PictureBookPlus>> picturesBookPlusListByClazzLevel = new ArrayList<>();
        List<PictureBookPlus> picturesBookPlusListSorted = sortPictureBookPlusList(picturesBookPlusList, pictureBookPlusRecommendRecord);
        for (Integer clazz : clazzLevels) {
            List<PictureBookPlus> picturesBookPlusListSub = picturesBookPlusListSorted.stream().filter(picturesBookPlus -> clazz == picturesBookPlus.getNewClazzLevels().get(0).getLevelNum()).collect(Collectors.toList());
            picturesBookPlusListByClazzLevel.add(picturesBookPlusListSub);
        }

        List<PictureBookPlus> recommendPictureList = new ArrayList<>();
        if (picturesBookPlusListByClazzLevel.size() > 0) {
            int clazzLevelSize = clazzLevels.size();
            int size = 6 / clazzLevelSize;
            int mod = 6 % clazzLevelSize;
            int[] numbers = new int[clazzLevelSize];

            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = size;
                if (mod > 0) {
                    numbers[i] = size + 1;
                    mod--;
                }
            }
            int i = 0;
            for (List<PictureBookPlus> picturesBookPlus : picturesBookPlusListByClazzLevel) {
                recommendPictureList.addAll(picturesBookPlus.subList(0, numbers[i++]));
            }
        }
        if (pictureBookPlusRecommendRecord.getPictureBookRecommendInfo().size() <= 0) {
            pictureBookPlusRecommendRecord.setSubject(teacherDetail.getSubject());
            pictureBookPlusRecommendRecord.setTeacherId(teacherDetail.getId());
        }
        Map<String, Integer> map = pictureBookPlusRecommendRecord.getPictureBookRecommendInfo();
        for (PictureBookPlus recommendData : recommendPictureList) {
            Integer recommendTimes = map.get(recommendData.getId());
            recommendTimes = recommendTimes != null ? recommendTimes + 1 : 1;
            map.put(recommendData.getId(), recommendTimes);
        }
        pictureBookPlusRecommendRecordLoader.updatePictureBookRecommendRecord(pictureBookPlusRecommendRecord);
        return recommendPictureList;
    }

    private List<PictureBookPlus> sortPictureBookPlusList(List<PictureBookPlus> picturesBookPlusList, PictureBookPlusRecommendRecord pictureBookRecommendRecord) {
        Map<String, Integer> recommendInfo = pictureBookRecommendRecord.getPictureBookRecommendInfo();
        return picturesBookPlusList.stream().sorted(((o1, o2) -> {
            int time = SafeConverter.toInt(recommendInfo.getOrDefault(o1.getId(), 0));
            int time2 = SafeConverter.toInt(recommendInfo.getOrDefault(o2.getId(), 0));
            if (time > time2) {
                return 1;
            } else if (time < time2) {
                return -1;
            } else {
                Integer level = PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.getOrDefault(o1.getSeriesId(), 5);
                Integer level1 = PICTURE_BOOK_PLUS_SERIES_ID_RANK_MAP.getOrDefault(o2.getSeriesId(), 5);
                if (level > level1) {
                    return 1;
                }
                if (level < level1) {
                    return -1;
                }
                return 0;
            }
        })).collect(Collectors.toList());
    }

    private List<Dubbing> recommendDubbingList(TeacherDetail teacherDetail, Map<Integer, EmbedBook> clazzLevelBookInfoMap) {
        List<Dubbing> allDubbingList = dubbingLoaderClient.loadAllDubbings();
        DubbingRecommendRecord dubbingRecommendRecord = dubbingRecommendRecordDao.loadDubbingRecommendRecord(teacherDetail.getId(), teacherDetail.getSubject());
        Map<String, Integer> dubbingRecommendTimes = dubbingRecommendRecord != null && MapUtils.isNotEmpty(dubbingRecommendRecord.getDubbingRecommendInfo()) ? dubbingRecommendRecord.getDubbingRecommendInfo() : new HashMap<>();
        allDubbingList.sort((d1, d2) -> {
            int rank1 = SafeConverter.toInt(dubbingRecommendTimes.get(d1.getDocId()));
            int rank2 = SafeConverter.toInt(dubbingRecommendTimes.get(d2.getDocId()));
            return Integer.compare(rank1, rank2);
        });
        Map<Integer, List<Dubbing>> clazzLevelDubbingListMap = new LinkedHashMap<>();
        for (Dubbing dubbing : allDubbingList) {
            int difficult = SafeConverter.toInt(dubbing.getDifficult());
            clazzLevelDubbingListMap.computeIfAbsent(difficult, k -> new ArrayList<>()).add(dubbing);
        }
        List<Dubbing> recommendDubbingList = new ArrayList<>();
        List<Integer> clazzLevels = new ArrayList<>(clazzLevelBookInfoMap.keySet());
        int clazzLevelSize = clazzLevels.size();
        int averageSize = 6 / clazzLevelSize;
        int mod = 6 % clazzLevelSize;
        for (int i = 1; i <= clazzLevelSize; i++) {
            int recommendSize = averageSize;
            if (i <= mod) {
                recommendSize += 1;
            }
            List<Dubbing> dubbingList = clazzLevelDubbingListMap.get(clazzLevels.get(i - 1));
            if (CollectionUtils.isNotEmpty(dubbingList)) {
                recommendDubbingList.addAll(dubbingList.stream().limit(recommendSize).collect(Collectors.toList()));
            }
        }
        List<String> recommendDubbingIds = recommendDubbingList.stream().map(Dubbing::getDocId).collect(Collectors.toList());
        for (String dubbinId : recommendDubbingIds) {
            int times = dubbingRecommendTimes.getOrDefault(dubbinId, 0);
            dubbingRecommendTimes.put(dubbinId, times + 1);
        }
        if (dubbingRecommendRecord != null) {
            dubbingRecommendRecord.setDubbingRecommendInfo(dubbingRecommendTimes);
            dubbingRecommendRecordDao.upsert(dubbingRecommendRecord);
        }
        return recommendDubbingList;
    }

    private List<Map<String, Object>> loadRecommendPictureBooks(TeacherDetail teacher, String bookId, String unitId) {
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries()
                .stream()
                .filter(pictureBookSeries -> Objects.equals(pictureBookSeries.getSubjectId(), teacher.getSubject().getId()))
                .collect(Collectors.toList());
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));

        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics()
                .stream()
                .filter(pictureBookTopic -> Objects.equals(pictureBookTopic.getSubjectId(), teacher.getSubject().getId()))
                .sorted(Comparator.comparingInt(PictureBookTopic::getRank))
                .collect(Collectors.toList());
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));

        Set<String> pictureBookPlusSet = new HashSet<>();
        List<PictureBookPlus> recommendPictureBooks = new ArrayList<>();
        // 课堂同步拓展
        List<PictureBookPlus> syncPbList = pictureBookLoaderClient.loadRecommendPictureBookPlusForClazz(unitId);
        if (CollectionUtils.isNotEmpty(syncPbList)) {
            for (PictureBookPlus pictureBookPlus : syncPbList) {
                if (pictureBookPlusSet.add(pictureBookPlus.getId())) {
                    recommendPictureBooks.add(pictureBookPlus);
                }
            }
        }
        // 主题阅读
        List<String> pictureBookPlusIds = pictureBookLoaderClient.loadRecommendPictureBookPlusIdsForTopic(unitId);
        List<PictureBookPlus> topicPbList = new ArrayList<>(pictureBookPlusServiceClient.loadByIds(pictureBookPlusIds).values());
        if (CollectionUtils.isNotEmpty(topicPbList)) {
            for (PictureBookPlus pictureBookPlus : topicPbList) {
                if (pictureBookPlusSet.add(pictureBookPlus.getId())) {
                    recommendPictureBooks.add(pictureBookPlus);
                }
            }
        }
        // 系列绘本推荐
        List<PictureBookPlus> seriesPbList = pictureBookLoaderClient.loadRecommendPictureBookPlusForSeries(unitId);
        if (CollectionUtils.isNotEmpty(seriesPbList)) {
            for (PictureBookPlus pictureBookPlus : seriesPbList) {
                if (pictureBookPlusSet.add(pictureBookPlus.getId())) {
                    recommendPictureBooks.add(pictureBookPlus);
                }
            }
        }
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
        List<PictureBookPlus> notAssignedPictureBooks = new ArrayList<>();
        List<PictureBookPlus> assignedPictureBooks = new ArrayList<>();
        for (PictureBookPlus pictureBookPlus : recommendPictureBooks) {
            if (teacherAssignmentRecord != null && teacherAssignmentRecord.getPictureBookInfo() != null
                    && SafeConverter.toInt(teacherAssignmentRecord.getPictureBookInfo().get(pictureBookPlus.getId())) > 0) {
                assignedPictureBooks.add(pictureBookPlus);
            } else {
                notAssignedPictureBooks.add(pictureBookPlus);
            }
        }
        List<PictureBookPlus> sortedPictureBooks = new ArrayList<>(notAssignedPictureBooks);
        sortedPictureBooks.addAll(assignedPictureBooks);
        return sortedPictureBooks
                .stream()
                .filter(PictureBookPlus::isOnline)
                // 过滤非作业端的绘本
                .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                .map(pictureBookPlus -> {
                    String pictureBookSeries = "";
                    if (pictureBookSeriesMap.containsKey(pictureBookPlus.getSeriesId())) {
                        pictureBookSeries = pictureBookSeriesMap.get(pictureBookPlus.getSeriesId()).fetchName();
                        if (pictureBookPlus.getVolume() != null) {
                            pictureBookSeries += pictureBookPlus.getVolume();
                        }
                    }
                    PictureBookNewClazzLevel clazzLevel = null;
                    if (CollectionUtils.isNotEmpty(pictureBookPlus.getNewClazzLevels())) {
                        clazzLevel = pictureBookPlus.getNewClazzLevels().get(0);
                    }
                    List<String> pictureBookTopicNameList = Collections.emptyList();
                    List<String> pictureBookTopicIdList = pictureBookPlus.getTopicIds();
                    if (CollectionUtils.isNotEmpty(pictureBookTopicIdList)) {
                        pictureBookTopicIdList = pictureBookTopicIdList.stream()
                                .filter(pictureBookTopicMap::containsKey)
                                .collect(Collectors.toList());
                        pictureBookTopicNameList = pictureBookTopicIdList.stream()
                                .map(id -> pictureBookTopicMap.get(id).getName())
                                .collect(Collectors.toList());
                    }
                    return MapUtils.m(
                            "pictureBookId", pictureBookPlus.getId(),
                            "pictureBookName", pictureBookPlus.getEname(),
                            "pictureBookSeries", pictureBookSeries,
                            "pictureBookClazzLevelName", clazzLevel == null ? "" : clazzLevel.getLevelName(),
                            "pictureBookThumbImgUrl", SafeConverter.toString(pictureBookPlus.getCoverThumbnailUrl(), ""),
                            "pictureBookTopics", pictureBookTopicNameList
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 获得可布置作业的系统自建班级
     * -- changyuan.liu
     */
    private Map<Long, ExClazz> getCandidateSystemClazz(Subject subject, List<ExClazz> clazzs) {
        Map<Long, ExClazz> clazzCandidate = clazzs.stream()
                .collect(Collectors.toMap(ExClazz::getId, Function.identity()));

        // 这里已经保证所有的都是clazz id -> group id对
        Set<ClazzGroup> cg = clazzs.stream()
                .filter(e -> CollectionUtils.isNotEmpty(e.getCurTeacherGroups()))
                .map(e -> new ClazzGroup(e.getId(), e.getCurTeacherGroups().get(0).getId()))
                .collect(Collectors.toSet());

        AssignableValidationResult r = validate(subject, cg);

        for (Iterator<Map.Entry<Long, ExClazz>> it = clazzCandidate.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Long, ExClazz> entry = it.next();
            Long cid = entry.getKey();
            List<GroupMapper> groupMappers = entry.getValue().getCurTeacherGroups();
            if (CollectionUtils.isEmpty(groupMappers)) {
                continue;
            }
            List<GroupMapper> arrangeableGroups = entry.getValue().getCurTeacherArrangeableGroups();
            if (arrangeableGroups == null) {
                arrangeableGroups = new LinkedList<>();
                entry.getValue().setCurTeacherArrangeableGroups(arrangeableGroups);
            }
            for (GroupMapper groupMapper : groupMappers) {
                ClazzGroup tcg = new ClazzGroup(cid, groupMapper.getId());
                if (r.getAssignables().contains(tcg)) {
                    arrangeableGroups.add(groupMapper);
                }
            }
            // 当无可布置作业分组时，班级同样是无法布置作业状态
            if (CollectionUtils.isEmpty(arrangeableGroups)) {
                it.remove();
            } else {
                Collections.sort(arrangeableGroups);
            }
        }
        return clazzCandidate;
    }

    // 默认所有的都有分组
    private AssignableValidationResult validate(Subject subject, Collection<ClazzGroup> clazzGroups) {
        AssignableValidationResult result = new AssignableValidationResult();
        if (CollectionUtils.isEmpty(clazzGroups)) return result;

        Set<Long> groupIds = clazzGroups.stream().map(ClazzGroup::getGroupId).collect(Collectors.toSet());

        // 验证每个分组是否有学生
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIds);
        for (ClazzGroup clazzGroup : clazzGroups) {
            Long clazzId = clazzGroup.getClazzId();
            Long groupId = clazzGroup.getGroupId();
            // 这里实际上只需要判断学生是否存在，不需要拿学生的具体信息
            List<Long> studentIds = groupStudentIds.get(groupId);
            if (CollectionUtils.isEmpty(studentIds)) {
                logger.debug("Clazz/group {}/{} has no students, non-assignable", clazzId, groupId);
                result.getNonAssignables().add(clazzGroup);
            }
        }

        // 学生数没有问题了
        // 验证是否存在未检查作业
        Map<Long, List<NewHomework.Location>> groupLocationMap = newHomeworkLoader.loadGroupHomeworks(groupIds, subject)
                .originalLocationsAsList()
                .stream()
                .filter(e -> NewHomeworkType.Normal.equals(e.getType()) && !e.isChecked())
                .collect(Collectors.groupingBy(NewHomework.Location::getClazzGroupId));

        for (ClazzGroup clazzGroup : clazzGroups) {
            if (result.getNonAssignables().contains(clazzGroup)) {
                continue;
            }

            Long groupId = clazzGroup.getGroupId();
            long unchecked = groupLocationMap.get(groupId) == null ? 0 : groupLocationMap.get(groupId).size();
            if (unchecked > 0) {
                logger.debug("Group {} has unchecked homework, non-assignable", groupId);
                result.getNonAssignables().add(clazzGroup);
            }
        }

        // 把所有剩余的班级/群组归入到可布置的集合
        clazzGroups.forEach(clazzGroup -> {
            if (!result.getNonAssignables().contains(clazzGroup)) {
                result.getAssignables().add(clazzGroup);
            }
        });

        return result;
    }

    Map<String, Object> buildBookMapper(NewBookProfile book, NewClazzBookRef newClazzBookRef) {
        // book返回结构
        Map<String, Object> bookMap = new LinkedHashMap<>();
        String bookId = book.getId();
        bookMap.put("bookId", bookId);
        bookMap.put("bookName", book.getName());
        bookMap.put("imgUrl", book.getImgUrl());
        bookMap.put("clazzLevel", book.getClazzLevel());
        bookMap.put("termType", book.getTermType());
        bookMap.put("latestVersion", Objects.equals(1, book.getLatestVersion())); //当为1的时候表示最新课本

        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(book.getSeriesId());
        if (newBookCatalog != null) {
            String seriesName = SafeConverter.toString(newBookCatalog.getName());
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(book.getSubjectId()), seriesName);
            if (bookPress != null) {
                bookMap.put("viewContent", bookPress.getViewContent());
                bookMap.put("color", bookPress.getColor());
            }
        }

        // 根据教材获取units
        List<NewBookCatalog> bookUnitList = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT)
                .getOrDefault(bookId, Collections.emptyList()).stream()
                .sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());

        // 根据教材获取modules
        List<NewBookCatalog> bookModuleList = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.MODULE)
                .getOrDefault(bookId, Collections.emptyList()).stream()
                .sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());

        int subjectId = SafeConverter.toInt(book.getSubjectId());
        boolean isEnglish = subjectId == 103 || subjectId == 503;
        // 如果是英语包含module节点的教材，对unit作进一步排序
        if (isEnglish && CollectionUtils.isNotEmpty(bookModuleList)) {
            Map<String, NewBookCatalog> moduleMap = bookModuleList
                    .stream()
                    .collect(Collectors.toMap(NewBookCatalog::getId, Function.identity()));
            bookUnitList = bookUnitList.stream()
                    .filter(unit -> unit.getParentId() != null)
                    .filter(unit -> moduleMap.containsKey(unit.getParentId()))
                    .sorted((u1, u2) -> {
                        int rank1 = SafeConverter.toInt(moduleMap.get(u1.getParentId()).getRank());
                        int rank2 = SafeConverter.toInt(moduleMap.get(u2.getParentId()).getRank());
                        return Integer.compare(rank1, rank2);
                    })
                    .collect(Collectors.toList());
        }

        // 根据units获取lessons
        Set<String> bookUnitIds = bookUnitList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());
        Map<String, List<NewBookCatalog>> unitLessonMap = newContentLoaderClient.loadChildren(bookUnitIds, BookCatalogType.LESSON);

        // 根据lessons获取sections
        Set<String> lessonIds = unitLessonMap.values().stream().flatMap(Collection::stream).map(NewBookCatalog::getId).collect(Collectors.toSet());
        Map<String, List<NewBookCatalog>> lessonSectionMap = newContentLoaderClient.loadChildren(lessonIds, BookCatalogType.SECTION);

        // 结果拼装
        // 生成单元结构
        List<Map<String, Object>> unitMaps = new ArrayList<>();
        // 设置默认单元
        // 没有之前使用的教材，设第一个为默认单元
        // 有的话设置之前布置作业的单元
        NewBookCatalog defaultUnit = MiscUtils.firstElement(bookUnitList);
        String defaultUnitId = defaultUnit != null ? defaultUnit.getId() : null;
        if (newClazzBookRef != null && bookUnitIds.stream().anyMatch(id -> Objects.equals(newClazzBookRef.getUnitId(), id))) {
            defaultUnitId = newClazzBookRef.getUnitId();
        }

        for (NewBookCatalog unit : bookUnitList) {
            Map<String, Object> unitMapper = new LinkedHashMap<>();
            unitMapper.put("unitId", unit.getId());
            unitMapper.put("cname", isEnglish ? unit.getAlias() : unit.getName());
            unitMapper.put("defaultUnit", Objects.equals(defaultUnitId, unit.getId()));
            unitMapper.put("parentId", unit.getParentId());
            unitMaps.add(unitMapper);
        }
        // 根据newClazzBookRef找到默认课时
        String defaultSectionId = null;
        if (newClazzBookRef != null && newClazzBookRef.getUnitId() != null) {
            String refDefaultUnitId = newClazzBookRef.getUnitId();
            if (MapUtils.isNotEmpty(unitLessonMap) && unitLessonMap.containsKey(refDefaultUnitId) && unitLessonMap.get(refDefaultUnitId) != null) {
                List<NewBookCatalog> lessonList = unitLessonMap.get(refDefaultUnitId).stream()
                        .sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());
                List<NewBookCatalog> sectionList = lessonList.stream()
                        .filter(lesson -> lessonSectionMap.get(lesson.getId()) != null)
                        .flatMap(lesson -> lessonSectionMap.get(lesson.getId()).stream().sorted(new NewBookCatalog.RankComparator()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sectionList)) {
                    if (newClazzBookRef.getSectionID() != null) {
                        int idx = -1;
                        for (int i = 0; i < sectionList.size(); i++) {
                            if (newClazzBookRef.getSectionID().equals(sectionList.get(i).getId())) {
                                idx = i;
                                break;
                            }
                        }
                        if (idx < sectionList.size() - 1) {
                            idx++;
                        }
                        defaultSectionId = sectionList.get(idx).getId();
                    } else {
                        defaultSectionId = sectionList.get(0).getId();
                    }
                }
            }
        }
        for (Map<String, Object> unitMap : unitMaps) {
            String unitId = SafeConverter.toString(unitMap.get("unitId"));
            List<NewBookCatalog> lessonList = unitLessonMap.getOrDefault(SafeConverter.toString(unitMap.get("unitId")), Collections.emptyList())
                    .stream()
                    .sorted(new NewBookCatalog.RankComparator())
                    .collect(Collectors.toList());
            int idx = 0;
            List<Map<String, Object>> sections = new ArrayList<>();
            for (NewBookCatalog lesson : lessonList) {
                List<NewBookCatalog> sectionList = lessonSectionMap.getOrDefault(lesson.getId(), Collections.emptyList()).stream()
                        .sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());
                for (NewBookCatalog section : sectionList) {
                    ++idx;
                    String sectionId = section.getId();
                    // 设置每个单元下的默认课时
                    // 默认单元需要设置为该单元下默认课时的后一个课时
                    boolean defaultSection = false;
                    if (StringUtils.isNotEmpty(defaultUnitId) && defaultUnitId.equals(unitId) && StringUtils.isNotEmpty(defaultSectionId)) {
                        if (defaultSectionId.equals(sectionId)) {
                            defaultSection = true;
                        }
                    }
                    // 非默认单元下的默认课时设置为该单元的第一个课时
                    else if (idx == 1) {
                        defaultSection = true;
                    }
                    sections.add(MapUtils.m("defaultSection", defaultSection, "sectionId", sectionId, "cname", isEnglish ? section.getAlias() : section.getName()));
                }
            }
            unitMap.put("sections", sections);
        }
        bookMap.put("unitList", unitMaps);

        if (isEnglish && CollectionUtils.isNotEmpty(bookModuleList)) {
            Map<String, List<Object>> moduleUnitMaps = new HashMap<>();
            for (Map<String, Object> unitMap : unitMaps) {
                String parentId = SafeConverter.toString(unitMap.get("parentId"));
                if (StringUtils.isNotBlank(parentId)) {
                    List<Object> moduleUnits = moduleUnitMaps.computeIfAbsent(parentId, k -> new ArrayList<>());
                    moduleUnits.add(unitMap);
                }
            }
            List<Map<String, Object>> moduleMaps = bookModuleList.stream()
                    .filter(m -> moduleUnitMaps.containsKey(m.getId()))
                    .map(m -> MapUtils.m("moduleName", m.getAlias(), "units", moduleUnitMaps.get(m.getId())))
                    .collect(Collectors.toList());
            bookMap.put("moduleList", moduleMaps);
        }
        return bookMap;
    }

    /**
     * 组装课外拓展视频数据
     */
    private Map<String, Object> buildExpandVideoMapper(Video video, String videoName, TeacherAssignmentRecord teacherAssignmentRecord, EmbedBook book) {
        if (CollectionUtils.isEmpty(video.getExtracurricularTasks())) {
            return Collections.emptyMap();
        }
        int questionCount = 0;
        List<Map<String, Object>> practiceList = new ArrayList<>();
        for (VideoExtracurricularTask task : video.getExtracurricularTasks()) {
            Map<String, Object> taskMapper = MapUtils.m(
                    "practiceName", task.getName(),
                    "tags", task.getTags(),
                    "descriptions", task.getDescriptions()
            );
            // 主观任务，处理到这里就结束了
            if (SafeConverter.toBoolean(task.getSubjective())) {
                practiceList.add(taskMapper);
            } else {
                List<VideoExtracurricularQuestion> questions = task.getQuestions();
                // 只处理包含两组题包的
                if (CollectionUtils.isNotEmpty(questions) && questions.size() == 2) {
                    Set<String> allQuestionDocIds = questions.stream()
                            .map(VideoExtracurricularQuestion::getQuestionIds)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet());
                    Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionByDocIds0(allQuestionDocIds);
                    if (MapUtils.isNotEmpty(questionMap)) {
                        List<Map<String, Object>> questionList = new ArrayList<>();
                        for (VideoExtracurricularQuestion question : questions) {
                            if (CollectionUtils.isNotEmpty(question.getQuestionIds())) {
                                List<String> questionIds = question.getQuestionIds()
                                        .stream()
                                        .filter(questionMap::containsKey)
                                        .map(e -> questionMap.get(e).getId())
                                        .collect(Collectors.toList());
                                if (CollectionUtils.isNotEmpty(questionIds)) {
                                    questionList.add(MapUtils.m(
                                            "name", question.getName(),
                                            "questionCount", questionIds.size(),
                                            "questionIds", questionIds
                                    ));
                                    questionCount += questionIds.size();
                                }
                            }
                        }
                        if (questionList.size() == 2) {
                            taskMapper.put("questions", questionList);
                            practiceList.add(taskMapper);
                        }
                    }
                }
            }
        }
        if (practiceList.size() == 2) {
            return MapUtils.m(
                    "videoId", video.getId(),
                    "videoName", videoName,
                    "coverUrl", video.getCoverUrl(),
                    "videoUrl", video.getVideoUrl(),
                    "seconds", video.getVideoSeconds(),
                    "practiceList", practiceList,
                    "teacherAssignTimes", teacherAssignmentRecord != null ? teacherAssignmentRecord.getAppInfo().getOrDefault(video.getDocId(), 0) : 0,
                    "questionCount", questionCount,
                    "book", NewHomeworkContentDecorator.buildBookMapper(book)
            );
        }
        return Collections.emptyMap();
    }

    /**
     * 获取ABTestA分组的学校ids
     */
    private List<Long> loadABTestPlanASchoolIds() {
        List<PageBlockContent> contents = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("PlanASchoolIds");
        if (CollectionUtils.isNotEmpty(contents)) {
            PageBlockContent configPageBlockContent = contents.stream()
                    .filter(p -> "ABTest".equals(p.getBlockName()))
                    .findFirst()
                    .orElse(null);
            if (configPageBlockContent != null) {
                String configContent = configPageBlockContent.getContent();
                if (StringUtils.isBlank(configContent)) {
                    return null;
                }
                configContent = configContent.replaceAll("[\n\r\t]", "").trim();
                return JsonUtils.fromJsonToList(configContent, Long.class);
            }
        }
        return null;
    }

    /**
     * 获取瀑布流显示横版的学校id列表
     */
    private List<Long> loadWaterfallPlanASchoolIds() {
        List<PageBlockContent> contents = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("PlanASchoolIds");
        if (CollectionUtils.isNotEmpty(contents)) {
            PageBlockContent configPageBlockContent = contents.stream()
                    .filter(p -> "WaterfallABTest".equals(p.getBlockName()))
                    .findFirst()
                    .orElse(null);
            if (configPageBlockContent != null) {
                String configContent = configPageBlockContent.getContent();
                if (StringUtils.isBlank(configContent)) {
                    return null;
                }
                configContent = configContent.replaceAll("[\n\r\t]", "").trim();
                return JsonUtils.fromJsonToList(configContent, Long.class);
            }
        }
        return null;
    }

    private int processContent(NewHomeworkPracticeContent practiceContent, List<Map<String, Object>> contentList) {
        ObjectiveConfigType objectiveConfigType = practiceContent.getType();
        List<NewHomeworkQuestion> questions = practiceContent.getQuestions();
        List<NewHomeworkApp> apps = practiceContent.getApps();
        Map<String, Object> content = Collections.emptyMap();
        int questionCount = 0;
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        switch (objectiveConfigType.getNewHomeworkContentProcessTemp()) {
            case EXAM:
            case SPECIAL_EXAM:
            case DICTATION:
                if (CollectionUtils.isNotEmpty(questions)) {
                    Set<String> questionIds = questions.stream().map(NewHomeworkQuestion::getQuestionId)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    List<NewQuestion> questionList = new ArrayList<>(questionLoaderClient.loadQuestionsIncludeDisabled(questionIds).values());
                    List<Map<String, Object>> questionMapperList = questionList.stream()
                            .map(question -> MapUtils.m(
                                    "questionId", question.getId(),
                                    "questionType", contentTypeMap.get(question.getContentTypeId()) != null ? contentTypeMap.get(question.getContentTypeId()).getName() : "无题型",
                                    "difficultyName", QuestionConstants.newDifficultyMap.get(question.getDifficultyInt())
                            ))
                            .collect(Collectors.toList());
                    content = MapUtils.m("type", objectiveConfigType,
                            "typeName", objectiveConfigType.getValue(),
                            "questions", questionMapperList);
                    questionCount += questionMapperList.size();
                }
                break;
            case BASIC_APP:
            case SPECIAL_APP:
                if (CollectionUtils.isNotEmpty(apps)) {
                    Set<String> lessonIds = apps.stream().map(NewHomeworkApp::getLessonId).collect(Collectors.toCollection(LinkedHashSet::new));
                    Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
                    Map<String, String> lessonUnitMap = new HashMap<>();
                    for (NewBookCatalog lesson : lessonMap.values()) {
                        List<NewBookCatalogAncestor> ancestors = lesson.getAncestors();
                        if (CollectionUtils.isNotEmpty(ancestors)) {
                            String unitId = "";
                            for (NewBookCatalogAncestor ancestor : ancestors) {
                                if (StringUtils.equalsIgnoreCase(ancestor.getNodeType(), BookCatalogType.UNIT.name())) {
                                    unitId = ancestor.getId();
                                    break;
                                }
                            }
                            if (StringUtils.isNotBlank(unitId)) {
                                lessonUnitMap.put(lesson.getId(), unitId);
                            }
                        }
                    }
                    Map<String, NewBookCatalog> unitMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonUnitMap.values());
                    Map<String, Set<Integer>> lessonCategoriesMap = new LinkedHashMap<>();
                    for (NewHomeworkApp app : apps) {
                        String lessonId = app.getLessonId();
                        Integer categoryId = app.getCategoryId();
                        lessonCategoriesMap.computeIfAbsent(lessonId, k -> new LinkedHashSet<>()).add(categoryId);
                    }
                    List<PracticeType> allPracticeList = practiceLoaderClient.loadPractices();
                    List<PracticeType> practiceTypeList = new ArrayList<>(allPracticeList);
                    Map<Integer, List<PracticeType>> categoryPracticeMap = practiceTypeList.stream()
                            .filter(p -> PracticeCategory.categoryPracticeTypesMap.get(p.getCategoryId()) != null && PracticeCategory.categoryPracticeTypesMap.get(p.getCategoryId()).contains(p.getId()))
                            .collect(Collectors.groupingBy(PracticeType::getCategoryId));
                    List<Map<String, Object>> lessonMapperList = new ArrayList<>();
                    lessonCategoriesMap.forEach((k, v) -> {
                        if (lessonMap.containsKey(k)) {
                            NewBookCatalog lesson = lessonMap.get(k);
                            List<Map<String, Object>> categoryMapperList = new ArrayList<>();
                            v.forEach(categoryId -> {
                                List<PracticeType> practiceTypes = categoryPracticeMap.get(categoryId);
                                if (CollectionUtils.isNotEmpty(practiceTypes)) {
                                    String categoryName = practiceTypes.iterator().next().getCategoryName();
                                    categoryMapperList.add(MapUtils.m(
                                            "categoryId", categoryId,
                                            "categoryName", categoryName));
                                }
                            });
                            if (CollectionUtils.isNotEmpty(categoryMapperList)) {
                                lessonMapperList.add(MapUtils.m(
                                        "lessonId", lesson.getId(),
                                        "lessonName", lesson.getAlias(),
                                        "categories", categoryMapperList
                                ));
                            }
                        }
                    });
                    Map<String, List<Map<String, Object>>> unitLessonMap = new LinkedHashMap<>();
                    for (Map<String, Object> lesson : lessonMapperList) {
                        String lessonId = SafeConverter.toString(lesson.get("lessonId"));
                        String unitId = lessonUnitMap.get(lessonId);
                        if (StringUtils.isNotBlank(unitId)) {
                            unitLessonMap.computeIfAbsent(unitId, k -> new ArrayList<>()).add(lesson);
                        }
                    }
                    List<Map<String, Object>> unitMapperList = new ArrayList<>();
                    unitLessonMap.forEach((k, v) -> {
                        if (unitMap.containsKey(k)) {
                            NewBookCatalog unit = unitMap.get(k);
                            unitMapperList.add(MapUtils.m(
                                    "unitId", unit.getId(),
                                    "unitName", unit.getAlias(),
                                    "lessons", v));
                        }
                    });
                    content = MapUtils.m("type", objectiveConfigType,
                            "typeName", objectiveConfigType.getValue(),
                            "units", unitMapperList);
                    questionCount += apps.size();
                }
                break;
            case READING:
                if (CollectionUtils.isNotEmpty(apps)) {
                    Set<String> pictureBookIds = apps.stream().map(NewHomeworkApp::getPictureBookId).collect(Collectors.toCollection(LinkedHashSet::new));
                    Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);
                    if (MapUtils.isNotEmpty(pictureBookMap)) {
                        List<Map<String, Object>> pictureBookMapperList = pictureBookMap.values()
                                .stream()
                                .map(pictureBook -> MapUtils.m(
                                        "pictureBookId", pictureBook.getId(),
                                        "pictureBookName", pictureBook.getName()
                                ))
                                .collect(Collectors.toList());
                        content = MapUtils.m("type", objectiveConfigType,
                                "typeName", objectiveConfigType.getValue(),
                                "pictureBooks", pictureBookMapperList);
                        questionCount += pictureBookMap.size();
                    }
                }
                break;
            case LEVEL_READINGS:
                if (CollectionUtils.isNotEmpty(apps)) {
                    Set<String> pictureBookIds = apps.stream().map(NewHomeworkApp::getPictureBookId).collect(Collectors.toCollection(LinkedHashSet::new));
                    Map<String, PictureBookPlus> pictureBookMap = pictureBookPlusServiceClient.loadByIds(pictureBookIds);
                    if (MapUtils.isNotEmpty(pictureBookMap)) {
                        List<Map<String, Object>> pictureBookMapperList = pictureBookMap.values()
                                .stream()
                                .map(pictureBook -> MapUtils.m(
                                        "pictureBookId", pictureBook.getId(),
                                        "pictureBookName", pictureBook.getEname()
                                ))
                                .collect(Collectors.toList());
                        content = MapUtils.m("type", objectiveConfigType,
                                "typeName", objectiveConfigType.getValue(),
                                "pictureBooks", pictureBookMapperList);
                        questionCount += pictureBookMap.size();
                    }
                }
                break;
            case DUBBING:
                if (CollectionUtils.isNotEmpty(apps)) {
                    Set<String> dubbingIds = apps.stream()
                            .map(NewHomeworkApp::getDubbingId)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);
                    if (MapUtils.isNotEmpty(dubbingMap)) {
                        List<Map<String, Object>> dubbingList = dubbingMap.values()
                                .stream()
                                .map(dubbing -> MapUtils.m(
                                        "dubbingId", dubbing.getId(),
                                        "dubbingName", dubbing.getVideoName()
                                ))
                                .collect(Collectors.toList());
                        content = MapUtils.m(
                                "type", objectiveConfigType,
                                "typeName", objectiveConfigType.getValue(),
                                "dubbings", dubbingList);
                        questionCount += dubbingMap.size();
                    }
                }
                break;
            case KEY_POINTS:
                if (CollectionUtils.isNotEmpty(apps)) {
                    Set<String> questionIds = apps.stream().map(NewHomeworkApp::getQuestions)
                            .flatMap(Collection::stream)
                            .map(NewHomeworkQuestion::getQuestionId)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    List<NewQuestion> questionList = new ArrayList<>(questionLoaderClient.loadQuestionsIncludeDisabled(questionIds).values());
                    List<Map<String, Object>> questionMapperList = questionList.stream()
                            .map(question -> MapUtils.m(
                                    "questionId", question.getId(),
                                    "questionType", contentTypeMap.get(question.getContentTypeId()) != null ? contentTypeMap.get(question.getContentTypeId()).getName() : "无题型",
                                    "difficultyName", QuestionConstants.newDifficultyMap.get(question.getDifficultyInt())
                            ))
                            .collect(Collectors.toList());
                    content = MapUtils.m("type", objectiveConfigType,
                            "typeName", objectiveConfigType.getValue(),
                            "questions", questionMapperList);
                    questionCount += questionMapperList.size();
                }
                break;
            case NEW_READ_RECITE:
            case READ_RECITE_WITH_SCORE:
                if (CollectionUtils.isNotEmpty(apps)) {
                    Set<String> lessonIds = apps.stream().map(NewHomeworkApp::getLessonId).collect(Collectors.toCollection(LinkedHashSet::new));
                    Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
                    List<Map<String, Object>> readLessons = new ArrayList<>();
                    List<Map<String, Object>> reciteLessons = new ArrayList<>();
                    for (NewHomeworkApp app : apps) {
                        String lessonId = app.getLessonId();
                        NewBookCatalog lesson = lessonMap.get(lessonId);
                        QuestionBoxType questionBoxType = app.getQuestionBoxType();
                        if (lesson != null) {
                            if (QuestionBoxType.READ == questionBoxType) {
                                readLessons.add(MapUtils.m("lessonId", lessonId,
                                        "lessonName", lesson.getName()
                                ));
                            } else if (QuestionBoxType.RECITE == questionBoxType) {
                                reciteLessons.add(MapUtils.m("lessonId", lessonId,
                                        "lessonName", lesson.getName()));
                            }
                        }
                    }
                    content = MapUtils.m("type", objectiveConfigType,
                            "typeName", objectiveConfigType.getValue(),
                            "readLessons", readLessons,
                            "reciteLessons", reciteLessons);
                    Set<String> questionIds = apps.stream().map(NewHomeworkApp::getQuestions)
                            .flatMap(Collection::stream)
                            .map(NewHomeworkQuestion::getQuestionId)
                            .collect(Collectors.toSet());
                    questionCount += questionIds.size();
                }
                break;
            case WORD_RECOGNITION_AND_READING:
                if (CollectionUtils.isNotEmpty(apps)) {
                    Set<String> lessonIds = apps.stream().map(NewHomeworkApp::getLessonId).collect(Collectors.toCollection(LinkedHashSet::new));
                    Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
                    List<Map<String, Object>> lessonDetail = new ArrayList<>();
                    for (NewHomeworkApp app : apps) {
                        String lessonId = app.getLessonId();
                        NewBookCatalog lesson = lessonMap.get(lessonId);
                        lessonDetail.add(MapUtils.m("lessonId", lessonId,
                                "lessonName", lesson.getName()
                        ));
                    }
                    content = MapUtils.m("type", objectiveConfigType,
                            "typeName", objectiveConfigType.getValue(),
                            "lessons", lessonDetail);
                    Set<String> questionIds = apps.stream().map(NewHomeworkApp::getQuestions)
                            .flatMap(Collection::stream)
                            .map(NewHomeworkQuestion::getQuestionId)
                            .collect(Collectors.toSet());
                    questionCount += questionIds.size();
                }
                break;
            case OCR_MENTAL_ARITHMETIC:
                content = MapUtils.m("type", objectiveConfigType,
                        "typeName", objectiveConfigType.getValue(),
                        "workBookName", practiceContent.getWorkBookName(),
                        "homeworkDetail", practiceContent.getHomeworkDetail());
                questionCount += 1;
                break;
            case ORAL_COMMUNICATION:
                Set<String> stoneIds = apps.stream()
                        .map(NewHomeworkApp::getStoneDataId)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(stoneIds);
                if (CollectionUtils.isNotEmpty(stoneBufferedDataList)) {
                    List<Map<String, Object>> stoneDataList = stoneBufferedDataList
                            .stream()
                            .map(stoneItem -> {
                                if (stoneItem.getOralPracticeConversion() != null) {
                                    return MapUtils.m(
                                            "stoneId", stoneItem.getId(),
                                            "topicName", StringUtils.isNotEmpty(stoneItem.getOralPracticeConversion().getTopicTrans()) ?
                                                    stoneItem.getOralPracticeConversion().getTopicTrans() :
                                                    stoneItem.getOralPracticeConversion().getTopicName()
                                    );
                                }
                                if (stoneItem.getInteractiveVideo() != null) {
                                    return MapUtils.m(
                                            "stoneId", stoneItem.getId(),
                                            "topicName", StringUtils.isNotEmpty(stoneItem.getInteractiveVideo().getTopicTrans()) ?
                                                    stoneItem.getInteractiveVideo().getTopicTrans() :
                                                    stoneItem.getInteractiveVideo().getTopicName()
                                    );
                                }
                                if (stoneItem.getInteractivePictureBook() != null) {
                                    return MapUtils.m(
                                            "stoneId", stoneItem.getId(),
                                            "topicName", StringUtils.isNotEmpty(stoneItem.getInteractivePictureBook().getTopicTrans()) ?
                                                    stoneItem.getInteractivePictureBook().getTopicTrans() :
                                                    stoneItem.getInteractivePictureBook().getTopicName()
                                    );
                                }
                                return null;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    content = MapUtils.m(
                            "type", objectiveConfigType,
                            "typeName", objectiveConfigType.getValue(),
                            "stoneData", stoneDataList);
                    questionCount += stoneBufferedDataList.size();
                }
                break;
            case WORD_TEACH_AND_PRACTICE:
                if (CollectionUtils.isNotEmpty(apps)) {
                    // 字词训练模块 所有题目
                    Set<String> questionIds = apps
                            .stream()
                            .filter(n -> CollectionUtils.isNotEmpty(n.getWordExerciseQuestions()))
                            .map(NewHomeworkApp::getWordExerciseQuestions)
                            .flatMap(Collection::stream)
                            .map(NewHomeworkQuestion::getQuestionId)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
                    //汉字文化课程
                    Map<String, IntelDiagnosisCourse> courseMap = new HashMap<>();
                    List<String> allCourseIds = apps
                            .stream()
                            .filter(n -> CollectionUtils.isNotEmpty(n.getChineseCharacterCultureCourseIds()))
                            .map(NewHomeworkApp::getChineseCharacterCultureCourseIds)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(allCourseIds)) {
                        courseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(allCourseIds);
                    }
                    //章节
                    Set<String> sectionIds = apps
                            .stream()
                            .filter(n -> n.getSectionId() != null)
                            .map(NewHomeworkApp::getSectionId)
                            .collect(Collectors.toSet());
                    //章节信息
                    Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(sectionIds);

                    List<WordTeachHomeworkMapper> wordTeachHomeworks = new ArrayList<>();
                    for (NewHomeworkApp newHomeworkApp : apps) {
                        WordTeachHomeworkMapper wordTeachHomework = new WordTeachHomeworkMapper();
                        wordTeachHomework.setStoneId(newHomeworkApp.getStoneDataId());
                        wordTeachHomework.setSectionId(newHomeworkApp.getSectionId());
                        wordTeachHomework.setSectionName(newBookCatalogMap.get(newHomeworkApp.getSectionId()) != null ? newBookCatalogMap.get(newHomeworkApp.getSectionId()).getName() : "");

                        // 字词训练
                        List<NewHomeworkQuestion> wordExerciseQuestions = newHomeworkApp.getWordExerciseQuestions();
                        if (CollectionUtils.isNotEmpty(wordExerciseQuestions)) {
                            List<NewQuestion> questionList = new ArrayList<>();
                            for (NewHomeworkQuestion question : newHomeworkApp.getWordExerciseQuestions()) {
                                if (questionMap.get(question.getQuestionId()) != null) {
                                    questionList.add(questionMap.get(question.getQuestionId()));
                                }
                            }
                            List<Map<String, Object>> questionMapperList = questionList.stream()
                                    .map(question -> MapUtils.m(
                                            "questionId", question.getId(),
                                            "questionType", contentTypeMap.get(question.getContentTypeId()) != null ? contentTypeMap.get(question.getContentTypeId()).getName() : "无题型",
                                            "difficultyName", QuestionConstants.newDifficultyMap.get(question.getDifficultyInt())
                                    ))
                                    .collect(Collectors.toList());
                            WordTeachHomeworkMapper.WordExerciseInfo wordExerciseInfo = new WordTeachHomeworkMapper.WordExerciseInfo();
                            wordExerciseInfo.setModuleName(WordTeachModuleType.WORDEXERCISE.getName());
                            wordExerciseInfo.setQuestionMapperList(questionMapperList);
                            wordTeachHomework.setWordExerciseInfo(wordExerciseInfo);
                        }

                        // 图文入韵
                        List<ImageTextRhymeHomework> imageTextRhymeQuestions = newHomeworkApp.getImageTextRhymeQuestions();
                        if (CollectionUtils.isNotEmpty(imageTextRhymeQuestions)) {
                            WordTeachHomeworkMapper.ImageTextRhymeInfo imageTextRhymeInfo = new WordTeachHomeworkMapper.ImageTextRhymeInfo();
                            imageTextRhymeInfo.setModuleName(WordTeachModuleType.IMAGETEXTRHYME.getName());
                            imageTextRhymeInfo.setTitles(imageTextRhymeQuestions.stream().map(ImageTextRhymeHomework::getTitle).collect(Collectors.toList()));
                            wordTeachHomework.setImageTextRhymeInfo(imageTextRhymeInfo);
                        }

                        // 汉字文化
                        List<String> courseIds = newHomeworkApp.getChineseCharacterCultureCourseIds();
                        if (CollectionUtils.isNotEmpty(courseIds)) {
                            WordTeachHomeworkMapper.ChineseCharacterCultureInfo chineseCharacterCultureInfo = new WordTeachHomeworkMapper.ChineseCharacterCultureInfo();
                            List<String> courseNames = new ArrayList<>();
                            for (String courseId : courseIds) {
                                if (courseMap.get(courseId) != null) {
                                    courseNames.add(courseMap.get(courseId).getName());
                                }
                            }
                            chineseCharacterCultureInfo.setModuleName(WordTeachModuleType.CHINESECHARACTERCULTURE.getName());
                            chineseCharacterCultureInfo.setCourseNames(courseNames);
                            wordTeachHomework.setChineseCharacterCultureInfo(chineseCharacterCultureInfo);
                        }
                        wordTeachHomeworks.add(wordTeachHomework);
                    }
                    content = MapUtils.m("type", objectiveConfigType,
                            "typeName", objectiveConfigType.getValue(),
                            "stoneDatas", wordTeachHomeworks);
                    questionCount += apps.size();
                }
                break;
            default:
                break;
        }
        if (MapUtils.isNotEmpty(content)) {
            contentList.add(content);
        }
        return questionCount;
    }

    /**
     * 获取数学基础必过内容
     *
     * @param bookId
     * @param stageId
     * @return
     */
    @Override
    public MapMessage loadBasicMathContent(String bookId, Integer stageId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<MathReview> mathReviewList = termReviewLoaderClient.getTermReviewLoader().loadMathReviews(bookId);
        if (CollectionUtils.isNotEmpty(mathReviewList)) {
            List<MathReview> resultMathReviewList = mathReviewList
                    .stream()
                    .filter(r -> Objects.equals(r.getRank(), stageId))
                    .collect(Collectors.toList());

            List<TermReview.MathContent> mathContents = resultMathReviewList.get(0).getContents();

            // 使用computeIfAbsent
            Map<String, List<String>> mathContentMap = new LinkedHashMap<>();
            for (TermReview.MathContent mathContent : mathContents) {
                mathContentMap.computeIfAbsent(mathContent.getKpId(), k -> new ArrayList<>()).add(mathContent.getQuestionId());
            }

            Set<String> kpIdSet = mathContentMap.keySet();
            Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoader.loadKnowledgePoints(kpIdSet);
            for (String kpId : kpIdSet) {
                NewKnowledgePoint newKnowledgePoint = newKnowledgePointMap.get(kpId);
                Map<String, Object> mathContentInfoMap = new HashMap<>();
                mathContentInfoMap.put("kpid", kpId);
                mathContentInfoMap.put("name", newKnowledgePoint == null ? "" : SafeConverter.toString(newKnowledgePoint.getName(), ""));
                mathContentInfoMap.put("questionIds", mathContentMap.get(kpId));
                mathContentInfoMap.put("questionCount", mathContentMap.get(kpId).size());
                resultList.add(mathContentInfoMap);
            }
        } else {
            return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
        }
        return MapMessage.successMessage().add("resultList", resultList);
    }

    @Override
    public MapMessage loadBasicChineseContent(Teacher teacher, String bookId, Integer stageId) {
        Map<String, List> contentMap = Maps.newLinkedHashMap();
        List<ChineseBasicReview> chineseBasicReviews = termReviewLoaderClient.getTermReviewLoader().loadChineseBasicReviews(bookId);
        if (CollectionUtils.isEmpty(chineseBasicReviews)) {
            return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
        }
        ChineseBasicReview chineseBasicContent = chineseBasicReviews
                .stream()
                .filter(r -> Objects.equals(r.getRank(), stageId))
                .findFirst().orElse(null);
        if (chineseBasicContent == null) {
            return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
        }

        //用之前的方法，拼  ：lessonId|questionBoxId|questionBoxType|questionId|sectionNum|paragraphNum|paragraphImportant
        List<TermReview.ChineseBasicContent> chineseBasicContentList = chineseBasicContent.getContents();
        if (CollectionUtils.isEmpty(chineseBasicContentList)) {
            return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
        }
        //section data
        List<String> sectionIds = chineseBasicContentList.stream()
                .filter(c -> StringUtils.isNotEmpty(c.getSectionId()))
                .map(TermReview.ChineseBasicContent::getSectionId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sectionIds)) {
            return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
        }
        Map<String, NewBookCatalog> sectionMap = newContentLoaderClient.loadBookCatalogByCatalogIds(sectionIds);
        if (MapUtils.isEmpty(sectionMap)) {
            return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
        }
        //question data
        List<String> questionDocIdList = chineseBasicContentList.stream()
                .filter(c -> CollectionUtils.isNotEmpty(c.getQuestionIds()))
                .map(TermReview.ChineseBasicContent::getQuestionIds)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(questionDocIdList)) {
            return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
        }
        Map<String, NewQuestion> newQuestionDocIdsMap = questionLoaderClient.loadLatestQuestionByDocIds(questionDocIdList);
        if (MapUtils.isEmpty(newQuestionDocIdsMap)) {
            return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
        }
        //sentences data
        List<Long> allChineseSentenceIds = newQuestionDocIdsMap.values()
                .stream()
                .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                .map(NewQuestion::getSentenceIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(allChineseSentenceIds)) {
            return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
        }
        List<ChineseSentence> allChineseSentencesList = chineseContentLoaderClient.loadChineseSentenceByIds(allChineseSentenceIds);
        if (CollectionUtils.isEmpty(allChineseSentencesList)) {
            return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
        }
        Map<Long, ChineseSentence> allSentencesMap = allChineseSentencesList.stream().collect(Collectors.toMap(ChineseSentence::getId, Function.identity()));
        List<String> contentIds = Lists.newArrayList();
        for (TermReview.ChineseBasicContent basicContent : chineseBasicContentList) {
            NewBookCatalog section = sectionMap.get(basicContent.getSectionId());
            if (section == null) {
                continue;
            }
            String lessonId = section.lessonId();
            List<String> questionDocIds = basicContent.getQuestionIds();
            if (CollectionUtils.isEmpty(questionDocIds)) {
                continue;
            }
            List<NewQuestion> newQuestionList = Lists.newArrayList();
            //question 顺序保持
            questionDocIds.stream()
                    .filter(q -> newQuestionDocIdsMap.get(q) != null)
                    .forEach(q -> newQuestionList.add(newQuestionDocIdsMap.get(q)));
            if (CollectionUtils.isEmpty(newQuestionList)) {
                continue;
            }
            Map<String, List<Long>> qidSentenceIdsMap = newQuestionList
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getDocId, NewQuestion::getSentenceIds));
            List<Long> chineseSentenceIds = newQuestionList
                    .stream()
                    .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                    .map(NewQuestion::getSentenceIds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            List<ChineseSentence> chineseSentences = Lists.newArrayList();
            chineseSentenceIds.stream()
                    .filter(s -> allSentencesMap.get(s) != null)
                    .forEach(s -> chineseSentences.add(allSentencesMap.get(s)));
            //句子所在的章节号
            Map<Long, Integer> sentenceIdSection = chineseSentences
                    .stream()
                    .collect(Collectors.toMap(ChineseSentence::getId, ChineseSentence::getParagraphContinuous));
            //句子所在章节的段落号
            Map<Long, Integer> sentenceIdParagraph = chineseSentences
                    .stream()
                    .collect(Collectors.toMap(ChineseSentence::getId, ChineseSentence::getParagraph));
            //句子所在排行
            Map<Long, Integer> sentenceIdRank = chineseSentences
                    .stream()
                    .collect(Collectors.toMap(ChineseSentence::getId, ChineseSentence::getRank));
            //题的章节号
            Map<String, Integer> qidSectionMap = new HashMap<>();
            //题的段落号
            Map<String, Integer> qidParagraphMap = new HashMap<>();
            Map<Long, ChineseSentence> mapChineseSentences = chineseSentences
                    .stream()
                    .collect(Collectors.toMap(ChineseSentence::getId, o -> o));
            //重点句子id
            Set<Long> keyPointSentenceIds = new HashSet<>();
            for (Map.Entry<Long, ChineseSentence> entry : mapChineseSentences.entrySet()) {
                ChineseSentence chineseSentence = entry.getValue();
                if (chineseSentence == null) {
                    continue;
                }
                if (SafeConverter.toBoolean(chineseSentence.getReciteParagraph())) {
                    keyPointSentenceIds.add(entry.getKey());
                }
            }
            //计算重点段落
            Map<String, Boolean> qidKeyPointMap = new HashMap<>();
            if (MapUtils.isNotEmpty(qidSentenceIdsMap)) {
                for (Map.Entry<String, List<Long>> entry : qidSentenceIdsMap.entrySet()) {
                    String qid = entry.getKey();
                    List<Long> sentenceIds = entry.getValue();
                    boolean tag = true;
                    if (CollectionUtils.isNotEmpty(sentenceIds)) {
                        for (Long sentenceId : sentenceIds) {
                            if (!keyPointSentenceIds.contains(sentenceId)) {
                                tag = false;
                                break;
                            }
                        }
                        qidKeyPointMap.put(qid, tag);
                    }
                }
            }
            if (MapUtils.isNotEmpty(qidSentenceIdsMap)) {
                for (Map.Entry<String, List<Long>> entry : qidSentenceIdsMap.entrySet()) {
                    String questionDocId = entry.getKey();
                    List<Long> sentenceIds = entry.getValue();
                    Long sentenceId = 0L;
                    if (CollectionUtils.isNotEmpty(sentenceIds)) {
                        sentenceId = sentenceIds.iterator().next();
                    }
                    qidSectionMap.put(questionDocId, sentenceIdSection.get(sentenceId));
                    qidParagraphMap.put(questionDocId, sentenceIdParagraph.get(sentenceId));
                }
            }

            String questionBoxType = "";
            NewQuestion question = newQuestionDocIdsMap.get(questionDocIds.get(0));
            if (question == null) {
                continue;
            }
            if (SafeConverter.toInt(question.getContentTypeId()) == 1010014) {
                questionBoxType = QuestionBoxType.READ.toString();
            }
            if (SafeConverter.toInt(question.getContentTypeId()) == 1010015) {
                questionBoxType = QuestionBoxType.RECITE.toString();
            }
            if (StringUtils.isEmpty(questionBoxType)) {
                continue;
            }
            String questionBoxId = section.getId() + "-" + questionBoxType;
            String keyPrefix = StringUtils.join(Arrays.asList(lessonId, questionBoxId, questionBoxType), "|") + "|";
            List<String> questionInfoList = Lists.newArrayList();
            for (NewQuestion q : newQuestionList) {
                String sectionNum = SafeConverter.toString(qidSectionMap.get(q.getDocId()));
                String paragraphNum = SafeConverter.toString(qidParagraphMap.get(q.getDocId()));
                String paragraphImportant = qidKeyPointMap.get(q.getDocId()) ? "1" : "0";
                questionInfoList.add(StringUtils.join(Arrays.asList(q.getId(), sectionNum, paragraphNum, paragraphImportant), ":"));
            }
            contentIds.add(keyPrefix + StringUtils.join(questionInfoList, ","));
        }
        contentMap.put(ObjectiveConfigType.READ_RECITE_WITH_SCORE.toString(), contentIds);
        MapMessage message = previewContent(teacher, bookId, contentMap);
        message.add("stageName", chineseBasicContent.getName());
        return message;
    }

    @Override
    public MapMessage loadObjectiveWaterfallContent(TeacherDetail teacher, String objectiveId, Set<Long> groupIds, List<String> sectionIds, String bookId, String unitId, String sys, String appVersion) {
        TeachingObjective teachingObjective = teachingObjectiveLoaderClient.loadTeachingObjectById(objectiveId);
        if (teachingObjective == null) {
            return MapMessage.errorMessage("场景错误");
        }
        List<ObjectiveConfig> objectiveConfigs = teachingObjectiveLoaderClient.loadObjectiveConfigByTeachingObjectiveId(objectiveId);
        if (CollectionUtils.isEmpty(objectiveConfigs)) {
            return MapMessage.errorMessage("配置包为空");
        }
        boolean waterfall = false;
        if (teachingObjective.getIsHorizontal() != null) {
            waterfall = !teachingObjective.getIsHorizontal();
        }
        if (!waterfall) {
            return MapMessage.errorMessage("场景错误");
        }
        List<AppObjectiveConfigShowConfig> configShowConfigList = getAppObjConfig();
        Map<ObjectiveConfigType, AppObjectiveConfigShowConfig> configShowConfigMap = configShowConfigList
                .stream().collect(Collectors.toMap(c -> ObjectiveConfigType.of(c.getObjectiveConfigTypeName()), Function.identity()));
        List<Map<String, Object>> contentList = new ArrayList<>();
        for (ObjectiveConfig objectiveConfig : objectiveConfigs) {
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfig.getConfigType());
            if (objectiveConfigType == null) {
                continue;
            }
            //根据版本号和灰度过滤作业形式
            if (!showObjectiveConfigType(teacher, configShowConfigMap, objectiveConfigType, appVersion)) {
                continue;
            }
            boolean related = true;
            Subject subject = teacher.getSubject();
            if ((subject == Subject.MATH || subject == Subject.CHINESE) && CollectionUtils.isNotEmpty(objectiveConfig.getRelatedCatalogs())) {
                related = false;
                for (EmbedBook book : objectiveConfig.getRelatedCatalogs()) {
                    // 按照sectionId进行过滤
                    if (book == null || book.getSectionId() == null || sectionIds.contains(book.getSectionId())) {
                        related = true;
                        break;
                    }
                }
            }
            if (related && objectiveConfigType != null) {
                processWaterfallContent(teacher, groupIds, sectionIds, bookId, unitId, objectiveConfigType, contentList, objectiveConfig);
            }
        }
        return MapMessage.successMessage()
                //.add("objectiveConfigList", objectiveConfigs)
                .add("contentList", contentList);
    }

    private void processWaterfallContent(TeacherDetail teacher, Set<Long> groupIds, List<String> sectionIds, String bookId, String unitId, ObjectiveConfigType objectiveConfigType, List<Map<String, Object>> contentList, ObjectiveConfig objectiveConfig) {
        NewHomeworkContentLoaderTemplate template = newHomeworkContentLoaderFactory.getTemplate(objectiveConfigType);
        if (template != null) {
            NewHomeworkContentLoaderMapper mapper = new NewHomeworkContentLoaderMapper();
            mapper.setTeacher(teacher);
            mapper.setObjectiveConfig(objectiveConfig);
            mapper.setGroupIds(groupIds);
            mapper.setSectionIds(sectionIds);
            mapper.setBookId(bookId);
            mapper.setUnitId(unitId);
            mapper.setWaterfall(true);
            mapper.setCurrentPageNum(1);

            Map<String, Object> content = template.loadWaterfallContent(mapper);
            if (MapUtils.isNotEmpty(content)) {
                contentList.add(content);
            }
        }
    }

    @Override
    public MapMessage loadBasicAppWaterfallContent(TeacherDetail teacher, String objectiveConfigId, String bookId, String unitId, String categoryGroup) {
        ObjectiveConfig objectiveConfig = teachingObjectiveLoaderClient.loadObjectiveConfigByIds(Collections.singleton(objectiveConfigId)).get(objectiveConfigId);
        if (objectiveConfig == null || objectiveConfig.getConfigType() == null) {
            return MapMessage.errorMessage("配置包错误");
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfig.getConfigType());
        if (ObjectiveConfigType.BASIC_APP != objectiveConfigType) {
            return MapMessage.errorMessage("配置包错误");
        }
        BasicAppCategoryType basicAppCategoryType = BasicAppCategoryType.of(categoryGroup);
        if (basicAppCategoryType == null) {
            return MapMessage.errorMessage("应用分组错误");
        }
        NewHomeworkContentLoaderMapper mapper = new NewHomeworkContentLoaderMapper();
        mapper.setTeacher(teacher);
        mapper.setObjectiveConfig(objectiveConfig);
        mapper.setWaterfall(false);
        mapper.setBookId(bookId);
        mapper.setUnitId(unitId);
        List<Map<String, Object>> contents = newHomeworkBasicAppContentLoader.loadContent(mapper);
        Map<String, Object> content = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(contents)) {
            for (Map<String, Object> contentMapper : contents) {
                String mapperCategoryGroup = SafeConverter.toString(contentMapper.get("categoryGroup"));
                if (StringUtils.equals(categoryGroup, mapperCategoryGroup)) {
                    content = contentMapper;
                    break;
                }
            }
        }
        if (MapUtils.isNotEmpty(content)) {
            return MapMessage.successMessage().add("content", Collections.singletonList(content));
        }
        return MapMessage.errorMessage("未找到合适的内容");
    }

    @Override
    public MapMessage loadOralCommunicationSearchWords() {
        List<String> recommendSearchWords = Collections.emptyList();
        try {
            recommendSearchWords = oralCommunicationSearchClient.getOralCommSearchLoader().getRecommendedQueryWords();
        } catch (Exception e) {
            logger.error("NewHomeworkContentServiceImpl call athena error:", e);
        }
        return MapMessage.successMessage().add("words", recommendSearchWords);
    }

    @Override
    public MapMessage searchOralCommunication(TeacherDetail teacherDetail, String clazzLevel, String type, String searchWord, String bookId, String uniId, Pageable pageable) {
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(uniId)) {
            return MapMessage.errorMessage("教材信息错误");
        }
        NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        int startGrade = 1;
        if (bookProfile != null) {
            startGrade = SafeConverter.toInt(bookProfile.getStartClazzLevel(), 1);
        }
        List<StoneBufferedData> stoneBufferedDataList = new ArrayList<>();
        // 判断有没有搜索词，有搜索词则调用大数据接口
        if (StringUtils.isNotEmpty(searchWord)) {
            if (StringUtils.isBlank(searchWord)) {
                return MapMessage.errorMessage("请输入要搜索的内容");
            }
            try {
                List<String> stoneIds = oralCommunicationSearchClient.getOralCommSearchLoader().getOralCommsByCondition(searchWord);
                stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(stoneIds);
            } catch (Exception e) {
                logger.error("NewHomeworkContentServiceImpl call athena error:", e);
            }
        } else {
            stoneBufferedDataList = stoneDataLoaderClient.loadAllStoneBufferedDataList();
        }
        OralCommunicationClazzLevel oralCommunicationClazzLevel = OralCommunicationClazzLevel.of(clazzLevel);
        List<OralCommunicationContentType> oralCommunicationContentTypeList = Lists.newArrayList();
        if (StringUtils.isNotEmpty(type)) {
            Set<String> types = Sets.newHashSet(StringUtils.split(type, ","));
            types.forEach(t -> oralCommunicationContentTypeList.add(OralCommunicationContentType.of(t)));
        }
        List<StoneBufferedData> oralCommunicationDataList = stoneBufferedDataList
                .stream()
                .filter(data -> data != null && (data.getOralPracticeConversion() != null || data.getInteractiveVideo() != null || data.getInteractivePictureBook() != null))
                .filter(data -> data.getDeletedAt() == null)
                .filter(data -> {
                    BaseOralPractice baseOralPractice = null;
                    if (data.getOralPracticeConversion() != null) {
                        baseOralPractice = data.getOralPracticeConversion();
                    }
                    if (data.getInteractivePictureBook() != null) {
                        baseOralPractice = data.getInteractivePictureBook();
                    }
                    if (data.getInteractiveVideo() != null) {
                        baseOralPractice = data.getInteractiveVideo();
                    }
                    if (baseOralPractice == null || (!"online".equals(baseOralPractice.getOlStatus()))) {
                        return false;
                    }
                    if (oralCommunicationClazzLevel == null) {
                        return true;
                    }
                    return oralCommunicationClazzLevel.getGrade().equals(baseOralPractice.getGrade());
                })
                .filter(data -> {
                    if (CollectionUtils.isEmpty(oralCommunicationContentTypeList)) {
                        return true;
                    }
                    if (data.getInteractiveVideo() != null) {
                        return oralCommunicationContentTypeList.contains(OralCommunicationContentType.INTERACTIVE_VIDEO);
                    }
                    if (data.getOralPracticeConversion() != null) {
                        return oralCommunicationContentTypeList.contains(OralCommunicationContentType.INTERACTIVE_CONVERSATION);
                    }
                    if (data.getInteractivePictureBook() != null) {
                        return oralCommunicationContentTypeList.contains(OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK);
                    }
                    return false;
                })
                .collect(Collectors.toList());
        //一起教材：低中年级>全年级>高年级  ;   三起教材：高年级>全年级>低中年级的顺序展示
        if (startGrade == 1) {
            List<OralCommunicationClazzLevel> oneStartOrder = Lists.newArrayList(OralCommunicationClazzLevel.LOW, OralCommunicationClazzLevel.ALL, OralCommunicationClazzLevel.HIGH);
            oralCommunicationDataList = oralCommunicationDataList.stream().sorted((s1, s2) -> {
                        OralCommunicationClazzLevel level1 = NewHomeworkContentDecorator.getOralOralCommunicationLevel(s1);
                        OralCommunicationClazzLevel level2 = NewHomeworkContentDecorator.getOralOralCommunicationLevel(s2);
                        return oneStartOrder.indexOf(level1)
                                - oneStartOrder.indexOf(level2);
                    }
            ).collect(Collectors.toList());
        }
        if (startGrade == 3) {
            List<OralCommunicationClazzLevel> oneStartOrder = Lists.newArrayList(OralCommunicationClazzLevel.HIGH, OralCommunicationClazzLevel.ALL, OralCommunicationClazzLevel.LOW);
            oralCommunicationDataList = oralCommunicationDataList.stream().sorted((s1, s2) -> {
                        OralCommunicationClazzLevel level1 = NewHomeworkContentDecorator.getOralOralCommunicationLevel(s1);
                        OralCommunicationClazzLevel level2 = NewHomeworkContentDecorator.getOralOralCommunicationLevel(s2);
                        return oneStartOrder.indexOf(level1)
                                - oneStartOrder.indexOf(level2);
                    }
            ).collect(Collectors.toList());
        }
        Page<StoneBufferedData> stoneBufferedDataPage = PageableUtils.listToPage(oralCommunicationDataList, pageable);
        EmbedBook book = new EmbedBook();
        book.setBookId(bookId);
        book.setUnitId(uniId);
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacherDetail.getSubject(), teacherDetail.getId(), bookId);
        return MapMessage.successMessage()
                .add("oralCommunicationList", stoneBufferedDataPage.getContent().stream().map(oralCommunication -> NewHomeworkContentDecorator.decorateOralCommunicationSummary(oralCommunication, book, teacherAssignmentRecord)).collect(Collectors.toList()))
                .add("totalSize", stoneBufferedDataPage.getTotalElements())
                .add("pageCount", stoneBufferedDataPage.getTotalPages())
                .add("pageNum", stoneBufferedDataPage.getNumber() + 1);
    }

    @Override
    public MapMessage loadOralCommunicationDetail(TeacherDetail teacherDetail, String bookId, String unitId, String oralCommunicationId) {
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(Collections.singleton(oralCommunicationId));
        if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
            return MapMessage.errorMessage("口语交际id错误");
        }
        EmbedBook embedBook = new EmbedBook();
        embedBook.setBookId(bookId);
        embedBook.setUnitId(unitId);
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacherDetail.getSubject(), teacherDetail.getId(), bookId);
        return MapMessage.successMessage()
                .add("oralCommunicationDetail", NewHomeworkContentDecorator.decorateOralCommunicationDetail(stoneBufferedDataList.get(0), embedBook, teacherAssignmentRecord));
    }


    /**
     * 过滤题型
     *
     * @param teacher
     * @param configMap
     * @param type
     * @param appVersion
     * @return
     */
    private boolean showObjectiveConfigType(TeacherDetail teacher, Map<ObjectiveConfigType, AppObjectiveConfigShowConfig> configMap, ObjectiveConfigType type, String appVersion) {
        if (!configMap.containsKey(type) || configMap.get(type) == null) {
            return true;
        }
        //常规处理
        AppObjectiveConfigShowConfig appObjectiveConfigShowConfig = configMap.get(type);
        boolean greyMatch = true;
        boolean matchVersion = true;
        if (StringUtils.isNotEmpty(appObjectiveConfigShowConfig.getMainFunctionName())
                && StringUtils.isNotEmpty(appObjectiveConfigShowConfig.getSubFunctionName())) {
            greyMatch = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, appObjectiveConfigShowConfig.getMainFunctionName(), appObjectiveConfigShowConfig.getSubFunctionName());
        }
        if (StringUtils.isNotEmpty(appVersion) && StringUtils.isNotEmpty(appObjectiveConfigShowConfig.getMinVersion())) {
            matchVersion = VersionUtil.compareVersion(appVersion, appObjectiveConfigShowConfig.getMinVersion()) >= 0;
        }
        if (StringUtils.isNotEmpty(appVersion) && StringUtils.isNotEmpty(appObjectiveConfigShowConfig.getMaxVersion())) {
            matchVersion = (VersionUtil.compareVersion(appObjectiveConfigShowConfig.getMaxVersion(), appVersion) >= 0) && matchVersion;
        }
        //特殊题型处理
        if (ObjectiveConfigType.DUBBING.equals(type)) {
            return greyMatch && matchVersion && !showObjectiveConfigType(teacher, configMap, ObjectiveConfigType.DUBBING_WITH_SCORE, appVersion);
        }
        return greyMatch && matchVersion;
    }

    /**
     * 获取题型显示隐藏配置
     *
     * @return
     */
    private List<AppObjectiveConfigShowConfig> getAppObjConfig() {
        //读取页面内容的配置信息
        List<PageBlockContent> teacherTask = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("AppVersionShow");
        if (CollectionUtils.isEmpty(teacherTask)) {
            return Collections.emptyList();
        }
        PageBlockContent configPageBlockContent = teacherTask.stream()
                .filter(p -> "AppVersionShowConfig".equals(p.getBlockName()))
                .findFirst()
                .orElse(null);
        if (configPageBlockContent == null) {
            return Collections.emptyList();
        }
        String configContent = configPageBlockContent.getContent();
        if (StringUtils.isBlank(configContent)) {
            return Collections.emptyList();
        }
        configContent = configContent.replaceAll("[\n\r\t]", "").trim();
        return JsonUtils.fromJsonToList(configContent, AppObjectiveConfigShowConfig.class);
    }

    /**
     * 题型显示隐藏配置信息
     */
    @Getter
    @Setter
    private static class AppObjectiveConfigShowConfig implements Serializable {
        private static final long serialVersionUID = -4242701326468676048L;
        private String objectiveConfigTypeName;
        private String mainFunctionName;
        private String subFunctionName;
        private String minVersion;
        private String maxVersion;
    }

    @Override
    public MapMessage loadTeachingResourceDefaultBook(TeacherDetail teacherDetail) {
        TeachingResourceBook teachingResourceBook = teachingResourceBookPersistence.load(teacherDetail.getId());
        if (teachingResourceBook == null || StringUtils.isEmpty(teachingResourceBook.getBookId())) {
            return MapMessage.errorMessage("未找到合适的教材");
        }
        String bookId = teachingResourceBook.getBookId();
        NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (bookProfile == null) {
            return MapMessage.errorMessage("未找到合适的教材");
        }
        Map<String, Object> bookMapper = buildBookMapper(bookProfile, null);
        return MapMessage.successMessage().add("book", bookMapper);
    }

    @Override
    public MapMessage changeTeachingResourceDefaultBook(TeacherDetail teacherDetail, String bookId) {
        TeachingResourceBook teachingResourceBook = new TeachingResourceBook();
        teachingResourceBook.setId(teacherDetail.getId());
        teachingResourceBook.setBookId(bookId);
        teachingResourceBookPersistence.upsert(teachingResourceBook);
        return MapMessage.successMessage("更换成功");
    }

    @Override
    public MapMessage loadTeachingResourceTypeList(TeacherDetail teacherDetail, String bookId, String unitId) {
        List<Map<String, Object>> typeMapperList;
        typeMapperList = DaiTeType.getTypesBySubject(teacherDetail.getSubject());
        return MapMessage.successMessage().add("typeList", typeMapperList);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapMessage loadTeachingResourceContent(TeacherDetail teacherDetail, String bookId, String unitId, String sectionId, String type, Map params) {
        Object content;
        MapMessage mapMessage = MapMessage.successMessage();
        DaiTeType daiTeType = DaiTeType.of(type);
        if (daiTeType == null) {
            return MapMessage.errorMessage("暂无该类型数据");
        }
        DaiTeTypeTemplate daiTeTypeTemplate = daiTeTypeFactory.getTemplate(daiTeType);
        if (daiTeTypeTemplate == null) {
            return MapMessage.errorMessage("暂无该类型数据");
        }
        NewHomeworkContentLoaderMapper mapper = new NewHomeworkContentLoaderMapper();
        mapper.setTeacher(teacherDetail);
        mapper.setBookId(bookId);
        mapper.setUnitId(unitId);
        mapper.setSectionIds(Collections.singletonList(sectionId));
        mapper.setGroupIds(Collections.singleton(123456L));
        mapper.setHomeworkSourceType(HomeworkSourceType.Web);
        mapper.setObjectiveConfig(new ObjectiveConfig());
        content = daiTeTypeTemplate.getDaiTeDataByType(mapper, params);
        return mapMessage.add("content", content);
    }

    @Override
    public MapMessage loadNaturalSpellingLevelsContent(String bookId) {
        MapMessage mapMessage = MapMessage.successMessage();
        List<Map<String, Object>> levelList = new ArrayList<>();
        int defaultLevel = newHomeworkNaturalSpellingContentLoader.processDefaultLevel(bookId);
        for (int level = 1; level <= 3; level++) {
            levelList.add(MapUtils.m("level", level, "levelName", "Level " + level, "defaultLevel", level == defaultLevel));
        }
        return mapMessage.add("levels", levelList);
    }

    @Override
    public List<Integer> loadHomeworkReportShareChannel(TeacherDetail teacherDetail) {
        return homeworkReportShareChannelHelper.loadHomeworkReportShareChannel(teacherDetail);
    }

    @Override
    public MapMessage loadOcrMentalBookList(Teacher teacher) {
        List<OcrMentalBook> bookList = ocrMentalBookDao.loadTeacherBooks(teacher.getId());
        List<Map<String, Object>> bookMapperList = bookList.stream()
                .map(book -> MapUtils.m("bookId", book.getId(), "bookName", book.getBookName()))
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("bookList", bookMapperList);
    }

    @Override
    public MapMessage addOcrMentalBook(Teacher teacher, String bookName) {
        OcrMentalBook ocrMentalBook = new OcrMentalBook();
        ocrMentalBook.setBookName(bookName);
        ocrMentalBook.setTeacherId(teacher.getId());
        ocrMentalBook.setId(RandomUtils.nextObjectId());
        ocrMentalBook.setDisabled(false);
        ocrMentalBookDao.upsert(ocrMentalBook);
        return MapMessage.successMessage("添加成功");
    }

    @Override
    public MapMessage deleteOcrMentalBook(Teacher teacher, String bookId) {
        OcrMentalBook ocrMentalBook = ocrMentalBookDao.load(bookId);
        if (ocrMentalBook == null || SafeConverter.toBoolean(ocrMentalBook.getDisabled())) {
            return MapMessage.errorMessage("课本不存在或已被删除");
        }
        ocrMentalBook.setDisabled(true);
        ocrMentalBookDao.upsert(ocrMentalBook);
        return MapMessage.successMessage("删除成功");
    }

    @Override
    public MapMessage loadOcrDictationUnitList(Teacher teacher, List<Long> groupIds) {
        // 根据groupId查询clazzId
        Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(groupIds, false);
        if (MapUtils.isNotEmpty(groupMapperMap)) {
            Map<Long, Long> clazzGroupMap = new LinkedHashMap<>();
            for (GroupMapper groupMapper : groupMapperMap.values()) {
                clazzGroupMap.put(groupMapper.getClazzId(), groupMapper.getId());
            }
            return loadClazzBook(teacher, clazzGroupMap, false);
        }
        return MapMessage.errorMessage("未找到合适的课本");
    }

    @Override
    public MapMessage loadOcrDictationContent(Teacher teacher, String bookId, String unitId) {
        NewHomeworkContentLoaderMapper mapper = new NewHomeworkContentLoaderMapper();
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        mapper.setTeacher(teacherDetail);
        mapper.setBookId(bookId);
        mapper.setUnitId(unitId);
        return MapMessage.successMessage().add("content", newHomeworkDictationContentLoader.loadContent(mapper));
    }
}
