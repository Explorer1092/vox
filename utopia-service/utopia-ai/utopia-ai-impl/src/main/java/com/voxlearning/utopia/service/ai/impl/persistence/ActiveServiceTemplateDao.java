package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceTemplate;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guangqing
 * @since 2019/1/7
 */
@Named
@CacheBean(type = ActiveServiceTemplate.class)
public class ActiveServiceTemplateDao extends AsyncStaticMongoPersistence<ActiveServiceTemplate, String>{

    @Override
    protected void calculateCacheDimensions(ActiveServiceTemplate template, Collection<String> collection) {

    }
}
