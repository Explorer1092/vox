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

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.data.TaskRecordReport;
import com.voxlearning.utopia.admin.service.crm.CrmTaskService;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.data.UserRecordSnapshot;
import com.voxlearning.utopia.entity.crm.CrmTask;
import com.voxlearning.utopia.entity.crm.CrmTaskRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.Cleanup;
import org.apache.http.client.utils.URIBuilder;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * @author Jia HuanYin
 * @since 2015/10/19
 */
@Controller
@RequestMapping("/crm/task")
public class CrmTaskController extends CrmAbstractController {

    private static final String RECORD_TEMPLATE_PATH = "/config/task_record.xlsx";

    @Inject private RaikouSystem raikouSystem;
    @Inject private CrmTaskService crmTaskService;

    // 任务列表
    @RequestMapping(value = "task_list.vpage")
    public String taskList(Model model) {
        Date createStart = requestDate("createStart");
        Date createEnd = requestDate("createEnd");
        Date endStart = requestDate("endStart");
        Date endEnd = requestDate("endEnd");
        Date finishStart = requestDate("finishStart");
        Date finishEnd = requestDate("finishEnd");
        String creator = requestString("creator");
        String executor = requestString("executor");
        UserType userType = userType(requestString("userType"));
        CrmTaskType type = CrmTaskType.nameOf(requestString("type"));
        CrmTaskStatus status = CrmTaskStatus.nameOf(requestString("status"));
        Pageable pageable = buildSortPageRequest(100, "createTime");
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        Page<CrmTask> tasks = crmTaskService.loadTasks(createStart, createEnd, endStart, endEnd, finishStart, finishEnd, creator, executor, userType, type, status, pageable, adminUser);
        model.addAttribute("tasks", tasks);
        model.addAttribute("adminUser", adminUser);
        model.addAttribute("taskUsers", crmTaskService.taskUsers(adminUser));
        model.addAttribute("taskTypes", CrmTaskService.taskTypes(adminUser));
        model.addAttribute("taskStatuses", CrmTaskStatus.values());
        model.addAttribute("recordCategoryJson", CrmTaskService.taskRecordCategoryJson(adminUser));
        model.addAttribute("contactTypes", CrmTaskService.contactTypes(adminUser));
        model.addAttribute("isPhoneOut", CrmTaskService.isPhoneOut(adminUser));
        model.addAttribute("createStart", formatDate(createStart));
        model.addAttribute("createEnd", formatDate(createEnd));
        model.addAttribute("endStart", formatDate(endStart));
        model.addAttribute("endEnd", formatDate(endEnd));
        model.addAttribute("creator", creator);
        model.addAttribute("executor", executor);
        model.addAttribute("userType", userType);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("ORDER", requestString("ORDER"));
        model.addAttribute("SORT", requestString("SORT"));
        return "crm/task/task_list";
    }

    // 用户任务
    @RequestMapping(value = "user_task.vpage")
    public String userTask(Model model) {
        Long userId = requestLong("userId");
        String creator = requestString("creator");
        String executor = requestString("executor");
        CrmTaskStatus status = CrmTaskStatus.nameOf(requestString("status"));
        UserType userType = userType(requestString("userType"));
        Pageable pageable = buildSortPageRequest(10, "createTime");
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        Page<CrmTask> tasks = crmTaskService.loadUserTasks(userId, creator, executor, status, pageable);
        model.addAttribute("tasks", tasks);
        model.addAttribute("adminUser", adminUser);
        model.addAttribute("taskUsers", crmTaskService.taskUsers(adminUser));
        model.addAttribute("taskTypes", CrmTaskService.taskTypes(adminUser));
        model.addAttribute("taskStatuses", CrmTaskStatus.values());
        model.addAttribute("recordCategoryJson", CrmTaskService.taskRecordCategoryJson(userType, adminUser));
        model.addAttribute("contactTypes", CrmTaskService.contactTypes(adminUser));
        model.addAttribute("isPhoneOut", CrmTaskService.isPhoneOut(adminUser));
        model.addAttribute("userId", userId);
        model.addAttribute("creator", creator);
        model.addAttribute("executor", executor);
        model.addAttribute("status", status);
        model.addAttribute("userType", userType);
        return "crm/task/user_task";
    }

