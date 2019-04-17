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

package com.voxlearning.utopia.service.psr.impl.data;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentences;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrAboveLevelBookEidsDao;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import com.voxlearning.utopia.service.psr.impl.util.PsrBooksPointsRef;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Named
public class PsrExamEnData implements Serializable {
    @Inject private PsrConfig psrConfig;
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrBooksPointsRef psrBooksPointsRef;
    @Inject private PsrBooksSentences psrBooksSentences;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;
    @Inject private PsrAboveLevelBookEidsDao psrAboveLevelBookEidsDao;
    @Inject private QuestionLoaderClient questionLoaderClient;

    public PsrBookPersistenceNew getPsrBookPersistence(PsrExamContext psrExamContext) {
        if (psrExamContext == null)
            return null;

        PsrBookPersistenceNew psrBookPersistenceNew = psrBooksSentencesNew.getBookPersistenceByBookId(psrExamContext.getBookId());
        psrExamContext.setPsrBookPersistenceNew(psrBookPersistenceNew);

        return psrBookPersistenceNew;
    }

    // 根据Uid 取出 learning_profile 的 Ek-list, From couchbase
    public UserExamContent getUserExamContentId(PsrExamContext psrExamContext, Subject subject) {
        if (psrExamContext == null || subject == null)
            return null;

        UserExamContent userExamContentId = psrExamContext.getUserExamContentId();
        if (userExamContentId == null) {
            userExamContentId = ekCouchbaseDao.getUserExamContentFromCouchbase(psrExamContext.getUserId(), psrExamContext.getGrade(), subject);
            psrExamContext.setUserExamContentId(userExamContentId);
        }

        return userExamContentId;
    }

    // 取出用户的uc
    public UserExamUcContent getUserExamUcContentId(PsrExamContext psrExamContext, Subject subject) {
        if (psrExamContext == null || subject == null)
            return null;

        UserExamUcContent userExamUcContentId = psrExamContext.getUserExamUcContentId();
        if (userExamUcContentId == null) {
            if (subject.equals(Subject.ENGLISH))
                userExamUcContentId = ekCouchbaseDao.getUserExamUcContentFromCouchbase(psrExamContext.getUserId());
            else
                userExamUcContentId = ekCouchbaseDao.getUserExamUcContentFromCouchbase(psrExamContext.getUserId(), subject);
            psrExamContext.setUserExamUcContentId(userExamUcContentId);
        }

        return userExamUcContentId;
    }

    // 获取 该用户 历史测验数据
    public PsrUserHistoryEid getPsrUserHistoryEid(PsrExamContext psrExamContext, Subject subject) {
        if (psrExamContext == null || subject == null)
            return null;

        PsrUserHistoryEid psrUserHistoryEid = psrExamContext.getPsrUserHistoryEid();
        if (psrUserHistoryEid == null)
            psrUserHistoryEid = new PsrUserHistoryEid();

        if (!psrUserHistoryEid.isEidMasterInfoMapInit()) {
            psrUserHistoryEid.setPsrUserHistoryEid(ekCouchbaseDao.getPsrUserHistoryEid(psrExamContext.getUserId(), subject));
            psrUserHistoryEid.setEidMasterInfoMapInit(true);
        }

        if (!psrUserHistoryEid.isEidPsrMapInit()) {
            psrUserHistoryEid.setPsrUserHistoryEid(ekCouchbaseDao.getPsrUserHistoryPsrEid(psrExamContext.getUserId(), subject));
            psrUserHistoryEid.setEidPsrMapInit(true);
        }

        psrExamContext.setPsrUserHistoryEid(psrUserHistoryEid);

        return psrUserHistoryEid;
    }

