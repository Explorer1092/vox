package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.constant.PageViewType;
import com.voxlearning.utopia.service.ai.entity.ChipsKeywordPrototype;
import com.voxlearning.utopia.service.ai.entity.ChipsUserPageViewLog;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/3/19
 */
@Named
@CacheBean(type = ChipsKeywordPrototype.class)
public class ChipsKeywordPrototypeDao extends AsyncStaticMongoPersistence<ChipsKeywordPrototype, String> {

    @Override
    protected void calculateCacheDimensions(ChipsKeywordPrototype chipsKeywordPrototype, Collection<String> collection) {

    }
}