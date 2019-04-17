/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchTermQuizQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiQuizResultDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_STUDENT;

/**
 * @author Ruib
 * @since 2016/10/19
 */
@Named
public class FTQQ_InitQuizResultIfNecessary extends SpringContainerSupport implements IAfentiTask<FetchTermQuizQuestionContext> {

    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private AfentiQuizResultDao afentiQuizResultDao;

    @Override
    public void execute(FetchTermQuizQuestionContext context) {
        if (CollectionUtils.isNotEmpty(context.getQrs())) return;

        ClazzLevel grade = context.getStudent().getClazzLevel();
        Subject subject = context.getSubject();
        String key = "AFENTI_" + subject.name() + "_" + grade.getLevel() + "_TQ";

        String value = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_STUDENT.name(), key);
        Map<String, Object> qkpm = JsonUtils.fromJson(value);

        List<AfentiQuizResult> results = new ArrayList<>();
        Date current = new Date();
        for (Map.Entry<String, Object> entry : qkpm.entrySet()) {
            AfentiQuizResult result = new AfentiQuizResult();
            result.setCreateTime(current);
            result.setUpdateTime(current);
            result.setUserId(context.getStudent().getId());
            result.setNewBookId(context.getBookId());
            result.setNewUnitId(UtopiaAfentiConstants.CURRENT_QUIZ);
            result.setKnowledgePoint(SafeConverter.toString(entry.getValue()));
            result.setExamId(entry.getKey());
            result.setRightNum(0);
            result.setErrorNum(0);
            result.setSubject(context.getSubject());
            results.add(result);
        }
        afentiQuizResultDao.inserts(results);
        context.getQrs().addAll(results);
    }
}
