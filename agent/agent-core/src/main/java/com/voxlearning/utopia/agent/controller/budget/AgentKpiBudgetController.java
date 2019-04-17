package com.voxlearning.utopia.agent.controller.budget;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.datareport.KpiBudgetReportData;
import com.voxlearning.utopia.agent.bean.export.XSSFWorkbookExportService;
import com.voxlearning.utopia.agent.constants.AgentKpiType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentKpiBudget;
import com.voxlearning.utopia.agent.persist.entity.AgentKpiBudgetRecord;
import com.voxlearning.utopia.agent.service.budget.AgentKpiBudgetService;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.export.XSSFWorkbookExportServiceImpl;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 *
 * @author song.wang
 * @date 2018/2/12
 */
@Controller
@RequestMapping("kpi/budget")
public class AgentKpiBudgetController extends AbstractAgentController {

    private static final String KPI_BUDGET_TEMPLATE = "/config/templates/kpi_budget_template.xlsx";

    @Inject
    private BaseExcelService baseExcelService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentKpiBudgetService agentKpiBudgetService;

    @RequestMapping(value = "download_template.vpage",method = RequestMethod.GET)
    public void downloadTemplate(){
        baseExcelService.downloadTemplate(KPI_BUDGET_TEMPLATE, "业绩目标");
    }

    @RequestMapping(value = "import_budget.vpage")
    @ResponseBody
    public MapMessage importBudget(HttpServletRequest request) {
        AuthCurrentUser user = getCurrentUser();
        if (!user.isCountryManager()) {
            return MapMessage.errorMessage("您无权导入业绩目标");
        }
        XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(request, "sourceFile");
        return agentKpiBudgetService.importBudget(workbook);
    }


    @RequestMapping(value = "budget_data_list.vpage")
    @ResponseBody
    public MapMessage budgetDataList() {

        Long groupId = getRequestLong("groupId");
        Integer month = getRequestInt("month");//月份
        if(groupId < 1){
            return MapMessage.errorMessage("部门ID错误！");
        }
        Date date = DateUtils.stringToDate(String.valueOf(month), "yyyyMM");
        if(month < 201801 || month > 202001 || date == null){
            return MapMessage.errorMessage("月份错误！");
        }

        MapMessage message = MapMessage.successMessage();
        List<Map<String, Object>> kpiTypeList = new ArrayList<>();
        List<AgentKpiType> kpiTypes = agentKpiBudgetService.fetchKpiTypeList(groupId);
        kpiTypes.stream().forEach(p -> {
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put("type", p.getType());
            typeMap.put("desc", p.getDesc());
            kpiTypeList.add(typeMap);
        });

        message.put("kpiTypeList", kpiTypeList);
        message.put("groupBudget", agentKpiBudgetService.generateGroupBudget(groupId, month));
        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
        if(groupRoleType != AgentGroupRoleType.City){
            List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(groupId);
            List<Long> groupIds = subGroupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
            message.put("subBudgetList", agentKpiBudgetService.generateGroupBudgetList(groupIds, month));
        }else {
            message.put("subBudgetList", agentKpiBudgetService.generateGroupUserBudgetList(groupId, month));
        }
        return message;
    }



