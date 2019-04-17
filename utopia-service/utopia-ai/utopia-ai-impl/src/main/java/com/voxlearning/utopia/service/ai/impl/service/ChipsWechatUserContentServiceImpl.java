package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserContentService;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.cache.manager.WechatUserDialogueTalkSceneResultCacheManager;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserEntity;
import com.voxlearning.utopia.service.ai.impl.context.ChipsWechatQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserPersistence;
import com.voxlearning.utopia.service.ai.impl.service.processor.v2.wechatresult.ChipsWechatQuestionResultProcessor;
import com.voxlearning.utopia.service.ai.impl.support.UserInfoSupport;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.ai.internal.ChipsVideoService;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Named
@ExposeService(interfaceClass = ChipsWechatUserContentService.class)
public class ChipsWechatUserContentServiceImpl implements ChipsWechatUserContentService {

    @Inject
    private AICacheSystem aiCacheSystem;

    @Inject
    private ChipsWechatUserPersistence wechatUserPersistence;

    @Inject
    private ChipsVideoService chipsUserVideoService;

    @Inject
    private ChipsContentService chipsContentService;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    private ChipsWechatQuestionResultProcessor questionResultProcessor;

    private static String LOW_LEVEL_REGEX = "[e|f|E|F]\\S*";

    @Override
    public MapMessage synthesisUserVideo(Long wechatUserId, ChipsLessonRequest request, List<String> userVideos) {
        if (wechatUserId == null || request == null || StringUtils.isBlank(request.getLessonId()) || CollectionUtils.isEmpty(userVideos)) {
            return MapMessage.errorMessage(ChipsErrorType.PARAMETER_ERROR.getInfo()).setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        String sessionId = aiCacheSystem.getUserTalkFeedSessionCacheManager().getSessionId(wechatUserId, request.getLessonId());
        chipsUserVideoService.processWechatSynthesisUserVideo(request.getUnitId(), request.getLessonId(), request.getLessonType(), wechatUserId, sessionId, userVideos);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage processDialogueFeedback(Long wechatUserId, String input, String qid, ChipsLessonRequest request) {
        if (wechatUserId == null || StringUtils.isAnyBlank(qid, input)) {
            return MapMessage.errorMessage(ChipsErrorType.PARAMETER_ERROR.getInfo()).setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }

        StoneSceneQuestionData questionData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(qid)))
                .filter(MapUtils::isNotEmpty)
                .map(e -> e.get(qid))
                .map(StoneSceneQuestionData::newInstance)
                .orElse(null);
        if (questionData == null) {
            return MapMessage.errorMessage("题目信息为空").setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        StoneSceneQuestionData.Feedback feed = chipsContentService.processFeedback(input, questionData, wechatUserId);
        String level = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getLevel).orElse("E");
        String cn = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getCn_translation).orElse("");
        String en = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getTranslation).orElse("");
        String video = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getVideo).orElse("");
        String tip = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getTip).orElse("");
        String roleImg = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getRole_image).orElse("");
        String feedback = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getFeedback).orElse("");
        String feedback_cover_pic = Optional.of(feed).map(StoneSceneQuestionData.Feedback::getFeedback_cover_pic).orElse("");

        //记录对话流程
        recordDialogueTalk(wechatUserId, request.getLessonId(), input, questionData, feed);

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

        WechatUserDialogueTalkSceneResultCacheManager cacheManager = aiCacheSystem.getWechatUserDialogueTalkSceneResultCacheManager();
        String sessionId = aiCacheSystem.getUserTalkFeedSessionCacheManager().getSessionId(user, lessonId);

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

                cacheManager.addRecord(user, lessonId, firstQuestion.getId(), talkResultInfo, "", null, sessionId);
            }
        }

        TalkResultInfo question = null, feedback = null;
        if (questionData.getJsonData() != null && StringUtils.isNoneBlank(questionData.getJsonData().getCn_translation(),
                questionData.getJsonData().getTranslation(), questionData.getJsonData().getRole_image(), questionData.getJsonData().getVideo())) {
            TalkResultInfoContent content = new TalkResultInfoContent();
            content.setCn_translation(questionData.getJsonData().getCn_translation());
            content.setVideo(questionData.getJsonData().getVideo());
            content.setRole_image(questionData.getJsonData().getRole_image());
            content.setTranslation(questionData.getJsonData().getTranslation());
            TalkResultInfoData infoData = new TalkResultInfoData();
            infoData.setContent(content);
            question = new TalkResultInfo();
            question.setData(Collections.singletonList(infoData));
        }

        if (feed != null && StringUtils.isNoneBlank(feed.getCn_translation(), feed.getTranslation(), feed.getRole_image(), feed.getVideo())) {
            TalkResultInfoContent content = new TalkResultInfoContent();
            content.setTranslation(feed.getTranslation());
            content.setCn_translation(feed.getCn_translation());
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
            TalkResultInfoKnowledge talkResultInfoKnowledge = new TalkResultInfoKnowledge();
            if (feedback == null || CollectionUtils.isEmpty(feedback.getData())) {
                feedback = new TalkResultInfo();
                TalkResultInfoData infoData = new TalkResultInfoData();
                feedback.setData(Arrays.asList(infoData));
            }

            talkResultInfoKnowledge.setSentences(questionData.getJsonData().getSentences());
            feedback.getData().get(0).setKnowledge(talkResultInfoKnowledge);
        }


        cacheManager.addRecord(user, lessonId, questionData.getId(), question, input, feedback, sessionId);

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
                cacheManager.addRecord(user, lessonId, endQuestion.getId(), endResultInfo, "", null, sessionId);

            }
        }

        //判断对话是否结束
        if (isEnd || (endQuestion != null && endQuestion.getSchemaName() == ChipsQuestionType.scene_interlude)) {
            List<AITalkSceneResult> aiTalkSceneResultList = cacheManager.loadRecord(user + sessionId, lessonId);
            if (CollectionUtils.isEmpty(aiTalkSceneResultList)) {
                return;
            }
            ChipsWechatUserEntity us = wechatUserPersistence.load(user);
            List<AITalkScene> talkSceneList = CourseRuleUtil.handleUserTalk(aiTalkSceneResultList, Optional.ofNullable(us).map(ChipsWechatUserEntity::getAvatar).filter(StringUtils::isNotBlank).orElse(UserInfoSupport.DEFAULT_USER_IMAGE_NAME), false);
            cacheManager.addTalkList(user.toString(), lessonId, talkSceneList);
        }
    }

    @Override
    public MapMessage processQuestionResult(Long wechatUserId, ChipsQuestionResultRequest request) {
        if (wechatUserId == null || request == null) {
            return MapMessage.errorMessage(ChipsErrorType.PARAMETER_ERROR.getInfo()).setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        ChipsWechatQuestionResultContext resultContext = questionResultProcessor.process(new ChipsWechatQuestionResultContext(request, wechatUserId));
        if (!resultContext.isSuccessful()) {
            return MapMessage.errorMessage(resultContext.getMessage()).setErrorCode(resultContext.getErrorCode());
        }
        return resultContext.getResult();
    }
}
