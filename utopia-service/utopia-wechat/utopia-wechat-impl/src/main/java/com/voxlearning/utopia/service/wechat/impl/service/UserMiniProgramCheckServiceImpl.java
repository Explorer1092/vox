package com.voxlearning.utopia.service.wechat.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.wechat.api.UserMiniProgramCheckService;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramApi;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import com.voxlearning.utopia.service.wechat.api.entities.MiniProgramNoticeFormId;
import com.voxlearning.utopia.service.wechat.api.entities.UserMiniProgramCheck;
import com.voxlearning.utopia.service.wechat.cache.UserMiniProgramCacheManager;
import com.voxlearning.utopia.service.wechat.impl.dao.MiniProgramNoticeFormIdDao;
import com.voxlearning.utopia.service.wechat.impl.dao.UserMiniProgramCheckDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;


@Named
@ExposeService(interfaceClass = UserMiniProgramCheckService.class)
@Slf4j
public class UserMiniProgramCheckServiceImpl implements UserMiniProgramCheckService {


    @Inject
    private UserMiniProgramCheckDao userMiniProgramCheckDao;

    @Inject
    private MiniProgramNoticeFormIdDao miniProgramNoticeFormIdDao;

    @Inject
    private UserMiniProgramCacheManager userMiniProgramCacheManager;

    private static final String CHECK_LOCK_KEY = "MINI_PROGRAM_DO_CHECK_LOCK:%s_%d";

    // Distribution lock
    private static final AtomicLockManager lock = AtomicLockManager.getInstance();


    @Override
    public boolean isChecked(Long uid, MiniProgramType type) {
        return userMiniProgramCacheManager.isChecked(uid, type);
    }

    @Override
    public MapMessage doCheck(Long uid, MiniProgramType type) {

        Assertions.notNull(uid, "uid must not be null!");
        Assertions.notNull(type, "type must not be null!");

        String lockKey = String.format(CHECK_LOCK_KEY, type, uid);
        try {
            lock.acquireLock(lockKey);
            // Is checking?
            if (isChecked(uid, type)) {
                return MapMessage.errorMessage("今日已经打过卡啦");
            }
            // redis record
            userMiniProgramCacheManager.checking(uid, type);

            UserMiniProgramCheck po = loadByUid(uid, type);
            if (po != null) {
                Calendar nowCalendar = Calendar.getInstance();
                Date now = nowCalendar.getTime();
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
                po = new UserMiniProgramCheck();
                po.setType(type);
                po.setChecked(1);
                po.setChecking(1);

            }
            po.setUid(uid);

            userMiniProgramCheckDao.save(po);
            // Update Cache
            userMiniProgramCacheManager.setUserCheckData(uid, po.getChecking(), po.getChecked(), type);


        } catch (CannotAcquireLockException e) {
            // Cant't get lock
            log.debug("The key [{}] can't get lock on doCheck method", lockKey);

        } finally {
            lock.releaseLock(lockKey);
        }
        return MapMessage.successMessage();
    }


    @Override
    public Long getTodayCheckCount(MiniProgramType type) {
        Assertions.notNull(type, "type must not be null!");
        return userMiniProgramCacheManager.todayCheckedCount(type);
    }

    @Override
    public int getWeekContinuousCheckCount(Long uid, MiniProgramType type) {
        Assertions.notNull(uid, "uid must not be null!");
        Assertions.notNull(type, "type must not be null!");
        return userMiniProgramCacheManager.getWeekContinuousCheckCount(uid, type);
    }


    @Override
    public int getTotalCheckCount(Long uid, MiniProgramType type) {
        Assertions.notNull(type, "type must not be null!");
        UserMiniProgramCheck po = loadByUid(uid, type);
        if (po != null) {
            return po.getChecked();
        }
        return 0;

    }

