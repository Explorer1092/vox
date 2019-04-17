package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.newhomework.api.DubbingScoreRecommendLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.DubbingRecommend;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.DubbingRecommendDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/8/25
 * \* Time: 下午3:18
 * \* Description:
 * \
 */
@Named
@Service(interfaceClass = DubbingScoreRecommendLoader.class)
@ExposeService(interfaceClass = DubbingScoreRecommendLoader.class)
public class DubbingScoreRecommendLoaderImpl implements DubbingScoreRecommendLoader {

    @Inject
    private DubbingRecommendDao dubbingRecommendDao;

    @Override
    public Map<String, DubbingRecommend> loadByHomeworkIds(Collection<String> homeworkIds) {
        return dubbingRecommendDao.loads(homeworkIds);
    }
}
