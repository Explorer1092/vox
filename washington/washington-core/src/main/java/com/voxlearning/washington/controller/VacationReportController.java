package com.voxlearning.washington.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.teacher.AbstractTeacherController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/container/vacation/report")
public class VacationReportController extends AbstractTeacherController {
    @Inject
    private VacationHomeworkReportLoaderClient vacationHomeworkReportLoaderClient;

    /**
     * 一份假期作业每个包信息展示
     */
    @RequestMapping(value = "packagereport.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage packageReport() {
        String packageId = this.getRequestString("packageId");
        if (StringUtils.isBlank(packageId)) {
            logger.error("packageId is blank");
            return MapMessage.errorMessage("packageId is blank").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        User user = currentUser();
        Long sid = getRequestLong("sid");
        Boolean fromJzt = getRequestBool("fromjzt");

        try {
            return vacationHomeworkReportLoaderClient.packageReport(packageId,user,sid, fromJzt);
        } catch (Exception e) {
            logger.error("fetch package report failed :packageId of {} ", packageId, e);
            return MapMessage.errorMessage();
        }
    }
}
