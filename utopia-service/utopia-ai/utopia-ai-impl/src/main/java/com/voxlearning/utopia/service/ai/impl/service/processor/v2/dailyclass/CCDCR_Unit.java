package com.voxlearning.utopia.service.ai.impl.service.processor.v2.dailyclass;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.constant.AIBookStatus;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsContentDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.Collections;
import java.util.Optional;

@Named
public class CCDCR_Unit extends AbstractAiSupport implements IAITask<ChipsContentDailyClassContext> {

    @Override
    public void execute(ChipsContentDailyClassContext context) {
        if (StringUtils.isBlank(context.getUnitId())) {
            return;
        }
        StoneBookData bookData = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(context.getBookId())))
                .filter(MapUtils::isNotEmpty)
                .map(e -> e.get(context.getBookId()))
                .map(StoneBookData::newInstance)
                .filter(e -> e.getJsonData() != null && CollectionUtils.isNotEmpty(e.getJsonData().getChildren()))
                .orElse(null);
        if (bookData == null) {
            context.errorResponse("没有教材");
            return;
        }

        if (!bookData.getId().equals(context.getBookRef().getBookId())) {
            context.setMapUrl(chipsContentService.fetchBookMapUrl(bookData.getId()));
        }

        StoneUnitData unitData = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(context.getUnitId())))
                .filter(MapUtils::isNotEmpty)
                .map(e -> e.get(context.getUnitId()))
                .map(StoneUnitData::newInstance)
                .filter(e -> e.getJsonData() != null)
                .orElse(null);

        if (unitData == null) {
            context.errorResponse("教材没有内容");
            return;
        }
        int rank = 1;
        for (StoneBookData.Node node : bookData.getJsonData().getChildren()) {
            if (unitData.getId().equals(node.getStone_data_id())) {
                break;
            }
            rank++;
        }
        context.setUnit(unitData);
        context.setBook(bookData);
        context.setRank(rank);
        context.setStatus(AIBookStatus.InTime);
    }
}
