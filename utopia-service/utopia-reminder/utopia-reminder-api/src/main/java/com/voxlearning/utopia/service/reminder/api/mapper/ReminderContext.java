package com.voxlearning.utopia.service.reminder.api.mapper;

import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.reminder.constant.ReminderTarget;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author shiwei.liao
 * @since 2017-5-9
 */
@Getter
@Setter
@NoArgsConstructor
public class ReminderContext implements Serializable {

    private static final long serialVersionUID = -2432414416749917134L;
    private String targetId;
    private ReminderTarget target;
    private ReminderPosition position;
    private Integer reminderCount;
    private Integer reminderNumCount;
    private String reminderContent;
    private Date lastUpdateDate;
}
