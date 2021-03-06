package com.voxlearning.utopia.service.newhomework.impl.template.daite;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;
import com.voxlearning.utopia.service.newhomework.impl.template.DaiTeTypeTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.content.NewHomeworkWordTeachAndPracticeContentLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.question.api.entity.TeachingObjective;
import com.voxlearning.utopia.service.question.consumer.TeachingObjectiveLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/3/5
 * \* Time: 5:45 PM
 * \* Description: 字词讲练
 * \
 */
@Named
public class DaiTeWordTeachAndPracticeTypeTemple implements DaiTeTypeTemplate {

    @Inject
    private NewHomeworkWordTeachAndPracticeContentLoader newHomeworkWordTeachAndPracticeContentLoader;

    @Inject
    private TeachingObjectiveLoaderClient teachingObjectiveLoaderClient;

    @Override
    public DaiTeType getDaiTeType() {
        return DaiTeType.WORD_TEACH_AND_PRACTICE;
    }

    @Override
    public Object getDaiTeDataByType(NewHomeworkContentLoaderMapper mapper, Map params) {
        TeacherDetail teacherDetail = mapper.getTeacher();
        String sectionId = CollectionUtils.isNotEmpty(mapper.getSectionIds()) ? mapper.getSectionIds().get(0) : "";
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
            if (related && objectiveConfigType.equals(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE)) {
                mapper.setObjectiveConfig(objectiveConfig);
                break;
            }
        }
        return newHomeworkWordTeachAndPracticeContentLoader.loadContent(mapper);
    }
}
