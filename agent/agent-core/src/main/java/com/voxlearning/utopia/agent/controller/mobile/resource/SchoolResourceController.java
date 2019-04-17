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

package com.voxlearning.utopia.agent.controller.mobile.resource;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.SchoolBasicInfo;
import com.voxlearning.utopia.agent.bean.performance.school.SchoolOnlineIndicatorData;
import com.voxlearning.utopia.agent.bean.performance.school.SchoolParentIndicatorData;
import com.voxlearning.utopia.agent.bean.resource.SingleSubjectAnsh;
import com.voxlearning.utopia.agent.bean.school.AgentHighPotentialSchoolInfo;
import com.voxlearning.utopia.agent.bean.school.AgentMauTopSchoolInfo;
import com.voxlearning.utopia.agent.bean.school.SchoolKeymanListResult;
import com.voxlearning.utopia.agent.constants.AgentTagType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.mapper.ClazzAlterMapper;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResource;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResourceExtend;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.mobile.AgentHiddenTeacherService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.mobile.SchoolClueService;
import com.voxlearning.utopia.agent.service.mobile.resource.*;
import com.voxlearning.utopia.agent.service.mobile.v2.CrmVisitPlanService;
import com.voxlearning.utopia.agent.service.organization.AgentOuterResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.agent.view.school.SchoolBasicData;
import com.voxlearning.utopia.agent.view.school.SchoolBasicExtData;
import com.voxlearning.utopia.agent.view.school.SchoolGradeBasicData;
import com.voxlearning.utopia.agent.view.school.SchoolPositionData;
import com.voxlearning.utopia.agent.view.teacher.TeacherBasicInfo;
import com.voxlearning.utopia.agent.view.teacher.TeacherSubject;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.Subjects;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.entity.crm.constants.SchoolOperationType;
import com.voxlearning.utopia.entity.crm.constants.UserPlatformType;
import com.voxlearning.utopia.entity.crm.schoolrecord.SchoolServiceRecord;
import com.voxlearning.utopia.mapper.SchoolEsInfo;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolServiceRecordServiceClient;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationState;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.agent.constants.AgentErrorCode.SCHOOL_RESOURCE_INFO_ERROR;
import static java.util.stream.Collectors.toList;

/**
 * 学校资源页面相关请求
 * Created by Yuechen.Wang on 2016/7/11.
 */
@Controller
@RequestMapping("/mobile/resource/school")
public class SchoolResourceController extends AbstractAgentController {

    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private CrmVisitPlanService crmVisitPlanService;
    @Inject
    private AgentResourceService agentResourceService;
    @Inject
    private AgentResourceMapperService agentResourceMapperService;
    @Inject
    private SchoolResourceService schoolResourceService;

    @Inject
    private AgentDictSchoolService agentDictSchoolService;

    @Inject
    private SchoolServiceRecordServiceClient schoolServiceRecordServiceClient;
//
//    @Inject
//    private SchoolMauIncreaseStatisticsService schoolMauIncreaseStatisticsService;

    @Inject
    private TeacherResourceService teacherResourceService;

    @Inject
    private AgentHiddenTeacherService agentHiddenTeacherService;

    @Inject
    private SearchService searchService;
    @Inject
    private SchoolClueService schoolClueService;
    @Inject
    private GradeResourceService gradeResourceService;
    @Inject
    private PerformanceService performanceService;
    @Inject
    private AgentRequestSupport agentRequestSupport;
    @Inject
    private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;

    @Inject
    private AgentOuterResourceService agentOuterResourceService;
    @Inject
    private AgentTagService agentTagService;

    private static final Long OTHER_ID = 99999999L;

