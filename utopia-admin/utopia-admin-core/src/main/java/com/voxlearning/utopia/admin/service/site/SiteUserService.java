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

package com.voxlearning.utopia.admin.service.site;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.service.site.SiteBatchInputHandler.UnitTransformer;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.RSManagedRegionType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.user.consumer.client.ResearchStaffUserServiceClient;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lcy
 * @since 16/4/5
 */
@Named
public class SiteUserService {

    private final static String DEFAULT_WORD_SEPARATOR = "\\t";
    private final static int DEFAULT_MAX_QUERY_MOBILE_COUNT = 100;

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private ClazzServiceClient clazzServiceClient;
    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private ResearchStaffLoaderClient researchStaffLoaderClient;
    @Inject private ResearchStaffUserServiceClient researchStaffUserServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;

    @Getter
    @Setter
    private static class QueryMobileObj {
        long userId;

        @Override
        public String toString() {
            return SafeConverter.toString(userId);
        }

        public static final UnitTransformer<String, QueryMobileObj> rowTransformer = row -> {
            String[] words = row.split(DEFAULT_WORD_SEPARATOR);
            if (words.length != 1) {
                return null;
            }
            QueryMobileObj queryMobileObj = new QueryMobileObj();
            queryMobileObj.setUserId(SafeConverter.toLong(words[0]));
            return queryMobileObj;
        };
    }

    @Getter
    @Setter
    public static class QueryMobileResult {
        long userId;
        String mobile;
        String userName;

        public QueryMobileResult(long userId, String mobile, String userName) {
            this.userId = userId;
            this.mobile = mobile;
            this.userName = userName;
        }

        // only for student
        Integer clazzLevel;
        String clazzName;
        Long schoolId;
        String schoolName;
        String region;
    }

    public MapMessage batchQueryMobile(String content) {
        DefaultExcelStringSiteBatchInputHandler<QueryMobileObj> defaultExcelStringInputHandler = new DefaultExcelStringSiteBatchInputHandler<>();
        List<SiteBatchInputHandler.FailedData<String>> failedRows = new ArrayList<>();
        List<QueryMobileObj> queryMobileObjs = defaultExcelStringInputHandler.handleInput(
                content, failedRows, QueryMobileObj.rowTransformer);

        List<QueryMobileResult> results = new ArrayList<>();
        for (QueryMobileObj queryMobileObj : queryMobileObjs) {
            long userId = queryMobileObj.getUserId();

            User user = userLoaderClient.loadUserIncludeDisabled(userId);
            String userName = "";
            if (user != null) {
                userName = user.fetchRealname();
            }

            String mobile = "";

            String authenticatedMobile = sensitiveUserDataServiceClient.showUserMobile(userId, "SiteUserService.batchQueryMobile", SafeConverter.toString(userId));
            if (authenticatedMobile != null) {
                mobile = authenticatedMobile;
            }

            QueryMobileResult queryMobileResult = new QueryMobileResult(userId, mobile, userName);

            results.add(queryMobileResult);
        }

        return MapMessage.successMessage().add("result", results);
    }

    @Getter
    @Setter
    private static class QueryUserIdByMobileObj {
        String mobile;
        UserType userType;

        @Override
        public String toString() {
            return mobile + " " + userType.name();
        }

        public static final UnitTransformer<String, QueryUserIdByMobileObj> rowTransformer = row -> {
            String[] words = row.split(DEFAULT_WORD_SEPARATOR);
            if (words.length != 2) {
                return null;
            }
            QueryUserIdByMobileObj queryUserIdByMobileObj = new QueryUserIdByMobileObj();
            queryUserIdByMobileObj.setMobile(SafeConverter.toString(words[0]));
            queryUserIdByMobileObj.setUserType(UserType.of(SafeConverter.toInt(words[1])));
            return queryUserIdByMobileObj;
        };
    }

