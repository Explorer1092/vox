package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanKnowledgeBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanKnowledgeDetailBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanStudyHabitBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanUnitDetailBO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangbin
 * @since 2018/3/12
 */

@ServiceVersion(version = "20180319")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface TermPlanService extends IPingable {
    /**
     * 获取知识点掌握情况
     */
    TermPlanKnowledgeBO loadKnowledgePointGraspInfos(Long groupId, String unitId);

    /**
     * 查看知识点详情
     */
    List<TermPlanKnowledgeDetailBO> loadKnowledgeDetail(Long groupId, String unitId);

    /**
     * 获取作业完成率、订正完成率
     */
    TermPlanStudyHabitBO loadHomeworkFinishInfo(Long groupId, String unitId);

    /**
     * 切换单元
     */
    List<TermPlanUnitDetailBO> changeUnit(Map<Long, Long> clazzIdGroupIdMap, Map<String, String> unitIdNameMap, String defaultUnitId);
}
