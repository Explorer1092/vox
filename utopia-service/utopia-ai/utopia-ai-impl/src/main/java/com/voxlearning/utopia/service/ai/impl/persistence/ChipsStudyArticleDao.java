package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsStudyArticle;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guangqing
 * @since 2019/1/7
 */
@Named
@CacheBean(type = ChipsStudyArticle.class)
public class ChipsStudyArticleDao extends AsyncStaticMongoPersistence<ChipsStudyArticle, String> {

    @Override
    protected void calculateCacheDimensions(ChipsStudyArticle chipsStudyArticle, Collection<String> collection) {

    }
}
