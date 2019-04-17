package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.dao.mongo.support.MongoExceptionUtils;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRecord;

import javax.inject.Named;
import java.util.*;

import static com.mongodb.client.model.ReturnDocument.AFTER;

/**
 * 磨耳朵记录dao
 *
 * @author jiangpeng
 * @since 2016-10-25 下午9:45
 **/
@Named
@UtopiaCacheSupport(StudentGrindEarRecord.class)
public class StudentGrindEarRecordDao extends AsyncStaticMongoPersistence<StudentGrindEarRecord, Long> {
    @Override
    protected void calculateCacheDimensions(StudentGrindEarRecord document, Collection<String> dimensions) {
        dimensions.add(StudentGrindEarRecord.ck_id(document.getId()));
    }


    public StudentGrindEarRecord pushRecord(Long studentId, Date date){
        try {
            return innerPushRecord(studentId, date);
        }catch (Exception e){
            if (MongoExceptionUtils.isDuplicateKeyError(e))
                return innerPushRecord(studentId, date);
            else
                throw e;
        }
    }


    private StudentGrindEarRecord innerPushRecord(Long studentId, Date date){

        Criteria criteria = Criteria.where("_id").is(studentId);
        Update update = new Update();
        Date dateArray[] = new Date[1];
        dateArray[0] = date;
        update.push("dateList", dateArray);
        Date now = new Date();
        update.setOnInsert("createDate", now);
        update.set("updateDate", now);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER);

        StudentGrindEarRecord after = $executeFindOneAndUpdate(createMongoConnection(), criteria, update, options).getUninterruptibly();
        if (after != null) {
            Set<String> dimensions = new HashSet<>();
            calculateCacheDimensions(after, dimensions);
            getCache().deletes(dimensions);
        }
        return after;

    }

    public List<StudentGrindEarRecord> loadAll(){
        Criteria criteria = Criteria.where("_id").exists();
        return query(Query.query(criteria));
    }

}
