package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionStateType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfPageContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.dao.WrongQuestionLibraryDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT2CORRECT;

/**
 * Created by Summer on 2017/10/31.
 */
@Named
public class FEP_ForSimilar extends SpringContainerSupport implements IAfentiTask<FetchElfPageContext> {

    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @Override
    public void execute(FetchElfPageContext context) {
        if (context.getStateType() != AfentiWrongQuestionStateType.similar || CollectionUtils.isEmpty(context.getLibraryList())) {
            return;
        }

        List<WrongQuestionLibrary> libraries = context.getLibraryList().stream()
                .filter(e -> e.getState() == INCORRECT2CORRECT)
                .filter(e -> StringUtils.isNotBlank(e.getSeid()))
                .filter(e -> !StringUtils.equals(e.getSeid(), UtopiaAfentiConstants.NO_SIMILAR_QUESTION))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(libraries)) {
            return;
        }
        int index = (context.getPage() - 1) * context.getPageSize();
        if (index < libraries.size()) {
            int subCount = 0;
            List<WrongQuestionLibrary> resultContent = new ArrayList<>();
            while (true) {
                //通过docId查询题目
                List<String> allDocIds = new ArrayList<>();
                List<WrongQuestionLibrary> questionSubList = libraries.subList(index, Math.min(index + context.getPageSize(), libraries.size()));
                questionSubList.forEach(e -> {
                    allDocIds.add(StringUtils.substringBefore(e.getSeid(), "-"));
                });
                Map<String, NewQuestion> allQs = questionLoaderClient.loadQuestionByDocIds0(allDocIds);
                for (WrongQuestionLibrary question : questionSubList) {
                    index++;
                    //检查allQs中有没有
                    String docId = StringUtils.substringBefore(question.getSeid(), "-");
                    NewQuestion newQuestion = allQs.get(docId);
                    if (newQuestion == null) {
                        subCount--;
                        context.getDisableIds().add(question.getId());
                        continue;
                    }
                    // 比较最新的题目ID和当前题目ID是否一致
                    if (!StringUtils.equals(newQuestion.getId(), question.getSeid())) {
                        // 更新类题
                        wrongQuestionLibraryDao.updateState(question.getId(), question.getState(), newQuestion.getId());
                    }
                    resultContent.add(question);
                }
                if (resultContent.size() >= context.getPageSize() || index >= libraries.size()) {
                    break;
                }
            }
            // 设置总数
            context.setTotalNum(libraries.size() - subCount);
            context.setPageContent(resultContent.subList(0, Math.min(context.getPageSize(), resultContent.size())));
        } else {
            context.setTotalNum(libraries.size());
        }
    }
}
