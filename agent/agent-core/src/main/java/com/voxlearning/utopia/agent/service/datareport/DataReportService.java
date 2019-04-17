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

package com.voxlearning.utopia.agent.service.datareport;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.athena.LoadSchoolServiceClient;
import com.voxlearning.utopia.agent.athena.LoadTeacherServiceClient;
import com.voxlearning.utopia.agent.bean.SchoolDepartmentInfo;
import com.voxlearning.utopia.agent.bean.datareport.ChoiceContent;
import com.voxlearning.utopia.agent.bean.datareport.SchoolKlxEverydayScanData;
import com.voxlearning.utopia.agent.bean.datareport.TeacherKlxEverydayScanData;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.agent.controller.datareport.O2oEveryDayScanDetailController;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.user.OrgConfigService;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.TeacherRoleCategory;
import com.voxlearning.utopia.service.user.api.constants.TeacherRolesType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.client.TeacherRolesServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 数据报表
 * Created by yaguang.wang on 2016/10/9.
 */
@Named
public class DataReportService extends AbstractAgentService {
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private OrgConfigService orgConfigService;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private LoadSchoolServiceClient loadSchoolServiceClient;
    @Inject
    private LoadTeacherServiceClient loadTeacherServiceClient;
//    @Inject
//    private AgentDictSchoolService agentDictSchoolService;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;

    @Inject
    private TeacherRolesServiceClient teacherRolesServiceClient;
    @Inject
    private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;
    @Inject
    private BaseDictService baseDictService;


    private final static String OTHER_BD_NAME = "其他";
    private final static List<Integer> DEFAULT_SCAN_DATA;
    public static final String SCHOOL_DATA_TYPE = "school";
    public static final String TEACHER_DATA_TYPE = "teacher";

    static {
        DEFAULT_SCAN_DATA = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            DEFAULT_SCAN_DATA.add(0);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 这里是获取每日扫描量的接口，接口的参数包括类型和需要的日期。
    ///////////////////////////////////////////////////////////////////////////
    public List<ExportAble> loadEveryDayScanDetail(String type, Date startDate) {
        if (type == null || startDate == null) {
            return Collections.emptyList();
        }
        List<Long> manageSchoolIds = baseDictService.loadAllSchoolDictData()
                .stream()
                .filter(AgentDictSchool::isKlxModeSchool)
                .map(AgentDictSchool::getSchoolId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(manageSchoolIds)) {
            return Collections.emptyList();
        }
        MapMessage result = MapMessage.errorMessage();
        if (Objects.equals(type, O2oEveryDayScanDetailController.SCHOOL_SCAN_TYPE)) {
            result = loadSchoolServiceClient.getDownLoadSchoolService().LoadKlxSchoolSanDetail(manageSchoolIds, getIntMonth(startDate));
        }
        if (Objects.equals(type, O2oEveryDayScanDetailController.TEACHER_SCAN_TYPE)) {
            result = loadTeacherServiceClient.getDownLoadTeacherService().LoadKlxTeacherSanDetail(manageSchoolIds, getIntMonth(startDate));
        }
        if (!result.isSuccess()) {
            return Collections.emptyList();
        }
        List<Object> dataList = new ArrayList<>();
        if (Objects.equals(type, TEACHER_DATA_TYPE)) {
            // FIXME 老师的Key
            dataList = (List<Object>) result.get("LoadKlxTeacherSanDetail");
        }
        if (Objects.equals(type, SCHOOL_DATA_TYPE)) {
            // FIXME 学校的Key
            dataList = (List<Object>) result.get("LoadKlxSchoolSanDetail");
        }
        if (dataList == null) {
            return Collections.emptyList();
        }
        Map<Long, List<Map<String, Object>>> schoolScanData = new HashMap<>();
        if (Objects.equals(type, SCHOOL_DATA_TYPE)) {
            for (Object data : dataList) {
                Map<String, Object> oneReport = (Map<String, Object>) data;
                Long schoolId = SafeConverter.toLong(oneReport.get("schoolId"));
                if (schoolScanData.containsKey(schoolId)) {
                    schoolScanData.get(schoolId).add(oneReport);
                } else {
                    List<Map<String, Object>> schoolScanDataInfo = new ArrayList<>();
                    schoolScanDataInfo.add(oneReport);
                    schoolScanData.put(schoolId, schoolScanDataInfo);
                }
            }
        }

        Map<Long, List<Map<String, Object>>> teacherScanData = new HashMap<>();
        if (Objects.equals(type, TEACHER_DATA_TYPE)) {
            for (Object data : dataList) {
                Map<String, Object> oneReport = (Map<String, Object>) data;
                Long teacherId = SafeConverter.toLong(oneReport.get("teacherId"));
                if (teacherScanData.containsKey(teacherId)) {
                    teacherScanData.get(teacherId).add(oneReport);
                } else {
                    List<Map<String, Object>> reportList = new ArrayList<>();
                    reportList.add(oneReport);
                    teacherScanData.put(teacherId, reportList);
                }
            }
        }
        Map<Long, School> school = schoolLoaderClient.getSchoolLoader()
                .loadSchools(manageSchoolIds)
                .getUninterruptibly();
        Map<Long, SchoolDepartmentInfo> schoolDepartmentInfoMap = loadDepartmentInfoBySchool(manageSchoolIds);
        List<ExportAble> exportAbleResult = new ArrayList<>();
        if (Objects.equals(type, SCHOOL_DATA_TYPE)) {

            new HashSet<>(manageSchoolIds).forEach(p -> {
                if (p != null && schoolDepartmentInfoMap.get(p) != null && school.get(p) != null) {
                    exportAbleResult.addAll(createSchoolEverydayScanData(schoolDepartmentInfoMap.get(p), schoolScanData.get(p), school.get(p), startDate));
                }
            });
        }
        if (Objects.equals(type, TEACHER_DATA_TYPE)) {
            List<CrmTeacherSummary> allTeacherList = new ArrayList<>();
            manageSchoolIds.forEach(p -> allTeacherList.addAll(crmSummaryLoaderClient.loadSchoolTeachers(p)));
            List<CrmTeacherSummary> klxTeacherList = allTeacherList.stream().filter(CrmTeacherSummary::isKlxTeacher).collect(toList());
            Map<Long, Long> teacherSchool = klxTeacherList.stream().collect(Collectors.toMap(CrmTeacherSummary::getTeacherId, CrmTeacherSummary::getSchoolId, (o1, o2) -> o1));
            klxTeacherList.forEach(p -> {
                if (p != null && p.getTeacherId() != null && teacherSchool.containsKey(p.getTeacherId()) && school.containsKey(teacherSchool.get(p.getTeacherId())) && schoolDepartmentInfoMap.containsKey(teacherSchool.get(p.getTeacherId()))
                        && schoolDepartmentInfoMap.get(teacherSchool.get(p.getTeacherId())) != null && school.get(teacherSchool.get(p.getTeacherId())) != null) {
                    exportAbleResult.addAll(createTeacherEverydayScanData(p, school.get(teacherSchool.get(p.getTeacherId())), schoolDepartmentInfoMap.get(teacherSchool.get(p.getTeacherId())), teacherScanData.get(p.getTeacherId()), startDate));
                }
            });
        }
        return exportAbleResult;
    }

    private List<SchoolKlxEverydayScanData> createSchoolEverydayScanData(SchoolDepartmentInfo schoolDepartmentInfo, List<Map<String, Object>> dataMap, School school, Date startDate) {
        List<SchoolKlxEverydayScanData> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dataMap)) {
            resultList.add(createSchoolKlxEverydayScanData(schoolDepartmentInfo, null, school, startDate));
        } else {
            dataMap.forEach(p -> {
                resultList.add(createSchoolKlxEverydayScanData(schoolDepartmentInfo, p, school, startDate));
            });
        }
        return resultList;
    }

    private SchoolKlxEverydayScanData createSchoolKlxEverydayScanData(SchoolDepartmentInfo schoolDepartmentInfo, Map<String, Object> dataMap, School school, Date startDate) {
        SchoolKlxEverydayScanData result = new SchoolKlxEverydayScanData();
        result.setTime(SafeConverter.toString(getIntMonth(startDate)));
        result.setRegionName(schoolDepartmentInfo.getRegionGroupName());
        result.setCityName(schoolDepartmentInfo.getGroupName());
        result.setCountyName(schoolDepartmentInfo.getRegionName());
        result.setBusinessDeveloper(schoolDepartmentInfo.getBusinessDeveloperName());
        // 学校信息
        result.setSchoolId(school.getId());
        result.setSchoolName(school.getCmainName());
        result.setSchoolPhase(SchoolLevel.safeParse(school.getLevel(), null) != null ? SchoolLevel.safeParse(school.getLevel(), null).getDescription() : "");
        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(school.getId());
        if (agentDictSchool != null) {
            result.setSchoolLevel(null == agentDictSchool.getSchoolPopularity() ? null : agentDictSchool.getSchoolPopularity().getLevel());
        }
        if (dataMap != null) {
            result.setSubject(Subject.ofWithUnknown(SafeConverter.toString(dataMap.get("subject"))).getValue());
            result.setScanData(dataMap.get("scanNum") == null ? null : (List<Integer>) dataMap.get("scanNum"));
        } else {
            result.setSubject(Subject.MATH.getValue()); // 默认科目为数学
            result.setScanData(DEFAULT_SCAN_DATA);
        }
        return result;
    }

    private List<TeacherKlxEverydayScanData> createTeacherEverydayScanData(CrmTeacherSummary teacher, School school, SchoolDepartmentInfo departmentInfo, List<Map<String, Object>> reportList, Date startDate) {
        List<TeacherKlxEverydayScanData> result = new ArrayList<>();

        boolean isBankManager = teacherRolesServiceClient.hasRole(teacher.getTeacherId(), school.getId(), TeacherRoleCategory.O2O.name(), TeacherRolesType.SCHOOL_BANK_MANAGER.name());

        boolean isSubjectLeader = teacherRolesServiceClient.hasRole(teacher.getTeacherId(), school.getId(), TeacherRoleCategory.O2O.name(), TeacherRolesType.SUBJECT_LEADER.name());
        if (CollectionUtils.isEmpty(reportList)) {
            TeacherKlxEverydayScanData scanData = new TeacherKlxEverydayScanData();
            scanData.setTime(SafeConverter.toString(getIntMonth(startDate)));
            scanData.setRegionName(departmentInfo.getRegionGroupName());
            scanData.setCityName(departmentInfo.getGroupName());
            scanData.setCountyName(departmentInfo.getRegionName());
            scanData.setBusinessDeveloper(departmentInfo.getBusinessDeveloperName());
            scanData.setSchoolId(school.getId());
            scanData.setSchoolName(school.getCname());
            scanData.setSchoolPhase(SchoolLevel.safeParse(school.getLevel(), null) != null ? SchoolLevel.safeParse(school.getLevel(), null).getDescription() : "");
            AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(school.getId());
            if (agentDictSchool != null) {
                scanData.setSchoolLevel(null == agentDictSchool.getSchoolPopularity() ? null : agentDictSchool.getSchoolPopularity().getLevel());
            }
            scanData.setTeacherId(teacher.getTeacherId());
            scanData.setTeacherName(teacher.getRealName());

            scanData.setIsQbManager(isBankManager);
            scanData.setIsSubjectLeader(isSubjectLeader);
            scanData.setSubject(Subject.MATH.getValue());
            scanData.setScanData(DEFAULT_SCAN_DATA); // 默认科目为数学
            result.add(scanData);
            return result;
        }
        reportList.forEach(p -> {
            TeacherKlxEverydayScanData scanData = new TeacherKlxEverydayScanData();
            scanData.setTime(SafeConverter.toString(getIntMonth(startDate)));
            scanData.setRegionName(departmentInfo.getRegionGroupName());
            scanData.setCityName(departmentInfo.getGroupName());
            scanData.setCountyName(departmentInfo.getRegionName());
            scanData.setBusinessDeveloper(departmentInfo.getBusinessDeveloperName());
            scanData.setSchoolId(school.getId());
            scanData.setSchoolName(school.getCname());
            scanData.setSchoolPhase(SchoolLevel.safeParse(school.getLevel(), null) != null ? SchoolLevel.safeParse(school.getLevel(), null).getDescription() : "");
            AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(school.getId());
            if (agentDictSchool != null) {
                scanData.setSchoolLevel(null == agentDictSchool.getSchoolPopularity() ? null : agentDictSchool.getSchoolPopularity().getLevel());
            }
            scanData.setTeacherId(teacher.getTeacherId());
            scanData.setTeacherName(teacher.getRealName());
            scanData.setIsQbManager(isBankManager);
            scanData.setIsSubjectLeader(isSubjectLeader);
            scanData.setSubject(Subject.ofWithUnknown(SafeConverter.toString(p.get("subject"))).getValue());
            scanData.setScanData(p.get("scanNum") == null ? null : (List<Integer>) p.get("scanNum"));
            result.add(scanData);
        });
        return result;
    }

    private Integer getIntMonth(Date date) {
        return SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMM"));
    }


    public List<Long> getUserManageSchoolIds(Long userId) {
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        List<Long> manageSchoolIds = baseOrgService.getManagedSchoolList(userId);
        if (userRole == AgentRoleType.CityManager) {
            List<AgentGroupUser> groupUser = baseOrgService.getGroupUserByUser(userId);
            if (CollectionUtils.isEmpty(groupUser)) {
                return Collections.emptyList();
            }
            List<Long> bdIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupUser.get(0).getGroupId(), AgentRoleType.BusinessDeveloper.getId());
            Set<Long> schoolIds = baseOrgService.getUserSchoolByUsers(bdIds).values().stream().flatMap(List::stream).map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
            manageSchoolIds = manageSchoolIds.stream().filter(p -> !schoolIds.contains(p)).collect(toList());
        }
        return manageSchoolIds;
    }

    private Map<Long, SchoolDepartmentInfo> loadDepartmentInfoBySchool(Collection<Long> manageSchoolId) {
        return orgConfigService.loadDepartmentInfoBySchool(manageSchoolId);
    }

    // 获取指定部门默认市场部下的部门\
    public List<ChoiceContent> loadDepartmentList(Long groupId) {
        return loadDepartmentList(groupId, null);
    }

    public List<ChoiceContent> loadDepartmentList(Long groupId, String type) {
        List<AgentGroup> agentGroupUserList;
        if (groupId == null) {
            AgentGroup group = baseOrgService.getGroupByName("市场部");
            agentGroupUserList = baseOrgService.getGroupListByParentId(group.getId());
        } else {
            agentGroupUserList = baseOrgService.getGroupListByParentId(groupId);
        }
        if (Objects.equals(type, "school")) {
            AgentGroup parentGroup = new AgentGroup();
            parentGroup.setId(groupId);
            parentGroup.setGroupName("全部地区");
            agentGroupUserList.add(parentGroup);

        }
        return agentGroupUserList.stream().map(this::createChoiceContent).collect(Collectors.toList());
    }

    // 获取指定部门下的专员
    public List<ChoiceContent> loadDepartmentUserList(Long groupId) {
        List<ChoiceContent> result = new ArrayList<>();
        List<AgentGroupUser> users = baseOrgService.getGroupUserByGroup(groupId);
        Set<Long> userIds = users.stream().filter(p -> p.getUserRoleType() == AgentRoleType.BusinessDeveloper).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        Set<Long> cityManagerId = users.stream().filter(p -> p.getUserRoleType() == AgentRoleType.CityManager).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        List<AgentUser> bdUser = baseOrgService.getUsers(userIds);

        List<AgentUser> cityManager = baseOrgService.getUsers(cityManagerId);
        result.addAll(bdUser.stream().map(this::createChoiceContent).collect(Collectors.toList()));
        cityManager.forEach(p -> {
            List<Long> manageSchool = getUserManageSchoolIds(p.getId());
            if (CollectionUtils.isNotEmpty(manageSchool)) {
                ChoiceContent content = new ChoiceContent();
                content.setId(p.getId());
                content.setName(OTHER_BD_NAME);
                result.add(content);
            }
        });
        return result;
    }

