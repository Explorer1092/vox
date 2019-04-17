package com.voxlearning.utopia.agent.service.authority;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.dao.mongo.authority.AgentRecordAuthorityDao;
import com.voxlearning.utopia.agent.persist.entity.authority.AgentRecordAuthority;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Named
public class AgentRecordAuthorityService {

    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentRecordAuthorityDao recordAuthorityDao;


    public AgentRecordAuthority getRecordAuthority(String recordId, Integer recordType){
        return recordAuthorityDao.loadByRidAndType(recordId, recordType);
    }

    public MapMessage saveRecordAuthority(String recordId, Integer recordType, Collection<Long> groupIds, Collection<Integer> roleIds, Collection<Long> userIds, Integer rule){
        AgentRecordAuthority authority = recordAuthorityDao.loadByRidAndType(recordId, recordType);
        if(authority == null){
            authority = new AgentRecordAuthority();
            authority.setRecordId(recordId);
            authority.setRecordType(recordType);
        }
        if(CollectionUtils.isNotEmpty(groupIds)){
            authority.setGroupIds(new ArrayList<>(groupIds));
        }else {
            authority.setGroupIds(new ArrayList<>());      // 赋一个空list，  null 值不会更新到数据库
        }
        if(CollectionUtils.isNotEmpty(roleIds)){
            authority.setRoleIds(new ArrayList<>(roleIds));
        }else {
            authority.setRoleIds(new ArrayList<>());
        }
        if(CollectionUtils.isNotEmpty(userIds)){
            authority.setUserIds(new ArrayList<>(userIds));
        }else {
            authority.setUserIds(new ArrayList<>());
        }
        authority.setRule(SafeConverter.toInt(rule));
        authority.setDisabled(false);
        recordAuthorityDao.upsert(authority);
        return MapMessage.successMessage();
    }

    public void deleteRecordAuthority(String recordId, Integer recordType){
        AgentRecordAuthority authority = recordAuthorityDao.loadByRidAndType(recordId, recordType);
        if(authority != null){
            authority.setDisabled(true);
            recordAuthorityDao.replace(authority);
        }
    }

    public List<String> getHasAuthorityRecordIds(Collection<String> recordIds, Integer recordType, Long userId){
        List<String> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(recordIds) || recordType == null){
            return resultList;
        }

