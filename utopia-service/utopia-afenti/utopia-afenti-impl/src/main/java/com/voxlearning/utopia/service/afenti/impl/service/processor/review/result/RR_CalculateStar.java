package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.List;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class RR_CalculateStar extends SpringContainerSupport implements IAfentiTask<ReviewResultContext> {

    @Override
    public void execute(ReviewResultContext context) {
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

        context.getResult().put("rank", 1);
        context.getResult().put("star", context.getStar());
        context.getResult().put("right", rn);
        context.getResult().put("total", context.getHistories().size());

        int currentStar = context.getStat() == null ? 0 : context.getStat().getStar();
        // 如果星星数没有变化，后面就可以歇了
        if (context.getStar() <= currentStar) {
            context.getResult().put("silver", 0);
            context.getResult().put("successiveSilver", 0);
            context.getResult().put("bonus", 0);
            context.getResult().put("creditCount", 0);
            context.terminateTask();
        }
    }
}
