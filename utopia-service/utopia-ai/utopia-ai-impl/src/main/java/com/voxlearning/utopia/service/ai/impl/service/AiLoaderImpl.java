package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.AiLoader;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsShareVideoRankCacheManager;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.context.AIUserDailyClassContext;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.exception.ProductNotExitException;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.ai.impl.service.processor.dailyclass.AIUserDailyClassResultProcessor;
import com.voxlearning.utopia.service.ai.impl.support.ChipCourseSupport;
import com.voxlearning.utopia.service.ai.impl.support.UserInfoSupport;
import com.voxlearning.utopia.service.ai.impl.support.WechatConfig;
import com.voxlearning.utopia.service.ai.support.MessageConfig;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2018/3/27
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = AiLoader.class, version = @ServiceVersion(version = "20181115")),
        @ExposeService(interfaceClass = AiLoader.class, version = @ServiceVersion(version = "20181024"))
})
public class AiLoaderImpl extends AbstractAiSupport implements AiLoader {

    @Inject
    private AIUserDailyClassResultProcessor aiUserDailyClassResultProcessor;
    @Inject
    private ChipEnglishInvitationPersistence chipEnglishInvitationPersistence;
    @Inject
    private ChipsShareVideoRankCacheManager chipsShareVideoRankCacheManager;

    @Inject
    private AICacheSystem aICacheSystem;

    @Inject
    private ChipsEnglishUserExtSplitDao chipsEnglishUserExtSplitDao;

    @Inject
    private ChipsEnglishUserSignRecordDao chipsEnglishUserSignRecordDao;
    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;
    @Inject
    private AiChipsEnglishConfigServiceImpl chipsEnglishConfigService;
    @Inject
    private AiChipsEnglishConfigServiceImpl aiChipsEnglishConfigService;
    @Inject
    private ChipsUserCoursePersistence chipsUserCoursePersistence;

    @Override
    public MapMessage loadDailyClass(User user, String unitId) {
        AIUserDailyClassContext context = new AIUserDailyClassContext();
        context.setUser(user);
        context.setUnitId(unitId);
        context = aiUserDailyClassResultProcessor.process(context);
        if (context.isSuccessful()) {
            MapMessage message = MapMessage.successMessage();
            if (MapUtils.isNotEmpty(context.getExtMap())) {
                message.putAll(context.getExtMap());
            }
            if (context.getClassInfo() != null) {
                message.add("classInfo", context.getClassInfo());
            }
            if (context.getStatus() != null) {
                message.add("classStatus", context.getStatus().name());
            }
            message.add("className", context.getClassName());
            return message;
        } else {
            return MapMessage.errorMessage(context.getMessage());
        }
    }

    @Override
    public MapMessage loadClassDetail(User user, String unitId) {
        NewBookCatalog unitCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unitCatalog == null) {
            return MapMessage.errorMessage("no unit find!");
        }
        // 获取lesson信息
        List<NewBookCatalog> lessons = newContentLoaderClient.loadChildrenSingle(unitId, BookCatalogType.LESSON);
        if (CollectionUtils.isEmpty(lessons)) {
            return MapMessage.errorMessage("no lesson!");
        }
        List<AIUserLessonResultHistory> lessonResults = aiUserLessonResultHistoryDao.loadByUserIdAndUnitId(user.getId(), unitId);

