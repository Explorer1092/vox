package com.voxlearning.utopia.agent.service.partner.model;

import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartnerMarketer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-04-03 16:55
 **/
@Data
public class PartnerMarketer {
    private Long userId;     //联系人id
    private Long partnerId;     //合作机构id

    public static class Builder {
        public static PartnerMarketer build(Partner partner) {
            PartnerMarketer partnerMarketer = new PartnerMarketer();
            partnerMarketer.setUserId(partner.getCreateUserId());
            partnerMarketer.setPartnerId(partner.getId());
            return partnerMarketer;
        }

        public static AgentPartnerMarketer build(PartnerMarketer partnerMarketer) {
            AgentPartnerMarketer agentPartnerMarketer = new AgentPartnerMarketer();
            BeanUtils.copyProperties(partnerMarketer, agentPartnerMarketer);
            return agentPartnerMarketer;
        }
    }
}
