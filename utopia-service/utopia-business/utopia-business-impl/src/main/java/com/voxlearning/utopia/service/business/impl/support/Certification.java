/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.support;

import lombok.Cleanup;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

abstract public class Certification {
    private static final String AMBASSADOR_CERTIFICATION_PATH = "certification/ambassador_apiclient_cert.p12";//校园大使微信号 红包证书
    private static final String TEACHER_CERTIFICATION_PATH = "certification/teacher_apiclient_cert.p12";//老师微信号 红包证书
    private static final String CHIPSENGLISH_CERTIFICATION_PATH = "certification/chips_apiclient_cert.p12";//薯条英语微信号 红包证书


    @Getter private static final byte[] ambassadorCertificationContent;
    @Getter private static final byte[] teacherCertificationContent;
    @Getter private static final byte[] chipsEnglishCertificationContent;

    public static InputStream openAmbassadorCertificationInputStream() {
        return new ByteArrayInputStream(ambassadorCertificationContent);
    }

    public static InputStream openTeacherCertificationInputStream() {
        return new ByteArrayInputStream(teacherCertificationContent);
    }

    static {
        Resource resource = new ClassPathResource(AMBASSADOR_CERTIFICATION_PATH);
        Resource teacherResource = new ClassPathResource(TEACHER_CERTIFICATION_PATH);
        Resource chipsResource = new ClassPathResource(CHIPSENGLISH_CERTIFICATION_PATH);
        if (!resource.exists() || !resource.isReadable()
                || !teacherResource.exists() || !teacherResource.isReadable()) {
            throw new IllegalStateException("Unable to load certification from classpath");
        }
        try {
            @Cleanup InputStream ambassadorIn = resource.getInputStream();
            ambassadorCertificationContent = IOUtils.toByteArray(ambassadorIn);

            @Cleanup InputStream teacherIn = teacherResource.getInputStream();
            teacherCertificationContent = IOUtils.toByteArray(teacherIn);

            @Cleanup InputStream chipsIn = chipsResource.getInputStream();
            chipsEnglishCertificationContent = IOUtils.toByteArray(chipsIn);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load certification", ex);
        }
    }
}
