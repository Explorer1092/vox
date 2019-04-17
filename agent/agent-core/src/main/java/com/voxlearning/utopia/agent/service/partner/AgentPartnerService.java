package com.voxlearning.utopia.agent.service.partner;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.bean.hierarchicalstructure.NodeStructure;
import com.voxlearning.utopia.agent.constants.AgentPartnerType;
import com.voxlearning.utopia.agent.persist.AgentOrgContractPersonPersistence;
import com.voxlearning.utopia.agent.persist.AgentPartnerLinkManPersistence;
import com.voxlearning.utopia.agent.persist.AgentPartnerMarketerPersistence;
import com.voxlearning.utopia.agent.persist.AgentPartnerPersistence;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentOrgContactPerson;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartner;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartnerLinkMan;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartnerMarketer;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.honeycomb.HoneycombUserService;
import com.voxlearning.utopia.agent.service.partner.domain.PartnerDomain;
import com.voxlearning.utopia.agent.service.partner.model.LinkMan;
import com.voxlearning.utopia.agent.service.partner.model.Partner;
import com.voxlearning.utopia.agent.service.partner.model.PartnerMarketer;
import com.voxlearning.utopia.agent.service.partner.outerfetch.LinkManHttpClient;
import com.voxlearning.utopia.agent.view.partner.input.UpsertPartnerParams;
import com.voxlearning.utopia.agent.view.partner.output.LinkManListVo;
import com.voxlearning.utopia.agent.view.partner.output.PartnerBaseVo;
import com.voxlearning.utopia.agent.view.partner.output.PartnerInfoVo;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRegionRank;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @description: 合作伙伴业务层
 * @author: kaibo.he
 * @create: 2019-04-02 11:37
 **/
