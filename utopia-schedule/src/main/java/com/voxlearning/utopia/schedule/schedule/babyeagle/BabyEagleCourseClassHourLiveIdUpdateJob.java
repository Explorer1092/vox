package com.voxlearning.utopia.schedule.schedule.babyeagle;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.schedule.support.ProgressedScheduleJob;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.wonderland.api.BabyEagleLoader;
import com.voxlearning.utopia.service.wonderland.api.BabyEagleService;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleClassHour;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 小鹰学堂：课程结束后更新观看记录信息job
 *
 * @author liu jingchao
 * @since 2017/07/07
 */
@Named
@ScheduledJobDefinition(
        jobName = "小鹰学堂：课程更新LiveId信息job",
        jobDescription = "不自动运行",
        disabled = {Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 1 0 1 1 ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class BabyEagleCourseClassHourLiveIdUpdateJob extends ProgressedScheduleJob {

    final private static String ONLINE_RECEIVER = "jingchao.liu@17zuoye.com";
    final private static String TEST_RECEIVER = "jingchao.liu@17zuoye.com";
    final private static String ONLINE_CC = "";

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

        Boolean run = false;
        String targetClassHourId = "";
        Date currentDate = new Date();

        if (MapUtils.isNotEmpty(parameters) && parameters.containsKey("hand"))
            run = (boolean) parameters.get("hand");
        else {
            progressMonitor.done();
            return;
        }

        if (MapUtils.isNotEmpty(parameters) && parameters.containsKey("classHourId"))
            targetClassHourId = parameters.get("classHourId").toString();


        List<BabyEagleClassHour> classHourList = new ArrayList<>();
        List<String> errorUpdateList = new ArrayList<>();
        if (StringUtils.isNotBlank(targetClassHourId)) {
            // 获取课时信息
            BabyEagleClassHour classHour = babyEagleLoader.getBabyEagleClassHourFromDB(targetClassHourId).getUninterruptibly();
            if (classHour == null) {
                progressMonitor.done();
                return;
            }
            classHourList.add(classHour);
        } else
            classHourList = babyEagleLoader.loadBabyEagleClassHourForNeedUpdateClassHourLiveId().getUninterruptibly();

        try {
            if (CollectionUtils.isNotEmpty(classHourList)) {
                for (BabyEagleClassHour thisClassHour : classHourList) {

                    MapMessage resultMessage = babyEagleService.updateClassHourLiveId(thisClassHour, run);
                    if (!resultMessage.isSuccess())
                        errorUpdateList.add(thisClassHour + ":" + resultMessage.getInfo());

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            progressMonitor.done();
        }

        // 如果没有可以更新的内容则不发送邮件
        if (classHourList.size() == 0) {
            progressMonitor.done();
            return;
        }

        StringBuilder desc = new StringBuilder();
        desc.append("小鹰学堂：课程LiveId信息更新完毕：\n\n");
        desc.append("1.更新的课程数量: ").append(classHourList.size()).append("\n\n");

        desc.append("更新的课程列表: ").append(classHourList.size()).append("\n");
        for (BabyEagleClassHour classHour : classHourList) {
            desc.append("classHourId:").append(classHour.getId()).append("::");
            desc.append("talkFunCourseId:").append(classHour.getTalkFunCourseId()).append("::");
            desc.append("time:").append(DateUtils.dateToString(classHour.getStartTime(), "MM-dd HH:mm:ss"))
                    .append("~").append(DateUtils.dateToString(classHour.getEndTime(), "HH:mm:ss")).append("\n");
        }

        desc.append("更新错误的课程列表: ").append(errorUpdateList.size()).append("\n");
        for (String errInfo : errorUpdateList) {
            desc.append(errInfo).append("\n");
        }

        sendMail("小鹰学堂：课程观看记录更新" + " (" + RuntimeMode.current().name() + "环境)", desc.toString());

        progressMonitor.done();
    }

    // 发送邮件
    private boolean sendMail(String title, String desc) {
        String date = DateUtils.getNowSqlDatetime();
        String content = desc + "\n\n";
        content += "操作时间：" + date;
        try {
            emailServiceClient.createPlainEmail()
                    .to(RuntimeMode.current().gt(Mode.STAGING) ? ONLINE_RECEIVER : TEST_RECEIVER)
                    .cc(RuntimeMode.current().gt(Mode.STAGING) ? ONLINE_CC : null)
                    .subject(title)
                    .body(content)
                    .send();
        } catch (Exception e) {
            logger.error("sendEmail failed");
            return false;
        }
        return true;
    }
}
