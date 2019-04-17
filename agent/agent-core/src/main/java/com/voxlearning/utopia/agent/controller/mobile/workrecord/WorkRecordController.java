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

package com.voxlearning.utopia.agent.controller.mobile.workrecord;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.SchoolRecordInfo;
import com.voxlearning.utopia.agent.bean.outerresource.AgentOuterResourceView;
import com.voxlearning.utopia.agent.bean.workrecord.WorkRecordData;
import com.voxlearning.utopia.agent.constants.AgentErrorCode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.memorandum.AgentMemorandum;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.memorandum.AgentMemorandumService;
import com.voxlearning.utopia.agent.service.mobile.AgentResearchersService;
import com.voxlearning.utopia.agent.service.mobile.SchoolClueService;
import com.voxlearning.utopia.agent.service.mobile.TeacherSummaryService;
import com.voxlearning.utopia.agent.service.mobile.resource.GradeResourceService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.service.mobile.v2.CrmVisitPlanService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.organization.AgentOuterResourceService;
import com.voxlearning.utopia.agent.service.schoollastworkrecord.AgentSchoolLastWorkRecordService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskCenterService;
import com.voxlearning.utopia.agent.service.user.OrgConfigService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.agent.utils.Pinyin4jUtils;
import com.voxlearning.utopia.api.constant.CrmSchoolClueStatus;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.agent.constants.AgentErrorCode.SCHOOL_RESOURCE_INFO_ERROR;

/**
 * @author Jia HuanYin
 * @since 2015/10/10
 */
@Controller
@RequestMapping("/mobile/work_record")
public class WorkRecordController extends AbstractAgentController {

    @Inject private RaikouSystem raikouSystem;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private WorkRecordService workRecordService;
    @Inject
    private TeacherSummaryService teacherSummaryService;
    @Inject
    private SchoolClueService schoolClueService;
    @Inject
    private OrgConfigService orgConfigService;
    @Inject
    private CrmVisitPlanService crmVisitPlanService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentNotifyService agentNotifyService;
    @Inject
    private AgentMemorandumService agentMemorandumService;
    @Inject
    private UserServiceClient userServiceClient;
    @Inject
    private SchoolResourceService schoolResourceService;
    @Inject
    private SearchService searchService;
    @Inject
    private AgentTaskCenterService agentTaskCenterService;
    @Inject
    private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;
    @Inject
    private AgentRequestSupport agentRequestSupport;
    @Inject
    private GradeResourceService gradeResourceService;
    @Inject
    private TeacherResourceService teacherResourceService;
    @Inject
    private AgentResearchersService agentResearchersService;
    @Inject
    private AgentOuterResourceService agentOuterResourceService;
    @Inject
    private AgentSchoolLastWorkRecordService agentSchoolLastWorkRecordService;


    @ImportService(interfaceClass = CrmSummaryService.class)
    private CrmSummaryService crmSummaryService;

    //--------------------------------------进校-----------------------------------------------------------------------
    //添加进校记录

    //测试页
    @RequestMapping(value = "add_intoSchool_record_test.vpage", method = RequestMethod.GET)
    public String addIntoSchoolRecodeTest(Model model) {
        return "rebuildViewDir/mobile/intoSchool/add_intoSchool_record_test";

    }

    @RequestMapping(value = "add_intoSchool_record.vpage", method = RequestMethod.GET)
    public String addIntoSchoolRecode(Model model) {

        Long schoolId = getRequestLong("schoolId");
        Long userId = getCurrentUserId();
        try {
            CrmWorkRecord crmWorkRecord = loadOrInitWorkRecord(CrmWorkRecordType.SCHOOL, userId, schoolId);

            // 记录时间
            model.addAttribute("workTime", new Date());

            // 学校信息
            model.addAttribute("schoolId", crmWorkRecord.getSchoolId());
            model.addAttribute("schoolName", crmWorkRecord.getSchoolName());
            model.addAttribute("schoolLevel", crmWorkRecord.getSchoolLevel());

            // 签到信息
            model.addAttribute("signType", crmWorkRecord.getSignType());
            model.addAttribute("photoUrl", crmWorkRecord.getSchoolPhotoUrl());

            // 拜访主题
            model.addAttribute("workTitle", crmWorkRecord.getWorkTitle());

            model.addAttribute("schoolMemorandumInfo", crmWorkRecord.getSchoolMemorandumInfo());
            model.addAttribute("visitTeacherList", crmWorkRecord.getVisitTeacherList());

//            // 代理人员信息
//            model.addAttribute("agencyId", crmWorkRecord.getAgencyId());
//            model.addAttribute("agencyName", crmWorkRecord.getAgencyName());
//            model.addAttribute("agencyList", orgConfigService.agencyListByUserId(userId));


            return "rebuildViewDir/mobile/intoSchool/add_intoSchool_record";
        } catch (Exception ex) {
            logger.error("visit add into school record is failed userId=" + userId, ex);
            return errorInfoPage(AgentErrorCode.INTO_SCHOOL_RECORD_ERROR, "添加进校记录页错误，请返回后重试", model);
        }
    }

    @RequestMapping(value = "add_intoSchool_record_new.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addIntoSchoolRecodeNew(Model model) {

//        Long schoolId = getRequestLong("schoolId");
        Long userId = getCurrentUserId();
        MapMessage mapMessage = MapMessage.successMessage();
        try {
            // 记录时间
            mapMessage.put("workTime", new Date());
            return mapMessage;
        } catch (Exception ex) {
            logger.error("visit add into school record is failed userId=" + userId, ex);
            return MapMessage.errorMessage("进校记录加载异常");
        }
    }

    // 获取或初始化工作记录
    private CrmWorkRecord loadOrInitWorkRecord(CrmWorkRecordType workRecordType, Long userId, Long schoolId) {
        CrmWorkRecord crmWorkRecord = new CrmWorkRecord();
        crmWorkRecord.setWorkType(workRecordType);
        // 通过学校详情页添加进校记录
        if (schoolId != null && schoolId > 0) {
            setSchoolData(crmWorkRecord, schoolId);
        }
        return crmWorkRecord;
    }


    // 设置学校数据
    private void setSchoolData(CrmWorkRecord crmWorkRecord, Long schoolId) {
        if (schoolId == null) {
            return;
        }
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null || (school.getSchoolAuthenticationState() != AuthenticationState.WAITING && school.getSchoolAuthenticationState() != AuthenticationState.SUCCESS)) {
            return;
        }
        crmWorkRecord.setSchoolId(schoolId);
        crmWorkRecord.setSchoolName(school.getCname());
        crmWorkRecord.setSchoolLevel(school.getLevel());
        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
        if (exRegion != null) {
            crmWorkRecord.setCountyCode(exRegion.getCountyCode());
            crmWorkRecord.setCountyName(exRegion.getCountyName());
            crmWorkRecord.setCityCode(exRegion.getCityCode());
            crmWorkRecord.setCityName(exRegion.getCityName());
            crmWorkRecord.setProvinceCode(exRegion.getProvinceCode());
            crmWorkRecord.setProvinceName(exRegion.getProvinceName());
        }
    }

