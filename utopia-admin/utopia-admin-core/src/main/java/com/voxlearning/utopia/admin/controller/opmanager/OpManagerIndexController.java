package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/opmanager")
public class OpManagerIndexController extends OpManagerAbstractController {

    @RequestMapping(value = "opindex.vpage", method = RequestMethod.GET)
    public String index() {
        return "opmanager/index";
    }

    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "leftmenu.vpage", method = RequestMethod.GET)
    public String leftmenu() {
        return "opmanager/leftmenu";
    }

}
