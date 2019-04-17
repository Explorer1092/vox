package com.voxlearning.utopia.service.crm.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.entity.crm.QiYuMobileRecord;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181023")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface QiYuMobileRecordService {
    /**
     * 批量插入记录
     * @param records
     * @return
     */
    void inserts(Collection<QiYuMobileRecord> records);

    default QiYuMobileRecord load(Long id) {
        return loads(Collections.singleton(id)).get(id);
    }

    Map<Long, QiYuMobileRecord> loads(Collection<Long> ids);

    Page<QiYuMobileRecord> query(Date startTime, Date endTime, String callOutNum, String callInNum, int page, int pageSize);
}

