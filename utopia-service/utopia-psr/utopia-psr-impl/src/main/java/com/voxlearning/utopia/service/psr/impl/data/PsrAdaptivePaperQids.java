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
import com.voxlearning.utopia.service.question.api.entity.NewQuestionKnowledgePoint;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * lichaoli, 2016-10-26
 */

@Slf4j
@Named
public class PsrAdaptivePaperQids implements Serializable {
    @Inject private PsrExamPaperData psrExamPaperData;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;


    public EkEidListContent dealWithPaperQids(PsrExamContext psrExamContext) {
        if (psrExamContext == null)
            return new EkEidListContent();

        EkEidListContent ekEidListContent = psrExamContext.getEkEidListContent();
        if (StringUtils.isBlank(psrExamContext.getBookId()) || ekEidListContent.getEids().size() >= psrExamContext.getECount())
            return ekEidListContent;

        Map<String, Boolean> paperQuestionIds = psrExamPaperData.getPaperQidsByBookId(psrExamContext);
        if (MapUtils.isEmpty(paperQuestionIds))
            return ekEidListContent;

        Integer reqEidCount = psrExamContext.getECount();
        Integer count = ekEidListContent.getEids().size();
        for (Map.Entry<String, Boolean> entry : paperQuestionIds.entrySet()) {
            // 不能使用的题目:跳过
            if (!entry.getValue())
                continue;

            String qid = entry.getKey();
            NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(qid);
            // 不合法的跳过
            if (! isValid(psrExamContext, qid, question))
                continue;

            KeyValuePair<String, String> keyValuePair = getEkAndEtByQuestion(psrExamContext, qid, question);
            if (keyValuePair == null) {
                psrExamPaperData.rmInvalidQuestion(psrExamContext.getBookId(), qid);
                continue;
            }

            EidItem item = new EidItem();
            item.setEid(qid);
            item.setEt(keyValuePair.getValue());
            ekEidListContent.addItemByEk(keyValuePair.getKey(), item);
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

        // 试卷下面挂载的题目不存在超纲问题
        // 历史上做过的题,或者psr最近几天推荐过的题 不推
        if (psrExamEnFilter.isDoItRightEid(psrExamContext, qid, psrExamContext.getSubject()))
            return false;

        // 检查题目是否符合推荐的其他条件
        if (!psrExamEnFilter.isPsrFromOnlineQuestionTable(psrExamContext, qid, psrExamContext.getSubject(), question)) {
            log.error("FindAnBadEid:" + qid + ",badEk:" + "from_paper");
            psrExamPaperData.rmInvalidQuestion(psrExamContext.getBookId(), qid);
            return false;
        }

        return true;
    }

    public KeyValuePair<String, String> getEkAndEtByQuestion(PsrExamContext psrExamContext, String qid, NewQuestion question) {
        if (StringUtils.isBlank(qid) || psrExamContext == null || question == null)
            return null;

        String ek = "";
        List<String> oldEks = new ArrayList<>();
        if (psrExamContext.getSubject().equals(Subject.CHINESE))
            ek = "chinese";
        else {
            List<NewQuestionKnowledgePoint> knowledgePoints = question.getKnowledgePointsNew();
            if (CollectionUtils.isNotEmpty(knowledgePoints)) {
                knowledgePoints.stream().filter(p -> p.getMain().equals(1)).forEach(p -> oldEks.add(p.getId()));
                if (CollectionUtils.isNotEmpty(oldEks))
                    ek = oldEks.get(0);
            }
        }

        if (StringUtils.isBlank(ek))
                return null;

        String et = question.getContentTypeId().toString();
        KeyValuePair<String, String> keyValuePair = new KeyValuePair<>();
        keyValuePair.setKey(ek);
        keyValuePair.setValue(et);

        return keyValuePair;
    }
}

