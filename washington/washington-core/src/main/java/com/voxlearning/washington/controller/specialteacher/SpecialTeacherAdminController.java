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
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.ClazzConstants;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.data.NeonatalClazz;
import com.voxlearning.utopia.service.clazz.client.GroupServiceClient;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import com.voxlearning.washington.mapper.specialteacher.markstudent.MarkStudentData;
import com.voxlearning.washington.mapper.specialteacher.markstudent.MarkStudentResult;
import com.voxlearning.washington.mapper.specialteacher.studentimport.StudentImportData;
import com.voxlearning.washington.mapper.specialteacher.studentimport.StudentImportResult;
import com.voxlearning.washington.mapper.specialteacher.studentimport.StudentImportResultV2;
import com.voxlearning.washington.mapper.specialteacher.teacherclazz.TeacherClazzImportData;
import com.voxlearning.washington.mapper.specialteacher.teacherclazz.TeacherClazzImportResult;
import com.voxlearning.washington.mapper.specialteacher.teacherimoprt.TeacherImportData;
import com.voxlearning.washington.mapper.specialteacher.teacherimoprt.TeacherImportResult;
import com.voxlearning.washington.support.WorkbookUtils;
import lombok.Cleanup;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 教务老师-老师学生管理相关
 * FIXME 后续把老师学生相关的放到这里咯
 */
@Controller
@RequestMapping("/specialteacher/admin")
public class SpecialTeacherAdminController extends AbstractSpecialTeacherController {

    @Inject private RaikouSDK raikouSDK;

    @Inject
    private GroupServiceClient clazzGroupServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String teacherStudentManage() {
        if (currentSpecialTeacher() == null) {
            return "redirect: /";
        }
        return "specialteacherV2/teacherstudent";
    }
    //========================================= 教务-老师学生管理 =========================================

    @RequestMapping(value = "checkupdatestudentnum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkUpdateStudentNum() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        // 解析excel文档
        Workbook workbook = getRequestWorkbook("adjustExcel");
        // 文档校验
        if (workbook == null) {
            return MapMessage.errorMessage("文档解析错误");
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage("excel文件中的sheet内容不能为空");
        }
        int trLength = sheet.getLastRowNum() + 1;//总行数(包括首行)
        if (trLength < 2) {//excel第二行及以下的A列为空时，报错——excel为空
            return MapMessage.errorMessage("excel为空");
        }
        List<StudentImportData> importData = parseStudentImportData(sheet);

        if (importData.isEmpty()) {
            return MapMessage.errorMessage("excel为空");
        }
        if (trLength > 1001) {//excel内容>1001行时，报错——导入学生不能超过1000人
            return MapMessage.errorMessage("导入学生不能超过1000人");
        }
        // 先跑一遍校验
        // 学校下的所有班级
        Map<String, Clazz> classNameMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(currentSchoolId())
                .enabled().toList().stream()
                .collect(Collectors.toMap(c -> c.getClazzLevel().getDescription() + c.getClassName(), Function.identity(), (u, v) -> v));
        // 更新学生根据班级分组
        Map<String, List<StudentImportData>> classImportStudents = importData.stream()
                .collect(Collectors.groupingBy(StudentImportData::classNameKey));
        // 标识有失败的
        boolean failed = false;
        // 标识有成功的
        boolean success = false;
        for (Map.Entry<String, List<StudentImportData>> entry : classImportStudents.entrySet()) {
            if (!failed && success) {
                break;
            }
            List<StudentImportData> importStudents = entry.getValue();
            Clazz clazz = classNameMap.get(entry.getKey());
            if (clazz == null) {
                failed = true;
                continue;
            }
            // 该班级下所有的学生
            List<Group> groups = asyncGroupServiceClient.getAsyncGroupService().loadGroupsByClazzId(clazz.getId()).getUninterruptibly();
            Map<Long, List<KlxStudent>> klxStudentsMap = newKuailexueLoaderClient.loadKlxGroupStudents(groups.stream().map(Group::getId).collect(Collectors.toList()));
            List<KlxStudent> klxStudents = klxStudentsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            klxStudents = klxStudents.stream().collect(Collectors.toMap(KlxStudent::getId, Function.identity(), (u, v) -> u))
                    .values()
                    .stream()
                    .sorted((k1, k2) -> {
                        Long n1 = SafeConverter.toLong(k1.getStudentNumber());
                        Long n2 = SafeConverter.toLong(k2.getStudentNumber());
                        return Long.compare(n1, n2);
                    })
                    .collect(Collectors.toList());
            Map<String, List<KlxStudent>> klxStudentsNameMap = klxStudents.stream().collect(Collectors.groupingBy(KlxStudent::getName));
            Map<String, KlxStudent> klxStudentsNumMap = klxStudents.stream().collect(Collectors.toMap(KlxStudent::getStudentNumber, Function.identity(), (u, v) -> u));
            for (StudentImportData data : importStudents) {
                if (!failed && success) {
                    break;
                }
                // 忽略 姓名为空 或者 学号为空的记录
                if (StringUtils.isAnyBlank(data.getStudentName(), data.getStudentName())) {
                    continue;
                }
                List<KlxStudent> students = klxStudentsNameMap.get(data.getStudentName());
                if (students == null) {
                    failed = true;
                    continue;
                }
                if (students.size() > 1) {
                    failed = true;
                    continue;
                }
                if (klxStudentsNumMap.containsKey(data.getStudentNumber())) {
                    failed = true;
                    continue;
                }
                success = true;
            }
        }
        // 0全部失败 1部分成功 2全部成功
        return MapMessage.successMessage().add("result", ((failed && !success) ? 0 : (!failed && success) ? 2 : 1));
    }

