package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalStudentHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.AvengerQueueServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.NewHomeworkQueueServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 需要上报的数据，异步
 *
 * @author xuesong.zhang
 * @since 2016-07-18
 */
@Named
public class FH_CreateJournalStudentHomework extends SpringContainerSupport implements FinishHomeworkTask {
    @Inject private NewHomeworkQueueServiceImpl newHomeworkQueueService;
    @Inject private AvengerQueueServiceImpl avengerQueueService;

    @Override
    public void execute(FinishHomeworkContext context) {
        NewHomework homework = context.getHomework();
        NewHomeworkResult result = context.getResult();

        JournalStudentHomework sh = new JournalStudentHomework();
        sh.setHomeworkId(homework.getId());
        if (homework.getSchoolLevel() == null) {
            sh.setSchoolLevel(SchoolLevel.JUNIOR.name());
        } else {
            sh.setSchoolLevel(homework.getSchoolLevel().name());
        }
        sh.setType(homework.getNewHomeworkType());
        sh.setHomeworkTag(homework.getHomeworkTag());
        sh.setActionId(homework.getActionId());
        sh.setTeacherId(homework.getTeacherId());
        sh.setStudentId(context.getUserId());
        sh.setSubjectId(homework.getSubject().getId());
        sh.setGroupId(homework.getClazzGroupId());
        sh.setHomeworkCreateAt(homework.getCreateAt());
        sh.setHomeworkFinishAt(new Date());
        sh.setStudentStartAt(result.getUserStartAt());
        sh.setHomeworkStartAt(homework.getStartTime());
        sh.setHomeworkEndAt(homework.getEndTime());
        sh.setIp(context.getIpImei());
        sh.setRepair(result.getRepair());
        sh.setPractices(result.getPractices());
        sh.setDuration(result.processDuration());
        sh.setEnv(RuntimeMode.getCurrentStage());
        Integer avgScore = result.processScore();
        if(avgScore != null){
            sh.setAvgScore(SafeConverter.toDouble(avgScore));
        }else {
            sh.setAvgScore(null);
        }
        newHomeworkQueueService.saveJournalStudentHomework(sh);
        avengerQueueService.sendJournalStudentHomework(sh);
    }
}
