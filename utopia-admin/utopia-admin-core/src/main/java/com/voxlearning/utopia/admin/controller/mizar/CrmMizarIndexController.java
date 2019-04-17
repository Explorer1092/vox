package com.voxlearning.utopia.admin.controller.mizar;

import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/mizar")
public class CrmMizarIndexController extends CrmMizarAbstractController {

    @RequestMapping(value = "mizarindex.vpage", method = RequestMethod.GET)
    public String index() {
        return "mizar/mizarindex";
    }

    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "leftmenu.vpage", method = RequestMethod.GET)
    public String leftmenu() {
        return "mizar/leftmenu";
    }

}
