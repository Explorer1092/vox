package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.service.WeekReportService;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.WeekReportLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.NewHomeworkPublisher;
import com.voxlearning.utopia.service.newhomework.impl.queue.NewHomeworkParentQueueProducer;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;


@Named
@Service(interfaceClass = WeekReportService.class)
@ExposeService(interfaceClass = WeekReportService.class)
public class WeekReportServiceImpl extends SpringContainerSupport implements WeekReportService {
    private static final int WXQQ_BEANNUM = 20; // 分享QQ和微信的奖励学豆数

    private static final int CLAZZ_BEANNUM = 50; // 分享QQ和微信的奖励学豆数

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;

    @Inject
    private WeekReportLoaderImpl weekReportLoader;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private AppMessageServiceClient appMessageServiceClient;

    @Inject
    private NewHomeworkParentQueueProducer newHomeworkParentQueueProducer;

    @Inject
    private NewHomeworkPublisher newHomeworkPublisher;

    @Override
    public MapMessage shareWholeReport(Long tid, String endTime, List<String> groupIdToReportIds, Long realId) {
        try {
//            Map<String, List<WeekReportRecord.Location>> stringListMap = weekReportRecordDao.loadWeekReportRecordByTeacherIdReportEndTime(Collections.singleton(tid + "|" + endTime));
//            if (stringListMap.get(tid + "|" + endTime).isEmpty()) {
//
//
////                String s = StringUtils.join(groupIdToReportIds.toArray(), ",");
//
//                weekReportRecordDao.shareWholeReport(tid, endTime, groupIdToReportIds);
//
//                int days = weekReportLoader.differentDays(new Date(), DateUtils.stringToDate(endTime, "yyyyMMdd"));
//                if (30 - days >= 0) {
//                    IntegralHistory integralHistory = new IntegralHistory(realId, IntegralType.TEACHER_SHARE_REPORT_EASEMOB_REWRAD, CLAZZ_BEANNUM);
//
//                    integralHistory.setComment("分享作业周报到家校群奖励");
//
//                    userIntegralService.changeIntegral(integralHistory);
//                }
//
//            }
            return teacherPushMessage(realId, groupIdToReportIds,endTime);
        } catch (Exception e) {
            logger.error("share Whole Report failed : tid {} , endTime {} , groupIdToReportIds {} , realId {}", tid, endTime, groupIdToReportIds, realId, e);
            return MapMessage.errorMessage();
        }
    }


    @Override
    public MapMessage teacherPushMessage(Long tid, List<String> groupIdToReportIds,String endTime) {

        try {
            String iMContent = "家长好，我为每个学生准备了周学习报告，请家长查看。";
//                               "家长好，请查看上周学习报告。"

            Teacher teacher = teacherLoaderClient.loadTeacher(tid);
            if (teacher == null) {
                return MapMessage.errorMessage();
            }

            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
            Long teacherId = mainTeacherId == null ? teacher.getId() : mainTeacherId;
            //这里才是取所有的学科
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
            List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
            List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
            String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";

            String em_push_title = teacher.fetchRealnameIfBlankId() + subjectsStr + "：" + iMContent;


            for (String s : groupIdToReportIds) {
                String link = "/view/mobile/common/weekreport/clazzreport";
                String[] split = StringUtils.split(s, "|");
                if (split != null && split.length == 2) {
                    // link += s;
                    Map<String, String> param = new LinkedHashMap<>();
                    param.put("garId", s);
                    link = UrlUtils.buildUrlQuery(link, param);
                    //新的极光push
                    Map<String, Object> jpushExtInfo = new HashMap<>();
                    jpushExtInfo.put("studentId", "");
                    jpushExtInfo.put("s", ParentAppPushType.HOMEWORK_WEEK_REPORT.name());
                    jpushExtInfo.put("url", link);
                    appMessageServiceClient.sendAppJpushMessageByTags(em_push_title,
                            AppMessageSource.PARENT,
                            Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(split[0]))),
                            null,
                            jpushExtInfo);

                    //发到Parent Provider 替换掉环信
                    //新的群组消息ScoreCircle
//                    ScoreCircleQueueCommand circleQueueCommand = new ScoreCircleQueueCommand();
//                    circleQueueCommand.setGroupId(SafeConverter.toLong(split[0]));
//                    circleQueueCommand.setCreateDate(new Date());
//                    circleQueueCommand.setGroupCircleType("HOMEWORK_WEEK_REPORT");
//                    circleQueueCommand.setTypeId(s);
//                    circleQueueCommand.setImgUrl("");
//                    circleQueueCommand.setLinkUrl(link);
//                    circleQueueCommand.setContent("这一周作业的整体情况，各位同学是进步还是退步了，请家长阅读并重视！");
//                    //正文
//                    if(StringUtils.isNotBlank(endTime)){
//                        Date endDate = DateUtils.stringToDate(endTime, "yyyyMMdd");
//                        Date beginDate = DateUtils.addDays(endDate, -6);
//                        String endTimeStr = DateUtils.dateToString(endDate,"MM.dd");
//                        String beginTimeStr = DateUtils.dateToString(beginDate,"MM.dd");
//                        circleQueueCommand.setCardContent("请查收老师分享的"+beginTimeStr+"-"+endTimeStr+"作业周报");
//                    }
//                    newHomeworkParentQueueProducer.getProducer().produce(Message.newMessage().writeObject(circleQueueCommand));

                    sendScoreCircleMessage(SafeConverter.toLong(split[0]), s, link, endTime);
                }
            }

            // 发送广播
            Map<String, Object> map = new HashMap<>();
            map.put("messageType", NewHomeworkPublishMessageType.shareWeekReport);
            map.put("teacherId", tid);
            newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error("teacher Push Message failed : tid {} , groupIdToReportIds {}", tid, groupIdToReportIds, e);
            return MapMessage.errorMessage();
        }

    }

    private void sendScoreCircleMessage(Long groupId, String typeId, String link, String endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("createDate", new Date().getTime());
        map.put("groupCircleType", "HOMEWORK_WEEK_REPORT");
        map.put("typeId", typeId);
        map.put("imgUrl", "");
        if (StringUtils.isNotBlank(endTime)) {
            map.put("endTime", endTime);
        }
        newHomeworkParentQueueProducer.getProducer().produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
    }


    @Override
    public void pushMessageToTeacher(List<Long> teacherIds) {
        String link = "/view/mobile/common/weekreport/list";

        Map<String, Object> extInfo = MapUtils.m("link", link,  "s", TeacherMessageType.WEEKREPORTNOTICE.getType(), "t", "week_report", "key", "j");
        appMessageServiceClient.sendAppJpushMessageByIds("作业周报已生成，快点击分享给家长吧~", AppMessageSource.PRIMARY_TEACHER, new ArrayList<>(teacherIds), extInfo, 0L);//jpush

    }
}
