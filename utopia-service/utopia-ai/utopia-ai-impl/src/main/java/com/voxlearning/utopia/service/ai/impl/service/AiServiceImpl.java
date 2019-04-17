package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.AiService;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsClassCountCacheManager;
import com.voxlearning.utopia.service.ai.cache.manager.UserPageVisitCacheManager;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractContext;
import com.voxlearning.utopia.service.ai.context.AIUserPerQuestionContext;
import com.voxlearning.utopia.service.ai.context.AIUserQuestionContext;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserUnitResultHistoryDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipEnglishInvitationPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishUserSignRecordDao;
import com.voxlearning.utopia.service.ai.impl.service.processor.talkinteracte.AIUserTalkInteractProcessor;
import com.voxlearning.utopia.service.ai.impl.support.ConstantSupport;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

/**
 * Created by Summer on 2018/3/27
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = AiService.class, version = @ServiceVersion(version = "20181115")),
        @ExposeService(interfaceClass = AiService.class, version = @ServiceVersion(version = "20190122"))
})
public class AiServiceImpl extends AbstractAiSupport implements AiService {

    public static String TEMP_QUESTION_ID_SEP = ".";

    @AlpsQueueProducer(queue = "utopia.ai.user.video.synthesis.queue", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer userVideoSynthesisQueue;

    @Inject
    private ChipEnglishInvitationPersistence chipEnglishInvitationPersistence;

    @Inject
    private AIUserTalkInteractProcessor aiUserTalkInteractProcessor;

    @Inject
    private AICacheSystem aiCacheSystem;

    @Inject
    private AIUserUnitResultHistoryDao aiUserUnitResultHistoryDao;

    @Inject
    private ChipsEnglishUserSignRecordDao chipsEnglishUserSignRecordDao;


    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private ChipsClassCountCacheManager chipsClassCountCacheManager;
    @Inject
    private ChipsUserVideoServiceImpl chipsUserVideoService;

    @AlpsQueueProducer(queue = "utopia.ai.user.video.handle.queue", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer userVideoHandleQueue;

    @Inject
    private UserPageVisitCacheManager userPageVisitCacheManager;

    @Override
    public MapMessage processAIQuestionResult(AIUserQuestionContext context) {
        aiUserQuestionResultProcessor.process(context);
        if (context.isSuccessful()) {
            return context.getResult();
        } else {
            return MapMessage.errorMessage(context.getMessage());
        }
    }

    @Override
    public MapMessage collectWarmUpResult(User user, String input, String lessonId, String unitId, String qId) {

        AIUserPerQuestionContext context = new AIUserPerQuestionContext();
        context.setUser(user);
        context.setInput(input);
        context.setLessonId(lessonId);
        context.setUnitId(unitId);
        context.setQid(qId);
        context.setType(LessonType.WarmUp);
        aiUserQuestionResultCollectionQueueProducer.processCollect(context);
        return MapMessage.successMessage();
    }

    @Override
    @Deprecated
    public MapMessage saveAIVideoResult(Long userId, String qid, String video) {
        if (userId == null || StringUtils.isBlank(qid) || StringUtils.isBlank(video)) {
            return MapMessage.errorMessage("请求参数异常").set("result", "400");
        }

        AIUserQuestionResultHistory questionHis = aiUserQuestionResultHistoryDao.loadByUidAndQid(userId, qid);
        if (questionHis == null) {
            return MapMessage.errorMessage("题目数据不存在").set("result", "400");
        }

        AIUserLessonResultHistory lessonResultHistory = aiUserLessonResultHistoryDao.load(userId, questionHis.getLessonId());
        if (lessonResultHistory == null) {
            return MapMessage.errorMessage("课程数据不存在").set("result", "400");
        }

        String userName = Optional.ofNullable(userLoaderClient.loadUser(userId))
                .filter(e -> e.getProfile() != null && StringUtils.isNotBlank(e.getProfile().getNickName()))
                .map(User::getProfile)
                .map(UserProfile::getNickName)
                .orElse("");
        String lessonName = Optional.ofNullable(newContentLoaderClient.loadBookCatalogByCatalogId(questionHis.getUnitId()))
                .filter(e -> MapUtils.isNotEmpty(e.getExtras()))
                .filter(e -> e.getExtras().get("ai_teacher") != null)
                .map(e -> JsonUtils.fromJson(SafeConverter.toString(e.getExtras().get("ai_teacher"))))
                .filter(e -> MapUtils.isNotEmpty(e) && e.get("pageSubTitle") != null)
                .map(e -> SafeConverter.toString(e.get("pageSubTitle")))
                .orElse("");
        String bookId = Optional.ofNullable(newContentLoaderClient.loadBookCatalogByCatalogId(lessonResultHistory.getUnitId())).map(NewBookCatalog::bookId).orElse("");
        AIUserVideo aiUserVideo = AIUserVideo.newInstance(questionHis.getUserId(), userName, bookId, questionHis.getUnitId(), lessonName);
        aiUserVideo.setVideo(video);
        aiUserVideoDao.upsert(aiUserVideo);

        AIUserUnitResultHistory aiUserUnitResultHistory = aiUserUnitResultHistoryDao.load(userId, questionHis.getUnitId());
        if (aiUserUnitResultHistory != null) {
            aiUserUnitResultHistory.setVideo(video);
            aiUserUnitResultHistory.setUserVideoId(aiUserVideo.getId());
            aiUserUnitResultHistory.setUpdateDate(new Date());
            aiUserUnitResultHistoryDao.upsert(aiUserUnitResultHistory);
        }
        lessonResultHistory.setUserVideo(video);
        lessonResultHistory.setUpdateDate(new Date());
        lessonResultHistory.setUserVideoId(aiUserVideo.getId());
        aiUserLessonResultHistoryDao.upsert(lessonResultHistory);

        aiUserQuestionResultHistoryDao.updateUserVideo(questionHis, video);
        return MapMessage.successMessage().set("result", "success");
    }

    @Override
    public MapMessage handleUserVideo(User user, String lessonId, List<String> videos) {
        if (user == null || StringUtils.isBlank(lessonId) || CollectionUtils.isEmpty(videos)) {
            return MapMessage.errorMessage("参数异常").set("result", "400");
        }

        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
        String unitId = lesson.unitId();
        String sessionId = aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().getSessonId(user.getId(), lessonId);
        AIUserVideo aiUserVideo = new AIUserVideo();
        aiUserVideo.setDisabled(true);
        aiUserVideo.setLessonId(lessonId);
        aiUserVideo.setBookId(lesson.bookId());
        aiUserVideo.setUnitId(unitId);
        aiUserVideo.setId(user.getId() + sessionId);
        aiUserVideo.setOriginalVideos(videos);
        aiUserVideo.setUserId(user.getId());
        aiUserVideo.setCreateTime(new Date());
        aiUserVideo.setUpdateTime(new Date());
        aiUserVideoDao.upsert(aiUserVideo);

        AIUserLessonResultHistory lessonResultHistory = aiUserLessonResultHistoryDao.loadById(sessionId);
        if (lessonResultHistory == null) {
            AIUserLessonResultHistory oldRes = aiUserLessonResultHistoryDao.load(user.getId(), lessonId);
            if (oldRes != null) {
                aiUserLessonResultHistoryDao.disableOld(user.getId(), oldRes.getLessonId());
                lessonResultHistory = oldRes;
            } else {
                lessonResultHistory = new AIUserLessonResultHistory();
                lessonResultHistory.setDisabled(false);
                lessonResultHistory.setFinished(false);
            }
            lessonResultHistory.setCreateDate(new Date());
            lessonResultHistory.setId(sessionId);
            lessonResultHistory.setLessonId(lessonId);
            lessonResultHistory.setUnitId(lessonId);
            lessonResultHistory.setLessonType(LessonType.video_conversation);
            lessonResultHistory.setUserId(user.getId());
        }
        lessonResultHistory.setUserVideo("#");
        lessonResultHistory.setUserVideoId("");
        lessonResultHistory.setUpdateDate(new Date());
        aiUserLessonResultHistoryDao.upsert(lessonResultHistory);

        Map<String, Object> message = new HashMap<>();
        String id = user.getId() + TEMP_QUESTION_ID_SEP + lessonId + TEMP_QUESTION_ID_SEP + sessionId;
        message.put("ID", id);
        message.put("V", videos);
        message.put("WATER", "yes");
        userVideoSynthesisQueue.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        if (RuntimeMode.lt(Mode.STAGING)) {
            logger.info("handleUserVideo, usertoken:{}, qid: {}, id:{}, videos:{}", user.getId(), lessonId, id, JsonUtils.toJson(videos));
        }
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", user.getId(),
                "mod1", lessonId,
                "mod2", id,
                "mod3", JsonUtils.toJson(videos),
                "op", "ai user video to synthesis"
        ));
        return MapMessage.successMessage().set("result", "success");
    }

    @Override
    public MapMessage saveInvitation(Long inviter, Long invitee) {
        Set<Long> invitees = chipEnglishInvitationPersistence.loadByInviterId(inviter).stream().map(ChipEnglishInvitation::getInvitee).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(invitees) && invitees.contains(invitee)) {
            return MapMessage.successMessage();
        }
        ChipEnglishInvitation chipEnglishInvitation = new ChipEnglishInvitation();
        chipEnglishInvitation.setInviter(inviter);
        chipEnglishInvitation.setInvitee(invitee);
        chipEnglishInvitation.setDisabled(false);
        chipEnglishInvitation.setUpdateTime(new Date());
        chipEnglishInvitation.setCreateTime(new Date());
        chipEnglishInvitationPersistence.insert(chipEnglishInvitation);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage loadAndRecordDialogueTalk(User user, String usercode, String input, String lessonId) {
        if (user == null || StringUtils.isAnyBlank(usercode, input)) {
            return MapMessage.errorMessage().add("result", "400").add("message", "参数异常");
        }
        AITalkLessonInteractContext context = new AITalkLessonInteractContext();
        context.setUser(user);
        context.setUsercode(usercode);
        context.setInput(input);
        context.setLessonId(lessonId);
        context.setType(LessonType.Dialogue);
        context = aiUserTalkInteractProcessor.process(context);
        return convertContext(context);
    }

    @Override
    public MapMessage loadAndRecordTaskTalk(User user, String usercode, String input, String roleName, String lessonId) {
        if (user == null || StringUtils.isAnyBlank(usercode, input, roleName)) {
            return MapMessage.errorMessage().add("result", "400").add("message", "参数异常");
        }

        AITalkLessonInteractContext context = new AITalkLessonInteractContext();
        context.setUser(user);
        context.setUsercode(usercode);
        context.setInput(input);
        context.setLessonId(lessonId);
        context.setType(LessonType.Task);
        context.setRoleName(roleName);
        context = aiUserTalkInteractProcessor.process(context);
        return convertContext(context);
    }

    @Override
    public MapMessage updateUserVideoStatus(String id, AIUserVideo.ExamineStatus from, AIUserVideo.ExamineStatus to, String updater, AIUserVideo.Category category, String description) {
        if (StringUtils.isAnyBlank(id, updater) || from == null || to == null) {
            return MapMessage.errorMessage("param error");
        }
        aiUserVideoDao.updateExamineStatus(id, from, to, updater, category, description);
        AIUserVideo video = aiUserVideoDao.load(id);
        //对一对一点评视频进行审核
        if (video != null && video.getForRemark() != null && video.getForRemark() && video.getStatus() == AIUserVideo.ExamineStatus.Passed) {
            return chipsUserVideoService.examineVideo(video);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateUserVideoComment(String id, String comment, String commentAudio, List<AIUserVideo.Label> labels, String updater, AIUserVideo.Category category) {
        if (StringUtils.isAnyBlank(id, updater)) {
            return MapMessage.errorMessage("param error");
        }
        aiUserVideoDao.updateContent(id, comment, commentAudio, labels, updater, category);
        return MapMessage.successMessage();
    }

    private MapMessage convertContext(AITalkLessonInteractContext context) {
        MapMessage mapMessage;
        if (context.isSuccessful()) {
            mapMessage = MapMessage.successMessage().add("result", "success");
            mapMessage.putAll(context.getResult());
        } else {
            mapMessage = MapMessage.errorMessage();
            mapMessage.put("message", context.getMessage());
            mapMessage.put("result", "400");
        }
        return mapMessage;
    }


    @Deprecated
    @Override
    public MapMessage recordUserShare(String unitId, User user) {
        // 对比今日课程是否与打卡单元匹配
        //fixme bookId
        Date begin = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), user.getId()).stream()
                .filter(e -> {
                    Map<String, Object> map = JsonUtils.fromJson(e.getProductAttributes());
                    if (MapUtils.isEmpty(map)) {
                        return true;
                    }
                    int grade = SafeConverter.toInt(map.get("grade"));
                    if (grade > 0) {
                        return false;
                    }
                    return true;
                }).findFirst()
                .map(e -> chipsEnglishProductTimetableDao.load(e.getProductId()))
                .map(ChipsEnglishProductTimetable::getBeginDate)
                .orElse(null);
        if (begin == null) {
            return MapMessage.successMessage();
        }
        NewBookCatalog todayUnit = chipCourseSupport.fetchDayUnit(new Date(), true, begin, chipCourseSupport.TRAVEL_ENGLISH_BOOK_ID);
        if (todayUnit == null || !StringUtils.equals(todayUnit.getId(), unitId)) {
            // 直接不记录
            return MapMessage.successMessage();
        }

        if (todayUnit.getRank().compareTo(8) > 0) {
            return MapMessage.successMessage();
        }

        ChipsEnglishUserSignRecord record = chipsEnglishUserSignRecordDao.loadByUserId(user.getId()).stream().filter(e -> unitId.equals(e.getUnitId())).findFirst().orElse(null);
        if (record != null) {
            return MapMessage.successMessage();
        }
        Date now = new Date();
        record = new ChipsEnglishUserSignRecord();
        record.setBookId(todayUnit.bookId());
        record.setCreateTime(now);
        record.setUnitId(unitId);
        record.setUserId(user.getId());
        chipsEnglishUserSignRecordDao.insert(record);

        String config = pageBlockContentServiceClient.getPageBlockContentBuffer().findByPageName("chipsEnglishUnitCouponConfig")
                .stream()
                .filter(e -> e.getDisabled() == null || !e.getDisabled())
                .filter(p -> StringUtils.isNotBlank(p.getContent()))
                .findFirst()
                .map(PageBlockContent::getContent)
                .orElse("");
        boolean send = true;
        if (StringUtils.isNotBlank(config)) {
            NewBookCatalog configUnit = newContentLoaderClient.loadBookCatalogByCatalogId(config);
            if (configUnit != null && configUnit.getRank().compareTo(todayUnit.getRank()) < 0) {
                send = false;
            }
        }
        if (!send) {
            return MapMessage.successMessage();
        }
        //发送模板消息提醒用户打卡成功
        String subTitle = "";
        String jsonStr = SafeConverter.toString(todayUnit.getExtras().get(AiTeacher));
        if (StringUtils.isNotBlank(jsonStr)) {
            Map<String, Object> jsonMap = JsonUtils.fromJson(jsonStr);
            if (MapUtils.isNotEmpty(jsonMap)) {
                subTitle = SafeConverter.toString(jsonMap.get("pageSubTitle"));
            }
        }
        Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
        templateDataMap.put("first", new WechatTemplateData("恭喜您的宝贝打卡成功，获得10元课程优惠券", "#FF6551"));
        templateDataMap.put("keyword1", new WechatTemplateData("旅行口语课程" + subTitle + "打卡", "#1BA9EF"));
        templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(new Date(), FORMAT_SQL_DATE), "#1BA9EF"));
        templateDataMap.put("remark", new WechatTemplateData("点击查看宝贝获得的奖励和打卡记录", "#FF6551"));
        try {
            wechatServiceClient.getWechatService()
                    .processWechatTemplateMessageNotice(user.getId(),
                            WechatTemplateMessageType.CHIPS_DAILY_SHARE_RECORD_REMIND.name(),
                            templateDataMap,
                            MapUtils.map("bookId", todayUnit.bookId()));
        } catch (Exception e) {
            logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", user.getId(), templateDataMap, e);
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage recordUserShareDoor(String unitId, String bookId, User user) {
        ChipsEnglishUserSignRecord record = chipsEnglishUserSignRecordDao.loadByUserId(user.getId()).stream().filter(e -> unitId.equals(e.getUnitId())).findFirst().orElse(null);
        if (record != null) {
            return MapMessage.successMessage("打卡记录已存在");
        }
        Date now = new Date();
        record = new ChipsEnglishUserSignRecord();
        record.setBookId(bookId);
        record.setCreateTime(now);
        record.setUnitId(unitId);
        record.setUserId(user.getId());
        chipsEnglishUserSignRecordDao.insert(record);

        return MapMessage.successMessage("加进去了，去查询一下看看吧。");
    }

    @Override
    public MapMessage fixClazzUserCount(Long clazzId, Integer number) {
        if (clazzId == null || number == null) {
            return MapMessage.errorMessage("参数为空");
        }

        Long val = 0L;
        for(int i = 0; i < number; i++) {
            val = chipsClassCountCacheManager.increase(clazzId);
        }

        return MapMessage.successMessage().add("val", val);
    }

    @Override
    public MapMessage fixUserVideo(String message) {
        userVideoHandleQueue.produce(Message.newMessage().withPlainTextBody(message));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage fixUserDrawingTask(List<Long> drawingTaskIds, Long userId) {
        for (Long taskId : drawingTaskIds) {
            userPageVisitCacheManager.addRecord(taskId, ConstantSupport.DRAWING_TASK_FINISH_CACHE_KEY + userId);
        }
        return MapMessage.successMessage();
    }
}
