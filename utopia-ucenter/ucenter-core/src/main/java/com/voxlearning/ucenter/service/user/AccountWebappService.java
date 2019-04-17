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

package com.voxlearning.ucenter.service.user;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.ucenter.cache.UcenterWebCacheSystem;
import com.voxlearning.ucenter.mapper.UserMapper;
import com.voxlearning.ucenter.support.SessionUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.client.UserEmailServiceClient;
import com.voxlearning.utopia.service.user.client.athena.UctUserServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author changyuan.liu
 * @since 2015.12.8
 */
@Named
@Slf4j
public class AccountWebappService {

    @Inject private RaikouSDK raikouSDK;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private UcenterWebCacheSystem ucenterWebCacheSystem;
    @Inject private TeacherWebappService teacherWebappService;
    @Inject private UserServiceClient userServiceClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private VendorLoaderClient vendorLoaderClient;
    @Inject private VendorServiceClient vendorServiceClient;
    @Inject private UserEmailServiceClient userEmailServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UctUserServiceClient uctUserServiceClient;

    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;

    public void onUserLogin(Long userId) {
        if (userId == null) {
            return;
        }

        // 如果是短信邀请进来的，默认绑定手机号
        teacherWebappService.bindInvitedTeacherMobile(userId);
    }

    /**
     * 注册一个学生号
     *
     * @param command
     * @return
     */
    public MapMessage registStudent(UserMapper command, Long teacherId) {
        RoleType roleType = RoleType.valueOf(command.getRole());
        UserType userType = UserType.of(command.getUserType());

        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(roleType);
        neonatalUser.setUserType(userType);
        neonatalUser.setEmail(command.getEmail());
        neonatalUser.setMobile(command.getMobile());
        neonatalUser.setPassword(command.getPassword());
        neonatalUser.setRealname(command.getRealname());
        neonatalUser.setInviter(command.getInviteInfo());
        neonatalUser.setInvitationType(command.getInvitationType());
        neonatalUser.setCode(command.getCode());
        neonatalUser.setGender(command.getGender());
        neonatalUser.setScanNumber(command.getScanNumber());

        if (teacherId != 0) {
            neonatalUser.setTeacherId(teacherId);
        }
        neonatalUser.setClazzId(StringUtils.isBlank(command.getClazzId()) ? null : Long.parseLong(command.getClazzId().trim()));
        neonatalUser.setWebSource(StringUtils.isBlank(command.getWebSource()) ? UserWebSource.web_self_reg.getSource() : command.getWebSource().trim());

        return userServiceClient.registerUserAndSendMessage(neonatalUser);
    }

    /**
     * 注册老师帐号
     */
    public MapMessage registTeacher(UserMapper command, String source) {
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(RoleType.valueOf(command.getRole()));
        neonatalUser.setUserType(UserType.of(command.getUserType()));
        neonatalUser.setMobile(command.getMobile());
        neonatalUser.setPassword(command.getPassword());
        neonatalUser.setRealname(command.getRealname());
        neonatalUser.setInviter(command.getInviteInfo());
        neonatalUser.setInvitationType(command.getInvitationType());
        neonatalUser.setWebSource(source);

        return userServiceClient.registerUserAndSendMessage(neonatalUser);
    }

    /**
     * 判断是否要做第三方帐号绑定
     *
     * @param command
     * @param userId
     */
    public void checkAccountBind(UserMapper command, Long userId) {
        // 判断是否要做第三方帐号绑定
        String dataKey = command.getDataKey();
        if (StringUtils.isBlank(dataKey)) {
            return;
        }

        CacheObject<Map> cacheObject = ucenterWebCacheSystem.CBS.unflushable.get(dataKey);
        if (cacheObject == null) {
            // failed to access couchbase server, ignore
            return;
        }

        Map map = cacheObject.getValue();
        if (map != null) {
            String sourceName = String.valueOf(map.get("source"));
            String sourceUid = String.valueOf(map.get("sourceUid"));
            String sourceUserName = String.valueOf(map.get("userName"));
            try {
                thirdPartyService.persistLandingSource(sourceName, sourceUid, sourceUserName, userId);
            } catch (Exception ignored) {
            }
            ucenterWebCacheSystem.CBS.unflushable.delete(dataKey);
        }
    }

