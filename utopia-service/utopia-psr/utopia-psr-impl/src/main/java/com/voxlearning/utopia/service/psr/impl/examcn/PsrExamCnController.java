/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */
package com.voxlearning.utopia.service.psr.impl.examcn;

/**
 * Created by Administrator on 2016/4/21.
 */

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContextInstance;
import com.voxlearning.utopia.service.psr.impl.data.PsrAdaptivePaperQids;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.psr.impl.service.PsrAdaptivePaperConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Named
@Data
public class PsrExamCnController implements Serializable {

    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;
    @Inject private PsrExamContextInstance psrExamContextInstance;
    @Inject private PsrExamEnData psrExamEnData;
    @Inject private PsrExamCnCore psrExamCnCore;
    @Inject private PsrAdaptivePaperQids psrAdaptivePaperQids;
    @Inject private PsrAdaptivePaperConfig psrAdaptivePaperConfig;

    public PsrExamContent deal(String product, String uType,
                               Long userId, int regionCode, String bookId, String unitId, int eCount,
                               float minP, float maxP, int grade) {

        return dealAll(psrExamContextInstance.getAndInitPsrExamEnContext(product,
                uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade, Subject.CHINESE)
        );
    }

    public PsrExamContent dealAll(PsrExamContext psrExamContext) {
        Date dtB = new Date();
        PsrExamContent retExamContent = new PsrExamContent();

        if (psrExamContext == null)
            return logContent(retExamContent, null, "PsrExamCnController dealAll psrExamContext is null.", dtB, "error");

        psrExamContext.setPsrExamType("psrexamcn");


        PsrBookPersistenceNew psrBookPersistenceNew = psrExamEnData.getPsrBookPersistence(psrExamContext);
        if (psrBookPersistenceNew == null) {
            return logContent(retExamContent, psrExamContext, "BookError:" + psrExamContext.getBookId() + " ", dtB, "info");
        }

        String psrExamType = psrExamContext.getPsrExamType();
        // 个性化逻辑
        psrExamContext.setAdaptive(false);
        psrExamContext.setPsrExamType(psrExamType + "_id_p");
        retExamContent = dealCore(retExamContent, psrExamContext);
        if (!retExamContent.isSuccess() || retExamContent.getExamList().size() < psrExamContext.getECount()) {
            // 自适应补题逻辑
            psrExamContext.setAdaptive(true);
            psrExamContext.setPsrExamType(psrExamType + "_id_std");
            retExamContent = dealCore(retExamContent, psrExamContext);
        }
        // // FIXME: 2017/7/19  不知道why以前写的这个循环，看到了顺手改了。
//        for (int i = 0; i < 1; i++) {
//            // 个性化逻辑
//            psrExamContext.setAdaptive(false);
//            psrExamContext.setPsrExamType(psrExamType + "_id_p");
//            retExamContent = dealCore(retExamContent, psrExamContext);
//            if (retExamContent.isSuccess() && retExamContent.getExamList().size() >= psrExamContext.getECount())
//                break;
//
//            // 自适应补题逻辑
//            psrExamContext.setAdaptive(true);
//            psrExamContext.setPsrExamType(psrExamType + "_id_std");
//            retExamContent = dealCore(retExamContent, psrExamContext);
//            if (retExamContent.isSuccess() && retExamContent.getExamList().size() >= psrExamContext.getECount())
//                break;
//        }

        // 使用Paper补题 fixme del
        if (psrExamContext.getAdaptivePaperLevel() == 1
                || (psrExamContext.getAdaptivePaperLevel() == 2 && psrAdaptivePaperConfig.isAdaptivePaperByBookId(psrExamContext.getBookId())))
            retExamContent = dealWithPaperQuestionIds(retExamContent, psrExamContext);

        // 默认的psr逻辑 , 更新数据库
        psrExamEnData.updateHistory(retExamContent, psrExamContext, Subject.CHINESE);

        Integer leftCount = psrExamContext.getECount() - retExamContent.getExamList().size();
        if (leftCount > 0) {
//            EkEidListContent ekEidListContentDefault = fillEkEidListContentDefault();
//            retExamContent.setExamListByEkEidListContent(ekEidListContentDefault, leftCount, "std_default", psrExamType + "_id_std_default");
//            retExamContent.setErrorContent("success");

            // 自杀式补题,打log查问题,如果教材下挂载的题量少,通知内容补题
            log.info("ExamChineseNotEnoughEids book:" + psrExamContext.getBookId());
        }

        return logContent(retExamContent, psrExamContext, "success", dtB, "info");
    }

    private PsrExamContent sortResult(PsrExamContent result) {
        PsrExamContent retExamContent = new PsrExamContent();

        if (result == null || !result.isSuccess())
            return retExamContent;

        retExamContent.setErrorContent(result.getErrorContent());
        retExamContent.setEids(result.getEids());
        List<PsrExamItem> redmineExamList = new LinkedList<>();

        for (PsrExamItem item : result.getExamList()) {
            if (item.getAlogv().startsWith("redmine"))
                redmineExamList.add(item);
            else
                retExamContent.getExamList().add(item);
        }
        redmineExamList.forEach(p -> retExamContent.getExamList().add(p));

        return retExamContent;
    }

