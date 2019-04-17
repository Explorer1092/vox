package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.clazz.client.DPClazzLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.BrainActivityService;
import com.voxlearning.utopia.service.zone.api.ClassCirclePlotService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivity;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRankLikeRecord;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import com.voxlearning.utopia.service.zone.api.entity.boss.AwardDetail;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossAward;
import com.voxlearning.utopia.service.zone.api.entity.giving.ChickenStudent;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotActivityBizObject;
import com.voxlearning.utopia.service.zone.impl.manager.ActivityClazzRankCacheManager;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityRankLikePersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.boss.ClazzBossAwardPersistence;
import com.voxlearning.utopia.service.zone.impl.queue.ClazzBossRewardQueueProducer;
import com.voxlearning.utopia.service.zone.impl.support.ClazzActivityRankLikeCacheManager;
import com.voxlearning.utopia.vo.ActivityRank;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chensn
 * @date 2018-11-10 16:58
 */

@Named
@ExposeServices({
        @ExposeService(interfaceClass = BrainActivityService.class, version = @ServiceVersion(version = "20181128")),
})
public class BrainActivityServiceImpl implements BrainActivityService {
    @Resource
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private ClazzActivityRecordPersistence clazzActivityRecordPersistence;
    @Inject
    private ActivityClazzRankCacheManager activityClazzRankCacheManager;
    @Inject
    private ClazzActivityPersistence clazzActivityPersistence;
    @Inject
    private DPClazzLoaderClient dpClazzLoaderClient;
    @Inject
    private ClazzBossAwardPersistence clazzBossAwardPersistence;
    @Inject
    private ClazzBossRewardQueueProducer clazzBossRewardQueueProducer;
    @Inject
    private ClazzActivityRankLikePersistence clazzActivityRankLikePersistence;
    @Inject
    private ClazzActivityRankLikeCacheManager clazzActivityRankLikeCacheManager;
    @Inject
    private ClassCirclePlotService classCirclePlotService;

