package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationClazzLevel;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.athena.RecommendOralCommunicationLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.stone.data.StoneBufferedData;
import com.voxlearning.utopia.service.question.api.entity.stone.data.oralpractice.BaseOralPractice;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class NewHomeworkOralCommunicationContentLoader extends NewHomeworkContentLoaderTemplate {
    @Inject private StoneDataLoaderClient stoneDataLoaderClient;
    @Inject private RecommendOralCommunicationLoaderClient oralCommunicationLoaderClient;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ORAL_COMMUNICATION;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();

        String bookId = mapper.getBookId();
        String unitId = mapper.getUnitId();
        NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        int startGrade = 1;
        int grade = 1;
        if (bookProfile != null) {
            startGrade = SafeConverter.toInt(bookProfile.getStartClazzLevel(), 1);
            grade = SafeConverter.toInt(bookProfile.getClazzLevel(), 1);
        }
        Map<String, List<String>> recommendOralCommunicationIdsMap = Collections.emptyMap();
        try {
            recommendOralCommunicationIdsMap = oralCommunicationLoaderClient.getRecommendOralCommuncationLoader()
                    .recommendOralCommunication(unitId, bookId, startGrade, grade, new ArrayList<>(mapper.getGroupIds()));
        } catch (Exception e) {
            logger.error("NewHomeworkOralCommunicationContentLoader call athena error: ", e);
        }
        Set<String> oralCommunicationIds = recommendOralCommunicationIdsMap.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(oralCommunicationIds);
        stoneBufferedDataList = stoneBufferedDataList.stream()
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
                    return baseOralPractice != null && "online".equals(baseOralPractice.getOlStatus());
                })
                .sorted((s1, s2) -> {
                    OralCommunicationClazzLevel level1 = NewHomeworkContentDecorator.getOralOralCommunicationLevel(s1);
                    OralCommunicationClazzLevel level2 = NewHomeworkContentDecorator.getOralOralCommunicationLevel(s2);
                    return Arrays.asList(OralCommunicationClazzLevel.values()).indexOf(level1)
                            - Arrays.asList(OralCommunicationClazzLevel.values()).indexOf(level2);
                }
        ).collect(Collectors.toList());
        EmbedBook book = new EmbedBook();
        book.setBookId(mapper.getBookId());
        book.setUnitId(mapper.getUnitId());
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(mapper.getTeacher().getSubject(), mapper.getTeacher().getId(), bookId);
        List<Map<String, Object>> recommendContentMapperList = stoneBufferedDataList
                .stream()
                .filter(data -> data != null && (data.getInteractiveVideo() != null || data.getOralPracticeConversion() != null || data.getInteractivePictureBook() != null))
                .map(data -> NewHomeworkContentDecorator.decorateOralCommunicationSummary(data, book, teacherAssignmentRecord))
                .limit(10)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(recommendContentMapperList)) {
            content.add(MapUtils.m("module", "recommend", "moduleName", "同步推荐", "description", "与单元所学句型相匹配", "oralCommunicationList", recommendContentMapperList));
        }

        List<Map<String, Object>> clazzLevelMapperList = new ArrayList<>();
        if (HomeworkSourceType.App == mapper.getHomeworkSourceType()) {
            clazzLevelMapperList.add(MapUtils.m("levelId", "", "levelName", "全部年级"));
        }
        for (OralCommunicationClazzLevel oralCommunicationClazzLevel : OralCommunicationClazzLevel.values()) {
            clazzLevelMapperList.add(MapUtils.m("levelId", oralCommunicationClazzLevel.name(), "levelName", oralCommunicationClazzLevel.getName()));
        }
        List<Map<String, Object>> contentTypeMapperList = new ArrayList<>();
        if (HomeworkSourceType.App == mapper.getHomeworkSourceType()) {
            contentTypeMapperList.add(MapUtils.m("typeId", "", "typeName", "全部类型"));
        }
        for (OralCommunicationContentType oralCommunicationContentType : OralCommunicationContentType.values()) {
            contentTypeMapperList.add(MapUtils.m("typeId", oralCommunicationContentType.name(), "typeName", oralCommunicationContentType.getName()));
        }
        content.add(MapUtils.m("module", "all", "moduleName", "全部内容", "description", "更多口语交际情景包", "clazzLevelList", clazzLevelMapperList, "oralTypeList", contentTypeMapperList));
        return content;
    }

    @Override
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        return super.loadWaterfallContent(mapper);
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(contentIdList);
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
        List<Map<String, Object>> oralCommunicationMapperList = stoneBufferedDataList
                .stream()
                .filter(data -> data != null && (data.getInteractiveVideo() != null || data.getOralPracticeConversion() != null || data.getInteractivePictureBook() != null))
                .map(data -> NewHomeworkContentDecorator.decorateOralCommunicationSummary(data, null, teacherAssignmentRecord))
                .collect(Collectors.toList());
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "oralCommunicationList", oralCommunicationMapperList
        );
    }


}
