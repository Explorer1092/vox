package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.service.newhomework.api.BasicReviewHomeworkCacheLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewStage;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkDetailCacheMapper;
import com.voxlearning.utopia.service.newhomework.consumer.cache.BasicReviewHomeworkCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = BasicReviewHomeworkCacheLoader.class)
@ExposeService(interfaceClass = BasicReviewHomeworkCacheLoader.class)
public class BasicReviewHomeworkCacheLoaderImpl extends NewHomeworkSpringBean implements BasicReviewHomeworkCacheLoader {

    @Inject private RaikouSDK raikouSDK;

    @Override
    public BasicReviewHomeworkCacheMapper loadBasicReviewHomeworkCacheMapper(String packageId, Long studentId) {
        if (StringUtils.isBlank(packageId) || SafeConverter.toLong(studentId) <= 0L) {
            return null;
        }
        BasicReviewHomeworkCacheManager manager = newHomeworkCacheService.getBasicReviewHomeworkCacheManager();
        String key = manager.getCacheKey(studentId, packageId);
        BasicReviewHomeworkCacheMapper mapper = manager.load(key);
        if (mapper == null) {
            return $init(packageId, studentId);
        }
        return mapper;
    }

    @Override
    public Map<Long, BasicReviewHomeworkCacheMapper> loadBasicReviewHomeworkCacheMapper(String packageId, Collection<Long> studentIds) {
        Map<Long, BasicReviewHomeworkCacheMapper> result = new LinkedHashMap<>();
        for (Long sid : studentIds) {
            BasicReviewHomeworkCacheMapper mapper = loadBasicReviewHomeworkCacheMapper(packageId, sid);
            if (mapper != null) {
                result.put(sid, mapper);
            }
        }
        return result;
    }

    @Override
    public void removeBasicReviewHomeworkCacheMapper(String packageId, Long studentId) {
        if (StringUtils.isBlank(packageId)) {
            return;
        }
        if (SafeConverter.toLong(studentId) <= 0L) {
            return;
        }
        BasicReviewHomeworkCacheManager manager = newHomeworkCacheService.getBasicReviewHomeworkCacheManager();
        String key = manager.getCacheKey(studentId, packageId);
        manager.evict(key);
    }

    @Override
    public BasicReviewHomeworkCacheMapper addOrModifyBasicReviewHomeworkCacheMapper(NewHomeworkResult newHomeworkResult, String packageId, Long studentId) {
        if (newHomeworkResult == null) {
            logger.error("add Or Modify BasicReviewHomeworkCacheMapper failed : newHomeworkResult is null and packageId {},userId {}", packageId, studentId);
            return null;
        }
        if (StringUtils.isBlank(packageId)) {
            logger.error("add Or Modify BasicReviewHomeworkCacheMapper failed : packageId is blank and packageId {},userId {}", packageId, studentId);
            return null;
        }
        if (SafeConverter.toLong(studentId) <= 0L) {
            logger.error("add Or Modify BasicReviewHomeworkCacheMapper failed : studentId is error and packageId {},userId {}", packageId, studentId);
            return null;
        }
        if (!newHomeworkResult.isFinished()) {
            logger.error("add Or Modify BasicReviewHomeworkCacheMapper failed : studentId is error and packageId {},userId {}", packageId, studentId);
            return null;
        }
        BasicReviewHomeworkCacheManager manager = newHomeworkCacheService.getBasicReviewHomeworkCacheManager();
        BasicReviewHomeworkCacheMapper mapper = loadBasicReviewHomeworkCacheMapper(packageId, studentId);
        if (mapper == null) {
            logger.error("add Or Modify BasicReviewHomeworkCacheMapper failed : studentId is error and packageId {},userId {}", packageId, studentId);
            return null;
        }
        //这份作业是已经加入
        if (mapper.getHomeworkDetail().containsKey(newHomeworkResult.getHomeworkId())) {
            return mapper;
        }
        //是否存在这个作业关卡
        BasicReviewHomeworkPackage homeworkPackage = basicReviewHomeworkPackageDao.load(packageId);
        BasicReviewStage target = null;
        for (BasicReviewStage stage : homeworkPackage.getStages()) {
            if (Objects.equals(stage.getHomeworkId(), newHomeworkResult.getHomeworkId())) {
                target = stage;
                break;
            }
        }
        if (target == null) {
            logger.error("add Or Modify BasicReviewHomeworkCacheMapper failed : newhomeworkResult not belong to package and packageId {},userId {}", packageId, studentId);
            return null;
        }
        //********* begin 新加的关卡数据 *********//
        BasicReviewHomeworkDetailCacheMapper detailCacheMapper = new BasicReviewHomeworkDetailCacheMapper();
        detailCacheMapper.setHomeworkId(newHomeworkResult.getHomeworkId());
        detailCacheMapper.setAvgScore(newHomeworkResult.processScore());
        detailCacheMapper.setDuration(newHomeworkResult.processDuration());
        detailCacheMapper.setFinishAt(newHomeworkResult.getFinishAt());
        detailCacheMapper.setStageId(target.getStageId());
        detailCacheMapper.setStageName(target.getStageName());
        LinkedHashMap<String, BasicReviewHomeworkDetailCacheMapper> homeworkDetail = mapper.getHomeworkDetail();
        if (homeworkDetail == null) {
            homeworkDetail = new LinkedHashMap<>();
            mapper.setHomeworkDetail(homeworkDetail);
        }
        homeworkDetail.put(newHomeworkResult.getHomeworkId(), detailCacheMapper);
        //********* begin 新加的关卡数据 *********//

        //计算完成的关卡数，是否完成
        mapper.setFinishPackageCount(1 + SafeConverter.toInt(mapper.getFinishPackageCount()));
        if (SafeConverter.toInt(mapper.getFinishPackageCount()) == homeworkPackage.getStages().size()) {
            mapper.setFinished(true);
        }
        String key = manager.getCacheKey(studentId, packageId);
        if (!manager.set(key, mapper)) {
            // 保存失败，清除缓存
            manager.evict(key);
            return null;
        }
        return mapper;
    }

