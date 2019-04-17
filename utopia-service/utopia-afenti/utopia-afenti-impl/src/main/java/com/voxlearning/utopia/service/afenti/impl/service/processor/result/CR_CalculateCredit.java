package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;

/**
 * 新增积分奖励[45497]
 *
 * @author liu jingchao
 * @since 2017/5/25
 */
@Named
public class CR_CalculateCredit extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Override
    public void execute(CastleResultContext context) {
        //预习的不发奖励
        if (AfentiLearningType.preparation == context.getAfentiLearningType()) return;

        // 当前星星数量
        int currentStar = context.getStat() == null ? 0 : context.getStat().getStar();

        int amount = 0;
        // 只有当前星星数为0，本次获得的星星数大于0，即第一次获得星星时进行10积分奖励
        if (currentStar == 0 && context.getStar() > 0)
            amount = 10;
        context.setCreditCount(amount);
    }
}
