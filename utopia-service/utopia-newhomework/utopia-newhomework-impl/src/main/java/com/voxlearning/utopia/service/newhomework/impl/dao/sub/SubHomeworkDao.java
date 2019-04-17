/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.async.AsyncStaticMongoDao;
import com.voxlearning.alps.dao.mongo.support.SingleResultFuture;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomework;
import com.voxlearning.utopia.service.newhomework.api.exception.CannotCreateHomeworkException;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/1/11
 */
@Named
@UtopiaCacheSupport(value = SubHomework.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class SubHomeworkDao extends AsyncStaticMongoDao<SubHomework, String> {

    @Override
    protected void calculateCacheDimensions(SubHomework document, Collection<String> dimensions) {
        dimensions.add(SubHomework.ck_id(document.getId()));
        dimensions.add(SubHomework.ck_clazzGroupId(document.getClazzGroupId()));
    }

    public void insert(SubHomework entity) {
        $insert(entity);
        SubHomework subHomework = $load(entity.getId());
        if (subHomework == null) {
            throw new CannotCreateHomeworkException("Failed to write SubHomework to the database");
        }
        // 缓存处理
        Map<String, SubHomework> map = new LinkedHashMap<>();
        map.put(SubHomework.ck_id(entity.getId()), entity);
        getCache().safeAdds(map, getDefaultCacheExpirationInSeconds());

        String ck_cg = SubHomework.ck_clazzGroupId(entity.getClazzGroupId());
        getCache().createCacheValueModifier()
                .key(ck_cg)
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> CollectionUtils.addToSet(currentValue, entity.toLocation()))
                .execute();
    }

    public void inserts(Collection<SubHomework> entities) {
        entities.forEach(this::insert);
    }

    public Boolean updateSubHomeworkChecked(String id, Boolean checked, Date checkTime, HomeworkSourceType checkHomeworkSource) {
        if (StringUtils.isBlank(id)) {
            return Boolean.FALSE;
        }
        return internalUpdateSubHomework(id, null, null, checked, checkTime, checkHomeworkSource);
    }

    public Boolean updateSubHomeworkTime(String id, Date start, Date end) {
        if (StringUtils.isBlank(id)) {
            return Boolean.FALSE;
        }
        return internalUpdateSubHomework(id, start, end, null, null, null);
    }


    public Boolean updateDisabledTrue(String homeworkId) {

        if (StringUtils.isBlank(homeworkId)) {
            return false;
        }

        Criteria criteria = Criteria.where("_id").is(homeworkId);
        Update update = new Update();
        update.set("updateAt", new Date());
        update.set("disabled", Boolean.TRUE);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);

        MongoNamespace namespace = calculateIdMongoNamespace(homeworkId);
        SingleResultFuture<SubHomework> future = executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options);
        SubHomework modified = future.get();

        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(SubHomework.ck_id(homeworkId))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();

            String ck_cg = SubHomework.ck_clazzGroupId(modified.getClazzGroupId());
            getCache().delete(ck_cg);
        }
        return modified != null;
    }

    /**
     * 目前只考虑这几个更新字段
     *
     * @param id                  主键
     * @param start               起始时间
     * @param end                 结束时间
     * @param checked             是否检查
     * @param checkTime           检查时间
     * @param checkHomeworkSource 检查的端类型
     * @return 是否更新成功
     */
    private Boolean internalUpdateSubHomework(String id,
                                              Date start,
                                              Date end,
                                              Boolean checked,
                                              Date checkTime,
                                              HomeworkSourceType checkHomeworkSource) {


        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("updateAt", new Date());

        // 起始时间
        if (Objects.nonNull(start)) {
            update.set("startTime", start);
        }
        // 结束时间
        if (Objects.nonNull(end)) {
            update.set("endTime", end);
        }
        // 检查状态，这三个应该同时出现
        if (Objects.nonNull(checked) && Objects.nonNull(checkTime)) {
            update.set("checked", checked);
            update.set("checkedAt", checkTime);
        }
        update.set("checkHomeworkSource", checkHomeworkSource);

        MongoNamespace namespace = calculateIdMongoNamespace(id);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);
        SingleResultFuture<SubHomework> future = executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options);
        SubHomework modified = future.get();
        if (modified != null) {
            changeCache(modified);
        }

        return modified != null;
    }

    //查询条件班组ID，作业创建时间在本学期开始之后
    @CacheMethod
    public Map<Long, List<SubHomework.Location>> loadSubHomeworksByClazzGroupIds(@CacheParameter(value = "CG", multiple = true) Collection<Long> clazzGroupIds) {
        Criteria criteria = Criteria.where("clazzGroupId").in(clazzGroupIds)/*.and("disabled").is(Boolean.FALSE)*/;
        criteria.and("createAt").gte(NewHomeworkConstants.STUDENT_ALLOW_SEARCH_HOMEWORK_START_TIME);
        Query query = Query.query(criteria);
        query.field().includes("_id", "type", "homeworkTag", "teacherId", "clazzGroupId", "checked", "createAt",
                "checkedAt", "startTime", "endTime", "actionId", "subject", "includeSubjective", "includeIntelligentTeaching", "remindCorrection", "disabled", "duration");

        Map<Long, List<SubHomework.Location>> ret = query(query).stream()
                .map(SubHomework::toLocation)
                .collect(Collectors.groupingBy(SubHomework.Location::getClazzGroupId));
        return clazzGroupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

    //这个接口不走缓存
    //根据班组IDs，创建作业的时间范围
    //默认这个时间范围是一个月
    public Map<Long, List<SubHomework.Location>> loadSubHomeworksByClazzGroupIdsWithTimeLimit(Collection<Long> clazzGroupIds, Date begin, Date end) {
        Criteria criteria = Criteria.where("clazzGroupId").in(clazzGroupIds).and("createAt").gte(begin).lte(end)/*.and("disabled").is(Boolean.FALSE)*/;
        Query query = Query.query(criteria);
        query.field().includes("_id", "type", "homeworkTag", "teacherId", "clazzGroupId", "checked", "createAt",
                "checkedAt", "startTime", "endTime", "actionId", "subject", "includeSubjective", "disabled", "duration");

        Map<Long, List<SubHomework.Location>> ret = query(query).stream()
                .map(SubHomework::toLocation)
                .collect(Collectors.groupingBy(SubHomework.Location::getClazzGroupId));
        return clazzGroupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

    /**
     * 根据时间段查询已经检查的作业id，定时任务专属
     * xuesong.zhang
     *
     * @param start 起始时间
     * @param end   结束时间
     * @return 作业ids
     */
    public Collection<String> findIdsByCheckedTimes(Date start, Date end) {
        Criteria criteria = Criteria.where("checked").is(Boolean.TRUE)
                .and("checkedAt").gte(start).lte(end);
        Query query = Query.query(criteria);
        query.field().includes("_id");

        SingleResultFuture<List<SubHomework>> list = executeQuery(createMongoConnection(), query, ReadPreference.secondaryPreferred(), null);
        List<SubHomework> homeworkList = list.get();
        if (CollectionUtils.isEmpty(homeworkList)) {
            return Collections.emptyList();
        }
        return list.get().stream().map(SubHomework::getId).collect(Collectors.toList());
    }

    /**
     * 根据老师和时间段查询已经检查的作业id，定时任务(老师洗白白任务)专属
     * guohong.tan
     *
     * @param teacherId 老师ID
     * @param start     起始时间
     * @return 作业ids
     */
    public Collection<String> findIdsByTeacherIdAndCheckedTimes(Long teacherId, Date start) {
        Criteria criteria = Criteria.where("teacherId").is(teacherId).and("checkedAt").gte(start);
        Query query = Query.query(criteria);
        query.field().includes("_id");

        SingleResultFuture<List<SubHomework>> list = executeQuery(createMongoConnection(), query, ReadPreference.secondaryPreferred(), null);
        List<SubHomework> homeworkList = list.get();
        if (CollectionUtils.isEmpty(homeworkList)) {
            return Collections.emptyList();
        }
        return list.get().stream().map(SubHomework::getId).collect(Collectors.toList());
    }

    /**
     * 根据老师和布置作业时间段查询作业id，定时任务(新认证领取话费活动)专属
     * guohong.tan
     *
     * @param teacherId 老师ID
     * @param start     起始时间
     * @return 作业ids
     */
    public Collection<SubHomework.Location> findIdsByTeacherIdAndCreateAt(Long teacherId, Date start, Date end) {
        Criteria criteria = Criteria.where("teacherId").is(teacherId).and("createAt").gte(start).lt(end);
        Query query = Query.query(criteria);
        query.field().includes("_id", "type", "homeworkTag", "teacherId", "clazzGroupId", "checked", "createAt",
                "checkedAt", "startTime", "endTime", "actionId", "subject", "includeSubjective", "disabled", "duration");

        SingleResultFuture<List<SubHomework>> list = executeQuery(createMongoConnection(), query, ReadPreference.secondaryPreferred(), null);
        List<SubHomework> homeworkList = list.get();
        if (CollectionUtils.isEmpty(homeworkList)) {
            return Collections.emptyList();
        }
        return list.get().stream().map(SubHomework::toLocation).collect(Collectors.toList());
    }

    /**
     * CRM专用，查询结束时间在某个时间段的作业
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return List
     */
    public List<SubHomework.Location> findHomeworkByEndTime(Date begin, Date end) {
        Criteria criteria = Criteria.where("endTime").gte(begin).lte(end).and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        query.field().includes("_id", "type", "homeworkTag", "teacherId", "clazzGroupId", "checked", "createAt",
                "checkedAt", "startTime", "endTime", "actionId", "subject", "includeSubjective", "disabled", "duration");

        SingleResultFuture<List<SubHomework>> list = executeQuery(createMongoConnection(), query, ReadPreference.secondaryPreferred(), null);
        List<SubHomework> homeworkList = list.get();
        if (CollectionUtils.isEmpty(homeworkList)) {
            return Collections.emptyList();
        }
        return list.get().stream().map(SubHomework::toLocation).collect(Collectors.toList());
    }

    /**
     * CRM专用，用于修改作业结束时间
     *
     * @param homeworkId 作业id
     * @param endTime    结束时间
     * @return boolean
     */
    public boolean changeHomeworkEndTime(String homeworkId, Date endTime) {
        if (StringUtils.isBlank(homeworkId)) {
            return false;
        }

        Criteria criteria = Criteria.where("_id").is(homeworkId);
        Update update = new Update();
        update.set("endTime", endTime);
        update.set("updateAt", new Date());
        update.set("additions.changeEndTime", "true");

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);

        MongoNamespace namespace = calculateIdMongoNamespace(homeworkId);
        SingleResultFuture<SubHomework> future = executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options);
        SubHomework modified = future.get();

        if (modified != null) {
            changeCache(modified);
        }

        return modified != null;
    }


    private void changeCache(SubHomework newHomework) {
        getCache().createCacheValueModifier()
                .key(SubHomework.ck_id(newHomework.getId()))
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> newHomework)
                .execute();

        String ck_cg = SubHomework.ck_clazzGroupId(newHomework.getClazzGroupId());
        SubHomework.Location modify = newHomework.toLocation();
        ChangeCacheObject<List<SubHomework.Location>> modifier = locations -> {
            locations.stream()
                    .filter(o -> StringUtils.equalsIgnoreCase(o.getId(), modify.getId()))
                    .forEach(location -> {
                        location.setStartTime(modify.getStartTime());
                        location.setEndTime(modify.getEndTime());
                        location.setChecked(modify.isChecked());
                        location.setCheckedTime(modify.getCheckedTime());
                    });
            return locations;
        };
        CacheValueModifierExecutor<List<SubHomework.Location>> executor = getCache().createCacheValueModifier();
        executor.key(ck_cg)
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(modifier)
                .execute();
    }

    //crm 恢复删除作业
    public Boolean crmUpdateDisabledTrue(String homeworkId) {

        if (StringUtils.isBlank(homeworkId)) {
            return false;
        }

        Criteria criteria = Criteria.where("_id").is(homeworkId);
        Update update = new Update();
        update.set("updateAt", new Date());
        update.set("disabled", Boolean.FALSE);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);

        MongoNamespace namespace = calculateIdMongoNamespace(homeworkId);
        SingleResultFuture<SubHomework> future = executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options);
        SubHomework modified = future.get();

        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(SubHomework.ck_id(homeworkId))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();

            String ck_cg = SubHomework.ck_clazzGroupId(modified.getClazzGroupId());
            getCache().delete(ck_cg);
        }
        return modified != null;
    }
}