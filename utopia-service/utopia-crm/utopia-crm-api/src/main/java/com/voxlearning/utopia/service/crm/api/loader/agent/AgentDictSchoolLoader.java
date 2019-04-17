package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AgentDictSchoolLoader
 *
 * @author song.wang
 * @date 2017/8/3
 */
@ServiceVersion(version = "2017.08.04")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentDictSchoolLoader extends IPingable{

    AgentDictSchool load(Long dictId);

    @Deprecated
    List<AgentDictSchool> findAllDictSchool();

    AgentDictSchool findBySchoolId(Long schoolId);

    Map<Long, AgentDictSchool> findBySchoolIds(Collection<Long> schoolIds);

    List<AgentDictSchool> findByCountyCode(Integer countyCode);

    Map<Integer, List<AgentDictSchool>> findByCountyCodes(Collection<Integer> countyCodes);
}
