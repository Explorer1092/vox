package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.service.campaign.api.constant.PlanConstant;
import com.voxlearning.utopia.service.campaign.api.entity.NewTermPlanActivity;
import com.voxlearning.utopia.service.campaign.api.mapper.NewTermStudentPlanMapper;
import com.voxlearning.utopia.service.campaign.impl.dao.UserNewTermPlanActivityRefDao;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsEnum;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsUtils;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.service.campaign.api.constant.PlanConstant.*;

@Named
@Slf4j
public class TeacherNewTermPlanListenerService {

    @Inject
    private UserNewTermPlanActivityRefDao userNewTermPlanActivityRefDao;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    protected AppMessageServiceClient appMessageServiceClient;
    @Inject
    private ParentNewTermPlanServiceImpl parentNewTermPlanService;
    @Inject
    private TeacherNewTermPlanServiceImpl teacherNewTermPlanService;
    @Inject
    private CacheExistsUtils cacheExistsUtils;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private SmsServiceClient smsServiceClient;

    public void handle(Long userId) {
        User user = userLoaderClient.loadUser(userId);

        NewTermPlanActivity load = userNewTermPlanActivityRefDao.load(user.getId());
        if (load == null) {
            return;
        }

        if (user.isTeacher()) {
            if (load.getTeacherShowActivity() == null) return;

            teacherHandler(user, load);
        } else if (user.isStudent()) {
            if (load.getStudentShowActivity() == null) return;

            studentHandler(user, load);
        } else if (user.isParent()) {
            if (load.getParentShowActivity() == null) return;

            parentHandler(user, load);
        }
    }

    private void teacherHandler(User user, NewTermPlanActivity ref) {
        boolean update = false;

        Date now = new Date();

        // 老师查看活动页, 7天内没有点击"发起活动按钮"
        DayRange dayRange = getDayRange(ref.getTeacherShowActivity(), 7);
        if (dayRange.contains(now)) {
            if (ref.getTeacherClickAssignBtn() == null && !ref.getTeacherSendMsg1()) {
                // 尊敬的老师，新学期伊始，您是否还在为孩子无心学习发愁？万千老师推荐的收心大法，点此查看！
                sendTeacherMsg(user.getId(), "尊敬的老师，新学期伊始，您是否还在为孩子无心学习发愁？万千老师推荐的收心大法，点此查看！");
                ref.setTeacherSendMsg1(true);
                update = true;
            }
        }

        if (ref.getTeacherClickAssignBtn() != null && !ref.getTeacherSendMsg2()) {
            DayRange beginDayRange = getDayRange(ref.getTeacherClickAssignBtn(), 3);
            if (beginDayRange.contains(now)) {
                boolean flag = true; // 假设需要发消息

                List<User> users = studentLoaderClient.loadTeacherStudents(user.getId());
                for (User userItem : users) {
                    boolean card = isCard(userItem.getId());
                    if (card) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    sendTeacherMsg(user.getId(), "尊敬的老师，您为班级报名的《新学期快速收心计划》活动，暂未收到同学制定的计划哦，赶快分享活动，去提醒一下家长和同学们吧！");
                    ref.setTeacherSendMsg2(true);
                    update = true;
                }
            }
        }
        if (update) {
            userNewTermPlanActivityRefDao.upsert(ref);
        }

        // 老师发起7天, 学生还没布置要给关键家长发短信
        if (ref.getTeacherClickAssignBtn() != null) {
            DayRange beginDayRange = getDayRange(ref.getTeacherClickAssignBtn(), 7);
            if (beginDayRange.contains(now)) {
                Set<Long> studentIdSet = teacherNewTermPlanService.getAllStudentIdByTeacherId(user.getId());
                Map<Long, List<StudentParent>> studentParents = parentLoaderClient.loadStudentParents(studentIdSet);

                for (Long studentId : studentIdSet) {
                    // 只处理老师发起的活动
                    Boolean teacherAssignStatus = teacherNewTermPlanService.loadTeacherAssignStatus(studentId);
                    if (teacherAssignStatus) {
                        // 没布置, 给关键家长发短信
                        NewTermPlanActivity studentActivity = userNewTermPlanActivityRefDao.load(studentId);
                        if (studentActivity == null || studentActivity.getStudentAssign() == null) {
                            List<StudentParent> parentList = studentParents.get(studentId);
                            if (CollectionUtils.isNotEmpty(parentList)) {
                                StudentParent studentParent = parentList.stream().filter(StudentParent::isKeyParent).findFirst().orElse(parentList.get(0));
                                sendParentSms(studentParent.getParentUser().getId(), "尊敬的家长，老师发起《新学期快速收心计划活动》，辅助孩子设定新学期目标，坚持打卡21天，培养受益终身的好习惯，赶快行动吧！");
                            }
                        }
                    }
                }
            }
        }

    }

