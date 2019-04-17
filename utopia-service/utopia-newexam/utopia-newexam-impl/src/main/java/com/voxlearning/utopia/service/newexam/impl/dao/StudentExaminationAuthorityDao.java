package com.voxlearning.utopia.service.newexam.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.DynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.utopia.service.newexam.api.entity.StudentExaminationAuthority;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

@Named
@UtopiaCacheSupport(StudentExaminationAuthority.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class StudentExaminationAuthorityDao extends DynamicMongoDao<StudentExaminationAuthority, String> {
    @Override
    protected String calculateDatabase(String template, StudentExaminationAuthority entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, StudentExaminationAuthority entity) {
        StudentExaminationAuthority.ID id = entity.parseID();
        return StringUtils.formatMessage(template, id.getMonth());
    }

    @Override
    protected void calculateCacheDimensions(StudentExaminationAuthority source, Collection<String> dimensions) {
        dimensions.add(StudentExaminationAuthority.ck_id(source.getId()));
    }

    public boolean updateStudentExaminationAuthorityDisabled(String id, Boolean disabled) {
        StudentExaminationAuthority studentExaminationAuthority = load(id);
        if (studentExaminationAuthority == null) {
            return false;
        }
        if (Objects.equals(studentExaminationAuthority.getDisabled(), disabled)) {
            return true;
        }
        Filter filter = filterBuilder.where("_id").is(id);
        Find find = Find.find(filter);
        Date currentDate = new Date();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection mongoLocation = createMongoConnection(namespace);
        Update update = updateBuilder.build();
        update.set("updateAt", currentDate);
        update.set("disabled", disabled);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);
        BsonDocument document = mongoLocation.collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(find.filter(), update.toBsonDocument(), options);
        StudentExaminationAuthority modified = transform(document);
        if (modified != null) {
            getCache().getCacheObjectModifier().modify(StudentExaminationAuthority.ck_id(id),
                    entityCacheExpirationInSeconds(), currentValue -> modified);
        }
        return modified != null;
    }
}
