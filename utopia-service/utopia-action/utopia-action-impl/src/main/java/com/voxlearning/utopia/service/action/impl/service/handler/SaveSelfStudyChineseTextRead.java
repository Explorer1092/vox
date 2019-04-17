package com.voxlearning.utopia.service.action.impl.service.handler;

import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;

import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 16/8/16
 * 家长端-语文朗读-录音并保存
 * 成长:1次/每天，5次/每周
 * 成就: 无
 */
@Named("actionEventHandler.saveSelfStudyChinseTextRead")
public class SaveSelfStudyChineseTextRead extends AbstractActionEventHandler {
    @Override
    public ActionEventType getEventType() {
        return ActionEventType.SaveSelfStudyChineseTextRead;
    }

    @Override
    public void handle(ActionEvent event) {
        // 每天一次
        long dc = actionEventDayRangeCounter.increase(event);
        if (dc == 0 || dc > 1) {
            return;
        }

        // 每周5次
        long wc = actionEventWeekRangeCounter.increase(event);
        if (wc == 0 || wc > 5) {
            return;
        }

        addGrowth(event);
    }
}