    @Override
    public MapMessage loadCheckData(Long uid, MiniProgramType type) {
        Assertions.notNull(uid, "uid must not be null!");
        Assertions.notNull(type, "type must not be null!");

        MapMessage mm = new MapMessage();
        Map<String, Object> map = userMiniProgramCacheManager.getUserCheckData(uid, type);
        if (map != null && map.size() > 0) {
            mm = MapMessage.of(map);
        } else {

            String lockKey = String.format(CHECK_LOCK_KEY, type, uid);
            try {
                lock.acquireLock(lockKey);
                // load db
                UserMiniProgramCheck po = loadByUid(uid, type);

                int checking = 0, checked = 0;
                if (po != null) {
                    checking = po.getChecking();
                    checked = po.getChecked();
                }
                mm.add("checking", checking);
                mm.add("checked", checked);

                // Update cache
                userMiniProgramCacheManager.setUserCheckData(uid, checking, checked, type);

            } catch (CannotAcquireLockException e) {
                // Cant't get lock
                log.debug("The key [{}] can't get lock on syncUpdateCheckData method", lockKey);
            } finally {
                lock.releaseLock(lockKey);
            }
        }

        mm.add("today", userMiniProgramCacheManager.isChecked(uid, type));

        return mm;

    }


    @Override
    public UserMiniProgramCheck loadByUid(Long uid, MiniProgramType type) {
        Assertions.notNull(uid, "uid must not be null!");
        Assertions.notNull(type, "type must not be null!");
        return userMiniProgramCheckDao.loadByUid(uid, type);

    }


    @Override
    public void addNoticeFormId(Long uid, String formId, MiniProgramType type) {
        Assertions.notNull(uid, "pid must not be null!");
        Assertions.notBlank(formId, "formId must bot be blank");
        Assertions.notNull(type, "type must not be null!");

        userMiniProgramCacheManager.addUserPushFormIds(uid, formId,type);
    }


    @Override
    public void addNoticeFormId(String openId, String formId, MiniProgramType type) {
        Assertions.notNull(type, "type must not be null!");
        if (StringUtils.isAnyBlank(openId, formId)) {
            return;
        }

        MiniProgramNoticeFormId po = new MiniProgramNoticeFormId();
        po.setFormId(formId);
        po.setOpenId(openId);
        po.setType(type);

        miniProgramNoticeFormIdDao.insert(po);
    }

    @Override
    public List<MiniProgramNoticeFormId> loadNoticeFormId(String openId, MiniProgramType type) {
        return miniProgramNoticeFormIdDao.loadByOpenId(openId, type);
    }

    @Override
    public void expireNoticeFormId(List<String> ids) {
        miniProgramNoticeFormIdDao.removes(ids);
    }

    @Override
    public String useNoticeFormId(String openId, MiniProgramType type) {
        List<MiniProgramNoticeFormId> formIds = loadNoticeFormId(openId, type);
        if (formIds.isEmpty()) {
            return "";
        }
        MiniProgramNoticeFormId latest = formIds.get(0);
        String id = latest.getFormId();
        // expire
        expireNoticeFormId(Collections.singletonList(latest.getId()));
        return id;
    }

    @Override
    public String getAccessToken(MiniProgramType type) {
        return getAccessToken(type, false);
    }

    @Override
    public String getAccessTokenNoCache(MiniProgramType type) {
        return getAccessToken(type, true);
    }


    private String getAccessToken(MiniProgramType type,boolean forceRefresh) {

        String key = type.getAccessTokenCacheKey();


        IRedisCommands redisCommands = userMiniProgramCacheManager.getRedisCommands();
        Object obj = redisCommands.sync().getRedisStringCommands().get(key);

        if (obj != null && !forceRefresh) {
            return String.valueOf(obj);
        }

        // Fetch new access token

        String url = MiniProgramApi.ACCESS_TOKEN.url(ProductConfig.get(type.getAppId()), ProductConfig.get(type.getAppSecret()));

        try {

            AlpsHttpResponse resp = HttpRequestExecutor.defaultInstance().get(url).socketTimeout(10000).execute();

            String result = resp.getResponseString();

            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "mod1", type,
                    "mod2", url,
                    "mod3", result,
                    "op", "MP_AccessToken"
            ));

            if (200 != resp.getStatusCode()) {
                log.error("Get mini program access token error, code: {} ,message: {}", resp.getStatusCode(), resp.getResponseString());
                return "";
            }


            Map<String, Object> ret = JsonUtils.fromJson(result);

            if (null != ret.get("access_token")) {
                String accessToken = SafeConverter.toString(ret.get("access_token"));
                redisCommands.sync().getRedisStringCommands().setex(key, 3600, accessToken);
                return accessToken;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return "";

    }




}
