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

package com.voxlearning.washington.controller.specialteacher;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.SpecialTeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SpecialTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.washington.mapper.specialteacher.base.ExcelExportData;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.WorkbookUtils;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教务老师基础服务类
 */
public abstract class AbstractSpecialTeacherController extends AbstractController {

    // in alphabetical order
    @Inject protected AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject protected AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject protected AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject protected GroupLoaderClient groupLoaderClient;
    @Inject protected NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject protected NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject protected SchoolLoaderClient schoolLoaderClient;
    @Inject protected SchoolExtServiceClient schoolExtServiceClient;
    @Inject protected SpecialTeacherLoaderClient specialTeacherLoaderClient;
    @Inject protected SpecialTeacherServiceClient specialTeacherServiceClient;
    @Inject protected UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject protected GlobalTagServiceClient globalTagServiceClient;

    ResearchStaff currentSpecialTeacher() {
        User user = currentUser();
        if (user == null || user.fetchUserType() != UserType.RESEARCH_STAFF) {
            return null;
        }
        ResearchStaff staff = researchStaffLoaderClient.loadResearchStaff(user.getId());
        if (staff == null || !staff.isAffairTeacher()) {
            return null;
        }
        return staff;
    }

    /**
     * 当前教务老师的学校ID
     */
    Long currentSchoolId() {
        ResearchStaffManagedRegion managed = specialTeacherLoaderClient.findSchoolId(currentUserId());
        return managed == null ? null : managed.getManagedRegionCode();
    }

    /**
     * 当前教务老师的学校
     */
    School currentSchool() {
        return schoolLoaderClient.getSchoolLoader()
                .loadSchool(currentSchoolId())
                .getUninterruptibly();
    }

    /**
     * 当前教务老师学校的学段
     */
    Ktwelve currentSchoolKtwelve() {
        School school = currentSchool();
        if (school == null) {
            return Ktwelve.UNKNOWN;
        }
        switch (SchoolLevel.safeParse(school.getLevel())) {
            case INFANT:
                return Ktwelve.INFANT;
            case JUNIOR:
                return Ktwelve.PRIMARY_SCHOOL;
            case MIDDLE:
                return Ktwelve.JUNIOR_SCHOOL;
            case HIGH:
                return Ktwelve.SENIOR_SCHOOL;
            default:
                return Ktwelve.PRIMARY_SCHOOL;
        }
    }

    String currentOperator() {
        User user = currentUser();
        if (user == null) {
            return null;
        }
        return "教务老师-" + user.fetchRealname() + "(" + user.getId() + ")";
    }

    public void logOperation() {
        User user = currentUser();
        if (user == null) {
            return;
        }
        HttpServletRequest request = getRequest();
        Map<String, String[]> paramMap = request.getParameterMap();
        Map<String, String> logInfo = new HashMap<>();
        logInfo.put("app", "special-teacher");
        logInfo.put("env", RuntimeMode.current().getStageMode());
        logInfo.put("userAgent", request.getHeader("User-Agent"));
        String relativeUriPath = request.getRequestURI().substring(request.getContextPath().length());
        logInfo.put("url", relativeUriPath);
        logInfo.put("requestMethod", request.getMethod());
        logInfo.put("requestTime", DateUtils.dateToString(new Date(), "yyyyMMddHHmmss.SSS"));
        logInfo.put("userId", ConversionUtils.toString(user.getId()));
        logInfo.put("userName", user.fetchRealname());
        StringBuilder params = new StringBuilder();
        paramMap.forEach((k, v) -> params.append(k).append("=").append(StringUtils.join(v, ",")).append("&"));
        logInfo.put("params", params.toString());
        LogCollector.info("special_teacher_operate_log", logInfo);
    }

    /**
     * 记录 UserServiceRecord
     */
    void logUserServiceRecord(Long userId, String operation, UserServiceRecordOperationType operationType, String content) {
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperatorId(currentOperator());
        userServiceRecord.setComments(operation);
        userServiceRecord.setOperationContent(content);
        userServiceRecord.setOperationType(operationType.name());

        userServiceClient.saveUserServiceRecord(userServiceRecord);
    }


    //---------------------------------------------------------------------------
    //----------------------       EXCEL 相关操作       --------------------------
    //---------------------------------------------------------------------------

