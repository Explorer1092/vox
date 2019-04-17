package com.voxlearning.utopia.service.business.impl.service;

import com.lambdaworks.redis.api.async.RedisSortedSetAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisHashCommands;
import com.lambdaworks.redis.api.sync.RedisSetCommands;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.business.api.util.TeachingResourceUtils;
import com.voxlearning.utopia.service.business.api.TeachingResourceService;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask.Status;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.entity.TeachingResourceCollect;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceRaw;
import com.voxlearning.utopia.service.business.impl.dao.TeacherResourceTaskDao;
import com.voxlearning.utopia.service.business.impl.dao.TeachingResourceCollectDao;
import com.voxlearning.utopia.service.business.impl.dao.TeachingResourceDao;
import com.voxlearning.utopia.service.business.impl.dao.TeachingResourceVersion;
import com.voxlearning.utopia.service.business.impl.loader.TeachingResourceLoaderImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.campaign.client.YiqiJTServiceClient;
import com.voxlearning.utopia.service.config.consumer.BadWordCheckerClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by haitian.gan on 2017/8/3.
 */
@Named
@Slf4j
@ExposeServices({
        @ExposeService(interfaceClass = TeachingResourceService.class, version = @ServiceVersion(version = "1.7"))
})
public class TeachingResourceServiceImpl implements TeachingResourceService,InitializingBean{

    private static final String TEACHING_RESOURCE_HOT_SEARCH_RANK = "TEACHING_RESOURCE_HOT_SEARCH_RANK";
    @Inject private TeachingResourceDao teachingResourceDao;
    @Inject private TeacherResourceTaskDao teacherResourceTaskDao;
    @Inject private TeachingResourceLoaderImpl teachingResourceLoader;
    @Inject private TeacherLoaderClient teacherLoader;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private BusinessCacheSystem cacheSystem;
    @Inject private TeachingResourceCollectDao teachingResourceCollectDao;
    @Inject private YiqiJTServiceClient yiqiJTServiceClient;
    @Inject private BadWordCheckerClient badWordCheckerClient;
    @Inject private TeachingResourceVersion teachingResourceVersion;
    @Inject private TeacherTaskPrivilegeServiceImpl teacherTaskPrivilegeService;

    // private IRedisCommands redisCommands;
    private static final String KEY_PREFIX = "teacherTask:";

    private IRedisCommands redisCommands;
    private RedisSortedSetAsyncCommands<String, Object> redisSortedSetAsyncCommands;

