package com.voxlearning.utopia.service.parent.homework.impl.template.submit.batch;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkPractice;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 初始化上下文
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
@Named
@SubType({
        ObjectiveConfigType.MENTAL_ARITHMETIC
})
public class HomeworkResultBatchInitContextProcessor implements HomeworkProcessor {

    //Local variables
    @Inject private HomeworkLoader homeworkLoader;

    //Logic
    /**
     * 初始化上下文
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        String homeworkId = param.getHomeworkId();

        HomeworkResult hr = JsonUtils.safeConvertMapToObject(param.getData(), HomeworkResult.class);
        List<HomeworkProcessResult> homeworkProcessResults = new ArrayList<>();
        ((List)(param.getData().get("studentHomeworkAnswers"))).stream().forEach(f->
                {
                    Map<String, Object> m = (Map)f;
                    HomeworkProcessResult hpr = new HomeworkProcessResult();
                    hpr.setQuestionId((String)m.get("questionId"));
                    hpr.setUserAnswers((List)m.get("answer"));
//                    hpr.setAnswers((List)m.get("answer"));
                    hpr.setDuration(SafeConverter.toLong(m.get("durationMilliseconds"), 0));
                    homeworkProcessResults.add(hpr);
                }
        );

        hc.setHomeworkProcessResults(homeworkProcessResults);
        //作业
        Homework homework = homeworkLoader.loadHomework(homeworkId);

        //查询题目信息
        HomeworkPractice homeworkPractice = homeworkLoader.loadHomeworkPractice(homeworkId);

        hc.setHomework(homework);
        hc.setHomeworkResult(hr);
        hc.setHomeworkPractice(homeworkPractice);
    }

}
