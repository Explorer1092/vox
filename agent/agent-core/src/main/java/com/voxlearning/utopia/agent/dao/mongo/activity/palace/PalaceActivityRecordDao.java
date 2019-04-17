package com.voxlearning.utopia.agent.dao.mongo.activity.palace;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollment;
import com.voxlearning.utopia.agent.persist.entity.activity.palace.PalaceActivityRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Named
@CacheBean(type =  PalaceActivityRecord.class)
public class PalaceActivityRecordDao extends StaticCacheDimensionDocumentMongoDao<PalaceActivityRecord, String> {

    public List<PalaceActivityRecord> loadByCoupon(String couponId, Long couponUserId){
        if(StringUtils.isBlank(couponId) || couponUserId == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("couponId").is(couponId).and("couponUserId").is(couponUserId);
        return query(Query.query(criteria));
    }

    public List<PalaceActivityRecord> loadByStudentId(Long studentId){
        if(studentId == null || studentId < 1){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("studentId").is(studentId);
        return query(Query.query(criteria));
    }

    public List<PalaceActivityRecord> loadByActivityAndUser(String activityId, Long userId){
        if(StringUtils.isBlank(activityId) || userId == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId);
        return query(Query.query(criteria));
    }

    public List<PalaceActivityRecord> loadByActivityAndUserAndTime(String activityId, Collection<Long> userIds, Date startDate, Date endDate){
        if(StringUtils.isBlank(activityId) || CollectionUtils.isEmpty(userIds)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").in(userIds);
        if(startDate != null || endDate != null){
            criteria.and("businessTime");
            if(startDate != null){
                criteria.gte(startDate);
            }
            if(endDate != null){
                criteria.lt(endDate);
            }
        }
        return query(Query.query(criteria));
    }
}
