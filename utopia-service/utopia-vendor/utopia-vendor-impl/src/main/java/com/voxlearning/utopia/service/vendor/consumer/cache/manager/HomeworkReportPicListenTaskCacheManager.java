package com.voxlearning.utopia.service.vendor.consumer.cache.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * 作业报告句子染红点读机任务缓存观里
 * 任务60天
 * 0 未完成(差不到也是未完成)  1 已完成
 * @author jiangpeng
 * @since 2017-02-09 下午1:15
 **/
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 60*60*24*60)
public class HomeworkReportPicListenTaskCacheManager extends PojoCacheObject<String, String> {
    public HomeworkReportPicListenTaskCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void finishTask(String homeworkId, Long studentId){
//        set(homeworkId, "1");
        getCache().set(cacheKey(generateKey(homeworkId, studentId)), ((int)(System.currentTimeMillis()/1000)) + 60*60*24*60, "1");
    }

    public Integer getTaskStatus(String homeworkId, Long studentId){
        return SafeConverter.toInt(load(generateKey(homeworkId, studentId)));
    }

    private String generateKey(String homeworkId, Long studentId){
        return homeworkId + "-" + studentId;
    }
}
