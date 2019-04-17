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

package com.voxlearning.utopia.agent.controller.mobile.schoolclue;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.SchoolType;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.SchoolShortInfo;
import com.voxlearning.utopia.agent.bean.resource.SchoolResourceCard;
import com.voxlearning.utopia.agent.constants.AgentErrorCode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.service.mobile.SchoolClueService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.agent.view.school.SchoolGradeBasicData;
import com.voxlearning.utopia.api.constant.CrmSchoolClueStatus;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.entity.crm.constants.SchoolOperationType;
import com.voxlearning.utopia.entity.crm.schoolrecord.SchoolServiceRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.consumer.service.CrmSimilarSchoolServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolServiceRecordLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Create new school and apply school clue begin here.
 * Created by yaguang.wang
 * on 2017/4/19.
 */
@Controller
@RequestMapping(value = "/mobile/school_clue")
public class SchoolClueController extends AbstractAgentController {

    private static final String NEW_SCHOOL = "new_school";
    private static final String UPDATE_SCHOOL = "update_school";
    private static final String APPRAISAL_SCHOOL = "appraisal_school";
    private static final Integer LOCKED_DAY = 25;
    private static final Integer SIMILAR_LIMIT = 5;

    @Inject private RaikouSystem raikouSystem;

    // remote
    @Inject private CrmSimilarSchoolServiceClient crmSimilarSchoolServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    // local
    @Inject private AgentDictSchoolService agentDictSchoolService;
    @Inject private SchoolClueService schoolClueService;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private SchoolServiceRecordLoaderClient schoolServiceRecordLoaderClient;
    @Inject private AgentRequestSupport agentRequestSupport;
    @Inject private SchoolResourceService schoolResourceService;
    @Inject private BaseDictService baseDictService;

    //鉴定学校
    @RequestMapping(value = "appraisalSchool.vpage", method = RequestMethod.GET)
    @OperationCode("0926d6d5026c4ec8")
    public String appraisalSchool(Model model) {
        initSchoolInfoPage(getAppraisalSchool(), model);
        model.addAttribute("type", APPRAISAL_SCHOOL);
        return "rebuildViewDir/mobile/school/schoolappraisal";
    }

    // 获取用户默认展示的学校列表
    @RequestMapping(value = "auth_school.vpage", method = RequestMethod.GET)
    public String choiceAuthSchool(Model model) {
        List<Long> manageSchoolIds = getUserManageSchoolIds(getCurrentUser());
        model.addAttribute("schoolCardList", createSchoolCard(manageSchoolIds));
        return "rebuildViewDir/mobile/school/chooseSchool";
    }

    private List<SchoolResourceCard> createSchoolCard(List<Long> manageSchoolIds) {
        Map<Long, School> manageSchools = schoolLoaderClient.getSchoolLoader().loadSchools(manageSchoolIds).getUninterruptibly();
        return manageSchools.values().stream().map(this::createSchoolCard).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private SchoolResourceCard createSchoolCard(School school) {
        if (school == null || school.getSchoolAuthenticationState() != AuthenticationState.WAITING) {
            return null;
        }
        SchoolResourceCard card = new SchoolResourceCard();
        card.setSchoolId(school.getId());
        card.setFullName(school.loadSchoolFullName());
        card.setShortName(school.getShortName());
        card.setSchoolLevel(SchoolLevel.safeParse(school.getLevel()));
        return card;
    }

    // 保存所需要鉴定的学校
    @RequestMapping(value = "save_appraisal_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAppraisalSchool() {
        Long schoolId = getRequestLong("schoolId");
        MapMessage msg = checkSchoolInfo(schoolId);
        if (!msg.isSuccess()) {
            return msg;
        }
        CrmSchoolClue schoolClue = getAppraisalSchool();
        if (!Objects.equals(schoolClue.getSchoolId(), schoolId)) {
            clearSchoolInfo(schoolClue);
        }
        schoolClue.setCmainName(((School) msg.get("school")).getCmainName());
        schoolClue.setSchoolDistrict(((School) msg.get("school")).getSchoolDistrict());
        schoolClue.setSchoolName(((School) msg.get("school")).loadSchoolFullName());
        schoolClue.setSchoolId(((School) msg.get("school")).getId());
        setAppraisalSchool(schoolClue);
        return MapMessage.successMessage();
    }

    // 提交学校鉴定申请
    @RequestMapping(value = "appraisalSchool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage appraisalSchoolApply() {
        CrmSchoolClue schoolClue = getAppraisalSchool();
        MapMessage msg = checkSchoolInfo(schoolClue.getSchoolId());
        if (!msg.isSuccess()) {
            return msg;
        }
        if (StringUtils.isBlank(schoolClue.getPhotoUrl())) {
            return MapMessage.errorMessage("请拍摄学校的正门照片");
        }
        if (StringUtils.isBlank(schoolClue.getLatitude()) || StringUtils.isBlank(schoolClue.getLongitude()) || StringUtils.isBlank(schoolClue.getAddress())) {
            return MapMessage.errorMessage("学校地理位置信息解析错误，请重新拍摄学校正门照片。");
        }
        AuthCurrentUser user = getCurrentUser();
        msg = schoolClueService.upsertSchoolClueBySchoolId(schoolClue.getSchoolId(), schoolClue.getLatitude(), schoolClue.getLongitude(), user.getUserId(), user.getRealName(), user.getUserPhone()
                , schoolClue.getPhotoUrl(), schoolClue.getCoordinateType(), schoolClue.getAddress());
        if (!msg.isSuccess()) {
            return msg;
        }
        cleanAppraisalSchool();
        return msg;
    }

    private MapMessage checkSchoolInfo(Long schoolId) {
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("无法找到所选的学校，学校ID：" + schoolId);
        }
        if (school.getSchoolAuthenticationState() == AuthenticationState.SUCCESS) {
            return MapMessage.errorMessage("该学校已经鉴定，无需再次鉴定");
        }
        List<CrmSchoolClue> crmSchoolClues = schoolClueService.loads(schoolId);
        if (crmSchoolClues.stream().anyMatch(p -> !p.getDisabled() && 5 == SafeConverter.toInt(p.getAuthenticateType()) && CrmSchoolClueStatus.codeOf(p.getStatus()) == CrmSchoolClueStatus.待审核)) {
            return MapMessage.errorMessage("该学校已提交鉴定学校申请，请勿重复提交");
        }
        return MapMessage.successMessage().add("school", school);
    }