    private void studentHandler(User user, NewTermPlanActivity ref) {
        boolean update = false;

        Date now = new Date();
        Date showActivity = ref.getStudentShowActivity();
        DayRange dayRange = getDayRange(showActivity, 7);

        // 查看后7天内没去制定
        if (dayRange.contains(now) && ref.getStudentClickGoAssignBtn() == null && !ref.getStudentSendMsg1()) {
            // 开学没状态？！那是方法没用对！揭秘新学期的正确打开方式！立即前往
            sendStudentMsg(user.getId(), "开学没状态？！那是方法没用对！揭秘新学期的正确打开方式！立即前往");
            ref.setStudentSendMsg1(true);
            update = true;
        }

        // 点击去制订3天内没布置
        if (ref.getStudentClickGoAssignBtn() != null && !ref.getStudentSendMsg2()) {
            DayRange dayRange1 = getDayRange(ref.getStudentClickGoAssignBtn(), 3);
            if (dayRange1.contains(now)) {
                if (ref.getStudentAssign() == null) {
                    // 同学你好！新学期要做行动派，计划就要晒出来，赶快行动，赢取《超级行动派》证书！
                    sendStudentMsg(user.getId(), "同学你好！新学期要做行动派，计划就要晒出来，赶快行动，赢取《超级行动派》证书！");
                    ref.setStudentSendMsg2(true);
                    update = true;
                }
            }
        }

        // 布置后3天内没打卡
        if (ref.getStudentAssign() != null && !ref.getStudentSendMsg3()) {
            DayRange dayRange1 = getDayRange(ref.getStudentAssign(), 3);
            if (dayRange1.contains(now)) {
                boolean noCard = isNotCard(user.getId());
                if (noCard) {
                    // Hi，同学，你参与的新学期计划打卡活动已经过去3天啦，30天内，坚持打卡21天，赢取《超级行动派》证书，更有超多学豆哦！
                    sendStudentMsg(user.getId(), "Hi，同学，你参与的新学期计划打卡活动已经过去3天啦，30天内，坚持打卡21天，赢取《超级行动派》证书，更有超多学豆哦！");
                    ref.setStudentSendMsg3(true);
                    update = true;
                }
            }
        }

        if (ref.getStudentClickGoAssignBtn() != null && !ref.getStudentSendMsg4()) {
            DayRange dayRange1 = getDayRange(ref.getStudentClickGoAssignBtn(), 3);
            if (dayRange1.contains(now)) {
                List<StudentParent> studentParents = parentLoaderClient.getParentLoader().loadStudentParents(user.getId());
                NewTermPlanActivity newTermPlanActivity = studentParents.stream().map(i -> userNewTermPlanActivityRefDao.load(i.getParentUser().getId())).filter(Objects::nonNull).findFirst().orElse(null);
                if (newTermPlanActivity == null) {
                    for (StudentParent studentParent : studentParents) {
                        // 尊敬的家长，您的孩子邀请您和Ta一起制定《新学期快速收心计划》，再忙也不要忘了陪Ta一起成长哦，即刻去定计划！
                        sendParentMsg(studentParent.getParentUser().getId(), "尊敬的家长，您的孩子邀请您和Ta一起制定《新学期快速收心计划》，再忙也不要忘了陪Ta一起成长哦，即刻去定计划！");
                    }
                    ref.setStudentSendMsg4(true);
                    update = true;
                }
            }
        }

        if (update) {
            userNewTermPlanActivityRefDao.upsert(ref);
        }

        String msg = "尊敬的家长，您的孩子邀请您和Ta一起制定《新学期快速收心计划》，再忙也不要忘了陪Ta一起成长哦，即刻去定计划！";

        // 接下来只处理学生自己发起的
        boolean exists = cacheExistsUtils.exists(CacheExistsEnum.STUDENT_PARTICIPATE, user.getId());
        if (exists && ref.getStudentClickGoAssignBtn() != null) {
            // 24小时没布置发 push
            DayRange dayRange1 = getDayRange(ref.getStudentClickGoAssignBtn(), 1);
            if (dayRange1.contains(now) && ref.getStudentAssign() == null) {
                StudentParent userKeyParent = getUserKeyParent(user.getId());
                if (userKeyParent != null) {
                    sendParentMsg(userKeyParent.getParentUser().getId(), msg);
                }
            }

            // 3天发短信
            dayRange1 = getDayRange(ref.getStudentClickGoAssignBtn(), 3);
            if (dayRange1.contains(now) && ref.getStudentAssign() == null) {
                StudentParent userKeyParent = getUserKeyParent(user.getId());
                if (userKeyParent != null) {
                    sendParentSms(userKeyParent.getParentUser().getId(), msg);
                }
            }
        }
    }

    private StudentParent getUserKeyParent(Long studentId) {
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        if (CollectionUtils.isNotEmpty(studentParents)) {
            return studentParents.stream().filter(StudentParent::isKeyParent).findFirst().orElse(studentParents.get(0));
        }
        return null;
    }

