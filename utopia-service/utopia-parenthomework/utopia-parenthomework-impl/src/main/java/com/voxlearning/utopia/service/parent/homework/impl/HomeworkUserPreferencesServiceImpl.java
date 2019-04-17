package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.core.utils.MQUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserPreferencesService;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;
import com.voxlearning.utopia.service.parent.homework.api.mapper.UserPreference;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserPreferencesDao;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.HOMEWORK_TOPIC;

/**
 * 布置作业查询接口实现
 *
 * @author Wenlong Meng
 * @version 20181111
 */
@Named
@ExposeService(interfaceClass = HomeworkUserPreferencesService.class)
@Slf4j
public class HomeworkUserPreferencesServiceImpl extends SpringContainerSupport implements HomeworkUserPreferencesService {

    @Inject
    private HomeworkUserPreferencesDao homeworkUserPreferencesDao;

    /**
     * 保存设置偏好
     *
     * @param userPreferences 偏好设置
     * @return
     */
    @Override
    public MapMessage upsertHomeworkUserPreferences(Collection<UserPreference> userPreferences) {
        if (CollectionUtils.isEmpty(userPreferences)) {
            return MapMessage.errorMessage();
        }
        // 参数校验
        boolean isValid = userPreferences.stream().anyMatch(h -> h.getUserId() == null || !Subject.isValidSubject(h.getSubject()) || StringUtils.isBlank(h.getBookId()));
        if (isValid) {
            return MapMessage.errorMessage("参数异常");
        }
        for(UserPreference userPreference : userPreferences) {
            HomeworkUserPreferences h = new HomeworkUserPreferences();
            h.setSubject(userPreference.getSubject());
            h.setUserId(userPreference.getUserId());
            h.setBookId(userPreference.getBookId());
            h.setLevels(userPreference.getLevels());
            h.setId(HomeworkUtil.generatorID(userPreference.getUserId(), userPreference.getSubject()));
            homeworkUserPreferencesDao.upsert(h);
            // 缓存的unitId
            String unitId = HomeWorkCache.load(CacheKey.UNIT, h.getUserId(), h.getSubject());
            if (StringUtils.isNotBlank(unitId)) {
                // 删除题包缓存
                HomeWorkCache.delete(CacheKey.BOX, userPreference.getBizType(), userPreference.getUserId(), unitId);
                // 删除单元缓存
                HomeWorkCache.delete(CacheKey.UNIT, userPreference.getUserId(), h.getSubject());
            }
        }
        // 发消息
        publishMessage(userPreferences.iterator().next().getUserId());
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage upsertUserPreferencesNoMessage(Collection<UserPreference> userPreferences) {
        if (CollectionUtils.isEmpty(userPreferences)) {
            return MapMessage.errorMessage();
        }
        // 参数校验
        boolean isValid = userPreferences.stream().anyMatch(h -> h.getUserId() == null || !Subject.isValidSubject(h.getSubject()) || StringUtils.isBlank(h.getBookId()));
        if (isValid) {
            return MapMessage.errorMessage("参数异常");
        }
        for(UserPreference userPreference : userPreferences) {
            HomeworkUserPreferences h = new HomeworkUserPreferences();
            h.setSubject(userPreference.getSubject());
            h.setUserId(userPreference.getUserId());
            h.setBookId(userPreference.getBookId());
            h.setLevels(userPreference.getLevels());
            h.setId(HomeworkUtil.generatorID(userPreference.getUserId(), userPreference.getSubject()));
            homeworkUserPreferencesDao.upsert(h);
        }
        return MapMessage.successMessage();
    }

    /**
     * 发消息
     * @param userId
     */
    private void publishMessage(Long userId) {
        Map<String, Object> message = MapUtils.m(
                "messageType", "preferences",
                "studentId", userId,
                "time", DateUtils.dateToString(new Date()),
                "desc", "偏好设置"
        );
        MQUtils.send(HOMEWORK_TOPIC, message);
    }

}
