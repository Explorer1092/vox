package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.DubbingScoreRecommendLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.DubbingRecommend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/8/25
 * \* Time: 下午3:15
 * \* Description:
 * \
 */
public class DubbingScoreRecommendLoaderClient implements DubbingScoreRecommendLoader {

    @ImportService(interfaceClass = DubbingScoreRecommendLoader.class)
    private DubbingScoreRecommendLoader remoteReference;

    @Override
    public Map<String, DubbingRecommend> loadByHomeworkIds(Collection<String> homeworkIds) {
        return remoteReference.loadByHomeworkIds(homeworkIds);
    }

    public List<DubbingRecommend> loadExcludeNoRecommend(Collection<String> homeworkIds){
        return new ArrayList<>(loadByHomeworkIds(homeworkIds).values());
    }
}
