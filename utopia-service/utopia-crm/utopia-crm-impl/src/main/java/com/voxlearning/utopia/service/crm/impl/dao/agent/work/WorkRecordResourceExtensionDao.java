package com.voxlearning.utopia.service.crm.impl.dao.agent.work;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordResourceExtension;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * WorkRecordResourceExtensionDao
 *
 * @author deliang.che
 * @since  2019/1/18
 */
@Named
@CacheBean(type = WorkRecordResourceExtension.class)
public class WorkRecordResourceExtensionDao extends StaticCacheDimensionDocumentMongoDao<WorkRecordResourceExtension, String> {

    public List<WorkRecordResourceExtension> loadByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate){
        Criteria criteria = Criteria.where("userId").in(userIds);
        criteria.and("workTime").gte(startDate).lt(endDate);
        return query(Query.query(criteria));
    }
}
