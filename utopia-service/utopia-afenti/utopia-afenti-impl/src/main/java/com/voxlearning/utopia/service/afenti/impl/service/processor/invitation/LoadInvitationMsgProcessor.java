package com.voxlearning.utopia.service.afenti.impl.service.processor.invitation;

import com.voxlearning.utopia.service.afenti.api.context.LoadInvitationMsgContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author peng.zhang.a
 * @since 16-7-19
 */
@Named
@AfentiTasks({
        LIM_LoadBaseMessage.class,
        LIM_SplitUserList.class,
        LIM_LoadClassmateMessage.class,
        LIM_GenerateUsingUserList.class,
        LIM_GenerateUnUsedUserList.class
})
public class LoadInvitationMsgProcessor extends AbstractAfentiProcessor<LoadInvitationMsgContext> {

}
