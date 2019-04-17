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

package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.DateUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.dao.CrmTaskRecordDao;
import com.voxlearning.utopia.agent.dao.CrmUGCSchoolDao;
import com.voxlearning.utopia.agent.dao.CrmUGCSchoolTaskDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentSchoolTaskDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentTaskDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentTaskDetailDao;
import com.voxlearning.utopia.agent.persist.entity.AgentSchoolTask;
import com.voxlearning.utopia.agent.persist.entity.AgentTask;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.api.constant.AgentTaskCategory;
import com.voxlearning.utopia.api.constant.AgentTaskStatus;
import com.voxlearning.utopia.entity.agent.AgentTaskDetail;
import com.voxlearning.utopia.entity.crm.CrmTaskRecord;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchool;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchoolTask;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/11/26
 */
@Named
public class AgentTaskService extends AbstractAgentService {

    private static final int TASK_NOTICE_TYPE = AgentNotifyType.TASK_DISPATCH_NOTICE.getType();
    private static final String TASK_DISPATCH_NOTICE = "任务导入完成，导入总数：{0}，成功派发数：{1}";
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
    private static final FastDateFormat TIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");
    private static final String CRM_TASK_URL = "/crm/task/add_agent_task.vpage";
    private static final FastDateFormat FORMATTIME = FastDateFormat.getInstance("yyyy-MM-dd", Locale.CHINA);

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private RaikouSDK raikouSDK;
    @Inject private CrmTaskRecordDao crmTaskRecordDao;
    @Inject private CrmUGCSchoolDao crmUGCSchoolDao;
    @Inject private CrmUGCSchoolTaskDao crmUGCSchoolTaskDao;
    @Inject private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;

    @Inject
    AgentTaskDao agentTaskDao;
    @Inject
    AgentSchoolTaskDao agentSchoolTaskDao;
    @Inject
    BaseUserService baseUserService;
    @Inject
    AgentNotifyService agentNotifyService;
    @Inject
    AgentTaskDetailDao agentTaskDetailDao;
    @Inject
    BaseOrgService baseOrgService;
    @Inject
    CrmSummaryLoaderClient crmSummaryLoaderClient;

    public AgentTask loadTask(String id) {
        return id == null ? null : agentTaskDao.load(id);
    }

    public List<AgentTask> createrTasks(Long createrId, Date createStart, Date createEnd) {
        if (createrId == null || createStart == null || createEnd == null) {
            return null;
        }
        Date start = DateUtils.truncate(createStart, Calendar.DATE);
        Date end = DateUtils.ceiling(createEnd, Calendar.DATE);
        List<AgentTask> createrTasks = agentTaskDao.findCreaterIdIs(createrId, start, end);
        if (CollectionUtils.isNotEmpty(createrTasks)) {
            createrTasks.forEach(this::countTaskFinishCount);
            createrTasks.forEach(this::countNeedOutboundCount);
        }
        return createrTasks;
    }

    private void countTaskFinishCount(AgentTask task) {
        String taskId = task.getId();
        AgentTaskCategory category = task.getCategory();
        switch (category) {
            case SCHOOL_FOLLOW:
                task.setFinishCount(agentSchoolTaskDao.countFinished(taskId));
                break;
            case WASTAGE_TEACHER_FOLLOW:
                List<AgentTaskDetail> taskDetailList = agentTaskDetailDao.findByTaskId(taskId);
                if (CollectionUtils.isEmpty(taskDetailList)) {
                    task.setFinishCount(0L);
                } else {
                    long count = taskDetailList.stream().filter(p -> AgentTaskStatus.FINISHED == p.getStatus()).count();
                    task.setFinishCount(count);
                }
                break;
            default:
                break;
        }
    }

    private void countNeedOutboundCount(AgentTask task) {
        String taskId = task.getId();
        AgentTaskCategory category = task.getCategory();
        switch (category) {
            case WASTAGE_TEACHER_FOLLOW:
                List<AgentTaskDetail> taskDetailList = agentTaskDetailDao.findByTaskId(taskId);
                if (CollectionUtils.isEmpty(taskDetailList)) {
                    task.setNeedOutboundCount(0L);
                } else {
                    long count = taskDetailList.stream().filter(p -> p.getNeedCustomerService() != null && p.getNeedCustomerService()).count();
                    task.setNeedOutboundCount(count);
                }
                break;
            default:
                break;
        }
    }


    public AgentTask createTask(String title, String content, Date endTime, AgentTaskCategory category, XSSFWorkbook workbook, AuthCurrentUser currentUser, boolean needCustomerService) {
        if (StringUtils.isBlank(title) || StringUtils.isBlank(content) || endTime == null || category == null || currentUser == null) {
            return null;
        }
        long totalCount = 0;
        AgentTask task = saveTask(title, content, endTime, totalCount, needCustomerService, category, currentUser.getUserId(), currentUser.getRealName());
        if (task == null) {
            return null;
        }

        String taskId = task.getId();
        switch (category) {
            case SCHOOL_FOLLOW:
                totalCount = (long) dispatchSchoolTasks(workbook, task);
                break;
            case WASTAGE_TEACHER_FOLLOW:
                totalCount = (long) dispatchTeacherTasks(workbook, task);
                break;
            case TEACHER_CHANGE_SCHOOL:

            default:
                break;
        }
        if (totalCount > 0) {
            task = updateTaskTotalCount(taskId, totalCount);
        } else {
            task = updateTaskDisabled(taskId, true);
        }
        return task;
    }


