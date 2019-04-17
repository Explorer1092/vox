package com.voxlearning.utopia.service.reward.impl.service;

import com.lambdaworks.redis.ScriptOutputType;
import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.reward.api.PublicGoodService;
import com.voxlearning.utopia.service.reward.api.enums.LikeSourceEnum;
import com.voxlearning.utopia.service.reward.api.mapper.CacheCollectMapper;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.impl.dao.PublicGoodCollectDao;
import com.voxlearning.utopia.service.reward.impl.dao.PublicGoodFeedDao;
import com.voxlearning.utopia.service.reward.impl.dao.PublicGoodRewardDao;
import com.voxlearning.utopia.service.reward.impl.dao.PublicGoodUserActivityDao;
import com.voxlearning.utopia.service.reward.impl.internal.InternalPGRankService;
import com.voxlearning.utopia.service.reward.impl.loader.PublicGoodLoaderImpl;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import com.voxlearning.utopia.service.reward.mapper.PGDonateMsg;
import com.voxlearning.utopia.service.reward.util.CacheKeyUtils;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupClazzMapper;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherSystemClazzServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Named
@Slf4j
@ExposeService(interfaceClass = PublicGoodService.class)
public class PublicGoodServiceImpl implements PublicGoodService, InitializingBean {

    /**
     * 日志
     **/
    private static Logger logger = LoggerFactory.getLogger(PublicGoodServiceImpl.class);

    @Inject private PublicGoodCollectDao collectDao;
    @Inject private PublicGoodFeedDao feedDao;
    @Inject private PublicGoodUserActivityDao userActivityDao;
    @Inject private PublicGoodRewardDao rewardDao;

    @Inject private RewardLoaderImpl rewardLoader;
    @Inject private PublicGoodLoaderImpl pgLoader;
    @Inject private RewardServiceImpl rewardService;
    @Inject private TeacherSystemClazzServiceClient teacherSystemClazzServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;
    @Inject private PrivilegeServiceClient privilegeServiceClient;
    @Inject private InternalPGRankService rankService;

    private IRedisCommands redisCommands;

