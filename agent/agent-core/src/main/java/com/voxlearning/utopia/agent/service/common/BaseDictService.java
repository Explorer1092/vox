package com.voxlearning.utopia.agent.service.common;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BaseDictService
 *
 * @author song.wang
 * @date 2018/9/17
 */
@Named
public class BaseDictService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;

    public List<AgentDictSchool> loadAllSchoolDictData() {
        List<ExRegion> exRegionList = raikouSystem.getRegionBuffer().loadRegions();
        Set<Integer> allCountyCodes = exRegionList.stream().filter(p -> p.fetchRegionType() == RegionType.COUNTY).map(ExRegion::getId).collect(Collectors.toSet());
        Map<Integer, List<AgentDictSchool>> countyDictMap = agentDictSchoolLoaderClient.findByCountyCodes(allCountyCodes);
        if (MapUtils.isEmpty(countyDictMap)) {
            return new ArrayList<>();
        }
        return countyDictMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }


    public boolean isDictSchool(Long schoolId) {
        return schoolId != null && agentDictSchoolLoaderClient.findBySchoolId(schoolId) != null;
    }
}
