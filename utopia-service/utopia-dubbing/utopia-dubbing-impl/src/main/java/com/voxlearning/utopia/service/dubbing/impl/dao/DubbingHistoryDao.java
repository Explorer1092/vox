package com.voxlearning.utopia.service.dubbing.impl.dao;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.api.concurrent.UninterruptiblyFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;

import javax.inject.Named;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2017/8/25.
 */
@Named
@CacheBean(type = DubbingHistory.class, useValueWrapper = true)
public class DubbingHistoryDao extends AsyncStaticMongoPersistence<DubbingHistory, String> {

    /**
     * Calculate cache dimensions based on specified document.
     *
     * @param document   the non null document.
     * @param dimensions put calculated result into this
     */
    @Override
    protected void calculateCacheDimensions(DubbingHistory document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod
    public List<DubbingHistory> getDubbingHistoryByUserId(@CacheParameter(value = "UID") Long userId) {
        if (userId == null || userId == 0L) {
            return Collections.emptyList();
        }
        Pattern pattern = Pattern.compile("^" + userId + "_");
        Criteria criteriaId = Criteria.where("fixId").regex(pattern);
        Criteria criteriaDisable = Criteria.where("disabled").is(Boolean.FALSE);
        Criteria andCriteria = Criteria.and(criteriaId, criteriaDisable);
        Query query = Query.query(andCriteria);
        return $executeQuery(createMongoConnection(), query).getUninterruptibly();
    }

    @CacheMethod
    public List<DubbingHistory> getDubbingHistoryByClazzIdAndDubbingId(@CacheParameter(value = "CID") Long clazzId,
                                                                       @CacheParameter(value = "DID") String dubbingId) {

        if (clazzId == null || clazzId == 0L || StringUtils.isBlank(dubbingId)) {
            return Collections.emptyList();
        }
        Criteria criteriaDubbingId = Criteria.where("dubbingId").is(dubbingId);
        Criteria criteriaClazzId = Criteria.where("clazzId").is(clazzId);
        Criteria criteriaDisable = Criteria.where("disabled").is(Boolean.FALSE).and("isPublished").is(Boolean.TRUE);
        Criteria andCriteria = Criteria.and(criteriaDubbingId, criteriaClazzId, criteriaDisable);
        Query query = Query.query(andCriteria);
        Map<Long, List<DubbingHistory>> userDubbings = $executeQuery(createMongoConnection(), query).getUninterruptibly().stream().collect(Collectors.groupingBy(DubbingHistory::getUserId));
        if (MapUtils.isEmpty(userDubbings)) {
            return Collections.emptyList();
        }
        List<DubbingHistory> latestDubbingList = new ArrayList<>();
        userDubbings.values().forEach(e -> {
            /*
            * 1、班级动态取用户已发布的配音
            * 2、一个同学多次完成一个配音，仅显示时间最近的一次
            * */
            DubbingHistory latestDubbing = e.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).findFirst().orElse(null);
            if (latestDubbing != null) {
                latestDubbingList.add(latestDubbing);
            }
        });
        return latestDubbingList;

    }

    @CacheMethod
    public Map<String, Long> getDubbingHistoryCountByUserIdAndDubbingIds(@CacheParameter(value = "UID") Long userId,
                                                                         @CacheParameter(value = "DID", multiple = true) Collection<String> dubbingIds) {
        if (userId == null || userId == 0L || CollectionUtils.isEmpty(dubbingIds)) {
            return Collections.emptyMap();
        }
        Map<String, UninterruptiblyFuture<Long>> futureMap = new HashMap<>();
        Map<String, Long> resultMap = new HashMap<>();
        Set<String> dubbingSet = new HashSet<>(dubbingIds);
        dubbingSet.forEach(e -> {
            Pattern pattern = Pattern.compile("^" + userId + "_" + e + "_");
            Criteria criteriaId = Criteria.where("fixId").regex(pattern);
            Criteria criteriaDisable = Criteria.where("disabled").is(Boolean.FALSE);
            Criteria criteriaIsHomework = Criteria.where("isHomework").is(Boolean.FALSE);
            Criteria andCriteria = Criteria.and(criteriaId, criteriaDisable, criteriaIsHomework);
            Query query = Query.query(andCriteria);
            UninterruptiblyFuture<Long> dubbingCountFuture = $executeCount(createMongoConnection(), query);
            futureMap.put(e, dubbingCountFuture);
        });
        futureMap.forEach((k, v) -> {
            if (StringUtils.isNotBlank(k) && v != null) {
                resultMap.put(k, v.getUninterruptibly());
            }
        });
        return resultMap;
    }

