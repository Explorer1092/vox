package com.voxlearning.utopia.schedule.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TEACHER_BACK")
@UtopiaCacheRevision("20190314")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TeacherBack extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {
    private static final long serialVersionUID = 7184326435518111830L;

    private Date pushTime;               // push 时间
    private Date smsTime;                // 发短信时间
    private Date awakenTime;             // 进入唤醒列表时间
    private Date marketTime;             // 进入市场人员进校任务时间

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                CacheKeyGenerator.generateCacheKey(TeacherBack.class, id)
        };
    }

}