    public MapMessage batchQueryUserIdByMobile(String content, String operator) {
        DefaultExcelStringSiteBatchInputHandler<QueryUserIdByMobileObj> defaultExcelStringInputHandler = new DefaultExcelStringSiteBatchInputHandler<>();
        List<SiteBatchInputHandler.FailedData<String>> failedRows = new ArrayList<>();
        List<QueryUserIdByMobileObj> queryUserIdByMobileObjs = defaultExcelStringInputHandler.handleInput(
                content, failedRows, QueryUserIdByMobileObj.rowTransformer);

        List<QueryMobileResult> results = new ArrayList<>();
        for (QueryUserIdByMobileObj queryUserIdByMobileObj : queryUserIdByMobileObjs) {
            List<User> users = userLoaderClient.loadUsers(queryUserIdByMobileObj.getMobile(), queryUserIdByMobileObj.getUserType());

            if (CollectionUtils.isNotEmpty(users)) {
                User user = users.get(0);
                QueryMobileResult queryMobileResult = new QueryMobileResult(user.getId(),
                        sensitiveUserDataServiceClient.showUserMobile(user.getId(), "SiteUserService.batchQueryMobile", operator), user.fetchRealname());
                if (user.isStudent()) {
                    // need for detail data for student
                    StudentDetail detail;
                    if (user instanceof StudentDetail) {
                        detail = (StudentDetail) user;
                    } else {
                        detail = studentLoaderClient.loadStudentDetail(user.getId());
                    }
                    if (detail != null) {
                        Clazz clazz = detail.getClazz();
                        if (clazz != null) {
                            queryMobileResult.setClazzLevel(clazz.getClazzLevel().getLevel());
                            queryMobileResult.setClazzName(clazz.formalizeClazzName());
                            queryMobileResult.setSchoolId(clazz.getSchoolId());
                        }
                        queryMobileResult.setSchoolName(detail.getStudentSchoolName());
                        Region region = raikouSystem.loadRegion(detail.getStudentSchoolRegionCode());
                        if (region != null) {
                            queryMobileResult.setRegion(region.getName());
                        }
                    }
                }
                results.add(queryMobileResult);
            } else {
                results.add(new QueryMobileResult(-1, queryUserIdByMobileObj.getMobile(), ""));
            }
        }

        return MapMessage.successMessage().add("result", results);
    }

    @Getter
    @Setter
    private static class AddStudentToGroupObj {
        long studentId;
        long teacherId;
        long clazzId;

        @Override
        public String toString() {
            return studentId + " " + teacherId + " " + clazzId;
        }

        public static final UnitTransformer<String, AddStudentToGroupObj> rowTransformer = row -> {
            String[] words = row.split(DEFAULT_WORD_SEPARATOR);
            if (words.length != 3) {
                return null;
            }
            AddStudentToGroupObj addStudentToGroupObj = new AddStudentToGroupObj();
            addStudentToGroupObj.setStudentId(SafeConverter.toLong(words[0]));
            addStudentToGroupObj.setTeacherId(SafeConverter.toLong(words[1]));
            addStudentToGroupObj.setClazzId(SafeConverter.toLong(words[2]));
            return addStudentToGroupObj;
        };
    }

    public MapMessage batchAddStudentsToClazz(String content) {
        DefaultExcelStringSiteBatchInputHandler<AddStudentToGroupObj> defaultExcelStringInputHandler = new DefaultExcelStringSiteBatchInputHandler<>();
        List<SiteBatchInputHandler.FailedData<String>> failedRows = new ArrayList<>();
        List<AddStudentToGroupObj> addStudentToGroupObjs = defaultExcelStringInputHandler.handleInput(
                content, failedRows, AddStudentToGroupObj.rowTransformer);

        for (AddStudentToGroupObj addStudentToGroupObj : addStudentToGroupObjs) {
            long studentId = addStudentToGroupObj.getStudentId();
            long teacherId = addStudentToGroupObj.getTeacherId();
            long clazzId = addStudentToGroupObj.getClazzId();

            List<GroupStudentTuple> tuples = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .getGroupStudentTupleService()
                    .dbFindByStudentIdIncludeDisabled(studentId)
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabledTrue())
                    .collect(Collectors.toList());
            if (tuples.size() > 0) {
                Set<Long> groupIds = tuples.stream().map(GroupStudentTuple::getGroupId).collect(Collectors.toSet());
                Map<Long, Group> groupMap = groupLoaderClient.getGroupLoader().loadGroups(groupIds).getUninterruptibly();
                groupMap.values()
                        .stream()
                        .map(Group::getClazzId)
                        .collect(Collectors.toSet())
                        .forEach(o -> clazzServiceClient.studentExitSystemClazz(studentId, o));
            }
            // 加入班级并关联老师
            clazzServiceClient.studentJoinSystemClazz(studentId, clazzId, teacherId, true, OperationSourceType.crm);
        }

