package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.MoralMedalService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.mobile.teacher.AbstractMobileTeacherController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Named;
import java.util.Objects;


@Named
@RequestMapping("/parentMobile/moral_medal/")
public class MobileParentMoralMedalController extends AbstractMobileTeacherController {

    @ImportService(interfaceClass = MoralMedalService.class)
    private MoralMedalService moralMedalService;

    @ResponseBody
    @RequestMapping(value = "like.vpage", method = RequestMethod.POST)
    public MapMessage parentLike() {
        User user = currentUser();
        if (user == null || (!user.isParent())) {
            return noLoginResult;
        }
        long id = getRequestLong("id");
        if (id == 0) {
            return MapMessage.errorMessage("id 不可为空");
        }

        return moralMedalService.parentLike(user.getId(), id);
    }

    @ResponseBody
    @RequestMapping(value = "medal_detail.vpage")
    public MapMessage medalDetail() {
        long id = getRequestLong("id");
        if (id == 0) {
            return MapMessage.errorMessage("id 不可为空");
        }

        return moralMedalService.loadMedalDetail(id);
    }

    /**
     * 个人勋章统计
     *
     * @return
     */
    @RequestMapping(value = "hot_medal.vpage")
    @ResponseBody
    public MapMessage hotMedal() {
        long sid = getRequestLong("sid");

        if (Objects.equals(sid, 0L)) {
            return MapMessage.errorMessage("学生ID为空");
        }
        String date = getRequestString("date");

        return moralMedalService.hotMedal(sid, date);
    }

    /**
     * 个人勋章历史记录
     *
     * @return
     */
    @RequestMapping(value = "/history_medal.vpage")
    @ResponseBody
    public MapMessage historyMedal() {
        long sid = getRequestLong("sid");

        if (Objects.equals(sid, 0L)) {
            return MapMessage.errorMessage("学生ID为空");
        }

        //历史记录分页查询
        int page = getRequestInt("page") == 0 ? 1 : getRequestInt("page");
        int pagesize = getRequestInt("pagesize") == 0 ? 7 : getRequestInt("pagesize");
        if (pagesize > 30) {
            return MapMessage.errorMessage("查询范围过大");
        }

        return moralMedalService.historyMedal(sid, page, pagesize);
    }

}
