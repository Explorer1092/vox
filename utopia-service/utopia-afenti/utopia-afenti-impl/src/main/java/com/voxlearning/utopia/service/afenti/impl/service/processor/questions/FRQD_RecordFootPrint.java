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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchRankQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserFootprint;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanUserFootprintPersistence;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/8/10
 */
@Named
public class FRQD_RecordFootPrint extends SpringContainerSupport implements IAfentiTask<FetchRankQuestionsContext> {

    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;

    @Inject private AfentiLearningPlanUserFootprintPersistence p;
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchRankQuestionsContext context) {
        String bookId = context.getBook().book.getId();
        if (context.getIsNewRankBook()) {
            bookId = AfentiUtils.getNewBookId(bookId);
        }
        AfentiLearningPlanUserFootprint footprint = p.findByUserIdAndSubject(context.getStudent().getId(), context.getSubject());

        // 如果当前足迹就在这里，啥也不动了
        if (footprint != null && StringUtils.equals(bookId, footprint.getNewBookId())
                && StringUtils.equals(context.getUnitId(), footprint.getNewUnitId())
                && Objects.equals(context.getRank(), footprint.getRank())) return;

        if (null == footprint) {
            footprint = new AfentiLearningPlanUserFootprint();
            footprint.setUserId(context.getStudent().getId());
            footprint.setNewBookId(bookId);
            footprint.setNewUnitId(context.getUnitId());
            footprint.setRank(context.getRank());
            footprint.setSubject(context.getSubject());
            try {
                p.insert(footprint);
            } catch (Exception ignored) {
            }
        } else {
            footprint.setNewBookId(bookId);
            footprint.setNewUnitId(context.getUnitId());
            footprint.setRank(context.getRank());
            p.update(footprint);
        }

        // 向jp上报使用进度
        try {
            jp_study_progress(context, bookId);
        } catch (Exception ex) {
            logger.error("Afenti jp study progress error", ex);
        }
    }

    private void jp_study_progress(FetchRankQuestionsContext context, String bookId) {
        SelfStudyType sst;
        switch (context.getSubject()) {
            case ENGLISH: {
                sst = SelfStudyType.AFENTI_ENGLISH;
                break;
            }
            case MATH: {
                sst = SelfStudyType.AFENTI_MATH;
                break;
            }
            case CHINESE: {
                sst = SelfStudyType.AFENTI_CHINESE;
                break;
            }
            default:
                sst = null;
        }
        if (sst == null) return;

        String text;
        // 终极单元
        UnitRankType rankType = UtopiaAfentiConstants.getUnitType(context.getUnitId());
        if (rankType == UnitRankType.ULTIMATE) {
            text = "总复习第" + context.getRank() + "关";
        } else if (rankType == UnitRankType.MIDTERM) {
            text = "期中复习第" + context.getRank() + "关";
        } else if (rankType == UnitRankType.TERMINAL) {
            text = "期末复习第" + context.getRank() + "关";
        } else {
            // 获取unitRank
            List<AfentiLearningPlanUnitRankManager> ranks = afentiLoader
                    .loadAfentiLearningPlanUnitRankManagerByNewBookId(bookId)
                    .stream()
                    .filter(rm -> StringUtils.equals(rm.getNewUnitId(), context.getUnitId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ranks)) return;

            String pattern = "第{0}单元第{1}关";
            text = MessageFormat.format(pattern, ranks.get(0).fetchUnitRank(), context.getRank());
        }
        mySelfStudyService.updateSelfStudyProgress(context.getStudent().getId(), sst, text);
    }
}
