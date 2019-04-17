package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.outside.OutsideReadingProcessResultDao;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;


/**
 * @author majianxin
 */
@Named
public class OS_CreateOutsideReadingProcessResult extends SpringContainerSupport implements OutsideReadingResultTask {

    @Inject private OutsideReadingProcessResultDao outsideReadingProcessResultDao;
    @Inject private QuestionLoaderClient questionLoaderClient;

    @Override
    public void execute(OutsideReadingContext context) {

        // 用于获取试题的版本和docId，题目中的version和_id中横线后面的那个版本号可能不一致
        StudentHomeworkAnswer answer = context.getStudentHomeworkAnswer();

        NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(answer.getQuestionId());

        OutsideReadingProcessResult result = buildProcessResult(context, answer, question);
        outsideReadingProcessResultDao.insert(result);
        context.setProcessResult(result);
    }

    private OutsideReadingProcessResult buildProcessResult(OutsideReadingContext context, StudentHomeworkAnswer sha, NewQuestion question) {
        String questionId = sha.getQuestionId();
        OutsideReadingProcessResult result = new OutsideReadingProcessResult();
        OutsideReadingProcessResult.ID id = new OutsideReadingProcessResult.ID(context.getOutsideReading().getCreateAt());
        result.setId(id.toString());
        result.setClazzGroupId(context.getClazzGroupId());
        result.setUserId(context.getUserId());
        result.setReadingId(context.getReadingId());
        result.setBookId(context.getBookId());
        result.setMissionId(context.getMissionId());
        result.setQuestionId(questionId);
        Date currentTime = new Date();
        result.setCreateAt(currentTime);
        result.setUpdateAt(currentTime);
        if (question != null) {
            result.setQuestionDocId(question.getDocId());
            result.setQuestionVersion(question.getOlUpdatedAt() != null ? question.getOlUpdatedAt().getTime() : SafeConverter.toLong(question.getVersion()));
        }
        result.setType(NewHomeworkType.OutsideReading);
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
        result.setClientType(context.getClientType());
        result.setClientName(context.getClientName());
        if (CollectionUtils.isNotEmpty(context.getFiles().get(questionId))) {
            long c = context.getFiles().get(questionId).stream().mapToLong(Collection::size).sum();
            if (c > 0) result.setFiles(context.getFiles().get(questionId));
        }
        result.setAdditions(context.getAdditions());
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