        List<AILessonInfo> lessonInfos = new ArrayList<>();
        lessons.sort(Comparator.comparing(NewBookCatalog::getRank));
        Boolean isLock = false;
        for (NewBookCatalog lesson : lessons) {
            AILessonInfo lessonInfo = new AILessonInfo();
            lessonInfo.setId(lesson.getId());
            lessonInfo.setName(lesson.getName());
            lessonInfo.setRank(lesson.getRank());
            LessonType lessonType = getLessonType(lesson.getName());
            if (lessonType == null) {
                logger.error("no lessonType find, lessonName {}", lesson.getName());
                continue;
            }
            lessonInfo.setLessonType(getLessonType(lesson.getName()));
            AIUserLessonResultHistory resultHistory = lessonResults.stream().filter(l -> StringUtils.equals(l.getLessonId(), lesson.getId()))
                    .filter(AIUserLessonResultHistory::getFinished)
                    .findFirst().orElse(null);
            Boolean finished = resultHistory != null;
            lessonInfo.setFinished(finished);
            lessonInfo.setIsLock(isLock);
            lessonInfo.setStar(resultHistory == null ? 0 : resultHistory.getStar());
            lessonInfo.setUnitId(unitId);
            lessonInfos.add(lessonInfo);
            if (!finished) isLock = true;
        }
        Map<String, Object> map = JsonUtils.fromJson(SafeConverter.toString(unitCatalog.getExtras().get("ai_teacher")));
        // 产品自己定义了N多的名字，有些还存在了extra里面 很坑！！
        return MapMessage.successMessage()
                .add("trial", chipsContentService.isTrailUnit(unitId))
                .add("lessons", lessonInfos)
                .add("unitCname", MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("pageTitle"), "") : "")
                .add("unitName", MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("pageSubTitle"), "") : "")
                .add("goals", Collections.singletonList(MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("goalOfStudy"), "")
                        .replace("\\u0a0", " ")
                        .replace("\\U0a0", " ")
                        .replace("　", " ")
                        .replace(" ", " ") : ""))
                .add("goalAudio", MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("goalOfStudyAudioUrl"), "") : "")
                .add("unitId", unitId);
    }

    @Override
    public MapMessage loadQuestions(User user, String lessonId) {
        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
        if (lesson == null) {
            return MapMessage.errorMessage("lesson is null").set("result", "400");
        }
        List<AIQuestion> aiQuestions = new ArrayList<>();
        Map<String, List<AIUserQuestionResultHistory>> questionResultMap = aiUserQuestionResultHistoryDao.loadByUidAndLessonId(user.getId(), lessonId).stream()
                .collect(Collectors.groupingBy(AIUserQuestionResultHistory::getQid));
        List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionsByLessonIds(Collections.singletonList(lessonId), Subject.ENGLISH.getId());
        for (NewQuestion newQuestion : newQuestionList) {
            if (newQuestion.getContent() == null || CollectionUtils.isEmpty(newQuestion.getContent().getSubContents())) {
                continue;
            }
            if (MapUtils.isEmpty(newQuestion.getContent().getSubContents().get(0).getExtras())) {
                continue;
            }
            String content = newQuestion.getContent().getSubContents().get(0).getExtras().get("ai_teacher");
            if (StringUtils.isBlank(content)) {
                continue;
            }
            AIQuestion aiQuestion = JsonUtils.fromJson(content, AIQuestion.class);
            if (aiQuestion == null || aiQuestion.getType() == null) {
                continue;
            }
            aiQuestion.setId(newQuestion.getId());
            aiQuestion.setFinished(MapUtils.isNotEmpty(questionResultMap) && CollectionUtils.isNotEmpty(questionResultMap.get(newQuestion.getId())));
            aiQuestions.add(aiQuestion);
        }
        Map<String, Object> map = JsonUtils.fromJson(SafeConverter.toString(lesson.getExtras().get("ai_teacher")));
        return MapMessage.successMessage()
                .set("result", "success")
                .set("questions", aiQuestions.stream().collect(Collectors.groupingBy(AIQuestion::getType)))
                .set("title", MapUtils.isNotEmpty(map) && map.get("title") != null ? map.get("title") : "")
                .set("unitId", lesson.unitId())
                .set("bookId", lesson.bookId())
                .set("lessonId", lessonId)
                .set("background", MapUtils.isNotEmpty(map) && map.get("backgroundIntro") != null ? map.get("backgroundIntro") : "")
                .set("backgroundAudio", MapUtils.isNotEmpty(map) && map.get("backgroundAudioUrl") != null ? map.get("backgroundAudioUrl") : "")
                .set("goal", MapUtils.isNotEmpty(map) && map.get("goalIntro") != null ? map.get("goalIntro") : "")
                .set("goalAudio", MapUtils.isNotEmpty(map) && map.get("goalAudioUrl") != null ? map.get("goalAudioUrl") : "")
                .set("image", MapUtils.isNotEmpty(map) && map.get("image") != null ? map.get("image") : "http://cdn.17zuoye.com/fs-resource/5ad99cfdd700a04137a7f19d.png")
                .set("subtitle", MapUtils.isNotEmpty(map) && map.get("subTitle") != null ? map.get("subTitle") : "")
                .set("rewards", Collections.emptyList())
                .set("summaries", MapUtils.isNotEmpty(map) && map.get("summaries") != null ? map.get("summaries") : Collections.emptyList());
    }

    @Override
    public MapMessage loadUnitResult(Long userId, String unitId) {

        AIUserUnitResultHistory unitResultHistory = aiUserUnitResultHistoryDao.load(userId, unitId);
        int total = 1;
        if (unitResultHistory != null) {
            // 获取情景对话视频地址
            List<AIUserLessonResultHistory> lessonResultHistoryList = aiUserLessonResultHistoryDao.loadByUserIdAndUnitId(userId, unitId);
            if (CollectionUtils.isNotEmpty(lessonResultHistoryList) && StringUtils.isBlank(unitResultHistory.getVideo())) {
                lessonResultHistoryList.stream().filter(f -> f.getLessonType() == LessonType.Dialogue || f.getLessonType() == LessonType.video_conversation)
                        .findFirst().ifPresent(lessonResultHistory -> {
                    unitResultHistory.setVideo(lessonResultHistory.getUserVideo());
                    unitResultHistory.setUserVideoId(lessonResultHistory.getUserVideoId());
                });
            }
            total = aiUserUnitResultHistoryDao.loadByUserId(userId).stream()
                    .filter(e -> StringUtils.isNotBlank(e.getBookId()))
                    .filter(e -> e.getBookId().equals(unitResultHistory.getBookId()))
                    .collect(Collectors.toList()).size();
            if (CollectionUtils.isNotEmpty(unitResultHistory.getWeekPoints())) {
                Set<String> words = new HashSet<>();
                List<QuestionWeekPoint> weekPoints = new ArrayList<>();
                for (QuestionWeekPoint weekPoint : unitResultHistory.getWeekPoints()) {
                    weekPoint.setSuggestUrl(converSuggestUrl(weekPoint.getSuggestUrl()));
                    if (words.contains(weekPoint.getOriginal())) {
                        continue;
                    }
                    weekPoints.add(weekPoint);
                    words.add(weekPoint.getOriginal());
                }
                unitResultHistory.setWeekPoints(weekPoints);
            }
            unitResultHistory.setPronunciation((unitResultHistory.getPronunciation() != null && unitResultHistory.getPronunciation() <= 8) ? (new BigDecimal(unitResultHistory.getPronunciation()).divide(new BigDecimal(8), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue()) : unitResultHistory.getPronunciation());
        }
        List<AIUserQuestionResultHistory> questionResultHistories = aiUserQuestionResultHistoryDao.loadByUidAndUnitId(userId, unitId);
        String taskPlay = Optional.ofNullable(questionResultHistories)
                .map(e -> e.stream().filter(e1 -> e1.getLessonType() == LessonType.Task || e1.getLessonType() == LessonType.task_conversation).findFirst().orElse(null))
                .map(e -> questionLoaderClient.loadQuestion(e.getQid()))
                .filter(e -> e.getContent() != null && CollectionUtils.isNotEmpty(e.getContent().getSubContents()) && MapUtils.isNotEmpty(e.getContent().getSubContents().get(0).getExtras()))
                .map(e -> e.getContent().getSubContents().get(0).getExtras().get("ai_teacher"))
                .filter(StringUtils::isNotBlank)
                .map(e -> JsonUtils.fromJson(e, AIQuestion.class))
                .filter(e -> StringUtils.isNotBlank(e.getDescription()))
                .map(AIQuestion::getDescription)
                .orElse("");
        String dialoguePlay = Optional.ofNullable(questionResultHistories)
                .filter(CollectionUtils::isNotEmpty)
                .map(e -> e.stream().filter(e1 -> e1.getLessonType() == LessonType.Dialogue || e1.getLessonType() == LessonType.video_conversation).findFirst().orElse(null))
                .map(e -> questionLoaderClient.loadQuestion(e.getQid()))
                .filter(e -> e.getContent() != null && CollectionUtils.isNotEmpty(e.getContent().getSubContents()) && MapUtils.isNotEmpty(e.getContent().getSubContents().get(0).getExtras()))
                .map(e -> e.getContent().getSubContents().get(0).getExtras().get("ai_teacher"))
                .filter(StringUtils::isNotBlank)
                .map(e -> JsonUtils.fromJson(e, AIQuestion.class))
                .filter(e -> StringUtils.isNotBlank(e.getDescription()))
                .map(AIQuestion::getDescription)
                .orElse("");
        User user = userLoaderClient.loadUser(userId);
        NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        String unitName = Optional.ofNullable(unit).map(NewBookCatalog::getAlias).orElse("");
        String unitImage = Optional.ofNullable(unit).map(NewBookCatalog::getExtras).map(e -> e.get("ai_teacher"))
                .map(e -> JsonUtils.fromJson(SafeConverter.toString(e))).map(e -> SafeConverter.toString(e.get("cardImgUrl"), "")).orElse("");
        List<AIUserLessonResultHistory> aiUserLessonResultHistoryList = aiUserLessonResultHistoryDao.loadByUserIdAndUnitId(userId, unitId);
        Map<String, Integer> lessonResult = new HashMap<>();
        int warmUp = 0, task = 0, diag = 0;
        if (CollectionUtils.isNotEmpty(aiUserLessonResultHistoryList)) {
            warmUp = aiUserLessonResultHistoryList.stream().filter(e -> e.getLessonType() == LessonType.WarmUp || e.getLessonType() == LessonType.warm_up)
                    .findFirst().map(AIUserLessonResultHistory::getScore).orElse(0);
            task = aiUserLessonResultHistoryList.stream().filter(e -> e.getLessonType() == LessonType.Task || e.getLessonType() == LessonType.task_conversation)
                    .findFirst().map(AIUserLessonResultHistory::getScore).orElse(0);
            diag = aiUserLessonResultHistoryList.stream().filter(e -> e.getLessonType() == LessonType.Dialogue || e.getLessonType() == LessonType.video_conversation)
                    .findFirst().map(AIUserLessonResultHistory::getScore).orElse(0);
        }
        lessonResult.put("WarmUp", warmUp);
        lessonResult.put("Task", task);
        lessonResult.put("Dialogue", diag);
        boolean showPlay = isShowPlay(userId, unit);
        return MapMessage.successMessage().add("report", unitResultHistory)
                .add("taskPlay", taskPlay)
                .add("showPlay", showPlay)
                .add("dialoguePlay", dialoguePlay)
                .add("totalFinish", total)
                .add("exceed", null)
                .add("unitName", unitName)
                .add("unitImage", unitImage)
                .add("studentName", Optional.ofNullable(user.getProfile()).map(UserProfile::getNickName).orElse(""))
                .add("lessonResult", lessonResult)
                .add("avatar", UserInfoSupport.getUserRoleImage(user));
    }

    private boolean isShowPlay(Long userId, NewBookCatalog unit) {
        // 1、现在短期课电子教材是对所有用户开放的，需要关闭掉未开课用户。
        // 2、现在可以得到电子教材的是三种用户1.推荐好友报名的用户；2已经完课的用户，3旅行口语二期的用户(此条件不用判断)。
        // 3、运营人员在crm后台配置了发送
        ChipEnglishInvitation chipEnglishInvitation = chipEnglishInvitationPersistence.loadByInviterId(userId).stream().findFirst().orElse(null);
        if (chipEnglishInvitation != null) {
            return true;
        }
        ChipsEnglishUserExtSplit userExtSplit = chipsEnglishUserExtSplitDao.load(userId);
        if (userExtSplit != null && userExtSplit.getShowPlay() != null && userExtSplit.getShowPlay()) {
            return true;
        }
        return Optional.ofNullable(unit)
                .map(e -> ifAllUnitFinished(userId, e.bookId()))
                .orElse(false);
    }

    @Override
    public boolean ifAllUnitFinished(Long userId, String bookId) {
        List<AIUserUnitResultHistory> unitResultHistoryList = aiUserUnitResultHistoryDao.loadByUserId(userId);
        if (CollectionUtils.isNotEmpty(unitResultHistoryList)) {
            // 拿到 book 所有的 unit 数量，排除试用单元
            int unitSize = fetchUnitListExcludeTrial(bookId).size();

            return unitResultHistoryList
                    .stream()
                    .filter(e -> !chipsContentService.isTrailUnit(e.getUnitId())) // 排除试用单元
                    .filter(e -> StringUtils.equals(e.getBookId(), bookId))
                    .filter(e -> Boolean.TRUE.equals(e.getFinished()))
                    .collect(Collectors.toList()).size() >= unitSize;
        }
        return false;
    }

    //课程总结
    @Override
    public MapMessage loadLessonResult(User user, String lessonId) {
        if (user == null || StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("parameter is error.");
        }
        AIUserLessonResultHistory aiUserLessonResultHistory = aiUserLessonResultHistoryDao.load(user.getId(), lessonId);
        if (aiUserLessonResultHistory == null) {
            return MapMessage.errorMessage("no lesson result");
        }
        int star = 0;
        int score = 0;
        if (aiUserLessonResultHistory.getStar() != null) {
            star = aiUserLessonResultHistory.getStar();
            score = aiUserLessonResultHistory.getScore() != null ? aiUserLessonResultHistory.getScore() : 0;
        }
        MapMessage message = MapMessage.successMessage();
        message.put("star", star);
        message.put("score", score);
        message.put("lessonType", aiUserLessonResultHistory.getLessonType() != null ? aiUserLessonResultHistory.getLessonType().name() : "");
        message.put("talkList", MapUtils.isNotEmpty(aiUserLessonResultHistory.getExt()) ? aiUserLessonResultHistory.getExt().get("talkList") : Collections.emptyList());
        return message;
    }

    // 任务地图
    @Override
    public MapMessage loadUserMapList(Long userId) {
        AIUserLessonBookRef bookRef;
        try {
            bookRef = chipsUserService.fetchOrInitBookRef(userId);
        } catch (ProductNotExitException e) {
            return MapMessage.successMessage("无购买信息").set("result", "401");
        }

        Date beginDate = Optional.ofNullable(chipsEnglishProductTimetableDao.load(bookRef.getProductId())).map(ChipsEnglishProductTimetable::getBeginDate).orElse(new Date());
        // 获取教材课单元
        List<NewBookCatalog> unitList = fetchUnitListExcludeTrial(bookRef.getBookId());
        if (CollectionUtils.isEmpty(unitList)) {
            return MapMessage.successMessage().add("mapList", Collections.emptyList()).add("beginDate", beginDate);
        }

        //计算开课的时间
        List<Date> lessonDateList = lessonDateList(unitList.size(), beginDate);

        List<AIUserUnitResultHistory> userUnitResultHistoryList = aiUserUnitResultHistoryDao.loadByUserId(userId);
        boolean iswhite = chipsUserService.isInWhiteList(userId);
        List<AIClassInfo> dataList = buildAIClassInfo(unitList, lessonDateList, iswhite, userUnitResultHistoryList);
        dataList.sort(Comparator.comparing(AIClassInfo::getRank));

        return MapMessage.successMessage()
                .add("mapList", dataList)
                .add("beginDate", beginDate)
                .add("reviewRedDot", isRedDot(userId, lessonDateList));
    }

    private boolean isRedDot(Long userId, List<Date> lessonDateList) {
        if (userId == null) {
            return false;
        }
        if (CollectionUtils.isEmpty(lessonDateList)) {
            return false;
        }
        boolean isReviewToday = isReviewToday(lessonDateList);
        if (isReviewToday) {
            String value = aICacheSystem.getAiLoaderRedDotCacheManager().read(userId);
            if (StringUtils.isEmpty(value)) {
                aICacheSystem.getAiLoaderRedDotCacheManager().save(userId, "true");
                return true;
            }
        }
        return false;
    }

    private void skipSatdaySunday(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day == 7) {
            calendar.add(Calendar.DAY_OF_WEEK, 2);
        }
        if (day == 1) {
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }
    }

    /**
     * 计算每个单位的开课时间，周六周日不开课
     */
    private List<Date> lessonDateList(int size, Date beginDate) {
        if (size < 1) {
            return new ArrayList<>();
        }
        Calendar instance = Calendar.getInstance();
        instance.setTime(beginDate);
        List<Date> dateList = new ArrayList<>();
        skipSatdaySunday(instance);
        dateList.add(instance.getTime());
        for (int i = 1; i < size; i++) {
            instance.add(Calendar.DAY_OF_WEEK, 1);
            skipSatdaySunday(instance);
            dateList.add(instance.getTime());
        }
        return dateList;
    }

    /**
     * @param unitList       非空
     * @param lessonDateList 非空
     * @return List<AIClassInfo>
     */
    private List<AIClassInfo> buildAIClassInfo(List<NewBookCatalog> unitList, List<Date> lessonDateList, boolean iswhite, List<AIUserUnitResultHistory> userUnitResultHistoryList) {
        if (unitList.size() != lessonDateList.size()) {
            logger.warn("课程单元与配置的时间数量不一致");
        }
        List<AIClassInfo> dataList = new ArrayList<>();
        for (int i = 0; i < unitList.size(); i++) {
            NewBookCatalog catalog = unitList.get(i);
            AIClassInfo classInfo = new AIClassInfo();
            classInfo.setId(catalog.getId());
            classInfo.setCname(catalog.getAlias());
            classInfo.setName(catalog.getName());
            classInfo.setRank(i + 1);
            classInfo.setCurrentDay(false);
            classInfo.setFinished(false);
            classInfo.setLock(isLock(iswhite, lessonDateList, i));
            classInfo.setImg(MapUtils.isNotEmpty(catalog.getExtras()) ? SafeConverter.toString(catalog.getExtras().get(ClassImgStr), "") : "");
            AIUserUnitResultHistory unitResult = userUnitResultHistoryList.stream().filter(u -> StringUtils.equals(catalog.getId(), u.getUnitId()))
                    .findFirst().orElse(null);
            if (unitResult != null) {
                classInfo.setFinished(true);
                classInfo.setStar(unitResult.getStar());
            }
            if (DayRange.current().contains(lessonDateList.get(Math.min(i, lessonDateList.size() - 1)))) {
                classInfo.setCurrentDay(true);
            }
            dataList.add(classInfo);
        }
        return dataList;
    }

    private Date getCurrentDate() {
        return new Date();
    }

    private boolean isReviewToday(List<Date> lessonDateList) {
        if (CollectionUtils.isEmpty(lessonDateList)) {
            return false;
        }
        Date now = getCurrentDate();
        Date min = lessonDateList.get(0);
        Date max = lessonDateList.get(lessonDateList.size() - 1);
        if (now.before(min) || now.after(max)) {
            return false;
        }
        String nowStr = DateUtils.dateToString(now, DateUtils.FORMAT_SQL_DATE);
        for (Date d : lessonDateList) {
            String dStr = DateUtils.dateToString(d, DateUtils.FORMAT_SQL_DATE);
            if (dStr.equals(nowStr)) {
                return false;
            }
        }
        return true;
    }

    private boolean isLock(boolean iswhite, List<Date> lessonDateList, int index) {
        if (iswhite) {
            return false;
        }
        if (lessonDateList.size() > index) {
            Date openDate = lessonDateList.get(index);
            Date now = getCurrentDate();
            if (now.after(openDate)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 　* @Description: 获取app视频模块信息
     * 　* @author zhiqi.yao
     * 　* @date 2018/4/20 16:18
     */
    @Override
    public MapMessage loadVideo(Long userId) {
        // 获取教材课单元
//        List<UserOrder> orderProducts = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), userId);
//        if (CollectionUtils.isEmpty(orderProducts)) {
//            return MapMessage.errorMessage("no order!");
//        }
//        UserOrder order = orderProducts.get(0);
//        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
//        if (CollectionUtils.isEmpty(orderProductItems)) {
//            return MapMessage.errorMessage("no order product item!");
//        }
//        OrderProductItem orderProductItem = orderProductItems.get(0);
//        String bookId = orderProductItem.getAppItemId();
//
//        List<NewBookCatalog> units = newContentLoaderClient.loadChildrenSingle(bookId, BookCatalogType.UNIT);
//        units.sort(Comparator.comparing(NewBookCatalog::getRank));
//        if (CollectionUtils.isEmpty(units)) {
//            return MapMessage.errorMessage("no unit!");
//        }
//        AIVideoConfig handouts = null;
//        String productItemId = orderProductItem.getId();
//        int day = howManydays(productItemId);
//        if (day > 0 && day <= units.size()) {
//            int i = day - 1;
//            NewBookCatalog catalog = units.get(i);
//            Map<String, Object> extMap = catalog.getExtras();
//            if (MapUtils.isNotEmpty(extMap) && extMap.get(AiTeacher) != null) {
//                String aiTeacher = extMap.get(AiTeacher).toString();
//                JSONObject jsonObject = JSONObject.parseObject(aiTeacher);
//                handouts = getAIVideoConfig(jsonObject);
//            }
//        }
//        /**
//         * 获取 热门  精选活动  搞笑集锦
//         */
//        Map<String, List<AIVideoConfig>> questionResultMap = aiVideoConfigDao.findAll().stream()
//                .collect(Collectors.groupingBy(AIVideoConfig::getType));
//        AIVideoHandouts aiVideoHandouts = new AIVideoHandouts();
//        aiVideoHandouts.setHandouts(handouts);
//        aiVideoHandouts.setHotVideo(questionResultMap.get(VideoType.HOT_VIDEO.name()));
//        aiVideoHandouts.setActivityVideo(questionResultMap.get(VideoType.ACTIVITY_VIDEO.name()));
//        aiVideoHandouts.setFunnyVideo(questionResultMap.get(VideoType.FUNNY_VIDEO.name()));
        return MapMessage.successMessage().add("videoList", Collections.emptyList());
    }

    @Override
    public MapMessage loadHandoutsList(Long userId) {
//        List<UserOrder> orderProducts = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), userId);
//        if (CollectionUtils.isEmpty(orderProducts)) {
//            return MapMessage.errorMessage("no order!");
//        }
//        UserOrder order = orderProducts.get(0);
//        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
//        if (CollectionUtils.isEmpty(orderProductItems)) {
//            return MapMessage.errorMessage("no order product item!");
//        }
//        OrderProductItem orderProductItem = orderProductItems.get(0);
//        String bookId = orderProductItem.getAppItemId();
//
//        List<NewBookCatalog> units = newContentLoaderClient.loadChildrenSingle(bookId, BookCatalogType.UNIT);
//        units.sort(Comparator.comparing(NewBookCatalog::getRank));
//        if (CollectionUtils.isEmpty(units)) {
//            return MapMessage.errorMessage("no unit!");
//        }
//        List<AIVideoConfig> aiVideoHandouts = new ArrayList<>();
//        String productItemId = orderProductItem.getId();
//        int day = howManydays(productItemId);
//        for (int i = 0; i < day && i < units.size(); i++) {
//            NewBookCatalog catalog = units.get(i);
//            Map<String, Object> extMap = catalog.getExtras();
//            if (MapUtils.isNotEmpty(extMap) && extMap.get(AiTeacher) != null) {
//                String aiTeacher = extMap.get(AiTeacher).toString();
//                JSONObject jsonObject = JSONObject.parseObject(aiTeacher);
//                AIVideoConfig handouts = getAIVideoConfig(jsonObject);
//                aiVideoHandouts.add(handouts);
//            }
//        }
        return MapMessage.successMessage().add("handoutsList", Collections.emptyList());
    }
//
//    private AIVideoConfig getAIVideoConfig(JSONObject jsonObject) {
//        AIVideoConfig handouts = new AIVideoConfig();
//        handouts.setTitle(jsonObject.getString("videoTitle"));
//        handouts.setSubhead(jsonObject.getString("videoSubTitle"));
//        handouts.setVideoUrl(jsonObject.getString("videoUrl"));
//        handouts.setDescription(jsonObject.getString("videoDescription"));
//        handouts.setUploaderHead(jsonObject.getString("videoAvatar"));
//        handouts.setUploaderName(jsonObject.getString("videoName"));
//        handouts.setType("0");
//        handouts.setDisabled(false);
//        return handouts;
//    }
//
//    /**
//     * 判断当前时间是对应的第X天讲义，
//     * 每天中午12点之前显示前一天的讲义，第一天12点之前讲义为空
//     * 超过第10天12点之后，就一直显示第10天讲义，不再进行更新。
//     *
//     * @param productItemId
//     * @return days
//     */
//    private int howManydays(String productItemId) {
//        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductByProductItemId(productItemId);
//        Date beginDate = ChipsOrderProductSupport.getBeginDate(orderProduct);
//
//        Date nowDate = new Date();
//        if (beginDate == null || beginDate.after(nowDate)) {
//            return 0;
//        }
//        long diff = DateUtils.dayDiff(nowDate, beginDate);
//        if (diff >= 10) {
//            return 10;
//        }
//        String hourDiff = DurationFormatUtils.formatPeriod(beginDate.getTime(), nowDate.getTime(), "H");
//        BigDecimal bigDecimal = new BigDecimal(hourDiff);
//        BigDecimal dayHours = new BigDecimal("24");
//        return bigDecimal.divide(dayHours, 0, BigDecimal.ROUND_HALF_UP).intValue();
//    }

    @Override
    public MapMessage loadOrderStatus(Long userId) {
        int days = Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName("chipsEnglishBuyEndDays"))
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .map(e -> SafeConverter.toInt(e.getValue()))
                .orElse(2);
        Date now = new Date();
        List<OrderProduct> orderProductList;
        if (RuntimeMode.current().le(Mode.STAGING)) {
            orderProductList = userOrderLoaderClient.loadAllOrderProductIncludeOffline();
        } else {
            orderProductList = userOrderLoaderClient.loadAvailableProduct();
        }
        List<String> orderProductListIdList = orderProductList.stream()
                .filter(p -> OrderProductServiceType.ChipsEnglish == OrderProductServiceType.safeParse(p.getProductType()))
                .map(OrderProduct::getId)
                .collect(Collectors.toList());
        Map<String, ChipsEnglishProductTimetable> timetableMap = chipsEnglishProductTimetableDao.loads(orderProductListIdList);

        OrderProduct orderProduct = orderProductList.stream()
                .filter(p -> OrderProductServiceType.ChipsEnglish == OrderProductServiceType.safeParse(p.getProductType()))
                .filter(e -> {
                    ChipsEnglishProductTimetable timetable = timetableMap.get(e.getId());
                    if (timetable == null) {
                        return false;
                    }

                    Date beginDate = timetable.getBeginDate();
                    return beginDate != null && DateUtils.addDays(now, days).before(beginDate);

                })
                .sorted((e1, e2) -> {
                    Date o1 = null;
                    ChipsEnglishProductTimetable timetable1 = timetableMap.get(e1.getId());
                    if (timetable1 != null) {
                        o1 = timetable1.getBeginDate();
                    }

                    Date o2 = null;
                    ChipsEnglishProductTimetable timetable2 = timetableMap.get(e2.getId());
                    if (timetable2 != null) {
                        o2 = timetable2.getBeginDate();
                    }
                    return o1 != null && o2 != null ? o1.compareTo(o2) : -1;
                }).findFirst().orElse(null);
        if (orderProduct == null) {
            orderProduct = Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName("chipsEnglishSellOutProduct"))
                    .filter(e -> StringUtils.isNotBlank(e.getValue()))
                    .map(e -> orderProductList.stream().filter(e1 -> e1.getId().equals(e.getValue())).findFirst().orElse(null))
                    .orElse(null);
        }

        if (orderProduct == null) {
            return MapMessage.errorMessage("no products");
        }
        String productId = orderProduct.getId();
        OrderProductItem orderProductItem = userOrderLoaderClient.loadProductItemsByProductId(productId).stream().findFirst().orElse(null);
        if (orderProductItem == null) {
            return MapMessage.errorMessage("no products");
        }

        Date beginDate = Optional.of(orderProduct)
                .map(e -> timetableMap.get(e.getId()))
                .map(ChipsEnglishProductTimetable::getBeginDate)
                .orElse(now);

        Date endDate = DateUtils.addDays(beginDate, 90);
        Date sellEndDate = DateUtils.addDays(beginDate, -days);

        List<String> images = Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName("chipsEnglishCourseImages"))
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .map(e -> JsonUtils.fromJsonToList(e.getValue(), String.class))
                .filter(CollectionUtils::isNotEmpty)
                .orElse(Collections.emptyList());

        List<ChipsUserCourse> chipsUserCourses = chipsUserService.loadUserEffectiveCourse(userId);
        if (CollectionUtils.isEmpty(chipsUserCourses) || !chipsUserCourses.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet()).contains(orderProductItem.getId())) {
            return MapMessage.successMessage()
                    .add("status", PaymentStatus.Unpaid.name())
                    .add("userId", userId)
                    .add("beginDate", DateUtils.dateToString(beginDate, "yyyy年MM月dd日"))
                    .add("endDate", DateUtils.dateToString(endDate, "yyyy年MM月dd日"))
                    .add("sellOutDate", DateUtils.dateToString(sellEndDate, "yyyy年MM月dd日"))
                    .add("productName", orderProduct.getName())
                    .add("sellOut", now.after(sellEndDate))
                    .add("productId", orderProduct.getId())
                    .add("originalPrice", orderProduct.getOriginalPrice())
                    .add("images", images)
                    .add("price", orderProduct.getPrice());
        }
        return MapMessage.successMessage()
                .add("productName", orderProduct.getName())
                .add("beginDate", DateUtils.dateToString(beginDate, "yyyy年MM月dd日"))
                .add("endDate", DateUtils.dateToString(endDate, "yyyy年MM月dd日"))
                .add("sellOutDate", DateUtils.dateToString(sellEndDate, "yyyy年MM月dd日"))
                .add("sellOut", now.after(sellEndDate))
                .add("userId", userId)
                .add("images", images)
                .add("status", PaymentStatus.Paid.name())
                .add("productId", orderProduct.getId());
    }

    @Override
    public MapMessage loadInvitationInfoByUserId(Long userId) {
        List<ChipEnglishInvitation> chipEnglishInvitations = chipEnglishInvitationPersistence.loadByInviterId(userId);
        return MapMessage.successMessage().add("userNO", chipEnglishInvitations.size()).add("totalAward", (CollectionUtils.isNotEmpty(chipEnglishInvitations) ? chipEnglishInvitations.stream().filter(e -> Boolean.TRUE.equals(e.getSend())).collect(Collectors.toList()).size() : 0) * 9.9);
    }

    @Override
    public MapMessage loadTrialCourseUnitInfo() {
        NewBookCatalog unitCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(ChipCourseSupport.TRAVEL_ENGLISH_TRIAL_UNIT);
        if (unitCatalog == null) {
            return MapMessage.errorMessage("no unit find!");
        }

        // 获取lesson信息
        List<NewBookCatalog> lessons = newContentLoaderClient.loadChildrenSingle(ChipCourseSupport.TRAVEL_ENGLISH_TRIAL_UNIT, BookCatalogType.LESSON);
        if (CollectionUtils.isEmpty(lessons)) {
            return MapMessage.errorMessage("no lesson!");
        }
        List<AILessonInfo> lessonInfos = new ArrayList<>();
        lessons.sort(Comparator.comparing(NewBookCatalog::getRank));
        for (NewBookCatalog lesson : lessons) {
            AILessonInfo lessonInfo = new AILessonInfo();
            lessonInfo.setId(lesson.getId());
            lessonInfo.setName(lesson.getName());
            lessonInfo.setRank(lesson.getRank());
            LessonType lessonType = getLessonType(lesson.getName());
            if (lessonType == null) {
                logger.error("no lessonType find, lessonName {}", lesson.getName());
                continue;
            }
            lessonInfo.setLessonType(getLessonType(lesson.getName()));
            lessonInfo.setFinished(false);
            lessonInfo.setIsLock(false);
            lessonInfo.setStar(0);
            lessonInfo.setUnitId(ChipCourseSupport.TRAVEL_ENGLISH_TRIAL_UNIT);
            lessonInfos.add(lessonInfo);
        }
        Map<String, Object> map = JsonUtils.fromJson(SafeConverter.toString(unitCatalog.getExtras().get("ai_teacher")));
        return MapMessage.successMessage().add("lessons", lessonInfos)
                .add("unitCname", MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("pageTitle"), "") : "")
                .add("unitName", MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("pageSubTitle"), "") : "")
                .add("goals", Collections.singletonList(MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("goalOfStudy"), "").replace("\\u0a0", " ").replace("\\U0a0", " ").replace("  ", " ") : ""))
                .add("goalAudio", MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("goalOfStudyAudioUrl"), "") : "")
                .add("unitId", ChipCourseSupport.TRAVEL_ENGLISH_TRIAL_UNIT);
    }

    @Override
    public MapMessage loadTrialCourseUnitLessonInfo(String lessonId) {
        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
        if (lesson == null) {
            return MapMessage.errorMessage("lesson is null").set("result", "400");
        }
        List<AIQuestion> aiQuestions = new ArrayList<>();
        List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionsByLessonIds(Collections.singletonList(lessonId), Subject.ENGLISH.getId());
        for (NewQuestion newQuestion : newQuestionList) {
            if (newQuestion.getContent() == null || CollectionUtils.isEmpty(newQuestion.getContent().getSubContents())) {
                continue;
            }
            if (MapUtils.isEmpty(newQuestion.getContent().getSubContents().get(0).getExtras())) {
                continue;
            }
            String content = newQuestion.getContent().getSubContents().get(0).getExtras().get("ai_teacher");
            if (StringUtils.isBlank(content)) {
                continue;
            }
            AIQuestion aiQuestion = JsonUtils.fromJson(content, AIQuestion.class);
            if (aiQuestion == null || aiQuestion.getType() == null) {
                continue;
            }
            aiQuestion.setId(newQuestion.getId());
            aiQuestion.setFinished(false);
            aiQuestions.add(aiQuestion);
        }
        Map<String, Object> map = JsonUtils.fromJson(SafeConverter.toString(lesson.getExtras().get("ai_teacher")));
        return MapMessage.successMessage()
                .set("result", "success")
                .set("questions", aiQuestions.stream().collect(Collectors.groupingBy(AIQuestion::getType)))
                .set("title", MapUtils.isNotEmpty(map) && map.get("title") != null ? map.get("title") : "")
                .set("background", MapUtils.isNotEmpty(map) && map.get("backgroundIntro") != null ? map.get("backgroundIntro") : "")
                .set("backgroundAudio", MapUtils.isNotEmpty(map) && map.get("backgroundAudioUrl") != null ? map.get("backgroundAudioUrl") : "")
                .set("goal", MapUtils.isNotEmpty(map) && map.get("goalIntro") != null ? map.get("goalIntro") : "")
                .set("goalAudio", MapUtils.isNotEmpty(map) && map.get("goalAudioUrl") != null ? map.get("goalAudioUrl") : "")
                .set("image", MapUtils.isNotEmpty(map) && map.get("image") != null ? map.get("image") : "http://cdn.17zuoye.com/fs-resource/5ad99cfdd700a04137a7f19d.png")
                .set("subtitle", MapUtils.isNotEmpty(map) && map.get("subTitle") != null ? map.get("subTitle") : "")
                .set("rewards", Collections.emptyList())
                .set("summaries", MapUtils.isNotEmpty(map) && map.get("summaries") != null ? map.get("summaries") : Collections.emptyList());
    }

    @Override
    public MapMessage loadCourseStudyPlanInfo(Long userId, String unitId) {
        if (userId == null || userId <= 0L || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("param error");
        }
        List<AIUserUnitResultPlan> userPlanList = aiUserUnitResultPlanDao.loadByUserId(userId);
        if (CollectionUtils.isEmpty(userPlanList)) {
            return MapMessage.errorMessage("no data");
        }
        AIUserUnitResultPlan userUnitResultPlan = userPlanList.stream().filter(e -> e.getUnitId().equals(unitId)).findFirst().orElse(null);
        if (userUnitResultPlan == null) {
            return MapMessage.errorMessage("no data");
        }

        NewBookCatalog unitCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unitCatalog == null) {
            return MapMessage.errorMessage("no data");
        }

        int rank = 1;
        List<NewBookCatalog> newBookCatalogs = newContentLoaderClient.loadChildrenSingle(unitCatalog.bookId(), BookCatalogType.UNIT).stream()
                .filter(e -> !chipsContentService.isTrailUnit(e.getId())).collect(Collectors.toList());
        newBookCatalogs.sort(Comparator.comparing(NewBookCatalog::getRank));
        for (NewBookCatalog unit : newBookCatalogs) {
            if (unit.getId().equals(unitId)) {
                break;
            }
            rank++;
        }

        int finishRanking = 1;
        int scoreRanking = 1;
        ChipsEnglishClass clazz = Optional.ofNullable(chipsUserService.loadUserBoughtProduct(userId))
                .filter(CollectionUtils::isNotEmpty)
                .map(e -> userOrderLoaderClient.loadOrderProducts(e))
                .filter(MapUtils::isNotEmpty)
                .map(Map::values)
                .map(e -> e.stream().filter(e1 -> StringUtils.isNotBlank(e1.getAttributes())).filter(e1 -> {
                    Map<String, Object> map = JsonUtils.fromJson(e1.getAttributes());
                    if (MapUtils.isEmpty(map)) {
                        return false;
                    }
                    int grade = SafeConverter.toInt(map.get("grade"));
                    return grade <= 0;
                }).findFirst().orElse(null))
                .map(e -> chipsUserService.loadClazzIdByUserAndProduct(userId, e.getId())).orElse(null);
        List<AIUserUnitResultPlan> aiUserUnitResultPlans = aiUserUnitResultPlanDao.loadByUnitId(unitId);
        if (clazz != null && CollectionUtils.isNotEmpty(aiUserUnitResultPlans)) {
            List<ChipsEnglishClassUserRef> userRefList = chipsUserService.selectChipsEnglishClassUserRefByClazzId(clazz.getId());
            if (CollectionUtils.isNotEmpty(userPlanList)) {
                List<Long> userIdList = userRefList.stream().map(ChipsEnglishClassUserRef::getUserId).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(userIdList)) {
                    scoreRanking = aiUserUnitResultPlans.stream().filter(e -> userIdList.contains(e.getUserId())).filter(e -> e.getScore().compareTo(userUnitResultPlan.getScore()) > 0).collect(Collectors.toList()).size() + 1;
                    finishRanking = aiUserUnitResultPlans.stream().filter(e -> userIdList.contains(e.getUserId())).filter(e -> e.getCreateDate().compareTo(userUnitResultPlan.getCreateDate()) < 0).collect(Collectors.toList()).size() + 1;
                }
            }
        }
        MapMessage message = MapMessage.successMessage();
        switch (userUnitResultPlan.getGrade()) {
            case A:
                message.add("summary", MessageConfig.unit_grade_A);
                break;
            case B:
                message.add("summary", MessageConfig.unit_grade_B);
                break;
            case C:
                message.add("summary", MessageConfig.unit_grade_C);
                break;
        }

        String title = Optional.ofNullable(newContentLoaderClient.loadBookCatalogByCatalogId(unitId)).map(NewBookCatalog::getExtras)
                .filter(MapUtils::isNotEmpty)
                .map(e -> JsonUtils.fromJson(SafeConverter.toString(e.get("ai_teacher"))))
                .filter(MapUtils::isNotEmpty)
                .map(e -> SafeConverter.toString(e.get("cardTitle"), ""))
                .orElse("");
        message.add("lessonHistory", lessonHistory(userPlanList, newBookCatalogs))
                .add("title", title)
                .add("finishRanking", finishRanking)
                .add("scoreRanking", scoreRanking)
                .add("gradeAScore", 90)
                .add("pointAbilityName", userUnitResultPlan.getPointAbility() != null ? userUnitResultPlan.getPointAbility().getDescription() : "")
                .add("lessonSummary", MessageConfig.getUnitExtInfo(unitId))
                .putAll(JsonUtils.fromJson(JsonUtils.toJson(userUnitResultPlan)));
        message.put("rank", rank);
        return message;
    }

    @Override
    public MapMessage loadBookResultInfo(Long userId) {
        if (userId == null || userId <= 0L) {
            return MapMessage.errorMessage("param error");
        }
        AIUserBookResult result = aiUserBookResultDao.loadByUserId(userId).stream().filter(e -> e.getBookId().equals(ChipCourseSupport.TRAVEL_ENGLISH_BOOK_ID)).findFirst().orElse(null);
        if (result == null) {
            return MapMessage.errorMessage("no data");
        }

        List<AIUserUnitResultPlan> userPlanList = aiUserUnitResultPlanDao.loadByUserId(userId);

        MapMessage message = MapMessage.successMessage();
        message.add("gradeAScore", 90)
                .add("levelName", result.getLevel() != null ? result.getLevel().getDescription() : "")
                .add("lessonHistory", lessonHistory(userPlanList, chipCourseSupport.fetchUnitListExcludeTrial(ChipCourseSupport.TRAVEL_ENGLISH_BOOK_ID)))
                .putAll(JsonUtils.fromJson(JsonUtils.toJson(result)));
        return message;
    }

    @Override
    public List<ChipEnglishInvitation> loadInvitationByInviterId(Long inviterId) {
        if (inviterId == null || Long.compare(inviterId, 0L) <= 0L) {
            return Collections.emptyList();
        }
        return chipEnglishInvitationPersistence.loadByInviterId(inviterId);
    }

    @Override
    public MapMessage loadLessonPlay(String unitId) {
        // 获取lesson信息
        List<NewBookCatalog> lessons = newContentLoaderClient.loadChildrenSingle(unitId, BookCatalogType.LESSON);
        if (CollectionUtils.isEmpty(lessons)) {
            return MapMessage.successMessage().add("taskPlay", Collections.emptyList()).add("dialoguePlay", Collections.emptyList());
        }
        MapMessage mapMessage = MapMessage.successMessage();
        NewBookCatalog unitCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        Map<String, Object> map = JsonUtils.fromJson(SafeConverter.toString(unitCatalog.getExtras().get("ai_teacher")));
        mapMessage.add("unitCname", MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("pageTitle"), "") : "")
                .add("unitName", MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("pageSubTitle"), "") : "");
        for (NewBookCatalog lesson : lessons) {
            LessonType lessonType = getLessonType(lesson.getName());
            if (lessonType == LessonType.WarmUp) {
                continue;
            }
            AILessonPlay aiLessonPlay = aiLessonPlayDao.load(lesson.getId());
            if (lessonType == LessonType.Dialogue) {
                mapMessage.put("dialoguePlay", aiLessonPlay != null && CollectionUtils.isNotEmpty(aiLessonPlay.getPlay()) ? aiLessonPlay.getPlay() : Collections.emptyList());
            }

            if (lessonType == LessonType.Task) {
                mapMessage.put("taskPlay", aiLessonPlay != null && CollectionUtils.isNotEmpty(aiLessonPlay.getPlay()) ? aiLessonPlay.getPlay() : Collections.emptyList());
            }
        }
        return mapMessage;
    }

    @Override
    public MapMessage loadCourseList() {
        List<NewBookCatalog> units = fetchUnitListExcludeTrial(ChipCourseSupport.TRAVEL_ENGLISH_BOOK_ID);
        List<Map<String, Object>> res = new ArrayList<>();
        for (int i = 0; i < units.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", units.get(i).getId());
            Map<String, Object> ext = JsonUtils.fromJson(SafeConverter.toString(units.get(i).getExtras().get("ai_teacher")));
            map.put("name", MapUtils.isNotEmpty(ext) ? SafeConverter.toString(ext.get("pageTitle"), "") : "");
            map.put("tip", MapUtils.isNotEmpty(ext) ? SafeConverter.toString(ext.get("pageSubTitle"), "") : "");
            map.put("rank", i + 1);
            res.add(map);
        }
        return MapMessage.successMessage().add("lessonList", res);
    }

    @Override
    public Long loadMyVirtualClazz(Long userId) {
//        AIUserLessonBookRef aiUserLessonBookRef = aiUserLessonBookSupport.fetchUserCurrentBook(userId);
//        ChipsVisualClass visualClass = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), userId).stream()
//                .filter(e -> aiUserLessonBookRef == null || e.getProductId().equals(aiUserLessonBookRef.getProductId()))
//                .sorted(Comparator.comparing(UserOrder::getUpdateDatetime))
//                .findFirst()
//                .map(e -> chipsVisualClassDao.findByProductId(e.getProductId()).stream().filter(e1 -> CollectionUtils.isNotEmpty(e1.getUserIds()) && e1.getUserIds().contains(userId)).findFirst().orElse(null))
//                .orElse(null);
//        if (visualClass == null || "Winston".equals(visualClass.getTeacherCode())) {
//            return 2L;
//        }

        return 1L;
    }

    @Override
    public List<ChipsRank> loadShareVideoRanking(String clazz, String unitId) {
        if (StringUtils.isAnyBlank(unitId, clazz)) {
            return Collections.emptyList();
        }
        return chipsShareVideoRankCacheManager.getRankList(400, unitId, clazz);
    }

    @Override
    public List<AIUserUnitResultPlan> loadUnitStudyPlan(String unitId) {
        if (StringUtils.isBlank(unitId)) {
            return Collections.emptyList();
        }
        return aiUserUnitResultPlanDao.loadByUnitId(unitId);
    }

    @Override
    public List<AIUserUnitResultPlan> loadUnitStudyPlan(Long userId) {
        if (userId == null || userId.compareTo(0L) <= 0) {
            return Collections.emptyList();
        }
        return aiUserUnitResultPlanDao.loadByUserId(userId);
    }

    @Override
    public Map<Long, List<AIUserUnitResultPlan>> loadUnitStudyPlan(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        Map<Long, List<AIUserUnitResultPlan>> res = new HashMap<>();
        userIds.forEach(e -> {
            if (e != null && e.compareTo(0L) > 0) {
                res.put(e, aiUserUnitResultPlanDao.loadByUserId(e).stream().filter(e1 -> !chipsContentService.isTrailUnit(e1.getUnitId())).collect(Collectors.toList()));
            }
        });
        return res;
    }

    @Override
    @Deprecated
    public NewBookCatalog loadTodayStudyUnit() {
        return chipCourseSupport.loadTodayShortTravelStudyUnit();
    }

    @Override
    public AIUserVideo loadUserVideoById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return aiUserVideoDao.load(id);
    }

    @Override
    public List<AIUserVideo> loadUserVideoListByUnitId(String unitId, AIUserVideo.ExamineStatus examineStatus) {
        if (StringUtils.isBlank(unitId) || examineStatus == null) {
            return Collections.emptyList();
        }
        return aiUserVideoDao.loadByUnitId(unitId, examineStatus);
    }

    @Override
    public List<AIUserVideo> loadUserVideoListByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return aiUserVideoDao.loadByUserId(userId);
    }

    @Override
    public Map<Long, AIUserBookResult> loadPreviewUserBookResult(Collection<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyMap();
        }
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIdList);
        if (MapUtils.isEmpty(userMap)) {
            return Collections.emptyMap();
        }
        Map<Long, AIUserBookResult> resultMap = new HashMap<>();
        userIdList.forEach(e -> {
            List<AIUserUnitResultPlan> aiUserUnitResultPlans = aiUserUnitResultPlanDao.loadByUserId(e).stream().filter(e2 -> !chipsContentService.isTrailUnit(e2.getUnitId())).sorted(Comparator.comparing(AIUserUnitResultPlan::getScore))
                    .collect(Collectors.toList());
            User user = userMap.get(e);
            if (user != null && CollectionUtils.isNotEmpty(aiUserUnitResultPlans) && aiUserUnitResultPlans.size() > 3) {
                AIUserBookResult bookResult = chipsContentService.initBookResult(user, aiUserUnitResultPlans, ChipCourseSupport.TRAVEL_ENGLISH_BOOK_ID);
                if (bookResult != null) {
                    if (aiUserUnitResultPlans.size() == 10 && aiUserBookResultDao.load(bookResult.getId()) == null) {
                        aiUserBookResultDao.insert(bookResult);
                        notifyBookResult(user.getId(), bookResult.getBookId());
                    }
                    resultMap.put(e, bookResult);
                }
            }
        });
        return resultMap;
    }

    @Override
    public Collection<Long> loadOfficialProductUser(Integer courseGrade) {
        if (courseGrade == null) {
            return Collections.emptyList();
        }
        return userOfficialProductBuyCacheManager.getRecord(courseGrade);
    }

    @Override
    public Map<String, Object> loadUserShareRecords(Long userId, String bookId) {
        if (userId == null || StringUtils.isBlank(bookId)) {
            return Collections.emptyMap();
        }
        Map<String, ChipsEnglishUserSignRecord> signRecordMap = chipsEnglishUserSignRecordDao.loadByUserId(userId).stream()
                .filter(e -> e.getBookId().equals(bookId)).collect(Collectors.toMap(ChipsEnglishUserSignRecord::getUnitId, e -> e));


        List<StoneUnitData> units = chipCourseSupport.fetchUnitListExcludeTrialV2(bookId);
        List<Map<String, Object>> records = new ArrayList<>();

        ChipsEnglishPageContentConfig obj = aiChipsEnglishConfigService.loadChipsConfigByName(COUPON_SEND_UNIT_IDX_KEY);
        int unitIdx = SafeConverter.toInt(Optional.ofNullable(obj).map(ChipsEnglishPageContentConfig::getValue).orElse("0"));

        int maxRecord = units.size();
        if (unitIdx > 0 && unitIdx < maxRecord) {
            maxRecord = unitIdx;
        }
        int count = 0;
        for (int i = 0; i < maxRecord; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("title", i + 1);
            if (MapUtils.isNotEmpty(signRecordMap) && signRecordMap.get(units.get(i).getId()) != null) {
                ChipsEnglishUserSignRecord record = signRecordMap.get(units.get(i).getId());
                if (record != null && record.getCurrent()) {
                    map.put("status", true);
                    count = count + 1;
                }
            } else {
                map.put("status", false);
            }
            records.add(map);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        data.put("records", records);
        return data;
    }

    @Override
    public Date loadUnitBeginTime(OrderProduct product, String bookId, String unitId) {
        if (product == null || StringUtils.isBlank(product.getId()) || StringUtils.isBlank(unitId)) {
            return null;
        }
        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(product.getId());
        if (timetable == null || CollectionUtils.isEmpty(timetable.getCourses())) {
            return null;
        }
        return timetable.getCourses().stream().filter(c -> StringUtils.isNotBlank(c.getUnitId()) && c.getUnitId().equals(unitId)).map(ChipsEnglishProductTimetable.Course::getBeginDate).findFirst().orElse(null);
    }

    @Override
    public List<String> loadAllValidUnitIdByBookIdSortWithRank(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return null;
        }
        List<NewBookCatalog> catalogList = fetchUnitListExcludeTrial(bookId);
        if (CollectionUtils.isEmpty(catalogList)) {
            return null;
        }
        List<String> unitIdList = new ArrayList<>();
        catalogList.forEach(unit -> {
            if (unit != null && StringUtils.isNotBlank(unit.getId())) {
                unitIdList.add(unit.getId());
            }
        });
        return unitIdList;
    }

    @Override
    @Deprecated
    public List<NewBookCatalog> loadAllValidUnitByBookIdSortWithRank(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return null;
        }
        List<NewBookCatalog> unitList = fetchUnitListExcludeTrial(bookId);
        if (CollectionUtils.isEmpty(unitList)) {
            return unitList;
        }
        unitList.sort(Comparator.comparing(NewBookCatalog::getRank));
        return unitList;
    }


    @Override
    @Deprecated
    public List<NewBookCatalog> loadValidBeginUnitByBookIdSortWithRank(OrderProduct orderProduct, String bookId) {
        if (orderProduct == null || StringUtils.isBlank(orderProduct.getId()) || StringUtils.isBlank(bookId)) {
            return null;
        }
        // 获取教材课单元
        List<NewBookCatalog> unitList = fetchUnitListExcludeTrial(bookId);
        if (CollectionUtils.isEmpty(unitList)) {
            return null;
        }
        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(orderProduct.getId());
        if (timetable == null || timetable.getBeginDate() == null) {
            return null;
        }
        Date beginDate = timetable.getBeginDate();
        //计算开课的时间
        List<Date> lessonDateList = lessonDateList(unitList.size(), beginDate);
        List<NewBookCatalog> list = new ArrayList<>();
        for (int i = 0; i < unitList.size(); i++) {
            boolean lock = isLock(false, lessonDateList, i);
            if (lock) {
                continue;
            }
            list.add(unitList.get(i));
        }
        return list;
    }

    @Override
    public MapMessage sendGradingReportTemplateMessage(Long userId, String bookId) {
        if (userId == null || userId == 0L || StringUtils.isBlank(bookId)) {
            return MapMessage.successMessage();
        }
        List<AIUserBookResult> userBookResultList = aiUserBookResultDao.loadByUserId(userId);
        if (CollectionUtils.isEmpty(userBookResultList)) {
            return MapMessage.successMessage();
        }
        AIUserBookResult bookResult = filterByBookId(userBookResultList, bookId);
        if (bookResult == null) {
            return MapMessage.successMessage();
        }
        notifyBookResult(userId, bookId);
        return MapMessage.successMessage();
    }


