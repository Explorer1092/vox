package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignselfstudy;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.context.AssignSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.HomeworkSelfStudyRef;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.HomeworkSelfStudyRefDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/3/27
 */
@Named
public class AHSS_SaveHomeworkSelfStudyRef extends SpringContainerSupport implements AssignSelfStudyHomeworkTask {

    @Inject private HomeworkSelfStudyRefDao homeworkSelfStudyRefDao;

    @Override
    public void execute(AssignSelfStudyHomeworkContext context) {
        SelfStudyHomework homework = context.getAssignedHomework();
        if (!StringUtils.equalsIgnoreCase(homework.getHomeworkTag().name(), HomeworkTag.Correct.name())) {
            return;
        }

        HomeworkSelfStudyRef ref = new HomeworkSelfStudyRef();
        HomeworkSelfStudyRef.ID ID = new HomeworkSelfStudyRef.ID(context.getSourceHomeworkId(), context.getStudentId());
        ref.setId(ID.toString());
        ref.setHomeworkId(homework.getSourceHomeworkId());
        ref.setSelfStudyId(homework.getId());
        ref.setSubject(homework.getSubject());
        ref.setGroupId(homework.getClazzGroupId());
        ref.setStudentId(homework.getStudentId());
        homeworkSelfStudyRefDao.insertIfNull(ref);
    }
}