    /**
     * 学校搜索
     *
     * @return
     */
    @RequestMapping(value = "search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchSchoolList(HttpServletRequest request) {
        String searchKey = getRequestString("searchKey");
        if (StringUtils.isBlank(searchKey)) {
            return MapMessage.errorMessage("请输入查询条件");
        }
        Integer scene = getRequestInt("scene", 1);
        searchKey = searchKey.trim();

        Double longitude = getRequestDouble("longitude");   //经度
        Double latitude = getRequestDouble("latitude");     //纬度
        Integer pageNo = getRequestInt("pageNo");       //第几页
        Integer pageSize = getRequestInt("pageSize");   //每页数量

        longitude = longitude != 0D ? longitude : null;
        latitude = latitude != 0D ? latitude : null;

        if (longitude != null && latitude != null) {
            //坐标转化
            String coordinateType;
            if (agentRequestSupport.isIOSRequest(request)) {
                coordinateType = "wgs84ll";//getRequestString("coordinateType");
            } else {
                coordinateType = "autonavi";
            }
            MapMessage address = AmapMapApi.getAddress(ConversionUtils.toString(latitude), ConversionUtils.toString(longitude), coordinateType);
            if (!address.isSuccess()) {
                return address;
            }

            String latitudeStr = ConversionUtils.toString(address.get("latitude"));
            String longitudeStr = ConversionUtils.toString(address.get("longitude"));

            latitude = StringUtils.isNotBlank(latitudeStr) ? Double.parseDouble(latitudeStr) : null;
            longitude = StringUtils.isNotBlank(longitudeStr) ? Double.parseDouble(longitudeStr) : null;
        }

        AuthCurrentUser user = getCurrentUser();
        Long userId = user.getUserId();
        if (user.isProductOperator() && user.getShadowId() != null) { // 如果是产品运营角色， 则使用影子账号，以全国总监的身份查看数据
            userId = user.getShadowId();
        }
        try {
            Page<SchoolEsInfo> esInfoPage = searchService.searchSchoolPageForScene(userId, searchKey, scene, longitude, latitude, pageNo, pageSize);
            List<SchoolBasicInfo> schoolBasicInfoList = schoolResourceService.generateSchoolBasicInfoWithPage(esInfoPage, userId);
//            List<SchoolBasicInfo> schoolCardList = schoolResourceService.searchSchool(userId, schoolKey, scene);
            // 按照字典表非字典表排个序
            schoolBasicInfoList.sort((s2, s1) -> Boolean.compare(s1.getIsDictSchool(), s2.getIsDictSchool()));

            //添加学校有无竞品标识
            schoolBasicInfoList.forEach(p -> {
                SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(p.getSchoolId()).getUninterruptibly();
                if (null != schoolExtInfo && null != schoolExtInfo.getCompetitiveProductFlag()) {
                    p.setCompetitiveProductFlag(schoolExtInfo.getCompetitiveProductFlag());
                } else {
                    p.setCompetitiveProductFlag(0);
                }
            });

            Map<String, Object> dataMap = new HashMap<>();

            List<Long> schoolIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(esInfoPage.getContent())) {
                esInfoPage.getContent().stream().forEach(p -> schoolIds.add(SafeConverter.toLong(p.getId())));
            }
            //判断是否没有更多数据了
            if (schoolIds.size() < pageSize) {
                dataMap.put("noMoreData", true);
            } else {
                dataMap.put("noMoreData", false);
            }
            dataMap.put("schoolList", schoolBasicInfoList);
            return MapMessage.successMessage().add("dataMap", dataMap);
        } catch (Exception ex) {
            logger.error("Failed searching school, user={}, key={}", userId, searchKey, ex);
            return MapMessage.errorMessage("查询学校失败");
        }
    }

    /**
     * 进校查询学校功能
     * 1. 专员搜索范围:  全市, 权限范围学校 + 本市非字典表学校
     * 2. 市经理搜索范围: 全市, 权限范围内地区 + 本市非字典表学校
     * 3. 大区经理搜索范围: 管辖大区下所有学校
     * 4. 全国总监搜索范围: 全国所有学校
     * <p>
     * 现在走范围2
     */
    @RequestMapping(value = "into_school_search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage intoSchoolSearchList() {
        String schoolKey = getRequestString("schoolKey");
//        if (StringUtils.isBlank(schoolKey)) {
//            return MapMessage.errorMessage("请输入查询条件");
//        }
        Integer scene = getRequestInt("scene", 1);
        schoolKey = schoolKey.trim();
        AuthCurrentUser user = getCurrentUser();
        Long userId = user.getUserId();
        if (user.isProductOperator() && user.getShadowId() != null) { // 如果是产品运营角色， 则使用影子账号，以全国总监的身份查看数据
            userId = user.getShadowId();
        }
        try {
            List<SchoolBasicInfo> schoolCardList = schoolResourceService.searchSchool(userId, schoolKey, scene);
            // 按照字典表非字典表排个序
            schoolCardList.sort((s2, s1) -> Boolean.compare(s1.getIsDictSchool(), s2.getIsDictSchool()));

            //添加学校有无竞品标识
            schoolCardList.forEach(p -> {
                SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(p.getSchoolId()).getUninterruptibly();
                if (null != schoolExtInfo && null != schoolExtInfo.getCompetitiveProductFlag()) {
                    p.setCompetitiveProductFlag(schoolExtInfo.getCompetitiveProductFlag());
                } else {
                    p.setCompetitiveProductFlag(0);
                }
            });

            return MapMessage.successMessage().add("schoolList", schoolCardList);
        } catch (Exception ex) {
            logger.error("Failed searching school, user={}, key={}", userId, schoolKey, ex);
            return MapMessage.errorMessage("查询学校失败");
        }
    }

    /**
     * 学校基本信息
     *
     * @return
     */
    @RequestMapping(value = "school_basic_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolBasicInfo() {
        Long schoolId = getRequestLong("schoolId");
        if (!searchService.hasSchoolPermission(getCurrentUserId(), schoolId, SearchService.SCENE_SEA)) {
            return MapMessage.errorMessage(StringUtils.formatMessage("您无查看该学校权限:{}", schoolId));
        }

        // 获取学校基本信息
        SchoolBasicInfo schoolBasicInfo = schoolResourceService.generateSchoolBasicInfoById(schoolId);
        if (schoolBasicInfo == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }

        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("dataMap", schoolBasicInfo);
        return mapMessage;
    }

    /**
     * 关键人物名单
     *
     * @return
     */
    @RequestMapping(value = "school_kp_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolKpInfo() {
        Long schoolId = getRequestLong("schoolId");
        // 获取学校基本信息
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        MapMessage map = MapMessage.successMessage();
        Map<String, Object> schoolInfoMap = new HashMap<>();
        Map<String, Object> schoolKpInfo = new HashMap<>();
        if (null != school && null != schoolExtInfo) {
            schoolInfoMap.put("schoolName", school.getCname());
            schoolInfoMap.put("address", schoolExtInfo.getAddress());
            schoolInfoMap.put("schoolLevel", SchoolLevel.safeParse(school.getLevel()));

            //获取学校关键kp
            schoolKpInfo = schoolResourceService.fetchSchoolKpInfo(schoolId, SchoolLevel.safeParse(school.getLevel()));
        }
        map.put("schoolInfoMap", schoolInfoMap);
        map.put("schoolKpInfo", schoolKpInfo);
        return map;
    }

    /**
     * 学校概览数据
     *
     * @return
     */
    @RequestMapping(value = "school_overview_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchool17Performance() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
//        School17PerformanceVO school17Performance = schoolResourceService.getSchool17Performance(schoolId);
        SchoolOnlineIndicatorData schoolOnlineIndicator = schoolResourceService.getSchoolOnlineIndicator(school);
        return MapMessage.successMessage().add("dataMap", schoolOnlineIndicator);
    }

    /**
     * 获取快乐学模式业绩
     *
     * @return
     */
    @RequestMapping(value = "school_klx_performance.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchoolKlxPerformance() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        Map<String, Object> klxPerformance = schoolResourceService.getSchoolKlxPerformance(schoolId);
        return MapMessage.successMessage().add("schoolKlxPerformance", klxPerformance);
    }

    /**
     * 分科目周测情况
     *
     * @return
     */
    @RequestMapping(value = "school_klx_subject_week_test.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchoolKlxSubjectWeekTest() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        return MapMessage.successMessage().add("dataList", schoolResourceService.getSchoolKlxSubjectWeekTest(schoolId));
    }

    /**
     * 获取快乐学模式柱状图数据
     *
     * @return
     */
    @RequestMapping(value = "school_klx_chart.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchoolKlxChart() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        return MapMessage.successMessage().add("data", schoolResourceService.generateSchoolKlxChartInfo(schoolId));
    }


    /**
     * OTO扫面权限
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "oto_auth.vpage", method = RequestMethod.GET)
    public String otoAuth(Model model) {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return errorInfoPage(SCHOOL_RESOURCE_INFO_ERROR, StringUtils.formatMessage("无效的学校ID:{}", schoolId), model);
        }
        model.addAttribute("schoolId", schoolId);
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        Map<String, Object> schoolKlxPrivilegeInfo = new HashMap<>();
        schoolKlxPrivilegeInfo.put("scanNumberDigit", schoolExtInfo == null ? null : schoolExtInfo.getScanNumberDigit());
        schoolKlxPrivilegeInfo.put("scanMachineFlag", schoolExtInfo != null && Boolean.TRUE.equals(schoolExtInfo.getScanMachineFlag()));
        schoolKlxPrivilegeInfo.put("questionCardFlag", schoolExtInfo != null && Boolean.TRUE.equals(schoolExtInfo.getQuestionCardFlag()));
        schoolKlxPrivilegeInfo.put("barcodeAnswerQuestionFlag", schoolExtInfo != null && Boolean.TRUE.equals(schoolExtInfo.getBarcodeAnswerQuestionFlag()));
        schoolKlxPrivilegeInfo.put("questionBankFlag", schoolExtInfo != null && Boolean.TRUE.equals(schoolExtInfo.getQuestionBankFlag()));
        schoolKlxPrivilegeInfo.put("a3AnswerQuestionFlag", schoolExtInfo != null && Boolean.TRUE.equals(schoolExtInfo.getA3AnswerQuestionFlag()));
        schoolKlxPrivilegeInfo.put("manualAnswerQuestionFlag", schoolExtInfo != null && Boolean.TRUE.equals(schoolExtInfo.getManualAnswerQuestionFlag()));
        List<Map<String, Object>> subjects = new ArrayList<>();
        Set<Subject> selectedSubjects = schoolExtInfo != null ? schoolExtInfo.loadValidSubjects() : SchoolExtInfo.DefaultSubjects;
        List<String> defaultNameList = SchoolExtInfo.DefaultSubjects.stream().map(Subject::getValue).collect(toList());
        subjects.add(MapUtils.m(
                "name", StringUtils.join(defaultNameList, "、"),
                "value", "",
                "opened", true,
                "showOpenBtn", false
        ));
        Subjects.ALL_SUBJECTS.forEach(subject -> {
            if (!SchoolExtInfo.DefaultSubjects.contains(subject)) {
                subjects.add(MapUtils.m(
                        "name", subject.getValue(),
                        "value", subject.name(),
                        "opened", selectedSubjects.contains(subject),
                        "showOpenBtn", true
                ));
            }
        });
        schoolKlxPrivilegeInfo.put("subjects", subjects);
        model.addAttribute("schoolKlxPrivilegeInfo", schoolKlxPrivilegeInfo);
        return "rebuildViewDir/mobile/resource/oto_auth";
    }

    @RequestMapping(value = "oto_auth_set.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setOTOAuth() {
        //接受的参数
        //学校ID
        Long schoolId = getRequestLong("schoolId");
        //授权类型 1：阅卷及答题卡相关权限,2：学科扫描权限
        int authType = getRequestInt("authType");
        //权限名称，直接字段名称或枚举名称
        String authName = getRequestString("authName");
        //是否开通
        Boolean opened = requestBoolean("opened");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        //责任区域场景，判断该用户是否有权限操作，若无权限，返回学校负责人员
        MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(getCurrentUserId(), schoolId, SearchService.SCENE_DICT);
        if (!mapMessage.isSuccess()) {
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))) {
                return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限", mapMessage.get("schoolManager")));
            } else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        if (agentDictSchool == null) {
            return MapMessage.errorMessage("请先将学校申请加入字典表再开通");
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo == null || !Objects.equals(Boolean.TRUE, schoolExtInfo.getScanMachineFlag())) {
            return MapMessage.errorMessage("请先申请开通阅卷机权限后，再申请开通其它权限");
        }
        if (null == opened) {
            MapMessage.errorMessage(StringUtils.formatMessage("无效的开通结果:{}", opened));
        }
        //阅卷及答题卡相关权限
        if (1 == authType) {
            switch (authName) {
                case "questionBankFlag":
                    schoolExtInfo.setQuestionBankFlag(opened);
                    break;
                case "questionCardFlag":
                    schoolExtInfo.setQuestionCardFlag(opened);
                    break;
                case "barcodeAnswerQuestionFlag":
                    schoolExtInfo.setBarcodeAnswerQuestionFlag(opened);
                    break;
                case "a3AnswerQuestionFlag":
                    schoolExtInfo.setA3AnswerQuestionFlag(opened);
                    break;
                case "manualAnswerQuestionFlag":
                    schoolExtInfo.setManualAnswerQuestionFlag(opened);
                    break;
                default:
                    return MapMessage.errorMessage(StringUtils.formatMessage("无效的权限名称:{}", authName));
            }
        } else if (2 == authType) {
            //学科扫描权限
            Subject subject = Subject.valueOf(authName);
            if (null == subject || SchoolExtInfo.DefaultSubjects.contains(subject)) {
                MapMessage.errorMessage(StringUtils.formatMessage("无效的学科类型:{}", authName));
            } else {
                if (opened) {
                    schoolExtInfo.addSubject(subject);
                } else {
                    schoolExtInfo.removeSubject(subject);
                }
            }
        } else {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的权限类型:{}", authType));
        }
        schoolExtServiceClient.getSchoolExtService()
                .upsertSchoolExtInfo(schoolExtInfo)
                .awaitUninterruptibly();
        List<String> content = new ArrayList<>();
        content.add("schoolId:" + schoolId);
        content.add("authType:" + authType);
        content.add("authName:" + authName);
        content.add("opened:" + opened);
        saveAgentSchoolServiceRecord(schoolId, school.getCname(), StringUtils.join(content, ","), SchoolOperationType.SET_OTO_AUTH);
        return MapMessage.successMessage();
    }

    private void saveAgentSchoolServiceRecord(Long schoolId, String schoolName, String operationContent, SchoolOperationType schoolOperationType) {
        SchoolServiceRecord schoolServiceRecord = new SchoolServiceRecord();
        schoolServiceRecord.setSchoolId(schoolId);
        schoolServiceRecord.setSchoolName(schoolName);
        schoolServiceRecord.setOperatorId(getCurrentUserId().toString());
        schoolServiceRecord.setOperatorName(getCurrentUser().getRealName());
        schoolServiceRecord.setUserPlatformType(UserPlatformType.AGENT);
        schoolServiceRecord.setSchoolOperationType(schoolOperationType);
        schoolServiceRecord.setOperationContent(operationContent);
        schoolServiceRecordServiceClient.addSchoolServiceRecord(schoolServiceRecord);
    }

    @RequestMapping(value = "single_subject_ansh.vpage", method = RequestMethod.GET)
    public String singleSubjectAnsh(Model model) {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return errorInfoPage(SCHOOL_RESOURCE_INFO_ERROR, StringUtils.formatMessage("无效的学校ID:{}", schoolId), model);
        }
        model.addAttribute("schoolId", schoolId);
        List<SingleSubjectAnsh> singleSubjectAnshes = schoolResourceService.getSingleSubjectAnshBySchoolId(schoolId);
        model.addAttribute("singleSubjectAnshes", singleSubjectAnshes);
        return "rebuildViewDir/mobile/resource/school_single_subject_ansh";
    }

    /**
     * 学校加入拜访计划
     */
    @RequestMapping(value = "add_plan.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addSchoolPlan() {
        AuthCurrentUser user = getCurrentUser();
        if (user.isProductOperator()) { // 产品运营角色 只有查看资源部分数据的功能， 没有操作功能
            return MapMessage.errorMessage("您没有权限操作该功能！");
        }
        Long schoolId = getRequestLong("schoolId");
        if (schoolId == 0L) {
            return MapMessage.errorMessage("未选择学校");
        }
        Date planDate = getRequestDate("planDate");
        String planNote = getRequestString("planNote");

        if (planDate == null) {
            return MapMessage.errorMessage("请填写有效的拜访时间");
        }
        try {
            return crmVisitPlanService.saveCrmVisitPlan(schoolId, getCurrentUser(), planDate, planNote);
        } catch (Exception ex) {
            logger.error("Failed add visit plan on school card page.", ex);
            return MapMessage.errorMessage("加入计划服务异常，原因:{}，请于报错群反馈，我们将尽快为您解决~", ex.getMessage());
        }
    }

    /**
     * 本校老师列表页面
     * 1. 过滤掉人工判假的老师
     * 2. 按照老师所教的科目分组
     */
    @RequestMapping(value = "teacher_list.vpage", method = RequestMethod.GET)
    public String teacherList(Model model) {
        //Long userId = getCurrentUserId();
        Long schoolId = getRequestLong("schoolId");
        String modeType = requestString("modeType", "online");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return errorInfoPage(SCHOOL_RESOURCE_INFO_ERROR, StringUtils.formatMessage("无效的学校ID:{}", schoolId), model);
        }
        boolean hide = getRequestBool("hide");       //1.显示的老师的列表 2.隐藏的老师列表
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("level", school.getLevel());
        model.addAttribute("hide", hide);
        if (school.isMiddleSchool() || school.isJuniorSchool() || school.isSeniorSchool()) {
            model.addAttribute("modeType", modeType);
        }
        return "rebuildViewDir/mobile/resource/school_teacher";
    }

    // 查找学校中的老师信息
    @RequestMapping(value = "find_teacher_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findTeacherList() {
        Long schoolId = getRequestLong("schoolId");
        String teacherKey = getRequestString("teacherKey");
        boolean hide = getRequestBool("hide");
        boolean all = getRequestBool("all");
        Integer subject = getRequestInt("subject"); //科目： 1 英语  2 数学  3 语文 4 其他
        Integer sortType = getRequestInt("sortType");      //排序类型：1 按照本月布置排序    2 按上月月活排序
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校信息未找到");
        }
        Set<Long> teacherIds = new HashSet<>();
        boolean includeFakeTeacher = false;
        if (StringUtils.isBlank(teacherKey)) {
            teacherIds.addAll(teacherLoaderClient.loadSchoolTeacherIds(schoolId));
        } else {
            List<Long> managedSchools = new ArrayList<>();
            managedSchools.add(schoolId);
            teacherIds.addAll(searchService.searchTeachersInSchoolsWithNew(managedSchools, teacherKey));
            if (MobileRule.isMobile(teacherKey) || SafeConverter.toLong(teacherKey) > 0) {
                includeFakeTeacher = true;
            }
        }

        MapMessage message = MapMessage.successMessage();

        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        Set<Long> teacherIdsNew = teacherMap.values()
                .stream()
                .filter(p -> {
                    if (subject == 4) {
                        return p.getSubject() != Subject.ENGLISH && p.getSubject() != Subject.MATH && p.getSubject() != Subject.CHINESE;
                    } else if (subject != 0) {
                        return p.getSubject() != null && Objects.equals(p.getSubject().getKey(), subject);
                    }
                    return true;
                })
                .map(Teacher::getId)
                .collect(Collectors.toSet());

        List<TeacherBasicInfo> teacherCardListNew = new ArrayList<>();
        List<TeacherBasicInfo> teacherCardList = teacherResourceService.generateTeacherBasicInfo(teacherIdsNew, true, includeFakeTeacher, true, false);
        for (TeacherBasicInfo teacher : teacherCardList) {
            if (null != teacher) {
                List<TeacherSubject> subjects = teacher.getSubjects();
                if (CollectionUtils.isNotEmpty(subjects)) {
                    for (TeacherSubject teacherSubject : subjects) {
                        TeacherBasicInfo teacherBasicInfo = new TeacherBasicInfo();
                        try {
                            teacherBasicInfo = (TeacherBasicInfo) BeanUtils.cloneBean(teacher);
                        } catch (Exception e) {
                            logger.error("copy bean failed");
                        }
                        teacherBasicInfo.setSubjects(new ArrayList<>());
                        teacherBasicInfo.getSubjects().add(teacherSubject);
                        teacherCardListNew.add(teacherBasicInfo);
                    }
                }
            }
        }

        if (sortType > 0) {
            teacherCardListNew.sort((o1, o2) -> {
                int sortValue1 = getSchoolTeacherSortValue(o1, sortType);
                int sortValue2 = getSchoolTeacherSortValue(o2, sortType);
                return Integer.compare(sortValue2, sortValue1);
            });
        }

        message.add("regCnt", teacherCardListNew.size());
        message.add("authCnt", teacherCardListNew.stream().filter(p -> p.getAuthState() == 1).count());
        message.add("hides", teacherCardListNew.stream().filter(p -> SafeConverter.toBoolean(p.getIsHidden())).count());
        if (!all) {
            if (hide) {
                teacherCardListNew = teacherCardListNew.stream().filter(p -> SafeConverter.toBoolean(p.getIsHidden())).collect(toList());
            } else {
                teacherCardListNew = teacherCardListNew.stream().filter(p -> !SafeConverter.toBoolean(p.getIsHidden())).collect(toList());
            }
        }
        message.add("teacherList", teacherCardListNew);
        message.add("hide", hide);
        return message;
    }

    /**
     * 获取学校老师排序字段
     *
     * @param teacherBasicInfo
     * @param sortType
     * @return
     */
    public int getSchoolTeacherSortValue(TeacherBasicInfo teacherBasicInfo, Integer sortType) {
        int sortValue = 0;
        List<TeacherSubject> subjects = teacherBasicInfo.getSubjects();
        if (CollectionUtils.isNotEmpty(subjects)) {
            TeacherSubject teacherSubject = subjects.get(0);
            if (teacherSubject != null) {
                Map<String, Object> kpiData = teacherSubject.getKpiData();
                if (MapUtils.isNotEmpty(kpiData)) {
                    if (sortType == 1) {
                        sortValue = SafeConverter.toInt(kpiData.get("tmHwSc"));
                    } else if (sortType == 2) {
                        sortValue = SafeConverter.toInt(kpiData.get("lmFinCsHwGte3AuStuCount"));
                    }
                }
            }
        }
        return sortValue;
    }

    /**
     * 获取学校的年级分布
     *
     * @return
     */
    @RequestMapping(value = "school_grade_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolGradeList() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校信息未找到");
        }
        // 获取学校的年级分布
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<ClazzLevel> clazzLevelList = gradeResourceService.getSchoolGradeList(school);
        for (ClazzLevel clazzLevel : clazzLevelList) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("level", clazzLevel.getLevel());
            dataMap.put("description", clazzLevel.getDescription());
            dataList.add(dataMap);
        }
        return MapMessage.successMessage().add("dataList", dataList);
    }

