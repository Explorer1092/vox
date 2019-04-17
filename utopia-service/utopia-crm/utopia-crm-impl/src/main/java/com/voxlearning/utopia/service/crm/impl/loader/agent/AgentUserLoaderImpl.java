package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentUserLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentUserPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentUserLoaderImpl
 *
 * @author song.wang
 * @date 2016/12/6
 */
@Named
@Service(interfaceClass = AgentUserLoader.class)
@ExposeService(interfaceClass = AgentUserLoader.class)
public class AgentUserLoaderImpl extends SpringContainerSupport implements AgentUserLoader {

    @Inject
    private AgentUserPersistence agentUserPersistence;

    @Override
    public AgentUser load(Long userId) {
        AgentUser user = agentUserPersistence.load(userId);
        return user != null && user.isValidUser() ? user : null;
    }

    @Override
    public List<AgentUser> findAll() {
        return agentUserPersistence.findAll();
    }

    @Override
    public AgentUser findByName(String accountName) {
        return agentUserPersistence.findByName(accountName);
    }

    @Override
    public Map<Long, AgentUser> findByIds(Collection<Long> userIds) {
        if(CollectionUtils.isEmpty(userIds)){
            return Collections.emptyMap();
        }
        Map<Long, AgentUser> userMap = agentUserPersistence.loads(userIds);
        return userMap.values().stream().filter(p -> p != null && p.isValidUser()).collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));
    }

    @Override
    public AgentUser findByMobile(String mobile) {
        return agentUserPersistence.findByMobile(mobile);
    }

    @Override
    public List<AgentUser> findByRealName(String realName) {
        return agentUserPersistence.findByRealName(realName);
    }

    @Override
    public List<AgentUser> getUserByRealName(String realName) {
        return agentUserPersistence.getUserByRealName(realName);
    }

    @Override
    public AgentUser loadUnValidUser(Long userId) {
        AgentUser user = agentUserPersistence.load(userId);
        return user != null && !user.isValidUser() ? user : null;
    }

    @Override
    public List<AgentUser> getUserByAccountNumber(String accountNumber) {
        return agentUserPersistence.getUserByAccountNumber(accountNumber);
    }

    @Override
    public AgentUser loadIncludeDel(Long userId) {
        return agentUserPersistence.load(userId);
    }
}
