package com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.core.utils.MQUtils;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;

import javax.inject.Named;

/**
 * 作业app事件消息
 *
 * @author Wenlong Meng
 * @since Feb, 22, 2019
 */
@Named
public class HomeworkAppEventMQProcessor implements HomeworkProcessor {

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();

        //发送事件通知
        MQUtils.send("platform.queue.parent.homework.app.use.topic", MapUtils.m("messageType", "use",
                "userId", param.getStudentId(),
                "op","questionPackage",
                "bizType",param.getBizType()));
    }
}
