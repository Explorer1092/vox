package com.voxlearning.utopia.agent.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2015/12/31.
 */
@Named("agent.CrmUGCSchoolDao")
public class CrmUGCSchoolDao extends StaticMongoDao<CrmUGCSchool, String> {
    @Override
    protected void calculateCacheDimensions(CrmUGCSchool source, Collection<String> dimensions) {
    }

    public List<CrmUGCSchool> findSchoolIdIs(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        return __find_OTF(filter.toBsonDocument());
    }
}
