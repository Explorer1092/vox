package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Named
public class NewHomeworkOcrMentalArithmeticContentLoader extends NewHomeworkContentLoaderTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.OCR_MENTAL_ARITHMETIC;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        if (CollectionUtils.isEmpty(contentIdList) || contentIdList.size() < 2) {
            return Collections.emptyMap();
        }
        String workBookName = contentIdList.get(0);
        String homeworkDetail = contentIdList.get(1);
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "workBookName", workBookName,
                "homeworkDetail", homeworkDetail
        );
    }
}
