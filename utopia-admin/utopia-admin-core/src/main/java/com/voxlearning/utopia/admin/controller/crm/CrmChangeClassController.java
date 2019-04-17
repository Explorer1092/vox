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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.admin.data.*;
import com.voxlearning.utopia.admin.service.crm.CrmChangeClassService;
import com.voxlearning.utopia.admin.support.WorkbookUtils;
import com.voxlearning.utopia.admin.util.HssfUtils;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.clazz.client.GroupServiceClient;
import com.voxlearning.utopia.service.user.api.constants.ClazzCreatorType;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.TeacherStudentMapper;
import com.voxlearning.utopia.service.user.api.mappers.kuailexue.KuailexueUserMapper;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author:XiaochaoWei
 * @Description:
 * @CreateTime: 2017/5/11
 * @updateTIme:2018/07/13
 */
@Controller
@RequestMapping("/crm/school")
public class CrmChangeClassController extends CrmAbstractController {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private CrmChangeClassService crmChangeClassService;
    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private GroupServiceClient groupServiceClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private RaikouSDK raikouSDK;

    /**
     * 打散换班-检查导入数据(小学)
     */
    @RequestMapping(value = "changeclass.vpage", method = RequestMethod.POST)
    public void changeClass(HttpServletResponse response) {
        MapMessage result = checkAndChange(true);
        try {
            if (!result.isSuccess()) {
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write(result.getInfo());
            }
        } catch (Exception e) {
            logger.error("回写页面异常:", e);
        }
    }

