/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.newhomework.api.mapper.termreport.*;
import com.voxlearning.utopia.service.psr.entity.termreport.*;
import com.voxlearning.utopia.service.psr.termreport.loader.PsrTermReportIPackageLoader;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 类TeacherNewHomeworkTermReportController的实现：老师PC端学期报告、单元报告
 * 从之前的TeacherNewHomeworkGoalController中移出来
 *
 * @author zhangbin
 * @since 2017/3/14 17:33
 */

@Controller
@RequestMapping("/teacher/newhomework/")
public class TeacherNewHomeworkTermReportController extends AbstractTeacherController {

  @Inject private RaikouSDK raikouSDK;
  @Inject private RaikouSystem raikouSystem;

  @ImportService(interfaceClass = PsrTermReportIPackageLoader.class)
  private PsrTermReportIPackageLoader psrTermReportIPackageLoader;

  /**
   * 学期报告
   */
  @RequestMapping(value = "termreport.vpage", method = RequestMethod.GET)
  public String fowardTermReport(Model model) {
    Teacher teacher = getSubjectSpecifiedTeacher();
    model.addAttribute("subject", teacher.getSubject());
    // 生成各年级信息
    List<Map<String, Object>> batchClazzsList = this.getClazzInfo(teacher.getId());
    //学年上学期月份范围
    Set<Integer> lastTerm = new HashSet<>(Arrays.asList(9, 10, 11, 12, 1, 2));
    //学年下学期月份范围
    Set<Integer> nextTerm = new HashSet<>(Arrays.asList(3, 4, 5, 6, 7, 8));
    // 1月和2月跨年且属于上学期
    Set<Integer> diffMonth = new HashSet<>(Arrays.asList(1, 2));
    List<Map<String, Object>> dateList = new LinkedList<>();
    Calendar cal = Calendar.getInstance();
    int currentYear = cal.get(Calendar.YEAR);
    int currentMonth = cal.get(Calendar.MONTH) + 1;
    int currentTermId = lastTerm.contains(currentMonth) ? 0 : 1;
    //学期报告是从2016学年开始
    for (int i = 2016; i <= currentYear; i++) {
      Map<String, Object> mapFirst = new LinkedHashMap<>();
      mapFirst.put("dateRange", i + "_" + 0);
      mapFirst.put("title", i + "年8月-" + (i + 1) + "年2月");
      mapFirst.put("name", i + "年8月-" + (i + 1) + "年2月");
      Boolean active = false;
      if (diffMonth.contains(currentMonth)) {
        currentYear = currentYear - 1;
      }
      if (i == currentYear && currentTermId == 0) {
        active = true;
      }
      mapFirst.put("active", active);
      dateList.add(mapFirst);

      Map<String, Object> mapSecond = new LinkedHashMap<>();
      mapSecond.put("dateRange", i + "_" + 1);
      mapSecond.put("title", (i + 1) + "年2月-" + (i + 1) + "年8月");
      mapSecond.put("name", (i + 1) + "年2月-" + (i + 1) + "年8月");
      active = false;
      if (diffMonth.contains(currentMonth)) {
        currentYear = currentYear + 1;
      }
      if (i == currentYear - 1 && currentTermId == 1) {
        active = true;
      }
      mapSecond.put("active", active);
      dateList.add(mapSecond);
    }
    model.addAttribute("batchclazzs", JsonUtils.toJson(batchClazzsList))
        .addAttribute("dateList", JsonUtils.toJson(dateList));
    return "teacherv3/goal/termreport";
  }

  /**
   * 单元报告
   */
  @RequestMapping(value = "unitreport.vpage", method = RequestMethod.GET)
  public String fowardUnitReport(Model model) {
    Teacher teacher = getSubjectSpecifiedTeacher();
    model.addAttribute("subject", teacher.getSubject());
    // 生成各年级信息
    List<Map<String, Object>> batchClazzList = this.getClazzInfo(teacher.getId());
    model.addAttribute("batchclazzs", JsonUtils.toJson(batchClazzList));
    return "teacherv3/goal/unitreport";
  }

  /**
   * 学期报告页面
   */
  @RequestMapping(value = "report/term.vpage", method = {RequestMethod.GET, RequestMethod.POST})
  @ResponseBody
  public MapMessage getTermReport() {
    String dateRange = getRequestString("dateRange"); //yearId_termId
    String[] dateRanges = dateRange.split("_");
    String yearId = "";//年份
    String termId = "";//学期，0：上学期，1：下学期
    if (dateRanges.length == 2) {
      yearId = dateRanges[0];
      termId = dateRanges[1];
    }
    String groupId = getRequestString("groupId");//班组id
    String subjectName = getRequestString("subject");//学科名称
    if (StringUtils.isAnyBlank(yearId, termId, groupId, subjectName)) {
      return MapMessage.errorMessage("yearId or termId or groupId or subjectName is null");
    }
    try {
      TermReportBO termReportBO = this.getTermReportInfo(SafeConverter.toInt(yearId), SafeConverter.toInt(termId),
          SafeConverter.toInt(groupId), subjectName);
      return MapMessage.successMessage().add("data", termReportBO);
    } catch (Exception e) {
      logger.error(getClass().getName() + e.getMessage(), e);
      return MapMessage.errorMessage();
    }
  }

