/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.task;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.com.alibaba.dubbo.common.URL;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.dao.mongo.AgentTaskDetailDao;
import com.voxlearning.utopia.agent.persist.entity.AgentTask;
import com.voxlearning.utopia.agent.service.mobile.AgentTaskDetailService;
import com.voxlearning.utopia.agent.service.mobile.AgentTaskService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.api.constant.AgentTaskCategory;
import com.voxlearning.utopia.api.constant.AgentTaskStatus;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.entity.agent.AgentTaskDetail;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/11/28
 */
@Controller
@RequestMapping(value = "/task/manage")
public class TaskManageController extends AbstractAgentController {

    private static final int BYTES_BUFFER_SIZE = 1024 * 8;
    private static final String CRM_DELETE_TASK_URL = "/crm/task/delete_crm_task.vpage";

    @Inject
    AgentTaskService agentTaskService;
    @Inject
    AgentTaskDetailDao agentTaskDetailDao;
    @Inject
    WorkRecordService workRecordService;
    @Inject
    AgentTaskDetailService agentTaskDetailService;

    @RequestMapping(value = "creater_tasks.vpage")
    public String createrTasks(Model model) {
        Date today = new Date();
        Long createrId = getCurrentUserId();
        Date createStart = requestDate("createStart", today);
        Date createEnd = requestDate("createEnd", today);
        List<AgentTask> createrTasks = agentTaskService.createrTasks(createrId, createStart, createEnd);
        String errorMessage = getRequestString("errorMessage");
        if (StringUtils.isNotBlank(errorMessage)) {
            model.addAttribute("errorMessage", errorMessage);
        }
        model.addAttribute("createrTasks", createrTasks);
        model.addAttribute("categories", AgentTaskCategory.values());
        model.addAttribute("createStart", formatDate(createStart));
        model.addAttribute("createEnd", formatDate(createEnd));
        return "task/manage/creater_tasks";
    }

    @RequestMapping(value = "edit_task.vpage")
    public String editTask(Model model) {
        AgentTaskCategory[] agentTaskCategorys = AgentTaskCategory.values();
        List<AgentTaskCategory> agentTaskCategoryList = new ArrayList<>();
        for (AgentTaskCategory agentTaskCategory : agentTaskCategorys) {
            if (!agentTaskCategory.name().equals("SCHOOL_FOLLOW")) {
                agentTaskCategoryList.add(agentTaskCategory);
            }
        }
        model.addAttribute("categories", agentTaskCategoryList);
        model.addAttribute("createStart", requestString("createStart"));
        model.addAttribute("createEnd", requestString("createEnd"));
        return "task/manage/edit_task";
    }

    @RequestMapping(value = "create_task.vpage")
    public String createTask() {
        String title = requestString("title");
        String content = requestString("content");
        Date endTime = requestDate("endTime");
        AgentTaskCategory category = AgentTaskCategory.nameOf(requestString("category"));
        XSSFWorkbook workbook = readRequestWorkbook("sourceFile");
        String needOutbound = requestString("needOutbound", "");
        AuthCurrentUser currentUser = getCurrentUser();
        endTime = DateUtils.getDayEnd(endTime);
        AgentTask agentTask = agentTaskService.createTask(title, content, endTime, category, workbook, currentUser, needOutbound.equals("needOutbound"));
        String errorMessage = "";
        if (agentTask == null || agentTask.getTotalCount() <= 0) {
            errorMessage = "创建失败";
        }
        String createStart = requestString("createStart", "");
        String createEnd = requestString("createEnd", "");
        return redirect("creater_tasks.vpage?createStart=" + createStart + "&createEnd=" + createEnd + "&errorMessage=" + URL.encode(errorMessage));
    }