    // 任务快照
    @RequestMapping(value = "task_snapshot.vpage")
    @ResponseBody
    public CrmTask taskSnapshot() {
        String taskId = requestString("taskId");
        return crmTaskService.taskSnapshot(taskId);
    }

    // 任务详情
    @RequestMapping(value = "task_detail.vpage")
    @ResponseBody
    public CrmTask taskDetail() {
        String taskId = requestString("taskId");
        return crmTaskService.loadTaskDetail(taskId);
    }

    // 新增任务
    @RequestMapping(value = "add_task.vpage")
    @ResponseBody
    public boolean addTask() {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        String executor = requestString("executor");
        CrmTaskType type = CrmTaskType.nameOf(requestString("type"));
        Date endTime = requestDate("endTime");
        String title = requestString("title");
        String content = requestString("content");
        Set<Long> userIds = requestLongSet("userIds");
        CrmTaskAction action = CrmTaskAction.nameOf(requestString("ACTION"));
        String source = requestString("SOURCE");
        List<CrmTask> tasks = crmTaskService.addTasks(user, executor, type, endTime, title, content, userIds, action, source);
        return CollectionUtils.isNotEmpty(tasks);
    }

    // 更改任务
    @RequestMapping(value = "update_task.vpage")
    @ResponseBody
    public CrmTask updateTask() {
        String taskId = requestString("taskId");
        Date endTime = requestDate("endTime");
        String content = requestString("content");
        CrmTaskStatus status = CrmTaskStatus.nameOf(requestString("status"));
        return crmTaskService.updateTask(taskId, endTime, content, status);
    }

    // 完成任务
    @RequestMapping(value = "finish_task.vpage")
    @ResponseBody
    public CrmTask finishTask() {
        String taskId = requestString("taskId");
        return crmTaskService.finishTask(taskId);
    }

    // 跟进任务
    @RequestMapping(value = "follow_task.vpage")
    @ResponseBody
    public CrmTask followTask() {
        String taskId = requestString("taskId");
        return crmTaskService.followTask(taskId);
    }

    // 新增工作记录
    @RequestMapping(value = "add_record.vpage")
    @ResponseBody
    public CrmTaskRecord addRecord() {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        String taskId = requestString("taskId");
        Long userId = requestLong("userId");
        CrmTaskRecordCategory recordCategory = CrmTaskRecordCategory.nameOf(requestString("recordCategory"));
        CrmContactType contactType = CrmContactType.nameOf(requestString("contactType"));
        String content = requestString("content");
        Integer redmineAssigned = requestInteger("redmineAssigned");
        return crmTaskService.addTaskRecord(user, taskId, userId, recordCategory, contactType, content, redmineAssigned);
    }

    // 工作记录列表
    @RequestMapping(value = "record_list.vpage")
    public String recordList(Model model) {
        Date today = new Date();
        Date createStart = requestDate("createStart", today);
        Date createEnd = requestDate("createEnd", today);
        String recorder = requestString("recorder");
        Set<CrmContactType> contactTypes = requestContactTypes("contactType");
        CrmTaskRecordCategory firstCategory = CrmTaskRecordCategory.nameOf(requestString("firstCategory"));
        CrmTaskRecordCategory secondCategory = CrmTaskRecordCategory.nameOf(requestString("secondCategory"));
        CrmTaskRecordCategory thirdCategory = CrmTaskRecordCategory.nameOf(requestString("thirdCategory"));
        UserType userType = userType(requestString("userType"));
        Pageable pageable = buildSortPageRequest(25, "createTime");
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        Page<CrmTaskRecord> taskRecords = crmTaskService.loadTaskRecords(createStart, createEnd, recorder, contactTypes, firstCategory, secondCategory, thirdCategory, userType, pageable, adminUser);
        model.addAttribute("taskRecords", taskRecords);
        model.addAttribute("taskUsers", crmTaskService.taskUsers(adminUser));
        model.addAttribute("contactTypes", CrmTaskService.contactTypes(adminUser));
        model.addAttribute("recordCategoryJson", CrmTaskService.taskRecordCategoryJson(adminUser));
        model.addAttribute("createStart", formatDate(createStart));
        model.addAttribute("createEnd", formatDate(createEnd));
        model.addAttribute("recorder", recorder);
        model.addAttribute("contactType", iContactTypes(contactTypes));
        model.addAttribute("firstCategory", firstCategory);
        model.addAttribute("secondCategory", secondCategory);
        model.addAttribute("thirdCategory", thirdCategory);
        model.addAttribute("userType", userType);
        return "crm/task/record_list";
    }

