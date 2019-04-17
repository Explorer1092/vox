package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;

/**
 * Created by tanguohong on 2016/4/6.
 */
@Named
public class ER_InitNewExamProcessResult extends SpringContainerSupport implements NewExamResultTask {
    @Override
    public void execute(NewExamResultContext context) {
        NewExamProcessResult result = new NewExamProcessResult();
        NewExamProcessResult.ID id = new NewExamProcessResult.ID(context.getNewExam().getCreatedAt());
        result.setId(id.toString());
        Date currentDate = new Date();
        result.setCreateAt(currentDate);
        result.setUpdateAt(currentDate);
        result.setClazzId(context.getClazzId());
        result.setClazzGroupId(context.getClazzGroupId());
        result.setUserId(context.getUserId());
        result.setNewExamId(context.getNewExamId());
        result.setPaperDocId(context.getPaperId());
        result.setPartId(context.getPartId());
        result.setQuestionId(context.getQuestionId());
        result.setQuestionDocId(context.getQuestionDocId());
        result.setStandardScore(context.getStandardScore());
        result.setScore(context.getScoreResult().getTotalScore());
        result.setGrasp(context.getScoreResult().getIsRight());
        result.setSubGrasp(context.getSubGrasp());
        result.setSubScore(context.getSubScore());
        result.setUserAnswers(context.getAnswer());
        //累加做题时长
        if(context.getOldProcessResult() != null){
            result.setDurationMilliseconds(context.getOldProcessResult().getDurationMilliseconds() + context.getDurationMilliseconds());
        }else {
            result.setDurationMilliseconds(context.getDurationMilliseconds());
        }
        result.setSubject(context.getSubject());
        result.setClientType(context.getClientType());
        result.setClientName(context.getClientName());
        if (CollectionUtils.isNotEmpty(context.getFiles())) {
            long c = context.getFiles().stream().flatMap(Collection::stream).count();
            if (c > 0) result.setFiles(context.getFiles());
        }
        result.setAdditions(context.getAdditions());
        result.setOralDetails(context.getOralScoreDetails());
        context.setCurrentProcessResult(result);
    }
}
