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

package com.voxlearning.utopia.service.psr.impl.competition;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnFilter;
import com.voxlearning.utopia.service.psr.impl.exammath.PsrExamMathCore;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionAnswer;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
/*
 * lcl
 * 2017.2.27
 * 竞赛产品使用
 * 仅用于小学英语、小学数学
 */

@Slf4j
@Named
@Data
public class CompetitionQids implements Serializable {
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrConfig psrConfig;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private PsrExamEnData psrExamEnData;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject private PsrExamMathCore psrExamMathCore;

    public Set<String> deal(Long userId, Subject subject, Integer grade, Integer term, int reqCount, String type) {
        PsrExamContext psrExamContext = new PsrExamContext(psrConfig,
                "competition", type, userId, 0, "0", "0", reqCount, 0.8f, 0.95f);

        psrExamContext.setSubject(subject);
        psrExamContext.setGrade(grade);

        return dealCore(psrExamContext, term);
    }

    public Set<String> dealCore(PsrExamContext psrExamContext, Integer term) {
        Date dtB = new Date();
        Set<String> retSet = new HashSet<>();
        if (psrExamContext == null || (term!=1 && term!=2))
            return logContent(psrExamContext, retSet, "PsrSelfStudySimilar para err", dtB, "info", term);

//        if (Subject.ENGLISH.equals(psrExamContext.getSubject()))
//            retSet = englishQids(psrExamContext);
//        else if(Subject.MATH.equals(psrExamContext.getSubject()))
//            retSet = mathQids(psrExamContext);

        retSet = competitionQids(psrExamContext, term);

        return logContent(psrExamContext, retSet, "success", dtB, "info", term);
    }

    private Set<String> competitionQids(PsrExamContext psrExamContext, Integer termId) {
        Set<String> retSet = new HashSet<>();
        if (psrExamContext == null)
            return retSet;

        // 1. 查找BookId列表
        List<NewBookProfile> newBookProfileList = getTwoTermNewBookProfileList(psrExamContext, termId);

        // 2. 从bookId列表随机2个课本,bookId1
        List<String> inValidBooks = new ArrayList<>();
        for (int i = 0; i < newBookProfileList.size(); i++) {
            if (retSet.size() >= psrExamContext.getECount())
                break;

            NewBookProfile newBookProfile = getRandomBookProfile(psrExamContext, newBookProfileList);
            if (newBookProfile == null || inValidBooks.contains(newBookProfile.getId()))
                continue;

            inValidBooks.add(newBookProfile.getId());
            //psrExamEnFilter.initBookEids(psrExamContext, psrExamContext.getSubject());
            psrExamContext.setBookId(newBookProfile.getId());
            Set<String> ret = getCompetitionQidsByBook(psrExamContext, newBookProfile);
            if (CollectionUtils.isNotEmpty(ret)) {
                for (String qid : ret) {
                    if (retSet.size() >= psrExamContext.getECount())
                        break;
                    retSet.add(qid);
                }
            }
        }

        return retSet;
    }

    private Set<String> getCompetitionQidsByBook(PsrExamContext psrExamContext, NewBookProfile newBookProfile) {
        Set<String> retSet = new HashSet<>();
        if (psrExamContext == null)
            return retSet;

        // 3. 取出bookId1的所有知识点集合Kps
        List<PsrUnitPersistenceNew> units = getValidUnitList(psrExamContext, newBookProfile);
        if (CollectionUtils.isEmpty(units))
            return retSet;

        List<String> kps = new ArrayList<>();
        units.stream().map(PsrUnitPersistenceNew::getSentences).forEach(p -> kps.addAll(p.keySet()));
        if (CollectionUtils.isEmpty(kps))
            return retSet;

        Integer maxKpNum = psrExamContext.getECount() <= 6 ? psrExamContext.getECount() : psrExamContext.getECount() / 2;
        if (maxKpNum <= 0 || maxKpNum > kps.size())
            maxKpNum = kps.size();
        Integer qidNumPerKp = psrExamContext.getECount() / maxKpNum;
        qidNumPerKp = (qidNumPerKp <= 0) ? 1 : qidNumPerKp;

        // 4. 随机Kps出KP
        Random random = psrExamContext.getRandom();
        for (int i = 0; i < kps.size(); i++) {
            Integer index = random.nextInt(10000) % kps.size();
            String kp = kps.get(index);
            List<EidItem> ret = getQidsByKp(psrExamContext, newBookProfile, kp);
            if (CollectionUtils.isEmpty(ret))
                continue;

            Integer curKpQidNum = 0;
            for (EidItem item : ret) {
                if (retSet.size() >= psrExamContext.getECount())
                    break;
                // 题型过滤
                NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(item.getEid());
                if (!isPsrFromOnlineQuestionTable(psrExamContext, item.getEid(), psrExamContext.getSubject(), question)) {
                    log.error("FindAnBadEid:" + item.getEid() + ",badEk:" + kp);
                    continue;
                }
                retSet.add(item.getEid());
                if (++curKpQidNum >= qidNumPerKp)
                    break;
            }

            if (retSet.size() >= psrExamContext.getECount())
                break;
        }

        return retSet;
    }

