package com.voxlearning.wechat.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.support.MessageProcessor;
import com.voxlearning.wechat.support.utils.MessageParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理微信调用的认证相关
 *
 * @author Xin Xin
 * @since 10/16/15
 */
@Controller
@RequestMapping(value = "/")
@Slf4j
public class AuthController extends AbstractController {
    @Inject
    private MessageProcessor messageProcessor;

    /**
     * 微信接口验证
     *
     * @return
     */
    @RequestMapping(value = {"/wxp.vpage", "/wxt.vpage", "/wxc.vpage"}, method = RequestMethod.GET)
    @ResponseBody
    public String validate() {
        String echostr = getRequestString("echostr");
        String signature = getRequestString("signature");
        String timestamp = getRequestString("timestamp");
        String nonce = getRequestString("nonce");
        String token = ProductConfig.get("wechat.token", "");

        if (StringUtils.isBlank(echostr) || StringUtils.isBlank(signature) || StringUtils.isBlank(timestamp)
                || StringUtils.isBlank(nonce) || StringUtils.isBlank(token)) {
            return "";
        }

        try {
            if (validate(token, timestamp, nonce, signature)) {
                return echostr;
            }
        } catch (Exception ex) {
            log.error("validate failed.token:{},echostr:{},timestamp:{},nonce:{},signature:{}", token, echostr, timestamp, nonce, signature, ex);
        }
        return "";
    }

    /**
     * 微信家长端消息接收
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/wxp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String process_parent(HttpServletRequest request) {
        String msg = null;
        try {
            msg = IOUtils.toString(request.getInputStream())
                    .replace("\u0014", "");//过滤微信表情中带有的设备控制符
            log.debug("receive msg:{}", msg);

            if (null != msg) {
                MessageContext context = MessageParser.parse(msg);

                return messageProcessor.process(context, WechatType.PARENT);
            }
        } catch (IOException e) {
            log.error("process msg error,msg:{}", msg, e);
        } catch (InvocationTargetException | InstantiationException | DocumentException | IllegalAccessException e) {
            log.error("msg parse error,msg:{}", msg, e);
        } catch (Exception ex) {
            log.error("Handle msg error,msg:{}", msg, ex);
        }
        return "";
    }

    /**
     * 微信老师端消息接收
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/wxt.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String process_teacher(HttpServletRequest request) {
        String msg = null;
        try {
            msg = IOUtils.toString(request.getInputStream())
                    .replace("\u0014", "");//过滤微信表情中带有的设备控制符;
            log.debug("receive msg:{}", msg);

            if (null != msg) {
                MessageContext context = MessageParser.parse(msg);

                return messageProcessor.process(context, WechatType.TEACHER);
            }
        } catch (IOException e) {
            log.error("process msg error,msg:{}", msg, e);
        } catch (InvocationTargetException | InstantiationException | DocumentException | IllegalAccessException e) {
            log.error("msg parse error,msg:{}", msg, e);
        }
        return "";
    }

    /**
     * 微信薯条英语接收消息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/wxc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String process_chips(HttpServletRequest request) {
        String msg = null;
        try {
            msg = IOUtils.toString(request.getInputStream())
                    .replace("\u0014", "");//过滤微信表情中带有的设备控制符
            log.debug("receive msg:{}", msg);

            if (null != msg) {
                MessageContext context = MessageParser.parse(msg);

                return messageProcessor.process(context, WechatType.CHIPS);
            }
        } catch (IOException e) {
            log.error("process msg error,msg:{}", msg, e);
        } catch (InvocationTargetException | InstantiationException | DocumentException | IllegalAccessException e) {
            log.error("msg parse error,msg:{}", msg, e);
        } catch (Exception ex) {
            log.error("Handle msg error,msg:{}", msg, ex);
        }
        return "";
    }

    /**
     * 验证是否是合法的微信请求
     */
    public boolean validate(String token, String timestamp, String nonce, String signature) throws NoSuchAlgorithmException {
        log.debug("token:{} timestamp:{} nonce:{} echostr:{} ", token, timestamp, nonce, signature);

        List<String> list = Arrays.asList(token, timestamp, nonce).stream()
                .sorted()
                .collect(Collectors.toList());
        String sortedStr = StringUtils.join(list, "");

        return DigestUtils.sha1Hex(sortedStr).equals(signature);
    }

    @RequestMapping(value = "/error.vpage", method = RequestMethod.GET)
    public String error(HttpServletRequest request, Model model) {
        Object errmsg = request.getAttribute("errmsg");
        if (null == errmsg) {
            errmsg = "系统错误";
        }

        return redirectWithMsg(errmsg.toString(), model);
    }

    @RequestMapping(value = "/stop.vpage")
    public String stop(Model model){
        return "/parent/block/stop";
    }
}
