/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.AgentSchoolDictData;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.service.user.OrgConfigService;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentDictSchoolDifficultyType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentModifyDictSchoolApplyLoaderClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yaguang.wang on 2016/6/28.
 */

@Controller
@RequestMapping("/sysconfig/schooldic")
@Slf4j
public class AgentSchoolDictConfigController extends AbstractAgentController {
    private final static String SCHOOL_DICT_OPERATORS = "school_dict_operators";
    private final static String IMPORT_SCHOOL_DICT_TEMPLATE = "/config/templates/import_school_dict_template.xlsx";
    //private final static String IMPORT_SCHOOL_PERFORMANCE = "/config/templates/import_school_performance.xlsx";
    private final static String EXPORT_SCHOOL_DICT_TEMPLATE = "/config/templates/export_school_dict_template.xlsx";
    private static final int BYTES_BUFFER_SIZE = 1024 * 8;
    private final Collator pinYinComparator = Collator.getInstance(Locale.CHINA);

    @Inject private RaikouSystem raikouSystem;

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private AgentDictSchoolService agentDictSchoolService;
    //@Inject private AgentSchoolBudgetService agentSchoolBudgetService;

    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentModifyDictSchoolApplyLoaderClient agentModifyDictSchoolApplyLoaderClient;

    @Inject private OrgConfigService orgConfigService;
    @Inject
    private EmailServiceClient emailServiceClient;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;
    @Inject
    private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;


    @RequestMapping(value = "dispose_apply_school.vpage", method = RequestMethod.GET)
    public String disposeApplySchool(Model model) {
        List<AgentModifyDictSchoolApply> agentModifyDictSchoolApplies = agentModifyDictSchoolApplyLoaderClient.findByStatusAndResolved(ApplyStatus.APPROVED, false);
        Set<Long> schoolIds = agentModifyDictSchoolApplies.stream().map(AgentModifyDictSchoolApply::getSchoolId).collect(Collectors.toSet());
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();
        model.addAttribute("data", createDisposeSchool(agentModifyDictSchoolApplies, schoolMap));
        return "/sysconfig/school/disposeapplyschool";
    }

    private List<Map<String, String>> createDisposeSchool(List<AgentModifyDictSchoolApply> schoolApplies, Map<Long, School> schoolMap) {
        List<Map<String, String>> result = new ArrayList<>();
        schoolApplies.forEach(p -> {
            Map<String, String> disposeSchoolApply = new HashMap<>();
            School school = schoolMap.get(p.getSchoolId());
            if (school == null) {
                return;
            }
            disposeSchoolApply.put("id", SafeConverter.toString(p.getId()));
            disposeSchoolApply.put("schoolId", SafeConverter.toString(school.getId()));
            disposeSchoolApply.put("schoolName", school.getCmainName());
            disposeSchoolApply.put("regionName", p.getRegionName());
            disposeSchoolApply.put("phase", SchoolLevel.safeParse(school.getLevel()).getDescription());
            disposeSchoolApply.put("accountName", p.getAccountName());
            disposeSchoolApply.put("modifyType", p.getModifyType() == 1 ? "添加学校" : p.getModifyType() == 2 ? "删除学校" : "业务变更");
            result.add(disposeSchoolApply);
        });
        return result;
    }

    @RequestMapping(value = "dispose_apply_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage disposeApplySchoolRecord(@RequestBody String applyIdsJson) {
        List<Long> applyIds = analysisApplyIdsJson(applyIdsJson);
        Map<Long, AgentModifyDictSchoolApply> agentModifyDictSchoolAppliesMap = agentModifyDictSchoolApplyLoaderClient.findByIds(applyIds);
        if (MapUtils.isEmpty(agentModifyDictSchoolAppliesMap)) {
            return MapMessage.errorMessage("未找到需要操作的学校申请");
        }
        List<AgentModifyDictSchoolApply> agentModifyDictSchoolApplies = new ArrayList<>(agentModifyDictSchoolAppliesMap.values());
        agentModifyDictSchoolApplies = agentModifyDictSchoolApplies.stream().filter(p -> p.getStatus() == ApplyStatus.APPROVED)
                .filter(p -> !SafeConverter.toBoolean(p.getResolved())).collect(Collectors.toList());
        return agentDictSchoolService.disposeApply(agentModifyDictSchoolApplies);
    }

    private List<Long> analysisApplyIdsJson(String applyIdsJson) {
        if (StringUtils.isBlank(applyIdsJson)) {
            return Collections.emptyList();
        }
        return JsonUtils.fromJsonToList(applyIdsJson, Long.class);
    }

