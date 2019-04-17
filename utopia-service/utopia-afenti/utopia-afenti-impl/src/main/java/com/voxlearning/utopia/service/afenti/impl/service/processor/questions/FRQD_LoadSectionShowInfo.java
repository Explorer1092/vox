package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchRankQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.data.SectionInfo;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2017/8/17.
 * 获取进入关卡时弹窗显示的section信息  sectionName（课时名称）  sectionRankNo(课时关卡序号)
 */
@Named
public class FRQD_LoadSectionShowInfo extends SpringContainerSupport implements IAfentiTask<FetchRankQuestionsContext> {
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;

    @Override
    public void execute(FetchRankQuestionsContext context) {
        UnitRankType unitRankType = UtopiaAfentiConstants.getUnitType(context.getUnitId());
        if (!context.getIsNewRankBook() || unitRankType != UnitRankType.COMMON) {
            return;
        }
        SectionInfo sectionInfo = new SectionInfo();
        // 获取课时关卡序号
        List<AfentiLearningPlanUnitRankManager> unitRanks = context.getUnitRanks();
        if (CollectionUtils.isNotEmpty(unitRanks)) {
            List<AfentiLearningPlanUnitRankManager> sectionRanks =
                    unitRanks.stream().filter(r -> StringUtils.isNotBlank(r.getNewSectionId()) && StringUtils.equals(r.getNewSectionId(), context.getSectionId()))
                            .collect(Collectors.toList());
            // 排序
            Collections.sort(sectionRanks, (o1, o2) -> o1.getRank().compareTo(o2.getRank()));
            for (int i = 0; i < sectionRanks.size(); i++) {
                if (Objects.equals(sectionRanks.get(i).getRank(), context.getRank())) {
                    // 获取关卡序号
                    sectionInfo.setSectionRankNo(i + 1);
                }
            }
        }
        // 获取课时名称
        NewBookCatalog section = newContentLoaderClient.loadBookCatalogByCatalogId(context.getSectionId());
        if (section != null) {
            sectionInfo.setSectionName(section.getName());
            // 学习目标
            if (section.getExtras() != null) {
                String goalStr = SafeConverter.toString(section.getExtras().get("study_goal"));
                if (StringUtils.isNotBlank(goalStr)) {
                    String[] goalArr = StringUtils.split(goalStr, "\n");
                    if (goalArr != null && goalArr.length > 0) {
                        sectionInfo.setStudyTargets(Arrays.asList(goalArr));
                    }
                }
            }
        }

        // 获取课时知识点
        List<NewKnowledgePoint> points = newKnowledgePointLoaderClient.loadNewKnowledgePointsByCatalogId(context.getSectionId());
        if (CollectionUtils.isNotEmpty(points)) {
            sectionInfo.setKps(points.stream().map(NewKnowledgePoint::getName).collect(Collectors.toList()));
        }
        context.setSectionInfo(sectionInfo);
    }
}
