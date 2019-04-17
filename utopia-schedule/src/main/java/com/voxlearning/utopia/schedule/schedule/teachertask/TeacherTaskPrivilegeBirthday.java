package com.voxlearning.utopia.schedule.schedule.teachertask;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.business.api.UserAdvertisementInfoService;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilege;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilegeTpl;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskPrivilegeServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by zhouwei on 2018/9/25
 **/
@Named
@ScheduledJobDefinition(
        jobName = "老师等级特权生日园丁豆",
        jobDescription = "老师等级特权生日园丁豆",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "10 10 5 * * ?"
)
public class TeacherTaskPrivilegeBirthday extends ScheduledJobWithJournalSupport {

    @Inject
    private UserServiceClient userServiceClient;

    @Inject
    private TeacherTaskPrivilegeServiceClient teacherTaskPrivilegeServiceClient;

    @Inject
    private TeacherLoaderClient teacherLoader;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @ImportService(interfaceClass = UserAdvertisementInfoService.class)
    private UserAdvertisementInfoService userAdvertisementInfoService;

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Inject
    private AppMessageServiceClient appMessageServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        try {
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            if (parameters.get("month") != null) {
                month = MapUtils.getIntValue(parameters, "month");
            }
            if (parameters.get("day") != null) {
                day = MapUtils.getIntValue(parameters, "day");
            }
            Date date = new Date();
            String dateString = DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATETIME);
            String year = DateUtils.dateToString(date, "yyyy");
            List<Long> teacherIds = userServiceClient.loadTeacherIdByBirthday(month, day);
            String dateStringToday = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
            Date todayStart = DateUtils.stringToDate(dateStringToday, DateUtils.FORMAT_SQL_DATE);
            for (Long id : teacherIds) {
                try {
                    MapMessage mapMessage = teacherTaskPrivilegeServiceClient.getPrivilege(id);
                    if (mapMessage.isSuccess()) {
                        TeacherTaskPrivilege teacherTaskPrivilege = (TeacherTaskPrivilege)mapMessage.get("teacherTaskPrivilege");
                        TeacherTaskPrivilege.Privilege privilege = teacherTaskPrivilege.getByPrivilegeTplId(TeacherTaskPrivilegeTpl.Privilege.BIRTHDAY_INTEGRAL.getId());

                        if (null == privilege) {//老师没有该特权，不处理
                            continue;
                        }

                        if (privilege.getExt() != null && Objects.equals(year, privilege.getExt().get("year"))) {//当前自然年已经发过了，不再重复发放
                            continue;
                        }

                        sendIntegralReward(id, "老师生日特权发放园丁豆", 100);
                        Map<String, Object> ext = new HashMap<>();//ext: date 发豆的详细时间, year 发豆的年份
                        ext.put("date", dateString);
                        ext.put("year", year);
                        privilege.setExt(ext);
                        teacherTaskPrivilegeServiceClient.upsertTeacherTaskPrivilege(teacherTaskPrivilege);
                        //生日发豆需要弹窗
                        //CacheSystem.CBS.getCache("persistence").set("teacher_new_change_level_birthday_app_" + id, 30 * 24 * 60 * 60, 1);
                        pushAndMessge(id);
                        pop(todayStart, id);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 首页弹窗
     * @param todayStart
     * @param id
     */
    private void pop(Date todayStart, Long id) {
        StudentAdvertisementInfo info = new StudentAdvertisementInfo();
        info.setUserId(id);
        info.setSlotId("120202");
        info.setMessageText("生日特权已入袋，可到园丁豆记录查看详情。");
        info.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/birthdaypop.png");
        info.setClickUrl("/view/mobile/teacher/activity2018/primary/level_system/index");
        info.setShowStartTime(todayStart.getTime());
        info.setShowEndTime(todayStart.getTime() + 24 * 60 * 60 * 1000 - 1);
        info.setBtnContent("心意已领，查看更多特权");
        userAdvertisementInfoService.insert(info);
    }

    /**
     * PUSH与消息
     * @param teacherId
     */
    public void pushAndMessge(Long teacherId) {
        String msgContent = "生日快乐，一起教育科技送豪礼！100园丁豆已入袋~";
        String msgTitle = "生日园丁豆";

        //发送App消息
        AppMessage msg = new AppMessage();
        msg.setUserId(teacherId);
        msg.setMessageType(TeacherMessageType.ACTIVIY.getType());
        msg.setContent(msgContent);
        msg.setTitle(msgTitle);
        msg.setCreateTime(new Date().getTime());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(msg);

        // 发送push
        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("s", TeacherMessageType.ACTIVIY.name());
        jpushExtInfo.put("key", "j");
        jpushExtInfo.put("t", "h5");
        TeacherDetail td = teacherLoader.loadTeacherDetail(teacherId);
        appMessageServiceClient.sendAppJpushMessageByIds(
                msgContent,
                AppMessageUtils.getMessageSource("17Teacher", td),
                Collections.singletonList(teacherId),
                jpushExtInfo);
    }

    /**
     * 发学豆
     * @param teacherId
     * @param name
     * @param num
     */
    public boolean sendIntegralReward(Long teacherId, String name, Integer num) {
        // 如果是小学老师换算成学豆
        TeacherDetail td = teacherLoader.loadTeacherDetail(teacherId);
        /** 小学学豆乘以10 **/
        if (td.isPrimarySchool()) {
            num = num * 10;
        }
        IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.TEACHER_GROWTH_REWARD_PRIVILEGE_BIRTHDAY_INTEGRAL, num);
        integralHistory.setComment(name);
        MapMessage chgIntegralResult = userIntegralService.changeIntegral(integralHistory);
        if (!chgIntegralResult.isSuccess()) {
            logger.error("生日园丁豆发送失败 !tId:{}, name:{}, reward:{}, detail:{}", teacherId, name, num, chgIntegralResult.getInfo());
            return false;
        }
        return true;
    }
}
