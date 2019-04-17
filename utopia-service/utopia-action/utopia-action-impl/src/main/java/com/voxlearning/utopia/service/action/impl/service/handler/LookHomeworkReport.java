package com.voxlearning.utopia.service.action.impl.service.handler;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;

import javax.inject.Named;

/**
 * @author jiangpeng
 * 家长端查看作业报告
 * 成长: 1次/每科/每天，5次/每科/每周
 * 成就: 无
 */
@Named("actionEventHandler.lookHomeworkReport")
public class LookHomeworkReport extends AbstractActionEventHandler {
    @Override
    public ActionEventType getEventType() {
        return ActionEventType.LookHomeworkReport;
    }

    @Override
    public void handle(ActionEvent event) {
        String subjectName = SafeConverter.toString(event.getAttributes().get("homeworkSubject"));
        Subject subject = Subject.safeParse(subjectName);
        if(subject == null)
            return;
        // 每科每天一次
        long dc = actionEventDayRangeCounter.increase(event, subject);
        if (dc == 0 || dc > 1) {
            return;
        }

        // 每科每周5次
        long wc = actionEventWeekRangeCounter.increase(event, subject);
        if (wc == 0 || wc > 5) {
            return;
        }

        addGrowth(event);
    }
}
