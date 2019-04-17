package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.math.BigDecimal;

/**
 * @author Ruib
 * @since 2017/4/10
 */
@Named
public class CR_CalculateMultiple extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Override
    public void execute(CastleResultContext context) {
        BigDecimal multiple = new BigDecimal(1);

        // 错题视频购买奖励翻倍
        if (CollectionUtils.isNotEmpty(context.getAfentiVideoActivatedProducts())) {
            multiple = multiple.multiply(new BigDecimal(2));
        }

        context.setMultiple(multiple);
        context.getResult().put("multiple", multiple.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
    }
}
