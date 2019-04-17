package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.campaign.api.TeacherNewTermPlanService;
import com.voxlearning.utopia.service.campaign.api.constant.PlanConstant;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityRef;
import com.voxlearning.utopia.service.campaign.api.mapper.NewTermClazzClockMapper;
import com.voxlearning.utopia.service.campaign.api.mapper.NewTermPlanMapper;
import com.voxlearning.utopia.service.campaign.api.mapper.NewTermStudentPlanMapper;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherActivityRefDao;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsEnum;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsUtils;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
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

import static com.voxlearning.utopia.service.campaign.api.constant.PlanConstant.PARENT_ACTIVITY_INDEX;
import static com.voxlearning.utopia.service.campaign.api.constant.PlanConstant.STUDENT_ACTIVITY_INDEX;
import static com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum.*;
import static java.util.stream.Collectors.toList;

@Named
@Slf4j
@ExposeService(interfaceClass = TeacherNewTermPlanService.class)
public class TeacherNewTermPlanServiceImpl implements TeacherNewTermPlanService {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private CacheExistsUtils cacheExistsUtils;
    @Inject private CampaignCacheSystem campaignCacheSystem;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private ParentNewTermPlanServiceImpl parentNewTermPlanService;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherActivityRefDao teacherActivityRefDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherNewTermPlanPointServiceImpl teacherNewTermPlanPointService;
    @Inject private UserLoginServiceClient userLoginServiceClient;

