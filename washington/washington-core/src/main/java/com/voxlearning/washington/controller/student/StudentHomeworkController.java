package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLivecastLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.washington.support.AbstractController;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.USTALK_MOVE_DATE;

@Controller
@RequestMapping("/student/homework")
public class StudentHomeworkController extends AbstractController {

    @Getter
    @ImportService(interfaceClass = NewHomeworkLivecastLoader.class)
    private NewHomeworkLivecastLoader newHomeworkLivecastLoader;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String homeworkId = getRequestString("homeworkId");
        model.addAttribute("homeworkId", homeworkId);
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework != null) {
            model.addAttribute("listUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/index.vpage", MiscUtils.m("homeworkId", homeworkId)));
            model.addAttribute("subject", newHomework.getSubject().name());
            return "studentv3/homeworkv3/index";
        }
        return "redirect:/student/index.vpage";
    }

    @RequestMapping(value = "ustalk.vpage", method = RequestMethod.GET)
    public String ustalk(Model model) {
        String homeworkId = getRequestString("homeworkId");
        model.addAttribute("homeworkId", homeworkId);

        if (System.currentTimeMillis() < USTALK_MOVE_DATE.getTime()) {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (newHomework != null) {
                model.addAttribute("listUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/index.vpage", MiscUtils.m("homeworkId", homeworkId)));
                model.addAttribute("subject", newHomework.getSubject().name());
                return "studentv3/ustalkhomework/index";
            }
        } else {
            LiveCastHomework homework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(homeworkId);
            if (homework != null) {
                model.addAttribute("listUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/index.vpage", MapUtils.m("homeworkId", homeworkId)));
                model.addAttribute("subject", homework.getSubject().name());
                return "studentv3/ustalkhomework/index";
            }
        }

        String data = JsonUtils.toJson(MapUtils.m("homeworkId", homeworkId));
        String errorUrl = ProductConfig.getUSTalkUrl() + UrlUtils.buildUrlQuery("/homework/error.vpage",
                MapUtils.m("uid", currentStudent().getId(), "tag", "ERROR_HOMEWORK_NULL", "data", data));
        return "redirect:" + errorUrl;
    }

    @RequestMapping(value = "english/index.vpage", method = RequestMethod.GET)
    public String engIndex(Model model) {
        return "redirect:/student/index.vpage";
    }

    @RequestMapping(value = "vacation/english/do.vpage", method = RequestMethod.GET)
    public String index1(Model model) {
        return "redirect:/student/index.vpage";
    }

    @RequestMapping(value = "vacation/math/do.vpage", method = RequestMethod.GET)
    public String index2(Model model) {
        return "redirect:/student/index.vpage";
    }

    @RequestMapping(value = "vacation/index.vpage", method = RequestMethod.GET)
    public String index3(Model model) {
        String packageId = getRequestString("packageId");
        if (StringUtils.isBlank(packageId)) {
            return "redirect:/student/index.vpage";
        }
        return "studentv3/vacation/index";
    }
}
