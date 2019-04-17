package com.voxlearning.utopia.service.psr.impl.dao.newhomework;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.psr.entity.newhomework.QuestionClusterProfile;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/22
 * Time: 18:56
 * To change this template use File | Settings | File Templates.
 */

@Named
@UtopiaCacheSupport(QuestionClusterProfile.class)
public class QuestionClusterProfileDao extends AlpsStaticMongoDao<QuestionClusterProfile, String> {
    @Override
    protected void calculateCacheDimensions(QuestionClusterProfile source, Collection<String> dimensions) {
    }

    public Map<String,QuestionClusterProfile> getClusterProfilesBySectionIds(List<String> sectionIds) {
        if (CollectionUtils.isEmpty(sectionIds)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("book_catalog_id").in(sectionIds).and("deleted_at").exists(false);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(QuestionClusterProfile::getBookCatalogId, Function.identity()));
    }

    public QuestionClusterProfile getClusterProfileBySectionId(String sectionId) {
        Map<String,QuestionClusterProfile> questionClusterProfileMap = getClusterProfilesBySectionIds(Collections.singletonList(sectionId));
        if (questionClusterProfileMap.isEmpty() || !questionClusterProfileMap.containsKey(sectionId)){
            return null;
        } else {
            return questionClusterProfileMap.get(sectionId);
        }
    }
}
