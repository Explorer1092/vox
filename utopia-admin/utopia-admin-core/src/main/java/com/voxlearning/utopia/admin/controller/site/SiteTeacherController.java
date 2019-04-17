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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.*;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.service.site.SiteTeacherService;
import com.voxlearning.utopia.api.constant.ClazzConstants;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.data.NeonatalClazz;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.constants.LatestType;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.latest.AbstractLatest;
import com.voxlearning.utopia.service.user.api.entities.latest.Latest_NewRegisterTeacher;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.consumer.ThirdPartyLoaderClient;
import lombok.Data;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Shuai Huan on 2014/10/30.
 */
@Controller
@RequestMapping("/site/teacher")
public class SiteTeacherController extends SiteAbstractController {

    private static final int ITEM_DOMAIN_MIN_LEN = 8;
    private static final int LANDING_ITEM_DOMAIN_LEN = 11;

    @Inject private RaikouSDK raikouSDK;

    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Inject
    private ThirdPartyLoaderClient thirdPartyLoaderClient;

    @Inject
    private SiteTeacherService siteTeacherService;

    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;

    @RequestMapping(value = "batchcreateteacherhomepage.vpage", method = RequestMethod.GET)
    String batchCreateTeacherHomepage() {
        return "site/batch/batchcreateteacherhomepage";
    }

    @RequestMapping(value = "batchcreateteacher.vpage", method = RequestMethod.POST)
    public String batchCreateTeacher(@RequestParam String content, Model model) {
        if (StringUtils.isEmpty(content)) {
            getAlertMessageManager().addMessageError("内容不能为空");
        }

        String[] messages = content.split("\\n");
        List<Map<String, Object>> lstSuccess = new ArrayList<>();
        List<String> lstFailed = new ArrayList<>();
        String password = "qsjklx";
        List<RegisterTeacherClazzStudent> list = new ArrayList<>();
        Map<Long, List<AbstractLatest>> school_latests = new HashMap<>();
        for (String m : messages) {
            String[] info = m.split("\\t");
            if (info.length < 4) {
                lstFailed.add(m);
                continue;
            }

            String realName = StringUtils.deleteWhitespace(info[0]);
            String mobile = StringUtils.deleteWhitespace(info[1]);
            String schoolId = StringUtils.deleteWhitespace(info[2]);
            String subject = StringUtils.deleteWhitespace(info[3]);

            if (StringUtils.isEmpty(realName) || StringUtils.isEmpty(mobile) ||
                    StringUtils.isEmpty(schoolId) || StringUtils.isEmpty(subject)) {
                lstFailed.add(m);
                continue;
            }
            if (!MobileRule.isMobile(mobile)) {
                lstFailed.add(m);
                continue;
            }

            Long sid = Long.parseLong(schoolId);
            RegisterTeacherClazzStudent registerTeacherClazzStudent = new RegisterTeacherClazzStudent();
            registerTeacherClazzStudent.setTeacherName(realName);
            registerTeacherClazzStudent.setMobile(mobile);
            registerTeacherClazzStudent.setSchoolId(Long.parseLong(schoolId));
            registerTeacherClazzStudent.setTeacherSubject(subject);
            String clazzId = null;
            if (info.length == 5) {
                clazzId = StringUtils.deleteWhitespace(info[4]);
                registerTeacherClazzStudent.setClazzid(Long.parseLong(clazzId));
            }

            try {
                UserAuthentication ua = userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER);
                User authenticatedUser = ua == null ? null : userLoaderClient.loadUser(ua.getId());
                if (authenticatedUser != null) {
                    if (clazzId == null) {
                        lstFailed.add(m);
                        continue;
                    }
                    registerTeacherClazzStudent.setTeacherId(authenticatedUser.getId());
                    registerTeacherClazzStudent.setPassword("");
                    list.add(registerTeacherClazzStudent);
                } else {
                    MapMessage mapMessage = createTeacher(mobile, password, realName);
                    if (mapMessage.isSuccess()) {
                        User user = (User) mapMessage.get("user");
                        mapMessage = userServiceClient.activateUserMobile(user.getId(), mobile, true, getCurrentAdminUser().getAdminUserName(), "批量生成");
                        if (!mapMessage.isSuccess()) {
                            lstFailed.add(m);
                            continue;
                        }
                        Subject s = Subject.valueOf(subject);
                        // FIXME hardcode primary school here
                        mapMessage = teacherServiceClient.setTeacherSubjectSchool(user, s, Ktwelve.PRIMARY_SCHOOL, Long.parseLong(schoolId));
                        if (!mapMessage.isSuccess()) {
                            lstFailed.add(m);
                            continue;
                        }

                        // 成功注册教师，激活手机，选择学校学科，发送动态
                        Latest_NewRegisterTeacher latest = new Latest_NewRegisterTeacher();
                        latest.setUserId(user.getId());
                        latest.setUserName(user.fetchRealname());
                        latest.setUserImg(user.fetchImageUrl());
                        latest.setUserSubject(s.getValue());
                        if (school_latests.containsKey(sid)) {
                            school_latests.get(sid).add(latest);
                        } else {
                            List<AbstractLatest> temp = new ArrayList<>();
                            temp.add(latest);
                            school_latests.put(sid, temp);
                        }

                        registerTeacherClazzStudent.setTeacherId(user.getId());
                        registerTeacherClazzStudent.setPassword("");
                        list.add(registerTeacherClazzStudent);
                        if (info.length < 5) {
                            Map<String, Object> result = new HashMap<>();
                            result.put("ID", user.getId());
                            result.put("PASSWORD", "");
                            result.put("NAME", user.getProfile().getRealname());
                            result.put("PHONE", mobile);
                            lstSuccess.add(result);
                        }
                    } else {
                        lstFailed.add(m);
                    }
                }
            } catch (Exception ex) {
                lstFailed.add(m);
            }
            // admin log
            addAdminLog("message-管理员" + getCurrentAdminUser().getAdminUserName() + "批量生成老师",
                    "", null, "姓名:" + realName + ", 手机:" + mobile + ", schoolId:" + schoolId + ", 学科:" + subject);
        }

