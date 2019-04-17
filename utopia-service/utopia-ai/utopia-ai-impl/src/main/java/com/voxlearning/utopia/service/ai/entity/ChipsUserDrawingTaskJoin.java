package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntityWithDisabledField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@NoArgsConstructor
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_CHIPS_USER_DRAWING_TASK_JOIN")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190318")
public class ChipsUserDrawingTaskJoin extends LongIdEntityWithDisabledField {
    private static final long serialVersionUID = -950058194329641828L;
    @UtopiaSqlColumn(name = "TASK_ID") private Long taskId;
    @UtopiaSqlColumn(name = "JOINER") private Long joiner;
    @UtopiaSqlColumn(name = "USER_ANSWER") private String userAnswer;
    @UtopiaSqlColumn(name = "MASTER") private Boolean master;
    @UtopiaSqlColumn(name = "ENERGY") private Integer energy;

    //缓存key
    public static String ck_task(Long taskId) {
        return CacheKeyGenerator.generateCacheKey(ChipsUserDrawingTaskJoin.class, new String[]{"T"},
                new Object[]{taskId});
    }

    //缓存key
    public static String ck_joiner(Long joiner) {
        return CacheKeyGenerator.generateCacheKey(ChipsUserDrawingTaskJoin.class, new String[]{"J"},
                new Object[]{joiner});
    }


    public ChipsUserDrawingTaskJoin(Long joinUser, Long taskId, String userAnwer, boolean master) {
        this.setEnergy(master ? 1 : 0);
        this.setJoiner(joinUser);
        this.setTaskId(taskId);
        this.setMaster(master);
        this.setUserAnswer(userAnwer);
        this.setDisabled(false);
        this.setCreateTime(new Date());
        this.setUpdateTime(new Date());
    }
}
