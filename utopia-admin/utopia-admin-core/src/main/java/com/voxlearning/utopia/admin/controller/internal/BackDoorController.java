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

package com.voxlearning.utopia.admin.controller.internal;

import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.common.InsertOption;
import com.voxlearning.alps.api.common.UpdateOption;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.athena.api.SummerMarketLoadSummaryService;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.entity.activity.TangramEntryRecord;
import com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord;
import com.voxlearning.utopia.entity.comment.UserRecordEcho;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.entity.like.UserLikedSummary;
import com.voxlearning.utopia.service.action.api.document.ClazzAchievementLog;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.business.api.TeacherTaskLoader;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceServiceClient;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityService;
import com.voxlearning.utopia.service.campaign.api.TeacherWinterPlanService;
import com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum;
import com.voxlearning.utopia.service.campaign.client.StudentActivityServiceClient;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.clazz.client.NewClazzServiceClient;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.footprint.client.UserDeviceInfoServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.Integral;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.parent.api.DPScoreCircleService;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContext;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContextStoreInfo;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleProcessResult;
import com.voxlearning.utopia.service.parent.constant.GroupCircleType;
import com.voxlearning.utopia.service.piclisten.api.CRMTextBookManagementService;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.reward.client.PublicGoodLoaderClient;
import com.voxlearning.utopia.service.reward.client.PublicGoodServiceClient;
import com.voxlearning.utopia.service.reward.entity.PublicGoodReward;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolServiceClient;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.constants.RefStatus;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.cache.UserCache;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.zone.client.UserLikeServiceClient;
import com.voxlearning.utopia.service.zone.client.UserRecordEchoServiceClient;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 各种后门,主要用于错误数据修复,避免手工执行SQL后还要手工清理缓存
 * Created by Alex on 17/5/12.
 */
