package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiElfLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 *
 * @author Ruib
 * @since 2016/7/24
 */
@Named
public class FE_LoadWrongQuestionLibrary extends SpringContainerSupport implements IAfentiTask<FetchElfContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject private AfentiElfLoaderImpl afentiElfLoader;

    @Override
    public void execute(FetchElfContext context) {

        List<WrongQuestionLibrary> realList = afentiElfLoader.loadAndUpdateWrongQuestionLibraryByUserIdAndSubject(context.getStudentId(), context.getSubject(), context.getStateType());

        context.setQuestionList(realList);

        asyncAfentiCacheService.AfentiPromptCacheManager_reset(context.getStudentId(), context.getSubject(), AfentiPromptType.elf).awaitUninterruptibly();
    }
}

