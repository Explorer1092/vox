package com.voxlearning.utopia.service.business.impl.loader;

import com.lambdaworks.redis.api.async.RedisSortedSetAsyncCommands;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.CallbackEvent;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.Maps;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.alps.spi.cache.ExternalLoader;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.business.api.constant.TeachingResourceUserType;
import com.voxlearning.utopia.business.api.util.TeachingResourceUtils;
import com.voxlearning.utopia.mapper.TeachingResourceStatistics;
import com.voxlearning.utopia.service.business.api.TeachingResourceLoader;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.entity.TeachingResourceCollect;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceRaw;
import com.voxlearning.utopia.service.business.buffer.TeachingResourceBuffer;
import com.voxlearning.utopia.service.business.buffer.internal.JVMTeachingResourceBuffer;
import com.voxlearning.utopia.service.business.buffer.mapper.TeachingResourceList;
import com.voxlearning.utopia.service.business.impl.dao.TeacherResourceTaskDao;
import com.voxlearning.utopia.service.business.impl.dao.TeachingResourceCollectDao;
import com.voxlearning.utopia.service.business.impl.dao.TeachingResourceDao;
import com.voxlearning.utopia.service.business.impl.dao.TeachingResourceVersion;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.campaign.api.mapper.YiqiJTCourseMapper;
import com.voxlearning.utopia.service.campaign.client.YiqiJTServiceClient;
import com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import net.sf.cglib.beans.BeanCopier;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.lang.convert.SafeConverter.toBoolean;
import static com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask.Status.EXPIRED;
import static com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask.Status.FINISH;
import static com.voxlearning.utopia.service.campaign.api.TeacherActivityService.YQJT_ALL_RAW_CACHE_KEY;

