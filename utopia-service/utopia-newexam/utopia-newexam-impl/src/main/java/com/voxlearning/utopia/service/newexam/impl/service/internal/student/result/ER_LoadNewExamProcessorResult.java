package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamProcessResultDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/22.
 */
@Named
public class ER_LoadNewExamProcessorResult  extends SpringContainerSupport implements NewExamResultTask{
    @Inject private NewExamProcessResultDao newExamProcessResultDao;

    @Override
    public void execute(NewExamResultContext context) {
        //查询老的结果，在后处理里面用来算考试总分，如果学生重复答题则以最后一次有效，那么总分先减去原来分数然后再加上当前得分
        if(context.getNewExamResult().getAnswers() != null && context.getNewExamResult().getAnswers().get(context.getQuestionDocId()) != null){
            context.setOldProcessResult(newExamProcessResultDao.load(context.getNewExamResult().getAnswers().get(context.getQuestionDocId())));
        }
    }
}
