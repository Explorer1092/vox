package com.voxlearning.utopia.service.mizar.impl.dao.microcourse;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriod;

import javax.inject.Named;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 微课堂-课程 DAO
 * Created by Yuechen.Wang on 2016/12/08.
 */
@Named
@CacheBean(type = MicroCoursePeriod.class, useValueWrapper = true)
public class MicroCoursePeriodDao extends StaticCacheDimensionDocumentMongoDao<MicroCoursePeriod, String> {

    // 仅运营使用
    public Page<MicroCoursePeriod> findByName(String theme, Pageable pageable) {
        if (pageable == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        Criteria criteria = Criteria.where("disabled").is(false);
        theme = StringRegexUtils.escapeExprSpecialWord(theme);
        if (StringUtils.isNotBlank(theme)) {
            criteria.and("theme").regex(Pattern.compile(".*" + theme + ".*"));
        }
        Query query = new Query(criteria);
        Sort sort = new Sort(Sort.Direction.DESC, "_id");
        return new PageImpl<>(query(query.with(pageable).with(sort)), pageable, count(query));
    }

    public long disable(Collection<String> periodIds) {
        periodIds = CollectionUtils.toLinkedList(periodIds);
        if (CollectionUtils.isEmpty(periodIds)) {
            return 0;
        }
        Criteria criteria = Criteria.where("_id").in(periodIds);
        Update update = Update.update("disabled", true).set("updateTime", new Date());
        long cnt = updateMany(createMongoConnection(), criteria, update).getModifiedCount();
        if (cnt > 0) {
            Set<String> cacheKeys = new HashSet<>();
            periodIds.forEach(id -> cacheKeys.add(CacheKeyGenerator.generateCacheKey(MicroCoursePeriod.class, id)));
            getCache().delete(cacheKeys);
        }
        return cnt;
    }
}
