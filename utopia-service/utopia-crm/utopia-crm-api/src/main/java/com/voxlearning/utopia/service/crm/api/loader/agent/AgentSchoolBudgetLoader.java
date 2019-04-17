package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentSchoolBudget;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chunlin.yu
 * @create 2017-08-08 10:27
 **/

@ServiceVersion(version = "2017.08.08")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentSchoolBudgetLoader extends IPingable {

    List<AgentSchoolBudget> loadBySchoolId(Long schoolId);

    List<AgentSchoolBudget> loadByMonth(Integer month);

    Map<Long, AgentSchoolBudget> loadBySchoolsAndMonth(Collection<Long> schoolIds, Integer month);
}
