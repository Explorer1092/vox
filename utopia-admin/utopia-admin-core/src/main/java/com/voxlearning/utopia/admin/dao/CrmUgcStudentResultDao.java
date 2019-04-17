package com.voxlearning.utopia.admin.dao;

import com.mongodb.WriteConcern;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.CrmUgcStudentResult;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by jiang wei on 2016/7/26.
 */
@Named
public class CrmUgcStudentResultDao extends AlpsStaticMongoDao<CrmUgcStudentResult, String> {


    @Override
    protected void calculateCacheDimensions(CrmUgcStudentResult source, Collection<String> dimensions) {

    }


    public List<CrmUgcStudentResult> findResults(Integer releId) {
//        Filter filter = filterBuilder.where("rele_Id").is(releId).and("status").is("1");
//        return __find_OTF(filter.toBsonDocument());
        Criteria criteriaReleId=Criteria.where("rele_id").is(releId);
        Criteria criteriaStatus=Criteria.where("status").is(1);
        Criteria andCriteria=Criteria.and(criteriaReleId,criteriaStatus);

        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "dt"));
        Query query=Query.query(andCriteria).with(sort);
        return query(query);

    }

    public List<CrmUgcStudentResult> findResultBySidAndRid(Long studentId,Integer releId){
        Criteria criteriaReleId=Criteria.where("rele_id").is(releId);
        Criteria criteriaStudentId=Criteria.where("student_id").is(studentId);
        Criteria criteriaStatus=Criteria.where("status").is(1);
        Criteria andCriteria=Criteria.and(criteriaStudentId,criteriaReleId,criteriaStatus);
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "dt"));
        Query query=Query.query(andCriteria).with(sort);
        return query(query);
    }

    public CrmUgcStudentResult findResultById(String id) {

        Criteria criteriaId=Criteria.where("_id").is(id);
        Criteria andCriteria=Criteria.and(criteriaId);

        Query query=Query.query(andCriteria);
        return query(query).stream().findFirst().orElse(null);
    }


    public BsonDocument updateUgcStudentResult(CrmUgcStudentResult crmUgcStudentResult){
        Criteria criteria=Criteria.where("_id").is(crmUgcStudentResult.getId());
        Bson filter= criteriaTranslator.translate(criteria);
        Update update=new Update();

        update.set("rele_id",crmUgcStudentResult.getReleId());
        update.set("status",crmUgcStudentResult.getStatus());


        BsonDocument updateResult = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED).findOneAndUpdate(filter, updateTranslator.translate(update));
        return updateResult;
    }


}
