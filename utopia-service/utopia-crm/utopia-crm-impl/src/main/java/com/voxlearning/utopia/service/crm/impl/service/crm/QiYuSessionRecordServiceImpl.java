package com.voxlearning.utopia.service.crm.impl.service.crm;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.entity.crm.QiYuSessionRecord;
import com.voxlearning.utopia.service.crm.api.QiYuSessionRecordService;
import com.voxlearning.utopia.service.crm.impl.dao.crm.QiYuSessionRecordDao;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = QiYuSessionRecordService.class)
@Log4j
public class QiYuSessionRecordServiceImpl implements QiYuSessionRecordService {

    @Inject
    private QiYuSessionRecordDao qiYuSessionRecordDao;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Override
    public void inserts(Collection<QiYuSessionRecord> records) {
        if (CollectionUtils.isNotEmpty(records)) {
            Map<Long, QiYuSessionRecord> loads = loads(records.stream().map(QiYuSessionRecord::getId).collect(Collectors.toList()));
            records = records.stream().filter(r -> loads.get(r.getId()) == null).collect(Collectors.toList());
            Date now = new Date();
            // 17id 至少5位
            Map<Long, UserAuthentication> userAuthenticationMap = userLoaderClient.loadUserAuthentications(records.stream().filter(m -> SafeConverter.toLong(m.getForeignId()) > 9999).map(m -> SafeConverter.toLong(m.getForeignId())).collect(Collectors.toList()));
            records.forEach(r -> {
                r.setCreateTime(now);
                UserAuthentication userAuthentication = userAuthenticationMap.get(SafeConverter.toLong(r.getForeignId()));
                if (userAuthentication != null) {
                    r.setMobile(userAuthentication.getSensitiveMobile());
                }
            });
            qiYuSessionRecordDao.inserts(records);
        }
    }

    @Override
    public Map<Long, QiYuSessionRecord> loads(Collection<Long> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return qiYuSessionRecordDao.loads(ids);
        }
        return Collections.emptyMap();
    }

    @Override
    public Page<QiYuSessionRecord> query(Date startTime, Date endTime,int page, int pageSize) {
        return qiYuSessionRecordDao.query(startTime, endTime, page, pageSize);
    }
}
