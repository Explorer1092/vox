package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户课程选教材历史
 */
@Getter
@Setter
@ToString
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_AI_USER_LESSON_BOOK_REF")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180817")
public class AIUserLessonBookRef extends LongIdEntityWithDisabledField {

    private static final long serialVersionUID = 4364089815230239584L;
    @UtopiaSqlColumn(name = "USER_ID") private Long userId;
    @UtopiaSqlColumn(name = "BOOK_ID") private String bookId;
    @UtopiaSqlColumn(name = "BOOK_NAME") private String bookName;
    @UtopiaSqlColumn(name = "PRODUCT_ID") private String productId;

    //缓存key
    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AIUserLessonBookRef.class, "UID", userId);
    }

}
