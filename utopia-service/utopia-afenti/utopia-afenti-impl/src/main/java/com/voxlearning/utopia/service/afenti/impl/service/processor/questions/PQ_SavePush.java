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

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.athena.api.psr.entity.PsrSectionPak;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.PushQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanPushExamHistoryDao;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.psr.entity.PsrExamItem;
import com.voxlearning.utopia.service.question.api.entity.AfentiPreviewQuestion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/7/18
 */
@Named
public class PQ_SavePush extends SpringContainerSupport implements IAfentiTask<PushQuestionContext> {

    @Inject private AfentiLearningPlanPushExamHistoryDao afentiLearningPlanPushExamHistoryDao;

    @Override
    public void execute(PushQuestionContext context) {
        if (context.getLearningType() == AfentiLearningType.castle) {
            // 城堡根据教材单元类型判断取值
            if (context.getIsNewRankBook() && UtopiaAfentiConstants.getUnitType(context.getUnitId()) != UnitRankType.ULTIMATE) {
                String algo_v = context.getStudent() + context.getUnitId() + context.getRank();
                for (PsrSectionPak pak : context.getNewRankQuestions()) {
                    AfentiLearningPlanPushExamHistory history = new AfentiLearningPlanPushExamHistory();

                    history.setUserId(context.getStudentId());
                    history.setNewBookId(AfentiUtils.getNewBookId(context.getBookId()));
                    history.setNewUnitId(context.getUnitId());
                    history.setSubject(context.getSubject().name());
                    history.setRank(context.getRank());
                    history.setExamId(pak.getQid());
                    history.setKnowledgePoint(pak.getKpId());
                    Map<String, Object> scoreCoefficient = new LinkedHashMap<>();
                    scoreCoefficient.put("algo_v", algo_v.hashCode());
                    scoreCoefficient.put("algo_w", pak.getAlgo_w());
                    history.setScoreCoefficient(JsonUtils.toJson(scoreCoefficient));
                    history.setCreatetime(new Date());
                    history.setUpdatetime(new Date());
                    history.setRightNum(0);
                    history.setErrorNum(0);
                    afentiLearningPlanPushExamHistoryDao.insert(history);
                    context.getHistories().add(history);
                }
            } else {
                String bookId = context.getBookId();
                if (context.getIsNewRankBook()) {
                    bookId = AfentiUtils.getNewBookId(bookId);
                }
                for (PsrExamItem item : context.getPsr().getExamList()) {
                    AfentiLearningPlanPushExamHistory history = new AfentiLearningPlanPushExamHistory();

                    history.setUserId(context.getStudentId());
                    history.setNewBookId(bookId);
                    history.setNewUnitId(context.getUnitId());
                    history.setSubject(context.getSubject().name());
                    history.setRank(context.getRank());
                    history.setKnowledgePoint(item.getEk());
                    history.setExamId(item.getEid());
                    history.setPattern(item.getEt());
                    history.setCreatetime(new Date());
                    history.setUpdatetime(new Date());
                    Map<String, Object> scoreCoefficient = new LinkedHashMap<>();
                    scoreCoefficient.put("weight", item.getWeight());
                    scoreCoefficient.put("alogv", item.getAlogv());
                    history.setScoreCoefficient(JsonUtils.toJson(scoreCoefficient));
                    history.setRightNum(0);
                    history.setErrorNum(0);

                    afentiLearningPlanPushExamHistoryDao.insert(history);
                    context.getHistories().add(history);
                }
            }

        } else if (context.getLearningType() == AfentiLearningType.preparation) {
            for (AfentiPreviewQuestion question : context.getPreviewQuestions()) {
                AfentiLearningPlanPushExamHistory history = new AfentiLearningPlanPushExamHistory();

                history.setUserId(context.getStudentId());
                history.setNewBookId(AfentiUtils.getBookId(context.getBookId(), context.getLearningType()));
                history.setNewUnitId(context.getUnitId());
                history.setSubject(context.getSubject().name());
                history.setRank(context.getRank());
                history.setKnowledgePoint(question.getKnowledgePointId());
                history.setExamId(question.getQuestionId());
                history.setPattern(question.getContentTypeId() == null ? "" : question.getContentTypeId().toString());
                history.setCreatetime(new Date());
                history.setUpdatetime(new Date());
                history.setRightNum(0);
                history.setErrorNum(0);
                afentiLearningPlanPushExamHistoryDao.insert(history);
                context.getHistories().add(history);
            }
        }

    }
}
