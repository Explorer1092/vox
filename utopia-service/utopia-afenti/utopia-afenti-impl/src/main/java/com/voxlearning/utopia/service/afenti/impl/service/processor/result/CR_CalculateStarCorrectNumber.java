package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * 根据推题数量计算正确题数和星星的对应关系
 *
 * @author Ruib
 * @since 2016/7/22
 */
@Named
public class CR_CalculateStarCorrectNumber extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Override
    public void execute(CastleResultContext context) {
        int divide = 3; // 一共有三颗星
        int total = context.getHistories().size();

        if (total < divide) {
            logger.error("CR_CalculateStarCorrectNumber question not enough, {}", JsonUtils.toJson(context));
            context.errorResponse();
            return;
        }

        int d = total / divide;
        int m = total % divide;
        int k = divide - m;
        List<Integer> result = new ArrayList<>();

        while (total > 0) {
            int t = d;
            if (k > 0) {
                if (result.size() > 0) {
                    result.add(result.get(result.size() - 1) + t);
                } else {
                    result.add(t);
                }
                k--;
            } else {
                t++;
                if (result.size() > 0) {
                    result.add(result.get(result.size() - 1) + t);
                } else {
                    result.add(t);
                }
            }
            total -= t;
        }

        if (result.size() != 3) {
            logger.error("CR_CalculateStarCorrectNumber correspondence calculate error, {}", JsonUtils.toJson(context));
            context.errorResponse();
            return;
        }

        context.getCorrespondence().addAll(result);
    }
}
