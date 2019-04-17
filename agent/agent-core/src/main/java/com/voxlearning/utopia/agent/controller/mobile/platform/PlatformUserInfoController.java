package com.voxlearning.utopia.agent.controller.mobile.platform;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.platform.AgentPlatformUserInfoService;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/mobile/platform/user")
public class PlatformUserInfoController extends AbstractAgentController {

    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private AgentPlatformUserInfoService platformUserInfoService;


    @ResponseBody
    @RequestMapping("mobile.vpage")
    public MapMessage getUserMobile(){
        Long userId = getRequestLong("userId");

        AuthCurrentUser user = getCurrentUser();
        String mobile = sensitiveUserDataServiceClient.showUserMobile(userId, "天玑查看手机号", user.getRealName() + "(ID:" + user.getUserId() + ")");
        return MapMessage.successMessage().add("mobile", mobile);
    }
}
