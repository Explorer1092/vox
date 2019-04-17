package com.voxlearning.utopia.admin.controller.equator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author fugui.chang
 * @since 2018/6/20.
 */
@Controller
@RequestMapping("equator")
@Slf4j
public class EquatorAdminController extends AbstractEquatorController {
    //首页
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET})
    public String fairylandIndex(Model model) {
        return "equator/index";
    }
}
