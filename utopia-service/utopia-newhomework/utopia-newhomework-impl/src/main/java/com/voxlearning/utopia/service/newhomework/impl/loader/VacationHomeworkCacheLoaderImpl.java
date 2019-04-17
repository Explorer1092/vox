package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkCacheLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkDetailCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkWinterPlanCacheMapper;
import com.voxlearning.utopia.service.newhomework.consumer.cache.VacationHomeworkCacheManager;
import com.voxlearning.utopia.service.newhomework.consumer.cache.VacationHomeworkWinterPlanCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.question.api.entity.WinterDayPlan;
import com.voxlearning.utopia.service.question.api.entity.WinterPlan;
import com.voxlearning.utopia.service.question.api.entity.WinterWeekPlan;
import com.voxlearning.utopia.service.question.consumer.WinterPlanLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/12/1
 */
@Named
@Service(interfaceClass = VacationHomeworkCacheLoader.class)
@ExposeService(interfaceClass = VacationHomeworkCacheLoader.class)
public class VacationHomeworkCacheLoaderImpl extends SpringContainerSupport implements VacationHomeworkCacheLoader {

    @Inject private RaikouSDK raikouSDK;

    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private VacationHomeworkPackageDao vacationHomeworkPackageDao;
    @Inject private VacationHomeworkDao vacationHomeworkDao;
    @Inject private VacationHomeworkResultDao vacationHomeworkResultDao;
    @Inject private WinterPlanLoaderClient winterPlanLoaderClient;

    @Override
    public VacationHomeworkCacheMapper addOrModifyVacationHomeworkCacheMapper(VacationHomework vacationHomework) {
        if (vacationHomework == null || vacationHomework.getClazzGroupId() == null || vacationHomework.getStudentId() == null) {
            return null;
        }

        VacationHomeworkCacheManager manager = newHomeworkCacheService.getVacationHomeworkCacheManager();
        Long clazzGroupId = vacationHomework.getClazzGroupId();
        Long studentId = vacationHomework.getStudentId();
        String key = manager.getCacheKey(clazzGroupId, studentId);

        VacationHomeworkCacheMapper mapper = manager.load(key);
        if (mapper == null) {
            // 先写数据库成功之后才会操作缓存，所以这一步拿到的是全量数据
            mapper = $init(clazzGroupId, studentId);
            return mapper;
        }

        String resultId = new VacationHomeworkResult.ID(vacationHomework.getPackageId(), vacationHomework.getWeekRank(), vacationHomework.getDayRank(), vacationHomework.getStudentId()).toString();
        VacationHomeworkResult homeworkResult = vacationHomeworkResultDao.load(resultId);
        LinkedHashMap<String, VacationHomeworkDetailCacheMapper> detailMapper = mapper.getHomeworkDetail();

        // 就不校验mapper中是否已经存在这个作业了，直接替换
        VacationHomeworkDetailCacheMapper detail = new VacationHomeworkDetailCacheMapper();
        detail.setWeekRank(vacationHomework.getWeekRank());
        detail.setDayRank(vacationHomework.getDayRank());

        if (StringUtils.isNoneBlank(resultId) && homeworkResult != null) {
            if (homeworkResult.isFinished()) {
                detail.setDuration(homeworkResult.processDuration());
                detail.setAvgScore(homeworkResult.processScore());
                detail.setFinishAt(homeworkResult.getFinishAt());
            }
        }
        detailMapper.put(vacationHomework.getId(), detail);

        // 校验是否已经全部完成
//        VacationHomeworkWinterPlanCacheMapper planCacheMapper = loadVacationHomeworkWinterPlanCacheMapper(mapper.getBookId());
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(vacationHomework.getPackageId());
        if (vacationHomeworkPackage != null && vacationHomeworkPackage.getPlannedDays() != null) {
            // 本次作业一共有多少个包
            int totalPackageCount = vacationHomeworkPackage.getPlannedDays();
            if (MapUtils.isNotEmpty(mapper.getHomeworkDetail())) {
                List<VacationHomeworkDetailCacheMapper> detailCacheMappers = mapper.getHomeworkDetail().values()
                        .stream()
                        .filter(VacationHomeworkDetailCacheMapper::isFinished)
                        .sorted(Comparator.comparingLong(o -> o.getFinishAt().getTime()))
                        .collect(Collectors.toList());
                mapper.setFinishPackageCount(detailCacheMappers.size());
                if (CollectionUtils.isNotEmpty(detailCacheMappers) && detailCacheMappers.size() == totalPackageCount) {
                    mapper.setFinishAt(detailCacheMappers.get(detailCacheMappers.size() - 1).getFinishAt());
                }
            }
        }

        if (!manager.set(key, mapper)) {
            // 保存失败，清除缓存
            manager.evict(key);
            return null;
        }
        return mapper;
    }

