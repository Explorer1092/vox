package com.voxlearning.utopia.admin.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.ai.api.ChipsUserVideoLoader;
import com.voxlearning.utopia.service.ai.api.ChipsUserVideoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author guangqing
 * @since 2019/3/25
 */
@Controller
@RequestMapping("/chips/video/black")
public class ChipsVideoBlackListController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = ChipsUserVideoLoader.class)
    private ChipsUserVideoLoader chipsUserVideoLoader;
    @ImportService(interfaceClass = ChipsUserVideoService.class)
    private ChipsUserVideoService chipsUserVideoService;

    /**
     * 视频黑名单列表入口
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        return "chips/black/index";
    }

    /**
     * 视频黑名单列表数据
     */
    @RequestMapping(value = "listData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage listData() {
        return chipsUserVideoLoader.loadVideoBlackListForCrm();
    }


    /**
     * 新增入口
     */
    @RequestMapping(value = "editIndex.vpage", method = RequestMethod.GET)
    public String editIndex(Model model) {
        return "chips/black/editIndex";
    }

    @RequestMapping(value = "add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage add() {
        long userId = getRequestLong("userId");
        return chipsUserVideoService.addVideoBlackList(userId);
    }

    /**
     * 保存
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete() {
        long userId = getRequestLong("userId");
        return chipsUserVideoService.deleteVideoBlackList(userId);
    }
}
