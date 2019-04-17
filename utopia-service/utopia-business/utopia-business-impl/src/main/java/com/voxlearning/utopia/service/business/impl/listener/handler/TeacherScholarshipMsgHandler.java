package com.voxlearning.utopia.service.business.impl.listener.handler;


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.cache.TeacherScholarshipRecordCache;
import com.voxlearning.utopia.entity.activity.TeacherAssignTermReviewRecord;
import com.voxlearning.utopia.entity.activity.TeacherScholarshipRecord;
import com.voxlearning.utopia.service.business.impl.listener.TeacherHomeworkMsgHandler;
import com.voxlearning.utopia.service.business.impl.service.TeacherActivityServiceImpl;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkResultLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.consumer.NewAccomplishmentLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType.BasicReview;
import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType.TermReview;

/**
 * 老师奖学金活动对作业事件的处理
 */
@Named
public class TeacherScholarshipMsgHandler extends SpringContainerSupport implements TeacherHomeworkMsgHandler {

    private final Logger logger = LoggerFactory.getLogger(TeacherScholarshipMsgHandler.class);
    public static Date TERM_REVIEW_START_DATE = DateUtils.stringToDate("2018-12-10 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
    public static Date TERM_REVIEW_END_DATE = DateUtils.stringToDate("2019-01-10 23:59:59", DateUtils.FORMAT_SQL_DATETIME);

    @Inject private RaikouSDK raikouSDK;

    @Inject private NewHomeworkLoader homeworkLoader;
    @Inject private TeacherActivityServiceImpl tchActService;
    @Inject private TeacherLoaderClient teacherLoader;
    @Inject private NewHomeworkResultLoader homeworkResultLoader;
    @Inject private NewAccomplishmentLoaderClient accomplishmentLoader;
    @Inject private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;

    @AlpsPubsubPublisher(topic = "utopia.business.activity.scholarship.topic")
    private MessagePublisher messagePublisher;

    private static final String CACHE_KEY_FINAL_ATTEND_NUM = "TeacherScholarshipActivity:FinalAttendNum";


    @Override
    public void handle(Map<String, Object> msgMap) {
        Long teacherId = MapUtils.getLong(msgMap, "teacherId");
        String homeworkType = MapUtils.getString(msgMap, "homeworkType");
        String homeworkId = MapUtils.getString(msgMap, "homeworkId");
        Long groupId = MapUtils.getLong(msgMap, "groupId");

        try {
            String messageType = MapUtils.getString(msgMap, "messageType");
            switch (messageType) {
                case "assign": {
                    String subject = MapUtils.getString(msgMap, "subject");
                    processAssignHomeworkMsg(teacherId, homeworkType, subject, groupId, homeworkId);
                    break;
                }
                // 如果有消息撞一起了，这个是重试用的
                case "retryAssign": {
                    String subject = MapUtils.getString(msgMap, "subject");
                    processAssignHomeworkMsg(teacherId, homeworkType, subject, groupId, homeworkId);
                    break;
                }
                case "checked": {
                    int finishNum = MapUtils.getInteger(msgMap, "finishCount");
                    processCheckHomeworkMsg(teacherId, homeworkId, groupId, finishNum);
                    break;
                }
                case "deleted": {
                    processDeletedHomeworkMsg(teacherId, homeworkType, groupId, homeworkId);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("TeacherScholarshipActivity:Process home work msg error!", e);
        }
    }

    private Long getMainTeacherId(Long teacherId) {
        Long mainTeacherId = teacherLoader.loadMainTeacherId(teacherId);
        if (mainTeacherId == null || mainTeacherId == 0L)
            mainTeacherId = teacherId;

        return mainTeacherId;
    }

    private void processDeletedHomeworkMsg(Long teacherId, String homeworkTypeCode, Long groupId, String homeworkId) {
        NewHomeworkType homeworkType = NewHomeworkType.of(homeworkTypeCode);
        if (!isValidHomeworkType(homeworkType))
            return;

        //根据homeworkId删除缓存记录
        List<TeacherAssignTermReviewRecord> records = TeacherScholarshipRecordCache.loadRecords(teacherId);
        if (Objects.nonNull(records)) {
            for (TeacherAssignTermReviewRecord record : records) {
                if (Objects.equals(record.getHomeworkId(), homeworkId) && Objects.equals(groupId, record.getGroupId())) {
                    records.remove(record);
                    break;
                }
            }
            TeacherScholarshipRecordCache.updateRecords(records, teacherId);
        }

        TeacherScholarshipRecord record = tchActService.loadTeacherScholarshipRecord(teacherId);
        if (record == null)
            return;

        int termReviewNumDelta = 0;
        int basicReviewNumDelta = 0;

        if (homeworkType == TermReview && record.getTermReviewNum() >= 1)
            termReviewNumDelta = -1;

        if (homeworkType == BasicReview && record.getBasicReviewNum() >= 1)
            basicReviewNumDelta = -1;

        MapMessage resultMsg = tchActService.updateTeacherScholarshipVariable(
                teacherId,
                termReviewNumDelta,
                basicReviewNumDelta,
                record.getFinishRate(),
                record.getScore());

        // 如果删除作业后，条件又不满足了，就把数字置回来。
        // 考虑包班制的情况，这里要置上主老师的
//        if(resultMsg.isSuccess() && !isSatisfiedFinal(teacherId)){
//            RedisStringCommands<String,Object> commands = redisCommands.sync().getRedisStringCommands();
//            commands.setbit(CACHE_KEY_FINAL_ATTEND_NUM,getMainTeacherId(teacherId),0);
//        }
    }

    private void processCheckHomeworkMsg(Long teacherId,
                                         String homeworkId,
                                         Long groupId,
                                         int finishCount) {

        NewHomework homework = homeworkLoader.loadNewHomework(homeworkId);
        if (homework == null || homework.getType() != TermReview) {
            return;
        }

        TeacherScholarshipRecord record = tchActService.loadTeacherScholarshipRecord(teacherId);
        if (record == null)
            return;

        int groupStuNum = getGroupStudentNum(groupId);
        double finishRate = (double) finishCount / groupStuNum;
        int beforeCheckedNum = record.getTermReviewChecked();
        int actualCheckedNum = Math.max(record.getTermReviewChecked() + 1, 1);
        // 计算完成作业平均比例
        double avgFinishRate = (record.getFinishRate() * beforeCheckedNum + finishRate) / actualCheckedNum;

        // 计算平均分数
        Double avgScore = homeworkResultLoader.findByHomeworkForReport(homework)
                .values()
                .stream()
                .filter(NewHomeworkResult::isFinished)
                .filter(r -> r.processScore() != null)
                .mapToInt(NewHomeworkResult::processScore)
                .average()
                .orElse(0D);
        // 总平均分
        double totalAvgScore = (record.getScore() * beforeCheckedNum + avgScore) / actualCheckedNum;

        record.setScore(totalAvgScore);
        record.setFinishRate(avgFinishRate);
        record.setTermReviewChecked(actualCheckedNum);

        Integer curMaxGroupFinishNum = record.getMaxGroupFinishNum();
        if (curMaxGroupFinishNum == null || curMaxGroupFinishNum < 30) {
            Date statStartTime = DateUtils.stringToDate("2018-06-01", "yyyy-MM-dd");
            HashSet<String> stuNum = new HashSet<>();
            // 统计12月1号以后，该组的完成人数
            homeworkLoader.loadNewHomeworksByClazzGroupIds(Collections.singletonList(groupId))
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(l -> l.getCheckedTime() > statStartTime.getTime())
                    .forEach(c -> stuNum.addAll(accomplishmentLoader.loadNewAccomplishment(c).getDetails().keySet()));

            int groupFinishSum = stuNum.size();// 去过重的
            if (curMaxGroupFinishNum == null || groupFinishSum > curMaxGroupFinishNum)
                record.setMaxGroupFinishNum(groupFinishSum);
        }

        tchActService.updateTeacherScholarshipRecord(record);
    }

    private void processAssignHomeworkMsg(Long teacherId, String homeworkTypeCode, String subject, Long groupId, String homeworkId) {
        // 这里不校验老师的认证状态，继续累积数据，等最后阶段的
        // Teacher teacher = teacherLoader.loadTeacher(teacherId);
        //if(teacher.getAuthenticationState() != AuthenticationState.SUCCESS.getState())
        //    return;

        NewHomeworkType homeworkType = NewHomeworkType.of(homeworkTypeCode);
        if (!isValidHomeworkType(homeworkType))
            return;

        try {
            AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherScholarship:upsert")
                    .keys(teacherId)
                    .callback(() -> internalUpsertRecord(teacherId, subject, homeworkType, groupId, homeworkId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            // 睡一会儿，避免频繁撞消息
            try {
                Thread.sleep(300);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            Map<String, Object> msgBody = new HashMap<>();
            msgBody.put("teacherId", teacherId);
            msgBody.put("homeworkType", homeworkTypeCode);
            msgBody.put("homeworkId", homeworkId);
            msgBody.put("groupId", groupId);
            msgBody.put("subject", subject);
            msgBody.put("messageType", "retryAssign");
            messagePublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msgBody)));
        }
    }

    private MapMessage internalUpsertRecord(Long teacherId, String subject, NewHomeworkType homeworkType, Long groupId, String homeworkId) {
        Date now = DateUtils.truncate(new Date(), Calendar.DATE);
        TeacherScholarshipRecord record = tchActService.loadTeacherScholarshipRecord(teacherId);
        if (record == null) {
            record = new TeacherScholarshipRecord();
            record.setTeacherId(teacherId);
            record.setMaxGroupFinishNum(calMostActiveGroupStuNum(teacherId));
        }

        record.setSubjects(subject);
        record.setLastAssignDate(now);

        if (homeworkType == TermReview) {
            record.addTermReviewNum(1);
            //新增一条缓存记录
            TeacherAssignTermReviewRecord termReviewRecord = new TeacherAssignTermReviewRecord();
            Date curDate = new Date();
            termReviewRecord.setAssignDate(curDate);
            termReviewRecord.setTeacherId(teacherId);
            termReviewRecord.setSubject(subject);
            termReviewRecord.setHomeworkId(homeworkId);
            termReviewRecord.setGroupId(groupId);
            Integer week = getWeek(curDate);
            termReviewRecord.setWeek(week);
            List<TeacherAssignTermReviewRecord> records = TeacherScholarshipRecordCache.loadRecords(teacherId);
            if (Objects.isNull(records)) {
                records = new ArrayList<>();
            }
            records.add(termReviewRecord);
            TeacherScholarshipRecordCache.addRecords(records);
        } else if (homeworkType == BasicReview) {
            record.addBasicReviewNum(1);
        }

        MapMessage resultMsg = tchActService.updateTeacherScholarshipRecord(record);
//        if (resultMsg.isSuccess() && isSatisfiedFinal(teacherId)) {
//            RedisStringCommands<String, Object> commands = redisCommands.sync().getRedisStringCommands();
//            commands.setbit(CACHE_KEY_FINAL_ATTEND_NUM, getMainTeacherId(teacherId), 1);
//        }
        return resultMsg;
    }

    private Integer getWeek(Date curDate) {
        int week = 0;
        if (curDate.after(TERM_REVIEW_START_DATE) && curDate.before(TERM_REVIEW_END_DATE)) {
            week = (int) DateUtils.dayDiff(curDate, TERM_REVIEW_START_DATE) / 7;
            week = week + 1;
        }
        return week;
    }

    public static void main(String[] args) {
        Date date = DateUtils.stringToDate("2018-12-12", DateUtils.FORMAT_SQL_DATE);
        System.out.println(new TeacherScholarshipMsgHandler().getWeek(date));
    }

    /**
     * 获取老师名下最活跃的班级的活跃学生数。活跃的判断依据是通过做作业来的。
     *
     * @param teacherId
     * @return
     */
    private int calMostActiveGroupStuNum(Long teacherId) {
        List<Long> teacherGroupIds = deprecatedGroupLoaderClient.loadTeacherGroups(teacherId, false)
                .stream()
                .map(GroupMapper::getId)
                .collect(Collectors.toList());

        Date statStartTime = DateUtils.stringToDate("2018-06-01 00:00:00");
        // 分组算作业活跃人数，取最大的
        AtomicReference<Integer> maxGroupNum = new AtomicReference<>(0);
        homeworkLoader.loadNewHomeworksByClazzGroupIds(teacherGroupIds)
                .forEach((groupId, locations) -> {
                    // 少跑几次，大于30就不再看了
                    if (maxGroupNum.get() > 30)
                        return;

                    Set<String> stuNum = new HashSet<>();
                    locations.forEach(l -> {
                        // 限制下时间，只看12月份的
                        if (l.getCheckedTime() < statStartTime.getTime())
                            return;

                        stuNum.addAll(accomplishmentLoader.loadNewAccomplishment(l).getDetails().keySet());
                    });

                    if (stuNum.size() > maxGroupNum.get()) {
                        maxGroupNum.set(stuNum.size());
                    }
                });

        return maxGroupNum.get();
    }

    /**
     * 判断是否满足总复习奖学金的评选条件
     *
     * @return
     */
    private boolean isSatisfiedFinal(Long teacherId) {
        Long mainTeacherId = teacherLoader.loadMainTeacherId(teacherId);
        if (mainTeacherId == null || mainTeacherId == 0L) {
            mainTeacherId = teacherId;
        }

        List<Long> teacherIds = new ArrayList<>();
        teacherIds.add(mainTeacherId);

        List<Long> subTeacherIds = teacherLoader.loadSubTeacherIds(mainTeacherId);
        teacherIds.addAll(subTeacherIds);

        AtomicInteger termReViewSumNum = new AtomicInteger(0);
        AtomicInteger basicReviewSumNum = new AtomicInteger(0);

        teacherIds.stream()
                .map(tId -> tchActService.loadTeacherScholarshipRecord(tId))
                .filter(Objects::nonNull)
                .forEach(r -> {
                    termReViewSumNum.addAndGet(r.getTermReviewNum());
                    basicReviewSumNum.addAndGet(r.getBasicReviewNum());
                });

        Teacher mainTeacher = teacherLoader.loadTeacher(mainTeacherId);
        Subject mainSubject = mainTeacher.getSubject();

        // 主科目是语文老师的话，就不看基础必过的次数了
        if (mainSubject == Subject.CHINESE)
            return termReViewSumNum.get() >= 5;
        else
            return termReViewSumNum.get() >= 5 && basicReviewSumNum.get() >= 1;
    }

    private boolean isValidHomeworkType(NewHomeworkType type) {
        return type == TermReview || type == BasicReview;
    }

    private int getGroupStudentNum(Long groupId) {
        return raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupId(groupId)
                .size();
    }

}
