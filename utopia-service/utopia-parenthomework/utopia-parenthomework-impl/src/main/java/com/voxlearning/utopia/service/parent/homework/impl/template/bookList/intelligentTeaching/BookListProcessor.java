package com.voxlearning.utopia.service.parent.homework.impl.template.bookList.intelligentTeaching;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.HomeworkUserProgressLoaderImpl;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.IntelligentTeachingServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教材列表
 *
 * @author Wenlong Meng
 * @since Feb 20, 2019
 */
@Named("IntelligentTeaching.BookListProcessor")
public class BookListProcessor implements HomeworkProcessor {
    //local variables
    @Inject
    private IntelligentTeachingServiceImpl intelligentTeachingService;
    @Inject
    private HomeworkUserProgressLoaderImpl homeworkUserProgressLoader;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    //Logic

    /**
     * 教材列表
     *
     * @param hc args
     */
    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        Subject subject = Subject.of(param.getSubject());
        // 先从缓存里面取
        List<String> bookIds = intelligentTeachingService.loadBookIds(subject.getId());
        if (CollectionUtils.isEmpty(bookIds)) {
            hc.setMapMessage(MapMessage.errorMessage("没有获取到教材"));
            return;
        }
        // 根据年级过滤教材列表
        int clazzLevel = SafeConverter.toInt(hc.getHomeworkParam().getData().get("clazzLevel"));
        Map<String, NewBookProfile> bookMap = newContentLoaderClient.loadBooks(bookIds);
        List<Map<String, Object>> bookList  = bookMap.values().stream()
                .filter(newBookProfile -> "ONLINE".equals(newBookProfile.getStatus()) && newBookProfile.getClazzLevel().equals(clazzLevel))
                .map(newBookProfile -> MapUtils.m("value", newBookProfile.getId(), "label", newBookProfile.getName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(bookList)) {
            hc.setMapMessage(MapMessage.errorMessage("未获取到教材"));
            return;
        }

        hc.setData(MapUtils.m("bookList", bookList));
    }
}
