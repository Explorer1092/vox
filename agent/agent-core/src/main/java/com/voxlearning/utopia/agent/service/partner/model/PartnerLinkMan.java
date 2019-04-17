package com.voxlearning.utopia.agent.service.partner.model;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartnerLinkMan;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @description: 合作伙伴联系人关系模型
 * @author: kaibo.he
 * @create: 2019-04-02 15:06
 **/
@Data
public class PartnerLinkMan {
    private Long linkManId;     //联系人id
    private Long partnerId;     //合作机构id

    public static class Builder {
        public static PartnerLinkMan build(AgentPartnerLinkMan agentPartnerLinkMan) {
            PartnerLinkMan man = new PartnerLinkMan();
            BeanUtils.copyProperties(agentPartnerLinkMan, man);
            return man;
        }
    }
}
