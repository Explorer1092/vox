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
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.cache.AdminCacheSystem;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import com.voxlearning.utopia.admin.service.crm.CrmRewardService;
import com.voxlearning.utopia.admin.service.crm.CrmTeacherSummaryService;
import com.voxlearning.utopia.admin.support.SessionUtils;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.api.constant.RecordType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.data.CertificationCondition;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.ucenter.CertificationApplication;
import com.voxlearning.utopia.entity.ucenter.CertificationApplicationOperatingLog;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;
import com.voxlearning.utopia.service.business.consumer.BusinessManagementClient;
import com.voxlearning.utopia.service.certification.client.CertificationServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.privilege.client.BlackWhiteListManagerClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.mapper.RewardOrderMapper;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.ActivityType;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationState;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.client.UserAuthQueryServiceClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.*;
import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;
import static com.voxlearning.utopia.api.constant.PopupType.DEFAULT_AD;
import static com.voxlearning.utopia.api.constant.Subjects.ALL_SUBJECTS;
import static com.voxlearning.utopia.api.constant.Subjects.BASIC_SUBJECTS;
import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.其他_产品业务;

/**
 * @author Longlong Yu
 * @since 下午5:46,13-6-25.
 * test环境查询ID：116080
 */
@Controller
@RequestMapping("/crm/teacher")
public class CrmTeacherController extends CrmAbstractController {

    private static final int MAX_TEACHER_AMOUNT = 20;

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private BusinessManagementClient businessManagementClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject private BlackWhiteListManagerClient blackWhiteListManagerClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private CertificationServiceClient certificationServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private UserAuthQueryServiceClient userAuthQueryServiceClient;
    @Inject private AdminCacheSystem adminCacheSystem;
    @Inject private CrmRewardService crmRewardService;

    @RequestMapping(value = "teachermobile.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String teacherMobile(Model model) {
        String mobile = getRequestParameter("teacherMobileNumber", "");
        if (StringUtils.isEmpty(mobile)) {
            getAlertMessageManager().addMessageError("老师手机注册后门-手机号为空。");
        } else {
            mobile = mobile.replaceAll("-", "");
            adminCacheSystem.CBS.flushable.set("sms:acs:risk:" + mobile, 60 * 60 * 24, "1");
            getAlertMessageManager().addMessageSuccess("手机号[" + mobile + "]设置成功,24小时内有效");
        }
        return "crm/teacher/teacherlist";
    }

    /**
     * ***********************查询相关*****************************************************************
     */
    @RequestMapping(value = "teacherlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    String teacherList(Model model) {

        List<String> conditionKeys = Arrays.asList("teacherId", "teacherMobile", "klxLoginName", "klxUserName");

        Map<String, Object> conditionMap = new HashMap<>();

        int validInputCount = 0;
        for (String conditionKey : conditionKeys) {
            String value = getRequestParameter(conditionKey, "");
            if (!value.isEmpty()) {
                validInputCount++;
            }
            //手机号搜索框支持 xxx-xxxx-xxxx 的这种344格式直接搜索
            if (conditionKey.equals("teacherMobile")) {
                value = value.replaceAll("-", "");
            }
            if (StringUtils.isNotBlank(value)) {
                //特殊字符过滤
                value = StringRegexUtils.normalizeCZ(value);
            }
            conditionMap.put(conditionKey, value);
        }

        List<Long> teacherIdList = getTeacherIdList(conditionMap);

        List<Map<String, Object>> teacherList = getTeacherSnapshot(teacherIdList);

        if (CollectionUtils.isEmpty(teacherList) && isRequestPost())
            getAlertMessageManager().addMessageError("用户不存在或者用户不是老师用户。");
        if (teacherList.size() == MAX_TEACHER_AMOUNT)
            getAlertMessageManager().addMessageError("若结果中未找到正确用户，请尝试缩小查找范围。");
        model.addAttribute("teacherList", teacherList);

        model.addAttribute("conditionMap", conditionMap);
        return "crm/teacher/teacherlist";
    }


    @RequestMapping(value = "teacherhomepage.vpage", method = RequestMethod.GET)
    public String teacherHomepage(Model model) {

        Long teacherId;

        try {
            teacherId = SafeConverter.toLong(getRequestParameter("teacherId", ""));
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("用户ID不合规范。");
            return redirect("/crm/teacher/teacherlist.vpage");
        }

        Map<String, Object> teacherInfoMap = getTeacherInfoMap(teacherId);

        if (MapUtils.isEmpty(teacherInfoMap)) {
            getAlertMessageManager().addMessageError("用户(ID:" + teacherId + ")不存在或者用户(ID:" + teacherId + ")不是老师用户。");
            return redirect("/crm/teacher/teacherlist.vpage");
        }

        List<KeyValuePair<Integer, String>> recordTypeList = RecordType.toKeyValuePairs();
        List<KeyValuePair<Integer, String>> activityTypeList = ActivityType.toKeyValuePairs();
        List<Subject> subjectTypeList = BASIC_SUBJECTS;

        Teacher teacher = (Teacher) teacherInfoMap.get("teacher");
        if (teacher.isJuniorTeacher() || teacher.isSeniorTeacher()) {
            subjectTypeList = ALL_SUBJECTS;
        }

        model.addAttribute("recordTypeList", recordTypeList);
        model.addAttribute("activityTypeList", activityTypeList);
        model.addAttribute("teacherInfoAdminMapper", teacherInfoMap);
        model.addAttribute("subjectTypeList", subjectTypeList);

        // 微信
        Map<Long, List<UserWechatRef>> dataMap = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(teacherId), WechatType.TEACHER);
        List<UserWechatRef> userWechatRefs = dataMap.get(teacherId);
        if (CollectionUtils.isNotEmpty(userWechatRefs)) {
            userWechatRefs.sort((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime()));
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

        // 拒收学生名单的标志
        boolean rejectReceiveGiftFlag = blackWhiteListManagerClient.loadUserBlackWhiteLists(teacherId, ActivityType.拒收学生奖品名单).size() > 0;
        model.addAttribute("rejectReceiveGiftFlag", rejectReceiveGiftFlag);

        model.addAttribute("ms_crm_admin_url", juniorCrmAdminUrlBase());

        return "crm/teacher/teacherhomepage";
    }

    /**
     * 更新老师端的sessionKey,让老师重新登录
     */
    @AdminAcceptRoles(postRoles = {AdminPageRole.POST_ACCESSOR})
    @RequestMapping(value = "kickOutOfApp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage kickOutOfApp() {

        long userId = getRequestLong("userId");
        if (userId > 0) {
            updateUserAppSessionKey(userId, "17Teacher");
            updateUserAppSessionKey(userId, "17JuniorTea");
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("userId???");
        }
    }

//    @RequestMapping(value = "teacherauthenticationlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
//    public String teacherAuthenticationList(Model model) {
//
//        List<String> conditionKeys = Arrays.asList("authenticationState", "currentPage", "totalPage", "startDate",
//                "endDate", "userId", "regionCode", "reverseOrder");
//
//        Map<String, Object> conditionMap = new HashMap<>();
//
//        for (String conditionKey : conditionKeys) {
//            String value = getRequestParameter(conditionKey, "").trim();
//            conditionMap.put(conditionKey, value);
//        }
//
//        try {
//            conditionMap.put("userId", Long.valueOf(String.valueOf(conditionMap.get("userId"))));
//        } catch (Exception ignored) {
//            conditionMap.remove("userId");
//        }
//
//        try {
//            conditionMap.put("regionCode", Integer.valueOf(String.valueOf(conditionMap.get("regionCode"))));
//        } catch (Exception ignored) {
//            conditionMap.remove("regionCode");
//        }
//
//        if (StringUtils.isBlank(conditionMap.get("currentPage").toString()))
//            conditionMap.put("currentPage", "1");
//        if (isRequestPost()) {
//            List<Long> teacherIdList = getTeacherAuthenticationIdList(conditionMap);
//            List<Map<String, Object>> teacherList = getTeacherSnapshot(teacherIdList);
//            // 如果是查询自动认证的老师，则对其认证时间使用老师申请认证日志里的时间
//            if ("4".equals(conditionMap.get("authenticationState")))
//                teacherList = getTeacherSnapshotWrapper(teacherList);
//            if (CollectionUtils.isEmpty(teacherList))
//                getAlertMessageManager().addMessageError("符合此条件的用户不存在");
//            model.addAttribute("teacherList", teacherList);
//        }
//
//        model.addAttribute("conditionMap", conditionMap);
//        return "crm/teacher/teacherauthenticationlist";
//    }

    /**
     * 查询换班历史
     */
    @RequestMapping(value = "teacherchangeclazzhistory.vpage", method = RequestMethod.GET)
    public String changeClazzHistory(Model model) {
        long teacherId = getRequestLong("teacherId", -1);
        if (teacherId > 0) {
            model.addAttribute("teacherId", teacherId);
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            model.addAttribute("teacherName", (teacher == null) ? null : (teacher.getProfile() == null) ? null : teacher.getProfile().getRealname());
            model.addAttribute("changeClazzHistoryList", crmTeacherService.getChangeClazzHistoryList(teacherId));
            return "crm/teacher/teacherchangeclazzhistory";
        } else {
            getAlertMessageManager().addMessageError(getRequestParameter("teacherId", "") + " 不是有效老师ID");
            return redirect("/crm/index.vpage");
        }
    }

    /**
     * ***********************认证相关*****************************************************************
     */
    @RequestMapping(value = "checkteacherauthentication.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage checkTeacherAuthentication(@RequestParam long teacherId) {
        CertificationCondition certificationCondition = businessTeacherServiceClient.getCertificationCondition(teacherId);
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();

        List<String> failureInfo = new ArrayList<>();
        if ((school == null) || school.getAuthenticationState() == 0) {
            failureInfo.add("  教师所在学校未认证");
        }

        if (!certificationCondition.isEnoughStudentsBindParentMobile()) {
            failureInfo.add("  不足3人绑定手机");
        }

        if (!certificationCondition.isEnoughStudentsFinishedHomework()) {
            failureInfo.add("  没有足够的学生完成作业");
        }

        if (!certificationCondition.isMobileAuthenticated()) {
            failureInfo.add("  老师手机未认证");
        }
        if (failureInfo.size() > 0)
            return MapMessage.errorMessage("<strong>老师不满足认证条件：</strong><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    StringUtils.join(failureInfo, ";<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));

