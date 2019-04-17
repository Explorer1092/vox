package com.voxlearning.utopia.service.business.impl.listener.handler;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask.Status;
import com.voxlearning.utopia.service.business.impl.listener.TeacherHomeworkMsgHandler;
import com.voxlearning.utopia.service.business.impl.loader.TeachingResourceLoaderImpl;
import com.voxlearning.utopia.service.business.impl.service.TeachingResourceServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask.*;

/**
 * 教学资源任务处理
 * Created by haitian.gan on 2017/8/8.
 */
@Named
public class TeacherResourceTaskMsgHandler implements TeacherHomeworkMsgHandler, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TeacherResourceTaskMsgHandler.class);

    private static final String KEY_PREFIX = "teacherTask:";
    private static final String KEY_PART_HOMEWORK = "homework";
    private static final String KEY_PART_ALLHW = "homeworks";
    private static final String KEY_FIELD_GROUPID = "groupId";
    private static final String KEY_FIELD_FINISH_NUM = "finishNum";

    private static final int MIN_ASSIGN_NUM = 3;
    private static final int CACHE_EXPIRE_DAY = 30;

    @Inject private BusinessCacheSystem cacheSystem;
    @Inject private GroupLoaderClient groupLoader;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeachingResourceLoaderImpl teachingResourceLoader;
    @Inject private TeachingResourceServiceImpl teachingResourceService;

    @Inject private RaikouSDK raikouSDK;

    private final List<String> RELATE_TASKS = new ArrayList<>();
    private final List<String> ASSIGN_TASKS = new ArrayList<>();

    {
        RELATE_TASKS.add(PRO_SURVIVAL_1.name());
        RELATE_TASKS.add(PRO_SURVIVAL_2.name());
        RELATE_TASKS.add(PRO_SURVIVAL_3.name());
        RELATE_TASKS.add(PRO_SURVIVAL_4.name());
        RELATE_TASKS.add(PRO_SURVIVAL_5.name());
        RELATE_TASKS.add(PRO_SURVIVAL_6.name());
        RELATE_TASKS.add(PRO_SURVIVAL_7.name());

        ASSIGN_TASKS.add(ASSIGN_WINTERVACATION.name());
        ASSIGN_TASKS.add(ASSIGN_BASICREVIEW_TERMREVIEW.name());
    }

    public void handle(Map<String, Object> msgMap) {
        String homeworkId = MapUtils.getString(msgMap, "homeworkId");
        Long teacherId = MapUtils.getLong(msgMap, "teacherId");
        Long groupId = MapUtils.getLong(msgMap, "groupId");
        String homeworkType = MapUtils.getString(msgMap, "homeworkType");

        String messageType = MapUtils.getString(msgMap, "messageType");
        switch (messageType) {
            case "assign": {
                processAssignHomeworkAction(teacherId, groupId, homeworkId);
                processAssignFinalWork(teacherId, homeworkType);// 处理寒假作业、期末复习
                break;
            }
            case "checked": {

                int finishNum = MapUtils.getInteger(msgMap, "finishCount");
                Long createAt = MapUtils.getLong(msgMap, "createAt");

                processCheckHomeworkAction(teacherId, groupId, homeworkId, createAt, finishNum);
                break;
            }
        }

    }

    /**
     * 获得由一组任务决定的缓存时间
     *
     * @param tasks
     * @return
     */
    private int calExpirationSeconds(List<TeacherResourceTask> tasks) {
        TeacherResourceTask latestTask = Optional.ofNullable(tasks)
                .orElse(Collections.emptyList())
                .stream()
                .max(Comparator.comparing(TeacherResourceTask::getExpiryDate))
                .orElse(null);

        // 为了查数据方便，把失效日期延迟至一个月。从创建时间算起
        Date latestExpiryTime;
        if (latestTask != null)
            latestExpiryTime = DateUtils.addDays(latestTask.getCreateAt(), CACHE_EXPIRE_DAY);
        else
            latestExpiryTime = DateUtils.addDays(new Date(), 7);

        // 过期时间， epoch seconds
        return (int) Instant.ofEpochMilli(latestExpiryTime.getTime()).getEpochSecond();
    }

    /**
     * 处理布置作业的逻辑，初始化作业的缓存数据。
     *
     * @param teacherId
     * @param groupId
     * @param homeworkId
     */
    private void processAssignHomeworkAction(Long teacherId, Long groupId, String homeworkId) {
        Date now = new Date();

        // 考虑包班制的情况，副账号查询主账号
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId == null || mainTeacherId == 0L)
            mainTeacherId = teacherId;

        //logger.info("TeacherTaskMsg1: teacherId:{},groupId:{},hwId:{}",teacherId,groupId,homeworkId);

        // 没有任务直接跳过
        // 副账号的任务也挂到了主账号上
        List<TeacherResourceTask> tasks = teachingResourceLoader.loadTeacherTasks(mainTeacherId)
                .stream()
                .filter(t -> Objects.equals(t.getStatus(), Status.ONGOING.name()))
                .filter(t -> t.getExpiryDate() != null && t.getExpiryDate().after(now))
                .filter(t -> RELATE_TASKS.contains(t.getTask()))
                .collect(Collectors.toList());

        if (tasks.size() == 0) {
            return;
        }

        //logger.info("TeacherTaskMsg2: taskNum:{},hwId:{}",tasks.size(),homeworkId);
        // 获得领取的任务中，最晚任务的过期时间
        // 过期时间， epoch seconds
        int expireSeconds = calExpirationSeconds(tasks);

        //String groupsKey = buildKey(teacherId, "groups");
        String allHomeworkKey = buildKey(teacherId, KEY_PART_ALLHW);
        String homeworkKey = buildKey(KEY_PART_HOMEWORK, homeworkId);

        //RedisSetCommands<String, Object> setCmd = redisCommands.sync().getRedisSetCommands();
        //RedisHashCommands<String, Object> hashCmd = redisCommands.sync().getRedisHashCommands();
        //RedisKeyCommands<String,Object> keyCmd = redisCommands.sync().getRedisKeyCommands();

        Cache cache = cacheSystem.CBS.persistence;

        boolean update;
        CacheObject<Set<String>> allHwCacheObj = cache.get(allHomeworkKey);
        if (allHwCacheObj.getValue() == null) {
            Set<String> allHwKeysSet = new HashSet<>();
            allHwKeysSet.add(homeworkKey);

            update = !cache.add(allHomeworkKey, expireSeconds, allHwKeysSet);
        } else
            update = true;

        if (update) {
            cache.cas(allHomeworkKey, expireSeconds, allHwCacheObj, keys -> {
                keys.add(homeworkKey);
                return keys;
            });
        }

        //setCmd.sadd(allHomeworkKey, homeworkKey);
        // 作业列表的失效期，即是最晚的任务失效的时间
        //keyCmd.expireat(allHomeworkKey,latestExpiryTime);

        // 记录作业的所属组，所属老师，创建时间，以及完成人数
        /*hashCmd.hmset(homeworkKey, MapUtils.m(
                "teacherId", teacherId,
                "groupId", groupId,
                "createTime", now.getTime())
        );*/

        Map<String, Object> hwData = MapUtils.m(
                "teacherId", teacherId,
                "groupId", groupId,
                "createTime", now.getTime(),
                KEY_FIELD_FINISH_NUM, 0);

        cache.set(homeworkKey, expireSeconds, hwData);

        // 初始化完成作业的filed
        //hashCmd.hincrby(homeworkKey, KEY_FIELD_FINISH_NUM, 0);

        //keyCmd.expireat(homeworkKey,latestExpiryTime);
    }

    /**
     * 检查作业的任务处理
     *
     * @param teacherId
     * @param groupId
     * @param homeworkId
     * @param createAt
     * @param finishNum
     */
    private void processCheckHomeworkAction(Long teacherId,
                                            Long groupId,
                                            String homeworkId,
                                            Long createAt,
                                            Integer finishNum) {

        Cache cache = cacheSystem.CBS.persistence;
        Date now = new Date();
        //取出homeworkId对应的groupId和teacherId信息
        //RedisHashCommands<String, Object> hashCommands = redisCommands.sync().getRedisHashCommands();
        //RedisSetCommands<String, Object> setCommands = redisCommands.sync().getRedisSetCommands();
        //RedisKeyCommands<String,Object> keyCommands = redisCommands.sync().getRedisKeyCommands();

        String homeworkKey = buildKey(KEY_PART_HOMEWORK, homeworkId);
        // 获得作业信息
        // Map<String, Object> homeworkMap = hashCommands.hgetall(homeworkKey);
        Map<String, Object> homeworkMap = cache.load(homeworkKey);

        // hash命令取不存在的key，返回的不是null，而是空的map(坑)
        if (MapUtils.isEmpty(homeworkMap))
            return;

        Long homeworkCreateTime = MapUtils.getLong(homeworkMap, "createTime");

        // 考虑包班制的情况，副账号查询主账号
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId == null || mainTeacherId == 0L)
            mainTeacherId = teacherId;

        if (homeworkCreateTime == null) {
            // logger.error("教学资源检查作业任务处理异常，teacherId:{},groupId:{}", teacherId, groupId);
            // return;
            // 缓存取不到，从消息里面拿
            homeworkCreateTime = createAt;
        }

        // 确定最终作业时间
        long finalHwCreateTime = homeworkCreateTime;

        // 筛出来作业处于有效期的任务
        List<TeacherResourceTask> tasks = teachingResourceLoader.loadTeacherTasks(mainTeacherId)
                .stream()
                .filter(t -> Objects.equals(t.getStatus(), Status.ONGOING.name()))
                .filter(t -> RELATE_TASKS.contains(t.getTask()))
                .filter(t -> t.getExpiryDate() != null && t.getExpiryDate().getTime() > now.getTime())
                .filter(t -> t.getCreateAt() != null && t.getCreateAt().getTime() < finalHwCreateTime)
                .collect(Collectors.toList());

        if (tasks.size() == 0) {
            return;
        }

        // 过期时间， epoch seconds
        int expireSeconds = calExpirationSeconds(tasks);

        // 更新完成人数
        // hashCommands.hincrby(homeworkKey,KEY_FIELD_FINISH_NUM,finishNum);
        cache.<Map<String, Object>>createCacheValueModifier()
                .key(homeworkKey)
                .modifier(m -> {
                    m.compute(KEY_FIELD_FINISH_NUM, (k, v) -> SafeConverter.toLong(v) + finishNum);
                    return m;
                })
                .expiration(expireSeconds)
                .execute();

        // 考虑到班级中间可能有人员变动，所以是实时差
        int groupStudentNum = getGroupStudentNum(groupId);

        // 缓存
        Map<Long, Integer> groupStuNumMap = new HashMap<>();
        groupStuNumMap.put(groupId, groupStudentNum);

        // 未达成任何一个任务条件，返回
        //int curRewardLvl = judgeRewardLevel(SafeConverter.toInt(finishNum),groupStudentNum);
        //if(curRewardLvl <= 0)
        //    return;

        List<Long> groupIdList = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByTeacherId(teacherId)
                .stream()
                .map(t -> groupLoader.getGroupLoader().loadGroup(t.getGroupId()).getUninterruptibly())
                .filter(Objects::nonNull)
                .filter(g -> {
                    Clazz clazz = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            .loadClazz(g.getClazzId());
                    // 过滤掉毕业班
                    return clazz != null && !clazz.isTerminalClazz();
                })
                .map(Group::getId)
                .collect(Collectors.toList());

        // 如果老师下面的班级发生了变化，由其指班级转让或者退出的情况。
        // 要删掉已经不存在的班级对应的作业记录
        List<String> needDelHomework = new ArrayList<>();
        String homeworksKey = buildKey(teacherId, KEY_PART_ALLHW);
        // 获得老师下面所有在任务有效期内的作业
        // List<Map<String,Object>> hwMaps = setCommands.smembers(homeworksKey)
        List<Map<String, Object>> hwMaps = cache.<Set<String>>load(homeworksKey)
                .stream()
                .map(hwKey -> {
                    //Map<String,Object> hwMap = hashCommands.hgetall(SafeConverter.toString(hwKey));
                    Map<String, Object> hwMap = cache.load(SafeConverter.toString(hwKey));
                    Long _groupId = MapUtils.getLong(hwMap, "groupId");

                    // 如果缓存中的班组已经不存在了，则删除掉
                    if (!groupIdList.contains(_groupId)) {
                        needDelHomework.add(hwKey.toString());
                        return null;
                    }

                    // 实时查询班组的人数，记录已经查询过的结果
                    Integer stuNum = groupStuNumMap.get(_groupId);
                    if (stuNum == null) {
                        stuNum = getGroupStudentNum(_groupId);
                        groupStuNumMap.put(_groupId, stuNum);
                    }

                    hwMap.put("studentNum", stuNum);
                    return hwMap;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 删除掉不存在的班组下面的作业
        // 这里就不删了，万一删错了就太吓人了
        /*needDelHomework.forEach(hwKey -> {
            setCommands.srem(homeworksKey,hwKey);
            //keyCommands.del(hwKey);
        });*/

        if (CollectionUtils.isNotEmpty(needDelHomework)) {
            cache.<Set<String>>createCacheValueModifier()
                    .key(homeworksKey)
                    .modifier(keys -> {
                        keys.removeAll(needDelHomework);
                        return keys;
                    })
                    .expiration(expireSeconds)
                    .execute();
        }

        for (TeacherResourceTask task : tasks) {
            TeachingResourceTask taskDict = TeachingResourceTask.parse(task.getTask());
            if (taskDict == null)
                continue;

            String conditionParamStr = taskDict.getConditionParam();
            if (StringUtils.isEmpty(conditionParamStr))
                continue;

            String[] condParams = conditionParamStr.split(",");
            int minAssignNum = SafeConverter.toInt(condParams[0]);
            int minFinishNum = SafeConverter.toInt(condParams[1]);
            double minRate = SafeConverter.toDouble(condParams[2]);

            // 筛选出时间满足条件的作业信息
            Map<Long, List<Map<String, Object>>> availableHwMaps = hwMaps.stream()
                    .filter(h -> {
                        Long createTime = MapUtils.getLong(h, "createTime");
                        return createTime > task.getCreateAt().getTime() && createTime < task.getExpiryDate().getTime();
                    })
                    .filter(h -> {
                        int _finishNum = MapUtils.getInteger(h, "finishNum");
                        int _studNum = MapUtils.getInteger(h, "studentNum");
                        // 提前算出某次作业能达到的奖励等级
                        //int rewardLvl = judgeRewardLevel(_finishNum, _studNum);

                        //h.put("rewardLvl", rewardLvl);
                        // 查看基本条件是否满足
                        //return rewardLvl > 0;
                        return _finishNum >= minFinishNum && _finishNum >= (int) (_studNum * minRate);

                    })
                    .collect(Collectors.groupingBy(hwm -> MapUtils.getLong(hwm, "groupId")));

            // 如果不是所有组都布置了作业，则略过处理
            // 这里把条件改松许多，只要一个组完成就可以
            if (availableHwMaps.size() <= 0) {
                continue;
            }

            // 改成任意一个班完成条件即可领取任务后，判断逻辑要从看最小的变成看最大的
            // int minRewardLvl = 4;// 默认是最高
            // int maxRewardLvl = 0;
            // 遍历每个组下面的作业
            for (List<Map<String, Object>> groupHws : availableHwMaps.values()) {
                // 如果任意一个组布置的作业次数，小于规定的值，也属于不满足条件
                // 这里如果不满足条件，不用跳外层了，继续看下一个组的情况就可以了。
                if (groupHws.size() < minAssignNum)
                    continue;

                // 能获得的奖励等级，从大到小排序
                /*List<Map<String, Object>> sortedGroupHws = groupHws.stream()
                        .sorted((gh1, gh2) -> MapUtils.getInteger(gh2, "rewardLvl").compareTo(MapUtils.getInteger(gh1, "rewardLvl")))
                        .collect(Collectors.toList());

                // 取第三高获得奖励的作业
                Map<String, Object> top3GroupHw = sortedGroupHws.get(MIN_ASSIGN_NUM - 1);
                int top3RewardLvl = MapUtils.getInteger(top3GroupHw, "rewardLvl");

                if (top3RewardLvl > maxRewardLvl)
                    maxRewardLvl = top3RewardLvl;*/

                teachingResourceService.finishTask(task.getId());
                break;
            }

            //int taskRewardLvl = SafeConverter.toInt(taskDict.name().split("_")[2]);
            //if (maxRewardLvl >= taskRewardLvl)
            //    teachingResourceService.finishTask(task.getId());
        }
    }

    private int getGroupStudentNum(Long groupId) {
        return raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupId(groupId)
                .size();
    }

    private String buildKey(Object... keyParts) {
        return KEY_PREFIX + StringUtils.join(keyParts, ":");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        //this.redisCommands = builder.getRedisCommands("user-easemob");
    }

    private void processAssignFinalWork(Long teacherId, String homeworkType) {
        try {
            Date now = new Date();

            // 考虑包班制的情况，副账号查询主账号
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
            if (mainTeacherId == null || mainTeacherId == 0L) {
                mainTeacherId = teacherId;
            }

            List<TeacherResourceTask> tasks = teachingResourceLoader.loadTeacherTasks(mainTeacherId)
                    .stream()
                    .filter(t -> Objects.equals(t.getStatus(), Status.ONGOING.name()))
                    .filter(t -> t.getExpiryDate() != null && t.getExpiryDate().after(now))
                    .filter(t -> ASSIGN_TASKS.contains(t.getTask()))
                    .collect(Collectors.toList());

            if (tasks.size() == 0) {
                return;
            }

            for (TeacherResourceTask task : tasks) {
                String taskName = task.getTask();

                if (Objects.equals(taskName, ASSIGN_WINTERVACATION.name()) && Objects.equals(homeworkType, "WinterVacation")) {
                    teachingResourceService.finishTask(task.getId());
                }

                if (Objects.equals(taskName, ASSIGN_BASICREVIEW_TERMREVIEW.name()) &&
                        (Objects.equals(homeworkType, "BasicReview") || Objects.equals(homeworkType, "TermReview"))) {
                    teachingResourceService.finishTask(task.getId());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
