package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfIndexContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.LinkedList;

import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT;

/**
 * @author songtao
 * @since 2017/9/20
 */
@Named
public class FEI_CountIncorrect extends SpringContainerSupport implements IAfentiTask<FetchElfIndexContext> {

    @Override
    public void execute(FetchElfIndexContext context) {
        int incorrect = context.getQuestions().getOrDefault(INCORRECT, new LinkedList<>()).size();
        if (context.getLimited() != null && context.getLimited()) {//语文还在用老的接口这里做兼容
            incorrect = Math.min(300, incorrect);
        }
        context.setIncorrect(incorrect);
    }
}
