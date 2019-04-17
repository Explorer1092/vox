package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCGrade;
import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2016/1/18.
 */

@Named
public class CrmUGCGradeDao extends StaticMongoDao<CrmUGCGrade, String> {

    @Override
    protected void calculateCacheDimensions(CrmUGCGrade source, Collection<String> dimensions) {

    }

    public Page<CrmUGCGrade> findCrmUGCGradeIs(Integer triggerType, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    public Page<CrmUGCGrade> findCrmUGCGradeIn(List<Integer> triggerTypes, Pageable page) {
        Filter filter = filterBuilder.where("triggerType").in(triggerTypes);
        return __pageFind_OTF(filter.toBsonDocument(), page);

    }

    public Page<CrmUGCGrade> ugcGradeNameAnswerChange(Integer triggerType, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        BsonDocument document = filter.toBsonDocument();
        document.put("$where", new BsonString("this.ugcGradeNames != this.historyUgcGradeName"));
        return __pageFind_OTF(document, pageable);
    }

    public long getUgcGradeCount() {
        return __count_OTF();
    }

    public List<CrmUGCGrade> allUgcGradeData(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }

    public CrmUGCGrade findUGCGradeBySchoolId(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        return __find_OTF(Find.find(filter)).stream().findFirst().orElse(null);
    }


}