    private Set<CrmContactType> requestContactTypes(String name) {
        Set<CrmContactType> contactTypes = new HashSet<>();
        String[] values = requestArray(name);
        if (values != null) {
            for (String value : values) {
                CrmContactType contactType = CrmContactType.nameOf(value);
                if (contactType != null) {
                    contactTypes.add(contactType);
                }
            }
        }
        return contactTypes;
    }

    // 用户工作记录列表
    @RequestMapping(value = "_user_record.vpage")
    public String userRecord(Model model) {
        Long userId = requestLong("userId");
        Date createStart = requestDate("createStart");
        Date createEnd = requestDate("createEnd");
        String recorder = requestString("recorder");
        CrmContactType contactType = CrmContactType.nameOf(requestString("contactType"));
        Pageable pageable = buildSortPageRequest(10, "createTime");
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        Page<CrmTaskRecord> taskRecords = crmTaskService.loadUserTaskRecords(userId, createStart, createEnd, recorder, contactType, pageable);
        model.addAttribute("taskRecords", taskRecords);
        model.addAttribute("taskUsers", crmTaskService.allTaskUsers(adminUser));
        model.addAttribute("contactTypes", CrmContactType.values());
        model.addAttribute("userId", userId);
        model.addAttribute("createStart", formatDate(createStart));
        model.addAttribute("createEnd", formatDate(createEnd));
        model.addAttribute("recorder", recorder);
        model.addAttribute("contactType", contactType);
        return "crm/task/user_record";
    }

    // 用户全部工作记录列表
    @RequestMapping(value = "user_all_record.vpage")
    @ResponseBody
    public List<CrmTaskRecord> userAllRecord() {
        Long userId = requestLong("userId");
        return crmTaskService.loadUserAllTaskRecords(userId);
    }

    // 用户工作记录时间轴
    @RequestMapping(value = "user_record.vpage")
    public String userRecordTimeline(Model model) {
        Long userId = requestLong("userId");
        List<UserRecordSnapshot> userRecords = crmTaskService.userRecordTimeline(userId);
        model.addAttribute("userRecords", userRecords);
        return "crm/task/user_record_timeline";
    }

    // 工作记录报表
    @RequestMapping(value = "record_report.vpage")
    public String taskRecordReport(Model model) {
        Date today = new Date();
        Date createStart = requestDate("createStart", today);
        Date createEnd = requestDate("createEnd", today);
        Set<CrmContactType> contactTypes = requestContactTypes("contactType");
        CrmTaskRecordCategory firstCategory = CrmTaskRecordCategory.nameOf(requestString("firstCategory"));
        CrmTaskRecordCategory secondCategory = CrmTaskRecordCategory.nameOf(requestString("secondCategory"));
        CrmTaskRecordCategory thirdCategory = CrmTaskRecordCategory.nameOf(requestString("thirdCategory"));
        UserType userType = userType(requestString("userType"));
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        TaskRecordReport taskRecordReport = crmTaskService.taskRecordReport(createStart, createEnd, contactTypes, firstCategory, secondCategory, thirdCategory, userType, adminUser);
        model.addAttribute("taskRecordReport", taskRecordReport);
        model.addAttribute("contactTypes", CrmTaskService.contactTypes(adminUser));
        model.addAttribute("recordCategoryJson", CrmTaskService.taskRecordCategoryJson(adminUser));
        model.addAttribute("createStart", formatDate(createStart));
        model.addAttribute("createEnd", formatDate(createEnd));
        model.addAttribute("contactType", iContactTypes(contactTypes));
        model.addAttribute("firstCategory", firstCategory);
        model.addAttribute("secondCategory", secondCategory);
        model.addAttribute("thirdCategory", thirdCategory);
        model.addAttribute("userType", userType);
        return "crm/task/record_report";
    }