    @AlpsPubsubPublisher(topic = "utopia.assign.new.term.plan.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher assignPublisher;

    @Override
    public MapMessage loadTeacherStatus(Long teacherId) {
        TeacherActivityRef load = teacherActivityRefDao.loadUserIdTypeId(getMainTeacherId(teacherId), NEW_TERM_PLAN_2019);
        return MapMessage.successMessage().add("assigned", load != null)
                .add("sign_up_status", getTeacherSignUpStatus(teacherId))
                .add("sign_up_count", getTeacherSignUpCount());
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

        List<NewTermPlanMapper> result = new ArrayList<>();

        for (Clazz clazz : clazzList) {

            Set<Long> assignSet = new HashSet<>();

            NewTermPlanMapper newTermPlanMapper = new NewTermPlanMapper();
            newTermPlanMapper.setClazzId(clazz.getId());
            newTermPlanMapper.setClazzName(clazz.formalizeClazzName());

            List<GroupMapper> groupMappers = teacherGroupMap.get(clazz.getId());
            if (CollectionUtils.isEmpty(groupMappers)) {
                continue;
            }

            groupMappers.sort(Comparator.comparing(GroupMapper::getId));

            Set<Long> studentId = new LinkedHashSet<>();

            for (GroupMapper group : groupMappers) {
                List<User> groupStudentIds = studentLoaderClient.loadGroupStudents(group.getId()).stream()
                        .sorted(Comparator.comparing(LongIdEntity::getId))
                        .collect(toList());

                List<User> studentInfos = groupStudentIds.stream()
                        .filter(i -> !Objects.equals(i.getProfile().getRealname(), "体验账号"))
                        .collect(toList());

                if (CollectionUtils.isEmpty(studentInfos)) {
                    continue;
                }

                Set<Long> groupAllStudentId = studentInfos.stream().map(User::getId).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(groupAllStudentId)) {
                    studentId.addAll(groupAllStudentId);
                }

                for (User studentInfo : studentInfos) {
                    NewTermStudentPlanMapper studentTargets = parentNewTermPlanService.getStudentTargets(studentInfo.getId());
                    if (studentTargets != null) {
                        assignSet.add(studentInfo.getId());
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(studentId)) {
                newTermPlanMapper.setAssignSize(assignSet.size());
                newTermPlanMapper.setSize(studentId.size());
                result.add(newTermPlanMapper);
            }

        }

        return MapMessage.successMessage().add("assigned", load != null).add("groups", result);
    }

    @Override
    public MapMessage assgin(Long id) {
        AtomicCallback<MapMessage> atomicCallback = () -> {
            Long mainTid = getMainTeacherId(id);
            TeacherActivityRef activityRef = teacherActivityRefDao.loadUserIdTypeId(mainTid, NEW_TERM_PLAN_2019);
            if (activityRef == null) {
                activityRef = new TeacherActivityRef();
                activityRef.setUserId(mainTid);
                activityRef.setType(NEW_TERM_PLAN_2019.name());
                teacherActivityRefDao.insert(activityRef);

                sendKafka(mainTid);

                teacherNewTermPlanPointService.teacherAssign(id);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("不可重复布置");
            }
        };

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("NEW_TREM_PLAN_ASSIGN_TEACHER")
                    .keys(id)
                    .callback(atomicCallback)
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("点击过快,请重试...");
        } catch (Exception ex) {
            return MapMessage.errorMessage();
        }
    }

    private void sendKafka(Long mainTid) {
        // 发送 push相关
        Map<String, Object> noticeMessage = new LinkedHashMap<>();
        noticeMessage.put("teacherId", mainTid);
        Message noticeMsg = Message.newMessage().withPlainTextBody(JsonUtils.toJson(noticeMessage));
        assignPublisher.publish(noticeMsg);
    }

    @Override
    public MapMessage loadClockInfo(Long teacherId, Long classId) {

        Map<Long, List<User>> clazzUserMap = studentLoaderClient.loadClazzStudents(Collections.singleton(classId));

        List<User> users = clazzUserMap.get(classId);
        if (CollectionUtils.isEmpty(users)) {
            return MapMessage.errorMessage("当前班级没有学生");
        }

        NewTermClazzClockMapper newTermClazzClockMapper = new NewTermClazzClockMapper();
        List<NewTermClazzClockMapper.StudentUser> studentUsers = new ArrayList<>();

        Integer exceedThreeClockPeople = 0;
        Integer exceedSevenClockPeople = 0;
        Integer exceedTwentyOneClockPeople = 0;

        for (User user : users) {
            if (Objects.equals(user.getProfile().getRealname(), "体验账号")) {
                continue;
            }
            NewTermStudentPlanMapper planMapper = parentNewTermPlanService.getStudentTargets(user.getId());
            if (planMapper == null) {
                continue;
            }

            NewTermClazzClockMapper.StudentUser studentUser = new NewTermClazzClockMapper.StudentUser();
            studentUser.setName(user.getProfile().getRealname());
            studentUser.setStudentId(user.getId());
            studentUsers.add(studentUser);

            boolean exceedThree = false;
            boolean exceedSeven = false;
            boolean exceedTwentyOne = false;

            //统计学生每个目标的打卡数
            List<NewTermStudentPlanMapper.Plan> plans = planMapper.getPlans();
            for (NewTermStudentPlanMapper.Plan plan : plans) {
                Long clockDays = loadStudentNewTermTargetCache(user.getId(), plan.getId());
                if (clockDays >= 21) {
                    exceedThree = true;
                    exceedSeven = true;
                    exceedTwentyOne = true;
                } else if (clockDays >= 7) {
                    exceedSeven = true;
                    exceedThree = true;
                } else if (clockDays >= 3) {
                    exceedThree = true;
                }
            }

            if (exceedThree) {
                exceedThreeClockPeople++;
            }
            if (exceedSeven) {
                exceedSevenClockPeople++;
            }
            if (exceedTwentyOne) {
                exceedTwentyOneClockPeople++;
            }

        }
        newTermClazzClockMapper.setStudentUsers(studentUsers);
        newTermClazzClockMapper.setExceedThreeClockPeople(exceedThreeClockPeople);
        newTermClazzClockMapper.setExceedSevenClockPeople(exceedSevenClockPeople);
        newTermClazzClockMapper.setExceedTwentyOneClockPeople(exceedTwentyOneClockPeople);

        newTermClazzClockMapper.setClockPeople(studentUsers.size());


        return MapMessage.successMessage().add("data", newTermClazzClockMapper);
    }

    @Override
    public MapMessage loadStudentClockInfo(Long studentId) {

        NewTermStudentPlanMapper planMapper = parentNewTermPlanService.getStudentTargets(studentId);
        if (planMapper == null) {
            return MapMessage.errorMessage("该活动已下线");
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        List<NewTermStudentPlanMapper.Plan> plans = planMapper.getPlans();
        for (NewTermStudentPlanMapper.Plan plan : plans) {
            Long clockDays = loadStudentNewTermTargetCache(studentId, plan.getId());
            Map<String, Object> targetMap = new HashMap<>();
            targetMap.put("targetName", plan.getName());
            targetMap.put("clockDays", clockDays);
            targetMap.put("plan_id", plan.getId());

            resultList.add(targetMap);
        }
        Integer cacheVal = loadPraiseStudentCache(studentId);

        return MapMessage.successMessage().add("data", resultList).add("isShow", cacheVal == null);
    }

    @Override
    public MapMessage praise(Long studentId) {

        Integer cacheVal = loadPraiseStudentCache(studentId);
        if (cacheVal == null) {
            setPraiseStudentCache(studentId);

            List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);

            sendStudentMsg(studentId, "亲爱的同学，老师为你的新学期计划点赞，记得每天坚持打卡，你就是老师心目中的超级行动派！");

            for (StudentParent studentParent : studentParents) {
                sendParentMsg(studentParent.getParentUser().getId(), "尊敬的家长，老师为您孩子的新学期计划点赞，记得每天陪伴Ta打卡哦，努力实现所有计划哦，每天坚持一点点，成长进步多一点！");
            }
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage sendParentPush(Long id) {
        Boolean teacherAssignStatus = loadTeacherAssignStatus(id);
        if (teacherAssignStatus) {
            return MapMessage.errorMessage("老师已经布置");
        }

        boolean exists = cacheExistsUtils.exists(CacheExistsEnum.STUDENT_PARTICIPATE, id);
        if (exists) {
            return MapMessage.errorMessage("学生已经布置");
        }
        cacheExistsUtils.set(CacheExistsEnum.STUDENT_PARTICIPATE, id);

        NewTermStudentPlanMapper studentTargets = parentNewTermPlanService.getStudentTargets(id);
        if (studentTargets != null) {
            return MapMessage.errorMessage("学生已制定过计划");
        }

        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(id);
        if (CollectionUtils.isEmpty(studentParents)) {
            return MapMessage.errorMessage("当前孩子没有家长");
        }
        for (StudentParent studentParent : studentParents) {
            Long parentId = studentParent.getParentUser().getId();

            boolean noExists = cacheExistsUtils.noExists(CacheExistsEnum.STUDENT_JOIN_NOTICE, parentId);
            if (noExists) {
                cacheExistsUtils.set(CacheExistsEnum.STUDENT_JOIN_NOTICE, parentId);
                sendParentMsg(parentId,
                        "尊敬的家长，您的孩子邀请您和Ta一起制定《新学期快速收心计划》，为孩子的上进心点赞，坚持21天成就一个好习惯，即刻去定计划！");

                //  学生发起活动的短信先停掉
                /*boolean notActivity = isNotActivity(parentId);
                if (notActivity) {
                    String mobile = sensitiveUserDataServiceClient.loadUserMobile(parentId);
                    if (StringUtils.isNotBlank(mobile)) {
                        SmsMessage smsMessage = new SmsMessage();
                        smsMessage.setMobile(mobile);
                        smsMessage.setType(SmsType.PARENT_NEWTERM_PLAN_NOTIFY.name());
                        smsMessage.setSmsContent("尊敬的家长，您的孩子邀请您和Ta一起制定《新学期快速收心计划》，为孩子的上进心点赞，坚持21天成就一个好习惯，即刻去定计划！");
                        smsServiceClient.getSmsService().sendSms(smsMessage);
                    }
                }*/
            }
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage getTargetDetail(Long id) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(id);
        NewTermStudentPlanMapper studentTargets = parentNewTermPlanService.getStudentTargets(id);
        if (studentTargets == null) {
            return MapMessage.successMessage().add("data", new ArrayList<>())
                    .add("day", 0)
                    .add("left_days", 0)
                    .add("studentName", studentDetail.fetchRealname())
                    .add("sid", studentDetail.getId())
                    .add("participate_count", getParticipateCount())
                    .add("sign_up_count", parentNewTermPlanService.getSignUpCount());
        }

        List<Map<String, Object>> resultList = new ArrayList<>();

        List<NewTermStudentPlanMapper.Plan> plans = studentTargets.getPlans();
        for (NewTermStudentPlanMapper.Plan plan : plans) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", plan.getId());
            map.put("name", StringUtils.isEmpty(plan.getDesc()) ? plan.getName() : plan.getDesc());
            map.put("clockDays", loadStudentNewTermTargetCache(id, plan.getId()));
            resultList.add(map);
        }

        Date beginDate = studentTargets.getBeginDate();
        return MapMessage.successMessage()
                .add("data", resultList)
                .add("day", beginDate == null ? 0 : DateUtils.dayDiff(new Date(), beginDate))
                .add("left_days", studentTargets.getEndDate() == null ? 0 : Math.max(DateUtils.dayDiff(studentTargets.getEndDate(), new Date()), 0))
                .add("studentName", studentDetail.fetchRealname())
                .add("sid", studentDetail.getId())
                .add("participate_count", getParticipateCount())
                .add("sign_up_count", parentNewTermPlanService.getSignUpCount());
    }

    @Override
    public Boolean loadTeacherAssignStatus(Long studentId) {
        List<Long> studentTeacherId = loadStudentTeacherId(studentId);

        for (Long teacherId : studentTeacherId) {
            Long mainTeacherId = getMainTeacherId(teacherId);

            TeacherActivityRef ref = teacherActivityRefDao.loadUserIdTypeId(mainTeacherId, NEW_TERM_PLAN_2019);
            if (ref != null) {
                return true;
            }
        }
        return false;
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

    @NotNull
    public Set<Long> getAllStudentIdByTeacherId(Long teacherId) {
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

    private void sendStudentMsg(Long studentId, String msg) {
        // 系统消息
        AppMessage message = new AppMessage();
        message.setUserId(studentId);
        message.setTitle(PlanConstant.NEW_TERM_PLAN_ACTIVITY_NAME);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(STUDENT_ACTIVITY_INDEX);
        message.setImageUrl("");
        message.setMessageType(StudentAppPushType.ACTIVITY_REMIND.getType());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        // push
        Map<String, Object> extInfo = MapUtils.map(
                "s", StudentAppPushType.ACTIVITY_REMIND.getType(),
                "link", STUDENT_ACTIVITY_INDEX,
                "t", "h5",
                "key", "j");
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.STUDENT, Collections.singletonList(studentId), extInfo);
    }

    private void sendParentMsg(Long parentId, String msg) {
        // 系统消息
        AppMessage message = new AppMessage();
        message.setUserId(parentId);
        message.setTitle(PlanConstant.NEW_TERM_PLAN_ACTIVITY_NAME);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(PARENT_ACTIVITY_INDEX);
        message.setImageUrl("");
        message.setMessageType(ParentMessageType.REMINDER.getType());
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("tag", ParentMessageTag.通知.name());
        message.setExtInfo(extInfo);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        //发送jpush
        Map<String, Object> extras = new HashMap<>();
        extras.put("url", PARENT_ACTIVITY_INDEX);
        extras.put("tag", ParentMessageTag.通知.name());
        extras.put("s", ParentAppPushType.ACTIVITY.name());
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PARENT, Collections.singletonList(parentId), extras);
    }


    /**
     * 点赞学生缓存key
     *
     * @param studentId
     * @return
     */
    public String genPraiseStudentCacheKey(Long studentId) {
        return CacheKeyGenerator.generateCacheKey("NewTermPlan:Praise:Student", new String[]{"SID"}, new Object[]{studentId});
    }

    public Integer loadPraiseStudentCache(Long studentId) {
        String cacheKey = genPraiseStudentCacheKey(studentId);
        return campaignCacheSystem.CBS.storage.load(cacheKey);
    }

    public void setPraiseStudentCache(Long studentId) {
        String cacheKey = genPraiseStudentCacheKey(studentId);
        campaignCacheSystem.CBS.storage.set(cacheKey, calcExpire(), 1);
    }


    /**
     * 学生目标打卡天数缓存key
     *
     * @param studentId
     * @param targetId
     * @return
     */
    public String genStudentNewTermTargetCacheKey(Long studentId, String targetId) {
        return CacheKeyGenerator.generateCacheKey("NewTermPlan:Target:Clock", new String[]{"SID", "TID"}, new Object[]{studentId, targetId});
    }

    public Long loadStudentNewTermTargetCache(Long studentId, String targetId) {
        String cacheKey = genStudentNewTermTargetCacheKey(studentId, targetId);
        Object result = campaignCacheSystem.CBS.storage.load(cacheKey);
        if (result == null) {
            return 0L;
        }

        return SafeConverter.toLong((String) result);
    }

    public void setStudentNewTermTargetCache(Long studentId, String targetId) {
        String cacheKey = genStudentNewTermTargetCacheKey(studentId, targetId);
        campaignCacheSystem.CBS.storage.incr(cacheKey, 1L, 1L, calcExpire());
    }

    private static final String NEW_TERM_PARTICIPATE_COUNT = "NEW_TERM_PARTICIPATE_COUNT";
    private static final long initValue = 311226L;

    public Long incrParticipateCount(Long incr) {
        return campaignCacheSystem.CBS.storage.incr(NEW_TERM_PARTICIPATE_COUNT, incr, initValue, calcExpire());
    }

    public Long setParticipateCount(Long incr) {
        campaignCacheSystem.CBS.storage.delete(NEW_TERM_PARTICIPATE_COUNT);
        return campaignCacheSystem.CBS.storage.incr(NEW_TERM_PARTICIPATE_COUNT, incr, incr, calcExpire());
    }

    public Long getParticipateCount() {
        CacheObject<Object> participateCount = campaignCacheSystem.CBS.storage.get(NEW_TERM_PARTICIPATE_COUNT);
        if (participateCount == null) {
            return initValue;
        } else {
            return SafeConverter.toLong(participateCount.getValue());
        }
    }

    @Override
    public MapMessage teacherSignUp(Long id) {
        TeacherActivityRef teacherActivityRef = teacherActivityRefDao.loadUserIdTypeId(id, NEW_TERM_PLAN_2019_SING_UP_T);
        if (teacherActivityRef == null) {
            teacherActivityRef = new TeacherActivityRef();
            teacherActivityRef.setUserId(id);
            teacherActivityRef.setType(NEW_TERM_PLAN_2019_SING_UP_T.name());
            teacherActivityRefDao.upsert(teacherActivityRef);
            incrTeacherSignUpCount(1L);
        }
        return MapMessage.successMessage();
    }

    private static final String NEW_TERN_PLAN_2019_2_SIGN_UP_TEACHER = "NEW_TERN_PLAN_2019_2_SIGN_UP_TEACHER";

    @Override
    public Long getTeacherSignUpCount() {
        CacheObject<Object> cacheObject = campaignCacheSystem.CBS.storage.get(NEW_TERN_PLAN_2019_2_SIGN_UP_TEACHER);
        if (cacheObject.containsValue()) {
            return SafeConverter.toLong(cacheObject.getValue().toString().trim());
        }
        return 0L;
    }

    @Override
    public Long incrTeacherSignUpCount(Long incr) {
        return campaignCacheSystem.CBS.storage.incr(NEW_TERN_PLAN_2019_2_SIGN_UP_TEACHER, incr, 1L, 0);
    }

    @Override
    public Long setTeacherSignUpCount(Long count) {
        campaignCacheSystem.CBS.storage.delete(NEW_TERN_PLAN_2019_2_SIGN_UP_TEACHER);
        return campaignCacheSystem.CBS.storage.incr(NEW_TERN_PLAN_2019_2_SIGN_UP_TEACHER, count, count, 0);
    }

    @Override
    public Boolean getTeacherSignUpStatus(Long studentId) {
        return teacherActivityRefDao.loadUserIdTypeId(studentId, NEW_TERM_PLAN_2019_SING_UP_T) != null;
    }

    public Long getMainTeacherId(Long teacherId) {
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        return mainTeacherId == null ? teacherId : mainTeacherId;
    }

    public int calcExpire() {
        long expire = NEW_TERM_PLAN_END_TIME.getTime() - new Date().getTime();
        return (int) (expire / 1000);
    }

    private static Date NEW_TERM_PLAN_END_TIME;

    static {
        try {
            NEW_TERM_PLAN_END_TIME = DateUtils.parseDate("2019-05-10 00:00:00", "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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


}
