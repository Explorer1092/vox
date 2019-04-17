package com.voxlearning.washington.controller.mobile.parent.wechat;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.washington.controller.mobile.parent.AbstractMobileParentController;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

/**
 * @author shiwei.liao
 * @since 2018-7-9
 */
@Controller
@Slf4j
@RequestMapping(value = "/parentMobile/wechat")
public class MobileParentWechatController extends AbstractMobileParentController {


    @RequestMapping(value = "bind.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindWecaht() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage(ApiConstants.RES_RESULT_LOAD_USER_ERROR).setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        String unionId = getRequestString("union_id");
        String openId = getRequestString("open_id");
        if (StringUtils.isBlank(unionId) || StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("获取微信信息失败，请重试");
        }
        //微信绑定的用户
        User wechatHadBindUser = wechatLoaderClient.getWechatLoader().loadWechatUserByUnionId(unionId);
        if (wechatHadBindUser != null && !wechatHadBindUser.getId().equals(parent.getId())) {
            return MapMessage.errorMessage("该微信已绑定家长号，请退出后重新登录");
        }
        //用户绑定的微信
        List<UserWechatRef> parentHadBindWechat = wechatLoaderClient.getWechatLoader().loadUserWechatRefs(Collections.singleton(parent.getId()), WechatType.PARENT_APP).getOrDefault(parent.getId(), Collections.emptyList());
        if (CollectionUtils.isNotEmpty(parentHadBindWechat) && parentHadBindWechat.stream().noneMatch(p -> StringUtils.equals(p.getUnionId(), unionId))) {
            return MapMessage.errorMessage("该家长已绑定微信，请退出后重新登录");
        }
        String lock = "bindUserAndWechat_" + parent.getId();
        try {
            AtomicLockManager.getInstance().acquireLock(lock);
            return wechatServiceClient.getWechatService().bindUserAndWechat(parent.getId(), openId, unionId, "", WechatType.PARENT_APP.getType());
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("操作正在进行，请勿重复操作");
        } finally {
            AtomicLockManager.getInstance().releaseLock(lock);
        }
    }


    @RequestMapping(value = "unbind.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage unbindWechat() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage(ApiConstants.RES_RESULT_LOAD_USER_ERROR).setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        List<UserWechatRef> parentHadBindWechat = wechatLoaderClient.getWechatLoader().loadUserWechatRefs(Collections.singleton(parent.getId()), WechatType.PARENT_APP).getOrDefault(parent.getId(), Collections.emptyList());
        if (CollectionUtils.isEmpty(parentHadBindWechat)) {
            return MapMessage.errorMessage("该账号暂未绑定微信");
        }
        return wechatServiceClient.getWechatService().unbindUserAndWechatWithUserIdAndType(parent.getId(), WechatType.PARENT_APP.getType());
    }
}
