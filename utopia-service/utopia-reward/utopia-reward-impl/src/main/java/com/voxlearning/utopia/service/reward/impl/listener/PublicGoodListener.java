package com.voxlearning.utopia.service.reward.impl.listener;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.BooleanUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.reward.api.PublicGoodService;
import com.voxlearning.utopia.service.reward.api.enums.LikeSourceEnum;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.impl.dao.PublicGoodFeedDao;
import com.voxlearning.utopia.service.reward.impl.dao.RewardActivityRecordDao;
import com.voxlearning.utopia.service.reward.impl.internal.InternalPGRankService;
import com.voxlearning.utopia.service.reward.impl.loader.PublicGoodLoaderImpl;
import com.voxlearning.utopia.service.reward.mapper.PGDonateMsg;
import com.voxlearning.utopia.service.reward.mapper.PGParentChildRef;
import com.voxlearning.utopia.service.reward.mapper.PGRankEntry;
import com.voxlearning.utopia.service.user.api.AsyncUserService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.BooleanUtils.isTrue;
import static com.voxlearning.utopia.service.reward.api.PublicGoodLoader.RANK_LIMIT;

/**
 * 公益 - 消息处理
 */
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.reward.public.good.topic4"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.reward.public.good.topic4")
})
public class PublicGoodListener extends SpringContainerSupport implements MessageListener {

    /**
     * CBS CAS操作的最大尝试次数
     **/
    private static int MAX_TRY_TIMES = 5;

    @Inject private PublicGoodLoaderImpl publicGoodLoader;
    @Inject private PublicGoodService publicGoodService;
    @Inject private StudentLoaderClient stuLoaderCli;
    @Inject private TeacherLoaderClient tchLoaderCli;
    @Inject private UserLoaderClient usrLoaderCli;
    @Inject private RewardActivityRecordDao rewardActivityRecordDao;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private PublicGoodFeedDao feedDao;
    @Inject private ParentLoaderClient parentLoader;
    @Inject private InternalPGRankService rankService;
    @Inject private RaikouSDK raikouSDK;

    private IRedisCommands redisCommands;

    @ImportService(interfaceClass = AsyncUserService.class)
    private AsyncUserService asyncUserService;

    @Override
    public void afterPropertiesSet() {
        redisCommands = RedisCommandsBuilder.getInstance().getRedisCommands("user-easemob");
    }

    @Override
    public void onMessage(Message message) {
        String type = message.getType();
        switch (type) {
            case "donate":
                PGDonateMsg msg = (PGDonateMsg) message.decodeBody();
                dealDonateRecord(msg);
                break;
            default:
                break;
        }
    }

    private <V> Cache computeCacheIfAbsent(Cache cache, String key, Function<String, V> func) {
        CacheObject<V> result = cache.get(key);
        if (result == null) {
            cache.add(key, 0, func.apply(key));
        }

        return cache;
    }

