package com.voxlearning.washington.controller.open.v1.util;

import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class InternalOffRewardService {

    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;

    public boolean offline(TeacherDetail teacherDetail) {
        if (teacherDetail == null) {
            return false;
        }

        boolean timeFlag = getTimeFlag();
        boolean offline = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "Reward", "CloseSchool");
        return timeFlag && offline;
    }

    public boolean offline(StudentDetail studentDetail) {
        if (studentDetail == null) {
            return false;
        }

        boolean timeFlag = getTimeFlag();
        boolean offline = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "Reward", "CloseSchool");
        return timeFlag && offline;
    }

    private boolean getTimeFlag() {
        boolean timeFlag = true;  // 是否满足时间条件

        long now = System.currentTimeMillis();
        if (RuntimeMode.isProduction()) {
            timeFlag = now >= 1553486400000L; // 2019/3/25 12:00:00
        } else if (RuntimeMode.isStaging()) {
            timeFlag = now >= 1553184000000L; // 2019/3/22 00:00:00
        }
        return timeFlag;
    }
}
