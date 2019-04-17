package com.voxlearning.utopia.service.ai.impl.support;

import java.math.BigDecimal;
import java.util.Date;

public class ChipsOrderProductSupport {

    /**
     * 向上取整算天数
     * @param begin
     * @param end
     * @return
     */
    public static int dayDiffCeil(Date begin, Date end) {
        return new Double(Math.ceil(new BigDecimal(end.getTime() - begin.getTime() ).divide(new BigDecimal(1000 * 60 * 60 * 24), 2, BigDecimal.ROUND_HALF_UP).doubleValue())).intValue();
    }

}
