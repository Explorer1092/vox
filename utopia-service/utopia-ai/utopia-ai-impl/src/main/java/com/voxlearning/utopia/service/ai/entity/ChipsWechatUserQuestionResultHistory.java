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
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionResultRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_wechat_user_question_result_history")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190311")
public class ChipsWechatUserQuestionResultHistory implements Serializable {

    private static final long serialVersionUID = 5146754963145156475L;
    @DocumentId private String id;
    private Long userId;
    private String unitId;
    private String lessonId;
    private String qid;
    private List<String> userAudio;  // 用户音频
    private ChipsQuestionType questionType;     // 题目类型
    private String bookId;           // 教材id
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
    @DocumentCreateTimestamp private Date createDate;
    @DocumentUpdateTimestamp private Date updateDate;
    private Boolean disabled;

    //缓存key
    public static String ck_userId_lessonId(Long userId, String lessonId) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserQuestionResultHistory.class, new String[]{"UID", "LID"},
                new Object[]{userId, lessonId});
    }

    //缓存key
    public static String ck_userId_qid(Long userId, String qid) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserQuestionResultHistory.class, new String[]{"UID", "QID"},
                new Object[]{userId, qid});
    }

    //缓存key
    public static String ck_userId_unitId(Long userId, String unitId) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserQuestionResultHistory.class, new String[]{"UID", "UNIT_ID"},
                new Object[]{userId, unitId});
    }

    public static ChipsWechatUserQuestionResultHistory translate(ChipsQuestionResultRequest request, ChipsQuestionType questionType, Long userId, LessonType lessonType) {
        ChipsWechatUserQuestionResultHistory history = new ChipsWechatUserQuestionResultHistory();
        history.setQid(request.getQid());
        history.setUnitId(request.getUnitId());
        history.setLessonId(request.getLessonId());
        history.setBookId(request.getBookId());
        history.setQuestionType(questionType);
        history.setLessonType(lessonType);
        history.setUserId(userId);
        if (CollectionUtils.isNotEmpty(request.getUserAudio())) {
            history.setUserAudio(request.getUserAudio());
        }
        history.setAnswerLevel(request.getAnswerLevel());
        history.setCompleteScore(request.getCompleteScore());
        history.setDeductScore(request.getDeductScore());
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
        history.setDisabled(false);
        history.setScore(request.getScore());
        return history;
    }
}
