package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/8/17
 */
@Named
public class CR_ActionTotalStarIncreased extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Inject private ActionServiceClient actionServiceClient;

    @Override
    public void execute(CastleResultContext context) {
        // TODO: 2018/2/1 阿分题转移了，这个待处理
        int currentStar = context.getStat() == null ? 0 : context.getStat().getStar();
        actionServiceClient.obtainStar(context.getStudent().getId(), context.getStar() - currentStar);
    }
}
