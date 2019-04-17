package com.voxlearning.washington.controller.ai;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.ai.api.AiOrderProductService;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_MESSAGE;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT;

/**
 * 用户中心
 * @Author songtao
 */

@Controller
@RequestMapping("/ai/center")
public class AIUserCenterController extends AbstractAiController {

    @ImportService(interfaceClass = AiOrderProductService.class)
    private AiOrderProductService aiOrderProductService;

    // 获取今日课程
    @RequestMapping(value = "course/my.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage myCourse() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no user!").add(RES_RESULT, "400").add(RES_MESSAGE, "没有登录");
        }
        try {
            return aiOrderProductService.loadUserCourseInfo(user.getId());
        } catch (Exception e) {
            logger.error("load my course error.", e);
            return MapMessage.errorMessage().add(RES_RESULT, "400").add(RES_MESSAGE, "服务器异常");
        }
    }

    // 获取今日课程
    @RequestMapping(value = "course/change.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage changeCourse() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no user!").add(RES_RESULT, "400").add(RES_MESSAGE, "没有登录");
        }
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().add(RES_RESULT, "402").add(RES_MESSAGE, "参数为空");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("aiOrderProductService.changeCourse")
                    .keys(user.getId())
                    .callback(() -> aiOrderProductService.changeUserBookRef(user.getId(), id))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage().add(RES_RESULT, "400").add(RES_MESSAGE, "正在处理中");
        } catch (Exception e) {
            logger.error("change course error.", e);
            return MapMessage.errorMessage().add(RES_RESULT, "400").add(RES_MESSAGE, "服务器异常");
        }
    }
}
