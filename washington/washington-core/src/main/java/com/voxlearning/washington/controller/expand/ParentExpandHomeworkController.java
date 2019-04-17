package com.voxlearning.washington.controller.expand;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.mobile.parent.AbstractMobileParentController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhangbin
 * @since 2017/8/8
 */

@Controller
@RequestMapping("/parent/expand/homework/report")
public class ParentExpandHomeworkController extends AbstractMobileParentController {

    @RequestMapping(value = "homeworklist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getHomeworkList() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "homeworkdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getHomeworkDetail() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "checkvoice.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage checkVoice() {
        return MapMessage.errorMessage("功能已下线");
    }

}
