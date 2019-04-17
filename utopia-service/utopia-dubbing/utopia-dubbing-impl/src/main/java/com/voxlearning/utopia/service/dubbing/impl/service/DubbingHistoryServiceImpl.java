package com.voxlearning.utopia.service.dubbing.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.dubbing.api.DubbingCacheService;
import com.voxlearning.utopia.service.dubbing.api.DubbingHistoryService;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;
import com.voxlearning.utopia.service.dubbing.impl.dao.DubbingHistoryDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * Created by jiang wei on 2017/8/25.
 */
@Named
@ExposeService(interfaceClass = DubbingHistoryService.class)
public class DubbingHistoryServiceImpl implements DubbingHistoryService {

    @Inject private DubbingHistoryDao dubbingHistoryDao;
    @Inject private DubbingCacheService dubbingCacheService;

    @Override
    public void saveDubbingHistory(DubbingHistory history) {
        if (history == null) {
            return;
        }
        if (StringUtils.isBlank(history.getId())) {
            history.setId(DubbingHistory.generateId(history.getUserId(), history.getDubbingId(), history.getClazzId(), history.getCategoryId()));
        }
        if (StringUtils.isBlank(history.getFixId())) {
            history.setFixId(DubbingHistory.generateFixId(history.getUserId(), history.getDubbingId()));
        }
        if (history.getDisabled() == null) {
            history.setDisabled(Boolean.FALSE);
        }
        dubbingHistoryDao.upsertDubbingHistory(history);
    }

    @Override
    public void disabledDubbingHistory(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        dubbingHistoryDao.disabledDubbingHistory(ids);
    }
}
