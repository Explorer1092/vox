package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/27
 */
@Named
public class CH_LoadStudents extends SpringContainerSupport implements CheckHomeworkTask {
    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public void execute(CheckHomeworkContext context) {
        List<StudentDetail> students = new ArrayList<>();
        List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(context.getGroupId());
        Map<Long, StudentDetail> studentDetailMap =  studentLoaderClient.loadStudentDetails(studentIds);
        if(!studentDetailMap.isEmpty()){
            students.addAll(studentDetailMap.values());
        }
        context.setStudents(students);
    }
}