//    /**
//     * 保存进校记录新
//     */
//    @RequestMapping(value = "saveSchoolRecordNew.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage saveSchoolRecordNew(HttpServletRequest request) {
//        Long userId = getCurrentUserId();
//
//        String longitude = getRequestString("longitude");
//        String latitude = getRequestString("latitude");
////        String url = getRequestString("url");
//        Integer visitSchoolType = getRequestInt("visitSchoolType");
//        String workTitle = getRequestString("workTitle");
//        Set<Long> teacherIdSet = requestLongSet("teacherIds");
//        //备注信息json串
//        String memorandumJson = getRequestString("schoolRecordJson");
//        String schoolPhotoUrl = getRequestString("schoolPhotoUrl");//照片签到时照片urls
//        Long schoolId = getRequestLong("schoolId");
//        if (schoolId == null || schoolId == 0L) {
//            return MapMessage.errorMessage("学校信息未提交，请重新选择学校");
//        }
//        CrmWorkRecord crmWorkRecord = loadOrInitWorkRecord(CrmWorkRecordType.SCHOOL, userId, schoolId);
//
////        if(!agentCacheSystem.loadSignInFlag(CrmWorkRecordType.SCHOOL.name(), userId, schoolId)) {
////            return MapMessage.errorMessage("签到失败或未签到请重新签到");
////        }
//
//        List<CrmWorkRecord> todayIntoSchool = workRecordService.getWorkerWorkRecords(userId, CrmWorkRecordType.SCHOOL, DateUtils.getTodayStart(), DateUtils.getTodayEnd());
//        todayIntoSchool = todayIntoSchool.stream().filter(p -> Objects.equals(p.getSchoolId(), schoolId) && (p.getVisitSchoolType() == null || p.getVisitSchoolType() == 2)).collect(Collectors.toList());
//        if (CollectionUtils.isNotEmpty(todayIntoSchool)) {
//            return MapMessage.errorMessage("您已经进过这所学校了，学校:" + crmWorkRecord.getSchoolName());
//        }
//
//
//        List<CrmTeacherVisitInfo> visitTeacherList = workRecordService.getVisitTeachers(teacherIdSet);
//        if (CollectionUtils.isEmpty(visitTeacherList)) {
//            return MapMessage.errorMessage("拜访老师为空请重新填写");
//        }
//        crmWorkRecord.setVisitTeacherList(visitTeacherList);
//        this.saveTeacherMemorandumList(crmWorkRecord, memorandumJson);
//
//        //责任区域场景，判断该用户是否有权限操作，若无权限，返回学校负责人员
//        MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(getCurrentUserId(), schoolId, SearchService.SCENE_SEA);
//        if (!mapMessage.isSuccess()) {
//            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))) {
//                return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限", mapMessage.get("schoolManager")));
//            } else {
//                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
//            }
//        }
//        //获取拜访老师ID列表（主账号）
//        List<Long> visitTeacherIdList = visitTeacherList.stream().map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toList());
//        //获取主副账号对应关系
//        Map<Long, List<Long>> mainSubTeacherIdMap = teacherLoaderClient.loadSubTeacherIds(visitTeacherIdList);
//        //定义拜访老师副账号ID列表
//        Set<Long> subVisitTeacherIdList = new HashSet<>();
//        //获取每个主账号对应的副账号
//        visitTeacherIdList.forEach(p -> {
//            if (mainSubTeacherIdMap.containsKey(p)) {
//                List<Long> subteacherIdList = mainSubTeacherIdMap.get(p);
//                subVisitTeacherIdList.addAll(subteacherIdList);
//            }
//        });
//        //获取副账号拜访老师信息
//        List<CrmTeacherVisitInfo> subVisitTeacherList = workRecordService.getVisitTeachers(subVisitTeacherIdList);
//        //将副账号拜访老师信息与主账号拜访老师信息拼接一块
//        crmWorkRecord.getVisitTeacherList().addAll(subVisitTeacherList);
//
//        AuthCurrentUser user = getCurrentUser();
//
//        if (StringUtils.isBlank(workTitle)) {
//            return MapMessage.errorMessage("拜访主题必填");
//        }
//        //判断是否要处理学校线索，签到类型为拍照
//
//        Integer signType = getRequestInt("signType");
////        if (signType == null) {
////            return MapMessage.errorMessage("签到失败或未签到请重新签到");
////        }
//        this.conversionCoordinate(request, longitude, latitude, crmWorkRecord);
//        if (signType == 2) {
//            //学校线索部分的修改
//            MapMessage msg = schoolClueService.upsertSchoolClueBySchoolId(schoolId, latitude, longitude, user.getUserId()
//                    , user.getRealName(), user.getUserPhone(), schoolPhotoUrl, crmWorkRecord.getCoordinateType(), crmWorkRecord.getAddress());
//            if (!msg.isSuccess()) {
//                return msg;
//            }
//            crmWorkRecord.setSchoolPhotoUrl(schoolPhotoUrl);
//        }
//        crmWorkRecord.setSignType(signType);
//        crmWorkRecord.setWorkerId(user.getUserId());
//        crmWorkRecord.setWorkerName(user.getRealName());
//        crmWorkRecord.setWorkTime(new Date());
//        crmWorkRecord.setWorkTitle(workTitle);
//        crmWorkRecord.setWorkType(CrmWorkRecordType.SCHOOL);
//        crmWorkRecord.setDisabled(false);
//        crmWorkRecord.setVisitSchoolType(visitSchoolType);
//
//        // 判断该进校人30天内进校次数
//        Date startDate = DayRange.current().getStartDate();
//        List<CrmWorkRecord> workerWorkRecords = workRecordService.getWorkerWorkRecords(crmWorkRecord.getWorkerId(), crmWorkRecord.getSchoolId(), CrmWorkRecordType.SCHOOL, DateUtils.calculateDateDay(startDate, -30), new Date());
//        crmWorkRecord.setVisitCountLte30(workerWorkRecords.size() + 1);
//
//        Boolean sendNotify = false;
//        //获取学校信息
//        School school = schoolLoaderClient.getSchoolLoader().loadSchool(crmWorkRecord.getSchoolId()).getUninterruptibly();
//        if (null != school) {
//            SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
//            //学校阶段为“初中、高中”时，30天以内第6次及以上拜访才发送消息
//            if (schoolLevel == SchoolLevel.MIDDLE || schoolLevel == SchoolLevel.HIGH) {
//                if (crmWorkRecord.getVisitCountLte30() >= 6) {
//                    sendNotify = true;
//                }
//                //学校阶段为“小学、学前”的，保持不变
//            } else if (schoolLevel == SchoolLevel.JUNIOR || schoolLevel == SchoolLevel.INFANT) {
//                if (crmWorkRecord.getVisitCountLte30() >= 3) {
//                    sendNotify = true;
//                }
//            }
//        }
//        //发送预警信息
//        if (sendNotify) {
//            AgentUser cityManager = findGroupCityManager(user.getUserId());
//            if (cityManager != null) {
//                agentNotifyService.sendNotify(AgentNotifyType.INTO_SCHOOL_WARNING.getType(), "学校连续拜访",
//                        StringUtils.formatMessage("{}近30天内被{}专员第{}次拜访，请您及时关注学校情况", crmWorkRecord.getSchoolName(), crmWorkRecord.getWorkerName(), crmWorkRecord.getVisitCountLte30()), Collections.singletonList(cityManager.getId()), null);
//            }
//        }
//        MapMessage msg = workRecordService.saveCrmWorkRecord(crmWorkRecord);
////        //添加进校记录
////        String coordinateType;
////        if (agentRequestSupport.isIOSRequest(request)) {
////            coordinateType = "wgs84ll";
////        } else {
////            coordinateType = "autonavi";
////        }
////        workRecordService.addIntoSchoolWorkRecord(coordinateType, longitude, latitude, workTitle, memorandumJson, schoolPhotoUrl, schoolId,signType,user.getUserId(),user.getRealName());
//        if (msg.isSuccess()) {
//            saveTeacherMemorandum(crmWorkRecord.getVisitTeacherList(), schoolId, SafeConverter.toString(msg.get("id")));
//            saveSchoolMemorandum(schoolId, crmWorkRecord.getSchoolMemorandumInfo(), SafeConverter.toString(msg.get("id")));
//            // 删除缓存数据
//            deleteCrmWorkRecordCache(CrmWorkRecordType.SCHOOL, userId);
//
//            workTitle = AgentSchoolWorkTitleType.of(workTitle) == null ? "" : AgentSchoolWorkTitleType.of(workTitle).getWorkTitle();
//            createUserServiceRecord(crmWorkRecord.getVisitTeacherList(), user, workTitle, visitTeacherList, ConversionUtils.toString(msg.get("id")));
//
//            //任务中心，设置进校维护老师
//            Set<Long> teacherIds = crmWorkRecord.getVisitTeacherList().stream().filter(item -> null != item).map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toSet());
//            AlpsThreadPool.getInstance().submit(() -> agentTaskCenterService.setSubTaskIntoSchoolAndVisitTeacherForWorkRecord(userId, schoolId, teacherIds));
//        }
//        return msg.add("appraisalSchool", true);
//    }

    public void conversionCoordinate(HttpServletRequest request, String longitude, String latitude, CrmWorkRecord crmWorkRecord) {
        String coordinateType;
        if (agentRequestSupport.isIOSRequest(request)) {
            coordinateType = "wgs84ll";//getRequestString("coordinateType");
        } else {
            coordinateType = "autonavi";
        }
        MapMessage address = AmapMapApi.getAddress(latitude, longitude, coordinateType);
        if (address.isSuccess()) {
            crmWorkRecord.setAddress(ConversionUtils.toString(address.get("address")));
            crmWorkRecord.setLatitude(ConversionUtils.toString(address.get("latitude")));
            crmWorkRecord.setLongitude(ConversionUtils.toString(address.get("longitude")));
            crmWorkRecord.setCoordinateType(coordinateType);
        }
    }


    /**
     * 添加老师的userServiceRecord
     */
    private void createUserServiceRecord(List<CrmTeacherVisitInfo> teacherInfos, AuthCurrentUser user, String workTitle, List<CrmTeacherVisitInfo> visitTeacherList, String workRecordId) {
        if (CollectionUtils.isEmpty(teacherInfos) || user == null || user.getUserId() == null) {
            return;
        }
        Set<Long> teacherIds = teacherInfos.stream().map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toSet());
        List<CrmTeacherSummary> teacherList = teacherSummaryService.load(teacherIds, user.getUserId());
        if (CollectionUtils.isEmpty(teacherList)) {
            return;
        }
        teacherList.forEach(p -> {
            UserServiceRecord userRecord = new UserServiceRecord();
            userRecord.setUserId(p.getTeacherId());
            userRecord.setUserName(p.getRealName());
            userRecord.setOperatorId(ConversionUtils.toString(user.getUserId()));
            userRecord.setOperatorName(user.getRealName());
            userRecord.setOperationType(UserServiceRecordOperationType.市场活动.name());
            userRecord.setOperationContent(String.format("进校,%s", workTitle));
            userRecord.setComments(String.format("市场%s进校拜访,%s", user.getRealName(), workRecordService.getWorkContent(visitTeacherList, p.getTeacherId())));
            userRecord.setAdditions("CrmWorkRecord:" + workRecordId);
            userServiceClient.saveUserServiceRecord(userRecord);
        });
    }


    /**
     * 签到
     */
    @RequestMapping(value = "signIn.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage signIn(HttpServletRequest request) {

        String targetId = getRequestString("targetId"); //目标ID
        Integer targetIdType = getRequestInt("idType", 1);         //  1: schoolId,   2: CrmWorkRecordId

        String longitude = getRequestString("longitude");
        String latitude = getRequestString("latitude");
        Integer distance = getRequestInt("distance", 2000);  // 默认2000米

        String coordinateType;
        if (agentRequestSupport.isIOSRequest(request)) {
            coordinateType = "wgs84ll";//getRequestString("coordinateType");
        } else {
            coordinateType = "autonavi";
        }

        List<Map<String, String>> locationList = new ArrayList<>();
        if (targetIdType == 1) {
            Long schoolId = SafeConverter.toLong(targetId);
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(schoolId)
                    .getUninterruptibly();
            if (school == null) {
                return MapMessage.errorMessage("学校不存在，学校ID为" + schoolId + ",请重新选择学校");
            }

            // 获取学校的坐标位置
            locationList = getSchoolLocationList(schoolId);
        } else if (targetIdType == 2) {
            Map<String, String> locationMap = workRecordService.loadWorkRecordLocation(targetId);
            if (MapUtils.isNotEmpty(locationMap)) {
                locationList.add(locationMap);
            }
        }

        if (CollectionUtils.isEmpty(locationList)) {
            return MapMessage.errorMessage().add("noLocation", true);
        }


        // 判断距离是否在2000米内
        boolean withinDistance = judgeDistance(locationList, longitude, latitude, coordinateType, distance);
        if (!withinDistance) {
            return MapMessage.errorMessage().add("farAway", true);
        }

        MapMessage address = AmapMapApi.getAddress(latitude, longitude, coordinateType);
        if (!address.isSuccess()) {
            return address;
        }

        // 进校的签到
        MapMessage message = MapMessage.successMessage();
        message.put("coordinateType", "autonavi");
        message.put("latitude", ConversionUtils.toString(address.get("latitude")));
        message.put("longitude", ConversionUtils.toString(address.get("longitude")));
        message.put("address", ConversionUtils.toString(address.get("address")));
        return message;

    }

    // 获取学校的坐标位置（从schoolExtInfo及学校位置申请记录中获取）
    private List<Map<String, String>> getSchoolLocationList(Long schoolId) {
        List<Map<String, String>> locationList = new ArrayList<>();
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo != null && StringUtils.isNotBlank(schoolExtInfo.getLatitude()) && StringUtils.isNotBlank(schoolExtInfo.getLongitude())) {
            Map<String, String> info = new HashMap<>();
            info.put("latitude", schoolExtInfo.getLatitude());
            info.put("longitude", schoolExtInfo.getLongitude());
            info.put("coordinateType", schoolExtInfo.getCoordinateType());
            locationList.add(info);
        }

        // 获取待审核的审核信息（审核通过的会更新到schoolExtInfo中，所以只需获取待审核的数据就可以了）
        List<CrmSchoolClue> schoolClues = schoolClueService.loads(schoolId).stream()
                .filter(p -> p.getAuthenticateType() != null && p.getAuthenticateType() == 5 && p.getStatus() != null && p.getStatus() == CrmSchoolClueStatus.待审核.getCode())
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(schoolClues)) {
            schoolClues.forEach(p -> {
                Map<String, String> info = new HashMap<>();
                info.put("latitude", p.getLatitude());
                info.put("longitude", p.getLongitude());
                info.put("coordinateType", p.getCoordinateType());
                locationList.add(info);
            });
        }
        return locationList;
    }

    // 判断指定的位置和多个目标位置的距离是否在指定的距离之内
    private boolean judgeDistance(List<Map<String, String>> targetLocationList, String longitude, String latitude, String coordinateType, long distance) {
        if (CollectionUtils.isEmpty(targetLocationList) || StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude) || StringUtils.isBlank(coordinateType)) {
            return false;
        }

        boolean result = false;
        for (Map<String, String> info : targetLocationList) {
            MapMessage msg = AmapMapApi.GetDistance(longitude, latitude, coordinateType, info.get("longitude"), info.get("latitude"), info.get("coordinateType"));
            if (!msg.isSuccess()) {
                continue;
            }

            if (msg.get("res") != null && ConversionUtils.toLong(msg.get("res")) < distance) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 选择教师页
     */
    @RequestMapping(value = "searchTeacherListPage.vpage", method = RequestMethod.GET)
    public String searchTeacherListPage(Model model) {
        Long userId = getCurrentUserId();
        Long schoolId = getRequestLong("schoolId");
        String backUrl = requestString("backUrl", "");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return errorInfoPage(SCHOOL_RESOURCE_INFO_ERROR, StringUtils.formatMessage("无效的学校ID:{}", schoolId), model);
        }
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        CrmWorkRecord crmWorkRecord = loadOrInitWorkRecord(CrmWorkRecordType.SCHOOL, userId, schoolId);
        try {
            List<CrmTeacherVisitInfo> teacherList = crmWorkRecord.getVisitTeacherList();
            List<Long> teacherIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(teacherList)) {
                teacherIds = teacherList.stream().map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toList());
            }
            MapMessage msg = workRecordService.getSchoolTeachers(schoolId, teacherIds);
            if (!msg.isSuccess()) {
                return errorInfoPage(AgentErrorCode.INTO_SCHOOL_RECORD_ERROR, msg.getInfo(), model);
            }
            List<WorkRecordService.TeacherData> english = (List<WorkRecordService.TeacherData>) msg.get("english");
            List<WorkRecordService.TeacherData> math = (List<WorkRecordService.TeacherData>) msg.get("math");
            List<WorkRecordService.TeacherData> chinese = (List<WorkRecordService.TeacherData>) msg.get("chinese");
            List<WorkRecordService.TeacherData> otherSubject = (List<WorkRecordService.TeacherData>) msg.get("otherSubject");

            List<WorkRecordService.TeacherData> other = new ArrayList<>();
            for (Long masterId : WorkRecordService.SCHOOL_MASTER_INFO.keySet()) {
                WorkRecordService.TeacherData master = workRecordService.getTeacherData();
                master.setTeacherId(masterId);
                master.setTeacherName(WorkRecordService.SCHOOL_MASTER_INFO.get(masterId));
                master.setChecked(teacherIds.contains(masterId));
                other.add(master);
            }

            model.addAttribute("english", english);
            model.addAttribute("math", math);
            model.addAttribute("backUrl", backUrl);
            model.addAttribute("chinese", chinese);
            model.addAttribute("other", other);
            model.addAttribute("schoolLevel", schoolLevel);
            if (schoolLevel.equals(SchoolLevel.MIDDLE) || schoolLevel.equals(SchoolLevel.HIGH)) {
                model.addAttribute("otherSubject", otherSubject);
            }
        } catch (Exception ex) {
            logger.error("schoolId get teacher is failed schoolId =" + schoolId, ex);
            return errorInfoPage(AgentErrorCode.INTO_SCHOOL_RECORD_ERROR, String.format("添加进校记录页搜索老师信息失败，学校ID为%d", schoolId), model);
        }
