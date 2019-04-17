package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassStatisticsLatest;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


@Named
@CacheBean(type = ChipsEnglishClassStatisticsLatest.class)
public class ChipsEnglishClassStatisticsLatestDao extends AlpsStaticMongoDao<ChipsEnglishClassStatisticsLatest, String> {

    @Override
    protected void calculateCacheDimensions(ChipsEnglishClassStatisticsLatest document, Collection<String> dimensions) {
        dimensions.add(ChipsEnglishClassStatisticsLatest.ck_class_id(document.getClassId()));
    }

    @CacheMethod
    public List<ChipsEnglishClassStatisticsLatest> loadByClassId(@CacheParameter("CID") Long classId) {
        Criteria criteria = Criteria.where("classid").is(classId);
        Sort sort = new Sort(Sort.Direction.ASC, "createtime");
        return query(Query.query(criteria).with(sort));
    }


}
