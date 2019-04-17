package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.constant.PageViewType;
import com.voxlearning.utopia.service.ai.entity.ChipsUserPageViewLog;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/3/19
 */
@Named
@CacheBean(type = ChipsUserPageViewLog.class)
public class ChipsUserPageViewLogDao extends AsyncStaticMongoPersistence<ChipsUserPageViewLog, String> {

    @Override
    protected void calculateCacheDimensions(ChipsUserPageViewLog chipsUserPageViewLog, Collection<String> collection) {

    }

    public List<ChipsUserPageViewLog> loadByUniqueKeyAndType(PageViewType type, String uniqueKey) {
        Criteria criteria = Criteria.where("type").is(type).and("uniqueKey").is(uniqueKey).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public List<ChipsUserPageViewLog> loadByTypeAndUsers(PageViewType type, Collection<Long> userCol) {
        Criteria criteria = Criteria.where("type").is(type).and("userId").in(userCol).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }
}