//        return "mobile/work_record/chooseTeacher";
        return "rebuildViewDir/mobile/intoSchool/chooseTeacher";
    }

    /**
     * 选择教师页新
     */
    @RequestMapping(value = "searchTeacherListPageNew.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchTeacherListPageNew(Model model) {
        MapMessage mapMessage = MapMessage.successMessage();
        Long userId = getCurrentUserId();
        Long schoolId = getRequestLong("schoolId");
        String backUrl = requestString("backUrl", "");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("无效的学校", schoolId);
        }
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        CrmWorkRecord crmWorkRecord = loadOrInitWorkRecord(CrmWorkRecordType.SCHOOL, userId, schoolId);
        try {
            List<CrmTeacherVisitInfo> teacherList = crmWorkRecord.getVisitTeacherList();
            List<Long> teacherIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(teacherList)) {
                teacherIds = teacherList.stream().map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toList());
            }
            MapMessage msg = workRecordService.getSchoolTeachers(schoolId, teacherIds);
            if (!msg.isSuccess()) {
                return MapMessage.errorMessage(AgentErrorCode.INTO_SCHOOL_RECORD_ERROR.getDesc(), msg.getInfo());
            }
            List<WorkRecordService.TeacherData> english = (List<WorkRecordService.TeacherData>) msg.get("english");
            List<WorkRecordService.TeacherData> math = (List<WorkRecordService.TeacherData>) msg.get("math");
            List<WorkRecordService.TeacherData> chinese = (List<WorkRecordService.TeacherData>) msg.get("chinese");
            List<WorkRecordService.TeacherData> otherSubject = (List<WorkRecordService.TeacherData>) msg.get("otherSubject");

            List<WorkRecordService.TeacherData> other = new ArrayList<>();
            for (Long masterId : WorkRecordService.SCHOOL_MASTER_INFO.keySet()) {
                WorkRecordService.TeacherData master = workRecordService.getTeacherData();
                master.setTeacherId(masterId);
                master.setTeacherName(WorkRecordService.SCHOOL_MASTER_INFO.get(masterId));
                master.setChecked(teacherIds.contains(masterId));
                other.add(master);
            }
            Map<String, Object> map = new HashMap();
            map.put("todayEnglish", this.todayRegisteredTeachers(english));
            map.put("english", this.assemblyTeacherMessage(english));
            map.put("todayMath", this.todayRegisteredTeachers(math));
            map.put("math", this.assemblyTeacherMessage(math));
            map.put("backUrl", backUrl);
            map.put("todayChinese", this.todayRegisteredTeachers(chinese));
            map.put("chinese", this.assemblyTeacherMessage(chinese));
            map.put("todayOther", Collections.emptyList());
            map.put("other", this.assemblyTeacherMessage(other));
            map.put("schoolLevel", schoolLevel);
            if (schoolLevel.equals(SchoolLevel.MIDDLE) || schoolLevel.equals(SchoolLevel.HIGH)) {
                map.put("otherSubject", assemblyOtherTeacherMessage(otherSubject));
            }
            mapMessage.put("data", map);
        } catch (Exception ex) {
            logger.error("schoolId get teacher is failed schoolId =" + schoolId, ex);
            return MapMessage.errorMessage("添加进校记录页搜索老师信息失败，学校ID为" + schoolId);
        }
        return mapMessage;
    }

    //拼装老师列表成前端要的样子
    public List<Map<String, Object>> assemblyTeacherMessage(List<WorkRecordService.TeacherData> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        //老师名称空格的给个默认排到最后一组
        Map<String, List<WorkRecordService.TeacherData>> map = list.stream().collect(Collectors.groupingBy(p -> Pinyin4jUtils.getFirstCapital(StringUtils.isBlank(p.getTeacherName()) ? "Z" : p.getTeacherName())));
        map.forEach((k, v) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("letter", k);
            item.put("teachers", v);
            result.add(item);
        });
        return result;
    }

    //其他学科老师按学科分组
    public List<Map<String, Object>> assemblyOtherTeacherMessage(List<WorkRecordService.TeacherData> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        //老师名称空格的给个默认排到最后一组
        Map<Subject, List<WorkRecordService.TeacherData>> map = list.stream().collect(Collectors.groupingBy(p -> p.getSubject()));
        map.forEach((k, v) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("subjectName", Objects.equals(k.getValue(), "未知") ? "未知科目" : k.getValue());
            item.put("teachers", v);
            item.put("teacherNum", v.size());
            result.add(item);
        });
        List<Map<String, Object>> sortedList = result.stream().sorted(((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("teacherNum")), SafeConverter.toInt(o1.get("teacherNum"))))).collect(Collectors.toList());
        return sortedList;
    }

    //当天注册老师
    public List<WorkRecordService.TeacherData> todayRegisteredTeachers(List<WorkRecordService.TeacherData> allTeachers) {
        Date dateStart = DayRange.current().getStartDate();
        if (CollectionUtils.isNotEmpty(allTeachers)) {
            return allTeachers.stream().filter(p -> p.getCreateTime().after(dateStart)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 保存老师列表
     */
    @RequestMapping(value = "saveTeacherList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTeacherList() {
        Set<Long> teacherIdSet = requestLongSet("teacherIds");
        try {
            CrmWorkRecord crmWorkRecord = loadOrInitWorkRecord(CrmWorkRecordType.SCHOOL, getCurrentUserId(), null);
            List<CrmTeacherVisitInfo> teacherList = workRecordService.getVisitTeachers(teacherIdSet);

            Map<Long, CrmTeacherVisitInfo> teacherMap = teacherList.stream().collect(Collectors.toMap(CrmTeacherVisitInfo::getTeacherId, Function.identity()));
            if (CollectionUtils.isNotEmpty(crmWorkRecord.getVisitTeacherList())) {
                crmWorkRecord.getVisitTeacherList().forEach(p -> {
                    if (teacherMap.containsKey(p.getTeacherId())) {
                        teacherMap.get(p.getTeacherId()).setVisitInfo(p.getVisitInfo());
                    }
                });
            }
            crmWorkRecord.setVisitTeacherList(teacherList);
            saveCrmWorkRecordToCache(CrmWorkRecordType.SCHOOL, getCurrentUserId(), crmWorkRecord);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("save teacher list if failed teacherIds=" + StringUtils.join(teacherIdSet, ","), ex);
            return MapMessage.errorMessage("老师选择错误");
        }
    }

    //提交进校信息时拼接老师备忘信息（备注为空的老师信息也要传过来）
    public void saveTeacherMemorandumList(CrmWorkRecord crmWorkRecord, String memorandumJson) {
        try {
            Map<String, Object> schoolRecord = JsonUtils.fromJson(memorandumJson);
            if (schoolRecord == null) {
                return;
            }
            //前端约定的结构
            Object teacherObj = schoolRecord.get("visitTeacher");
            if (teacherObj != null) {
                List<Map<String, Object>> visitTeacher = (List<Map<String, Object>>) teacherObj;
                crmWorkRecord.getVisitTeacherList().forEach(t -> {
                    visitTeacher.forEach(v -> {
                        if (t.getTeacherId() == SafeConverter.toLong(v.get("teacherId"))) {
                            t.setVisitInfo(v.get("visitInfoStr").toString());
                        }
                    });
                });
            }
            String schoolMemorandum = SafeConverter.toString(schoolRecord.get("schoolMemorandum"));
            crmWorkRecord.setSchoolMemorandumInfo(schoolMemorandum);
        } catch (Exception ex) {
            logger.error("解析进校备忘信息异常=" + memorandumJson, ex);
            return;
        }
    }

    /**
     * 进校记录的学校保存
     */
    @RequestMapping(value = "saveSchoolRecordSchool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSchoolRecordSchool() {
        Long schoolId = getRequestLong("schoolId");
        try {
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(schoolId)
                    .getUninterruptibly();
            if (school == null) {
                return MapMessage.errorMessage("学校不存在或已被叛假");
            }

            if (!searchService.hasSchoolPermission(getCurrentUserId(), schoolId, SearchService.SCENE_SEA)) {
                return MapMessage.errorMessage("ID:" + schoolId + "学校不在用户的权限范围内");
            }
            loadOrInitWorkRecord(CrmWorkRecordType.SCHOOL, getCurrentUserId(), schoolId);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("save school record school is failed schoolId=" + schoolId, ex);
            return MapMessage.errorMessage("");
        }
    }

    /**
     * 查看进校记录
     */
    @RequestMapping(value = "showSchoolRecordNew.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage showSchoolRecordNew() {
        String recordId = getRequestString("recordId");
        MapMessage mapMessage = MapMessage.successMessage();
        Map<String, Object> map = new HashMap<>();
        try {
            WorkRecordData workRecordData = workRecordService.getWorkRecordDataByIdAndType(recordId, AgentWorkRecordType.SCHOOL);
            if (workRecordData == null) {
                return MapMessage.errorMessage("进校记录不存在！");
            }
            map.put("recordId", recordId);
            map.put("schoolId", workRecordData.getSchoolId());
            map.put("schoolName", workRecordData.getSchoolName());
            map.put("address", workRecordData.getAddress());
            map.put("workTitle", workRecordData.getWorkTitle());

            addVisitSubject(workRecordData);
            List<Map<String, Object>> teacherList = new ArrayList<>();
            List<WorkRecordVisitUserInfo> visitUserInfoList = workRecordData.getVisitUserInfoList();
            if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
                visitUserInfoList.forEach(t -> {
                    Map<String, Object> teacherMap = new HashMap<>();
                    teacherMap.put("teacherId", t.getId());
                    teacherMap.put("teacherName", t.getName());
                    teacherMap.put("visitInfo", t.getResult() != null ? t.getResult() : "");
                    teacherMap.put("subject", t.getSubject() == null ? "" : t.getSubject().getValue());
                    teacherMap.put("job", t.getJob());
                    teacherList.add(teacherMap);
                });
            }
            map.put("visitTeacherList", teacherList);
            map.put("workTime", workRecordData.getWorkTime());
            map.put("visitSchoolType", workRecordData.getVisitSchoolType());
            //进校组会类型有下边内容
            map.put("meetingTime", workRecordData.getMeetingTime());
            map.put("meetingForm", workRecordData.getMeetingForm());
            map.put("lecturerName", workRecordData.getLecturerName());
            map.put("workContent", workRecordData.getResult());
            List<String> photoUrls = workRecordData.getPhotoUrls();
            if (CollectionUtils.isNotEmpty(photoUrls)) {
                map.put("photoUrl", photoUrls.get(0));//现场照片
            }
            //陪访记录
            map.put("accompanyRecordList", workRecordService.getAccompanyRecordsByBusinessRecordId(recordId));
            if (Objects.equals(getCurrentUserId(), workRecordData.getUserId())) {
                map.put("isYour", true);
            }
            mapMessage.put("data", map);
            return mapMessage;
        } catch (Exception ex) {
            logger.error("show school record is faield recordId=" + recordId, ex);
            return MapMessage.errorMessage(AgentErrorCode.INTO_SCHOOL_RECORD_ERROR.getDesc());

        }
    }

    /**
     * 组会记录
     */
    @RequestMapping(value = "meeting_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage meetingRecord() {
        String recordId = getRequestString("recordId");
        CrmWorkRecord workInfo = workRecordService.getWorkInfo(recordId);
        return MapMessage.successMessage().add("data", workInfo);
    }

    /**
     * 查看组会记录
     */
    @RequestMapping(value = "showMeetingRecordNew.vpage", method = RequestMethod.GET)
    @ResponseBody()
    public MapMessage showMeetingRecordNew() {
        MapMessage mapMessage = MapMessage.successMessage();
        String recordId = getRequestString("recordId");
        WorkRecordData workRecordData = workRecordService.getWorkRecordDataByIdAndType(recordId, AgentWorkRecordType.MEETING);
        mapMessage.put("meetingRecord", workRecordService.generateWorkRecordMeetingDetail(workRecordData));
        mapMessage.put("accompanyRecordList", workRecordService.getAccompanyRecordsByBusinessRecordId(recordId));
        return mapMessage;
    }

    /**
     * 查看参与组会记录
     */
    @RequestMapping(value = "showJoinMeetingRecord.vpage", method = RequestMethod.GET)
    public String showJoinMeetingRecord(Model model) {
        String recordId = getRequestString("recordId");
        CrmWorkRecord workInfo = workRecordService.getWorkInfo(recordId);
        if (workInfo != null) {
            //组会
            model.addAttribute("meetingRecord", workRecordService.getWorkInfo(workInfo.getSchoolWorkRecordId()));
        }
        //参与组会
        model.addAttribute("joinMeetingRecord", workInfo);
        return "rebuildViewDir/mobile/intoSchool/join_meeting_record";
    }

    /**
     * 修改备忘信息页
     */
    @RequestMapping(value = "modificationSchoolRecord.vpage", method = RequestMethod.GET)
    public String modificationSchoolRecord(Model model) {
        String schoolRecordId = getRequestString("schoolRecordId");
        CrmWorkRecord workInfo;
        String editOrNewPage = "rebuildViewDir/mobile/intoSchool/edit_visit_record";

        if (StringUtils.isNotBlank(schoolRecordId)) {
            workInfo = workRecordService.getWorkInfo(schoolRecordId);
            if (workInfo == null) {
                return editOrNewPage;
            }
            model.addAttribute("schoolRecordId", schoolRecordId);
        } else {
            workInfo = loadOrInitWorkRecord(CrmWorkRecordType.SCHOOL, getCurrentUserId(), null);
        }
        addVisitMemorandum(workInfo, model);
        return editOrNewPage;
    }

    private void addVisitMemorandum(CrmWorkRecord crmWorkRecord, Model model) {
        if (crmWorkRecord == null) {
            return;
        }
        if (StringUtils.isNoneBlank(crmWorkRecord.getId())) {
            List<CrmTeacherVisitInfo> visitTeacherList = crmWorkRecord.getVisitTeacherList();
            List<AgentMemorandum> agentMemorandums = agentMemorandumService.loadMemorandumByIntoSchoolRecordId(crmWorkRecord.getId());
            AgentMemorandum sMemorandum = agentMemorandums.stream().filter(p -> p.getTeacherId() == null).findFirst().orElse(null);
            Map<Long, AgentMemorandum> agentMemorandumMap = agentMemorandums.stream().filter(p -> p.getTeacherId() != null).collect(Collectors.toMap(AgentMemorandum::getTeacherId, Function.identity(), (o1, o2) -> o1));
            if (CollectionUtils.isNotEmpty(visitTeacherList)) {
                Set<Long> teacherIds = visitTeacherList.stream().filter(CrmTeacherVisitInfo::isRealTeacher).map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toSet());
                Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
                visitTeacherList.forEach(p -> {
                    if (agentMemorandumMap.containsKey(p.getTeacherId())) {
                        if (agentMemorandumMap.get(p.getTeacherId()) != null) {
                            p.setVisitInfo(agentMemorandumMap.get(p.getTeacherId()).getContent());
                        }
                    } else {
                        p.setVisitInfo("");
                    }
                    if (teacherMap.containsKey(p.getTeacherId())) {
                        Subject subject = teacherMap.get(p.getTeacherId()).getSubject();
                        ;
                        p.setSubject(subject);
                    }
                });
            } else {
                visitTeacherList = new ArrayList<>();
            }
            model.addAttribute("schoolMemorandumInfo", sMemorandum == null ? "" : sMemorandum.getContent());
            model.addAttribute("visitTeacherList", visitTeacherList);
            Map<String, List<CrmTeacherVisitInfo>> subjectMap = new LinkedHashMap<>();
            subjectMap.put("ENGLISH", new ArrayList<>());
            subjectMap.put("MATH", new ArrayList<>());
            subjectMap.put("OTHER", new ArrayList<>());
            for (CrmTeacherVisitInfo crmTeacherVisitInfo : visitTeacherList) {
                if (Objects.equals(crmTeacherVisitInfo.getSubject(), Subject.ENGLISH)) {
                    List<CrmTeacherVisitInfo> english = subjectMap.get("ENGLISH");
                    english.add(crmTeacherVisitInfo);
                    subjectMap.put("ENGLISH", english);
                } else if (Objects.equals(crmTeacherVisitInfo.getSubject(), Subject.MATH)) {
                    List<CrmTeacherVisitInfo> math = subjectMap.get("MATH");
                    math.add(crmTeacherVisitInfo);
                    subjectMap.put("MATH", math);
                } else {
                    List<CrmTeacherVisitInfo> other = subjectMap.get("OTHER");
                    other.add(crmTeacherVisitInfo);
                    subjectMap.put("OTHER", other);
                }
            }
            model.addAttribute("subjectVisitTeacherMap", subjectMap);

        } else {
            model.addAttribute("schoolMemorandumInfo", crmWorkRecord.getSchoolMemorandumInfo());
            model.addAttribute("visitTeacherList", crmWorkRecord.getVisitTeacherList());
        }
    }

    //兼容旧数据把老师学科填上  CrmTeacherVisitInfo Subject是18年317号才加上的 之前的数据没学科
    private void addVisitSubject(WorkRecordData crmWorkRecord) {
        if (crmWorkRecord == null) {
            return;
        }
        if (StringUtils.isNoneBlank(crmWorkRecord.getId())) {
            List<WorkRecordVisitUserInfo> visitTeacherList = crmWorkRecord.getVisitUserInfoList().stream().filter(p -> Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(visitTeacherList)) {
                Set<Long> teacherIds = visitTeacherList.stream().map(WorkRecordVisitUserInfo::getId).collect(Collectors.toSet());
                Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
                visitTeacherList.forEach(p -> {
                    if (teacherMap.containsKey(p.getId())) {
                        Subject subject = teacherMap.get(p.getId()).getSubject();
                        p.setSubject(subject);
                    }
                });
            }
        }
    }

    /**
     * 进校记录
     */
    @RequestMapping(value = "schoolRecordListPage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolRecordListPage() {
        MapMessage mapMessage = MapMessage.successMessage();
        Long schoolId = getRequestLong("schoolId");
        try {
            List<WorkRecordData> schoolRecordList = workRecordService.getSchoolWorkRecords(schoolId);
            List<SchoolRecordInfo> res = getSchoolRecordInfo(schoolRecordList);
            if (CollectionUtils.isNotEmpty(res)) {
                res = res.stream().sorted((o1, o2) ->
                        o2.getWorkTime().compareTo(o1.getWorkTime())).collect(Collectors.toList());
            }
            mapMessage.put("schoolRecordList", res);
//            return "/mobile/work_record/schoolRecordList";
            return mapMessage;
        } catch (Exception ex) {
            logger.error("get school record list is failed", ex);
            return MapMessage.errorMessage(String.format("进校记录页，在查找该学校的进校记录时发生错误，学校ID为%d", schoolId));

        }
    }

    /**
     * 封装进校记录
     */
    private List<SchoolRecordInfo> getSchoolRecordInfo(List<WorkRecordData> schoolRecordList) {
        if (CollectionUtils.isEmpty(schoolRecordList)) {
            return Collections.emptyList();
        }
        List<SchoolRecordInfo> res = new ArrayList<>();
        schoolRecordList.forEach(p -> {
            if (p == null) {
                return;
            }
            SchoolRecordInfo info = new SchoolRecordInfo();
            info.setSchoolRecordId(p.getId());
            info.setWorkTime(p.getWorkTime());
            info.setWorkerId(p.getUserId());
            info.setWorkerName(p.getUserName());
            info.setWorkTitle(p.getWorkTitle());
            info.setInstructorCount(p.getVisitUserInfoList() != null ? p.getVisitUserInfoList().size() : 0);
            res.add(info);
        });
        return res;
    }

    private void saveCrmWorkRecordToCache(CrmWorkRecordType workRecordType, Long userId, CrmWorkRecord crmWorkRecord) {
        if (workRecordType == null || userId == null || crmWorkRecord == null) {
            return;
        }
        String key = "WORKRECORD_" + workRecordType.name() + "_" + userId;
        // 删除原有的缓存记录
        agentCacheSystem.CBS.flushable.delete(key);
        agentCacheSystem.CBS.flushable.set(key, DateUtils.getCurrentToDayEndSecond(), crmWorkRecord);
    }

    private void deleteCrmWorkRecordCache(CrmWorkRecordType workRecordType, Long userId) {
        if (workRecordType == null || userId == null) {
            return;
        }
        String key = "WORKRECORD_" + workRecordType.name() + "_" + userId;
        agentCacheSystem.CBS.flushable.delete(key);
    }

    //--------------------------------------进校-----------------------------------------------------------------------

    //--------------------------------------组会-----------------------------------------------------------------------
    //添加组会
    @RequestMapping(value = "addGroupMeeting.vpage", method = RequestMethod.GET)
    public String addGroupMeeting(Model model) {
        return "rebuildViewDir/mobile/intoSchool/add_group_meeting";
    }

    // 组会选择区域
    @RequestMapping(value = "load_region_page.vpage", method = RequestMethod.GET)
    public String loadRegionPage(Model model) {
        Integer selectedRegion = getRequestInt("regionCode");
        Long schoolId = getRequestLong("schoolId");
        String type = getRequestString("type");
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("type", type);
        model.addAttribute("regionCode", selectedRegion);
        return "rebuildViewDir/mobile/intoSchool/add_meeting_region";
    }

    // 加载区域内容
    @RequestMapping(value = "load_region_detail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadCityRegion() {
        Integer selectedRegion = getRequestInt("regionCode");
        MapMessage msg = getUserGroupIds();
        if (!msg.isSuccess()) {
            return msg;
        }
        Map<Integer, ExRegion> regionMap = raikouSystem.getRegionBuffer().loadRegions((Collection<Integer>) msg.get("regionCodes"));
        ExRegion region = raikouSystem.loadRegion(selectedRegion);
        List<Integer> selectedRegionCode = new ArrayList<>();
        if (region != null) {
            selectedRegionCode.add(region.getProvinceCode());
            selectedRegionCode.add(region.getCityCode());
            selectedRegionCode.add(region.getCountyCode());
        }
        return baseOrgService.createRegionTree(regionMap.values().stream().collect(Collectors.toList()), 3, selectedRegionCode);
    }

    private MapMessage getUserGroupIds() {
        List<AgentGroup> groups = baseOrgService.getUserGroups(getCurrentUserId());
        if (CollectionUtils.isEmpty(groups)) {
            return MapMessage.errorMessage("该用户不在任何部门下，请找管理员配置。");
        }
        Long groupId = groups.get(0).getId();
        List<AgentGroupRegion> groupRegions = baseOrgService.getGroupRegionByGroup(groupId);
        List<Integer> regionCodes = groupRegions.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toList());
        List<Integer> allCountyCodes = agentRegionService.getCountyCodes(regionCodes);
        return MapMessage.successMessage().add("regionCodes", new HashSet<>(allCountyCodes));
    }

