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
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.athena.api.UctUserService;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import com.voxlearning.utopia.admin.dao.feedback.UserFeedbackPersistence;
import com.voxlearning.utopia.admin.data.ParentRewardMapper;
import com.voxlearning.utopia.admin.service.crm.CrmRewardService;
import com.voxlearning.utopia.admin.service.crm.CrmTaskService;
import com.voxlearning.utopia.admin.service.crm.CrmUserService;
import com.voxlearning.utopia.admin.service.legacy.CrmRefundService;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.RecordType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.mapper.DisplayStudentHomeWorkHistoryMapper;
import com.voxlearning.utopia.service.action.api.document.Achievement;
import com.voxlearning.utopia.service.action.api.document.AchievementBuilder;
import com.voxlearning.utopia.service.action.api.document.UserAchievementRecord;
import com.voxlearning.utopia.service.action.api.document.UserGrowth;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.campaign.client.StudentActivityServiceClient;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.integral.api.entities.Credit;
import com.voxlearning.utopia.service.integral.api.entities.CreditHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.client.CreditLoaderClient;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.VacationReportForParent;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.crm.PackageHomeworkDetail;
import com.voxlearning.utopia.service.newhomework.consumer.BasicReviewHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.parent.api.DPScoreCircleLoader;
import com.voxlearning.utopia.service.parent.api.DPScoreCircleService;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContext;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContextStoreInfo;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleProcessResult;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleResponse;
import com.voxlearning.utopia.service.parent.constant.GroupCircleType;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardBufferLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardItemType;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardCategory;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardItem;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardLog;
import com.voxlearning.utopia.service.parentreward.api.entity.StudentRewardSendCount;
import com.voxlearning.utopia.service.parentreward.api.mapper.ParentRewardSendResult;
import com.voxlearning.utopia.service.parentreward.helper.ParentRewardHelper;
import com.voxlearning.utopia.service.piclisten.api.GrindEarService;
import com.voxlearning.utopia.service.region.api.constant.RegionConstants;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.mapper.RewardOrderMapper;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.AccountStatus;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.constants.RefStatus;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.service.financial.Finance;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.client.UserAuthQueryServiceClient;
import com.voxlearning.utopia.service.user.client.athena.UctUserServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.AppParentSignRecord;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRecord;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.temp.GrindEarActivity;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Longlong Yu
 * @since 下午5:46,13-6-25.
 */
@Controller
@RequestMapping("/crm/student")
public class CrmStudentController extends CrmAbstractController {

    @Inject
    private RaikouSDK raikouSDK;
    @Inject
    private RaikouSystem raikouSystem;

    @Inject
    private ActionLoaderClient actionLoaderClient;
    @Inject
    private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject
    private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;
    @Inject
    private AsyncStudentServiceClient asyncStudentServiceClient;
    @Inject
    private BasicReviewHomeworkReportLoaderClient basicReviewHomeworkReportLoaderClient;
    @Inject
    private CouponLoaderClient couponLoaderClient;
    @Inject
    private CreditLoaderClient creditLoaderClient;
    @Inject
    private CrmRefundService crmRefundService;
    @Inject
    private CrmTaskService crmTaskService;
    @Inject
    private FinanceServiceClient financeServiceClient;
    @Inject
    private GlobalTagServiceClient globalTagServiceClient;
    @Inject
    private GroupLoaderClient groupLoaderClient;
    @Inject
    private IntegralLoaderClient integralLoaderClient;
    @Inject
    private NewClazzBookLoaderClient newClazzBookLoaderClient;
    @Inject
    private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject
    private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject
    private ParentRewardBufferLoaderClient parentRewardBufferLoaderClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private StudentActivityServiceClient studentActivityServiceClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private StudentServiceClient studentServiceClient;
    @Inject
    private UctUserServiceClient uctUserServiceClient;
    @Inject
    private UserAuthQueryServiceClient userAuthQueryServiceClient;
    @Inject
    private UserFeedbackPersistence userFeedbackPersistence;
    @Inject
    private UserServiceClient userServiceClient;
    @Inject
    private CrmRewardService crmRewardService;
    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;


    @Getter
    @ImportService(interfaceClass = UctUserService.class)
    private UctUserService uctUserService;
    @ImportService(interfaceClass = GrindEarService.class)
    private GrindEarService grindEarService;
    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;
    @ImportService(interfaceClass = DPScoreCircleLoader.class)
    private DPScoreCircleLoader dpScoreCircleLoader;
    @ImportService(interfaceClass = DPScoreCircleService.class)
    private DPScoreCircleService dpScoreCircleService;

    private static final int MAX_STUDENT_AMOUNT = 20;

    /**
     * ***********************查询相关*****************************************************************
     */
    @RequestMapping(value = "studentlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String studentList(Model model) {

        String[] conditionKeys = new String[]{"studentId", "userMobile", "regionId", "schoolId", "studentName"};

        Map<String, String> conditionMap = new HashMap<>();
        for (String key : conditionKeys) {
            String value = getRequestParameter(key, "");
            //手机号搜索框支持 xxx-xxxx-xxxx 的这种344格式直接搜索
            if (key.equals("userMobile")) {
                value = value.replaceAll("-", "");
            }
            if (StringUtils.isNotBlank(value)) {
                //特殊字符过滤
                value = StringRegexUtils.normalizeCZ(value);
            }
            conditionMap.put(key, value);
        }

        List<Long> studentIdList = getStudentIdList(conditionMap);
        if (!CollectionUtils.isEmpty(studentIdList)) {
            studentIdList = studentIdList.stream().collect(Collectors.toSet()).stream().collect(Collectors.toList());
        }
        List<Map<String, Object>> studentList = getStudentSnapshot(studentIdList, conditionMap.get("teacherName"));

        if (CollectionUtils.isEmpty(studentList)) {
            if (isRequestPost())
                getAlertMessageManager().addMessageError("用户不存在或者用户不是学生用户。");
        } else if (studentList.size() == MAX_STUDENT_AMOUNT)
            getAlertMessageManager().addMessageError("若结果中未找到正确用户，请尝试缩小查找范围。");

        model.addAttribute("studentList", studentList);
        model.addAttribute("conditionMap", conditionMap);

        return "crm/student/studentlist";
    }

    @RequestMapping(value = "searchklxstudent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchKlxstudent() {
        String klxId = getRequestString("klxId");
        klxId = klxId.trim();

        if (StringUtils.isBlank(klxId)) {
            return MapMessage.errorMessage("请输入正确的快乐学ID");
        }

        Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(Collections.singleton(klxId));
        KlxStudent klxStudent = klxStudentMap.get(klxId);

        if (klxStudent == null) {
            return MapMessage.errorMessage("未找到该学生，快乐学ID:" + klxId);
        }

        Map<String, Object> studentMap = new HashMap<>();

        if (klxStudent.getA17id() != null && klxStudent.getA17id() != 0) { // klxId关联了17Id
            Student student = studentLoaderClient.loadStudent(klxStudent.getA17id());

            studentMap.put("studentId", student.getId());
            if (student.getProfile() != null) {
                studentMap.put("studentName", student.getProfile().getRealname());
            }

            School school = asyncStudentServiceClient.getAsyncStudentService()
                    .loadStudentSchool(student.getId())
                    .getUninterruptibly();
            if (school != null) {
                studentMap.put("schoolId", school.getId());
                studentMap.put("schoolName", school.getCname());
            }

            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(student.getId());
            if (clazz != null) {
                ClazzTeacher ct = teacherLoaderClient.loadClazzCreator(clazz.getId());
                Teacher teacher = ct == null ? null : ct.getTeacher();
                studentMap.put("classLevel", clazz.getClassLevel());
                studentMap.put("className", clazz.getClassName());
                studentMap.put("classId", clazz.getId());
                if (teacher != null) {
                    UserAuthentication teacherUa = userLoaderClient.loadUserAuthentication(teacher.getId());
                    studentMap.put("teacherId", teacher.getId());
                    studentMap.put("teacherName", teacher.getProfile().getRealname());
                    studentMap.put("teacherMobile", teacherUa.getSensitiveMobile());
                }
            }
        } else {
            studentMap.put("studentId", null);
            studentMap.put("studentName", klxStudent.getName());
            GroupKlxStudentRef groupKlxStudentRef = asyncGroupServiceClient.getAsyncGroupService()
                    .findGroupKlxStudentRefsByStudent(klxId)
                    .getUninterruptibly()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (groupKlxStudentRef != null) {
                GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupKlxStudentRef.getGroupId(), false);
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(groupMapper.getClazzId());

                if (clazz != null) {
                    studentMap.put("classLevel", clazz.getClassLevel());
                    studentMap.put("className", clazz.getClassName());
                    studentMap.put("classId", clazz.getId());

                    School school = schoolLoaderClient.getSchoolLoader().loadSchool(clazz.getSchoolId()).getUninterruptibly();
                    if (school != null) {
                        studentMap.put("schoolId", school.getId());
                        studentMap.put("schoolName", school.getCname());
                    }

                    ClazzTeacher ct = teacherLoaderClient.loadClazzCreator(clazz.getId());
                    Teacher teacher = ct == null ? null : ct.getTeacher();
                    if (teacher != null) {
                        UserAuthentication teacherUa = userLoaderClient.loadUserAuthentication(teacher.getId());
                        studentMap.put("teacherId", teacher.getId());
                        studentMap.put("teacherName", teacher.getProfile().getRealname());
                        studentMap.put("teacherMobile", teacherUa.getSensitiveMobile());
                    }
                }
            }

        }

