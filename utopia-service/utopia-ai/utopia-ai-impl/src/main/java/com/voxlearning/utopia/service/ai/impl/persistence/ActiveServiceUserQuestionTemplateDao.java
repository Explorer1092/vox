package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceUserQuestionTemplate;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guangqing
 * @since 2018/11/7
 */
@Named
@CacheBean(type = ActiveServiceUserQuestionTemplate.class)
public class ActiveServiceUserQuestionTemplateDao extends AsyncStaticMongoPersistence<ActiveServiceUserQuestionTemplate, String> {

    @Override
    protected void calculateCacheDimensions(ActiveServiceUserQuestionTemplate template, Collection<String> collection) {

    }

}