    private void dealDonateRecord(PGDonateMsg msg) {
        if (msg == null)
            return;

        PublicGoodCollect collect = publicGoodLoader.loadCollectByUserId(msg.getUserId())
                .stream()
                .filter(pg -> Objects.equals(pg.getId(), msg.getCollectId()))
                .findFirst()
                .orElse(null);
        if (collect == null)
            return;

        Cache cache = RewardCache.getPersistent();
        Long activityId = collect.getActivityId();
        Long donateMoney = msg.getMoney();
        Long userId = msg.getUserId();
        Long schoolId;
        User user = usrLoaderCli.loadUser(userId);
        if (user == null)
            return;

        // 插入建造教室的里程碑事件
        if (msg.getFinish()) {
            insertRewardMilestones(msg, user);
        }

        // 维护家长孩子的关系表
        if (user.isStudent() && BooleanUtils.isTrue(msg.getFinish())) {
            parentLoader.loadStudentParents(user.getId()).forEach(sp -> {
                Long parentId = sp.getParentUser().getId();
                String key = CacheKeyGenerator.generateCacheKey(
                        "PulbicGoodParentChildRef",
                        new String[]{"parentId"},
                        new Object[]{sp.getParentUser().getId()});

                CacheObject<PGParentChildRef> pcRefCO = cache.get(key);
                if (pcRefCO.getValue() == null) {
                    PGParentChildRef pcRef = new PGParentChildRef();
                    pcRef.setParentId(parentId);
                    pcRef.addFinishCollectId(user.getId(), collect.getId());

                    cache.add(key, 0, pcRef);
                } else {
                    cache.cas(key, 0, pcRefCO, org -> {
                        org.addFinishCollectId(user.getId(), collect.getId());
                        return org;
                    });
                }
            });
        }

        String schoolName;
        School school = asyncUserService.loadUserSchool(user).getUninterruptibly();
        if (school == null)
            return;
        else {
            schoolId = school.getId();
            schoolName = school.getShortName();
        }

        final List<Long> clazzIds = new ArrayList<>();
        if (user.isStudent()) {
            Optional.ofNullable(stuLoaderCli.loadStudentDetail(userId)).map(StudentDetail::getClazzId).ifPresent(clazzIds::add);
        } else if (user.isTeacher()) {
            clazzIds.addAll(tchLoaderCli.loadTeacherClazzIds(userId));
        }

        if (CollectionUtils.isEmpty(clazzIds))
            return;

        AtomicLong finishNum = new AtomicLong();
        // 全校排行
        for (Long clazzId : clazzIds) {
            PGRankEntry mock = new PGRankEntry();
            mock.setClazzId(clazzId);
            mock.setSchoolId(schoolId);
            mock.setMoney(0L);

            // 过滤掉毕业班
            String clazzName = Optional.ofNullable(raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId))
                    .filter(c -> !c.isTerminalClazz())
                    .map(Clazz::formalizeClazzName)
                    .orElse(null);
            if (clazzName == null)
                continue;
            else
                mock.setName(clazzName);

            String schoolRankKey = CacheKeyGenerator.generateCacheKey(
                    "PublicGoodRank:school",
                    new String[]{"activityId", "schoolId"},
                    new Object[]{activityId, schoolId});

            boolean update;
            CacheObject<Map<Long, PGRankEntry>> result = cache.get(schoolRankKey);
            if (result.getValue() == null) {
                Map<Long, PGRankEntry> initMap = new HashMap<>();
                mock.addMoney(donateMoney);
                initMap.put(clazzId, mock);
                update = !cache.add(schoolRankKey, 0, initMap);
            } else {
                update = true;
            }

            if (update) {
                cache.cas(schoolRankKey, 0, result, MAX_TRY_TIMES, entryMap -> {
                    PGRankEntry entry = entryMap.computeIfAbsent(clazzId, cId -> mock);
                    entry.addMoney(donateMoney);
                    if (isTrue(msg.getFinish())) {
                        entry.addFinishNum(1L);
                    }

                    entryMap.put(clazzId, entry);
                    // 汇总所有班的完成人数
                    long fNum = entryMap.values().stream().mapToLong(e -> SafeConverter.toLong(e.getFinishNum())).sum();
                    finishNum.set(fNum);

                    return entryMap;
                });
            }
        }

        // 全国排行
        PGRankEntry mock = new PGRankEntry();
        mock.setSchoolId(schoolId);
        mock.setMoney(0L);
        mock.setName(schoolName);

        RedisSortedSetCommands<String, Object> ssCmd = redisCommands.sync().getRedisSortedSetCommands();
        RedisStringCommands<String, Object> strCmd = redisCommands.sync().getRedisStringCommands();
        RedisKeyCommands<String, Object> keyCmd = redisCommands.sync().getRedisKeyCommands();

        String cacheKey = "PublicGoodRank:nation:activityId:" + collect.getActivityId();
        String schoolKey = cacheKey + ":school:" + schoolId;

        // 如果全国榜排序的redis数据是空的，则从数据库里面reload
        if (keyCmd.exists(cacheKey) == 0 && !RuntimeMode.isStaging()) {
            try {
                MapMessage restoreResult = AtomicCallbackBuilderFactory.getInstance()
                        .<MapMessage>newBuilder()
                        .keyPrefix("PG:RestoreRankToRedis")
                        .keys(activityId)
                        .callback(() -> publicGoodService.restoreRank(activityId))
                        .build()
                        .execute();

                if (!restoreResult.isSuccess()) {
                    logger.error("PG:restore rank to redis failed!");
                }
            } catch (CannotAcquireLockException e) {
                // Nothing happen
            }
        }

        // 如果值为空，则从数据库里面查，如果还是空说明是新建的
        if (keyCmd.exists(schoolKey) == 0) {
            Optional.ofNullable(rankService.loadNationRankEntry(schoolId)).ifPresent(e -> {
                strCmd.incrby(schoolKey, e.getMoney());
            });
        }

