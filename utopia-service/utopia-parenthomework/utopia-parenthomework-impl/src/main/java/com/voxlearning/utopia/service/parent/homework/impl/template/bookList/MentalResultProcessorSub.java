package com.voxlearning.utopia.service.parent.homework.impl.template.bookList;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@SubType({
        ObjectiveConfigType.MENTAL_ARITHMETIC,
        ObjectiveConfigType.OCR_MENTAL_ARITHMETIC
})
public class MentalResultProcessorSub implements HomeworkProcessor {
    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Override
    public void process(HomeworkContext hc) {
        Map<String, Object> resultData = new HashMap<>();
        int clazzLevel = SafeConverter.toInt(hc.getHomeworkParam().getData().get("clazzLevel"));
        // 过滤教材列表
        Map<String, NewBookProfile> bookMap = newContentLoaderClient.loadBooks(hc.getBookIds());
        List<Map<String, Object>> bookList  = bookMap.values().stream()
                .filter(newBookProfile -> "ONLINE".equals(newBookProfile.getStatus()) && newBookProfile.getClazzLevel().equals(clazzLevel))
                .map(newBookProfile -> MapUtils.m("value", newBookProfile.getId(), "label", newBookProfile.getName()))
                .collect(Collectors.toList());
        resultData.put("bookList", bookList);
        hc.setData(resultData);
    }
}
