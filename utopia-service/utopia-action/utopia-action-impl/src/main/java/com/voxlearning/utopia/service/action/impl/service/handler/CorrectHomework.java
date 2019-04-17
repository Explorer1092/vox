package com.voxlearning.utopia.service.action.impl.service.handler;

import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;

import javax.inject.Named;

/**
 * @author xinxin
 * @since 15/8/2016
 * 订正作业
 */
@Named("actionEventHandler.correctHomework")
public class CorrectHomework extends AbstractActionEventHandler {
    @Override
    public ActionEventType getEventType() {
        return ActionEventType.CorrectHomework;
    }

    @Override
    public void handle(ActionEvent event) {
        addAndGet(event.getUserId(),event.getType(),1);
    }
}
