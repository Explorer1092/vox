package com.voxlearning.utopia.agent.controller.surl;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.core.helper.ShortUrlGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@RequestMapping("/surl")
public class SurlController extends AbstractAgentController {

    @RequestMapping(value = "/create.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createShortUrl(){
        String url = getRequestString("url");
        if(StringUtils.isBlank(url)){
            return MapMessage.errorMessage("url为空");
        }
        try {
            Optional<String> optional = ShortUrlGenerator.generateShortUrl(url, true);
            if (!optional.isPresent()) {
                return MapMessage.errorMessage("短连接获取失败！");
            }
            String domainUrl =  ShortUrlGenerator.getShortUrlSiteUrl();
            if (StringUtils.isNotBlank(domainUrl) && domainUrl.contains("http:")) {
                domainUrl = domainUrl.replace("http:", "https:");
            }

            return MapMessage.successMessage().add("surl", domainUrl + "/" + optional.get());
        }catch (Exception e){
            return MapMessage.errorMessage("短连接获取失败！");
        }
    }
}