    @Override
    public void removeVacationHomeworkCacheMapper(Long clazzGroupId, Long studentId) {
        if (clazzGroupId == null || studentId == null) {
            return;
        }
        VacationHomeworkCacheManager manager = newHomeworkCacheService.getVacationHomeworkCacheManager();
        String key = manager.getCacheKey(clazzGroupId, studentId);
        manager.evict(key);
    }

    @Override
    public void removeVacationHomeworkCacheMapper(Long clazzGroupId) {
        if (clazzGroupId == null) {
            return;
        }
        Set<Long> studentIds = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupId(clazzGroupId)
                .stream()
                .filter(o -> o.getStudentId() != null)
                .map(GroupStudentTuple::getStudentId)
                .collect(Collectors.toSet());

        for (Long studentId : studentIds) {
            removeVacationHomeworkCacheMapper(clazzGroupId, studentId);
        }
    }

    @Override
    public VacationHomeworkCacheMapper loadVacationHomeworkCacheMapper(Long clazzGroupId, Long studentId) {
        if (clazzGroupId == null || studentId == null) {
            return null;
        }

        VacationHomeworkCacheManager manager = newHomeworkCacheService.getVacationHomeworkCacheManager();
        String key = manager.getCacheKey(clazzGroupId, studentId);

        VacationHomeworkCacheMapper mapper = manager.load(key);
        if (mapper == null) {
            return $init(clazzGroupId, studentId);
        }

        return mapper;
    }

    @Override
    public List<VacationHomeworkCacheMapper> loadVacationHomeworkCacheMappers(Long clazzGroupId) {
        if (clazzGroupId == null) {
            return Collections.emptyList();
        }
        Set<Long> studentIds = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupId(clazzGroupId)
                .stream()
                .filter(o -> o.getStudentId() != null)
                .map(GroupStudentTuple::getStudentId)
                .collect(Collectors.toSet());

        Map<Long, List<VacationHomeworkPackage.Location>> packageMap = vacationHomeworkPackageDao.loadVacationHomeworkPackageByClazzGroupIds(Collections.singleton(clazzGroupId));
        if (CollectionUtils.isEmpty(studentIds) || MapUtils.isEmpty(packageMap) || CollectionUtils.isEmpty(packageMap.get(clazzGroupId))) {
            return Collections.emptyList();
        }

        List<VacationHomeworkCacheMapper> result = new ArrayList<>();
        studentIds.forEach(studentId -> {
            VacationHomeworkCacheMapper mapper = loadVacationHomeworkCacheMapper(clazzGroupId, studentId);
            if (mapper != null) {
                result.add(mapper);
            }
        });

        return result;
    }

