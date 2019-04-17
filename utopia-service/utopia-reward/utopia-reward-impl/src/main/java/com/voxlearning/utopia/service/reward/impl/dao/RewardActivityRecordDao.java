package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.reward.entity.RewardActivityRecord;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 奖品中心 - 用户参与活动记录表持久层实现
 * Created by haitian.gan on 2017/2/4.
 */
@Named
@CacheBean(type = RewardActivityRecord.class)
public class RewardActivityRecordDao extends AlpsStaticJdbcDao<RewardActivityRecord,Long> {

    //@Inject private UtopiaSqlFactory utopiaSqlFactory;
    //private UtopiaSql utopiaSql;
    @Override
    public void afterPropertiesSet() throws Exception {
        //this.utopiaSql = utopiaSqlFactory.getUtopiaSql("order");
    }

    @Override
    protected void calculateCacheDimensions(RewardActivityRecord document, Collection<String> dimensions) {
        //dimensions.add(RewardActivityRecord.ck_acid_uid(document.getActivityId(), document.getUserId()));
        dimensions.add(RewardActivityRecord.ck_acid(document.getActivityId()));
        dimensions.add(RewardActivityRecord.ck_uid(document.getUserId()));
        //dimensions.add(RewardActivityRecord.ck_collect_uid(document.getUserId()));
    }

    /*@CacheMethod
    public List<RewardActivityRecord> loadActivityRecordsUnderUser(
            @CacheParameter("ACID") Long activityId,
            @CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("ACTIVITY_ID").is(activityId).and("USER_ID").is(userId);
        Query query = Query.query(criteria);
        return query(query);
    }*/

/*    @CacheMethod
    public List<RewardActivityRecord> loadActivityRecords(@CacheParameter("ACID") Long activityId, int limit) {
        Criteria criteria = Criteria.where("ACTIVITY_ID").is(activityId);
        Sort createTimeDesc = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        Query query = Query.query(criteria).with(createTimeDesc).limit(limit);

        return query(query);
    }*/

    @CacheMethod(expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today))
    public List<RewardActivityRecord> loadUserRecords(@CacheParameter("USER_ID")Long userId){
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        Query query = Query.query(criteria);
        return query(query);
    }

    /*@CacheMethod(expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today))
    public List<RewardActivityRecord> loadUserRecordsInDay(@CacheParameter("UID")Long userId, Date date){
        Date startTime = DateUtils.getDayStart(date);
        Date endTime = DateUtils.getDayEnd(date);

        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("CREATE_DATETIME").gte(startTime).lte(endTime);
        Query query = Query.query(criteria);
        return query(query);
    }*/

   /* @CacheMethod
    public List<RewardActivityRecord> loadUserCollectRecords(@CacheParameter("COLLECT_UID") Long userId) {
        return utopiaSql.withSql(
                "SELECT ID,ACTIVITY_ID,USER_ID,USER_NAME,sum(price) as price,count(ID) as collectNums,MAX(CREATE_DATETIME) as create_datetime " +
                        " FROM VOX_REWARD_ACTIVITY_RECORD WHERE USER_ID = ? GROUP BY ACTIVITY_ID ")
                .useParamsArgs(userId)
                .queryAll(BeanPropertyRowMapper.newInstance(RewardActivityRecord.class));
    }*/

}
