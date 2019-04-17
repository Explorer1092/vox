package com.voxlearning.utopia.schedule.schedule;

import com.lambdaworks.redis.api.sync.RedisHashCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.nature.commons.lang.util.StringUtil;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.consumer.TeacherActivityServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.consumer.NewAccomplishmentLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkResultLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.entity.StudentRewardSendCount;
import com.voxlearning.utopia.service.parentreward.helper.ParentRewardHelper;
import com.voxlearning.utopia.service.psr.entity.termreport.StudentTermReport;
import com.voxlearning.utopia.service.psr.entity.termreport.TermReportPackage;
import com.voxlearning.utopia.service.psr.termreport.client.IPsrTermReportIPackageLoaderClient;
import com.voxlearning.utopia.service.psr.termreport.loader.PsrTermReportIPackageLoader;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.*;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.stringToDate;

@Named
@ScheduledJobDefinition(
        jobName = "老师奖学金活动抽取中奖老师的job",
        jobDescription = "每天早上6点运行",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 6 * * ?"
)
@ProgressTotalWork(100)
public class AutoSelectScholarshipTeacherJob extends ScheduledJobWithJournalSupport {

    @Inject private NewHomeworkLoaderClient homeworkLoader;
    @Inject private NewAccomplishmentLoaderClient accomplishmentLoader;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherActivityServiceClient tchActServiceCli;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoader;
    @Inject private NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;

