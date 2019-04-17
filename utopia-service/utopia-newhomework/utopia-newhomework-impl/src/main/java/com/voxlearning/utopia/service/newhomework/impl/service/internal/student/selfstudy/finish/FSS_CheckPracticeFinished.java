package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.PropertiesUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.FinishSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.CalculateResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@Named
public class FSS_CheckPracticeFinished extends SpringContainerSupport implements FinishSelfStudyHomeworkTask {

    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private CalculateScoreAndDuration calculateScoreAndDuration;
    @Inject private SelfStudyHomeworkResultDao selfStudyHomeworkResultDao;

    @Override
    public void execute(FinishSelfStudyHomeworkContext context) {
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();
        SelfStudyHomework selfStudyHomework = context.getSelfStudyHomework();
        SelfStudyHomeworkResult homeworkResult = context.getSelfStudyHomeworkResult();
        NewHomework newHomework = new NewHomework();
        PropertiesUtils.copyProperties(newHomework, context.getSelfStudyHomework());
        Map<ObjectiveConfigType, List<String>> answerMap = initProcessMap(context, newHomework);

        //没有后测题的作业形式特殊处理
        if (ObjectiveConfigType.ORAL_INTERVENTIONS.equals(objectiveConfigType)) {
            homeworkResult = selfStudyHomeworkResultDao.finishAppPractice(selfStudyHomework.toLocation(), objectiveConfigType, context.getAppChameleonId(), context.getPracticeScore(), context.getPracticeDuration());
            //判断该作业形式其他课程是否完成, 都完成则标记这个作业形式完成.
            LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswers = homeworkResult.getPractices().get(objectiveConfigType).getAppAnswers();
            List<NewHomeworkApp> newHomeworkApps = newHomework.findNewHomeworkApps(objectiveConfigType);
            if (appAnswers.size() != newHomeworkApps.size()) {
                context.terminateTask();
                return;
            }
            context.setPracticeFinished(true);
            return;
        }

        List<String> answerIds = answerMap.get(objectiveConfigType);
        Map<String, SubHomeworkResultAnswer> resultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(answerIds);
        if (ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.equals(objectiveConfigType)) {
            Map<String, List<NewHomeworkQuestion>> courseAppQuestionsMap = selfStudyHomework.findCourseAppQuestionsMap(objectiveConfigType);
            if (MapUtils.isEmpty(courseAppQuestionsMap)) {
                logger.error("SelfStudyHomework {} does not contain practice {}", context.getHomeworkId(), objectiveConfigType);
                context.errorResponse();
                return;
            }

            Map<String, List<SubHomeworkResultAnswer>> courseResultAnswerMap = resultAnswerMap.values().stream().collect(Collectors.groupingBy(o -> o.parseID().getJoinKeys().stream().findFirst().orElse(null)));
            //巩固学习下当前课程是否完成
            String courseId = context.getAppChameleonId();
            List<NewHomeworkQuestion> courseQuestionList = courseAppQuestionsMap.get(courseId);//当前课程对应后测题
            List<SubHomeworkResultAnswer> courseResultAnswers = courseResultAnswerMap == null ? Collections.emptyList() : courseResultAnswerMap.get(courseId);//当前课程对应已完成后测题
            if (CollectionUtils.isEmpty(courseResultAnswers) || courseQuestionList.size() != courseResultAnswers.size()) {
                context.terminateTask();
                return;
            }
            Set<String> processIds = courseResultAnswers.stream().map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toSet());
            CalculateResult calculate = calculateScoreAndDuration.calculate(processIds);
            if (calculate == null) {
                context.terminateTask();
                return;
            }
            homeworkResult = selfStudyHomeworkResultDao.finishAppPractice(selfStudyHomework.toLocation(), objectiveConfigType, courseId, calculate.getScore(), calculate.getDuration());
            //判断该作业形式其他课程是否完成, 都完成则标记这个作业形式完成.
            List<NewHomeworkQuestion> appQuestions = Lists.newLinkedList();
            courseAppQuestionsMap.values().forEach(appQuestions::addAll);
            LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswers = homeworkResult.getPractices().get(objectiveConfigType).getAppAnswers();
            if (courseAppQuestionsMap.size() != appAnswers.size() || resultAnswerMap.size() != appQuestions.size()) {
                context.terminateTask();
                return;
            }
        } else {
            // 作业形式的全部题量
            List<String> questionList = selfStudyHomework.findQuestionIds(objectiveConfigType, false);
            if (CollectionUtils.isEmpty(questionList)) {
                logger.error("SelfStudyHomework {} does not contain practice {}", context.getHomeworkId(), objectiveConfigType);
                context.errorResponse();
                return;
            }
            if (MapUtils.isEmpty(answerMap) || MapUtils.isEmpty(resultAnswerMap) || questionList.size() > resultAnswerMap.size()) { // 某个作业类型都没有完成
                context.terminateTask();
                return;
            }
        }
        context.setSelfStudyHomeworkResult(homeworkResult);
        context.setPracticeFinished(true);
        Set<String> objProcessIds = resultAnswerMap.values().stream().map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toSet());
        context.setProcessIds(objProcessIds);//当前作业形式processId
    }


    private Map<ObjectiveConfigType, List<String>> initProcessMap(FinishSelfStudyHomeworkContext context, NewHomework newHomework) {
        Map<ObjectiveConfigType, List<String>> answerIdsMap = newHomeworkResultLoader.initSubHomeworkResultAnswerIdsMap(newHomework, context.getSelfStudyHomework().getStudentId());
        context.setAnswerIdMap(answerIdsMap);
        return answerIdsMap;
    }
}
