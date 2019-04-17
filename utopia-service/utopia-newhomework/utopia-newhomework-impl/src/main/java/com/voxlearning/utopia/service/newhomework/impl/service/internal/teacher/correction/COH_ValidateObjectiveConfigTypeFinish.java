package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.correction;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Named
public class COH_ValidateObjectiveConfigTypeFinish extends SpringContainerSupport implements CorrectHomeworkTask {

    @Inject private NewHomeworkResultServiceImpl newHomeworkResultService;
    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject
    private QuestionLoaderClient questionLoaderClient;

    @Override
    public void execute(CorrectHomeworkContext context) {

        NewHomework newHomework = context.getNewHomework();
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), context.getStudentId(), true);

        // 1.本次作业包含可主观作答的习题，并且没有做批改完成的
        if (Objects.equals(Boolean.TRUE, newHomework.getIncludeSubjective()) && !Objects.equals(Boolean.TRUE, newHomeworkResult.getFinishCorrect())) {
            NewHomeworkPracticeContent content = newHomework.findPracticeContents().getOrDefault(context.getType(), null);
            NewHomeworkResultAnswer answer = newHomeworkResult.getPractices().getOrDefault(context.getType(), null);

            // 2.本次作业形式需要主观作答，并且还没有批改完成的
            if (content != null && answer != null && Objects.equals(Boolean.TRUE, content.getIncludeSubjective()) && !Objects.equals(Boolean.TRUE, answer.isCorrected())) {

                if (context.getType() == ObjectiveConfigType.NEW_READ_RECITE) {
                    long count = newHomeworkResult.getPractices()
                            .get(ObjectiveConfigType.NEW_READ_RECITE)
                            .getAppAnswers()
                            .values()
                            .stream()
                            .filter(o -> Objects.equals(Boolean.FALSE, SafeConverter.toBoolean(o.getReview())))
                            .count();
                    if (count == 0) {
                        boolean b = newHomeworkResultService.finishCorrect(newHomework.toLocation(), context.getStudentId(), context.getType(), true, false);
                        if (!Objects.equals(Boolean.TRUE, b)) {
                            context.setSuccessful(false);
                            logger.warn("Finish Homework ObjectiveConfigType correct fails, homeworkResultId:{}" + newHomeworkResult.getId());
                        } else {
                            context.setPartFinished(true);
                        }
                    }


                } else {
                    // 3.取出本次作业形式中所有需要主观作答的题目，并判断是否已经全部批改完成
                    // <qid, processId>
                    Map<String, String> map = answer.getAnswers();
                    Set<String> qids = map.keySet();

                    // 需要主观作答的题目
                    List<String> questions = questionLoaderClient.loadQuestionsIncludeDisabled(qids)
                            .values().stream()
                            .filter(NewQuestion::isSubjective)
                            .map(NewQuestion::getId)
                            .collect(Collectors.toList());

                    // 所有需要作答题目的明细id
                    List<String> processIds = map.entrySet().stream().filter(entry -> questions.contains(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
                    // 是否全部批改完成
                    List<NewHomeworkProcessResult> processResults = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds)
                            .values().stream()
                            .filter(o -> !Objects.equals(Boolean.TRUE, o.getReview()))
                            .collect(Collectors.toList());

                    if (CollectionUtils.isEmpty(processResults)) {
                        // 更新本次作业形式的批改完成状态
                        boolean b = newHomeworkResultService.finishCorrect(newHomework.toLocation(), context.getStudentId(), context.getType(), true, false);
                        if (!Objects.equals(Boolean.TRUE, b)) {
                            context.setSuccessful(false);
                            logger.warn("Finish Homework ObjectiveConfigType correct fails, homeworkResultId:{}" + newHomeworkResult.getId());
                        } else {
                            context.setPartFinished(true);
                        }
                    }
                }
            } else {
                if (content != null && answer != null && Objects.equals(Boolean.TRUE, content.getIncludeSubjective()) && Objects.equals(Boolean.TRUE, answer.isCorrected())) {
                    context.setPartFinished(true);
                }
            }
        }
    }
}