    // 5. 随机kp1下若干题目,并返回, fixme 限制题型,难度系数, newBookProfile.getLatestVersion()
    // 1>小英、小数旧版 - 取题.2>小数新版取题
    private List<EidItem> getQidsByKp(PsrExamContext psrExamContext, NewBookProfile newBookProfile, String kp) {
        if (psrExamContext == null || newBookProfile == null || StringUtils.isBlank(kp))
            return Collections.emptyList();

        // 数学新教材 从huihui接口获取对应的题目信息
        if (psrExamContext.getSubject().equals(Subject.MATH) && newBookProfile.getLatestVersion() == 1) {
            EkEidListContent ekEidListContentEx = psrExamMathCore.getMathNewKpQid(psrExamContext, kp);
            if (ekEidListContentEx == null || CollectionUtils.isEmpty(ekEidListContentEx.getEkList()))
                return Collections.emptyList();
            EkToEidContent ekToEidContent = ekEidListContentEx.getEkList().get(0);
            if (ekToEidContent == null || CollectionUtils.isEmpty(ekToEidContent.getEidList()))
                return Collections.emptyList();

            return ekToEidContent.getEidList();
        } else {
            EkToEidContent ekToEidContent = ekCouchbaseDao.getEkToEidContentFromCouchbase(kp, psrExamContext.getGrade());
            if (ekToEidContent == null || CollectionUtils.isEmpty(ekToEidContent.getEidList()))
                return Collections.emptyList();

            return ekToEidContent.getEidList();
        }
    }

    private List<PsrUnitPersistenceNew> getValidUnitList(PsrExamContext psrExamContext, NewBookProfile newBookProfile) {
        List<PsrUnitPersistenceNew> retList = new ArrayList<>();
        if (psrExamContext == null || newBookProfile == null)
            return retList;

        PsrBookPersistenceNew psrBookPersistenceNew = psrExamEnData.getPsrBookPersistence(psrExamContext);
        if (psrBookPersistenceNew == null)
            return retList;

        List<Integer> ranks = psrBookPersistenceNew.getRanks();
        Integer currentUnitIndex = -1;
        try {
            currentUnitIndex = getCurrentUnitIndex(newBookProfile);
        } catch (ParseException e) {
            currentUnitIndex = -1;
        }
        currentUnitIndex = (currentUnitIndex == -1) ? ranks.size() : currentUnitIndex;
        for (int i = 0; i < currentUnitIndex && i < ranks.size(); i++) {
            PsrUnitPersistenceNew psrUnitPersistenceNew = psrBookPersistenceNew.getUnitPersistenceByUnitRankId(ranks.get(i));
            if (psrUnitPersistenceNew == null)
                continue;

            retList.add(psrUnitPersistenceNew);
        }

        return retList;
    }

    private Integer getCurrentUnitIndex(NewBookProfile newBookProfile) throws ParseException {
        if (newBookProfile == null)
            return -1;

        Date date = new Date();
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;
        if (newBookProfile.getTermType() != Term.ofMonth(month).getKey())
            return -1;

        String ftDate = Integer.valueOf(year).toString();
        if (Term.ofMonth(month).getKey() == 1) {
            ftDate += "-09-01 00:00:00";
        } else if (Term.ofMonth(month).getKey() == 2) {
            ftDate += "-03-06 00:00:00";
        } else
            return -1;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date termStartDate = sdf.parse(ftDate);
        Long msTime = date.getTime() - termStartDate.getTime();
        if (msTime < 0)
            return 1;
        int days =(int) (msTime / (1000*86400)); // 距离开学多少天

        return  (days / 14) + 1;  // 两周14天学习一个单元
    }

    private NewBookProfile getRandomBookProfile(PsrExamContext psrExamContext, List<NewBookProfile> bookProfiles) {
        if (CollectionUtils.isEmpty(bookProfiles))
            return null;
        if (bookProfiles.size() == 1)
            return bookProfiles.get(0);

        Random random = (psrExamContext == null ? new Random() : psrExamContext.getRandom());
        Integer index = random.nextInt(10000) % bookProfiles.size();
        return bookProfiles.get(index);
    }

