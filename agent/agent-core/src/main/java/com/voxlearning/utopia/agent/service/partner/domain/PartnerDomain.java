package com.voxlearning.utopia.agent.service.partner.domain;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.persist.AgentPartnerPersistence;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentOrgContactPerson;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartner;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartnerLinkMan;
import com.voxlearning.utopia.agent.service.partner.model.Partner;
import com.voxlearning.utopia.agent.service.partner.model.PartnerLinkMan;
import com.voxlearning.utopia.agent.view.partner.input.UpsertPartnerParams;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.springframework.beans.BeanUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 模型逻辑层
 * @author: kaibo.he
 * @create: 2019-04-02 14:24
 **/
@Named
public class PartnerDomain {

    @Inject private RaikouSystem raikouSystem;
    @Inject
    private AgentPartnerPersistence agentPartnerPersistence;

    public Partner build(final String longitude, final String latitude, final String address, UpsertPartnerParams upsertPartnerParams, AuthCurrentUser agentUser) {
        Partner partner = new Partner();
        BeanUtils.copyProperties(upsertPartnerParams, partner);
        partner.setLongitude(longitude);
        partner.setLatitude(latitude);
        partner.setAddress(address);
        ExRegion exRegion = raikouSystem.loadRegion(upsertPartnerParams.getRegionCode());
        if (Objects.nonNull(exRegion)) {
            partner.setCountyCode(exRegion.getCountyCode());
            partner.setCityCode(exRegion.getCityCode());
            partner.setCountyName(exRegion.getCountyName());
            partner.setCityName(exRegion.getCityName());
            partner.setProvinceCode(exRegion.getProvinceCode());
            partner.setProvinceName(exRegion.getProvinceName());
        }
        partner.setCreateUserId(agentUser.getUserId());
        partner.setCreateUserName(agentUser.getRealName());
        if (Objects.equals(partner.getId(), 0L)) {
            partner.setId(null);
        } else {
            //不修改创建人
            AgentPartner agentPartner = agentPartnerPersistence.load(partner.getId());
            partner.setCreateUserName(agentPartner.getCreateUserName());
            partner.setCreateUserId(agentPartner.getCreateUserId());
        }
        return partner;
    }

    public AgentPartner build(Partner partner) {
        AgentPartner agentPartner = new AgentPartner();
        if (Objects.isNull(partner)) {
            return agentPartner;
        }
        BeanUtils.copyProperties(partner, agentPartner);
        StringBuilder otherPhtotUrls = new StringBuilder();
        if (CollectionUtils.isNotEmpty(partner.getOtherPhotoUrls())) {
            partner.getOtherPhotoUrls().forEach(url -> {
                otherPhtotUrls.append(",").append(url);
            });
        }
        if (otherPhtotUrls.length() > 0) {
            agentPartner.setOtherPhotoUrls(otherPhtotUrls.substring(1));
        } else {
            agentPartner.setOtherPhotoUrls("");
        }
        return agentPartner;
    }

    public Partner build(AgentPartner agentPartner) {
        Partner partner = new Partner();
        if (Objects.isNull(agentPartner)) {
            return partner;
        }
        BeanUtils.copyProperties(agentPartner, partner);
        if (StringUtils.isNotBlank(agentPartner.getOtherPhotoUrls())) {
            List<String> otherPhotoUrls = new ArrayList<>(Arrays.asList(agentPartner.getOtherPhotoUrls().split(",")));
            partner.setOtherPhotoUrls(otherPhotoUrls);
        }
        partner.setCreateTime(DateUtils.dateToString(agentPartner.getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
        return partner;
    }

    public Partner build(Map<Long, AgentOrgContactPerson> contactPersonMap, List<AgentPartnerLinkMan> linkManList, AgentPartner agentPartner) {
        Partner partner = new Partner();
        if (Objects.isNull(agentPartner)) {
            return partner;
        }
        partner = build(agentPartner);

        if (MapUtils.isNotEmpty(contactPersonMap) && CollectionUtils.isNotEmpty(linkManList)) {
            List<PartnerLinkMan> linkMans = new ArrayList<>();
            linkManList.stream().forEach(lm -> {
                if (contactPersonMap.containsKey(lm.getLinkManId())) {
                    AgentOrgContactPerson person = contactPersonMap.get(lm.getLinkManId());
                    if (Objects.nonNull(person.getHoneycombId()) && person.getHoneycombId() > 0) {
                        linkMans.add(PartnerLinkMan.Builder.build(lm));
                    }
                }
            });
            if (CollectionUtils.isNotEmpty(linkMans)) {
                partner.setLinkMans(linkMans);
            }
        }
        return partner;
    }
}