//    /**
//     * 保存组会信息
//     */
//    @RequestMapping(value = "saveGroupMeetingRecord.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage saveGroupMeetingRecord(HttpServletRequest request) {
//        AuthCurrentUser user = getCurrentUser();
//        if (user == null || user.getUserId() == null) {
//            return MapMessage.errorMessage("用户数据异常");
//        }
//        String meetingTitle = getRequestString("meetingTitle");
//
////        Integer meetingRegion = getRequestInt("meetingRegion");
//        Integer meetingType = getRequestInt("meetingType");
//        CrmMeetingType iType = meetingType == 0 ? null : meetingType == 1 ? CrmMeetingType.PROVINCE_LEVEL : meetingType == 2 ? CrmMeetingType.CITY_LEVEL : meetingType == 3 ? CrmMeetingType.COUNTY_LEVEL : meetingType == 4 ? CrmMeetingType.SCHOOL_LEVEL : null;
//
//        Integer visitSchoolType = getRequestInt("visitSchoolType");
//        Set<Long> teacherIdSet = requestLongSet("teacherIds");
//        Integer meeterCount = getRequestInt("meeterCount");
//        Long schoolId = getRequestLong("schoolId");
//        String schoolName = getRequestString("schoolName");
//        //教研员ID
//        String instructorName = getRequestString("instructorName");
//        //教研员名字
//        Long instructorId = getRequestLong("instructorId");
////        String instructorMobile = getRequestString("instructorMobile");
//        String meetingContent = getRequestString("meetingContent");
//        String lecturer = getRequestString("lecturer");
//
//        //Integer isAgencyClue = getRequestInt("isAgencyClue"); // 是否是代理提供的线索 1.是 2否
////        Long agencyId = getRequestLong("agencyId");
//        Integer meetingLong = getRequestInt("meetingLong");   // 会议时长 1.小于15，2 15-60 分钟  3 大于1小时
//        Integer showFrom = getRequestInt("showFrom");         // 类型 1.专场，2 插播
//        Integer instructorAttend = getRequestInt("instructorAttend");//教研员是否出席 1.是 2.否
//        String scenePhotoUrl = getRequestString("scenePhotoUrl"); // 现场照片
//        String schoolPhotoUrl = getRequestString("schoolPhotoUrl");//照片签到时照片urls
//        Integer signType = getRequestInt("signType");
//        String longitude = getRequestString("longitude");
//        String latitude = getRequestString("latitude");
//
//        String coordinateType;
//        if (agentRequestSupport.isIOSRequest(request)) {
//            coordinateType = "wgs84ll";
//        } else {
//            coordinateType = "autonavi";
//        }
//        if (StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude) || StringUtils.isBlank(coordinateType)) {
//            return MapMessage.errorMessage("坐标信息为空");
//        }
//        if (signType == 2 && StringUtils.isBlank(schoolPhotoUrl)) {
//            return MapMessage.errorMessage("请上传照片信息");
//        }
//        try {
//
//
//            CrmWorkRecord crmWorkRecord = new CrmWorkRecord();
//            crmWorkRecord.setWorkerId(user.getUserId());
//            crmWorkRecord.setWorkerName(user.getRealName());
//
//            if (meetingLong == 0) {
//                return MapMessage.errorMessage("宣讲时长不能为空");
//            }
//            if (showFrom == 0) {
//                return MapMessage.errorMessage("类型不能为空");
//            }
//
//            if (StringUtils.isBlank(scenePhotoUrl)) {
//                return MapMessage.errorMessage("现场照片不能为空");
//            }
//
//
//            MapMessage addressMsg = AmapMapApi.getAddress(latitude, longitude, coordinateType);
//            if (!addressMsg.isSuccess()) {
//                return addressMsg;
//            }
//
//            crmWorkRecord.setAddress(ConversionUtils.toString(addressMsg.get("address")));
//            crmWorkRecord.setCoordinateType("autonavi");
//            crmWorkRecord.setLatitude(ConversionUtils.toString(addressMsg.get("latitude")));
//            crmWorkRecord.setLongitude(ConversionUtils.toString(addressMsg.get("longitude")));
//            crmWorkRecord.setSchoolPhotoUrl(schoolPhotoUrl);
//            crmWorkRecord.setWorkTime(new Date());
//
//            crmWorkRecord.setMeetingTime(meetingLong);
//            crmWorkRecord.setShowFrom(showFrom);
//            crmWorkRecord.setInstructorAttend(instructorAttend == 1);
//            crmWorkRecord.setScenePhotoUrl(scenePhotoUrl);
//            if (visitSchoolType == 1) {//2是校级组会暂时定义成进校   校级会议进校一天不能超过两次
//                List<CrmWorkRecord> todayIntoSchool = workRecordService.getWorkerWorkRecords(user.getUserId(), CrmWorkRecordType.SCHOOL, DateUtils.getTodayStart(), DateUtils.getTodayEnd());
//                todayIntoSchool = todayIntoSchool.stream().filter(p -> Objects.equals(p.getSchoolId(), schoolId) && p.getVisitSchoolType() == 1).collect(Collectors.toList());
//                if (CollectionUtils.isNotEmpty(todayIntoSchool) && todayIntoSchool.size() >= 2) {
//                    return MapMessage.errorMessage("校级会议进校一天不能超过两次");
//                }
//                crmWorkRecord.setWorkType(CrmWorkRecordType.SCHOOL);
//            } else {
//                crmWorkRecord.setWorkType(CrmWorkRecordType.MEETING);
//            }
//            if (iType == null) {
//                return MapMessage.errorMessage("会议类型错误，会议类型为必填");
//            }
//            if (iType == CrmMeetingType.SCHOOL_LEVEL) {
//                List<CrmTeacherVisitInfo> teacherList = workRecordService.getVisitTeachers(teacherIdSet);
//
//                if (CollectionUtils.isEmpty(teacherList)) {
//                    return MapMessage.errorMessage("老师选择错误");
//                }
//                crmWorkRecord.setVisitTeacherList(teacherList);
//                if (schoolId <= 0) {
//                    return MapMessage.errorMessage("学校ID为空");
//                }
//                if (schoolName == null) {
//                    return MapMessage.errorMessage("学校名称为空");
//                }
//                //责任区域场景，判断该用户是否有权限操作，若无权限，返回学校负责人员
//                MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(getCurrentUserId(), schoolId, SearchService.SCENE_SEA);
//                if (!mapMessage.isSuccess()) {
//                    if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))) {
//                        return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限", mapMessage.get("schoolManager")));
//                    } else {
//                        return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
//                    }
//                }
//                meetingTitle = schoolName;
//                crmWorkRecord.setSchoolId(schoolId);
//                crmWorkRecord.setSchoolName(schoolName);
//            } else {
//                if (StringUtils.isBlank(meetingTitle)) {
//                    return MapMessage.errorMessage("会议主题不能为空");
//                }
//                if (meetingTitle.length() > 10) {
//                    return MapMessage.errorMessage("会议主题过长");
//                }
//                if (instructorAttend == 0) {
//                    return MapMessage.errorMessage("教研员是否出席为必填");
//                }
//                if (StringUtils.isBlank(instructorName)) {
//                    return MapMessage.errorMessage("关键人姓名不能为空");
//                }
//            }
//            crmWorkRecord.setWorkTitle(meetingTitle);
//            crmWorkRecord.setMeetingType(iType);
//            if (meeterCount <= 0) {
//                return MapMessage.errorMessage("会议人数必须大于0");
//            }
//
////            if (!MobileRule.isMobile(instructorMobile)) {
////                return MapMessage.errorMessage("关键人电话格式错误");
////            }
//            crmWorkRecord.setMeeteeCount(meeterCount);
//            crmWorkRecord.setVisitSchoolType(visitSchoolType);//进校方式（普通进校、组会进校）
//            crmWorkRecord.setMeetingNote(lecturer);//讲师
//            crmWorkRecord.setInstructorName(instructorName);
//            crmWorkRecord.setResearchersId(instructorId);
//            crmWorkRecord.setResearchersName(instructorName);
////            crmWorkRecord.setInstructorMobile(instructorMobile);
//            crmWorkRecord.setWorkContent(meetingContent);
//            crmWorkRecord.setDisabled(false);
//            crmWorkRecord.setSignType(signType);
//            return workRecordService.saveCrmWorkRecord(crmWorkRecord);
//        } catch (Exception ex) {
//            return MapMessage.errorMessage("保存组会信息失败");
//        }
//    }


    //是否是代理地区
    @RequestMapping(value = "judgeRegionIsAgent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage judgeRegionIsAgent() {
        Integer region = getRequestInt("regionCode");
        if (region == 0) {
            return MapMessage.errorMessage("请选择组会区域");
        }
        //return MapMessage.successMessage().add("isAgencyRegion", orgConfigService.isAgencyRegion(region));
        return MapMessage.errorMessage();
    }

    //--------------------------------------组会-----------------------------------------------------------------------
    //添加陪访
    @RequestMapping(value = "addMeeting.vpage", method = RequestMethod.GET)
    public String addMeeting(Model model) {

        return "rebuildViewDir/mobile/intoSchool/addMeeting";
    }

    //参与陪访
    @RequestMapping(value = "joinMeeting.vpage", method = RequestMethod.GET)
    public String joinMeeting(Model model) {

        return "rebuildViewDir/mobile/intoSchool/joinMeeting";
    }
    //--------------------------------------陪访-----------------------------------------------------------------------


