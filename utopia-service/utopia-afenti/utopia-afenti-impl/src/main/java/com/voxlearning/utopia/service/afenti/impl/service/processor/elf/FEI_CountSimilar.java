package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfIndexContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT2CORRECT;

/**
 * @author songtao
 * @since 2017/9/20
 */
@Named
public class FEI_CountSimilar extends SpringContainerSupport implements IAfentiTask<FetchElfIndexContext> {
    @Inject private QuestionLoaderClient questionLoaderClient;

    @Override
    public void execute(FetchElfIndexContext context) {

        // 获取类题存在的并按照时间倒序排列
        List<WrongQuestionLibrary> questions = context.getQuestions().getOrDefault(INCORRECT2CORRECT, new LinkedList<>())
                .stream()
                .filter(l -> StringUtils.isNotBlank(l.getSeid()))
                .filter(l -> !StringUtils.equals(l.getSeid(), UtopiaAfentiConstants.NO_SIMILAR_QUESTION))
                .collect(Collectors.toList());
        int size = questions.size();
        if (context.getLimited() != null && context.getLimited()) {//语文还在用老的接口这里做兼容
            size = Math.min(300, size);
        }
        context.setSimilar(size);
    }
}
