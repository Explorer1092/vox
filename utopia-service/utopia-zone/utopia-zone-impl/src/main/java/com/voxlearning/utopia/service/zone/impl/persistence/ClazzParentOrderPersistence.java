package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.hash.MongoShardCalculator;
import com.voxlearning.alps.dao.mongo.hash.MongoShardCalculatorFactory;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.zone.api.entity.ClazzParentOrderRecord;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Objects;

/**
 * @author : chensn
 * @version : 2018-12-24
 * @description :
 **/

@Repository
@CacheBean(type = ClazzParentOrderRecord.class, cacheName = "columb-zone-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class ClazzParentOrderPersistence extends DynamicMongoShardPersistence<ClazzParentOrderRecord,String> {

    private static final String ID_SEP = "_";

    @Override
    protected void calculateCacheDimensions(ClazzParentOrderRecord document, Collection<String> dimensions) {
        dimensions.add(cacheKeyFromId(document.getId()));
    }

    @Override
    protected MongoShardCalculator getShardCalculator(int shardSize) {
        return new MongoShardCalculator() {
            @Override
            public int calculate(Object id) {
                String[] idParts = StringUtils.split(SafeConverter.toString(id), "_");
                if(idParts.length==0){
                    return MongoShardCalculatorFactory.getInstance().getCalculator(shardSize).calculate(id);
                }else{
                    return MongoShardCalculatorFactory.getInstance().getCalculator(shardSize).calculate(idParts[0]);
                }
            }

            @Override
            public void close() {
            }
        };
    }


    @Override
    protected String calculateDatabase(String template, ClazzParentOrderRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, ClazzParentOrderRecord document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = document.getId().split(ID_SEP);
        if (RuntimeMode.le(Mode.TEST)) {
            return StringUtils.formatMessage(template, SafeConverter.toLong(ids[0]) % 2);
        } else {
            return StringUtils.formatMessage(template, SafeConverter.toLong(ids[0]) % 10);
        }
    }
}
