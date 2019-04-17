package com.voxlearning.utopia.service.campaign.impl.service;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.galaxy.service.studyplanning.api.data.StudyPlanningItemMapper;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.api.constant.WarmHeartPlanConstant;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.campaign.api.WarmHeartPlanService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityRef;
import com.voxlearning.utopia.service.campaign.api.enums.WarmHeartTargetEnum;
import com.voxlearning.utopia.service.campaign.api.mapper.NewTermPlanMapper;
import com.voxlearning.utopia.service.campaign.api.mapper.WarmHeartClazzClockMapper;
import com.voxlearning.utopia.service.campaign.api.mapper.WarmHeartPlanMapper;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherActivityRefDao;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsEnum;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsUtils;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.WarmHeartPlanConstant.*;
import static com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum.PARENT_CHILD_2018;
import static com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum.WARM_HEART_T;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Named
@Slf4j
@ExposeService(interfaceClass = WarmHeartPlanService.class)
public class WarmHeartPlanServiceImpl implements WarmHeartPlanService {
    @Inject
    private RaikouSDK raikouSDK;
    @Inject
    private TeacherActivityRefDao teacherActivityRefDao;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private CampaignCacheSystem campaignCacheSystem;
    @Inject
    private NewTermPlanDPBridge newTermPlanDPBridge;
    @Inject
    private InternalWarmHeartPlanCacheService internalWarmHeartPlanCacheService;
    @Inject
    private UserIntegralServiceClient userIntegralServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private UserServiceClient userServiceClient;
    @Inject
    private CouponServiceClient couponServiceClient;
    @Inject
    private WarmHeartPlanPointServiceImpl warmHeartPlanPointService;
    @Inject
    private CacheExistsUtils cacheExistsUtils;

