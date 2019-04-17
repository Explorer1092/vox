package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollmentOrder;
import org.ini4j.CommonMultiMap;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class LiveEnrollmentOrderDao extends StaticCacheDimensionDocumentMongoDao<LiveEnrollmentOrder, String> {

    public List<LiveEnrollmentOrder> loadByOrderId(String orderId){
        Criteria criteria = Criteria.where("orderId").is(orderId);
        return query(Query.query(criteria));
    }

    public Map<String, List<LiveEnrollmentOrder>> loadByDeliveryIds(Collection<String> deliveryIds,Integer courseType, Date startDate, Date endDate){
        if(CollectionUtils.isEmpty(deliveryIds)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("deliveryId").in(deliveryIds);
        criteria.and("courseType").is(courseType);
        if(startDate != null){
            criteria.and("payTime").gte(startDate);
            if(endDate != null){
                criteria.lt(endDate);
            }
        }else {
            if(endDate != null){
                criteria.and("payTime").lt(endDate);
            }
        }
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(LiveEnrollmentOrder::getDeliveryId));
    }

    public List<LiveEnrollmentOrder> loadByDate(Date startDate, Date endDate){
        if(startDate == null){
            DayRange dayRange = DayRange.current();
            startDate = dayRange.getStartDate();
        }
        Criteria criteria = Criteria.where("payTime").gte(startDate);
        if(endDate != null){
            criteria.lt(endDate);
        }

        return query(Query.query(criteria));
    }

}
