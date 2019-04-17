package com.voxlearning.utopia.service.parent.homework.impl.template.correct.processor;

import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultService;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 存储作业结果
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Named
public class StoreSubmitCorrectProcessor implements IProcessor<CorrectContext> {

    //Local variables
    @Inject private HomeworkResultService homeworkResultService;

    //Logic
    /**
     * 存储作业结果
     *
     * @param c args
     * @return result
     */
    public void process(CorrectContext c) {
        HomeworkResult hr = c.getHomeworkResult();
        //开始和最后一次保存结果
        if(hr.getFinished()){
            homeworkResultService.saveHomeworkResult(hr);
        }
        //保存结果详情
        List<HomeworkProcessResult> homeworkProcessResults = c.getHomeworkProcessResults();
        homeworkResultService.saveHomeworkProcessResult(homeworkProcessResults);
    }

}
