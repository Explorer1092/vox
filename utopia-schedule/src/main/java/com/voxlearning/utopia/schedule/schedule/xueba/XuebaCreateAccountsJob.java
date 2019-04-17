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

package com.voxlearning.utopia.schedule.schedule.xueba;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.guest.api.mapper.XuebaStudentMapper;
import com.voxlearning.utopia.service.guest.api.mapper.XuebaTeacherMapper;
import com.voxlearning.utopia.service.guest.api.service.XuebaService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author changyuan
 * @since 2017/3/1
 */
@Named
@ScheduledJobDefinition(
        jobName = "翻转课堂批量添加老师学生",
        jobDescription = "翻转课堂批量添加老师学生",
        disabled = {Mode.UNIT_TEST, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 11 30 7 ? ",
        ENABLED = false
)
public class XuebaCreateAccountsJob extends ScheduledJobWithJournalSupport {

    @Inject private UserLoaderClient userLoaderClient;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;

    @ImportService(interfaceClass = XuebaService.class) private XuebaService xuebaService;


    @Override
    @SuppressWarnings("unchecked")
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<Map<String, Object>> data = (List<Map<String, Object>>)parameters.get("data");

        List<Long> teacherIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            for (Map<String, Object> map : data) {
                long teacherId = SafeConverter.toLong(map.get("teacherId"));
                if (teacherId == 0) {
                    String teacherName = SafeConverter.toString(map.get("teacherName"));

                    // 注册老师
                    XuebaTeacherMapper xuebaTeacherMapper = new XuebaTeacherMapper();
                    xuebaTeacherMapper.setName(teacherName);
                    MapMessage message = xuebaService.bulkCreateTeachers(Collections.singleton(xuebaTeacherMapper));
                    if (!message.isSuccess()) {
                        logger.error("create teacher failed: {}", message.getInfo());
                        return;
                    }
                    Map<Long, String> usersMap = (Map<Long, String>) message.get("users");

                    logger.info("create teacher result: {}", JsonUtils.toJson(usersMap));

                    teacherId = usersMap.keySet().iterator().next();
                }

                if (teacherId != 0) {
                    teacherIds.add(teacherId);

                    // 注册学生
                    List<Map<String, Object>> clazzs = (List<Map<String, Object>>) map.get("clazzs");
                    if (CollectionUtils.isNotEmpty(clazzs)) {
                        List<XuebaStudentMapper> xuebaStudentMappers = new ArrayList<>();
                        for (Map<String, Object> clazzData : clazzs) {
                            String clazzName = SafeConverter.toString(clazzData.get("clazzName"));
                            List<Map<String, Object>> students = (List<Map<String, Object>>) clazzData.get("students");


                            for (Map<String, Object> studentData : students) {
                                XuebaStudentMapper xuebaStudentMapper = new XuebaStudentMapper();
                                xuebaStudentMapper.setClazzName(clazzName);
                                xuebaStudentMapper.setName(SafeConverter.toString(studentData.get("name")));
                                xuebaStudentMapper.setMobile(SafeConverter.toString(studentData.get("mobile")));
                                xuebaStudentMapper.setNeedBindMobile(SafeConverter.toBoolean(studentData.get("needBindMobile")));

                                xuebaStudentMappers.add(xuebaStudentMapper);
                            }
                        }
                        MapMessage message = xuebaService.bulkAddStudents(xuebaStudentMappers, teacherId);
                        if (!message.isSuccess()) {
                            logger.error("create students failed: {}", message.getInfo());
                            return;
                        }

                        logger.info("create teacher {} students result: {}", teacherId, JsonUtils.toJson(message.get("users")));
                    }
                }
            }
        }

        // 生成excel结果
        String osName = System.getProperty("os.name");
        String filenName = "创建账号" + DateUtils.dateToString(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".xlsx";
        if (osName != null && !osName.contains("Windows")) {
            filenName = "\\tmp\\create-account-" + DateUtils.dateToString(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".xlsx";
        }
        Workbook workbook = generateSheet(teacherIds);
        try {
            FileOutputStream outputStream = new FileOutputStream(filenName);
            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException ex) {
            logger.error("生成结果文件失败!", ex.getMessage(), ex);
        }
    }

    private Workbook generateSheet(Collection<Long> teacherIds) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        int rowNum = 0;
        for (Long teacherId : teacherIds) {
            User user = userLoaderClient.loadUser(teacherId);
            if (user == null) {
                continue;
            }

            Row row = sheet.createRow(rowNum);

            Cell cell = row.createCell(0);
            cell.setCellValue(user.fetchRealname());

            cell = row.createCell(1);
            cell.setCellValue(teacherId);

            List<Clazz> clazzes = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId);
            for (Clazz clazz : clazzes) {

                cell = row.createCell(2);
                cell.setCellValue(clazz.formalizeClazzName());

                List<Long> userIds = asyncGroupServiceClient.getAsyncGroupService()
                        .findStudentIdsByClazzId(clazz.getId());
                Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);

                for (User u : userMap.values()) {
                    cell = row.createCell(3);
                    cell.setCellValue(u.fetchRealname());

                    cell = row.createCell(4);
                    cell.setCellValue(u.getId());

                    rowNum++;
                    row = sheet.createRow(rowNum);
                }
            }
        }
        return workbook;
    }

}
