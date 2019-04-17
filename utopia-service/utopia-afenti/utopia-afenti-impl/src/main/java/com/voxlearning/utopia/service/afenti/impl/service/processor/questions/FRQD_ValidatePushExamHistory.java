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

package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchRankQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanPushExamHistoryDao;
import com.voxlearning.utopia.service.afenti.impl.persistence.UserAfentiStatsPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class FRQD_ValidatePushExamHistory extends SpringContainerSupport implements IAfentiTask<FetchRankQuestionsContext> {

    @Inject private AfentiLearningPlanPushExamHistoryDao afentiLearningPlanPushExamHistoryDao;
    @Inject private UserAfentiStatsPersistence dao;
    @Inject private QuestionLoaderClient questionLoaderClient;

    @Override
    public void execute(FetchRankQuestionsContext context) {
        List<AfentiLearningPlanPushExamHistory> list = context.getHistories();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        // 新的推题逻辑
        if (context.getIsNewRankBook()) {
            // 题量不够的  重新推题
            if (list.size() < 3) {
                resetForNewRank(context, "新推题接口题量小于3道需要重新推题");
            }
            return;
        }
        if (list.stream().map(AfentiLearningPlanPushExamHistory::getExamId).collect(Collectors.toSet()).size() < list.size()) {
            reset(context, "推题重复需要重新推题");
            return;
        }

        if (list.stream().anyMatch(h -> h.getExamId().matches("(?:(?!^[Q]_).)*"))) {
            reset(context, "关卡的做题记录有旧题需要重新推题");
        }
        // 处理一下语文的内容  有错误题型的， 重新推
        if (context.getSubject() == Subject.CHINESE) {
            List<Integer> blackList = Arrays.asList(6, 16, 17, 11, 12, 13, 14, 15, 18, 19, 20, 21);
            List<String> examIds = context.getHistories().stream().map(AfentiLearningPlanPushExamHistory::getExamId).collect(Collectors.toList());
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(examIds);
            for (Map.Entry<String, NewQuestion> entry : questionMap.entrySet()) {
                NewQuestion question = entry.getValue();
                if (question != null && question.getContent() != null) {
                    List<NewQuestionsSubContents> contentsList = question.getContent().getSubContents();
                    if (CollectionUtils.isNotEmpty(contentsList)) {
                        List<Integer> subContentTypeIds = contentsList
                                .stream()
                                .filter(o -> o.getSubContentTypeId() != null)
                                .map(NewQuestionsSubContents::getSubContentTypeId)
                                .collect(Collectors.toList());

                        if (CollectionUtils.containsAny(subContentTypeIds, blackList)) {
                            reset(context, "语文题型错误，需要重新推题");
                            return;
                        }
                    }
                }
            }
        }
    }

    private void resetForNewRank(FetchRankQuestionsContext ctx, String text) {
        String bookId = ctx.getNewRankBookId();
        String key = StringUtils.join(Arrays.asList(bookId, ctx.getUnitId(), ctx.getRank()), "_");

        afentiLearningPlanPushExamHistoryDao.delete(ctx.getStudent().getId(), bookId, ctx.getUnitId(), ctx.getRank());
        dao.updateStats(ctx.getStudent().getId(), key, null);
        ctx.setHistories(new ArrayList<>());
        logger.warn(text + ctx.getStudent().getId() + "-" + bookId + "-" + ctx.getUnitId() + "-" + ctx.getRank());
    }

    private void reset(FetchRankQuestionsContext ctx, String text) {
        String bookId = AfentiUtils.getBookId(ctx.getBook().book.getId(), ctx.getLearningType());
        String key = StringUtils.join(Arrays.asList(bookId, ctx.getUnitId(), ctx.getRank()), "_");

        afentiLearningPlanPushExamHistoryDao.delete(ctx.getStudent().getId(), bookId, ctx.getUnitId(), ctx.getRank());
        dao.updateStats(ctx.getStudent().getId(), key, null);
        ctx.setHistories(new ArrayList<>());
        logger.warn(text + ctx.getStudent().getId() + "-" + bookId + "-" + ctx.getUnitId() + "-" + ctx.getRank());
    }
}
