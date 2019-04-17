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

package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.lang.util.ZipUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.BulkAccountInfoMapper;
import com.voxlearning.utopia.agent.bean.BulkSchoolInfoMapper;
import com.voxlearning.utopia.agent.bean.ClazzReformInfoMapper;
import com.voxlearning.utopia.agent.bean.JoinClazzStudentInfoMapper;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.interceptor.AgentHttpRequestContext;
import com.voxlearning.utopia.agent.service.workspace.MarketToolService;
import com.voxlearning.utopia.agent.support.TeacherResourceDownloadHelper;
import com.voxlearning.utopia.data.DownloadContent;
import com.voxlearning.utopia.service.crm.api.bean.ImportKLXStudentInfo;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.agent.ImportKLXStudentsRecord;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.ImportKLXStudentsRecordLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NewbieUser;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * @author shiwei.liao
 * @since 2015/9/6.
 */

@Controller
@RequestMapping(value = "/workspace")
@Slf4j
public class MarketToolController extends AbstractAgentController {

    @Inject
    MarketToolService marketToolService;

    @Inject private TeacherResourceDownloadHelper teacherResourceDownloadHelper;
    @Inject private ImportKLXStudentsRecordLoaderClient importKLXStudentsRecordLoaderClient;
    @Inject private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;

    private final static String IMPORT_KLX_STUDENT = "/config/templates/import_klx_student.xlsx";
    private static final int BYTES_BUFFER_SIZE = 1024 * 8;
    @RequestMapping(value = "markettool/addstudentindex.vpage", method = RequestMethod.GET)
    public String addMoreStudents() {
        return "workspace/markettool/addstudentindex";
    }

    @RequestMapping(value = "markettool/addstudentconfirm.vpage", method = RequestMethod.POST)
    public String confirmStudents(HttpServletResponse response) {
        String sourceFile = upload("sourceExcelFile", "mtasfs");
        if (StringUtils.isBlank(sourceFile)) {
            getAlertMessageManager().addMessageError("请选择需要导入的EXCEL数据文件!");
            return "workspace/markettool/addstudentindex";
        }

        List<JoinClazzStudentInfoMapper> studentInfoList;
        try {
            if (sourceFile.toLowerCase().endsWith(".xls")) {
                studentInfoList = loadJoinClazzStudentsFromXls(sourceFile);
            } else {
                studentInfoList = loadJoinClazzStudentsFromXlsx(sourceFile);
            }

        } catch (Exception e) {
            logger.error("读取EXCEL数据文件失败，请确认文件内容格式是否正确!", e);
            getAlertMessageManager().addMessageError("读取EXCEL数据文件失败，请确认文件内容格式是否正确!");
            return "workspace/markettool/addstudentindex";
        }

        MapMessage operResult = marketToolService.bulkAddClazzStudents(studentInfoList, UserWebSource.crm_batch.getSource());
        if (!operResult.isSuccess()) {
            getAlertMessageManager().addMessageError("操作失败，" + operResult.getInfo());
            return "workspace/markettool/addstudentindex";
        }

        List<String> failedInfoList = (List<String>) operResult.get("failedInfo");
        Map<Long, List<Clazz>> teacherClazzData = (Map<Long, List<Clazz>>) operResult.get("teacherClazzData");
        Map<String, Collection<NewbieUser>> studentData = (Map<String, Collection<NewbieUser>>) operResult.get("studentData");

        asyncLogService.logBatchRegTeacher(getCurrentUser(), getRequest().getRequestURI(), "true", "操作者," + getCurrentUser().getRealName() + "id," + getCurrentUserId() + ",批量添加学生成功,文件地址:" + sourceFile);

        try {
            String fileName = String.format("download_%s.zip", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME));
            ZipArchiveOutputStream outputStream = getZipOutputStreamForDownloading(fileName);

            if (teacherClazzData != null && teacherClazzData.size() > 0) {
                for (Long teacherId : teacherClazzData.keySet()) {
                    TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
                    List<Clazz> teacherClazz = teacherClazzData.get(teacherId);
                    if (CollectionUtils.isEmpty(teacherClazz)) {
                        continue;
                    }
                    for (Clazz clazz : teacherClazz) {
                        DownloadContent downloadContent = teacherResourceDownloadHelper.downloadClazzStudents(teacherDetail, Collections.singletonList(clazz));
                        if (outputStream != null) {
                            ZipUtils.addZipEntry(outputStream, downloadContent.getFilename(), downloadContent.getContent());
                        }
                    }
                }
            }

            HSSFWorkbook hssfWorkbook = createJoinClazzStudentsResult(studentData, failedInfoList);
            String filename = "一起作业网市场后台系统-批量添加学生结果-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xls";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            if (outputStream != null) {
                ZipUtils.addZipEntry(outputStream, filename, outStream.toByteArray());
                outputStream.flush();
                outputStream.close();
            }

        } catch (Exception ex) {
            log.error("下载失败!", ex.getMessage(), ex);
        }

