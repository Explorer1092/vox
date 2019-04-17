package com.voxlearning.washington.controller.mobile.student;


import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.service.campaign.client.StudentActivityServiceClient;
import com.voxlearning.utopia.service.crm.api.ActivityConfigService;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.cache.WashingtonCacheSystem;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import java.util.Date;

public class BaseStudentActivityController extends AbstractMobileController {

    protected static final String ACTIVITY_ID = "activityId";

    @Inject
    protected StudentActivityServiceClient activityServiceClient;
    @Inject
    protected StudentActivityServiceClient stuActSrvCli;
    @Inject
    protected SchoolLoaderClient schoolLoaderClient;

    @Inject
    protected ActivityConfigServiceClient activityConfigServiceClient;
    @Inject
    protected ActivityConfigServiceClient configServiceClient;
    protected ActivityConfigService activityConfigService;

    @Inject
    protected WashingtonCacheSystem washingtonCacheSystem;
    protected UtopiaCache flushable;
    protected UtopiaCache persistence;

    @Override
    public void afterPropertiesSet() throws Exception {
        activityConfigService = configServiceClient.getActivityConfigService();
        flushable = washingtonCacheSystem.CBS.flushable;
        persistence = washingtonCacheSystem.CBS.persistence;
    }

    Integer getActivityLimitTime(String code, Integer defaultLimitTime) throws Exception {
        if (code.length() == 24) {
            ActivityConfig load = activityConfigServiceClient.getActivityConfigService().load(code);
            if (load.getDisabled()) {
                throw new RuntimeException("活动已被删除");
            }
            Date now = new Date();
            if (now.before(load.getStartTime())) {
                throw new RuntimeException("活动未开始");
            }
            if (now.after(load.getEndTime())) {
                throw new RuntimeException("活动已过期");
            }
            return load.getRules().getLimitTime();
        }
        return defaultLimitTime;
    }

    String genApiRetryCacheKey(StudentDetail student, String timestamp) {
        return "RETRY_" + student.getId() + "_" + timestamp;
    }

    void checkTeacherSignUpThrowException(String activityId, Long studentClazzId) {
        if (checkActivityId(activityId, studentClazzId)) return;

        // 老师布置的不用报名
        ActivityConfig activityConfig = configServiceClient.loadById(activityId);
        if (activityConfig.hasTeacher()) return;

        checkTeacherSignUpThrowException(studentClazzId, activityId);
    }

    void checkTeacherSignUpThrowException(ActivityConfig activityConfig, Long studentClazzId) {
        String activityId = activityConfig.getId();

        if (checkActivityId(activityId, studentClazzId)) return;

        // 老师布置的不用报名
        if (activityConfig.hasTeacher() || activityConfig.hasDev()) return;

        checkTeacherSignUpThrowException(studentClazzId, activityId);
    }

    private boolean checkActivityId(String activityId, Long studentClazzId) {
        if (StringUtils.isEmpty(activityId) || studentClazzId == null) return true;
        if (activityId.length() < 24) return true;
        int timestamp = new ObjectId(activityId).getTimestamp();
        return timestamp < ActivityConfigService.TEACHER_SIGN_UP_ON_LINE_TIME;
    }

    private void checkTeacherSignUpThrowException(Long studentClazzId, String activityId) {
        Boolean notSignUp = configServiceClient.isNotSignUp(persistence, activityId, studentClazzId);
        if (notSignUp) {
            throw new RuntimeException("老师尚未报名");
        }
    }
}