    public AgentTask saveTask(String title, String content, Date endTime, Long totalCount, boolean needCustomerService, AgentTaskCategory category, Long createrId, String createrName) {
        if (StringUtils.isBlank(title) || StringUtils.isBlank(content) || endTime == null || category == null || createrId == null) {
            return null;
        }
        AgentTask task = new AgentTask();
        task.setCreaterId(createrId);
        task.setCreaterName(createrName);
        task.setTitle(title);
        task.setContent(content);
        task.setEndTime(endTime);
        task.setTotalCount(totalCount);
        task.setNeedCustomerService(needCustomerService);
        task.setCategory(category);
        task.setStatus(AgentTaskStatus.FOLLOWING);
        task.setDisabled(false);
        agentTaskDao.insert(task);
        return task;
    }

    public AgentTask updateTaskTotalCount(String id, Long totalCount) {
        AgentTask task = loadTask(id);
        if (task == null) {
            return null;
        }
        task.setTotalCount(totalCount);
        agentTaskDao.update(id, task);
        return task;
    }

    public AgentTask updateTaskDisabled(String id, boolean disabled) {
        AgentTask task = loadTask(id);
        if (task == null) {
            return null;
        }
        task.setDisabled(disabled);
        agentTaskDao.update(id, task);
        return task;
    }

