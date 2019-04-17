package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.AncientPoetryCacheLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMissionResult;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.AncientPoetryResultCacheMapper;
import com.voxlearning.utopia.service.newhomework.consumer.cache.AncientPoetryResultCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryActivityDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryMissionDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryMissionResultDao;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */

@Named
@Service(interfaceClass = AncientPoetryCacheLoader.class)
@ExposeService(interfaceClass = AncientPoetryCacheLoader.class)
public class AncientPoetryCacheLoaderImpl extends SpringContainerSupport implements AncientPoetryCacheLoader {

    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private AncientPoetryActivityDao ancientPoetryActivityDao;
    @Inject private AncientPoetryMissionDao ancientPoetryMissionDao;
    @Inject private AncientPoetryMissionResultDao ancientPoetryMissionResultDao;

    @Override
    public List<AncientPoetryResultCacheMapper> loadAncientPoetryResultCacheMapper(List<String> activityIds, List<Long> studentIds) {
        AncientPoetryResultCacheManager cacheManager = newHomeworkCacheService.getAncientPoetryResultCacheManager();
        List<AncientPoetryResultCacheMapper> mappers = new ArrayList<>();
        for (String activityId : activityIds) {
            for (Long studentId : studentIds) {
                AncientPoetryResultCacheMapper mapper = cacheManager.load(cacheManager.getCacheKey(activityId, studentId));
                if (mapper == null) {
                    mapper = $init(activityId, studentId);
                }
                if (mapper != null) {
                    mappers.add(mapper);
                }
            }
        }
        return mappers;
    }

    @Override
    public AncientPoetryResultCacheMapper loadAncientPoetryResultCacheMapper(String activityId, Long studentId) {
        AncientPoetryResultCacheManager cacheManager = newHomeworkCacheService.getAncientPoetryResultCacheManager();
        AncientPoetryResultCacheMapper mapper = cacheManager.load(cacheManager.getCacheKey(activityId, studentId));
        if (mapper == null) {
            mapper = $init(activityId, studentId);
        }
        return mapper;
    }


    private AncientPoetryResultCacheMapper $init(String activityId, Long studentId) {
        AncientPoetryActivity activity = ancientPoetryActivityDao.load(activityId);
        if (activity == null) {
            return null;
        }
        List<String> missionIds = activity.getMissions().stream().map(AncientPoetryActivity.Mission::getMissionId).collect(Collectors.toList());
        Map<String, AncientPoetryMission> missionMap = ancientPoetryMissionDao.loads(missionIds);
        List<String> missionResultIds = missionMap.values().stream().map(mission -> AncientPoetryMissionResult.generateId(activityId, mission.getId(), studentId, Boolean.FALSE)).collect(Collectors.toList());
        missionResultIds.add(AncientPoetryMissionResult.generateId(activityId, null, studentId, Boolean.TRUE));// 亲子助力关卡
        Map<String, AncientPoetryMissionResult> missionResultMap = ancientPoetryMissionResultDao.loads(missionResultIds);
        if (MapUtils.isEmpty(missionResultMap)) {
            return null;
        }

        int finishCount = 0;
        double totalStar = 0D;
        Date lastFinishAt = null;
        long duration = 0;
        int wrongNum = 0;
        int correctTrueNum = 0;
        int correctFalseNum = 0;
        AncientPoetryResultCacheMapper mapper = new AncientPoetryResultCacheMapper();
        for (AncientPoetryMissionResult missionResult : missionResultMap.values()) {
            AncientPoetryResultCacheMapper.PoetryMissionCacheMapper missionCache = new AncientPoetryResultCacheMapper.PoetryMissionCacheMapper();
            missionCache.setMissionId(missionResult.getMissionId());
            missionCache.setStar(missionResult.getStar());
            totalStar += SafeConverter.toDouble(missionResult.getStar());
            missionCache.setFinishAt(missionResult.getFinishAt());

            if (MapUtils.isNotEmpty(missionResult.getAnswers())) {
                for (AncientPoetryProcessResult baseProcess : missionResult.getAnswers().values()) {
                    AncientPoetryProcessResult.QuestionProcessResult processResult = baseProcess.getProcessResult();
                    duration += processResult.getDuration();
                    if (!processResult.getGrasp()) {
                        wrongNum++;
                    }
                    if (baseProcess.getCorrectProcessResult() != null) {
                        if (baseProcess.getCorrectProcessResult().getGrasp()) {
                            correctTrueNum++;
                        } else {
                            correctFalseNum++;
                        }
                    }
                }
            }

            // 亲子助力关卡处理
            if (missionResult.isParentMissionFinished()) {
                mapper.getMissionCache().put(AncientPoetryMission.getHelpMissionId(), missionCache);
            } else {
                // missionResultMap有序
                lastFinishAt = missionResult.getFinishAt();
                if (missionResult.isFinished()) {
                    finishCount++;
                }
                mapper.getMissionCache().put(missionResult.getMissionId(), missionCache);
            }
        }

        mapper.setDuration(duration);
        mapper.setWrongNum(wrongNum);
        mapper.setCorrectTrueNum(correctTrueNum);
        mapper.setCorrectFalseNum(correctFalseNum);
        mapper.setStudentId(studentId);
        mapper.setActivityId(activityId);
        mapper.setUpdateAt(new Date());
        mapper.setStar(totalStar);
        mapper.setFinishMissionCount(finishCount);

        //助力关卡是-1（AncientPoetryMission.getHelpMissionId）,只有当助力关卡和普通关卡全都完成的情况下才算通关
        AncientPoetryResultCacheMapper.PoetryMissionCacheMapper helpMissionCache = mapper.getMissionCache().get(AncientPoetryMission.getHelpMissionId());
        if (finishCount == missionIds.size() && helpMissionCache != null && lastFinishAt != null) {
            if (helpMissionCache.getFinishAt().after(lastFinishAt)) {
                lastFinishAt = helpMissionCache.getFinishAt();
            }
            // 通关时间是包含助力关卡的最终完成时间
            mapper.setFinishAt(lastFinishAt);
        }

        // 数据放入缓存
        AncientPoetryResultCacheManager cacheManager = newHomeworkCacheService.getAncientPoetryResultCacheManager();
        String key = cacheManager.getCacheKey(activityId, studentId);
        if (cacheManager.addPoetryResult(key, mapper)) {
            return mapper;
        } else {
            return null;
        }
    }
}
