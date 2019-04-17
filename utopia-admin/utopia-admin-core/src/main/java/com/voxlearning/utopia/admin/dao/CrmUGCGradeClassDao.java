package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCGradeClass;
import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2016/1/27.
 */

@Named
public class CrmUGCGradeClassDao extends StaticMongoDao<CrmUGCGradeClass,String> {
    @Override
    protected void calculateCacheDimensions(CrmUGCGradeClass source, Collection<String> dimensions) {

    }

    public Page<CrmUGCGradeClass> findCrmUGCGradeClassIs(Integer triggerType, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    public Page<CrmUGCGradeClass> findCrmUGCGradeClassIn(List<Integer> triggerTypes, Pageable page) {
        Filter filter = filterBuilder.where("triggerType").in(triggerTypes);
        return __pageFind_OTF(filter.toBsonDocument(), page);

    }

    public Page<CrmUGCGradeClass> ugcGradeClassNameAnswerChange(Integer triggerType, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        BsonDocument document = filter.toBsonDocument();
        document.put("$where", new BsonString("this.ugcClassName != this.historyUgcClassName"));
        return __pageFind_OTF(document, pageable);
    }

    public long getUgcGradeClassCount() {
        return __count_OTF();
    }

    public List<CrmUGCGradeClass> allUgcGradeClassData(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }

    public CrmUGCGradeClass findCrmUGCGradeClassBySchoolIdAndGrade(Long schoolId,String grade){
        Filter filter = filterBuilder.where("schoolId").is(schoolId).and("grade").is(grade);
        return __find_OTF(Find.find(filter)).stream().findFirst().orElse(null);
    }

}