    public void updateHistory(PsrExamContent retExamContent, PsrExamContext psrExamContext, Subject subject) {
        //if (psrExamContext == null || !psrExamContext.isWriteLog() || psrExamContext.getProduct().equals("TEST"))
        if (psrExamContext == null || !psrExamContext.isWriteLog())
            return;

        PsrUserHistoryEid psrUserHistoryEid = getPsrUserHistoryEid(psrExamContext, subject);
        if (psrUserHistoryEid == null)
            psrUserHistoryEid = new PsrUserHistoryEid();
        if (subject.equals(Subject.ENGLISH)) {
            // 入库 推题数据
            if (retExamContent != null && retExamContent.getExamList() != null && retExamContent.getExamList().size() > 0) {
                String strValue = psrUserHistoryEid.formatEidPsrMapToString(retExamContent);
                if (!StringUtils.isEmpty(strValue)) {
                    ekCouchbaseDao.setCouchbaseData("historypsr_" + psrExamContext.getUserId().toString(), strValue);
                }
            }
        } else {
            // 入库 推题数据
            String strSubject = subject.equals(Subject.MATH) ? subject.name().toLowerCase() : subject.name();
            if (retExamContent != null && retExamContent.getExamList() != null && retExamContent.getExamList().size() > 0) {
                String strValue = psrUserHistoryEid.formatEidPsrMapToString(retExamContent);
                if (!StringUtils.isEmpty(strValue)) {
                    ekCouchbaseDao.setCouchbaseData("historypsr_" + psrExamContext.getUserId().toString() + "_" + strSubject, strValue);
                }
            }
        }
    }

    public boolean isModuleId(PsrExamContext psrExamContext) {
        if (psrExamContext == null)
            return false;

        PsrBookPersistenceNew psrBookPersistenceNew = getPsrBookPersistence(psrExamContext);
        if (psrBookPersistenceNew == null)
            return false;

        psrExamContext.setModuleId(psrBookPersistenceNew.isModuleId(psrExamContext.getUnitId()));

        return psrExamContext.isModuleId();
    }

    public void initAdaptiveUserInfo(UserExamContent userExamContent, PsrExamContext psrExamContext) {
        if (userExamContent == null || psrExamContext == null)
            return;

        Map<String, List<String>> unitsKnowledgePointsMap =  psrBooksSentencesNew.getUnitsSentenceByBookId(psrExamContext.getBookId(), psrExamContext.getSubject(), psrExamContext.getUnitId());
        if (unitsKnowledgePointsMap == null || unitsKnowledgePointsMap.size() <= 0)
            return;

        userExamContent.setUType(psrExamContext.getUType());
        userExamContent.setUserId(psrExamContext.getUserId());
        userExamContent.setRegionCode(psrExamContext.getRegionCode());

        // 如果没有l_p 则获取 Uc 值，在把Uc当做用户的irtTheta，并且把课本的所有知识点取出Kc 计算出weight 并把weight当做master 进行后续计算
        // 如果没有l_p 也没有Uc值， 则吧book的首单元作为考察单元进行补题，并把首单元的知识点 取出Kc 并把Kc当做master，进行后续计算
        // 后续计算 一定要对 userExamContent 做适配

        List<String> eks = new ArrayList<>();
        UserExamUcContent userExamUcContent = getUserExamUcContentId(psrExamContext, psrExamContext.getSubject());
        if (userExamUcContent == null) {
            // 无 Uc，则默认book的第一单元的知识点为考察对象,并取出该单元的eks
            // 1. 新教材结构传进来的unitid是groupid需要按查出对应的unitids再获取eks.2. 非新教材用原来的unitid获取eks
            String firstUnitId = psrExamContext.getUnitId();
            if (StringUtils.equals(psrExamContext.getUnitId(), "-1"))
                firstUnitId = PsrTools.getFirstUnitIdFromPointsMap(unitsKnowledgePointsMap);

            eks = unitsKnowledgePointsMap.get(firstUnitId);
            userExamContent.setIrtTheta(-1);
            userExamContent.setUserInfoLevel(2);
        } else {
            // 使用Uc ,作为能力值, 默认book所有单元的知识点为考察对象
//            eks = PsrTools.getEksIdFromPointRefs(unitKnowledgePointRefs);
            for (Map.Entry<String,List<String> > entry : unitsKnowledgePointsMap.entrySet()){
                eks.addAll(entry.getValue());
            }
            userExamContent.setIrtTheta(-1);
            userExamContent.setUc(userExamUcContent.getUc());
            userExamContent.setUserInfoLevel(1);
        }
        if (eks != null) {
            for (String ek : eks) {
                ExamKcContent examKcContent = ekCouchbaseDao.getExamKcContentFromCouchbase(ek);
                if (examKcContent == null)
                    continue;
                UserEkContent userEkContent = new UserEkContent();
                userEkContent.setEk(ek);
                userEkContent.setMaster(examKcContent.getAccuracyRate());
                userEkContent.setCount((short) 0);
                userEkContent.setLevel((short) 1);
                userExamContent.getEkList().add(userEkContent);
            }
        }

        psrExamContext.setUserExamContentId(userExamContent);
    }

}