    private int dispatchSchoolTasks(XSSFWorkbook workbook, AgentTask task) {
        int rows = 0;
        int dispatch = 0;
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet != null) {
            List<AgentSchoolTask> schoolTasks = new ArrayList<>();
            while (true) {
                XSSFRow row = sheet.getRow(rows++);
                if (row == null) {
                    break;
                }
                Long schoolId = XssfUtils.getLongCellValue(row.getCell(0));
                if (schoolId == null) {
                    continue;
                }
                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(schoolId)
                        .getUninterruptibly();
                if (school == null) {
                    logger.warn("Null school for schoolId = {}", schoolId);
                    continue;
                }
                AgentUser agentUser = schoolAgentUser(school);
                if (agentUser == null) {
                    logger.warn("None schoolAgentUser for schoolId = {}", schoolId);
                    continue;
                }
                AgentSchoolTask schoolTask = new AgentSchoolTask();
                BeanUtils.copyProperties(task, schoolTask);
                schoolTask.setId(null);
                schoolTask.setTaskId(task.getId());
                schoolTask.setSchoolId(schoolId);
                schoolTask.setSchoolName(school.getCname());
                schoolTask.setContactId(XssfUtils.getLongCellValue(row.getCell(2)));
                schoolTask.setContactName(XssfUtils.getStringCellValue(row.getCell(1)));
                schoolTask.setExecutorId(agentUser.getId());
                schoolTask.setExecutorName(agentUser.getRealName());
                schoolTasks.add(schoolTask);
            }
            if (CollectionUtils.isNotEmpty(schoolTasks)) {
                agentSchoolTaskDao.inserts(schoolTasks);
                dispatch = schoolTasks.size();
            }
        }
        String content = MessageFormat.format(TASK_DISPATCH_NOTICE, String.valueOf(rows), String.valueOf(dispatch));
        List<Long> receivers = Collections.singletonList(task.getCreaterId());
        agentNotifyService.sendNotify(TASK_NOTICE_TYPE, content, receivers);
        return dispatch;
    }

    public int dispatchTeacherTasks(XSSFWorkbook workbook, AgentTask task) {
        //计算任务的总数和完成率
        int rows = 0;
        int dispatch = 0;
        if (task == null) {
            return 0;
        }
        //String taskId = task.getId();
        Set<Long> teacherIds = new HashSet<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet != null) {
            //接收任务详情
            List<AgentTaskDetail> taskDetails = new ArrayList<>();
            while (true) {
                XSSFRow row = sheet.getRow(rows++);
                if (row == null) {
                    break;
                }
                //获取学校详情
                Long schoolId = XssfUtils.getLongCellValue(row.getCell(0));
                if (schoolId == null) {
                    continue;
                }
                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(schoolId)
                        .getUninterruptibly();
                if (school == null) {
                    logger.warn("Null school for schoolId = {}", schoolId);
                    continue;
                }
                //获取学校姓名
                String schoolName = XssfUtils.getStringCellValue(row.getCell(1));
                if (schoolName == null) {
                    logger.warn("Null school for schoolName ,schoolId = {}", schoolId);
                    continue;
                }
                schoolName = school.getCname();
//                if (!schoolName.equals(school.getCname()) && !schoolName.equals(school.getShortName()) && !schoolName.equals(school.getEname())) {
//                    logger.warn("school for schoolName is different schoolId = {} ,schoolName = {}", school.getId(), schoolName);
//                    continue;
//                }

                //获取老师详情
                Long teacherId = XssfUtils.getLongCellValue(row.getCell(2));
                if (teacherId == null) {
                    continue;
                }
                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                if (teacher == null) {
                    logger.warn("Null teacher for teacherId = {}", teacherId);
                    continue;
                }

                //获取老师名字
                String teacherName = XssfUtils.getStringCellValue(row.getCell(3));

                if (teacherName == null) {
                    logger.warn("Null teacher for teacherName ,teacherId = {}", teacherName);
                    continue;
                }
                teacherName = teacher.getProfile().getRealname();
//                if (!teacherName.equals(teacher.getProfile().getEname()) && !teacherName.equals(teacher.getProfile().getRealname()) && !teacherName.equals(teacher.getProfile().getNickName())) {
//                    logger.warn("teacher for teacherName is different teacherId = {} ,teacherName = {}", teacher.getId(), teacherName);
//                    continue;
//                }

                //取老师的学校信息
                Map<Long, School> teacherIdSchoolMap = asyncTeacherServiceClient.getAsyncTeacherService()
                        .loadTeacherSchools(Collections.singletonList(teacherId))
                        .getUninterruptibly();
                if (teacherIdSchoolMap == null || teacherIdSchoolMap.get(teacherId) == null) {
                    logger.warn("teacher for school is Null teacherId={}", teacherId);
                    continue;
                }

                //判断老师是不是这个学校中
                School teacherSchool = teacherIdSchoolMap.get(teacherId);
                if (!schoolId.equals(teacherSchool.getId())) {
                    logger.warn("The teacher is not in the school teacherId={} ,schoolId ={}", teacherId, schoolId);
                    continue;
                }

                //Excel导入的老师去重
                if (teacherIds.contains(teacherId)) {
                    logger.warn("The teacher is already exist teacherId={} ,schoolId ={}", teacherId, schoolId);
                    continue;
                }

                AgentTaskDetail taskDetail = new AgentTaskDetail();
                taskDetail.setTaskId(task.getId());
                taskDetail.setTitle(task.getTitle());
                taskDetail.setContent(task.getContent());
                taskDetail.setCategory(task.getCategory());

                List<AgentUser> agentUserList = baseOrgService.getSchoolManager(schoolId); // 获取负责该学校的市场人员
                if (CollectionUtils.isNotEmpty(agentUserList)) {
                    taskDetail.setExecutorId(agentUserList.get(0).getId());
                    taskDetail.setExecutorName(agentUserList.get(0).getRealName());
                }

                taskDetail.setSchoolId(schoolId);
                taskDetail.setSchoolName(schoolName);
                taskDetail.setTeacherId(teacherId);
                taskDetail.setTeacherName(teacherName);

                taskDetail.setStatus(AgentTaskStatus.FOLLOWING);
                taskDetail.setEndTime(task.getEndTime());
                taskDetail.setNeedCustomerService(task.getNeedCustomerService());  //是否需要客服跟进
                taskDetail.setDisabled(false);

                //存入可插入的结果集中
                taskDetails.add(taskDetail);
                //存入老师去重集合
                teacherIds.add(teacherId);
            }
            agentTaskDetailDao.inserts(taskDetails);
            if (task.getNeedCustomerService()) {
                List<AgentTaskDetail> agentTaskDetails = agentTaskDetailDao.findByTaskId(task.getId());
                executeRemoteRequest(agentTaskDetails);
            }
            List<AgentTaskDetail> list = agentTaskDetailDao.findByTaskId(task.getId());
            if (CollectionUtils.isNotEmpty(list)) {
                dispatch = list.size();
            }
        }
        String content = MessageFormat.format(TASK_DISPATCH_NOTICE, String.valueOf(rows), String.valueOf(dispatch));
        List<Long> receivers = Collections.singletonList(task.getCreaterId());
        agentNotifyService.sendNotify(TASK_NOTICE_TYPE, content, receivers);
        return dispatch;
    }

    public MapMessage transferToCrmByIds(Collection<String> taskDetailIds) {
        if (CollectionUtils.isEmpty(taskDetailIds)) {
            return MapMessage.successMessage();
        }
        Map<String, AgentTaskDetail> detailMap = agentTaskDetailDao.loads(taskDetailIds);
        if (MapUtils.isNotEmpty(detailMap)) {
            executeRemoteRequest(detailMap.values());
        }
        return MapMessage.successMessage();
    }

    private void executeRemoteRequest(Collection<AgentTaskDetail> agentTaskDetailList) {
        List<AgentTaskDetail> targetList = agentTaskDetailList.stream().filter(AgentTaskDetail::getNeedCustomerService).collect(Collectors.toList());
        Map<Integer, List<AgentTaskDetail>> targetMap = targetList.stream().collect(Collectors.groupingBy(p -> targetList.indexOf(p) / 300, Collectors.toList()));

        targetMap.values().forEach(p -> {
            List<Map<String, Object>> paramMapList = createRemoteRequestParamMapList(p);

            String paramData = JsonUtils.toJson(paramMapList);
            Map<Object, Object> dataMap = new HashMap<>();
            dataMap.put("data", paramData);
            dataMap.put("source", "agent_batch");
            String URL = getAdminBaseUrl().concat(CRM_TASK_URL);
            AlpsThreadPool.getInstance().submit(() -> HttpRequestExecutor.defaultInstance().post(URL).addParameter(dataMap).execute());
        });

    }

    private List<Map<String, Object>> createRemoteRequestParamMapList(List<AgentTaskDetail> agentTaskDetails) {
        if (CollectionUtils.isEmpty(agentTaskDetails)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Object> paramMap;
        for (AgentTaskDetail taskDetail : agentTaskDetails) {
            paramMap = new HashMap<>();
            paramMap.put("type", taskDetail.getCategory().getValue());
            paramMap.put("title", taskDetail.getTitle());
            paramMap.put("endTime", FORMATTIME.format(taskDetail.getEndTime()));
            paramMap.put("content", taskDetail.getContent());
            paramMap.put("taskDetailId", taskDetail.getId());
            if (taskDetail.getCategory() == AgentTaskCategory.WASTAGE_TEACHER_FOLLOW ||
                    taskDetail.getCategory() == AgentTaskCategory.TEACHER_CHANGE_SCHOOL ||
                    taskDetail.getCategory() == AgentTaskCategory.TEACHER_CREATE_CLAZZ ||
                    taskDetail.getCategory() == AgentTaskCategory.TEACHER_BIND_MOBILE) {
                paramMap.put("teacherId", taskDetail.getTeacherId() == null ? 0L : taskDetail.getTeacherId());
                paramMap.put("applicantName", taskDetail.getExecutorName());
                paramMap.put("applicantMobile", taskDetail.getExecutorMobile());
            }
            retList.add(paramMap);
        }
        return retList;
    }

    public XSSFWorkbook exportTask(String id) {
        AgentTask task = loadTask(id);
        if (task == null) {
            return null;
        }
        AgentTaskCategory category = task.getCategory();
        switch (category) {
            case SCHOOL_FOLLOW:
                return exportSchoolTasks(task);
            case WASTAGE_TEACHER_FOLLOW:
                return exportTeacherTasks(task);
            default:
                return null;
        }
    }

    private XSSFWorkbook exportTeacherTasks(AgentTask task) {
        Resource resource = new ClassPathResource(AgentTaskCategory.WASTAGE_TEACHER_FOLLOW.getTemplateExport());
        if (!resource.exists()) {
            logger.error("exportSchoolTasks - template not exists");
            return null;
        }
        String taskId = task.getId();
        try {
            @Cleanup InputStream in = resource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFRow header = sheet.getRow(0);
            cellValue(header, 1, format(task.getTitle()));
            cellValue(header, 3, formatDate(task.getCreateTime()));
            cellValue(header, 5, formatDate(task.getEndTime()));
            List<AgentTaskDetail> taskDetails = loadAgentTaskDeatil(taskId);
            //查询工作记录
            if (CollectionUtils.isNotEmpty(taskDetails)) {
                int index = 2;
                for (AgentTaskDetail taskDetail : taskDetails) {
                    //任务详情中的记录
                    List<CrmTaskRecord> crmTaskRecordList = crmTaskRecordDao.findByAgentTaskDetailId(taskDetail.getId());
                    for (CrmTaskRecord crmTaskRecord : crmTaskRecordList) {
                        XSSFRow row = sheet.createRow(index++);
                        createCell(row, 0, cellStyle, format(taskDetail.getSchoolId()));
                        createCell(row, 1, cellStyle, format(taskDetail.getSchoolName()));
                        createCell(row, 2, cellStyle, format(taskDetail.getTeacherId()));
                        createCell(row, 3, cellStyle, format(taskDetail.getTeacherName()));
                        createCell(row, 4, cellStyle, format("admin." + crmTaskRecord.getRecorderName()));
                        createCell(row, 5, cellStyle, format("admin." + crmTaskRecord.getRecorder()));
                        createCell(row, 6, cellStyle, formatDate(crmTaskRecord.getCreateTime()));
                        createCell(row, 7, cellStyle, format(crmTaskRecord.getContent()));
                        createCell(row, 8, cellStyle, format(taskDetail.getStatus() == AgentTaskStatus.FINISHED ? "已完成" : taskDetail.getNeedCustomerService() ? "外呼跟踪" : "未完成"));
                    }
                    //工作详情中的记录
                    List<CrmWorkRecord> crmWorkRecordList = crmWorkRecordLoaderClient.listByTaskDetailId(taskDetail.getId());
                    for (CrmWorkRecord crmWorkRecord : crmWorkRecordList) {
                        XSSFRow row = sheet.createRow(index++);
                        createCell(row, 0, cellStyle, format(taskDetail.getSchoolId()));
                        createCell(row, 1, cellStyle, format(taskDetail.getSchoolName()));
                        createCell(row, 2, cellStyle, format(taskDetail.getTeacherId()));
                        createCell(row, 3, cellStyle, format(taskDetail.getTeacherName()));
                        createCell(row, 4, cellStyle, format("marketing." + crmWorkRecord.getWorkerName()));
                        createCell(row, 5, cellStyle, format("marketing." + crmWorkRecord.getWorkerId()));
                        createCell(row, 6, cellStyle, formatDate(crmWorkRecord.getWorkTime()));
                        createCell(row, 7, cellStyle, format(crmWorkRecord.getWorkContent()));
                        createCell(row, 8, cellStyle, format(taskDetail.getStatus() == AgentTaskStatus.FINISHED ? "已完成" : taskDetail.getNeedCustomerService() ? "外呼跟踪" : "未完成"));
                    }

                    if (crmTaskRecordList.size() == 0 && crmWorkRecordList.size() == 0) {
                        XSSFRow row = sheet.createRow(index++);
                        createCell(row, 0, cellStyle, format(taskDetail.getSchoolId()));
                        createCell(row, 1, cellStyle, format(taskDetail.getSchoolName()));
                        createCell(row, 2, cellStyle, format(taskDetail.getTeacherId()));
                        createCell(row, 3, cellStyle, format(taskDetail.getTeacherName()));
                        createCell(row, 4, cellStyle, format(""));
                        createCell(row, 5, cellStyle, format(""));
                        createCell(row, 6, cellStyle, format(""));
                        createCell(row, 7, cellStyle, format(""));
                        createCell(row, 8, cellStyle, format(taskDetail.getNeedCustomerService() ? "外呼跟踪" : "未完成"));
                    }
                }
            }
            return workbook;
        } catch (Exception e) {
            logger.error("exportSchoolTasks - Excp : {}; taskId = {}", e, taskId);
            return null;
        }
    }

    private XSSFWorkbook exportSchoolTasks(AgentTask task) {
        Resource resource = new ClassPathResource(AgentTaskCategory.SCHOOL_FOLLOW.getTemplateExport());
        if (!resource.exists()) {
            logger.error("exportSchoolTasks - template not exists");
            return null;
        }
        String taskId = task.getId();
        try {
            @Cleanup InputStream in = resource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFRow header = sheet.getRow(0);
            cellValue(header, 1, formatDate(task.getCreateTime()));
            cellValue(header, 3, formatDate(task.getEndTime()));
            cellValue(header, 4, format(task.getTitle()));
            List<AgentSchoolTask> schoolTasks = loadSchoolTasks(taskId);
            if (CollectionUtils.isNotEmpty(schoolTasks)) {
                int index = 2;
                for (AgentSchoolTask schoolTask : schoolTasks) {
                    XSSFRow row = sheet.createRow(index++);
                    createCell(row, 0, cellStyle, format(schoolTask.getSchoolId()));
                    createCell(row, 1, cellStyle, format(schoolTask.getSchoolName()));
                    createCell(row, 2, cellStyle, format(schoolTask.getExecutorName()));
                    createCell(row, 3, cellStyle, formatTime(schoolTask.getFinishTime()));
                    createCell(row, 4, cellStyle, format(schoolTask.getExecuteNote()));
                }
            }
            return workbook;
        } catch (Exception e) {
            logger.error("exportSchoolTasks - Excp : {}; taskId = {}", e, taskId);
            return null;
        }
    }

    private XSSFCell createCell(XSSFRow row, int index, XSSFCellStyle style, String value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(value);
        return cell;
    }

    private XSSFCell cellValue(XSSFRow row, int index, String value) {
        XSSFCell cell = row.getCell(index);
        if (cell != null) {
            cell.setCellValue(value);
        }
        return cell;
    }

    private String formatDate(Date date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    private String formatTime(Date time) {
        return time == null ? "" : TIME_FORMAT.format(time);
    }

    private String format(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public List<AgentSchoolTask> loadSchoolTasks(String taskId) {
        return taskId == null ? null : agentSchoolTaskDao.findTaskIdIs(taskId);
    }

    public List<AgentTaskDetail> loadAgentTaskDeatil(String taskId) {
        return taskId == null ? null : agentTaskDetailDao.findByTaskId(taskId);
    }

    public AgentUser schoolAgentUser(School school) {
        List<AgentUser> agentUsers = baseOrgService.getSchoolManager(school.getId());
        if (CollectionUtils.isNotEmpty(agentUsers)) {
            return agentUsers.get(0);
        }
        return null;
    }

    public CrmUGCSchoolTask loadUGCSchoolTask(String id) {
        return id == null ? null : crmUGCSchoolTaskDao.load(id);
    }

    public CrmUGCSchoolTask dispatchUGCSchoolTask(Long schoolId, String creater, String createrName, Boolean branchSchool) {
        if (schoolId == null) {
            return null;
        }
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            logger.warn("Null school for schoolId = {}", schoolId);
            return null;
        }
        AgentUser agentUser = schoolAgentUser(school);
        if (agentUser == null) {
            logger.warn("None schoolAgentUser for schoolId = {}", schoolId);
            return null;
        }
        CrmUGCSchoolTask ugcSchoolTask = new CrmUGCSchoolTask();
        ugcSchoolTask.setSchoolId(schoolId);
        ugcSchoolTask.setSchoolName(school.getCname());
        ugcSchoolTask.setCreater(creater);
        ugcSchoolTask.setCreaterName(createrName);
        ugcSchoolTask.setExecutorId(agentUser.getId());
        ugcSchoolTask.setExecutorName(agentUser.getRealName());
        ugcSchoolTask.setBranchSchool(branchSchool);
        crmUGCSchoolTaskDao.insert(ugcSchoolTask);
        return ugcSchoolTask;
    }

    public boolean finishUGCSchoolTask(String taskId, Long schoolId) {
        CrmUGCSchoolTask ugcSchoolTask = loadUGCSchoolTask(taskId);
        if (ugcSchoolTask == null) {
            return false;
        }
        ugcSchoolTask.finish();
        crmUGCSchoolTaskDao.update(ugcSchoolTask.getId(), ugcSchoolTask);
        CrmUGCSchool ugcSchool = loadUGCSchool(schoolId);
        if (ugcSchool != null) {
            ugcSchool.finishTask();
            crmUGCSchoolDao.update(ugcSchool.getId(), ugcSchool);
        }
        return true;
    }

    public CrmUGCSchool loadUGCSchool(Long schoolId) {
        if (schoolId == null) {
            return null;
        }
        List<CrmUGCSchool> ugcSchools = crmUGCSchoolDao.findSchoolIdIs(schoolId);
        return CollectionUtils.isEmpty(ugcSchools) ? null : ugcSchools.get(0);
    }

    public String getAdminBaseUrl() {
        switch (RuntimeMode.current()) {
            case PRODUCTION:
                return "http://admin.17zuoye.net";
            case STAGING:
                return "http://admin.staging.17zuoye.net";
            case TEST:
                return "http://admin.test.17zuoye.net";
            case DEVELOPMENT:
                return "http://localhost:8082";
            default:
                return "http://admin.test.17zuoye.net";
        }
    }

    public AgentTask createTask2(Long createrId, String createrName, Long executorId, String executorName, String executorMobile, String title, String content, Date endTime, boolean needCustomerService, AgentTaskCategory category, List<Map<String, Object>> taskDetailDataList) {

        if (StringUtils.isBlank(title) || StringUtils.isBlank(content) || endTime == null || category == null || createrId == null || executorId == null) {
            return null;
        }
        long totalCount = 0;
        AgentTask task = saveTask(title, content, endTime, totalCount, needCustomerService, category, createrId, createrName);
        if (task == null) {
            return null;
        }

        // 创建子任务
        createTaskDetailList(task, taskDetailDataList, executorId, executorName, executorMobile);

        List<AgentTaskDetail> list = agentTaskDetailDao.findByTaskId(task.getId());
        if (CollectionUtils.isNotEmpty(list)) {
            totalCount = list.size();
        }
        if (totalCount > 0) {
            task = updateTaskTotalCount(task.getId(), totalCount);
        } else {
            task = updateTaskDisabled(task.getId(), true);
        }
        return task;
    }

    private void createTaskDetailList(AgentTask task, List<Map<String, Object>> taskDetailDataList, Long executorId, String executorName, String executorMobile) {
        if (CollectionUtils.isEmpty(taskDetailDataList)) {
            return;
        }
        List<AgentTaskDetail> taskDetailList = new ArrayList<>();
        for (Map<String, Object> taskDetailDataMap : taskDetailDataList) {
            AgentTaskDetail taskDetail = new AgentTaskDetail();
            taskDetail.setTaskId(task.getId());
            taskDetail.setTitle(task.getTitle());
            taskDetail.setCategory(task.getCategory());
            if (task.getCategory() == null) {
                continue;
            }
            // 设置数据
            boolean setDataFlag = false;
            if (task.getCategory() == AgentTaskCategory.TEACHER_CHANGE_SCHOOL) {
                setDataFlag = setTeacherChangeSchoolTaskDetailData(taskDetail, taskDetailDataMap);
            } else if (task.getCategory() == AgentTaskCategory.TEACHER_CREATE_CLAZZ) {
                setDataFlag = setTeacherCreateClazzTaskDetailData(taskDetail, taskDetailDataMap);
            } else if (task.getCategory() == AgentTaskCategory.TEACHER_BIND_MOBILE) {
                setDataFlag = setTeacherBindMobileTaskDetailData(taskDetail, taskDetailDataMap);
            } else if (task.getCategory() == AgentTaskCategory.TEACHER_GIFT) {
                setDataFlag = setTeacherGiftTaskDetailData(taskDetail, taskDetailDataMap);
            }

            if (!setDataFlag) {
                continue;
            }

            taskDetail.setExecutorId(executorId);
            taskDetail.setExecutorName(executorName);
            taskDetail.setExecutorMobile(executorMobile);

            taskDetail.setStatus(AgentTaskStatus.FOLLOWING);
            taskDetail.setEndTime(task.getEndTime());
            taskDetail.setNeedCustomerService(task.getNeedCustomerService());  //是否需要客服跟进
            taskDetail.setDisabled(false);
            taskDetailList.add(taskDetail);
        }

        agentTaskDetailDao.inserts(taskDetailList);
        if (task.getNeedCustomerService()) {
            List<AgentTaskDetail> agentTaskDetails = agentTaskDetailDao.findByTaskId(task.getId());
            executeRemoteRequest(agentTaskDetails);
        }


        int count = 0;
        List<AgentTaskDetail> list = agentTaskDetailDao.findByTaskId(task.getId());
        if (CollectionUtils.isNotEmpty(list)) {
            count = list.size();
        }
        String content = MessageFormat.format(TASK_DISPATCH_NOTICE, String.valueOf(taskDetailDataList.size()), String.valueOf(count));
        List<Long> receivers = Collections.singletonList(task.getCreaterId());
        agentNotifyService.sendNotify(TASK_NOTICE_TYPE, content, receivers);
    }

    public MapMessage createTeacherChangeSchoolTask(AuthCurrentUser currentUser, Long teacherId, Long targetSchoolId, boolean includeClazz, Set<Long> clazzIds, String comment, boolean needCustomerService) {
//        CrmTeacherSummary teacherSummary = crmTeacherSummaryDao.findByTeacherId(teacherId);

        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            MapMessage.errorMessage("老师不存在");
        }
        School targetSchool = schoolLoaderClient.getSchoolLoader()
                .loadSchool(targetSchoolId)
                .getUninterruptibly();
        if (targetSchool == null) {
            MapMessage.errorMessage("学校不存在");
        }
        List<AgentTaskDetail> taskDetails = agentTaskDetailDao.findByTeacherId(teacherId);
        boolean containsUnDealData = taskDetails.stream().anyMatch(item -> Objects.equals(item.getCategory(), AgentTaskCategory.TEACHER_CHANGE_SCHOOL) && !Objects.equals(item.getStatus(), AgentTaskStatus.FINISHED) && !item.isExpired());
        if (containsUnDealData) {
            return MapMessage.errorMessage("老师名下已有客服转校任务，请勿重复创建");
        }


        String title = AgentTaskCategory.TEACHER_CHANGE_SCHOOL.getValue();
        String content = teacher.fetchRealname() + "老师（" + teacher.getId() + "）" + (includeClazz ? "带班转校" : "不带班转校");
        Date endTime = DayUtils.addDay(new Date(), 2);

        List<Map<String, Object>> taskDetailDataList = new ArrayList<>();
        Map<String, Object> taskDetailDataMap = new HashMap<>();
        taskDetailDataMap.put("teacherId", teacherId);
        taskDetailDataMap.put("targetSchoolId", targetSchoolId);
        taskDetailDataMap.put("includeClazz", includeClazz);
        taskDetailDataMap.put("clazzIds", clazzIds);
        taskDetailDataMap.put("comment", comment);
        taskDetailDataList.add(taskDetailDataMap);
        createTask2(currentUser.getUserId(), currentUser.getRealName(), currentUser.getUserId(), currentUser.getRealName(), currentUser.getUserPhone(), title, content, endTime, needCustomerService, AgentTaskCategory.TEACHER_CHANGE_SCHOOL, taskDetailDataList);
        return MapMessage.successMessage();
    }

    private boolean setTeacherChangeSchoolTaskDetailData(AgentTaskDetail taskDetail, Map<String, Object> dataMap) {
        Long teacherId = (Long) dataMap.get("teacherId");
        Long targetSchoolId = (Long) dataMap.get("targetSchoolId");
        boolean includeClazz = (boolean) dataMap.get("includeClazz");
        Set<Long> clazzIds = (Set<Long>) dataMap.get("clazzIds");
        String comment = (String) dataMap.get("comment");

//        CrmTeacherSummary teacherSummary = crmTeacherSummaryDao.findByTeacherId(teacherId);
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return false;
        }

        School targetSchool = schoolLoaderClient.getSchoolLoader()
                .loadSchool(targetSchoolId)
                .getUninterruptibly();
        if (targetSchool == null) {
            return false;
        }
        StringBuilder content = new StringBuilder();
        content.append(teacher.fetchRealname()).append("老师（").append(teacher.getId()).append("）");
        content.append(includeClazz ? "带班转校" : "不带班转校").append("，");
        content.append("转入：").append(targetSchool.getCname()).append("（").append(targetSchool.getId()).append("）");
        if (includeClazz) {
            Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzsIncludeDisabled(clazzIds)
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));
            if (MapUtils.isNotEmpty(clazzMap)) {
                content.append("，所带班级：");
                List<String> clazzNameList = clazzMap.values().stream().map(p -> StringUtils.join(p.getClassName(), p.getId())).collect(Collectors.toList());
                content.append(StringUtils.join(clazzNameList, ","));
            }
        }
        if (StringUtils.isNotBlank(comment)) {
            content.append("，（备注：").append(comment).append("）");
        }
        taskDetail.setContent(content.toString());

        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        taskDetail.setSchoolId(school == null ? 0L : school.getId());
        taskDetail.setSchoolName(school == null ? "" : school.getCname());
        taskDetail.setTeacherId(teacher.getId());
        taskDetail.setTeacherName(teacher.fetchRealname());
        return true;
    }


    public void createTeacherCreateClazzTask(AuthCurrentUser currentUser, Long teacherId, String clazzNames, String comment, boolean needCustomerService) {
//        CrmTeacherSummary teacherSummary = crmTeacherSummaryDao.findByTeacherId(teacherId);
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return;
        }
        String title = AgentTaskCategory.TEACHER_CREATE_CLAZZ.getValue();
        String content = teacher.fetchRealname() + "（" + teacher.getId() + "）申请新建班级";
        Date endTime = DayUtils.addDay(new Date(), 2);

        List<Map<String, Object>> taskDetailDataList = new ArrayList<>();
        Map<String, Object> taskDetailDataMap = new HashMap<>();
        taskDetailDataMap.put("teacherId", teacherId);
        taskDetailDataMap.put("clazzNames", clazzNames);
        taskDetailDataMap.put("comment", comment);
        taskDetailDataList.add(taskDetailDataMap);
        createTask2(currentUser.getUserId(), currentUser.getRealName(), currentUser.getUserId(), currentUser.getRealName(), currentUser.getUserPhone(), title, content, endTime, needCustomerService, AgentTaskCategory.TEACHER_CREATE_CLAZZ, taskDetailDataList);
    }


    private boolean setTeacherCreateClazzTaskDetailData(AgentTaskDetail taskDetail, Map<String, Object> dataMap) {
        Long teacherId = (Long) dataMap.get("teacherId");
        String clazzNames = (String) dataMap.get("clazzNames");
        String comment = (String) dataMap.get("comment");

//        CrmTeacherSummary teacherSummary = crmTeacherSummaryDao.findByTeacherId(teacherId);
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return false;
        }
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        String schoolName = school == null ? "" : school.getCname();
        Long schoolId = school == null ? 0L : school.getId();

        StringBuilder content = new StringBuilder();
        content.append(teacher.fetchRealname()).append("老师（").append(teacher.getId()).append("），");
        content.append("所在学校：").append(schoolName).append("（").append(schoolId).append("）");
        if (StringUtils.isNotBlank(clazzNames)) {
            content.append("，申请新建班级：").append(clazzNames);
        }
        if (StringUtils.isNotBlank(comment)) {
            content.append("，（备注：").append(comment).append("）");
        }
        taskDetail.setContent(content.toString());

        taskDetail.setSchoolId(schoolId);
        taskDetail.setSchoolName(schoolName);
        taskDetail.setTeacherId(teacher.getId());
        taskDetail.setTeacherName(teacher.fetchRealname());
        return true;
    }


    public void createTeacherBindMobileTask(AuthCurrentUser currentUser, Long teacherId, String unbindMobile, String bindMobile, String comment, boolean needCustomerService) {
//        CrmTeacherSummary teacherSummary = crmTeacherSummaryDao.findByTeacherId(teacherId);
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return;
        }
        String title = AgentTaskCategory.TEACHER_BIND_MOBILE.getValue();
        String content = teacher.fetchRealname() + "（" + teacher.getId() + "）申请绑定/解绑手机";
        Date endTime = DayUtils.addDay(new Date(), 2);

        List<Map<String, Object>> taskDetailDataList = new ArrayList<>();
        Map<String, Object> taskDetailDataMap = new HashMap<>();
        taskDetailDataMap.put("teacherId", teacherId);
        taskDetailDataMap.put("unbindMobile", unbindMobile);
        taskDetailDataMap.put("bindMobile", bindMobile);
        taskDetailDataMap.put("comment", comment);
        taskDetailDataList.add(taskDetailDataMap);
        createTask2(currentUser.getUserId(), currentUser.getRealName(), currentUser.getUserId(), currentUser.getRealName(), currentUser.getUserPhone(), title, content, endTime, needCustomerService, AgentTaskCategory.TEACHER_BIND_MOBILE, taskDetailDataList);
    }


    private boolean setTeacherBindMobileTaskDetailData(AgentTaskDetail taskDetail, Map<String, Object> dataMap) {
        Long teacherId = (Long) dataMap.get("teacherId");
        String unbindMobile = (String) dataMap.get("unbindMobile");
        String bindMobile = (String) dataMap.get("bindMobile");
        String comment = (String) dataMap.get("comment");

//        CrmTeacherSummary teacherSummary = crmTeacherSummaryDao.findByTeacherId(teacherId);
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return false;
        }
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        String schoolName = school == null ? "" : school.getCname();
        Long schoolId = school == null ? 0L : school.getId();

        StringBuilder content = new StringBuilder();
        content.append(teacher.fetchRealname()).append("老师（").append(teacher.getId()).append("），");
        content.append("所在学校：").append(schoolName).append("（").append(schoolId).append("）");
        if (StringUtils.isNotBlank(unbindMobile)) {
            content.append("，解绑手机：").append(unbindMobile);
        }
        if (StringUtils.isNotBlank(bindMobile)) {
            content.append("，绑定手机：").append(bindMobile);
        }
        if (StringUtils.isNotBlank(comment)) {
            content.append("，（备注：").append(comment).append("）");
        }
        taskDetail.setContent(content.toString());

        taskDetail.setSchoolId(schoolId);
        taskDetail.setSchoolName(schoolName);
        taskDetail.setTeacherId(teacher.getId());
        taskDetail.setTeacherName(teacher.fetchRealname());
        return true;
    }

    public List<Map<String, Object>> setComposedDataWithTaskRecord(List<AgentTaskDetail> taskDetailList) {
        if (CollectionUtils.isEmpty(taskDetailList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> retList = new ArrayList<>();
        for (AgentTaskDetail taskDetail : taskDetailList) {
            Map<String, Object> composedMap = new HashMap<>();
            composedMap.put("taskDetail", taskDetail);
            List<CrmTaskRecord> crmTaskRecordList = crmTaskRecordDao.findByAgentTaskDetailId(taskDetail.getId());
            composedMap.put("taskRecord", crmTaskRecordList);
            retList.add(composedMap);
        }
        return retList;
    }

    public List<Map<String, Object>> setComposedDataWithTaskRecordNew(List<AgentTaskDetail> taskDetailList) {
        if (CollectionUtils.isEmpty(taskDetailList)) {
            return Collections.emptyList();
        }
        return getDetailList(taskDetailList);
    }

    public List<Map<String, Object>> getDetailList(List<AgentTaskDetail> taskDetailList) {
        List<Map<String, Object>> retList = new ArrayList<>();
        for (AgentTaskDetail taskDetail : taskDetailList) {
            Map<String, Object> composedMap = new HashMap<>();
            Map<String, Object> map = new HashMap<>();
            map.put("id", taskDetail.getId());
            map.put("taskId", taskDetail.getTaskId());
            map.put("title", taskDetail.getTitle());
            map.put("content", taskDetail.getContent());
            map.put("status", taskDetail.getStatus().getValue());
            map.put("category", taskDetail.getCategory().getValue());
            map.put("createTime", taskDetail.getCreateTime());
            map.put("expired", taskDetail.isExpired());
//            if(taskDetail.isExpired()){
//                map.put("status","已过期");
//            }
            composedMap.put("taskDetail", map);
            List<CrmTaskRecord> crmTaskRecordList = crmTaskRecordDao.findByAgentTaskDetailId(taskDetail.getId());
            composedMap.put("taskRecord", crmTaskRecordList);
            retList.add(composedMap);
        }
        return retList;
    }

    private boolean setTeacherGiftTaskDetailData(AgentTaskDetail taskDetail, Map<String, Object> dataMap) {
        Long teacherId = (Long) dataMap.get("teacherId");
        String giftId = (String) dataMap.get("giftId");
        String giftName = (String) dataMap.get("giftName");
        String giftPicUrl = (String) dataMap.get("giftPicUrl");
        if (StringUtils.isNotBlank(giftPicUrl)) {
            giftPicUrl = getPlatformDomain() + "/public/skin/project/ctepackage/gift/" + giftPicUrl;
        }
        String receiveTime = (String) dataMap.get("receiveTime");

        CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacherId);
        if (teacherSummary == null) {
            return false;
        }

        StringBuilder content = new StringBuilder();
        content.append(teacherSummary.getRealName()).append("老师（").append(teacherSummary.getTeacherId()).append("），所在学校：").append(teacherSummary.getSchoolName()).append("（").append(teacherSummary.getSchoolId()).append("）");
        content.append("，选择了老师专属礼包").append(giftName);
        content.append("，老师方便接收时间为").append(receiveTime);
        content.append("，请及时配送！");
        taskDetail.setContent(content.toString());
        taskDetail.setPicUrl(giftPicUrl);

        taskDetail.setSchoolId(teacherSummary.getSchoolId());
        taskDetail.setSchoolName(teacherSummary.getSchoolName());
        taskDetail.setTeacherId(teacherSummary.getTeacherId());
        taskDetail.setTeacherName(teacherSummary.getRealName());
        return true;
    }

    private String getPlatformDomain() {
        if (RuntimeMode.isDevelopment()) {
            return "http://cdn-cnc.test.17zuoye.net";
        } else if (RuntimeMode.isTest()) {
            return "http://cdn-cnc.test.17zuoye.net";
        } else if (RuntimeMode.isStaging()) {
            return "http://cdn-cnc.staging.17zuoye.net";
        } else if (RuntimeMode.isProduction()) {
            return "http://cdn-cnc.17zuoye.cn";
        }
        return "http://cdn-cnc.17zuoye.cn";
    }

}
