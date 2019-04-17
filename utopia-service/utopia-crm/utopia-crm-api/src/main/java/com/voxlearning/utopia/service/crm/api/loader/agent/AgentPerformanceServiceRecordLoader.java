package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceServiceRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chunlin.yu
 * @create 2017-10-30 20:03
 **/
@ServiceVersion(version = "2017.10.26")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentPerformanceServiceRecordLoader extends IPingable{


    List<AgentPerformanceServiceRecord> load(@CacheParameter("m") Integer month, @CacheParameter("t") Long targetId, @CacheParameter("a") AgentPerformanceGoalType agentPerformanceGoalType);


}