    @CacheMethod
    public DubbingHistory getDubbingHistoryByHomeworkId(@CacheParameter(value = "UID") Long userId, @CacheParameter(value = "DID") String dubbingId, @CacheParameter(value = "HID") String homeworkId) {
        if (userId == null || userId == 0L || StringUtils.isEmpty(dubbingId) || StringUtils.isBlank(homeworkId)) {
            return null;
        }

        Pattern pattern = Pattern.compile("^" + userId + "_" + dubbingId + "_");
        Criteria criteriaId = Criteria.where("fixId").regex(pattern);
        Criteria criteriaDisable = Criteria.where("disabled").is(Boolean.FALSE);
        Criteria criteriaIsHomework = Criteria.where("isHomework").is(Boolean.TRUE);
        Criteria criteriaHomeworkId = Criteria.where("homeworkId").is(homeworkId);
        Criteria andCriteria = Criteria.and(criteriaId, criteriaDisable, criteriaIsHomework, criteriaHomeworkId);
        Query query = Query.query(andCriteria);
        UninterruptiblyFuture<List<DubbingHistory>> executeQuery = $executeQuery(createMongoConnection(), query);
        return executeQuery.getUninterruptibly().stream().findFirst().orElse(null);

    }

    @CacheMethod
    public Long getDubbingHistoryCountByUserId(@CacheParameter(value = "UID_COUNT") Long userId) {
        if (userId == null || userId == 0L) {
            return 0L;
        }
        Pattern pattern = Pattern.compile("^" + userId + "_");
        Criteria criteriaId = Criteria.where("fixId").regex(pattern);
        Criteria criteriaDisable = Criteria.where("disabled").is(Boolean.FALSE);
        Criteria criteriaIsHomework = Criteria.where("isHomework").is(Boolean.FALSE);
        Criteria andCriteria = Criteria.and(criteriaId, criteriaDisable, criteriaIsHomework);
        Query query = Query.query(andCriteria);
        return $executeCount(createMongoConnection(), query).getUninterruptibly();
    }

    @CacheMethod
    public Map<String, Integer> getDubbingHistoryCountByUserIdAndCategoryId(@CacheParameter(value = "UID") Long userId, @CacheParameter(value = "CAID", multiple = true) Collection<String> categoryIds) {
        if (userId == null || userId == 0L || CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }
        Map<String, UninterruptiblyFuture<List<DubbingHistory>>> futureMap = new HashMap<>();
        Map<String, Integer> resultMap = new HashMap<>();
        Set<String> categorySet = new HashSet<>(categoryIds);
        categorySet.forEach(e -> {
            Pattern pattern = Pattern.compile("^" + userId + "_");
            Criteria criteriaId = Criteria.where("fixId").regex(pattern);
            Criteria criteriaCategoryId = Criteria.where("categoryId").is(e);
            Criteria criteriaDisable = Criteria.where("disabled").is(Boolean.FALSE);
            Criteria criteriaIsHomework = Criteria.where("isHomework").is(Boolean.FALSE);
            Criteria andCriteria = Criteria.and(criteriaId, criteriaCategoryId, criteriaDisable, criteriaIsHomework);
            Query query = Query.query(andCriteria);
            UninterruptiblyFuture<List<DubbingHistory>> categoryCountList = $executeQuery(createMongoConnection(), query);
            futureMap.put(e, categoryCountList);
        });
        futureMap.forEach((k, v) -> {
            if (StringUtils.isNotBlank(k) && v != null) {
                resultMap.put(k, v.getUninterruptibly().stream().collect(Collectors.groupingBy(DubbingHistory::getDubbingId)).keySet().size());
            }
        });
        return resultMap;
    }


    //刷fixId,job用,业务禁用
    @Deprecated
    public List<DubbingHistory> jobQueryBySecondary() {
        Criteria criteria = new Criteria();
        Query query = Query.query(criteria);
        UninterruptiblyFuture<List<DubbingHistory>> listUninterruptiblyFuture = $executeQuery(createMongoConnection(), query, ReadPreference.secondaryPreferred(), null);
        return listUninterruptiblyFuture.getUninterruptibly();
    }

    public void disabledDubbingHistory(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        Map<String, DubbingHistory> removeDubbings = loads(ids);
        if (MapUtils.isEmpty(removeDubbings)) {
            return;
        }
        removeDubbings.values().forEach(e -> {
            e.setDisabled(Boolean.TRUE);
            upsert(e);
        });
    }


    public void upsertDubbingHistory(DubbingHistory document) {
        if (document == null) {
            return;
        }
        if (StringUtils.isBlank(document.getId())) {
            document.setId(DubbingHistory.generateId(document.getUserId(), document.getDubbingId(), document.getClazzId(), document.getCategoryId()));
        }
        if (StringUtils.isBlank(document.getFixId())) {
            document.setFixId(DubbingHistory.generateFixId(document.getUserId(), document.getDubbingId()));
        }
        if (document.getDisabled() == null) {
            document.setDisabled(Boolean.FALSE);
        }
        super.upsert(document);
    }
}
