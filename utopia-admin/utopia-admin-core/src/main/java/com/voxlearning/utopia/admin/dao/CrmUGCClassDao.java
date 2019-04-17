package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCClass;
import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2016/1/21.
 */

@Named
public class CrmUGCClassDao extends StaticMongoDao<CrmUGCClass, String> {

    @Override
    protected void calculateCacheDimensions(CrmUGCClass source, Collection<String> dimensions) {

    }

    public Page<CrmUGCClass> findCrmUgcClassIs(Integer triggerType, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    public Page<CrmUGCClass> findCrmUgcClassIn(List<Integer> triggerTypes, Pageable page) {
        Filter filter = filterBuilder.where("triggerType").in(triggerTypes);
        return __pageFind_OTF(filter.toBsonDocument(), page);

    }

    public Page<CrmUGCClass> findCrmUgcClassAnswerChange(Integer triggerType, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        BsonDocument document = filter.toBsonDocument();
        document.put("$where", new BsonString("this.ugcClassName != this.historyUgcClassName"));
        return __pageFind_OTF(document, pageable);

    }

    public CrmUGCClass findUgcClassBySchoolIdAndGroupId(Long schoolId, Long groupId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId).and("groupId").is(groupId);
        return __find_OTF(Find.find(filter)).stream().findFirst().orElse(null);
    }


    public long getCrmUgcClassCount() {
        return __count_OTF();
    }

    public List<CrmUGCClass> allCrmUgcClassData(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }

}
