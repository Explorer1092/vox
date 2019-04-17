package com.voxlearning.utopia.service.parent.homework.impl.template.submit;

import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultService;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 存储作业结果
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
@Named
public class HomeworkResultStoreProcessor implements HomeworkProcessor {

    //Local variables
    @Inject private HomeworkResultService homeworkResultService;

    //Logic
    /**
     * 存储作业结果
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        HomeworkResult hr = hc.getHomeworkResult();
        //开始和最后一次保存结果
        if(hr.getDoQuestionCount() == 1 || hr.getFinished()){
            homeworkResultService.saveHomeworkResult(hr);
        }
    }

}
