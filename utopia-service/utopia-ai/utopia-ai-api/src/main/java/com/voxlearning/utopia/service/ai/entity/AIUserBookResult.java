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
import java.util.Date;
import java.util.List;

/**
 * 定级报告
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_ai_user_book_result")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180627")
public class AIUserBookResult implements Serializable {

    private static final long serialVersionUID = 5173227794979794361L;
    public static final String ID_SEP = "-";

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                          // 唯一id eq: userId + "-" + bookId

    private Long userId;             //用户id
    private String bookId;           // 教材ID
    private Integer score;           // 得分
    private Level level;           // 等级
    private String enSummary;
    private String cnSummary;
    private List<Week> warmUp;
    private List<Week> diaglogue;
    private List<Week> task;

    private Boolean disabled;

    @DocumentCreateTimestamp private Date createDate;
    @DocumentUpdateTimestamp private Date updateDate;

    //缓存key
    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AIUserBookResult.class, id);
    }

    //缓存key
    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AIUserBookResult.class, "UID", userId);
    }

    //生成id
    public static String generateId(Long userId, String bookId) {
        return userId + ID_SEP + bookId;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Level {
        One("一级"), Two("二级"), Three("三级");
        @Getter
        private final String description;
    }

    public enum Week {
        CS,G,L,P
    }
}
