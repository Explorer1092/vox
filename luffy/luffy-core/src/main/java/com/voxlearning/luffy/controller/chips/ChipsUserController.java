package com.voxlearning.luffy.controller.chips;


import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.luffy.aggregate.ChipsWechatUserAggregate;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserService;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.data.ChipsWechatUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@Slf4j
@RequestMapping(value = "/chips/user")
public class ChipsUserController extends AbstractChipsController {

    @Inject
    private ChipsWechatUserAggregate chipsWechatUserService;

    @ImportService(interfaceClass = ChipsWechatUserService.class)
    private ChipsWechatUserService wechatUserService;

    @RequestMapping(value = "auth.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage auth() {
        String code = getRequestString("code");
        if (StringUtils.isBlank(code)) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }
        return wrapper(mm -> mm.putAll(AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("chipsWechatUserService.loginAndRegister")
                .keys(code)
                .callback(() -> chipsWechatUserService.loginAndRegister(code))
                .build()
                .execute()));
    }

    @RequestMapping(value = "updateinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateInfo() {
        String nickName = getRequestString("nickName");
        String avatarUrl = getRequestString("avatarUrl");

        if (StringUtils.isAnyBlank(nickName, avatarUrl)) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }
        ChipsWechatUser wechatUser = getWechatUser();
        return wrapper(mm -> mm.putAll(AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("chipsWechatUserService.updateUserInfo")
                .keys(wechatUser.getWechatUserId())
                .callback(() -> wechatUserService.updateUserInfo(wechatUser.getWechatUserId(), nickName, avatarUrl))
                .build()
                .execute()));
    }

}
