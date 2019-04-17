package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseGroupService;
import com.voxlearning.utopia.agent.service.mobile.SchoolClueService;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yaguang.wang on 2016/6/13.
 */

@Controller
@RequestMapping("/workspace/kpiinfo")
@Slf4j
public class SchoolBasisInfoDailyController extends AbstractAgentController {

    private static final String SCHOOL_BASIS_INFO_DAILY_LOCATION = "/config/templates/school_basis_information_daily.xlsx";
    private static final FastDateFormat TIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");
    @Inject private SchoolClueService schoolClueService;
    @Inject private BaseGroupService baseGroupService;

    @RequestMapping(value = "schoolClue.vpage")
    public String schoolClueIndex() {
        return "workspace/schoolClue/index";
    }

    @RequestMapping(value = "downloadSchoolBasisInfo.vpage", method = RequestMethod.POST)
    void downloadSchoolBasisInfoDaily(HttpServletResponse response) {
        try {
            Date endTime = new Date();
            Date startTime = DateUtils.getDayStart(DateUtils.calculateDateDay(endTime, -1));
            String reqStartDate = getRequestString("startDate").replaceAll("-", "");
            String reqEndDate = getRequestString("endDate").replaceAll("-", "");
            if (StringUtils.isNoneBlank(reqStartDate) && StringUtils.isNoneBlank(reqEndDate)) {
                startTime = DateUtils.stringToDate(reqStartDate + " 00:00:00", "yyyyMMdd HH:mm:ss");
                endTime = DateUtils.stringToDate(reqEndDate + " 23:59:59", "yyyyMMdd HH:mm:ss");
            }

            List<CrmSchoolClue> crmSchoolClueList = schoolClueService.getSchoolClueByTime(startTime, endTime);
            XSSFWorkbook workbook = exportSchoolBasisInfoDaily(crmSchoolClueList);
            String filename = "市场人员学校基本信息下载-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception ex) {
            log.error("下载失败!", ex.getMessage(), ex);
        }
    }


