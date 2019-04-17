package com.voxlearning.utopia.service.parent.homework.impl.template.bookList;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserPreferencesLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.parent.homework.util.SubjectUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 同步练习
 * 构建返回值
 */
@Named
@SubType({
        ObjectiveConfigType.EXAM
})
public class ExamResultProcessorSub implements HomeworkProcessor {

    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private HomeworkUserPreferencesLoader homeworkUserPreferencesLoader;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam homeworkParam = hc.getHomeworkParam();
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("subjects", SubjectUtils.BASIC_SUBJECTS.stream().map(s -> MapUtils.m("name", s.name(), "value", s.getValue())).collect(Collectors.toList()));
        resultData.put("levelList", HomeworkUtil.levels.entrySet().stream().map(m -> MapUtils.m("name", m.getKey(), "value", m.getValue())).collect(Collectors.toList()));
        HomeworkUserPreferences userPreferences = homeworkUserPreferencesLoader.loadHomeworkUserPreference(homeworkParam.getStudentId(), homeworkParam.getSubject());
        resultData.put("selectedLevels", ObjectUtils.get(()->userPreferences.getLevels(), Lists.newArrayList("BASE")));
        int clazzLevel = SafeConverter.toInt(hc.getHomeworkParam().getData().get("clazzLevel"));
        // 过滤教材列表
        Collection<NewBookProfile> bookProfile = newContentLoaderClient.loadBooks(hc.getBookIds()).values();
        List<Map<String, Object>> bookList  = bookProfile.stream()
                .filter(newBookProfile -> "ONLINE".equals(newBookProfile.getStatus()) && newBookProfile.getClazzLevel().equals(clazzLevel))
                .map(newBookProfile -> MapUtils.m(
                        "value", newBookProfile.getId(),
                        "label", newBookProfile.getName()))
                .collect(Collectors.toList());
        resultData.put("bookList", bookList);
        hc.setData(resultData);
    }
}
