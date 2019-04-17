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

import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Range;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import com.voxlearning.utopia.admin.entity.CrmSchoolEvaluate;
import com.voxlearning.utopia.admin.service.crm.CrmSchoolEvaluateService;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.data.CertificationCondition;
import com.voxlearning.utopia.entity.TeacherRoles;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.crm.constants.SchoolOperationType;
import com.voxlearning.utopia.entity.crm.schoolrecord.SchoolServiceRecord;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorCompetition;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorLevelDetail;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.clazz.client.NewClazzServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrgLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.CrmSimilarSchoolServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmSchoolExtInfoCheckServiceClient;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.privilege.client.BlackWhiteListManagerClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.region.buffer.ExRegionBuffer;
import com.voxlearning.utopia.service.school.client.*;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.constants.*;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.client.TeacherRolesServiceClient;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SpecialTeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SpecialTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.ResearchStaffUserServiceClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.utopia.api.constant.Subjects.ALL_SUBJECTS;
import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;

@Controller
@RequestMapping("/crm/school")
public class CrmSchoolController extends CrmAbstractController {

    private static final int MAX_SCHOOL_AMOUNT = 20;
    private PinYinComparator pinYinComparator = new PinYinComparator();
    //查询 老师ID——老师所在班级数量
    private static final String teacherClassNumSql = "SELECT teacher.USER_ID AS uid, count(g.CLAZZ_GROUP_ID) AS ct "
            + " FROM VOX_USER_SCHOOL_REF teacher LEFT JOIN VOX_GROUP_TEACHER_REF g ON teacher.USER_ID = g.TEACHER_ID"
            + " WHERE school_id=? AND g.STATUS='VALID' AND teacher.DISABLED=0 AND g.DISABLED=0 GROUP BY uid ORDER BY ct";
    private static final String otherTeacherClassNum = "SELECT r.TEACHER_ID AS uid,COUNT(DISTINCT v2.CLAZZ_ID) AS ct FROM " +
            " VOX_CLAZZ_GROUP v1 INNER JOIN VOX_CLAZZ_GROUP v2 ON v1.CLAZZ_ID = v2.CLAZZ_ID" +
            " JOIN VOX_GROUP_TEACHER_REF r ON v1.ID=r.CLAZZ_GROUP_ID" +
            " WHERE v1.CLAZZ_ID IN(SELECT ID FROM VOX_CLASS WHERE SCHOOL_ID = ? AND DISABLED=0)" +
            " AND r.`STATUS`='VALID' AND v1.DISABLED = 0 AND v2.DISABLED = 0 AND r.DISABLED=0 AND v1.ID <> v2.ID" +
            " GROUP BY r.TEACHER_ID";

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private AgentOrgLoaderClient agentOrgLoaderClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private BlackWhiteListManagerClient blackWhiteListManagerClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private CrmSchoolEvaluateService crmSchoolEvaluateService;
    @Inject private CrmSimilarSchoolServiceClient crmSimilarSchoolServiceClient;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private GlobalTagServiceClient globalTagServiceClient;
    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SchoolServiceClient schoolServiceClient;
    @Inject private SpecialTeacherLoaderClient specialTeacherLoaderClient;
    @Inject private SpecialTeacherServiceClient specialTeacherServiceClient;
    @Inject private UserActivityServiceClient userActivityServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private CrmSchoolExtInfoCheckServiceClient crmSchoolExtInfoCheckServiceClient;
    @Inject private SchoolServiceRecordLoaderClient schoolServiceRecordLoader;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private NewClazzServiceClient newClazzServiceClient;
    @Inject private SchoolServiceRecordServiceClient schoolServiceRecordServiceClient;
    @Inject private TeacherRolesServiceClient teacherRolesServiceClient;
    @Inject private UserLoaderClient userLoader;
    @Inject private ResearchStaffUserServiceClient researchStaffUserServiceClient;

    private final Comparator<Map<String, Object>> clazzInfoSort = (o1, o2) -> {
        final Integer o1_ClassLevel = Integer.valueOf((String) o1.get("classLevel"));
        final Integer o2_ClassLevel = Integer.valueOf((String) o2.get("classLevel"));
        if (o1_ClassLevel.equals(o2_ClassLevel)) {
            return Long.compare((Long) o1.get("id"), (Long) o2.get("id"));
        } else {
            return Integer.compare(o1_ClassLevel, o2_ClassLevel);
        }
    };

    /**
     * *********************** 学校评分相关   ********************************************************
     */
    @RequestMapping(value = "add_school_evaluate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addSchoolEvaluate() {
        Long schoolId = getRequestLong("schoolId");
        Integer placeScore = getRequestInt("placeScore");
        Integer teachScore = getRequestInt("teachScore");
        Integer studentScore = getRequestInt("studentScore");
        Integer commercializeScore = getRequestInt("commercializeScore");
        String remark = getRequestString("remark");
        AuthCurrentAdminUser user = getCurrentAdminUser();
        return crmSchoolEvaluateService.addSchoolEvaluate(schoolId, placeScore, teachScore, studentScore,
                commercializeScore, remark, user.getAdminUserName(), user.getRealName());
    }

    @RequestMapping(value = "find_school_evaluate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findSchoolEvaluate() {
        Long schoolId = getRequestLong("schoolId");
        List<CrmSchoolEvaluate> schoolEvaluates = crmSchoolEvaluateService.loadCrmSchoolEvaluateBySchoolId(schoolId);
        return MapMessage.successMessage().add("data", createEvaluateInfo(schoolEvaluates));
    }