    @SuppressWarnings("unchecked")
    private MapMessage checkAndChange(boolean type) { // type=false表明只校验不换班
        Long schoolId = getRequestLong("schoolId");
        String excelName = getRequestString("excelName");

        // 1.读取Excel文件内容xhjx
        MapMessage result = loadExcelFileData("excelFile");
        if (!result.isSuccess()) {
            return result;
        }
        List<ChangeClassModelMapper> changeClassModelList = (List<ChangeClassModelMapper>) result.get("data");
        List<ChangeClassModelMapper> collect = changeClassModelList.stream().filter(p -> p.isEmpty()).collect(Collectors.toList());
        if (collect.size() > 0) {
            return MapMessage.errorMessage("必填项存在空值，请检查文件");
        }
        // 2. 根据文件中的年级信息加载数据库数据
        // A.年级下的所有Class,

        // 用于班级列表的过滤
        List<String> clazzLevelList = changeClassModelList.stream().map(ChangeClassModelMapper::getClassLevel)
                .distinct().collect(Collectors.toList());
        // 班级基础数据
        List<Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .enabled()
                .toList()
                .stream()
                .filter(Objects::nonNull)
                .filter(p -> clazzLevelList.contains(p.getClassLevel()))
                .filter(p -> ClazzCreatorType.SYSTEM.equals(p.getCreateBy()))
                .collect(Collectors.toList());
        // 班级 ID-Clazz 字典
        Map<Long, Clazz> clazzIdData = clazzList.stream().collect(Collectors.toMap(Clazz::getId, Function.identity()));
        // clazzLevel_clazzName, clazzId 字典， 用于匹配找不到的班级 以及 换班操作时取到班级ID
        Map<String, Long> clazzNameData = new HashMap<>();
        for (Clazz p : clazzList) {
            String name = StringUtils.join(p.getClassLevel(), "_", p.getClassName());
            if (clazzNameData.containsKey(name)) {
                return MapMessage.errorMessage("学校存在同名班级 : {} ， 请检查数据", p.formalizeClazzName());
            }
            clazzNameData.put(name, p.getId());
        }

        // B.年级下所有Group,
        Map<Long, List<TeacherStudentMapper>> groupInfos = userAggregationLoaderClient.loadClazzTeacherStudents(clazzIdData.keySet());
        // 老师的组, key: clazzLevel_clazzName_teacherId, value: groupId
        Map<String, Long> teacherGroups = new HashMap<>();
        // 老师的组下学生, key: clazzLevel_clazzName_teacherId, value: studentIdList
//        Map<String, List<Long>> teacherGroupStudents = new HashMap<>();
//        Map<String, List<String>> teacherGroupVirStudents = new HashMap<>();
        // 学生信息, key: clazzLevel_studentName, value:student Id
        Map<String, Set<Long>> dupStudentInfo = new HashMap<>();  // 年级内有重名的学生
        Map<String, Long> studentInfo = new HashMap<>();
        // 学生的组，key: clazzLevel_studentName_groupId, value: groupId
//        Map<String, Long> studentGroup = new HashMap<>();

        // 有17ID的数据
        for (Long clazzId : groupInfos.keySet()) {
            Clazz clazz = clazzIdData.get(clazzId);
            List<TeacherStudentMapper> tsmList = groupInfos.get(clazzId);
            String keyPrefix = StringUtils.join(clazz.getClassLevel(), "_", clazz.getClassName(), "_");

            for (TeacherStudentMapper tsm : tsmList) {
                TeacherStudentMapper.TeacherInfoMapper tim = MiscUtils.firstElement(tsm.getTeachers());
                if (tim == null) {
                    continue;
                }
                teacherGroups.put(StringUtils.join(keyPrefix, tim.getId()), tsm.getGroupId());
                // 用来判断目标分组是否已包含该学生 begin
//                teacherGroupStudents.put(StringUtils.join(keyPrefix, tim.getId()), tsm.getStudents().stream().map(TeacherStudentMapper.StudentInfoMapper::getId).collect(Collectors.toList()));
//                List<String> virStudentIds = asyncGroupServiceClient.getAsyncGroupService().findGroupKlxStudentRefsByGroup(tsm.getGroupId()).take().stream().map(GroupKlxStudentRef::getKlxStudentId).collect(Collectors.toList());
//                teacherGroupVirStudents.put(StringUtils.join(keyPrefix, tim.getId()), virStudentIds);
//                // 用来判断目标分组是否已包含该学生 end
                List<TeacherStudentMapper.StudentInfoMapper> simList = tsm.getStudents();
                if (CollectionUtils.isNotEmpty(simList)) {
                    for (TeacherStudentMapper.StudentInfoMapper sim : simList) {
                        String stuKey = StringUtils.join(clazz.getClassLevel(), "_", sim.getName());
                        if (!studentInfo.containsKey(stuKey)) {
                            studentInfo.put(stuKey, sim.getId());
                        } else {
                            Long stuId = studentInfo.get(stuKey);
                            if (!Objects.equals(stuId, sim.getId())) {
                                Set<Long> dupStuIds = dupStudentInfo.get(stuKey);
                                if (dupStuIds == null) {
                                    dupStuIds = new HashSet<>();
                                }
                                dupStuIds.add(sim.getId());
                                dupStudentInfo.put(stuKey, dupStuIds);
                            }
                        }
                    }
                }
            }

        }
        // 无17ID的数据
        // 学生信息, key: clazzLevel_studentName, value:student Id
        Map<String, Set<String>> dupVirStudentInfo = new HashMap<>();  // 年级内有重名的学生
        Map<String, String> virStudentInfo = new HashMap<>();
        List<Long> groupIds = groupInfos.values().stream().flatMap(Collection::stream).map(TeacherStudentMapper::getGroupId).collect(Collectors.toList());
//        Map<Long, List<KuailexueUserMapper>> map = (Map<Long, List<KuailexueUserMapper>>) kuailexueLoaderClient.loadGroupStudents(groupIds).get("data");
        // Map<Long, List<KuailexueUserMapper>> map = newKuailexueLoaderClient.loadKlxGroupStudentsMapper(groupIds);
        Map<Long, List<KuailexueUserMapper>> map = new HashMap<>();
        for (Long gid : groupIds) {
            List<KuailexueUserMapper> klxStudents = newKuailexueLoaderClient.loadKlxGroupStudentsMapper(gid);
            map.put(gid, klxStudents);
        }

        List<String> userNameList = map.values().stream().flatMap(Collection::stream)
                .map(KuailexueUserMapper::getKlxStudentId).collect(Collectors.toList());
        Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(userNameList);
        List<KlxStudent> tmp = new ArrayList<>(klxStudentMap.values());
        for (Long clazzId : groupInfos.keySet()) {
            Clazz clazz = clazzIdData.get(clazzId);
            if (CollectionUtils.isNotEmpty(tmp)) {
                for (KlxStudent virStu : tmp) {
                    String stuKey = StringUtils.join(clazz.getClassLevel(), "_", virStu.getName());
                    if (!virStudentInfo.containsKey(stuKey)) {
                        virStudentInfo.put(stuKey, virStu.getId());
                    } else {
                        String stuId = virStudentInfo.get(stuKey);
                        // 同名同ID无所谓
                        if (!Objects.equals(stuId, virStu.getId())) {
                            Set<String> dupStuIds = dupVirStudentInfo.get(stuKey);
                            if (dupStuIds == null) {
                                dupStuIds = new HashSet<>();
                            }
                            dupStuIds.add(virStu.getId());
                            dupVirStudentInfo.put(stuKey, dupStuIds);
                        }
                    }
                }
            }
        }

        // C.年级下的所有Student,

        // 3. 文件内数据检查
        //    A. 班级, 年级下目标班级存在,                    重大错误(整个文件不处理)
        //       班级, 班级的学制和年级不匹配(已经被上面的检查包含了)
        Map<String, List<ChangeClassModelMapper>> clazzNameList = changeClassModelList.stream()
                .collect(Collectors.groupingBy(ChangeClassModelMapper::classKey, Collectors.toList()));
        List<String> notFoundClazzList = clazzNameList.keySet().stream().filter(p -> !clazzNameData.containsKey(p)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notFoundClazzList)) {
            return MapMessage.errorMessage("班级{}不存在", JsonUtils.toJson(notFoundClazzList));
        }

