package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.impl.loader.TikuStrategy;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/10.
 */
@Named
public class ER_LoadNewPaper extends SpringContainerSupport implements NewExamResultTask {

    @Inject private TikuStrategy tikuStrategy;

    @Override
    public void execute(NewExamResultContext context) {
//        NewPaper newPaper =  paperLoaderClient.loadPaperByDocid(context.getPaperId());
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(context.getPaperId());
        if (newPaper == null) {
            logger.error("NewPaper is null paperId {}", context.getPaperId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PAPER_NOT_EXIST);
            return;
        }

        context.setQuestionScoreMap(newPaper.getQuestionScoreMapByQid());
        context.setNewPaper(newPaper);
    }
}