//    // 年级列表
//    @RequestMapping(value = "grade_info.vpage", method = RequestMethod.GET)
//    public String gradeInfo(Model model) {
//        Long schoolId = getRequestLong("schoolId");
//        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
//        if (school == null) {
//            return errorInfoPage(SCHOOL_RESOURCE_INFO_ERROR, StringUtils.formatMessage("无效的学校ID:{}", schoolId), model);
//        }
//        model.addAttribute("schoolId", schoolId);
//        if (SchoolLevel.safeParse(school.getLevel(), null) != null) {
//            model.addAttribute("schoolLevel", school.getLevel());
//        }
//        //model.addAttribute("grade_info", agentResourceMapperService.mapGradeInfo(school, getCurrentUserId()));
//        List<GradeResourceCard> gradeInfo = agentResourceMapperService.mapGradeInfoNew(school);
//        // 按年级排序
//        gradeInfo.sort(Comparator.comparingInt(GradeResourceCard::getGradeLevel));
//        // 对年级下面的班排序
//        gradeInfo.forEach(p -> {
//            if (CollectionUtils.isEmpty(p.getClazzList())) {
//                return;
//            }
//            (p.getClazzList()).sort((o1, o2) -> {
//                int order1 = AgentResourceMapperService.fetchClazzOrder(o1.getShortName());
//                int order2 = AgentResourceMapperService.fetchClazzOrder(o2.getShortName());
//                return Integer.compare(order1, order2);
//            });
//        });
//        model.addAttribute("class_info", gradeInfo);
//        return "rebuildViewDir/mobile/resource/school_grade";
//    }
//
//    @RequestMapping(value = "grade_info_map.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage gradeInfoMap() {
//        Long schoolId = getRequestLong("schoolId");
//        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
//        if (school == null) {
//            return MapMessage.errorMessage("学校信息未找到");
//        }
//        return MapMessage.successMessage().add("grade_info", agentResourceMapperService.mapGradeInfo(school));
//    }

    /**
     * 取消隐藏老师
     *
     * @return
     */
    @RequestMapping(value = "show_teacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage showTeacher() {
        AuthCurrentUser user = getCurrentUser();
        if (user.isProductOperator()) { // 产品运营角色 只有查看资源部分数据的功能， 没有操作功能
            return MapMessage.errorMessage("您没有权限操作该功能！");
        }
        Long teacherId = requestLong("teacherId");
        //公私海场景，判断该用户是否有权限，若无权限，返回老师负责人员
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(user.getUserId(), teacherId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()) {
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))) {
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限", mapMessage.get("teacherManager")));
            } else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        return agentHiddenTeacherService.cancelHideTeacher(teacherId, getCurrentUser());
    }

    /**
     * 换班详情页
     */
    @RequestMapping(value = "clazz_alter.vpage", method = RequestMethod.GET)
    public String clazzAlterList(Model model) {
        Long schoolId = getRequestLong("schoolId");
        if (schoolId == 0L) {
            return errorInfoPage(SCHOOL_RESOURCE_INFO_ERROR, StringUtils.formatMessage("无效的学校ID:{}", schoolId), model);
        }
        model.addAttribute("schoolId", schoolId);
        List<ClazzAlterMapper> clazzAlterationBySchool = agentResourceService.getClazzAlterationBySchool(Collections.singleton(schoolId), 30, 0);

        // 未处理, 其中未处理的需要根据 CC_PROCESS_STATE 去提示
        List<ClazzAlterMapper> pendingList = clazzAlterationBySchool.stream().filter(alt -> Objects.equals(ClazzTeacherAlterationState.PENDING, alt.getState())).collect(Collectors.toList());
        model.addAttribute("pendingList", pendingList);
        // 已处理
        List<ClazzAlterMapper> successList = clazzAlterationBySchool.stream().filter(alt -> Objects.equals(ClazzTeacherAlterationState.SUCCESS, alt.getState()) || Objects.equals(ClazzTeacherAlterationState.REJECT, alt.getState())).collect(Collectors.toList());
        model.addAttribute("overList", successList);
      /*  // 已驳回
        List<ClazzAlterMapper> rejectList = clazzAlterationBySchool.stream().filter(alt -> Objects.equals(ClazzTeacherAlterationState.REJECT, alt.getState())).collect(Collectors.toList());
        model.addAttribute("rejectList", rejectList);*/
        return "rebuildViewDir/mobile/resource/clazz_alter";
    }

    /**
     * 同意换班申请
     */
    @RequestMapping(value = "approve_alter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveClazzAlter() {
        AuthCurrentUser user = getCurrentUser();
        if (user.isProductOperator()) { // 产品运营角色 只有查看资源部分数据的功能， 没有操作功能
            return MapMessage.errorMessage("您没有权限操作该功能！");
        }
        Long respondent = getRequestLong("respondent");
        Long recordId = getRequestLong("recordId");
        String type = getRequestString("alterType");
        ClazzTeacherAlterationType alterationType = ClazzTeacherAlterationType.valueOf(type);
        try {
            return agentResourceService.approveApplication(getCurrentUserId(), recordId, respondent, alterationType);
        } catch (Exception ex) {
            logger.error("Failed approve clazz alteration, id={}, tid={}, type={}", recordId, respondent, alterationType, ex);
            return MapMessage.errorMessage("同意换班请求失败:" + ex.getMessage());
        }
    }

    /**
     * 拒绝换班申请
     */
    @RequestMapping(value = "reject_alter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rejectClazzAlter() {
        AuthCurrentUser user = getCurrentUser();
        if (user.isProductOperator()) { // 产品运营角色 只有查看资源部分数据的功能， 没有操作功能
            return MapMessage.errorMessage("您没有权限操作该功能！");
        }
        Long respondent = getRequestLong("respondent");
        Long recordId = getRequestLong("recordId");
        String type = getRequestString("alterType");
        ClazzTeacherAlterationType alterationType = ClazzTeacherAlterationType.valueOf(type);
        try {
            return agentResourceService.rejectApplication(getCurrentUserId(), recordId, respondent, alterationType);
        } catch (Exception ex) {
            logger.error("Failed reject clazz alteration, id={}, tid={}, type={}", recordId, respondent, alterationType, ex);
            return MapMessage.errorMessage("拒绝换班请求失败:" + ex.getMessage());
        }
    }

    /**
     * 关注学校以及取消关注学校的功能
     */
    @RequestMapping(value = "update_follow.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage followOrUnfollowSchool() {
        AuthCurrentUser user = getCurrentUser();
        if (user.isProductOperator()) { // 产品运营角色 只有查看资源部分数据的功能， 没有操作功能
            return MapMessage.errorMessage("您没有权限操作该功能！");
        }
        Long schoolId = getRequestLong("schoolId");
        Boolean followed = getRequestBool("followed");
        Long userId = getCurrentUserId();
        if (schoolId == 0L) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return agentResourceService.followOrUnfollowSchool(userId, schoolId, followed);
        } catch (Exception ex) {
            logger.error("Failed process follow school, user={}, school={}, followed={}, ex={}", userId, schoolId, followed, ex.getMessage(), ex);
            return MapMessage.errorMessage("关注/取消关注更新失败, 原因:" + ex.getMessage());
        }
    }

    /**
     * 学生兑换奖品物流信息
     *
     * @return
     */
    @RequestMapping(value = "student_reward_logistics.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studentRewardLogisticsList() {
        MapMessage mapMessage = MapMessage.successMessage();
        Long schoolId = getRequestLong("schoolId");
        if (schoolId == 0L) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        mapMessage.put("rewardLogistics", schoolResourceService.getRewardLogisticsList(schoolId));
        return mapMessage;
    }


    /**
     * 学校动态
     *
     * @return
     */
    @RequestMapping(value = "school_dynamics.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolDynamics() {
        long schoolId = getRequestLong("schoolId");
        if (0L == schoolId) {
            return MapMessage.errorMessage("未找到schoolId");
        }
        return schoolResourceService.getSchoolDynamics(schoolId);
    }

