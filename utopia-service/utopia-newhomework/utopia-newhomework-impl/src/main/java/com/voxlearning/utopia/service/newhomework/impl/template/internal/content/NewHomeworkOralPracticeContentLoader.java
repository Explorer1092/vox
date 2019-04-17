package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/10/11
 */
@Named
public class NewHomeworkOralPracticeContentLoader extends NewHomeworkIntelligenceExamContentLoader {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ORAL_PRACTICE;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        return super.loadContent(mapper);
    }
}
