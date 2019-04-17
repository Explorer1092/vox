package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.data.ActivityBaseRule;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.crm.api.ActivityConfigLoader;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportClassSnapshotData;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportStudentData;
import com.voxlearning.utopia.service.rstaff.consumer.ActivityReportServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.washington.cache.WashingtonCacheSystem;
import com.voxlearning.washington.mapper.activity.TeacherActivityIndexMapper;
import com.voxlearning.washington.mapper.activity.TeacherActivityMapper;
import org.jetbrains.annotations.NotNull;
import org.jsoup.helper.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/activity/teacher/v2/")
public class TeacherActivityV2Controller extends AbstractTeacherController {

    private static final String PATTERN = "yyyy-MM-dd";
    private static final String END_DATE_FIX = " 23:59:59";

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject
    private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject
    private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    private ActivityReportServiceClient activityReportServiceClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private WashingtonCacheSystem washingtonCacheSystem;

    @ImportService(interfaceClass = ActivityConfigLoader.class)
    private ActivityConfigLoader activityConfigLoader;

    private final String TEACHER_CLICK_ASSGIN_BUTTON_CACHE = "TEACHER_CLICK_ASSGIN_BUTTON";
    private final String TEACHER_CAN_SEND_REWARD_CACHE = "TEACHER_CAN_SEND_REWARD";

