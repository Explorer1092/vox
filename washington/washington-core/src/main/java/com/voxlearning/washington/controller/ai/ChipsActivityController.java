package com.voxlearning.washington.controller.ai;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.helper.ShortUrlGenerator;
import com.voxlearning.utopia.service.ai.api.ChipsInvitionRewardLoader;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

@Controller
@RequestMapping("/chips/activity")
public class ChipsActivityController extends AbstractAiController {

    @ImportService(interfaceClass = ChipsInvitionRewardLoader.class)
    private ChipsInvitionRewardLoader chipsInvitionRewardLoader;

    private static final int BLACK = -16777216;
    private static final int WHITE = -1;

    @RequestMapping(value = "invite/index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage inviteIndex() {
        User user = currentUser();
        if (user == null) {
            return failMessage("400", "没有登录");
        }
        String refer = getRequestString("refer");
        return wrapper(mm -> {
            MapMessage mapMessage = chipsInvitionRewardLoader.loadInvitionIndexData(user.getId());
            mm.putAll(mapMessage);
            if (mapMessage.isSuccess()) {
                String linkUrl = SafeConverter.toString(mapMessage.get("linkUrl"));
                mm.put("linkUrl", linkUrl + "&refer=" + (StringUtils.isNotBlank(refer) ? refer : "330350") + "&channel=parent_app&type=invite");
            }
        });
    }

    @RequestMapping(value = "image.vpage", method = RequestMethod.GET)
    public void image(HttpServletResponse resp) {
        String url = getRequestString("url");
        if (StringUtils.isBlank(url)) {
            return;
        }
        try (OutputStream out = resp.getOutputStream(); InputStream inputStream = getRemoteStream(url)) {
            byte[] buffer = new byte[1024 * 4];
            int n = 0;
            while ((n = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            out.flush();
        } catch (Exception e) {
            logger.error("get image error. url:{}", url, e);
        }
    }

    private InputStream getRemoteStream(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(300 * 1000);
        return conn.getInputStream();
    }

    //二维码生成
    @RequestMapping(value = "qrcode.vpage", method = RequestMethod.GET)
    public void captcha(HttpServletResponse resp) throws IOException {
        String url = getRequestParameter("url", "");
        if (StringUtils.isBlank(url)) {
            return;
        }
        try (OutputStream out = resp.getOutputStream()) {
            String icon = getRequestString("icon");
            int color = getRequestInt("color");

            int imgWidth = getRequestInt("width", 300);
            int imgHeight = getRequestInt("height", 300);

            urlToQRCode(url, imgWidth, imgHeight, icon, color, out);
        } catch (Exception e) {
            logger.error("gen qrimage error. url:{}", url, e);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getOutputStream().write("生成二维码内容失败!".getBytes("utf-8"));
        }
    }
}
