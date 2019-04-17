package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.service.campaign.api.entity.WarmHeartPlanActivity;
import com.voxlearning.utopia.service.campaign.api.mapper.WarmHeartPlanMapper;
import com.voxlearning.utopia.service.campaign.impl.dao.WarmHeartPlanActivityDao;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsEnum;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsUtils;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
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
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.WarmHeartPlanConstant.*;

@Named
@Slf4j
public class WarmHeartPlanJobListenerService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private WarmHeartPlanActivityDao warmHeartPlanActivityDao;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    protected AppMessageServiceClient appMessageServiceClient;
    @Inject
    private CacheExistsUtils cacheExistsUtils;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private WarmHeartPlanServiceImpl warmHeartPlanService;
    @Inject
    private WarmHeartPlanListenerServiceUtils warmHeartPlanListenerServiceUtils;
    @Inject
    private UserLoginServiceClient userLoginServiceClient;

    public void handle(Long userId) {
        User user = userLoaderClient.loadUser(userId);

        WarmHeartPlanActivity load = warmHeartPlanActivityDao.load(user.getId());
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

    private void studentHandler(User user, WarmHeartPlanActivity ref) {
        boolean update = false;

        Date now = new Date();
        Date showActivity = ref.getStudentShowActivity();
        DayRange dayRange = getDayRange(showActivity, 3);

        // 3天内没有点击召唤我的超级英雄
        if (dayRange.contains(now) && ref.getStudentClickGoAssignBtn() == null && !ref.getStudentSendMsg1()) {
            sendStudentMsg(user.getId(), "【神秘力量】想让爸爸妈妈放下手机陪陪你，却不敢告诉他们。戳我，让你的成长因父母的陪伴而美妙！\uD83D\uDC49");
            ref.setStudentSendMsg1(true);
            update = true;
        }

        WarmHeartPlanMapper studentTargets = warmHeartPlanService.getStudentTargets(user.getId());

        // 召唤了但是还没设置目标
        if (ref.getStudentClickGoAssignBtn() != null && studentTargets == null) {
            DayRange oneDayRange = getDayRange(ref.getStudentClickGoAssignBtn(), 1);
            DayRange threeDayRange = getDayRange(ref.getStudentClickGoAssignBtn(), 3);
            if (oneDayRange.contains(now)) {
                String parentMsg2Text = warmHeartPlanListenerServiceUtils.getParentMsg2Text(user.getId());
                sendStudentParentMsg(user.getId(), parentMsg2Text);
                ref.setStudentSendMsg2(true);
                update = true;
            }
            if (threeDayRange.contains(now)) {
                String parentMsg2Text = warmHeartPlanListenerServiceUtils.getParentMsg2Text(user.getId());
                sendStudentParentSms(user.getId(), parentMsg2Text);
                ref.setStudentSendMsg2(true);
                update = true;
            }

            //30天内 每6天发一次 push
            if (DateUtils.dayDiff(now, ref.getStudentClickGoAssignBtn()) <= 30) {
                if (DateUtils.dayDiff(now, oneDayRange.getStartDate()) % 6 == 0) {
                    String parentMsg2Text = warmHeartPlanListenerServiceUtils.getParentMsg2Text(user.getId());
                    sendStudentParentMsg(user.getId(), parentMsg2Text);
                    ref.setStudentSendMsg2(true);
                    update = true;
                }
            }
        }

        // 超过30天提醒继续添加
        if (studentTargets != null) {
            boolean notEnd = studentTargets.getPlans().stream().filter(i -> i.getEndDate() != null).anyMatch(i -> now.before(i.getEndDate()));
            if (notEnd) {
                studentFestival(user, now);
            }

            DayRange yesterdayRange = DayRange.newInstance(DateUtils.addDays(now, -1).getTime());
            for (WarmHeartPlanMapper.Plan plan : studentTargets.getPlans()) {
                if (yesterdayRange.contains(plan.getEndDate())) {
                    // 学生只有才首次过期才提醒
                    boolean noExists = cacheExistsUtils.noExists(CacheExistsEnum.WARM_HEART_PARENT_FIRST_END_30, user.getId());
                    if (noExists) {
                        cacheExistsUtils.set(CacheExistsEnum.WARM_HEART_PARENT_FIRST_END_30, user.getId());
                        sendStudentMsg(user.getId(), "【好消息】成长的路上，有父母的陪伴真好！赶快和爸妈继续制定陪伴计划！\uD83D\uDC49");
                    }
                    String parentMsg1Text = warmHeartPlanListenerServiceUtils.getParentMsg1Text(user.getId());
                    sendStudentParentMsg(user.getId(), parentMsg1Text);
                    ref.setStudentSendMsg3(true);
                    update = true;
                    break;
                }
            }

            // 三天内没有打卡 7天重复推
            if (ref.getStudentClickGoAssignBtn() != null) {
                DayRange threeDayRange = getDayRange(ref.getStudentClickGoAssignBtn(), 3);

                if (threeDayRange.contains(now)) {
                    boolean notCard = isNotCard(user.getId());
                    if (notCard) {
                        sendStudentMsg(user.getId(), "【加油提醒】Hi，同学！「召唤你的超级英雄」已经过去3天啦，为爸妈加油，坚持陪伴打卡21天，赢得超多学豆奖励！\uD83D\uDC49");
                        String parentMsg3Text = warmHeartPlanListenerServiceUtils.getParentMsg3Text(user.getId());
                        sendStudentParentMsg(user.getId(), parentMsg3Text);

                        ref.setStudentSendMsg4(true);
                        update = true;
                    }
                }

                if (DateUtils.dayDiff(now, threeDayRange.getEndDate()) % 7 == 0) {
                    String parentMsg3Text = warmHeartPlanListenerServiceUtils.getParentMsg3Text(user.getId());
                    sendStudentParentMsg(user.getId(), parentMsg3Text);

                    ref.setStudentSendMsg4(true);
                    update = true;
                }
            }
        }

        if (update) {
            warmHeartPlanActivityDao.upsert(ref);
        }
    }

    private void parentHandler(User user, WarmHeartPlanActivity ref) {
        boolean update = false;

        Date now = new Date();

        festival(user, ref, now);

        DayRange dayRange = getDayRange(ref.getParentShowActivity(), 3);
        if (dayRange.contains(now) && ref.getParentClickGoAssign() == null && !ref.getParentSendMsg1()) {
            // 尊敬的家长，新学期伊始，您是否还在为孩子无心学习焦虑？行之有效的收心大法，点此查看！
            sendParentMsg(user.getId(), "【暖心亲子计划】一辈子很短，再不陪伴，孩子就长大了！马上加入\uD83D\uDC49");
            ref.setParentSendMsg1(true);
            update = true;
        }

        if (ref.getParentClickGoAssign() != null) {
            // 点击启动计划、3天内未定制目标
            if (dayRange.contains(now)) {
                Set<Long> studentIds = parentLoaderClient.loadParentStudentRefs(user.getId()).stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());

                boolean sendParent = false;
                for (Long studentId : studentIds) {
                    WarmHeartPlanMapper studentTargets = warmHeartPlanService.getStudentTargets(studentId);
                    if (studentTargets == null) {
                        sendParent = true;
                        sendStudentMsg(studentId, "【爱的召唤】和爸爸妈妈在一起走过的时光，总是那么快乐。赶快和爸妈一起制定陪伴计划，成为最幸福的成长之星！马上召唤爸妈\uD83D\uDC49");
                    }
                }
                if (sendParent) {
                    String parentMsg4Text = warmHeartPlanListenerServiceUtils.getParentMsg4Text(user.getId());
                    sendParentMsg(user.getId(), parentMsg4Text);
                    ref.setParentSendMsg2(true);
                    update = true;
                }
            }

            // 还没指定的话 7天内重复推送
            if (DateUtils.dayDiff(now, dayRange.getEndDate()) % 7 == 0) {
                String parentMsg4Text = warmHeartPlanListenerServiceUtils.getParentMsg4Text(user.getId());
                if (StringUtils.isNotBlank(parentMsg4Text)) {
                    boolean parentSend = parentLoaderClient.loadParentStudentRefs(user.getId()).stream().map(StudentParentRef::getStudentId).anyMatch(i -> warmHeartPlanService.getStudentTargets(i) == null);
                    if (parentSend) {
                        sendParentMsg(user.getId(), parentMsg4Text);
                        ref.setParentSendMsg2(true);
                        update = true;
                    }
                }
            }
        }

        if (update) {
            warmHeartPlanActivityDao.upsert(ref);
        }
    }

    private void teacherHandler(User user, WarmHeartPlanActivity ref) {
        boolean update = false;

        Date now = new Date();

        // 老师查看活动页, 1天内没有点击"发起活动按钮"
        DayRange dayRange = getDayRange(ref.getTeacherShowActivity(), 1);
        if (dayRange.contains(now)) {
            if (ref.getTeacherClickAssignBtn() == null && !ref.getTeacherSendMsg1()) {
                sendTeacherMsg(user.getId(), "【好消息】家校合力，给学生最好的教育！一键启动「家校共育计划！\uD83D\uDC49");
                ref.setTeacherSendMsg1(true);
                update = true;
            }
        }

        if (ref.getTeacherClickAssignBtn() != null) {
            DayRange beginDayRange = getDayRange(ref.getTeacherClickAssignBtn(), 3);
            if (beginDayRange.contains(now)) {
                List<Long> studentIds = warmHeartPlanService.loadStudentsByTeacherId(user.getId());

                Map<Long, List<StudentParentRef>> studentParentMap = raikouSystem.findStudentParentRefs(studentIds).asStudentIdGroup();
                List<Long> bindParentStudentIds = studentIds.stream().filter(i -> CollectionUtils.isNotEmpty(studentParentMap.get(i))).collect(Collectors.toList());

                for (Long studentId : bindParentStudentIds) {
                    boolean noTargets = warmHeartPlanService.getStudentTargets(studentId) == null;
                    if (noTargets) {
                        List<StudentParentRef> itemParents = studentParentMap.getOrDefault(studentId, Collections.emptyList());
                        for (StudentParentRef parentRef : itemParents) {
                            if (isNotActive(parentRef.getParentId())) {
                                sendParentSms(parentRef.getParentId(), "【好消息】老师已开启《家校共育》计划。每天陪孩子做些小事，坚持21天，和孩子一起成为更好的自己！点击查看>>");
                            }
                        }
                        sendStudentMsg(studentId, "【好消息】你的老师已开启《家校共育》计划，和爸妈每天做一些小事，成为最幸福的成长之星！点击参加>>");
                    }
                }
            }
        }

        if (ref.getTeacherClickAssignBtn() != null && !ref.getTeacherSendMsg2()) {
            DayRange beginDayRange = getDayRange(ref.getTeacherClickAssignBtn(), 3);
            if (beginDayRange.contains(now)) {
                boolean flag = true; // 假设需要发消息

                List<Long> studentIds = warmHeartPlanService.loadStudentsByTeacherId(user.getId());
                for (Long studentId : studentIds) {
                    boolean card = isCard(studentId);
                    if (card) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    sendTeacherMsg(user.getId(), "【贴心提示】敬爱的老师，您已开启《家校共育》计划，暂未收到学生家长制定的亲子陪伴计划！赶紧鼓励更多家长用爱陪伴孩子成长，Go！>>");
                    ref.setTeacherSendMsg2(true);
                    update = true;
                }
            }
        }
        if (update) {
            warmHeartPlanActivityDao.upsert(ref);
        }
    }

    private boolean isNotCard(Long studentId) {
        return isCard(studentId);
    }

    private boolean isCard(Long studentId) {
        WarmHeartPlanMapper studentTargets = warmHeartPlanService.getStudentTargets(studentId);
        if (studentTargets != null && CollectionUtils.isNotEmpty(studentTargets.getPlans())) {
            for (WarmHeartPlanMapper.Plan plan : studentTargets.getPlans()) {
                Long cardDay = warmHeartPlanService.getStudentWarmHeartTargetCache(studentId, plan.getId());
                if (cardDay > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNotActive(Long userId) {
        return !isActive(userId);
    }

    public boolean isActive(Long userId) {
        Date userLastLoginTime = userLoginServiceClient.findUserLastLoginTime(userId);
        if (userLastLoginTime == null) {
            return false;
        }
        Date preMonth = DateUtils.addMonths(new Date(), -1);
        return userLastLoginTime.after(preMonth);
    }

    private void festival(User user, WarmHeartPlanActivity ref, Date now) {
        try {
            String msg = "";
            if (DAY_0422.contains(now)) {
                msg = "陪伴孩子，共读不同寻常的“世界地球日”！马上记录打卡>>";
            } else if (DAY_0501.contains(now)) {
                msg = "如何凭本事在小长假高质量陪伴孩子？看这里>";
            } else if (DAY_0506.contains(now)) {
                msg = "今天立夏，你给孩子编蛋兜、吃蛋、称体重了吗？记录你的亲子时光！Go>>";
            } else if (DAY_0512.contains(now)) {
                msg = "母亲节快乐！母亲是孩子最好的老师～点我，留给孩子最美好的记忆！>>";
            }
            if (StringUtils.isNotBlank(msg)) {
                sendParentMsg(user.getId(), msg);
            }
        } catch (Exception ignore) {
        }
    }

    private void studentFestival(User user, Date now) {
        try {
            String msg = "";
            if (DAY_0601.contains(now)) {
                msg = "儿童节，陪伴是给孩子最好的礼物！爱娃马上晒出来>>";
            } else if (DAY_0607.contains(now)) {
                msg = "端午小长假，不知道怎么陪伴孩子，照着这样陪伴孩子总是没错了。立即查看>>";
            } else if (DAY_0616.contains(now)) {
                msg = "父亲节快乐！最好的爱，不要等～点我，记录欢乐的亲子时光>>";
            } else if (DAY_0623.contains(now)) {
                msg = "奥林匹克日，用运动拉近与孩子梦想的距离！马上打卡动起来>>";
            }
            if (StringUtils.isNotBlank(msg)) {
                sendStudentParentMsg(user.getId(), msg);
            }
        } catch (Exception ignore) {
        }
    }

    private void sendTeacherMsg(Long teacherId, String msg) {
        if (StringUtils.isBlank(msg)) {
            return;
        }

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
        long time = DateUtils.getTodayStart().getTime() + 17 * 60 * 60 * 1000;
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PRIMARY_TEACHER,
                Collections.singletonList(teacherId), pushExtInfo, time);
    }

    private void sendStudentMsg(Long studentId, String msg) {
        if (StringUtils.isBlank(msg)) {
            return;
        }

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
        long time = DateUtils.getTodayStart().getTime() + 19 * 60 * 60 * 1000;
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.STUDENT, Collections.singletonList(studentId), extInfo, time);
    }

    private void sendParentMsg(Long parentId, String msg) {
        if (StringUtils.isBlank(msg)) {
            return;
        }

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
        long time = DateUtils.getTodayStart().getTime() + 19 * 60 * 60 * 1000;

        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PARENT, Collections.singletonList(parentId), extras, time);
    }

    private void sendParentSms(Long parentId, String msg) {
        if (StringUtils.isBlank(msg)) {
            return;
        }
        //boolean noExists1 = cacheExistsUtils.noExists(CacheExistsEnum.WARM_HEART_PARENT_NOTICE_ED, parentId);
        boolean noExists1 = true; // 源头上已经控制，需要发送两次，限制去掉
        if (noExists1) {
            cacheExistsUtils.set(CacheExistsEnum.WARM_HEART_PARENT_NOTICE_ED, parentId);
            String mobile = sensitiveUserDataServiceClient.loadUserMobile(parentId);
            if (StringUtils.isNotBlank(mobile)) {
                SmsMessage smsMessage = new SmsMessage();
                smsMessage.setMobile(mobile);
                smsMessage.setType(SMS_TYPE.name());
                smsMessage.setSmsContent(msg);

                String time = DateUtils.dateToString(new Date(DateUtils.getTodayStart().getTime()
                        + 19 * 60 * 60 * 1000), "yyyyMMddHHmmss");
                smsMessage.setSendTime(time);
                smsServiceClient.getSmsService().sendSms(smsMessage);
            }
        }
    }

    private void sendStudentParentMsg(Long studentId, String msg) {
        if (StringUtils.isBlank(msg)) {
            return;
        }
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        for (StudentParent studentParent : studentParents) {
            sendParentMsg(studentParent.getParentUser().getId(), msg);
        }
    }

    private void sendStudentParentSms(Long studentId, String msg) {
        if (StringUtils.isBlank(msg)) {
            return;
        }
        List<StudentParent> parentList = parentLoaderClient.loadStudentParents(studentId);
        if (CollectionUtils.isNotEmpty(parentList)) {
            StudentParent keyParent = parentList.stream()
                    .filter(StudentParent::isKeyParent).findFirst().orElse(parentList.get(0));
            sendParentSms(keyParent.getParentUser().getId(), msg);
        }
    }

    private DayRange getDayRange(Date date, int day) {
        return DayRange.newInstance(DateUtils.addDays(DateUtils.getDayStart(date), day + 1).getTime());
    }

}
