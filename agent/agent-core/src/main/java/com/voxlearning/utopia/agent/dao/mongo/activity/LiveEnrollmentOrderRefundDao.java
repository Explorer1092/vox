package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollmentOrderRefund;

import javax.inject.Named;
import java.util.List;

@Named
public class LiveEnrollmentOrderRefundDao extends StaticCacheDimensionDocumentMongoDao<LiveEnrollmentOrderRefund, String> {

    public List<LiveEnrollmentOrderRefund> loadByOrderId(String orderId){
        Criteria criteria = Criteria.where("orderId").is(orderId);
        return query(Query.query(criteria));
    }
}
