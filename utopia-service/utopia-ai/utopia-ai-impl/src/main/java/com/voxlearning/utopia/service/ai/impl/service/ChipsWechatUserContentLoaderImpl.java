package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserContentLoader;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsTalkLowLevelCountCacheManager;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.ChipsLessonRequest;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserEntity;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserLessonResultHistoryDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserUnitResultHistoryDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserPersistence;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = ChipsWechatUserContentLoader.class)
public class ChipsWechatUserContentLoaderImpl implements ChipsWechatUserContentLoader {

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    private ChipsWechatUserLessonResultHistoryDao chipsWechatUserLessonResultHistoryDao;

    @Inject
    private ChipsWechatUserUnitResultHistoryDao chipsWechatUserUnitResultHistoryDao;

    @Inject
    private ChipsTalkLowLevelCountCacheManager chipsTalkLowLevelCountCacheManager;

    @Inject
    private ChipsWechatUserPersistence wechatUserPersistence;

    @Inject
    private ChipsContentService chipsContentService;

    @Inject
    private AICacheSystem aICacheSystem;


    @Override
    public MapMessage loadTrailCourse(Long wechatUserId) {
        if (wechatUserId == null) {
            return MapMessage.errorMessage(ChipsErrorType.PARAMETER_ERROR.getInfo()).setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        List<String> trialBooks = chipsContentService.loadMiniProgramTrailBook();

        if (CollectionUtils.isEmpty(trialBooks)) {
            return  MapMessage.successMessage().set("courses", Collections.emptyList());
        }

        List<ChipsWechatUserUnitResultHistory> unitResultHistories = chipsWechatUserUnitResultHistoryDao.loadByUser(wechatUserId);

        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(trialBooks);
        List<Map<String, Object>> courses = new ArrayList<>();
        for(String book : trialBooks) {
            StoneBookData bookData = Optional.ofNullable(stoneDataMap)
                    .map(ma -> ma.get(book))
                    .map(StoneBookData::newInstance)
                    .filter(bo -> bo.getJsonData() != null && CollectionUtils.isNotEmpty(bo.getJsonData().getChildren()))
                    .orElse(null);
            if (bookData == null) {
                continue;
            }

            for (StoneBookData.Node unitNode: bookData.getJsonData().getChildren()) {
                Map<String, Object> map = new HashMap<>();
                map.put("bookId", bookData.getId());
                map.put("title", bookData.getJsonData().getName());
                map.put("img", bookData.getJsonData().getCover_image());
                map.put("description", bookData.getJsonData().getName());
                map.put("unitId", unitNode.getStone_data_id());
                map.put("type", ChipsUnitType.dialogue_practice);
                map.put("lessonId", unitNode.getChildren().get(0).getStone_data_id());
                ChipsWechatUserUnitResultHistory unitResultHistory = unitResultHistories.stream().filter(e -> e.getUnitId().equals(unitNode.getStone_data_id())).findFirst().orElse(null);
                map.put("finished", unitResultHistory != null);
                map.put("star", unitResultHistory != null ? unitResultHistory.getStar() : 0);
                map.put("score", unitResultHistory != null ? unitResultHistory.getScore() : 0);
                courses.add(map);
            }
        }
        return MapMessage.successMessage().set("courses", courses);
    }

    @Override
    public MapMessage loadLessonInfo(Long wechatUserId, ChipsLessonRequest chipsLessonRequest) {
        String lessonId = chipsLessonRequest.getLessonId();
        StoneLessonData lessonData = Optional
                .ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(lessonId)))
                .filter(MapUtils::isNotEmpty)
                .map(m -> m.get(lessonId))
                .map(StoneLessonData::newInstance)
                .orElse(null);
        if (lessonData == null) {
            return MapMessage.errorMessage("课程不存在").setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        StoneLessonData.Lesson lesson = lessonData.getJsonData();
        MapMessage mapMessage = MapMessage.successMessage()
                .add("title", lesson.getName())
                .add("bookId", chipsLessonRequest.getBookId())
                .add("unitId", chipsLessonRequest.getUnitId())
                .add("unitType", Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(chipsLessonRequest.getUnitId())))
                        .filter(MapUtils::isNotEmpty)
                        .map(m -> m.get(chipsLessonRequest.getUnitId()))
                        .map(StoneUnitData::newInstance)
                        .map(StoneUnitData::getJsonData)
                        .map(StoneUnitData.Unit::getUnit_type)
                        .map(ChipsUnitType::name)
                        .orElse(""))
                .add("lessonId", lessonId)
                .add("lessonType", lesson.getLesson_type())
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
                        .orElse(null);
                if (stoneData == null) {
                    continue;
                }
                ChipsQuestionType type = ChipsQuestionType.of(stoneData.getSchemaName());
                if (type == null || type == ChipsQuestionType.unknown || type == ChipsQuestionType.video_conversation) {
                    continue;
                }
                questionList.add(stoneData);
            }
        }
        aICacheSystem.getUserTalkFeedSessionCacheManager().genSessionId(wechatUserId, lessonId);
        chipsTalkLowLevelCountCacheManager.delete(questionList.stream().map(StoneData::getId).collect(Collectors.toList()), wechatUserId);
        return mapMessage.add("questions", questionList);
    }

    @Override
    public MapMessage loadLessonResult(Long wechatUserId, ChipsLessonRequest chipsLessonRequest) {
        if (wechatUserId == null || chipsLessonRequest == null || StringUtils.isBlank(chipsLessonRequest.getLessonId())) {
            return MapMessage.errorMessage(ChipsErrorType.PARAMETER_ERROR.getInfo()).setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        ChipsWechatUserLessonResultHistory lessonResultHistory = chipsWechatUserLessonResultHistoryDao.load(wechatUserId, chipsLessonRequest.getLessonId());
        if (lessonResultHistory == null) {
            return MapMessage.errorMessage("没有课程信息").setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        int star = 0;
        int score = 0;
        if (lessonResultHistory.getStar() != null) {
            star = lessonResultHistory.getStar();
            score = lessonResultHistory.getScore() != null ? lessonResultHistory.getScore() : 0;
        }
        MapMessage message = MapMessage.successMessage();
        message.put("star", star);
        message.put("score", score);
        message.put("lessonType", lessonResultHistory.getLessonType() != null ? lessonResultHistory.getLessonType().name() : "");
        message.put("talkList", MapUtils.isNotEmpty(lessonResultHistory.getExt()) ? lessonResultHistory.getExt().get("talkList") : Collections.emptyList());
        return message;
    }


    @Override
    public MapMessage loadUnitResult(Long wechatUserId, String bookId, String unitId) {
        if (wechatUserId == null || StringUtils.isAnyBlank(bookId, unitId)) {
            return MapMessage.errorMessage(ChipsErrorType.PARAMETER_ERROR.getInfo()).setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        MapMessage mm = MapMessage.successMessage();

        Long userId = wechatUserId;

        ChipsWechatUserUnitResultHistory unitResultHistory = chipsWechatUserUnitResultHistoryDao.load(userId, unitId);
        if (unitResultHistory == null) {
            return MapMessage.errorMessage("数据不存在").setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        StoneBookData bookData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(bookId)))
                .map(e -> e.get(bookId))
                .map(StoneBookData::newInstance)
                .orElse(null);
        if (bookData == null) {
            return MapMessage.errorMessage("教材不存在").setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
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
        List<ChipsWechatUserLessonResultHistory> lessonResult = chipsWechatUserLessonResultHistoryDao.loadByUnit(userId, unitId);
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
        mm.put("report", unitResultHistory);
        // lesson
        List<Map<String, Object>> lessonMapList = new ArrayList<>();
        lessonResult.stream()
                .filter(lesson -> lesson.getScore() != null && lesson.getScore() >= 0)
                .filter(e -> CollectionUtils.isNotEmpty(lessonIds) && lessonIds.contains(e.getLessonId())).forEach(lesson -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", lesson.getLessonId());
            map.put("lessonType", lesson.getLessonType());
            map.put("lessonTypeDesc", Optional.ofNullable(lesson.getLessonType()).map(LessonType::getDesc).orElse(""));
            map.put("score", lesson.getScore());
            lessonMapList.add(map);
        });
        mm.put("lessonResult", lessonMapList);

        StoneUnitData unitData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(unitId))).map(e -> e.get(unitId))
                .map(StoneUnitData::newInstance)
                .orElse(null);
        String unitName = Optional.ofNullable(unitData).map(StoneUnitData::getJsonData).map(StoneUnitData.Unit::getName).orElse("");
        mm.put("unitName", unitName);
        String unitImage = Optional.ofNullable(unitData).map(StoneUnitData::getJsonData).map(StoneUnitData.Unit::getCover_image).orElse("");
        mm.put("unitImage", unitImage);
        ChipsWechatUserEntity user = wechatUserPersistence.load(wechatUserId);
        mm.put("studentName", Optional.ofNullable(user).map(ChipsWechatUserEntity::getNickName).orElse(""));
        mm.put("avatar", Optional.ofNullable(user).map(ChipsWechatUserEntity::getAvatar).orElse(""));

        return mm;
    }
}
