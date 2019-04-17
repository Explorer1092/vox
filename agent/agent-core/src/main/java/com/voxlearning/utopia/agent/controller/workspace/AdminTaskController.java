/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.bean.FormerEmployeeData;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016Persistence;
import com.voxlearning.utopia.agent.salary.SalaryCalculatorEngine;
import com.voxlearning.utopia.agent.salary.type.SalaryKpiType;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by Alex on 14-9-19.
 */
@Controller
@RequestMapping("/workspace/admin")
@Slf4j
public class AdminTaskController extends AbstractAgentController {

    @Inject private SalaryCalculatorEngine salaryCalculatorEngine;
    @Inject private BaseExcelService baseExcelService;
    @Inject private AgentUserKpiResultSpring2016Persistence agentUserKpiResultSpring2016Persistence;


    private static final Map<String, SalaryKpiType> KpiDescMap = SalaryKpiType.getKpiDescMap();
    private final static String INSERT_KPI_RESULT_TEMPLATE = "/config/templates/insert_kpi_result_template.xlsx";
    private final static String UPDATE_KPI_RESULT_TEMPLATE = "/config/templates/update_kpi_result_template.xlsx";
    private static final int BYTES_BUFFER_SIZE = 1024 * 8;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String index() {
        return "workspace/admin/index";
    }

    @RequestMapping(value = "calsalary.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage calSalary(String runDate, String userIds) {
        MapMessage message = new MapMessage();
        try {
            Date calculateDate = getRequestDate("runDate");
            if (calculateDate == null) {
                return MapMessage.errorMessage("请指定执行时间!");
            }
            // FIXME 3月份工资已经封存，不允许再对4月份之前的工资进行重算了
            // FIXME 5月份工资策略调整，对5月份之前的数据也不允许重复计算了 By Wyc 2016-05-16
            if (calculateDate.before(DateUtils.stringToDate("2016-07-01", "yyyy-MM-dd"))) {
                return MapMessage.errorMessage("2016年9月之前工资已经结算完毕，不允许再重新计算！");
            }
            Set<Long> userIdSet = requestLongSet("userIds");
            XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(getRequest(), "sourceExcelFile");
            List<FormerEmployeeData> formerEmployeeDataList = generateFormerEmployeeDataList(workbook);

            boolean includeDictSchool = getRequestBool("includeDictSchool");

            long start = System.currentTimeMillis();
            salaryCalculatorEngine.start(calculateDate, new ArrayList<>(userIdSet), formerEmployeeDataList, includeDictSchool);
            long end = System.currentTimeMillis();
            long total = (end - start) / 1000;
            log.info("Spring 2016 kpi runtime : {}", total);
            message.setSuccess(true);
            message.setInfo("春季KPI计算完成，耗时：" + total / 60 + "分" + total % 60 + " 秒");
            return message;
        } catch (Exception e) {
            log.error("任务执行失败", e);
            message.setSuccess(false);
            message.setInfo("操作失败!" + e.getMessage());
            return message;
        }
    }

    @RequestMapping(value = "adjustsalary.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adjustSalary() {
        String type = getRequestString("type");
        if (!"insert".equals(type) && !"update".equals(type)) {
            return MapMessage.errorMessage("无效的类型");
        }
        try {
            XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(getRequest(), "adjustExcel");
            return processAdjustSalary(type, workbook);
        } catch (Exception e) {
            log.error("任务执行失败", e);
            return MapMessage.errorMessage("操作失败!" + e.getMessage());
        }
    }

