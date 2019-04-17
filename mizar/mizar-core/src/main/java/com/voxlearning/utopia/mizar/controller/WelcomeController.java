package com.voxlearning.utopia.mizar.controller;

import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Mizar welcome controller
 * Created by Alex on 16/8/13.
 */
@Controller
@RequestMapping("/")
public class WelcomeController extends AbstractMizarController {

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String userId = currentUserId();
        if (userId == null) {
            return "index";
        }
        MizarAuthUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return "index";
        }
        if (currentUser.isMicroTeacher()) {
            return "redirect: /course/manage/index.vpage";
        }
        if (currentUser.isTangramJury()) {
            return "redirect: /activity/tangram/index.vpage";
        }
        long unreadCnt = mizarNotifyLoaderClient.loadUserAllNotify(currentUserId())
                .stream()
                .filter(n -> !n.isRead())
                .count();
        model.addAttribute("unreadCnt", unreadCnt);
        return "welcome";
    }
}
