package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.campaign.api.UserActivityService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityRef;
import com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherActivityRefDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
@ExposeService(interfaceClass = UserActivityService.class)
public class UserActivityServiceImpl implements UserActivityService {

    @Inject
    private TeacherActivityRefDao userActivityRefDao;

    @Override
    public TeacherActivityRef loadUserActivity(Long userId, String enumType) {
        TeacherActivityEnum activityEnum = TeacherActivityEnum.safeValueOf(enumType);
        if (activityEnum == null) {
            return null;
        }
        return userActivityRefDao.loadUserIdTypeId(userId, activityEnum);
    }

    @Override
    public TeacherActivityRef saveUserActivity(Long userId, String activityEnumType) {
        TeacherActivityRef userActivity = loadUserActivity(userId, activityEnumType);
        if (userActivity == null) {
            userActivity = new TeacherActivityRef();
            userActivity.setUserId(userId);
            userActivity.setType(activityEnumType);
            userActivityRefDao.upsert(userActivity);
        }
        return userActivity;
    }
}
