package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;
import com.voxlearning.utopia.service.mizar.api.loader.SchoolSettlementLoader;
import com.voxlearning.utopia.service.mizar.impl.dao.settlement.SchoolSettlementDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * SchoolSettlementLoaderImpl
 *
 * @author song.wang
 * @date 2017/6/23
 */
@Named
@Service(interfaceClass = SchoolSettlementLoader.class)
@ExposeService(interfaceClass = SchoolSettlementLoader.class)
public class SchoolSettlementLoaderImpl implements SchoolSettlementLoader {
    @Inject
    private SchoolSettlementDao schoolSettlementDao;


    @Override
    public SchoolSettlement loadSettlementBySchoolId(Long schoolId, Integer month) {
        return schoolSettlementDao.loadBySchoolId(schoolId, month);
    }

    @Override
    public Map<Long, SchoolSettlement> loadSettlementBySchoolIds(Collection<Long> schoolIds, Integer month) {
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }

        return schoolSettlementDao.loadBySchoolIds(schoolIds, month);
    }

    @Override
    public Map<Integer, SchoolSettlement> loadSettlementByMonths(Long schoolId, Collection<Integer> months) {
        if(CollectionUtils.isEmpty(months)){
            return Collections.emptyMap();
        }
        return schoolSettlementDao.loadByMonths(schoolId, months);
    }
}