        return MapMessage.errorMessage("老师" + teacherId + "已满足认证条件");
    }

    @RequestMapping(value = "addteacherauthentication.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> teacherAuthentication(@RequestParam Long userId,
                                                     @RequestParam String authenticationDesc,
                                                     @RequestParam String authenticationExtraDesc) {
        return changeTeacherAuthenticationState(userId, 1, authenticationDesc, authenticationExtraDesc);
    }

    @RequestMapping(value = "cancelteacherauthentication.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> cancelTeacherAuthentication(@RequestParam Long userId,
                                                           @RequestParam int authenticationState,
                                                           @RequestParam String authenticationDesc,
                                                           @RequestParam String authenticationExtraDesc) {
        return changeTeacherAuthenticationState(userId, authenticationState, authenticationDesc, authenticationExtraDesc);
    }

    private Map<String, Object> changeTeacherAuthenticationState(Long userId, int authenticationState,
                                                                 String authenticationDesc, String authenticationExtraDesc) {
        Map<String, Object> message = new HashMap<>();

        authenticationDesc = authenticationDesc.replaceAll("\\s", "");
        authenticationExtraDesc = authenticationExtraDesc.replaceAll("\\s", "");
        User user = userLoaderClient.loadUser(userId);
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(userId)
                .getUninterruptibly();
        // 认证老师添加前置条件判断（线上环境判断）
        if (RuntimeMode.ge(Mode.PRODUCTION) && AuthenticationState.SUCCESS.getState() == authenticationState) {
            CertificationCondition certificationCondition = businessTeacherServiceClient.getCertificationCondition(userId);

            // 判断8人三次
            boolean authCond1Reached = false;
            if (school != null && school.getLevel() != null && SchoolLevel.MIDDLE.equals(SchoolLevel.safeParse(school.getLevel()))) {
                Integer teacherHomeworkFinishCount = CrmTeacherSummaryService.getMiddleSchoolTeacherHomework(Collections.singletonList(userId)).get(userId);
                authCond1Reached = teacherHomeworkFinishCount != null && teacherHomeworkFinishCount >= 8;
            } else if (school != null && school.getLevel() != null && SchoolLevel.JUNIOR.equals(SchoolLevel.safeParse(school.getLevel()))) {
                authCond1Reached = certificationCondition.isEnoughStudentsFinishedHomework();
            }

            if (!authCond1Reached) {
                message.put("success", false);
                message.put("info", "没有足够的学生完成作业");
                return message;
            }

            if (!certificationCondition.isEnoughStudentsBindParentMobile()) {
                message.put("success", false);
                message.put("info", "不足3人绑定手机");
                return message;
            }

            if (!certificationCondition.isMobileAuthenticated()) {
                message.put("success", false);
                message.put("info", "老师手机未认证");
                return message;
            }

            if (school.isTraingingSchool()) {
                message.put("success", false);
                message.put("info", "培训学校老师不允许认证");
                return message;
            }

            if (school.isInfantSchool()) {
                message.put("success", false);
                message.put("info", "学前老师暂不允许认证");
                return message;
            }

            // 如果教师的学校是未认证学校，不能自动认证  培训学校的老师不进行自动认证
//            if (agentOrgLoaderClient.isDictSchool(school.getId()) && school.getSchoolAuthenticationState() != AuthenticationState.SUCCESS) {
//                message.put("success", false);
//                message.put("info", "重点学校的老师学校必须先认证");
//                return message;
//            }
        }

        //取消认证
        if (AuthenticationState.SUCCESS.getState() != authenticationState) {
            TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(userId);
            if (detail != null && detail.isSchoolAmbassador()) {
                message.put("success", false);
                message.put("info", "老师是校园大使，请先取消大使，再操作");
                return message;
            }
        }

        if (StringUtils.isBlank(authenticationDesc)) {
            message.put("success", false);
            message.put("info", "请选择问题描述");
            return message;
        }
        if (user == null) {
            message.put("success", false);
            message.put("info", "用户不存在");
            return message;
        }
        if (user.getAuthenticationState() == authenticationState) {
            message.put("success", false);
            message.put("info", "用户当前认证状态无变化，请确认操作");
            return message;
        }
        if ("其他".equals(authenticationDesc) && StringUtils.isBlank(authenticationExtraDesc)) {
            message.put("success", false);
            message.put("info", "请填写附加描述");
            return message;
        }
        if (school == null || school.getType().equals(SchoolType.TRAINING.getType())) {
            message.put("success", false);
            message.put("info", "用户学校数据异常，请检查学校信息");
            return message;
        }

        authenticationDesc += "(" + authenticationExtraDesc + ")";

        AuthenticationState originalState = user.fetchCertificationState();

        Long operatorId = getCurrentAdminUser().getFakeUserId();
        String operatorName = getCurrentAdminUser().getAdminUserName();
        businessTeacherServiceClient.changeUserAuthenticationState(userId, safeParse(authenticationState), operatorId, operatorName);

        AuthenticationState currentAuthenticationState = safeParse(authenticationState);
        if (originalState == WAITING && currentAuthenticationState != SUCCESS) {
            userPopupServiceClient.createPopup(userId)
                    .content("对不起，您的认证申请已经被拒绝。")
                    .type(DEFAULT_AD)
                    .category(LOWER_RIGHT)
                    .unique(true)
                    .create();
        }

        //记录进线日志
        String operation = "管理员" + getCurrentAdminUser().getAdminUserName() +
                "更新用户认证状态,操作前状态：" + originalState.name() + "，" +
                "新状态：" + safeParse(authenticationState).name();


        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("更新认证状态");
        userServiceRecord.setComments(operation + "；说明[" + authenticationDesc + "]");
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        message.put("customerServiceRecord", userServiceRecord);
        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        message.put("createTime", sdf.format(new Date()));
        message.put("authenticationState", safeParse(authenticationState).getDescription());
        message.put("verifyTime", sdf.format(new Date()));
        message.put("success", true);

        return message;
    }

    @RequestMapping(value = "teacherhomeworkdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String getTeacherHomeworkDetail(@RequestParam("userId") Long userId,
                                           @RequestParam int day,
                                           Model model) {

        //todo : 将来这里可以写成分页
        PageRequest pageable = new PageRequest(0, 200);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
//        if (teacher == null) {
//            return "crm/teacher/teacherhomeworkdetail";
//        }
//
//        Page<HomeworkHistoryListMapper> page = businessHomeworkServiceClient.getHomeworkHistory(teacher, null,
//                DateUtils.nextDay(new Date(), -1 * day), null, pageable);
//        List<HomeworkHistoryListMapper> historyListMapperList = Collections.emptyList();
//        if (page != null)
//            historyListMapperList = page.getContent();
//
//        List<Map<String, Object>> teacherHomeworkHistoryList = new ArrayList<>();
//        for (HomeworkHistoryListMapper historyListMapper : historyListMapperList) {
//            MapMessage mapMessage = null;
//            Subject subject = historyListMapper.getHomeworkType().getSubject();
//
//            if (historyListMapper.getHomeworkType() == HomeworkType.MIDDLESCHOOL_HOMEWORK) {
//                mapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(historyListMapper.getHomeworkId(), HomeworkType.MIDDLESCHOOL_HOMEWORK);
//            } else {
//                switch (subject) {
//                    case ENGLISH:
//                        mapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(historyListMapper.getHomeworkId(), HomeworkType.ENGLISH);
//                        break;
//                    case MATH:
//                        mapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(historyListMapper.getHomeworkId(), HomeworkType.MATH);
//                        break;
//                    case CHINESE:
//                        mapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(historyListMapper.getHomeworkId(), HomeworkType.CHINESE);
//                        break;
//                    default:
//                        break;
//                }
//            }
//
//            Map<String, Object> map = new HashMap<>();
//
//            if (mapMessage == null) {
//                continue;
//            }
//            map.put("homeworkId", historyListMapper.getHomeworkId());
//            map.put("homeworkSubject", subject);
//            map.put("homeworkStartDate", mapMessage.get("homeworkStartDate"));
//            map.put("homeworkEndDate", mapMessage.get("homeworkEndDate"));
//            map.put("schoolName", mapMessage.get("schoolName"));
//            map.put("checkTime", historyListMapper.getCheckTime());
//            map.put("clazzId", historyListMapper.getClazzId());
//            map.put("clazzName", mapMessage.get("clazzName"));
//            map.put("schoolId", mapMessage.get("schoolId"));
//            map.put("schoolName", mapMessage.get("schoolName"));
//            int stucentCount = SafeConverter.toInt(mapMessage.get("joinCount")) + SafeConverter.toInt(mapMessage.get("completeCount")) + SafeConverter.toInt(mapMessage.get("undoCount"));
//            int joinCount = SafeConverter.toInt(mapMessage.get("joinCount")) + SafeConverter.toInt(mapMessage.get("completeCount"));
//            map.put("studentCount", stucentCount);
//            map.put("joinCount", joinCount);
//            map.put("completeCount", mapMessage.get("completeCount"));
////            map.put("bookName", (report1 != null) ? report1.getBookName() : report2.getBookName());
//
//            PossibleCheatingHomework possibleCheatingHomework = crmTeacherService.loadPossibleCheatingHomework(teacher.getId(), historyListMapper.getHomeworkId(), historyListMapper.getHomeworkType());
//            map.put("possibleCheat", possibleCheatingHomework);
//
//            //时间大于2014-03-24的作业不用手动检查ip了
//            if (StringUtils.isNotBlank(historyListMapper.getCheckTime())) {
//                Date checkDate = DateUtils.stringToDate(historyListMapper.getCheckTime());
//                Date flagDate = DateUtils.stringToDate("2014-03-24 00:00:00");
//                map.put("handcheck", (checkDate != null && checkDate.compareTo(flagDate) <= 0));
//            } else {
//                map.put("handcheck", false);
//            }
//            HomeworkType homeworkType = HomeworkType.of(teacher.getSubject().name());
//            if (teacher.isJuniorTeacher()) {
//                homeworkType = HomeworkType.MIDDLESCHOOL_HOMEWORK;
//            }
////            List<String> ips = crmTeacherService.countHomeworkAccomplishmentIp(historyListMapper.getHomeworkId(), teacher.getSubject());
//            Accomplishment accomplishment = null;
//            HomeworkLocation location = HomeworkLocation.newInstance(homeworkType, historyListMapper.getHomeworkId());
//            IHomework homework = homeworkLoaderClient.loadHomework(location);
//            if (homework != null) {
//                accomplishment = accomplishmentLoaderClient.loadAccomplishment(homework.getHomeworkCreateTime(), location);
//            }
//            Set<String> ips = new HashSet<>();
//            if (accomplishment != null && accomplishment.getDetails() != null) {
//                ips.addAll(accomplishment.getDetails().values()
//                        .stream()
//                        .filter(detail -> StringUtils.isNotBlank(detail.getIp()))
//                        .map(Accomplishment.Detail::getIp)
//                        .collect(Collectors.toList())
//                );
//            }
//            map.put("ipcount", ips.size());
//
//            teacherHomeworkHistoryList.add(map);
//        }
//
//        model.addAttribute("teacherHomeworkHistoryList", teacherHomeworkHistoryList);
//        model.addAttribute("userId", userId);
//        model.addAttribute("day", day);
        if (teacher.getKtwelve() == Ktwelve.JUNIOR_SCHOOL) {
            return "crm/teacher/teacherhomeworkdetail_middleschool";
        } else {
            return "crm/teacher/teacherhomeworkdetail";
        }
    }

    //查询消耗的学豆
    @RequestMapping(value = "getusedhomeworkprize.vpage")
    @ResponseBody
    public MapMessage getUsedHomeworkPrize(@RequestParam String homeworkId, @RequestParam String homeworkType) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 检查作业补充金币，老师按照每个学生15积分，学生按照每个学生11积分，金币类型：其他_产品业务139，补做【已加检查】
     */
    @RequestMapping(value = "addnewhomeworkintegral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addNewHomeworkIntegral(@RequestParam Long teacherId, @RequestParam String homeworkId) {
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null || StringUtils.isBlank(homeworkId)) return MapMessage.errorMessage("教师不存在");

        NewHomework homework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (homework == null || !homework.isHomeworkChecked()) return MapMessage.errorMessage("作业不存在或者还未检查");
        HomeworkType ht = HomeworkType.of(homework.getSubject().name());

        PossibleCheatingHomework pch = newHomeworkLoaderClient.loadPossibleCheatingHomeworkByTeacherIdAndHomeworkId(teacherId, homeworkId, ht);
        if (pch != null && !pch.getRecordOnly()) return MapMessage.errorMessage("该作业是作弊作业，请使用给作弊作业补加园丁豆的方法");

        // 在正常时间内完成作业的学生数量
        NewAccomplishment acc = newAccomplishmentLoaderClient.loadNewAccomplishment(homework.toLocation());
        int count = acc == null ? 0 : (int) acc.getDetails().values().stream().filter(d -> !d.isRepairTrue()).count();

        if (count > 0) {
            GroupMapper group = deprecatedGroupLoaderClient.loadGroup(homework.getClazzGroupId(), false);
            String fullName = "";
            if (group != null) {
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(group.getClazzId());
                fullName = clazz == null ? "" : clazz.formalizeClazzName();
            }
            int gold = new BigDecimal(count).multiply(new BigDecimal(1.5D)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            IntegralHistory integralHistory = new IntegralHistory(teacherId, 其他_产品业务, gold * 10);
            integralHistory.setComment(StringUtils.formatMessage("共{}个{}的学生完成作业，您获得园丁豆", count, fullName));
            integralHistory.setHomeworkUniqueKey(ht.name(), homeworkId);
            userIntegralService.changeIntegral(integralHistory);
            return MapMessage.successMessage("搞定了");
        } else {
            return MapMessage.errorMessage("没有人完成");
        }
    }

    /**
     * 检查作业补充金币，老师按照每个学生15积分，学生按照每个学生11积分，金币类型：其他_产品业务139，补做【已加检查】
     */
    @RequestMapping(value = "addhomeworkintegral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addHomeworkIntegral(@RequestParam Long teacherId, @RequestParam String homeworkId) {
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null || StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("教师不存在");
        }

        int finishCount = 0;
        String fullName;

        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
        if (newHomework == null || !newHomework.isHomeworkChecked()) {
            return MapMessage.errorMessage("作业不存在或者还未检查");
        }
        String accomplishmentId = NewAccomplishment.ID.build(newHomework.getCreateAt(), newHomework.getSubject(), newHomework.getId()).toString();
        NewAccomplishment newAccomplishment = newAccomplishmentLoaderClient.__loadNewAccomplishment(accomplishmentId);
        for (String studentId : newAccomplishment.getDetails().keySet()) {
            NewAccomplishment.Detail detail = newAccomplishment.getDetails().get(studentId);
            if (detail.getAccomplishTime().before(newHomework.getCheckedAt())) {
                finishCount++;
            }
        }
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(newHomework.getClazzGroupId(), false);
        if (groupMapper == null) {
            return MapMessage.errorMessage("作业班组不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(groupMapper.getClazzId());
        if (clazz == null) {
            return MapMessage.errorMessage("作业班级不存在");
        }
        fullName = clazz.formalizeClazzName() + groupMapper.getGroupName();

        if (finishCount > 0) {
            int gold = new BigDecimal(finishCount).multiply(new BigDecimal(1.5D)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            IntegralHistory integralHistory = new IntegralHistory(teacherId, 其他_产品业务, gold * 10);
            integralHistory.setComment(StringUtils.formatMessage("共{}个{}的学生完成作业，您获得园丁豆", finishCount, fullName));
            integralHistory.setHomeworkUniqueKey("ENGLISH", homeworkId);
            userIntegralService.changeIntegral(integralHistory);
            return MapMessage.successMessage("搞定了");
        } else {
            return MapMessage.errorMessage("没有人完成");
        }
    }

    @RequestMapping(value = "changesubject.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeTeacherSubject() {
        Long teacherId = getRequestLong("teacherId");
        String subjectName = getRequestString("subject");
        String desc = getRequestString("desc");
        Boolean confirm = getRequestBool("confirm");
        if (StringUtils.isEmpty(desc)) {
            return MapMessage.errorMessage("请填写描述！");
        }
        Subject subject = Subject.ofWithUnknown(subjectName);
        if (subject == Subject.UNKNOWN) {
            return MapMessage.errorMessage("不存在此学科");
        }

        // FIXME 没找到这个force是干嘛用的
        Boolean force = SafeConverter.toBoolean(getRequestString("force"));

        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("老师不在任何学校中");
        }

//        if (!force && (school.isJuniorSchool() || school.isJuniorSchool())) {
//            return MapMessage.errorMessage("暂不允许初高中老师更改学科，如需变更请联系产品同学");
//        }

        // 简单检查一下学科的合法性
        if (school.isPrimarySchool() && !BASIC_SUBJECTS.contains(subject)) {
            return MapMessage.errorMessage("抱歉，小学暂时不支持更改为该学科: " + subject.getValue());
        }

        if ((school.isMiddleSchool() || school.isSeniorSchool()) && !ALL_SUBJECTS.contains(subject)) {
            return MapMessage.errorMessage("抱歉，初高中暂时不支持更改为该学科 : " + subject.getValue());
        }

        // 检查是否有未处理的换班相关（包括转让、接管以及添加异科老师）请求
        List<ClazzTeacherAlteration> alterations = teacherLoaderClient.loadApplicantOrRespondentAlterations(teacherId)
                .stream()
                .filter(e -> {
                    Clazz clazz = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            .loadClazz(e.getClazzId());
                    return clazz != null && !clazz.isTerminalClazz() && e.getState() == ClazzTeacherAlterationState.PENDING
                            && (e.getType() == ClazzTeacherAlterationType.LINK
                            || e.getType() == ClazzTeacherAlterationType.TRANSFER
                            || e.getType() == ClazzTeacherAlterationType.REPLACE);
                }).collect(Collectors.toList());
        if (!confirm) {
            if (alterations.size() > 0) {
                return MapMessage.successMessage("您的账号{},存在接管班级或转让班级的申请, 继续修改学科将自动取消转让/接管班级\n 是否确认继续修改学科? ", teacherId).add("hasPendingApplications", true);
            }
        } else {
            // 先取消换班相关
            for (ClazzTeacherAlteration e : alterations) {
                MapMessage msg = teacherAlterationServiceClient.cancelApplication(e.getApplicantId(), e.getId(), e.getType(), OperationSourceType.crm);
                if (!msg.isSuccess()) {
                    return msg;
                }
            }
        }
        // 更改学科
        MapMessage message = crmTeacherSystemClazzService.changeTeacherSubject(teacherId, subject, getCurrentAdminUser().getAdminUserName());
        if (!message.isSuccess()) {
            return message;
        }

        // 保险起见这里再清一次
        raikouSystem.getCacheService().evictUserCache(teacherId);

        //记录进线日志
        String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "更改老师学科,班级老师：" + teacherId + ",更改后学科:" + subject.getValue();

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("修改学科");
        userServiceRecord.setComments(operation + "；说明[" + desc + "]");
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        // 更新app session
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Teacher", teacherId);
        if (vendorAppsUserRef != null) {
            vendorServiceClient.expireSessionKey(
                    "17Teacher",
                    teacherId,
                    SessionUtils.generateSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), teacherId));
        }

        return MapMessage.successMessage("操作成功！");
    }

    /**
     * ***********************班级操作*****************************************************************
     */