@Controller
@RequestMapping("/backdoor")
public class BackDoorController extends AbstractAdminController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private ActionLoaderClient actionLoaderClient;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private ClazzServiceClient clazzServiceClient;
    @Inject private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private GroupServiceClient groupServiceClient;
    @Inject private IntegralLoaderClient integralLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandService;
    @Inject private NewClazzServiceClient newClazzServiceClient;
    @Inject private PublicGoodLoaderClient publicGoodLoaderClient;
    @Inject private PublicGoodServiceClient publicGoodServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SchoolServiceClient schoolServiceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private StudentActivityServiceClient stuActSrvCli;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherServiceClient teacherServiceClient;
    @Inject private TeachingResourceServiceClient teachingResourceServiceClient;
    @Inject private UserDeviceInfoServiceClient userDeviceInfoServiceClient;
    @Inject private UserIntegralServiceClient userIntegralServiceClient;
    @Inject private UserLikeServiceClient userLikeServiceClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserRecordEchoServiceClient userRecordEchoServiceClient;
    @Inject private UserServiceClient userServiceClient;
    @Inject private UserTagLoaderClient userTagLoaderClient;

    @Inject private com.voxlearning.utopia.service.clazz.client.GroupServiceClient clazzGroupServiceClient;

    @ImportService(interfaceClass = TeacherTaskLoader.class)
    private TeacherTaskLoader teacherTaskLoader;

    @ImportService(interfaceClass = SummerMarketLoadSummaryService.class)
    private SummerMarketLoadSummaryService stuAuthQueryService;

    @ImportService(interfaceClass = DPScoreCircleService.class)
    private DPScoreCircleService dpScoreCircleService;

    @ImportService(interfaceClass = TeacherActivityService.class)
    private TeacherActivityService teacherActivityService;

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient pmcStorageClient;

    @ImportService(interfaceClass = TeacherWinterPlanService.class)
    private TeacherWinterPlanService teacherWinterPlanService;

    // enable group
    @RequestMapping(value = "enablegroup.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage enableGroup() {
        Long groupId = getRequestLong("gid");
        if (groupId <= 0L) {
            return MapMessage.errorMessage("今天风真大");
        }

        Group group = groupLoaderClient.getGroupLoader().loadGroupIncludeDisabled(groupId).getUninterruptibly();
        if (group == null) {
            return MapMessage.errorMessage("找不到组呢");
        }
        if (group.isDisabledTrue()) {
            asyncGroupServiceClient.getAsyncGroupService().enableGroup(group);
        }

        // 如果班级也被disabled了的话，一样也要恢复
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzIncludeDisabled(group.getClazzId());
        if (clazz.isDisabledTrue()) {
            newClazzServiceClient.getNewClazzService().enableClazz(clazz);
        }

        // 恢复组内学生
        boolean recoveryStudent = getRequestBool("recoveryStudents");
        if (!recoveryStudent) {
            return MapMessage.successMessage("干的漂亮!!!");
        }

        List<GroupStudentTuple> gsrList = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByGroupIdIncludeDisabled(groupId)
                .getUninterruptibly();
        if (CollectionUtils.isNotEmpty(gsrList)) {
            String fromDate = getRequestString("fromDate");
            if (StringUtils.isNoneBlank(fromDate)) {
                Date from = DateUtils.stringToDate(fromDate, "yyyyMMddHHmmss");
                gsrList = gsrList.stream().filter(p -> p.getUpdateTime().after(from)).collect(Collectors.toList());
            }

            for (GroupStudentTuple gsr : gsrList) {
                raikouSDK.getClazzClient()
                        .getGroupStudentTupleServiceClient()
                        .getGroupStudentTupleService()
                        .enable(gsr.getId())
                        .awaitUninterruptibly();
            }
        }

        List<GroupKlxStudentRef> gksrList = asyncGroupServiceClient.getAsyncGroupService().loadDeletedGroupKlxStudentRefs(groupId).getUninterruptibly();
        if (CollectionUtils.isNotEmpty(gksrList)) {
            String fromDate = getRequestString("fromDate");
            if (StringUtils.isNoneBlank(fromDate)) {
                Date from = DateUtils.stringToDate(fromDate, "yyyyMMddHHmmss");
                gksrList = gksrList.stream().filter(p -> p.getUpdateDatetime().after(from)).collect(Collectors.toList());
            }

            for (GroupKlxStudentRef gksr : gksrList) {
                asyncGroupServiceClient.getAsyncGroupService().enableGroupKlxStudentRefs(Collections.singleton(gksr));
                asyncUserServiceClient.getAsyncUserService().evictUserCache(gksr.getA17id()).awaitUninterruptibly();
            }
        }

        return MapMessage.successMessage("干的漂亮!!!");
    }

    // enable group
    @RequestMapping(value = "disablegroup.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage disableGroup() {
        Long groupId = getRequestLong("gid");
        boolean force = getRequestBool("force");
        if (groupId <= 0L) {
            return MapMessage.errorMessage("今天风真大");
        }

        Group group = groupLoaderClient.getGroupLoader().loadGroupIncludeDisabled(groupId).getUninterruptibly();
        if (group == null) {
            return MapMessage.errorMessage("找不到组呢");
        }
        if (group.isDisabledTrue()) {
            return MapMessage.errorMessage("组已经被删除了!");
        }

        // 组下有学生不能删除
        List<GroupStudentTuple> gsrList = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByGroupIdIncludeDisabled(groupId)
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(gsrList) && !force) {
            return MapMessage.errorMessage("组里面还有学生，先把学生移走!!!");
        }

        if (CollectionUtils.isNotEmpty(gsrList)) {
            for (GroupStudentTuple gsr : gsrList) {
                raikouSDK.getClazzClient()
                        .getGroupStudentTupleServiceClient()
                        .getGroupStudentTupleService()
                        .disable(gsr.getId())
                        .awaitUninterruptibly();
            }
        }

        List<GroupKlxStudentRef> gksrList = asyncGroupServiceClient.findKlxGroupStudentRefsWithCache(groupId);
        if (CollectionUtils.isNotEmpty(gksrList)) {
            for (GroupKlxStudentRef gksr : gksrList) {
                asyncGroupServiceClient.getAsyncGroupService().disableGroupKlxStudentRefs(Collections.singleton(gksr.getId()));
                asyncUserServiceClient.getAsyncUserService().evictUserCache(gksr.getA17id()).awaitUninterruptibly();
            }
        }

        // 去除组下的老师
        List<GroupTeacherTuple> gtrList = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient().findByGroupId(groupId);
        if (CollectionUtils.isNotEmpty(gtrList)) {
            for (GroupTeacherTuple gtr : gtrList) {
                raikouSDK.getClazzClient()
                        .getGroupTeacherTupleServiceClient()
                        .getGroupTeacherTupleService()
                        .disable(gtr.getId(), new UpdateOption().recordLog(true))
                        .awaitUninterruptibly();
                asyncUserServiceClient.getAsyncUserService().evictUserCache(gtr.getTeacherId()).awaitUninterruptibly();
            }
        }

        // 删除组
        asyncGroupServiceClient.getAsyncGroupService().disableGroup(groupId);

        return MapMessage.successMessage("干的漂亮!!!");
    }

    // 变更组的CLAZZ ID
    @RequestMapping(value = "updgc.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage updateGroupClazz() {
        Long groupId = getRequestLong("gid");
        Long clazzId = getRequestLong("cid");

        if (groupId <= 0L || clazzId <= 0L) {
            return MapMessage.errorMessage("今天风真大");
        }

        Group group = groupLoaderClient.getGroupLoader().loadGroup(groupId).getUninterruptibly();
        if (group == null) {
            return MapMessage.errorMessage("找不到组呢");
        }

        if (Objects.equals(group.getClazzId(), clazzId)) {
            return MapMessage.successMessage("已经在班级下面了");
        }

        // 先看下是不是共享组, 如果是,需要断开
        if (StringUtils.isNoneBlank(group.getGroupParent())) {
            Set<Long> groupIds = deprecatedGroupLoaderClient.loadSharedGroupIds(groupId);
            if (CollectionUtils.isNotEmpty(groupIds)) {
                groupServiceClient.shareGroups(groupIds, false);
            }
        }

        clazzServiceClient.updateClazzDataForSystemClazzMove(0L, group.getClazzId(), clazzId, groupId);

        return MapMessage.successMessage("干的漂亮!!!");
    }

    // 去掉共享组状态
    @RequestMapping(value = "unsharegroup.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage unshareGroup() {
        String gids = getRequestString("gids");

        if (StringUtils.isBlank(gids)) {
            return MapMessage.errorMessage("今天风真大");
        }

        List<Long> groupIds = new ArrayList<>();
        String[] gidList = gids.split(",");
        for (String gid : gidList) {
            Long groupId = SafeConverter.toLong(gid);
            Group group = groupLoaderClient.getGroupLoader().loadGroupIncludeDisabled(groupId).getUninterruptibly();
            if (group == null) {
                return MapMessage.errorMessage("找不到组{}", groupId);
            }

            if (StringUtils.isBlank(group.getGroupParent())) {
                return MapMessage.errorMessage("组{}不是共享组", groupId);
            }

            groupIds.add(SafeConverter.toLong(gid));
        }

        // 执行共享组操作
        for (Long groupId : groupIds) {
            Set<Long> sharedGroupIds = deprecatedGroupLoaderClient.loadSharedGroupIds(groupId);
            if (CollectionUtils.isNotEmpty(groupIds)) {
                groupServiceClient.shareGroups(sharedGroupIds, false);
            }
        }

        return MapMessage.successMessage("干的漂亮!!!");
    }

    // 添加共享组状态
    @RequestMapping(value = "sharegroup.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage shareGroup() {
        String gids = getRequestString("gids");

        if (StringUtils.isBlank(gids)) {
            return MapMessage.errorMessage("今天风真大");
        }

        List<Long> groupIds = new ArrayList<>();
        String[] gidList = gids.split(",");
        for (String gid : gidList) {
            Long groupId = SafeConverter.toLong(gid);
            Group group = groupLoaderClient.getGroupLoader().loadGroup(groupId).getUninterruptibly();
            if (group == null) {
                return MapMessage.errorMessage("找不到组{}", groupId);
            }

            if (StringUtils.isNoneBlank(group.getGroupParent())) {
                return MapMessage.errorMessage("组{}已经是共享组", groupId);
            }

            groupIds.add(SafeConverter.toLong(gid));
        }

        // 执行共享组操作
        groupServiceClient.shareGroups(groupIds, true);

        return MapMessage.successMessage("干的漂亮!!!");
    }

    // 添加共享组状态
    @RequestMapping(value = "updclazzintegral.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage updateClazzIntegral() {
        Long gid = getRequestLong("gid");
        Integer delta = getRequestInt("delta");
        String comment = getRequestString("comment");

        if (gid <= 0L) {
            return MapMessage.errorMessage("今天风真大");
        }

        GroupMapper group = deprecatedGroupLoaderClient.loadGroup(gid, false);
        if (group == null) {
            return MapMessage.errorMessage("你要干啥??");
        }

        if (StringUtils.isBlank(comment)) {
            comment = "管理员修正班级学豆";
        }

        ClazzIntegralHistory history = new ClazzIntegralHistory();
        history.setGroupId(group.getId());
        history.setClazzIntegralType(ClazzIntegralType.系统充值.getType());
        history.setIntegral(delta);
        history.setComment(comment);
        history.setAddIntegralUserId(getCurrentAdminUser().getFakeUserId());
        return clazzIntegralServiceClient.getClazzIntegralService()
                .changeClazzIntegral(history)
                .getUninterruptibly();
    }

