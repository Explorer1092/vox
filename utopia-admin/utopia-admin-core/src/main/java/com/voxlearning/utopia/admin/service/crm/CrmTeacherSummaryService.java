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

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.dao.finance.WirelessChargingPersistence;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;
import com.voxlearning.utopia.entity.crm.CrmTaskRecord;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.ucenter.CertificationApplicationOperatingLog;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.business.consumer.CertificationManagementClient;
import com.voxlearning.utopia.service.certification.client.CertificationServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2015/11/2.
 */

@Named
@Slf4j
public class CrmTeacherSummaryService extends AbstractAdminService {

//    private static final String HOMEWORK_SUM_SQL = "SELECT MIN(CREATETIME) AS FIRST_USE_TIME,MAX(CREATETIME) AS LATEST_USE_TIME ,MIN(CHECKED_TIME) AS FIRST_CHECK_TIME FROM VOX_HOMEWORK " +
//            "WHERE TEACHER_ID = ? AND DISABLED=FALSE";
//    private static final String MATH_HOMEWORK_SUM_SQL = "SELECT  MIN(CREATE_DATETIME) AS FIRST_USE_TIME,MAX(CREATE_DATETIME) AS LATEST_USE_TIME, MIN(CHECKED_TIME) AS FIRST_CHECK_TIME FROM MATH_HOMEWORK " +
//            "WHERE TEACHER_ID = ? AND DISABLED=FALSE";
//    private static final String QUIZ_SUM_SQL = "SELECT MIN(CREATE_DATETIME) AS FIRST_USE_TIME, MAX(CREATE_DATETIME) AS LATEST_USE_TIME,MIN(CHECKED_TIME) AS FIRST_CHECK_TIME FROM VOX_QUIZ " +
//            "WHERE TEACHER_ID = ? AND DISABLED=FALSE";

    private static final String MIDDLE_SCHOOL_HOMEWORK_COUNT_API_TEST = "http://zx2.test.17zuoye.net/extend/teacherHomework";
    private static final String MIDDLE_SCHOOL_HOMEWORK_COUNT_API_STAGING = "http://zx.staging.17zuoye.net/extend/teacherHomework";
    private static final String MIDDLE_SCHOOL_HOMEWORK_COUNT_API_RELEASE = "http://zx.17zuoye.com/extend/teacherHomework";

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;

    @Inject private WechatLoaderClient wechatLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private CrmTaskService crmTaskService;
    @Inject private CertificationManagementClient certificationManagementClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private CertificationServiceClient certificationServiceClient;

    @Inject private WirelessChargingPersistence wirelessChargingPersistence;

    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;

    public List<CrmTeacherSummary> getTeacherSummaryListByTeacherIds(Collection<Long> teacherIds) {
        Map<Long, CrmTeacherSummary> teacherSummaries = crmSummaryLoaderClient.loadTeacherSummary(teacherIds);
        return new ArrayList<>(teacherSummaries.values());
    }

    public List<CrmTeacherSummary> getTeacherSummaryListByTeacherIds(Collection<Long> teacherIds, SchoolLevel schoolLevel) {
        Map<Long, CrmTeacherSummary> teacherSummaries = crmSummaryLoaderClient.loadTeacherSummary(teacherIds);
        return teacherSummaries.values()
                .stream()
                .filter(p -> schoolLevel == null || schoolLevel.name().equals(p.getSchoolLevel()))
                .collect(Collectors.toList());
    }

    public CrmTeacherSummary getCrmTeacherSummary(String teacherKey, String type) {
        CrmTeacherSummary teacherSummary = null;
        if (StringUtils.equalsIgnoreCase(type, "teacherId")) {
            teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(ConversionUtils.toLong(teacherKey));
        } else if (StringUtils.equalsIgnoreCase(type, "mobile")) {
            teacherSummary = crmSummaryLoaderClient.loadTeacherSummaryByMobile(teacherKey);
        }

        //根据teacherId和teacherMobile查  teacherSummary如果没有 直接查线上
        if (teacherSummary == null) {
            TeacherDetail teacherDetail = null;
            if (StringUtils.equalsIgnoreCase(type, "teacherId")) {
                teacherDetail = teacherLoaderClient.loadTeacherDetail(ConversionUtils.toLong(teacherKey));
            } else if (StringUtils.equalsIgnoreCase(type, "mobile")) {
                List<User> userList = userLoaderClient.loadUsers(teacherKey, UserType.TEACHER);
                if (userList != null && userList.size() == 1) {
                    teacherDetail = teacherLoaderClient.loadTeacherDetail(userList.get(0).getId());
                }
            }
            if (teacherDetail != null) {
                teacherSummary = copyTeacherDetailToSummary(teacherDetail);
            }
        }
        return teacherSummary;
    }

