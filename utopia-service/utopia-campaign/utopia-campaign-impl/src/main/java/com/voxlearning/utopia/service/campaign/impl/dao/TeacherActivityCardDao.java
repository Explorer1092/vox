package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityCard;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TeacherActivityCard.class, useValueWrapper = true)
public class TeacherActivityCardDao extends AlpsStaticMongoDao<TeacherActivityCard, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherActivityCard document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @Override
    public TeacherActivityCard load(Long teacherId) {
        TeacherActivityCard load = super.load(teacherId);
        if (load == null) load = TeacherActivityCard.newInstance(teacherId);
        return load;
    }

    public List<TeacherActivityCard> loadCard(Long startId, Integer size) {
        Criteria criteria = Criteria.where("cards.0").exists().and("_id").gt(startId);
        return query(Query.query(criteria).limit(size));
    }
}
