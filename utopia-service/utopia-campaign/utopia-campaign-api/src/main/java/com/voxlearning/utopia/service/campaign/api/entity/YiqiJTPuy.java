package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 一起讲堂 - 购买信息
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_17JT_PUY")
@CacheBean(type = YiqiJTPuy.class)
@UtopiaCacheRevision("20180619")
public class YiqiJTPuy extends AbstractDatabaseEntity {

    @UtopiaSqlColumn(name = "COURSE_ID") private Long courseId;
    @UtopiaSqlColumn(name = "USER_ID") private Long userId;
    @UtopiaSqlColumn(name = "BUY_TIME") private Date buyTime;

    public static String ck_user_course(Long userId,Long courseId){
        return CacheKeyGenerator.generateCacheKey(
                YiqiJTPuy.class,
                new String[]{"USER_ID","COURSE_ID"},
                new Object[]{userId,courseId});
    }

}
