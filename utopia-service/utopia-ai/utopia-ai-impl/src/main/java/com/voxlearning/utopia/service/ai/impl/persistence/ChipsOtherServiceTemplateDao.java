package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsOtherServiceTemplate;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guangqing
 * @since 2019/1/14
 */
@Named
@CacheBean(type = ChipsOtherServiceTemplate.class)
public class ChipsOtherServiceTemplateDao extends AsyncStaticMongoPersistence<ChipsOtherServiceTemplate, String> {

    @Override
    protected void calculateCacheDimensions(ChipsOtherServiceTemplate chipsOtherServiceTemplate, Collection<String> collection) {

    }
}
