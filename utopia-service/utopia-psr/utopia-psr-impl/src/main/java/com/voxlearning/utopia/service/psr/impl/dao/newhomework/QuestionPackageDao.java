package com.voxlearning.utopia.service.psr.impl.dao.newhomework;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/27
 * Time: 11:24
 * To change this template use File | Settings | File Templates.
 */

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.psr.entity.newhomework.QuestionPackage;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@UtopiaCacheSupport(QuestionPackage.class)
public class QuestionPackageDao extends AlpsStaticMongoDao<QuestionPackage,String> {
    @Override
    protected void calculateCacheDimensions(QuestionPackage source, Collection<String> dimensions){
    }

    @UtopiaCacheable
    public Map<String, List<QuestionPackage>> getPackagesBySectionIds(List<String> sectionIds){
        if (CollectionUtils.isEmpty(sectionIds)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("section_id").in(sectionIds).and("deleted_at").exists(false);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(QuestionPackage::getSectionId));
    }

    @UtopiaCacheable
    public Map<String, List<QuestionPackage>> getPackagesByUnitIds(List<String> unitIds){
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("unit_id").in(unitIds).and("deleted_at").exists(false);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(QuestionPackage::getUnitId));
    }
}
