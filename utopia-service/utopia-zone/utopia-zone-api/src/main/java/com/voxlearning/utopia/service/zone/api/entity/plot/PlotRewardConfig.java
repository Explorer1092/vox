package com.voxlearning.utopia.service.zone.api.entity.plot;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 奖励配置基础表
 * @author dongfeng.xue
 * @date 2018-11
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_clazz_plot_reward_config")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20181112")
public class PlotRewardConfig implements Serializable {
    private static final long serialVersionUID = -719121872149632677L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;  //规则 activityId_type

    @DocumentField("activity_id")
    private Integer activityId;

    //剧情活动  1:玫瑰    2：星星   3： 666  4 爱心
    //火鸡活动  1：烤箱      2：托盘       3： 火鸡
    @DocumentField("type")
    private Integer type;

    @DocumentField("name")
    private String name; //奖励名称

    @DocumentField("pic")
    private String pic; //图片

    public void generateId() {
        id = activityId + "_" + type;
    }

    public static String generateId(Integer activityId, Integer type) {
        return activityId + "_" + type;
    }

    public static String cacheKeyFromActivityId(Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(PlotRewardConfig.class, new String[]{"activityId"}, new Object[]{activityId});
    }

}
