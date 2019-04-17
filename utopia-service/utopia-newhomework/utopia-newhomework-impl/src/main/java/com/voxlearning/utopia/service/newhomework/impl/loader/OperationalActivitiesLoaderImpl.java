package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.OperationalActivitiesLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.consumer.cache.ActivityHomeworkParentRewardCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.NewHomeworkPublisher;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = OperationalActivitiesLoader.class)
@ExposeService(interfaceClass = OperationalActivitiesLoader.class)
public class OperationalActivitiesLoaderImpl extends SpringContainerSupport implements OperationalActivitiesLoader {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private DubbingLoaderClient dubbingLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private NewHomeworkPublisher newHomeworkPublisher;

    @Override
    public MapMessage fetchMotherDayJztReport(String hid, Long sid, User parent) {
        NewHomework newHomework = newHomeworkLoader.load(hid);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (newHomework.isDisabledTrue()) {
            return MapMessage.errorMessage("作业已删除");
        }

        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        User user = null;
        if (userMap.containsKey(sid)) {
            user = userMap.get(sid);
        } else if (parent != null && parent.isParent()) {
            //兼容家长数据
            List<User> users = studentLoaderClient.loadParentStudents(parent.getId());
            if (CollectionUtils.isNotEmpty(users)) {
                for (User u : users) {
                    if (userMap.containsKey(u.getId())) {
                        user = u;
                        break;
                    }
                }
            }
        }
        if (user == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING);
        if (target == null) {
            return MapMessage.errorMessage("作业不存在配音类型");
        }
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), user.getId(), false);
        if (newHomeworkResult == null) {
            return MapMessage.errorMessage("学生未完成该作业");
        }
        if (!newHomeworkResult.isFinished()) {
            return MapMessage.errorMessage("学生未完成");
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING);
        if (newHomeworkResultAnswer == null) {
            return MapMessage.errorMessage("答题记录不包含配音");
        }
        if (MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
            return MapMessage.errorMessage("答题成绩丢失");
        }
        NewHomeworkResultAppAnswer appAnswer = newHomeworkResultAnswer.getAppAnswers().values().iterator().next();
        Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdIncludeDisabled(appAnswer.getDubbingId());
        if (dubbing == null) {
            return MapMessage.errorMessage("视频已经不存在");
        }

        // 是否已发放过奖励
        ActivityHomeworkParentRewardCacheManager manager = newHomeworkCacheService.getActivityHomeworkParentRewardCacheManager();
        String cacheKey = manager.getKidsDayCacheKey(hid, sid);
        Long cacheResult = manager.load(cacheKey);
        return MapMessage.successMessage()
                .add("dubbingId", appAnswer.getDubbingId())
                .add("coverUrl", dubbing.getCoverUrl())
                .add("studentName", user.fetchRealnameIfBlankId())
                .add("videoUrl", appAnswer.getVideoUrl())
                .add("rewarded", cacheResult != null && SafeConverter.toLong(cacheResult) != 0L);
    }

    @Override
    public MapMessage rewardStudent(String hid, Long sid, User user) {
        NewHomework newHomework = newHomeworkLoader.load(hid);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (newHomework.isDisabledTrue()) {
            return MapMessage.errorMessage("作业已删除");
        }

        if (user == null || !user.isParent()) {
            return MapMessage.errorMessage("请在家长端登录");
        }
        Long parentId = user.getId();
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(sid);
        if (CollectionUtils.isNotEmpty(studentParentRefs) && studentParentRefs.stream().anyMatch(studentParentRef -> parentId.equals(studentParentRef.getParentId()))) {
            try {
                return AtomicLockManager.getInstance().wrapAtomic(this)
                        .keys(sid)
                        .proxy()
                        .postRewardMessage(hid, sid, parentId);
            } catch (CannotAcquireLockException ex) {
                return MapMessage.errorMessage("奖励发放中，请不要重复点击!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
            }
        } else {
            return MapMessage.errorMessage("学生与家长关联关系不存在");
        }
    }

    public MapMessage postRewardMessage(String hid, Long studentId, Long parentId) {
        // 读取缓存判断这个学生是否发放过奖励
        ActivityHomeworkParentRewardCacheManager manager = newHomeworkCacheService.getActivityHomeworkParentRewardCacheManager();
        String cacheKey = manager.getKidsDayCacheKey(hid, studentId);
        Long cacheResult = manager.load(cacheKey);
        // 发放过直接返回失败已奖励
        if (cacheResult != null && SafeConverter.toLong(cacheResult) != 0L) {
            return MapMessage.errorMessage("已发放过奖励！");
        }
        // 没发放过就发送广播并更新缓存 家长端收到后会专做如下处理:生成家长奖励、发放家长奖励、发送亲子信
        Map<String, Object> map = new HashMap<>();
        map.put("messageType", HomeworkPublishMessageType.finished);
        map.put("studentId", studentId);
        map.put("parentId", parentId);
        map.put("homeworkType", NewHomeworkType.Activity);
        map.put("homeworkTag", HomeworkTag.KidsDay);
        newHomeworkPublisher.getParentRewardPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        manager.add(cacheKey, parentId);
        return MapMessage.successMessage();
    }
}
