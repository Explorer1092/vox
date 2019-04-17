package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.ai.api.TobbitMathScoreService;
import com.voxlearning.utopia.service.ai.api.TobbitMathService;
import com.voxlearning.utopia.service.ai.cache.manager.TobbitCacheManager;
import com.voxlearning.utopia.service.ai.constant.TobbitScoreType;
import com.voxlearning.utopia.service.ai.entity.TobbitMathCourse;
import com.voxlearning.utopia.service.ai.entity.TobbitMathScoreHistory;
import com.voxlearning.utopia.service.ai.impl.persistence.TobbitMathCourseDao;
import com.voxlearning.utopia.service.ai.impl.persistence.TobbitMathScoreHistoryDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = TobbitMathScoreService.class)
@Slf4j
public class TobbitMathScoreServiceImpl implements TobbitMathScoreService {

    @Inject
    private TobbitCacheManager tobbitCacheManager;

    @Inject
    private TobbitMathScoreHistoryDao tobbitMathScoreHistoryDao;
    @Inject
    private TobbitMathCourseDao tobbitMathCourseDao;

    @Inject
    private TobbitMathService tobbitMathService;


    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.tobbit.math.mp.user.score", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer producer;


    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024));


    public static final long MAX_SCORE_DAY = 30;


    private static final String DATE_PATTERN = "yyyy-MM-dd";


    private static final String SCORE_LOCK_KEY = "TOBBIT_MINI_PROGRAM_SCORE_LOCK:%s_%d";

    // Distribution lock
    private static final AtomicLockManager lock = AtomicLockManager.getInstance();


    @Override
    public MapMessage history(Long uid) {

        List<TobbitMathScoreHistory> list = tobbitMathScoreHistoryDao.loadByUid(uid, 20);
        if (list.size() > 0) {

            List<Map> listm = new ArrayList<>(list.size());

            list.forEach(x -> {
                Map<String, Object> map = new HashMap<>();
                map.put("createTime", DateUtils.dateToString(x.getCreateTime(), DATE_PATTERN));
                TobbitScoreType type = TobbitScoreType.of(x.getType());
                map.put("score", x.getScore());
                map.put("name", type.getName());
                listm.add(map);
            });

            return MapMessage.successMessage().add("data", listm);
        }

        return MapMessage.successMessage().add("data", Collections.emptyList());
    }

    @Override
    public long total(Long uid) {

        long score = tobbitCacheManager.getTotalScore(uid);
        if (score > 0) {
            return score;
        }

        score = getTotalScore(uid);
        tobbitCacheManager.setTotalScore(uid, score);
        return score;

    }


    @Override
    public boolean addScore(Long uid, TobbitScoreType type) {
        return addScore(uid, "", type);
    }

    @Override
    public boolean addScore(Long uid, String openId, TobbitScoreType type) {

        Assertions.notNull(uid, "uid must not be null");
        Assertions.notNull(type, "score type must not be null");


        boolean ok = AtomicLock(uid, (a) -> {

            long addScore = type.getScore();

            if (TobbitScoreType.SIGNUP == type) {
                // 首次登录不计单天最大
                if (firstPlayScore(uid)) {
                    _addScoreM(uid, openId, addScore, type, false);
                    return true;
                }
                return false;

            } else {

                long score = tobbitCacheManager.getTodayScore(uid);

                if (score > 0) {
                    // over max score day
                    if (score >= MAX_SCORE_DAY) {
                        return false;
                    }

                    // score add new score over max day
                    if (addScore + score > MAX_SCORE_DAY) {
                        addScore = MAX_SCORE_DAY - score;
                    }

                } else {
                    // 老用户默认给积分
                    if (firstPlayScore(uid)) {
                        TobbitScoreType _utype = TobbitScoreType.SIGNUP;
                        _addScoreM(uid, openId, (long) _utype.getScore(), _utype, false);
                    }

                    if (score < 0) {
                        // NOT SUPPORT
                        return false;
                    }
                }

                _addScoreM(uid, openId, addScore, type, true);

            }

            return true;
        });

        if (!ok) {
            // log
            syncLog(uid, openId, type, 0, "");
        }

        return ok;
    }


    private void _addScoreM(Long uid, String openId, Long addScore, TobbitScoreType type, boolean isIncre) {

        // persistence
        String pid = addScoreLog(uid, type, addScore, null, null);

        if (isIncre) {
            tobbitCacheManager.addTodayScore(uid, addScore);
        }
        // log
        syncLog(uid, openId, type, addScore, pid);

    }


    @Override
    public MapMessage redeemCourse(Long uid, String cid) {
        Assertions.notNull(uid, "uid must not be null");
        Assertions.notBlank(cid, "course id can not be blank");

        AtomicLong logScore = new AtomicLong(0);
        StringBuffer sid = new StringBuffer();

        TobbitMathCourse course = tobbitMathCourseDao.load(cid);
        if (course == null) {
            return MapMessage.errorMessage("课程已下架");
        }


        MapMessage mm = MapMessage.successMessage();
        if (course.getTrail()) {
            return mm.add("data", "兑换成功\n快去学习吧");
        }

        boolean s = AtomicLock(uid, (a) -> {

            long totalScore = getTotalScore(uid);

            if (totalScore <= 0) {
                return false;
            }

            TobbitScoreType type = TobbitScoreType.REDEEM50;

            // Default need score
            int needScore = -type.getScore();


            if (course.getCredit() != null) {
                needScore = Math.abs(course.getCredit());
            }
            if (needScore > totalScore) {
                return false;
            }

            // expire cache
            tobbitCacheManager.updateUserCourse(uid, null);

            // persistence
            String pid = addScoreLog(uid, type, -needScore, cid, null);

            sid.append(pid);
            logScore.addAndGet(-needScore);
            return true;
        });

        if (s) {
            return mm.add("data", "兑换成功\n快去学习吧");
        }

        // log
        syncLog(uid, "", TobbitScoreType.REDEEM50, logScore.longValue(), sid.toString());

        return MapMessage.errorMessage().setInfo("您的积分不足~\n快去获得更多积分吧");
    }


    private List<TobbitMathCourse> loadAllCourse() {
        List<TobbitMathCourse> all = tobbitCacheManager.loadAllCourse();
        if (all.size() > 0) {
            return all;
        }

        // load from db
        List<TobbitMathCourse> courseList = tobbitMathCourseDao.load();
        if (courseList.size() > 0) {
            tobbitCacheManager.updateAllCourse(courseList);
        }

        return courseList;

    }

    @Override
    public MapMessage course(Long uid) {


        if (uid == null) {
            // No Login users
            uid = 0L;
        }
        List<TobbitMathCourse> userCouse;
        List<TobbitMathCourse> list = tobbitCacheManager.loadUserCourse(uid);
        if (list.size() > 0) {
            userCouse = list;
        } else {

            // update cache
            List<TobbitMathCourse> courseList = loadAllCourse();

            courseList.forEach(x -> {
                x.setDisabled(null);
                x.setVersion(null);
                x.setUpdateTime(null);
                x.setCreateTime(null);
            });

            Set<String> allCIDS = courseList.stream().map(TobbitMathCourse::getId).collect(Collectors.toSet());

            List<TobbitMathScoreHistory> shs = tobbitMathScoreHistoryDao.loadByUidCids(uid, allCIDS);

            Set<String> cids = shs.stream().filter(x -> x.getCid() != null).map(TobbitMathScoreHistory::getCid).collect(Collectors.toSet());

            userCouse = new ArrayList<>();


            for (int i = 0; i < courseList.size(); i++) {
                TobbitMathCourse c = courseList.get(i);

                boolean trail = c.getTrail() == null ? false : c.getTrail();

                if (!trail && !cids.contains(c.getId())) {
                    c.setVideoUrl("");
                    c.setKeyPoint("");
                }
                userCouse.add(c);
            }


            // Sort
            List<TobbitMathCourse> sortedCourse = userCouse.stream().sorted(Comparator.comparing(TobbitMathCourse::getVideoUrl).reversed().thenComparing(TobbitMathCourse::getSeq)).collect(Collectors.toList());

            tobbitCacheManager.updateUserCourse(uid, sortedCourse);

            userCouse = sortedCourse;

        }

        return MapMessage.successMessage().add("data", userCouse).add("score", total(uid));
    }

    @Override
    public MapMessage invite(String openId, String inviter) {

        Assertions.notBlank(openId, "openId cant not be null");
        Assertions.notBlank(inviter, "inviter can not be blank");
        boolean isInvited = tobbitCacheManager.invited(openId, inviter);

        long uid = SafeConverter.toLong(inviter);

        if (uid > 0 && !isInvited) {

            if (tobbitMathService.isNewUser(openId) && tobbitMathService.hasUser(uid)) {
                boolean s = addScore(uid, TobbitScoreType.INVITE);
                if (s) {
                    return MapMessage.successMessage().add("score", TobbitScoreType.INVITE.json());
                }
            }
        }

        return MapMessage.errorMessage();
    }


    @Override
    public void addCourseDoNotCallIfYouConfused(List<TobbitMathCourse> courses) {
        if (courses.size() > 0) {
            tobbitMathCourseDao.inserts(courses);
            tobbitCacheManager.updateAllCourse(null);
        }

    }

    @Override
    public void cleanCourseDoNotCallIfYouConfused() {
        List<TobbitMathCourse> course = tobbitMathCourseDao.query();
        Set<String> ids = course.stream().map(TobbitMathCourse::getId).collect(Collectors.toSet());
        if (ids.size() > 0) {
            tobbitMathCourseDao.removes(ids);
            tobbitCacheManager.updateAllCourse(null);
        }

    }


    private boolean AtomicLock(Long uid, Predicate<String> predicate) {

        String lockKey = String.format(SCORE_LOCK_KEY, today(), uid);
        try {
            lock.acquireLock(lockKey);
            return predicate.test(lockKey);
        } catch (CannotAcquireLockException e) {
            // Cant't get lock
            log.debug("The key [{}] can't get lock", lockKey);
        } catch (Exception e) {
            log.error("Atomic operate failed,error: {} ", e.getMessage());
        } finally {
            lock.releaseLock(lockKey);
        }

        return false;
    }


    private String addScoreLog(Long uid, TobbitScoreType type, long score, String cid, String[] ext) {

        TobbitMathScoreHistory po = new TobbitMathScoreHistory();

        po.setScore(score);
        po.setType(type.getType());
        po.setUid(uid);
        po.setTotalScore(getTotalScore(uid) + score);
        po.setCid(cid);
        po.setExt(ext);

        tobbitMathScoreHistoryDao.save(po);
        // update Total score Cache
        tobbitCacheManager.setTotalScore(uid, po.getTotalScore());

        return po.getId();
    }


    public void syncLog(long uid, String openId, TobbitScoreType type, long addScore, String sid) {

//        module: m_pQd5XuaxTB
//        op: events_pointbehavior
//        s0: 行为类型 首次登录（firstlogin）、成功批改（identify）、分享（share）、拉新（newuser）
//        s1: 积分id
//        s2: 应获得积分数
//        s3: 实际获得积分数
//        s4: openId
//        s5: uid

        Map<String, Object> map = new HashMap<>();
        map.put("module", "m_pQd5XuaxTB");
        map.put("op", "events_pointbehavior");
        map.put("s0", type);
        map.put("s1", sid);
        map.put("s2", type.getScore());
        map.put("s3", addScore);
        map.put("s4", openId);
        map.put("s5", uid);

        final String json = JsonUtils.toJson(map);

        try {
            EXECUTOR_SERVICE.submit(() -> {
                Message message = Message.newMessage();
                message.withPlainTextBody(json);
                producer.produce(message);

            });
        } catch (RejectedExecutionException executionException) {
            log.warn("Tobbit math log queen full,do not send data anymore");
        } catch (Exception e) {
            log.error("Tobbit math log queen cause error: {}", e.getMessage());
        }


    }

    private boolean firstPlayScore(Long uid) {
        List<TobbitMathScoreHistory> list = tobbitMathScoreHistoryDao.loadByUid(uid, 1);
        return list.size() < 1;
    }

    private long getTotalScore(Long uid) {
        List<TobbitMathScoreHistory> list = tobbitMathScoreHistoryDao.loadByUid(uid, 1);
        if (list.size() > 0) {
            return list.get(0).getTotalScore();
        }
        return 0;
    }

    private String today() {
        return DateUtils.getTodaySqlDate();
    }
}