  /**
   * 单元报告页面
   */
  @RequestMapping(value = "report/unit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
  @ResponseBody
  public MapMessage getUnitReport() {
    String groupId = getRequestString("groupId");
    String unitId = getRequestString("unitId");
    if (StringUtils.isAnyBlank(groupId, unitId)) {
      return MapMessage.errorMessage("groupId or unitId is null");
    }
    try {
      return MapMessage.successMessage().add("data", this.getUnitReportInfo(groupId, unitId));
    } catch (Exception e) {
      logger.error(getClass().getName() + e.getMessage(), e);
      return MapMessage.errorMessage();
    }
  }

  /**
   * 学期报告下载
   *
   * @param response
   */
  @RequestMapping(value = "report/downloadHomeworkTermReport.vpage", method = RequestMethod.GET)
  public void downloadHomeworkTermReport(HttpServletResponse response) {
    String dateRange = getRequestString("dateRange"); //yearId_termId
    String[] dateRanges = dateRange.split("_");
    String yearId = "";//年份
    String termId = "";//学期，0：上学期，1：下学期
    if (dateRanges.length == 2) {
      yearId = dateRanges[0];
      termId = dateRanges[1];
    }
    String groupId = getRequestString("groupId");//班组id
    String subjectName = getRequestString("subject");//学科名称
    if (StringUtils.isAnyBlank(yearId, termId, groupId, subjectName)) {
      return;
    }
    try {
      TermReportBO termReportBO = getTermReportInfo(SafeConverter.toInt(yearId), SafeConverter.toInt(termId),
          SafeConverter.toInt(groupId), subjectName);
      HSSFWorkbook hSSFWorkbook = this.getTermReportExcel(termReportBO);
      @Cleanup ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      assert hSSFWorkbook != null;
      hSSFWorkbook.write(byteArrayOutputStream);
      byteArrayOutputStream.flush();
      String className = this.getClassName(SafeConverter.toLong(groupId));
      String termName = yearId + "-" + (SafeConverter.toInt(yearId) + 1) + (termId.equals("0") ? "上学期" : "下学期");
      String filename = "学期报告-" + className + "-" + termName + ".xls";//表格名“学期报告-x年级x班-学期名”
      HttpRequestContextUtils.currentRequestContext().downloadFile(
          filename,
          "application/vnd.ms-excel",
          byteArrayOutputStream.toByteArray());
    } catch (Exception e) {
      try {
        response.getWriter().write("不能下载");
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      } catch (IOException ex) {
        logger.error(String.format("download exception! groupId=%s,yearId=%s,termId=%s", groupId, yearId, termId), ex);
      }
    }
  }

  /**
   * 单元报告下载
   *
   * @param response
   */
  @RequestMapping(value = "report/downloadHomeworkUnitReport.vpage", method = RequestMethod.GET)
  public void downloadHomeworkUnitReport(HttpServletResponse response) {
    String classId = getRequestString("clazzId");//班级id
    String groupId = getRequestString("groupId");//班组id
    String unitId = getRequestString("unitId");//单元id
    if (StringUtils.isAnyBlank(groupId, unitId)) {
      return;
    }
    try {
      UnitReportBO unitReportBO = getUnitReportInfo(groupId, unitId);
      List<StudentUnitReportBO> studentUnitReportBOList = unitReportBO.getStudentUnitReportBOList();
      HSSFWorkbook hSSFWorkbook = this.getUnitReportExcel(studentUnitReportBOList);
      @Cleanup ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      assert hSSFWorkbook != null;
      hSSFWorkbook.write(byteArrayOutputStream);
      byteArrayOutputStream.flush();
      String className = this.getClassName(SafeConverter.toLong(groupId));
      Map<Long, Long> clazzIdGroupIdMap = new HashMap<>();
      clazzIdGroupIdMap.put(SafeConverter.toLong(classId), SafeConverter.toLong(groupId));
      String unitName = "";
      //所有班级的最新教材
      MapMessage mapMessage = newHomeworkContentServiceClient.loadClazzBook(getSubjectSpecifiedTeacher(), clazzIdGroupIdMap, false);
      if (mapMessage != null && mapMessage.containsKey("clazzBook")) {
        Map<String, Object> clazzBookMap = JsonUtils.fromJson(JsonUtils.toJson(mapMessage.get("clazzBook")));
        if (clazzBookMap != null && clazzBookMap.containsKey("unitList")) {
          //获取单元名称
          List<Map<String, Object>> unitMap = (List<Map<String, Object>>) clazzBookMap.get("unitList");
          if (CollectionUtils.isNotEmpty(unitMap)) {
            for (Map<String, Object> map : unitMap) {
              if (map != null && map.containsKey("unitId")) {
                if (map.get("unitId").equals(unitId)) {
                  unitName = map.get("cname").toString();
                  break;
                }
              }
            }
          }
        }
      }
      String filename = "单元报告-" + className + "-" + unitName + ".xls";//表格名“单元报告-x年级x班-单元名”
      HttpRequestContextUtils.currentRequestContext().downloadFile(
          filename,
          "application/vnd.ms-excel",
          byteArrayOutputStream.toByteArray());
    } catch (Exception e) {
      try {
        response.getWriter().write("不能下载");
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      } catch (IOException ex) {
        logger.error(String.format("download exception! groupId=%s,unitId=%s", groupId, unitId), ex);
      }
    }
  }

  /**
   * 获取所要下载的单元报告表格
   *
   * @return 返回对应的Excel
   */
  private HSSFWorkbook getUnitReportExcel(List<StudentUnitReportBO> studentUnitReportBOList) {
    // 创建Excel的工作书册 Workbook,对应到一个excel文档
    HSSFWorkbook wb = new HSSFWorkbook();
    // 创建Excel的工作sheet,对应到一个excel文档的tab
    HSSFSheet sheet = wb.createSheet("sheet1");
    // 设置excel列宽度
    sheet.setColumnWidth(5, 3000);
    // 创建单元格样式
    HSSFCellStyle style = wb.createCellStyle();
    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
    // 创建Excel的sheet的一行
    HSSFRow row = sheet.createRow(0);
    // 创建一个Excel的单元格
    HSSFCell cell;
    cell = row.createCell(0);
    cell.setCellStyle(style);
    cell.setCellValue("学生姓名");
    cell = row.createCell(1);
    cell.setCellStyle(style);
    cell.setCellValue("按时完成");
    cell = row.createCell(2);
    cell.setCellStyle(style);
    cell.setCellValue("补做");
    cell = row.createCell(3);
    cell.setCellStyle(style);
    cell.setCellValue("未做");
    cell = row.createCell(4);
    cell.setCellStyle(style);
    cell.setCellValue("平均分");
    cell = row.createCell(5);
    cell.setCellStyle(style);
    cell.setCellValue("总作业时长");
    cell = row.createCell(6);
    cell.setCellStyle(style);
    cell.setCellValue("出勤率(%)");

    int rowNum = 1;
    if (CollectionUtils.isNotEmpty(studentUnitReportBOList)) {
      for (StudentUnitReportBO studentUnitReportBO : studentUnitReportBOList) {
        row = sheet.createRow(rowNum++);// 生成行
        cell = row.createCell(0);// 生成第0个单元格
        cell.setCellStyle(style);
        cell.setCellValue(studentUnitReportBO.getStudentName());// 填充值
        cell = row.createCell(1);
        cell.setCellStyle(style);
        if (studentUnitReportBO.getOnTimeNum() != null) {
          cell.setCellValue(studentUnitReportBO.getOnTimeNum());
        } else {
          cell.setCellValue(0);
        }
        cell = row.createCell(2);
        cell.setCellStyle(style);
        if (studentUnitReportBO.getMakeupNum() != null) {
          cell.setCellValue(studentUnitReportBO.getMakeupNum());
        } else {
          cell.setCellValue(0);
        }
        cell = row.createCell(3);
        cell.setCellStyle(style);
        if (studentUnitReportBO.getNotDoneNum() != null) {
          cell.setCellValue(studentUnitReportBO.getNotDoneNum());
        } else {
          cell.setCellValue(0);
        }
        cell = row.createCell(4);
        cell.setCellStyle(style);
        if (studentUnitReportBO.getAvgScore() != null) {
          Integer avgScore = studentUnitReportBO.getAvgScore();
          cell.setCellValue(avgScore);
        } else {
          cell.setCellValue(0);
        }
        cell = row.createCell(5);
        cell.setCellStyle(style);
        if (studentUnitReportBO.getDoHomeworkDuration() != null) {
          cell.setCellValue(studentUnitReportBO.getDoHomeworkDuration());
        } else {
          cell.setCellValue(0);
        }
        cell = row.createCell(6);
        cell.setCellStyle(style);
        if (studentUnitReportBO.getAttendanceRate() != null) {
          cell.setCellValue(studentUnitReportBO.getAttendanceRate());
        } else {
          cell.setCellValue(0);
        }
      }
    }
    return wb;
  }

  /**
   * 获取所要下载的学期报告表格
   *
   * @return 返回对应的Excel
   */
  private HSSFWorkbook getTermReportExcel(TermReportBO termReportBO) {
    HSSFWorkbook workbook = new HSSFWorkbook();
    HSSFSheet sheet = workbook.createSheet();
    // 创建单元格样式
    HSSFCellStyle style = workbook.createCellStyle();
    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
    HSSFRow row = sheet.createRow(0);
    row.setHeight((short) 1000);
    sheet.setColumnWidth(0, 8000);
    HSSFCellStyle cellStyle = workbook.createCellStyle();
    cellStyle.setWrapText(true);
    cellStyle.setAlignment((short) 2);
    cellStyle.setVerticalAlignment((short) 1);
    HSSFCell cell = row.createCell(0);
    cell.setCellValue("姓名       完成次数              布置次数");
    cell.setCellStyle(cellStyle);
    //画线(由左上到右下的斜线)
    HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
    HSSFClientAnchor a = new HSSFClientAnchor(0, 0, 1000, 200, (short) 0, 0, (short) 0, 0);
    HSSFClientAnchor b = new HSSFClientAnchor(0, 0, 400, 255, (short) 0, 0, (short) 0, 0);
    HSSFSimpleShape shape1 = patriarch.createSimpleShape(a);
    HSSFSimpleShape shape2 = patriarch.createSimpleShape(b);
    shape1.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
    shape2.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
    shape1.setLineStyle(HSSFSimpleShape.LINESTYLE_SOLID);
    shape2.setLineStyle(HSSFSimpleShape.LINESTYLE_SOLID);

    List<MonthLayoutInfoBO> monthLayoutInfoList = termReportBO.getMonthLayoutInfoList();
    int columnNum = 0;
    if (CollectionUtils.isNotEmpty(monthLayoutInfoList)) {
      monthLayoutInfoList = monthLayoutInfoList.stream()
          .filter(e -> e.getMonth() != null)
          .sorted(Comparator.comparing(MonthLayoutInfoBO::getMonth))
          .collect(Collectors.toList());

      for (; columnNum < monthLayoutInfoList.size(); columnNum++) {
        List<String> layoutMonthList = new ArrayList<>();
        layoutMonthList.add(monthLayoutInfoList.get(columnNum).getMonth());
        String month = monthLayoutInfoList.get(columnNum).getMonth().split("-")[1];
        Integer layoutCount = monthLayoutInfoList.get(columnNum).getLayoutCount();
        cell = row.createCell(columnNum + 1);
        cell.setCellStyle(style);
        cell.setCellValue(month + "月 " + "(" + layoutCount + "次)");
      }

      cell = row.createCell(++columnNum);
      cell.setCellStyle(style);
      cell.setCellValue("累计 " + "(" + termReportBO.getTotalMonthLayoutTimes() + "次)");
      cell = row.createCell(++columnNum);
      cell.setCellStyle(style);
      cell.setCellValue("出勤率(%)");
      cell = row.createCell(++columnNum);
      cell.setCellStyle(style);
      cell.setCellValue("平均分");

      List<StudentTermReportBO> studentTermReportList = termReportBO.getStudentTermReportList();
      if (CollectionUtils.isNotEmpty(studentTermReportList)) {
        int rowNum = 1;
        for (StudentTermReportBO studentTermReportBO : studentTermReportList) {
          int colNum = 0;
          row = sheet.createRow(rowNum++);// 生成行
          cell = row.createCell(colNum++);// 生成第colNum个单元格
          cell.setCellStyle(style);//设置样式
          cell.setCellValue(studentTermReportBO.getStudentName());// 填充值
          List<MonthDoHomeworkBO> monthDoHomeworkBOList = studentTermReportBO.getMonthDoHomeworkBOList();
          if (CollectionUtils.isNotEmpty(monthDoHomeworkBOList)) {
            monthDoHomeworkBOList = monthDoHomeworkBOList.stream()
                .filter(e -> e.getMonth() != null)
                .sorted(Comparator.comparing(MonthDoHomeworkBO::getMonth))
                .collect(Collectors.toList());

            for (MonthDoHomeworkBO monthDoHomeworkBO : monthDoHomeworkBOList) {
              cell = row.createCell(colNum++);
              cell.setCellStyle(style);
              if (monthDoHomeworkBO.getCompleteCount() != null) {
                cell.setCellValue(monthDoHomeworkBO.getCompleteCount());
              } else {
                cell.setCellValue(0);
              }
            }
          }
          cell = row.createCell(colNum++);
          cell.setCellStyle(style);
          if (studentTermReportBO.getAttendTimes() != null) {
            cell.setCellValue(studentTermReportBO.getAttendTimes());
          } else {
            cell.setCellValue(0);
          }
          cell = row.createCell(colNum++);
          cell.setCellStyle(style);
          if (studentTermReportBO.getAttendanceRate() != null) {
            cell.setCellValue(studentTermReportBO.getAttendanceRate());
          } else {
            cell.setCellValue(0);
          }
          cell = row.createCell(colNum);
          cell.setCellStyle(style);
          if (studentTermReportBO.getAvgScore() != null) {
            cell.setCellValue(studentTermReportBO.getAvgScore());
          } else {
            cell.setCellValue(0);
          }
        }
      }
    }
    return workbook;
  }

  /**
   * 学期报告年份和学期
   * <p>
   * 之所以新增此接口是应PM王志的强烈需求，此处业务不能使用SchoolYear规定的学期格式。。。
   */
  @RequestMapping(value = "report/termdate.vpage", method = {RequestMethod.GET, RequestMethod.POST})
  @ResponseBody
  public MapMessage getTermDate() {
    String termId = getRequestString("termId");//学期，0：上学期，1：下学期
    if (StringUtils.isBlank(termId)) {
      termId = "0";
    }
    try {
      Calendar cal = Calendar.getInstance();
      int year = cal.get(Calendar.YEAR);
      int month = cal.get(Calendar.MONTH) + 1;
      List<Integer> dateList = new ArrayList<>(4);
      if (month >= 1 && month <= 8) {
        if (termId.equals("0")) {
          dateList = Arrays.asList(year, 2, year, 8);
        } else {
          dateList = Arrays.asList(year - 1, 8, year, 2);
        }
      }
      if (month > 8 && month <= 12) {
        if (termId.equals("0")) {
          dateList = Arrays.asList(year, 8, year + 1, 2);
        } else {
          dateList = Arrays.asList(year, 2, year, 8);
        }
      }
      return MapMessage.successMessage().add("data", dateList);
    } catch (Exception e) {
      logger.error(getClass().getName() + e.getMessage(), e);
      return MapMessage.errorMessage();
    }
  }

  /**
   * 获取当前老师的班级信息
   */
  private List<Map<String, Object>> getClazzInfo(Long teacherId) {
    List<Map<String, Object>> batchClazzList = getClazzList();
    List<Long> clazzIds = batchClazzList
        .stream()
        .map(clazz -> SafeConverter.toLong(clazz.get("id")))
        .collect(Collectors.toList());
    Map<Long, GroupMapper> groupMap = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacherId, clazzIds, false);
    List<Map<String, Object>> clazzList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(batchClazzList)) {
      for (Map<String, Object> map : batchClazzList) {
        Map<String, Object> classMap = new HashMap<>();
        if (MapUtils.isNotEmpty(map)) {
          Long classId = (Long) map.get("id");
          classMap.put("classId", classId);
          classMap.put("className", map.get("className"));
          if (MapUtils.isNotEmpty(groupMap)) {
            if (groupMap.get(classId) != null) {
              Long groupId = groupMap.get(classId).getId();
              classMap.put("groupId", groupId);
            }
          }
        }
        clazzList.add(classMap);
      }
    }
    return clazzList;
  }

  /**
   * 根据groupId获取班级名称
   */
  private String getClassName(Long groupId) {
    String clazzName = "";
    Map<Long, GroupMapper> groupMap = deprecatedGroupLoaderClient.loadGroups(Collections.singletonList(groupId), false);
    if (groupMap.get(groupId) != null) {
      Long clazzId = groupMap.get(groupId).getClazzId();
      Map<Long, Clazz> classMap = raikouSDK.getClazzClient()
          .getClazzLoaderClient()
          .loadClazzs(Collections.singleton(clazzId))
          .stream()
          .collect(Collectors.toMap(Clazz::getId, Function.identity()));
      Clazz clazz = classMap.get(clazzId);
      if (clazz != null) {
        clazzName = clazz.formalizeClazzName();
      }
    }
    return clazzName;
  }

  /**
   * 获取单元报告页面信息
   *
   * @param groupId 班组id
   * @param unitId  单元id
   */
  private UnitReportBO getUnitReportInfo(String groupId, String unitId) {
    UnitReportBO unitReportBO = new UnitReportBO();
    GroupUnitReportPackage groupUnitReportPackage = psrTermReportIPackageLoader.loadGroupUnitReportPackage(SafeConverter.toInt(groupId), unitId);
    if (groupUnitReportPackage != null) {
      //本单元共布置的作业次数
      if (groupUnitReportPackage.getLayoutHomeworkTimes() != null) {
        unitReportBO.setLayoutHomeworkTimes(groupUnitReportPackage.getLayoutHomeworkTimes());
      } else {
        unitReportBO.setLayoutHomeworkTimes(0);
      }
      List<StudentGroupUnitReport> studentGroupUnitReportList = groupUnitReportPackage.getStudentGroupUnitReport();
      List<Long> userIds = new ArrayList<>();
      //做过作业的学生
      Set<Long> doHomeworkStudentIdSet = new HashSet<>();
      if (CollectionUtils.isNotEmpty(studentGroupUnitReportList)) {
        studentGroupUnitReportList.forEach(studentGroupUnitReport ->
            doHomeworkStudentIdSet.add(SafeConverter.toLong(studentGroupUnitReport.getStudentId())));
      }
      //获取该班级的所有学生id列表
      Collection<Long> groupIdList = new ArrayList<>();
      Set<Long> StudentIdSet = new HashSet<>();
      groupIdList.add(SafeConverter.toLong(groupId));
      Map<Long, List<Long>> groupIdStudentIdMap = studentLoaderClient.loadGroupStudentIds(groupIdList);
      List<Long> studentIdList = groupIdStudentIdMap.get(SafeConverter.toLong(groupId));
      if (CollectionUtils.isNotEmpty(studentIdList)) {
        studentIdList.forEach(userIds::add);
        studentIdList.forEach(StudentIdSet::add);
      }
      //所有学生的<id,name>的哈希表
      Map<Long, User> userMap = raikouSystem.matrix()
          .getUserLoader()
          .loadUsersIncludeDisabled(userIds)
          .getUninterruptibly()
          .asMap();
      Map<Long, String> userIdNameMap = new HashMap<>(userIds.size());
      if (MapUtils.isNotEmpty(userMap)) {
        userIds.stream().filter(userMap::containsKey).forEach(userId -> userIdNameMap.put(userId, userMap.get(userId).fetchRealname()));
      }
      //从来没有做过作业的学生单元报告列表
      List<StudentUnitReportBO> undoHomeworkStudentUnitReportBOList = new ArrayList<>();
      StudentIdSet.stream()
          .filter(studentId -> !doHomeworkStudentIdSet.contains(studentId))
          .forEach(studentId -> {
            StudentUnitReportBO studentUnitReportBO = new StudentUnitReportBO();
            studentUnitReportBO.setStudentId(SafeConverter.toString(studentId));
            if (StringUtils.isBlank(userIdNameMap.get(studentId))) {
              studentUnitReportBO.setStudentName("无");
            } else {
              studentUnitReportBO.setStudentName(userIdNameMap.get(studentId));
            }
            studentUnitReportBO.setOnTimeNum(0);
            studentUnitReportBO.setMakeupNum(0);
            studentUnitReportBO.setNotDoneNum(unitReportBO.getLayoutHomeworkTimes());
            studentUnitReportBO.setAvgScore(0);
            studentUnitReportBO.setDoHomeworkDuration("0");
            studentUnitReportBO.setAttendanceRate(0.0);
            undoHomeworkStudentUnitReportBOList.add(studentUnitReportBO);
          });
      //做过作业的学生单元报告列表
      List<StudentUnitReportBO> studentUnitReportBOList = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(studentGroupUnitReportList)) {
        for (int i = 0; i < studentGroupUnitReportList.size(); i++) {
          StudentUnitReportBO studentUnitReportBO = new StudentUnitReportBO();
          if (studentGroupUnitReportList.get(i).getStudentId() != null) {
            //过滤大数据接口中的脏数据
            if (!StudentIdSet.contains(SafeConverter.toLong(studentGroupUnitReportList.get(i).getStudentId()))) {
              continue;
            }
            studentUnitReportBO.setStudentId(SafeConverter.toString(studentGroupUnitReportList.get(i).getStudentId()));
            String studentName = userIdNameMap.get(SafeConverter.toLong(studentGroupUnitReportList.get(i).getStudentId()));
            if (StringUtils.isBlank(studentName)) {
              studentUnitReportBO.setStudentName("无");
            } else {
              studentUnitReportBO.setStudentName(studentName);
            }
          }
          if (studentGroupUnitReportList.get(i).getOntime_num() != null) {
            studentUnitReportBO.setOnTimeNum(studentGroupUnitReportList.get(i).getOntime_num());
          }
          if (studentGroupUnitReportList.get(i).getMakeup_num() != null) {
            studentUnitReportBO.setMakeupNum(studentGroupUnitReportList.get(i).getMakeup_num());
          }
          if (studentGroupUnitReportList.get(i).getNotdone_num() != null) {
            studentUnitReportBO.setNotDoneNum(studentGroupUnitReportList.get(i).getNotdone_num());
          }
          if (studentGroupUnitReportList.get(i).getAvgscores() != null) {
            Double avgScore = studentGroupUnitReportList.get(i).getAvgscores();
            Integer avg = (int) Math.round(avgScore);
            studentUnitReportBO.setAvgScore(avg);
          }
          if (studentGroupUnitReportList.get(i).getDo_homework_duration() != null) {
            Double doHomeworkDuration = studentGroupUnitReportList.get(i).getDo_homework_duration();
            //时间格式：x小时x分，不到1分钟的向上取整计为1分钟
            Double seconds = doHomeworkDuration * 3600;
            int hour = (int) (seconds / 3600);
            int minute = (int) (seconds - hour * 3600) / 60;
            if (minute == 0) {
              ++minute;
            }
            studentUnitReportBO.setDoHomeworkDuration(hour + "小时" + minute + "分");
          }
          if (studentGroupUnitReportList.get(i).getAttendance_rate() != null) {
            Double attendanceRate = studentGroupUnitReportList.get(i).getAttendance_rate();
            attendanceRate = (double) Math.round(attendanceRate * 100);
            studentUnitReportBO.setAttendanceRate(attendanceRate);
          }
          studentUnitReportBOList.add(studentUnitReportBO);
        }
      }
      //追加没有作业信息的学生列表
      studentUnitReportBOList.addAll(undoHomeworkStudentUnitReportBOList);
      studentUnitReportBOList = studentUnitReportBOList.stream()
          .filter(e -> e.getAttendanceRate() != null)
          .sorted((e1, e2) -> e2.getAttendanceRate().compareTo(e1.getAttendanceRate()))
          .collect(Collectors.toList());
      unitReportBO.setStudentUnitReportBOList(studentUnitReportBOList);
    }
    return unitReportBO;
  }

  /**
   * 获取学期报告页面信息
   *
   * @param yearId      年份id
   * @param termId      学期id
   * @param groupId     班组id
   * @param subjectName 学科名称
   */
  private TermReportBO getTermReportInfo(Integer yearId, Integer termId, Integer groupId, String subjectName) {
    TermReportBO termReportBO = new TermReportBO();
    TermReportPackage termReportPackage = psrTermReportIPackageLoader.loadTermtReportPackage(yearId, termId, groupId, subjectName);
    if (termReportPackage != null) {
      List<MonthLayoutInfoBO> monthLayoutInfoBOList = new ArrayList<>();
      //计算所有月份老师累计布置的次数
      Integer totalMonthLayoutTimes = 0;
      if (termReportPackage.getMonthLayoutInfos() != null) {
        for (MonthLayoutInfo monthLayoutInfo : termReportPackage.getMonthLayoutInfos()) {
          MonthLayoutInfoBO monthLayoutInfoBO = new MonthLayoutInfoBO();
          if (monthLayoutInfo != null) {
            monthLayoutInfoBO.setMonth(monthLayoutInfo.getMonth());
            monthLayoutInfoBO.setLayoutCount(monthLayoutInfo.getLayout_count());
            monthLayoutInfoBOList.add(monthLayoutInfoBO);
            totalMonthLayoutTimes += monthLayoutInfo.getLayout_count();
          }
        }
      }
      monthLayoutInfoBOList = monthLayoutInfoBOList.stream()
          .filter(e -> e.getMonth() != null)
          .sorted(Comparator.comparing(MonthLayoutInfoBO::getMonth))
          .collect(Collectors.toList());

      //按顺序保存每个月份的布置次数
      List<String> monthLayoutList = new ArrayList<>(monthLayoutInfoBOList.size());
      monthLayoutInfoBOList.forEach(monthLayoutInfoBO -> monthLayoutList.add(monthLayoutInfoBO.getMonth()));
      //做过作业的学生
      Collection<Long> userIds = new ArrayList<>();
      Set<Long> doHomeworkStudentIdSet = new HashSet<>();
      List<StudentTermReport> studentTermReportList = termReportPackage.getStudentTermReports();
      if (CollectionUtils.isNotEmpty(studentTermReportList)) {
        studentTermReportList.forEach(studentTermReport -> doHomeworkStudentIdSet.add(SafeConverter.toLong(studentTermReport.getStudentId())));
      }
      //获取该班级的所有学生id列表
      Collection<Long> groupIdList = new ArrayList<>();
      Set<Long> StudentIdSet = new HashSet<>();
      groupIdList.add(SafeConverter.toLong(groupId));
      Map<Long, List<Long>> groupIdStudentIdMap = studentLoaderClient.loadGroupStudentIds(groupIdList);
      List<Long> studentIdList = groupIdStudentIdMap.get(SafeConverter.toLong(groupId));
      if (CollectionUtils.isNotEmpty(studentIdList)) {
        studentIdList.forEach(userIds::add);
        studentIdList.forEach(StudentIdSet::add);
      }
      //所有学生的<id,name>的哈希表
      Map<Long, User> userMap = raikouSystem.matrix()
          .getUserLoader()
          .loadUsersIncludeDisabled(userIds)
          .getUninterruptibly()
          .asMap();
      Map<Long, String> userIdNameMap = new HashMap<>(userIds.size());
      if (MapUtils.isNotEmpty(userMap)) {
        userIds.stream()
            .filter(userMap::containsKey)
            .forEach(userId -> userIdNameMap.put(userId, userMap.get(userId).fetchRealname()));
      }
      //从来没有做过作业的学生学期报告列表
      List<StudentTermReportBO> undoHomeworkStudentTermReportBOList = new ArrayList<>();
      for (Long studentId : StudentIdSet) {
        if (!doHomeworkStudentIdSet.contains(studentId)) {
          StudentTermReportBO studentTermReportBO = new StudentTermReportBO();
          studentTermReportBO.setStudentId(SafeConverter.toString(studentId));
          if (StringUtils.isBlank(userIdNameMap.get(studentId))) {
            studentTermReportBO.setStudentName("无");
          } else {
            studentTermReportBO.setStudentName(userIdNameMap.get(studentId));
          }
          studentTermReportBO.setAttendanceRate(0.0);
          studentTermReportBO.setAttendTimes(0);
          studentTermReportBO.setAvgScore(0);
          List<MonthDoHomeworkBO> undoMonthDoHomeworkBOList = new ArrayList<>();
          for (String month : monthLayoutList) {
            MonthDoHomeworkBO monthDoHomeworkBO = new MonthDoHomeworkBO();
            monthDoHomeworkBO.setMonth(month);
            monthDoHomeworkBO.setCompleteCount(0);
            undoMonthDoHomeworkBOList.add(monthDoHomeworkBO);
          }
          studentTermReportBO.setMonthDoHomeworkBOList(undoMonthDoHomeworkBOList);
          undoHomeworkStudentTermReportBOList.add(studentTermReportBO);
        }
      }
      //做过作业的学生学期报告列表
      List<StudentTermReportBO> studentTermReportBOList = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(studentTermReportList)) {
        for (StudentTermReport studentTermReport : studentTermReportList) {
          if (studentTermReport != null) {
            StudentTermReportBO studentTermReportBO = new StudentTermReportBO();
            //过滤大数据接口中的脏数据
            if (!StudentIdSet.contains(SafeConverter.toLong(studentTermReport.getStudentId()))) {
              continue;
            }
            if (!StringUtils.isBlank(studentTermReport.getStudentId())) {
              studentTermReportBO.setStudentId(studentTermReport.getStudentId());
              String studentName = userIdNameMap.get(SafeConverter.toLong(studentTermReport.getStudentId()));
              if (StringUtils.isBlank(studentName)) {
                studentTermReportBO.setStudentName("无");
              } else {
                studentTermReportBO.setStudentName(studentName);
              }
            }
            if (studentTermReport.getAttendanceRate() != null) {
              Double attendanceRate = studentTermReport.getAttendanceRate();
              attendanceRate = (double) Math.round(attendanceRate * 100);
              studentTermReportBO.setAttendanceRate(attendanceRate);
            }
            if (studentTermReport.getAttendTimes() != null) {
              studentTermReportBO.setAttendTimes(studentTermReport.getAttendTimes());
            }
            if (studentTermReport.getAveScore() != null) {
              Double avgScore = studentTermReport.getAveScore();
              Integer avg = (int) Math.round(avgScore);
              studentTermReportBO.setAvgScore(avg);
            }
            List<MonthDoHomework> monthDoHomeworkList = studentTermReport.getHomeworkStatus();
            monthDoHomeworkList = monthDoHomeworkList.stream()
                .filter(e -> e.getMonth() != null)
                .sorted(Comparator.comparing(MonthDoHomework::getMonth))
                .collect(Collectors.toList());
            //每个学生的各个月份完成次数
            Map<String, Integer> monthDoHomeworkMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(monthDoHomeworkList)) {
              monthDoHomeworkList.stream()
                  .filter(monthDoHomework -> monthDoHomework.getMonth() != null && monthDoHomework.getComplete_count() != null)
                  .forEach(monthDoHomework -> monthDoHomeworkMap.put(monthDoHomework.getMonth(), monthDoHomework.getComplete_count()));
            }
            List<MonthDoHomeworkBO> monthDoHomeworkBOList = new ArrayList<>();
            //每个学生每个月的完成次数与所有月份的布置情况按顺序匹配，某个学生某个月份找不到数据则补零显示
            for (String month : monthLayoutList) {
              MonthDoHomeworkBO monthDoHomeworkBO = new MonthDoHomeworkBO();
              if (monthDoHomeworkMap.containsKey(month)) {
                monthDoHomeworkBO.setMonth(month);
                monthDoHomeworkBO.setCompleteCount(monthDoHomeworkMap.get(month));
              } else {
                monthDoHomeworkBO.setMonth(month);
                monthDoHomeworkBO.setCompleteCount(0);
              }
              monthDoHomeworkBOList.add(monthDoHomeworkBO);
            }
            studentTermReportBO.setMonthDoHomeworkBOList(monthDoHomeworkBOList);
            studentTermReportBOList.add(studentTermReportBO);
          }
        }
      }
      //追加没有作业信息的学生列表
      studentTermReportBOList.addAll(undoHomeworkStudentTermReportBOList);
      //按学生出勤率降序
      studentTermReportBOList = studentTermReportBOList.stream()
          .filter(e -> e.getAttendanceRate() != null)
          .sorted((e1, e2) -> e2.getAttendanceRate().compareTo(e1.getAttendanceRate()))
          .collect(Collectors.toList());
      termReportBO.setDateRange(yearId + "_" + termId);
      termReportBO.setGroupId(SafeConverter.toLong(groupId));
      termReportBO.setSubject(Subject.safeParse(subjectName));
      termReportBO.setMonthLayoutInfoList(monthLayoutInfoBOList);
      termReportBO.setStudentTermReportList(studentTermReportBOList);
      termReportBO.setTotalMonthLayoutTimes(totalMonthLayoutTimes);
    }
    return termReportBO;
  }
}
