package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @author guoqiang.li
 * @since 2017/10/26
 */
@Named
public class NewHomeworkDubbingContentLoader extends NewHomeworkContentLoaderTemplate {
    @Inject private DubbingLoaderClient dubbingLoaderClient;

    private static final Map<Integer, String> CLAZZ_LEVEL_DESCRIPTION_MAP = new HashMap<>();
    private static final Map<Integer, Integer> CLAZZ_LEVEL_DEFAULT_CLAZZ_LEVEL_MAP = new HashMap<>();

    static {
        CLAZZ_LEVEL_DESCRIPTION_MAP.put(3, "三起教材建议选择一,二年级配音，内容更新ing~");
        CLAZZ_LEVEL_DESCRIPTION_MAP.put(4, "三起教材建议选择二,三年级配音，内容更新ing~");
        CLAZZ_LEVEL_DESCRIPTION_MAP.put(5, "三起教材建议选择四,五年级配音，内容更新ing~");
        CLAZZ_LEVEL_DESCRIPTION_MAP.put(6, "三起教材建议选择五,六年级配音，内容更新ing~");
        CLAZZ_LEVEL_DEFAULT_CLAZZ_LEVEL_MAP.put(3, 1);
        CLAZZ_LEVEL_DEFAULT_CLAZZ_LEVEL_MAP.put(4, 2);
        CLAZZ_LEVEL_DEFAULT_CLAZZ_LEVEL_MAP.put(5, 4);
        CLAZZ_LEVEL_DEFAULT_CLAZZ_LEVEL_MAP.put(6, 5);
    }

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.DUBBING;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();