//
//    public <T extends SchoolBaseReportData> Map<Long, T> gengeralBaseSchoolReportData(Collection<Long> schoolIds, Integer day, Class<T> tClass) {
//        Map<Long, T> resultMap = new HashMap<>();
//        List<AgentDictSchool> dictSchoolList = agentDictSchoolService.loadSchoolDictDataBySchool(schoolIds);
//        Map<Long, CrmSchoolSummary> crmSchoolSummaryMap = agentDictSchoolService.batchLoadCrmSchoolSummary(schoolIds);
//        Map<Long, SchoolExtInfo> schoolExtInfoMap = batchLoadSchoolsExtInfoData(schoolIds);
//
//        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader().loadSchools(schoolIds).getUninterruptibly();
//
//        Map<String, AgentUser> allUsersMap = baseUserService.getAllAgentUsers();
//        List<AgentGroupUser> allGroupUsers = agentGroupUserLoaderClient.findAll();
//        // 获取市经理信息
//        List<AgentGroupUser> cityManageUsers = allGroupUsers.stream().filter(p -> Objects.equals(p.getUserRoleType(), AgentRoleType.CityManager)).collect(Collectors.toList());
//        Map<Long, Long> cityManageUsersMap = cityManageUsers.stream().collect(Collectors.toMap(AgentGroupUser::getGroupId, AgentGroupUser::getUserId, (o1, o2) -> o1));
//
//        // 获取所有专员信息
//        List<AgentGroupUser> businessDeveloperUsers = allGroupUsers.stream().filter(p -> Objects.equals(p.getUserRoleType(), AgentRoleType.BusinessDeveloper)).collect(Collectors.toList());
//        Map<Long, AgentGroupUser> businessDeveloperUserMap = businessDeveloperUsers.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, Function.identity(), (o1, o2) -> o1));
//
//        // 获取部门数据
//        Map<Long, AgentGroup> allGroupMap = agentGroupLoaderClient.findAllGroups().stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
//
//        List<AgentUserSchool> agentUserSchools = batchLoadUserSchoolData(schoolIds);
//        if (CollectionUtils.isEmpty(agentUserSchools)) {
//            agentUserSchools = Collections.emptyList();
//        }
//        Map<Long, List<AgentUserSchool>> agentUserSchoolMap = agentUserSchools.stream().collect(Collectors.groupingBy(AgentUserSchool::getSchoolId));
//
//        dictSchoolList.forEach(item -> {
//            try {
//                T t = tClass.newInstance();
//                t.setDay(day);
//                t.setSchoolId(item.getSchoolId());
//                // 设置部门信息
//                List<Long> groupIds = agentGroupSupport.getGroupIdsBySchool(item.getSchoolId(), Collections.singletonList(AgentGroupRoleType.City));
//                if (CollectionUtils.isNotEmpty(groupIds)) {
//                    AgentGroup group = allGroupMap.get(groupIds.get(0));
//                    if (null != group) {
//                        t.setGroupName(group.getGroupName());
//                    }
//                }
//                // 设置负责人信息
//                {
//                    List<AgentUserSchool> agentUserSchoolsTemp = agentUserSchoolMap.get(item.getSchoolId());
//                    if (CollectionUtils.isNotEmpty(agentUserSchoolsTemp)) {
//                        agentUserSchoolsTemp.forEach(p -> {
//                            Long userId = p.getUserId();
//                            AgentGroupUser businessDeveloperUser = businessDeveloperUserMap.get(userId);
//                            if (null != businessDeveloperUser && allUsersMap.get(String.valueOf(userId)) != null) {
//                                AgentUser user = allUsersMap.get(String.valueOf(userId));
//                                t.setChargePerson(user.getRealName());
//                            }
//                        });
//                    }
//                    if (null == t.getChargePerson()) {
//
//                        if (CollectionUtils.isNotEmpty(groupIds)) {
//                            // 设置部门信息
//                            Long groupId = groupIds.get(0);
//                            // 设置市经理信息
//                            if (cityManageUsersMap.get(groupId) != null) {
//                                Long cityManageId = cityManageUsersMap.get(groupId);
//                                if (allUsersMap.get(String.valueOf(cityManageId)) != null) {
//                                    AgentUser user = allUsersMap.get(String.valueOf(cityManageId));
//                                    t.setChargePerson(user.getRealName());
//                                }
//                            }
//                        }
//                    }
//                }
//                CrmSchoolSummary crmSchoolSummary = crmSchoolSummaryMap.get(item.getSchoolId());
//                if (null != crmSchoolSummary) {
//                    t.setCityName(crmSchoolSummary.getCityName());
//                    t.setCountyName(crmSchoolSummary.getCountyName());
//                    t.setSchoolName(crmSchoolSummary.getSchoolName());
//                    t.setSchoolLevel(crmSchoolSummary.getSchoolLevel());
//                }
//                SchoolExtInfo schoolExtInfo = schoolExtInfoMap.get(item.getSchoolId());
//                if (null != schoolExtInfo) {
//                    if (null != schoolExtInfo.fetchEduSystem()){
//                        t.setEduSystemType(schoolExtInfo.fetchEduSystem());
//                    }else {
//                        School school = schoolMap.get(item.getSchoolId());
//                        t.setEduSystemType(EduSystemType.of(schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly()));
//                    }
//                    t.setEnglishStartGrade(schoolExtInfo.getEnglishStartGrade());
//                }else {
//                    School school = schoolMap.get(item.getSchoolId());
//                    t.setEduSystemType(EduSystemType.of(schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly()));
//                }
//                t.setSchoolPopularity(item.getSchoolPopularity());
//                t.setPermeabilityType(item.getPermeabilityType());
//                resultMap.put(item.getSchoolId(), t);
//            } catch (InstantiationException e) {
//                logger.error("error info: ", e);
//            } catch (IllegalAccessException e) {
//                logger.error("error info: ", e);
//            }
//        });
//        return resultMap;
//    }
//
//
//    public <T extends SchoolBaseReportData> Map<Long, T> generalBaseSchoolReportDataByGroupId(Long groupId, Integer day, Class<T> clazz){
//        return convertSchoolReportToMap(generalBaseSchoolReportDataByGroupId(groupId, day), clazz);
//    }
//
//    private <T extends SchoolBaseReportData> Map<Long, T> convertSchoolReportToMap(List<SchoolBaseReportData> schoolBaseReportDataList, Class<T> clazz){
//        if(CollectionUtils.isEmpty(schoolBaseReportDataList)){
//            return Collections.emptyMap();
//        }
//        return schoolBaseReportDataList.stream().filter(Objects::nonNull).map(p -> p.convert(clazz)).filter(Objects::nonNull).collect(Collectors.toMap(SchoolBaseReportData::getSchoolId, Function.identity(), (o1, o2) -> o1));
//    }
//
//    private List<SchoolBaseReportData> generalBaseSchoolReportDataByGroupId(Long groupId, Integer day) {
//        List<SchoolBaseReportData> resultList = new ArrayList<>();
//        AgentGroup group = agentGroupLoaderClient.load(groupId);
//        if(group == null){
//            return resultList;
//        }
//
//        List<Integer> schoolLevelList = group.fetchServiceTypeList().stream().map(AgentServiceType::toSchoolLevel).map(SchoolLevel::getLevel).collect(Collectors.toList());
//        if(CollectionUtils.isEmpty(schoolLevelList)){
//            return resultList;
//        }
//
//        List<AgentGroup> groupList = new ArrayList<>();
//        groupList.add(group);
//        groupList.addAll(baseOrgService.getSubGroupList(groupId));
//        Map<Long, AgentGroup> groupMap = groupList.stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
//
//        List<Integer> regionCodeList = baseOrgService.getGroupRegionCodeList(groupId);
//        List<Integer> countyCodeList = agentRegionService.getCountyCodes(regionCodeList);
//        Map<Integer, List<AgentDictSchool>> regionSchoolMap = agentDictSchoolLoaderClient.findByCountyCodes(countyCodeList);
//
//        // 保存区域 -- 阶段 -- 学校数据
//        Map<Long, AgentUser> userMap = new HashMap<>();
//        List<Future<List<SchoolBaseReportData>>> futureList = new ArrayList<>();
//
//        Map<Integer, Map<Integer, List<Long>>> regionLevelSchoolMap = new HashMap<>();
//        for(Integer k : regionSchoolMap.keySet()){
//            List<AgentDictSchool> v = regionSchoolMap.get(k);
//            Map<Integer, List<Long>> levelDictMap = v.stream().filter(p -> schoolLevelList.contains(p.getSchoolLevel())).collect(Collectors.groupingBy(AgentDictSchool::getSchoolLevel, Collectors.mapping(AgentDictSchool::getSchoolId, Collectors.toList())));
//            if(MapUtils.isNotEmpty(levelDictMap)){
//                Map<Integer, List<Long>> levelSchoolMap = regionLevelSchoolMap.get(k);
//                if(levelSchoolMap == null){
//                    levelSchoolMap = new HashMap<>();
//                    regionLevelSchoolMap.put(k, levelSchoolMap);
//                }
//                levelSchoolMap.putAll(levelDictMap);
//                if(regionLevelSchoolMap.size() > 300){
//                    Map<Integer, Map<Integer, List<Long>>> regionLevelSchoolMapTmp = new HashMap<>(regionLevelSchoolMap);
//                    futureList.add(AlpsThreadPool.getInstance().submit(() -> innerDataList(regionLevelSchoolMapTmp, groupMap, userMap, day)));
//                    regionLevelSchoolMap = new HashMap<>();
//                }
//            }
//        }
//        if(regionLevelSchoolMap.size() > 0){
//            Map<Integer, Map<Integer, List<Long>>> regionLevelSchoolMapTmp = new HashMap<>(regionLevelSchoolMap);
//            futureList.add(AlpsThreadPool.getInstance().submit(() -> innerDataList(regionLevelSchoolMapTmp, groupMap, userMap, day)));
//        }
//
//        for(Future<List<SchoolBaseReportData>> future : futureList){
//            try{
//                List<SchoolBaseReportData> dataList = future.get();
//                if(CollectionUtils.isNotEmpty(dataList)){
//                    resultList.addAll(dataList);
//                }
//            }catch (Exception ignored){
//                logger.error("天权表下载异常", ignored);
//                emailServiceClient.createPlainEmail()
//                        .body("")
//                        .subject("接口调用失败【" + RuntimeMode.current().getStageMode() + "】")
//                        .to("song.wang@17zuoye.com")
//                        .send();
//            }
//        }
//        return resultList;
//    }
//
//    public <T extends SchoolBaseReportData> Map<Long, T> generalBaseSchoolReportDataByUserId(Long userId, Integer day, Class<T> clazz){
//        return convertSchoolReportToMap(generalBaseSchoolReportDataByUserId(userId, day), clazz);
//    }
//
//    private List<SchoolBaseReportData> generalBaseSchoolReportDataByUserId(Long userId, Integer day) {
//        List<SchoolBaseReportData> resultList = new ArrayList<>();
//        AgentUser user = baseOrgService.getUser(userId);
//        if(user == null){
//            return resultList;
//        }
//
//        List<AgentUserSchool> agentUserSchools = agentUserSchoolLoaderClient.findByUserId(userId);
//        if(CollectionUtils.isEmpty(agentUserSchools)){
//            return resultList;
//        }
//        List<Long> allSchoolIds = agentUserSchools.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toList());
//
//        List<Long> groupIds = baseOrgService.getGroupIdListByUserId(userId);
//        AgentGroup group = null;
//        if(CollectionUtils.isNotEmpty(groupIds)){
//            group = agentGroupLoaderClient.load(groupIds.get(0));
//        }
//
//        Map<Long, CrmSchoolSummary> schoolSummaryMap = agentSchoolSupport.batchLoadCrmSchoolSummary(allSchoolIds);
//        Map<Long, SchoolExtInfo> schoolExtInfoMap = batchLoadSchoolsExtInfoData(allSchoolIds);
//
//        Map<Long, AgentDictSchool> dictSchoolMap = agentDictSchoolLoaderClient.findBySchoolIds(allSchoolIds);
//
//        for(Long schoolId : allSchoolIds){
//            SchoolBaseReportData data = new SchoolBaseReportData();
//            data.setDay(day);
//            data.setSchoolId(schoolId);
//            data.setGroupName(group != null ? group.getGroupName() : "");
//            data.setChargePerson(user.getRealName());
//
//            SchoolLevel schoolLevel = null;
//            CrmSchoolSummary crmSchoolSummary = schoolSummaryMap.get(schoolId);
//            if (null != crmSchoolSummary) {
//                data.setCityName(crmSchoolSummary.getCityName());
//                data.setCountyName(crmSchoolSummary.getCountyName());
//                data.setSchoolName(crmSchoolSummary.getSchoolName());
//                data.setSchoolLevel(crmSchoolSummary.getSchoolLevel());
//                schoolLevel = crmSchoolSummary.getSchoolLevel();
//            }
//
//            AgentDictSchool dictSchool = dictSchoolMap.get(schoolId);
//            if(dictSchool != null){
//                data.setSchoolPopularity(dictSchool.getSchoolPopularity());
//                if(schoolLevel == null){
//                    schoolLevel = SchoolLevel.safeParse(dictSchool.getSchoolLevel(), null);
//                }
//            }
//
//            data.setEduSystemType(getDefaultEduSystem(schoolLevel));
//
//            SchoolExtInfo schoolExtInfo = schoolExtInfoMap.get(schoolId);
//            if(null != schoolExtInfo){
//                data.setEnglishStartGrade(schoolExtInfo.getEnglishStartGrade());
//            }
//            if (null != schoolExtInfo && null != schoolExtInfo.fetchEduSystem()) {
//                data.setEduSystemType(schoolExtInfo.fetchEduSystem());
//            }else {
//                if(schoolLevel == null){
//                    School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
//                    if(school != null){
//                        data.setEduSystemType(EduSystemType.of(school.getDefaultEduSystemType()));
//                    }
//                }
//            }
//            resultList.add(data);
//        }
//        return resultList;
//    }
//
//    private List<SchoolBaseReportData>  innerDataList(Map<Integer, Map<Integer, List<Long>>> regionLevelSchoolMap, Map<Long, AgentGroup> groupMap, Map<Long, AgentUser> userMap, Integer day){
//        List<SchoolBaseReportData> resultList = new ArrayList<>();
//
//        if(MapUtils.isEmpty(regionLevelSchoolMap)){
//            return resultList;
//        }
//        List<Long> allSchoolIds = new ArrayList<>();
//        regionLevelSchoolMap.values().forEach(m -> m.values().forEach(allSchoolIds::addAll));
//
//        Map<Long, CrmSchoolSummary> schoolSummaryMap = agentSchoolSupport.batchLoadCrmSchoolSummary(allSchoolIds);
//
//        Map<Long, SchoolExtInfo> schoolExtInfoMap = batchLoadSchoolsExtInfoData(allSchoolIds);
//
//        Map<Integer, List<AgentDictSchool>> regionSchoolMap = agentDictSchoolLoaderClient.findByCountyCodes(regionLevelSchoolMap.keySet());
//        Map<Long, AgentDictSchool> dictSchoolMap = regionSchoolMap.values().stream().flatMap(List::stream).collect(Collectors.toMap(AgentDictSchool::getSchoolId, Function.identity(), (o1, o2) -> o1));
//
//        regionLevelSchoolMap.forEach((k, v) -> {
//            for(Integer level : v.keySet()){
//                List<Long> groupIds = agentGroupSupport.getGroupIdsByRegionCodeAndSchoolLevels(k, Collections.singleton(level), Collections.singletonList(AgentGroupRoleType.City));
//                AgentGroup regionGroup = null;
//                if(CollectionUtils.isNotEmpty(groupIds)){
//                    regionGroup = groupMap.get(groupIds.get(0));
//                }
//                List<Long> schoolIds = v.get(level);
//                List<AgentUserSchool> agentUserSchools = agentUserSchoolLoaderClient.findBySchoolIds(schoolIds);
//                Map<Long, Long> schoolUserMap = agentUserSchools.stream().collect(Collectors.toMap(AgentUserSchool::getSchoolId, AgentUserSchool::getUserId, (o1, o2) -> o1));
//
//                for(Long schoolId : schoolIds){
//                    SchoolBaseReportData data = new SchoolBaseReportData();
//                    data.setDay(day);
//                    data.setSchoolId(schoolId);
//                    data.setGroupName(regionGroup != null ? regionGroup.getGroupName() : "");
//                    Long userId = schoolUserMap.get(schoolId);
//                    if(userId != null){
//                        AgentUser user = userMap.get(userId);
//                        if(user == null){
//                            user = baseOrgService.getUser(userId);
//                            if(user != null){
//                                data.setChargePerson(user.getRealName());
//                                userMap.put(user.getId(), user);
//                            }
//                        }else {
//                            data.setChargePerson(user.getRealName());
//                        }
//                    }else {
//                        if(regionGroup != null){
//                            Long managerId = baseOrgService.getGroupManager(regionGroup.getId());
//                            if(managerId != null){
//                                AgentUser user = userMap.get(managerId);
//                                if(user == null){
//                                    user = baseOrgService.getUser(managerId);
//                                    if(user != null){
//                                        data.setChargePerson(user.getRealName());
//                                        userMap.put(user.getId(), user);
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    SchoolLevel schoolLevel = null;
//                    CrmSchoolSummary crmSchoolSummary = schoolSummaryMap.get(schoolId);
//                    if (null != crmSchoolSummary) {
//                        data.setCityName(crmSchoolSummary.getCityName());
//                        data.setCountyName(crmSchoolSummary.getCountyName());
//                        data.setSchoolName(crmSchoolSummary.getSchoolName());
//                        data.setSchoolLevel(crmSchoolSummary.getSchoolLevel());
//                        schoolLevel = crmSchoolSummary.getSchoolLevel();
//                    }
//
//                    AgentDictSchool dictSchool = dictSchoolMap.get(schoolId);
//                    if(dictSchool != null){
//                        data.setSchoolPopularity(dictSchool.getSchoolPopularity());
//                        if(schoolLevel == null){
//                            schoolLevel = SchoolLevel.safeParse(dictSchool.getSchoolLevel(), null);
//                        }
//                    }
//
//                    data.setEduSystemType(getDefaultEduSystem(schoolLevel));
//
//                    SchoolExtInfo schoolExtInfo = schoolExtInfoMap.get(schoolId);
//                    if(null != schoolExtInfo){
//                        data.setEnglishStartGrade(schoolExtInfo.getEnglishStartGrade());
//                    }
//                    if (null != schoolExtInfo && null != schoolExtInfo.fetchEduSystem()) {
//                        data.setEduSystemType(schoolExtInfo.fetchEduSystem());
//                    }else {
//                        if(schoolLevel == null){
//                            School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
//                            if(school != null){
//                                data.setEduSystemType(EduSystemType.of(school.getDefaultEduSystemType()));
//                            }
//                        }
//                    }
//
//                    resultList.add(data);
//                }
//            }
//        });
//        return resultList;
//    }
//
//    private EduSystemType getDefaultEduSystem(SchoolLevel schoolLevel){
//        if(schoolLevel == null){
//            return null;
//        }
//        if(schoolLevel == SchoolLevel.JUNIOR){
//            return EduSystemType.P6;
//        }else if(schoolLevel == SchoolLevel.MIDDLE){
//            return EduSystemType.J4;
//        }else if(schoolLevel == SchoolLevel.HIGH){
//            return EduSystemType.S3;
//        }else if(schoolLevel == SchoolLevel.INFANT){
//            return EduSystemType.I4;
//        }
//        return null;
//    }
//
//    private Integer getLastMonth(Integer day){
//        Date date = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
//        Date targetDate = DayUtils.getLastDayOfMonth(DateUtils.addMonths(date, -1));
//        return SafeConverter.toInt(DateUtils.dateToString(targetDate, "yyyyMMdd"));
//    }
//
//    public Map<Long, SchoolOnlineReportData> getSchoolOnlineReportData(Long groupId, Long userId, Integer day) {
//        Map<Long, SchoolOnlineReportData> schoolOnlineReportDataMap = new HashMap<>();
//        Map<Long, SchoolOnlineReportData> tmpMap = null;
//        if(groupId != null && groupId > 0){
//            tmpMap = generalBaseSchoolReportDataByGroupId(groupId, day, SchoolOnlineReportData.class);
//        }else if(userId != null && userId > 0){
//            tmpMap = generalBaseSchoolReportDataByUserId(userId, day, SchoolOnlineReportData.class);
//        }
//        if(MapUtils.isNotEmpty(tmpMap)){
//            schoolOnlineReportDataMap.putAll(tmpMap);
//        }
//
//        if(MapUtils.isEmpty(schoolOnlineReportDataMap)){
//            return schoolOnlineReportDataMap;
//        }
//        List<Integer> dimensions = new ArrayList<>();
//        dimensions.add(AgentConstants.ONLINE_INDICATOR_MONTH);
//        dimensions.add(AgentConstants.ONLINE_INDICATOR_SUM);
//        Map<Long, SchoolOnlineIndicator> schoolOnlineIndicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(schoolOnlineReportDataMap.keySet(), day, dimensions);
//
//        Map<Long, OnlineIndicator> lmSchoolOnlineIndicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator2(schoolOnlineReportDataMap.keySet(), getLastMonth(day), AgentConstants.ONLINE_INDICATOR_MONTH);
//
//        schoolOnlineReportDataMap.forEach((k, onlineReportData) -> {
//            if (schoolOnlineIndicatorMap.containsKey(k)) {
//                SchoolOnlineIndicator schoolOnlineIndicator = schoolOnlineIndicatorMap.get(k);
//                if(schoolOnlineIndicator == null){
//                    return;
//                }
//
//                AgentSchoolPermeabilityType sglSubjPermeabilityType = schoolResourceService.getSchoolSubjectPermeabilityType(schoolOnlineIndicator.fetchMonthData().getMaxPenetrateRateSglSubj());
//                onlineReportData.setMaxPenetrateRateSglSubj(sglSubjPermeabilityType != null ? sglSubjPermeabilityType.getDesc() : "");
//                AgentSchoolPermeabilityType engPermeabilityType = schoolResourceService.getSchoolSubjectPermeabilityType(schoolOnlineIndicator.fetchMonthData().getMaxPenetrateRateEng());
//                onlineReportData.setMaxPenetrateRateEng(engPermeabilityType != null ? engPermeabilityType.getDesc() : "");
//
//                AgentSchoolPermeabilityType mathPermeabilityType = schoolResourceService.getSchoolSubjectPermeabilityType(schoolOnlineIndicator.fetchMonthData().getMaxPenetrateRateMath());
//                onlineReportData.setMaxPenetrateRateMath(mathPermeabilityType != null ? mathPermeabilityType.getDesc() : "");
//
//                AgentSchoolPermeabilityType chnPermeabilityType = schoolResourceService.getSchoolSubjectPermeabilityType(schoolOnlineIndicator.fetchMonthData().getMaxPenetrateRateChn());
//                onlineReportData.setMaxPenetrateRateChn(chnPermeabilityType != null ? chnPermeabilityType.getDesc() : "");
//
//
//                onlineReportData.setStuScale(SafeConverter.toInt(schoolOnlineIndicator.fetchSumData().getStuScale()));
//
//                onlineReportData.setRegTeaCount(SafeConverter.toInt(schoolOnlineIndicator.fetchSumData().getRegSglSubjTeaCount()));
//                onlineReportData.setTmRegTeaCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getRegSglSubjTeaCount()));
//                onlineReportData.setTmAssignHwTeaCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getHwSglSubjTeaCount()));
//
//
//                onlineReportData.setRegStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchSumData().getRegStuCount()));
//                onlineReportData.setAuStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchSumData().getAuStuCount()));
//
//
//                //初高中学校，添加本月学生注册数（升学）
//                int promoteRegStuCount = 0;
//
//                if (onlineReportData.getSchoolLevel() == SchoolLevel.MIDDLE || onlineReportData.getSchoolLevel() == SchoolLevel.HIGH){
//                    OnlineIndicator onlineIndicator = schoolOnlineIndicator.fetchMonthData();
//                    if (onlineIndicator != null){
//                        promoteRegStuCount = onlineIndicator.getPromoteRegStuCount() != null ? onlineIndicator.getPromoteRegStuCount() : 0;
//                    }
//                }
//                onlineReportData.setTmRegStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getRegStuCount()) + promoteRegStuCount);
//                onlineReportData.setTmAuStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getAuStuCount()));
//
//                //英语
//                onlineReportData.setEngSettlementStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchSumData().getIncSettlementEngStuCount()));
//                onlineReportData.setFinEngHwEq1UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinEngHwEq1UnSettleStuCount()));
//                onlineReportData.setFinEngHwEq2UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinEngHwEq2UnSettleStuCount()));
//                onlineReportData.setIncSettlementEngStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getIncSettlementEngStuCount()));
//                onlineReportData.setNewEngTeaHwGte3UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getNewEngTeaHwGte3UnSettleStuCount()));
//                onlineReportData.setOldEngTeaHwGte3UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getOldEngTeaHwGte3UnSettleStuCount()));
//                onlineReportData.setFinEngHwEq1SettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinEngHwEq1SettleStuCount()));
//                onlineReportData.setFinEngHwEq2SettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinEngHwEq2SettleStuCount()));
//                onlineReportData.setFinEngHwGte3SettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinEngHwGte3SettleStuCount()));
//                onlineReportData.setFinEngHwGte3AuStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinEngHwGte3AuStuCount()));
//
//                OnlineIndicator onlineIndicator = lmSchoolOnlineIndicatorMap.get(k);
//                if(onlineIndicator != null){
//                    onlineReportData.setLmFinEngHwGte3AuStuCount(SafeConverter.toInt(onlineIndicator.getFinEngHwGte3AuStuCount()));
//                }
//
//                //数学
//                onlineReportData.setMathSettlementStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchSumData().getIncSettlementMathStuCount()));
//                onlineReportData.setFinMathHwEq1UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinMathHwEq1UnSettleStuCount()));
//                onlineReportData.setFinMathHwEq2UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinMathHwEq2UnSettleStuCount()));
//                onlineReportData.setIncSettlementMathStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getIncSettlementMathStuCount()));
//                onlineReportData.setNewMathTeaHwGte3UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getNewMathTeaHwGte3UnSettleStuCount()));
//                onlineReportData.setOldMathTeaHwGte3UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getOldMathTeaHwGte3UnSettleStuCount()));
//                onlineReportData.setFinMathHwEq1SettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinMathHwEq1SettleStuCount()));
//                onlineReportData.setFinMathHwEq2SettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinMathHwEq2SettleStuCount()));
//                onlineReportData.setFinMathHwGte3SettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinMathHwGte3SettleStuCount()));
//                onlineReportData.setFinMathHwGte3AuStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinMathHwGte3AuStuCount()));
//
//                if(onlineIndicator != null){
//                    onlineReportData.setLmFinMathHwGte3AuStuCount(SafeConverter.toInt(onlineIndicator.getFinMathHwGte3AuStuCount()));
//                }
//                //语文
//                onlineReportData.setChnSettlementStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchSumData().getIncSettlementChnStuCount()));
//                onlineReportData.setFinChnHwEq1UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinChnHwEq1UnSettleStuCount()));
//                onlineReportData.setFinChnHwEq2UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinChnHwEq2UnSettleStuCount()));
//                onlineReportData.setIncSettlementChnStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getIncSettlementChnStuCount()));
//                onlineReportData.setNewChnTeaHwGte3UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getNewChnTeaHwGte3UnSettleStuCount()));
//                onlineReportData.setOldChnTeaHwGte3UnSettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getOldChnTeaHwGte3UnSettleStuCount()));
//                onlineReportData.setFinChnHwEq1SettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinChnHwEq1SettleStuCount()));
//                onlineReportData.setFinChnHwEq2SettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinChnHwEq2SettleStuCount()));
//                onlineReportData.setFinChnHwGte3SettleStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinChnHwGte3SettleStuCount()));
//                onlineReportData.setFinChnHwGte3AuStuCount(SafeConverter.toInt(schoolOnlineIndicator.fetchMonthData().getFinChnHwGte3AuStuCount()));
//
//                if(onlineIndicator != null){
//                    onlineReportData.setLmFinChnHwGte3AuStuCount(SafeConverter.toInt(onlineIndicator.getFinChnHwGte3AuStuCount()));
//                }
//
//                onlineReportData.setLatestVisitTime(null); // TODO: 2018/10/11
//            }
//        });
//        return schoolOnlineReportDataMap;
//    }
//
//    public Map<Long, SchoolOfflineReportData> getSchoolOfflineReportData(List<Long> schoolIds, Integer day) {
//        //获取并组装与online模式相同字段
//        Map<Long, SchoolOfflineReportData> schoolOfflineReportDataMap = gengeralBaseSchoolReportData(schoolIds, day, SchoolOfflineReportData.class);
//        //获取offline当月数据
//        Map<Long, AgentSchoolKlxPerformanceData> agentSchoolKlxPerformanceDataMap = batchLoadSchoolKlxPerformanceData(schoolOfflineReportDataMap.keySet(), day);
//        //获取offline上月数据
//        Integer lastMonthLastDay = getLastMonthLastDay(day);
//        Map<Long, AgentSchoolKlxPerformanceData> agentSchoolKlxPerformanceDataMapLastMonth = batchLoadSchoolKlxPerformanceData(schoolOfflineReportDataMap.keySet(), lastMonthLastDay);
//        //获取17数据
//        Map<Long, AgentSchool17PerformanceData> agentSchool17PerformanceDataMap = batchLoadSchool17PerformanceData(schoolOfflineReportDataMap.keySet(), day);
//        schoolOfflineReportDataMap.forEach((k, offlineReportData) -> {
//            //设置当月数据
//            if (agentSchoolKlxPerformanceDataMap.containsKey(k)) {
//                AgentSchoolKlxPerformanceData performanceData = agentSchoolKlxPerformanceDataMap.get(k);
//                if (null != performanceData){
//                    AgentSchoolKlxPerformanceIndicator indicatorData = performanceData.getIndicatorData();
//                    if (null != indicatorData){
//                        //设置“学校规模”
//                        offlineReportData.setStuScale(indicatorData.getStuScale());
//                        //设置“快乐学考号数”
//                        offlineReportData.setKlxTnCount(indicatorData.getKlxTnCount());
//                        //设置“本月扫描试卷老师数”
//                        int tmScanTpTeaCount = indicatorData.getTmScanTpMathTeaCount() + indicatorData.getTmScanTpEngTeaCount() + indicatorData.getTmScanTpPhyTeaCount() +
//                                indicatorData.getTmScanTpCheTeaCount() + indicatorData.getTmScanTpBiolTeaCount() + indicatorData.getTmScanTpChnTeaCount() +
//                                indicatorData.getTmScanTpHistTeaCount() + indicatorData.getTmScanTpGeogTeaCount() + indicatorData.getTmScanTpPolTeaCount();
//                        offlineReportData.setTmScanTpTeaCount(tmScanTpTeaCount);
//                        //设置“普通扫描≥1次学生数”
//                        Integer tmFinTpEqStuCount = indicatorData.getTmFinTpEq1StuCount() + indicatorData.getTmFinTpEq2StuCount() + indicatorData.getTmFinTpGte3StuCount();
//                        offlineReportData.setTmFinTpEqStuCount(tmFinTpEqStuCount);
//                        //设置“普通扫描≥3次学生数”
//                        offlineReportData.setTmFinTpGte3StuCount(indicatorData.getTmFinTpGte3StuCount());
//
//                        //设置“本月数学大考扫描学生数”
//                        offlineReportData.setTmFinMathBgExamStuCount(indicatorData.getTmFinMathBgExamStuCount());
//                        //设置“本月英语大考扫描学生数”
//                        offlineReportData.setTmFinEngBgExamStuCount(indicatorData.getTmFinEngBgExamStuCount());
//                        //设置“本月语文大考扫描学生数”
//                        offlineReportData.setTmFinChnBgExamStuCount(indicatorData.getTmFinChnTpGte1StuCount());
//                        //设置“本月物理大考扫描学生数”
//                        offlineReportData.setTmFinPhyBgExamStuCount(indicatorData.getTmFinPhyBgExamStuCount());
//                        //设置“本月化学大考扫描学生数”
//                        offlineReportData.setTmFinCheBgExamStuCount(indicatorData.getTmFinCheBgExamStuCount());
//                        //设置“本月生物大考扫描学生数”
//                        offlineReportData.setTmFinBiolBgExamStuCount(indicatorData.getTmFinBiolBgExamStuCount());
//                        //设置“本月政治大考扫描学生数”
//                        offlineReportData.setTmFinPolBgExamStuCount(indicatorData.getTmFinPolBgExamStuCount());
//                        //设置“本月历史大考扫描学生数”
//                        offlineReportData.setTmFinHistBgExamStuCount(indicatorData.getTmFinHistBgExamStuCount());
//                        //设置“本月地理大考扫描学生数”
//                        offlineReportData.setTmFinGeogBgExamStuCount(indicatorData.getTmFinGeogBgExamStuCount());
//                    }
//                }
//            }
//            //设置上月数据
//            if (agentSchoolKlxPerformanceDataMapLastMonth.containsKey(k)) {
//                AgentSchoolKlxPerformanceData performanceData = agentSchoolKlxPerformanceDataMapLastMonth.get(k);
//                if (null != performanceData){
//                    AgentSchoolKlxPerformanceIndicator indicatorData = performanceData.getIndicatorData();
//                    if (null != indicatorData){
//                        //设置“上月普通扫描≥1次学生数”
//                        int lmFinTpEqStuCount = indicatorData.getTmFinTpEq1StuCount() + indicatorData.getTmFinTpEq2StuCount() + indicatorData.getTmFinTpGte3StuCount();
//                        offlineReportData.setLmFinTpEqStuCount(lmFinTpEqStuCount);
//                        //设置“上月普通扫描≥3次学生数”
//                        offlineReportData.setLmFinTpGte3StuCount(indicatorData.getTmFinTpGte3StuCount());
//                    }
//                }
//            }
//            //设置“最近拜访日期”
//            if (agentSchool17PerformanceDataMap.containsKey(k)) {
//                AgentSchool17PerformanceData performanceData = agentSchool17PerformanceDataMap.get(k);
//                if (null != performanceData && null != performanceData.getIndicatorData()){
//                    offlineReportData.setLatestVisitTime(performanceData.getIndicatorData().getLatestVisitTime());
//                }
//            }
//        });
//        return schoolOfflineReportDataMap;
//    }
//
//    //gengeralBaseSchoolOnlineReportData
//
//    public <SchoolT extends SchoolBaseReportData, TeacherT extends TeacherBaseReportData> Map<Long, TeacherT> generateBaseTeacherReportData(Map<Long, SchoolT> schoolReportDataMap, Integer day, Class<TeacherT> teacherClass) {
//        Map<Long, TeacherT> resultMap = new HashMap<>();
//        Map<Long, List<CrmTeacherSummary>> schoolTeacherSummayMap = batchLoadCrmTeacherSummay(schoolReportDataMap.keySet());
//        Set<Long> teacherIdSet = new HashSet<>();
//        schoolTeacherSummayMap.values().forEach(p -> teacherIdSet.addAll(p.stream().map(CrmTeacherSummary::getTeacherId).collect(Collectors.toSet())));
//        Map<Long, AgentHiddenTeacher> agentHiddenTeachers = batchLoadAgentHiddenTeacher(teacherIdSet);
//        schoolTeacherSummayMap.forEach((schoolId, teacherSummaryList) -> {
//            SchoolT schoolT = schoolReportDataMap.get(schoolId);
//            try {
//                TeacherT teacherT = teacherClass.newInstance();
//                if (null != schoolT) {
//                    teacherT.setDay(day);
//                    teacherT.setChargePerson(schoolT.getChargePerson());
//                    teacherT.setCityName(schoolT.getCityName());
//                    teacherT.setCountyName(schoolT.getCountyName());
//                    teacherT.setSchoolId(schoolT.getSchoolId());
//                    teacherT.setSchoolName(schoolT.getSchoolName());
//                    teacherT.setSchoolLevel(schoolT.getSchoolLevel());
//                    teacherT.setSchoolPopularity(schoolT.getSchoolPopularity());
//                }
//                if (CollectionUtils.isNotEmpty(teacherSummaryList)) {
//                    //过滤假老师和禁用老师
//                    teacherSummaryList = teacherSummaryList.stream().filter(ip -> !Objects.equals(ip.getDisabled(), true) && ip.getManualFakeTeacher()).collect(toList());
//                    //过滤隐藏老师
//                    Map<Long, CrmTeacherSummary> crmTeacherSummaryMap = teacherSummaryList.stream().filter(p -> !agentHiddenTeachers.containsKey(p.getTeacherId())).collect(toMap(CrmTeacherSummary::getTeacherId, Function.identity(), (o1, o2) -> o2));
//                    crmTeacherSummaryMap.forEach((k, v) -> {
//                        try {
//                            TeacherT teacherT1 = (TeacherT) BeanUtils.cloneBean(teacherT);
//                            teacherT1.setTeacherId(v.getTeacherId());
//                            teacherT1.setTeacherName(v.getRealName());
//                            teacherT1.setSubject(v.getSubject());
//                            teacherT1.setAuState(v.getAuthState());
//                            Long authTime = v.getAuthTime();
//                            if (authTime != null) {
//                                teacherT1.setAuTime(DateUtils.stringToDate(String.valueOf(authTime), "yyyyMMddHHmmss"));
//                            }
//                            Long registerTime = v.getRegisterTime();
//                            if (registerTime != null) {
//                                teacherT1.setRegTime(DateUtils.stringToDate(String.valueOf(registerTime), "yyyyMMddHHmmss"));
//                            }
//                            resultMap.put(v.getTeacherId(), teacherT1);
//                        } catch (Exception e) {
//                        }
//                    });
//                }
//            } catch (InstantiationException e) {
//                logger.error("error info: ", e);
//            } catch (IllegalAccessException e) {
//                logger.error("error info: ", e);
//            }
//        });
//        return resultMap;
//    }
//
//    public Map<Long, TeacherOnlineReportData> getTeacherOnlineReportData(Collection<Long> schoolIds, Integer day) {
//        Map<Long, SchoolOnlineReportData> schoolOnlineReportDataMap = gengeralBaseSchoolReportData(schoolIds, day, SchoolOnlineReportData.class);
//        Map<Long, TeacherOnlineReportData> teacherOnlineReportDataMap = generateBaseTeacherReportData(schoolOnlineReportDataMap, day, TeacherOnlineReportData.class);
//        Map<Long, AgentTeacher17PerformanceData> teacher17PerformanceDataMap = batchLoadTeacher17PerformanceData(teacherOnlineReportDataMap.keySet(), day);
//        Integer lastMonthLastDay = getLastMonthLastDay(day);
//        Map<Long, AgentTeacher17PerformanceData> lastMonthLastDayTeacher17PerformanceDataMap = batchLoadTeacher17PerformanceData(teacherOnlineReportDataMap.keySet(), lastMonthLastDay);
//        //获取老师主副账号
//        Map<Long, Long> subMainTeacherIdMap = teacherLoaderClient.loadMainTeacherIds(teacherOnlineReportDataMap.keySet());
//        teacherOnlineReportDataMap.forEach((k,reportData) -> {
//            AgentTeacher17PerformanceData performanceData = teacher17PerformanceDataMap.get(k);
//            if (null != performanceData){
//                //根据老师副账号获取主账号ID
//                Long mainTeacherId = subMainTeacherIdMap.get(k);
//                //若有值，说明是副账号
//                if (null != mainTeacherId){
//                    //将副账号“老师ID”设置为主主账号“老师ID”
//                    reportData.setTeacherId(mainTeacherId);
//                    //将副账号“注册日期”设置为主账号“注册日期”
//                    AgentTeacher17PerformanceData performanceDataMain = teacher17PerformanceDataMap.get(mainTeacherId);
//                    if(null != performanceDataMain && null != performanceDataMain.getRegTime()){
//                        reportData.setRegTime(performanceDataMain.getRegTime());
//                    }
//                }else{
//                    if(null != performanceData.getRegTime()){
//                        reportData.setRegTime(performanceData.getRegTime());
//                    }
//                }
//                if (null != performanceData.getAuTime()){
//                    reportData.setAuTime(performanceData.getAuTime());
//                }
//                reportData.setAuState(performanceData.getAuState());
//                reportData.setClazzCount(performanceData.getClazzCount());
//                reportData.setRegStuCount(performanceData.getRegStuCount());
//                reportData.setAuStuCount(performanceData.getAuStuCount());
//                reportData.setLatestHwTime(performanceData.getLatestHwTime());
//                reportData.setTmHwSc(performanceData.getTmHwSc());
//                reportData.setCsSettlementStuCount(performanceData.getCsSettlementStuCount());
//                reportData.setTmFinCsHwEq1IncStuCount(performanceData.getTmFinCsHwEq1IncStuCount());
//                reportData.setTmFinCsHwEq2IncStuCount(performanceData.getTmFinCsHwEq2IncStuCount());
//                reportData.setTmFinCsHwGte3IncAuStuCount(performanceData.getTmFinCsHwGte3IncAuStuCount());
//                reportData.setBfEq1StuCount(performanceData.getFinCsHwEq1StuCount() - performanceData.getTmFinCsHwEq1IncStuCount());
//                reportData.setBfEq2StuCount(performanceData.getFinCsHwEq2StuCount() - performanceData.getTmFinCsHwEq2IncStuCount());
//                reportData.setBfGte3StuCount(performanceData.getFinCsHwGte3AuStuCount() - performanceData.getTmFinCsHwGte3IncAuStuCount());
//                reportData.setFinCsHwGte3AuStuCount(performanceData.getFinCsHwGte3AuStuCount());
//
//                AgentTeacher17PerformanceData lastMonthperformanceData = lastMonthLastDayTeacher17PerformanceDataMap.get(k);
//                if (null != lastMonthperformanceData) {
//                    reportData.setLmFinCsHwGte3AuStuCount(lastMonthperformanceData.getFinCsHwGte3AuStuCount());
//                }
//                //布置假期作业的班组数
//                reportData.setVacnHwGroupCount(performanceData.getVacnHwGroupCount());
//                //布置期末作业的班组数
//                reportData.setTermReviewGroupCount(performanceData.getTermReviewGroupCount());
//            }
//        });
//        return teacherOnlineReportDataMap;
//    }
//
//    public Map<Long, TeacherOfflineReportData> getTeacherOfflineReportData(Collection<Long> schoolIds, Integer day) {
//        //获取并组装与online模式相同字段
//        Map<Long, SchoolOfflineReportData> schoolOfflineReportDataMap = gengeralBaseSchoolReportData(schoolIds, day, SchoolOfflineReportData.class);
//        Map<Long, TeacherOfflineReportData> teacherOfflineReportDataMap = generateBaseTeacherReportData(schoolOfflineReportDataMap, day, TeacherOfflineReportData.class);
//        //获取offline当月数据
//        Map<Long, AgentTeacherKlxPerformanceData> teacherKlxPerformanceDataMap = batchLoadTeacherKlxPerformanceData(teacherOfflineReportDataMap.keySet(), day);
//        //获取offline上月数据
//        Integer lastMonthLastDay = getLastMonthLastDay(day);
//        Map<Long, AgentTeacherKlxPerformanceData> teacherKlxPerformanceDataMapLastMonth = batchLoadTeacherKlxPerformanceData(teacherOfflineReportDataMap.keySet(), lastMonthLastDay);
//        //获取17数据
//        Map<Long, AgentTeacher17PerformanceData> teacher17PerformanceDataMap = batchLoadTeacher17PerformanceData(teacherOfflineReportDataMap.keySet(), day);
//        teacherOfflineReportDataMap.forEach((k, reportData) -> {
//            if (teacherKlxPerformanceDataMap.containsKey(k)) {
//                AgentTeacherKlxPerformanceData performanceData = teacherKlxPerformanceDataMap.get(k);
//                if (null != performanceData) {
//                    AgentTeacherKlxPerformanceIndicator indicatorData = performanceData.getIndicatorData();
//                    //设置“快乐学考号数”
//                    reportData.setKlxTnCount(indicatorData.getKlxTnCount());
//                    //设置“本月扫描试卷套数”
//                    reportData.setTmScanTpCount(indicatorData.getTmScanTpCount());
//                    //设置“普通扫描≥1次学生数”
//                    int tmFinCsTpEqStuCount = indicatorData.getTmFinCsTpEq1StuCount() + indicatorData.getTmFinCsTpEq2StuCount() + indicatorData.getTmFinCsTpGte3StuCount();
//                    reportData.setTmFinCsTpEqStuCount(tmFinCsTpEqStuCount);
//                    //设置“普通扫描≥3次学生数”
//                    reportData.setTmFinCsTpGte3StuCount(indicatorData.getTmFinCsTpGte3StuCount());
//                }
//            }
//            if (teacherKlxPerformanceDataMapLastMonth.containsKey(k)) {
//                AgentTeacherKlxPerformanceData performanceData = teacherKlxPerformanceDataMapLastMonth.get(k);
//                if (null != performanceData) {
//                    AgentTeacherKlxPerformanceIndicator indicatorData = performanceData.getIndicatorData();
//                    //设置“上月普通扫描≥1次学生数”
//                    int lmFinCsTpEqStuCount = indicatorData.getTmFinCsTpEq1StuCount() + indicatorData.getTmFinCsTpEq2StuCount() + indicatorData.getTmFinCsTpGte3StuCount();
//                    reportData.setLmFinCsTpEqStuCount(lmFinCsTpEqStuCount);
//                    //设置“上月普通扫描≥3次学生数”
//                    reportData.setLmFinCsTpGte3StuCount(indicatorData.getTmFinCsTpGte3StuCount());
//                }
//
//            }
//            if (teacher17PerformanceDataMap.containsKey(k)) {
//                AgentTeacher17PerformanceData performanceData = teacher17PerformanceDataMap.get(k);
//                if (null != performanceData) {
//                    //设置“注册日期”
//                    if (null != performanceData.getRegTime()) {
//                        reportData.setRegTime(performanceData.getRegTime());
//                    }
//                    //设置“带班数量”
//                    reportData.setClazzCount(performanceData.getClazzCount());
//                }
//            }
//        });
//        return teacherOfflineReportDataMap;
//    }
//
//
//
//
//    /**
//     * 获取班级online/offline模式基本信息数据
//     *
//     * @param schoolReportDataMap
//     * @param day
//     * @param classClass
//     * @param <SchoolT>
//     * @param <ClassT>
//     * @return
//     */
//    public <SchoolT extends SchoolBaseReportData, ClassT extends ClassBaseReportData> Map<Long, ClassT> generateBaseClassReportData(Map<Long, SchoolT> schoolReportDataMap, Integer day, Class<ClassT> classClass) {
//        Map<Long, ClassT> resultMap = new HashMap<>();
//        // 获取学校的班级信息
//        Map<Long, List<CrmClazzSummary>> schoolClassMap = batchLoadCrmClassSummary(schoolReportDataMap.keySet());
//        schoolClassMap.forEach((schoolId, classList) -> {
//            SchoolT schoolT = schoolReportDataMap.get(schoolId);
//            try {
//                ClassT classT = classClass.newInstance();
//                if (null != schoolT) {
//                    classT.setDay(day);
//                    classT.setChargePerson(schoolT.getChargePerson());
//                    classT.setCityName(schoolT.getCityName());
//                    classT.setCountyName(schoolT.getCountyName());
//                    classT.setSchoolId(schoolT.getSchoolId());
//                    classT.setSchoolName(schoolT.getSchoolName());
//                    classT.setSchoolLevel(schoolT.getSchoolLevel());
//                    classT.setSchoolPopularity(schoolT.getSchoolPopularity());
//
//                    if (CollectionUtils.isNotEmpty(classList)) {
//                        classList.forEach(item -> {
//                            try {
//                                ClassT bean = (ClassT) BeanUtils.cloneBean(classT);
//                                ClazzLevel classLevel = null;
//                                if (null != item.getClazzLevel()) {
//                                    classLevel = ClazzLevel.parse(item.getClazzLevel());
//                                }
//                                if (classLevel != null && !ClazzLevel.getGraduatedClazzLevels().contains(classLevel)) {
//                                    bean.setClazzLevel(classLevel.getLevel());
//                                    bean.setClazzName(item.getClazzName());
//                                    bean.setRegStuCount(item.getRegStuCount());
//                                    bean.setAuStuCount(item.getAuthStuCount());
//                                    resultMap.put(item.getClazzId(), bean);
//                                }
//                            } catch (Exception e) {
//                            }
//                        });
//                    }
//                }
//
//            } catch (InstantiationException e) {
//                logger.error("error info: ", e);
//            } catch (IllegalAccessException e) {
//                logger.error("error info: ", e);
//            }
//        });
//        return resultMap;
//    }

