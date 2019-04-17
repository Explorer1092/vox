package com.voxlearning.washington.controller.parent.homework;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Objects;

@Controller
@RequestMapping("/parent/backdoor")
public class HomeworkBackDoorController extends AbstractController {

    @RequestMapping(value = "qlimit.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage qLimit() {
        if (getRequestString("count") ==  null || !Objects.equals(getRequestString("q"), DateUtils.dateToString(new Date(), "yyyyMMdd") + "17zuoye")) {
            return MapMessage.errorMessage();
        }
        washingtonCacheSystem.CBS.flushable.set("PARENT_HOMEWORK_Q_LIMIT_20190111.", 0, getRequestInt("count"));
        return MapMessage.successMessage();
    }
}
