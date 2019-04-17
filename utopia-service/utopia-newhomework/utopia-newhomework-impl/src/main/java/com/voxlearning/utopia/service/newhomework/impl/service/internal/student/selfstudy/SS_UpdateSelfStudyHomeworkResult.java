package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy;

import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 新的结构里面，已经不做result表的更新，直接插入resultAnswer表即可
 *
 * @author xuesong.zhang
 * @since 2017/2/3
 */
@Named
public class SS_UpdateSelfStudyHomeworkResult extends SpringContainerSupport implements SelfStudyHomeworkResultTask {

    @Inject private NewHomeworkResultServiceImpl newHomeworkResultService;

    @Override
    public void execute(SelfStudyHomeworkContext context) {
        if (ObjectiveConfigType.ORAL_INTERVENTIONS.equals(context.getObjectiveConfigType())) {
            return;
        }
        SelfStudyHomework homework = context.getSelfStudyHomework();
        SubHomeworkProcessResult processResult = context.getProcessResult();
        String qid = processResult.getQuestionId();

        String day = DayRange.newInstance(homework.getCreateAt().getTime()).toString();
        String hid = homework.getId();
        List<String> joinKeys = new ArrayList<>();
        SubHomeworkResultAnswer.ID aid = new SubHomeworkResultAnswer.ID();
        aid.setDay(day);
        aid.setHid(hid);
        if (context.getObjectiveConfigType().equals(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS)) {
            joinKeys.add(processResult.getCourseId());
        }
        aid.setJoinKeys(joinKeys);
        aid.setType(context.getObjectiveConfigType());
        aid.setUserId(SafeConverter.toString(context.getUserId()));
        aid.setQuestionId(qid);
        SubHomeworkResultAnswer answer = new SubHomeworkResultAnswer();
        answer.setId(aid.toString());
        answer.setProcessId(processResult.getId());
        newHomeworkResultService.saveSubHomeworkResultAnswers(Collections.singletonList(answer));
    }
}
