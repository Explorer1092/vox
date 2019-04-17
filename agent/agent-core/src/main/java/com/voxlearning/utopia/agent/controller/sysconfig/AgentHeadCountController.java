package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.AgentGroupHeadCountInfo;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import lombok.Cleanup;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门HC维护
 *
 * @author chunlin.yu
 * @create 2018-04-25 20:06
 **/
@Controller
@RequestMapping("/sysconfig/headcount")
public class AgentHeadCountController extends AbstractAgentController {

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @OperationCode("b643bb83ebbb490e")
    public String indexPage() {
        return "/sysconfig/headcount/index";
    }

    /**
     * 查询数据列表
     *
     * @return
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage list() {
        AuthCurrentUser currentUser = getCurrentUser();
        List<AgentGroup> userGroups = baseOrgService.getUserGroups(currentUser.getUserId());
        if (CollectionUtils.isNotEmpty(userGroups)) {
            AgentGroup agentGroup = userGroups.get(0);
            List<AgentGroup> cityGroupList = new ArrayList<>();
            if (agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City) {
                cityGroupList.add(agentGroup);
            } else {
                List<AgentGroup> agentSubGroupList = baseOrgService.getSubGroupList(agentGroup.getId()).stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
                cityGroupList.addAll(agentSubGroupList);
            }
            List<AgentGroupHeadCountInfo> dataList = new ArrayList<>();
            cityGroupList.forEach(item -> {
                AgentGroupHeadCountInfo info = new AgentGroupHeadCountInfo();
                info.setGroupName(item.getGroupName());
                info.setGroupId(item.getId());
                info.setHeadCount(item.getHeadCount() != null ? item.getHeadCount() : 0);
                info.setRoleType(item.fetchGroupRoleType());
                Integer actuallyCount = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(item.getId(), AgentRoleType.BusinessDeveloper.getId()).size();
                info.setActuallyCount(actuallyCount);
                dataList.add(info);
            });
            return MapMessage.successMessage().add("dataList", dataList);
        }
        return MapMessage.successMessage();
    }

    /**
     * 下载导入模板
     */
    @RequestMapping(value = "download_template.vpage", method = RequestMethod.GET)
    public void downloadTemplate() {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        generateTemplate(workbook);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            workbook.write(outStream);
            outStream.flush();
            String nowTime = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME);
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    "部门HC导入模板-" + nowTime + ".xlsx",
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            workbook.dispose();
        } catch (IOException e) {
            logger.error("AgentHeadCountController.downloadTemplate info: ", e);
        }
    }

    /**
     * 导出部门HC  导出所有部门级别为市场的部门及其子部门下的HC数据
     */
    @RequestMapping(value = "export_group_hc.vpage", method = RequestMethod.GET)
    public void export() {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        List<AgentGroup> allSubGroupList = new ArrayList<>();
        List<AgentGroup> marketingGroups = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.Marketing);//
        marketingGroups.forEach(p -> {
            List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(p.getId());
            if(CollectionUtils.isNotEmpty(subGroupList)){
                allSubGroupList.addAll(subGroupList);
            }
        });

        allSubGroupList.addAll(marketingGroups);
        List<AgentGroupHeadCountInfo> dataList = new ArrayList<>();
        allSubGroupList.forEach(item -> {
            AgentGroupHeadCountInfo info = getAgentGroupHeadCountInfoByGroup(item);
            if (null != info) {
                dataList.add(info);
            }
        });

        generateData(workbook, dataList);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            workbook.write(outStream);
            outStream.flush();
            String nowTime = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME);
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    "部门HC数据-" + nowTime + ".xlsx",
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            workbook.dispose();
        } catch (IOException e) {
            logger.error("AgentHeadCountController.downloadTemplate info: ", e);
        }
    }


    @RequestMapping(value = "import_headcount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importHeadcount() {
        AuthCurrentUser user = getCurrentUser();
        if (!user.isCountryManager()) {
            return MapMessage.errorMessage("您无权操作部门HC数据");
        }
        XSSFWorkbook workbook = readRequestWorkbook("sourceFile");
        List<AgentGroupHeadCountInfo> agentGroupHeadCountInfos = convert2AgentGroupHeadCountInfo(workbook);
        MapMessage mapMessage = validateAndFillAgentGroupHeadCountInfo(agentGroupHeadCountInfos);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        agentGroupHeadCountInfos.forEach(item -> {
            AgentGroup group = new AgentGroup();
            group.setId(item.getGroupId());
            group.setHeadCount(item.getHeadCount());
            baseOrgService.updateAgentGroup(group);
        });
        return MapMessage.successMessage();
    }

    private MapMessage validateAndFillAgentGroupHeadCountInfo(List<AgentGroupHeadCountInfo> agentGroupHeadCountInfos) {
        List<String> errorList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(agentGroupHeadCountInfos)) {
            for (int i = 0; i < agentGroupHeadCountInfos.size(); i++) {
                int rows = i + 2;
                AgentGroupHeadCountInfo headCountInfo = agentGroupHeadCountInfos.get(i);
                if (StringUtils.isEmpty(headCountInfo.getGroupName())) {
                    errorList.add(rows + "行部门为空。");
                    continue;
                }
                AgentGroup agentGroup = baseOrgService.getGroupByName(headCountInfo.getGroupName());
                if (agentGroup == null) {
                    errorList.add(rows + "行部门在系统中不存在。");
                    continue;
                }
                if (agentGroup.fetchGroupRoleType() != AgentGroupRoleType.City) {
                    errorList.add(rows + "行部门级别不为分区。");
                    continue;
                }
                headCountInfo.setGroupId(agentGroup.getId());
                if (headCountInfo.getHeadCount() == null || headCountInfo.getHeadCount() <= 0) {
                    errorList.add(rows + "行应招专员数不为大于0的整数。");
                    continue;
                }
            }
        } else {
            errorList.add("Excel中无内容");
        }

        if (CollectionUtils.isNotEmpty(errorList)) {
            return MapMessage.errorMessage().add("errorList", errorList);
        }
        return MapMessage.successMessage();
    }

    private List<AgentGroupHeadCountInfo> convert2AgentGroupHeadCountInfo(XSSFWorkbook workbook) {
        List<AgentGroupHeadCountInfo> list = new ArrayList<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        int rows = 1;
        if (null != sheet) {
            while (true) {
                try {
                    AgentGroupHeadCountInfo headCountInfo = new AgentGroupHeadCountInfo();
                    XSSFRow row = sheet.getRow(rows++);
                    if (row == null) {
                        break;
                    }
                    String groupName = XssfUtils.getStringCellValue(row.getCell(0));
                    Integer headCount = XssfUtils.getIntCellValue(row.getCell(1));
                    headCountInfo.setGroupName(groupName);
                    headCountInfo.setHeadCount(headCount);
                    list.add(headCountInfo);
                } catch (Exception ex) {
                    logger.error("read excel failed", ex);
                    break;
                }
            }
        }
        return list;
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


    private void generateTemplate(SXSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet("HC");
        sheet.createFreezePane(0, 1, 0, 1);
        Font font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);
        CellStyle firstRowStyle = workbook.createCellStyle();
        firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        firstRowStyle.setFont(font);
        firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Row firstRow = sheet.createRow(0);
        setCellValue(firstRow, 0, firstRowStyle, "部门");
        setCellValue(firstRow, 1, firstRowStyle, "应招专员");
    }

    private void generateData(SXSSFWorkbook workbook, List<AgentGroupHeadCountInfo> dataList) {
        Sheet sheet = workbook.createSheet("HC");
        sheet.createFreezePane(0, 1, 0, 1);
        Font font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);
        CellStyle firstRowStyle = workbook.createCellStyle();
        firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        firstRowStyle.setFont(font);
        firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Row firstRow = sheet.createRow(0);
        setCellValue(firstRow, 0, firstRowStyle, "部门");
        setCellValue(firstRow, 1, firstRowStyle, "部门级别");
        setCellValue(firstRow, 2, firstRowStyle, "上级部门");
        setCellValue(firstRow, 3, firstRowStyle, "应招专员数");
        setCellValue(firstRow, 4, firstRowStyle, "实际在岗专员数");
        setCellValue(firstRow, 5, firstRowStyle, "满编率");
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        if (CollectionUtils.isNotEmpty(dataList)) {
            Integer index = 1;
            for (AgentGroupHeadCountInfo data : dataList) {
                Row row = sheet.createRow(index++);
                setCellValue(row, 0, cellStyle, data.getGroupName());
                setCellValue(row, 1, cellStyle, data.getRoleTypeName());
                setCellValue(row, 2, cellStyle, data.getParentGroupName());
                setCellValue(row, 3, cellStyle, data.getHeadCount());
                setCellValue(row, 4, cellStyle, data.getActuallyCount());
                setCellValue(row, 5, cellStyle, String.valueOf(data.getActuallyRate()) + "%");
            }
        }
    }

    private void setCellValue(Row row, int column, CellStyle style, Object value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        if (null != style) {
            cell.setCellStyle(style);
        }
        String info = value == null ? "" : String.valueOf(value).trim();
        if (!NumberUtils.isDigits(info)) {
            cell.setCellValue(info);
        } else {
            cell.setCellValue(SafeConverter.toLong(info));
        }
    }


    private AgentGroupHeadCountInfo getAgentGroupHeadCountInfoByGroup(AgentGroup agentGroup) {
        if (null != agentGroup) {
            AgentGroupHeadCountInfo info = new AgentGroupHeadCountInfo();
            Integer headCount = 0;
            if (agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City) {
                if (null != agentGroup.getHeadCount()) {
                    headCount = agentGroup.getHeadCount();
                }
            } else {
                List<AgentGroup> agentSubGroupList = baseOrgService.getSubGroupList(agentGroup.getId()).stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
                for (int i = 0; i < agentSubGroupList.size(); i++) {
                    AgentGroup groupTemp = agentSubGroupList.get(i);
                    if (groupTemp.getHeadCount() != null) {
                        headCount += groupTemp.getHeadCount();
                    }
                }
            }
            Integer actuallyCount = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(agentGroup.getId(), AgentRoleType.BusinessDeveloper.getId()).size();
            info.setGroupId(agentGroup.getId());
            info.setGroupName(agentGroup.getGroupName());
            info.setActuallyCount(actuallyCount);
            info.setRoleType(agentGroup.fetchGroupRoleType());
            info.setHeadCount(headCount);
            if (agentGroup.getParentId() != 0 && agentGroup.getParentId() > 0) {
                info.setParentGroupId(agentGroup.getParentId());
                AgentGroup parentGroup = baseOrgService.getGroupById(agentGroup.getParentId());
                info.setParentGroupName(parentGroup.getGroupName());
            }
            return info;
        }
        return null;
    }
}
