package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentUserLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AgentUserLoaderClient
 *
 * @author song.wang
 * @date 2016/12/6
 */
public class AgentUserLoaderClient implements AgentUserLoader {

    @ImportService(interfaceClass = AgentUserLoader.class)
    private AgentUserLoader remoteReference;

    @Override
    public AgentUser load(Long userId) {
        return remoteReference.load(userId);
    }

    @Override
    public List<AgentUser> findAll() {
        return remoteReference.findAll();
    }

    @Override
    public AgentUser findByName(String accountName) {
        return remoteReference.findByName(accountName);
    }

    @Override
    public Map<Long, AgentUser> findByIds(Collection<Long> userIds) {
        return remoteReference.findByIds(userIds);
    }

    @Override
    public AgentUser findByMobile(String mobile) {
        return remoteReference.findByMobile(mobile);
    }

    @Override
    public List<AgentUser> findByRealName(String realName) {
        return remoteReference.findByRealName(realName);
    }

    @Override
    public List<AgentUser> getUserByRealName(String realName) {
        return remoteReference.getUserByRealName(realName);
    }

    @Override
    public AgentUser loadUnValidUser(Long userId) {
        return remoteReference.loadUnValidUser(userId);
    }

    @Override
    public List<AgentUser> getUserByAccountNumber(String accountNumber) {
        return remoteReference.getUserByAccountNumber(accountNumber);
    }

    @Override
    public AgentUser loadIncludeDel(Long userId) {
        return remoteReference.loadIncludeDel(userId);
    }


}
