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

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.dao.CrmTaskDao;
import com.voxlearning.utopia.admin.dao.CrmTaskRecordDao;
import com.voxlearning.utopia.admin.dao.CrmTaskStubDao;
import com.voxlearning.utopia.admin.data.TaskRecordCounter;
import com.voxlearning.utopia.admin.data.TaskRecordReport;
import com.voxlearning.utopia.admin.persist.entity.AdminUser;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.admin.util.RedmineUtil;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.data.UserRecordSnapshot;
import com.voxlearning.utopia.entity.crm.CrmTask;
import com.voxlearning.utopia.entity.crm.CrmTaskRecord;
import com.voxlearning.utopia.entity.crm.CrmTaskStub;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmTeacherVisitInfo;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.client.AdminUserServiceClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;

/**
 * @author Jia HuanYin
 * @since 2015/10/19
 */
@Named
public class CrmTaskService extends AbstractAdminService {

    private static final String DEPT_CSPI = "CSPI"; // 客服一线
    private static final String DEPT_CSPIS = "CSPIS"; // 客服现场
    private static final String DEPT_CSPO = "CSPO"; // 客服外呼
    private static final String DEPT_CSPIA = "CSPIA"; // 客服呼入管理
    private static final String DEPT_CSPOA = "CSPOA"; // 客服呼出管理

    private static final String STUB_CONTENT = "截止时间：{0}→{1}；任务状态：{2}；任务内容：{3}";

    @Inject
    private CrmTaskDao crmTaskDao;
    @Inject
    private CrmTaskStubDao crmTaskStubDao;
    @Inject
    private CrmTaskRecordDao crmTaskRecordDao;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private CrmSummaryServiceClient crmSummaryServiceClient;

    @Inject private AdminUserServiceClient adminUserServiceClient;
    @Inject private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;

    public Map<String, String> taskUsers(AuthCurrentAdminUser adminUser) {
        Map<String, String> taskUsers = new LinkedHashMap<>();
        taskUsers.put(adminUser.getAdminUserName(), adminUser.getRealName());
        String department = adminUser.getDepartmentName();
        if (StringUtils.isBlank(department)) {
            return taskUsers;
        }
        Set<AdminUser> users = new LinkedHashSet<>();
        switch (department) {
            case DEPT_CSPI:
            case DEPT_CSPIS:
                users.addAll(departmentUsers(DEPT_CSPIS));
                users.addAll(departmentUsers(DEPT_CSPI));
                users.addAll(departmentUsers(DEPT_CSPIA));
                break;
            case DEPT_CSPIA:
                users.addAll(departmentUsers(DEPT_CSPIA));
                users.addAll(departmentUsers(DEPT_CSPI));
                users.addAll(departmentUsers(DEPT_CSPIS));
                users.addAll(departmentUsers(DEPT_CSPOA));
                users.addAll(departmentUsers(DEPT_CSPO));
                break;
            case DEPT_CSPO:
                users.addAll(departmentUsers(DEPT_CSPO));
                users.addAll(departmentUsers(DEPT_CSPOA));
                break;
            case DEPT_CSPOA:
                users.addAll(departmentUsers(DEPT_CSPOA));
                users.addAll(departmentUsers(DEPT_CSPO));
                users.addAll(departmentUsers(DEPT_CSPIA));
                users.addAll(departmentUsers(DEPT_CSPI));
                users.addAll(departmentUsers(DEPT_CSPIS));
                break;
            default:
                users.addAll(departmentUsers(DEPT_CSPI));
                break;
        }
        for (AdminUser user : users) {
            String userName = user.getAdminUserName();
            if (!taskUsers.containsKey(userName)) {
                taskUsers.put(userName, user.getRealName());
            }
        }
        return taskUsers;
    }

    public Map<String, String> allTaskUsers(AuthCurrentAdminUser adminUser) {
        Map<String, String> taskUsers = new LinkedHashMap<>();
        taskUsers.put(adminUser.getAdminUserName(), adminUser.getRealName());
        String department = adminUser.getDepartmentName();
        if (StringUtils.isBlank(department)) {
            return taskUsers;
        }
        Set<AdminUser> users = new LinkedHashSet<>();
        users.addAll(departmentUsers(DEPT_CSPI));
        users.addAll(departmentUsers(DEPT_CSPIS));
        users.addAll(departmentUsers(DEPT_CSPIA));
        users.addAll(departmentUsers(DEPT_CSPO));
        users.addAll(departmentUsers(DEPT_CSPOA));
        for (AdminUser user : users) {
            String userName = user.getAdminUserName();
            if (!taskUsers.containsKey(userName)) {
                taskUsers.put(userName, user.getRealName());
            }
        }
        return taskUsers;
    }

    private List<AdminUser> departmentUsers(String department) {
        List<AdminUser> users = adminUserServiceClient.getAdminUserService()
                .findAdminUsersByDepartmentName(department)
                .getUninterruptibly();
        return CollectionUtils.isEmpty(users) ? Collections.emptyList() : users;
    }

    public static List<CrmTaskType> taskTypes(AuthCurrentAdminUser adminUser) {
        String department = adminUser.getDepartmentName();
        if (StringUtils.isBlank(department)) {
            return Collections.emptyList();
        }
        switch (department) {
            case DEPT_CSPI:
                return Arrays.asList(CrmTaskType.退款, CrmTaskType.远程协助, CrmTaskType.客户回馈, CrmTaskType.投诉回电, CrmTaskType.电话回访);
            case DEPT_CSPIS:
                return Arrays.asList(CrmTaskType.退款, CrmTaskType.远程协助, CrmTaskType.客户回馈, CrmTaskType.投诉回电, CrmTaskType.电话回访,
                        CrmTaskType.产品BUG, CrmTaskType.老师转校, CrmTaskType.老师新建班级, CrmTaskType.老师手机绑定解绑);
            case DEPT_CSPIA:
                return Arrays.asList(CrmTaskType.退款, CrmTaskType.远程协助, CrmTaskType.客户回馈, CrmTaskType.投诉回电, CrmTaskType.电话回访,
                        CrmTaskType.产品BUG, CrmTaskType.未呼通需跟进, CrmTaskType.呼通重点跟进, CrmTaskType.呼通一般, CrmTaskType.预约);
            case DEPT_CSPO:
                return Arrays.asList(CrmTaskType.未呼通需跟进, CrmTaskType.呼通重点跟进, CrmTaskType.呼通一般, CrmTaskType.预约, CrmTaskType.电话回访, CrmTaskType.回访流失老师);
            case DEPT_CSPOA:
                return Arrays.asList(CrmTaskType.未呼通需跟进, CrmTaskType.呼通重点跟进, CrmTaskType.呼通一般, CrmTaskType.预约, CrmTaskType.退款,
                        CrmTaskType.远程协助, CrmTaskType.客户回馈, CrmTaskType.投诉回电, CrmTaskType.电话回访, CrmTaskType.回访流失老师);
            default:
                return Arrays.asList(CrmTaskType.退款, CrmTaskType.远程协助, CrmTaskType.客户回馈, CrmTaskType.投诉回电, CrmTaskType.电话回访);
        }
    }

