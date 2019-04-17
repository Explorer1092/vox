package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRange;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRecord;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 磨耳朵服务
 *
 * @author jiangpeng
 * @since 2016-10-25 下午9:42
 **/
@ServiceVersion(version = "5.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface GrindEarService {

    default String waiyanKey(Long studentId){
        return "grindEarBookIsWaiYan_" + studentId;
    }

    default Boolean timeStandard(Long millisSeconds){
        return millisSeconds != null && millisSeconds >= 5*60*1000;
    }


    @CacheMethod(
            writeCache = false,
            type = StudentGrindEarRecord.class
    )
    public StudentGrindEarRecord loadGrindEarRecord(@CacheParameter Long studentId);


    @Async
    public AlpsFuture<Map<Long, StudentGrindEarRecord>> loadStudentGrindEarRecords(Collection<Long> studentIds);

    @NoResponseWait
    public void pushTodayRecord(Long studentId, Date date);

    /**
     * 补数据
     * 外研社用，不用校验点读时长
     * @param studentId
     * @param date
     */
    @NoResponseWait
    public void mockPushRecord(Long studentId, Date date);


    @ServiceMethod(
            timeout = 30, unit = TimeUnit.SECONDS
    )
    public List<StudentGrindEarRecord> loadAll();


    /**
     * 获取参加磨耳朵总人数
     * 1小时缓存
     * @return
     */
    @CacheMethod(
            type = Long.class,
            expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 3600)
    )
    public Long grindEarStudentCount();

    /**
     * 获取学生排名
     * @param schoolId
     * @param studentId
     * @return
     */
    public Integer loadStudentRank(Long schoolId, Long studentId);

    /**
     * 获取学校排行榜
     * @param pageable
     * @param schoolId
     * @return
     */
    public Page<StudentGrindEarRange> loadSchoolRangePage(Pageable pageable, Long schoolId);

    public List<StudentGrindEarRange> rawLoadSchoolRangeThird(Long schoolId);

    /**
     * 今天的学豆奖励是否已领取
     * @param studentId
     * @return
     */
    @Async
    public AlpsFuture<Boolean> todayIntegralIsSend(Long studentId, DayRange dayRange);


    public Integer sendIntegral(StudentDetail studentDetail, DayRange dayRange);

    public Boolean studentIsFinishTodayTask(Long studentId);


}
