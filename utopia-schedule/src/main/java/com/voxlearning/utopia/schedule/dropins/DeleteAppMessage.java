package com.voxlearning.utopia.schedule.dropins;

import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.schedule.support.AbstractSweeperTask;
import com.voxlearning.utopia.schedule.support.SweeperTask;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;

import java.util.Map;

/**
 * @author xinxin
 * @since 2/4/17.
 */
@SweeperTask
public class DeleteAppMessage extends AbstractSweeperTask {
    @Override
    public void execute(Map<String, Object> beans) {
        //测试环境QA需要跑些自动化测试的需求。所以AppMessage在测试环境就不删除历史数据了。
        //线上环境肯定是正常运行的。
        if (!RuntimeMode.isTest()) {
            MessageCommandServiceClient messageCommandServiceClient = applicationContext.getBean(MessageCommandServiceClient.class);
            messageCommandServiceClient.getMessageCommandService().cleanupAppMessages();
        }
    }
}
