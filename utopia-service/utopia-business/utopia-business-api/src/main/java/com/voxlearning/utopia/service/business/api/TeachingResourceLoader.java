package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mapper.TeachingResourceStatistics;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.entity.TeachingResourceCollect;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceRaw;
import com.voxlearning.utopia.service.business.buffer.mapper.TeachingResourceList;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by haitian.gan on 2017/8/1.
 */
@ServiceVersion(version = "1.8")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface TeachingResourceLoader {

    @CacheMethod(type = TeachingResourceStatistics.class, expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week))
    TeachingResourceStatistics loadResourceStatistics(@CacheParameter String id);

    @CacheMethod(type = TeachingResource.class)
    TeachingResource loadResource(@CacheParameter String id);

    @Deprecated
    List<TeachingResource> loadAllResources();

    @CacheMethod(type = TeachingResourceRaw.class, key = "ALL", expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today))
    List<TeachingResourceRaw> loadAllResourcesRaw();

    /**
     * 获得首页的精选推荐资源列表
     */
    MapMessage loadHomePageChoicestResources(Long teacherId);

    @CacheMethod(type = TeacherResourceTask.class)
    List<TeacherResourceTask> loadTeacherTasks(@CacheParameter("USER_ID") Long userId);

    /**
     * 获得老师下面对应具体某个资源的任务数据
     * @param userId
     * @param resourceId
     * @return
     */
    default TeacherResourceTask loadTeacherTask(Long userId,String resourceId){
        return loadTeacherTasks(userId)
                .stream()
                .filter(t -> Objects.equals(t.getResourceId(),resourceId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 很久以前跑job用的
     * @param status
     * @return
     */
    @Deprecated
    List<TeacherResourceTask> loadTasksByStatus(String status);

    /**
     * 以前测试用的
     * @param teacherId
     * @return
     */
    @Deprecated
    List<String> testForRedis(Long teacherId);

    /**
     * 获得一个任务关联的详细作业数据
     * @param teacherId
     * @return
     */
    List<Map<String,Object>> loadTaskHomeworkDetail(Long teacherId);

    /**
     * 获得老师领取的资源任务的进展详情
     * @param teacherId
     * @param resourceId
     * @return
     */
    Map<String,Object> loadTaskProgress(Long teacherId,String resourceId);

    List<TeachingResourceCollect> loadCollectByUserId(Long userId);

    List<String> getHotWord();

    /**
     * 获取一起新讲堂
     * @return
     */
    List<TeachingResourceRaw> getYQJTRaws();

    @NoResponseWait
    void reloadTeachingResourceList();

    TeachingResourceList loadTeachingResourceList(long version);

    default void fillReadCollectCount(List<TeachingResourceRaw> content) {
        // 填充阅读、收藏、完成、参加
        for (TeachingResourceRaw resourceRaw : content) {
            if (resourceRaw.getIsCourse()) continue;
            String category = resourceRaw.getCategory();
            TeachingResourceStatistics teachingResource = new TeachingResourceStatistics();
            if (Objects.equals(TeachingResource.Category.WEEK_WELFARE.name(), category)
                    || Objects.equals(TeachingResource.Category.TEACHING_SPECIAL.name(), category)
                    || Objects.equals(TeachingResource.Category.SYNC_COURSEWARE.name(), category)

                    || Objects.equals(TeachingResource.Category.IMPORTANT_CASE.name(), category)
                    || Objects.equals(TeachingResource.Category.GROW_UP.name(), category)
                    || Objects.equals(TeachingResource.Category.ACTIVITY_NOTICE.name(), category)
                    || Objects.equals(TeachingResource.Category.OTHER_STONE.name(), category)) {
                teachingResource = loadResourceStatistics(resourceRaw.getId());
                resourceRaw.setReadCount(SafeConverter.toLong(teachingResource.getReadCount()));
                resourceRaw.setCollectCount(SafeConverter.toLong(teachingResource.getCollectCount()));
            }
            if (Objects.equals(TeachingResource.Category.WEEK_WELFARE.name(), category)) {
                resourceRaw.setFinishNum(SafeConverter.toLong(teachingResource.getFinishNum()));
                resourceRaw.setParticipateNum(SafeConverter.toLong(teachingResource.getParticipateNum()));
            }
        }
    }

}
