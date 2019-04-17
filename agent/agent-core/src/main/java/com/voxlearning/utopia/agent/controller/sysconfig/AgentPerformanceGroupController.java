package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.constants.PerformanceGroupType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.dao.mongo.AgentPerformanceGroupDao;
import com.voxlearning.utopia.agent.persist.entity.AgentPerformanceGroup;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentPerformanceGroupService;
import com.voxlearning.utopia.agent.view.AgentPerformanceGroupView;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 绩效分组
 *
 * @author song.wang
 * @date 2018/2/9
 */
@Controller
@RequestMapping("/sysconfig/performance_group")
public class AgentPerformanceGroupController extends AbstractAgentController {

    private static final String PERFORMANCE_GROUP_TEMPLATE = "/config/templates/performance_group_template.xlsx";

    @Inject
    private AgentPerformanceGroupDao agentPerformanceGroupDao;
    @Inject
    private BaseExcelService baseExcelService;
    @Inject
    private AgentPerformanceGroupService agentPerformanceGroupService;

    @RequestMapping("index.vpage")
    public String index(Model model){
        Integer month = requestInteger("month");
        if(month == null){
            month = SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMM"));
        }
        List<AgentPerformanceGroupView> performanceGroupList = agentPerformanceGroupService.generateAllUserGroupData(month);
        model.addAttribute("dataList", performanceGroupList);
        return "/sysconfig/performance_group/index";
    }

    @RequestMapping(value = "download_template.vpage")
    public void downloadTemplate() {
        baseExcelService.downloadTemplate(PERFORMANCE_GROUP_TEMPLATE, "专员绩效分组" + DateUtils.dateToString(new Date(), "yyyy-MM-dd"));
    }

    @RequestMapping(value = "import_data.vpage")
    @ResponseBody
    public MapMessage importLogisticsInfo() {
        XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(getRequest(), "sourceExcelFile");
        if (workbook == null) {
            return MapMessage.errorMessage();
        }
        return agentPerformanceGroupService.importPerformanceGroupData(workbook);
    }

    @RequestMapping(value = "export_data.vpage")
    public void exportData(HttpServletResponse response){
        Integer month = requestInteger("month");
        if(month == null){
            month = SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMM"));
        }
        XSSFWorkbook workbook = baseExcelService.readWorkBookFromTemplate(PERFORMANCE_GROUP_TEMPLATE);
        if(workbook == null){
            try {
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                response.getWriter().write("模板不存在");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<AgentPerformanceGroupView> performanceGroupList = agentPerformanceGroupService.generateAllUserGroupData(month);
        agentPerformanceGroupService.generateWorkbookData(workbook, performanceGroupList);
        try {
            String filename = "专员绩效分组" + DateUtils.dateToString(new Date(), "yyyy-MM-dd") + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                getResponse().getWriter().write("不能下载");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }catch (Exception e){

        }
    }

    @RequestMapping(value = "detail.vpage")
    @ResponseBody
    public MapMessage dataDetail(){
        Integer month = getRequestInt("month");
        Long userId = getRequestLong("userId");
        if(month < 201801 || userId < 1){
            return MapMessage.errorMessage("参数错误");
        }

        AgentPerformanceGroupView performanceGroup = agentPerformanceGroupService.generateUserPerformanceGroupView(month, userId);
        if(performanceGroup == null){
            return MapMessage.errorMessage("对应的分组不存在");
        }
        MapMessage message = MapMessage.successMessage();
        message.put("performanceGroup", performanceGroup);
        Map<String, String> typeList = new LinkedHashMap<>();
        for(PerformanceGroupType groupType : PerformanceGroupType.values()){
            typeList.put(groupType.name(), groupType.getDesc());
        }
        message.put("typeList", typeList);
        return message;
    }

    @RequestMapping(value = "update.vpage")
    @ResponseBody
    public MapMessage updateData(){
        Integer month = requestInteger("month");
        Long userId = requestLong("userId");
        String type = requestString("type");
        if(month < 201801 || userId < 1){
            return MapMessage.errorMessage("month或userId有误");
        }
        if(StringUtils.isNotBlank(type) && PerformanceGroupType.nameOf(type) == null){
            return MapMessage.errorMessage("绩效分组有误");
        }

        return agentPerformanceGroupService.upsertPerformanceGroup(month, userId, PerformanceGroupType.nameOf(type));
    }
}
