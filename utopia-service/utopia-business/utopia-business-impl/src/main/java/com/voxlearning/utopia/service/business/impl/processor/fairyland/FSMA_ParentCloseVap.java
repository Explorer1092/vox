package com.voxlearning.utopia.service.business.impl.processor.fairyland;

import com.voxlearning.utopia.service.business.impl.processor.AbstractExecuteTask;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 家长关闭了增值应用，和黑名单一样，如果关了就什么应用都看不到
 *
 * @author Ruib
 * @since 2019/1/2
 */
@Named
public class FSMA_ParentCloseVap extends AbstractExecuteTask<FetchStudentAppContext> {

    @Inject private StudentLoaderClient client;

    @Override
    public void execute(FetchStudentAppContext context) {

        if (context.isWhite()) return;

        StudentExtAttribute ext = client.loadStudentExtAttribute(context.getStudentId());
        if (ext != null && ext.vapClosed()) context.terminateTask();
    }
}
