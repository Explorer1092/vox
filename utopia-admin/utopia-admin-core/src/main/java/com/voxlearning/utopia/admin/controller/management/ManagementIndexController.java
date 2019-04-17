package com.voxlearning.utopia.admin.controller.management;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-4
 * Time: 下午2:35
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/management")
class ManagementIndexController extends ManagementAbstractController {

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        return "management/index";
    }
}
