package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsKeywordVideo;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/3/19
 */
@Named
@CacheBean(type = ChipsKeywordVideo.class)
public class ChipsKeywordVideoDao extends AsyncStaticMongoPersistence<ChipsKeywordVideo, String> {

    @Override
    protected void calculateCacheDimensions(ChipsKeywordVideo chipsKeywordVideo, Collection<String> collection) {

    }

    public List<ChipsKeywordVideo> loadAll() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }
}