    // FIXME 奖励应该都不在这处理了。。。
    // 邀请者为真实班级学生时
    //     每有1个学生注册，邀请者得1个学豆，每天有上限3，12/31日后停止
    // 邀请者为虚拟班级学生时
    //     此链接注册的学生进入和邀请者相同虚拟班级，超过90进新班级。
    //     每有1个学生注册，邀请者得5个走遍美国钻石，5个通天塔星星，100个PK经验值。
    public void awardForInvitation(Long inviteeId, String inviteCode) {
        if (inviteeId == null || inviteeId == 0L || StringUtils.isBlank(inviteCode)) {
            return;
        }

        // FIXME: 防作弊处理
        // FIXME: inviteeId应该是刚注册的用户，不应该有被邀请记录。
        // FIXME: 这里的逻辑是这样的吗？
        // FIXME: xiaohai.zhang
        long count = asyncInvitationServiceClient.loadByInvitee(inviteeId).enabled().count();
        if (count > 0) {
            return;
        }

        String defaultDesKey = ConfigManager.instance().getCommonConfig().getConfigs().get("default_des_key");
        if (defaultDesKey == null) {
            throw new ConfigurationException("No 'default_des_key' configured");
        }
        Long invitationId = ConversionUtils.toLong(DesUtils.decryptHexString(defaultDesKey, inviteCode));
        if (invitationId == 0 || invitationId.equals(inviteeId)) {
            return;
        }

        // 保存邀请信息
        InviteHistory inviteHistory = InviteHistory.newInstance();
        inviteHistory.setUserId(invitationId);
        inviteHistory.setInviteeUserId(inviteeId);
        inviteHistory.setInvitationType(InvitationType.STUDENT_INVITE_STUDENT_LINK);
        asyncInvitationServiceClient.getAsyncInvitationService()
                .createInviteHistory(inviteHistory)
                .awaitUninterruptibly();
    }

