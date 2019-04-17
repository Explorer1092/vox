package com.voxlearning.washington.controller.mobile.api;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.mobile.AbstractMobileWonderlandActivityController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liu jingchao
 * @since 2017/6/29
 */
@Controller
@RequestMapping(value = "/mobileApi/fairyland")
public class MobileFairylandApiController extends AbstractMobileWonderlandActivityController {

    @RequestMapping(value = "talk-fun/callback.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage callback() {

        Map<String, Object> parameterMap = new HashMap<>();

        if (StringUtils.isNotBlank(getRequest().getParameter("ver")))
            parameterMap.put("ver", getRequest().getParameter("ver"));
        if (StringUtils.isNotBlank(getRequest().getParameter("openID")))
            parameterMap.put("openID", getRequest().getParameter("openID"));
        if (StringUtils.isNotBlank(getRequest().getParameter("timestamp")))
            parameterMap.put("timestamp", getRequest().getParameter("timestamp"));
        if (StringUtils.isNotBlank(getRequest().getParameter("cmd")))
            parameterMap.put("cmd", getRequest().getParameter("cmd"));
        if (StringUtils.isNotBlank(getRequest().getParameter("params")))
            parameterMap.put("params", getRequest().getParameter("params"));
        if (StringUtils.isNotBlank(getRequest().getParameter("sign")))
            parameterMap.put("sign", getRequest().getParameter("sign"));

        if (MapUtils.isEmpty(parameterMap) || parameterMap.size() < 6 || !parameterMap.containsKey("cmd")) {
            return MapMessage.errorMessage().add("code", -1).add("msg", "验证失败").add("data", parameterMap);
        }

        MapMessage resultMessage = babyEagleServiceClient.getRemoteReference().classHourCallBack(parameterMap);
        if (resultMessage.isSuccess()) {
            return MapMessage.successMessage().add("code", 0).add("msg", "处理成功").add("data", parameterMap);
        } else {
            return MapMessage.errorMessage().add("code", -1).add("msg", resultMessage.getInfo()).add("data", parameterMap);
        }
    }

}
