package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsTalkLowLevelCountCacheManager;
import com.voxlearning.utopia.service.ai.cache.manager.UserPageVisitCacheManager;
import com.voxlearning.utopia.service.ai.constant.AIUnitMapType;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.exception.ProductNotExitException;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsContentDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.ai.impl.service.processor.v2.dailyclass.ChipsContentDailyClassResultProcessor;
import com.voxlearning.utopia.service.ai.impl.support.ChipCourseSupport;
import com.voxlearning.utopia.service.ai.impl.support.ConstantSupport;
import com.voxlearning.utopia.service.ai.impl.support.UserInfoSupport;
import com.voxlearning.utopia.service.ai.support.MessageConfig;
import com.voxlearning.utopia.service.ai.util.StringExtUntil;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Named
@ExposeServices({
        @ExposeService(interfaceClass = ChipsEnglishContentLoader.class, version = @ServiceVersion(version = "20181123")),
        @ExposeService(interfaceClass = ChipsEnglishContentLoader.class, version = @ServiceVersion(version = "20190107"))
})
public class ChipsEnglishContentLoaderImpl extends AbstractAiSupport implements ChipsEnglishContentLoader {

    @Inject
    private ChipsContentDailyClassResultProcessor chipsContentDailyClassResultProcessor;
    @Inject
    private AICacheSystem aICacheSystem;

    @Inject
    private ChipsEnglishUserExtSplitDao chipsEnglishUserExtSplitDao;

    @Inject
    private ChipsEnglishClassExtDao chipsEnglishClassExtDao;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private ChipCourseSupport chipCourseSupport;

    @Inject
    private AIUserLessonBookRefPersistence aiUserLessonBookRefPersistence;

    @Inject
    private ChipsTalkLowLevelCountCacheManager chipsTalkLowLevelCountCacheManager;

    @Inject
    private UserPageVisitCacheManager userPageVisitCacheManager;

    @Inject
    private ChipsVideoBlackListDao videoBlackListDao;

    private static final String OLD_TALK_VERSION_CONFIG = "chips_talk_question_app_version";

    private static final String OLD_ROLE_PLAY_VERSION_CONFIG = "chips_role_play_question_app_version";

    private static final Set<LessonType> LESSON_PLAY_TYPE = Arrays.asList(LessonType.video_conversation, LessonType.task_conversation, LessonType.Dialogue, LessonType.Task).stream().collect(Collectors.toSet());

    @Override
    public MapMessage loadDailyClassInfo(User user, String bookId, String unitId) {
        ChipsContentDailyClassContext context = chipsContentDailyClassResultProcessor.process(new ChipsContentDailyClassContext(user, unitId, bookId));
        if (context.isSuccessful()) {
            MapMessage message = MapMessage.successMessage();

            if (context.getClassInfo() != null) {
                message.add("classInfo", context.getClassInfo());
            }
            if (MapUtils.isNotEmpty(context.getExtMap())) {
                message.putAll(context.getExtMap());
            }

            if (context.getStatus() != null) {
                message.add("classStatus", context.getStatus().name());
            }
            String userName = Optional.ofNullable(user)
                    .map(User::getProfile)
                    .map(UserProfile::getNickName)
                    .orElse("");
            String userEnName = Optional.ofNullable(user)
                    .map(User::getProfile)
                    .map(UserProfile::getNickName)
                    .map(StringExtUntil::getPinyinString)
                    .filter(StringUtils::isNotBlank)
                    .orElse("Kid");
            message.put("userName", userName);
            message.put("userEnName", userEnName);
            message.put("checkIn", context.getCheckIn());
            message.put("sentenceNumber", context.getSentenceNumber());
            message.put("studyNumber", context.getStudyNumber());
            message.put("className", context.getClassName());
            message.put("summaryUrl", context.getSummaryUrl());
            message.put("mapUrl", context.getMapUrl());
            message.put("voiceRatio", context.getVoiceRadio());
            message.put("popDrawing", !userPageVisitCacheManager.getRecordIds(ConstantSupport.DRAWING_TASK_FIRST_PAGE_CACHE_KEY).contains(user.getId()));
            return message;
        } else {
            return MapMessage.errorMessage(context.getMessage());
        }
    }