//    public MapMessage sendGraduationCertificateTemplateMessageOld(Long userId) {
//        if (userId == null || userId == 0L) {
//            return MapMessage.successMessage();
//        }
//        List<UserOrder> userOrderList = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), userId);
//        if (CollectionUtils.isEmpty(userOrderList)) {
//            return MapMessage.successMessage();
//        }
//        for (UserOrder userOrder : userOrderList) {
//            String productId = userOrder.getProductId();
//            OrderProductItem item = userOrderLoaderClient.loadProductItemsByProductId(productId).stream().findFirst().orElse(null);
//            if (item == null || StringUtils.isBlank(item.getAppItemId())) {
//                continue;
//            }
//            List<AIUserUnitResultHistory> aiUserBookResultList = aiUserUnitResultHistoryDao.loadByUserId(userId).stream().filter(e -> item.getAppItemId().equals(e.getBookId())).collect(Collectors.toList());
//            if (aiUserBookResultList == null || aiUserBookResultList.size() < 8) {
//                continue;
//            }
//            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
//            if (orderProduct == null) {
//                continue;
//            }
//            Date endDate = Optional.ofNullable(orderProduct.getId()).map(p -> chipsEnglishProductTimetableDao.load(p)).map(ChipsEnglishProductTimetable::getEndDate).orElse(null);
//            if (endDate == null || new Date().before(endDate)) {
//                continue;
//            }
//            sendGraduationCertificateMessage(userId, userOrder, orderProduct);
//        }
//        return MapMessage.successMessage();
//    }

    /**
     * 已经购买 并且课程结束 并且上完八节课的发消息
     */
    @Override
    public MapMessage sendGraduationCertificateTemplateMessage(Long userId) {
        if (userId == null || userId == 0L) {
            return MapMessage.successMessage();
        }
        List<ChipsUserCourse> userCourseList = chipsUserCoursePersistence.loadByUserId(userId);
        for (ChipsUserCourse userCourse : userCourseList) {
            String productId = userCourse.getProductId();
            OrderProductItem item = userOrderLoaderClient.loadProductItemsByProductId(productId).stream().findFirst().orElse(null);
            if (item == null || StringUtils.isBlank(item.getAppItemId())) {
                continue;
            }
            List<AIUserUnitResultHistory> aiUserBookResultList = aiUserUnitResultHistoryDao.loadByUserId(userId).stream().filter(e -> item.getAppItemId().equals(e.getBookId())).collect(Collectors.toList());
            if (aiUserBookResultList == null || aiUserBookResultList.size() < 8) {
                continue;
            }
            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
            if (orderProduct == null) {
                continue;
            }
            Date endDate = Optional.ofNullable(orderProduct.getId()).map(p -> chipsEnglishProductTimetableDao.load(p)).map(ChipsEnglishProductTimetable::getEndDate).orElse(null);
            if (endDate == null || new Date().before(endDate)) {
                continue;
            }
            sendGraduationCertificateMessage(userId, orderProduct);
        }
        return MapMessage.successMessage();
    }


    /**
     * // /chips/center/bookcatalog.vpage
     */
    @Override
    public MapMessage sendElectronictTextBookTextMessage(Long userId) {
        List<ChipsUserCourse> userCourseList = chipsUserCoursePersistence.loadByUserId(userId);
        if (CollectionUtils.isEmpty(userCourseList)) {
            return MapMessage.errorMessage().add("errorInfo", "");
        }
        for (ChipsUserCourse userCourse : userCourseList) {
            String productId = userCourse.getProductId();
            boolean isFinished = isOrderProductFinished(productId);
            if (isFinished) {
                return MapMessage.successMessage().add("url", WechatConfig.getBaseSiteUrl() + "/chips/center/bookcatalog.vpage");
            }
            if (isInviteSucess(userId)) {
                return MapMessage.successMessage().add("url", WechatConfig.getBaseSiteUrl() + "/chips/center/bookcatalog.vpage");
            }
        }
        return MapMessage.successMessage();
    }

