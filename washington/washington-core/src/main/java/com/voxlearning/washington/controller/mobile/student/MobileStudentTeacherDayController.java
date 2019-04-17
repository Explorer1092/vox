package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.mobile.AbstractMobileTeacherDayController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by jiang wei on 2016/8/22.
 * 活动已下线 留着入口防止404
 */
@Controller
@RequestMapping("/studentMobile/teacherDay")
@NoArgsConstructor
@Slf4j
public class MobileStudentTeacherDayController extends AbstractMobileTeacherDayController {


    @RequestMapping(value = "/bless/index.vpage", method = {RequestMethod.GET})
    public String loadBlessByStudent(Model model) {
        return "studentmobilev3/logininvalid";
    }
    @RequestMapping(value = "/bless/send.vpage", method = {RequestMethod.GET})
    public String sendBlessIndex(Model model) {
        return "studentmobilev3/logininvalid";
    }
    @RequestMapping(value = "/bless/update.vpage", method = {RequestMethod.GET})
    public String update(Model model) {
        return "studentmobilev3/logininvalid";
    }


    @RequestMapping(value = "/bless/send.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage saveBless() {
        return MapMessage.errorMessage("活动已下线,请参加其他活动");
    }

    @RequestMapping(value = "/entry/banner.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage entryBanner() {
        return MapMessage.errorMessage("活动已下线,请参加其他活动");
    }

}
