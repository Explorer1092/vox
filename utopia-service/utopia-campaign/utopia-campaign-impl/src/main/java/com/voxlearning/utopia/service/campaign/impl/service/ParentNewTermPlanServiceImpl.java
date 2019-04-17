package com.voxlearning.utopia.service.campaign.impl.service;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.service.campaign.api.ParentNewTermPlanService;
import com.voxlearning.utopia.service.campaign.api.constant.PlanConstant;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityRef;
import com.voxlearning.utopia.service.campaign.api.mapper.NewTermStudentPlanMapper;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherActivityRefDao;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.mapper.IntegralInfo;
import com.voxlearning.utopia.service.integral.client.IntegralServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.service.campaign.api.constant.PlanConstant.TEACHER_ACTIVITY_INDEX;
import static com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum.NEW_TERM_PLAN_2019_SING_UP;

@Named
@Slf4j
@ExposeService(interfaceClass = ParentNewTermPlanService.class)
public class ParentNewTermPlanServiceImpl implements ParentNewTermPlanService {

    @Inject
    private NewTermPlanDPBridge newTermPlanDPBridge;
    @Inject
    private CampaignCacheSystem campaignCacheSystem;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    protected AppMessageServiceClient appMessageServiceClient;
    @Inject
    private IntegralServiceClient integralServiceClient;
    @Inject
    private TeacherWinterPlanServiceImpl teacherParentChildService;
    @Inject
    private TeacherNewTermPlanServiceImpl teacherNewTermPlanService;
    @Inject
    private TeacherActivityRefDao teacherActivityRefDao;

    @Override
    public List<Map<String, Object>> saveNewTermActivityPlans(Long studentId, String plans) {
        NewTermStudentPlanMapper studentTargets = getStudentTargets(studentId);
        if (studentTargets != null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> maps = newTermPlanDPBridge.saveNewTermActivityPlans(studentId, plans);

        if (RuntimeMode.le(Mode.STAGING)) {
            log.info("saveNewTermActivityPlans response userId: {} {}", studentId, JSON.toJSONString(maps));
        }

        setStudentTargets(studentId, maps);

        sendStudentIntegral(studentId);

        teacherNewTermPlanService.incrParticipateCount(1L);

        //老师点击“发起活动” ，名下有学生制定了计划（一天最多推送一条）
        List<Long> teacherIdList = teacherParentChildService.loadStudentTeacherId(studentId);

        if (CollectionUtils.isEmpty(teacherIdList)) {
            return maps;
        }

        for (Long tid : teacherIdList) {
            MapMessage mapMessage = teacherNewTermPlanService.loadTeacherStatus(tid);
            if (mapMessage != null && mapMessage.get("assigned").equals(true)) {
                sendTeacherIntegral(tid, studentId);
                Integer teacherStatus = getTeacherStatus(tid);
                if (!Objects.equals(teacherStatus, 1)) {
                    sendTeacherMsg(tid, "Hi，老师，又有一名同学上传了自己的新学期计划，孩子的成长离不开您的肯定，赶快去看看给ta点赞鼓励吧！");
                    setTeacherStatus(tid);
                }
            }
        }

        return maps;
    }

    @Override
    public MapMessage signUp(Long id) {
        TeacherActivityRef teacherActivityRef = teacherActivityRefDao.loadUserIdTypeId(id, NEW_TERM_PLAN_2019_SING_UP);
        if (teacherActivityRef == null) {
            teacherActivityRef = new TeacherActivityRef();
            teacherActivityRef.setUserId(id);
            teacherActivityRef.setType(NEW_TERM_PLAN_2019_SING_UP.name());
            teacherActivityRefDao.upsert(teacherActivityRef);
            incrSignUpCount(1L);
        }
        return MapMessage.successMessage();
    }

    private static final String NEW_TERN_PLAN_2019_2_SIGN_UP = "NEW_TERN_PLAN_2019_2_SIGN_UP";

    @Override
    public Long getSignUpCount() {
        CacheObject<Object> cacheObject = campaignCacheSystem.CBS.storage.get(NEW_TERN_PLAN_2019_2_SIGN_UP);
        if (cacheObject.containsValue()) {
            return SafeConverter.toLong(cacheObject.getValue().toString().trim());
        }
        return 0L;
    }

    @Override
    public Long incrSignUpCount(Long incr) {
        return campaignCacheSystem.CBS.storage.incr(NEW_TERN_PLAN_2019_2_SIGN_UP, incr, 1L, 0);
    }

    @Override
    public Long setSignUpCount(Long count) {
        campaignCacheSystem.CBS.storage.delete(NEW_TERN_PLAN_2019_2_SIGN_UP);
        return campaignCacheSystem.CBS.storage.incr(NEW_TERN_PLAN_2019_2_SIGN_UP, count, count, 0);
    }

    @Override
    public Boolean getSignUpStatus(Long parentId) {
        return teacherActivityRefDao.loadUserIdTypeId(parentId, NEW_TERM_PLAN_2019_SING_UP) != null;
    }

    private void sendTeacherIntegral(Long teacherId, Long studentId) {
        int integral = 10 * 10;// (10 个园丁豆)
        IntegralInfo integralInfo = new IntegralInfo();
        integralInfo.setUserId(teacherId);
        integralInfo.setIntegralType(IntegralType.TEACHER_ASSIGN_NEWTERMVACATION_PLAN_ACTIVITY.getType());
        integralInfo.setUniqueKey("NEW_TERM_PLAN" + "_" + teacherId + "_" + studentId);
        integralInfo.setIntegral(integral);
        integralInfo.setComment("推荐学生制定新学期计划奖励");
        boolean duplicate = integralServiceClient.checkDuplicate(integralInfo);
        if (!duplicate) {
            MapMessage reward = integralServiceClient.reward(integralInfo);
            if (!reward.isSuccess()) {
                log.warn("给老师发放布置新学期计划奖励时失败, userId:{},info:{}", studentId, reward.getInfo());
            }
        }
    }

    private void sendStudentIntegral(Long studentId) {
        int integral = 20;
        IntegralInfo integralInfo = new IntegralInfo();
        integralInfo.setUserId(studentId);
        integralInfo.setIntegralType(IntegralType.STUDENT_NEW_TERM_PLAN_OTHER.getType());
        integralInfo.setUniqueKey("NEW_TERM_PLAN_STUDENT_SAVE" + "_" + studentId);
        integralInfo.setIntegral(integral);
        integralInfo.setComment("学生制定新学期计划奖励");
        boolean duplicate = integralServiceClient.checkDuplicate(integralInfo);
        if (!duplicate) {
            MapMessage reward = integralServiceClient.reward(integralInfo);
            if (!reward.isSuccess()) {
                log.warn("学生布置新学期计划发放奖励时失败, userId:{},info:{}", studentId, reward.getInfo());
            }
        }
    }

    private void sendTeacherMsg(Long teacherId, String msg) {
        // push
        AppMessage message = new AppMessage();
        message.setUserId(teacherId);
        message.setTitle(PlanConstant.NEW_TERM_PLAN_ACTIVITY_NAME);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(TEACHER_ACTIVITY_INDEX);
        message.setImageUrl("");
        message.setMessageType(TeacherMessageType.ACTIVIY.getType());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        Map<String, Object> pushExtInfo = new HashMap<>();
        pushExtInfo.put("link", TEACHER_ACTIVITY_INDEX);
        pushExtInfo.put("s", TeacherMessageType.ACTIVIY.getType());
        pushExtInfo.put("t", "h5");
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PRIMARY_TEACHER, Collections.singletonList(teacherId), pushExtInfo);
    }