//    /**
//     * 学校近6个月汇总数据
//     * @return
//     */
//    @RequestMapping(value = "schoolmonth.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage schoolMonth(){
//        long schoolId = getRequestLong("schoolId");
//        if (0L == schoolId) {
//            return MapMessage.errorMessage("未找到schoolId");
//        }
//        School school = schoolLoaderClient.getSchoolLoader()
//                .loadSchool(schoolId)
//                .getUninterruptibly();
//        if (school == null) {
//            return MapMessage.errorMessage("学校信息未找到");
//        }
//        Date todayEnd = DateUtils.getTodayEnd();
//        Date sixMonthBefore = DateUtils.addMonths(todayEnd,-5);
//        int  startMonth = SafeConverter.toInt(DateUtils.dateToString(sixMonthBefore,"yyyyMM"));
//        int  endMonth = SafeConverter.toInt(DateUtils.dateToString(todayEnd,"yyyyMM"));
//        return schoolResourceService.getSchoolMonthData(schoolId,SchoolLevel.safeParse(school.getLevel()),startMonth,endMonth);
//    }
//
//
//    /**
//     * 学校单科活跃数据
//     *
//     * @return
//     */
//    @RequestMapping(value = "school_subject_active.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage subjectiveActive() {
//        long schoolId = getRequestLong("schoolId");
//        if (0L == schoolId) {
//            return MapMessage.errorMessage("未找到schoolId");
//        }
//        School school = schoolLoaderClient.getSchoolLoader()
//                .loadSchool(schoolId)
//                .getUninterruptibly();
//        if (school == null) {
//            return MapMessage.errorMessage("学校信息未找到");
//        }
//        return MapMessage.successMessage().add("data", schoolResourceService.getSubjectiveActiveMap(schoolId));
//    }
//

