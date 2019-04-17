package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/9/10
 */
@Named
@CacheBean(type = ChipsEnglishPageContentConfig.class)
public class ChipsEnglishPageContentConfigDao extends AlpsStaticMongoDao<ChipsEnglishPageContentConfig, String> {
    @Override
    protected void calculateCacheDimensions(ChipsEnglishPageContentConfig document, Collection<String> dimensions) {
    }
}
