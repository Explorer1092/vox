package com.voxlearning.utopia.service.vendor.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyData;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.*;
import java.util.regex.Pattern;

import static com.mongodb.client.model.ReturnDocument.BEFORE;

/**
 * Dao of MySelfStudyData
 *
 * @author jiangpeng
 * @since 2016-10-20 下午2:18
 **/
@Named
@UtopiaCacheSupport(MySelfStudyData.class)
public class MySelfStudyDataDao extends AlpsStaticMongoDao<MySelfStudyData, String> {
    @Override
    protected void calculateCacheDimensions(MySelfStudyData document, Collection<String> dimensions) {
        if (document == null)
            return;
        dimensions.add(MySelfStudyData.generateCacheKeyById(document.getId()));
        dimensions.add(MySelfStudyData.generateCacheKeyBySId(document.getStudentId()));
    }

    @CacheMethod
    public MySelfStudyData loadById(@CacheParameter(value = "ID") String id){
        return load(id);
    }


    @CacheMethod(
            type = MySelfStudyData.class
    )
    public List<MySelfStudyData> loadByStudentId(@CacheParameter(value = "SID") Long studentId){
        Pattern pattern = Pattern.compile("^" + studentId + "_");
        Criteria criteria = Criteria.where("_id").regex(pattern);

        Query query = Query.query(criteria);
        return query(query);
    }

    public Boolean insertIfAbsentOrElseUpdate (MySelfStudyData mySelfStudyData, Boolean incStudyDayCount) {
        if (mySelfStudyData == null)
            return false;
        if (mySelfStudyData.getId() == null) {
            insert(mySelfStudyData);
            return true;
        }
        try {
            myUpsert(mySelfStudyData, incStudyDayCount);
            return true;
        }catch (Exception e){
            if (e.getMessage().contains("E11000")) {
                myUpsert(mySelfStudyData, incStudyDayCount);
                return true;
            } else
                throw e;
        }
    }

    private void myUpsert(MySelfStudyData mySelfStudyData, Boolean incStudyDayCount){
        String id = mySelfStudyData.getId();
        Criteria criteria = Criteria.where("_id").is(id);
        Bson filter = criteriaTranslator.translate(criteria);
        Update update = new Update();
        if (StringUtils.isNotBlank(mySelfStudyData.getStudyProgress()))
            update.set("studyProgress", mySelfStudyData.getStudyProgress());
        if (incStudyDayCount)
            update.inc("studyDayCount", 1);
        if (mySelfStudyData.getLastUseDate() != null)
            update.set("lastUseDate", mySelfStudyData.getLastUseDate());
        if (mySelfStudyData.getExpireDate() != null)
            update.set("expireDate", mySelfStudyData.getExpireDate());
        Date now = new Date();
        update.setOnInsert("createDate", now);
        update.set("updateDate", now);
        update.setOnInsert("studentId", mySelfStudyData.getStudentId());
        update.setOnInsert("selfStudyType", mySelfStudyData.getSelfStudyType());

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(BEFORE);
        MongoNamespace namespace = calculateIdMongoNamespace(id);
        MongoConnection connection = createMongoConnection(namespace);
        BsonDocument oneAndUpdate = connection.collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(filter, updateTranslator.translate(update), options);
        MySelfStudyData original = convertBsonDocument(oneAndUpdate);
        Set<String> dimensions = new HashSet<>();
        if (original != null) {
            calculateCacheDimensions(original, dimensions);
        }
        calculateCacheDimensions(mySelfStudyData, dimensions);
        getCache().deletes(dimensions);
    }
}
