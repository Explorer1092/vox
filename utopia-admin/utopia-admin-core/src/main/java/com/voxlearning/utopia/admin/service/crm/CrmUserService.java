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

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.admin.support.SessionUtils;
import com.voxlearning.utopia.api.constant.RecordType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class CrmUserService extends AbstractAdminService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;

    // 表示Feedback state
    public static final Map<String, String> feedbackStateMap;

    static {
        Map<String, String> map = MiscUtils.<String, String>map()
                .add("0", "未处理")
                .add("1", "已确认")
                .add("2", "已解决")
                .add("3", "已关闭");
        feedbackStateMap = Collections.unmodifiableMap(map);
    }

    // 获得所有省份
    public List<Map<String, Object>> getAllProvince() {
        List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadProvinces();
        List<Map<String, Object>> provinces = new ArrayList<>();

        for (ExRegion region : regionList) {
            provinces.add(MiscUtils.<String, Object>map()
                    .add("code", region.getCode())
                    .add("name", region.getName())
            );
        }

        return provinces;
    }

    // 根据UserType返回对应的RecordType
    public RecordType userTypeToRecordType(UserType userType) {
        switch (userType) {
            case PARENT:
                return RecordType.家长操作;
            case RESEARCH_STAFF:
                return RecordType.教研员操作;
            case STUDENT:
                return RecordType.学生操作;
            case TEACHER:
                return RecordType.老师操作;
            case TEMPORARY:
                return RecordType.临时员工;
            case EMPLOYEE:
                return RecordType.市场人员操作;
            default:
                return RecordType.其他操作;
        }
    }

    /**
     * 重置用户密码
     * 如果mobile不为空，将进行手机号验证
     */
    public User resetUserPassword(Long userId, String password, boolean checkMobile, String mobile, String desc, String operator, String refer) {
        User user = raikouSystem.loadUser(userId);
        if (null == user) {
            throw new RuntimeException("user " + userId + " not found");
        }
        if (StringUtils.isEmpty(password)) {
            throw new RuntimeException("密码不能为空");
        }

        //验证手机
        if (checkMobile && !validateMobile(userId, mobile)) {
            throw new RuntimeException("手机号验证失败");
        }

        String operation = "管理员" + operator + "重置用户密码。" + (StringUtils.isEmpty(mobile) ? "" : "验证手机号[" + mobile + "]");

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperatorId(operator);
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("重置密码");
        userServiceRecord.setComments(operation + "问题描述[" + desc + "]");
        userServiceRecord.setAdditions("refer:" + refer);
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        //修改密码
        MapMessage message = userServiceClient.setPassword(user, password);
        if (!message.isSuccess()) {
            throw new RuntimeException("修改用户密码失败");
        }

        // 更新用户app端sessionkey，使其重新登录
        if (user.fetchUserType() == UserType.STUDENT) {
            expireUserAppSessionKey("17Student", userId);
        } else if (user.fetchUserType() == UserType.TEACHER) {
            expireUserAppSessionKey("17Teacher", userId);
        }
        return user;
    }

    private void expireUserAppSessionKey(String appKey, Long userId) {
        VendorAppsUserRef ref = vendorLoaderClient.loadVendorAppUserRef(appKey, userId);
        if (ref != null) {
            vendorServiceClient.expireSessionKey(
                    appKey,
                    userId,
                    SessionUtils.generateSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), userId));
        }
    }

    /**
     * 验证手机号
     * 验证顺序：a.家长绑定手机，b.家长未绑定的手机,c.学生绑定手机,d.学生未绑定的手机,c.进线日志填写的手机号
     * 如果abc不存在历史数据，不验证直接返回true；如果abc存在历史数据，则需要验证是否一致
     */
    private boolean validateMobile(Long userId, String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return true;
        }
        boolean valid = true;
        List<StudentParent> parents = parentLoaderClient.loadStudentParents(userId);
        for (StudentParent parent : parents) {
            //家长绑定手机验证
            UserAuthentication ua = raikouSystem.loadUserAuthentication(parent.getParentUser().getId());
            if (null != ua && ua.isMobileAuthenticated()) {
                valid = sensitiveUserDataServiceClient.mobileEquals(ua.getSensitiveMobile(), mobile);
                if (valid) {
                    return true;
                }
            }

            //家长未绑定手机验证
            if (!StringUtils.isEmpty(ua.getSensitiveMobile())) {
                if (sensitiveUserDataServiceClient.mobileEquals(ua.getSensitiveMobile(), mobile)) {
                    return true;
                }
            }
        }

        //学生绑定手机验证
        UserAuthentication ua = raikouSystem.loadUserAuthentication(userId);
        if (null != ua && ua.isMobileAuthenticated()) {
            if (sensitiveUserDataServiceClient.mobileEquals(ua.getSensitiveMobile(), mobile)) {
                return true;
            }
        }

//        //学生未绑定手机验证
//        User student = userLoaderClient.loadUser(userId, UserType.STUDENT);
//        if (student != null && !StringUtils.isEmpty(student.getProfile().getSensitiveMobile())) {
//            if (sensitiveServiceClient.getSensitiveService().mobileEquals(student.getProfile().getSensitiveMobile(), mobile).getUninterruptibly()) {
//                return true;
//            }
//        }

        //进线日志手机验证
        //查找当前用户的重置密码进线日志，验证在结果中是否存在此手机号
        List<UserServiceRecord> userServiceRecords = userLoaderClient.loadUserServiceRecords(userId);
        for (UserServiceRecord record : userServiceRecords) {
            if (record.getComments() != null && record.getComments().contains("验证手机号[")) {  //如果是第一次在进线日志里做验证，则不做进线日志验证
                valid = record.getComments().contains("验证手机号[" + mobile + "]");
                if (valid) {
                    return true;
                }
            }
        }

        return valid;
    }

    /**
     * 解除学生与家长的关联关系
     */
    public void unbindStudentParentRef(final Long studentId, final Long parentId, final String operator) {
        if (null == studentId || null == parentId) {
            throw new IllegalArgumentException("IllegalArgument [studentId:" + studentId + ",parentId:" + parentId + "]");
        }
        final List<StudentParentRef> refs = new ArrayList<>();
        for (StudentParentRef ref : studentLoaderClient.loadStudentParentRefs(studentId)) {
            if (Objects.equals(ref.getParentId(), parentId)) {
                refs.add(ref);
            }
        }
        for (StudentParentRef ref : refs) {
            String operation = "解除学生" + studentId + "与家长" + parentId + "的关联";
            parentServiceClient.disableStudentParentRef(ref);

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(studentId);
            userServiceRecord.setOperatorId(operator);
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("与家长解除关联");
            userServiceRecord.setComments(operation);
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }
    }

}
