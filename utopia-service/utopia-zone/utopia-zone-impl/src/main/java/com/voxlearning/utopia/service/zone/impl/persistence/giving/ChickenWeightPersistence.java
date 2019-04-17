package com.voxlearning.utopia.service.zone.impl.persistence.giving;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.connection.IMongoConnection;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.zone.api.entity.giving.ClassCircleChickenWeight;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author yulong.ma
 * @date 2018-11-16
 */
@Repository
@CacheBean(type = ClassCircleChickenWeight.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class ChickenWeightPersistence extends StaticMongoShardPersistence<ClassCircleChickenWeight, String> {
    private static final String ID_SEP = "_";

    protected List<IMongoConnection> calculateMongoConnection(Long activityId) {
        String mockId = activityId + "_9999l_00000l_0_1_1";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

    @Override
    protected void calculateCacheDimensions(ClassCircleChickenWeight document,
        Collection <String> dimensions) {

    }
}