//    @RequestMapping(value = "resetclazzpassword.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public Map<String, Object> resetClazzPassword(@RequestParam("teacherId") Long teacherId,
//                                                  @RequestParam("clazzId") Long clazzId,
//                                                  @RequestParam("password") String password,
//                                                  @RequestParam("passwordDesc") String passwordDesc) {
//
//        Map<String, Object> message = new HashMap<>();
//
//        password = password.replaceAll("\\s", "");
//        passwordDesc = passwordDesc.replaceAll("\\s", "");
//
//        List<User> students = studentLoaderClient.loadClazzStudents(clazzId);
//
//        if (StringUtils.isBlank(password) || StringUtils.isBlank(passwordDesc) || (students == null)) {
//
//            message.put("success", false);
//            message.put("errorInfo", "修改失败，请填写新密码和备注，并检查班级中是否存在学生。");
//
//            return message;
//        }
//
//        boolean success = true;
//        String errorInfo = "";
//
//        if (success) {
//
//            //记录进线日志
//            String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "重置班级密码，" +
//                    "老师ID：" + teacherId + "，班级ID：" + clazzId + "。";
//            CustomerServiceRecord customerServiceRecord = customerServiceRecordPersistence.addCustomerServiceRecord(
//                    teacherId, getCurrentAdminUser().getAdminUserName(), RecordType.老师操作, passwordDesc, operation);
//
//            message.put("customerServiceRecord", customerServiceRecord);
//            FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
//            message.put("createTime", sdf.format(customerServiceRecord.getCreateDatetime()));
//
//            //记录管理员操作日志
//            addAdminLog(operation, clazzId, passwordDesc);
//
//            for (User student : students) {
//                MapMessage msg = userServiceClient.setPassword(student, password);
//                if (!msg.isSuccess()) {
//                    success = false;
//                    message.put("errorInfo", msg.getInfo());
//                    break;
//                }
//                LogCollector.instance().info("password_change_track", passwordChangeTrackMap(student.getId(),
//                        "CrmTeacherController_/crm/teacher/resetclazzpassword.vpage"));
//                // TODO PWD_HISTORY_CRM By Wyc 2016-06-03
//                Map<String, String> infoMap = MiscUtils.map(
//                        "env", RuntimeMode.getCurrentStage()
//                        , "platform", "crm"
//                        , "op_type", "update_pwd"
//                        , "url", getRequest().getRequestURL().toString()
//                        , "user_agent", getRequest().getHeader("User-Agent")
//                        , "operator", getCurrentAdminUser().getAdminUserName()
//                        , "user", student.getId()
//                        , "user_type", UserType.STUDENT.getDescription()
//                );
//                LogCollector.instance().info("pwd_history", infoMap);
//            }
//        } else
//            message.put("errorInfo", errorInfo);
//
//        message.put("success", success);
//
//        return message;
//    }

    /**
     * ***********************修改老师信息*****************************************************************
     */
    @RequestMapping(value = "changeschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeSchool() {
        Long teacherId = NumberUtils.toLong(getRequest().getParameter("teacherId"));
        Long schoolId;
        String changeSchoolDesc;

        //用于 /crm/teacher/teacherhomepage.vpage 页面点击[修改学校]的预判，如果
        // 1.老师所在班中还有其他老师则不许修改
        // 2.老师是校园大使则不许修改
        String precheck = getRequest().getParameter("precheck");
        if ("true".equals(precheck)) {
            School sourceSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(teacherId)
                    .getUninterruptibly();

            if (sourceSchool != null) {
                Long targetSchoolId = NumberUtils.toLong(getRequestParameter("schoolId", "").replaceAll("\\s", ""));
                SchoolExtInfo sourceSchoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                        .loadSchoolExtInfo(sourceSchool.getId())
                        .getUninterruptibly();
                SchoolExtInfo targetSchoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                        .loadSchoolExtInfo(targetSchoolId)
                        .getUninterruptibly();
                if (sourceSchoolExtInfo != null && targetSchoolExtInfo != null && sourceSchoolExtInfo.isScanMachineFlag() && targetSchoolExtInfo.isScanMachineFlag()) {
                    return MapMessage.errorMessage("两所学校均开通了阅卷机，无法给老师带班转校");
                }
            }

            List<GroupMapper> groupMapperList = deprecatedGroupLoaderClient.loadTeacherGroupsByTeacherId(teacherId, false);

            for (GroupMapper mapper : groupMapperList) {
                Set<Long> sharedGroupIds = deprecatedGroupLoaderClient.loadSharedGroupIds(mapper.getId());
                if (sharedGroupIds.size() > 0) {
                    return MapMessage.errorMessage("老师(ID:" + teacherId + ")所在班级(ID:" + mapper.getClazzId() + ")存在其他老师");
                }
            }
            return MapMessage.successMessage();
        }

        try {
            teacherId = NumberUtils.toLong(getRequestParameter("teacherId", ""));
            schoolId = NumberUtils.toLong(getRequestParameter("schoolId", "").replaceAll("\\s", ""));
            changeSchoolDesc = getRequestParameter("changeSchoolDesc", "").replaceAll("\\s", "");
        } catch (Exception ignored) {
            return MapMessage.errorMessage("请检查填写的内容，注意描述信息不能为空");
        }

        if (changeSchoolDesc.length() == 0) {
            return MapMessage.errorMessage("描述信息不能为空");
        }

        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage("老师不存在");
        }

        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("学校ID" + schoolId + "不存在");
        }

        List<GroupTeacherMapper> groupMapperList = deprecatedGroupLoaderClient.loadTeacherGroups(teacherId, false);
        Set<Long> teacherGroupIds = groupMapperList.stream().map(GroupMapper::getId).collect(Collectors.toSet());
        Map<Long, Set<Long>> sharedGroupIds = deprecatedGroupLoaderClient.loadSharedGroupIds(teacherGroupIds);
        for (Long key : sharedGroupIds.keySet()) {
            if (sharedGroupIds.get(key).size() > 0) {
                return MapMessage.errorMessage("老师(ID:" + teacherId + ")所在组(" + key + ")关联着其他老师，不能修改学校");
            }
        }

        // FIX BUG
        // 需要检查老师名下的学生是否在其他组，如果在则不能转校
        Map<Long, List<Long>> studentIdMap = studentLoaderClient.loadGroupStudentIds(teacherGroupIds);
        Set<Long> studentIdSet = studentIdMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Map<Long, List<GroupMapper>> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentIdSet, false);
        for (Map.Entry<Long, List<GroupMapper>> entry : studentGroups.entrySet()) {
            Long u = entry.getKey();
            List<GroupMapper> gs = entry.getValue();
            if (gs.size() > 1) {
                return MapMessage.errorMessage("学生" + u + "关联着其他老师，不能修改学校");
            }
        }

        School originalSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (originalSchool != null) {
            if (Objects.equals(originalSchool.getId(), schoolId)) {
                return MapMessage.errorMessage("修改前后的学校不能为同一学校");
            }

            if (!Objects.equals(school.getLevel(), originalSchool.getLevel()) && groupMapperList.size() > 0) {
                return MapMessage.errorMessage("该老师目前有执教班级,不能跨学段更换学校");
            }

            SchoolExtInfo sourceSchoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(originalSchool.getId())
                    .getUninterruptibly();
            SchoolExtInfo targetSchoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(school.getId())
                    .getUninterruptibly();
            boolean sourceScanMachineFlag = sourceSchoolExtInfo != null && sourceSchoolExtInfo.isScanMachineFlag();
            boolean targetScanMachineFlag = targetSchoolExtInfo != null && targetSchoolExtInfo.isScanMachineFlag();
            if (sourceScanMachineFlag && targetScanMachineFlag) {
                return MapMessage.errorMessage("两所学校均开通了阅卷机，无法给老师带班转校");
            }

            if (!sourceScanMachineFlag && targetScanMachineFlag) {//来源学校有阅卷机权限,目标学校没有阅卷机权限时,删除老师名下学生的阅卷机号
                newKuailexueServiceClient.clearTeacherStudentScanNumber(originalSchool.getId(), teacher.getId());
            }
        }


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

        // 更换分组班级关系
        MapMessage msg = crmTeacherSystemClazzService.changeTeacherSchool(teacherId, teacherGroupIds, schoolId, getCurrentAdminUser().getAdminUserName());
        if (!msg.isSuccess()) {
            return msg;
        }

        //清理前台cache
        asyncUserServiceClient.getAsyncUserService().evictUserCache(teacherId).awaitUninterruptibly();

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.校园大使.name());
        userServiceRecord.setOperationContent("管理员修改老师学校");
        userServiceRecord.setComments("原学校:" + (originalSchool == null ? "无" : originalSchool.getId()) + "，修改后的学校" + schoolId);
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        MapMessage message = MapMessage.successMessage("修改成功").add("customerServiceRecord", userServiceRecord);
        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        message.add("createTime", sdf.format(new Date()));
        return message;
    }