    public Map<String, Object> getTeacherInfoMap(Long teacherId) {
        Map<String, Object> teacherInfoMap = new HashMap<>();
        if (teacherId == null) {
            return teacherInfoMap;
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (null == teacher || teacher.getDisabled()) {
            return teacherInfoMap;
        }

        teacherInfoMap.put("teacher", teacher);
        teacherInfoMap.put("id", teacher.getId());
        //性别
        teacherInfoMap.put("sex", Gender.fromCode(teacher.getProfile().getGender()).getDescription());
        //出生年月
        teacherInfoMap.put("birthYear", teacher.getProfile().getYear());
        teacherInfoMap.put("birthMonth", teacher.getProfile().getMonth());
        teacherInfoMap.put("birthDay", teacher.getProfile().getDay());
        //邮箱
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
        teacherInfoMap.put("email", ua.getSensitiveEmail());
        //QQ号
        teacherInfoMap.put("qq", ua.getSensitiveQq());
        //园丁豆
        teacherInfoMap.put("integral", teacher.getUserIntegral().getUsable());
        //通信地址
        UserShippingAddress shippingAddresses = MiscUtils.firstElement(userLoaderClient.loadUserShippingAddresses(Collections.singleton(teacherId)).values().stream().collect(Collectors.toList()));
        if (shippingAddresses != null) {
            teacherInfoMap.put("shippingAddresses", shippingAddresses.getDetailAddress());
        }

        //排假直接从teacherSummary里面取
        CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacherId);
        if (teacherSummary != null) {
            teacherInfoMap.put("fakeTeacher", SafeConverter.toBoolean(teacherSummary.getFakeTeacher()));
            if (SafeConverter.toBoolean(teacherSummary.getFakeTeacher()) && teacherSummary.getValidationType() != null) {
                CrmTeacherFakeValidationType validationType = CrmTeacherFakeValidationType.get(teacherSummary.getValidationType());
                if (validationType != null) {
                    teacherInfoMap.put("validationType", validationType.getDesc());
                } else {
                    teacherInfoMap.put("validationType", "");
                }
                teacherInfoMap.put("fakeDesc", teacherSummary.getFakeDesc());
            }
            teacherInfoMap.put("authStudentCount", teacherSummary.getAuthStudentCount());
            teacherInfoMap.put("totalHomeworkCount", teacherSummary.getTotalHomeworkCount());
            teacherInfoMap.put("latestUseTime", teacherSummary.getLatestAssignHomeworkTime());
        }
        //名下班级数
        List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId);
        teacherInfoMap.put("clazzCount", CollectionUtils.isEmpty(clazzList) ? 0 : clazzList.size());
        //注册时间
        teacherInfoMap.put("registerTime", DateUtils.dateToString(teacher.getCreateTime(), "yyyy-MM-dd"));
        //注册方式
        teacherInfoMap.put("regType", getUserRegType(teacher.getWebSource(), teacher).getDesc());
        //注册邀请人
        List<InviteHistory> inviteHistoryList = asyncInvitationServiceClient.loadByInvitee(teacherId).toList();
        if (CollectionUtils.isNotEmpty(inviteHistoryList)) {
            User inviter = userLoaderClient.loadUser(inviteHistoryList.get(0).getUserId());
            if (inviter != null) {
                teacherInfoMap.put("inviterName", inviter.getProfile().getRealname());
                teacherInfoMap.put("inviterId", inviter.getId());
            }
        }
        //首次建班时间没有了。现在用首次加入班级时间
        // FIXME 貌似没有使用。。。
//        List<ClazzTeacherRef> clazzTeacherRefs = teacherLoaderClient.loadTeacherClazzRefs(Collections.singleton(teacherId)).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
//        teacherInfoMap.put("firstJoinClazzTime", "");
//        if (CollectionUtils.isNotEmpty(clazzTeacherRefs)) {
//            Collections.sort(clazzTeacherRefs, new Comparator<ClazzTeacherRef>() {
//                @Override
//                public int compare(ClazzTeacherRef o1, ClazzTeacherRef o2) {
//                    return o1.getCreateDatetime().compareTo(o2.getCreateDatetime());
//                }
//            });
//            teacherInfoMap.put("firstJoinClazzTime", clazzTeacherRefs.get(0).getCreateDatetime());
//        }
        //首次使用时间--首次检查时间-最近使用时间
        //作业
//        Map<String, Object> homework = new HashMap<>();
//        if (teacher.getSubject() == Subject.ENGLISH) {
//            homework = utopiaSql.withSql(HOMEWORK_SUM_SQL).useParamsArgs(teacherId).queryRow();
//        } else if (teacher.getSubject() == Subject.MATH) {
//            homework = utopiaSql.withSql(MATH_HOMEWORK_SUM_SQL).useParamsArgs(teacherId).queryRow();
//        }
//        //测验
//        Map<String, Object> quizMap = utopiaSql.withSql(QUIZ_SUM_SQL).useParamsArgs(teacherId).queryRow();
//        if (homework.size() > 0 && quizMap.size() > 0) {
//            teacherInfoMap.put("firstUseTime", ConversionUtils.toString(homework.get("FIRST_USE_TIME")).compareTo(ConversionUtils.toString(quizMap.get("FIRST_USE_TIME"))) < 0 ? homework.get("FIRST_USE_TIME") : quizMap.get("FIRST_USE_TIME"));
//            teacherInfoMap.put("firstCheckTime", ConversionUtils.toString(homework.get("FIRST_CHECK_TIME")).compareTo(ConversionUtils.toString(quizMap.get("FIRST_CHECK_TIME"))) < 0 ? homework.get("FIRST_CHECK_TIME") : quizMap.get("FIRST_CHECK_TIME"));
//        } else if (homework.size() > 0) {
//            teacherInfoMap.put("firstUseTime", homework.get("FIRST_USE_TIME"));
//            teacherInfoMap.put("firstCheckTime", homework.get("FIRST_CHECK_TIME"));
//        } else if (quizMap.size() > 0) {
//            teacherInfoMap.put("firstUseTime", quizMap.get("FIRST_USE_TIME"));
//            teacherInfoMap.put("firstCheckTime", quizMap.get("FIRST_CHECK_TIME"));
//        }