    /**
     * 活动首页
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "index.vpage")
    public MapMessage teacherActivityIndex() {
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            Validate.isTrue(teacherDetail != null, "未登录");
            Long teacherSchoolId = teacherDetail.getTeacherSchoolId();
            Integer areaId = teacherDetail.getRegionCode();
            List<Clazz> clazzList = getTeacherClazz(teacherDetail);
            // 封装数据
            List<ActivityConfig> activityConfigs = activityConfigServiceClient.getActivityConfigService().loadByApplicant(teacherDetail.getId(), ActivityConfig.ROLE_TEACHER);
            List<TeacherActivityIndexMapper> myActivity = mapperList(activityConfigs, clazzList);
            List<TeacherActivityIndexMapper> otherActivity = mapperList(activityConfigServiceClient.loadBySchoolIdAreaIdClazzIds(teacherSchoolId, areaId, clazzList), clazzList);
            return MapMessage.successMessage()
                    .add("myActivity", myActivity)
                    .add("otherActivity", otherActivity)
                    .add("hasActivity", ObjectUtils.get(() -> teacherDetail.getSubjects().stream().anyMatch(subject -> subject == Subject.MATH), false));
        } catch (IllegalArgumentException | NullPointerException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.successMessage();
    }

    /**
     * 老师的班级
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "teacher_clazz.vpage")
    public MapMessage teacherClazz() {
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            Validate.isTrue(teacherDetail != null, "未登录");
            String startTime = getRequestString("startTime");
            String endTime = getRequestString("endTime");
            if (StringUtils.isAnyBlank(startTime, endTime)) {
                return MapMessage.errorMessage("请选择时间");
            }
            Date startDate = DateUtils.stringToDate(startTime, PATTERN);
            Date endDate = DateUtils.stringToDate(endTime + END_DATE_FIX);
            if (startDate == null || endDate == null) {
                return MapMessage.errorMessage("时间格式错误:yyyy-MM-dd");
            }
            if (DateUtils.dayDiff(endDate, startDate) > 4) {
                return MapMessage.errorMessage("活动时间不能大于5天");
            }
            // 包班制
            Set<Long> relatedIds = teacherLoaderClient.loadRelTeacherIds(teacherDetail.getId());
            // 过滤掉非数学老师
            List<Teacher> teachers = teacherLoaderClient.loadTeachers(relatedIds).values().stream().filter(t -> Objects.equals(t.getSubject(), Subject.MATH)).collect(Collectors.toList());
            List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teachers.stream().map(Teacher::getId).collect(Collectors.toSet()))
                    .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            // 过滤毕业班
            Set<Long> idSet = new HashSet<>();
            clazzs = clazzs
                    .stream()
                    .filter(Clazz::isPublicClazz)
                    .filter(i -> !i.isTerminalClazz())
                    .filter(i -> !i.isDisabledTrue())
                    .filter(i -> {
                        boolean flag = !idSet.contains(i.getId());
                        idSet.add(i.getId());
                        return flag;
                    })
                    .collect(Collectors.toList());

            String clazzIds = getRequestString("clazzIds");
            if (StringUtils.isNotBlank(clazzIds)) {
                List<Long> requireClazzIds = Arrays.stream(clazzIds.split(",")).map(SafeConverter::toLong).collect(Collectors.toList());
                clazzs = clazzs.stream().filter(clazz -> requireClazzIds.contains(clazz.getId())).collect(Collectors.toList());
                boolean isAssign = activityConfigLoader.loadClassesActivity(clazzs.stream().map(Clazz::getId).collect(Collectors.toList())).values().stream().flatMap(Collection::stream).anyMatch(a -> !(endDate.before(a.getStartTime()) || startDate.after(a.getEndTime())));
                return MapMessage.successMessage().add("isAssign", isAssign);
            } else {
                // 查询当前班级是布置的活动
                Map<Long, List<ActivityConfig>> activityMap = new HashMap<>();
                activityConfigLoader.loadClassesActivity(clazzs.stream().map(Clazz::getId).collect(Collectors.toList())).forEach((k, v) -> {
                    List<ActivityConfig> activityConfigs = v.stream().filter(a -> !(endDate.before(a.getStartTime()) || startDate.after(a.getEndTime()))).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(activityConfigs)) {
                        activityMap.put(k, activityConfigs);
                    }
                });
                // 封装数据
                List<Map<String, Object>> classList = new ArrayList<>();
                clazzs.stream().sorted(Comparator.comparing(Clazz::getClassLevel))
                        .map(clazz ->
                                MapUtils.m("id", clazz.getId(), "name", clazz.getClassName(), "isAssign", activityMap.containsKey(clazz.getId()), "clazzLevel", clazz.getClazzLevel().getDescription())
                        ).collect(Collectors.groupingBy(m -> (String) m.get("clazzLevel"), LinkedHashMap::new, Collectors.toList())).forEach((k, v) -> classList.add(MapUtils.m("clazzLevel", k, "clazzList", v)));
                return MapMessage.successMessage().add("clazzList", classList);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.successMessage();
    }

    @ResponseBody
    @RequestMapping(value = "detail.vpage")
    public MapMessage detail() {
        try {
            User user = currentUser();

            String activityId = getRequestString("id");
            Validate.notEmpty(activityId, "id 不可为空");

            ActivityConfig config = activityConfigServiceClient.getActivityConfigService().load(activityId);
            if (config == null) {
                return MapMessage.errorMessage("活动不存在");
            }
            TeacherActivityIndexMapper mapper = new TeacherActivityIndexMapper();
            BeanUtils.copyProperties(mapper, config);

            Collator instance = Collator.getInstance(Locale.CHINESE);

            List<Map<String, String>> schoolList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(config.getSchoolIds())) {
                Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader().loadSchools(config.getSchoolIds()).getUninterruptibly();
                List<School> values = new ArrayList<>(schoolMap.values());
                values.sort((o1, o2) -> instance.compare(o1.getCname(), o2.getCname()));
                for (School school : values) {
                    schoolList.add(MapUtils.map("schoolId", school.getId(), "schoolName", school.getCname()));
                }
            }

            List<Map<String, String>> areaList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(config.getAreaIds())) {
                List<Integer> areaIds = config.getAreaIds().stream().map(SafeConverter::toInt).collect(Collectors.toList());
                Map<Integer, ExRegion> areaMap = raikouSystem.getRegionBuffer().loadRegions(areaIds);
                List<ExRegion> values = new ArrayList<>(areaMap.values());
                values.sort((o1, o2) -> instance.compare(o1.getCountyName(), o2.getCountyName()));
                for (ExRegion area : values) {
                    areaList.add(MapUtils.map("areaId", area.getId(), "areaName", area.getCountyName()));
                }
            }

            List<Map<String, Object>> teacherClazzList = null;
            Boolean signUpStatus = true;
            if (user != null && user.isTeacher()) {
                Set<Long> relatedIds = teacherLoaderClient.loadRelTeacherIds(user.getId());
                List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(relatedIds)
                        .values().stream().flatMap(Collection::stream).distinct().collect(Collectors.toList());

                clazzList = clazzList
                        .stream()
                        .filter(Clazz::isPublicClazz)
                        .filter(i -> !i.isTerminalClazz())
                        .filter(i -> !i.isDisabledTrue())
                        .filter(i -> config.getClazzLevels().contains(i.getClazzLevel().getLevel()))
                        .filter(i -> CollectionUtils.isEmpty(config.getClazzIds()) || config.getClazzIds().contains(i.getId()))
                        .sorted(Comparator.comparing(Clazz::getClassLevel).thenComparing(Clazz::getClassName))
                        .collect(Collectors.toList());
                teacherClazzList = clazzList.stream()
                        .map(clazz -> MapUtils.m("clazzId", clazz.getId(), "clazzName", clazz.formalizeClazzName()))
                        .collect(Collectors.toList());

                Set<Long> clazzIdList = clazzList.stream().map(Clazz::getId).collect(Collectors.toSet());
                Long participateCount = activityReportServiceClient.loadParticipateCountByClazzIds(mapper.getId(), clazzIdList);
                mapper.setParticipantsCount(participateCount);

                signUpStatus = activityConfigServiceClient.loadSignUpStatus(user.getId(), activityId);
            }

            return MapMessage.successMessage().add("activity", mapper)
                    .add("school", schoolList)
                    .add("area", areaList)
                    .add("clazz", teacherClazzList)
                    .add("signUpStatus", signUpStatus)
                    .add("isOneself", (user != null && user.isTeacher()) && Objects.equals(mapper.getApplicant(), user.getId()));
        } catch (IllegalArgumentException | NullPointerException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.successMessage();
    }

    @ResponseBody
    @RequestMapping(value = "report.vpage")
    public MapMessage report() {
        String activityId = getRequestString("activityId");
        Long clazzId = getRequestLong("clazzId");
        List<Long> clazzIdList = Collections.singletonList(clazzId);

        Long studentCount = studentLoaderClient.loadClazzStudentIds(clazzIdList).values().stream().flatMap(Collection::parallelStream).count();
        Long participateCount = activityReportServiceClient.loadParticipateCountByClazzIds(activityId, clazzIdList);

        Double participateRate = 0D;
        if (studentCount != 0) {
            BigDecimal multiply = new BigDecimal(participateCount).divide(new BigDecimal(studentCount), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            participateRate = multiply.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        List<ActivityReportClassSnapshotData> snapshotData = activityReportServiceClient.loadClassSnapshot(activityId, clazzId);

        List<String> date = new ArrayList<>();
        List<Double> avgScore = new ArrayList<>();
        List<Double> avgTime = new ArrayList<>();

        for (ActivityReportClassSnapshotData item : snapshotData) {
            date.add(formatCurDate(item.getCurDate()));
            avgScore.add(item.getAvgScore());
            avgTime.add(item.getAvgTime());
        }

        return MapMessage.successMessage()
                .add("studentCount", studentCount)
                .add("participateCount", participateCount)
                .add("participateRate", participateRate + "%")
                .add("date", date)
                .add("avgScore", avgScore)
                .add("avgTime", avgTime);
    }

    @ResponseBody
    @RequestMapping(value = "student.vpage")
    public MapMessage student() {
        try {
            String activityId = getRequestString("activityId");
            Long clazzId = getRequestLong("clazzId");

            List<ActivityReportStudentData> studentData = activityReportServiceClient.loadStudentData(activityId, clazzId);
            studentData.sort((o1, o2) -> o2.getMaxScore().compareTo(o1.getMaxScore()));

            return MapMessage.successMessage().add("student", studentData);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage();
    }

    @ResponseBody
    @RequestMapping(value = "sign_up.vpage")
    public MapMessage signUp() {
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage("未登录");
            }
            String activityId = getRequestString("activityId");
            if (StringUtils.isEmpty(activityId)) {
                return MapMessage.errorMessage("activityId 为空");
            }

            return activityConfigServiceClient.signUpActivity(teacherDetail.getId(), activityId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage();
    }

    /**
     * 布置活动
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "assign.vpage")
    public MapMessage assign() {
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage("未登录");
            }
            if (!ObjectUtils.get(() -> teacherDetail.getSubjects().stream().anyMatch(subject -> subject == Subject.MATH), false)) {
                return MapMessage.errorMessage("只支持数学老师布置活动");
            }
            String clazzStrIds = getRequestString("clazzIds");// 班级ids "," 号分割
            if (StringUtils.isBlank(clazzStrIds)) {
                return MapMessage.errorMessage("请选择班级");
            }
            List<Long> clazzIds = Arrays.stream(clazzStrIds.split(",")).map(SafeConverter::toLong).collect(Collectors.toList());
            Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(clazzIds)
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));
            if (clazzMap.isEmpty()) {
                return MapMessage.errorMessage("班级选择错误");
            }

            Set<Integer> clazzLevels = clazzMap.values().stream().map(l -> SafeConverter.toInt(l.getClassLevel())).collect(Collectors.toSet());
            ActivityConfig config = new ActivityConfig();
            config.setClazzIds(clazzIds);
            config.setClazzLevels(new ArrayList<>(clazzLevels));
            config.setSchoolIds(Collections.singletonList(clazzMap.values().iterator().next().getSchoolId())); // 班级对应的学校，老师不能跨校带班
            String type = getRequestString("type");// 游戏type
            String title = getRequestString("title");// 标题
            if (StringUtils.isAnyBlank(type, title)) {
                return MapMessage.errorMessage("游戏类型、标题 不能为空");
            }
            config.setType(ActivityTypeEnum.valueOf(type));
            Date startTime = DateUtils.stringToDate(getRequestString("startTime"), PATTERN); // 活动开始时间
            Date endTime = DateUtils.stringToDate(getRequestString("endTime") + END_DATE_FIX); // 活动结束时间
            if (startTime == null || endTime == null) {
                return MapMessage.errorMessage("时间格式错误:yyyy-MM-dd");
            }
            if (startTime.after(endTime)) {
                return MapMessage.errorMessage("开始时间不能大于结束时间");
            }
            if (DateUtils.dayDiff(endTime, startTime) > 4) {
                return MapMessage.errorMessage("时间间隔大于5天");
            }
            // 校验当前时间段是否有正在进行中的游戏
            for (Map.Entry<Long, List<ActivityConfig>> entry : activityConfigLoader.loadClassesActivity(clazzIds).entrySet()) {
                if (entry.getValue().stream().anyMatch(a -> !(endTime.before(a.getStartTime()) || startTime.after(a.getEndTime())))) {
                    return MapMessage.errorMessage("选择的班级已有其他老师布置的活动");
                }
            }
            config.setStartTime(startTime);
            config.setEndTime(endTime);
            config.setRules(validateRule(getRequestString("rules"))); // 游戏规则 ActivityBaseRule
            config.setApplicant(teacherDetail.getId());
            config.setApplicantRole(1); // 老师布置
            config.setStatus(2); // 老师布置的直接审核通过
            config.setSubjects(Collections.singleton(Subject.MATH)); // 设置数学学科
            config.setTitle(title);
            return activityConfigServiceClient.getActivityConfigService().publishActivity(config);
        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage();
    }

    /**
     * 布置活动
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "delete.vpage")
    public MapMessage deleteActivity() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage("未登录");
        }
        String id = getRequestString("id");
        ActivityConfig activityConfig;
        if (StringUtils.isBlank(id) || (activityConfig = activityConfigServiceClient.getActivityConfigService().load(id)) == null) {
            return MapMessage.errorMessage("请选择正确的活动");
        }
        if (activityConfig.isStarting(new Date())) {
            return MapMessage.errorMessage("活动正在进行中，不能删除");
        }
        activityConfigServiceClient.getActivityConfigService().delete(id);
        return MapMessage.successMessage();
    }

    private String dateTimeErrMsg = "活动时间异常";

    @ResponseBody
    @RequestMapping(value = "click_assgin_button.vpage")
    public MapMessage clickAssginButton() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage("未登录");
        }

        String endTime = getRequestString("endTime");
        if (StringUtils.isEmpty(endTime)) {
            return MapMessage.errorMessage(dateTimeErrMsg);
        }

        Date endDate;
        try {
            endDate = DateUtils.parseDate(endTime, "yyyyMMdd");
            endDate = DateUtils.getDayEnd(endDate);
        } catch (Exception e) {
            return MapMessage.errorMessage(dateTimeErrMsg);
        }
        long l = DateUtils.dayDiff(new Date(), endDate);
        if (l > 60) {
            return MapMessage.errorMessage("活动时间过久");
        }

        int expiration = SafeConverter.toInt((endDate.getTime() - System.currentTimeMillis()) / 1000);
        String cacheKey = CacheKeyGenerator.generateCacheKey("TEACHER_CLICK_ASSGIN_BUTTON", new String[]{"TID"}, new Object[]{teacherDetail.getId()});
        washingtonCacheSystem.CBS.persistence.set(cacheKey, expiration, "1");
        return MapMessage.successMessage();
    }


    @ResponseBody
    @RequestMapping(value = "activity_index.vpage")
    public MapMessage activity() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage("未登录");
        }

        String startTime = getRequestString("startTime");
        String endTime = getRequestString("endTime");
        if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            return MapMessage.errorMessage(dateTimeErrMsg);
        }

        Date startDate, endDate;
        try {
            startDate = DateUtils.parseDate(startTime, "yyyyMMdd");
            endDate = DateUtils.parseDate(endTime, "yyyyMMdd");
            endDate = DateUtils.getDayEnd(endDate);
        } catch (Exception e) {
            return MapMessage.errorMessage(dateTimeErrMsg);
        }
        if (endDate.before(startDate)) {
            return MapMessage.errorMessage(dateTimeErrMsg);
        }

        // 如果没点过布置按钮, 什么都不返回
        String clickAssginCache = CacheKeyGenerator.generateCacheKey(TEACHER_CLICK_ASSGIN_BUTTON_CACHE, new String[]{"TID"}, new Object[]{teacherDetail.getId()});
        String canSendRewardCacheKey = CacheKeyGenerator.generateCacheKey(TEACHER_CAN_SEND_REWARD_CACHE, new String[]{"TID"}, new Object[]{teacherDetail.getId()});

        boolean clicked = washingtonCacheSystem.CBS.persistence.get(clickAssginCache).containsValue();
        boolean sendReward = washingtonCacheSystem.CBS.persistence.get(canSendRewardCacheKey).containsValue();

        if (!clicked) {
            TeacherActivityMapper mapper = new TeacherActivityMapper();
            mapper.setStartTime(startDate);
            mapper.setEndTime(endDate);
            return MapMessage.successMessage().add("data", mapper).add("clicked", clicked).add("reward", sendReward);
        }

        AtomicBoolean canSendReward = new AtomicBoolean(false);

        // 老师指定时间段创建的活动
        List<ActivityConfig> activityConfigs = activityConfigLoader.loadTeacherActivity(teacherDetail.getId(), startDate, endDate);

        // 老师班级
        List<Clazz> clazzList = getTeacherClazz(teacherDetail);

        List<TeacherActivityMapper.ActivityDetail> activityDetailList = activityConfigs.stream().map(config -> {

            // 符合当前活动范围的班级
            List<Clazz> clazzes = clazzList.stream()
                    .filter(i -> config.getClazzLevels().contains(i.getClazzLevel().getLevel()))
                    .filter(i -> CollectionUtils.isEmpty(config.getClazzIds()) || config.getClazzIds().contains(i.getId()))
                    .collect(Collectors.toList());

            // 当前活动每个班级的参与人数
            Map<Long, Long> clazzIdParticipantCountMap = activityReportServiceClient.loadParticipateCountMapByClazzIds(config.getId());

            // 拼装每个班级参与人数的 View 展示
            List<TeacherActivityMapper.ClazzDetail> clazzDetails = clazzes.stream().map(clazz -> {
                TeacherActivityMapper.ClazzDetail clazzDetail = new TeacherActivityMapper.ClazzDetail();
                clazzDetail.setClazzId(clazz.getId());
                clazzDetail.setClazzName(clazz.formalizeClazzName());
                Long participantsCount = clazzIdParticipantCountMap.getOrDefault(clazz.getId(), 0L);
                clazzDetail.setParticipantsCount(participantsCount);
                if (participantsCount >= 20) {
                    canSendReward.set(true);
                }
                return clazzDetail;
            }).collect(Collectors.toList());

            // 拼装当前活动的 View 展示
            TeacherActivityMapper.ActivityDetail activityDetail = new TeacherActivityMapper.ActivityDetail();
            activityDetail.setActivityId(config.getId());
            activityDetail.setTitle(config.getTitle());
            activityDetail.setType(config.getType().getName());
            activityDetail.setClazzDetails(clazzDetails);
            return activityDetail;
        }).collect(Collectors.toList());

        TeacherActivityMapper mapper = new TeacherActivityMapper();
        mapper.setStartTime(startDate);
        mapper.setEndTime(endDate);
        mapper.setDetails(activityDetailList);

        if (canSendReward.get()) {
            int expiration = SafeConverter.toInt((endDate.getTime() - System.currentTimeMillis()) / 1000);
            washingtonCacheSystem.CBS.persistence.set(canSendRewardCacheKey, expiration, "1");
        }

        return MapMessage.successMessage().add("data", mapper).add("clicked", CollectionUtils.isNotEmpty(activityDetailList)).add("reward", canSendReward);
    }

    /**
     * 校验活动规则
     *
     * @return
     */
    private ActivityBaseRule validateRule(String rules) {
        ActivityBaseRule activityBaseRule = JsonUtils.fromJson(rules, ActivityBaseRule.class);
        if (activityBaseRule == null) {
            throw new IllegalArgumentException("请选择正确的活动规则");
        }
        activityBaseRule.setPlayLimit(7);
        return activityBaseRule;
    }

