package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/21
 */
@Named
public class CR_LastQuestion extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Override
    public void execute(CastleResultContext context) {
        if (!Boolean.TRUE.equals(context.getFinished())) context.terminateTask();
    }
}