        TeacherDetail teacher = mapper.getTeacher();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();

        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
        // 课堂同步拓展模块
        processClassroomTopicsModule(mapper, content, unitId, bookId, teacherAssignmentRecord);
        // 全部配音模块
        allDubbingModule(bookId, mapper, content);
        return content;
    }

    /**
     * 拼装课堂话题拓展模块数据
     */
    @SuppressWarnings("unchecked")
    private void processClassroomTopicsModule(NewHomeworkContentLoaderMapper mapper, List<Map<String, Object>> content, String unitId, String bookId, TeacherAssignmentRecord teacherAssignmentRecord) {
        Set<String> configDubbingIds = new LinkedHashSet<>();
        List<Dubbing> allRecommendDubbingList = new ArrayList<>();
        // 获取内容配置的配音ids
        if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                if (type == ObjectiveConfig.DUBBING) {
                    List<String> dubbingIds = (List<String>) configContent.get("dubbing_ids");
                    if (CollectionUtils.isNotEmpty(dubbingIds)) {
                        configDubbingIds.addAll(dubbingIds);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(configDubbingIds)) {
            Map<String, Dubbing> configDubbingMap = dubbingLoaderClient.loadDubbingByDocIds(configDubbingIds);
            if (MapUtils.isNotEmpty(configDubbingMap)) {
                allRecommendDubbingList.addAll(configDubbingMap.values());
            }
        }
        List<Dubbing> recommendDubbingList = dubbingLoaderClient.loadRecommendDubbingByBookCatalog(bookId, unitId);
        if (CollectionUtils.isNotEmpty(recommendDubbingList)) {
            for (Dubbing dubbing : recommendDubbingList) {
                if (!configDubbingIds.contains(dubbing.getDocId())) {
                    allRecommendDubbingList.add(dubbing);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(allRecommendDubbingList)) {
            Set<String> albumIds = allRecommendDubbingList.stream().map(Dubbing::getCategoryId).collect(Collectors.toSet());
            Map<String, DubbingCategory> albums = dubbingLoaderClient.loadDubbingCategoriesByIds(albumIds);
            Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                    .stream()
                    .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            List<Map<String, Object>> dubbingMapperList = allRecommendDubbingList
                    .stream()
                    .map(dubbing -> NewHomeworkContentDecorator.decorateDubbing(dubbing, albums.get(dubbing.getCategoryId()), teacherAssignmentRecord, book, null, getObjectiveConfigType(), dubbingThemeMap))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(dubbingMapperList)) {
                content.add(MapUtils.m("module", "recommend", "moduleName", "话题拓展", "description", "基于单元话题推荐的配音", "dubbingList", dubbingMapperList));
            }
        }
    }

    /**
     * 全部配音模块
     */
    private void allDubbingModule(String bookId, NewHomeworkContentLoaderMapper mapper, List<Map<String, Object>> content) {
        // 搜索模块
        String description = "配音内容不断更新中";
        int defaultClazzLevel = 1;
        NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (newBookProfile != null) {
            int startClazzLevel = SafeConverter.toInt(newBookProfile.getStartClazzLevel());
            defaultClazzLevel = SafeConverter.toInt(newBookProfile.getClazzLevel(), 1);
            if (startClazzLevel == 3) {
                description = CLAZZ_LEVEL_DESCRIPTION_MAP.getOrDefault(defaultClazzLevel, "");
                defaultClazzLevel = CLAZZ_LEVEL_DEFAULT_CLAZZ_LEVEL_MAP.getOrDefault(defaultClazzLevel, defaultClazzLevel);
            }
        }

        // 获取所有的频道
        List<DubbingCategory> channelList = dubbingLoaderClient.loadAllChannels();
        List<Map<String, Object>> channelMapperList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(channelList)) {
            channelMapperList = channelList.stream()
                    .map(channel -> MapUtils.m("channelId", channel.getId(), "channelName", channel.getName()))
                    .collect(Collectors.toList());
        }
        // 获取所有的专辑
        List<Map<String, Object>> albumMapperList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(channelList)) {
            Map<String, DubbingCategory> albumMap = new LinkedHashMap<>();
            Set<String> channelIds = channelList.stream().map(DubbingCategory::getId).collect(Collectors.toCollection(LinkedHashSet::new));
            Map<String, List<DubbingCategory>> channelAlbumsMap = dubbingLoaderClient.loadDubbingCategoriesByParentIds(channelIds);
            for (DubbingCategory channel : channelList) {
                List<DubbingCategory> albumList = channelAlbumsMap.get(channel.getId());
                if (CollectionUtils.isNotEmpty(albumList)) {
                    albumList.forEach(album -> albumMap.put(album.getId(), album));
                }
            }
            if (MapUtils.isNotEmpty(albumMap)) {
                albumMap.forEach((k, v) -> albumMapperList.add(MapUtils.m("albumId", k, "albumName", v.getName())));
            }
        }
        // 获取所有的主题
        List<DubbingTheme> dubbingThemeList = dubbingLoaderClient.loadAllDubbingThemes();
        List<Map<String, Object>> dubbingThemeMapperList = new ArrayList<>();
        if (HomeworkSourceType.App == mapper.getHomeworkSourceType()) {
            dubbingThemeMapperList.add(MapUtils.m("themeId", "", "themeName", "全部主题"));
        }
        dubbingThemeList.forEach(dubbingTheme -> dubbingThemeMapperList.add(MapUtils.m("themeId", dubbingTheme.getId(), "themeName", dubbingTheme.getName())));
        content.add(MapUtils.m(
                "module", "all",
                "moduleName", "全部配音",
                "description", description,
                "defaultClazzLevel", defaultClazzLevel,
                "channelList", channelMapperList,
                "albumList", albumMapperList,
                "themeList", dubbingThemeMapperList));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        TeacherDetail teacher = mapper.getTeacher();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();

        Set<String> configDubbingIds = new LinkedHashSet<>();
        List<Dubbing> allRecommendDubbingList = new ArrayList<>();
        boolean needRecommend = false;
        // 获取内容配置的配音ids
        if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                List<String> module = (List<String>) configContent.get("module");
                boolean containsRecommend = CollectionUtils.isNotEmpty(module) && module.contains("recommend");
                if (containsRecommend) {
                    needRecommend = true;
                }
                if (type == ObjectiveConfig.DUBBING && containsRecommend) {
                    List<String> dubbingIds = (List<String>) configContent.get("dubbing_ids");
                    if (CollectionUtils.isNotEmpty(dubbingIds)) {
                        configDubbingIds.addAll(dubbingIds);
                    }
                }
            }
        }
        if (needRecommend) {
            if (CollectionUtils.isNotEmpty(configDubbingIds)) {
                Map<String, Dubbing> configDubbingMap = dubbingLoaderClient.loadDubbingByDocIds(configDubbingIds);
                if (MapUtils.isNotEmpty(configDubbingMap)) {
                    allRecommendDubbingList.addAll(configDubbingMap.values());
                }
            }
            List<Dubbing> recommendDubbingList = dubbingLoaderClient.loadRecommendDubbingByBookCatalog(bookId, unitId);
            if (CollectionUtils.isNotEmpty(recommendDubbingList)) {
                for (Dubbing dubbing : recommendDubbingList) {
                    if (!configDubbingIds.contains(dubbing.getDocId())) {
                        allRecommendDubbingList.add(dubbing);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(allRecommendDubbingList)) {
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
            Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                    .stream()
                    .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
            Set<String> albumIds = allRecommendDubbingList.stream().map(Dubbing::getCategoryId).collect(Collectors.toSet());
            Map<String, DubbingCategory> albums = dubbingLoaderClient.loadDubbingCategoriesByIds(albumIds);
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            Date newDate = DateUtils.addDays(new Date(), -7);

            List<Map<String, Object>> dubbingMapperList = allRecommendDubbingList
                    .stream()
                    .map(dubbing -> NewHomeworkContentDecorator.decorateDubbing(dubbing, albums.get(dubbing.getCategoryId()), teacherAssignmentRecord, book, newDate, getObjectiveConfigType(), dubbingThemeMap))
                    .collect(Collectors.toList());

            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "dubbingList", dubbingMapperList
            );
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
        Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                .stream()
                .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(contentIdList);
        Set<String> albumIds = dubbingMap.values().stream().map(Dubbing::getCategoryId).collect(Collectors.toSet());
        Map<String, DubbingCategory> albums = dubbingLoaderClient.loadDubbingCategoriesByIds(albumIds);
        Date newDate = DateUtils.addDays(new Date(), -7);
        List<Map<String, Object>> dubbingMapperList = dubbingMap.values()
                .stream()
                .map(dubbing -> NewHomeworkContentDecorator.decorateDubbing(dubbing, albums.get(dubbing.getCategoryId()), teacherAssignmentRecord, null, newDate, getObjectiveConfigType(), dubbingThemeMap))
                .collect(Collectors.toList());
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "dubbingList", dubbingMapperList
        );
    }
}
