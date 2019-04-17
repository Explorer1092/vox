package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.ai.api.ChipsUserVideoService;
import com.voxlearning.utopia.service.ai.constant.*;
import com.voxlearning.utopia.service.ai.data.AIQuestionAppraisionRequest;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.data.StoneQuestionData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.ai.impl.persistence.task.ChipsUserDrawingTaskPersistence;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.ai.internal.ChipsVideoService;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.util.Assert;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = ChipsUserVideoService.class)
public class ChipsUserVideoServiceImpl implements ChipsUserVideoService {
    private final static Logger logger = LoggerFactory.getLogger(ChipsUserVideoServiceImpl.class);
    @Inject
    private ChipsVideoService chipsVideoService;

    @Inject
    private AIUserVideoDao aiUserVideoDao;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private ChipsContentService chipsContentService;

    @Inject
    private ChipsUserDrawingTaskPersistence chipsUserDrawingTaskPersistence;
    @Inject
    private ChipsVideoBlackListDao videoBlackListDao;

    @Inject
    private AIUserQuestionResultCollectionDao userQuestionResultCollectionDao;
    @Inject
    private AIUserLessonResultHistoryDao userLessonResultHistoryDao;
    @Inject
    private AIUserVideoDao userVideoDao;
    @Inject
    private ChipsActiveServiceRecordDao chipsActiveServiceRecordDao;
    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;
    @Inject
    private ChipsEncourageVideoDao chipsEncourageVideoDao;
    @Inject
    private ChipsKeywordPrototypeDao keywordPrototypeDao;

    @Inject
    private AIUserUnitResultHistoryDao aiUserUnitResultHistoryDao;

