package com.voxlearning.utopia.agent.service.organization;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.bean.hierarchicalstructure.NodeStructure;
import com.voxlearning.utopia.agent.constants.AgentUserOperationType;
import com.voxlearning.utopia.agent.constants.ResearchersJobType;
import com.voxlearning.utopia.agent.persist.AgentOrganizationPersistence;
import com.voxlearning.utopia.agent.persist.AgentResearchersPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchers;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOrganization;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.AgentResearchersService;
import com.voxlearning.utopia.agent.service.useroperationrecord.AgentUserOperationRecordService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrganizationType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRegionRank;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class AgentOrganizationService extends AbstractAgentService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private RaikouSystem raikouSystem;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentOrganizationPersistence agentOrganizationPersistence;
    @Inject
    private AgentResearchersPersistence agentResearchersPersistence;
    @Inject
    private AgentOuterResourceService agentOuterResourceService;
    @Inject
    private AgentUserOperationRecordService agentUserOperationRecordService;
    @Inject
    private AgentResearchersService agentResearchersService;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;

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
            mapMessage = baseOrgService.loadRegionTree(userId, regionRank - 1, new ArrayList<>());

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
     * 添加/编辑机构
     *
     * @param id
     * @param name
     * @param regionRank
     * @param regionCode
     * @param provinceCode
     * @param cityCode
     * @param countyCode
     * @param longitude
     * @param latitude
     * @param address
     * @param coordinateType
     * @param photoUrl
     * @param needTransform
     * @return
     */
    public MapMessage saveOrganization(Long id, String name, Integer regionRank, Integer regionCode, Integer provinceCode, Integer cityCode, Integer countyCode, Double longitude, Double latitude, String address, String coordinateType, String photoUrl, Boolean needTransform, String type, String webAddress) {
        AgentOrganization agentOrganization = null;
        //新增
        if (id == null) {
            agentOrganization = new AgentOrganization();
            //编辑
        } else {
            agentOrganization = agentOrganizationPersistence.load(id);
        }
        //省级、市级、区级
        if (regionRank == 2 || regionRank == 3 || regionRank == 4) {
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            if (exRegion == null) {
                return MapMessage.errorMessage("负责区域不存在！");
            }
            //有负地区编码不匹配没法校验    比如  延庆区  110119  撤县设区之前编码  110229
//            if(exRegion.fetchRegionType() == RegionType.PROVINCE){
//                if (!Objects.equals(regionCode, provinceCode)){
//                    return MapMessage.errorMessage("办公地址不在负责区域内！");
//                }
//            }else if (exRegion.fetchRegionType() == RegionType.CITY) {
//                cityCode = countyCode/100*100;  //客户端取到的cityCode 不是地区编码   是区号
//                if (!Objects.equals(regionCode, cityCode)){
//                    return MapMessage.errorMessage("办公地址不在负责区域内！");
//                }
//            } else {
//                if (!Objects.equals(regionCode, countyCode)){
//                    return MapMessage.errorMessage("办公地址不在负责区域内！");
//                }
//            }
            agentOrganization.setProvinceCode(exRegion.getProvinceCode());
            agentOrganization.setProvinceName(exRegion.getProvinceName());

            agentOrganization.setCityCode(exRegion.getCityCode());
            agentOrganization.setCityName(exRegion.getCityName());

            agentOrganization.setCountyCode(exRegion.getCountyCode());
            agentOrganization.setCountyName(exRegion.getCountyName());
        }

        String longitudeFinal;
        String latitudeFinal;
        String addressFinal;

        //坐标转换
        if (needTransform) {
            MapMessage mapAddress = AmapMapApi.getAddress(ConversionUtils.toString(latitude), ConversionUtils.toString(longitude), coordinateType);
            if (!mapAddress.isSuccess()) {
                return mapAddress;
            }
            longitudeFinal = ConversionUtils.toString(mapAddress.get("longitude"));
            latitudeFinal = ConversionUtils.toString(mapAddress.get("latitude"));
            addressFinal = ConversionUtils.toString(mapAddress.get("address"));
        } else {
            longitudeFinal = ConversionUtils.toString(longitude);
            latitudeFinal = ConversionUtils.toString(latitude);
            addressFinal = address;
        }

        agentOrganization.setName(name);
        agentOrganization.setRegionRank(AgentRegionRank.rankOf(regionRank));
        agentOrganization.setLongitude(longitudeFinal);
        agentOrganization.setLatitude(latitudeFinal);
        agentOrganization.setCoordinateType(coordinateType);
        agentOrganization.setAddress(addressFinal);
//        List<String> photoUrls = new ArrayList<>();
//        photoUrls.add(photoUrl);
        agentOrganization.setPhotoUrl(photoUrl);
        agentOrganization.setDisabled(false);
        agentOrganization.setType(AgentOrganizationType.nameOf(type));
        agentOrganization.setWebAddress(webAddress);
        agentOrganization.setOrgType(1);
        //新增
        if (id == null) {
            agentOrganizationPersistence.insert(agentOrganization);
            //操作日志
            addOrEditOrganizationOperationRecord(agentOrganization, "add");
            //编辑
        } else {
            //操作日志
            addOrEditOrganizationOperationRecord(agentOrganization, "edit");
            agentOrganizationPersistence.replace(agentOrganization);
        }
        return MapMessage.successMessage().add("id", agentOrganization.getId());
    }

    /**
     * 组织列表
     *
     * @param regionCode
     * @param name
     * @param longitude
     * @param latitude
     * @param coordinateType
     * @return
     */
    public List<Map<String, Object>> organizationList(Integer regionCode, String name, Double longitude, Double latitude, String coordinateType) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        //坐标转化
        MapMessage mapAddress = AmapMapApi.getAddress(ConversionUtils.toString(latitude), ConversionUtils.toString(longitude), coordinateType);
        if (!mapAddress.isSuccess()) {
            return dataList;
        }
        String longitudeStr = ConversionUtils.toString(mapAddress.get("longitude"));
        String latitudeStr = ConversionUtils.toString(mapAddress.get("latitude"));

        List<AgentOrganization> organizationList = getListByRegionCode(regionCode);
        if (StringUtils.isNotBlank(name)) {
            organizationList = organizationList.stream().filter(p -> StringUtils.contains(p.getName(), name)).collect(Collectors.toList());
        }
        return transformOrgList(organizationList, longitudeStr, latitudeStr, coordinateType);
    }


    /**
     * 根据区域编码获取列表 不包含学校机构
     *
     * @param regionCode
     * @return
     */
    public List<AgentOrganization> getListByRegionCode(Integer regionCode) {
        List<AgentOrganization> organizationList = new ArrayList<>();
        Integer cityCode = 0;
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion != null) {
            if (exRegion.fetchRegionType() == RegionType.CITY) {
                cityCode = regionCode;
            } else if (exRegion.fetchRegionType() == RegionType.COUNTY) {
                cityCode = exRegion.getCityCode();
            }
            //省级机构列表
            Integer provinceCode = exRegion.getProvinceCode();
            List<AgentOrganization> provinceOrgList = agentOrganizationPersistence.loadByProvinceCode(provinceCode).stream().filter(p -> p.getRegionRank() == AgentRegionRank.PROVINCE).collect(Collectors.toList());

            //国家级机构列表
            List<AgentOrganization> countryOrgList = agentOrganizationPersistence.loadByRegionRank(AgentRegionRank.COUNTRY);

            organizationList.addAll(provinceOrgList);
            organizationList.addAll(countryOrgList);
            //市级+区级
            organizationList.addAll(agentOrganizationPersistence.loadByCityCode(cityCode));
        }
        return organizationList;
    }

    /**
     * 根据区域编码获取列表  包含根据学校创建的机构
     *
     * @param provinceCodes
     * @param cityCodes
     * @param countyCodes
     * @return List
     */
    public List<AgentOrganization> getListByRegionCodes(Collection<Integer> provinceCodes, Collection<Integer> cityCodes, Collection<Integer> countyCodes) {
        List<AgentOrganization> organizationList = new ArrayList<>();
        List<AgentOrganization> regionCodesOrgList = agentOrganizationPersistence.loadByRegionCodes(provinceCodes, cityCodes, countyCodes);
        //国家级机构列表
        List<AgentOrganization> countryOrgList = agentOrganizationPersistence.loadByRegionRank(AgentRegionRank.COUNTRY);
        if (CollectionUtils.isNotEmpty(regionCodesOrgList)) {
            organizationList.addAll(regionCodesOrgList);
        }
        if (CollectionUtils.isNotEmpty(countryOrgList)) {
            organizationList.addAll(countryOrgList);
        }
        return organizationList;
    }

    /**
     * 机构详情
     *
     * @param id
     * @return
     */
    public MapMessage organizationDetail(Long id) {
        AgentOrganization organization = agentOrganizationPersistence.load(id);
        if (organization == null) {
            return MapMessage.errorMessage("机构不存在！");
        }
        return MapMessage.successMessage().add("dataMap", organization);
    }

    /**
     * 机构资源列表
     *
     * @param id
     * @param sortType
     * @return
     */
    public List<Map<String, Object>> organizationResourceList(Long id, Integer sortType) {
        AgentOrganization organization = agentOrganizationPersistence.load(id);
        List<Map<String, Object>> dataList = agentOuterResourceService.assembleList(Collections.singleton(organization), null);
        if (sortType == 1) {
            dataList = dataList.stream().sorted(Comparator.comparing(p -> p.get("name") == null ? "" : p.get("name"), Collator.getInstance(Locale.CHINA))).collect(Collectors.toList());
        } else if (sortType == 2) {
            dataList = dataList.stream().sorted((o1, o2) -> {
                if (o1.get("visitTime") == null && o2.get("visitTime") != null) {
                    return 1;
                } else if (o1.get("visitTime") != null && o2.get("visitTime") == null) {
                    return -1;
                } else if (o1.get("visitTime") == null && o2.get("visitTime") == null) {
                    return 0;
                } else {
                    return o1.get("visitTime").toString().compareTo(o2.get("visitTime").toString());
                }
            }).collect(Collectors.toList());
        }
        return dataList;
    }

    /**
     * 修改机构操作记录
     *
     * @param organization
     * @param flag
     */
    public void addOrEditOrganizationOperationRecord(AgentOrganization organization, String flag) {
        //记录操作日志
        String organizationOperationContent = "";
        //编辑
        if (flag.equals("edit")) {
            AgentOrganization organizationOld = agentOrganizationPersistence.load(organization.getId());
            if (organizationOld != null) {
                if (!Objects.equals(organizationOld.getName(), organization.getName())) {
                    organizationOperationContent += "机构名称，从 " + (organizationOld.getName() != null ? organizationOld.getName() : "空") + " 变更到 " + (organization.getName() != null ? organization.getName() : "空") + "；";
                }
                if (organizationOld.getRegionRank() != organization.getRegionRank()) {
                    organizationOperationContent += "级别，从 " + (organizationOld.getRegionRank().getRankName() != null ? organizationOld.getRegionRank().getRankName() : "空") + " 变更到 " + (organization.getRegionRank().getRankName() != null ? organization.getRegionRank().getRankName() : "空") + "；";
                }
                if (!Objects.equals(organizationOld.getProvinceName(), organization.getProvinceName()) || !Objects.equals(organizationOld.getCityName(), organization.getCityName()) || !Objects.equals(organizationOld.getCountyName(), organization.getCountyName())) {
                    organizationOperationContent += "负责区域，从 " + ((organizationOld.getProvinceName() == null && organizationOld.getCityName() == null && organizationOld.getCountyName() == null) ? "空" : (organizationOld.getProvinceName() + organizationOld.getCityName() + organizationOld.getCountyName()))
                            + " 变更到 " + ((organization.getProvinceName() == null && organization.getCityName() == null && organization.getCountyName() == null) ? "空" : (organization.getProvinceName() + organization.getCityName() + organization.getCountyName())) + "；";
                }
                if (!Objects.equals(organizationOld.getAddress(), organization.getAddress())) {
                    organizationOperationContent += "办公地址，从 " + (organizationOld.getAddress() != null ? organizationOld.getAddress() : "空") + " 变更到 " + (organization.getAddress() != null ? organization.getAddress() : "空") + "；";
                }
            }
            //新增
        } else if (flag.equals("add")) {
            organizationOperationContent += "添加机构，机构ID：" + organization.getId() + "；";
        }
        //操作日志
        agentUserOperationRecordService.addOperationRecord(SafeConverter.toString(organization.getId()), AgentUserOperationType.ORGANIZATION_INFO, organizationOperationContent);
    }

    public MapMessage recommendResourceId(Long id) {
        MapMessage mapMessage = MapMessage.successMessage();
        AgentOrganization organization = agentOrganizationPersistence.load(id);
        if (organization == null) {
            return MapMessage.errorMessage("机构不存在！");
        }
        List<AgentResearchers> researchers;
        if (organization.getType() == AgentOrganizationType.STAFF_ROOM || organization.getType() == AgentOrganizationType.EDUCATION_BUREAU || organization.getType() == AgentOrganizationType.EDUCATION_COMMISSION) {
            researchers = agentResearchersPersistence.findListByRegionCode(organization.getProvinceCode(), organization.getCityCode(), organization.getCountyCode()).stream().filter(p -> p.getJob() == 1).collect(Collectors.toList());
        } else {
            researchers = agentResearchersPersistence.findListByRegionCode(organization.getProvinceCode(), organization.getCityCode(), organization.getCountyCode()).stream().filter(p -> p.getJob() != 1).collect(Collectors.toList());
        }

        if (organization.getRegionRank().getId() == 2) {
            researchers = researchers.stream().filter(p -> p.getLevel() == 1).collect(Collectors.toList());
        } else if (organization.getRegionRank().getId() == 3) {
            researchers = researchers.stream().filter(p -> p.getLevel() == 2).collect(Collectors.toList());
        } else if (organization.getRegionRank().getId() == 4) {
            researchers = researchers.stream().filter(p -> p.getLevel() == 3).collect(Collectors.toList());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        researchers.forEach(p -> {
            Map<String, Object> data = new HashMap<>();
            data.put("name", p.getName());
            data.put("id", p.getId());
            data.put("gender", p.getGender());
            data.put("subject", p.getSubject() == null ? 0 : p.getSubject().getId());
            data.put("subjectName", p.getSubject() == null ? 0 : p.getSubject().getValue());
            data.put("job", p.getJob());
            ResearchersJobType jobType = ResearchersJobType.typeOf(p.getJob());
            data.put("jobName", jobType == null ? "" : jobType.getJobName());
            result.add(data);
        });
        return mapMessage.add("dataList", result);
    }

    /**
     * 资源页搜索机构
     *
     * @param name
     * @param longitude
     * @param latitude
     * @param coordinateType
     * @return
     */
    public List<Map<String, Object>> searchOrganization(String name, Double longitude, Double latitude, String coordinateType) {

        //坐标转化
        MapMessage mapAddress = AmapMapApi.getAddress(ConversionUtils.toString(latitude), ConversionUtils.toString(longitude), coordinateType);
        if (!mapAddress.isSuccess()) {
            return Collections.emptyList();
        }
        String longitudeStr = ConversionUtils.toString(mapAddress.get("longitude"));
        String latitudeStr = ConversionUtils.toString(mapAddress.get("latitude"));
        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        Map<String, Set<Integer>> regionMap = agentResearchersService.getRegionCodes(getCurrentUserId(), roleType);
        Set<Integer> provinceCodes = regionMap.get("province");
        Set<Integer> cityCodes = regionMap.get("city");
        Set<Integer> countyCodes = regionMap.get("county");
        List<AgentOrganization> organizationList = getListByRegionCodes(provinceCodes, cityCodes, countyCodes)
                .stream().filter(p -> p.getOrgType() == 1).collect(Collectors.toList());
        if (StringUtils.isNotBlank(name)) {
            organizationList = organizationList.stream().filter(p -> StringUtils.contains(p.getName(), name)).collect(Collectors.toList());
        }
        return transformOrgList(organizationList, longitudeStr, latitudeStr, coordinateType);
    }

    public List<Map<String, Object>> transformOrgList(List<AgentOrganization> organizationList, String longitudeStr, String latitudeStr, String coordinateType) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        organizationList.forEach(item -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("id", item.getId());
            dataMap.put("name", item.getName());
            dataMap.put("address", item.getAddress());
            //计算坐标距离
            String distanceStr;
            MapMessage msg = AmapMapApi.GetDistance(longitudeStr, latitudeStr, coordinateType, item.getLongitude(), item.getLatitude(), item.getCoordinateType());
            if (msg.isSuccess() && msg.get("res") != null && ConversionUtils.toLong(msg.get("res")) >= 0) {
                long distance = ConversionUtils.toLong(msg.get("res"));
                if (distance >= 1000) {
                    distanceStr = StringUtils.formatMessage("{}km", MathUtils.doubleDivide(distance, 1000, 1));
                } else {
                    distanceStr = StringUtils.formatMessage("{}m", distance);
                }
            } else {
                distanceStr = "未知";
            }
            dataMap.put("distance", distanceStr);
            dataList.add(dataMap);
        });
        return dataList;
    }

    /**
     * 添加学校机构
     *
     * @return
     */
    public MapMessage createSchoolOrganization(Long schoolId) {
        AgentOrganization agentOrganization = agentOrganizationPersistence.loadBySchoolIdAndOrgType(schoolId);
        if (agentOrganization != null) {
            return MapMessage.successMessage().add("org", agentOrganization);
        }
        agentOrganization = new AgentOrganization();
        // 获取学校基本信息
        School school = raikouSystem.loadSchool(schoolId);
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在！");
        }
        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
        if (exRegion == null) {
            return MapMessage.errorMessage("学校地区编码不正确！");
        }
        agentOrganization.setProvinceCode(exRegion.getProvinceCode());
        agentOrganization.setProvinceName(exRegion.getProvinceName());

        agentOrganization.setCityCode(exRegion.getCityCode());
        agentOrganization.setCityName(exRegion.getCityName());

        agentOrganization.setCountyCode(exRegion.getCountyCode());
        agentOrganization.setCountyName(exRegion.getCountyName());
        agentOrganization.setSchoolId(schoolId);
        agentOrganization.setOrgType(2);
        agentOrganization.setName(school.getCname());
        agentOrganization.setRegionRank(AgentRegionRank.COUNTY);
        if (schoolExtInfo != null) {
            agentOrganization.setLongitude(schoolExtInfo.getLongitude());
            agentOrganization.setLatitude(schoolExtInfo.getLatitude());
            agentOrganization.setCoordinateType(schoolExtInfo.getCoordinateType());
            agentOrganization.setAddress(schoolExtInfo.getAddress());
            agentOrganization.setPhotoUrl(schoolExtInfo.getPhotoUrl());
        }
        agentOrganization.setDisabled(false);
        agentOrganization.setType(AgentOrganizationType.OTHER);
//        agentOrganization.setWebAddress(webAddress);
//        addOrEditOrganizationOperationRecord(agentOrganization,"add");
        agentOrganizationPersistence.insert(agentOrganization);
        return MapMessage.successMessage().add("org", agentOrganization);
    }

    public MapMessage checkNameRepeat(Long id, String name) {
        List<AgentOrganization> nameList = agentOrganizationPersistence.loadByName(name);
        if (CollectionUtils.isEmpty(nameList)) {
            return MapMessage.successMessage();
        }
        if (id != null && id > 0) {
            nameList = nameList.stream().filter(p -> !Objects.equals(id, p.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(nameList)) {
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("机构名称重复").add("id", nameList.get(0).getId()).add("nameRepeat", true);
            }
        } else {
            return MapMessage.errorMessage("机构名称重复").add("id", nameList.get(0).getId()).add("nameRepeat", true);
        }
    }
}
