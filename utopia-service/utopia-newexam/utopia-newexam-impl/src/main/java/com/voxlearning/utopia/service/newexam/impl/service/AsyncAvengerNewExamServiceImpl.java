package com.voxlearning.utopia.service.newexam.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.newexam.api.entity.AvengerNewExam;
import com.voxlearning.utopia.service.newexam.api.service.AsyncAvengerNewExamService;
import com.voxlearning.utopia.service.newexam.impl.service.helper.AvengerNewExamHelper;
import com.voxlearning.utopia.service.question.api.entity.NewExam;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @Description: 考试(单元检测)数据上报
 * @author: Mr_VanGogh
 * @date: 2019/4/10 下午4:56
 */
@Named
@ExposeService(interfaceClass = AsyncAvengerNewExamService.class)
public class AsyncAvengerNewExamServiceImpl extends SpringContainerSupport implements AsyncAvengerNewExamService {

    @Inject
    private AvengerNewExamHelper avengerHomeworkHelper;

    @Override
    public AlpsFuture<AvengerNewExam> informNewExamToBigData(NewExam newExam) {
        AvengerNewExam avengerNewExam = avengerHomeworkHelper.generateAvengerNewExam(newExam);
        return new ValueWrapperFuture<>(avengerNewExam);
    }

}