//    @RequestMapping(value = "pendingteacher.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage pendingTeacher() {
//
//        long teacherId = getRequestLong("teacherId");
//        int pending = getRequestInt("pending");
//        String desc = getRequestParameter("desc", "").trim();
//        if (StringUtils.isEmpty(desc))
//            return MapMessage.errorMessage("请填写原因");
//
//        userServiceClient.updatePending(teacherId, pending);
//
//        String content = "暂停老师";
//        if (pending != 1) {
//            content = "恢复暂停老师";
//        }
//        // 记录 UserServiceRecord
//        UserServiceRecord userServiceRecord = new UserServiceRecord();
//        userServiceRecord.setUserId(teacherId);
//        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
//        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
//        userServiceRecord.setOperationContent(content);
//        userServiceRecord.setComments("说明[" + desc + "]");
//        crmSummaryServiceClient.saveUserServiceRecord(userServiceRecord);
//
//        return MapMessage.successMessage("操作成功");
//    }

    /**
     * ***********************登录老师账号*****************************************************************
     */
    @RequestMapping(value = "teacherlogin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> teacherLogin(@RequestParam Long teacherId,
                                            @RequestParam String teacherLoginDesc) {
        Map<String, Object> message = new HashMap<>();
        teacherLoginDesc = teacherLoginDesc.replaceAll("\\s", "");

        // #60109教务账号登陆到教务系统首页
        Boolean affair = getRequestBool("affair");
        if (affair) {
            if (StringUtils.isBlank(teacherLoginDesc)) {
                message.put("success", false);
                return message;
            }
            Map<String, Object> params = new HashMap<>();
            params.put("j_username", teacherId);
            String password = userLoaderClient.generateUserTempPassword(teacherId);
            params.put("j_password", password);
            message.put("success", true);
            message.put("postUrl", UrlUtils.buildUrlQuery(ProductConfig.getMainSiteBaseUrl() + "/j_spring_security_check", params));

            // 记录一条处理日志
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.客服添加.name());
            userServiceRecord.setOperationContent("CRM跳转到教务系统主页");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            return message;
        }

        User teacher = userLoaderClient.loadUser(teacherId);

        if (StringUtils.isBlank(teacherLoginDesc) || (null == teacher) || teacher.getUserType() != UserType.TEACHER.getType())
            message.put("success", false);
        else {
            message.put("success", true);

            String password = userLoaderClient.generateUserTempPassword(teacherId);
            Map<String, Object> params = new HashMap<>();
            params.put("j_username", teacher.getId());
            params.put("j_password", password);

            Teacher tempTeacher = teacherLoaderClient.loadTeacher(teacherId);
            if (tempTeacher != null && tempTeacher.isKLXTeacher()) {
                params.put("returnURL", ProductConfig.getKuailexueUrl());
            }
            message.put("postUrl", UrlUtils.buildUrlQuery(ProductConfig.getMainSiteBaseUrl() + "/j_spring_security_check", params));
        }

        return message;
    }

    @RequestMapping(value = "delauthapplication.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delAuthApp(@RequestParam String teacherIds, @RequestParam String delDesc) {
        MapMessage message = new MapMessage();
        if (StringUtils.isEmpty(teacherIds)) {
            message.setSuccess(false);
            message.setInfo("请选择要删除申请记录的老师");
            return message;
        }
        if (StringUtils.isEmpty(delDesc)) {
            message.setSuccess(false);
            message.setInfo("请输入问题描述");
            return message;
        }
        try {
            String[] ids = teacherIds.split(",");
            for (String id : ids) {
                CertificationApplication ca = certificationServiceClient.getRemoteReference()
                        .findCertificationApplication(Long.valueOf(id)).getUninterruptibly();
                crmTeacherService.deleteTeacherCertificationApplication(Long.valueOf(id));
                addAdminLog("删除老师认证申请", Long.valueOf(id), "USER_ID", "管理员" + getCurrentAdminUser().getAdminUserName() + "删除老师" + id + "的认证申请，原因：" + delDesc, ca);
            }
            message.setSuccess(true);
        } catch (Exception ex) {
            logger.warn("删除老师认证申请失败，teacherIds:{},delDesc:{},msg:{}", teacherIds, delDesc, ex.getMessage());
            message.setSuccess(false);
            message.setInfo("操作失败，请重新再试！");
        }
        return message;
    }


    /**************************private method******************************************************************/

    /**
     * ***********************查询相关*****************************************************************
     */
    private Map<String, Object> getTeacherInfoMap(Long teacherId) {
        if (teacherId == null) {
            return new HashMap<>();
        }

        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (null == teacher || teacher.fetchUserType() != UserType.TEACHER || teacher.isDisabledTrue()) {
            return new HashMap<>();
        }
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());

        Map<String, Object> teacherInfoMap = new HashMap<>();

        teacherInfoMap.put("teacher", teacher);
        teacherInfoMap.put("qq", ua.getSensitiveQq());
        teacherInfoMap.put("canBindMobile", crmTeacherService.canBindMobile(teacherId));
        teacherInfoMap.put("isCheat", newHomeworkServiceClient.isCheatingTeacher(teacherId));

        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (school != null) {
            teacherInfoMap.put("schoolId", school.getId());
            teacherInfoMap.put("schoolLevel", school.getLevel());
            teacherInfoMap.put("schoolName", school.getCname());
            teacherInfoMap.put("schoolType", SchoolType.safeParse(school.getType()).getDescription());
            teacherInfoMap.put("regionCode", school.getRegionCode());

            ExRegion region = null;
            if (school.getRegionCode() != null)
                region = raikouSystem.loadRegion(school.getRegionCode());

            if (region != null)
                teacherInfoMap.put("regionName", region.toString("/"));
        }

        UserIntegral userIntegralMapper = teacherLoaderClient.loadMainSubTeacherUserIntegral(teacherId, teacher.getKtwelve());
        teacherInfoMap.put("integral", userIntegralMapper != null ? (userIntegralMapper.getUsable()) : 0);

        List<CertificationApplicationOperatingLog> operatingLogList = certificationServiceClient.getRemoteReference()
                .findCertificationApplicationOperatingLogs(teacherId).getUninterruptibly();
        if (operatingLogList != null) {
            for (CertificationApplicationOperatingLog operatingLog : operatingLogList) {
                if ((null == teacherInfoMap.get("verifyTime"))
                        || operatingLog.getCreateTime().after((Date) teacherInfoMap.get("verifyTime"))) {
                    teacherInfoMap.put("verifyTime", operatingLog.getCreateTime());
                }
            }
        }
        teacherInfoMap.put("verifyState", safeParse(teacher.getAuthenticationState()).getDescription());

        List<InviteHistory> inviteHistoryList = asyncInvitationServiceClient.loadByInvitee(teacherId).toList();
        if (!CollectionUtils.isEmpty(inviteHistoryList)) {
            User inviter = userLoaderClient.loadUser(inviteHistoryList.get(0).getUserId());
            if (inviter != null) {
                teacherInfoMap.put("inviterName", inviter.getProfile().getRealname());
                teacherInfoMap.put("inviterId", inviter.getId());
                teacherInfoMap.put("inviteSuccess", inviteHistoryList.get(0).getDisabled());
            }
        }

        List<TeacherActivateTeacherHistory> histories = businessManagementClient
                .findTeacherActivateTeacherHistoryMapByInviteeIds(Collections.singleton(teacherId))
                .values()
                .stream()
                .flatMap(List::stream)
                .sorted((o1, o2) -> {
                    long c1 = SafeConverter.toLong(o1.getCreateTime());
                    long c2 = SafeConverter.toLong(o2.getCreateTime());
                    return Long.compare(c2, c1);
                })
                .collect(Collectors.toList());

        TeacherActivateTeacherHistory history = MiscUtils.firstElement(histories);
        if (history != null) {
            User inviter = userLoaderClient.loadUser(history.getInviterId());
            Map<String, Object> activateHistory = new HashMap<>();
            if (inviter != null) {
                activateHistory.put("inviter", inviter);
            }
            activateHistory.put("history", history);
            teacherInfoMap.put("activateHistory", activateHistory);
        }

        List<GroupMapper> groupMapperList = deprecatedGroupLoaderClient.loadTeacherGroupsByTeacherId(teacherId, false);
        if (!CollectionUtils.isEmpty(groupMapperList)) {
            List<Map> clazzList = new ArrayList<>();
            List<Map> groupNotInClazzList = new ArrayList<>();
            for (GroupMapper mapper : groupMapperList) {
                if (mapper != null) {
                    //判断老师和班级的关系，为了兼容老的数据关系
                    Clazz clazzInfo = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            .loadClazz(mapper.getClazzId());
                    if (clazzInfo != null) {
                        Map<String, Object> clazzMap = new HashMap<>();
                        clazzMap.put("groupId", mapper.getId());
                        clazzMap.put("id", clazzInfo.getId());
                        clazzMap.put("classLevel", clazzInfo.getClassLevel());
                        clazzMap.put("className", clazzInfo.formalizeClazzName());
                        clazzMap.put("classType", clazzInfo.getClassType());
                        if (teacherLoaderClient.isTeachingClazz(teacherId, mapper.getClazzId())) {
                            clazzList.add(clazzMap);
                        } else {
                            groupNotInClazzList.add(clazzMap);
                        }
                    }
                }
            }

            clazzList.sort((o1, o2) -> {
                final Integer o1_ClassLevel = Integer.valueOf((String) o1.get("classLevel"));
                final Integer o2_ClassLevel = Integer.valueOf((String) o2.get("classLevel"));
                if (o1_ClassLevel.equals(o2_ClassLevel)) {
                    return Long.compare((Long) o1.get("id"), (Long) o2.get("id"));
                } else {
                    return Integer.compare(o1_ClassLevel, o2_ClassLevel);
                }
            });
            groupNotInClazzList.sort((o1, o2) -> {
                final Integer o1_ClassLevel = Integer.valueOf((String) o1.get("classLevel"));
                final Integer o2_ClassLevel = Integer.valueOf((String) o2.get("classLevel"));
                if (o1_ClassLevel.equals(o2_ClassLevel)) {
                    return Long.compare((Long) o1.get("id"), (Long) o2.get("id"));
                } else {
                    return Integer.compare(o1_ClassLevel, o2_ClassLevel);
                }
            });

            Map<Object, List<Map>> clazzLevelMap = new HashMap<>();
            for (Map clazzMap : clazzList) {
                Object key = clazzMap.get("classLevel");
                List<Map> clazzLevelChildList = clazzLevelMap.computeIfAbsent(key, k -> new ArrayList<>());
                clazzLevelChildList.add(clazzMap);
            }

            Map<Object, List<Map>> groupNotInClazzLevelMap = new HashMap<>();
            for (Map clazzMap : groupNotInClazzList) {
                Object key = clazzMap.get("classLevel");
                List<Map> clazzLevelChildList = groupNotInClazzLevelMap.computeIfAbsent(key, k -> new ArrayList<>());
                clazzLevelChildList.add(clazzMap);
            }

            teacherInfoMap.put("clazzLevelList", clazzLevelMap.values());
            teacherInfoMap.put("groupNotInClazzLevelList", groupNotInClazzLevelMap.values());
        }

        List<UserServiceRecord> customerServiceRecordList = userLoaderClient.loadUserServiceRecords(teacherId);
        teacherInfoMap.put("customerServiceRecordList", customerServiceRecordList);

        return teacherInfoMap;
    }

    private List<Long> getTeacherIdList(Map conditionMap) {
        List<Long> teacherIdList = new ArrayList<>();

        //如果查询条件中包含“teacherId”,则忽略其他条件
        if (StringUtils.isNotBlank((String) conditionMap.get("teacherId"))) {
            try {
                Long teacherId = conversionService.convert(conditionMap.get("teacherId"), Long.class);
                User teacher = userLoaderClient.loadUser(teacherId);
                if (teacher != null && teacher.getUserType() == UserType.TEACHER.getType() && !teacher.getDisabled()) {
                    teacherIdList.add(teacherId);
                }

            } catch (Exception ignored) {
                //do nothing here
            }
        } else if (StringUtils.isNotBlank((String) conditionMap.get("teacherMobile"))) {
            try {
                String userMobile = SafeConverter.toString(conditionMap.get("teacherMobile"));
                List<User> teachers = userLoaderClient.loadUserByToken(userMobile);
                teachers = teachers.stream().filter(p -> p.fetchUserType() == UserType.TEACHER && !p.isDisabledTrue()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(teachers)) {
                    teacherIdList.addAll(teachers.stream().map(User::getId).collect(Collectors.toList()));
                }

            } catch (Exception ignored) {
                //do nothing here
            }
        } else {

            if (StringUtils.isNotBlank((String) conditionMap.get("klxLoginName"))) {
                MapMessage mapMessage = newKuailexueLoaderClient.loadTeacherIdByLoginName((String) conditionMap.get("klxLoginName"));
                if (mapMessage.isSuccess() && mapMessage.containsKey("teacherId")) {
                    teacherIdList.add((Long) mapMessage.get("teacherId"));
                }
            }

            if (StringUtils.isNotBlank((String) conditionMap.get("klxUserName"))) {
                MapMessage mapMessage = newKuailexueLoaderClient.loadTeacherIdByUserName((String) conditionMap.get("klxUserName"));
                if (mapMessage.isSuccess() && mapMessage.containsKey("teacherId")) {
                    teacherIdList.add((Long) mapMessage.get("teacherId"));
                }
            }
        }

        conditionMap.put("currentPage", "1");
        conditionMap.put("totalPage", "1");

        return teacherIdList;
    }

