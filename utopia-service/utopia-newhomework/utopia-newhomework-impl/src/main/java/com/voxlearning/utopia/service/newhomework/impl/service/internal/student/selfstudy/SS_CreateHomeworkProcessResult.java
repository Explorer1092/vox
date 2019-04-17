package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkProcessResultServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;

/**
 * @author xuesong.zhang
 * @since 2017/2/3
 */
@Named
public class SS_CreateHomeworkProcessResult extends SpringContainerSupport implements SelfStudyHomeworkResultTask {

    @Inject private NewHomeworkProcessResultServiceImpl newHomeworkProcessResultService;
    @Inject private QuestionLoaderClient questionLoaderClient;

    @Override
    public void execute(SelfStudyHomeworkContext context) {
        if (ObjectiveConfigType.ORAL_INTERVENTIONS.equals(context.getObjectiveConfigType())) {
            return;
        }
        // 用于获取试题的版本和docId，题目中的version和_id中横线后面的那个版本号可能不一致
        StudentHomeworkAnswer studentHomeworkAnswer = context.getStudentHomeworkAnswer();

        NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(studentHomeworkAnswer.getQuestionId());

        SubHomeworkProcessResult result = buildProcessResult(context, studentHomeworkAnswer, question);
        newHomeworkProcessResultService.insertSubHomeworkProcessResults(Collections.singleton(result));
        context.setProcessResult(result);
    }

    private SubHomeworkProcessResult buildProcessResult(SelfStudyHomeworkContext context, StudentHomeworkAnswer sha, NewQuestion question) {
        String questionId = sha.getQuestionId();
        SubHomeworkProcessResult result = new SubHomeworkProcessResult();
        SubHomeworkProcessResult.ID id = new SubHomeworkProcessResult.ID(context.getSelfStudyHomework().getCreateAt());
        result.setId(id.toString());
        result.setType(context.getSelfStudyHomework().getType());
        result.setHomeworkTag(context.getSelfStudyHomework().getHomeworkTag());
        result.setClazzGroupId(context.getClazzGroupId());
        result.setUserId(context.getUserId());
        result.setHomeworkId(context.getHomeworkId());
        result.setBookId(context.getBookId());
        result.setUnitGroupId(context.getUnitGroupId());
        result.setUnitId(context.getUnitId());
        result.setLessonId(context.getLessonId());
        result.setSectionId(context.getSectionId());
        result.setQuestionId(questionId);

        if (question != null) {
            result.setQuestionDocId(question.getDocId());
            result.setQuestionVersion(question.getOlUpdatedAt() != null ? question.getOlUpdatedAt().getTime() : SafeConverter.toLong(question.getVersion()));
        }

        result.setStandardScore(context.getStandardScore().get(questionId));
        result.setScore(context.getScoreResult().get(questionId).getTotalScore());
        result.setActualScore(context.getScoreResult().get(questionId).getActualScore());
        result.setAppOralScoreLevel(context.getScoreResult().get(questionId).getAppOralScoreLevel());
        result.setGrasp(context.getScoreResult().get(questionId).getIsRight());
        result.setSubGrasp(context.getSubGrasp().get(questionId));
        result.setSubScore(context.getSubScore().get(questionId));
        result.setUserAnswers(sha.getAnswer());
        result.setDuration(NewHomeworkUtils.processDuration(sha.getDurationMilliseconds()));
        result.setSubject(context.getSubject());
        result.setObjectiveConfigType(context.getObjectiveConfigType());
        result.setClientType(context.getClientType());
        result.setClientName(context.getClientName());
        if (CollectionUtils.isNotEmpty(context.getFiles().get(questionId))) {
            long c = context.getFiles().get(questionId).stream().mapToLong(Collection::size).sum();
            if (c > 0) result.setFiles(context.getFiles().get(questionId));
        }
        result.setAdditions(context.getAdditions());
        result.setCourseId(sha.getCourseId());
        context.getResult().put(questionId,
                MapUtils.m(
                        "fullScore", context.getStandardScore().get(questionId),
                        "score", context.getScoreResult().get(questionId).getTotalScore(),
                        "answers", context.getStandardAnswer().get(questionId),
                        "userAnswers", sha.getAnswer(),
                        "subMaster", context.getSubGrasp().get(questionId),
                        "subScore", context.getSubScore().get(questionId),
                        "master", context.getScoreResult().get(questionId).getIsRight()));

        return result;
    }
}