        teacherInfoMap.put("isAuth", teacher.fetchCertificationState() == AuthenticationState.SUCCESS);
        //认证时间
        teacherInfoMap.put("authTime", teacher.getLastAuthDate() != null ? DateUtils.dateToString(teacher.getLastAuthDate(), "yyyy-MM-dd") : "");
        //认证方式
        if (teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
            List<CertificationApplicationOperatingLog> authLogs = certificationServiceClient.getRemoteReference()
                    .findCertificationApplicationOperatingLogs(teacherId)
                    .getUninterruptibly();
            if (CollectionUtils.isNotEmpty(authLogs)) {
                CertificationApplicationOperatingLog authLog = authLogs.get(authLogs.size() - 1);
                String authType = "自动认证".equals(authLog.getOperatorName()) ? "自动认证" : "人工认证(" + authLog.getOperatorName() + ")";
                teacherInfoMap.put("authType", authType);
            }
        } else {
            teacherInfoMap.put("authType", "");
        }

        School school = raikouSystem.loadSchool(teacher.getTeacherSchoolId());
        //authCond1Reached
        //如果是中学
        if (school != null && school.getLevel() != null && SchoolLevel.MIDDLE.equals(SchoolLevel.safeParse(school.getLevel()))) {
            Integer teacherHomeworkFinishCount = getMiddleSchoolTeacherHomework(Collections.singletonList(teacherId)).get(teacherId);
            teacherInfoMap.put("authCond1Reached", teacherHomeworkFinishCount != null && teacherHomeworkFinishCount >= 8);
        } else {
            if (certificationManagementClient.getRemoteReference().hasEnoughStudentsFinishedHomework(teacherId)) {
                teacherInfoMap.put("authCond1Reached", true);
            }
        }
        //authCond2Reached
        if (StringUtils.isNotBlank(teacher.fetchRealname())) {
            //fixe 2个uct_user记录的mobile相同,但UCT_USER_AUTHENTICATION只有一条记录
            //不判断id的话老师详情页会把两个老师的认证二都标记已完成

            //最终确定查询方式为:用teacherId查UserAuthentication记录。存在记录且手机号不为空即为绑定了手机
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(teacherId);
            if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {
                teacherInfoMap.put("authCond2Reached", true);
            }
        }
        //authCond3Reached
        if (certificationManagementClient.getRemoteReference().hasEnoughStudentsBindParentMobileOrBindSelfMobile(teacherId)) {
            teacherInfoMap.put("authCond3Reached", true);
        }
        //活跃等级和分数
        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacher.getId());
        teacherInfoMap.put("level", extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getLevel()));
        teacherInfoMap.put("levelValue", extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getLevelValue()));
        //学校地区
        if (school != null) {
            ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
            if (exRegion != null) {
                teacherInfoMap.put("schoolProvinceName", exRegion.getProvinceName());
                teacherInfoMap.put("schoolCityName", exRegion.getCityName());
                teacherInfoMap.put("schoolCountyName", exRegion.getCountyName());
            }
        }
        //是否是校园大使
        teacherInfoMap.put("schoolAmbassador", teacher.isSchoolAmbassador());
        //校园大使时间
        if (teacher.isSchoolAmbassador()) {
            AmbassadorSchoolRef ambassadorSchoolRef = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(teacherId)
                    .stream().findFirst().orElse(null);
            if (ambassadorSchoolRef != null) {
                teacherInfoMap.put("ambassadorTime", ambassadorSchoolRef.getCreateDatetime());
            }
        }
        //微信绑定
        List<UserWechatRef> wechatRefList = wechatLoaderClient.loadUserWechatRefs(Collections.singletonList(teacherId), WechatType.TEACHER)
                .values().stream().flatMap(Collection::stream).filter(p -> !p.isDisabledTrue()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(wechatRefList)) {
            Collections.sort(wechatRefList, (o1, o2) -> o1.getCreateDatetime().compareTo(o2.getCreateDatetime()));
            teacherInfoMap.put("wechatBinded", true);
            teacherInfoMap.put("wechatBindedTime", wechatRefList.get(0).getCreateDatetime());
        }
        // 是否使用APP by wyc 2016-05-06
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Teacher", teacherId);
        if (vendorAppsUserRef != null) {
            teacherInfoMap.put("appUsed", true);
            teacherInfoMap.put("appCreateTime", vendorAppsUserRef.getCreateTime());
        }

        //最近登录时间