    /**
     * 从db里面取数据
     */
    private VacationHomeworkCacheMapper $init(Long clazzGroupId, Long studentId) {

        List<VacationHomeworkPackage.Location> packageList = vacationHomeworkPackageDao.loadVacationHomeworkPackageByClazzGroupIds(Collections.singleton(clazzGroupId)).get(clazzGroupId);
        if (CollectionUtils.isEmpty(packageList) || packageList.get(0) == null) {
            return null;
        }
        // 同一时间只能有1个有效的package存在
        VacationHomeworkPackage.Location packageLocation = packageList.get(0);
        Map<String, List<VacationHomework.Location>> homeworkLocationMap = vacationHomeworkDao.loadVacationHomeworkByPackageIds(Collections.singleton(packageLocation.getId()));
        if (MapUtils.isEmpty(homeworkLocationMap) || homeworkLocationMap.get(packageLocation.getId()) == null) {
            return null;
        }
        List<VacationHomework.Location> locations = homeworkLocationMap.get(packageLocation.getId());
        List<VacationHomework.Location> studentLocations = locations.stream()
                .filter(o -> Objects.equals(o.getStudentId(), studentId))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(studentLocations)) {
            return null;
        }

        List<String> resultIds = new ArrayList<>();
        studentLocations.forEach(location -> {
            String resultId = new VacationHomeworkResult.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), location.getStudentId()).toString();
            resultIds.add(resultId);
        });

        Map<String, VacationHomeworkResult> resultMap = vacationHomeworkResultDao.loads(resultIds);

        // 拼数据
        VacationHomeworkCacheMapper mapper = new VacationHomeworkCacheMapper();
        mapper.setClazzGroupId(clazzGroupId);
        mapper.setStudentId(studentId);
        mapper.setType(NewHomeworkType.WinterVacation);

        mapper.setPackageId(packageLocation.getId());
        mapper.setBookId(packageLocation.getBookId());
        mapper.setSubject(packageLocation.getSubject());
        mapper.setTeacherId(packageLocation.getTeacherId());

        // 如果寒假作业已经全部做完，赋一个最后做完的时间
        Date lastFinishedAt = getVacationHomeworkAllFinishedAt(packageLocation, resultMap);
        mapper.setFinishAt(lastFinishedAt);

        LinkedHashMap<String, VacationHomeworkDetailCacheMapper> detailMap = new LinkedHashMap<>();

        studentLocations.sort(Comparator.comparingLong(VacationHomework.Location::getCreateTime));
        int finishPackageCount = 0;
        for (VacationHomework.Location location : studentLocations) {
            VacationHomeworkDetailCacheMapper detail = new VacationHomeworkDetailCacheMapper();
            String rid = new VacationHomeworkResult.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), location.getStudentId()).toString();
            VacationHomeworkResult homeworkResult = resultMap.get(rid);

            if (StringUtils.isNoneBlank(rid) && homeworkResult != null) {
                detail.setWeekRank(location.getWeekRank());
                detail.setDayRank(location.getDayRank());
                if (homeworkResult.isFinished()) {
                    detail.setDuration(homeworkResult.processDuration());
                    detail.setAvgScore(homeworkResult.processScore());
                    detail.setFinishAt(homeworkResult.getFinishAt());
                    finishPackageCount++;
                }
                detailMap.put(location.getId(), detail);
            }
        }
        mapper.setFinishPackageCount(finishPackageCount);
        // 还没有做完的作业的时候也会有值
        mapper.setHomeworkDetail(detailMap);

        // 将数据扔进缓存
        VacationHomeworkCacheManager manager = newHomeworkCacheService.getVacationHomeworkCacheManager();
        String key = manager.getCacheKey(clazzGroupId, studentId);
        if (manager.set(key, mapper)) {
            return mapper;
        } else {
            return null;
        }
    }

    @Override
    public VacationHomeworkWinterPlanCacheMapper loadVacationHomeworkWinterPlanCacheMapper(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return null;
        }
        VacationHomeworkWinterPlanCacheManager manager = newHomeworkCacheService.getVacationHomeworkWinterPlanCacheManager();
        String key = manager.getCacheKey(bookId);
        VacationHomeworkWinterPlanCacheMapper mapper = null;
        try {
            mapper = manager.load(key);
        } catch (Exception e) {
            logger.error("key is {} ", key);
            logger.error(e.getMessage());
        }
        if (mapper == null) {
            // 先写数据库成功之后才会操作缓存
            mapper = initWinterPlan(bookId);
            return mapper;
        }
        return mapper;
    }


    /**
     * 获取假期作业是否全部完成，并返回最后一个完成的时间
     */
    private Date getVacationHomeworkAllFinishedAt(VacationHomeworkPackage.Location packageLocation, Map<String, VacationHomeworkResult> resultMap) {
        // 如果寒假作业已经全部做完，赋一个最后做完的时间
        if (packageLocation != null && packageLocation.getPlannedDays() != null) {
            // 本次作业一共有多少个包
            int totalPackageCount = packageLocation.getPlannedDays();
            if (MapUtils.isNotEmpty(resultMap)) {
                List<VacationHomeworkResult> finishedResult = resultMap.values()
                        .stream()
                        .filter(BaseHomeworkResult::isFinished)
                        .sorted(Comparator.comparingLong(o -> o.getFinishAt().getTime()))
                        .collect(Collectors.toList());
                // 本次寒假作业一共已经完成的包数
                int alreadyFinishedCount = finishedResult.size();
                // 用于校验本次寒假作业是否全部完成
                if (alreadyFinishedCount != 0 && totalPackageCount == alreadyFinishedCount) {
                    // 获取最后一个作业完成的时间
                    Date lastFinishedAt = finishedResult.get(alreadyFinishedCount - 1).getFinishAt();
                    if (lastFinishedAt != null) {
                        return lastFinishedAt;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 从db里面取数据
     */
    private VacationHomeworkWinterPlanCacheMapper initWinterPlan(String bookId) {
        WinterPlan winterPlan = winterPlanLoaderClient.loadByBookId(bookId);
        if (winterPlan == null) {
            return null;
        }

        VacationHomeworkWinterPlanCacheMapper winterPlanCacheMapper = new VacationHomeworkWinterPlanCacheMapper();
        winterPlanCacheMapper.setBookId(bookId);
        winterPlanCacheMapper.setId(winterPlan.getId());
        LinkedHashMap<String, WinterWeekPlan> weekPlans = new LinkedHashMap<>();
        LinkedHashMap<String, WinterDayPlan> dayPlans = new LinkedHashMap<>();
        LinkedHashMap<String, List<String>> weekPlanDays = new LinkedHashMap<>();
        for (WinterWeekPlan weekPlan : winterPlan.getWeekPlans()) {
            String weekRank = SafeConverter.toString(weekPlan.getWeekRank());
            if (StringUtils.isBlank(weekRank)) {
                continue;
            }
            List<String> planDays = new ArrayList<>();
            for (WinterDayPlan winterDayPlan : weekPlan.getDayPlans()) {
                String dayRank = SafeConverter.toString(winterDayPlan.getDayRank());
                if (StringUtils.isBlank(dayRank)) {
                    continue;
                }
                String weekDayRankKey = StringUtils.join(Arrays.asList(weekPlan.getWeekRank(), winterDayPlan.getDayRank()), "-");
                dayPlans.put(weekDayRankKey, winterDayPlan);
                planDays.add(dayRank);
            }
            weekPlan.setDayPlans(Collections.emptyList());
            weekPlans.put(weekRank, weekPlan);
            weekPlanDays.put(weekRank, planDays);
        }
        winterPlanCacheMapper.setWeekPlan(weekPlans);
        winterPlanCacheMapper.setWeekPlanDays(weekPlanDays);
        winterPlanCacheMapper.setDayPlan(dayPlans);

        // 将数据扔进缓存
        VacationHomeworkWinterPlanCacheManager manager = newHomeworkCacheService.getVacationHomeworkWinterPlanCacheManager();
        String key = manager.getCacheKey(bookId);
        if (manager.set(key, winterPlanCacheMapper)) {
            return winterPlanCacheMapper;
        } else {
            return null;
        }
    }
}
