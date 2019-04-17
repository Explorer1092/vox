package com.voxlearning.utopia.service.zone.api.entity.plot;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author : kai.sun
 * @version : 2018-11-26
 * @description : 用户选择记录
 **/
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_plot_activity_select_record_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181126")
public class PlotActivitySelectRecord implements Serializable {

    private static final long serialVersionUID = 7426922354504494479L;
    /**activityId + userId +plotGroup*/
    @DocumentId
    private String id;

    private Integer common;

    private Boolean condition;

    public static String generatorId(Integer activityId,Long userId,Integer plotGroup){
        if(activityId==null||userId==null||plotGroup==null) return null;
        return activityId+"_"+userId+"_"+plotGroup;
    }

    public Integer getActivityId(){
        return SafeConverter.toInt(id.split("_")[0]);
    }

    public Long getUserId(){
        return SafeConverter.toLong(id.split("_")[1]);
    }

    public Integer getPlotGroup(){
        return SafeConverter.toInt(id.split("_")[2]);
    }

    public static String cacheKeyFromActivityIdUserId(Integer activityId,Long userId) {
        return CacheKeyGenerator.generateCacheKey(PlotActivitySelectRecord.class, new String[]{"activityId","userId"}, new Object[]{activityId,userId});
    }

}
