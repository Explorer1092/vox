package com.voxlearning.wechat.controller.xcx;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author jiangpeng
 * @since 2017-09-14 上午10:49
 **/
@Controller
@RequestMapping(value = "/xcx/piclisten")
public class ParentXcxPicListenController extends AbstractXcxController {


    private static String defToken = new ObjectId().toHexString();


    @RequestMapping(value = "/mock_login.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage shelf() {
        getRequestContext().getCookieManager().setCookie("token", defToken, 86400);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/test_cookies.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage cookies() {
        String token = getRequestContext().getCookieManager().getCookie("token", null);
        if (token == null)
            return MapMessage.errorMessage("cookie 没有 token");
        if (StringUtils.isBlank(token))
            return MapMessage.errorMessage("cookie 有token，但是值为空串");
        if (!defToken.equals(token))
            return MapMessage. errorMessage("token 不正确");
        getRequestContext().getCookieManager().setCookie("token", defToken, 86400);
        return MapMessage.successMessage();
    }

}
