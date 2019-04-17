package com.voxlearning.utopia.service.piclisten.impl.handler;

import com.voxlearning.alps.api.event.AlpsEventContext;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.MinuteTimerEventListener;
import com.voxlearning.alps.api.event.dsl.TimerEvent;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.piclisten.consumer.cache.manager.MiniProgramCacheManager;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

/**
 * @author RA
 */
@Named
@Slf4j
public class MiniProgramCommonHandler extends SpringContainerSupport {

    @Inject
    private MiniProgramCacheManager miniProgramCacheManager;


    @Inject
    private MiniProgramApiHandler miniProgramApiHandler;


    // Distribution lock
    private static final AtomicLockManager lock = AtomicLockManager.getInstance();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        MiniProgramPush event = new MiniProgramPush();
        EventBus.subscribe(event);
    }

    private class MiniProgramPush extends MinuteTimerEventListener {

        private static final String lockKey = "MINI_PROGRAM_NOTICE_PUSH_LOCK";

        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            try {
                lock.acquireLock(lockKey);

                boolean done = miniProgramCacheManager.isDoneDayPlanPush();

                if (done) {
                    // already execute,return
                    return;
                }

                Set<String> tasks = miniProgramCacheManager.getDayPlanPushTask();

                if (tasks.size() > 0) {
                    for (String mem : tasks) {

                        String[] ids = mem.split("_");
                        if (ids.length < 2) {
                            continue;
                        }
                        Long pid = SafeConverter.toLong(ids[0]);
                        Long uid = SafeConverter.toLong(ids[1]);
                        // Check remind?
                        boolean remind = miniProgramCacheManager.isRemindUserDayPlan(pid, uid);
                        if (!remind) {
                            continue;
                        }

                        // Get formId
                        String formId = miniProgramCacheManager.getUserPushFormId(pid);
                        if (StringUtils.isNotBlank(formId)) {
                            // send message to weixin
                            miniProgramApiHandler.asyncSendCheckRemindNotice(pid, uid, formId);
                        } else {
                            logger.warn("[MPMAPI] User {} no weixin program form id, give up", pid);

                        }
                    }

                }
                // Set bitmap value
                miniProgramCacheManager.doneDayPlanPush();

            } catch (CannotAcquireLockException e) {
                // Cant't get lock
                log.debug("The key [{}] can't get lock on MiniProgramPush method", lockKey);
            } finally {
                lock.releaseLock(lockKey);
            }


        }
    }
}
