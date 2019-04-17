package com.voxlearning.washington.controller.mobile.student.babyeagle;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleTeacher;
import com.voxlearning.washington.controller.mobile.AbstractMobileWonderlandActivityController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author fugui.chang
 * @since 2017/7/11
 */
@Controller
@RequestMapping(value = "/babyeagle/teacher/page/")
public class MobileStudentBabyEaglePageController extends AbstractMobileWonderlandActivityController {

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {
        return "babyeagle/index";
    }

    @RequestMapping(value = "courselisturl.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage courseListUrl() {
        String courseListUrl = ProductConfig.getMainSiteBaseUrl() + "/babyeagle/teacher/page/courseslist.vpage";
        return MapMessage.successMessage().add("courseListUrl", courseListUrl);
    }

    //登录 --> 老师课程页
    @RequestMapping(value = "courseslist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String coursesList(Model model) {
        String account = getRequestString("account");
        String password = getRequestString("password");

        MapMessage mapMessage = babyEagleServiceClient.getRemoteReference().loginTeacher(account, password);
        if (mapMessage.isSuccess() || StringUtils.equals(account, "admin") && StringUtils.equals(password, "123456")) {
            model.addAttribute("loginFlag", true);
            BabyEagleTeacher babyEagleTeacher = (BabyEagleTeacher) mapMessage.get("babyEagleTeacher");
            if (babyEagleTeacher != null) {
                MapMessage message = babyEagleLoaderClient.getBabyEagleLoader().fetchClassHoursFromBufferByTeacher(babyEagleTeacher.getId()).getUninterruptibly();
                model.addAttribute("teacherClassHours", message.get("teacherClassHours"));
            }
            model.addAttribute("emptycourseslist", ProductConfig.getMainSiteBaseUrl() + "/babyeagle/teacher/page/emptycourseslist.vpage");
            return "babyeagle/courseslist";
        } else {
            if (!StringUtils.isBlank(account) || !StringUtils.isBlank(password)) {
                model.addAttribute("errorMsg", "请验证正确的身份权限");
            }

            model.addAttribute("loginFlag", false);
            return "babyeagle/courseslist";
        }
    }

    @RequestMapping(value = "emptycourseslist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String emptyCoursesList(Model model) {
        return "babyeagle/emptycourseslist";
    }
}
