package com.voxlearning.utopia.service.parent.homework.impl.template.submit;

import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultService;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 存储作业结果
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
@Named
public class HomeworkProcessResultStoreProcessor implements HomeworkProcessor {

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
        //保存结果详情
        List<HomeworkProcessResult> homeworkProcessResults = hc.getHomeworkProcessResults();
        homeworkResultService.saveHomeworkProcessResult(homeworkProcessResults);
    }

}
