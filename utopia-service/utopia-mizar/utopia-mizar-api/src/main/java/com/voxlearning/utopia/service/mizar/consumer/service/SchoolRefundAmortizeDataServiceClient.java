package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolRefundAmortizeData;
import com.voxlearning.utopia.service.mizar.api.service.SchoolRefundAmortizeDataService;

import java.util.Collection;

/**
 * SchoolRefundAmortizeDataServiceClient
 *
 * @author song.wang
 * @date 2017/6/23
 */
public class SchoolRefundAmortizeDataServiceClient implements SchoolRefundAmortizeDataService {

    @ImportService(interfaceClass = SchoolRefundAmortizeDataService.class)
    private SchoolRefundAmortizeDataService remoteReference;

    @Override
    public MapMessage saveRefundAmortizeData(Collection<SchoolRefundAmortizeData> refundAmortizeDatas) {
        return remoteReference.saveRefundAmortizeData(refundAmortizeDatas);
    }

    @Override
    public MapMessage disableByMonth(Collection<Long> schoolIds, Integer month) {
        return remoteReference.disableByMonth(schoolIds, month);
    }
}