    private List<TeacherActivityIndexMapper> mapperList(List<ActivityConfig> activityConfigs, List<Clazz> clazzList) {
        Date now = new Date();
        Set<Long> clzzIdSet = clazzList.stream().map(Clazz::getId).collect(Collectors.toSet());
        return activityConfigs.stream().filter(i -> {
            // 过滤非老师教的班级
            if (i.hasTeacher()) {
                return i.getClazzIds().stream().anyMatch(clzzIdSet::contains);
            }
            return true;
        }).sorted((o1, o2) -> {
            Integer o1Status = o1.isEnd(now) ? 3 : (o1.isUnStart(now) ? 2 : 1);
            Integer o2Status = o2.isEnd(now) ? 3 : (o2.isUnStart(now) ? 2 : 1);
            int i = o1Status.compareTo(o2Status);
            return i == 0 ? o2.getStartTime().compareTo(o1.getStartTime()) : i;
        }).map(i -> {
            TeacherActivityIndexMapper mapper = new TeacherActivityIndexMapper();
            try {
                BeanUtils.copyProperties(mapper, i);
            } catch (Exception e) {
            }
            // 如果活动没有班级
            Set<Long> activityClazzId;
            if (i.hasTeacher() && CollectionUtils.isNotEmpty(i.getClazzIds())) {
                activityClazzId = new HashSet<>(i.getClazzIds());
            } else {
                activityClazzId = clazzList.stream()
                        .filter(item -> i.getClazzLevels().contains(item.getClazzLevel().getLevel()))
                        .map(LongIdEntity::getId).collect(Collectors.toSet());
            }
            Long clazzStudentCount = studentLoaderClient.loadClazzStudentIds(activityClazzId).values().stream().flatMap(Collection::parallelStream).count();
            Long participateCount = activityReportServiceClient.loadParticipateCountByClazzIds(mapper.getId(), activityClazzId);
            mapper.setSumCount(clazzStudentCount);
            mapper.setParticipantsCount(participateCount);
            return mapper;
        }).collect(Collectors.toList());
    }

    /**
     * 获取老师行政班, 已处理包班制, 过滤毕业班
     */
    @NotNull
    private List<Clazz> getTeacherClazz(TeacherDetail teacherDetail) {
        // 包班制
        Set<Long> relatedIds = teacherLoaderClient.loadRelTeacherIds(teacherDetail.getId());
        List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(relatedIds)
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        // 过滤毕业班
        clazzList = clazzList
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(i -> !i.isTerminalClazz())
                .filter(i -> !i.isDisabledTrue())
                .distinct()
                .sorted(Comparator.comparing(Clazz::getClassLevel).thenComparing(Clazz::getClassName))
                .collect(Collectors.toList());
        return clazzList;
    }


    private static String formatCurDate(String curDate) {
        if (StringUtils.isEmpty(curDate) || curDate.length() != 8) {
            return curDate;
        }
        String monthDay = curDate.substring(4);

        String month = monthDay.substring(0, 2);
        if (month.startsWith("0")) month = month.substring(1);

        String day = monthDay.substring(2);
        if (day.startsWith("0")) day = day.substring(1);

        return month + "." + day;
    }

}