    /**
     * 通过excel更新学生学号
     */
    @RequestMapping(value = "updatestudentnum.vpage", method = RequestMethod.POST)
    public void updateStudentNum() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return;
        }

        // 解析excel文档
        Workbook workbook = getRequestWorkbook("adjustExcel");
        // 文档校验
        if (workbook == null) {
            return;
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return;
        }
        int trLength = sheet.getLastRowNum() + 1;//总行数(包括首行)
        if (trLength < 2) {//excel第二行及以下的A列为空时，报错——excel为空
            return;
        }
        if (trLength > 1001) {//excel内容>1001行时，报错——导入学生不能超过1000人
            return;
        }
        List<StudentImportData> importData = parseStudentImportData(sheet);

        // 学校下的所有班级
        Map<String, Clazz> classNameMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(currentSchoolId())
                .enabled().toList().stream()
                .collect(Collectors.toMap(c -> c.getClazzLevel().getDescription() + c.getClassName(), Function.identity(), (u, v) -> v));
        // 更新学生根据班级分组
        Map<String, List<StudentImportData>> classImportStudents = importData.stream()
                .collect(Collectors.groupingBy(StudentImportData::classNameKey));

        // 开始更新
        StudentImportResultV2 result = new StudentImportResultV2(new String[] {"年级", "班级", "学生姓名", "新学号"}, new String[] {"年级", "班级", "学生姓名", "新学号", "失败原因"});

        for (Map.Entry<String, List<StudentImportData>> entry : classImportStudents.entrySet()) {
            List<StudentImportData> importStudents = entry.getValue();
            Clazz clazz = classNameMap.get(entry.getKey());
            if (clazz == null) {
                importStudents.forEach(data -> {
                    data.failed("系统中无此年级班级，请确认与系统中的年级名称、班级名称一致");
                    result.failed(data);
                });
                continue;
            }
            // 该班级下所有的学生
            List<Group> groups = asyncGroupServiceClient.getAsyncGroupService().loadGroupsByClazzId(clazz.getId()).getUninterruptibly();
            Map<Long, List<KlxStudent>> klxStudentsMap = newKuailexueLoaderClient.loadKlxGroupStudents(groups.stream().map(Group::getId).collect(Collectors.toList()));
            List<KlxStudent> klxStudents = klxStudentsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            klxStudents = klxStudents.stream().collect(Collectors.toMap(KlxStudent::getId, Function.identity(), (u, v) -> u))
                    .values()
                    .stream()
                    .sorted((k1, k2) -> {
                        Long n1 = SafeConverter.toLong(k1.getStudentNumber());
                        Long n2 = SafeConverter.toLong(k2.getStudentNumber());
                        return Long.compare(n1, n2);
                    })
                    .collect(Collectors.toList());
            Map<String, List<KlxStudent>> klxStudentsNameMap = klxStudents.stream().collect(Collectors.groupingBy(KlxStudent::getName));
            Map<String, KlxStudent> klxStudentsNumMap = klxStudents.stream().collect(Collectors.toMap(KlxStudent::getStudentNumber, Function.identity(), (u, v) -> u));
            for (StudentImportData data : importStudents) {
                // 忽略 姓名为空 或者 学号为空的记录
                if (StringUtils.isAnyBlank(data.getStudentName(), data.getStudentNumber())) {
                    continue;
                }
                List<KlxStudent> students = klxStudentsNameMap.get(data.getStudentName());
                if (students == null) {
                    data.failed("该班级无此学生，请确认学生姓名与班内姓名一致，若学生无账号需先添加学生账号");
                    result.failed(data);
                    continue;
                }
                if (students.size() > 1) {
                    data.failed("该班级内有多个同名学生，需手动修改学生学号");
                    result.failed(data);
                    continue;
                }
                if (klxStudentsNumMap.containsKey(data.getStudentNumber())) {
                    KlxStudent klxStudent = klxStudentsNumMap.get(data.getStudentNumber());
                    if (Objects.equals(klxStudent.getName(), data.getStudentName())) {
                        result.success(data);
                        continue;
                    }
                    data.failed(String.format("该学号已被%s的%s使用，学号要求校内不重复，请修改", clazz.formalizeClazzName(), klxStudent.getName()));
                    result.failed(data);
                    continue;
                }
                // 开始更新
                KlxStudent klxStudent = students.get(0);
                newKuailexueServiceClient.updateKlxStudentNumber(clazz.getSchoolId(), klxStudent.getId(), data.getStudentNumber());
                result.success(data);
            }

        }

        // 开始导出咯
        HSSFWorkbook resultFile = createXlsExcelExportData(result.toExportExcelData());
        String fileName = currentSchool().getShortName() + "更新学生学号结果附件" + DateUtils.dateToString(new Date(), SpecialTeacherConstants.TIME_PATTERN) + SpecialTeacherConstants.XLS_SUFFIX;
        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            resultFile.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception ex) {
            logger.error("Failed download special teacher batch import student result.", ex);
        }
    }


    /**
     * 批量添加老师账号-检查导入数据
     */
    @RequestMapping(value = "checkimportteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkBatchAddTeachers() {
        Workbook workbook = getRequestWorkbook("importTeacher");
        if (workbook == null) {
            return MapMessage.errorMessage("上传文件失败");
        }

        MapMessage checkMsg = checkWorkbook(workbook, 300);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }

        // 开始解析咯
        List<TeacherImportData> importData = parseTeacherImportData(workbook.getSheetAt(0));
        if (CollectionUtils.isEmpty(importData)) {
            return MapMessage.errorMessage("文档内容为空");
        }
        if (importData.size() > 300) {
            return MapMessage.errorMessage("文档人数异常，最多不超过300人");
        }

        //check mobile
        List<TeacherImportData> errorMobilesRecords = importData.stream().filter(p -> StringUtils.isBlank(p.getMobile())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(errorMobilesRecords)) {
            String errorNames = errorMobilesRecords.stream().map(d -> d.getName()).limit(17).collect(Collectors.joining(","));
            return MapMessage.errorMessage("手机号错误:" + errorNames);
        }

        // 检查一下有没有相同的手机号存在
        Map<String, List<TeacherImportData>> mobileGroupList = importData.stream().collect(Collectors.groupingBy(p -> p.getMobile(), Collectors.toList()));
        for (String mobile : mobileGroupList.keySet()) {
            if (mobileGroupList.get(mobile).size() > 1) {
                return MapMessage.errorMessage("文档中发现有重复的手机号:" + mobile);
            }
        }

        return MapMessage.successMessage();
    }

    /**
     * 批量添加老师账号-执行导入数据
     */
    @RequestMapping(value = "batchimportteacher.vpage", method = RequestMethod.POST)
    public void batchImportTeachers() {
        Workbook workbook = getRequestWorkbook("importTeacher");
        if (workbook == null) {
            return;
        }

        MapMessage checkMsg = checkWorkbook(workbook, 300);
        if (!checkMsg.isSuccess()) {
            return;
        }

        // 开始解析咯
        List<TeacherImportData> importData = parseTeacherImportData(workbook.getSheetAt(0));
        if (CollectionUtils.isEmpty(importData) || importData.size() > 300) {
            return;
        }

        try {
            // 导入处理,加个锁
            MapMessage result = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("SpecialTeacherAdminController.batchImportTeachers")
                    .keys(currentSpecialTeacher().getId())
                    .callback(() -> executeImportTeacherDataCallBack(importData))
                    .build()
                    .execute();

            TeacherImportResult importResult = (TeacherImportResult) result.get("result");

            // 开始导出咯
            HSSFWorkbook resultFile = createXlsExcelExportData(importResult.toExportExcelData());
            String fileName = currentSchool().getShortName() + "一起作业老师账号" + DateUtils.dateToString(new Date(), SpecialTeacherConstants.TIME_PATTERN) + SpecialTeacherConstants.XLS_SUFFIX;
            try {
                @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
                resultFile.write(out);
                out.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
            } catch (Exception ex) {
                logger.error("Failed download special teacher batch import teacher result.", ex);
            }

        } catch (DuplicatedOperationException ex) {
            // ignore
        } catch (Exception ex) {
            logger.error("Failed to import teacher with exception.", ex);
        }

        // 开始导入咯
//        TeacherImportResult importResult = executeImportTeacherData(importData);

//        // 开始导出咯
//        HSSFWorkbook resultFile = createXlsExcelExportData(importResult.toExportExcelData());
//        String fileName = currentSchool().getShortName() + "一起作业老师账号" + DateUtils.dateToString(new Date(), SpecialTeacherConstants.TIME_PATTERN) + SpecialTeacherConstants.XLS_SUFFIX;
//        try {
//            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
//            resultFile.write(out);
//            out.flush();
//            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
//        } catch (Exception ex) {
//            logger.error("Failed download special teacher batch import teacher result.", ex);
//        }
    }

    private MapMessage executeImportTeacherDataCallBack(List<TeacherImportData> importData) {
        TeacherImportResult importResult = executeImportTeacherData(importData);
        return MapMessage.successMessage().add("result", importResult);
    }

    /**
     * 为老师建班授课-检查导入数据
     */
    @RequestMapping(value = "checkteacherclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkTeacherClazz() {
        Workbook workbook = getRequestWorkbook("teacherClazz");
        if (workbook == null) {
            return MapMessage.errorMessage("上传文件失败");
        }

        MapMessage checkMsg = checkWorkbook(workbook, 100);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }

        MapMessage msg = parseTeacherClazzImportData(workbook.getSheetAt(0));
        if (!msg.isSuccess()) {
            return msg;
        }

        List<TeacherClazzImportData> importData = (List<TeacherClazzImportData>) msg.get("data");

        if (CollectionUtils.isEmpty(importData)) {
            return MapMessage.errorMessage("文档内容为空");
        }

        School school = currentSchool();
        EduSystemType eduSystem = EduSystemType.of(
                schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly()
        );
        if (eduSystem == null) {
            return MapMessage.errorMessage("学校学制异常");
        }

        Set<String> invalidGrade = importData.stream().map(p -> SpecialTeacherConstants.parseGradeOfChinese(p.getGrade()))
                .filter(d -> !eduSystem.getCandidateClazzLevel().contains(String.valueOf(d.getLevel())))
                .map(ClazzLevel::getDescription)
                .collect(Collectors.toCollection(TreeSet::new));

        if (invalidGrade.size() > 0) {
            return MapMessage.errorMessage(" 当前学校学制不支持（{}）导入，请校验后再次导入", StringUtils.join(invalidGrade, "、"));
        }

        List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(school.getId());
        if (CollectionUtils.isEmpty(teachers)) {
            return MapMessage.errorMessage("未在学校内找到老师，schoolId:" + school.getId());
        }

        List<String> foundTeachers = new ArrayList<>();    // 找到的老师
        Set<String> unfoundTeachers = new HashSet<>();  // 未找到的老师
        Set<String> multiTeachers = new HashSet<>();       // 同学科同姓名老师
        Set<String> teacherNameList = new HashSet<>();

        Set<String> importTeacherNames = importData.stream().map(TeacherClazzImportData::getTeacherName).collect(Collectors.toSet());
        for (Teacher data : teachers) {
            teachers.forEach(t -> {
                if (t.fetchRealname().equals(data.fetchRealname()) && t.getSubject() == data.getSubject() && importTeacherNames.contains(t.fetchRealname())) {
                    if (foundTeachers.contains(data.fetchRealname())) {
                        multiTeachers.add(data.fetchRealname());
                    }
                    foundTeachers.add(data.fetchRealname());
                    int clazzNum = specialTeacherLoaderClient.findValidGroup(t.getId());
                    if (clazzNum >= ClazzConstants.MAX_GROUP_COUNT) {
                        teacherNameList.add(t.fetchRealname() + "老师");
                    }
                }
            });
        }

        importData.forEach(t -> {
            if (!foundTeachers.contains(t.getTeacherName())) {
                unfoundTeachers.add(t.getTeacherName());
            }
        });

        if (unfoundTeachers.size() > 0) {
            return MapMessage.errorMessage("未找到该学科该姓名老师（" + JsonUtils.toJson(unfoundTeachers) + "),请确认是否已添加该老师账号");
        }
        if (multiTeachers.size() > 0) {
            return MapMessage.errorMessage("找到多位同学科同姓名老师（" + JsonUtils.toJson(multiTeachers) + "),无法批量导入，请在班级管理页面手动添加");
        }

        if (teacherNameList.size() > 0) {
            return MapMessage.errorMessage("老师名下班级数不能超过8个：" + JsonUtils.toJson(teacherNameList));
        }

        return MapMessage.successMessage();
    }

    /**
     * 为老师建班授课-执行导入数据
     */
    @RequestMapping(value = "createteacherclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public void createTeacherClazz() {
        Workbook workbook = getRequestWorkbook("teacherClazz");
        if (workbook == null) {
            return;
        }

        MapMessage checkMsg = checkWorkbook(workbook, 100);
        if (!checkMsg.isSuccess()) {
            return;
        }

        MapMessage msg = parseTeacherClazzImportData(workbook.getSheetAt(0));
        List<TeacherClazzImportData> importData = (List<TeacherClazzImportData>) msg.get("data");

        // 开始导入咯
        TeacherClazzImportResult importResult = executeTeacherClazzImportData(importData);

        // 开始导出咯
        HSSFWorkbook resultFile = createXlsExcelExportData(importResult.toExportExcelData());
        String fileName = "建班授课" + DateUtils.dateToString(new Date(), SpecialTeacherConstants.TIME_PATTERN) + SpecialTeacherConstants.XLS_SUFFIX;
        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            resultFile.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception ex) {
            logger.error("Failed download create teacher clazz result.", ex);
        }
    }


    /**
     * 批量添加学生账号-检查导入数据
     */
    @RequestMapping(value = "checkimportstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkBatchAddStudents() {
        Workbook workbook = getRequestWorkbook("importStudent");
        if (workbook == null) {
            return MapMessage.errorMessage("上传文件失败");
        }

        MapMessage checkMsg = checkWorkbook(workbook, 1000);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }

        // 开始解析咯
        List<StudentImportData> importData = parseStudentImportData(workbook.getSheetAt(0));
        if (CollectionUtils.isEmpty(importData)) {
            return MapMessage.errorMessage("文档内容为空");
        }
        if (importData.size() > 1000) {
            return MapMessage.errorMessage("文档人数异常，最多不超过1000人");
        }

        // 检查班级数据
        Set<String> invalidClass = importData.stream()
                .filter(c -> StringUtils.isAnyBlank(c.getGrade(), c.getClassName()))
                .map(StudentImportData::invalidClass)
                .collect(Collectors.toCollection(TreeSet::new));

        if (invalidClass.size() > 0) {
            return MapMessage.errorMessage(StringUtils.join(invalidClass, "<br/>"));
        }

        // 学校下所有班级, 年级+下划线+班级名称
        Map<String, Long> classNameDict = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(currentSchoolId())
                .enabled().toList().stream()
                .collect(Collectors.toMap(c -> c.getClazzLevel().getDescription() + c.getClassName(), Clazz::getId, (u, v) -> u));

        // 检查班级
        Set<String> notExistClass = new TreeSet<>();                    // 没有对应班级的班级
        Map<String, Set<Subject>> dupSubjectClass = new TreeMap<>();    // 班级内有多个相同学科的班级
        Set<String> clazzNameList = new HashSet<>();                 // group内人数是否>=100人的班级

        Map<String, List<StudentImportData>> clazzStudentsMap = importData.stream().collect(Collectors.groupingBy(StudentImportData::classNameKey));
        Map<Long, List<GroupMapper>> clazzGroupMap = new HashMap<>();
        for (StudentImportData data : importData) {
            String key = data.classNameKey();

            Long clazzId = classNameDict.get(key);
            if (clazzId == null) {
                notExistClass.add(data.fullClassName());
                continue;
            }

            List<GroupMapper> groups;
            if (!clazzGroupMap.containsKey(clazzId)) {
                groups = deprecatedGroupLoaderClient.loadClazzGroups(clazzId);
                clazzGroupMap.put(clazzId, groups);
            } else {
                groups = clazzGroupMap.get(clazzId);
            }
            if (CollectionUtils.isEmpty(groups)) {
                notExistClass.add(data.fullClassName());
                continue;
            }

            Set<Subject> dupSubjects = new TreeSet<>();
            groups.stream().filter(g -> g.getSubject() != null)
                    .collect(Collectors.groupingBy(GroupMapper::getSubject))
                    .forEach((subject, list) -> {
                        if (list.size() > 1) {
                            dupSubjects.add(subject);
                        }
                    });
            if (dupSubjects.size() > 0) dupSubjectClass.put(data.fullClassName(), dupSubjects);

            // #55742 group内人数是否>100人 提示文案
            List<Long> gids = groups.stream().map(GroupMapper::getId).collect(Collectors.toList());
            Set<String> klxStudentIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findGroupKlxStudentRefsByGroups(gids).getUninterruptibly().values()
                    .stream().flatMap(Collection::stream).map(GroupKlxStudentRef::getKlxStudentId)
                    .collect(Collectors.toSet());
            if (klxStudentIds.size() + clazzStudentsMap.get(key).size() > globalTagServiceClient.getGlobalTagBuffer()
                    .loadSchoolMaxClassCapacity(currentSchoolId(), ClazzConstants.MAX_CLAZZ_CAPACITY)) {
                clazzNameList.add(data.fullClassName());
            }
        }

        StringBuilder errorMsg = new StringBuilder();
        boolean error = false;
        if (notExistClass.size() > 0) {
            errorMsg.append("未给该班级老师授课（")
                    .append(StringUtils.join(notExistClass, "，"))
                    .append("），无法将学生导入该班级").append("<br/>");
            error = true;
        }
        if (dupSubjectClass.size() > 0) {
            dupSubjectClass.forEach((className, subjects) -> {
                List<String> subjectName = subjects.stream().map(Subject::getValue).collect(Collectors.toList());
                errorMsg.append(className).append("中有多个（").append(StringUtils.join(subjectName, "、")).append("）班群，请先去班级管理中将错误班群删除").append("<br/>");
            });
            error = true;
        }
        if (clazzNameList.size() > 0) {
            errorMsg.append("班级学生数不能超过100人：")
                    .append(JsonUtils.toJson(clazzNameList));
            error = true;
        }
        if (error) {
            return MapMessage.errorMessage(errorMsg.toString()).setErrorCode("POP");
        }

        // 历史删除检验
        // 学校下的所有班级
        Map<String, Clazz> classNameMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(currentSchoolId())
                .enabled().toList().stream()
                .collect(Collectors.toMap(c -> c.getClazzLevel().getDescription() + c.getClassName(), Function.identity(), (u, v) -> v));

        // 导入学生根据班级分组
        Map<String, List<StudentImportData>> classImportStudents = importData.stream()
                .collect(Collectors.groupingBy(StudentImportData::classNameKey));
        @Getter
        @Setter
        class SameStudent{
            private String clazzName;
            private String name;
            public SameStudent(String clazzName, String name){
                this.clazzName = clazzName;
                this.name = name;
            }
        }
        List<SameStudent> deleteSameStudents = new ArrayList<>(); // 与删除的同名的学生
        for (Map.Entry<String, List<StudentImportData>> entry : classImportStudents.entrySet()) {
            Clazz clazz = classNameMap.get(entry.getKey());
            if (clazz == null) {
                continue;
            }
            List<Group> groups = clazzGroupServiceClient.getGroupService().findGroupsByClazzId(clazz.getId()).getUninterruptibly();
            List<GroupKlxStudentRef> deleteGroupKlxStudentRefs = new ArrayList<>();
            groups.forEach(g -> deleteGroupKlxStudentRefs.addAll(asyncGroupServiceClient.getAsyncGroupService().loadDeletedGroupKlxStudentRefs(g.getId()).getUninterruptibly()));
            Set<String> klxStudentIds = deleteGroupKlxStudentRefs.stream().map(GroupKlxStudentRef::getKlxStudentId).collect(Collectors.toSet());
            Map<String, List<GroupKlxStudentRef>> groupKlxStudentRefMap = asyncGroupServiceClient.getAsyncGroupService().findGroupKlxStudentRefsByStudents(klxStudentIds).getUninterruptibly();
            // 过滤掉有组的学生
            Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(klxStudentIds.stream().filter(s -> groupKlxStudentRefMap.get(s).isEmpty()).collect(Collectors.toSet()));

            // 重名恢复取最近更新的
            Map<String, KlxStudent> klxStudentNameMap = klxStudentMap.values().stream().collect(Collectors.toMap(KlxStudent::getName, Function.identity(), (u, v) ->
                u.getUpdateTime().after(v.getUpdateTime()) ? u : v
            ));
            Map<String, StudentImportData> stuInfoMap = entry.getValue().stream().collect(Collectors.toMap(StudentImportData::getStudentName, Function.identity(), (u, v) -> u));
            klxStudentNameMap.values().forEach(k -> {
                if (stuInfoMap.containsKey(k.getName())) {
                    SameStudent sameStudent = new SameStudent(clazz.formalizeClazzName(), k.getName());
                    deleteSameStudents.add(sameStudent);
                }
            });
        }
        if (!deleteSameStudents.isEmpty()) {
            // 按班级分组返回
            Map<String, Set<String>> deleteSameStudentsMap = deleteSameStudents.stream().collect(Collectors.groupingBy(SameStudent::getClazzName, Collectors.mapping(SameStudent::getName, Collectors.toSet())));
            return MapMessage.successMessage().add("klxDeleteSameName", true).add("deleteSameStudents", deleteSameStudentsMap);
        }
        return MapMessage.successMessage();
    }

    /**
     * 批量添加学生账号-执行导入数据
     */
    @RequestMapping(value = "batchimportstudent.vpage", method = RequestMethod.POST)
    public void batchImportStudents() {
        Workbook workbook = getRequestWorkbook("importStudent");
        if (workbook == null) {
            return;
        }

        MapMessage checkMsg = checkWorkbook(workbook, 1000);
        if (!checkMsg.isSuccess()) {
            return;
        }

        // 开始解析咯
        List<StudentImportData> importData = parseStudentImportData(workbook.getSheetAt(0));
        if (CollectionUtils.isEmpty(importData) || importData.size() > 1000) {
            return;
        }
        boolean recoverStudent = getRequestBool("recoverStudent");
        try {
            // 导入处理,加个锁
            MapMessage result = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("SpecialTeacherAdminController.batchImportStudents")
                    .keys(currentSpecialTeacher().getId())
                    .callback(() -> executeImportStudentDataCallBack(importData, recoverStudent))
                    .build()
                    .execute();

            StudentImportResult importResult = (StudentImportResult) result.get("result");

            // 开始导出咯
            HSSFWorkbook resultFile = createXlsExcelExportData(importResult.toExportExcelData());
            String fileName = currentSchool().getShortName() + "一起作业学生账号" + DateUtils.dateToString(new Date(), SpecialTeacherConstants.TIME_PATTERN) + SpecialTeacherConstants.XLS_SUFFIX;
            try {
                @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
                resultFile.write(out);
                out.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
            } catch (Exception ex) {
                logger.error("Failed download special teacher batch import student result.", ex);
            }

        } catch (DuplicatedOperationException ex) {
            // ignore
        } catch (Exception ex) {
            logger.error("Failed to import student with exception.", ex);
        }

//        // 开始导入咯
//        StudentImportResult importResult = executeImportStudentData(importData);
//
//        // 开始导出咯
//        HSSFWorkbook resultFile = createXlsExcelExportData(importResult.toExportExcelData());
//        String fileName = currentSchool().getShortName() + "一起作业学生账号" + DateUtils.dateToString(new Date(), SpecialTeacherConstants.TIME_PATTERN) + SpecialTeacherConstants.XLS_SUFFIX;
//        try {
//            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
//            resultFile.write(out);
//            out.flush();
//            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
//        } catch (Exception ex) {
//            logger.error("Failed download special teacher batch import student result.", ex);
//        }
    }

    private MapMessage executeImportStudentDataCallBack(List<StudentImportData> importData, boolean recoverStudent) {
        StudentImportResult result = executeImportStudentData(importData, recoverStudent);
        return MapMessage.successMessage().add("result", result);
    }

    /**
     * 批量增加借读生标记-检查数据
     */
    @RequestMapping(value = "checkmarkstudents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkMarkStudents() {
        Workbook workbook = getRequestWorkbook("markStudents");
        if (workbook == null) {
            return MapMessage.errorMessage("上传文件失败");
        }
        MapMessage checkMsg = checkWorkbook(workbook, 1000);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        List<MarkStudentData> markStudentDatas = parseMarkStudentData(workbook.getSheetAt(0));
        if (CollectionUtils.isEmpty(markStudentDatas)) {
            return MapMessage.errorMessage("文档内容为空");
        }
//        if (importData.size() > 1000) {
//            return MapMessage.errorMessage("文档人数异常，最多不超过1000人");
//        }

        // 学校下所有班级, 年级+下划线+班级名称
        Map<String, Long> classNameDict = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(currentSchoolId())
                .enabled().toList().stream()
                .collect(Collectors.toMap(c -> c.getClazzLevel().getDescription() + c.getClassName(), Clazz::getId, (u, v) -> u));

        // 检查班级
        Set<String> notExistClass = new TreeSet<>();                    // 没有对应班级的班级
        Map<String, Set<Subject>> dupSubjectClass = new TreeMap<>();    // 班级内有多个相同学科的班群

        Map<Long, List<GroupMapper>> clazzGroupMap = new HashMap<>();

        for (MarkStudentData data : markStudentDatas) {
            String key = data.classNameKey();

            Long clazzId = classNameDict.get(key);
            if (clazzId == null) {
                notExistClass.add(data.fullClassName());
                continue;
            }

            List<GroupMapper> groups;
            if (!clazzGroupMap.containsKey(clazzId)) {
                groups = deprecatedGroupLoaderClient.loadClazzGroups(clazzId);
                clazzGroupMap.put(clazzId, groups);
            } else {
                groups = clazzGroupMap.get(clazzId);
            }
            if (CollectionUtils.isEmpty(groups)) {
                notExistClass.add(data.fullClassName());
                continue;
            }

            Set<Subject> dupSubjects = new TreeSet<>();
            groups.stream().filter(g -> g.getSubject() != null)
                    .collect(Collectors.groupingBy(GroupMapper::getSubject))
                    .forEach((subject, list) -> {
                        if (list.size() > 1) {
                            dupSubjects.add(subject);
                        }
                    });
            if (dupSubjects.size() > 0) dupSubjectClass.put(data.fullClassName(), dupSubjects);

        }

        StringBuilder errorMsg = new StringBuilder();
        boolean error = false;
        if (notExistClass.size() > 0) {
            errorMsg.append("未找到该班级（")
                    .append(StringUtils.join(notExistClass, "，"))
                    .append("），请确认导入信息").append("<br/>");
            error = true;
        }
        if (dupSubjectClass.size() > 0) {
            dupSubjectClass.forEach((className, subjects) -> {
                List<String> subjectName = subjects.stream().map(Subject::getValue).collect(Collectors.toList());
                errorMsg.append(className).append("中有多个（").append(StringUtils.join(subjectName, "、")).append("）班群，请先去班级管理中将错误班群删除").append("<br/>");
            });
            error = true;
        }
        if (error) {
            return MapMessage.errorMessage(errorMsg.toString());
        }
        return MapMessage.successMessage();
    }

    /**
     * 批量增加借读生标记-执行数据
     */
    @RequestMapping(value = "batchmarkstudents.vpage", method = RequestMethod.POST)
    public void batchMarkStudents() {
        Workbook workbook = getRequestWorkbook("markStudents");
        if (workbook == null) {
            return;
        }
        MapMessage checkMsg = checkWorkbook(workbook, 1000);
        if (!checkMsg.isSuccess()) {
            return;
        }
        // 开始解析咯
        List<MarkStudentData> markStudentDatas = parseMarkStudentData(workbook.getSheetAt(0));
        if (CollectionUtils.isEmpty(markStudentDatas)) {
            return;
        }

        // 开始导入咯
        MarkStudentResult markResult = executeMarkStudent(markStudentDatas);

        // 开始导出咯
        HSSFWorkbook resultFile = createXlsExcelExportData(markResult.toExportExcelData());
        String fileName = currentSchool().getShortName() + "借读生标记" + DateUtils.dateToString(new Date(), SpecialTeacherConstants.TIME_PATTERN) + SpecialTeacherConstants.XLS_SUFFIX;
        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            resultFile.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception ex) {
            logger.error("Failed download special teacher batch mark student result.", ex);
        }


    }

    //----------------     PRIVATE METHODS    --------------------
    private List<TeacherImportData> parseTeacherImportData(Sheet sheet) {
        List<TeacherImportData> data = new LinkedList<>();
        // 从第一行开始
        for (int index = 1; index <= sheet.getLastRowNum(); index++) {
            Row row = sheet.getRow(index);
            if (row == null) {
                continue;
            }

            String teacherName = WorkbookUtils.getCellValue(row.getCell(0));
            String subjectName = WorkbookUtils.getCellValue(row.getCell(1));
            String mobile = WorkbookUtils.getCellValue(row.getCell(2));

            // 读到了一个空行，忽略吧
            if (StringUtils.isBlank(teacherName) && StringUtils.isBlank(subjectName) && StringUtils.isBlank(mobile)) {
                continue;
            }
            data.add(new TeacherImportData(teacherName, subjectName, mobile));
        }

        return data;
    }

    private TeacherImportResult executeImportTeacherData(List<TeacherImportData> importData) {
        User affairTeacher = currentUser();
        Long schoolId = currentSchoolId();
        Ktwelve ktwelve = currentSchoolKtwelve();

        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        Set<Subject> validSubject = schoolExtInfo == null ? SchoolExtInfo.DefaultSubjects : schoolExtInfo.loadValidSubjects();
        TeacherImportResult result = new TeacherImportResult();
        for (TeacherImportData data : importData) {
            if (!data.introspect()) {
                result.failed(data);
                continue;
            }

            if (!validSubject.contains(data.subject())) {
                data.failed("暂时不支持[" + data.subject().getValue() + "]学科老师注册");
                result.failed(data);
                continue;
            }

            UserAuthentication fromImportData = userLoaderClient.loadMobileAuthentication(data.getMobile(), UserType.TEACHER);
            if(fromImportData != null){
                TeacherDetail alreadyHaveTeacher = teacherLoaderClient.loadTeacherDetail(fromImportData.getId());
                if (alreadyHaveTeacher.getSubject()!= null || alreadyHaveTeacher.getTeacherSchoolId()!= null) {
                    data.failed("手机号已被注册");
                    result.failed(data);
                    continue;
                }

                // 如果手机号码已被绑定，那么解绑手机号码
                userServiceClient.getRemoteReference().cleanupUserMobile(currentOperator(), alreadyHaveTeacher.getId());
            }

            // 开始创建老师
            String initialPassword = SpecialTeacherConstants.generatePassword(schoolId);

            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
            neonatalUser.setUserType(UserType.TEACHER);
            neonatalUser.setMobile(data.getMobile());
            neonatalUser.setRealname(data.getName());
            neonatalUser.setSubject(data.subject().name());
            neonatalUser.setPassword(initialPassword);
            neonatalUser.setWebSource("AFFAIR_BATCH");
            neonatalUser.attachPasswordState(PasswordState.AUTO_GEN);

            MapMessage regResult = userServiceClient.registerUser(neonatalUser);
            if (!regResult.isSuccess()) {
                data.failed(regResult.getInfo());
                result.failed(data);
                continue;
            }

            User teacher = (User) regResult.get("user");
            // 如果手机号码尚未绑定，那么绑定手机号码
            if (userLoaderClient.loadMobileAuthentication(data.getMobile(), UserType.TEACHER) == null) {
                userServiceClient.activateUserMobile(teacher.getId(), data.getMobile(), false, SafeConverter.toString(affairTeacher.getId()), "批量导入");
            }
            // 设置老师学校
            teacherServiceClient.setTeacherSubjectSchool(teacher, data.subject(), ktwelve, schoolId);
            // 发送注册成功短信
            userServiceClient.sendRegistrationSms(teacher.getId(), data.getMobile(), SpecialTeacherConstants.generateSmsContent(affairTeacher.fetchRealnameIfBlankId(), initialPassword));

            data.success(teacher.getId(), initialPassword);
            result.success(data);
        }
        return result;
    }

    private MapMessage parseTeacherClazzImportData(Sheet sheet) {
        List<TeacherClazzImportData> data = new LinkedList<>();
        // 从第一行开始
        for (int index = 1; index <= sheet.getLastRowNum(); index++) {
            Row row = sheet.getRow(index);
            if (row == null) {
                continue;
            }

            String teacherName = WorkbookUtils.getCellValue(row.getCell(0));
            String subject = WorkbookUtils.getCellValue(row.getCell(1));
            String grade = WorkbookUtils.getCellValue(row.getCell(2));
            String clazzName = WorkbookUtils.getCellValue(row.getCell(3));
            String clazzType = WorkbookUtils.getCellValue(row.getCell(4));

            // 读到了一个空行，忽略吧
            if (StringUtils.isBlank(teacherName) && StringUtils.isBlank(subject) && StringUtils.isBlank(grade)
                    && StringUtils.isBlank(clazzName) && StringUtils.isBlank(clazzType)) {
                continue;
            }

            if (SpecialTeacherConstants.parseGradeOfChinese(grade) == null) {
                return MapMessage.errorMessage("年级错误：" + grade + "，请输入正确年级");
            }

            if (SpecialTeacherConstants.parseImportType(clazzType) == null) {
                return MapMessage.errorMessage("班级类型错误：" + clazzType);
            }

            if (StringUtils.isBlank(teacherName) || StringUtils.isBlank(subject) || StringUtils.isBlank(grade)
                    || StringUtils.isBlank(clazzName) || StringUtils.isBlank(clazzType)) {
                return MapMessage.errorMessage("必填项存在空值，请检查数据");
            }
            data.add(new TeacherClazzImportData(teacherName, subject, grade, clazzName, clazzType));
        }
        return MapMessage.successMessage().add("data", data);
    }

    private TeacherClazzImportResult executeTeacherClazzImportData(List<TeacherClazzImportData> importData) {
        TeacherClazzImportResult result = new TeacherClazzImportResult();
        Long schoolId = currentSchoolId();
        List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(schoolId);
        Map<String, Long> teacherMap = new HashMap<>(); // key:teacherName  value:teacherId
        for (TeacherClazzImportData data : importData) {
            teachers.forEach(t -> {
                if (t.fetchRealname().equals(data.getTeacherName())
                        && t.getSubject() == SpecialTeacherConstants.parseSubjectOfChinese(data.getSubject())) {
                    teacherMap.put(data.teacherKey(), t.getId());
                }
            });
        }

        Map<String, Clazz> clazzMap = new HashMap<>();
        for (TeacherClazzImportData data : importData) {
            ClazzLevel clazzLevel = SpecialTeacherConstants.parseGradeOfChinese(data.getGrade());
            ClazzType clazzType = SpecialTeacherConstants.parseImportType(data.getClazzType());
            String systemClazzName = data.getClazzName();
            String walkingClazzName = data.getSubject() + data.getClazzName();
            Subject subject = SpecialTeacherConstants.parseSubjectOfChinese(data.getSubject());
            Long teacherId = teacherMap.get(data.teacherKey());

            List<Clazz> clazzList = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(schoolId).enabled().toList();
            for (Clazz clazz : clazzList) {
                if ((clazz.getClassName().equals(systemClazzName) || clazz.getClassName().equals(walkingClazzName))
                        && clazz.getClazzLevel() == clazzLevel && clazz.getClazzType() == clazzType) {
                    // 学校内已存在同名同类型班级
                    clazzMap.put(clazz.formalizeClazzName(), clazz);
                }
            }

            if (clazzType == ClazzType.PUBLIC) {
                Clazz clazz = clazzMap.get(clazzLevel.getDescription() + systemClazzName);
                if (clazzMap.keySet().contains(clazzLevel.getDescription() + systemClazzName)) { // 找到同名班级
                    List<GroupMapper> groupMappers = teacherLoaderClient.findTeacherAllGroupInClazz(clazz.getId(), teacherId);

                    if (CollectionUtils.isNotEmpty(groupMappers)) { // 该老师在该clazz内是否有关联的group
                        result.success(data);
                        continue;
                    }

                    List<GroupMapper> groupMapperList = deprecatedGroupLoaderClient.loadClazzGroups(clazz.getId());
                    Map<String, List<GroupMapper>> groupParentMap = groupMapperList.stream()
                            .collect(Collectors.groupingBy(GroupMapper::getGroupParent));

                    if (groupParentMap.keySet().size() > 1
                            || (groupParentMap.containsKey("") && groupParentMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList()).size() > 1)) {
                        // 该clazz下是否有>1个班群 || 没有共享分组时班级下分组大于1
                        data.setReason("班中有多个班群，请先去班级管理中调整好班群");
                        result.failed(data);
                        continue;
                    }

                    List<Subject> systemGroupSubjects = groupMapperList.stream().filter(g -> g.getGroupType() != GroupType.WALKING_GROUP).map(GroupMapper::getSubject).collect(Collectors.toList());

                    if (systemGroupSubjects.contains(subject)) { // 该clazz下是否有该学科group
                        data.setReason("班内已有" + subject.getValue() + "学科老师，请去班群详情页手动调整");
                        result.failed(data);
                        continue;
                    }

                    MapMessage msg = groupServiceClient.createTeacherGroup(teacherId, clazz.getId(), null);
                    Long groupId = ConversionUtils.toLong(msg.get("groupId"));
                    Set<Long> groupIds = groupMapperList.stream()
                            .filter(g -> g.getGroupType() != GroupType.WALKING_GROUP)
                            .map(GroupMapper::getId)
                            .collect(Collectors.toSet());
                    groupIds.add(groupId);
                    // 同步共享分组
                    groupServiceClient.shareGroups(groupIds, true);
                    // 同步学生名单
                    newKuailexueServiceClient.syncSharedGroupStudent(groupId);
                }else { // 未找到同名班级
                    String eduSystem = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(currentSchool()).getUninterruptibly();
                    // 创建行政班流程
                    ClassMapper classMapper = new ClassMapper();
                    classMapper.setSchoolId(schoolId);
                    classMapper.setClassLevel(ConversionUtils.toString(clazzLevel.getLevel()));
                    classMapper.setClazzName(systemClazzName);
                    classMapper.setFreeJoin(Boolean.TRUE);
                    classMapper.setEduSystem(eduSystem);
                    classMapper.setOperatorId("教务建班授课:" + currentSpecialTeacher().getId());
                    MapMessage message = clazzServiceClient.createSystemClazz(Collections.singleton(classMapper));
                    Collection<NeonatalClazz> neonatals = (Collection<NeonatalClazz>) message.get("neonatals");
                    Long clazzId = neonatals.stream().findFirst().orElse(null).getClazzId();
                    MapMessage msg = clazzServiceClient.teacherJoinSystemClazz(teacherId, clazzId, OperationSourceType.jwls);
                    if (!msg.isSuccess()) {
                        data.setReason(msg.getInfo());
                        result.failed(data);
                        specialTeacherServiceClient.deleteClazz(clazzId);
                        continue;
                    }
                }
                result.success(data);
            }else if (clazzType == ClazzType.WALKING) {
                Clazz clazz = clazzMap.get(clazzLevel.getDescription() + walkingClazzName);
                if (clazzMap.keySet().contains(clazzLevel.getDescription() + walkingClazzName)) { // 找到同名班级
                    List<GroupMapper> groupMapperList = deprecatedGroupLoaderClient.loadClazzGroups(clazz.getId());
                    List<Subject> walkingGroupSubjects = groupMapperList.stream().filter(g -> g.getGroupType() == GroupType.WALKING_GROUP).map(GroupMapper::getSubject).collect(Collectors.toList());

                    if (walkingGroupSubjects.contains(subject)) {
                        data.setReason("此学校已经存在" + clazz.formalizeClazzName());
                        result.failed(data);
                        continue;
                    }
                    // 创建教学分组
                    groupServiceClient.createWalkingGroup(teacherId, clazz.getId());

                }else { // 未找到同名班级
                    String eduSystem = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(currentSchool()).getUninterruptibly();
                    // 创建教学班流程
                    ClassMapper classMapper = new ClassMapper();
                    classMapper.setSchoolId(schoolId);
                    classMapper.setClassLevel(ConversionUtils.toString(clazzLevel.getLevel()));
                    classMapper.setClazzName(walkingClazzName);
                    classMapper.setFreeJoin(Boolean.TRUE);
                    classMapper.setEduSystem(eduSystem);
                    MapMessage msg = clazzServiceClient.createWalkingClazz(teacherId, Collections.singleton(classMapper));
                    if (!msg.isSuccess()) {
                        data.setReason(msg.getInfo());
                        result.failed(data);
                        continue;
                    }
                    schoolExtServiceClient.getSchoolExtService()
                            .addWalkingClazzName(schoolId, clazzLevel, subject, walkingClazzName)
                            .getUninterruptibly();
                }
                result.success(data);
            }
        }
        return result;
    }


    private List<StudentImportData> parseStudentImportData(Sheet sheet) {
        List<StudentImportData> data = new LinkedList<>();
        // 从第一行开始
        for (int index = 1; index <= sheet.getLastRowNum(); index++) {
            Row row = sheet.getRow(index);
            if (row == null) {
                continue;
            }

            String grade = WorkbookUtils.getCellValue(row.getCell(0));
            String className = WorkbookUtils.getCellValue(row.getCell(1));
            String studentName = WorkbookUtils.getCellValue(row.getCell(2));
            String studentNumber = WorkbookUtils.getCellValue(row.getCell(3));

            // 读到了一个空行，忽略吧
            if (StringUtils.isBlank(grade) && StringUtils.isBlank(className) && StringUtils.isBlank(studentName) && StringUtils.isBlank(studentNumber)) {
                continue;
            }
            data.add(new StudentImportData(index, grade, className, studentName, studentNumber));
        }

        return data;
    }

    @SuppressWarnings("unchecked")
    private StudentImportResult executeImportStudentData(List<StudentImportData> importData, boolean recoverStudent) {
        School school = currentSchool();
        Long schoolId = school.getId();

        EduSystemType eduSystem = EduSystemType.of(
                schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly()
        );
        StudentImportResult result = new StudentImportResult();

        // 学校下的所有班级
        Map<String, Clazz> classNameMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(currentSchoolId())
                .enabled().toList().stream()
                .collect(Collectors.toMap(c -> c.getClazzLevel().getDescription() + c.getClassName(), Function.identity(), (u, v) -> v));

        // 导入学生根据班级分组
        Map<String, List<StudentImportData>> classImportStudents = importData.stream()
                .collect(Collectors.groupingBy(StudentImportData::classNameKey));

        // 开始导入
        for (Map.Entry<String, List<StudentImportData>> classStudents : classImportStudents.entrySet()) {
            String className = classStudents.getKey();
            List<StudentImportData> students = classStudents.getValue();
            Clazz clazz = classNameMap.get(className);
            if (clazz == null) {
                students.forEach(data -> {
                    data.failed("未给该班级老师授课，无法将学生导入该班级");
                    result.failed(data);
                });
                continue;
            }

            if (eduSystem == null || !eduSystem.getCandidateClazzLevel().contains(clazz.getClassLevel())) {
                students.forEach(data -> {
                    data.failed("当前学校不支持" + clazz.getClazzLevel().getDescription() + "学生导入");
                    result.failed(data);
                });
                continue;
            }

            List<KeyValuePair<String, String>> studentInfo = new LinkedList<>();
            List<StudentImportData> processData = new LinkedList<>();

            Set<String> existStudents = new HashSet<>();  // 班内已有学生名单
            Set<String> existStudentNumbers = new HashSet<>(); // 班内已有学号
            existStudents.addAll(
                    studentLoaderClient.loadClazzStudents(clazz.getId()).stream().map(User::fetchRealname).collect(Collectors.toSet())
            );
            List<KlxStudent> klxStudents = specialTeacherLoaderClient.loadKlxStudentsByClazzId(clazz.getId());

            existStudents.addAll(
                    klxStudents.stream().map(KlxStudent::getName).collect(Collectors.toSet())
            );
            existStudentNumbers.addAll(
                    klxStudents.stream().map(KlxStudent::getStudentNumber).collect(Collectors.toSet())
            );

            // 对这个班级的导入学生按学号分个组, 检查学号
            Map<String, List<StudentImportData>> studentNumberMap = students.stream()
                    .filter(stu -> StringUtils.isNotBlank(stu.getStudentNumber()))
                    .collect(Collectors.groupingBy(StudentImportData::getStudentNumber));
            for (Map.Entry<String, List<StudentImportData>> entry : studentNumberMap.entrySet()) {
                String studentNumber = entry.getKey();
                List<StudentImportData> student = entry.getValue();

                if (CollectionUtils.isEmpty(student)) {
                    continue;
                }

                // 名单内学生姓名重复
                if (student.size() > 1) {
                    Set<Integer> rows = student.stream().map(StudentImportData::getRowIndex).collect(Collectors.toCollection(TreeSet::new));
                    student.forEach(data -> {
                        data.failed(StringUtils.formatMessage("导入同班级学生学号重复（位于第{}行），请加标记以区分", StringUtils.join(rows, "、")));
                        result.failed(data);
                    });
                    continue;
                }

                // 剩下的再去看看跟班级内有没重复学号的
                StudentImportData data = student.get(0);
                if (existStudentNumbers.contains(studentNumber)) {
                    data.failed("填写的学号与班内学生重复");
                    result.failed(data);
                }
            }

            // 对这个班级的导入学生按姓名分个组, 检查姓名
            Map<String, List<StudentImportData>> studentNameMap = students.stream()
                    .filter(stu -> StringUtils.isNotBlank(stu.getStudentName()))
                    .collect(Collectors.groupingBy(StudentImportData::getStudentName));
            for (Map.Entry<String, List<StudentImportData>> entry : studentNameMap.entrySet()) {
                String studentName = entry.getKey();
                List<StudentImportData> student = entry.getValue();

                if (CollectionUtils.isEmpty(student)) {
                    continue;
                }

                // 名单内学生姓名重复
                if (student.size() > 1) {
                    Set<Integer> rows = student.stream().map(StudentImportData::getRowIndex).collect(Collectors.toCollection(TreeSet::new));
                    student.forEach(data -> {
                        data.failed(StringUtils.formatMessage("导入同班级学生姓名重复（位于第{}行），请加标记以区分", StringUtils.join(rows, "、")));
                        result.failed(data);
                    });
                    continue;
                }

                // 剩下的再去看看跟班级内有没有重名的
                StudentImportData data = student.get(0);
                if (existStudents.contains(studentName)) {
                    data.failed("学生姓名在该班级中已注册，如果重名请加标记以区分；如需更新原学生学号填涂号，请在【班级管理】中逐个班级导入更新");
                    result.failed(data);
                }
            }

            // 历史删除恢复
            if (recoverStudent) {
                List<GroupKlxStudentRef> recoverGroupKlxStudentRefs = new ArrayList<>();
                List<Group> groups = clazzGroupServiceClient.getGroupService().findGroupsByClazzId(clazz.getId()).getUninterruptibly();
                List<GroupKlxStudentRef> deleteGroupKlxStudentRefs = new ArrayList<>();
                groups.forEach(g -> deleteGroupKlxStudentRefs.addAll(asyncGroupServiceClient.getAsyncGroupService().loadDeletedGroupKlxStudentRefs(g.getId()).getUninterruptibly()));
                Set<String> klxStudentIds = deleteGroupKlxStudentRefs.stream().map(GroupKlxStudentRef::getKlxStudentId).collect(Collectors.toSet());
                Map<String, List<GroupKlxStudentRef>> groupKlxStudentRefMap = asyncGroupServiceClient.getAsyncGroupService().findGroupKlxStudentRefsByStudents(klxStudentIds).getUninterruptibly();
                // 过滤掉有组的学生
                Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(klxStudentIds.stream().filter(s -> groupKlxStudentRefMap.get(s).isEmpty()).collect(Collectors.toSet()));
                // 重名恢复取最近更新的
                Map<String, KlxStudent> klxStudentNameMap = klxStudentMap.values().stream().collect(Collectors.toMap(KlxStudent::getName, Function.identity(), (u, v) -> {
                    if (u.getUpdateTime().after(v.getUpdateTime())) {
                        return u;
                    }
                    return v;
                }));
                // 过滤掉班级里面已经有的学生名称
                List<KlxStudent> deletedStudents = klxStudentMap.values().stream().filter(s -> !existStudents.contains(s.getName())).collect(Collectors.toList());
                // 学生的组
                Map<String, List<GroupKlxStudentRef>> deleteKlxStudentRefIdsMap = deleteGroupKlxStudentRefs.stream().collect(Collectors.groupingBy(GroupKlxStudentRef::getKlxStudentId));
                // 过滤掉校验没有成功的学生
                Map<String, StudentImportData> stuInfoMap = students.stream().filter(data -> !StringUtils.isNotBlank(data.getReason())).collect(Collectors.toMap(StudentImportData::getStudentName, Function.identity(), (u, v) -> u));
                List<StudentImportData> recoverData = new ArrayList<>();
                // 需要恢复的学生学号和学生ID, 恢复的时候需要去更新的学号
                Map<String, String> recoverNewKlxStudentNumber = new HashMap<>();
                deletedStudents.forEach(k -> {
                    if (stuInfoMap.containsKey(k.getName())) {
                        recoverGroupKlxStudentRefs.addAll(deleteKlxStudentRefIdsMap.get(k.getId()));
                        StudentImportData studentImportData = stuInfoMap.get(k.getName());
                        recoverData.add(studentImportData);
                        recoverNewKlxStudentNumber.put(k.getId(), studentImportData.getStudentNumber());
                        // 移除
                        stuInfoMap.remove(k.getName());
                    }
                });
                Map<Long, List<GroupKlxStudentRef>> groupKlxStudentRefs = recoverGroupKlxStudentRefs.stream().collect(Collectors.groupingBy(GroupKlxStudentRef::getGroupId));

                if (!groupKlxStudentRefs.isEmpty()) {
                    List<Long> groupRefIds = groupKlxStudentRefs.values().stream().flatMap(Collection::stream).map(GroupKlxStudentRef::getId).collect(Collectors.toList());
                    MapMessage mapMessage = newKuailexueServiceClient.recoverGroupKlxStudentRefs(groupKlxStudentRefs.keySet().stream().findFirst().get(), recoverNewKlxStudentNumber, groupRefIds);

                    Map<String, String> klxScanNumbers = new HashMap<>();
                    if (mapMessage.get("klxScanNumbers") != null) {
                        klxScanNumbers = (Map<String, String>) mapMessage.get("klxScanNumbers");
                    }
                    // 移除已恢复学生
                    students = stuInfoMap.values().stream().collect(Collectors.toList());
                    // 记录日志
                    Map<String, String> finalKlxScanNumbers = klxScanNumbers;
                    recoverData.forEach(data ->{
                        data.recovered(finalKlxScanNumbers.get(data.getStudentName()));
                        result.recovered(data);
                    });
                }
            }

            // 剩下的数据再过一遍
            for (StudentImportData data : students) {
                if (StringUtils.isNotBlank(data.getReason())) {
                    continue;
                }

                if (!data.introspect()) {
                    result.failed(data);
                    continue;
                }
                studentInfo.add(new KeyValuePair<>(data.getStudentName(), data.getStudentNumber()));
                processData.add(data);
            }

            // 没有就算咯
            if (CollectionUtils.isEmpty(studentInfo)) {
                continue;
            }

            // 创建学生，然后加入班级下所有group
            MapMessage message = specialTeacherServiceClient.studentJoinClazz(clazz.getId(), studentInfo, schoolId);
            if (message.isSuccess()) {
                Map<String, String> scanNumberMap = (Map<String, String>) message.get("scanNumberMap");
                processData.forEach(data -> {
                    data.success(scanNumberMap.getOrDefault(data.getStudentName(), "未生成"));
                    result.success(data);
                });
            } else {
                processData.forEach(data -> {
                    data.failed("系统批量添加学生失败, 原因：" + message.getInfo());
                    result.failed(data);
                });
            }
        }
        return result;
    }

    private List<MarkStudentData> parseMarkStudentData(Sheet sheet) {
        List<MarkStudentData> data = new LinkedList<>();
        // 从第一行开始
        for (int index = 1; index <= sheet.getLastRowNum(); index++) {
            Row row = sheet.getRow(index);
            if (row == null) {
                continue;
            }

            String grade = WorkbookUtils.getCellValue(row.getCell(0));
            String className = WorkbookUtils.getCellValue(row.getCell(1));
            String studentName = WorkbookUtils.getCellValue(row.getCell(2));
            String studentNumber = WorkbookUtils.getCellValue(row.getCell(3));
            String isMarked = WorkbookUtils.getCellValue(row.getCell(4));

            // 读到了一个空行，忽略吧
            if (StringUtils.isBlank(grade) && StringUtils.isBlank(className) && StringUtils.isBlank(studentName)
                    && StringUtils.isBlank(studentNumber) && StringUtils.isBlank(isMarked)) {
                continue;
            }
            data.add(new MarkStudentData(grade, className, studentName, studentNumber, isMarked));
        }

        return data;
    }

    private MarkStudentResult executeMarkStudent(List<MarkStudentData> markStudentDatas) {
        MarkStudentResult markStudentResult = new MarkStudentResult();

        // 找出导入的excel中所包含的班级
        Set<String> clazzNameSet = markStudentDatas.stream().map(MarkStudentData::classNameKey).distinct().collect(Collectors.toSet());

        List<Clazz> clazzes = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(currentSchoolId()).enabled().toList()
                .stream().filter(clazz -> clazzNameSet.contains(clazz.getClazzLevel().getDescription() + clazz.getClassName()))
                .collect(Collectors.toList());

        List<Long> clazzIds = clazzes.stream().map(Clazz::getId).collect(Collectors.toList());

        Map<Long, List<KlxStudent>> clazzIdKlxStudentMap = specialTeacherLoaderClient.loadKlxStudentsByClazzIds(clazzIds);

        Map<String, List<KlxStudent>> clazzNameKlxStudentMap = new HashMap<>();
        // 班级下的学生
        clazzes.forEach(clazz -> {
            String clazzName = clazz.getClazzLevel().getDescription() + clazz.getClassName();
            clazzNameKlxStudentMap.put(clazzName, clazzIdKlxStudentMap.get(clazz.getId()));
        });

        for (MarkStudentData data : markStudentDatas) {
            if (!"借读生".equals(data.getIsMarked())) {
                data.failed("未识别标记信息");
                markStudentResult.failed(data);
                continue;
            }

            String clazzNameKey = data.classNameKey();
            List<KlxStudent> klxStudents = clazzNameKlxStudentMap.get(clazzNameKey);
            if (CollectionUtils.isEmpty(klxStudents)) {
                data.failed("未找到学生所在班级");
                markStudentResult.failed(data);
                continue;
            }
            int count = 0;
            KlxStudent targetStudent = null;
            for (KlxStudent klxStudent : klxStudents) {
                if (data.getStudentName().equals(klxStudent.getName())
                        && data.getStudentNumber().equals(klxStudent.getStudentNumber())) {
                    count++;
                    targetStudent = klxStudent;
                }
            }

            if (count == 0) {
                data.failed("在该班级未找到该学生，请检查学生信息、确认该学生是否已注册");
                markStudentResult.failed(data);
            } else if (count > 1) {
                data.failed("在该班级有多个重名学生，请确认学生信息");
                markStudentResult.failed(data);
            } else {
                targetStudent.setIsMarked(Boolean.TRUE);
                specialTeacherServiceClient.markReadingStudent(targetStudent);
                data.success("");
                markStudentResult.success(data);
            }
        }
        return markStudentResult;
    }

    @EqualsAndHashCode(of = "clazzId")
    private static class ClazzShareGroup {
        @Getter private Long clazzId;
        @Getter private Long newTeacherGroupId;
        @Getter private Set<Long> shareGroups;

        ClazzShareGroup(Long clazzId) {
            this.clazzId = clazzId;
            newTeacherGroupId = null;
            shareGroups = new HashSet<>();
        }

        void addNewGroup(Long groupId) {
            this.shareGroups.add(groupId);
            this.newTeacherGroupId = groupId;
        }

        void addAllGroup(List<GroupMapper> groups) {
            List<Long> groupIds = groups.stream().map(GroupMapper::getId).collect(Collectors.toList());
            this.shareGroups.addAll(groupIds);
        }

    }

}
