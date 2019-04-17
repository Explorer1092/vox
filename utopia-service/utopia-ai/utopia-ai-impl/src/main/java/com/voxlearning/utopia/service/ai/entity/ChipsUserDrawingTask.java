package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntityWithDisabledField;
import com.voxlearning.utopia.service.ai.constant.ChipsUserDrawingTaskStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 图鉴任务
 */
@Data
@NoArgsConstructor
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_CHIPS_USER_DRAWING_TASK")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190318")
public class ChipsUserDrawingTask extends LongIdEntityWithDisabledField {
    private static final long serialVersionUID = -950058194329641828L;

    @UtopiaSqlColumn(name = "USER_ID") private Long userId;
    @UtopiaSqlColumn(name = "DRAWING_ID") private String drawingId;
    @UtopiaSqlColumn(name = "UNIT_ID") private String unitId;
    @UtopiaSqlColumn(name = "BOOK_ID") private String bookId;
    @UtopiaSqlColumn(name = "COVER") private String cover;
    @UtopiaSqlColumn(name = "VIDEO_ID") private String videoId;
    @UtopiaSqlColumn(name = "STATUS") private String status;
    @UtopiaSqlColumn(name = "TOTAL_ENERGY") private Integer totalEnergy;
    @UtopiaSqlColumn(name = "SHARE") private Boolean share;


    public ChipsUserDrawingTaskStatus fetchStatus() {
       return ChipsUserDrawingTaskStatus.safe(this.getStatus());
    }

    public ChipsUserDrawingTask(Long userId, String bookId, String unitId, String drawingId) {
        this.setBookId(bookId);
        this.setUserId(userId);
        this.setUnitId(unitId);
        this.setDrawingId(drawingId);
        this.setTotalEnergy(5);
        this.setShare(false);
        this.setStatus(ChipsUserDrawingTaskStatus.underway.name());
        this.setDisabled(false);
        this.setCreateTime(new Date());
        this.setUpdateTime(new Date());
    }

    //缓存key
    public static String ck_user(Long userId) {
        return CacheKeyGenerator.generateCacheKey(ChipsUserDrawingTask.class, new String[]{"U"},
                new Object[]{userId});
    }


    //缓存key
    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ChipsUserDrawingTask.class, id);
    }

}