    private String iContactTypes(Set<CrmContactType> contactTypes) {
        StringBuilder builder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(contactTypes)) {
            for (CrmContactType contactType : contactTypes) {
                builder.append("contactType=").append(contactType.name()).append("&");
            }
        }
        return builder.toString();
    }

    // 工作记录导出
    @RequestMapping(value = "record_export.vpage")
    public void recordExport() {
        try {
            Resource resource = new ClassPathResource(RECORD_TEMPLATE_PATH);
            if (!resource.exists()) {
                logger.error("TaskRecord template not exists @ path = {}", RECORD_TEMPLATE_PATH);
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 18);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFSheet sheet = workbook.getSheetAt(0);
            List<CrmTaskRecord> taskRecords = allTaskRecords();
            if (CollectionUtils.isNotEmpty(taskRecords)) {
                int index = 3;
                FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd HH:mm");
                for (CrmTaskRecord record : taskRecords) {
                    XSSFRow row = sheet.createRow(index++);
                    createCell(row, cellStyle, 0, createTime(format, record.getCreateTime()));
                    createCell(row, cellStyle, 1, userType(record.getUserType()));
                    createCell(row, cellStyle, 2, userSubject(record.getUserType(), record.getUserId()));
                    createCell(row, cellStyle, 3, record.getUserName());
                    createCell(row, cellStyle, 4, String.valueOf(record.getUserId()));
                    createCell(row, cellStyle, 5, recordCategory(record.getFirstCategory()));
                    createCell(row, cellStyle, 6, recordCategory(record.getSecondCategory()));
                    createCell(row, cellStyle, 7, recordCategory(record.getThirdCategory()));
                    createCell(row, cellStyle, 8, record.getContent());
                    createCell(row, cellStyle, 9, contactType(record.getContactType()));
                    createCell(row, cellStyle, 10, record.getRecorderName());
                }
            }
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile("工作记录.xlsx", "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("TaskRecord export Excp : {}; @ path = {}", e, RECORD_TEMPLATE_PATH);
        }
    }

    private List<CrmTaskRecord> allTaskRecords() {
        Date createStart = requestDate("createStart");
        Date createEnd = requestDate("createEnd");
        String recorder = requestString("recorder");
        Set<CrmContactType> contactTypes = requestContactTypes("contactType");
        CrmTaskRecordCategory firstCategory = CrmTaskRecordCategory.nameOf(requestString("firstCategory"));
        CrmTaskRecordCategory secondCategory = CrmTaskRecordCategory.nameOf(requestString("secondCategory"));
        CrmTaskRecordCategory thirdCategory = CrmTaskRecordCategory.nameOf(requestString("thirdCategory"));
        UserType userType = userType(requestString("userType"));
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        return crmTaskService.loadAllTaskRecords(createStart, createEnd, recorder, contactTypes, firstCategory, secondCategory, thirdCategory, userType, adminUser);
    }

    private XSSFCell createCell(XSSFRow row, XSSFCellStyle style, int index, String value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(value);
        return cell;
    }

    private UserType userType(String value) {
        try {
            return UserType.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String createTime(final FastDateFormat format, Date createTime) {
        return format == null || createTime == null ? "" : format.format(createTime);
    }

    private String userType(UserType userType) {
        return userType == null ? "" : userType.getDescription();
    }

    private String userSubject(UserType userType, Long userId) {
        if (userType == null || userType != UserType.TEACHER) {
            return "";
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        return teacher == null || teacher.getSubject() == null ? "" : teacher.getSubject().getValue();
    }

    private String recordCategory(CrmTaskRecordCategory recordCategory) {
        return recordCategory == null ? "" : recordCategory.name();
    }

    private String contactType(CrmContactType contactType) {
        return contactType == null ? "" : contactType.name();
    }

    // 市场工作记录列表
    @RequestMapping(value = "work_record_list.vpage")
    public String workRecordList(Model model) {
        Date today = new Date();
        CrmWorkRecordType workType = CrmWorkRecordType.nameOf(requestString("workType", CrmWorkRecordType.MEETING.name()));
        Date startTime = requestDate("startTime", today);
        Date endTime = requestDate("endTime", today);
        Integer provinceCode = requestInteger("provinceCode");
        Integer cityCode = requestInteger("cityCode");
        Integer countyCode = requestInteger("countyCode");
        Pageable pageable = buildSortPageRequest(25, "workTime");
        Page<CrmWorkRecord> workRecords = crmTaskService.loadWorkRecords(workType, startTime, endTime, provinceCode, cityCode, countyCode, pageable);
        model.addAttribute("workRecords", workRecords);
        model.addAttribute("provinces", raikouSystem.getRegionBuffer().loadProvinces());
        model.addAttribute("workType", workType);
        model.addAttribute("startTime", formatDate(startTime));
        model.addAttribute("endTime", formatDate(endTime));
        model.addAttribute("provinceCode", provinceCode);
        model.addAttribute("cityCode", cityCode);
        model.addAttribute("countyCode", countyCode);
        return "crm/task/work_record_list";
    }

    // 省级区域列表
    @RequestMapping(value = "province_regions.vpage")
    @ResponseBody
    public List<ExRegion> provinceRegions() {
        return raikouSystem.getRegionBuffer().loadProvinces();
    }

    // 下级区域列表
    @RequestMapping(value = "child_regions.vpage")
    @ResponseBody
    public List<ExRegion> childRegions() {
        Integer regionCode = requestInteger("regionCode");
        return raikouSystem.getRegionBuffer().loadChildRegions(regionCode);
    }

    //任务批量转发
    @RequestMapping(value = "task_batch_forward.vpage")
    @ResponseBody
    public boolean taskBatchForward() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        String executor = requestString("executor");
        String taskIds = requestString("taskIds");
        List<String> taskIdList = Arrays.asList(StringUtils.split(taskIds, ","));
        CrmTaskAction action = CrmTaskAction.nameOf(requestString("ACTION"));
        Boolean create_flag = requestBoolean("create_flag"); //标记是创建新的任务，还是修改原来的任务
        List retList = crmTaskService.taskBatchForward(adminUser, executor, taskIdList, action, create_flag == null ? false : create_flag);
        return CollectionUtils.isNotEmpty(retList);
    }

    // 市场人员流转的任务
    @RequestMapping(value = "add_agent_task.vpage")
    @ResponseBody
    public MapMessage addAgentTask() {
        List<Properties> dataList = JsonUtils.fromJsonToList(requestString("data"), Properties.class);
        CrmTaskAction action = CrmTaskAction.TASK_NEW;
        List<CrmTask> tasks = crmTaskService.addAgentTasks(dataList, action);
        if (CollectionUtils.isEmpty(tasks)) {
            return MapMessage.errorMessage("create crmTask fired");
        }
        return MapMessage.successMessage(tasks.size() + "");

    }


    // 将任务转发给市场
    @RequestMapping(value = "task_forward_to_agent.vpage")
    @ResponseBody
    public boolean taskForwardToAgent() {
        String taskId = requestString("taskId");
        Boolean needFollow = requestBoolean("needFollow");  // 是否需要市场人员继续跟进
        boolean result = false;
        CrmTask crmTask = crmTaskService.loadTask(taskId);
        if (crmTask != null && StringUtils.isNotEmpty(crmTask.getAgentTaskId())) {
            try {
                URIBuilder builder = new URIBuilder(super.getMarketingUrl() + "/task/manage/update_task_detail_state.vpage");
                URI uri = builder.build();
                HttpRequestExecutor executor = HttpRequestExecutor.defaultInstance();
                AlpsHttpResponse response = executor.post(uri)
                        .addParameter("taskDetailId", crmTask.getAgentTaskId())
                        .addParameter("needFollow", String.valueOf(needFollow))
                        .execute();
                if (!response.hasHttpClientException()) {
                    //成功
                    result = true;
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

    @RequestMapping(value = "allready_record_by_myself.vpage")
    @ResponseBody
    public boolean checkWorkRecord() {
        String taskId = requestString("taskId");
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        List recordList = crmTaskService.loadRecordByRecorderAndTaskId(adminUser.getAdminUserName(), taskId);
        if (CollectionUtils.isEmpty(recordList)) {
            return false;
        }
        return true;
    }

    @RequestMapping(value = "delete_crm_task.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteCrmTask() {
        List<String> taskDetailId = JsonUtils.fromJsonToList(getRequestString("taskDetailIds"), String.class);
        return crmTaskService.finishTasksBytaskDetailId(taskDetailId);
    }
}