        for (Long each : school_latests.keySet()) {
            List<AbstractLatest> details = school_latests.get(each);
            if (CollectionUtils.isNotEmpty(details)) {
                userServiceClient.createSchoolLatest(each, LatestType.NEW_REGISTER_TEACHER)
                        .withDetails(details).send();
            }
        }

        for (RegisterTeacherClazzStudent registerTeacherClazzStudent : list) {
            if (registerTeacherClazzStudent.getClazzid() == null) {
                continue;
            }
            MapMessage mapMessage = joinClazz(
                    registerTeacherClazzStudent.getClazzid(),
                    registerTeacherClazzStudent.getSchoolId(),
                    registerTeacherClazzStudent.getTeacherSubject(),
                    registerTeacherClazzStudent.getTeacherId()
            );
            if (!mapMessage.isSuccess()) {
                lstFailed.add(mapMessage.getInfo());
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("ID", registerTeacherClazzStudent.getTeacherId());
                result.put("PASSWORD", registerTeacherClazzStudent.getPassword());
                result.put("NAME", registerTeacherClazzStudent.getTeacherName());
                result.put("PHONE", registerTeacherClazzStudent.getMobile());
                lstSuccess.add(result);
            }
        }
        model.addAttribute("successlist", lstSuccess);
        model.addAttribute("failedlist", lstFailed);
        return "/site/batch/batchcreateteacherhomepage";
    }

    private MapMessage joinClazz(Long clazzId, Long schoolId, String s, Long userId) {
        //如果有这个字段，就把老师加入到这个班级中
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage().setInfo("班级" + clazzId + "不存在! ");
        }
        if (!(clazz.getSchoolId().equals(schoolId))) {
            return MapMessage.errorMessage().setInfo("老师与班级必须同校!");
        }
        clazzServiceClient.teacherJoinSystemClazz(userId, clazzId, OperationSourceType.crm);
        return MapMessage.successMessage();
    }

    /**
     * 批量注册老师班级学生 - 预览
     *
     * @param content
     * @return
     */
    @RequestMapping(value = "batchcreatetcspreview.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> previewForBatchCreateTeacherClazzStudent(String content) {
        Map<String, Object> resp = new HashMap<>();
        if (StringUtils.isEmpty(content)) {
            resp.put("errorMessage", "内容不能为空");
            return resp;
        }

        List<RegisterTeacherClazzStudent> srcList = new ArrayList<>();
        List<FailItem> failList = new ArrayList<>();
        String[] items = content.split("\\n");
        final int initLen = items[0].split("\\t").length;
        final boolean isLanding = initLen == LANDING_ITEM_DOMAIN_LEN;
        for (String e : items) {
            String[] item = e.split("\\t");
            final int len = item.length;
            if (len < ITEM_DOMAIN_MIN_LEN) {
                failList.add(buildFailItem("缺失必需的属性", item));
                continue;
            }
            if (len != ITEM_DOMAIN_MIN_LEN && len != LANDING_ITEM_DOMAIN_LEN) {
                failList.add(buildFailItem("属性数目不符合规则（不等于8或11）", item));
                continue;
            }
            String schoolId = StringUtils.deleteWhitespace(item[0]);
            String schoolName = StringUtils.deleteWhitespace(item[1]);
            String realName = StringUtils.deleteWhitespace(item[2]);
            String subject = StringUtils.deleteWhitespace(item[3]);
            String mobile = StringUtils.deleteWhitespace(item[4]);
            String clazzLevel = StringUtils.deleteWhitespace(item[5]);
            String clazzName = StringUtils.deleteWhitespace(item[6]);
            String studentName = StringUtils.deleteWhitespace(item[7]);
            if (StringUtils.isEmpty(schoolId) || StringUtils.isEmpty(realName) || StringUtils.isEmpty(subject) || StringUtils.isEmpty(mobile)
                    || StringUtils.isEmpty(clazzLevel) || StringUtils.isEmpty(clazzName) || StringUtils.isEmpty(studentName)) {
                failList.add(buildFailItem("包含有空的属性", item));
                continue;
            }
            if (!NumberUtils.isNumber(schoolId)) {
                failList.add(buildFailItem("学校ID格式错误", item));
                continue;
            }
            if (!MobileRule.isMobile(mobile)) {
                failList.add(buildFailItem("手机号码格式错误", item));
                continue;
            }
            RegisterTeacherClazzStudent entity = new RegisterTeacherClazzStudent();
            entity.setSchoolId(Long.parseLong(schoolId));
            entity.setSchoolName(schoolName);
            entity.setTeacherName(realName);
            entity.setTeacherSubject(subject);
            entity.setMobile(mobile);
            entity.setClazzLevel(clazzLevel);
            entity.setClazzName(clazzName);
            entity.setStudentName(studentName);
            if (len == ITEM_DOMAIN_MIN_LEN) {
                srcList.add(entity);
            } else {
                String sourceTid = StringUtils.deleteWhitespace(item[8]);
                String sourceSid = StringUtils.deleteWhitespace(item[9]);
                String sourceName = StringUtils.deleteWhitespace(item[10]);
                if (StringUtils.isEmpty(sourceTid) || StringUtils.isEmpty(sourceSid) || StringUtils.isEmpty(sourceName)) {
                    failList.add(buildFailItem("包含有空的属性", item));
                    continue;
                }
                entity.setSourceTid(sourceTid);
                entity.setSourceSid(sourceSid);
                entity.setSourceName(sourceName);
                srcList.add(entity);
            }
        }

        Map<Unique, List<RegisterTeacherClazzStudent>> uniqueMap = srcList.stream()
                .collect(Collectors.groupingBy(e -> {
                    List<FieldValuePair> pairList = new ArrayList<>();
                    pairList.add(new FieldValuePair("schoolId", e.getSchoolId()));
                    pairList.add(new FieldValuePair("teacherName", e.getTeacherName()));
                    pairList.add(new FieldValuePair("mobile", e.getMobile()));
                    return new Unique(pairList);
                }));
        for (Unique unique : uniqueMap.keySet()) {
            List<RegisterTeacherClazzStudent> teachers = uniqueMap.get(unique);
            RegisterTeacherClazzStudent teacher = MiscUtils.firstElement(teachers);
            try {
                // 验证老师班级是否超限
                Set<String> clsKey = new HashSet<>();
                for (RegisterTeacherClazzStudent e : teachers) {
                    clsKey.add(e.getClazzLevel() + "_" + e.getClazzName());
                }
                int clzCnt = clsKey.size();
                if (clzCnt >= ClazzConstants.MAX_CLAZZ_COUNT.get(Subject.of(teacher.getTeacherSubject()))) {
                    failList.add(buildFailItem("老师班级数量{" + clzCnt + "}已达上限{" + ClazzConstants.MAX_CLAZZ_COUNT + "}", teacher, isLanding));
                    continue;
                }
                // 验证学校是否存在
                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(teacher.getSchoolId())
                        .getUninterruptibly();
                if (school == null) {
                    failList.add(buildFailItem("学校不存在", teacher, isLanding));
                    continue;
                }
                // 验证手机号码是否已注册
                UserAuthentication ua = userLoaderClient.loadMobileAuthentication(teacher.getMobile(), UserType.TEACHER);
                if (ua != null) {
                    failList.add(buildFailItem("手机号码已注册", teacher, isLanding));
                    continue;
                }
                // 验证班级是否存在
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadSchoolClazzs(teacher.getSchoolId())
                        .enabled()
                        .clazzLevel(ClazzLevel.parse(SafeConverter.toInt(teacher.getClazzLevel())))
                        .toList()
                        .stream()
                        .filter(t -> StringUtils.equals(t.getClassName(), handleClazzName(teacher.getClazzName().trim())))
                        .findFirst()
                        .orElse(null);
                if (clazz == null) {
                    failList.add(buildFailItem("班级不存在，请新建班级", teacher, isLanding));
                    continue;
                }
                // 验证第三方绑定信息是否存在
                if (isLanding) {
                    LandingSource source = thirdPartyLoaderClient.loadLandingSource(teacher.getSourceName(), teacher.getSourceTid());
                    if (source != null) {
                        failList.add(buildFailItem("已绑定为第三方老师账号", teacher, isLanding));
                    }
                }
            } catch (Exception ex) {
                logger.error("previewForBatchCreateTeacherClazzStudent - Excp : {}", ex.getMessage());
                failList.add(buildFailItem("老师信息验证异常 : " + ex.getMessage(), teacher, isLanding));
            }
        }

        if (isLanding) {
            uniqueMap = srcList.stream()
                    .collect(Collectors.groupingBy(e -> {
                        List<FieldValuePair> pairList = new ArrayList<>();
                        pairList.add(new FieldValuePair("sourceName", e.getSourceName()));
                        pairList.add(new FieldValuePair("sourceSid", e.getSourceSid()));
                        return new Unique(pairList);
                    }));
            for (Unique unique : uniqueMap.keySet()) {
                List<RegisterTeacherClazzStudent> students = uniqueMap.get(unique);
                RegisterTeacherClazzStudent student = MiscUtils.firstElement(students);
                try {
                    // 验证是否包含重复的学生
                    int stuCnt = students.size();
                    if (stuCnt > 1) {
                        failList.add(buildFailItem("包含有学生ID、来源都相同的重复记录，共{" + stuCnt + "}条", student, isLanding));
                        continue;
                    }
                    // 验证第三方绑定信息是否存在
                    if (isLanding) {
                        LandingSource source = thirdPartyLoaderClient.loadLandingSource(student.getSourceName(), student.getSourceSid());
                        if (source != null) {
                            failList.add(buildFailItem("已绑定为第三方学生账号", student, isLanding));
                        }
                    }
                } catch (Exception ex) {
                    logger.error("previewForBatchCreateTeacherClazzStudent - Excp : {}", ex.getMessage());
                    failList.add(buildFailItem("学生信息验证异常 : " + ex.getMessage(), student, isLanding));
                }
            }
        }

        if (!failList.isEmpty()) {
            resp.put("errorMessage", "检测到有导致录入失败的记录");
            resp.put("failList", failList);
        } else {
            resp.put("errorMessage", "未检测到有导致录入失败的记录");
        }
        return resp;
    }

    /////////////////////////////////////System Clazz Related/////////////////////////////////

    @RequestMapping(value = "batchcreateschomepage.vpage", method = RequestMethod.GET)
    public String batchCreateSystemClazzHomepage() {
        return "site/batch/batchcreateschomepage";
    }

    @RequestMapping(value = "batchcreatesc.vpage", method = RequestMethod.POST)
    public String batchCreateSystemClazz(@RequestParam String content, Model model) {
        if (StringUtils.isEmpty(content)) {
            getAlertMessageManager().addMessageError("内容不能为空");
        }

        // 错误信息
        List<String> failedMessage = new LinkedList<>();
        List<String> successMessage = new LinkedList<>();
        List<ClassMapper> mappers = new LinkedList<>();

        String[] messages = content.split("\\n");
        for (String message : messages) {
            String[] info = message.split("\\t");
            if (info.length < 3) {
                failedMessage.add(message);
                continue;
            }

            String schoolIdStr = StringUtils.deleteWhitespace(info[0]);
            String clazzLevel = StringUtils.deleteWhitespace(info[1]);
            String clazzName = StringUtils.deleteWhitespace(info[2]);

            if (StringUtils.isEmpty(schoolIdStr)
                    || StringUtils.isEmpty(clazzLevel)
                    || StringUtils.isEmpty(clazzName)) {
                failedMessage.add(message);
                continue;
            }

            long schoolId = SafeConverter.toLong(schoolIdStr);
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(schoolId)
                    .getUninterruptibly();
            if (school == null) {
                failedMessage.add(message);
                continue;
            }

            ClassMapper mapper = new ClassMapper();
            mapper.setClassLevel(clazzLevel);
            mapper.setClazzName(clazzName);
            mapper.setSchoolId(schoolId);
            mapper.setEduSystem(schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly());
            mappers.add(mapper);
        }

        MapMessage result = clazzServiceClient.createSystemClazz(mappers);
        List<NeonatalClazz> neonatals = (List<NeonatalClazz>) result.get("neonatals");
        for (NeonatalClazz neonatal : neonatals) {
            if (neonatal.isSuccessful()) {
                successMessage.add(neonatal.getClazzName());
            } else {
                failedMessage.add("(" + neonatal.getExtensionAttributes().get("schoolId").toString()
                        + "-"
                        + neonatal.getExtensionAttributes().get("clazzLevel").toString()
                        + "-"
                        + neonatal.getExtensionAttributes().get("clazzName").toString()
                        + ")"
                        + neonatal.getErrorMessage());
            }
        }

        model.addAttribute("failedlist", failedMessage);
        model.addAttribute("successlist", successMessage);
        return "/site/batch/batchcreateschomepage";
    }

    @RequestMapping(value = "batchaddteachertoclazzpage.vpage", method = RequestMethod.GET)
    public String batchAddTeacherToClazzPage() {
        return "site/batch/batchaddteachertoclazzpage";
    }

    @RequestMapping(value = "batchaddteachertoclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchAddTeacherToClazz(@RequestParam String content) {
        if (StringUtils.isEmpty(content)) {
            return MapMessage.errorMessage("内容不能为空");
        }

        return siteTeacherService.batchAddTeacherToClazz(content);
    }

    /////////////////////////////////////Private Methods///////////////////////////////////

    private MapMessage createTeacher(String mobile, String password, String realName) {
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
        neonatalUser.setUserType(UserType.TEACHER);
        neonatalUser.setMobile(mobile);
        neonatalUser.setPassword(password);
        neonatalUser.setRealname(realName);
        neonatalUser.attachPasswordState(PasswordState.AUTO_GEN);
        neonatalUser.setWebSource(UserWebSource.crm_batch.getSource());
        return userServiceClient.registerUserAndSendMessage(neonatalUser);
    }

    private XSSFWorkbook convertToXSSF(List<Map<String, Object>> succeededList, List<String> failedList) {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();

        if (CollectionUtils.isNotEmpty(succeededList)) {
            XSSFSheet xssfSheet = xssfWorkbook.createSheet("成功列表");
            for (int i = 0; i < 10; i++) {
                xssfSheet.setColumnWidth(i, 400 * 15);
            }
            XSSFRow firstRow = xssfSheet.createRow(0);
            firstRow.setHeightInPoints(20);
            firstRow.createCell(0).setCellValue("老师ID");
            firstRow.createCell(1).setCellValue("老师姓名");
            firstRow.createCell(2).setCellValue("老师手机");
            firstRow.createCell(3).setCellValue("老师密码");
            firstRow.createCell(4).setCellValue("年级");
            firstRow.createCell(5).setCellValue("班级ID");
            firstRow.createCell(6).setCellValue("班级名称");
            firstRow.createCell(7).setCellValue("学生姓名");
            firstRow.createCell(8).setCellValue("学生ID");
            firstRow.createCell(9).setCellValue("学生密码");

            int rowNum = 1;
            for (Map<String, Object> data : succeededList) {
                XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
                xssfRow.setHeightInPoints(20);
                xssfRow.createCell(0).setCellValue((Long) data.get("ID"));
                xssfRow.createCell(1).setCellValue((String) data.get("TEACHER_NAME"));
                xssfRow.createCell(2).setCellValue((String) data.get("PHONE"));
                xssfRow.createCell(3).setCellValue((String) data.get("PASSWORD"));
                xssfRow.createCell(4).setCellValue((String) data.get("CLAZZ_LEVEL"));
                xssfRow.createCell(5).setCellValue((Long) data.get("CLAZZ_ID"));
                xssfRow.createCell(6).setCellValue((String) data.get("CLAZZ_NAME"));
                xssfRow.createCell(7).setCellValue((String) data.get("STUDENT_NAME"));
                xssfRow.createCell(8).setCellValue((Long) data.get("STUDENT_ID"));
                xssfRow.createCell(9).setCellValue((String) data.get("STUDENT_PWD"));
            }
        }

        if (CollectionUtils.isNotEmpty(failedList)) {
            XSSFSheet xssfFailedSheet = xssfWorkbook.createSheet("失败列表");
            xssfFailedSheet.setColumnWidth(1, 400 * 60);
            XSSFRow firstRow = xssfFailedSheet.createRow(0);
            firstRow.setHeightInPoints(20);
            firstRow.createCell(0).setCellValue("序号");
            firstRow.createCell(1).setCellValue("错误列表");
            int rowNum = 1;
            for (String failed : failedList) {
                XSSFRow xssfRow = xssfFailedSheet.createRow(rowNum);
                xssfRow.setHeightInPoints(20);
                xssfRow.createCell(0).setCellValue(rowNum);
                xssfRow.createCell(1).setCellValue(failed);
                rowNum++;
            }
        }

        return xssfWorkbook;
    }

    /**
     * 批量查询论坛老师 shippingaddress信息
     */
    @RequestMapping(value = "batchgetshippingaddress.vpage", method = RequestMethod.GET)
    public String batchGetShippingAddress() {
        return "site/batch/batchgetshippingaddress";
    }

    @RequestMapping(value = "batchgetshippingaddress.vpage", method = RequestMethod.POST)
    public String batchGetShippingAddress(@RequestParam String userNames, Model model) {
        if (StringUtils.isBlank(userNames)) {
            getAlertMessageManager().addMessageError("内容格式错误");
            return "site/batch/batchgetshippingaddress";
        }
        Map<String, Map<String, Object>> buffer = new HashMap<>();
        String[] names = userNames.split("\\r\\n");
        String param = StringUtils.join(names, ",");
        if (param.length() > 0) {
            String url = ProductConfig.getBbsSiteBaseUrl() + "/open.php?mod=platformId";
            String response = HttpRequestExecutor.defaultInstance().post(url).addParameter("usernames", param.replaceAll(" ", "")).execute().getResponseString();
            if (StringUtils.isNotBlank(response)) {
                Map<String, Object> userIds = JsonUtils.fromJson(response);
                if (MapUtils.isNotEmpty(userIds)) {
                    for (Map.Entry<String, Object> entry : userIds.entrySet()) {
                        User user = userLoaderClient.loadUser(ConversionUtils.toLong(entry.getValue()));
                        if (user == null) {
                            continue;
                        }
                        Map<String, Object> map = new HashMap<>();
                        UserShippingAddress address = userLoaderClient.loadUserShippingAddress(user.getId());
                        String bbsName = entry.getKey();
                        map.put("bbsName", bbsName);
                        map.put("address", address.getDetailAddress());
                        map.put("logisticType", address.getLogisticType());
                        map.put("phone", address.getSensitivePhone());
                        map.put("userName", user.fetchRealname());
                        map.put("userId", user.getId());
                        map.put("pname", address.getProvinceName());
                        map.put("cname", address.getCityName());
                        map.put("aname", address.getCountyName());
                        buffer.put(bbsName, map);
                    }
                }
            }
        }
        Map<String, Map<String, Object>> dataMap = new LinkedHashMap<>();
        for (String name : names) {
            String iName = name.replaceAll(" ", "");
            Map<String, Object> data = buffer.get(iName);
            dataMap.put(iName, data);
        }
        model.addAttribute("dataMap", dataMap);
        return "site/batch/batchgetshippingaddress";
    }

    private static FailItem buildFailItem(String message, String[] item) {
        StringBuilder builder = new StringBuilder();
        if (item != null) {
            for (String e : item) {
                builder.append("{").append(e).append("}  ");
            }
        }
        FailItem failItem = new FailItem();
        failItem.setMessage(message);
        failItem.setItem(builder.toString());
        return failItem;
    }

    private static FailItem buildFailItem(String message, RegisterTeacherClazzStudent item, boolean isLanding) {
        StringBuilder builder = new StringBuilder();
        if (item != null) {
            builder.append("{").append(item.getSchoolId()).append("}  ");
            builder.append("{").append(item.getSchoolName()).append("}  ");
            builder.append("{").append(item.getTeacherName()).append("}  ");
            builder.append("{").append(item.getTeacherSubject()).append("}  ");
            builder.append("{").append(item.getMobile()).append("}  ");
            builder.append("{").append(item.getClazzLevel()).append("}  ");
            builder.append("{").append(item.getClazzName()).append("}  ");
            builder.append("{").append(item.getStudentName()).append("}  ");
            if (isLanding) {
                builder.append("{").append(item.getSourceTid()).append("}  ");
                builder.append("{").append(item.getSourceSid()).append("}  ");
                builder.append("{").append(item.getSourceName()).append("}  ");
            }
        }
        FailItem failItem = new FailItem();
        failItem.setMessage(message);
        failItem.setItem(builder.toString());
        return failItem;
    }

    private static String handleClazzName(String clazzName) {
        return clazzNameMap.containsKey(clazzName) ? clazzNameMap.get(clazzName) : clazzName;
    }

    private final static Map<String, String> clazzNameMap = new HashMap<String, String>() {
        {
            put("一班", "1班");
            put("二班", "2班");
            put("三班", "3班");
            put("四班", "4班");
            put("五班", "5班");
            put("六班", "6班");
            put("七班", "7班");
            put("八班", "8班");
            put("九班", "9班");
            put("十班", "10班");
            put("十一班", "11班");
            put("十二班", "12班");
            put("十三班", "13班");
            put("十四班", "14班");
            put("十五班", "15班");
            put("十六班", "16班");
            put("十七班", "17班");
            put("十八班", "18班");
            put("十九班", "19班");
            put("二十班", "20班");
        }
    };

    @Data
    private static class RegisterTeacherClazzStudent {
        private Long schoolId;
        private String schoolName;
        private String teacherName;
        private String teacherSubject;
        private String mobile;
        private String clazzLevel;
        private String clazzName;
        private String studentName;
        private Long teacherId;
        private String sourceName;
        private String sourceTid;
        private String sourceSid;
        private Long clazzid;
        private String password;
    }

    @Data
    private static class FailItem {
        private String message;
        private String item;
    }
}
