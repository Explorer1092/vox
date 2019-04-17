package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/2/28
 */
@Named
public class NewHomeworkInterestingPictureContentLoader extends NewHomeworkContentLoaderTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.INTERESTING_PICTURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> packageContent = getPackageContent(mapper.getTeacher(), mapper.getObjectiveConfig(), mapper.getBookId(), mapper.getUnitId());
        String sectionId = CollectionUtils.isEmpty(mapper.getSectionIds()) ? null : mapper.getSectionIds().get(0);
        if (MapUtils.isNotEmpty(packageContent)) {
            List<Map<String, Object>> packages = (List<Map<String, Object>>) packageContent.get("packages");
            if (CollectionUtils.isNotEmpty(packages)) {
                packages.forEach(p -> {
                    List<Map<String, Object>> questions = (List<Map<String, Object>>) p.get("questions");
                    if (CollectionUtils.isNotEmpty(questions)) {
                        questions.forEach(q -> {
                            Map<String, Object> book = (Map<String, Object>) q.get("book");
                            book.put("sectionId", sectionId);
                        });
                    }
                });
                content.add(packageContent);
            }
        }
        return content;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        List<Map<String, Object>> content = loadContent(mapper);
        if (CollectionUtils.isNotEmpty(content)) {
            List<Map<String, Object>> packageList = Collections.emptyList();
            for (Map<String, Object> contentMapper : content) {
                String type = SafeConverter.toString(contentMapper.get("type"));
                if ("package".equals(type)) {
                    List<Map<String, Object>> packages = (List<Map<String, Object>>) contentMapper.get("packages");
                    if (CollectionUtils.isNotEmpty(packages)) {
                        packageList = packages;
                        break;
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(packageList)) {
                return MapUtils.m(
                        "objectiveConfigId", objectiveConfig.getId(),
                        "type", getObjectiveConfigType().name(),
                        "typeName", getObjectiveConfigType().getValue(),
                        "name", objectiveConfig.getName(),
                        "packages", packageList
                );
            }
        }
        return Collections.emptyMap();
    }
}
