package com.voxlearning.utopia.service.mizar.impl.dao.microcourse;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.CoursePeriodUserRef;

import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 微课堂-课程 DAO
 * Created by Yuechen.Wang on 2016/12/08.
 */
@Named
@CacheBean(type = CoursePeriodUserRef.class)
public class CoursePeriodUserRefDao extends StaticCacheDimensionDocumentMongoDao<CoursePeriodUserRef, String> {

    @CacheMethod
    public List<CoursePeriodUserRef> findByPeriod(@CacheParameter("P") String periodId) {
        if (StringUtils.isBlank(periodId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("periodId").is(periodId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<CoursePeriodUserRef> findByPeriodAndUser(@CacheParameter("P") String periodId, @CacheParameter("U") String userId) {
        Criteria criteria = Criteria.where("periodId").is(periodId).and("userId").is(userId);;
        return query(Query.query(criteria));
    }

    public boolean insertSpecificRef(String periodId, String userId, String targetId, CoursePeriodUserRef.UserPeriodRelation relation, boolean fromWechat) {
        if (StringUtils.isBlank(periodId) || StringUtils.isBlank(userId) || relation == null) {
            return false;
        }

        CoursePeriodUserRef ref = new CoursePeriodUserRef();
        ref.setPeriodId(periodId);
        ref.setUserId(userId);
        ref.setTargetId(targetId);
        ref.setRelation(relation);
        ref.setFromWechat(fromWechat);
        ref.setNotified(false);
        insert(ref);
        return true;
    }

    public long updateUserPeriodRef(String periodId, List<Long> userIds) {
        if (StringUtils.isBlank(periodId) || CollectionUtils.isEmpty(userIds)) {
            return 0;
        }
        Set<String> userIdSet = userIds.stream()
                .map(SafeConverter::toString)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Criteria criteria = Criteria.where("periodId").is(periodId)
                .and("userId").in(userIdSet);
        Update update = Update.update("notified", true).set("updateTime", new Date());
        long cnt = updateMany(createMongoConnection(), criteria, update).getModifiedCount();
        if (cnt > 0) {
            CoursePeriodUserRef ref = new CoursePeriodUserRef();
            ref.setPeriodId(periodId);
            evictDocumentCache(ref);
        }
        return cnt;
    }

}
