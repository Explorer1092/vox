package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfIndexContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT2MASTER;
import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT2SPENDING;

/**
 * @author songtao
 * @since 2017/09/21
 */
@Named
public class FEI_CountRescued extends SpringContainerSupport implements IAfentiTask<FetchElfIndexContext> {

    @Override
    public void execute(FetchElfIndexContext context) {

        List<WrongQuestionLibrary> questions = new ArrayList<>();

        questions.addAll(context.getQuestions()
                .getOrDefault(INCORRECT2MASTER, new LinkedList<>())
                .stream()
                .filter(l -> StringUtils.isNotBlank(l.getSeid()))
                .collect(Collectors.toList()));

        questions.addAll(context.getQuestions()
                .getOrDefault(INCORRECT2SPENDING, new LinkedList<>())
                .stream()
                .filter(l -> StringUtils.isNotBlank(l.getSeid()))
                .collect(Collectors.toList()));

        int size = questions.size();
        if (context.getLimited() != null && context.getLimited()) {//语文还在用老的接口这里做兼容
            size = Math.min(300, size);
        }
        context.setRescued(size);
    }
}