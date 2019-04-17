package com.voxlearning.utopia.service.afenti.impl.service.processor.ranks;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.context.FetchUnitRanksContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/12/6
 */
@Named
public class FURD_LoadUnit extends SpringContainerSupport implements IAfentiTask<FetchUnitRanksContext> {
    @Inject private NewContentLoaderClient newContentLoaderClient;

    @Override
    public void execute(FetchUnitRanksContext context) {
        if (context.getUnitRankType() != UnitRankType.COMMON) {
            if (context.getUnitRankType() == UnitRankType.ULTIMATE) {
                context.setUnitName("复习单元");
            }
            if (context.getUnitRankType() == UnitRankType.MIDTERM) {
                context.setUnitName("期中复习");
            }
            if (context.getUnitRankType() == UnitRankType.TERMINAL) {
                context.setUnitName("期末复习");
            }
        } else {
            NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(context.getUnitId());
            if (null == unit) {
                logger.error("FURD_LoadUnit Cannot load unit for user {}, subject {}",
                        context.getStudent().getId(), context.getSubject());
                context.errorResponse();
                return;
            }
            context.setUnitName(StringUtils.defaultString(unit.getName()));
        }
    }
}
