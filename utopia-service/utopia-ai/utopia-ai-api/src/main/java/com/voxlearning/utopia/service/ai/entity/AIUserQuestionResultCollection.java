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
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_ai_user_question_result_collection")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181207")
public class AIUserQuestionResultCollection implements Serializable {

    private static final long serialVersionUID = 2176515195037771813L;
    @DocumentId
    private String id;

    private Long userId; // 用户Id
    private String bookId; // 课本Id
    private String unitId; // 单元Id
    private String lessonId; // 课程模块, (热身warmup, 情景dialogue, 任务task)
    private LessonType lessonType; // 课程类型
    private String qid; // 题目Id(热身里，每个单词/句子对应一个qid, 情景和任务里，每一个topic对应一个qid, 取郭老师返回的pathid)
    private String userAudio; //用户音频
    private BigDecimal score; // 客户端显示分数
    private Integer independent; // 独立性维度分数
    private Integer listening; // 听力维度分数
    private Integer express; // 表达维度分数
    private BigDecimal fluency; // 流利性维度分数
    private BigDecimal pronunciation; // 发音维度分数
    private String voiceEngineJson; // 语音引擎返回json
    private BigDecimal originScore; // 打分引擎百分制分数(语音引擎返回)
    private BigDecimal standardScore; // 打分引擎8分制分数(语音引擎返回，仅情景和任务有)
    private String sample; // 匹配上的sample语句(语音引擎返回)
    private String userText; // 用户语句(语音引擎返回)
    private BigDecimal duration; // 时长(取打分引擎里的end)
    private BigDecimal integrity; // 打分引擎完整性维度
    private String level; // 用户回答等级(对话引擎返回)
    private BigDecimal deductScore; // 扣除分数
    private Long answerN; // 第N次回答
    private Boolean disabled; // 是否是最新数据

    private BigDecimal originPronunciation; // 打分引擎发音维度
    private BigDecimal originFluency; // 打分引擎流利度维度
    private String sessionId; // 用户做题session唯一性(answerN用到)

    private BigDecimal businessLevel; //打分系数，【0，2】之间保留1位小数，1为默认标准，数字越大打分越宽松，打分偏高。只会影响百分制成绩

    private ChipsQuestionType questionType; // 题型类型
    private String userAnswer; // 用户选择的option
    private Boolean master; // 是否正确

    private String env; // 当前环境

    private String userVideo;//用户的视频


    @DocumentCreateTimestamp
    private Date createDate; // 做题时间
    @DocumentUpdateTimestamp
    private Date updateDate; // 更新时间

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

}