    private List<Long> getUserManageSchoolIds(AuthCurrentUser currentUser) {
        List<Long> manageSchoolIds = new ArrayList<>();
        if (currentUser.isBusinessDeveloper()) {
            manageSchoolIds = baseOrgService.getManagedSchoolList(currentUser.getUserId());
        } else if (currentUser.isCityManager()) {
            manageSchoolIds = baseOrgService.getManagedSchoolList(currentUser.getUserId());
            List<AgentGroupUser> groupUser = baseOrgService.getGroupUserByUser(currentUser.getUserId());
            if (CollectionUtils.isEmpty(groupUser)) {
                return Collections.emptyList();
            }
            List<Long> bdIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupUser.get(0).getGroupId(), AgentRoleType.BusinessDeveloper.getId());
            Set<Long> schoolIds = baseOrgService.getUserSchoolByUsers(bdIds).values().stream().flatMap(List::stream).map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
            manageSchoolIds = manageSchoolIds.stream().filter(p -> !schoolIds.contains(p)).collect(toList());
        }
        return manageSchoolIds;
    }

    private static void clearSchoolInfo(CrmSchoolClue schoolInfo) {
        schoolInfo.setSchoolName(null);
        schoolInfo.setShortName(null);
        schoolInfo.setCmainName(null);
        schoolInfo.setSchoolDistrict(null);
        schoolInfo.setSchoolPhase(null);
        schoolInfo.setPhotoUrl(null);
        schoolInfo.setLatitude(null);
        schoolInfo.setLongitude(null);
        schoolInfo.setAddress(null);
        schoolInfo.setCoordinateType(null);
        schoolInfo.setDateTime(null);
        schoolInfo.setMake(null);
        schoolInfo.setModel(null);
        //schoolInfo.setSchoolingLength(null);
        schoolInfo.setEduSystem(null);
        schoolInfo.setEnglishStartGrade(null);
        schoolInfo.setBranchSchoolIds(null);
        schoolInfo.setNewGrade1ClassCount(null);
        schoolInfo.setNewGrade2ClassCount(null);
        schoolInfo.setNewGrade3ClassCount(null);
        schoolInfo.setNewGrade4ClassCount(null);
        schoolInfo.setNewGrade5ClassCount(null);
        schoolInfo.setNewGrade6ClassCount(null);
        schoolInfo.setNewGrade7ClassCount(null);
        schoolInfo.setNewGrade8ClassCount(null);
        schoolInfo.setNewGrade9ClassCount(null);
        schoolInfo.setNewGrade13ClassCount(null);
        schoolInfo.setNewGrade11ClassCount(null);
        schoolInfo.setNewGrade12ClassCount(null);
        schoolInfo.setGrade1StudentCount(null);
        schoolInfo.setGrade2StudentCount(null);
        schoolInfo.setGrade3StudentCount(null);
        schoolInfo.setGrade4StudentCount(null);
        schoolInfo.setGrade5StudentCount(null);
        schoolInfo.setGrade6StudentCount(null);
        schoolInfo.setGrade7StudentCount(null);
        schoolInfo.setGrade8StudentCount(null);
        schoolInfo.setGrade9StudentCount(null);
        schoolInfo.setGrade13StudentCount(null);
        schoolInfo.setGrade11StudentCount(null);
        schoolInfo.setGrade12StudentCount(null);
        schoolInfo.setInfantGrade(new ArrayList<>());
    }

    // 添加新学校
    @RequestMapping(value = "addnewschoolpage.vpage", method = RequestMethod.GET)
    public String addNewSchoolPage(Model model) {
        initSchoolInfoPage(getNewSchool(), model);
        model.addAttribute("type", "newSchool");                     // 当前页的类型
        model.addAttribute("locked", false);
        return "rebuildViewDir/mobile/school/addnewschoolpage";
    }

    // 编辑学校信息页
    @RequestMapping(value = "updateschool.vpage", method = RequestMethod.GET)
    public String updateSchool(Model model) {
        Long schoolId = getRequestLong("schoolId");
        CrmSchoolClue updateSchool = getUpdateInfo(schoolId);
        if (null == updateSchool) {
            return errorInfoPage(AgentErrorCode.SCHOOL_UPDATE_INFO_ERROR, "编辑学校信息时未找到学校信息", model);
        }
        setUpdateSchool(updateSchool);
        initSchoolInfoPage(updateSchool, model);
        model.addAttribute("locked", false);
        model.addAttribute("type", "updateSchool");                          // 当前页的类型
        model.addAttribute("gradeDataList", schoolResourceService.generateGradeBasicDataList(schoolId));
        return "rebuildViewDir/mobile/school/updateschool";
    }

    /**
     * 学校信息确认页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "confirm_school_info.vpage", method = RequestMethod.GET)
    public String confirmSchoolInfo(Model model) {
        Long schoolId = getRequestLong("schoolId");
        CrmSchoolClue updateSchool = getUpdateInfo(schoolId);
        if (null == updateSchool) {
            return errorInfoPage(AgentErrorCode.SCHOOL_UPDATE_INFO_ERROR, "编辑学校信息时未找到学校信息", model);
        }
        setUpdateSchool(updateSchool);
        initSchoolInfoPage(updateSchool, model);
        model.addAttribute("locked", false);
        model.addAttribute("type", "confirmSchool");                          // 当前页的类型
        model.addAttribute("gradeDataList", schoolResourceService.generateGradeBasicDataList(schoolId));
        return "rebuildViewDir/mobile/school/confirm_school_info";
    }

    /**
     * 学校信息修改对比页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "school_info_diff.vpage", method = RequestMethod.GET)
    public String schoolInfoDiffPage(Model model) {
        String recordId = getRequestString("recordId");
        SchoolServiceRecord schoolServiceRecord = schoolServiceRecordLoaderClient.load(recordId);
        if (null != schoolServiceRecord && null != schoolServiceRecord.getOperationContent()) {
            String content = schoolServiceRecord.getOperationContent();
            Map<String, Object> objectMap = JsonUtils.fromJson(content);
            Map<String, Object> resultMap = new TreeMap<>();
            if (objectMap.containsKey("newData")) {

                Map<String, Object> newData = (Map<String, Object>) objectMap.get("newData");
                if (newData.containsKey("infantGrade")) {
                    List<Map<String, Object>> infantGradeList = (List<Map<String, Object>>) newData.get("infantGrade");
                    newData.putAll(getGradeInfo(infantGradeList));
                }
                resultMap.put("newData", newData);
            }
            if (objectMap.containsKey("oldData")) {
                Map<String, Object> oldData = (Map<String, Object>) objectMap.get("oldData");
                if (oldData.containsKey("infantGrade")) {
                    List<Map<String, Object>> infantGradeList = (List<Map<String, Object>>) oldData.get("infantGrade");
                    oldData.putAll(getGradeInfo(infantGradeList));
                }
                resultMap.put("oldData", oldData);
            }
            model.addAttribute("diffData", resultMap);
        }
        return "rebuildViewDir/mobile/school/school_info_diff";
    }

    private CrmSchoolClue getUpdateInfo(Long schoolId) {
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return null;
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        CrmSchoolClue updateSchool = getUpdateSchool();
        if (!Objects.equals(updateSchool.getSchoolId(), schoolId)) {
            updateSchool = initSchoolClue(school, schoolExtInfo);
        }
        return updateSchool;
    }

    /**
     * 校验学校信息完整性
     *
     * @return
     */
    @RequestMapping(value = "check_school_info_complete.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage checkSchoolInfoComplete() {
        Long schoolId = getRequestLong("schoolId");
        boolean isComplete = true;
        CrmSchoolSummary crmSchoolSummary = crmSummaryLoaderClient.loadSchoolSummary(schoolId);
        if (null != crmSchoolSummary && null != crmSchoolSummary.getStudentAuthedCount() && crmSchoolSummary.getStudentAuthedCount() > 100) {

            isComplete = schoolResourceService.checkGradeBasicDataIsComplete(schoolId);
            //2.校验这个学校在这个学年有没有被这个人确认过基础信息
            if (isComplete) {
                DateRange schoolYearDateRange = SchoolYear.newInstance().getSchoolYearDateRange();
                List<SchoolServiceRecord> schoolServiceRecords = schoolServiceRecordLoaderClient.load(String.valueOf(getCurrentUserId()), SchoolOperationType.CONFIRM_SCHOOL_INFO, schoolYearDateRange.getStartDate());
                if (CollectionUtils.isNotEmpty(schoolServiceRecords)) {
                    Stream<SchoolServiceRecord> schoolServiceRecordStream = schoolServiceRecords.stream().filter(item -> Objects.equals(item.getSchoolId(), schoolId));
                    isComplete = schoolServiceRecordStream.count() > 0;
                } else {
                    isComplete = false;
                }
            }
            //3.
        }
        return MapMessage.successMessage().add("complete", isComplete);
    }
