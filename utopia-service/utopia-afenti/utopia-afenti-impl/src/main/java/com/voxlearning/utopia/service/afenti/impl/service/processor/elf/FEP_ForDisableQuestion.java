package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfPageContext;
import com.voxlearning.utopia.service.afenti.impl.dao.WrongQuestionLibraryDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by songtao on 2017/10/31.
 *
 */
@Named
public class FEP_ForDisableQuestion extends SpringContainerSupport implements IAfentiTask<FetchElfPageContext> {

    @Inject private WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @Override
    public void execute(FetchElfPageContext context) {
        if (CollectionUtils.isNotEmpty(context.getDisableIds())) {
            List<String> disableIds = context.getDisableIds();
            int index = 0;
            int step = 50;
            do {
                wrongQuestionLibraryDao.disableLibrary(disableIds.subList(index, Math.min(index + step, disableIds.size())));
                index += step;
            } while(index < disableIds.size());
        }
    }
}
