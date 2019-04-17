package com.voxlearning.utopia.service.afenti.impl.service.processor.login;

import com.voxlearning.utopia.service.afenti.api.context.LoginContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/11
 */
@Named
@AfentiTasks({
        L_InitIfNecessary.class,
        L_LoadUserBasicInfo.class,
        L_LoadAfentiBook.class,
        L_LoadAfentiStar.class,
        L_LoadAfentiOrder.class,
        L_LoadPopup.class,
        L_LoadConditions.class,
        L_NotifyLoginAchievement.class,
})
public class LoginProcessor extends AbstractAfentiProcessor<LoginContext> {
}
