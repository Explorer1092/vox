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

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.context.FetchBookUnitsContext;
import com.voxlearning.utopia.service.afenti.api.data.BookUnit;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.*;

/**
 * @author Ruib
 * @since 2016/7/14
 */
@Named
public class FBU_LoadUltimateUnit extends SpringContainerSupport implements IAfentiTask<FetchBookUnitsContext> {
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    @Override
    public void execute(FetchBookUnitsContext context) {
        if (context.getLearningType() == AfentiLearningType.preparation) return;

        int classLevel = context.getStudent().getClazzLevel().getLevel();
        if (classLevel > 6 && classLevel < 10 && context.getSubject() == Subject.ENGLISH) {//中学英语没有终极单元
            return;
        }

        NewBookProfile book = context.getBook().book;
        Map<String, Integer> unit_asc_map = context.getUnit_asc_map();
        Map<String, Integer> unit_asrc_map = context.getUnit_asrc_map();
        Map<String, Integer> unit_footprint_map = context.getUnit_footprint_map();

        if (book.getSubjectId() == Subject.CHINESE.getId() && context.getIsNewRankBook()) {
            // 语文新关卡逻辑增加期中期末单元
            BookUnit midterm = new BookUnit();
            midterm.bookId = book.getId();
            midterm.unitId = MIDTERM_UNIT;
            midterm.unitRankType = UnitRankType.MIDTERM;
            midterm.unitRank = context.getUnits().size() / 2;
            midterm.acquiredStarCount = unit_asc_map.containsKey(MIDTERM_UNIT) ? unit_asc_map.get(MIDTERM_UNIT) : 0;
            midterm.totalStarCount = MIDTERM_RANK * 3;
            midterm.acquiredStarRankCount = unit_asrc_map.containsKey(MIDTERM_UNIT) ? unit_asrc_map.get(MIDTERM_UNIT) : 0;
            midterm.totalRankCount = MIDTERM_RANK;
            midterm.footprintCount = unit_footprint_map.containsKey(MIDTERM_UNIT) ? unit_footprint_map.get(MIDTERM_UNIT) : 0;
            midterm.locked = false;
            midterm.openDate = "";
            context.getUnits().add(midterm);

            BookUnit terminal = new BookUnit();
            terminal.bookId = book.getId();
            terminal.unitId = TERMINAL_UNIT;
            terminal.unitRankType = UnitRankType.TERMINAL;
            terminal.unitRank = context.getUnits().size() + 1;
            terminal.acquiredStarCount = unit_asc_map.containsKey(TERMINAL_UNIT) ? unit_asc_map.get(TERMINAL_UNIT) : 0;
            terminal.totalStarCount = TERMINAL_RANK * 3;
            terminal.acquiredStarRankCount = unit_asrc_map.containsKey(TERMINAL_UNIT) ? unit_asrc_map.get(TERMINAL_UNIT) : 0;
            terminal.totalRankCount = TERMINAL_RANK;
            terminal.footprintCount = unit_footprint_map.containsKey(TERMINAL_UNIT) ? unit_footprint_map.get(TERMINAL_UNIT) : 0;
            terminal.locked = false;
            terminal.openDate = "";
            context.getUnits().add(terminal);

        } else {
            BookUnit bu = new BookUnit();
            bu.bookId = book.getId();
            bu.unitId = ULTIMATE_UNIT;
            bu.unitRankType = UnitRankType.ULTIMATE;
            bu.unitRank = context.getUnits().size() + 1;
            bu.acquiredStarCount = unit_asc_map.containsKey(ULTIMATE_UNIT) ? unit_asc_map.get(ULTIMATE_UNIT) : 0;
            bu.totalStarCount = 99 * 3;
            bu.acquiredStarRankCount = unit_asrc_map.containsKey(ULTIMATE_UNIT) ? unit_asrc_map.get(ULTIMATE_UNIT) : 0;
            bu.totalRankCount = 99;
            bu.footprintCount = unit_footprint_map.containsKey(ULTIMATE_UNIT) ? unit_footprint_map.get(ULTIMATE_UNIT) : 0;
            bu.locked = false;
            bu.openDate = "";
            context.getUnits().add(bu);
        }
    }
}