    private XSSFWorkbook readRequestWorkbook(String name) {
        HttpServletRequest request = getRequest();
        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return new XSSFWorkbook(in);
        } catch (Exception e) {
            logger.error("readRequestWorkbook - Excp : {}", e);
            return null;
        }
    }

    @RequestMapping(value = "export_task.vpage")
    public void exportTask() {
        String id = requestString("id");
        String title = requestString("title", "");
        try {
            XSSFWorkbook workbook = agentTaskService.exportTask(id);
            if (workbook == null) {
                logger.error("exportTask - Null workbook for id = {}", id);
                return;
            }
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            out.flush();
            String fileName = "任务【" + title + "】完成情况.xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("exportTask - Excp : {}; id = {}", e, id);
        }
    }

    @RequestMapping(value = "download_template.vpage")
    public void downloadTemplate() {
        AgentTaskCategory category = AgentTaskCategory.nameOf(requestString("category"));
        if (category == null) {
            logger.error("downloadTemplate - Null category");
            return;
        }
        try {
            Resource resource = new ClassPathResource(category.getTemplateImport());
            if (!resource.exists()) {
                logger.error("downloadTemplate - template not exists for category = {}", category);
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            String fileName = "【" + category.getValue() + "】任务导入模板.xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("downloadTemplate - Excp : {}; category = {}", e, category);
        }
    }

    @RequestMapping(value = "update_task_detail_state.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateTaskDetailStatus() {
        String taskDetailId = getRequestString("taskDetailId");
        Boolean needAgentFollow = getRequestBool("needFollow");//是否需要继续跟进
        boolean isSuccess = agentTaskDetailService.updateTransferBackStatus(taskDetailId, needAgentFollow);
        return isSuccess ? MapMessage.successMessage() : MapMessage.errorMessage();
    }

    private AgentTaskDetail loadTaskDetail(String id) {
        return id == null ? null : agentTaskDetailDao.load(id);
    }

    @RequestMapping(value = "add_crm_work_record.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addCrmWorkRecord() {
        String taskDetailId = getRequestString("taskDetailId");
        String title = getRequestString("title");
        String content = getRequestString("content");
        Long executorId = getRequestLong("executorId");
        Boolean isFinished = getRequestBool("isFinished");
        CrmWorkRecord crmWorkRecord = workRecordService.addTaskDetailWorkRecord(taskDetailId, title, content, executorId);
        if (crmWorkRecord == null) {
            return MapMessage.errorMessage("add crmWorkRecord field ");
        }
        if (isFinished) {
            AgentTaskDetail agentTaskDetail = loadTaskDetail(taskDetailId);
            if (agentTaskDetail == null) {
                return MapMessage.errorMessage("can`t found AgentTaskDetail taskDetailId={}", taskDetailId);
            }
            agentTaskDetail.setStatus(AgentTaskStatus.FINISHED);
            try {
                agentTaskDetailDao.upsert(agentTaskDetail);
            } catch (Exception ex) {
                return MapMessage.errorMessage("update taskDetail field ", ex);
            }
        }
        return MapMessage.successMessage("succeed");
    }

    @RequestMapping(value = "delete_task.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTask() {
        String taskId = requestString("taskId");
        Map<Object, Object> paramMap = new HashMap<>();
        List<AgentTaskDetail> agentTaskDetails = agentTaskDetailService.findByNeedCustomerService(taskId);
        if (CollectionUtils.isNotEmpty(agentTaskDetails)) {
            try {
                List<String> taskDetailIds = agentTaskDetails.stream().map(AgentTaskDetail::getId).collect(Collectors.toList());
                String URL = agentTaskService.getAdminBaseUrl().concat(CRM_DELETE_TASK_URL);
                Map<String, Long> taskDetailIdTeacherIdMap = new HashMap<>();
                agentTaskDetails.forEach(p -> taskDetailIdTeacherIdMap.put(p.getId(), p.getTeacherId()));
                String taskDetailIdJson = JsonUtils.toJson(taskDetailIds);
                paramMap.put("taskDetailIds", taskDetailIdJson);
                paramMap.put("source", "agent_batch");
                AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(URL).addParameter(paramMap).execute();
                if (response == null || response.hasHttpClientException() || response.getStatusCode() != 200) {
                    return MapMessage.errorMessage("任务删除失败,请与管理员联系！");
                }
                MapMessage resultMap = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
                if (!resultMap.isSuccess()) {
                    logger.warn(resultMap.getInfo());
                    return MapMessage.errorMessage("任务删除失败,请与管理员联系！");
                }
            } catch (Exception ex) {
                //HTTP请求发送失败
                logger.error("Create crmTask is field paramMap ={} ,URL = {}", paramMap, agentTaskService.getAdminBaseUrl().concat(CRM_DELETE_TASK_URL));
            }
        }
        agentTaskDetailService.deleteTaskDetail(taskId);

        AgentTask agentTask = agentTaskService.updateTaskDisabled(taskId, true);
        if (agentTask == null) {
            return MapMessage.errorMessage("任务删除失败或已删除");
        }
        return MapMessage.successMessage("任务删除成功");
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
