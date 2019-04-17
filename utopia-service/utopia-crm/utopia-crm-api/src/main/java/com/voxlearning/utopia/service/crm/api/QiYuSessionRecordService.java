package com.voxlearning.utopia.service.crm.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.entity.crm.QiYuSessionRecord;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181023")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface QiYuSessionRecordService {
    /**
     * 批量插入记录
     * @param records
     * @return
     */
    void inserts(Collection<QiYuSessionRecord> records);

    default QiYuSessionRecord load(Long id) {
        return loads(Collections.singleton(id)).get(id);
    }

    Map<Long, QiYuSessionRecord> loads(Collection<Long> ids);

    Page<QiYuSessionRecord> query(Date startTime, Date endTime, int page, int pageSize);
}
