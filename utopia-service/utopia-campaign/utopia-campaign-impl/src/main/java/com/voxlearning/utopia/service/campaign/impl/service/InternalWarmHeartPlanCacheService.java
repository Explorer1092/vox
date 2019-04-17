package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.api.constant.WarmHeartPlanConstant;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import static com.voxlearning.utopia.api.constant.WarmHeartPlanConstant.*;

@Named
public class InternalWarmHeartPlanCacheService implements InitializingBean {
    @Inject
    private CampaignCacheSystem campaignCacheSystem;

    private UtopiaCache storageCache;
    @Inject
    private WarmHeartPlanServiceImpl warmHeartPlanService;

    @Override
    public void afterPropertiesSet() throws Exception {
        storageCache = campaignCacheSystem.CBS.storage;
    }


    private String genTeacher21DaySizeCacheKey(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(WARM_HEART_TEACHER_21DAY, new String[]{"TID"}, new Object[]{teacherId});
    }

    /**
     * 查询老师有多少个学生打满21天
     */
    public Long getTeacher21DaySize(Long teacherId) {
        String cacheKey = genTeacher21DaySizeCacheKey(teacherId);
        CacheObject<Object> cacheObject = storageCache.get(cacheKey);
        if (cacheObject.containsValue()) {
            return SafeConverter.toLong(cacheObject.getValue().toString().trim());
        }
        return 0L;
    }

    private Long incrTeacher21DaySize(Long teacherId, Long incr) {
        String cacheKey = genTeacher21DaySizeCacheKey(teacherId);
        return storageCache.incr(cacheKey, incr, 1, calcExpire());
    }

    /**
     * 学生打满21天后调用此方法
     */
    public void addStudent21daySize(Long studentId) {
        Set<Long> teacherIds = warmHeartPlanService.loadTeachersByStudentId(studentId);
        for (Long teacherId : teacherIds) {
            String generateCacheKey = CacheKeyGenerator.generateCacheKey(WARM_HEART_TEACHER_STUDENT_21DAY, new String[]{"T", "S"}, new Object[]{teacherId, studentId});
            if (!storageCache.get(generateCacheKey).containsValue()) {
                Long result = incrTeacher21DaySize(teacherId, 1L);
                if (Objects.equals(result, 5L)) {
                    warmHeartPlanService.sendTeacherMsg(teacherId, "【奖励特权】感谢您对教育的热忱与付出，恭喜您已经获得《家校共育大使》电子荣誉证书。点击领取>>");
                }
                if (Objects.equals(result, 15L)) {
                    warmHeartPlanService.sendTeacherMsg(teacherId, "【奖励特权】恭喜您已完成「家校共育」计划，获得价值100元一起学课程礼券和优质课教学视频100G，点击领取>> ");
                    warmHeartPlanService.sendCoupon(teacherId);
                    warmHeartPlanService.sendTeacherResourceMsg(teacherId,
                            "【家校共育】大使奖励",
                            "恭喜您获得优质课教学视频100G，点击领取>> 请在电脑端下载",
                            RESOURCE_PATH + "5c9b7d335ff054b28cb2fce3");
                }
                storageCache.set(generateCacheKey, calcExpire(), 0);
            }
        }
    }

    private String genStudent21DaySizeCacheKey(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(WARM_HEART_STUDENT_21DAY, new String[]{"SID"}, new Object[]{studentId});
    }

    /**
     * 查询学生有几个计划满足21天
     */
    public Long getStudent21DaySize(Long studentId) {
        String cacheKey = genStudent21DaySizeCacheKey(studentId);
        CacheObject<Object> cacheObject = storageCache.get(cacheKey);
        if (cacheObject.containsValue()) {
            return SafeConverter.toLong(cacheObject.getValue().toString().trim());
        }
        return 0L;
    }

    public Long incrStudent21DaySize(Long studentId, Long incr) {
        String cacheKey = genTeacher21DaySizeCacheKey(studentId);
        return storageCache.incr(cacheKey, incr, 1, calcExpire());
    }


    private int calcExpire() {
        long expire = WarmHeartPlanConstant.WARM_HEART_PLAN_END_TIME.getTime() - new Date().getTime();
        return (int) (expire / 1000);
    }

    public MapMessage backdoorDelKey(String key) {
        storageCache.delete(key);
        return MapMessage.successMessage();
    }

    public MapMessage backdoorIncrKey(String key, Long incr, Long init) {
        Long result = storageCache.incr(key, incr, init, calcExpire());
        return MapMessage.successMessage().add("result", result);
    }
}
