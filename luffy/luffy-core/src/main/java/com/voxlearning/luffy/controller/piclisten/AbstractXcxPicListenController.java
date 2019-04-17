package com.voxlearning.luffy.controller.piclisten;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.luffy.controller.AbstractXcxController;
import com.voxlearning.luffy.controller.ApiConstants;
import com.voxlearning.luffy.exception.MiniProgramErrorException;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import com.voxlearning.utopia.service.wechat.api.entities.UserMiniProgramRef;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author jiangpeng
 * @since 2017-09-14 上午11:23
 **/
@Slf4j
public class AbstractXcxPicListenController extends AbstractXcxController {


    @ImportService(interfaceClass = ParentSelfStudyService.class)
    protected ParentSelfStudyService parentSelfStudyService;


    protected User currentParent() {
        User user = super.currentUserByUserType(UserType.PARENT);
        if (user == null && RuntimeMode.isDevelopment())
            return userLoaderClient.loadUser(getRequestLong("uid"));

        if (user != null && user.isParent())
            return user;
        else
            return null;
    }


    @Override
    public boolean onBeforeControllerMethod() {
        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            handleNeedLogin();
            return false;
        }
        User parent = currentParent();
        if (parent == null) {
            handleNeedLogin();
            return false;
        }
        UserMiniProgramRef userMiniProgramRef = wechatLoaderClient.loadMiniProgramUserRef(openId, MiniProgramType.PICLISTEN);
        if (userMiniProgramRef == null) {
            handleNeedLogin();
            return false;
        }
        return true;
    }

    private void handleNeedLogin() {
        try {
            getResponse().getWriter().write(JsonUtils.toJson(getNoLoginResult()));
        } catch (IOException e) {
            log.warn("onBeforeControllerMethod error: {}", e.getMessage());
        }
    }



    protected Long uid() {
        Long sid = getRequestContext().getId("sid");
        return sid;
    }

    protected Long pid() {
        User parent = currentParent();
        if (parent == null) {
            return null;
        }
        return parent.getId();
    }

    protected String gid() {

        String openId = getOpenId();

        if (!nb(openId)) {
            throw new MiniProgramErrorException(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE, "请重新登录");
        }

        String encryptedData = getRequestString("encryptedData").trim();
        String iv = getRequestString("iv").trim();

        if (!nb(encryptedData) || !nb(iv)) {
            throw new MiniProgramErrorException(ApiConstants.RES_RESULT_DECODE_FAILED_CODE, "未获取到群信息，请重新分享一下吧");
        }


        String sessionKey = getSessionKeyByOpenId(openId);
        String openGid = getDecryptData(iv, encryptedData, sessionKey, "openGId");
        if (!nb(openGid)) {
            throw new MiniProgramErrorException(ApiConstants.RES_RESULT_DECODE_FAILED_CODE, "未获取到群信息，请重新分享一下吧.");
        }

        return openGid;
    }


    protected String sys() {
        return "mini_program";
    }
}