//    public MapMessage sendElectronictTextBookTextMessageOld(Long userId) {
//        List<UserOrder> userOrderList = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), userId);
//        if (CollectionUtils.isEmpty(userOrderList)) {
//            return MapMessage.errorMessage().add("errorInfo", "");
//        }
//        for (UserOrder userOrder : userOrderList) {
//            String productId = userOrder.getProductId();
//            boolean isFinished = isOrderProductFinished(productId);
//            if (isFinished) {
//                return MapMessage.successMessage().add("url", WechatConfig.getBaseSiteUrl() + "/chips/center/bookcatalog.vpage");
//            }
//            if (isInviteSucess(userId)) {
//                return MapMessage.successMessage().add("url", WechatConfig.getBaseSiteUrl() + "/chips/center/bookcatalog.vpage");
//            }
//        }
//        return MapMessage.successMessage();
//    }

    @Override
    public MapMessage sendDailyLessonTemplateMessage(Long userId, StoneUnitData unit, OrderProduct orderProduct, String bookId) {
        if (unit == null || StringUtils.isBlank(unit.getId()) || StringUtils.isBlank(bookId)) {
            return MapMessage.successMessage();
        }
        ChipsEnglishClass clazz = getClazz(orderProduct, userId);
        if (clazz == null || clazz.getId() == null || clazz.getId() == 0L) {
            return MapMessage.successMessage();
        }
        Date unitBeginTime = loadUnitBeginTime(orderProduct, bookId, unit.getId());
        sendDailyLessonTemplateMessage(userId, unit.getId(), clazz.getTeacher(), buildUnitDisPlayName(unit), clazz.getId() + "", unitBeginTime);
        return MapMessage.successMessage();
    }

    private String getUnitNameCN(StoneUnitData unit) {
        return Optional.ofNullable(unit).map(StoneUnitData::getJsonData).map(StoneUnitData.Unit::getName).orElse("");
    }

    private String getUnitNameEN(StoneUnitData unit) {
        return Optional.ofNullable(unit).map(StoneUnitData::getJsonData).map(StoneUnitData.Unit::getTitle).orElse("");
    }

    private String buildUnitDisPlayName(StoneUnitData unit) {
        if (unit == null) {
            return "";
        }
        String unitNameCN = getUnitNameCN(unit);
        String unitNameEN = getUnitNameEN(unit);
        if (StringUtils.isBlank(unitNameCN) && StringUtils.isBlank(unitNameEN)) {
            return "";
        }
        if (StringUtils.isBlank(unitNameCN)) {
            return unitNameEN;
        }
        if (StringUtils.isBlank(unitNameEN)) {
            return unitNameCN;
        }
        return unitNameEN + "(" + unitNameCN + ")";
    }

    /***
     * 获取该product下学生所在的班级
     */
    private ChipsEnglishClass getClazz(OrderProduct product, Long userId) {
        if (product == null || StringUtils.isEmpty(product.getId()) || userId == null || userId == 0L) {
            return null;
        }
        return chipsUserService.loadClazzIdByUserAndProduct(userId, product.getId());
    }

    private void sendDailyLessonTemplateMessage(Long userId, String unitId, String teacherName, String lessonTitle, String clazzId, Date date) {
        Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
        templateDataMap.put("first", new WechatTemplateData("今日学习内容新鲜出炉！\n\r", "#FF6551"));
        templateDataMap.put("keyword1", new WechatTemplateData(DateUtils.dateToString(date == null ? new Date() : date, "MM月dd日"), null));
        templateDataMap.put("keyword2", new WechatTemplateData(teacherName == null ? "" : teacherName, null));
        templateDataMap.put("keyword3", new WechatTemplateData(lessonTitle == null ? "" : lessonTitle, null));
        templateDataMap.put("remark", new WechatTemplateData("\n\r→快来点击查看今日优秀学员以及数据总结.", "#FF6551"));
        Map<String, Object> map = new HashMap<>();
        map.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/todaystudy.vpage?clazzId=" + clazzId + "&unitId=" + unitId);
        wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_COURSE_DAILY_LESSON.name(), templateDataMap, map);
    }

    /**
     * 该用户是否成功推荐过该product
     */
    private boolean isInviteSucess(Long userId) {
        if (userId == null || userId == 0L) {
            return false;
        }
        List<ChipEnglishInvitation> invitationList = chipEnglishInvitationPersistence.loadByInviterId(userId);
        return CollectionUtils.isNotEmpty(invitationList);
    }

    private boolean isOrderProductFinished(String productId) {
        if (StringUtils.isBlank(productId)) {
            return false;
        }
        Date endDate = Optional.ofNullable(productId).map(p -> chipsEnglishProductTimetableDao.load(p)).map(ChipsEnglishProductTimetable::getEndDate).orElse(null);
        return endDate != null && !new Date().before(endDate);
    }