//    /**
//     * 保存陪同结果
//     *
//     * @param request
//     * @return
//     */
//    @RequestMapping(value = "save_accompany_visit_record.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage saveAccompanyVisitRecord(HttpServletRequest request) {
//        AuthCurrentUser user = getCurrentUser();
//        String workRecordId = getRequestString("workRecordId");                 //拜访记录ID
//        Integer signType = getRequestInt("signType");
//        String imgUrl = getRequestString("imgUrl");
//        String longitude = getRequestString("longitude");
//        String latitude = getRequestString("latitude");
//        String coordinateType;
//        if (agentRequestSupport.isIOSRequest(request)) {
//            coordinateType = "wgs84ll";
//        } else {
//            coordinateType = "autonavi";
//        }
//        String accompanyPurpose = getRequestString("accompanyPurpose");                 //陪同目的
//        String accompanySuggest = getRequestString("accompanySuggest");                 //陪同建议
//
//        Integer preparationScore = getRequestInt("preparationScore");
//        Integer productProficiencyScore = getRequestInt("productProficiencyScore");
//        Integer resultMeetExpectedResultScore = getRequestInt("resultMeetExpectedResultScore");
//
//        if (StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude) || StringUtils.isBlank(coordinateType)) {
//            return MapMessage.errorMessage("坐标信息为空");
//        }
//
//        if (StringUtils.isBlank(accompanyPurpose)) {
//            return MapMessage.errorMessage("请填写陪访目的");
//        }
//
//        MapMessage addressMapMessage = AmapMapApi.getAddress(latitude, longitude, coordinateType);
//        if (!addressMapMessage.isSuccess()) {
//            return addressMapMessage;
//        }
//
//        CrmWorkRecord workRecord = workRecordService.getWorkInfo(workRecordId);
//        if (workRecord == null) {
//            return MapMessage.errorMessage("拜访记录不存在");
//        }
//
//        CrmWorkRecord record = new CrmWorkRecord();
//        record.setWorkType(CrmWorkRecordType.VISIT);
//        record.setWorkTime(new Date());
//        record.setInterviewerId(workRecord.getWorkerId());
//        record.setInterviewerName(workRecord.getWorkerName());
//        record.setWorkerId(user.getUserId());
//        record.setWorkerName(user.getRealName());
//        record.setWorkTitle(accompanyPurpose);
//        record.setPartnerSuggest(accompanySuggest);
//        record.setSchoolWorkRecordId(workRecordId);
//        record.setSchoolId(workRecord.getSchoolId());
//        record.setSchoolName(workRecord.getSchoolName());
//
//        record.setImgUrl(imgUrl);
//
//        record.setAddress(ConversionUtils.toString(addressMapMessage.get("address")));
//        record.setCoordinateType("autonavi");
//        record.setLatitude(ConversionUtils.toString(addressMapMessage.get("latitude")));
//        record.setLongitude(ConversionUtils.toString(addressMapMessage.get("longitude")));
//        record.setSignType(signType);
//
//        record.setCountyName(workRecord.getCountyName());
//        record.setCountyCode(workRecord.getCountyCode());
//        record.setCityCode(workRecord.getCityCode());
//        record.setCityName(workRecord.getCityName());
//        record.setProvinceName(workRecord.getProvinceName());
//        record.setProvinceCode(workRecord.getProvinceCode());
//        // 评分
//        record.setPreparationScore(preparationScore);
//        record.setProductProficiencyScore(productProficiencyScore);
//        record.setResultMeetExpectedResultScore(resultMeetExpectedResultScore);
//        record.setDisabled(false);
//        try {
//            return AtomicCallbackBuilderFactory.getInstance()
//                    .<MapMessage>newBuilder()
//                    .keyPrefix("WorkRecord:saveAccompanyVisitRecord")
//                    .keys("workType:" + record.getWorkType().name() + ",workerId:" + record.getWorkerId() + ",schoolWorkRecordId:" + record.getSchoolWorkRecordId())
//                    .callback(() -> {
//                        return doAddVisitWorkRecord(record, user, signType);
//                    }).build()
//                    .execute();
//        } catch (CannotAcquireLockException e) {
//            return MapMessage.errorMessage("正在处理，请勿重复操作");
//        }
//    }

