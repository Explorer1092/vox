package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.loader.MizarChangeRecordLoader;
import com.voxlearning.utopia.service.mizar.impl.dao.change.MizarEntityChangeRecordDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * Created by yuechen.wang on 2016/10/10.
 */
@Named
@Service(interfaceClass = MizarChangeRecordLoader.class)
@ExposeService(interfaceClass = MizarChangeRecordLoader.class)
public class MizarChangeRecordLoaderImpl extends SpringContainerSupport implements MizarChangeRecordLoader {

    @Inject private MizarEntityChangeRecordDao mizarEntityChangeRecordDao;

    @Override
    public MizarEntityChangeRecord loadRecordById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return mizarEntityChangeRecordDao.load(id);
    }

    @Override
    public List<MizarEntityChangeRecord> loadRecordsByApplicant(String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        return mizarEntityChangeRecordDao.loadByApplicant(userId);
    }

    @Override
    public List<MizarEntityChangeRecord> loadRecordsByStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return Collections.emptyList();
        }
        return mizarEntityChangeRecordDao.loadByStatus(status);
    }

    @Override
    public List<MizarEntityChangeRecord> loadRecordsByEntityType(String entityType) {
        if (StringUtils.isBlank(entityType)) {
            return Collections.emptyList();
        }
        return mizarEntityChangeRecordDao.loadByEntityType(entityType);
    }

    @Override
    public List<MizarEntityChangeRecord> loadRecordByTarget(String targetId, String entityType) {
        if (StringUtils.isBlank(targetId) || StringUtils.isBlank(entityType)) {
            return Collections.emptyList();
        }
        return mizarEntityChangeRecordDao.loadByTarget(targetId, entityType);
    }
}
