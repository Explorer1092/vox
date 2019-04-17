package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchool;
import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2015/12/31.
 */
@Named
public class CrmUGCSchoolDao extends StaticMongoDao<CrmUGCSchool, String> {
    @Override
    protected void calculateCacheDimensions(CrmUGCSchool source, Collection<String> dimensions) {
    }

    public List<CrmUGCSchool> findSchoolIdIs(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        return __find_OTF(filter.toBsonDocument());
    }

    public Page<CrmUGCSchool> ugcSchoolFindIn(Collection<Integer> triggerType, Integer checkupStatus, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").in(triggerType);
        if (checkupStatus != null) {
            filter.and("authenticationstate").is(checkupStatus);
        }
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    public Page<CrmUGCSchool> ugcSchoolFindIs(Integer triggerType, Integer checkupStatus, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        if (checkupStatus != null) {
            filter.and("authenticationstate").is(checkupStatus);
        }
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    public Page<CrmUGCSchool> ugcSchoolAnswerChange(Integer triggerType, Integer checkupStatus, Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        if (checkupStatus != null) {
            filter.and("authenticationstate").is(checkupStatus);
        }
        BsonDocument document = filter.toBsonDocument();
        document.put("$where", new BsonString("this.ugcSchoolName != this.historyUgcSchoolName"));
        return __pageFind_OTF(document, pageable);
    }

    public long getUgcSchoolCount() {
        return __count_OTF();
    }

    public List<CrmUGCSchool> allUgcSchoolData(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }

    public List<CrmUGCSchool> getUgcSchoolInfo(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        return __find_OTF(filter.toBsonDocument());

    }

    public Page<CrmUGCSchool> ugcSchoolTaskAssignedIs(boolean isTaskAssigned, Boolean isTaskFinished, Pageable pageable) {
        Filter filter = filterBuilder.where("isTaskAssigned").is(isTaskAssigned);
        if (isTaskFinished != null) {
            filter.and("isTaskFinished").is(isTaskFinished);
        }
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    public List<CrmUGCSchool> findTriggerTypeIn(Collection<Integer> triggerTypes, int limit, int skip) {
        Filter filter = filterBuilder.where("triggerType").in(triggerTypes);
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }
}
