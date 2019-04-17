package com.voxlearning.utopia.service.reminder.impl.cache;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.reminder.api.mapper.ReminderContext;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.reminder.constant.ReminderTarget;
import com.voxlearning.utopia.service.reminder.impl.support.ReminderCacheSystem;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

/**
 * @author shiwei.liao
 * @since 2017-5-10
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestReminderCacheSystem {

    @Inject
    private ReminderCacheSystem reminderCacheSystem;

    @Test
    public void testUserReminder() {
        /*for (Long i = 0L; i < 10L; i++) {
            reminderCacheSystem.getReminderCache().incr(ReminderTarget.USER, ReminderPosition.PARENT_APP_NEW_INDEX_CHILD, i.toString());
            if (i % 2 == 0) {
                reminderCacheSystem.getReminderCache().incr(ReminderTarget.USER, ReminderPosition.PARENT_APP_NEW_INDEX_CHILD, i.toString());
            }
        }*/
        ReminderContext context = reminderCacheSystem.getReminderCache().load(ReminderTarget.USER, ReminderPosition.PARENT_APP_NEW_INDEX_CHILD, "0");
        Assert.assertEquals(2,context.getReminderCount().intValue());
        context = reminderCacheSystem.getReminderCache().load(ReminderTarget.USER, ReminderPosition.PARENT_APP_NEW_INDEX_CHILD, "1");
        Assert.assertEquals(1,context.getReminderCount().intValue());
        context = reminderCacheSystem.getReminderCache().load(ReminderTarget.USER, ReminderPosition.PARENT_APP_NEW_INDEX_CHILD, "2");
        Assert.assertEquals(2,context.getReminderCount().intValue());

    }

}
