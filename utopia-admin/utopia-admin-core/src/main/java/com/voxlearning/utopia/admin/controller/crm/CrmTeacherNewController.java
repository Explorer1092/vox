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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.common.UpdateOption;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.entity.CrmTeacherTransferSchoolRecord;
import com.voxlearning.utopia.admin.service.crm.*;
import com.voxlearning.utopia.admin.support.SessionUtils;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.entity.TeacherRoles;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralHistory;
import com.voxlearning.utopia.entity.ucenter.CertificationApplicationOperatingLog;
import com.voxlearning.utopia.entity.ucenter.MentorHistory;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorCompetition;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskLoaderClient;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.client.CampaignLoaderClient;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.certification.client.CertificationServiceClient;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.crm.api.constants.crm.CrmClueType;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrgLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.TeacherAgentLoaderClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.Integral;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.mentor.client.MentorServiceClient;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamReportForClazz;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamReportForStudent;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamReportLoaderClient;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.mapper.RewardOrderMapper;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.constants.*;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.*;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.api.mappers.TeacherSummaryQuery;
import com.voxlearning.utopia.service.user.client.*;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.*;
import org.bson.types.ObjectId;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.Collator;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Alex
 * @version 0.1
 * @since 2015/7/3
 */
@Controller
@RequestMapping("/crm/teachernew")
public class CrmTeacherNewController extends CrmAbstractController {

    private static final Integer pageNum = 10;
    private static final Integer MAX_QUERY_COUNT = 500;
    private static final String SESSION_ESQUERY_STRING_PREFIX = "sessgion_es_query_";

    private static List<IntegralType> UNSUPPORTED_INTEGRAL_TYPE = new ArrayList<>();
    private static List<Integer> KEY_CITY_CODE_COLLECTION = new ArrayList<>();

    static {
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.学生补作作业);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.学生完成作业);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.每个学生完成作业老师获得积分);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.学生完成假期作业);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.成功邀请其他老师);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.PK周冠军);
        // 初始化重点城市信息
        KEY_CITY_CODE_COLLECTION.addAll(KeyCity.toMap().keySet());
    }

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    // Inject In Alphabetical Order
    @Inject private AgentOrgLoaderClient agentOrgLoaderClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;

    @Inject private CampaignLoaderClient campaignLoaderClient;
    @Inject private CampaignServiceClient campaignServiceClient;
    @Inject private CertificationServiceClient certificationServiceClient;
    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject private CrmTaskService crmTaskService;
    @Inject private CrmTeacherFakeService crmTeacherFakeService;
    @Inject private CrmTeacherSummaryService crmTeacherSummaryService;
    @Inject private CrmTeacherTransferService crmTeacherTransferService;

    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private GroupLoaderClient groupLoaderClient;

    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;

    @Inject private MentorServiceClient mentorServiceClient;
    @Inject private MiscServiceClient miscServiceClient;

    @Inject private NewExamReportLoaderClient newExamReportLoaderClient;
    @Inject private NewExamServiceClient newExamServiceClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;

    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Inject private TeacherAgentLoaderClient teacherAgentLoaderClient;

    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserManagementClient userManagementClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private SchoolServiceClient schoolServiceClient;
    @Inject private IntegralLoaderClient integralLoaderClient;
    @Inject private UserIntegralServiceClient userIntegralServiceClient;
    @Inject private UserAuthQueryServiceClient userAuthQueryServiceClient;
    @Inject private VendorServiceClient serviceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private TeacherSummaryEsServiceClient teacherSummaryEsServiceClient;
    @Inject private TeacherRolesServiceClient teacherRolesServiceClient;
    @Inject private TeacherTaskLoaderClient teacherTaskLoaderClient;
    @Inject private TeacherLevelServiceClient teacherLevelServiceClient;
    @Inject private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject private CrmRewardService crmRewardService;

    @ImportService(interfaceClass = CrmSummaryService.class)
    private CrmSummaryService crmSummaryService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        //地区列表
        List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadProvinces();
        List<Map<String, Object>> provinceList = new ArrayList<>();
        for (ExRegion ex : regionList) {
            Map<String, Object> province = new HashMap<>();
            province.put("key", ex.getCode());
            province.put("value", ex.getName());
            provinceList.add(province);
        }
        //TODO 还有一些页面需要参数是在页面写死的。还是得改来从这里传出去
        //推荐查询类型
        model.addAttribute("unusualStatus", CrmTeacherUnusualStatusType.toValidMap());
        model.addAttribute("unusualStatusNames", JsonUtils.toJson(CrmTeacherUnusualStatusType.toValidMap().keySet()));
        //查询条件区域
        model.addAttribute("provinceList", provinceList);
        model.addAttribute("totalCount", 0);
        return "crm/teachernew/index";
    }

    @RequestMapping(value = "newresetscore.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage newResetScore() {
        String param = this.getRequestString("param");
        if (StringUtils.isBlank(param)) {
            return MapMessage.errorMessage("param is blank");
        }
        return newExamServiceClient.newResetScore(param);
    }


    @RequestMapping(value = "jobCorrect.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage jobCorrect() {
        String newExamId = this.getRequestString("newExamId");
        String questionId = this.getRequestString("questionId");
        String paperDocId = this.getRequestString("paperDocId");
        String studentIdsStr = this.getRequestString("studentIds");
        List<Long> userIds = StringUtils.toLongList(studentIdsStr);
        if (StringUtils.isAnyBlank(newExamId, questionId, paperDocId)) {
            return MapMessage.errorMessage("参数错误");
        }
        return newExamServiceClient.resetOralQuestionScoreV2(newExamId, questionId, paperDocId, userIds);

    }


    @RequestMapping(value = "newstudentexam.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage newStudentExam() {
        String studentId = getRequestString("studentId");
        String newExamId = getRequestString("newExamId");

        if (StringUtils.isBlank(studentId) || StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("参数为空");
        }

        Long sid = SafeConverter.toLong(studentId);

        return newExamServiceClient.handlerStudentExaminationAuthority(sid, newExamId);
    }

    @RequestMapping(value = "submitexam.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage crmSubmitNewExam() {
        String studentId = getRequestString("studentId");
        String newExamId = getRequestString("newExamId");

        if (StringUtils.isBlank(studentId) || StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("参数为空");
        }

        Long sid = SafeConverter.toLong(studentId);

        return newExamServiceClient.crmSubmitNewExam(newExamId, sid);
    }


    @RequestMapping(value = "newexamclazz.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String newExamClazz(Model model) {
        String path = "crm/teachernew/newexamclazz";
        Long teacherId = this.getRequestLong("teacherId");
        if (teacherId == 0L) {
            model.addAttribute("success", false);
            model.addAttribute("desc", "teacherId is error");
            return path;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            model.addAttribute("desc", "teacher is null");
            return path;
        }
        model.addAttribute("teacherId", teacherId);

        MapMessage mapMessage = newExamServiceClient.loadTeacherClazzListNew(Collections.singleton(teacher.getId()));

        model.addAttribute("success", mapMessage.isSuccess());
        if (mapMessage.isSuccess()) {
            model.addAttribute("clazzLevels", mapMessage.get("clazzList"));
        }
        return path;
    }

    @RequestMapping(value = "newexaminformation.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String newExamInformation(Model model) {
        String path = "crm/teachernew/newexaminformation";
        Long teacherId = this.getRequestLong("teacherId");
        if (teacherId == 0L) {
            model.addAttribute("success", false);
            model.addAttribute("desc", "teacherId is error");
            return path;
        }
        long groupId = getRequestLong("groupId");
        if (groupId == 0L) {
            model.addAttribute("success", false);
            model.addAttribute("desc", "groupId is error");
            return path;
        }

        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            model.addAttribute("desc", "teacher is null");
            return path;
        }
        Long clazzId = this.getRequestLong("clazzId");
        if (clazzId == 0L) {
            model.addAttribute("success", false);
            model.addAttribute("desc", "clazzId is error");
            return path;
        }
        MapMessage mapMessage = newExamReportLoaderClient.crmUnifyExamList(teacherId, clazzId, teacher.getSubject(), groupId);

        if (mapMessage.isSuccess()) {
            model.addAttribute("teacherId", teacherId);
            model.addAttribute("clazzId", clazzId);
            model.addAttribute("success", true);
            model.addAttribute("list", mapMessage.get("list"));
            model.addAttribute("appExamList", mapMessage.get("appExamList"));
            if (mapMessage.containsKey("results")) {
                model.addAttribute("results", mapMessage.get("results"));
            }

        } else {
            model.addAttribute("success", false);
            model.addAttribute("desc", mapMessage.getErrorCode());
            return path;
        }
        return path;
    }

    @RequestMapping(value = "outsidereadingclazz.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String outsideReadingClazz(Model model) {
        String path = "crm/teachernew/outsidereadingclazz";
        long teacherId = this.getRequestLong("teacherId");
        if (teacherId == 0L) {
            model.addAttribute("success", false);
            model.addAttribute("desc", "teacherId is error");
            return path;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            model.addAttribute("desc", "teacher is null");
            return path;
        }
        model.addAttribute("teacherId", teacherId);

        MapMessage mapMessage = newExamServiceClient.loadTeacherClazzListNew(Collections.singleton(teacher.getId()));

        model.addAttribute("success", mapMessage.isSuccess());
        if (mapMessage.isSuccess()) {
            model.addAttribute("clazzLevels", mapMessage.get("clazzList"));
        }
        return path;
    }

    @RequestMapping(value = "outsidereadinglist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String outsideReadingList(Model model) {
        String path = "crm/teachernew/outsidereadinglist";
        long groupId = getRequestLong("groupId");
        if (groupId == 0L) {
            model.addAttribute("success", false);
            model.addAttribute("desc", "groupId is error");
            return path;
        }
        MapMessage mapMessage = outsideReadingLoaderClient.crmLoadOutsideReadingsByGroupId(groupId);
        if (mapMessage.isSuccess()) {
            model.addAttribute("groupId", groupId);
            model.addAttribute("list", mapMessage.get("list"));
            model.addAttribute("success", true);
        } else {
            model.addAttribute("success", false);
            model.addAttribute("desc", "查询失败");
        }
        return path;
    }

    @RequestMapping(value = "deleteoutsidereading.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage outsideReadingDelete() {
        String readingId = getRequestString("readingId");
        return outsideReadingServiceClient.crmDeleteOutsideReading(readingId);
    }

    // 老师查询处理
    @RequestMapping(value = "teacherlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String followingIndex(Model model) {
        Map<String, Object> inputConditionMap = new HashMap<>();
        //老师异常状态-全部
        inputConditionMap.put("all", getRequestBool("all"));
        TeacherSummaryQuery query = new TeacherSummaryQuery();
        //老师异常状态
        for (String typeName : CrmTeacherUnusualStatusType.toMap().keySet()) {
            boolean type = getRequestBool(typeName);
            inputConditionMap.put(typeName, type);
        }
        // 老师学号
        Long teacherId = getRequestLong("teacherId");
        if (teacherId > 0) {
            inputConditionMap.put("teacherId", teacherId);
            query.setIds(Collections.singletonList(teacherId));
        }
        //学校类别
        SchoolLevel schoolLevel = SchoolLevel.safeParse(getRequestInt("schoolLevel"), null);
        inputConditionMap.put("schoolLevel", schoolLevel);
        if (schoolLevel != null) {
            query.setSchoolLevels(Collections.singletonList(schoolLevel.name()));
        }

        // 老师姓名
        String teacherName = getRequestString("teacherName");
        inputConditionMap.put("teacherName", teacherName);
        if (StringUtils.isNotBlank(teacherName)) {
            query.setTeacherName(teacherName);
        }

        // 老师手机
        String teacherMobile = getRequestParameter("teacherMobile", "").replaceAll("\\s", "").replaceAll("-", "");
        inputConditionMap.put("teacherMobile", teacherMobile);
        if (StringUtils.isNotBlank(teacherMobile)) {
            query.setMobile(teacherMobile);
        }

        // 注册时间
        String regTimeStart = getRequestString("regTimeStart");
        String regTimeEnd = getRequestString("regTimeEnd");
        if (StringUtils.isNotBlank(regTimeStart)) {
            query.setRegStartTime(DateUtils.dateToString(DateUtils.stringToDate(regTimeStart, "yyyy-MM-dd"), "yyyyMMddHHmmss"));
            inputConditionMap.put("regTimeStart", regTimeStart);
        }
        if (StringUtils.isNotBlank(regTimeEnd)) {
            query.setRegEndTime(DateUtils.dateToString(DateUtils.stringToDate(regTimeEnd, "yyyy-MM-dd"), "yyyyMMddHHmmss"));
            inputConditionMap.put("regTimeEnd", regTimeEnd);
        }

        // 老师学科
        String subject = getRequestString("subject");
        inputConditionMap.put("subject", subject);
        if (StringUtils.isNotBlank(subject)) {
            query.setSubject(subject);
        }
        // 老师学校
        String schoolName = getRequestString("school");
        inputConditionMap.put("school", schoolName);
        if (StringUtils.isNotBlank(schoolName)) {
            query.setSchoolName(schoolName);
        }
        // 认证状态
        String authStatus = getRequestString("authStatus");
        inputConditionMap.put("authStatus", authStatus);
        query.setAuthStatus(authStatus);

        //三个认证条件
        String authCondReached = getRequestString("authCondReached");
        inputConditionMap.put("authCondReached", authCondReached);
        if ("authCond1Reached".equals(authCondReached)) {
            query.setAuthCond1reached(false);
            query.setAuthCond2reached(true);
            query.setAuthCond3reached(true);
            query.setAuthStatus("WAITING");
        } else if ("authCond2Reached".equals(authCondReached)) {
            query.setAuthCond1reached(true);
            query.setAuthCond2reached(false);
            query.setAuthCond3reached(true);
            query.setAuthStatus("WAITING");
        } else if ("authCond3Reached".equals(authCondReached)) {
            query.setAuthCond1reached(true);
            query.setAuthCond2reached(true);
            query.setAuthCond3reached(false);
            query.setAuthStatus("WAITING");
        } else if ("reachedButNotAuthed".equals(authCondReached)) {
            query.setAuthCond1reached(true);
            query.setAuthCond2reached(true);
            query.setAuthCond3reached(true);
            query.setAuthStatus("WAITING");
        } else if ("sysAutoAuthed".equals(authCondReached)) {
            query.setAuthStatus("SUCCESS");
        }
        // 90天内未使用筛选
        Boolean used90Days = getRequestBool("used90Days");
        inputConditionMap.put("used90Days", used90Days);
        if (used90Days) {
            query.setUnUsedDays(90);
        }

        // 所属地区
        Integer provCode = getRequestInt("provCode");
        Integer cityCode = getRequestInt("cityCode");
        Integer countyCode = getRequestInt("countyCode");
        inputConditionMap.put("provCode", provCode);
        inputConditionMap.put("cityCode", cityCode);
        inputConditionMap.put("countyCode", countyCode);
        if (provCode > 0) {
            query.setProvinceCodesMust(Collections.singleton(Long.valueOf(provCode)));
        }
        if (cityCode > 0) {
            query.setCityCodesMust(Collections.singleton(Long.valueOf(cityCode)));
        }
        if (countyCode > 0) {
            query.setCountyCodesMust(Collections.singleton(Long.valueOf(countyCode)));
        }

        // 按照重点城市筛选 Task #29651 By Wyc 2016-08-19
        Boolean keyCity = getRequestBool("keycity");
        inputConditionMap.put("keycity", keyCity);
        if (keyCity) {
            query.setCityCodes(KEY_CITY_CODE_COLLECTION.stream().map(Long::valueOf).collect(Collectors.toList()));
        }
        //分页
        Integer currentPage = getRequestInt("currentPage");
        query.setLimit(10);
        query.setPage(currentPage);
        Page<TeacherSummaryEsInfo> page = teacherSummaryEsServiceClient.getTeacherSummaryEsService().query(query);

        List<TeacherSummaryEsInfo> teachers = page.getContent();
        Integer pageCount = page.getTotalPages();
        Long totalCount = page.getTotalElements();
        Map<String, Map<String, Object>> teacherCCRecordInfo = new HashMap<>();
        List<Map<String, Object>> teacherMapList = new ArrayList<>();
        if (teachers.size() > 0) {
            teacherCCRecordInfo = crmTeacherSummaryService.getTeacherCCRecordInfo(teachers.stream().map(TeacherSummaryEsInfo::getTeacherId).collect(Collectors.toList()));
            // 拿出来排序 es 里面没有这些字段无法排序, 目前是单字段排序
            String queryParam = getRequestString("queryParam");
            if (totalCount <= 500 && StringUtils.isNoneBlank(queryParam)) {
                // 最近接通时间排序
                Map<String, Map<String, Object>> finalTeacherCCRecordInfo = teacherCCRecordInfo;
                teachers = new ArrayList<>(teachers);
                teachers.sort((o1, o2) -> {
                    Map<String, Object> o1Map = finalTeacherCCRecordInfo.get(ConversionUtils.toString(o1.getTeacherId()));
                    Map<String, Object> o2Map = finalTeacherCCRecordInfo.get(ConversionUtils.toString(o2.getTeacherId()));
                    if ("outCallCount".equals(queryParam)) {
                        return SafeConverter.toInt(o1Map.get("outCallCount")) - SafeConverter.toInt(o2Map.get("outCallCount"));
                    } else {
                        if (StringUtils.isNotBlank((String) o1Map.get(queryParam)) && StringUtils.isNotBlank((String) o2Map.get(queryParam))) {
                            return SafeConverter.toInt(DateUtils.dayDiff(DateUtils.stringToDate((String) o1Map.get(queryParam), "yyyy-MM-dd"), DateUtils.stringToDate((String) o2Map.get(queryParam), "yyyy-MM-dd")));
                        } else if (StringUtils.isBlank((String) o1Map.get(queryParam))) {
                            return -1;
                        } else if (StringUtils.isBlank((String) o2Map.get(queryParam))) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });
            }
            // 转换成页面需要的值
            teachers.forEach(t -> {
                Map<String, Object> teacherMap = new HashMap<>();
                teacherMap.put("teacherId", t.getTeacherId());
                teacherMap.put("teacherName", t.getRealName());
                try {
                    teacherMap.put("authStatus", AuthenticationState.valueOf(t.getAuthStatus()));
                } catch (Exception e) {
                    teacherMap.put("authStatus", AuthenticationState.WAITING);
                }
                teacherMap.put("authStatus", StringUtils.isBlank(t.getAuthStatus()) ? AuthenticationState.WAITING : AuthenticationState.valueOf(t.getAuthStatus()));
                teacherMap.put("authCond1Reached", t.getAuthCond1Reached());
                teacherMap.put("authCond2Reached", t.getAuthCond2Reached());
                teacherMap.put("authCond3Reached", t.getAuthCond3Reached());
                teacherMap.put("mobile", t.getMobile());
                teacherMap.put("latestAssignHomeworkTime", t.getLatestAssignHomeworkTime() == null ? "" : DateUtils.dateToString(new Date(t.getLatestAssignHomeworkTime()), "yyyy-MM-dd"));
                teacherMap.put("subject", Subject.safeParse(t.getSubject()) == null ? "" : Subject.safeParse(t.getSubject()).getValue());

                teacherMap.put("schoolName", t.getSchoolName());
                teacherMap.put("registerTime", t.getRegisterTime() == null ? "" : DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(t.getRegisterTime()), "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss"));
                teacherMapList.add(teacherMap);
            });
        }
        teacherMapList.forEach(p -> p.put("taskCount", crmTaskService.countUserFollowingTask(ConversionUtils.toLong(p.get("teacherId")))));

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPage", pageCount);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("teacherMapList", teacherMapList);
        model.addAttribute("ccRecordInfo", teacherCCRecordInfo);

        //TODO 还有一些页面需要参数是在页面写死的。还是得改来从这里传出去
        List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadProvinces();
        List<Map<String, Object>> provinceList = new ArrayList<>();
        regionList.forEach(ex -> provinceList.add(MapUtils.m("key", ex.getCode(), "value", ex.getName())));
        //推荐查询类型
        model.addAttribute("unusualStatus", CrmTeacherUnusualStatusType.toValidMap());
        model.addAttribute("inputConditionMap", inputConditionMap);
        model.addAttribute("provinceList", provinceList);

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        model.addAttribute("taskUsers", crmTaskService.taskUsers(adminUser));
        model.addAttribute("taskTypes", CrmTaskService.taskTypes(adminUser));
        model.addAttribute("clueTypes", CrmClueType.values());
        model.addAttribute("recordCategoryJson", CrmTaskService.teacherTaskRecordCategoryJson(adminUser));
        model.addAttribute("contactTypes", CrmTaskService.contactTypes(adminUser));
        model.addAttribute("isPhoneOut", CrmTaskService.isPhoneOut(adminUser));

        return "crm/teachernew/teacherlist";
    }

