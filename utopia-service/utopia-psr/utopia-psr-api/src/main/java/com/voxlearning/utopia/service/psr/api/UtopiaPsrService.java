/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.monitor.ServiceMetric;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.psr.entity.PsrExamContent;
import com.voxlearning.utopia.service.psr.entity.PsrExamEnSimilarContentEx;
import com.voxlearning.utopia.service.psr.entity.PsrPrimaryAppEnContent;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Chaoli li
 * @since 19:49,2014/06/03.
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@ServiceMetric
public interface UtopiaPsrService extends IPingable {

    /*
     * 返回值：PsrExamContent
     * PsrExamContent.errorContent == "success" 取题成功,反之为失败,并且包含了失败信息
     */
    PsrExamContent getPsrExam(String product, String uType,
                              Long userId, int regionCode, Long bookId, Long unitId, int eCount,
                              float minP, float maxP, int grade);

    /*
     * 返回值：PsrExamContent
     * PsrExamContent.errorContent == "success" 取题成功,反之为失败,并且包含了失败信息
     */
    PsrExamContent getPsrExam(String product, String uType,
                              Long userId, int regionCode, String bookId, String unitId, int eCount,
                              float minP, float maxP, int grade, Subject subject);

    /*
     * 返回值：PsrExamEnSimilarContentEx
     * PsrExamEnSimilarContentEx.errorContent == "success" 取题成功,反之为失败,并且包含了失败信息
     */
    PsrExamEnSimilarContentEx getPsrExamEnSimilarByQid(String product, Long userId, List<String> qids, int eCount);

    /*
     * 小学英语 应用推荐接口
     * 返回值：PsrPrimaryAppEnContent
     * PsrPrimaryAppEnContent.errorContent == "success" 取题成功,反之为失败,并且包含了失败信息
     */
    PsrPrimaryAppEnContent getPsrPrimaryAppEn(String product, Long userId,
                                              int regionCode, String bookId, String unitId, int eCount,
                                              String eType);

    PsrPrimaryAppEnContent getPsrPrimaryAppEn(String product, Long userId,
                                              int regionCode, Long bookId, Long unitId, int eCount,
                                              String eType);
}