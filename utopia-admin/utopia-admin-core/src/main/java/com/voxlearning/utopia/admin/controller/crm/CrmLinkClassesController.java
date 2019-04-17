package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.data.LinkClassesModelMapper;
import com.voxlearning.utopia.admin.service.crm.CrmChangeClassService;
import com.voxlearning.utopia.admin.util.HssfUtils;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.GroupKlxStudentRef;
import com.voxlearning.utopia.service.user.api.entities.KlxStudent;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.TeacherStudentMapper;
import com.voxlearning.utopia.service.user.api.mappers.kuailexue.KuailexueUserMapper;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author:XiaochaoWei
 * @Description: 关联教学班学生
 * @CreateTime: 2017/5/25
 */
@Controller
@RequestMapping("/crm/school")
public class CrmLinkClassesController extends CrmAbstractController {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private CrmChangeClassService crmChangeClassService;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private RaikouSDK raikouSDK;

    @RequestMapping(value = "linkclasses.vpage", method = RequestMethod.POST)
    public void linkClasses(HttpServletResponse response) {
        MapMessage result = checkAndLink();
        try {
            if (!result.isSuccess()) {
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write(result.getInfo());
            }
        } catch (Exception e) {
            logger.error("回写页面异常:", e);
        }
    }


    // 处理流程

