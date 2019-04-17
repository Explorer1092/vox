package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalStudentHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.AvengerQueueServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.NewHomeworkQueueServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 需要上报的数据：异步执行
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class FVH_CreateJournalStudentHomework extends SpringContainerSupport implements FinishVacationHomeworkTask {
    @Inject private NewHomeworkQueueServiceImpl newHomeworkQueueService;
    @Inject private AvengerQueueServiceImpl avengerQueueService;

    @Override
    public void execute(FinishVacationHomeworkContext context) {
        VacationHomework homework = context.getVacationHomework();
        VacationHomeworkResult result = context.getResult();

        JournalStudentHomework sh = new JournalStudentHomework();
        sh.setHomeworkId(homework.getId());
        sh.setSchoolLevel(SchoolLevel.JUNIOR.name());
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
        sh.setPractices(result.getPractices());
        sh.setDuration(result.processDuration());
        sh.setEnv(RuntimeMode.getCurrentStage());

        List<NewHomeworkResultAnswer> answers = result.getPractices().values()
                .stream()
                .filter(o -> o.getScore() != null)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(answers)) {
            sh.setAvgScore(0D);
        } else {
            Double avgScore = answers
                    .stream()
                    .mapToDouble(NewHomeworkResultAnswer::getScore)
                    .average()
                    .orElse(0D);
            sh.setAvgScore(avgScore);
        }

        newHomeworkQueueService.saveJournalStudentHomework(sh);
        avengerQueueService.sendJournalStudentHomework(sh);
    }
}
