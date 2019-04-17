package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsUserPageViewLogService;
import com.voxlearning.utopia.service.ai.constant.PageViewType;
import com.voxlearning.utopia.service.ai.entity.ChipsUserPageViewLog;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsUserPageViewLogDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author guangqing
 * @since 2019/3/19
 */
@Named
@ExposeService(interfaceClass = ChipsUserPageViewLogService.class)
public class ChipsUserPageViewLogServiceImpl implements ChipsUserPageViewLogService {

    @Inject
    private ChipsUserPageViewLogDao chipsUserPageViewLogDao;

    @Override
    public List<ChipsUserPageViewLog> loadChipsUserPageViewLogByType(Collection<Long> userCol, PageViewType type) {
        if (CollectionUtils.isEmpty(userCol)) {
            return Collections.emptyList();
        }
        return chipsUserPageViewLogDao.loadByTypeAndUsers(type, userCol);
    }

    @Override
    public MapMessage upsertChipsUserPageViewLog(ChipsUserPageViewLog viewLog) {
        ChipsUserPageViewLog upsert = chipsUserPageViewLogDao.upsert(viewLog);
        return MapMessage.successMessage();
    }

    @Override
    public ChipsUserPageViewLog loadChipsUserPageViewLogById(String id) {
        return chipsUserPageViewLogDao.load(id);
    }

    public Map<String, ChipsUserPageViewLog> loadChipsUserPageViewLogByIds(Collection<String> ids) {
        return chipsUserPageViewLogDao.loads(ids);
    }
}
