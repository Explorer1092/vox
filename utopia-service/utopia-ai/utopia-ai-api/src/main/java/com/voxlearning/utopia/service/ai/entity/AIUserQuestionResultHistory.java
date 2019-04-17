package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.AIUserQuestionResultRequest;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionResultRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Summer on 2018/3/26
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_ai_user_question_result_history_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180911")
public class AIUserQuestionResultHistory implements Serializable {

    private static final long serialVersionUID = 5146754963145156475L;
    @DocumentId
    private String id;
    private Long userId;
    private String unitId;
    private String lessonId;
    private String qid;
    @Deprecated
    private List<QuestionWeekPoint> weekPoints;   // 薄弱的点 以及用户的回答
    private List<String> userAudio;  // 用户音频
    private String userVideo;        // 用户视频
    @Deprecated
    private List<String> userOriginalVideos; // 用户合成前视频
    private ChipsQuestionType questionType;     // 题目类型
    private String bookId;           // 教材id
    private String userAnswer;       //用户的回答
    private Boolean master;          //是否正确
    private Integer engineScore;     // 打分引擎分数
    private Integer keysIntegrity;   // 关键词完整度
    private Integer sentIntegrity;   // 句子完整度
    private Integer independent;     // 独立性
    private Integer listening;       // 听力
    private Integer express;         // 表达
    private Integer fluency;         // 流利度
    private Integer pronunciation;   // 发音
    private Integer score;           // 回答总得分
    private LessonType lessonType;   // 课程类型--对应取题目类型
    private Integer completeScore;   // 打分引擎-完整性得分
    private String answerLevel;      // 回答类型- A B C D E...
    private Integer deductScore;     // 扣分累计值
    @DocumentCreateTimestamp
    private Date createDate;
    @DocumentUpdateTimestamp
    private Date updateDate;
    private Boolean disabled;

    //缓存key
    public static String ck_userId_lessonId(Long userId, String lessonId) {
        return CacheKeyGenerator.generateCacheKey(AIUserQuestionResultHistory.class, new String[]{"UID", "LID"},
                new Object[]{userId, lessonId});
    }

    //缓存key
    public static String ck_userId_qid(Long userId, String qid) {
        return CacheKeyGenerator.generateCacheKey(AIUserQuestionResultHistory.class, new String[]{"UID", "QID"},
                new Object[]{userId, qid});
    }

    //缓存key
    public static String ck_userId_unitId(Long userId, String unitId) {
        return CacheKeyGenerator.generateCacheKey(AIUserQuestionResultHistory.class, new String[]{"UID", "UNIT_ID"},
                new Object[]{userId, unitId});
    }

    public static AIUserQuestionResultHistory translate(ChipsQuestionResultRequest request, ChipsQuestionType questionType, Long userId, LessonType lessonType) {
        AIUserQuestionResultHistory history = new AIUserQuestionResultHistory();
        history.setQid(request.getQid());
        history.setUnitId(request.getUnitId());
        history.setLessonId(request.getLessonId());
        history.setBookId(request.getBookId());
        if (Boolean.FALSE.equals(request.getMaster())) {
            history.setScore(25);
        } else if (Boolean.TRUE.equals(request.getMaster())) {
            history.setScore(100);
        } else {
            switch (questionType) {
                case video_dialogue:
                case task_topic:
                case qa_sentence:
                case mock_qa:
                case mock_qa_audio:
                    Integer fiveScore = Optional.ofNullable(request.getScore()).map(e -> 5 * e).orElse(0);
                    Integer twoEngineScore = Optional.ofNullable(request.getEngineScore()).map(e -> 2 * e).orElse(0);
                    int newScore = (fiveScore - twoEngineScore) / 3;
                    if (newScore > 100) {
                        newScore = 100;
                    }
                    if (newScore < 0) {
                        newScore = 0;
                    }
                    if (questionType == ChipsQuestionType.video_dialogue || questionType == ChipsQuestionType.task_topic) {
                        history.setScore(newScore > 80 ? 80 : newScore);
                    } else {
                        history.setScore(newScore);
                    }
                    break;
                default:
                    history.setScore(request.getScore() == null || request.getScore().compareTo(0) < 0 ? 0 : request.getScore());
                    break;
            }
        }
        history.setQuestionType(questionType);
        history.setLessonType(lessonType);
        history.setUserId(userId);
        if (CollectionUtils.isNotEmpty(request.getUserAudio())) {
            history.setUserAudio(request.getUserAudio());
        }
        history.setAnswerLevel(request.getAnswerLevel());
        history.setCompleteScore(request.getCompleteScore());
        history.setDeductScore(request.getDeductScore() != null && request.getDeductScore() > 0 ? 16 : 0);
        history.setExpress(request.getExpress());
        history.setFluency(request.getFluency());
        history.setIndependent(request.getIndependent());
        history.setListening(request.getListening());
        history.setPronunciation(request.getPronunciation());
        history.setEngineScore(request.getEngineScore());
        history.setSentIntegrity(request.getSentIntegrity());
        history.setKeysIntegrity(request.getKeysIntegrity());
        history.setCreateDate(new Date());
        history.setUpdateDate(new Date());
        history.setMaster(request.getMaster());
        history.setUserAnswer(request.getUserAnswer());
        history.setDisabled(false);
        return history;
    }

    public static AIUserQuestionResultHistory translate(AIUserQuestionResultRequest request) {
        AIUserQuestionResultHistory history = new AIUserQuestionResultHistory();
        history.setQid(request.getQid());
        history.setUnitId(request.getUnitId());
        history.setLessonId(request.getLessonId());
        history.setWeekPoints(request.getWeekPoints());
        history.setUserAudio(request.getUserAudio());
        history.setIndependent(request.getIndependent());
        history.setListening(request.getListening());
        history.setExpress(request.getExpress());
        history.setFluency(request.getFluency());
        history.setPronunciation(request.getPronunciation());
        history.setScore(request.getScore());
        history.setLessonType(request.getLessonType());
        history.setCompleteScore(request.getCompleteScore());
        history.setAnswerLevel(request.getAnswerLevel());
        history.setDeductScore(request.getDeductScore());
        history.setCreateDate(new Date());
        history.setUpdateDate(new Date());
        history.setDisabled(false);
        return history;
    }


    public static int getValueFromLevel(String level) {
        if (StringUtils.isBlank(level)) {
            return 0;
        }

        int ret = 0;
        switch (level.trim()) {
            case "A+":
                ret = 100;
                break;
            case "A":
                ret = 90;
                break;
            case "B":
                ret = 75;
                break;
            case "C":
                ret = 45;
                break;
            case "D":
                ret = 30;
                break;
            case "E":
                ret = 0;
                break;
            case "F":
                ret = 0;
            default:
        }
        return ret;
    }

}