/*    public MapMessage checkSchoolInfoComplete() {
        Long schoolId = getRequestLong("schoolId");
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        boolean isComplete = (null != schoolExtInfo) && schoolExtInfo.isCompleteExtInfo();
        return MapMessage.successMessage().add("complete",isComplete);
    }*/


    // 保存新建和修改学校的内容到session
    @RequestMapping(value = "save_school_info_session.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSchoolInfoSession(@RequestBody String schoolBasicInfo) {
        Long schoolId = getRequestLong("schoolId");
        CrmSchoolClue schoolInfo = getNowSchoolClue(schoolId);
        initSchoolBasicByJson(schoolBasicInfo, schoolInfo, true);
        setNowSchoolClue(schoolInfo, schoolId);
        return MapMessage.successMessage();
    }

    // 保存新学校
    @RequestMapping(value = "save_new_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("c7c4ca02c2cf4d6c")
    public MapMessage saveNewSchool(@RequestBody String schoolBasicInfo) {
        CrmSchoolClue clue = getNewSchool();
        MapMessage msg = initSchoolBasicByJson(schoolBasicInfo, clue, true);
        if (!msg.isSuccess()) {
            return msg;
        }
        AuthCurrentUser user = getCurrentUser();
        clue.setRecorderId(user.getUserId());
        clue.setRecorderName(user.getRealName());
        clue.setRecorderPhone(user.getUserPhone());
        msg = schoolClueService.addNewSchool(clue);
        if (msg.isSuccess()) {
            cleanNewSchool();
            Long schoolId = SafeConverter.toLong(msg.get("schoolId"));
            if (schoolId > 0) {
                Map<String, Object> basicInfo = JsonUtils.fromJson(schoolBasicInfo);
                String gradeDataJson = JsonUtils.toJson(basicInfo.get("gradeDataJson"));
                List<SchoolGradeBasicData> gradeDataList = JsonUtils.fromJsonToList(gradeDataJson, SchoolGradeBasicData.class);
                schoolResourceService.updateSchoolGradeData(schoolId, gradeDataList);
            }

        }


        return msg;
    }

    // 保存修改的学校信息，此时需要检查学制的变更情况
    @RequestMapping(value = "save_update_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveUpdateSchool(@RequestBody String schoolBasicInfo) {
        CrmSchoolClue clue = getUpdateSchool();
        Map<String, Object> basicInfo = JsonUtils.fromJson(schoolBasicInfo);
        String gradeDataJson = JsonUtils.toJson(basicInfo.get("gradeDataJson"));
        List<SchoolGradeBasicData> gradeDataList = JsonUtils.fromJsonToList(gradeDataJson, SchoolGradeBasicData.class);
        return doUpdate(schoolBasicInfo, clue, gradeDataList);
    }

    private MapMessage doUpdate(String schoolBasicInfo, CrmSchoolClue clue, List<SchoolGradeBasicData> gradeDataList) {
        MapMessage msg = initSchoolBasicByJson(schoolBasicInfo, clue, false);
        if (!msg.isSuccess()) {
            return msg;
        }
        Boolean confirm = StringUtils.isNoneBlank(SafeConverter.toString(msg.get("confirm")));
        AuthCurrentUser user = getCurrentUser();
        clue.setRecorderId(user.getUserId());
        clue.setRecorderName(user.getRealName());
        clue.setRecorderPhone(user.getUserPhone());

        msg = schoolClueService.updateSchool(clue, confirm, gradeDataList);
        if (msg.isSuccess()) {
            cleanUpdateSchool();
        }
        return msg;
    }

    @RequestMapping(value = "save_confirm_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveConfirmSchool(@RequestBody String schoolBasicInfo) {
        CrmSchoolClue clue = getUpdateSchool();
        Map<String, Object> basicInfo = JsonUtils.fromJson(schoolBasicInfo);
        String gradeDataJson = JsonUtils.toJson(basicInfo.get("gradeDataJson"));
        List<SchoolGradeBasicData> gradeDataList = JsonUtils.fromJsonToList(gradeDataJson, SchoolGradeBasicData.class);
        MapMessage msg = doUpdate(schoolBasicInfo, clue, gradeDataList);
        if (msg.isSuccess()) {
            schoolClueService.addConfirmSchoolInfoLog(clue, SchoolOperationType.CONFIRM_SCHOOL_INFO);
        }
        return msg;
    }


    // 新建学校完善信息，学制没有才显示所以不需要校验学制的问题
    @RequestMapping(value = "update_school_ext_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateSchoolExtInfo(@RequestBody String schoolBasicInfo) {
        CrmSchoolClue clue = new CrmSchoolClue();
        MapMessage msg = initSchoolExtInfo(schoolBasicInfo, clue);
        if (!msg.isSuccess()) {
            return msg;
        }
        Map<String, Object> basicInfo = JsonUtils.fromJson(schoolBasicInfo);
        String gradeDataJson = JsonUtils.toJson(basicInfo.get("gradeDataJson"));
        List<SchoolGradeBasicData> gradeDataList = JsonUtils.fromJsonToList(gradeDataJson, SchoolGradeBasicData.class);

        AuthCurrentUser user = getCurrentUser();
        clue.setRecorderId(user.getUserId());
        clue.setRecorderName(user.getRealName());
        clue.setRecorderPhone(user.getUserPhone());
        return schoolClueService.updateSchool(clue, gradeDataList);
    }

    //审核中的学校信息
    @RequestMapping(value = "apply_list.vpage", method = RequestMethod.GET)
    public String schoolApplyList(Model model) {
        Long schoolId = getRequestLong("schoolId");
        model.addAttribute("list", schoolClueService.loads(schoolId)
                .stream()
                .filter(p -> 5 == p.getAuthenticateType() && 1 == p.getStatus())
                .collect(Collectors.toList()));
        return "rebuildViewDir/mobile/school/school_apply_list";
    }

    private CrmSchoolClue initSchoolClue(School school, SchoolExtInfo schoolExtInfo) {
        CrmSchoolClue clue = new CrmSchoolClue();
        ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
        if (region == null) {
            return clue;
        }
        clue.setProvinceName(region.getProvinceName());
        clue.setProvinceCode(region.getProvinceCode());
        clue.setCityName(region.getCityName());
        clue.setCityCode(region.getCityCode());
        clue.setCountyCode(region.getCountyCode());
        clue.setCountyName(region.getCountyName());
        clue.setSchoolId(school.getId());
        clue.setSchoolPhase(school.getLevel());
        clue.setCmainName(school.getCmainName());
        clue.setSchoolDistrict(school.getSchoolDistrict());
        clue.setShortName(school.getShortName());
        if (schoolExtInfo != null) {
            BeanUtils.copyProperties(schoolExtInfo, clue);
        }
        clue.setEduSystem(schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly());
        return clue;
    }


    // fixme ziqi.feng 此处为页面可以取到的字段，在新学校，学校编辑，学校详情页都使用如下字段
    private void initSchoolInfoPage(CrmSchoolClue clue, Model model) {
        // 地区名称
        model.addAttribute("regionName", StringUtils.isBlank(clue.getCountyName()) ? null : String.format("%s %s", SafeConverter.toString(clue.getCityName()), SafeConverter.toString(clue.getCountyName())));
        // 学校所在地区的ID
        model.addAttribute("regionCode", clue.getCountyCode());
        // 学校的主干名
        model.addAttribute("cname", clue.loadSchoolFullName());
        // 学校的校区信息
        model.addAttribute("schoolDistrict", clue.getSchoolDistrict());
        // 学校的阶段
        model.addAttribute("phase", createSchoolPhase(clue.getSchoolPhase()));
        model.addAttribute("phase_value", clue.getSchoolPhase());

        // 学校的照片
        model.addAttribute("photoUrl", clue.getPhotoUrl());
        // 学校的地址
        model.addAttribute("address", clue.getAddress());
        AlpsFuture<Map<Long, School>> future = schoolLoaderClient.getSchoolLoader()
                .loadSchools(clue.getBranchSchoolIds());
        // 分校信息
        model.addAttribute("branchSchools", createBranchSchoolInfo(future.getUninterruptibly().values()));
        // 学制
        model.addAttribute("eduSystem", clue.fetchEduSystem());
        model.addAttribute("eduSystemName", clue.fetchEduSystem() != null ? clue.fetchEduSystem().getDescription() : "");
        // 英语启示年级
        model.addAttribute("englishStartGrade", clue.getEnglishStartGrade());
        // 学校ID
        model.addAttribute("schoolId", clue.getSchoolId());

        model.addAttribute("eduSystemTypes", createSchoolLength(clue.fetchEduSystem()));
        model.addAllAttributes(getGradeInfo(clue.fetchGradeInfo(true)));
        model.addAllAttributes(getGradeInfo(clue.getInfantGrade()));
        //走读方式
        model.addAttribute("externOrBoarder", clue.getExternOrBoarder());
    }

    private Map<String, Object> getGradeInfo(List<Map<String, Object>> gradeInfo) {
        Map<String, Object> result = new HashMap<>();
        if (gradeInfo == null) {
            return result;
        }
        gradeInfo.forEach(p -> {
            Object gradeLevel = p.get("gradeLevel");
            result.put(StringUtils.formatMessage("studentCount{}", gradeLevel), StringUtils.isBlank(SafeConverter.toString(p.get("studentCount"))) ? null : SafeConverter.toInt(p.get("studentCount")));
            result.put(StringUtils.formatMessage("classCount{}", gradeLevel), StringUtils.isBlank(SafeConverter.toString(p.get("classCount"))) ? null : SafeConverter.toInt(p.get("classCount")));
        });
        return result;
    }

    private List<Map<String, Object>> createSchoolPhase(Integer schoolPhase) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SchoolLevel schoolLevel : SchoolLevel.values()) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", schoolLevel.getLevel());
            info.put("phase", schoolLevel.getDescription());
            if (Objects.equals(schoolLevel.getLevel(), schoolPhase)) {
                info.put("selected", true);
            }
            result.add(info);
        }
        return result;
    }

    // 保存学校的地区
    @RequestMapping(value = "choice_region.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSchoolRegion() {
        Integer regionCode = getRequestInt("regionCode");
        CrmSchoolClue newSchool = getNewSchool();
        if (!Objects.equals(newSchool.getCountyCode(), regionCode)) {
            clearSchoolInfo(newSchool);
        }
        ExRegion region = raikouSystem.loadRegion(regionCode);
        if (region == null) {
            return MapMessage.errorMessage("请先选择地区");
        }
        newSchool.setProvinceName(region.getProvinceName());
        newSchool.setProvinceCode(region.getProvinceCode());
        newSchool.setCityName(region.getCityName());
        newSchool.setCityCode(region.getCityCode());
        newSchool.setCountyCode(region.getCountyCode());
        newSchool.setCountyName(region.getCountyName());
        setNewSchool(newSchool);
        return MapMessage.successMessage();
    }

    // 学校名称页 （只有新建学校才能进入这个页）
    @RequestMapping(value = "school_name.vpage", method = RequestMethod.GET)
    public String schoolNamePage(Model model) {
        CrmSchoolClue newSchool = getNewSchool();
        model.addAttribute("schoolId", newSchool.getSchoolId());
        model.addAttribute("mainName", newSchool.getCmainName());
        model.addAttribute("schoolDistrict", newSchool.getSchoolDistrict());
        return "rebuildViewDir/mobile/school/edit_school_name";
    }

    // 查询并保存学校全称或简称页
    @RequestMapping(value = "save_name.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveName() {
        CrmSchoolClue schoolInfo = getNewSchool();
        Integer countyCode = schoolInfo.getCountyCode();
        Integer schoolPhase = schoolInfo.getSchoolPhase();
        String schoolDistrict = getRequestString("schoolDistrict");
        if (countyCode == null || Objects.equals(countyCode, 0)) {
            return MapMessage.errorMessage("请先选择学校所在的地区");
        }
        if (schoolPhase == null || Objects.equals(schoolPhase, 0)) {
            return MapMessage.errorMessage("请先选择学校所处的阶段");
        }
        String schoolName = getRequestString("schoolName");
        if (StringUtils.isBlank(schoolName)) {
            return MapMessage.errorMessage("学校名称不能为空");
        }
        MapMessage msg = getRepetitiveName(loadSchoolFullName(schoolName, schoolDistrict), countyCode, schoolPhase);
        if (msg.isSuccess()) {
            schoolInfo.setCmainName(schoolName);
            schoolInfo.setShortName(schoolName);
            schoolInfo.setSchoolDistrict(schoolDistrict);
            if (StringUtils.isNotBlank(schoolName)) {
                if (StringUtils.isNotBlank(schoolDistrict)) {
                    schoolInfo.setSchoolName(schoolName + "(" + schoolDistrict + ")");
                } else {
                    schoolInfo.setSchoolName(schoolName);
                }
            }
            setNewSchool(schoolInfo);
        }
        return msg;
    }

    protected String loadSchoolFullName(String cmainName, String schoolDistrict) {
        if (StringUtils.isBlank(cmainName)) {
            return "";
        }
        return cmainName + (StringUtils.isBlank(schoolDistrict) ? "" : StringUtils.formatMessage("({})", schoolDistrict));
    }

    // 任然使用学校名
    @RequestMapping(value = "continue_use_school_name.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage continueUseSchoolName() {
        String name = getRequestString("name");
        String schoolDistrict = getRequestString("schoolDistrict");
        String schoolName = name + schoolDistrict;
        CrmSchoolClue schoolInfo = getNewSchool();
        schoolInfo.setCmainName(name);
        schoolInfo.setSchoolDistrict(schoolDistrict);
        schoolInfo.setShortName(schoolName);
        setNewSchool(schoolInfo);
        return MapMessage.successMessage();
    }

    // 跳转掉学校照片页
    @RequestMapping(value = "school_photo_page.vpage", method = RequestMethod.GET)
    public String schoolCluePhone(Model model) {
        String returnUrl = getRequestString("returnUrl");
        Long schoolId = getRequestLong("schoolId");
        String type = getRequestString("type");
        CrmSchoolClue schoolClue = getNowSchoolClue(schoolId, type);
        String errorMessage = getRequestString("errorMessage");
        String photoUrl = schoolClue.getPhotoUrl();
        model.addAttribute("schoolId", schoolClue.getSchoolId());
        model.addAttribute("photoUrl", photoUrl);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("type", type);
        model.addAttribute("errorMessage", errorMessage);
        return "rebuildViewDir/mobile/school/school_clue_photo";
    }

    /**
     * photo explain
     *
     * @return photo explain page
     */
    @RequestMapping(value = "photodesc.vpage", method = RequestMethod.GET)
    public String photoDesc() {
        return "rebuildViewDir/mobile/school/school_photo_desc";
    }

    // 保存学校照片的请求
    @RequestMapping(value = "save_school_info_photo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSchoolInfoPhoto(HttpServletRequest request) {
        String url = getRequestString("url");
        String info = getRequestString("info");
        Long schoolId = getRequestLong("schoolId");
        String type = getRequestString("type");
        if (StringUtils.isBlank(url) || StringUtils.isBlank(info)) {
            return MapMessage.errorMessage("图片上传失败");
        }
        Map<String, Object> stringMapMap = JsonUtils.fromJsonToMap(info, String.class, Object.class);
        if (stringMapMap == null) {
            return MapMessage.errorMessage("图片信息读取失败");
        }
        CrmSchoolClue schoolClue = getNowSchoolClue(schoolId, type);
        schoolClue.setPhotoUrl(url);
        if (!stringMapMap.containsKey("Latitude") || !stringMapMap.containsKey("Longitude")) {
            return MapMessage.errorMessage("图片不包含地址位置信息");
        }
        String latitude = ConversionUtils.toString(stringMapMap.get("Latitude"));
        String longitude = ConversionUtils.toString(stringMapMap.get("Longitude"));
        //坑在此处小心
        if (stringMapMap.containsKey("NeedGeoConv") && ConversionUtils.toBool(stringMapMap.get("NeedGeoConv"))) {
            schoolClue.setCoordinateType("wgs84ll");
        } else {
            if (agentRequestSupport.isIOSRequest(request)) {
                schoolClue.setCoordinateType("wgs84ll");
            } else {
                schoolClue.setCoordinateType("autonavi");
            }
        }

        if (stringMapMap.containsKey("DateTime")) {
            schoolClue.setDateTime(ConversionUtils.toString(stringMapMap.get("DateTime")));
        }
        if (stringMapMap.containsKey("Make")) {
            schoolClue.setMake(ConversionUtils.toString(stringMapMap.get("Make")));
        }
        if (stringMapMap.containsKey("Model")) {
            schoolClue.setModel(ConversionUtils.toString(stringMapMap.get("Model")));
        }

        MapMessage msg = AmapMapApi.getAddress(latitude, longitude, schoolClue.getCoordinateType());
        if (!msg.isSuccess()) {
            return msg;
        }
        schoolClue.setAddress(ConversionUtils.toString(msg.get("address")));
        schoolClue.setLongitude(ConversionUtils.toString(msg.get("longitude")));
        schoolClue.setLatitude(ConversionUtils.toString(msg.get("latitude")));
        schoolClue.setCoordinateType("autonavi");
        //添加地址
        setNowSchoolClue(schoolClue, schoolId, type);
        return MapMessage.successMessage();
    }

    //  判断是否有完全相同的学校名称创建失败
    //  判断名称的相似度
    //  无疑似重复创建成功
    //  重复学校推荐
    private MapMessage getRepetitiveName(String schoolName, Integer regionCode, Integer schoolPhase) {
        ExRegion region = raikouSystem.loadRegion(regionCode);
        if (region == null) {
            return MapMessage.errorMessage("您所选的地区出现错误!");
        }

        // 判断同一地区是否有名称完全相同学校
        List<School> sameNameSchoolList = schoolLoaderClient.getSchoolLoader().querySchoolsByRegionCodeAndName(regionCode, schoolName).getUninterruptibly();
        if (CollectionUtils.isNotEmpty(sameNameSchoolList)) {
            MapMessage message = MapMessage.errorMessage();
            message.put("repeatSchool", createSchoolShortInfo(sameNameSchoolList));
            message.put("allSame", true);
            return message;
        }

        // 判断是否有疑似重名学校
        SchoolLevel level = SchoolLevel.safeParse(schoolPhase);
        List<Integer> countyCodes = agentRegionService.getCountyCodes(region.getCityCode());
        List<School> schools = loadAreaSchools(countyCodes, level);
        Map<Long, School> schoolMap = schools.stream().collect(Collectors.toMap(School::getId, Function.identity()));
        Map<String, Long> schoolNameMap = schools.stream().collect(Collectors.toMap(School::loadSchoolFullName, School::getId, (o1, o2) -> o1));
        if (MapUtils.isEmpty(schoolNameMap)) {
            // 当前区域下没有学校
            return MapMessage.successMessage();
        }
        Map<String, Double> similarityMap = crmSimilarSchoolServiceClient.getSchoolNameSimilarity(schoolName, schoolNameMap.keySet(), region.getCityName(), SchoolLevel.safeParse(schoolPhase), SIMILAR_LIMIT, CrmSimilarSchoolServiceClient.SIMILAR_VALUE);
        if (MapUtils.isEmpty(similarityMap)) {
            // 接口没有返回相同没成的学校
            return MapMessage.successMessage();
        }
        List<SchoolShortInfo> sameSchoolList = new ArrayList<>();
        MapMessage msg = MapMessage.errorMessage();
        for (String similarityName : similarityMap.keySet()) {
            Double similarity = similarityMap.get(similarityName);
            Long sameSchoolId = schoolNameMap.get(similarityName);
            School school = schoolMap.get(sameSchoolId);
            if (school == null) {
                continue;
            }
            if (Objects.equals(1.0, similarity)) {
                msg.put("repeatSchool", createSchoolShortInfo(Collections.singletonList(school)));
                msg.put("allSame", false);
                return msg;
            } else {
                sameSchoolList.addAll(createSchoolShortInfo(Collections.singletonList(school)));
            }
        }
        if (SchoolLevel.INFANT == level) {
            return MapMessage.successMessage();
        }
        msg.put("repeatSchool", sameSchoolList);
        msg.put("allSame", false);
        return msg;
    }

    // 分校列表
    @RequestMapping(value = "branch_school_list.vpage", method = RequestMethod.GET)
    public String branchSchoolList(Model model) {
        Long schoolId = getRequestLong("schoolId");
        CrmSchoolClue clue = getNowSchoolClue(schoolId);
        model.addAttribute("schoolId", clue.getSchoolId());      // 新建学校时学校的ID为空
        return "rebuildViewDir/mobile/school/branch_school_list";
    }

    // 添加分校
    @RequestMapping(value = "add_branch_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addBranchSchool() {
        try {
            Long schoolId = getRequestLong("schoolId");
            Long branchSchoolId = getRequestLong("branchSchoolId");
            CrmSchoolClue schoolClue = getNowSchoolClue(schoolId);
            if (CollectionUtils.isEmpty(schoolClue.getBranchSchoolIds())) {
                Set<Long> branchSchoolIds = new HashSet<>();
                branchSchoolIds.add(branchSchoolId);
                schoolClue.setBranchSchoolIds(branchSchoolIds);
            } else {
                schoolClue.getBranchSchoolIds().add(branchSchoolId);
            }
            setNowSchoolClue(schoolClue, schoolId);
        } catch (Exception ex) {
            logger.error("schoolClue Add branch school failed", ex);
            return MapMessage.errorMessage("分校添加失败");
        }
        return MapMessage.successMessage();
    }

    // 删除分校
    @RequestMapping(value = "drop_branch_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage dropBranchSchool() {
        try {
            Long schoolId = getRequestLong("schoolId");
            Long branchSchoolId = getRequestLong("branchSchoolId");
            CrmSchoolClue schoolClue = getNowSchoolClue(schoolId);
            if (CollectionUtils.isEmpty(schoolClue.getBranchSchoolIds())) {
                return MapMessage.successMessage();
            }
            Set<Long> branchSchoolIds = schoolClue.getBranchSchoolIds();
            if (branchSchoolIds.remove(branchSchoolId)) {
                schoolClue.setBranchSchoolIds(branchSchoolIds);
            }
            setNowSchoolClue(schoolClue, schoolId);
        } catch (Exception ex) {
            logger.error("schoolClue remove branch school failed", ex);
            return MapMessage.errorMessage("删除分校失败");
        }
        return MapMessage.successMessage("删除分校成功");
    }

    // 根据key查询分校信息列表
    @RequestMapping(value = "find_repeat_branch_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findRepeatSchool() {
        String schoolKey = getRequestString("schoolKey");
        Long schoolId = getRequestLong("schoolId");
        try {
            MapMessage map = new MapMessage();
            CrmSchoolClue schoolClue = getNowSchoolClue(schoolId);
            Integer regions = schoolClue.getCountyCode();
            List<SchoolShortInfo> schoolShortInfos = getSchoolShortInfoBySchoolKey(regions, schoolKey);
            if (CollectionUtils.isEmpty(schoolShortInfos)) {
                return MapMessage.errorMessage("未找到对应的学校");
            }
            map.put("schoolShortInfos", schoolShortInfos);
            map.setSuccess(true);
            return map;
        } catch (Exception ex) {
            logger.error(String.format("can~t find branch school schoolKey=%s", schoolKey), ex);
            return MapMessage.errorMessage("未找到对应的学校");
        }
    }

    // 点击分校查询分校的详细信息
    @RequestMapping(value = "find_branch_school.vpage", method = RequestMethod.GET)
    public String findBranchSchool(Model model) {
        Long schoolId = requestLong("branchSchoolId");
        Long schoolClueId = getRequestLong("schoolId");
        CrmSchoolClue clue = getNowSchoolClue(schoolClueId);
        SchoolShortInfo schoolInfo = new SchoolShortInfo();
        School school = raikouSystem.loadSchoolIncludeDisabled(schoolId);
        if (school != null) {
            schoolInfo.setSchoolId(school.getId());
            schoolInfo.setSchoolName(school.getCname());
            ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
            if (region != null) {
                schoolInfo.setRegionName(region.getCityName() + region.getCountyName());
            }
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo != null) {
            EduSystemType eduSystemType = schoolExtInfo.fetchEduSystem();
            if (eduSystemType == null && school != null) {
                eduSystemType = EduSystemType.of(school.getDefaultEduSystemType());
            }
            schoolInfo.setGradeDistribution(eduSystemType != null ? eduSystemType.getCandidateClazzLevel() : "");
        }
        model.addAttribute("schoolInfo", schoolInfo);
        model.addAttribute("schoolId", clue.getSchoolId());
        return "rebuildViewDir/mobile/school/branch_schoolId_info";
    }

    // 用户学校线索
    @RequestMapping(value = "user_clues.vpage")
    public String userClues(Model model) {
        Long userId = getCurrentUserId();
        Map<String, List<CrmSchoolClue>> schoolCluesList = schoolClueService.userSchoolClues(userId);
        model.addAttribute("schoolClues", schoolCluesList);
        return "rebuildViewDir/mobile/school/school_clues";
    }

    @RequestMapping(value = "school_clue_detail.vpage", method = RequestMethod.GET)
    public String schoolClueDetail(Model model) {
        Long userId = getCurrentUserId();
        String clueId = getRequestString("id");
        CrmSchoolClue schoolClue = schoolClueService.load(clueId);
        model.addAttribute("schoolClue", schoolClue);
        return "rebuildViewDir/mobile/school/school_clue_detail";
    }


    // 获得当时的学校线索
    private CrmSchoolClue getNowSchoolClue(Long schoolId) {
        return getNowSchoolClue(schoolId, null);
    }

    // 获得当时的学校线索
    private CrmSchoolClue getNowSchoolClue(Long schoolId, String type) {
        if (StringUtils.isBlank(type)) {
            if (schoolId == 0) {
                return getNewSchool();
            } else {
                return getUpdateSchool();
            }
        } else if (Objects.equals(APPRAISAL_SCHOOL, type)) {
            return getAppraisalSchool();
        }
        return new CrmSchoolClue();
    }

    private void setNowSchoolClue(CrmSchoolClue clue, Long schoolId) {
        setNowSchoolClue(clue, schoolId, null);
    }

    // 为当时的学校线索赋值
    private void setNowSchoolClue(CrmSchoolClue clue, Long schoolId, String type) {
        if (StringUtils.isBlank(type)) {
            if (Objects.equals(schoolId, 0L)) {
                setNewSchool(clue);
            } else {
                setUpdateSchool(clue);
            }
        } else if (Objects.equals(APPRAISAL_SCHOOL, type)) {
            setAppraisalSchool(clue);
        }
    }

    private List<Map<String, String>> createBranchSchoolInfo(Collection<School> branchSchools) {
        List<Map<String, String>> branchSchoolInfos = new ArrayList<>();
        branchSchools.forEach(p -> {
            Map<String, String> branchSchool = new HashMap<>();
            branchSchool.put("schoolId", SafeConverter.toString(p.getId()));
            branchSchool.put("schoolName", p.getShortName());
            branchSchoolInfos.add(branchSchool);
        });
        return branchSchoolInfos;
    }

    private List<School> loadAreaSchools(Collection<Integer> regionCodes, SchoolLevel schoolLevel) {
        Set<Integer> codes = CollectionUtils.toLinkedHashSet(regionCodes);
        if (codes.isEmpty()) {
            return Collections.emptyList();
        }
        return schoolLoaderClient.getSchoolLoader()
                .loadSchools(AlpsFutureBuilder.<Integer, Set<School.Location>>newBuilder()
                        .ids(codes)
                        .generator(code -> schoolLoaderClient.getSchoolLoader().querySchoolLocations(code))
                        .buildList()
                        .regularize()
                        .stream()
                        .flatMap(Set::stream)
                        .filter(e -> !e.isDisabled())
                        .filter(e -> e.match(AuthenticationState.SUCCESS) || e.match(AuthenticationState.WAITING))
                        .filter(e -> e.match(schoolLevel))
                        .filter(e -> !e.match(SchoolType.TRAINING) && !e.match(SchoolType.CONFIDENTIAL))
                        .map(School.Location::getId)
                        .collect(Collectors.toSet()))
                .getUninterruptibly()
                .values()
                .stream()
                .sorted(Comparator.comparing(LongIdEntity::getId))
                .collect(Collectors.toList());
    }

    private CrmSchoolClue getAppraisalSchool() {
        return getSchool(APPRAISAL_SCHOOL);
    }

    private void setAppraisalSchool(CrmSchoolClue attrValue) {
        setSchool(APPRAISAL_SCHOOL, attrValue);
    }

    private void cleanAppraisalSchool() {
        setSchool(APPRAISAL_SCHOOL, new CrmSchoolClue());
    }

    private CrmSchoolClue getNewSchool() {
        return getSchool(NEW_SCHOOL);
    }

    private void setNewSchool(CrmSchoolClue attrValue) {
        setSchool(NEW_SCHOOL, attrValue);
    }

    private void cleanNewSchool() {
        setSchool(NEW_SCHOOL, new CrmSchoolClue());
    }

    private CrmSchoolClue getUpdateSchool() {
        return getSchool(UPDATE_SCHOOL);
    }

    private void setUpdateSchool(CrmSchoolClue attrValue) {
        setSchool(UPDATE_SCHOOL, attrValue);
    }

    private void cleanUpdateSchool() {
        setSchool(UPDATE_SCHOOL, new CrmSchoolClue());
    }

    private CrmSchoolClue getSchool(String key) {
        Long userId = getCurrentUserId();
        Object obj = agentCacheSystem.getUserSessionAttribte(userId, key);
        if (obj == null) return new CrmSchoolClue();
        if (obj instanceof CrmSchoolClue) return (CrmSchoolClue) obj;
        return new CrmSchoolClue();
    }

    private void setSchool(String key, CrmSchoolClue attrValue) {
        Long userId = getCurrentUserId();
        agentCacheSystem.addUserSessionAttribte(userId, key, attrValue);
    }


    private List<SchoolShortInfo> createSchoolShortInfo(List<School> schools) {
        if (CollectionUtils.isEmpty(schools)) {
            return Collections.emptyList();
        }
        List<SchoolShortInfo> schoolShortList = new ArrayList<>();
        schools.forEach(p -> {
            SchoolShortInfo info = new SchoolShortInfo();
            info.setSchoolId(p.getId());
            info.setSchoolName(p.getCname());
            ExRegion schoolRegion = raikouSystem.loadRegion(p.getRegionCode());
            info.setRegionName(StringUtils.join(schoolRegion.getCityName(), schoolRegion.getCountyName()));
            schoolShortList.add(info);
        });
        return schoolShortList;
    }

    private Boolean checkTodayDayNeedLocked(Long schoolId) {
        if (schoolId == null) {
            return true;
        }
        Calendar today = Calendar.getInstance();
        Integer day = today.get(Calendar.DAY_OF_MONTH);
        return baseDictService.isDictSchool(schoolId) && day >= LOCKED_DAY;
    }

    private List<Map<String, Object>> createSchoolLength(EduSystemType schoolEduSystemType) {
        List<Map<String, Object>> eduSystemTypes = new ArrayList<>();
        for (EduSystemType eduSystemType : EduSystemType.values()) {
            if (eduSystemType == EduSystemType.I4) {
                continue;
            }

            Map<String, Object> eduSystemTypeInfo = new HashMap<>();
            eduSystemTypeInfo.put("name", eduSystemType.getDescription());
            eduSystemTypeInfo.put("code", eduSystemType.name());
            if (Objects.equals(schoolEduSystemType, eduSystemType)) {
                eduSystemTypeInfo.put("selected", true);
            }
            eduSystemTypeInfo.put("group", eduSystemType.getKtwelve().name());
            eduSystemTypes.add(eduSystemTypeInfo);
        }
        return eduSystemTypes;
    }

    // 解析学校json
    private MapMessage initSchoolBasicByJson(String schoolBasicInfo, CrmSchoolClue clue, boolean isNewSchool) {
        Map<String, Object> basicInfo = JsonUtils.fromJson(schoolBasicInfo);
        if (basicInfo == null) {
            return MapMessage.errorMessage("学校基本信息错误..");
        }
        if (isNewSchool) {
            Integer phase = SafeConverter.toInt(basicInfo.get("phase"));
            if (SchoolLevel.safeParse(phase, null) != null) {
                if (Objects.equals(phase, 0) || !Objects.equals(phase, clue.getSchoolPhase())) {
                    clearSchoolInfo(clue);
                }
                clue.setSchoolPhase(SafeConverter.toInt(basicInfo.get("phase")));
            } else {
                return MapMessage.errorMessage("学校阶段填写错误");
            }
        } else {
            Long schoolId = SafeConverter.toLong(basicInfo.get("schoolId"));
            if (schoolId > 0) {
                clue.setSchoolId(schoolId);
            }
        }
        String eduSystem = SafeConverter.toString(basicInfo.get("eduSystem"));
        if (StringUtils.isNoneBlank(eduSystem)) {
            clue.setEduSystem(eduSystem);
        }
        Integer englishStartGrade = SafeConverter.toInt(basicInfo.get("englishStartGrade"));
        if (englishStartGrade >= ClazzLevel.FIRST_GRADE.getLevel() && englishStartGrade <= ClazzLevel.SIXTH_GRADE.getLevel()) {
            clue.setEnglishStartGrade(englishStartGrade);
        }
        //走读方式
        Integer externOrBoarder = SafeConverter.toInt(basicInfo.get("externOrBoarder"));
        if (externOrBoarder > 0) {
            clue.setExternOrBoarder(externOrBoarder);
        }

//        Map<Integer, Map<String, Object>> gradeInfo = formatGradeInfo((List<Map<String, Object>>) basicInfo.get("gradeInfo"));
//        initStuGrade(gradeInfo, clue);
        return MapMessage.successMessage().add("confirm", basicInfo.get("confirm"));
    }

    private MapMessage initSchoolExtInfo(String schoolBasicInfo, CrmSchoolClue clue) {
        Map<String, Object> basicInfo = JsonUtils.fromJson(schoolBasicInfo);
        Long schoolId = SafeConverter.toLong(basicInfo.get("schoolId"));
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校信息未找到");
        }
        clue.setSchoolId(schoolId);
        clue.setSchoolPhase(school.getLevel());
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        String eduSystem = SafeConverter.toString(basicInfo.get("eduSystem"));
        if (StringUtils.isNoneBlank(eduSystem)) {
            clue.setEduSystem(eduSystem);
        } else if (schoolExtInfo != null) {
            clue.setEduSystem(schoolExtInfo.getEduSystem());
        }
        if (school.getLevel() == SchoolLevel.INFANT.getLevel()) {
            clue.setEduSystem(EduSystemType.I4.name());
        }
        Integer englishStartGrade = SafeConverter.toInt(basicInfo.get("englishStartGrade"));
        if (englishStartGrade >= ClazzLevel.FIRST_GRADE.getLevel() && englishStartGrade <= ClazzLevel.SIXTH_GRADE.getLevel()) {
            clue.setEnglishStartGrade(englishStartGrade);
        } else if (schoolExtInfo != null) {
            clue.setEnglishStartGrade(schoolExtInfo.getEnglishStartGrade());
        }
