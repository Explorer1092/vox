package com.voxlearning.utopia.agent.support;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * AgentSchoolLevelSupport
 *
 * @author song.wang
 * @date 2018/11/6
 */
@Named
public class AgentSchoolLevelSupport {

    @Inject
    private BaseOrgService baseOrgService;

    public List<Integer> fetchTargetSchoolLevelIds(Long id, Integer dataType, Integer schoolLevelFlag){
        List<Integer> resultList = fetchTargetSchoolLevelIds(Collections.singleton(id), dataType, schoolLevelFlag).get(id);
        if(CollectionUtils.isEmpty(resultList)){
            return new ArrayList<>();
        }
        return resultList;
    }

    private Map<Long, List<Integer>> fetchTargetSchoolLevelIds(Collection<Long> ids, Integer dataType, Integer schoolLevelFlag){
        Map<Long, List<Integer>> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids)){
            return resultMap;
        }

        Map<Long, List<SchoolLevel>> schoolLevelsMap = new HashMap<>();
        if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
            schoolLevelsMap.putAll(baseOrgService.getGroupServiceSchoolLevels(ids));
        } else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
            schoolLevelsMap.putAll(baseOrgService.getUserServiceSchoolLevels(ids));
        }

        ids.forEach(p -> {
            List<SchoolLevel> schoolLevels = schoolLevelsMap.get(p);
            List<Integer> schoolLevelIds = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(schoolLevels)){
                schoolLevels.stream().forEach(s -> {
                    if(schoolLevelFlag == 1){
                        if(s == SchoolLevel.JUNIOR){
                            schoolLevelIds.add(s.getLevel());
                        }
                    }else if(schoolLevelFlag == 24){
                        if(s == SchoolLevel.MIDDLE || s == SchoolLevel.HIGH){
                            schoolLevelIds.add(s.getLevel());
                        }
                    }else if (schoolLevelFlag == 2){
                        if (s == SchoolLevel.MIDDLE){
                            schoolLevelIds.add(s.getLevel());
                        }
                    }else if (schoolLevelFlag == 124){
                        if(s == SchoolLevel.JUNIOR || s == SchoolLevel.MIDDLE || s == SchoolLevel.HIGH){
                            schoolLevelIds.add(s.getLevel());
                        }
                    }
                });
            }
            resultMap.put(p, schoolLevelIds);
        });
        return resultMap;
    }

    public Map<Long, Integer> fetchTargetCompositeSchoolLevel(Collection<Long> ids, Integer dataType, Integer schoolLevelFlag){
        Map<Long, Integer> resultMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(ids) || (Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP) || Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER))){
            Map<Long, List<Integer>> tmpMap = fetchTargetSchoolLevelIds(ids, dataType, schoolLevelFlag);
            ids.forEach(p -> {
                List<Integer> schoolLevels = tmpMap.get(p);
                if(schoolLevels == null){
                    schoolLevels = new ArrayList<>();
                }
                resultMap.put(p, AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels));
            });
        }
        return resultMap;
    }
}