//    private List<Long> getTeacherAuthenticationIdList(Map conditionMap) {
//
//        // FIXME COMMENT BY ZHAO REX FOR startDate may not have been initialized.
//        Date startDate = null;
//        // FIXME COMMENT BY ZHAO REX FOR endDate may not have been initialized.
//        Date endDate = null;
//        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd");
//        try {
//            String startDateStr = conditionMap.get("startDate").toString().trim();
//            if (StringUtils.isNotBlank(startDateStr))
//                startDate = sdf.parse(startDateStr);
//
//            String endDateStr = conditionMap.get("endDate").toString().trim();
//            if (StringUtils.isNotBlank(endDateStr))
//                endDate = sdf.parse(endDateStr);
//
//        } catch (Exception ignored) {
//            //leave it alone
//        }
//
//        List<Long> teacherIdList = new ArrayList<>();
//
//        Integer currentPage = conversionService.convert(conditionMap.get("currentPage"), Integer.class);
//
//        String queryTeacher;
//        Map<String, Object> queryTeacherParams = new HashMap<>();
//
//        // 查询已满足前置条件但未自动通过认证的老师
//        if ("2".equals(conditionMap.get("authenticationState"))) {
//            queryTeacher = "SELECT DISTINCT TEACHER_ID FROM VOX_PREMISE_AUTHENTICATE_TEACHER WHERE DISABLED = FALSE";
//            teacherIdList = utopiaSql.withSql(queryTeacher).queryColumnValues(Long.class);
//
//            teacherIdList = userLoaderClient.loadUsers(teacherIdList).values()
//                    .stream()
//                    .filter(t -> t.getAuthenticationState() != null)
//                    .filter(t -> t.getAuthenticationState() != 1)
//                    .filter(t -> t.getAuthenticationState() != 3)
//                    .map(User::getId)
//                    .collect(Collectors.toList());
//            // 后面不再进行查询
//            queryTeacher = "";
//            // 查询系统自动认证过的老师
//        } else {
//            queryTeacher = " select distinct vcaol.APPLICANT_ID,vcaol.CREATETIME from VOX_CERTIFICATION_APPLICATION_OPERATING_LOG vcaol " +
//                    " inner join UCT_USER uu on uu.ID = vcaol.APPLICANT_ID and uu.AUTHENTICATION_STATE = :authenticationState " +
//                    " where vcaol.OPERATOR_NAME = '自动认证' ";
//            queryTeacherParams.put("authenticationState", SUCCESS.getState());
//
//            // FIXME COMMENT BY ZHAO REX FOR startDate may not have been initialized.
//            if (null != startDate) {
//                queryTeacher += " AND vcaol.CREATETIME >= :startDate ";
//                queryTeacherParams.put("startDate", DayRange.newInstance(startDate.getTime()).getStartDate());
//            }
//            // FIXME COMMENT BY ZHAO REX FOR endDate may not have been initialized.
//            if (null != endDate) {
//                queryTeacher += " AND vcaol.CREATETIME <= :endDate ";
//                queryTeacherParams.put("endDate", DayRange.newInstance(endDate.getTime()).getEndDate());
//            }
//            queryTeacher += " order by vcaol.CREATETIME desc";
//
//        }
//        if (StringUtils.isNotBlank(queryTeacher)) {
//            teacherIdList = utopiaSql.withSql(queryTeacher).useParams(queryTeacherParams).queryColumnValues(Long.class);
//
//            if (CollectionUtils.isEmpty(teacherIdList))
//                return Collections.emptyList();
//        }
//        //对于根据老师信息查出的结果，以schoolId,userId升序排序， 另外的查询分支和学校相关，在sql语句中指定了顺序
//        if (!CollectionUtils.isEmpty(teacherIdList) && ("2".equals(conditionMap.get("authenticationState")) || "3".equals(conditionMap.get("authenticationState")))) {
//            Map<String, Object> params = new HashMap<>();
//            params.put("a", teacherIdList);
//            String sortQuery = " select distinct vtsr.USER_ID from VOX_TEACHER_SUBJECT_REF vtsr " +
//                    " left join VOX_GROUP_TEACHER_REF vgtr on vgtr.TEACHER_ID = vtsr.USER_ID and vgtr.STATUS='VALID' and vgtr.DISABLED = 0 " +
//                    " left join VOX_CLAZZ_GROUP vcg on vcg.ID = vgtr.CLAZZ_GROUP_ID and vcg.DISABLED = 0 " +
//                    " left join VOX_CLASS vc on vc.ID = vcg.CLAZZ_ID and vc.DISABLED = 0 " +
//                    " where vtsr.USER_ID in (:a) and vtsr.DISABLED = 0 order by vc.SCHOOL_ID, vtsr.USER_ID ";
//            teacherIdList = utopiaSql.withSql(sortQuery).useParams(params).queryColumnValues(Long.class);
//        }
//
//        // 简易分页方案
//        int totalPage = (int) Math.ceil(teacherIdList.size() * 1.0 / MAX_TEACHER_AMOUNT);
//        conditionMap.put("totalPage", totalPage);
//
//        if (currentPage > totalPage) {
//            currentPage = totalPage;
//            conditionMap.put("currentPage", currentPage.toString());
//        }
//
//        if (currentPage > 0) {
//            int beginIndex = (currentPage - 1) * MAX_TEACHER_AMOUNT;
//            int endIndex = beginIndex + MAX_TEACHER_AMOUNT <= teacherIdList.size() ? beginIndex + MAX_TEACHER_AMOUNT : teacherIdList.size();
//            List<Long> tempTeacherIdList = teacherIdList.subList(beginIndex, endIndex);
//            teacherIdList = new ArrayList<>(tempTeacherIdList);
//        } else {
//            teacherIdList = Collections.emptyList();
//        }
//
//        return teacherIdList;
//
//    }

