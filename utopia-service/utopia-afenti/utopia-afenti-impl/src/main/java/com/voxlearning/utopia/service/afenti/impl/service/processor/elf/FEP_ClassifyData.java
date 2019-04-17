package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfPageContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author songtao
 * @since 2017/09/22
 */
@Named
public class FEP_ClassifyData extends SpringContainerSupport implements IAfentiTask<FetchElfPageContext> {

    @Override
    public void execute(FetchElfPageContext context) {
        List<WrongQuestionLibrary> questions = context.getPageContent();
        context.setPageNum(context.getPage());
        switch (context.getStateType()) {
            case similar:
                for (WrongQuestionLibrary question : questions) {
                    Map<String, String> map = new HashMap<>();
                    map.put("qid", question.getEid());
                    map.put("sqid", question.getSeid());
                    context.getResult().add(map);
                }
                break;
            case incorrect:
                for (WrongQuestionLibrary question : questions) {
                    Map<String, String> map = new HashMap<>();
                    map.put("qid", question.getEid());
                    context.getResult().add(map);
                }
                break;
            case rescued:
                for (WrongQuestionLibrary question : questions) {
                    Map<String, String> map = new HashMap<>();
                    map.put("qid", question.getEid());
                    context.getResult().add(map);
                }
            default:
                break;
        }
    }

}
