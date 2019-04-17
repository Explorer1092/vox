package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.MongoCommandException;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.DocumentAccessException;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.mapper.ScoreCircleQueueCommand;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ReadingDubbingRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.service.ReadingReportService;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.ReadingDubbingRecommendDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.queue.NewHomeworkParentQueueProducer;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = ReadingReportService.class)
@ExposeService(interfaceClass = ReadingReportService.class)
public class ReadingReportServiceImpl extends SpringContainerSupport implements ReadingReportService {
    @Inject private ReadingDubbingRecommendDao readingDubbingRecommendDao;
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private NewHomeworkParentQueueProducer newHomeworkParentQueueProducer;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    public MapMessage submitReadingDubbingData(Teacher teacher, String hid, ObjectiveConfigType type, String pictureId, List<ReadingDubbingRecommend.ReadingDubbing> readingDubbings, String recommendComment) {
        if (StringUtils.isBlank(hid)) {
            return MapMessage.errorMessage("作业ID为空");
        }
        if (type == null) {
            return MapMessage.errorMessage("作业类型不存在");
        }
        if (StringUtils.isBlank(pictureId)) {
            return MapMessage.errorMessage("绘本ID错误");
        }
        if (CollectionUtils.isEmpty(readingDubbings)) {
            return MapMessage.errorMessage("推荐数据为空");
        }
        if (readingDubbings.size() > 5) {
            return MapMessage.errorMessage("推荐数据超过五条");
        }
        NewHomework newHomework = newHomeworkLoader.load(hid);
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (target == null) {
            return MapMessage.errorMessage("作业不包含该类型");
        }
        if (CollectionUtils.isEmpty(target.getApps())) {
            return MapMessage.errorMessage("作业不包含绘本");
        }
        ReadingDubbingRecommend.ID id = new ReadingDubbingRecommend.ID(hid, type, pictureId);
        ReadingDubbingRecommend dubbingRecommend = readingDubbingRecommendDao.load(id.toString());
        if (dubbingRecommend != null) {
            return MapMessage.errorMessage("绘本已经推荐");
        }
        dubbingRecommend = new ReadingDubbingRecommend();
        dubbingRecommend.setId(id.toString());
        dubbingRecommend.setPictureId(pictureId);
        dubbingRecommend.setType(type);
        dubbingRecommend.setReadingDubbings(readingDubbings);
        dubbingRecommend.setTeacherId(teacher.getId());
        dubbingRecommend.setRecommendComment(recommendComment);
        try {
            ReadingDubbingRecommend dubbingRecommend1 = readingDubbingRecommendDao.upsert(dubbingRecommend);
            if (dubbingRecommend1 == null) {
                return MapMessage.errorMessage("插入失败");
            }
        } catch (DocumentAccessException ex) {
            if (ex.getCause() != null && ex.getCause() instanceof MongoCommandException && ((MongoCommandException) ex.getCause()).getErrorCode() == 11000) {
                logger.error("Failed to upsert ReadingDubbingRecommend!Duplicate id:{}", id);
            } else {
                logger.error("Failed to upsert ReadingDubbingRecommend!id:{}", id, ex);
            }
        }
        String jztPath = "/view/pictureReading/recommendDetail?hid=" + hid + "&pictureId=" + pictureId;
        String studentPath = "/view/pictureReading/recommendStudent?hid="+hid + "&pictureId=" + pictureId;
        String voiceRecommendMsgContent = recommendComment;
        Set<String> studentName = new LinkedHashSet<>();
        dubbingRecommend.getReadingDubbings().forEach(recommendVoice -> studentName.add(recommendVoice.getUserName()));
        voiceRecommendMsgContent += "\n老师推荐以下同学的优秀配音：" + StringUtils.join(studentName, "，");


        //这里只是取发送人的ID
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
        Long teacherId = mainTeacherId == null ? teacher.getId() : mainTeacherId;
        //这里才是取所有的学科
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
        List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
        String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";
        String teacherName = teacher.fetchRealnameIfBlankId();
        String em_push_title = teacherName + subjectsStr + "老师：" + voiceRecommendMsgContent;


        //新的极光push
        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("studentId", "");
        jpushExtInfo.put("s", ParentAppPushType.VOICE_RECOMMEND.name());
        jpushExtInfo.put("url", jztPath);
        appMessageServiceClient.sendAppJpushMessageByTags(em_push_title,
                AppMessageSource.PARENT,
                Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(newHomework.getClazzGroupId()))),
                null,
                jpushExtInfo);

        //发往Parent provider
        ScoreCircleQueueCommand circleQueueCommand = new ScoreCircleQueueCommand();
        circleQueueCommand.setGroupId(newHomework.getClazzGroupId());
        circleQueueCommand.setCreateDate(new Date());
        circleQueueCommand.setGroupCircleType("VOICE_RECOMMEND");
        circleQueueCommand.setTypeId(dubbingRecommend.getId());
        circleQueueCommand.setImgUrl("");
        circleQueueCommand.setLinkUrl(jztPath);
        circleQueueCommand.setContent(voiceRecommendMsgContent);
        newHomeworkParentQueueProducer.getProducer().produce(Message.newMessage().writeObject(circleQueueCommand));


        //学生端消息中心
        List<AppMessage> appUserMessageDynamicList = dubbingRecommend.getReadingDubbings().stream()
                .map(e -> {
                    AppMessage appUserMessageDynamic = new AppMessage();
                    appUserMessageDynamic.setUserId(e.getUserId());
                    appUserMessageDynamic.setMessageType(StudentAppPushType.VOICE_RECOMMEND_REMIND.getType());
                    appUserMessageDynamic.setTitle("配音被推荐");
                    appUserMessageDynamic.setContent("恭喜！你的绘本配音完成的很优秀，被老师推荐了！要继续努力哦。");
                    //学生端和老师端、家长端的UI不一样。所以单独区分是否是学生打开
                    appUserMessageDynamic.setLinkUrl(studentPath + "&user_type=" + UserType.STUDENT.name());
                    appUserMessageDynamic.setLinkType(1);
                    return appUserMessageDynamic;
                })
                .collect(Collectors.toList());
        appUserMessageDynamicList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);

        //学生端push
        List<Long> userIdList = dubbingRecommend.getReadingDubbings()
                .stream()
                .map(ReadingDubbingRecommend.ReadingDubbing::getUserId)
                .collect(Collectors.toList());
        String link = studentPath + "&user_type=" + UserType.STUDENT.name();
        appMessageServiceClient.sendAppJpushMessageByIds("恭喜！你的绘本配音完成的很优秀，被老师推荐了！要继续努力哦。", AppMessageSource.STUDENT, userIdList,
                MapUtils.m("t", "h5", "key", "j", "link", link));


        return MapMessage.successMessage();
    }
}
