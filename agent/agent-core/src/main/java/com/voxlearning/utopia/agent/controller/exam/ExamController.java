package com.voxlearning.utopia.agent.controller.exam;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.bean.exam.AgentExamGradeVO;
import com.voxlearning.utopia.agent.bean.exam.AgentExamSchoolVO;
import com.voxlearning.utopia.agent.bean.exam.AgentExamSubjectVO;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.exam.AgentLargeExamService;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.Cleanup;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chunlin.yu
 * @create 2018-03-14 12:35
 **/

@Controller
@RequestMapping("/exam/exammanage")
public class ExamController extends AbstractAgentController {

    @Inject private RaikouSystem raikouSystem;
    @Inject
    AgentLargeExamService agentLargeExamService;

    @RequestMapping(value = "manage.vpage", method = RequestMethod.GET)
    @OperationCode("2a836acaf1504254")
    public String manage(Model model) {
        return "exam/exam_manage";
    }

    @RequestMapping(value = "statistics.vpage", method = RequestMethod.GET)
    @OperationCode("2a836acaf1504254")
    public String statistics(Model model) {
        return "exam/exam_statistics";
    }

    /**
     * 取到所有的城市
     *
     * @return
     */
    @RequestMapping(value = "get_all_city.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAllCity() {
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadAllRegions();
        return MapMessage.successMessage().add("dataList", exRegionMap.values().stream().filter(item -> Objects.equals(item.fetchRegionType(), RegionType.CITY)).collect(Collectors.toList()));
    }

    /**
     * 检索
     *
     * @return
     */
    @RequestMapping(value = "search_exam.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchExam() {
        Integer cityCode = requestInteger("cityCode");
        Long userId = requestLong("userId");
        Long schoolId = requestLong("schoolId");
        String month = requestString("month");
        int type = getRequestInt("type");
        List<AgentExamSchoolVO> agentExamSchoolVOList = new ArrayList<>();
        if (type == 1) {
            if (null == month) {
                return MapMessage.errorMessage("请输入月份");
            }
            agentExamSchoolVOList = agentLargeExamService.searchExamStatistics(cityCode, userId, schoolId, month);
        } else if (type == 2) {
            if (null == cityCode && null == userId && null == schoolId && null == month) {
                return MapMessage.errorMessage("请输入月份");
            }
            agentExamSchoolVOList = agentLargeExamService.searchExamPost(cityCode, userId, schoolId, month);
        }
        return MapMessage.successMessage().add("dataList", agentExamSchoolVOList);
    }

    /**
     * 导出
     *
     * @return
     */
    @RequestMapping(value = "export_exam.vpage", method = RequestMethod.GET)
    public void exportExam(HttpServletResponse response) {
        Integer cityCode = requestInteger("cityCode");
        Long userId = requestLong("userId");
        Long schoolId = requestLong("schoolId");
        String month = requestString("month");
        int type = getRequestInt("type");
        try {
            List<AgentExamSchoolVO> agentExamSchoolVOList = new ArrayList<>();
            if (type == 1) {
                if (null == month) {
                    response.setHeader("Content-type", "text/html;charset=UTF-8");
                    response.getWriter().write("月份不能为空");
                    return;
                }
                agentExamSchoolVOList = agentLargeExamService.searchExamStatistics(cityCode, userId, schoolId, month);
            } else if (type == 2) {
                if (null == cityCode && null == userId && null == schoolId && null == month) {
                    response.setHeader("Content-type", "text/html;charset=UTF-8");
                    response.getWriter().write("月份不能为空");
                    return;
                }
                agentExamSchoolVOList = agentLargeExamService.searchExamPost(cityCode, userId, schoolId, month);
            }
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            generalSheet(workbook, agentExamSchoolVOList);
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            String nowTime = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME);
            String fileName = "大考统计数据下载" + nowTime + "-{}" + ".xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            workbook.dispose();
        } catch (Exception ex) {
            try {
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                response.getWriter().write("所查询的数据不存在");
            } catch (IOException ignored) {
            }
        }
    }

    private void generalSheet(SXSSFWorkbook workbook, List<AgentExamSchoolVO> agentExamSchoolVOList) {
        try {
            Sheet sheet = workbook.createSheet("大考统计");
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
            setCellValue(firstRow, 0, firstRowStyle, "月份");
            setCellValue(firstRow, 1, firstRowStyle, "城市");
            setCellValue(firstRow, 2, firstRowStyle, "学校ID");
            setCellValue(firstRow, 3, firstRowStyle, "学校名称");
            setCellValue(firstRow, 4, firstRowStyle, "负责人");
            setCellValue(firstRow, 5, firstRowStyle, "年级");
            setCellValue(firstRow, 6, firstRowStyle, "大考三科");
            setCellValue(firstRow, 7, firstRowStyle, "大考六科");


            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            if (CollectionUtils.isNotEmpty(agentExamSchoolVOList)) {
                Integer index = 1;
                for (AgentExamSchoolVO data : agentExamSchoolVOList) {
                    if (CollectionUtils.isNotEmpty(data.getExamGradeVOList())) {
                        for (int i = 0; i < data.getExamGradeVOList().size(); i++) {
                            Row row = sheet.createRow(index++);
                            AgentExamGradeVO gradeVO = data.getExamGradeVOList().get(i);

                            setCellValue(row, 0, cellStyle, data.getMonthStr());
                            setCellValue(row, 1, cellStyle, data.getCityName());
                            setCellValue(row, 2, cellStyle, data.getSchoolId());
                            setCellValue(row, 3, cellStyle, data.getSchoolName());
                            setCellValue(row, 4, cellStyle, data.getOwnerName());
                            setCellValue(row, 5, cellStyle, gradeVO.getGradeDes());
                            setCellValue(row, 6, cellStyle, gradeVO.getBgExamGte3StuCount());
                            setCellValue(row, 7, cellStyle, gradeVO.getBgExamGte6StuCount());
                        }
                    }
                }
            }
        } catch (Exception ex) {
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
     * 科目大考详情
     *
     * @return
     */
    @RequestMapping(value = "exam_subject_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage detail() {
        Long examSubjectId = requestLong("examSubjectId");
        if (null == examSubjectId) {
            return MapMessage.errorMessage("大考ID不正确");
        }
        AgentExamSubjectVO agentExamSubjectVO = agentLargeExamService.getAgentExamSubject(examSubjectId);
        return MapMessage.successMessage().add("data", agentExamSubjectVO);
    }


}
