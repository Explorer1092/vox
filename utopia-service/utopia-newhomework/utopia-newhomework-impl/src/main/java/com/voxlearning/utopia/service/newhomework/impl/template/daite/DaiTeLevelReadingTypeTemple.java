package com.voxlearning.utopia.service.newhomework.impl.template.daite;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.core.helper.DaiTeLevelReadingsConfig;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;
import com.voxlearning.utopia.service.newhomework.impl.template.DaiTeTypeTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.content.NewHomeworkLevelReadingsContentLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.TeachingObjectiveLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/2/26
 * \* Time: 3:30 PM
 * \* Description: 绘本
 * \
 */
@Named
public class DaiTeLevelReadingTypeTemple implements DaiTeTypeTemplate {

    @Inject
    private TeachingObjectiveLoaderClient teachingObjectiveLoaderClient;

    @Inject
    private NewHomeworkLevelReadingsContentLoader newHomeworkLevelReadingsContentLoader;

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Override
    public DaiTeType getDaiTeType() {
        return DaiTeType.LEVEL_READINGS;
    }

    @Override
    public Object getDaiTeDataByType(NewHomeworkContentLoaderMapper mapper, Map params) {
        TeacherDetail teacherDetail = mapper.getTeacher();
        Subject subject = teacherDetail.getSubject();
        String sectionId = CollectionUtils.isNotEmpty(mapper.getSectionIds()) ? mapper.getSectionIds().get(0) : "";
        if (subject == Subject.ENGLISH) {
            return newHomeworkLevelReadingsContentLoader.loadDaiTeContent(mapper);
        }
        if (subject == Subject.MATH) {
            return null;
        }
        //语文
        Long schoolId = teacherDetail.getTeacherSchoolId();
        List<TeachingObjective> teachingObjectiveList = teachingObjectiveLoaderClient
                .loadLocalTeachingObjectiveByRegionAndUnit(teacherDetail.getRootRegionCode(), teacherDetail.getCityCode(), teacherDetail.getRegionCode(), schoolId, mapper.getUnitId());
        List<String> teachingObjectiveIdList = teachingObjectiveList
                .stream()
                .map(TeachingObjective::getId)
                .collect(Collectors.toList());
        Map<String, List<ObjectiveConfig>> objectiveConfigsMap = teachingObjectiveLoaderClient.loadObjectiveConfigByTeachingObjectiveIds(teachingObjectiveIdList);
        List<ObjectiveConfig> allObjectiveConfig = objectiveConfigsMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        for (ObjectiveConfig objectiveConfig : allObjectiveConfig) {
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfig.getConfigType());
            boolean related = true;
            if (CollectionUtils.isNotEmpty(objectiveConfig.getRelatedCatalogs())) {
                related = false;
                // 没有关联section或者关联的section在已选的里面，则认为关联
                for (EmbedBook book : objectiveConfig.getRelatedCatalogs()) {
                    if (book == null || book.getSectionId() == null || sectionId.equals(book.getSectionId())) {
                        related = true;
                        break;
                    }
                }
            }
            if (related && objectiveConfigType.equals(ObjectiveConfigType.LEVEL_READINGS)) {
                mapper.setObjectiveConfig(objectiveConfig);
                break;
            }
        }

        //来自页面内容的配置数据
        List<DaiTeLevelReadingsConfig> daiTeLevelReadingsConfigList = getDaiTeLevelReadingsConfigList();
        Map<String, DaiTeLevelReadingsConfig> daiTeLevelReadingsConfigMap;
        DaiTeLevelReadingsConfig levelReadingsConfig;
        if (CollectionUtils.isEmpty(daiTeLevelReadingsConfigList) || StringUtils.isEmpty(sectionId)) {
            return newHomeworkLevelReadingsContentLoader.loadDaiTeContent(mapper);
        }
        daiTeLevelReadingsConfigMap = daiTeLevelReadingsConfigList.stream().collect(Collectors.toMap(DaiTeLevelReadingsConfig::getSectionId, Function.identity()));
        if (MapUtils.isEmpty(daiTeLevelReadingsConfigMap) || daiTeLevelReadingsConfigMap.get(sectionId) == null) {
            return newHomeworkLevelReadingsContentLoader.loadDaiTeContent(mapper);
        }
        levelReadingsConfig = daiTeLevelReadingsConfigMap.get(sectionId);
        if (levelReadingsConfig == null) {
            return newHomeworkLevelReadingsContentLoader.loadDaiTeContent(mapper);
        }
        List<DaiTeLevelReadingsConfig.LevelReadingBook> levelReadingBooks = levelReadingsConfig.getLevelReadingBooks();
        if (CollectionUtils.isEmpty(levelReadingBooks)) {
            return newHomeworkLevelReadingsContentLoader.loadDaiTeContent(mapper);
        }
        List<String> pictureBooksIds = levelReadingBooks.stream().filter(Objects::nonNull).map(DaiTeLevelReadingsConfig.LevelReadingBook::getPictureBookId).collect(Collectors.toList());
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        if (objectiveConfig == null) {
            objectiveConfig = new ObjectiveConfig();
        }
        List<Map<String, Object>> contents = objectiveConfig.getContents();
        if (CollectionUtils.isEmpty(contents)) {
            contents = Lists.newArrayList();
            contents.add(MapUtils.m("picture_book_ids", pictureBooksIds));
        } else {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                List<String> pictureBookIdList = (List<String>) configContent.get("picture_book_ids");
                if (CollectionUtils.isEmpty(pictureBookIdList)) {
                    pictureBookIdList = new ArrayList<>();
                }
                pictureBookIdList.addAll(pictureBooksIds);
            }
        }
        objectiveConfig.setContents(contents);
        return newHomeworkLevelReadingsContentLoader.loadDaiTeContent(mapper);
    }

    private List<DaiTeLevelReadingsConfig> getDaiTeLevelReadingsConfigList() {
        //读取页面内容的配置信息
        List<PageBlockContent> teacherTask = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("dai_te_level_readings");
        if (CollectionUtils.isEmpty(teacherTask)) {
            return Collections.emptyList();
        }
        PageBlockContent configPageBlockContent = teacherTask.stream()
                .filter(p -> "level_reading_config".equals(p.getBlockName()))
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
        return JsonUtils.fromJsonToList(configContent, DaiTeLevelReadingsConfig.class);
    }

}