    private List<NewBookProfile> getTwoTermNewBookProfileList(PsrExamContext psrExamContext, Integer termId) {
        List<NewBookProfile> retList = new ArrayList<>();
        if (psrExamContext == null || termId < 0 || termId > 2) {
            return retList;
        }

        String invalidBookType = "TEXTBOOK";

        // 取当前学期所有教材
        Term currentTerm = Term.of(termId);
        ClazzLevel currentClazzLevel = ClazzLevel.parse(psrExamContext.getGrade());
        List<NewBookProfile> currentTermBookList = newContentLoaderClient.loadBooksByClassLevelAndTermAndSeriesIdAndBookType(psrExamContext.getSubject(), currentClazzLevel, currentTerm, null, invalidBookType);
        if (CollectionUtils.isNotEmpty(currentTermBookList))
            retList.addAll(currentTermBookList);

        // 取前一学期所有教材
        Integer lastGrade = psrExamContext.getGrade();
        Integer lastTermId = termId;
        if (termId == 2 || psrExamContext.getGrade() > 1) {
            if (termId == 2)
                lastTermId = 1;
            else{
                lastTermId = 2;
                lastGrade = psrExamContext.getGrade() - 1;
            }
        }
        Term lastTerm = Term.of(lastTermId);
        ClazzLevel lastClazzLevel = ClazzLevel.parse(psrExamContext.getGrade());
        List<NewBookProfile> lastTermBookList = null; // newContentLoaderClient.loadBooksByClassLevelAndTermAndSeriesIdAndBookType(psrExamContext.getSubject(), lastClazzLevel, lastTerm, null, invalidBookType);
        if (CollectionUtils.isNotEmpty(lastTermBookList))
            retList.addAll(lastTermBookList);

        if (CollectionUtils.isEmpty(retList)) {
            // 根据Subject默认基本教材
            // call function init newBookProfileList
            //retList.addAll(currentTermBookList);
        }

        return retList;
    }

    // 过滤规则仅适用于竞赛产品
    // 小英:基础题型:1单选小题\5判断小题
    // 小数:基础题型:1单选小题\5判断小题\4填空小题[答案为1个空 且 答案为纯数字]
    public boolean isPsrFromOnlineQuestionTable(PsrExamContext psrExamContext, String eid, Subject subject, NewQuestion question) {
        if (StringUtils.isEmpty(eid) || (psrExamContext != null && !psrExamContext.isFilterFromOnlineQuestion()))
            return true;

        if (question == null)
            return false;

        // deleted_at
        if (question.getDeletedAt() != null)
            return false;

        // not_fit_mobile
        if (!question.getNotFitMobile().equals(0))
            return false;

        // 熔断的题不推荐
        if (question.isBroken())
            return false;

        // 不符合竞赛的条件不推荐
        if (MapUtils.isEmpty(question.getOthers()))
            return false;
        if (!question.getOthers().containsKey("afenti_valid"))
            return false;
        if (!(boolean)question.getOthers().get("afenti_valid"))
            return false;

        if (question.getContent() == null)
            return false;

        // subContentType
        List<NewQuestionsSubContents> subContentses = question.getContent().getSubContents();
        if (subContentses == null)
            return false;

        boolean isPsr = true;
        for (NewQuestionsSubContents sbContent : subContentses) {
            int typeId = sbContent.getSubContentTypeId();
            List<NewQuestionAnswer> answers = sbContent.getAnswers();
            if (Subject.MATH.equals(subject)) {
                if (typeId!=1 && typeId!=5 && typeId!=4)
                    return false;
                if (typeId==4) {
                    if (subContentses.size()!=1 || answers==null || answers.size()!=1)
                        return false;
//                    if (answers.get(0) == null || !StringUtils.isNumeric(answers.get(0).getAnswer()))
//                        return false;
                }
            }
            if (Subject.ENGLISH.equals(subject)) {
                if (typeId!=1 && typeId!=5)
                    return false;
            }
        }

        return isPsr;
    }

    private Set<String> logContent(PsrExamContext psrExamContext, Set<String> retSet, String errorMsg,
                                              Date dtB, String logLevel, Integer term) {
        if (StringUtils.isEmpty(errorMsg))
            errorMsg = "success";

        if (psrExamContext != null && psrExamContext.isWriteLog()) {
            if (StringUtils.isEmpty(logLevel))
                logLevel = "info";
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            String strLog = formatReturnLog(psrExamContext, errorMsg, retSet, uTAll, term);
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

        return retSet;
    }

    private String formatReturnLog(PsrExamContext psrExamContext, String errorMsg, Set<String> retSet, Long spendTime, Integer term) {
        if (psrExamContext == null || CollectionUtils.isEmpty(retSet) || spendTime == null) {
            return "psrExamContext or retExamContent or spendTime is null.";
        }
        String strLog = "[CompetitionQids:return code:"+ errorMsg +"][retcount:" + retSet.size() + "]";
        strLog += retSet.toString();
        strLog += "[product:" + psrExamContext.getProduct() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " subject:" + psrExamContext.getSubject().toString();
        strLog += " grade:" + Integer.valueOf(psrExamContext.getGrade()).toString();
        strLog += " term:" + term.toString();
        strLog += " type:" + psrExamContext.getUType();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString()  + "]";
        strLog += "[TotalTime:" + spendTime.toString() + "]";

        return strLog;
    }

}

