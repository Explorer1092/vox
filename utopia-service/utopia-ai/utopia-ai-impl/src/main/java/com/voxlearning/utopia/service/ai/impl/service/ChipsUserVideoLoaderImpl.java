package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsUserVideoLoader;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;
import com.voxlearning.utopia.service.ai.entity.ChipsVideoBlackList;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserVideoDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsVideoBlackListDao;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = ChipsUserVideoLoader.class)
public class ChipsUserVideoLoaderImpl implements ChipsUserVideoLoader {

    @Inject
    private AIUserVideoDao aiUserVideoDao;
    @Inject
    private ChipsVideoBlackListDao videoBlackListDao;
    @Inject
    private UserLoaderClient userLoaderClient;

    @Override
    public AIUserVideo loadById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return aiUserVideoDao.load(id);
    }

    @Override
    public List<AIUserVideo> loadByUnitId(String unitId, AIUserVideo.ExamineStatus examineStatus) {
        if (StringUtils.isBlank(unitId) || examineStatus == null) {
            return Collections.emptyList();
        }
        return aiUserVideoDao.loadByUnitId(unitId, examineStatus);
    }

    @Override
    public List<AIUserVideo> loadByUserId(Long userId) {
        if (userId == null || userId.compareTo(0L) <= 0) {
            return Collections.emptyList();
        }
        return aiUserVideoDao.loadByUserId(userId);
    }

    @Override
    public List<AIUserVideo> loadByDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null || startDate.after(endDate)) {
            return Collections.emptyList();
        }
        return aiUserVideoDao.loadByDateRange(startDate, endDate);
    }

    @Override
    public Set<Long> loadVideoBlackList() {
        List<ChipsVideoBlackList> blackList = videoBlackListDao.loadAll();
        if (CollectionUtils.isEmpty(blackList)) {
            return Collections.emptySet();
        }
        return blackList.stream().map(ChipsVideoBlackList::getId).collect(Collectors.toSet());
    }


    @Override
    public MapMessage loadVideoBlackListForCrm() {
        List<ChipsVideoBlackList> blackList = videoBlackListDao.loadAll();
        return MapMessage.successMessage().add("blackList", blackList);
    }
}
