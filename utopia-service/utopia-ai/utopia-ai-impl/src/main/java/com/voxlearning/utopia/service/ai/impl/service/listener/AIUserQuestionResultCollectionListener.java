package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractContext;
import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractV2Context;
import com.voxlearning.utopia.service.ai.context.AIUserPerQuestionContext;
import com.voxlearning.utopia.service.ai.context.AIUserQuestionContext;
import com.voxlearning.utopia.service.ai.data.AIQuestionAppraisionRequest;
import com.voxlearning.utopia.service.ai.data.AIUserQuestionResultRequest;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultCollection;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.context.ChipsNewTalkCollectContext;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultCollectContext;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserQuestionResultCollectionDao;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.question.result.collect.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.question.result.collect.queue")
        },
        maxPermits = 64
)
@Slf4j
public class AIUserQuestionResultCollectionListener implements MessageListener {

    @Inject
    private AIUserQuestionResultCollectionDao aiUserQuestionResultCollectionDao;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Inject
    private ChipsUserService chipsUserService;

    @Inject
    private AICacheSystem aiCacheSystem;

    @Inject
    protected StoneDataLoaderClient stoneDataLoaderClient;

    /**
     * 薯条英语用户答题结果上报
     */
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.chips.user.question.result.collection", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer chipsQuestionResultCollectionProducer;


    private static List<ChipsQuestionType> COUNT_QUESTION_TYPE_LIST = Arrays.asList(ChipsQuestionType.mock_qa, ChipsQuestionType.sentence_repeat, ChipsQuestionType.qa_sentence, ChipsQuestionType.task_conversation, ChipsQuestionType.video_conversation, ChipsQuestionType.task_topic,
            ChipsQuestionType.video_dialogue, ChipsQuestionType.role_play_practice);

    private void processTalkLessonV2(AITalkLessonInteractV2Context context) {

        User user = context.getUser();
        if (user == null) {
            log.warn("Ai user question result collect user is null");
            return;
        }

        Long userId = user.getId();
        if (userId == null) {
            log.warn("Ai user question result collect userId is null");
            return;
        }
        String lessonId = context.getLessonId();
        if (blank(lessonId)) {
            log.warn("Ai user question result collect lessonId is null");
            return;
        }
        if (lessonId.equals(context.getInput())) {
            return;
        }
        String unitId = context.getUnitId();
        Map<String, Object> map = context.getResult();

        if (map.get("data") == null || ((Collection) map.get("data")).isEmpty()) {
            log.warn("Ai user question result collect response data is null or empty");
            return;
        }

        List data = ((List) map.get("data"));
        Map data0 = (Map) data.get(0);
        Map content = (Map) data0.get("content");

        String path = String.valueOf(data0.get("path"));
        String qid = "";

        if (!blank(path)) {
            int idx = path.indexOf("-contents");
            if (idx > 0) {
                qid = path.substring(0, idx);
            } else {
                log.error("AIUserQuestionResultCollectionListener Question id is not correct, path value: {}", path);
            }

        }
        if (blank(qid)) {
            log.warn("Ai user question result collect question id is null");
            return;
        }

        String level = String.valueOf(content.get("level"));
        String input = context.getInput();
        AIQuestionAppraisionRequest request = JsonUtils.fromJson(input, AIQuestionAppraisionRequest.class);
        List<AIQuestionAppraisionRequest.Line> lines = request.getLines();

        if (null == lines || lines.isEmpty()) {
            log.warn("Ai user question result collect lines is null or empty");
            return;
        }

        // Get first record now
        AIQuestionAppraisionRequest.Line req = lines.get(0);
        String useAudio = request.getVoiceURI();
        AIUserQuestionResultCollection po = new AIUserQuestionResultCollection();
        po.setUserId(userId);
        po.setUnitId(unitId);
        po.setBookId(context.getBookId());
        po.setLessonId(lessonId);
        po.setLessonType(context.getType());
        po.setQid(qid);
        po.setUserAudio(useAudio);
        // cal
        po.setScore(calScore(context.getType(), req.getScore(), level));
        po.setIndependent(-1);
        po.setListening(-1);
        po.setExpress(-1);
        po.setFluency(req.getFluency());
        po.setOriginFluency(req.getFluency());
        BigDecimal pronunciation = req.getPronunciation();
        po.setOriginPronunciation(pronunciation);
        if (pronunciation != null) {
            // cast to 100
            pronunciation = pronunciation.multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP);
        }
        po.setPronunciation(pronunciation);

