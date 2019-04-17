package com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.HomeworkSelfStudyRef;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/3/22
 */
@Named
@CacheBean(type = HomeworkSelfStudyRef.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class HomeworkSelfStudyRefDao extends AsyncDynamicMongoPersistence<HomeworkSelfStudyRef, String> {

    @Override
    protected String calculateDatabase(String template, HomeworkSelfStudyRef document) {
        HomeworkSelfStudyRef.ID id = document.parseID();
        String month = StringUtils.substring(id.getDay(), 0, 6);
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, HomeworkSelfStudyRef document) {
        return null;
    }

    @Override
    protected void calculateCacheDimensions(HomeworkSelfStudyRef document, Collection<String> dimensions) {
        dimensions.add(HomeworkSelfStudyRef.ck_id(document.getId()));
    }

    public void insertIfNull(HomeworkSelfStudyRef entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            return;
        }
        insertIfAbsent(entity.getId(), entity);
    }

    /**
     * 查询自学作业ID
     * @param newHomeworkId 原作业ID
     * @param users users
     * @return
     */
    public Set<String> loadSelfStudyHomeworkIds(String newHomeworkId, Collection<User> users) {
        Set<String> refIds = users.stream().map(i -> new HomeworkSelfStudyRef.ID(newHomeworkId, i.getId()).toString()).collect(Collectors.toSet());
        return loadSelfStudyHomeworkIds(refIds);
    }

    /**
     * 查询自学作业ID
     * @param newHomeworkId 原作业ID
     * @param userIds userIds
     * @return
     */
    public Set<String> loadSelfStudyHomeworkIds(String newHomeworkId, List<Long> userIds) {
        Set<String> refIds = userIds.stream().map(i -> new HomeworkSelfStudyRef.ID(newHomeworkId, i).toString()).collect(Collectors.toSet());
        return loadSelfStudyHomeworkIds(refIds);
    }

    /**
     * 查询自学作业ID
     * @param newHomeworkIds 原作业IDs
     * @param userId userId
     * @return
     */
    public Set<String> loadSelfStudyHomeworkIds(List<String> newHomeworkIds, Long userId) {
        Set<String> refIds = newHomeworkIds.stream().map(i -> new HomeworkSelfStudyRef.ID(i, userId).toString()).collect(Collectors.toSet());
        return loadSelfStudyHomeworkIds(refIds);
    }

    private Set<String> loadSelfStudyHomeworkIds(Set<String> refIds) {
        Map<String, HomeworkSelfStudyRef> selfStudyRefMap = loads(refIds);
        return selfStudyRefMap.values().stream().map(HomeworkSelfStudyRef::getSelfStudyId).collect(Collectors.toSet());
    }

}