/**
 * Created by haitian.gan on 2017/8/1.
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = TeachingResourceLoader.class, version = @ServiceVersion(version = "1.8")),
        @ExposeService(interfaceClass = TeachingResourceLoader.class, version = @ServiceVersion(version = "1.7")),
        @ExposeService(interfaceClass = TeachingResourceLoader.class, version = @ServiceVersion(version = "1.6")),
})
public class TeachingResourceLoaderImpl implements TeachingResourceLoader, InitializingBean, TeachingResourceBuffer.Aware {

    private static final Logger logger = LoggerFactory.getLogger(TeachingResourceLoader.class);

    @Inject private TeachingResourceDao teachingResourceDao;
    @Inject private TeacherResourceTaskDao teacherResourceTaskDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient1;
    @Inject private BusinessCacheSystem cacheSystem;
    @Inject private TeachingResourceCollectDao teachingResourceCollectDao;
    @Inject private YiqiJTServiceClient yiqiJTServiceClient;
    @Inject private TeachingResourceVersion teachingResourceVersion;

    @Inject private RaikouSDK raikouSDK;

    // private IRedisCommands redisCommands;
    private static final String KEY_PREFIX = "teacherTask:";
    private static final String TEACHING_RESOURCE_HOT_SEARCH_RANK = "TEACHING_RESOURCE_HOT_SEARCH_RANK";

    private IRedisCommands redisCommands;
    private RedisSortedSetAsyncCommands<String, Object> redisSortedSetAsyncCommands;

    @Override
    public TeachingResourceStatistics loadResourceStatistics(String id) {
        TeachingResourceStatistics statistics = new TeachingResourceStatistics();
        TeachingResource resource = loadResource(id);

        statistics.setId(resource.getId());
        statistics.setCollectCount(resource.getCollectCount());
        statistics.setReadCount(resource.getReadCount());
        statistics.setFinishNum(resource.getFinishNum());
        statistics.setParticipateNum(resource.getParticipateNum());
        return statistics;
    }

    @Override
    public TeachingResource loadResource(String id) {
        return teachingResourceDao.load(id);
    }

    @Override
    @Deprecated
    public List<TeachingResource> loadAllResources() {
        return teachingResourceDao.loadAll()
                .stream()
                .peek(tr -> {
                    tr.setReadCount(SafeConverter.toLong(tr.getReadCount()));
                    tr.setCollectCount(SafeConverter.toLong(tr.getCollectCount()));
                    tr.setFinishNum(SafeConverter.toLong(tr.getFinishNum()));
                    tr.setParticipateNum(SafeConverter.toLong(tr.getParticipateNum()));
                    if (tr.getSource() == null) {
                        tr.setSource(TeachingResource.SOURCE_PLATFORM);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TeachingResourceRaw> loadAllResourcesRaw() {
        BeanCopier beanCopier = BeanCopier.create(TeachingResource.class, TeachingResourceRaw.class, false);
        return teachingResourceDao.loadAll()
                .stream()
                .map(tr -> {
                    TeachingResourceRaw trRaw = new TeachingResourceRaw();
                    beanCopier.copy(tr, trRaw, null);
                    return trRaw;
                })
                .peek(tr -> {
                    tr.setReadCount(SafeConverter.toLong(tr.getReadCount()));
                    tr.setCollectCount(SafeConverter.toLong(tr.getCollectCount()));
                    tr.setFinishNum(SafeConverter.toLong(tr.getFinishNum()));
                    tr.setParticipateNum(SafeConverter.toLong(tr.getParticipateNum()));
                    if (tr.getSource() == null) {
                        tr.setSource(TeachingResource.SOURCE_PLATFORM);
                    }
                    tr.setFileType(getFileExtensionName(tr.getFileUrl()));
                    tr.setIsCourse(false);
                })
                .collect(Collectors.toList());
    }

    @Override
    public MapMessage loadHomePageChoicestResources(Long teacherId) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (teacherId == null) {
            return MapMessage.errorMessage();
        }

        try {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (teacher == null) {
                return MapMessage.errorMessage();
            }

            List<TeachingResourceRaw> featuredList = loadOnlineResources(teacher)
                    .stream()
                    .filter(t -> Objects.equals(t.getSource(), TeachingResource.SOURCE_PLATFORM))
                    .filter(t -> toBoolean(t.getFeaturing()))
                    .filter(t ->
                            t.getVisitLimited() == null || t.getVisitLimited() == TeachingResourceUserType.All ||
                                    (t.getVisitLimited() == TeachingResourceUserType.Unauthorized && teacher.fetchCertificationState() != AuthenticationState.SUCCESS) ||
                                    (t.getVisitLimited() == TeachingResourceUserType.Authorized && teacher.fetchCertificationState() == AuthenticationState.SUCCESS)
                    )
                    .sorted((t1, t2) -> t2.getOnlineAt().compareTo(t1.getOnlineAt())) // 按上线时间倒序
                    .collect(Collectors.toList());

            PageRequest pageable = new PageRequest(0, 20);
            List<TeachingResourceRaw> content = new ArrayList<>(PageableUtils.listToPage(featuredList, pageable).getContent());
            Collections.shuffle(content);
            if (content.size() > 4) {
                content = content.subList(0, 4);
            }

            fillReadCollectCount(content);

            for (TeachingResourceRaw raw : content) {
                boolean isTask = Objects.equals(raw.getCategory(), TeachingResource.Category.WEEK_WELFARE.name());
                boolean isXJT = Objects.equals(raw.getCategory(), TeachingResourceUtils.YIQI_JIANGTANG);

                String name = raw.getName();
                String subTitle = isXJT ? raw.getLecturerUserName() + " " + raw.getLecturerIntroduction() : raw.getSubHead();
                Long pageView = raw.getReadCount();
                String type = isTask ? "TASK" : "READING";
                Long collect = raw.getCollectCount();
                Long attendNum = raw.getParticipateNum();
                Long finishNum = raw.getFinishNum();
                String coverUrl = StringUtils.isEmpty(raw.getAppImage()) ? raw.getImage() : raw.getAppImage();
                String openUrl = isXJT ? "/view/mobile/teacher/activity/newforum/detail?forum_index=" + raw.getId()
                        :
                        "/view/mobile/teacher/teaching_assistant/resourcedetail?resourceId=" + raw.getId() + "&category=" + raw.getCategory();

                Map<String, Object> item = Maps.m("name", name,
                        "subTitle", subTitle,
                        "pageView", pageView,
                        "type", type,
                        "collect", collect,
                        "attendNum", attendNum,
                        "finishNum", finishNum,
                        "coverUrl", coverUrl,
                        "openUrl", openUrl
                );
                result.add(item);
            }
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }

        return MapMessage.successMessage()
                .add("result", result)
                .add("moreUrl", "view/mobile/teacher/teaching_assistant/index")
                .add("tabName", "精选教学");
    }

    @Override
    public List<TeacherResourceTask> loadTeacherTasks(Long userId) {
        final Date now = new Date();
        return teacherResourceTaskDao.loadTeacherTasks(userId)
                .stream()
                .map(t -> {
                    // 免费任务永远不会过期，不用设置过期状态
                    // 已完成的任务，也不用置成过期状态
                    // 已过期的，直接返回，不要浪费再修改一次
                    if (Objects.equals(t.getTask(), TeachingResourceTask.FREE.name())
                            || Objects.equals(t.getStatus(), FINISH.name())
                            || Objects.equals(t.getStatus(), EXPIRED.name())
                            || t.getExpiryDate() == null
                            || t.getExpiryDate().after(now)) {
                        return t;
                    } else {
                        teacherResourceTaskDao.updateTaskStatus(t.getId(), EXPIRED.name());
                        t.setStatus(EXPIRED.name());
                        return t;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherResourceTask> loadTasksByStatus(String status) {
        //return teacherResourceTaskDao.loadAllTasks(status);
        return Collections.emptyList();
    }

    @Override
    public List<String> testForRedis(Long teacherId) {
        return null;
    }

    @Override
    public List<Map<String, Object>> loadTaskHomeworkDetail(Long teacherId) {
        String allHomeworkKey = buildKey(teacherId, "homeworks");
        Cache cache = cacheSystem.CBS.persistence;

        //RedisSetCommands<String,Object> setCommands = redisCommands.sync().getRedisSetCommands();
        //RedisHashCommands<String,Object> hashCommands = redisCommands.sync().getRedisHashCommands();

        return Optional.ofNullable(cache.<Set<String>>load(allHomeworkKey))
                .orElse(Collections.emptySet())
                .stream()
                .map(SafeConverter::toString)
                .map(h -> {
                    Map<String, Object> info = cache.load(h);
                    if (info == null || info.isEmpty())
                        return null;

                    Date createTime = new Date(SafeConverter.toLong(info.get("createTime")));
                    info.put("createTimeStr", DateUtils.dateToString(createTime));
                    info.put("homeworkIdKey", h);

                    return info;
                })
                .filter(Objects::nonNull)
                .sorted((h1, h2) -> MapUtils.getLong(h2, "createTime").compareTo(MapUtils.getLong(h1, "createTime")))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> loadTaskProgress(Long teacherId, String resourceId) {
        try {
            final TeacherResourceTask task = loadTeacherTask(teacherId, resourceId);
            Validate.notNull(task, "未查询到对应任务!");

            final TeachingResourceTask taskDict = TeachingResourceTask.parse(task.getTask());
            if (StringUtils.isBlank(taskDict.getConditionParam())) {
                Map<String, Object> progressMap = new HashMap<>();
                progressMap.put("taskInfo", task);
                progressMap.put("taskDesc", taskDict.getDesc());
                progressMap.put("createTime", DateUtils.dateToString(task.getCreateAt(), "MM-dd HH:mm"));
                return progressMap;
            }

            final Map<Long, String> groupNameMap = new HashMap<>();
            final Map<Long, Integer> groupStuNumMap = new HashMap<>();
            final Map<Long, String> groupSubjectMap = new HashMap<>();

            List<Long> allTeacherIds = new ArrayList<>();
            List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(teacherId);

            allTeacherIds.add(teacherId);
            allTeacherIds.addAll(subTeacherIds);

            // 遍历所有组，生成组-名字，组-人数，组-学科的对照
            // fix 埋了很久的包班制的bug
            teacherLoaderClient.loadTeachersClazzIds(allTeacherIds)
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(cId -> raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(cId))
                    .filter(Objects::nonNull)
                    // 过滤掉毕业班
                    .filter(clazz -> !clazz.isTerminalClazz())
                    .forEach(clazz -> groupLoaderClient1.loadClazzGroups(clazz.getId()).forEach(g -> {
                        groupNameMap.put(g.getId(), clazz.formalizeClazzName());
                        groupSubjectMap.put(g.getId(), g.getSubject().getValue());

                        int stuNum = raikouSDK.getClazzClient()
                                .getGroupStudentTupleServiceClient()
                                .findByGroupId(g.getId())
                                .size();
                        groupStuNumMap.put(g.getId(), stuNum);
                    }));

            String[] conditionParams = taskDict.getConditionParam().split(",");
            int minFinishNum = SafeConverter.toInt(conditionParams[1]);
            double finishRate = SafeConverter.toDouble(conditionParams[2]);
            int assignNum = SafeConverter.toInt(conditionParams[0]);

            Map<String, List<Map<String, Object>>> hwDetails = new HashMap<>();
            for (Long tId : allTeacherIds) {
                // 把作业按照班组分组
                Map<String, List<Map<String, Object>>> _hwDetails = loadTaskHomeworkDetail(tId)
                        .stream()
                        // 过滤掉不满足条件的group下面的作业
                        .filter(detail -> groupNameMap.containsKey(MapUtils.getLong(detail, "groupId")))
                        // 筛选在任务有效期内的作业
                        .filter(detail -> {
                            Long ct = MapUtils.getLong(detail, "createTime");
                            return ct > task.getCreateAt().getTime()
                                    && ct < task.getExpiryDate().getTime();
                        })
                        // 按照groupId + 学科 分组，以班组名字命名
                        .peek(detail -> {
                            Long groupId = MapUtils.getLong(detail, "groupId");
                            detail.put("className", groupNameMap.get(groupId));
                            detail.put("subject", groupSubjectMap.get(groupId));

                            int stuNum = groupStuNumMap.get(groupId);
                            detail.put("stuNum", stuNum);

                            // 修正后的完成人数标准线
                            int fixedFinishLine = (int) Math.floor(finishRate * stuNum);

                            // 判断是否完成
                            int finishNum = MapUtils.getInteger(detail, "finishNum");
                            if (finishNum >= minFinishNum && fixedFinishLine <= finishNum) {
                                detail.put("finish", true);
                            } else
                                detail.put("finish", false);

                            // 计算完成率
                            BigDecimal multiply = BigDecimal.valueOf(finishNum).divide(BigDecimal.valueOf(stuNum), 2, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(100));
                            detail.put("completeRate", multiply.setScale(2, BigDecimal.ROUND_HALF_UP));
                        })
                        .collect(Collectors.groupingBy(
                                d -> MapUtils.getString(d, "className") + "_" + MapUtils.getString(d, "subject")
                        ));

                hwDetails.putAll(_hwDetails);
            }

            Map<String, Object> progressMap = new HashMap<>();
            progressMap.put("taskInfo", task);
            progressMap.put("taskDesc", taskDict.getDesc());
            progressMap.put("details", hwDetails);
            progressMap.put("createTime", DateUtils.dateToString(task.getCreateAt(), "MM-dd HH:mm"));
            progressMap.put("assignNum", assignNum);

            return progressMap;
        } catch (Exception e) {
            logger.error("LoadTaskProgress error!teacherId:{},resourceId:{}", teacherId, resourceId);
            return null;
        }
    }

    @Override
    public List<TeachingResourceCollect> loadCollectByUserId(Long userId) {
        return teachingResourceCollectDao.loadAllByUser(userId);
    }

    @Override
    public List<String> getHotWord() {
        try {
            List<Object> objects = redisSortedSetAsyncCommands.zrevrange(TEACHING_RESOURCE_HOT_SEARCH_RANK, 0, 9).get();
            return objects.stream().map(i -> (String) i).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<TeachingResourceRaw> getYQJTRaws() {
        String KEY_ALL = "ALL"; // 假的,只是为了用缓存框架虚拟出来的

        ExternalLoader<String, List<YiqiJTCourseMapper>> externalLoader = new ExternalLoader<String, List<YiqiJTCourseMapper>>() {
            @Override
            public Map<String, List<YiqiJTCourseMapper>> loadFromExternal(Collection<String> collection) {
                Map<String, List<YiqiJTCourseMapper>> result = new HashMap<>();
                List<YiqiJTCourseMapper> yiqiJTCourseMappers = yiqiJTServiceClient.load17JTCourseMapperAll();
                result.put(KEY_ALL, yiqiJTCourseMappers);
                return result;
            }
        };

        // 不过期, 后台有编辑时删除缓存 (子表太多, 覆盖更新太难, 读多写少) 增加阅读量和收藏采用覆盖更新
        List<YiqiJTCourseMapper> YiqiJTCourseMapperRaws = cacheSystem.CBS.flushable
                .<String, List<YiqiJTCourseMapper>>createCacheValueLoader()
                .keyGenerator(s -> YQJT_ALL_RAW_CACHE_KEY)
                .keys(Collections.singleton(KEY_ALL))
                .loads()
                .externalLoader(externalLoader)
                .loadsMissed()
                .write()
                .getAndResortResult()
                .get(KEY_ALL);

        //把 17讲堂 转化成 TeachingResourceRaw 方便统一处理
        List<TeachingResourceRaw> resourceRaws = YiqiJTCourseMapperRaws.stream().map(i -> {
            TeachingResourceRaw raw = new TeachingResourceRaw();
            raw.setId(String.valueOf(i.getId()));
            raw.setName(i.getTitle());
            raw.setSubject(TeachingResourceUtils.convertSubject(i.getSubjectList()));
            if (StringUtils.isEmpty(i.getCategory())) {
                raw.setCategory(TeachingResourceUtils.YIQI_JIANGTANG);
            } else {
                raw.setCategory(i.getCategory());
            }
            raw.setGrade(StringUtils.join(new LinkedHashSet<>(i.getGradeList()), ",")); // 有重复的快去重
            raw.setImage(i.getTitlePictureUrl());
            raw.setAppImage(i.getAppPictureUrl());
            raw.setFeaturing(i.getFeaturing());
            raw.setDisplayOrder(i.getTopNum());
            raw.setOnline(i.getStatus() == 1); // 1 是上线 2是下线
            //raw.setValidityPeriod();
            //raw.setFileUrl();
            raw.setSubHead("");
            raw.setVisitLimited(TeachingResourceUserType.All);
            raw.setReceiveLimited(TeachingResourceUserType.All);
            raw.setCreateAt(i.getCreateDatetime());
            raw.setUpdateAt(i.getUpdateDatetime());
            raw.setOnlineAt(i.getCreateDatetime()); // 没有上线时间先取创建时间
            raw.setWorkType(TeachingResource.WorkType.无);
            raw.setLabel(TeachingResource.Label.parse(i.getLabel()));
            raw.setReadCount(Objects.equals(i.getPrice(), 0L) ? SafeConverter.toLong(i.getReadCount()) : SafeConverter.toLong(i.getAttendNum()));
            raw.setCollectCount(SafeConverter.toLong(i.getCollectCount()));
            raw.setLecturerUserName(i.getLecturerUserName());
            raw.setLecturerIntroduction(i.getLecturerIntroduction());
            raw.setSource(i.getSource());
            raw.setIsCourse(true);
            return raw;
        }).collect(Collectors.toList());

        return resourceRaws;
    }

    private final LazyInitializationSupplier<JVMTeachingResourceBuffer> rewardActivityBufferSupplier = new LazyInitializationSupplier<>(() -> {
        TeachingResourceList data = new TeachingResourceList();
        data.setVersion(teachingResourceVersion.current());

        List<TeachingResource> teachingResourceList = loadAllResources();
        fillBufferData(data, teachingResourceList);

        JVMTeachingResourceBuffer buffer = new JVMTeachingResourceBuffer();
        buffer.attach(data);
        return buffer;
    });

    private void fillBufferData(TeachingResourceList data, List<TeachingResource> teachingResourceList) {
        BeanCopier beanCopier = BeanCopier.create(TeachingResource.class, TeachingResourceRaw.class, false);
        List<TeachingResourceRaw> teachingResourceRawList = teachingResourceList.stream()
                .map(tr -> {
                    TeachingResourceRaw trRaw = new TeachingResourceRaw();
                    beanCopier.copy(tr, trRaw, null);
                    return trRaw;
                })
                .peek(tr -> {
                    tr.setReadCount(SafeConverter.toLong(tr.getReadCount()));
                    tr.setCollectCount(SafeConverter.toLong(tr.getCollectCount()));
                    tr.setFinishNum(SafeConverter.toLong(tr.getFinishNum()));
                    tr.setParticipateNum(SafeConverter.toLong(tr.getParticipateNum()));
                    tr.setFileType(getFileExtensionName(tr.getFileUrl()));
                    tr.setIsCourse(false);
                })
                .collect(Collectors.toList());

        data.setTeachingResourceList(teachingResourceList);
        data.setTeachingResourceRawList(teachingResourceRawList);
    }

    @Override
    public void reloadTeachingResourceList() {
        long actualVersion = teachingResourceVersion.current();
        TeachingResourceBuffer buffer = getTeachingResourceBuffer();
        long bufferVersion = buffer.getVersion();
        if (bufferVersion != actualVersion) {
            TeachingResourceList data = new TeachingResourceList();
            data.setVersion(actualVersion);

            List<TeachingResource> teachingResourceList = loadAllResources();
            fillBufferData(data, teachingResourceList);

            buffer.attach(data);
            logger.info("[TeachingResourceBuffer] reloaded: [{}] -> [{}]", bufferVersion, actualVersion);
        }
    }

    @Override
    public TeachingResourceList loadTeachingResourceList(long version) {
        FlightRecorder.closeLog();
        TeachingResourceList data = getTeachingResourceBuffer().dump();
        return (version < data.getVersion()) ? data : null;
    }

    @Override
    public TeachingResourceBuffer getTeachingResourceBuffer() {
        return rewardActivityBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetTeachingResourceBuffer() {
        rewardActivityBufferSupplier.reset();
    }

    private List<TeachingResourceRaw> loadAllResourcesRaw(Long teacherId) {
        List<TeacherResourceTask> teacherResourceTasks = loadTeacherTasks(teacherId);
        Map<String, TeacherResourceTask> taskMap = teacherResourceTasks.stream().collect(Collectors.toMap(TeacherResourceTask::getResourceId, Function.identity(), (o1, o2) -> o1));

        //解决 loadAllResourcesRaw() 内部调用没走 AOP, 导致的缓存注解失效
//        String cacheKey = CacheKeyGenerator.generateCacheKey(TeachingResourceRaw.class, "ALL");
//        Object load = cacheSystem.CBS.flushable.load(cacheKey);
//        if (load == null) {
//            load = loadAllResourcesRaw();
//            cacheSystem.CBS.flushable.set(cacheKey, DateUtils.getCurrentToDayEndSecond(), load);
//        }
//        List<TeachingResourceRaw> resources = (List<TeachingResourceRaw>) load;
        List<TeachingResourceRaw> resources = getTeachingResourceBuffer().loadTeachingResourceRaw();
        resources = resources.stream()
                .filter(i -> TeachingResourceUtils.category.contains(i.getCategory()))
                .peek(itemRaw -> {
                    if (Objects.equals(itemRaw.getCategory(), TeachingResource.Category.WEEK_WELFARE) && teacherId != null) {
                        TeacherResourceTask task = taskMap.get(itemRaw.getId());
                        if (task != null) {
                            itemRaw.setTaskStatus(task.getStatus());
                        }
                    }
                }).collect(Collectors.toList());
        return resources;
    }

    /**
     * 获得老师所属学科(包班制是算上主副账号一起)覆盖的所有已上线资源
     *
     * @param teacher
     * @return
     */
    private List<TeachingResourceRaw> loadOnlineResources(Teacher teacher) {
        // 考虑包班制的情况，把主副账号的学科都查出来
        List<String> allSubNames = teacherLoaderClient.loadSubTeacherIds(teacher.getId())
                .stream()
                .map(tId -> teacherLoaderClient.loadTeacher(tId).getSubject())
                .filter(Objects::nonNull)
                .map(Enum::name)
                .collect(Collectors.toList());

        if (teacher.getSubject() != null)
            allSubNames.add(teacher.getSubject().name());

        // 获得老师名下所有班级的ClazzLevel列表(String)
        Set<String> clazzLvlList = getTeacherClassesLvlList(teacher.getId());

        List<TeachingResourceRaw> resources = loadAllResourcesRaw(teacher.getId());

        List<TeachingResourceRaw> yiqiJiangtang = getYQJTRaws();
        resources.addAll(yiqiJiangtang);

        return resources
                .stream()
                .filter(TeachingResourceUtils.filterCategory)
                .filter(r -> {
                    // 看资源所属学科和老师所有学科的交集
                    List<String> resourceSubNames = Arrays.asList(r.getSubject().split(","));
                    Collection<String> interSubject = CollectionUtils.intersection(allSubNames, resourceSubNames);
                    return !CollectionUtils.isEmpty(interSubject);
                })
                // 过滤年级选项，取资源和老师班级的交集
                .filter(r -> {
                    List<String> resourceGrades = Arrays.asList(r.getGrade().split(","));
                    Collection<String> interClazzLvl = CollectionUtils.intersection(resourceGrades, clazzLvlList);
                    return !CollectionUtils.isEmpty(interClazzLvl);
                })
                .filter(t -> toBoolean(t.getOnline()))
                .filter(t -> t.getOnlineAt() != null)
                /*.filter(t -> {
                    ResourceVisibleChecker checker = resourceVisibleManager.getChecker(t.getTask());
                    return checker != null && checker.check(rvcContext);
                })*/
                .collect(Collectors.toList());
    }

    private Set<String> getTeacherClassesLvlList(Long teacherId) {
        return teacherLoaderClient.loadTeacherClazzIds(teacherId)
                .stream()
                .map(cId -> raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(cId))
                .filter(Objects::nonNull)
                .filter(clazz -> !clazz.isTerminalClazz())
                .map(clazz -> clazz.getClazzLevel().getLevel())
                .map(String::valueOf)
                .collect(Collectors.toSet());
    }

    private String buildKey(Object... keyParts) {
        return KEY_PREFIX + StringUtils.join(keyParts, ":");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        this.redisCommands = builder.getRedisCommands("user-easemob");
        redisSortedSetAsyncCommands = redisCommands.async().getRedisSortedSetAsyncCommands();

        EventBus.publish(new CallbackEvent(() -> {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            TeachingResourceList data = getTeachingResourceBuffer().dump();
            logger.info("[TeachingResourceBuffer] initialized: [{}]", data.getVersion());
        })).awaitUninterruptibly();
    }

    private static String getFileExtensionName(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            fileName = fileName.trim();
            int i = fileName.lastIndexOf(".");
            String type = fileName.substring(i + 1);
            if (type.length() < 5) {
                return type;
            }
        }
        return "";
    }
}
