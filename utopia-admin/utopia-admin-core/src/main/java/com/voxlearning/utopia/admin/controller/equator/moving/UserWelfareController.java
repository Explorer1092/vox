package com.voxlearning.utopia.admin.controller.equator.moving;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import com.voxlearning.utopia.service.wonderland.api.entity.welfare.UserWelfareInfo;
import com.voxlearning.utopia.service.wonderland.client.WonderlandLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.WonderlandServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

/**
 * 老增值业务迁移
 *
 * @author lei.liu
 * @version 18-10-22
 */
@Controller
@RequestMapping("/equator/userwelfare")
public class UserWelfareController extends AbstractEquatorController {

    @Inject private WonderlandLoaderClient wonderlandLoaderClient;
    @Inject private WonderlandServiceClient wonderlandServiceClient;

    /**
     * 用户福利券
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String userWelfare(Model model) {
        Long studentId = getRequestLong("studentId");
        model.addAttribute("studentId", studentId);
        if (studentId.equals(Long.valueOf("0"))) {
            model.addAttribute("errMsg", "请您在上方输入studentid！");
            return "equator/welfare/index";
        }

        List<UserWelfareInfo> userWelfareList = wonderlandLoaderClient.getWonderlandLoader().loadUserWelfareList(studentId);
        model.addAttribute("userWelfareList", userWelfareList);

        return "equator/welfare/index";
    }

    /**
     * 删除用户的福利券
     */
    @RequestMapping(value = "remove.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removewelfare() {
        Long studentId = getRequestLong("studentId");
        String welfareId = getRequestString("welfareId");
        return wonderlandServiceClient.getWonderlandService().removeUserWelfare(welfareId, studentId);
    }
}
