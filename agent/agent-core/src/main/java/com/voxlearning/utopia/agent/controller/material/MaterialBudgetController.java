package com.voxlearning.utopia.agent.controller.material;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.bean.AgentMaterialBudgetVO;
import com.voxlearning.utopia.agent.bean.export.XSSFWorkbookExportService;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBalanceChangeRecord;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialCostExportData;
import com.voxlearning.utopia.agent.service.export.XSSFWorkbookExportServiceImpl;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品材料控制类
 *
 * @author chunlin.yu
 * @create 2018-02-06 17:58
 **/
@Controller
@RequestMapping(value = "/materialbudget/budget")
public class MaterialBudgetController extends AbstractAgentController {

    private static final int BYTES_BUFFER_SIZE = 1024 * 8;
    private final static String IMPORT_MATERIAL_BUDGET_TEMPLATE = "/config/templates/import_material_budget_template.xlsx";
    private final static String EXPORT_MATERIAL_BUDGET_TEMPLATE = "/config/templates/export_material_budget_template.xlsx";
    private final static String IMPORT_CITY_BUDGET_TEMPLATE = "/config/templates/import_city_budget_template.xlsx";
    private final static String IMPORT_CITY_BALANCE_TEMPLATE = "/config/templates/import_city_balance_template.xlsx";
    private final static String EXPORT_CITY_BUDGET_TEMPLATE = "/config/templates/export_city_budget_template.xlsx";
    private final static String IMPORT_MATERIAL_COST_TEMPLATE = "/config/templates/import_material_cost_template.xlsx";
    private final static String IMPORT_MATERIAL_COST_USER_TEMPLATE = "/config/templates/import_material_cost_user_template.xlsx";
    private final static String EXPORT_MATERIAL_COST_TEMPLATE = "/config/templates/export_material_cost_template.xlsx";

    @Inject private RaikouSystem raikouSystem;

    @Inject
    AgentMaterialBudgetService agentMaterialBudgetService;

    @RequestMapping("budget.vpage")
    @OperationCode("8b10e59d817547e3")
    public String budget() {
        String type = requestString("type", "material");
        if (type.equals("city")) {
            return "/materialbudget/city";
        }
        return "/materialbudget/budget";
    }

    /**
     * 城市检索
     *
     * @return
     */
    @RequestMapping(value = "search_city.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchCity() {
        String cityKey = getRequestString("cityKey");
        if (StringUtils.isEmpty(cityKey)) {
            return MapMessage.errorMessage("检索词为空");
        }
        int cityCode = SafeConverter.toInt(cityKey);
        List<ExRegion> dataList = new ArrayList<>();
        List<ExRegion> exRegions = raikouSystem.getRegionBuffer().loadRegions().stream().filter(item -> item.fetchRegionType() == RegionType.CITY).collect(Collectors.toList());
        if (cityCode > 0) {
            dataList.addAll(exRegions.stream().filter(item -> item.getCityCode() == cityCode).collect(Collectors.toList()));
        } else {
            dataList.addAll(exRegions.stream().filter(item -> item.getCityName().startsWith(cityKey)).collect(Collectors.toList()));
        }
        return MapMessage.successMessage().add("dataList", dataList);
    }

