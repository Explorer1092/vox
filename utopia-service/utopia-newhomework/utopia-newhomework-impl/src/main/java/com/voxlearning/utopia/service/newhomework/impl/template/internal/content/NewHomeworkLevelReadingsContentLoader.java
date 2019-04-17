package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.athena.api.search.entity.PictureBookInfo;
import com.voxlearning.athena.api.search.entity.PictureBookPackage;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookPracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.athena.RecommendPictureBookLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.constant.PictureBookApply;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class NewHomeworkLevelReadingsContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private RecommendPictureBookLoaderClient recommendPictureBookLoaderClient;

    private static final Map<Integer, String> readingHabitsMap = new LinkedHashMap<>();

    static {
        readingHabitsMap.put(1, "固定频率的阅读有利于孩子形成阅读习惯，大纲推荐一年级孩子每周阅读1-2次，阅读1-2本绘本读物");
        readingHabitsMap.put(2, "固定频率的阅读有利于孩子形成阅读习惯，大纲推荐二年级孩子每周阅读2次，阅读2本绘本读物");
        readingHabitsMap.put(3, "固定频率的阅读有利于孩子形成阅读习惯，大纲推荐三年级孩子每周阅读2-3次，阅读2-3本绘本读物");
        readingHabitsMap.put(4, "固定频率的阅读有利于孩子形成阅读习惯，大纲推荐四年级孩子每周阅读3次，阅读3本绘本读物");
        readingHabitsMap.put(5, "固定频率的阅读有利于孩子形成阅读习惯，大纲推荐五年级孩子每周阅读3次，阅读3本绘本读物");
        readingHabitsMap.put(6, "固定频率的阅读有利于孩子形成阅读习惯，大纲推荐六年级孩子每周阅读4次，阅读3-4本绘本读物");
    }

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.LEVEL_READINGS;
    }

    /**
     * 加载为戴特提供的绘本
     * @param mapper
     * @return
     */
    public List<Map<String, Object>> loadDaiTeContent(NewHomeworkContentLoaderMapper mapper) {
        Subject subject = mapper.getTeacher().getSubject();
        List<Map<String, Object>> content = new ArrayList<>();
        // 查询所有绘本
        List<PictureBookPlus> allPictureBookPlus = pictureBookPlusServiceClient.loadAllOnline()
                .stream()
                .filter(pictureBookPlus -> Objects.equals(pictureBookPlus.getSubjectId(), subject.getId()))
                // 过滤非作业端的绘本
                .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                .collect(Collectors.toList());
        Collator collator = Collator.getInstance(java.util.Locale.CHINA);
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries()
                .stream()
                .filter(pictureBookSeries -> Objects.equals(pictureBookSeries.getSubjectId(), subject.getId()))
                .sorted((a, b) -> collator.compare(a.fetchName(), b.fetchName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(pictureBookSeriesList)) {
            Set<String> existsPictureBookSeriesSet = allPictureBookPlus.stream()
                    .map(PictureBookPlus::getSeriesId)
                    .collect(Collectors.toSet());
            pictureBookSeriesList = pictureBookSeriesList.stream()
                    .filter(pictureBookSeries -> existsPictureBookSeriesSet.contains(pictureBookSeries.getId()))
                    .collect(Collectors.toList());
        }
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));

        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics()
                .stream()
                .filter(pictureBookTopic -> Objects.equals(pictureBookTopic.getSubjectId(), subject.getId()))
                .sorted(Comparator.comparingInt(PictureBookTopic::getRank))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(pictureBookTopicList)) {
            Set<String> existsPictureBookTopicSet = allPictureBookPlus.stream()
                    .map(PictureBookPlus::getTopicIds)
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            pictureBookTopicList = pictureBookTopicList.stream()
                    .filter(pictureBookTopic -> existsPictureBookTopicSet.contains(pictureBookTopic.getId()))
                    .collect(Collectors.toList());
        }
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));

        HomeworkSourceType homeworkSourceType = mapper.getHomeworkSourceType();
        Set<String> configPictureBookIds = new LinkedHashSet<>();
        if (Subject.ENGLISH == subject) {
            processEnglishRecommend(mapper.getTeacher(), mapper.getBookId(), mapper.getUnitId(), mapper.getGroupIds(), homeworkSourceType, mapper.getSys(), mapper.getAppVersion(), pictureBookSeriesMap, pictureBookTopicMap, content);
        } else if (Subject.CHINESE == subject && CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                List<String> pictureBookIds = (List<String>) configContent.get("picture_book_ids");
                if (CollectionUtils.isNotEmpty(pictureBookIds)) {
                    configPictureBookIds.addAll(pictureBookIds);
                }
            }
        }

        // 全部绘本
        // 班级列表
        List<PictureBookNewClazzLevel> pictureBookNewClazzLevels = PictureBookNewClazzLevel.primarySchoolLevels();
        if (Subject.CHINESE == subject) {
            pictureBookNewClazzLevels = Arrays.asList(PictureBookNewClazzLevel.L1A, PictureBookNewClazzLevel.L2A, PictureBookNewClazzLevel.L3A);
        }
        List<Map<String, Object>> clazzLevelMapperList = new ArrayList<>();
        if (Subject.CHINESE == subject) {
            pictureBookNewClazzLevels.forEach(level -> clazzLevelMapperList.add(MapUtils.m("levelId", level.name(), "levelName", NewHomeworkUtils.processChinesePictureBookClazzLevel(level.name()))));
        } else {
            pictureBookNewClazzLevels.forEach(level -> clazzLevelMapperList.add(MapUtils.m("levelId", level.name(), "levelName", level.getLevelName())));
        }
        // 系列列表
        List<Map<String, Object>> seriesMapperList = new ArrayList<>();
        pictureBookSeriesList.forEach(series -> seriesMapperList.add(MapUtils.m("seriesId", series.getId(), "seriesName", series.fetchName())));
        // 主题列表
        List<Map<String, Object>> topicMapperList = new ArrayList<>();
        pictureBookTopicList.forEach(topic -> topicMapperList.add(MapUtils.m("topicId", topic.getId(), "topicName", topic.getName())));
        if (Subject.CHINESE == subject && CollectionUtils.isNotEmpty(configPictureBookIds)) {
            Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(configPictureBookIds);
            List<PictureBookPlus> pictureBookPlusList = MapUtils.resort(pictureBookPlusMap, configPictureBookIds).values()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(PictureBookPlus::isOnline)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(pictureBookPlusList)) {
                EmbedBook book = new EmbedBook();
                book.setBookId(mapper.getBookId());
                book.setUnitId(mapper.getUnitId());
                TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(subject, mapper.getTeacher().getId(), mapper.getBookId());
                List<Map<String, Object>> configPbList = pictureBookPlusList
                        .stream()
                        .map(p -> NewHomeworkContentDecorator.decoratePictureBookPlus(p, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, mapper.getSys(), mapper.getAppVersion()))
                        .collect(Collectors.toList());
                content.add(MapUtils.m("module", "syncRecommend", "moduleName", "课堂同步拓展", "description", "与本单元话题匹配的绘本", "pictureBookList", configPbList, "showType", "recommend"));
            }
        }
        content.add(MapUtils.m("module", "all", "moduleName", "全部绘本", "description", "", "clazzLevelList", clazzLevelMapperList, "seriesList", seriesMapperList, "topicList", topicMapperList, "showType", "all"));
        return content;
    }

    /**
     * 加载布置作业需要的绘本数据
     * @param mapper
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        Subject subject = mapper.getTeacher().getSubject();
        List<Map<String, Object>> content = new ArrayList<>();
        // 查询所有绘本
        List<PictureBookPlus> allPictureBookPlus = pictureBookPlusServiceClient.loadAllOnline()
                .stream()
                .filter(pictureBookPlus -> Objects.equals(pictureBookPlus.getSubjectId(), subject.getId()))
                // 过滤非作业端的绘本
                .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                .collect(Collectors.toList());
        Collator collator = Collator.getInstance(java.util.Locale.CHINA);
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries()
                .stream()
                .filter(pictureBookSeries -> Objects.equals(pictureBookSeries.getSubjectId(), subject.getId()))
                .sorted((a, b) -> collator.compare(a.fetchName(), b.fetchName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(pictureBookSeriesList)) {
            Set<String> existsPictureBookSeriesSet = allPictureBookPlus.stream()
                    .map(PictureBookPlus::getSeriesId)
                    .collect(Collectors.toSet());
            pictureBookSeriesList = pictureBookSeriesList.stream()
                    .filter(pictureBookSeries -> existsPictureBookSeriesSet.contains(pictureBookSeries.getId()))
                    .collect(Collectors.toList());
        }
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));

        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics()
                .stream()
                .filter(pictureBookTopic -> Objects.equals(pictureBookTopic.getSubjectId(), subject.getId()))
                .sorted(Comparator.comparingInt(PictureBookTopic::getRank))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(pictureBookTopicList)) {
            Set<String> existsPictureBookTopicSet = allPictureBookPlus.stream()
                    .map(PictureBookPlus::getTopicIds)
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            pictureBookTopicList = pictureBookTopicList.stream()
                    .filter(pictureBookTopic -> existsPictureBookTopicSet.contains(pictureBookTopic.getId()))
                    .collect(Collectors.toList());
        }
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));

        HomeworkSourceType homeworkSourceType = mapper.getHomeworkSourceType();
        Set<String> configPictureBookIds = new LinkedHashSet<>();
        if (Subject.ENGLISH == subject) {
            processEnglishRecommend(mapper.getTeacher(), mapper.getBookId(), mapper.getUnitId(), mapper.getGroupIds(), homeworkSourceType, mapper.getSys(), mapper.getAppVersion(), pictureBookSeriesMap, pictureBookTopicMap, content);
        } else if (Subject.CHINESE == subject && CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                List<String> pictureBookIds = (List<String>) configContent.get("picture_book_ids");
                if (CollectionUtils.isNotEmpty(pictureBookIds)) {
                    configPictureBookIds.addAll(pictureBookIds);
                }
            }
        }

        // 全部绘本
        // 班级列表
        List<PictureBookNewClazzLevel> pictureBookNewClazzLevels = PictureBookNewClazzLevel.primarySchoolLevels();
        if (Subject.CHINESE == subject) {
            pictureBookNewClazzLevels = Arrays.asList(PictureBookNewClazzLevel.L1A, PictureBookNewClazzLevel.L2A, PictureBookNewClazzLevel.L3A);
        }
        List<Map<String, Object>> clazzLevelMapperList = new ArrayList<>();
        if (HomeworkSourceType.App == homeworkSourceType) {
            if (Subject.CHINESE == subject) {
                clazzLevelMapperList.add(MapUtils.m("levelId", "", "levelName", "全部等级"));
            } else {
                clazzLevelMapperList.add(MapUtils.m("levelId", "", "levelName", "全部年级"));
            }
        }
        if (Subject.CHINESE == subject) {
            pictureBookNewClazzLevels.forEach(level -> clazzLevelMapperList.add(MapUtils.m("levelId", level.name(), "levelName", NewHomeworkUtils.processChinesePictureBookClazzLevel(level.name()))));
        } else {
            pictureBookNewClazzLevels.forEach(level -> clazzLevelMapperList.add(MapUtils.m("levelId", level.name(), "levelName", level.getLevelName())));
        }
        // 系列列表
        List<Map<String, Object>> seriesMapperList = new ArrayList<>();
        if (HomeworkSourceType.App == homeworkSourceType) {
            seriesMapperList.add(MapUtils.m("seriesId", "", "seriesName", "全部系列"));
        }
        pictureBookSeriesList.forEach(series -> seriesMapperList.add(MapUtils.m("seriesId", series.getId(), "seriesName", series.fetchName())));
        // 主题列表
        List<Map<String, Object>> topicMapperList = new ArrayList<>();
        if (HomeworkSourceType.App == homeworkSourceType) {
            topicMapperList.add(MapUtils.m("topicId", "", "topicName", "全部主题"));
        }
        pictureBookTopicList.forEach(topic -> topicMapperList.add(MapUtils.m("topicId", topic.getId(), "topicName", topic.getName())));
        if (Subject.CHINESE == subject && CollectionUtils.isNotEmpty(configPictureBookIds)) {
            Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(configPictureBookIds);
            List<PictureBookPlus> pictureBookPlusList = MapUtils.resort(pictureBookPlusMap, configPictureBookIds).values()
                    .stream()
                    .filter(Objects::nonNull)
                    // 过滤未发布的绘本
                    .filter(PictureBookPlus::isOnline)
                    // 过滤非作业端的绘本
                    .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(pictureBookPlusList)) {
                EmbedBook book = new EmbedBook();
                book.setBookId(mapper.getBookId());
                book.setUnitId(mapper.getUnitId());
                TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(subject, mapper.getTeacher().getId(), mapper.getBookId());
                List<Map<String, Object>> configPbList = pictureBookPlusList
                        .stream()
                        .map(p -> NewHomeworkContentDecorator.decoratePictureBookPlus(p, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, mapper.getSys(), mapper.getAppVersion()))
                        .collect(Collectors.toList());
                content.add(MapUtils.m("module", "syncRecommend", "moduleName", "课堂同步拓展", "description", "与本单元话题匹配的绘本", "pictureBookList", configPbList, "showType", "recommend"));
                if (HomeworkSourceType.App == mapper.getHomeworkSourceType()) {
                    content.add(MapUtils.m("module", "all", "moduleName", "全部绘本", "description", "", "clazzLevelList", clazzLevelMapperList, "seriesList", seriesMapperList, "topicList", topicMapperList, "showType", "all"));
                }
            }
        } else {
            content.add(MapUtils.m("module", "all", "moduleName", "全部绘本", "description", "", "clazzLevelList", clazzLevelMapperList, "seriesList", seriesMapperList, "topicList", topicMapperList, "showType", "all"));
        }
        return content;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        try {
            Subject subject = mapper.getTeacher().getSubject();
            switch (subject) {
                case CHINESE:
                    return loadChineseWaterfallContent(mapper);
                case ENGLISH:
                    return loadEnglishWaterfallContent(mapper);
                default:
                    return Collections.emptyMap();
            }
        } catch (Exception e) {
            logger.error("Failed to load NewHomeworkLevelReadingsContent, mapper:{}", mapper, e);
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadEnglishWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        Set<String> allModules = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(objectiveConfig.getContents())) {
            for (Map<String, Object> content : objectiveConfig.getContents()) {
                List<String> modules = (List<String>) content.get("module");
                if (CollectionUtils.isNotEmpty(modules)) {
                    for (String module : modules) {
                        if (StringUtils.isNotBlank(module)) {
                            allModules.add(module);
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(allModules)) {
            List<Map<String, Object>> content = new ArrayList<>();
            List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                    .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));

            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                    .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
            processEnglishRecommend(mapper.getTeacher(), mapper.getBookId(), mapper.getUnitId(), mapper.getGroupIds(), HomeworkSourceType.App, null, null, pictureBookSeriesMap, pictureBookTopicMap, content);
            List<Map<String, Object>> pictureBookMappers = new ArrayList();
            if (CollectionUtils.isNotEmpty(content)) {
                Map<String, List<Map<String, Object>>> modulePictureBooksMap = new HashMap<>();
                for (Map<String, Object> contentMapper : content) {
                    String module = SafeConverter.toString(contentMapper.get("module"));
                    List<Map<String, Object>> pictureBookList = (List<Map<String, Object>>) contentMapper.get("pictureBookList");
                    if (CollectionUtils.isNotEmpty(pictureBookList)) {
                        modulePictureBooksMap.put(module, pictureBookList);
                    }
                }
                for (String module : allModules) {
                    if (modulePictureBooksMap.containsKey(module)) {
                        pictureBookMappers.addAll(modulePictureBooksMap.get(module));
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(pictureBookMappers)) {
                List<Map<String, Object>> clazzLevelMapperList = new ArrayList<>();
                clazzLevelMapperList.add(MapUtils.m("levelId", "", "levelName", "全部年级"));
                List<PictureBookNewClazzLevel> pictureBookNewClazzLevels = PictureBookNewClazzLevel.primarySchoolLevels();
                pictureBookNewClazzLevels.forEach(level -> clazzLevelMapperList.add(MapUtils.m("levelId", level.name(), "levelName", level.getLevelName())));

                return MapUtils.m(
                        "objectiveConfigId", objectiveConfig.getId(),
                        "type", getObjectiveConfigType().name(),
                        "typeName", getObjectiveConfigType().getValue(),
                        "name", objectiveConfig.getName(),
                        "pictureBookList", pictureBookMappers,
                        "previewUrl", UrlUtils.buildUrlQuery(TopLevelDomain.getHttpsMainSiteBaseUrl() + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html", MapUtils.m("from", "preview")),
                        "clazzLevelList", clazzLevelMapperList
                );
            }
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadChineseWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        List<String> pictureBookIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(objectiveConfig.getContents())) {
            for (Map<String, Object> content : objectiveConfig.getContents()) {
                pictureBookIds = (List<String>) content.get("picture_book_ids");
            }
        }
        Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(pictureBookIds);

        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));

        EmbedBook book = new EmbedBook();
        book.setBookId(mapper.getBookId());
        book.setUnitId(mapper.getUnitId());
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(Subject.CHINESE, mapper.getTeacher().getId(), mapper.getBookId());

        List<Map<String, Object>> pictureBookMappers = pictureBookPlusMap.values()
                .stream()
                // 过滤未发布的绘本
                .filter(PictureBookPlus::isOnline)
                // 过滤非作业端的绘本
                .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                .map(p -> NewHomeworkContentDecorator.decoratePictureBookPlus(p, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, mapper.getSys(), mapper.getAppVersion()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(pictureBookMappers)) {
            List<Map<String, Object>> clazzLevelMapperList = new ArrayList<>();
            clazzLevelMapperList.add(MapUtils.m("levelId", "", "levelName", "全部等级"));
            List<PictureBookNewClazzLevel> pictureBookNewClazzLevels = Arrays.asList(PictureBookNewClazzLevel.L1A, PictureBookNewClazzLevel.L2A, PictureBookNewClazzLevel.L3A);
            pictureBookNewClazzLevels.forEach(level -> clazzLevelMapperList.add(MapUtils.m("levelId", level.name(), "levelName", NewHomeworkUtils.processChinesePictureBookClazzLevel(level.name()))));

            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "pictureBookList", pictureBookMappers,
                    "previewUrl", UrlUtils.buildUrlQuery(TopLevelDomain.getHttpsMainSiteBaseUrl() + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html", MapUtils.m("from", "preview")),
                    "clazzLevelList", clazzLevelMapperList
            );
        }
        return Collections.emptyMap();
    }

    private void processEnglishRecommend(TeacherDetail teacher, String bookId, String unitId, Set<Long> groupIds, HomeworkSourceType homeworkSourceType, String sys, String appVersion,
                                         Map<String, PictureBookSeries> pictureBookSeriesMap, Map<String, PictureBookTopic> pictureBookTopicMap, List<Map<String, Object>> content) {
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
        EmbedBook book = new EmbedBook();
        book.setBookId(bookId);
        book.setUnitId(unitId);

        int startClazzLevel = 1;
        int clazzLevel = 1;
        int termType = 1;
        NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (newBookProfile != null) {
            startClazzLevel = SafeConverter.toInt(newBookProfile.getStartClazzLevel(), 1);
            clazzLevel = SafeConverter.toInt(newBookProfile.getClazzLevel(), 1);
            termType = SafeConverter.toInt(newBookProfile.getTermType(), 1);
        }
        Map<String, PictureBookPackage> recommendPictureBookPackages = null;
        try {
            // 调用大数据接口获取推荐绘本内容
            recommendPictureBookPackages = recommendPictureBookLoaderClient.getRecommendPictureBookLoader()
                    .recommendPictureBooks(unitId, bookId, startClazzLevel, clazzLevel, termType, new ArrayList<>(groupIds));
        } catch (Exception e) {
            logger.error("NewHomeworkLevelReadingsContentLoader call athena error:", e);
        }
        List<Map<String, Object>> recommendContents = new ArrayList<>();
        boolean hasNaturalSpelling = false;
        boolean hasReadingComprehension = false;
        if (MapUtils.isNotEmpty(recommendPictureBookPackages)) {
            PictureBookPackage synchronousProgressPackage = recommendPictureBookPackages.get("SYNCHRONOUS_PROGRESS");
            PictureBookPackage naturalSpellingPackage = recommendPictureBookPackages.get("NATURAL_SPELLING");
            PictureBookPackage readingComprehensionPackage = recommendPictureBookPackages.get("READING_COMPREHENSION");
            PictureBookPackage recommendedReadingPackage = recommendPictureBookPackages.get("RECOMMENDED_READING");
            // 课堂同步拓展
            if (synchronousProgressPackage != null && CollectionUtils.isNotEmpty(synchronousProgressPackage.getPictureBooks())) {
                List<Map<String, Object>> pcMapperList = processPictureBookPackage(synchronousProgressPackage, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, sys, appVersion);
                if (CollectionUtils.isNotEmpty(pcMapperList)) {
                    recommendContents.add(MapUtils.m("module", "syncRecommend", "moduleName", "课堂同步拓展", "description", "与本单元话题匹配的绘本", "pictureBookList", pcMapperList, "showType", "recommend"));
                }
            }
            // 自然拼读
            if (naturalSpellingPackage != null && CollectionUtils.isNotEmpty(naturalSpellingPackage.getPictureBooks())) {
                List<Map<String, Object>> pcMapperList = processPictureBookPackage(naturalSpellingPackage, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, sys, appVersion);
                if (CollectionUtils.isNotEmpty(pcMapperList)) {
                    hasNaturalSpelling = true;
                    recommendContents.add(MapUtils.m("module", "naturalSpelling", "moduleName", "自然拼读", "description", "“见字可读”，低年级学生必备", "pictureBookList", pcMapperList, "showType", "recommend"));
                }
            }
            // 阅读理解
            if (readingComprehensionPackage != null && CollectionUtils.isNotEmpty(readingComprehensionPackage.getPictureBooks())) {
                List<Map<String, Object>> pcMapperList = processPictureBookPackage(readingComprehensionPackage, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, sys, appVersion);
                if (CollectionUtils.isNotEmpty(pcMapperList)) {
                    hasReadingComprehension = true;
                    recommendContents.add(MapUtils.m("module", "readingComprehension", "moduleName", "阅读理解", "description", "提升学生阅读理解能力，高年级学生必备", "pictureBookList", pcMapperList, "showType", "recommend"));
                }
            }
            // 推荐阅读
            if (recommendedReadingPackage != null && CollectionUtils.isNotEmpty(recommendedReadingPackage.getPictureBooks())) {
                List<Map<String, Object>> pcMapperList = processPictureBookPackage(recommendedReadingPackage, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, sys, appVersion);
                if (CollectionUtils.isNotEmpty(pcMapperList)) {
                    recommendContents.add(MapUtils.m("module", "recommendedReading", "moduleName", "推荐阅读", "description", "适合本班孩子兴趣和难度的绘本", "pictureBookList", pcMapperList, "showType", "recommend"));
                }
            }
        }

        // 推荐语，只在App且版本大于等于1.7.5才显示
        if (homeworkSourceType == HomeworkSourceType.App && VersionUtil.compareVersion(appVersion, "1.7.5") >= 0) {
            List<String> recommendDescriptions = new ArrayList<>();
            // 推荐文案规则
            // 根据老师id末位数加上当前周，对4取余
            // 余数为0：展示阅读习惯，阅读习惯的文案根据教材的年级来取
            // 余数为1：展示解码能力或者阅读理解（大数据推出来自然拼读，展示解码能力的文案；推出来阅读理解，展示阅读理解的文案；两个都没有，展示阅读习惯咯）
            // 余数为2：展示文化意识的文案
            // 余数为3：展示语言知识的文案
            int keyId = (int) (teacher.getId() % 10) + Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
            int remainder = keyId % 4;
            String readingHabits = readingHabitsMap.get(clazzLevel);
            switch (remainder) {
                case 0:
                    recommendDescriptions.add(readingHabits);
                    break;
                case 1:
                    if (hasNaturalSpelling) {
                        recommendDescriptions.add("每周给学生推荐至少一本自然拼读绘本吧！自然拼读绘本帮助学生提升解码能力，是开启自主阅读的关键哟。");
                    } else if (hasReadingComprehension) {
                        recommendDescriptions.add("推荐L3及以上绘本配有的“习题”，检测学生对绘本的理解，获得阅读乐趣的同时提升阅读理解能力！");
                    } else {
                        recommendDescriptions.add(readingHabits);
                    }
                    break;
                case 2:
                    recommendDescriptions.add("原汁原味的绘本帮助学生提升文化意识，学生们还不太了解国外风情，可以试试推荐阅读中的书目哦。");
                    break;
                case 3:
                    recommendDescriptions.add("适合学生水平的绘本可以拓展学生词汇量，巩固已经掌握句型，可以试试推荐阅读中为您的班级个性化推荐的书目哦");
                    break;
                default:
                    recommendDescriptions.add(readingHabits);
                    break;
            }
            content.add(MapUtils.m("module", "recommendDescriptions", "descriptions", recommendDescriptions, "title", "阅读建议", "showType", "recommendDescriptions"));
        }

        List<String> pictureBookPlusIds = pictureBookLoaderClient.loadRecommendPictureBookPlusIdsForTopic(unitId);
        // 主题阅读
        List<PictureBookPlus> topicPbList = pictureBookPlusServiceClient.loadByIds(pictureBookPlusIds).values()
                .stream()
                .filter(Objects::nonNull)
                // 过滤未发布的绘本
                .filter(PictureBookPlus::isOnline)
                // 过滤非作业端的绘本
                .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(topicPbList)) {
            List<Map<String, Object>> topicRecommendMapperList = topicPbList
                    .stream()
                    .map(pictureBookPlus -> NewHomeworkContentDecorator.decoratePictureBookPlus(pictureBookPlus, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, sys, appVersion))
                    .collect(Collectors.toList());
            content.add(MapUtils.m("module", "topicRecommend", "moduleName", "节日主题阅读", "description", "与本月节日相关的绘本", "pictureBookList", topicRecommendMapperList, "showType", "recommend"));
        }

        if (CollectionUtils.isNotEmpty(recommendContents)) {
            content.addAll(recommendContents);
        }
    }

    private List<Map<String, Object>> processPictureBookPackage(PictureBookPackage pictureBookPackage, Map<String, PictureBookSeries> pictureBookSeriesMap, Map<String, PictureBookTopic> pictureBookTopicMap,
                                                                EmbedBook book, TeacherAssignmentRecord teacherAssignmentRecord, String sys, String appVersion) {
        if (pictureBookPackage == null || CollectionUtils.isEmpty(pictureBookPackage.getPictureBooks())) {
            return Collections.emptyList();
        }
        Set<String> pbIds = pictureBookPackage.getPictureBooks()
                .stream()
                .map(PictureBookInfo::getPictureBookId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(pbIds);
        List<PictureBookPlus> pictureBookPlusList = MapUtils.resort(pictureBookPlusMap, pbIds).values()
                .stream()
                .filter(Objects::nonNull)
                // 过滤未发布的绘本
                .filter(PictureBookPlus::isOnline)
                // 过滤非作业端的绘本
                .filter(pictureBookPlus -> CollectionUtils.isNotEmpty(pictureBookPlus.getApplyTo()) && pictureBookPlus.getApplyTo().contains(PictureBookApply.HOMEWORK))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(pictureBookPlusList)) {
            return pictureBookPlusList
                    .stream()
                    .map(p -> NewHomeworkContentDecorator.decoratePictureBookPlus(p, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord, sys, appVersion))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> pictureBookList = Collections.emptyList();
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));

        Map<String, Set<PictureBookPracticeType>> pictureBookPracticeTypeMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(contentIdList)) {
            contentIdList.forEach(contentId -> {
                if (StringUtils.isNotBlank(contentId)) {
                    String[] splitContentIds = StringUtils.split(contentId, "|");
                    if (splitContentIds.length == 2) {
                        String pictureBookPlusId = splitContentIds[0];
                        List<String> practiceTypes = StringUtils.toList(splitContentIds[1], String.class);
                        if (CollectionUtils.isNotEmpty(practiceTypes)) {
                            Set<PictureBookPracticeType> practiceTypeSet = practiceTypes.stream()
                                    .map(PictureBookPracticeType::of)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toCollection(LinkedHashSet::new));
                            if (CollectionUtils.isNotEmpty(practiceTypeSet)) {
                                pictureBookPracticeTypeMap.put(pictureBookPlusId, practiceTypeSet);
                            }
                        }
                    }
                }
            });
        }
        if (MapUtils.isNotEmpty(pictureBookPracticeTypeMap)) {
            Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(pictureBookPracticeTypeMap.keySet());
            if (MapUtils.isNotEmpty(pictureBookPlusMap)) {
                TeacherAssignmentRecord teacherAssignmentRecord = StringUtils.isBlank(bookId) ? null :
                        teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
                pictureBookList = pictureBookPlusMap.values().stream()
                        .filter(Objects::nonNull)
                        .map(pictureBookPlus -> {
                            Map<String, Object> pictureBookPlusMapper = NewHomeworkContentDecorator.decoratePictureBookPlus(pictureBookPlus, pictureBookSeriesMap, pictureBookTopicMap, null, teacherAssignmentRecord, "", "");
                            Set<PictureBookPracticeType> practiceTypeSet = pictureBookPracticeTypeMap.get(pictureBookPlus.getId());
                            if (CollectionUtils.isNotEmpty(practiceTypeSet)) {
                                List<Map<String, Object>> practices = practiceTypeSet.stream()
                                        .map(practiceType -> MapUtils.m("type", practiceType.name(), "typeName", practiceType.getTypeName()))
                                        .collect(Collectors.toList());
                                pictureBookPlusMapper.put("practices", practices);
                            }
                            return pictureBookPlusMapper;
                        })
                        .collect(Collectors.toList());
            }
        }
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "pictureBooks", pictureBookList);
    }
}
