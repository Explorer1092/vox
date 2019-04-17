package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.lang.util.MapMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author guoqiang.li
 * @since 2017/7/25
 */
@Controller
@RequestMapping("/teacher/expand/homework")
public class TeacherExpandHomeworkController extends AbstractTeacherController {

    /**
     * 课外拓展h5首页，返回年级教材列表
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 根据教材获取子目标和作业形式
     */
    @RequestMapping(value = "objective/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadObjectiveList() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 根据子目标和作业形式获取内容
     */
    @RequestMapping(value = "objective/content.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadObjectiveContent() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 查看视频包详情
     */
    @RequestMapping(value = "video/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadVideoDetail() {
        return MapMessage.errorMessage("功能已下线");
    }
}