    @AlpsPubsubPublisher(topic = "utopia.reward.public.good.topic4")
    private MessagePublisher messagePublisher;

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        redisCommands = builder.getRedisCommands("user-easemob");
    }

    @Override
    public MapMessage upsertCollect(PublicGoodCollect collect) {
        try {
            Long userId = collect.getUserId();
            Validate.isTrue(userId != null && userId > 0, "用户ID为空!");
            Validate.isTrue(collect.getStatus() != null, "状态为空!");

            RewardActivity activity = rewardLoader.loadRewardActivity(collect.getActivityId());
            Validate.isTrue(activity != null, "活动不存在!");

            PublicGoodStyle style = pgLoader.loadStyleByModel(activity.getModel())
                    .stream()
                    .filter(s -> Objects.equals(s.getId(), collect.getStyleId()))
                    .findFirst()
                    .orElse(null);
            Validate.notNull(style, "样式不存在!");

            boolean isNew = false;
            // 如果是新建的情况
            if (collect.getId() == null) {
                isNew = true;
                PublicGoodCollect existCollect = pgLoader.loadCollectByUserId(userId)
                        .stream()
                        .filter(c -> Objects.equals(c.getActivityId(), collect.getActivityId()))
                        .filter(c -> Objects.equals(c.getStyleId(), collect.getStyleId()))
                        .findFirst()
                        .orElse(null);
                Validate.isTrue(existCollect == null, "不能重复创建!");
            }

            MapMessage resultMsg = MapMessage.successMessage();

            PublicGoodCollect modified = collectDao.upsertCollect(collect);

            if (isNew) {
                PublicGoodUserActivity userActivity = pgLoader.loadUserActivityByUserId(collect.getActivityId(), collect.getUserId());
                if (userActivity == null) {
                    userActivity = new PublicGoodUserActivity();
                    userActivity.setActivityId(collect.getActivityId());
                    userActivity.setUserId(collect.getUserId());
                    userActivityDao.upsertUserActivity(userActivity);
                }
                // 班级建设情况持久化到缓存 更新时会在累计学豆后会重新保存
                persistenceClazzCollectToRedis(modified, SafeConverter.toLong(userActivity.getMoneyNum()), false);
            }
            return resultMsg.add("collect", modified);
        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("PG:Upsert collect error!", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage addCollectLike(PublicGoodCollect collect, PublicGoodFeed feed) {
        try {
            Long activityId = collect.getActivityId();
            if (feed.getSourceEnum() == LikeSourceEnum.A17) {
                // 点赞者的参与记录
                PublicGoodUserActivity userActivities = userActivityDao.loadByUserId(feed.getOpId())
                        .stream()
                        .filter(i -> Objects.equals(collect.getActivityId(), i.getActivityId()))
                        .findFirst()
                        .orElse(null);

                Set<Long> likedUserId = new HashSet<>();
                if (userActivities != null) {
                    if (userActivities.getLikedUser() != null) {
                        likedUserId = userActivities.getLikedUser();
                    }
                    Set<Long> oldRedisCache = pgLoader.loadLikedCollect(activityId, feed.getOpId());
                    likedUserId.addAll(oldRedisCache);

                    if (likedUserId.contains(collect.getUserId())) {
                        return MapMessage.errorMessage("你已经给他点过赞了,不可重复操作");
                    }
                }

                feedDao.upsertFeed(feed);

                if (userActivities != null) {
                    likedUserId.add(collect.getUserId());
                    userActivityDao.addLikedUser(userActivities, likedUserId); // 把 redis 的数据同步到 mongo
                }
            } else {
                String sourceName = feed.getSourceEnum().name();
                String cacheKey = CacheKeyUtils.genLikeKey(activityId, collect.getUserId(), sourceName);
                redisCommands.sync().getRedisStringCommands().incr(cacheKey);
            }

            PublicGoodUserActivity userActivity = pgLoader.loadUserActivityByUserId(collect.getActivityId(), collect.getUserId());
            userActivityDao.addLike(userActivity);
            addFeedRedis(activityId, collect.getUserId());
            return MapMessage.successMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage addUserActivityMoney(PublicGoodCollect collect, Long money) {
        try {
            PublicGoodUserActivity userActivity = pgLoader.loadUserActivityByUserId(collect.getActivityId(), collect.getUserId());
            PublicGoodUserActivity userActivityEd = userActivityDao.addMoney(userActivity, money);

            persistenceClazzCollectToRedis(collect, userActivityEd.getMoneyNum(), !Objects.equals(PublicGoodCollect.Status.ONGOING, collect.getStatus()));

            return MapMessage.successMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }


    public void persistenceClazzCollectToRedis(PublicGoodCollect collect, Boolean done) {
        PublicGoodUserActivity userActivity = pgLoader.loadUserActivityByUserId(collect.getActivityId(), collect.getUserId());
        if (userActivity != null) {
            persistenceClazzCollectToRedis(collect, SafeConverter.toLong(userActivity.getMoneyNum()), done);
        } else {
            log.warn("collectId:{} not fount vox_public_good_user_activity_ !!!!!!", collect.getId());
        }
    }


    /**
     * 班级建设情况持久化到缓存
     *
     * @param collect
     * @param money
     * @param done
     */
    private void persistenceClazzCollectToRedis(PublicGoodCollect collect, Long money, Boolean done) {
        CacheCollectMapper cacheCollectMapper = new CacheCollectMapper();
        cacheCollectMapper.setCollectId(collect.getId());
        cacheCollectMapper.setStyleId(collect.getStyleId());
        cacheCollectMapper.setActivityId(collect.getActivityId());
        cacheCollectMapper.setUserId(collect.getUserId());
        cacheCollectMapper.setMoney(money);
        cacheCollectMapper.setEnableCode(collect.getEnabledCode());
        cacheCollectMapper.setDone(done);
        cacheCollectMapper.setUpdateTime(new Date());

        User user = userLoaderClient.loadUser(collect.getUserId());
        if (user.fetchUserType() == UserType.TEACHER) {
            cacheCollectMapper.setType(0);

            // 获取包班制子账号班级
            List<Long> allTeacherId = teacherLoaderClient.loadSubTeacherIds(user.getId());
            allTeacherId.add(user.getId());

            Set<Long> clazzIdSet = new HashSet<>();
            for (Long teacherId : allTeacherId) {
                clazzIdSet.addAll(teacherSystemClazzServiceClient.loadTeacherAllGroupsData(teacherId)
                        .stream()
                        .filter(i -> Objects.equals(ClazzType.PUBLIC.name(), i.getClazzType()))
                        .map(GroupClazzMapper::getClazzId)
                        .collect(Collectors.toSet()));
            }

            for (Long clazzId : clazzIdSet) {
                redisCommands.sync().getRedisHashCommands().hset(CacheKeyUtils.genClassCollectKey(collect.getActivityId(), clazzId), collect.getId(), cacheCollectMapper);
            }
        } else if (user.fetchUserType() == UserType.STUDENT) {
            cacheCollectMapper.setType(1);
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
            if (studentDetail.getClazzId() != null) {
                redisCommands.sync().getRedisHashCommands().hset(CacheKeyUtils.genClassCollectKey(collect.getActivityId(), SafeConverter.toLong(studentDetail.getClazzId())), collect.getId(), cacheCollectMapper);
            }
        }
    }

    @Override
    public MapMessage collectThirdPartyLike(Long activityId, Long userId) {
        try {
            for (LikeSourceEnum likeSourceEnum : LikeSourceEnum.values()) {
                String sourceName = likeSourceEnum.name();
                String cacheKey = CacheKeyUtils.genLikeKey(activityId, userId, sourceName);

                // redisCommands.sync().getRedisStringCommands().getset() 方法有坑,  incr 存的是未序列化的 string, getset 存的是序列后的值, 后导致下次 incr 失败

                Object getResult = redisCommands.sync().getRedisScriptingCommands()
                        .eval("local va = redis.call('get',KEYS[1]) redis.call('del',KEYS[1]) return va", ScriptOutputType.VALUE, cacheKey);
                if (getResult != null) {
                    long count = SafeConverter.toLong(getResult);
                    if (count > 0) {
                        PublicGoodFeed publicGoodFeed = new PublicGoodFeed();
                        publicGoodFeed.setActivityId(activityId);
                        publicGoodFeed.setUserId(userId);
                        publicGoodFeed.setOpId(0l);
                        publicGoodFeed.setOpName("系统汇总");
                        publicGoodFeed.setType(PublicGoodFeed.Type.LIKE);
                        publicGoodFeed.setSourceEnum(likeSourceEnum);
                        publicGoodFeed.setCount(count);
                        feedDao.upsertFeed(publicGoodFeed);
                    }
                }
            }
            return MapMessage.successMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }

    }

    @Override
    public MapMessage upsertFeed(PublicGoodFeed feed) {
        try {
            PublicGoodFeed publicGoodFeed = feedDao.upsertFeed(feed);
            return MapMessage.successMessage().add("feed", publicGoodFeed);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 处理奖励
     *
     * @param collect
     */
    private List<PublicGoodReward> dealWithReward(PublicGoodCollect collect, String model) {

        Long activityId = collect.getActivityId();
        Long userId = collect.getUserId();
        User user = userLoaderClient.loadUser(userId);

        // 表达式用到的参数
        // 处理奖励时，完成状态的collect还未入库。这里手工累积上
        int finishNum = (int) pgLoader.loadUserCollectByActId(userId, activityId)
                .stream()
                .filter(c -> c.isFinished())
                .count() + 1;

        JexlEngine jexlEngine = new JexlEngine();
        return pgLoader.loadRewardByModel(model)
                .stream()
                .filter(reward -> {
                    JexlContext jc = new MapContext();
                    Boolean isPrimaryStudent = false;
                    if (user.isStudent()) {
                        StudentDetail studentDetail = (StudentDetail) user;
                        isPrimaryStudent = studentDetail.isPrimaryStudent();
                    }

                    jc.set("finishNum", finishNum);
                    jc.set("result", false);
                    jc.set("userType", user.getUserType());
                    jc.set("primaryStudent", isPrimaryStudent);

                    Expression e = jexlEngine.createExpression(reward.getExpression());
                    e.evaluate(jc);

                    boolean received = SafeConverter.toBoolean(jc.get("result"));
                    if (!received) return false;

                    boolean enabledFiled = ofNullable(reward.getExtAttr()).map(m -> m.containsKey("enable")).orElse(false);
                    PublicGoodCollect.Reward addedReward = collect.addReward(reward.getId(), 1, enabledFiled);

                    // 如果是头饰的话，则发给用户
                    if ("HEAD_WEAR".equals(reward.getType())) {
                        String headWearCode = SafeConverter.toString(reward.getExtAttrValue("headWearCode"));
                        Privilege privilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(headWearCode);

                        // 永久的没有时间限制
                        if (privilege != null) {
                            privilegeServiceClient.getPrivilegeService().grantPrivilege(userId, privilege);
                        }
                    } else if ("KEY".equals(reward.getType())) {
                        String letterUrl = Optional.ofNullable(reward.getExtAttrValue("letterUrls"))
                                .map(v -> (List<String>) v)
                                .filter(ls -> ls.size() > 0)
                                .map(urls -> urls.get(RandomUtils.nextInt(urls.size())))
                                .orElse(null);

                        if (letterUrl != null)
                            addedReward.setAttrVal("letterUrl", letterUrl);
                    }

                    return true;
                })
                .collect(toList());
    }

    private int getLeftCollectNum(Long userId, Long activityId, String model) {
        int finishNum = (int) pgLoader.loadUserCollectByActId(userId, activityId)
                .stream()
                .filter(c -> c.isFinished())
                .count();

        int totalNum = pgLoader.loadStyleByModel(model).size();
        return totalNum - finishNum;
    }

    @Override
    public MapMessage donate(String typeCode, RewardActivityRecord record) {
        if (record == null)
            return MapMessage.errorMessage("记录不存在!");

        // 处理奖励
        RewardActivity activity = rewardLoader.loadRewardActivity(record.getActivityId());
        if (activity == null) {
            return MapMessage.errorMessage("活动不存在!");
        }

        MapMessage resultMsg = rewardService.createPublicGoodActivityRecord(record);
        if (!resultMsg.isSuccess()) {
            return resultMsg;
        }

        Long userId = record.getUserId();

        PublicGoodCollect collect = pgLoader.loadCollectById(userId, record.getCollectId());
        if (collect == null) {
            return MapMessage.errorMessage("Collect is not exist!");
        }

        List<PublicGoodElementType> elementTypes = pgLoader.loadElementTypeByStyleId(collect.getStyleId());
        PublicGoodElementType enableEle = elementTypes.stream()
                .filter(e -> Objects.equals(e.getCode(), typeCode))
                .findFirst()
                .orElse(null);
        if (enableEle == null)
            return MapMessage.errorMessage("Element type is not exists!");

        // 后端先点亮
        PublicGoodCollect enableCollect = collect.putElementValueOf(enableEle);
        // 设置完成状态
        // 如果刚好是完成状态则记录下
        if (collect.getEnabledEleNum() >= elementTypes.size() && !collect.isFinished()) {
            collect.finish();
            resultMsg.add("finish", true);

            // 如果教室完成，处理奖励
            resultMsg.add("rewards", dealWithReward(collect, activity.getModel()));
            resultMsg.add("leftNum", getLeftCollectNum(userId, activity.getId(), activity.getModel()) - 1);
        }

        resultMsg.putAll(upsertCollect(enableCollect));
        if (!resultMsg.isSuccess()) {
            return resultMsg;
        }

        //累加捐赠金额
        addUserActivityMoney(collect, SafeConverter.toLong(enableEle.getPrice()));

        // 发消息，异步处理剩余耗时操作
        Message message = Message.newMessage();
        message.setType("donate");

        PGDonateMsg msg = new PGDonateMsg();
        msg.setCollectId(record.getCollectId());
        msg.setMoney(record.getPrice().longValue());
        msg.setFinish(SafeConverter.toBoolean(resultMsg.get("finish")));
        msg.setUserId(record.getUserId());

        message.writeObject(msg);
        messagePublisher.publish(message);

        return resultMsg.add("enableElement", enableEle);
    }

    @Override
    public void addFeedRedis(Long activityId, Long userId) {
        redisCommands.sync().getRedisStringCommands().setbit(CacheKeyUtils.genFeedKey(activityId), userId, 1);
    }

    @Override
    public MapMessage useKey(Long userId, Long activityId) {
        RewardActivity activity = rewardLoader.loadRewardActivity(activityId);
        if (activity == null)
            return MapMessage.errorMessage("活动不存在!");

        AtomicReference<PublicGoodReward> letterRewardRef = new AtomicReference<>();
        Map<Long, PublicGoodReward> rewardMap = pgLoader.loadRewardByModel(activity.getModel())
                .stream()
                .peek(r -> {
                    if (Objects.equals("LETTER", r.getType()))
                        letterRewardRef.set(r);
                })
                .collect(toMap(k -> k.getId(), v -> v));

        PublicGoodReward letterReward = letterRewardRef.get();
        if (letterReward == null)
            return MapMessage.errorMessage("感谢信的配置丢失!");

        List<PublicGoodCollect> collectList = pgLoader.loadUserCollectByActId(userId, activityId)
                .stream()
                .filter(c -> c.isFinished())
                .collect(toList());

        OUT:
        for (PublicGoodCollect collect : collectList) {
            if (CollectionUtils.isEmpty(collect.getRewardList())) {
                logger.warn("PG:The rewards of a finished collect is empty!id:{}", collect.getId());
                continue;
            }

            PublicGoodCollect.Reward reward;
            String rewardType;
            // 遍历所有教室找到钥匙奖励
            for (Iterator<PublicGoodCollect.Reward> iter = collect.getRewardList().iterator(); iter.hasNext(); ) {
                reward = iter.next();
                rewardType = Optional.ofNullable(rewardMap.get(reward.getId())).map(r -> r.getType()).orElse("");

                if (!Objects.equals(rewardType, "KEY"))
                    continue;

                // 移掉钥匙，添加感谢信
                iter.remove();

                PublicGoodCollect.Reward addedReward = collect.addReward(letterReward.getId(), 1, false);
                addedReward.setAttrVal("letterUrl", reward.getAttrVal("letterUrl"));

                MapMessage resultMsg = upsertCollect(collect);
                if (!resultMsg.isSuccess())
                    return resultMsg;

                break OUT;
            }
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage upsertUserActivity(PublicGoodUserActivity userActivity) {
        try {
            userActivityDao.upsertUserActivity(userActivity);
            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error("PG:Upsert user activity error!", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage updateReward(PublicGoodReward reward) {
        try {
            rewardDao.upsert(reward);
            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error("PG:Update reward error!", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage persistRank() {
        return rankService.persistRank();
    }

    @Override
    public MapMessage restoreRank(Long activityId) {
        return rankService.restoreRank(activityId);
    }

    @Override
    public MapMessage moveLikeToMongo(Integer start, Integer end) {
        if (start == null) start = 0;
        if (end == null) end = 100;

        // 查询所有分表
        for (int i = start; i < end; i++) {
            try {
                List<PublicGoodUserActivity> shareTable = userActivityDao.findShareTable((long) i);
                for (PublicGoodUserActivity itemActivity : shareTable) {
                    Long userId = itemActivity.getUserId();
                    Long activityId = itemActivity.getActivityId();

                    // 防止初期 staging 测试时有异常数据
                    if (userId == null || activityId == null) {
                        continue;
                    }

                    Map<String, Object> redisLiked = redisCommands.sync().getRedisHashCommands().hgetall(CacheKeyUtils.genLikedKey(activityId, userId));

                    if (redisLiked != null) {
                        Set<Long> userIdSet = new HashSet<>();
                        for (String s : redisLiked.keySet()) {
                            userIdSet.add(Long.valueOf(s));
                        }

                        itemActivity.setLikedUser(userIdSet);
                        userActivityDao.addLikedUser(itemActivity, userIdSet);
                    }

                }
            } catch (Exception e) {
                log.warn("error: current progress is " + i + "/" + end, e);
            }
            log.warn("public good move liked data progress is {}/{}", i, end);
        }
        return MapMessage.successMessage();
    }
}
