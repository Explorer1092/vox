package com.voxlearning.utopia.service.zone.impl.persistence.plot;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotInfoDate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author : kai.sun
 * @version : 2018-11-23
 * @description :
 **/

@Repository
@CacheBean(type = PlotInfoDate.class,cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class PlotInfoDatePersistence extends StaticMongoShardPersistence<PlotInfoDate,String> {

    @Override
    protected void calculateCacheDimensions(PlotInfoDate document, Collection<String> dimensions) {
        dimensions.add(cacheKeyFromId(document.getId()));
        String[] ids = document.getId().split("_");
        dimensions.add(PlotInfoDate.ck_list(SafeConverter.toInt(ids[0])));
    }

    @CacheMethod
    public List<PlotInfoDate> getPlotInfoDateList(@CacheParameter("activityId")Integer activityId){
        Pattern pattern = Pattern.compile("^"+activityId+"_");
        Criteria criteria = Criteria.where("_id").regex(pattern);
        return query(new Query(criteria)).stream().sorted(Comparator.comparing(PlotInfoDate::getPlotGroup)).collect(Collectors.toList());
    }


}