//    /**
//     * 获取班级online模式数据
//     *
//     * @param schoolIds
//     * @param day
//     * @return
//     */
//    public Map<Long, ClassOnlineReportData> getClassOnlineReportData(Collection<Long> schoolIds, Integer day) {
//        //获取online模式学校信息
//        Map<Long, SchoolOnlineReportData> schoolOnlineReportDataMap = gengeralBaseSchoolReportData(schoolIds, day, SchoolOnlineReportData.class);
//        //获取online模式班级基本信息
//        Map<Long, ClassOnlineReportData> classOnlineReportDataMap = generateBaseClassReportData(schoolOnlineReportDataMap, day, ClassOnlineReportData.class);
//        //分批根据班级ids获取班组信息
//        Map<Long, List<CrmGroupSummary>> crmClassGroupSummaryMap = batchLoadCrmGroupSummary(classOnlineReportDataMap.keySet());
//        //所有班组id
//        List<Long> groupIdsAll = new ArrayList<>();
//        //所有老师id
//        Set<Long> teacherIdsAll = new HashSet<>();
//        crmClassGroupSummaryMap.forEach((k, v) -> {
//            if (CollectionUtils.isNotEmpty(v)) {
//                v.forEach(p -> {
//                    teacherIdsAll.add(p.getTeacherId());
//                    groupIdsAll.add(p.getGroupId());
//                });
//            }
//        });
//
//        //根据班组ids获取班组老师关系信息
//        Map<Long, List<GroupTeacherRef>> refs = batchLoadGroupTeacherRef(groupIdsAll);
//        Map<Long, List<GroupTeacherRef>> refsTempMap = new HashMap<>();
//        refs.forEach((key, refList) -> {
//            //获取有效老师，过滤掉已退出班级的老师
//            refsTempMap.put(key, refList.stream().filter(r -> r.getRefStatus() == RefStatus.VALID).collect(toList()));
//        });
//        //班级、老师映射关系
//        Map<Long, List<Long>> crmClassTeacherSummaryMap = new HashMap<>();
//        crmClassGroupSummaryMap.forEach((k, v) -> {
//            List<Long> teacherIds = new ArrayList<>();
//            if (CollectionUtils.isNotEmpty(v)) {
//                v.stream().forEach(item -> {
//                    List<GroupTeacherRef> groupTeacherRefs = refsTempMap.get(item.getGroupId());
//                    if (CollectionUtils.isNotEmpty(groupTeacherRefs)) {
//                        teacherIds.addAll(groupTeacherRefs.stream().map(GroupTeacherRef::getTeacherId).collect(toList()));
//                    }
//                });
//            }
//            crmClassTeacherSummaryMap.put(k, teacherIds);
//        });
//        //获取所有老师信息
//        Map<Long, CrmTeacherSummary> crmTeacherSummaryMapAll = batchLoadTeacherSummary(teacherIdsAll);
//        //获取隐藏老师
//        Map<Long, AgentHiddenTeacher> agentHiddenTeachers = batchLoadAgentHiddenTeacher(teacherIdsAll);
//        Map<Long, ClassOnlineReportData> classOnlineReportDataMapNew = new HashMap<>();
//        classOnlineReportDataMap.forEach((k, v) -> {
//            if (crmClassTeacherSummaryMap.containsKey(k)) {
//                List<Long> teacherIds = crmClassTeacherSummaryMap.get(k);
//                List<CrmTeacherSummary> teacherSummaryList = new ArrayList<>();
//                for (int i = 0; i < teacherIds.size(); i++) {
//                    Long item = teacherIds.get(i);
//                    CrmTeacherSummary crmTeacherSummary = crmTeacherSummaryMapAll.get(item);
//                    if (null != crmTeacherSummary) {
//                        teacherSummaryList.add(crmTeacherSummary);
//                    }
//                }
//                if (CollectionUtils.isNotEmpty(teacherSummaryList)) {
//                    //过滤假老师和禁用老师、过滤隐藏老师
//                    teacherSummaryList = teacherSummaryList.stream().filter(p -> !Objects.equals(p.getDisabled(), true) && p.getManualFakeTeacher() && !agentHiddenTeachers.containsKey(p.getTeacherId())).collect(toList());
//                    //该班级存在老师，或者存在学生，才可以下载
//                    if (CollectionUtils.isNotEmpty(teacherSummaryList) || v.getRegStuCount() > 0) {
//                        //设置英语老师
//                        v.setEngTeacher(StringUtils.join(teacherSummaryList.stream().filter(teacher -> Subject.safeParse(teacher.getSubject()) == Subject.ENGLISH).map(CrmTeacherSummary::getRealName).collect(toList()), "、"));
//                        //设置数学老师
//                        v.setMathTeacher(StringUtils.join(teacherSummaryList.stream().filter(teacher -> Subject.safeParse(teacher.getSubject()) == Subject.MATH).map(CrmTeacherSummary::getRealName).collect(toList()), "、"));
//                        //设置语文老师
//                        v.setChnTeacher(StringUtils.join(teacherSummaryList.stream().filter(teacher -> Subject.safeParse(teacher.getSubject()) == Subject.CHINESE).map(CrmTeacherSummary::getRealName).collect(toList()), "、"));
//                        classOnlineReportDataMapNew.put(k, v);
//                    }
//                }
//            }
//        });
//
//        //获取online模式班级指标信息
//        Map<Long, AgentClass17PerformanceData> agentClass17PerformanceDataMap = batchLoadClass17PerformanceData(classOnlineReportDataMapNew.keySet(), day);
//        classOnlineReportDataMapNew.forEach((key, value) -> {
//            AgentClass17PerformanceData performanceData = agentClass17PerformanceDataMap.get(key);
//            if (null != performanceData) {
//                AgentClass17PerformanceIndicator indicatorData = performanceData.getIndicatorData();
//                if (null != indicatorData) {
//                    //注册学生数
//                    value.setRegStuCount(indicatorData.getRegStuCount());
//                    //认证学生数
//                    value.setAuStuCount(indicatorData.getAuStuCount());
//                    //本月布置英语作业
//                    value.setTmEngHwSc(indicatorData.getTmEngHwSc());
//                    //本月布置数学作业
//                    value.setTmMathHwSc(indicatorData.getTmMathHwSc());
//                    //本月布置语文作业
//                    value.setTmChnHwSc(indicatorData.getTmChnHwSc());
//                    //英语累计新增
//                    value.setEngSettlementStuCount(indicatorData.getEngSettlementStuCount());
//                    //英语新增1套
//                    value.setTmFinEngHwEq1IncStuCount(indicatorData.getTmFinEngHwEq1IncStuCount());
//                    //英语新增2套
//                    value.setTmFinEngHwEq2IncStuCount(indicatorData.getTmFinEngHwEq2IncStuCount());
//                    //英语新增3套
//                    value.setTmFinEngHwGte3IncAuStuCount(indicatorData.getTmFinEngHwGte3IncAuStuCount());
//                    //英语回流1套
//                    value.setEngBfEq1StuCount(indicatorData.getFinEngHwEq1StuCount() - indicatorData.getTmFinEngHwEq1IncStuCount());
//                    //英语回流2套
//                    value.setEngBfEq2StuCount(indicatorData.getFinEngHwEq2StuCount() - indicatorData.getTmFinEngHwEq2IncStuCount());
//                    //英语回流3套
//                    value.setEngBfGte3StuCount(indicatorData.getFinEngHwGte3AuStuCount() - indicatorData.getTmFinEngHwGte3IncAuStuCount());
//                    //英语本月月活
//                    value.setFinEngHwGte3AuStuCount(indicatorData.getFinEngHwGte3AuStuCount());
//                    //英语上月月活
//                    value.setLmFinEngHwGte3AuStuCount(indicatorData.getLmFinEngHwGte3AuStuCount());
//
//                    //数学累计新增
//                    value.setMathSettlementStuCount(indicatorData.getMathSettlementStuCount());
//                    //数学新增1套
//                    value.setTmFinMathHwEq1IncStuCount(indicatorData.getTmFinMathHwEq1IncStuCount());
//                    //数学新增2套
//                    value.setTmFinMathHwEq2IncStuCount(indicatorData.getTmFinMathHwEq2IncStuCount());
//                    //数学新增3套
//                    value.setTmFinMathHwGte3IncAuStuCount(indicatorData.getTmFinMathHwGte3IncAuStuCount());
//                    //数学回流1套
//                    value.setMathBfEq1StuCount(indicatorData.getFinMathHwEq1StuCount() - indicatorData.getTmFinMathHwEq1IncStuCount());
//                    //数学回流2套
//                    value.setMathBfEq2StuCount(indicatorData.getFinMathHwEq2StuCount() - indicatorData.getTmFinMathHwEq2IncStuCount());
//                    //数学回流3套
//                    value.setMathBfGte3StuCount(indicatorData.getFinMathHwGte3AuStuCount() - indicatorData.getTmFinMathHwGte3IncAuStuCount());
//                    //数学本月月活
//                    value.setFinMathHwGte3AuStuCount(indicatorData.getFinMathHwGte3AuStuCount());
//                    //数学上月月活
//                    value.setLmFinMathHwGte3AuStuCount(indicatorData.getLmFinMathHwGte3AuStuCount());
//
//                    //语文累计新增
//                    value.setChnSettlementStuCount(indicatorData.getChnSettlementStuCount());
//                    //语文新增1套
//                    value.setTmFinChnHwEq1IncStuCount(indicatorData.getTmFinChnHwEq1IncStuCount());
//                    //语文新增2套
//                    value.setTmFinChnHwEq2IncStuCount(indicatorData.getTmFinChnHwEq2IncStuCount());
//                    //语文新增3套
//                    value.setTmFinChnHwGte3IncAuStuCount(indicatorData.getTmFinChnHwGte3IncAuStuCount());
//                    //语文回流1套
//                    value.setChnBfEq1StuCount(indicatorData.getFinChnHwEq1StuCount() - indicatorData.getTmFinChnHwEq1IncStuCount());
//                    //语文回流2套
//                    value.setChnBfEq2StuCount(indicatorData.getFinChnHwEq2StuCount() - indicatorData.getTmFinChnHwEq2IncStuCount());
//                    //语文回流3套
//                    value.setChnBfGte3StuCount(indicatorData.getFinChnHwGte3AuStuCount() - indicatorData.getTmFinChnHwGte3IncAuStuCount());
//                    //语文本月月活
//                    value.setFinChnHwGte3AuStuCount(indicatorData.getFinChnHwGte3AuStuCount());
//                    //语文上月月活
//                    value.setLmFinChnHwGte3AuStuCount(indicatorData.getLmFinChnHwGte3AuStuCount());
//
//                    //布置期末作业包科目
//                    List<String> termReviewList = new ArrayList<>();
//                    if (indicatorData.isEngTermReviewFlag()){
//                        termReviewList.add("英");
//                    }
//                    if (indicatorData.isMathTermReviewFlag()){
//                        termReviewList.add("数");
//                    }
//                    if (indicatorData.isChnTermReviewFlag()){
//                        termReviewList.add("语");
//                    }
//                    value.setTermReviewSubject(StringUtils.join(termReviewList,"、"));
//                    //布置暑假作业科目
//                    List<String> vacnHwList = new ArrayList<>();
//                    if (indicatorData.isEngVacnHwFlag()){
//                        vacnHwList.add("英");
//                    }
//                    if (indicatorData.isMathVacnHwFlag()){
//                        vacnHwList.add("数");
//                    }
//                    if (indicatorData.isChnVacnHwFlag()){
//                        vacnHwList.add("语");
//                    }
//                    value.setVacnHwSubject(StringUtils.join(vacnHwList,"、"));
//                }
//            }
//        });
//        return classOnlineReportDataMapNew;
//    }
//
//    /**
//     * 获取班级offline模式数据
//     *
//     * @param schoolIds
//     * @param day
//     * @return
//     */
//    public Map<Long, ClassOfflineReportData> getClassOfflineReportData(Collection<Long> schoolIds, Integer day) {
//        //获取offline模式学校信息
//        Map<Long, SchoolOfflineReportData> schoolOfflineReportDataMap = gengeralBaseSchoolReportData(schoolIds, day, SchoolOfflineReportData.class);
//        //获取offline模式班级基本信息
//        Map<Long, ClassOfflineReportData> classOfflineReportDataMap = generateBaseClassReportData(schoolOfflineReportDataMap, day, ClassOfflineReportData.class);
//        //分批根据班级ids获取班组信息
//        Map<Long, List<CrmGroupSummary>> crmClassGroupSummaryMap = batchLoadCrmGroupSummary(classOfflineReportDataMap.keySet());
//        //所有班组id
//        List<Long> groupIdsAll = new ArrayList<>();
//        //所有老师id
//        Set<Long> teacherIdsAll = new HashSet<>();
//        crmClassGroupSummaryMap.forEach((k, v) -> {
//            if (CollectionUtils.isNotEmpty(v)) {
//                v.forEach(p -> {
//                    teacherIdsAll.add(p.getTeacherId());
//                    groupIdsAll.add(p.getGroupId());
//                });
//            }
//        });
//
//        //根据班组ids获取班组老师关系信息
//        Map<Long, List<GroupTeacherRef>> refs = batchLoadGroupTeacherRef(groupIdsAll);
//        Map<Long, List<GroupTeacherRef>> refsTempMap = new HashMap<>();
//        refs.forEach((key, refList) -> {
//            //获取有效老师，过滤掉已退出班级的老师
//            refsTempMap.put(key, refList.stream().filter(r -> r.getRefStatus() == RefStatus.VALID).collect(toList()));
//        });
//        //班级、老师映射关系
//        Map<Long, List<Long>> crmClassTeacherSummaryMap = new HashMap<>();
//        crmClassGroupSummaryMap.forEach((k, v) -> {
//            List<Long> teacherIds = new ArrayList<>();
//            if (CollectionUtils.isNotEmpty(v)) {
//                v.stream().forEach(item -> {
//                    List<GroupTeacherRef> groupTeacherRefs = refsTempMap.get(item.getGroupId());
//                    if (CollectionUtils.isNotEmpty(groupTeacherRefs)) {
//                        teacherIds.addAll(groupTeacherRefs.stream().map(GroupTeacherRef::getTeacherId).collect(toList()));
//                    }
//                });
//            }
//            crmClassTeacherSummaryMap.put(k, teacherIds);
//        });
//        //获取所有老师信息
//        Map<Long, CrmTeacherSummary> crmTeacherSummaryMapAll = batchLoadTeacherSummary(teacherIdsAll);
//        //获取隐藏老师
//        Map<Long, AgentHiddenTeacher> agentHiddenTeachers = batchLoadAgentHiddenTeacher(teacherIdsAll);
//
//        Map<Long, ClassOfflineReportData> classOfflineReportDataMapNew = new HashMap<>();
//        classOfflineReportDataMap.forEach((k, v) -> {
//            if (crmClassTeacherSummaryMap.containsKey(k)) {
//                List<Long> teacherIds = crmClassTeacherSummaryMap.get(k);
//                List<CrmTeacherSummary> teacherSummaryList = new ArrayList<>();
//                for (int i = 0; i < teacherIds.size(); i++) {
//                    Long item = teacherIds.get(i);
//                    CrmTeacherSummary crmTeacherSummary = crmTeacherSummaryMapAll.get(item);
//                    if (null != crmTeacherSummary) {
//                        teacherSummaryList.add(crmTeacherSummary);
//                    }
//                }
//                if (CollectionUtils.isNotEmpty(teacherSummaryList)) {
//                    //过滤假老师和禁用老师、过滤隐藏老师
//                    teacherSummaryList = teacherSummaryList.stream().filter(p -> !Objects.equals(p.getDisabled(), true) && p.getManualFakeTeacher() && !agentHiddenTeachers.containsKey(p.getTeacherId())).collect(toList());
//                    //该班级存在老师，或者存在学生，才可以下载
//                    if (CollectionUtils.isNotEmpty(teacherSummaryList) || v.getRegStuCount() > 0) {
//                        //设置老师
//                        List<String> teacherList = new ArrayList<>();
//                        teacherSummaryList.forEach(p -> {
//                            if (null != p) {
//                                String realName = (null != p.getRealName() && !"".equals(p.getRealName())) ? p.getRealName() : String.valueOf(p.getTeacherId());
//                                if (Subject.safeParse(p.getSubject()) == Subject.ENGLISH) {
//                                    teacherList.add(realName + "（英）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.MATH) {
//                                    teacherList.add(realName + "（数）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.CHINESE) {
//                                    teacherList.add(realName + "（语）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.PHYSICS) {
//                                    teacherList.add(realName + "（物）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.CHEMISTRY) {
//                                    teacherList.add(realName + "（化）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.BIOLOGY) {
//                                    teacherList.add(realName + "（生）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.POLITICS) {
//                                    teacherList.add(realName + "（政）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.GEOGRAPHY) {
//                                    teacherList.add(realName + "（地）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.HISTORY) {
//                                    teacherList.add(realName + "（历）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.INFORMATION) {
//                                    teacherList.add(realName + "（信）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.HISTORY_SOCIETY) {
//                                    teacherList.add(realName + "（社）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.SCIENCE) {
//                                    teacherList.add(realName + "（科）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.GENERIC_TECHNOLOGY) {
//                                    teacherList.add(realName + "（通）");
//                                }
//                                if (Subject.safeParse(p.getSubject()) == Subject.UNKNOWN) {
//                                    teacherList.add(realName + "（未）");
//                                }
//                            }
//                        });
//                        v.setTeacher(StringUtils.join(teacherList.stream().collect(toList()), "、"));
//                        classOfflineReportDataMapNew.put(k, v);
//                    }
//                }
//            }
//        });
//
//        //获取当月offline模式班级指标信息
//        Map<Long, AgentClassKlxPerformanceData> agentClassKlxPerformanceDataMap = batchloadClassKlxPerformanceData(classOfflineReportDataMapNew.keySet(), day);
//        //获取上月offline模式班级指标信息
//        Integer lastMonthLastDay = getLastMonthLastDay(day);
//        Map<Long, AgentClassKlxPerformanceData> agentClassKlxPerformanceDataMapLastMonth = batchloadClassKlxPerformanceData(classOfflineReportDataMapNew.keySet(), lastMonthLastDay);
//        classOfflineReportDataMapNew.forEach((key, value) -> {
//            if (agentClassKlxPerformanceDataMap.containsKey(key)) {
//                AgentClassKlxPerformanceData performanceData = agentClassKlxPerformanceDataMap.get(key);
//                if (null != performanceData) {
//                    AgentClassKlxPerformanceIndicator indicatorData = performanceData.getIndicatorData();
//                    if (null != indicatorData) {
//                        //快乐学考号
//                        value.setKlxTnCount(indicatorData.getKlxTnCount());
//                        //普通扫描≥1次学生数
//                        value.setTmFinTpGte1StuCount(indicatorData.getTmFinTpGte1StuCount());
//                        //普通扫描≥3次学生数
//                        value.setTmFinTpGte3StuCount(indicatorData.getTmFinTpGte3StuCount());
//                    }
//                }
//            }
//            if (agentClassKlxPerformanceDataMapLastMonth.containsKey(key)) {
//                AgentClassKlxPerformanceData performanceData = agentClassKlxPerformanceDataMapLastMonth.get(key);
//                if (null != performanceData) {
//                    AgentClassKlxPerformanceIndicator indicatorData = performanceData.getIndicatorData();
//                    if (null != indicatorData) {
//                        //普通扫描≥1次学生数
//                        value.setTmFinTpGte1StuCountLastMonth(indicatorData.getTmFinTpGte1StuCount());
//                        //普通扫描≥3次学生数
//                        value.setTmFinTpGte3StuCountLastMonth(indicatorData.getTmFinTpGte3StuCount());
//                    }
//                }
//            }
//        });
//        return classOfflineReportDataMapNew;
//    }
//
//    private Map<Long, AgentHiddenTeacher> batchLoadAgentHiddenTeacher(Collection<Long> teacherIds) {
//        Map<Long, AgentHiddenTeacher> resultMap = new HashMap<>();
//        AgentResourceService.batchIds(teacherIds, 1000).forEach((k, v) -> {
//            Map<Long, AgentHiddenTeacher> agentHiddenTeachers = agentHiddenTeacherService.getAgentHiddenTeachers(v);
//            resultMap.putAll(agentHiddenTeachers);
//        });
//
//        return resultMap;
//    }
//
//    private Integer getLastMonthLastDay(int day) {
//        Date date = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
//        Date endDate = MonthRange.newInstance(date.getTime()).previous().getEndDate();
//        return Integer.valueOf(DateUtils.dateToString(endDate, "yyyyMMdd"));
//    }
//
//
//    /**
//     * 批量根据学校ID获取老师summary
//     *
//     * @param schoolIds
//     * @return
//     */
//    private Map<Long, List<CrmTeacherSummary>> batchLoadCrmTeacherSummay(Collection<Long> schoolIds) {
//        Map<Long, List<CrmTeacherSummary>> resultMap = new HashMap<>();
//        AgentResourceService.batchIds(schoolIds, 200).forEach((k, v) -> {
//            Map<Long, List<CrmTeacherSummary>> tempSummaryMap = crmSummaryLoaderClient.loadSchoolTeachers(v);
//            resultMap.putAll(tempSummaryMap);
//        });
//        return resultMap;
//    }
//
//    /**
//     * 分批获取学校online模式业绩数据
//     *
//     * @param schoolIds
//     * @param day
//     * @return
//     */
//    private Map<Long, AgentSchool17PerformanceData> batchLoadSchool17PerformanceData(Collection<Long> schoolIds, Integer day) {
//        Map<Long, AgentSchool17PerformanceData> agentSchool17PerformanceDataMap = new HashMap<>();
//        AgentResourceService.batchIds(schoolIds, 1000).forEach((k, v) -> {
//            Map<Long, AgentSchool17PerformanceData> performanceDataMap = loadPerformanceServiceClient.loadSchool17PerformanceData(v, day);
//            agentSchool17PerformanceDataMap.putAll(performanceDataMap);
//        });
//        return agentSchool17PerformanceDataMap;
//    }
//
//    /**
//     * 分批获取学校offline模式业绩数据
//     *
//     * @param schoolIds
//     * @param day
//     * @return
//     */
//    private Map<Long, AgentSchoolKlxPerformanceData> batchLoadSchoolKlxPerformanceData(Collection<Long> schoolIds, Integer day) {
//        Map<Long, AgentSchoolKlxPerformanceData> agentSchoolKlxPerformanceDataMap = new HashMap<>();
//        AgentResourceService.batchIds(schoolIds, 1000).forEach((k, v) -> {
//            Map<Long, AgentSchoolKlxPerformanceData> performanceDataMap = loadPerformanceServiceClient.loadSchoolKlxPerformanceData(v, day);
//            agentSchoolKlxPerformanceDataMap.putAll(performanceDataMap);
//        });
//        return agentSchoolKlxPerformanceDataMap;
//    }
//
//    /**
//     * 分批获取老师online模式业绩数据
//     *
//     * @param teacherIds
//     * @param day
//     * @return
//     */
//    private Map<Long, AgentTeacher17PerformanceData> batchLoadTeacher17PerformanceData(Collection<Long> teacherIds, Integer day) {
//        Map<Long, AgentTeacher17PerformanceData> agentTeacher17PerformanceDataMap = new HashMap<>();
//        List<Future<Map<Long, AgentTeacher17PerformanceData>>> futureList = new ArrayList<>();
//        AgentResourceService.batchIds(teacherIds, 300).forEach((k, v) -> {
//            Future<Map<Long, AgentTeacher17PerformanceData>> futureData = AlpsThreadPool.getInstance().submit(() -> loadPerformanceServiceClient.loadTeacher17PerformanceData(v, day));
//            futureList.add(futureData);
////            Map<Long, AgentTeacher17PerformanceData> performanceDataMap = loadPerformanceServiceClient.loadTeacher17PerformanceData(v, day);
////            agentTeacher17PerformanceDataMap.putAll(performanceDataMap);
//        });
//        for (Future<Map<Long,AgentTeacher17PerformanceData>> futureData : futureList){
//            try{
//                Map<Long, AgentTeacher17PerformanceData> performanceDataMap = futureData.get();
//                agentTeacher17PerformanceDataMap.putAll(performanceDataMap);
//            }catch (Exception e){
//
//            }
//        }
//        return agentTeacher17PerformanceDataMap;
//    }
//
//    /**
//     * 分批获取老师offline模式业绩数据
//     *
//     * @param teacherIds
//     * @param day
//     * @return
//     */
//    private Map<Long, AgentTeacherKlxPerformanceData> batchLoadTeacherKlxPerformanceData(Collection<Long> teacherIds, Integer day) {
//        Map<Long, AgentTeacherKlxPerformanceData> agentTeacherKlxPerformanceDataMap = new HashMap<>();
//        AgentResourceService.batchIds(teacherIds, 1000).forEach((k, v) -> {
//            Map<Long, AgentTeacherKlxPerformanceData> performanceDataMap = loadPerformanceServiceClient.loadTeacherKlxPerformanceData(v, day);
//            agentTeacherKlxPerformanceDataMap.putAll(performanceDataMap);
//        });
//        return agentTeacherKlxPerformanceDataMap;
//    }
//
//
//    /**
//     * 分批获取人员与学校关系信息
//     *
//     * @param schoolIds
//     * @return
//     */
//    private List<AgentUserSchool> batchLoadUserSchoolData(Collection<Long> schoolIds) {
//        List<AgentUserSchool> agentUserSchoolList = new ArrayList<>();
//        AgentResourceService.batchIds(schoolIds, 1000).forEach((k, v) -> {
//            List<AgentUserSchool> agentUserSchools = agentUserSchoolLoaderClient.findBySchoolIds(v);
//            agentUserSchoolList.addAll(agentUserSchools);
//        });
//        return agentUserSchoolList;
//    }
//
//    /**
//     * 分批获取学校扩展信息
//     *
//     * @param schoolIds
//     * @return
//     */
//    private Map<Long, SchoolExtInfo> batchLoadSchoolsExtInfoData(Collection<Long> schoolIds) {
//        Map<Long, SchoolExtInfo> agentSchoolExtInfoMap = new HashMap<>();
//        AgentResourceService.batchIds(schoolIds, 1000).forEach((k, v) -> {
//            Map<Long, SchoolExtInfo> schoolExtInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(v).getUninterruptibly();
//            agentSchoolExtInfoMap.putAll(schoolExtInfoMap);
//        });
//        return agentSchoolExtInfoMap;
//    }
//
//    /**
//     * 分批获取班级online模式业绩数据
//     *
//     * @param classIds
//     * @param day
//     * @return
//     */
//    private Map<Long, AgentClass17PerformanceData> batchLoadClass17PerformanceData(Collection<Long> classIds, Integer day) {
//        Map<Long, AgentClass17PerformanceData> agentClass17PerformanceDataMap = new HashMap<>();
//        AgentResourceService.batchIds(classIds, 1000).forEach((k, v) -> {
//            Map<Long, AgentClass17PerformanceData> performanceDataMap = loadPerformanceServiceClient.loadClass17PerformanceData(v, day);
//            agentClass17PerformanceDataMap.putAll(performanceDataMap);
//        });
//        return agentClass17PerformanceDataMap;
//    }
//
//    /**
//     * 分批获取班级offline模式业绩数据
//     *
//     * @param classIds
//     * @param day
//     * @return
//     */
//    private Map<Long, AgentClassKlxPerformanceData> batchloadClassKlxPerformanceData(Collection<Long> classIds, Integer day) {
//        Map<Long, AgentClassKlxPerformanceData> agentClassKlxPerformanceDataMap = new HashMap<>();
//        AgentResourceService.batchIds(classIds, 1000).forEach((k, v) -> {
//            Map<Long, AgentClassKlxPerformanceData> performanceDataMap = loadPerformanceServiceClient.loadClassKlxPerformanceData(v, day);
//            agentClassKlxPerformanceDataMap.putAll(performanceDataMap);
//        });
//        return agentClassKlxPerformanceDataMap;
//    }

    private ChoiceContent createChoiceContent(AgentGroup group) {
        ChoiceContent content = new ChoiceContent();
        content.setName(group.getGroupName());
        content.setId(group.getId());
        return content;
    }


    private ChoiceContent createChoiceContent(AgentUser user) {
        ChoiceContent content = new ChoiceContent();
        content.setId(user.getId());
        content.setName(user.getRealName());
        return content;
    }
