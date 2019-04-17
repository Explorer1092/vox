package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiType;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.dao.WrongQuestionLibraryDao;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiElfLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author songtao
 * @since 2017/09/22
 */
@Named
public class FE_ClassifyData extends SpringContainerSupport implements IAfentiTask<FetchElfContext> {

    @Inject
    private AfentiElfLoaderImpl afentiElfLoader;

    @Inject
    private WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @Override
    public void execute(FetchElfContext context) {
        List<WrongQuestionLibrary> questions = context.getQuestionList();
        Collections.sort(questions, (o1, o2) -> {
            long ts1 = o1.getUpdateAt() == null ? 0L : o1.getUpdateAt().getTime();
            long ts2 = o2.getUpdateAt() == null ? 0L : o2.getUpdateAt().getTime();
            return Long.compare(ts2, ts1);
        });

        switch (context.getStateType()) {
            case similar:
                // 修改删除或者升级的题目
                List<String> allDocIds = new ArrayList<>();
                questions.forEach(e -> {
                    allDocIds.add(StringUtils.substringBefore(e.getSeid(), "-"));
                });

                Map<String, NewQuestion> allQs = afentiElfLoader.loadQuestionsByDocIds(allDocIds);
                for (WrongQuestionLibrary question : questions) {
                    String docId = StringUtils.substringBefore(question.getSeid(), "-");
                    if (allQs.get(docId) == null) {
                        continue;
                    }
                    NewQuestion newQuestion = allQs.get(docId);
                    if (!StringUtils.equals(question.getSeid(), newQuestion.getId())) {
                        // 更新类题
                        wrongQuestionLibraryDao.updateState(question.getId(), question.getState(), newQuestion.getId());
                    }

                    Map<String, String> map = new HashMap<>();
                    map.put("qid", question.getEid());
                    map.put("sqid", newQuestion.getId());
                    context.getSimilar().add(map);
                }

                break;
            case incorrect:
                Map<String, List<String>> map = new HashMap<>();
                map.put("castle", new LinkedList<>());
                map.put("homework", new LinkedList<>());

                for (WrongQuestionLibrary question : questions) {
                    if (StringUtils.equals(question.getSource(), AfentiType.学习城堡.name())) {
                        map.get("castle").add(question.getEid());
                    } else if (StringUtils.equals(question.getSource(), StudyType.homework.name())) {
                        map.get("homework").add(question.getEid());
                    }
                }
                context.getIncorrect().putAll(map);
                break;
            case rescued:
                questions.forEach(e -> {
                    context.getRescued().add(e.getEid());
                });
            default:
                break;
        }
    }

}
