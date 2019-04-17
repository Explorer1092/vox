package com.voxlearning.utopia.service.ai.impl.persistence;

import com.mongodb.client.result.UpdateResult;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserExt;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = ChipsEnglishUserExt.class)
public class ChipsEnglishUserExtDao extends AlpsStaticMongoDao<ChipsEnglishUserExt, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsEnglishUserExt document, Collection<String> dimensions) {
        dimensions.add(ChipsEnglishUserExt.ck_id(document.getId()));
    }

}
