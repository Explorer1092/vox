package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramReadService;
import com.voxlearning.utopia.service.piclisten.api.entity.MiniProgramRead;
import com.voxlearning.utopia.service.piclisten.consumer.cache.manager.MiniProgramCacheManager;
import com.voxlearning.utopia.service.piclisten.impl.dao.MiniProgramReadDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author RA
 */

@Named
@ExposeService(interfaceClass = MiniProgramReadService.class)
@Slf4j
public class MiniProgramReadServiceImpl implements MiniProgramReadService {


    @Inject
    private MiniProgramReadDao miniProgramReadDao;

    @Inject
    private MiniProgramCacheManager miniProgramCacheManager;


    private static final String READ_LOCK_KEY = "MINI_PROGRAM_DO_READ_LOCK:%d";


    // Distribution lock
    private static final AtomicLockManager lock = AtomicLockManager.getInstance();


    @Override
    public MiniProgramRead save(MiniProgramRead po) {
        if (po != null) {
            if (po.getId() != null) {
                // Update
                MiniProgramRead lpo = miniProgramReadDao.load(po.getId());
                if (lpo != null) {

                    po.setCreateTime(null);
                    po.setUpdateTime(null);
                    return miniProgramReadDao.replace(po);
                }

            } else {
                po.setVersion(0L);
                miniProgramReadDao.insert(po);
            }


        }
        return null;

    }


    @Override
    public void addNoticeFormId(Long pid, String formId) {
        Assertions.notNull(pid, "pid must not be null!");
        Assertions.notBlank(formId, "formId must bot be blank");

        miniProgramCacheManager.addUserPushFormIds(pid, formId);
    }


    @Override
    public MapMessage setUserDayPlan(Long pid, Long uid, int planMinutes, int remind, String remindTime) {
        Assertions.notNull(pid, "uid must not be null!");
        Assertions.notNull(uid, "pid must not be null!");

        LocalTime time;
        try {
            time = LocalTime.parse(remindTime,DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception e) {
           return MapMessage.errorMessage("时间格式不支持!");
        }
        remindTime = time.toString();
        miniProgramCacheManager.setUserDayPlanData(pid, uid, planMinutes, remind, remindTime);
        return MapMessage.successMessage();

    }

    @Override
    public MapMessage getUserDayPlan(Long pid, Long uid) {
        Assertions.notNull(pid, "uid must not be null!");
        Assertions.notNull(uid, "uid must not be null!");

        Map<String, Object> map = new HashMap<>();

        Map<String, Object> rmap = miniProgramCacheManager.getUserDayPlanData(pid, uid);

        map.put("plan_minutes", rmap.get("plan_minutes"));
        map.put("is_remind", rmap.get("is_remind"));
        map.put("remind_time", rmap.get("remind_time"));

        return MapMessage.of(map);

    }

    @Override
    public void incrReadData(Long uid, Long readMillis, Integer readWords) {

        Assertions.notNull(uid, "uid must not be null!");

        if (readMillis == null) {
            readMillis = 0L;
        }
        if (readWords == null) {
            readWords = 0;
        }

        if (readMillis <= 0 && readWords <= 0) {
            return; // do not change
        }

        String lockKey = String.format(READ_LOCK_KEY, uid);
        try {
            lock.acquireLock(lockKey);

            MiniProgramRead po = loadByUid(uid);
            if (po == null) {
                po = new MiniProgramRead();
                po.setReadWords(readWords);
                po.setReadTimes(readMillis);
                po.setUid(uid);
                miniProgramReadDao.insert(po);
            } else {
                MiniProgramRead dto = new MiniProgramRead();
                dto.setId(po.getId());
                dto.setReadTimes(po.getReadTimes() + readMillis);
                dto.setReadWords(po.getReadWords() + readWords);
                miniProgramReadDao.replace(dto);
            }

            // Increment today read minutes cache
            miniProgramCacheManager.increTodayReadTime(uid, readMillis);

        } catch (CannotAcquireLockException e) {
            // Cant't get lock
            log.debug("The key [{}] can't get lock on incrReadData method", lockKey);

        } finally {
            lock.releaseLock(lockKey);
        }

    }


    @Override
    public MapMessage getTodayReadData(Long pid, Long uid) {
        Assertions.notNull(pid, "pid must not be null!");
        Assertions.notNull(uid, "uid must not be null!");
        Map<String, Object> map = new HashMap<>();
        map.put("today_read_plan", miniProgramCacheManager.getTodayReadPlan(pid, uid));
        map.put("today_read_time", miniProgramCacheManager.getTodayReadTime(uid));
        return MapMessage.of(map);

    }

    /**
     * Start Monday,end by sunday
     *
     * @param uid
     * @return
     */
    @Override
    public List<Long> getWeekReadTimes(Long uid) {
        Assertions.notNull(uid, "uid must not be null!");
        return miniProgramCacheManager.getWeekReadTimeData(uid);
    }


    @Override
    public Long getTotalReadTimes(Long uid) {

        MiniProgramRead po = loadByUid(uid);
        if (po != null) {
            return TimeUnit.MILLISECONDS.toMinutes(po.getReadTimes());
        }
        return 0L;

    }


    @Override
    public MiniProgramRead loadByUid(Long uid) {
        Assertions.notNull(uid, "uid must not be null!");
        return miniProgramReadDao.loadByUid(uid);

    }


}
