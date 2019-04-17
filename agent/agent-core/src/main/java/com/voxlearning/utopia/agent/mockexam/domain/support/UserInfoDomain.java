package com.voxlearning.utopia.agent.mockexam.domain.support;

import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * 获取用户信息
 *
 * @Author: peng.zhang
 * @Date: 2018/8/23
 */
@Service
public class UserInfoDomain {

    @Inject
    protected BaseOrgService baseOrgService;

    public String getUserEmailByUserId(Long creatorId){
        AgentUser user = baseOrgService.getUser(creatorId);
        return user == null ? null : user.getEmail();
    }

    public Long getUserIdByRealName(String realName){
        List<AgentUser> users = baseOrgService.getUserByRealName(realName);
        return users.isEmpty() ? null : users.get(0).getId();
    }

//    public List<String> getUserInfoByAccountNumber(){
//
//    }
}
