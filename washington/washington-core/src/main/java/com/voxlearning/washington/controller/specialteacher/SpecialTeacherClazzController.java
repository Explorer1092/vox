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

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.service.clazz.client.GroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.NewClazzServiceClient;
import com.voxlearning.utopia.service.user.api.constants.ClazzCreatorType;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.washington.mapper.specialteacher.base.ClassGroupDictionary;
import com.voxlearning.washington.mapper.specialteacher.base.KlxStudentClazz;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import com.voxlearning.washington.mapper.specialteacher.changeclass.ChangeClassImportData;
import com.voxlearning.washington.mapper.specialteacher.changeclass.ChangeClassImportResult;
import com.voxlearning.washington.mapper.specialteacher.linkclass.LinkClassImportData;
import com.voxlearning.washington.mapper.specialteacher.linkclass.LinkClassImportResult;
import com.voxlearning.washington.support.WorkbookUtils;
import lombok.Cleanup;
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
import java.util.stream.Collectors;

/**
 * 教务老师-班级管理相关
 * FIXME 后续把班级管理的放到这里咯
 */
@Controller
@RequestMapping("/specialteacher/clazz")
public class SpecialTeacherClazzController extends AbstractSpecialTeacherController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private GroupServiceClient groupServiceClient;
    @Inject private NewClazzServiceClient newClazzServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String clazzManage() {
        if (currentSpecialTeacher() == null) {
            return "redirect: /";
        }
        return "specialteacherV2/clazzmanage";
    }
    //========================================= 教务-班级管理 =========================================

    /**
     * 调整班级
     */
    @ResponseBody
    @RequestMapping(value = "adjustclazz.vpage", method = RequestMethod.POST)
    public MapMessage adjustClazz() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }
        Long clazzId = getRequestLong("clazzId");
        Integer type = getRequestInt("type"); // 1.更改班级属性，2.删除班级
        String clazzName = getRequestString("clazzName");

        if (type == 1 && StringUtils.isEmpty(clazzName)) {
            return MapMessage.errorMessage("班级名称不能为空");
        }

        if (type == 1) {
            // 更改班级属性
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz.isWalkingClazz()) {
                Boolean flag = false;
                Set<String> subjectNameSet = SpecialTeacherConstants.subjectNameMap.keySet();
                for (String subjectName : subjectNameSet) {
                    if (clazzName.startsWith(subjectName)) {
                        flag = true;
                    }
                }
                if (!flag) {
                    return MapMessage.errorMessage("教学班名称请以学科为前缀，例：语文**班，数学**班");
                }
                // 教学班设置分层
                String stage = getRequestString("stageType");
                Long groupId = getRequestLong("groupId");
                if (StringUtils.isNotBlank(stage)) {
                    boolean rep = groupServiceClient.getGroupService().updateGroupStageType(groupId, StageType.parse(stage)).getUninterruptibly();
                    if (!rep) {
                        return MapMessage.errorMessage("修改教学班分层属性失败");
                    }
                }
            }

            School school = schoolLoaderClient.getSchoolLoader().loadSchool(clazz.getSchoolId()).getUninterruptibly();
            if (clazz.isPublicClazz() && school.isSeniorSchool()) {
                String artScienceType = getRequestString("artScienceType");
                if (StringUtils.isNotBlank(artScienceType)) { //artScienceType为空就是行政班下没有group,只能改班级名称
                    if (!Arrays.asList(ArtScienceType.values()).contains(ArtScienceType.of(artScienceType))) {
                        return MapMessage.errorMessage("请选择正确的文理科属性");
                    }
                    // 行政班设置文理科属性
                    List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadClazzGroups(clazzId);
                    // 看一个group上的文理科属性就好，不同就将班级下的所有group都更新成传入的文理科属性
                    GroupMapper groupMapper = groupMappers.get(0);
                    if (ArtScienceType.of(artScienceType) != groupMapper.getArtScienceType()) {
                        Set<Long> groupIds = groupMappers.stream().map(GroupMapper::getId).collect(Collectors.toSet());
                        MapMessage msg = clazzServiceClient.updateClazzArtScienceType(groupIds, ArtScienceType.of(artScienceType));
                        if (ConversionUtils.toInt(msg.get("rows")) <= 0) {
                            return MapMessage.errorMessage("修改文理科属性失败");
                        }
                    }
                }
            }

            if (!clazzName.equals(clazz.getClassName())) {
                int row = newClazzServiceClient.getNewClazzService()
                        .updateClazzName(clazzId, clazzName)
                        .getUninterruptibly();

                if (row <= 0) {
                    return MapMessage.errorMessage("修改班级名称失败");
                }
            }

            return MapMessage.successMessage();
        } else if (type == 2) {
            // 删除班级
            List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadClazzGroups(clazzId);
            if (groupMappers.size() > 0) {
                return MapMessage.errorMessage("该班级内已创建班群，如需删除，请在班级详情页删除相应班群");
            }
            return specialTeacherServiceClient.deleteClazz(clazzId);
        }
        return MapMessage.errorMessage();
    }

    /**
     * 打散换班-检查导入数据
     */
    @RequestMapping(value = "checkchangeclassdata.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage checkChangeClassData() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }
        Workbook workbook = getRequestWorkbook("changeStudentData");
        if (workbook == null) {
            return MapMessage.errorMessage("上传文件失败");
        }

        MapMessage checkMsg = checkWorkbook(workbook, 2000);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        MapMessage msg = parseChangeClassData(workbook.getSheetAt(0));
        if (!msg.isSuccess()) {
            return msg;
        }
        List<ChangeClassImportData> importData = (List<ChangeClassImportData>) msg.get("data");
        ClazzType clazzType = (ClazzType) msg.get("importType");

        if (CollectionUtils.isEmpty(importData)) {
            return MapMessage.errorMessage("文档内容为空");
        }
        if (importData.size() > 2000) {
            return MapMessage.errorMessage("文档人数异常，最多不超过2000人");
        }
        if (clazzType == null) {
            return MapMessage.errorMessage("导入的班级类型无效");
        }

        School school = currentSchool();
        List<ClazzLevel> clazzLevelList = importData.stream()
                .map(ChangeClassImportData::getGrade)
                .distinct().collect(Collectors.toList());

        ClassGroupDictionary dictionary = buildChangeClassDictionary(school, clazzLevelList);

        // clazzLevel_clazzName, clazzId 字典， 用于匹配找不到的班级 以及 换班操作时取到班级ID
        Map<String, Long> clazzNameData = new HashMap<>();
        List<String> importClazzName = importData.stream().map(ChangeClassImportData::classKey).distinct().collect(Collectors.toList());
        Set<String> duplicateClazzNames = new TreeSet<>();
        for (Clazz clazz : dictionary.queryAllClazz()) {
            String name = StringUtils.join(clazz.getClassLevel(), "_", clazz.getClassName());
            // 检查导入文件中的班级在学校中是否存在重复班级，
            if (clazzNameData.containsKey(name) && importClazzName.contains(name)) {
                duplicateClazzNames.add(clazz.formalizeClazzName());
                continue;
            }
            clazzNameData.put(name, clazz.getId());
        }
        if (duplicateClazzNames.size() > 0) {
            return MapMessage.errorMessage("学校存在同名班级 : {} ， 请检查数据", StringUtils.join(duplicateClazzNames, "、"));
        }

        // 3. 文件内数据检查
        //    A. 班级, 年级下目标班级是否存在
        Map<String, List<ChangeClassImportData>> clazzNameList = importData.stream()
                .collect(Collectors.groupingBy(ChangeClassImportData::classKey, Collectors.toList()));
        List<String> notFoundClazzList = clazzNameList.keySet()
                .stream().filter(p -> !clazzNameData.containsKey(p))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notFoundClazzList)) {
            Set<String> notFoundClazzName = new TreeSet<>();
            for (String classKey : notFoundClazzList) {
                notFoundClazzName.add(SpecialTeacherConstants.parseClassKeyToClassName(classKey));
            }
            return MapMessage.errorMessage("班级 {} 不存在", StringUtils.join(notFoundClazzName, "、"));
        }

        //   B. 班级, 存在的班级下是否有组 以及 是否有多个同学科的组
        Set<String> existClazzList = clazzNameList.keySet().stream()
                .filter(clazzNameData::containsKey).collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(existClazzList)) {
            Set<String> noneGroupClazz = new TreeSet<>();              // 没有组的班级
            Map<String, Set<Subject>> duplicateSubjectClazz = new TreeMap<>();        // 多个重复学科组的班级

            for (String classKey : existClazzList) {
                Long clazzId = clazzNameData.get(classKey);
                if (!dictionary.existClass(clazzId)) {
                    continue;
                }

                if (dictionary.checkNoneGroup(clazzId)) {
                    noneGroupClazz.add(SpecialTeacherConstants.parseClassKeyToClassName(classKey));
                }

                Set<Subject> dupSubject = dictionary.queryDuplicateSubjects(clazzId);
                if (CollectionUtils.isNotEmpty(dupSubject)) {
                    duplicateSubjectClazz.put(SpecialTeacherConstants.parseClassKeyToClassName(classKey), dupSubject);
                }
            }

            StringBuilder errorMsg = new StringBuilder();
            boolean error = false;
            if (noneGroupClazz.size() > 0) {
                errorMsg.append(StringUtils.join(noneGroupClazz, "、")).append("中暂无班群，请先创建班群").append("<br/>");
                error = true;
            }
            if (duplicateSubjectClazz.size() > 0) {
                duplicateSubjectClazz.forEach((className, subjects) -> {
                    List<String> subjectName = subjects.stream().map(Subject::getValue).collect(Collectors.toList());
                    errorMsg.append(className).append("中有多个（").append(StringUtils.join(subjectName, "、")).append("）班群，请先去班级管理中将错误班群删除").append("<br/>");
                });
                error = true;
            }
            if (error) {
                return MapMessage.errorMessage(errorMsg.toString());
            }
        }


        //    C. 学生, 年级下目标学生存在并且唯一
        // key:clazzLevel_studentName value:changeStudentImportDataList
        Map<String, List<ChangeClassImportData>> importDataMappers = importData.stream()
                .collect(Collectors.groupingBy(ChangeClassImportData::classStudentKey, Collectors.toList()));

        for (Map.Entry<String, List<ChangeClassImportData>> entry : importDataMappers.entrySet()) {
            String studentKey = entry.getKey();
            List<ChangeClassImportData> data = entry.getValue();
            List<String> list = data.stream().map(ChangeClassImportData::getTargetClazz).distinct().collect(Collectors.toList());
            if (list.size() > 1) {
                if (ClazzType.PUBLIC == clazzType) {
                    return MapMessage.errorMessage("学生 {} 不可以绑定到两个行政班下，请检查文件", studentKey.split("_")[1]);
                }

                // 教学班检查这些班级是不是不同学科
                if (ClazzType.WALKING == clazzType) {
                    Set<Subject> subjects = new HashSet<>();
                    List<Group> importGroups = data.stream().map(d -> dictionary.queryClazzByKey(d.classKey()))
                            .filter(Objects::nonNull)
                            .map(Clazz::getId)
                            .map(dictionary::queryWalkingClazzGroup)
                            .collect(Collectors.toList());

                    for (Group group : importGroups) {
                        if (group == null || group.getSubject() == null) {
                            continue;
                        }
                        if (subjects.contains(group.getSubject())) {
                            return MapMessage.errorMessage("学生 {} 不可以绑定到两个相同学科教学班下，请检查文件", studentKey.split("_")[1]);
                        }
                        subjects.add(group.getSubject());
                    }
                }
            }
        }

        return MapMessage.successMessage();
    }

    /**
     * 打散换班-执行导入数据
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "executechangeclass.vpage", method = RequestMethod.POST)
    public void executeChangeClass() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return;
        }

        Workbook workbook = getRequestWorkbook("changeStudentData");
        if (workbook == null) {
            return;
        }
        MapMessage checkMsg = checkWorkbook(workbook, 2000);
        if (!checkMsg.isSuccess()) {
            return;
        }

        // 开始解析咯
        MapMessage msg = parseChangeClassData(workbook.getSheetAt(0));
        List<ChangeClassImportData> importData = (List<ChangeClassImportData>) msg.get("data");
        ClazzType clazzType = (ClazzType) msg.get("importType");

        // 开始导入咯
        ChangeClassImportResult importResult = executeChangeClassData(importData, clazzType);
        String fileName = SpecialTeacherConstants.generateXlsFileName("打散换班处理结果");

        if (importResult == null) {
            return;
        }

        // 开始导出咯
        HSSFWorkbook resultFile = createXlsExcelExportData(importResult.toExportExcelData());
        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            resultFile.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception ex) {
            logger.error("Failed download change class result.", ex);
        }
    }

    /**
     * 复制教学班学生-检查导入数据
     */
    @RequestMapping(value = "checklinkclassdata.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage checkLinkClassData() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        Workbook workbook = getRequestWorkbook("changeStudentData");
        if (workbook == null) {
            return MapMessage.errorMessage("上传文件失败");
        }

        MapMessage checkMsg = checkWorkbook(workbook, 2000);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }

        MapMessage msg = parseLinkClassData(workbook.getSheetAt(0));
        if (!msg.isSuccess()) {
            return msg;
        }
        List<LinkClassImportData> importData = (List<LinkClassImportData>) msg.get("data");
        if (CollectionUtils.isEmpty(importData)) {
            return MapMessage.errorMessage("文档内容为空");
        }
        if (importData.size() > 2000) {
            return MapMessage.errorMessage("文档人数异常，最多不超过2000人");
        }

        Long schoolId = currentSchoolId();
        ClassGroupDictionary dictionary = buildLinkClassDictionary(schoolId, importData);

        List<LinkClassImportData> emptyStudent = importData.stream().filter(s -> dictionary.queryKlxStudentId(s.classStudentKey()) == null).collect(Collectors.toList());
        if (!emptyStudent.isEmpty()) {
            return MapMessage.errorMessage("学生不存在：{}， 请检查数据", StringUtils.join(emptyStudent.stream().map(LinkClassImportData::getStudentName).collect(Collectors.toList()), "、"));
        }

        // clazzLevel_clazzName, clazzId 字典， 用于匹配找不到的班级 以及 换班操作时取到班级ID
        Map<String, Long> clazzNameData = new HashMap<>();
        List<String> importClazzName = importData.stream().map(LinkClassImportData::classKey).distinct().collect(Collectors.toList());
        Set<String> duplicateClazzNames = new TreeSet<>();
        for (Clazz clazz : dictionary.queryAllClazz()) {
            String name = StringUtils.join(clazz.getClassLevel(), "_", clazz.getClassName());
            // 检查导入文件中的班级在学校中是否存在重复班级，
            if (clazzNameData.containsKey(name) && importClazzName.contains(name)) {
                duplicateClazzNames.add(clazz.formalizeClazzName());
                continue;
            }
            clazzNameData.put(name, clazz.getId());
        }
        if (duplicateClazzNames.size() > 0) {
            return MapMessage.errorMessage("学校存在同名班级 : {} ， 请检查数据", StringUtils.join(duplicateClazzNames, "、"));
        }

        // 3. 文件内数据检查
        //    A. 班级, 年级下目标班级是否存在
        Map<String, List<LinkClassImportData>> clazzNameList = importData.stream()
                .collect(Collectors.groupingBy(LinkClassImportData::classKey, Collectors.toList()));
        List<String> notFoundClazzList = clazzNameList.keySet().stream().filter(p -> !clazzNameData.containsKey(p)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notFoundClazzList)) {
            Set<String> notFoundClazzName = new TreeSet<>(); // 将数字年级转为汉字 11 -> 高一
            for (String classKey : notFoundClazzList) {
                notFoundClazzName.add(SpecialTeacherConstants.parseClassKeyToClassName(classKey));
            }
            return MapMessage.errorMessage("班级 {} 不存在", StringUtils.join(notFoundClazzName, "、"));
        }

        //   B. 班级, 存在的班级下是否有组 以及 是否有多个同学科的组
        Set<String> existClazzList = clazzNameList.keySet().stream().filter(clazzNameData::containsKey).collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(existClazzList)) {
            Set<String> noneGroupClazz = new TreeSet<>();              // 没有组的班级
            Map<String, Set<Subject>> duplicateSubjectClazz = new TreeMap<>();        // 多个重复学科组的班级

            for (String classKey : existClazzList) {
                Long clazzId = clazzNameData.get(classKey);
                if (!dictionary.existClass(clazzId)) {
                    continue;
                }
                if (dictionary.checkNoneGroup(clazzId)) {
                    noneGroupClazz.add(SpecialTeacherConstants.parseClassKeyToClassName(classKey));
                }

                Set<Subject> dupSubject = dictionary.queryDuplicateSubjects(clazzId);
                if (CollectionUtils.isNotEmpty(dupSubject)) {
                    duplicateSubjectClazz.put(SpecialTeacherConstants.parseClassKeyToClassName(classKey), dupSubject);
                }
            }

            StringBuilder errorMsg = new StringBuilder();
            boolean error = false;
            if (noneGroupClazz.size() > 0) {
                errorMsg.append(StringUtils.join(noneGroupClazz, "、")).append("中暂无班群，请先创建班群").append("<br/>");
                error = true;
            }
            if (duplicateSubjectClazz.size() > 0) {
                duplicateSubjectClazz.forEach((className, subjects) -> {
                    List<String> subjectName = subjects.stream().map(Subject::getValue).collect(Collectors.toList());
                    errorMsg.append(className).append("中有多个（").append(StringUtils.join(subjectName, "、")).append("）班群，请先去班级管理中将错误班群删除").append("<br/>");
                });
                error = true;
            }
            if (error) {
                return MapMessage.errorMessage(errorMsg.toString());
            }
        }

        //    C. 学生, 年级下目标学生存在并且唯一
        // key:clazzLevel_studentName value:changeStudentImportDataList
        Map<String, List<LinkClassImportData>> importDataMappers = importData.stream()
                .collect(Collectors.groupingBy(LinkClassImportData::classStudentKey, Collectors.toList()));

        for (Map.Entry<String, List<LinkClassImportData>> entry : importDataMappers.entrySet()) {
            String studentKey = entry.getKey();
            List<LinkClassImportData> data = entry.getValue();
            List<String> list = data.stream().map(LinkClassImportData::getTargetClazz).distinct().collect(Collectors.toList());
            if (list.size() > 1) {
                return MapMessage.errorMessage("同一个学生 {} 不可以绑定到到两个班级下", studentKey.split("_")[1]);
            }

            //     D. 一个学生不能同时存在于同一年级下相同学科的两个教学班中
            KlxStudentClazz klxStudent = dictionary.queryKlxStudentId(studentKey);
            for (LinkClassImportData csData : entry.getValue()) {
                String classKey = csData.classKey();
                Clazz clazz = dictionary.queryClazzByKey(classKey);
                Group group = dictionary.queryWalkingClazzGroup(clazz.getId());
                Set<Long> studentGroupIds = asyncGroupServiceClient.getAsyncGroupService()
                        .findGroupKlxStudentRefsByStudent(klxStudent.getKlxId())
                        .getUninterruptibly()
                        .stream()
                        .map(GroupKlxStudentRef::getGroupId)
                        .filter(g -> !Objects.equals(group.getId(), g))
                        .collect(Collectors.toSet());

                List<Long> sameSubjectClazzs = groupLoaderClient.getGroupLoader().loadGroups(studentGroupIds)
                        .getUninterruptibly()
                        .values()
                        .stream()
                        .filter(g -> GroupType.WALKING_GROUP == g.getGroupType())
                        .filter(g -> Objects.equals(group.getSubject(), g.getSubject()))
                        .map(Group::getClazzId)
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(sameSubjectClazzs)) {
                    Set<String> clazzName = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            .loadClazzs(sameSubjectClazzs)
                            .stream()
                            .map(Clazz::formalizeClazzName)
                            .collect(Collectors.toCollection(TreeSet::new));

                    return MapMessage.errorMessage(
                            "学生{}已经存在于相同{}学科的教学班：({}), 不允许加入，请确认学生班级信息",
                            csData.getStudentName(),
                            group.getSubject() == null ? "" : group.getSubject().getValue(),
                            StringUtils.join(clazzName, "、")
                    );
                }
            }
        }

        return MapMessage.successMessage();
    }

    /**
     * 复制教学班学生-执行导入数据
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "executelinkclass.vpage", method = RequestMethod.POST)
    public void executeLinkClass() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return;
        }

        Workbook workbook = getRequestWorkbook("changeStudentData");
        if (workbook == null) {
            return;
        }
        MapMessage checkMsg = checkWorkbook(workbook, 2000);
        if (!checkMsg.isSuccess()) {
            return;
        }

        // 开始解析咯
        MapMessage msg = parseLinkClassData(workbook.getSheetAt(0));
        List<LinkClassImportData> importData = (List<LinkClassImportData>) msg.get("data");

        Long schoolId = currentSchoolId();
        // 开始导入咯
        LinkClassImportResult importResult = executeLinkClazzData(schoolId, importData);
        String fileName = SpecialTeacherConstants.generateXlsFileName("复制教学班学生处理结果");

        if (importResult == null) {
            return;
        }

        // 开始导出咯
        HSSFWorkbook resultFile = createXlsExcelExportData(importResult.toExportExcelData());
        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            resultFile.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception ex) {
            logger.error("Failed download change clazz result.", ex);
        }
    }

    /**
     * 删除班群
     */
    @RequestMapping(value = "deletegroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteGroup() {
        Long groupId = getRequestLong("groupId");
        GroupMapper group = deprecatedGroupLoaderClient.loadGroup(groupId, false);
        if (group == null) {
            return MapMessage.errorMessage("班组不存在 或者 已经删除");
        }

        String desc = getRequestString("desc");
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("请选择删除原因");
        }
        List<KlxStudent> students = newKuailexueLoaderClient.loadKlxGroupStudents(groupId);
        students = students.stream().filter(KlxStudent::isRealStudent).collect(Collectors.toList());
        if (students.size() >= 5) {
            return MapMessage.errorMessage("班内已注册学生，无法删除班群，如有问题请联系客服处理。");
        }

        String key = "SpecialTeacher::deleteGroup::" + groupId;
        MapMessage resultMsg = MapMessage.successMessage();
        try {
            AtomicLockManager.getInstance().acquireLock(key);
            // 找到关联的组，i.e. 班群
            Set<Long> sharedGroupIds = new HashSet<>(deprecatedGroupLoaderClient.loadSharedGroupIds(groupId));
            sharedGroupIds.add(groupId);

            // 班群下所有的老师
            List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(sharedGroupIds)
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(teachers)) {
                sharedGroupIds.forEach(
                        gid -> groupServiceClient.getGroupService().disableGroup(gid).getUninterruptibly()
                );
                return MapMessage.successMessage();
            }

            for (Teacher teacher : teachers) {
                Long teacherId = teacher.getId();

                newKuailexueServiceClient.deleteKlxStudentsByTeacherAndClazz(teacherId, group.getClazzId());

                // 删除老师与组关系
                MapMessage msg = clazzServiceClient.teacherExitSystemClazz(teacherId, group.getClazzId(), true, OperationSourceType.jwls);
                if (msg.isSuccess()) {
                    String operation = "删除老师的组[" + groupId + "]并移除了组内的学生";
                    // 记录 UserServiceRecord
                    logUserServiceRecord(teacherId, operation, UserServiceRecordOperationType.班组信息变更, "删除班级");
                }
            }
        } catch (CannotAcquireLockException dup) {
            resultMsg = MapMessage.errorMessage("正在处理，请勿重复操作");
        } catch (Exception ex) {
            resultMsg = MapMessage.errorMessage("系统异常，请联系管理员");
        } finally {
            AtomicLockManager.getInstance().releaseLock(key);
        }
        return resultMsg;
    }

    //==========================================================================================================
    //=================================        打  散  换  班  相  关         ===================================
    //==========================================================================================================
    private MapMessage parseChangeClassData(Sheet sheet) {
        List<ChangeClassImportData> data = new LinkedList<>();

        ClazzType importType = null;
        // 从第一行开始
        for (int index = 1; index <= sheet.getLastRowNum(); index++) {
            Row row = sheet.getRow(index);
            if (row == null) {
                continue;
            }
            String gradeName = WorkbookUtils.getCellValue(row.getCell(0));
            String studentName = WorkbookUtils.getCellValueTrim(row.getCell(1));
            String targetClazz = WorkbookUtils.getCellValueTrim(row.getCell(2));
            String importTypeName = WorkbookUtils.getCellValue(row.getCell(3));

            // 读到了一个空行，忽略吧
            if (SpecialTeacherConstants.isAllBlank(gradeName, studentName, targetClazz, importTypeName)) {
                continue;
            }

            if (StringUtils.isAnyBlank(gradeName, studentName, targetClazz, importTypeName)) {
                return MapMessage.errorMessage("第{}行：必填项存在空值，请检查文件", (index + 1));
            }

            ChangeClassImportData info = new ChangeClassImportData(gradeName, studentName, targetClazz, importTypeName);

            if (info.getImportType() == null) {
                return MapMessage.errorMessage("第{}行：无效的导入班级类型", (index + 1));
            }

            if (info.getGrade() == null) {
                return MapMessage.errorMessage("第{}行：无效的年级", (index + 1));
            }

            if (importType == null) {
                importType = info.getImportType();
            } else if (importType != info.getImportType()) {
                return MapMessage.errorMessage("一次导入只能处理一种类型打散换班（行政班or教学班）");
            }

            data.add(info);
        }
        return MapMessage.successMessage().add("data", data).add("importType", importType);
    }

    private ChangeClassImportResult executeChangeClassData(List<ChangeClassImportData> importData, ClazzType clazzType) {

        School school = currentSchool();

        ChangeClassImportResult result = new ChangeClassImportResult();
        List<ClazzLevel> clazzLevelList = importData.stream().map(ChangeClassImportData::getGrade)
                .distinct().collect(Collectors.toList());
        ClassGroupDictionary dictionary = buildChangeClassDictionary(school, clazzLevelList);

        //    C. 学生, 年级下目标学生存在并且唯一
        // key:clazzLevel_studentName value:changeStudentImportDataList
        Map<String, List<ChangeClassImportData>> importDataMappers = importData.stream()
                .collect(Collectors.groupingBy(ChangeClassImportData::classStudentKey, Collectors.toList()));

        for (Map.Entry<String, List<ChangeClassImportData>> entry : importDataMappers.entrySet()) {
            String studentKey = entry.getKey();
            KlxStudentClazz klxStudentClazz = dictionary.queryKlxStudentId(studentKey);
            for (ChangeClassImportData csData : entry.getValue()) {
                Set<Long> groupIds = dictionary.queryClazzGroupIds(csData.classKey());
                Clazz clazz = dictionary.queryClazzByKey(csData.classKey());
                if (clazz == null || !Objects.equals(clazzType.getType(), clazz.getClassType())) {
                    csData.failed("没有找到对应的班级，请检查班级");
                    result.failed(csData);
                    continue;
                }

                if (CollectionUtils.isEmpty(groupIds)) {
                    csData.failed("班级中暂无班群，请先创建班群");
                    result.failed(csData);
                    continue;
                }

                if (klxStudentClazz == null) {
                    String[] array = studentKey.split("_");
                    String cn = ClazzLevel.getDescription(ConversionUtils.toInt(array[0])) + array[1];
                    csData.failed("未找到此学生: " + cn);
                    result.failed(csData);
                    continue;
                }
                String klxStudentId = klxStudentClazz.getKlxId();
                if (dictionary.containsDuplicateKlxStudents(studentKey)) {
                    // 有重名学生且ID不一样
                    Set<KlxStudentClazz> klxStudent = dictionary.queryDuplicateKlxStudents(studentKey);
                    klxStudent.add(dictionary.queryKlxStudentId(studentKey));
                    // 写入到失败excel
                    Map<String, List<KlxStudentClazz>> klxStudentMap = klxStudent.stream().collect(Collectors.groupingBy(k -> k.getClazz().getClassName()));
                    StringBuilder builder = new StringBuilder();
                    for (Map.Entry<String, List<KlxStudentClazz>> klx : klxStudentMap.entrySet()) {
                        builder.append(klx.getKey()).append(",").append("ID为：").append(StringUtils.join(klx.getValue().stream().map(KlxStudentClazz::getKlxId).collect(Collectors.toList()), "、")).append("\r\n");
                    }
                    csData.failed("在该年级行政班中找到多个该姓名学生，请在班级管理中手动处理（" + builder.toString() + "）");
                    result.failed(csData);
                    continue;
                }

                // 打散换班的话，先断开与原先组的关联
                List<GroupKlxStudentRef> breakGroupKlxRefs = null;

                if (ClazzType.PUBLIC == clazzType) {
                    breakGroupKlxRefs = asyncGroupServiceClient.getAsyncGroupService()
                            .findGroupKlxStudentRefsByStudentWithoutWalkingGroup(klxStudentId)
                            .getUninterruptibly();
                } else if (ClazzType.WALKING == clazzType) {
                    // 如果是一个教学班，去看一下这个学生有没有其他学科的教学班
                    Group walkingGroup = dictionary.queryWalkingClazzGroup(clazz.getId());
                    if (walkingGroup != null) {
                        List<GroupKlxStudentRef> groupKlxRefs = asyncGroupServiceClient.getAsyncGroupService()
                                .findGroupKlxStudentRefsByStudent(klxStudentId)
                                .getUninterruptibly();

                        Set<Long> klxGroupIds = groupKlxRefs.stream()
                                .map(GroupKlxStudentRef::getGroupId)
                                .collect(Collectors.toSet());

                        Map<Long, Group> groupMap = groupLoaderClient.getGroupLoader()
                                .loadGroups(klxGroupIds)
                                .getUninterruptibly();

                        Set<Long> sameSubjectGroups = groupMap.values()
                                .stream()
                                .filter(g -> GroupType.WALKING_GROUP == g.getGroupType())
                                .filter(g -> Objects.equals(walkingGroup.getSubject(), g.getSubject()))
                                .map(Group::getId)
                                .collect(Collectors.toSet());

//                        if (CollectionUtils.isNotEmpty(sameSubjectGroups)) {
//                            csData.failed("学生已经在一个" + walkingGroup.getSubject().getValue() + "学科教学班");
//                            result.failed(csData);
//                            continue;
//                        }
                        // 把相同学科的教学班组解除
                        breakGroupKlxRefs = groupKlxRefs.stream()
                                .filter(ref -> sameSubjectGroups.contains(ref.getGroupId()))
                                .collect(Collectors.toList());
                    }
                }

                if (CollectionUtils.isNotEmpty(breakGroupKlxRefs)) {
                    newKuailexueServiceClient.disableGroupKlxStudentRefs(breakGroupKlxRefs);
                }

                KlxStudent klxStudent = dictionary.queryKlxStudent(klxStudentId);
                Long studentId = klxStudent != null && klxStudent.isRealStudent() ? klxStudent.getA17id() : 0L;
                List<GroupKlxStudentRef> refs = groupIds.stream()
                        .map(groupId -> GroupKlxStudentRef.newInstance(groupId, klxStudentId, studentId))
                        .collect(Collectors.toList());
                newKuailexueServiceClient.persistGroupKlxStudentRefs(refs);

                // 写入到成功excel
                csData.success("学生ID:" + klxStudentId);
                result.success(csData);

                String operation = "groups[" + StringUtils.join(groupIds, ",") + "],学生[" + klxStudentId + "]";
                logUserServiceRecord(null, operation, UserServiceRecordOperationType.班组信息变更, "导入学生到分组");

            }

        }
        return result;
    }

    private ClassGroupDictionary buildChangeClassDictionary(School school, List<ClazzLevel> clazzLevelList) {
        EduSystemType eduSystem = EduSystemType.of(
                schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly()
        );

        // 班级基础数据
        List<Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .enabled().toList().stream().filter(Objects::nonNull)
                .filter(p -> eduSystem == null || eduSystem.getCandidateClazzLevel().contains(p.getClassLevel()))
                .filter(p -> clazzLevelList.contains(p.getClazzLevel()))
                .collect(Collectors.toList());

        // 找到班级下所有分组
        Set<Long> clazzIds = clazzList.stream().map(Clazz::getId).collect(Collectors.toSet());
        Map<Long, List<Group>> groupMap = groupServiceClient.getGroupService()
                .findGroupsByClazzIds(clazzIds)
                .getUninterruptibly();

        // 找到所有分组对应的学生
        List<Long> groupIdList = groupMap.values().stream()
                .flatMap(Collection::stream)
                .map(Group::getId)
                .collect(Collectors.toList());
        Map<Long, List<KlxStudent>> klxGroupStudents = newKuailexueLoaderClient.loadKlxGroupStudents(groupIdList);

        return ClassGroupDictionary.newInstance()
                .initClazzList(clazzList)
                .initGroups(groupMap)
                .initStudents(klxGroupStudents)
                .buildParam();
    }

    //==========================================================================================================
    //===============================        复  制  教  学  班  相  关         =================================
    //==========================================================================================================
    private MapMessage parseLinkClassData(Sheet sheet) {
        List<LinkClassImportData> data = new LinkedList<>();
        // 从第一行开始
        for (int index = 1; index <= sheet.getLastRowNum(); index++) {
            Row row = sheet.getRow(index);
            if (row == null) {
                continue;
            }
            String gradeName = WorkbookUtils.getCellValue(row.getCell(0));
            ClazzLevel clazzLevel = SpecialTeacherConstants.parseGradeOfChinese(gradeName);
            String grade = ConversionUtils.toString(clazzLevel == null ? "" : clazzLevel.getLevel());

            String studentName = WorkbookUtils.getCellValueTrim(row.getCell(1));
            String targetClazz = WorkbookUtils.getCellValueTrim(row.getCell(2));
//            String mobile = WorkbookUtils.getCellValue(row.getCell(3));

            // 读到了一个空行，忽略吧
            if (StringUtils.isBlank(grade) && StringUtils.isBlank(studentName) && StringUtils.isBlank(targetClazz)) {
                continue;
            }

            if (StringUtils.isEmpty(grade)) {
                return MapMessage.errorMessage("年级填写错误，请检查文件");
            }

            if (StringUtils.isAnyBlank(grade, studentName, targetClazz)) {
                return MapMessage.errorMessage("必填项存在空值，请检查文件");
            }

            LinkClassImportData result = new LinkClassImportData(grade, studentName, targetClazz);

            data.add(result);
        }
        return MapMessage.successMessage().add("data", data);
    }

    private LinkClassImportResult executeLinkClazzData(Long schoolId, List<LinkClassImportData> importData) {
        LinkClassImportResult result = new LinkClassImportResult();
        ClassGroupDictionary dictionary = buildLinkClassDictionary(schoolId, importData);

        //    C. 学生, 年级下目标学生存在并且唯一
        // key:clazzLevel_studentName value:changeStudentImportDataList
        Map<String, List<LinkClassImportData>> importDataMappers = importData.stream()
                .collect(Collectors.groupingBy(LinkClassImportData::classStudentKey, Collectors.toList()));

        for (Map.Entry<String, List<LinkClassImportData>> entry : importDataMappers.entrySet()) {
            String studentKey = entry.getKey();
            KlxStudentClazz klxStudentClazz = dictionary.queryKlxStudentId(studentKey);

            for (LinkClassImportData csData : entry.getValue()) {
                String classKey = csData.classKey();
                csData.setGrade(ClazzLevel.parse(ConversionUtils.toInt(csData.getGrade())).getDescription());
                Clazz clazz = dictionary.queryClazzByKey(classKey);
                if (clazz == null || !clazz.isWalkingClazz()) {
                    csData.failed("班级不存在，或者不是教学班");
                    result.failed(csData);
                    continue;
                }

                Set<Long> groupIds = dictionary.queryClazzGroupIds(classKey);
                if (CollectionUtils.isEmpty(groupIds)) {
                    csData.failed("班级中暂无班群，请先创建班群");
                    result.failed(csData);
                    continue;
                }

                if (klxStudentClazz == null) {
                    csData.failed("在该年级行政班中未找到该姓名学生: " + csData.getStudentName());
                    result.failed(csData);
                    continue;
                }
                String klxStudentId = klxStudentClazz.getKlxId();

                if (dictionary.containsDuplicateKlxStudents(studentKey)) {
                    // 有重名学生且ID不一样
                    Set<KlxStudentClazz> klxStudent = dictionary.queryDuplicateKlxStudents(studentKey);
                    klxStudent.add(dictionary.queryKlxStudentId(studentKey));
                    // 写入到失败excel
                    Map<String, List<KlxStudentClazz>> klxStudentMap = klxStudent.stream().collect(Collectors.groupingBy(k -> k.getClazz().getClassName()));
                    StringBuilder builder = new StringBuilder();
                    for (Map.Entry<String, List<KlxStudentClazz>> klx : klxStudentMap.entrySet()) {
                        builder.append(klx.getKey()).append(",").append("ID为：").append(StringUtils.join(klx.getValue().stream().map(KlxStudentClazz::getKlxId).collect(Collectors.toList()), "、")).append("\r\n");
                    }
                    csData.failed("在该年级行政班中找到多个该姓名学生，请在班级管理中手动处理（" + builder.toString() + "）");
                    result.failed(csData);
                    continue;
                }

                KlxStudent student = dictionary.queryKlxStudent(klxStudentId);
                Long a17Id = student != null && student.isRealStudent() ? student.getA17id() : 0L;
                List<GroupKlxStudentRef> refs = groupIds.stream()
                        .map(groupId -> GroupKlxStudentRef.newInstance(groupId, klxStudentId, a17Id))
                        .collect(Collectors.toList());
                newKuailexueServiceClient.persistGroupKlxStudentRefs(refs);
                // 写入到成功excel
                csData.success("学生ID:" + klxStudentId);
                result.success(csData);

                String operation = "groups[" + StringUtils.join(groupIds, ",") + "],学生[" + klxStudentId + "]";
                logUserServiceRecord(null, operation, UserServiceRecordOperationType.班组信息变更, "导入学生到分组");

            }

        }
        return result;
    }

    private ClassGroupDictionary buildLinkClassDictionary(Long schoolId, List<LinkClassImportData> importData) {
        // 用于班级列表的过滤
        List<String> clazzLevelList = importData.stream().map(LinkClassImportData::getGrade)
                .distinct().collect(Collectors.toList());

        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        EduSystemType eduSystem = EduSystemType.of(
                schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly()
        );
        // 班级基础数据
        List<Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .enabled().toList().stream().filter(Objects::nonNull)
                .filter(p -> eduSystem == null || eduSystem.getCandidateClazzLevel().contains(p.getClassLevel()))
                .filter(p -> clazzLevelList.contains(p.getClassLevel()))
                .filter(p -> ClazzCreatorType.SYSTEM.equals(p.getCreateBy()))
                .collect(Collectors.toList());

        // 找到班级下所有分组
        Set<Long> clazzIds = clazzList.stream().map(Clazz::getId).collect(Collectors.toSet());
        Map<Long, List<Group>> groupMap = groupServiceClient.getGroupService()
                .findGroupsByClazzIds(clazzIds)
                .getUninterruptibly();

        // 找到所有分组对应的学生
        List<Long> groupIdList = groupMap.values().stream()
                .flatMap(Collection::stream)
                .map(Group::getId)
                .collect(Collectors.toList());
        Map<Long, List<KlxStudent>> klxGroupStudents = newKuailexueLoaderClient.loadKlxGroupStudents(groupIdList);

        return ClassGroupDictionary.newInstance()
                .initClazzList(clazzList)
                .initGroups(groupMap)
                .initStudents(klxGroupStudents)
                .buildParam();
    }

}
