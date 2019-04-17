package com.voxlearning.utopia.service.ai.impl.service.processor.dailyclass;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.constant.AIBookStatus;
import com.voxlearning.utopia.service.ai.context.AIUserDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;

import javax.inject.Named;
import java.util.Date;
import java.util.stream.Collectors;

@Named
public class ADC_LoadUnit extends AbstractAiSupport implements IAITask<AIUserDailyClassContext> {

    @Override
    public void execute(AIUserDailyClassContext context) {
        if (StringUtils.isBlank(context.getUnitId())) {
            return;
        }
        NewBookCatalog res = newContentLoaderClient.loadBookCatalogByCatalogId(context.getUnitId());
        context.setUnit(res);
        int rank = res != null ? fetchUnitListExcludeTrial(res.bookId()) //排除试用单元
                .stream()
                .filter(e -> res.getRank() >= e.getRank())
                .collect(Collectors.toList()).size() : 0;
        if (!chipsUserService.isInWhiteList(context.getUser().getId()) && DateUtils.addDays(context.getBeginDate(), rank - 1).after(new Date())) {
            context.errorResponse("地图未开放");
        }
        context.setStatus(AIBookStatus.InTime);
        context.setRank(rank);
    }
}
