package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsEncourageVideo;
import com.voxlearning.utopia.service.ai.entity.ChipsKeywordVideo;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guangqing
 * @since 2019/3/19
 */
@Named
@CacheBean(type = ChipsEncourageVideo.class)
public class ChipsEncourageVideoDao extends AsyncStaticMongoPersistence<ChipsEncourageVideo, String> {

    @Override
    protected void calculateCacheDimensions(ChipsEncourageVideo chipsEncourageVideo, Collection<String> collection) {

    }
}