//    private void sendGraduationCertificateMessageOld(Long userId, UserOrder userOrder, OrderProduct orderProduct) {
//        Date endDate = Optional.ofNullable(orderProduct).filter(p -> StringUtils.isNotBlank(p.getId()))
//                .map(p -> chipsEnglishProductTimetableDao.load(p.getId())).map(ChipsEnglishProductTimetable::getEndDate).orElse(new Date());
//        Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
//        templateDataMap.put("first", new WechatTemplateData("恭喜宝贝毕业啦！\n\r", "#FF6551"));
//        templateDataMap.put("keyword1", new WechatTemplateData(userOrder.getProductName(), null));
//        templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(endDate, "MM月dd日"), null));
//        templateDataMap.put("remark", new WechatTemplateData("\n\r→点击领取毕业证书，晒晒孩子的学习成果~", "#FF6551"));
//        Map<String, Object> map = new HashMap<>();
//        String userName = Optional.ofNullable(userLoaderClient.loadUser(userId))
//                .filter(e -> e.getProfile() != null)
//                .map(User::getProfile)
//                .map(UserProfile::getNickName)
//                .filter(StringUtils::isNotBlank)
//                .orElse("");
//        map.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/getcertificate.vpage?user=" + userName);
//        try {
//            wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_COURSE_GRADUATION_CERTIFICATE.name(), templateDataMap, map);
//        } catch (Exception e) {
//            logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
//        }
//    }

    private void sendGraduationCertificateMessage(Long userId, OrderProduct orderProduct) {
        Date endDate = Optional.ofNullable(orderProduct).filter(p -> StringUtils.isNotBlank(p.getId()))
                .map(p -> chipsEnglishProductTimetableDao.load(p.getId())).map(ChipsEnglishProductTimetable::getEndDate).orElse(new Date());
        Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
        templateDataMap.put("first", new WechatTemplateData("恭喜宝贝毕业啦！\n\r", "#FF6551"));
        templateDataMap.put("keyword1", new WechatTemplateData(orderProduct.getName(), null));
        templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(endDate, "MM月dd日"), null));
        templateDataMap.put("remark", new WechatTemplateData("\n\r→点击领取毕业证书，晒晒孩子的学习成果~", "#FF6551"));
        Map<String, Object> map = new HashMap<>();
        String userName = Optional.ofNullable(userLoaderClient.loadUser(userId))
                .filter(e -> e.getProfile() != null)
                .map(User::getProfile)
                .map(UserProfile::getNickName)
                .filter(StringUtils::isNotBlank)
                .orElse("");
        map.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/getcertificate.vpage?user=" + userName);
        try {
            wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_COURSE_GRADUATION_CERTIFICATE.name(), templateDataMap, map);
        } catch (Exception e) {
            logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
        }
    }

    /**
     * @param userBookResultList 同一个用户的 userBookResultList 中一个bookId只会有一条记录
     */
    private AIUserBookResult filterByBookId(List<AIUserBookResult> userBookResultList, String bookId) {
        for (AIUserBookResult result : userBookResultList) {
            if (result == null || StringUtils.isBlank(result.getBookId())) {
                continue;
            }
            if (result.getBookId().equals(bookId)) {
                return result;
            }
        }
        return null;
    }

    private String converSuggestUrl(String suggestUrl) {
        if (StringUtils.isBlank(suggestUrl)) {
            return "";
        }
        if (suggestUrl.contains("http")) {
            return suggestUrl;
        }
        return "http://cdn.17zuoye.com" + (suggestUrl.startsWith("/") ? suggestUrl : ("/" + suggestUrl));
    }

    private List<Map<String, Object>> lessonHistory(List<AIUserUnitResultPlan> userPlanList, List<NewBookCatalog> units) {
        if (CollectionUtils.isEmpty(userPlanList)) {
            return Collections.emptyList();
        }

        Map<String, List<AIUserUnitResultPlan>> userPlansMap = userPlanList.stream().collect(Collectors.groupingBy(AIUserUnitResultPlan::getUnitId));

        List<Map<String, Object>> userPlans = new ArrayList<>();
        for (int i = 0; i < units.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", units.get(i).getId());
            map.put("score", MapUtils.isNotEmpty(userPlansMap) && CollectionUtils.isNotEmpty(userPlansMap.get(units.get(i).getId())) ?
                    userPlansMap.get(units.get(i).getId()).get(0).getScore() : null);
            map.put("rank", i + 1);
            userPlans.add(map);
        }
        return userPlans;
    }

    @Override
    public MapMessage sendChipsCourseDailyRankTemplateMessage(String unitId, ChipsEnglishClass chipsClazz, Collection<Long> userList, int count) {
        if (StringUtils.isBlank(unitId) || chipsClazz == null || CollectionUtils.isEmpty(userList)) {
            return MapMessage.errorMessage().add("info", "unit is null : " + (StringUtils.isBlank(unitId)) + "; clazz is null : " + (chipsClazz == null)
                    + "; userList is empty: " + (CollectionUtils.isEmpty(userList)));
        }
        String url = WechatConfig.getBaseSiteUrl() + "/chips/center/ranking.vpage" + "?id=" + unitId + "&clazz=" + chipsClazz.getId();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("url", url);
        Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
        templateDataMap.put("first", new WechatTemplateData("老师们，" + chipsClazz.getName() + "今天的排行榜出来啦 \n\r", "#FF6551"));
        templateDataMap.put("keyword1", new WechatTemplateData(DateUtils.dateToString(new Date(), "MM月dd日") + " 20:00", null));
        templateDataMap.put("keyword2", new WechatTemplateData("" + count, null));
        templateDataMap.put("remark", new WechatTemplateData("\n\r点击链接，快去看看学生的排名吧", "#FF6551"));
        for (long userId : userList) {
            try {
                wechatServiceClient.getWechatService()
                        .processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_COURSE_DAILY_RANK.name(), templateDataMap, paramMap);
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
                return MapMessage.errorMessage().add("info", e.getMessage());
            }
        }
        return MapMessage.successMessage();
    }

}
