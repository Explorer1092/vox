package com.voxlearning.washington.controller.teacher.activity;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityCardService;
import com.voxlearning.utopia.service.campaign.api.mapper.ExchangeFeedMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.teacher.AbstractTeacherController;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/activity/teacher/2018/card/")
public class TeacherWinterActivityController extends AbstractTeacherController {

    @ImportService(interfaceClass = TeacherActivityCardService.class)
    private TeacherActivityCardService teacherActivityCardService;

    @ResponseBody
    @RequestMapping("index.vpage")
    public MapMessage index() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage("未登录");
        }
        return teacherActivityCardService.index(user.getId());
    }


    @ResponseBody
    @RequestMapping("turn_over_card.vpage")
    public MapMessage turnOverCard() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage("未登录");
        }
        return teacherActivityCardService.turnOverCard(user.getId());
    }

    @ResponseBody
    @RequestMapping("compose.vpage")
    public MapMessage compose() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage("未登录");
        }
        String cardA = getRequestString("cardA");
        String cardB = getRequestString("cardB");

        if (StringUtils.isEmpty(cardA) || StringUtils.isEmpty(cardB)) {
            return MapMessage.errorMessage("参数错误");
        }

        return teacherActivityCardService.compose(user.getId(), cardA, cardB);
    }

    @ResponseBody
    @RequestMapping("exchange.vpage")
    public MapMessage exchange() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage("未登录");
        }

        String type = getRequestString("type");
        if (StringUtils.isEmpty(type)) {
            return MapMessage.errorMessage("参数错误");
        }
        return teacherActivityCardService.exchange(user.getId(), type);
    }

    @ResponseBody
    @RequestMapping("sign.vpage")
    public MapMessage sign() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage("未登录");
        }
        return teacherActivityCardService.sign(user.getId());
    }

    @ResponseBody
    @RequestMapping("exchange_feed.vpage")
    public MapMessage exchangeFeed() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage("未登录");
        }
        List<ExchangeFeedMapper> feed = teacherActivityCardService.loadExchangeFeed();
        return MapMessage.successMessage().add("feed", feed);
    }

    @ResponseBody
    @RequestMapping("clean_data.vpage")
    public MapMessage cleanUserData() {
        if (RuntimeMode.isProduction()) {
            return MapMessage.errorMessage();
        }

        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage("未登录");
        }
        return teacherActivityCardService.clearUserData(user.getId());
    }

    @ResponseBody
    @RequestMapping("test_draw.vpage")
    public MapMessage testDraw() {
        if (RuntimeMode.isProduction()) {
            return MapMessage.errorMessage();
        }

        long teacherId = getRequestLong("teacherId");
        return teacherActivityCardService.testDraw(teacherId);
    }

    @ResponseBody
    @RequestMapping("set_opportunity.vpage")
    public MapMessage setOpportunity() {
        if (RuntimeMode.isProduction()) {
            return MapMessage.errorMessage();
        }

        long teacherId = getRequestLong("teacherId");
        long count = getRequestLong("count");
        return teacherActivityCardService.setOpportunity(teacherId, count);
    }
}
