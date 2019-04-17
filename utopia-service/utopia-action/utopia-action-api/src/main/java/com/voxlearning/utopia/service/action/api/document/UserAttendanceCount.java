package com.voxlearning.utopia.service.action.api.document;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author xinxin
 * @since 11/10/2016
 * 用户每月的签到次数计数表
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-app")
@DocumentDatabase(database = "vox-attendance-{}", dynamic = true)
@DocumentCollection(collection = "vox_user_attendance_count")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class UserAttendanceCount implements CacheDimensionDocument {

    private static final long serialVersionUID = 915363107718865991L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    @DocumentField("ct")
    private Date createTime;
    @DocumentUpdateTimestamp
    @DocumentField("ut")
    private Date updateTime;

    private Long userId;
    private Integer count;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    public static String generateId(Long userId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        String month = formatter.format(LocalDateTime.now());

        return userId + "-" + month;
    }
}