        //    B. 老师, 老师ID存在,并且在班级下面有组,          重大错误(整个文件不处理)
        List<Long> teacherIdList = changeClassModelList.stream().map(ChangeClassModelMapper::getTeacherId).distinct().collect(Collectors.toList());
        Map<Long, TeacherDetail> teachers = teacherLoaderClient.loadTeacherDetails(teacherIdList);
        List<Long> notFoundTeacherIds = teacherIdList.stream().filter(p -> !teachers.containsKey(p)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notFoundTeacherIds)) {
            return MapMessage.errorMessage("老师{}不存在", JsonUtils.toJson(notFoundTeacherIds));
        }

        List<Long> diffSchoolTeachers = teachers.values().stream().filter(p -> !Objects.equals(p.getTeacherSchoolId(), schoolId)).map(TeacherDetail::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(diffSchoolTeachers)) {
            return MapMessage.errorMessage("老师{}不存在于选中的学校，或不止存在于选中的学校", JsonUtils.toJson(diffSchoolTeachers));
        }

        Map<String, List<ChangeClassModelMapper>> clazzTeacherList = changeClassModelList.stream()
                .collect(Collectors.groupingBy(ChangeClassModelMapper::classTeacherKey, Collectors.toList()));
        List<String> notFoundGroups = clazzTeacherList.keySet().stream().filter(p -> !teacherGroups.containsKey(p)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notFoundGroups)) {
            return MapMessage.errorMessage("老师{}在该班级下没有组", JsonUtils.toJson(notFoundGroups));
        }


        //    C. 学生, 年级下目标学生存在并且唯一,             一般错误(该条记录跳过)
        // 不用做检查, 处理的时候处理即可

        //    D. 学生班组关系丢失                          重大错误(整个文件不处理)      已和产品确认 继续换班逻辑

        // 换班处理
        List<ChangeClassModelMapper> successList = new ArrayList<>();
        List<ChangeClassModelMapper> failList = new ArrayList<>();

        // key:年级+学生姓名 value:changeClassModelMapperList
        Map<String, List<ChangeClassModelMapper>> changeClassModelMappers = changeClassModelList.stream()
                .collect(Collectors.groupingBy(ChangeClassModelMapper::classStudentKey, Collectors.toList()));

        List<TeacherStudentMapper.TeacherInfoMapper> teacherInfoMappers = groupInfos.values().stream().flatMap(Collection::stream).map(TeacherStudentMapper::getTeachers).flatMap(Collection::stream).collect(Collectors.toList());
        Map<Long, Subject> teacherSubject = teacherInfoMappers.stream().collect(Collectors.toMap(TeacherStudentMapper.TeacherInfoMapper::getId, TeacherStudentMapper.TeacherInfoMapper::getSubject, (u, v) -> u, LinkedHashMap::new));

        for (String key : changeClassModelMappers.keySet()) {
            List<String> list = changeClassModelMappers.get(key).stream().map(ChangeClassModelMapper::getAfterClass).distinct().collect(Collectors.toList());
            if (list.size() > 1) {
                return MapMessage.errorMessage("同一个学生 {} 不可以绑定到到两个班级下", key.split("_")[1]);
            }
            List<Long> tIds = changeClassModelMappers.get(key).stream().map(ChangeClassModelMapper::getTeacherId).distinct().collect(Collectors.toList());
            List<Subject> subjectList = new ArrayList<>();
            for (Long tId : tIds) {
                subjectList.add(teacherSubject.get(tId));
            }
            List<Subject> tmpSubjects = subjectList.stream().distinct().collect(Collectors.toList());
            if (subjectList.size() != tmpSubjects.size()) {
                return MapMessage.errorMessage("同一个班级下这些老师中 {} 所在分组存在相同的科目", JsonUtils.toJson(tIds));
            }
        }

        Map<Long, List<GroupStudentTuple>> studentGroupsMap = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByStudentIdsIncludeDisabled(studentInfo.values())
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .collect(Collectors.groupingBy(GroupStudentTuple::getStudentId));
        Set<Long> allGroupIds = studentGroupsMap.values().stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .filter(t -> t.getGroupId() != null)
                .map(GroupStudentTuple::getGroupId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, Group> allGroups = groupLoaderClient.getGroupLoader()
                .loadGroups(allGroupIds).getUninterruptibly();
        Map<Long, List<Long>> studentGroupIdsMap = new LinkedHashMap<>(); // key:studentId value:groupIds
        for (Long studentId : studentInfo.values()) {
            List<Long> list = new LinkedList<>();
            studentGroupsMap.getOrDefault(studentId, Collections.emptyList()).stream()
                    .filter(Objects::nonNull)
                    .filter(t -> t.getGroupId() != null)
                    .map(GroupStudentTuple::getGroupId)
                    .map(allGroups::get)
                    .filter(Objects::nonNull)
                    .map(Group::getId)
                    .filter(Objects::nonNull)
                    .forEach(list::add);
            studentGroupIdsMap.put(studentId, list);
        }

        for (String key : changeClassModelMappers.keySet()) {
            List<ChangeClassModelMapper> classModelMappers = changeClassModelMappers.get(key);
            if (CollectionUtils.isEmpty(classModelMappers)) {
                continue;
            }
            boolean flag = true;
            for (int i = 0; i < classModelMappers.size(); i++) {
                ChangeClassModelMapper cc = classModelMappers.get(i);
                if (dupStudentInfo.containsKey(key)) {
                    // 有重名学生且ID不一样
                    Set<Long> dupStudentIds = dupStudentInfo.get(key);
                    dupStudentIds.add(studentInfo.get(key));
                    // 写入到失败excel
                    cc.failed("年级内学生重复: " + JsonUtils.toJson(dupStudentIds));
                    failList.add(cc);
                    flag = false;
                }
                if (dupVirStudentInfo.containsKey(key)) {
                    // 有重名学生且ID不一样
                    Set<String> dupVirStudentIds = dupVirStudentInfo.get(key);
                    dupVirStudentIds.add(virStudentInfo.get(key));
                    // 写入到失败excel
                    cc.failed("年级内学生重复" + JsonUtils.toJson(dupVirStudentIds));
                    failList.add(cc);
                    flag = false;
                }
                if (!flag) {
                    classModelMappers.remove(i);
                }
            }
            Long studentId = null;
            String userName = null;
            if (!studentInfo.isEmpty()) {
                studentId = studentInfo.get(key);
                if (studentId != null && flag) {
                    List<Long> gIds = studentGroupIdsMap.get(studentId);
                    crmChangeClassService.removeStudentGroupRef(studentId, gIds);
                }
            }
            if (!virStudentInfo.isEmpty()) {
                userName = virStudentInfo.get(key);
                if (userName != null && flag) {
                    crmChangeClassService.removeKlxVirtualStudentGroupRef(userName);
                }
            }

            for (ChangeClassModelMapper cc : classModelMappers) {
                if (Objects.isNull(cc)) {
                    continue;
                }
                Long groupId = teacherGroups.get(cc.classTeacherKey());
                if (studentId == null && userName == null) {
                    cc.failed("未找到此学生: " + key);
                    failList.add(cc);
                    continue;
                }

                if (!studentInfo.isEmpty()) {
                    if (studentId != null && flag) {
                        String classKey = cc.classKey();
                        MapMessage mapMessage = crmChangeClassService.importStudent(cc.getTeacherId(), studentId, clazzNameData.get(classKey));
                        if (!mapMessage.isSuccess() && StringUtils.isNotBlank(mapMessage.getInfo())) {
                            // 写入到失败excel
                            cc.failed("studentId:" + studentId + "errorMsg:" + mapMessage.getInfo());
                            failList.add(cc);
                            continue;
                        }
                    }
                }
                if (!virStudentInfo.isEmpty()) {
                    if (userName != null && flag) {
                        KlxStudent student = klxStudentMap.get(userName);
                        Long a17id = 0L;
                        if (student.isRealStudent()) {
                            a17id = student.getA17id();
                        }
                        MapMessage mapMessage = crmChangeClassService.addKlxVirtualStudentGroupRef(groupId, userName, a17id);
                        if (!mapMessage.isSuccess()) {
                            // 写入到失败excel
                            cc.failed("studentId:" + userName + "errorMsg:" + mapMessage.getInfo());
                            failList.add(cc);
                            continue;
                        }
                    }
                }

                // 写入到成功excel
                String sid = studentId == null ? userName : ConversionUtils.toString(studentId);
                cc.success("学生ID:" + sid);
                successList.add(cc);
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                if (studentId != null) {
                    userServiceRecord.setUserId(studentId);
                }
                userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
                userServiceRecord.setOperationContent("CRM管理员导入学生到分组");
                userServiceRecord.setComments("group[" + groupId + "],学生[" + sid + "]");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }

        }

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        writeResult(hssfWorkbook, "成功数据", successList);
        writeResult(hssfWorkbook, "失败数据", failList);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        String filename = excelName + "-" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss") + "-打散换班处理结果.xls";

//        try {
//            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
//            hssfWorkbook.write(out);
//            out.flush();
//            HttpRequestContextUtils.currentRequestContext().downloadFile(filename, "application/vnd.ms-excel", out.toByteArray());
//        } catch (Exception ex) {
//            logger.error("Failed download change class result.", ex);
//        }

        try {
            hssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext()
                    .downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (Throwable t) {
            logger.error("年级打散换班下载失败", t);
        } finally {
            IOUtils.closeQuietly(outStream);
        }
        return MapMessage.successMessage();
    }

    private MapMessage loadExcelFileData(String excelFile) {
        if (StringUtils.isBlank(excelFile)) {
            return MapMessage.errorMessage("上传文件参数异常");
        }
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            return MapMessage.errorMessage();
        }

        try {
            MultipartHttpServletRequest request = (MultipartHttpServletRequest) getRequest();
            MultipartFile file = request.getFile(excelFile);
            if (file == null || file.isEmpty()) {
                return MapMessage.errorMessage("年级打散换班读取EXCEL数据文件失败，请确认文件内容格式是否正确");
            }

            String ext = StringUtils.substringAfterLast(file.getOriginalFilename(), ".").toLowerCase();
            if (!ext.equals("xls") && !ext.equals("xlsx")) {
                return MapMessage.errorMessage("无效的文件类型");
            }

            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            List<ChangeClassModelMapper> dataList = new ArrayList<>();
            int rowIndex = 1;
            if (sheet.getLastRowNum() > 2000) {
                return MapMessage.errorMessage("年级打散换班excel数据量超过阀值!");
            }
            while (true) {
                Row row = sheet.getRow(rowIndex++);
                if (row == null) {
                    break;
                }
                ChangeClassModelMapper mapper = new ChangeClassModelMapper();
                mapper.setClassLevel(getStringCellValue(row.getCell(0)));
                mapper.setOriginClass(getStringCellValue(row.getCell(1)));
                mapper.setOriginStudyNo(getStringCellValue(row.getCell(2)));
                mapper.setAfterStudyNo(getStringCellValue(row.getCell(3)));
                mapper.setStudentName(getStringCellValue(row.getCell(4)));
                mapper.setAfterClass(getStringCellValue(row.getCell(5)));
                mapper.setTeacherId(getLongCellValue(row.getCell(6)));
                if (mapper.isAllEmpty()) {
                    continue;
                }
                List<String> studentNames = dataList.stream().map(ChangeClassModelMapper::getStudentName).collect(Collectors.toList());
                if (studentNames.contains(mapper.getStudentName())) {
                    String msg = "excel中学生重复，姓名：" + mapper.getStudentName();
                    return MapMessage.errorMessage(msg);
                }
                dataList.add(mapper);
            }
            return MapMessage.successMessage().add("data", dataList);
        } catch (Exception ex) {
            logger.error("Failed read change class model data form excel, fileName={}", excelFile, ex);
            return MapMessage.errorMessage("读取数据异常：" + ex.getMessage());
        }
    }

    private void writeResult(HSSFWorkbook workbook, String name, List<ChangeClassModelMapper> resultList) {
        if (workbook == null) {
            return;
        }
        HSSFSheet hssfSheet = workbook.createSheet(name);
        hssfSheet.setColumnWidth(1, 8000);
        hssfSheet.setColumnWidth(2, 4000);
        hssfSheet.setColumnWidth(7, 3500);

        HSSFCellStyle borderStyle = workbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        HSSFRow firstRow = HssfUtils.createRow(hssfSheet, 0, 8, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "年级");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "换班前班级（非必填）");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "换班前学号（非必填）");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "换班后学号（非必填）");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "学生姓名");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "换班后班级");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "换班后老师ID");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "成功/失败（操作后回填）");
        HssfUtils.setCellValue(firstRow, 8, borderStyle, "备注（操作后回填）");

        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(resultList)) {
            for (ChangeClassModelMapper changeClassModel : resultList) {
                HSSFRow row = HssfUtils.createRow(hssfSheet, rowNum++, 8, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, changeClassModel.getClassLevel());
                HssfUtils.setCellValue(row, 1, borderStyle, changeClassModel.getOriginClass());
                HssfUtils.setCellValue(row, 2, borderStyle, changeClassModel.getOriginStudyNo());
                HssfUtils.setCellValue(row, 3, borderStyle, changeClassModel.getAfterStudyNo());
                HssfUtils.setCellValue(row, 4, borderStyle, changeClassModel.getStudentName());
                HssfUtils.setCellValue(row, 5, borderStyle, changeClassModel.getAfterClass());
                HssfUtils.setCellValue(row, 6, borderStyle, changeClassModel.getTeacherId());
                HssfUtils.setCellValue(row, 7, borderStyle, changeClassModel.getResult());
                HssfUtils.setCellValue(row, 8, borderStyle, changeClassModel.getRemark());
            }
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return StringUtils.deleteWhitespace(cell.getStringCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return String.valueOf(new BigDecimal(cell.getNumericCellValue()).longValue());
        }
        return null;
    }

    private Long getLongCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return new BigDecimal(cell.getNumericCellValue()).longValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return ConversionUtils.toLong(cell.getStringCellValue().trim());
        }
        return null;
    }

    /**
     * 打散换班-检查导入数据(中学)
     */
    @RequestMapping(value = "checkchangeclassdata.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage checkChangeClassData() {

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
     * 打散换班-执行导入数据(中学)
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "executechangeclass.vpage", method = RequestMethod.POST)
    public void executeChangeClass() {

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

    //==========================================================================================================
    //=================================       打  散  换  班  相  关 (中学)  ===================================
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
                .enabled()
                .toList()
                .stream()
                .filter(Objects::nonNull)
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

    //  ========================================= 其他方法 =========================================
    @RequestMapping(value = "gettemplate.vpage", method = RequestMethod.GET)
    public void getImportTemplate() {
        School school = currentSchool();
        if (school == null) {
            return;
        }
        String templateId = getRequestString("template");
        ExcelTemplate template = ExcelTemplate.safeParse(templateId);
        if (template == null) {
            return;
        }
        // 根据初高中重新调整一下
        if (template == ExcelTemplate.CREATE_TEACHER_CLASS_MIDDLE && school.isSeniorSchool()) {
            template = ExcelTemplate.CREATE_TEACHER_CLASS_SENIOR;
        }
        String fileName = template.getFileName();
        String filePath = template.getTemplatePath();
        try {
            Resource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                logger.error("download special teacher  template {} failed - template not exists", filePath);
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);

            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download special teacher template {} failed - ExMsg : {};", filePath, e);
        }
    }

    School currentSchool() {
        Long currentSchoolId = getRequestLong("schoolId");
        return schoolLoaderClient.getSchoolLoader()
                .loadSchool(currentSchoolId)
                .getUninterruptibly();
    }

}