        // 累积
        Long newMoney = strCmd.incrby(schoolKey, donateMoney);
        // staging和线儿上是一个库，但redis不同。担心放开staging会干扰正式库的数据。
        if (!RuntimeMode.isStaging()) {
            // 把记录在库里面一份儿
            PublicGoodNationRank rankEntry = new PublicGoodNationRank();
            rankEntry.setId(schoolId);
            rankEntry.setMoney(newMoney);
            rankService.saveNationRankEntry(rankEntry);
        }

        ssCmd.zadd(cacheKey, newMoney, schoolId);
        Long rank = ssCmd.zrevrank(cacheKey, schoolId) + 1;
        if (rank > RANK_LIMIT)
            return;

        String nationRankKey = CacheKeyGenerator.generateCacheKey(
                "PublicGoodRank:nation",
                new String[]{"activityId"},
                new Object[]{activityId});

        boolean update;
        CacheObject<Map<Long, PGRankEntry>> result = cache.get(nationRankKey);
        if (result.getValue() == null) {
            Map<Long, PGRankEntry> initMap = new HashMap<>();
            mock.setMoney(donateMoney);

            if (isTrue(msg.getFinish()))
                mock.setFinishNum(1L);

            initMap.put(schoolId, mock);
            // 如果并发增加失败，则尝试更新
            update = !cache.add(nationRankKey, 0, initMap);
        } else {
            update = true;
        }

        if (update) {
            cache.cas(nationRankKey, 0, result, MAX_TRY_TIMES, rankMap -> {
                if (rankMap == null)
                    rankMap = new HashMap<>();

                PGRankEntry entry = rankMap.computeIfAbsent(schoolId, k -> mock);
                // 记录有可能被挤出100名外，用这里的不准，要用redis的
                // entry.addMoney(donateMoney);
                entry.setMoney(newMoney);
                // 同上，这样加不准
                //if (isTrue(msg.getFinish()))
                //    entry.addFinishNum(1L);
                entry.setFinishNum(finishNum.get());

                List<PGRankEntry> rankList = new ArrayList<>(rankMap.values());
                if (rankList.size() > RANK_LIMIT) {
                    rankList.sort((r1, r2) -> Long.compare(r2.getMoney(), r1.getMoney()));
                    rankList = rankList.subList(0, RANK_LIMIT);
                }

                return rankList.stream().collect(Collectors.toMap(r -> r.getSchoolId(), v -> v));
            });
        }

    }

    private void insertRewardMilestones(PGDonateMsg msg, User user) {
        try {
            PublicGoodCollect collect = publicGoodLoader.loadCollectById(msg.getUserId(), msg.getCollectId());
            PublicGoodStyle goodStyle = publicGoodLoader.loadStyleById(collect.getStyleId());

            // 捐赠记录 插入里程碑事件
            RewardActivityRecord milestonesRecord = new RewardActivityRecord();
            milestonesRecord.setActivityId(collect.getActivityId());
            milestonesRecord.setCollectId(collect.getId());
            milestonesRecord.setPrice(0d);
            milestonesRecord.setUserId(user.getId());
            milestonesRecord.setUserName(user.getProfile().getRealname());
            milestonesRecord.setType(RewardActivityRecord.MILESTONES);
            milestonesRecord.setComment(String.format("成功建造了“%s”", goodStyle.getName()));
            rewardActivityRecordDao.insert(milestonesRecord);

            // 插入到老师的动态中
            if (user.isStudent()) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
                if (studentDetail == null || studentDetail.getClazzId() == null) {
                    return;
                }
                List<ClazzTeacher> clazzTeachers = teacherLoaderClient.loadClazzTeachers(studentDetail.getClazzId());
                for (ClazzTeacher clazzTeacher : clazzTeachers) {
                    Long id = clazzTeacher.getTeacher().getId();

                    PublicGoodFeed feed = new PublicGoodFeed();
                    feed.setActivityId(collect.getActivityId());
                    feed.setUserId(id);
                    feed.setOpId(user.getId());
                    feed.setOpName(user.getProfile().getRealname());
                    feed.setSourceEnum(LikeSourceEnum.A17);
                    feed.setType(PublicGoodFeed.Type.MILESTONES);
                    feed.setComments(String.format("%s成功建造了“%s”", user.getProfile().getRealname(), goodStyle.getName()));
                    feedDao.insert(feed);
                    publicGoodService.addFeedRedis(collect.getActivityId(), id);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}
