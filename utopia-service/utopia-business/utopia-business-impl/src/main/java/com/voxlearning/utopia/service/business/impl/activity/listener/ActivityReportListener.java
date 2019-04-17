package com.voxlearning.utopia.service.business.impl.activity.listener;

import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.core.calendar.StopWatch;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.business.impl.activity.entity.ReportContext;
import com.voxlearning.utopia.service.business.impl.activity.listener.handler.GenSudokuBaseData;
import com.voxlearning.utopia.service.business.impl.activity.listener.handler.GenTangramBaseData;
import com.voxlearning.utopia.service.business.impl.activity.listener.handler.GenTwoFourBaseData;
import com.voxlearning.utopia.service.business.impl.activity.service.ActivityReportServiceClient;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportClassSnapshotData;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportCollectData;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.voxlearning.utopia.service.business.impl.activity.listener.ActivityReportProducer.ACTIVITY_REPORT_TOPIC;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = ACTIVITY_REPORT_TOPIC),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = ACTIVITY_REPORT_TOPIC)
        },
        maxPermits = 2
)
public class ActivityReportListener implements MessageListener, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ActivityReportListener.class);

    @Inject
    private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject
    private ActivityReportServiceClient activityReportServiceClient;
    @Inject
    private GenTangramBaseData genTangramBaseData;
    @Inject
    private GenTwoFourBaseData genTwoFourBaseData;
    @Inject
    private GenSudokuBaseData genSudokuBaseData;
    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql utopiaSqlOrder;

    @Override
    public void afterPropertiesSet() throws Exception {
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("hs_misc");
    }

    @Override
    public void onMessage(Message message) {
        StopWatch activityWatch = new StopWatch(true);
        String activityId = message.getBodyAsString();
        try {
            ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(activityId);
            execute(activityConfig);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            activityWatch.stop();
            long time = activityWatch.getTime(TimeUnit.MINUTES);
            logger.info("ActivityReportListener Total Time activityId:{} time:{} minutes", activityId, time);
        }
    }

    private void execute(ActivityConfig activityConfig) {
        String activityId = activityConfig.getId();

        try {
            if (activityConfig.getType() == ActivityTypeEnum.TANGRAM) {
                genTangramBaseData.execute(new ReportContext(activityConfig));
            } else if (activityConfig.getType() == ActivityTypeEnum.SUDOKU) {
                genSudokuBaseData.execute(new ReportContext(activityConfig));
            } else if (activityConfig.getType() == ActivityTypeEnum.TWENTY_FOUR) {
                genTwoFourBaseData.execute(new ReportContext(activityConfig));
            } else {
                //其他活动
            }

            try {
                calcAndSaveCollectData(activityId);
            } catch (Exception e) {
                logger.error("calcAndSaveCollectData Exception,id:" + activityId, e);
            }
        } catch (Exception e) {
            logger.error("活动" + activityId + "数据处理异常", e);
        }
    }

    private void calcAndSaveCollectData(String id) {
        StopWatch collecDataWatch = new StopWatch(true);

        genActivityReportCollecData(id);

        collecDataWatch.stop();
        logger.info("ActivityReportListener genActivityReportCollecData end. id:{} time:{} seconds", id, collecDataWatch.getTime(TimeUnit.SECONDS));
    }


    private void genActivityReportCollecData(String id) {
        String whereActivityId = " ACTIVITY_ID ='" + id + "' ";

        //按照活动ID和班级ID进行分组 统计做题总时间 ，题目总数量  ，参与次数，参与人数
        String TABLE = " VOX_ACTIVITY_REPORT_BASE_DATA ";
        String clazzTakeTimesSql = "SELECT province_code,province_name,city_code,city_name,region_code,region_name,school_id,school_name,clazz_level,clazz_id,clazz_name,activity_type,activity_id,\n" +
                "COUNT(DISTINCT(user_id)) AS participantStuds,\n" +
                "COUNT(1) AS participantTimes,\n" +
                "SUM(take_times) AS clazzTakeTimes,\n" +
                "SUM(exercises) AS clazzExercises\n" +
                "FROM " + TABLE +
                "WHERE" + whereActivityId +
                "GROUP BY clazz_id,activity_id\n";
        List<Map<String, Object>> clazzTakeTimesRes = utopiaSqlOrder.withSql(clazzTakeTimesSql).queryAll();
        List<ActivityReportCollectData> collectDatas = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(clazzTakeTimesRes)) {
            for (Map<String, Object> temp : clazzTakeTimesRes) {
                ActivityReportCollectData activityReportCollectData = new ActivityReportCollectData();
                Integer participantStuds = SafeConverter.toInt(temp.get("participantStuds"));
                Integer participantTimes = SafeConverter.toInt(temp.get("participantTimes"));
                Integer clazzTakeTimes = SafeConverter.toInt(temp.get("clazzTakeTimes"));
                Integer clazzExercises = SafeConverter.toInt(temp.get("clazzExercises"));
                Integer provinceCode = SafeConverter.toInt(temp.get("province_code"));
                String provinceName = SafeConverter.toString(temp.get("province_name"));
                Integer cityCode = SafeConverter.toInt(temp.get("city_code"));
                String cityName = SafeConverter.toString(temp.get("city_name"));
                Integer regionCode = SafeConverter.toInt(temp.get("region_code"));
                String regionName = SafeConverter.toString(temp.get("region_name"));
                Long schoolId = SafeConverter.toLong(temp.get("school_id"));
                String schoolName = SafeConverter.toString(temp.get("school_name"));
                Integer clazzLevel = SafeConverter.toInt(temp.get("clazz_level"));
                Long clazzId = SafeConverter.toLong(temp.get("clazz_id"));
                String clazzName = SafeConverter.toString(temp.get("clazz_name"));
                String activityType = SafeConverter.toString(temp.get("activity_type"));
                String activityId = SafeConverter.toString(temp.get("activity_id"));
                activityReportCollectData.setProvinceCode(provinceCode);
                activityReportCollectData.setProvinceName(provinceName);
                activityReportCollectData.setCityCode(cityCode);
                activityReportCollectData.setCityName(cityName);
                activityReportCollectData.setRegionCode(regionCode);
                activityReportCollectData.setRegionName(regionName);
                activityReportCollectData.setSchoolId(schoolId);
                activityReportCollectData.setSchoolName(schoolName);
                activityReportCollectData.setClazzLevel(clazzLevel);
                activityReportCollectData.setClazzId(clazzId);
                activityReportCollectData.setClazzName(clazzName);
                activityReportCollectData.setActivityId(activityId);
                activityReportCollectData.setParticipantTimes(participantTimes);
                activityReportCollectData.setParticipantStuds(participantStuds);
                activityReportCollectData.setClazzTakeTimes(clazzTakeTimes);
                activityReportCollectData.setClazzExercises(clazzExercises);
                collectDatas.add(activityReportCollectData);
            }
        }