    public List<Map<String, Object>> findByStudentNameAndSchoolId(Long schoolId, Integer clazzLevel, String studentName) {
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null || school.isDisabledTrue()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new LinkedList<>();

        List<Long> candidateStudentIds = uctUserServiceClient.queryUserByName(studentName, Collections.singletonList(school.getRegionCode()));
        if (CollectionUtils.isNotEmpty(candidateStudentIds)) {
            Map<Long, StudentDetail> candidateStudents = studentLoaderClient.loadStudentDetails(candidateStudentIds);
            for (StudentDetail studentDetail : candidateStudents.values()) {
                if (studentDetail.getClazz() == null) {
                    continue;
                }

                if (Objects.equals(studentDetail.getClazz().getSchoolId(), schoolId)
                        && Objects.equals(studentDetail.getClazz().getClazzLevel().getLevel(), clazzLevel)) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("clazzType", studentDetail.getClazz().getClassType());
                    map.put("clazzName", studentDetail.getClazz().getClassName());
                    map.put("eduSystem", studentDetail.getClazz().getEduSystem());
                    map.put("userId", studentDetail.getId());
                    map.put("jie", studentDetail.getClazz().getJie());
                    result.add(map);
                }
            }
        } else {
            log.warn("no candidate student result found with query, schooid:{}, student name:{}", schoolId, studentName);

            // 获得学校下所有班级
            List<Long> clazzIds = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(schoolId)
                    .enabled()
                    .filter(p -> clazzLevel == 0 || ClassJieHelper.toClazzLevel(p.getJie(), p.getEduSystemType()).getLevel() == clazzLevel)
                    .nature()
                    .originalLocationsAsList()
                    .stream()
                    .map(Clazz.Location::getId)
                    .collect(Collectors.toList());

            Map<Long, List<Long>> clazzStudentIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzIds(clazzIds);
            Set<Long> studentIds = clazzStudentIds.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            Map<Long, User> userMap = userLoaderClient.loadUsers(studentIds);

            // 同名学生的学生Id->clazzId map
            Map<Long, Long> sameNameStudentIdClazzIdMap = new LinkedHashMap<>();
            clazzStudentIds.forEach((cid, sids) ->
                    sids.forEach(studentId -> {
                        User student = userMap.get(studentId);
                        if (student != null && Objects.equals(student.fetchRealname(), studentName)) {
                            sameNameStudentIdClazzIdMap.put(student.getId(), cid);
                        }
                    })
            );

            // 读取班级信息
            Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(sameNameStudentIdClazzIdMap.values())
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));

            sameNameStudentIdClazzIdMap.forEach((studentId, clazzId) -> {
                Map<String, Object> map = new LinkedHashMap<>();
                Clazz clazz = clazzs.get(clazzId);
                if (clazz != null) {
                    map.put("clazzType", clazz.getClassType());
                    map.put("clazzName", clazz.getClassName());
                    map.put("eduSystem", clazz.getEduSystem());
                    map.put("userId", studentId);
                    map.put("jie", clazz.getJie());
                    result.add(map);
                }
            });
        }

        return result;
    }

    /**
     * 修改密码后处理
     *
     * @param user
     * @param newPassword
     */
    public void onPasswordReset(User user, String newPassword) {
        if (user == null) {
            return;
        }

        //若是教师，修改密码之后发送短信
        if (user.fetchUserType() == UserType.TEACHER) {
            sendPasswordChangeNoticeForTeacher(user, newPassword);
        }

        // 如果学生修改密码，更新学生端sessionkey
        if (user.fetchUserType() == UserType.STUDENT) {
            updateAppSessionKeyForStudent(user);
        }

        // 如果老师修改密码，更新老师端sessionkey
        if (user.fetchUserType() == UserType.TEACHER) {
            updateAppSessionKeyForTeacher(user);
        }
    }

    /**
     * 修改密码后更新学生App的session key
     *
     * @param user
     * @author changyuan.liu
     */
    public void updateAppSessionKeyForStudent(User user) {
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", user.getId());
        if (vendorAppsUserRef != null) {
            vendorServiceClient.expireSessionKey(
                    "17Student",
                    user.getId(),
                    SessionUtils.generateSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), user.getId()));
        }
    }

    ///////////////////////////////////////////////private methods////////////////////////////////////////////

    /**
     * 修改密码后给老师发送邮件通知
     *
     * @param user
     * @param newPassword
     * @author changyuan.liu
     */
    private void sendPasswordChangeNoticeForTeacher(User user, String newPassword) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        if (ua != null && ua.getSensitiveEmail() != null) {
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("name", user.getProfile().getRealname());
            content.put("userId", user.getId());
            content.put("password", newPassword);   // <- put new password here
            content.put("hotline", Constants.HOTLINE_SPACED);
            content.put("date", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
            content.put("time", DateUtils.dateToString(new Date(), "yyyy年MM月dd日 HH点mm分"));

            // 短信内容不能含有 “操” --> "...如非本人操作请与我们联系..."
            userEmailServiceClient.buildEmail(EmailTemplate.teachermodifypassword)
                    .to(ua)
                    .subject("您已更改在一起作业的个人资料")
                    .content(content)
                    .send();
        }
    }


    /**
     * 修改密码后更新老师app的session key
     *
     * @param user
     * @author changyuan.liu
     * @author peng.jiang
     */
    private void updateAppSessionKeyForTeacher(User user) {
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Teacher", user.getId());
        if (vendorAppsUserRef != null) {
            vendorServiceClient.expireSessionKey(
                    "17Teacher",
                    user.getId(),
                    SessionUtils.generateSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), user.getId()));
        }
    }
}