    private List<Map<String, String>> createEvaluateInfo(List<CrmSchoolEvaluate> schoolEvaluates) {
        if (CollectionUtils.isEmpty(schoolEvaluates)) {
            return Collections.emptyList();
        }
        List<Map<String, String>> result = new ArrayList<>();
        schoolEvaluates.forEach(p -> {
            Map<String, String> evaluateInfo = new HashMap<>();
            evaluateInfo.put("date", DateUtils.dateToString(p.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
            evaluateInfo.put("accountName", p.getAccountName());
            evaluateInfo.put("placeScore", SafeConverter.toString(p.getPlaceScore()));
            evaluateInfo.put("teachScore", SafeConverter.toString(p.getTeachScore()));
            evaluateInfo.put("studentScore", SafeConverter.toString(p.getStudentScore()));
            evaluateInfo.put("commercializeScore", SafeConverter.toString(p.getCommercializeScore()));
            evaluateInfo.put("remark", p.getRemark());
            result.add(evaluateInfo);
        });
        return result;
    }

    /**
     * *********************** 解锁学校信息收集********************************************************
     */
    @RequestMapping(value = "un_locked_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage unLockedSchool() {
        Long schoolId = getRequestLong("schoolId");

        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo == null) {
            return MapMessage.errorMessage("所选学校的扩展信息不存在");
        }
        schoolExtInfo.setLocked(false);
        schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .upsertSchoolExtInfo(schoolExtInfo)
                .getUninterruptibly();
        if (schoolExtInfo == null) {
            return MapMessage.errorMessage("学校解锁失败");
        } else {
            return MapMessage.successMessage("学校解锁成功");
        }
    }

    @RequestMapping(value = "setscannumber.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setSchoolScanNumber() {
        Long schoolId = getRequestLong("schoolId");
        MapMessage checkMsg = checkKlxPrivilege(schoolId);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }

        int digit = getRequestInt("digit");

        if (!Range.between(5, 11).contains(digit)) {
            return MapMessage.errorMessage("暂时只能支持5~11位的填涂号");
        }

//        if (digit != 5) {
//            return MapMessage.errorMessage("暂时只能支持5位的填涂号");
//        }

        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();

        String key = "crm_school_scan_len_privilege";
        String allowedAccount = "";
        try {
            allowedAccount = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), key);
        } catch (Exception e) {
            logger.error("填涂号长度编辑特权通用配置设置有误crm_school_scan_len_privilege");
        }
        String adminUserName = getCurrentAdminUser().getAdminUserName();
        if (Stream.of(allowedAccount.split(",")).noneMatch(account -> StringUtils.equals(account, adminUserName))) {
            if (schoolExtInfo != null && schoolExtInfo.getScanNumberDigit() != null) {
                return MapMessage.errorMessage("填涂号位数已指定，暂不支持填涂号位数修改");
            }
        }


        if (schoolExtInfo == null) {
            schoolExtInfo = new SchoolExtInfo();
            schoolExtInfo.setId(schoolId);
        }
        schoolExtInfo.setScanNumberDigit(digit);
        schoolExtInfo.setLocked(false);
        schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .upsertSchoolExtInfo(schoolExtInfo)
                .getUninterruptibly();
        if (schoolExtInfo == null) {
            return MapMessage.errorMessage("设置填涂号失败");
        }
        return MapMessage.successMessage("设置填涂号成功");
    }

    /**
     * ***********************查询学校*****************************************************************
     */

    @RequestMapping(value = "schoollist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String schoolList(Model model) {
        Map<String, String> conditionMap = new HashMap<>();
        try {
            conditionMap.put("provinces", getRequestParameter("provinces", "-1"));
            conditionMap.put("citys", getRequestParameter("citys", "-1"));
            conditionMap.put("countys", getRequestParameter("countys", "-1"));
            conditionMap.put("schoolId", getRequestParameter("schoolId", "").replaceAll("\\s", ""));
            conditionMap.put("schoolName", getRequestParameter("schoolName", "").replaceAll("\\s", ""));
            conditionMap.put("shortName", getRequestParameter("shortName", "").replaceAll("\\s", ""));
        } catch (Exception ignored) {
        }

        List<Map<String, Object>> schoolSnapShotList = getSchoolSnapShot(conditionMap);

        if (CollectionUtils.isEmpty(schoolSnapShotList)) {
            if (isRequestPost()) {
                getAlertMessageManager().addMessageError("学校不存在。");
            }
        } else if (schoolSnapShotList.size() == MAX_SCHOOL_AMOUNT) {
            getAlertMessageManager().addMessageError("若结果中未找到正确学校，请尝试缩小查找范围。");
        }
        model.addAttribute("provinces", getAllProvincePinYin());
        model.addAttribute("conditionMap", conditionMap);
        model.addAttribute("schoolSnapShotList", schoolSnapShotList);

        return "crm/school/schoollist";
    }

    // fixme : 这个应该使用UserController里的对应方法
    @AdminAcceptRoles(postRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "regionlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> regionList(@RequestParam Integer regionCode) {
        Map<String, Object> message = new HashMap<>();
        if (regionCode == null) {
            return message;
        }
        //返回时增加“全部”选项，小于0的regionCode值都为无效值
        Region regionAll = new Region();
        regionAll.setName("全部");
        regionAll.setCode(-1);

        List<Region> regionList = new ArrayList<>();
        if (regionCode >= 0) {
            regionList.addAll(raikouSystem.getRegionBuffer().loadChildRegions(regionCode));
        }

        Set<Region> rs = new TreeSet<Region>(pinYinComparator);
        rs.addAll(regionList);
        rs.add(regionAll);
        message.put("regionList", rs);
        return message;
    }

    @RequestMapping(value = "schoolhomepage.vpage", method = RequestMethod.GET)
    @SuppressWarnings("unchecked")
    public String schoolHomepage(Model model) {
        Long schoolId = getRequestLong("schoolId");
        model.addAttribute("clazzLevels", ClazzLevel.toKeyValuePairs());
        if (schoolId == 0L) {
            getAlertMessageManager().addMessageError("学校ID: " + getRequestString("schoolId") + " 不合规范。");
            return "crm/index";
        }

        Map<String, Object> schoolInfoAdminMapper = getSchoolInfoMap(schoolId);
        if (MapUtils.isEmpty(schoolInfoAdminMapper)) {
            getAlertMessageManager().addMessageError("学校(ID:" + schoolId + ")不存在。");
            return "crm/index";
        }

        //判断是不是重点校
        model.addAttribute("isDictSchool", agentOrgLoaderClient.isDictSchool(schoolId));

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
        ALL_SUBJECTS.forEach(subject ->
                subjects.add(MapUtils.m(
                        "name", subject.getValue(),
                        "value", subject.name(),
                        "checked", selectedSubjects.contains(subject),
                        "disabled", SchoolExtInfo.DefaultSubjects.contains(subject)
                ))
        );
        schoolKlxPrivilegeInfo.put("subjects", subjects);

        model.addAttribute("schoolKlxPrivilegeInfo", schoolKlxPrivilegeInfo);
        model.addAttribute("schoolInfoAdminMapper", schoolInfoAdminMapper);
        model.addAttribute("schoolExtInfo", schoolExtInfo);
        model.addAttribute("provinces", getAllProvince());
        model.addAttribute("isCjlSchool", isCJLSchool(schoolId));
        model.addAttribute("isSeiueSchool", isSeiueSchool(schoolId));

        List<SchoolServiceRecord> records = schoolServiceRecordLoader.loadRecordsUnderSchool(schoolId);
        model.addAttribute("records", records);

        // 是否戴特合作校
        boolean daiteFlag = false;
        if (schoolExtInfo != null && schoolExtInfo.fetchDaiteSchoolFlag()) {
            daiteFlag = true;
        }
        model.addAttribute("daiteSchool", daiteFlag);
        model.addAttribute("adjustClazz", schoolExtInfo != null && schoolExtInfo.fetchAdjustClazz());

        return "crm/school/schoolhomepage";
    }

    /**
     * 打散换班
     */
    @RequestMapping(value = "breakchangeclass.vpage", method = RequestMethod.GET)
    public String changeClassPage(Model model) {
        Long schoolId = getRequestLong("schoolId");
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            model.addAttribute("error", "无效的学校ID：" + schoolId);
        } else {
            model.addAttribute("school", school);
            model.addAttribute("schoolId", getRequestString("schoolId"));
        }
        model.addAttribute("isSeiueSchool", isSeiueSchool(schoolId));
        return "crm/school/breakchangeclass";
    }

    /**
     * 关联教学班
     */
    @RequestMapping(value = "linkclasses.vpage", method = RequestMethod.GET)
    public String linkClassPage(Model model) {
        Long schoolId = getRequestLong("schoolId");
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            model.addAttribute("error", "无效的学校ID：" + schoolId);
        } else if (school.isPrimarySchool() || school.isInfantSchool()) {
            model.addAttribute("error", "此功能只针对初高中使用");
        } else {
            model.addAttribute("school", school);
            model.addAttribute("schoolId", getRequestString("schoolId"));
        }
        model.addAttribute("isSeiueSchool", isSeiueSchool(schoolId));
        return "crm/school/linkclasses";
    }

    // 根据经纬度获取学校的地址
    @RequestMapping(value = "loadschooladdress.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadSchoolAddress(Model model) {
        Long schoolId = getRequestLong("schoolId");
        String latitude = getRequestString("latitude");
        String longitude = getRequestString("longitude");

        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();

        if (StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude)) {
            return MapMessage.successMessage().add("address", "获取位置失败");
        }

        MapMessage message = AmapMapApi.getAddress(latitude, longitude, "autonavi");
        if (message.isSuccess()) {

            if (schoolExtInfo == null) {
                schoolExtInfo = new SchoolExtInfo();
                schoolExtInfo.setId(schoolId);
            }
            schoolExtInfo.setLatitude(latitude);
            schoolExtInfo.setLongitude(longitude);
            schoolExtInfo.setCoordinateType("wgs84ll");
            schoolExtInfo.setAddress(message.get("address").toString());
            schoolExtServiceClient.getSchoolExtService()
                    .upsertSchoolExtInfo(schoolExtInfo)
                    .getUninterruptibly();

            return message;
        } else {
            return MapMessage.successMessage().add("address", "获取位置失败");
        }
    }

    /**
     * 获取拒收奖品的老师名单
     */
    @RequestMapping(value = "getrejectreceivegiftlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getRejectReceiveGiftList() {

        Long schoolId = getRequestLong("schoolId", 0L);
        if (schoolId == 0L) {
            return MapMessage.errorMessage("非法的学校ID");
        }

        List<String> rejectList = teacherLoaderClient.loadSchoolTeachers(schoolId)
                .stream()
                .filter(t -> {
                    List<BlackWhiteList> bwList = blackWhiteListManagerClient
                            .loadUserBlackWhiteLists(t.getId(), ActivityType.拒收学生奖品名单);
                    return bwList.size() > 0;
                })
                .map(Teacher::fetchRealname)
                .collect(Collectors.toList());

        return MapMessage.successMessage().add("list", rejectList);
    }

    /**
     * ***********************下载相关************************************************************
     */

    @RequestMapping(value = "downloadcountyschool.vpage", method = RequestMethod.GET)
    public void downloadcountyschool(HttpServletResponse response) {

        int countyCode = getRequestInt("countyCode");

        List<School> schoolList = raikouSystem.querySchoolLocations(countyCode)
                .enabled()
                .filter(e -> e.match(AuthenticationState.SUCCESS) || e.match(AuthenticationState.WAITING))
                .transform()
                .asList()
                .stream()
                .sorted(Comparator.comparing(School::getId))
                .collect(Collectors.toList());

        HSSFWorkbook hssfWorkbook = convertToHSSfWorkbook(schoolList);

        ExRegion county = raikouSystem.loadRegion(countyCode);
        String filename = county == null ? "_" : county.toString("-") + ".xls";

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            hssfWorkbook.write(outStream);
            outStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("", e);
        } finally {
            IOUtils.closeQuietly(outStream);
        }

        try {
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException ignored) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                throw new RuntimeException("", e);
            }
        }
    }

    @RequestMapping(value = "downloadschoolteachers.vpage", method = RequestMethod.GET)
    public void downloadSchoolTeachers(@RequestParam Long schoolId, HttpServletResponse response)
            throws IOException {

        School school = raikouSystem.loadSchool(schoolId);
        if (school != null && school.getSchoolAuthenticationState() != AuthenticationState.SUCCESS) {
            school = null;
        }
        if (school == null) {
            response.setCharacterEncoding("utf-16");
            response.getWriter().write("学校ID对应学校不存在");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<Teacher> teacherList = teacherLoaderClient.loadSchoolTeachers(schoolId);
        HSSFWorkbook hssfWorkbook = convertToHSSfWorkbookForSchoolTeachers(school.getCname(),
                raikouSystem.loadRegion(school.getRegionCode()), teacherList);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            hssfWorkbook.write(outStream);
            outStream.flush();
        } finally {
            IOUtils.closeQuietly(outStream);
        }

        String filename = school.getCname() + ".xls";

        try {
            HttpRequestContextUtils.currentRequestContext()
                    .downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (IOException ignored) {
            response.getWriter().write("不能下载");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * ***********************增加学校*****************************************************************
     */

    @RequestMapping(value = "addschool.vpage", method = RequestMethod.GET)
    public String addSchoolIndex(Model model) {

        model.addAttribute("provinces", getAllProvince());

        return "crm/school/addschool";
    }

    @RequestMapping(value = "addschool.vpage", method = RequestMethod.POST)
    @SuppressWarnings("unchecked")
    public String addSchool(Model model) {
        String sessionKey = getRequestString("sessionKey");
        Map<String, String> conditionMap = new HashMap<>();
        List<String> schoolNameList = new ArrayList<>();
        School school = new School();
        String schoolDesc;
        if (StringUtils.isNotBlank(sessionKey)) {
            //此时是查询es里的相似学校并返回
            Map<String, Object> sessionMap = adminCacheSystem.CBS.flushable.load(sessionKey);
            conditionMap = (Map) (sessionMap.get("conditionMap"));
            schoolNameList = (List) (sessionMap.get("schoolNameList"));
            school = (School) (sessionMap.get("school"));
            schoolDesc = ConversionUtils.toString(sessionMap.get("schoolDesc"));
            model.addAttribute("provinces", getAllProvince());
            model.addAttribute("conditionMap", conditionMap);
        } else {
            String[] conditionKeys = new String[]{"cmainname", "schooldistrict", "cname", "shortname", "cname_list", "type", "authenticationState", "vip", "provinces", "citys", "countys", "schoolDesc", "level", "eduSystem"};
            for (String key : conditionKeys) {
                conditionMap.put(key, getRequestParameter(key, "").replaceAll("\\s", ""));
            }
            schoolDesc = String.valueOf(conditionMap.get("schoolDesc"));
            model.addAttribute("provinces", getAllProvince());
            model.addAttribute("conditionMap", conditionMap);
            try {
                school.setLevel(Integer.parseInt(String.valueOf(conditionMap.get("level"))));
                school.setType(Integer.parseInt(String.valueOf(conditionMap.get("type"))));
                school.setAuthenticationState(Integer.parseInt(String.valueOf(conditionMap.get("authenticationState"))));
                school.setVip(Integer.parseInt(String.valueOf(conditionMap.get("vip"))));
                school.setRegionCode(Integer.parseInt(String.valueOf(conditionMap.get("countys"))));
                school.setCode(school.getRegionCode().toString());
                school.setCmainName(conditionMap.get("cmainname"));
                school.setSchoolDistrict(conditionMap.get("schooldistrict"));
            } catch (Exception ignored) {
                getAlertMessageManager().addMessageError("增加学校失败，请正确设置各参数，并记录日志。");
                return "crm/school/addschool";
            }

            if (StringUtils.isBlank(schoolDesc) || school.getRegionCode() < 0) {
                getAlertMessageManager().addMessageError("增加学校失败，请正确设置各参数，并记录日志。");
                return "crm/school/addschool";
            }
            //批量增加学校和单独增加学校不能同时使用，防止用户出错
            if (StringUtils.isNotBlank(conditionMap.get("cname_list")) && StringUtils.isNotBlank(conditionMap.get("cname"))) {
                getAlertMessageManager().addMessageError("批量增加学校和单独增加学校不能同时使用");
                return "crm/school/addschool";
            }

            EduSystemType eduSystem = EduSystemType.of(conditionMap.get("eduSystem"));
            if (eduSystem == null) {
                getAlertMessageManager().addMessageError("无效的学校学制");
                return "crm/school/addschool";
            }

            if (school.isPrimarySchool()) {
                if (!Arrays.asList(EduSystemType.P5, EduSystemType.P6).contains(eduSystem)) {
                    getAlertMessageManager().addMessageError("学校级别和学校学制不配，请检查参数");
                    return "crm/school/addschool";
                }
            } else if (school.isMiddleSchool()) {
                if (!Arrays.asList(EduSystemType.J3, EduSystemType.J4).contains(eduSystem)) {
                    getAlertMessageManager().addMessageError("学校级别和学校学制不配，请检查参数");
                    return "crm/school/addschool";
                }
            } else if (school.isSeniorSchool()) {
                if (!Arrays.asList(EduSystemType.S3, EduSystemType.S4).contains(eduSystem)) {
                    getAlertMessageManager().addMessageError("学校级别和学校学制不配，请检查参数");
                    return "crm/school/addschool";
                }
            } else if (school.isInfantSchool()) {
                if (!Arrays.asList(EduSystemType.I4).contains(eduSystem)) {
                    getAlertMessageManager().addMessageError("学校级别和学校学制不配，请检查参数");
                    return "crm/school/addschool";
                }
            }

            if (StringUtils.isNotBlank(conditionMap.get("cname_list"))) {
                schoolNameList = Arrays.asList(String.valueOf(conditionMap.get("cname_list")).split("[,，]"));
                schoolNameList = new ArrayList(new HashSet(schoolNameList));
                schoolNameList.removeAll(Collections.singletonList(""));
            } else if (StringUtils.isNotBlank(conditionMap.get("cmainname")) || StringUtils.isNotBlank(conditionMap.get("cname"))) {
                //当学校校区信息为空的时候，学校全称=主干名；当学校校区名字段有信息时，学校全称=主干名（绿地校区）
                //为了保证兼容,如果学校主干名不存在,则跳过学校全称的拼接,使用cname字段
                if (StringUtils.isNotBlank(conditionMap.get("cmainname"))) {
                    if (StringUtils.isNotBlank(conditionMap.get("schooldistrict"))) {
                        schoolNameList.add(conditionMap.get("cmainname") + "(" + conditionMap.get("schooldistrict") + ")");
                    } else {
                        schoolNameList.add(conditionMap.get("cmainname"));
                    }
                } else {
                    schoolNameList.add(String.valueOf(conditionMap.get("cname")));
                }
                if (StringUtils.isBlank(conditionMap.get("shortname"))) {
                    getAlertMessageManager().addMessageError("增加学校失败，学校简称不能为空");
                    return "crm/school/addschool";
                }
            } else {
                getAlertMessageManager().addMessageError("增加学校失败，学校名字不能为空");
                return "crm/school/addschool";
            }
            //存入session备用
            Map<String, Object> sessionMap = new HashMap<>();
            sessionMap.put("conditionMap", conditionMap);
            sessionMap.put("schoolNameList", schoolNameList);
            sessionMap.put("school", school);
            sessionMap.put("schoolDesc", schoolDesc);
            String key = getCurrentAdminUser().getAdminUserName() + ConversionUtils.toString(new Date().getTime());
            adminCacheSystem.CBS.flushable.set(key, DateUtils.getCurrentToDayEndSecond(), sessionMap);

            //返回页面key==确认要增加学校的时候用
            model.addAttribute("sessionKey", key);


            //查询同名学校
            String message;
            for (String schoolName : schoolNameList) {
                school.setCname(schoolName);
                List<Long> schoolIdList = getSchoolIdIfExist(school);

                //判断同一地区是否有同名学校
                if (CollectionUtils.isNotEmpty(schoolIdList)) {
                    String ids = StringUtils.join(schoolIdList, ",");
                    message = "增加学校失败，该地区已有与" + schoolName + "同名的学校ID:" + ids + "注册";
                    getAlertMessageManager().addMessageError(message);
                    model.addAttribute("message", message);
                    return "crm/school/addschool";
                }
            }

            // 查询同名类似学校
            Map<String, String> existSchoolMap = getSchoolSimilarityInfo(school.getCmainName(), SchoolLevel.safeParse(school.getLevel()), school.getRegionCode());

            if (MapUtils.isNotEmpty(existSchoolMap)) {
                model.addAttribute("existSchoolMap", existSchoolMap);
                return "crm/school/addschool";
            }


            //从ES查类似学校
//            for (String schoolName : schoolNameList) {
//                school.setCname(ConversionUtils.toString(conditionMap.get("shortname")).replaceAll("小学", ""));
//                Map<String, String> existSchoolMap = getSchoolIdAndNameInES(school);
//                if (existSchoolMap.size() > 0) {
//                    model.addAttribute("existSchoolMap", existSchoolMap);
//                    return "crm/school/addschool";
//                }
//            }
        }

        //查询同名学校
        String message;
        for (String schoolName : schoolNameList) {
            school.setCname(schoolName);
            List<Long> schoolIdList = getSchoolIdIfExist(school);

            //判断同一地区是否有同名学校
            if (CollectionUtils.isNotEmpty(schoolIdList)) {
                String ids = StringUtils.join(schoolIdList, ",");
                message = "增加学校失败，该地区已有与" + schoolName + "同名的学校ID:" + ids + "注册";
                getAlertMessageManager().addMessageError(message);
                model.addAttribute("message", message);
                return "crm/school/addschool";
            }
        }

        String schoolIds = "";
        List<Map<String, Object>> schoolSnapShotList = new ArrayList<>();
        for (String schoolName : schoolNameList) {
            school.setCname(schoolName);
            school.setId(null);

            //如果是批量增加学校，则学校简称同学校名称
            if (StringUtils.isNotBlank(conditionMap.get("cname_list"))) {
                school.setShortName(schoolName);
            } else {
                school.setShortName(conditionMap.get("shortname"));
            }

            MapMessage msg = deprecatedSchoolServiceClient.getRemoteReference().upsertSchool(school, getCurrentAdminUser().getAdminUserName());
            if (!msg.isSuccess()) {
                continue;
            }
            long schoolId = SafeConverter.toLong(msg.get("id"));

            EduSystemType eduSystem = EduSystemType.of(conditionMap.get("eduSystem"));
            if (eduSystem != null) {
                SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
                if (extInfo == null) {
                    extInfo = new SchoolExtInfo();
                    extInfo.setId(schoolId);
                }
                extInfo.setEduSystem(eduSystem.name());
                schoolExtServiceClient.getSchoolExtService().upsertSchoolExtInfo(extInfo).getUninterruptibly();
            }
            if (StringUtils.isBlank(schoolIds))
                schoolIds += schoolId;
            else
                schoolIds += "," + schoolId;

            Map<String, Object> schoolSnapShot = new HashMap<>();
            schoolSnapShot.put("schoolId", schoolId);
            schoolSnapShot.put("schoolName", school.getCname());
            schoolSnapShot.put("regionCode", school.getRegionCode());

            if (school.getRegionCode() != null) {
                ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                if (region != null) {
                    schoolSnapShot.put("regionName", region.toString("/"));
                }
            }

            schoolSnapShot.put("authenticationState", AuthenticationState.safeParse(school.getAuthenticationState()).getDescription());
            schoolSnapShot.put("schoolLevel", SchoolLevel.safeParse(school.getLevel()).getDescription());
            schoolSnapShot.put("vipLevel", school.getVip());
            schoolSnapShot.put("schoolType", SchoolType.safeParse(school.getType()).getDescription());
            schoolSnapShot.put("eduSystem", eduSystem == null ? "未知" : eduSystem.getDescription());
            schoolSnapShotList.add(schoolSnapShot);
        }

        //operation不能过长
        String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "增加学校";

        //记录管理员操作日志
        addAdminLog(operation, schoolIds, schoolDesc);

        model.addAttribute("schoolSnapShotList", schoolSnapShotList);
        message = "添加学校" + schoolIds + "成功";
        getAlertMessageManager().addMessageInfo(message);

        model.addAttribute("message", message);

        return "crm/school/addschool";
    }

    /**
     * ***********************修改学校*****************************************************************
     */

    @RequestMapping(value = "updateschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updateSchool() {
        String[] conditionKeys = new String[]{"schoolId", "cmainname", "schooldistrict", "cname", "shortname", "type", "authenticationState",
                "vip", "countys", "schoolDesc", "level"};
        Map<String, String> conditionMap = new HashMap<>();
        for (String key : conditionKeys)
            conditionMap.put(key, getRequestParameter(key, "").replaceAll("\\s", ""));

        Map<String, Object> message = new HashMap<>();

        long schoolId;
        int type;
        int vip;
        int countys;
        int level;
        /*int authenticationState;*/

        try {
            schoolId = Long.parseLong(conditionMap.get("schoolId"));
            type = Integer.parseInt(conditionMap.get("type"));
            vip = Integer.parseInt(conditionMap.get("vip"));
            countys = Integer.parseInt(conditionMap.get("countys"));
            level = Integer.parseInt(conditionMap.get("level"));
            /*authenticationState = Integer.parseInt(conditionMap.get("authenticationState"));*/
        } catch (Exception ignored) {
            message.put("success", false);
            return message;
        }

        /*School school = schoolLoaderClient.loadAuthenticatedSchool(schoolId);*/
        School school = raikouSystem.loadSchool(schoolId);
        if (null == school || StringUtils.isBlank(conditionMap.get("schoolDesc")) || countys < 0) {
            message.put("success", false);
            return message;
        }

        // admin_log增量记录
        Map<String, Object> deltaMap = new HashMap<>();
        /*if (school.getAuthenticationState() != authenticationState) {
            deltaMap.put("authenticationState", school.getAuthenticationState() + "->" + authenticationState);
        }*/
        // 学校增加主干名和校区名 By 王悦晨2016-08-16
        if (!StringUtils.equals(school.getCmainName(), conditionMap.get("cmainname"))) {
            deltaMap.put("cmainname", school.getCmainName() + "->" + conditionMap.get("cmainname"));
        }
        if (!StringUtils.equals(school.getSchoolDistrict(), conditionMap.get("schooldistrict"))) {
            deltaMap.put("schooldistrict", school.getCmainName() + "->" + conditionMap.get("schooldistrict"));
        }
        if (!school.getCname().equals(conditionMap.get("cname"))) {
            deltaMap.put("cname", school.getCname() + "->" + conditionMap.get("cname"));
        }
        if (!school.getShortName().equals(conditionMap.get("shortname"))) {
            deltaMap.put("shortName", school.getShortName() + "->" + conditionMap.get("shortname"));
        }
        if (school.getRegionCode() != countys) {
            deltaMap.put("regionCode", school.getRegionCode() + "->" + countys);
        }
        if (school.getLevel() != level) {
            deltaMap.put("level", deltaMap.get("level") + "->" + level);
        }
        if (school.getType() != type) {
            deltaMap.put("type", school.getType() + "->" + type);
        }
        if (school.getVip() != vip) {
            deltaMap.put("vip", school.getVip() + "->" + vip);
        }
        if (!deltaMap.isEmpty()) {
            boolean check = getRequestBool("check");
            if (check) {
                Map<String, String> existSchoolMap = getSchoolSimilarityInfo(conditionMap.get("cmainname"), SchoolLevel.safeParse(level), countys);
                List<Map<String, String>> existSchoolList = new LinkedList<>();
                existSchoolMap.forEach((id, name) -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("schoolId", id);
                    map.put("schoolName", name);
                    existSchoolList.add(map);
                });
                return MapMessage.successMessage().add("existSchoolList", existSchoolList);
            }

//            school.setAuthenticationState(authenticationState);
            school.setCmainName(conditionMap.get("cmainname"));
            school.setSchoolDistrict(conditionMap.get("schooldistrict"));
            // 学校名称使用主干名 + 校区名 拼接而成
//            school.setCname(conditionMap.get("cname"));
            school.setShortName(conditionMap.get("shortname"));
            school.setRegionCode(countys);
            school.setCode(school.getRegionCode().toString());
            school.setLevel(level);
            school.setType(type);
            school.setVip(vip);

            String desc = conditionMap.get("schoolDesc");
            try {
                String operatorName = getCurrentAdminUser().getAdminUserName();
                MapMessage msg = deprecatedSchoolServiceClient.getRemoteReference().upsertSchoolWithDesc(school, operatorName, desc);
                message.put("success", msg.isSuccess());

                String operation = "管理员" + operatorName + "修改学校（" + schoolId + "）";

                //记录管理员操作日志
                addAdminLog(operation, schoolId, desc, deltaMap.toString());
            } catch (Exception ignored) {
                message.put("success", false);
            }
        } else {
            message.put("success", false);
        }

        return message;
    }

    /**
     * ***********************删除学校*****************************************************************
     */

    @RequestMapping(value = "deleteschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteSchool(@RequestParam String schoolId,
                                   @RequestParam String deleteDesc) {
        long sid = NumberUtils.toLong(schoolId);
        if (sid == 0) {
            return MapMessage.errorMessage();
        }
        deleteDesc = deleteDesc.replaceAll("\\s", "");
        if (StringUtils.isBlank(deleteDesc)) {
            return MapMessage.errorMessage("描述信息不能为空");
        }

        if (isSchoolInDict(sid, -1L)) {
            return MapMessage.errorMessage("该学校是字典表学校");
        }

        // 合作校不允许删除
        if (schoolExtServiceClient.isDaiteSchool(sid)) {
            return MapMessage.errorMessage("该学校是合作校，不允许删除");
        }

        MapMessage message = $deleteSchools(Collections.singleton(sid));
        if (!message.isSuccess()) {
            return message;
        }

        Collection successIds = (Collection) message.get("successIds");
        if (!successIds.contains(sid)) {
            return MapMessage.errorMessage("删除失败，请检查学校中是否存在未合并班级或者无班级的老师");
        }

        getAlertMessageManager().addMessageInfo("成功删除学校" + sid);
        String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "删除学校（" + sid + "）";
        //记录管理员操作日志
        addAdminLog(operation, sid, deleteDesc);
        return MapMessage.successMessage();
    }

    /**
     * ***********************校园大使*****************************************************************
     */

    @RequestMapping(value = "addschoolambassador.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addSchoolAmbassador(@RequestParam Long schoolId,
                                          @RequestParam String ambassadorUserId) {

        if (schoolId == null || schoolId == 0 || StringUtils.isBlank(ambassadorUserId)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {

            School school = raikouSystem.loadSchool(schoolId);
            Long newAmbassadorId = ConversionUtils.toLong(ambassadorUserId);
            if (school == null) {
                return MapMessage.errorMessage("学校不存在");
            }
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(newAmbassadorId);
            if (teacher == null || teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
                return MapMessage.errorMessage("老师不存在或者不是认证老师");
            }
            if (!Objects.equals(teacher.getTeacherSchoolId(), schoolId)) {
                return MapMessage.errorMessage("老师与学校不匹配");
            }
            if (teacherLoaderClient.loadMainTeacherId(Long.parseLong(ambassadorUserId)) != null) {//使用的账号是副账号则阻止
                return MapMessage.errorMessage("账号" + ambassadorUserId + "是老师的副账号,需要使用主账号");
            }

            //查询本校大使
            List<AmbassadorSchoolRef> refList = ambassadorLoaderClient.getAmbassadorLoader().findSchoolAmbassadorRefs(schoolId);
            if (CollectionUtils.isNotEmpty(refList)) {
                //判断有没有同学科的大使
                AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(teacher.getSubject(), schoolId);
                if (ref != null) {
                    return MapMessage.errorMessage("已经存在同学科校园大使");
                }
            }
            AmbassadorSchoolRef ref = new AmbassadorSchoolRef();
            ref.setSchoolId(teacher.getTeacherSchoolId());
            ref.setAmbassadorId(teacher.getId());
            ref = ambassadorServiceClient.getAmbassadorService().$insertAmbassadorSchoolRef(ref);

            // 插入大使级别 如果以前有记录， 删除
            AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(teacher.getId());
            if (levelDetail != null) {
                ambassadorServiceClient.getAmbassadorService().$disableAmbassadorLevelDetail(levelDetail.getId());
            }
            levelDetail = new AmbassadorLevelDetail();
            levelDetail.setSchoolId(teacher.getTeacherSchoolId());
            levelDetail.setAmbassadorId(teacher.getId());
            levelDetail.setLevel(AmbassadorLevel.TONG_PAI);
            levelDetail.setBornDate(new Date());
            ambassadorServiceClient.getAmbassadorService().$insertAmbassadorLevelDetail(levelDetail);
            // 如果有预备大使记录  删除
            AmbassadorCompetition competition = ambassadorLoaderClient.getAmbassadorLoader().loadTeacherAmbassadorCompetition(teacher.getId());
            if (competition != null) {
                ambassadorServiceClient.getAmbassadorService().$disableAmbassadorCompetition(competition.getId());
            }
            asyncUserServiceClient.getAsyncUserService()
                    .evictUserCache(newAmbassadorId)
                    .awaitUninterruptibly();
            // 将新老师的激活请求改为校园大使的
            businessTeacherServiceClient.changeActivationType(newAmbassadorId, true);

            // 提醒 短信 右下角
            String comment = StringUtils.formatMessage("恭喜您成为新的校园大使。马上到<a href='/ambassador/center.vpage'>『校园大使』</a>页面看看吧！");
            userPopupServiceClient.createPopup(teacher.getId())
                    .content(comment)
                    .type(PopupType.AMBASSADOR_REMIND)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
            UserAuthentication authentication = userLoaderClient.loadUserAuthentication(teacher.getId());
            if (authentication != null && authentication.isMobileAuthenticated()) {
                // 发短信
                userSmsServiceClient.buildSms().to(authentication)
                        .content("恭喜您成为新的校园大使。马上到『校园大使』页面看看吧！")
                        .type(SmsType.AMBASSADOR_REMIND_SMS)
                        .send();
            }

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacher.getId());
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.校园大使.name());
            userServiceRecord.setOperationContent("管理员添加用户为校园大使");
            userServiceRecord.setComments("学校:" + schoolId);
            userServiceClient.saveUserServiceRecord(userServiceRecord);

        } catch (Exception ignored) {
            return MapMessage.errorMessage("操作失败");
        }
        return MapMessage.successMessage();
    }


    //删除校园大使
    @RequestMapping(value = "disabledambassador.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage disabledSchoolAmbassador(@RequestParam Long schoolId,
                                               @RequestParam String ambassadorId) {
//        businessTeacherServiceClient.resignationAmbassador(teacherLoaderClient.loadTeacherDetail(SafeConverter.toLong(ambassadorId)));

        // 校园大使改版 此功能暂时封闭
        return MapMessage.errorMessage("此功能暂时关闭");
    }

    //批量导入校园大使
    @RequestMapping(value = "batchgenambassador.vpage", method = RequestMethod.GET)
    public String batchGenAmbassador() {
        return "site/batch/batchgenambassador";
    }

    @RequestMapping(value = "batchgenambassador.vpage", method = RequestMethod.POST)
    public String genAmbassador(@RequestParam String teacherIds, Model model) {
        getAlertMessageManager().addMessageError("此功能暂时关闭");
        return "site/batch/batchgenambassador";
    }

    @RequestMapping(value = "createaffairteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createAffairTeacher() {
        // 检查权限
        String authList = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.MIDDLE_PLATFORM_GENERAL.getType(), "CREATE_AFFAIR_TEACHER");
        if (Stream.of(authList.split(",")).noneMatch(auth -> auth.equals(getCurrentAdminUser().getAdminUserName()))) {
            return MapMessage.errorMessage("很抱歉，您没有操作权限");
        }

        String teacherName = getRequestString("teacherName");
        String mobile = getRequestString("phone");
        Long schoolId = getRequestLong("schoolId");

        if (StringUtils.isAnyBlank(teacherName, mobile)) {
            return MapMessage.errorMessage("请填写老师姓名和手机号");
        }

        // 检查姓名
        if (!teacherName.matches("^[\u4e00-\u9fa5]{1,6}$")) {
            return MapMessage.errorMessage("姓名只能是汉字，最多6个字");
        }

        // 检查手机号
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效的手机号");
        }
        List<UserAuthentication> userList = userLoaderClient.loadMobileAuthentications(mobile);
        boolean occupied = userList.stream()
                .filter(ua -> ua.getUserType() != null)
                .anyMatch(ua -> UserType.RESEARCH_STAFF == ua.getUserType());
        if (occupied) {
            return MapMessage.errorMessage("手机号已注册教务老师号");
        }
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("请确认添加教务老师的学校ID：" + schoolId);
        }

        // 检查完毕，开始创建账号
        try {
            MapMessage result = specialTeacherServiceClient.createAffairTeacher(schoolId, teacherName, mobile);
            if (result.isSuccess()) {
                String passwd = (String) result.get("defaultPassword");
                smsServiceClient.createSmsMessage(mobile)
                        .content("注册成功！用手机号和密码(" + passwd + ")即可登录一起作业教务系统。如有问题，可拨打400-160-1717")
                        .type(SmsType.AFFAIR_TEACHER_ACCOUNT_NOTICE.name())
                        .send();
            }
            return result;
        } catch (Exception ex) {
            logger.error("Failed create affair teacher account, schoolId={}, name={}, mobile={}", schoolId, teacherName, mobile, ex);
            return MapMessage.errorMessage("创建教务老师账号失败");
        }
    }

    @RequestMapping(value = "modifyaffairteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyAffairTeacher() {
        // 检查权限
        String authList = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.MIDDLE_PLATFORM_GENERAL.getType(), "CREATE_AFFAIR_TEACHER");
        if (Stream.of(authList.split(",")).noneMatch(auth -> auth.equals(getCurrentAdminUser().getAdminUserName()))) {
            return MapMessage.errorMessage("很抱歉，您没有操作权限");
        }

        Long teacherId = getRequestLong("teacherId");
        String mobile = getRequestString("mobile");

        ResearchStaff affairTeacher = researchStaffLoaderClient.loadResearchStaff(teacherId);
        if (affairTeacher == null) {
            return MapMessage.errorMessage("无效的教务老师ID：" + teacherId);
        }

        if (StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("请填写老师手机号");
        }

        // 检查手机号
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效的手机号");
        }

        List<UserAuthentication> userList = userLoaderClient.loadMobileAuthentications(mobile);
        boolean occupied = userList.stream()
                .filter(ua -> !teacherId.equals(ua.getId()))
                .filter(ua -> ua.getUserType() != null)
                .anyMatch(ua -> UserType.RESEARCH_STAFF == ua.getUserType());
        if (occupied) {
            return MapMessage.errorMessage("手机号已注册教务老师号");
        }

        // 检查完毕，激活手机号
        try {
            return userServiceClient.activateUserMobile(teacherId, mobile, true);
        } catch (Exception ex) {
            logger.error("Failed modify affair teacher account, teacherId={}, mobile={}", teacherId, mobile, ex);
            return MapMessage.errorMessage("创建教务老师账号失败");
        }
    }

    /**
     * ***********************批量处理******************************************************************
     */

    // todo : 参数应该写成Map传递
    @RequestMapping(value = "{batchtype}/batchdeleteormodifyschool.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String batchDeleteSchool(@PathVariable(value = "batchtype") String batchType,
                                    @RequestParam(value = "schoolIds", required = false, defaultValue = "") String schoolIdsStr,
                                    @RequestParam(required = false, defaultValue = "") String batchDeleteOrModifyDesc,
                                    @RequestParam(required = false) Integer provinceCode,
                                    @RequestParam(required = false) Integer cityCode,
                                    @RequestParam(required = false) Integer countyCode,
                                    Model model) {

        model.addAttribute("schoolIds", schoolIdsStr);
        model.addAttribute("batchDeleteOrModifyDesc", batchDeleteOrModifyDesc);
        model.addAttribute("batchType", batchType);

        if (batchType.equals("batchmodifyregion")) {
            model.addAttribute("provinceCode", provinceCode);
            model.addAttribute("cityCode", cityCode);
            model.addAttribute("countyCode", countyCode);
            model.addAttribute("provinces", crmUserService.getAllProvince());
        }

        if (StringUtils.isBlank(schoolIdsStr))
            return "crm/school/batchdeleteormodifyschool";

        batchDeleteOrModifyDesc = batchDeleteOrModifyDesc.replaceAll("\\s", "");
        if (StringUtils.isBlank(batchDeleteOrModifyDesc)) {
            getAlertMessageManager().addMessageError("描述信息不能为空");
            return "crm/school/batchdeleteormodifyschool";
        }

        Set<Long> schoolIdSet = new HashSet<>();
        try {

            String[] schoolIdStrList = schoolIdsStr.split("[,，\\s]+");
            for (String it : schoolIdStrList) {
//            schoolIdStrList.each { String it ->
                if (StringUtils.isNotBlank(it)) {
                    schoolIdSet.add(Long.valueOf(it));
                }
            }
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("存在不符合规范的学校ID");
            return "crm/school/batchdeleteormodifyschool";
        }

        if (CollectionUtils.isEmpty(schoolIdSet)) {
            getAlertMessageManager().addMessageError("未输入学校ID");
            return "crm/school/batchdeleteormodifyschool";
        }

        //TODO 长远：底层统一调的deleteSchools接口--添加组的相关逻辑
        model.addAttribute("success", batchSchoolDispatcher(batchType, schoolIdSet, batchDeleteOrModifyDesc, countyCode));

        return "crm/school/batchdeleteormodifyschool";
    }

    /**
     * ***********************合并学校*****************************************************************
     */

    @RequestMapping(value = "mergeschoolprecheck.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mergeSchoolPreCheck() {
        Long sourceSchoolId;//被合并学校
        Long targetSchoolId;//合并到的目标学校
        String mergeDesc;

        try {
            sourceSchoolId = Long.parseLong(getRequestParameter("sourceSchoolId", "").replaceAll("\\s", ""));
            targetSchoolId = Long.parseLong(getRequestParameter("targetSchoolId", ""));
            mergeDesc = getRequestParameter("mergeDesc", "").replaceAll("\\s", "");
        } catch (Exception ignored) {
            return MapMessage.errorMessage("请正确填写各参数");
        }

        return doMergeSchoolPreCheck(sourceSchoolId, targetSchoolId, mergeDesc);
    }

    @RequestMapping(value = "mergeschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mergeSchool() {

        Long sourceSchoolId;//被合并学校
        Long targetSchoolId;//合并到的目标学校
        String mergeDesc;

        try {
            sourceSchoolId = Long.parseLong(getRequestParameter("sourceSchoolId", "").replaceAll("\\s", ""));
            targetSchoolId = Long.parseLong(getRequestParameter("targetSchoolId", ""));
            mergeDesc = getRequestParameter("mergeDesc", "").replaceAll("\\s", "");
        } catch (Exception ignored) {
            return MapMessage.errorMessage("请正确填写各参数");
        }

        MapMessage msg = doMergeSchoolPreCheck(sourceSchoolId, targetSchoolId, mergeDesc);
        if (!msg.isSuccess()) {
            return msg;
        }

        //合并学校时学校信息处理;合并副校有已完善的信息，将信息合并至主校，取所有字段有值的并集。若两个学校同一字段均有值，取信息填写时间最新的值。
        MapMessage mm = AtomicCallbackBuilderFactory.getInstance().<MapMessage>newBuilder()
                .keyPrefix("CrmSchoolController.mergeSchoolExtInfo")
                .keys("ALL")
                .callback(() -> schoolExtServiceClient.getSchoolExtService()
                        .mergeSchoolExtInfo(sourceSchoolId, targetSchoolId)
                        .getUninterruptibly())
                .build()
                .execute();
        if (!mm.isSuccess()) {
            return MapMessage.errorMessage("合并学校信息失败");
        }

        try {
            MapMessage message = crmTeacherSystemClazzService.mergeSchool(sourceSchoolId, targetSchoolId, getCurrentAdminUser().getAdminUserName(), mergeDesc);
            if (!message.isSuccess()) {
                logger.error("合并学校失败,[targetSchoolId:{},sourceSchoolId:{}]", targetSchoolId, sourceSchoolId);
                return message;
            }
        } catch (Exception e) {
            logger.error("合并学校失败,[targetSchoolId:{},sourceSchoolId:{}]", targetSchoolId, sourceSchoolId, e);
            return MapMessage.errorMessage("合并学校失败，请联系技术同学");
        }

        //记录管理员操作日志
        String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "将学校（" + sourceSchoolId + "）合并到学校（" + targetSchoolId + "）";
        addAdminLog(operation, targetSchoolId, mergeDesc);

        getAlertMessageManager().addMessageInfo("学校ID:" + sourceSchoolId + "成功合并到ID:" + targetSchoolId);
        return MapMessage.successMessage();
    }

    // 批量合并学校
    @RequestMapping(value = "batchmergeschool.vpage", method = RequestMethod.GET)
    public String batchMergeSchool() {
        return "site/batch/batchmergeschool";
    }

    /**
     * 批量合并学校
     */
    @RequestMapping(value = "batchmergeschool.vpage", method = RequestMethod.POST)
    public String batchMergeSchool(Model model) {
        String content = getRequestString("batchContext");
        if (StringUtils.isBlank(content)) {
            getAlertMessageManager().addMessageError("请输入批量合并学校的数据");
            return "/site/batch/batchmergeschool";
        }
        String[] contents = content.split("\\r\\n");

        List<String> lstFailed = new ArrayList<>();
        List<String> lstSuccess = new ArrayList<>();

        int totalRecord = contents.length;
        for (String m : contents) {
            String[] contextArray = m.trim().split("[\\s]+");
            int contextArrayLen = contextArray.length;
            if (contextArrayLen != 3) {
                lstFailed.add(m + " 存在错误数据或不完整数据");
                continue;
            }
            String errmsg = "";
            try {
                // 被合并的学校
                Long sourceSchoolId = SafeConverter.toLong(contextArray[0]);
                // 合并到的学校
                Long targetSchoolId = SafeConverter.toLong(contextArray[1]);

                String mergeDesc = contextArray[2];

                MapMessage msg = doMergeSchoolPreCheck(sourceSchoolId, targetSchoolId, mergeDesc);
                if (!msg.isSuccess()) {
                    errmsg = msg.getInfo();
                    lstFailed.add(m + " " + errmsg);
                    continue;
                }

                MapMessage message = crmTeacherSystemClazzService.mergeSchool(sourceSchoolId, targetSchoolId, getCurrentAdminUser().getAdminUserName(), mergeDesc);
                if (!message.isSuccess()) {
                    logger.error("合并学校失败,[targetSchoolId:{},sourceSchoolId:{}]", targetSchoolId, sourceSchoolId);
                    errmsg = "合并失败：" + message.getInfo();
                    lstFailed.add(m + " " + errmsg);
                } else {
                    lstSuccess.add(m);
                    //记录管理员操作日志
                    String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "将学校（" + sourceSchoolId + "）合并到学校（" + targetSchoolId + "）";
                    addAdminLog(operation, targetSchoolId, mergeDesc);
                }
            } catch (Exception ignored) {
                lstFailed.add(m + " 合并发生异常" + ignored.getMessage());
            }
        }
        model.addAttribute("successlist", lstSuccess);
        model.addAttribute("failedlist", lstFailed);
        model.addAttribute("totalRecord", totalRecord);
        return "/site/batch/batchmergeschool";
    }

    @RequestMapping(value = "modifyedusystem.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifySchoolEduSystem() {
        Long schoolId = getRequestLong("schoolId");
        EduSystemType eduSystem = EduSystemType.of(getRequestString("eduSys"));
        String desc = getRequestString("desc");
        String confirmFlag = getRequestString("confirmCode");
        if (eduSystem == null) {
            return MapMessage.errorMessage("无效的学制").setErrorCode("001");
        }
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("请认真填写操作备注").setErrorCode("001");
        }
        boolean confirm = StringUtils.isNotBlank(confirmFlag);
        if (!confirm) {
            return crmSchoolExtInfoCheckServiceClient.beforeUpdateSchoolExtInfoEduSystem(schoolId, eduSystem);
        }
        return crmSchoolExtInfoCheckServiceClient.updateSchoolExtInfoEduSystem(schoolId, eduSystem, desc, "admin:" + getCurrentAdminUser().getAdminUserName());
    }

    @RequestMapping(value = "modifydaiteschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyDaiteSchool() {
        Long schoolId = getRequestLong("schoolId");
        String daiteFlag = getRequestString("daiteFlag");
        String daiteDesc = getRequestString("daiteDesc");

        if (schoolId == 0L || StringUtils.isBlank(daiteDesc) || StringUtils.isBlank(daiteFlag)) {
            return MapMessage.errorMessage("无效的参数");
        }

        // 指定用户有权限
        String operators = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "daite_school_operators");
        String curUsers = getCurrentAdminUser().getAdminUserName();
        if (!operators.contains("," + curUsers.toLowerCase() + ",")) {
            return MapMessage.errorMessage("此操作需要授权!");
        }

        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (extInfo == null) {
            extInfo = new SchoolExtInfo();
            extInfo.setId(schoolId);
        }

        if ("1".equals(daiteFlag)) {
            extInfo.setDaiteSchool(true);
        } else {
            extInfo.setDaiteSchool(false);
        }

        schoolExtServiceClient.getSchoolExtService().upsertSchoolExtInfo(extInfo);

        SchoolServiceRecord record = new SchoolServiceRecord();
        record.setSchoolOperationType(SchoolOperationType.UPDATE_SCHOOL_INFO);
        String updateContent = "1".equals(daiteFlag) ? "是" : "否";
        record.setOperationContent("更新合作校信息，更新为:" + updateContent);
        record.setSchoolId(schoolId);
        record.setOperatorId(getCurrentAdminUser().getAdminUserName());
        record.setOperatorName(getCurrentAdminUser().getAdminUserName());
        schoolServiceRecordServiceClient.addSchoolServiceRecord(record);

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "modifyadjustclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyAdjustClazz() {
        Long schoolId = getRequestLong("schoolId");
        boolean adjustClazzFlag = getRequestBool("adjustClazzFlag");
        String adjustClazzDesc = getRequestString("adjustClazzDesc");
        if (schoolId == 0L || StringUtils.isBlank(adjustClazzDesc)) {
            return MapMessage.errorMessage("无效的参数");
        }

        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (extInfo == null) {
            extInfo = new SchoolExtInfo();
            extInfo.setId(schoolId);
        }
        extInfo.setAdjustClazz(adjustClazzFlag);
        schoolExtServiceClient.getSchoolExtService().upsertSchoolExtInfo(extInfo);
        SchoolServiceRecord record = new SchoolServiceRecord();
        record.setSchoolOperationType(SchoolOperationType.UPDATE_SCHOOL_INFO);
        String updateContent = adjustClazzFlag ? "开启" : "关闭";
        record.setOperationContent("自主调整班级开关，更新为:" + updateContent + "[" + adjustClazzDesc + "]");
        record.setSchoolId(schoolId);
        record.setOperatorId(getCurrentAdminUser().getAdminUserName());
        record.setOperatorName(getCurrentAdminUser().getAdminUserName());
        schoolServiceRecordServiceClient.addSchoolServiceRecord(record);
        return MapMessage.successMessage();
    }

    /**
     * 设置教务老师的大考班权限
     *
     * @param userId     教务老师ID
     * @param schoolId   学习ID
     * @param permission 是否设置权限，true设置，false取消
     * @return
     */
    @RequestMapping(value = "modifyTeacherLagerExam.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyTeacherLagerExam(@RequestParam Long userId, @RequestParam Long schoolId, @RequestParam Boolean permission) {
        if (null == userId || null == permission) {
            return MapMessage.errorMessage("请填写用户ID与是否取消权限").setErrorCode("001");
        }

        try {
            MapMessage result = null;
            if (permission) {//设置权限
                result = teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(userId, schoolId, TeacherRoleCategory.O2O.name(), TeacherRolesType.EXAM_GROUP_MANAGER.name(), "");
                logger.info(userId + ":" + "设置了大考班权限");
            } else {//取消权限
                result = teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(userId, schoolId, TeacherRoleCategory.O2O.name(), TeacherRolesType.EXAM_GROUP_MANAGER.name());
                logger.info(userId + ":" + "取消了大考班权限");
            }
            if (null == result || !result.isSuccess()) {
                return MapMessage.errorMessage("内部错误，导致设置失败").setErrorCode("002");
            } else {
                String info = permission ? "设置大考班权限成功" : "取消大考班权限成功";
                logger.info(userId + ":" + info);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("内部错误，导致设置失败").setErrorCode("002");
        }
        return MapMessage.successMessage();
    }

    /**
     * *********************private method*****************************************************************
     */

    private List<Map<String, Object>> getSchoolSnapShot(Map<String, String> conditionMap) {

        List<Map<String, Object>> schoolSnapShotList = new ArrayList<>();

        //如果包含有schoolId，则忽略所有其他条件
        if (StringUtils.isNotBlank(conditionMap.get("schoolId"))) {

            Long schoolId;
            try {
                schoolId = conversionService.convert(conditionMap.get("schoolId"), Long.class);
            } catch (Exception ignored) {
                return Collections.emptyList();
            }

//            School school = schoolLoaderClient.loadAuthenticatedSchool(schoolId); // 未认证学校有统一的处理入口
            School school = raikouSystem.loadSchool(schoolId);
            if (null == school)
                return Collections.emptyList();

            Map<String, Object> schoolSnapShot = new HashMap<>();
            schoolSnapShot.put("schoolId", school.getId());
            schoolSnapShot.put("schoolName", school.getCname());
            schoolSnapShot.put("shortName", school.getShortName());
            schoolSnapShot.put("regionCode", school.getRegionCode());
            schoolSnapShot.put("createTime", school.getCreateTime());

            if (school.getRegionCode() != null) {
                ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                if (region != null) {
                    schoolSnapShot.put("regionName", region.toString("/"));
                }
            }

            //引自School,认证状态（0等待认证、1已认证、3未通过）
            if (school.getAuthenticationState() == null || school.getAuthenticationState() == 0)
                schoolSnapShot.put("authenticationState", "待鉴定");
            else if (school.getAuthenticationState() == 1)
                schoolSnapShot.put("authenticationState", "鉴定通过");
            else if (school.getAuthenticationState() == 3)
                schoolSnapShot.put("authenticationState", "未通过");
            else if (school.getAuthenticationState() == 4)
                schoolSnapShot.put("authenticationState", "假学校");

            schoolSnapShot.put("schoolLevel", SchoolLevel.safeParse(school.getLevel()).getDescription());
            if (school.getVip() != null)
                if (school.getVip() == 1)
                    schoolSnapShot.put("vipLevel", "重点学校");
                else if (school.getVip() == 2)
                    schoolSnapShot.put("vipLevel", "非重点学校");
                else
                    schoolSnapShot.put("vipLevel", 0);
            schoolSnapShot.put("schoolType", SchoolType.safeParse(school.getType()).getDescription());
            schoolSnapShotList.add(schoolSnapShot);
        } else {

            String querySchool = "";
            Map<String, Object> querySchoolParams = new HashMap<>();

            if (StringUtils.isNotBlank(conditionMap.get("schoolName"))) {
                querySchool += " AND CNAME LIKE :schoolName ";
                //fixme : here 这两种方式开始不管用，
                // 后来又管用了，在我使用这种"%${conditionMap.schoolName}%".toString()这种调用方法之后。
                //querySchool += " AND CNAME LIKE '%${conditionMap.schoolName}%' "
                //querySchoolParams.schoolName = "%${conditionMap.schoolName}%"
                querySchoolParams.put("schoolName", "%" + conditionMap.get("schoolName") + "%");
            }
            if (StringUtils.isNotBlank(conditionMap.get("shortName"))) {
                querySchool += " AND SHORT_NAME LIKE :shortName ";
                //fixme : here 这两种方式开始不管用，
                // 后来又管用了，在我使用这种"%${conditionMap.schoolName}%".toString()这种调用方法之后。
                //querySchool += " AND CNAME LIKE '%${conditionMap.schoolName}%' "
                //querySchoolParams.schoolName = "%${conditionMap.schoolName}%"
                querySchoolParams.put("shortName", "%" + conditionMap.get("shortName") + "%");
            }
            int provinces;
            int citys;
            int countys;
            try {
                provinces = Integer.parseInt(conditionMap.get("provinces"));
                citys = Integer.parseInt(conditionMap.get("citys"));
                countys = Integer.parseInt(conditionMap.get("countys"));
            } catch (Exception ignored) {
                return Collections.emptyList();
            }

            // FIXME: Longlong, please help to confirm if this code can work or not
            List<Integer> regionCodeList = new ArrayList<>();
            if (countys > 0) {
                regionCodeList.add(countys);
            } else if (citys > 0) {
                regionCodeList.add(citys);
                ExRegion cityRegion = raikouSystem.loadRegion(citys);
                for (ExRegion child : ExRegion.fetchAllChildren(cityRegion)) {
                    regionCodeList.add(child.getCode());
                }

            } else if (provinces > 0) {
                regionCodeList.add(provinces);
                ExRegion provinceRegion = raikouSystem.loadRegion(provinces);
                for (ExRegion child : ExRegion.fetchAllChildren(provinceRegion)) {
                    regionCodeList.add(child.getCode());
                }
            }

            if (!CollectionUtils.isEmpty(regionCodeList)) {
                querySchool += " AND REGION_CODE IN (:regionCodeList) ";
                querySchoolParams.put("regionCodeList", regionCodeList);
            }

            if (StringUtils.isNotBlank(querySchool)) {

                querySchool = "SELECT DISTINCT ID as schoolId, REGION_CODE as regionCode, CREATETIME as createTime, CNAME as schoolName, SHORT_NAME as shortName," +
                        " AUTHENTICATION_STATE as authenticationState, LEVEL as schoolLevel, VIP as vip, TYPE as type " +
                        " FROM VOX_SCHOOL WHERE DISABLED=0" + querySchool + "LIMIT :schoolLimit ";
                querySchoolParams.put("schoolLimit", MAX_SCHOOL_AMOUNT);

                final String querySchoolSql = querySchool;
                List<Map<String, Object>> schoolList = utopiaSql.withRoutingPolicy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave,
                        () -> utopiaSql.withSql(querySchoolSql).useParams(querySchoolParams).queryAll());
                if (schoolList != null) {

                    for (Map<String, Object> school : schoolList) {

                        Map<String, Object> schoolSnapShot = new HashMap<>();

                        schoolSnapShot.put("schoolId", school.get("schoolId"));
                        schoolSnapShot.put("schoolName", school.get("schoolName"));
                        schoolSnapShot.put("shortName", school.get("shortName"));
                        schoolSnapShot.put("regionCode", school.get("regionCode"));
                        schoolSnapShot.put("createTime", school.get("createTime"));
                        if (school.get("regionCode") != null) {
                            ExRegion region = raikouSystem.loadRegion((Integer) school.get("regionCode"));
                            if (region != null)
                                schoolSnapShot.put("regionName", region.toString("\\"));
                        }

                        //引自School,认证状态（0等待认证、1已认证、3未通过）
                        if (null == school.get("authenticationState") || Integer.valueOf(0).equals(school.get("authenticationState")))
                            schoolSnapShot.put("authenticationState", "待鉴定");
                        else if (Integer.valueOf(1).equals(school.get("authenticationState")))
                            schoolSnapShot.put("authenticationState", "鉴定通过");
                        else if (Integer.valueOf(3).equals(school.get("authenticationState")))
                            schoolSnapShot.put("authenticationState", "未通过");
                        else if (Integer.valueOf(4).equals(school.get("authenticationState")))
                            schoolSnapShot.put("authenticationState", "假学校");

                        schoolSnapShot.put("schoolLevel", SchoolLevel.safeParse((Integer) school.get("schoolLevel")).getDescription());
                        if (school.get("vip") != null)
                            if (Integer.valueOf(1).equals(school.get("vip")))
                                schoolSnapShot.put("vipLevel", "重点学校");
                            else if (Integer.valueOf(2).equals(school.get("vip")))
                                schoolSnapShot.put("vipLevel", "非重点学校");
                            else
                                schoolSnapShot.put("vipLevel", 0);
                        schoolSnapShot.put("schoolType", SchoolType.safeParse((Integer) school.get("type")).getDescription());
                        schoolSnapShotList.add(schoolSnapShot);
                    }
                }
            }
        }

        return schoolSnapShotList;
    }

    private List<Map<String, Object>> getAllProvince() {
        List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadProvinces();
        List<Map<String, Object>> provinces = new ArrayList<>();

        for (ExRegion region : regionList) {
            Map<String, Object> province = new HashMap<>();
            province.put("key", region.getCode());
            province.put("value", region.getName());
            provinces.add(province);
        }

        return provinces;
    }

    private List<Map<String, Object>> getAllProvincePinYin() {
        List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadProvinces();
        Set<ExRegion> rt = new TreeSet<>(pinYinComparator);
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

    private Map<String, Object> getSchoolInfoMap(Long schoolId) {
        School school = raikouSystem.loadSchool(schoolId);
        if (null == school) {
            return new HashMap<>();
        }

        Map<String, Object> schoolInfoAdminMapper = new HashMap<>();
        schoolInfoAdminMapper.put("schoolId", school.getId());
        schoolInfoAdminMapper.put("schoolName", school.getCname());
        schoolInfoAdminMapper.put("cmainName", school.getCmainName());
        schoolInfoAdminMapper.put("schoolDistrict", school.getSchoolDistrict());
        schoolInfoAdminMapper.put("schoolShortName", school.getShortName());
        schoolInfoAdminMapper.put("regionCode", school.getRegionCode());
        schoolInfoAdminMapper.put("createTime", school.getCreateTime());

        if (school.getRegionCode() != null) {
            ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
            if (region != null) {
                schoolInfoAdminMapper.put("regionName", region.toString("/"));
                schoolInfoAdminMapper.put("provinces", ConversionUtils.toString(region.getProvinceCode()));
                schoolInfoAdminMapper.put("citys", ConversionUtils.toString(region.getCityCode()));
                schoolInfoAdminMapper.put("countys", ConversionUtils.toString(region.getCountyCode()));
            }
        }

        //校园大使
        List<AmbassadorSchoolRef> refList = ambassadorLoaderClient.getAmbassadorLoader().findSchoolAmbassadorRefs(schoolId);
        List<Map<String, Object>> ambassadors = new ArrayList<>();
        for (AmbassadorSchoolRef ref : refList) {
            TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(ref.getAmbassadorId());
            Map<String, Object> map = new HashMap<>();
            map.put("ambassadorId", detail.getId());
            map.put("userName", detail.fetchRealname());
            map.put("subject", detail.getSubject());
            map.put("ambassadorDate", DateUtils.dateToString(ref.getCreateDatetime()));
            ambassadors.add(map);
        }
        schoolInfoAdminMapper.put("ambassadorList", ambassadors);

        // 教务老师
        boolean hasAffairTeacher = school.isJuniorSchool() || school.isSeniorSchool();
        schoolInfoAdminMapper.put("affairTeacher", hasAffairTeacher);
        if (hasAffairTeacher) {
            List<User> users = specialTeacherLoaderClient.findSchoolAffairTeachers(school.getId());
            //获取该学校有大考班权限的老师
            List<TeacherRoles> teacherRoles = teacherRolesServiceClient.getTeacherRolesService().loadSchoolRoleTeachers(schoolId, TeacherRolesType.EXAM_GROUP_MANAGER.toString());
            //抽取大考班权限的老师ID
            List<Long> lagerExamUserIds = teacherRoles.stream().collect(ArrayList::new, ((t, u) -> t.add(u.getUserId())), ArrayList::addAll);
            schoolInfoAdminMapper.put("affairTeacherList", users);
            schoolInfoAdminMapper.put("lagerExamUserIds", lagerExamUserIds);
        }

        //引自School,认证状态（0等待认证、1已认证、3未通过）
        if (null == school.getAuthenticationState() || school.getAuthenticationState() == 0)
            schoolInfoAdminMapper.put("authenticationState", "待鉴定");
        else if (school.getAuthenticationState() == 1)
            schoolInfoAdminMapper.put("authenticationState", "鉴定通过");
        else if (school.getAuthenticationState() == 3)
            schoolInfoAdminMapper.put("authenticationState", "未通过");
        else if (school.getAuthenticationState() == 4)
            schoolInfoAdminMapper.put("authenticationState", "假学校");
        schoolInfoAdminMapper.put("authenticationStateValue", (school.getAuthenticationState() != null) ? school.getAuthenticationState() : 0);

        schoolInfoAdminMapper.put("schoolLevel", SchoolLevel.safeParse(school.getLevel()).getDescription());
        schoolInfoAdminMapper.put("schoolLevelValue", school.getLevel());
        if (school.getVip() != null)
            if (school.getVip() == 1)
                schoolInfoAdminMapper.put("vipLevel", "重点学校");
            else if (school.getVip() == 2)
                schoolInfoAdminMapper.put("vipLevel", "非重点学校");
            else
                schoolInfoAdminMapper.put("vipLevel", 0);
        schoolInfoAdminMapper.put("schoolType", SchoolType.safeParse(school.getType()).getDescription());
        schoolInfoAdminMapper.put("schoolTypeValue", school.getType());
        schoolInfoAdminMapper.put("payOpen", school.getPayOpen());

        List<Clazz> clazzInfoList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .enabled()
                .toList();
        List<Long> clazzIds = clazzInfoList.stream().map(Clazz::getId).collect(Collectors.toList());
        List<GroupMapper> clazzGroups = deprecatedGroupLoaderClient.loadClazzGroups(clazzIds).values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        Set<Long> clazzExistGroupIds = new HashSet<>();
        List<Map<String, Object>> clazzList = new ArrayList<>();
        for (GroupMapper mapper : clazzGroups) {
            if (mapper != null && !clazzExistGroupIds.contains(mapper.getClazzId())) {
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(mapper.getClazzId());
                if (clazz != null) {

                    Map<String, Object> clazzMap = new HashMap<>();
                    clazzMap.put("id", clazz.getId());
                    clazzMap.put("classLevel", clazz.getClassLevel());
                    clazzMap.put("className", clazz.formalizeClazzName());

                    Set<Long> clazzGroupIds = deprecatedGroupLoaderClient.loadClazzGroups(mapper.getClazzId()).stream().map(GroupMapper::getId).collect(Collectors.toSet());
                    List<GroupTeacherTuple> groupTeacherRefList = raikouSDK.getClazzClient()
                            .getGroupTeacherTupleServiceClient()
                            .findByGroupIds(clazzGroupIds);
                    List<Map<String, Object>> teacherInfoList = new ArrayList<>();
                    for (GroupTeacherTuple clazzTeacherRef : groupTeacherRefList) {
                        CollectionUtils.addNonNullElement(teacherInfoList, mapTeacherInfo(clazzTeacherRef.getTeacherId()));
                    }

                    clazzMap.put("teacherInfoList", teacherInfoList);
                    clazzList.add(clazzMap);
                    clazzExistGroupIds.add(clazz.getId());
                }
            }
        }

        // 学校学制
        String schoolEduSystem = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
        EduSystemType eduSystemType = EduSystemType.of(schoolEduSystem);
        schoolInfoAdminMapper.put("schoolEduSystem", schoolEduSystem);
        schoolInfoAdminMapper.put("edusystem", eduSystemType == null ? "未设置" : eduSystemType.getDescription());

        List<Map<String, Object>> clazzNoGroupList = new ArrayList<>();
        for (Clazz c : clazzInfoList) {
            if (c.isSystemClazz() && !clazzExistGroupIds.contains(c.getId())) {
                Map<String, Object> noGroupsClazzMap = new HashMap<>();
                List<Map<String, Object>> noGroupsClazzTeacherInfoList = new ArrayList<>();
                noGroupsClazzMap.clear();
                noGroupsClazzMap.put("id", c.getId());
                noGroupsClazzMap.put("classLevel", c.getClassLevel());
                noGroupsClazzMap.put("className", c.formalizeClazzName());
                noGroupsClazzMap.put("teacherInfoList", noGroupsClazzTeacherInfoList);
                clazzNoGroupList.add(noGroupsClazzMap);
            }
        }

        if (CollectionUtils.isNotEmpty(clazzInfoList)) {
            clazzList.sort(clazzInfoSort);
            clazzNoGroupList.sort(clazzInfoSort);

            List<List<Map<String, Object>>> clazzLevelList = new ArrayList<>();
            for (Map<String, Object> clazzMap : clazzList) {
                List<Map<String, Object>> clazzLevelChildList = null;
                for (List<Map<String, Object>> itList : clazzLevelList) {
                    if (!CollectionUtils.isEmpty(itList) && itList.get(0).get("classLevel").equals(clazzMap.get("classLevel"))) {
                        clazzLevelChildList = itList;
                    }
                }

                if (null == clazzLevelChildList) {
                    clazzLevelChildList = new ArrayList<>();
                    clazzLevelChildList.add(clazzMap);
                    clazzLevelList.add(clazzLevelChildList);
                } else {
                    clazzLevelChildList.add(clazzMap);
                }
            }
            List<List<Map<String, Object>>> noGroupsClazzLevelList = new ArrayList<>();
            for (Map<String, Object> clazzMap : clazzNoGroupList) {
                List<Map<String, Object>> clazzLevelChildList = null;
                for (List<Map<String, Object>> itList : noGroupsClazzLevelList) {
                    if (!CollectionUtils.isEmpty(itList) && itList.get(0).get("classLevel").equals(clazzMap.get("classLevel"))) {
                        clazzLevelChildList = itList;
                    }
                }

                if (null == clazzLevelChildList) {
                    clazzLevelChildList = new ArrayList<>();
                    clazzLevelChildList.add(clazzMap);
                    noGroupsClazzLevelList.add(clazzLevelChildList);
                } else {
                    clazzLevelChildList.add(clazzMap);
                }
            }

            schoolInfoAdminMapper.put("clazzLevelList", clazzLevelList);
            schoolInfoAdminMapper.put("noGroupsClazzLevelList", noGroupsClazzLevelList);

        }

        String queryTeacherWithoutClazz = "SELECT VUS.USER_ID FROM VOX_USER_SCHOOL_REF VUS WHERE NOT EXISTS " +
                " (SELECT ID FROM VOX_GROUP_TEACHER_REF GTR WHERE GTR.TEACHER_ID = VUS.USER_ID AND GTR.STATUS='VALID' AND GTR.DISABLED = 0 " +
                " AND VUS.DISABLED = 0) AND VUS.SCHOOL_ID = ? AND VUS.DISABLED = 0";
        List<Long> teacherWithoutClazzIdList = utopiaSql.withSql(queryTeacherWithoutClazz).useParamsArgs(schoolId).queryColumnValues(Long.class);
        List<Map<String, Object>> teacherInfoWithoutClazzList = new ArrayList<>();

        for (Long teacherWithoutClazzId : teacherWithoutClazzIdList) {
            CollectionUtils.addNonNullElement(teacherInfoWithoutClazzList, mapTeacherInfo(teacherWithoutClazzId));
        }
        schoolInfoAdminMapper.put("teacherInfoWithoutClazzList", teacherInfoWithoutClazzList);

        // 中学，教学班级信息
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo != null) {
            schoolInfoAdminMapper.put("locked", SafeConverter.toBoolean(schoolExtInfo.getLocked()));
            if (school.isJuniorSchool() || school.isSeniorSchool()) {
                Map<ClazzLevel, Map<Subject, Set<String>>> walkingClazzs = schoolExtInfo.getWalkingClazzs();
                if (MapUtils.isNotEmpty(walkingClazzs)) {
                    Set<String> walkingClazzNames = new HashSet<>();
                    walkingClazzs.forEach((cl, map) -> {
                        String grade = cl.getDescription();
                        map.forEach((sub, names) -> {
                            names.forEach(name -> walkingClazzNames.add(grade + sub.getValue() + name));
                        });
                    });
                    schoolInfoAdminMapper.put("walkingClazzList", walkingClazzNames);
                }
            }
        }

        return schoolInfoAdminMapper;
    }

    private List<Long> getSchoolIdIfExist(School school) {
        if (null == school)
            return Collections.emptyList();
        String querySchool = "SELECT ID FROM VOX_SCHOOL WHERE DISABLED=0 AND CNAME=? " +
                "AND REGION_CODE=? " +
                "AND LEVEL=?";
        return utopiaSql.withSql(querySchool).useParamsArgs(school.getCname(), school.getRegionCode(), school.getLevel()).queryColumnValues(Long.class);
    }

    private HSSFWorkbook convertToHSSfWorkbook(List<School> schoolList) {

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow firstRow = hssfSheet.createRow(0);
        firstRow.createCell(0).setCellValue("学校ID");
        firstRow.createCell(1).setCellValue("学校名称");
        firstRow.createCell(2).setCellValue("地区编号");
        firstRow.createCell(3).setCellValue("鉴定状态");
        firstRow.createCell(4).setCellValue("学校级别");
        firstRow.createCell(5).setCellValue("vip等级");
        firstRow.createCell(6).setCellValue("学校类型");

        int rowNum = 1;
        for (School school : schoolList) {
            HSSFRow hssfRow = hssfSheet.createRow(rowNum++);
            hssfRow.createCell(0).setCellValue(school.getId());
            hssfRow.createCell(1).setCellValue(school.getCname());
            hssfRow.createCell(2).setCellValue(school.getRegionCode());
            hssfRow.createCell(3).setCellValue(school.getAuthenticationState() == 0 ? "待鉴定" : (school.getAuthenticationState() == 1 ? "鉴定通过" : "假学校"));
            hssfRow.createCell(4).setCellValue(SchoolLevel.safeParse(school.getLevel()).getDescription());
            hssfRow.createCell(5).setCellValue(school.getVip());
            hssfRow.createCell(6).setCellValue(SchoolType.safeParse(school.getType()).getDescription());
        }

        return hssfWorkbook;
    }

    private Boolean batchSchoolDispatcher(String batchType, Set<Long> schoolIdSet, String desc, Object... params) {
        switch (batchType.intern()) {
            case "batchdelete":
                return batchDeleteSchoolDealer(schoolIdSet, desc);
            case "batchmodifyregion":
                return batchModifySchoolRegionDealer(schoolIdSet, desc, (Integer) params[0]);
        }
        return false;
    }

    private boolean batchModifySchoolRegionDealer(Set<Long> schoolIdSet, String batchDeleteOrModifyDesc, Integer regionCode) {
        MapMessage message = changeSchoolsRegion(schoolIdSet, regionCode,
                new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
        if (!message.isSuccess()) {
            getAlertMessageManager().addMessageError("批量修改学校区域失败,请正确设置学校所在区");
            return false;
        }
        //记录管理员操作日志
        String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "修改学校区域->" + regionCode;
        addAdminLog(operation, StringUtils.join(schoolIdSet, ","));
        getAlertMessageManager().addMessageSuccess("批量修改学校区域成功");
        return true;
    }

    private HSSFWorkbook convertToHSSfWorkbookForSchoolTeachers(String schoolName, ExRegion regionEx, List<Teacher> teacherList) {

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow firstHssfRow = hssfSheet.createRow(0);
        firstHssfRow.createCell(0).setCellValue("老师ID");
        firstHssfRow.createCell(1).setCellValue("老师姓名");
        firstHssfRow.createCell(2).setCellValue("科目");
        firstHssfRow.createCell(3).setCellValue("是否认证");
        firstHssfRow.createCell(4).setCellValue("电话");
        firstHssfRow.createCell(5).setCellValue("邮箱");
        firstHssfRow.createCell(6).setCellValue("学校");
        firstHssfRow.createCell(7).setCellValue("地区");

        int hssfRowIndex = 1;
        for (Teacher teacher : teacherList) {
//        teacherList.each { Teacher teacher ->
            HSSFRow hssfRow = hssfSheet.createRow(hssfRowIndex++);
            hssfRow.createCell(0).setCellValue(teacher.getId());
            hssfRow.createCell(3).setCellValue(teacher.fetchCertificationState().getDescription());
            hssfRow.createCell(6).setCellValue(schoolName);

            if (teacher.getProfile() != null) {
                UserProfile profile = teacher.getProfile();
                hssfRow.createCell(1).setCellValue(profile.getRealname());
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
                if (ua != null) {
                    hssfRow.createCell(4).setCellValue(ua.getSensitiveMobile());
                    hssfRow.createCell(5).setCellValue(ua.getSensitiveEmail());
                }
            }

            if (teacher.getSubject() != null) {
                hssfRow.createCell(2).setCellValue(teacher.getSubject().getValue());
            }

            if (regionEx != null) {
                hssfRow.createCell(7).setCellValue(regionEx.toString(" / ") + "(" + regionEx.getProvinceCode() + ")");
            }
        }

        return hssfWorkbook;
    }

    @RequestMapping(value = "schoolteacherlist.vpage", method = RequestMethod.GET)
    public String schoolTeacherList(Model model) {
        Long schoolId = getRequestLong("schoolId", -1);
        if (schoolId > 0) {
            List<Map> teacherList = new ArrayList<>();
            List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(schoolId);
            if (!CollectionUtils.isEmpty(teachers)) {
                List<Long> teacherIds = new ArrayList<>();
                for (Teacher teacher : teachers) {
                    teacherIds.add(teacher.getId());
                }
                Map<Long, CertificationCondition> ctMap = businessTeacherServiceClient.batchGetCertificationCondition(teacherIds);
                String inviteSlq = "SELECT INVITE_ID,COUNT(1) AS COUNT FROM VOX_INVITE_HISTORY WHERE INVITE_ID IN(:teacherIds) AND DISABLED = TRUE  GROUP BY INVITE_ID;";
                List<Map<String, Object>> inviteList = utopiaSql.withSql(inviteSlq).useParams(MiscUtils.map("teacherIds", teacherIds)).queryAll();
                Map<Long, Integer> inviteMap = new HashMap<>();
                for (Map<String, Object> invite : inviteList) {
                    inviteMap.put(ConversionUtils.toLong(invite.get("INVITE_ID")), ConversionUtils.toInt(invite.get("COUNT")));
                }
                Map<Long, UserActivity> userActivityMap = userActivityServiceClient.getUserActivityService()
                        .findUserActivities(teacherIds)
                        .getUninterruptibly()
                        .values()
                        .stream()
                        .map(t -> t.stream()
                                .filter(a -> a.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME)
                                .sorted((o1, o2) -> {
                                    long a1 = SafeConverter.toLong(o1.getActivityTime());
                                    long a2 = SafeConverter.toLong(o2.getActivityTime());
                                    return Long.compare(a2, a1);
                                })
                                .findFirst()
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(UserActivity::getUserId, t -> t));
                for (Teacher teacher : teachers) {
                    Map<String, Object> teacherMap = new HashMap<>();
                    CertificationCondition certificationCondition = ctMap.get(teacher.getId());
                    teacherMap.put("integral", teacherLoaderClient.loadMainSubTeacherUserIntegral(teacher.getId(), teacher.getKtwelve()).getUsable());
                    if (userActivityMap.get(teacher.getId()) != null)
                        teacherMap.put("activityTime", userActivityMap.get(teacher.getId()).getActivityTime());
                    teacherMap.put("inviteCount", inviteMap.get(teacher.getId()) == null ? 0 : inviteMap.get(teacher.getId()));
                    teacherMap.put("teacher", teacher);
                    teacherMap.put("certificationCondition", certificationCondition);
                    teacherMap.put("certificationState", AuthenticationState.safeParse(teacher.getAuthenticationState()).getDescription());
                    teacherList.add(teacherMap);
                }
            }
            model.addAttribute("teachers", teacherList);
        }
        List<Map<String, Object>> teacherClassNumRs = utopiaSql.withSql(teacherClassNumSql).useParamsArgs(schoolId).queryAll();
        Map<String, String> teacherClassNum = new HashMap<>();
        for (Map<String, Object> row : teacherClassNumRs) {
            teacherClassNum.put(String.valueOf(row.get("uid")), String.valueOf(row.get("ct")));
        }

        List<Map<String, Object>> otherTeacherClassNumRs = utopiaSql.withSql((String) otherTeacherClassNum).useParamsArgs(schoolId).queryAll();
        Map<String, String> otherClassNum = new HashMap<String, String>();
        for (Map<String, Object> row : otherTeacherClassNumRs) {
            otherClassNum.put(String.valueOf(row.get("uid")), String.valueOf(row.get("ct")));
        }
        model.addAttribute("teacherClassNum", teacherClassNum);
        model.addAttribute("otherClassNum", otherClassNum);
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("recordTypeList", RecordType.toKeyValuePairs());
        return "crm/school/schoolteacherlist";
    }

    /**
     * 学校详情页新建班级
     */
    @RequestMapping(value = "addsysclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addNewSystemClazz() {
        Long schoolId = getRequestLong("schoolId");
        int level = getRequestInt("clazzLevel");
        ClazzLevel clazzLevel = ClazzLevel.parse(level);
        String clazzName = getRequestString("clazzName");
        clazzName = StringUtils.remove(clazzName, "班") + "班";
        if (schoolId < 0) {
            return MapMessage.errorMessage("学校Id获取错误,请刷新重试");
        }
        if (clazzLevel == null) {
            return MapMessage.errorMessage("年级信息获取错误,请刷新重试");
        }
        if (StringUtils.isBlank(clazzName)) {
            return MapMessage.errorMessage("班级名称不能为空");
        }
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("学校不存在");
        }
        String N = clazzName;
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .enabled()
                .clazzLevel(clazzLevel)
                .toList()
                .stream()
                .filter(t -> StringUtils.equals(t.getClassName(), N))
                .filter(Clazz::isSystemClazz)
                .findFirst()
                .orElse(null);
        if (clazz != null) {
            return MapMessage.errorMessage("此学校已经存在{}{}", clazzLevel.getDescription(), clazzName);
        }
        ClassMapper classMapper = new ClassMapper();
        classMapper.setSchoolId(schoolId);
        classMapper.setClassLevel(ConversionUtils.toString(clazzLevel.getLevel()));
        classMapper.setClazzName(clazzName);
        classMapper.setFreeJoin(Boolean.TRUE);
        classMapper.setEduSystem(schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly());

        classMapper.setOperatorId(getCurrentAdminUser().getAdminUserName());
        MapMessage createSysClazzMessage = clazzServiceClient.createSystemClazz(Collections.singleton(classMapper));
        if (createSysClazzMessage.isSuccess()) {
            String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "在学校:" + schoolId + "创建了" + clazzLevel.getDescription() + clazzName;
            addAdminLog(operation);
            return createSysClazzMessage.setInfo("创建" + clazzLevel.getDescription() + clazzName + "成功");
        } else {
            return createSysClazzMessage.setInfo("创建" + clazzLevel.getDescription() + clazzName + "失败");
        }
    }

    /**
     * 学校详情页班级列表 删除班级
     */
    @RequestMapping(value = "delclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delClazz() {
        Long schoolId = getRequestLong("schoolId");
        Long clazzId = getRequestLong("clazzId");
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (Objects.isNull(clazz)) {
            return MapMessage.errorMessage("未找到此班级，请检查班级ID输入是否正确！");
        }
        if (!clazz.getSchoolId().equals(schoolId)) {
            return MapMessage.errorMessage("请在班级所在学校详情页进行此操作！");
        }
        Map<Long, List<GroupMapper>> clazzGroupMap = deprecatedGroupLoaderClient.loadClazzGroups(Collections.singleton(clazzId));
        List<GroupMapper> groupMappers = clazzGroupMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(groupMappers)) {
            return MapMessage.errorMessage("请先删除班级分组下的学生和老师，再进行此操作！");
        }

        int row = newClazzServiceClient.getNewClazzService()
                .disableClazz(clazzId)
                .getUninterruptibly();

        if (row > 0) {
            return MapMessage.successMessage("删除成功，请刷新页面查看数据！");
        }

        return MapMessage.errorMessage("删除失败！");

    }

    @RequestMapping(value = "addwalkingclazzname.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addWalkingClazzName() {
        Long schoolId = getRequestLong("schoolId");
        int level = getRequestInt("clazzLevel");
        ClazzLevel clazzLevel = ClazzLevel.parse(level);
        String clazzName = getRequestString("clazzName");
        clazzName = StringUtils.remove(clazzName, "班") + "班";
        String subjectStr = getRequestString("clazzSubject");
        Subject subject = Subject.of(subjectStr);

        return schoolExtServiceClient.getSchoolExtService()
                .addWalkingClazzName(schoolId, clazzLevel, subject, clazzName)
                .getUninterruptibly();
    }

    // 老师角色列表
    @RequestMapping(value = "teacherroleslist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> teacherRolesListShow() {
        Long schoolId = getRequestLong("schoolId");
        String teacherRoles = getRequestString("teacherRoles");

        School school = raikouSystem.loadSchool(schoolId);

        String schoolEduSystem = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
        EduSystemType eduSystemType = EduSystemType.of(schoolEduSystem);

        List<Map<String, Object>> result = teacherRolesListBack(schoolId, teacherRoles, eduSystemType);

        return MapMessage.successMessage().add("data", result);
    }

    private List<Map<String, Object>> teacherRolesListBack(Long schoolId, String teacherRoles, EduSystemType edusystem) {

        List<Map<String, Object>> result = new ArrayList<>();
        List<TeacherRoles> teacherRolesList = teacherRolesServiceClient.getTeacherRolesService().loadSchoolRoleTeachers(schoolId, teacherRoles);

        for (TeacherRoles roleInfo : teacherRolesList) {
            User user = userLoader.loadUser(roleInfo.getUserId());
            if (user == null) {
                continue;
            }

            Map<String, Object> roleMap = new HashMap<>();

            roleMap.put("rolesName", TeacherRolesType.valueOf(teacherRoles).getDescription());
            roleMap.put("teacherId", roleInfo.getUserId());
            roleMap.put("teacherName", user.fetchRealname());
            if (StringUtils.isBlank(roleInfo.getRoleContent())) {
                roleMap.put("comment", "-");
            } else {
                // 班主任
                if (Objects.equals(roleInfo.getRoleType(), TeacherRolesType.CLASS_MANAGER.name())) {
                    List<Long> clazzIds = JsonUtils.fromJsonToList(roleInfo.getRoleContent(), Long.class);
                    Map<Long, Clazz> classes = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            .loadClazzs(clazzIds)
                            .stream()
                            .collect(Collectors.toMap(Clazz::getId, Function.identity()));
                    Set<String> classNames = classes.values().stream().map(Clazz::formalizeClazzName).collect(Collectors.toSet());
                    roleMap.put("comment", JsonUtils.toJson(classNames));
                } else if (Objects.equals(roleInfo.getRoleType(), TeacherRolesType.GRADE_MANAGER.name())) {
                    List<Integer> jieList = JsonUtils.fromJsonToList(roleInfo.getRoleContent(), Integer.class);
                    Set<String> clazzLevelNames = new HashSet<>();
                    for (Integer jie : jieList) {
                        ClazzLevel clazzLevel = ClassJieHelper.toClazzLevel(jie, edusystem);
                        if (clazzLevel == ClazzLevel.PRIVATE_GRADE || clazzLevel.getLevel() > ClazzLevel.SENIOR_THREE.getLevel()) {
                            continue;
                        }
                        clazzLevelNames.add(clazzLevel.getDescription());
                    }
                    roleMap.put("comment", JsonUtils.toJson(clazzLevelNames));
                } else if (Objects.equals(roleInfo.getRoleType(), TeacherRolesType.SUBJECT_LEADER.name())) {
                    List<ClazzLevel> clazzLevels = JsonUtils.fromJsonToList(roleInfo.getRoleContent(), ClazzLevel.class);
                    Set<String> clazzLevelNames = clazzLevels.stream().map(ClazzLevel::getDescription).collect(Collectors.toSet());

                    TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(roleInfo.getUserId());
                    String subject = "";
                    if (teacher != null) {
                        subject = teacher.getSubject().getValue();
                    }
                    roleMap.put("comment", subject + JsonUtils.toJson(clazzLevelNames));
                }
            }

            result.add(roleMap);
        }

        return result;
    }

    /**
     * **设置学校下的班级最大人数***
     */
    @RequestMapping(value = "setclazzstudentcount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setClazzStudentCount() {
        long schoolId = getRequestLong("schoolId");
        Integer studentCount = getRequestInt("studentCount");
        String comment = getRequestString("comment");
        if (schoolId < 1) {
            return MapMessage.errorMessage("学校id错误，请刷新重试");
        }
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("学校信息错误，请刷新重试");
        }
        if (studentCount < 100 || studentCount > 200) {
            return MapMessage.errorMessage("班级人数只能在100-200之间");
        }
        if (StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("备注信息不能为空,必须填写是哪个市场人员提出的修改需求");
        }
        List<GlobalTag> collect = globalTagServiceClient.getGlobalTagService()
                .loadAllGlobalTagsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> GlobalTagName.MaxClassCapacity == GlobalTagName.valueOf(e.getTagName()))
                .filter(p -> p.getTagValue().startsWith(schoolId + ":"))
                .collect(Collectors.toList());
        try {
            GlobalTag tag;
            String operation = "setClazzStudentCount : " + "schoolId : " + schoolId + " studentCount : " + studentCount;
            if (CollectionUtils.isEmpty(collect)) {
                tag = new GlobalTag();
                tag.setTagName(GlobalTagName.MaxClassCapacity.name());
                tag.setTagValue(schoolId + ":" + studentCount);
                tag.setTagComment(comment);
                globalTagServiceClient.getGlobalTagService().insertGlobalTag(tag).awaitUninterruptibly();
                addAdminLog(operation, schoolId, comment);
            } else if (collect.size() == 1) {
                tag = collect.get(0);
                tag.setTagValue(schoolId + ":" + studentCount);
                tag.setTagComment(comment);
                globalTagServiceClient.getGlobalTagService().updateGlobalTag(tag).awaitUninterruptibly();
                addAdminLog(operation, schoolId, comment);
            } else {
                logger.error(" crm setClazzStudentCount error : list of tagvlue is not 1 " + "schoolId : " + schoolId + " studentCount : " + studentCount + " comment : " + comment);
                return MapMessage.errorMessage("修改班级人数上限失败,请联系开发人员");
            }
        } catch (Exception e) {
            logger.error(" crm setClazzStudentCount error : " + "schoolId : " + schoolId + " studentCount : " + studentCount + " comment : " + comment);
            return MapMessage.errorMessage("修改班级人数上限失败,请联系开发人员");
        }
        return MapMessage.successMessage("修改班级人数上限成功,学校" + schoolId + "现在的班级人数上限为:" + studentCount);
    }

    /**
     * 删除定制的教学班级
     */
    @RequestMapping(value = "removewalkingclazzname.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeWalkingClazzName() {
        long schoolId = getRequestLong("schoolId");
        String name = getRequestString("clazzFullName");

        return schoolExtServiceClient.getSchoolExtService()
                .removeWalkingClazzName(schoolId, name)
                .getUninterruptibly();
    }

    /**
     * 删除教务老师
     */
    @RequestMapping(value = "removeResearchStaff.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeResearchStaff() {
        Long userId = getRequestLong("userId");
        researchStaffUserServiceClient.unsetManagedRegionByRStaffId(userId);

        // 删除手机号
        userServiceClient.getRemoteReference().cleanupUserMobile(getCurrentAdminUser().getAdminUserName(), userId);
        return MapMessage.successMessage().add("userId", userId);
    }

    private boolean batchDeleteSchoolDealer(Set<Long> schoolIdSet, String batchDeleteOrModifyDesc) {
        MapMessage message = $deleteSchools(schoolIdSet);
        if (!message.isSuccess()) {
            return false;
        }

        Collection successIds = (Collection) message.get("successIds");
        Collection failureIds = (Collection) message.get("failureIds");

        if (CollectionUtils.isEmpty(failureIds)) {
            getAlertMessageManager().addMessageSuccess("成功删除所有学校");
        } else {
            getAlertMessageManager().addMessageError("未成功删除的学校ID:" + StringUtils.join(failureIds, ","));
        }
        //记录管理员操作日志
        String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "删除学校";
        addAdminLog(operation, "", batchDeleteOrModifyDesc, "成功删除的学校：" +
                StringUtils.join(successIds, ",") + ";未成功删除的学校:" + StringUtils.join(failureIds, ","));

        return true;
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    private class PinYinComparator implements Comparator<Region> {
        private Map<String, String> regionPinYin = new HashMap<>();

        @Override
        public int compare(Region s1, Region s2) {
            if (s1.getId() == -1) {
                return -1;
            }
            if (s2.getId() == -1) {
                return 1;
            }
            String str1 = this.getPinYin(s1.getName());
            String str2 = this.getPinYin(s2.getName());
            for (int i = 0; i < str1.length() && i < str2.length(); i++) {
                char c1 = str1.charAt(i);
                char c2 = str2.charAt(i);
                if (c1 > c2) {
                    return 1;
                }
                if (c1 < c2) {
                    return -1;
                }
            }
            return Integer.compare(str1.length(), str2.length());

        }

        private String getPinYin(String sin) {
            String rtn = regionPinYin.get(sin);
            if (null == rtn) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < sin.length(); i++) {
                    char c = sin.charAt(i);
//                for (char c : sin.getChars()) {
                    if ('广' == c) {
                        sb.append("guang3");
                        continue;
                    }
                    String[] py = PinyinHelper.toHanyuPinyinStringArray(c);
                    if (null == py || py.length < 1) {
                        sb.append(c);
                        continue;
                    }
                    if (py.length > 1) {
                        Arrays.sort(py, (str1, str2) -> {
                            for (int i1 = 0; i1 < str1.length() && i1 < str2.length(); i1++) {
                                char c1 = str1.charAt(i1);
                                char c2 = str2.charAt(i1);
                                if (c1 > c2) {
                                    return 1;
                                }
                                if (c1 < c2) {
                                    return -1;
                                }
                            }
                            return Integer.compare(str1.length(), str2.length());
                        });
                    }
                    sb.append(py[0]);
                }
                rtn = sb.toString();
                regionPinYin.put(sin, rtn);
            }
            return rtn;
        }
    }

    private Map<String, String> getSchoolIdAndNameInES(School school) {
        String esUrl = "http://10.0.1.18:9200/vox_crm_teacher_summary_10/main/_search?";
        String esStr = "{\"query\":{\"filtered\": {\"query\":{\"bool\":{\"must\":[{\"match\":{\"analyzedSchoolName\":\"" + school.getCname() + "\"}},{\"match\":{\"countyCode\":" + school.getRegionCode() + "}}]}},\"filter\": {\"bool\": {\"must\": [ ]}}}},\"from\":0,\"size\":10000}";

        System.out.println(esStr);
        String r = HttpRequestExecutor.defaultInstance().post(esUrl).json(esStr).execute().getResponseString();
        Map<String, Object> responseMap = JsonUtils.convertJsonObjectToMap(r);
        Map<String, Object> hitInfoMap = (Map) responseMap.get("hits");
        List<Map<String, Object>> hitList = (List) hitInfoMap.get("hits");
        Map<String, String> schoolMap = new HashMap<>();
        for (Map<String, Object> map : hitList) {
            Map<String, Object> sourceMap = (Map) (map.get("_source"));
            schoolMap.put(ConversionUtils.toString(sourceMap.get("schoolId")), ConversionUtils.toString(sourceMap.get("schoolName")));
        }
        return schoolMap;
    }

    @RequestMapping(value = "updateauthstate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updateAuthState() {
        Map<String, Object> message = new HashMap<>();
        Long schoolId = requestLong("schoolId");
        Integer authenticationState = requestInteger("authenticationState");
        String schoolDesc = requestString("schoolDesc");
        if (schoolId == null || authenticationState == null || StringUtils.isBlank(schoolDesc)) {
            message.put("success", false);
            return message;
        }
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            message.put("success", false);
            return message;
        }
        // admin_log增量记录
        Map<String, Object> deltaMap = new HashMap<>();
        if (!authenticationState.equals(school.getAuthenticationState())) {
            deltaMap.put("authenticationState", school.getAuthenticationState() + "->" + authenticationState);
        }
        if (!deltaMap.isEmpty()) {
            school.setAuthenticationState(authenticationState);
            if (AuthenticationState.SUCCESS == AuthenticationState.safeParse(authenticationState)) {
                school.setAuthenticationSource(AuthenticationSource.MARKET);
            }
            try {
                MapMessage msg = deprecatedSchoolServiceClient.getRemoteReference().upsertSchoolWithDesc(school, getCurrentAdminUser().getAdminUserName(), schoolDesc);
                message.put("success", msg.isSuccess());
                String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "修改学校认证状态（" + schoolId + "）";
                //记录管理员操作日志
                addAdminLog(operation, schoolId, schoolDesc, deltaMap.toString());
            } catch (Exception ignored) {
                message.put("success", false);
            }
        } else {
            message.put("success", false);
        }
        return message;
    }

    @RequestMapping(value = "checkDictSchoolForChangeSchool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkDictSchoolForChangeSchool() {
        Long teacherId = getRequestLong("teacherId");
        Long targetSchoolId = getRequestLong("targetSchoolId");
        if (teacherId == 0L || targetSchoolId == 0L) {
            return MapMessage.errorMessage("换校时提示重点学校的参数错误");
        }
        School originalSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();

        School targetSchool = raikouSystem.loadSchool(targetSchoolId);
        if (targetSchool == null) {
            return MapMessage.errorMessage("目标学校(ID:" + targetSchoolId + ")不存在");
        }

        MapMessage result = MapMessage.successMessage();
        if (originalSchool != null) {
            if (agentOrgLoaderClient.isDictSchool(originalSchool.getId())) {
                result.add("sourceSchoolId", originalSchool.getId());
                result.add("sourceSchoolName", originalSchool.getCname());
            }
        }
        if (agentOrgLoaderClient.isDictSchool(targetSchoolId)) {
            result.add("targetSchoolId", targetSchoolId);
            result.add("targetSchoolName", targetSchool.getCname());
        }
        return result;
    }

    @RequestMapping(value = "updateschoolklxprivilege.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateSchoolKlxPrivilege() {
        Long schoolId = getRequestLong("schoolId");
        MapMessage checkMsg = checkKlxPrivilege(schoolId);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo == null || schoolExtInfo.getScanNumberDigit() == null) {
            return MapMessage.errorMessage("若要开通权限，请先设置本校填涂号位数");
        }

        schoolExtInfo.setScanMachineFlag(SafeConverter.toBoolean(getRequestBool("scanMachineFlag")));
        schoolExtInfo.setQuestionBankFlag(SafeConverter.toBoolean(getRequestBool("questionBankFlag")));
        schoolExtInfo.setBarcodeAnswerQuestionFlag(SafeConverter.toBoolean(getRequestBool("barcodeAnswerQuestionFlag")));
        schoolExtInfo.setQuestionCardFlag(SafeConverter.toBoolean(getRequestBool("questionCardFlag")));
        schoolExtInfo.setA3AnswerQuestionFlag(SafeConverter.toBoolean(getRequestBool("a3AnswerQuestionFlag")));
        schoolExtInfo.setManualAnswerQuestionFlag(SafeConverter.toBoolean(getRequestBool("manualAnswerQuestionFlag")));
        Set<Subject> subjects = Stream.of(getRequestString("subjects").split(","))
                .map(StringUtils::trim)
                .map(Subject::of).collect(Collectors.toSet());
        schoolExtInfo.updateValidSubjects(subjects);
        schoolExtServiceClient.getSchoolExtService()
                .upsertSchoolExtInfoWithOperator(schoolExtInfo, getCurrentAdminUser().getAdminUserName())
                .awaitUninterruptibly();

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "addschoolquizbankadministrator.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addSchoolQuizBankAdministrator() {
        Long schoolId = getRequestLong("schoolId");
        Long teacherId = getRequestLong("teacherId");
        if (schoolId == 0 || teacherId == 0) {
            return MapMessage.errorMessage("参数错误schoolId:" + schoolId + ",teacherId:" + teacherId);
        }

        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("无效的schoolId:" + schoolId);
        }

        TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (detail == null) {
            return MapMessage.errorMessage("无效的老师ID:" + teacherId);
        }

        if (!Objects.equals(detail.getTeacherSchoolId(), schoolId)) {
            return MapMessage.errorMessage("该老师" + teacherId + "学校id为" + detail.getTeacherSchoolId() + ",不在该" + schoolId + "学校内");
        }

        return teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(
                teacherId,
                schoolId,
                TeacherRoleCategory.O2O.name(),
                TeacherRolesType.SCHOOL_BANK_MANAGER.name(),
                "");
    }

    @RequestMapping(value = "cancelschoolquizbankadministrator.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancelSchoolQuizBankAdministrator() {
        Long schoolId = getRequestLong("schoolId");
        Long teacherId = getRequestLong("teacherId");
        if (teacherId == 0) {
            return MapMessage.errorMessage("老师id不能为空");
        }
        return teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(
                teacherId,
                schoolId,
                TeacherRoleCategory.O2O.name(),
                TeacherRolesType.SCHOOL_BANK_MANAGER.name());
    }

    @RequestMapping(value = "addExamManager.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addExamManager() {
        Long schoolId = getRequestLong("schoolId");
        Long teacherId = getRequestLong("teacherId");
        if (schoolId == 0 || teacherId == 0) {
            return MapMessage.errorMessage("参数错误 学校ID:" + schoolId + ",老师ID:" + teacherId);
        }

        User user = userLoaderClient.loadUser(teacherId);
        if (user == null) {
            return MapMessage.errorMessage("该账号" + teacherId + "不是老师或教务老师");
        }

        Long localSchoolId = null;
        if (user.fetchUserType() == UserType.TEACHER) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (teacherDetail != null) {
                localSchoolId = teacherDetail.getTeacherSchoolId();
            }
        } else if (user.fetchUserType() == UserType.RESEARCH_STAFF) {
            ResearchStaff staff = researchStaffLoaderClient.loadResearchStaff(user.getId());
            if (staff != null && staff.isAffairTeacher()) {
                ResearchStaffManagedRegion researchStaffManagedRegion = specialTeacherLoaderClient.findSchoolId(user.getId());
                if (researchStaffManagedRegion != null) {
                    localSchoolId = researchStaffManagedRegion.getManagedRegionCode();
                }
            }
        } else {
            return MapMessage.errorMessage("该账号" + teacherId + "不是老师或教务老师");
        }

        if (localSchoolId == null || !Objects.equals(localSchoolId, schoolId)) {
            return MapMessage.errorMessage("该老师" + teacherId + "学校id为" + localSchoolId + ",不在该" + schoolId + "学校内");
        }

        teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(teacherId,
                schoolId,
                TeacherRoleCategory.O2O.name(),
                TeacherRolesType.EXAM_MANAGER.name(),
                "");

        return MapMessage.successMessage().add("teacherId", user.getId())
                .add("teacherName", user.fetchRealname());
    }

    @RequestMapping(value = "cancelExamManager.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancelExamManager() {
        Long schoolId = getRequestLong("schoolId");
        Long teacherId = getRequestLong("teacherId");
        if (teacherId == 0) {
            return MapMessage.errorMessage("老师ID不能为空");
        }

        TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (detail == null) {
            return MapMessage.errorMessage("无效的老师ID!");
        }

        teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(teacherId,
                detail.getTeacherSchoolId(),
                TeacherRoleCategory.O2O.name(),
                TeacherRolesType.EXAM_MANAGER.name());

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "checkschoolscannumber.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkSchoolScanNumber() {
        Long schoolId = getRequestLong("schoolId");
        String scanNumber = getRequestString("scanNumber");
        if (schoolId == 0 || StringUtils.isBlank(scanNumber)) {
            return MapMessage.errorMessage("参数错误");
        }
        return newKuailexueLoaderClient.findScanNumberInfo(schoolId, scanNumber);
    }

    /**
     * 合并学校前的字段检查
     */
    private MapMessage doMergeSchoolPreCheck(Long sourceSchoolId, Long targetSchoolId, String mergeDesc) {
        mergeDesc = mergeDesc.replaceAll("\\s", "");
        if (StringUtils.isBlank(mergeDesc)) {
            return MapMessage.errorMessage("问题描述不能为空");
        }

        if (sourceSchoolId.equals(targetSchoolId)) {
            return MapMessage.errorMessage("被合并学校与合并学校不能相同");
        }

        School sourceSchool = raikouSystem.loadSchool(sourceSchoolId);
        if (sourceSchool == null) {
            return MapMessage.errorMessage("源学校不存在");
        }
        School targetSchool = raikouSystem.loadSchool(targetSchoolId);
        if (targetSchool == null) {
            return MapMessage.errorMessage("目标学校不存在");
        }
        if (!Objects.equals(sourceSchool.getLevel(), targetSchool.getLevel())) {
            return MapMessage.errorMessage("学校学段不同，不能合并");
        }
        // 判断是否字典表
        if (isSchoolInDict(sourceSchoolId, targetSchoolId)) {
            return MapMessage.errorMessage("该学校是字典表学校，请勿合并");
        }

        if (schoolExtServiceClient.isDaiteSchool(sourceSchoolId)) {
            return MapMessage.errorMessage("该学校是合作校，不允许合并");
        }

        SchoolExtInfo sourceSchoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(sourceSchoolId)
                .getUninterruptibly();
        SchoolExtInfo targetSchoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(targetSchoolId)
                .getUninterruptibly();
        String sourceSchoolExtInfoMessage = "", targetSchoolExtInfoMessage = "";
        if (sourceSchoolExtInfo != null) {
            sourceSchoolExtInfoMessage = "已完善信息";
        }
        if (targetSchoolExtInfo != null) {
            targetSchoolExtInfoMessage = "已完善信息";
        }

        if (targetSchool.isJuniorSchool() || targetSchool.isSeniorSchool()) {
            if (sourceSchoolExtInfo != null && targetSchoolExtInfo != null) {
                if (sourceSchoolExtInfo.isScanMachineFlag() && targetSchoolExtInfo.isScanMachineFlag()) {
                    return MapMessage.errorMessage("两所均开通了阅卷机的学校不能合并");
                }
            }
        }

        return MapMessage.successMessage()
                .add("sourceSchoolName", sourceSchool.getCname() + "(" + AuthenticationState.safeParse(sourceSchool.getAuthenticationState()).getDescription() + ")" + sourceSchoolExtInfoMessage)
                .add("targetSchoolName", targetSchool.getCname() + "(" + AuthenticationState.safeParse(targetSchool.getAuthenticationState()).getDescription() + ")" + targetSchoolExtInfoMessage);
    }

    /**
     * 判断学校是否在字典表中
     * 如果异常情况,返回true,避免被误删
     */
    private boolean isSchoolInDict(Long sourceSchoolId, Long targetSchoolId) {
        assert sourceSchoolId != null;
        assert targetSchoolId != null;
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .post(UrlUtils.buildUrlQuery(getMarketingUrl() + "/crm/validateschool.vpage", MapUtils.m("sourceSchoolId", sourceSchoolId, "targetSchoolId", targetSchoolId)))
                .execute();

        if (response.hasHttpClientException()) {
            return true;// 网络异常,返回true,以防误删
        }

        MapMessage message = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
        return message == null || !message.isSuccess();
    }

    @RequestMapping(value = "removescannumberfromschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeScanNumberFromSchool() {
        Long schoolId = getRequestLong("schoolId");
        String scanNumber = getRequestString("scanNumber");
        if (schoolId == 0 || StringUtils.isBlank(scanNumber)) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage checkMsg = checkKlxPrivilege(schoolId);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }

        return newKuailexueServiceClient.removeScanNumberFromSchool(schoolId, scanNumber);
    }

    @RequestMapping(value = "queryscannumber.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage queryScanNumber() {
        String scanNumber = getRequestString("scanNumber").trim();
        Long schoolId = getRequestLong("schoolId");
        if (StringUtils.isBlank(scanNumber) || schoolId == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();

        int scanNumberDigit = schoolExtInfo.fetchScanNumberDigit();
        if (scanNumber.length() != scanNumberDigit) {
            return MapMessage.errorMessage("该学校填涂号是" + scanNumberDigit + "位数字");
        }

        MapMessage message = newKuailexueLoaderClient.findScanNumberInfo(schoolId, scanNumber);
        if (!message.isSuccess()) {
            return message;
        }
        Set<Long> groupIds = (Set<Long>) message.get("groupIds");

        List<Map<String, Object>> groupInfos = groupLoaderClient.getGroupLoader()
                .loadGroupsIncludeDisabled(groupIds)
                .getUninterruptibly()
                .values()
                .stream()
                .map(g -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("groupId", g.getId());
                    info.put("disabled", g.isDisabledTrue());
                    return info;
                }).collect(Collectors.toList());

        return message.add("groupInfos", groupInfos);
    }


    private MapMessage changeSchoolsRegion(Collection<Long> schoolIds, Integer regionCode, ExRegionBuffer regionBuffer) {
        if (CollectionUtils.isEmpty(schoolIds) || regionCode == null) {
            return MapMessage.errorMessage();
        }
        ExRegion region = Objects.requireNonNull(regionBuffer).loadRegion(regionCode);
        if (region == null || region.fetchRegionType() != RegionType.COUNTY) {
            return MapMessage.errorMessage();
        }
        Map<Long, School> schools = raikouSystem.loadSchools(schoolIds);
        for (Long schoolId : schoolIds) {
            School school = schools.get(schoolId);
            if (school == null || school.getSchoolAuthenticationState() != AuthenticationState.SUCCESS) {
                return MapMessage.errorMessage();
            }
        }
        Collection<School> candidates = schools.values();
        try {
            return changeSchoolsRegion(candidates, region);
        } catch (Exception ex) {
            logger.error("Failed to change schools region", ex);
            return MapMessage.errorMessage();
        }
    }

    private MapMessage $deleteSchools(Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return MapMessage.errorMessage();
        }
        Collection<Long> successIds = new LinkedHashSet<>();
        Collection<Long> failureIds = new LinkedHashSet<>();
        Collection<School> candidates = new LinkedHashSet<>();
        Map<Long, School> schools = raikouSystem.loadSchools(schoolIds);
        Map<Long, List<Clazz>> schoolClazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolIds)
                .enabled()
                .toGroup(Clazz::getSchoolId);
        for (Long schoolId : schoolIds) {
            School school = schools.get(schoolId);
            if (school == null) {
                failureIds.add(schoolId);
                continue;
            }
            List<Clazz> clazzs = schoolClazzs.get(schoolId);
            if (CollectionUtils.isNotEmpty(clazzs)) {

                List<Long> clazzIds = clazzs.stream().map(Clazz::getId).collect(Collectors.toList());

                List<GroupMapper> groups = deprecatedGroupLoaderClient.loadClazzGroups(clazzIds)
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(groups)) {
                    failureIds.add(schoolId);
                    continue;
                }

                long systemClazzCount = clazzs.stream().filter(Clazz::isSystemClazz).count();
                if (systemClazzCount > 0) {// 当系统包含系统班级时，需判断学校是否仍有老师
                    // 否则，跳过该判断，可以直接删除
                    List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(schoolId);
                    if (CollectionUtils.isNotEmpty(teachers)) {
                        failureIds.add(schoolId);
                        continue;
                    }
                }
            }
            candidates.add(school);
        }
        if (CollectionUtils.isNotEmpty(candidates)) {
            try {
                MapMessage message = deprecatedSchoolServiceClient.getRemoteReference().deleteSchools(candidates);
                if (message.isSuccess()) {
                    Collection sIds = (Collection) message.get("successIds");
                    for (Object sId : sIds) {
                        successIds.add((Long) sId);
                    }
                    Collection fIds = (Collection) message.get("failureIds");
                    for (Object fId : fIds) {
                        failureIds.add((Long) fId);
                    }
                }
            } catch (Exception ex) {
                logger.error("Failed to disable schools", ex);
                return MapMessage.errorMessage();
            }
        }
        return MapMessage.successMessage().add("successIds", successIds).add("failureIds", failureIds);
    }

    private MapMessage changeSchoolsRegion(Collection<School> schools, Region region) {
        AlpsFutureBuilder.<Long, Boolean>newBuilder()
                .ids(schools.stream().map(School::getId).collect(Collectors.toSet()))
                .generator(schoolId -> schoolServiceClient.getSchoolService().changeSchoolRegion(schoolId, region.getCode()))
                .buildList()
                .awaitUninterruptibly();
        return MapMessage.successMessage();
    }

    private Map<String, String> getSchoolSimilarityInfo(String nameUgc, SchoolLevel schoolLevel, Integer regionCode) {
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        String cityName = exRegion.getCityName();

        Map<String, Long> schoolNameMap = new LinkedHashMap<>();
        List<ExRegion> exRegions = raikouSystem.getRegionBuffer().loadChildRegions(exRegion.getCityCode());//本城市下所有地区
        exRegions.forEach(tempExRegion -> {
            schoolLoaderClient.getSchoolLoader()
                    .loadSchools(schoolLoaderClient.getSchoolLoader()
                            .querySchoolLocations(tempExRegion.getCountyCode())
                            .getUninterruptibly()
                            .stream()
                            .filter(e -> !e.isDisabled())
                            .filter(e -> e.match(schoolLevel))
                            .map(School.Location::getId)
                            .collect(Collectors.toSet()))
                    .getUninterruptibly()
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(School::getId))
                    .forEach(tempSchool -> {
                        if (!schoolNameMap.containsKey(tempSchool.getCmainName())) {
                            schoolNameMap.put(tempSchool.getCmainName(), tempSchool.getId());
                        }
                    });
        });

        int limit = 5;
        Double similarValue = CrmSimilarSchoolServiceClient.SIMILAR_VALUE;
        Map<String, String> existSchoolMap = new LinkedHashMap<>();
        List<String> schoolNameSysList = new ArrayList<>(schoolNameMap.keySet());
        Map<String, Double> similarityMap;
        int blockSize = 400;
        if (schoolNameSysList.size() > blockSize) {
            int beginIndex = 0;
            int endIndex = blockSize;
            similarityMap = new LinkedHashMap<>();
            while (beginIndex < schoolNameSysList.size()) {
                Map<String, Double> tempMap = crmSimilarSchoolServiceClient.getSchoolNameSimilarity(nameUgc, schoolNameSysList.subList(beginIndex, endIndex), cityName, schoolLevel, limit, similarValue);
                similarityMap.putAll(tempMap);
                beginIndex = endIndex;
                endIndex = Integer.min(endIndex + blockSize, schoolNameSysList.size());
            }
            similarityMap = similarityMap.entrySet().stream().sorted((l1, l2) -> BigDecimal.valueOf(l2.getValue()).compareTo(BigDecimal.valueOf(l1.getValue())))
                    .limit(limit).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            similarityMap = crmSimilarSchoolServiceClient.getSchoolNameSimilarity(nameUgc, schoolNameSysList, cityName, schoolLevel, limit, similarValue);
        }

        similarityMap.keySet().stream().filter(schoolNameMap::containsKey).forEach(schoolName -> {
            existSchoolMap.put(schoolNameMap.get(schoolName).toString(), schoolName);
        });
        return existSchoolMap;
    }

    private MapMessage checkKlxPrivilege(Long schoolId) {
        if (schoolId == 0) {
            return MapMessage.errorMessage("学校不存在");
        }
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校" + schoolId + "不存在");
        }

        if (!school.isJuniorSchool() && !school.isSeniorSchool()) {
            return MapMessage.errorMessage("暂时只支持初高中开通快乐学学校权限");
        }
        String key = "crm_school_scan_privilege";
        String allowedAccount;
        try {
            allowedAccount = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), key);
        } catch (Exception e) {
            logger.error("阅卷机权限通用配置设置有误crm_school_scan_privilege");
            return MapMessage.errorMessage("阅卷机权限通用配置设置有误");
        }
        if (StringUtils.isBlank(allowedAccount)) {
            return MapMessage.errorMessage("对不起，您没有操作权限");
        }
        String adminUserName = getCurrentAdminUser().getAdminUserName();
        if (Stream.of(allowedAccount.split(",")).noneMatch(account -> StringUtils.equals(account, adminUserName))) {
            return MapMessage.errorMessage("对不起，您没有操作权限");
        }
        return MapMessage.successMessage();
    }

    private Map<String, Object> mapTeacherInfo(Long teacherId) {
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return null;
        }
        Map<String, Object> teacherInfo = new HashMap<>();
        teacherInfo.put("teacherId", teacherId);
        TeacherExtAttribute attribute = teacherLoaderClient.loadTeacherExtAttribute(teacherId);
        CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacherId);
        teacherInfo.put("teacherName", (teacher.getProfile() == null) ? null : teacher.getProfile().getRealname());
        teacherInfo.put("subject", (teacher.getSubject() == null) ? null : teacher.getSubject().getValue());
        // 老师认证状态
        teacherInfo.put("authenticationState", teacher.getAuthenticationState());
        // 老师判假状态
        teacherInfo.put("fakeTeacher", isFake(teacherSummary, attribute));
        teacherInfo.put("fakeType", isFake(teacherSummary, attribute));
        return teacherInfo;
    }

    private boolean isFake(CrmTeacherSummary summary, TeacherExtAttribute attribute) {
        if (attribute != null && attribute.isFakeTeacher()) {
            return true;
        }
        return summary != null && !summary.isNotManualFakeTeacher();
    }

}