    /**
     * 分区检索
     *
     * @return
     */
    @RequestMapping(value = "search_group.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchGroup() {
        String groupKey = getRequestString("groupKey");
        if (StringUtils.isEmpty(groupKey)) {
            return MapMessage.errorMessage("检索词为空");
        }
        long groupId = SafeConverter.toLong(groupKey);
        List<AgentGroup> dataList = new ArrayList<>();
        if (groupId > 0) {
            dataList.add(baseOrgService.getGroupById(groupId));
        } else {
            List<AgentGroup> agentGroups = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.City);
            dataList.addAll(agentGroups.stream().filter(item -> item.getGroupName().startsWith(groupKey)).collect(Collectors.toList()));
        }
        return MapMessage.successMessage().add("dataList", dataList);
    }

    /**
     * 物料预算查询
     */
    @RequestMapping(value = "material.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMaterialBudget() {
        String groupName = requestString("groupName");
        List<AgentMaterialBudgetVO> budgetVOList = agentMaterialBudgetService.getMaterialBudgetsByGroup(groupName);
        return MapMessage.successMessage().add("budgetList", budgetVOList);
    }

    /**
     * 城市预算查询
     */
    @RequestMapping(value = "city.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCityBudget() {
        String groupName = requestString("groupName");
        String cityName = requestString("cityName");
        int beginMonth = getRequestInt("beginMonth");
        int endMonth = getRequestInt("endMonth");
        List<AgentMaterialBudgetVO> budgetVOList = agentMaterialBudgetService.getCityBudgets(groupName, cityName, beginMonth, endMonth);
        return MapMessage.successMessage().add("budgetList", budgetVOList);
    }

    /**
     * 物料余额修改记录
     */
    @RequestMapping(value = "material_change_record.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMaterialChangeRecord() {
        String budgetId = getRequestString("budgetId");
        List<AgentMaterialBalanceChangeRecord> budgetChangeRecords = agentMaterialBudgetService.getBalanceChangeRecords(budgetId);
        return MapMessage.successMessage().add("budgetChangeRecords", budgetChangeRecords);
    }


    /**
     * 导入物料
     *
     * @return
     */
    @RequestMapping(value = "importMaterialBudget.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importMaterialBudget() {
//        if (!getCurrentUser().isCountryManager()) {
//            return MapMessage.errorMessage("只有是全国总监才能使用该功能");
//        }
        XSSFWorkbook workbook = readRequestWorkbook("sourceFile");
        return agentMaterialBudgetService.importMaterialBudget(workbook);
    }


    /**
     * 导入城市预算
     *
     * @return
     */
    @RequestMapping(value = "importCityBudget.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importCityBudget() {
//        if (!getCurrentUser().isCountryManager()) {
//            return MapMessage.errorMessage("只有是全国总监才能使用该功能");
//        }
        Integer templateType = getRequestInt("templateType", 1);//1 导入城市费用  2 导入城市余额
        XSSFWorkbook workbook = readRequestWorkbook("sourceFile");
        return agentMaterialBudgetService.importCityBudget(workbook, templateType);
    }

    /**
     * 导出城市预算
     *
     * @return
     */
    @RequestMapping(value = "exportCityBudget.vpage", method = RequestMethod.GET)
    public void exportCityBudget(HttpServletResponse response) {
        try {
            String filename = "城市预算表-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
            String groupName = requestString("groupName");
            String cityName = requestString("cityName");
            int beginMonth = getRequestInt("beginMonth");
            int endMonth = getRequestInt("endMonth");
            List<AgentMaterialBudgetVO> budgetVOList = agentMaterialBudgetService.getCityBudgets(groupName, cityName, beginMonth, endMonth);
            XSSFWorkbookExportService xssfWorkbookExportService = new XSSFWorkbookExportServiceImpl(budgetVOList, EXPORT_CITY_BUDGET_TEMPLATE, true);
            Workbook workbook = xssfWorkbookExportService.convertToSXSSFWorkbook();
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            assert workbook != null;
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
        } catch (Exception ex) {
            logger.error("export is failed", ex);
        }
    }


    /**
     * 导出物料预算
     *
     * @return
     */
    @RequestMapping(value = "exportMaterialBudget.vpage", method = RequestMethod.GET)
    public void exportMaterialBudget(HttpServletResponse response) {
        try {
            String filename = "物料预算表-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
            String groupName = requestString("groupName");
            List<AgentMaterialBudgetVO> budgetVOList = agentMaterialBudgetService.getMaterialBudgetsByGroup(groupName);
            XSSFWorkbookExportService xssfWorkbookExportService = new XSSFWorkbookExportServiceImpl(budgetVOList, EXPORT_MATERIAL_BUDGET_TEMPLATE, true);
            Workbook workbook = xssfWorkbookExportService.convertToSXSSFWorkbook();
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            assert workbook != null;
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
        } catch (Exception ex) {
            logger.error("export is failed", ex);
        }
    }


    /**
     * 修改预算
     *
     * @return
     */
    @RequestMapping(value = "changeBudget.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeBudget() {
//        if (!getCurrentUser().isCountryManager()) {
//            return MapMessage.errorMessage("只有是全国总监才能使用该功能");
//        }
        String budgetId = getRequestString("id");
        //1:增加，2：减少
        int modifyType = getRequestInt("modifyType");
        double modifyCount = getRequestDouble("modifyCount");
        String modifyReason = getRequestString("modifyReason");
        return agentMaterialBudgetService.changeBudget(budgetId, modifyType, modifyCount, modifyReason);

    }

    /**
     * 下载物料预算导入模版
     */
    @RequestMapping(value = "download_import_material_budget_template.vpage", method = RequestMethod.GET)
    public void download_import_material_budget_template() {
        try {
            Resource resource = new ClassPathResource(IMPORT_MATERIAL_BUDGET_TEMPLATE);
            if (!resource.exists()) {
                logger.error("download import school dict template - template not exists ");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            String fileName = "物料预算导入模版.xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download import school dict Template - Excp : {};", e);
        }
    }

    /**
     * 下载城市预算导入模版
     */
    @RequestMapping(value = "download_import_city_budget_template.vpage", method = RequestMethod.GET)
    public void download_import_city_budget_template() {
        try {
            Integer templateType = getRequestInt("templateType");
            String path;
            String fileName;
            if (Objects.equals(templateType, 1)) {
                path = IMPORT_CITY_BUDGET_TEMPLATE;
                fileName = "城市预算导入模版.xlsx";
            } else {
                path = IMPORT_CITY_BALANCE_TEMPLATE;
                fileName = "批量更新城市费用余额模板.xlsx";
            }

            Resource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                logger.error("download import school dict template - template not exists ");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download import school dict Template - Excp : {};", e);
        }
    }

    /**
     * 修改余额
     *
     * @return
     */
    @RequestMapping(value = "changeBalance.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeBalance() {
        String id = getRequestString("id");
        //1:增加，2：减少
        int modifyType = getRequestInt("modifyType");
        double modifyCount = getRequestDouble("modifyCount");
        String modifyReason = getRequestString("modifyReason");
        return agentMaterialBudgetService.changeBalance(id, modifyType, modifyCount, modifyReason, getCurrentUserId());
    }


    private XSSFWorkbook readRequestWorkbook(String name) {
        HttpServletRequest request = getRequest();
        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return new XSSFWorkbook(in);
        } catch (Exception e) {
            logger.error("readRequestWorkbook - Excp : {}", e);
            return null;
        }
    }


    private static void write(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[BYTES_BUFFER_SIZE];
        int size;
        while ((size = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }


    /**
     * 下载物料费用导入模版
     */
    @RequestMapping(value = "download_import_material_cost_template.vpage", method = RequestMethod.GET)
    public void downloadImportMaterialCostTemplate() {
        try {
            Integer templateType = getRequestInt("templateType");
            String path;
            String fileName;
            if (Objects.equals(templateType, 1)) {
                path = IMPORT_MATERIAL_COST_TEMPLATE;
                fileName = "物料费用导入模版.xlsx";
            } else {
                path = IMPORT_MATERIAL_COST_USER_TEMPLATE;
                fileName = "物料余额导入模版.xlsx";
            }
            Resource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                logger.error("download import_material_cost_template - template not exists ");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);

            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download import school dict Template - Excp : {};", e);
        }
    }

    /**
     * 导入物料费用
     *
     * @return
     */
    @RequestMapping(value = "importMaterialCost.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importMaterialCost() {
        Integer templateType = getRequestInt("templateType", 1);//1 物料费用  2 物料余额
//        if (!getCurrentUser().isCountryManager()) {
//            return MapMessage.errorMessage("只有是全国总监才能使用该功能");
//        }
        XSSFWorkbook workbook = readRequestWorkbook("sourceFile");
        return agentMaterialBudgetService.importMaterialCost(workbook, templateType);
    }

    /**
     * 部门物料费用列表
     */
    @RequestMapping(value = "group_material_cost_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroupMaterialCost() {
        String groupName = getRequestString("groupName");
        List<Map<String, Object>> dataList = agentMaterialBudgetService.getGroupMaterialCostByGroup(groupName);
        return MapMessage.successMessage().add("dataList", dataList);
    }

    @RequestMapping("budget_list.vpage")
    public String budgetList() {
        return "/materialbudget/budget_list";
    }

    /**
     * 人员物料费用列表
     */
    @RequestMapping(value = "user_material_cost_list.vpage")
    public String getUserMaterialCost(Model model) {
        Long groupId = getRequestLong("groupId");
        String schoolTerm = getRequestString("schoolTerm");
        Map<String, Object> dataMap = agentMaterialBudgetService.getUserMaterialCost(groupId, schoolTerm);
        model.addAttribute("dataMap", dataMap);
        return "/materialbudget/budget_list";
    }


    /**
     * 修改部门预算
     *
     * @return
     */
    @RequestMapping(value = "change_group_budget.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeGroupBudget() {
//        if (!getCurrentUser().isCountryManager()) {
//            return MapMessage.errorMessage("只有是全国总监才能使用该功能");
//        }
        String id = getRequestString("id");
        //1:增加，2：减少
        Integer modifyType = getRequestInt("modifyType");
        Double modifyCount = getRequestDouble("modifyCount");
        String modifyReason = getRequestString("modifyReason");
        if (modifyCount <= 0) {
            return MapMessage.errorMessage("修改金额不正确！");
        }
        return agentMaterialBudgetService.changeGroupBudget(id, modifyType, modifyCount, modifyReason);

    }


    /**
     * 修改人员余额
     *
     * @return
     */
    @RequestMapping(value = "change_user_balance.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeUserBalance() {
        String id = getRequestString("id");
        Long userId = getRequestLong("userId");
        //1:增加，2：减少
        Integer modifyType = getRequestInt("modifyType");
        Double modifyCount = getRequestDouble("modifyCount");
        String modifyReason = getRequestString("modifyReason");
        String schoolTerm = getRequestString("schoolTerm");
        if (modifyCount <= 0) {
            return MapMessage.errorMessage("修改金额不正确！");
        }
        return agentMaterialBudgetService.changeUserBalance(id, userId, modifyType, modifyCount, modifyReason, getCurrentUserId(), schoolTerm);
    }

    /**
     * 导出物料费用
     *
     * @return
     */
    @RequestMapping(value = "export_material_cost.vpage", method = RequestMethod.GET)
    public void exportMaterialCost(HttpServletResponse response) {
        try {
            String filename = "物料费用表-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
            String groupName = getRequestString("groupName");
            List<AgentMaterialCostExportData> dataList = agentMaterialBudgetService.getMaterialCostExportData(groupName);
            XSSFWorkbookExportService xssfWorkbookExportService = new XSSFWorkbookExportServiceImpl(dataList, EXPORT_MATERIAL_COST_TEMPLATE, true);
            Workbook workbook = xssfWorkbookExportService.convertToSXSSFWorkbook();
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            assert workbook != null;
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
        } catch (Exception ex) {
            logger.error("export is failed", ex);
        }
    }

//    /**
//     * 历史数据迁移
//     * @return
//     */
//    @RequestMapping(value = "move_history_data.vpage")
//    @ResponseBody
//    public MapMessage moveHistoryData(){
//        return agentMaterialBudgetService.moveHistoryData();
//    }
}
