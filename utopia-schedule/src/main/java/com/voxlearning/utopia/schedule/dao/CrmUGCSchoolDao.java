package com.voxlearning.utopia.schedule.dao;

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
@Named("schedule.CrmUGCSchoolDao")
public class CrmUGCSchoolDao extends StaticMongoDao<CrmUGCSchool, String> {
    @Override
    protected void calculateCacheDimensions(CrmUGCSchool source, Collection<String> dimensions) {
    }

    public List<CrmUGCSchool> findTriggerTypeIn(Collection<Integer> triggerTypes, int limit, int skip) {
        Filter filter = filterBuilder.where("triggerType").in(triggerTypes);
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }
}