    private XSSFWorkbook exportSchoolBasisInfoDaily(List<CrmSchoolClue> crmSchoolClueList) {
        Resource resource = new ClassPathResource(SCHOOL_BASIS_INFO_DAILY_LOCATION);
        if (!resource.exists()) {
            logger.error("exportSchoolTasks - template not exists");
            return null;
        }
        try {
            @Cleanup InputStream in = resource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            if (CollectionUtils.isNotEmpty(crmSchoolClueList)) {
                int index = 1;
                for (CrmSchoolClue crmSchoolClue : crmSchoolClueList) {
                    if (crmSchoolClue == null) {
                        continue;
                    }
                    Integer schoolPhase = crmSchoolClue.getSchoolPhase();
                    Integer externOrBoarder = crmSchoolClue.getExternOrBoarder();
                    if (schoolPhase == null || externOrBoarder == null) {
                        continue;
                    }
                    Integer cityCode = crmSchoolClue.getCityCode();
                    Integer countyCode = crmSchoolClue.getCountyCode();
                    Integer provinceCode = crmSchoolClue.getProvinceCode();
                    Long schoolId = crmSchoolClue.getSchoolId();
                    // FIXME todo
                    List<AgentGroup> agentGroup = new ArrayList<>(); //baseGroupService.getAgentGroupByCityCode(provinceCode, cityCode, countyCode, schoolId, schoolPhase);
                    XSSFRow row = sheet.createRow(index++);
                    createCell(row, 0, cellStyle, formatTime(crmSchoolClue.getCreateTime()));
                    createCell(row, 1, cellStyle, formatTime(crmSchoolClue.isApproved() ? crmSchoolClue.getReviewTime() : null));
                    createCell(row, 2, cellStyle, format(getAuthenticateStatus(crmSchoolClue)));
                    createCell(row, 3, cellStyle, format(StringUtils.join(agentGroup.stream().map(AgentGroup::getGroupName).collect(Collectors.toList()), ",")));
                    createCell(row, 4, cellStyle, format(StringUtils.join(agentGroup.stream().map(AgentGroup::getId).collect(Collectors.toList()), ",")));
                    createCell(row, 5, cellStyle, format(crmSchoolClue.getCityName()));
                    createCell(row, 6, cellStyle, format(cityCode));
                    createCell(row, 7, cellStyle, format(crmSchoolClue.getCountyName()));
                    createCell(row, 8, cellStyle, format(countyCode));
                    createCell(row, 9, cellStyle, format(schoolPhase == 1 ? "小学" : "中学"));
                    createCell(row, 10, cellStyle, format(crmSchoolClue.getSchoolId()));
                    createCell(row, 11, cellStyle, format(crmSchoolClue.getSchoolName()));
                    createCell(row, 12, cellStyle, format(getReviewStatus(crmSchoolClue)));
                    createCell(row, 13, cellStyle, format(crmSchoolClue.getExternOrBoarder() == 1 ? "走读" : crmSchoolClue.getExternOrBoarder() == 2 ? "寄宿" : "半走半宿"));
                    createCell(row, 14, cellStyle, format(crmSchoolClue.formatGradeDistribution()));
                    createCell(row, 15, cellStyle, format(crmSchoolClue.getGrade1StudentCount()));
                    createCell(row, 16, cellStyle, format(crmSchoolClue.getGrade2StudentCount()));
                    createCell(row, 17, cellStyle, format(crmSchoolClue.getGrade3StudentCount()));
                    createCell(row, 18, cellStyle, format(crmSchoolClue.getGrade4StudentCount()));
                    createCell(row, 19, cellStyle, format(crmSchoolClue.getGrade5StudentCount()));
                    createCell(row, 20, cellStyle, format(crmSchoolClue.getGrade6StudentCount()));
                    createCell(row, 21, cellStyle, format(crmSchoolClue.getGrade7StudentCount()));
                    createCell(row, 22, cellStyle, format(crmSchoolClue.getGrade8StudentCount()));
                    createCell(row, 23, cellStyle, format(crmSchoolClue.getGrade9StudentCount()));
                    createCell(row, 24, cellStyle, format(crmSchoolClue.getEnglishStartGrade()));
                    createCell(row, 25, cellStyle, format(StringUtils.join(crmSchoolClue.getBranchSchoolIds(), ",")));
                }
            }
            return workbook;
        } catch (Exception ex) {
            return null;
        }
    }

    private XSSFCell createCell(XSSFRow row, int index, XSSFCellStyle style, String value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(value);
        return cell;
    }

    private String formatTime(Date time) {
        return time == null ? "" : TIME_FORMAT.format(time);
    }


    private String format(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String getReviewStatus(CrmSchoolClue crmSchoolClue) {
        if (crmSchoolClue == null) {
            return "";
        }
        Integer authenticateType = crmSchoolClue.getAuthenticateType();
        if (authenticateType == null) {
            return "学校鉴定";
        }
        if (authenticateType == 1) {
            return "学校鉴定";
        } else return "信息完善";
    }

    private String getAuthenticateStatus(CrmSchoolClue crmSchoolClue) {
        if (crmSchoolClue == null) {
            return "";
        }
        Integer authenticateType = crmSchoolClue.getAuthenticateType();
        Integer status = crmSchoolClue.getStatus();
        Integer infoStatus = crmSchoolClue.getInfoStatus();
        if (authenticateType == null) {
            return getStatusStr(status);
        }
        if (authenticateType == 1) {
            return getStatusStr(status);
        } else return getStatusStr(infoStatus);
    }

    public String getStatusStr(Integer status) {
        switch (status) {
            case -1:
                return "已驳回";
            case 0:
                return "暂存";
            case 1:
                return "待审核";
            case 2:
                return "已通过";
        }
        return "暂存";
    }
}
