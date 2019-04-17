package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentService;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.cache.manager.UserDialogueTalkSceneResultCacheManager;
import com.voxlearning.utopia.service.ai.cache.manager.UserTaskTalkSceneResultCacheManager;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractContext;
import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractV2Context;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsNewTalkCollectContext;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultCollectContext;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishClassExtDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishClassPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishUserSignRecordDao;
import com.voxlearning.utopia.service.ai.impl.service.processor.talkinteracte.AIUserTalkInteractProcessor;
import com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult.ChipsQuestionResultProcessor;
import com.voxlearning.utopia.service.ai.impl.service.queue.AIUserQuestionResultCollectionQueueProducer;
import com.voxlearning.utopia.service.ai.impl.support.UserInfoSupport;
import com.voxlearning.utopia.service.ai.internal.ChipsVideoService;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;


@Named
@ExposeService(interfaceClass = ChipsEnglishContentService.class)
public class ChipsEnglishContentServiceImpl extends AbstractAiSupport implements ChipsEnglishContentService {

    @Inject
    private ChipsQuestionResultProcessor chipsQuestionResultProcessor;

    @Inject
    private AIUserTalkInteractProcessor aiUserTalkInteractProcessor;

    @Inject
    private ChipsEnglishUserSignRecordDao chipsEnglishUserSignRecordDao;

    @Inject
    private ChipsEnglishClassPersistence chipsEnglishClassPersistence;

    @Inject
    private ChipsEnglishClassExtDao chipsEnglishClassExtDao;

    @Inject
    private AIUserQuestionResultCollectionQueueProducer aiUserQuestionResultCollectionQueueProducer;

    @Inject
    private AICacheSystem aiCacheSystem;

    @Inject
    private AiChipsEnglishConfigServiceImpl aiChipsEnglishConfigService;

    @Inject
    private ChipsVideoService chipsUserVideoService;

    @Inject
    private ChipsEnglishContentLoaderImpl chipsEnglishContentLoader;

    private static String LOW_LEVEL_REGEX = "[e|f|E|F]\\S*";

    @Override
    public MapMessage processQuestionResult(Long userId, ChipsQuestionResultRequest chipsQuestionResultRequest) {
        if (userId == null || chipsQuestionResultRequest == null) {
            return MapMessage.errorMessage("参数异常");
        }
        ChipsQuestionResultContext context = chipsQuestionResultProcessor.process(new ChipsQuestionResultContext(chipsQuestionResultRequest, userId));
        if (context.isSuccessful()) {
            return context.getResult();
        } else {
            return MapMessage.errorMessage(context.getMessage());
        }
    }


