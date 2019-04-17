package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceUserTemplate;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guangqing
 * @since 2019/1/7
 */
@Named
@CacheBean(type = ActiveServiceUserTemplate.class)
public class ActiveServiceUserTemplateDao extends AsyncStaticMongoPersistence<ActiveServiceUserTemplate, String> {
    @Override
    protected void calculateCacheDimensions(ActiveServiceUserTemplate activeServiceUserTemplate, Collection<String> collection) {

    }
}
