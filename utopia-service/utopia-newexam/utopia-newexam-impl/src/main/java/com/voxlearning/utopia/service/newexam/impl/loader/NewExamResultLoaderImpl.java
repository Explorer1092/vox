package com.voxlearning.utopia.service.newexam.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.newexam.api.loader.NewExamResultLoader;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;
import com.voxlearning.utopia.service.question.api.entity.NewExam;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/3/7.
 */
@Named
@Service(interfaceClass = NewExamResultLoader.class)
@ExposeService(interfaceClass = NewExamResultLoader.class)
public class NewExamResultLoaderImpl extends NewExamSpringBean implements NewExamResultLoader {

    @Override
    public NewExamResult loadNewExamResult(String newExamId, Long userId) {
        if (StringUtils.isBlank(newExamId) || Objects.isNull(userId)) {
            return null;
        }

        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return null;
        }

        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), userId.toString());
        return newExamResultDao.load(id.toString());
    }

    @Override
    public Map<Long, NewExamResult> loadNewExamResult(String newExamId, Collection<Long> userIds) {
        if (StringUtils.isBlank(newExamId) || CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return Collections.emptyMap();
        }


        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        Set<String> ids = userIds.stream().map(userId -> new NewExamResult.ID(month, Subject.fromSubjectId(newExam.getSubjectId()),
                newExam.getId(), userId.toString()).toString()).collect(Collectors.toSet());
        return newExamResultDao.loads(ids).values().stream()
                .collect(Collectors.toMap(NewExamResult::getUserId, Function.identity()));
    }

    @Override
    public Map<String, NewExamResult> loadNewExamResults(Collection<String> newExamResultIds) {
        return newExamResultDao.loads(newExamResultIds);
    }
}
