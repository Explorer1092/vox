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

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.EidItem;
import com.voxlearning.utopia.service.psr.entity.EkEidListContent;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/*
 * lichaoli, 2016-10-26
 */

@Slf4j
@Named
public class PsrAdaptivePreUnitQids implements Serializable {
    @Inject private PsrExamPaperData psrExamPaperData;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;


    public EkEidListContent dealWithPreUnitQids(PsrExamContext psrExamContext) {
        if (psrExamContext == null)
            return new EkEidListContent();

        EkEidListContent ekEidListContent = psrExamContext.getEkEidListContent();
        List<KeyValuePair<String, EidItem>> recommendedList = psrExamContext.getRecommendedList();  // 之前单元已经被推过的题目
        if (StringUtils.isBlank(psrExamContext.getBookId()) || ekEidListContent.getEids().size() >= psrExamContext.getECount() || CollectionUtils.isEmpty(recommendedList))
            return ekEidListContent;

        Integer reqEidCount = psrExamContext.getECount();
        Integer count = ekEidListContent.getEids().size();
        for (KeyValuePair<String, EidItem> kv : recommendedList) {
            String ek = kv.getKey();
            EidItem item = kv.getValue();
            String qid = item.getEid();

            NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(qid);
            // 不合法的跳过
            if (! isValid(psrExamContext, qid, question))
                continue;

            String oldEk = getOldEkByNewEk(psrExamContext, ek);
            if (StringUtils.isBlank(oldEk))
                continue;

            ekEidListContent.addItemByEk(oldEk, item);
            psrExamContext.getRecommendEids().add(qid);

            // 已达到需要的题目数量
            if (++count >= reqEidCount)
                break;
        }

        return ekEidListContent;
    }

    // true:推荐,false:不推荐
    public boolean isValid(PsrExamContext psrExamContext, String qid, NewQuestion question) {
        if (StringUtils.isBlank(qid) || psrExamContext == null)
            return true;
        if (question == null)
            return false;

        // 本次推荐是否已经存在此题
        if (psrExamContext.getRecommendEids().contains(qid))
            return false;

        // 检查题目是否符合推荐的其他条件
        if (!psrExamEnFilter.isPsrFromOnlineQuestionTable(psrExamContext, qid, psrExamContext.getSubject(), question)) {
            log.error("FindAnBadEid:" + qid + ",badEk:" + "from_def_preunit");
            return false;
        }

        return true;
    }

    public String getOldEkByNewEk(PsrExamContext psrExamContext, String newEk) {
        if (StringUtils.isBlank(newEk) || psrExamContext == null)
            return null;

        if (psrExamContext.getSubject().equals(Subject.CHINESE))
            return "chinese";
        else if (psrExamContext.getSubject().equals(Subject.MATH))
            return newEk;
        else {
            return newEk;
        }
    }
}

