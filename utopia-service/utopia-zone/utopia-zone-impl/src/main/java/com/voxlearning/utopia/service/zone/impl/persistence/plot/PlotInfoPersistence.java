package com.voxlearning.utopia.service.zone.impl.persistence.plot;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotInfo;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotInfoBo;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author : kai.sun
 * @version : 2018-11-09
 * @description :
 **/

@Repository
@CacheBean(type = PlotInfo.class,cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class PlotInfoPersistence extends StaticMongoShardPersistence<PlotInfo,String> {

    @Override
    protected void calculateCacheDimensions(PlotInfo document, Collection<String> dimensions) {
        dimensions.add(cacheKeyFromId(document.getId()));
        dimensions.add(PlotInfo.ck_regex(document.getId().substring(0,document.getId().lastIndexOf("_")+1)));
        dimensions.add(PlotInfo.ck_plotInfoList());
    }

    /**根据剧情id 获取当前一个组集合*/
    @CacheMethod
    public List<PlotInfo> getPlotInfoListById(@CacheParameter("regex") String regex){
        Pattern pattern = Pattern.compile("^"+regex);
        Criteria criteria = Criteria.where("_id").regex(pattern);
        return query(new Query(criteria)).stream().sorted(Comparator.comparing(PlotInfo::getPlotNum)).collect(Collectors.toList());
    }

    @CacheMethod(key="plotInfoList")
    public List<PlotInfo> getPlotInfoList(){
        return query();
    }

}
