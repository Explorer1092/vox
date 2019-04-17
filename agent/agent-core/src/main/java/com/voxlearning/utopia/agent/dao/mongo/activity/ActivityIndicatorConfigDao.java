package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityIndicatorConfig;

import javax.inject.Named;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Named
@CacheBean(type =  ActivityIndicatorConfig.class)
public class ActivityIndicatorConfigDao extends StaticCacheDimensionDocumentMongoDao<ActivityIndicatorConfig, String> {

    @CacheMethod
    public List<ActivityIndicatorConfig> loadByAid(@CacheParameter("aid") String activityId){
        if(StringUtils.isBlank(activityId)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("disabled").is(false);
        List<ActivityIndicatorConfig> dataList = query(Query.query(criteria));
        if(CollectionUtils.isNotEmpty(dataList)){
            dataList.sort(Comparator.comparingInt(o -> SafeConverter.toInt(o.getSortNo())));
        }
        return dataList;
    }

}
