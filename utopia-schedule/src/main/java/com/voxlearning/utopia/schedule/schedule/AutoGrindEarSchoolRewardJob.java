package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.piclisten.api.GrindEarService;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.constants.UserConstants;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRange;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRecord;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-10-24 上午11:51
 **/

@Named
@ScheduledJobDefinition(
        jobName = "发放21天阿分题英语活动学校排行榜",
        jobDescription = "手动执行, 不可以在 staging 跑哦",
        disabled = {Mode.UNIT_TEST},
        cronExpression = "0 0 2 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoGrindEarSchoolRewardJob extends ScheduledJobWithJournalSupport {


    @ImportService(interfaceClass = GrindEarService.class)
    private GrindEarService grindEarService;

    @Inject
    private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    private void myLogger(String str, Object... t){
        logger.info("sendGrindEarWinterReward " + str, t);
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        List<StudentGrindEarRecord> studentGrindEarRecordV1s = grindEarService.loadAll();
        Map<Long, StudentGrindEarRecord> grindEarRecordV1Map = studentGrindEarRecordV1s.stream().collect(Collectors.toMap(StudentGrindEarRecord::getId, Function.identity()));

//
//        Set<Long> resendRankRewardStudentIdSet = new HashSet<>();
//        Set<Long> resendClazzGroupRewardStudentIdSet = new HashSet<>();
//        List<Object> rankListObj = (List<Object>)parameters.get("rankList");
//        if (CollectionUtils.isNotEmpty(rankListObj))
//            rankListObj.forEach(t -> resendRankRewardStudentIdSet.add(SafeConverter.toLong(t)));
//        List<Object> clazzListObj = (List<Object>)parameters.get("clazzList");
//        if (CollectionUtils.isNotEmpty(clazzListObj))
//            clazzListObj.forEach(t -> resendClazzGroupRewardStudentIdSet.add(SafeConverter.toLong(t)));


        final boolean realSend = SafeConverter.toBoolean(parameters.get("realSend"), false);

        final AtomicInteger schoolCount = new AtomicInteger(0);
        final AtomicInteger schoolIntegralCount = new AtomicInteger(0);
        final AtomicInteger rewardClazzCount = new AtomicInteger(0);
        final AtomicInteger rewardClazzIntegralCount = new AtomicInteger(0);

        ConcurrentHashSet<Long> clazzIdSet = new ConcurrentHashSet<>();
        ConcurrentHashSet<Long> schoolIdSet = new ConcurrentHashSet<>();
        //第一遍跑出 clazzIdSet  schoolIdSet
        ISimpleProgressMonitor iSimpleProgressMonitor = progressMonitor.subTask(30, studentGrindEarRecordV1s.size());

        List<List<StudentGrindEarRecord>> taskRecordList = CollectionUtils.splitList(studentGrindEarRecordV1s, 30);
        int threadCount = taskRecordList.size();
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0 ; i< threadCount ;i++) {
            List<StudentGrindEarRecord> studentGrindEarRecordV1s1 = taskRecordList.get(i);
            AlpsThreadPool.getInstance().submit(() -> {
                try{
                    for (StudentGrindEarRecord recordV1 : studentGrindEarRecordV1s1) {
                        try{
                            Long studentId = recordV1.getId();
                            if (studentId == null || studentId == 0)
                                continue;
                            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                            if (studentDetail == null)
                                continue;
                            Clazz clazz = studentDetail.getClazz();
                            if (clazz == null)
                                continue;
                            clazzIdSet.add(clazz.getId());
                            schoolIdSet.add(clazz.getSchoolId());
                        }catch (Exception ex){

                        }finally {
                            iSimpleProgressMonitor.worked(1);
                        }
                        //处理
                    }
                }catch (Exception e){

                }finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        myLogger("first step : clazzIdSet and schoolIdSet is ok !");


        //发送学校排行榜奖励
        List<Long> schoolIdList = new ArrayList<>(schoolIdSet);
        List<List<Long>> lists = CollectionUtils.splitList(schoolIdList, 30);
        ISimpleProgressMonitor iSimpleProgressMonitor2 = iSimpleProgressMonitor.subTask(30, schoolIdList.size());
        threadCount = lists.size();
        final CountDownLatch countDownLatch1 = new CountDownLatch(threadCount);
        for (int i = 0 ; i< threadCount ;i++) {
            List<Long> schoolIds = lists.get(i);
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    for (Long schoolId : schoolIds) {
                        try {
                            List<StudentGrindEarRange> studentGrindEarRanges = grindEarService.rawLoadSchoolRangeThird(schoolId);
                            if (CollectionUtils.isNotEmpty(studentGrindEarRanges)) {
                                studentGrindEarRanges.forEach(range -> sendSchoolRankReward(range, schoolIntegralCount, realSend, grindEarRecordV1Map));
                                schoolCount.incrementAndGet();
                            }
                        }catch (Exception ex){
                            logger.error("deal schools error: " + ex);
                        }finally {
                            iSimpleProgressMonitor2.worked(1);
                        }
                    }
                }catch (Exception e){
                    logger.error("submit error ：", e);
                }finally {
                    countDownLatch1.countDown();
                }
            });
        }
        countDownLatch1.await();
        myLogger("second step: school rank reward ok!");

        //发送班级团体奖励
        List<Long> clazzIdList = new ArrayList<>(clazzIdSet);
        ISimpleProgressMonitor iSimpleProgressMonitor1 = iSimpleProgressMonitor.subTask(40, clazzIdList.size());
        List<List<Long>> lists1 = CollectionUtils.splitList(clazzIdList, 30);
        int threadCount2 = lists1.size();
        final CountDownLatch countDownLatch2 = new CountDownLatch(threadCount2);
        for (int i = 0 ; i < threadCount2; i++){
            List<Long> clazzIdTaskList = lists1.get(i);
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    for (Long clazzId : clazzIdTaskList) {
                        try {
                            ClazzRewardMapper clazzRewardMapper = clazzRewardInfo(clazzId, grindEarRecordV1Map);
                            if (clazzRewardMapper == null) {
                                continue;
                            }
                            int clazzIntegral = getClazzIntegral(clazzRewardMapper.getClazzStudentCount(), clazzRewardMapper.getClazzFinishCount());
                            if (clazzIntegral != 0) {
                                rewardClazzCount.incrementAndGet();
                                clazzRewardMapper.getFinishStudentIdSet().forEach(sid -> {
                                    MapMessage message = sendIntegral(sid, clazzIntegral, realSend, "clazz");
                                    if (message.isSuccess())
                                        rewardClazzIntegralCount.addAndGet(clazzIntegral);
                                });
                            }
                        } catch (Exception ex) {
                            logger.error("deal clazz error: " + ex);
                        } finally {
                            iSimpleProgressMonitor1.worked(1);
                        }
                    }
                }catch (Exception e){
                    logger.error("submit clazz error ：", e);
                }finally {
                    countDownLatch2.countDown();
                }
            });
        }
        countDownLatch2.await();
        myLogger("third step: clazz group reward ok!");
        myLogger("寒假磨耳朵活动，是否真正发送了学豆：{}", realSend);
        myLogger("寒假磨耳朵活动，排行榜奖励，学校总数：{}" , schoolCount.get());
        myLogger("寒假磨耳朵活动，排行榜奖励，学豆总数：{}" ,schoolIntegralCount.get());
        myLogger("寒假磨耳朵活动，班级团体奖励, 班级总数：{}", rewardClazzCount.get());
        myLogger("寒假磨耳朵活动，班级团体奖励, 学豆总数：{}", rewardClazzIntegralCount.get());
    }

    private void sendSchoolRankReward(StudentGrindEarRange rankInfo, AtomicInteger schoolIntegralCount, boolean realSend, Map<Long, StudentGrindEarRecord> map){
        try {
            if (rankInfo == null) {
                return;
            }
            if (rankInfo.getRank() > 3)
                return;
            Long studentId = rankInfo.getStudentId();
            StudentGrindEarRecord studentGrindEarRecordV1 = map.get(studentId);
            if (studentGrindEarRecordV1 == null)
                return;
            if (studentGrindEarRecordV1.dayCount() < 7) {
                return;
            }
            Integer rank = rankInfo.getRank();
            switch (rank){
                case 1 : if (sendIntegral(studentId, 20, realSend, "rank").isSuccess()){
                    schoolIntegralCount.addAndGet(20);
                }break;
                case 2 : if (sendIntegral(studentId, 10, realSend, "rank").isSuccess()){
                    schoolIntegralCount.addAndGet(10);
                }break;
                case 3 : if (sendIntegral(studentId, 5, realSend, "rank").isSuccess()){
                    schoolIntegralCount.addAndGet(5);
                }break;
            }
        }catch (Exception e){
            logger.error("handle data error : ", e);
        }
    }


    private MapMessage sendIntegral(Long studentId, Integer count, boolean realSend, String source) {
        if (!realSend)
            return MapMessage.successMessage();
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null)
            return MapMessage.errorMessage("error student");
        IntegralHistory integralHistory = new IntegralHistory();
        IntegralType integralType = IntegralType.GRIND_EAR_ACTIVITY;
        integralHistory.setUserId(student.getId());
        integralHistory.setIntegral(count);
        integralHistory.setIntegralType(integralType.getType());
        if ("rank".equals(source))
            integralHistory.setComment(integralType.getDescription() + ": 排行榜奖励");
        else
            integralHistory.setComment(integralType.getDescription() + ": 班级团体奖励");
