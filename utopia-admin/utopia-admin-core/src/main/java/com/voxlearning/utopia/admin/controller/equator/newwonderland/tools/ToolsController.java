package com.voxlearning.utopia.admin.controller.equator.newwonderland.tools;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * 小工具类
 *
 * @author lei.liu
 * @since 2018-10-10T11:23:38+08:00
 */
@Controller
@RequestMapping(value = "equator/newwonderland/tools")
public class ToolsController extends AbstractEquatorController {

    @RequestMapping(value = "workbook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {
        if ((getRequest() instanceof MultipartHttpServletRequest)) {
            int pageNum = getRequestInt("pageNum", 0);
            int startRowNum = getRequestInt("startRowNum");
            try {
                List<Map<String, String>> data = resolveExcel("file", pageNum, startRowNum);
                model.addAttribute("data", JsonUtils.toJson(data));
            } catch (Exception e) {
                getAlertMessageManager().addMessageError(e.getMessage());
            }
        }
        return "equator/tools/workbookindex";
    }
}