    private void parentHandler(User user, NewTermPlanActivity ref) {
        boolean update = false;

        Date now = new Date();
        DayRange dayRange = getDayRange(ref.getParentShowActivity(), 7);
        if (dayRange.contains(now) && ref.getParentClickGoAssign() == null && !ref.getParentSendMsg1()) {
            // 尊敬的家长，新学期伊始，您是否还在为孩子无心学习焦虑？行之有效的收心大法，点此查看！
            sendParentMsg(user.getId(), "尊敬的家长，新学期伊始，您是否还在为孩子无心学习焦虑？行之有效的收心大法，点此查看！");
            ref.setParentSendMsg1(true);
            update = true;
        }

        if (ref.getParentClickGoAssign() != null && !ref.getParentSendMsg2()) {
            dayRange = getDayRange(ref.getParentClickGoAssign(), 3);
            if (dayRange.contains(now) && ref.getParentAssign() == null) {
                // 尊敬的家长，您好！新学期要做行动派，趁热打铁，赶快和孩子一起定计划，坚持打卡吧！
                sendParentMsg(user.getId(), "尊敬的家长，您好！新学期要做行动派，趁热打铁，赶快和孩子一起定计划，坚持打卡吧！");
                ref.setParentSendMsg2(true);
                update = true;
            }
        }

        if (ref.getParentAssign() != null && !ref.getParentSendMsg3()) {
            dayRange = getDayRange(ref.getParentAssign(), 3);
            if (dayRange.contains(now)) {
                boolean flag = true; // 假设需要发消息
                List<StudentParentRef> studentParentRefs = parentLoaderClient.getParentLoader().loadParentStudentRefs(user.getId());
                for (StudentParentRef studentParentRef : studentParentRefs) {
                    boolean card = isCard(studentParentRef.getStudentId());
                    if (card) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    // 尊敬的家长，您好！您对孩子新学期的期待是？赶快和孩子一起定计划，坚持打卡，培养好习惯吧！
                    sendParentMsg(user.getId(), "尊敬的家长，您好！您对孩子新学期的期待是？赶快和孩子一起定计划，坚持打卡，培养好习惯吧！");
                    ref.setParentSendMsg3(true);
                    update = true;
                }
            }
        }
        if (update) {
            userNewTermPlanActivityRefDao.upsert(ref);
        }
    }

    private boolean isNotCard(Long studentId) {
        return !isCard(studentId);
    }

    /**
     * 判断学生是否打过卡
     */
    private boolean isCard(Long studentId) {
        NewTermStudentPlanMapper studentTargets = parentNewTermPlanService.getStudentTargets(studentId);
        if (studentTargets != null && CollectionUtils.isNotEmpty(studentTargets.getPlans())) {
            for (NewTermStudentPlanMapper.Plan plan : studentTargets.getPlans()) {
                Long cardDay = teacherNewTermPlanService.loadStudentNewTermTargetCache(studentId, plan.getId());
                if (cardDay > 0) {
                    return true;
                }
            }
        }
        return false;
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
        long time = DateUtils.getTodayStart().getTime() + 17 * 60 * 60 * 1000;
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PRIMARY_TEACHER,
                Collections.singletonList(teacherId), pushExtInfo, time);
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
        long time = DateUtils.getTodayStart().getTime() + 19 * 60 * 60 * 1000;
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.STUDENT, Collections.singletonList(studentId), extInfo, time);
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
        long time = DateUtils.getTodayStart().getTime() + 19 * 60 * 60 * 1000;

        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PARENT, Collections.singletonList(parentId), extras, time);
    }

    private void sendParentSms(Long parentId, String msg) {
        boolean noExists1 = cacheExistsUtils.noExists(CacheExistsEnum.PARENT_NOTICE_ED, parentId);
        boolean noExists2 = cacheExistsUtils.noExists(CacheExistsEnum.STUDENT_JOIN_NOTICE, parentId);
        if (noExists1 && noExists2) {
            cacheExistsUtils.set(CacheExistsEnum.PARENT_NOTICE_ED, parentId);
            String mobile = sensitiveUserDataServiceClient.loadUserMobile(parentId);
            if (StringUtils.isNotBlank(mobile)) {
                SmsMessage smsMessage = new SmsMessage();
                smsMessage.setMobile(mobile);
                smsMessage.setType(SmsType.PARENT_NEWTERM_PLAN_NOTIFY.name());
                smsMessage.setSmsContent(msg);

                String time = DateUtils.dateToString(new Date(DateUtils.getTodayStart().getTime()
                        + 19 * 60 * 60 * 1000), "yyyyMMddHHmmss");
                smsMessage.setSendTime(time);
                smsServiceClient.getSmsService().sendSms(smsMessage);
            }
        }
    }

    private DayRange getDayRange(Date date, int day) {
        return DayRange.newInstance(DateUtils.addDays(DateUtils.getDayStart(date), day + 1).getTime());
    }

}
