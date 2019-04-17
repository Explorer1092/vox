package com.voxlearning.utopia.service.crm.impl.dao.crm;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.QiYuSessionRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;

@Named
public class QiYuSessionRecordDao extends AlpsStaticMongoDao<QiYuSessionRecord, Long> {

    @Override
    protected void calculateCacheDimensions(QiYuSessionRecord document, Collection<String> dimensions) {

    }

    public Page<QiYuSessionRecord> query(Date startTime, Date endTime, int page, int pageSize) {
        Criteria criteria = new Criteria();
        if (startTime != null) {
            criteria.and("startTime").gte(startTime.getTime());
        }
        if (endTime != null) {
            criteria.and("startTime").lte(endTime.getTime());
        }
        int realPage = page <= 0 ? 0 : page;
        Query query = new Query(criteria);
        long count = count(query);
        Sort sort = new Sort(Sort.Direction.DESC, "startTime");
        return new PageImpl<>(query(query.with(sort).skip(realPage * pageSize).limit(pageSize)), new PageRequest(realPage, pageSize), count);
    }
}