//    // 添加共享组状态
//    @RequestMapping(value = "upduar.vpage", method = {RequestMethod.GET})
//    @ResponseBody
//    private MapMessage updateUserAchievement() {
//        Long uid = getRequestLong("uid");
//
//        if (uid <= 0L) {
//            return MapMessage.errorMessage("今天风真大");
//        }
//
//        return actionLoaderClient.getRemoteReference().reloadUserAchievement(uid);
//    }

    // 添加共享组状态
    @RequestMapping(value = "updcarl.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage updateClazzAchievementLog() {
        Long cid = getRequestLong("cid");
        Long uid = getRequestLong("uid");

        if (cid <= 0L || uid <= 0L) {
            return MapMessage.errorMessage("今天风真大");
        }

        List<ClazzAchievementLog> cals = actionLoaderClient.getRemoteReference().getClazzAchievementWall(cid);
        cals = cals.stream().filter(p -> Objects.equals(p.getUserId(), uid)).collect(Collectors.toList());

        for (ClazzAchievementLog cal : cals) {
            actionLoaderClient.getRemoteReference().rebuildClazzAchievementData(cal);
        }

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "viewsyahum.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage viewSyahUserMobile() {
        String mobile = getRequestString("mobile");

        if (StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("今天风真大");
        }

        SyahUserMobile syahUserMobile = userLoaderClient.loadSyahUserMobile(mobile);

        return MapMessage.successMessage().add("data", JsonUtils.toJson(syahUserMobile));
    }

    @RequestMapping(value = "viewusertag.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage viewUserTag() {
        Long uid = getRequestLong("uid");

        if (uid <= 0L) {
            return MapMessage.errorMessage("今天风真大");
        }

        UserTag userTag = userTagLoaderClient.loadUserTag(uid);

        return MapMessage.successMessage().add("data", JsonUtils.toJson(userTag));
    }


    // 清理学校所有老师和学生的缓存
    @RequestMapping(value = "cleanschoolcache.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage cleanSchoolCache() {
        Long schoolId = getRequestLong("sid");

        if (schoolId <= 0L) {
            return MapMessage.errorMessage("今天风真大");
        }

        // 清除学校的所有老师的个人缓存
        List<Teacher> teacherList = teacherLoaderClient.loadSchoolTeachers(schoolId);
        for (Teacher teacher : teacherList) {
            UserCache.getUserCache().delete(User.ck_id(teacher.getId()));
        }

        // 清除学校的所有学生的个人缓存
        Collection<Long> clazzIds = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .originalLocationsAsList()
                .stream()
                .map(Clazz.Location::getId)
                .collect(Collectors.toSet());
        studentLoaderClient.loadClazzStudents(clazzIds)
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(User::getId)
                .distinct()
                .forEach(e -> raikouSystem.getCacheService().evictUserCache(e));

        return MapMessage.successMessage("干的漂亮!!!");
    }

    @RequestMapping(value = "delClazzFromSchool.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage delClazzFromSchool() {
        Long clazzId = getRequestLong("clazzId");

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzIncludeDisabled(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("未找到班级！");
        }

        int row = newClazzServiceClient.getNewClazzService()
                .disableClazz(clazzId)
                .getUninterruptibly();

        if (row > 0) {
            return MapMessage.successMessage("干的漂亮!!!");
        }

        return MapMessage.errorMessage("删失败了！");
    }

    @RequestMapping(value = "updategrouptype.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage updateGroupType() {
        Long gid = getRequestLong("gid");
        String type = getRequestString("type");

        Map<String, GroupType> groupTypeMap = MapUtils.map(
                "walking", GroupType.WALKING_GROUP,
                "sys", GroupType.TEACHER_GROUP
        );
        Map<String, ClazzType> clazzTypeMap = MapUtils.map(
                "walking", ClazzType.WALKING,
                "sys", ClazzType.PUBLIC
        );
        GroupType groupType = groupTypeMap.get(type);
        ClazzType clazzType = clazzTypeMap.get(type);
        if (groupType == null || clazzType == null) {
            return MapMessage.errorMessage("参数错了");
        }

        Group group = groupLoaderClient.getGroupLoader().loadGroup(gid).getUninterruptibly();
        if (group == null) {
            return MapMessage.errorMessage("未找到分组！");
        }
        group.setGroupType(groupType);
        group.setUpdateDatetime(new Date());
        Group tmp = clazzGroupServiceClient.getGroupService().updateGroup(group).getUninterruptibly();

        if (groupType != tmp.getGroupType()) {
            return MapMessage.successMessage("更新分组类型失败！");
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(group.getClazzId());
        if (clazz == null) {
            return MapMessage.errorMessage("未找到分组所在班级！");
        }
        if (clazzType != clazz.getClazzType()) {
//        if (!clazz.isWalkingClazz()) {
            int row = newClazzServiceClient.getNewClazzService()
                    .updateClazzType(clazz.getId(), clazzType.getType()).getUninterruptibly();
            if (row == 0) {
                return MapMessage.successMessage("更新班级类型失败！");
            }
        }

        return MapMessage.successMessage("干的漂亮!!!");
    }

    @RequestMapping(value = "exchangegroup.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage exchangeGroup() {
        Long oldGroupId = getRequestLong("oid");
        Long newGroupId = getRequestLong("nid");

        // 检查 同班级下， 同科
        Group oldGroup = groupLoaderClient.getGroupLoader().loadGroup(oldGroupId).getUninterruptibly();
        Group newGroup = groupLoaderClient.getGroupLoader().loadGroupIncludeDisabled(newGroupId).getUninterruptibly();
        if (oldGroup == null || newGroup == null
                || !oldGroup.getClazzId().equals(newGroup.getClazzId())
                || oldGroup.getSubject() != newGroup.getSubject()) {
            return MapMessage.errorMessage("今天风真大");
        }

        // 处理1 newGroup看是否要做enable处理
        if (newGroup.isDisabledTrue()) {
            asyncGroupServiceClient.getAsyncGroupService().enableGroup(newGroup);
        }

        // 处理2 oldGroup 共享分组处理
        if (StringUtils.isNoneBlank(oldGroup.getGroupParent()) && !Objects.equals(oldGroup.getGroupParent(), newGroup.getGroupParent())) {
            List<Group> clazzGroups = asyncGroupServiceClient.getAsyncGroupService().loadGroupsByClazzId(oldGroup.getClazzId()).getUninterruptibly();
            Set<Long> clazzGroupIds = clazzGroups.stream()
                    .filter(p -> Objects.equals(p.getGroupParent(), oldGroup.getGroupParent()))
                    .map(Group::getId)
                    .collect(Collectors.toSet());
            clazzGroupIds.add(newGroup.getId());
            groupServiceClient.shareGroups(clazzGroupIds, true);
        }

        // 处理3 GroupTeacherRef 关系处理
        List<GroupTeacherTuple> oldGtrList = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient().findByGroupId(oldGroupId);
        GroupTeacherTuple oldGtr = oldGtrList.stream()
                .filter(GroupTeacherTuple::isValidTrue)
                .findFirst().orElse(null);
        if (oldGtr != null) {
            List<GroupTeacherTuple> newGtrList = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient().findByGroupId(newGroupId);
            GroupTeacherTuple newGtr = newGtrList.stream()
                    .filter(p -> Objects.equals(p.getTeacherId(), oldGtr.getTeacherId()))
                    .findAny()
                    .orElse(null);
            if (newGtr == null) {
                GroupTeacherTuple gtt = new GroupTeacherTuple();
                gtt.setGroupId(newGroupId);
                gtt.setTeacherId(oldGtr.getTeacherId());
                gtt.setStatus(RefStatus.VALID.name());
                raikouSDK.getClazzClient()
                        .getGroupTeacherTupleServiceClient()
                        .getGroupTeacherTupleService()
                        .insert(gtt, new InsertOption().recordLog(true))
                        .awaitUninterruptibly();
            } else if (!newGtr.isValidTrue()) {
                raikouSDK.getClazzClient()
                        .getGroupTeacherTupleServiceClient()
                        .getGroupTeacherTupleService()
                        .updateStatus(newGtr.getId(), RefStatus.VALID.name(), new UpdateOption().recordLog(true))
                        .awaitUninterruptibly();
            }

            raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .getGroupTeacherTupleService()
                    .disable(oldGtr.getId(), new UpdateOption().recordLog(true))
                    .awaitUninterruptibly();

            asyncUserServiceClient.getAsyncUserService().evictUserCache(oldGtr.getTeacherId()).awaitUninterruptibly();
        }

        // 处理4 GroupStudentRef 关系处理
        List<GroupStudentTuple> oldGsrList = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByGroupIdIncludeDisabled(oldGroupId)
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(oldGsrList)) {
            List<GroupStudentTuple> newGsrList = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .getGroupStudentTupleService()
                    .dbFindByGroupIdIncludeDisabled(newGroupId)
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabledTrue())
                    .collect(Collectors.toList());
            for (GroupStudentTuple oldGsr : oldGsrList) {
                GroupStudentTuple newGsr = newGsrList.stream()
                        .filter(p -> Objects.equals(oldGsr.getStudentId(), p.getStudentId()))
                        .findAny()
                        .orElse(null);
                if (newGsr == null) {
                    GroupStudentTuple gsr = new GroupStudentTuple();
                    gsr.setGroupId(newGroupId);
                    gsr.setStudentId(oldGsr.getStudentId());
                    raikouSDK.getClazzClient()
                            .getGroupStudentTupleServiceClient()
                            .getGroupStudentTupleService()
                            .insert(gsr)
                            .awaitUninterruptibly();
                }

                raikouSDK.getClazzClient()
                        .getGroupStudentTupleServiceClient()
                        .getGroupStudentTupleService()
                        .disable(oldGsr.getId())
                        .awaitUninterruptibly();
            }
        }

        // 处理5 old group disable
        asyncGroupServiceClient.getAsyncGroupService().disableGroup(oldGroupId);

        return MapMessage.successMessage("干的漂亮!!!");
    }

    @RequestMapping(value = "cleanusermobile.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage cleanUserMobile() {
        Long uid = getRequestLong("uid");
        if (uid <= 0L) {
            return MapMessage.errorMessage("今天风真大");
        }

        userServiceClient.getRemoteReference().cleanupUserMobile("job", uid);

        return MapMessage.successMessage("干的漂亮!!!");
    }

    @RequestMapping(value = "exchangems.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage exchangeMainSubAccount() {
        Long uid = getRequestLong("uid");
        if (uid <= 0L) {
            return MapMessage.errorMessage("今天风真大");
        }

        User user = userLoaderClient.loadUser(uid);
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage("今天风真大");
        }

        Long tobeSubId = teacherLoaderClient.loadMainTeacherId(uid);
        if (tobeSubId == null) {
            return MapMessage.errorMessage("今天风真大");
        }

        // 主副账号切换1. 手机号码绑定到副账号
        String decodedPhone = sensitiveUserDataServiceClient.showUserMobile(tobeSubId, "exchangeMainSubAccount", SafeConverter.toString(tobeSubId));
        userServiceClient.cleanupUserMobile("system", tobeSubId);
        userServiceClient.activateUserMobile(uid, decodedPhone);

        // 主副账号切换2. 积分切换，只能对主账号操作
        Integral integral = integralLoaderClient.getIntegralLoader().loadIntegral(tobeSubId);
        if (integral != null && integral.getUsableIntegral() > 0) {
            IntegralHistory decrease = new IntegralHistory();
            decrease.setUserId(tobeSubId);
            decrease.setIntegral(integral.getUsableIntegral() * -1);
            decrease.setIntegralType(IntegralType.积分调整.getType());
            decrease.setComment("主副账号切换调整积分");
            userIntegralServiceClient.getUserIntegralService().changeIntegral(decrease);
        }

        // 主副账号切换3 TeacherRef 处理
        List<Long> allSubIds = teacherLoaderClient.loadSubTeacherIds(tobeSubId);
        teacherServiceClient.disableAllRefs(tobeSubId);
        teacherServiceClient.saveTeacherRef(uid, tobeSubId);
        for (Long subId : allSubIds) {
            if (Objects.equals(subId, uid)) {
                continue;
            }

            teacherServiceClient.saveTeacherRef(uid, subId);
        }

        // 主副账号切换2补充处理. 积分切换，只能对主账号操作
        if (integral != null && integral.getUsableIntegral() > 0) {
            IntegralHistory increase = new IntegralHistory();
            increase.setUserId(uid);
            increase.setIntegral(integral.getUsableIntegral());
            increase.setIntegralType(IntegralType.积分调整.getType());
            increase.setComment("主副账号切换调整积分");
            userIntegralServiceClient.getUserIntegralService().changeIntegral(increase);
        }

        // 处理缓存
        asyncUserServiceClient.getAsyncUserService().evictUserCache(tobeSubId).awaitUninterruptibly();
        asyncUserServiceClient.getAsyncUserService().evictUserCache(allSubIds).awaitUninterruptibly();

        return MapMessage.successMessage("干的漂亮!!!");
    }

    @RequestMapping(value = "recallheadline.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage recallHeadline() {
        Long journalId = getRequestLong("journalId");
        Long userId = getRequestLong("userId");
        String comment = getRequestString("comment");
        String type = getRequestString("type");

        if (StringUtils.equals("com", type)) {
            return userRecordEchoServiceClient.getRemoteReference()
                    .recallCommentClazzJournal(journalId, userId, comment);
        }

        return MapMessage.errorMessage("类型错了");
    }

    @RequestMapping(value = "viewuls.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage viewUserLikedSummary() {
        Long uid = getRequestLong("uid");

        if (uid <= 0L) {
            return MapMessage.errorMessage("今天风真大");
        }

        Date actionTime = new Date();

        String date = getRequestString("date");
        if (StringUtils.isNoneBlank(date)) {
            actionTime = DateUtils.stringToDate(date, "yyyyMMdd");
        }

        UserLikedSummary summary = userLikeServiceClient.loadUserLikedSummary(uid, actionTime);
        return MapMessage.successMessage().add("data", JsonUtils.toJson(summary));
    }

    @RequestMapping(value = "viewulr.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage viewUserLikedRecords() {
        String recId = getRequestString("recId");
        if (StringUtils.isBlank(recId)) {
            return MapMessage.errorMessage("今天风真大");
        }

        UserLikeType likeType = UserLikeType.of(getRequestString("likeType"));
        if (likeType == null) {
            return MapMessage.errorMessage("今天风真大");
        }

        UserRecordEcho record = userLikeServiceClient.loadCommentRecord(likeType, recId);
        return MapMessage.successMessage().add("data", JsonUtils.toJson(record));
    }

    @RequestMapping(value = "viewrli.vpage", method = {RequestMethod.GET})
    @ResponseBody
    private MapMessage viewRecordLikeInfo() {
        String recId = getRequestString("recId");
        if (StringUtils.isBlank(recId)) {
            return MapMessage.errorMessage("今天风真大");
        }

        UserLikeType likeType = UserLikeType.of(getRequestString("likeType"));
        if (likeType == null) {
            return MapMessage.errorMessage("今天风真大");
        }

        RecordLikeInfo record = userLikeServiceClient.loadRecordLikeInfo(likeType, recId);
        return MapMessage.successMessage().add("data", JsonUtils.toJson(record));
    }

    @RequestMapping(value = "getUserDeviceInfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserDeviceInfo() {
        Long userId = getRequestLong("userId");
        if (userId == 0L)
            return MapMessage.errorMessage("非法的参数值!");

        UserDeviceInfo udi = userDeviceInfoServiceClient.loadUserDeviceInfo(userId);
        return MapMessage.successMessage().add("udi", udi);
    }

    @RequestMapping(value = "queryAuthStudent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage queryAuthStudent() {
        Long teacherId = getRequestLong("userId");
        if (teacherId == 0L) {
            return MapMessage.errorMessage("非法的参数值!");
        }
        Subject subject = Subject.safeParse(getRequestString("subject"));
        if (subject == null || subject == Subject.UNKNOWN) {
            return MapMessage.errorMessage("无效的学期");
        }
        SchoolLevel schoolLevel = SchoolLevel.safeParse(getRequestInt("level"));
        // 调用大数据接口
        Set<Long> studentIds = studentLoaderClient.loadTeacherStudents(teacherId)
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        // 数据接口参考wiki --> http://wiki.17zuoye.net/pages/viewpage.action?pageId=37394807
        Map<Long, Map<String, Object>> authStuMap = null;
        try {
            MapMessage authStuResult = stuAuthQueryService.loadStudentAuthInfoData(studentIds, subject.name(), schoolLevel.getLevel());
            authStuMap = (Map<Long, Map<String, Object>>) authStuResult.get("dataMap");
        } catch (Exception ex) {
            logger.error("Failed invoke athena stuAuthQueryService, please check it.", ex);
        }

        // 开始统计
        Map<Long, String> stuMap = new HashMap<>();

        if (authStuMap == null || authStuMap.isEmpty()) {
            return MapMessage.successMessage().add("info", stuMap);
        }

        for (Long studentId : studentIds) {
            Map<String, Object> info = authStuMap.get(studentId);
            if (info == null || info.isEmpty()) {
                continue;
            }
            String authDate = SafeConverter.toString(info.get("auth_date"));
            if (StringUtils.isNotBlank(authDate)) {
                stuMap.put(studentId, authDate);
            } else {
                stuMap.put(studentId, "--");
            }
        }
        return MapMessage.successMessage().add("info", stuMap);
    }

    @RequestMapping(value = "updatessnd.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateSchoolScanNumberDigit() {
        Long schoolId = getRequestLong("sid");
        if (schoolId == 0L) {
            return MapMessage.errorMessage("非法的参数值sid!");
        }

        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("非法的参数值sid!");
        }

        Integer snd = getRequestInt("snd");
        if (snd > 11 || snd < 5) {
            return MapMessage.errorMessage("非法的参数值snd!");
        }

        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();

        if (schoolExtInfo == null) {
            schoolExtInfo = new SchoolExtInfo();
        } else if (schoolExtInfo.getScanNumberDigit() != null && schoolExtInfo.getScanNumberDigit() >= snd) {
            return MapMessage.errorMessage("参数值snd小与已有的填涂号位数!");
        }

        schoolExtInfo.setScanNumberDigit(snd);
        schoolExtServiceClient.getSchoolExtService().updateSchoolExtInfo(schoolExtInfo);

        return MapMessage.successMessage().add("info", schoolExtInfo);
    }

    @RequestMapping(value = "updatetc.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateTeacherSchool() {
        Long teacherId = getRequestLong("tid");
        if (teacherId == 0L) {
            return MapMessage.errorMessage("非法的参数值tid!");
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            return MapMessage.errorMessage("非法的参数值tid!");
        }

        Long schoolId = getRequestLong("sid");
        if (schoolId == 0L) {
            return MapMessage.errorMessage("非法的参数值sid!");
        }

        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("非法的参数值sid!");
        }

        if (Objects.equals(teacherDetail.getTeacherSchoolId(), schoolId)) {
            return MapMessage.successMessage();
        }

        schoolServiceClient.getSchoolService().setUserSchool(teacherId, schoolId);

        return MapMessage.successMessage().add("info", "干得漂亮");
    }

    @RequestMapping(value = "add_test_circle_notice.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage addTestCircleNotice() {
        String testStr = getRequestString("testStr");
        ScoreCircleGroupContextStoreInfo info = JsonUtils.fromJson(testStr, ScoreCircleGroupContextStoreInfo.class);
        if (info == null || info.getCreateDate() == null) {
            return MapMessage.errorMessage("消息格式错误");
        }
        info.setTypeId(new ObjectId().toString());
        info.setGroupCircleType(GroupCircleType.COMMON);

        ScoreCircleGroupContext context = new ScoreCircleGroupContext();
        context.setCreateDate(info.getCreateDate());
        context.setGroupCircleType(info.getGroupCircleType());
        context.setTypeId(info.getTypeId());
        ScoreCircleProcessResult processResult = dpScoreCircleService.process(context);
        if (processResult.isSuccess()) {
            dpScoreCircleService.saveGroupCircleConcreteInfo(info);
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("生成消息失败");
    }


    @RequestMapping(value = "edit_tangram_record_in_cheat.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage editTangramRecordInCheat() {
        try {
            Long userId = getRequestLong("userId");
            String code = getRequestString("code");
            TangramEntryRecord record = stuActSrvCli.loadTangramRecord(userId, code);
            Validate.notNull(record, "记录不存在!");

            Integer action = getRequestInt("action");
            Validate.isTrue(action > 0, "指令错误!");

            switch (action) {
                case 1: // 加一次机会
                    Date startTime = new Date(record.getStartTime());
                    if (DayRange.current().contains(startTime)) {
                        // 把开始时间调成昨天
                        record.setStartTime(DateUtils.addDays(startTime, -1).getTime());
                        stuActSrvCli.editTangramRecordInCheat(record);
                    } else {
                        return MapMessage.errorMessage("今天还未比赛，不用加次数!");
                    }
                    break;
                default:
                    break;
            }

            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "export_tangram_result.vpage")
    public void exportTangramResult(HttpServletResponse resp) {
        try {
            Integer pageSize = getRequestInt("pageSize");
            Integer pageNum = getRequestInt("pageNum");
            String code = getRequestString("code");
            Validate.isTrue(pageSize > 0, "参数1丢失!");
            Validate.isTrue(pageNum > 0, "参数2丢失!");

            List<TangramEntryRecord> allRecords = stuActSrvCli.loadAllTangramRecordsForStat(code);
            Pageable pageReq = PageableUtils.startFromOne(pageNum, pageSize);
            Page<TangramEntryRecord> page = PageableUtils.listToPage(allRecords, pageReq);

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();

            int cellNum = 0;
            int rowNum = 0;
            XSSFRow row;

            createTangramTableHead(sheet);

            for (TangramEntryRecord record : page.getContent()) {
                if (record.getScoreMap() == null)
                    continue;

                for (Map.Entry<Long, Integer> entry : record.getScoreMap().entrySet()) {
                    row = sheet.createRow(rowNum++);
                    XSSFCell cell = row.createCell(cellNum++);
                    cell.setCellValue(DateUtils.dateToString(new Date(entry.getKey()), "MM.dd"));

                    StudentDetail sd = studentLoaderClient.loadStudentDetail(record.getUserId());
                    cell = row.createCell(cellNum++);

                    String region = Optional.ofNullable(raikouSystem.loadRegion(sd.getStudentSchoolRegionCode()))
                            .map(Region::getName)
                            .orElse("未知");
                    cell.setCellValue(region);

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(record.getUserId());

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(sd.fetchRealname());

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(Optional.ofNullable(sd.getClazz()).map(Clazz::getSchoolId).orElse(0L));

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(sd.getStudentSchoolName());

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(SafeConverter.toLong(sd.getClazzId()));

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(Optional.ofNullable(sd.getClazz()).map(s -> s.getClazzLevel().getDescription()).orElse(null));

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(Optional.ofNullable(sd.getClazz()).map(s -> s.getClassName()).orElse(null));

/*                    int maxScore = Optional.ofNullable(record.getScoreMap())
                            .flatMap(r -> r.values().stream().max(Integer::compareTo))
                            .orElse(0);*/

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(entry.getValue());

                    cellNum = 0;
                }
            }

            try {
                @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                workbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        "七巧板成绩导出.xlsx",
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                try {
                    resp.getWriter().write("不能下载");
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                } catch (IOException e) {
                    logger.error("download auditing order exception!");
                }
            }
        } catch (Exception e) {
            try {
                logger.error("Export tangram result error!", e);
                resp.getWriter().write("错误:" + e.getMessage());
            } catch (IOException e1) {
            }
        }
    }

    @RequestMapping(value = "parent_17xue_join.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage parentJoin17xue() {
        Long pid = getRequestLong("pid");
        String lessonId = getRequestString("lid");
        return studyTogetherServiceClient.parentSignUpLesson(lessonId, pid, false);
    }

    @RequestMapping(value = "edit_twofour_record_in_cheat.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage editTwofourRecordInCheat() {
        try {
            Long userId = getRequestLong("userId");
            Validate.notNull(userId, "userId 不能为空!");

            String code = getRequestString("code");
            Validate.notEmpty(code, "code 不能为空");

            TwoFourPointEntityRecord record = stuActSrvCli.loadTwofourPointRecord(userId, code);
            Validate.notNull(record, "记录不存在!");

            Integer action = getRequestInt("action");
            Validate.isTrue(action > 0, "指令错误!");

            switch (action) {
                case 1: // 加一次机会
                    Date startTime = new Date(record.getStartTime());
                    if (DayRange.current().contains(startTime)) {
                        // 把开始时间调成昨天
                        record.setStartTime(DateUtils.getDayStart(DateUtils.addDays(startTime, -1)).getTime());
                        stuActSrvCli.editTwoFourRecordInCheat(record);
                    } else {
                        return MapMessage.errorMessage("今天还未比赛，不用加次数!");
                    }
                    break;
                default:
                    break;
            }
            return MapMessage.successMessage("干的漂亮");
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "export_twofour_result.vpage")
    public void exportTwofourResult(HttpServletResponse resp) {
        try {
            Integer pageSize = getRequestInt("pageSize");
            Integer pageNum = getRequestInt("pageNum");
            Validate.isTrue(pageSize > 0, "参数1丢失!");
            Validate.isTrue(pageNum > 0, "参数2丢失!");

            String code = getRequestString("code");
            Validate.notEmpty(code, "code 不能为空");

            List<TwoFourPointEntityRecord> allRecords = stuActSrvCli.loadAllTwofourRecords(code);
            Pageable pageReq = PageableUtils.startFromOne(pageNum, pageSize);
            Page<TwoFourPointEntityRecord> page = PageableUtils.listToPage(allRecords, pageReq);

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            createTwoFourTableHead(sheet);

            int cellNum = 0;
            int rowNum = 0;
            XSSFRow row;
            for (TwoFourPointEntityRecord record : page.getContent()) {
                StudentDetail sd = studentLoaderClient.loadStudentDetail(record.getUserId());
                if (sd == null) continue;

                Map<Long, Integer> scoreMap = record.getScoreMap();
                Map<Integer, Set<Long>> maxScoreDays = getMaxScoreDays(scoreMap);

                for (Map.Entry<Integer, Set<Long>> entry : maxScoreDays.entrySet()) {
                    Integer score = entry.getKey();
                    Set<Long> days = entry.getValue();
                    for (Long dayTimeStamp : days) {
                        row = sheet.createRow(rowNum++);

                        String dateString = DateFormatUtils.format(new Date(dayTimeStamp), "MM-dd");

                        XSSFCell cell = row.createCell(cellNum++);
                        cell.setCellValue(dateString);

                        cell = row.createCell(cellNum++);
                        cell.setCellValue(Optional.ofNullable(sd.getClazz()).map(Clazz::getSchoolId).orElse(0L));

                        cell = row.createCell(cellNum++);
                        cell.setCellValue(sd.getStudentSchoolName());

                        cell = row.createCell(cellNum++);
                        if (sd.getClazz() != null && sd.getClazz().getClazzLevel() != null) {
                            cell.setCellValue(sd.getClazz().getClazzLevel().getLevel());
                        } else {
                            cell.setCellValue("");
                        }

                        cell = row.createCell(cellNum++);
                        cell.setCellValue(SafeConverter.toLong(sd.getClazzId()));

                        cell = row.createCell(cellNum++);
                        if (sd.getClazz() != null && sd.getClazz().getClazzLevel() != null) {
                            cell.setCellValue(sd.getClazz().formalizeClazzName());
                        } else {
                            cell.setCellValue("");
                        }

                        cell = row.createCell(cellNum++);
                        cell.setCellValue(record.getUserId());

                        cell = row.createCell(cellNum++);
                        cell.setCellValue(sd.fetchRealname());

                        cell = row.createCell(cellNum++);
                        cell.setCellValue(score);

                        cell = row.createCell(cellNum++);
                        cell.setCellValue(record.getResetCount() == null ? 0 : record.getResetCount());

                        cell = row.createCell(cellNum++);
                        cell.setCellValue(record.getSkipCount() == null ? 0 : record.getSkipCount());

                        cellNum = 0;
                    }
                }
            }

            try {
                @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                workbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        "24点成绩导出_单个学生最高分_" + code + ".xlsx",
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                try {
                    resp.getWriter().write("不能下载");
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                } catch (IOException e) {
                    logger.error("download twofour result exception!");
                }
            }
        } catch (Exception e) {
            try {
                logger.error("Export twofour result error!", e);
                resp.getWriter().write("错误:" + e.getMessage());
            } catch (IOException e1) {
            }
        }
    }

    @RequestMapping(value = "export_twofour_result_all.vpage")
    public void exportTwofourResultAll(HttpServletResponse resp) {
        try {
            Integer pageSize = getRequestInt("pageSize");
            Integer pageNum = getRequestInt("pageNum");
            Validate.isTrue(pageSize > 0, "参数1丢失!");
            Validate.isTrue(pageNum > 0, "参数2丢失!");

            String code = getRequestString("code");
            Validate.notEmpty(code, "code 不能为空");

            List<TwoFourPointEntityRecord> allRecords = stuActSrvCli.loadAllTwofourRecords(code);
            Pageable pageReq = PageableUtils.startFromOne(pageNum, pageSize);
            Page<TwoFourPointEntityRecord> page = PageableUtils.listToPage(allRecords, pageReq);

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            createTwoFourTableHead(sheet);

            int cellNum = 0;
            int rowNum = 0;
            XSSFRow row;
            for (TwoFourPointEntityRecord record : page.getContent()) {
                Map<Long, Integer> scoreMap = record.getScoreMap();
                if (scoreMap == null) continue;

                StudentDetail sd = studentLoaderClient.loadStudentDetail(record.getUserId());
                if (sd == null) continue;

                for (Map.Entry<Long, Integer> entry : scoreMap.entrySet()) {
                    Long dayTimeStamp = entry.getKey();
                    Integer score = entry.getValue();

                    row = sheet.createRow(rowNum++);

                    String dateString = DateFormatUtils.format(new Date(dayTimeStamp), "MM-dd");

                    XSSFCell cell = row.createCell(cellNum++);
                    cell.setCellValue(dateString);

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(Optional.ofNullable(sd.getClazz()).map(Clazz::getSchoolId).orElse(0L));

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(sd.getStudentSchoolName());

                    cell = row.createCell(cellNum++);
                    if (sd.getClazz() != null && sd.getClazz().getClazzLevel() != null) {
                        cell.setCellValue(sd.getClazz().getClazzLevel().getLevel());
                    } else {
                        cell.setCellValue("");
                    }

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(SafeConverter.toLong(sd.getClazzId()));

                    cell = row.createCell(cellNum++);
                    if (sd.getClazz() != null && sd.getClazz().getClazzLevel() != null) {
                        cell.setCellValue(sd.getClazz().formalizeClazzName());
                    } else {
                        cell.setCellValue("");
                    }

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(record.getUserId());

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(sd.fetchRealname());

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(score);

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(record.getResetCount() == null ? 0 : record.getResetCount());

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(record.getSkipCount() == null ? 0 : record.getSkipCount());

                    cellNum = 0;
                }
            }

            try {
                @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                workbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        "24点成绩导出_学生所有成绩_" + code + ".xlsx",
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                try {
                    resp.getWriter().write("不能下载");
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                } catch (IOException e) {
                    logger.error("download twofour result exception!");
                }
            }
        } catch (Exception e) {
            try {
                logger.error("Export twofour result error!", e);
                resp.getWriter().write("错误:" + e.getMessage());
            } catch (IOException e1) {
            }
        }
    }

    /**
     * 查找分数最高的日期, 可能两天都是10分
     *
     * @param map key 是时间戳, value 是分数
     * @return key 是分数 value 是时间戳 set
     */
    private Map<Integer, Set<Long>> getMaxScoreDays(Map<Long, Integer> map) {
        if (map == null || map.isEmpty()) {
            return new HashMap<>();
        }

        Integer maxScore = null; // 最高分
        Map<Integer, Set<Long>> scoreDays = new HashMap<>();//key 分数 value 时间戳

        for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            Long day = entry.getKey(); // 时间戳
            Integer score = entry.getValue(); // 分数

            if (maxScore == null || score >= maxScore) {
                maxScore = score;
            }

            Set<Long> daySet = scoreDays.get(score);
            if (daySet == null) {
                daySet = new LinkedHashSet<>();
            }
            daySet.add(day);
            scoreDays.put(score, daySet);
        }

        Map<Integer, Set<Long>> result = new HashMap<>();
        result.put(maxScore, scoreDays.get(maxScore));
        return result;
    }

    @RequestMapping(value = "load_17jt_user.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage load17JTUserData() {
        Long userId = getRequestLong("userId");
        return MapMessage.successMessage().add("user", teacherActivityService.load17JTUserData(userId));
    }

    @RequestMapping(value = "fix_17jt.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fix17JTData() {
        Long userId = getRequestLong("userId");
        return teacherActivityService.fixJTData(userId);
    }

    @RequestMapping(value = "bind_mobile.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage bindMobile() {
        try {
            Long userId = getRequestLong("userId");
            String mobile = getRequestString("mobile");

            Validate.notNull(userId, "请输入用户ID");
            Validate.notNull(mobile, "请输入用户手机号");
            Validate.isTrue(mobile.length() == 11, "手机号必须为11位");

            MapMessage mapMessage = userServiceClient.activateUserMobile(userId, mobile);

            if (mapMessage.isSuccess()) {
                return MapMessage.successMessage("干的漂亮");
            }
            return mapMessage;
        } catch (NullPointerException | IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "student_change_group.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage StudentChangeGroup() {
        try {
            Long oid = getRequestLong("oid");
            Long nid = getRequestLong("nid");

            if (oid <= 0 || nid <= 0) {
                return MapMessage.errorMessage("今天风真大");
            }
            Group oGroup = groupLoaderClient.getGroupLoader().loadGroup(oid).getUninterruptibly();
            if (oGroup == null) {
                return MapMessage.errorMessage("找不到组呢oid");
            }
            Group nGroup = groupLoaderClient.getGroupLoader().loadGroup(nid).getUninterruptibly();
            if (nGroup == null) {
                return MapMessage.errorMessage("找不到组呢nid");
            }
            List<GroupStudentTuple> ogsrList = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .getGroupStudentTupleService()
                    .dbFindByGroupIdIncludeDisabled(oid)
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabledTrue())
                    .collect(Collectors.toList());
            if (ogsrList.isEmpty()) {
                return MapMessage.errorMessage("oid没有学生");
            }
            List<GroupStudentTuple> ngsrList = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .getGroupStudentTupleService()
                    .dbFindByGroupIdIncludeDisabled(nid)
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabledTrue())
                    .collect(Collectors.toList());
            Set<Long> nStudentIds = ngsrList.stream().map(GroupStudentTuple::getStudentId).collect(Collectors.toSet());
            // 没有在要转移过去的组，把这些学生加入班级
            Set<GroupStudentTuple> oStudents = ogsrList.stream().filter(s -> !nStudentIds.contains(s.getStudentId())).collect(Collectors.toSet());

            if (oStudents.isEmpty()) {
                return MapMessage.errorMessage("没有需要转移的学生");
            }

            // 换到新组
            MapMessage mapMessage = groupServiceClient.moveStudentsBetweenGroup(oid, nid, oStudents.stream().map(GroupStudentTuple::getStudentId).collect(Collectors.toList()));
            if (!mapMessage.isSuccess()) {
                return MapMessage.errorMessage(mapMessage.getInfo());
            }
            return MapMessage.successMessage("干的漂亮");

        } catch (NullPointerException | IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "disablegtf.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage disableGroupTeacherRef() {
        try {
            Long gid = getRequestLong("gid");
            Long rid = getRequestLong("rid");

            if (gid <= 0 || rid <= 0) {
                return MapMessage.errorMessage("今天风真大");
            }

            List<GroupTeacherTuple> gtrList = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient().findByGroupId(gid);
            if (CollectionUtils.isEmpty(gtrList)) {
                return MapMessage.errorMessage("今天风真大");
            }

            GroupTeacherTuple gtr = gtrList.stream()
                    .filter(p -> Objects.equals(p.getId(), rid.toString()))
                    .findFirst().orElse(null);

            if (gtr == null) {
                return MapMessage.errorMessage("今天风真大");
            }

            raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .getGroupTeacherTupleService()
                    .disable(rid.toString(), new UpdateOption().recordLog(true))
                    .awaitUninterruptibly();

            return MapMessage.successMessage("干的漂亮");

        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 更新公益项目的奖励配置
     *
     * @return
     */
    @RequestMapping(value = "update_pg_reward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updatePGReward() {
        try {
            String configJson = getRequestString("json");
            Validate.notBlank(configJson, "非法的参数!");

            PublicGoodReward reward = JsonUtils.fromJson(configJson, PublicGoodReward.class);
            Validate.notNull(reward.getId(), "数据错误");

            return publicGoodServiceClient.updateReward(reward);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "check_pg_rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage checkPGRank() {
        Long activityId = getRequestLong("activityId");
        Long schoolId = getRequestLong("schoolId");
        return publicGoodLoaderClient.loadRankForBackDoor(activityId, schoolId);
    }

    @RequestMapping(value = "move_teaching_res_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage moveTeachingResData() {
        return teachingResourceServiceClient.moveDataForBackDoor();
    }

    private void createTangramTableHead(XSSFSheet sheet) {
        int rowNum = 0, cellNum = 0;

        XSSFRow row = sheet.createRow(rowNum++);

        XSSFCell cell = row.createCell(cellNum++);
        cell.setCellValue("日期");

        cell = row.createCell(cellNum++);
        cell.setCellValue("地区");

        cell = row.createCell(cellNum++);
        cell.setCellValue("学生ID");

        cell = row.createCell(cellNum++);
        cell.setCellValue("学生姓名");

        cell = row.createCell(cellNum++);
        cell.setCellValue("学校ID");

        cell = row.createCell(cellNum++);
        cell.setCellValue("学校名称");

        cell = row.createCell(cellNum++);
        cell.setCellValue("班级ID");

        cell = row.createCell(cellNum++);
        cell.setCellValue("年级");

        cell = row.createCell(cellNum++);
        cell.setCellValue("班级");

        cell = row.createCell(cellNum++);
        cell.setCellValue("成绩");
    }

    private void createTwoFourTableHead(XSSFSheet sheet) {
        int rowNum = 0, cellNum = 0;

        XSSFRow row = sheet.createRow(rowNum++);

        XSSFCell cell = row.createCell(cellNum++);
        cell.setCellValue("日期");

        cell = row.createCell(cellNum++);
        cell.setCellValue("学校ID");

        cell = row.createCell(cellNum++);
        cell.setCellValue("学校名称");

        cell = row.createCell(cellNum++);
        cell.setCellValue("年级");

        cell = row.createCell(cellNum++);
        cell.setCellValue("班级ID");

        cell = row.createCell(cellNum++);
        cell.setCellValue("班级名称");

        cell = row.createCell(cellNum++);
        cell.setCellValue("学生ID");

        cell = row.createCell(cellNum++);
        cell.setCellValue("学生姓名");

        cell = row.createCell(cellNum++);
        cell.setCellValue("成绩");

        cell = row.createCell(cellNum++);
        cell.setCellValue("重置次数");

        cell = row.createCell(cellNum++);
        cell.setCellValue("跳过次数");
    }

    @RequestMapping(value = "move_liked_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage moveLikedData() {
        int start = getRequestInt("start", 0);
        int end = getRequestInt("end", 100);
        return publicGoodServiceClient.moveLikeToMongo(start, end);
    }

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @RequestMapping(value = "delete_page.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deletePage() {
        if (RuntimeMode.isProduction())
            return MapMessage.errorMessage();
        String pageName = getRequestString("p");
        String blockName = getRequestString("b");
        List<PageBlockContent> byPageName = pageBlockContentServiceClient.getPageBlockContentBuffer().findByPageName(pageName);
        List<PageBlockContent> pageBlockContents = byPageName.stream().filter(t -> t.getBlockName().equals(blockName)).collect(Collectors.toList());
        if (pageBlockContents.size() <= 1)
            return MapMessage.successMessage("这个配置不重复");

        PageBlockContent pageBlockContent = pageBlockContents.get(0);
        boolean b = crmConfigService.$disablePageBlockContent(pageBlockContent.getId());
        if (b)
            return MapMessage.successMessage("删除成功，哼！");
        else
            return MapMessage.errorMessage("卧槽，怎么失败了！");
    }

    @RequestMapping(value = "update_global_message_expired.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage updateGlobalMessageExpired() {
        try {
            String id = getRequestString("id");
            String expired = getRequestString("expired");

            if (StringUtils.isEmpty(id)) {
                return MapMessage.errorMessage("id 为空");
            }
            if (StringUtils.isEmpty(expired)) {
                return MapMessage.errorMessage("expired 为空");
            }

            Date date = DateUtils.parseDate(expired, "yyyy-MM-dd HH:mm:ss");
            messageCommandService.getMessageCommandService().updateGlobalMessageExpiredTime(id, date);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage("执行成功,别忘了检查执行结果");
    }

    @RequestMapping(value = "delete_global_message.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteGlobalMessage() {
        try {
            String id = getRequestString("id");
            if (StringUtils.isEmpty(id)) {
                return MapMessage.errorMessage("id 为空");
            }

            messageCommandService.getMessageCommandService().deleteGlobalMessage(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage("执行成功,别忘了检查执行结果");
    }

    @ImportService(interfaceClass = CRMTextBookManagementService.class)
    private CRMTextBookManagementService crmTextBookManagementService;

    @RequestMapping(value = "init_piclisten_listname.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage init_piclisten_listname() {
        try {
            String fileUrl = getRequestString("fileUrl");
            if (StringUtils.isEmpty(fileUrl)) {
                return MapMessage.errorMessage("fileUrl 为空");
            }
            String content = downLoadFromUrl(fileUrl);
            String[] lines = content.split("\n");
            for (String line : lines) {
                String[] split = line.split(",");
                String bookId = split[0];
                String listName = split[1];
                TextBookManagement textBookManagement = new TextBookManagement();
                textBookManagement.setBookId(bookId);
                textBookManagement.setBookListName(listName);
                crmTextBookManagementService.$upsertTextBook(textBookManagement);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage("执行成功,别忘了检查执行结果");
    }


    /**
     * 上传图片到aliyun-17pmc
     */
    @RequestMapping(value = "/upload_file_to_oss_17pmc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadImgToOss(MultipartFile inputFile) {
        String activityName = getRequestString("activity_name");
        if (inputFile == null || StringUtils.isBlank(activityName)) {
            return MapMessage.errorMessage("没有上传的文件");
        }
        try {
            return uploadFileTo17pmcOss(inputFile, activityName);
        } catch (Exception e) {
            logger.error("课程文件上传失败{}", e);
            return MapMessage.errorMessage("课程文件上传失败");
        }
    }

    private static String downLoadFromUrl(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3 * 1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        InputStream inputStream = conn.getInputStream();

        List<Long> list = new ArrayList<>();

        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, "UTF-8");
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }


    private MapMessage uploadFileTo17pmcOss(MultipartFile inputFile, String activityName) throws IOException {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = activityName + "/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = activityName + "/test/";
        }
        String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
        String fileName = inputFile.getOriginalFilename().replaceAll(" ", "");
        String realName = pmcStorageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
        String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        return MapMessage.successMessage().add("fileName", realName).add("fileUrl", fileUrl);
    }

    @RequestMapping(value = "del_winter_plan.vpage")
    @ResponseBody
    public MapMessage delWinterPlan() {
        long teacherId = getRequestLong("teacherId");
        String type = getRequestParameter("type", TeacherActivityEnum.PARENT_CHILD_2018.name());
        return teacherWinterPlanService.delTeacherActivityRef(teacherId, type);
    }

}
