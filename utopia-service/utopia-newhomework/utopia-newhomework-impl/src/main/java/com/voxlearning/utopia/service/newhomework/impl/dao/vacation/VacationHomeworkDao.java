package com.voxlearning.utopia.service.newhomework.impl.dao.vacation;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;

import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/11/25
 */
@Named
@CacheBean(type = VacationHomework.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class VacationHomeworkDao extends StaticMongoShardPersistence<VacationHomework, String> {

    @Override
    protected void calculateCacheDimensions(VacationHomework document, Collection<String> dimensions) {
        dimensions.add(VacationHomework.ck_id(document.getId()));
        dimensions.add(VacationHomework.ck_clazzGroupId(document.getClazzGroupId()));
        dimensions.add(VacationHomework.ck_packageId(document.getPackageId()));
    }

    public void insert(VacationHomework entity) {
        if (entity == null
                || StringUtils.isBlank(entity.getPackageId())
                || entity.getWeekRank() == null
                || entity.getDayRank() == null
                || entity.getStudentId() == null) {
            return;
        }
        VacationHomework.ID ID = new VacationHomework.ID(entity.getPackageId(), entity.getWeekRank(), entity.getDayRank(), entity.getStudentId());
        entity.setId(ID.toString());
        $insert(entity).awaitUninterruptibly();

        VacationHomework vacationHomework = $load(entity.getId()).getUninterruptibly();
        if (vacationHomework == null) {
            return;
        }

        // 缓存处理
        Map<String, VacationHomework> map = new LinkedHashMap<>();
        map.put(VacationHomework.ck_id(entity.getId()), entity);
        getCache().safeAdds(map, getDefaultCacheExpirationInSeconds());

        String ck_cg = VacationHomework.ck_clazzGroupId(entity.getClazzGroupId());
        getCache().createCacheValueModifier()
                .key(ck_cg)
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> CollectionUtils.addToSet(currentValue, entity))
                .execute();

        String ck_pk = VacationHomework.ck_packageId(entity.getPackageId());
        getCache().createCacheValueModifier()
                .key(ck_pk)
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> CollectionUtils.addToSet(currentValue, entity))
                .execute();
    }

    public void inserts(Collection<VacationHomework> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        for (VacationHomework vacationHomework : entities) {
            insert(vacationHomework);
        }
    }

    @CacheMethod
    public Map<Long, List<VacationHomework.Location>> loadVacationHomeworkByClazzGroupIds(@CacheParameter(value = "CG", multiple = true) Collection<Long> groupIds) {
        Criteria criteria = Criteria.where("clazzGroupId").in(groupIds);
        Query query = Query.query(criteria);
        query.field().includes("_id", "packageId", "weekRank", "dayRank", "type", "teacherId", "clazzGroupId", "studentId", "createAt",
                "startTime", "endTime", "actionId", "subject");
        return query(query).stream().map(VacationHomework::toLocation)
                .collect(Collectors.groupingBy(VacationHomework.Location::getClazzGroupId));
    }

    @CacheMethod
    public Map<String, List<VacationHomework.Location>> loadVacationHomeworkByPackageIds(@CacheParameter(value = "VP", multiple = true) Collection<String> packageIds) {
        Criteria criteria = Criteria.where("packageId").in(packageIds);
        Query query = Query.query(criteria);
        query.field().includes("_id", "packageId", "weekRank", "dayRank", "type", "teacherId", "clazzGroupId", "studentId", "createAt",
                "startTime", "endTime", "actionId", "subject");
        return query(query).stream().map(VacationHomework::toLocation)
                .collect(Collectors.groupingBy(VacationHomework.Location::getPackageId));
    }
}
