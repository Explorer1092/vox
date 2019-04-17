package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMissionResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.AncientPoetryResultCacheMapper;
import com.voxlearning.utopia.service.newhomework.impl.dao.AncientPoetryStudentGlobalStarPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryMissionResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.AncientPoetryCacheLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author majianxin
 */
@Named
public class AP_FinishAncientPoetryMissionResult extends SpringContainerSupport implements AncientPoetryResultTask {

    @Inject private AncientPoetryMissionResultDao ancientPoetryMissionResultDao;
    @Inject private AncientPoetryStudentGlobalStarPersistence ancientPoetryStudentGlobalStarPersistence;
    @Inject private AncientPoetryCacheLoaderImpl ancientPoetryCacheLoader;

    @Override
    public void execute(AncientPoetryProcessContext context) {
        AncientPoetryMissionResult missionResult = context.getMissionResult();
        missionResult.setUpdateAt(context.getCurrentDate());
        ancientPoetryMissionResultDao.upsert(missionResult);
        ancientPoetryStudentGlobalStarPersistence.addStarAndDuration(context.getStudentId(), context.getAddStar(), context.getAddDuration(), context.getClazzLevel(), context.getSchoolId(), context.getRegionId());

        // 准备活动结果缓存数据
        AncientPoetryResultCacheMapper cacheMapper = ancientPoetryCacheLoader.loadAncientPoetryResultCacheMapper(context.getActivityId(), context.getStudentId());
        if (cacheMapper == null) {
            cacheMapper = new AncientPoetryResultCacheMapper();
            cacheMapper.setStudentId(context.getStudentId());
            cacheMapper.setActivityId(context.getActivityId());
        }
        cacheMapper.setUpdateAt(context.getCurrentDate());
        if (context.isCorrect()) {
            if (context.getGrasp()) {
                cacheMapper.setCorrectTrueNum(cacheMapper.getCorrectTrueNum() + 1);
            } else {
                cacheMapper.setCorrectFalseNum(cacheMapper.getCorrectFalseNum() + 1);
            }
        } else {
            // 只有巩固练习模块的时候才会产生错题
            // 只统计最后一个模块的答题用时
            if (context.getModelType().equals(ModelType.FUN)) {
                cacheMapper.setWrongNum(cacheMapper.getWrongNum() + (context.getGrasp() ? 0 : 1));
                cacheMapper.setDuration(cacheMapper.getDuration() + context.getDurationMilliseconds());
            }
        }

        // 关卡结果数据
        AncientPoetryResultCacheMapper.PoetryMissionCacheMapper missionCacheMapper = new AncientPoetryResultCacheMapper.PoetryMissionCacheMapper();
        missionCacheMapper.setMissionId(context.getMissionId());
        if (missionResult.getStar() != null) {
            missionCacheMapper.setStar(missionResult.getStar());
        }
        if (missionResult.isFinished() && !missionCacheMapper.isFinished()) {
            missionCacheMapper.setFinishAt(context.getCurrentDate());
        }
        // 助力关卡missionCache的key为-1, 具体missionId为原关卡id
        String cacheMissionId = context.isParentMission() ? AncientPoetryMission.getHelpMissionId() : missionResult.getMissionId();
        cacheMapper.getMissionCache().put(cacheMissionId, missionCacheMapper);
        // 活动总星星
        Double star = cacheMapper.getMissionCache().values().stream().filter(o -> o.getStar() != null).mapToDouble(AncientPoetryResultCacheMapper.PoetryMissionCacheMapper::getStar).sum();
        cacheMapper.setStar(star);
        context.setCacheMapper(cacheMapper);
    }
}