//    //导出符合前置条件 但未自动认证成功的老师
//    @RequestMapping(value = "downloadpremiseteacher.vpage", method = RequestMethod.GET)
//    public void downloadPremiseTeacher(HttpServletResponse response) {
//        String queryTeacher = "SELECT DISTINCT TEACHER_ID FROM VOX_PREMISE_AUTHENTICATE_TEACHER WHERE DISABLED = FALSE";
//        List<Long> teacherIdList = utopiaSql.withSql(queryTeacher).queryColumnValues(Long.class);
//        Map<Long, User> map = userLoaderClient.loadUsers(teacherIdList);
//        teacherIdList = map.values().stream()
//                .filter(u -> u != null)
//                .filter(u -> u.getAuthenticationState() != null)
//                .filter(u -> u.getAuthenticationState() != 1)
//                .filter(u -> u.getAuthenticationState() != 3)
//                .map(User::getId)
//                .collect(Collectors.toList());
//        List<Map<String, Object>> data = getTeacherSnapshot(teacherIdList);
//        XSSFWorkbook xssfWorkbook = convertToPremiseXSSF(data);
//
//        String filename = "满足前置条件未认证老师" + "-" + DateUtils.dateToString(new Date()) + ".xlsx";
//        try {
//            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            xssfWorkbook.write(outStream);
//            outStream.flush();
//            HttpRequestContextUtils.currentRequestContext().downloadFile(
//                    filename,
//                    "application/vnd.ms-excel",
//                    outStream.toByteArray());
//        } catch (IOException ignored) {
//            try {
//                response.getWriter().write("不能下载");
//                response.sendError(HttpServletResponse.SC_FORBIDDEN);
//            } catch (IOException e) {
//                logger.error("download auditing order exception!");
//            }
//        }
//    }

    private XSSFWorkbook convertToPremiseXSSF(List<Map<String, Object>> exportList) {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        for (int i = 0; i <= 3; i++) {
            xssfSheet.setColumnWidth(i, 200 * 15);
        }
        XSSFRow firstRow = xssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("用户ID");
        firstRow.createCell(1).setCellValue("用户姓名");
        firstRow.createCell(2).setCellValue("进线日志数目");
        firstRow.createCell(3).setCellValue("手机");
        firstRow.createCell(4).setCellValue("邮箱");
        firstRow.createCell(5).setCellValue("学科");
        firstRow.createCell(6).setCellValue("学校");
        firstRow.createCell(7).setCellValue("省市区");
        firstRow.createCell(8).setCellValue("园丁豆");
        firstRow.createCell(9).setCellValue("是否认证");
        firstRow.createCell(10).setCellValue("申请状态更新时间");
        int rowNum = 1;
        for (Map<String, Object> data : exportList) {
            XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
            xssfRow.setHeightInPoints(20);
            xssfRow.createCell(0).setCellValue(data.get("teacherId").toString());
            xssfRow.createCell(1).setCellValue(data.get("teacherName").toString());
            xssfRow.createCell(2).setCellValue(data.get("customerServiceRecordCount").toString());
            xssfRow.createCell(3).setCellValue(data.get("teacherMobile").toString());
            xssfRow.createCell(4).setCellValue(data.get("teacherEmail").toString());
            xssfRow.createCell(5).setCellValue(data.get("subjectName").toString());
            xssfRow.createCell(6).setCellValue(data.get("schoolName").toString() + "(" + data.get("schoolId").toString() + ")");
            xssfRow.createCell(7).setCellValue(data.get("regionName").toString() + "(" + data.get("regionCode").toString() + ")");
            xssfRow.createCell(8).setCellValue(data.get("integral").toString());
            xssfRow.createCell(9).setCellValue(data.get("verifiedState").toString());
            xssfRow.createCell(10).setCellValue(data.get("applyDate") == null ? "" : data.get("applyDate").toString());
        }
        xssfSheet.setColumnWidth(4, 300 * 15);
        xssfSheet.setColumnWidth(6, 700 * 15);
        xssfSheet.setColumnWidth(7, 400 * 15);
        xssfSheet.setColumnWidth(10, 200 * 15);
        return xssfWorkbook;
    }

    private List<Map<String, Object>> getTeacherSnapshot(List<Long> teacherIdList) {

        List<Map<String, Object>> teacherList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(teacherIdList)) {
            for (Long teacherId : teacherIdList) {

                Map<String, Object> teacherInfo = new HashMap<>();

                // TODO COMMENT BY ZHAO REX change to multi load Teachers?
                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacherId);
                if (null == teacher || null == ua) continue;


                teacherInfo.put("teacherId", teacherId);
                teacherInfo.put("subjectName", (teacher.getSubject() == null) ? null : teacher.getSubject().getValue());
                if (teacher.getProfile() != null) {
                    teacherInfo.put("teacherName", teacher.getProfile().getRealname());
                    teacherInfo.put("teacherMobile", ua.getSensitiveMobile());
                    teacherInfo.put("teacherEmail", ua.getSensitiveEmail());
                }

                School school = asyncTeacherServiceClient.getAsyncTeacherService()
                        .loadTeacherSchool(teacherId)
                        .getUninterruptibly();
                if (school != null) {
//                    if (school.isTraingingSchool()) {
//                        continue;
//                    }


                    teacherInfo.put("schoolName", school.getCname());
                    teacherInfo.put("schoolId", school.getId());
                    teacherInfo.put("schoolType", SchoolType.safeParse(school.getType()).getDescription());

                    ExRegion region = null;
                    if (school.getRegionCode() != null) {
                        region = raikouSystem.loadRegion(school.getRegionCode());
                        teacherInfo.put("regionCode", school.getRegionCode());
                    }
                    if (region != null)
                        teacherInfo.put("regionName", region.toString("/"));
                }

                UserIntegral userIntegralMapper = teacherLoaderClient.loadMainSubTeacherUserIntegral(teacherId, teacher.getKtwelve());
                if (userIntegralMapper != null && userIntegralMapper.getIntegral() != null)
                    teacherInfo.put("integral", userIntegralMapper.getUsable());
                else
                    teacherInfo.put("integral", 0);

                if (teacher.getAuthenticationState() != null)
                    teacherInfo.put("verifiedState", safeParse(teacher.getAuthenticationState()).getDescription());
                else
                    teacherInfo.put("verifiedState", WAITING.getDescription());

                //老师申请认证的时间
                CertificationApplication certificationApplication = certificationServiceClient.getRemoteReference()
                        .findCertificationApplication(teacherId)
                        .getUninterruptibly();
                if (certificationApplication != null) {
                    if (certificationApplication.getCertificationTime() != null)
                        teacherInfo.put("applyDate", FastDateFormat.getInstance("yyyy-MM-dd").format(certificationApplication.getCertificationTime()));
                    else
                        teacherInfo.put("applyDate", FastDateFormat.getInstance("yyyy-MM-dd").format(certificationApplication.getCreateTime()));
                }

                // 查询老师进线日志数目
                teacherInfo.put("customerServiceRecordCount", userLoaderClient.loadUserServiceRecords(teacherId).size());

                teacherList.add(teacherInfo);
            }
        }
        return teacherList;
    }

    private List<Map<String, Object>> getTeacherSnapshotWrapper(List<Map<String, Object>> teacherList) {

        for (Map<String, Object> it : teacherList) {
            // FIXME COMMENT BY ZHAO REX FOR change to query multi?
            List<CertificationApplicationOperatingLog> certificationApplicationOperatingLogList = certificationServiceClient.getRemoteReference()
                    .findCertificationApplicationOperatingLogs((Long) it.get("teacherId"))
                    .getUninterruptibly();

            CertificationApplicationOperatingLog certificationApplicationOperatingLog = null;
            for (CertificationApplicationOperatingLog self : certificationApplicationOperatingLogList) {
                if (!self.getOperatorName().equals("自动认证")) continue;
                certificationApplicationOperatingLog = self;
                break;
            }

            if (certificationApplicationOperatingLog != null)
                it.put("applyDate", certificationApplicationOperatingLog.getCreateTime());
        }

        return teacherList;
    }

    @RequestMapping(value = "bindmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindmobile(@RequestParam Long teacherId, @RequestParam String mobile, @RequestParam String desc) {
        MapMessage message = new MapMessage();

        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("错误的手机号格式,请确认不包含空格或其他字符");
        }

        try {
            Validate.isTrue(crmTeacherService.canBindMobile(teacherId), "该用户不需要绑定手机");
            Validate.isTrue(userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) == null, "手机号已被占用，请重新输入");
            //同步shippingaddress 手机号
            MapMessage msg = userServiceClient.activateUserMobile(teacherId, mobile, true, getCurrentAdminUser().getAdminUserName(), "管理员");
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

    @RequestMapping(value = "/studentandparentbind.vpage", method = RequestMethod.GET)
    public String studentandparentbind(Model model) {
        Long teacherId = getRequestLong("teacherId", -1L);
        if (teacherId > 0) {
            List<Map<String, Object>> records = crmTeacherService.loadStudentAndParentAuthentication(teacherId);
            model.addAttribute("binds", records);
        }
        return "crm/teacher/studentandparentbind";
    }

    /**
     * 查询是否在一个月内改过用户密码
     */
    @AdminAcceptRoles(getRoles = AdminPageRole.ALLOW_ALL)
    @RequestMapping(value = "checkchangedpassword.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage checkChangedPassword() {
        Long teacherId = getRequestLong("teacherId");
        Date startDate = DateUtils.nextDay(new Date(), -30);
        List<UserServiceRecord> recordList = userLoaderClient.loadUserServiceRecords(teacherId);
        UserServiceRecord record = null;
        if (CollectionUtils.isNotEmpty(recordList)) {
            record = recordList.stream().filter(r -> r.getCreateTime().after(startDate))
                    .filter(r -> Objects.equals("重置密码", r.getOperationContent()))
                    .findAny().orElse(null);
        }
        return MapMessage.successMessage().setSuccess(record != null);
    }

    /*
       获取用户特殊属性
     */
    @RequestMapping(value = "getUserActivity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUserActivity() {
        long userId = getRequestLong("userId");
        if (userId == 0)
            return MapMessage.errorMessage("参数错误");
        //黑白名单
        return MapMessage.successMessage().add("bwlist", blackWhiteListManagerClient.getBlackWhiteListManager()
                .findBlackWhiteListsByUserId(userId).getUninterruptibly());
    }

    /*
       修改用户特殊属性
     */
    @RequestMapping(value = "updateUserActivity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateUserActivity() {

        Long userId = getRequestLong("userId");
        String key = getRequestParameter("key", "");
        String checkFlag = getRequestParameter("checkFlag", "");

        if (userId == 0 || StringUtils.isBlank(key) || StringUtils.isBlank(checkFlag)) {
            return MapMessage.errorMessage("参数错误");
        }
        ActivityType activityType = ActivityType.parse(conversionService.convert(key, Integer.class));
        if (activityType == null) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage msg = null;
        //如果是允许班级人数超限 或 试用语文教师,则同步到副账号
        Set<Long> teacherIds = null;
        if (ActivityType.允许班级人数超限.equals(activityType) || ActivityType.试用语文教师.equals(activityType)) {
            teacherIds = teacherLoaderClient.loadRelTeacherIds(userId);
        } else {
            teacherIds = new HashSet<>();
            teacherIds.add(userId);
        }
        for (Long tempteacherId : teacherIds) {
            if (StringUtils.equals("checked", checkFlag)) {
                msg = blackWhiteListManagerClient.getBlackWhiteListManager().createUserBlackWhiteList(tempteacherId, activityType);
            } else {
                msg = blackWhiteListManagerClient.getBlackWhiteListManager().deleteUserBlackWhiteList(tempteacherId, activityType);
            }
        }

        return msg;
    }

    @RequestMapping(value = "teacherrewardorder.vpage", method = RequestMethod.GET)
    public String teacherrewardorder(Model model) {
        Long teacherId;
        try {
            teacherId = SafeConverter.toLong(getRequestParameter("teacherId", ""));
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("用户ID " + getRequestParameter("teacherId", "") + " 不合规范。");
            return redirect("/crm/teacher/teacherlist.vpage");
        }
        try {
            Map<String, String> param = new HashMap<>();
            param.put("uid", teacherId.toString());
            String URL = UrlUtils.buildUrlQuery(ProductConfig.getRewardSiteBaseUrl() + "/open/order_list", param);
            String orderinfo = HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString();
            model.addAttribute("orderinfo", orderinfo);
        } catch (Exception ex) {
            logger.warn("读取用户兑换信息失败", ex);
        }
        return "/crm/teacher/teacherrewardorder";
    }

    @RequestMapping(value = "teachernewrewardorder.vpage", method = RequestMethod.GET)
    public String teacherNewRewardOrder(Model model) {
        Long teacherId;
        try {
            teacherId = SafeConverter.toLong(getRequestParameter("teacherId", ""));
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("用户ID " + getRequestParameter("teacherId", "") + " 不合规范。");
            return redirect("/crm/teacher/teacherlist.vpage");
        }
        try {
            List<RewardOrderMapper> orders = crmRewardService.generateUserRewardOrderMapper(teacherId);
            model.addAttribute("orders", orders);
            model.addAttribute("orderStatus", RewardOrderStatus.values());
        } catch (Exception ex) {
            logger.warn("读取用户兑换信息失败", ex);
        }
        return "/crm/teacher/teachernewrewardorder";
    }