    /**
     * 数据模板
     */
    @RequestMapping(value = "downloadtemplate.vpage", method = RequestMethod.GET)
    public void downloadKpiResultTemplate() {
        String type = getRequestString("type");
        String filePath = null;
        String title = "";
        if ("insert".equals(type)) {
            filePath = INSERT_KPI_RESULT_TEMPLATE;
            title = "插入";
        } else if ("update".equals(type)) {
            filePath = UPDATE_KPI_RESULT_TEMPLATE;
            title = "更新";
        }
        if (StringUtils.isBlank(filePath)) return;
        try {
            Resource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                logger.error("download {} kpi result template failed - template not exists ", type);
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            String fileName = title + "数据模版.xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download {} kpi result template failed - ExMsg : {};", e);
        }
    }

    private List<FormerEmployeeData> generateFormerEmployeeDataList(XSSFWorkbook workbook) {

        List<FormerEmployeeData> formerEmployeeDataList = new ArrayList<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet != null) {
            int rowNo = 1; // 从第二行开始
            FormerEmployeeData formerEmployeeData;
            Set<Long> userIdSet = new HashSet<>();
            while (true) {
                XSSFRow row = sheet.getRow(rowNo++);
                if (row == null) {
                    break;
                }

                // 获取发货单
                Long userId = XssfUtils.getLongCellValue(row.getCell(0));
                if (userId == null || userIdSet.contains(userId) || baseUserService.getUser(userId) == null) {
                    continue;
                }

                formerEmployeeData = new FormerEmployeeData();
                formerEmployeeData.setUserId(userId);
                AgentRoleType userRole = AgentRoleType.of(XssfUtils.getIntCellValue(row.getCell(2)));
                formerEmployeeData.setRoleType(userRole);

                AgentCityLevelType cityLevelType = AgentCityLevelType.CityLevelB;
                Integer cityLevel = XssfUtils.getIntCellValue(row.getCell(3));
                if (cityLevel != null) {
                    if (cityLevel == 1) {
                        cityLevelType = AgentCityLevelType.CityLevelSS;
                    } else if (cityLevel == 2) {
                        cityLevelType = AgentCityLevelType.CityLevelS;
                    } else if (cityLevel == 3) {
                        cityLevelType = AgentCityLevelType.CityLevelA;
                    } else if (cityLevel == 4) {
                        cityLevelType = AgentCityLevelType.CityLevelB;
                    }
                }
                formerEmployeeData.setCityLevelType(cityLevelType);

                boolean isJuniorAgentModel = XssfUtils.getIntCellValue(row.getCell(4)) == 1;
                boolean isMiddleAgentModel = XssfUtils.getIntCellValue(row.getCell(5)) == 1;
                formerEmployeeData.setIsJuniorAgentModel(isJuniorAgentModel);
                formerEmployeeData.setIsMiddleAgentModel(isMiddleAgentModel);
                String schoolIdsStr = StringUtils.trim(XssfUtils.getStringCellValue(row.getCell(6))); // 物流公司
                Set<Long> schoolSet = convertLongSet(schoolIdsStr, ",");
                formerEmployeeData.setSchoolIdList(new ArrayList<>(schoolSet));

                formerEmployeeDataList.add(formerEmployeeData);
                userIdSet.add(userId);
            }
        }
        return formerEmployeeDataList;
    }

    private MapMessage processAdjustSalary(String type, XSSFWorkbook workbook) {
        if (!"insert".equals(type) && !"update".equals(type)) {
            return MapMessage.errorMessage("无效的类型");
        }
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet == null) {
            return MapMessage.errorMessage("无效的Excel数据");
        }
        int rowNo = 1; // 从第二行开始
        StringBuilder info = new StringBuilder();
        try {
            if ("insert".equals(type)) {
                while (true) {
                    XSSFRow row = sheet.getRow(rowNo++);
                    if (row == null) {
                        break;
                    }
                    Long regionId = SafeConverter.toLong(XssfUtils.getLongCellValue(row.getCell(0))); // 大区ID
                    if (regionId == 0L) {
                        info.append(String.format("第%d行导入失败：无效的大区ID:%d\n", rowNo, regionId));
                        continue;
                    }
                    String regionName = XssfUtils.getStringCellValue(row.getCell(1)); // 大区名称
                    Long provinceId = SafeConverter.toLong(XssfUtils.getLongCellValue(row.getCell(2))); // 部门ID
                    String provinceName = XssfUtils.getStringCellValue(row.getCell(3)); // 部门名称
                    Integer salaryMonth = SafeConverter.toInt(XssfUtils.getIntCellValue(row.getCell(4))); // 结算月
                    if (salaryMonth > 201707 || salaryMonth < 201609) {
                        info.append(String.format("第%d行导入失败：无效的结算月份:%d\n", rowNo, salaryMonth));
                        continue;
                    }
                    Long userId = SafeConverter.toLong(XssfUtils.getLongCellValue(row.getCell(5))); // 人员ID
                    if (userId == 0L) {
                        info.append(String.format("第%d行导入失败：无效的人员ID:%d\n", rowNo, userId));
                        continue;
                    }
                    String userName = XssfUtils.getStringCellValue(row.getCell(6)); // 人员姓名

                    String start = XssfUtils.getStringCellValue(row.getCell(7)); // 结算开始时间
                    Date startDate = DateUtils.stringToDate(start, DateUtils.FORMAT_SQL_DATE);

                    String end = XssfUtils.getStringCellValue(row.getCell(8)); // 结算截止时间
                    Date endDate = DateUtils.stringToDate(end, DateUtils.FORMAT_SQL_DATE);

                    String cpaType = XssfUtils.getStringCellValue(row.getCell(9)); // 结算指标名称
                    if (!KpiDescMap.containsKey(cpaType)) {
                        info.append(String.format("第%d行导入失败：无效的结算指标:%s\n", rowNo, cpaType));
                        continue;
                    }
                    Long target = SafeConverter.toLong(XssfUtils.getLongCellValue(row.getCell(10))); // 目标
                    Long result = SafeConverter.toLong(XssfUtils.getLongCellValue(row.getCell(11))); // 实绩
                    Long salary = SafeConverter.toLong(XssfUtils.getLongCellValue(row.getCell(12))); // 工资
                    String note = XssfUtils.getStringCellValue(row.getCell(13)); // 备注
                    if (StringUtils.isNotBlank(note) && note.length() > 180) {
                        info.append(String.format("第%d行插入失败：备注不要超过150个字\n", rowNo));
                        continue;
                    }
                    AgentUserKpiResultSpring2016 entity = new AgentUserKpiResultSpring2016();
                    entity.setRegionId(regionId);
                    entity.setRegionName(regionName);
                    entity.setProvinceId(provinceId);
                    entity.setProvinceName(provinceName);
                    entity.setCountyCode(0);
                    entity.setCountyName(null);
                    entity.setSalaryMonth(salaryMonth);
                    entity.setUserId(userId);
                    entity.setUserName(userName);
                    entity.setStartDate(startDate);
                    entity.setEndDate(endDate);
                    entity.setCpaType(cpaType);
                    entity.setCpaTarget(target);
                    entity.setCpaResult(result);
                    entity.setCpaSalary(salary);
                    entity.setCpaNote(note);
                    // FIXME 事出有急，校验的过程暂时简化吧。。。人工核对
                    agentUserKpiResultSpring2016Persistence.deleteByUserId(userId, salaryMonth, cpaType);
                    Long persist = agentUserKpiResultSpring2016Persistence.persist(entity);
                    if (persist != null) {
                        info.append(String.format("第%d行导入成功\n", rowNo));
                    }
                }
            } else if ("update".equals(type)) {
                while (true) {
                    XSSFRow row = sheet.getRow(rowNo++);
                    if (row == null) {
                        break;
                    }
                    Long userId = SafeConverter.toLong(XssfUtils.getLongCellValue(row.getCell(0))); // 人员ID
                    if (userId == 0L) {
                        info.append(String.format("第%d行更新失败：无效的人员ID:%d\n", rowNo, userId));
                        continue;
                    }
                    String account = XssfUtils.getStringCellValue(row.getCell(1)); // 人员账号
                    String cpaType = SafeConverter.toString(XssfUtils.getStringCellValue(row.getCell(2))); // 结算指标名称
                    if (!KpiDescMap.containsKey(cpaType)) {
                        info.append(String.format("第%d行更新失败：无效的结算指标:%s\n", rowNo, cpaType));
                        continue;
                    }
                    Long salary = SafeConverter.toLong(XssfUtils.getLongCellValue(row.getCell(3))); // 工资
                    String note = XssfUtils.getStringCellValue(row.getCell(4)); // 备注
                    if (StringUtils.isNotBlank(note) && note.length() > 180) {
                        info.append(String.format("第%d行更新失败：备注不要超过150个字\n", rowNo));
                        continue;
                    }
                    Integer salaryMonth = SafeConverter.toInt(XssfUtils.getIntCellValue(row.getCell(5))); // 结算月
                    if (salaryMonth > 201707 || salaryMonth < 201609) {
                        info.append(String.format("第%d行更新失败：无效的结算月份:%d\n", rowNo, salaryMonth));
                        continue;
                    }
                    // FIXME 事出有急，校验的过程暂时简化吧。。。人工核对
                    int cnt = agentUserKpiResultSpring2016Persistence.adjustUserSalary(userId, salaryMonth, cpaType, salary, note);
                    if (cnt > 0) {
                        info.append(String.format("第%d行更新成功\n", rowNo));
                    } else {
                        info.append(String.format("第%d行更新失败: 找不到对应更新项\n", rowNo));
                    }
                }
            }
            return MapMessage.successMessage(info.toString());
        } catch (Exception ex) {
            return MapMessage.errorMessage("导入数据失败: " + ex.getMessage());
        }
    }

    private Set<Long> convertLongSet(String str, String sep) {
        Set<Long> values = new HashSet<>();
        if (StringUtils.isBlank(str)) {
            return values;
        }
        String[] array = str.split(sep);
        for (String e : array) {
            long value = SafeConverter.toLong(e);
            if (value > 0) {
                values.add(value);
            }
        }
        return values;
    }

    private void write(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[BYTES_BUFFER_SIZE];
        int size;
        while ((size = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }

}
