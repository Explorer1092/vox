package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;
import com.voxlearning.utopia.service.mizar.api.service.SchoolSettlementService;
import com.voxlearning.utopia.service.mizar.impl.dao.settlement.SchoolSettlementDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * SchoolSettlementServiceImpl
 *
 * @author song.wang
 * @date 2017/6/23
 */
@Named
@Service(interfaceClass = SchoolSettlementService.class)
@ExposeService(interfaceClass = SchoolSettlementService.class)
public class SchoolSettlementServiceImpl implements SchoolSettlementService {

    @Inject
    private SchoolSettlementDao schoolSettlementDao;

    @Override
    public MapMessage saveSettlementData(Collection<SchoolSettlement> schoolSettlements) {
        schoolSettlementDao.inserts(schoolSettlements);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage disableByMonth(Collection<Long> schoolIds, Integer month) {
        if(CollectionUtils.isNotEmpty(schoolIds)){
            schoolSettlementDao.disableByMonth(schoolIds, month);
        }
        return MapMessage.successMessage();
    }
}
