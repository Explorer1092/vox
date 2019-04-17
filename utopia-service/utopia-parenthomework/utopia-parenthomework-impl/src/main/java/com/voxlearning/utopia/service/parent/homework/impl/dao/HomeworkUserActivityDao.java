package com.voxlearning.utopia.service.parent.homework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserActivity;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 作业活动表
 *
 * @author Wenlong Meng
 * @since Feb 23, 2019
 */
@Named
@CacheBean(type = UserActivity.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
@Slf4j
public class HomeworkUserActivityDao extends AlpsStaticMongoDao<UserActivity, String> {

    /**
     * 缓存维度
     *
     * @param document
     * @param dimensions
     */
    @Override
    protected void calculateCacheDimensions(UserActivity document, Collection<String> dimensions) {
        dimensions.add(document.ckId());
    }

    /**
     * 查询未完成任务用户
     *
     * @param activityId 活动id
     * @param offset
     * @param limit
     * @param startTime
     * @param endTime
     * @return
     */
    public List<UserActivity> loadDNFUsers(String activityId, int offset, int limit, Date startTime, Date endTime) {
        Criteria criteria = Criteria
                .where("activityId").is(activityId)
                .and("finished").ne(true)
                .and("updateTime").gte(startTime)
                .lt(endTime);
        Query query = Query.query(criteria).skip(offset).limit(limit);
        return query(query);
    }

    /**
     * 查询未完成任务用户
     *
     * @param activityId 活动id
     * @return
     */
    public long count(String activityId) {
        Criteria criteria = Criteria
                .where("activityId").is(activityId);
        Query query = Query.query(criteria);
        return count(query);
    }

    /**
     * 查询完成任务用户
     *
     * @param activityId 活动id
     * @param startTime
     * @param endTime
     * @return
     */
    public List<UserActivity> loadDoneUsers(String activityId, Date startTime, Date endTime) {
        Criteria criteria = Criteria
                .where("activityId").is(activityId)
                .and("finished").is(true)
                .and("createTime").gte(startTime)
                .lt(endTime);
        Query query = Query.query(criteria);
        return query(query);
    }

}
