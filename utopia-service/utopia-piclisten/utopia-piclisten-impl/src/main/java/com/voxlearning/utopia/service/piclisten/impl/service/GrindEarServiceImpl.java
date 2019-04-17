package com.voxlearning.utopia.service.piclisten.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.lambdaworks.redis.ScoredValue;
import com.lambdaworks.redis.api.async.RedisSortedSetAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.piclisten.api.GrindEarService;
import com.voxlearning.utopia.service.piclisten.cache.PiclistenCache;
import com.voxlearning.utopia.service.piclisten.impl.dao.StudentGrindEarRecordDao;
import com.voxlearning.utopia.service.piclisten.impl.dao.StudentGrindEarRecordV1Dao;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenReportDayResult;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRange;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRecord;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 磨耳朵服务
 *
 * @author jiangpeng
 * @since 2016-10-26 下午1:08
 **/
@Named
@ExposeServices({
        @ExposeService(interfaceClass = GrindEarService.class, version = @ServiceVersion(version = "4.0")),
        @ExposeService(interfaceClass = GrindEarService.class, version = @ServiceVersion(version = "5.0"))
})
@Service(interfaceClass = GrindEarService.class)
public class GrindEarServiceImpl extends SpringContainerSupport implements GrindEarService {

    @Inject private AsyncPiclistenCacheServiceImpl asyncVendorCacheService;

    @Inject
    private StudentGrindEarRecordDao studentGrindEarRecordDao;

    @Inject
    private StudentGrindEarRecordV1Dao studentGrindEarRecordV1Dao; //上一次活动的。。。

    @Inject
    private ParentSelfStudyServiceImpl parentSelfStudyService;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    private IRedisCommands redisCommands;

