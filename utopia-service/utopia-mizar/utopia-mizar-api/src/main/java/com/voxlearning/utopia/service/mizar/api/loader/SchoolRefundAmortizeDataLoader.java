package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolRefundAmortizeData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * SchoolRefundAmortizeDataLoader 退款摊销数据
 *
 * @author song.wang
 * @date 2017/6/23
 */
public interface SchoolRefundAmortizeDataLoader extends IPingable{

    List<SchoolRefundAmortizeData> loadAmortizeBySchoolId(Long schoolId, Integer month);

    Map<Long, List<SchoolRefundAmortizeData>> loadAmortizeBySchoolIds(Collection<Long> schoolIds, Integer month);
}