    @Override
    public MapMessage processUserVideo(Long userId, String qid, String lessonId, String unitId, String bookId, List<String> videos) {
        if (userId == null || StringUtils.isBlank(lessonId) || CollectionUtils.isEmpty(videos)) {
            return MapMessage.errorMessage("参数异常").set("result", "400");
        }
        String sessionId = aiCacheSystem.getUserTalkFeedSessionCacheManager().getSessionId(userId, lessonId);
        if (StringUtils.isBlank(sessionId)) {
            sessionId = aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().getSessonId(userId, lessonId);
        }
        LessonType lessonType = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singleton(lessonId)))
                .map(map -> map.get(lessonId))
                .filter(stone -> stone != null)
                .map(StoneLessonData::newInstance)
                .map(StoneLessonData::getJsonData)
                .map(StoneLessonData.Lesson::getLesson_type)
                .orElse(LessonType.unknown);
        chipsUserVideoService.processSynthesisUserVideo(lessonId, lessonType, bookId, unitId, userId, sessionId, videos);
        return MapMessage.successMessage().set("result", "success");
    }


    @Override
    public MapMessage collectData(Long uid, ChipsQuestionType type, ChipsQuestionBO question, String input, String userVideo) {

        // Some question type not collect
        if (type == null || !type.isPersistentResult()) {
            return MapMessage.successMessage("warning: {} not collect anymore.", type);
        }

        ChipsQuestionResultCollectContext context = new ChipsQuestionResultCollectContext();
        context.setQuestionType(type);
        context.setUserId(uid);
        context.setBookId(question.getBookId());
        context.setLessonId(question.getLessonId());
        context.setUnitId(question.getUnitId());
        context.setQid(question.getQId());
        context.setInput(input);
        context.setUserVideo(userVideo);
        aiUserQuestionResultCollectionQueueProducer.processCollect(context);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage processInterlocutionLevel(Long uid, String bookId, String lessonId, String unitId, String qId, String input) {
        StoneQuestionData questionData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(qId)))
                .filter(MapUtils::isNotEmpty)
                .map(e -> e.get(qId))
                .map(StoneQuestionData::newInstance)
                .orElse(null);
        if (questionData == null) {
            return MapMessage.errorMessage("no question data");
        }
        Map<String, Object> resultMap = chipsContentService.fetchLevelResult(qId, input, uid);
        if (MapUtils.isEmpty(resultMap)) {
            return MapMessage.errorMessage("no result data");
        }
        StoneLessonData lessonData = chipsEnglishContentLoader.loadLessonById(lessonId);
        if (lessonData == null) {
            return MapMessage.errorMessage("no lesson data");
        }
        //记录对话数据
        String level = Optional.of(resultMap).map(e -> JsonUtils.toJson(e.get("data")))
                .map(JsonUtils::fromJson)
                .map(e -> SafeConverter.toString(e.get("level")))
                .orElse("-1");

        ChipsLessonRequest request = new ChipsLessonRequest();
        request.setBookId(bookId);
        request.setLessonId(lessonId);
        request.setLessonType(lessonData.getJsonData().getLesson_type());
        request.setUnitId(unitId);
        collectTalkData(uid, null, input, level, qId, questionData.getSchemaName(), request);

        MapMessage mapMessage = "success".equalsIgnoreCase(SafeConverter.toString(resultMap.get("result"))) ? MapMessage.successMessage() : MapMessage.errorMessage();
        mapMessage.putAll(resultMap);
        return mapMessage;
    }

    @Override
    public MapMessage processDialogueTalk(User user, String usercode, String input, String lessonId, String unitId, String bookId) {
        if (user == null || StringUtils.isAnyBlank(usercode, input)) {
            return MapMessage.errorMessage().add("result", "400").add("message", "参数异常");
        }
        AITalkLessonInteractV2Context context = new AITalkLessonInteractV2Context();
        context.setUser(user);
        context.setUsercode(usercode);
        context.setInput(input);
        context.setLessonId(lessonId);
        context.setType(LessonType.video_conversation);
        context.setBookId(bookId);
        context.setUnitId(unitId);
        context.setQuestionType(ChipsQuestionType.video_conversation);
        AITalkLessonInteractContext result = aiUserTalkInteractProcessor.process(context);
        return convertContext(result);
    }

    @Override
    public MapMessage processTaskTalk(User user, String usercode, String input, String roleName, String lessonId, String unitId, String bookId) {
        if (user == null || StringUtils.isAnyBlank(usercode, input, roleName)) {
            return MapMessage.errorMessage().add("result", "400").add("message", "参数异常");
        }

        AITalkLessonInteractV2Context context = new AITalkLessonInteractV2Context();
        context.setUser(user);
        context.setUsercode(usercode);
        context.setInput(input);
        context.setLessonId(lessonId);
        context.setType(LessonType.task_conversation);
        context.setRoleName(roleName);
        context.setUnitId(unitId);
        context.setBookId(bookId);
        context.setQuestionType(ChipsQuestionType.task_conversation);
        AITalkLessonInteractContext result = aiUserTalkInteractProcessor.process(context);
        return convertContext(result);
    }

    @Override
    public MapMessage processMockQAQuestionLevel(Long userId, ChipsQuestionBO question, String input, String userVideo) {
        StoneQuestionData questionData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(question.getQId())))
                .filter(MapUtils::isNotEmpty)
                .map(e -> e.get(question.getQId()))
                .map(StoneQuestionData::newInstance)
                .orElse(null);
        if (questionData == null) {
            return MapMessage.errorMessage("no question data");
        }

        Map<String, Object> resultMap = chipsContentService.fetchLevelResult(question.getQId(), input, userId);
        if (MapUtils.isEmpty(resultMap)) {
            return MapMessage.errorMessage("no result data");
        }
        StoneLessonData lessonData = chipsEnglishContentLoader.loadLessonById(question.getQId());
        if (lessonData == null) {
            return MapMessage.errorMessage("no lesson data");
        }
        //记录对话数据
        String level = Optional.of(resultMap).map(e -> JsonUtils.toJson(e.get("data")))
                .map(JsonUtils::fromJson)
                .map(e -> SafeConverter.toString(e.get("level")))
                .orElse("-1");

        ChipsLessonRequest request = new ChipsLessonRequest();
        request.setBookId(question.getBookId());
        request.setLessonId(question.getLessonId());
        request.setLessonType(lessonData.getJsonData().getLesson_type());
        request.setUnitId(question.getUnitId());
        collectTalkData(userId, userVideo, input, level, question.getQId(), questionData.getSchemaName(), request);

        MapMessage mapMessage = "success".equalsIgnoreCase(SafeConverter.toString(resultMap.get("result"))) ? MapMessage.successMessage() : MapMessage.errorMessage();
        mapMessage.putAll(resultMap);
        return mapMessage;
    }

    @Override
    public MapMessage recordUserShare(String bookId, String unitId, Long userId) {
        ChipsEnglishProductTimetable chipsEnglishProductTimetable = chipsUserService.loadTimetableByUserAndBook(userId, bookId);
        if (chipsEnglishProductTimetable == null || CollectionUtils.isEmpty(chipsEnglishProductTimetable.getCourses())) {
            // 直接不记录
            return MapMessage.errorMessage("暂无课表");
        }

        ChipsEnglishProductTimetable.Course course = chipsEnglishProductTimetable.getCourses().stream().filter(e -> e.getUnitId().equals(unitId)).findFirst().orElse(null);
        if (course == null || course.getBeginDate() == null) {
            // 直接不记录
            return MapMessage.errorMessage("暂无课表");
        }
        List<ChipsEnglishUserSignRecord> chipsEnglishUserSignRecords = chipsEnglishUserSignRecordDao.loadByUserId(userId);
        ChipsEnglishUserSignRecord record = chipsEnglishUserSignRecords.stream().filter(e -> unitId.equals(e.getUnitId())).findFirst().orElse(null);
        if (record != null) {
            return MapMessage.successMessage("已经打卡了");
        }

        record = new ChipsEnglishUserSignRecord();
        record.setBookId(bookId);
        record.setUnitId(unitId);
        record.setUserId(userId);

        Date now = new Date();
        record.setCreateTime(now);
        record.setCurrent(DateUtils.dateToString(now, "yyyy-MM-dd").equals(DateUtils.dateToString(course.getBeginDate(), "yyyy-MM-dd")));

        // 打卡
        chipsEnglishUserSignRecordDao.insert(record);

        // Book id list
        List<String> bookList=currentChipsEnglishBookIds();

        if (!bookList.contains(bookId) || !record.getCurrent()) {
            return MapMessage.successMessage();
        }

        ChipsEnglishPageContentConfig obj = aiChipsEnglishConfigService.loadChipsConfigByName(COUPON_SEND_UNIT_IDX_KEY);
        int unitIdx = SafeConverter.toInt(Optional.ofNullable(obj).map(ChipsEnglishPageContentConfig::getValue).orElse("0"));

        // 周六日不发,第五课后的不发
        boolean weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(LocalDate.now().getDayOfWeek());
        int courseIdx = chipsEnglishProductTimetable.getCourses().indexOf(course);
        if (weekend || courseIdx > unitIdx - 1) {
            return MapMessage.successMessage();
        }

        String subTitle = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(unitId))).map(e -> e.get(unitId))
                .map(StoneUnitData::newInstance)
                .map(StoneUnitData::getJsonData)
                .map(StoneUnitData.Unit::getSub_title)
                .orElse("");
        Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
        templateDataMap.put("first", new WechatTemplateData("恭喜您的宝贝打卡成功，获得10元课程优惠券", "#FF6551"));
        templateDataMap.put("keyword1", new WechatTemplateData("旅行口语课程day" + (courseIdx + 1) + "打卡", "#1BA9EF"));
        templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(new Date(), FORMAT_SQL_DATE), "#1BA9EF"));
        templateDataMap.put("remark", new WechatTemplateData("点击查看宝贝获得的奖励和打卡记录", "#FF6551"));
        try {
            wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(userId,
                    WechatTemplateMessageType.CHIPS_DAILY_SHARE_RECORD_REMIND.name(),
                    templateDataMap,
                    MapUtils.map("bookId", bookId));
        } catch (Exception e) {
            logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage upsertChipsEnglishProductTimetable(ChipsEnglishProductTimetable chipsEnglishProductTimetable) {
        chipsEnglishProductTimetableDao.upsert(chipsEnglishProductTimetable);
        chipsEnglishClassPersistence.loadByProductId(chipsEnglishProductTimetable.getId()).forEach(e -> {
            ChipsEnglishClassExt englishClassExt = new ChipsEnglishClassExt();
            englishClassExt.setId(e.getId());
            englishClassExt.setCreateTime(new Date());
            englishClassExt.setUpdateTime(new Date());
            englishClassExt.setCourses(chipsEnglishProductTimetable.getCourses());
            chipsEnglishClassExtDao.upsert(englishClassExt);
        });
        return MapMessage.successMessage();
    }


    @Override
    public MapMessage updateUserWxInfo(Long userId, String wxCode, String wxName) {
        if (StringUtils.isNotBlank(wxCode)) {
            chipsEnglishUserExtSplitDao.updateWx(userId, wxCode, wxName);
        }
        return MapMessage.successMessage();
    }


    private boolean isCourseStarted(String productId) {
        if (StringUtils.isBlank(productId)) {
            return true;
        }
        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(productId);

        return Optional.ofNullable(timetable).map(x -> x.getBeginDate().before(new Date())).orElse(true);
    }


    /**
     * <pre>
     * 薯条英语短期课第6期以及之后的报名短期课学员，即从11月21号之后报名的短期课学员在报名之后到开课前
     * </pre>
     *
     * @param userId
     * @return
     */
    @Override
    public boolean existUserWxInfo(Long userId, String productId) {
        if (null == userId) {
            return true;
        }
        if (isCourseStarted(productId)) {
            return true;
        }

        // 需要弹窗的产品
        String courseStartStr = Optional.ofNullable(aiChipsEnglishConfigService.loadChipsConfigByName("chips_wx_tips_limit")).map(ChipsEnglishPageContentConfig::getValue).orElse("6");
        int shortCourseStart = SafeConverter.toInt(courseStartStr);
        List<OrderProduct> allProductList = userOrderLoaderClient.loadAllOrderProductIncludeOffline();

        List<String> excludePids = new ArrayList<>();

        allProductList.forEach(x -> {
            //{rank:1,short:true}
            Map<String, Object> attr = Optional.ofNullable(x.getAttributes()).map(JsonUtils::fromJson).orElse(Collections.emptyMap());

            if (attr.size() > 0) {

                int rank = Optional.ofNullable(attr.get("rank")).map(SafeConverter::toInt).orElse(0);
                boolean isShort = Optional.ofNullable(attr.get("short")).map(SafeConverter::toBoolean).orElse(false);

                if (!isShort || rank < shortCourseStart) {
                    excludePids.add(x.getId());
                }
            }
        });

        if (excludePids.contains(productId)) {
            return true;
        }

        boolean added = false;
        ChipsEnglishUserExtSplit info = chipsEnglishUserExtSplitDao.load(userId);
        if (info != null) {
            added = StringUtils.length(info.getWxCode()) > 0;
        }
        return added;
    }


    /**
     * <pre>
     * 在2018年11月23号以及之后购买薯条英语正式课的学员
     * </pre>
     *
     * @param userId
     * @return
     */
    @Override
    public boolean existUserRecipientInfo(Long userId, String productId) {
        if (null == userId) {
            return true;
        }

        if (isCourseStarted(productId)) {
            return true;
        }

        // 不需要弹窗的产品课程课程数规则
        List<OrderProduct> allProductList = userOrderLoaderClient.loadAllOrderProductIncludeOffline();
        List<String> excludePids = new ArrayList<>();
        allProductList.forEach(x -> {
            //{rank:1,short:true}
            Map<String, Object> attr = Optional.ofNullable(x.getAttributes()).map(JsonUtils::fromJson).orElse(Collections.emptyMap());
            if (attr.size() > 0) {
                boolean isShort = Optional.ofNullable(attr.get("short")).map(SafeConverter::toBoolean).orElse(false);
                if (isShort) {
                    excludePids.add(x.getId());
                }
            }
        });

        if (excludePids.contains(productId)) {
            return true;
        }
        boolean added = false;
        ChipsEnglishUserExtSplit info = chipsEnglishUserExtSplitDao.load(userId);
        if (info != null) {
            added = StringUtils.length(info.getRecipientName()) > 0;
        }
        return added;
    }

    @Override
    public MapMessage updateUserRecipientInfo(Long userId, String recipientName, String recipientTel, String recipientAddr) {
        if (StringUtils.isNotBlank(recipientName)) {
            chipsEnglishUserExtSplitDao.updateRecipient(userId, recipientName, recipientTel, recipientAddr);
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage processDialogueTalkV2(Long user, String input, String qid, String userVideo, ChipsLessonRequest request) {
        if (user == null || StringUtils.isAnyBlank(qid, input)) {
            return MapMessage.errorMessage("param is null");
        }

        StoneSceneQuestionData questionData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(qid)))
                .filter(MapUtils::isNotEmpty)
                .map(e -> e.get(qid))
                .map(StoneSceneQuestionData::newInstance)
                .orElse(null);
        if (questionData == null) {
            return MapMessage.errorMessage("no question data");
        }

        StoneSceneQuestionData.Feedback feed = chipsContentService.processFeedback(input, questionData, user);

        collectTalkData(user, userVideo, input, feed.getLevel(), qid, questionData.getSchemaName(), request);

        String level = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getLevel).orElse("E");
        String cn = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getCn_translation).orElse("");
        String en = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getTranslation).orElse("");
        String video = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getVideo).orElse("");
        String tip = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getTip).orElse("");
        String roleImg = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getRole_image).orElse("");
        String feedback = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getFeedback).orElse("");
        String feedback_cover_pic = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getFeedback_cover_pic).orElse("");

        //记录对话流程
        recordDialogueTalk(user, request.getLessonId(), input, questionData, feed);

        return MapMessage.successMessage().set("level", level)
                .set("cn_translation", cn)
                .set("translation", en)
                .set("video", video)
                .set("tip", tip)
                .set("feedback_cover_pic", feedback_cover_pic)
                .set("status", !level.matches(LOW_LEVEL_REGEX))
                .set("role_image", roleImg)
                .set("feedback", feedback);
    }

    private void recordDialogueTalk(Long user, String lessonId, String input, StoneSceneQuestionData questionData, StoneSceneQuestionData.Feedback feed) {
        StoneLessonData lessonData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(lessonId)))
                .map(e -> e.get(lessonId))
                .map(StoneLessonData::newInstance)
                .filter(e -> e.getJsonData() != null && CollectionUtils.isNotEmpty(e.getJsonData().getContent_ids()))
                .orElse(null);
        if (lessonData == null) {
            return;
        }

        UserDialogueTalkSceneResultCacheManager cacheManager = aiCacheSystem.getUserDialogueTalkSceneResultCacheManager();
        String sessionId = cacheManager.getSessonId(user, lessonId);

        //判断第一个是不是过场
        boolean isFirst = lessonData.getJsonData().getContent_ids().get(0).equals(questionData.getId());
        if (!isFirst && lessonData.getJsonData().getContent_ids().size() > 1 &&
                lessonData.getJsonData().getContent_ids().get(1).equals(questionData.getId())) {
            StoneQuestionData firstQuestion = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(lessonData.getJsonData().getContent_ids().get(0))))
                    .map(e -> e.get(lessonData.getJsonData().getContent_ids().get(0)))
                    .map(StoneQuestionData::newInstance)
                    .orElse(null);
            if (firstQuestion == null) {
                return;
            }
            if (firstQuestion.getSchemaName() == ChipsQuestionType.scene_interlude) {
                TalkResultInfoContent content = new TalkResultInfoContent();
                content.setCn_translation(Optional.of(firstQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("cn_translation"))).orElse(""));
                content.setTranslation(Optional.of(firstQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("translation"))).orElse(""));
                content.setVideo(Optional.of(firstQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("video"))).orElse(""));
                content.setRole_image(Optional.of(firstQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("role_image"))).orElse(""));
                TalkResultInfoData infoData = new TalkResultInfoData();
                infoData.setContent(content);

                TalkResultInfo talkResultInfo = new TalkResultInfo();
                talkResultInfo.setData(Collections.singletonList(infoData));

                cacheManager.addRecordV2(user, lessonId, firstQuestion.getId(), talkResultInfo, "", null);
            }
        }

        TalkResultInfo question = null, feedback = null;
        if (questionData.getJsonData() != null && StringUtils.isNoneBlank(questionData.getJsonData().getCn_translation(),
                questionData.getJsonData().getTranslation(), questionData.getJsonData().getRole_image(), questionData.getJsonData().getVideo())) {
            TalkResultInfoContent content = new TalkResultInfoContent();
            content.setCn_translation(questionData.getJsonData().getCn_translation());
            content.setTranslation(questionData.getJsonData().getTranslation());
            content.setVideo(questionData.getJsonData().getVideo());
            content.setRole_image(questionData.getJsonData().getRole_image());
            TalkResultInfoData infoData = new TalkResultInfoData();
            infoData.setContent(content);
            question = new TalkResultInfo();
            question.setData(Collections.singletonList(infoData));
        }

        if (feed != null && StringUtils.isNoneBlank(feed.getCn_translation(), feed.getTranslation(), feed.getRole_image(), feed.getVideo())) {
            TalkResultInfoContent content = new TalkResultInfoContent();
            content.setCn_translation(feed.getCn_translation());
            content.setTranslation(feed.getTranslation());
            content.setVideo(feed.getVideo());
            content.setLevel(feed.getLevel());
            content.setRole_image(feed.getRole_image());

            TalkResultInfoData infoData = new TalkResultInfoData();
            infoData.setContent(content);
            feedback = new TalkResultInfo();
            feedback.setData(Collections.singletonList(infoData));
        }

        if (feed != null && StringUtils.isNotBlank(feed.getLevel()) &&
                !feed.getLevel().matches(LOW_LEVEL_REGEX) && CollectionUtils.isNotEmpty(questionData.getJsonData().getSentences())) {
            if (feedback == null || CollectionUtils.isEmpty(feedback.getData())) {
                feedback = new TalkResultInfo();
                TalkResultInfoData infoData = new TalkResultInfoData();
                feedback.setData(Arrays.asList(infoData));
            }

            TalkResultInfoKnowledge talkResultInfoKnowledge = new TalkResultInfoKnowledge();
            talkResultInfoKnowledge.setSentences(questionData.getJsonData().getSentences());
            feedback.getData().get(0).setKnowledge(talkResultInfoKnowledge);
        }


        cacheManager.addRecordV2(user, lessonId, questionData.getId(), question, input, feedback);

        //判断最后一个是不是过场
        boolean isEnd = lessonData.getJsonData().getContent_ids().get(lessonData.getJsonData().getContent_ids().size() - 1).equals(questionData.getId());
        StoneQuestionData endQuestion = null;
        if (!isEnd && lessonData.getJsonData().getContent_ids().size() > 1 &&
                lessonData.getJsonData().getContent_ids().get(lessonData.getJsonData().getContent_ids().size() - 2).equals(questionData.getId())) {
            String endQuestionId = lessonData.getJsonData().getContent_ids().get(lessonData.getJsonData().getContent_ids().size() - 1);
            endQuestion = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(endQuestionId)))
                    .map(e -> e.get(endQuestionId))
                    .map(StoneQuestionData::newInstance)
                    .orElse(null);
            if (endQuestion == null) {
                return;
            }
            if (endQuestion.getSchemaName() == ChipsQuestionType.scene_interlude) {
                TalkResultInfoContent endContent = new TalkResultInfoContent();
                endContent.setTranslation(Optional.of(endQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("translation"))).orElse(""));
                endContent.setCn_translation(Optional.of(endQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("cn_translation"))).orElse(""));

                endContent.setVideo(Optional.of(endQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("video"))).orElse(""));
                endContent.setRole_image(Optional.of(endQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("role_image"))).orElse(""));

                TalkResultInfoData endInfoData = new TalkResultInfoData();
                endInfoData.setContent(endContent);
                TalkResultInfo endResultInfo = new TalkResultInfo();
                endResultInfo.setData(Collections.singletonList(endInfoData));
                cacheManager.addRecordV2(user, lessonId, endQuestion.getId(), endResultInfo, "", null);

            }
        }

        //判断对话是否结束
        if (isEnd || (endQuestion != null && endQuestion.getSchemaName() == ChipsQuestionType.scene_interlude)) {
            List<AITalkSceneResult> aiTalkSceneResultList = cacheManager.loadRecord(user + sessionId, lessonId);
            if (CollectionUtils.isEmpty(aiTalkSceneResultList)) {
                return;
            }
            User us = userLoaderClient.loadUser(user);
            List<AITalkScene> talkSceneList = CourseRuleUtil.handleUserTalk(aiTalkSceneResultList, UserInfoSupport.getUserRoleImage(us), false);
            cacheManager.addTalkList(user.toString(), lessonId, talkSceneList);
        }

    }

    @Override
    public MapMessage processTaskTalkV2(Long user, String usercode, String input, String roleName,
                                        String qid, String lessonId, String unitId, String bookId) {
        StoneSceneQuestionData questionData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(qid)))
                .filter(MapUtils::isNotEmpty)
                .map(e -> e.get(qid))
                .map(StoneSceneQuestionData::newInstance)
                .orElse(null);
        if (questionData == null) {
            return MapMessage.errorMessage("no question data");
        }

        StoneSceneQuestionData.Feedback feed = chipsContentService.processFeedback(input, questionData, user);
        String level = Optional.ofNullable(feed).map(StoneSceneQuestionData.Feedback::getLevel).orElse("E");

        //记录对话数据
        ChipsLessonRequest request = new ChipsLessonRequest();
        request.setBookId(bookId);
        request.setLessonId(lessonId);
        request.setLessonType(LessonType.task_conversation);
        request.setUnitId(unitId);
        collectTalkData(user, null, input, level, qid, questionData.getSchemaName(), request);

        //根据level 返回相应的内容
        String cn = "", en = "", audio = "", tip = "", feedback = "";
        if (!level.equals("E")) {
            cn = Optional.ofNullable(feed).map(StoneSceneQuestionData.Feedback::getCn_translation).orElse("");
            en = Optional.ofNullable(feed).map(StoneSceneQuestionData.Feedback::getTranslation).orElse("");
            audio = Optional.ofNullable(feed).map(StoneSceneQuestionData.Feedback::getAudio).orElse("");
            tip = Optional.ofNullable(feed).map(StoneSceneQuestionData.Feedback::getTip).orElse("");
            feedback = Optional.ofNullable(feed).map(StoneSceneQuestionData.Feedback::getFeedback).orElse("");
        }

        //记录对话流程
        recordTaskTalk(usercode, input, roleName, lessonId, questionData, feed);
        return MapMessage.successMessage().set("level", level)
                .set("cn_translation", cn)
                .set("translation", en)
                .set("audio", audio)
                .set("status", !level.matches(LOW_LEVEL_REGEX))
                .set("tip", tip)
                .set("feedback", feedback);
    }

    private void recordTaskTalk(String usercode, String input, String roleName, String lessonId, StoneSceneQuestionData questionData, StoneSceneQuestionData.Feedback feed) {
        StoneTalkNpcQuestionData npcQuestionData = chipCourseSupport.fetchTaskNpcStoneData(lessonId, roleName);
        if (npcQuestionData == null || npcQuestionData.getJsonData() == null ||
                CollectionUtils.isEmpty(npcQuestionData.getJsonData().getContent_ids())) {
            return;
        }

        UserTaskTalkSceneResultCacheManager cacheManager = aiCacheSystem.getUserTaskTalkSceneResultCacheManager();
        //判断第一个是不是过场
        boolean isFirst = npcQuestionData.getJsonData().getContent_ids().get(0).equals(questionData.getId());
        if (!isFirst && npcQuestionData.getJsonData().getContent_ids().size() > 1
                && npcQuestionData.getJsonData().getContent_ids().get(1).equals(questionData.getId())) {
            String firstQId = npcQuestionData.getJsonData().getContent_ids().get(0);
            StoneQuestionData firstQuestion = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(firstQId)))
                    .map(e -> e.get(firstQId))
                    .map(StoneQuestionData::newInstance)
                    .orElse(null);
            if (firstQuestion == null) {
                return;
            }
            if (firstQuestion.getSchemaName() == ChipsQuestionType.task_interlude) {
                TalkResultInfoContent content = new TalkResultInfoContent();
                content.setCn_translation(Optional.of(firstQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("cn_translation"))).orElse(""));
                content.setTranslation(Optional.of(firstQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("translation"))).orElse(""));
                content.setAudio(Optional.of(firstQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("audio"))).orElse(""));
                content.setRole_image(Optional.of(firstQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("role_image"))).orElse(""));
                TalkResultInfoData infoData = new TalkResultInfoData();
                infoData.setContent(content);

                TalkResultInfo talkResultInfo = new TalkResultInfo();
                talkResultInfo.setData(Collections.singletonList(infoData));
                cacheManager.addRecordV2(usercode, lessonId, firstQId, roleName, talkResultInfo, null, null);
            }
        }

        TalkResultInfo question = null, feedback = null;
        if (questionData.getJsonData() != null && StringUtils.isNoneBlank(questionData.getJsonData().getCn_translation(),
                questionData.getJsonData().getTranslation(), questionData.getJsonData().getRole_image(), questionData.getJsonData().getAudio())) {
            TalkResultInfoContent content = new TalkResultInfoContent();
            content.setCn_translation(questionData.getJsonData().getCn_translation());
            content.setTranslation(questionData.getJsonData().getTranslation());
            content.setAudio(questionData.getJsonData().getAudio());
            content.setRole_image(questionData.getJsonData().getRole_image());
            TalkResultInfoData infoData = new TalkResultInfoData();
            infoData.setContent(content);
            question = new TalkResultInfo();
            question.setData(Collections.singletonList(infoData));
        }

        if (feed != null && StringUtils.isNoneBlank(feed.getCn_translation(), feed.getTranslation(), feed.getAudio())) {
            TalkResultInfoContent content = new TalkResultInfoContent();
            content.setCn_translation(feed.getCn_translation());
            content.setTranslation(feed.getTranslation());
            content.setAudio(feed.getAudio());
            content.setLevel(feed.getLevel());
            content.setRole_image(Optional.of(questionData)
                    .map(StoneSceneQuestionData::getJsonData)
                    .map(StoneSceneQuestionData.Topic::getRole_image)
                    .filter(StringUtils::isNotBlank)
                    .orElse(npcQuestionData.getJsonData().getRole_image()));

            TalkResultInfoData infoData = new TalkResultInfoData();
            infoData.setContent(content);
            feedback = new TalkResultInfo();
            feedback.setData(Collections.singletonList(infoData));
        }

        if (feed != null && StringUtils.isNotBlank(feed.getLevel()) &&
                !feed.getLevel().matches(LOW_LEVEL_REGEX) && CollectionUtils.isNotEmpty(questionData.getJsonData().getSentences())) {
            if (feedback == null || CollectionUtils.isEmpty(feedback.getData())) {
                TalkResultInfoData infoData = new TalkResultInfoData();
                feedback = new TalkResultInfo();
                feedback.setData(Arrays.asList(infoData));
            }
            TalkResultInfoKnowledge talkResultInfoKnowledge = new TalkResultInfoKnowledge();
            talkResultInfoKnowledge.setSentences(questionData.getJsonData().getSentences());
            feedback.getData().get(0).setKnowledge(talkResultInfoKnowledge);
        }

        cacheManager.addRecordV2(usercode, lessonId, questionData.getId(), roleName, question, input, feedback);

        //判断最后一个是不是过场
        boolean isEnd = npcQuestionData.getJsonData().getContent_ids().get(npcQuestionData.getJsonData().getContent_ids().size() - 1).equals(questionData.getId());
        if (!isEnd && npcQuestionData.getJsonData().getContent_ids().size() > 1 &&
                npcQuestionData.getJsonData().getContent_ids().get(npcQuestionData.getJsonData().getContent_ids().size() - 2).equals(questionData.getId())) {
            String endQuestionId = npcQuestionData.getJsonData().getContent_ids().get(npcQuestionData.getJsonData().getContent_ids().size() - 1);
            StoneQuestionData endQuestion = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(endQuestionId)))
                    .map(e -> e.get(endQuestionId))
                    .map(StoneQuestionData::newInstance)
                    .orElse(null);
            if (endQuestion == null) {
                return;
            }
            if (endQuestion.getSchemaName() == ChipsQuestionType.task_interlude) {
                TalkResultInfoContent endContent = new TalkResultInfoContent();
                endContent.setTranslation(Optional.of(endQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("translation"))).orElse(""));
                endContent.setCn_translation(Optional.of(endQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("cn_translation"))).orElse(""));
                endContent.setAudio(Optional.of(endQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("audio"))).orElse(""));
                endContent.setRole_image(Optional.of(endQuestion).map(StoneQuestionData::getJsonData).map(e -> SafeConverter.toString(e.get("role_image"))).orElse(""));

                TalkResultInfoData endInfoData = new TalkResultInfoData();
                endInfoData.setContent(endContent);

                TalkResultInfo endResultInfo = new TalkResultInfo();
                endResultInfo.setData(Collections.singletonList(endInfoData));

                cacheManager.addRecordV2(usercode, lessonId, endQuestion.getId(), roleName, endResultInfo, null, null);
            }
        }
    }



    private MapMessage convertContext(AITalkLessonInteractContext context) {
        MapMessage mapMessage;
        if (context.isSuccessful()) {
            mapMessage = MapMessage.successMessage();
            mapMessage.putAll(context.getResult());
        } else {
            mapMessage = MapMessage.errorMessage(context.getMessage());
        }
        return mapMessage;
    }

    private void collectTalkData(Long uid, String userVideo, String input, String level, String qId, ChipsQuestionType type, ChipsLessonRequest chipsLessonRequest) {
        ChipsNewTalkCollectContext context = new ChipsNewTalkCollectContext();
        context.setQuestionType(type);
        context.setUserId(uid);
        context.setBookId(chipsLessonRequest.getBookId());
        context.setLessonId(chipsLessonRequest.getLessonId());
        context.setUnitId(chipsLessonRequest.getUnitId());
        context.setQid(qId);
        context.setInput(input);
        context.setLevel(level);
        context.setLessonType(chipsLessonRequest.getLessonType());
        context.setUserVideo(userVideo);
        aiUserQuestionResultCollectionQueueProducer.processCollect(context);
    }
}
