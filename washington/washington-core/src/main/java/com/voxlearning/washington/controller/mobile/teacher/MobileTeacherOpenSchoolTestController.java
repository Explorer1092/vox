package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.OpenSchoolTestService;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/teacherMobile/open_school_activity/")
@Slf4j
public class MobileTeacherOpenSchoolTestController extends AbstractMobileTeacherController {

    private static final MapMessage NO_LOGIN_MSG = MapMessage.errorMessage("未登录").add("code", 201);

    @ImportService(interfaceClass = OpenSchoolTestService.class)
    private OpenSchoolTestService openSchoolTestService;

    @RequestMapping(value = "index.vpage")
    @ResponseBody
    public MapMessage index() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }
        return openSchoolTestService.index(user.getId());
    }

}


