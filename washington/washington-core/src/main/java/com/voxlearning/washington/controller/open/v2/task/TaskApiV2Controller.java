package com.voxlearning.washington.controller.open.v2.task;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Ruib
 * @since 2017/4/18
 */
@Controller
@RequestMapping(value = "/v2/task")
public class TaskApiV2Controller extends AbstractApiController {

    // 参与闯关事件
    @RequestMapping(value = "/ftfappchuangparticipate.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fairylandTaskFinished_App_Chuang_Participate() {
        MapMessage mesg = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            mesg.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mesg.add(RES_MESSAGE, e.getMessage());
            return mesg;
        }

        mesg.add(RES_RESULT, RES_RESULT_SUCCESS);
        return mesg;
    }
}
