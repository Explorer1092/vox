package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfIndexContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiElfLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取精灵，类型要"homework"和"学习城堡"
 *
 * @author songtao
 * @since 2017/9/20
 */
@Named
public class FEI_LoadWrongQuestionLibrary extends SpringContainerSupport implements IAfentiTask<FetchElfIndexContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject private AfentiElfLoaderImpl afentiElfLoader;

    @Override
    public void execute(FetchElfIndexContext context) {

        List<WrongQuestionLibrary> realList = afentiElfLoader.loadWrongQuestionLibraryByUserIdAndSubject(context.getStudentId(), context.getSubject());

        Map<AfentiState, List<WrongQuestionLibrary>> map = realList.stream().collect(Collectors.groupingBy(WrongQuestionLibrary::getState));

        context.getQuestions().putAll(map);

        asyncAfentiCacheService.AfentiPromptCacheManager_reset(context.getStudentId(), context.getSubject(), AfentiPromptType.elf).awaitUninterruptibly();
    }

}
