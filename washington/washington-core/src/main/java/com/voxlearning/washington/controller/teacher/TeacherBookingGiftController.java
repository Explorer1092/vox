package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.mobile.teacher.AbstractMobileTeacherController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by jiangpeng on 16/9/9.
 */
@Controller
@RequestMapping(value = "/teacher/gz/gift")
@Slf4j
public class TeacherBookingGiftController extends AbstractMobileTeacherController {



    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET})
    public String index() {
        if (isMobileRequest(getRequest())) {
            return "teacherv3/activity/ctepackage/index_app";
        }

        return "teacherv3/activity/ctepackage/index_pc";

    }


    @RequestMapping(value = "/book.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage book() {
        return MapMessage.errorMessage("活动已结束,请参加其他活动。");
    }



}
