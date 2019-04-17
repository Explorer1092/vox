package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolRefundAmortizeData;

import java.util.Collection;

/**
 * Created by Administrator on 2017/6/23.
 */
public interface SchoolRefundAmortizeDataService extends IPingable {

    MapMessage saveRefundAmortizeData(Collection<SchoolRefundAmortizeData> refundAmortizeDatas);

    MapMessage disableByMonth(Collection<Long> schoolIds, Integer month);
}
