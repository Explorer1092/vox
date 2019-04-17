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

package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.Getter;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by jiangpeng on 16/5/13.
 */
public class AbstractMobileTeacherController extends AbstractMobileController {


    @Inject protected FlowerServiceClient flowerServiceClient;
    @Inject protected ClazzIntegralServiceClient clazzIntegralServiceClient;

    @Inject
    @Getter
    protected GrayFunctionManagerClient grayFunctionManagerClient;

    protected String generateShareClazzUrl(Teacher teacher){
        if(teacher == null)
            return null;
        String encodeTeacherName;
        String encodeSubject;
        try {
            encodeTeacherName = URLEncoder.encode(teacher.fetchRealname(), "utf-8");
            encodeSubject = URLEncoder.encode(teacher.getSubject().getValue(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            encodeTeacherName = "";
            encodeSubject = "";
        }
        return fetchMainsiteUrlByCurrentSchema() + StringUtils.formatMessage("/view/mobile/teacher/share?id={}&name={}&subject={}", teacher.getId(), encodeTeacherName, encodeSubject);
    }


    protected List<Clazz> getTeacherClazzListForFlower(Teacher teacher, Subject subject) {
        if (teacher.getSubjects() != null && teacher.getSubjects().size() == 1) {
            return deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId()).stream().filter(Clazz::isPublicClazz)
                    .filter(e -> !e.isTerminalClazz())
                    .collect(Collectors.toList());
        } else {
            if (subject == null) {
                Set<Long> allTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
                return deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(allTeacherIds).values().stream().flatMap(Collection::stream).filter(Clazz::isPublicClazz)
                        .filter(e -> !e.isTerminalClazz()).collect(Collectors.toList());
            } else {
                Long subTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
                return deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(subTeacherId).stream().filter(Clazz::isPublicClazz)
                        .filter(e -> !e.isTerminalClazz())
                        .collect(Collectors.toList());
            }
        }
    }
}
