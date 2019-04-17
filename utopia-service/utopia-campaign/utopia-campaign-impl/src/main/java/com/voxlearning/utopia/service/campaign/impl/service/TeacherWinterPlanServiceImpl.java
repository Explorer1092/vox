package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.campaign.api.TeacherWinterPlanService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityRef;
import com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum;
import com.voxlearning.utopia.service.campaign.api.mapper.WinterPlanMapper;
import com.voxlearning.utopia.service.campaign.api.mapper.dp.StudentPlanningCount;
import com.voxlearning.utopia.service.campaign.api.mapper.dp.StudentPlanningWeekInfo;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherActivityRefDao;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum.PARENT_CHILD_2018;
import static java.util.stream.Collectors.toList;

@Named
@Slf4j
@ExposeService(interfaceClass = TeacherWinterPlanService.class)
public class TeacherWinterPlanServiceImpl implements TeacherWinterPlanService {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private CampaignCacheSystem campaignCacheSystem;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherActivityRefDao teacherActivityRefDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherWinterPlanDPBridge teacherWinterPlanDPBridge;
    @Inject private UserLoginServiceClient userLoginServiceClient;

    private static final String HOLIDAY_ACTIVITY_INDEX = "/view/mobile/common/activity/holiday_activity/index";


    @AlpsPubsubPublisher(topic = "utopia.campaign.planning.msg.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher noticePublisher;

    @AlpsPubsubPublisher(topic = "utopia.campaign.teacher.assign.plan.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;

    @Override
    public MapMessage loadTeacherStatus(Long teacherId) {
        TeacherActivityRef load = teacherActivityRefDao.loadUserIdTypeId(getMainTeacherId(teacherId), PARENT_CHILD_2018);
        return MapMessage.successMessage().add("assigned", load != null);
    }

    @Override
    public MapMessage loadTeacherClazzInfo(Long teacherId) {
        TeacherActivityRef load = teacherActivityRefDao.loadUserIdTypeId(getMainTeacherId(teacherId), PARENT_CHILD_2018);

        Set<Long> allTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference()
                .loadTeacherClazzs(allTeacherIds).values().stream()
                .flatMap(Collection::stream)
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(Comparator.comparing(Clazz::getClazzLevel).thenComparing(Clazz::formalizeClazzName))
                .distinct()
                .collect(toList());

        Set<Long> clazzIdSet = clazzList.stream().map(Clazz::getId).collect(Collectors.toSet());
        Map<Long, List<GroupMapper>> teacherGroupMap = teacherLoaderClient.findTeacherAllGroupInClazz(clazzIdSet, teacherId);

        List<WinterPlanMapper> result = new ArrayList<>();

        for (Clazz clazz : clazzList) {
            WinterPlanMapper winterPlanMapper = new WinterPlanMapper();
            winterPlanMapper.setClazzLevel(clazz.getClazzLevel().getLevel());
            winterPlanMapper.setClazzId(clazz.getId());
            winterPlanMapper.setClazzName(clazz.formalizeClazzName());

            List<GroupMapper> groupMappers = teacherGroupMap.get(clazz.getId());
            if (CollectionUtils.isEmpty(groupMappers)) {
                continue;
            }

            groupMappers.sort(Comparator.comparing(GroupMapper::getId));

            Set<Long> studentId = new LinkedHashSet<>();
            Set<WinterPlanMapper.StudentInfo> studentInfoList = new LinkedHashSet<>();

            for (GroupMapper group : groupMappers) {
                List<User> groupStudentIds = studentLoaderClient.loadGroupStudents(group.getId()).stream().sorted(Comparator.comparing(LongIdEntity::getId)).collect(toList());

                List<WinterPlanMapper.StudentInfo> studentInfos = groupStudentIds.stream()
                        .filter(i -> !Objects.equals(i.getProfile().getRealname(), "体验账号"))
                        .map(i -> new WinterPlanMapper.StudentInfo(i.getId(), i.getProfile().getRealname()))
                        .collect(toList());

                if (CollectionUtils.isEmpty(studentInfos)) {
                    continue;
                }

                Set<Long> groupAllStudentId = studentInfos.stream().map(WinterPlanMapper.StudentInfo::getStudentId).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(groupAllStudentId)) {
                    studentId.addAll(groupAllStudentId);
                }

                for (WinterPlanMapper.StudentInfo studentInfo : studentInfos) {
                    Boolean studentStatus = getStudentStatus(studentInfo.getStudentId(), teacherId);
                    if (studentStatus) {
                        studentInfoList.add(studentInfo);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(studentId)) {
                List<WinterPlanMapper.StudentInfo> students = new ArrayList<>(studentInfoList);
                students.sort(Comparator.comparing(WinterPlanMapper.StudentInfo::getStudentId));

                winterPlanMapper.setAssignSize(studentInfoList.size());
                winterPlanMapper.setSize(studentId.size());
                winterPlanMapper.setStudents(students);
                result.add(winterPlanMapper);
            }
        }

        return MapMessage.successMessage().add("assigned", load != null).add("groups", result);
    }

    @Override
    public MapMessage loadStudentPlanDetail(Long studentId) {
        List<StudentPlanningWeekInfo> studentPlanningWeekInfos = teacherWinterPlanDPBridge.loadStudentPlanningWeekInfo(studentId);
        return MapMessage.successMessage().add("data", studentPlanningWeekInfos);
    }

    @Override
    public MapMessage assgin(Long teacherId) {
        AtomicCallback<MapMessage> atomicCallback = () -> {
            Long mainTid = getMainTeacherId(teacherId);

            TeacherActivityRef activityRef = teacherActivityRefDao.loadUserIdTypeId(mainTid, PARENT_CHILD_2018);
            if (activityRef == null) {
                activityRef = new TeacherActivityRef();
                activityRef.setUserId(mainTid);
                activityRef.setType(PARENT_CHILD_2018.name());
                teacherActivityRefDao.insert(activityRef);

                sendKafka(mainTid);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("不可重复布置");
            }
        };

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("WINTER_PLAN_ASSIGN_TEACHER")
                    .keys(teacherId)
                    .callback(atomicCallback)
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("点击过快,请重试...");
        } catch (Exception ex) {
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage studentAssgin(Long studentId) {
        // 如果老师已经布置了, 学生再布置就不再提醒家长了
        Boolean teacherAssignStatus = loadTeacherAssignStatus(studentId);
        if (teacherAssignStatus) {
            return MapMessage.successMessage();
        }

        List<StudentParentRef> parentList = studentLoaderClient.loadStudentParentRefs(studentId);
        if (CollectionUtils.isEmpty(parentList)) {
            return MapMessage.errorMessage("还未绑定家长").add("code", 202);
        }

        // 是否打扰过家长
        Boolean parentStatusCache = loadBotherStudentParentsCache(studentId);
        if (parentStatusCache) {
            return MapMessage.successMessage();
        }

        setBotherStudentParentsCache(studentId);
        setStudentAssignCache(studentId);

        String content = "家长您好！您的孩子邀请您和Ta制定亲子趣味寒假计划，共同成长！好玩又充实寒假离不开您的有效规划，立即前往";

        for (StudentParentRef parentRef : parentList) {
            boolean notActivity = isNotActivity(parentRef.getParentId());
            if (notActivity) {
                // 短信
                String mobile = sensitiveUserDataServiceClient.loadUserMobile(parentRef.getParentId());
                if (StringUtils.isNotBlank(mobile)) {
                    SmsMessage smsMessage = new SmsMessage();
                    smsMessage.setMobile(mobile);
                    smsMessage.setType(SmsType.PARENT_WINTER_PLAN_NOTIFY.name());
                    smsMessage.setSmsContent(content + " https://17zyw.cn/2V7MBIyj");
                    smsServiceClient.getSmsService().sendSms(smsMessage);
                }
            } else {
                // push
                AppMessage message = new AppMessage();
                message.setUserId(parentRef.getParentId());
                message.setTitle("寒假计划");
                message.setContent(content);
                message.setLinkType(1);
                message.setLinkUrl(HOLIDAY_ACTIVITY_INDEX);
                message.setImageUrl("");
                message.setMessageType(ParentMessageType.REMINDER.getType());
                Map<String, Object> extInfo = new HashMap<>();
                extInfo.put("tag", ParentMessageTag.通知.name());
                message.setExtInfo(extInfo);
                messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

                Map<String, Object> pushExtInfo = new HashMap<>();
                pushExtInfo.put("url", HOLIDAY_ACTIVITY_INDEX);
                pushExtInfo.put("s", ParentAppPushType.RECOMMEND_NEWS.name());
                pushExtInfo.put("tag", ParentMessageTag.通知);
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, Collections.singletonList(parentRef.getParentId()), pushExtInfo);
            }
        }
        return MapMessage.successMessage();
    }

    public boolean isNotActivity(Long userId) {
        return !isActivity(userId);
    }

    public boolean isActivity(Long userId) {
        Date userLastLoginTime = userLoginServiceClient.findUserLastLoginTime(userId);
        if (userLastLoginTime == null) {
            return false;
        }
        Date preMonth = DateUtils.addMonths(new Date(), -1);
        return userLastLoginTime.after(preMonth);
    }

    public void sendSmsPush(Long teacherId) {
        Set<Long> allUserIdSet = getAllStudentIdByTeacherId(teacherId);

        String content = "家长您好！老师发起了全班活动：推荐您帮助孩子制定趣味寒假计划，过一个学习成长两不误的快乐寒假！（自愿参与）立即前往";

        Set<Long> parentUserPushId = new HashSet<>();
        for (Long studentId : allUserIdSet) {
            Boolean parentStatusCache = loadBotherStudentParentsCache(studentId);
            if (parentStatusCache) {
                continue;
            }

            setBotherStudentParentsCache(studentId);

            AppMessage studentAppMsg = new AppMessage();
            studentAppMsg.setUserId(studentId);
            studentAppMsg.setMessageType(StudentAppPushType.ACTIVITY_REMIND.getType());
            studentAppMsg.setTitle("2019新年挑战，比比谁的寒假最有趣！");
            studentAppMsg.setContent("亲爱的同学，老师推荐你和爸妈共同制定寒假计划，快来比一比看谁的寒假计划最有趣儿吧！");
            studentAppMsg.setLinkUrl(HOLIDAY_ACTIVITY_INDEX);
            studentAppMsg.setLinkType(1);
            messageCommandServiceClient.getMessageCommandService().createAppMessage(studentAppMsg);

            List<StudentParentRef> parentList = studentLoaderClient.loadStudentParentRefs(studentId);

            if (CollectionUtils.isEmpty(parentList)) {
                // 短信 先不抽取方法了, 测都测完了不敢动
                String mobile = sensitiveUserDataServiceClient.loadUserMobile(studentId);
                if (StringUtils.isNotBlank(mobile)) {
                    SmsMessage smsMessage = new SmsMessage();
                    smsMessage.setMobile(mobile);
                    smsMessage.setType(SmsType.PARENT_WINTER_PLAN_NOTIFY.name());
                    smsMessage.setSmsContent(content + " https://17zyw.cn/2V7MBIyj");
                    smsServiceClient.getSmsService().sendSms(smsMessage);
                }
                continue;
            }

            for (StudentParentRef parent : parentList) {
                boolean notActivity = isNotActivity(parent.getParentId());
                if (notActivity) {
                    // 短信
                    String mobile = sensitiveUserDataServiceClient.loadUserMobile(parent.getParentId());
                    if (StringUtils.isNotBlank(mobile)) {
                        SmsMessage smsMessage = new SmsMessage();
                        smsMessage.setMobile(mobile);
                        smsMessage.setType(SmsType.PARENT_WINTER_PLAN_NOTIFY.name());
                        smsMessage.setSmsContent(content + " https://17zyw.cn/2V7MBIyj");
                        smsServiceClient.getSmsService().sendSms(smsMessage);
                    }
                } else {
                    // push
                    AppMessage message = new AppMessage();
                    message.setUserId(parent.getParentId());
                    message.setTitle("寒假计划");
                    message.setContent(content);
                    message.setLinkType(1);
                    message.setLinkUrl(HOLIDAY_ACTIVITY_INDEX);
                    message.setImageUrl("");
                    message.setMessageType(ParentMessageType.REMINDER.getType());
                    Map<String, Object> extInfo = new HashMap<>();
                    extInfo.put("tag", ParentMessageTag.通知.name());
                    message.setExtInfo(extInfo);
                    messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

                    parentUserPushId.add(parent.getParentId());
                }
            }
        }

        if (CollectionUtils.isNotEmpty(parentUserPushId)) {
            Map<String, Object> pushExtInfo = new HashMap<>();
            pushExtInfo.put("url", HOLIDAY_ACTIVITY_INDEX);
            pushExtInfo.put("s", ParentAppPushType.RECOMMEND_NEWS.name());
            pushExtInfo.put("tag", ParentMessageTag.通知);
            appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, new ArrayList<>(parentUserPushId), pushExtInfo);
        }
    }

    @Override
    public MapMessage delTeacherActivityRef(Long teacherId, String activityType) {
        TeacherActivityEnum teacherActivityEnum = TeacherActivityEnum.valueOf(activityType);
        if (teacherActivityEnum == null) {
            return MapMessage.successMessage("活动类型错误");
        }
        TeacherActivityRef teacherActivityRef = teacherActivityRefDao.loadUserIdTypeId(teacherId, teacherActivityEnum);
        if (teacherActivityRef == null) {
            return MapMessage.successMessage("未找到记录");
        } else {
            teacherActivityRefDao.remove(teacherActivityRef.getId());
        }
        return MapMessage.successMessage("删除成功");
    }

    @NotNull
    private Set<Long> getAllStudentIdByTeacherId(Long teacherId) {
        Set<Long> allTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference()
                .loadTeacherClazzs(allTeacherIds).values().stream()
                .flatMap(Collection::stream)
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(Comparator.comparing(Clazz::getClazzLevel).thenComparing(Clazz::formalizeClazzName))
                .distinct()
                .collect(toList());

        Set<Long> clazzIdSet = clazzList.stream().map(Clazz::getId).collect(Collectors.toSet());
        Map<Long, List<GroupMapper>> teacherGroupMap = teacherLoaderClient.findTeacherAllGroupInClazz(clazzIdSet, teacherId);

        Set<Long> groupIds = new HashSet<>();
        for (Clazz clazz : clazzList) {
            List<GroupMapper> groupMappers = teacherGroupMap.get(clazz.getId());
            if (CollectionUtils.isNotEmpty(groupMappers)) {
                for (GroupMapper groupMapper : groupMappers) {
                    groupIds.add(groupMapper.getId());
                }
            }
        }
        return studentLoaderClient.loadGroupStudentIds(groupIds).values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public List<Long> loadStudentTeacherId(Long studentId) {
        return raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient()
                .findByGroupIds(raikouSDK.getClazzClient().getGroupStudentTupleServiceClient()
                        .findByStudentId(studentId)
                        .stream()
                        .map(GroupStudentTuple::getGroupId)
                        .collect(Collectors.toSet()))
                .stream()
                .map(GroupTeacherTuple::getTeacherId)
                .distinct()
                .collect(toList());
    }

    @Override
    public List<StudentPlanningCount> loadStudentPlanningCountInfo(Long teacherId) {
        Set<Long> allTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        return teacherWinterPlanDPBridge.loadStudentPlanningCountInfo(allTeacherIds);
    }

    @Override
    public List<StudentPlanningWeekInfo> loadStudentPlanningWeekInfo(Long studentId) {
        return teacherWinterPlanDPBridge.loadStudentPlanningWeekInfo(studentId);
    }

    @Override
    public Boolean loadTeacherAssignStatus(Long studentId) {
        List<Long> studentTeacherId = loadStudentTeacherId(studentId);

        for (Long teacherId : studentTeacherId) {
            Long mainTeacherId = getMainTeacherId(teacherId);

            TeacherActivityRef ref = teacherActivityRefDao.loadUserIdTypeId(mainTeacherId, PARENT_CHILD_2018);
            if (ref != null) {
                return true;
            }
        }
        return false;
    }

    private void sendKafka(Long teacherId) {
        // 通知家长端
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("teacherId", teacherId);
        Message msg = Message.newMessage().withPlainTextBody(JsonUtils.toJson(message));
        messagePublisher.publish(msg);

        // 发送 push 、短信相关
        Map<String, Object> noticeMessage = new LinkedHashMap<>();
        noticeMessage.put("teacherId", teacherId);
        Message noticeMsg = Message.newMessage().withPlainTextBody(JsonUtils.toJson(noticeMessage));
        noticePublisher.publish(noticeMsg);
    }

    public Long getMainTeacherId(Long teacherId) {
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        return mainTeacherId == null ? teacherId : mainTeacherId;
    }

    public String genStudentCacheKey(Long student) {
        return "WinterPlaning:StudentId:" + student;
    }

    public void setStudentStatus(Long student, Set<Long> teacherId) {
        if (CollectionUtils.isEmpty(teacherId)) return;

        String cacheKey = genStudentCacheKey(student);
        campaignCacheSystem.CBS.storage.set(cacheKey, calcExpire(), teacherId);
    }

    public Boolean getStudentStatus(Long student, Long teacherId) {
        String cacheKey = genStudentCacheKey(student);
        Object cacheValue = campaignCacheSystem.CBS.storage.load(cacheKey);
        if (cacheValue == null) {
            return false;
        }
        @SuppressWarnings("ALL")
        Set<Long> teacherIdSet = (Set<Long>) cacheValue;
        return teacherIdSet.contains(teacherId);
    }

    /**
     * 是否打扰过家长 key
     */
    public String genBotherStudentParentsCacheKey(Long student) {
        return "WinterPlaning:Parent:StudentId:" + student;
    }

    /**
     * 设置打扰过家长
     */
    public void setBotherStudentParentsCache(Long student) {
        String cacheKey = genBotherStudentParentsCacheKey(student);
        campaignCacheSystem.CBS.storage.set(cacheKey, calcExpire(), System.currentTimeMillis());
    }

    /**
     * 是否打扰过家长
     */
    public Boolean loadBotherStudentParentsCache(Long student) {
        String cacheKey = genBotherStudentParentsCacheKey(student);
        return campaignCacheSystem.CBS.storage.get(cacheKey).containsValue();
    }

    /**
     * 学生是否布置过 key
     */
    public String genStudentAssignCacheKey(Long student) {
        return "WinterPlaning:student:asign:StudentId:" + student;
    }

    /**
     * 学生布置过
     */
    public void setStudentAssignCache(Long student) {
        String cacheKey = genStudentAssignCacheKey(student);
        campaignCacheSystem.CBS.storage.set(cacheKey, calcExpire(), System.currentTimeMillis());
    }

    /**
     * 查询学生布置状态
     */
    public Boolean loadStudentAssignCache(Long student) {
        String cacheKey = genStudentAssignCacheKey(student);
        return campaignCacheSystem.CBS.storage.get(cacheKey).containsValue();
    }

    public int calcExpire() {
        long expire = TEACHER_WINTER_PLAN_END_TIME.getTime() - new Date().getTime();
        return (int) (expire / 1000);
    }

    private static Date TEACHER_WINTER_PLAN_END_TIME;

    static {
        try {
            // 活动 2019-02-24 23:59:59 结束 多存几天吧
            TEACHER_WINTER_PLAN_END_TIME = DateUtils.parseDate("2019-03-01 23:59:59", "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