        return "workspace/markettool/addstudentindex";
    }

    @RequestMapping(value = "markettool/bulkaccountindex.vpage", method = RequestMethod.GET)
    public String bulkTeacherIndex() {
        return "workspace/markettool/bulkaccountindex";
    }

    @RequestMapping(value = "markettool/bulkaccountconfirm.vpage", method = RequestMethod.POST)
    public String bulkAccountConfrim(HttpServletResponse response) {
        String sourceFile = upload("sourceExcelFile", "mtasfs");
        if (StringUtils.isBlank(sourceFile)) {
            getAlertMessageManager().addMessageError("请选择需要导入的EXCEL数据文件!");
            return "workspace/markettool/bulkaccountindex";
        }

        List<BulkAccountInfoMapper> accountInfoList;
        try {
            if (sourceFile.toLowerCase().endsWith(".xls")) {
                accountInfoList = loadBulkAccountsFromXls(sourceFile);
            } else {
                accountInfoList = loadBulkAccountsFromXlsx(sourceFile);
            }
        } catch (Exception e) {
            logger.error("读取EXCEL数据文件失败，请确认文件内容格式是否正确!", e);
            getAlertMessageManager().addMessageError("读取EXCEL数据文件失败，请确认文件内容格式是否正确!");
            return "workspace/markettool/bulkaccountindex";
        }

        MapMessage operResult = marketToolService.bulkCreateAccount(accountInfoList, UserWebSource.crm_batch.getSource(), getCurrentUser().getRealName());
        if (!operResult.isSuccess()) {
            getAlertMessageManager().addMessageError("操作失败，" + operResult.getInfo());
            return "workspace/markettool/bulkaccountindex";
        }

        List<String> failedInfoList = (List<String>) operResult.get("failedInfo");
        List<TeacherDetail> teacherAccountList = (List<TeacherDetail>) operResult.get("teacherData");
        Map<Long, List<Clazz>> teacherClazzData = (Map<Long, List<Clazz>>) operResult.get("teacherClazzData");
        Map<String, Collection<NewbieUser>> studentData = (Map<String, Collection<NewbieUser>>) operResult.get("studentData");

        asyncLogService.logBatchRegTeacher(getCurrentUser(), getRequest().getRequestURI(), "true", "操作者," + getCurrentUser().getRealName() + "id," + getCurrentUserId() + ",批量添加学生成功,文件地址:" + sourceFile);

        try {
            String fileName = String.format("download_%s.zip", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME));
            ZipArchiveOutputStream outputStream = getZipOutputStreamForDownloading(fileName);

            if (teacherClazzData == null || teacherClazzData.size() == 0) {
                HSSFWorkbook hssfWorkbook = createBulkTeacherAccountResult(teacherAccountList, failedInfoList);
                String filename = "一起作业网市场后台系统-批量注册老师-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xls";
                @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                hssfWorkbook.write(outStream);
                outStream.flush();
                if (outputStream != null) {
                    ZipUtils.addZipEntry(outputStream, filename, outStream.toByteArray());
                    outputStream.flush();
                    outputStream.close();
                }
            } else {
                for (TeacherDetail teacherDetail : teacherAccountList) {
                    List<Clazz> teacherClazz = teacherClazzData.get(teacherDetail.getId());
                    if (CollectionUtils.isEmpty(teacherClazz)) {
                        continue;
                    }

                    for (Clazz clazz : teacherClazz) {
                        DownloadContent downloadContent = teacherResourceDownloadHelper.downloadClazzStudents(teacherDetail, Collections.singletonList(clazz));
                        if (outputStream != null) {
                            ZipUtils.addZipEntry(outputStream, downloadContent.getFilename(), downloadContent.getContent());
                        }
                    }
                }

                HSSFWorkbook hssfWorkbook = createBulkAccountResult(studentData, failedInfoList);
                String filename = "一起作业网市场后台系统-批量注册老师和学生-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xls";
                @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                hssfWorkbook.write(outStream);
                outStream.flush();
                if (outputStream != null) {
                    ZipUtils.addZipEntry(outputStream, filename, outStream.toByteArray());
                    outputStream.flush();
                    outputStream.close();
                }
            }
        } catch (Exception ex) {
            log.error("下载失败!", ex.getMessage(), ex);
        }

        return "workspace/markettool/addstudentindex";
    }

    @RequestMapping(value = "markettool/namelistindex.vpage", method = RequestMethod.GET)
    public String nameListIndex() {
        return "workspace/markettool/namelistindex";
    }

    @RequestMapping(value = "markettool/namelistdownload.vpage", method = RequestMethod.POST)
    public String downloadNameList() {
        String reqTeacherIds = getRequestString("teacherIds");
        if (StringUtils.isBlank(reqTeacherIds)) {
            getAlertMessageManager().addMessageError("请输入要下载名单的老师ID!");
            return "workspace/markettool/namelistindex";
        }

        String[] teacherIds = reqTeacherIds.split(",");
        Map<Long, List<Clazz>> teacherClass = new LinkedHashMap<>();
        Map<Long, TeacherDetail> teacherDetails = new LinkedHashMap<>();
        List<Long> groupSchoolList = baseOrgService.getManagedSchoolList(getCurrentUserId());

        for (String teacherId : teacherIds) {
            if (StringUtils.isBlank(teacherId)) {
                continue;
            }

            if (!NumberUtils.isNumber(teacherId)) {
                getAlertMessageManager().addMessageError("非法的老师ID:" + teacherId);
                return "workspace/markettool/namelistindex";
            }

            teacherId = teacherId.trim();

            TeacherDetail detail = null;
            // 支持手机号码和用户ID两种模式
            if (MobileRule.isMobile(teacherId)) {
                UserAuthentication ua = userLoaderClient.loadMobileAuthentication(teacherId, UserType.TEACHER);
                if (ua != null) {
                    detail = teacherLoaderClient.loadTeacherDetail(ua.getId());
                }
            } else {
                Long tid = ConversionUtils.toLong(teacherId);
                detail = teacherLoaderClient.loadTeacherDetail(ConversionUtils.toLong(tid));
            }

            if (detail == null || detail.isDisabledTrue()) {
                getAlertMessageManager().addMessageError("不存在的老师ID:" + teacherId);
                return "workspace/markettool/namelistindex";
            }
            teacherDetails.put(detail.getId(), detail);

            List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(detail.getId());
            if (CollectionUtils.isEmpty(clazzList)) {
                getAlertMessageManager().addMessageError("老师" + teacherId + "没有班级!");
                return "workspace/markettool/namelistindex";
            }

            teacherClass.put(detail.getId(), clazzList);

            // 权限检查
            Long teacherSchoolId = detail.getTeacherSchoolId();
            if (CollectionUtils.isNotEmpty(groupSchoolList) && !groupSchoolList.contains(teacherSchoolId)) {
                getAlertMessageManager().addMessageError("你不能下载老师" + teacherId + "的班级名单!");
                return "workspace/markettool/namelistindex";
            }
        }

        asyncLogService.logDownloadNameList(getCurrentUser(), getRequest().getRequestURI(), "true", "操作者," + getCurrentUser().getRealName() + "id," + getCurrentUserId() + "，下载老师学生名单,老师列表:" + reqTeacherIds);

        try {
            String fileName = String.format("download_%s.zip", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME));
            ZipArchiveOutputStream outputStream = getZipOutputStreamForDownloading(fileName);

            for (Long tid : teacherDetails.keySet()) {
                TeacherDetail detail = teacherDetails.get(tid);
                List<Clazz> clazzList = teacherClass.get(tid);
                for (Clazz clazz : clazzList) {
                    DownloadContent downloadContent = teacherResourceDownloadHelper.downloadClazzStudents(detail, Collections.singletonList(clazz));
                    ZipUtils.addZipEntry(outputStream, downloadContent.getFilename(), downloadContent.getContent());
                }

                // FIXME 加上EXCEL格式输出
                HSSFWorkbook hssfWorkbook = createTeacherStudentData(detail, clazzList);
                String filename = "teacher_student_" + detail.getId() + "_" + detail.fetchRealname() + ".xls";
                @Cleanup ByteArrayOutputStream excelStream = new ByteArrayOutputStream();
                hssfWorkbook.write(excelStream);
                excelStream.flush();
                ZipUtils.addZipEntry(outputStream, filename, excelStream.toByteArray());

                outputStream.flush();
            }

            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            log.error("下载失败!", ex.getMessage(), ex);
        }

        return "workspace/markettool/namelistindex";
    }

    @RequestMapping(value = "markettool/clazzreform.vpage", method = RequestMethod.GET)
    public String reformClazz() {
        return "workspace/markettool/clazzreformindex";
    }

    @RequestMapping(value = "markettool/clazzreformconfirm.vpage", method = RequestMethod.POST)
    public String reformClazzConfrim(HttpServletResponse response) {
        String sourceFile = upload("sourceExcelFile", "mtasfs");
        if (StringUtils.isBlank(sourceFile)) {
            getAlertMessageManager().addMessageError("请选择需要导入的EXCEL数据文件!");
            return "workspace/markettool/clazzreformindex";
        }

        List<ClazzReformInfoMapper> studentInfoList;
        try {
            if (sourceFile.toLowerCase().endsWith(".xls")) {
                studentInfoList = loadClazzReformInfoFromXls(sourceFile);
            } else {
                studentInfoList = loadClazzReformInfoFromXlsx(sourceFile);
            }

        } catch (Exception e) {
            logger.error("读取EXCEL数据文件失败，请确认文件内容格式是否正确!", e);
            getAlertMessageManager().addMessageError("读取EXCEL数据文件失败，请确认文件内容格式是否正确!");
            return "workspace/markettool/clazzreformindex";
        }

        MapMessage operResult = marketToolService.reformClazzStudents(studentInfoList);
        if (!operResult.isSuccess()) {
            getAlertMessageManager().addMessageError("操作失败，" + operResult.getInfo());
            return "workspace/markettool/clazzreformindex";
        }

        List<String> failedInfoList = (List<String>) operResult.get("failedInfo");
        Map<Long, List<Clazz>> teacherClazzData = (Map<Long, List<Clazz>>) operResult.get("teacherClazzData");
        Map<String, Collection<NewbieUser>> studentData = (Map<String, Collection<NewbieUser>>) operResult.get("studentData");

        asyncLogService.logBatchRegTeacher(getCurrentUser(), getRequest().getRequestURI(), "true", "操作者," + getCurrentUser().getRealName() + "id," + getCurrentUserId() + ",班级重组成功,文件地址:" + sourceFile);

        try {
            String fileName = String.format("download_%s.zip", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME));
            ZipArchiveOutputStream outputStream = getZipOutputStreamForDownloading(fileName);

            if (teacherClazzData != null && teacherClazzData.size() > 0) {
                for (Long teacherId : teacherClazzData.keySet()) {
                    TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
                    List<Clazz> teacherClazz = teacherClazzData.get(teacherId);
                    if (CollectionUtils.isEmpty(teacherClazz)) {
                        continue;
                    }
                    for (Clazz clazz : teacherClazz) {
                        DownloadContent downloadContent = teacherResourceDownloadHelper.downloadClazzStudents(teacherDetail, Collections.singletonList(clazz));
                        if (outputStream != null) {
                            ZipUtils.addZipEntry(outputStream, downloadContent.getFilename(), downloadContent.getContent());
                        }
                    }
                }
            }

            HSSFWorkbook hssfWorkbook = createJoinClazzStudentsResult(studentData, failedInfoList);
            String filename = "一起作业网市场后台系统-班级重组结果-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xls";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            if (outputStream != null) {
                ZipUtils.addZipEntry(outputStream, filename, outStream.toByteArray());
                outputStream.flush();
                outputStream.close();
            }

        } catch (Exception ex) {
            log.error("下载失败!", ex.getMessage(), ex);
        }

        return "workspace/markettool/addstudentindex";

    }


    public ZipArchiveOutputStream getZipOutputStreamForDownloading(String filename) throws IOException {
        getResponse().reset();
        filename = attachmentFilenameEncoding(filename, getRequest());
        getResponse().addHeader("Content-Disposition", "attachment;filename=" + filename);
        getResponse().setContentType("application/x-zip-compressed");

        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(getResponse().getOutputStream());
        zos.setEncoding("GBK");
        return zos;
    }

    public static String attachmentFilenameEncoding(String filename, HttpServletRequest request) {
        try {
            AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
            if (StringUtils.contains(request.getHeader(context.getHeaderMap().getOrDefault("user-agent", "User-Agent")), "MSIE")
                    || StringUtils.contains(request.getHeader(context.getHeaderMap().getOrDefault("user-agent", "User-Agent")), "Trident")) {
                // IE browser
                return new String(filename.getBytes("gbk"), "iso8859-1");
            } else {
                // non-IE browser
                return new String(filename.getBytes("utf-8"), "iso8859-1");
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException();
        }
    }


    @RequestMapping(value = "markettool/bulkschoolindex.vpage", method = RequestMethod.GET)
    public String bulkAddSchoolIndex() {
        return "workspace/markettool/bulkschoolindex";
    }

    @RequestMapping(value = "markettool/bulkschoolconfirm.vpage", method = RequestMethod.POST)
    public String confirmBulkAddSchool(HttpServletResponse response) {
        String sourceFile = upload("sourceExcelFile", "mtasfs");
        if (StringUtils.isBlank(sourceFile)) {
            getAlertMessageManager().addMessageError("请选择需要导入的EXCEL数据文件!");
            return "workspace/markettool/bulkschoolindex";
        }


        List<BulkSchoolInfoMapper> schoolInfoList;
        try {
            if (sourceFile.toLowerCase().endsWith(".xls")) {
                schoolInfoList = loadBulkSchoolInfoFromXls(sourceFile);
            } else {
                schoolInfoList = loadBulkSchoolInfoFromXlsx(sourceFile);
            }

        } catch (Exception e) {
            logger.error("读取EXCEL数据文件失败，请确认文件内容格式是否正确!", e);
            getAlertMessageManager().addMessageError("读取EXCEL数据文件失败，请确认文件内容格式是否正确!");
            return "workspace/markettool/bulkschoolindex";
        }

        MapMessage operResult = marketToolService.bulkAddSchool(schoolInfoList);
        if (!operResult.isSuccess()) {
            getAlertMessageManager().addMessageError("操作失败，" + operResult.getInfo());
            return "workspace/markettool/bulkschoolindex";
        }

        List<School> schoolList = (List<School>) operResult.get("schools");

        asyncLogService.logBatchRegSchool(getCurrentUser(), getRequest().getRequestURI(), "true", "操作者," + getCurrentUser().getRealName() + "id," + getCurrentUserId() + ",批量创建学校成功,文件地址:" + sourceFile);

        try {
            String fileName = String.format("download_%s.zip", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME));
            ZipArchiveOutputStream outputStream = getZipOutputStreamForDownloading(fileName);

            HSSFWorkbook hssfWorkbook = createBulkSchoolResult(schoolList);
            String filename = "一起作业网市场后台系统-批量添加学校结果-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xls";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            if (outputStream != null) {
                ZipUtils.addZipEntry(outputStream, filename, outStream.toByteArray());
                outputStream.flush();
                outputStream.close();
            }

        } catch (Exception ex) {
            log.error("下载失败!", ex.getMessage(), ex);
        }

        return "workspace/markettool/bulkschoolindex";
    }

    // ==============================================================================================================
    private List<JoinClazzStudentInfoMapper> loadJoinClazzStudentsFromXls(String excelFileUrl) throws Exception {
        URL url = new URL(excelFileUrl);
        HSSFWorkbook workbook = new HSSFWorkbook(url.openConnection().getInputStream());
        HSSFSheet sheet = workbook.getSheetAt(0);

        List<JoinClazzStudentInfoMapper> dataList = new ArrayList<>();
        int rowIndex = 1;
        while (true) {
            HSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }

            JoinClazzStudentInfoMapper mapper = new JoinClazzStudentInfoMapper();
            mapper.setSchoolId(HssfUtils.getLongCellValue(row.getCell(0)));
            mapper.setSchoolName(HssfUtils.getStringCellValue(row.getCell(1)));
            mapper.setTeacherMobileOrid(HssfUtils.getStringCellValue(row.getCell(2)));
            mapper.setTeacherName(HssfUtils.getStringCellValue(row.getCell(3)));
            mapper.setClazzLevel(HssfUtils.getIntCellValue(row.getCell(4)));
            mapper.setClazzName(HssfUtils.getStringCellValue(row.getCell(5)));
            mapper.setStudentName(HssfUtils.getStringCellValue(row.getCell(6)));
            mapper.setStudentMobile(HssfUtils.getStringCellValue(row.getCell(7)));

            if (mapper.isAllEmpty()) {
                break;
            }

            dataList.add(mapper);
            rowIndex++;
        }

        return dataList;
    }

    private List<JoinClazzStudentInfoMapper> loadJoinClazzStudentsFromXlsx(String excelFileUrl) throws Exception {
        URL url = new URL(excelFileUrl);
        XSSFWorkbook workbook = new XSSFWorkbook(url.openConnection().getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);

        List<JoinClazzStudentInfoMapper> dataList = new ArrayList<>();
        int rowIndex = 1;
        while (true) {
            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }

            JoinClazzStudentInfoMapper mapper = new JoinClazzStudentInfoMapper();
            mapper.setSchoolId(XssfUtils.getLongCellValue(row.getCell(0)));
            mapper.setSchoolName(XssfUtils.getStringCellValue(row.getCell(1)));
            mapper.setTeacherMobileOrid(XssfUtils.getStringCellValue(row.getCell(2)));
            mapper.setTeacherName(XssfUtils.getStringCellValue(row.getCell(3)));
            mapper.setClazzLevel(XssfUtils.getIntCellValue(row.getCell(4)));
            mapper.setClazzName(XssfUtils.getStringCellValue(row.getCell(5)));
            mapper.setStudentName(XssfUtils.getStringCellValue(row.getCell(6)));
            mapper.setStudentMobile(HssfUtils.getStringCellValue(row.getCell(7)));

            if (mapper.isAllEmpty()) {
                break;
            }

            dataList.add(mapper);
            rowIndex++;
        }

        return dataList;
    }

    private HSSFWorkbook createJoinClazzStudentsResult(Map<String, Collection<NewbieUser>> studentData, List<String> failedInfoList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFSheet hssfSheet = hssfWorkbook.createSheet("成功列表");
        hssfSheet.setColumnWidth(1, 8000);
        hssfSheet.setColumnWidth(2, 4000);
        hssfSheet.setColumnWidth(7, 3500);

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        Row firstRow = HssfUtils.createRow(hssfSheet, 0, 8, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "学校ID");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "学校名");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "老师学号");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "老师姓名");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "年级");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "班级名称");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "学生姓名");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "学生学号");
        HssfUtils.setCellValue(firstRow, 8, borderStyle, "学生密码");

        int rowNum = 1;
        if (studentData != null && studentData.size() > 0) {
            for (String studentKey : studentData.keySet()) {
                String[] studentKeys = studentKey.split("_");
                Long schoolId = ConversionUtils.toLong(studentKeys[0]);
                String schoolName = studentKeys[1];
                Long teacherId = ConversionUtils.toLong(studentKeys[2]);
                String teacherName = studentKeys[3];
                int classLevel = ConversionUtils.toInt(studentKeys[4]);
                String className = studentKeys[5];

                for (NewbieUser user : studentData.get(studentKey)) {
                    Row row = HssfUtils.createRow(hssfSheet, rowNum++, 8, borderStyle);
                    HssfUtils.setCellValue(row, 0, borderStyle, schoolId);
                    HssfUtils.setCellValue(row, 1, borderStyle, schoolName);
                    HssfUtils.setCellValue(row, 2, borderStyle, String.valueOf(teacherId));
                    HssfUtils.setCellValue(row, 3, borderStyle, teacherName);
                    HssfUtils.setCellValue(row, 4, borderStyle, classLevel);
                    HssfUtils.setCellValue(row, 5, borderStyle, className);
                    HssfUtils.setCellValue(row, 6, borderStyle, user.getUsername());
                    HssfUtils.setCellValue(row, 7, borderStyle, String.valueOf(user.getUserId()));
                    HssfUtils.setCellValue(row, 8, borderStyle, user.getPwd());
                }
            }
        }

        if (failedInfoList != null && failedInfoList.size() > 0) {
            hssfSheet = hssfWorkbook.createSheet("失败列表");
            firstRow = HssfUtils.createRow(hssfSheet, 0, 1, borderStyle);
            HssfUtils.setCellValue(firstRow, 0, borderStyle, "错误信息");

            rowNum = 1;
            for (String failedInfo : failedInfoList) {
                Row row = HssfUtils.createRow(hssfSheet, rowNum++, 1, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, failedInfo);
            }
        }

        return hssfWorkbook;
    }

    private List<BulkAccountInfoMapper> loadBulkAccountsFromXls(String excelFileUrl) throws Exception {
        URL url = new URL(excelFileUrl);
        HSSFWorkbook workbook = new HSSFWorkbook(url.openConnection().getInputStream());
        HSSFSheet sheet = workbook.getSheetAt(0);

        List<BulkAccountInfoMapper> dataList = new ArrayList<>();
        int rowIndex = 1;
        while (true) {
            HSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }

            BulkAccountInfoMapper mapper = new BulkAccountInfoMapper();
            mapper.setSchoolId(HssfUtils.getLongCellValue(row.getCell(0)));
            mapper.setSchoolName(HssfUtils.getStringCellValue(row.getCell(1)));
            mapper.setTeacherName(HssfUtils.getStringCellValue(row.getCell(2)));
            mapper.setSubject(HssfUtils.getStringCellValue(row.getCell(3)));
            mapper.setTeacherMobile(HssfUtils.getStringCellValue(row.getCell(4)));
            mapper.setClazzLevel(HssfUtils.getIntCellValue(row.getCell(5)));
            mapper.setClazzName(HssfUtils.getStringCellValue(row.getCell(6)));
            mapper.setStudentName(HssfUtils.getStringCellValue(row.getCell(7)));

            if (mapper.isAllEmpty()) {
                break;
            }

            dataList.add(mapper);
            rowIndex++;
        }

        return dataList;
    }

    private List<BulkAccountInfoMapper> loadBulkAccountsFromXlsx(String excelFileUrl) throws Exception {
        URL url = new URL(excelFileUrl);
        XSSFWorkbook workbook = new XSSFWorkbook(url.openConnection().getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);

        List<BulkAccountInfoMapper> dataList = new ArrayList<>();
        int rowIndex = 1;
        while (true) {
            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }

            BulkAccountInfoMapper mapper = new BulkAccountInfoMapper();
            mapper.setSchoolId(XssfUtils.getLongCellValue(row.getCell(0)));
            mapper.setSchoolName(XssfUtils.getStringCellValue(row.getCell(1)));
            mapper.setTeacherName(XssfUtils.getStringCellValue(row.getCell(2)));
            mapper.setSubject(XssfUtils.getStringCellValue(row.getCell(3)));
            mapper.setTeacherMobile(XssfUtils.getStringCellValue(row.getCell(4)));
            mapper.setClazzLevel(XssfUtils.getIntCellValue(row.getCell(5)));
            mapper.setClazzName(XssfUtils.getStringCellValue(row.getCell(6)));
            mapper.setStudentName(XssfUtils.getStringCellValue(row.getCell(7)));

            if (mapper.isAllEmpty()) {
                break;
            }

            dataList.add(mapper);
            rowIndex++;
        }

        return dataList;
    }

    private HSSFWorkbook createBulkAccountResult(Map<String, Collection<NewbieUser>> studentData, List<String> failedInfoList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFSheet hssfSheet = hssfWorkbook.createSheet("成功列表");
        hssfSheet.setColumnWidth(1, 3000);
        hssfSheet.setColumnWidth(2, 4000);
        hssfSheet.setColumnWidth(8, 3000);

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        Row firstRow = HssfUtils.createRow(hssfSheet, 0, 9, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "老师ID");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "老师姓名");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "老师手机");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "老师密码");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "年级");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "班级ID");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "班级名称");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "学生姓名");
        HssfUtils.setCellValue(firstRow, 8, borderStyle, "学生ID");
        HssfUtils.setCellValue(firstRow, 9, borderStyle, "学生密码");

        int rowNum = 1;
        if (studentData != null && studentData.size() > 0) {
            for (String studentKey : studentData.keySet()) {
                String[] studentKeys = studentKey.split("_");
                Long teacherId = ConversionUtils.toLong(studentKeys[0]);
                String teacherName = studentKeys[1];
                String teacherMobile = studentKeys[2];
                String teacherPassword = "";
                int classLevel = ConversionUtils.toInt(studentKeys[3]);
                String className = studentKeys[4];
                Long clazzId = ConversionUtils.toLong(studentKeys[5]);

                for (NewbieUser user : studentData.get(studentKey)) {
                    Row row = HssfUtils.createRow(hssfSheet, rowNum++, 9, borderStyle);
                    HssfUtils.setCellValue(row, 0, borderStyle, String.valueOf(teacherId));
                    HssfUtils.setCellValue(row, 1, borderStyle, teacherName);
                    HssfUtils.setCellValue(row, 2, borderStyle, teacherMobile);
                    HssfUtils.setCellValue(row, 3, borderStyle, teacherPassword);
                    HssfUtils.setCellValue(row, 4, borderStyle, classLevel);
                    HssfUtils.setCellValue(row, 5, borderStyle, String.valueOf(clazzId));
                    HssfUtils.setCellValue(row, 6, borderStyle, className);
                    HssfUtils.setCellValue(row, 7, borderStyle, user.getUsername());
                    HssfUtils.setCellValue(row, 8, borderStyle, String.valueOf(user.getUserId()));
                    HssfUtils.setCellValue(row, 9, borderStyle, user.getPwd());
                }
            }
        }

        if (failedInfoList != null && failedInfoList.size() > 0) {
            hssfSheet = hssfWorkbook.createSheet("失败列表");
            firstRow = HssfUtils.createRow(hssfSheet, 0, 1, borderStyle);
            HssfUtils.setCellValue(firstRow, 0, borderStyle, "错误信息");

            rowNum = 1;
            for (String failedInfo : failedInfoList) {
                Row row = HssfUtils.createRow(hssfSheet, rowNum++, 1, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, failedInfo);
            }
        }

        return hssfWorkbook;
    }

    private HSSFWorkbook createBulkTeacherAccountResult(List<TeacherDetail> teacherData, List<String> failedInfoList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFSheet hssfSheet = hssfWorkbook.createSheet("成功列表");
        hssfSheet.setColumnWidth(1, 3000);
        hssfSheet.setColumnWidth(2, 4000);

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        Row firstRow = HssfUtils.createRow(hssfSheet, 0, 3, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "老师ID");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "老师姓名");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "老师手机");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "老师密码");

        int rowNum = 1;
        if (teacherData != null && teacherData.size() > 0) {
            for (TeacherDetail teacherDetail : teacherData) {
                Row row = HssfUtils.createRow(hssfSheet, rowNum++, 3, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, String.valueOf(teacherDetail.getId()));
                HssfUtils.setCellValue(row, 1, borderStyle, teacherDetail.fetchRealname());
                // FIXME 20171001
                // HssfUtils.setCellValue(row, 2, borderStyle, teacherDetail.getProfile().getSensitiveMobile());
                HssfUtils.setCellValue(row, 3, borderStyle, "");
            }
        }

        if (failedInfoList != null && failedInfoList.size() > 0) {
            hssfSheet = hssfWorkbook.createSheet("失败列表");
            firstRow = HssfUtils.createRow(hssfSheet, 0, 1, borderStyle);
            HssfUtils.setCellValue(firstRow, 0, borderStyle, "错误信息");

            rowNum = 1;
            for (String failedInfo : failedInfoList) {
                Row row = HssfUtils.createRow(hssfSheet, rowNum++, 1, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, failedInfo);
            }
        }

        return hssfWorkbook;
    }

    private List<ClazzReformInfoMapper> loadClazzReformInfoFromXls(String excelFileUrl) throws Exception {
        URL url = new URL(excelFileUrl);
        HSSFWorkbook workbook = new HSSFWorkbook(url.openConnection().getInputStream());
        HSSFSheet sheet = workbook.getSheetAt(0);

        List<ClazzReformInfoMapper> dataList = new ArrayList<>();
        int rowIndex = 1;
        while (true) {
            HSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }

            ClazzReformInfoMapper mapper = new ClazzReformInfoMapper();
            mapper.setSchoolId(HssfUtils.getLongCellValue(row.getCell(0)));
            mapper.setSchoolName(HssfUtils.getStringCellValue(row.getCell(1)));
            mapper.setTeacherMobileOrid(HssfUtils.getStringCellValue(row.getCell(2)));
            mapper.setTeacherName(HssfUtils.getStringCellValue(row.getCell(3)));
            mapper.setClazzLevel(HssfUtils.getIntCellValue(row.getCell(4)));
            mapper.setClazzName(HssfUtils.getStringCellValue(row.getCell(5)));
            mapper.setStudentName(HssfUtils.getStringCellValue(row.getCell(6)));

            if (mapper.isAllEmpty()) {
                break;
            }

            dataList.add(mapper);
            rowIndex++;
        }

        return dataList;
    }

    private List<ClazzReformInfoMapper> loadClazzReformInfoFromXlsx(String excelFileUrl) throws Exception {
        URL url = new URL(excelFileUrl);
        XSSFWorkbook workbook = new XSSFWorkbook(url.openConnection().getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);

        List<ClazzReformInfoMapper> dataList = new ArrayList<>();
        int rowIndex = 1;
        while (true) {
            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }

            ClazzReformInfoMapper mapper = new ClazzReformInfoMapper();
            mapper.setSchoolId(XssfUtils.getLongCellValue(row.getCell(0)));
            mapper.setSchoolName(XssfUtils.getStringCellValue(row.getCell(1)));
            mapper.setTeacherMobileOrid(XssfUtils.getStringCellValue(row.getCell(2)));
            mapper.setTeacherName(XssfUtils.getStringCellValue(row.getCell(3)));
            mapper.setClazzLevel(XssfUtils.getIntCellValue(row.getCell(4)));
            mapper.setClazzName(XssfUtils.getStringCellValue(row.getCell(5)));
            mapper.setStudentName(XssfUtils.getStringCellValue(row.getCell(6)));

            if (mapper.isAllEmpty()) {
                break;
            }

            dataList.add(mapper);
            rowIndex++;
        }

        return dataList;
    }

    private HSSFWorkbook createTeacherStudentData(TeacherDetail detail, List<Clazz> clazzList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFSheet hssfSheet = hssfWorkbook.createSheet("老师学生数据");
        hssfSheet.setColumnWidth(1, 8000);
        hssfSheet.setColumnWidth(2, 4000);
        hssfSheet.setColumnWidth(7, 3500);

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        Row firstRow = HssfUtils.createRow(hssfSheet, 0, 8, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "学校ID");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "学校名");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "老师学号");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "老师姓名");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "年级");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "班级名称");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "学生姓名");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "学生学号");
        HssfUtils.setCellValue(firstRow, 8, borderStyle, "学生密码");

        int rowNum = 1;
        for (Clazz clazz : clazzList) {
            Long schoolId = detail.getTeacherSchoolId();
            String schoolName = detail.getTeacherSchoolName();
            Long teacherId = detail.getId();
            String teacherName = detail.fetchRealname();
            int classLevel = clazz.fetchClazzLevel().getLevel();
            String className = clazz.getClassName();

            GroupMapper mapper = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazz.getId(), true);

            for (GroupMapper.GroupUser user : mapper.getStudents()) {
                Row row = HssfUtils.createRow(hssfSheet, rowNum++, 8, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, schoolId);
                HssfUtils.setCellValue(row, 1, borderStyle, schoolName);
                HssfUtils.setCellValue(row, 2, borderStyle, String.valueOf(teacherId));
                HssfUtils.setCellValue(row, 3, borderStyle, teacherName);
                HssfUtils.setCellValue(row, 4, borderStyle, classLevel);
                HssfUtils.setCellValue(row, 5, borderStyle, className);
                HssfUtils.setCellValue(row, 6, borderStyle, user.getName());
                HssfUtils.setCellValue(row, 7, borderStyle, String.valueOf(user.getId()));
                HssfUtils.setCellValue(row, 8, borderStyle, "");
            }
        }

        return hssfWorkbook;
    }

    private HSSFWorkbook createClazzReformResult(Map<String, Collection<NewbieUser>> studentData, List<String> failedInfoList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFSheet hssfSheet = hssfWorkbook.createSheet("成功列表");
        hssfSheet.setColumnWidth(1, 8000);
        hssfSheet.setColumnWidth(2, 4000);
        hssfSheet.setColumnWidth(7, 3500);

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        Row firstRow = HssfUtils.createRow(hssfSheet, 0, 8, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "学校ID");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "学校名");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "老师学号");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "老师姓名");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "年级");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "班级名称");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "学生姓名");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "学生学号");
        HssfUtils.setCellValue(firstRow, 8, borderStyle, "学生密码");

        int rowNum = 1;
        if (studentData != null && studentData.size() > 0) {
            for (String studentKey : studentData.keySet()) {
                String[] studentKeys = studentKey.split("_");
                Long schoolId = ConversionUtils.toLong(studentKeys[0]);
                String schoolName = studentKeys[1];
                Long teacherId = ConversionUtils.toLong(studentKeys[2]);
                String teacherName = studentKeys[3];
                int classLevel = ConversionUtils.toInt(studentKeys[4]);
                String className = studentKeys[5];

                for (NewbieUser user : studentData.get(studentKey)) {
                    Row row = HssfUtils.createRow(hssfSheet, rowNum++, 8, borderStyle);
                    HssfUtils.setCellValue(row, 0, borderStyle, schoolId);
                    HssfUtils.setCellValue(row, 1, borderStyle, schoolName);
                    HssfUtils.setCellValue(row, 2, borderStyle, String.valueOf(teacherId));
                    HssfUtils.setCellValue(row, 3, borderStyle, teacherName);
                    HssfUtils.setCellValue(row, 4, borderStyle, classLevel);
                    HssfUtils.setCellValue(row, 5, borderStyle, className);
                    HssfUtils.setCellValue(row, 6, borderStyle, user.getUsername());
                    HssfUtils.setCellValue(row, 7, borderStyle, String.valueOf(user.getUserId()));
                    HssfUtils.setCellValue(row, 8, borderStyle, user.getPwd());
                }
            }
        }

        if (failedInfoList != null && failedInfoList.size() > 0) {
            hssfSheet = hssfWorkbook.createSheet("失败列表");
            firstRow = HssfUtils.createRow(hssfSheet, 0, 1, borderStyle);
            HssfUtils.setCellValue(firstRow, 0, borderStyle, "错误信息");

            rowNum = 1;
            for (String failedInfo : failedInfoList) {
                Row row = HssfUtils.createRow(hssfSheet, rowNum++, 1, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, failedInfo);
            }
        }

        return hssfWorkbook;
    }

    private List<BulkSchoolInfoMapper> loadBulkSchoolInfoFromXls(String excelFileUrl) throws Exception {
        URL url = new URL(excelFileUrl);
        HSSFWorkbook workbook = new HSSFWorkbook(url.openConnection().getInputStream());
        HSSFSheet sheet = workbook.getSheetAt(0);

        List<BulkSchoolInfoMapper> dataList = new ArrayList<>();
        int rowIndex = 1;
        while (true) {
            HSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }

            BulkSchoolInfoMapper mapper = new BulkSchoolInfoMapper();
            mapper.setRegionCode(HssfUtils.getIntCellValue(row.getCell(3)));
            mapper.setSchoolName(HssfUtils.getStringCellValue(row.getCell(5)));
            mapper.setShortName(HssfUtils.getStringCellValue(row.getCell(6)));
            mapper.setSchoolLevel(HssfUtils.getStringCellValue(row.getCell(7)));
            mapper.setSchoolType(HssfUtils.getStringCellValue(row.getCell(8)));
            mapper.setAuthState(HssfUtils.getStringCellValue(row.getCell(9)));
            mapper.setAuthSource(HssfUtils.getStringCellValue(row.getCell(10)));
            mapper.setVip(HssfUtils.getStringCellValue(row.getCell(11)));

            if (mapper.isAllEmpty()) {
                break;
            }

            dataList.add(mapper);
            rowIndex++;
        }

        return dataList;
    }

    private List<BulkSchoolInfoMapper> loadBulkSchoolInfoFromXlsx(String excelFileUrl) throws Exception {
        URL url = new URL(excelFileUrl);
        XSSFWorkbook workbook = new XSSFWorkbook(url.openConnection().getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);

        List<BulkSchoolInfoMapper> dataList = new ArrayList<>();
        int rowIndex = 1;
        while (true) {
            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }

            BulkSchoolInfoMapper mapper = new BulkSchoolInfoMapper();
            mapper.setRegionCode(XssfUtils.getIntCellValue(row.getCell(3)));
            mapper.setSchoolName(XssfUtils.getStringCellValue(row.getCell(5)));
            mapper.setShortName(XssfUtils.getStringCellValue(row.getCell(6)));
            mapper.setSchoolLevel(XssfUtils.getStringCellValue(row.getCell(7)));
            mapper.setSchoolType(XssfUtils.getStringCellValue(row.getCell(8)));
            mapper.setAuthState(XssfUtils.getStringCellValue(row.getCell(9)));
            mapper.setAuthSource(XssfUtils.getStringCellValue(row.getCell(10)));
            mapper.setVip(XssfUtils.getStringCellValue(row.getCell(11)));

            if (mapper.isAllEmpty()) {
                break;
            }

            dataList.add(mapper);
            rowIndex++;
        }

        return dataList;
    }

    private HSSFWorkbook createBulkSchoolResult(List<School> schoolList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFSheet hssfSheet = hssfWorkbook.createSheet("学校列表");
        hssfSheet.setColumnWidth(1, 8000);

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        Row firstRow = HssfUtils.createRow(hssfSheet, 0, 2, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "学校ID");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "学校名");

        if (schoolList != null && schoolList.size() > 0) {
            int rowNum = 1;
            for (School school : schoolList) {
                Row row = HssfUtils.createRow(hssfSheet, rowNum++, 2, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, school.getId());
                HssfUtils.setCellValue(row, 1, borderStyle, school.getCname());
            }
        }

        return hssfWorkbook;
    }

    /**
     * 批量导入快乐学学生账号
     * 界面 跳转
     * @param model
     * @return
     */
    @RequestMapping(value = "import/import_klxstudents_view.vpage", method = RequestMethod.GET)
    public String importKLXStudentsView(Model model){
        AuthCurrentUser acu = getCurrentUser();
        List<ImportKLXStudentsRecord> srList = importKLXStudentsRecordLoaderClient.findByOperatorIdAndSourceType(acu.getUserId(), SystemPlatformType.AGENT.name());
        srList.stream().sorted((p1,p2)->Long.compare(p1.getCreateTime().getTime(),p2.getCreateTime().getTime()));
        model.addAttribute("record",srList);
        return "workspace/markettool/importklxstudentindex";
    }

    /**
     * 批量导入快乐学学生账号
     * 待 产品 整理后在调整url 吧
     * @return
     */
    @RequestMapping(value = "import/import_klxstudents.vpage", method = RequestMethod.POST)
    public String  importKLXStudents(Model model){
        String sourceFile = upload("sourceExcelFile", "mtasfs");
        if (StringUtils.isBlank(sourceFile)) {
            getAlertMessageManager().addMessageError("请选择需要导入的Excel数据文件!");
            return redirect("import_klxstudents_view.vpage");//待处理界面地址
        }
        if (!(sourceFile.toLowerCase().endsWith(".xls") || sourceFile.toLowerCase().endsWith(".xlsx"))) {
            getAlertMessageManager().addMessageError("目前仅支持Excel文件导入，请重新导入的Excel数据文件!");
            return redirect("import_klxstudents_view.vpage");//待处理界面地址
        }
        List <ImportKLXStudentInfo> klxStudentList ;
        try{
            if(sourceFile.toLowerCase().endsWith(".xls")){
                klxStudentList = excelFormatImportKLXStudentInfoXls(sourceFile);
            }else{
                klxStudentList = excelFormatImportKLXStudentInfoXlsx(sourceFile);
            }
            //数据是否存在
            if(CollectionUtils.isEmpty(klxStudentList)){//文案待处理
                getAlertMessageManager().addMessageError("excel为空");
                return redirect("import_klxstudents_view.vpage");
            }
            MapMessage operResult = marketToolService.batchAddKLXStudendts(klxStudentList,getCurrentUser());
            if(operResult.getSuccess()){
                asyncLogService.logBatchRegSchool(getCurrentUser(), getRequest().getRequestURI(), "true", "操作者," + getCurrentUser().getRealName() + "id," + getCurrentUserId() + ",批量快乐学账号成功,文件地址:" + sourceFile);
            }else{
                getAlertMessageManager().addMessageError("errorMessage","存在问题",operResult.get("errorMessage"));
            }
        }catch(Exception exp){
            logger.error("读取Excel数据文件失败，请确认文件内容格式是否正确!", exp);
            getAlertMessageManager().addMessageError("读取Excel数据文件失败，请确认文件内容格式是否正确!");
            return redirect("import_klxstudents_view.vpage");
        }

        return redirect("import_klxstudents_view.vpage");
    }

    /**
     * 将 excel 数据转化成 ImportKLXStudentInfoXls
     * @return
     */
    private  List <ImportKLXStudentInfo> excelFormatImportKLXStudentInfoXls(String excelFileUrl)throws Exception{
        URL url = new URL(excelFileUrl);
        HSSFWorkbook workbook = new HSSFWorkbook(url.openConnection().getInputStream());
        HSSFSheet sheet = workbook.getSheetAt(0);

        List<ImportKLXStudentInfo> dataList = new ArrayList<>();
        int rowIndex = 1;
        while (true) {
            HSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }
            ImportKLXStudentInfo mapper = new ImportKLXStudentInfo();
            mapper.setSchoolId(HssfUtils.getLongCellValue(row.getCell(0)));
            mapper.setSchoolName(HssfUtils.getStringCellValue(row.getCell(1)));
            mapper.setTeacherId(HssfUtils.getLongCellValue(row.getCell(2)));
            mapper.setTeacherName(HssfUtils.getStringCellValue(row.getCell(3)));
            mapper.setGradeLevel(HssfUtils.getIntCellValue(row.getCell(4)));
            mapper.setCalzzName(HssfUtils.getStringCellValue(row.getCell(5)));
            mapper.setStudentName(HssfUtils.getStringCellValue(row.getCell(6)));
            mapper.setStudentNumber(HssfUtils.getStringCellValue(row.getCell(7)));
            mapper.setRows(rowIndex);
            if (mapper.isAllEmpty()) {
                break;
            }

            dataList.add(mapper);
            rowIndex++;
        }
        return dataList;
    }

    /**
     * 将 excel 数据转化成 ImportKLXStudentInfoXlsx
     * @return
     */
    private  List <ImportKLXStudentInfo> excelFormatImportKLXStudentInfoXlsx(String excelFileUrl)throws Exception{
        URL url = new URL(excelFileUrl);
        XSSFWorkbook workbook = new XSSFWorkbook(url.openConnection().getInputStream());
        XSSFSheet  sheet = workbook.getSheetAt(0);

        List<ImportKLXStudentInfo> dataList = new ArrayList<>();
        int rowIndex = 1;
        while (true) {
            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }
            ImportKLXStudentInfo mapper = new ImportKLXStudentInfo();
            mapper.setSchoolId(XssfUtils.getLongCellValue(row.getCell(0)));
            mapper.setSchoolName(XssfUtils.getStringCellValue(row.getCell(1)));
            mapper.setTeacherId(XssfUtils.getLongCellValue(row.getCell(2)));
            mapper.setTeacherName(XssfUtils.getStringCellValue(row.getCell(3)));
            mapper.setGradeLevel(XssfUtils.getIntCellValue(row.getCell(4)));
            mapper.setCalzzName(XssfUtils.getStringCellValue(row.getCell(5)));
            mapper.setStudentName(XssfUtils.getStringCellValue(row.getCell(6)));
            mapper.setStudentNumber(XssfUtils.getStringCellValue(row.getCell(7)));
            mapper.setRows(rowIndex);
            if (mapper.isAllEmpty()) {
                break;
            }

            dataList.add(mapper);
            rowIndex++;
        }
        return dataList;
    }

    /**
     * 下载上传快乐学学生记录
     */
    @RequestMapping(value = "import/download_klxstudents.vpage", method = RequestMethod.GET)
    public void downloadKLXStudentInfoRecord(HttpServletResponse response){
        String  recordId = getRequestString("recordId");
        try{
            ImportKLXStudentsRecord sr = importKLXStudentsRecordLoaderClient.findByRecordId(recordId);
            HSSFWorkbook hssfWorkbook = convertToWorkbook(sr);
            String filename = "快乐学学生账号记录下载-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xls";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();

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
    private HSSFWorkbook  convertToWorkbook( ImportKLXStudentsRecord sr){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet schoolSheet = workbook.createSheet();
        schoolSheet.setColumnWidth(0, 5000);
        schoolSheet.setColumnWidth(1, 5000);
        schoolSheet.setColumnWidth(2, 5000);
        schoolSheet.setColumnWidth(3, 4000);
        schoolSheet.setColumnWidth(4, 5000);
        schoolSheet.setColumnWidth(5, 5000);
        schoolSheet.setColumnWidth(6, 10000);
        schoolSheet.setColumnWidth(7, 4000);
        schoolSheet.setColumnWidth(8, 10000);
        // 设置单元格边框样式
        HSSFCellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);
        Row firstRow = HssfUtils.createRow(schoolSheet, 0, 8, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "学校ID");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "学校名称");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "老师ID");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "老师姓名");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "年级");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "班级");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "学生姓名");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "学生学号");
        HssfUtils.setCellValue(firstRow, 8, borderStyle, "填涂号");
        int rowNum = 1;
        if(sr !=null && CollectionUtils.isNotEmpty(sr.getImportKLXStudentInfoList()) ){
            for(ImportKLXStudentInfo info : sr.getImportKLXStudentInfoList()){
                Row row = HssfUtils.createRow(schoolSheet, rowNum++, 8, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, info.getSchoolId());
                HssfUtils.setCellValue(row, 1, borderStyle, info.getSchoolName());
                HssfUtils.setCellValue(row, 2, borderStyle, info.getTeacherId());
                HssfUtils.setCellValue(row, 3, borderStyle, info.getTeacherName());
                HssfUtils.setCellValue(row, 4, borderStyle, info.getGradeLevel());
                HssfUtils.setCellValue(row, 5, borderStyle, info.getCalzzName());
                HssfUtils.setCellValue(row, 6, borderStyle, info.getStudentName());
                HssfUtils.setCellValue(row, 7, borderStyle, info.getStudentNumber());
                HssfUtils.setCellValue(row, 8, borderStyle, info.getScanNumber());
            }
        }

        return workbook;
    }
    @RequestMapping(value = "import/import_klxstudents_model.vpage", method = RequestMethod.GET)
    public void importKLXstudentModel(){
        try {
            Resource resource = new ClassPathResource(IMPORT_KLX_STUDENT);
            if (!resource.exists()) {
                logger.error("download import school dict template - template not exists ");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            String fileName = "快乐学学生账号导入模版.xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download import school dict Template - Excp : {};", e);
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
}
