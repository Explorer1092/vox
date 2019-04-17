package com.voxlearning.utopia.service.business.impl.processor.fairyland;

import com.voxlearning.utopia.service.business.impl.processor.AbstractExecuteTask;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 简单校验
 *
 * @author Ruib
 * @since 2019/1/2
 */
@Named
public class FSMA_Validate extends AbstractExecuteTask<FetchStudentAppContext> {

    @Inject private StudentLoaderClient client;

    @Override
    public void execute(FetchStudentAppContext context) {
        StudentDetail student = client.loadStudentDetail(context.getStudentId());

        if (student == null || student.getClazz() == null || (!student.isPrimaryStudent() && !student.isInfantStudent())) {
            context.terminateTask();
            return;
        }

        context.setStudent(student);
    }
}
