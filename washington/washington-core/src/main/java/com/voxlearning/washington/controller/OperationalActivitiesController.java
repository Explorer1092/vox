package com.voxlearning.washington.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.consumer.OperationalActivitiesLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/container/operational/activities")
public class OperationalActivitiesController extends AbstractController {
    @Inject private OperationalActivitiesLoaderClient operationalActivitiesLoaderClient;

    @ResponseBody
    @RequestMapping(value = "motherdayreport.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public MapMessage fetchMotherDayJztReport() {
        long studentId = this.getRequestLong("studentId");
        if (studentId == 0L) {
            return MapMessage.errorMessage("studentId 参数缺失");
        }
        String hid = this.getRequestString("hid");
        if (StringUtils.isBlank(hid)) {
            return MapMessage.errorMessage("hid 参数缺失");
        }
        User user = currentUser();
        return operationalActivitiesLoaderClient.fetchMotherDayJztReport(hid, studentId, user);
    }

    @ResponseBody
    @RequestMapping(value = "rewardstudent.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public MapMessage rewardStudent() {
        User user = currentUser();
        long studentId = this.getRequestLong("studentId");
        if (studentId == 0L) {
            return MapMessage.errorMessage("studentId 参数缺失");
        }
        String hid = this.getRequestString("hid");
        if (StringUtils.isBlank(hid)) {
            return MapMessage.errorMessage("hid 参数缺失");
        }
        return operationalActivitiesLoaderClient.rewardStudent(hid, studentId, user);
    }
}
