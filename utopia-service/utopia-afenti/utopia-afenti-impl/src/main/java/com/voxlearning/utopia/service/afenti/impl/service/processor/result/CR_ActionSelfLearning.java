package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/8/17
 */
@Named
public class CR_ActionSelfLearning extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Inject private ActionServiceClient actionServiceClient;

    @Override
    public void execute(CastleResultContext context) {
        // TODO: 2018/2/1 阿分题转移了，这个待处理
        if (asyncAfentiCacheService.SelfLearningActionCacheManager_sended(context.getStudent().getId()).take()) return;
        actionServiceClient.finishSelfLearning(context.getStudent().getId());
    }
}
