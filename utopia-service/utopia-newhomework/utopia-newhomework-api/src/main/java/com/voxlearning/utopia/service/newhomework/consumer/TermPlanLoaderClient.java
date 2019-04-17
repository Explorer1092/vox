package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanKnowledgeBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanKnowledgeDetailBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanStudyHabitBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanUnitDetailBO;
import com.voxlearning.utopia.service.newhomework.api.service.TermPlanService;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2018/3/9
 */

public class TermPlanLoaderClient implements TermPlanService {

    @ImportService(interfaceClass = TermPlanService.class)
    private TermPlanService remoteReference;


    @Override
    public TermPlanKnowledgeBO loadKnowledgePointGraspInfos(Long groupId, String unitId) {
        return remoteReference.loadKnowledgePointGraspInfos(groupId, unitId);
    }

    @Override
    public List<TermPlanKnowledgeDetailBO> loadKnowledgeDetail(Long groupId, String unitId) {
        return remoteReference.loadKnowledgeDetail(groupId, unitId);
    }

    @Override
    public TermPlanStudyHabitBO loadHomeworkFinishInfo(Long groupId, String unitId) {
        return remoteReference.loadHomeworkFinishInfo(groupId, unitId);
    }

    @Override
    public List<TermPlanUnitDetailBO> changeUnit(Map<Long, Long> clazzIdGroupIdMap, Map<String, String> unitIdNameMap, String defaultUnitId) {
        return remoteReference.changeUnit(clazzIdGroupIdMap, unitIdNameMap, defaultUnitId);
    }

}
