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
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-app")
@DocumentDatabase(database = "vox-attendance-{}", dynamic = true)
@DocumentCollection(collection = "vox_clazz_attendance_count_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ClazzAttendanceCount implements CacheDimensionDocument {

    private static final long serialVersionUID = -8569905201040051474L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    @DocumentField("ct")
    private Date createTime;
    @DocumentUpdateTimestamp
    @DocumentField("ut")
    private Date updateTime;

    private Long clazzId;
    private Long schoolId;
    private Integer totalCount;   //班级内学生总数
    private Integer count;  //已签到学生数量
    private String day;


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    public static String generateId(Long schoolId, Long clazzId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String day = formatter.format(LocalDateTime.now());
        return schoolId + "-" + day + "-" + clazzId;
    }
}