    @RequestMapping(value = "schoolDictDetail.vpage", method = RequestMethod.GET)
    @OperationCode("197b84bc5f5c4f70")
    public String schoolDictDetail(Model model) {
        AuthCurrentUser user = getCurrentUser();
        Long schoolId = getRequestLong("schoolId", -1);
        Integer regionCode = getRequestInt("provinces");//这个一定是市一级的区域代码
        Integer cityCode = getRequestInt("citys");
        Integer countyCode = getRequestInt("countys");
        String error = getRequestString("error");
        if (cityCode != 0 && countyCode == -1) {
            Set<Integer> countyCodeList = new HashSet<>();
            List<ExRegion> childRegionList = raikouSystem.getRegionBuffer().loadChildRegions(cityCode);
            if (CollectionUtils.isNotEmpty(childRegionList)) {
                countyCodeList = childRegionList.stream().map(ExRegion::getCountyCode).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(countyCodeList)) {
                    countyCodeList.add(regionCode);
                }
            }
            model.addAttribute("schoolList", agentDictSchoolService.getWrappedSchoolDictDataByRegion(countyCodeList));
        }
        if (countyCode != -1 && countyCode != 0) {
            model.addAttribute("schoolList", agentDictSchoolService.getWrappedSchoolDictDataByRegion(Collections.singleton(countyCode)));
        }
        if (schoolId != -1) {
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
            if (school != null && school.getRegionCode() != null) {
                ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                if (region != null) {
                    regionCode = region.getProvinceCode();
                    cityCode = region.getCityCode();
                    countyCode = region.getCountyCode();
                }
            }
            model.addAttribute("schoolList", agentDictSchoolService.getWrappedSchoolDictDataBySchool(schoolId));
        }
        Map<String, String> conditionMap = new HashMap<>();
        conditionMap.put("provinces", "" + regionCode);
        conditionMap.put("citys", "" + cityCode);
        conditionMap.put("countys", "" + countyCode);
        model.addAttribute("conditionMap", conditionMap);
        model.addAttribute("schoolOperate", checkSchoolDictOperators(user.getUserName()));
        model.addAttribute("provinces", getAllProvincePinYin());
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("error", error);
        }
        return "/sysconfig/school/agentschooldictconfig";
    }

    private List<Map<String, Object>> getAllProvincePinYin() {
        List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadProvinces();
        Set<ExRegion> rt = new TreeSet<>((o1, o2) -> pinYinComparator.compare(o1.getProvinceName(), o2.getProvinceName()));
        rt.addAll(regionList);
        List<Map<String, Object>> provinces = new ArrayList<>();
        for (ExRegion region : rt) {
            Map<String, Object> province = new HashMap<>();
            province.put("key", region.getCode());
            province.put("value", region.getName());
            provinces.add(province);
        }
        return provinces;
    }

    @RequestMapping(value = "regionlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> regionList(@RequestParam Integer regionCode) {
        Map<String, Object> message = new HashMap<>();
        if (regionCode == null) {
            return message;
        }
        List<Region> regionList = new ArrayList<>();
        if (regionCode >= 0) {
            regionList.addAll(raikouSystem.getRegionBuffer().loadChildRegions(regionCode));
        }
        regionList = regionList.stream().filter(p -> StringUtils.isNoneBlank(p.getName())).collect(Collectors.toList());
        Set<Region> rs = new TreeSet<>((o1, o2) -> pinYinComparator.compare(o1.getName(), o2.getName()));
        rs.addAll(regionList);
        message.put("regionList", rs);
        return message;
    }

    private Boolean checkSchoolDictOperators(String userName) {
        if (StringUtils.isBlank(userName)) {
            return false;
        }
        String authOperators = crmConfigService.$loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), SCHOOL_DICT_OPERATORS);
        return authOperators.contains(userName + ",");
    }

    private void createRegionDate(List<AgentGroupRegion> managedRegionList, List<Map<String, Object>> regionDate) {
        if (CollectionUtils.isEmpty(managedRegionList)) {
            return;
        }
        managedRegionList.forEach(p -> {
            if (p == null) {
                return;
            }
            Map<String, Object> date = new HashMap<>();
            date.put("code", p.getRegionCode());
            date.put("name", p.getRegionName());
            regionDate.add(date);
        });
    }

    @RequestMapping(value = "updateSchoolDictInfo.vpage", method = RequestMethod.GET)
    public String updateSchoolDictInfo(Model model) {
        AuthCurrentUser user = getCurrentUser();
        Long dictId = getRequestLong("dictId", -1);
        String error = getRequestString("error");
        try {
            if (!checkSchoolDictOperators(user.getUserName())) {
                return redirect("schoolDictDetail.vpage?error=" + "用户无权操作字典表");
            }
            if (dictId != -1) {
                AgentSchoolDictData schoolData = agentDictSchoolService.getWrappedSchoolDictData(dictId);
                if (schoolData == null) {
                    return redirect("schoolDictDetail.vpage?error=" + "未找到对应的部门信息,字典表信息ID=" + dictId);
                }
                model.addAttribute("schoolData", schoolData);
                model.addAttribute("department", findGroupByRegionCode(schoolData.getRegionCode()));
            }
            model.addAttribute("schoolDifficulty", AgentDictSchoolDifficultyType.viewSchoolDifficulty());
            model.addAttribute("schoolPopularity", AgentSchoolPopularityType.viewSchoolPopularity());
            model.addAttribute("permeability", AgentSchoolPermeabilityType.viewPermeability());
        } catch (Exception ex) {
            logger.error(String.format("get update school dict info is failed dictId =%d", dictId), ex);
            return redirect("schoolDictDetail.vpage?error=" + "未找到对应的部门信息,字典表信息ID=" + dictId);
        }
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("error", error);
        }
        return "/sysconfig/school/editagentschooldict";
    }

    /**
     * 保存学校字典表的信息（增加，修改）
     */
    @RequestMapping(value = "saveSchoolDictInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSchoolDictInfo() {
        AuthCurrentUser user = getCurrentUser();
        if (!checkSchoolDictOperators(user.getUserName())) {
            return MapMessage.errorMessage("您无权操作字典表");
        }
        long dictId = getRequestLong("dictId");
        long schoolId = getRequestLong("schoolId");
        int calPerformance = getRequestInt("calPerformance");
        String schoolDifficulty = getRequestString("schoolDifficulty");
        String schoolPopularity = getRequestString("schoolPopularity");
        String permeability = getRequestString("permeability");

//        long department = getRequestLong("department");
//        if (0L == department){
//            return MapMessage.errorMessage("请选择部门");
//        }

        try {
            if (!AgentDictSchoolService.isAgentDictSchoolDifficultyType(schoolDifficulty)) {
                return MapMessage.errorMessage("任务难度选择错误");
            }
            if (!AgentDictSchoolService.isAgentSchoolPopularityType(schoolPopularity)) {
                return MapMessage.errorMessage("学校等级选择错误");
            }
            if (!AgentDictSchoolService.isAgentSchoolPermeabilityType(permeability)) {
                return MapMessage.errorMessage("学校渗透选择错误");
            }
            if (calPerformance == 0) {
                return MapMessage.errorMessage("请选择该学校是否需要计算业绩");
            }
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(schoolId)
                    .getUninterruptibly();
            if (school == null) {
                return MapMessage.errorMessage("学校ID所对应的学校不存在，学校ID为" + schoolId);
            }
            AgentDictSchool agentDictSchool = new AgentDictSchool();
            agentDictSchool.setCalPerformance(calPerformance == 1);
            agentDictSchool.setSchoolId(schoolId);
            agentDictSchool.setSchoolLevel(school.getLevel());
            if (StringUtils.isNotEmpty(schoolDifficulty)) {
                agentDictSchool.setSchoolDifficulty(AgentDictSchoolDifficultyType.of(schoolDifficulty));
            }
            if (StringUtils.isNotEmpty(schoolPopularity)) {
                agentDictSchool.setSchoolPopularity(AgentSchoolPopularityType.of(schoolPopularity));
            }
            if (StringUtils.isNotEmpty(permeability)) {
                agentDictSchool.setPermeabilityType(AgentSchoolPermeabilityType.of(permeability));
            }
            MapMessage validMsg = validateSchoolDic(agentDictSchool, dictId);
            if (!validMsg.isSuccess()) return validMsg;

            if (dictId == 0L) {
                MapMessage msg = agentDictSchoolService.addAgentDictSchool(agentDictSchool);
                if (msg.isSuccess()) {
//                    orgConfigService.setSchoolForGroup(department, schoolId);
                    asyncLogService.logDictOperation(getCurrentUser(), getRequest().getRequestURI(), "Add schoolDict",
                            "id：" + dictId);
                }
                return msg;
            } else {
                agentDictSchool.setId(dictId);
                MapMessage msg = agentDictSchoolService.updateAgentDictSchool(agentDictSchool);
//                orgConfigService.setSchoolForGroup(department, schoolId);
                if (msg.isSuccess()) {
                    asyncLogService.logDictOperation(getCurrentUser(), getRequest().getRequestURI(), "Modify regionDict",
                            "id：" + dictId);
                }
                return msg;
            }
        } catch (Exception ex) {
            logger.error("save school dict info is failed schoolId=" + schoolId, ex);
            if (dictId == 0L) {
                return MapMessage.errorMessage(String.format("新增字典表失败，学校ID=%d", schoolId));
            } else {
                return MapMessage.errorMessage(String.format("修改字典表信息失败，字典表ID=%d,学校ID=%d", dictId, schoolId));
            }
        }
    }

    private MapMessage validateSchoolDic(AgentDictSchool dictData, Long dictId) {
        StringBuilder msg = new StringBuilder();
        School school = raikouSystem.loadSchoolIncludeDisabled(dictData.getSchoolId());
        if (school == null) {
            msg.append("无效的学校编码！\r\n");
        } else {
            if (school.getDisabled()) {
                msg.append("该学校已失效！\r\n");
            } else if (school.getAuthenticationState().equals(3)) {
                msg.append("该学校已判假！\r\n");
            }
            Integer regionCode = school.getRegionCode();
            if (regionCode == null) {
                msg.append("学校地区无法找到！\r\n");
            }
            ExRegion region = raikouSystem.loadRegion(regionCode);
            if (region == null) {
                msg.append("学校地区无法找到！\r\n");
            } else {
                dictData.setCountyCode(regionCode);
                dictData.setCountyName(ConversionUtils.toString(region.getCityName()) + "--" + ConversionUtils.toString(region.getCountyName()));
            }
        }

        if (dictId == 0L) {
            AgentDictSchool existDict = agentDictSchoolLoaderClient.findBySchoolId(dictData.getSchoolId());
            if (existDict != null) {
                msg.append("已存在一条相同记录的字典表信息！");
            }
        }
        if (msg.length() > 0) {
            return MapMessage.errorMessage(msg.toString());
        }
        return MapMessage.successMessage();
    }


    /**
     * 移除单个字典表信息
     */
    @RequestMapping(value = "removeSchoolDictInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeSchoolDictInfo(@RequestParam Long dictId) {
        try {
            AuthCurrentUser user = getCurrentUser();
            if (!checkSchoolDictOperators(user.getUserName())) {
                return MapMessage.errorMessage("您无权操作字典表");
            }
            int row = agentDictSchoolService.removeSchoolDictData(dictId);
            asyncLogService.logDictOperation(getCurrentUser(), getRequest().getRequestURI(), "Delete schoolDict",
                    "id：" + dictId + " cnt:" + row);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(String.format("remove school dict info is failed dictId=%d", dictId), ex);
            return MapMessage.errorMessage("删除校字典宝数据失败: {}", dictId);
        }
    }

    @RequestMapping(value = "getSchoolDictionaryData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchoolDictionaryData() {
        long schoolId = getRequestLong("schoolId");
        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        try {
            if (schoolId == 0L) {
                return MapMessage.errorMessage("学校ID为空，请重新输入");
            }
            School school = raikouSystem.loadSchoolIncludeDisabled(schoolId);
            if (school == null) {
                return MapMessage.errorMessage(String.format("无法找对该学校ID对应的学校 学校ID:%d", schoolId));
            }

            msg.put("department", findGroupByRegionCode(school.getRegionCode()));
            msg.put("schoolInfo", getSchoolDictionaryDataBySchool(school));
        } catch (Exception ex) {
            return MapMessage.errorMessage("未找到该学校 学校ID" + schoolId);
        }
        return msg;
    }

    private List<AgentGroup> findGroupByRegionCode(Integer regionCode) {

        Set<Integer> regionCodes = new HashSet<>();
        regionCodes.add(regionCode);
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion != null) {
            if (exRegion.fetchRegionType() == RegionType.COUNTY) {
                regionCodes.add(exRegion.getCityCode());
                regionCodes.add(exRegion.getProvinceCode());
            } else if (exRegion.fetchRegionType() == RegionType.CITY) {
                regionCodes.add(exRegion.getProvinceCode());
            }
        }
        Set<Long> groupIds = new HashSet<>();
        regionCodes.forEach(p -> {
            groupIds.addAll(baseOrgService.getGroupRegionByRegion(p).stream().filter(t -> t.getGroupId() != null).map(AgentGroupRegion::getGroupId).collect(Collectors.toList()));
        });

        return baseOrgService.getGroupByIds(groupIds).stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
    }

    /**
     * 通过学校获得学校字典信息
     */
    private AgentSchoolDictData getSchoolDictionaryDataBySchool(School school) {
        AgentSchoolDictData data = new AgentSchoolDictData();
        data.setSchoolName(ConversionUtils.toString(school.getCname() + (school.getDisabled() ? "(已失效)" : school.getAuthenticationState().equals(3) ? "(已判假)" : "")));
        data.setRegionCode(ConversionUtils.toInt(school.getRegionCode()));
        ExRegion region = raikouSystem.loadRegion(ConversionUtils.toInt(school.getRegionCode()));
        if (region != null) {
            data.setRegionName(region.getCityName() + "--" + region.getCountyName() + (region.isDisabledTrue() ? "(已禁用)" : ""));
        }
        data.setSchoolLevel(ConversionUtils.toString(SchoolLevel.safeParse(school.getLevel()).getDescription()));
        //FIXME 增加学校的部门Id查询
        //data.setGroupId();
        return data;
    }

    @RequestMapping(value = "exportSchoolDictPage.vpage", method = RequestMethod.GET)
    public String exportSchoolDictPage() {
        return "/sysconfig/school/exportschooldictpage";
    }

    @RequestMapping(value = "importSchoolDictPage.vpage", method = RequestMethod.GET)
    public String importSchoolDictPage(Model model) {
        AuthCurrentUser user = getCurrentUser();
        String type = getRequestString("type");
        if (!checkSchoolDictOperators(user.getUserName())) {
            return redirect("schoolDictDetail.vpage?error=" + "用户无权操作字典表");
        }
        model.addAttribute("type", type);
        return "/sysconfig/school/importschooldictinfo";
    }

    /**
     * 批量导入字典表信息
     */
    @RequestMapping(value = "bulkImportSchoolDictInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bulkImportSchoolDictInfo() {
        AuthCurrentUser user = getCurrentUser();
        if (!checkSchoolDictOperators(user.getUserName())) {
            return MapMessage.errorMessage("您无权操作字典表");
        }
        XSSFWorkbook workbook = readRequestWorkbook("sourceFile");
        return agentDictSchoolService.importSchoolDictInfo(workbook, getCurrentUser());
    }


    private XSSFWorkbook readRequestWorkbook(String name) {
        HttpServletRequest request = getRequest();
        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return new XSSFWorkbook(in);
        } catch (Exception e) {
            logger.error("readRequestWorkbook - Excp : {}", e);
            return null;
        }
    }

    /**
     * 导出字典表的信息
     */
    @RequestMapping(value = "exportSchoolDictInfo.vpage", method = RequestMethod.GET)
    public void exportSchoolDictInfo(HttpServletResponse response) {
        try {
            String filename = "学校字典表-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";

            List<AgentSchoolDictData> dataList = agentDictSchoolService.getWrappedSchoolDictData();
            //导出Excel文件
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            agentDictSchoolService.exportSchoolDictData(workbook, dataList);
//            XSSFWorkbookExportService xssfWorkbookExportService = new XSSFWorkbookExportServiceImpl(dataList, EXPORT_SCHOOL_DICT_TEMPLATE,false);
//            Workbook workbook = xssfWorkbookExportService.convertToSXSSFWorkbook();
            @Cleanup org.apache.commons.io.output.ByteArrayOutputStream outStream = new org.apache.commons.io.output.ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            workbook.dispose();
        } catch (Exception e) {
            logger.error("error info: ", e);
            emailServiceClient.createPlainEmail()
                    .body("error info: " + e)
                    .subject("学校字典表导出异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("deliang.che@17zuoye.com")
                    .send();
        }
    }

    /**
     * 下载导入模版
     */
    @RequestMapping(value = "importSchoolDictTemplate.vpage", method = RequestMethod.GET)
    public void importSchoolDictTemplate() {
        try {
            Resource resource = new ClassPathResource(IMPORT_SCHOOL_DICT_TEMPLATE);
            if (!resource.exists()) {
                logger.error("download import school dict template - template not exists ");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            String fileName = "学校字典表导入模版.xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download import school dict Template - Excp : {};", e);
        }
    }

    private static void write(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[BYTES_BUFFER_SIZE];
        int size;
        while ((size = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }
}
