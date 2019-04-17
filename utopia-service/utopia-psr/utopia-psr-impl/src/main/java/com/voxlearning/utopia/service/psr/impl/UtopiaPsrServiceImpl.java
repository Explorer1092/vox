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

package com.voxlearning.utopia.service.psr.impl;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.psr.api.UtopiaPsrService;
import com.voxlearning.utopia.service.psr.entity.PsrExamContent;
import com.voxlearning.utopia.service.psr.entity.PsrExamEnSimilarContentEx;
import com.voxlearning.utopia.service.psr.entity.PsrPrimaryAppEnContent;
import com.voxlearning.utopia.service.psr.impl.appen.PsrAppEnController;
import com.voxlearning.utopia.service.psr.impl.appen.PsrAppEnNewController;
import com.voxlearning.utopia.service.psr.impl.examcn.PsrExamCnController;
import com.voxlearning.utopia.service.psr.impl.examen.PsrExamEnController;
import com.voxlearning.utopia.service.psr.impl.exammath.PsrExamMathController;
import com.voxlearning.utopia.service.psr.impl.similarity.PsrExamEnSimilarExController;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;


/**
 * @author ChaoLi Lee
 * @since 11:37,2014/06/03.
 */
@Named
@ExposeService(interfaceClass = UtopiaPsrService.class)
public class UtopiaPsrServiceImpl extends SpringContainerSupport implements UtopiaPsrService {
    @Inject private PsrExamEnController psrExamEnController;
    @Inject private PsrAppEnController psrAppEnController;
    @Inject private PsrExamEnSimilarExController psrExamEnSimilarExController;
    @Inject private PsrExamMathController psrExamMathController;
    @Inject private PsrExamCnController psrExamCnController;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private PsrAppEnNewController psrAppEnNewController;

    /*
     * FIXME: 现在这个请求应该是从 washington:/student/afenti/arena/join.vpage 转发过来的，而且会有每天一次的缓存
     */
    @Override
    public PsrExamContent getPsrExam(String product, String uType,
                                     Long userId, int regionCode, Long bookId, Long unitId, int eCount,
                                     float minP, float maxP, int grade) {
        // 默认教材 新版-PEP-三年级(上) :old_id: 100278,new_id:BK_10300000265057, unit : -1
        String newBookId = "BK_10300000265057";
        String newUnitId = "-1";
        NewBookProfile newBookProfile = null;
        if (bookId > 0) {
            newBookProfile = newContentLoaderClient.loadNewBookProfileByOldId(Subject.ENGLISH, bookId);
            if (newBookProfile != null)
                newBookId = newBookProfile.getId();
        }
        if (unitId > 0) {
            newBookProfile = newContentLoaderClient.loadNewBookProfileByOldId(Subject.ENGLISH, unitId);
            if (newBookProfile != null)
                newUnitId = newBookProfile.getId();
        }

        return psrExamEnController.deal(product, uType, userId, regionCode, newBookId, newUnitId, eCount, minP, maxP, grade);
    }

    @Override
    public PsrExamContent getPsrExam(String product, String uType,
                                     Long userId, int regionCode, String bookId, String unitId, int eCount,
                                     float minP, float maxP, int grade, Subject subject) {
        if (Subject.CHINESE.equals(subject))
            return psrExamCnController.deal(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);
        else if (Subject.MATH.equals(subject))
            return psrExamMathController.deal(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);
        else
            return psrExamEnController.deal(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);
    }

    @Override
    public PsrExamEnSimilarContentEx getPsrExamEnSimilarByQid(String product, Long userId, List<String> qids, int eCount) {

        return psrExamEnSimilarExController.deal(product, userId, qids, eCount);
    }

    // ******************************************************************************************************************* //
    // fixme 以下应用类的接口适配新的教材bookId:String

    @Override
    public PsrPrimaryAppEnContent getPsrPrimaryAppEn(String product, Long userId,
                                                     int regionCode, String bookId, String unitId, int eCount,
                                                     String eType) {

        return psrAppEnNewController.deal(product, userId, regionCode, bookId, unitId, eCount, eType);
    }

    // ******************************************************************************************************************* //
    // fixme 以下应用类的接口不做改动

    @Override
    public PsrPrimaryAppEnContent getPsrPrimaryAppEn(String product, Long userId,
                                                     int regionCode, Long bookId, Long unitId, int eCount,
                                                     String eType) {

        return psrAppEnController.deal(product, userId, regionCode, bookId.toString(), unitId.toString(), eCount, eType);
    }
}
