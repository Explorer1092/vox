package com.voxlearning.utopia.service.afenti.impl.service.processor.login;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.LoginContext;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 获取阿分题首页星星数量
 *
 * @author Ruib
 * @since 2016/7/12
 */
@Named
public class L_LoadAfentiStar extends SpringContainerSupport implements IAfentiTask<LoginContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(LoginContext context) {
        context.getResult().put("star", afentiLoader.loadUserTotalStar(context.getStudent().getId(), context.getSubject()));
    }
}
