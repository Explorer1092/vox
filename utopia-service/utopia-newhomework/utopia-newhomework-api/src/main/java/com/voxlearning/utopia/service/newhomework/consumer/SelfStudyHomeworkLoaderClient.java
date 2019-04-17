package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.SelfStudyHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.SelfStudyWordIncreaseMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description: 订正作业
 * @author: Mr_VanGogh
 * @date: 2018/9/20 上午11:34
 */
public class SelfStudyHomeworkLoaderClient implements SelfStudyHomeworkLoader {

    @ImportService(interfaceClass = SelfStudyHomeworkLoader.class)
    private SelfStudyHomeworkLoader remoteReference;

    @Override
    public Map<String, SelfStudyHomework> loadSelfStudyHomeworkIncludeDisabled(Collection<String> ids) {
        return remoteReference.loadSelfStudyHomeworkIncludeDisabled(ids);
    }

    @Override
    public Map<String, SelfStudyHomeworkResult> loadSelfStudyHomeworkResult(Collection<String> ids) {
        return remoteReference.loadSelfStudyHomeworkResult(ids);
    }

    @Override
    public List<SelfStudyWordIncreaseMapper> findSelfStudyWordIncreaseMapper() {
        return remoteReference.findSelfStudyWordIncreaseMapper();
    }

    @Override
    public Set<String> loadSelfStudyHomeworkIds(String newHomeworkId, List<Long> userIds) {
        return remoteReference.loadSelfStudyHomeworkIds(newHomeworkId, userIds);
    }

    @Override
    public Map<String, SelfStudyHomework> loadSelfStudyHomeworkIds(List<String> newHomeworkIds, Long userId) {
        return remoteReference.loadSelfStudyHomeworkIds(newHomeworkIds, userId);
    }

    @Override
    public Map<String, SelfStudyHomeworkReport> loadSelfStudyHomeworkReport(Collection<String> ids) {
        return remoteReference.loadSelfStudyHomeworkReport(ids);
    }

    @Override
    public Map<String, SelfStudyAccomplishment> loadSelfStudyAccomplishment(Collection<String> ids) {
        return remoteReference.loadSelfStudyAccomplishment(ids);
    }
}
