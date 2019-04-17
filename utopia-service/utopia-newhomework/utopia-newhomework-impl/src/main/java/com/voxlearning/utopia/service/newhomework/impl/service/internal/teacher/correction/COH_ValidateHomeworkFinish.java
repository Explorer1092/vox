package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.correction;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Named
public class COH_ValidateHomeworkFinish extends SpringContainerSupport implements CorrectHomeworkTask {

    @Inject private NewHomeworkResultServiceImpl newHomeworkResultService;
    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private QuestionLoaderClient questionLoaderClient;

    @Override
    public void execute(CorrectHomeworkContext context) {

        if (context.isPartFinished()) {
            NewHomework newHomework = context.getNewHomework();
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), context.getStudentId(), true);

            if (Objects.equals(Boolean.TRUE, newHomework.getIncludeSubjective()) && !Objects.equals(Boolean.TRUE, newHomeworkResult.getFinishCorrect())) {

                Map<ObjectiveConfigType, NewHomeworkPracticeContent> contents = newHomework.findPracticeContents();
                Map<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();

                // 1.遍历homework的作业形式，过滤出需要批改的作业形式
                List<ObjectiveConfigType> needCorrectedList = new ArrayList<>();
                for (Map.Entry<ObjectiveConfigType, NewHomeworkPracticeContent> entry : contents.entrySet()) {
                    if (entry.getKey().isSubjective()) {
                        needCorrectedList.add(entry.getKey());
                    }

                }

                // 2.判断需要批改的作业形式是否已经批改完成
                long unCorrectTypeNum = 0;
                if (CollectionUtils.isNotEmpty(needCorrectedList)) {
                    unCorrectTypeNum = needCorrectedList.stream()
                            .map(type -> {
                                if (type == ObjectiveConfigType.NEW_READ_RECITE) {
                                    NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(type);
                                    if (newHomeworkResultAnswer.isFinished()) {
                                        long count = newHomeworkResultAnswer.getAppAnswers()
                                                .values()
                                                .stream()
                                                .filter(o -> Objects.equals(Boolean.FALSE, o.getReview()))
                                                .count();
                                        return count == 0;
                                    } else {
                                        return false;
                                    }

                                } else {
                                    return isCorrected(type, practices);
                                }
                            })
                            .filter(o -> !o)
                            .count();
                }
                // 3.作业全部批改完成
                if (unCorrectTypeNum == 0) {
                    boolean b = newHomeworkResultService.finishCorrect(newHomework.toLocation(), context.getStudentId(), context.getType(), false, true);
                    if (!Objects.equals(Boolean.TRUE, b)) {
                        context.setSuccessful(false);
                        logger.warn("Finish Homework correct fails, homeworkResultId:{}" + newHomeworkResult.getId());
                    }
                }
            }
        }
    }

    private boolean isCorrected(ObjectiveConfigType type, Map<ObjectiveConfigType, NewHomeworkResultAnswer> practices) {
        return !(type == null || MapUtils.isEmpty(practices)) && practices.get(type).isCorrected();
    }

}