    @Inject
    private ChipsKeywordVideoDao keywordVideoDao;
    @AlpsQueueProducer(queue = "utopia.ai.user.video.synthesis.queue", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer userVideoSynthesisQueue;

    @AlpsQueueProducer(queue = "utopia.ai.user.audio.image.synthesis.queue", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer userImageAndAudioQueue;
    @Override
    public MapMessage synthesisDrawingTaskVideo(Long userId, Long drawingTaskId, String coverImage, List<String> userVideos) {
        String id = RandomUtils.nextObjectId();
        ChipsUserDrawingTask drawingTask = chipsUserDrawingTaskPersistence.load(drawingTaskId);
        drawingTask.setUpdateTime(new Date());
        drawingTask.setCover(coverImage);
        drawingTask.setVideoId(id);
        drawingTask.setShare(true);
        chipsUserDrawingTaskPersistence.upsert(drawingTask);

        String userName = Optional.ofNullable(userLoaderClient.loadUser(userId))
                .filter(e -> e.getProfile() != null && StringUtils.isNotBlank(e.getProfile().getNickName()))
                .map(User::getProfile)
                .map(UserProfile::getNickName)
                .orElse("");

        AIUserVideo aiUserVideo = new AIUserVideo();
        aiUserVideo.setBookId(drawingTask.getBookId());
        aiUserVideo.setUnitId(drawingTask.getUnitId());
        aiUserVideo.setUserId(drawingTask.getUserId());
        aiUserVideo.setUserName(userName);
        aiUserVideo.setId(id);
        aiUserVideo.setCreateTime(new Date());
        aiUserVideo.setUpdateTime(new Date());
        aiUserVideo.setOriginalVideos(userVideos);
        aiUserVideoDao.upsert(aiUserVideo);

        chipsVideoService.processUserVideoSythesisMessage("3." + id + "." + drawingTaskId, userVideos, userId, "app_drawingTask");

        Map<String, Object> shareCfg = chipsContentService.loadDrawingTaskShare();
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.putAll(shareCfg);

        String url = SafeConverter.toString(shareCfg.get("linkUrl")) + "?t=" +drawingTaskId + "&u=" + userId;
        mapMessage.set("linkUrl", url);

        String title = SafeConverter.toString(shareCfg.get("title"));
        if (StringUtils.isNotBlank(title)) {
            Set<String> unitList = aiUserUnitResultHistoryDao.loadByUserId(userId).stream().filter(e -> !chipsContentService.isTrailUnit(e.getUnitId())).map(AIUserUnitResultHistory::getUnitId).collect(Collectors.toSet());
            String cardTitle = Optional.ofNullable(drawingTask.getUnitId())
                    .map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e)))
                    .map(ma -> ma.get(drawingTask.getUnitId()))
                    .map(StoneUnitData::newInstance)
                    .map(StoneUnitData::getJsonData)
                    .map(StoneUnitData.Unit::getImage_discription)
                    .orElse("薯条英语");
            title = MessageFormat.format(title, unitList.size(), cardTitle);
            mapMessage.put("title", title);
        }
        String icon = Optional.ofNullable(drawingTask.getDrawingId())
                .map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e)))
                .map(ma -> ma.get(drawingTask.getDrawingId()))
                .map(StoneQuestionData::newInstance)
                .map(StoneQuestionData::getJsonData)
                .map(ma -> SafeConverter.toString(ma.get("reward_pic_after_front")))
                .orElse("");
        if (StringUtils.isNotBlank(icon)) {
            mapMessage.put("icon", icon);
        }

        return mapMessage;
    }


    private void handleVideoV2(Long userId, String unitId) {
        List<AIUserQuestionResultCollection> questionResultList = userQuestionResultCollectionDao.loadByUidAndUnitId(userId, unitId);
        List<AIUserLessonResultHistory> lessonResultList = userLessonResultHistoryDao.loadByUserIdAndUnitIdWithDisabled(userId, unitId);
        if (CollectionUtils.isEmpty(questionResultList)) {
            return;
        }
        List<AIUserQuestionResultCollection> notFList = questionResultList.stream().filter(e -> e.getLevel() == null || !e.getLevel().equals("F")).collect(Collectors.toList());
        boolean allF = false;
        if (CollectionUtils.isEmpty(notFList)) {
            notFList = questionResultList;
            allF = true;
        }
        //筛选分数最低的用户回答结果
        AIUserQuestionResultCollection answer = getAiUserQuestionResultCollection(notFList);
        //选取最大lesson分数
        Integer lscore = getMaxLessonScore(lessonResultList);
        //构建level
        AiUserVideoLevel level;
        if (allF) {
            level = AiUserVideoLevel.N;
        } else {
            if(lscore < 95){
                level = AiUserVideoLevel.M;
            } else {
                level = AiUserVideoLevel.L;
            }
        }
        List<AIUserVideo> userVideoList = userVideoDao.loadByUserIdAndUnitId(userId, unitId).stream().filter(e -> StringUtils.isNotBlank(e.getLessonId())).collect(Collectors.toList());
        AIUserVideo selectedVideo = Optional.ofNullable(userVideoList).filter(l -> CollectionUtils.isNotEmpty(l)).map(l -> l.get(0)).orElse(null);
        if (selectedVideo != null && answer != null) {
            userVideoDao.updateForRemark(selectedVideo.getId(), level, true, lscore, answer.getId());
            chipsActiveServiceRecordDao.updateUserVideoId(userId, unitId, selectedVideo.getId());
            chipsActiveServiceRecordDao.updateRemarkStatus(userId, unitId, ChipsActiveServiceRecord.RemarkStatus.One);
            if (selectedVideo.getStatus() != null && selectedVideo.getStatus() == AIUserVideo.ExamineStatus.Passed) {
                examineVideo(userVideoDao.load(selectedVideo.getId()));
            }
        } else {
            logger.info("handlevideov2 :" + (selectedVideo == null ? null : selectedVideo.getId()) + ";answer : " + (answer == null ? null : answer.getId()));
            chipsActiveServiceRecordDao.updateRemarkStatus(userId, unitId, ChipsActiveServiceRecord.RemarkStatus.Two);
        }
    }


    /**
     * 计算每个用户的一对一点评视频
     */
    public void filterVideo(Long userId, String unitId) {
        List<AIUserVideo> userVideoList = userVideoDao.loadByUserIdAndUnitId(userId, unitId).stream().filter(e -> StringUtils.isNotBlank(e.getLessonId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userVideoList)) {
            filterAndExamineAudio(userId, unitId);//复习单元-练习（单词跟读、句子跟读）,模考单元1-模考课程1（仅考虑问答练习题）
            return;
        }
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(unitId));
        StoneUnitData unitData = Optional.ofNullable(stoneDataMap.get(unitId)).map(StoneUnitData::newInstance).orElse(null);
        if (unitData == null || unitData.getJsonData() == null ||unitData.getJsonData().getUnit_type() == null) {
            return;
        }
        ChipsUnitType unit_type = unitData.getJsonData().getUnit_type();
        if (unit_type != null && (unit_type == ChipsUnitType.role_play_unit || unit_type == ChipsUnitType.mock_test_unit_2)) {
            handleVideoV2(userId, unitId);
            return;
        }
        List<AIUserVideo> videoList = userVideoList.stream().filter(e -> e.getCategory() != AIUserVideo.Category.Bad).collect(Collectors.toList());//有脸视频
        if (CollectionUtils.isEmpty(videoList)) {
            videoList = userVideoList;//有脸 > 无脸
        }
        List<AIUserQuestionResultCollection> questionResultList = userQuestionResultCollectionDao.loadByUidAndUnitId(userId, unitId);
        List<AIUserLessonResultHistory> lessonResultList = userLessonResultHistoryDao.loadByUserIdAndUnitIdWithDisabled(userId, unitId);
        //用户题目回答结果 和 合成视频的对应关系
        Map<String, String> questionToVideoMap = questionResultToVideoMap(videoList, questionResultList);//AIUserQuestionResultCollection 的id 与 AIUserVideo id的对应关系
        //视频id与lesson id的对应关系
        Map<String, String> videoIdToLessonIdMap = lessonResultList.stream().map(e -> e.getId()).collect(Collectors.toMap(e -> String.valueOf(userId) + e.toString(), Function.identity(), (k1, k2) -> k2));
        //用户题目回答结果 和  AIUserLessonResultHistory 对应关系
        Map<String, String> questionToLessonMap = questionResultToLessonResult(videoIdToLessonIdMap, questionToVideoMap);
        Map<String, AIUserQuestionResultCollection> questionMap = questionResultList.stream().collect(Collectors.toMap(e -> e.getId(), Function.identity(), (k1, k2) -> k2));
        Map<String, AIUserLessonResultHistory> lessonMap = lessonResultList.stream().collect(Collectors.toMap(e -> e.getId(), Function.identity(), (k1, k2) -> k2));
        //每个题目对应的level
        Map<String, AiUserVideoLevel> questionToVideoLevelMap = calQuestionLevel(questionToLessonMap, questionMap, lessonMap);
        // 合成视频 与用户题目回答结果 的对应关系
        Map<String, Set<String>> videoToQuestionMap = videoToQuestionMap(questionToVideoMap);

        Map<String, AiUserVideoLevel> videoLevelMap = videoToLevelMap(videoList, videoToQuestionMap, questionToVideoLevelMap);
        //选取一个视频
        Set<String> selectedVideoSet = new HashSet<>();
        AiUserVideoLevel selectedLevel = AiUserVideoLevel.UNKNOW;
        for (AIUserVideo video : videoList) {
            AiUserVideoLevel level = videoLevelMap.get(video.getId());
            if (level == null) {
                continue;
            }
            if (level.getLevel() > selectedLevel.getLevel()) {
                selectedVideoSet = new HashSet<>();
                selectedLevel = level;
            }
            if (level.getLevel() == selectedLevel.getLevel()) {
                selectedVideoSet.add(video.getId());
            }
        }
        if (CollectionUtils.isEmpty(selectedVideoSet)) {
            selectedVideoSet = videoList.stream().map(AIUserVideo::getId).collect(Collectors.toSet());
        }
        //得分最小的用户回答
        String qrcolId = selectLowestScoreQRCol(selectedVideoSet, videoToQuestionMap, questionMap);
        String videoId = Optional.ofNullable(qrcolId).map(e -> questionToVideoMap.get(e)).orElse(null);
        if (StringUtils.isBlank(videoId)) {
            logger.info("videoId is blank: " + videoId + "; userId: " + userId + "; unitId: " + unitId);
            chipsActiveServiceRecordDao.updateRemarkStatus(userId, unitId, ChipsActiveServiceRecord.RemarkStatus.Two);
            return;
        }
        Map<String, AIUserVideo> idToVideoMap = videoList.stream().collect(Collectors.toMap(e -> e.getId(), Function.identity(), (k1, k2) -> k2));
        AIUserVideo selectedVideo = idToVideoMap.get(videoId);
        Integer remarkLessonScore = Optional.ofNullable(videoIdToLessonIdMap.get(videoId)).map(lid -> lessonMap.get(lid)).map(l -> l.getScore()).orElse(-1);
        if (selectedVideo != null) {
            userVideoDao.updateForRemark(selectedVideo.getId(), selectedLevel, true, remarkLessonScore, qrcolId);
            chipsActiveServiceRecordDao.updateUserVideoId(userId, unitId, videoId);
            chipsActiveServiceRecordDao.updateRemarkStatus(userId, unitId, ChipsActiveServiceRecord.RemarkStatus.One);
            if (selectedVideo.getStatus() != null && selectedVideo.getStatus() == AIUserVideo.ExamineStatus.Passed) {
                examineVideo(userVideoDao.load(selectedVideo.getId()));
            }
        }
    }


    /**
     * 选取分数最低的
     * @param selectedVideoSet
     * @param videoToQuestionMap
     * @param questionMap
     * @return
     */
    private String selectLowestScoreQRCol( Set<String> selectedVideoSet,Map<String, Set<String>> videoToQuestionMap, Map<String, AIUserQuestionResultCollection> questionMap) {
        Assert.notEmpty(selectedVideoSet);
        BigDecimal score = BigDecimal.valueOf(101);
        AIUserQuestionResultCollection remarkQRCol = null;
        for (String vid : selectedVideoSet) {
            Set<String> qset = videoToQuestionMap.get(vid);
            if (qset == null) {
                continue;
            }
            BigDecimal s = BigDecimal.valueOf(101);
            AIUserQuestionResultCollection rqcol  = null;
            for (String q : qset) {
                AIUserQuestionResultCollection collection = questionMap.get(q);
                if (collection != null && collection.getScore().compareTo(s) < 0) {
                    s = collection.getScore();
                    rqcol = collection;
                }
            }
            if (s.compareTo(score) < 0) {
                score = s;
                remarkQRCol = rqcol;
            }
        }
        return remarkQRCol == null ? null : remarkQRCol.getId();
    }

    /**
     * 每个视频对应的 AiUserVideoLevel
     *
     * @param videoList
     * @param videoToQuestionMap
     * @param questionToVideoLevelMap
     * @return
     */
    private Map<String, AiUserVideoLevel> videoToLevelMap(List<AIUserVideo> videoList, Map<String, Set<String>> videoToQuestionMap, Map<String, AiUserVideoLevel> questionToVideoLevelMap) {
        if (CollectionUtils.isEmpty(videoList)) {
            return Collections.emptyMap();
        }
        Map<String, AiUserVideoLevel> map = new HashMap<>();
        for (AIUserVideo userVideo : videoList) {
            Set<String> qSet = videoToQuestionMap.get(userVideo.getId());
            if (CollectionUtils.isEmpty(qSet)) {
                map.put(userVideo.getId(), AiUserVideoLevel.UNKNOW);
            } else {
                AiUserVideoLevel level = AiUserVideoLevel.UNKNOW;
                for (String q : qSet) {
                    AiUserVideoLevel l = questionToVideoLevelMap.get(q);
                    if (l != null && l.getLevel() > level.getLevel()) {
                        level = l;
                    }
                }
                map.put(userVideo.getId(), level);
            }
        }
        return map;
    }

    /**
     * 每个视频对应的AIUserQuestionResultCollection Id
     *
     * @param questionToVideoMap
     * @return
     */
    private Map<String, Set<String>> videoToQuestionMap(Map<String, String> questionToVideoMap) {
        if (MapUtils.isEmpty(questionToVideoMap)) {
            return Collections.emptyMap();
        }
        Map<String, Set<String>> videoToQMap = new HashMap<>();
        questionToVideoMap.forEach((k, v) -> {
            Set<String> set = videoToQMap.get(v);
            if (set == null) {
                set = new HashSet<>();
                videoToQMap.put(v, set);
            }
            set.add(k);
        });
        return videoToQMap;
    }

    /**
     * lessonType questionType 不在处理范围内，map中无对应的level，
     *
     * @param questionToLessonMap
     * @param questionMap
     * @param lessonMap
     */
    private Map<String, AiUserVideoLevel> calQuestionLevel(Map<String, String> questionToLessonMap,
                                                           Map<String, AIUserQuestionResultCollection> questionMap, Map<String, AIUserLessonResultHistory> lessonMap) {
        Map<String, AiUserVideoLevel> questionToLevelMap = new HashMap<>();
        questionMap.forEach((k, qrcol) -> {
            LessonType lessonType = qrcol.getLessonType();
            if (lessonType == null) {
                return;
            }
            //角色扮演-演角色、专项巩固-视频对话、对话实战-视频对话
            if (lessonType == LessonType.role_play_lesson || lessonType == LessonType.video_conversation) {
                AIUserLessonResultHistory lesson = Optional.ofNullable(questionToLessonMap.get(k)).map(e -> lessonMap.get(e)).orElse(null);
                if (lesson == null) {
                    questionToLevelMap.put(k, AiUserVideoLevel.A);
                } else {
                    questionToLevelMap.put(k, calType1(qrcol.getScore(), qrcol.getLevel(), lesson.getScore()));
                }
            }
            //模考课程1（仅考虑问答练习题）TODO
            if (lessonType == LessonType.mock_test_lesson_1) {
                ChipsQuestionType questionType = qrcol.getQuestionType();
                if (questionType == null) {
                    return;
                }
                if (questionType == ChipsQuestionType.mock_qa_audio) {//取音频

                }
            }
            if (lessonType == LessonType.mock_test_lesson_2) {
                ChipsQuestionType questionType = qrcol.getQuestionType();
                if (questionType == null) {
                    return;
                }
                if (questionType == ChipsQuestionType.mock_qa) {
                    AIUserLessonResultHistory lesson = Optional.ofNullable(questionToLessonMap.get(k)).map(e -> lessonMap.get(e)).orElse(null);
                    if (lesson == null) {
                        questionToLevelMap.put(k, AiUserVideoLevel.A);
                    } else {
                        questionToLevelMap.put(k, calType2(qrcol.getScore(), qrcol.getLevel(), lesson.getScore()));
                    }
                }
            }
        });
        return questionToLevelMap;
    }

    /**
     * 计算level
     * ii) E>D>C>B>A;
     * iii) 选取得分较低的视频。
     * A: >=95，单元>=95
     * B: <95且A+,单元>=95
     * C: <95且A+,单元<95
     * D: <95且非A+,单元>=95
     * E: <95且非A+,单元<95
     *
     * @param score
     * @param level
     * @param lessonScore
     * @return
     */
    private AiUserVideoLevel calType1(BigDecimal score, String level, Integer lessonScore) {
        if (lessonScore == null) {
            lessonScore = 0;
        }
        if (score == null) {
            return AiUserVideoLevel.A;
        }
        if (score.compareTo(BigDecimal.valueOf(95)) >= 0) {// >= 95
            return AiUserVideoLevel.A;
        }
        if (StringUtils.isBlank(level)) {
            return AiUserVideoLevel.A;
        }
        if ("A+".equals(level)) {//B,C
            if (lessonScore < 95) {
                return AiUserVideoLevel.C;
            } else {
                return AiUserVideoLevel.B;
            }
        } else {//D,E
            if (lessonScore < 95) {
                return AiUserVideoLevel.E;
            } else {
                return AiUserVideoLevel.D;
            }
        }
    }

    /***
     * 计算level
     * K>J>I>H>G>F>N；
     * iv) 选取得分较低的视频。
     * F: 所有题目>=95，单元>=95
     * G: 所有题目>=95，单元<95
     * H: <95且A+，单元>=95
     * I: <95且A+，单元<95
     * J: <95且非A+，单元>=95
     * K: <95且非A+，单元<95
     * N: 所有题目得分都为F
     * @param score
     * @param level
     * @param lessonScore
     * @return
     */
    private AiUserVideoLevel calType2(BigDecimal score, String level, Integer lessonScore) {
        if (lessonScore == null) {
            lessonScore = 0;
        }
        if (StringUtils.isNotBlank(level) && level.equals("F")) {
            return AiUserVideoLevel.N;
        }
        if (score == null) {
            return AiUserVideoLevel.F;
        }
        if (score.compareTo(BigDecimal.valueOf(95)) >= 0) {// >= 95
            if (lessonScore < 95) {
                return AiUserVideoLevel.G;
            } else {
                return AiUserVideoLevel.F;
            }
        }
        if (StringUtils.isBlank(level)) {//没有level情况
            if (lessonScore < 95) {
                return AiUserVideoLevel.G;
            } else {
                return AiUserVideoLevel.F;
            }
        }
        if ("A+".equals(level)) {//I,H
            if (lessonScore < 95) {
                return AiUserVideoLevel.I;
            } else {
                return AiUserVideoLevel.H;
            }
        } else {//J,K
            if (lessonScore < 95) {
                return AiUserVideoLevel.K;
            } else {
                return AiUserVideoLevel.J;
            }
        }
    }

    /**
     * 用户题目回答结果 和  AIUserLessonResultHistory 对应关系
     *
     * @param lessonMap
     * @param questionToVideoMap
     * @return
     */
    private Map<String, String> questionResultToLessonResult(Map<String, String> lessonMap, Map<String, String> questionToVideoMap) {
        if (MapUtils.isEmpty(lessonMap) || MapUtils.isEmpty(questionToVideoMap)) {
            return Collections.emptyMap();
        }
        Map<String, String> qToLessonMap = new HashMap<>();
        questionToVideoMap.forEach((k, v) -> {
            String l = lessonMap.get(v);
            if (StringUtils.isBlank(l)) {
                return;
            }
            qToLessonMap.put(k, l);
        });
        return qToLessonMap;
    }

    /**
     * 用户题目回答结果 和 合成视频的对应关系
     *
     * @param videoList
     * @param qList
     * @return
     */
    private Map<String, String> questionResultToVideoMap(List<AIUserVideo> videoList, List<AIUserQuestionResultCollection> qList) {
        if (CollectionUtils.isEmpty(qList) || CollectionUtils.isEmpty(videoList)) {
            return Collections.emptyMap();
        }
        Map<String, String> urlToVideoMap = new HashMap<>();
        for (AIUserVideo video : videoList) {
            List<String> originalVideoList = video.getOriginalVideos();
            if (CollectionUtils.isEmpty(originalVideoList)) {
                continue;
            }
            originalVideoList.forEach(url -> urlToVideoMap.put(url, video.getId()));
        }
        Map<String, String> qcolToVideoMap = new HashMap<>();
        for (AIUserQuestionResultCollection qCol : qList) {
            String userVideo = qCol.getUserVideo();
            if (StringUtils.isBlank(userVideo)) {
                continue;
            }
            String videoId = urlToVideoMap.get(userVideo);
            if (StringUtils.isBlank(videoId)) {
                continue;
            }
            qcolToVideoMap.put(qCol.getId(), videoId);
        }
        return qcolToVideoMap;
    }

    /**
     * 没有视频 ，只有音频,筛选并审核
     * 复习单元-练习（单词跟读、句子跟读）,模考单元1-模考课程1（仅考虑问答练习题）
     */
    private void filterAndExamineAudio(Long userId, String unitId) {
        List<AIUserQuestionResultCollection> questionResultList = userQuestionResultCollectionDao.loadByUidAndUnitId(userId, unitId);
        List<AIUserLessonResultHistory> lessonResultList = userLessonResultHistoryDao.loadByUserIdAndUnitIdWithDisabled(userId, unitId);
        if (CollectionUtils.isEmpty(questionResultList)) {
            return;
        }
        List<AIUserQuestionResultCollection> notFList = questionResultList.stream().filter(e -> e.getLevel() == null || !e.getLevel().equals("F")).collect(Collectors.toList());
        boolean allF = false;
        if (CollectionUtils.isEmpty(notFList)) {
            notFList = questionResultList;
            allF = true;
        }
        //筛选分数最低的用户回答结果
        AIUserQuestionResultCollection answer = getAiUserQuestionResultCollection(notFList);
        //选取最大lesson分数
        Integer lscore = getMaxLessonScore(lessonResultList);
        //构建level
        AiUserVideoLevel level;
        if (allF) {
            level = AiUserVideoLevel.N;
        } else {
            if(lscore < 95){
                level = AiUserVideoLevel.M;
            } else {
                level = AiUserVideoLevel.L;
            }
        }
        String image = loadQuestionImage(answer.getQid());
        if (StringUtils.isNotBlank(image) && StringUtils.isNotBlank(answer.getUserAudio())) {
            List<String> words = getImageAudioWord(answer);
            processUserAudioImageSythesisMessage(userId, unitId, level, answer.getId(), lscore, answer.getUserAudio(), image, words);
        }
    }

    /**
     * 音频和图片进行视频合成时所需的图片
     * @param qid
     * @return
     */
    private String loadQuestionImage(String qid) {
        StoneQuestionData questionData = Optional.ofNullable(qid).map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e)))
                .map(e -> e.get(qid)).map(StoneQuestionData::newInstance).orElse(null);
        if (questionData != null && questionData.getSchemaName() != null && questionData.getSchemaName() == ChipsQuestionType.word_repeat) {
            return Optional.ofNullable(questionData.getJsonData()).map(e -> e.get("word_image")).map(e -> e.toString()).orElse(null);
        }
        return "http://cdn.17zuoye.com/fs-resource/5ca2caf6b433278cf9d65f89.png";
    }

    /**
     * 合并音频的一对一视频点评
     * @param unitId
     * @param level
     * @param qrcId
     * @param lscore
     */
    public MapMessage examineAudio(Long userId, String unitId, AiUserVideoLevel level,String qrcId , Integer lscore, String video) {
        List<String> tocombineVideoList = new ArrayList<>();
        List<String> vdoneList = new ArrayList<>();
        if (StringUtils.isNotBlank(video)) {
            tocombineVideoList.add(video);
            String fileName = urlFileName(video);
            if (StringUtils.isNotBlank(fileName)) {
                vdoneList.add(fileName);
            }
        }
        ChipsUnitType chipsUnitType = handleUnitType(unitId);
        //外教鼓励语
        String foreignEncourageVideo = handleForeignEncourageVideo(lscore, chipsUnitType);
        if (StringUtils.isNotBlank(foreignEncourageVideo)) {
            tocombineVideoList.add(foreignEncourageVideo);
        }
        //中教鼓励语
        String cnEncourageVideo = handleCnEncourageVideo(level, chipsUnitType);
        if (StringUtils.isNotBlank(cnEncourageVideo)) {
            tocombineVideoList.add(cnEncourageVideo);
        }
        if (level == AiUserVideoLevel.N) {// 4(c).单元通用讲解
            String unitCommonRemarkVideo = extractUnitCommonRemarkVideo(unitId);
            if (StringUtils.isNotBlank(unitCommonRemarkVideo)) {
                tocombineVideoList.add(unitCommonRemarkVideo);
            }
        } else {//L,M  (a).语音讲解
            AIUserQuestionResultCollection answer = Optional.ofNullable(qrcId).map(userQuestionResultCollectionDao::load).orElse(null);
            Set<AIQuestionAppraisionRequest.Word> wordSet = extractKeywords(answer);
            //4(a).语音讲解
//            String keywordVideo = extractKeywordVideo(wordSet);
//            if (StringUtils.isNotBlank(keywordVideo)) {
//                tocombineVideoList.add(keywordVideo);
//            }
            List<String> wordVideoList = extractKeywordVideos(wordSet);
            if (CollectionUtils.isNotEmpty(wordVideoList)) {
                tocombineVideoList.addAll(wordVideoList);
            }
        }
        if (CollectionUtils.isEmpty(tocombineVideoList)) {
            return MapMessage.successMessage().add("info","未取到要合成的视频: " + qrcId);
        } else {
            if (tocombineVideoList.size() == 1) {
                //将用户视频更新到表中
                chipsActiveServiceRecordDao.updateExamineStatusAndVideoUrl(userId, unitId, tocombineVideoList.get(0));
                chipsActiveServiceRecordDao.updateRemarkStatus(userId, unitId, ChipsActiveServiceRecord.RemarkStatus.Five);
                return MapMessage.successMessage();
            } else {
                processUserVideoSythesisMessage(userId, unitId, tocombineVideoList, vdoneList);
                return MapMessage.successMessage();
            }
        }
    }

    private Integer getMaxLessonScore(List<AIUserLessonResultHistory> lessonResultList) {
        Integer lscore = -1;
        if (CollectionUtils.isEmpty(lessonResultList)) {
            return lscore;
        }
        for (AIUserLessonResultHistory lesson : lessonResultList) {
            if (lesson.getScore() != null && lesson.getScore() > lscore) {
                lscore = lesson.getScore();
            }
        }
        return lscore;
    }

    /**
     * 筛选出分数最小的用户回答
     * @param notFList
     * @return
     */
    @Nullable
    private AIUserQuestionResultCollection getAiUserQuestionResultCollection(List<AIUserQuestionResultCollection> notFList) {
        BigDecimal qScore = BigDecimal.valueOf(101);
        AIUserQuestionResultCollection answer = null;//选取的音频的用户回答
        for (AIUserQuestionResultCollection col : notFList) {
            if (StringUtils.isBlank(col.getUserAudio())) {
                continue;
            }
            if (answer == null) {
                answer = col;//先选取一个带音频的用户回答
            }
            BigDecimal s = col.getScore();
            if(s != null && s.compareTo(qScore) < 0 && s.compareTo(BigDecimal.ZERO) >= 0){
                qScore = s;
                answer = col;
            }
        }
        return answer;
    }

    /**
     * 获取url地址最后一级路径
     * @param url
     * @return
     */
    private String urlFileName(String url) {
        if (StringUtils.isBlank(url) || url.lastIndexOf("/") == -1) {
            return null;
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 有视频的,
     * @param video
     */
    @Override
    public MapMessage examineVideo(AIUserVideo video) {
        if (video == null || video.getDisabled() || video.getForRemark() == null || !video.getForRemark()) {
            return MapMessage.errorMessage((video != null ? video.getId() : "") + "; video 为空 或者不是一对一点评视频: " + video.getForRemark());
        }
        String unitId = video.getUnitId();
        //判断是哪个单元
        ChipsUnitType chipsUnitType = handleUnitType(unitId);
        List<String> tocombineVideoList = new ArrayList<>();
        //用户回答视频
        List<String> vdoneList = new ArrayList<>();
        String userVideo = video.getVideo();
        if (StringUtils.isNotBlank(userVideo)) {
            tocombineVideoList.add(userVideo);
            String fileName = urlFileName(userVideo);
            if (StringUtils.isNotBlank(fileName)) {
                vdoneList.add(fileName);
            }
        }
//        String userAnswerVideo = Optional.ofNullable(video.getRemarkQRCid()).map(userQuestionResultCollectionDao::load)
//                .map(e -> e.getUserVideo()).filter(StringUtils::isNotBlank).orElse(null);
//        if (StringUtils.isNotBlank(userAnswerVideo)) {
//            tocombineVideoList.add(userAnswerVideo);
//        } else {
//            List<String> originalVideos = video.getOriginalVideos();
//            if (CollectionUtils.isNotEmpty(originalVideos)) {
//                tocombineVideoList.addAll(originalVideos);
//            }
//        }
        //外教鼓励语
        String foreignEncourageVideo = handleForeignEncourageVideo(video.getRemarkLessonScore(), chipsUnitType);
        if (StringUtils.isNotBlank(foreignEncourageVideo)) {
            tocombineVideoList.add(foreignEncourageVideo);
        }
        //中教鼓励语
        String cnEncourageVideo = handleCnEncourageVideo(video.getRemarkLevel(), chipsUnitType);
        if (StringUtils.isNotBlank(cnEncourageVideo)) {
            tocombineVideoList.add(cnEncourageVideo);
        }
        AiUserVideoLevel remarkLevel = video.getRemarkLevel();
        if (remarkLevel != null) {
            if(remarkLevel == AiUserVideoLevel.B || remarkLevel == AiUserVideoLevel.C || remarkLevel == AiUserVideoLevel.H
                    || remarkLevel == AiUserVideoLevel.I || remarkLevel == AiUserVideoLevel.L || remarkLevel == AiUserVideoLevel.M
                    || remarkLevel == AiUserVideoLevel.A || remarkLevel == AiUserVideoLevel.D || remarkLevel == AiUserVideoLevel.E){//临时增加ADE
                AIUserQuestionResultCollection answer = Optional.ofNullable(video.getRemarkQRCid()).map(userQuestionResultCollectionDao::load).orElse(null);
                Set<AIQuestionAppraisionRequest.Word> wordSet = extractKeywords(answer);
                //4(a).语音讲解
//                String keywordVideo = extractKeywordVideo(wordSet);
//                if (StringUtils.isNotBlank(keywordVideo)) {
//                    tocombineVideoList.add(keywordVideo);
//                }
                List<String> wordVideoList = extractKeywordVideos(wordSet);
                if (CollectionUtils.isNotEmpty(wordVideoList)) {
                    tocombineVideoList.addAll(wordVideoList);
                }
            }
            if (remarkLevel == AiUserVideoLevel.D || remarkLevel == AiUserVideoLevel.E
                    || remarkLevel == AiUserVideoLevel.J || remarkLevel == AiUserVideoLevel.K) {
                //4(b).语法讲解
                String grammarRemarkVideo = extractGrammmarRemarkVideo(video.getRemarkQRCid());
                if (StringUtils.isNotBlank(grammarRemarkVideo)) {
                    tocombineVideoList.add(grammarRemarkVideo);
                }
            }
            if (remarkLevel == AiUserVideoLevel.N) {
                //4(c).单元通用讲解
                String unitCommonRemarkVideo = extractUnitCommonRemarkVideo(video.getUnitId());
                if (StringUtils.isNotBlank(unitCommonRemarkVideo)) {
                    tocombineVideoList.add(unitCommonRemarkVideo);
                }
            }
        }
        if (chipsUnitType == ChipsUnitType.role_play_unit || chipsUnitType == ChipsUnitType.special_consolidation || chipsUnitType == ChipsUnitType.dialogue_practice) {
            //5.分享题
            String shareVideo = extractShareVideo(video.getLessonId());
            if (StringUtils.isNotBlank(shareVideo)) {
                tocombineVideoList.add(shareVideo);
            }
        }
        if (CollectionUtils.isEmpty(tocombineVideoList)) {
            return MapMessage.successMessage().add("info","未取到要合成的视频: " + video.getId());
        } else {
            if (tocombineVideoList.size() == 1) {
                //将用户视频更新到表中
                chipsActiveServiceRecordDao.updateExamineStatusAndVideoUrl(video.getUserId(), video.getUnitId(), tocombineVideoList.get(0));
                chipsActiveServiceRecordDao.updateRemarkStatus(video.getUserId(), video.getUnitId(), ChipsActiveServiceRecord.RemarkStatus.Five);
                return MapMessage.successMessage();
            } else {
                //TODO, 进行视频合成
                processUserVideoSythesisMessage(video.getUserId(), video.getUnitId(), tocombineVideoList, vdoneList);
                return MapMessage.successMessage();
            }
        }
    }

    /**
     * 进行视频合成
     * @param userId
     * @param unitId
     * @param videos
     */
    private void processUserVideoSythesisMessage(Long userId, String unitId, List<String> videos, List<String> vdoneList) {
        Map<String, Object> message = new HashMap<>();
        String id = "4." + userId + "." + unitId;
        message.put("ID", id);
        message.put("V", videos);
        message.put("WATER", "no");
        message.put("V_DONE", vdoneList);
        userVideoSynthesisQueue.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        chipsActiveServiceRecordDao.updateRemarkStatus(userId, unitId,ChipsActiveServiceRecord.RemarkStatus.Four);
        String type = "video_remark_oneByOne";
        if (RuntimeMode.lt(Mode.STAGING)) {
            logger.info("handleUserRemarkVideo, usertoken:{}, type: {}, id:{}, videos:{}", userId, type, id, JsonUtils.toJson(videos));
        }
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", userId,
                "mod4", type,
                "mod2", id,
                "mod3", JsonUtils.toJson(message),
                "op", "ai user video to synthesis"
        ));
    }

    private void processUserAudioImageSythesisMessage(Long userId, String unitId, AiUserVideoLevel level, String qrcId , Integer lscore, String audio, String image, List<String> words) {
        Map<String, Object> message = new HashMap<>();
        String id = userId + "." + unitId + "." + level.name() + "." + qrcId + "." + lscore ;
        message.put("ID", id);
        message.put("images", Arrays.asList("http://cdn.17zuoye.com/fs-resource/5c9d8cc39831c70cfcee8add.png", image));
        message.put("audio", audio);
        message.put("words", words);
        userImageAndAudioQueue.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        chipsActiveServiceRecordDao.updateRemarkStatus(userId,unitId, ChipsActiveServiceRecord.RemarkStatus.Three);
        String type = "audio_remark_oneByOne";
        if (RuntimeMode.lt(Mode.STAGING)) {
            logger.info("handleUserRemarkVideo, usertoken:{}, type: {}, id:{}, audio:{}", userId, type, id, audio);
        }
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", userId,
                "mod4", type,
                "mod2", id,
                "mod3", JsonUtils.toJson(message),
                "op", "ai user image audio to video"
        ));
    }

    /**
     * 获取分享题视频
     * 5.分享题
     * @return
     */
    private String extractShareVideo(String lessonId) {
        return Optional.ofNullable(lessonId).map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e)))
                .map(e -> e.get(lessonId)).map(StoneLessonData::newInstance).map(e -> e.getJsonData()).map(e -> e.getVideo())
                .filter(StringUtils::isNotBlank).orElse(null);
    }

    /**
     * 获取单元通用讲解视频地址
     * 4(c).单元通用讲解
     * @param unitId
     * @return
     */
    private String extractUnitCommonRemarkVideo(String unitId) {
        return Optional.ofNullable(unitId).map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e)))
                .map(e -> e.get(unitId)).map(StoneUnitData::newInstance).map(e -> e.getJsonData()).map(e -> e.getVideo())
                .filter(StringUtils::isNotBlank).orElse(null);
    }

    /**
     * 提取语法讲解视频地址
     * @param qrcId
     * @return
     */
    private String extractGrammmarRemarkVideo(String qrcId) {
        String qid = Optional.ofNullable(qrcId).map(userQuestionResultCollectionDao::load).map(e -> e.getQid()).orElse(null);
        StoneQuestionData stoneQuestionData = Optional.ofNullable(qid).map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e))).map(e -> e.get(qid))
                .map(StoneQuestionData::newInstance).orElse(null);
        return Optional.ofNullable(stoneQuestionData).filter(e -> e.getSchemaName() == ChipsQuestionType.video_dialogue || e.getSchemaName() == ChipsQuestionType.mock_qa)
                .map(e -> e.getJsonData()).map(m -> m.get("grammar_video")).map(e -> e.toString()).filter(StringUtils::isNotBlank).orElse(null);
    }

    /**
     * 语音讲解 ，关键词讲解video地址
     * @param wordSet
     * @return
     */
    private String extractKeywordVideo(Set<AIQuestionAppraisionRequest.Word> wordSet) {
        Set<String> wordTextSet = wordSet.stream().map(e -> e.getText().trim().toLowerCase()).collect(Collectors.toSet());
        Map<String, ChipsKeywordPrototype> prototypeMap = keywordPrototypeDao.loads(wordTextSet);
        Set<String> keywordSet = wordTextSet.stream().map(e -> Optional.ofNullable(prototypeMap.get(e))
                .map(e1 -> e1.getPrototype())
                .filter(StringUtils::isNotBlank)
                .orElse(e)).collect(Collectors.toSet());
        Map<String, ChipsKeywordVideo> keywordVideoMap = keywordVideoDao.loads(keywordSet);
        String url = null;
        BigDecimal wordScore = BigDecimal.valueOf(101);
        for (AIQuestionAppraisionRequest.Word word : wordSet) {
            String wordText = word.getText().trim().toLowerCase();
            String prototype = Optional.ofNullable(wordText).map(e -> prototypeMap.get(e))
                    .map(e -> e.getPrototype()).filter(StringUtils::isNotBlank).orElse(wordText);
            String videourl = Optional.ofNullable(keywordVideoMap.get(prototype)).map(e -> e.getVideo()).orElse(null);
            if (StringUtils.isBlank(videourl)) {
                logger.info("word: " + wordText + "; can not find video");
                continue;
            }
            if (wordScore.compareTo(word.getScore()) > 0) {
                url = videourl;
                wordScore = word.getScore();
            }
        }
        return url;
    }


    /**
     * 语音讲解 ，关键词讲解video地址
     * @param wordSet
     * @return
     */
    private List<String> extractKeywordVideos(Set<AIQuestionAppraisionRequest.Word> wordSet) {
        Set<String> wordTextSet = wordSet.stream().map(e -> e.getText().trim().toLowerCase()).collect(Collectors.toSet());
        Map<String, ChipsKeywordPrototype> prototypeMap = keywordPrototypeDao.loads(wordTextSet);
        Set<String> keywordSet = wordTextSet.stream().map(e -> Optional.ofNullable(prototypeMap.get(e))
                .map(e1 -> e1.getPrototype())
                .filter(StringUtils::isNotBlank)
                .orElse(e)).collect(Collectors.toSet());
        Map<String, ChipsKeywordVideo> keywordVideoMap = keywordVideoDao.loads(keywordSet);
        List<AIQuestionAppraisionRequest.Word> wordList = new ArrayList<>();
        for (AIQuestionAppraisionRequest.Word word : wordSet) {
            String wordText = word.getText().trim().toLowerCase();
            String prototype = Optional.ofNullable(wordText).map(e -> prototypeMap.get(e))
                    .map(e -> e.getPrototype()).filter(StringUtils::isNotBlank).orElse(wordText);
            String videourl = Optional.ofNullable(keywordVideoMap.get(prototype)).map(e -> e.getVideo()).orElse(null);
            if (StringUtils.isBlank(videourl)) {
                logger.info("word: " + wordText + "; can not find video");
                continue;
            }
            wordList.add(word);
        }
        wordList.sort(Comparator.comparing(AIQuestionAppraisionRequest.Word::getScore));
        int count = 0;
        List<String> wordVideoList = new ArrayList<>();
        for (AIQuestionAppraisionRequest.Word word : wordList) {
            String wordText = word.getText().trim().toLowerCase();
            String prototype = Optional.ofNullable(wordText).map(e -> prototypeMap.get(e))
                    .map(e -> e.getPrototype()).filter(StringUtils::isNotBlank).orElse(wordText);
            String videourl = Optional.ofNullable(keywordVideoMap.get(prototype)).map(e -> e.getVideo()).orElse(null);
            if (StringUtils.isBlank(videourl) || count > 1) {
                continue;
            }
            count ++;
            wordVideoList.add(videourl);
        }
        return wordVideoList;
    }

    /**
     * 获取用户回答的word
     * @param answer
     * @return
     */
    private Set<AIQuestionAppraisionRequest.Word> extractKeywords(AIUserQuestionResultCollection answer) {
        if (answer == null) {
            return Collections.emptySet();
        }
        List<AIQuestionAppraisionRequest.Line> lines = Optional.ofNullable(answer.getVoiceEngineJson())
                .map(json -> JsonUtils.fromJson(json, AIQuestionAppraisionRequest.class))
                .map(AIQuestionAppraisionRequest::getLines).orElse(Collections.emptyList());
        Set<AIQuestionAppraisionRequest.Word> wordSet = new HashSet<>();
        BigDecimal zero = new BigDecimal(0);
        for (AIQuestionAppraisionRequest.Line line : lines) {
            List<AIQuestionAppraisionRequest.Word> words = line.getWords();
            if (CollectionUtils.isEmpty(words)) {
                continue;
            }
            for (AIQuestionAppraisionRequest.Word word : words) {
                if (word == null || StringUtils.isBlank(word.getText()) || word.getScore() == null || !word.getText().matches(ChipsEnglishUserLoaderImpl.REGEX)) {
                    continue;
                }
                if (word.getScore().compareTo(zero) >= 0) {
                    wordSet.add(word);
                }
            }
        }
        return wordSet;
    }

    /**
     * 获取中教鼓励语视频地址
     * @param remarkLevel
     * @param unitType
     * @return
     */
    private String handleCnEncourageVideo(AiUserVideoLevel remarkLevel, ChipsUnitType unitType) {
        AiUserVideoLevel level = remarkLevel;
        if (remarkLevel == AiUserVideoLevel.A) {
            level = AiUserVideoLevel.B;
        }
        if (remarkLevel == AiUserVideoLevel.D) {
            level = AiUserVideoLevel.B;
        }
        if (remarkLevel == AiUserVideoLevel.E) {
            level = AiUserVideoLevel.C;
        }
        return Optional.ofNullable(level)
                .map(e -> chipsEncourageVideoDao.load("1-" + unitType.name() + "-" + e.name()))
                .map(e -> e.getVideo()).orElse(null);
    }

    /**
     * 获取外教鼓励语视频地址
     * @param lessonScore
     * @param unitType
     * @return
     */
    private String handleForeignEncourageVideo(Integer lessonScore, ChipsUnitType unitType) {
        String level = "A";
        if (lessonScore != null && lessonScore < 95) {
            level = "B";
        }
        return Optional.ofNullable(chipsEncourageVideoDao.load("0-" + unitType.name() + "-" + level))
                .map(e -> e.getVideo()).orElse(null);
    }

    /**
     * Day1(1): 单元类型为角色扮演
     * Day2(2): 单元类型为话题学习且自定义名称含有“专项巩固”  --> 专项巩固
     * Day3(3): 单元类型为话题学习且自定义名称不含有”专项巩固“  --> 对话实战
     * Day10(4): 单元类型为复习单元
     * Day11(5): 单元类型为模考单元1
     * Day12(6): 单元类型为模考单元2
     * 获取单元类型
     * @param unitId
     * @return
     */
    private ChipsUnitType handleUnitType(String unitId) {
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(unitId));
        StoneUnitData unitData = Optional.ofNullable(stoneDataMap.get(unitId)).map(StoneUnitData::newInstance).orElse(null);
        if (unitData == null) {
            return ChipsUnitType.unknown;
        }
        ChipsUnitType chipsUnitType = Optional.ofNullable(unitData).map(e -> e.getJsonData()).map(e -> e.getUnit_type()).orElse(null);
        if (chipsUnitType == null) {
            return ChipsUnitType.unknown;
        }
        if (chipsUnitType == ChipsUnitType.short_lesson) {
            return ChipsUnitType.dialogue_practice;
        }
        if(chipsUnitType == ChipsUnitType.topic_learning){
            if(StringUtils.isNotBlank(unitData.getCustomName()) && unitData.getCustomName().contains("专项巩固")){
                return ChipsUnitType.special_consolidation;
            } else {
                return ChipsUnitType.dialogue_practice;
            }
        } else {
            return chipsUnitType;
        }
    }


    @Override
    public MapMessage addVideoBlackList(Long userId) {
        if (userId == null) {
            return MapMessage.errorMessage("userId is null");
        }
        User user = userLoaderClient.loadUser(userId, UserType.PARENT);
        if (user == null) {
            return MapMessage.errorMessage("该用户不存在: " + userId);
        }
        ChipsVideoBlackList black = videoBlackListDao.load(userId);
        if (black != null && black.getDisabled() != null && !black.getDisabled()) {
            return MapMessage.errorMessage("该用户已经在黑名单中");
        }
        black = new ChipsVideoBlackList();
        black.setId(userId);
        UserProfile userProfile = user.getProfile();
        black.setUserName(userProfile == null ? "" : userProfile.getNickName() != null ? userProfile.getNickName() : "");
        black.setDisabled(false);
        videoBlackListDao.upsert(black);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteVideoBlackList(Long userId) {
        if (userId == null) {
            return MapMessage.errorMessage("userId is null");
        }
        User user = userLoaderClient.loadUser(userId, UserType.PARENT);
        if (user == null) {
            return MapMessage.errorMessage("该用户不存在: " + userId);
        }
        ChipsVideoBlackList black = videoBlackListDao.load(userId);
        if (black == null || (black.getDisabled() != null && black.getDisabled())) {
            return MapMessage.errorMessage("该用户不在黑名单中");
        }
        black.setId(userId);
        black.setDisabled(true);
//        UserProfile userProfile = user.getProfile();
//        black.setUserName(userProfile == null ? "" : userProfile.getNickName() != null ? userProfile.getNickName() : "");
//        black.setDisabled(false);
        videoBlackListDao.upsert(black);
        return MapMessage.successMessage();
    }


    /**
     * 计算每个用户的一对一点评视频
     */
    public MapMessage filterRemarkVideo(Long userId, String unitId) {
        List<AIUserVideo> userVideoList = userVideoDao.loadByUserIdAndUnitId(userId, unitId).stream().filter(e -> StringUtils.isNotBlank(e.getLessonId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userVideoList)) {
            return filterAndExamineAudioV2(userId, unitId);//复习单元-练习（单词跟读、句子跟读）,模考单元1-模考课程1（仅考虑问答练习题）
        }
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(unitId));
        StoneUnitData unitData = Optional.ofNullable(stoneDataMap.get(unitId)).map(StoneUnitData::newInstance).orElse(null);
        if (unitData == null || unitData.getJsonData() == null ||unitData.getJsonData().getUnit_type() == null) {
            return MapMessage.errorMessage().add("info", "can not get unitType: " + unitId);
        }
        ChipsUnitType unit_type = unitData.getJsonData().getUnit_type();
        if (unit_type != null && (unit_type == ChipsUnitType.role_play_unit || unit_type == ChipsUnitType.mock_test_unit_2)) {
            return handleNotUserVideo(userId, unitId);
        }
        List<AIUserVideo> videoList = userVideoList.stream().filter(e -> e.getCategory() != AIUserVideo.Category.Bad).collect(Collectors.toList());//有脸视频
        if (CollectionUtils.isEmpty(videoList)) {
            videoList = userVideoList;//有脸 > 无脸
        }
        List<AIUserQuestionResultCollection> questionResultList = userQuestionResultCollectionDao.loadByUidAndUnitId(userId, unitId);
        List<AIUserLessonResultHistory> lessonResultList = userLessonResultHistoryDao.loadByUserIdAndUnitIdWithDisabled(userId, unitId);
        //用户题目回答结果 和 合成视频的对应关系
        Map<String, String> questionToVideoMap = questionResultToVideoMap(videoList, questionResultList);//AIUserQuestionResultCollection 的id 与 AIUserVideo id的对应关系
        //视频id与lesson id的对应关系
        Map<String, String> videoIdToLessonIdMap = lessonResultList.stream().map(e -> e.getId()).collect(Collectors.toMap(e -> String.valueOf(userId) + e.toString(), Function.identity(), (k1, k2) -> k2));
        //用户题目回答结果 和  AIUserLessonResultHistory 对应关系
        Map<String, String> questionToLessonMap = questionResultToLessonResult(videoIdToLessonIdMap, questionToVideoMap);
        Map<String, AIUserQuestionResultCollection> questionMap = questionResultList.stream().collect(Collectors.toMap(e -> e.getId(), Function.identity(), (k1, k2) -> k2));
        Map<String, AIUserLessonResultHistory> lessonMap = lessonResultList.stream().collect(Collectors.toMap(e -> e.getId(), Function.identity(), (k1, k2) -> k2));
        //每个题目对应的level
        Map<String, AiUserVideoLevel> questionToVideoLevelMap = calQuestionLevel(questionToLessonMap, questionMap, lessonMap);
        // 合成视频 与用户题目回答结果 的对应关系
        Map<String, Set<String>> videoToQuestionMap = videoToQuestionMap(questionToVideoMap);

        Map<String, AiUserVideoLevel> videoLevelMap = videoToLevelMap(videoList, videoToQuestionMap, questionToVideoLevelMap);
        //选取一个视频
        Set<String> selectedVideoSet = new HashSet<>();
        AiUserVideoLevel selectedLevel = AiUserVideoLevel.UNKNOW;
        for (AIUserVideo video : videoList) {
            AiUserVideoLevel level = videoLevelMap.get(video.getId());
            if (level == null) {
                continue;
            }
            if (level.getLevel() > selectedLevel.getLevel()) {
                selectedVideoSet = new HashSet<>();
                selectedLevel = level;
            }
            if (level.getLevel() == selectedLevel.getLevel()) {
                selectedVideoSet.add(video.getId());
            }
        }
        if (CollectionUtils.isEmpty(selectedVideoSet)) {
            selectedVideoSet = videoList.stream().map(AIUserVideo::getId).collect(Collectors.toSet());
        }
        //得分最小的用户回答
        String qrcolId = selectLowestScoreQRCol(selectedVideoSet, videoToQuestionMap, questionMap);
        String videoId = Optional.ofNullable(qrcolId).map(e -> questionToVideoMap.get(e)).orElse(null);
        if (StringUtils.isBlank(videoId)) {
            logger.info("videoId is blank: " + videoId + "; userId: " + userId + "; unitId: " + unitId);
            chipsActiveServiceRecordDao.updateRemarkStatus(userId, unitId, ChipsActiveServiceRecord.RemarkStatus.Two);
            return MapMessage.errorMessage().add("info", "can not filter userVideo:" + qrcolId);
        }
        Map<String, AIUserVideo> idToVideoMap = videoList.stream().collect(Collectors.toMap(e -> e.getId(), Function.identity(), (k1, k2) -> k2));
        AIUserVideo selectedVideo = idToVideoMap.get(videoId);
        Integer remarkLessonScore = Optional.ofNullable(videoIdToLessonIdMap.get(videoId)).map(lid -> lessonMap.get(lid)).map(l -> l.getScore()).orElse(-1);
        MapMessage message = MapMessage.successMessage();
        if (selectedVideo != null) {
            userVideoDao.updateForRemark(selectedVideo.getId(), selectedLevel, true, remarkLessonScore, qrcolId);
            chipsActiveServiceRecordDao.updateUserVideoId(userId, unitId, videoId);
            chipsActiveServiceRecordDao.updateRemarkStatus(userId, unitId, ChipsActiveServiceRecord.RemarkStatus.One);
            if (selectedVideo.getStatus() != null && selectedVideo.getStatus() == AIUserVideo.ExamineStatus.Passed) {
                return examineVideo(userVideoDao.load(selectedVideo.getId()));
            } else {
                message.add("info", "筛选完成，请去审核");
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 没有视频 ，只有音频,筛选并审核
     * 复习单元-练习（单词跟读、句子跟读）,模考单元1-模考课程1（仅考虑问答练习题）
     */
    private MapMessage filterAndExamineAudioV2(Long userId, String unitId) {
        List<AIUserQuestionResultCollection> questionResultList = userQuestionResultCollectionDao.loadByUidAndUnitId(userId, unitId);
        List<AIUserLessonResultHistory> lessonResultList = userLessonResultHistoryDao.loadByUserIdAndUnitIdWithDisabled(userId, unitId);
        if (CollectionUtils.isEmpty(questionResultList)) {
            return MapMessage.errorMessage().add("info", "can not find userQuestionResult");
        }
        List<AIUserQuestionResultCollection> notFList = questionResultList.stream().filter(e -> e.getLevel() == null || !e.getLevel().equals("F")).collect(Collectors.toList());
        boolean allF = false;
        if (CollectionUtils.isEmpty(notFList)) {
            notFList = questionResultList;
            allF = true;
        }
        //筛选分数最低的用户回答结果
        AIUserQuestionResultCollection answer = getAiUserQuestionResultCollection(notFList);
        //选取最大lesson分数
        Integer lscore = getMaxLessonScore(lessonResultList);
        //构建level
        AiUserVideoLevel level;
        if (allF) {
            level = AiUserVideoLevel.N;
        } else {
            if(lscore < 95){
                level = AiUserVideoLevel.M;
            } else {
                level = AiUserVideoLevel.L;
            }
        }
        String image = loadQuestionImage(answer.getQid());
        if (StringUtils.isNotBlank(image) && StringUtils.isNotBlank(answer.getUserAudio())) {
            List<String> words = getImageAudioWord(answer);
            processUserAudioImageSythesisMessage(userId, unitId, level, answer.getId(), lscore, answer.getUserAudio(), image, words);
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage().add("info", StringUtils.isBlank(image) ? "can not get image " : "can not get userAudio");
        }
    }

    /**
     * 获取图片和音频合成视频时的word
     * @param answer
     * @return
     */
    @NotNull
    private List<String> getImageAudioWord(AIUserQuestionResultCollection answer) {
        return  Optional.ofNullable(answer.getQid())
                        .map(qid -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(qid)))
                        .map(ma -> ma.get(answer.getQid()))
                        .map(StoneQuestionData::newInstance)
                        .map(question -> {
                            List<String> res = new ArrayList<>();
                            switch (question.getSchemaName()) {
                                case word_repeat:
                                    res.add(SafeConverter.toString(question.getJsonData().get("word")));
                                    res.add(SafeConverter.toString(question.getJsonData().get("word_cn")));
                                    break;
                                case sentence_repeat:
                                    res.add(SafeConverter.toString(question.getJsonData().get("sentence")));
                                    break;
                                case mock_qa_audio:
                                    res.add(SafeConverter.toString(question.getJsonData().get("ref_answer_en")));
                                    res.add(SafeConverter.toString(question.getJsonData().get("ref_answer_cn")));
                                    break;
                            }
                            return res;
                        })
                        .orElse(Collections.emptyList());
    }

    /**
     * 有用户合成视频，questionResultCollection中没有userVideo字段
     * 目前是角色扮演的处理逻辑
     * @param userId
     * @param unitId
     * @return
     */
    private MapMessage handleNotUserVideo(Long userId, String unitId) {
        List<AIUserQuestionResultCollection> questionResultList = userQuestionResultCollectionDao.loadByUidAndUnitId(userId, unitId);
        List<AIUserLessonResultHistory> lessonResultList = userLessonResultHistoryDao.loadByUserIdAndUnitIdWithDisabled(userId, unitId);
        if (CollectionUtils.isEmpty(questionResultList)) {
            return MapMessage.errorMessage().add("info", "can not find userQuestionResult");
        }
        List<AIUserQuestionResultCollection> notFList = questionResultList.stream().filter(e -> e.getLevel() == null || !e.getLevel().equals("F")).collect(Collectors.toList());
        boolean allF = false;
        if (CollectionUtils.isEmpty(notFList)) {
            notFList = questionResultList;
            allF = true;
        }
        //筛选分数最低的用户回答结果
        AIUserQuestionResultCollection answer = getAiUserQuestionResultCollection(notFList);
        //选取最大lesson分数
        Integer lscore = getMaxLessonScore(lessonResultList);
        //构建level
        AiUserVideoLevel level;
        if (allF) {
            level = AiUserVideoLevel.N;
        } else {
            if(lscore < 95){
                level = AiUserVideoLevel.M;
            } else {
                level = AiUserVideoLevel.L;
            }
        }
        List<AIUserVideo> userVideoList = userVideoDao.loadByUserIdAndUnitId(userId, unitId).stream().filter(e -> StringUtils.isNotBlank(e.getLessonId())).collect(Collectors.toList());
        AIUserVideo selectedVideo = Optional.ofNullable(userVideoList).filter(l -> CollectionUtils.isNotEmpty(l)).map(l -> l.get(0)).orElse(null);
        if (selectedVideo != null && answer != null) {
            MapMessage message = MapMessage.successMessage();
            userVideoDao.updateForRemark(selectedVideo.getId(), level, true, lscore, answer.getId());
            chipsActiveServiceRecordDao.updateUserVideoId(userId, unitId, selectedVideo.getId());
            chipsActiveServiceRecordDao.updateRemarkStatus(userId, unitId, ChipsActiveServiceRecord.RemarkStatus.One);
            if (selectedVideo.getStatus() != null && selectedVideo.getStatus() == AIUserVideo.ExamineStatus.Passed) {
                return examineVideo(userVideoDao.load(selectedVideo.getId()));
            }
            message.add("info", "筛选完成，请去审核");
            return message;
        } else {
            logger.info("handlevideov2 :" + (selectedVideo == null ? null : selectedVideo.getId()) + ";answer : " + (answer == null ? null : answer.getId()));
            chipsActiveServiceRecordDao.updateRemarkStatus(userId, unitId, ChipsActiveServiceRecord.RemarkStatus.Two);
            return MapMessage.errorMessage().add("info", "can not find userVideo");
        }
    }

    public MapMessage modifyRemarkVideo(String beginDateStr) throws ParseException {
        if (StringUtils.isBlank(beginDateStr)) {
            return MapMessage.errorMessage().add("info", "请输入正确参数");
        }
        Date startDate = DateUtils.parseDate(beginDateStr, DateUtils.FORMAT_SQL_DATETIME);
        List<ChipsActiveServiceRecord> recordList = chipsActiveServiceRecordDao.loadByServiceTypeDate(ChipsActiveServiceType.SERVICE, startDate);
        if (CollectionUtils.isEmpty(recordList)) {
            return MapMessage.successMessage();
        }
        StringBuffer sb = new StringBuffer();
        List<ChipsActiveServiceRecord> toExamineList = recordList.stream().filter(e -> e.getExamineStatus() == null || !e.getExamineStatus()).collect(Collectors.toList());
        List<AIUserVideo> toExamineWithVideoList = toExamineList.stream().filter(e -> StringUtils.isNotBlank(e.getUserVideoId()))
                .map(e -> e.getUserVideoId()).map(userVideoDao::load).filter(e -> e.getStatus() != null && e.getStatus() == AIUserVideo.ExamineStatus.Passed).collect(Collectors.toList());
        for (AIUserVideo video : toExamineWithVideoList) {
            filterVideo(video.getUserId(), video.getUnitId());
            sb.append("userId:").append(video.getUserId()).append("unitId:").append(video.getUnitId()).append(";");
        }

        List<ChipsActiveServiceRecord> toExamineWithoutVideoList = toExamineList.stream().filter(e -> StringUtils.isBlank(e.getUserVideoId())).collect(Collectors.toList());

        for (ChipsActiveServiceRecord record : toExamineWithoutVideoList) {
            filterVideo(record.getUserId(), record.getUnitId());
            sb.append("userId:").append(record.getUserId()).append("unitId:").append(record.getUnitId()).append(";");
        }
        MapMessage message = MapMessage.successMessage();
        message.add("info", sb.toString());
        return message;
    }

    public MapMessage modifyRemarkVideoByReamkStatus(String beginDateStr, String endDateStr, int remark_status, boolean flag) throws ParseException {
        if (StringUtils.isBlank(beginDateStr) || StringUtils.isBlank(endDateStr)) {
            return MapMessage.errorMessage().add("info", "请输入正确的参数");
        }
        Date startDate = DateUtils.parseDate(beginDateStr, DateUtils.FORMAT_SQL_DATETIME);
        Date endDate = DateUtils.parseDate(endDateStr, DateUtils.FORMAT_SQL_DATETIME);
        List<ChipsActiveServiceRecord> recordList = chipsActiveServiceRecordDao.loadByServiceTypeDate(ChipsActiveServiceType.SERVICE, startDate);
        if (CollectionUtils.isEmpty(recordList)) {
            return MapMessage.successMessage();
        }
        StringBuffer sb = new StringBuffer();
        List<ChipsActiveServiceRecord> toExamineList = recordList.stream().filter(e -> e.getCreateDate().before(endDate)).filter(e -> e.getRemarkStatus() != null && e.getRemarkStatus() == remark_status).collect(Collectors.toList());
        for (ChipsActiveServiceRecord record : toExamineList) {
            if (flag) {
                filterVideo(record.getUserId(), record.getUnitId());
            }
            sb.append("userId:").append(record.getUserId()).append("unitId:").append(record.getUnitId()).append(";");
        }
        MapMessage message = MapMessage.successMessage();
        message.add("info", sb.toString());
        return message;
    }
}
