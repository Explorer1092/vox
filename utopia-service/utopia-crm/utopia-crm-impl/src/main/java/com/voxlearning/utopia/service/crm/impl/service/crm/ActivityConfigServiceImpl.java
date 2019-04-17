package com.voxlearning.utopia.service.crm.impl.service.crm;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.buffer.ActivityConfigBuffer;
import com.voxlearning.utopia.data.ActivityBaseRule;
import com.voxlearning.utopia.data.ActivityReport;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityDifficultyLevelEnum;
import com.voxlearning.utopia.enums.ActivityPatternEnum;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.crm.api.ActivityConfigService;
import com.voxlearning.utopia.service.crm.impl.cache.CRMCacheSystem;
import com.voxlearning.utopia.service.crm.impl.dao.crm.ActivityConfigDao;
import com.voxlearning.utopia.service.crm.impl.dao.crm.ActivityConfigVersionDao;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.log4j.Log4j;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = ActivityConfigService.class)
@Log4j
public class ActivityConfigServiceImpl implements ActivityConfigService, InitializingBean {

    @Inject
    private ActivityConfigDao activityConfigDao;
    @Inject
    private ActivityConfigVersionDao activityConfigVersion;
    @Inject
    private DeprecatedClazzLoaderClient clazzLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private CRMCacheSystem crmCacheSystem;

    @AlpsQueueProducer(queue = "utopia.campaign.activity.generate.question")
    private MessageProducer producer;

    private static ManagedNearBuffer<List<ActivityConfig>, ActivityConfigBuffer> latelyPassedActivityConfigBuffer;

    @Override
    public void afterPropertiesSet() throws Exception {
        NearBufferBuilder<List<ActivityConfig>, ActivityConfigBuffer> builder = NearBufferBuilder.newBuilder();
        builder.name("ActivityConfigBuffer");
        builder.category("SERVER");
        builder.nearBufferClass(ActivityConfigBuffer.class);
        builder.reloadNearBuffer(10, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> {
            long version = activityConfigVersion.current();
            List<ActivityConfig> list = activityConfigDao.loadLatelyPassedActivityConfigBufferData();
            return new VersionedBufferData<>(version, list);
        });
        builder.reloadNearBuffer((oldVersion, attributes) -> {
            long currentVersion = activityConfigVersion.current();
            if (oldVersion < currentVersion) {
                List<ActivityConfig> list = activityConfigDao.loadLatelyPassedActivityConfigBufferData();
                return new VersionedBufferData<>(currentVersion, list);
            }
            return null;
        });
        latelyPassedActivityConfigBuffer = builder.build();
    }

    @Override
    public MapMessage publishActivity(ActivityConfig activityConfig) {
        if (activityConfig == null || activityConfig.getType() == null || activityConfig.getStartTime() == null || activityConfig.getEndTime() == null || activityConfig.getApplicant() == null) {
            return MapMessage.errorMessage("参数错误");
        }
        if (StringUtils.isAnyBlank(activityConfig.getTitle()) || CollectionUtils.isEmpty(activityConfig.getClazzLevels())) {
            return MapMessage.errorMessage("参数错误");
        }
        if (activityConfig.getStatus() == null) {
            activityConfig.setStatus(1);
        }
        activityConfig.setDisabled(false);
        activityConfig.setNoticeStatus(false);
        if (activityConfig.getType() == ActivityTypeEnum.SUDOKU) {
            ActivityBaseRule rules = activityConfig.getRules();
            // 数独得分模式限制50题
            if (rules.getPattern() == ActivityPatternEnum.NORMAL) {
                rules.setLimitAmount(50);
            }
            activityConfig.setRules(rules);
        }
        return insertActivity(activityConfig);
    }

