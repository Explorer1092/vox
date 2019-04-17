package com.voxlearning.utopia.service.ai.impl.service.processor.v2.dailyclass;

import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserExtSplit;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsContentDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Named
public class CCDCR_LoadUserStudyInfo extends AbstractAiSupport implements IAITask<ChipsContentDailyClassContext> {

    @Override
    public void execute(ChipsContentDailyClassContext context) {
        Set<String> unitList = aiUserUnitResultHistoryDao.loadByUserId(context.getUser().getId()).stream().filter(e -> !chipsContentService.isTrailUnit(e.getUnitId())).map(AIUserUnitResultHistory::getUnitId).collect(Collectors.toSet());
        int studyNum = unitList != null ? unitList.size() : 0;
        context.setStudyNumber(studyNum);
        Long senNum = Optional.ofNullable(chipsEnglishUserExtSplitDao.load(context.getUser().getId()))
                .map(ChipsEnglishUserExtSplit::getSentenceLearn)
                .orElse(0L);
        context.setSentenceNumber(senNum);
    }
}