    public static List<CrmContactType> contactTypes(AuthCurrentAdminUser adminUser) {
        String department = adminUser.getDepartmentName();
        if (StringUtils.isBlank(department)) {
            return Collections.emptyList();
        }
        switch (department) {
            case DEPT_CSPI:
            case DEPT_CSPIS:
                return Arrays.asList(CrmContactType.电话呼入, CrmContactType.在线咨询, CrmContactType.微信咨询, CrmContactType.其他, CrmContactType.电话呼出);
            case DEPT_CSPIA:
                return Arrays.asList(CrmContactType.电话呼入, CrmContactType.在线咨询, CrmContactType.微信咨询, CrmContactType.其他, CrmContactType.电话呼出);
            case DEPT_CSPO:
                return Collections.singletonList(CrmContactType.电话呼出);
            case DEPT_CSPOA:
                return Arrays.asList(CrmContactType.电话呼出, CrmContactType.电话呼入, CrmContactType.在线咨询, CrmContactType.微信咨询, CrmContactType.其他);
            default:
                return Arrays.asList(CrmContactType.电话呼入, CrmContactType.在线咨询, CrmContactType.微信咨询, CrmContactType.其他);
        }
    }

    public List<CrmTask> addTasks(AuthCurrentAdminUser adminUser, String executor, CrmTaskType type, Date endTime, String title, String content, Collection<Long> userIds, CrmTaskAction action, String source) {
        if (adminUser == null || StringUtils.isBlank(executor) || type == null || endTime == null || StringUtils.isBlank(content) || CollectionUtils.isEmpty(userIds)) {
            return null;
        }
        List<CrmTask> tasks = new ArrayList<>();
        String creator = adminUser.getAdminUserName();
        String creatorName = adminUser.getRealName();
        AdminUser iExecutor = adminUserServiceClient.getAdminUserService()
                .loadAdminUser(executor)
                .getUninterruptibly();
        String executorName = iExecutor == null ? null : iExecutor.getRealName();
        for (Long userId : userIds) {
            CrmTask task = saveTask(creator, creatorName, executor, executorName, userId, type, endTime, title, content, "", "", "");
            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(userId);
            userServiceRecord.setOperatorId(creator);
            userServiceRecord.setOperationType(UserServiceRecordOperationType.新建任务.name());
            userServiceRecord.setOperationContent("新建任务");
            userServiceRecord.setComments("任务类型[" + type + "]，标题[" + title + "]，内容[" + content + "]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            tasks.add(task);
            smartTaskAddStub(action, source, task);
        }
        if (action != null && action == CrmTaskAction.RECORD_NEW) {
            bindTaskRecord(source, tasks);
        }
        return tasks;
    }

    private CrmTask saveTask(String creator, String creatorName, String executor, String executorName, Long userId, CrmTaskType type, Date endTime, String title, String content, String agentTaskId, String applicantName, String applicantMobile) {
        CrmTask task = createCrmTask(creator, creatorName, executor, executorName, userId, type, endTime, title, content, agentTaskId, applicantName, applicantMobile);
        crmTaskDao.insert(task);
        return task;
    }

    private CrmTask createCrmTask(String creator, String creatorName, String executor, String executorName, Long userId, CrmTaskType type, Date endTime, String title, String content, String agentTaskId, String applicantName, String applicantMobile) {
        CrmTask task = new CrmTask();
        task.setCreator(creator);
        task.setCreatorName(creatorName);
        task.setExecutor(executor);
        task.setExecutorName(executorName);
        task.setUserId(userId);
        User user = userLoaderClient.loadUser(userId);
        if (user != null) {
            task.setUserName(user.fetchRealname());
            task.setUserType(user.fetchUserType());
        }
        task.setType(type);
        task.setEndTime(DateUtils.truncate(endTime, Calendar.DATE));
        task.setTitle(title);
        task.setContent(content);
        task.setStatus(CrmTaskStatus.NEW);
        if (StringUtils.isNotEmpty(agentTaskId)) {
            task.setAgentTaskId(agentTaskId);
        }
        task.setDisabled(false);
        if (StringUtils.isNotEmpty(applicantName)) {
            task.setApplicantName(applicantName);
        }
        if (StringUtils.isNotEmpty(applicantMobile)) {
            task.setApplicantMobile(applicantMobile);
        }
        return task;
    }

    private CrmTaskStub smartTaskAddStub(CrmTaskAction action, String source, CrmTask task) {
        Date actionTime = new Date();
        if (action == null) {
            return null;
        }
        String taskId = task.getId();
        String title;
        String content;
        switch (action) {
            case TASK_NEW:
                title = "[任务新建]";
                content = task.getContent();
                break;
            case TASK_FORWARD:
                title = "[任务转发]";
                content = task.getContent();
                break;
            case RECORD_NEW:
                CrmTaskRecord sourceRecord = loadTaskRecord(source);
                if (sourceRecord == null) {
                    return null;
                }
                title = stubTitle(sourceRecord.getThirdCategory());
                content = sourceRecord.getContent();
                break;
            default:
                return null;

        }
        return saveTaskStub(taskId, title, content, actionTime);
    }

    private static String stubTitle(CrmTaskRecordCategory recordCategory) {
        return "[" + (recordCategory == null ? "无记录分类" : recordCategory.name()) + "]";
    }

    private CrmTaskRecord bindTaskRecord(String taskRecordId, List<CrmTask> tasks) {
        if (StringUtils.isBlank(taskRecordId) || CollectionUtils.isEmpty(tasks)) {
            return null;
        }
        return crmTaskRecordDao.updateTaskId(taskRecordId, tasks.get(0).getId());
    }

    public Page<CrmTask> loadTasks(Date createStart, Date createEnd, Date endStart, Date endEnd, Date finishStart, Date finishEnd, String creator, String executor, UserType userType, CrmTaskType type, CrmTaskStatus status, Pageable pageable, AuthCurrentAdminUser adminUser) {
        createStart = createStart == null ? null : DateUtils.truncate(createStart, Calendar.DATE);
        createEnd = createEnd == null ? null : DateUtils.ceiling(createEnd, Calendar.DATE);
        endStart = endStart == null ? null : DateUtils.truncate(endStart, Calendar.DATE);
        endEnd = endEnd == null ? null : DateUtils.ceiling(endEnd, Calendar.DATE);
        finishStart = finishStart == null ? null : DateUtils.truncate(finishStart, Calendar.DATE);
        finishEnd = finishEnd == null ? null : DateUtils.ceiling(finishEnd, Calendar.DATE);
        StatusStartEnd statusStartEnd = new StatusStartEnd(status, endStart, endEnd);
        status = statusStartEnd.status;
        endStart = statusStartEnd.start;
        endEnd = statusStartEnd.end;
        Set<String> creators = smartTaskUsers(creator, adminUser);
        Set<String> executors = smartTaskUsers(executor, adminUser);
        Set<CrmTaskType> types = smartTaskTypes(type, adminUser);
        Page<CrmTask> tasks = crmTaskDao.smartFind(createStart, createEnd, endStart, endEnd, finishStart, finishEnd, creators, executors, userType, types, status, pageable);
        if (tasks != null && tasks.hasContent()) {
            tasks.forEach(this::niceTask);
        }
        return tasks;
    }

    private Set<String> smartTaskUsers(String user, AuthCurrentAdminUser adminUser) {
        return StringUtils.isNotBlank(user) ? Collections.singleton(user) : taskUsers(adminUser).keySet();
    }

    private Set<CrmTaskType> smartTaskTypes(CrmTaskType taskType, AuthCurrentAdminUser adminUser) {
        return taskType != null ? Collections.singleton(taskType) : new HashSet<>(taskTypes(adminUser));
    }

    public Page<CrmTask> loadUserTasks(Long userId, String creator, String executor, CrmTaskStatus status, Pageable pageable) {
        if (userId == null) {
            return null;
        }
        StatusStartEnd statusStartEnd = new StatusStartEnd(status, null, null);
        status = statusStartEnd.status;
        Date endStart = statusStartEnd.start;
        Date endEnd = statusStartEnd.end;
        Page<CrmTask> tasks = crmTaskDao.smartFind(endStart, endEnd, creator, executor, userId, status, pageable);
        if (tasks != null && tasks.hasContent()) {
            tasks.forEach(this::niceTask);
        }
        return tasks;
    }

    public CrmTask loadTask(String taskId) {
        return StringUtils.isBlank(taskId) ? null : crmTaskDao.load(taskId);
    }

    public CrmTask taskSnapshot(String taskId) {
        CrmTask task = loadTask(taskId);
        if (task == null) {
            return null;
        }
        task.setNiceEndTime(niceDate(task.getEndTime()));
        task.setNiceCreateTime(niceDate(task.getCreateTime()));
        return task;
    }

    public CrmTask loadTaskDetail(String taskId) {
        CrmTask task = loadTask(taskId);
        if (task == null) {
            return null;
        }
        niceTask(task);
        List<CrmTaskStub> taskStubs = crmTaskStubDao.findByTaskId(taskId);
        task.setTaskStubs(niceTaskStubs(taskStubs));
        return task;
    }

    private void niceTask(CrmTask task) {
        task.setNiceStatus(niceStatus(task));
        task.setNiceEndTime(niceDate(task.getEndTime()));
        task.setNiceCreateTime(niceDate(task.getCreateTime()));
        User user = userLoaderClient.loadUser(task.getUserId());
        if (user != null) {
            task.setUserAuthStatus(user.fetchCertificationState());
        }
    }

    private static String niceStatus(CrmTask task) {
        Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        CrmTaskStatus status = task.getStatus();
        Date endTime = task.getEndTime();
        CrmTaskStatus niceStatus = status;
        if (status != null && endTime != null) {
            boolean end = endTime.before(today);
            switch (status) {
                case NEW:
                    niceStatus = end ? CrmTaskStatus.UNFOLLOW : CrmTaskStatus.NEW;
                    break;
                case FOLLOWING:
                    niceStatus = end ? CrmTaskStatus.UNFINISH : CrmTaskStatus.FOLLOWING;
                    break;
                default:
                    break;
            }
        }
        return niceStatus == null ? null : niceStatus.value;
    }

    private static String niceDate(Date date) {
        return date == null ? null : ISO_DATE_FORMAT.format(date);
    }

    private static String niceDateTime(Date date, String pattern) {
        return date == null ? null : FastDateFormat.getInstance(pattern).format(date);
    }

    private static List<CrmTaskStub> niceTaskStubs(List<CrmTaskStub> taskStubs) {
        if (CollectionUtils.isNotEmpty(taskStubs)) {
            taskStubs.forEach(stub -> stub.setNiceActionTime(niceDateTime(stub.getActionTime(), "MM-dd HH:mm")));
        }
        return taskStubs;
    }

    public CrmTask finishTask(String taskDetailId) {
        CrmTask task = updateTaskStatus(taskDetailId, CrmTaskStatus.FINISHED);
//        if(task != null && task.getType() == CrmTaskType.回访流失老师){
//            //任务结束时，结束全部agentTaskId相同的任务
//            String agentTaskId = task.getAgentTaskId();
//            finishByAgentTaskId(agentTaskId);
//        }
        return task;
    }

    public MapMessage finishTasksBytaskDetailId(List<String> taskDetailId) {
        if (CollectionUtils.isEmpty(taskDetailId)) {
            return MapMessage.errorMessage("任务删除失败，未找到对应的外呼任务");
        }
        return crmTaskDao.disabledCrmTask(taskDetailId);
    }

    public CrmTask followTask(String taskId) {
        return updateTaskStatus(taskId, CrmTaskStatus.FOLLOWING);
    }

    private CrmTask updateTaskStatus(String taskId, CrmTaskStatus status) {
        Date actionTime = new Date();
        if (StringUtils.isBlank(taskId) || status == null) {
            return null;
        }
        CrmTask task = crmTaskDao.load(taskId);
        if (task == null) {
            return null;
        }
        task.setStatus(status);
        CrmTask iTask = crmTaskDao.update(taskId, task);
        if (iTask != null) {
            String stubTitle = "[任务修改]";
            String stubContent = "任务状态：" + status.value;
            saveTaskStub(taskId, stubTitle, stubContent, actionTime);
        }
        return iTask;
    }

    public CrmTask updateTask(String taskId, Date endTime, String content, CrmTaskStatus status) {
        Date actionTime = new Date();
        if (StringUtils.isBlank(taskId) || endTime == null || status == null) {
            return null;
        }
        CrmTask task = crmTaskDao.load(taskId);
        if (task == null) {
            return null;
        }
        String iEndTime = ISO_DATE_FORMAT.format(task.getEndTime());
        String iContent = task.getContent();
        task.setEndTime(endTime);
        task.setContent(content);
        task.setStatus(status);
        CrmTask iTask = crmTaskDao.update(taskId, task);
        if (iTask != null) {
            String stubTitle = "[任务修改]";
            String stubContent = MessageFormat.format(STUB_CONTENT, iEndTime, ISO_DATE_FORMAT.format(endTime), status.value, iContent);
            saveTaskStub(taskId, stubTitle, stubContent, actionTime);
        }
        return iTask;
    }

    private CrmTaskStub saveTaskStub(String taskId, String title, String content, Date actionTime) {
        CrmTaskStub taskStub = new CrmTaskStub(taskId, title, content, actionTime);
        crmTaskStubDao.insert(taskStub);
        return taskStub;
    }

    public int countUserFollowingTask(Long userId) {
        if (userId == null) {
            return 0;
        }
        Date endStart = DateUtils.truncate(new Date(), Calendar.DATE);
        Collection<CrmTaskStatus> statuses = Arrays.asList(CrmTaskStatus.NEW, CrmTaskStatus.FOLLOWING);
        long count = crmTaskDao.smartCount(userId, endStart, null, statuses);
        return count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
    }

    public static List<CrmTaskRecordCategory> taskRecordCategories(AuthCurrentAdminUser adminUser) {
        String department = adminUser.getDepartmentName();
        if (StringUtils.isBlank(department)) {
            return Collections.emptyList();
        }
        List<CrmTaskRecordCategory> taskRecordCategories = new ArrayList<>();
        taskRecordCategories.addAll(teacherTaskRecordCategories(adminUser));
        if (!DEPT_CSPO.equals(department)) {
            taskRecordCategories.addAll(studentTaskRecordCategories());
        }
        return taskRecordCategories;
    }

    public static String taskRecordCategoryJson(AuthCurrentAdminUser adminUser) {
        List<CrmTaskRecordCategory> taskRecordCategories = taskRecordCategories(adminUser);
        if (CollectionUtils.isEmpty(taskRecordCategories)) {
            return JsonUtils.EMPTY_JSON_OBJECT;
        }
        Map<CrmTaskRecordCategory, Map<CrmTaskRecordCategory, Set<CrmTaskRecordCategory>>> buffer = new LinkedHashMap<>();
        for (CrmTaskRecordCategory category : taskRecordCategories) {
            buffer.put(category, CrmTaskRecordCategory.TREE.get(category));
        }
        return JsonUtils.toJson(buffer);
    }

    public static String taskRecordCategoryJson(UserType userType, AuthCurrentAdminUser adminUser) {
        List<CrmTaskRecordCategory> taskRecordCategories;
        if (UserType.TEACHER == userType) {
            taskRecordCategories = teacherTaskRecordCategories(adminUser);
        } else if (UserType.STUDENT == userType) {
            taskRecordCategories = studentTaskRecordCategories();
        } else {
            taskRecordCategories = taskRecordCategories(adminUser);
        }
        if (CollectionUtils.isEmpty(taskRecordCategories)) {
            return JsonUtils.EMPTY_JSON_OBJECT;
        }
        Map<CrmTaskRecordCategory, Map<CrmTaskRecordCategory, Set<CrmTaskRecordCategory>>> buffer = new LinkedHashMap<>();
        for (CrmTaskRecordCategory category : taskRecordCategories) {
            buffer.put(category, CrmTaskRecordCategory.TREE.get(category));
        }
        return JsonUtils.toJson(buffer);
    }

    public static List<CrmTaskRecordCategory> teacherTaskRecordCategories(AuthCurrentAdminUser adminUser) {
        String department = adminUser.getDepartmentName();
        if (StringUtils.isBlank(department)) {
            return Collections.emptyList();
        }
        switch (department) {
            case DEPT_CSPI:
            case DEPT_CSPIS:
                return Arrays.asList(CrmTaskRecordCategory.认证问题, CrmTaskRecordCategory.班级or学生管理问题, CrmTaskRecordCategory.作业or检测or教材问题,
                        CrmTaskRecordCategory.账号or密码or绑定问题, CrmTaskRecordCategory.园丁豆or学豆问题, CrmTaskRecordCategory.奖品中心问题,
                        CrmTaskRecordCategory.活动问题, CrmTaskRecordCategory.其余问题, CrmTaskRecordCategory.无效问题);
            case DEPT_CSPIA:
                return Arrays.asList(CrmTaskRecordCategory.认证问题, CrmTaskRecordCategory.班级or学生管理问题, CrmTaskRecordCategory.作业or检测or教材问题,
                        CrmTaskRecordCategory.账号or密码or绑定问题, CrmTaskRecordCategory.园丁豆or学豆问题, CrmTaskRecordCategory.奖品中心问题,
                        CrmTaskRecordCategory.活动问题, CrmTaskRecordCategory.其余问题, CrmTaskRecordCategory.无效问题, CrmTaskRecordCategory.接通,
                        CrmTaskRecordCategory.未接通);
            case DEPT_CSPO:
                return Arrays.asList(CrmTaskRecordCategory.未接通, CrmTaskRecordCategory.接通, CrmTaskRecordCategory.不回流老师原因);
            case DEPT_CSPOA:
                return Arrays.asList(CrmTaskRecordCategory.未接通, CrmTaskRecordCategory.接通, CrmTaskRecordCategory.认证问题,
                        CrmTaskRecordCategory.班级or学生管理问题, CrmTaskRecordCategory.作业or检测or教材问题, CrmTaskRecordCategory.账号or密码or绑定问题,
                        CrmTaskRecordCategory.园丁豆or学豆问题, CrmTaskRecordCategory.奖品中心问题, CrmTaskRecordCategory.活动问题,
                        CrmTaskRecordCategory.其余问题, CrmTaskRecordCategory.无效问题, CrmTaskRecordCategory.不回流老师原因);
            default:
                return Arrays.asList(CrmTaskRecordCategory.认证问题, CrmTaskRecordCategory.班级or学生管理问题, CrmTaskRecordCategory.作业or检测or教材问题,
                        CrmTaskRecordCategory.账号or密码or绑定问题, CrmTaskRecordCategory.园丁豆or学豆问题, CrmTaskRecordCategory.奖品中心问题,
                        CrmTaskRecordCategory.活动问题, CrmTaskRecordCategory.其余问题, CrmTaskRecordCategory.无效问题);
        }
    }

    public static String teacherTaskRecordCategoryJson(AuthCurrentAdminUser adminUser) {
        List<CrmTaskRecordCategory> taskRecordCategories = teacherTaskRecordCategories(adminUser);
        if (CollectionUtils.isEmpty(taskRecordCategories)) {
            return JsonUtils.EMPTY_JSON_OBJECT;
        }
        Map<CrmTaskRecordCategory, Map<CrmTaskRecordCategory, Set<CrmTaskRecordCategory>>> buffer = new LinkedHashMap<>();
        for (CrmTaskRecordCategory category : taskRecordCategories) {
            buffer.put(category, CrmTaskRecordCategory.TREE.get(category));
        }
        return JsonUtils.toJson(buffer);
    }

    public static List<CrmTaskRecordCategory> studentTaskRecordCategories() {
        return Arrays.asList(CrmTaskRecordCategory.学生_个人中心, CrmTaskRecordCategory.学生_学习中心, CrmTaskRecordCategory.学生_活动,
                CrmTaskRecordCategory.学生_奖品中心, CrmTaskRecordCategory.学生_学生APP, CrmTaskRecordCategory.学生_课外乐园问题,
                CrmTaskRecordCategory.学生_无效电话, CrmTaskRecordCategory.学生_家长通APP, CrmTaskRecordCategory.学生_其他);
    }

    public static String studentTaskRecordCategoryJson() {
        List<CrmTaskRecordCategory> taskRecordCategories = studentTaskRecordCategories();
        if (CollectionUtils.isEmpty(taskRecordCategories)) {
            return JsonUtils.EMPTY_JSON_OBJECT;
        }
        Map<CrmTaskRecordCategory, Map<CrmTaskRecordCategory, Set<CrmTaskRecordCategory>>> buffer = new LinkedHashMap<>();
        for (CrmTaskRecordCategory category : taskRecordCategories) {
            buffer.put(category, CrmTaskRecordCategory.TREE.get(category));
        }
        return JsonUtils.toJson(buffer);
    }

    public static boolean isPhoneOut(AuthCurrentAdminUser adminUser) {
        String department = adminUser.getDepartmentName();
        if (StringUtils.isBlank(department)) {
            return false;
        }
        switch (department) {
            case DEPT_CSPO:
            case DEPT_CSPOA:
                return true;
            default:
                return false;
        }
    }

    public CrmTaskRecord addTaskRecord(AuthCurrentAdminUser adminUser, String taskId, Long userId, CrmTaskRecordCategory recordCategory, CrmContactType contactType, String content, Integer redmineAssigned) {
        Date actionTime = new Date();
        if (adminUser == null || userId == null || recordCategory == null || contactType == null || StringUtils.isBlank(content)) {
            return null;
        }
        String recorder = adminUser.getAdminUserName();
        String recorderName = adminUser.getRealName();
        String agentTaskId = "";
        CrmTask crmTask = crmTaskDao.load(taskId);
        if (crmTask != null) {
            agentTaskId = crmTask.getAgentTaskId();
        }
        CrmTaskRecord taskRecord = saveTaskRecord(taskId, recorder, recorderName, userId, recordCategory, contactType, null, content, null, null, agentTaskId);
        if (loadTask(taskId) != null) {
            String stubTitle = stubTitle(recordCategory);
            saveTaskStub(taskId, stubTitle, content, actionTime);
        }
        if (UserType.TEACHER == taskRecord.getUserType()) {
            User user = userLoaderClient.loadUser(userId);
            if (user != null) {
                UserServiceRecordOperationType userServiceRecordOperationType = contactType == CrmContactType.电话呼出 ? UserServiceRecordOperationType.客服外呼 : UserServiceRecordOperationType.用户咨询;
                UserServiceRecord userRecord = new UserServiceRecord();
                userRecord.setUserId(userId);
                userRecord.setUserName(user.fetchRealname());
                userRecord.setOperatorId(recorder);
                userRecord.setOperatorName(recorderName);
                userRecord.setOperationType(userServiceRecordOperationType.name());
                if (userServiceRecordOperationType == UserServiceRecordOperationType.客服外呼) {
                    userRecord.setOperationContent(crmTask == null ? "客服建立新记录" : crmTask.getTitle());
                    userRecord.setComments(recorderName + "外呼," + content);
                }
                if (userServiceRecordOperationType == UserServiceRecordOperationType.用户咨询) {
                    userRecord.setOperationContent(contactType.name());
                    userRecord.setComments(recorderName + "受理咨询," + content);
                }
                userRecord.setAdditions("CrmTaskRecord:" + taskRecord.getId());
                userServiceClient.saveUserServiceRecord(userRecord);
            }
        }
        if (redmineAssigned != null) {
            createRedmine(adminUser, taskRecord, redmineAssigned);
        }
        return taskRecord;
    }

    private CrmTaskRecord saveTaskRecord(String taskId, String recorder, String recorderName, Long userId, CrmTaskRecordCategory recordCategory, CrmContactType contactType, String title, String content, Integer callTime, String audioUrl, String agentTaskId) {
        CrmTaskRecord taskRecord = new CrmTaskRecord();
        taskRecord.setTaskId(taskId);
        taskRecord.setRecorder(recorder);
        taskRecord.setRecorderName(recorderName);
        taskRecord.setUserId(userId);
        User user = userLoaderClient.loadUser(userId);
        if (user != null) {
            taskRecord.setUserName(user.fetchRealname());
            taskRecord.setUserType(user.fetchUserType());
        }
        taskRecord.touchCategory(recordCategory);
        taskRecord.setContactType(contactType);
        taskRecord.setTitle(title);
        taskRecord.setContent(content);
        taskRecord.setCallTime(callTime);
        taskRecord.setAudioUrl(audioUrl);
        if (StringUtils.isNotEmpty(agentTaskId)) {
            taskRecord.setAgentTaskId(agentTaskId);
        }
        crmTaskRecordDao.insert(taskRecord);
        return taskRecord;
    }

    private void createRedmine(AuthCurrentAdminUser adminUser, CrmTaskRecord taskRecord, Integer redmineAssigned) {
        String redmineKey = adminUser.getRedmineApikey();
        String subject = StringUtils.join("【From: 客服工作记录】", taskRecord.getThirdCategory());
        String recorder = StringUtils.join("【记录人】", taskRecord.getRecorderName());
        String category = StringUtils.join("【记录分类】", taskRecord.getFirstCategory(), "→", taskRecord.getSecondCategory(), "→", taskRecord.getThirdCategory());
        String userType = taskRecord.getUserType() == null ? "" : taskRecord.getUserType().getDescription();
        String user = StringUtils.join("【用户信息】", "ID:", taskRecord.getUserId(), " 姓名:", taskRecord.getUserName(), " 角色:", userType);
        String content = StringUtils.join("【记录内容】", "\n", taskRecord.getContent());
        String description = StringUtils.join(recorder, "\n\n", category, "\n\n", user, "\n\n", content);
        RedmineUtil.createIssue(redmineKey, subject, description, redmineAssigned, RedmineUtil.Priority.普通, RedmineUtil.Tracker.Feedback);
    }

    public CrmTaskRecord loadTaskRecord(String taskRecordId) {
        return StringUtils.isBlank(taskRecordId) ? null : crmTaskRecordDao.load(taskRecordId);
    }

    public Page<CrmTaskRecord> loadTaskRecords(Date createStart, Date createEnd, String recorder, Set<CrmContactType> contactTypes, CrmTaskRecordCategory firstCategory, CrmTaskRecordCategory secondCategory, CrmTaskRecordCategory thirdCategory, UserType userType, Pageable pageable, AuthCurrentAdminUser adminUser) {
        createStart = createStart == null ? null : DateUtils.truncate(createStart, Calendar.DATE);
        createEnd = createEnd == null ? null : DateUtils.ceiling(createEnd, Calendar.DATE);
        Set<String> recorders = smartTaskUsers(recorder, adminUser);
        contactTypes = smartContactTypes(contactTypes, adminUser);
        Set<CrmTaskRecordCategory> firstCategories = smartFirstCategories(firstCategory, adminUser);
        return crmTaskRecordDao.smartFind(createStart, createEnd, recorders, contactTypes, firstCategories, secondCategory, thirdCategory, userType, pageable);
    }

    public List<CrmTaskRecord> loadAllTaskRecords(Date createStart, Date createEnd, String recorder, Set<CrmContactType> contactTypes, CrmTaskRecordCategory firstCategory, CrmTaskRecordCategory secondCategory, CrmTaskRecordCategory thirdCategory, UserType userType, AuthCurrentAdminUser adminUser) {
        createStart = createStart == null ? null : DateUtils.truncate(createStart, Calendar.DATE);
        createEnd = createEnd == null ? null : DateUtils.ceiling(createEnd, Calendar.DATE);
        Set<String> recorders = smartTaskUsers(recorder, adminUser);
        contactTypes = smartContactTypes(contactTypes, adminUser);
        Set<CrmTaskRecordCategory> firstCategories = smartFirstCategories(firstCategory, adminUser);
        return crmTaskRecordDao.smartFindAll(createStart, createEnd, recorders, contactTypes, firstCategories, secondCategory, thirdCategory, userType);
    }

    public TaskRecordReport taskRecordReport(Date createStart, Date createEnd, Set<CrmContactType> contactTypes, CrmTaskRecordCategory firstCategory, CrmTaskRecordCategory secondCategory, CrmTaskRecordCategory thirdCategory, UserType userType, AuthCurrentAdminUser adminUser) {
        createStart = createStart == null ? null : DateUtils.truncate(createStart, Calendar.DATE);
        createEnd = createEnd == null ? null : DateUtils.ceiling(createEnd, Calendar.DATE);
        contactTypes = smartContactTypes(contactTypes, adminUser);
        Set<CrmTaskRecordCategory> firstCategories = smartFirstCategories(firstCategory, adminUser);
        List<CrmTaskRecord> taskRecords = crmTaskRecordDao.smartFindAll(createStart, createEnd, null, contactTypes, firstCategories, secondCategory, thirdCategory, userType);
        TaskRecordReport report = new TaskRecordReport(taskRecords == null ? 0 : taskRecords.size());
        if (taskRecords != null) {
            Map<CrmTaskRecordCategory, TaskRecordCounter> buffer = new HashMap<>();
            for (CrmTaskRecord taskRecord : taskRecords) {
                TaskRecordCounter firstCounter = increase(buffer, taskRecord.getFirstCategory());
                if (firstCounter.count == 1) {
                    report.summary.add(firstCounter);
                }
                TaskRecordCounter secondCounter = increase(buffer, taskRecord.getSecondCategory());
                if (secondCounter.count == 1) {
                    firstCounter.addChild(secondCounter);
                }
                TaskRecordCounter thirdCounter = increase(buffer, taskRecord.getThirdCategory());
                if (thirdCounter.count == 1) {
                    report.detail.add(thirdCounter);
                    secondCounter.moreBase();
                    firstCounter.moreBase();
                    secondCounter.addChild(thirdCounter);
                }
            }
            Collections.sort(report.detail);
        }
        return report;
    }

    private static TaskRecordCounter increase(Map<CrmTaskRecordCategory, TaskRecordCounter> buffer, CrmTaskRecordCategory category) {
        TaskRecordCounter counter = buffer.get(category);
        if (counter == null) {
            counter = new TaskRecordCounter(category);
            buffer.put(category, counter);
        }
        counter.increase();
        return counter;
    }

    private Set<CrmTaskRecordCategory> smartFirstCategories(CrmTaskRecordCategory firstCategory, AuthCurrentAdminUser adminUser) {
        return firstCategory != null ? Collections.singleton(firstCategory) : new HashSet<>(taskRecordCategories(adminUser));
    }

    public Page<CrmTaskRecord> loadUserTaskRecords(Long userId, Date createStart, Date createEnd, String recorder, CrmContactType contactType, Pageable pageable) {
        if (userId == null) {
            return null;
        }
        createStart = createStart == null ? null : DateUtils.truncate(createStart, Calendar.DATE);
        createEnd = createEnd == null ? null : DateUtils.ceiling(createEnd, Calendar.DATE);
        return crmTaskRecordDao.smartFind(createStart, createEnd, recorder, contactType, userId, pageable);
    }

    public List<CrmTaskRecord> loadUserAllTaskRecords(Long userId) {
        if (userId == null) {
            return null;
        }
        List<CrmTaskRecord> taskRecords = crmTaskRecordDao.findUserIdIs(userId);
        if (CollectionUtils.isNotEmpty(taskRecords)) {
            taskRecords.forEach(this::niceTaskRecord);
        }
        return taskRecords;
    }

    private void niceTaskRecord(CrmTaskRecord taskRecord) {
        taskRecord.setNiceCreateTime(niceDateTime(taskRecord.getCreateTime(), "yyyy-MM-dd HH:mm"));
    }

    private Set<CrmContactType> smartContactTypes(Set<CrmContactType> contactTypes, AuthCurrentAdminUser adminUser) {
        return CollectionUtils.isNotEmpty(contactTypes) ? contactTypes : new HashSet<>(contactTypes(adminUser));
    }

    public Page<CrmWorkRecord> loadWorkRecords(CrmWorkRecordType workType, Date startTime, Date endTime, Integer provinceCode, Integer cityCode, Integer countyCode, Pageable pageable) {
        return crmWorkRecordLoaderClient.findPageByDateAndRegion(workType, startTime, endTime, provinceCode, cityCode, countyCode, pageable);
    }

    public Map<Long, List<CrmTaskRecord>> loadUserTaskRecords(Set<Long> userIds) {
        Map<Long, List<CrmTaskRecord>> buffer = new HashMap<>();
        List<CrmTaskRecord> taskRecords = crmTaskRecordDao.findUserIdIn(userIds);
        if (CollectionUtils.isNotEmpty(taskRecords)) {
            for (CrmTaskRecord taskRecord : taskRecords) {
                Long userId = taskRecord.getUserId();
                List<CrmTaskRecord> userRecords = buffer.get(userId);
                if (userRecords == null) {
                    userRecords = new ArrayList<>();
                    buffer.put(userId, userRecords);
                }
                userRecords.add(taskRecord);
            }
        }
        return buffer;
    }

    public List<UserRecordSnapshot> userRecordTimeline(Long userId) {
        List<UserRecordSnapshot> userRecords = new ArrayList<>();
        List<CrmTaskRecord> taskRecords = crmTaskRecordDao.findUserIdIs(userId);
        if (CollectionUtils.isNotEmpty(taskRecords)) {
            for (CrmTaskRecord taskRecord : taskRecords) {
                userRecords.add(new UserRecordSnapshot(taskRecord));
            }
        }
        List<CrmWorkRecord> workRecords = crmWorkRecordLoaderClient.findByTeacherId(userId);
        if (CollectionUtils.isNotEmpty(workRecords)) {
            for (CrmWorkRecord workRecord : workRecords) {
                userRecords.add(convertWorkRecordToUserRecordSnapshot(workRecord, userId));
            }
        }
        Collections.sort(userRecords);
        return userRecords;
    }

    private UserRecordSnapshot convertWorkRecordToUserRecordSnapshot(CrmWorkRecord workRecord, Long teacherId){
        UserRecordSnapshot snapshot = new UserRecordSnapshot();
        snapshot.setRecordId(workRecord.getId());
        snapshot.setRecorder(String.valueOf(workRecord.getWorkerId()));
        snapshot.setRecorderName(workRecord.getWorkerName());
        CrmWorkRecordType workType = workRecord.getWorkType();
        snapshot.setRecordType(workType == null ? null : workType.value);
        snapshot.setRecordTime(workRecord.getWorkTime());
        snapshot.setRecordTitle(workRecord.fetchWorkTitle());
        CrmTeacherVisitInfo visitInfo = null;
        if(CollectionUtils.isNotEmpty(workRecord.getVisitTeacherList())){
            visitInfo = workRecord.getVisitTeacherList().stream().filter(p -> Objects.equals(p.getTeacherId(), teacherId)).findFirst().orElse(null);
        }
        snapshot.setRecordContent(visitInfo != null && StringUtils.isNotBlank(visitInfo.getVisitInfo()) ? visitInfo.getVisitInfo() : "");
        snapshot.setRecordNote(workRecord.getFollowingPlan());
        return snapshot;
    }

    public List<CrmTask> addAgentTasks(List<Properties> agentTaskDataList, CrmTaskAction action) {
        if (CollectionUtils.isEmpty(agentTaskDataList)) {
            return Collections.emptyList();
        }

        List<String> agentTaskIds = new ArrayList<>();
        for (Properties taskData : agentTaskDataList) {
            String agentTaskId = (String) taskData.get("taskDetailId");
            agentTaskIds.add(agentTaskId);
        }
        List<CrmTask> existTaskList = crmTaskDao.findByAgentTaskIds(agentTaskIds);
        Set<String> existAgentTaskIds = new HashSet<>();
        if(CollectionUtils.isNotEmpty(existTaskList)){
            existTaskList.forEach(p -> existAgentTaskIds.add(p.getAgentTaskId()));
        }

        //设置来自市场的任务信息的帐号
        List<CrmTask> tasks = new ArrayList<>();
        for (Properties taskData : agentTaskDataList) {
            CrmTaskType type = CrmTaskType.nameOf((String) taskData.get("type"));
            if (type == null) {
                continue;
            }
            AdminUser creatorUser = getCreator(type);
            if (creatorUser == null) {
                continue;
            }
            AdminUser executorUser = getExecutor(type);
            if (executorUser == null) {
                continue;
            }
            String title = (String) taskData.get("title");
            Date endTime = DateUtils.stringToDate((String) taskData.get("endTime"), "yyyy-MM-dd");
            String content = (String) taskData.get("content");
            String agentTaskId = (String) taskData.get("taskDetailId");
            if(existAgentTaskIds.contains(agentTaskId)){
                continue;
            }
            Long teacherId = ConversionUtils.toLong((String) taskData.get("teacherId"));
            String applicantName = (String) taskData.get("applicantName");
            String applicantMobile = (String) taskData.get("applicantMobile");

            CrmTask task = createCrmTask(creatorUser.getAdminUserName(), creatorUser.getRealName(), executorUser.getAdminUserName(), executorUser.getRealName(), teacherId, type, endTime, title, content, agentTaskId, applicantName, applicantMobile);
            tasks.add(task);
        }
        if(CollectionUtils.isNotEmpty(tasks)){
            crmTaskDao.inserts(tasks);
            tasks.forEach(p -> smartTaskAddStub(action, "", p));
        }
        return tasks;
    }

    private AdminUser getCreator(CrmTaskType type) {
        String creator = "";
        if (CrmTaskType.回访流失老师 == type) {
            creator = "guestmarket";
        } else if (CrmTaskType.老师转校 == type || CrmTaskType.老师新建班级 == type || CrmTaskType.老师手机绑定解绑 == type) {
            creator = "guestmarket";
        }
        if (StringUtils.isBlank(creator)) {
            return null;
        }
        return adminUserServiceClient.getAdminUserService()
                .loadAdminUser(creator)
                .getUninterruptibly();
    }

    private AdminUser getExecutor(CrmTaskType type) {
        String dept = "";
        if (CrmTaskType.回访流失老师 == type) {
            dept = DEPT_CSPOA;
        } else if (CrmTaskType.老师转校 == type || CrmTaskType.老师新建班级 == type || CrmTaskType.老师手机绑定解绑 == type) {
            dept = DEPT_CSPIS;
        }
        if (StringUtils.isBlank(dept)) {
            return null;
        }
        List<AdminUser> adminUserList = departmentUsers(dept);
        if (CollectionUtils.isEmpty(adminUserList)) {
            return null;
        }
        // 老师转校, 老师新建班级, 老师手机绑定解绑  优先指定给指定账户
        List<AdminUser> tempUserList = adminUserList.stream().filter(p -> Objects.equals(p.getAdminUserName(), "guestcs")).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(tempUserList)) {
            return tempUserList.get(0);
        }

        return adminUserList.get(0);
    }

