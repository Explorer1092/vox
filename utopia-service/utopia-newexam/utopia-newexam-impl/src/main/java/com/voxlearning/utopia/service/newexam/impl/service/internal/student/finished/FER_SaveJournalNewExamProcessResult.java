package com.voxlearning.utopia.service.newexam.impl.service.internal.student.finished;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.newexam.api.context.FinishNewExamContext;
import com.voxlearning.utopia.service.newexam.api.entity.JournalStudentNewExam;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.impl.queue.NewExamQueueProducer;
import com.voxlearning.utopia.service.newexam.impl.service.AvengerQueueServiceImpl;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.consumer.NewExamLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * Created by tanguohong on 2016/3/25.
 */
@Named
public class FER_SaveJournalNewExamProcessResult extends SpringContainerSupport implements FinishNewExamTask {
    @Inject private NewExamQueueProducer newExamQueueProducer;
    @Inject private AvengerQueueServiceImpl avengerQueueService;
    @Inject private NewExamLoaderClient newExamLoaderClient;

    @Override
    public void execute(FinishNewExamContext context) {
        newExamQueueProducer.sendSaveResultMessage(context.getCurrentProcessResult());

        NewExamProcessResult currentProcessResult = context.getCurrentProcessResult();
        NewExam newExam = newExamLoaderClient.load(currentProcessResult.getNewExamId());

        // TODO: 2019/4/10   ?????
        JournalStudentNewExam sh = new JournalStudentNewExam();
        sh.setHomeworkId(currentProcessResult.getNewExamId());
        sh.setSchoolLevel(SchoolLevel.JUNIOR.name());
        //sh.setActionId(currentProcessResult.getActionId());
        sh.setTeacherId(newExam.getTeacherId());
        sh.setStudentId(currentProcessResult.getUserId());
        sh.setSubjectId(currentProcessResult.getSubject().getId());
        sh.setGroupId(currentProcessResult.getClazzGroupId());
        sh.setHomeworkCreateAt(currentProcessResult.getCreateAt());
        sh.setHomeworkFinishAt(new Date());
        //sh.setStudentStartAt(newExam.getUserStartAt());
        sh.setHomeworkStartAt(newExam.getExamStartAt());
        sh.setHomeworkEndAt(newExam.getExamStopAt());
        //sh.setIp(context);
        sh.setEnv(RuntimeMode.getCurrentStage());
        avengerQueueService.sendJournalStudentExam(sh);
    }
}
