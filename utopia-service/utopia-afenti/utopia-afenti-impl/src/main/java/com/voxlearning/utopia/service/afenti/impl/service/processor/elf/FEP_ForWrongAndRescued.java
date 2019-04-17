package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionStateType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiType;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfPageContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.dao.WrongQuestionLibraryDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT2MASTER;
import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT2SPENDING;

/**
 * Created by Summer on 2017/10/31.
 */
@Named
public class FEP_ForWrongAndRescued extends SpringContainerSupport implements IAfentiTask<FetchElfPageContext> {
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @Override
    public void execute(FetchElfPageContext context) {
        if (context.getStateType() == AfentiWrongQuestionStateType.similar || CollectionUtils.isEmpty(context.getLibraryList())) {
            return;
        }
        List<WrongQuestionLibrary> libraries = context.getLibraryList();
        if (context.getStateType() == AfentiWrongQuestionStateType.incorrect) {
            Map<String, List<WrongQuestionLibrary>> libraryMap = libraries.stream().filter(e -> e.getState() == AfentiState.INCORRECT)
                    .collect(Collectors.groupingBy(WrongQuestionLibrary::getSource));
            List<WrongQuestionLibrary> temp = null;
            switch (context.getSource()) {
                case homework:
                    libraries = libraryMap.get(StudyType.homework.name());
                    temp = libraryMap.get(AfentiType.学习城堡.name());
                    if (temp != null) {
                        context.setAfentiNum(temp.size());
                    }
                    break;
                case castle:
                    libraries = libraryMap.get(AfentiType.学习城堡.name());
                    temp = libraryMap.get(StudyType.homework.name());
                    if (temp != null) {
                        context.setHomeworkNum(temp.size());
                    }
                    break;
                default:
                    break;
            }
        } else if (context.getStateType() == AfentiWrongQuestionStateType.rescued) {
            libraries = libraries.stream()
                    .filter(e -> e.getState() == INCORRECT2MASTER || e.getState() == INCORRECT2SPENDING)
                    .filter(e -> StringUtils.isNotBlank(e.getSeid()))
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(libraries)) {
            return;
        }

        int index = (context.getPage() - 1) * context.getPageSize();
        if (index < libraries.size()) {
            int subCount = 0;
            Map<String, WrongQuestionLibrary> questionMap = libraries.stream().collect(Collectors.toMap(WrongQuestionLibrary::getId, v -> v));
            List<WrongQuestionLibrary> resultContent = new ArrayList<>();
            while (true) {
                //通过docId查询题目
                List<String> allDocIds = new ArrayList<>();
                List<WrongQuestionLibrary> questionSubList = libraries.subList(index, Math.min(index + context.getPageSize(), libraries.size()));
                questionSubList.forEach(e -> {
                    allDocIds.add(StringUtils.substringBefore(e.getEid(), "-"));
                });
                Map<String, NewQuestion> allQs = questionLoaderClient.loadQuestionByDocIds0(allDocIds);
                for (WrongQuestionLibrary question : questionSubList) {
                    index++;
                    //检查allQs中有没有
                    String docId = StringUtils.substringBefore(question.getEid(), "-");
                    NewQuestion newQuestion = allQs.get(docId);
                    if (newQuestion == null) {//没有需要disable
                        context.getDisableIds().add(question.getId());
                        subCount++;
                        continue;
                    }
                    // 比较最新的题目ID和当前题目ID是否一致
                    String newId = WrongQuestionLibrary.generateId(context.getStudentId(), context.getSubject(), newQuestion.getId());
                    if (!StringUtils.equals(newId, question.getId())) {
                        context.getDisableIds().add(question.getId());
                        subCount++;
                        WrongQuestionLibrary history = questionMap.get(newId);
                        if (history != null) {//questionMap有的情况下只disable
                            continue;
                        }
                        //没有的情况下需要upsert
                        WrongQuestionLibrary questionLibrary = new WrongQuestionLibrary();
                        questionLibrary.setId(newId);
                        questionLibrary.setEid(newQuestion.getId());
                        questionLibrary.setUserId(context.getStudentId());
                        questionLibrary.setCreateAt(question.getCreateAt());
                        questionLibrary.setUpdateAt(question.getUpdateAt());
                        questionLibrary.setDisabled(false);
                        questionLibrary.setSource(question.getSource());
                        questionLibrary.setState(question.getState());
                        questionLibrary.setSubject(question.getSubject());
                        wrongQuestionLibraryDao.upsert(questionLibrary);
                        question = questionLibrary;
                        subCount--;
                    }
                    resultContent.add(question);
                }

                if (resultContent.size() >= context.getPageSize() || index >= libraries.size()) {
                    break;
                }
            }

            context.setPageContent(resultContent.subList(0, Math.min(context.getPageSize(), resultContent.size())));
            context.setTotalNum(libraries.size() - subCount);
        } else {
            context.setTotalNum(libraries.size());
        }

        if (context.getStateType() == AfentiWrongQuestionStateType.incorrect) {
            switch (context.getSource()) {
                case homework:
                    context.setHomeworkNum(context.getTotalNum());
                    break;
                case castle:
                    context.setAfentiNum(context.getTotalNum());
                    break;
                default:
                    break;
            }
        }
    }
}