//        Map<Integer, Map<String, Object>> gradeInfo = formatGradeInfo((List<Map<String, Object>>) basicInfo.get("gradeInfo"));
//        initStuGrade(gradeInfo, clue);
//        if (schoolExtInfo != null) {
//            CrmSchoolClue.mergerSchoolExtInfoGradInfo(clue, schoolExtInfo);
//        }
        return MapMessage.successMessage();
    }

    /**
     * 高中的年级要-1
     *
     * @param gradeInfo 前端取到的值
     * @param clue      初始化属性
     */
    private void initStuGrade(Map<Integer, Map<String, Object>> gradeInfo, CrmSchoolClue clue) {
        synchronized (clue) {
            EduSystemType eduSystemType = clue.fetchEduSystem();
            if (Objects.equals(clue.getSchoolPhase(), SchoolLevel.INFANT.getLevel())) {
                initInfantGrade(gradeInfo.get(ClazzLevel.INFANT_FIRST.getLevel()), clue, ClazzLevel.INFANT_FIRST.getLevel());
                initInfantGrade(gradeInfo.get(ClazzLevel.INFANT_SECOND.getLevel()), clue, ClazzLevel.INFANT_SECOND.getLevel());
                initInfantGrade(gradeInfo.get(ClazzLevel.INFANT_THIRD.getLevel()), clue, ClazzLevel.INFANT_THIRD.getLevel());
                initInfantGrade(gradeInfo.get(ClazzLevel.INFANT_FOURTH.getLevel()), clue, ClazzLevel.INFANT_FOURTH.getLevel());
            }
            if (eduSystemType == null) {
                return;
            }
            if (EduSystemType.P5 == eduSystemType || EduSystemType.P6 == eduSystemType) {
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.FIRST_GRADE.getLevel()), clue, ClazzLevel.FIRST_GRADE.getLevel());
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.SECOND_GRADE.getLevel()), clue, ClazzLevel.SECOND_GRADE.getLevel());
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.THIRD_GRADE.getLevel()), clue, ClazzLevel.THIRD_GRADE.getLevel());
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.FOURTH_GRADE.getLevel()), clue, ClazzLevel.FOURTH_GRADE.getLevel());
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.FIFTH_GRADE.getLevel()), clue, ClazzLevel.FIFTH_GRADE.getLevel());
            }
            if (EduSystemType.P6 == eduSystemType || EduSystemType.J4 == eduSystemType) {
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.SIXTH_GRADE.getLevel()), clue, ClazzLevel.SIXTH_GRADE.getLevel());
            }
            if (EduSystemType.J4 == eduSystemType || EduSystemType.J3 == eduSystemType) {
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.SEVENTH_GRADE.getLevel()), clue, ClazzLevel.SEVENTH_GRADE.getLevel());
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.EIGHTH_GRADE.getLevel()), clue, ClazzLevel.EIGHTH_GRADE.getLevel());
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.NINTH_GRADE.getLevel()), clue, ClazzLevel.NINTH_GRADE.getLevel());
            }
            if (EduSystemType.S4 == eduSystemType) {
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.NINTH_GRADE.getLevel()), clue, ClazzLevel.NINTH_GRADE.getLevel());
            }
            if (EduSystemType.S3 == eduSystemType || EduSystemType.S4 == eduSystemType) {
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.SENIOR_ONE.getLevel()), clue, ClazzLevel.SENIOR_ONE.getLevel());
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.SENIOR_TWO.getLevel()), clue, ClazzLevel.SENIOR_TWO.getLevel());
                initStuCountAndGradeCount(gradeInfo.get(ClazzLevel.SENIOR_THREE.getLevel()), clue, ClazzLevel.SENIOR_THREE.getLevel());
            }
        }
    }

    private void initInfantGrade(Map<String, Object> gradeInfo, CrmSchoolClue clue, Integer gradeLevel) {
        if (clue.getInfantGrade() == null) {
            clue.setInfantGrade(new ArrayList<>());
        }
        if (gradeInfo == null) {
            gradeInfo = new HashMap<>();
        }
        gradeInfo.put("gradeLevel", gradeLevel);

        Map<Integer, Map<String, Object>> gradeInfoMap = clue.getInfantGrade().stream().collect(Collectors.toMap(p -> SafeConverter.toInt(p.get("gradeLevel")), Function.identity()));
        if (gradeInfoMap.containsKey(gradeLevel)) {
            Map<String, Object> oneGradeInfo = gradeInfoMap.get(gradeLevel);
            clue.getInfantGrade().remove(oneGradeInfo);
        }
        clue.getInfantGrade().add(gradeInfo);
    }

    private void initStuCountAndGradeCount(Map<String, Object> gradeInfo, CrmSchoolClue clue, Integer gradeLevel) {
        if (gradeInfo == null) {
            return;
        }
        try {
            Object studentCount = gradeInfo.get("studentCount");
            Object classCount = gradeInfo.get("classCount");
            if (Objects.equals(SafeConverter.toString(studentCount), "")) {
                studentCount = null;
            }
            if (Objects.equals(SafeConverter.toString(classCount), "")) {
                classCount = null;
            }
            Field stuCount = clue.getClass().getDeclaredField(StringUtils.formatMessage("grade{}StudentCount", gradeLevel));
            stuCount.setAccessible(true);
            stuCount.set(clue, studentCount == null ? null : SafeConverter.toInt(studentCount));
            Field clsCount = clue.getClass().getDeclaredField(StringUtils.formatMessage("newGrade{}ClassCount", gradeLevel));
            clsCount.setAccessible(true);
            clsCount.set(clue, classCount == null ? null : SafeConverter.toInt(classCount));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, Map<String, Object>> formatGradeInfo(List<Map<String, Object>> gradeInfoList) {
        if (CollectionUtils.isEmpty(gradeInfoList)) {
            return Collections.emptyMap();
        }
        Map<Integer, Map<String, Object>> result = new HashMap<>();
        gradeInfoList.forEach(p -> {
            if (p.get("level") == null) {
                return;
            }
            Map<String, Object> gradeInfo = new HashMap<>();
            Integer level = SafeConverter.toInt(p.get("level"));
            gradeInfo.put("gradeLevel", level);
            gradeInfo.put("studentCount", p.get("allMan"));
            gradeInfo.put("classCount", p.get("banClass"));
            result.put(level, gradeInfo);
        });
        return result;
    }

    private List<SchoolShortInfo> getSchoolShortInfoBySchoolKey(Integer regionCode, String schoolKey) {
        if (regionCode == null) {
            return new ArrayList<>();
        }
        long schoolId = SafeConverter.toLong(schoolKey);
        ExRegion region = raikouSystem.loadRegion(regionCode);
        List<Integer> countyCodes = agentRegionService.getCountyCodes(region.getCityCode());
        List<School> schools = loadAreaSchools(countyCodes);
        if (schoolId > 0) {
            schools = schools.stream().filter(p -> p.getId().equals(schoolId)).collect(Collectors.toList());
        } else {
            schools = schools.stream().filter(p -> p.getCname().contains(schoolKey)).collect(Collectors.toList());
        }
        return createSchoolShortInfo(schools);
    }

    private List<School> loadAreaSchools(Collection<Integer> regionCodes) {
        Set<Integer> codes = CollectionUtils.toLinkedHashSet(regionCodes);
        if (codes.isEmpty()) {
            return Collections.emptyList();
        }
        return schoolLoaderClient.getSchoolLoader()
                .loadSchools(AlpsFutureBuilder.<Integer, Set<School.Location>>newBuilder()
                        .ids(codes)
                        .generator(code -> schoolLoaderClient.getSchoolLoader().querySchoolLocations(code))
                        .buildList()
                        .regularize()
                        .stream()
                        .flatMap(Set::stream)
                        .filter(e -> !e.isDisabled())
                        .filter(e -> e.match(AuthenticationState.WAITING) || e.match(AuthenticationState.SUCCESS))
                        .filter(e -> !e.match(SchoolType.TRAINING) && !e.match(SchoolType.CONFIDENTIAL))
                        .map(School.Location::getId)
                        .collect(Collectors.toSet()))
                .getUninterruptibly()
                .values()
                .stream()
                .sorted(Comparator.comparing(LongIdEntity::getId))
                .collect(Collectors.toList());
    }

    @RequestMapping("dict_school_apply.vpage")
    public String modifyDictSchoolApplyPage(Model model) {
        Long schoolId = getRequestLong("schoolId");
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isCityManager()) {
            List<AgentUser> userList = baseOrgService.getManagedGroupUsers(currentUser.getUserId(), false);
            model.addAttribute("bdUserList", userList);
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        Integer schoolLevel = school == null ? SchoolLevel.JUNIOR.getLevel() : school.getLevel();
        model.addAttribute("schoolLevel", schoolLevel);
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();

        if (schoolExtInfo != null) {
            if (schoolResourceService.checkGradeBasicDataIsComplete(schoolId)) {
                model.addAttribute("needCompleteExtInfo", false);
            } else {
                model.addAttribute("needCompleteExtInfo", true);
                model.addAttribute("needEduSystem", schoolExtInfo.getEduSystem() == null);
                model.addAttribute("needEnglishStartGrade", schoolExtInfo.getEnglishStartGrade() == null);
                model.addAttribute("needGradeInfo", true);
                model.addAttribute("gradeDataList", schoolResourceService.generateGradeBasicDataList(schoolId));
            }
        } else {
            model.addAttribute("needCompleteExtInfo", true);
            model.addAttribute("needEduSystem", true);
            model.addAttribute("needEnglishStartGrade", true);
            model.addAttribute("needGradeInfo", true);
        }

        model.addAttribute("schoolId", schoolId);
        return "rebuildViewDir/mobile/school/dict_school_apply";
    }
}
