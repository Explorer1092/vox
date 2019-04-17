package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivity;
import com.voxlearning.utopia.service.zone.api.entity.SignRecord;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chensn
 * @date 2018-10-23 16:41
 */
@Named("com.voxlearning.utopia.service.zone.impl.support.ClazzActivityCacheManager")
public class ClazzActivityCacheManager implements InitializingBean {

    private ActivityCache activityCache;
    private Halloween2018UsersCache halloween2018UsersCache;
    private ClassActivityCountCache classActivityCountCache;
    private ClazzBossCountCache clazzBossCountCache;
    private ClazzCircleChickenCache clazzCircleChickenCache;
    private ClazzBossSubjectCache cLazzBossSubjectCache;
    private ClazzPlotActivitySelectCache clazzPlotActivitySelectCache;
    private ClassCircleModifySignUserCountCache classCircleModifySignUserCountCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        UtopiaCache cache = CacheSystem.CBS.getCacheBuilder().getCache("columb-zone-cache");
        UtopiaCache cacheStorage = CacheSystem.CBS.getCacheBuilder().getCache("storage");
        activityCache = new ActivityCache(cache);
        halloween2018UsersCache = new Halloween2018UsersCache(cache);
        classActivityCountCache = new ClassActivityCountCache(cacheStorage);
        clazzBossCountCache = new ClazzBossCountCache(cacheStorage);
        cLazzBossSubjectCache= new ClazzBossSubjectCache(cacheStorage);
        clazzCircleChickenCache= new ClazzCircleChickenCache(cacheStorage);
        clazzPlotActivitySelectCache = new ClazzPlotActivitySelectCache(cacheStorage);
        classCircleModifySignUserCountCache = new ClassCircleModifySignUserCountCache(cacheStorage);
    }

    public List<ClazzActivity> findActivityCache() {
        return activityCache.findActivityCache();
    }

    public void save(List<ClazzActivity> list) {
        activityCache.set(ActivityCache.KEY, list);
    }

    public void saveHalloweenCount(Integer type) {
        halloween2018UsersCache.increase(type);
    }

    public Map<String, Long> queryHalloweenCount() {
        Map<String, Long> map = halloween2018UsersCache.loadLikedByTypeCounts();
        Map<String, Long> sortMap = new LinkedHashMap<>();
        map.entrySet().stream().forEach(entry -> {
            int i = entry.getKey().split("_").length;
            sortMap.put(entry.getKey().split("_")[i - 1], entry.getValue());
        });
        return sortMap;
    }

    public void increaseByActivity(Integer activityId) {
        classActivityCountCache.increaseByActivity(activityId);
    }

    public void setIncreaseByActivity(Integer activityId, Long num) {
        classActivityCountCache.setIncreaseByActivity(activityId, num);
    }
    public Long loadByActivity(Integer activityId) {
        return classActivityCountCache.loadByActivity(activityId);
    }

    public String loadByKey(String key) {
        return halloween2018UsersCache.loadByKey(key);
    }

    public void setValueByKey(String key, String value) {
        classActivityCountCache.setValueByKey(key, value);
    }

    public void deleteCache(String key) {
        halloween2018UsersCache.deleteKey(key);
    }

    public Long increaseClazzBossCountByKey(String key) {
        return clazzBossCountCache.increase(key);
    }

    public Long loadClazzBossCountByKey(String key) {
        return clazzBossCountCache.loadByKey(key);
    }

    public void deleteActivityCache() {
        activityCache.evict(ActivityCache.KEY);
    }

    public Boolean setClazzBossCountByKey(String key, Long value) {
        return clazzBossCountCache.setByKey(key, value);
    }

    public Boolean deleteClazzBossCountByKey(String key) {
        return clazzBossCountCache.deleteByKey(key);
    }

    public Boolean setClazzBossSubject(String key, String value) {
        return cLazzBossSubjectCache.setValueByKey(key, value);
    }

    public String getClazzBossSubject(String key) {
        return cLazzBossSubjectCache.getValueByKey(key);
    }

    //吃鸡活动增加
    public Long increaseChickenCountByKey(String key) {
        return clazzCircleChickenCache.increase(key);
    }

    //吃鸡活动 获取
    public Long loadChickenCountByKey(String key) {
        return clazzCircleChickenCache.loadByKey(key);
    }

    //吃鸡活动 设置
    public Boolean setChickenCountByKey(String key, Long value) {
        return clazzCircleChickenCache.setByKey(key, value);
    }

    //吃鸡活动 删除
    public Boolean deleteChickenCountByKey(String key) {
        return clazzCircleChickenCache.deleteByKey(key);
    }

    //剧情弹框结果增加
    public Long increasePlotSelectResult(String key) {
        return clazzPlotActivitySelectCache.increase(key);
    }
    //剧情弹框结果获取
    public Long loadPlotSelectResult(String key) {
        return clazzPlotActivitySelectCache.loadByKey(key);
    }

    //补签设置
    public void setModifySignUserCountCache(Long userId, List<SignRecord> signRecords) {
        classCircleModifySignUserCountCache.setKey(userId, signRecords);
    }

    //补签统计
    public List<SignRecord> loadModifySignUserCountCache(Long userId) {
        return classCircleModifySignUserCountCache.loadByKey(userId.toString());
    }

    //更新弹窗
    public void updateModifySign(Long userId, Integer type) {
        classCircleModifySignUserCountCache.updateKey(userId, type);
    }

}


