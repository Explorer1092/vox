package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassExt;

import javax.inject.Named;
import java.util.Collection;


@Named
@CacheBean(type = ChipsEnglishClassExt.class)
public class ChipsEnglishClassExtDao extends AlpsStaticMongoDao<ChipsEnglishClassExt, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsEnglishClassExt document, Collection<String> dimensions) {
        dimensions.add(ChipsEnglishClassExt.ck_id(document.getId()));
    }
}
