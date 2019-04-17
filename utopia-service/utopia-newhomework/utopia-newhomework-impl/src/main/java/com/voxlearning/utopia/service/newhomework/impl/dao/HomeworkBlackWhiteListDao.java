package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkBlackWhiteList;

import javax.inject.Named;
import java.util.*;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/17
 */
@Named
@CacheBean(type = HomeworkBlackWhiteList.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class HomeworkBlackWhiteListDao extends StaticMongoShardPersistence<HomeworkBlackWhiteList, String> {

    @Override
    protected void calculateCacheDimensions(HomeworkBlackWhiteList document, Collection<String> dimensions) {
        dimensions.add(HomeworkBlackWhiteList.ck_id(document.getId()));
        dimensions.add(HomeworkBlackWhiteList.ck_bi(document.getBusinessType(), document.getIdType()));
    }

    /**
     *
     * @param blackWhiteIds 主键ids
     * @return 是否是黑(白)名单
     */
    public boolean isBlackWhiteList(List<String> blackWhiteIds) {
        Objects.requireNonNull(blackWhiteIds);
        Criteria criteria = Criteria.where("_id").in(blackWhiteIds).and("disabled").is(Boolean.FALSE);
        return count(Query.query(criteria)) > 0;
    }

    /**
     *
     * @param businessType 业务类型
     * @param idType
     * @param blackWhiteId
     * @return
     */
    @CacheMethod
    public PageImpl<HomeworkBlackWhiteList> loadBlackWhiteLists(@CacheParameter(value = "bt") String businessType, @CacheParameter(value = "it") String idType,
                                                                String blackWhiteId, Pageable pageable) {
        Criteria criteria = Criteria.where("businessType").is(businessType).and("idType").is(idType);
        if (StringUtils.isNotBlank(blackWhiteId)) {
            criteria.and("blackWhiteId").is(blackWhiteId);
        }
        criteria.and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        query.skip((pageable.getPageNumber()-1) * pageable.getPageSize()).limit(pageable.getPageSize());
        return new PageImpl<>(query(query), pageable, count(query));
    }

    public boolean updateDisabledTrue(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }

        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("updateAt", new Date());
        update.set("disabled", Boolean.TRUE);
        HomeworkBlackWhiteList modified = $executeFindOneAndUpdate(createMongoConnection(id), criteria, update).getUninterruptibly();
        if (modified != null) {
            getCache().deletes(Arrays.asList(HomeworkBlackWhiteList.ck_id(id),
                    HomeworkBlackWhiteList.ck_bi(modified.getBusinessType(), modified.getIdType())));
        }
        return modified != null;
    }
}