//    @RequestMapping(value = "query_teacher_list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
//    public String queryTeacherList(Model model) {
//        String queryParam = getRequestString("queryParam");
//        int currentPage = getRequestInt("currentPage");
//        SchoolLevel schoolLevel = SchoolLevel.safeParse(getRequestInt("schoolLevel"), null);
//        Map<String, Object> sessionMap = adminCacheSystem.CBS.flushable.load(SESSION_ESQUERY_STRING_PREFIX + getCurrentAdminUser().getAdminUserName());
//        Map<String, Object> inputConditionMap = (Map) sessionMap.get("inputConditionMap");
//        CrmTeacherSummaryEsqueryBuilder builder = (CrmTeacherSummaryEsqueryBuilder) sessionMap.get("builder");
//        builder = builder.withPageFrom(0, MAX_QUERY_COUNT);
//        CrmTeacherSummaryEsQueryContent queryContent = CrmTeacherSummaryEsQueryContent.Instance(builder.toQueryString());
//        Integer totalCount = queryContent.getTotalCount();
//        int pageCount = queryContent.getPageCount();
//        Set<Long> teacherIds = queryContent.getTeacherIds();
//        List<Long> returnTeacherList = new ArrayList<>();
//        Map<String, Map<String, Object>> teacherCCRecordInfo;
//        List<Long> sortedTeacherList = new ArrayList<>();
//        sortedTeacherList.addAll(teacherIds);
//        if (CollectionUtils.isNotEmpty(teacherIds)) {
//            teacherCCRecordInfo = crmTeacherSummaryService.getTeacherCCRecordInfo(teacherIds);
//            if (StringUtils.isNotBlank(queryParam)) {
//                Collections.sort(sortedTeacherList, (o1, o2) -> {
//                    Map<String, Object> o1Map = teacherCCRecordInfo.get(ConversionUtils.toString(o1));
//                    Map<String, Object> o2Map = teacherCCRecordInfo.get(ConversionUtils.toString(o2));
//                    if ("outCallCount".equals(queryParam)) {
//                        return SafeConverter.toInt(o1Map.get("outCallCount")) - SafeConverter.toInt(o2Map.get("outCallCount"));
//                    } else {
//                        if (StringUtils.isNotBlank((String) o1Map.get(queryParam)) && StringUtils.isNotBlank((String) o2Map.get(queryParam))) {
//                            return SafeConverter.toInt(DateUtils.dayDiff(DateUtils.stringToDate((String) o1Map.get(queryParam), "yyyy-MM-dd"), DateUtils.stringToDate((String) o2Map.get(queryParam), "yyyy-MM-dd")));
//                        } else if (StringUtils.isBlank((String) o1Map.get(queryParam))) {
//                            return -1;
//                        } else if (StringUtils.isBlank((String) o2Map.get(queryParam))) {
//                            return 1;
//                        } else {
//                            return 0;
//                        }
//                    }
//                });
//            }
//
//            currentPage = currentPage < 0 ? 1 : currentPage;
//            totalCount = totalCount < MAX_QUERY_COUNT ? totalCount : MAX_QUERY_COUNT;
//            if (currentPage < pageCount) {
//                returnTeacherList = sortedTeacherList.subList((currentPage - 1) * pageNum, currentPage * pageNum);
//            } else if (currentPage == pageCount) {
//                returnTeacherList = sortedTeacherList.subList((currentPage - 1) * pageNum, totalCount);
//            }
//
//            model.addAttribute("teacherMapList", crmTeacherSummaryService.generateTeacherDetailMap(returnTeacherList, schoolLevel, false));
//            model.addAttribute("ccRecordInfo", teacherCCRecordInfo);
//        }
//
//        //TODO 还有一些页面需要参数是在页面写死的。还是得改来从这里传出去
//        List<ExRegion> regionList = regionServiceClient.getExRegionBuffer().loadProvinces();
//        List<Map<String, Object>> provinceList = new ArrayList<>();
//        for (ExRegion ex : regionList) {
//            Map<String, Object> province = new HashMap<>();
//            province.put("key", ex.getCode());
//            province.put("value", ex.getName());
//            provinceList.add(province);
//        }
//        //actionType 用来区分action的url
//        model.addAttribute("actionType", 1);
//        model.addAttribute("queryParam", queryParam);
//        //推荐查询类型
//        model.addAttribute("unusualStatus", CrmTeacherUnusualStatusType.toMap());
//        model.addAttribute("unusualStatusNames", JsonUtils.toJson(CrmTeacherUnusualStatusType.toMap().keySet()));
//
//        model.addAttribute("inputConditionMap", inputConditionMap);
//        model.addAttribute("provinceList", provinceList);
//
//        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
//        model.addAttribute("taskUsers", crmTaskService.taskUsers(adminUser));
//        model.addAttribute("taskTypes", CrmTaskService.taskTypes(adminUser));
//        model.addAttribute("recordCategoryJson", CrmTaskService.teacherTaskRecordCategoryJson(adminUser));
//        model.addAttribute("contactTypes", CrmTaskService.contactTypes(adminUser));
//        model.addAttribute("isPhoneOut", CrmTaskService.isPhoneOut(adminUser));
//
//        model.addAttribute("currentPage", currentPage);
//        model.addAttribute("totalPage", pageCount);
//        model.addAttribute("totalCount", totalCount);
//
//        return "crm/teachernew/teacherlist";
//    }

    private static final Map<String, String> SUB_ACCOUNT_STATE_MAP = new HashMap<String, String>() {
        {
            put("passwdResetState", "true");
            put("specialPropertySetState", "true");
            put("decideForFalseState", "true");
            put("relieveDecideFalseState", "true");
            put("updateAuthenticationState", "true");
            put("gardenerBeanProvideSate", "true");
            put("telephoneChargeProvideState", "true");
            put("loginIndexState", "true");
            put("newRecordState", "true");
            put("newTaskState", "true");
            put("bigDataAuthState", "true");
            put("pendingTeacherState", "true");
            put("canBindMobile", "true");
        }
    };

    @RequestMapping(value = "teacherdetail.vpage")
    public String teacherDetail(Model model) {
        Long teacherId = getRequestLong("teacherId");
        if (teacherId == 0L) {
            return "redirect: /";
        }
        int selectTab = getRequestInt("selectTab");

        TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacherId);
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacherId);

        List<IntegralType> integralTypeList = new ArrayList<>();
        for (IntegralType type : IntegralType.values()) {
            if (!UNSUPPORTED_INTEGRAL_TYPE.contains(type)) {
                integralTypeList.add(type);
            }
        }

        List<KeyValuePair<Integer, String>> activityTypeList = ActivityType.toKeyValuePairs();

        model.addAttribute("teacherId", teacherId);
        model.addAttribute("selectTab", selectTab);

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();

        //task_new.ftl need
        model.addAttribute("taskUsers", crmTaskService.taskUsers(adminUser));
        model.addAttribute("taskTypes", CrmTaskService.taskTypes(adminUser));
        //record_new.ftl need
        model.addAttribute("isPhoneOut", CrmTaskService.isPhoneOut(adminUser));
        model.addAttribute("recordCategoryJson", CrmTaskService.teacherTaskRecordCategoryJson(adminUser));
        model.addAttribute("contactTypes", CrmTaskService.contactTypes(adminUser));
        model.addAttribute("taskCount", crmTaskService.countUserFollowingTask(teacherId));
        //teacherinfoheader.ftl need
        model.addAttribute("activityTypeList", activityTypeList);
        model.addAttribute("integralTypeList", integralTypeList);
        Map<String, Object> teacherInfoHeaderMap = new HashMap<>();
        teacherInfoHeaderMap.put("teacherName", teacherDetail == null ? "" : teacherDetail.fetchRealname());
        teacherInfoHeaderMap.put("ktwelve", teacherDetail == null || teacherDetail.getKtwelve() == null ? "" : teacherDetail.getKtwelve().name());
        //只有userAuthentication为null或者userAuthentication的mobile为空时才允许绑定手机
        UserAuthentication ua = raikouSystem.loadUserAuthentication(teacherId);
        teacherInfoHeaderMap.put("canBindMobile", ua == null || StringUtils.isBlank(ua.getSensitiveMobile()));
        teacherInfoHeaderMap.put("subject", teacherDetail == null || teacherDetail.getSubject() == null ? "" : teacherDetail.getSubject().getValue());
        teacherInfoHeaderMap.put("subjectName", teacherDetail == null || teacherDetail.getSubject() == null ? "" : teacherDetail.getSubject().name());
        teacherInfoHeaderMap.put("schoolId", teacherDetail == null ? "" : teacherDetail.getTeacherSchoolId());
        teacherInfoHeaderMap.put("schoolName", teacherDetail == null ? "" : teacherDetail.getTeacherSchoolName());
        teacherInfoHeaderMap.put("authenticationState", teacherDetail == null ? "" : teacherDetail.getAuthenticationState());
        teacherInfoHeaderMap.put("isCheat", newHomeworkServiceClient.isCheatingTeacher(teacherId));

        teacherInfoHeaderMap.put("authType", "");
        teacherInfoHeaderMap.put("ambassadorTime", "");
        teacherInfoHeaderMap.put("isAmbassador", "");
        teacherInfoHeaderMap.put("fakeTeacher", teacherLoaderClient.isFakeTeacher(teacherId));

        if (teacherExtAttribute != null) {
            CrmTeacherFakeValidationType validationType = CrmTeacherFakeValidationType.get(teacherExtAttribute.getValidationType());
            if (validationType != null) {
                teacherInfoHeaderMap.put("fakeType", validationType.getName());
                teacherInfoHeaderMap.put("fakeTypeDesc", validationType.getDesc());
            } else {
                teacherInfoHeaderMap.put("fakeType", "");
                teacherInfoHeaderMap.put("fakeTypeDesc", "");
            }
            teacherInfoHeaderMap.put("fakeDesc", teacherSummary == null ? "" : teacherSummary.getFakeDesc());
            teacherInfoHeaderMap.put("accountStatus", teacherExtAttribute.getAccountStatus().name());
        } else {
            teacherInfoHeaderMap.put("accountStatus", AccountStatus.NORMAL.name());
        }

        // teacherInfoHeaderMap.put("pending", teacherDetail == null ? "" : teacherDetail.getPending());
        teacherInfoHeaderMap.put("userType", teacherDetail == null ? "" : teacherDetail.getUserType());
        if (teacherDetail != null && teacherDetail.fetchCertificationState() == AuthenticationState.SUCCESS) {
            List<CertificationApplicationOperatingLog> authLogs = certificationServiceClient.getRemoteReference()
                    .findCertificationApplicationOperatingLogs(teacherId)
                    .getUninterruptibly();
            if (CollectionUtils.isNotEmpty(authLogs)) {
                CertificationApplicationOperatingLog authLog = authLogs.get(authLogs.size() - 1);
                String authType = "自动认证".equals(authLog.getOperatorName()) ? "自动认证" : "人工认证(" + authLog.getOperatorName() + ")";
                teacherInfoHeaderMap.put("authType", authType);
            }
        }
        if (teacherDetail != null && teacherDetail.isSchoolAmbassador()) {
            AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(teacherId).stream().findFirst().orElse(null);
            model.addAttribute("ambassadorTime", ref == null ? "--" : DateUtils.dateToString(ref.getCreateDatetime(), "yyyy-MM-dd"));
            teacherInfoHeaderMap.put("isAmbassador", true);
        }
        model.addAttribute("teacherInfoHeaderMap", teacherInfoHeaderMap);
        model.addAttribute("ms_crm_admin_url", juniorCrmAdminUrlBase());

        //主副账号切换
        //传到前台的信息要有 账号:主副标记:课程
        // 感谢长远大神！！！ By Wyc 2016-08-15
        List<KeyValuePair<Long, String>> mainSubAccount = new LinkedList<>();
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(SafeConverter.toLong(teacherId));
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(SafeConverter.toLong(teacherId));
        boolean mainaccountflag = false;
        if (mainTeacherId == null) {//当mainTeacherId是null时,teacherId是主账号
            mainTeacherId = SafeConverter.toLong(teacherId);
            mainaccountflag = true;
        }
        for (Long tempTeacherId : relTeacherIds) {
            Teacher tempTeacher = teacherLoaderClient.loadTeacher(tempTeacherId);
            if (tempTeacher == null) continue;
            if (mainTeacherId.equals(tempTeacher.getId())) {
                mainSubAccount.add(0, new KeyValuePair<>(tempTeacherId, "★主账号-" + (tempTeacher.getSubject() != null ? tempTeacher.getSubject().getValue() : "")));
            } else {
                mainSubAccount.add(new KeyValuePair<>(tempTeacherId, "&nbsp;&nbsp;副账号-" + (tempTeacher.getSubject() != null ? tempTeacher.getSubject().getValue() : "")));
            }
        }
        model.addAttribute("accountMainSubSubjectMap", mainSubAccount);

        //如果当前账号是副账号时,禁止:密码重置、特殊属性设置、判定为假、解除判假、更新认证状态、园丁豆发放、话费发放以及绑定手机号
        Map<String, String> subaccountStateMap = new HashMap<>();
        if (!mainaccountflag) {
            subaccountStateMap.putAll(SUB_ACCOUNT_STATE_MAP);
        }
        model.addAttribute("subaccountStateMap", subaccountStateMap);
        model.addAttribute("mainaccountflag", mainaccountflag);

        List<Map<String, String>> validSubjects = new LinkedList<>();
        if (teacherDetail != null && teacherDetail.getTeacherSchoolId() != null) {
            //判断是不是重点校
            boolean isDictSchool = Boolean.TRUE.equals(agentOrgLoaderClient.isDictSchool(teacherDetail.getTeacherSchoolId()));
            model.addAttribute("isDictSchool", isDictSchool);
            //重点校返回市场人员姓名和手机号
            if (isDictSchool) {
                Map<String, Object> marketInfo = teacherAgentLoaderClient.getSchoolManager(teacherDetail.getTeacherSchoolId()); //重点校返回市场人员姓名和手机号
                if (MapUtils.isNotEmpty(marketInfo)) {
                    model.addAttribute("marketInfo", marketInfo);
                }
            }

            Boolean isPrimarySchool = true;
            School school = raikouSystem.loadSchool(teacherDetail.getTeacherSchoolId());
            if (school != null) {
                if (school.isJuniorSchool() || school.isSeniorSchool()) {
                    isPrimarySchool = false;
                }
            }
            model.addAttribute("isPrimarySchool", isPrimarySchool);

            //中学开通所有
            if (!isPrimarySchool) {
                Subjects.ALL_SUBJECTS.stream().forEach(e -> {
                    validSubjects.add(MapUtils.map("value", e.getValue(), "name", e.name()));
                });
            }

        }

        //选择学科：默认基础三科
        if (validSubjects.isEmpty()) {
            Subjects.BASIC_SUBJECTS.forEach(e -> {
                validSubjects.add(MapUtils.map("value", e.getValue(), "name", e.name()));
            });
        }
        model.addAttribute("validSubjects", validSubjects);


        if (teacherDetail != null && (teacherDetail.isJuniorTeacher() || teacherDetail.isSeniorTeacher())) {
            List<TeacherRoles> teacherRoles = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacherDetail.getId());
            Map<String, Object> subjectLeaderMap = new HashMap<>();

            TeacherRoles subjectLeader = teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SUBJECT_LEADER.name()))
                    .findAny().orElse(null);
            subjectLeaderMap.put("subjectLeaderFlag", subjectLeader != null);

            if (subjectLeader != null && StringUtils.isNoneBlank(subjectLeader.getRoleContent())) {
                List<ClazzLevel> clazzLevels = JsonUtils.fromJsonToList(subjectLeader.getRoleContent(), ClazzLevel.class);
                if (CollectionUtils.isNotEmpty(clazzLevels)) {
                    List<String> clazzLevelStrList = new HashSet<>(clazzLevels)
                            .stream().sorted(Comparator.comparingInt(o -> o.getLevel()))
                            .map(ClazzLevel::getDescription)
                            .collect(Collectors.toList());
                    subjectLeaderMap.put("clazzLevelStrSet", clazzLevelStrList);
                }
            }

            model.addAttribute("subjectLeaderMap", subjectLeaderMap);
            if (teacherDetail.isJuniorTeacher()) {
                model.addAttribute("juniorTeacher", true);
            } else {
                model.addAttribute("seniorTeacher", true);
            }

            // 班主任
            TeacherRoles classManager = teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.CLASS_MANAGER.name()))
                    .findAny().orElse(null);
            model.addAttribute("classManagerFlag", classManager != null);
            if (classManager != null && StringUtils.isNoneBlank(classManager.getRoleContent())) {
                List<Long> managedClassIds = JsonUtils.fromJsonToList(classManager.getRoleContent(), Long.class);
                Collection<Clazz> managedClassList = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazzs(managedClassIds);
                List<String> managedClassNameList = managedClassList.stream().map(Clazz::formalizeClazzName).collect(Collectors.toList());
                model.addAttribute("managedClassList", StringUtils.join(managedClassNameList, ","));
            }

            // 年级主任
            TeacherRoles gradeManager = teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.GRADE_MANAGER.name()))
                    .findAny().orElse(null);

            model.addAttribute("gradeManagerFlag", gradeManager != null);
            if (gradeManager != null && StringUtils.isNoneBlank(gradeManager.getRoleContent())) {
                List<Integer> gradeList = JsonUtils.fromJsonToList(gradeManager.getRoleContent(), Integer.class);
                if (CollectionUtils.isNotEmpty(gradeList)) {
                    EduSystemType est = EduSystemType.S4;
                    if (teacherDetail.isJuniorTeacher()) {
                        est = EduSystemType.J4;
                    }
                    List<Integer> managedGradeList = new ArrayList<>();
                    for (Integer jie : gradeList) {
                        ClazzLevel clazzLevel = ClassJieHelper.toClazzLevel(jie, est);
                        if (clazzLevel.getLevel() >= ClazzLevel.SIXTH_GRADE.getLevel() && clazzLevel.getLevel() <= ClazzLevel.SENIOR_THREE.getLevel()) {
                            managedGradeList.add(clazzLevel.getLevel());
                        }
                    }
                    model.addAttribute("managedGradeList", StringUtils.join(managedGradeList, ","));
                }
            }

            // 中学校长
            TeacherRoles schoolMaster = teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SCHOOL_MASTER.name()))
                    .findAny().orElse(null);
            model.addAttribute("schoolMasterFlag", schoolMaster != null);
        }

        if (teacherDetail != null && teacherDetail.isKLXTeacher()) {
            String username;
            if (teacherExtAttribute == null || teacherExtAttribute.getHappyUserName() == null) {
                username = teacherId.toString();
            } else {
                username = teacherExtAttribute.getHappyUserName();
            }
            model.addAttribute("klxTeacherUserName", username);
        }
        model.addAttribute("isSeiueSchool", teacherDetail != null && isSeiueSchool(teacherDetail.getTeacherSchoolId()));

        return "crm/teachernew/teacherdetail";
    }

    @RequestMapping(value = "changeschoolwithclass.vpage")
    public String changeschoolwithclass(Model model) {
        model.addAttribute("teacherId", getRequestString("teacherId"));
        return "crm/teachernew/changeschoolwithclass";
    }

    @RequestMapping(value = "teacherinfo.vpage")
    public String teacherInfo(Model model) {
        String teacherId = getRequestString("teacherId");
        Map<String, Object> teacherInfoMap = crmTeacherSummaryService.getTeacherInfoMap(ConversionUtils.toLong(teacherId));
        model.addAttribute("teacherInfoMap", teacherInfoMap);
        return "crm/teachernew/teacherhomepage";
    }

    @RequestMapping(value = "teacherclazz.vpage")
    @SuppressWarnings("unchecked")
    public String getTeacherClazz(Model model) {
        Long teacherId = getRequestLong("teacherId");
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherId == 0 || teacher == null) {
            return "crm/teachernew/teacherclazz";
        }
        Map<String, Object> teacherClazzGroupInfo = crmTeacherSummaryService.getTeacherClazzGroupInfo(teacherId);
        model.addAttribute("teacherClazzGroupInfo", teacherClazzGroupInfo);

        CrmTeacherSummary teacherSummary = crmTeacherSummaryService.getCrmTeacherSummary(ConversionUtils.toString(teacherId), "teacherId");
        boolean klxTeacher = teacher.isKLXTeacher() || teacher.isJuniorEnglishOrChineseTeacher();
        List<GroupMapper> groupMapperList = deprecatedGroupLoaderClient.loadTeacherGroupsByTeacherId(teacherId, true);
        List<Long> clazzIds = groupMapperList.stream().map(GroupMapper::getClazzId).collect(Collectors.toList());
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        Map<Long, List<KlxStudent>> klxStudentMap = new LinkedHashMap<>();
        if (klxTeacher) {
            klxStudentMap = newKuailexueLoaderClient.loadKlxGroupStudents(
                    groupMapperList.stream().map(GroupMapper::getId).collect(toList())
            );
        }
        List<Map<String, Object>> groupStudentList = new ArrayList<>();

        for (GroupMapper mapper : groupMapperList) {
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", mapper.getId());
            Clazz clazz = clazzs.get(mapper.getClazzId());
            map.put("clazzId", clazz != null ? clazz.getId() : "");
            map.put("clazzLevel", clazz != null ? clazz.getClassLevel() : "");
            map.put("clazzName", clazz != null ? clazz.formalizeClazzName() : "");
            List<GroupMapper.GroupUser> students = mapper.getStudents();
            if (klxTeacher) {
                List<KlxStudent> klxStudents = klxStudentMap.get(mapper.getId());
                // 修正一下学生列表
                if (CollectionUtils.isNotEmpty(klxStudents)) {
                    if (CollectionUtils.isNotEmpty(students)) {
                        // 把已经存在的学生从student里合并进来
                        Set<Long> realIds = klxStudents.stream()
                                .filter(KlxStudent::isRealStudent)
                                .map(KlxStudent::getA17id)
                                .collect(toSet());
                        students = students.stream().filter(u -> !realIds.contains(u.getId())).collect(toList());
                    }

                    klxStudents = klxStudents.stream().sorted((o1, o2) -> {
                        String n1 = SafeConverter.toString(o1.getName());
                        String n2 = SafeConverter.toString(o2.getName());
                        return Collator.getInstance(Locale.CHINESE).compare(n1, n2);// 按拼音排序
                    }).collect(Collectors.toList());
                }
                map.put("klxStudents", klxStudents);
            }

            map.put("studentList", students);
            groupStudentList.add(map);
        }
        groupStudentList.sort((o1, o2) -> {
            final Integer o1_ClassLevel = SafeConverter.toInt(o1.get("clazzLevel"));
            final Integer o2_ClassLevel = SafeConverter.toInt(o2.get("clazzLevel"));
            if (o1_ClassLevel.equals(o2_ClassLevel)) {
                String n1 = SafeConverter.toString(o1.get("clazzName"));
                String n2 = SafeConverter.toString(o2.get("clazzName"));
                return Collator.getInstance(Locale.CHINESE).compare(n1, n2);// 按拼音排序
            }
            return Integer.compare(o1_ClassLevel, o2_ClassLevel);
        });
        model.addAttribute("teacherSummary", teacherSummary);
        model.addAttribute("groupStudentList", groupStudentList);
        model.addAttribute("cjlSchool", isCJLSchool(teacher.getTeacherSchoolId()));
        model.addAttribute("isSeiueSchool", teacher != null && isSeiueSchool(teacher.getTeacherSchoolId()));
        return "crm/teachernew/teacherclazz";
    }

    // 老师包班列表
    @RequestMapping(value = "teacherclazzapply.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTeacherClazzListForApply() {
        Long teacherId = getRequestLong("teacherId");
        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacher == null || teacher.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的老师账号！");
            }
            if (!teacher.isPrimarySchool()) {
                return MapMessage.errorMessage("中学老师暂不支持开通包班！");
            }
            if (teacherLoaderClient.loadMainTeacherId(teacherId) != null) {
                return MapMessage.errorMessage("副账号不支持开通包班！");
            }

            // 已经包班的账号
            Set<Long> relatedTeachers = teacherLoaderClient.loadRelTeacherIds(teacherId);
            Map<Long, List<GroupMapper>> teacherGroupMap = deprecatedGroupLoaderClient.loadTeacherGroupsByTeacherId(relatedTeachers, false);
            // 主账号班级
            List<GroupMapper> mainGroupList = teacherGroupMap.getOrDefault(teacherId, Collections.emptyList());
            Set<Long> mainClazzId = mainGroupList.stream().map(GroupMapper::getClazzId).collect(toSet());
            Map<Long, Clazz> mainClazzMap = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(mainClazzId)
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));

            // 已经包班的学科
            Set<Subject> subjectFilter = teacherGroupMap.values().stream().flatMap(List::stream)
                    .filter(k -> k != null && k.getSubject() != null && Subject.UNKNOWN != k.getSubject())
                    .map(GroupMapper::getSubject).collect(toSet());

            // 对班级按名称粗略排个序
            List<GroupMapper> mainGroup = mainGroupList.stream().filter(g -> {
                Clazz clazz = mainClazzMap.get(g.getClazzId());
                if (clazz == null) {
                    return false;
                }
                // 过滤掉没有在教的班级
                if (!teacherLoaderClient.isTeachingClazz(teacherId, g.getClazzId())) {
                    return false;
                }
                int classLevel = SafeConverter.toInt(clazz.getClassLevel());
                return classLevel > 0 && classLevel <= 6;
            }).sorted((g1, g2) -> {
                Clazz clazz1 = mainClazzMap.get(g1.getClazzId());
                Clazz clazz2 = mainClazzMap.get(g2.getClazzId());
                return clazz1.formalizeClazzName().compareTo(clazz2.formalizeClazzName());
            }).collect(toList());


            // 主账号没有可以可以开通包班的班级
            if (CollectionUtils.isEmpty(mainGroup)) {
                return MapMessage.errorMessage("没有可以开通包班的班级");
            }

            Map<String, List<Map<String, Object>>> subjectClazz = new LinkedHashMap<>();

            for (Subject subject : Arrays.asList(Subject.CHINESE, Subject.ENGLISH, Subject.MATH)) {
                // 已经包班的学科(包括主账号)
                if (subjectFilter.contains(subject)) continue;
                // 判断语文的灰度
                if (subject == Subject.CHINESE
                        && !grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, "Chinese", "Register")) {
                    continue;
                }

                List<Map<String, Object>> clazzList = mainGroup.stream().map(p -> {
                    Map<String, Object> groupInfo = new HashMap<>();
                    groupInfo.put("groupId", p.getId());
                    groupInfo.put("clazzId", p.getClazzId());
                    Clazz clazz = mainClazzMap.get(p.getClazzId());
                    groupInfo.put("clazzName", clazz == null ? "" : clazz.formalizeClazzName());
                    return groupInfo;
                }).collect(toList());

                if (CollectionUtils.isNotEmpty(clazzList)) {
                    subjectClazz.put(subject.name(), clazzList);
                }
            }

            if (MapUtils.isEmpty(subjectClazz)) {
                return MapMessage.errorMessage("没有可以开通包班的班级");
            }

            return MapMessage.successMessage()
                    .add("subjectClazz", subjectClazz);
        } catch (Exception ex) {
            logger.error("Failed load teacher class list. teacherId={}", teacherId, ex);
            return MapMessage.errorMessage("读取班级信息失败");
        }
    }

    @RequestMapping(value = "teacherclazzapply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processTeacherSubAccountApply() {
        Long teacherId = getRequestLong("teacherId");
        Long clazzId = getRequestLong("clazzId");
        Subject subject = Subject.ofWithUnknown(getRequestString("subject"));
        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacher == null || teacher.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的老师ID");
            }
            if (subject == Subject.UNKNOWN) {
                return MapMessage.errorMessage("无效的学科");
            }

            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null || clazz.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的班级");
            }

            MapMessage result = teacherSystemClazzServiceClient.createSubTeacherForTeacherAndClazz(teacherId, clazzId, subject, OperationSourceType.crm);

            // 记录一条处理日志
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.包班申请.name());
            userServiceRecord.setOperationContent("开通包班");
            userServiceRecord.setComments(StringUtils.formatMessage("开通学科:{}, 开通班级:{}", subject.getValue(), clazzId));
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            return MapMessage.successMessage("班级：" + clazzId + " 开通包班" + (result.isSuccess() ? "处理成功" : "处理失败：" + result.getInfo()) + "\n");
        } catch (Exception ex) {
            logger.error("Failed create sub Teacher Account. teacherId={}, clazz={}, subject={}", teacherId, clazzId, subject, ex);
            return MapMessage.errorMessage("开通包班失败");
        }

    }

    @RequestMapping(value = "findsubteachersubject.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findSubTeacherSubject() {
        Long mainTeacherId = getRequestLong("teacherId");
        Teacher teacher = teacherLoaderClient.loadTeacher(mainTeacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师账号");
        }
        if (!teacher.isPrimarySchool()) {
            return MapMessage.errorMessage("小学老师才能有此操作");
        }
        List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(mainTeacherId);
        if (CollectionUtils.isEmpty(subTeacherIds)) {
            return MapMessage.errorMessage("未找到老师的副账号信息");
        }
        List<String> subjectList = teacherLoaderClient.loadTeachers(subTeacherIds)
                .values()
                .stream()
                .map(Teacher::getSubject)
                .map(Subject::name)
                .collect(Collectors.toList());

        return MapMessage.successMessage().add("subjectList", subjectList);
    }

    // 取消包班
    @RequestMapping(value = "cancelsubteacheraccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancelSubTeacherAccount() {
        Long mainTeacherId = getRequestLong("teacherId");
        Subject subject = Subject.ofWithUnknown(getRequestString("subject"));
        Teacher mainTeacher = teacherLoaderClient.loadTeacher(mainTeacherId);
        if (mainTeacher == null || mainTeacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师账号");
        }
        if (!mainTeacher.isPrimarySchool()) {
            return MapMessage.errorMessage("小学老师才能操作");
        }
        List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(mainTeacherId);
        if (CollectionUtils.isEmpty(subTeacherIds)) {
            return MapMessage.errorMessage("未找到老师的副账号信息");
        }
        Teacher teacher = teacherLoaderClient.loadTeachers(subTeacherIds)
                .values()
                .stream()
                .filter(t -> t.getSubject() == subject)
                .findFirst().orElse(null);
        if (teacher == null) {
            return MapMessage.errorMessage("未找到" + subject.getValue() + "科目所对应的包班制老师");
        }
        List<GroupTeacherTuple> groupTeacherRefs = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByTeacherId(teacher.getId());
        List<GroupTeacherTuple> validGroupTeacherRefs = groupTeacherRefs.stream()
                .filter(GroupTeacherTuple::isValidTrue)
                .collect(Collectors.toList());
        List<GroupTeacherTuple> invalidGroupTeacherRefs = groupTeacherRefs.stream()
                .filter(p -> !p.isValidTrue())
                .collect(Collectors.toList());
        if (validGroupTeacherRefs.size() > 0) {
            return MapMessage.errorMessage("该账号下有班级未转出，请处理后再进行取消操作");
        }
        if (invalidGroupTeacherRefs.size() > 0) { //老师名下有不教了的分组，直接断开关系，同时断开分组和学生关系
            for (GroupTeacherTuple groupTeacherRef : invalidGroupTeacherRefs) {
                raikouSDK.getClazzClient()
                        .getGroupTeacherTupleServiceClient()
                        .getGroupTeacherTupleService()
                        .disable(groupTeacherRef.getId(), new UpdateOption().recordLog(true))
                        .awaitUninterruptibly();
                List<GroupStudentTuple> groupStudentRefs = raikouSDK.getClazzClient()
                        .getGroupStudentTupleServiceClient()
                        .getGroupStudentTupleService()
                        .dbFindByGroupIdIncludeDisabled(groupTeacherRef.getGroupId())
                        .getUninterruptibly()
                        .stream()
                        .filter(e -> !e.isDisabledTrue())
                        .collect(toList());
                if (CollectionUtils.isNotEmpty(groupStudentRefs)) {
                    groupStudentRefs.forEach(p -> raikouSDK.getClazzClient()
                            .getGroupStudentTupleServiceClient()
                            .getGroupStudentTupleService()
                            .disable(p.getId())
                            .awaitUninterruptibly());
                }
            }
        }
        teacherServiceClient.disableRefByMainSubId(mainTeacherId, teacher.getId());
        userServiceClient.updateAuthenticationState(teacher.getId(), AuthenticationState.FAILURE.getState());
        schoolServiceClient.getSchoolService().disableUserSchoolRefByUserId(teacher.getId());

        // 记录一条处理日志
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(mainTeacherId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.取消包班.name());
        userServiceRecord.setOperationContent("取消包班");
        userServiceRecord.setComments("");
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        return MapMessage.successMessage();
    }

    // 变更主账号
    @RequestMapping(value = "changemainteacheraccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeMainTeacherAccount() {
        Long subTeacherId = getRequestLong("teacherId");
        Teacher subTeacher = teacherLoaderClient.loadTeacher(subTeacherId);
        if (subTeacher == null || subTeacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师账号");
        }

        Long tobeSubId = teacherLoaderClient.loadMainTeacherId(subTeacherId);
        if (tobeSubId == null) {
            return MapMessage.errorMessage("未找到对应的主账号");
        }

        // 主副账号切换1. 手机号码绑定到副账号
        String decodedPhone = sensitiveUserDataServiceClient.showUserMobile(tobeSubId, "changeMainTeacherAccount", SafeConverter.toString(tobeSubId));
        userServiceClient.cleanupUserMobile("system", tobeSubId);
        userServiceClient.activateUserMobile(subTeacherId, decodedPhone);

        // 主副账号切换2. 积分切换，只能对主账号操作
        Integral integral = integralLoaderClient.getIntegralLoader().loadIntegral(tobeSubId);
        if (integral != null && integral.getUsableIntegral() > 0) {
            IntegralHistory decrease = new IntegralHistory();
            decrease.setUserId(tobeSubId);
            decrease.setIntegral(integral.getUsableIntegral() * -1);
            decrease.setComment("主副账号切换调整积分");
            decrease.setIntegralType(IntegralType.积分调整.getType());
            userIntegralServiceClient.getUserIntegralService().changeIntegral(decrease);
        }

        // 主副账号切换3 TeacherRef 处理
        List<Long> allSubIds = teacherLoaderClient.loadSubTeacherIds(tobeSubId);
        teacherServiceClient.disableAllRefs(tobeSubId);
        teacherServiceClient.saveTeacherRef(subTeacherId, tobeSubId);
        for (Long subId : allSubIds) {
            if (Objects.equals(subId, subTeacherId)) {
                continue;
            }

            teacherServiceClient.saveTeacherRef(subTeacherId, subId);
        }

        // 主副账号切换2补充处理. 积分切换，只能对主账号操作
        if (integral != null && integral.getUsableIntegral() > 0) {
            IntegralHistory increase = new IntegralHistory();
            increase.setUserId(subTeacherId);
            increase.setIntegral(integral.getUsableIntegral());
            increase.setComment("主副账号切换调整积分");
            increase.setIntegralType(IntegralType.积分调整.getType());
            userIntegralServiceClient.getUserIntegralService().changeIntegral(increase);
        }
        UserAuthentication authentication = raikouSystem.loadUserAuthentication(tobeSubId);
        userServiceClient.changePwdAndSalt(subTeacher.getId(), authentication.getPassword(), authentication.getSalt());

        // 交换两个账号的扩展属性
        teacherServiceClient.swapTeacherExtAttribute(subTeacherId, tobeSubId);

        // 处理缓存
        asyncUserServiceClient.getAsyncUserService().evictUserCache(tobeSubId).awaitUninterruptibly();
        asyncUserServiceClient.getAsyncUserService().evictUserCache(allSubIds).awaitUninterruptibly();

        // 记录一条处理日志
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(subTeacherId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.变更主账号.name());
        userServiceRecord.setOperationContent("变更主账号");
        userServiceRecord.setComments("");
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "teacherhomeworkhistory.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String getTeacherHomeworkHistory(Model model) {
//        String teacherId = getRequestString("teacherId");
//        Integer homeworkDay = getRequestInt("homeworkDay") == 0 ? 30 : getRequestInt("homeworkDay");
//        Integer quizDay = getRequestInt("quizDay") == 0 ? 30 : getRequestInt("quizDay");
//        Teacher teacher = teacherLoaderClient.loadTeacher(ConversionUtils.toLong(teacherId));
//        PageRequest pageable = new PageRequest(0, 200);
//
//        List<Map<String, Object>> teacherQuizList = new ArrayList<>();
//        List<Map<String, Object>> teacherHomeworkHistoryList = new ArrayList<>();
//        //老师基本信息
//        CrmTeacherSummary teacherSummary = crmTeacherSummaryService.getCrmTeacherSummary(teacherId, "teacherId");
//        //一般作业历史
//        if (teacher != null) {
//            if (teacher.getKtwelve() == Ktwelve.JUNIOR_SCHOOL) {
//                pageable = new PageRequest(0, 200, new Sort(Sort.Direction.DESC, "create_time"));
//
//                teacherSummary.setTotalHomeworkCount(businessHomeworkServiceClient.getMiddleSchoolTeacherTotalHomeworksCount(teacherSummary.getTeacherId()).intValue());
//                teacherSummary.setDay7HomeworkCount(businessHomeworkServiceClient.getMiddleSchoolTeacherLast7DaysHomeworksCount(teacherSummary.getTeacherId()).intValue());
//                teacherSummary.setDay30HomeworkCount(businessHomeworkServiceClient.getMiddleSchoolTeacherLast30DaysHomeworksCount(teacherSummary.getTeacherId()).intValue());
//                MiddleSchoolHomework homework = businessHomeworkServiceClient.getMiddleSchoolTeacherFirstHomework(teacherSummary.getTeacherId());
//                if (homework != null) {
//                    teacherSummary.setFirstAssignHomeworkTime(homework.getCreateTime().getTime());
//                }
//                homework = businessHomeworkServiceClient.getMiddleSchoolTeacherLastHomework(teacherSummary.getTeacherId());
//                if (homework != null) {
//                    teacherSummary.setLatestAssignHomeworkTime(homework.getCreateTime().getTime());
//                }
//            }
//
//            Page<HomeworkHistoryListMapper> page = businessHomeworkServiceClient.getHomeworkHistory(teacher, null,
//                    DateUtils.nextDay(new Date(), -1 * homeworkDay), null, pageable);
//            List<HomeworkHistoryListMapper> historyListMapperList = Collections.emptyList();
//            if (page != null)
//                historyListMapperList = page.getContent();
//            for (HomeworkHistoryListMapper historyListMapper : historyListMapperList) {
//                MapMessage mapMessage = null;
//                Subject subject = historyListMapper.getHomeworkType().getSubject();
//                if (historyListMapper.getHomeworkType() == HomeworkType.MIDDLESCHOOL_HOMEWORK) {
//                    mapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(historyListMapper.getHomeworkId(), historyListMapper.getHomeworkType());
//                } else {
////                    switch (subject) {
////                        case ENGLISH:
////                            mapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(historyListMapper.getHomeworkId(), HomeworkType.ENGLISH);
////                            break;
////                        case MATH:
////                            mapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(historyListMapper.getHomeworkId(), HomeworkType.MATH);
////                            break;
////                        case CHINESE:
////                            mapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(historyListMapper.getHomeworkId(), HomeworkType.CHINESE);
////                            break;
////                        default:
////                            break;
////                    }
//                }
//
//                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
//
//                if (mapMessage == null || !mapMessage.isSuccess()) {
//                    continue;
//                }
//                HomeworkLocation location = new HomeworkLocation();
//                location.setHomeworkId(historyListMapper.getHomeworkId());
//                location.setHomeworkType(historyListMapper.getHomeworkType());
//                IHomework homework = homeworkLoaderClient.loadHomework(location);
//                map.put("arrangeTime", homework.getHomeworkCreateTime());
//                if (homework instanceof Homework) {
//                    map.put("isTermEnd", (Homework.class.cast(homework).isTermEnd()));
//                }
//                map.put("checkTime", homework.getCheckTime());
//                map.put("homeworkId", historyListMapper.getHomeworkId());
//                map.put("homeworkSubject", subject != Subject.UNKNOWN ? subject : homework.getSubject());
//                map.put("clazzName", mapMessage.get("clazzName"));
//                map.put("clazzId", mapMessage.get("clazzId"));
//                map.put("source", HomeworkSourceType.of(SafeConverter.toString(mapMessage.get("source"))).getDesc());
//                // add disabled status
//                map.put("disabled", homework.isHomeworkDisabled());
//
//                int studentCount = SafeConverter.toInt(mapMessage.get("joinCount")) + SafeConverter.toInt(mapMessage.get("completeCount")) + SafeConverter.toInt(mapMessage.get("undoCount"));
//                int joinCount = SafeConverter.toInt(mapMessage.get("joinCount")) + SafeConverter.toInt(mapMessage.get("completeCount"));
//                map.put("studentCount", studentCount);
//                map.put("joinCount", joinCount);
//                map.put("completeCount", mapMessage.get("completeCount"));
//                List<String> ips = crmTeacherService.countHomeworkAccomplishmentIp(historyListMapper.getHomeworkId(), homework.getSubject());
//                map.put("ipcount", ips.size());
//
//                if (homework.getTeacherId() != null && !teacherId.equals(String.valueOf(homework.getTeacherId()))) {
//                    map.put("homeworkTeacherId", homework.getTeacherId());
//                    Teacher homeworkTeacher = teacherLoaderClient.loadTeacher(homework.getTeacherId());
//                    map.put("homeworkTeacherName", homeworkTeacher.getProfile().getRealname());
//                }
//
//                teacherHomeworkHistoryList.add(map);
//            }
//
//            //一般测验历史
//            List<GroupMapper> groupMapperList = groupLoaderClient.loadTeacherGroupsByTeacherId(teacher.getId(), false);
//            if (CollectionUtils.isNotEmpty(groupMapperList)) {
//                Map<Long, List<GroupStudentRef>> groupStudentRefs = asyncGroupServiceClient.getAsyncGroupService()
//                        .findGroupStudentRefs(groupMapperList.stream().map(GroupMapper::getId).collect(Collectors.toList()))
//                        .getUninterruptibly();
//                Map<Long, GroupMapper> groupMapperMap = new HashMap<>();
//                groupMapperList.forEach(p -> groupMapperMap.put(p.getId(), p));
//                List<Clazz> clazzs = clazzLoaderClient.loadTeacherClazzs(teacher.getId()).stream()
//                        .filter(Clazz::isPublicClazz)
//                        .filter(c -> !c.isTerminalClazz())
//                        .collect(Collectors.toList());
//                Map<Long, Clazz> clazz_map = ConversionUtils.toMap(clazzs);
//                List<GroupMapper> groups = groupLoaderClient.loadTeacherGroupsByTeacherId(teacher.getId(), false);
//                Set<Long> groupIds = groups.stream().map(GroupMapper::getId).collect(Collectors.toSet());
//                Page<Quiz> quizPage = crmTeacherSummaryService.loadGroupQuizs(groupIds, teacher.getSubject(), pageable, quizDay);
//                for (Quiz quiz : quizPage.getContent()) {
//                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
//                    map.put("arrangeTime", quiz.getCreateDatetime());
//                    map.put("checkTime", quiz.getCheckedTime());
//                    map.put("quizId", quiz.getId());
//                    map.put("clazzName", clazz_map.containsKey(quiz.getClazzId()) ? clazz_map.get(quiz.getClazzId()).formalizeClazzName() : "");
//                    map.put("clazzId", quiz.getClazzId());
//                    map.put("studentCount", groupStudentRefs.containsKey(quiz.getGroupId()) ? groupStudentRefs.get(quiz.getGroupId()).size() : 0);
//                    map.put("source", HomeworkSourceType.of(quiz.getSource()).getDesc());
//
//                    String month = MonthRange.newInstance(quiz.fetchCreateTimestamp()).toString();
//                    List<StudentQuizResult> resultList = quizResultLoaderClient.loadStudentQuizResults(month, quiz.getHomeworkType(), quiz.getId()).toList();
//                    Integer completeCount = 0;
//                    Integer joinCount = 0;
//                    if (CollectionUtils.isNotEmpty(resultList)) {
//                        joinCount = resultList.size();
//                        completeCount = resultList.stream().filter((p) -> p.getFinished().equals(true)).collect(Collectors.toList()).size();
//                    }
//                    map.put("joinCount", joinCount);
//                    map.put("completeCount", completeCount);
//
//                    List<String> ips = crmTeacherService.countHomeworkAccomplishmentIp(ConversionUtils.toString(quiz.getId()), Subject.valueOf(quiz.getSubject()));
//                    map.put("ipcount", ips.size());
//
//                    if (quiz.getTeacherId() != null && !teacherId.equals(String.valueOf(quiz.getTeacherId()))) {
//                        map.put("quizTeacherId", quiz.getTeacherId());
//                        Teacher homeworkTeacher = teacherLoaderClient.loadTeacher(quiz.getTeacherId());
//                        map.put("quizTeacherName", homeworkTeacher.getProfile().getRealname());
//                    }
//
//                    teacherQuizList.add(map);
//                }
//            }
//        }
//        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(ConversionUtils.toLong(teacherId));
//        model.addAttribute("teacherDetail", teacherDetail);
//        model.addAttribute("teacherSummary", teacherSummary);
//        model.addAttribute("homeworkHistoryList", teacherHomeworkHistoryList);
//        model.addAttribute("quizList", teacherQuizList);
//        model.addAttribute("homeworkDay", homeworkDay);
//        model.addAttribute("quizDay", quizDay);
//        model.addAttribute("unuseToToday", (teacherSummary == null || teacherSummary.getLatestAssignHomeworkTime() == null) ? "" : DateUtils.dayDiff(new Date(), new Date(teacherSummary.getLatestAssignHomeworkTime())));
        return "crm/teachernew/teacherhomeworkhistory";
    }

    @RequestMapping(value = "teachernewexamreportforstudent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String teacherNewExamReportForStudent(Model model) {
        String newExamResultId = this.getRequestString("newExamResultId");
        String path = "crm/teachernew/teachernewexamreportforstudent";
        NewExamReportForStudent newExamReportForStudent = new NewExamReportForStudent();
        if (StringUtils.isBlank(newExamResultId)) {
            newExamReportForStudent.setSuccess(false);
            newExamReportForStudent.setDescription("newExamResultId is blank");
            model.addAttribute("newExamReportForStudent", newExamReportForStudent);
            return path;
        }
        newExamReportForStudent = newExamReportLoaderClient.crmReceiveNewExamReportForStudent(newExamResultId);
        model.addAttribute("newExamReportForStudent", newExamReportForStudent);
        return path;
    }

    @RequestMapping(value = "teachernewexamreportforclazz.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String teacherNewExamReportForClazz(Model model) {
        String teacherId = getRequestString("teacherId");
        String path = "crm/teachernew/teachernewexamreportlist";
        NewExamReportForClazz newExamReportForClazz = new NewExamReportForClazz();
        if (StringUtils.isBlank(teacherId)) {
            newExamReportForClazz.setSuccess(false);
            newExamReportForClazz.setDescription("teacherId is blank");
            model.addAttribute("newExamReportForClazz", newExamReportForClazz);
            return path;
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(ConversionUtils.toLong(teacherId));
        if (teacher == null) {
            newExamReportForClazz.setSuccess(false);
            newExamReportForClazz.setDescription("teacher is null");
            model.addAttribute("newExamReportForClazz", newExamReportForClazz);
            return path;
        }

        String newExamId = getRequestString("newExamId");
        if (StringUtils.isBlank(newExamId)) {
            newExamReportForClazz.setSuccess(false);
            newExamReportForClazz.setDescription("newExamId is blank");
            model.addAttribute("newExamReportForClazz", newExamReportForClazz);
            return path;
        }
        Long clazzId = getRequestLong("clazzId");
        if (clazzId == 0L) {
            newExamReportForClazz.setSuccess(false);
            newExamReportForClazz.setDescription("clazzId is error");
            model.addAttribute("newExamReportForClazz", newExamReportForClazz);
            return path;
        }
        newExamReportForClazz = newExamReportLoaderClient.crmReceiveNewExamReportForClazz(teacher, newExamId, clazzId);
        model.addAttribute("newExamReportForClazz", newExamReportForClazz);
        return path;
    }

    @RequestMapping(value = "teachernewhomeworkhistory.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String getTeacherNewHomeworkHistory(Model model) {
        String teacherId = getRequestString("teacherId");
        Integer homeworkDay = getRequestInt("homeworkDay", 30);
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(ConversionUtils.toLong(teacherId));
        PageRequest pageable = new PageRequest(0, 200);
        // 老师基本信息
        CrmTeacherSummary cts = crmTeacherSummaryService.getCrmTeacherSummary(teacherId, "teacherId");
        List<Map<String, Object>> teacherHomeworkHistoryList = new ArrayList<>();
        if (teacher.getKtwelve() == Ktwelve.PRIMARY_SCHOOL || teacher.getKtwelve() == Ktwelve.INFANT) {
            // 获取老师所有班级名称及学生人数
            Map<Long, GroupTeacherMapper> groups = deprecatedGroupLoaderClient.loadTeacherGroups(teacher.getId(), true)
                    .stream().collect(Collectors.toMap(GroupTeacherMapper::getId, Function.identity()));
            Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(groups.values().stream().map(GroupTeacherMapper::getClazzId).collect(toSet()))
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));
            // 获取作业
            Page<NewHomework.Location> page = newHomeworkCrmLoaderClient.loadGroupNewHomeworks(groups.keySet(),
                    DateUtils.nextDay(new Date(), -1 * homeworkDay), new Date(), pageable, true);
            if (page != null && page.hasContent()) {
                for (NewHomework.Location location : page.getContent()) {
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    map.put("isTermEnd", location.getType() != null ? location.getType().name() : NewHomeworkType.Normal.name());
                    map.put("arrangeTime", new Date(location.getCreateTime()));
                    map.put("checkTime", location.getCheckedTime() == 0 ? "" : new Date(location.getCheckedTime()));
                    map.put("homeworkId", location.getId());
                    map.put("groupId", location.getClazzGroupId());
                    map.put("homeworkSubject", location.getSubject());
//                    map.put("source", location.getSource().getDesc());
                    // fix Bug#21209
                    map.put("disabled", location.isDisabled());
                    GroupTeacherMapper group = groups.get(location.getClazzGroupId());
                    List<GroupMapper.GroupUser> students = new ArrayList<>();
                    if (group != null) {
                        students = group.getStudents();
                        map.put("studentCount", students.size());
                        Clazz clazz = clazzs.get(group.getClazzId());
                        if (clazz != null) {
                            map.put("clazzName", clazz.formalizeClazzName());
                            map.put("clazzId", clazz.getId());
                        }
                    }
                    NewAccomplishment acc = newAccomplishmentLoaderClient.loadNewAccomplishment(location);
                    map.put("completeCount", 0);
                    map.put("ipcount", 0);
                    Set<Long> studentIds = students.stream().map(GroupMapper.GroupUser::getId).collect(toSet());
                    if (acc != null && acc.size() > 0) {
                        int completeCount = 0;
                        Set<String> ipSet = new HashSet<>();
                        for (String studentId : acc.getDetails().keySet()) {
                            NewAccomplishment.Detail detail = acc.getDetails().get(studentId);
                            if (studentIds.contains(SafeConverter.toLong(studentId))) {
                                if (StringUtils.isNotBlank(detail.getIp())) ipSet.add(detail.getIp());
                                completeCount++;
                            }
                        }
                        map.put("completeCount", completeCount);
                        map.put("ipcount", ipSet.size());
                    }
                    Map<Long, NewHomeworkResult> results = newHomeworkResultLoaderClient.loadNewHomeworkResult(location, studentIds, false);
                    map.put("joinCount", results.size());
                    if (!teacherId.equals(String.valueOf(location.getTeacherId()))) {
                        map.put("homeworkTeacherId", location.getTeacherId());
                        Teacher homeworkTeacher = teacherLoaderClient.loadTeacher(location.getTeacherId());
                        map.put("homeworkTeacherName", homeworkTeacher.getProfile().getRealname());
                    }
                    teacherHomeworkHistoryList.add(map);
                }
            }
        }
        teacherHomeworkHistoryList.sort((o1, o2) -> {
            if (o1.get("arrangeTime") != null && o2.get("arrangeTime") != null) {
                return ((Date) o1.get("arrangeTime")).getTime() >= ((Date) o2.get("arrangeTime")).getTime() ? -1 : 1;
            } else if (o1.get("arrangeTime") != null) {
                return -1;
            } else {
                return 1;
            }
        });
        model.addAttribute("teacherDetail", teacher);
        model.addAttribute("teacherSummary", cts);
        model.addAttribute("homeworkHistoryList", teacherHomeworkHistoryList);
        model.addAttribute("homeworkDay", homeworkDay);
        model.addAttribute("unuseToToday", (cts == null || cts.getLatestAssignHomeworkTime() == null) ? "" :
                DateUtils.dayDiff(new Date(), new Date(cts.getLatestAssignHomeworkTime())));
        return "crm/teachernew/teachernewhomeworkhistory";
    }

    private static DayRange activityRange = null;
    private static Date invitation2019StartDate = null;
    private static Date invitation2019endDate = null;

    static {
        try {
            invitation2019StartDate = DateUtils.parseDate("2019-01-16", "yyyy-MM-dd");
            invitation2019endDate = DateUtils.parseDate("2019-02-24 23:59:59", "yyyy-MM-dd HH:mm:ss");
            activityRange = new DayRange(invitation2019StartDate.getTime(), invitation2019endDate.getTime());
        } catch (ParseException ig) {
        }
    }

    @RequestMapping(value = "teacherinviteandmentor.vpage")
    public String teacherInviteAndMentorInfo(Model model) {
        Long teacherId = getRequestLong("teacherId");
        CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacherId);

        TeacherDetail loadTeacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        boolean teacherAuth = Objects.equals(loadTeacherDetail.getAuthenticationState(), AuthenticationState.SUCCESS.getState());

        //邀请他人
        List<InviteHistory> inviteHistoryList = asyncInvitationServiceClient.loadByInviter(teacherId).toList()
                .stream().filter(p -> p.getInviteeUserId() != null).collect(Collectors.toList());
        Set<Long> inviteTeacherIdSet = inviteHistoryList.stream().map(InviteHistory::getInviteeUserId).collect(toSet());
        Map<Long, TeacherDetail> teacherMap = teacherLoaderClient.loadTeacherDetails(inviteTeacherIdSet);
