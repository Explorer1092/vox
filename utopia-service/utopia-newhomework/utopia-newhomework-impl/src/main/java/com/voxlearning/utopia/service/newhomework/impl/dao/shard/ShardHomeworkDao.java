package com.voxlearning.utopia.service.newhomework.impl.dao.shard;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.connection.IMongoConnection;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomework;
import com.voxlearning.utopia.service.newhomework.api.exception.CannotCreateHomeworkException;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@CacheBean(type = ShardHomework.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class ShardHomeworkDao extends DynamicMongoShardPersistence<ShardHomework, String> {

    private static final int SHARD_HOMEWORK_START_YEAR = 2018;

    @Override
    protected void calculateCacheDimensions(ShardHomework document, Collection<String> dimensions) {
        dimensions.add(ShardHomework.ck_id(document.getId()));
        dimensions.add(ShardHomework.ck_clazzGroupId(document.getClazzGroupId()));
    }

    @Override
    protected String calculateDatabase(String template, ShardHomework document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, ShardHomework document) {
        ShardHomework.ID id = document.parseID();
        String year = StringUtils.substring(id.getMonth(), 0, 4);
        return StringUtils.formatMessage(template, year);
    }

    private String calculateCollectionByYear(Integer year) {
        if (year == null) {
            throw new RuntimeException("year is null");
        }
        String yearStr = year.toString();
        if (StringUtils.isEmpty(yearStr) || yearStr.length() != 4) {
            throw new RuntimeException("Invalid year: " + yearStr);
        }
        String collectionNameTemplate = getMongoDocument().getCollectionName();
        return StringUtils.formatMessage(collectionNameTemplate, yearStr);
    }

    public void insert(ShardHomework entity) {
        $insert(entity).awaitUninterruptibly();
        ShardHomework shardHomework = $load(entity.getId()).getUninterruptibly();
        if (shardHomework == null) {
            throw new CannotCreateHomeworkException("Failed to write ShardHomework to the database");
        }
        // 缓存处理
        Map<String, ShardHomework> map = new LinkedHashMap<>();
        map.put(ShardHomework.ck_id(entity.getId()), entity);
        getCache().safeAdds(map, getDefaultCacheExpirationInSeconds());

        String ck_cg = ShardHomework.ck_clazzGroupId(entity.getClazzGroupId());
        getCache().createCacheValueModifier()
                .key(ck_cg)
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> CollectionUtils.addToSet(currentValue, entity.toLocation()))
                .execute();
    }

    public void inserts(Collection<ShardHomework> entities) {
        entities.forEach(this::insert);
    }

    public Boolean updateShardHomeworkChecked(String id, Boolean checked, Date checkTime, HomeworkSourceType checkHomeworkSource) {
        if (StringUtils.isBlank(id)) {
            return Boolean.FALSE;
        }
        return internalUpdateShardHomework(id, null, null, checked, checkTime, checkHomeworkSource);
    }

    public Boolean updateShardHomeworkTime(String id, Date start, Date end) {
        if (StringUtils.isBlank(id)) {
            return Boolean.FALSE;
        }
        return internalUpdateShardHomework(id, start, end, null, null, null);
    }

    public Boolean updateHomeworkRemindCorrection(String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) {
            return false;
        }

        Criteria criteria = Criteria.where("_id").is(homeworkId);
        Update update = new Update();
        update.set("remindCorrection", Boolean.TRUE);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);

        IMongoConnection connection = createMongoConnection(calculateIdMongoNamespace(homeworkId), homeworkId);
        ShardHomework modified = $executeFindOneAndUpdate(connection, criteria, update, options).getUninterruptibly();

        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(ShardHomework.ck_id(homeworkId))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();

            String ck_cg = ShardHomework.ck_clazzGroupId(modified.getClazzGroupId());
            getCache().delete(ck_cg);
        }
        return modified != null;
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

        IMongoConnection connection = createMongoConnection(calculateIdMongoNamespace(homeworkId), homeworkId);
        ShardHomework modified = $executeFindOneAndUpdate(connection, criteria, update, options).getUninterruptibly();

        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(ShardHomework.ck_id(homeworkId))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();

            String ck_cg = ShardHomework.ck_clazzGroupId(modified.getClazzGroupId());
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
    private Boolean internalUpdateShardHomework(String id,
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

        IMongoConnection connection = createMongoConnection(calculateIdMongoNamespace(id), id);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);
        ShardHomework modified = $executeFindOneAndUpdate(connection, criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(modified);
        }

        return modified != null;
    }

    @CacheMethod
    public Map<Long, List<ShardHomework.Location>> loadShardHomeworksByClazzGroupIds(@CacheParameter(value = "CG", multiple = true) Collection<Long> clazzGroupIds) {
        Map<Long, List<ShardHomework.Location>> resultMap = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        for (int year = SHARD_HOMEWORK_START_YEAR; year <= currentYear; year++) {
            Map<Long, List<ShardHomework.Location>> yearResultMap = internalLoadShardHomeworksByClazzGroupIds(clazzGroupIds, year);
            if (MapUtils.isNotEmpty(yearResultMap)) {
                yearResultMap.forEach((clazzGroupId, locations) -> {
                    if (CollectionUtils.isNotEmpty(locations)) {
                        resultMap.computeIfAbsent(clazzGroupId, k -> new ArrayList<>()).addAll(locations);
                    }
                });
            }
        }
        return clazzGroupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> resultMap.getOrDefault(e, new LinkedList<>())));
    }

    private Map<Long, List<ShardHomework.Location>> internalLoadShardHomeworksByClazzGroupIds(Collection<Long> clazzGroupIds, int year) {
        Criteria criteria = Criteria.where("clazzGroupId").in(clazzGroupIds);
        criteria.and("createAt").gte(NewHomeworkConstants.STUDENT_ALLOW_SEARCH_HOMEWORK_START_TIME);
        Query query = Query.query(criteria);
        query.field().includes("_id", "type", "homeworkTag", "teacherId", "clazzGroupId", "checked", "createAt",
                "checkedAt", "startTime", "endTime", "actionId", "subject", "includeSubjective", "includeIntelligentTeaching", "remindCorrection", "disabled", "duration");

        String databaseName = getMongoDocument().getDatabaseName();
        String collectionName = calculateCollectionByYear(year);
        MongoNamespace mongoNamespace = new MongoNamespace(databaseName, collectionName);
        Map<Long, List<ShardHomework.Location>> ret = $executeQuery(createMongoConnection(mongoNamespace), query)
                .getUninterruptibly()
                .stream()
                .map(ShardHomework::toLocation)
                .collect(Collectors.groupingBy(ShardHomework.Location::getClazzGroupId));
        return clazzGroupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

    //这个接口不走缓存
    //根据班组IDs，创建作业的时间范围
    //默认这个时间范围是一个月
    public Map<Long, List<ShardHomework.Location>> loadShardHomeworksByClazzGroupIdsWithTimeLimit(Collection<Long> clazzGroupIds, Date begin, Date end) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        int startYear = calendar.get(Calendar.YEAR);
        calendar.setTime(end);
        int endYear = calendar.get(Calendar.YEAR);
        Map<Long, List<ShardHomework.Location>> resultMap = new LinkedHashMap<>();
        for (int year = startYear; year <= endYear; year++) {
            Map<Long, List<ShardHomework.Location>> oneYearResultMap = internalLoadShardHomeworksByClazzGroupIdsWithTimeLimit(clazzGroupIds, begin, end, year);
            if (MapUtils.isNotEmpty(oneYearResultMap)) {
                for (Map.Entry<Long, List<ShardHomework.Location>> entry : oneYearResultMap.entrySet()) {
                    if (CollectionUtils.isNotEmpty(entry.getValue())) {
                        resultMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
                    }
                }
            }
        }
        return resultMap;
    }

    private Map<Long, List<ShardHomework.Location>> internalLoadShardHomeworksByClazzGroupIdsWithTimeLimit(Collection<Long> clazzGroupIds, Date begin, Date end, int year) {
        Criteria criteria = Criteria.where("clazzGroupId").in(clazzGroupIds).and("createAt").gte(begin).lte(end)/*.and("disabled").is(Boolean.FALSE)*/;
        Query query = Query.query(criteria);
        query.field().includes("_id", "type", "homeworkTag", "teacherId", "clazzGroupId", "checked", "createAt",
                "checkedAt", "startTime", "endTime", "actionId", "subject", "includeSubjective", "disabled", "duration");
        String databaseName = getMongoDocument().getDatabaseName();
        String collectionName = calculateCollectionByYear(year);
        MongoNamespace mongoNamespace = new MongoNamespace(databaseName, collectionName);
        Map<Long, List<ShardHomework.Location>> ret = $executeQuery(createMongoConnection(mongoNamespace), query, ReadPreference.secondaryPreferred(), null)
                .getUninterruptibly()
                .stream()
                .map(ShardHomework::toLocation)
                .collect(Collectors.groupingBy(ShardHomework.Location::getClazzGroupId));
        return clazzGroupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

    /**
     * 根据老师和布置作业时间段查询作业id，定时任务(新认证领取话费活动)专属
     * guohong.tan
     *
     * @param teacherId 老师ID
     * @param start     起始时间
     * @return 作业ids
     */
    public Collection<ShardHomework.Location> findIdsByTeacherIdAndCreateAt(Long teacherId, Date start, Date end) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        int startYear = calendar.get(Calendar.YEAR);
        calendar.setTime(end);
        int endYear = calendar.get(Calendar.YEAR);
        List<ShardHomework.Location> resultList = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            List<ShardHomework.Location> homeworkList = internalFindIdsByTeacherIdAndCreateAt(teacherId, start, end, year);
            if (CollectionUtils.isNotEmpty(homeworkList)) {
                resultList.addAll(homeworkList);
            }
        }
        return resultList;
    }

    private List<ShardHomework.Location> internalFindIdsByTeacherIdAndCreateAt(Long teacherId, Date start, Date end, int year) {
        Criteria criteria = Criteria.where("teacherId").is(teacherId).and("createAt").gte(start).lt(end);
        Query query = Query.query(criteria);
        query.field().includes("_id", "type", "homeworkTag", "teacherId", "clazzGroupId", "checked", "createAt",
                "checkedAt", "startTime", "endTime", "actionId", "subject", "includeSubjective", "disabled", "duration");
        String databaseName = getMongoDocument().getDatabaseName();
        String collectionName = calculateCollectionByYear(year);
        MongoNamespace mongoNamespace = new MongoNamespace(databaseName, collectionName);
        List<ShardHomework> homeworkList = $executeQuery(createMongoConnection(mongoNamespace), query, ReadPreference.secondaryPreferred(), null).getUninterruptibly();
        if (CollectionUtils.isEmpty(homeworkList)) {
            return Collections.emptyList();
        }
        return homeworkList.stream().map(ShardHomework::toLocation).collect(Collectors.toList());
    }

    /**
     * CRM专用，查询结束时间在某个时间段的作业
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return List
     */
    public List<ShardHomework.Location> findHomeworkByEndTime(Date begin, Date end) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        int startYear = calendar.get(Calendar.YEAR);
        calendar.setTime(end);
        int endYear = calendar.get(Calendar.YEAR);
        List<ShardHomework.Location> resultList = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            List<ShardHomework.Location> homeworkList = internalFindHomeworkByEndTime(begin, end, year);
            if (CollectionUtils.isNotEmpty(homeworkList)) {
                resultList.addAll(homeworkList);
            }
        }
        return resultList;
    }

    private List<ShardHomework.Location> internalFindHomeworkByEndTime(Date begin, Date end, int year) {
        Criteria criteria = Criteria.where("endTime").gte(begin).lte(end).and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        query.field().includes("_id", "type", "homeworkTag", "teacherId", "clazzGroupId", "checked", "createAt",
                "checkedAt", "startTime", "endTime", "actionId", "subject", "includeSubjective", "disabled", "duration");
        String databaseName = getMongoDocument().getDatabaseName();
        String collectionName = calculateCollectionByYear(year);
        MongoNamespace mongoNamespace = new MongoNamespace(databaseName, collectionName);
        List<ShardHomework> homeworkList = $executeQuery(createMongoConnection(mongoNamespace), query, ReadPreference.secondaryPreferred(), null).getUninterruptibly();
        if (CollectionUtils.isEmpty(homeworkList)) {
            return Collections.emptyList();
        }
        return homeworkList.stream().map(ShardHomework::toLocation).collect(Collectors.toList());
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

        IMongoConnection connection = createMongoConnection(calculateIdMongoNamespace(homeworkId), homeworkId);
        ShardHomework modified = $executeFindOneAndUpdate(connection, criteria, update, options).getUninterruptibly();

        if (modified != null) {
            changeCache(modified);
        }

        return modified != null;
    }

    private void changeCache(ShardHomework shardHomework) {
        getCache().createCacheValueModifier()
                .key(ShardHomework.ck_id(shardHomework.getId()))
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> shardHomework)
                .execute();

        String ck_cg = ShardHomework.ck_clazzGroupId(shardHomework.getClazzGroupId());
        ShardHomework.Location modify = shardHomework.toLocation();
        ChangeCacheObject<List<ShardHomework.Location>> modifier = locations -> {
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
        CacheValueModifierExecutor<List<ShardHomework.Location>> executor = getCache().createCacheValueModifier();
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

        IMongoConnection connection = createMongoConnection(calculateIdMongoNamespace(homeworkId), homeworkId);
        ShardHomework modified = $executeFindOneAndUpdate(connection, criteria, update, options).getUninterruptibly();

        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(ShardHomework.ck_id(homeworkId))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();

            String ck_cg = ShardHomework.ck_clazzGroupId(modified.getClazzGroupId());
            getCache().delete(ck_cg);
        }
        return modified != null;
    }

    /**
     * 查询作业数量
     * 定时任务专用 通过从库查询
     */
    public Long getHomeworkCount(Date start, Date end) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        int startYear = calendar.get(Calendar.YEAR);
        calendar.setTime(end);
        int endYear = calendar.get(Calendar.YEAR);
        long count = 0;
        for (int year = startYear; year <= endYear; year++) {
            long oneYearCount = SafeConverter.toLong(internalGetHomeworkCount(start, end, year));
            count += oneYearCount;
        }
        return count;
    }

    private Long internalGetHomeworkCount(Date start, Date end, int year) {
        Criteria criteria = Criteria.where("createAt").gte(start).lte(end);
        Query query = Query.query(criteria);
        String databaseName = getMongoDocument().getDatabaseName();
        String collectionName = calculateCollectionByYear(year);
        MongoNamespace mongoNamespace = new MongoNamespace(databaseName, collectionName);
        return $executeCount(createMongoConnection(mongoNamespace), query, ReadPreference.secondaryPreferred(), null).getUninterruptibly();
    }
}