    /**
     * 获取上传的Excel文件
     *
     * @param name 上传文件名
     */
    Workbook getRequestWorkbook(String name) {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            logger.error("getRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest request = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile file = request.getFile(name);
            if (file == null || file.isEmpty()) {
                logger.error("getRequestWorkbook - Empty MultipartFile with name['{}']", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("getRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return WorkbookFactory.create(in);
        } catch (Exception e) {
            logger.error("SpecialTeacherController getRequestWorkbook - Excp : {}", e);
            return null;
        }
    }

    /**
     * 初步检查EXCEL
     *
     * @param workbook excel文件
     * @param maximum  最大行数，默认100
     */
    MapMessage checkWorkbook(Workbook workbook, int maximum) {
        if (workbook == null) {
            return MapMessage.errorMessage("文档内容为空");
        }
        // 默认取第一个sheet
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage("文档内容为空");
        }
        // 默认第一行都是title
        if (sheet.getLastRowNum() < 1) {
            return MapMessage.errorMessage("文档内容为空");
        }
        if (sheet.getLastRowNum() > Integer.max(100, maximum)) {
            return MapMessage.errorMessage("文档人数异常，最多不超过" + maximum + "人");
        }
        return MapMessage.successMessage();
    }

    /**
     * 根据下载内容生成要下载的Excel文件，仅支持 xls
     */
    HSSFWorkbook createXlsExcelExportData(List<ExcelExportData> excelExportData) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        for (ExcelExportData exportData : excelExportData) {
            HSSFSheet sheet = hssfWorkbook.createSheet(exportData.getSheet());
            sheet.setActive(true);

            int rowNum = 0;
            int columns = exportData.getColumns();
            // 表头处理
            Row title = WorkbookUtils.createRow(sheet, rowNum++, columns, borderStyle);
            for (int i = 0; i < columns; ++i) {
                sheet.setColumnWidth(i, exportData.getWidth()[i]);
                if (exportData.highlight(i)) {
                    HSSFCellStyle newStyle = hssfWorkbook.createCellStyle();
                    // 设置单元格边框样式
                    newStyle.setBorderBottom(CellStyle.BORDER_THIN);
                    newStyle.setBorderTop(CellStyle.BORDER_THIN);
                    newStyle.setBorderLeft(CellStyle.BORDER_THIN);
                    newStyle.setBorderRight(CellStyle.BORDER_THIN);
                    newStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                    newStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                    WorkbookUtils.setCellValue(title, i, newStyle, exportData.getTitle()[i]);
                } else {
                    WorkbookUtils.setCellValue(title, i, borderStyle, exportData.getTitle()[i]);
                }
            }

            // 数据处理
            for (List<String> line : exportData.getData()) {
                Row row = WorkbookUtils.createRow(sheet, rowNum++, columns, borderStyle);
                for (int i = 0; i < columns; ++i) {
                    WorkbookUtils.setCellValue(row, i, borderStyle, line.get(i));
                }
            }
        }
        return hssfWorkbook;
    }

    Set<Subject> getSchoolValidSubjects() {
        Long schoolId = currentSchoolId();
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();

        Set<Subject> validSubjects = SchoolExtInfo.DefaultSubjects;
        if (schoolExtInfo != null) {
            validSubjects = schoolExtInfo.loadValidSubjects();
        }

        return validSubjects;
    }

    List<KeyValuePair<String, String>> validSubjectList() {
        Long schoolId = currentSchoolId();
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();

        Set<Subject> validSubject = schoolExtInfo == null ? SchoolExtInfo.DefaultSubjects : schoolExtInfo.loadValidSubjects();

        return SpecialTeacherConstants.validSubjects().stream()
                .filter(validSubject::contains)
                .map(sub -> new KeyValuePair<>(sub.name(), sub.getValue()))
                .collect(Collectors.toList());
    }

    KeyValuePair<String, String> checkValidSubject(String subject) {
        List<KeyValuePair<String, String>> subjects = this.validSubjectList();
        if (CollectionUtils.isNotEmpty(subjects)) {
            for (KeyValuePair<String, String> kv : subjects) {
                if (kv.getKey().equals(subject)) {
                    return kv;
                }
            }
        }
        return null;
    }

    ExcelExportData toExcelExportData(List<KlxStudent> klxStudents, String className, String sheetName, int scanNumberDigit) {

        String[] title = new String[]{"班级", "学生姓名", "校内学号", "阅卷机填涂号", "一起作业ID", "手机号"};
        int[] width = new int[]{6000, 6000, 6000, 6000, 6000, 6000};

        List<List<String>> data = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(klxStudents)) {
            for (KlxStudent klxStudent : klxStudents) {
                String name = StringUtils.isBlank(klxStudent.getName()) ? "未添加" : klxStudent.getName();
                String studentNumber = StringUtils.isBlank(klxStudent.getStudentNumber()) ? "未添加" : klxStudent.getStudentNumber();
                String scanNumber = StringUtils.isBlank(klxStudent.getScanNumber()) ? "未添加" : lpadZero(klxStudent.getScanNumber(), scanNumberDigit);
                String userId = klxStudent.isRealStudent() ? klxStudent.getA17id().toString() : "未注册";
                String mobile;
                if (klxStudent.isRealStudent()) {
                    mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(klxStudent.getA17id());
                } else {
                    mobile = sensitiveUserDataServiceClient.loadKlxStudentMobileObscured(klxStudent.getId());
                }
                data.add(Arrays.asList(className, name, studentNumber, scanNumber, userId, mobile));
            }
        }
        return new ExcelExportData(sheetName, title, width, data, 6);
    }

    protected void write(InputStream in, OutputStream out) throws Exception {
        int BUFFER_SIZE = 1024 * 8;
        byte[] buffer = new byte[BUFFER_SIZE];
        int size;
        while ((size = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }

    protected static String lpadZero(String number, Integer length) {
        if (NumberUtils.isDigits(number)) {
            return String.format("%0" + length + "d", SafeConverter.toLong(number));
        } else {
            return number;
        }
    }


//    public static void main(String[] args) {
//        String number = "1";
//        int length = 9;
//        System.out.println(lpadZero("1", length));
//        System.out.println(lpadZero("30001", length));
//        System.out.println(lpadZero("1234567", 5));
//    }

}
