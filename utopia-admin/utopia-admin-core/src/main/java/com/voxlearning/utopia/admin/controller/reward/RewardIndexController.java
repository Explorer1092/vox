package com.voxlearning.utopia.admin.controller.reward;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * Created by XiaoPeng.Yang on 14-7-16.
 */
@Controller
@Slf4j
@RequestMapping(value = "/reward")
public class RewardIndexController extends RewardAbstractController {

    @RequestMapping(value = "rewardindex.vpage", method = RequestMethod.GET)
    public String couponIndex(Model model) {
        return "reward/index";
    }

    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "leftmenu.vpage", method = RequestMethod.GET)
    public String leftmenu() {
        return "reward/leftmenu";
    }


}
