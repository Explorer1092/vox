package com.voxlearning.utopia.service.parent.homework.impl.template.submit.batch;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;

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
public class HomeworkResultBatchStatisticsProcessor implements HomeworkProcessor {

    //Local variables
    @Inject private HomeworkLoader homeworkLoader;

    //Logic
    /**
     * 判题算分
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        HomeworkResult hr = hc.getHomeworkResult();
        List<HomeworkProcessResult> hprs = hc.getHomeworkProcessResults();
        hr.setDoQuestionCount(hprs.size());
        if(CollectionUtils.isEmpty(hprs.get(hprs.size() - 1).getUserAnswers())){
            Integer timelimit = hr.getTimeLimit();
            if(timelimit != null && timelimit>0){
                hr.setUserDuration(timelimit * 60 * 1000L);
                return;
            }
        }
        hr.setUserDuration(hprs.stream().mapToLong(HomeworkProcessResult::getDuration).sum());

    }



}
