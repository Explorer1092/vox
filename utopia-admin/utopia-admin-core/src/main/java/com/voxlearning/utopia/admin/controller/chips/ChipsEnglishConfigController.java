package com.voxlearning.utopia.admin.controller.chips;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.ai.client.AiChipsEnglishConfigServiceClient;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/9/10
 */
@Controller
@RequestMapping("/chips/chips/config")
public class ChipsEnglishConfigController extends AbstractAdminSystemController {

    @Inject
    private AiChipsEnglishConfigServiceClient aiChipsEnglishConfigServiceClient;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        List<ChipsEnglishPageContentConfig> configList = aiChipsEnglishConfigServiceClient.getRemoteReference().loadAllChipsConfig4Crm();
        model.addAttribute("configList", configList);
        return "chips/list";
    }

    @RequestMapping(value = "addIndex.vpage", method = RequestMethod.GET)
    public String addIndex(Model model) {
        List<ChipsEnglishPageContentConfig> configList = aiChipsEnglishConfigServiceClient.getRemoteReference().loadAllChipsConfig4Crm();
        model.addAttribute("configList", configList);
        return "chips/addIndex";
    }

    @RequestMapping(value = "editIndex.vpage", method = RequestMethod.GET)
    public String editIndex(Model model) {
        String id = getRequestString("id");
        ChipsEnglishPageContentConfig config = aiChipsEnglishConfigServiceClient.getRemoteReference().loadChipsEnglishConfigById(id);
        model.addAttribute("config", config);
        return "chips/editIndex";
    }


    @RequestMapping(value = "add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage add() {
        String name = getRequestString("name");
        String value = getRequestString("value");
        String memo = getRequestString("memo");
        aiChipsEnglishConfigServiceClient.getRemoteReference().addChipsEnglishPageContentConfig(name, value, memo);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage edit() {
        String id = getRequestString("id");
        String name = getRequestString("name");
        String value = getRequestString("value");
        String memo = getRequestString("memo");
        aiChipsEnglishConfigServiceClient.getRemoteReference().updateChipsEnglishPageContentConfig(id, name, value, memo);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete() {
        String id = getRequestString("id");
        aiChipsEnglishConfigServiceClient.getRemoteReference().deleteChipsEnglishPageContentConfig(id);
        return MapMessage.successMessage();
    }
}
