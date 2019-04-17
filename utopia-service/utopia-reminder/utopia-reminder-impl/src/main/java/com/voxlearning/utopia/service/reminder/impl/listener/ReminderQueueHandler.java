package com.voxlearning.utopia.service.reminder.impl.listener;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.mapper.ReminderQueueCommand;
import com.voxlearning.utopia.service.reminder.api.ReminderService;
import com.voxlearning.utopia.service.reminder.constant.ReminderCommandType;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.reminder.constant.ReminderTarget;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2017-7-25
 */
@Named
public class ReminderQueueHandler {

    @Inject
    private ReminderService reminderService;

    public void handle(ReminderQueueCommand command) {
        if (command == null) {
            return;
        }
        if (StringUtils.isBlank(command.getTargetId()) || StringUtils.isBlank(command.getPosition()) || StringUtils.isBlank(command.getTarget()) || StringUtils.isBlank(command.getCommandType())) {
            return;
        }
        ReminderPosition position = ReminderPosition.of(command.getPosition());
        if (position == null) {
            return;
        }
        ReminderTarget target = ReminderTarget.of(command.getTarget());
        if (target == null) {
            return;
        }
        ReminderCommandType commandType = ReminderCommandType.of(command.getCommandType());
        if (commandType == null) {
            return;
        }
        if (commandType == ReminderCommandType.INCR) {
            handleIncr(command, target, position);
        } else if (commandType == ReminderCommandType.DECR) {
            handleDecr(command, target, position);
        } else {
            handleClear(command, target, position);
        }
    }

    private void handleIncr(ReminderQueueCommand command, ReminderTarget target, ReminderPosition position) {
        if (target == ReminderTarget.USER) {
            reminderService.addUserReminder(SafeConverter.toLong(command.getTargetId()), position);
        } else if (target == ReminderTarget.USER_IN_GROUP) {
            reminderService.addUserReminderInGroup(SafeConverter.toLong(command.getTargetId()), command.getGroupId(), position, command.getReminderContent());
        } else if (target == ReminderTarget.CLAZZ_GROUP) {
            reminderService.addClazzGroupReminder(SafeConverter.toLong(command.getTargetId()), position);
        }
    }

    private void handleDecr(ReminderQueueCommand command, ReminderTarget target, ReminderPosition position) {
        if (target == ReminderTarget.USER) {
            reminderService.decrUserReminder(SafeConverter.toLong(command.getTargetId()), position);
        } else if (target == ReminderTarget.USER_IN_GROUP) {
            reminderService.decrUserReminderInGroup(SafeConverter.toLong(command.getTargetId()), command.getGroupId(), position);
        } else if (target == ReminderTarget.CLAZZ_GROUP) {
            reminderService.decrClazzGroupReminder(SafeConverter.toLong(command.getTargetId()), position);
        }
    }

    private void handleClear(ReminderQueueCommand command, ReminderTarget target, ReminderPosition position) {
        if (target == ReminderTarget.USER) {
            reminderService.clearUserReminder(SafeConverter.toLong(command.getTargetId()), position);
        } else if (target == ReminderTarget.USER_IN_GROUP) {
            reminderService.clearUserReminderInGroup(SafeConverter.toLong(command.getTargetId()), command.getGroupId(), position);
        } else if (target == ReminderTarget.CLAZZ_GROUP) {
            reminderService.clearClazzGroupReminder(SafeConverter.toLong(command.getTargetId()), position);
        }
    }
}
