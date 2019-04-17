package com.voxlearning.utopia.service.afenti.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLoginDetail;

import javax.inject.Named;
import java.util.Collection;

import static com.mongodb.client.model.ReturnDocument.AFTER;

/**
 * @author Ruib
 * @since 2016/8/25
 */
@Named
public class AfentiLoginDetailDao extends AlpsStaticMongoDao<AfentiLoginDetail, String> {

    @Override
    protected void calculateCacheDimensions(AfentiLoginDetail document, Collection<String> dimensions) {
    }

    public AfentiLoginDetail login(Long userId, Subject subject, String... dates) {
        String id = AfentiLoginDetail.generateId(userId, subject);

        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().addToSet("details", dates);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER);
        MongoNamespace namespace = calculateIdMongoNamespace(id);
        MongoConnection connection = createMongoConnection(namespace);

        return executeFindOneAndUpdate(connection, criteria, update, options);
    }

}
