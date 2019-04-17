package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;
import com.voxlearning.utopia.service.mizar.api.loader.SchoolSettlementLoader;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * SchoolSettlementLoaderClient
 *
 * @author song.wang
 * @date 2017/6/23
 */
public class SchoolSettlementLoaderClient implements SchoolSettlementLoader {

    @Getter
    @ImportService(interfaceClass = SchoolSettlementLoader.class)
    private SchoolSettlementLoader remoteReference;

    @Override
    public SchoolSettlement loadSettlementBySchoolId(Long schoolId, Integer month) {
        return remoteReference.loadSettlementBySchoolId(schoolId, month);
    }

    @Override
    public Map<Long, SchoolSettlement> loadSettlementBySchoolIds(Collection<Long> schoolIds, Integer month) {
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        return remoteReference.loadSettlementBySchoolIds(schoolIds, month);
    }

    @Override
    public Map<Integer, SchoolSettlement> loadSettlementByMonths(Long schoolId, Collection<Integer> months) {
        if(CollectionUtils.isEmpty(months)){
            return Collections.emptyMap();
        }
        return remoteReference.loadSettlementByMonths(schoolId, months);
    }
}
