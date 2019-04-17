package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolRefundAmortizeData;
import com.voxlearning.utopia.service.mizar.api.loader.SchoolRefundAmortizeDataLoader;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SchoolRefundAmortizeDataLoaderClient
 *
 * @author song.wang
 * @date 2017/6/23
 */
public class SchoolRefundAmortizeDataLoaderClient implements SchoolRefundAmortizeDataLoader {

    @Getter
    @ImportService(interfaceClass = SchoolRefundAmortizeDataLoader.class)
    private SchoolRefundAmortizeDataLoader remoteReference;

    @Override
    public List<SchoolRefundAmortizeData> loadAmortizeBySchoolId(Long schoolId, Integer month) {
        return remoteReference.loadAmortizeBySchoolId(schoolId, month);
    }

    @Override
    public Map<Long, List<SchoolRefundAmortizeData>> loadAmortizeBySchoolIds(Collection<Long> schoolIds, Integer month) {
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        return remoteReference.loadAmortizeBySchoolIds(schoolIds, month);
    }
}