    @SuppressWarnings("unchecked")
    private MapMessage checkAndLink() {

        Long schoolId = getRequestLong("schoolId");
        String excelName = getRequestString("excelName");

        // 1.读取Excel文件内容
        MapMessage result = loadExcelFileData("excelFile");
        if (!result.isSuccess()) {
            return result;
        }
        List<LinkClassesModelMapper> linkClassesModelList = (List<LinkClassesModelMapper>) result.get("data");

        // 2. 根据文件中的年级信息加载数据库数据
        // A.年级下的所有Class,

        // 用于班级列表的过滤
        List<String> clazzLevelList = linkClassesModelList.stream().map(LinkClassesModelMapper::getClassLevel)
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
        Map<String, List<Long>> teacherGroupStudents = new HashMap<>();
        Map<String, List<String>> teacherGroupVirStudents = new HashMap<>();
        // 学生信息, key: clazzLevel_studentName, value:student Id
        Map<String, Set<Long>> dupStudentInfo = new HashMap<>();  // 年级内有重名的学生
        Map<String, Long> studentInfo = new HashMap<>();
        // 学生的组，key: clazzLevel_studentName_groupId, value: groupId
        Map<String, Long> studentGroup = new HashMap<>();

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
                teacherGroupStudents.put(StringUtils.join(keyPrefix, tim.getId()), tsm.getStudents().stream().map(TeacherStudentMapper.StudentInfoMapper::getId).collect(Collectors.toList()));
                List<String> virStudentIds = asyncGroupServiceClient.getAsyncGroupService().findGroupKlxStudentRefsByGroup(tsm.getGroupId()).take().stream().map(GroupKlxStudentRef::getKlxStudentId).collect(Collectors.toList());
                teacherGroupVirStudents.put(StringUtils.join(keyPrefix, tim.getId()), virStudentIds);
                // 用来判断目标分组是否已包含该学生 end
                List<TeacherStudentMapper.StudentInfoMapper> simList = tsm.getStudents();
                if (CollectionUtils.isNotEmpty(simList)) {
                    for (TeacherStudentMapper.StudentInfoMapper sim : simList) {
                        String stuKey = StringUtils.join(clazz.getClassLevel(), "_", sim.getName());
                        if (!studentInfo.containsKey(stuKey)) {
                            studentInfo.put(stuKey, sim.getId());
                            String tmpKey = StringUtils.join(stuKey, "_", tsm.getGroupId());
                            studentGroup.put(tmpKey, tsm.getGroupId());
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
        Map<Long, List<KuailexueUserMapper>> map = newKuailexueLoaderClient.loadKlxGroupStudentsMapper(groupIds);
        List<String> userNameList = map.values().stream().flatMap(Collection::stream).filter(u -> u.getUser_id() == null)
                .map(KuailexueUserMapper::getKlxStudentId).collect(Collectors.toList());
        Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(userNameList);
        List<KlxStudent> tmp = new LinkedList<>(klxStudentMap.values());
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
        Map<String, List<LinkClassesModelMapper>> clazzNameList = linkClassesModelList.stream()
                .collect(Collectors.groupingBy(LinkClassesModelMapper::classKey, Collectors.toList()));
        List<String> notFoundClazzList = clazzNameList.keySet().stream().filter(p -> !clazzNameData.containsKey(p)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notFoundClazzList)) {
            return MapMessage.errorMessage("班级{}不存在", JsonUtils.toJson(notFoundClazzList));
        }

        //    B. 老师, 老师ID存在,并且在班级下面有组,          重大错误(整个文件不处理)
        List<Long> teacherIdList = linkClassesModelList.stream().map(LinkClassesModelMapper::getTeacherId).distinct().collect(Collectors.toList());
        Map<Long, TeacherDetail> teachers = teacherLoaderClient.loadTeacherDetails(teacherIdList);
        List<Long> notFoundTeacherIds = teacherIdList.stream().filter(p -> !teachers.containsKey(p)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notFoundTeacherIds)) {
            return MapMessage.errorMessage("老师{}不存在", JsonUtils.toJson(notFoundTeacherIds));
        }

        List<Long> diffSchoolTeachers = teachers.values().stream().filter(p -> !Objects.equals(p.getTeacherSchoolId(), schoolId)).map(TeacherDetail::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(diffSchoolTeachers)) {
            return MapMessage.errorMessage("老师{}不存在于选中的学校，或不止存在于选中的学校", JsonUtils.toJson(diffSchoolTeachers));
        }

        Map<String, List<LinkClassesModelMapper>> clazzTeacherList = linkClassesModelList.stream()
                .collect(Collectors.groupingBy(LinkClassesModelMapper::classTeacherKey, Collectors.toList()));
        List<String> notFoundGroups = clazzTeacherList.keySet().stream().filter(p -> !teacherGroups.containsKey(p)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notFoundGroups)) {
            return MapMessage.errorMessage("老师{}在该班级下没有组", JsonUtils.toJson(notFoundGroups));
        }
        for (LinkClassesModelMapper cc : linkClassesModelList) {
            String studentIdKey = cc.classStudentKey();
            String studentIdsKey = cc.classTeacherKey();
            if (!studentInfo.isEmpty()) {
                if (teacherGroupStudents.get(studentIdsKey) != null && teacherGroupStudents.get(studentIdsKey).contains(studentInfo.get(studentIdKey))) {
                    return MapMessage.errorMessage("老师{}的分组下已存在该学生{}", cc.getTeacherId(), studentInfo.get(studentIdKey));
                }
            }
            if (!virStudentInfo.isEmpty()) {
                if (teacherGroupVirStudents.get(studentIdsKey) != null && teacherGroupVirStudents.get(studentIdsKey).contains(virStudentInfo.get(studentIdKey))) {
                    return MapMessage.errorMessage("老师{}的分组下已存在该学生{}", cc.getTeacherId(), virStudentInfo.get(studentIdKey));
                }
            }
        }


        //    C. 学生, 年级下目标学生存在并且唯一,             一般错误(该条记录跳过)
        // 不用做检查, 处理的时候处理即可

        //    D. 学生班组关系丢失                          重大错误(整个文件不处理)      已和产品确认 继续换班逻辑

        // 换班处理
        List<LinkClassesModelMapper> successList = new ArrayList<>();
        List<LinkClassesModelMapper> failList = new ArrayList<>();

        // key:年级+学生姓名 value:changeClassModelMapperList
        Map<String, List<LinkClassesModelMapper>> linkClassModelMappers = linkClassesModelList.stream()
                .collect(Collectors.groupingBy(LinkClassesModelMapper::classStudentKey, Collectors.toList()));

        for (String key : linkClassModelMappers.keySet()) {
            List<LinkClassesModelMapper> classModelMappers = linkClassModelMappers.get(key);
            Long studentId = studentInfo.get(key);
            String userName = virStudentInfo.get(key);
            for (LinkClassesModelMapper cc : classModelMappers) {
                if (cc.isEmpty()) {
                    cc.failed("必填项存在空值");
                    failList.add(cc);
                    continue;
                }

                if (studentId == null && userName == null) {
                    cc.failed("年级内无此学生: " + cc.getStudentName());
                    failList.add(cc);
                    continue;
                }
                Long groupId = teacherGroups.get(cc.classTeacherKey());
                if (!studentInfo.isEmpty()) {
                    if (dupStudentInfo.containsKey(key)) {
                        // 有重名学生且ID不一样
                        Set<Long> dupStudentIds = dupStudentInfo.get(key);
                        dupStudentIds.add(studentInfo.get(key));
                        // 写入到失败excel
                        cc.failed("年级内学生重复: " + JsonUtils.toJson(dupStudentIds));
                        failList.add(cc);
                        continue;
                    }
                    if (studentId != null) {
                        String classKey = cc.classKey();
                        MapMessage mapMessage = crmChangeClassService.linkTeacher(cc.getTeacherId(), studentId, clazzNameData.get(classKey));
                        if (!mapMessage.isSuccess()) {
                            // 写入到失败excel
                            cc.failed(mapMessage.getInfo());
                            failList.add(cc);
                            continue;
                        }
                    }
                }
                if (!virStudentInfo.isEmpty()) {
                    if (dupVirStudentInfo.containsKey(key)) {
                        // 有重名学生且ID不一样
                        Set<String> dupStudentIds = dupVirStudentInfo.get(key);
                        dupStudentIds.add(virStudentInfo.get(key));
                        // 写入到失败excel
                        cc.failed("年级内学生重复" + JsonUtils.toJson(dupStudentIds));
                        failList.add(cc);
                        continue;
                    }

                    KlxStudent student = klxStudentMap.get(userName);
                    Long a17id = 0L;
                    if (student.isRealStudent()) {
                        a17id = student.getA17id();
                    }
                    if (userName != null) {
                        MapMessage mapMessage = crmChangeClassService.addKlxVirtualStudentGroupRef(groupId, userName, a17id);
                        if (!mapMessage.isSuccess()) {
                            // 写入到失败excel
                            cc.failed(mapMessage.getInfo());
                            failList.add(cc);
                            continue;
                        }
                    }
                }

                // 写入到成功excel
                String sid = studentId == null ? userName : ConversionUtils.toString(studentId);
                cc.success("学生ID:" + studentId);
                successList.add(cc);
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                if (studentId != null) {
                    userServiceRecord.setUserId(studentId);
                }
                userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
                userServiceRecord.setOperationContent("CRM管理员关联学生到分组");
                userServiceRecord.setComments("group[" + groupId + "],学生[" + sid + "]");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }

        }

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        writeResult(hssfWorkbook, "成功数据", successList);
        writeResult(hssfWorkbook, "失败数据", failList);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        String filename = excelName + "-" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss") + "-关联走课学生处理结果.xls";
        try {
            hssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext()
                    .downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (IOException e) {
            logger.error("关联走课学生下载失败", e);
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
                return MapMessage.errorMessage("关联走课学生读取EXCEL数据文件失败，请确认文件内容格式是否正确");
            }

            String ext = StringUtils.substringAfterLast(file.getOriginalFilename(), ".").toLowerCase();
            if (!ext.equals("xls") && !ext.equals("xlsx")) {
                return MapMessage.errorMessage("文件格式不正确，请导入excel表格");
            }

            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            List<LinkClassesModelMapper> dataList = new ArrayList<>();
            int rowIndex = 1;
            if (sheet.getLastRowNum() > 2000) {
                return MapMessage.errorMessage("关联走课学生excel数据量超过阀值!");
            }
            while (true) {
                Row row = sheet.getRow(rowIndex++);
                if (row == null) {
                    break;
                }
                LinkClassesModelMapper mapper = new LinkClassesModelMapper();
                mapper.setClassLevel(getStringCellValue(row.getCell(0)));
                mapper.setCurrentClass(getStringCellValue(row.getCell(1)));
                mapper.setStudyNo(getStringCellValue(row.getCell(2)));
                mapper.setStudentName(getStringCellValue(row.getCell(3)));
                mapper.setLinkedClass(getStringCellValue(row.getCell(4)));
                mapper.setTeacherId(getLongCellValue(row.getCell(5)));
                if (mapper.isAllEmpty()) {
                    continue;
                }
                dataList.add(mapper);
            }
            return MapMessage.successMessage().add("data", dataList);
        } catch (Exception ex) {
            logger.error("Failed read change class model data form excel, fileName={}", excelFile, ex);
            return MapMessage.errorMessage("读取数据异常：" + ex.getMessage());
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

    private void writeResult(HSSFWorkbook workbook, String name, List<LinkClassesModelMapper> resultList) {
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
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "当前班级（非必填）");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "校内学号（非必填）");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "学生姓名");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "关联的新班级");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "新班级老师ID");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "成功/失败（操作后回填）");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "备注（操作后回填）");

        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(resultList)) {
            for (LinkClassesModelMapper linkClasses : resultList) {
                HSSFRow row = HssfUtils.createRow(hssfSheet, rowNum++, 8, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, linkClasses.getClassLevel());
                HssfUtils.setCellValue(row, 1, borderStyle, linkClasses.getCurrentClass());
                HssfUtils.setCellValue(row, 2, borderStyle, linkClasses.getStudyNo());
                HssfUtils.setCellValue(row, 3, borderStyle, linkClasses.getStudentName());
                HssfUtils.setCellValue(row, 4, borderStyle, linkClasses.getLinkedClass());
                HssfUtils.setCellValue(row, 5, borderStyle, linkClasses.getTeacherId());
                HssfUtils.setCellValue(row, 6, borderStyle, linkClasses.getResult());
                HssfUtils.setCellValue(row, 7, borderStyle, linkClasses.getRemark());
            }
        }
    }

}