    private UtopiaSql utopiaSql;
    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() {
        utopiaSql = UtopiaSqlFactory.instance().getUtopiaSql("hs_misc");
        redisCommands = RedisCommandsBuilder.getInstance().getRedisCommands("user-easemob");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        String lotteryTime = SafeConverter.toString(parameters.get("date"));
        Boolean runFinal = SafeConverter.toBoolean(parameters.get("runFinal"));// 是否生成总复习的奖学金名单

        if (runFinal)
            runFinalLottery();

        Date now = new Date();
        Date yesterday;
        if (StringUtils.isNotEmpty(lotteryTime)) {
            yesterday = stringToDate(lotteryTime, "yyyy-MM-dd");
        } else {
            // 自动跑的时候清除一下数据
            // 重置下每日奖学金的领取标志
            utopiaSql.withSql("UPDATE VOX_TEACHER_SCHOLARSHIP_RECORD SET DAILY_LOTTERY = 0 WHERE 1 = 1").executeUpdate();

            yesterday = DateUtils.addDays(now, -1);
        }

        // 保护一下
        Date startDate = stringToDate("2018-05-30", DateUtils.FORMAT_SQL_DATE);
        if (now.before(startDate))
            return;

        Date endDate = stringToDate("2018-06-28 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
        if (now.after(endDate))
            return;

        String yesterdayStrFormat = DateUtils.dateToString(yesterday, "MM月dd日");
        String yesterdayStr = DateUtils.dateToString(yesterday, "yyyy-MM-dd");

        List<Long> teacherIds = utopiaSql.withSql(
                "SELECT TEACHER_ID FROM VOX_TEACHER_SCHOLARSHIP_RECORD " +
                        " WHERE LAST_ASSIGN_DATE = ?" +
                        " AND (BASIC_REVIEW_NUM > 0 OR TERM_REVIEW_NUM > 0)")
                .useParamsArgs(yesterdayStr)
                .queryColumnValues(Long.class);

        Date start = DateUtils.addMonths(now, -30);
        DateRange monthRange = new DateRange(start, now);

        // 取已经得过奖的名单
        Set<Long> luckySet = tchActServiceCli.loadScholarshipDailyList()
                .stream()
                .map(d -> MapUtils.getLong(d, "teacherId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        while (teacherIds.size() > 0) {
            int selectIndex = RandomUtils.nextInt(teacherIds.size());
            Long selectTeacherId = teacherIds.get(selectIndex);
            // 选中的就移除
            teacherIds.remove(selectIndex);

            // 不能重复得奖
            if (luckySet.contains(selectTeacherId))
                continue;

            // 最后阶段判断假老师
            if (teacherLoaderClient.isFakeTeacher(selectTeacherId))
                continue;

            List<Long> teacherGroupIds = groupLoader.loadTeacherGroups(selectTeacherId, false)
                    .stream()
                    .map(GroupMapper::getId)
                    .collect(Collectors.toList());

            List<NewHomework.Location> locations = homeworkLoader.loadNewHomeworksByClazzGroupIds(teacherGroupIds)
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(l -> l.getType() == NewHomeworkType.Normal)
                    .filter(NewHomework.Location::isChecked)
                    .filter(l -> monthRange.contains(l.getCheckedTime()))
                    .sorted((l1, l2) -> {
                        Long checkTime2 = l2.getCheckedTime();
                        Long checkTime1 = l1.getCheckedTime();
                        return checkTime2.compareTo(checkTime1);
                    })
                    .collect(Collectors.toList());

            if (locations.size() < 2)
                continue;

            // 30天内最近两次作业人数都大于10
            if (!isCompleteNumLagerThan(locations.get(0)) || !isCompleteNumLagerThan(locations.get(1))) {
                continue;
            }

            // 副账号的要算在主账号头上
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(selectTeacherId);
            if (mainTeacherId == null || mainTeacherId == 0L)
                mainTeacherId = selectTeacherId;

            // 在最后阶段验证老师是不是认证的
            TeacherDetail selectTeacher = teacherLoaderClient.loadTeacherDetail(mainTeacherId);
            if (selectTeacher == null || selectTeacher.getAuthenticationState() != AuthenticationState.SUCCESS.getState())
                continue;

            Map<String, Object> awardDetail = new HashMap<>();
            awardDetail.put("teacherId", selectTeacher.getId());
            awardDetail.put("date", yesterdayStr);
            awardDetail.put("formatDate", yesterdayStrFormat);
            awardDetail.put("name", selectTeacher.fetchRealname());
            awardDetail.put("school", selectTeacher.getTeacherSchoolName());
            awardDetail.put("award", "500教育基金");
            awardDetail.put("img", selectTeacher.getProfile().getImgUrl());

            RedisHashCommands<String, Object> hashCommands = redisCommands.sync().getRedisHashCommands();
            String cacheKey = "TeacherScholarshipActivity:DailyAwards";
            hashCommands.hset(cacheKey, yesterdayStr, awardDetail);

            // 记录下日志，防止redis出问题找不到记录
            logger.info("TeacherScholarship:{} daily lottery winner is {}", yesterdayStr, selectTeacherId);
            break;
        }

    }

    private void runFinalLottery() {
        String[] rules = {"score", "assignNum", "finishRate"};// 三个考察指标
        String[] subjects = new String[]{"MATH", "ENGLISH", "CHINESE"};

        Map<String, Integer> subjectLimit = new HashMap<>();
        subjectLimit.put("score_MATH", 1);
        subjectLimit.put("score_CHINESE", 1);
        subjectLimit.put("score_ENGLISH", 2);
        subjectLimit.put("assignNum_ENGLISH", 1);
        subjectLimit.put("assignNum_MATH", 1);
        subjectLimit.put("finishRate_ENGLISH", 2);
        subjectLimit.put("finishRate_MATH", 1);
        subjectLimit.put("finishRate_CHINESE", 1);

        Map<String, String> orderCondt = new HashMap<>();
        orderCondt.put("score", "SCORE");
        orderCondt.put("assignNum", "TERM_REVIEW_NUM");
        orderCondt.put("finishRate", "FINISH_RATE");

        // 取已经得过奖的名单
        Set<Long> luckySet = tchActServiceCli.loadScholarshipDailyList()
                .stream()
                .map(d -> MapUtils.getLong(d, "teacherId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 排除的老师列表
        List<Long> excludeTeacherIds = new ArrayList<>(luckySet);
        excludeTeacherIds.add(13208599L);
        excludeTeacherIds.add(13221110L);
        excludeTeacherIds.add(12401763L);

        List<Map<String, Object>> finalAwardMap = new ArrayList<>();
        //for(String rule : rules){
        //for(String subject : subjects){
//                Integer limit = subjectLimit.get(rule + "_" + subject);
//                if(limit == null)
//                    continue;
//
//                String orderCon = orderCondt.get(rule);
//                String sql = " SELECT TEACHER_ID FROM VOX_TEACHER_SCHOLARSHIP_RECORD " +
//                        " WHERE SUBJECTS = :SUBJECT " +
//                        " AND MAX_GROUP_FINISH_NUM >= 30 " +
//                        ((!"CHINESE".equals(subject)) ? "AND BASIC_REVIEW_NUM > 0 " : "") +
//                        " AND TERM_REVIEW_NUM > 8 " +
//                        " AND FINISH_RATE > 0.7 " +
//                        " AND TEACHER_ID NOT IN (:IDS) " +
//                        " ORDER BY " + orderCon +" DESC " +
//                        " LIMIT " + limit;

        // 这是王老师定好的名单
        List<Long> teacherIds = Arrays.asList(13215268L,1853600L,1954402L,1772334L,1998960L,12201029L,13075226L,12695366L,16748L,13488527L);

//                while(true){
//                    Map<String,Object> params = new HashMap<>();
//                    params.put("SUBJECT",subject);
//                    params.put("IDS",excludeTeacherIds);
//
//                    teacherIds = utopiaSql.withSql(sql)
//                            .useParams(params)
//                            .queryColumnValues(Long.class);
//
//                    boolean onceAgain = false;
//                    for(Long teacherId : teacherIds){
//                        TeacherDetail selectTeacher = teacherLoaderClient.loadTeacherDetail(teacherId);
//                        if(selectTeacher == null || selectTeacher.getAuthenticationState() != AuthenticationState.SUCCESS.getState()){
//                            excludeTeacherIds.add(teacherId);
//                            onceAgain = true;
//                        }
//
//                        if(crmSummaryLoaderClient.isFakeTeacher(teacherId)){
//                            excludeTeacherIds.add(teacherId);
//                            onceAgain = true;
//                        }
//                    }
//
//                    if(!onceAgain)
//                        break;
//                }

        Function<Long, Map<String, Object>> finalLotteryFunc = id -> {
            if (luckySet.contains(id))
                return null;

            Map<String, Object> awardMap = new HashMap<>();
            awardMap.put("userId", id);

            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(id);
            awardMap.put("userName", teacher.fetchRealname());
            awardMap.put("schoolName", teacher.getTeacherSchoolName());

            String stuList;
            String subject = teacher.getSubject().name();

            /**
             * 优异复习奖 - 获奖老师班级的学生中，取学生的期末复习普通作业平均分高到低排序；
             */
            List<Long> teacherGroupIds = groupLoader.loadTeacherGroups(id, false)
                    .stream()
                    .map(GroupMapper::getId)
                    .collect(Collectors.toList());

            Map<Long, Integer> stuScoreMap = new HashMap<>();
            Map<Long, Integer> stuHwMap = new HashMap<>();

            homeworkLoader.loadNewHomeworksByClazzGroupIds(teacherGroupIds)
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(l -> subject.equals(l.getSubject().name()))
                    .filter(l -> l.getType() == NewHomeworkType.TermReview)
                    .forEach(l -> {
                        NewHomework homework = newHomeworkLoaderClient.loadNewHomework(l.getId());
                        newHomeworkResultLoaderClient.findByHomeworkForReport(homework)
                                .values()
                                .stream()
                                .filter(BaseHomeworkResult::isFinished)
                                .forEach(r -> {
                                    Integer score = r.processScore();
                                    if (score == null)
                                        return;

                                    Integer orgScore = stuScoreMap.get(r.getUserId());
                                    if (orgScore == null)
                                        orgScore = 0;

                                    stuScoreMap.put(r.getUserId(), orgScore + score);

                                    Integer orgHw = stuHwMap.get(r.getUserId());
                                    if (orgHw == null)
                                        orgHw = 0;

                                    stuHwMap.put(r.getUserId(), orgHw + 1);
                                });
                    });

            // 按平均分倒序排列
            List<Long> stuIds = new ArrayList<>(stuScoreMap.keySet());
            stuIds.sort((sId1, sId2) -> {
                double avgScore1 = (double) stuScoreMap.get(sId1) / stuHwMap.get(sId1);
                double avgScore2 = (double) stuScoreMap.get(sId2) / stuHwMap.get(sId2);

                return Double.compare(avgScore2, avgScore1);
            });

            List<Long> award1StudentIds = stuIds.subList(0, Math.min(stuIds.size(), 5));
            logger.info("AutoSelectScholarshipTeacherJob:teacherId-{},award stuIds1-{}",id,award1StudentIds);

            List<String> award1StudentNames = award1StudentIds.stream()
                    .map(b -> studentLoaderClient.loadStudent(b).fetchRealname())
                    .collect(Collectors.toList());

            stuList = StringUtils.join(award1StudentNames, "、");

            /**
             * 学生进步奖 - 剔除学期报告出勤率低于60%的学生，取学生的期末复习普通作业平均分与学期报告平均分的差值，由高到底排序
             */
            Map<Long, Integer> termScoreMap = new HashMap<>();
            Map<Long, Integer> termNumMap = new HashMap<>();
                    /*teacherGroupIds.forEach(gId -> {
                        TermReportPackage termPackage = psrTermReportIPackageLoader.loadTermtReportPackage(2017,1,gId.intValue(),subject);
                        logger.info("AutoSelectScholarshipTeacherJob:gId-{},reportNum:{}",gId,termPackage.getStudentTermReports().size());
                        termPackage.getStudentTermReports()
                                .stream()
                                .filter(tr -> tr.getAttendanceRate() < 0.6d)
                                .forEach(tp -> {
                                    Long stuId = Long.parseLong(tp.getStudentId());
                                    // 刨除第一次中奖的那些人
                                    if(award1StudentIds.contains(stuId))
                                        return;

                                    termScoreMap.put(Long.parseLong(tp.getStudentId()), tp.getAveScore());
                                });
                    });*/
            long startTime = stringToDate("2017-10-01 00:00:00").getTime();

            homeworkLoader.loadNewHomeworksByClazzGroupIds(teacherGroupIds)
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(l -> subject.equals(l.getSubject().name()))
                    .filter(l -> l.getType() == NewHomeworkType.Normal)
                    .filter(l -> l.getCheckedTime() > startTime)
                    .forEach(l -> {
                        NewHomework homework = newHomeworkLoaderClient.loadNewHomework(l.getId());
                        newHomeworkResultLoaderClient.findByHomeworkForReport(homework)
                                .values()
                                .stream()
                                .filter(BaseHomeworkResult::isFinished)
                                .forEach(r -> {
                                    Integer score = r.processScore();
                                    if (score == null)
                                        return;

                                    Integer orgHw = termNumMap.get(r.getUserId());
                                    if (orgHw == null)
                                        orgHw = 0;

                                    termNumMap.put(r.getUserId(), orgHw + 1);

                                    Integer orgScore = termScoreMap.get(r.getUserId());
                                    if (orgScore == null)
                                        orgScore = 0;

                                    termScoreMap.put(r.getUserId(), orgScore + score);
                                });
                    });

            stuIds = new ArrayList<>(termScoreMap.keySet());
            stuIds = stuIds.stream()
                    .filter(stuScoreMap::containsKey)
                    .filter(sId -> !award1StudentIds.contains(sId))
                    .sorted((sId1, sId2) -> {
                        double avgScore1 = (double) stuScoreMap.get(sId1) / stuHwMap.get(sId1);
                        double avgScore2 = (double) stuScoreMap.get(sId2) / stuHwMap.get(sId2);

                        double termScore1 = (double) termScoreMap.get(sId1) / termNumMap.get(sId1);
                        double termScore2 = (double) termScoreMap.get(sId2) / termNumMap.get(sId2);

                        double deduct1 = avgScore1 - termScore1;
                        double deduct2 = avgScore2 - termScore2;

                        return Double.compare(deduct2, deduct1);
                    })
                    .collect(Collectors.toList());

            List<Long> award2StudentIds = stuIds.subList(0, Math.min(stuIds.size(), 5));
            logger.info("AutoSelectScholarshipTeacherJob:teacherId-{},award stuIds2-{}",id,award2StudentIds);

            List<String> award2StudentNames = award2StudentIds.stream()
                    .map(b -> studentLoaderClient.loadStudent(b).fetchRealname())
                    .collect(Collectors.toList());

            if (award2StudentNames.size() > 0)
                stuList = stuList + "、" + StringUtils.join(award2StudentNames, "、");

            awardMap.put("stuList", stuList);

            luckySet.add(id);
            return awardMap;
        };


        List<Map<String, Object>> awardMapList = teacherIds.stream()
                .map(finalLotteryFunc)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        finalAwardMap.addAll(awardMapList);

        String cacheKey = "TeacherScholarshipActivity:FinalAwards";
        RedisStringCommands<String, Object> stringCommands = redisCommands.sync().getRedisStringCommands();
        stringCommands.set(cacheKey, finalAwardMap);
    }

    private boolean isCompleteNumLagerThan(NewHomework.Location location) {
        NewAccomplishment accomplishment = accomplishmentLoader.loadNewAccomplishment(location);

        int finishCount = 0;
        if (accomplishment != null && MapUtils.isNotEmpty(accomplishment.getDetails())) {
            finishCount = accomplishment.getDetails().size();
        }

        return finishCount >= 10;
    }
}
