package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntityWithDisabledField;
import com.voxlearning.utopia.service.ai.constant.ChipsEnglishTeacher;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 薯条班级
 */

@Data
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_CHIPS_ENGLISH_CLASS")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190121")
public class ChipsEnglishClass extends LongIdEntityWithDisabledField {

    @UtopiaSqlColumn(name = "NAME") private String name;
    @UtopiaSqlColumn(name = "TEACHER") private String teacher;
    @UtopiaSqlColumn(name = "PRODUCT_ID") private String productId;
    @UtopiaSqlColumn(name = "USER_LIMIT") private Integer userLimit;
    @UtopiaSqlColumn(name = "TYPE") private Type type;


    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Type {
        NORMAL("普通班"), MARKING_BRAND("地推班");
        @Getter
        private final String description;

        public static Type safeOf(String name) {
            try {
                return Type.valueOf(name);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ChipsEnglishClass.class, id);
    }

    //缓存key
    public static String ck_product_id(String productId) {
        return CacheKeyGenerator.generateCacheKey(ChipsEnglishClass.class, "PID", productId);
    }
    public static String ck_teacher(String teacher) {
        return CacheKeyGenerator.generateCacheKey(ChipsEnglishClass.class, "TID", teacher);
    }

    public ChipsEnglishTeacher getTeacherInfo() {
        return ChipsEnglishTeacher.safe(this.getTeacher());
    }
}
