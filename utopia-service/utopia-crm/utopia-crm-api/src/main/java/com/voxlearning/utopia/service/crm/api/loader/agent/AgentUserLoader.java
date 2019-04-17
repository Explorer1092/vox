package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AgentUserLoader
 *
 * @author song.wang
 * @date 2016/12/6
 */
@ServiceVersion(version = "2017.11.01")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentUserLoader extends IPingable {

    @Idempotent
    AgentUser load(Long userId);

    @Idempotent
    List<AgentUser> findAll();

    @Idempotent
    AgentUser findByName(String accountName);

    @Idempotent
    Map<Long, AgentUser> findByIds(Collection<Long> userIds);

    @Idempotent
    AgentUser findByMobile(String mobile);

    @Idempotent
    List<AgentUser> findByRealName(String realName);

    /**
     * 根据RealName全匹配查询
     * @param realName
     * @return
     */
    @Idempotent
    List<AgentUser> getUserByRealName(String realName);


    /**
     * 获取无效的用户
     * @param userId
     * @return
     */
    @Idempotent
    AgentUser loadUnValidUser(Long userId);

    /**
     * 根据工号获取用户
     * @param accountNumber
     * @return
     */
    @Idempotent
    List<AgentUser> getUserByAccountNumber(String accountNumber);

    /**
     * 根据用户id获取用户信息
     * @param userId
     * @return
     */
    @Idempotent
    AgentUser loadIncludeDel(Long userId);
}
