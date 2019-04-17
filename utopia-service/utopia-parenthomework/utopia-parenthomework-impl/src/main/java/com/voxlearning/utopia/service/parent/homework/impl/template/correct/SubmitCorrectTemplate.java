package com.voxlearning.utopia.service.parent.homework.impl.template.correct;

import com.voxlearning.utopia.service.parent.homework.api.model.Command;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.SupportCommand;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectBaseTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.correct.processor.*;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * 提交作业接口
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Named
@Slf4j
@SupportCommand(Command.SUBMIT)
@Processors({
        InitSubmitCorrectProcessor.class,
        ScoreSubmitCorrectProcessor.class,
        StatisticsSubmitCorrectProcessor.class,
        StoreSubmitCorrectProcessor.class,
        MapMessageSubmitCorrectProcessor.class
})
public class SubmitCorrectTemplate extends CorrectBaseTemplate {

}