    @AlpsPubsubPublisher(topic = "utopia.assign.warm.heart.plan.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher assignPublisher;

    @AlpsPubsubPublisher(topic = "utopia.reward.toby.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher tobyPublisher;

    @Override
    public MapMessage loadTeacherStatus(User user) {

        long teacherId = user.getId();
        Long mainTeacherId = getMainTeacherId(teacherId);

        TeacherActivityRef teacherActivityRef = teacherActivityRefDao.loadUserIdTypeId(mainTeacherId, WARM_HEART_T);
        Long participateCount = getParticipateCount();

        Long teacher21DaySize = internalWarmHeartPlanCacheService.getTeacher21DaySize(teacherId);
        boolean canGetCoupon = teacher21DaySize >= 15L;
        boolean lightCertificate = teacher21DaySize >= 5L;

        String mdPhone = "";
        String userMobile = sensitiveUserDataServiceClient.loadUserMobile(teacherId);
        if (StringUtils.isNotBlank(userMobile)) {
            mdPhone = String.format("%s****%s", userMobile.substring(0, 3), userMobile.substring(7));
        }

        return MapMessage.successMessage()
                .add("teacherId", user.getId())
                .add("assigned", teacherActivityRef != null)
                .add("teacherCnt", participateCount)
                .add("lightCertificate", lightCertificate)
                .add("canGetCoupon", canGetCoupon)
                .add("tname", user.getProfile().getRealname())
                .add("tel", mdPhone);

    }


    @Override
    public MapMessage assgin(Long teacherId) {

        AtomicCallback<MapMessage> atomicCallback = () -> {
            Long mainTeacherId = getMainTeacherId(teacherId);

            TeacherActivityRef activityRef = teacherActivityRefDao.loadUserIdTypeId(mainTeacherId, WARM_HEART_T);
            if (activityRef == null) {
                activityRef = new TeacherActivityRef();
                activityRef.setUserId(mainTeacherId);
                activityRef.setType(WARM_HEART_T.name());
                teacherActivityRefDao.insert(activityRef);
                incrParticipateCount(1L);

                sendKafka(mainTeacherId);

                warmHeartPlanPointService.teacherAssign(mainTeacherId);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("不可重复布置");
            }
        };

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("WARM_HEART_PLAN_ASSIGN_TEACHER")
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
    public MapMessage loadTeacherClazzInfo(Long teacherId) {

        Long mainTeacherId = getMainTeacherId(teacherId);
        TeacherActivityRef load = teacherActivityRefDao.loadUserIdTypeId(mainTeacherId, PARENT_CHILD_2018);

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
                    WarmHeartPlanMapper studentTargets = getStudentTargets(studentInfo.getId());
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

        return MapMessage.successMessage().add("groups", result);

    }

    @Override
    public MapMessage loadStudentStatus(Long id, Long studentId) {

        if (studentId == 0L) {
            List<StudentParentRef> studentRefs = parentLoaderClient.loadParentStudentRefs(id);
            if (CollectionUtils.isEmpty(studentRefs)) {
                return MapMessage.errorMessage("请指定一个孩子");
            } else {
                studentId = studentRefs.get(0).getStudentId();
            }
        }

        WarmHeartPlanMapper studentTargets = getStudentTargets(studentId);
        Long stuParticipateCount = getStuParticipateCount();

        if (studentTargets == null)
            return MapMessage.successMessage()
                    .add("assigned", false).add("stuCnt", stuParticipateCount);

        return MapMessage.successMessage()
                .add("assigned", true)
                .add("stuCnt", stuParticipateCount);
    }

    @Override
    public MapMessage warmHeartTargets(Long id, String imgUrl, Long studentId) {

        if (studentId == 0L) {
            List<StudentParentRef> studentRefs = parentLoaderClient.loadParentStudentRefs(id);
            if (CollectionUtils.isEmpty(studentRefs)) {
                return MapMessage.errorMessage("请指定一个孩子");
            } else {
                studentId = studentRefs.get(0).getStudentId();
            }
        }
        String name = "";
        try {
            name = getStudentCallName(id, studentId);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }

        WarmHeartPlanMapper studentTargets = getStudentTargets(studentId);
        List<WarmHeartPlanMapper.Plan> plans = studentTargets == null ? null : studentTargets.getPlans();

        WarmHeartTargetEnum[] targetEnums = WarmHeartTargetEnum.values();
        List<Map<String, Object>> studyList = new ArrayList<>();
        List<Map<String, Object>> lifeList = new ArrayList<>();
        List<Map<String, Object>> userList = new ArrayList<>();
        final long sid = studentId;

        for (WarmHeartTargetEnum targetEnum : targetEnums) {

            Map<String, Object> map = new HashMap<>();
            map.put("id", targetEnum.getId());
            map.put("planning_name", targetEnum.name());
            map.put("icon", targetEnum.getIcon());
            map.put("start_time", targetEnum.getStartTime());
            map.put("end_time", targetEnum.getEndTime());
            map.put("chooseNum", getTargetChooseNum(targetEnum.getId()));
            map.put("type", getTypeByTargetEnum(targetEnum));
            if (studentTargets == null || CollectionUtils.isEmpty(studentTargets.getPlans())) {
                map.put("selected", false);
            } else {
                WarmHeartPlanMapper.Plan p = plans.stream()
                        .filter(plan -> {
                            boolean sameEquals = transferPlanName(plan.getName()).equals(targetEnum.name());
                            if (sameEquals) {
                                Long clockDays = getStudentWarmHeartTargetCache(sid, plan.getId());
                                if (clockDays < 21) return true;
                            }
                            return sameEquals;
                        })
                        .findFirst().orElse(null);
                map.put("selected", p != null);
            }

            if (targetEnum.getTypeId().equals(1)) {
                studyList.add(map);
            } else {
                lifeList.add(map);
            }

        }

        if (CollectionUtils.isNotEmpty(plans)) {
            for (WarmHeartPlanMapper.Plan plan : plans) {
                if (WarmHeartTargetEnum.iconMap.containsKey(transferPlanName(plan.getName()))) {
                    continue;
                }
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("planning_name", transferPlanName(plan.getName()));
                userMap.put("selected", true);
                userList.add(userMap);
            }

        }

        return MapMessage.successMessage().add("study", studyList).add("life", lifeList).add("user", userList)
                .add("name", name).add("photo", imgUrl);
    }

    private String getStudentCallName(Long id, Long studentId) {
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null) throw new RuntimeException("学生不存在");

        List<StudentParent> studentParentList = parentLoaderClient.loadStudentParents(studentId);

        if (CollectionUtils.isEmpty(studentParentList)) throw new RuntimeException("学生没有家长");

        StudentParent parent = studentParentList.stream()
                .filter(studentParent -> id.equals(studentParent.getParentUser().getId()))
                .findFirst().orElse(null);

        if (parent == null) throw new RuntimeException("学生没有家长");
        String name = student.getProfile().getRealname() + parent.getCallName();

        return name;
    }

    private String getTypeByTargetEnum(WarmHeartTargetEnum targetEnum) {

        if (targetEnum.getId() == 2) return "PICLISTEN";  //点读机
        if (targetEnum.getId() == 3) return "DUBBING";       //趣味配音
        if (targetEnum.getId() == 1) return "READING_ENGLISH";    //绘本

        return "USER_DEFINED";
    }

    @Override
    public MapMessage loadStudentStatus(User user) {

        Long studentId = user.getId();
        String sname = user.getProfile().getRealname();
        boolean lightCertificate = false;
        WarmHeartPlanMapper studentTargets = getStudentTargets(studentId);
        Long stuParticipateCount = getStuParticipateCount();

        List<Map<String, Object>> resultList = new ArrayList<>();

        if (studentTargets == null || CollectionUtils.isEmpty(studentTargets.getPlans()))
            return MapMessage.successMessage().add("assigned", false).add("stuCnt", stuParticipateCount)
                    .add("lightCertificate", lightCertificate).add("sid", studentId)
                    .add("sname", sname).add("data", resultList);

        List<WarmHeartPlanMapper.Plan> plans = studentTargets.getPlans();
        for (WarmHeartPlanMapper.Plan plan : plans) {
            Map<String, Object> map = new HashMap<>();

            Long clockDays = getStudentWarmHeartTargetCache(studentId, plan.getId());
            long leftDays = plan.getEndDate() == null ? 0 : Math.max(0, DateUtils.dayDiff(plan.getEndDate(), new Date()));
            if (clockDays == 21) {
                lightCertificate = true;
            }

            map.put("targetName", transferPlanName(plan.getName()));
            map.put("targetIcon", WarmHeartTargetEnum.iconMap.getOrDefault(transferPlanName(plan.getName()),
                    "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/define-icon.png"));
            map.put("targetDays", 21);
            map.put("clockDays", clockDays);
            map.put("leftDays", leftDays);
            resultList.add(map);
        }

        return MapMessage.successMessage().add("assigned", true).add("stuCnt", stuParticipateCount)
                .add("lightCertificate", lightCertificate).add("sid", studentId)
                .add("sname", sname).add("data", resultList);
    }

    @Override
    public MapMessage saveWarmHeartPlans(long studentId, String plans) {

        List<StudyPlanningItemMapper> itemMappers = JsonUtils.fromJsonToList(plans, StudyPlanningItemMapper.class);

        WarmHeartPlanMapper studentTargets = getStudentTargets(studentId);
        if (studentTargets != null) {
            List<WarmHeartPlanMapper.Plan> planList = studentTargets.getPlans();
            long count = 0L;

            for (WarmHeartPlanMapper.Plan plan : planList) {
                //报名日期内，统计学生未完成的目标
                Long clockDays = getStudentWarmHeartTargetCache(studentId, plan.getId());
                if (clockDays < 21) {
                    count++;
                }
            }

            if (count + itemMappers.size() > WarmHeartPlanConstant.MAX_TARGET_NUM) {
                return MapMessage.errorMessage("最多只可设置3个目标");
            }
        }

        for (StudyPlanningItemMapper itemMapper : itemMappers) {
            incrTargetChooseNum(SafeConverter.toInt(itemMapper.getId()));
            itemMapper.setId(null);
        }

        List<Map<String, Object>> maps = newTermPlanDPBridge.saveActivityPlans(studentId, JsonUtils.toJson(itemMappers));

        if (RuntimeMode.le(Mode.STAGING)) {
            log.info("saveNewTermActivityPlans response userId: {} {}", studentId, JSON.toJSONString(maps));
        }


        setStudentTargets(studentId, maps);

        boolean noExists = cacheExistsUtils.noExists(CacheExistsEnum.WARM_HEART_PARENT_FIRST_SET_TARGET, studentId);
        if (noExists) {
            cacheExistsUtils.set(CacheExistsEnum.WARM_HEART_PARENT_FIRST_SET_TARGET, studentId);
            incrStuParticipateCount(1L);
            sendTeacherIntegral(studentId);

            Set<Long> teacherIds = loadTeachersByStudentId(studentId);
            for (Long teacherId : teacherIds) {
                sendTeacherMsg(teacherId, "【温馨提醒】Hi～您又有一组学生家庭制定了亲子陪伴计划喔！孩子和家长的努力离不开您的肯定！马上去给他们点赞❤>>");
            }
        }
        return MapMessage.successMessage().add("data", maps);
    }

    @Override
    public MapMessage loadClockInfo(Long tid, long classId) {

        Map<Long, List<User>> clazzUserMap = studentLoaderClient.loadClazzStudents(Collections.singleton(classId));

        List<User> users = clazzUserMap.get(classId);
        if (CollectionUtils.isEmpty(users)) {
            return MapMessage.errorMessage("当前班级没有学生");
        }

        WarmHeartClazzClockMapper warmHeartPlanMapper = new WarmHeartClazzClockMapper();
        List<WarmHeartClazzClockMapper.StudentUser> studentUsers = new ArrayList<>();

        Integer exceedThreeClockPeople = 0;
        Integer exceedTwentyOneClockPeople = 0;

        for (User user : users) {
            if (Objects.equals(user.getProfile().getRealname(), "体验账号")) {
                continue;
            }
            WarmHeartPlanMapper planMapper = getStudentTargets(user.getId());
            if (planMapper == null) {
                continue;
            }

            WarmHeartClazzClockMapper.StudentUser studentUser = new WarmHeartClazzClockMapper.StudentUser();
            studentUser.setName(user.getProfile().getRealname());
            studentUser.setStudentId(user.getId());
            studentUsers.add(studentUser);

            boolean exceedThree = false;
            boolean exceedTwentyOne = false;

            //统计学生每个目标的打卡数
            List<WarmHeartPlanMapper.Plan> plans = planMapper.getPlans();
            for (WarmHeartPlanMapper.Plan plan : plans) {
                Long clockDays = getStudentWarmHeartTargetCache(user.getId(), plan.getId());
                if (clockDays >= 21) {
                    exceedThree = true;
                    exceedTwentyOne = true;
                } else if (clockDays >= 3) {
                    exceedThree = true;
                }
            }

            if (exceedThree) {
                exceedThreeClockPeople++;
            }

            if (exceedTwentyOne) {
                exceedTwentyOneClockPeople++;
            }

        }
        warmHeartPlanMapper.setStudentUsers(studentUsers);
        warmHeartPlanMapper.setExceedThreeClockPeople(exceedThreeClockPeople);
        warmHeartPlanMapper.setExceedTwentyOneClockPeople(exceedTwentyOneClockPeople);

        warmHeartPlanMapper.setClockPeople(studentUsers.size());

        return MapMessage.successMessage().add("data", warmHeartPlanMapper);
    }

    @Override
    public MapMessage loadStudentClockInfo(long studentId) {
        WarmHeartPlanMapper planMapper = getStudentTargets(studentId);
        if (planMapper == null) {
            return MapMessage.errorMessage("该活动已下线");
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        List<WarmHeartPlanMapper.Plan> plans = planMapper.getPlans();
        for (WarmHeartPlanMapper.Plan plan : plans) {
            Long clockDays = getStudentWarmHeartTargetCache(studentId, plan.getId());
            Map<String, Object> targetMap = new HashMap<>();
            targetMap.put("targetName", transferPlanName(plan.getName()));
            targetMap.put("clockDays", clockDays);
            targetMap.put("plan_id", plan.getId());

            resultList.add(targetMap);
        }
        Integer cacheVal = loadPraiseStudentCache(studentId);

        return MapMessage.successMessage().add("data", resultList).add("isShow", cacheVal == null);
    }

    @Override
    public MapMessage praise(long studentId) {

        Integer cacheVal = loadPraiseStudentCache(studentId);
        if (cacheVal == null) {
            setPraiseStudentCache(studentId);

            List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);


            sendStudentMsg(studentId, "【老师的表扬】一份来自老师的爱，你又收到了一个「赞」哟～每天和爸妈做一些小事，总是那么快乐。为爸妈继续加油！");

            for (StudentParent studentParent : studentParents) {
                sendParentMsg(studentParent.getParentUser().getId(), "【老师的表扬】叮～老师已为您的用心陪伴点赞了！世间有一种幸福叫陪伴孩子成长，戳我，坚持今日份的亲子打卡！>>");
            }
        }

        return MapMessage.successMessage();
    }

    @Override
    public Boolean loadTeacherAssignStatus(Long studentId) {
        Set<Long> studentTeacherId = loadTeachersByStudentId(studentId);

        for (Long teacherId : studentTeacherId) {
            TeacherActivityRef ref = teacherActivityRefDao.loadUserIdTypeId(teacherId, WARM_HEART_T);
            if (ref != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MapMessage backdoorDelKey(String key) {
        return internalWarmHeartPlanCacheService.backdoorDelKey(key);
    }

    @Override
    public MapMessage backdoorIncrKey(String key, Long incr, Long init) {
        return internalWarmHeartPlanCacheService.backdoorIncrKey(key, incr, init);
    }

    /**
     * 点赞学生缓存key
     *
     * @param studentId
     * @return
     */
    public String genPraiseStudentCacheKey(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(WARM_HEART_PRAISE_STUDENT, new String[]{"SID"}, new Object[]{studentId});
    }

    public Integer loadPraiseStudentCache(Long studentId) {
        String cacheKey = genPraiseStudentCacheKey(studentId);
        return campaignCacheSystem.CBS.storage.load(cacheKey);
    }

    public void setPraiseStudentCache(Long studentId) {
        String cacheKey = genPraiseStudentCacheKey(studentId);
        campaignCacheSystem.CBS.storage.set(cacheKey, calcExpire(), 1);
    }

    public void delStudentTargets(Long studentId, String planId) {
        WarmHeartPlanMapper planMapper = getStudentTargets(studentId);
        if (planMapper == null || CollectionUtils.isEmpty(planMapper.getPlans())) return;

        String cacheKey = genStudentTargetCacheKey(studentId);
        List<WarmHeartPlanMapper.Plan> planList = planMapper.getPlans().stream()
                .filter(plan -> !plan.getId().equals(planId)).collect(toList());
        planMapper.setPlans(planList);
        campaignCacheSystem.CBS.storage.set(cacheKey, calcExpire(), planMapper);

    }

    public void setStudentTargets(Long student, List<Map<String, Object>> maps) {
        WarmHeartPlanMapper planMapper = getStudentTargets(student);
        String cacheKey = genStudentTargetCacheKey(student);
        planMapper = planMapper == null ? new WarmHeartPlanMapper() : planMapper;

        Date beginDay = new Date();
        Date dayStart = DateUtils.getDayStart(beginDay);
        Date endDay = DateUtils.addDays(dayStart, 30);
        Date dayEnd = DateUtils.getDayEnd(endDay);
        List<WarmHeartPlanMapper.Plan> planList = new ArrayList<>();
        for (Map<String, Object> map : maps) {

            WarmHeartPlanMapper.Plan plan = new WarmHeartPlanMapper.Plan();
            plan.setId(SafeConverter.toString(map.get("id")));
            plan.setName(SafeConverter.toString(map.get("planning_name")));
            plan.setBeginDate(dayStart);
            plan.setEndDate(dayEnd);
            planList.add(plan);
        }

        if (CollectionUtils.isEmpty(planMapper.getPlans())) {
            planMapper.setPlans(planList);
        } else
            planMapper.getPlans().addAll(planList);

        campaignCacheSystem.CBS.storage.set(cacheKey, calcExpire(), planMapper);
    }

    public WarmHeartPlanMapper getStudentTargets(Long student) {
        String cacheKey = genStudentTargetCacheKey(student);
        return campaignCacheSystem.CBS.storage.load(cacheKey);
    }

    /**
     * 暖心计划目标ID
     *
     * @param studentId
     * @return
     */
    public String genStudentTargetCacheKey(Long studentId) {
        return WARM_HEART_STUDENT_ID_TARGET + studentId;
    }


    public void incrTargetChooseNum(Integer id) {
        String cacheKey = genTargetChooseCacheKey(id);
        campaignCacheSystem.CBS.storage.incr(cacheKey, 1L, 1L, calcExpire());
    }

    public Long getTargetChooseNum(Integer id) {
        String cacheKey = genTargetChooseCacheKey(id);
        return campaignCacheSystem.CBS.storage.load(cacheKey) == null ? 0L : SafeConverter.toLong(campaignCacheSystem.CBS.storage.load(cacheKey));
    }

    /**
     * 暖心计划目标已被xx人选择
     *
     * @param id
     * @return
     */
    public String genTargetChooseCacheKey(Integer id) {
        return WARM_HEART_TARGET_CHOOSE + id;
    }

    /**
     * 学生目标打卡天数缓存key
     *
     * @param studentId
     * @param targetId
     * @return
     */
    public String genStudentWarmHeartTargetCacheKey(Long studentId, String targetId) {
        return CacheKeyGenerator.generateCacheKey(WARM_HEART_STUDENT_TARGET_CLOCK, new String[]{"SID", "TID"}, new Object[]{studentId, targetId});
    }

    public Long getStudentWarmHeartTargetCache(Long studentId, String targetId) {
        String cacheKey = genStudentWarmHeartTargetCacheKey(studentId, targetId);
        Object result = campaignCacheSystem.CBS.storage.load(cacheKey);
        if (result == null) {
            return 0L;
        }

        return SafeConverter.toLong(result);
    }

    public void setStudentWarmHeartTargetCache(Long studentId, String targetId) {
        String cacheKey = genStudentWarmHeartTargetCacheKey(studentId, targetId);
        Long incr = campaignCacheSystem.CBS.storage.incr(cacheKey, 1L, 1L, calcExpire());

        if (Objects.equals(incr, 14L)) {
            List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
            for (StudentParent studentParent : studentParents) {
                sendParentMsg(studentParent.getParentUser().getId(), "【暖心亲子计划】加油，您已高质量陪伴孩子14天！坚持21天打卡，和孩子感受成长的幸福。>>\uD83D\uDC49");
            }
        }

        if (Objects.equals(incr, 20L)) {
            List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
            for (StudentParent studentParent : studentParents) {
                sendParentMsg(studentParent.getParentUser().getId(), "【暖心亲子计划】有一种嘚瑟是“坚持打卡21天”，再坚持陪伴打卡1次，就能超过98%的家长喽！>>\uD83D\uDC49");
            }
        }

        if (Objects.equals(incr, 21L)) {
            Long size = internalWarmHeartPlanCacheService.incrStudent21DaySize(studentId, 1L);
            if (Objects.equals(size, 1L)) {
                internalWarmHeartPlanCacheService.addStudent21daySize(studentId);
                sendStudentMsg(studentId, "【奖励特权】感谢父母用心的陪伴，恭喜你已获「快乐成长之星」电子荣誉证书和限量托比装扮！点击领取>>");
                sendStudentParentMsg(studentId, "【奖励特权】@最具亲子力的父母，您有一个惊喜礼包等待领取！");
                sendTobyMsg(studentId);
            } else if (Objects.equals(size, 3L)) {
                sendStudentParentMsg(studentId, "【奖励特权】恭喜您已达成3个亲子目标，戳我打开礼盒>>");
            } else if (Objects.equals(size, 5L)) {
                sendStudentParentMsg(studentId, "【奖励特权】恭喜您已达成5个亲子目标，马上打开礼盒>> ");
            } else if (Objects.equals(size, 7L)) {
                sendStudentParentMsg(studentId, "【好消息】Hi～恭喜您已达成7个亲子目标！速速开启礼盒，领取终极惊喜！");
            }
        }

        if (cacheExistsUtils.noExists(CacheExistsEnum.WARM_HEART_PARENT_FIRST_CARD, studentId)) {
            cacheExistsUtils.set(CacheExistsEnum.WARM_HEART_PARENT_FIRST_CARD, studentId);
            sendStudentIntegral(studentId);
            sendStudentMsg(studentId, "【好消息】叮～！恭喜你已获得20个学豆奖励。感谢父母的爱，为爸妈加油，坚持21天，赢更多学豆奖励！\uD83D\uDC49");
        }
    }

    private void sendTobyMsg(Long studentId) {
        List<Integer> productIdList = Arrays.asList(2183, 2185);
        if (RuntimeMode.lt(Mode.STAGING)) {
            productIdList = Arrays.asList(2153, 2154);
        }
        for (Integer integer : productIdList) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", studentId);
            map.put("productId", integer);
            tobyPublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        }
    }

    private void sendStudentParentMsg(Long studentId, String msg) {
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        for (StudentParent studentParent : studentParents) {
            sendParentMsg(studentParent.getParentUser().getId(), msg);
        }
    }

    @Override
    public Long incrParticipateCount(Long incr) {
        return campaignCacheSystem.CBS.storage.incr(WarmHeartPlanConstant.WARM_HEART_PARTICIPATE_COUNT, incr, 1, calcExpire());
    }

    @Override
    public Long getParticipateCount() {
        CacheObject<Object> participateCount = campaignCacheSystem.CBS.storage.get(WarmHeartPlanConstant.WARM_HEART_PARTICIPATE_COUNT);
        return participateCount == null ? 0L : SafeConverter.toLong(participateCount.getValue());
    }

    @Override
    public Long incrStuParticipateCount(Long incr) {
        return campaignCacheSystem.CBS.storage.incr(WarmHeartPlanConstant.WARM_HEART_STU_PARTICIPATE_COUNT, incr, 1, calcExpire());
    }

    @Override
    public Long getStuParticipateCount() {
        CacheObject<Object> participateCount = campaignCacheSystem.CBS.storage.get(WarmHeartPlanConstant.WARM_HEART_STU_PARTICIPATE_COUNT);
        return participateCount == null ? 0L : SafeConverter.toLong(participateCount.getValue());
    }


    public int calcExpire() {
        long expire = WarmHeartPlanConstant.WARM_HEART_PLAN_END_TIME.getTime() - new Date().getTime();
        return (int) (expire / 1000);
    }

    private void sendKafka(Long mainTid) {
        // 发送 push相关
        Map<String, Object> noticeMessage = new LinkedHashMap<>();
        noticeMessage.put("teacherId", mainTid);
        Message noticeMsg = Message.newMessage().withPlainTextBody(JsonUtils.toJson(noticeMessage));
        assignPublisher.publish(noticeMsg);
    }

    private Long getMainTeacherId(Long teacherId) {
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        return mainTeacherId == null ? teacherId : mainTeacherId;
    }

    /**
     * 根据学生查询布置了计划的老师ID
     * 已处理包班制
     */
    public Set<Long> loadTeachersByStudentId(Long studentId) {
        Set<Long> groupIdSet = raikouSDK.getClazzClient().getGroupStudentTupleServiceClient()
                .findByStudentId(studentId)
                .stream()
                .map(GroupStudentTuple::getGroupId)
                .collect(Collectors.toSet());

        return raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient()
                .findByGroupIds(groupIdSet)
                .stream()
                .map(GroupTeacherTuple::getTeacherId)
                .map(this::getMainTeacherId)
                .filter(i -> {
                    TeacherActivityRef teacherActivityRef = teacherActivityRefDao.loadUserIdTypeId(i, WARM_HEART_T);
                    return teacherActivityRef != null;
                })
                .collect(toSet());
    }

    /**
     * 根据老师查询所有学生
     * 已过滤毕业班
     */
    public List<Long> loadStudentsByTeacherId(Long teacherId) {
        return studentLoaderClient.loadStudentIdsNotTerminal(teacherId);
    }

    /**
     * 学生首次制定计划给老师发放园丁豆
     */
    private void sendTeacherIntegral(Long studentId) {
        Set<Long> teacherIds = loadTeachersByStudentId(studentId);
        for (Long teacherId : teacherIds) {
            IntegralHistory integralHistory = new IntegralHistory(teacherId, TEACHER_INTEGRAL_TYPE, 100);
            integralHistory.setComment("学生制定亲子计划奖励");
            MapMessage mapMessage = userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory);
            if (!mapMessage.isSuccess()) {
                log.info("学生首次制定亲子计划给老师发放奖励失败 sid:{} tid:{} info:{}", studentId, teacherId, mapMessage.getInfo());
            }
        }
    }

    /**
     * 学生首次打卡成功
     */
    private void sendStudentIntegral(Long studentId) {
        IntegralHistory integralHistory = new IntegralHistory(studentId, STUDENT_INTEGRAL_TYPE, 20);
        integralHistory.setComment(WARM_HEART_PLAN_ACTIVITY_NAME + "首次打卡奖励");
        MapMessage mapMessage = userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory);
        if (!mapMessage.isSuccess()) {
            log.info("亲子计划学生首次打卡成功 sid:{} info:{}", studentId, mapMessage.getInfo());
        }
    }

    public void sendCoupon(Long teacherId) {
        //log.info("亲子计划 给老师发优惠券流程 teacherId：{}", teacherId);

        String coupon5 = "5c99a4068edbc831ada93a23";
        String coupon10 = "5c99a22eac745959cd18cad7";
        String coupon15 = "5c99a356ac745959cd18cae1";

        if (RuntimeMode.isProduction()) {
            coupon5 = "5c9df37ba29361bd71c104ca";
            coupon10 = "5c9df3d25272e50a17dec12c";
            coupon15 = "5c9df428a29361bd71c10653";
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);

        String mobile = sensitiveUserDataServiceClient.loadUserMobile(teacherId);
        if (StringUtils.isEmpty(mobile)) {
            log.info("亲子计划 老师没有绑定手机号, 暂不发优惠券 teacherId:{}", teacherId);
            return;
        }

        Long parentId = null;
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
        if (userAuthentication != null) {
            parentId = userAuthentication.getId();
        } else {
            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_PARENT);
            neonatalUser.setUserType(UserType.PARENT);
            neonatalUser.setRealname(teacherDetail.fetchRealname());
            neonatalUser.setMobile(mobile);
            neonatalUser.setPassword(RandomGenerator.generatePlainPassword());
            MapMessage mapMessage = userServiceClient.registerUserAndSendMessage(neonatalUser);
            if (!mapMessage.isSuccess()) {
                log.info("亲子计划 给老师注册家长号异常 teacherId:{}", teacherId);
            } else {
                User user = (User) mapMessage.get("user");
                parentId = user.getId();
            }
        }

        if (parentId != null) {
            for (int i = 0; i < 3; i++) {
                couponServiceClient.sendCoupon(coupon5, parentId);
                couponServiceClient.sendCoupon(coupon15, parentId);
            }
            for (int i = 0; i < 4; i++) {
                couponServiceClient.sendCoupon(coupon10, parentId);
            }
        }
    }

    private void sendStudentMsg(Long studentId, String msg) {
        // 系统消息
        AppMessage message = new AppMessage();
        message.setUserId(studentId);
        message.setTitle(WARM_HEART_PLAN_ACTIVITY_NAME);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(STUDENT_INDEX_PAGE);
        message.setImageUrl("");
        message.setMessageType(StudentAppPushType.ACTIVITY_REMIND.getType());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        // push
        Map<String, Object> extInfo = MapUtils.map(
                "s", StudentAppPushType.ACTIVITY_REMIND.getType(),
                "link", STUDENT_INDEX_PAGE,
                "t", "h5",
                "key", "j");
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.STUDENT, Collections.singletonList(studentId), extInfo);
    }

