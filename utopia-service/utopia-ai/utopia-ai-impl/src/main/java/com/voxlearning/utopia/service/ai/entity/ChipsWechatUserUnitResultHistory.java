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
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_wechat_user_unit_result_history")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180911")
public class ChipsWechatUserUnitResultHistory implements Serializable {

    private static final long serialVersionUID = 5173227794979794361L;

    @DocumentId
    private String id;                          // 唯一id eq: userId + "-" + unitId

    private Long userId;             //用户id
    private String unitId;           //单元id
    private String bookId;           // 教材ID
    private Boolean finished;
    private String video;            // 情景对话视频地址
    private String userVideoId;      // 用户视频id
    private ChipsUnitType unitType;
    private Integer engineScore;    // 打分引擎分数
    private Integer keysIntegrity;  // 关键词完整度
    private Integer sentIntegrity;  // 句子完整度
    private Integer independent;     // 独立性
    private Integer listening;       // 听力
    private Integer express;         // 表达
    private Integer fluency;         // 流利度
    private Integer pronunciation;   // 发音
    private Integer star;           // 星星数
    private Integer score;          // 得分
    private Map<String, Object> ext; //额外的属性
    private Boolean disabled; // 是否是最新数据

    @DocumentCreateTimestamp private Date createDate;
    @DocumentUpdateTimestamp private Date updateDate;

    //缓存key
    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserUnitResultHistory.class, id);
    }

    //缓存key
    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserUnitResultHistory.class, "UID", userId);
    }

    //缓存key
    public static String ck_unit_id(String unitId) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserUnitResultHistory.class, "UNID", unitId);
    }

    //缓存key
    public static String ck_userId_unitId(Long userId, String unitId) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserUnitResultHistory.class, new String[]{"UID", "UNIT_ID"},
                new Object[]{userId, unitId});
    }

    public static ChipsWechatUserUnitResultHistory build(Long userId, String unitId, ChipsUnitType unitType, String bookId) {
        ChipsWechatUserUnitResultHistory newUnitResult = new ChipsWechatUserUnitResultHistory();
        newUnitResult.setUnitId(unitId);
        newUnitResult.setUserId(userId);
        newUnitResult.setBookId(bookId);
        newUnitResult.setUnitType(unitType);
        newUnitResult.setFinished(true);
        newUnitResult.setUpdateDate(new Date());
        newUnitResult.setCreateDate(new Date());
        newUnitResult.setDisabled(false);
        return newUnitResult;
    }
}
