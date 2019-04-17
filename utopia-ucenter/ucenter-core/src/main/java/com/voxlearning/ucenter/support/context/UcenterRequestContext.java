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

package com.voxlearning.ucenter.support.context;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.support.LastUserNameCookieManager;
import com.voxlearning.ucenter.support.PageBlockContentGenerator;
import com.voxlearning.ucenter.support.gray.StudentWebGrayFunction;
import com.voxlearning.ucenter.support.gray.TeacherWebGrayFunction;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.*;
import com.voxlearning.utopia.service.user.api.mappers.UserSecurity;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.ResearchStaffLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 这个类可能写的很tricky，很多是特殊优化，如果要动请先做综合评估
 */
@Slf4j
public class UcenterRequestContext extends UtopiaHttpRequestContext {

    @Setter private RaikouSystem raikouSystem;
    @Setter private PageBlockContentServiceClient pageBlockContentServiceClient;
    @Setter private UserLoaderClient userLoaderClient;
    @Setter private TeacherLoaderClient teacherLoaderClient;
    @Setter private StudentLoaderClient studentLoaderClient;
    @Setter private ResearchStaffLoaderClient researchStaffLoaderClient;
    @Setter private GrayFunctionManagerClient grayFunctionManagerClient;

    @Setter @Getter private long startTimeMillis;

    private User currentUser;

    private Boolean isCurrentUserTeacher;
    private Boolean isCurrentUserStudent;
    private Boolean isCurrentUserParent;
    private Boolean isCurrentUserExam;
    private Boolean isCurrentUserAdmin;
    private Boolean isCurrentUserResearchStaff;
    private Boolean isCurrentUserMarketer;

    private PageBlockContentGenerator pageBlockContentGenerator;

    public UcenterRequestContext(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
        startTimeMillis = System.currentTimeMillis();
    }

    public boolean isCurrentUserTeacher() {
        if (isCurrentUserTeacher == null)
            isCurrentUserTeacher = getRoleTypes().contains(RoleType.ROLE_TEACHER);
        return isCurrentUserTeacher;
    }

    public boolean isCurrentUserStudent() {
        if (isCurrentUserStudent == null)
            isCurrentUserStudent = getRoleTypes().contains(RoleType.ROLE_STUDENT);
        return isCurrentUserStudent;
    }

    public boolean isCurrentUserParent() {
        if (isCurrentUserParent == null)
            isCurrentUserParent = getRoleTypes().contains(RoleType.ROLE_PARENT);
        return isCurrentUserParent;
    }

    public boolean isCurrentUserExam() {
        if (isCurrentUserExam == null) {
            isCurrentUserExam = getRoleTypes().contains(RoleType.ROLE_TEMPORARY_EXAMINATION)
                    || getRoleTypes().contains(RoleType.ROLE_TEMPORARY_TEAM_EXAMINATION);
        }
        return isCurrentUserExam;
    }

    public boolean isCurrentUserMarketer() {
        if (isCurrentUserMarketer == null) {
            isCurrentUserMarketer = getRoleTypes().contains(RoleType.ROLE_MARKETER);
        }
        return isCurrentUserMarketer;
    }

    public boolean isCurrentUserAdmin() {
        if (isCurrentUserAdmin == null) {
            isCurrentUserAdmin = getRoleTypes().contains(RoleType.ROLE_ADMIN);
        }
        return isCurrentUserAdmin;
    }

    public boolean isCurrentUserResearchStaff() {
        if (isCurrentUserResearchStaff == null) {
            isCurrentUserResearchStaff = getRoleTypes().contains(RoleType.ROLE_RESEARCH_STAFF);
        }
        return isCurrentUserResearchStaff;
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            if (isCurrentUserStudent()) {
                //getCurrentStudent/getCurrentStudentDetail 依赖这个实现
                currentUser = studentLoaderClient.loadStudentDetail(getUserId());
            } else if (isCurrentUserTeacher()) {
                //getCurrentTeacher/getCurrentTeacherDetail 依赖这个实现
                currentUser = teacherLoaderClient.loadTeacherDetail(getUserId());
            } else if (isCurrentUserParent()) {
                currentUser = raikouSystem.loadUser(getUserId());
                if (currentUser.fetchUserType() != UserType.PARENT) {
                    currentUser = null;
                }
            } else if (isCurrentUserResearchStaff()) {
                //getCurrentResearchStaff/getCurrentResearchStaffDetail 依赖这个实现
                currentUser = researchStaffLoaderClient.loadResearchStaffDetail(getUserId());
            } else if (isCurrentUserExam() || isCurrentUserAdmin() || isCurrentUserMarketer()) {
                currentUser = raikouSystem.loadUser(getUserId());
            }
        }
        return currentUser;
    }


    public Teacher getCurrentTeacher() {
        return (Teacher) getCurrentUser();
    }

    public TeacherDetail getCurrentTeacherDetail() {
        return (TeacherDetail) getCurrentUser();
    }

    public Student getCurrentStudent() {
        return (Student) getCurrentUser();
    }

    public StudentDetail getCurrentStudentDetail() {
        return (StudentDetail) getCurrentUser();
    }

    public ResearchStaff getCurrentResearchStaff() {
        return (ResearchStaff) getCurrentUser();
    }

    public ResearchStaffDetail getCurrentResearchStaffDetail() {
        return (ResearchStaffDetail) getCurrentUser();
    }

    public synchronized PageBlockContentGenerator getPageBlockContentGenerator() {
        if (pageBlockContentGenerator == null) {
            pageBlockContentGenerator = new PageBlockContentGenerator(pageBlockContentServiceClient);
        }
        return pageBlockContentGenerator;
    }

    @Override
    public Object getRootModelAttribute(String key) {
        switch (key) {
            case "currentUser":
                return getCurrentUser();
            case "currentTeacherDetail":
                return getCurrentTeacherDetail();
            case "currentStudentDetail":
                return getCurrentStudentDetail();
            case "currentResearchStaffDetail":
                return getCurrentResearchStaffDetail();
            case "pageBlockContentGenerator":
                return getPageBlockContentGenerator();
            case "currentCdnType":
                return CdnResourceUrlGenerator.formalizeCdnType(getRequest()).getKey();
            case "cdnDomainMapKeys":
                return ProductConfig.getCdnDomainMapKeys();
            case "currentTeacherWebGrayFunction":
                return new TeacherWebGrayFunction(grayFunctionManagerClient, getCurrentTeacherDetail());
            case "currentStudentWebGrayFunction":
                return new StudentWebGrayFunction(grayFunctionManagerClient, getCurrentStudentDetail());
        }
        return super.getRootModelAttribute(key);
    }

    private LastUserNameCookieManager lastUserNameCookieManager;

    public synchronized LastUserNameCookieManager getLastUserNameCookieManager() {
        if (lastUserNameCookieManager == null) {
            lastUserNameCookieManager = new LastUserNameCookieManager(getCookieManager());
        }
        return lastUserNameCookieManager;
    }

    public void saveAuthenticationStates(int expire, UserSecurity mapper) {
        List<RoleType> list = mapper.getRoleTypes();
        RoleType[] roleTypes = list.toArray(new RoleType[list.size()]);
        saveAuthenticationStates(expire, mapper.getUserId(), mapper.getPassword(), roleTypes);
    }

    @Override
    protected void authenticationStatesSaved() {
        getLastUserNameCookieManager().cleanupLastUserNameCookie();
    }
}
