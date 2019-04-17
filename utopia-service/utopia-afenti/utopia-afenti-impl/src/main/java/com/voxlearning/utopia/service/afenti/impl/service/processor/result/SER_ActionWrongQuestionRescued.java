package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/8/17
 */
@Named
public class SER_ActionWrongQuestionRescued extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {

    @Inject private ActionServiceClient actionServiceClient;

    @Override
    public void execute(ElfResultContext context) {
        actionServiceClient.correctWrongIssue(context.getStudent().getId(), 1);
    }
}
