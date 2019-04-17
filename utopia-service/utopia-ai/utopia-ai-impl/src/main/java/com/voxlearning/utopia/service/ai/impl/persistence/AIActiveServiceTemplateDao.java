package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.AIActiveServiceTemplate;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserExtSplit;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2018/10/30
 */
@Named
@CacheBean(type = AIActiveServiceTemplate.class)
public class AIActiveServiceTemplateDao  extends AsyncStaticMongoPersistence<AIActiveServiceTemplate, String> {
    @Override
    protected void calculateCacheDimensions(AIActiveServiceTemplate aiActiveServiceTemplate, Collection<String> collection) {
        collection.add(AIActiveServiceTemplate.ck_bookId(aiActiveServiceTemplate.getBookId()));
        collection.add(AIActiveServiceTemplate.ck_unitId(aiActiveServiceTemplate.getUnitId()));
        collection.add(AIActiveServiceTemplate.ck_bookId_unitId(aiActiveServiceTemplate.getBookId(),aiActiveServiceTemplate.getUnitId()));
    }
    @CacheMethod
    public List<AIActiveServiceTemplate> loadByBookId(String bookId) {

        Criteria criteria = Criteria.where("bookId").is(bookId);
        return query(Query.query(criteria));
    }
    @CacheMethod
    public List<AIActiveServiceTemplate> loadByUnitId(String unitId) {

        Criteria criteria = Criteria.where("unitId").is(unitId);
        Sort sort = new Sort(Sort.Direction.DESC, "_id");
        return query(Query.query(criteria).with(sort));
    }
    @CacheMethod
    public List<AIActiveServiceTemplate> loadByBookIdAndUnitId(String bookId, String unitId) {

        Criteria criteria = Criteria.where("unitId").is(unitId);
        Sort sort = new Sort(Sort.Direction.DESC, "_id");
        List<AIActiveServiceTemplate> list = query(Query.query(criteria).with(sort));
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        return list.stream().filter(t -> t.getBookId().equals(bookId)).collect(Collectors.toList());
    }

    private void cleanCache(AIActiveServiceTemplate template) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(AIActiveServiceTemplate.ck_bookId(template.getBookId()));
        cacheIds.add(AIActiveServiceTemplate.ck_unitId(template.getUnitId()));
        cacheIds.add(AIActiveServiceTemplate.ck_bookId_unitId(template.getBookId(), template.getUnitId()));
        getCache().deletes(cacheIds);
    }
}
