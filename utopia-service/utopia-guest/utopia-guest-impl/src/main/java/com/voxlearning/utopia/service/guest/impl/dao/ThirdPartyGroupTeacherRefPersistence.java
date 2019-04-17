package com.voxlearning.utopia.service.guest.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroupTeacherRef;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link ThirdPartyGroupTeacherRef} persistence implementation.
 *
 * @author xuesong.zhang
 * @since 2016/9/22
 */
@Named
@UtopiaCacheSupport(ThirdPartyGroupTeacherRef.class)
public class ThirdPartyGroupTeacherRefPersistence extends StaticPersistence<Long, ThirdPartyGroupTeacherRef> {

    @Override
    protected void calculateCacheDimensions(ThirdPartyGroupTeacherRef source, Collection<String> dimensions) {
        dimensions.add(ThirdPartyGroupTeacherRef.ck_id(source.getId()));
        dimensions.add(ThirdPartyGroupTeacherRef.ck_teacherId(source.getTeacherId()));
        dimensions.add(ThirdPartyGroupTeacherRef.ck_groupId(source.getGroupId()));
    }

    /**
     * @param teacherId 老师id
     * @return List
     */
    public List<ThirdPartyGroupTeacherRef> findByTeacherIdIncludeDisabled(final Long teacherId) {
        if (teacherId == null) {
            return Collections.emptyList();
        }
        return withSelectFromTable("WHERE TEACHER_ID=?")
                .useParamsArgs((teacherId)).queryAll();
    }

    /**
     * @param teacherId 老师id
     * @return List
     */
    @UtopiaCacheable
    public List<ThirdPartyGroupTeacherRef> findByTeacherId(@UtopiaCacheKey(name = "TID") final Long teacherId) {
        if (teacherId == null) {
            return Collections.emptyList();
        }
        return withSelectFromTable("WHERE TEACHER_ID=? AND DISABLED=FALSE")
                .useParamsArgs(teacherId).queryAll();
    }

    /**
     * @param teacherIds 老师ids
     * @return Map
     */
    public Map<Long, List<ThirdPartyGroupTeacherRef>> findByTeacherIds(Collection<Long> teacherIds) {
        if (CollectionUtils.isEmpty(teacherIds)) {
            return Collections.emptyMap();
        }

        CacheObjectLoader.Loader<Long, List<ThirdPartyGroupTeacherRef>> loader = getCache()
                .getCacheObjectLoader()
                .createLoader(ThirdPartyGroupTeacherRef::ck_teacherId);

        return loader.loads(teacherIds).loadsMissed(
                missedTeacherIds -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("teacherIds", missedTeacherIds);
                    List<ThirdPartyGroupTeacherRef> refs = withSelectFromTable("WHERE TEACHER_ID IN (:teacherIds) AND DISABLED=FALSE").useParams(map).queryAll();
                    return refs.stream()
                            .collect(Collectors.groupingBy(ThirdPartyGroupTeacherRef::getTeacherId));
                }).writeAsList(entityCacheExpirationInSeconds()).getResult();
    }

    /**
     * @param groupId 班组id
     * @return List
     */
    @UtopiaCacheable
    public List<ThirdPartyGroupTeacherRef> findByGroupId(@UtopiaCacheKey(name = "GID") final Long groupId) {
        if (groupId == null) {
            return Collections.emptyList();
        }
        return withSelectFromTable("WHERE THIRD_PARTY_GROUP_ID=? AND DISABLED=FALSE")
                .useParamsArgs((groupId)).queryAll();
    }

    /**
     * @param groupIds 班组ids
     * @return Map
     */
    public Map<Long, List<ThirdPartyGroupTeacherRef>> findByGroupIds(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        CacheObjectLoader.Loader<Long, List<ThirdPartyGroupTeacherRef>> loader = getCache()
                .getCacheObjectLoader()
                .createLoader(ThirdPartyGroupTeacherRef::ck_groupId);

        return loader.loads(groupIds)
                .loadsMissed(missedGroupIds -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("groupIds", missedGroupIds);
                    List<ThirdPartyGroupTeacherRef> refs = withSelectFromTable("WHERE THIRD_PARTY_GROUP_ID IN (:groupIds) AND DISABLED=FALSE").useParams(map).queryAll();
                    return refs.stream()
                            .collect(Collectors.groupingBy(ThirdPartyGroupTeacherRef::getGroupId));
                }).writeAsList(entityCacheExpirationInSeconds()).getResult();
    }

    /**
     * disable group teacher refs
     * <p>
     * FIXME
     * do not use this method
     * please use {@link com.voxlearning.utopia.service.user.impl.service.processor.ThirdPartyGroupTeacherRefProcessor} for logging purpose
     *
     * @param groupTeacherRefs
     * @return
     */
    public int disable(Collection<ThirdPartyGroupTeacherRef> groupTeacherRefs, Date date) {
        if (CollectionUtils.isEmpty(groupTeacherRefs) || date == null) {
            return 0;
        }
        Set<String> cacheKeys = new LinkedHashSet<>();
        List<Long> ids = new LinkedList<>();
        groupTeacherRefs.forEach(e -> {
            ids.add(e.getId());
            calculateCacheDimensions(e, cacheKeys);
        });
        String sql = "SET UPDATE_DATETIME=:date, DISABLED=TRUE WHERE ID IN (:ids) AND DISABLED=FALSE";
        int rows = withUpdateTable(sql).useParams(MiscUtils.m("ids", ids, "date", date)).executeUpdate();
        if (rows > 0) {
            getCache().delete(cacheKeys);
        }
        return rows;
    }
}
