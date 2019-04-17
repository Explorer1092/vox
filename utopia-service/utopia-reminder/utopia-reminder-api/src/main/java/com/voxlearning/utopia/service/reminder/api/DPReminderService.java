package com.voxlearning.utopia.service.reminder.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.concurrent.TimeUnit;

/**
 * @author shiwei.liao
 * @since 2017-5-23
 */
@ServiceVersion(version = "20170523")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
public interface DPReminderService extends IPingable {

    Boolean addLiveTabReminderToUser(Long userId);
}
