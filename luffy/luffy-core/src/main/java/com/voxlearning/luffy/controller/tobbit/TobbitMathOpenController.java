package com.voxlearning.luffy.controller.tobbit;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.com.alibaba.dubbo.common.URL;
import com.voxlearning.luffy.controller.MiniProgramController;
import com.voxlearning.utopia.service.ai.api.TobbitMathScoreService;
import com.voxlearning.utopia.service.ai.api.TobbitMathService;
import com.voxlearning.utopia.service.ai.client.TobbitMathServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramApi;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * <a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=45039152">wiki</a>
 */
@Slf4j
@Controller
@RequestMapping(value = "/mp/tobbit")
public class TobbitMathOpenController extends MiniProgramController {


    @Inject
    private TobbitMathServiceClient tobbitMathServiceClient;

    @Override
    protected MiniProgramType type() {
        return MiniProgramType.TOBBIT;
    }


    @Override
    public boolean onBeforeControllerMethod() {
        return true;
    }


    @RequestMapping(value = "/welcome.vpage")
    @ResponseBody
    public MapMessage welcome() {
        String sp = getRequestString("sp");
        String openId = getOpenId();
        String suid = "";
        Long uid = uid();
        if (uid != null) {
            // 打卡
            miniProgramServiceClient.getUserMiniProgramCheckService().doCheck(uid, type());
            suid = String.valueOf(uid);

        }
        //邀请新用户
        if (nb(openId)) {

            // 渠道(蜂巢...)邀请用户
            if (nb(sp)) {
                sp = URL.decode(sp);
                tobbitMathServiceClient.getTobbitMathService().markSpUser(openId, sp);
            }
        }


        String logtoken = String.format("%s;%s", openId == null ? "" : openId, suid);
        // Set point info
        getRequestContext().getCookieManager().setCookie("Point", logtoken, 24 * 60 * 60);

        MapMessage mm = MapMessage.successMessage();
        mm.add("boost", tobbitMathServiceClient.getTobbitMathBoostService().isOnline());
        return mm;
    }


    @RequestMapping(value = "/wauth.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage wxAuth() {
        String code = getRequestString("code");
        String name = getRequestString("name");
        String avatar = getRequestString("avatar");

        MapMessage mm = MapMessage.successMessage();

        if (StringUtils.isBlank(code)) {
            return mm;
        }

        String openId = getOpenIdByCode(code);
        if (StringUtils.isBlank(openId)) {
            return mm;
        }
        return wrapper(mapMessage -> {
            tobbitServ().appendAuthUser(openId, name, avatar);
        });
    }


    @RequestMapping(value = "/atbot.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage atBot() {
        String latex = getRequestString("latex");
        if (StringUtils.isBlank(latex)) {
            return MapMessage.errorMessage("参数错误");
        }
        List<String> list = Arrays.asList(latex.split(","));
        return wrapper((mm) -> {
            mm.putAll(tobbitServ().atBot(uid(), list));
        });
    }

    @RequestMapping(value = "/identify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage identify(@RequestParam("file") MultipartFile file) {
        Long uid = uid();


        byte[] bytes = getFileBytes(file);
        if (bytes.length < 1) {
            return MapMessage.errorMessage("数据丢失，请重新上传");
        }

        return wrapper((mm) -> {
            mm.putAll(tobbitServ().identify(getOpenId(), uid, bytes, sys()));
        });
    }

    @RequestMapping(value = "/detail.vpage")
    @ResponseBody
    public MapMessage detail() {
        String qid = getRequestString("qid");


        if (StringUtils.isBlank(qid)) {
            return MapMessage.errorMessage("您所访问的资源不存在了");
        }
        return wrapper((mm) -> {
            mm.putAll(tobbitServ().load(qid));
        });
    }


    @RequestMapping(value = "share.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage share() {
        Long uid = uid();
        String qid = getRequestString("qid");
        return wrapper((mm) -> {
            mm.putAll(tobbitServ().share(getOpenId(), uid, qid));
        });
    }

