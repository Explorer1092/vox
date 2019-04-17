package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramCheckService;
import com.voxlearning.utopia.service.piclisten.api.entity.MiniProgramCheck;
import com.voxlearning.utopia.service.piclisten.consumer.cache.manager.MiniProgramCacheManager;
import com.voxlearning.utopia.service.piclisten.impl.dao.MiniProgramCheckDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author RA
 */

@Named
@ExposeService(interfaceClass = MiniProgramCheckService.class)
@Slf4j
public class MiniProgramCheckServiceImpl implements MiniProgramCheckService {


    @Inject
    private MiniProgramCheckDao miniProgramCheckDao;

    @Inject
    private MiniProgramCacheManager miniProgramCacheManager;

    private static final String CHECK_LOCK_KEY = "MINI_PROGRAM_DO_CHECK_LOCK:%d";

    // Distribution lock
    private static final AtomicLockManager lock = AtomicLockManager.getInstance();


    @Override
    public boolean isChecked(Long uid) {
        return miniProgramCacheManager.isChecked(uid);
    }

    @Override
    public MapMessage doCheck(Long pid, Long uid) {

        Assertions.notNull(pid, "pid must not be null!");
        Assertions.notNull(uid, "uid must not be null!");

        // Must done read plan
        boolean rpDone = miniProgramCacheManager.isDoneReadPlan(pid, uid);
        if (!rpDone) {
            return MapMessage.errorMessage("请先完成今日计划哦");
        }
        String lockKey = String.format(CHECK_LOCK_KEY, uid);
        try {
            lock.acquireLock(lockKey);
            // Is checking?
            if (isChecked(uid)) {
                return MapMessage.errorMessage("今日已经打过卡啦");
            }
            // redis record
            miniProgramCacheManager.checking(uid);

            MiniProgramCheck po = loadByUid(uid);
            if (po != null) {
                Calendar nowCalendar=Calendar.getInstance();
                Date now=nowCalendar.getTime();
                Date lastCheckTime = po.getCreateTime();
                Calendar lastCalendar = DateUtils.toCalendar(lastCheckTime);

                boolean hasCheck = DateUtils.isSameDay(lastCheckTime, now);

                // Has checked
                if (hasCheck) {
                    return MapMessage.errorMessage("今日已经打过卡了哦");
                }

                long dayDiff = DateUtils.dayDiff(now, lastCheckTime);

                if (dayDiff == 0) {
                    // After the morning
                    if (nowCalendar.get(Calendar.DAY_OF_MONTH) > lastCalendar.get(Calendar.DAY_OF_MONTH)) {
                        dayDiff = 1;
                    }
                }

                // Continuous check?
                if (dayDiff == 1) {
                    // continuous check add 1
                    po.increChecking();
                } else {
                    po.setChecking(1);
                }
                // Total check add 1
                po.incrChecked();

                // destroy id
                po.setId(null);
            } else {
                // New data
                po = new MiniProgramCheck();
                po.setChecked(1);
                po.setChecking(1);

            }
            po.setUid(uid);
            po.setPid(pid);

            miniProgramCheckDao.insert(po);
            // Update Cache
            miniProgramCacheManager.setUserCheckData(uid, po.getChecking(), po.getChecked());


        } catch (CannotAcquireLockException e) {
            // Cant't get lock
            log.debug("The key [{}] can't get lock on doCheck method", lockKey);

        } finally {
            lock.releaseLock(lockKey);
        }
        return MapMessage.successMessage();
    }


    @Override
    public Long getTodayCheckCount() {
        return miniProgramCacheManager.todayCheckedCount();
    }


    @Override
    public int getWeekContinuousCheckCount(Long uid) {
        Assertions.notNull(uid, "uid must not be null!");
        return miniProgramCacheManager.getWeekContinuousCheckCount(uid);
    }

    @Override
    public int getTotalCheckCount(Long uid) {
        MiniProgramCheck po = loadByUid(uid);
        if (po != null) {
            return po.getChecked();
        }
        return 0;

    }

    @Override
    public MapMessage loadCheckData(Long uid) {
        Assertions.notNull(uid, "uid must not be null!");

        MapMessage mm = new MapMessage();
        Map<String, Object> map = miniProgramCacheManager.getUserCheckData(uid);
        if (map != null && map.size() > 0) {
            mm = MapMessage.of(map);
        } else {

            String lockKey = String.format(CHECK_LOCK_KEY, uid);
            try {
                lock.acquireLock(lockKey);
                // load db
                MiniProgramCheck po = loadByUid(uid);

                int checking = 0, checked = 0;
                if (po != null) {
                    checking = po.getChecking();
                    checked = po.getChecked();
                }
                mm.add("checking", checking);
                mm.add("checked", checked);

                // Update cache
                miniProgramCacheManager.setUserCheckData(uid, checking, checked);

            } catch (CannotAcquireLockException e) {
                // Cant't get lock
                log.debug("The key [{}] can't get lock on syncUpdateCheckData method", lockKey);
            } finally {
                lock.releaseLock(lockKey);
            }
        }

        mm.add("today", miniProgramCacheManager.isChecked(uid));

        return mm;

    }


    @Override
    public MiniProgramCheck loadByUid(Long uid) {
        Assertions.notNull(uid, "uid must not be null!");
        return miniProgramCheckDao.loadByUid(uid);

    }


}