//        if (!RuntimeMode.isUsingTestData())
            integralHistory.setUniqueKey(integralType.name() + "_18spring_" + source + student.getId());
        return userIntegralService.changeIntegral(student, integralHistory);
    }




    @Data
    private class ClazzRewardMapper{
        private Integer clazzFinishCount;
        private Integer clazzStudentCount;
        private Integer clazzRemain;
        private Integer clazzIntegral;
        private Set<Long> finishStudentIdSet;
    }

    private ClazzRewardMapper clazzRewardInfo(Long clazzId, Map<Long, StudentGrindEarRecord> map){
        List<Long> groupIds = deprecatedGroupLoaderClient.loadClazzGroups(clazzId).stream().map(GroupMapper::getId).collect(Collectors.toList());
        Map<Long, GroupMapper> groupMapperMap = deprecatedGroupLoaderClient.loadGroups(groupIds, true);
        List<GroupMapper.GroupUser> clazzUserList = groupMapperMap.values().stream().map(GroupMapper::getStudents).flatMap(Collection::stream).
                distinct().filter(t -> !t.getName().equals(UserConstants.EXPERIENCE_ACCOUNT_NAME)).
                sorted(Comparator.comparingLong(GroupMapper.GroupUser::getId)).collect(Collectors.toList());
        int clazzStudentCount = clazzUserList.size();
        Set<Long> studentIdSet = clazzUserList.stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
        Map<Long, StudentGrindEarRecord> studentGrindEarRecordMap = new HashMap<>();
        studentIdSet.forEach(t -> {
            StudentGrindEarRecord studentGrindEarRecordV1 = map.get(t);
            if (studentGrindEarRecordV1 == null)
                return;
            studentGrindEarRecordMap.put(t, studentGrindEarRecordV1);
        });
        int clazzFinishCount = studentGrindEarRecordMap.size();

        ClazzRewardMapper mapper = new ClazzRewardMapper();
        mapper.setClazzFinishCount(clazzFinishCount);
        mapper.setClazzIntegral(getClazzIntegral(clazzStudentCount, clazzFinishCount));
        mapper.setClazzRemain(clazzStudentCount - clazzFinishCount);
        mapper.setClazzStudentCount(clazzStudentCount);
        mapper.setFinishStudentIdSet(studentGrindEarRecordMap.keySet());
        return mapper;
    }

    private int getClazzIntegral(int clazzStudentCount, int clazzFinishCount) {
        if (clazzStudentCount == 0 )
            return 0;
        if (clazzFinishCount == 0 )
            return 0;
        int levelA = new BigDecimal(clazzStudentCount * 0.2).setScale(0, RoundingMode.HALF_UP).intValue();
        int levelB = new BigDecimal(clazzStudentCount * 0.4).setScale(0, RoundingMode.HALF_UP).intValue();
        int levelC = new BigDecimal(clazzStudentCount * 0.8).setScale(0, RoundingMode.HALF_UP).intValue();
        if (clazzFinishCount < levelA)
            return 0;
        if (clazzFinishCount >= levelA && clazzFinishCount < levelB)
            return 5;
        if (clazzFinishCount >= levelB && clazzFinishCount < levelC)
            return 10;
        if (clazzFinishCount >= levelC)
            return 15;
        return 0;
    }
}