//    /**
//     * 专员名下学校月活增长情况
//     *
//     * @param model
//     * @return
//     */
//    @RequestMapping(value = "school_mau_increase_detail.vpage", method = RequestMethod.GET)
//    public String schoolMauIncreaseDetail(Model model) {
//        Long id = getRequestLong("id");
//        String idType = getRequestString("idType");
//        if (id == 0L) {
//            id = getCurrentUserId();
//        }
//        if (StringUtils.isEmpty(idType)) {
//            idType = SchoolMauIncreaseStatistics.IdType.USER.name();
//        }
//        model.addAttribute("schoolMauIncreaseData", schoolMauIncreaseStatisticsService.getSchoolMauIncreaseData(id, idType));
//        return "rebuildViewDir/mobile/resource/school_mau_increase_detail";
//    }
//
//    /**
//     * 学校月活增长统计
//     *
//     * @param model
//     * @return
//     */
//    @RequestMapping(value = "school_mau_increase_statistics.vpage", method = RequestMethod.GET)
//    public String schoolMauIncreaseStatistics(Model model) {
//        AuthCurrentUser currentUser = getCurrentUser();
//        if (currentUser.isBusinessDeveloper()) {
//            return redirect("school_mau_increase_detail.vpage");
//        } else if (currentUser.isCityManager()) {
//            List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(getCurrentUserId());
//            if (CollectionUtils.isNotEmpty(groupIds)) {
//                Map<String, List<SchoolMauIncreaseStatistics>> groupCityCategoryMap = schoolMauIncreaseStatisticsService.generateGroupCityCategoryMap(groupIds.get(0));
//                model.addAttribute("RegionData", groupCityCategoryMap);
//                List<SchoolMauIncreaseStatistics> businessDeveloperData = schoolMauIncreaseStatisticsService.generateGroupUserCategoryMap(groupIds.get(0), AgentRoleType.BusinessDeveloper);
//                if (CollectionUtils.isNotEmpty(businessDeveloperData)) {
//                    model.addAttribute("BusinessDeveloperData", businessDeveloperData);
//                }
//            }
//        } else if (currentUser.isRegionManager() || currentUser.isCountryManager() || currentUser.isAreaManager()) {
//            long groupId = getRequestLong("groupId");
//            if (0L == groupId) {
//                List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(currentUser.getUserId());
//                if (CollectionUtils.isNotEmpty(groupUserByUser)) {
//                    groupId = groupUserByUser.get(0).getGroupId();
//                }
//            }
//            Map<String, List<SchoolMauIncreaseStatistics>> stringListMap = schoolMauIncreaseStatisticsService.generateGroupCategoryMap(groupId);
//            Map<String, List<SchoolMauIncreaseStatistics>> groupCityCategoryMap = schoolMauIncreaseStatisticsService.generateGroupCityCategoryMap(groupId);
//            stringListMap.putAll(groupCityCategoryMap);
//            List<SchoolMauIncreaseStatistics> businessDeveloperData = schoolMauIncreaseStatisticsService.generateGroupUserCategoryMap(groupId, AgentRoleType.BusinessDeveloper);
//            if (MapUtils.isNotEmpty(stringListMap)) {
//                model.addAttribute("RegionData", stringListMap);
//            }
//            if (CollectionUtils.isNotEmpty(businessDeveloperData)) {
//                model.addAttribute("BusinessDeveloperData", businessDeveloperData);
//            }
//        }
//        return "rebuildViewDir/mobile/resource/school_mau_increase_statistics";
//    }
//
//    /**
//     * 获取学校不活跃老师列表
//     *
//     * @param model
//     * @return
//     */
//    @RequestMapping(value = "un_active_teacher_list.vpage", method = RequestMethod.GET)
//    public String getUnActiveTeachers(Model model) {
//        Long schoolId = getRequestLong("schoolId");
//        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
//        if (school == null) {
//            return errorInfoPage(SCHOOL_RESOURCE_INFO_ERROR, StringUtils.formatMessage("无效的学校ID:{}", schoolId), model);
//        }
//        model.addAttribute("schoolId", schoolId);
//        model.addAttribute("unActiveTeachers", teacherResourceService.loadUnActiveTeachers(schoolId));
//        return "rebuildViewDir/mobile/resource/un_active_teacher_list";
//    }

    /**
     * 跳转学校详情页之前，操作权限控制
     *
     * @return
     */
    @RequestMapping(value = "school_detail_authority_message.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage schoolDetailAuthorityMessage() {
        Long userId = getRequestLong("userId");
        if (userId <= 0) {
            userId = getCurrentUserId();
        }
        Long schoolId = getRequestLong("schoolId");
        Integer scene = getRequestInt("scene", 1);
        MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(userId, schoolId, scene);
        if (!mapMessage.isSuccess()) {
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))) {
                return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限", mapMessage.get("schoolManager")));
            } else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        return MapMessage.successMessage();
    }

    // 学校基础信息接口
    @RequestMapping(value = "show_school_basic_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage showSchoolBasicData() {
        Long schoolId = getRequestLong("schoolId");
        SchoolBasicData schoolBasicData = schoolResourceService.generateSchoolBasicData(schoolId);
        if (schoolBasicData == null) {
            return MapMessage.errorMessage("查询学校信息时未找到学校信息");
        }

        SchoolBasicExtData schoolBasicExtData = schoolResourceService.generateSchoolBasicExtData(schoolId);
        SchoolPositionData schoolPositionData = schoolResourceService.generateSchoolPositionData(schoolId);
        List<SchoolGradeBasicData> gradeDataList = schoolResourceService.generateGradeBasicDataList(schoolId);
        //计算班级数、学生数
        int classNum = 0;

        if (CollectionUtils.isNotEmpty(gradeDataList)) {
            for (SchoolGradeBasicData schoolGradeBasicData : gradeDataList) {
                if (null != schoolGradeBasicData) {
                    classNum += schoolGradeBasicData.getClazzNum() != null ? schoolGradeBasicData.getClazzNum() : 0;
                }
            }
        }

        MapMessage msg = MapMessage.successMessage();
        msg.put("schoolBasicData", schoolBasicData);
        msg.put("schoolBasicExtData", schoolBasicExtData);
        msg.put("schoolPositionData", schoolPositionData);
        msg.put("gradeDataList", gradeDataList);
        //拼装学制
        msg.put("eduSystemTypes", schoolResourceService.appendEduSystemType(schoolBasicExtData.getEduSystem()));

        msg.put("classNum", classNum);
        return msg;
    }

    @RequestMapping(value = "get_sign_address.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getSignAddress(HttpServletRequest request) {
        String latitude = getRequestString("latitude");
        String longitude = getRequestString("longitude");
        String coordinateType;
        if (agentRequestSupport.isIOSRequest(request)) {
            coordinateType = "wgs84ll";
        } else {
            coordinateType = "autonavi";
        }

        if (StringUtils.isBlank(latitude) || StringUtils.isBlank(longitude) || StringUtils.isBlank(coordinateType)) {
            return MapMessage.errorMessage("坐标信息为空");
        }

        MapMessage address = AmapMapApi.getAddress(latitude, longitude, coordinateType);
        if (!address.isSuccess()) {
            return address;
        }

        MapMessage message = MapMessage.successMessage();
        message.put("coordinateType", "autonavi");
        message.put("latitude", ConversionUtils.toString(address.get("latitude")));
        message.put("longitude", ConversionUtils.toString(address.get("longitude")));
        message.put("address", ConversionUtils.toString(address.get("address")));
        return message;
    }

    // 更新学校基础信息
    @RequestMapping(value = "update_school_basic_data.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSchoolBasicData() {
        AuthCurrentUser user = getCurrentUser();
        Long schoolId = getRequestLong("schoolId");
        String url = getRequestString("url");               // 照片URL
        String coordinateType = getRequestString("coordinateType");               // GPS类型:wgs84 ,百度类型: bd09ll 高德类型:autonavi
        String latitude = getRequestString("latitude");                     // 地理坐标：纬度
        String longitude = getRequestString("longitude");                    // 地理坐标：经度
        EduSystemType eduSystemType = EduSystemType.of(getRequestString("eduSystem"));

        Integer englishStartGrade = getRequestInt("englishStartGrade");

        String gradeDataJson = getRequestString("gradeDataJson");

        Integer externOrBoarder = requestInteger("externOrBoarder");//走读方式

        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());