//
//    /**
//     * 分批根据学校ids获取班级
//     *
//     * @param schoolIds
//     * @return
//     */
//    private Map<Long, List<CrmClazzSummary>> batchLoadCrmClassSummary(Collection<Long> schoolIds) {
//        Map<Long, List<CrmClazzSummary>> crmClazzSummaryMap = new HashMap<>();
//        if (CollectionUtils.isNotEmpty(schoolIds)) {
//            AgentResourceService.batchIds(schoolIds, 1000).forEach((k, v) -> {
//                crmClazzSummaryMap.putAll(crmSummaryLoaderClient.getCrmClazzSummaryBySchoolIds(v));
//            });
//        }
//        return crmClazzSummaryMap;
//    }
//
//    /**
//     * 分批根据班级ids获取班组信息
//     *
//     * @param classIds
//     * @return
//     */
//    private Map<Long, List<CrmGroupSummary>> batchLoadCrmGroupSummary(Collection<Long> classIds) {
//        Map<Long, List<CrmGroupSummary>> crmGroupSummaryMap = new HashMap<>();
//        if (CollectionUtils.isNotEmpty(classIds)) {
//            AgentResourceService.batchIds(classIds, 1000).forEach((k, v) -> {
//                crmGroupSummaryMap.putAll(crmSummaryLoaderClient.getCrmGroupSummaryByClassIds(v));
//            });
//        }
//        return crmGroupSummaryMap;
//    }
//
//    /**
//     * 分批获取老师信息
//     *
//     * @param teacherIds
//     * @return
//     */
//    private Map<Long, CrmTeacherSummary> batchLoadTeacherSummary(Collection<Long> teacherIds) {
//        Map<Long, CrmTeacherSummary> crmTeacherSummaryMap = new HashMap<>();
//        if (CollectionUtils.isNotEmpty(teacherIds)) {
//            AgentResourceService.batchIds(teacherIds, 1000).forEach((k, v) -> {
//                crmTeacherSummaryMap.putAll(crmSummaryLoaderClient.loadTeacherSummary(v));
//            });
//        }
//        return crmTeacherSummaryMap;
//    }
//
//    /**
//     * 分批根据班组ids获取班组老师关系信息
//     *
//     * @param groupIds
//     * @return
//     */
//    private Map<Long, List<GroupTeacherRef>> batchLoadGroupTeacherRef(Collection<Long> groupIds) {
//        Map<Long, List<GroupTeacherRef>> groupTeacherRefMap = new HashMap<>();
//        if (CollectionUtils.isNotEmpty(groupIds)) {
//            AgentResourceService.batchIds(groupIds, 1000).forEach((k, v) -> {
//                groupTeacherRefMap.putAll(asyncGroupServiceClient.getAsyncGroupService().findGroupTeacherRefs(v).getUninterruptibly());
//            });
//        }
//        return groupTeacherRefMap;
//    }
}
