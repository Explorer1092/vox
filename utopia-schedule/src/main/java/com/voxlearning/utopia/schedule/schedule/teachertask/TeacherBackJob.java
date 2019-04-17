package com.voxlearning.utopia.schedule.schedule.teachertask;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.NamedDaemonThreadFactory;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.alps.util.MapUtils;
import com.voxlearning.athena.api.tag.UserTagService;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.schedule.cache.ScheduleCacheSystem;
import com.voxlearning.utopia.schedule.dao.TeacherBackDao;
import com.voxlearning.utopia.schedule.entity.TeacherBack;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.invitation.api.TeacherActivateService;
import com.voxlearning.utopia.service.invitation.entity.TeacherActivate;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Named
@ScheduledJobDefinition(
        jobName = "召回唤醒老师-可幂等执行,后期可能改成t+1放在凌晨",
        jobDescription = "召回唤醒老师",
        disabled = {Mode.STAGING, Mode.DEVELOPMENT, Mode.TEST, Mode.UNIT_TEST},
        cronExpression = "0 0 9 * * ?"
)
public class TeacherBackJob extends ScheduledJobWithJournalSupport {

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private UserLoginServiceClient userLoginServiceClient;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private TeacherBackDao teacherBackDao;
    @Inject
    private ScheduleCacheSystem scheduleCacheSystem;
    @ImportService(interfaceClass = UserTagService.class)
    private UserTagService userTagService;
    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql utopiaSql;

    @ImportService(interfaceClass = TeacherActivateService.class)
    private TeacherActivateService teacherActivateService;

