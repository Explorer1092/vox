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

package com.voxlearning.utopia.service.afenti.impl.service.processor.units;

import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.context.FetchBookUnitsContext;
import com.voxlearning.utopia.service.afenti.api.data.BookUnit;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.CHINESE;

/**
 * @author Ruib
 * @since 2016/7/14
 */
@Named
public class FBU_LoadCommonUnits extends SpringContainerSupport implements IAfentiTask<FetchBookUnitsContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchBookUnitsContext context) {
        NewBookProfile book = context.getBook().book;

        Map<String, Integer> unit_asc_map = context.getUnit_asc_map();
        Map<String, Integer> unit_asrc_map = context.getUnit_asrc_map();
        Map<String, Integer> unit_footprint_map = context.getUnit_footprint_map();
        String bookId = context.getBook().book.getId();
        if (context.getIsNewRankBook()) {
            bookId = context.getNewRankBookId();
        }
        // 获取这本书所有关卡，按照单元分组
        Map<String, List<AfentiLearningPlanUnitRankManager>> map = afentiLoader
                .loadAfentiLearningPlanUnitRankManagerByNewBookId(bookId)
                .stream()
                .filter(r -> context.getLearningType() == r.getType())
                .filter(r -> StringUtils.isNotBlank(r.getNewUnitId()))
                .collect(Collectors.groupingBy(AfentiLearningPlanUnitRankManager::getNewUnitId));

        for (String unitId : map.keySet()) {
            List<AfentiLearningPlanUnitRankManager> ranks = map.get(unitId);
            BookUnit bu = new BookUnit();
            bu.bookId = book.getId();
            bu.unitId = unitId;
            bu.unitRankType = UnitRankType.COMMON;
            bu.unitRank = MiscUtils.firstElement(ranks).fetchUnitRank();
            bu.totalStarCount = ranks.size() * 3;
            bu.acquiredStarCount = Math.min(bu.totalStarCount, (unit_asc_map.containsKey(unitId) ? unit_asc_map.get(unitId) : 0));
            bu.totalRankCount = ranks.size();
            bu.acquiredStarRankCount = Math.min(bu.totalRankCount, (unit_asrc_map.containsKey(unitId) ? unit_asrc_map.get(unitId) : 0));
            bu.footprintCount = unit_footprint_map.containsKey(unitId) ? unit_footprint_map.get(unitId) : 0;
            bu.locked = false;
            bu.openDate = "";

            context.getUnits().add(bu);
        }
    }
}
