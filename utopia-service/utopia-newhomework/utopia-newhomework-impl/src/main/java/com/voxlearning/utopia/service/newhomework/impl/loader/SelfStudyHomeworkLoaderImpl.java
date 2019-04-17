package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.SelfStudyHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.SelfStudyWordIncreaseMapper;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@Named
@ExposeService(interfaceClass = SelfStudyHomeworkLoader.class)
public class SelfStudyHomeworkLoaderImpl implements SelfStudyHomeworkLoader {

    @Inject private SelfStudyHomeworkDao selfStudyHomeworkDao;
    @Inject private SelfStudyHomeworkResultDao selfStudyHomeworkResultDao;
    @Inject private SelfStudyWordIncreasePersistence selfStudyWordIncreasePersistence;
    @Inject private HomeworkSelfStudyRefDao homeworkSelfStudyRefDao;
    @Inject private SelfStudyHomeworkReportDao selfStudyHomeworkReportDao;
    @Inject private SelfStudyAccomplishmentDao selfStudyAccomplishmentDao;

    @Override
    public Map<String, SelfStudyHomework> loadSelfStudyHomeworkIncludeDisabled(Collection<String> ids) {
        ids = NewHomeworkUtils.filterIllegalSelfStudyHomeworkIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return selfStudyHomeworkDao.loads(ids);
    }

    @Override
    public Map<String, SelfStudyHomeworkResult> loadSelfStudyHomeworkResult(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return selfStudyHomeworkResultDao.loads(ids);
    }

    @Override
    public List<SelfStudyWordIncreaseMapper> findSelfStudyWordIncreaseMapper() {
        Map<Long, List<SelfStudyWordIncrease>> tempMap = selfStudyWordIncreasePersistence.findAllByClazzGroupId();
        List<SelfStudyWordIncreaseMapper> resultList = new ArrayList<>();

        for (Map.Entry<Long, List<SelfStudyWordIncrease>> entry : tempMap.entrySet()) {
            SelfStudyWordIncreaseMapper mapper = new SelfStudyWordIncreaseMapper();
            mapper.setGroupId(entry.getKey());

            // Map<bookId, Map<unitId, List<kpid>>>
            Map<String, Map<String, List<String>>> bookToKpMap = new HashMap<>();
            Map<String, List<SelfStudyWordIncrease>> bookIdToIdentityMap = entry.getValue().stream().collect(Collectors.groupingBy(SelfStudyWordIncrease::getBookId));
            for (Map.Entry<String, List<SelfStudyWordIncrease>> entry1 : bookIdToIdentityMap.entrySet()) {
                Map<String, List<String>> unitToKpMap = entry1
                        .getValue()
                        .stream()
                        .collect(Collectors.groupingBy(SelfStudyWordIncrease::getUnitId,
                                Collectors.mapping(SelfStudyWordIncrease::getKnowledgePointId, Collectors.toList())));

                bookToKpMap.put(entry1.getKey(), unitToKpMap);
            }
            mapper.setBookToKpMap(bookToKpMap);
            resultList.add(mapper);
        }
        return resultList;
    }

    @Override
    public Set<String> loadSelfStudyHomeworkIds(String newHomeworkId, List<Long> userIds) {
        if (StringUtils.isEmpty(newHomeworkId)) {
            return Collections.emptySet();
        }
        return homeworkSelfStudyRefDao.loadSelfStudyHomeworkIds(newHomeworkId, userIds);
    }

    @Override
    public Map<String, SelfStudyHomework> loadSelfStudyHomeworkIds(List<String> newHomeworkIds, Long userId) {
        Map<String, SelfStudyHomework> selfStudyHomeworkMap = new HashMap<>();
        if (CollectionUtils.isEmpty(newHomeworkIds)) {
            return Collections.emptyMap();
        }
        Set<String> refIds = homeworkSelfStudyRefDao.loadSelfStudyHomeworkIds(newHomeworkIds, userId);
        if (CollectionUtils.isNotEmpty(refIds)) {
            selfStudyHomeworkMap = selfStudyHomeworkDao.loads(refIds);
        }
        return selfStudyHomeworkMap;
    }

    @Override
    public Map<String, SelfStudyHomeworkReport> loadSelfStudyHomeworkReport(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return selfStudyHomeworkReportDao.loads(ids);
    }

    @Override
    public Map<String, SelfStudyAccomplishment> loadSelfStudyAccomplishment(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return selfStudyAccomplishmentDao.loads(ids);
    }
}