    @Override
    public MapMessage publishActivityByDev(String startTime, String endTime, String model, String level,
                                           Integer limitTime, Integer limitAmount, Integer playLimit) {
        try {
            Date startDate = DateUtils.parseDate(startTime + " 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
            Date endDate = DateUtils.parseDate(endTime + " 23:59:59", DateUtils.FORMAT_SQL_DATETIME);

            ActivityConfig activityConfig = new ActivityConfig();
            activityConfig.setTitle("数独趣味活动");
            activityConfig.setDescription("数独趣味活动");
            activityConfig.setStartTime(startDate);
            activityConfig.setEndTime(endDate);
            activityConfig.setType(ActivityTypeEnum.SUDOKU);
            activityConfig.setEmail("junbaor@qq.com");
            activityConfig.setApplicant(0L);
            activityConfig.setApplicantRole(ActivityConfig.ROLE_DEV);
            activityConfig.setDisabled(false);
            activityConfig.setNoticeStatus(true);

            ActivityBaseRule rule = new ActivityBaseRule();
            rule.setPattern(ActivityPatternEnum.valueOf(model));
            rule.setLevel(ActivityDifficultyLevelEnum.valueOf(level));
            rule.setLimitTime(limitTime);
            rule.setPlayLimit(playLimit);
            rule.setLimitAmount(limitAmount == null ? 50 : limitAmount);
            activityConfig.setRules(rule);

            return insertActivity(activityConfig);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    private MapMessage insertActivity(ActivityConfig activityConfig) {
        ActivityReport report = new ActivityReport();
        activityConfig.setReport(report);
        activityConfigDao.insert(activityConfig);
        producer.produce(Message.newMessage().withStringBody(activityConfig.getId()));
        return MapMessage.successMessage().add("id",activityConfig.getId());
    }

    @Override
    public List<ActivityConfig> loadByApplicants(Collection<Long> applicants, Integer role) {
        if (applicants == null) {
            return Collections.emptyList();
        }
        return activityConfigDao.loadByApplicant(applicants, role).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public Page<ActivityConfig> query(Integer status, String type, Integer clazzLevel, String name, Integer applicantRole, int page, int pageSize) {
        return activityConfigDao.load(status, type, clazzLevel, name, applicantRole, page, pageSize);
    }

    @Override
    public boolean agree(String id, String auditor, Set<Subject> subjects) {
        if (StringUtils.isAnyBlank(id, auditor)) {
            return false;
        }
        activityConfigDao.agree(id, auditor, subjects);
        return true;
    }

    @Override
    public boolean reject(String id, String auditor, String rejectReason) {
        if (StringUtils.isAnyBlank(id, auditor)) {
            return false;
        }
        activityConfigDao.reject(id, auditor);
        return true;
    }

    @Override
    public ActivityConfig load(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return activityConfigDao.load(id);
    }

    @Override
    public boolean delete(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        activityConfigDao.disableActivity(id);
        return true;
    }

    @Override
    public List<ActivityConfig> loadAgreeStartingNoNotice() {
        return activityConfigDao.loadAgreeStartingNoNotice();
    }

    @Override
    public boolean editNoticeStatus(String id, Boolean noticeStatus) {
        if (StringUtils.isBlank(id) || noticeStatus == null) {
            return false;
        }
        activityConfigDao.editNoticeStatus(id, noticeStatus);
        return true;
    }

    @Override
    public List<ActivityConfig> loadAllActivityConfig() {
        return activityConfigDao.loadAllActivityCofig();
    }

    @Override
    public void updateActivityConfig(ActivityConfig activityConfig) {
        activityConfigDao.upsert(activityConfig);
    }

    @Override
    public MapMessage updateEmail(String id, String email) {
        try {
            activityConfigDao.updateEmail(id, email);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    @Override
    public List<ActivityConfig> loadActivityConfigListByTypeAndDate(String activityType, Date startDate) {
        return activityConfigDao.loadActivityConfigListByTypeAndDate(activityType,startDate);
    }

    @Override
    public MapMessage incrementLatelyPassedActivityVersion() {
        activityConfigVersion.increment();
        return MapMessage.successMessage();
    }

    @Override
    public VersionedBufferData<List<ActivityConfig>> getLatelyPassedActivityBuffer(Long version) {
        ActivityConfigBuffer nativeBuffer = latelyPassedActivityConfigBuffer.getNativeBuffer();
        if (version < 0 || version < nativeBuffer.getVersion()) {
            return nativeBuffer.dump();
        }
        return null;
    }

    @Override
    public void resetLatelyPassedActivityConfigBuffer() {
        latelyPassedActivityConfigBuffer.reset();
    }

    @Override
    public MapMessage updateDisabledStatus(String activityId, Boolean disabled) {
        activityConfigDao.updateDisabledStatus(activityId, disabled);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateEndTime(String activityId, String yyyyMmdd) {
        if (yyyyMmdd.length() != 8) {
            return MapMessage.errorMessage();
        }
        activityConfigDao.updateEndTime(activityId, yyyyMmdd);
        return MapMessage.successMessage();
    }

    @Override
    public List<ActivityConfig> loadAllActivityConfigIncludeIsEnd() {
        return activityConfigDao.loadAllActivityConfigIncludeIsEnd();
    }

    @Override
    public List<ActivityConfig> loadNoSignUpActivity(Long teacherId) {
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            return Collections.emptyList();
        }
        // 老师名下的班级
        List<Clazz> clazzList = loadTeacherClazz(teacherId);

        Date date = new Date();
        // 返回未(完全)报名的活动, buffer 里面包含最近7天内结束的
        List<ActivityConfig> activityConfigs = latelyPassedActivityConfigBuffer.getNativeBuffer().loadBySchoolIdAreaIdClazzIds(
                teacherDetail.getTeacherSchoolId(),
                teacherDetail.getRegionCode(),
                clazzList
        ).stream().filter(i -> !i.isEnd(date)).collect(Collectors.toList());

        return activityConfigs.stream()
                .filter(i -> isNewActivity(i.getId()))
                .filter(i -> i.fetchSubjects().contains(Subject.UNKNOWN) || CollectionUtils.containsAny(i.fetchSubjects(), teacherDetail.getSubjects()))
                .filter(i -> !isSignUp(i.getId(), clazzList)).collect(Collectors.toList());
    }


    @Override
    public Boolean loadSignUpStatus(Long teacherId, String activityId) {
        if (!isNewActivity(activityId)) {
            return true;
        }

        List<Clazz> clazzList = loadTeacherClazz(teacherId);
        for (Clazz clazz : clazzList) {
            CacheObject<Object> cacheObject = crmCacheSystem.CBS.persistence.get(buildActivityCacheKey(activityId, clazz.getId()));
            if (!cacheObject.containsValue()) return false;
        }
        return true;
    }

    @Override
    public MapMessage signUpActivity(Long teacherId, String activityId) {
        ActivityConfig config = activityConfigDao.load(activityId);
        if (config == null || config.getDisabled()) {
            return MapMessage.errorMessage("活动不存在或已被删除");
        }

        long endTime = config.getEndTime().getTime();
        long curTime = System.currentTimeMillis();
        int second = SafeConverter.toInt((endTime - curTime) / 1000);

        if (config.getEndTime().getTime() < curTime) {
            return MapMessage.errorMessage("活动已结束");
        }

        if (!config.fetchSubjects().contains(Subject.UNKNOWN)) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            String msg = "只有数学老师才能报名";
            if (CollectionUtils.isEmpty(teacherDetail.getSubjects())) {
                return MapMessage.errorMessage(msg);
            }
            if (!CollectionUtils.containsAny(teacherDetail.getSubjects(), config.fetchSubjects())) {
                return MapMessage.errorMessage(msg);
            }
        }

        try {
            List<Clazz> clazzList = loadTeacherClazz(teacherId);
            for (Clazz clazz : clazzList) {
                String cacheKey = buildActivityCacheKey(activityId, clazz.getId());
                crmCacheSystem.CBS.persistence.set(cacheKey, second, 1);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return MapMessage.errorMessage("内部异常");
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage cancelSignUpActivity(Long teacherId, String activityId) {
        List<Clazz> clazzList = loadTeacherClazz(teacherId);
        for (Clazz clazz : clazzList) {
            String cacheKey = buildActivityCacheKey(activityId, clazz.getId());
            crmCacheSystem.CBS.persistence.delete(cacheKey);
        }
        return MapMessage.successMessage();
    }

    private static final String TEACHER_SING_UP_ACTIVITY_POP = "TEACHER_SING_UP_ACTIVITY_POP:";

    @Override
    public MapMessage resetTeacherIndexPop(Long teacherId) {
        Boolean delete = crmCacheSystem.CBS.flushable.delete(TEACHER_SING_UP_ACTIVITY_POP + teacherId);
        return MapMessage.successMessage().add("status", delete);
    }

    @NotNull
    private List<Clazz> loadTeacherClazz(Long teacherId) {
        Set<Long> relatedIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Clazz> clazzList = clazzLoaderClient.getRemoteReference().loadTeacherClazzs(relatedIds)
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        return clazzList
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(i -> !i.isTerminalClazz())
                .filter(i -> !i.isDisabledTrue())
                .collect(Collectors.toList());
    }

    /**
     * 班级是否全报名
     */
    private boolean isSignUp(String activityId, List<Clazz> clazzList) {
        for (Clazz clazz : clazzList) {
            boolean signUp = isSignUp(activityId, clazz.getId());
            if (!signUp) {
                return false;
            }
        }
        return true;
    }

    /**
     * 班级是否已报名
     */
    private boolean isSignUp(String activityId, Long clazzId) {
        CacheObject<Object> cacheObject = crmCacheSystem.CBS.persistence.get(buildActivityCacheKey(activityId, clazzId));
        if (cacheObject.containsValue()) {
            return true;
        }
        return false;
    }

    /**
     * 生成报名 cache key
     */
    private String buildActivityCacheKey(String activityId, Long clazzId) {
        return CacheKeyGenerator.generateCacheKey("TEACHER_SIGN_UP_ACTIVITY",
                new String[]{"A", "C"},
                new Object[]{activityId, clazzId}
        );
    }

    /**
     * 生成班级布置活动的 cache key
     */
//    private String buildClazzActivityCacheKey(Long clazzId) {
//        return CacheKeyGenerator.generateCacheKey("TEACHER_CLAZZ_ACTIVITY",
//                new String[]{"T", "C"},
//                new Object[]{clazzId}
//        );
//    }

    private boolean isNewActivity(String activityId) {
        if (StringUtils.isEmpty(activityId) || activityId.length() < 24) {
            return false;
        }
        int timestamp = new ObjectId(activityId).getTimestamp();
        return timestamp >= ActivityConfigService.TEACHER_SIGN_UP_ON_LINE_TIME;
    }

}
