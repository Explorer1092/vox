package com.voxlearning.utopia.service.ai.internal;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.ai.impl.persistence.task.ChipsUserDrawingTaskJoinPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.task.ChipsUserDrawingTaskPersistence;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChipsVideoService {
    private final static Logger logger = LoggerFactory.getLogger(ChipsVideoService.class);
    @Inject
    private AIUserUnitResultHistoryDao aiUserUnitResultHistoryDao;

    @Inject
    private AIUserLessonResultHistoryDao aiUserLessonResultHistoryDao;

    @Inject
    private AIUserVideoDao aiUserVideoDao;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    private ChipsWechatUserLessonResultHistoryDao chipsWechatUserLessonResultHistoryDao;

    @AlpsQueueProducer(queue = "utopia.ai.user.video.synthesis.queue", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer userVideoSynthesisQueue;
    @Inject
    private ChipsVideoBlackListDao videoBlackListDao;

    public static String TEMP_QUESTION_ID_SEP = ".";

    public MapMessage saveUserVideoV2(Long userId, String lessonId, String sessionId, String video) {
        if (userId == null || StringUtils.isAnyBlank(lessonId, video)) {
            return MapMessage.errorMessage("请求参数异常").set("result", "400");
        }
        String userName = Optional.ofNullable(userLoaderClient.loadUser(userId))
                .filter(e -> e.getProfile() != null && StringUtils.isNotBlank(e.getProfile().getNickName()))
                .map(User::getProfile)
                .map(UserProfile::getNickName)
                .orElse("");
        String lessonName = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(lessonId)))
                .filter(e -> MapUtils.isNotEmpty(e))
                .map(e -> e.get(lessonId))
                .map(StoneLessonData::newInstance)
                .map(StoneLessonData::getJsonData)
                .map(StoneLessonData.Lesson::getName)
                .orElse("");
        AIUserVideo aiUserVideo = AIUserVideo.newInstance(userId, userName, lessonId, lessonName);
        aiUserVideo.setVideo(video);
        aiUserVideo.setId(userId + sessionId);
        //黑名单用户期间,视频状态更新为违规
        Set<Long> blackList = videoBlackListDao.loadAll().stream().map(e -> e.getId()).collect(Collectors.toSet());
        Long blackUser = Optional.ofNullable(userId).filter(e -> blackList.contains(e)).orElse(null);
        if (blackUser != null) {
            aiUserVideo.setStatus(AIUserVideo.ExamineStatus.Failed);
        }
        aiUserVideoDao.upsert(aiUserVideo);

        AIUserLessonResultHistory lessonResultHistory = aiUserLessonResultHistoryDao.loadById(sessionId);
        if (lessonResultHistory == null) {
            return MapMessage.errorMessage("合成后的视频无法存储").set("result", "400");
        }
        lessonResultHistory.setUserVideo(video);
        lessonResultHistory.setUpdateDate(new Date());
        lessonResultHistory.setUserVideoId(aiUserVideo.getId());
        aiUserLessonResultHistoryDao.upsert(lessonResultHistory);

        return MapMessage.successMessage().set("result", "success");
    }

    public MapMessage saveAIVideoResultV1(Long userId, String lessonId, String sessionId, String video) {
        if (userId == null || StringUtils.isBlank(lessonId) || StringUtils.isBlank(video)) {
            return MapMessage.errorMessage("请求参数异常").set("result", "400");
        }
        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
        if (lesson == null) {
            return MapMessage.errorMessage("课程不存在").set("result", "400");
        }
        String userName = Optional.ofNullable(userLoaderClient.loadUser(userId))
                .filter(e -> e.getProfile() != null && StringUtils.isNotBlank(e.getProfile().getNickName()))
                .map(User::getProfile)
                .map(UserProfile::getNickName)
                .orElse("");
        String lessonName = Optional.ofNullable(newContentLoaderClient.loadBookCatalogByCatalogId(lesson.unitId()))
                .filter(e -> MapUtils.isNotEmpty(e.getExtras()))
                .filter(e -> e.getExtras().get("ai_teacher") != null)
                .map(e -> JsonUtils.fromJson(SafeConverter.toString(e.getExtras().get("ai_teacher"))))
                .filter(e -> MapUtils.isNotEmpty(e) && e.get("pageSubTitle") != null)
                .map(e -> SafeConverter.toString(e.get("pageSubTitle")))
                .orElse("");
        AIUserVideo aiUserVideo = AIUserVideo.newInstance(userId, userName, lesson.bookId(), lesson.unitId(), lessonName);
        aiUserVideo.setVideo(video);
        aiUserVideo.setLessonId(lessonId);
        aiUserVideo.setId(userId + sessionId);
        aiUserVideoDao.upsert(aiUserVideo);

        AIUserLessonResultHistory lessonResultHistory = aiUserLessonResultHistoryDao.loadById(sessionId);
        if (lessonResultHistory == null) {
            return MapMessage.errorMessage("合成后的视频无法存储").set("result", "400");
        }
        lessonResultHistory.setUserVideo(video);
        lessonResultHistory.setUpdateDate(new Date());
        lessonResultHistory.setUserVideoId(aiUserVideo.getId());
        aiUserLessonResultHistoryDao.upsert(lessonResultHistory);

        AIUserUnitResultHistory aiUserUnitResultHistory = aiUserUnitResultHistoryDao.load(userId, lessonResultHistory.getUnitId());
        if (aiUserUnitResultHistory != null) {
            aiUserUnitResultHistory.setVideo(video);
            aiUserUnitResultHistory.setUserVideoId(aiUserVideo.getId());
            aiUserUnitResultHistory.setUpdateDate(new Date());
            aiUserUnitResultHistoryDao.upsert(aiUserUnitResultHistory);
        }
        return MapMessage.successMessage().set("result", "success");
    }

    public void processSynthesisUserVideo(String lessonId, LessonType lessonType,  String bookId, String unitId, Long userId, String sessionId, List<String> videos) {
        AIUserVideo aiUserVideo = new AIUserVideo();
        aiUserVideo.setLessonId(lessonId);
        aiUserVideo.setDisabled(true);
        aiUserVideo.setBookId(bookId);
        aiUserVideo.setUnitId(unitId);
        aiUserVideo.setId(userId + sessionId);
        aiUserVideo.setOriginalVideos(videos);
        aiUserVideo.setUserId(userId);
        aiUserVideo.setCreateTime(new Date());
        aiUserVideo.setUpdateTime(new Date());
        aiUserVideoDao.upsert(aiUserVideo);

        AIUserLessonResultHistory lessonResultHistory = aiUserLessonResultHistoryDao.loadById(sessionId);
        if (lessonResultHistory == null) {
            AIUserLessonResultHistory oldRes = aiUserLessonResultHistoryDao.load(userId, lessonId);
            if (oldRes != null) {
                aiUserLessonResultHistoryDao.disableOld(userId, oldRes.getLessonId());
                lessonResultHistory = oldRes;
            } else {
                lessonResultHistory = new AIUserLessonResultHistory();
                lessonResultHistory.setDisabled(false);
                lessonResultHistory.setFinished(false);
            }
            lessonResultHistory.setCreateDate(new Date());
            lessonResultHistory.setId(sessionId);
            lessonResultHistory.setLessonId(lessonId);
            lessonResultHistory.setLessonType(lessonType);
            lessonResultHistory.setUserId(userId);
        }
        lessonResultHistory.setUserVideo("#");
        lessonResultHistory.setUserVideoId("");
        lessonResultHistory.setUpdateDate(new Date());
        aiUserLessonResultHistoryDao.upsert(lessonResultHistory);

        String id = "1" + TEMP_QUESTION_ID_SEP + userId + TEMP_QUESTION_ID_SEP + lessonId + TEMP_QUESTION_ID_SEP + sessionId;

        processUserVideoSythesisMessage(id, videos, userId, "app");
    }

    public void updateFinishVideoStatus(String id, String video) {
        AIUserVideo aiUserVideo = new AIUserVideo();
        aiUserVideo.setVideo(video);
        aiUserVideo.setDisabled(false);
        aiUserVideo.setUpdateTime(new Date());
        aiUserVideo.setStatus(AIUserVideo.ExamineStatus.Waiting);
        //黑名单用户期间,视频状态更新为违规
        boolean blackUser = blackUserVideo(id);
        if (blackUser) {
            aiUserVideo.setStatus(AIUserVideo.ExamineStatus.Failed);
        }
        aiUserVideo.setId(id);
        aiUserVideoDao.upsert(aiUserVideo);
    }

    private boolean blackUserVideo(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        AIUserVideo video = aiUserVideoDao.load(id);
        Set<Long> blackList = videoBlackListDao.loadAll().stream().map(e -> e.getId()).collect(Collectors.toSet());
        Long blackUser = Optional.of(video).map(e -> e.getUserId()).filter(e -> blackList.contains(e)).orElse(null);
        return blackUser != null;
    }

    public void processWechatSynthesisUserVideo(String unitId, String lessonId, LessonType lessonType, Long userId, String sessionId, List<String> videos) {
        ChipsWechatUserLessonResultHistory lessonResultHistory = chipsWechatUserLessonResultHistoryDao.load(sessionId);
        if (lessonResultHistory == null) {
            ChipsWechatUserLessonResultHistory oldRes = chipsWechatUserLessonResultHistoryDao.load(userId, lessonId);
            if (oldRes != null) {
                chipsWechatUserLessonResultHistoryDao.disableOld(userId, oldRes.getLessonId(), unitId);
                lessonResultHistory = oldRes;
            } else {
                lessonResultHistory = new ChipsWechatUserLessonResultHistory();
                lessonResultHistory.setDisabled(false);
                lessonResultHistory.setFinished(false);
            }
            lessonResultHistory.setCreateDate(new Date());
            lessonResultHistory.setId(sessionId);
            lessonResultHistory.setLessonId(lessonId);
            lessonResultHistory.setLessonType(lessonType);
            lessonResultHistory.setUserId(userId);
        }
        lessonResultHistory.setUserVideo("#");
        lessonResultHistory.setUserVideoId("");
        lessonResultHistory.setUpdateDate(new Date());
        chipsWechatUserLessonResultHistoryDao.upsert(lessonResultHistory);

        String id =  "2" + TEMP_QUESTION_ID_SEP + userId + TEMP_QUESTION_ID_SEP + lessonId + TEMP_QUESTION_ID_SEP + sessionId;
        processUserVideoSythesisMessage(id, videos, userId, "wechatMiniProgram");
    }

    public void processUserVideoSythesisMessage(String id, List<String> videos, Long userId, String type) {
        Map<String, Object> message = new HashMap<>();
        message.put("ID", id);
        message.put("V", videos);
        message.put("WATER", "yes");

        userVideoSynthesisQueue.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        if (RuntimeMode.lt(Mode.STAGING)) {
            logger.info("handleUserVideo, usertoken:{}, type: {}, id:{}, videos:{}", userId, type, id, JsonUtils.toJson(videos));
        }
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", userId,
                "mod4", type,
                "mod2", id,
                "mod3", JsonUtils.toJson(videos),
                "op", "ai user video to synthesis"
        ));
    }

    public MapMessage saveWechatUserVideo(Long userId, String lessonId, String sessionId, String video) {
        if (userId == null || StringUtils.isAnyBlank(lessonId, video)) {
            return MapMessage.errorMessage("请求参数异常").set("result", "400");
        }

        ChipsWechatUserLessonResultHistory lessonResultHistory = chipsWechatUserLessonResultHistoryDao.load(sessionId);
        if (lessonResultHistory == null) {
            return MapMessage.errorMessage("合成后的视频无法存储").set("result", "400");
        }
        lessonResultHistory.setUserVideo(video);
        lessonResultHistory.setUpdateDate(new Date());
        lessonResultHistory.setUserVideoId("");
        chipsWechatUserLessonResultHistoryDao.upsert(lessonResultHistory);

        return MapMessage.successMessage();
    }
}