    @Override
    public MapMessage loadUnitDetail(Long userId, String bookId, String unitId) {
        // 课本详情（石头堆获取）
        StoneBookData stoneBookData = Optional
                .ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(bookId)))
                .filter(MapUtils::isNotEmpty)
                .map(m -> m.get(bookId))
                .map(StoneBookData::newInstance)
                .orElse(null);
        long user;
        if (stoneBookData == null) {
            return MapMessage.errorMessage("no book data found for bookId: [bookId= {}]", bookId);
        }

        StoneBookData.Book book = stoneBookData.getJsonData();
        List<StoneBookData.Node> unitNodes = book.getChildren();

        // 单元
        StoneBookData.Node unitInfo = unitNodes.stream()
                .filter(u -> unitId.equals(u.getStone_data_id()))
                .findFirst()
                .orElse(null);

        if (unitInfo == null) {
            return MapMessage.errorMessage("no unit info found for in bookStone: [bookId= {}, unitId= {}]", bookId, unitId);
        }

        // 课程列表
        List<StoneBookData.Node> lessonNodes = unitInfo.getChildren();
        if (CollectionUtils.isEmpty(lessonNodes)) {
            return MapMessage.errorMessage("no lesson info found in bookStone: [bookId= {}, unitId= {}]", bookId, unitId);
        }

        // 单元详情(石头堆获取)
        StoneUnitData stoneLessonData = Optional
                .ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(unitId)))
                .filter(MapUtils::isNotEmpty)
                .map(m -> m.get(unitId))
                .map(StoneUnitData::newInstance)
                .orElse(null);
        if (stoneLessonData == null) {
            return MapMessage.errorMessage("no unit info found in unitStone: [unitId= {}]", unitId);
        }
        StoneUnitData.Unit unit = stoneLessonData.getJsonData();
        MapMessage mapMessage = MapMessage.successMessage()
                .add("unitName", unit.getTitle())
                .add("unitCame", unit.getSub_title())
                .add("bookId", bookId)
                .add("unitId", unitId)
                .add("goals", Optional.of(unit)
                        .filter(e -> StringUtils.isNotBlank(e.getKey_points()))
                        .map(e -> Collections.singletonList(e.getKey_points()))
                        .orElse(Collections.emptyList()))
                .add("goalAudio", unit.getKey_points_audio())
                .add("trial", chipsContentService.loadTrailUnit().contains(unitId));


        // lesson 列表
        Map<String, StoneData> lessonMap =
                stoneDataLoaderClient.loadStoneDataIncludeDisabled(lessonNodes.stream().map(StoneBookData.Node::getStone_data_id).collect(Collectors.toList()));
        if (MapUtils.isEmpty(lessonMap)) {
            return mapMessage.add("lessons", Collections.EMPTY_LIST);
        }

        // 用户的成绩信息
        List<AIUserLessonResultHistory> lessonResultHistories =
                aiUserLessonResultHistoryDao.loadByUserIdAndUnitId(userId, unitId);
        Map<String, AIUserLessonResultHistory> lessonResultHistoryMap = lessonResultHistories
                .stream()
                .collect(Collectors.toMap(AIUserLessonResultHistory::getLessonId, Function.identity(), (k1, k2) -> k2));

        List<Map<String, Object>> lessons = new ArrayList<>();
        int rank = 1;
        //课程是否锁定，第一个默认开启，下一个依赖前一个
        boolean isLock = false;
        for (StoneBookData.Node node : lessonNodes) {
            String lessonId = node.getStone_data_id();
            StoneData v = lessonMap.get(lessonId);
            AIUserLessonResultHistory lessonResultHistory = lessonResultHistoryMap.get(lessonId);
            StoneLessonData lessonData = StoneLessonData.newInstance(v);
            StoneLessonData.Lesson lesson = lessonData.getJsonData();
            Map<String, Object> map = new HashMap<>();
            map.put("id", lessonId);
            map.put("name", lesson.getName());
            map.put("rank", rank++);
            map.put("score", lessonResultHistory != null ? lessonResultHistory.getScore() : null);
            map.put("lessonType", lesson.getLesson_type());
            map.put("lessonTypeDesc", lesson.getLesson_type() != null ? lesson.getLesson_type().getDesc() : "");
            map.put("finished", lessonResultHistory != null ? lessonResultHistory.getFinished() : false);
            map.put("isLock", isLock);
            map.put("star", lessonResultHistory != null ? lessonResultHistory.getStar() : null);
            lessons.add(map);

            // 此课程未完成则下一课程为锁定状态
            isLock = isLock || lessonResultHistory == null || !lessonResultHistory.getFinished();
        }

        return mapMessage.add("lessons", lessons);
    }

    @Override
    public MapMessage loadLessonDetail(Long userId, String bookId, String unitId, String lessonId, String appVersion) {
        StoneLessonData lessonData = Optional
                .ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(lessonId)))
                .filter(MapUtils::isNotEmpty)
                .map(m -> m.get(lessonId))
                .map(StoneLessonData::newInstance)
                .orElse(null);
        if (lessonData == null) {
            return MapMessage.errorMessage("no lesson data found for bookId: [lessonId= {}]", lessonId);
        }

        boolean lastLesson = Optional.ofNullable(bookId)
                .map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e)))
                .map(e -> e.get(bookId))
                .map(StoneBookData::newInstance)
                .map(StoneBookData::getJsonData)
                .map(StoneBookData.Book::getChildren)
                .filter(CollectionUtils::isNotEmpty)
                .map(e -> {
                    StoneBookData.Node unitNode = e.stream().filter(unit -> unit.getStone_data_id().equals(unitId)).findFirst().orElse(null);
                    if (unitNode == null || CollectionUtils.isEmpty(unitNode.getChildren())) {
                        return false;
                    }
                    List<StoneBookData.Node> lessonList = unitNode.getChildren();
                    return lessonList.get(lessonList.size() - 1).getStone_data_id().equals(lessonId);
                }).orElse(false);

        StoneLessonData.Lesson lesson = lessonData.getJsonData();

        MapMessage mapMessage = MapMessage.successMessage()
                .add("title", lesson.getName())
                .add("bookId", bookId)
                .add("unitId", unitId)
                .add("unitType", Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(unitId)))
                        .filter(MapUtils::isNotEmpty)
                        .map(m -> m.get(unitId))
                        .map(StoneUnitData::newInstance)
                        .map(StoneUnitData::getJsonData)
                        .map(StoneUnitData.Unit::getUnit_type)
                        .map(ChipsUnitType::name)
                        .orElse(""))
                .add("lessonId", lessonId)
                .add("lessonType", lesson.getLesson_type())
                .add("lastLesson", lastLesson)
                .add("background", lesson.getBackground_intro())
                .add("image", lesson.getBackground_image())
                .add("backgroundAudio", lesson.getIntro_audio())
                .add("goal", lesson.getTarget())
                .add("goalAudio", lesson.getTarget_audio());
        List<StoneData> questionList = new ArrayList<>();

        List<String> contendIds = lesson.getContent_ids();
        if (CollectionUtils.isNotEmpty(contendIds)) {
            Map<String, StoneData> questionStones = stoneDataLoaderClient.loadStoneDataIncludeDisabled(contendIds);
            for (String string : contendIds) {
                StoneData stoneData = Optional.ofNullable(questionStones)
                        .map(e -> e.get(string))
                        .filter(e -> e.getJsonData() != null)
                        .map((StoneData e) -> {
                            e.setJsonData(e.getJsonData().replace("http://cdn.17zuoye.com", "https://cdn.17zuoye.com"));
                            return e;
                        })
                        .orElse(null);
                if (stoneData == null) {
                    continue;
                }
                ChipsQuestionType type = ChipsQuestionType.of(stoneData.getSchemaName());
                if (type == null || type == ChipsQuestionType.unknown) {
                    continue;
                }
                // Shuffle
                shuffleQuestionOptions(stoneData);
                questionList.add(stoneData);
            }
        }

        String versionConfig = Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(OLD_TALK_VERSION_CONFIG))
                .map(ChipsEnglishPageContentConfig::getValue)
                .orElse("2.4.6");
        boolean oldApp = VersionUtil.compareVersion(appVersion, versionConfig) < 0;
        switch (lesson.getLesson_type()) {
            case video_conversation:
            case Dialogue:
                String sessionId = aICacheSystem.getUserDialogueTalkSceneResultCacheManager().genSessionId(userId, lessonId);
                aICacheSystem.getUserTalkFeedSessionCacheManager().saveSessionId(userId, lessonId, sessionId);
                chipsTalkLowLevelCountCacheManager.delete(questionList.stream().map(StoneData::getId).collect(Collectors.toList()), userId);
                questionList = questionList.stream()
                        .filter(e -> (oldApp && e.getSchemaName().equals(ChipsQuestionType.video_conversation.name())) || //old
                                (!oldApp && !e.getSchemaName().equals(ChipsQuestionType.task_conversation.name()) && !e.getSchemaName().equals(ChipsQuestionType.video_conversation.name())))
                        .collect(Collectors.toList());
                break;
            case mock_test_lesson_1:
            case mock_test_lesson_2:
                aICacheSystem.getUserTalkFeedSessionCacheManager().genSessionId(userId, lessonId);
                break;
            case task_conversation:
            case Task:
                questionList = questionList.stream()
                        .filter(e -> (oldApp && e.getSchemaName().equals(ChipsQuestionType.task_conversation.name())) || //old
                                (!oldApp && !e.getSchemaName().equals(ChipsQuestionType.task_conversation.name()) && !e.getSchemaName().equals(ChipsQuestionType.video_conversation.name())))
                        .collect(Collectors.toList());
                break;
            case role_play_lesson:
                aICacheSystem.getUserTalkFeedSessionCacheManager().genSessionId(userId, lessonId);
                String roleVersion = Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(OLD_ROLE_PLAY_VERSION_CONFIG))
                        .map(ChipsEnglishPageContentConfig::getValue)
                        .orElse("2.8.5");
                boolean roleOld = VersionUtil.compareVersion(appVersion, roleVersion) < 0;
                questionList = questionList.stream()
                        .filter(e -> {
                            if (!roleOld) {
                                return true;
                            }
                            return !ChipsQuestionType.scene_interlude.name().equals(e.getCustomName());
                        })
                        .collect(Collectors.toList());

                break;
        }

        return mapMessage.add("questions", questionList);
    }

    private void shuffleQuestionOptions(StoneData stoneData) {
        ChipsQuestionType qtype = ChipsQuestionType.of(stoneData.getSchemaName());

        // shuffle options
        switch (qtype) {
            case choice_sentence2audio:
            case choice_sentence2pic:
            case choice_word2pic:
            case choice_word2trans:
            case choice_cultural:
            case choice_cultural2pic:
            case mock_choice:
            case mock_choice_audio:
                Map<String, Object> question = JsonUtils.fromJson(stoneData.getJsonData());

                List options = Optional.ofNullable(question).map(x -> x.get("options")).map(x -> (List) x).orElse(Collections.EMPTY_LIST);

                if (options.size() > 0) {
                    // shuffle
                    Collections.shuffle(options);
                    question.put("options", options);
                    // rewrite
                    stoneData.setJsonData(JsonUtils.toJson(question));
                }
                break;
            default:
                // do not nothing
                break;
        }

    }

    private boolean isBlackUser(Long userId) {
        if (userId == null) {
            return false;
        }
        Set<Long> blackList = videoBlackListDao.loadAll().stream().map(e -> e.getId()).collect(Collectors.toSet());
        return blackList.contains(userId);
    }

    @Override
    public MapMessage loadUnitResult(User user, String bookId, String unitId) {
        Assertions.notNull(user, "uid must not null");
        Assertions.notNull(unitId, "unitId must not null");

        MapMessage mm = MapMessage.successMessage();

        Long userId = user.getId();

        AIUserUnitResultHistory unitResultHistory = aiUserUnitResultHistoryDao.load(userId, unitId);
        if (unitResultHistory == null) {
            return MapMessage.errorMessage("数据不存在");
        }
        StoneBookData bookData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(bookId)))
                .map(e -> e.get(bookId))
                .map(StoneBookData::newInstance)
                .orElse(null);
        if (bookData == null) {
            return MapMessage.errorMessage("教材不存在");
        }
        //unit
        Set<String> lessonIds = Optional.of(bookData)
                .map(StoneBookData::getJsonData)
                .map(StoneBookData.Book::getChildren)
                .filter(CollectionUtils::isNotEmpty)
                .map(e -> {
                    //从单元节点取出来所有的lesson
                    Set<String> set = new HashSet<>();
                    e.stream().filter(e1 -> CollectionUtils.isNotEmpty(e1.getChildren()))
                            .forEach(e1 -> set.addAll(e1.getChildren().stream()
                                    .map(StoneBookData.Node::getStone_data_id)
                                    .collect(Collectors.toSet())));
                    return set;
                })
                .orElse(Collections.emptySet());
        List<AIUserLessonResultHistory> lessonResult = aiUserLessonResultHistoryDao.loadByUserIdAndUnitId(userId, unitId);
        // 获取情景对话视频地址
        if (CollectionUtils.isNotEmpty(lessonResult) && StringUtils.isBlank(unitResultHistory.getVideo())) {
            lessonResult.stream().filter(e -> CollectionUtils.isNotEmpty(lessonIds) && lessonIds.contains(e.getLessonId()))
                    .filter(f -> f.getLessonType() == LessonType.Dialogue || f.getLessonType() == LessonType.video_conversation
                            || f.getLessonType() == LessonType.role_play_lesson
                            || f.getLessonType() == LessonType.mock_test_lesson_2
                            || f.getLessonType() == LessonType.mock_test_lesson_1)
                    .findFirst().ifPresent(lessonResultHistory -> {
                unitResultHistory.setVideo(lessonResultHistory.getUserVideo());
                unitResultHistory.setUserVideoId(lessonResultHistory.getUserVideoId());
            });
        }
        if (unitResultHistory.getPronunciation() != null && unitResultHistory.getPronunciation() <= 8) {
            Integer pronunciation = new BigDecimal(unitResultHistory.getPronunciation()).divide(new BigDecimal(8), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
            unitResultHistory.setPronunciation(pronunciation);
        }
        boolean blackUser = isBlackUser(userId);
        if (blackUser) {
            unitResultHistory.setVideo("BLACK");
        }
        Map<String, Object> drawingContent = new HashMap<>();
        mm.put("report", unitResultHistory);
        boolean popDrawing = false;
        if (unitResultHistory.getDrawingTaskId() != null) {
            ChipsUserDrawingTask task = chipsUserDrawingTaskPersistence.load(unitResultHistory.getDrawingTaskId());
            popDrawing = task != null && Boolean.FALSE.equals(task.getShare());
            drawingContent = Optional.ofNullable(task)
                    .map(ChipsUserDrawingTask::getDrawingId)
                    .filter(StringUtils::isNotBlank)
                    .map(dId -> {
                        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(dId));
                        if (MapUtils.isEmpty(stoneDataMap)) {
                            return null;
                        }
                        return stoneDataMap.get(dId);
                    })
                    .map(StoneQuestionData::newInstance)
                    .map(StoneQuestionData::getJsonData)
                    .filter(MapUtils::isNotEmpty)
                    .orElse(Collections.emptyMap());
        }
        mm.put("drawingTaskPop", popDrawing);
        mm.put("drawingContent", drawingContent);

        // lesson
        List<Map<String, Object>> lessonMapList = new ArrayList<>();
        lessonResult.stream()
                .filter(lesson -> lesson.getScore() != null && lesson.getScore() >= 0)
                .filter(e -> CollectionUtils.isNotEmpty(lessonIds) && lessonIds.contains(e.getLessonId())).forEach(lesson -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", lesson.getLessonId());
            map.put("score", lesson.getScore());
            map.put("lessonType", lesson.getLessonType());
            map.put("lessonTypeDesc", Optional.ofNullable(lesson.getLessonType()).map(LessonType::getDesc).orElse(""));
            lessonMapList.add(map);
        });
        mm.put("lessonResult", lessonMapList);

        // question
        List<Map<String, Object>> questionsMap = new ArrayList<>();
        List<AIUserQuestionResultHistory> questionResult = aiUserQuestionResultHistoryDao.loadByUidAndUnitId(userId, unitId);
        if (CollectionUtils.isNotEmpty(lessonIds) && CollectionUtils.isNotEmpty(questionResult)) {
            Set<String> questionIdSet = new HashSet<>();
            stoneDataLoaderClient.loadStoneDataIncludeDisabled(lessonIds).values().stream().map(StoneLessonData::newInstance)
                    .filter(e -> e.getJsonData() != null)
                    .filter(e -> CollectionUtils.isNotEmpty(e.getJsonData().getContent_ids()))
                    .forEach(e -> questionIdSet.addAll(e.getJsonData().getContent_ids()));
            Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(questionIdSet);

            questionResult.stream().filter(e -> questionIdSet.contains(e.getQid())).forEach(question -> {
                Map<String, Object> map = new HashMap<>();
                String id = question.getQid();
                StoneData data = stoneDataMap.get(id);
                if (data == null) {
                    return;
                }
                map.put("id", id);
                map.put("jsonData", data.getJsonData());
                map.put("schemaName", data.getSchemaName());
                String userAnswer = Optional.ofNullable(question.getUserAudio()).filter(e -> !e.isEmpty()).map(e -> e.get(0)).orElse("");
                if (question.getMaster() != null) {
                    userAnswer = question.getUserAnswer();
                }
                map.put("userAnswer", userAnswer);
                map.put("score", question.getScore());
                questionsMap.add(map);
            });
        }
        mm.put("questions", questionsMap);

        boolean showPlay = isShowPlay(bookId, userId);
        mm.put("showPlay", showPlay);

        StoneUnitData unitData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(unitId))).map(e -> e.get(unitId))
                .map(StoneUnitData::newInstance)
                .orElse(null);
        String unitName = Optional.ofNullable(unitData).map(StoneUnitData::getJsonData).map(StoneUnitData.Unit::getName).orElse("");
        mm.put("unitName", unitName);
        String unitImage = Optional.ofNullable(unitData).map(StoneUnitData::getJsonData).map(StoneUnitData.Unit::getCover_image).orElse("");
        mm.put("unitImage", unitImage);
        mm.put("studentName", Optional.ofNullable(user.getProfile()).map(UserProfile::getNickName).orElse(""));
        mm.put("avatar", UserInfoSupport.getUserRoleImage(user));

        int total = aiUserUnitResultHistoryDao.loadByUserId(userId).stream()
                .filter(e -> StringUtils.isNotBlank(e.getBookId()))
                .filter(e -> e.getBookId().equals(unitResultHistory.getBookId()))
                .collect(Collectors.toList()).size();
        mm.put("totalFinish", total);

        return mm;
    }

    private boolean isShowPlay(String bookId, Long userId) {
        // 1、现在短期课电子教材是对所有用户开放的，需要关闭掉未开课用户。
        // 2、现在可以得到电子教材的是三种用户1.推荐好友报名的用户；2已经完课的用户，3旅行口语二期的用户(此条件不用判断)。
        // 3、运营人员在crm后台配置了发送
        // 4、超级用户
        if (chipsUserService.isInWhiteList(userId)) {
            return true;
        }
        ChipEnglishInvitation chipEnglishInvitation = chipEnglishInvitationPersistence.loadByInviterId(userId).stream().findFirst().orElse(null);
        if (chipEnglishInvitation != null) {
            return true;
        }
        ChipsEnglishUserExtSplit userExtSplit = chipsEnglishUserExtSplitDao.load(userId);
        if (userExtSplit != null && userExtSplit.getShowPlay() != null && userExtSplit.getShowPlay()) {
            return true;
        }
        return Optional.ofNullable(bookId)
                .map(e -> ifAllUnitFinished(userId, e))
                .orElse(false);
    }

    @Override
    public MapMessage loadUnitMap(Long userId, String appVersion) {
        if (userId == null || userId == 0L) {
            return MapMessage.errorMessage("没有获得用户").set("result", "401");
        }
        AIUserLessonBookRef bookRef = getUserMapBookRef(userId);
        if (bookRef == null || StringUtils.isBlank(bookRef.getBookId())) {
            return MapMessage.errorMessage("无购买信息").set("result", "401");
        }

        ChipsEnglishPageContentConfig urlConfig = chipsEnglishConfigService.loadChipsConfigByName("chipsEnglishMapUrlToMap");
        List<String> toMapUrlBookIdList = new ArrayList<>();
        if (urlConfig != null && StringUtils.isNotBlank(urlConfig.getValue())) {
            List<String> urls = JsonUtils.fromJsonToList(urlConfig.getValue(), String.class);
            if (CollectionUtils.isNotEmpty(urls)) {
                toMapUrlBookIdList = urls;
            }
        }

        //TODO bookId and showType
        AIUnitMapType showType = toMapUrlBookIdList.contains(bookRef.getBookId()) && !ChipCourseSupport.TRAVEL_ENGLISH_BOOK_ID.equals(bookRef.getBookId()) ?
                AIUnitMapType.SHOW_EIGHT : AIUnitMapType.SHOW_NORMAL;

        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(bookRef.getProductId());
        if (orderProduct == null) {
            return MapMessage.errorMessage(bookRef.getProductId() + " can not get OrderProduct").set("result", "401");
        }

        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(bookRef.getProductId());

        Date beginDate = Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getBeginDate).orElse(new Date());
        if (StringUtils.isNotBlank(appVersion) && VersionUtil.compareVersion(appVersion, "2.4.3") < 0 && !ChipCourseSupport.TRAVEL_ENGLISH_BOOK_ID.equals(bookRef.getBookId())) {
            return MapMessage.successMessage()
                    .add("mapList", Collections.emptyList())
                    .add("bookId", bookRef.getBookId())
                    .add("showType", showType)
                    .add("beginDate", DateUtils.dateToString(beginDate, "MM月dd日"));
        }

        List<AIUserUnitResultHistory> userUnitResultHistoryList = aiUserUnitResultHistoryDao.loadByUserId(userId);
        List<StoneUnitData> unitList = chipCourseSupport.fetchUnitListExcludeTrialV2(bookRef.getBookId());
        if (CollectionUtils.isEmpty(unitList)) {
            return MapMessage.successMessage().add("mapList", Collections.emptyList())
                    .add("bookId", bookRef.getBookId())
                    .add("showType", showType)
                    .add("beginDate", DateUtils.dateToString(beginDate, "MM月dd日"))
                    .add("reviewRedDot", false);
        }