        return MapMessage.successMessage().add("studentMap", studentMap);
    }

    @RequestMapping(value = "searchregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchRegion(@RequestParam String regionName, @RequestParam Integer limit) {
        Stream<ExRegion> stream = raikouSystem.getRegionBuffer()
                .loadAllRegions()
                .values().stream()
                .filter(e -> e.getName().contains(regionName))
                .sorted((o1, o2) -> Integer.compare(o1.getCode(), o2.getCode()));
        if (SafeConverter.toInt(limit) > 0) {
            stream = stream.limit(SafeConverter.toInt(limit));
        }
        // FIXME: 蛋疼，不知道为什么要返回List<Map>到前端
        List<Map<String, Object>> regions = stream.map(e -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("CODE", e.getCode());
            map.put("PCODE", e.getPcode());
            map.put("DISABLED", e.getDisabled());
            map.put("CREATE_DATETIME", e.getCreateDatetime());
            map.put("UPDATE_DATETIME", e.getUpdateDatetime());
            map.put("NAME", (e.getProvinceName() + e.getCityName() + e.getCountyName().replace("直辖", "")));
            return map;
        }).collect(Collectors.toList());
        return MapMessage.successMessage().add("regions", regions);
    }

    @RequestMapping(value = "searchschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchSchool(@RequestParam String schoolName, @RequestParam Integer regionCode,
                                   @RequestParam Integer limit) {

        schoolName = "%" + schoolName + "%";
        Map<String, Object> queryParamsMap = new HashMap<>();
        queryParamsMap.put("schoolName", schoolName);
        String sql = "SELECT ID, CNAME FROM VOX_SCHOOL " +
                "WHERE CNAME like :schoolName AND DISABLED=FALSE";
        if (regionCode != null && regionCode > 0) {
            List<Integer> codes = getAllChildrenRegionCodesByPcode(regionCode);
            codes.add(regionCode);
            sql += " AND REGION_CODE IN(:codes)";
            queryParamsMap.put("codes", codes);
        }
        if (limit > 0) {
            sql += " limit :limit";
            queryParamsMap.put("limit", limit);
        }

        final String querySql = sql;
        List<Map<String, Object>> schools = utopiaSql.withRoutingPolicy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave, () -> utopiaSql.withSql(querySql).useParams(queryParamsMap).queryAll());

        return MapMessage.successMessage().add("schools", schools);
    }

    @RequestMapping(value = "studenthomepage.vpage", method = RequestMethod.GET)
    public String studentHomepage(Model model) {

        Long studentId;

        try {
            studentId = Long.parseLong(getRequestParameter("studentId", ""));
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("用户ID " + getRequestParameter(" studentId", "") + " 不合规范。");
            return redirect("/crm/student/studentlist.vpage");
        }

        Map<String, Object> studentInfoMap = getStudentInfoMap(studentId);

        if (MapUtils.isEmpty(studentInfoMap)) {
            getAlertMessageManager().addMessageError("用户(ID:" + studentId + ")不存在或者用户(ID:" + studentId + ")不是学生用户。");
            return redirect("/crm/student/studentlist.vpage");
        }
        List<KeyValuePair<Integer, String>> recordTypeList = RecordType.toKeyValuePairs();
        User student = userLoaderClient.loadUser(studentId);
        model.addAttribute("student", student);
        model.addAttribute("recordTypeList", recordTypeList);

        model.addAttribute("studentInfoAdminMapper", studentInfoMap);

        List<GroupStudentTuple> allGroupStudentTuples = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByStudentIdIncludeDisabled(studentId)
                .getUninterruptibly();

        Collection<Long> allGroupIds = allGroupStudentTuples.stream()
                .map(GroupStudentTuple::getGroupId).collect(Collectors.toSet());
        Map<Long, Group> allGroups = groupLoaderClient.getGroupLoader()
                .loadGroupsIncludeDisabled(allGroupIds)
                .getUninterruptibly();

        Collection<Long> allClazzIds = allGroups.values().stream()
                .map(Group::getClazzId).collect(Collectors.toSet());
        Map<Long, Clazz> allClazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzsIncludeDisabled(allClazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        List<Map<String, Object>> exClass = new LinkedList<>();
        for (GroupStudentTuple tuple : allGroupStudentTuples) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("CLASS_NAME", null);
            map.put("CLAZZ_ID", null);
            map.put("GROUP_ID", null);
            map.put("USER_ID", null);
            map.put("UPDATETIME", null);
            map.put("DISABLED", null);
            map.put("FULL_NAME", null);

            Group group = allGroups.get(tuple.getGroupId());
            if (group != null) {
                Clazz clazz = allClazzs.get(group.getClazzId());
                if (clazz != null) {
                    map.put("CLASS_NAME", clazz.getClassName());
                    map.put("CLAZZ_ID", clazz.getId());
                    map.put("FULL_NAME", clazz.formalizeClazzName());
                }
                map.put("GROUP_ID", group.getId());
            }
            map.put("USER_ID", tuple.getStudentId());
            map.put("UPDATETIME", tuple.getUpdateTime());
            map.put("DISABLED", tuple.getDisabled());
            exClass.add(map);
        }

        exClass.sort((o1, o2) -> {
            Date d1 = (Date) o1.get("UPDATETIME");
            long u1 = (d1 == null ? 0 : d1.getTime());
            Date d2 = (Date) o2.get("UPDATETIME");
            long u2 = (d2 == null ? 0 : d2.getTime());
            return Long.compare(u2, u1);
        });

        model.addAttribute("exClass", exClass);


/* ================================================================================================
   原来的代码是这样的
   ================================================================================================
    String studentExClazzSql = "SELECT t3.CLASS_NAME, t3.ID AS CLAZZ_ID, t2.ID as GROUP_ID, t1.STUDENT_ID as USER_ID, t1.UPDATE_DATETIME AS UPDATETIME, t1.DISABLED " +
        "from VOX_GROUP_STUDENT_REF t1, VOX_CLAZZ_GROUP t2, VOX_CLASS t3 " +
        "WHERE t1.STUDENT_ID=? and t1.CLAZZ_GROUP_ID = t2.ID and t2.CLAZZ_ID = t3.ID " +
        "ORDER BY t1.UPDATE_DATETIME DESC";

    List<Map<String, Object>> exClass = utopiaSql.withSql(studentExClazzSql).useParamsArgs(studentId).queryAll();
    if (null == exClass) {
      exClass = Collections.emptyList();
    }

    Iterator<Map<String, Object>> iter = exClass.iterator();
    Map<Long, Clazz> clazzMap = new HashMap<>();
    while (iter.hasNext()) {
      Map<String, Object> row = iter.next();
      Long clazzId = SafeConverter.toLong(row.get("CLAZZ_ID"));
      Clazz clazz = null;
      if (!clazzMap.containsKey(clazzId)) {
        clazz = clazzLoaderClient.getClazzLoader().loadClazz(clazzId).getUninterruptibly();
        clazzMap.put(clazzId, clazz);
      } else {
        clazz = clazzMap.get(clazzId);
      }
      row.put("FULL_NAME", clazz == null ? null : clazz.formalizeClazzName());
    }

    model.addAttribute("exClass", exClass);
   ================================================================================================ */

        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        Set<Long> parentIds = studentParents.stream().filter(p -> p.getParentUser() != null).map(p -> p.getParentUser().getId()).collect(Collectors.toSet());
        Map<Long, List<UserWechatRef>> dataMap = wechatLoaderClient.loadUserWechatRefs(parentIds, WechatType.PARENT);
        List<Map<String, Object>> wechats = new ArrayList<>();
        if (MapUtils.isNotEmpty(dataMap)) {
            for (List<UserWechatRef> userWechatRefList : dataMap.values()) {
                Collections.sort(userWechatRefList, (o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime()));
                for (UserWechatRef uwr : userWechatRefList) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("USER_ID", uwr.getUserId());
                    item.put("OPEN_ID", uwr.getOpenId());
                    item.put("DISABLED", uwr.getDisabled());
                    item.put("CREATE_DATETIME", uwr.getCreateDatetime());
                    item.put("UPDATE_DATETIME", uwr.getUpdateDatetime());
                    item.put("SOURCE", uwr.getSource());
                    wechats.add(item);
                }
            }
        }
        model.addAttribute("wechats", wechats);

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        model.addAttribute("taskUsers", crmTaskService.taskUsers(adminUser));
        model.addAttribute("taskTypes", CrmTaskService.taskTypes(adminUser));
        model.addAttribute("recordCategoryJson", CrmTaskService.studentTaskRecordCategoryJson());
        model.addAttribute("contactTypes", CrmTaskService.contactTypes(adminUser));
        model.addAttribute("isPhoneOut", CrmTaskService.isPhoneOut(adminUser));

        model.addAttribute("ms_crm_admin_url", juniorCrmAdminUrlBase());
        model.addAttribute("studentId", studentId);

        if (studentLoaderClient.isStudentForbidden(studentId)) {
            model.addAttribute("stuAccountStatus", AccountStatus.FORBIDDEN.toString());
        } else if (studentLoaderClient.isStudentFreezing(studentId)) {
            model.addAttribute("stuAccountStatus", AccountStatus.FREEZING.toString());
        } else {
            model.addAttribute("stuAccountStatus", AccountStatus.NORMAL.toString());
        }

        UserGrowth userGrowth = actionLoaderClient.getRemoteReference().loadUserGrowth(studentId);
        model.addAttribute("growth", null == userGrowth ? 0 : userGrowth.getGrowthValue());
        model.addAttribute("growthLevel", null == userGrowth ? 1 : userGrowth.toLevel());
        int achievementCount = 0;

        List<UserAchievementRecord> uarList = actionLoaderClient.getRemoteReference().loadUserAchievementRecords(studentId);
        if (CollectionUtils.isNotEmpty(uarList)) {
            for (UserAchievementRecord uar : uarList) {
                Achievement achievement = AchievementBuilder.build(uar);
                if (achievement != null && achievement.getType() != null && achievement.getRank() > 0) {
                    achievementCount += achievement.getRank();
                }
            }
        }

        model.addAttribute("achievements", achievementCount);

        Credit credit = creditLoaderClient.getCreditLoader().loadCredit(studentId);
        List<CreditHistory> creditHistories = creditLoaderClient.getCreditLoader().loadCreditHistories(studentId);
        Integer creditListSize = getRequestInt("creditListSize", 20);
        if (creditHistories.size() > creditListSize) {
            creditHistories = creditHistories.subList(0, creditListSize);
        }
        model.addAttribute("credit", credit);
        model.addAttribute("creditHistories", creditHistories);

        // 17ID关联的快乐学账号列表
        List<KlxStudent> klxStudents = newKuailexueLoaderClient.loadKlxStudentsBy17Id(studentId);
        model.addAttribute("klxStudents", klxStudents);

        //家长是否关闭奖品中心
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        if (studentExtAttribute != null && studentExtAttribute.getCloseIntegralFairyland() != null
                && studentExtAttribute.getCloseIntegralFairyland()) {
            model.addAttribute("closeIntegral", true);
        } else {
            model.addAttribute("closeIntegral", false);
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        //是否允许兑换实物   如果为 true 代表不开放
        boolean offlineShiwu = grayFunctionManagerClient.getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(studentDetail, "Reward", "OfflineShiWu", true);
        //是否开放奖品中心   如果为 true 不允许兑换
        boolean closeSchool = grayFunctionManagerClient.getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(studentDetail, "Reward", "CloseSchool");

        model.addAttribute("offlineShiWu", offlineShiwu);
        model.addAttribute("closeSchool", closeSchool);

        return "crm/student/studenthomepage";
    }

    @RequestMapping(value = "studentrewardorder.vpage", method = RequestMethod.GET)
    public String studentrewardorder(Model model) {
        Long studentId;
        try {
            studentId = Long.parseLong(getRequestParameter("studentId", ""));
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("用户ID " + getRequestParameter(" studentId", "") + " 不合规范。");
            return redirect("/crm/student/studentlist.vpage");
        }
        try {
            Map<String, String> param = new HashMap<>();
            param.put("uid", studentId.toString());
            String URL = UrlUtils.buildUrlQuery(ProductConfig.getRewardSiteBaseUrl() + "/open/order_list", param);
            String orderinfo = HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString();
            model.addAttribute("orderinfo", orderinfo);
        } catch (Exception ex) {
            logger.warn("读取用户兑换信息失败", ex);
        }
        return "/crm/student/studentrewardorder";
    }

    @RequestMapping(value = "studentnewrewardorder.vpage", method = RequestMethod.GET)
    public String studentNewRewardOrder(Model model) {
        Long studentId;
        try {
            studentId = Long.parseLong(getRequestParameter("studentId", ""));
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("用户ID " + getRequestParameter("studentId", "") + " 不合规范。");
            return redirect("/crm/student/studentlist.vpage");
        }
        try {
            List<RewardOrderMapper> orders = crmRewardService.generateUserRewardOrderMapper(studentId);
            model.addAttribute("orders", orders);
            model.addAttribute("orderStatus", RewardOrderStatus.values());
        } catch (Exception ex) {
            logger.warn("读取用户兑换信息失败", ex);
        }
        return "/crm/student/studentnewrewardorder";
    }

    @RequestMapping(value = "userfeedback.vpage", method = RequestMethod.GET)
    public String userFeedback(@RequestParam Long userId, Model model) {
        List<UserFeedback> userFeedbackList = userFeedbackPersistence.findByUserId(userId);
        List<Map<String, Object>> userFeedbackInfoList = new ArrayList<>();
        for (UserFeedback userFeedback : userFeedbackList) {
//        userFeedbackList.each { UserFeedback userFeedback ->
            Map<String, Object> userFeedbackInfo = new HashMap<>();
            userFeedbackInfo.put("createDatetime", userFeedback.getCreateDatetime());
            userFeedbackInfo.put("updateDatetime", userFeedback.getUpdateDatetime());
            userFeedbackInfo.put("content", userFeedback.getContent());
            userFeedbackInfo.put("tagFeedbackType", userFeedback.getFeedbackType());
            userFeedbackInfo.put("practiceType", userFeedback.getPracticeName());
            userFeedbackInfo.put("state", CrmUserService.feedbackStateMap.get(userFeedback.getState().toString()));
            userFeedbackInfo.put("reply", userFeedback.getReply());
            userFeedbackInfo.put("id", userFeedback.getId());
            userFeedbackInfo.put("userId", userFeedback.getUserId());
            userFeedbackInfoList.add(userFeedbackInfo);
        }

        model.addAttribute("userId", userId);
        User user = userLoaderClient.loadUser(userId);
        String userName = (user == null || user.getProfile() == null) ? null : user.getProfile().getRealname();
        model.addAttribute("userName", userName);
        model.addAttribute("userFeedbackInfoList", userFeedbackInfoList);
        model.addAttribute("feedbackQuickReplyList", getFeedbackQuickReplyList());
        return "crm/user/userfeedback";
    }

