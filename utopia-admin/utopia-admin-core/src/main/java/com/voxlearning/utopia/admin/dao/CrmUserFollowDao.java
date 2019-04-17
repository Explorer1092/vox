package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.entity.crm.CrmUserFollow;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 用户关注信息DAO
 * Created by Yuechen.Wang on 2016-07-18
 */
@Named("admin.CrmUserFollowDao")
public class CrmUserFollowDao extends AlpsStaticMongoDao<CrmUserFollow, String> {

    @Override
    protected void calculateCacheDimensions(CrmUserFollow source, Collection<String> dimensions) {

    }

    /**
     * 根据UserId找到用户关注的对象
     */
    public List<CrmUserFollow> findByFollower(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria
                .where("followerId").is(userId)
                .and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    public CrmUserFollow findByFollowerAndTarget(Long userId, Long targetId, String type) {
        if (userId == null || targetId == null || StringUtils.isBlank(type)) {
            return null;
        }
        Criteria criteria = Criteria
                .where("followerId").is(userId)
                .and("target").is(SafeConverter.toString(targetId))
                .and("followType").is(type)
                .and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }
}