    @Override
    public List<ActivityRank> getRank(Integer activityId, Long userId) {

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        List<ClazzActivityRecord> clazzActivityRecords = clazzActivityRecordPersistence.findByClazzId(activityId, studentDetail.getClazz().getSchoolId(), studentDetail.getClazzId());
        ClazzActivity clazzActivity = clazzActivityPersistence.load(activityId);
        if (clazzActivity != null && clazzActivity.getType() != null && clazzActivity.getType() == 3) {
            List<ActivityRank> activityRankList = clazzActivityRecords.stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.getBizObject() != null && e.getScore() != null && e.getScore() != 0)
                    .limit(30)
                    .map(e -> {
                        PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(e.getBizObject()), PlotActivityBizObject.class);
                        ActivityRank ar = new ActivityRank();
                        ar.setUserId(e.getUserId());
                        ar.setDiscription(clazzActivity.getRankDiscription());
                        ar.setNum(plotActivityBizObject.getCurrentHighestDiffiCult());
                        ar.setIsVip(plotActivityBizObject.getVip());
                        return ar;
                    }).sorted(Comparator.comparingInt(ActivityRank::getNum).reversed()).collect(Collectors.toList());
            return activityRankList;
        }
        //通用个人在班级排行
        if (clazzActivity != null) {
            List<ActivityRank> activityRankList = clazzActivityRecords.stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.getScore() != null && e.getScore() != 0)
                    .limit(30)
                    .map(e -> {
                        ActivityRank ar = new ActivityRank();
                        ar.setUserId(e.getUserId());
                        ar.setNum(e.getScore());
                        return ar;
                    }).sorted(Comparator.comparingInt(ActivityRank::getNum).reversed()).collect(Collectors.toList());
            return activityRankList;
        }
        return Collections.emptyList();
    }

    @Override
    public List<ActivityRank> getDailyRank(Integer activityId, Long userId, Date date) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        List<ActivityRank> clazzRank = activityClazzRankCacheManager.getRank(50, activityId, clazz.getSchoolId(), clazz.getClazzLevel().getLevel(), clazz.getId(), 1, 1, date);
        calculateIndex(clazzRank, activityId, 6, userId);
        return clazzRank;
    }

    @Override
    public ActivityRank getSelfDailyRank(Integer activityId, Long userId, Date date) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        ActivityRank selfClazzRank = activityClazzRankCacheManager.getSelfRank(activityId, clazz.getSchoolId(), clazz.getClazzLevel().getLevel(), clazz.getId(), userId, 1, 1, date);
        selfClazzRank.setUserName(studentDetail.fetchRealname());
        selfClazzRank.setLikeNum(SafeConverter.toInt(clazzActivityRankLikeCacheManager.loadLikeCount(activityId, 6, userId.toString(), true)));
        String likeId = ClazzActivityRankLikeRecord.generateId(userId, activityId, 6, selfClazzRank.getUserId().toString(), DateUtils.dateToString(new Date(), "yyyy-MM-dd"));
        selfClazzRank.setIsLike(clazzActivityRankLikePersistence.load(likeId) != null);
        return selfClazzRank;
    }

    @Override
    public List<ActivityRank> getClazzRank(Integer activityId, Long userId, Integer timeType, Date date) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        List<ActivityRank> clazzRank = activityClazzRankCacheManager.getRank(50, activityId, clazz.getSchoolId(), clazz.getClazzLevel().getLevel(), null, 3, timeType, date);
        Integer rankType = timeType == null ? 3 : 3 + timeType * 5;
        calculateIndex(clazzRank, activityId, rankType, userId);
        return clazzRank;
    }

    @Override
    public ActivityRank getSelfClazzRank(Integer activityId, Long userId, Integer timeType, Date date) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        ActivityRank selfClazzRank = activityClazzRankCacheManager.getSelfRank(activityId, clazz.getSchoolId(), clazz.getClazzLevel().getLevel(), null, clazz.getId(), 3, timeType, date);
        selfClazzRank.setClazzName(clazz.getClassName());
        selfClazzRank.setGrade(clazz.formalizeClazzName());
        Integer rankType = timeType == null ? 3 : 3 + timeType * 5;
        selfClazzRank.setLikeNum(SafeConverter.toInt(clazzActivityRankLikeCacheManager.loadLikeCount(activityId, rankType, clazz.getId().toString(), rankType > 5)));
        String likeId = ClazzActivityRankLikeRecord.generateId(userId, activityId, rankType, selfClazzRank.getClazzId().toString(), rankType > 5 ? DateUtils.dateToString(new Date(), "yyyy-MM-dd") : null);
        selfClazzRank.setIsLike(clazzActivityRankLikePersistence.load(likeId) != null);
        return selfClazzRank;
    }

    @Override
    public List<ActivityRank> getPersonInLevelRank(Integer activityId, Long userId, Integer timeType, Date date) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        List<ActivityRank> clazzRank = activityClazzRankCacheManager.getRank(50, activityId, clazz.getSchoolId(), clazz.getClazzLevel().getLevel(), null, 2, timeType, date);
        Integer rankType = timeType == null ? 2 : 2 + timeType * 5;
        calculateIndex(clazzRank, activityId, rankType, userId);
        return clazzRank;
    }

    @Override
    public ActivityRank getSelfPersonInLevelRank(Integer activityId, Long userId, Integer timeType, Date date) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        ActivityRank selfClazzRank = activityClazzRankCacheManager.getSelfRank(activityId, clazz.getSchoolId(), clazz.getClazzLevel().getLevel(), null, userId, 2, timeType, date);
        selfClazzRank.setUserName(studentDetail.fetchRealname());
        Integer rankType = timeType == null ? 2 : 2 + timeType * 5;
        selfClazzRank.setLikeNum(SafeConverter.toInt(clazzActivityRankLikeCacheManager.loadLikeCount(activityId, rankType, userId.toString(), rankType > 5)));
        String likeId = ClazzActivityRankLikeRecord.generateId(userId, activityId, rankType, selfClazzRank.getUserId().toString(), rankType > 5 ? DateUtils.dateToString(new Date(), "yyyy-MM-dd") : null);
        selfClazzRank.setIsLike(clazzActivityRankLikePersistence.load(likeId) != null);
        return selfClazzRank;
    }

    @Override
    public List<ActivityRank> getClazzInSchoolRank(Integer activityId, Long userId, Integer timeType, Date date) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        List<ActivityRank> clazzRank = activityClazzRankCacheManager.getRank(50, activityId, clazz.getSchoolId(), clazz.getClazzLevel().getLevel(), null, 5, timeType, date);
        Integer rankType = timeType == null ? 5 : 5 + timeType * 5;
        calculateIndex(clazzRank, activityId, rankType, userId);
        return clazzRank;
    }

    @Override
    public ActivityRank getSelfClazzInSchoolRank(Integer activityId, Long userId, Integer timeType, Date date) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        ActivityRank selfClazzRank = activityClazzRankCacheManager.getSelfRank(activityId, clazz.getSchoolId(), clazz.getClazzLevel().getLevel(), null, clazz.getId(), 5, timeType, date);
        selfClazzRank.setClazzName(clazz.getClassName());
        selfClazzRank.setGrade(clazz.formalizeClazzName());
        Integer rankType = timeType == null ? 5 : 5 + timeType * 5;
        selfClazzRank.setLikeNum(SafeConverter.toInt(clazzActivityRankLikeCacheManager.loadLikeCount(activityId, rankType, clazz.getId().toString(), rankType > 5)));
        String likeId = ClazzActivityRankLikeRecord.generateId(userId, activityId, rankType, selfClazzRank.getClazzId().toString(), rankType > 5 ? DateUtils.dateToString(new Date(), "yyyy-MM-dd") : null);
        selfClazzRank.setIsLike(clazzActivityRankLikePersistence.load(likeId) != null);
        return selfClazzRank;
    }

    @Override
    public List<ActivityRank> getPersonInSchoolRank(Integer activityId, Long userId, Integer timeType, Date date) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        List<ActivityRank> clazzRank = activityClazzRankCacheManager.getRank(50, activityId, clazz.getSchoolId(), null, null, 4, timeType, date);
        Integer rankType = timeType == null ? 4 : 4 + timeType * 5;
        calculateIndex(clazzRank, activityId, rankType, userId);
        return clazzRank;
    }

    @Override
    public ActivityRank getSelfPersonInSchoolRank(Integer activityId, Long userId, Integer timeType, Date date) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        ActivityRank selfClazzRank = activityClazzRankCacheManager.getSelfRank(activityId, clazz.getSchoolId(), null, null, userId, 4, timeType, date);
        selfClazzRank.setUserName(studentDetail.fetchRealname());
        Integer rankType = timeType == null ? 4 : 4 + timeType * 5;
        selfClazzRank.setLikeNum(SafeConverter.toInt(clazzActivityRankLikeCacheManager.loadLikeCount(activityId, rankType, userId.toString(), rankType > 5)));
        String likeId = ClazzActivityRankLikeRecord.generateId(userId, activityId, rankType, selfClazzRank.getUserId().toString(), rankType > 5 ? DateUtils.dateToString(new Date(), "yyyy-MM-dd") : null);
        selfClazzRank.setIsLike(clazzActivityRankLikePersistence.load(likeId) != null);
        return selfClazzRank;
    }

    @Override
    public void updateAllRank(Integer activityId, Long schoolId, Integer level, Long clazzId, Long userId, Integer num) {
        //个人 年级榜 (总榜)
        activityClazzRankCacheManager.updateRank(activityId, schoolId, userId, level, null, num, 2, 0);
        //班级 年级榜 (总榜)
        activityClazzRankCacheManager.updateRank(activityId, schoolId, clazzId, level, null, num, 3, 0);
        //个人 全校榜 (总榜)
        activityClazzRankCacheManager.updateRank(activityId, schoolId, userId, null, null, num, 4, 0);
        //班级 全校榜 (总榜)
        activityClazzRankCacheManager.updateRank(activityId, schoolId, clazzId, null, null, num, 5, 0);

        //个人 班级榜 (日榜)
        activityClazzRankCacheManager.updateRank(activityId, schoolId, userId, level, clazzId, num, 1, 1);
        //个人 年级榜 (日榜)
        activityClazzRankCacheManager.updateRank(activityId, schoolId, userId, level, null, num, 2, 1);
        //班级 年级榜 (日榜)
        activityClazzRankCacheManager.updateRank(activityId, schoolId, clazzId, level, null, num, 3, 1);
        //个人 全校榜 (日榜)
        activityClazzRankCacheManager.updateRank(activityId, schoolId, userId, null, null, num, 4, 1);
        //班级 全校榜 (日榜)
        activityClazzRankCacheManager.updateRank(activityId, schoolId, clazzId, null, null, num, 5, 1);
    }

    public MapMessage getReward(Long userId) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        ClazzActivity clazzActivity = clazzActivityPersistence.findTheLastActivity();
        if (clazzActivity == null) {
            return MapMessage.errorMessage("活动不存在");
        }
        Date today = new Date();
        Date dateLimit = DateUtils.nextDay(clazzActivity.getEndDate(), 5);
        if (today.before(clazzActivity.getEndDate()) || today.after(dateLimit)) {
            //如果活动没结束 或者 活动结束超过5天
            return MapMessage.errorMessage("活动不存在");
        }
        List<Map<String, Object>> notice = new ArrayList<>();
        Integer activityId = clazzActivity.getId();
        if (clazzActivity.getType() == 4) {
            ClazzActivityRecord record = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId, clazz.getSchoolId(), clazz.getId(), userId));
            ActivityRank selfPersonInLevelRank = getSelfPersonInLevelRank(activityId, userId, 0, null);
            ActivityRank selfClazzInSchoolRank = getSelfClazzInSchoolRank(activityId, userId, 0, null);
            if (record != null && record.getBizObject() != null) {
                if (clazzActivity.getType() == 4) {
                    ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(record.getBizObject()), ChickenStudent.class);
                    if (chickenStudent.getIsReceiedPerson() == null || !chickenStudent.getIsReceiedPerson()) {
                        //个人奖励
                        List<ClazzBossAward> personReward = clazzBossAwardPersistence.getList(activityId, 2);
                        AtomicReference<Boolean> isPersonRecived = new AtomicReference<>(false);
                        personReward.stream().filter(Objects::nonNull).forEach(e -> {
                            if (selfPersonInLevelRank.getIndex() != null && !isPersonRecived.get() && selfPersonInLevelRank.getIndex() <= e.getTargetValue()) {
                                Map<String, Object> clazzRewardMap = new HashMap<>();
                                clazzRewardMap.put("msg", StringUtils.formatMessage("在感恩节火鸡快“跑”活动中,你在个人贡献榜上排名第{}，获得了以下奖励:", selfPersonInLevelRank.getIndex()));
                                clazzRewardMap.put("type", 1);
                                clazzRewardMap.put("reward", e.getAwards());
                                notice.add(clazzRewardMap);
                                isPersonRecived.set(true);
                            }
                        });
                    }

                    if (chickenStudent.getIsReceiedClazz() == null || !chickenStudent.getIsReceiedClazz()) {
                        if (chickenStudent.getJoinClass()) {
                            List<ClazzBossAward> clazzReward = clazzBossAwardPersistence.getList(activityId, 3);
                            AtomicReference<Boolean> isRecived = new AtomicReference<>(false);
                            clazzReward.stream().filter(Objects::nonNull).forEach(e -> {
                                if (selfClazzInSchoolRank.getIndex() != null && !isRecived.get() && selfClazzInSchoolRank.getIndex() <= e.getTargetValue()) {
                                    Map<String, Object> clazzRewardMap = new HashMap<>();
                                    clazzRewardMap.put("msg", StringUtils.formatMessage("在感恩节火鸡快“跑”活动中,你在班级贡献榜上排名第{}，获得了以下奖励:", selfClazzInSchoolRank.getIndex()));
                                    clazzRewardMap.put("type", 2);
                                    clazzRewardMap.put("reward", e.getAwards());
                                    notice.add(clazzRewardMap);
                                    isRecived.set(true);
                                }
                            });
                        }
                    }

                }

            }
        } else {
            ClazzActivityRecord record = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId, clazz.getSchoolId(), clazz.getId(), userId));
            if (record != null) {
                PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(record.getBizObject()), PlotActivityBizObject.class);
                if (plotActivityBizObject != null && plotActivityBizObject.getIsReceived() == null || !plotActivityBizObject.getIsReceived()) {
                    List<AwardDetail> awardDetails = classCirclePlotService.getLastDayRankReward(activityId, userId, clazzActivity.getEndDate());
                    if (CollectionUtils.isNotEmpty(awardDetails)) {
                        ActivityRank selfPersonInLevelRank = getSelfPersonInLevelRank(activityId, userId, 1, DateUtils.nextDay(clazzActivity.getEndDate(), -1));
                        Map<String, Object> rewardMap = new HashMap<>();
                        rewardMap.put("msg", StringUtils.formatMessage("在小王子邀请你星际探险活动中,你在个人贡献榜上排名第{}，获得了以下奖励:", (selfPersonInLevelRank.getIndex() == null ? 0 : selfPersonInLevelRank.getIndex())));
                        rewardMap.put("type", 1);
                        rewardMap.put("reward", awardDetails);
                        notice.add(rewardMap);
                    }
                }
            }
        }
        return MapMessage.successMessage().add("noticeList", notice).add("activityId", activityId);
    }

    @Override
    public MapMessage sendReward(Integer activityId, Long userId, Integer type) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        ClazzActivity clazzActivity = clazzActivityPersistence.load(activityId);
        ClazzActivityRecord record = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId, clazz.getSchoolId(), clazz.getId(), userId));
        if (record.getBizObject() != null) {
            if (clazzActivity.getType() == 4) {
                ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(record.getBizObject()), ChickenStudent.class);
                if (type == 1) {
                    if (chickenStudent.getIsReceiedPerson() != null && chickenStudent.getIsReceiedPerson()) {
                        return MapMessage.errorMessage("已经领取过个人奖励");
                    }
                    //个人奖励
                    List<ClazzBossAward> personReward = clazzBossAwardPersistence.getList(activityId, 2);
                    ActivityRank selfPersonInLevelRank = getSelfPersonInLevelRank(activityId, userId, 0, null);
                    AtomicReference<Boolean> isPersonRecived = new AtomicReference<>(false);
                    ChickenStudent finalChickenStudent = chickenStudent;
                    personReward.stream().filter(Objects::nonNull).forEach(e -> {
                        if (selfPersonInLevelRank.getIndex() != null && !isPersonRecived.get() && selfPersonInLevelRank.getIndex() <= e.getTargetValue()) {
                            List<AwardDetail> awardDetails = e.getAwards();
                            Map<String, Object> map = new HashMap<>();
                            awardDetails.forEach(award -> {
                                if (award.getType() == 1) {
                                    map.put("CHEST_GENERAL", award.getNum());
                                }
                                if (award.getType() == 2) {
                                    map.put("CHEST_MIDDLE", award.getNum());
                                }
                                if (award.getType() == 3) {
                                    map.put("CHEST_ADVANCED", award.getNum());
                                }
                                if (award.getType() == 4) {
                                    map.put("VW00003", award.getNum());
                                }
                            });
                            Map<String, Object> rewardSource = new HashMap<>();
                            rewardSource.put("type", "person");
                            rewardSource.put("rankIndex", selfPersonInLevelRank.getIndex());
                            sendReward(map, userId, rewardSource);
                            isPersonRecived.set(true);
                            finalChickenStudent.setIsReceiedPerson(true);
                            record.setBizObject(finalChickenStudent);
                            clazzActivityRecordPersistence.upsert(record);
                        }
                    });
                } else {
                    if (chickenStudent.getIsReceiedClazz() != null && chickenStudent.getIsReceiedClazz()) {
                        return MapMessage.errorMessage("已经领取过班级奖励");
                    }
                    if (!chickenStudent.getJoinClass()) {
                        return MapMessage.errorMessage("没有完成班级活动，不能领取班级奖励");
                    }
                    ActivityRank selfClazzInSchoolRank = getSelfClazzInSchoolRank(activityId, userId, 0, null);
                    //班级奖励
                    List<ClazzBossAward> clazzReward = clazzBossAwardPersistence.getList(activityId, 3);
                    AtomicReference<Boolean> isRecived = new AtomicReference<>(false);
                    ChickenStudent finalChickenStudent = chickenStudent;
                    clazzReward.stream().filter(Objects::nonNull).forEach(e -> {
                        if (selfClazzInSchoolRank.getIndex() != null && !isRecived.get() && selfClazzInSchoolRank.getIndex() <= e.getTargetValue()) {
                            List<AwardDetail> awardDetails = e.getAwards();
                            Map<String, Object> map = new HashMap<>();
                            awardDetails.forEach(award -> {
                                if (award.getType() == 1) {
                                    map.put("CHEST_GENERAL", award.getNum());
                                }
                                if (award.getType() == 2) {
                                    map.put("CHEST_MIDDLE", award.getNum());
                                }
                                if (award.getType() == 3) {
                                    map.put("CHEST_ADVANCED", award.getNum());
                                }
                                if (award.getType() == 4) {
                                    map.put("VW00003", award.getNum());
                                }
                            });
                            Map<String, Object> rewardSource = new HashMap<>();
                            rewardSource.put("type", "clazz");
                            rewardSource.put("rankIndex", selfClazzInSchoolRank.getIndex());
                            sendReward(map, userId, rewardSource);
                            isRecived.set(true);
                            finalChickenStudent.setIsReceiedClazz(true);
                            record.setBizObject(finalChickenStudent);
                            clazzActivityRecordPersistence.upsert(record);
                        }
                    });
                }
            } else {
                PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(record.getBizObject()), PlotActivityBizObject.class);
                if (plotActivityBizObject.getIsReceived() != null && plotActivityBizObject.getIsReceived()) {
                    return MapMessage.errorMessage("已经领取过个人奖励");
                }
                if (classCirclePlotService.sendEveryDayReward(activityId, userId, clazzActivity.getEndDate())) {
                    plotActivityBizObject.setIsReceived(true);
                    record.setBizObject(plotActivityBizObject);
                    clazzActivityRecordPersistence.upsert(record);
                }

            }
        } else {
            return MapMessage.errorMessage("没有参加活动，领取失败");
        }


        return MapMessage.successMessage("发送奖励成功");
    }

    @Override
    public MapMessage doRankLike(Integer activityId, Long userId, Integer rankType, String toObjectId) {
        ClazzActivityRankLikeRecord clazzActivityRankLikeRecord = new ClazzActivityRankLikeRecord();
        clazzActivityRankLikeRecord.setActivityId(activityId);
        clazzActivityRankLikeRecord.setRankType(rankType);
        clazzActivityRankLikeRecord.setToObjectId(toObjectId);
        clazzActivityRankLikeRecord.setUserId(userId);
        clazzActivityRankLikeRecord.setDailyString(rankType > 5 ? DateUtils.dateToString(new Date(), "yyyy-MM-dd") : null);
        clazzActivityRankLikeRecord.generateId();
        if (clazzActivityRankLikePersistence.load(clazzActivityRankLikeRecord.getId()) == null) {
            clazzActivityRankLikePersistence.insert(clazzActivityRankLikeRecord);
            clazzActivityRankLikeCacheManager.increaseLikeCount(activityId, rankType, toObjectId, rankType > 5);
        } else {
            return MapMessage.errorMessage("已经点过赞");
        }
        return MapMessage.successMessage("点赞成功");
    }

    private void calculateIndex(List<ActivityRank> clazzRank, Integer activityId, Integer rankType, Long userId) {
        ClazzActivity activity = clazzActivityPersistence.load(activityId);
        if (CollectionUtils.isNotEmpty(clazzRank)) {
            List<Long> clazzIds = clazzRank.stream().filter(Objects::nonNull).map(e -> e.getClazzId()).filter(Objects::nonNull).collect(Collectors.toList());
            List<Long> userIds = clazzRank.stream().filter(Objects::nonNull).map(e -> e.getUserId()).filter(Objects::nonNull).collect(Collectors.toList());
            List<ClazzActivityRankLikeRecord> userLikeRecord = clazzActivityRankLikePersistence.getUserLikeRecord(userId, activityId);
            Map<String, ClazzActivityRankLikeRecord> recordMap = userLikeRecord.stream().filter(Objects::nonNull).collect(Collectors.toMap(ClazzActivityRankLikeRecord::getId, Function.identity()));
            Map<Long, Clazz> clazzs = new HashMap<>();
            Map<Long, StudentDetail> students = new HashMap<>();
            if (CollectionUtils.isNotEmpty(clazzIds)) {
                clazzs = dpClazzLoaderClient.loadClazzs(clazzIds, true);
            }
            if (CollectionUtils.isNotEmpty(userIds)) {
                students = studentLoaderClient.loadStudentDetails(userIds);
            }
            int index = 1;
            for (ActivityRank ar : clazzRank) {
                Clazz clazz1 = ar.getClazzId() == null ? null : clazzs.get(ar.getClazzId());
                ar.setIndex(index++);
                if (clazz1 != null) {
                    ar.setClazzName(clazz1.getClassName());
                    ar.setGrade(clazz1.formalizeClazzName());
                    String likeId = ClazzActivityRankLikeRecord.generateId(userId, activityId, rankType, ar.getClazzId().toString(), rankType > 5 ? DateUtils.dateToString(new Date(), "yyyy-MM-dd") : null);
                    ar.setIsLike(recordMap.containsKey(likeId));
                    ar.setLikeNum(SafeConverter.toInt(clazzActivityRankLikeCacheManager.loadLikeCount(activityId, rankType, ar.getClazzId().toString(), rankType > 5)));
                }
                if (ar.getUserId() != null) {
//                    //剧情活动排行榜有vip字段
//                    if (activity.getType() == 3) {
//                        StudentDetail student = students.get(ar.getUserId());
//                        if (student != null && student.getClazz() != null) {
//                            ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId, student.getClazz().getSchoolId(), student.getClazz().getId(), ar.getUserId()));
//                            if (clazzActivityRecord != null && clazzActivityRecord.getBizObject() != null) {
//                                PlotActivityBizObject bizObject = JsonUtils.fromJson(JsonUtils.toJson(clazzActivityRecord.getBizObject()), PlotActivityBizObject.class);
//                                ar.setIsVip(bizObject.getVip());
//                            }
//                        }
//                    }
                    String likeId = ClazzActivityRankLikeRecord.generateId(userId, activityId, rankType, ar.getUserId().toString(), rankType > 5 ? DateUtils.dateToString(new Date(), "yyyy-MM-dd") : null);
                    ar.setIsLike(recordMap.containsKey(likeId));
                    ar.setLikeNum(SafeConverter.toInt(clazzActivityRankLikeCacheManager.loadLikeCount(activityId, rankType, ar.getUserId().toString(), rankType > 5)));
                }
            }
        }
    }


    public void sendReward(Map<String, Object> map, Long studentId, Map<String, Object> rewardSource) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> info = new HashMap<>();
        resultMap.put("rewards", map);
        resultMap.put("success", true);
        resultMap.put("event", "chicken_reward");
        resultMap.put("studentId", studentId);
        resultMap.put("subject", "all");
        resultMap.put("rewardSource", rewardSource);
        Message message = Message.newMessage();
        String json = JsonUtils.toJson(resultMap);
        message.withPlainTextBody(json);
        clazzBossRewardQueueProducer.getRewordProducer().produce(message);
    }

}