    @RequestMapping(value = "/history.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage history() {
        Long uid = uid();
        return wrapper((mm) -> {
            mm.putAll(tobbitServ().loadByUid(getOpenId(), uid));
        });
    }


    @RequestMapping(value = "/course.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage myCourse() {
        return wrapper((mm) -> {
            mm.putAll(scoreServ().course(uid()));
        });
    }


    @RequestMapping(value = "/form_id.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage formId() {

        String formId = getRequestString("formId");
        if (StringUtils.isBlank(formId)) {
            return MapMessage.errorMessage("not accept empty value");
        }
        return wrapper((mm -> {
            miniProgramServiceClient.getUserMiniProgramCheckService().addNoticeFormId(getOpenId(), formId, type());
        }));
    }


    @RequestMapping(value = "/wxcode.vpage")
    public void getWxCodeImage() {

        String ref = getRequestString("r");
        String source = getRequestString("s");
        int width = getRequestInt("w");
        String token = "";
        byte[] bytes = new byte[0];
        boolean cache = true;
        try {

            if (ref.length() + source.length() > 28) {
                getResponse().setStatus(400);
                getResponse().getWriter().println("参数太长(28c)");
                return;
            }

            token = String.format("sp=%s;%s", escape(ref), escape(source));


            bytes = postCode(token, width, false);

            if (bytes.length < 1) {
                // try again
                bytes = postCode(token, width, true);
                cache = false;
            }

            if (bytes.length > 0) {
                writeImage(bytes);
            } else {
                getResponse().setStatus(400);
                getResponse().getWriter().println("生成失败,请稍后再试");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "mod1", ref,
                    "mod2", source,
                    "mod3", token,
                    "mod4", bytes.length,
                    "mod5", cache,
                    "mod6", getRequest().getRemoteAddr(),
                    "op", "wxcode_unlimit"
            ));

        }
    }


    private void writeImage(byte[] bytes) throws IOException {
        getResponse().setStatus(200);
        getResponse().setContentType("image/jpeg");
        getResponse().setHeader("Content-disposition", "filename=\"code.jpg\"");
        getResponse().setContentLength(bytes.length);
        getResponse().getOutputStream().write(bytes);
        getResponse().getOutputStream().flush();
    }


    private byte[] postCode(String token, int width, boolean forceAccessToken) {
        String accessToken;
        if (forceAccessToken) {
            accessToken = miniProgramServiceClient.getUserMiniProgramCheckService().getAccessTokenNoCache(MiniProgramType.TOBBIT);
        } else {
            accessToken = miniProgramServiceClient.getUserMiniProgramCheckService().getAccessToken(MiniProgramType.TOBBIT);
        }

        if (width == 0) {
            width = 430;
        } else if (width > 1280) {
            width = 1280;
        } else if (width < 280) {
            width = 280;
        }

        String url = MiniProgramApi.PIC_CODE_UNLIMITED.url(accessToken);
        String json = "{\"path\":\"pages/index/main\",\"width\":\"" + width + "\",\"scene\":\"" + token + "\"}";
        AlpsHttpResponse resp = HttpRequestExecutor.defaultInstance().post(url).json(json).execute();
        if (resp.getStatusCode() == 200) {
            byte[] bytes = resp.getOriginalResponse();
            if (bytes.length > 512) {
                return bytes;
            }
        }

        return new byte[0];
    }

    private String escape(String src) {
        return src.replaceAll(";", "");
    }


    private byte[] getFileBytes(MultipartFile file) {
        byte[] bytes = new byte[0];
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return bytes;
    }


    private TobbitMathService tobbitServ() {
        return tobbitMathServiceClient.getTobbitMathService();
    }

    private TobbitMathScoreService scoreServ() {
        return tobbitMathServiceClient.getTobbitMathScoreService();
    }

}