        po.setVoiceEngineJson(input);
        po.setOriginScore(req.getScore());
        po.setSample(req.getSample());
        po.setUserText(req.getUsertext());
        po.setDuration(req.getEnd());
        po.setIntegrity(req.getIntegrity());
        po.setLevel(level);
        po.setDeductScore(new BigDecimal(-1));

        po.setDisabled(false);
        po.setQuestionType(context.getQuestionType());
        po.setStandardScore(req.getStandardScore());
        po.setSessionId(getSessionId(context.getType(), userId, lessonId));
        aiUserQuestionResultCollectionDao.disableOld(userId, qid);
        aiUserQuestionResultCollectionDao.insert(po);

        processAddUserSentenceNum(context.getQuestionType(), userId);
        syncResult2BigData(po);
    }

    private void processNewTalk(ChipsNewTalkCollectContext context) {
        String lessonId = context.getLessonId();
        String unitId = context.getUnitId();
        String level = context.getLevel();
        String input = context.getInput();
        AIQuestionAppraisionRequest request = JsonUtils.fromJson(input, AIQuestionAppraisionRequest.class);
        List<AIQuestionAppraisionRequest.Line> lines = request.getLines();
        if (null == lines || lines.isEmpty()) {
            log.warn("Ai user question result collect lines is null or empty");
            return;
        }
        AIQuestionAppraisionRequest.Line req = lines.get(0);
        String useAudio = request.getVoiceURI();
        AIUserQuestionResultCollection po = new AIUserQuestionResultCollection();
        po.setUserId(context.getUserId());
        po.setUnitId(unitId);
        po.setBookId(context.getBookId());
        po.setLessonId(lessonId);
        po.setLessonType(context.getLessonType());
        po.setQid(context.getQid());
        po.setUserAudio(useAudio);
        // cal
        po.setScore(calScoreV2(context.getLessonType(), req.getScore(), level));
        po.setIndependent(-1);
        po.setListening(-1);
        po.setExpress(-1);
        po.setFluency(req.getFluency());
        po.setOriginFluency(req.getFluency());
        BigDecimal pronunciation = req.getPronunciation();
        po.setOriginPronunciation(pronunciation);
        if (pronunciation != null) {
            // cast to 100
            pronunciation = pronunciation.multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP);
        }
        po.setPronunciation(pronunciation);
        po.setUserVideo(context.getUserVideo());

        po.setVoiceEngineJson(input);
        po.setOriginScore(req.getScore());
        po.setSample(req.getSample());
        po.setUserText(req.getUsertext());
        po.setDuration(req.getEnd());
        po.setIntegrity(req.getIntegrity());
        po.setLevel(level);
        po.setDeductScore(new BigDecimal(-1));

        po.setDisabled(false);
        po.setQuestionType(context.getQuestionType());
        po.setStandardScore(req.getStandardScore());
        aiUserQuestionResultCollectionDao.disableOld(context.getUserId(), context.getQid());
        aiUserQuestionResultCollectionDao.insert(po);

        processAddUserSentenceNum(context.getQuestionType(), context.getUserId());
        syncResult2BigData(po);
    }

    private void processTalkLesson(AITalkLessonInteractContext context) {

        User user = context.getUser();
        if (user == null) {
            log.warn("Ai user question result collect user is null");
            return;
        }

        Long userId = user.getId();
        if (userId == null) {
            log.warn("Ai user question result collect userId is null");
            return;
        }

        String lessonId = context.getLessonId();
        if (blank(lessonId)) {
            log.warn("Ai user question result collect lessonId is null");
            return;
        }


        if (lessonId.equals(context.getInput())) {
            return;
        }

        NewBookCatalog nbc = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
        String unitId = nbc.unitId();

        Map<String, Object> map = context.getResult();

        if (map.get("data") == null || ((Collection) map.get("data")).isEmpty()) {
            log.warn("Ai user question result collect response data is null or empty");
            return;
        }


        List data = ((List) map.get("data"));
        Map data0 = (Map) data.get(0);

        Map content = (Map) data0.get("content");

        String path = String.valueOf(data0.get("path"));
        String qid = "";

        if (!blank(path)) {
            int idx = path.indexOf("-contents");
            if (idx > 0) {
                qid = path.substring(0, idx);
            } else {
                log.error("AIUserQuestionResultCollectionListener Question id is not correct, path value: {}", path);
            }

        }

        if (blank(qid)) {
            log.warn("Ai user question result collect question id is null");
            return;
        }

        String level = String.valueOf(content.get("level"));


        String input = context.getInput();
        AIQuestionAppraisionRequest request = JsonUtils.fromJson(input, AIQuestionAppraisionRequest.class);

        List<AIQuestionAppraisionRequest.Line> lines = request.getLines();

        if (null == lines || lines.isEmpty()) {
            log.warn("Ai user question result collect lines is null or empty");
            return;
        }

        // Get first record now
        AIQuestionAppraisionRequest.Line req = lines.get(0);


        String useAudio = request.getVoiceURI();


        AIUserQuestionResultCollection po = new AIUserQuestionResultCollection();
        po.setUserId(userId);
        po.setUnitId(unitId);
        po.setBookId(nbc.bookId());
        po.setLessonId(lessonId);
        po.setLessonType(context.getType());
        po.setQid(qid);
        po.setUserAudio(useAudio);
        // cal
        po.setScore(calScore(context.getType(), req.getScore(), level));

        po.setIndependent(-1);
        po.setListening(-1);
        po.setExpress(-1);
        po.setFluency(req.getFluency());
        po.setOriginFluency(req.getFluency());

        BigDecimal pronunciation = req.getPronunciation();

        po.setOriginPronunciation(pronunciation);


        if (pronunciation != null) {
            // cast to 100
            pronunciation = pronunciation.multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP);
        }
        po.setPronunciation(pronunciation);

        po.setVoiceEngineJson(input);
        po.setOriginScore(req.getScore());
        po.setSample(req.getSample());
        po.setUserText(req.getUsertext());
        po.setDuration(req.getEnd());
        po.setIntegrity(req.getIntegrity());
        po.setLevel(level);
        po.setDeductScore(new BigDecimal(-1));

        po.setDisabled(false);
        po.setStandardScore(req.getStandardScore());

        po.setSessionId(getSessionId(context.getType(), userId, lessonId));

        aiUserQuestionResultCollectionDao.disableOld(userId, qid);
        aiUserQuestionResultCollectionDao.insert(po);

        processAddUserSentenceNum(ChipsQuestionType.task_conversation, userId);
    }

    private void processQuestion(AIUserQuestionContext context) {

        User user = context.getUser();
        if (user == null) {
            log.warn("Ai user question result collect user is null");
            return;
        }


        Long userId = user.getId();
        if (userId == null) {
            log.warn("Ai user question result collect userId is null");
            return;
        }

        AIUserQuestionResultRequest req = context.getAiUserQuestionResultRequest();
        String qid = req.getQid();
        if (blank(qid)) {
            log.warn("Ai user question result collect question id is null");
            return;
        }

        BigDecimal dValue = new BigDecimal(-1);

        AIUserQuestionResultCollection po = new AIUserQuestionResultCollection();
        po.setUserId(userId);
        po.setUnitId(req.getUnitId());
        po.setLessonId(req.getLessonId());
        po.setLessonType(req.getLessonType());
        po.setQid(qid);
        po.setUserAudio(req.getUserAudio() == null ? "" : req.getUserAudio().get(0));
        po.setScore(new BigDecimal(-1));
        po.setIndependent(req.getIndependent());
        po.setListening(req.getListening());
        po.setExpress(req.getExpress());
        po.setFluency(new BigDecimal(req.getFluency()));
        po.setOriginFluency(new BigDecimal(req.getFluency()));
        po.setOriginPronunciation(new BigDecimal(req.getPronunciation()));

        BigDecimal pronunciation = null;
        if (req.getPronunciation() != null) {
            // cast to 100
            pronunciation = new BigDecimal(req.getPronunciation()).multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP);
        }


        po.setPronunciation(pronunciation);
        po.setVoiceEngineJson(context.getData());
        po.setOriginScore(new BigDecimal(req.getScore()));
        po.setSample("");
        po.setUserText("");
        po.setDuration(dValue);
        po.setIntegrity(dValue);
        po.setLevel("");
        po.setDeductScore(new BigDecimal(req.getDeductScore()));
        po.setDisabled(false);
        po.setStandardScore(dValue);


        aiUserQuestionResultCollectionDao.disableOld(userId, qid);
        aiUserQuestionResultCollectionDao.insert(po);
    }


    private void processPerQuestion(AIUserPerQuestionContext context) {

        User user = context.getUser();
        if (user == null) {
            log.warn("Ai user question result collect user is null");
            return;
        }


        Long userId = user.getId();
        if (userId == null) {
            log.warn("Ai user question result collect userId is null");
            return;
        }


        String lessonId = context.getLessonId();
        if (blank(lessonId)) {
            log.warn("Ai user per question result collect lessonId is null");
            return;
        }


        String qid = context.getQid();

        if (blank(qid)) {
            log.warn("Ai user question result collect question id is null");
            return;
        }


        String unitId = context.getUnitId();


        String input = context.getInput();
        AIQuestionAppraisionRequest request = JsonUtils.fromJson(input, AIQuestionAppraisionRequest.class);


        List<AIQuestionAppraisionRequest.Line> lines = request.getLines();

        if (null == lines || lines.isEmpty()) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod1", lessonId,
                    "mod2", qid,
                    "mod3", JsonUtils.toJson(request),
                    "op", "ai user question result collection"
            ));
            return;
        }

        // Get first record now
        AIQuestionAppraisionRequest.Line req = lines.get(0);


        String useAudio = request.getVoiceURI();


        AIUserQuestionResultCollection po = new AIUserQuestionResultCollection();
        po.setUserId(userId);
        po.setUnitId(unitId);
        po.setLessonId(lessonId);
        po.setLessonType(context.getType());
        po.setQid(qid);
        po.setUserAudio(useAudio);
        // cal
        po.setScore(req.getScore());

        po.setIndependent(-1);
        po.setListening(-1);
        po.setExpress(-1);
        po.setFluency(req.getFluency());
        po.setOriginFluency(req.getFluency());

        BigDecimal pronunciation = req.getPronunciation();

        po.setOriginPronunciation(pronunciation);


        if (pronunciation != null) {
            // cast to 100
            pronunciation = pronunciation.multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP);
        }
        po.setPronunciation(pronunciation);

        po.setVoiceEngineJson(input);
        po.setOriginScore(req.getScore());
        po.setSample(req.getSample());
        po.setUserText(req.getUsertext());
        po.setDuration(req.getEnd());
        po.setIntegrity(req.getIntegrity());
        po.setLevel("");
        po.setDeductScore(new BigDecimal(-1));

        po.setDisabled(false);
        po.setStandardScore(req.getStandardScore());

        po.setSessionId(getSessionId(context.getType(), userId, lessonId));

        aiUserQuestionResultCollectionDao.disableOld(userId, qid);
        aiUserQuestionResultCollectionDao.insert(po);
    }

    /**
     * ChipsEnglish v2 process
     * @param context
     */
    private void processChipsQuestionResult(ChipsQuestionResultCollectContext context) {
        Long userId = context.getUserId();
        if (userId == null) {
            log.warn("Ai user chips question result collect userId is null");
            return;
        }

        String qid = context.getQid();

        if (blank(qid)) {
            log.warn("Ai user chips question result collect question id is null");
            return;
        }

        AIUserQuestionResultCollection po = new AIUserQuestionResultCollection();
        po.setUserId(userId);
        po.setUnitId(context.getUnitId());
        po.setLessonId(context.getLessonId());
        po.setBookId(context.getBookId());
        po.setUserVideo(context.getUserVideo());
        po.setQid(qid);
        po.setQuestionType(context.getQuestionType());
        po.setVoiceEngineJson(context.getInput());


        po.setLessonType(getLessonType(context.getLessonId()));
        po.setSessionId(getSessionId(po.getLessonType(), userId, po.getLessonId()));

        ChipsQuestionType type = context.getQuestionType();
        switch (type) {
            case choice_lead_in:
            case choice_sentence2pic:
            case choice_word2pic:
            case choice_word2trans:
            case choice_sentence2audio:
            case choice_cultural:
            case mock_choice:
                // 选择题
                // input => {"userAnswer":"", "master":true}
                optionTypeHandler(po, context);
                break;
            default:
                // input => {打分引擎返回结果}
                engineTypeHandler(po, context);
        }


        po.setDisabled(false);
        aiUserQuestionResultCollectionDao.disableOld(userId, qid);
        aiUserQuestionResultCollectionDao.insert(po);

        processAddUserSentenceNum(context.getQuestionType(), userId);
    }


    private void optionTypeHandler(AIUserQuestionResultCollection po, ChipsQuestionResultCollectContext context) {
        Map<String, Object> input = JsonUtils.fromJson(context.getInput());
        if (input != null) {
            po.setUserAnswer(SafeConverter.toString(input.get("userAnswer")));
            Boolean master = SafeConverter.toBoolean(input.get("master"));
            po.setMaster(master);
            int score = master ? 100 : 25;
            po.setScore(new BigDecimal(score));
            po.setOriginScore(new BigDecimal(score));

        }

    }


    private void engineTypeHandler(AIUserQuestionResultCollection po, ChipsQuestionResultCollectContext context) {

        String input = context.getInput();
        AIQuestionAppraisionRequest request = JsonUtils.fromJson(input, AIQuestionAppraisionRequest.class);

        List<AIQuestionAppraisionRequest.Line> lines = Optional.ofNullable(request).map(AIQuestionAppraisionRequest::getLines).orElse(null);

        if (null == lines || lines.isEmpty()) {
            log.warn("Ai user question result collect lines is null or empty");
            return;
        }

        String useAudio = request.getVoiceURI();

        // Get first record now
        AIQuestionAppraisionRequest.Line req = lines.get(0);

        po.setUserAudio(useAudio);
        po.setScore(req.getScore());
        po.setStandardScore(req.getStandardScore());
        po.setSample(req.getSample());
        po.setUserText(req.getUsertext());
        po.setFluency(req.getFluency());
        po.setIntegrity(req.getIntegrity());
        po.setPronunciation(req.getPronunciation());
        po.setBusinessLevel(req.getBusinessLevel());

        po.setDuration(req.getEnd());

        po.setOriginFluency(req.getFluency());
        po.setOriginPronunciation(req.getPronunciation());
        po.setOriginScore(req.getScore());

    }


    @Override
    public void onMessage(Message message) {
        if (message == null) {
            log.error("Ai user question result collect  queue no message");
            return;
        }

        Object body = message.decodeBody();

        if (body != null) {
            try {

                // Lesson type is Dialogue or Task
                if (body instanceof AITalkLessonInteractV2Context) {
                    processTalkLessonV2((AITalkLessonInteractV2Context) body);
                } else if (body instanceof AITalkLessonInteractContext) {
                    processTalkLesson((AITalkLessonInteractContext) body);
                } else if (body instanceof ChipsNewTalkCollectContext) {
                    processNewTalk((ChipsNewTalkCollectContext) body);
                }else if (body instanceof AIUserQuestionContext) {
                    // Lesson type is WarmUp
                    processQuestion((AIUserQuestionContext) body);
                } else if (body instanceof AIUserPerQuestionContext) {
                    // per question collect
                    processPerQuestion((AIUserPerQuestionContext) body);
                } else if (body instanceof ChipsQuestionResultCollectContext) {
                    // chips v2
                    processChipsQuestionResult((ChipsQuestionResultCollectContext) body);
                }
            } catch (Exception e) {
                // Cache all Exception
                log.error("Ai user question result collect error, case: {}", e.getMessage(), e);
            }
        }
    }


    private LessonType getLessonType(String lessonId) {
        Map<String, StoneData> lessonData = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(lessonId));
        if (lessonData.size() > 0) {
            StoneData obj = lessonData.entrySet().iterator().next().getValue();
            StoneLessonData data = StoneLessonData.newInstance(obj);
            return Optional.of(data.getJsonData()).map(StoneLessonData.Lesson::getLesson_type).orElse(null);

        }
        return null;
    }


    private String getSessionId(LessonType type, long userId, String lessonId) {
        String ret = "";
        if (LessonType.Dialogue == type || LessonType.video_conversation == type) {

            ret = aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().getSessonId(userId, lessonId);
        } else if (LessonType.Task == type || LessonType.task_conversation == type) {
            ret = aiCacheSystem.getUserTaskTalkSceneResultCacheManager().getSessonId(userId, lessonId);
        }
        return ret;
    }

    private BigDecimal calScore(LessonType type, BigDecimal score, String level) {

        double ret = 0;
        if (LessonType.Dialogue == type || LessonType.video_conversation == type) {
            // 单句得分如下=打分引擎分数*30%+回答类型得分*70%
            ret = score.doubleValue() * 0.3 + AIUserQuestionResultHistory.getValueFromLevel(level) * 0.7;

        } else if (LessonType.Task == type || LessonType.task_conversation == type) {
            // 单句得分如下=打分引擎分数*30%+回答类型得分*70%
            ret = score.doubleValue() * 0.3 + AIUserQuestionResultHistory.getValueFromLevel(level) * 0.7;
        }
        return BigDecimal.valueOf(ret);
    }

    private BigDecimal calScoreV2(LessonType type, BigDecimal score, String level) {
        double ret = score.doubleValue() * 0.5 + AIUserQuestionResultHistory.getValueFromLevel(level) * 0.5;
        return BigDecimal.valueOf(ret);
    }

    private void syncResult2BigData(AIUserQuestionResultCollection po) {
        if (null == po) return;
        po.setEnv(RuntimeMode.getCurrentStage());
        String json=JsonUtils.toJson(po);
        if (blank(json))return;
        Message message = Message.newMessage();
        message.withPlainTextBody(json);
        chipsQuestionResultCollectionProducer.produce(message);
    }




    private void processAddUserSentenceNum(ChipsQuestionType questionType, Long userId) {
        if (COUNT_QUESTION_TYPE_LIST.contains(questionType)) {
            chipsUserService.addUserSentenceLearnNum(userId);
        }
    }

    private boolean empty(Collection list) {
        return CollectionUtils.isEmpty(list);
    }

    private boolean blank(CharSequence src) {
        return StringUtils.isBlank(src);
    }
}