    public void sendTeacherMsg(Long teacherId, String msg) {
        // push
        AppMessage message = new AppMessage();
        message.setUserId(teacherId);
        message.setTitle(WARM_HEART_PLAN_ACTIVITY_NAME);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(TEACHER_INDEX_PAGE);
        message.setImageUrl("");
        message.setMessageType(TeacherMessageType.ACTIVIY.getType());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        Map<String, Object> pushExtInfo = new HashMap<>();
        pushExtInfo.put("link", TEACHER_INDEX_PAGE);
        pushExtInfo.put("s", TeacherMessageType.ACTIVIY.getType());
        pushExtInfo.put("t", "h5");
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PRIMARY_TEACHER,
                Collections.singletonList(teacherId), pushExtInfo);
    }

    private void sendParentMsg(Long parentId, String msg) {
        // 系统消息
        AppMessage message = new AppMessage();
        message.setUserId(parentId);
        message.setTitle(WARM_HEART_PLAN_ACTIVITY_NAME);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(PARENT_INDEX_PAGE);
        message.setImageUrl("");
        message.setMessageType(ParentMessageType.REMINDER.getType());
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("tag", ParentMessageTag.通知.name());
        message.setExtInfo(extInfo);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        //发送jpush
        Map<String, Object> extras = new HashMap<>();
        extras.put("url", PARENT_INDEX_PAGE);
        extras.put("tag", ParentMessageTag.通知.name());
        extras.put("s", ParentAppPushType.ACTIVITY.name());
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PARENT, Collections.singletonList(parentId), extras);
    }


    public void sendTeacherResourceMsg(Long teacherId, String title, String msg, String url) {
        // push
        AppMessage message = new AppMessage();
        message.setUserId(teacherId);
        message.setTitle(title);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(url);
        message.setImageUrl("");
        message.setMessageType(TeacherMessageType.ACTIVIY.getType());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        Map<String, Object> pushExtInfo = new HashMap<>();
        pushExtInfo.put("link", url);
        pushExtInfo.put("s", TeacherMessageType.ACTIVIY.getType());
        pushExtInfo.put("t", "h5");
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PRIMARY_TEACHER,
                Collections.singletonList(teacherId), pushExtInfo);
    }

    /**
     * 转换目标名称(上线后改过名称的处理)
     *
     * @param planName
     * @return
     */
    private String transferPlanName(String planName) {
        if ("读英文绘本".equals(planName)) return "英文绘本";
        if ("晨读磨耳朵".equals(planName)) return "点读机";
        if ("一起趣配音".equals(planName)) return "趣味配音";

        return planName;
    }
}
