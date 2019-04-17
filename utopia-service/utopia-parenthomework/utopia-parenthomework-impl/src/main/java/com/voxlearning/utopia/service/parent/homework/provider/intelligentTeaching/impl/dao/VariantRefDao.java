package com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.entity.Variant;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.entity.VariantRef;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 讲练测dao
 *
 * @author Wenlong Meng
 * @since Feb 12, 2019
 */
@Named
public class VariantRefDao extends AlpsStaticMongoDao<VariantRef, String> {

    //Local variables
    /**
     * 可用教材列表缓存
     */
    LoadingCache<Integer, List<String>> bookIdsCache = CacheBuilder.newBuilder().maximumSize(100).refreshAfterWrite(17, TimeUnit.HOURS).build(
            new CacheLoader<Integer, List<String>>() {
                @Override
                public List<String> load(Integer subjectId) {
                    return $loadBookIds(subjectId);
                }
            });

    /**
     * 课时-变式关系缓存
     */
    LoadingCache<String, List<String>> setion2VariantRefCache = CacheBuilder.newBuilder().maximumSize(100).refreshAfterWrite(17, TimeUnit.HOURS).build(
            new CacheLoader<String, List<String>>() {
                @Override
                public List<String> load(String sectionId) {
                    return $loadVariantIds(sectionId);
                }
            });

    @Override
    protected void calculateCacheDimensions(VariantRef course, Collection<String> collection) {
    }

    /**
     * 获取可用教材id列表
     *
     * @param subjectId 学科id
     * @return
     */
    public List<String> loadBookIds(Integer subjectId) {
        return bookIdsCache.getUnchecked(subjectId);
    }


    /**
     * 根据课时id查询变式id
     *
     * @param sectionId 课时id
     * @return
     */
    public List<String> loadVariantIds(String sectionId) {
        return setion2VariantRefCache.getUnchecked(sectionId);
    }

    /**
     * 获取可用教材id列表
     *s
     * @return
     */
    private List<String> $loadBookIds(Integer subjectId) {
        Criteria criteria = Criteria.where("subject_id").is(subjectId);
        Query query = Query.query(criteria).limit(1000);
        Set<String> result = Sets.newHashSet();
        List<VariantRef> variantRefs = query(query);
        for (VariantRef variantRef : variantRefs) {
            result.add(variantRef.getBookId());
        }
        return new ArrayList<>(result);
    }

    /**
     * 根据课时id获取变式id
     *s
     * @return
     */
    private List<String> $loadVariantIds(String sectionId) {
        Criteria criteria = Criteria.where("book_catalog_id").is(sectionId);
        Query query = Query.query(criteria).limit(100);
        Set<String> result = Sets.newHashSet();
        List<VariantRef> variantRefs = query(query);
        for (VariantRef variantRef : variantRefs) {
            result.addAll(variantRef.getVariants().stream().map(Variant::getId).collect(Collectors.toList()));
        }
        return new ArrayList<>(result);
    }

}