//    @RequestMapping(value = "studentinviteelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
//    public String studentInviteeList(Model model) {
//        long studentId = getRequestLong("studentId");
//        //查询邀请历史
//        String queryInvitee = "select ih.INVITE_ID as inviteeId, ih.CREATETIME as createTime, " +
//                " ih.UPDATETIME as updateTime, ih.DISABLED as success, uu.REALNAME as inviteeName " +
//                " from VOX_INVITE_HISTORY ih " +
//                " inner join UCT_USER uu on uu.ID = ih.INVITE_ID and uu.DISABLED = 0 " +
//                " where ih.USER_ID=?";
//        List<Map<String, Object>> inviteHistoryList = utopiaSql.withSql(queryInvitee).useParamsArgs(studentId).queryAll();
//
//        model.addAttribute("inviteHistoryList", inviteHistoryList);
//        model.addAttribute("studentId", studentId);
//        User user = userLoaderClient.loadUser(studentId, UserType.STUDENT);
//        model.addAttribute("studentName", (user == null || user.getProfile() == null) ? null : user.getProfile().getRealname());
//        return "crm/student/studentinviteelist";
//    }

    /**
     * 查询是否在15天内改过用户密码
     */
    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "checkchangedpassword.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage checkChangedPassword() {

        long studentId = getRequestLong("studentId");
        if (studentId > 0) {
            Date startDate = DateUtils.nextDay(new Date(), -15);
            List<UserServiceRecord> recordList = userLoaderClient.loadUserServiceRecords(studentId);
            UserServiceRecord record = null;
            if (CollectionUtils.isNotEmpty(recordList)) {
                record = recordList.stream().filter(r -> r.getCreateTime().after(startDate))
                        .filter(r -> Objects.equals("重置密码", r.getOperationContent()))
                        .findAny().orElse(null);
            }
            return MapMessage.successMessage().setSuccess(record != null);
        } else {
            return MapMessage.errorMessage();
        }
    }

    /**
     * 更新学生端的sessionKey,让学生重新登录
     */
    @AdminAcceptRoles(postRoles = {AdminPageRole.POST_ACCESSOR})
    @RequestMapping(value = "kickOutOfApp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage kickOutOfApp() {

        long userId = getRequestLong("userId");
        if (userId > 0) {
            updateUserAppSessionKey(userId, "17Student");
            updateUserAppSessionKey(userId, "17JuniorStu");
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("userId???");
        }
    }

    /**
     * 查询是否绑定手机或设置密保
     */
    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "checkbindingphoneorsetsq.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage checkBindingPhoneOrSetSq() {

        long studentId = getRequestLong("studentId");
        if (studentId > 0) {
            MapMessage mapMessage = new MapMessage();
            List<String> info = new ArrayList<>();
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(studentId);
            if (userAuthentication.isMobileAuthenticated()) {
                String phone = sensitiveUserDataServiceClient.showUserMobile(studentId, "checkMobileAuthenticated", SafeConverter.toString(studentId));
                mapMessage.set("mobile", phone == null ? null : phone);
                info.add("绑定手机");
            }
            mapMessage.setSuccess(info.size() > 0);
            mapMessage.setInfo("该生已经" + StringUtils.join(info, "并已") + ",请确认是否继续？");
            return mapMessage;
        } else {
            return MapMessage.errorMessage();
        }
    }

    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "setstudentpayfree.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage setStudentPayFree() {
        long studentId = getRequestLong("studentId");
        if (studentId > 0) {
            asyncOrderCacheServiceClient.getAsyncOrderCacheService().StudentPayFreeCacheManager_set(studentId).awaitUninterruptibly();
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }
    }

    /**
     * 查询用户是否有付费产品
     */
    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "haspaidproduct.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage hasPaidProduct() {
        Long studentId = requestLong("studentId");
        Set<OrderProductServiceType> products = userOrderLoaderClient.loadUserOrderList(studentId).stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                .map(o -> OrderProductServiceType.safeParse(o.getOrderProductServiceType())).collect(Collectors.toSet());
        return CollectionUtils.isEmpty(products) ? MapMessage.errorMessage() : MapMessage.successMessage();
    }


    @RequestMapping(value = "studentbasichomeworkdetail.vpage", method = RequestMethod.GET)
    public String crmPackageHomeworkDetail(@RequestParam("userId") Long userId, Model model) {
        String path = "crm/student/studentbasichomeworkdetail";
        model.addAttribute("userId", userId);
        Map<Subject, PackageHomeworkDetail> subjectPackageHomeworkDetailMap = basicReviewHomeworkReportLoaderClient.crmPackageHomeworkDetail(userId);
        if (MapUtils.isEmpty(subjectPackageHomeworkDetailMap)) {
            model.addAttribute("success", false);
            model.addAttribute("info", "学生没有基础必过练习");
            return path;
        }
        model.addAttribute("success", true);
        if (subjectPackageHomeworkDetailMap.containsKey(Subject.MATH)) {
            model.addAttribute("mathPackage", subjectPackageHomeworkDetailMap.get(Subject.MATH));
        }
        if (subjectPackageHomeworkDetailMap.containsKey(Subject.ENGLISH)) {
            model.addAttribute("englishPackage", subjectPackageHomeworkDetailMap.get(Subject.ENGLISH));
        }
        if (subjectPackageHomeworkDetailMap.containsKey(Subject.CHINESE)) {
            model.addAttribute("chinesePackage", subjectPackageHomeworkDetailMap.get(Subject.CHINESE));
        }
        return path;

    }


    /**
     * ***********************作业相关****************************************************************
     */
    private static final int DURATION_DAY = 10;

    @RequestMapping(value = "studenthomeworkdetail.vpage", method = RequestMethod.GET)
    public String studentHomeworkDetail(@RequestParam("userId") Long userId, Model model) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);

        String startTime = this.getRequestString("startTime");
        String endTime = this.getRequestString("endTime");
        //判断是否有参数传入，没有的时候null
        Date currentDate = StringUtils.isNotBlank(endTime) ? DateUtils.stringToDate(endTime, DateUtils.FORMAT_SQL_DATE) : null;
        Date startDate = StringUtils.isNotBlank(startTime) ? DateUtils.stringToDate(startTime, DateUtils.FORMAT_SQL_DATE) : null;


        if (studentDetail == null) {
            return "crm/student/studenthomeworkdetail_middleschool";
        }
        if (currentDate == null || startDate == null) {
            currentDate = new Date();
            startDate = DateUtils.calculateDateDay(currentDate, -DURATION_DAY);
        }
        String time = DateUtils.dateToString(startDate, DateUtils.FORMAT_SQL_DATE) + "--" + DateUtils.dateToString(currentDate, DateUtils.FORMAT_SQL_DATE);

        //-30指最近一个月时间的作业
        List<DisplayStudentHomeWorkHistoryMapper> homeworks = newHomeworkCrmLoaderClient.crmLoadStudentNewHomeworkHistory(studentDetail, startDate, currentDate);
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(userId);
        User user = userLoaderClient.loadUser(userId);
        List<DisplayStudentHomeWorkHistoryMapper> englishList = new ArrayList<>();
        List<DisplayStudentHomeWorkHistoryMapper> mathhList = new ArrayList<>();
        List<DisplayStudentHomeWorkHistoryMapper> chinessList = new ArrayList<>();
        for (DisplayStudentHomeWorkHistoryMapper obj : homeworks) {
            if (user != null) {
                obj.setClazzId(clazz.getId());
                obj.setClazzName(clazz.getClassName());
                obj.setUserId(userId);
                if (user.getProfile() != null)
                    obj.setUserName(user.getProfile().getRealname());
            }
            switch (obj.getSubject()) {
                case ENGLISH:
                    englishList.add(obj);
                    break;
                case MATH:
                    mathhList.add(obj);
                    break;
                case CHINESE:
                    chinessList.add(obj);
                    break;
                default:
                    break;
            }
        }

        model.addAttribute("studentHomeworkHistoryList", englishList);
        model.addAttribute("mathHomeWorkHistoryMapperList", mathhList);
        model.addAttribute("chineseHomeworkHistoryList", chinessList);
        model.addAttribute("userId", userId);
        model.addAttribute("time", time);
        User user0 = userLoaderClient.loadUser(userId);
        model.addAttribute("userName", (user0 == null || user0.getProfile() == null) ? null : user0.getProfile().getRealname());
        return "crm/student/studenthomeworkdetail";
    }


    /**
     * 课外拓展任务，查询一个学生30天内的作业情况
     */
    @RequestMapping(value = "studentexpandhomeworkdetail.vpage", method = RequestMethod.GET)
    public String studentExpandHomeworkDetail(@RequestParam("userId") Long userId, Model model) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        if (studentDetail == null) {
            return "crm/student/studenthomeworkdetail_middleschool";
        }
        return "crm/student/studentexpandhomeworkdetail";
    }

    /**
     * ***********************磨耳朵活动****************************************************************
     */
    @RequestMapping(value = "studentgrindear.vpage", method = RequestMethod.GET)
    public String studentGrindEarDetail(@RequestParam("userId") Long userId, Model model) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        if (studentDetail == null) {
            return "crm/student/studenthomeworkdetail_middleschool";
        }
        model.addAttribute("userName", studentDetail.fetchRealname());
        model.addAttribute("userId", userId);

        StudentGrindEarRecord studentGrindEarRecord = grindEarService.loadGrindEarRecord(studentDetail.getId());
        List<Date> dateList = studentGrindEarRecord == null ? Collections.emptyList() : studentGrindEarRecord.getDateList();
        dateList = dateList == null ? Collections.emptyList() : dateList;
        Set<DayRange> finishDayRangeSet = dateList.stream().map(t -> DayRange.newInstance(t.getTime())).distinct().collect(Collectors.toSet());
        List<GrindEarDataWrapper> list = new ArrayList<>();
        Boolean todayIsFinish = false;
        DayRange todayDayRange = DayRange.current();
        Set<Integer> alreadyHadDaySet = new HashSet<>();
        LinkedList<DayRange> continuityDayRangeList = new LinkedList<>();
        Boolean startWait = false;
        for (int i = 0; i < GrindEarActivity.dayRangeList.size(); i++) {
            DayRange dayRange = GrindEarActivity.dayRangeList.get(i);
            GrindEarDataWrapper wrapper = new GrindEarDataWrapper();
            wrapper.setDay(dayRange.toString());
            if (startWait) {
                wrapper.setStatus(GrindEarActivity.wait);
                wrapper.setHasParentReward(false);
                list.add(wrapper);
                continue;
            }
            int status;
            if (dayRange.getStartDate().before(todayDayRange.getStartDate())) {
                if (finishDayRangeSet.contains(dayRange)) {
                    status = GrindEarActivity.finish;
                } else
                    status = GrindEarActivity.dead;
            } else if (dayRange.equals(todayDayRange)) {
                if (finishDayRangeSet.contains(dayRange)) {
                    todayIsFinish = true;
                    status = GrindEarActivity.finish;
                } else
                    status = GrindEarActivity.wait;
                startWait = true;
            } else
                status = GrindEarActivity.wait;
            wrapper.setStatus(status);
            wrapper.setHasParentReward(false);
            wrapper.setParentRewardIsSend(false);
            if (status == GrindEarActivity.finish) {//判断是否有家长奖励
                if (continuityDayRangeList.size() > 0 && !continuityDayRangeList.getLast().equals(dayRange.previous())) {
                    continuityDayRangeList.clear();
                }
                continuityDayRangeList.addLast(dayRange);
                Integer rewardDay = continuityDayRangeList.size();
                if (GrindEarActivity.days2RewardTypeMap.containsKey(rewardDay) && !alreadyHadDaySet.contains(rewardDay)) {
                    alreadyHadDaySet.add(rewardDay);
                    boolean parentRewardIsSend = parentRewardIsSend(studentDetail.getId(), rewardDay);
                    wrapper.setHasParentReward(true);
                    wrapper.setParentRewardIsSend(parentRewardIsSend);
                }

            }
            list.add(wrapper);
        }
        Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false).stream()
                .filter(t -> t.getSubject() == Subject.ENGLISH)
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        List<NewClazzBookRef> clazzBookRefList = newClazzBookLoaderClient.loadGroupBookRefs(groupIds).toList();
        NewClazzBookRef newClazzBookRef = clazzBookRefList.stream().sorted((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime())).findFirst().orElse(null);
        if (newClazzBookRef != null) {
            String bookId = newClazzBookRef.getBookId();
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
            if (newBookProfile != null) {
                model.addAttribute("homeworkBookName", newBookProfile.getShortName());
                model.addAttribute("homeworkDate", DateUtils.dateToString(newClazzBookRef.getUpdateDatetime()));
            }
        }

        model.addAttribute("grindEarList", list);
        model.addAttribute("todayIsFinish", todayIsFinish);
        model.addAttribute("startDate", GrindEarActivity.startDay.toString());
        model.addAttribute("endDate", GrindEarActivity.endDay.toString());
        model.addAttribute("isInPeriod", GrindEarActivity.isInActivityPeriod());
        return "crm/student/studentgrindear";
    }

    @RequestMapping(value = "parentreward.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String parentReward(@RequestParam Long userId, Model model) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        model.addAttribute("userId", userId);
        if (studentDetail == null) {
            return redirect("/crm/student/studentlist.vpage");
        }
        model.addAttribute("userName", studentDetail.fetchRealname());
        String startDate = getRequestString("startDate");
        String endDate = getRequestString("endDate");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        String id = StudentRewardSendCount.generateId(userId, ParentRewardHelper.currentTermDateRange().getStartDate());
        Map<String, StudentRewardSendCount> sendCountMap = parentRewardLoader.getStudentRewardSendCount(Collections.singletonList(id));
        if (MapUtils.isNotEmpty(sendCountMap)) {
            StudentRewardSendCount studentRewardSendCount = sendCountMap.get(id);
            if (studentRewardSendCount != null) {
                model.addAttribute("itemCount", SafeConverter.toInt(studentRewardSendCount.getItemCount()));
            }
        }

        Date startTime = null;
        Date endTime = null;
        if (StringUtils.isNotBlank(startDate)) {
            startTime = DateUtils.stringToDate(startDate + " 00:00:00");
        }
        if (StringUtils.isNotBlank(endDate)) {
            endTime = DateUtils.stringToDate(endDate + " 23:59:59");
        }
        List<ParentRewardLog> rewardLogs = new ArrayList<>();
        //默认查询7天内的所有奖励
        if (startTime == null && endTime == null) {
            rewardLogs = parentRewardLoader.getSevenDaysRewardList(userId);
        } else if (startTime != null && endTime != null && startTime.before(endTime)) {
            if (DateUtils.dayDiff(endTime, startTime) > 90) {
                startTime = DateUtils.addDays(endTime, -90);
            }
            rewardLogs = parentRewardLoader.getParentRewardLogByDate(userId, startTime, endTime);
        } else if (startTime != null && endTime == null) {
            endTime = DateUtils.addDays(startTime, 90);
            Date now = new Date();
            endTime = endTime.before(now) ? endTime : now;
            rewardLogs = parentRewardLoader.getParentRewardLogByDate(userId, startTime, endTime);
        } else {
            model.addAttribute("error", "请填写正确的日期区间");
        }
        List<ParentRewardMapper> rewards = new ArrayList<>();
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(userId);
        rewardLogs.stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .forEach(rewardLog -> {
                    ParentRewardMapper mapper = new ParentRewardMapper();
                    Long parentId = rewardLog.getParentId();
                    if (rewardLog.getStatus() != 0 && parentId != null) {
                        StudentParentRef studentParentRef = studentParentRefs.stream()
                                .filter(s -> Objects.equals(s.getParentId(), parentId))
                                .findFirst()
                                .orElse(null);
                        if (studentParentRef != null) {
                            String callName = CallName.其它监护人.name().equals(studentParentRef.getCallName()) ? "“其它”家长" : studentParentRef.getCallName();
                            mapper.setSendUser(callName);
                        } else {
                            mapper.setSendUser("“其它”家长");
                        }
                    }

                    ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
                    if (item != null) {
                        ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
                        if (category != null) {
                            mapper.setTitle(getParentRewardDescription(rewardLog, item, category));
                        }
                    }

                    mapper.setId(rewardLog.getId());
                    mapper.setKey(rewardLog.getKey());
                    String type = rewardLog.getType();
                    mapper.setRealType(type);
                    ParentRewardItemType itemType = ParentRewardItemType.of(type);
                    if (itemType != null) {
                        mapper.setType(itemType.getCategoryName());
                    }
                    mapper.setCount(rewardLog.getCount());
                    mapper.setStatus(rewardLog.getStatus());
                    mapper.setCreateDate(DateUtils.dateToString(rewardLog.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
                    if (rewardLog.getSendTime() != null) {
                        mapper.setSendDate(DateUtils.dateToString(rewardLog.getSendTime(), "yyyy-MM-dd HH:mm:ss"));
                    }
                    if (rewardLog.getStatus() == 2) {
                        mapper.setReceiveDate(DateUtils.dateToString(rewardLog.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
                    }
                    rewards.add(mapper);
                });
        model.addAttribute("rewards", rewards);
        return "crm/student/studentParentReward";
    }

    @RequestMapping(value = "sendReward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendReward() {
        Long studentId = getRequestLong("studentId");
        String id = getRequestString("id");
        String key = getRequestString("key");
        String type = getRequestString("type");
        Integer count = getRequestInt("count");
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(key) || StringUtils.isEmpty(type)) {
            return MapMessage.errorMessage("没有可发放的奖励");
        }
        ParentRewardLog log = new ParentRewardLog();
        log.setId(id);
        log.setKey(key);
        log.setType(type);
        log.setCount(count);
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
        Long parentId = null;
        StudentParentRef studentParentRef = studentParentRefs.stream()
                .filter(StudentParentRef::isKeyParent)
                .findFirst()
                .orElse(null);
        if (studentParentRef == null) {
            studentParentRef = studentParentRefs.stream()
                    .findFirst()
                    .orElse(null);
        }
        if (studentParentRef != null) {
            parentId = studentParentRef.getParentId();
        }
        if (parentId != null) {
            ParentRewardSendResult sendResult;
            try {
                sendResult = atomicLockManager.wrapAtomic(parentRewardService)
                        .keyPrefix("SEND_STUDENT_REWARD")
                        .keys(studentId)
                        .proxy()
                        .sendParentRewards(parentId, studentId, Collections.singletonList(log));
            } catch (DuplicatedOperationException ex) {
                return MapMessage.errorMessage("当前奖励正在被发放，请稍后再试");
            }
            if (sendResult == null) {
                return MapMessage.errorMessage("奖励发放失败");
            }
        } else {
            return MapMessage.errorMessage("没有家长");
        }
        return MapMessage.successMessage();
    }

    @ResponseBody
    @RequestMapping(value = "patchDayFinish.vpage", method = RequestMethod.POST)
    public MapMessage patchDayFinish(@RequestParam Long studentId, @RequestParam String day) {
        if (studentId == null || studentId == 0L
                || StringUtils.isBlank(day))
            return MapMessage.errorMessage("参数错误");
        if (!GrindEarActivity.isInActivityPeriod())
            return MapMessage.errorMessage("活动已过期");
        DayRange dayRange = DayRange.parse(day);
        if (dayRange == null)
            return MapMessage.errorMessage("参数错误");
        if (!GrindEarActivity.dayRangeList.contains(dayRange))
            return MapMessage.errorMessage("参数错误");
        if (dayRange.getStartDate().after(DayRange.current().getStartDate()))
            return MapMessage.errorMessage("还不可以补哦");
        grindEarService.mockPushRecord(studentId, new Date(dayRange.getStartDate().getTime() + 1000 * 3610));
        grindEarService.sendIntegral(studentLoaderClient.loadStudentDetail(studentId), dayRange);
        return MapMessage.successMessage();
    }

    @ResponseBody
    @RequestMapping(value = "deleteWaiyanFlag.vpage", method = RequestMethod.POST)
    public MapMessage deleteWaiyanFlag(@RequestParam Long studentId) {
        if (studentId == null || studentId == 0L)
            return MapMessage.errorMessage("参数错误");
        String key = grindEarService.waiyanKey(studentId);
        VendorCache.getVendorPersistenceCache().delete(key);
        return MapMessage.successMessage();
    }

    private String getParentRewardDescription(ParentRewardLog log, ParentRewardItem item, ParentRewardCategory category) {
        String description = item.getDescription();
        if ("HOMEWORK".equals(category.getKey()) && StringUtils.isNotEmpty(description)) {
            String[] descArr = description.split("_");
            if (descArr.length == 2) {
                return log.realTitle(descArr[0]);
            }
        }
        return description;
    }

    private boolean parentRewardIsSend(Long studentId, Integer day) {
        Map<Integer, Boolean> map = parentRewardsIsSend(studentId, Collections.singleton(day));
        if (map == null)
            return false;
        Boolean aBoolean = map.get(day);
        return aBoolean != null && aBoolean;
    }

    private Map<Integer, Boolean> parentRewardsIsSend(Long studentId, Collection<Integer> days) {
        Map<Integer, Boolean> map = new HashMap<>();
        for (Integer day : days) {
            CacheObject<Object> objectCacheObject = VendorCache.getVendorPersistenceCache().get(generateParentRewardKey(studentId, day));
            if (objectCacheObject != null && objectCacheObject.getValue() != null && SafeConverter.toBoolean(objectCacheObject.getValue()))
                map.put(day, true);
            else
                map.put(day, false);
        }
        return map;
    }

    private String generateParentRewardKey(Long studentId, Integer day) {
        return keyPrefix + studentId + "_" + day;
    }

    private static String keyPrefix = "grindEarBigParentReward_";


    @Data
    public static class GrindEarDataWrapper {
        private String day;
        private Integer status;
        private String statusStr;
        private Boolean hasParentReward;
        private String color;
        private Boolean parentRewardIsSend;

        public void setStatus(Integer status) {
            this.status = status;
            if (status == GrindEarActivity.finish) {
                this.statusStr = "已完成";
                this.color = "green";
            }
            if (status == GrindEarActivity.wait) {
                this.statusStr = "待完成";
                this.color = "royalblue";
            }
            if (status == GrindEarActivity.dead) {
                this.statusStr = "未完成";
                this.color = "red";
            }
        }
    }


    @RequestMapping(value = "useraudionewhomeworkresultdetail.vpage", method = RequestMethod.GET)
    public String userAudioNewhomeworkResultdetail(Model model) {

        String path = "crm/homework/useraudionewhomeworkresultdetail";

        Long sid = this.getRequestLong("sid");
        model.addAttribute("sid", sid);
        if (sid == 0L) {
            model.addAttribute("success", false);
            return path;
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);

        model.addAttribute("sname", studentDetail.fetchRealname());

        model.addAttribute("success", true);

        model.addAttribute("crmAudioNewhomework", Collections.emptyList());

        return path;

    }

    @RequestMapping(value = "groupcircle.vpage", method = RequestMethod.GET)
    public String getGroupCircleList(Model model) {
        Long studentId = getRequestLong("userId");
        long limit = getRequestLong("limit", 10);
        User user = userLoaderClient.loadUser(studentId);
        List<GroupMapper> groupMapperList = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);

        Map<Long, Subject> groupSubjectMaps = groupMapperList.stream().collect(Collectors.toMap(GroupMapper::getId, GroupMapper::getSubject));

        //缓存记录
        List<ScoreCircleGroupContext> contextList = new ArrayList<>();
        Date createDate = new Date();
        Date oldestDate = DateUtils.addDays(new Date(), -90);
        do {
            ScoreCircleGroupContext context = new ScoreCircleGroupContext();
            context.setCreateDate(createDate);
            for (Long groupId : groupSubjectMaps.keySet()) {
                context.setGroupId(groupId);
                ScoreCircleResponse circleResponse = dpScoreCircleLoader.loadGroupCircleByScoreDesc(context, limit);
                if (circleResponse.isSuccess() && CollectionUtils.isNotEmpty(circleResponse.getContextList())) {
                    circleResponse.getContextList().forEach(p -> contextList.add((ScoreCircleGroupContext) p));
                }
                ScoreCircleResponse ignoreSubjectCircleResponse = dpScoreCircleLoader.loadGroupIgnoreSubjectCircleByScoreDesc(context, limit);
                if (ignoreSubjectCircleResponse.isSuccess() && CollectionUtils.isNotEmpty(ignoreSubjectCircleResponse.getContextList())) {
                    ignoreSubjectCircleResponse.getContextList().forEach(p -> contextList.add((ScoreCircleGroupContext) p));
                }
            }
            if (contextList.size() > limit) {
                break;
            }
            createDate = MonthRange.newInstance(createDate.getTime()).previous().getEndDate();
        } while (oldestDate.before(createDate));

        //数据库记录
        Set<String> idSet = contextList.stream().map(ScoreCircleGroupContext::generateId).collect(Collectors.toSet());
        Map<String, ScoreCircleGroupContextStoreInfo> contextStoreInfoMap = dpScoreCircleLoader.loads(idSet);
        List<Map<String, Object>> mapList = new ArrayList<>();
        contextList.sort((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()));
        for (ScoreCircleGroupContext context : contextList) {
            Map<String, Object> map = JsonUtils.safeConvertObjectToMap(context);
            if (MapUtils.isNotEmpty(map)) {
                map.put("db_exists", contextStoreInfoMap.containsKey(context.generateId()));
                map.put("subject", groupSubjectMaps.get(context.getGroupId()) == null ? "" : groupSubjectMaps.get(context.getGroupId()).getValue());
                mapList.add(map);
            }
        }
        model.addAttribute("userId", studentId);
        model.addAttribute("limit", limit);
        model.addAttribute("userName", user == null ? "" : user.fetchRealname());
        model.addAttribute("circleList", mapList);
        return "crm/student/parentappcirclelist";
    }

    @RequestMapping(value = "delete_group_circle.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteGroupCircle() {
        Long groupId = getRequestLong("groupId");
        String circleType = getRequestString("groupCircleType");
        String typeId = getRequestString("typeId");
        Long date = getRequestLong("createDate");
        GroupCircleType groupCircleType = GroupCircleType.parse(circleType);
        if (groupCircleType == null) {
            return MapMessage.errorMessage("类型错误");
        }
        ScoreCircleGroupContext context = new ScoreCircleGroupContext();
        context.setGroupId(groupId);
        context.setGroupCircleType(groupCircleType);
        context.setTypeId(typeId);
        context.setCreateDate(new Date(date));
        ScoreCircleProcessResult processResult = dpScoreCircleService.delete(context);
        addAdminLog("delete_group_circle", JsonUtils.toJson(context), getCurrentAdminUser().getAdminUserName() + "删除了作业和通知");
        return processResult.isSuccess() ? MapMessage.successMessage() : MapMessage.errorMessage("删除失败");
    }


    private void handle(List<Map<String, Object>> homeWorkHistoryMapperList, User user, Clazz clazz, Long userId) {
        if (!CollectionUtils.isEmpty(homeWorkHistoryMapperList)) {
            for (Map<String, Object> displayStudentHomeWorkHistoryMapper : homeWorkHistoryMapperList) {
                displayStudentHomeWorkHistoryMapper.put("clazzId", clazz.getId());
                displayStudentHomeWorkHistoryMapper.put("clazzName", clazz.getClassName());
                displayStudentHomeWorkHistoryMapper.put("userId", userId);
                if (user.getProfile() != null)
                    displayStudentHomeWorkHistoryMapper.put("userName", user.getProfile().getRealname());
            }
        }
    }


    /**
     * *********************private method*****************************************************************
     */
    private Map<String, Object> getStudentInfoMap(Long studentId) {

        if (null == studentId) {
            return new HashMap<>();
        }

        User student = userLoaderClient.loadUser(studentId);
        if ((null == student) || student.getUserType() != UserType.STUDENT.getType() || student.getDisabled())
            return new HashMap<>();

        // 返回结果map
        Map<String, Object> studentInfoMap = new HashMap<>();

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(student.getId());
//        // 设置qq和email
//        if (student.getProfile().getSensitiveQq() != null) {
//            String qq = sensitiveUserDataServiceClient.loadUserQq(student.getId(), "/crm/student/studenthomepage.vpage");
//            student.getProfile().setSensitiveQq(qq);
//        }
        studentInfoMap.put("student", student);

//        UserLoginInfo loginInfo = userLoaderClient.loadUserLoginInfo(studentId);
        UserLoginInfo loginInfo = userLoginServiceClient.getUserLoginService().loadUserLoginInfo(studentId).getUninterruptibly();
        if (loginInfo != null) {
            studentInfoMap.put("lastLoginTime", loginInfo.getLoginTime());
        }

        // authentication
        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(studentId);
        studentInfoMap.put("authentication", authentication);

        // vendor app
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", studentId);
        studentInfoMap.put("vendorAppsUserRef", vendorAppsUserRef);

        // user order
        List<UserOrder> tempOrderList = userOrderLoaderClient.loadUserOrderList(studentId).stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                .filter(o -> o.getOrderStatus() == OrderStatus.Confirmed)
                .collect(Collectors.toList());
        studentInfoMap.put("latestOrderList", tempOrderList.size() > 10 ? tempOrderList.subList(0, 10) : tempOrderList);

        // user coupon
        List<CouponShowMapper> couponShowMappers = couponLoaderClient.loadUserCoupons(studentId);
        studentInfoMap.put("couponList", couponShowMappers);

        // 学校信息
        School school = asyncStudentServiceClient.getAsyncStudentService()
                .loadStudentSchool(student.getId())
                .getUninterruptibly();
        if (school != null) {

            studentInfoMap.put("schoolId", school.getId());
            studentInfoMap.put("schoolName", school.getCname());
            studentInfoMap.put("schoolLevel", school.getLevel());
            studentInfoMap.put("regionCode", school.getRegionCode());

            ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
            if (region != null) {
                studentInfoMap.put("regionName", region.toString("/"));
            }
        }
        ChannelCUserAttribute channelCUserAttribute = studentLoaderClient.loadStudentChannelCAttribute(studentId);
        if (channelCUserAttribute != null) {
            ChannelCUserAttribute.ClazzCLevel cLevel = ChannelCUserAttribute.getClazzCLevelByClazzJie(channelCUserAttribute.getClazzJie());
            if (cLevel != null) {
                studentInfoMap.put("cClazzLevel", cLevel.getLevel());
            }
        }
        List<Map<String, Object>> teacherInfoList = new ArrayList<>();
        // 老师信息
        List<GroupMapper> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        Set<Long> groupIds = studentGroups.stream().map(GroupMapper::getId).collect(Collectors.toSet());
        List<GroupTeacherTuple> groupTeacherRefList = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByGroupIds(groupIds);
        // 批量读取老师信息
        List<Long> teacherIds = groupTeacherRefList.stream().map(GroupTeacherTuple::getTeacherId).collect(Collectors.toList());
        Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(teacherIds);
        //学生所在行政班级信息
        Map<Long, Long> groupClazzIdMap = studentGroups.stream().collect(Collectors.toMap(GroupMapper::getId, GroupMapper::getClazzId, (g1, g2) -> g1));
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(groupClazzIdMap.values())
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        //学生所在组的任教老师信息
        for (GroupTeacherTuple ref : groupTeacherRefList) {
            Map<String, Object> teacherInfo = new HashMap<>();

            Long teacherId = ref.getTeacherId();
            teacherInfo.put("teacherId", teacherId);

            Teacher teacher = teachers.get(teacherId);
            UserAuthentication teacherUa = userLoaderClient.loadUserAuthentication(teacher.getId());
            if (null != teacher && ua != null) {
                teacherInfo.put("teacherName", teacher.fetchRealname());
                teacherInfo.put("teacherSubject", (teacher.getSubject() == null) ? null : teacher.getSubject().getValue());
                teacherInfo.put("teacherMobile", teacherUa.getSensitiveMobile());
                Clazz teacherClazz = clazzs.get(groupClazzIdMap.get(ref.getGroupId()));
                //TODO removeManager fixed
                if (teacherClazz != null) {
                    teacherInfo.put("isExit", RefStatus.INVALID.name().equals(ref.getStatus()));
                    teacherInfo.put("classId", teacherClazz.getId());
                    teacherInfo.put("className", teacherClazz.formalizeClazzName());
                    teacherInfo.put("classLevel", teacherClazz.getClassLevel());
                    // Task #29850 增加学制 By Wyc 2016-08-19
                    teacherInfo.put("eduSys", teacherClazz.getEduSystem() != null ? teacherClazz.getEduSystem().getDescription() : "");
                }
                teacherInfo.put("creator", false);
                teacherInfoList.add(teacherInfo);
            }
        }
        studentInfoMap.put("teacherInfoList", teacherInfoList);

        // Parent related
        List<Map<String, Object>> parentsInfo = new ArrayList<>();
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
        List<Long> parentIds = studentParentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toList());
        Map<Long, VendorAppsUserRef> parentAppMap = vendorLoaderClient.loadVendorAppUserRefs("17Parent", parentIds);
        List<AppParentSignRecord> signRecords = vendorLoaderClient.loadAppParentSignRecordByUserIds(parentIds);
        Map<String, AppParentSignRecord> signRecordMap = signRecords.stream().collect(Collectors.toMap(AppParentSignRecord::getId, t -> t));
        Map<Long, User> parentUserMap = userLoaderClient.loadUsers(parentIds);
        MonthRange currentMonth = MonthRange.current();
        MonthRange lastMonth = MonthRange.current().previous();

        studentParentRefs
                .stream()
                .filter(e -> parentUserMap.get(e.getParentId()) != null)
                .forEach(e -> {
                    Map<String, Object> parentInfo = new HashMap<>();
                    User parent = parentUserMap.get(e.getParentId());
                    UserAuthentication parentUa = userLoaderClient.loadUserAuthentication(parent.getId());
                    parentInfo.put("id", parent.getId());
                    parentInfo.put("realName", parent.getProfile().getRealname());
                    parentInfo.put("mobile", parentUa.getSensitiveMobile());
                    parentInfo.put("isAuthenticated", ua.isMobileAuthenticated());
                    parentInfo.put("isKeyParent", e.isKeyParent());
                    parentInfo.put("parentApp", parentAppMap.get(e.getParentId()));
                    parentInfo.put("callName", e.getCallName());
                    AppParentSignRecord signRecord = signRecordMap.get(parent.getId().toString());
                    parentInfo.put("isLastMonthSigned", signRecord != null && signRecord.getAppParentSignRecordMap().keySet().stream().anyMatch(p -> lastMonth.contains(DateUtils.stringToDate(p, "yyyyMMdd"))));
                    parentInfo.put("isCurrentMonthSigned", signRecord != null && signRecord.getAppParentSignRecordMap().keySet().stream().anyMatch(p -> currentMonth.contains(DateUtils.stringToDate(p, "yyyyMMdd"))));
                    parentsInfo.add(parentInfo);
                });

        studentInfoMap.put("parentsInfo", parentsInfo);

        User target = new User();
        target.setId(studentId);
        target.setUserType(UserType.STUDENT.getType());

        UserIntegral userIntegralMapper = integralLoaderClient.getIntegralLoader().loadStudentIntegral(student.getId());
        if (userIntegralMapper != null && userIntegralMapper.getIntegral() != null)
            studentInfoMap.put("integral", userIntegralMapper.getIntegral().getUsableIntegral());
        else
            studentInfoMap.put("integral", 0);

        Finance userFinance = financeServiceClient.getFinanceService()
                .loadUserFinance(student.getId())
                .getUninterruptibly();
        if (userFinance != null)
            studentInfoMap.put("balance", userFinance.getBalance());
        else
            studentInfoMap.put("balance", 0);
//        List<CustomerServiceRecord> customerServiceRecordList = customerServiceRecordPersistence.getCustomerServiceRecord(studentId);
        List<UserServiceRecord> userServiceRecords = userLoaderClient.loadUserServiceRecords(studentId, 20);
        studentInfoMap.put("customerServiceRecordList", userServiceRecords);
        //student authed
        boolean studentAuthed = userAuthQueryServiceClient.isAuthedStudent(studentId, school == null ? SchoolLevel.JUNIOR : SchoolLevel.safeParse(school.getLevel()));
        studentInfoMap.put("studentAuthed", studentAuthed);

        //blackStatus黑名单
        String blackStatus = "";

        //whiteStatus白名单
        String whiteStatus = "";

        //判断是否在学校黑名单
        List<GlobalTag> blackSchools = globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.AfentiBlackListSchools.name());
        Set<String> blackSchoolList = CollectionUtils.toLinkedList(blackSchools).stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getTagValue() != null)
                .map(GlobalTag::getTagValue)
                .collect(Collectors.toSet());
        if (school != null && school.getId() != null) {
            if (blackSchoolList.contains(school.getId().toString())) {
                blackStatus = blackStatus + "学校黑名单 ";
            }
        }

        //判断是否在个人黑名单
        List<GlobalTag> blackUsers = globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.AfentiBlackListUsers.name());
        Set<String> blackUserList = CollectionUtils.toLinkedList(blackUsers).stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getTagValue() != null)
                .map(GlobalTag::getTagValue)
                .collect(Collectors.toSet());
        if (blackUserList.contains(studentId.toString())) {
            blackStatus = blackStatus + "个人黑名单 ";
        }

        //判断是否在地区黑名单
        if (school != null && school.getRegionCode() != null) {
            ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
            if (region != null && region.containsTag(RegionConstants.TAG_PAYMENT_BLACKLIST_REGIONS)) {
                blackStatus = blackStatus + "地区黑名单 ";
            }
        }

        //判断是否在家长通黑名单
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        if (studentExtAttribute != null && SafeConverter.toBoolean(studentExtAttribute.getCloseFairyland())) {
            blackStatus = blackStatus + "家长关闭成长世界";
        }

        //无黑名单
        if (blackStatus.equals("")) {
            blackStatus = "无黑名单";
        }

        GlobalTag globalTag = globalTagServiceClient.getGlobalTagBuffer()
                .findByName(GlobalTagName.PaymentWhiteListUsers.name())
                .stream()
                .filter(t -> StringUtils.equals(studentId.toString(), t.getTagValue()))
                .findFirst()
                .orElse(null);

        // 付费白名单用户
        if (globalTag != null) {
            whiteStatus = "是";
        } else {
            whiteStatus = "否";
        }

        studentInfoMap.put("blackStatus", blackStatus);
        studentInfoMap.put("whiteStatus", whiteStatus);
        return studentInfoMap;
    }

    private List<Long> getStudentIdList(Map conditionMap) {

        List<Long> studentIdList = new ArrayList<>();

        //如果查询条件中包含“studentId”,则忽略其他条件
        if (StringUtils.isNotBlank((String) conditionMap.get("studentId"))) {
            Long studentId;
            try {
                studentId = SafeConverter.toLong(conditionMap.get("studentId"));
                User student = userLoaderClient.loadUser(studentId);
                if (student != null && !student.isDisabledTrue() && student.fetchUserType() == UserType.STUDENT)
                    studentIdList.add(studentId);

            } catch (Exception ignored) {
                //do nothing here
            }
        } else if (StringUtils.isNotBlank((String) conditionMap.get("userMobile"))) {
            // 如果有手机号，也就直接查了吧
            try {
                String mobile = sensitiveUserDataServiceClient.encodeMobile((String) conditionMap.get("userMobile"));
                List<UserAuthentication> userList = userLoaderClient.loadMobileAuthentications(mobile);
                for (UserAuthentication ua : userList) {
                    if (ua.getUserType() == UserType.STUDENT) {
                        studentIdList.add(ua.getId());
                    } else if (ua.getUserType() == UserType.PARENT) {
                        Set<Long> children = parentLoaderClient.loadParentStudentRefs(ua.getId()).stream()
                                .filter(c -> !c.isDisabledTrue()).map(StudentParentRef::getStudentId).collect(Collectors.toSet());
                        if (!CollectionUtils.isEmpty(children)) studentIdList.addAll(children);
                    }
                }
            } catch (Exception ignored) {
                //do nothing here
            }
        } else {
            // 其他条件，可以是 城市 + 【学校】 + 学生姓名
            // 先根据城市+学生姓名模糊查询，然后根据学校ID做一次过滤
            // 如果城市+学生姓名没有结果，那么直接根据学校ID+学生姓名查询
            String studentName = SafeConverter.toString(conditionMap.get("studentName"));
            if (StringUtils.isBlank(studentName)) {
                return studentIdList;
            }

            Integer regionCode = SafeConverter.toInt(conditionMap.get("regionId"));
            if (regionCode <= 0) {
                return studentIdList;
            }

            ExRegion region = raikouSystem.loadRegion(regionCode);
            List<Integer> regionList = new ArrayList<>();
            if (region != null && region.fetchRegionType() == RegionType.COUNTY) {
                regionList.add(regionCode);
            } else if (region != null && region.fetchRegionType() == RegionType.CITY) {
                List<ExRegion> countyList = region.getChildren();
                for (ExRegion county : countyList) {
                    regionList.add(county.getCode());
                }
            }

            if (CollectionUtils.isEmpty(regionList)) {
                return studentIdList;
            }

            List<Long> queryResult = uctUserServiceClient.queryUserByName(studentName, regionList);
            Long schoolId = SafeConverter.toLong(conditionMap.get("schoolId"));

            if (CollectionUtils.isEmpty(queryResult)) {  // 无结果，如果有学校ID，那么根据学校ID+学生姓名再查一次
                if (schoolId > 0L) {
                    School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
                    if (school == null) {
                        return studentIdList;
                    }

                    Collection<Long> clazzIds = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            .loadSchoolClazzs(school.getId())
                            .originalLocationsAsList()
                            .stream()
                            .map(Clazz.Location::getId)
                            .collect(Collectors.toSet());
                    Map<Long, List<User>> clazzStudents = studentLoaderClient.loadClazzStudents(clazzIds);
                    for (List<User> userList : clazzStudents.values()) {
                        for (User user : userList) {
                            if (user.fetchRealname().contains(studentName) && !studentIdList.contains(user.getId())) {
                                studentIdList.add(user.getId());
                            }
                        }
                    }
                }
            } else {
                if (schoolId > 0L) { // 如果有结果，并且有学校，那么做一下学校过滤
                    Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(queryResult);
                    for (StudentDetail detail : studentDetailMap.values()) {
                        if (detail.getClazz() != null && Objects.equals(detail.getClazz().getSchoolId(), schoolId)) {
                            studentIdList.add(detail.getId());
                        }
                    }
                } else {
                    studentIdList.addAll(queryResult);
                }
            }

        }

        return studentIdList;
    }

    private List<Map<String, Object>> getStudentSnapshot(List<Long> studentIdList, String teacherName) {

        List<Map<String, Object>> studentList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(studentIdList)) {

            for (Long studentId : studentIdList) {

                Map<String, Object> studentMap = new HashMap<>();

                User student = userLoaderClient.loadUser(studentId);

                if (null == student) continue;

                studentMap.put("studentId", studentId);
                if (student.getProfile() != null)
                    studentMap.put("studentName", student.getProfile().getRealname());

                School school = asyncStudentServiceClient.getAsyncStudentService()
                        .loadStudentSchool(student.getId())
                        .getUninterruptibly();
                if (school != null) {
                    studentMap.put("schoolId", school.getId());
                    studentMap.put("schoolName", school.getCname());
                }

                Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(student.getId());
                if (clazz != null) {
                    // TODO removeManager fixed
                    ClazzTeacher ct = teacherLoaderClient.loadClazzCreator(clazz.getId());
                    Teacher teacher = ct == null ? null : ct.getTeacher();
                    //判断没有班主任信息
                    studentMap.put("classLevel", clazz.getClassLevel());
                    studentMap.put("className", clazz.getClassName());
                    studentMap.put("classId", clazz.getId());
                    if (teacher != null) {
                        UserAuthentication teacherUa = userLoaderClient.loadUserAuthentication(teacher.getId());
                        studentMap.put("teacherId", teacher.getId());
                        studentMap.put("teacherName", teacher.getProfile().getRealname());
                        studentMap.put("teacherMobile", teacherUa.getSensitiveMobile());
                    }
                }

                if (studentId != null) {
                    List<Map<String, Object>> parentsInfo = new ArrayList<>();
                    List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
                    if (studentParentRefs != null && studentParentRefs.size() > 0) {
                        for (StudentParentRef studentParentRef : studentParentRefs) {
                            User parent = userLoaderClient.loadUser(studentParentRef.getParentId());
                            if (parent != null && parent.getProfile() != null) {
                                Map<String, Object> parentInfo = new HashMap<>();
                                parentInfo.put("id", parent.getId());
                                parentInfo.put("realName", parent.getProfile().getRealname());
                                UserAuthentication parentUa = userLoaderClient.loadUserAuthentication(parent.getId());
                                parentInfo.put("mobile", parentUa.getSensitiveMobile());
                                parentInfo.put("isAuthenticated", parentUa.isMobileAuthenticated());
                                parentInfo.put("isKeyParent", studentParentRef.isKeyParent());

                                parentsInfo.add(parentInfo);
                            }
                        }
                    }
                    studentMap.put("parentsInfo", parentsInfo);
                }
                //老师姓名的模糊查询====老师姓名为空或者以输入的老师姓名为起始字符
                if (StringUtils.isBlank(teacherName) || ConversionUtils.toString(studentMap.get("teacherName")).startsWith(teacherName)) {
                    studentList.add(studentMap);
                }
            }
        }

        return studentList;
    }

    @RequestMapping(value = "unbindparent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage unbindparent(@RequestParam Long studentId, @RequestParam Long parentId) {
        MapMessage message = new MapMessage();
        try {
            crmUserService.unbindStudentParentRef(studentId, parentId, getCurrentAdminUser().getAdminUserName());
            message.setSuccess(true);
            message.setInfo("解绑成功");
        } catch (Exception ex) {
            logger.error("解绑失败,[studentId:{},parentId:{},msg:{}]", studentId, parentId, ex.getMessage(), ex);
            message.setSuccess(false);
            message.setInfo("解绑失败，" + ex.getMessage());
        }
        return message;
    }

    // 绑定17Id和klxId
    @RequestMapping(value = "linkklxstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage linkKlxstudent() {
        Long a17Id = getRequestLong("studentId");
        String klxId = getRequestString("linkedKlxId");
        klxId = klxId.trim();

        if (StringUtils.isBlank(klxId)) {
            return MapMessage.errorMessage("请输入正确的快乐学ID");
        }

        Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(Collections.singleton(klxId));
        KlxStudent klxStudent = klxStudentMap.get(klxId);

        if (Objects.isNull(klxStudent)) {
            return MapMessage.errorMessage("未找到此快乐学学生:" + klxId);
        }

        if (klxStudent.getA17id() != null && klxStudent.getA17id() != 0) {
            return MapMessage.errorMessage("此快乐学ID:" + klxId + "已和17ID:" + klxStudent.getA17id() + "绑定");
        }

        List<KlxStudent> klxStudents = newKuailexueLoaderClient.loadKlxStudentsBy17Id(a17Id);
        if (CollectionUtils.isNotEmpty(klxStudents)) {
            return MapMessage.errorMessage("此17ID已绑定快乐学ID,请先解绑再操作");
        }

        // 输入的快乐学账号必须没有组关系
        List<Long> groupIds = asyncGroupServiceClient.getAsyncGroupService()
                .findGroupKlxStudentRefsByStudent(klxId)
                .getUninterruptibly()
                .stream()
                .map(GroupKlxStudentRef::getGroupId)
                .collect(Collectors.toList());

        if (groupIds.size() > 0) {
            return MapMessage.errorMessage("此快乐学ID:" + klxId + "已绑定分组" + JsonUtils.toJson(groupIds) + "，无法进行此操作");
        }

        MapMessage msg = newKuailexueServiceClient.updateA17id(klxId, a17Id);
        if (msg.isSuccess()) {
            List<GroupStudentTuple> tuples = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .getGroupStudentTupleService()
                    .dbFindByStudentIdIncludeDisabled(a17Id)
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabledTrue())
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(tuples)) {
                List<GroupKlxStudentRef> groupKlxStudentRefList = new ArrayList<>();
                for (GroupStudentTuple tuple : tuples) {
                    groupKlxStudentRefList.add(GroupKlxStudentRef.newInstance(tuple.getGroupId(), klxId, a17Id));
                }
                newKuailexueServiceClient.persistGroupKlxStudentRefs(groupKlxStudentRefList);
            }

        }

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(a17Id);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("绑定17Id和klxId关联关系");
        userServiceRecord.setComments("17Id:" + a17Id + "，klxId:" + klxId);
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        return MapMessage.successMessage();
    }

    // 解绑17Id和klxId
    @RequestMapping(value = "unbindklxstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage unbindKlxstudent() {
        Long a17Id = getRequestLong("a17Id");
        String klxId = getRequestString("klxId");
        if (a17Id == 0 || StringUtils.isEmpty(klxId)) {
            return MapMessage.errorMessage();
        }

        // 清除KlxStudent的a17id内容
        List<KlxStudent> klxStudents = newKuailexueLoaderClient.loadKlxStudentsBy17Id(a17Id)
                .stream()
                .filter(p -> Objects.equals(p.getId(), klxId))
                .collect(Collectors.toList());
        klxStudents.forEach(p -> newKuailexueServiceClient.clearA17id(p.getId(), p.getA17id()));

        // 清除GroupKlxStudentRef的a17id的内容
        List<GroupKlxStudentRef> groupKlxStudentRefs = asyncGroupServiceClient.getAsyncGroupService()
                .findGroupKlxStudentRefsByStudent(klxId)
                .getUninterruptibly()
                .stream()
                .filter(p -> Objects.equals(p.getA17id(), a17Id))
                .collect(Collectors.toList());
        List<GroupKlxStudentRef> groupKlxStudentRefsNew = new ArrayList<>();
        groupKlxStudentRefs.forEach(p -> {
            p.setA17id(0L);
            groupKlxStudentRefsNew.add(p);
        });
        asyncGroupServiceClient.getAsyncGroupService().updateGroupKlxStudentRefs(groupKlxStudentRefsNew);

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(a17Id);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("解绑17Id和klxId关联关系");
        userServiceRecord.setComments("17Id:" + a17Id + "，klxId:" + klxId);
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        return MapMessage.successMessage();
    }

    //student login from crm
    @RequestMapping(value = "studentlogin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> teacherLogin(@RequestParam Long studentId,
                                            @RequestParam String studentLoginDesc) {
        Map<String, Object> message = new HashMap<>();
        User student = userLoaderClient.loadUser(studentId, UserType.STUDENT);

        studentLoginDesc = studentLoginDesc.trim();
        if (StringUtils.isBlank(studentLoginDesc) || (null == student) || student.getUserType() != UserType.STUDENT.getType())
            message.put("success", false);
        else {
            message.put("success", true);

            String password = userLoaderClient.generateUserTempPassword(studentId);
            Map<String, Object> params = new HashMap<>();
            params.put("j_username", student.getId());
            params.put("j_password", password);

            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            if (clazz != null && clazz.getEduSystem().getKtwelve() == Ktwelve.SENIOR_SCHOOL) {
                params.put("returnURL", ProductConfig.getJuniorSchoolUrl());
            }

            message.put("postUrl", UrlUtils.buildUrlQuery(ProductConfig.getMainSiteBaseUrl() + "/j_spring_security_check", params));
        }

        return message;
    }

    private List<String> getFeedbackQuickReplyList() {
        String query = "select ad.DESCRIPTION from ADMIN_DICT ad " +
                " where ad.GROUP_NAME = '反馈快速回复' and ad.DISABLED = 0 ";
        return utopiaSqlAdmin.withSql(query).queryColumnValues(String.class);
    }

    private List<Integer> getAllChildrenRegionCodesByPcode(Integer pcode) {

        List<Integer> childrenCodes = new ArrayList<>();
        constructChildrenList(pcode, childrenCodes);
        return childrenCodes;
    }

    private void constructChildrenList(Integer pcode, List<Integer> childrenCodes) {
        List<ExRegion> children = raikouSystem.getRegionBuffer().loadChildRegions(pcode);
        List<Integer> codes = children.stream().map(Region::getCode).collect(Collectors.toCollection(LinkedList::new));
        if (codes.size() != 0) {
            for (Integer code : codes) {
                childrenCodes.add(code);
                constructChildrenList(code, childrenCodes);
            }
        } else {
            childrenCodes.add(pcode);
        }
    }


    /**
     * 学生操作信息诊断，红色button"操作诊断"
     * 参数userId,查询时间date
     * 需求详见redmine/issues/10968
     */
    @RequestMapping(value = "studentoptioncheck.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String studentOptionCheck(Model model) {
        return redirect("/crm/student/studentlist.vpage");
    }


    /**
     * 用于flash数据由朝立给的接口提供
     * TODO 如需要可以单独写成抽象，加入签名进行使用
     * 由于不支持跨天查询，endDate隐藏
     * 注：http://project.17zuoye.net/redmine/issues/11192
     *
     * @param uid       用户ID
     * @param startDate 时间(yyyy-MM-dd)
     */
    private Map<String, Object> getLogsStatUserInfoApi(long uid, String startDate) {

        Map<String, Object> resMap = new HashMap<>();
        try {
            String url = "http://log.17zuoye.net/stat/user_info_api.php";
            Map<String, String> params = new HashMap<>();
            params.put("uid", String.valueOf(uid));
            params.put("startdate", startDate);
            String URL = UrlUtils.buildUrlQuery(url, params);
            String result = HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString();
            if (StringUtils.isNotBlank(result)) {
                resMap = JsonUtils.fromJson(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resMap;
    }

    @Inject
    private VacationHomeworkReportLoaderClient vacationHomeworkReportLoaderClient;

    @RequestMapping(value = "studentfetchvacationhomework.vpage", method = RequestMethod.GET)
    public String studentFetchVacationHomework(@RequestParam("userId") Long userId, Model model) {
        model.addAttribute("userId", userId);
        try {

            List<VacationReportForParent> vacationReportForParents = vacationHomeworkReportLoaderClient.loadVacationReportForParent(userId);
            model.addAttribute("vacationReportForParents", vacationReportForParents);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("success", false);
            logger.error("fetch vacationReport failed useId of {}", userId, e);
        }

        return "crm/student/studentfetchvacationhomework";
    }


    @RequestMapping(value = "studentvacationhomework.vpage", method = RequestMethod.GET)
    public String studentVacationHomework(@RequestParam("userId") Long userId, Model model) {
//        EnglishVacationHomework englishVacationHomework = null;
//        MathVacationHomework mathVacationHomework = null;
//        List<GroupMapper> groups = groupLoaderClient.loadStudentGroups(userId, false);
//        for (GroupMapper group : groups) {
//            Subject subject = group.getSubject();
//            if (Subject.ENGLISH == subject) {
//                englishVacationHomework = englishVacationHomeworkLoaderClient.loadCurrentTermHomework(group.getId());
//            } else if (Subject.MATH == subject) {
//                mathVacationHomework = mathVacationHomeworkLoaderClient.loadCurrentTermHomework(group.getId());
//            }
//        }
//        model.addAttribute("englishVacationHomework", englishVacationHomework);
//        model.addAttribute("mathVacationHomework", mathVacationHomework);
//
//        List<EnglishVacationHomeworkPackage> englishVacationHomeworkPackages = Collections.emptyList();
//        if (englishVacationHomework != null) {
//            englishVacationHomeworkPackages = englishVacationHomeworkLoaderClient.loadHomeworkPackageList(englishVacationHomework.getId());
//        }
//        model.addAttribute("englishVacationHomeworkPackages", englishVacationHomeworkPackages);
//        List<MathVacationHomeworkPackage> mathVacationHomeworkPackages = Collections.emptyList();
//        if (mathVacationHomework != null) {
//            mathVacationHomeworkPackages = mathVacationHomeworkLoaderClient.loadHomeworkPackageList(mathVacationHomework.getId());
//        }
//        model.addAttribute("mathVacationHomeworkPackages", mathVacationHomeworkPackages);
//
//        Map<String, EnglishVacationHomeworkPackageInfo> englishVacationHomeworkPackageInfos = new HashMap<>();
//        for (EnglishVacationHomeworkPackage homeworkPackage : englishVacationHomeworkPackages) {
//            EnglishVacationHomeworkPackageInfo homeworkPackageInfo = vacationHomeworkPackageDataParser.parseEvhPackageData(homeworkPackage);
//            englishVacationHomeworkPackageInfos.put(String.valueOf(homeworkPackage.getId()), homeworkPackageInfo);
//        }
//        model.addAttribute("englishVacationHomeworkPackageInfos", englishVacationHomeworkPackageInfos);
//        Map<String, MathVacationHomeworkPackageInfo> mathVacationHomeworkPackageInfos = new HashMap<>();
//        for (MathVacationHomeworkPackage homeworkPackage : mathVacationHomeworkPackages) {
//            MathVacationHomeworkPackageInfo homeworkPackageInfo = vacationHomeworkPackageDataParser.parseMvhPackageData(homeworkPackage);
//            mathVacationHomeworkPackageInfos.put(String.valueOf(homeworkPackage.getId()), homeworkPackageInfo);
//        }
//        model.addAttribute("mathVacationHomeworkPackageInfos", mathVacationHomeworkPackageInfos);
//
//        Map<String, String> vacationHomeworkPractices = new HashMap<>();
//        for (EnglishVacationHomeworkPackageInfo homeworkPackageInfo : englishVacationHomeworkPackageInfos.values()) {
//            List<EnglishVacationHomeworkPractice> practices = homeworkPackageInfo.getPractices();
//            for (EnglishVacationHomeworkPractice practice : practices) {
//                Long practiceId = practice.getPracticeId();
//                PracticeType practiceType = practiceLoaderClient.loadPractice(practiceId);
//                if (practiceType != null) {
//                    vacationHomeworkPractices.put(String.valueOf(practiceId), practiceType.getPracticeName());
//                }
//            }
//        }
//        for (MathVacationHomeworkPackageInfo homeworkPackageInfo : mathVacationHomeworkPackageInfos.values()) {
//            List<MathVacationHomeworkPractice> practices = homeworkPackageInfo.getPractices();
//            for (MathVacationHomeworkPractice practice : practices) {
//                Long practiceId = practice.getPracticeId();
//                PracticeType practiceType = practiceLoaderClient.loadPractice(practiceId);
//                if (practiceType != null) {
//                    vacationHomeworkPractices.put(String.valueOf(practiceId), practiceType.getPracticeName());
//                }
//            }
//        }
//        model.addAttribute("vacationHomeworkPractices", vacationHomeworkPractices);
//
//        User user = userLoaderClient.loadUser(userId);
//        model.addAttribute("user", user);
//        return "crm/student/studentvacationhomework";
        return redirect("/crm/student/studentlist.vpage");
    }

    @RequestMapping(value = "studentvacationhomeworkpackage.vpage", method = RequestMethod.GET)
    public String studentVacationHomeworkPackage(@RequestParam("groupId") Long groupId, @RequestParam("packageId") Long packageId, @RequestParam("subject") String subject, Model model) {
//        HomeworkType homeworkType = null;
//        if ("ENGLISH".equals(subject)) {
//            homeworkType = HomeworkType.VACATION_ENGLISH;
//        } else if ("MATH".equals(subject)) {
//            homeworkType = HomeworkType.VACATION_MATH;
//        }
//        List<StudentVacationHomeworkPackageAccomplishment> homeworkPackageAccomplishments = Collections.emptyList();
//        if (homeworkType != null) {
//            List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(groupId);
//            if (!CollectionUtils.isEmpty(studentIds)) {
//                Map<Long, StudentVacationHomeworkPackageAccomplishment> studentAccomplishmentMap = homeworkManagement
//                        .findStudentVacationHomeworkPackageAccomplishmentsByStudents(studentIds, packageId, homeworkType);
//                if (MapUtils.isNotEmpty(studentAccomplishmentMap)) {
//                    homeworkPackageAccomplishments = new ArrayList<>(studentAccomplishmentMap.values());
//                }
//            }
//        }
//        model.addAttribute("homeworkPackageAccomplishments", homeworkPackageAccomplishments);
//        model.addAttribute("packageId", packageId);
//        return "crm/student/studentvacationhomeworkpackage";
        return redirect("/crm/student/studentlist.vpage");
    }


    @RequestMapping(value = "updateStudentAccountStatus.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateStudentAccountStatus() {
        Long studentId = getRequestLong("studentId");

        String status = getRequestParameter("status", "");

        if (studentId == 0 || "".equals(status)) {
            return MapMessage.errorMessage("用户参数错误");
        }
        //学生账号状态AccountStatus.PAUSE暂时没使用，如果使用要添加判断
        if (AccountStatus.FORBIDDEN.toString().equals(status)) {//封禁
            return studentServiceClient.forbidStudent(studentId, true);
        } else if (AccountStatus.FREEZING.toString().equals(status)) {//冻结
            return studentServiceClient.freezeStudent(studentId, true);
        } else if (AccountStatus.NORMAL.toString().equals(status)) {
            if (studentLoaderClient.isStudentForbidden(studentId)) {
                return studentServiceClient.forbidStudent(studentId, false);
            } else if (studentLoaderClient.isStudentFreezing(studentId)) {
                return studentServiceClient.freezeStudent(studentId, false);
            } else {
                return MapMessage.successMessage();
            }
        }

        return MapMessage.errorMessage("操作失败");
    }


    @RequestMapping(value = "getStudentAccountStatus.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getStudentAccountStatus() {
        long studentId = getRequestLong("studentId");
        if (studentId == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        if (studentExtAttribute != null) {
            if (studentExtAttribute.isForbidden()) {
                return MapMessage.successMessage().add("status", "forbid");
            } else if (studentExtAttribute.isFreezing()) {
                return MapMessage.successMessage().add("status", "freeze");
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 获得退款的手机号
     *
     * @return
     */
    @RequestMapping(value = "getRefundPhone.vpage")
    @ResponseBody
    public MapMessage getRefundPhone() {
        Long stuId = getRequestLong("stuId");

        Student stu = studentLoaderClient.loadStudent(stuId);
        if (stu == null)
            return MapMessage.errorMessage("学生不存在!");

        UserAuthentication ua = crmRefundService.getUserRemindUA(stuId);
        String phone = "";
        if (ua != null) {
            phone = sensitiveUserDataServiceClient.showUserMobile(ua.getId(), "getRefundPhone", SafeConverter.toString(ua.getId()));
        }

        addAdminLog("queryUserMobile", stuId, StringUtils.mobileObscure(phone), "crm", "ID:" + stuId + ", phone:" + StringUtils.mobileObscure(phone));

        return MapMessage.successMessage().add("phone", phone);
    }

    /**
     * 封禁学生
     *
     * @return
     */
    @RequestMapping(value = "forbidstudent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage forbidStudent() {
        Long studentId = getRequestLong("studentId");
        String forbidReason = getRequestString("forbidReason");
        if (studentId == null) {
            return MapMessage.errorMessage("学生信息不正确");
        }
        if (StringUtils.isBlank(forbidReason)) {
            return MapMessage.errorMessage("请输入原因");
        }
        MapMessage mapMessage = userServiceClient.forbidUser(studentId);
        if (mapMessage.isSuccess()) {
            // 清空sessionKey 强制退出
//            serviceClient.expireSessionKey("17Student", studentId, SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), studentId));
            // 记录操作日志
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(studentId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent(forbidReason);
            userServiceRecord.setComments("封禁学生");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }
        return mapMessage;
    }

    /**
     * 解封学生
     *
     * @return
     */
    @RequestMapping(value = "unforbidstudent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage unForbidStudent() {
        Long studentId = getRequestLong("studentId");
        String forbidReason = getRequestString("forbidReason");
        if (studentId == null) {
            return MapMessage.errorMessage("学生信息不正确");
        }
        if (StringUtils.isBlank(forbidReason)) {
            return MapMessage.errorMessage("请输入原因");
        }
        MapMessage mapMessage = userServiceClient.unForbidUser(studentId);
        if (mapMessage.isSuccess()) {
            // 记录操作日志
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(studentId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent(forbidReason);
            userServiceRecord.setComments("解封学生");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }
        return mapMessage;
    }

    @RequestMapping(value = "studenthistorycredit.vpage", method = RequestMethod.GET)
    public String studentHistoryCredit(Model model) {
        Long studentId = getRequestLong("studentId");
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student != null) {
            Integer creditListSize = getRequestInt("number", 20);
            List<CreditHistory> creditHistories = creditLoaderClient.getCreditLoader().loadCreditHistories(studentId);
            if (creditHistories.size() > creditListSize) {
                creditHistories = creditHistories.subList(0, creditListSize);
            }
            model.addAttribute("creditHistories", creditHistories);
        }
        return "crm/student/studentcredit";
    }

    @RequestMapping(value = "studentrecords.vpage", method = RequestMethod.GET)
    public String studentRecords(Model model) {
        Long studentId = getRequestLong("studentId");
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student != null) {
            Integer recordsSize = getRequestInt("number", 20);
            List<UserServiceRecord> userServiceRecords = userLoaderClient.loadUserServiceRecords(studentId, recordsSize);
            model.addAttribute("customerServiceRecordList", userServiceRecords);
        }
        return "crm/student/studentrecords";
    }

    @RequestMapping(value = "activity.vpage", method = RequestMethod.GET)
    public String activity(Model model) {
        Long userId = getRequestLong("userId");
        MapMessage mapMessage = studentActivityServiceClient.loadCanParticipateActivity(userId);
        model.addAttribute("datas", mapMessage.get("data"));
        model.addAttribute("studentId", userId);
        return "crm/student/activity/index";
    }

    @ResponseBody
    @RequestMapping(value = "loadCanParticipateActivity.vpage", method = RequestMethod.GET)
    public MapMessage loadCanParticipateActivity() {
        Long studentId = getRequestLong("studentId");
        return studentActivityServiceClient.loadCanParticipateActivity(studentId);
    }

    @ResponseBody
    @RequestMapping(value = "addActivityOpportunity.vpage", method = RequestMethod.POST)
    public MapMessage addActivityOpportunity() {
        String activityId = getRequestString("activityId");
        String studentId = getRequestString("studentId");
        if (StringUtils.isEmpty(activityId) || StringUtils.isEmpty(studentId)) {
            return MapMessage.errorMessage("参数错误");
        }
        return studentActivityServiceClient.addActivityOpportunity(activityId, SafeConverter.toLong(studentId));
    }
}
