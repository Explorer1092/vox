package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/crm")
public class CrmIndexController extends CrmAbstractController {

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("adminName", getCurrentAdminUser().getAdminUserName());
        return "crm/index";
    }

    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "leftmenu.vpage", method = RequestMethod.GET)
    public String leftmenu() {
        return "crm/leftmenu";
    }

}

