package com.voxlearning.utopia.service.parent.homework.impl.template.correct.processor;

import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectContext;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 统计结果信息
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Named
public class StatisticsSubmitCorrectProcessor implements IProcessor<CorrectContext> {

    //Local variables
    @Inject private HomeworkResultLoader homeworkResultLoader;

    //Logic
    /**
     * 统计
     *
     * @param c args
     * @return result
     */
    public void process(CorrectContext c) {
        HomeworkResult hr = c.getHomeworkResult();
        hr.setDoQuestionCount(HomeworkUtil.sum(hr.getDoQuestionCount(), 1));
        if(hr.getFinished()){//完成则统计错题数、得分
            List<HomeworkProcessResult> hprs = homeworkResultLoader.loadHomeworkProcessResults(hr.getId());
            hprs.addAll(c.getHomeworkProcessResults());
            hr.setErrorQuestionCount((int)hprs.stream().filter(e->!e.getRight()).count());
            hr.setUserScore(hprs.stream().mapToDouble(HomeworkProcessResult::getUserScore).sum());
            hr.setUserDuration(hprs.stream().mapToLong(HomeworkProcessResult::getDuration).sum());
            hr.setDoQuestionCount(hprs.size());
            hr.setScoreLevel(HomeworkUtil.score2Level(hr.getScore()).name());
        }
    }

}
