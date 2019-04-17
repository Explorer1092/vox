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

package com.voxlearning.utopia.service.vendor.consumer.util;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2015/12/28
 */
public class AppMessageUtils {

    public static List<AppMessage> generateAppUserMessage(List<Long> userIdList, Integer messageType, String title, String content, String imgUrl, String linkUrl, Integer linkType, String extInfoStr) {
        List<AppMessage> messageList = new ArrayList<>();
        if (CollectionUtils.isEmpty(userIdList)) {
            return messageList;
        }
        userIdList.forEach(p -> {
            AppMessage message = new AppMessage();
            message.setUserId(p);
            message.setMessageType(messageType);
            message.setTitle(title);
            message.setContent(content);
            message.setImageUrl(imgUrl);
            message.setLinkUrl(linkUrl);
            message.setLinkType(linkType);
            if (StringUtils.isNotBlank(extInfoStr)) {
                Map<String, Object> extInfo = JsonStringDeserializer.getInstance().deserialize(extInfoStr);
                message.setExtInfo(extInfo);
            }
            messageList.add(message);
        });
        return messageList;
    }


    public static AppMessageSource getMessageSource(String appKey, Object userDetail) {
        AppMessageSource source;
        switch (appKey) {
            case "17Parent":
                source = AppMessageSource.PARENT;
                break;
            case "17JuniorPar":
                source = AppMessageSource.JUNIOR_PARENT;
                break;
            case "17Student":
                if (userDetail instanceof StudentDetail) {
                    StudentDetail studentDetail = (StudentDetail) userDetail;
                    if (studentDetail.isJuniorStudent()) {
                        source = AppMessageSource.XUESHE;
                    } else if (studentDetail.isSeniorStudent()) {
                        source = AppMessageSource.XUESHE; // FIXME 高中暂时先跟初中一样
                    } else if (studentDetail.isInfantStudent()) {
                        source = AppMessageSource.INFANT;
                    } else {
                        source = AppMessageSource.STUDENT;
                    }
                    break;
                } else {
                    source = AppMessageSource.UNKNOWN;
                    break;
                }

            case "17JuniorStu":
                if (userDetail instanceof StudentDetail) {
                    StudentDetail studentDetail = (StudentDetail) userDetail;
                    if (studentDetail.isJuniorStudent()) {
                        source = AppMessageSource.JUNIOR_STUDENT;
                    } else if (studentDetail.isSeniorStudent()) {
                        source = AppMessageSource.JUNIOR_STUDENT; // FIXME 高中暂时先跟初中一样
                    } else {
                        source = AppMessageSource.UNKNOWN;
                    }
                    break;
                } else {
                    source = AppMessageSource.UNKNOWN;
                    break;
                }

            case "17Teacher":
                if (userDetail instanceof TeacherDetail) {
                    TeacherDetail teacherDetail = (TeacherDetail) userDetail;
                    if (Ktwelve.JUNIOR_SCHOOL.equals(teacherDetail.getKtwelve())) {
                        source = AppMessageSource.JUNIOR_TEACHER;
                    } else if (Ktwelve.SENIOR_SCHOOL.equals(teacherDetail.getKtwelve())) {
                        source = AppMessageSource.JUNIOR_TEACHER; // FIXME 高中暂时先跟初中一样
                    } else if (Ktwelve.PRIMARY_SCHOOL.equals(teacherDetail.getKtwelve())) {
                        source = AppMessageSource.PRIMARY_TEACHER;
                    } else if (Ktwelve.INFANT.equals(teacherDetail.getKtwelve())) {
                        source = AppMessageSource.INFANT_TEACHER;
                    } else
                        source = AppMessageSource.UNKNOWN;
                    break;
                }

            case "17JuniorTea":
                if (userDetail instanceof TeacherDetail) {
                    TeacherDetail teacherDetail = (TeacherDetail) userDetail;
                    if (Ktwelve.JUNIOR_SCHOOL.equals(teacherDetail.getKtwelve())) {
                        source = AppMessageSource.JUNIOR_TEACHER;
                    } else if (Ktwelve.SENIOR_SCHOOL.equals(teacherDetail.getKtwelve())) {
                        source = AppMessageSource.JUNIOR_TEACHER; // FIXME 高中暂时先跟初中一样
                    } else
                        source = AppMessageSource.UNKNOWN;
                    break;
                }
            default:
                source = AppMessageSource.UNKNOWN;
        }
        return source;
    }
}