        return MapMessage.successMessage();
    }

    @Getter
    @Setter
    private static class SetRstaffManagedSchoolObj {
        long rstaffId;
        long schoolId;

        @Override
        public String toString() {
            return rstaffId + " " + schoolId;
        }

        public static final UnitTransformer<String, SetRstaffManagedSchoolObj> rowTransformer = row -> {
            String[] words = row.split(DEFAULT_WORD_SEPARATOR);
            if (words.length != 2) {
                return null;
            }
            SetRstaffManagedSchoolObj setRstaffManagedSchoolObj = new SetRstaffManagedSchoolObj();
            setRstaffManagedSchoolObj.setRstaffId(SafeConverter.toLong(words[0]));
            setRstaffManagedSchoolObj.setSchoolId(SafeConverter.toLong(words[1]));
            return setRstaffManagedSchoolObj;
        };
    }

    public MapMessage batchSetRstaffManagedSchool(String content) {
        DefaultExcelStringSiteBatchInputHandler<SetRstaffManagedSchoolObj> defaultExcelStringInputHandler = new DefaultExcelStringSiteBatchInputHandler<>();
        List<SiteBatchInputHandler.FailedData<String>> failedRows = new ArrayList<>();
        List<SetRstaffManagedSchoolObj> setRstaffManagedSchoolObjs = defaultExcelStringInputHandler.handleInput(
                content, failedRows, SetRstaffManagedSchoolObj.rowTransformer);

        Set<Long> handledRstaffIds = new HashSet<>();// 处理过的教研员，不需要设置user region和unset已有的管理区域
        Set<Long> unexistedRstaffIds = new HashSet<>();// 不存在的教研员
        Set<Long> existedRstaffIds = new HashSet<>();// 存在的教研员
        for (SetRstaffManagedSchoolObj setRstaffManagedSchoolObj : setRstaffManagedSchoolObjs) {
            long rstaffId = setRstaffManagedSchoolObj.getRstaffId();
            long schoolId = setRstaffManagedSchoolObj.getSchoolId();

            if (unexistedRstaffIds.contains(rstaffId)) {
                continue;
            }

            if (!existedRstaffIds.contains(rstaffId)) {
                // 需要判断教研员是否存在
                ResearchStaff researchStaff = researchStaffLoaderClient.loadResearchStaff(rstaffId);
                if (researchStaff == null || researchStaff.isDisabledTrue()) {
                    unexistedRstaffIds.add(rstaffId);
                    continue;
                } else {
                    existedRstaffIds.add(rstaffId);
                }
            }

            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(schoolId)
                    .getUninterruptibly();
            if (school == null) {
                continue;// 去找下一个可能的school
            }

            if (!handledRstaffIds.contains(rstaffId)) {

                // 清除已有managed region
                researchStaffUserServiceClient.unsetManagedRegionByRStaffId(rstaffId);

                handledRstaffIds.add(rstaffId);
            }

            ResearchStaffManagedRegion researchStaffManagedRegion = new ResearchStaffManagedRegion();
            researchStaffManagedRegion.setRstaffId(rstaffId);
            researchStaffManagedRegion.setManagedRegionCode(schoolId);
            researchStaffManagedRegion.setManagedRegionType(RSManagedRegionType.SCHOOL);
            researchStaffUserServiceClient.setManagedRegion(researchStaffManagedRegion);
        }
        return MapMessage.successMessage();
    }

}