    private static String KEY_PREFIX = "grindEarRange2108Start_";


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder instance = RedisCommandsBuilder.getInstance();
        redisCommands = instance.getRedisCommands("parent-app");
    }

    @Override
    public StudentGrindEarRecord loadGrindEarRecord(Long studentId) {
        return studentGrindEarRecordDao.load(studentId);
    }

    @Override
    public AlpsFuture<Map<Long, StudentGrindEarRecord>> loadStudentGrindEarRecords(Collection<Long> studentIds) {
        Map<Long, StudentGrindEarRecord> loads = studentGrindEarRecordDao.loads(studentIds);
        return new ValueWrapperFuture<>(loads);
    }

    public void pushTodayRecord(Long studentId, Date date, Boolean checkLearnTime) {
        if (studentId == null || studentId == 0L)
            return;
        DayRange dayRange = DayRange.newInstance(date.getTime());
        if ( !asyncVendorCacheService.StudentGrindEarDayRecordCacheManager_hasRecord(studentId, date).getUninterruptibly()) {
            if (checkLearnTime) {
                PicListenReportDayResult reportDayResult = parentSelfStudyService.loadReportDayResult(studentId, dayRange).getUninterruptibly();
                if (reportDayResult != null && timeStandard(reportDayResult.getLearnTime())) {
                    doPushRecord(studentId, date);
                }
            }else {
                doPushRecord(studentId, date);
            }
        }
    }

    private void doPushRecord(Long studentId, Date date){
        StudentGrindEarRecord studentGrindEarRecord = studentGrindEarRecordDao.pushRecord(studentId, date);
        if (studentGrindEarRecord != null) {
            asyncVendorCacheService.StudentGrindEarDayRecordCacheManager_todayRecord(studentId, date);
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail != null && studentDetail.getClazz() != null && studentDetail.getClazz().getSchoolId() != null)
                updateRangedays(studentId, studentDetail.getClazz().getSchoolId(), studentGrindEarRecord.dayCount());
        }
    }

    @Override
    public void pushTodayRecord(Long studentId, Date date) {
        if (studentId == null || studentId == 0L)
            return;
        pushTodayRecord(studentId, date, true);
    }

    @Override
    public void mockPushRecord(Long studentId, Date date) {
        if (studentId == null || studentId == 0L)
            return;
        pushTodayRecord(studentId, date, false);
    }


    @Override
    public List<StudentGrindEarRecord> loadAll() {
        return studentGrindEarRecordDao.query();
    }


    @Override
    public Long grindEarStudentCount() {
        return studentGrindEarRecordDao.count();
    }


    @Override
    public Page<StudentGrindEarRange> loadSchoolRangePage(Pageable pageable, Long schoolId) {
        LinkedHashMap<Long, StudentGrindEarRange> schoolRankMap = getSchoolRankMap(schoolId);
        if (schoolRankMap == null)
            return new PageImpl<>(Collections.emptyList());
        Collection<StudentGrindEarRange> values = schoolRankMap.values();
        List<StudentGrindEarRange> list = new ArrayList<>(values);
        return PageableUtils.listToPage(list, pageable);
    }

    @Override
    public List<StudentGrindEarRange> rawLoadSchoolRangeThird(Long schoolId) {
        String key = generateRangeKey(schoolId);
        RedisSortedSetCommands<String, Object> redisSortedSetCommands =
                redisCommands.sync().getRedisSortedSetCommands();
        List<ScoredValue<Object>> scoredValues = redisSortedSetCommands.zrevrangeWithScores(key, 0, -1);
        List<StudentGrindEarRange> list = new ArrayList<>();
        int index = 1;
        StudentGrindEarRange previousRange = null;
        for (ScoredValue<Object> sv : scoredValues) {
            Double score = sv.score;
            long studentId = SafeConverter.toLong(sv.value);
            Long count = score.longValue();
            StudentGrindEarRange range = new StudentGrindEarRange();
            range.setDayCount(count);
            range.setStudentId(studentId);
            if (index == 1) {
                range.setRank(1);
            } else {
                Integer rank;
                Integer previousWrapperRank = previousRange.getRank();
                if (previousRange.getDayCount().equals(count))
                    rank = previousWrapperRank;
                else
                    rank = previousWrapperRank + 1;
                if (rank > 3)
                    break;
                range.setRank(rank);
            }
            previousRange = range;
            list.add(range);
            index ++;
        }
        return list;
    }

    private String generateSendIntegralKey(Long studentId, DayRange dayRange){
        return "grindEar_integral_send_"+ dayRange.toString() + "_" + studentId;
    }

    @Override
    public AlpsFuture<Boolean> todayIntegralIsSend(Long studentId, DayRange dayRange) {

        return new ValueWrapperFuture<>(innerTodayIntegralIsSend(studentId, dayRange));
    }

    private Boolean innerTodayIntegralIsSend(Long studentId, DayRange dayRange){
        CacheObject<Object> objectCacheObject = PiclistenCache.getPersistenceCache().get(generateSendIntegralKey(studentId, dayRange));
        if (objectCacheObject == null || objectCacheObject.getValue() == null)
            return false;
        return SafeConverter.toBoolean(objectCacheObject.getValue());
    }

    private void markTodaySendIntegral(Long studentId, DayRange dayRange){
        PiclistenCache.getPersistenceCache().set(generateSendIntegralKey(studentId, dayRange), 86400, "true");
    }

    private static List<Integer> integralList = new ArrayList<>();
    static {
        integralList.add(8);

        integralList.add(5);
        integralList.add(5);

        integralList.add(3);
        integralList.add(3);
        integralList.add(3);

        integralList.add(2);
        integralList.add(2);
        integralList.add(2);
        integralList.add(2);
        integralList.add(2);
        integralList.add(2);

        integralList.add(1);
        integralList.add(1);
        integralList.add(1);
        integralList.add(1);
        integralList.add(1);
        integralList.add(1);
        integralList.add(1);
        integralList.add(1);

    }
    @Override
    public Integer sendIntegral(StudentDetail studentDetail, DayRange dayRange) {
        if (innerTodayIntegralIsSend(studentDetail.getId(), dayRange))
            return null;
        Integer integralCount = RandomUtils.pickRandomElementFromList(integralList);
        MapMessage mapMessage = sendIntegral(studentDetail, integralCount, dayRange);
        if (!mapMessage.isSuccess())
            return null;
        markTodaySendIntegral(studentDetail.getId(), dayRange);
        return integralCount;
    }

    @Override
    public Boolean studentIsFinishTodayTask(Long studentId) {
        StudentGrindEarRecord studentGrindEarRecord = studentGrindEarRecordDao.load(studentId);
        if (studentGrindEarRecord == null)
            return false;
        if (CollectionUtils.isEmpty(studentGrindEarRecord.getDateList()))
            return false;
        DayRange today = DayRange.current();
        return studentGrindEarRecord.getDateList().stream().anyMatch(t -> {
            DayRange x = DayRange.newInstance(t.getTime());
            return x.equals(today);
        });
    }

    private MapMessage sendIntegral(Student student, Integer count, DayRange dayRange) {
        IntegralHistory integralHistory = new IntegralHistory();
        IntegralType integralType = IntegralType.GRIND_EAR_ACTIVITY;
        integralHistory.setUserId(student.getId());
        integralHistory.setIntegral(count);
        integralHistory.setIntegralType(integralType.getType());
        integralHistory.setComment(integralType.getDescription());
        if (!RuntimeMode.isTest())
            integralHistory.setUniqueKey(IntegralType.GRIND_EAR_ACTIVITY.name() + dayRange.toString() + student.getId());
        return userIntegralService.changeIntegral(student, integralHistory);
    }
    @Override
    public Integer loadStudentRank(Long schoolId, Long studentId){
        LinkedHashMap<Long, StudentGrindEarRange> schoolRankMap = getSchoolRankMap(schoolId);
        if (MapUtils.isEmpty(schoolRankMap))
            return 0;
        StudentGrindEarRange range = schoolRankMap.get(studentId);
        if (range == null)
            return 0;
        return range.getRank();
    }


    private LinkedHashMap<Long, StudentGrindEarRange> getSchoolRankMap(Long schoolId){
        String key = "grind_ear_school_rang_map_18start_" + schoolId;
        CacheObject<Object> objectCacheObject = PiclistenCache.getPiclistenCache().get(key);
        if (objectCacheObject != null && objectCacheObject.getValue() != null){
            Object value = objectCacheObject.getValue();
            if (value instanceof LinkedHashMap){
                return (LinkedHashMap<Long, StudentGrindEarRange>) value;
            }
        }
        LinkedHashMap<Long, StudentGrindEarRange> grindEarRangeLinkedHashMap = $getSchoolRankMap(schoolId);
        int expiry = 3600;
        if (RuntimeMode.lt(Mode.STAGING))
            expiry = 60;
        PiclistenCache.getPiclistenCache().set(key, expiry, grindEarRangeLinkedHashMap);
        return grindEarRangeLinkedHashMap;
    }


    private LinkedHashMap<Long, StudentGrindEarRange> $getSchoolRankMap(Long schoolId){
        String key = generateRangeKey(schoolId);
        RedisSortedSetCommands<String, Object> redisSortedSetCommands =
                redisCommands.sync().getRedisSortedSetCommands();
        List<ScoredValue<Object>> scoredValues = redisSortedSetCommands.zrevrangeWithScores(key, 0, -1);
        LinkedHashMap<Long, StudentGrindEarRange> studentRangeMap = new LinkedHashMap<>();
        int i = 1;
        StudentGrindEarRange previousRange = null;
        for (ScoredValue<Object> sv : scoredValues) {
            Double score = sv.score;
            long studentId = SafeConverter.toLong(sv.value);
            Long count = score.longValue();
            StudentGrindEarRange range = new StudentGrindEarRange();
            range.setDayCount(count);
            range.setStudentId(studentId);
            if (i == 1) {
                range.setRank(1);
            } else {
                Integer previousWrapperRank = previousRange.getRank();
                if (previousRange.getDayCount().equals(count))
                    range.setRank(previousWrapperRank);
                else
                    range.setRank(previousWrapperRank + 1);
            }
            previousRange = range;
            studentRangeMap.put(studentId, range);
            i++;
        }
        return studentRangeMap;
    }

    private void updateRangedays(Long studentId, Long schoolId, Long count){
        String key = generateRangeKey(schoolId);
        RedisSortedSetAsyncCommands<String, Object> redisSortedSetAsyncCommands =
                redisCommands.async().getRedisSortedSetAsyncCommands();
        redisSortedSetAsyncCommands.zadd(key, count, studentId.toString());
    }


    private String generateRangeKey(Long schoolId){
        return KEY_PREFIX + schoolId;
    }

    // 发之前奖励用的
    private static String LAST_KEY_PREFIX = "grindEarRange2107Winter_";
    private String generateRangeKey_last(Long schoolId){
        return LAST_KEY_PREFIX + schoolId;
    }

}
