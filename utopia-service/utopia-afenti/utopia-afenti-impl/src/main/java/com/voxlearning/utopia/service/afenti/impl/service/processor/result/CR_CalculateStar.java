package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiOperationalInfoService;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/7/21
 */
@Named
public class CR_CalculateStar extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Inject AfentiOperationalInfoService afentiOperationalInfoService;

    @Override
    public void execute(CastleResultContext context) {
        List<Integer> correspondence = context.getCorrespondence();
        long rn = context.getHistories().stream().filter(h -> h.getRightNum() > 0).count();

        if (correspondence.get(2) == rn) {
            context.setStar(3);
        } else if (correspondence.get(1) <= rn) {
            context.setStar(2);
        } else if (correspondence.get(0) <= rn) {
            context.setStar(1);
        } else {
            context.setStar(0);
        }

        context.getResult().put("rank", context.getRank());
        context.getResult().put("star", context.getStar());
        context.getResult().put("right", rn);
        context.getResult().put("total", context.getHistories().size());
        // 如果星星数没有变化，后面就可以歇了(从CR_CalculateStar.class中移了过来，要发完报告再执行)
        int currentStar = context.getStat() == null ? 0 : context.getStat().getStar();
        if (context.getStar() <= currentStar) {
            context.getResult().put("silver", 0);
            context.getResult().put("successiveSilver", 0);
            context.getResult().put("bonus", 0);
            context.getResult().put("creditCount", 0);
            if (context.isBoughtAfenti()) {
                afentiOperationalInfoService.addUserRewardInfo(context.getStudent());
            }
            context.terminateTask();
        }
    }
}
