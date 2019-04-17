package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolRefundAmortizeData;
import com.voxlearning.utopia.service.mizar.api.service.SchoolRefundAmortizeDataService;
import com.voxlearning.utopia.service.mizar.impl.dao.settlement.SchoolRefundAmortizeDataDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * SchoolRefundAmortizeDataServiceImpl
 *
 * @author song.wang
 * @date 2017/6/23
 */
@Named
@Service(interfaceClass = SchoolRefundAmortizeDataService.class)
@ExposeService(interfaceClass = SchoolRefundAmortizeDataService.class)
public class SchoolRefundAmortizeDataServiceImpl implements SchoolRefundAmortizeDataService {

    @Inject
    private SchoolRefundAmortizeDataDao schoolRefundAmortizeDataDao;

    @Override
    public MapMessage saveRefundAmortizeData(Collection<SchoolRefundAmortizeData> refundAmortizeDatas) {
        schoolRefundAmortizeDataDao.inserts(refundAmortizeDatas);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage disableByMonth(Collection<Long> schoolIds, Integer month) {
        if(CollectionUtils.isNotEmpty(schoolIds)) {
            schoolRefundAmortizeDataDao.disableByMonth(schoolIds, month);
        }
        return MapMessage.successMessage();
    }
}