//        Date lastLoginTime = userLoaderClient.findUserLastLoginTime(teacher);
        Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(teacher.getId());
        if (lastLoginTime != null) {
            teacherInfoMap.put("latestLoginTime", DateUtils.dateToString(lastLoginTime, "yyyy/MM/dd"));
        } else {
            teacherInfoMap.put("latestLoginTime", "");
        }

        //最近维护时间
//        Date latestUpdateTime = crmTaskRecordBackupDao.findLatestUpdateTime(teacherId);
//        teacherInfoMap.put("latestUpdateTime", latestUpdateTime);
//        List<CustomerServiceRecord> customerServiceRecordList = customerServiceRecordPersistence.getCustomerServiceRecord(teacherId);
        List<UserServiceRecord> userServiceRecords = userLoaderClient.loadUserServiceRecords(teacherId);
        teacherInfoMap.put("customerServiceRecordList", userServiceRecords);

        return teacherInfoMap;
    }

    /**
     * 微信绑定记录
     *
     * @param userId
     * @return
     */
    public List<WirelessCharging> getWirelessChargingList(Long userId) {
        Criteria criteria = new Criteria();
        if (SafeConverter.toLong(userId) > 0) {
            criteria = criteria.and("USER_ID").is(userId);
        }
        criteria = criteria.and("STATUS").is(2);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return wirelessChargingPersistence.query(Query.query(criteria).with(sort));
    }

    /**
     * 新版老师班级列表
     *
     * @param teacherId
     * @return
     */
    public Map<String, Object> getTeacherClazzGroupInfo(Long teacherId) {
        Map<String, Object> teacherClazzMapInfo = new HashMap<>();
        List<GroupMapper> groupMapperList = groupLoaderClient.loadTeacherGroupsByTeacherId(teacherId, false);
        if (!CollectionUtils.isEmpty(groupMapperList)) {
            List<Map> clazzList = new ArrayList<>();
            List<Map> groupNotInClazzList = new ArrayList<>();
            for (GroupMapper mapper : groupMapperList) {
                if (mapper != null) {
                    //判断老师和班级的关系，为了兼容老的数据关系
                    if (teacherLoaderClient.isTeachingClazz(teacherId, mapper.getClazzId())) {
                        Map<String, Object> clazzMap = new HashMap<>();
                        Clazz clazzInfo = raikouSDK.getClazzClient()
                                .getClazzLoaderClient()
                                .loadClazz(mapper.getClazzId());
                        if (clazzInfo == null) {
                            continue;
                        }
                        clazzMap.put("groupId", mapper.getId());

                        clazzMap.put("id", clazzInfo.getId());
                        clazzMap.put("classLevel", clazzInfo.getClassLevel());
                        clazzMap.put("className", clazzInfo.formalizeClazzName());
                        // Task #29850 增加学制 By Wyc 2016-08-19
                        clazzMap.put("eduSys", clazzInfo.getEduSystem() != null ? clazzInfo.getEduSystem().getDescription() : "");
                        clazzList.add(clazzMap);
                    } else {
                        Map<String, Object> tmpMap = new HashMap<>();
                        Clazz clazzInfo = raikouSDK.getClazzClient()
                                .getClazzLoaderClient()
                                .loadClazz(mapper.getClazzId());
                        if (clazzInfo == null) {
                            continue;
                        }
                        tmpMap.put("groupId", mapper.getId());
                        tmpMap.put("id", clazzInfo.getId());
                        tmpMap.put("classLevel", clazzInfo.getClassLevel());
                        tmpMap.put("className", clazzInfo.getClassName());
                        // Task #29850 增加学制 By Wyc 2016-08-19
                        tmpMap.put("eduSys", clazzInfo.getEduSystem() != null ? clazzInfo.getEduSystem().getDescription() : "");
                        groupNotInClazzList.add(tmpMap);
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

            teacherClazzMapInfo.put("clazzLevelList", clazzLevelMap.values());
            teacherClazzMapInfo.put("groupNotInClazzLevelList", groupNotInClazzLevelMap.values());
        }
        return teacherClazzMapInfo;
    }

    /**
     * 新版老师查询列表中老师呼叫记录
     *
     * @param teacherIds
     * @return
     */
    public Map<String, Map<String, Object>> getTeacherCCRecordInfo(Collection<Long> teacherIds) {
        if (CollectionUtils.isEmpty(teacherIds)) {
            return new HashMap<>();
        }
        Set<Long> teacherIdSet = new HashSet<>(teacherIds);

        Map<String, Map<String, Object>> map = new HashMap<>();
        Map<Long, List<CrmTaskRecord>> recordListMap = crmTaskService.loadUserTaskRecords(teacherIdSet);
        for (Long id : teacherIdSet) {
            Map<String, Object> teacherMap = new HashMap<>();
            teacherMap.put("latestConnectedTime", "");
            teacherMap.put("latestOutCallTime", "");
            teacherMap.put("latestOutCallUser", "");
            teacherMap.put("outCallCount", 0);
            List<CrmTaskRecord> list = recordListMap.get(id);
            if (CollectionUtils.isNotEmpty(list)) {
                Collections.sort(list, new Comparator<CrmTaskRecord>() {
                    @Override
                    public int compare(CrmTaskRecord o1, CrmTaskRecord o2) {
                        return SafeConverter.toInt(DateUtils.dayDiff(o2.getCreateTime(), o1.getCreateTime()));
                    }
                });
                //latestConnectedTime
                CrmTaskRecord connectedRecord = list.stream().filter(p -> CrmTaskRecordCategory.接通.equals(p.getFirstCategory()) && CrmContactType.电话呼出.equals(p.getContactType())).findFirst().orElse(null);
                if (connectedRecord != null) {
                    teacherMap.put("latestConnectedTime", DateUtils.dateToString(connectedRecord.getCreateTime(), "yyyy-MM-dd"));
                }
                //latestOutCallTime
                List<CrmTaskRecord> phoneCallList = list.stream().filter(p -> CrmContactType.电话呼出.equals(p.getContactType())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(phoneCallList)) {
                    CrmTaskRecord phoneCallRecord = phoneCallList.get(0);
                    teacherMap.put("latestOutCallTime", DateUtils.dateToString(phoneCallRecord.getCreateTime(), "yyyy-MM-dd"));
                    teacherMap.put("latestOutCallUser", phoneCallRecord.getRecorderName());
                    Date today = new Date();
                    //如果最新的记录是在当天，才会有当日呼叫次数，否则为0
                    if (DateUtils.dayDiff(phoneCallRecord.getCreateTime(), today) == 0) {
                        teacherMap.put("outCallCount", phoneCallList.stream().filter(p -> DateUtils.dayDiff(p.getCreateTime(), today) == 0).collect(Collectors.toList()).size());
                    }

                }
            }
            map.put(ConversionUtils.toString(id), teacherMap);
        }
        return map;
    }

    /**
     * 获取新版老师查询列表页的信息
     * 一部分从teacherDetail取
     * 一部分从teacherSummray取
     *
     * @param teacherIds
     * @return
     */
    public List<Map<String, Object>> generateTeacherDetailMap(Collection<Long> teacherIds, SchoolLevel schoolLevel, boolean preciseSearch) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isEmpty(teacherIds)) {
            return mapList;
        }
        Map<Long, TeacherDetail> teacherDetailMap = teacherLoaderClient.loadTeacherDetails(teacherIds);
        if (MapUtils.isEmpty(teacherDetailMap)) {
            return mapList;
        }

        //只有一种情况，一定不查询中学信息，就是非精准查询，并且没有选小学
        //上面的注释是不是有BUG，应该是下面的意思：
        //只有一种情况，一定不查询中学信息，就是非精准查询，并且【选了】小学
        boolean possibleSearchMiddleSchool = preciseSearch || SchoolLevel.JUNIOR != schoolLevel;
        /*
        如果是精准查询（用teacherId或mobile）或是没有选择学校类别，需要查出老师所在学校的学校类别，以保证精准查询方式的情况下学校类别选择
        错误导致查询信息有误的情况。
        例如：使用11378查询老师（老师所在学校本身是小学），但操作人员误选择中学，导致老师被查出，但认证信息是中学判断出来的认证信息
         */
        Map<Long, SchoolLevel> juniorSchoolLevelMap = new HashMap<>();

        //只有一种情况不需要查中学，就是条件查询，并选择小学类别的情况
        Map<Long, Integer> teacherHomeworkFinishCountMap = new HashMap<>();
        if (possibleSearchMiddleSchool) {
            Set<Long> schoolIds = teacherDetailMap.values().stream().map(TeacherDetail::getTeacherSchoolId).collect(Collectors.toSet());
            Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);
            juniorSchoolLevelMap = schoolMap.values().stream().collect(Collectors.toMap(School::getId, s -> SchoolLevel.safeParse(s.getLevel(), SchoolLevel.JUNIOR)));
            teacherHomeworkFinishCountMap = getMiddleSchoolTeacherHomework(teacherIds);
        }

        Collection<TeacherDetail> teacherDetailList = teacherDetailMap.values();
        //小学8人3次
        Map<Long, Boolean> teacherAuthCondReachedMap = certificationManagementClient.getRemoteReference()
                .hasEnoughStudentsFinishHomeworkByTeacherIds(teacherIds);
        //绑定手机
        Map<Long, Boolean> teacherAuthCond3ReachedMap = certificationManagementClient.getRemoteReference()
                .hasEnoughStudentsBindParentMobileOrStudentsBindSelfMobile(teacherIds);
        Map<Long, UserAuthentication> userAuthenticationMap = userLoaderClient.loadUserAuthentications(teacherIds);
        List<CrmTeacherSummary> teacherSummaryList = getTeacherSummaryListByTeacherIds(teacherIds, schoolLevel);
        Map<Long, CrmTeacherSummary> teacherSummaryMap = new HashMap<>();
        teacherSummaryList.stream().forEach(p -> teacherSummaryMap.put(p.getTeacherId(), p));
        for (TeacherDetail detail : teacherDetailList) {
            Map<String, Object> teacherMap = new HashMap<>();
            teacherMap.put("teacherName", "");
            teacherMap.put("authStatus", "");
            teacherMap.put("authCond1Reached", "");
            teacherMap.put("authCond2Reached", "");
            teacherMap.put("authCond3Reached", "");
            teacherMap.put("fakeTeacher", "");
            teacherMap.put("fakeDesc", "");
            teacherMap.put("mobile", "");
            teacherMap.put("subject", "");
            teacherMap.put("schoolName", "");
            teacherMap.put("latestAssignHomeworkTime", "");
            if (detail != null) {
                CrmTeacherSummary teacherSummary = teacherSummaryMap.get(detail.getId());
                teacherMap.put("teacherId", detail.getId());
                teacherMap.put("teacherName", detail.getProfile() == null ? "" : detail.getProfile().getRealname());
                teacherMap.put("authStatus", detail.fetchCertificationState());
                SchoolLevel realSchoolLevel;
                //如果可能查中学则进入判断，否则直接查小学信息
                if (possibleSearchMiddleSchool) {
                    //非精准查询并且条件查询中学情况下直接调用中学接口,否则查出老师所在学校真实类别
                    if (!preciseSearch && SchoolLevel.MIDDLE == schoolLevel) {
                        realSchoolLevel = schoolLevel;
                    } else {
                        realSchoolLevel = juniorSchoolLevelMap.containsKey(detail.getTeacherSchoolId()) ? juniorSchoolLevelMap.get(detail.getTeacherSchoolId()) : SchoolLevel.JUNIOR;
                    }
                } else {
                    realSchoolLevel = SchoolLevel.JUNIOR;
                }
                boolean authCond1Reached;
                if (realSchoolLevel == SchoolLevel.JUNIOR) {
                    authCond1Reached = teacherAuthCondReachedMap.get(detail.getId());
                } else {
                    Integer teacherHomeworkFinishCount = teacherHomeworkFinishCountMap.containsKey(detail.getId()) ? teacherHomeworkFinishCountMap.get(detail.getId()) : 0;
                    authCond1Reached = teacherHomeworkFinishCount >= 8;
                }
                teacherMap.put("authCond1Reached", authCond1Reached);
                teacherMap.put("authCond3Reached", teacherAuthCond3ReachedMap.get(detail.getId()));

                if (StringUtils.isNotBlank(detail.fetchRealname())) {
                    UserAuthentication userAuthentication = userAuthenticationMap.get(detail.getId());
                    if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {
                        teacherMap.put("authCond2Reached", true);
                    }
                }

                if (teacherSummary != null) {
                    teacherMap.put("fakeTeacher", teacherSummary.getFakeTeacher());
                    teacherMap.put("fakeDesc", teacherSummary.getFakeDesc());
                    teacherMap.put("mobile", teacherSummary.getSensitiveMobile());
                    teacherMap.put("latestAssignHomeworkTime", teacherSummary.getLatestAssignHomeworkTime() == null ? "" : DateUtils.dateToString(new Date(teacherSummary.getLatestAssignHomeworkTime()), "yyyy-MM-dd"));
                }
                teacherMap.put("subject", detail.getSubject() == null ? "" : detail.getSubject().getValue());
                teacherMap.put("schoolName", detail.getTeacherSchoolName());
                teacherMap.put("registerTime", detail.getCreateTime());
            }
            mapList.add(teacherMap);
        }
        //按注册时间倒序
        Collections.sort(mapList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                String sn1 = SafeConverter.toString(o1.get("schoolName"), "");
                String sn2 = SafeConverter.toString(o2.get("schoolName"), "");
                return sn1.compareTo(sn2);
            }
        });
        return mapList;
    }


    /**
     * 当天注册的老师在CrmTeacherSummary不存在。手动生成summary返回前台
     *
     * @param detail
     * @return
     */
    private CrmTeacherSummary copyTeacherDetailToSummary(TeacherDetail detail) {
        CrmTeacherSummary teacherSummary = new CrmTeacherSummary();
        teacherSummary.setTeacherId(detail.getId());
        teacherSummary.setRealName(detail.getProfile().getRealname());
//        teacherSummary.setGender(Gender.fromCode(detail.getProfile().getGender()).name());

        //FIXME: 这里不需要真实手机号吧?
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(detail.getId());
        teacherSummary.setSensitiveMobile(ua.getSensitiveMobile());

//        teacherSummary.setSensitiveEmail(detail.getProfile().getSensitiveEmail());
//        teacherSummary.setBirthday(ConversionUtils.toString(detail.getProfile().getYear()) + ConversionUtils.toString(detail.getProfile().getMonth()) + ConversionUtils.toString(detail.getProfile().getDay()));
//        teacherSummary.setSensitiveQq(detail.getProfile().getSensitiveQq());
        teacherSummary.setSchoolName(detail.getTeacherSchoolName());
        teacherSummary.setSchoolId(detail.getTeacherSchoolId());
        teacherSummary.setProvinceCode(detail.getRegionCode());
        teacherSummary.setProvinceName(detail.getRootRegionName());
        teacherSummary.setCityCode(detail.getCityCode());
        teacherSummary.setCityName(detail.getCityName());
        teacherSummary.setCountyCode(detail.getRegionCode());
        teacherSummary.setCountyName(detail.getCountyName());
//        teacherSummary.setAmbassador(detail.isSchoolAmbassador());
        return teacherSummary;
    }

    private CrmTeacherWebSourceCategoryType getUserRegType(String webSource, TeacherDetail teacherDetail) {
        // TODO 好蛋疼啊。在这定义了这些KEY---哦也
        // 第三方注册
        final List<String> APP_REG_KEYS = new ArrayList<>();
        OrderProductServiceType[] productServiceTypes = OrderProductServiceType.values();
        for (OrderProductServiceType productServiceType : productServiceTypes) {
            APP_REG_KEYS.add(productServiceType.name());
        }
        APP_REG_KEYS.add("oap.91.com");
        APP_REG_KEYS.add("www.edures.bjedu.cn");
        APP_REG_KEYS.add("picturetalk");
        APP_REG_KEYS.add("studycraft");
        APP_REG_KEYS.add("timeep");

        // 批量注册
        final List<String> BATCH_REG_KEYS = new ArrayList<>();
        BATCH_REG_KEYS.add(UserWebSource.keti.getSource());
        BATCH_REG_KEYS.add(UserWebSource.crm_batch.getSource());
        // 邀请注册
        //直接用UCT_USER的is_invite字段

        //教务注册
        final List<String> JIAOWU_REG_KEYS = new ArrayList<>();
        JIAOWU_REG_KEYS.add(UserWebSource.affair_batch.getSource());

        //pc主站
        final List<String> PC_REG_KEYS = new ArrayList<>();
        PC_REG_KEYS.add("17zuoye.com");
        PC_REG_KEYS.add("www.17zuoye.com");
        PC_REG_KEYS.add("email");
        PC_REG_KEYS.add("mobile");
        PC_REG_KEYS.add("web_self_reg");

        //微信注册
        final List<String> WECHAT_REG_KEYS = new ArrayList<>();
        WECHAT_REG_KEYS.add("wechat");

        //o2o扫码
        final List<String> QORCODE_OTO_REG_KEYS = new ArrayList<>();
        QORCODE_OTO_REG_KEYS.add("qrcode");


        // 直播
        final List<String> LIVE_REG_KEYS = new ArrayList<>();
        LIVE_REG_KEYS.add("17xue");
        LIVE_REG_KEYS.add("17xueba");
        LIVE_REG_KEYS.add("ustalk_web");

        // 快乐学
        final List<String> KLX_REG_KEYS = new ArrayList<>();
        KLX_REG_KEYS.add("happy_study");

        // 活动
        final List<String> ACTIVITY_REG_KEYS = new ArrayList<>();
        ACTIVITY_REG_KEYS.add("invite_teacher_activity");

        // 老师APP
        final List<String> TEACHERAPP_REG_KEYS = new ArrayList<>();
        TEACHERAPP_REG_KEYS.add("17Teacher");

        // 神算
        final List<String> SHEN_SZ_REG_KEYS = new ArrayList<>();
        SHEN_SZ_REG_KEYS.add("shensz");

        if (teacherDetail.getIsInvite()) {
            return CrmTeacherWebSourceCategoryType.INVITE_REG;
        }

        if (StringUtils.isEmpty(webSource) || isWebSourceContainsRegKeysPrefixIgnoreCase(PC_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.PC_SITE;
        }

        if (isWebSourceContainsRegKeysPrefixIgnoreCase(BATCH_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.BATCH_REG;
        }

        if (APP_REG_KEYS.contains(webSource)) {
            return CrmTeacherWebSourceCategoryType.APP_REG;
        }

        if (isWebSourceContainsRegKeysPrefixIgnoreCase(BATCH_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.BATCH_REG;
        }

        if (isWebSourceContainsRegKeysPrefixIgnoreCase(JIAOWU_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.AFFAIR_BATCH;
        }
        if (isWebSourceContainsRegKeysPrefixIgnoreCase(WECHAT_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.WECHAT;
        }

        if (isWebSourceContainsRegKeysPrefixIgnoreCase(QORCODE_OTO_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.QRCODE_O2O;
        }
        if (isWebSourceContainsRegKeysPrefixIgnoreCase(LIVE_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.LIVE;
        }
        if (isWebSourceContainsRegKeysPrefixIgnoreCase(KLX_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.KLX;
        }

        if (isWebSourceContainsRegKeysPrefixIgnoreCase(ACTIVITY_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.ACTIVITY;
        }

        if (isWebSourceContainsRegKeysPrefixIgnoreCase(TEACHERAPP_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.TEACHER_APP;
        }

        if (isWebSourceContainsRegKeysPrefixIgnoreCase(SHEN_SZ_REG_KEYS, webSource)) {
            return CrmTeacherWebSourceCategoryType.SHEN_SZ;
        }
        if (StringUtils.equalsIgnoreCase(CrmTeacherWebSourceCategoryType.AFFAIR_TEACHER.getName(), webSource)) {
            return CrmTeacherWebSourceCategoryType.AFFAIR_TEACHER;
        }
        return CrmTeacherWebSourceCategoryType.OTHER_REG;
    }


    private boolean isWebSourceContainsRegKeysPrefixIgnoreCase(List<String> regKeys, String webSource) {
        if (CollectionUtils.isNotEmpty(regKeys) && null != webSource) {
            for (String regKey : regKeys) {
                if (webSource.equalsIgnoreCase(regKey) || webSource.toLowerCase().startsWith(regKey.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }


    public static Map<Long, Integer> getMiddleSchoolTeacherHomework(Collection<Long> teacherIds) {

        if (CollectionUtils.isEmpty(teacherIds)) {
            return Collections.emptyMap();
        }

        String teacherIdsString = StringUtils.join(teacherIds.toArray(), ",");

        String url;
        if (RuntimeMode.isProduction()) {
            url = MIDDLE_SCHOOL_HOMEWORK_COUNT_API_RELEASE;
        } else if (RuntimeMode.isStaging()) {
            url = MIDDLE_SCHOOL_HOMEWORK_COUNT_API_STAGING;
        } else {
            url = MIDDLE_SCHOOL_HOMEWORK_COUNT_API_TEST;
        }
        Map<String, Object> params = MiscUtils.m("teacher_ids", teacherIdsString);
        String URL = UrlUtils.buildUrlQuery(url, params);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL)
                .execute();
//        log.info("response from zx:" + response.getResponseString());

        if (response.hasHttpClientException()) {
            //logger.error("网络异常,请求中学作业数失败！exception is {}", response.getHttpClientExceptionMessage());
            return Collections.emptyMap();
        } else {
            Map<String, Object> result = JsonUtils.convertJsonObjectToMap(response.getResponseString());
            if (!SafeConverter.toBoolean(ObjectUtils.get(() -> result.get("success")))) {
                return Collections.emptyMap();
            } else {
                Map<String, Object> map = (Map) result.get("data");
                Map<Long, Integer> teacherHomeworkFinishCountMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Map<String, Object> subMap = (Map) entry.getValue();
                    Long teacherId = Long.valueOf(entry.getKey());
                    Integer homeworkFinishCount = (Integer) subMap.get("homework_finish_count");
                    teacherHomeworkFinishCountMap.put(teacherId, homeworkFinishCount);
                }
                return teacherHomeworkFinishCountMap;
            }
        }
    }

    public List<CrmTeacherSummary> loadUnusualTeachers(Date startTime, Date endTime) {
        return Collections.emptyList();
//        if (startTime == null || endTime == null) {
//            return Collections.emptyList();
//        }
//        List<CrmUnusualTeacher> teachers = crmUnusualTeacherDao.findByCreateTime(startTime.getTime(), endTime.getTime());
//        if (CollectionUtils.isEmpty(teachers)) {
//            return Collections.emptyList();
//        }
//        Set<Long> teacherIds = teachers.stream().map(CrmUnusualTeacher::getTeacherId).collect(Collectors.toSet());
//        final long registerStart = 1446307200L; // 在2015-11-01之后注册的老师 FIXME 这里应该不用了
//        List<CrmTeacherSummary> teacherSummaries = new ArrayList<>();//crmTeacherSummaryDao.loadUnusualTeachers(teacherIds, registerStart);
//        if (CollectionUtils.isEmpty(teacherSummaries)) {
//            return Collections.emptyList();
//        }
//        List<CrmTeacherSummary> unusualTeachers = new ArrayList<>();
//        for (CrmTeacherSummary teacherSummary : teacherSummaries) {
//            List<String> unusualStatus = teacherSummary.getUnusualStatus();
//            if (CollectionUtils.isNotEmpty(unusualStatus) && (unusualStatus.contains(NOCLS_AFTER_REG_2DAYS.name())
//                    || unusualStatus.contains(NOUSE_AFTER_CLS_3DAYS.name())
//                    || unusualStatus.contains(NO_ASSIGN_5DAYS.name()))) {
//                unusualTeachers.add(teacherSummary);
//            }
//        }
//        return unusualTeachers;
    }
}
