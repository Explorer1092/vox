package com.voxlearning.utopia.agent.controller.mobile.live;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.crypto.Mac;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * 视频直播间
 */

@Controller
@RequestMapping(value = "/mobile/live")
public class LiveController extends AbstractAgentController {

    //首页设置页
    @RequestMapping(value = "index.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String live(Model model) {
        AgentUser agentUser = baseOrgService.getUser(getCurrentUserId());

        String url = "";
        String appId = "";
        String secretKey = "";
        String liveId = "";
        if(RuntimeMode.lt(Mode.STAGING)){
            url = "https://activity.test.17zuoye.net/index.html";
            appId = "58eee6ac19b005fec0d848ce";
            secretKey = "4911898908f9d03ae7bf913f2ae16cb1";
            liveId = "5cac4089910ef16d17237ba8";
        }else {
            url = "https://livecdn.17zuoye.com/zylive/index.html";
            appId = "59a91c3237d3d8d28516801c";
            secretKey = "ea4958b53cd9da924e1223252d5d215b";
            liveId = "5cac3a0de5a9ce3b5fe6bb78";
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("nickname", agentUser.getRealName());
        paramMap.put("user_id", String.valueOf(agentUser.getId()));
        paramMap.put("avatar_url", StringUtils.isBlank(agentUser.getAvatar()) ? "" : agentUser.getAvatar());
        paramMap.put("user_type", "1");
        paramMap.put("live_id", liveId);
        paramMap.put("room_index", "5");
        paramMap.put("timestamp", System.currentTimeMillis() + "");
        paramMap.put("app_id", appId);

        String sign = generateSign(paramMap, secretKey);
        paramMap.put("sign", sign);

        String p = generateRequestQueryStr(paramMap, true);
        model.addAttribute("liveUrl", url + "?" + p);
        return "rebuildViewDir/mobile/my/live";
    }

    private String generateRequestQueryStr(Map<String, String> m, boolean encoded){
        List<String> keys = new ArrayList<>(m.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        try {
            for (String key : keys) {
                sb.append(key).append('=').append(encoded ? URLEncoder.encode(m.get(key), "UTF-8") : m.get(key)).append('&');
            }
        } catch (UnsupportedEncodingException ex) {
            logger.error("URLEncoder.encode Failed, param=", ex);
        }

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private String generateSign(Map<String, String> m, String secretKey){
        String paramStr = generateRequestQueryStr(m, false);

        Mac mac = HmacUtils.getHmacSha256(secretKey.getBytes());
        String sign = null;
        try {
            sign = Hex.encodeHexString(mac.doFinal(paramStr.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sign;
    }
}
