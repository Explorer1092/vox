package com.voxlearning.utopia.admin.controller.toolkit;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created with IntelliJ IDEA.
 * User: dell
 * Date: 13-6-3
 * Time: 下午5:44
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
@RequestMapping("/toolkit/pk")
@NoArgsConstructor
public class ToolKitPkController extends ToolKitAbstractController {
    @RequestMapping(value = "setVitality.vpage", method = RequestMethod.GET)
    String getVitality() {
        return "toolkit/toolkit";
    }

    /**
     * 设置活力值
     *
     * @param userId   学号
     * @param vitality 活力值
     */
    @RequestMapping(value = "setVitality.vpage", method = RequestMethod.POST)
    String setVitality(@RequestParam(value = "userId", required = false) String userId,
                       @RequestParam(value = "vitality", required = false) String vitality, Model model) {
        getAlertMessageManager().addMessageError("DISABLED");
        return "toolkit/toolkit";
    }


    @RequestMapping(value = "setAttack.vpage", method = RequestMethod.GET)
    String getAttack() {
        return "toolkit/toolkit";
    }

    /**
     * 设置攻击力
     *
     * @param userId 学号
     * @param attack 攻击力值
     */
    @RequestMapping(value = "setAttack.vpage", method = RequestMethod.POST)
    String setAttack(@RequestParam(value = "userId", required = false) String userId,
                     @RequestParam(value = "attack", required = false) String attack, Model model) {
        getAlertMessageManager().addMessageError("DISABLED");
        return "toolkit/toolkit";
    }


}
