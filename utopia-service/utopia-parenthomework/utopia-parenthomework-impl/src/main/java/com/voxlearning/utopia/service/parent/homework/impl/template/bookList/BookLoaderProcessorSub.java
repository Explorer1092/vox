package com.voxlearning.utopia.service.parent.homework.impl.template.bookList;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.FamousSchoolSynHomeworkService;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserPreferencesLoader;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.util.SubjectUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * 从大数据获取城市的教材列表
 *
 */
@Named
@SubType({
        ObjectiveConfigType.EXAM,
        ObjectiveConfigType.MENTAL_ARITHMETIC,
        ObjectiveConfigType.OCR_MENTAL_ARITHMETIC
})
public class BookLoaderProcessorSub implements HomeworkProcessor {

    @ImportService(interfaceClass = FamousSchoolSynHomeworkService.class)
    private FamousSchoolSynHomeworkService famousSchoolSynHomeworkService;
    @Inject private HomeworkUserPreferencesLoader homeworkUserPreferencesLoader;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam homeworkParam = hc.getHomeworkParam();
        Map<String, Object> data = homeworkParam.getData();
        int clazzLevel = SafeConverter.toInt(data.get("clazzLevel"));
        if (ObjectUtils.anyBlank(data, clazzLevel)) {
            hc.setMapMessage(MapMessage.errorMessage("参数校验失败"));
            return;
        }
        String subject = homeworkParam.getSubject();
        if (StringUtils.isBlank(subject)) {
            subject = SubjectUtils.BASIC_SUBJECTS.get(0).name();
        }
        if (!SubjectUtils.isValid(subject)) {
            hc.setMapMessage(MapMessage.errorMessage("请选择正确的学科"));
            return;
        }

        // 缓存book 以城市id 科目 缓存
        Long studentId = homeworkParam.getStudentId();
        List<String> bookIds = HomeWorkCache.load(CacheKey.BOOK, subject);
        if (CollectionUtils.isEmpty(bookIds)) {
            bookIds = famousSchoolSynHomeworkService.querySynHomeworkBookList(subject);
            LoggerUtils.info("queryFamousSchoolSynHomeworkBookList", subject, bookIds != null ? bookIds.size():0);
        }
        // 获取偏好设置
        String finalSubject = subject;
        hc.setBookId(ObjectUtils.get(() -> homeworkUserPreferencesLoader.loadHomeworkUserPreference(studentId, finalSubject).getBookId()));
        // 获取到教材，放入缓存
        if (CollectionUtils.isNotEmpty(bookIds)) {
            HomeWorkCache.set(DateUtils.getCurrentToDayEndSecond(), bookIds, CacheKey.BOOK, subject);
        }
        // 上下文
        hc.setBookIds(bookIds);
        homeworkParam.setSubject(subject);
    }
}
