package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/site")
public class SiteIndexController extends SiteAbstractController {

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        return "site/index";
    }

    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "leftmenu.vpage", method = RequestMethod.GET)
    public String leftmenu() {
        return "site/leftmenu";
    }

    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "main.vpage", method = RequestMethod.GET)
    public String main() {
        return "site/main";
    }

}