    @RequestMapping(value = "budget_detail.vpage")
    @ResponseBody
    public MapMessage showBudgetDetail() {

        Integer month = getRequestInt("month");    //月份
        Integer groupOrUser = getRequestInt("groupOrUser");
        if(!Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_GROUP) && !Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_USER)){
            return MapMessage.errorMessage("groupOrUser错误！");
        }
        Long groupId = getRequestLong("groupId");
        if(groupId < 1){
            return MapMessage.errorMessage("groupId错误！");
        }
        Long userId = getRequestLong("userId");
        if(Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_USER) && userId < 1){
            return MapMessage.errorMessage("userId错误！");
        }
        Date date = DateUtils.stringToDate(String.valueOf(month), "yyyyMM");
        if(month < 201801 || month > 202001 || date == null){
            return MapMessage.errorMessage("月份错误！");
        }

        MapMessage message = MapMessage.successMessage();

        if(Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_GROUP)){
            message.put("budgetDetail", agentKpiBudgetService.generateGroupBudget(groupId, month));
        }else {
            message.put("budgetDetail", agentKpiBudgetService.generateUserBudget(userId, groupId, month));
        }
        return message;
    }


    @RequestMapping(value = "update_budget.vpage")
    @ResponseBody
    public MapMessage updateBudget() {
        AuthCurrentUser user = getCurrentUser();
        Integer month = getRequestInt("month");//月份类型，1：本月，2：下个月
        Integer groupOrUser = getRequestInt("groupOrUser");
        if(!Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_GROUP) && !Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_USER)){
            return MapMessage.errorMessage("groupOrUser错误！");
        }
        Long groupId = getRequestLong("groupId");
        if(groupId < 1){
            return MapMessage.errorMessage("groupId错误！");
        }
        Long userId = getRequestLong("userId");
        if(Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_USER) && userId < 1){
            return MapMessage.errorMessage("userId错误！");
        }
        Date date = DateUtils.stringToDate(String.valueOf(month), "yyyyMM");
        if(month < 201801 || month > 202001 || date == null){
            return MapMessage.errorMessage("月份错误！");
        }

        String comment = requestString("comment");

        Map<AgentKpiType, Integer> kpiBudgetMap = new HashMap<>();
        String kpiBudgetData = getRequestString("kpiBudgetData");
        if(StringUtils.isNotBlank(kpiBudgetData)){
            Map<String, String> parameterMap = JsonUtils.fromJsonToMapStringString(kpiBudgetData);
            if(MapUtils.isNotEmpty(parameterMap)){
                for(Map.Entry<String, String> entry : parameterMap.entrySet()){
                    AgentKpiType kpiType = AgentKpiType.typeOf(SafeConverter.toInt(entry.getKey()));
                    if(kpiType == null){
                        return MapMessage.errorMessage("指标错误！");
                    }
                    if(!StringUtils.isNumeric(entry.getValue())){
                        return MapMessage.errorMessage("请输入正确的数值");
                    }
                    kpiBudgetMap.put(kpiType, SafeConverter.toInt(entry.getValue()));
                }
            }
        }

        if(MapUtils.isNotEmpty(kpiBudgetMap)){
            boolean confirmed = agentKpiBudgetService.judgeConfirmed(groupId, month);
            // 指标未确认 或 全国总监，业务部经理可以修改
            if(!confirmed || (user.isCountryManager() || user.isBuManager())){
                if(Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_GROUP)){
                    return agentKpiBudgetService.updateGroupBudget(groupId, month, kpiBudgetMap, comment);
                }else {
                    return agentKpiBudgetService.updateUserBudget(userId, groupId, month, kpiBudgetMap, comment);
                }
            }else {
                MapMessage.errorMessage("业绩指标已确认，如需修改请联系上级主管！");
            }

        }

        return MapMessage.successMessage();

    }



    @RequestMapping(value = "confirm_budget.vpage")
    @ResponseBody
    public MapMessage confirmBudget() {

        Integer month = getRequestInt("month");//月份

        Long groupId = getRequestLong("groupId");
        if(groupId < 1){
            return MapMessage.errorMessage("groupId错误！");
        }
        Date date = DateUtils.stringToDate(String.valueOf(month), "yyyyMM");
        if(month < 201801 || month > 202001 || date == null){
            return MapMessage.errorMessage("月份错误！");
        }

        return agentKpiBudgetService.confirmBudget(groupId, month);
    }


    @RequestMapping(value = "export_budget.vpage")
    public void exportBudget() {
        Integer month = getRequestInt("month");//月份
        Long groupId = getRequestLong("groupId");
        try {
            if(groupId < 1){
                getResponse().getWriter().write("groupId错误！");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            Date date = DateUtils.stringToDate(String.valueOf(month), "yyyyMM");
            if(month < 201801 || month > 202001 || date == null){
                getResponse().getWriter().write("月份错误！");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            List<KpiBudgetReportData> reportDataList = agentKpiBudgetService.generateReportData(groupId, month);
            XSSFWorkbookExportService exportService = new XSSFWorkbookExportServiceImpl(reportDataList, KPI_BUDGET_TEMPLATE, true);

            Workbook workbook = exportService.convertToSXSSFWorkbook();
            String filename = month + "预算" + DateUtils.dateToString(new Date(), "yyyy-MM-dd") + ".xlsx";
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
        } catch (Exception e) {
        }
    }


    @RequestMapping(value = "budget_record_list.vpage")
    @ResponseBody
    public MapMessage budgetRecordList() {

        Integer month = getRequestInt("month");//月份类型，1：本月，2：下个月
        Integer groupOrUser = getRequestInt("groupOrUser");
        if(!Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_GROUP) && !Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_USER)){
            return MapMessage.errorMessage("groupOrUser错误！");
        }
        Long groupId = getRequestLong("groupId");
        if(groupId < 1){
            return MapMessage.errorMessage("groupId错误！");
        }
        Long userId = getRequestLong("userId");
        if(Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_USER) && userId < 1){
            return MapMessage.errorMessage("userId错误！");
        }
        Date date = DateUtils.stringToDate(String.valueOf(month), "yyyyMM");
        if(month < 201801 || month > 202001 || date == null){
            return MapMessage.errorMessage("月份错误！");
        }

        MapMessage message = MapMessage.successMessage();
        List<AgentKpiBudgetRecord> recordList = agentKpiBudgetService.generateBudgetRecordList(month, groupOrUser, groupId, userId);
        List<Map<String,Object>> resultList = recordList.stream().map(p -> {
            Map<String, Object> item = BeanMapUtils.tansBean2Map(p);
            item.put("kpiType", p.getKpiType().getDesc());
            return item;
        }).collect(Collectors.toList());
        message.put("recordList", resultList);
        return message;
    }


}
