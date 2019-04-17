package com.voxlearning.wechat.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.ai.api.ChipsTaskLoader;
import com.voxlearning.utopia.service.ai.api.ChipsTaskService;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.AuthType;
import com.voxlearning.wechat.context.WxConfig;
import com.voxlearning.wechat.controller.AbstractChipsController;
import com.voxlearning.wechat.support.utils.OAuthUrlGenerator;
import com.voxlearning.wechat.support.utils.StringExtUntil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping(value = "/chips/task")
public class ChipsTaskController extends AbstractChipsController {

    @ImportService(interfaceClass = ChipsTaskLoader.class)
    private ChipsTaskLoader chipsTaskLoader;

    @ImportService(interfaceClass = ChipsTaskService.class)
    private ChipsTaskService chipsTaskService;

    // 激励体系 - 图鉴 - 用户答题升级
    @RequestMapping(value = "drawing_update.vpage", method = RequestMethod.GET)
    public String drawingUpdate(Model model) {
       String openId = getOpenId();
       boolean preview = getRequestBool("preview");
       if (StringUtils.isBlank(openId) && !preview) {
           String taskId = getRequestString("t");
           String param = "t=" + taskId;
           String key = StringExtUntil.md5(param);
           persistenceCache(key, param);
           return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChips(AuthType.CHIPS_DRAWING_TASK_JOIN, key);
       }

       WechatType wechatType = WechatType.CHIPS;
       WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
       initWechatConfigModel(model, wxConfig, wechatType);

        return "/parent/chips/app/drawing_update";
    }
    // 激励体系 - 图鉴 - 用户好用能量
    @RequestMapping(value = "friend_detaile.vpage", method = RequestMethod.GET)
    public String friendDetaile(Model model) {
//        WechatType wechatType = WechatType.CHIPS;
//        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
//        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/app/friend_detaile";
    }

    // 任务详情
    @RequestMapping(value = "drawing/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage drawingDetail() {
        long drawingTaskId = getRequestLong("drawingTaskId");
        if (drawingTaskId <= 0L) {
            return MapMessage.errorMessage("参数为空");
        }
        return chipsTaskLoader.loadDrawingTask(drawingTaskId);
    }

    // 任务详情
    @RequestMapping(value = "drawing/userinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userDrawingInfo() {
        long user = getRequestLong("user");
        if (user <= 0L) {
            return MapMessage.errorMessage("参数为空");
        }
        return chipsTaskLoader.loadUserDrawingInfo(user, getOpenId());
    }

    // 做任务
    @RequestMapping(value = "drawing/todo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doDrawingTask() {
        String openId = getOpenId();
        String userAnswer = getRequestString("userAnswer");
        long drawingTaskId = getRequestLong("drawingTaskId");
        if (drawingTaskId <= 0L || StringUtils.isAnyBlank(openId, userAnswer)) {
            return MapMessage.errorMessage("参数为空");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("chipsTaskService.processDrawingTaskJoin")
                    .keys(openId)
                    .callback(() -> chipsTaskService.processDrawingTaskJoin(drawingTaskId, openId, userAnswer))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理中");
        } catch (Exception e) {
            log.error("doDrawingTask error.", e);
            return MapMessage.errorMessage("服务器异常");
        }
    }

}
