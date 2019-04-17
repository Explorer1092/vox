package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCStudentOfClass;
import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2016/1/25.
 */

@Named
public class CrmUGCStudentCountDao extends StaticMongoDao<CrmUGCStudentOfClass,String> {
    @Override
    protected void calculateCacheDimensions(CrmUGCStudentOfClass source, Collection<String> dimensions) {

    }

    public Page<CrmUGCStudentOfClass> findCrmUgcStudentCountIs(Integer triggerType, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    public Page<CrmUGCStudentOfClass> findCrmUgcStudentCountIn(List<Integer> triggerTypes, Pageable page) {
        Filter filter = filterBuilder.where("triggerType").in(triggerTypes);
        return __pageFind_OTF(filter.toBsonDocument(), page);

    }

    public Page<CrmUGCStudentOfClass> findCrmUgcStudentCountAnswerChange(Integer triggerType, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        BsonDocument document = filter.toBsonDocument();
        document.put("$where", new BsonString("this.ugcStudentCount != this.historyUgcStudentCount"));
        return __pageFind_OTF(document, pageable);

    }

    public CrmUGCStudentOfClass findUgcStudentCountByClazzId(Long schoolId,Long clazzId){

        Filter filter = filterBuilder.where("schoolId").is(schoolId).and("clazzId").is(clazzId);
        return __find_OTF(Find.find(filter)).stream().findFirst().orElse(null);
    }

    public long getCrmUgcStudentsCount() {
        return __count_OTF();
    }

    public List<CrmUGCStudentOfClass> allCrmUgcStudentCountData(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }
}
