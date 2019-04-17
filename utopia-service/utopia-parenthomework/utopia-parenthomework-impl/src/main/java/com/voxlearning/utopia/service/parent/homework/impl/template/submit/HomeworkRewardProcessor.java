package com.voxlearning.utopia.service.parent.homework.impl.template.submit;

import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkRewardService;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 作业奖励
 *
 * @author Wenlong Meng
 * @since Feb 21, 2019
 */
@Named
@Slf4j
public class HomeworkRewardProcessor implements HomeworkProcessor {

    //Local variables
    @Inject private HomeworkRewardService homeworkRewardService;

    //Logic
    /**
     * 奖励
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        HomeworkResult homeworkResult = hc.getHomeworkResult();
        //已完成
        if(hc.getGroupId() != null && ObjectUtils.get(()->homeworkResult.getFinished(), Boolean.FALSE)){
            homeworkRewardService.reward(homeworkResult, hc.getStudentInfo());
        }
    }

}
