package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Summer on 2018/3/26
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_ai_user_unit_result_plan")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180630")
public class AIUserUnitResultPlan implements Serializable {

    private static final long serialVersionUID = 5173227794979794361L;
    public static final String ID_SEP = "-";

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                          // 唯一id eq: userId + "-" + unitId

    private Long userId;             //用户id
    @Deprecated
    private Long clazzId;            //虚拟班级id 目前是写死的1L和2L
    private String unitId;           //单元id
    private String bookId;           // 教材ID
    private Integer rank;            // 教材排序
    private Integer independent;     // 独立性
    private Integer listening;       // 听力
    private Integer express;         // 表达
    private Integer fluency;         // 流利度
    private Integer pronunciation;   // 发音
    private Integer score;           // 总得分
    private Grade grade;
    private Ability pointAbility;    //重点评价的能力
    private String studyPlan;        //总结
    private Boolean disabled;

    @DocumentCreateTimestamp private Date createDate;
    @DocumentUpdateTimestamp private Date updateDate;

    //缓存key
    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AIUserUnitResultPlan.class, id);
    }

    //缓存key
    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AIUserUnitResultPlan.class, "UID", userId);
    }

    //缓存key
    public static String ck_unit_id(String unitId) {
        return CacheKeyGenerator.generateCacheKey(AIUserUnitResultPlan.class, "UNID", unitId);
    }

    //生成id
    public static String generateId(Long userId, String unitId) {
        return userId + ID_SEP + unitId;
    }

    public enum Grade {
        A,B,C
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Ability {
        Independent("独立"), Listening("听力"), Express("表达"), Fluency("流利"), Pronunciation("发音");
        @Getter
        private final String description;
    }

    public static AIUserUnitResultPlan initPlan(AIUserUnitResultHistory history) {
        AIUserUnitResultPlan plan = new AIUserUnitResultPlan();
        plan.setId(generateId(history.getUserId(), history.getUnitId()));
        plan.setUserId(history.getUserId());
        plan.setUnitId(history.getUnitId());
        plan.setBookId(history.getBookId());
        plan.setExpress(history.getExpress());
        plan.setFluency(history.getFluency());
        plan.setIndependent(history.getIndependent());
        plan.setListening(history.getListening());
        plan.setPronunciation((history.getPronunciation() != null && history.getPronunciation() <= 8) ?
                (new BigDecimal(history.getPronunciation()).divide(new BigDecimal(8), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue()) :
                history.getPronunciation());
        plan.setScore(history.getScore());
        plan.setUpdateDate(new Date());
        plan.setCreateDate(new Date());
        plan.setDisabled(false);

        return plan;
    }
}
