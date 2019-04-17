package com.voxlearning.utopia.admin.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.entity.AiChipsEnglishTeacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author guangqing
 * @since 2018/12/4
 */
@Controller
@RequestMapping("/chips/ai/teacher")
public class ChipsEnglishTeacherController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        List<AiChipsEnglishTeacher> allList = chipsEnglishClazzService.loadAllChipsEnglishTeacher();
        model.addAttribute("teacherList", allList);
        return "chips/teacher/list";
    }

    @RequestMapping(value = "create.vpage", method = RequestMethod.GET)
    public String create(Model model) {
        String id = getRequestString("id");
        model.addAttribute("id", id);
        return "chips/teacher/create";
    }

    @RequestMapping(value = "query.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryTeacher() {
        String id = getRequestString("id");
        AiChipsEnglishTeacher teacher = chipsEnglishClazzService.loadChipsEnglishTeacherById(id);
        if (teacher == null) {
            teacher = new AiChipsEnglishTeacher();
        }
        MapMessage message = MapMessage.successMessage();
        message.add("teacher", teacher);
        return message;
    }

    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage save() {
        String id = getRequestString("id");
        String name = getRequestString("name");
        String wxCode = getRequestString("wxCode");
        String qrImage = getRequestString("qrImage");
        String headPortrait = getRequestString("headPortrait");
        AiChipsEnglishTeacher teacher = new AiChipsEnglishTeacher();
        if (StringUtils.isNotBlank(id)) {
            teacher.setId(id);
        }
        teacher.setName(name);
        teacher.setQrImage(qrImage);
        teacher.setWxCode(wxCode);
        teacher.setHeadPortrait(headPortrait);
        teacher.setDisabled(false);
        chipsEnglishClazzService.upsertAiChipsEnglishTeacher(teacher);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "delete.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage delete() {
        String id = getRequestString("id");
        return chipsEnglishClazzService.removeAiChipsEnglishTeacher(id);
    }

}
