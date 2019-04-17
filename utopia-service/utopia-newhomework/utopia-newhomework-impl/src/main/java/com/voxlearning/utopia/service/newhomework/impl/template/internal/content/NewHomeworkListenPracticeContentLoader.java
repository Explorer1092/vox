package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;

/**
 * @author guoqiang.li
 * @since 2016/8/22
 */
@Named
public class NewHomeworkListenPracticeContentLoader extends NewHomeworkContentLoaderTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.LISTEN_PRACTICE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(getPackageContent(mapper.getTeacher(), mapper.getObjectiveConfig(), mapper.getBookId(), mapper.getUnitId()));
        return content;
    }
}