//    /**
//     * 添加陪访到库
//     *
//     * @param record
//     * @param user
//     * @param signType
//     * @return
//     */
//    private MapMessage doAddVisitWorkRecord(CrmWorkRecord record, AuthCurrentUser user, Integer signType) {
//        List<CrmWorkRecord> crmVisitWorkRecords = workRecordService.getVisitRecordsByIntoRecordId(record.getSchoolWorkRecordId());
//        if (CollectionUtils.isNotEmpty(crmVisitWorkRecords) && crmVisitWorkRecords.stream().anyMatch(item -> Objects.equals(item.getWorkerId(), record.getWorkerId()))) {
//            return MapMessage.errorMessage("您已经填写过陪同，请不要重复填写");
//        }
//        MapMessage retMsg = workRecordService.saveCrmWorkRecord(record);
//        if (retMsg.isSuccess()) {
//            CrmWorkRecord workRecord = workRecordService.getWorkInfo(record.getSchoolWorkRecordId());
//            //是否是上级对下级，陪同填写后要给被陪访人发送系统消息提醒
//            AgentGroupUser agentGroupUser = baseOrgService.getGroupUserByUser(record.getWorkerId()).stream().findFirst().orElse(null);
//            boolean groupManager = baseOrgService.isGroupManager(record.getWorkerId(), agentGroupUser.getGroupId());
//            if (groupManager) {
//                String visitFlagStr = "";
//                String accompanyTheme = "";
//                //进校
//                if (workRecord.getWorkType() == CrmWorkRecordType.SCHOOL) {
//                    visitFlagStr = "拜访";
//                    //学校名称
//                    accompanyTheme = workRecord.getSchoolName();
//                }
//                //组会
//                if (workRecord.getWorkType() == CrmWorkRecordType.MEETING) {
//                    visitFlagStr = "组织";
//                    //组会主题
//                    accompanyTheme = workRecord.getWorkTitle();
//                }
//                //拜访教研员
//                if (workRecord.getWorkType() == CrmWorkRecordType.TEACHING) {
//                    //拜访教研员主题
//                    accompanyTheme = workRecordService.getVisitResearcherTitle(workRecord);
//                    visitFlagStr = "拜访";
//                }
//
//                String messageContent = StringUtils.formatMessage("{}对和您的共同{}的{}创建了反馈建议", record.getWorkerName(), visitFlagStr, accompanyTheme);
//                agentNotifyService.sendNotify(
//                        AgentNotifyType.VISIT_FEEDBACK.getType(),
//                        "陪同反馈",
//                        messageContent,
//                        Collections.singleton(record.getInterviewerId()),
//                        StringUtils.formatMessage("/mobile/work_record/accompany_visit_record_detail.vpage?workRecordId={}", retMsg.get("id"))
//                );
//            }
//
//            // 照片签到,并且是陪同进校
//            if (signType == 2 && workRecord.getWorkType() == CrmWorkRecordType.SCHOOL) {
//                //学校线索部分的修改
//                schoolClueService.upsertSchoolClueBySchoolId(record.getSchoolId(), record.getLatitude(), record.getLongitude(), user.getUserId()
//                        , user.getRealName(), user.getUserPhone(), record.getSchoolPhotoUrl(), record.getCoordinateType(), record.getAddress());
//            }
//        }
//        return retMsg;
//    }

    //--------------------------------------陪访-----------------------------------------------------------------------

    // --------------------------这些代码其他地方还在引用--------------------------------------------------------------
    //组会的地区选择
    @RequestMapping(value = "regions_list.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage regionList() {
        AuthCurrentUser currentUser = getCurrentUser();
        return MapMessage.successMessage().add("regionTree", agentRegionService.buildUserRegionMapTree(currentUser));
    }

    /**
     * 根据工作记录id标识 查询工作记录详情
     *
     * @pram workRecordId
     */
    @RequestMapping(value = "record_details.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String recordDetails(Model model) {
        String workRecordId = getRequestString("workRecordId");//搜索工作记录详情id
        if (StringUtils.isBlank(workRecordId)) {
            logger.error("find record details page is failed userId=" + workRecordId);// 此处为校验数据失败
            return errorInfoPage(AgentErrorCode.VISIT_RECORD_ERROR, String.format("查询工作记录详情时失败，该用户的ID为%s", workRecordId), model);
        }
        Map<String, Object> stringObjectMap = workRecordService.workRecordDetailsByWorkRecordId(workRecordId);
        addVisitMemorandum((CrmWorkRecord) stringObjectMap.get("crmWorkRecord"), model);
        model.addAttribute("recordDetails", stringObjectMap);
        return "rebuildViewDir/mobile/workRecord/workrecordinfo";
    }

    /**
     * 查找未填写陪访的下属的当天的进校记录
     */
    @RequestMapping(value = "search_into_school.vpage", method = RequestMethod.GET)
    public String searchIntoSchool() {
        return "rebuildViewDir/mobile/workRecord/search_into_school";
    }


    /**
     * 获取当前用户可以填写参与组会的记录
     *
     * @return
     */
    @RequestMapping(value = "get_can_join_meeting_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public List<CrmWorkRecord> getCanJoinMeetingRecordData() {
        List<CrmWorkRecord> resultList = workRecordService.getCanJoinMeetingRecords(getCurrentUser());
        return resultList;
    }

    //------------------------------------------进校--------------------------------------------------------------------

    /**
     * 进校选择老师
     *
     * @return
     */
    @RequestMapping(value = "into_school_search_teacher_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage intoSchoolSearchTeacherList() {
        MapMessage mapMessage = MapMessage.successMessage();
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("无效的学校", schoolId);
        }
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        try {
            List<Long> checkedTeacherIds = new ArrayList<>();
            //获取老师信息
            MapMessage msg = workRecordService.getSchoolTeachers(schoolId, checkedTeacherIds);
            if (!msg.isSuccess()) {
                return MapMessage.errorMessage(AgentErrorCode.INTO_SCHOOL_RECORD_ERROR.getDesc(), msg.getInfo());
            }
            List<WorkRecordService.TeacherData> englishTeaList = new ArrayList<>();
            if (msg.get("english") != null) {
                englishTeaList.addAll((List<WorkRecordService.TeacherData>) msg.get("english"));
            }
            List<WorkRecordService.TeacherData> mathTeaList = new ArrayList<>();
            if (msg.get("math") != null) {
                mathTeaList.addAll((List<WorkRecordService.TeacherData>) msg.get("math"));
            }
            List<WorkRecordService.TeacherData> chineseTeaList = new ArrayList<>();
            if (msg.get("chinese") != null) {
                chineseTeaList.addAll((List<WorkRecordService.TeacherData>) msg.get("chinese"));
            }

            List<WorkRecordService.TeacherData> otherSubjectTeaList = new ArrayList<>();
            if (schoolLevel.equals(SchoolLevel.MIDDLE) || schoolLevel.equals(SchoolLevel.HIGH)) {
                List<WorkRecordService.TeacherData> otherSubject = (List<WorkRecordService.TeacherData>) msg.get("otherSubject");
                if (CollectionUtils.isNotEmpty(otherSubject)) {
                    otherSubjectTeaList.addAll(otherSubject);
                }
            }

            //年级与老师对应关系
            Map<ClazzLevel, List<Long>> allGradeTeacherListMap = gradeResourceService.getGradeTeacherListMap(school);

            //上层资源中未注册老师列表
            List<WorkRecordService.TeacherData> unRegTeaList = new ArrayList<>();
            //其他上层资源列表
            List<Map<String, Object>> otherList = new ArrayList<>();

            //上层资源列表
            Map<String, Object> outerResourceMap = workRecordService.getOuterResourceList(schoolId);
            if (MapUtils.isNotEmpty(outerResourceMap)) {
                unRegTeaList = (List<WorkRecordService.TeacherData>) outerResourceMap.get("unRegTeaList");
                otherList = (List<Map<String, Object>>) outerResourceMap.get("otherList");
                if (CollectionUtils.isNotEmpty(unRegTeaList)) {
                    unRegTeaList.forEach(item -> {
                        if (item.getSubject() == Subject.ENGLISH) {
                            englishTeaList.add(item);
                        } else if (item.getSubject() == Subject.MATH) {
                            mathTeaList.add(item);
                        } else if (item.getSubject() == Subject.CHINESE) {
                            chineseTeaList.add(item);
                        } else {
                            otherSubjectTeaList.add(item);
                        }
                    });
                    unRegTeaList.forEach(item -> {
                        List<ClazzLevel> gradeList = item.getGradeList();
                        gradeList.forEach(p -> {
                            List<Long> teacherIds = allGradeTeacherListMap.get(p);
                            if (teacherIds == null) {
                                teacherIds = new ArrayList<>();
                            }
                            teacherIds.add(item.getTeacherId());
                            allGradeTeacherListMap.put(p, teacherIds);
                        });
                    });
                }
            }

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("englishTeaList", generateTeacherInfo(englishTeaList, allGradeTeacherListMap));
            dataMap.put("mathTeaList", generateTeacherInfo(mathTeaList, allGradeTeacherListMap));
            dataMap.put("chineseTeaList", generateTeacherInfo(chineseTeaList, allGradeTeacherListMap));
            dataMap.put("otherList", otherList);
            dataMap.put("otherSubjectTeaList", assemblyOtherTeacherMessage(otherSubjectTeaList));
            mapMessage.put("dataMap", dataMap);
            mapMessage.put("schoolLevel", schoolLevel);
        } catch (Exception ex) {
            logger.error("into school search teacher is failed schoolId =" + schoolId, ex);
            return MapMessage.errorMessage("添加进校记录页搜索老师信息失败，学校ID为" + schoolId);
        }
        return mapMessage;
    }


    public List<Map<String, Object>> generateTeacherInfo(List<WorkRecordService.TeacherData> teacherList, Map<ClazzLevel, List<Long>> allGradeTeacherListMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Long> allGradeTeacherIds = allGradeTeacherListMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        List<WorkRecordService.TeacherData> noGradeTeacherList = new ArrayList<>();
        Map<ClazzLevel, List<WorkRecordService.TeacherData>> gradeTeacherListMap = new HashMap<>();
        teacherList.forEach(p -> {
            //有年级老师
            allGradeTeacherListMap.forEach((k, v) -> {
                List<WorkRecordService.TeacherData> haveGradeTeacherList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(v) && v.contains(p.getTeacherId())) {
                    haveGradeTeacherList.add(p);
                }
                if (CollectionUtils.isNotEmpty(haveGradeTeacherList)) {
                    List<WorkRecordService.TeacherData> gradeTeacherList = gradeTeacherListMap.get(k);
                    if (CollectionUtils.isNotEmpty(gradeTeacherList)) {
                        gradeTeacherList.addAll(haveGradeTeacherList);
                        gradeTeacherListMap.put(k, gradeTeacherList);
                    } else {
                        gradeTeacherListMap.put(k, haveGradeTeacherList);
                    }
                }
            });
            //无年级老师
            if (!allGradeTeacherIds.contains(p.getTeacherId())) {
                noGradeTeacherList.add(p);
            }
        });

        List<ClazzLevel> clazzLevelList = new ArrayList<>(gradeTeacherListMap.keySet());
        clazzLevelList.sort((o1, o2) -> {
            int level1 = o1.getLevel();
            int level2 = o2.getLevel();
            return Integer.compare(level1, level2);
        });
        clazzLevelList.forEach(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("gradeLevel", item.getLevel());
            itemMap.put("gradeName", item.getDescription());
            itemMap.put("teacherList", gradeTeacherListMap.get(item));
            result.add(itemMap);
        });
        if (CollectionUtils.isNotEmpty(noGradeTeacherList)) {
            Map<String, Object> item = new HashMap<>();
            item.put("gradeLevel", 0);
            item.put("gradeName", "未知年级");
            item.put("teacherList", noGradeTeacherList);
            result.add(item);
        }
        return result;
    }


    /**
     * 添加进校工作记录
     *
     * @return
     */
    @RequestMapping(value = "add_into_school_work_record.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addIntoSchoolWorkRecord() {
        Long schoolId = getRequestLong("schoolId");
        String signInRecordId = getRequestString("signInRecordId");

        Integer visitType = getRequestInt("visitType");//1：校级会议   2：拜访老师进校 3：直播展位推广
        String lecturerName = getRequestString("lecturerName");
        Integer preachingTime = getRequestInt("preachingTime");
        Integer meetingForm = getRequestInt("meetingForm");
        String photoUrl = getRequestString("photoUrl");
        String visitTheme = getRequestString("visitTheme");
        String visitUserInfoJson = getRequestString("visitUserInfoJson");
        String result = getRequestString("result");

        AuthCurrentUser user = getCurrentUser();

        if (visitType == 1 && (schoolId < 1 || StringUtils.isBlank(signInRecordId) || StringUtils.isBlank(visitUserInfoJson) || StringUtils.isBlank(lecturerName)
                || preachingTime < 1 || meetingForm < 1 || StringUtils.isBlank(photoUrl))) {
            return MapMessage.errorMessage("请填写完整的信息！");
        }
        if (visitType == 2 && (schoolId < 1 || StringUtils.isBlank(signInRecordId) || StringUtils.isBlank(visitTheme)
                || StringUtils.isBlank(visitUserInfoJson))) {
            return MapMessage.errorMessage("请填写完整的信息！");
        }
        if (visitType == 3 && (schoolId < 1 || StringUtils.isBlank(signInRecordId))) {
            return MapMessage.errorMessage("请填写完整的信息！");
        }

        //责任区域场景，判断该用户是否有权限操作，若无权限，返回学校负责人员
        MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(getCurrentUserId(), schoolId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()) {
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))) {
                return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限", mapMessage.get("schoolManager")));
            } else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }

        List<WorkRecordData> todayIntoSchool = workRecordService.getWorkRecordDataListByUserTypeTime(Collections.singletonList(user.getUserId()), AgentWorkRecordType.SCHOOL, DateUtils.getTodayStart(), DateUtils.getTodayEnd());
        if (visitType == 1) {
            todayIntoSchool = todayIntoSchool.stream().filter(p -> Objects.equals(p.getSchoolId(), schoolId) && p.getVisitSchoolType() == 1).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(todayIntoSchool) && todayIntoSchool.size() >= 2) {
                return MapMessage.errorMessage("校级会议进校一天只能两次，学校ID:" + schoolId);
            }
        }
        if (visitType == 2) {
            todayIntoSchool = todayIntoSchool.stream().filter(p -> Objects.equals(p.getSchoolId(), schoolId) && p.getVisitSchoolType() == 2).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(todayIntoSchool)) {
                return MapMessage.errorMessage("一天只能拜访该学校一次，学校ID:" + schoolId);
            }
        }
        if (visitType == 3) {
            todayIntoSchool = todayIntoSchool.stream().filter(p -> Objects.equals(p.getSchoolId(), schoolId) && p.getVisitSchoolType() == 3).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(todayIntoSchool)) {
                return MapMessage.errorMessage("一天只能拜访该学校一次，学校ID:" + schoolId);
            }
        }

        Map<Long, String> teacherDataMap = new HashMap<>();
        Map<Long, String> outerResourceDataMap = new HashMap<>();
        //校级会议、拜访老师进校
        if (visitType == 1 || visitType == 2) {
            List<Map> visitUserInfoMapList = JsonUtils.fromJsonToList(visitUserInfoJson, Map.class);
            visitUserInfoMapList.forEach(item -> {
                String origin = SafeConverter.toString(item.get("origin"));
                //老师
                if (origin.equals("teacher")) {
                    teacherDataMap.put(SafeConverter.toLong(item.get("userId")), SafeConverter.toString(item.get("visitInfo")));
                    //资源
                } else if (origin.equals("outerResource")) {
                    outerResourceDataMap.put(SafeConverter.toLong(item.get("userId")), SafeConverter.toString(item.get("visitInfo")));
                }
            });
        }

        MapMessage mapMsg = workRecordService.addIntoSchoolWorkRecord(schoolId, signInRecordId, visitType, lecturerName, preachingTime, meetingForm,
                Collections.singletonList(photoUrl), visitTheme, teacherDataMap, outerResourceDataMap, result, user.getUserId(), user.getRealName(), user.getUserPhone());
        if (mapMsg.isSuccess()) {
            AlpsThreadPool.getInstance().submit(() -> workRecordService.saveTeacherMemorandum(teacherDataMap, schoolId, SafeConverter.toString(mapMsg.get("id")), user.getUserId()));
            AlpsThreadPool.getInstance().submit(() -> workRecordService.saveSchoolMemorandum(schoolId, result, SafeConverter.toString(mapMsg.get("id")), user.getUserId()));
            AlpsThreadPool.getInstance().submit(() -> agentSchoolLastWorkRecordService.updateLastVisitTime(SafeConverter.toString(mapMsg.get("id")), schoolId, user.getUserId(), user.getRealName()));
        }
        return mapMsg;
    }

    //------------------------------------------组会--------------------------------------------------------------------

    /**
     * 添加组会工作记录
     *
     * @return
     */
    @RequestMapping(value = "add_meeting_work_record.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addMeetingWorkRecord() {

        CrmMeetingType meetingType = CrmMeetingType.nameOf(getRequestString("meetingType"));
        String signInRecordId = getRequestString("signInRecordId");

        String meetingTitle = getRequestString("meetingTitle");
        Integer attendances = getRequestInt("attendances");

        String supporterDataJson = getRequestString("supporterDataJson");
        Boolean isPresent = getRequestBool("isPresent");
        String lecturerName = getRequestString("lecturerName");  // 讲师姓名
        Integer preachingTime = getRequestInt("preachingTime");  // 宣讲时长
        Integer form = getRequestInt("form");            // 形式
        String photoUrl = getRequestString("photoUrl");
        String result = getRequestString("result");

        if (meetingType == null || StringUtils.isBlank(signInRecordId) || StringUtils.isBlank(lecturerName)
                || (preachingTime != 1 && preachingTime != 2 && preachingTime != 3)
                || (form != 1 && form != 2)
                || StringUtils.isBlank(photoUrl)
        ) {
            return MapMessage.errorMessage("信息不全，请填写完整");

        }

        AuthCurrentUser user = getCurrentUser();

        List<Map> resultList = JsonUtils.fromJsonToList(supporterDataJson, Map.class);
        List<Map<String, Object>> supporterDataList = workRecordService.convertMap(resultList);

        return workRecordService.saveMeetingWorkRecord(meetingType, signInRecordId, meetingTitle, attendances, supporterDataList, isPresent, lecturerName, preachingTime, form, Collections.singletonList(photoUrl), result, user.getUserId(), user.getRealName());

    }

    //------------------------------------------资源拓维----------------------------------------------------------------

    /**
     * 资源拓维，搜索老师
     *
     * @return
     */
    @RequestMapping(value = "resource_extension_search_teacher_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage resourceExtensionSearchTeacherList() {
        String searchName = getRequestString("searchName");
        Integer scene = getRequestInt("scene", 3);
        Integer pageNo = getRequestInt("pageNo");       //第几页
        Integer pageSize = getRequestInt("pageSize");   //每页数量
        Long userId = getCurrentUserId();
        try {
            //es获取老师信息
            Set<Long> teacherIds = new HashSet<>();
            List<Long> esTeacherList = searchService.searchTeachersForSceneWithPage(userId, searchName, scene, pageNo, pageSize);
            if (CollectionUtils.isNotEmpty(esTeacherList)) {
                teacherIds.addAll(esTeacherList);
            }

            List<AgentOuterResourceView> teacherInfoList = teacherResourceService.generateTeacherInfo(teacherIds);
            Map<String, Object> dataMap = new HashMap<>();
            //判断是否没有更多数据了
            if (esTeacherList.size() < pageSize) {
                dataMap.put("noMoreData", true);
            } else {
                dataMap.put("noMoreData", false);
            }
            dataMap.put("teacherList", teacherInfoList);
            return MapMessage.successMessage().add("dataMap", dataMap);
        } catch (Exception ex) {
            logger.error("Failed searching teacher, user={}, key={}", userId, searchName, ex);
            return MapMessage.errorMessage();
        }

    }

    /**
     * 资源拓维，选择上层资源列表
     *
     * @return
     */
    @RequestMapping(value = "resource_extension_search_outer_resource_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage resourceExtensionSearchOuterResourceList() {
        String searchName = getRequestString("searchName");
        Integer pageNo = getRequestInt("pageNo");       //第几页
        Integer pageSize = getRequestInt("pageSize");   //每页数量

        if (pageSize <= 0) {
            return MapMessage.errorMessage("每页显示数量必须大于零！");
        }
        List<AgentOuterResourceView> outerResourceViewList = new ArrayList<>();
        Boolean noMoreData = true;
        Pageable pageable = new PageRequest(pageNo, pageSize);
        Long userId = getCurrentUserId();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        //搜索上层资源
        List<Map<String, Object>> outerResourceList = agentResearchersService.searchResearcherList(getCurrentUserId(), searchName, pageable);
        if (CollectionUtils.isNotEmpty(outerResourceList)) {
            //过滤出已解锁的上层资源
            if (userRole == AgentRoleType.BusinessDeveloper) {
                outerResourceList = outerResourceList.stream().filter(p -> !SafeConverter.toBoolean(p.get("lockStatus"))).collect(Collectors.toList());
            }
            MapMessage mapMessage = agentOuterResourceService.pageResource(outerResourceList, pageable);
            if (mapMessage.isSuccess()) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) mapMessage.get("dataList");
                noMoreData = !SafeConverter.toBoolean(mapMessage.get("hasNext"));
                //上层资源数据转换
                outerResourceViewList.addAll(agentOuterResourceService.generateOuterResourceInfo(dataList));
            }
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("outerResourceList", outerResourceViewList);
        dataMap.put("noMoreData", noMoreData);
        return MapMessage.successMessage().add("dataMap", dataMap);
    }

    /**
     * 添加资源拓维工作记录
     *
     * @return
     */
    @RequestMapping(value = "add_resource_extension_work_record.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addResourceExtensionWorkRecord() {
        Integer intention = getRequestInt("intention");   //拜访目的
        String signInRecordId = getRequestString("signInRecordId");         //签到记录
        String visitPhotoUrl = getRequestString("visitPhotoUrl");   //拜访照片
        String content = getRequestString("content");             //拜访过程
        String teacherResultJsonStr = getRequestString("teacherResultJsonStr");//老师达成结果
        String outerResourceResultJsonStr = getRequestString("outerResourceResultJsonStr");//上层资源达成结果
        if (intention < 1 || StringUtils.isBlank(signInRecordId) || StringUtils.isBlank(visitPhotoUrl) || StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("信息填写不完整！");
        }
        if (StringUtils.isBlank(teacherResultJsonStr) && StringUtils.isBlank(outerResourceResultJsonStr)) {
            return MapMessage.errorMessage("请填写达成结果！");
        }

        //老师
        Map<Long, String> teacherResultMap = workRecordService.generateMapFormJson(teacherResultJsonStr);
        //上层资源
        Map<Long, String> outerResourceResultMap = workRecordService.generateMapFormJson(outerResourceResultJsonStr);

        AuthCurrentUser user = getCurrentUser();
        return workRecordService.addResourceExtensionWorkRecord(intention, signInRecordId, Collections.singletonList(visitPhotoUrl), content, teacherResultMap, outerResourceResultMap, "", user.getUserId(), user.getRealName());
    }

    /**
     * 资源拓维详情
     *
     * @return
     */
    @RequestMapping(value = "resource_extension_work_record_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage resourceExtensionWorkRecordDetail() {
        MapMessage mapMessage = MapMessage.successMessage();
        String workRecordId = getRequestString("workRecordId");
        if (StringUtils.isBlank(workRecordId)) {
            return MapMessage.errorMessage("拜访记录不正确");
        }
        WorkRecordData workRecordData = workRecordService.getWorkRecordDataByIdAndType(workRecordId, AgentWorkRecordType.RESOURCE_EXTENSION);
        if (workRecordData == null) {
            return MapMessage.errorMessage("拜访记录不存在！");
        }
        mapMessage.put("dataMap", workRecordService.resourceExtensionDetailToMap(workRecordData));
        return mapMessage;
    }

    //----------------------------------------------陪同----------------------------------------------------------------

    /**
     * 查找当天当前用户直属部门及其子部门所有人员中，未被当前用户陪同的拜访记录数据
     *
     * @return
     */
    @RequestMapping(value = "search_accompany_visit_record.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchAccompanyVisitRecord() {
        String keyWords = getRequestString("keyWords");
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("dataList", workRecordService.searchNeedAccompanyWorkRecordMap(keyWords, getCurrentUser()));
        return mapMessage;
    }

    /**
     * 添加陪同之前校验接口
     *
     * @return
     */
    @RequestMapping(value = "before_add_accompany_visit_record.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage beforeAddAccompanyVisitRecord() {
        MapMessage mapMessage = MapMessage.successMessage();
        String workRecordId = requestString("workRecordId");
        String workTypeStr = getRequestString("workType");
        if (StringUtils.isBlank(workRecordId)) {
            return MapMessage.errorMessage("选择陪同记录不正确");
        }
        AgentWorkRecordType workRecordType = AgentWorkRecordType.nameOf(workTypeStr);
        if (workRecordType == null) {
            return MapMessage.errorMessage("工作类型不正确！");
        }
        WorkRecordData workRecordData = workRecordService.getWorkRecordDataByIdAndType(workRecordId, workRecordType);
        if (workRecordData == null) {
            return MapMessage.errorMessage("陪同记录不正确");
        }
        mapMessage.put("dataMap", workRecordService.beforeAddAccompanyVisitRecord(getCurrentUserId(), workRecordData));
        return mapMessage;
    }

    /**
     * 添加陪同工作记录
     *
     * @return
     */
    @RequestMapping(value = "add_accompany_work_record.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addAccompanyWorkRecord() {
        AccompanyBusinessType businessType = AccompanyBusinessType.nameOf(getRequestString("businessType"));
        String businessId = getRequestString("businessId");
        String signInRecordId = getRequestString("signInRecordId");

        String purpose = getRequestString("purpose");

        String photoUrl = getRequestString("photoUrl");
        String result = getRequestString("result");

        String evaluateInfo = getRequestString("evaluateInfo");

        if (businessType == null || StringUtils.isBlank(businessId)
                || StringUtils.isBlank(signInRecordId)
                || StringUtils.isBlank(purpose)
        ) {
            return MapMessage.errorMessage("信息不全，请填写完整");
        }
        boolean needUploadImg = getRequestBool("needUploadImg");
        if (needUploadImg && StringUtils.isBlank(photoUrl)) {
            return MapMessage.errorMessage("请上传图卡片！");
        }
        boolean needShowAppraiseAndSuggest = getRequestBool("needShowAppraiseAndSuggest");
        if (needShowAppraiseAndSuggest && (StringUtils.isBlank(result) || StringUtils.isBlank(evaluateInfo))) {
            return MapMessage.errorMessage("信息不全，请填写完整！");
        }

        AuthCurrentUser user = getCurrentUser();

        Map<EvaluationIndicator, Integer> evaluationMap = new HashMap<>();
        if (StringUtils.isNotBlank(evaluateInfo)) {
            Map<String, String> evaluationStrMap = JsonUtils.fromJsonToMapStringString(evaluateInfo);
            if (MapUtils.isNotEmpty(evaluationStrMap)) {
                evaluationStrMap.forEach((k, v) -> {
                    evaluationMap.put(EvaluationIndicator.nameOf(k), SafeConverter.toInt(v));
                });
            }
        }
        return workRecordService.saveAccompanyRecord(businessType, businessId, signInRecordId, purpose, Collections.singletonList(photoUrl), result, evaluationMap, user.getUserId(), user.getRealName());
    }

    /**
     * 陪同详情
     *
     * @return
     */
    @RequestMapping(value = "accompany_work_record_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage accompanyWorkRecordDetail() {
        String workRecordId = getRequestString("workRecordId");
        WorkRecordData workRecordData = workRecordService.getWorkRecordDataByIdAndType(workRecordId, AgentWorkRecordType.ACCOMPANY);
        if (workRecordData == null) {
            return MapMessage.errorMessage("陪同信息不存在！");
        }
        return workRecordService.accompanyWorkRecordDetail(workRecordData);
    }
}