    public PsrExamContent dealWithPaperQuestionIds(PsrExamContent retExamContent, PsrExamContext psrExamContext) {
        if (retExamContent == null) retExamContent = new PsrExamContent();
        if (psrExamContext == null) return retExamContent;

        // 使用Paper补题
        EkEidListContent ekEidListContent = psrAdaptivePaperQids.dealWithPaperQids(psrExamContext);

        String algov = "std_paper";
        String psrExamType = psrExamContext.getPsrExamType() + "_" + algov;

        retExamContent.setExamListByEkEidListContent(ekEidListContent, psrExamContext.getECount() - retExamContent.getExamList().size(), algov, psrExamContext.getPsrExamType());
        retExamContent.setErrorContent("success");

        return retExamContent;
    }

    public PsrExamContent dealCore(PsrExamContent retExamContent, PsrExamContext psrExamContext) {
        if (retExamContent == null) retExamContent = new PsrExamContent();
        if (psrExamContext == null) return retExamContent;

        // 根据Uid 取出 learning_profile 的 Ek-list, From couchbase
        UserExamContent userExamContent = psrExamEnData.getUserExamContentId(psrExamContext, Subject.CHINESE);
        if (!psrExamContext.isAdaptive()) {
            // psr算法
            if (userExamContent == null) {
                retExamContent.setErrorContent("[NotFoundUserExamContent]");
                return retExamContent;
            } else {
                userExamContent.setUserInfoLevel(0);
            }
        } else {
            // 适配逻辑,补题算法
            if (userExamContent == null) {
                userExamContent = new UserExamContent();
            }
            if (userExamContent.isEkListNull()) userExamContent.setEkList(new ArrayList<>());
            userExamContent.setUType(psrExamContext.getUType());
            userExamContent.setUserId(psrExamContext.getUserId());
            userExamContent.setRegionCode(psrExamContext.getRegionCode());
            userExamContent.setGrade(psrExamContext.getGrade());
            userExamContent.setIrtTheta(-1);
            userExamContent.setUserInfoLevel(2);
            psrExamContext.setUserExamContentId(userExamContent);
        }

        // 个性化推荐 推题 算法
        EkEidListContent ekEidListContent = psrExamCnCore.doCore(psrExamContext);

        String algov = "std2";
        switch (userExamContent.getUserInfoLevel()) {
            case 0:
                algov = "p004";
                break;
            case 1:
                algov = "a002";
                break;
            case 2:
                algov = "std2";
                break;
            default:
                break;
        }

        retExamContent.setExamListByEkEidListContent(ekEidListContent, psrExamContext.getECount() - retExamContent.getExamList().size(), algov, psrExamContext.getPsrExamType());
        retExamContent.setErrorContent("success");

        return retExamContent;
    }

    private PsrExamContent logContent(PsrExamContent retExamContent,
                                      PsrExamContext psrExamContext, String errorMsg,
                                      Date dtB, String logLevel) {
        if (retExamContent == null)
            retExamContent = new PsrExamContent();
        if (StringUtils.isEmpty(errorMsg))
            errorMsg = "success";

        retExamContent.setErrorContent(errorMsg);
        if (!errorMsg.equals("success"))
            retExamContent.getExamList().clear();

        if (psrExamContext != null && psrExamContext.isWriteLog()) {
            if (StringUtils.isEmpty(logLevel))
                logLevel = "info";
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            String strLog = formatReturnLog(retExamContent, psrExamContext, uTAll);
            switch (logLevel) {
                case "info":
                    log.info(strLog);
                    break;
                case "error":
                    log.error(strLog);
                    break;
                case "warn":
                    log.warn(strLog);
                    break;
                default:
                    log.info(strLog);
            }
        }

        return retExamContent;
    }

    private String formatReturnLog(PsrExamContent retExamContent, PsrExamContext psrExamContext, Long totalTime) {
        String strLog = retExamContent.formatList("ExamCn");
        strLog += "[product:" + psrExamContext.getProduct() + " uType:" + psrExamContext.getUType() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " region:" + Integer.valueOf(psrExamContext.getRegionCode()).toString() + " book:" + psrExamContext.getBookId() + " unit:" + psrExamContext.getUnitId();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString();
        strLog += " minP:" + Float.valueOf(psrExamContext.getMinP()).toString() + " maxP:" + Float.valueOf(psrExamContext.getMaxP()).toString();
        strLog += " grade:" + Integer.valueOf(psrExamContext.getGrade()).toString() + "]";
        strLog += "[TotalTime:" + totalTime.toString() + "]";

        return strLog;
    }
}
