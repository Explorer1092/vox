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

package com.voxlearning.utopia.agent.service;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.dao.mongo.AgentHiddenTeacherDao;
import com.voxlearning.utopia.agent.interceptor.AgentHttpRequestContext;
import com.voxlearning.utopia.agent.persist.entity.AgentHiddenTeacher;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.service.content.consumer.ContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.ContentServiceClient;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;

import javax.inject.Inject;
import java.util.*;

/**
 * Base Abrstract Agent Service Class.
 * <p>
 * Created by Alex on 14-7-5.
 */
abstract public class AbstractAgentService extends SpringContainerSupport {

    @Inject
    protected UtopiaSqlFactory utopiaSqlFactory;
    protected UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Inject protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject protected TeacherLoaderClient teacherLoaderClient;
    @Inject protected StudentLoaderClient studentLoaderClient;
    @Inject protected UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject protected ClazzServiceClient clazzServiceClient;
    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject protected UserServiceClient userServiceClient;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected UserLoginServiceClient userLoginServiceClient;
    @Inject protected TeacherServiceClient teacherServiceClient;
    @Inject protected ContentLoaderClient contentLoaderClient;
    @Inject protected ContentServiceClient contentServiceClient;
    @Inject protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject protected CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject protected AgentHiddenTeacherDao agentHiddenTeacherDao;
    @Inject
    protected CdnResourceUrlGenerator cdnResourceUrlGenerator;

    // 此处在多线程时会有问题
    protected AuthCurrentUser getCurrentUser() {
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        return context.getCurrentUser();
    }

    // 此处在多线程时会有问题
    protected Long getCurrentUserId() {
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        return context.getCurrentUser().getUserId();
    }

    // 判断老师是否是真老师， 是真老师的话返回true;
    protected boolean isRealTeacher(Long teacherId){
        return isRealTeacher(Collections.singleton(teacherId)).getOrDefault(teacherId, true);
    }

    protected Map<Long, Boolean> isRealTeacher(Collection<Long> teacherIds){
        Map<Long, Boolean> retMap = new HashMap<>();
        if(CollectionUtils.isEmpty(teacherIds)){
            return retMap;
        }
        Map<Long, TeacherExtAttribute> teacherExtMap = teacherLoaderClient.loadTeacherExtAttributes(teacherIds);
        teacherIds.forEach(p -> {
            TeacherExtAttribute extAttribute = teacherExtMap.get(p);
            if(extAttribute != null && Objects.equals(extAttribute.getIsFake(),true) && Objects.equals(extAttribute.getValidationType(), CrmTeacherFakeValidationType.MANUAL_VALIDATION.name)){
                retMap.put(p, false);
            }else {
                retMap.put(p, true);
            }
        });
        return retMap;
    }


    protected boolean isHiddenTeacher(Long teacherId){
        return isHiddenTeacher(Collections.singleton(teacherId)).getOrDefault(teacherId, false);
    }

    protected Map<Long, Boolean> isHiddenTeacher(Collection<Long> teacherIds){
        Map<Long, Boolean> retMap = new HashMap<>();
        if(CollectionUtils.isEmpty(teacherIds)){
            return retMap;
        }
        Map<Long, AgentHiddenTeacher> hiddenTeacherMap = agentHiddenTeacherDao.loads(teacherIds);
        teacherIds.forEach(p -> {
            AgentHiddenTeacher hiddenTeacher = hiddenTeacherMap.get(p);
            if(hiddenTeacher != null){
                retMap.put(p, true);
            }else {
                retMap.put(p, false);
            }
        });
        return retMap;
    }

    protected String getUserAvatarImgUrl(User user) {
        return getUserAvatarImgUrl(user.fetchImageUrl());
    }

    protected String getUserAvatarImgUrl(String imgFile) {
        String imgUrl;
        if (!StringUtils.isEmpty(imgFile)) {
            imgUrl = "gridfs/" + imgFile;
        } else {
            imgUrl = "upload/images/avatar/avatar_default.png";
        }

        return getCdnBaseUrlAvatarWithSep() + imgUrl;
    }

    protected String getCdnBaseUrlAvatarWithSep() {
        return cdnResourceUrlGenerator.getCdnBaseUrlAvatarWithSep(null);
    }

}