        Map<String, Future<Boolean>> futureMap = new HashMap<>();
        recordIds.forEach(p -> futureMap.put(p, AlpsThreadPool.getInstance().submit(() -> hasAuthority(p, recordType, userId))));
        for(String rid : futureMap.keySet()){
            try{
                Future<Boolean> future = futureMap.get(rid);
                if(future == null){
                    continue;
                }
                Boolean hasAuthority = future.get();
                if(SafeConverter.toBoolean(hasAuthority)){
                    resultList.add(rid);
                }
            }catch (Exception e){
            }
        }
        return resultList;
    }

    public boolean hasAuthority(String recordId, Integer recordType, Long userId){
        if(StringUtils.isBlank(recordId) || recordType == null){
            return false;
        }
        AgentRecordAuthority authority = recordAuthorityDao.loadByRidAndType(recordId, recordType);
        if(authority == null || (CollectionUtils.isEmpty(authority.getGroupIds()) && CollectionUtils.isEmpty(authority.getRoleIds()) && CollectionUtils.isEmpty(authority.getUserIds()))){
            return false;
        }
        if(CollectionUtils.isNotEmpty(authority.getUserIds()) && authority.getUserIds().contains(userId)){
            return true;
        }

        if(CollectionUtils.isNotEmpty(authority.getGroupIds())){
            Set<Long> groupIds = new HashSet<>();
            authority.getGroupIds().forEach(p -> {
                if(groupIds.contains(p)){
                    return;
                }
                groupIds.add(p);
                List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(p);
                if(CollectionUtils.isNotEmpty(subGroupList)){
                    subGroupList.forEach(s -> groupIds.add(s.getId()));
                }
            });

            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroups(groupIds);

            if(CollectionUtils.isEmpty(authority.getRoleIds())){
                return CollectionUtils.isNotEmpty(groupUserList) && groupUserList.stream().anyMatch(p -> Objects.equals(p.getUserId(), userId));
            }else {
                if(SafeConverter.toInt(authority.getRule()) == 0) {          // 取交集
                    return CollectionUtils.isNotEmpty(groupUserList)
                            && groupUserList.stream().anyMatch(p -> Objects.equals(p.getUserId(), userId) && authority.getRoleIds().contains(p.getUserRoleId()));
                }else if(SafeConverter.toInt(authority.getRule()) == 1){     // 取并集

                    if(CollectionUtils.isNotEmpty(groupUserList) && groupUserList.stream().anyMatch(p -> Objects.equals(p.getUserId(), userId))){
                        return true;
                    }
                    AgentRoleType roleType = baseOrgService.getUserRole(userId);
                    return roleType != null && authority.getRoleIds().contains(roleType.getId());
                }
            }
            return false;
        }else {
            if(CollectionUtils.isNotEmpty(authority.getRoleIds())){
                List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(userId);
                return CollectionUtils.isNotEmpty(groupUserList)
                        && groupUserList.stream().anyMatch(p -> authority.getRoleIds().contains(p.getUserRoleId()));
            }else {
                return false;
            }
        }
    }

    public List<Long> getAuthorityUsers(String recordId, Integer recordType){
        Set<Long> userIds = new HashSet<>();
        if(StringUtils.isBlank(recordId) || recordType == null){
            return new ArrayList<>(userIds);
        }

        AgentRecordAuthority authority = recordAuthorityDao.loadByRidAndType(recordId, recordType);
        if(authority == null || (CollectionUtils.isEmpty(authority.getGroupIds()) && CollectionUtils.isEmpty(authority.getRoleIds()) && CollectionUtils.isEmpty(authority.getUserIds()))){
            return new ArrayList<>(userIds);
        }

        if(CollectionUtils.isNotEmpty(authority.getUserIds())){
            userIds.addAll(authority.getUserIds());
        }

        if(CollectionUtils.isNotEmpty(authority.getGroupIds())){
            Set<Long> groupIds = new HashSet<>();
            authority.getGroupIds().forEach(p -> {
                if(groupIds.contains(p)){
                    return;
                }
                groupIds.add(p);
                List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(p);
                if(CollectionUtils.isNotEmpty(subGroupList)){
                    subGroupList.forEach(s -> groupIds.add(s.getId()));
                }
            });

            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroups(groupIds);

            if(CollectionUtils.isEmpty(authority.getRoleIds())){
                if(CollectionUtils.isNotEmpty(groupUserList)) {
                    groupUserList.forEach(p -> userIds.add(p.getUserId()));
                }
            }else {

                if(SafeConverter.toInt(authority.getRule()) == 0){          // 取交集
                    if(CollectionUtils.isNotEmpty(groupUserList)){
                        groupUserList.forEach(p -> {
                            if(authority.getRoleIds().contains(p.getUserRoleId())){
                                userIds.add(p.getUserId());
                            }
                        });
                    }
                }else if(SafeConverter.toInt(authority.getRule()) == 1){   // 取并集
                    if(CollectionUtils.isNotEmpty(groupUserList)) {
                        groupUserList.forEach(p -> userIds.add(p.getUserId()));
                    }
                    authority.getRoleIds().forEach(r -> {
                        List<AgentGroupUser> roleUserList = baseOrgService.getGroupUserByRole(r);
                        if(CollectionUtils.isNotEmpty(roleUserList)){
                            roleUserList.forEach(u -> userIds.add(u.getUserId()));
                        }
                    });
                }
            }
        }else {
            if(CollectionUtils.isNotEmpty(authority.getRoleIds())){
                authority.getRoleIds().forEach(p -> {
                    List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByRole(p);
                    if(CollectionUtils.isNotEmpty(groupUserList)){
                        groupUserList.forEach(g -> userIds.add(g.getUserId()));
                    }
                });
            }
        }

        return new ArrayList<>(userIds);
    }



}
