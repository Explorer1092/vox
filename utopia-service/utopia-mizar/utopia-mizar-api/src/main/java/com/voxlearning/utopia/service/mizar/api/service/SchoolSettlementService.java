package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;

import java.util.Collection;

/**
 * SchoolSettlementService
 *
 * @author song.wang
 * @date 2017/6/23
 */
public interface SchoolSettlementService extends IPingable {

    MapMessage saveSettlementData(Collection<SchoolSettlement> schoolSettlements);

    MapMessage disableByMonth(Collection<Long> schoolIds, Integer month);
}
