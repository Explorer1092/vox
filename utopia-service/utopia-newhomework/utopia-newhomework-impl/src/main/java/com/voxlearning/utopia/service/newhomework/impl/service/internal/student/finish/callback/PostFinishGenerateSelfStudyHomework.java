package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.SelfStudyHomeworkGenerateServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.NeedSelfStudyHomeworkSubjects;

/**
 * @author xuesong.zhang
 * @since 2017/2/12
 */
@Named
public class PostFinishGenerateSelfStudyHomework extends SpringContainerSupport implements PostFinishHomework {

    @Inject private SelfStudyHomeworkGenerateServiceImpl selfStudyHomeworkGenerateService;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        NewHomework homework = context.getHomework();
        NewHomeworkResult homeworkResult = context.getResult();
        if (!(NewHomeworkUtils.isSubHomework(homework.getId()) || NewHomeworkUtils.isShardHomework(homework.getId()))
                || homeworkResult == null
                || homeworkResult.getPractices() == null
                || !NeedSelfStudyHomeworkSubjects.contains(homework.getSubject())
                || Objects.equals(homework.getSchoolLevel(), SchoolLevel.INFANT)) {
            return;
        }
        //去生成订正任务
        selfStudyHomeworkGenerateService.generateSelfStudyHomework(homework, homeworkResult);
    }

}