    private DayRange todayRange = DayRange.current();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getUtopiaSql("hs_misc");
    }

    private String TAG_ID = RuntimeMode.isUsingProductionData() ? "T_03_1025_0" : "T_03_1025_null";
    private String DAY_CACHE_KEY = "TeacherBackJob_" + DateFormatUtils.format(new Date(), "yyyyMMdd");

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        long startTime = System.currentTimeMillis();
        todayRange = DayRange.current();
        DAY_CACHE_KEY = "TeacherBackJob_" + DateFormatUtils.format(new Date(), "yyyyMMdd");

        if (RuntimeMode.isUsingTestData()) {
            scheduleCacheSystem.CBS.persistence.delete(DAY_CACHE_KEY);
        }

        long startUserId = getJobStart();
        int pageSize = RuntimeMode.isProduction() ? 1000 : 2;

        logger.info("TeacherBackJob start ,startUserId:{} pageSize:{}", startUserId, pageSize);

        BigDataSource bigDataSource = new BigDataSource(TAG_ID, startUserId, pageSize, userTagService);

        for (List<Long> next : bigDataSource) {
            for (Long teacherId : next) {
                try {
                    oneTeacher(teacherId);
                } catch (Exception e) {
                    logger.error("召回唤醒任务异常, teacherId:" + teacherId, e);
                }
            }
            setJobStart(bigDataSource.getStartUserId());
            logger.info("TeacherBackJob progress : userId " + bigDataSource.getStartUserId());
        }

        //joinMarketCancelTeacherActivateRef();

        long timeConsuming = System.currentTimeMillis() - startTime;
        logger.info("TeacherBackJob end  " + startTimestamp + " ms");

        if (RuntimeMode.gt(Mode.DEVELOPMENT)) {
            emailServiceClient.createPlainEmail()
                    .body("正常结束,耗时：" + timeConsuming + " ms")
                    .subject("【" + RuntimeMode.current() + "】" + "老师召回任务完成")
                    .to("junbao.zhang@17zuoye.com")
                    .send();
        }
    }

    private void oneTeacher(Long teacherId) {
        if (isSubTeacher(teacherId)) return;

        CrmTeacherSummary crmTeacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacherId);
        if (crmTeacherSummary == null || crmTeacherSummary.getLatestAssignHomeworkTime() == null) {
            return;
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null || teacherDetail.getSubject() == null || (!teacherDetail.isPrimarySchool())) {
            return;
        }

        pointLog(MapUtils.map(
                "module", "teacher_back_job",
                "op", "o_o3u83xv19D",
                "teacher_id", teacherId
        ));

        Date now = new Date();

        boolean upsert = false;
        TeacherBack teacherBack = teacherBackDao.load(teacherId);

        // 初始化或重置
        if (teacherBack == null) {
            teacherBack = new TeacherBack();
            teacherBack.setId(teacherId);
            upsert = true;
        } else if (isReset(teacherBack, crmTeacherSummary)) {
            pointLog(MapUtils.map(
                    "module", "teacher_back_job",
                    "op", "o_uSBlqWKY4c",
                    "teacher_id", teacherId
            ));

            teacherBackDao.remove(teacherBack.getId()); // replace \ upsert 都不行

            teacherBack = new TeacherBack();
            teacherBack.setId(teacherId);
            upsert = true;
        }

        // 距最近一次布置作业>7天，则进行站内push
        if (teacherBack.getPushTime() == null) {
            Date lastAssignHomeworkDate = new Date(crmTeacherSummary.getLatestAssignHomeworkTime());
            if (ltTodayDay(lastAssignHomeworkDate, 7)) {
                teacherBack.setPushTime(now);
                sendPush(teacherDetail);
                upsert = true;
            }
        }

        // Push>24H，30天内有过登陆，则进行短信召回
        if (teacherBack.getPushTime() != null && teacherBack.getSmsTime() == null) {
            boolean _1day = todayRange.contains(DateUtils.addDays(teacherBack.getPushTime(), 1));
            if (_1day) {
                Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(teacherId);
                if (geTodayDay(lastLoginTime, 30)) {
                    String userMobile = sensitiveUserDataServiceClient.loadUserMobile(teacherId, "召回唤醒老师发送短信");
                    if (StringUtils.isNotBlank(userMobile)) {
                        teacherBack.setSmsTime(now);
                        sendSms(teacherDetail, userMobile);
                        upsert = true;
                    }
                }
            }
        }

        // 不符合短信召回条件的用户，在站内push>72h后，进入唤醒list
        if (teacherBack.getPushTime() != null && teacherBack.getSmsTime() == null && teacherBack.getAwakenTime() == null) {
            boolean _3day = todayRange.contains(DateUtils.addDays(teacherBack.getPushTime(), 3));
            if (_3day) {
                teacherBack.setAwakenTime(now);
                joinAwakenList(teacherId);
                upsert = true;
            }
        }

        // 短信>72H，则进入唤醒list
        if (teacherBack.getPushTime() != null && teacherBack.getSmsTime() != null && teacherBack.getAwakenTime() == null) {
            boolean _3day = todayRange.contains(DateUtils.addDays(teacherBack.getSmsTime(), 3));
            if (_3day) {
                teacherBack.setAwakenTime(now);
                joinAwakenList(teacherId);
                upsert = true;
            }
        }

        if (upsert) {
            teacherBackDao.upsert(teacherBack);
        }
    }

    private boolean isReset(TeacherBack teacherBack, CrmTeacherSummary crmTeacherSummary) {
        if (teacherBack.getPushTime() != null) {
            if (crmTeacherSummary.getLatestAssignHomeworkTime() > teacherBack.getPushTime().getTime()) {
                return true;
            }
        }

        // 唤醒list > 7 天
        if (teacherBack.getAwakenTime() != null) {
            List<TeacherActivate> teacherActivates = teacherActivateService.loadActivateInitOrIng(Collections.singleton(teacherBack.getId()));

            boolean result = false;

            for (TeacherActivate activate : teacherActivates) {
                Date time = activate.getCreateDatetime();
                if (Objects.equals(activate.getStatus(), TeacherActivate.Status.ING.getCode())) {
                    time = activate.getSuccessTime();
                }

                boolean before = ltTodayDay(time, 7);
                if (before) {
                    result = true;
                    teacherActivateService.cancel(activate.getId());
                }
            }

            return result;
        }
        return false;
    }

    private ZoneOffset zoneOffset = ZoneOffset.of("+8");

    private Long getReceiveTime() {
        LocalTime time = LocalTime.parse("08:30:00");
        LocalDateTime sendTime = LocalDate.now().atTime(time);
        DayOfWeek dayOfWeek = sendTime.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY) {
            sendTime = sendTime.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        } else {
            sendTime = sendTime.plusDays(1);
        }
        return sendTime.toInstant(zoneOffset).toEpochMilli();
    }

    private void sendPush(TeacherDetail teacherDetail) {
        Long receiveTime = getReceiveTime();

        String pushText = getPushText(teacherDetail.getSubject());
        if (StringUtils.isBlank(pushText)) {
            return;
        }

        pointLog(MapUtils.map(
                "module", "teacher_back_job",
                "op", "o_HeOwGWg7w6",
                "teacher_id", teacherDetail.getId(),
                "send_time", System.currentTimeMillis(),
                "receive_time", receiveTime
        ));

        Map<String, Object> pushExtInfo = new HashMap<>();
        String pushLink = getPushLink(teacherDetail.getSubject());
        if (StringUtils.isNotBlank(pushText)) {
            pushExtInfo.put("link", pushLink);
        }
        pushExtInfo.put("s", TeacherMessageType.ACTIVIY.getType());
        pushExtInfo.put("t", "h5");

        appMessageServiceClient.sendAppJpushMessageByIds(pushText, AppMessageSource.PRIMARY_TEACHER,
                Collections.singletonList(teacherDetail.getId()), pushExtInfo, receiveTime);
    }

    private void sendSms(TeacherDetail teacherDetail, String userMobile) {
        Long receiveTime = getReceiveTime();

        String pushText = getSmsText(teacherDetail.getSubject());
        if (StringUtils.isBlank(pushText)) {
            return;
        }

        if (StringUtils.isBlank(userMobile)) {
            return;
        }

        pointLog(MapUtils.map(
                "module", "teacher_back_job",
                "op", "o_ohhYUGKmfc",
                "teacher_id", teacherDetail.getId(),
                "send_time", System.currentTimeMillis(),
                "receive_time", receiveTime
        ));

        String time = DateUtils.dateToString(new Date(receiveTime), "yyyyMMddHHmmss");
        smsServiceClient.createSmsMessage(userMobile)
                .type(SmsType.TEACHER_OPERATION_JOB.name())
                .time(time)
                .content(pushText)
                .send();
    }

    private void joinAwakenList(Long teacherId) {
        pointLog(MapUtils.map(
                "module", "teacher_back_job",
                "op", "o_TcqEAVDAuO",
                "teacher_id", teacherId
        ));

        teacherActivateService.saveTeacherActivate(teacherId);
    }

    private void joinMarketList(Long teacherId) {
        // TODO
    }

    private boolean isSubTeacher(Long teacherId) {
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        return mainTeacherId != null;
    }

    private boolean ltTodayDay(Date date, int day) {
        if (date == null) {
            return false;
        }
        Date todayStart = com.voxlearning.alps.calendar.DateUtils.getTodayStart();
        Date pre = DateUtils.addDays(todayStart, -day);
        return date.before(pre);
    }

    private boolean geTodayDay(Date date, int day) {
        if (date == null) {
            return false;
        }
        return !ltTodayDay(date, day);
    }

    @SuppressWarnings("ALL")
    private List<TeacherActivate> getTeacherActivates() {
        String sql = "SELECT * FROM VOX_TEACHER_ACTIVATE WHERE " +
                "(STATUS = 0 AND CREATE_DATETIME > ?) OR (STATUS = 1 AND REF_TIME IS NULL)";
        Date date = DateUtils.addDays(new Date(), -3);
        return utopiaSql.withSql(sql).useParamsArgs(date)
                .queryAll(new BeanPropertyRowMapper<>(TeacherActivate.class));
    }

    private void pointLog(Map<String, String> map) {
        LogCollector.info("backend-general", map);
    }

    /**
     * (未建立唤醒关系>3天或建立唤醒关系>3天未完成)进入市场人员列表
     */
    private void joinMarketCancelTeacherActivateRef() {
        List<TeacherActivate> teacherActivates = getTeacherActivates();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5, 5, 10, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(10),
                NamedDaemonThreadFactory.getInstance("CancelTeacherActivateRef-Pool"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        CountDownLatch countDownLatch = new CountDownLatch(teacherActivates.size());

        for (TeacherActivate teacherActivate : teacherActivates) {
            executor.submit(() -> {
                try {
                    teacherActivateService.cancel(teacherActivate.getId());  // 结束关系
                    TeacherBack teacherBack = teacherBackDao.load(teacherActivate.getActivateId());
                    if (teacherBack != null) {
                        teacherBack.setMarketTime(new Date());              // 设置进入市场时间
                        joinMarketList(teacherActivate.getActivateId());
                        teacherBackDao.upsert(teacherBack);
                    }
                } catch (Exception e) {
                    logger.error("进入市场列表并取消待激活时异常, teacherId:" + teacherActivate.getActivateId(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.error("joinMarketCancelTeacherActivateRef countDownLatch exception", e);
        }
    }

    private String getPushText(Subject subject) {
        String text = "";
        if (subject == Subject.CHINESE) {
            text = "课内高效阅读的教学策略！ 马上学习>>";
        } else if (subject == Subject.MATH) {
            text = "数学课堂的奇幻魔术秀，教你上不一样的数学课！>>";
        } else if (subject == Subject.ENGLISH) {
            text = "教学大咖带你揭秘美国课堂中的写作教学！ 马上学习>>";
        }
        return text;
    }

    private String getPushLink(Subject subject) {
        String text = "";
        if (subject == Subject.CHINESE) {
            text = "https://www.17zuoye.com/view/mobile/teacher/activity/newforum/detail?forum_index=10";
        } else if (subject == Subject.MATH) {
            text = "https://www.17zuoye.com/view/mobile/teacher/activity/newforum/detail?forum_index=11";
        } else if (subject == Subject.ENGLISH) {
            text = "https://www.17zuoye.com/view/mobile/teacher/activity/newforum/detail?forum_index=12";
        }
        return text;
    }

    private String getSmsText(Subject subject) {
        String text = "";
        if (subject == Subject.CHINESE) {
            text = "生字认读，课文读背，有效提升学生预习效果>>快来抢先体验智能读背 https://17zyw.cn/AQV37YBe";
        } else if (subject == Subject.MATH) {
            text = "数学讲练测，题不会做？微课教你方法>>抢先体验免费微课讲解 https://17zyw.cn/AQV37YBe";
        } else if (subject == Subject.ENGLISH) {
            text = "情景式口语练习，有趣的互动形式，快来抢先体验智能教学的口语交际吧 https://17zyw.cn/AQV37YBe";
        }
        return text;
    }

    private Long getJobStart() {
        long start = 0L;
        CacheObject<Object> cacheObject = scheduleCacheSystem.CBS.persistence.get(DAY_CACHE_KEY);
        if (cacheObject.containsValue()) {
            start = SafeConverter.toLong(cacheObject.getValue().toString().trim());
        }
        return start;
    }

    private void setJobStart(Long start) {
        if (start != null) {
            scheduleCacheSystem.CBS.persistence.set(DAY_CACHE_KEY, DateUtils.getCurrentToDayEndSecond(), start);
        }
    }

    private static class BigDataSource implements Iterable<List<Long>> {
        private String tagId;
        @Getter
        private long startUserId;
        private int length;

        private List<Long> data;
        private UserTagService userTagService;

        BigDataSource(String tagId, long startUserId, int length, UserTagService userTagService) {
            this.tagId = tagId;
            this.startUserId = startUserId;
            this.length = length;
            this.userTagService = userTagService;
        }

        @NotNull
        @Override
        public Iterator<List<Long>> iterator() {
            return new Iterator<List<Long>>() {
                @Override
                public boolean hasNext() {
                    data = getTagUser(tagId, startUserId, length);
                    boolean notEmpty = CollectionUtils.isNotEmpty(data);
                    if (notEmpty) {
                        startUserId = data.get(data.size() - 1);
                    }
                    return notEmpty;
                }

                @Override
                public List<Long> next() {
                    return data;
                }
            };
        }

        private List<Long> getTagUser(String tagId, long start, int length) {
            List<Long> tagUsers = userTagService.getTagUsers(tagId, start, length);
            if (tagUsers == null) {
                return new ArrayList<>();
            }
            return tagUsers;
        }
    }
}
