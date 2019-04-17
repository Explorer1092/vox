package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.zone.api.entity.ClassCircleCouchBaseKey;
import com.voxlearning.utopia.service.zone.api.entity.ClassCircleCouchBaseRecord;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivity;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chensn
 * @date 2018-10-30 14:15
 */
@ServiceVersion(version = "20181030")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClazzActivityService {

    public List<ClazzActivity> getList(Long userId, Long schoolId, Long clazzId);

    public ClazzActivity getActivity(Integer activityId);

    public void addOrUpdate(ClazzActivity clazzActivity);

    /**
     * 初始化和更改活动基础信息接口
     *
     * @param userId
     * @param schoolId
     * @param clazzId
     * @param activityId
     * @param status
     * @param condition
     */
    public void addOrUpdateRecord(Long userId, Long schoolId, Long clazzId, Integer activityId, Integer status, Map<String, Object> condition);

    /**
     * 修改字段属性接口，无任何其他业务逻辑
     *
     * @param clazzActivityRecord
     */

    public void updateRecord(ClazzActivityRecord clazzActivityRecord);

    public ClazzActivityRecord findUserRecord(Long userId, Long schoolId, Long clazzId, Integer activityId);

    public void increase(Integer type) ;

    public Map <String, Long> loadLikedCounts() ;

    public Map<String, Object> findBySchooldId(Long schoolId, Integer activityId);

    public void saveOrUpdateCouchBaseToMongo(ClassCircleCouchBaseRecord classCircleCouchBaseRecord);

    public ClassCircleCouchBaseRecord queryCouchBase(String couchBase);

    public void saveOrUpdateCouchBaseKeyToMongo(String key);

    public List<ClassCircleCouchBaseKey>  queryCouchBaseKey();

    public Long loadByKey(String key);

    public void setValueByKey(String key, String value);

    public void  deleteCache(String key);

    public void deleteActivityListCache();

}
