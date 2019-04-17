package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;
import com.voxlearning.utopia.service.mizar.api.service.SchoolSettlementService;

import java.util.Collection;

/**
 * SchoolSettlementServiceClient
 *
 * @author song.wang
 * @date 2017/6/23
 */
public class SchoolSettlementServiceClient implements SchoolSettlementService {

    @ImportService(interfaceClass = SchoolSettlementService.class)
    private SchoolSettlementService remoteReference;

    @Override
    public MapMessage saveSettlementData(Collection<SchoolSettlement> schoolSettlements) {
        return remoteReference.saveSettlementData(schoolSettlements);
    }

    @Override
    public MapMessage disableByMonth(Collection<Long> schoolIds, Integer month) {
        return remoteReference.disableByMonth(schoolIds, month);
    }
}
