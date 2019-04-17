package com.voxlearning.utopia.admin.controller.junior;

import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * Created by alex on 2018/6/8.
 */
@Controller
@RequestMapping("/junior")
public class CrmJuniorIndexController extends AbstractAdminSystemController {

    @RequestMapping(value = "juniorindex.vpage", method = RequestMethod.GET)
    public String index() {
        return "junior/juniorindex";
    }

    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "leftmenu.vpage", method = RequestMethod.GET)
    public String leftmenu() {
        return "junior/leftmenu";
    }

}