    @Override
    public MapMessage upsertTeachingResource(TeachingResource resource) {
        try{
            if (resource.getFinishNum() != null) {
                resource.setFileUrl(resource.getFileUrl().trim());
            }
            teachingResourceDao.upsert(resource);
            teachingResourceVersion.increment();
            // 删掉Raw的缓存
            String cacheKey = CacheKeyGenerator.generateCacheKey(TeachingResourceRaw.class,"ALL");
            teachingResourceDao.getCache().delete(cacheKey);
        }catch(Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage receiveTask(Long userId, String resourceId, String type) {
        if (userId == null || userId == 0L) {
            return MapMessage.errorMessage("用户ID不存在!");
        }

        TeachingResource resource = teachingResourceDao.load(resourceId);
        if (resource == null) {
            return MapMessage.errorMessage("资源不存在!");
        }

        Long total = null, times = null;
        if (Objects.equals(type, TeachingResourceUtils.HAVE_TYPE_PRIVILEGE)) {
            MapMessage privilegeMessage = teacherTaskPrivilegeService.getCoursewareDownloadTimes(userId);
            if (!privilegeMessage.isSuccess()) {
                return privilegeMessage;
            }

            // 校验特权次数,如果够就减一个,如果不够,就返回失败,null 表示特权不限次数
            Long typeId = MapUtils.getLong(privilegeMessage, "id");
            times = MapUtils.getLong(privilegeMessage, "times");
            total = MapUtils.getLong(privilegeMessage, "total");
            if (times != null) {
                if (times <= 0) {
                    return MapMessage.errorMessage("兑换次数已经用完啦~");
                } else {
                    MapMessage mapMessage = teacherTaskPrivilegeService.cousumerPrivilege(userId, typeId, "[兑换课件资源]-" + resourceId);
                    if (!mapMessage.isSuccess()) {
                        return mapMessage;
                    }
                }
            }
        }

        Date now = new Date();

        TeacherResourceTask existTask = teacherResourceTaskDao.loadTeacherTasks(userId)
                .stream()
                .filter(t -> Objects.equals(t.getResourceId(),resourceId))
                .findFirst()
                .orElse(null);
        if (existTask != null) {
            // 只要完成就不让再次领
            if (Objects.equals(Status.FINISH.name(), existTask.getStatus())) {
                return MapMessage.errorMessage("任务已经已完成");
            }
            // 任务在有效期内不能重复领取, 超过有效期(未完成&&超期)时允许重复领取
            if (existTask.getExpiryDate().after(now)) {
                return MapMessage.errorMessage("任务已经存在!不能重复领取");
            }
        }

        // 如果是过了期的任务，可以重复领取
        TeacherResourceTask task = existTask;
        if (task == null) task = new TeacherResourceTask();

        task.setUserId(userId);
        task.setResourceId(resourceId);
        task.setTask(resource.getTask());
        task.setCreateAt(new Date());// 自己置时间不走框架

        TeachingResourceTask taskDict = TeachingResourceTask.parse(resource.getTask());
        // 如果是免费任务、特权兑换直接就完成了
        if ((taskDict == TeachingResourceTask.FREE || taskDict == TeachingResourceTask.NONE) || Objects.equals(type, TeachingResourceUtils.HAVE_TYPE_PRIVILEGE)) {
            task.setStatus(Status.FINISH.name());
            teachingResourceDao.incrFinishNum(resourceId);
        } else {
            task.setStatus(Status.ONGOING.name());
        }

        // 计算失效的日期
        Date receiveStartDate = DateUtils.ceiling(new Date(), Calendar.DAY_OF_MONTH);
        // 有效期不在代码里写死了，改成配置的
        Date expiryDate = DateUtils.addDays(receiveStartDate,resource.getValidityPeriod());
        task.setExpiryDate(expiryDate);

        try {
            teacherResourceTaskDao.upsert(task);
        }catch (Throwable t){
            return MapMessage.errorMessage(t.getMessage());
        }
        teachingResourceDao.incrParticipateNum(resourceId);
        return MapMessage.successMessage()
                .add("privilegeCount", total)
                .add("privilegeSurplusCount", times != null ? times - 1 : null);
    }

    @Override
    public MapMessage checkTask(TeacherResourceTask task) {
        return null;
    }

    @Override
    public MapMessage finishTask(String taskId) {
        TeacherResourceTask task = teacherResourceTaskDao.load(taskId);
        if(task == null)
            return MapMessage.errorMessage("任务不存在!");

        task.setStatus(TeacherResourceTask.Status.FINISH.name());
        try {
            teacherResourceTaskDao.upsert(task);
            teachingResourceDao.incrFinishNum(task.getResourceId());

            String receiePageUrl = "/view/mobile/teacher/teachingresource/received";
            // 发送消息提醒
            AppMessage msg = new AppMessage();
            msg.setUserId(task.getUserId());
            msg.setMessageType(TeacherMessageType.ACTIVIY.getType());
            msg.setContent("你有一份教学资源可以领取");
            msg.setTitle("教学资源消息");
            msg.setLinkType(1);
            msg.setLinkUrl(receiePageUrl);
            msg.setCreateTime(new Date().getTime());
            // 发完成任务的App消息
            Long mainId = teacherLoader.loadMainTeacherId(task.getUserId());
            if (mainId != null && mainId > 0L) {
                msg.setUserId(mainId);
            }

            messageCommandServiceClient.getMessageCommandService().createAppMessage(msg);

            // pc端的消息，还要有个跳转地址
            String pcReceiveUrl = "https://www." + TopLevelDomain.getTopLevelDomain()
                    + "/teacherMobile/teachingres/myresources.vpage";
            String pcMsg = "你有一份教学资源可以领取。<a href=\""+ pcReceiveUrl +"\" class=\"w-blue\" target=\"_blank\">【点击查看】</a>";
            teacherLoader.sendTeacherMessage(task.getUserId(),pcMsg);

            // 推送
            String messageContent = "你有一份教学资源可以领取";
            Map<String, Object> extInfo = MapUtils.m(
                    "s", TeacherMessageType.APPLICATION.getType(),
                    "link", "https://www." + TopLevelDomain.getTopLevelDomain() + "/" + receiePageUrl,
                    "t", "h5");

            appMessageServiceClient.sendAppJpushMessageByIds(
                    messageContent,
                    AppMessageSource.JUNIOR_TEACHER,
                    Collections.singletonList(task.getUserId()),
                    extInfo);

            return MapMessage.successMessage();
        }catch (Throwable t){
            return MapMessage.errorMessage(t.getMessage());
        }
    }

    @Override
    public MapMessage finishUserTask(Long userId,String taskType) {
        List<TeacherResourceTask> tasks = teachingResourceLoader.loadTeacherTasks(userId)
                .stream()
                .filter(t -> Objects.equals(t.getTask(),taskType))
                .filter(t -> Objects.equals(TeacherResourceTask.Status.ONGOING.name(),t.getStatus()))
                .collect(Collectors.toList());

        if(CollectionUtils.isEmpty(tasks))
            return MapMessage.errorMessage("用户未领取拉新任务!");
        else{
            boolean result = true;
            String errorInfo = "";
            for(TeacherResourceTask task : tasks){
                MapMessage resultMsg = finishTask(task.getId());
                if(!resultMsg.isSuccess()){
                    result = false;
                    errorInfo += resultMsg.getInfo();
                }
            }

            // 如果报错，汇总出错信息
            if(result)
                return MapMessage.successMessage();
            else
                return MapMessage.errorMessage(errorInfo);
        }
    }

    @Override
    public MapMessage supplyUserTask(Long userId) {
        return MapMessage.errorMessage("已废弃");
    }

    @Override
    public MapMessage addReadCount(String id) {
        try {
            teachingResourceDao.incrReadCount(id,1L);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage addCollectCount(String id) {
        try {
            teachingResourceDao.incrCollectCount(id,1L);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage addCollect(Long userId, String categorie, String resourceId) {
        List<TeachingResourceCollect> collects = teachingResourceCollectDao.loadAllByUser(userId);

        TeachingResourceCollect saveModle = new TeachingResourceCollect();
        saveModle.setUserId(userId);
        saveModle.setCategory(categorie);
        saveModle.setResourceId(resourceId);
        saveModle.setDisabled(false);
        if (collects.indexOf(saveModle) >= 0) {
            return MapMessage.errorMessage("不可重复收藏");
        }

        // 这里不从栏目区分了, 如果资源ID是24位的ObjectId 说明是资源那一套逻辑,否则就是新讲堂那一套新逻辑
        if (TeachingResourceUtils.isTeachingResource(resourceId)) {
            if (teachingResourceDao.load(resourceId) == null) {
                return MapMessage.errorMessage("资源不存在");
            }
        } else {
            // 一起新讲堂的 load 较重且跨服务, 异常数据入库暂可接受, 控制层已过滤
        }

        teachingResourceCollectDao.upsert(saveModle);
        // 增加收藏量
        if (TeachingResourceUtils.isCourcseId(resourceId)) {
            yiqiJTServiceClient.add17JTCollectCount(SafeConverter.toLong(resourceId), 1L);
        } else {
            addCollectCount(resourceId);
        }
        return MapMessage.successMessage().add("delCollectId", saveModle.getId());
    }

    @Override
    public MapMessage disableCollect(Long userId, String resourceId, String categorie, String collectId) {
        long disable = teachingResourceCollectDao.disable(userId, collectId);
        if (disable > 0) {
            // 减少收藏量
            if (TeachingResourceUtils.isCourcseId(resourceId)) {
                yiqiJTServiceClient.add17JTCollectCount(SafeConverter.toLong(resourceId), -1L);
            } else {
                teachingResourceDao.incrCollectCount(resourceId, -1L);
            }
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("未找到要删除的收藏资源");
    }

    @Override
    public MapMessage addHotSearch(String word) {
        if (!badWordCheckerClient.containsConversationBadWord(word)) {
            redisSortedSetAsyncCommands.zincrby(TEACHING_RESOURCE_HOT_SEARCH_RANK, 1, word);
            //redisSortedSetAsyncCommands.zremrangebyrank(TEACHING_RESOURCE_HOT_SEARCH_RANK, 200, -1);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage moveDataForBackDoor() {
        try {
            RedisSetCommands<String,Object> setCmd = redisCommands.sync().getRedisSetCommands();
            RedisHashCommands<String,Object> hashCmd = redisCommands.sync().getRedisHashCommands();
            Cache cache = cacheSystem.CBS.persistence;

            AtomicInteger opCount = new AtomicInteger();
            teacherResourceTaskDao.loadAllValidTaskForBackDoor()
                    .stream()
                    .map(t -> t.getUserId())
                    // 考虑包班制，要把所有老师都算上
                    .flatMap(mId -> {
                        List<Long> allTchIds = new ArrayList<>();
                        allTchIds.addAll( teacherLoader.loadSubTeacherIds(mId));
                        allTchIds.add(mId);

                        return allTchIds.stream();
                    })
                    .distinct()
                    .forEach(tId -> {
                        String allHomeworkKey = buildKey(tId, "homeworks");

                        Set<Object> orgObjs = setCmd.smembers(allHomeworkKey);
                        if(orgObjs == null || orgObjs.size() == 0){
                            return;
                        }

                        Set<String> orgKeys = orgObjs.stream().map(SafeConverter::toString).collect(Collectors.toSet());
                        cache.set(allHomeworkKey, 0 , orgKeys);
                        opCount.incrementAndGet();

                        orgKeys.forEach(key -> {
                            Map<String,Object> hwMap = hashCmd.hgetall(key);
                            if(MapUtils.isEmpty(hwMap))
                                return;

                            cache.set(key, 0, hwMap);
                        });
                    });

            return MapMessage.successMessage().add("count", opCount.get());
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage fixExpiryData() {
        AlpsThreadPool.getInstance().submit(() -> {
            List<TeacherResourceTask> teacherResourceTasks = teacherResourceTaskDao.loadExpiryData();

            if (RuntimeMode.isStaging()) {
                return;
            }
            for (TeacherResourceTask task : teacherResourceTasks) {
                try {
                    MapMessage mapMessage = finishTask(task.getId());
                    if (!mapMessage.isSuccess()) {
                        log.error("TeacherResourceTask fix data exception,userId:" + task.getUserId() + " taskId:" + task.getId());
                    }
                } catch (Exception e) {

                }
            }
        });
        return MapMessage.successMessage("已执行,请检查");
    }

    private String buildKey(Object... keyParts) {
        return KEY_PREFIX + StringUtils.join(keyParts, ":");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        this.redisCommands = builder.getRedisCommands("user-easemob");
        redisSortedSetAsyncCommands = redisCommands.async().getRedisSortedSetAsyncCommands();
    }

}