//        reviewRedDot = isRedDot(userId, OrderProductUtil.parseIntegerTypeFieldFromAttr(orderProduct, "grade") > 0);
        boolean reviewRedDot = isRedDot(userId, orderProduct.getId());
        boolean iswhite = chipsUserService.isInWhiteList(userId);
        List<ChipsEnglishClassInfo> dataList;
        //先查看一下课表
        if (timetable != null && CollectionUtils.isNotEmpty(timetable.getCourses())) {
            dataList = buildChipsEnglishClassInfo(unitList, timetable.getCourses().stream().collect(Collectors.toMap(ChipsEnglishProductTimetable.Course::getUnitId, e -> e)), iswhite, userUnitResultHistoryList);
        } else {
            dataList = Collections.emptyList();
//            dataList = buildChipsEnglishClassInfo(orderProduct.getId(), unitList, iswhite, userUnitResultHistoryList);
        }
        return MapMessage.successMessage()
                .add("mapList", dataList)
                .add("beginDate", DateUtils.dateToString(beginDate, "MM月dd日"))
                .add("bookId", bookRef.getBookId())
                .add("showType", showType)
                .add("reviewRedDot", reviewRedDot);
    }

    @Override
    public MapMessage loadUnitMapByBook(Long userId, String bookId) {
        if (userId == null || StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("参数为空");
        }
        List<AIUserUnitResultHistory> userUnitResultHistoryList = aiUserUnitResultHistoryDao.loadByUserId(userId).stream().filter(e -> e.getBookId().equals(bookId)).collect(Collectors.toList());

        List<StoneUnitData> unitDataList = chipCourseSupport.fetchUnitListExcludeTrialV2(bookId);

        return MapMessage.successMessage().set("mapList", buildChipsEnglishClassInfo(unitDataList, userUnitResultHistoryList));
    }

    @Override
    public MapMessage loadUnitShareInfo(Long userId, String unitId, String bookId) {
        int studyNum = aiUserUnitResultHistoryDao.loadByUserId(userId).stream()
                .filter(e -> !chipsContentService.isTrailUnit(e.getUnitId()))
                .map(AIUserUnitResultHistory::getUnitId)
                .collect(Collectors.toSet()).size();

        Long sentenceNum = Optional.ofNullable(chipsEnglishUserExtSplitDao.load(userId))
                .map(ChipsEnglishUserExtSplit::getSentenceLearn)
                .orElse(0L);
        ChipsEnglishPageContentConfig config = chipsEnglishConfigService.loadChipsConfigByName("chipsSignInShare");
        Map<String, Object> configMap = Optional.ofNullable(config)
                .map(e -> JsonUtils.fromJson(e.getValue()))
                .orElse(Collections.emptyMap());
        User user = userLoaderClient.loadUser(userId);
        String userImg = UserInfoSupport.getUserRoleImage(user);
        String userName = Optional.ofNullable(user).map(User::getProfile).map(UserProfile::getNickName).orElse("我");
        String unitName = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(unitId)))
                .map(e -> e.get(unitId))
                .map(StoneUnitData::newInstance)
                .map(StoneUnitData::getJsonData)
                .map(StoneUnitData.Unit::getUnit_type)
                .map(ChipsUnitType::getDesc)
                .orElse("薯条英语");
        String bookName = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(bookId)))
                .map(e -> e.get(bookId))
                .map(StoneBookData::newInstance)
                .map(StoneBookData::getJsonData)
                .map(StoneBookData.Book::getName)
                .orElse("薯条英语");
        String link = Optional.of(configMap)
                .map(e -> e.get("link"))
                .map(SafeConverter::toString)
                .map(e -> e + "?studyNum=" + studyNum + "&sentenceNum="
                        + sentenceNum + "&userName=" + userName + "&unitName=" + unitName + "&bookName=" + bookName + "&userImg=" + userImg)
                .orElse("");

        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.putAll(configMap);
        mapMessage.put("link", link);
        String title = Optional.of(configMap)
                .map(e -> e.get("title"))
                .map(SafeConverter::toString)
                .map(e -> MessageFormat.format(e, userName, studyNum))
                .orElse("");
        mapMessage.put("title", title);
        return mapMessage;
    }

    @Override
    public String loadCurrentUnitId(Long clazzId) {
        if (clazzId == null) {
            return null;
        }
        ChipsEnglishClassExt classExt = chipsEnglishClassExtDao.load(clazzId);
        List<ChipsEnglishProductTimetable.Course> courses = classExt.getCourses();
        if (CollectionUtils.isEmpty(courses)) {
            return null;
        }
        DayRange current = DayRange.current();
        for (ChipsEnglishProductTimetable.Course course : courses) {
            if (current.contains(course.getBeginDate())) {
                return course.getUnitId();
            }
        }
        return null;
    }

    private boolean ifAllUnitFinished(Long userId, String bookId) {
        List<AIUserUnitResultHistory> unitResultHistoryList = aiUserUnitResultHistoryDao.loadByUserId(userId);
        if (CollectionUtils.isNotEmpty(unitResultHistoryList)) {
            // 拿到 book 所有的 unit 数量，排除试用单元
            int unitSize = chipCourseSupport.fetchUnitListExcludeTrialV2(bookId).size();

            return unitResultHistoryList
                    .stream()
                    .filter(e -> !chipsContentService.isTrailUnit(e.getUnitId())) // 排除试用单元
                    .filter(e -> StringUtils.equals(e.getBookId(), bookId))
                    .filter(e -> Boolean.TRUE.equals(e.getFinished()))
                    .collect(Collectors.toList()).size() >= unitSize;
        }
        return false;
    }

    /**
     * 获取用户地图页需要展示的book信息
     */
    private AIUserLessonBookRef getUserMapBookRef(Long userId) {
        try {
            return chipsUserService.fetchOrInitBookRef(userId);
        } catch (ProductNotExitException e) {
           return null;
        }
    }

    private boolean isRedDot(Long userId, String productId) {
        if (userId == null || StringUtils.isBlank(productId)) {
            return false;
        }
        //过滤掉unitid或beginDate未空的
        List<ChipsEnglishProductTimetable.Course> courseList = Optional.ofNullable(chipsEnglishProductTimetableDao.load(productId)).filter(l -> CollectionUtils.isNotEmpty(l.getCourses()))
                .map(l -> l.getCourses().stream().filter(c -> c.getBeginDate() != null && StringUtils.isNotBlank(c.getUnitId())).collect(Collectors.toList())).orElse(null);
        if (CollectionUtils.isEmpty(courseList)) {
            return false;
        }
        Date now = new Date();
        if (now.before(courseList.get(0).getBeginDate())) {//未开课
            return false;
        }
        if ((courseList.get(courseList.size() - 1).getBeginDate().getTime() + 24 * 60 * 60 * 1000) <= now.getTime()) {//已结课
            return false;
        }
        DayRange current = DayRange.current();
        ChipsEnglishProductTimetable.Course course = courseList.stream().filter(c -> current.contains(c.getBeginDate())).findFirst().orElse(null);
        if (course != null) {
            return false;
        }
        String value = aICacheSystem.getAiLoaderRedDotCacheManager().read(userId);
        if (StringUtils.isEmpty(value)) {
            aICacheSystem.getAiLoaderRedDotCacheManager().save(userId, "true");
            return true;
        }
        return false;
    }

    private List<Map<String, Object>> buildChipsEnglishClassInfo(List<StoneUnitData> unitList, List<AIUserUnitResultHistory> userUnitResultHistoryList) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < unitList.size(); i++) {
            StoneUnitData unit = unitList.get(i);
            if (unit == null) {
                continue;
            }
            Map<String, Object> classInfo = new HashMap<>();
            classInfo.put("id", unit.getId());
            classInfo.put("name", Optional.ofNullable(unit.getJsonData()).map(StoneUnitData.Unit::getName).orElse(""));
            classInfo.put("img", Optional.ofNullable(unit.getJsonData()).map(StoneUnitData.Unit::getCover_image).orElse(""));
            classInfo.put("rank", i + 1);
            AIUserUnitResultHistory unitResult = Optional.ofNullable(userUnitResultHistoryList)
                    .map(m -> m.stream().filter(u -> StringUtils.equals(unit.getId(), u.getUnitId()))
                            .findFirst().orElse(null))
                    .orElse(null);
            classInfo.put("star", (unitResult == null || unitResult.getStar() == null ? 0 : unitResult.getStar()));
            classInfo.put("finished", unitResult != null);
            dataList.add(classInfo);
        }
        return dataList;
    }

    private List<ChipsEnglishClassInfo> buildChipsEnglishClassInfo(List<StoneUnitData> unitList, Map<String, ChipsEnglishProductTimetable.Course> unitDateMap, boolean iswhite, List<AIUserUnitResultHistory> userUnitResultHistoryList) {
        List<ChipsEnglishClassInfo> dataList = new ArrayList<>();
        for (int i = 0; i < unitList.size(); i++) {
            StoneUnitData unit = unitList.get(i);
            if (unit == null) {
                continue;
            }
            ChipsEnglishClassInfo classInfo = new ChipsEnglishClassInfo();
            classInfo.setId(unit.getId());
            if (unit.getJsonData() != null) {
                classInfo.setName(unit.getJsonData().getName());
                classInfo.setImg(unit.getJsonData().getCover_image());
            }
            classInfo.setRank(i + 1);
            classInfo.setType(Optional.of(unit).map(StoneUnitData::getJsonData).map(StoneUnitData.Unit::getUnit_type).map(ChipsUnitType::name).orElse(""));
            classInfo.setTypeDesc(Optional.of(unit).map(StoneUnitData::getJsonData).map(StoneUnitData.Unit::getUnit_type).map(ChipsUnitType::getDesc).orElse(""));
            classInfo.setLock(Optional.ofNullable(unitDateMap)
                    .map(e -> e.get(unit.getId()))
                    .map(ChipsEnglishProductTimetable.Course::getBeginDate)
                    .map(e -> isLock(iswhite, e))
                    .orElse(false));
            AIUserUnitResultHistory unitResult = Optional.ofNullable(userUnitResultHistoryList)
                    .map(m -> m.stream().filter(u -> StringUtils.equals(unit.getId(), u.getUnitId()))
                            .findFirst().orElse(null))
                    .orElse(null);
            classInfo.setStar(unitResult == null || unitResult.getStar() == null ? null : unitResult.getStar());
            classInfo.setFinished(unitResult != null);
            classInfo.setCurrentDay(Optional.ofNullable(unitDateMap)
                    .map(e -> e.get(unit.getId()))
                    .map(ChipsEnglishProductTimetable.Course::getBeginDate)
                    .map(e -> DayRange.current().contains(e))
                    .orElse(false));
            dataList.add(classInfo);
        }
        return dataList;
    }

    private boolean isLock(boolean iswhite, Date openDate) {
        if (iswhite) {
            return false;
        }
        if (openDate == null) {
            return true;
        }
        Date now = getCurrentDate();
        return now.before(openDate);
    }

    private Date getCurrentDate() {
        return new Date();
    }

    @Override
    public List<StoneUnitData> fetchUnitListExcludeTrialV2(String bookId) {
        return chipCourseSupport.fetchUnitListExcludeTrialV2(bookId);
    }

    @Override
    public ChipsEnglishProductTimetable loadChipsEnglishProductTimetableById(String productId) {
        return chipsEnglishProductTimetableDao.load(productId);
    }

    @Override
    public Map<String, ChipsEnglishProductTimetable> loadChipsEnglishProductTimetableByIds(Collection<String> productIds) {
        return chipsEnglishProductTimetableDao.loads(productIds);
    }

    @Override
    public List<StoneUnitData> loadValidBeginUnitByBookIdSortWithRank(OrderProduct orderProduct, String bookId) {
        if (orderProduct == null || StringUtils.isBlank(orderProduct.getId()) || StringUtils.isBlank(bookId)) {
            return null;
        }
        // 获取教材课单元
        List<StoneUnitData> unitList = chipCourseSupport.fetchUnitListExcludeTrialV2(bookId);
        if (CollectionUtils.isEmpty(unitList)) {
            return null;
        }
        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(orderProduct.getId());
        if (timetable == null || CollectionUtils.isEmpty(timetable.getCourses())) {
            return null;
        }
        //计算开课的时间
        Map<String, Date> unitIdToBeginDateMap = timetable.getCourses().stream().collect(Collectors.toMap(ChipsEnglishProductTimetable.Course::getUnitId, ChipsEnglishProductTimetable.Course::getBeginDate));
        Date now = getCurrentDate();
        return unitList.stream().filter(e -> e != null && StringUtils.isNotBlank(e.getId())).filter(e -> unitIdToBeginDateMap.get(e.getId()) != null && unitIdToBeginDateMap.get(e.getId()).before(now)).collect(Collectors.toList());
    }

    @Override
    public StoneUnitData loadTodayStudyUnit(String productId) {
        String unitId = loadTodayStudyUnitId(productId);
        if (StringUtils.isBlank(unitId)) {
            return null;
        }
        return Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(unitId))).map(m -> m.get(unitId)).map(StoneUnitData::newInstance).orElse(null);
    }

    @Override
    public MapMessage loadTalkRoleQuestionList(Long userId, String userCode, String roleName, String lessonId, String unitId, String bookId) {
        StoneTalkNpcQuestionData npcquestion = chipCourseSupport.fetchTaskNpcStoneData(lessonId, roleName);
        if (npcquestion == null) {
            return MapMessage.errorMessage("no question data");
        }
        List<StoneData> questions = chipCourseSupport.fetchTaskRoleStoneData(npcquestion);
        if (CollectionUtils.isEmpty(questions)) {
            return MapMessage.errorMessage("no question data");
        }

        chipsTalkLowLevelCountCacheManager.delete(questions.stream().map(StoneData::getId).collect(Collectors.toList()), userId);

        aICacheSystem.getUserTaskTalkSceneResultCacheManager().resetCache(userId, lessonId, roleName, userCode);
        return MapMessage.successMessage().set("questions", questions)
                .set("npcBackground", Optional.ofNullable(npcquestion.getJsonData()).map(StoneTalkNpcQuestionData.Npc::getBackground_image).orElse(""))
                .set("lessonType", Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singleton(lessonId)))
                        .map(e -> e.get(lessonId))
                        .map(StoneLessonData::newInstance)
                        .map(StoneLessonData::getJsonData)
                        .map(StoneLessonData.Lesson::getLesson_type)
                        .map(LessonType::name)
                        .orElse(""));
    }

    private String loadTodayStudyUnitId(String productId) {
        if (StringUtils.isBlank(productId)) {
            return null;
        }
        List<ChipsEnglishProductTimetable.Course> courseList = Optional.ofNullable(chipsEnglishProductTimetableDao.load(productId)).map(ChipsEnglishProductTimetable::getCourses).orElse(null);
        if (CollectionUtils.isEmpty(courseList)) {
            return null;
        }
        return courseList.stream().filter(c -> c.getBeginDate() != null && DayRange.current().contains(c.getBeginDate())).map(ChipsEnglishProductTimetable.Course::getUnitId).findFirst().orElse(null);
    }

    public Set<ChipsUnitType> getAllChipsUnitType(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return Collections.emptySet();
        }
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(bookId));
        if (MapUtils.isEmpty(stoneDataMap) || CollectionUtils.isEmpty(stoneDataMap.values())) {
            return Collections.emptySet();
        }
        Set<String> unitSet = stoneDataMap.values().stream().filter(Objects::nonNull).map(StoneBookData::newInstance).map(StoneBookData::getJsonData).filter(Objects::nonNull)
                .map(StoneBookData.Book::getChildren).filter(CollectionUtils::isNotEmpty)
                .flatMap(l -> l.stream().filter(n -> n != null && StringUtils.isNotBlank(n.getStone_data_id())).map(StoneBookData.Node::getStone_data_id)).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(unitSet)) {
            return Collections.emptySet();
        }
        Map<String, StoneData> unitDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(unitSet);
        if (MapUtils.isEmpty(unitDataMap) || CollectionUtils.isEmpty(unitDataMap.values())) {
            return Collections.emptySet();
        }
        return unitDataMap.values().stream().filter(Objects::nonNull).map(StoneUnitData::newInstance).map(StoneUnitData::getJsonData).filter(Objects::nonNull)
                .map(StoneUnitData.Unit::getUnit_type).collect(Collectors.toSet());
    }

    /**
     * @param chipsUnitType 为 unknown 时返回所有类型的unit
     */
    public List<StoneUnitData> getUnitByChipsUnitType(String bookId, ChipsUnitType chipsUnitType) {
        if (StringUtils.isBlank(bookId)) {
            return Collections.emptyList();
        }
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(bookId));
        if (MapUtils.isEmpty(stoneDataMap) || CollectionUtils.isEmpty(stoneDataMap.values())) {
            return Collections.emptyList();
        }
        List<String> unitIdList = stoneDataMap.values().stream().filter(Objects::nonNull).map(StoneBookData::newInstance).map(StoneBookData::getJsonData).filter(Objects::nonNull)
                .map(StoneBookData.Book::getChildren).filter(CollectionUtils::isNotEmpty)
                .flatMap(l -> l.stream().filter(n -> n != null && StringUtils.isNotBlank(n.getStone_data_id())).map(StoneBookData.Node::getStone_data_id)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(unitIdList)) {
            return Collections.emptyList();
        }
        Map<String, StoneData> unitDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(unitIdList);
        if (MapUtils.isEmpty(unitDataMap) || CollectionUtils.isEmpty(unitDataMap.values())) {
            return Collections.emptyList();
        }
        if (chipsUnitType == null || chipsUnitType.equals(ChipsUnitType.unknown)) {
            return unitIdList.stream().filter(u -> unitDataMap.get(u) != null).map(unitDataMap::get).map(StoneUnitData::newInstance).collect(Collectors.toList());
        }
        return unitIdList.stream().filter(u -> unitDataMap.get(u) != null).map(unitDataMap::get).map(StoneUnitData::newInstance)
                .filter(e -> e.getJsonData() != null && (e.getJsonData().getUnit_type() != null && e.getJsonData().getUnit_type().equals(chipsUnitType))).collect(Collectors.toList());
    }

    public List<StoneBookData> loadAllChipsEnglishBooks() {
        List<OrderProduct> productList = userOrderLoaderClient.loadAllOrderProductIncludeOfflineForCrm().stream()
                .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }
        Set<String> productIdSet = productList.stream().map(OrderProduct::getId).collect(Collectors.toSet());
        Map<String, List<OrderProductItem>> itemMap = userOrderLoaderClient.loadProductItemsByProductIds(productIdSet);
        if (MapUtils.isEmpty(itemMap)) {
            return Collections.emptyList();
        }
        Set<String> bookSet = itemMap.values().stream().flatMap(l -> l.stream().map(OrderProductItem::getAppItemId)).collect(Collectors.toSet());
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(bookSet);
        if (MapUtils.isEmpty(stoneDataMap)) {
            return Collections.emptyList();
        }
        return stoneDataMap.values().stream().filter(Objects::nonNull).map(StoneBookData::newInstance).collect(Collectors.toList());
    }


    @Override
    public List<StoneData> loadLessonByUnitId(String bookId, String unitId) {
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return Collections.emptyList();
        }
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(bookId));
        if (MapUtils.isEmpty(stoneDataMap)) {
            return Collections.emptyList();
        }
        //unit
        List<StoneBookData.Node> unitList = Optional.ofNullable(stoneDataMap.get(bookId)).map(StoneBookData::newInstance).map(StoneBookData::getJsonData)
                .map(StoneBookData.Book::getChildren).filter(CollectionUtils::isNotEmpty).orElse(Collections.emptyList());
        List<String> lessonIdList = unitList.stream().filter(u -> u != null && u.getStone_data_id().equals(unitId)).findFirst().map(StoneBookData.Node::getChildren)
                .map(l -> l.stream().map(StoneBookData.Node::getStone_data_id).collect(Collectors.toList())).orElse(Collections.emptyList());
        if (CollectionUtils.isEmpty(lessonIdList)) {
            return Collections.emptyList();
        }
        Map<String, StoneData> lessonDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(lessonIdList);
        if (MapUtils.isEmpty(lessonDataMap)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(lessonDataMap.values());
    }

    @Override
    public StoneLessonData loadLessonById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return Optional.of(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(id))).map(m -> m.get(id))
                .map(StoneLessonData::newInstance).orElse(null);
    }


    @Override
    public MapMessage loadCourseStudyPlanInfo(Long userId, String unitId, String bookId) {
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

        StoneUnitData unitData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(unitId)))
                .map(e -> e.get(unitId))
                .map(StoneUnitData::newInstance)
                .orElse(null);
        StoneBookData stoneBookData = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(bookId)))
                .map(e -> e.get(bookId))
                .map(StoneBookData::newInstance)
                .filter(e -> e.getJsonData() != null)
                .filter(e -> CollectionUtils.isNotEmpty(e.getJsonData().getChildren()) &&
                        e.getJsonData().getChildren().stream().filter(ch -> unitId.equals(ch.getStone_data_id())).findFirst().orElse(null) != null)
                .orElse(null);
        if (stoneBookData == null || unitData == null) {
            return MapMessage.errorMessage("no data");
        }

        MapMessage message = MapMessage.successMessage();

        int finishRanking = 1;
        int scoreRanking = 1;
        ChipsEnglishClass clazz = Optional.ofNullable(aiUserLessonBookRefPersistence.loadByUserId(userId))
                .map(e -> e.stream()
                        .filter(bookRef -> bookRef.getBookId().equals(bookId))
                        .findFirst()
                        .orElse(null))
                .map(e -> chipsUserService.loadClazzIdByUserAndProduct(userId, e.getProductId()))
                .orElse(null);
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
        message.add("finishRanking", finishRanking).add("scoreRanking", scoreRanking);

        switch (userUnitResultPlan.getGrade()) {
            case B:
                message.add("summary", MessageConfig.unit_grade_B);
                break;
            case C:
                message.add("summary", MessageConfig.unit_grade_C);
                break;
            case A:
                message.add("summary", MessageConfig.unit_grade_A);
                break;
        }

        String title = Optional.of(unitData)
                .map(StoneUnitData::getJsonData)
                .map(StoneUnitData.Unit::getTitle)
                .orElse("");
        message.add("lessonHistory", lessonHistory(userPlanList, stoneBookData.getJsonData().getChildren()))
                .add("title", title)
                .add("gradeAScore", 90)
                .add("pointAbilityName", userUnitResultPlan.getPointAbility() != null ? userUnitResultPlan.getPointAbility().getDescription() : "")
                .add("lessonSummary", MessageConfig.getUnitExtInfo(unitId))
                .putAll(JsonUtils.fromJson(JsonUtils.toJson(userUnitResultPlan)));

        int rank = 1;
        for (StoneBookData.Node unit : stoneBookData.getJsonData().getChildren()) {
            if (unitId.equals(unit.getStone_data_id())) {
                break;
            }
            rank++;
        }
        message.put("rank", rank);
        return message;
    }

    @Override
    public MapMessage loadBookResultInfo(Long userId, String bookId) {
        if (userId == null || userId <= 0L) {
            return MapMessage.errorMessage("param error");
        }

        if (StringUtils.isBlank(bookId)) {
            bookId = ChipCourseSupport.TRAVEL_ENGLISH_BOOK_ID;
        }

        String book = bookId;
        AIUserBookResult result = aiUserBookResultDao.loadByUserId(userId).stream().filter(e -> e.getBookId().equals(book)).findFirst().orElse(null);
        if (result == null) {
            return MapMessage.errorMessage("no data");
        }
        StoneBookData stoneBookData = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(bookId)))
                .map(e -> e.get(book))
                .map(StoneBookData::newInstance)
                .filter(e -> e.getJsonData() != null)
                .filter(e -> CollectionUtils.isNotEmpty(e.getJsonData().getChildren()))
                .orElse(null);

        List<AIUserUnitResultPlan> userPlanList = aiUserUnitResultPlanDao.loadByUserId(userId);

        GradeReportConfig config = chipsContentService.loadGradeReportConfig().stream().filter(e -> book.equals(e.getBook())).filter(e -> CollectionUtils.isNotEmpty(e.getUnits())).findFirst().orElse(null);
        MapMessage message = MapMessage.successMessage();
        message.add("gradeAScore", 90)
                .add("levelName", result.getLevel() != null ? result.getLevel().getDescription() : "")
                .add("lessonHistory", lessonHistory(userPlanList, Optional.ofNullable(stoneBookData).map(d -> d.getJsonData().getChildren().stream()
                        .filter(e -> config == null || config.getUnits().contains(e.getStone_data_id()))
                        .collect(Collectors.toList())).orElse(Collections.emptyList())))
                .putAll(JsonUtils.fromJson(JsonUtils.toJson(result)));
        return message;
    }

    @Override
    public Map<Long, AIUserBookResult> loadPreviewUserBookResult(Collection<Long> userIdList, String book) {
        if (CollectionUtils.isEmpty(userIdList) || StringUtils.isBlank(book)) {
            return Collections.emptyMap();
        }
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIdList);
        if (MapUtils.isEmpty(userMap)) {
            return Collections.emptyMap();
        }

        GradeReportConfig config = chipsContentService.loadGradeReportConfig().stream()
                .filter(e -> e.getBook().equals(book))
                .filter(e -> CollectionUtils.isNotEmpty(e.getUnits()))
                .findFirst().orElse(null);
        if (config == null) {
            return Collections.emptyMap();
        }
        Map<Long, AIUserBookResult> resultMap = new HashMap<>();
        for(Long userId : userIdList) {
            User user = userMap.get(userId);
            if (user == null) {
                continue;
            }
            String id = AIUserBookResult.generateId(userId, book);
            AIUserBookResult bookResult = aiUserBookResultDao.load(id);
            if (bookResult == null) {
                List<AIUserUnitResultPlan> aiUserUnitResultPlans = aiUserUnitResultPlanDao.loadByUserId(userId).stream()
                        .filter(e2 -> !chipsContentService.isTrailUnit(e2.getUnitId()))
                        .filter(e2 -> config.getUnits().contains(e2.getUnitId()))
                        .sorted(Comparator.comparing(AIUserUnitResultPlan::getScore))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(aiUserUnitResultPlans) || aiUserUnitResultPlans.size() <= 3) {
                    continue;
                }
                bookResult = chipsContentService.initBookResult(user, aiUserUnitResultPlans, book);
            }

            if (bookResult == null) {
                continue;
            }
            resultMap.put(userId, bookResult);
        }
        return resultMap;
    }

    @Override
    public MapMessage loadLessonPlay(Long userId, String bookId, String unitId) {
        if (StringUtils.isAnyBlank(bookId, unitId) || userId == null) {
            return MapMessage.errorMessage("参数为空");
        }
        boolean showPlay = isShowPlay(bookId, userId);
        if (!showPlay) {
            return MapMessage.errorMessage("暂无权限");
        }
        StringBuffer cname = new StringBuffer("");
        StringBuffer name = new StringBuffer("");
        List<StoneLessonData> lessonDataList = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(bookId)))
                .map(ma -> ma.get(bookId))
                .map(StoneBookData::newInstance)
                .filter(book -> book != null && book.getJsonData() != null && CollectionUtils.isNotEmpty(book.getJsonData().getChildren()))
                .map(book -> book.getJsonData().getChildren().stream().filter(node -> unitId.equals(node.getStone_data_id())).findFirst().orElse(null))
                .filter(unitNode -> CollectionUtils.isNotEmpty(unitNode.getChildren()))
                .map(unitNode -> {
                    StoneData unitData =
                            stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(unitNode.getStone_data_id())).get(unitNode.getStone_data_id());
                    if (unitData != null) {
                        StoneUnitData unit = StoneUnitData.newInstance(unitData);
                        cname.append(unit.getJsonData() != null && StringUtils.isNotBlank(unit.getJsonData().getTitle()) ? unit.getJsonData().getTitle() : "");
                        name.append(unit.getJsonData() != null && StringUtils.isNotBlank(unit.getJsonData().getSub_title()) ? unit.getJsonData().getSub_title() : "");
                    }
                    Map<String, StoneData> lessonMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(unitNode.getChildren().stream().map(StoneBookData.Node::getStone_data_id).collect(Collectors.toSet()));
                    List<StoneLessonData> stoneDataList = new ArrayList<>();
                    for(StoneBookData.Node lessonNode : unitNode.getChildren()) {
                        StoneData stoneData = lessonMap.get(lessonNode.getStone_data_id());
                        if (stoneData == null) {
                            continue;
                        }
                        StoneLessonData lessonData = StoneLessonData.newInstance(stoneData);
                        if (lessonData == null || lessonData.getJsonData() == null || lessonData.getJsonData().getLesson_type() == null || !LESSON_PLAY_TYPE.contains(lessonData.getJsonData().getLesson_type())) {
                            continue;
                        }
                        stoneDataList.add(lessonData);
                    }
                    return stoneDataList;
                })
                .orElse(Collections.emptyList());
        if (CollectionUtils.isEmpty(lessonDataList)) {
            return MapMessage.successMessage().add("taskPlay", Collections.emptyList()).add("dialoguePlay", Collections.emptyList());
        }
        MapMessage mapMessage = MapMessage.successMessage()
                .add("taskPlay", Collections.emptyList())
                .add("dialoguePlay", Collections.emptyList())
                .add("unitCname", cname.toString())
                .add("unitName", name.toString());

        for (StoneLessonData lessonData : lessonDataList) {
            AILessonPlay aiLessonPlay = aiLessonPlayDao.load(lessonData.getId());
            if (aiLessonPlay == null || CollectionUtils.isEmpty(aiLessonPlay.getPlay())) {
                continue;
            }
            LessonType lessonType = lessonData.getJsonData().getLesson_type();
            if (lessonType == LessonType.Dialogue || lessonType == LessonType.video_conversation) {
                mapMessage.put("dialoguePlay", aiLessonPlay.getPlay());
            }

            if (lessonType == LessonType.Task || lessonType == LessonType.task_conversation) {
                mapMessage.put("taskPlay", aiLessonPlay.getPlay());
            }
        }
        return mapMessage;
    }

    private List<Map<String, Object>> lessonHistory(List<AIUserUnitResultPlan> userPlanList, List<StoneBookData.Node> units) {
        if (CollectionUtils.isEmpty(userPlanList)) {
            return Collections.emptyList();
        }

        Map<String, List<AIUserUnitResultPlan>> userPlansMap = userPlanList.stream().collect(Collectors.groupingBy(AIUserUnitResultPlan::getUnitId));

        List<Map<String, Object>> userPlans = new ArrayList<>();
        for (int i = 0; i < units.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", units.get(i).getStone_data_id());
            map.put("score", MapUtils.isNotEmpty(userPlansMap) && CollectionUtils.isNotEmpty(userPlansMap.get(units.get(i).getStone_data_id())) ?
                    userPlansMap.get(units.get(i).getStone_data_id()).get(0).getScore() : null);
            map.put("rank", i + 1);
            userPlans.add(map);
        }
        return userPlans;
    }

    public Map<String, StoneData> loadQuestionStoneData(List<String> qidList) {
        Map<String, StoneData> allMap = new HashMap<>();
        Map<String, StoneData> qMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(qidList);
        qMap.forEach((k, v) -> {
            if (k == null || v == null) {
                return;
            }
            if(StringUtils.isNotBlank(v.getSchemaName()) && v.getSchemaName().equals(ChipsQuestionType.task_npc.name())){
                StoneTalkNpcQuestionData npcQuestionData = StoneTalkNpcQuestionData.newInstance(v);
                List<String> list = Optional.ofNullable(npcQuestionData).map(StoneTalkNpcQuestionData::getJsonData).map(StoneTalkNpcQuestionData.Npc::getContent_ids).orElse(null);
                if (CollectionUtils.isEmpty(list)) {
                    return;
                }
                Map<String, StoneData> tempMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(list);
                if (MapUtils.isEmpty(tempMap)) {
                    return;
                }
                allMap.putAll(tempMap);
            } else {
                allMap.put(k, v);
            }
        });
        return allMap;
    }


}