//        if(StringUtils.isBlank(url)
//                || StringUtils.isBlank(coordinateType)
//                || StringUtils.isBlank(latitude)
//                || StringUtils.isBlank(longitude)
//                || eduSystemType == null
//                || englishStartGrade < ClazzLevel.FIRST_GRADE.getLevel() && englishStartGrade > ClazzLevel.SIXTH_GRADE.getLevel()
//                ){
//            return MapMessage.errorMessage("信息有误，请重新填写");
//        }

        if (schoolLevel == SchoolLevel.JUNIOR && (englishStartGrade < ClazzLevel.FIRST_GRADE.getLevel() || englishStartGrade > ClazzLevel.SIXTH_GRADE.getLevel())) {
            return MapMessage.errorMessage("英语起始年级有误，请重新填写");
        }

        MapMessage msg;

        List<SchoolGradeBasicData> gradeDataList = JsonUtils.fromJsonToList(gradeDataJson, SchoolGradeBasicData.class);

        SchoolBasicExtData basicExtData = new SchoolBasicExtData();
        basicExtData.setSchoolId(schoolId);
        basicExtData.setEduSystem(eduSystemType);
        basicExtData.setEnglishStartGrade(englishStartGrade);
        basicExtData.setExternOrBoarder(externOrBoarder);
        msg = schoolResourceService.updateSchoolExtData(basicExtData);
        if (!msg.isSuccess()) {
            return msg;
        }

        msg = schoolResourceService.updateSchoolGradeData(schoolId, gradeDataList);
        if (!msg.isSuccess()) {
            return msg;
        }

        if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude) && StringUtils.isNotBlank(coordinateType)) {
            msg = AmapMapApi.getAddress(latitude, longitude, coordinateType);
            if (!msg.isSuccess()) {
                return msg;
            }

            // 采用转换后的高德坐标系
            coordinateType = "autonavi";
            latitude = ConversionUtils.toString(msg.get("latitude"));
            longitude = ConversionUtils.toString(msg.get("longitude"));
            String address = ConversionUtils.toString(msg.get("address"));

            SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
            if (extInfo == null || !Objects.equals(url, extInfo.getPhotoUrl())) {
                msg = schoolClueService.upsertSchoolClueBySchoolId(schoolId, latitude, longitude, user.getUserId(), user.getRealName(), user.getUserPhone(), url, coordinateType, address);
                if (!msg.isSuccess()) {
                    return msg;
                }
            }
        }
        return MapMessage.successMessage();
    }

    // 获取学校年级的基础数据
    @RequestMapping(value = "get_grade_basic_data_list.vpage")
    @ResponseBody
    public MapMessage fetchGradeBasicDataList() {
        Long schoolId = getRequestLong("schoolId");
        List<SchoolGradeBasicData> gradeDataList = schoolResourceService.generateGradeBasicDataList(schoolId);
        MapMessage msg = MapMessage.successMessage();
        msg.put("gradeDataList", gradeDataList);
        return msg;
    }

    /**
     * 学校等级
     *
     * @return
     */
    @RequestMapping(value = "school_popularity_type_list.vpage")
    @ResponseBody
    public MapMessage schoolPopularityTypeList() {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<AgentSchoolPopularityType> agentSchoolPopularityTypes = AgentSchoolPopularityType.viewSchoolPopularity();
        agentSchoolPopularityTypes.forEach(item -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("level", item.getLevel());
            dataMap.put("description", item.getDescribe());
            dataList.add(dataMap);
        });
        return MapMessage.successMessage().add("dataList", dataList);
    }


    /**
     * 学校标签列表
     * @return
     */
    @RequestMapping(value = "school_tag_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolTagList() {
         return MapMessage.successMessage().add("tagList",agentTagService.getTagListByType(AgentTagType.SCHOOL,true));
    }

    /**
     * 我的学校默认列表
     *
     * @return
     */
    @RequestMapping(value = "my_school_list.vpage")
    @ResponseBody
    public MapMessage mySchoolList(HttpServletRequest request) {
        Double longitude = getRequestDouble("longitude");   //经度
        Double latitude = getRequestDouble("latitude");     //纬度
        Integer sortType = getRequestInt("sortType");       //排序类型 1：按拜访时间由新到旧 2：按拜访时间由久到新 3：按距离由近到远 4：按距离由远到近

        longitude = longitude != 0D ? longitude : null;
        latitude = latitude != 0D ? latitude : null;

        if (longitude != null && latitude != null) {
            //坐标转化
            String coordinateType;
            if (agentRequestSupport.isIOSRequest(request)) {
                coordinateType = "wgs84ll";//getRequestString("coordinateType");
            } else {
                coordinateType = "autonavi";
            }
            MapMessage address = AmapMapApi.getAddress(ConversionUtils.toString(latitude), ConversionUtils.toString(longitude), coordinateType);
            if (!address.isSuccess()) {
                return address;
            }

            String latitudeStr = ConversionUtils.toString(address.get("latitude"));
            String longitudeStr = ConversionUtils.toString(address.get("longitude"));

            latitude = StringUtils.isNotBlank(latitudeStr) ? Double.parseDouble(latitudeStr) : null;
            longitude = StringUtils.isNotBlank(longitudeStr) ? Double.parseDouble(longitudeStr) : null;
        }

        AuthCurrentUser currentUser = getCurrentUser();
        Long userId = currentUser.getUserId();
        List<Long> managedSchoolIds = new ArrayList<>();
        try {
            // 专员显示自己管辖的所有学校
            if (currentUser.isBusinessDeveloper()) {
                managedSchoolIds = baseOrgService.getUserSchools(userId);

            } else {
                //获取用户部门
                List<AgentGroup> userGroups = baseOrgService.getUserGroups(currentUser.getUserId());
                AgentGroup agentGroup = userGroups.get(0);
                //获取部门及其子部门中，部门类型为“分区”的部门
                List<AgentGroup> cityGroupList = new ArrayList<>();
                if (agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City) {
                    cityGroupList.add(agentGroup);
                } else {
                    List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(agentGroup.getId()).stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
                    cityGroupList.addAll(subGroupList);
                }

                Set<Long> groupIds = cityGroupList.stream().filter(Objects::nonNull).map(AgentGroup::getId).collect(Collectors.toSet());
                //获取指定部门（多个）中，指定角色（专员）的用户，随机选择一个
                userId = baseOrgService.getUserByGroupIdsAndRole(groupIds, AgentRoleType.BusinessDeveloper).stream().findFirst().orElse(OTHER_ID);
                //如果市经理，并且选择“未分配”
                if (currentUser.isCityManager() && Objects.equals(userId, OTHER_ID)) {
                    //获取市经理负责的人员
                    List<AgentUser> managedUsers = baseOrgService.getManagedGroupUsers(currentUser.getUserId(), false);
                    // 先查出所有市经理负责的学校
                    managedSchoolIds = baseOrgService.getManagedSchoolList(getCurrentUserId());
                    // 市经理名下所有专员
                    Set<Long> arrangedSchools = new HashSet<>();
                    managedUsers.forEach(user -> arrangedSchools.addAll(baseOrgService.getManagedSchoolList(user.getId())));
                    managedSchoolIds.removeAll(arrangedSchools);
                } else {
                    managedSchoolIds = baseOrgService.getUserSchools(userId);
                }
            }
            return MapMessage.successMessage().add("dataList", schoolResourceService.generateMySchoolInfoByIds(null, managedSchoolIds, sortType, userId, longitude, latitude));
        } catch (Exception ex) {
            return MapMessage.errorMessage("查询我的学校失败");
        }
    }

    /**
     * 我的学校搜索
     *
     * @return
     */
    @RequestMapping(value = "my_school_search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mySchoolSearch(HttpServletRequest request) {
        Integer regionCode = getRequestInt("regionCode");   //地址
        Long selectedUserId = getRequestLong("userId");             //选择专员
        Integer sortType = getRequestInt("sortType");       //排序类型 1：按拜访时间由新到旧 2：按拜访时间由久到新 3：按距离由近到远 4：按距离由远到近

        Double longitude = getRequestDouble("longitude");   //经度
        Double latitude = getRequestDouble("latitude");     //纬度

        longitude = longitude != 0D ? longitude : null;
        latitude = latitude != 0D ? latitude : null;

        if (longitude != null && latitude != null) {
            //坐标转化
            String coordinateType;
            if (agentRequestSupport.isIOSRequest(request)) {
                coordinateType = "wgs84ll";//getRequestString("coordinateType");
            } else {
                coordinateType = "autonavi";
            }
            MapMessage address = AmapMapApi.getAddress(ConversionUtils.toString(latitude), ConversionUtils.toString(longitude), coordinateType);
            if (!address.isSuccess()) {
                return address;
            }

            String latitudeStr = ConversionUtils.toString(address.get("latitude"));
            String longitudeStr = ConversionUtils.toString(address.get("longitude"));

            latitude = StringUtils.isNotBlank(latitudeStr) ? Double.parseDouble(latitudeStr) : null;
            longitude = StringUtils.isNotBlank(longitudeStr) ? Double.parseDouble(longitudeStr) : null;
        }

        AuthCurrentUser currentUser = getCurrentUser();
        Long userId = currentUser.getUserId();
        try {
            Set<Integer> regionCodes = new HashSet<>();
            //根据地址过滤
            if (regionCode > 0) {
                regionCodes.add(regionCode);
                //地址“不限”
            } else {
                AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
                List<AgentGroupRegion> groupRegionList = baseOrgService.getGroupRegionByGroup(groupUser.getGroupId());
                if (CollectionUtils.isNotEmpty(groupRegionList)) {
                    regionCodes.addAll(groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(toList()));
                }
            }
            List<Long> managedSchoolIds = new ArrayList<>();
            if (currentUser.isBusinessDeveloper()) {
                managedSchoolIds = baseOrgService.getUserSchools(userId);
            } else {
                if (selectedUserId > 0) {
                    userId = selectedUserId;
                    //如果市经理，并且选择“未分配”
                    if (currentUser.isCityManager() && Objects.equals(userId, OTHER_ID)) {
                        //获取市经理负责的人员
                        List<AgentUser> managedUsers = baseOrgService.getManagedGroupUsers(currentUser.getUserId(), false);
                        // 先查出所有市经理负责的学校
                        managedSchoolIds = baseOrgService.getManagedSchoolList(getCurrentUserId());
                        // 市经理名下所有专员
                        Set<Long> arrangedSchools = new HashSet<>();
                        managedUsers.forEach(user -> arrangedSchools.addAll(baseOrgService.getManagedSchoolList(user.getId())));
                        managedSchoolIds.removeAll(arrangedSchools);
                    } else {
                        managedSchoolIds = baseOrgService.getUserSchools(userId);
                    }
                }
            }
            return MapMessage.successMessage().add("dataList", schoolResourceService.generateMySchoolInfoByIds(regionCodes, managedSchoolIds, sortType, userId, longitude, latitude));
        } catch (Exception ex) {
            return MapMessage.errorMessage("查询我的学校失败");
        }
    }


    /**
     * 新增高潜校信息
     *
     * @return
     */
    @RequestMapping(value = "high_potential_school_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage highPotentialSchoolList(HttpServletRequest request) {
        Long userId = getRequestLong("userId");     //选择专员
        int subject = getRequestInt("subject");     //学科
        Double longitude = getRequestDouble("longitude");   //经度
        Double latitude = getRequestDouble("latitude");     //纬度

        Integer topN = getRequestInt("topN", 20);      //显示数量
        Integer model = getRequestInt("model", 1);     //模式

        longitude = longitude != 0D ? longitude : null;
        latitude = latitude != 0D ? latitude : null;

        if (longitude != null && latitude != null) {
            //坐标转化
            String coordinateType;
            if (agentRequestSupport.isIOSRequest(request)) {
                coordinateType = "wgs84ll";//getRequestString("coordinateType");
            } else {
                coordinateType = "autonavi";
            }
            MapMessage address = AmapMapApi.getAddress(ConversionUtils.toString(latitude), ConversionUtils.toString(longitude), coordinateType);
            if (!address.isSuccess()) {
                return address;
            }

            String latitudeStr = ConversionUtils.toString(address.get("latitude"));
            String longitudeStr = ConversionUtils.toString(address.get("longitude"));

            latitude = StringUtils.isNotBlank(latitudeStr) ? Double.parseDouble(latitudeStr) : null;
            longitude = StringUtils.isNotBlank(longitudeStr) ? Double.parseDouble(longitudeStr) : null;
        }

        AuthCurrentUser currentUser = getCurrentUser();
        List<AgentHighPotentialSchoolInfo> highPotentialSchoolInfoList = schoolResourceService.highPotentialSchoolList(currentUser, userId, subject, longitude, latitude, topN, model);
        return MapMessage.successMessage().add("dataList", highPotentialSchoolInfoList);
    }


    /**
     * 获取月活TOP校部门负责区域地址信息
     *
     * @return
     */
    @RequestMapping(value = "mau_top_school_group_region.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroupRegion() {
        Integer type = getRequestInt("type", 1);//1：小学业务 2：中学业务
        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        Map<String, List<ExRegion>> regionMap = null;

        Collection<ExRegion> counties = baseOrgService.getGroupCountyRegion(getCurrentUserId(), roleType, type);
        if (CollectionUtils.isEmpty(counties)) {
            return MapMessage.errorMessage("该用户下无地区");
        }
        List<Map<String, Object>> regionTreeGroupByFLetter = baseOrgService.createRegionTreeGroupByFLetter(counties);
        return MapMessage.successMessage().add("dataList", regionTreeGroupByFLetter);

    }

    /**
     * 月活TOP校
     *
     * @return
     */
    @RequestMapping(value = "mau_top_school_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mauTopSchoolList(HttpServletRequest request) {
        Integer regionCode = getRequestInt("regionCode");   //地区编码
        Integer subject = getRequestInt("subject", 2);         //学科 1： 单科 2：语文 3：数学 4：英语
        Double longitude = getRequestDouble("longitude");   //经度
        Double latitude = getRequestDouble("latitude");     //纬度
        Integer type = getRequestInt("type", 1);        //业务类型 1：小学业务 2：中学业务
        Integer topN = getRequestInt("topN", 20);      //显示数量
        Integer model = getRequestInt("model", 1);     //模式

        longitude = longitude != 0D ? longitude : null;
        latitude = latitude != 0D ? latitude : null;

        if (longitude != null && latitude != null) {
            //坐标转化
            String coordinateType;
            if (agentRequestSupport.isIOSRequest(request)) {
                coordinateType = "wgs84ll";//getRequestString("coordinateType");
            } else {
                coordinateType = "autonavi";
            }
            MapMessage address = AmapMapApi.getAddress(ConversionUtils.toString(latitude), ConversionUtils.toString(longitude), coordinateType);
            if (!address.isSuccess()) {
                return address;
            }

            String latitudeStr = ConversionUtils.toString(address.get("latitude"));
            String longitudeStr = ConversionUtils.toString(address.get("longitude"));

            latitude = StringUtils.isNotBlank(latitudeStr) ? Double.parseDouble(latitudeStr) : null;
            longitude = StringUtils.isNotBlank(longitudeStr) ? Double.parseDouble(longitudeStr) : null;
        }

        AuthCurrentUser currentUser = getCurrentUser();
        List<AgentMauTopSchoolInfo> mauTopSchoolInfoList = schoolResourceService.mauTopSchoolList(currentUser, longitude, latitude, type, regionCode, subject, topN, model);
        return MapMessage.successMessage().add("dataList", mauTopSchoolInfoList);
    }

    // 潜力值老师页面  月活趋势 感觉只能用学校查了
    @RequestMapping(value = "school_month_data_view.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolMonthDataView() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        MapMessage msg = MapMessage.successMessage("成功");
        Date date = performanceService.lastSuccessDataDate();
        msg.add("lastUpdateTime", DateUtils.dateToString(date, "yyyy/MM/dd"));
        msg.add("dataList", schoolResourceService.generateSchoolChartInfoMonth(schoolId));
        return msg;
    }

    // 潜力值老师页面  日活趋势
    @RequestMapping(value = "school_day_data_view.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolDayDataView() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        Integer startDate = getRequestInt("startDate");
        String direction = getRequestString("direction");
        MapMessage msg = MapMessage.successMessage("成功");
        Date date = performanceService.lastSuccessDataDate();
        msg.add("lastUpdateTime", DateUtils.dateToString(date, "yyyy/MM/dd"));
        msg.add("dataList", schoolResourceService.generateSchoolChartInfoDay(schoolId, startDate, direction));
        return msg;
    }

    /**
     * 学校数据中心-作业
     *
     * @return
     */
    @RequestMapping(value = "school_data_center_work.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolDataCenterWork() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        return MapMessage.successMessage().add("dataMap", schoolResourceService.getSchoolOnlineIndicator(school));
    }

    /**
     * 学校老师数量统计
     *
     * @return
     */
    @RequestMapping(value = "school_teacher_statistics.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolTeacherStatistics() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        return MapMessage.successMessage().add("dataMap", teacherResourceService.schoolTeacherStatistics(schoolId));
    }


    /**
     * 月活top校排名
     *
     * @return
     */
    @RequestMapping(value = "mau_top_school_ranking.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage mauTopSchoolRanking() {
        Long schoolId = getRequestLong("schoolId");
        int type = getRequestInt("type", 0);
        int regionCode = getRequestInt("regionCode", 0);
        int topN = getRequestInt("topN", 20);
        int model = getRequestInt("model", 1);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("userRoleType", baseOrgService.getUserRole(getCurrentUserId()));
        mapMessage.add("dataList", schoolResourceService.mauTopSchoolRanking(schoolId, getCurrentUser(), type, regionCode, topN, model));
        return mapMessage;
    }

    /**
     * 待办任务
     *
     * @return
     */
    @RequestMapping(value = "todo_task_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage todoTaskList() {
        Long schoolId = getRequestLong("schoolId");
        return MapMessage.successMessage().add("dataMap", schoolResourceService.todoTaskList(schoolId));
    }

    /**
     * 学校数据中心-家长
     *
     * @return
     */
    @RequestMapping(value = "school_data_center_parent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolDataCenterParent() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        SchoolParentIndicatorData schoolParentIndicator = schoolResourceService.getSchoolParentIndicator(schoolId);
        return MapMessage.successMessage().add("dataMap", schoolParentIndicator);
    }

    @RequestMapping(value = "keyman/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage keymanList() {
        Long schoolId = getRequestLong("schoolId");

        List<AgentOuterResourceExtend> extendList = agentOuterResourceService.getOuterResourceExtendByOrganizationId(schoolId);
        List<Long> resourceIds = extendList.stream()
                .filter(extend -> Objects.nonNull(extend.getResourceId()))
                .map(AgentOuterResourceExtend::getResourceId)
                .collect(Collectors.toList());
        Map<Long,AgentOuterResource> outerResourceMap = agentOuterResourceService.getOuterResourceByIds(resourceIds);

        SchoolKeymanListResult result = SchoolKeymanListResult.Builder.build(outerResourceMap, extendList);

        MapMessage msg = MapMessage.successMessage("成功");
        msg.put("directorList", result.getDirectorList());
        msg.put("groupLeaderList", result.getGroupLeaderList());
        msg.put("schoolMasterList", result.getSchoolMasterList());
        msg.put("unregisteredTeacherList", result.getUnregisteredTeacherList());
        msg.put("otherList", result.getOtherList());
        return msg;
    }
}
