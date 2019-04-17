package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.BasicReviewHomeworkCacheLoader;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkCacheMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/11/10
 */
public class BasicReviewHomeworkCacheLoaderClient implements BasicReviewHomeworkCacheLoader {
    @ImportService(interfaceClass = BasicReviewHomeworkCacheLoader.class)
    private BasicReviewHomeworkCacheLoader remoteRegerence;

    @Override
    public BasicReviewHomeworkCacheMapper loadBasicReviewHomeworkCacheMapper(String packageId, Long studentId) {
        return remoteRegerence.loadBasicReviewHomeworkCacheMapper(packageId, studentId);
    }

    @Override
    public void removeBasicReviewHomeworkCacheMapper(String packageId, Long studentId) {
        remoteRegerence.removeBasicReviewHomeworkCacheMapper(packageId, studentId);
    }

    @Override
    public Map<Long, BasicReviewHomeworkCacheMapper> loadBasicReviewHomeworkCacheMapper(String packageId, Collection<Long> studentIds) {
        return remoteRegerence.loadBasicReviewHomeworkCacheMapper(packageId, studentIds);
    }

    @Override
    public BasicReviewHomeworkCacheMapper addOrModifyBasicReviewHomeworkCacheMapper(NewHomeworkResult newHomeworkResult, String packageId, Long studentId) {
        return remoteRegerence.addOrModifyBasicReviewHomeworkCacheMapper(newHomeworkResult, packageId, studentId);
    }

    @Override
    public List<BasicReviewHomeworkCacheMapper> loadBasicReviewHomeworkCacheMappers(Long clazzGroupId) {
        return remoteRegerence.loadBasicReviewHomeworkCacheMappers(clazzGroupId);
    }
}
