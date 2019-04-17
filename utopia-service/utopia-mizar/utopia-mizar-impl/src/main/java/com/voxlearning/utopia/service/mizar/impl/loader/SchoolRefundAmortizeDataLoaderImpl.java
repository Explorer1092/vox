package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolRefundAmortizeData;
import com.voxlearning.utopia.service.mizar.api.loader.SchoolRefundAmortizeDataLoader;
import com.voxlearning.utopia.service.mizar.impl.dao.settlement.SchoolRefundAmortizeDataDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SchoolRefundAmortizeDataLoaderImpl
 *
 * @author song.wang
 * @date 2017/6/23
 */
@Named
@Service(interfaceClass = SchoolRefundAmortizeDataLoader.class)
@ExposeService(interfaceClass = SchoolRefundAmortizeDataLoader.class)
public class SchoolRefundAmortizeDataLoaderImpl implements SchoolRefundAmortizeDataLoader {

    @Inject
    private SchoolRefundAmortizeDataDao schoolRefundAmortizeDataDao;

    @Override
    public List<SchoolRefundAmortizeData> loadAmortizeBySchoolId(Long schoolId, Integer month) {
        return schoolRefundAmortizeDataDao.loadBySchoolId(schoolId, month);
    }

    @Override
    public Map<Long, List<SchoolRefundAmortizeData>> loadAmortizeBySchoolIds(Collection<Long> schoolIds, Integer month) {
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        return schoolRefundAmortizeDataDao.loadBySchoolIds(schoolIds, month);
    }
}
