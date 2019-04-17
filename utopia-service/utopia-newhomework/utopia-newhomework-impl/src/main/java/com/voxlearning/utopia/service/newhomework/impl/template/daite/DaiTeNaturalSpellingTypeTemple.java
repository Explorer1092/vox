package com.voxlearning.utopia.service.newhomework.impl.template.daite;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;
import com.voxlearning.utopia.service.newhomework.impl.template.DaiTeTypeTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.content.NewHomeworkNaturalSpellingContentLoader;
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
 * \* Date: 2019/3/12
 * \* Time: 5:26 PM
 * \* Description: 自然拼读
 * \
 */
@Named
public class DaiTeNaturalSpellingTypeTemple implements DaiTeTypeTemplate {

    @Inject
    private TeachingObjectiveLoaderClient teachingObjectiveLoaderClient;
    @Inject
    private NewHomeworkNaturalSpellingContentLoader newHomeworkNaturalSpellingContentLoader;

    @Override
    public DaiTeType getDaiTeType() {
        return DaiTeType.NATURAL_SPELLING;
    }

    @Override
    public Object getDaiTeDataByType(NewHomeworkContentLoaderMapper mapper, Map params) {
        TeacherDetail teacherDetail = mapper.getTeacher();
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
                    if (book == null || book.getSectionId() == null) {
                        related = true;
                        break;
                    }
                }
            }
            if (related && objectiveConfigType.equals(ObjectiveConfigType.NATURAL_SPELLING)) {
                boolean nonUniversal = MapUtils.isNotEmpty(objectiveConfig.getExtras()) && StringUtils.equals("nonUniversal", SafeConverter.toString(objectiveConfig.getExtras().get("module")));
                if (nonUniversal) {
                    mapper.setObjectiveConfig(objectiveConfig);
                    break;
                }
            }
        }
        List<Map<String, Object>> content = newHomeworkNaturalSpellingContentLoader.loadContent(mapper);
        if (CollectionUtils.isEmpty(content) || content.get(0) == null) {
            return null;
        }
        Map<String, Object> dataContent = content.get(0);
        if (MapUtils.isEmpty(dataContent) || dataContent.get("nonUniversalContents") == null) {
            return null;
        }
        List<Map<String, Object>> nonUniversalContents = (List<Map<String, Object>>) dataContent.get("nonUniversalContents");
        return nonUniversalContents.stream().filter(c -> SafeConverter.toString(c.get("unitId"), "").equals(mapper.getUnitId())).findFirst().orElse(null);
    }
}