    private BasicReviewHomeworkCacheMapper $init(String packageId, Long studentId) {
        BasicReviewHomeworkPackage homeworkPackage = basicReviewHomeworkPackageDao.load(packageId);
        if (homeworkPackage == null || SafeConverter.toBoolean(homeworkPackage.getDisabled())) {
            return null;
        }
        if (CollectionUtils.isEmpty(homeworkPackage.getStages())) {
            return null;
        }
        String day = DayRange.newInstance(homeworkPackage.getCreateAt().getTime()).toString();
        List<String> newHomeworkResultIds = new LinkedList<>();
        Map<String, BasicReviewStage> stageMap = new LinkedHashMap<>();
        for (BasicReviewStage stage : homeworkPackage.getStages()) {
            String s = new NewHomeworkResult.ID(day, homeworkPackage.getSubject(), stage.getHomeworkId(), studentId.toString()).toString();
            newHomeworkResultIds.add(s);
            stageMap.put(stage.getHomeworkId(), stage);
        }
        Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loads(newHomeworkResultIds, false);
        BasicReviewHomeworkCacheMapper mapper = new BasicReviewHomeworkCacheMapper();
        //******包基本信息******//
        mapper.setClazzGroupId(homeworkPackage.getClazzGroupId());
        mapper.setBookId(homeworkPackage.getBookId());
        mapper.setPackageId(homeworkPackage.getId());
        mapper.setStudentId(studentId);
        mapper.setTeacherId(homeworkPackage.getTeacherId());
        mapper.setSubject(homeworkPackage.getSubject());
        LinkedHashMap<String, BasicReviewHomeworkDetailCacheMapper> detailMap = new LinkedHashMap<>();
        mapper.setHomeworkDetail(detailMap);
        //完成关卡信息
        for (NewHomeworkResult n : newHomeworkResultMap.values()) {
            if (n.isFinished() && stageMap.containsKey(n.getHomeworkId())) {
                BasicReviewStage stage = stageMap.get(n.getHomeworkId());
                BasicReviewHomeworkDetailCacheMapper detailCacheMapper = new BasicReviewHomeworkDetailCacheMapper();
                detailCacheMapper.setHomeworkId(n.getHomeworkId());
                detailCacheMapper.setAvgScore(n.processScore());
                detailCacheMapper.setDuration(n.processDuration());
                detailCacheMapper.setFinishAt(n.getFinishAt());
                detailCacheMapper.setStageId(stage.getStageId());
                detailCacheMapper.setStageName(stage.getStageName());
                detailMap.put(n.getHomeworkId(), detailCacheMapper);
                mapper.setFinishPackageCount(1 + SafeConverter.toInt(mapper.getFinishPackageCount()));
            }
        }
        //是否完成包
        if (SafeConverter.toInt(mapper.getFinishPackageCount()) == stageMap.size()) {
            mapper.setFinished(true);
        }
        BasicReviewHomeworkCacheManager manager = newHomeworkCacheService.getBasicReviewHomeworkCacheManager();
        String key = manager.getCacheKey(studentId, packageId);
        if (manager.set(key, mapper)) {
            return mapper;
        } else {
            return null;
        }
    }

    /**
     * 组下面所有做过作业的学生
     * For 期末复习
     *
     * @param clazzGroupId
     * @return
     */
    @Override
    public List<BasicReviewHomeworkCacheMapper> loadBasicReviewHomeworkCacheMappers(Long clazzGroupId) {
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
        Map<Long, List<BasicReviewHomeworkPackage>> basicReviewHomeworkPackageMap = basicReviewHomeworkPackageDao.loadBasicReviewHomeworkPackageByClazzGroupIds(Collections.singleton(clazzGroupId));
        if (CollectionUtils.isEmpty(studentIds) || MapUtils.isEmpty(basicReviewHomeworkPackageMap) || CollectionUtils.isEmpty(basicReviewHomeworkPackageMap.get(clazzGroupId))) {
            return Collections.emptyList();
        }

        List<BasicReviewHomeworkPackage> basicReviewHomeworkPackages = Lists.newArrayList();
        basicReviewHomeworkPackageMap.values().forEach(basicReviewHomeworkPackages::addAll);
        List<String> packageIds = Lists.transform(basicReviewHomeworkPackages, BasicReviewHomeworkPackage::getId);

        List<BasicReviewHomeworkCacheMapper> result = new ArrayList<>();
        packageIds.forEach(packageId -> {
            Map<Long, BasicReviewHomeworkCacheMapper> mapper = loadBasicReviewHomeworkCacheMapper(packageId, studentIds);
            if (MapUtils.isNotEmpty(mapper)) {
                for (BasicReviewHomeworkCacheMapper basicReviewHomeworkCacheMapper : mapper.values()) {
                    if (basicReviewHomeworkCacheMapper != null) {
                        result.add(basicReviewHomeworkCacheMapper);
                    }
                }
            }
        });
        return result;
    }
}
