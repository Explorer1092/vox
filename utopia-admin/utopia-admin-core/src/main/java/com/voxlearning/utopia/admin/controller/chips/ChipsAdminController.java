package com.voxlearning.utopia.admin.controller.chips;

import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Summer on 2018/10/23
 */
@Controller
@RequestMapping("chips")
@Slf4j
public class ChipsAdminController extends AbstractAdminController {
    //首页
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET})
    public String chipsIndex(Model model) {
        return "chips/index";
    }
}