    public void setTeacherStatus(Long teacherId) {
        String cacheKey = genTeacherCacheKey(teacherId);
        campaignCacheSystem.CBS.storage.set(cacheKey, 86400, 1);
    }

    public Integer getTeacherStatus(Long teacherId) {
        String cacheKey = genTeacherCacheKey(teacherId);
        return campaignCacheSystem.CBS.storage.load(cacheKey);
    }

    /**
     * 老师点击“发起活动” ，名下有学生制定了计划（一天最多推送一条）
     *
     * @param teacherId
     * @return
     */
    public String genTeacherCacheKey(Long teacherId) {
        return "NewTermPlan:TeacherId" + teacherId;
    }

    public void setStudentTargets(Long student, List<Map<String, Object>> maps) {
        String cacheKey = genStudentTargetCacheKey(student);
        NewTermStudentPlanMapper planMapper = new NewTermStudentPlanMapper();
        List<NewTermStudentPlanMapper.Plan> planList = new ArrayList<>();
        for (Map<String, Object> map : maps) {

            NewTermStudentPlanMapper.Plan plan = new NewTermStudentPlanMapper.Plan();
            plan.setId(SafeConverter.toString(map.get("id")));
            plan.setName(SafeConverter.toString(map.get("planning_name")));
            plan.setDesc(SafeConverter.toString(map.get("planning_desc")));
            planList.add(plan);
        }
        planMapper.setPlans(planList);

        Date beginDay = new Date();
        Date dayStart = DateUtils.getDayStart(beginDay);
        Date endDay = DateUtils.addDays(dayStart, 30);
        Date dayEnd = DateUtils.getDayEnd(endDay);
        planMapper.setBeginDate(dayStart);
        planMapper.setEndDate(dayEnd);

        campaignCacheSystem.CBS.storage.set(cacheKey, calcExpire(), planMapper);
    }

    public NewTermStudentPlanMapper getStudentTargets(Long student) {
        String cacheKey = genStudentTargetCacheKey(student);
        return campaignCacheSystem.CBS.storage.load(cacheKey);
    }

    /**
     * 学生的2019收心计划目标ID
     *
     * @param studentId
     * @return
     */
    public String genStudentTargetCacheKey(Long studentId) {
        return "NewTermPlan:StudentId:Target" + studentId;
    }

    public int calcExpire() {
        long expire = NEW_TERM_PLAN_END_TIME.getTime() - new Date().getTime();
        return (int) (expire / 1000);
    }

    private static Date NEW_TERM_PLAN_END_TIME;

    static {
        try {
            //:todo 结束日期未确定
            // 活动 2019-02-24 23:59:59 结束 多存几天吧
            NEW_TERM_PLAN_END_TIME = DateUtils.parseDate("2019-05-10 00:00:00", "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
