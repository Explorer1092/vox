package com.voxlearning.utopia.service.parent.homework.impl.template.submit.single;

import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 统计结果信息
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-21
 */
@Named
public class HomeworkResultStatisticsProcessor implements HomeworkProcessor {

    //Local variables
    @Inject private HomeworkResultLoader homeworkResultLoader;

    //Logic
    /**
     * 判题算分
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        HomeworkResult hr = hc.getHomeworkResult();
        hr.setDoQuestionCount(HomeworkUtil.sum(hr.getDoQuestionCount(), 1));
        if(hr.getFinished()){//完成则统计错题数、得分
            List<HomeworkProcessResult> hprs = homeworkResultLoader.loadHomeworkProcessResults(hr.getId());
            hr.setErrorQuestionCount((int)hprs.stream().filter(e->!e.getRight()).count());
            hr.setUserScore(hprs.stream().mapToDouble(HomeworkProcessResult::getUserScore).sum());
            hr.setUserDuration(hprs.stream().mapToLong(HomeworkProcessResult::getDuration).sum());
            hr.setDoQuestionCount(hprs.size());
            hr.setScoreLevel(HomeworkUtil.score2Level(hr.getScore()).name());
        }

    }



}
