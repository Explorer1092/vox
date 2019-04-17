package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.ChipsUserOralTestSchedule;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


@Named
@CacheBean(type = ChipsUserOralTestSchedule.class)
public class ChipsUserOralTestScheduleDao extends AlpsStaticMongoDao<ChipsUserOralTestSchedule, String> {

    @Override
    protected void calculateCacheDimensions(ChipsUserOralTestSchedule document, Collection<String> dimensions) {
        dimensions.add(ChipsUserOralTestSchedule.ck_id(document.getId()));
        dimensions.add(ChipsUserOralTestSchedule.ck_clazzId(document.getClazzId()));
    }

    @CacheMethod
    public List<ChipsUserOralTestSchedule> loadByClazzId(@CacheParameter("CID") Long clazzId) {
        Criteria criteria = Criteria.where("clazzId").is(clazzId);
        return query(Query.query(criteria));
    }

}
