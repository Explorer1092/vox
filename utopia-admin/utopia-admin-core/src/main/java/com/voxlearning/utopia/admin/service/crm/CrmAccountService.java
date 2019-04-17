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
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.RealnameRule;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.data.UserDataAuthorityMapper;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.RSManagedRegionType;
import com.voxlearning.utopia.service.user.api.entities.ResearchStaffManagedRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.cache.UserCache;
import com.voxlearning.utopia.service.user.consumer.UserManagementClient;
import com.voxlearning.utopia.service.user.consumer.client.ResearchStaffUserServiceClient;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * @author xin.xin
 * @since 2014-1-26
 */
@Service
public class CrmAccountService extends AbstractAdminService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private UserManagementClient userManagementClient;
    @Inject private ResearchStaffUserServiceClient researchStaffUserServiceClient;

    public Map<String, Object> findAccounts(Map<String, String> paras, int pageNum, int pageSize) {
        return userManagementClient.findAccounts(paras, pageNum, pageSize);
    }

    public void updateUserInfo(Map<String, Object> pars, Long operatorId, String operatorName) {
        if (pars.get("realName").toString().trim().length() == 0) {
            throw new UtopiaRuntimeException("请输入帐号姓名！");
        }

        if (!RealnameRule.isValidRealName(pars.get("realName").toString())) {
            throw new UtopiaRuntimeException("用户姓名不合规范！");
        }
        String mobile = SafeConverter.toString(pars.get("mobile"));
        if (StringUtils.isNotBlank(mobile)) {
            if (!MobileRule.isMobile(mobile)) {
                throw new UtopiaRuntimeException("用户手机号不合规范！");
            }
        }

        User user = userLoaderClient.loadUserIncludeDisabled(Long.valueOf(pars.get("id").toString()));
        if (null == user) {
            throw new UtopiaRuntimeException("user " + pars.get("id") + " not found！");
        }

        //更新用户信息
        user.setUpdateTime(new Date());
        userServiceClient.changeName(user.getId(), pars.get("realName").toString());
        if (StringUtils.isNotBlank(mobile)) {
            userServiceClient.updateEmailMobile(user.getId(), "", mobile);
        }

        com.voxlearning.alps.spi.bootstrap.LogCollector.info("backend-general", MiscUtils.map("usertoken", user.getId(),
                "usertype", user.getUserType(),
                "platform", "crm",
                "version", "",
                "op", "change user name",
                "mod1", user.fetchRealname(),
                "mod2", pars.get("realName").toString(),
                "mod3", user.getAuthenticationState(),
                "mod4", operatorName));

    }

    //查找用户被授权的地区
    public List<UserDataAuthorityMapper> getRstaffDataAuthor(Long userId) {
        User user = userLoaderClient.loadUserIncludeDisabled(userId);
        if (user == null || user.isDisabledTrue() || user.fetchUserType() != UserType.RESEARCH_STAFF) {
            return Collections.emptyList();
        }

        List<UserDataAuthorityMapper> regionMappers = new ArrayList<>();
        // 教研员，读取管辖区域
        List<ResearchStaffManagedRegion> regions = userManagementClient.findByRStaffId(userId);

        List<Integer> regionCodes = new ArrayList<>();
        List<Long> schoolIds = new ArrayList<>();

        for (ResearchStaffManagedRegion region : regions) {
            if (region.getManagedRegionType() == RSManagedRegionType.SCHOOL) {
                schoolIds.add(region.getManagedRegionCode());
            } else {
                regionCodes.add(region.getManagedRegionCode().intValue());
            }
        }

        if (regionCodes.size() > 0) {
            for (ExRegion exRegion : raikouSystem.getRegionBuffer().loadRegions(regionCodes).values()) {
                UserDataAuthorityMapper mapper = new UserDataAuthorityMapper();
                mapper.setRegionCodes(String.valueOf(exRegion.getId()));
                mapper.setRegionNames(exRegion.getName());
                mapper.setRegionType(ResearchStaff.convertToManagedRegionTypeStr(exRegion.fetchRegionType()));
                regionMappers.add(mapper);
            }
        }

        if (schoolIds.size() > 0) {
            for (School school : schoolLoaderClient.getSchoolLoader().loadSchools(schoolIds).getUninterruptibly().values()) {
                UserDataAuthorityMapper mapper = new UserDataAuthorityMapper();
                mapper.setRegionCodes(String.valueOf(school.getId()));
                mapper.setRegionNames(school.getCname());
                mapper.setRegionType("SCHOOL");
                regionMappers.add(mapper);
            }
        }

        return regionMappers;
    }

    public void updateRstaffAuthorityRegion(Long userId, String regions, String regionTypes) {
        User user = userLoaderClient.loadUserIncludeDisabled(userId);
        if (user == null || user.isDisabledTrue() || user.fetchUserType() != UserType.RESEARCH_STAFF) {
            throw new IllegalArgumentException("Only support RESEARCH_STAFF!!!");
        }

        String[] rs = regions.split(",");
        // 教研员可设置多个区域，但只能取一个作为AuthorityRegion
        String[] regionArr = StringUtils.split(regions, ',');
        String[] regionTypeArr = StringUtils.split(regionTypes, ',');

        researchStaffUserServiceClient.unsetManagedRegionByRStaffId(userId);

        for (int i = 0; i < regionArr.length; i++) {
            // 更新教研员管辖区域表
            ResearchStaffManagedRegion researchStaffManagedRegion = new ResearchStaffManagedRegion();
            researchStaffManagedRegion.setRstaffId(userId);
            researchStaffManagedRegion.setManagedRegionCode(Long.valueOf(regionArr[i]));
            researchStaffManagedRegion.setManagedRegionType(RSManagedRegionType.getRSManagedRegionType(regionTypeArr[i]));
            researchStaffUserServiceClient.setManagedRegion(researchStaffManagedRegion);
        }

        // 更新用户缓存
        String ck = User.ck_id(userId);
        UserCache.getUserCache().delete(ck);
    }

//    private void sendEmailAndSmsToAuthenticatedTeacher(Long teacherId) {
//        User teacher = userLoaderClient.loadUserIncludeDisabled(teacherId);
//        String name = StringUtils.defaultString(teacher.getProfile().getRealname());
//        if (!StringUtils.isBlank(teacher.getProfile().getSensitiveEmail())) {
//
//            // send email.
//            String subject = "恭喜您成为一起作业认证老师！";
//            Map<String, Object> content = new LinkedHashMap<>();
//            content.put("name", name);
//            content.put("date", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
//            content.put("hotline", Constants.HOTLINE_SPACED);
//
//            userEmailServiceClient.buildEmail(EmailTemplate.teacherAuthenticationsuccess)
//                    .to(teacher)
//                    .subject(subject)
//                    .content(content)
//                    .send();
//        }
//
//
//        if (!StringUtils.isBlank(teacher.getProfile().getSensitiveMobile())) {
//            String payload = name + "老师，恭喜您已通过教师身份认证，快去为学生布置作业吧！详情请登录查看";
//            userSmsServiceClient.buildSms().to(teacher)
//                    .content(payload)
//                    .type(SmsType.CRM_TEACHER_AUTH_SUCC)
//                    .send();
//        }
//    }

}