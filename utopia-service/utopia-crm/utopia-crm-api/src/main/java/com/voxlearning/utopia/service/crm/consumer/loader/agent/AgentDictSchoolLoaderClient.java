package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.ListUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentDictSchoolLoader;

import java.util.*;
import java.util.concurrent.Future;

/**
 * AgentDictSchoolLoaderClient
 *
 * @author song.wang
 * @date 2017/8/3
 */
public class AgentDictSchoolLoaderClient implements AgentDictSchoolLoader {

    @ImportService(interfaceClass = AgentDictSchoolLoader.class)
    private AgentDictSchoolLoader remoteReference;

    @Override
    public AgentDictSchool load(Long dictId) {
        return remoteReference.load(dictId);
    }

    @Deprecated
    @Override
    public List<AgentDictSchool> findAllDictSchool() {
        return remoteReference.findAllDictSchool();
    }

    @Override
    public AgentDictSchool findBySchoolId(Long schoolId) {
        return remoteReference.findBySchoolId(schoolId);
    }

    @Override
    public Map<Long, AgentDictSchool> findBySchoolIds(Collection<Long> schoolIds){
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        Map<Long, AgentDictSchool> resultMap = new HashMap<>();

        List<List<Long>> splitList = ListUtils.partition(new ArrayList<>(schoolIds), 500);
        List<Future<Map<Long, AgentDictSchool>>> futureList = new ArrayList<>();
        for(List<Long> partitionList : splitList){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> remoteReference.findBySchoolIds(partitionList)));
        }
        for(Future<Map<Long, AgentDictSchool>> future : futureList){
            try {
                Map<Long, AgentDictSchool> schoolDictMap = future.get();
                if(MapUtils.isNotEmpty(schoolDictMap)){
                    resultMap.putAll(schoolDictMap);
                }
            }catch (Exception e){
            }
        }
        return resultMap;
    }



    @Override
    public List<AgentDictSchool> findByCountyCode(Integer countyCode) {
        return remoteReference.findByCountyCode(countyCode);
    }

    @Override
    public Map<Integer, List<AgentDictSchool>> findByCountyCodes(Collection<Integer> countyCodes) {
        if(CollectionUtils.isEmpty(countyCodes)){
            return Collections.emptyMap();
        }

        Map<Integer, List<AgentDictSchool>> resultMap = new HashMap<>();

        List<List<Integer>> splitList = ListUtils.partition(new ArrayList<>(countyCodes), 300);
        List<Future<Map<Integer, List<AgentDictSchool>>>> futureList = new ArrayList<>();
        for(List<Integer> partitionList : splitList){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> remoteReference.findByCountyCodes(partitionList)));
        }
        for(Future<Map<Integer, List<AgentDictSchool>>> future : futureList){
            try {
                Map<Integer, List<AgentDictSchool>> regionDictMap = future.get();
                if(MapUtils.isNotEmpty(regionDictMap)){
                    resultMap.putAll(regionDictMap);
                }
            }catch (Exception e){
            }
        }
        return resultMap;
    }
}