//        List<CrmTeacherSummary> teacherSummaryList = crmTeacherSummaryService.getTeacherSummaryListByTeacherIds(inviteTeacherIdSet);
//        Map<String, CrmTeacherSummary> inviteTeacherSummaryMap = new HashMap<>();
//        teacherSummaryList.forEach(p -> inviteTeacherSummaryMap.put(ConversionUtils.toString(p.getTeacherId()), p));
        List<LinkedHashMap<String, Object>> inviteMapList = new ArrayList<>();
        inviteHistoryList.forEach((p) -> {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
//            CrmTeacherSummary inviteCrmTeacher = inviteTeacherSummaryMap.get(ConversionUtils.toString(p.getInviteeUserId()));
            Teacher inviteCrmTeacher = teacherMap.get(p.getInviteeUserId());
            map.put("userName", inviteCrmTeacher == null ? "" : inviteCrmTeacher.fetchRealname());
            map.put("userId", inviteCrmTeacher == null ? "" : inviteCrmTeacher.getId());
            map.put("subject", (inviteCrmTeacher == null || inviteCrmTeacher.getSubject() == null) ? "" : inviteCrmTeacher.getSubject().getValue());
            map.put("createTime", p.getCreateTime());
            map.put("updateTime", p.getUpdateTime());
            map.put("inviteType", p.getInvitationType() == null ? "" : p.getInvitationType().getNote());
            Boolean flag = false;
            if (activityRange.contains(p.getCreateTime())) {
                flag = Objects.equals(p.getIsChecked(), 9);
                if ((!flag) && inviteCrmTeacher != null) {
                    StringBuffer sb = new StringBuffer();
                    if (!teacherAuth) {
                        sb.append("邀请者未认证、");
                    }
                    if (!Objects.equals(inviteCrmTeacher.getAuthenticationState(), AuthenticationState.SUCCESS.getState())) {
                        sb.append("被邀请者未认证");
                    }
                    map.put("reason", sb.toString());
                }
            } else {
                flag = p.getDisabled();
                if (p.getIsChecked() == 9) {
                    flag = true;
                }
            }
            map.put("success", flag);
            inviteMapList.add(map);
        });
        //我帮助他人
        List<MentorHistory> iHelpedList = mentorServiceClient.getRemoteReference()
                .findMentorHistoriesByMentorId(teacherId)
                .getUninterruptibly();
        Set<Long> iHelpedTeacherIdSet = iHelpedList.stream().map(MentorHistory::getMenteeId).collect(toSet());
        List<CrmTeacherSummary> iHelpedTeacherSummaryList = crmTeacherSummaryService.getTeacherSummaryListByTeacherIds(iHelpedTeacherIdSet);
        Map<String, CrmTeacherSummary> iHelpedTeacherSummaryMap = new HashMap<>();
        iHelpedTeacherSummaryList.forEach(p -> iHelpedTeacherSummaryMap.put(ConversionUtils.toString(p.getTeacherId()), p));
        List<LinkedHashMap<String, Object>> helpOtherMapList = new ArrayList<>();
        iHelpedList.forEach((p) -> {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            CrmTeacherSummary iHelpedCrmTeacher = iHelpedTeacherSummaryMap.get(ConversionUtils.toString(p.getMenteeId()));
            map.put("userName", iHelpedCrmTeacher == null ? "" : iHelpedCrmTeacher.getRealName());
            map.put("userId", iHelpedCrmTeacher == null ? "" : iHelpedCrmTeacher.getTeacherId());
            map.put("subject", iHelpedCrmTeacher == null ? "" : Subject.ofWithUnknown(iHelpedCrmTeacher.getSubject()).getValue());
            map.put("createTime", p.getCreateDatetime());
            map.put("updateTime", p.getUpdateDatetime());
            map.put("mentorCategory", MentorCategory.of(p.getMentorCategory()).getDescription());
            map.put("success", p.getSuccess());
            helpOtherMapList.add(map);
        });
        //我被他人帮助
        List<MentorHistory> helpedMeList = mentorServiceClient.getRemoteReference()
                .findMentorHistoriesByMenteeId(teacherId)
                .getUninterruptibly();
        Set<Long> helpedMeTeacherIdSet = helpedMeList.stream().map(MentorHistory::getMentorId).collect(toSet());
        List<CrmTeacherSummary> helpedMeTeacherSummaryList = crmTeacherSummaryService.getTeacherSummaryListByTeacherIds(helpedMeTeacherIdSet);
        Map<String, CrmTeacherSummary> helpedMeTeacherSummaryMap = new HashMap<>();
        helpedMeTeacherSummaryList.forEach(p -> helpedMeTeacherSummaryMap.put(ConversionUtils.toString(p.getTeacherId()), p));
        List<LinkedHashMap<String, Object>> helpedMapList = new ArrayList<>();
        helpedMeList.forEach((p) -> {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            CrmTeacherSummary helpedMeCrmTeacher = helpedMeTeacherSummaryMap.get(ConversionUtils.toString(p.getMentorId()));
            map.put("userName", helpedMeCrmTeacher == null ? "" : helpedMeCrmTeacher.getRealName());
            map.put("userId", helpedMeCrmTeacher == null ? "" : helpedMeCrmTeacher.getTeacherId());
            map.put("subject", helpedMeCrmTeacher == null ? "" : Subject.ofWithUnknown(helpedMeCrmTeacher.getSubject()).getValue());
            map.put("createTime", p.getCreateDatetime());
            map.put("updateTime", p.getUpdateDatetime());
            map.put("mentorCategory", MentorCategory.of(p.getMentorCategory()).getDescription());
            map.put("success", p.getSuccess());
            helpedMapList.add(map);
        });
        //关联老师  （关于绑定手机号和解绑手机号的关联老师）
        UserAuthentication authentication = raikouSystem.loadUserAuthentication(teacherId);
        List<Map<String, Object>> bindData = new ArrayList<>();
        if (authentication != null && authentication.isMobileAuthenticated()) {
            // 当前老师有绑定手机号码，查询当前绑定手机号码的解绑记录，列出解绑过该号码的用户。
            String offBindSql = "SELECT TARGET_ID,ADMIN_USER_NAME,COMMENT,CREATE_DATETIME FROM ADMIN_LOG WHERE TARGET_STR IN(?, ?) AND OPERATION = 'cleanupBindedMobile' AND TARGET_ID IS NOT NULL";
            String phone = sensitiveUserDataServiceClient.showUserMobile(authentication.getId(), "crm:search_bind_history", SafeConverter.toString(authentication.getId()));
            String mobileEncoded = sensitiveUserDataServiceClient.encodeMobile(phone);
            List<Map<String, Object>> offBindList = utopiaSqlAdmin.withSql(offBindSql).useParamsArgs(phone, mobileEncoded).queryAll();
            for (Map<String, Object> offMap : offBindList) {
                Long userId = SafeConverter.toLong(offMap.get("TARGET_ID"));
                User user = raikouSystem.loadUser(userId);
                if (user != null) {
                    if (user.fetchUserType() == UserType.TEACHER) {
                        Map<String, Object> teacher = new HashMap<>();
                        teacher.put("userId", userId);
                        teacher.put("userType", user.fetchUserType().getDescription());
                        teacher.put("authtication", user.fetchCertificationState().getDescription());
                        if (teacherSummary != null) {
                            teacher.put("webSource", CrmTeacherWebSourceCategoryType.getCategoryDesc(teacherSummary.getWebSourceCategory()));
                        } else {
                            teacher.put("webSource", CrmTeacherWebSourceCategoryType.getCategoryDesc(null));
                        }

                        teacher.put("log", offMap.get("CREATE_DATETIME").toString() + "  " + offMap.get("COMMENT").toString());
                        teacher.put("adminUser", offMap.get("ADMIN_USER_NAME").toString());
                        bindData.add(teacher);
                    } else {
                        Map<String, Object> bindUserMap = new HashMap<>();
                        bindUserMap.put("userId", userId);
                        bindUserMap.put("userType", user.fetchUserType().getDescription());
                        bindUserMap.put("authtication", "--");
                        bindUserMap.put("webSource", "--");
                        bindUserMap.put("log", offMap.get("CREATE_DATETIME").toString() + "  " + offMap.get("COMMENT").toString());
                        bindUserMap.put("adminUser", offMap.get("ADMIN_USER_NAME").toString());
                        bindData.add(bindUserMap);
                    }
                }
            }
        }

        // 查询当前老师解绑手机历史，并且将解绑过的历史手机号码的当前绑定用户列出来。
        String historyMobileSql = "SELECT TARGET_STR FROM ADMIN_LOG WHERE TARGET_ID = ? AND OPERATION = 'cleanupBindedMobile' AND TARGET_STR IS NOT NULL;";
        List<String> mobiles = utopiaSqlAdmin.withSql(historyMobileSql).useParamsArgs(teacherId).queryColumnValues(String.class);
        if (CollectionUtils.isNotEmpty(mobiles)) {
            for (String mobile : mobiles) {
                List<UserAuthentication> authentications = userLoaderClient.loadMobileAuthentications(mobile);
                if (CollectionUtils.isNotEmpty(authentications)) {
                    for (UserAuthentication auth : authentications) {
                        User user = raikouSystem.loadUser(auth.getId());
                        if (user != null) {
                            if (user.fetchUserType() == UserType.TEACHER) {
                                Map<String, Object> teacher = new HashMap<>();
                                teacher.put("userId", user.getId());
                                teacher.put("userType", user.fetchUserType().getDescription());
                                teacher.put("authtication", user.fetchCertificationState().getDescription());
                                CrmTeacherSummary summary = crmSummaryLoaderClient.loadTeacherSummary(user.getId());
                                if (summary != null) {
                                    teacher.put("webSource", CrmTeacherWebSourceCategoryType.getCategoryDesc(summary.getWebSourceCategory()));
                                } else {
                                    teacher.put("webSource", "--");
                                }
                                teacher.put("log", "--");
                                teacher.put("adminUser", "--");
                                bindData.add(teacher);
                            } else {
                                Map<String, Object> bindUserMap = new HashMap<>();
                                bindUserMap.put("userId", user.getId());
                                bindUserMap.put("userType", user.fetchUserType().getDescription());
                                bindUserMap.put("authtication", "--");
                                bindUserMap.put("webSource", "--");
                                bindUserMap.put("log", "--");
                                bindUserMap.put("adminUser", "--");
                                bindData.add(bindUserMap);
                            }
                        }
                    }
                }
            }
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        model.addAttribute("teacherDetail", teacherDetail);
        model.addAttribute("teacherSummary", teacherSummary);
        model.addAttribute("inviteMapList", inviteMapList);
        model.addAttribute("helpOtherMapList", helpOtherMapList);
        model.addAttribute("helpedMapList", helpedMapList);
        model.addAttribute("bindData", bindData);
        return "crm/teachernew/teacherinviteandmentor";
    }

    @RequestMapping(value = "funnyeventsearch.vpage")
    public String getFunnyEventSearch(Model model) {
        Long teacherId = getRequestLong("teacherId");
//        List<ActivityConfig> activityConfigs = activityConfigServiceClient.getActivityConfigService().loadAllActivityConfigIncludeIsEnd();
        List<ActivityConfig> activityConfigs = activityConfigServiceClient.getActivityConfigService().loadByApplicant(teacherId, 1)
                .stream()
                .filter(p -> p.getCreateTime().after(DateUtils.addDays(new Date(), -90)))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());

        model.addAttribute("activityConfigs", activityConfigs);
        return "crm/teachernew/funnyeventsearch";
    }

    @RequestMapping(value = "funnyeventremove.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage funnyEventRemove() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id) || !ObjectId.isValid(id)) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            activityConfigServiceClient.getActivityConfigService().delete(id);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("funnyEventRemove error, id:{}", id, ex);
            return MapMessage.errorMessage("Failed remove funny envent!");
        }
    }

    @RequestMapping(value = "teacherrewardhistory.vpage")
    public String getTeacherRewardHistory(Model model) {
        Long teacherId = getRequestLong("teacherId");
        CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacherId);

        //金币记录
        Integral nowTeacherIntegral = teacherLoaderClient.loadMainSubTeacherIntegral(teacherId);
        List<IntegralHistory> integralHistoryList = integralHistoryLoaderClient.getIntegralHistoryLoader()
                .loadUserIntegralHistories(teacherId, 3);
        List<Map<String, Object>> integralHistoryMapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(integralHistoryList)) {
            integralHistoryList.forEach(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("integralType", IntegralType.of(p.getIntegralType()).getDescription());
                p.setIntegral(p.getIntegral() / 10);
                map.put("IntegralHistory", p);
                integralHistoryMapList.add(map);

            });
        }

        //话费记录
        List<WirelessCharging> wirelessChargingList = crmTeacherSummaryService.getWirelessChargingList(teacherId);
        List<Map<String, Object>> wirelessChargingMapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(wirelessChargingList)) {
            wirelessChargingList.forEach(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("wirelessType", ChargeType.get(p.getChargeType()).getDescription());
                map.put("wirelessCharging", p);
                wirelessChargingMapList.add(map);
            });
        }

        //任务记录
        MapMessage taskListMessage = teacherTaskLoaderClient.loadCrmTaskList(teacherId);
        if (taskListMessage.isSuccess()) {//任务信息
            model.addAttribute("teacherTask", taskListMessage.get("data"));
        }

        //积分信息
        List<TeacherExpHistory> teacherExpHistoryInfo = teacherLevelServiceClient.getByTeacherIdAndCreateTime(teacherId, new Date(new Date().getTime() - 180 * 24 * 60 * 60 * 1000L));
        TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacherId);
        Integer level = 1;
        Integer exp = 0;
        Date dateLevel = new Date(new Date().getTime() + 180 * 24 * 60 * 60 * 1000L);
        if (teacherExtAttribute != null && teacherExtAttribute.getNewLevel() != null) {
            level = teacherExtAttribute.getNewLevel();
            dateLevel = new Date(teacherExtAttribute.getInitNewExpTime() + 180 * 24 * 60 * 60 * 1000L);
            exp = teacherExtAttribute.getExp();
        }
        List<Map<String, Object>> teacherExpHistory = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(teacherExpHistoryInfo)) {
            for (TeacherExpHistory teacherExpHistoryTmp : teacherExpHistoryInfo) {
                Map<String, Object> info = new HashMap<>();
                teacherExpHistory.add(info);
                info.put("id", teacherExpHistoryTmp.getId());
                info.put("createDatetime", DateUtils.dateToString(teacherExpHistoryTmp.getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
                info.put("teacherId", teacherId);
                info.put("exp", teacherExpHistoryTmp.getExp());
                info.put("comment", teacherExpHistoryTmp.getComment());
                info.put("type", "老师任务奖励");
            }
        }
        Map<String, Object> expInfo = new HashMap<>();
        expInfo.put("level", level);
        expInfo.put("levelName", TeacherExtAttribute.NewLevel.getNewLevelByLevel(level).getValue());
        expInfo.put("exp", exp);
        expInfo.put("levelValidDate", DateUtils.dateToString(dateLevel, DateUtils.FORMAT_SQL_DATETIME));
        expInfo.put("teacherExpHistory", teacherExpHistory);
        model.addAttribute("expInfo", expInfo);


        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        model.addAttribute("teacherDetail", teacherDetail);
        model.addAttribute("teacherSummary", teacherSummary);
        model.addAttribute("nowIntegral", nowTeacherIntegral.getUsableIntegral());
        model.addAttribute("integralHistoryMapList", integralHistoryMapList);
        model.addAttribute("wirelessChargingMapList", wirelessChargingMapList);
        return "crm/teachernew/teacherrewardhistory";
    }

    @RequestMapping(value = "teacherrewardstudenthistory.vpage")
    public String getTeacherrewardstudenthistory(Model model) {
        Long teacherId = getRequestLong("teacherId");
        String startTimeStr = getRequestString("startDate");
        String endTimeStr = getRequestString("endDate");

        if (StringUtils.isBlank(startTimeStr)) {
            startTimeStr = DateUtils.dateToString(DateUtils.calculateDateDay(new Date(), -30), DateUtils.FORMAT_SQL_DATE);
        }

        if (StringUtils.isBlank(endTimeStr)) {
            endTimeStr = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
        }

        model.addAttribute("startDate", startTimeStr);
        model.addAttribute("endDate", endTimeStr);
        model.addAttribute("teacherId", teacherId);
        final Date startTime = StringUtils.isNotBlank(startTimeStr) ? DateUtils.stringToDate(startTimeStr, DateUtils.FORMAT_SQL_DATE) : DateUtils.calculateDateDay(new Date(), -30);
        final Date endTime = StringUtils.isNotBlank(endTimeStr) ? DayRange.newInstance(DateUtils.stringToDate(endTimeStr, DateUtils.FORMAT_SQL_DATE).getTime()).getEndDate() : new Date();

        List<SmartClazzIntegralHistory> smartClazzIntegralHistorys = clazzIntegralServiceClient.getClazzIntegralService()
                .loadSmartClazzIntegralHistoryByAddIntegralUserId(teacherId)
                .getUninterruptibly()
                .stream()
                .filter(source -> source.getCreateDatetime() != null)
                .filter(source -> source.getCreateDatetime().after(startTime) && source.getCreateDatetime().before(endTime))
                .collect(Collectors.toList());

        Set<Long> studentIds = smartClazzIntegralHistorys.stream().map(SmartClazzIntegralHistory::getUserId).collect(toSet());
        Map<Long, Student> studentMap = studentLoaderClient.loadStudents(studentIds);

        List<Map<String, Object>> rewardStudentHistoryList = new ArrayList<>();
        int totalIntegral = 0;
        for (SmartClazzIntegralHistory smartClazzIntegralHistory : smartClazzIntegralHistorys) {
            Map<String, Object> smartClazzIntegralHistoryMap = new HashMap<>();
            smartClazzIntegralHistoryMap.put("studentId", smartClazzIntegralHistory.getUserId());
            smartClazzIntegralHistoryMap.put("clazzId", smartClazzIntegralHistory.getClazzId());
            smartClazzIntegralHistoryMap.put("smartClazzRewardItem", smartClazzIntegralHistory.getRewardType() != null ? smartClazzIntegralHistory.getRewardType().getValue() : null);
            smartClazzIntegralHistoryMap.put("createTime", smartClazzIntegralHistory.getCreateDatetime());
            smartClazzIntegralHistoryMap.put("groupId", smartClazzIntegralHistory.getGroupId());
            smartClazzIntegralHistoryMap.put("studentName", studentMap.containsKey(smartClazzIntegralHistory.getUserId()) ? studentMap.get(smartClazzIntegralHistory.getUserId()).fetchRealname() : null);

            Integer integral = smartClazzIntegralHistory.getIntegral();
            smartClazzIntegralHistoryMap.put("integral", integral);
            rewardStudentHistoryList.add(smartClazzIntegralHistoryMap);

            totalIntegral += integral != null ? integral : 0;
        }

        model.addAttribute("rewardStudentHistoryList", rewardStudentHistoryList);
        model.addAttribute("totalIntegral", totalIntegral);
        return "crm/teachernew/teacherrewardstudenthistory";
    }

    @RequestMapping(value = "teacherintegralwhereaboutsrecord.vpage")
    public String getTeacherintegralwhereaboutsrecord(Model model) {

        Long teacherId = getRequestLong("teacherId");
        String startTimeStr = getRequestString("startDate");
        String endTimeStr = getRequestString("endDate");

        if (StringUtils.isBlank(startTimeStr)) {
            startTimeStr = DateUtils.dateToString(DateUtils.calculateDateDay(new Date(), -30), DateUtils.FORMAT_SQL_DATE);
        }

        if (StringUtils.isBlank(endTimeStr)) {
            endTimeStr = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
        }

        model.addAttribute("startDate", startTimeStr);
        model.addAttribute("endDate", endTimeStr);
        model.addAttribute("teacherId", teacherId);
        final Date startTime = StringUtils.isNotBlank(startTimeStr) ? DateUtils.stringToDate(startTimeStr, DateUtils.FORMAT_SQL_DATE) : DateUtils.calculateDateDay(new Date(), -30);
        final Date endTime = StringUtils.isNotBlank(endTimeStr) ? DayRange.newInstance(DateUtils.stringToDate(endTimeStr, DateUtils.FORMAT_SQL_DATE).getTime()).getEndDate() : new Date();

        /*Set<Long> studentsIds = studentLoaderClient.loadTeacherStudents(teacherId)
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());*/
        List<Long> groupIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .getTeacherGroupIds(teacherId);

        Set<Long> studentIds = studentLoaderClient.loadGroupStudentIds(groupIds)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(toSet());

        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIds);

        List<IntegralHistory> integralHistorys = new ArrayList<>();

        Date cacheStartDate = DateUtils.addDays(new Date(), -90);

        for (Long studentId : studentIds) {
            // 三个月以内走缓存,以外不走缓存
            if (startTime.after(cacheStartDate)) {
                integralHistorys.addAll(integralHistoryLoaderClient.getIntegralHistoryLoader()
                        .loadUserIntegralHistories(studentId));
            } else {
                integralHistorys.addAll(integralHistoryLoaderClient.getIntegralHistoryLoader()
                        .loadUserIntegralHistories(studentId, startTime, endTime));
            }
        }
        integralHistorys = integralHistorys.stream()
                .filter(o -> o.getCreatetime().after(startTime) && o.getCreatetime().before(endTime))
                .sorted((o1, o2) -> Long.compare(o2.getCreatetime().getTime(), o1.getCreatetime().getTime()))
                .collect(Collectors.toList());

        List<Map<String, Object>> teacherIntegralWhereaboutsRecordsMaps = new ArrayList<>();
        for (IntegralHistory integralHistory : integralHistorys) {
            StudentDetail studentDetail = studentDetailMap.get(integralHistory.getUserId());
            Map<String, Object> teacherIntegralWhereaboutsRecordsMap = new HashMap<>();
            teacherIntegralWhereaboutsRecordsMap.put("Id", integralHistory.getId());
            teacherIntegralWhereaboutsRecordsMap.put("studentId", integralHistory.getUserId());
            teacherIntegralWhereaboutsRecordsMap.put("studentName", studentDetail.fetchRealname());
            teacherIntegralWhereaboutsRecordsMap.put("studentClazzId", studentDetail.getClazzId());
            teacherIntegralWhereaboutsRecordsMap.put("studentClazzName", studentDetail.getClazz() != null ? studentDetail.getClazz().formalizeClazzName() : null);
            teacherIntegralWhereaboutsRecordsMap.put("createTime", integralHistory.getCreatetime());
            teacherIntegralWhereaboutsRecordsMap.put("integral", integralHistory.getIntegral());
            teacherIntegralWhereaboutsRecordsMap.put("comment", integralHistory.getComment());
            teacherIntegralWhereaboutsRecordsMap.put("totalIntegralBefore", integralHistory.getTotalIntegralBefore());
            teacherIntegralWhereaboutsRecordsMap.put("totalIntegralAfter", integralHistory.getTotalIntegralAfter());
            teacherIntegralWhereaboutsRecordsMaps.add(teacherIntegralWhereaboutsRecordsMap);
        }

        model.addAttribute("records", teacherIntegralWhereaboutsRecordsMaps);
        return "crm/teachernew/teacherintegralwhereaboutsrecord";
    }

    @RequestMapping(value = "appteacherlog.vpage")
    public String getAppTeacherLog(Model model) {
        Long teacherId = getRequestLong("teacherId");
        model.addAttribute("teacherId", teacherId);
        return "crm/teachernew/appteacherlog";
    }

    @RequestMapping(value = "teacherwechathistory.vpage")
    public String getTeacherWechatHistory(Model model) {
        Long teacherId = getRequestLong("teacherId");

        Map<Long, List<UserWechatRef>> dataMap = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(teacherId), WechatType.TEACHER);
        List<UserWechatRef> userWechatRefs = dataMap.get(teacherId);
        if (CollectionUtils.isNotEmpty(userWechatRefs)) {
            Collections.sort(userWechatRefs, (o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime()));
            List<Map<String, Object>> wechats = new ArrayList<>();
            for (UserWechatRef uwr : userWechatRefs) {
                Map<String, Object> item = new HashMap<>();
                item.put("USER_ID", uwr.getUserId());
                item.put("OPEN_ID", uwr.getOpenId());
                item.put("DISABLED", uwr.getDisabled());
                item.put("CREATE_DATETIME", uwr.getCreateDatetime());
                item.put("UPDATE_DATETIME", uwr.getUpdateDatetime());
                item.put("SOURCE", uwr.getSource());
                wechats.add(item);
            }
            model.addAttribute("wechats", wechats);
        }

        return "crm/teachernew/teacherwechathistory";
    }

    @RequestMapping(value = "teacherphonecall.vpage")
    public String teacherPhoneCall(Model model) {
        String teacherId = getRequestString("teacherId");
        CrmTeacherSummary teacherSummary = crmTeacherSummaryService.getCrmTeacherSummary(teacherId, "teacherId");
        AuthCurrentAdminUser currentAdminUser = getCurrentAdminUser();
        model.addAttribute("adminUser", currentAdminUser);
        model.addAttribute("teacherSummary", teacherSummary);
        return "crm/teachernew/teacherphonecall";
    }

    @RequestMapping(value = "faketeacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage fakeTeacher() {
        Long teacherId = getRequestLong("teacherId");
        String desc = getRequestString("desc");
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("请填写判定说明");
        }
        Set<Long> teacherMainSubIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        Set<Long> receivers = new HashSet<>();
        for (Long tid : teacherMainSubIds) {
            // 加载出相关换班申请
            List<ClazzTeacherAlteration> alterations = teacherLoaderClient.loadApplicantOrRespondentAlterations(tid)
                    .stream()
                    .filter(alteration -> ClazzTeacherAlterationState.PENDING == alteration.getState())
                    .collect(toList());

            // 然后全部取消
            if (CollectionUtils.isNotEmpty(alterations)) {
                for (ClazzTeacherAlteration alteration : alterations) {
                    teacherAlterationServiceClient.cancelApplication(
                            alteration.getApplicantId(), alteration.getId(),
                            alteration.getType(), OperationSourceType.crm
                    );
                    // 取消完了之后给老师发消息
                    Long receiver = null;
                    if (Objects.equals(tid, alteration.getApplicantId())) {
                        receiver = alteration.getRespondentId();
                    } else if (Objects.equals(tid, alteration.getRespondentId())) {
                        receiver = alteration.getApplicantId();
                    }
                    CollectionUtils.addNonNullElement(receivers, receiver);
                }
            }
        }

        MapMessage message = crmSummaryServiceClient.updateTeacherFakeType(teacherId, CrmTeacherFakeValidationType.MANUAL_VALIDATION, desc);
        if (message.isSuccess()) {
            // 发送申诉消息
            miscServiceClient.sendFakeAppealMessage(teacherId);

            // 发送通知消息
            miscServiceClient.sendFakeNoticeMessage(teacherId, receivers);

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.老师判假.name());
            userServiceRecord.setOperationContent("老师判假");
            userServiceRecord.setComments("判定原因[" + desc + "]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }
        return message;
    }

    @RequestMapping(value = "regionlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public List<Map<String, Object>> getRegionList(HttpServletRequest request) {
        List<Map<String, Object>> regionMapList = new ArrayList<>();
        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("code", -1);
        rootMap.put("name", "全部");
        regionMapList.add(rootMap);
        Integer reginCode = ConversionUtils.toInt(request.getParameter("regionCode"));
        Map<Integer, ExRegion> childRegionMap = raikouSystem.getRegionBuffer().loadChildRegions(reginCode)
                .stream()
                .collect(Collectors.toMap(ExRegion::getCode, Function.identity(),
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new));
        for (Integer i : childRegionMap.keySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", String.valueOf(i));
            map.put("name", childRegionMap.get(i).getName());
            regionMapList.add(map);
        }
        return regionMapList;
    }

    @RequestMapping(value = "teachernewrewardorder.vpage", method = RequestMethod.GET)
    public String teacherNewRewardOrder(Model model) {
        Long teacherId;
        try {
            teacherId = SafeConverter.toLong(getRequestParameter("teacherId", ""));
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("用户ID " + getRequestParameter("teacherId", "") + " 不合规范。");
            return redirect("/crm/teachernew/index.vpage");
        }
        try {
            //List<RewardOrderMapper> orders = rewardLoaderClient.generateUserRewardOrderMappers(teacherId);
            List<RewardOrderMapper> orders = crmRewardService.generateUserRewardOrderMapper(teacherId);
            model.addAttribute("orders", orders);
            model.addAttribute("orderStatus", RewardOrderStatus.values());
        } catch (Exception ex) {
            logger.warn("读取用户兑换信息失败", ex);
        }
        return "/crm/teachernew/teachernewrewardorder";
    }

    @RequestMapping(value = "getTeacher.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTeacher() {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (!schoolId.equals(school.getId())) {
            return MapMessage.errorMessage("请输入本校老师ID");
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        Map<String, String> result = new HashMap<>();
        result.put("teacherName", teacher.getProfile().getRealname());
        result.put("subject", teacher.getSubject().getValue());
        result.put("teacherId", ConversionUtils.toString(teacher.getId()));
        String mobile = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "带班转校查询老师手机号", SafeConverter.toString(teacher.getId()));
        result.put("mobile", mobile);
        return MapMessage.successMessage().add("teacherInfo", result);
    }

    @RequestMapping(value = "getGroups.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroups() {
        Long teacherId = getRequestLong("teacherId");
        List<GroupTeacherMapper> groupTeacherMapperList = deprecatedGroupLoaderClient.loadTeacherGroups(teacherId, false);
        List<Map<String, String>> result = new ArrayList<>();
        for (GroupTeacherMapper groupTeacherMapper : groupTeacherMapperList) {
            if (RefStatus.INVALID == groupTeacherMapper.getTeacherGroupRefStatusMap().get(teacherId)) {
                continue;
            }
            Map<String, String> map = new HashMap<>();
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(groupTeacherMapper.getClazzId());
            if (Objects.isNull(clazz) || clazz.isTerminalClazz()) {
                continue;
            }
            map.put("clazzName", clazz.formalizeClazzName());
            map.put("groupId", ConversionUtils.toString(groupTeacherMapper.getId()));
            map.put("groupParent", groupTeacherMapper.getGroupParent());
            result.add(map);
        }
        return MapMessage.successMessage().add("groupList", result);
    }

    @RequestMapping(value = "getGroupTeachers.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroupTeachers() {
        Long groupId = getRequestLong("groupId");
        Set<Long> groupIds = deprecatedGroupLoaderClient.loadSharedGroupIds(groupId);
        List<Teacher> teacherList = teacherLoaderClient.loadGroupTeacher(groupIds, RefStatus.VALID)
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        List<Map<String, String>> result = new ArrayList<>();
        for (Teacher teacher : teacherList) {
            Map<String, String> map = new HashMap<>();
            map.put("teacherName", teacher.getProfile().getRealname());
            map.put("subject", teacher.getSubject().getValue());
            map.put("teacherId", ConversionUtils.toString(teacher.getId()));
            String mobile = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "带班转校查询老师手机号", SafeConverter.toString(teacher.getId()));
            map.put("mobile", mobile);
            result.add(map);
        }
        return MapMessage.successMessage().add("groupTeacherList", result);
    }


    // FIXME 下面这一坨真是不太敢动啊，在上面套一层数据校验及返回吧
    @RequestMapping(value = "changeschoolpre.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeSchoolPre() {
        String teacherIdsData = getRequestString("teacherIds");
        String allTeacherIdsData = getRequestString("allTeacherIds");
        String groupsData = getRequestString("groups");
        boolean check = getRequestBool("check");

        List<Long> teacherIds = JsonUtils.fromJsonToList(teacherIdsData, Long.class);
        List<Long> realTeacherIds = JsonUtils.fromJsonToList(teacherIdsData, Long.class);
        List<Long> allTeacherIds = JsonUtils.fromJsonToList(allTeacherIdsData, Long.class);
        List<Map> groupMap = JsonUtils.fromJsonToList(groupsData, Map.class);
        Map<String, List<Long>> groupParentGroupIdsMap = new HashMap<>();
        Set<Long> groupIdsWithoutGroupParent = new HashSet<>();
        Set<Long> groupIdList = new HashSet<>();
        for (Map map : groupMap) {
            Long groupId = ConversionUtils.toLong(map.get("groupId"));
            String groupParent = ConversionUtils.toString(map.get("groupParent"));
            if (StringUtils.isEmpty(groupParent)) {
                groupIdsWithoutGroupParent.add(groupId);
                continue;
            }
            if (!groupParentGroupIdsMap.containsKey(groupParent)) {
                List<Long> groupIds = new ArrayList<>();
                groupIds.add(groupId);
                groupParentGroupIdsMap.put(groupParent, groupIds);
            } else {
                List<Long> groupIds = groupParentGroupIdsMap.get(groupParent);
                groupIds.add(groupId);
                groupParentGroupIdsMap.put(groupParent, groupIds);
            }
        }
        groupIdList = groupParentGroupIdsMap.values().stream().flatMap(Collection::stream).collect(toSet());
        groupIdList.addAll(groupIdsWithoutGroupParent);
        for (int i = 0; i < teacherIds.size(); i++) {
            if (Objects.nonNull(teacherIds.get(i))) {
                // 要考虑包班制的处理
                Set<Long> teacherMainSubIds = teacherLoaderClient.loadRelTeacherIds(teacherIds.get(i));
                teacherMainSubIds.remove(teacherIds.get(i));
                teacherIds.removeAll(teacherMainSubIds);
                List<GroupTeacherMapper> groupTeacherMappers = deprecatedGroupLoaderClient.loadTeacherGroups(teacherMainSubIds, false)
                        .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(groupTeacherMappers)) {
                    continue;
                }
                Set<Long> groupIds = groupTeacherMappers.stream().map(GroupTeacherMapper::getId).collect(Collectors.toSet());
                groupIdList.addAll(groupIds);
            }
        }
        if (check) {
            // 这个逻辑太闹听了，要拿学生取查老师，再判断通过学生取到的老师是不是都在传过来的teacherIds中

            Map<Long, List<User>> groupStudentsMap = studentLoaderClient.loadGroupStudents(groupIdList);
            Map<Long, User> studentMap = groupStudentsMap.values()
                    .stream().flatMap(Collection::stream).collect(Collectors.toMap(User::getId, Function.identity(), (u, v) -> u));
            List<Long> studentIds = groupStudentsMap.values()
                    .stream().flatMap(Collection::stream).map(User::getId).collect(Collectors.toList());
            Map<Long, List<GroupMapper>> studentGroupsMap = deprecatedGroupLoaderClient.loadStudentGroups(studentIds, false);
            List<Long> groupIds = studentGroupsMap.values()
                    .stream().flatMap(Collection::stream).map(GroupMapper::getId).collect(Collectors.toList());
            Map<Long, List<Teacher>> groupTeacherMap = teacherLoaderClient.loadGroupTeacher(groupIds);

            List<Map<String, Object>> result = new ArrayList<>();
            for (Long studentId : studentIds) {
                List<GroupMapper> groupMappers = studentGroupsMap.get(studentId);
                for (GroupMapper groupMapper : groupMappers) {
                    List<Teacher> teachers = groupTeacherMap.get(groupMapper.getId());
                    for (Teacher teacher : teachers) {
                        if (!realTeacherIds.contains(teacher.getId())) {
                            Map<String, Object> map = new HashMap<>();
                            Map<String, String> teacherInfo = new HashMap<>();
                            Map<String, String> studentInfo = new HashMap<>();
                            teacherInfo.put("teacherName", teacher.fetchRealname());
                            teacherInfo.put("subject", teacher.getSubject().getValue());
                            teacherInfo.put("teacherId", ConversionUtils.toString(teacher.getId()));
                            map.put("teacherInfo", teacherInfo);
                            User student = studentMap.get(studentId);
                            studentInfo.put("studentName", student.fetchRealname());
                            studentInfo.put("studentId", ConversionUtils.toString(studentId));
                            map.put("studentInfo", studentInfo);
                            result.add(map);
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(result)) {
                return MapMessage.errorMessage().add("result", result);
            }
            return MapMessage.successMessage();
        }

        Long schoolId = getRequestLong("schoolId");
        String changeSchoolDesc = getRequestString("changeSchoolDesc");
        if (schoolId == 0 || StringUtils.isEmpty(changeSchoolDesc)) {
            return MapMessage.errorMessage("请检查填写的内容，注意描述信息不能为空");
        }

        // 共享分组中不转校的老师，分组内学生删之
        allTeacherIds.removeAll(realTeacherIds);
        List<Long> unMarkedTeacherIds = new ArrayList<>(); // 页面未选中的老师
        allTeacherIds.forEach(t -> {
            if (!realTeacherIds.contains(t)) {
                unMarkedTeacherIds.add(t);
            }
        });
        List<GroupTeacherMapper> groupTeacherMapperList = deprecatedGroupLoaderClient.loadTeacherGroups(unMarkedTeacherIds, false)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        for (GroupTeacherMapper groupTeacherMapper : groupTeacherMapperList) {
            if (groupParentGroupIdsMap.keySet().contains(groupTeacherMapper.getGroupParent())) {
                Long groupId = groupTeacherMapper.getId();
                groupServiceClient.shareGroups(Collections.singleton(groupId), false);
                List<GroupStudentTuple> groupStudentRefs = raikouSDK.getClazzClient()
                        .getGroupStudentTupleServiceClient()
                        .getGroupStudentTupleService()
                        .dbFindByGroupIdIncludeDisabled(groupId)
                        .getUninterruptibly()
                        .stream()
                        .filter(e -> !e.isDisabledTrue())
                        .collect(toList());
                if (CollectionUtils.isNotEmpty(groupStudentRefs)) {
                    groupStudentRefs.forEach(p -> raikouSDK.getClazzClient()
                            .getGroupStudentTupleServiceClient()
                            .getGroupStudentTupleService()
                            .disable(p.getId())
                            .awaitUninterruptibly());
                }
                List<GroupKlxStudentRef> groupKlxStudentRefs = asyncGroupServiceClient.getAsyncGroupService().findGroupKlxStudentRefsByGroup(groupId).getUninterruptibly();
                if (CollectionUtils.isNotEmpty(groupKlxStudentRefs)) {
                    newKuailexueServiceClient.disableGroupKlxStudentRefs(groupKlxStudentRefs);
                }
            }
        }


        // 下面该做解除共享分组关系，执行转校操作, 添加回原共享分组关系
        for (String key : groupParentGroupIdsMap.keySet()) {
            if (groupParentGroupIdsMap.get(key).size() == 1) {// 共享分组不转班情况
                continue;
            }
            List<Long> gIds = groupParentGroupIdsMap.get(key);
            groupServiceClient.shareGroups(gIds, false);
        }
        for (Long teacherId : teacherIds) {
            // 要考虑包班制的处理
            Set<Long> teacherMainSubIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
            teacherMainSubIds.remove(teacherId);
            teacherIds.removeAll(teacherMainSubIds);
        }

        for (Long teacherId : teacherIds) {
            MapMessage msg = this.changeSchoolNew(teacherId, schoolId, changeSchoolDesc, groupIdList, realTeacherIds);
            if (!msg.isSuccess()) {
                for (String key : groupParentGroupIdsMap.keySet()) { // 转校失败了，要恢复转校前的分组关系
                    List<Long> gIds = groupParentGroupIdsMap.get(key);
                    groupServiceClient.shareGroups(gIds, true);
                }
                return msg;
            }
        }
        for (String key : groupParentGroupIdsMap.keySet()) {
            List<Long> gIds = groupParentGroupIdsMap.get(key);
            groupServiceClient.shareGroups(gIds, true);
        }
        return MapMessage.successMessage();
    }

    // FIXME 前方高能预警，这是一段超过300行的方法，请紧张的往下看
    // 把修改学校的校验逻辑拆除去了，修改学校不是带班转校

    private MapMessage changeSchoolNew(Long teacherId, Long schoolId, String changeSchoolDesc, Set<Long> groupIdList, List<Long> teacherIds) {

        School school = raikouSystem.loadSchool(schoolId);
        if (school == null)
            return MapMessage.errorMessage("学校ID" + schoolId + "不存在");

        School sourceSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (sourceSchool == null) {
            return MapMessage.errorMessage("老师不在任何学校中");
        }

        Set<Long> teacherMainSubIds = teacherLoaderClient.loadRelTeacherIds(teacherId);

        //fix  包班制下一个老师的主副账号的group相互关联时,允许带班换校
        Set<Long> filterGroupIds = deprecatedGroupLoaderClient.loadTeacherGroups(teacherMainSubIds, false).values()
                .stream()
                .flatMap(Collection::stream)
                .map(GroupTeacherMapper::getId)
                .collect(Collectors.toSet());
        List<Set<Long>> shareGroupIdSetList = null; //不为空时,说明主副账号中的group关联了
        if (teacherMainSubIds.size() > 0) {
            Map<String, Set<Long>> tempMap = new HashMap<>();
            Map<Long, GroupMapper> groupMapperMap = deprecatedGroupLoaderClient.loadGroups(filterGroupIds, false);
            for (GroupMapper groupMapper : groupMapperMap.values()) {
                if (StringUtils.isNotBlank(groupMapper.getGroupParent())) {
                    if (!tempMap.containsKey(groupMapper.getGroupParent())) {
                        tempMap.put(groupMapper.getGroupParent(), new HashSet<>());
                    }
                    tempMap.get(groupMapper.getGroupParent()).add(groupMapper.getId());
                }
            }
            shareGroupIdSetList = tempMap.values().stream().filter(t -> t.size() > 1).collect(Collectors.toList());
        }

        // 合并主副账号的分组信息
        List<GroupMapper> teacherMainSubAllGroupMapperList = new ArrayList<>();
        Set<Long> teacherMainSubAllGroupIds = new HashSet<>();
        for (Long tempTeacherId : teacherMainSubIds) {
            List<GroupMapper> groupMapperList = deprecatedGroupLoaderClient.loadTeacherGroupsByTeacherId(tempTeacherId, false);
            Set<Long> teacherGroupIds = groupMapperList.stream().map(GroupMapper::getId).collect(toSet());
            if (CollectionUtils.isNotEmpty(teacherGroupIds)) {
                teacherMainSubAllGroupIds.addAll(teacherGroupIds);
            }
            if (CollectionUtils.isNotEmpty(groupMapperList)) {
                teacherMainSubAllGroupMapperList.addAll(groupMapperList);
            }
        }
        Map<Long, Set<Long>> sharedGroupIds = deprecatedGroupLoaderClient.loadSharedGroupIds(teacherMainSubAllGroupIds);
        for (Long key : sharedGroupIds.keySet()) {
            Set<Long> groupIds = sharedGroupIds.get(key);
            if (CollectionUtils.isNotEmpty(shareGroupIdSetList)) {
                groupIds = sharedGroupIds.get(key).stream().filter(aLong -> !filterGroupIds.contains(aLong)).collect(toSet());
            }
            if (groupIds.size() > 0) {
                if (groupIdList.contains(key)) {// 副账号默认同主账号转校，要解除副账号关联的分组1（此分组1和其他分组2共享，但那个分组2不转校）
                    continue;
                }
                Group group = groupLoaderClient.getGroupLoader().loadGroup(key).getUninterruptibly();
                List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(key);
                if (teachers.size() > 1) {
                    return MapMessage.errorMessage("未找到唯一匹配的老师");
                }
                Long tId = Objects.isNull(teachers.get(0)) ? null : teachers.get(0).getId();
                groupServiceClient.disableTeacherGroup(tId, group.getClazzId(), true);
//                return MapMessage.errorMessage("老师(ID:" + teacherId + ")所在组(" + key + ")关联着其他老师，不能修改学校");
            }
        }

        List<GroupTeacherMapper> groupTeacherMappers = deprecatedGroupLoaderClient.loadTeacherGroups(teacherId, false);
        List<Long> groupIds = groupTeacherMappers.stream().map(GroupTeacherMapper::getId).collect(toList());

        for (GroupTeacherMapper group : groupTeacherMappers) {// 老师关联的但不转校的分组解除关系
            if (!groupIdList.contains(group.getId())) {
                groupServiceClient.disableTeacherGroup(teacherId, group.getClazzId(), true);
                groupIds.remove(group.getId());
            }
        }

        Map<Long, Set<Long>> sharedGroupIdList = deprecatedGroupLoaderClient.loadSharedGroupIds(groupIds);
        for (Long key : sharedGroupIdList.keySet()) {// 如果老师所在组的共享组也在转校数据内则继续，否则解除关系
            Set<Long> groupIdSet = sharedGroupIdList.get(key);
            for (Long groupId : groupIdSet) {
                if (!groupIdList.contains(groupId)) {
                    List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(groupId);
                    if (teachers.size() > 1) {
                        return MapMessage.errorMessage("解除关联分组未找到唯一匹配的老师");
                    }
                    Group group = groupLoaderClient.getGroupLoader().loadGroup(groupId).getUninterruptibly();
                    Long tId = Objects.isNull(teachers.get(0)) ? null : teachers.get(0).getId();
                    groupServiceClient.disableTeacherGroup(tId, group.getClazzId(), true);
                }
            }
        }

        // FIX BUG
        // 需要检查老师名下的学生是否在其他组，如果在则不能转校
        // 如果学生在无老师的组内,断开学生与无老师组的关联,允许转校
        // FIXME 这里之前考虑过快乐学的虚拟学生吗
        Map<Long, List<Long>> studentIdMap = studentLoaderClient.loadGroupStudentIds(teacherMainSubAllGroupIds);
        Set<Long> studentIdSet = studentIdMap.values().stream().flatMap(Collection::stream).collect(toSet());
        Map<Long, List<GroupMapper>> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentIdSet, false);
        Map<Long, Set<Long>> groupStudentsNeedDelete = new HashMap<>();//在无老师组的情况下,保存需要断开关联的学生和组
        for (Map.Entry<Long, List<GroupMapper>> entry : studentGroups.entrySet()) {
            Long u = entry.getKey();
            List<GroupMapper> gs = entry.getValue();
            int teacherNum = 0;
            for (GroupMapper tempGroupMapper : gs) {
                Long tempGroupId = tempGroupMapper.getId();
                //fix  包班制下一个老师的主副账号的group相互关联时,允许带班换校
                if (CollectionUtils.isNotEmpty(shareGroupIdSetList)) {
                    boolean checkFlag = false;
                    for (Set<Long> longSet : shareGroupIdSetList) {
                        if (longSet.contains(tempGroupId)) {
                            checkFlag = true;
                            break;
                        }
                    }
                    if (checkFlag) {
                        continue;
                    }
                }

                List<GroupTeacherTuple> groupTeacherRefs = raikouSDK.getClazzClient()
                        .getGroupTeacherTupleServiceClient()
                        .findByGroupId(tempGroupId);
                if (CollectionUtils.isNotEmpty(groupTeacherRefs)) {
                    if (!teacherIds.contains(groupTeacherRefs.get(0).getTeacherId())) {
                        teacherNum++;
                    }
                } else {
                    if (!groupStudentsNeedDelete.containsKey(tempGroupId)) {
                        groupStudentsNeedDelete.put(tempGroupId, new HashSet<>());
                    }
                    groupStudentsNeedDelete.get(tempGroupId).add(entry.getKey());
                }
            }
            if (teacherNum > 1) {
                return MapMessage.errorMessage("学生" + u + "关联着其他老师，不能修改学校");
            }
        }


        if (CollectionUtils.isNotEmpty(groupStudentsNeedDelete.keySet())) {
            groupStudentsNeedDelete.forEach((groupId, studentIds) -> {
                // FIXME 这个地方是不是要处理 GroupKlxStudentRef
                groupServiceClient.removeStudents(studentIds, groupId);
            });
        }

        //处理大使
        MapMessage ambassadorMapMessage = teacherAmbassadorCancel(teacherMainSubIds);
        if (!ambassadorMapMessage.isSuccess()) {
            return ambassadorMapMessage;
        }


        School originalSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (originalSchool == null) {
            return MapMessage.errorMessage("老师(ID:" + teacherId + ")没有学校");
        }

        if (Objects.equals(originalSchool.getId(), schoolId)) {
            return MapMessage.errorMessage("修改前后的学校不能为同一学校");
        }

        if (!Objects.equals(school.getLevel(), originalSchool.getLevel()) && teacherMainSubAllGroupMapperList.size() > 0) {
            return MapMessage.errorMessage("该老师(ID:" + teacherId + ")目前有执教班级,不能跨学段更换学校");
        }

        // 学制要跟新学校一致
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        Ktwelve ktwelve;
        switch (schoolLevel) {
            case HIGH:
                ktwelve = Ktwelve.SENIOR_SCHOOL;
                break;
            case MIDDLE:
                ktwelve = Ktwelve.JUNIOR_SCHOOL;
                break;
            case INFANT:
                ktwelve = Ktwelve.INFANT;
                break;
            default:
                ktwelve = Ktwelve.PRIMARY_SCHOOL;
        }
        Map<Long, Teacher> mainSubTeachers = teacherLoaderClient.loadTeachers(teacherMainSubIds);
        for (Long tid : teacherMainSubIds) {
            Teacher teacher = mainSubTeachers.get(tid);
            if (teacher == null) {
                return MapMessage.errorMessage("老师不存在");
            }
            teacherServiceClient.setTeacherSubject(tid, teacher.getSubject(), ktwelve);
        }

        //fix 判断包班制情况中主副账号下的group是否关联,如果关联,需要特殊处理,换校前断开关联,换校后在进行关联
//        final long remindTeacherId = teacherId;
//        if (CollectionUtils.isNotEmpty(shareGroupIdSetList)) {
//            shareGroupIdSetList.forEach(groupIds -> {
//                groupServiceClient.shareGroups(groupIds, false);
//                logger.info("带班转校时,老师" + remindTeacherId + "包班制主副账号下的group关联,先断开关联" + groupIds);
//            });
//        }
        // 转校前释放填涂号
        newKuailexueServiceClient.clearTeacherStudentScanNumber(originalSchool.getId(), teacherId);
        UserServiceRecord userServiceRecord = null;

        // 包班制副账号存在但是没有关联的分组，或是副账号没有选中不转校，要解除副账号原来的分组关系
//        for (Long tempTeacherId : teacherMainSubIds) {
//            if (!teacherIds.contains(tempTeacherId)) {
//                List<Long> clazzIds = teacherLoaderClient.loadTeacherClazzIds(tempTeacherId);
//                for (Long clazzId : clazzIds) {
//                    groupServiceClient.disableTeacherGroup(tempTeacherId, clazzId, true);
//                }
//            }
//        }

        for (Long tempTeacherId : teacherMainSubIds) {
            List<GroupMapper> groupMapperList = deprecatedGroupLoaderClient.loadTeacherGroupsByTeacherId(tempTeacherId, false);
            Set<Long> teacherGroupIds = groupMapperList.stream().map(GroupMapper::getId).collect(toSet());
            // 更换分组班级关系
            MapMessage msg = crmTeacherSystemClazzService.changeTeacherSchool(tempTeacherId, teacherGroupIds, schoolId, getCurrentAdminUser().getAdminUserName());
            if (!msg.isSuccess()) {
                logger.error("转校失败,原因:{},老师id:{},旧学校:{},新学校:{}", msg.getInfo(), tempTeacherId, originalSchool.getId(), schoolId);
                return msg;
            }
            //清理前台cache teacher and students
            asyncUserServiceClient.getAsyncUserService().evictUserCache(tempTeacherId).awaitUninterruptibly();
            Map<Long, List<Long>> groupStudents = studentLoaderClient.loadGroupStudentIds(teacherGroupIds);
            for (List<Long> students : groupStudents.values()) {
                asyncUserServiceClient.getAsyncUserService().evictUserCache(students).awaitUninterruptibly();
            }

            //记录进线日志
            String operation = "老师带班更改学校；管理员" + getCurrentAdminUser().getAdminUserName() + "修改老师" + tempTeacherId + "学校:" + "原学校" + originalSchool.getId() + "，修改后的学校" + schoolId;

            // 记录 UserServiceRecord
            userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("带班转校");
            userServiceRecord.setComments(operation + "；说明[" + changeSchoolDesc + "]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            if (crmTeacherTransferService.isSchoolDictSchool(schoolId)) {
                //用于风控组二次审核
                Teacher tempTeacher = mainSubTeachers.get(tempTeacherId);
                boolean authenticationState = false;
                if (tempTeacher != null) {
                    authenticationState = tempTeacher.fetchCertificationState() == AuthenticationState.SUCCESS;
                }
                CrmTeacherTransferSchoolRecord crmTeacherTransferSchoolRecord = new CrmTeacherTransferSchoolRecord(
                        tempTeacherId, authenticationState, CrmTeacherTransferSchoolRecord.ChangeType.WITHCLAZZS,
                        originalSchool.getId(), crmTeacherTransferService.isSchoolDictSchool(originalSchool.getId()), schoolId,
                        getCurrentAdminUser().getAdminUserName(), new Date(), changeSchoolDesc, CrmTeacherTransferSchoolRecord.CheckResult.NOTHANDLED);
                crmTeacherTransferService.upsertCrmTeacherTransferSchoolRecord(crmTeacherTransferSchoolRecord);
            }
        }

        //fix 判断包班制情况中主副账号下的group是否关联,如果关联,需要特殊处理,换校前断开关联,换校后在进行关联
//        if (CollectionUtils.isNotEmpty(shareGroupIdSetList)) {
//            shareGroupIdSetList.forEach(groupIds -> {
//                groupServiceClient.shareGroups(groupIds, true);
//                logger.info("带班转校时,老师" + remindTeacherId + "包班制主副账号下的group关联,重新关联" + groupIds);
//            });
//        }

//        SchoolExtInfo sourceSchoolExtInfo = schoolExtServiceClient.getSchoolExtService()
//                .loadSchoolExtInfo(sourceSchool.getId())
//                .getUninterruptibly();
//        SchoolExtInfo targetSchoolExtInfo = schoolExtServiceClient.getSchoolExtService()
//                .loadSchoolExtInfo(school.getId())
//                .getUninterruptibly();
//        boolean sourceScanMachineFlag = sourceSchoolExtInfo != null && sourceSchoolExtInfo.isScanMachineFlag();
//        boolean targetScanMachineFlag = targetSchoolExtInfo != null && targetSchoolExtInfo.isScanMachineFlag();
//
//        // 来源学校有阅卷机权限,目标学校没有阅卷机权限时,删除老师名下学生的学号和阅卷机号
//        if (sourceScanMachineFlag && !targetScanMachineFlag) {
//            newKuailexueServiceClient.clearTeacherStudentScanNumber(originalSchool.getId(), teacherId);
////            kuailexueServiceClient.removeScanNumberStudentNumberFromTeacher(teacherId, originalSchool.getId());
//        }

        // 换跨学段换校后需要让用户登出app
        if (!Objects.equals(sourceSchool.getLevel(), school.getLevel())) {
            VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Teacher", teacherId);
            if (vendorAppsUserRef != null) {
                vendorServiceClient.expireSessionKey("17Teacher", teacherId,
                        SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), teacherId));
            }
        }

        MapMessage message = MapMessage.successMessage("修改成功").add("customerServiceRecord", userServiceRecord);
        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        message.add("createTime", sdf.format(new Date()));
        return message;
    }

    //
    @RequestMapping(value = "changeschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    private MapMessage changeSchool() {
        Long teacherId = NumberUtils.toLong(getRequest().getParameter("teacherId"));
        Set<Long> teacherMainSubIds = teacherLoaderClient.loadRelTeacherIds(teacherId);

        //fix  包班制下一个老师的主副账号的group相互关联时,允许带班换校
        Set<Long> filterGroupIds = deprecatedGroupLoaderClient.loadTeacherGroups(teacherMainSubIds, false).values()
                .stream()
                .flatMap(Collection::stream)
                .map(GroupTeacherMapper::getId)
                .collect(Collectors.toSet());
        List<Set<Long>> shareGroupIdSetList = null; //不为空时,说明主副账号中的group关联了
        if (teacherMainSubIds.size() > 0) {
            Map<String, Set<Long>> tempMap = new HashMap<>();
            Map<Long, GroupMapper> groupMapperMap = deprecatedGroupLoaderClient.loadGroups(filterGroupIds, false);
            for (GroupMapper groupMapper : groupMapperMap.values()) {
                if (StringUtils.isNotBlank(groupMapper.getGroupParent())) {
                    if (!tempMap.containsKey(groupMapper.getGroupParent())) {
                        tempMap.put(groupMapper.getGroupParent(), new HashSet<>());
                    }
                    tempMap.get(groupMapper.getGroupParent()).add(groupMapper.getId());
                }
            }
            shareGroupIdSetList = tempMap.values().stream().filter(t -> t.size() > 1).collect(Collectors.toList());
        }

        //用于 /crm/teacher/teacherhomepage.vpage 页面点击[修改学校]的预判，如果
        // 1.老师所在班中还有其他老师则不许修改
        String preCheckFlag = getRequest().getParameter("precheck");
        //预留接口,避免特殊情况下老师无法转校
        boolean checkMachineFlag = true;
        String checkMachine = getRequest().getParameter("checkmachine");
        if (StringUtils.equals(checkMachine, "checkmachine")) {
            checkMachineFlag = false;
        }

        if ("true".equals(preCheckFlag)) {
            School sourceSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(teacherId)
                    .getUninterruptibly();
            if (sourceSchool == null) {
                return MapMessage.errorMessage("老师不在任何学校中");
            }

            Long targetSchoolId = NumberUtils.toLong(getRequestParameter("schoolId", "").replaceAll("\\s", ""));
            SchoolExtInfo sourceSchoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(sourceSchool.getId())
                    .getUninterruptibly();
            SchoolExtInfo targetSchoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(targetSchoolId)
                    .getUninterruptibly();
            if (checkMachineFlag) {
                if (sourceSchoolExtInfo != null && targetSchoolExtInfo != null && sourceSchoolExtInfo.isScanMachineFlag() && targetSchoolExtInfo.isScanMachineFlag()) {
                    return MapMessage.errorMessage("两所学校均开通了阅卷机，无法给老师带班转校");
                }
            }

            Map<String, String> clazzMap = new HashMap<>();
            for (Long tempTeacherId : teacherMainSubIds) {
                List<GroupMapper> groupMapperList = deprecatedGroupLoaderClient.loadTeacherGroupsByTeacherId(tempTeacherId, false);
                for (GroupMapper mapper : groupMapperList) {
                    Set<Long> sharedGroupIds = deprecatedGroupLoaderClient.loadSharedGroupIds(mapper.getId());
                    //fix  包班制下一个老师的主副账号的group相互关联时,允许带班换校
                    if (CollectionUtils.isNotEmpty(shareGroupIdSetList)) {
                        sharedGroupIds = sharedGroupIds.stream().filter(gid -> !filterGroupIds.contains(gid)).collect(toSet());
                    }
                    if (sharedGroupIds.size() > 0) {
                        Clazz clazz = raikouSDK.getClazzClient()
                                .getClazzLoaderClient()
                                .loadClazz(mapper.getClazzId());
                        clazzMap.put(ConversionUtils.toString(mapper.getId()), clazz == null ? "" : clazz.formalizeClazzName());
                    }
                }
            }
            return new MapMessage().setSuccess(MapUtils.isEmpty(clazzMap)).add("clazzMap", clazzMap);
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "changeschoolwithoutclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage changeSchoolWithoutClazz() {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolIdwithoutclazz");
        String changeSchoolDesc = getRequestString("changeSchoolDescwithoutclazz");
        if (teacherId == 0L || schoolId == 0L || StringUtils.isBlank(changeSchoolDesc)) {
            return MapMessage.errorMessage("请检查填写的内容，注意描述信息不能为空");
        }

        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("学校ID" + schoolId + "不存在");
        }

        School originalSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (originalSchool != null) {
            if (Objects.equals(originalSchool.getId(), schoolId)) {
                return MapMessage.errorMessage("修改前后的学校不能为同一学校");
            }
        }

        //处理大使
        Set<Long> mainSubTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        MapMessage ambassadorMapMessage = teacherAmbassadorCancel(mainSubTeacherIds);
        if (!ambassadorMapMessage.isSuccess()) {
            return ambassadorMapMessage.add("tip", "取消校园大使失败");
        }

        // 自动检查作业
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        deprecatedGroupLoaderClient.loadTeacherGroups(teacherId, false).forEach(g -> {
            List<String> homeworkIds = newHomeworkLoaderClient.loadGroupHomeworks(g.getId(), g.getSubject()).originalLocationsAsList()
                    .stream()
                    .map(NewHomework.Location::getId)
                    .collect(Collectors.toList());
            homeworkIds.forEach(id -> newHomeworkServiceClient.checkHomework(teacher, id, HomeworkSourceType.CRM));
        });

        MapMessage mapMessage = crmTeacherSystemClazzService.changeTeacherSchoolNotCarryOldClazz(teacherId, schoolId);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }

        // 学制要跟新学校一致
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        Ktwelve ktwelve;
        switch (schoolLevel) {
            case HIGH:
                ktwelve = Ktwelve.SENIOR_SCHOOL;
                break;
            case MIDDLE:
                ktwelve = Ktwelve.JUNIOR_SCHOOL;
                break;
            case INFANT:
                ktwelve = Ktwelve.INFANT;
                break;
            default:
                ktwelve = Ktwelve.PRIMARY_SCHOOL;
        }
        teacherServiceClient.setTeacherSubject(teacherId, teacher.getSubject(), ktwelve);

        Map<Long, Collection<Long>> teacherIdGroupIdMap = (Map<Long, Collection<Long>>) mapMessage.get("teacherIdGroupIdMap");
        Map<Long, Teacher> longTeacherMap = teacherLoaderClient.loadTeachers(mainSubTeacherIds);
        for (Long tid : mainSubTeacherIds) {
            //清理前台cache
            asyncUserServiceClient.getAsyncUserService()
                    .evictUserCache(tid)
                    .awaitUninterruptibly();
            //记录进线日志
            String operation;
            if (teacherIdGroupIdMap.get(tid) != null) {
                operation = "老师不带班更改学校 ;  管理员" + getCurrentAdminUser().getAdminUserName() + "解除老师" + tid + "班级关系" + teacherIdGroupIdMap.get(tid) + "; 学校:" + "原学校" + (originalSchool == null ? "无" : originalSchool.getId()) + "，修改后的学校" + schoolId;
            } else {
                operation = "老师不带班更改学校 ;  管理员" + getCurrentAdminUser().getAdminUserName() + "解除老师" + tid + "班级关系[空];  学校:" + "原学校" + (originalSchool == null ? "无" : originalSchool.getId()) + "，修改后的学校" + schoolId;
            }

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("不带班转校");
            userServiceRecord.setComments(operation + "；说明[" + changeSchoolDesc + "]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            //用于风控组二次审核
            if (crmTeacherTransferService.isSchoolDictSchool(schoolId)) {
                Teacher tempTeacher = longTeacherMap.get(tid);
                boolean authenticationState = false;
                if (tempTeacher != null) {
                    authenticationState = tempTeacher.fetchCertificationState() == AuthenticationState.SUCCESS;
                }

                CrmTeacherTransferSchoolRecord crmTeacherTransferSchoolRecord = new CrmTeacherTransferSchoolRecord(
                        tid, authenticationState, CrmTeacherTransferSchoolRecord.ChangeType.WITHOUTCLAZZS,
                        originalSchool == null ? 0 : originalSchool.getId(),
                        crmTeacherTransferService.isSchoolDictSchool(originalSchool == null ? 0 : originalSchool.getId()),
                        schoolId,
                        getCurrentAdminUser().getAdminUserName(),
                        new Date(),
                        changeSchoolDesc,
                        CrmTeacherTransferSchoolRecord.CheckResult.NOTHANDLED);
                crmTeacherTransferService.upsertCrmTeacherTransferSchoolRecord(crmTeacherTransferSchoolRecord);
            }
        }

        //换跨学段换校后需要让用户登出app
        if (originalSchool != null) {
            if (!Objects.equals(originalSchool.getLevel(), school.getLevel())) {
                VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Teacher", teacherId);
                if (vendorAppsUserRef != null) {
                    vendorServiceClient.expireSessionKey("17Teacher", teacherId,
                            SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), teacherId));
                }
            }
        }

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "newhomeworkgolddetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getNewHomeworkGoldDetail() {
        String homeworkId = getRequestString("homeworkId");
        long teacherId = getRequestLong("teacherId");
        if (StringUtils.isBlank(homeworkId) || teacherId <= 0) return MapMessage.errorMessage("查询失败");

        NewHomework homework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (homework == null) return MapMessage.errorMessage("查询失败");
        HomeworkType ht = HomeworkType.of(homework.getSubject().name());

        PossibleCheatingHomework pch = newHomeworkLoaderClient.loadPossibleCheatingHomeworkByTeacherIdAndHomeworkId(teacherId, homeworkId, ht);
        if (pch == null || pch.getRecordOnly()) {
            String uniqueKey = "homeworkType:" + ht.name() + "," + "homeworkId:" + homeworkId;
            IntegralHistory history = integralHistoryLoaderClient.getIntegralHistoryLoader()
                    .loadUserIntegralHistories(teacherId)
                    .stream().filter(ih -> StringUtils.equals(uniqueKey, ih.getUniqueKey())).findFirst().orElse(null);
            if (history == null) {
                return MapMessage.successMessage().add("isCheating", false);
            } else {
                return MapMessage.successMessage().add("homeworkIntegral", history.getIntegral() / 10)
                        .add("isCheating", false);
            }
        } else {
            return MapMessage.successMessage().add("isCheating", true);
        }
    }

    @RequestMapping(value = "homeworkgolddetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getHomeworkGoldDetail() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "undofaketeacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage undoFakeTeacher() {
        // 对于假老师的处理，解除客服误操作判假的老师
        // 对于客服手工判假的老师会在VOX_USER_TAG里面有数据，同时还在CRM_TEACHER_SUMMARY里面有数据
        // 两个地方需要同时清理
        Long teacherId = getRequestLong("teacherId");
        String desc = getRequestString("desc");

        Set<Long> teacherMainSubIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        MapMessage message = null;
        for (Long tid : teacherMainSubIds) {
            CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(tid);
            if (teacherSummary != null && Boolean.TRUE.equals(teacherSummary.getFakeTeacher())) {
                // set teacher fake in teacher ext attribute
                // 挪到里面，否则会出现Summary和ExtAttribute不一致的情况
                userManagementClient.setTeacherFake(tid, false, null);

                AuthCurrentAdminUser adminUser = getCurrentAdminUser();
                message = crmSummaryServiceClient.removeTeacherFakeType(tid);
                crmTeacherFakeService.defakeTeacher(tid, adminUser);

                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(teacherId);
                userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.老师判假.name());
                userServiceRecord.setOperationContent("解除老师判假");
                userServiceRecord.setComments("说明[" + desc + "]");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            } else {
                userManagementClient.setTeacherFake(tid, false, null);
            }
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "bindmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindmobile(@RequestParam Long teacherId, @RequestParam String mobile, @RequestParam String desc) {
        MapMessage message = new MapMessage();
        try {
            if (!crmTeacherService.canBindMobile(teacherId)) {
                return MapMessage.errorMessage("该用户不需要绑定手机");
            }
            if (userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
                return MapMessage.errorMessage("手机号已被占用，请重新输入");
            }


            if (StringUtils.isBlank(mobile) || !MobileRule.isMobile(mobile)) {
                return MapMessage.errorMessage("无效的手机号码");
            }
            //同步shippingaddress 手机号
            MapMessage msg = userServiceClient.activateUserMobile(teacherId, mobile, true, getCurrentAdminUser().getAdminUserName(), desc);
            if (!msg.isSuccess()) {
                return MapMessage.errorMessage("绑定手机失败");
            }

            message.setSuccess(true);
            message.setInfo("绑定完成");
        } catch (Exception ex) {
            logger.error("绑定手机失败，[teacherId:{},mobile:{},desc:{}],msg:{}", teacherId, mobile, desc, ex.getMessage(), ex);
            message.setSuccess(false);
            message.setInfo("绑定失败," + ex.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "isbindmobile.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage isbindmobile(@RequestParam Long teacherId) {
        try {
            if (crmTeacherService.canBindMobile(teacherId)) {
                return MapMessage.successMessage().setSuccess(true);
            } else {
                return MapMessage.errorMessage();
            }
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    /**
     * 重置教务老师密码
     */
    @RequestMapping(value = "resetaffairpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetAffairPassword() {
        Long userId = getRequestLong("userId");
        String passwordDesc = getRequestString("passwordDesc");
        String passwordExtraDesc = getRequestString("passwordExtraDesc");

        if (StringUtils.isBlank(passwordDesc) || ("其他".equals(passwordDesc) && StringUtils.isBlank(passwordExtraDesc))) {
            return MapMessage.errorMessage("重置用户密码失败，请正确填写各参数");
        }

        ResearchStaff staff = researchStaffLoaderClient.loadResearchStaff(userId);
        // 修改密码
        String userPasswd = RandomGenerator.generateUserPassword(RandomGenerator.generatePlainPassword()).getPlainPassword();
        MapMessage msg = userServiceClient.setPassword(staff, userPasswd);

        if (msg.isSuccess()) {
            User user = raikouSystem.loadUser(userId);
            String payload = user.getProfile().getRealname() + "老师您好，您的密码已被重置为" + userPasswd + "，登录后可进行修改。如有问题，可拨打400-160-1717";
            UserAuthentication ua = raikouSystem.loadUserAuthentication(userId);
            if (!StringUtils.isBlank(ua.getSensitiveMobile())) {
                userSmsServiceClient.buildSms()
                        .to(user)
                        .content(payload)
                        .type(SmsType.AFFAIR_TEACHER_ACCOUNT_NOTICE)
                        .send();
            }

            // 记录一条处理日志
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(userId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("CRM重置教务老师密码");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }

        return MapMessage.successMessage();
    }

    /**
     * 修改老师密码为随机的
     */
    @RequestMapping(value = "resetpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPassword() {
        long userId = getRequestLong("userId", -1L);
        String passwordDesc = getRequestParameter("passwordDesc", "");
        String passwordExtraDesc = getRequestParameter("passwordExtraDesc", "");
        String mobile = getRequestParameter("verifyMobile", "");
        String preBindMobile = getRequestParameter("bindMobile", "");
        MapMessage message = new MapMessage();
        //校验参数
        if (StringUtils.isBlank(passwordDesc) || ("其他".equals(passwordDesc) && StringUtils.isBlank(passwordExtraDesc))) {
            message.setSuccess(false);
            message.setInfo("重置用户密码失败，请正确填写各参数");
            return message;
        }

        //如果没有绑定手机号
        if (!StringUtils.isBlank(preBindMobile)) {
            if (!MobileRule.isMobile(preBindMobile)) {
                message.setSuccess(false);
                message.setInfo("绑定手机号码失败，手机号不合法");
                return message;
            }
            //先绑定手机号
            try {
                Validate.isTrue(crmTeacherService.canBindMobile(userId), "该用户不需要绑定手机");
                Validate.isTrue(userLoaderClient.loadMobileAuthentication(preBindMobile, UserType.TEACHER) == null, "手机号已被占用，请重新输入");
                //同步shippingaddress 手机号
                MapMessage msg = userServiceClient.activateUserMobile(userId, preBindMobile, true, getCurrentAdminUser().getAdminUserName(), passwordExtraDesc);
                if (!msg.isSuccess()) {
                    return MapMessage.errorMessage("绑定手机失败");
                }
                addAdminLog("绑定老师手机", userId, "mobile", "管理员为老师绑定手机", StringUtils.mobileObscure(preBindMobile));

                message.setSuccess(true);
                message.setInfo("绑定完成");
                mobile = preBindMobile;
            } catch (Exception ex) {
                logger.error("绑定手机失败，[teacherId:{},mobile:{},desc:{}],msg:{}", userId, preBindMobile, passwordExtraDesc, ex.getMessage(), ex);
                message.setSuccess(false);
                message.setInfo("绑定失败," + ex.getMessage());
                return message;
            }
        }
        //重置密码
        String password = RandomUtils.randomString(6);
        try {
            passwordDesc += "（" + passwordExtraDesc + "）";
            crmUserService.resetUserPassword(userId, password, true, mobile.trim(), passwordDesc, getCurrentAdminUser().getAdminUserName(), "CrmTeacherNewController.resetpassword");

            message.setSuccess(true);
            message.setInfo("重置密码成功");
        } catch (Exception ex) {
            logger.warn("重置密码失败，userId:{},password:{},passwordDesc:{},passwordExtraDesc:{},mobile:{},msg:{}", userId, password, passwordDesc, passwordExtraDesc, mobile, ex.getMessage());
            message.setSuccess(false);
            message.setInfo("重置密码失败，" + ex.getMessage());
        }
        //send message
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        String payload = teacher.getProfile().getRealname() + "老师您好，您的密码已被重置为" + password + "，登录后可进行修改。如有问题，可拨打400-160-1717";
        UserAuthentication ua = raikouSystem.loadUserAuthentication(userId);
        if (!StringUtils.isBlank(ua.getSensitiveMobile())) {
            userSmsServiceClient.buildSms()
                    .to(teacher)
                    .content(payload)
                    .type(SmsType.CRM_RESET_TEACHER_PWD)
                    .send();
        }
        return message;
    }

    // 老师抽奖相关
    @RequestMapping(value = "aboutlottery.vpage", method = RequestMethod.GET)
    public String aboutLottery(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Integer campaignId = getRequestInt("campaignId");
        if (teacherId == 0) {
            return "crm/teachernew/aboutlottery";
        }
        // 获取老师抽奖记录
        CampaignType campaignType = CampaignType.TEACHER_SCHOLARSHIP_GOLD_LOTTERY;
        if (campaignId != 0) {
            campaignType = CampaignType.of(campaignId);
        }
        List<CampaignLotteryHistory> histories = campaignLoaderClient.getCampaignLoader().findCampaignLotteryHistories(campaignType.getId(), teacherId);
        histories = histories.stream().
                sorted((o1, o2) -> Long.compare(o2.getCreateDatetime().getTime(), o1.getCreateDatetime().getTime())).collect(Collectors.toList());
        List<CampaignLottery> lotteries = campaignLoaderClient.getCampaignLoader().findCampaignLotteries(campaignType.getId());
        Map<Integer, String> lotteryNameMap = lotteries.stream().collect(Collectors.toMap(CampaignLottery::getAwardId, CampaignLottery::getAwardName));
        List<Map<String, Object>> data = new ArrayList<>();
        for (CampaignLotteryHistory history : histories) {
            Map<String, Object> result = new HashMap<>();
            result.put("awardId", history.getAwardId());
            result.put("awardName", lotteryNameMap.get(history.getAwardId()) == null ? "" : lotteryNameMap.get(history.getAwardId()));
            result.put("createDatetime", DateUtils.dateToString(history.getCreateDatetime()));
            data.add(result);
        }
        model.addAttribute("histories", data);
        // 用户剩余免费抽奖次数
        int count = campaignServiceClient.getCampaignService().getTeacherLotteryFreeChance(campaignType, teacherId);
        model.addAttribute("freeCount", count);
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("campaignId", campaignId);
        return "crm/teachernew/aboutlottery";
    }

    // 添加免费抽奖次数
    @RequestMapping(value = "addlotteryfreecount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addLotteryFreeCount() {
        Long teacherId = getRequestLong("teacherId");
        Integer campaignId = getRequestInt("campaignId");
        Integer freeCount = getRequestInt("freeCount");
        if (teacherId == 0 || freeCount == 0 || campaignId == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        CampaignType campaignType = CampaignType.of(campaignId);
        campaignServiceClient.getCampaignService().addLotteryFreeChance(campaignType, teacherId, freeCount);
        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.抽奖相关.name());
        userServiceRecord.setOperationContent("添加抽奖次数");
        userServiceRecord.setComments("管理员给用户添加了" + freeCount + "次抽奖[活动ID：" + campaignId + "]次数");
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        return MapMessage.successMessage("添加成功，请刷新页面查看");
    }

    // 导出异常老师
    @RequestMapping(value = "unusual_teacher_export.vpage")
    public void unusualTeacherExport() {
        Date startTime = requestDate("startTime");
        Date endTime = requestDate("endTime");
        try {
            Resource resource = new ClassPathResource("/config/unusual_teacher.xlsx");
            if (!resource.exists()) {
                logger.error("UnusualTeacher template not exists @ path = /config/unusual_teacher.xlsx");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 18);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFSheet sheet = workbook.getSheetAt(0);
            List<CrmTeacherSummary> unusualTeachers = crmTeacherSummaryService.loadUnusualTeachers(startTime, endTime);
            if (CollectionUtils.isNotEmpty(unusualTeachers)) {
                int index = 3;
                for (CrmTeacherSummary teacher : unusualTeachers) {
                    XSSFRow row = sheet.createRow(index++);
                    Long teacherId = teacher.getTeacherId();
                    createCell(row, cellStyle, 0, String.valueOf(teacherId));
                    createCell(row, cellStyle, 1, String.valueOf(teacher.getRealName()));
                    String mobile = sensitiveUserDataServiceClient.loadUserMobile(teacherId);
                    createCell(row, cellStyle, 2, String.valueOf(mobile));
                    createCell(row, cellStyle, 3, Subject.ofWithUnknown(teacher.getSubject()).getValue());
                    createCell(row, cellStyle, 4, String.valueOf(teacher.getSchoolName()));
                    createCell(row, cellStyle, 5, SchoolLevel.valueOf(teacher.getSchoolLevel()).getDescription());
                    createCell(row, cellStyle, 6, String.valueOf(teacher.getProvinceName()) + "/" + String.valueOf(teacher.getCityName()) + "/" + String.valueOf(teacher.getCountyName()));
                    createCell(row, cellStyle, 7, teacherUnusualStatus(teacher));
                }
            }
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile("异常老师列表.xlsx", "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("unusualTeacherExport Excp : {}; @ path = /config/unusual_teacher.xlsx", e);
        }
    }

    @RequestMapping(value = "changeklxsubjectleader.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeKlxSubjectLeader() {
        Long teacherId = getRequestLong("teacherId");
        boolean klxSubjectLeaderSetFlag = getRequestBool("klxSubjectLeaderSetFlag"); //是否设置为学科组长
        String desc = getRequestString("desc");
        if (teacherId == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("问题描述不能为空");
        }

        if (klxSubjectLeaderSetFlag) {
            boolean clazzLevelChoice = getRequestBool("clazzLevelChoice");//添加年级或删除年级
            ClazzLevel clazzLevel = ClazzLevel.parse(getRequestInt("clazzLevel"));
            if (clazzLevel == null) {
                return MapMessage.errorMessage("年级选择错误");
            }

            teacherServiceClient.setKlxSubjectLeader(teacherId, true, clazzLevelChoice, clazzLevel);
        } else {
            teacherServiceClient.setKlxSubjectLeader(teacherId, false, null, null);
        }

        return MapMessage.successMessage();
    }

    /**
     * 设置中学校长
     *
     * @return
     */
    @RequestMapping(value = "addSchoolMaster.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addSchoolMaster() {
        Long schoolId = getRequestLong("schoolId");
        Long teacherId = getRequestLong("teacherId");
        Boolean setSchoolMaster = getRequestBool("setSchoolMaster");
        String desc = getRequestString("desc");
        if (schoolId == 0 || teacherId == 0) {
            return MapMessage.errorMessage("参数错误 学校ID:" + schoolId + ",老师ID:" + teacherId);
        }
        User user = raikouSystem.loadUser(teacherId);
        if (user == null) {
            return MapMessage.errorMessage("未找到该老师：" + teacherId);
        }
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("问题描述不能为空");
        }

        Long localSchoolId = null;
        if (user.fetchUserType() == UserType.TEACHER) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (teacherDetail != null) {
                localSchoolId = teacherDetail.getTeacherSchoolId();
            }
        } else {
            return MapMessage.errorMessage("该账号" + teacherId + "不是老师");
        }

        if (localSchoolId == null || !Objects.equals(localSchoolId, schoolId)) {
            return MapMessage.errorMessage("该老师" + teacherId + "学校id为" + localSchoolId + ",不在该" + schoolId + "学校内");
        }

        List<TeacherRoles> teacherExtAttribute = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacherId);
        if (Objects.isNull(teacherExtAttribute)) {
            return MapMessage.errorMessage("该老师" + teacherId + "学校id为" + localSchoolId + ",不在该" + schoolId + "学校内");
        }

        if (setSchoolMaster) {
            teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(teacherId,
                    schoolId,
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.SCHOOL_MASTER.name(),
                    "");
        } else {
            teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(teacherId,
                    schoolId,
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.SCHOOL_MASTER.name());
        }

        // 记录一条处理日志
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("设置中学校长");
        userServiceRecord.setComments(desc);
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        return MapMessage.successMessage().add("teacherId", user.getId())
                .add("teacherName", user.fetchRealname());
    }

    @RequestMapping(value = "getclassmanagelist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getClassManagerList() {
        Long teacherId = getRequestLong("teacherId");
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师信息");
        }
        School school = raikouSystem.loadSchool(teacher.getTeacherSchoolId());
        if (school == null) {
            return MapMessage.errorMessage("无效的学校");
        }

        Map<ClazzLevel, List<Clazz>> gradeMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .enabled()
                .toList()
                .stream()
                .filter(c -> c.getClazzLevel() != null)
                .collect(Collectors.groupingBy(Clazz::getClazzLevel));

        if (MapUtils.isEmpty(gradeMap)) {
            return MapMessage.errorMessage("该学校还没有创建班级");
        }

        final List<Long> clazzIds;
        TeacherRoles classManager = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacherId)
                .stream()
                .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.CLASS_MANAGER.name()))
                .findAny().orElse(null);
        if (classManager != null) {
            clazzIds = JsonUtils.fromJsonToList(classManager.getRoleContent(), Long.class);
        } else {
            clazzIds = new ArrayList<>();
        }

        List<Map<String, Object>> gradeList = new LinkedList<>();
        for (Map.Entry<ClazzLevel, List<Clazz>> entry : gradeMap.entrySet()) {
            ClazzLevel grade = entry.getKey();
            if (grade == null || grade.getLevel() > 80) {
                continue;
            }
            Map<String, Object> gradeInfo = new HashMap<>();
            gradeInfo.put("grade", grade.getDescription());
            gradeInfo.put("level", grade.getLevel());

            List<Map<String, Object>> classList = entry.getValue()
                    .stream()
                    .sorted((c1, c2) -> {
                        Long n1 = c1.getId();
                        Long n2 = c2.getId();
                        return n1.compareTo(n2);
                    })
                    .map(c -> {
                        Map<String, Object> classInfo = new HashMap<>();
                        classInfo.put("classId", c.getId());
                        classInfo.put("className", c.getClassName());
                        classInfo.put("fullName", c.formalizeClazzName());
                        classInfo.put("selected", clazzIds.contains(c.getId()));
                        return classInfo;
                    }).collect(Collectors.toList());
            gradeInfo.put("classList", classList);
            gradeList.add(gradeInfo);
        }

        gradeList.sort(Comparator.comparingInt(g -> ClazzLevel.parse(SafeConverter.toInt(g.get("level"))).ordinal()));

        List<Map<String, Object>> teacherClass = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .sorted((c1, c2) -> {
                    int lv1 = c1.getClazzLevel() != null ? c1.getClazzLevel().getLevel() : 100;
                    int lv2 = c2.getClazzLevel() != null ? c2.getClazzLevel().getLevel() : 100;
                    if (lv1 != lv2) {
                        return Integer.compare(lv1, lv2);
                    }
                    Long n1 = c1.getId();
                    Long n2 = c2.getId();
                    return n1.compareTo(n2);
                })
                .map(c -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("classId", c.getId());
                    info.put("fullName", c.formalizeClazzName());
                    return info;
                }).collect(toList());


        return MapMessage.successMessage().add("gradeList", gradeList)
                .add("teacherClass", teacherClass);
    }

    @RequestMapping(value = "setclassmanagelist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setClassManagerList() {
        // 检查权限
        MapMessage message = checkUser(ConfigCategory.PRIMARY_PLATFORM_GENERAL, "SET_CLASS_MANAGER");
        if (!message.isSuccess()) {
            return message;
        }
        // 检查参数
        String desc = getRequestString("desc");
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("请输入描述");
        }

        Long teacherId = getRequestLong("teacherId");
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师信息");
        }

        String classIds = getRequestString("classIds");
        Set<Long> classIdSet = Stream.of(classIds.split(","))
                .filter(p -> SafeConverter.toLong(p) > 0L)
                .map(SafeConverter::toLong)
                .collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(classIdSet)) {
            teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(teacherId,
                    teacher.getTeacherSchoolId(),
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.CLASS_MANAGER.name(),
                    JsonUtils.toJson(classIdSet));
        } else {
            teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(teacherId,
                    teacher.getTeacherSchoolId(),
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.CLASS_MANAGER.name());
        }

        // 记录日志
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("设置班主任");
        userServiceRecord.setComments("设置班主任[" + classIds + "]，说明[" + desc + "]");
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "getgrademanagelist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGradeManagerList() {
        Long teacherId = getRequestLong("teacherId");
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师信息");
        }
        EduSystemType eduSystemType = EduSystemType.S4;
        if (teacher.isJuniorTeacher()) {
            eduSystemType = EduSystemType.J4;
        }

        List<Integer> managedGradeJies = new ArrayList<>();

        TeacherRoles gradeManager = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacherId)
                .stream()
                .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.GRADE_MANAGER.name()))
                .findAny()
                .orElse(null);

        if (gradeManager != null) {
            managedGradeJies = JsonUtils.fromJsonToList(gradeManager.getRoleContent(), Integer.class);
        }

        Set<ClazzLevel> managedGradeList = new HashSet<>();
        for (Integer jie : managedGradeJies) {
            ClazzLevel clazzLevel = ClassJieHelper.toClazzLevel(jie, eduSystemType);
            if (clazzLevel.getLevel() < ClazzLevel.SIXTH_GRADE.getLevel() || clazzLevel.getLevel() > ClazzLevel.SENIOR_THREE.getLevel()) {
                continue;
            }
            managedGradeList.add(clazzLevel);
        }

        List<Map<String, Object>> gradeList = new ArrayList<>();
        for (String level : eduSystemType.getCandidateClazzLevel().split(",")) {
            ClazzLevel clazzLevel = ClazzLevel.parse(Integer.parseInt(level));
            gradeList.add(
                    MapUtils.m("value", clazzLevel.getLevel(),
                            "text", clazzLevel.getDescription(),
                            "selected", managedGradeList.contains(clazzLevel)
                    )
            );
        }
        return MapMessage.successMessage().add("gradeList", gradeList);
    }

    @RequestMapping(value = "setgrademanagelist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setGradeManagerList() {
        // 检查权限
        MapMessage message = checkUser(ConfigCategory.PRIMARY_PLATFORM_GENERAL, "SET_GRADE_MANAGER");
        if (!message.isSuccess()) {
            return message;
        }
        // 检查参数
        String desc = getRequestString("desc");
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("请输入描述");
        }

        Long teacherId = getRequestLong("teacherId");
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师信息");
        }

        String clazzLevels = getRequestString("grades");
        EduSystemType eduSystemType = teacher.isJuniorTeacher() ? EduSystemType.J4 : EduSystemType.S4;
        List<ClazzLevel> levels = Stream.of(clazzLevels.split(","))
                .filter(level -> eduSystemType.getCandidateClazzLevel().contains(level))
                .map(t -> ClazzLevel.parse(SafeConverter.toInt(t)))
                .filter(Objects::nonNull)
                .collect(toList());
        Set<Integer> jies = levels.stream()
                .map(ClassJieHelper::fromClazzLevel)
                .collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(jies)) {
            teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(teacherId,
                    teacher.getTeacherSchoolId(),
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.GRADE_MANAGER.name(),
                    JsonUtils.toJson(jies));
        } else {
            teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(teacherId,
                    teacher.getTeacherSchoolId(),
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.GRADE_MANAGER.name());
        }

        // 记录日志
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("设置年级主任");
        userServiceRecord.setComments("设置年级主任[" + StringUtils.join(levels.stream().map(ClazzLevel::getDescription).collect(toList()), ",") + "]，说明[" + desc + "]");
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "authstuquery.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage authStudentQuery() {
        Long teacherId = getRequestLong("teacherId");

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            return MapMessage.errorMessage("无效的老师");
        }
        Long schoolId = teacherDetail.getTeacherSchoolId();
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("老师学校无效");
        }
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());

        // 调用大数据接口
        Set<Long> studentIds = studentLoaderClient.loadTeacherStudents(teacherId)
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        // 返回的是一个 List< Map<学生ID(Long), 认证时间(String yyyy-MM-dd)> > 的数据结构，可以理解成 KeyValuePair
        List<Object> authStuResult = null;
        try {
            authStuResult = userAuthQueryServiceClient.queryStudentAuthTime(studentIds, schoolLevel);
        } catch (Exception ex) {
            logger.error("Failed invoke athena stuAuthQueryService, please check it.", ex);
        }

        return MapMessage.successMessage().add("authStu", authStuResult);
    }

    /**
     * 封禁老师
     *
     * @return
     */
    @RequestMapping(value = "forbidteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage forbidTeacher() {
        Long teacherId = getRequestLong("teacherId");
        String desc = getRequestString("desc");
        if (teacherId == null) {
            return MapMessage.errorMessage("无效的老师信息");
        }
        MapMessage mapMessage = userServiceClient.forbidUser(teacherId);
        if (mapMessage.isSuccess()) {
//            serviceClient.expireSessionKey("17Teacher", teacherId, SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), teacherId));
            // 记录操作日志
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent(desc);
            userServiceRecord.setComments("封禁老师");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            // 强制
        }
        return mapMessage;
    }

    /**
     * 解封老师
     *
     * @return
     */
    @RequestMapping(value = "unforbidteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage unForbidTeacher() {
        Long teacherId = getRequestLong("teacherId");
        String desc = getRequestString("desc");
        if (teacherId == null) {
            return MapMessage.errorMessage("无效的老师信息");
        }
        MapMessage mapMessage = userServiceClient.unForbidUser(teacherId);
        if (mapMessage.isSuccess()) {
            // 记录操作日志
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent(desc);
            userServiceRecord.setComments("解封老师");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }
        return mapMessage;
    }

    private XSSFCell createCell(XSSFRow row, XSSFCellStyle style, int index, String value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(value);
        return cell;
    }

    private String teacherUnusualStatus(CrmTeacherSummary teacher) {
        return "";
//        List<String> unusualStatus = teacher.getUnusualStatus();
//        if (CollectionUtils.isEmpty(unusualStatus)) {
//            return "";
//        }
//        StringBuilder builder = new StringBuilder();
//        if (unusualStatus.contains(NOCLS_AFTER_REG_2DAYS.name())) {
//            builder.append(NOCLS_AFTER_REG_2DAYS.desc).append(" ");
//        }
//        if (unusualStatus.contains(NOUSE_AFTER_CLS_3DAYS.name())) {
//            builder.append(NOUSE_AFTER_CLS_3DAYS.desc).append(" ");
//        }
//        if (unusualStatus.contains(NO_ASSIGN_5DAYS.name())) {
//            builder.append(NO_ASSIGN_5DAYS.desc);
//        }
//        return builder.toString();
    }

    private MapMessage teacherAmbassadorCancel(Collection<Long> teacherIds) {
        for (Long teacherId : teacherIds) {
            // 处理大使
            AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(teacherId)
                    .stream().findFirst().orElse(null);
            if (ref != null) {
                // 如果是大使 直接辞任
                MapMessage message = businessTeacherServiceClient.resignationAmbassador(teacherLoaderClient.loadTeacherDetail(teacherId));
                if (!message.isSuccess()) {
                    return message;
                }
                // 给大使发右下角弹窗
                String content = "您已经转校，系统自动取消了您的大使身份。";
                userPopupServiceClient.createPopup(teacherId).content(content).type(PopupType.AMBASSADOR_NOTICE).category(LOWER_RIGHT).create();
            }
            // 如果有预备大使记录  删除
            AmbassadorCompetition competition = ambassadorLoaderClient.getAmbassadorLoader().loadTeacherAmbassadorCompetition(teacherId);
            if (competition != null) {
                ambassadorServiceClient.getAmbassadorService().$disableAmbassadorCompetition(competition.getId());
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage checkUser(ConfigCategory category, String key) {
        try {
            String config = commonConfigServiceClient.getCommonConfigBuffer()
                    .loadCommonConfigValue(category.getType(), key);
            String adminUserName = getCurrentAdminUser().getAdminUserName();
            if (Stream.of(config.split(",")).anyMatch(t -> t.equals(adminUserName))) {
                return MapMessage.successMessage();
            }
            return MapMessage.errorMessage("您没有操作权限");
        } catch (Exception ignored) {
            return MapMessage.errorMessage("配置异常");
        }
    }

}
