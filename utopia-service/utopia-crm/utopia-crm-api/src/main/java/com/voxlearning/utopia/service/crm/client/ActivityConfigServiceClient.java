package com.voxlearning.utopia.service.crm.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.buffer.ActivityConfigBuffer;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.service.crm.api.ActivityConfigLoader;
import com.voxlearning.utopia.service.crm.api.ActivityConfigService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ActivityConfigServiceClient implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ActivityConfigServiceClient.class);

    @Getter
    @ImportService(interfaceClass = ActivityConfigService.class)
    private ActivityConfigService activityConfigService;

    @ImportService(interfaceClass = ActivityConfigLoader.class)
    private ActivityConfigLoader activityConfigLoader;

    // buffer 中存的是最近一周审核通过的活动
    private ManagedNearBuffer<List<ActivityConfig>, ActivityConfigBuffer> latelyPassedActivityConfigBuffer;

    @Override
    public void afterPropertiesSet() throws Exception {
        NearBufferBuilder<List<ActivityConfig>, ActivityConfigBuffer> builder = NearBufferBuilder.newBuilder();
        builder.nearBufferClass(ActivityConfigBuffer.class);
        builder.reloadNearBuffer(5, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> activityConfigService.getLatelyPassedActivityBuffer(-1L));
        builder.reloadNearBuffer((version, attributes) -> activityConfigService.getLatelyPassedActivityBuffer(version));
        latelyPassedActivityConfigBuffer = builder.build();
    }

    public ActivityConfigBuffer getLatelyPassedActivityBuffer() {
        return latelyPassedActivityConfigBuffer.getNativeBuffer();
    }

    public ActivityConfig loadById(String id) {
        return activityConfigService.load(id);
    }

    public List<ActivityConfig> getCanParticipateActivity(StudentDetail studentDetail) {
        return getLatelyPassedActivityBuffer().getCanParticipateActivity(studentDetail);
    }

    public List<ActivityConfig> loadBySchoolIdAreaIdClazzIds(Long schoolId, Integer areaId, List<Clazz> clazzList) {
        return getLatelyPassedActivityBuffer().loadBySchoolIdAreaIdClazzIds(schoolId, areaId, clazzList);
    }

    public List<ActivityConfig> loadNoSignUpActivity(Long teacherId) {
        return activityConfigService.loadNoSignUpActivity(teacherId);
    }

    public MapMessage signUpActivity(Long teacherId, String activityId) {
        return activityConfigService.signUpActivity(teacherId, activityId);
    }

    public Boolean loadSignUpStatus(Long teacherId, String activityId) {
        return activityConfigService.loadSignUpStatus(teacherId, activityId);
    }

    public Boolean isSignUp(UtopiaCache cache, String activityId, Long clazzId) {
        String cacheKey = buildActivitySignUpCacheKey(activityId, clazzId);
        CacheObject<Object> cacheObject = cache.get(cacheKey);
        return cacheObject.containsValue();
    }

    public Boolean isNotSignUp(UtopiaCache cache, String activityId, Long clazzId) {
        return !isSignUp(cache, activityId, clazzId);
    }

    /**
     * 查询老师布置的正在进行中的活动
     * 一个班级只能有一个正在进行中的活动
     * @param clazzIds
     * @return
     */
    public Map<Long, ActivityConfig> loadClazzActivityConfig(Collection<Long> clazzIds) {
        Map<Long, ActivityConfig> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(clazzIds)) {
            Map<Long, List<ActivityConfig>> activity = activityConfigLoader.loadClassesActivity(clazzIds);
            activity.forEach((k, v) ->
                v.stream().filter(a -> a.isStarting(new Date())).findAny().ifPresent(activityConfig -> map.put(k, activityConfig))
            );
        }
        return map;
    }

    private String buildActivitySignUpCacheKey(String activityId, Long clazzId) {
        return CacheKeyGenerator.generateCacheKey("TEACHER_SIGN_UP_ACTIVITY", new String[]{"A", "C"}, new Object[]{activityId, clazzId});
    }

    /**
     * 生成班级布置活动的 cache key
     */
//    private String buildClazzActivityCacheKey(Long clazzId) {
//        return CacheKeyGenerator.generateCacheKey("TEACHER_CLAZZ_ACTIVITY",
//                new String[]{"T", "C"},
//                new Object[]{clazzId}
//        );
//    }
}
