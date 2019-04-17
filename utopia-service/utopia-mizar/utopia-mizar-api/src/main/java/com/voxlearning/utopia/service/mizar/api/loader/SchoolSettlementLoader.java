package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;

import java.util.Collection;
import java.util.Map;

/**
 * SchoolSettlementLoader
 *
 * @author song.wang
 * @date 2017/6/23
 */
public interface SchoolSettlementLoader extends IPingable {

    SchoolSettlement loadSettlementBySchoolId(Long schoolId, Integer month);

    Map<Long, SchoolSettlement> loadSettlementBySchoolIds(Collection<Long> schoolIds, Integer month);

    Map<Integer, SchoolSettlement> loadSettlementByMonths(Long schoolId, Collection<Integer> months);
}
