package com.voxlearning.utopia.service.newexam.impl.service.helper;

import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.newexam.api.entity.AvengerNewExam;
import com.voxlearning.utopia.service.newexam.impl.service.AvengerQueueServiceImpl;
import com.voxlearning.utopia.service.question.api.entity.NewExam;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * @Description: 单元测试上报
 * @author: Mr_VanGogh
 * @date: 2019/4/10 下午4:14
 */
@Named
public class AvengerNewExamHelper {

    @Inject
    private AvengerQueueServiceImpl avengerQueueService;

    public AvengerNewExam generateAvengerNewExam(NewExam newExam) {
        if (newExam == null) {
            return null;
        }
        AvengerNewExam avengerNewExam = new AvengerNewExam();
        avengerNewExam.setId(null);
        avengerNewExam.setHomeworkId(avengerNewExam.getId());
        avengerNewExam.setEnv(RuntimeMode.getCurrentStage());
        avengerNewExam.setTimeFiled(new Date());
        avengerNewExam.setSchoolLevel(newExam.getSchoolLevel());
        avengerNewExam.setSubject(newExam.getSubject());
        avengerNewExam.setTitle(newExam.getName());
        avengerNewExam.setStartTime(newExam.getExamStartAt());
        avengerNewExam.setEndTime(newExam.getExamStopAt());
        avengerQueueService.sendExam(avengerNewExam);
        return avengerNewExam;
    }
}
