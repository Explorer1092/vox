package com.voxlearning.utopia.service.action.api.document;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.*;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author xinxin
 * @since 19/8/2016
 * 用户签到记录
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-app")
@DocumentDatabase(database = "vox-attendance-{}", dynamic = true)
@DocumentCollection(collection = "vox_user_attendance_log_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class UserAttendanceLog implements CacheDimensionDocument {

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    @DocumentField("ct")
    private Date createTime;
    @DocumentUpdateTimestamp
    @DocumentField("ut")
    private Date updateTime;

    private Long userId;
    private Long clazzId;
    private Date signDate;  //签到日期

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id),
                newCacheKey("CID", clazzId)
        };
    }

    public static String generateId(Long clazzId, Long userId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return clazzId + "-" + userId + "-" + formatter.format(LocalDateTime.now());
    }
}
