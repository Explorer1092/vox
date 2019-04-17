package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 计算星星的区间
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class RR_CalculateStarInterval extends SpringContainerSupport implements IAfentiTask<ReviewResultContext> {

    @Override
    public void execute(ReviewResultContext context) {
        int divide = 3; // 一共有三颗星
        int total = context.getHistories().size();

        if (total < divide) {
            logger.error("RR_CalculateStarInterval question not enough, {}", JsonUtils.toJson(context));
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
            logger.error("RR_CalculateStarInterval correspondence calculate error, {}", JsonUtils.toJson(context));
            context.errorResponse();
            return;
        }

        context.getCorrespondence().addAll(result);
    }
}
