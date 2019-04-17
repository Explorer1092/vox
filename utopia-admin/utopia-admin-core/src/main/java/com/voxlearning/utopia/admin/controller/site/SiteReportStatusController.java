package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.service.site.SiteReportStatusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Report Status维护功能
 * Created by yaguang.wang on 2016/10/18.
 */
@Controller
@RequestMapping("/site/reportstatus")
public class SiteReportStatusController extends SiteAbstractController {
    @Inject SiteReportStatusService siteReportStatusService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("statusList", siteReportStatusService.loadReportStatus());
        return "/site/reportstatus/index";
    }

    @RequestMapping(value = "update_status.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateStatus(@RequestBody String dataList) {
        Map<String, Object> jsonMap = JsonUtils.fromJson(dataList);
        try {
            Object data = jsonMap.get("dataList");
            List<Map<String, String>> statusList = (List<Map<String, String>>) data;
            return siteReportStatusService.updateReportStatus(statusList);
        } catch (Exception ex) {
            logger.error("Json data is error", jsonMap, ex);
            return MapMessage.errorMessage("数据传输错误");
        }
    }
}
