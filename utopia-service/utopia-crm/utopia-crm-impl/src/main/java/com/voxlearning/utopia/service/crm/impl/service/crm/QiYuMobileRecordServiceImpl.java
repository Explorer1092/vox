package com.voxlearning.utopia.service.crm.impl.service.crm;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.extension.sensitive.codec.SensitiveLib;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.entity.crm.QiYuMobileRecord;
import com.voxlearning.utopia.service.crm.api.QiYuMobileRecordService;
import com.voxlearning.utopia.service.crm.impl.dao.crm.QiYuMobileRecordDao;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = QiYuMobileRecordService.class)
@Log4j
public class QiYuMobileRecordServiceImpl implements  QiYuMobileRecordService{

    @Inject
    private QiYuMobileRecordDao qiYuMobileRecordDao;

    @Override
    public void inserts(Collection<QiYuMobileRecord> records) {
        if (CollectionUtils.isNotEmpty(records)) {
            Date now = new Date();
            Map<Long, QiYuMobileRecord> loads = loads(records.stream().map(QiYuMobileRecord::getSessionId).collect(Collectors.toList()));
            records = records.stream().filter(r -> loads.get(r.getSessionId()) == null).collect(Collectors.toList());
            records.forEach(r -> {
                r.setCreateTime(now);
                // 手机号加密存储
                // 有的手机号查出来的带*** ，会导致异常，这里直接替换
                if (r.getCallOutNum() != null) {
                    r.setCallOutNum(SensitiveLib.encodeMobile(r.getCallOutNum().replaceAll("\\*", "")));
                }
                if (r.getCallInNum() != null) {
                    r.setCallInNum(SensitiveLib.encodeMobile(r.getCallInNum()));
                }
            });
            qiYuMobileRecordDao.inserts(records);
        }
    }

    @Override
    public Map<Long, QiYuMobileRecord> loads(Collection<Long> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return qiYuMobileRecordDao.loads(ids);
        }
        return Collections.emptyMap();
    }

    @Override
    public Page<QiYuMobileRecord> query(Date startTime, Date endTime, String callOutNum, String callInNum, int page, int pageSize) {
        return qiYuMobileRecordDao.query(startTime, endTime, callOutNum, callInNum, page, pageSize);
    }
}
