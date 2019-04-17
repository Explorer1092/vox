package com.voxlearning.utopia.service.zone.api.entity.plot;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : kai.sun
 * @version : 2018-11-23
 * @description :
 **/

@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_clazz_plot_info_date")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181109")
public class PlotInfoDate implements Serializable {

    private static final long serialVersionUID = 6906866737256359891L;

    @DocumentId
    private String id;

    @DocumentField("open_date")
    private Date openDate;

    @DocumentField("end_date")
    private Date endDate;

    public Integer getActivityId(){
        return SafeConverter.toInt(id.split("_")[0]);
    }

    public Integer getPlotGroup(){
        return SafeConverter.toInt(id.split("_")[1]);
    }

    public static String generatorId(Integer activityId,Integer plotGroup){
        if(activityId==null||plotGroup==null) return null;
        return  activityId+"_"+plotGroup;
    }

    //缓存key
    public static String ck_list(Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(PlotInfoDate.class,new String[]{"activityId"},new Object[]{activityId});
    }


}