@Named
public class AgentPartnerService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AgentPartnerPersistence agentPartnerPersistence;
    @Inject
    private AgentPartnerLinkManPersistence agentPartnerLinkManPersistence;
    @Inject
    private AgentPartnerMarketerPersistence agentPartnerMarketerPersistence;
    @Inject
    private PartnerDomain partnerDomain;
    @Inject
    private LinkManHttpClient linkManHttpClient;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private HoneycombUserService honeycombUserService;
    @Inject
    private AgentOrgContractPersonPersistence agentOrgContractPersonPersistence;

    /**
     * 删除用户对应的机构所关联的联系人
     *
     * @param userId
     * @return
     */
    public Integer removeLinkManByUserId(Long userId) {
        Integer count = 0;
        List<AgentPartnerMarketer> partnerMarketers = agentPartnerMarketerPersistence.queryByUseridId(userId);
        if (CollectionUtils.isNotEmpty(partnerMarketers)) {
            count = agentPartnerLinkManPersistence.removeByPartnerIds(partnerMarketers.stream().map(AgentPartnerMarketer::getPartnerId).collect(Collectors.toList()));
        }
        return count;
    }

    /**
     * 删除用户对应的机构所关联的蜂巢粉丝
     *
     * @param userId
     * @return
     */
    public Integer removeFansByUserId(Long userId) {
        Integer count = 0;
        List<AgentPartnerMarketer> partnerMarketers = agentPartnerMarketerPersistence.queryByUseridId(userId);
        if (CollectionUtils.isNotEmpty(partnerMarketers)) {
            Map<Long, List<AgentPartnerLinkMan>> pLinkManMap = agentPartnerLinkManPersistence.queryByPartnerIds(partnerMarketers.stream().map(AgentPartnerMarketer::getPartnerId).collect(Collectors.toList()));

            if (MapUtils.isNotEmpty(pLinkManMap)) {
                Set<Long> linkManIds = new HashSet<>();
                for (Map.Entry<Long, List<AgentPartnerLinkMan>> entry : pLinkManMap.entrySet()) {
                    List<Long> subLinkManIds = entry.getValue().stream().map(v -> v.getLinkManId()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(subLinkManIds)) {
                        linkManIds.addAll(subLinkManIds);
                    }
                }
                Map<Long, AgentOrgContactPerson> linkManMap = agentOrgContractPersonPersistence.loads(linkManIds);
                if (MapUtils.isNotEmpty(linkManMap)) {
                    List<Long> fansIds = linkManMap.values()
                            .stream()
                            .filter(v -> Objects.nonNull(v.getHoneycombId()) && v.getHoneycombId() > 0)
                            .map(AgentOrgContactPerson::getId)
                            .collect(Collectors.toList());
                    count = agentPartnerLinkManPersistence.removeByLinkManIds(fansIds);
                }
            }

        }
        return count;
    }

    /**
     * 删除用户对应合作机构所关联的用户及联系人
     *
     * @param userId
     * @return
     */
    public Integer removePartnerRefByUserId(Long userId) {
        Integer count = agentPartnerMarketerPersistence.removeByUserId(userId);
        removeLinkManByUserId(userId);
        return count;
    }

    /**
     * 查找时间段内创建的，并且是用户负责的，合作机构数
     *
     * @param userIds
     * @param beginTime
     * @param endTime
     * @return
     */
    public Map<Long, Integer> findPartnerNumByUserIds(Collection<Long> userIds, Date beginTime, Date endTime) {
        Map<Long, List<AgentPartnerMarketer>> mPartnerMarketerMap = agentPartnerMarketerPersistence.queryByUseridIds(userIds);
        if (MapUtils.isEmpty(mPartnerMarketerMap)) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> result = new HashMap<>();

        mPartnerMarketerMap.forEach((k, v) -> {
            List<Long> pids = v.stream().map(AgentPartnerMarketer::getPartnerId).collect(Collectors.toList());
            Map<Long, List<AgentPartnerLinkMan>> linkManDataMap = agentPartnerLinkManPersistence.queryByPartnerIds(pids);
            long count = linkManDataMap.values().stream().filter(p -> p.stream().anyMatch(t -> t.getCreateDatetime().after(beginTime) && t.getCreateDatetime().before(endTime))).count();
            result.put(k, (int)count);
        });
        return result;
    }

    /**
     * 根据userids获取对应的联系人
     *
     * @param userIds
     * @return
     */
    public Map<Long, List<Long>> findHoneycomIdsByUserIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        Map<Long, List<Long>> result = new HashMap<>();

        //获得市场人员合作机构关系
        Map<Long, List<AgentPartnerMarketer>> mPartnerMarketerMap = agentPartnerMarketerPersistence.queryByUseridIds(userIds);

        mPartnerMarketerMap.forEach((k, v) -> {
            //获得或者伙伴联系人关系
            if (CollectionUtils.isNotEmpty(v)) {
                Set<Long> partnerIdSet = v.stream().map(AgentPartnerMarketer::getPartnerId).collect(Collectors.toSet());
                List<Long> linkManIdList = Optional.ofNullable(agentPartnerLinkManPersistence.queryByPartnerIds(partnerIdSet)).orElse(new HashMap<>())
                        .values().stream()
                        .map(l -> l.stream().map(AgentPartnerLinkMan::getLinkManId).collect(Collectors.toList()))
                        .reduce(new ArrayList<>(), (all, item) -> {
                            all.addAll(item);
                            return all;
                        });
                result.put(k, Optional.ofNullable(linkManIdList).orElse(new ArrayList<>()));
            }
        });
        return result;
    }

    public MapMessage upsertCheck(Long userId, UpsertPartnerParams params) {
        if (StringUtils.isBlank(params.getName())) {
            return MapMessage.errorMessage("机构名称不得为空！");
        }
        if (params.getRegionCode() <= 0) {
            return MapMessage.errorMessage("地区不得为空！");
        }
        if (params.getLongitude() <= 0 || params.getLatitude() <= 0) {
            return MapMessage.errorMessage("经纬度不得为空！");
        }
        if (StringUtils.isBlank(params.getType())) {
            return MapMessage.errorMessage("类别不得为空！");
        }
        if (Objects.isNull(AgentPartnerType.valueOf(params.getType()))) {
            return MapMessage.errorMessage("类别不正确！");
        }
        if (StringUtils.isBlank(params.getHomePhotoUrl())) {
            return MapMessage.errorMessage("门头照片不得为空！");
        }
        if (params.getId() != null && params.getId() != 0) {
            AgentPartner agentPartner = agentPartnerPersistence.load(params.getId());
            if (Objects.isNull(agentPartner)) {
                return MapMessage.errorMessage("该机构不存在！");
            }
            List<Long> entitleUserIds = findEntitleUserIds(userId);
            if (!entitleUserIds.contains(agentPartner.getCreateUserId())) {
                return MapMessage.errorMessage("您无权修改此机构！");
            }
        } else {
            List<AgentPartner> existPartner = agentPartnerPersistence.queryByName(params.getName());
            if (CollectionUtils.isNotEmpty(existPartner)) {
                return MapMessage.errorMessage("该机构名称已经存在！");
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 当前用户有权限查看以及操作，创建人为此列表的的合作机构
     *
     * @param userId
     * @return
     */
    private List<Long> findEntitleUserIds(Long userId) {
        List<Long> userIds = new ArrayList<>();
        userIds.add(userId);
        List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        if (CollectionUtils.isNotEmpty(managedGroupIds)) {
            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(managedGroupIds.get(0));
            userIds.addAll(groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList()));
        }
        return userIds;
    }

    public MapMessage upsert(Partner partner) {
        AgentPartner agentPartner = partnerDomain.build(partner);
        agentPartner = agentPartnerPersistence.upsert(agentPartner);
        if (Objects.isNull(partner.getId()) || partner.getId() == 0) {

            partner.setId(agentPartner.getId());
            PartnerMarketer partnerMarketer = PartnerMarketer.Builder.build(partner);
            agentPartnerMarketerPersistence.upsert(PartnerMarketer.Builder.build(partnerMarketer));
        }
        return MapMessage.successMessage();
    }

    /**
     * 根据城市和机构名称查询自己创建的合作伙伴
     *
     * @param regionCode
     * @param name
     * @return
     */
    public List<Partner> searchPartners(Integer regionCode, String name, Long userId) {

        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        Integer cityCode = Objects.isNull(exRegion) ? regionCode : exRegion.getCityCode();

        //获得市场人员合作机构关系
        Map<Long, List<AgentPartnerMarketer>> mPartnerMarketerMap = agentPartnerMarketerPersistence.queryByUseridIds(findEntitleUserIds(userId));
        Set<Long> partnerIdSet = Optional.ofNullable(mPartnerMarketerMap).orElse(new HashMap<>()).values()
                .stream()
                .map(list -> Optional.ofNullable(list).orElse(new ArrayList<>()).stream().map(AgentPartnerMarketer::getPartnerId).collect(Collectors.toSet())
                ).reduce(new HashSet<>(), (all, item) -> {
                    all.addAll(item);
                    return all;
                });
        List<AgentPartner> partners = new ArrayList<>(Optional.ofNullable(agentPartnerPersistence.loads(partnerIdSet)).orElse(new HashMap<>()).values());
        //过滤人名和城市
        partners = partners
                .stream()
                .filter(partner -> StringUtils.isBlank(name) || partner.getName().contains(name) || Objects.equals(NumberUtils.toLong(name), partner.getId()))
                .filter(partner -> Objects.equals(partner.getCityCode(), cityCode))
                .collect(Collectors.toList());

        //获得或者伙伴联系人关系
        List<AgentPartnerLinkMan> linkManList = Optional.ofNullable(agentPartnerLinkManPersistence.queryByPartnerIds(partnerIdSet)).orElse(new HashMap<>())
                .values().stream()
                .reduce(new ArrayList<>(), (all, item) -> {
                    all.addAll(item);
                    return all;
                });
        Map<Long, List<AgentPartnerLinkMan>> linkManMap = Optional.ofNullable(linkManList).orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.groupingBy(AgentPartnerLinkMan::getPartnerId));

        Map<Long, AgentOrgContactPerson> contactPersonMap = agentOrgContractPersonPersistence.loads(
                Optional.ofNullable(linkManList).orElse(new ArrayList<>())
                        .stream().map(AgentPartnerLinkMan::getLinkManId).collect(Collectors.toList()));

        return Optional.ofNullable(partners).orElse(new ArrayList<>())
                .stream()
                .map(agentPartner -> partnerDomain.build(contactPersonMap, linkManMap.get(agentPartner.getId()), agentPartner))
                .collect(Collectors.toList());
    }

    public MapMessage queryBaseById(Long id) {
        AgentPartner agentPartner = agentPartnerPersistence.load(id);
        if (Objects.isNull(agentPartner)) {
            return MapMessage.successMessage("机构不存在！");
        }

        Partner partner = partnerDomain.build(agentPartner);
        PartnerBaseVo vo = PartnerBaseVo.Builder.build(partner);
        return MapMessage.successMessage().add("data", vo);
    }

    public List<Long> findHoneycombIds(List<AgentPartnerLinkMan> linkManList) {
        if (CollectionUtils.isEmpty(linkManList)) {
            return Collections.emptyList();
        }
        List<Long> linkManIds = linkManList.stream().map(AgentPartnerLinkMan::getLinkManId).collect(Collectors.toList());
        Map<Long, AgentOrgContactPerson> personMap = agentOrgContractPersonPersistence.loads(linkManIds);
        return Optional.ofNullable(personMap).orElse(new HashMap<>()).values().stream().map(AgentOrgContactPerson::getHoneycombId).collect(Collectors.toList());
    }

    public MapMessage queryById(Long id) {
        AgentPartner agentPartner = agentPartnerPersistence.load(id);
        if (Objects.isNull(agentPartner)) {
            return MapMessage.successMessage("机构不存在！");
        }

        List<AgentPartnerLinkMan> linkManList = agentPartnerLinkManPersistence.queryByPartnerIds(Collections.singleton(agentPartner.getId())).get(agentPartner.getId());
        Map<Long, AgentOrgContactPerson> contactPersonMap = agentOrgContractPersonPersistence.loads(
                Optional.ofNullable(linkManList).orElse(new ArrayList<>())
                        .stream().map(AgentPartnerLinkMan::getLinkManId).collect(Collectors.toList()));
        Partner partner = partnerDomain.build(contactPersonMap, linkManList, agentPartner);
        LinkMan linkMan = null;
        if (CollectionUtils.isNotEmpty(linkManList)) {
            List<Long> orgContactPersion = new ArrayList<>();
            List<Long> honeycombIds = findHoneycombIds(linkManList);
            Map<Long, List<AgentOrgContactPerson>> lmMap = agentOrgContractPersonPersistence.queryByHoneycombIds(honeycombIds);
            if (MapUtils.isNotEmpty(lmMap)) {
                lmMap.forEach((k, v) -> {
                    if (CollectionUtils.isNotEmpty(v)) {
                        orgContactPersion.addAll(v
                                .stream()
                                .filter(ocp -> Objects.nonNull(ocp.getHoneycombId()) && ocp.getHoneycombId() > 0)
                                .map(AgentOrgContactPerson::getHoneycombId)
                                .collect(Collectors.toList()));
                    }
                });
            }

            Long agentUserId;
            List<AgentPartnerMarketer> partnerMarketers = agentPartnerMarketerPersistence.queryByPartnerId(partner.getId());
            if (CollectionUtils.isEmpty(partnerMarketers)) {
                agentUserId = partner.getCreateUserId();
            } else {
                agentUserId = partnerMarketers.get(0).getUserId();
            }
            List<Long> honeycombUserIds = honeycombUserService.getHoneycombUserIds(agentUserId);
            MapMessage msg = linkManHttpClient.query(honeycombUserIds);
            if (msg.isSuccess()) {
                List<LinkMan> linkMans = (List<LinkMan>) msg.get("data");
                linkMan = Optional.ofNullable(linkMans).orElse(new ArrayList<>())
                        .stream()
                        .filter(lm -> orgContactPersion.contains(lm.getId()))
                        .findFirst().orElse(null);
            }
        }

        PartnerInfoVo vo = PartnerInfoVo.Builder.build(partner, linkMan);
        return MapMessage.successMessage().add("data", vo);
    }

    public MapMessage queryLinkMan(Long partnerId) {
        AgentPartner agentPartner = agentPartnerPersistence.load(partnerId);
        if (Objects.isNull(agentPartner)) {
            return MapMessage.successMessage("机构不存在！");
        }
        List<Long> honeycombUserIds = honeycombUserService.getHoneycombUserIds(agentPartner.getCreateUserId());
        if (CollectionUtils.isEmpty(honeycombUserIds)) {
            return MapMessage.errorMessage("获取联系人失败，请先关联蜂巢账号！");
        }
        MapMessage msg = linkManHttpClient.query(honeycombUserIds);
        if (!msg.isSuccess()) {
            return msg;
        }
        List<LinkMan> linkMans = (List<LinkMan>) msg.get("data");

        List<AgentPartnerMarketer> partnerMarketers = agentPartnerMarketerPersistence.queryByUseridId(agentPartner.getCreateUserId());
        List<AgentPartnerLinkMan> linkManList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(partnerMarketers)) {
            Map<Long, List<AgentPartnerLinkMan>> plMap = agentPartnerLinkManPersistence.queryByPartnerIds(partnerMarketers.stream().map(AgentPartnerMarketer::getPartnerId).collect(Collectors.toList()));
            if (MapUtils.isNotEmpty(plMap)) {
                plMap.forEach((k, v) -> {
                    linkManList.addAll(v);
                });
            }
        }
        Set<Long> linkManIdSet = linkManList
                .stream()
                .map(AgentPartnerLinkMan::getLinkManId)
                .collect(Collectors.toSet());

        Map<Long, AgentOrgContactPerson> personMap = agentOrgContractPersonPersistence.loads(linkManIdSet);
        Set<Long> fansIds;
        if (MapUtils.isEmpty(personMap)) {
            fansIds = new HashSet<>();
        } else {
            fansIds = personMap.values()
                    .stream()
                    .filter(p -> Objects.nonNull(p.getHoneycombId()) && p.getHoneycombId() > 0)
                    .map(AgentOrgContactPerson::getHoneycombId)
                    .collect(Collectors.toSet());
        }

        List<LinkManListVo> vos = Optional.ofNullable(linkMans).orElse(new ArrayList<>())
                .stream()
                .filter(linkMan -> !fansIds.contains(linkMan.getId()))
                .map(linkMan -> LinkManListVo.Builder.build(linkManIdSet, linkMan))
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("dataList", vos);
    }

    public MapMessage insertPaertnerLinkMan(Long linkManId, Long partnerId) {
        if (linkManId == 0 || partnerId == 0) {
            return MapMessage.errorMessage("请选择联系人和合作机构！");
        }
        AgentPartner agentPartner = agentPartnerPersistence.load(partnerId);
        if (Objects.isNull(agentPartner)) {
            return MapMessage.successMessage("机构不存在！");
        }

        Long orgContactPersonId;

        List<AgentOrgContactPerson> linkMans = agentOrgContractPersonPersistence.queryByHoneycombId(linkManId);
        if (CollectionUtils.isNotEmpty(linkMans)) {
            orgContactPersonId = linkMans.get(0).getId();
        } else {
            AgentOrgContactPerson person = new AgentOrgContactPerson();
            person.setHoneycombId(linkManId);
            person.setId(null);
            person = agentOrgContractPersonPersistence.upsert(person);
            orgContactPersonId = person.getId();
        }

        agentPartnerLinkManPersistence.insert(orgContactPersonId, partnerId);
        return MapMessage.successMessage();
    }

    /**
     * 部门负责区域列表
     *
     * @param userId
     * @param regionRank
     * @param isSelectedIds
     * @return
     */
    public MapMessage groupManageRegionList(Long userId, Integer regionRank, List<Integer> isSelectedIds) {
        MapMessage mapMessage = MapMessage.successMessage();
        AgentRegionRank agentRegionRank = AgentRegionRank.rankOf(regionRank);
        if (agentRegionRank == AgentRegionRank.PROVINCE || agentRegionRank == AgentRegionRank.CITY || agentRegionRank == AgentRegionRank.COUNTY) {
            mapMessage = baseOrgService.loadRegionTreeWiderRole(userId, regionRank - 1, new ArrayList<>());

            //选中的省市区
            if (CollectionUtils.isNotEmpty(isSelectedIds)) {
                Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(isSelectedIds);
                if (MapUtils.isNotEmpty(exRegionMap)) {
                    MapMessage isSelectedIdsMapMessage = baseOrgService.createRegionTree(exRegionMap.values(), 3, new ArrayList<>());
                    if (isSelectedIdsMapMessage.isSuccess()) {
                        mapMessage.add("selectedNodeList", (List<NodeStructure>) isSelectedIdsMapMessage.get("nodeList"));
                    }
                }
            }
        }

        return mapMessage;
    }

    /**
     * 迁移蜂巢粉丝和异业机构关系表
     * 逻辑（关系表中id小于500和linkManId大于500，则认为是需要迁移的数据）
     * 使用场景（需求：异业机构过程管理，上线之后仅需执行一次）
     * @return
     */
    public MapMessage mvPartnerLinkmanRef() {
        AtomicInteger count = new AtomicInteger(0);
        List<AgentPartnerLinkMan> allData = agentPartnerLinkManPersistence.query();
        List<AgentPartnerLinkMan> targetData = allData
                .stream()
                .filter(data -> data.getId() < 500)         //前500行数据
                .filter(data -> data.getLinkManId() > 200)  //粉丝id小于500的
                .collect(Collectors.toList());
        List<AgentOrgContactPerson> allPerson = agentOrgContractPersonPersistence.query();
        Map<Long, AgentOrgContactPerson> personMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allPerson)) {
            allPerson.forEach(p -> {
                if (Objects.nonNull(p.getHoneycombId()) && p.getHoneycombId() > 0) {
                    personMap.put(p.getHoneycombId(), p);
                }
            });
        }
        targetData.forEach(data -> {
            Long personId;
            if (!personMap.containsKey(data.getLinkManId())) {
                AgentOrgContactPerson person = new AgentOrgContactPerson();
                person.setHoneycombId(data.getLinkManId());
                person = agentOrgContractPersonPersistence.upsert(person);
                personId = person.getId();
            } else {
                personId = personMap.get(data.getLinkManId()).getId();
            }
            AgentPartnerLinkMan partnerLinkMan = new AgentPartnerLinkMan();
            partnerLinkMan.setPartnerId(data.getPartnerId());
            partnerLinkMan.setLinkManId(personId);
            agentPartnerLinkManPersistence.insert(partnerLinkMan);
            agentPartnerLinkManPersistence.remove(data.getId());
            count.getAndDecrement();
        });
        return MapMessage.successMessage().add("count", count);
    }
}