//        activityReportServiceClient.saveActivityReportCollectDatas(collectDatas);
        Map<String, ActivityReportCollectData> collectDataMap = new LinkedHashMap<>();
        for (ActivityReportCollectData temp : collectDatas) {
            String key = SafeConverter.toString(temp.getClazzId()) + temp.getActivityId();
            collectDataMap.put(key, temp);
        }

        String topScoreStudSumSql = "SELECT clazz_id,activity_id,\n" +
                "COUNT(user_id) AS topScoreStudSum, \n" +
                "SUM(score) AS topScoreSum \n" +
                "FROM " + TABLE +
                "WHERE " + whereActivityId +
                "AND IS_TOP_SCORE=1 GROUP BY clazz_id,activity_id";
        List<Map<String, Object>> topScoreStudSumRes = utopiaSqlOrder.withSql(topScoreStudSumSql).queryAll();
        if (CollectionUtils.isNotEmpty(topScoreStudSumRes)) {
            for (Map<String, Object> temp : topScoreStudSumRes) {
                Long clazzId = SafeConverter.toLong(temp.get("clazz_id"));
                String activityId = SafeConverter.toString(temp.get("activity_id"));
                Integer topScoreStudSum = SafeConverter.toInt(temp.get("topScoreStudSum"));
                Integer topScoreSum = SafeConverter.toInt(temp.get("topScoreSum"));
                String key = SafeConverter.toString(clazzId) + activityId;
                ActivityReportCollectData collectData = collectDataMap.get(key);
                if (Objects.nonNull(collectData)) {
                    collectData.setTopScoreStudSum(topScoreStudSum);
                    collectData.setTopScoreSum(topScoreSum);
                }
            }
        }

        String scoreLevelStuds1Sql = "SELECT clazz_id,activity_id,\n" +
                "COUNT(user_id) AS scoreLevelStuds1 \n " +
                "FROM " + TABLE +
                "WHERE " + whereActivityId +
                "AND IS_TOP_SCORE=1 AND score>=0 AND score<=9  GROUP BY clazz_id,activity_id";
        List<Map<String, Object>> scoreLevelStuds1Res = utopiaSqlOrder.withSql(scoreLevelStuds1Sql).queryAll();
        if (CollectionUtils.isNotEmpty(scoreLevelStuds1Res)) {
            for (Map<String, Object> temp : scoreLevelStuds1Res) {
                Long clazzId = SafeConverter.toLong(temp.get("clazz_id"));
                String activityId = SafeConverter.toString(temp.get("activity_id"));
                Integer scoreLevelStuds1 = SafeConverter.toInt(temp.get("scoreLevelStuds1"));
                String key = SafeConverter.toString(clazzId) + activityId;
                ActivityReportCollectData collectData = collectDataMap.get(key);
                if (Objects.nonNull(collectData)) {
                    collectData.setScoreLevelStuds1(scoreLevelStuds1);
                }
            }
        }

        String scoreLevelStuds2Sql = "SELECT clazz_id,activity_id,\n" +
                "COUNT(user_id) AS scoreLevelStuds2 \n" +
                "FROM " + TABLE +
                "WHERE " + whereActivityId +
                "AND IS_TOP_SCORE=1 AND score>=10 AND score<=19  GROUP BY clazz_id,activity_id";
        List<Map<String, Object>> scoreLevelStuds2Res = utopiaSqlOrder.withSql(scoreLevelStuds2Sql).queryAll();
        if (CollectionUtils.isNotEmpty(scoreLevelStuds2Res)) {
            for (Map<String, Object> temp : scoreLevelStuds2Res) {
                Long clazzId = SafeConverter.toLong(temp.get("clazz_id"));
                String activityId = SafeConverter.toString(temp.get("activity_id"));
                Integer scoreLevelStuds2 = SafeConverter.toInt(temp.get("scoreLevelStuds2"));
                String key = SafeConverter.toString(clazzId) + activityId;
                ActivityReportCollectData collectData = collectDataMap.get(key);
                if (Objects.nonNull(collectData)) {
                    collectData.setScoreLevelStuds2(scoreLevelStuds2);
                }
            }
        }

        String scoreLevelStuds3Sql = "SELECT clazz_id,activity_id,\n" +
                "COUNT(user_id) AS scoreLevelStuds3 \n " +
                "FROM " + TABLE +
                "WHERE " + whereActivityId +
                "AND IS_TOP_SCORE=1 AND score>=20 AND score<=29 GROUP BY clazz_id,activity_id";
        List<Map<String, Object>> scoreLevelStuds3Res = utopiaSqlOrder.withSql(scoreLevelStuds3Sql).queryAll();
        if (CollectionUtils.isNotEmpty(scoreLevelStuds3Res)) {
            for (Map<String, Object> temp : scoreLevelStuds3Res) {
                Long clazzId = SafeConverter.toLong(temp.get("clazz_id"));
                String activityId = SafeConverter.toString(temp.get("activity_id"));
                Integer scoreLevelStuds3 = SafeConverter.toInt(temp.get("scoreLevelStuds3"));
                String key = SafeConverter.toString(clazzId) + activityId;
                ActivityReportCollectData collectData = collectDataMap.get(key);
                if (Objects.nonNull(collectData)) {
                    collectData.setScoreLevelStuds3(scoreLevelStuds3);
                }
            }
        }

        String scoreLevelStuds4Sql = "SELECT clazz_id,activity_id,\n" +
                "COUNT(user_id) AS scoreLevelStuds4 \n" +
                "FROM " + TABLE +
                "WHERE " + whereActivityId +
                "AND IS_TOP_SCORE=1 AND score>=30 AND score<=39 GROUP BY clazz_id,activity_id";
        List<Map<String, Object>> scoreLevelStuds4Res = utopiaSqlOrder.withSql(scoreLevelStuds4Sql).queryAll();
        if (CollectionUtils.isNotEmpty(scoreLevelStuds4Res)) {
            for (Map<String, Object> temp : scoreLevelStuds4Res) {
                Long clazzId = SafeConverter.toLong(temp.get("clazz_id"));
                String activityId = SafeConverter.toString(temp.get("activity_id"));
                Integer scoreLevelStuds4 = SafeConverter.toInt(temp.get("scoreLevelStuds4"));
                String key = SafeConverter.toString(clazzId) + activityId;
                ActivityReportCollectData collectData = collectDataMap.get(key);
                if (Objects.nonNull(collectData)) {
                    collectData.setScoreLevelStuds4(scoreLevelStuds4);
                }
            }
        }

        String scoreLevelStuds5Sql = "SELECT clazz_id,activity_id,\n" +
                "COUNT(user_id) AS scoreLevelStuds5 \n" +
                "FROM " + TABLE +
                "WHERE " + whereActivityId +
                "AND IS_TOP_SCORE=1 AND score>=40 AND score<=49 GROUP BY clazz_id,activity_id";
        List<Map<String, Object>> scoreLevelStuds5Res = utopiaSqlOrder.withSql(scoreLevelStuds5Sql).queryAll();
        if (CollectionUtils.isNotEmpty(scoreLevelStuds5Res)) {
            for (Map<String, Object> temp : scoreLevelStuds5Res) {
                Long clazzId = SafeConverter.toLong(temp.get("clazz_id"));
                String activityId = SafeConverter.toString(temp.get("activity_id"));
                Integer scoreLevelStuds5 = SafeConverter.toInt(temp.get("scoreLevelStuds5"));
                String key = SafeConverter.toString(clazzId) + activityId;
                ActivityReportCollectData collectData = collectDataMap.get(key);
                if (Objects.nonNull(collectData)) {
                    collectData.setScoreLevelStuds5(scoreLevelStuds5);
                }
            }
        }

        String scoreLevelStuds6Sql = "SELECT clazz_id,activity_id,\n" +
                "COUNT(user_id) AS scoreLevelStuds6 \n" +
                "FROM " + TABLE +
                "WHERE " + whereActivityId +
                "AND IS_TOP_SCORE=1 AND score>=50 GROUP BY clazz_id,activity_id";
        List<Map<String, Object>> scoreLevelStuds6Res = utopiaSqlOrder.withSql(scoreLevelStuds6Sql).queryAll();
        if (CollectionUtils.isNotEmpty(scoreLevelStuds6Res)) {
            for (Map<String, Object> temp : scoreLevelStuds6Res) {
                Long clazzId = SafeConverter.toLong(temp.get("clazz_id"));
                String activityId = SafeConverter.toString(temp.get("activity_id"));
                Integer scoreLevelStuds6 = SafeConverter.toInt(temp.get("scoreLevelStuds6"));
                String key = SafeConverter.toString(clazzId) + activityId;
                ActivityReportCollectData collectData = collectDataMap.get(key);
                if (Objects.nonNull(collectData)) {
                    collectData.setScoreLevelStuds6(scoreLevelStuds6);
                }
            }
        }
        //scoreLevel1为空的设置为0
        for (ActivityReportCollectData temp : collectDatas) {
            if (Objects.isNull(temp.getScoreLevelStuds1())) {
                temp.setScoreLevelStuds1(0);
            }
            if (Objects.isNull(temp.getScoreLevelStuds2())) {
                temp.setScoreLevelStuds2(0);
            }
            if (Objects.isNull(temp.getScoreLevelStuds3())) {
                temp.setScoreLevelStuds3(0);
            }
            if (Objects.isNull(temp.getScoreLevelStuds4())) {
                temp.setScoreLevelStuds4(0);
            }
            if (Objects.isNull(temp.getScoreLevelStuds5())) {
                temp.setScoreLevelStuds5(0);
            }
            if (Objects.isNull(temp.getScoreLevelStuds6())) {
                temp.setScoreLevelStuds6(0);
            }
        }

        activityReportServiceClient.deleteCollectDataByActivityId(id);
        activityReportServiceClient.saveActivityReportCollectDatas(collectDatas);

        saveClassSnapshotData(id, collectDatas);
    }

    private void saveClassSnapshotData(String id, List<ActivityReportCollectData> collectDatas) {
        StopWatch stopWatch = new StopWatch(true);
        String curDate = DateFormatUtils.format(new Date(), "yyyyMMdd");
        for (ActivityReportCollectData collectData : collectDatas) {
            try {
                ActivityReportClassSnapshotData snapshotData = new ActivityReportClassSnapshotData();
                snapshotData.setActivityId(collectData.getActivityId());
                snapshotData.setClazzId(collectData.getClazzId());
                snapshotData.setCurDate(curDate);
                if (collectData.getTopScoreStudSum() != null && collectData.getTopScoreSum() != null) {
                    double result = 0;
                    if (collectData.getTopScoreStudSum() != 0) {
                        BigDecimal divide = new BigDecimal(collectData.getTopScoreSum()).divide(new BigDecimal(collectData.getTopScoreStudSum()), 2, BigDecimal.ROUND_HALF_EVEN);
                        result = divide.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                    snapshotData.setAvgScore(result);
                }
                if (collectData.getClazzTakeTimes() != null && collectData.getClazzExercises() != null) {
                    double result = 0;
                    if (collectData.getClazzExercises() != 0) {
                        BigDecimal divide = new BigDecimal(collectData.getClazzTakeTimes()).divide(new BigDecimal(collectData.getClazzExercises()), 2, BigDecimal.ROUND_HALF_EVEN);
                        result = divide.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                    snapshotData.setAvgTime(result);
                }
                activityReportServiceClient.saveActivityReportClassSnapshotData(snapshotData);
            } catch (Exception e) {
                logger.error("保存班级每日活动成绩快照异常", e);
            }
        }
        stopWatch.stop();
        logger.info("ActivityReportListener ActivityReportClassSnapshotData end. id:{} time:{} seconds", id, stopWatch.getTime(TimeUnit.SECONDS));
    }

}
