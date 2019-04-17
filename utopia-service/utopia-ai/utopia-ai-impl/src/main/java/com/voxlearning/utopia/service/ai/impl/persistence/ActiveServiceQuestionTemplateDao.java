package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceQuestionTemplate;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guangqing
 * @since 2018/11/7
 */
@Named
@CacheBean(type = ActiveServiceQuestionTemplate.class)
public class ActiveServiceQuestionTemplateDao extends AsyncStaticMongoPersistence<ActiveServiceQuestionTemplate, String> {

    @Override
    protected void calculateCacheDimensions(ActiveServiceQuestionTemplate activeServiceQuestionTemplate, Collection<String> collection) {

    }

}
