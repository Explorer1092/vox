package com.voxlearning.utopia.schedule.schedule.babyeagle;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.schedule.support.ProgressedScheduleJob;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.wonderland.api.BabyEagleLoader;
import com.voxlearning.utopia.service.wonderland.api.BabyEagleService;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleClassHour;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleCourseInfo;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleTeacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 小鹰学堂：教师课时列表每日通知job
 *
 * @author liu jingchao
 * @since 2017/07/19
 */
@Named
@ScheduledJobDefinition(
        jobName = "小鹰学堂：教师课时列表每日通知job",
        jobDescription = "自动运行",
        disabled = {Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 1 22 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class BabyEagleTeacherClassHourEmailNoticeJob extends ProgressedScheduleJob {

    final private static String ONLINE_CC = "jingchao.liu@17zuoye.com;fugui.chang@17zuoye.com";

    @ImportService(interfaceClass = BabyEagleService.class)
    private BabyEagleService babyEagleService;

    @ImportService(interfaceClass = BabyEagleLoader.class)
    private BabyEagleLoader babyEagleLoader;

    @Inject
    private EmailServiceClient emailServiceClient;

    @Override
    protected void executeJob(long startTimestamp,
                              Map<String, Object> parameters,
                              ISimpleProgressMonitor progressMonitor) {

        Boolean run = true;
        String targetTeacherId = "";
        Date currentDate = new Date();

        if (MapUtils.isNotEmpty(parameters) && parameters.containsKey("hand"))
            run = (boolean) parameters.get("hand");

        if (MapUtils.isNotEmpty(parameters) && parameters.containsKey("teacherId"))
            targetTeacherId = parameters.get("teacherId").toString();


        List<BabyEagleTeacher> teacherInfoList = new ArrayList<>();
        List<String> errorUpdateList = new ArrayList<>();
        if (StringUtils.isNotBlank(targetTeacherId)) {
            // 获取老师信息
            BabyEagleTeacher teacherInfo = babyEagleLoader.getBabyEagleTeacherInfoFromDB(targetTeacherId).getUninterruptibly();
            if (teacherInfo == null) {
                progressMonitor.done();
                return;
            }
            teacherInfoList.add(teacherInfo);
        } else
            teacherInfoList = babyEagleLoader.loadAllBabyEagleTeachersFromDB().getUninterruptibly();

        try {
            if (CollectionUtils.isNotEmpty(teacherInfoList)) {
                for (BabyEagleTeacher thisTeacher : teacherInfoList) {

                    if (StringUtils.isBlank(thisTeacher.getEmail()))
                        continue;

                    // 查询教师的所有课时
                    List<BabyEagleClassHour> classHourList = babyEagleLoader.loadAllBabyEagleClassHourByTeacherIdFromDB(thisTeacher.getId()).getUninterruptibly();
                    classHourList = classHourList.stream()
                            .filter(hour -> hour.getStartTime() != null)
                            .filter(hour -> hour.getStartTime().after(DateUtils.getTodayStart()) && hour.getStartTime().before(DateUtils.getDayEnd(DateUtils.addDays(currentDate, 1))))
                            .collect(Collectors.toList());

                    if (classHourList.size() == 0)
                        continue;

                    // 按课程内容转化为map
                    Map<String, List<BabyEagleClassHour>> todayCourseInfoClassHourListMap = classHourList.stream().filter(hour -> hour.getStartTime().before(DateUtils.getTodayEnd())).collect(Collectors.groupingBy(BabyEagleClassHour::getCourseId));
                    Map<String, List<BabyEagleClassHour>> tomorrowCourseInfoClassHourListMap = classHourList.stream().filter(hour -> hour.getStartTime().after(DateUtils.getTodayEnd())).collect(Collectors.groupingBy(BabyEagleClassHour::getCourseId));

                    StringBuilder desc = new StringBuilder();
                    desc.append("你好~").append(thisTeacher.getName()).append("老师：\n\n");
                    desc.append("欢拓ID账号：").append(thisTeacher.getBid()).append(" (请牢记) \n\n");
                    if (MapUtils.isNotEmpty(tomorrowCourseInfoClassHourListMap)) {
                        desc.append("---------------------------------------------------------------- \n");
                        desc.append("明日").append(DateUtils.dateToString(DateUtils.addDays(currentDate, 1), "MM-dd")).append("课程课时列表如下：\n");
                        int order = 0;
                        for (String courseInfoId : tomorrowCourseInfoClassHourListMap.keySet()) {
                            List<BabyEagleClassHour> thisClassHourList = tomorrowCourseInfoClassHourListMap.get(courseInfoId);
                            if (CollectionUtils.isNotEmpty(thisClassHourList)) {
                                order++;
                                BabyEagleCourseInfo thisCourseInfo = babyEagleLoader.getBabyEagleCourseInfoFromDB(courseInfoId).getUninterruptibly();
                                desc.append(order).append(".").append(thisCourseInfo.getCourseName()).append("\n");
                                thisClassHourList.stream().sorted(Comparator.comparing(BabyEagleClassHour::getStartTime)).forEach(classHour ->
                                        desc.append(DateUtils.dateToString(classHour.getStartTime(), "HH:mm:ss")).append("~").append(DateUtils.dateToString(classHour.getEndTime(), "HH:mm:ss")).append("\n")
                                );
                            }

                        }
                        desc.append("(如有课时时间调整，请马上联系管理员！) \n");
                    }

                    if (MapUtils.isNotEmpty(todayCourseInfoClassHourListMap)) {
                        desc.append("---------------------------------------------------------------- \n");
                        desc.append("今日").append(DateUtils.dateToString(currentDate, "MM-dd")).append("课程课时列表如下：\n");
                        int order = 0;
                        for (String courseInfoId : todayCourseInfoClassHourListMap.keySet()) {
                            List<BabyEagleClassHour> thisClassHourList = todayCourseInfoClassHourListMap.get(courseInfoId);
                            if (CollectionUtils.isNotEmpty(thisClassHourList)) {
                                order++;
                                BabyEagleCourseInfo thisCourseInfo = babyEagleLoader.getBabyEagleCourseInfoFromDB(courseInfoId).getUninterruptibly();
                                desc.append(order).append(".").append(thisCourseInfo.getCourseName()).append("\n");
                                thisClassHourList.stream().sorted(Comparator.comparing(BabyEagleClassHour::getStartTime)).forEach(classHour ->
                                        desc.append(DateUtils.dateToString(classHour.getStartTime(), "HH:mm:ss")).append("~").append(DateUtils.dateToString(classHour.getEndTime(), "HH:mm:ss"))
                                                .append("：").append(classHour.isFinish() ? "已正常下课" : "未正常下课").append("：").append(classHour.canPlayBack() ? "回放已上传" : "回放未上传").append("：").append(classHour.liveUv() == 0 ? "" : "观看人数" + classHour.liveUv()).append("\n")
                                );
                            }

                        }
                        desc.append("\n\n劳累了一天，辛苦了!^^\n");
                    }

                    desc.append("---------------------------------------------------------------- \n");
                    desc.append("小贴士：为了达到最理想的听课效果，请于开课前5分钟左右开始上课，结课时间过后5分钟内开始下课，尽量不要提前下课哦！谢谢配合~ \n");
                    desc.append("例如：上课时间为11：00~11：30，那么10:55~11:32就是最佳课程时间 \n");

                    if (run)
                        sendMail(desc.toString(), thisTeacher.getEmail());
                    else
                        sendMail(desc.toString(), ONLINE_CC);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            progressMonitor.done();
        }


        progressMonitor.done();
    }

    // 发送邮件
    private boolean sendMail(String desc, String address) {
        String date = DateUtils.getNowSqlDatetime();
        String content = desc + "\n\n";
        content += "操作时间：" + date;
        try {
            emailServiceClient.createPlainEmail()
                    .to(address)
                    .cc(ONLINE_CC)
                    .subject("小鹰学堂：教师课时通知")
                    .body(content)
                    .send();
        } catch (Exception e) {
            logger.error("sendEmail failed");
            return false;
        }
        return true;
    }
}