//    @RequestMapping(value = "createclazz.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage createClazz(@RequestParam Long teacherId,
//                                  @RequestParam String classLevel,
//                                  @RequestParam String clazzName,
//                                  @RequestParam Integer classSize,
//                                  @RequestParam Long schoolId,
//                                  @RequestParam String eduSystem,
//                                  @RequestParam String addStudentType) {
//        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//        if (teacher == null) {
//            return MapMessage.errorMessage("参数错误");
//        }
//        MapMessage mapMessage = teacherClazzServiceClient.findClazzWithSameName(teacher, classLevel, new String[]{clazzName});
//        if (!mapMessage.isSuccess()) {
//            return mapMessage;
//        }
//        ClassMapper command = new ClassMapper();
//        command.setClassLevel(classLevel);
//        command.setClazzName(clazzName);
//        command.setClassSize(classSize);
//        command.setSchoolId(schoolId);
//        command.setEduSystem(eduSystem);
//        command.setAddStudentType(addStudentType);
//
//        MapMessage message = clazzServiceClient.createPublicClazzs(teacher, Collections.singleton(command));
//        if (!message.isSuccess()) {
//            return message;
//        }
//
//        Collection neonatals = (Collection) message.get("neonatals");
//        NeonatalClazz neonatal = (NeonatalClazz) MiscUtils.firstElement(neonatals);
//        if (neonatal == null || !neonatal.isSuccessful()) {
//            return MapMessage.errorMessage("创建班级失败");
//        }
//
//        // 初始化创建的新班级所需要的课本
//        ExRegion region = userLoaderClient.loadUserRegion(teacher);
//        Long bookId = contentLoaderClient.getExtension().initializeClazzBook(
//                teacher.getSubject(),
//                ConversionUtils.toInt(command.getClassLevel()),
//                region.getCode(),
//                regionLoaderClient);
//        if (bookId != null) {
//            ChangeBookMapper cmb = new ChangeBookMapper();
//            cmb.setBooks(String.valueOf(bookId));
//            cmb.setClazzs(String.valueOf(neonatal.getClazzId()));
//            cmb.setType(0); // 0表示添加/修改课本
//            try {
//                contentServiceClient.setClazzBook(teacher, cmb);
//            } catch (Exception ignored) {
//                logger.warn("Failed to set book for neonatal clazz {}", neonatal.getClazzId(), ignored);
//            }
//        }
//
//        return MapMessage.successMessage("新建班级成功!")
//                .add("clazzId", neonatal.getClazzId())
//                .add("students", neonatal.getExtensionAttributes().get("studentList"));
//    }

    @RequestMapping(value = "teacheractivehistory.vpage", method = RequestMethod.GET)
    public String teacherActivateHistory(Model model) {
        Long teacherId;
        try {
            teacherId = SafeConverter.toLong(getRequestParameter("teacherId", ""));
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("用户ID " + getRequestParameter("teacherId", "") + " 不合规范。");
            return redirect("/crm/teacher/teacherlist.vpage");
        }
        List<Map<String, Object>> datas = new LinkedList<>();
        try {
            List<TeacherActivateTeacherHistory> histories = businessManagementClient
                    .findTeacherActivateTeacherHistoryMapByInviterIds(Collections.singleton(teacherId))
                    .values()
                    .stream()
                    .flatMap(List::stream)
                    .sorted((o1, o2) -> {
                        long c1 = SafeConverter.toLong(o1.getCreateTime());
                        long c2 = SafeConverter.toLong(o2.getCreateTime());
                        return Long.compare(c2, c1);
                    })
                    .collect(Collectors.toList());
            for (TeacherActivateTeacherHistory history : histories) {
                Map<String, Object> map = new HashMap<>();
                map.put("inviteeId", history.getInviteeId());
                User user = userLoaderClient.loadUser(history.getInviteeId());
                if (user != null)
                    map.put("realname", user.getProfile().getRealname());

                map.put("over", SafeConverter.toBoolean(history.getOver()));
                map.put("activationType", history.getActivationType());
                map.put("createTime", history.getCreateTime());
                datas.add(map);
            }
        } catch (Exception ex) {
            logger.warn("读取用户兑换信息失败", ex);
        }
        model.addAttribute("teacher", userLoaderClient.loadUser(teacherId));
        model.addAttribute("datas", datas);
        return "/crm/teacher/teacheractivatehistory";
    }

    @RequestMapping(value = "disablenewhomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage disableNewHomework(@RequestParam Long teacherId, @RequestParam String homeworkId) {
        if (teacherId == null || homeworkId == null) return MapMessage.errorMessage("参数错误");

        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) return MapMessage.errorMessage("参数错误");

        return newHomeworkServiceClient.deleteHomework(teacherId, homeworkId);
    }

    @RequestMapping(value = "batchdeletehomework.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage batchDeleteHomework() {
        Long teacherId = getRequestLong("teacherId");
        if (teacherId <= 0) {
            return MapMessage.errorMessage("teacherId参数错误");
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage("教师不存在");
        }
        Integer homeworkDay = getRequestInt("homeworkDay", 30);//默认是30
        PageRequest pageable = new PageRequest(0, 1000);//这个1000其实没什么分页效果，只是用于一次取出全部数据
        Map<Long, GroupTeacherMapper> groups = deprecatedGroupLoaderClient.loadTeacherGroups(teacher.getId(), true)
                .stream().collect(Collectors.toMap(GroupTeacherMapper::getId, Function.identity()));
        // 获取作业
        Page<NewHomework.Location> page = newHomeworkCrmLoaderClient.loadGroupNewHomeworks(groups.keySet(),
                DateUtils.nextDay(new Date(), -1 * homeworkDay), new Date(), pageable, true);
        List<String> successHomeworkId = new LinkedList<>();
        List<String> failedHomeworkId = new LinkedList<>();
        if (page != null) {
            if (page.getContent() != null) {
                for (NewHomework.Location location : page.getContent()) {
                    if (!SafeConverter.toBoolean(location.isDisabled())) {
                        MapMessage mapMessage = newHomeworkServiceClient.deleteHomework(teacher.getId(), location.getId());
                        if (mapMessage.isSuccess()) {
                            successHomeworkId.add(location.getId());
                            LogCollector.info("backend-general", MapUtils.map(
                                    "env", RuntimeMode.getCurrentStage(),
                                    "usertoken", teacherId,
                                    "mod1", location.getId(),
                                    "op", "success crm bacth delete homework"
                            ));
                        } else {
                            failedHomeworkId.add(location.getId());
                            LogCollector.info("backend-general", MapUtils.map(
                                    "env", RuntimeMode.getCurrentStage(),
                                    "usertoken", teacherId,
                                    "mod1", location.getId(),
                                    "op", "failed crm bacth delete homework"
                            ));
                        }
                    }
                }
            }
        }
        return MapMessage.successMessage()
                .add("successHomeworkId", successHomeworkId)
                .add("failedHomeworkId", failedHomeworkId);
    }


    @RequestMapping(value = "disablehomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage disableHomework(@RequestParam Long teacherId,
                                      @RequestParam String homeworkId) {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "teacherquizdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String getTeacherQuizDetail(Model model) {

        return redirect("/crm/teacher/teacherlist.vpage");
    }

    @RequestMapping(value = "addIntegral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addIntegral(@RequestParam("cheateId") String cheatId) {
        try {
            PossibleCheatingHomework cheat = crmTeacherService.loadPossibleCheatingHomework(cheatId);
            if (cheat == null) return MapMessage.errorMessage();

            IntegralType tit;
            String tc;
            switch (cheat.getHomeworkType()) {
                case ENGLISH:
                    tit = IntegralType.每个学生完成作业老师获得积分;
                    tc = "作业";
                    break;
                case MATH:
                    tit = IntegralType.每个学生完成作业老师获得积分;
                    tc = "作业";
                    break;
                default:
                    return MapMessage.errorMessage();
            }

            List<Map<Long, Object>> studentIntegral = cheat.getStudentIntegral();
            Map<Long, Object> teacherIntegral = cheat.getTeacherIntegral();
            if (studentIntegral == null || teacherIntegral == null) {
                return MapMessage.errorMessage();
            }

            // 加老师
            Set<Map.Entry<Long, Object>> teacherEntry = teacherIntegral.entrySet();
            Integer integralCount = 0;
            for (Map.Entry<Long, Object> data : teacherEntry) {
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(cheat.getClazzId());
                IntegralHistory integralHistory = new IntegralHistory(data.getKey(), tit, (Integer) data.getValue());
                integralCount += (Integer) data.getValue();
                integralHistory.setComment(StringUtils.formatMessage("共{}个{}的学生完成您布置的{}，您获得园丁豆（补加）", studentIntegral.size(), clazz.formalizeClazzName(), tc));
                integralHistory.setHomeworkUniqueKey(cheat.getHomeworkType().name(), cheat.getHomeworkId());
                GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(data.getKey(), clazz.getId(), false);
                integralHistory.setRelationClassId(group == null ? null : group.getId());

                MapMessage msg = userIntegralService.changeIntegral(integralHistory);
                if (msg.isSuccess()) {
                    logger.debug("Add integral:");
                    logger.debug("  integral      -> {}", data.getValue());
                    logger.debug("  integral type -> {}", tit);
                    logger.debug("  user id       -> {}", cheat.getTeacherId());
                    logger.debug("  homework id   -> {}", cheat.getHomeworkId());
                } else {
                    logger.warn("给老师{}加园丁豆失败", cheat.getTeacherId());
                }
            }
            //更新已加金币标示
            crmTeacherService.updatePossibleCheatingHomeworkIntegral(cheat.getId());

            //记录进线日志
            String operation = StringUtils.formatMessage("管理员补加园丁豆，作业类型：{}，作业ID{}，园丁豆{}。",
                    cheat.getHomeworkType().name(), cheat.getHomeworkId(), integralCount);

            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(cheat.getTeacherId());
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.积分修改.name());
            userServiceRecord.setOperationContent(operation);
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            return MapMessage.successMessage();
        } catch (Exception ignored) {
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "sendwxnotice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendWxNotice(@RequestParam Long teacherId,
                                   @RequestParam String content) {
        if (teacherId == null || StringUtils.isEmpty(content)) {
            return MapMessage.errorMessage("参数错误");
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage("参数错误");
        }
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("content", content);
        wechatServiceClient.processWechatNotice(
                WechatNoticeProcessorType.TeacherCrmMessageNotice,
                teacherId,
                extensions,
                WechatType.TEACHER
        );

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户联络.name());
        userServiceRecord.setOperationContent(StringUtils.formatMessage("管理员发送微信消息，内容：{}", content));
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        return MapMessage.successMessage().setInfo("已发送");
    }

    @RequestMapping(value = "teacherjoinclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherJoinClazz() {
        long teacherId = getRequestLong("teacherId");
        if (teacherId == 0) {
            return MapMessage.errorMessage("老师ID不正确");
        }

        int clazzLevel = getRequestInt("clazzLevel");
        if (clazzLevel == 0) {
            return MapMessage.errorMessage("年级不正确");
        }

        String clazzName = getRequestString("clazzName");
        if (StringUtils.isBlank(clazzName)) {
            return MapMessage.errorMessage("班级名称不正确");
        }

        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage("老师不存在");
        }

        Long schoolId = teacher.getTeacherSchoolId();
        if (schoolId == null) {
            return MapMessage.errorMessage("找不到老师所在学校");
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .enabled()
                .clazzLevel(ClazzLevel.parse(clazzLevel))
                .toList()
                .stream()
                .filter(t -> StringUtils.equals(t.getClassName(), clazzName))
                .filter(Clazz::isSystemClazz)// 必须是系统班级
                .filter(t -> !t.isDisabledTrue())// 必须是未删除班级
                .findFirst()
                .orElse(null);
        if (clazz == null) {
            return MapMessage.errorMessage("找不到该班级，请确认班级是否已被删除或非新体系下班级");
        }

        return clazzServiceClient.teacherJoinSystemClazzForce(teacherId, clazz.getId());
    }

    @RequestMapping(value = "bigdataauth.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> bigDataAuth(@RequestParam("teacherId") Long teacherId) {
        Map<String, Object> message = new HashMap<>();
        message.put("success", false);
        String date = DateUtils.dateToString(DayRange.current().previous().getStartDate(), "yyyy-MM-dd");

        try {
            boolean teacherAuthStatus = userAuthQueryServiceClient.getTeacherAuthStatus(teacherId, date);
            message.put("success", teacherAuthStatus);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return message;
    }

}
