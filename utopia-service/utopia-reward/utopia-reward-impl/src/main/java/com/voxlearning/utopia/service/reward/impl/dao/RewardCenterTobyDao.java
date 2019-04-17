package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.reward.entity.newversion.RewardCenterToby;

import javax.inject.Named;
import java.util.*;

@Named
@CacheBean(type = RewardCenterToby.class, useValueWrapper = true)
public class RewardCenterTobyDao extends AlpsStaticMongoDao<RewardCenterToby, String> {
    @Override
    protected void calculateCacheDimensions(RewardCenterToby document, Collection<String> dimensions) {
        dimensions.add(RewardCenterToby.ck_userId(document.getUserId()));
    }

    public List<RewardCenterToby> loadAccessoryErrExpiryTime(Long timeStamp) {
        Criteria criteria;
        if (timeStamp == -1) {
            criteria = Criteria.where("accessoryExpiryTimeStamp").is(timeStamp);
        } else {
            criteria = Criteria.where("accessoryExpiryTimeStamp").gt(timeStamp);
        }
        return query(Query.query(criteria).limit(1000));
    }

    public List<RewardCenterToby> loadCountenanceErrExpiryTime(Long timeStamp) {
        Criteria criteria;
        if (timeStamp == -1) {
            criteria = Criteria.where("countenanceExpiryTimeStamp").is(timeStamp);
        } else {
            criteria = Criteria.where("countenanceExpiryTimeStamp").gt(timeStamp);
        }
        return query(Query.query(criteria).limit(1000));
    }

    public List<RewardCenterToby> loadImageErrExpiryTime(Long timeStamp) {
        Criteria criteria;
        if (timeStamp == -1) {
            criteria = Criteria.where("imageExpiryTimeStamp").is(timeStamp);
        } else {
            criteria = Criteria.where("imageExpiryTimeStamp").gt(timeStamp);
        }
        return query(Query.query(criteria).limit(1000));
    }

    public List<RewardCenterToby> loadPropsErrExpiryTime(Long timeStamp) {
        Criteria criteria;
        if (timeStamp == -1) {
            criteria = Criteria.where("propsExpiryTimeStamp").is(timeStamp);
        } else {
            criteria = Criteria.where("propsExpiryTimeStamp").gt(timeStamp);
        }
        return query(Query.query(criteria).limit(1000));
    }

    @CacheMethod
    public RewardCenterToby loadByUserId(@CacheParameter("UID") Long userId)  {
        Criteria criteria = Criteria.where("userId").is(userId);
        List<RewardCenterToby> tobyList = query(Query.query(criteria));
        if (CollectionUtils.isNotEmpty(tobyList)) {
            return tobyList.get(0);
        }

        return null;
    }

    @CacheMethod
    public Map<Long, RewardCenterToby> loadByUserIdList(@CacheParameter(value = "UID", multiple = true) Collection<Long> userIdList)  {
        Criteria criteria = Criteria.where("userId").in(userIdList);
        List<RewardCenterToby> result = query(Query.query(criteria));
        if (CollectionUtils.isEmpty(result)) {
            return Collections.emptyMap();
        }

        Map<Long, RewardCenterToby> retMap = new LinkedHashMap<>();

        for (RewardCenterToby toby : result) {
            if (!retMap.containsKey(toby.getUserId())) {
                retMap.put(toby.getUserId(), toby);
            }
        }

        return retMap;
    }

}
