package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceGoal;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Agent业绩目标Loader
 *
 * @author chunlin.yu
 * @create 2017-10-26 13:34
 **/
@ServiceVersion(version = "2017.10.26")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentPerformanceGoalLoader extends IPingable{

    /**
     * 按月取业绩目标
     * @param month
     * @return
     */
    List<AgentPerformanceGoal> loadByMonth(Integer month);

    /**
     * 获取被确认过的业绩目标
     * @param month
     * @return
     */
    List<AgentPerformanceGoal> loadConfirmedByMonth(Integer month);



    AgentPerformanceGoal  loadConfirmedByIdAndType(Long id, AgentPerformanceGoalType type, Integer month);

    /**
     * 根据开始月份查询大于等于的数据,包含自己以及对应的各子部门数据
     * @return
     */
    List<AgentPerformanceGoal> loadByIdAndTypeAndBeginMonth(Long id, AgentPerformanceGoalType idType,Integer beginMonth);

}