    /**
     * 任务批量转发
     *
     * @param adminUser 操作用户
     * @param executor  转发给
     * @param taskIds   taskIds
     * @param action    action
     * @param createFlg 是否创建新的任务
     * @return
     */
    public List<CrmTask> taskBatchForward(AuthCurrentAdminUser adminUser, String executor, Collection<String> taskIds, CrmTaskAction action, Boolean createFlg) {

        if (adminUser == null || StringUtils.isBlank(executor) || CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        if (createFlg == null) {
            createFlg = false;
        }
        List<CrmTask> tasks = new ArrayList<>();
        String creator = adminUser.getAdminUserName();
        String creatorName = adminUser.getRealName();
        AdminUser iExecutor = adminUserServiceClient.getAdminUserService()
                .loadAdminUser(executor)
                .getUninterruptibly();
        String executorName = iExecutor == null ? null : iExecutor.getRealName();

        Map<String, CrmTask> originTaskMap = crmTaskDao.loads(taskIds);
        Map<String, CrmTask> newTaskMap = new HashMap<>();
        if (createFlg) {
            originTaskMap.forEach((k, p) -> {
                if (!executor.equals(p.getExecutor())) {
                    newTaskMap.put(k, this.createCrmTask(creator, creatorName, executor, executorName, p.getUserId(), p.getType(), p.getEndTime(), p.getTitle(), p.getContent(), p.getAgentTaskId(), p.getApplicantName(), p.getApplicantMobile()));
                }
            });
            crmTaskDao.inserts(newTaskMap.values());
        } else {
            originTaskMap.forEach((k, p) -> {
                if (!executor.equals(p.getExecutor())) {
                    p.setCreator(creator);
                    p.setCreatorName(creatorName);
                    p.setExecutor(executor);
                    p.setExecutorName(executorName);
                    crmTaskDao.update(p.getId(), p);
                    newTaskMap.put(k, p);
                }
            });
        }
        tasks.addAll(newTaskMap.values());
        newTaskMap.forEach((k, v) -> smartTaskAddStub(action, k, v));
        return tasks;
    }


    public List<CrmTaskRecord> loadRecordByRecorderAndTaskId(String recorder, String taskId) {
        if (StringUtils.isBlank(recorder) || StringUtils.isBlank(taskId)) {
            return Collections.emptyList();
        }
        return crmTaskRecordDao.loadByRecorderAndTaskId(recorder, taskId);
    }
}

class StatusStartEnd {
    CrmTaskStatus status;
    Date start;
    Date end;

    public StatusStartEnd(CrmTaskStatus status, Date startTime, Date endTime) {
        Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        CrmTaskStatus iStatus = status;
        Date start = startTime;
        Date end = endTime;
        if (status != null) {
            switch (status) {
                case NEW:
                    iStatus = CrmTaskStatus.NEW;
                    start = today;
                    break;
                case UNFOLLOW:
                    iStatus = CrmTaskStatus.NEW;
                    end = today;
                    break;
                case FOLLOWING:
                    iStatus = CrmTaskStatus.FOLLOWING;
                    start = today;
                    break;
                case UNFINISH:
                    iStatus = CrmTaskStatus.FOLLOWING;
                    end = today;
                    break;
                default:
                    break;
            }
        }
        this.status = iStatus;
        this.start = startTime != null && startTime.after(start) ? startTime : start;
        this.end = endTime != null && endTime.before(end) ? endTime : end;
    }
}
