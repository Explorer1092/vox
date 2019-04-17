package com.voxlearning.utopia.agent.service.taskmanage;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.constants.AgentTaskType;
import com.voxlearning.utopia.agent.dao.mongo.task.AgentTaskMainDao;
import com.voxlearning.utopia.agent.dao.mongo.task.AgentTaskSubIntoSchoolDao;
import com.voxlearning.utopia.agent.dao.mongo.task.AgentTaskSubOnlineDao;
import com.voxlearning.utopia.agent.persist.entity.task.*;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 任务中心service
 *
 * @author deliang.che
 * @since 2018-05-28
 */
@Named
public class AgentTaskCenterService {

    @Inject private RaikouSystem raikouSystem;
    @Inject
    private AgentTaskMainDao agentTaskMainDao;
    @Inject
    private TeacherLoaderClient teacherLoader;
    @Inject
    private AgentTaskSubOnlineDao agentTaskSubOnlineDao;
    @Inject
    private AgentTaskSubIntoSchoolDao agentTaskSubIntoSchoolDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;

    /**
     * 获取所有主任务
     *
     * @return
     */
    public List<AgentTaskMain> getAllMainTask() {
        return agentTaskMainDao.loadByDate(null);
    }

    /**
     * 获取主任务
     *
     * @param id
     * @return
     */
    public AgentTaskMain getMainTaskById(String id) {
        return agentTaskMainDao.load(id);
    }

    /**
     * 获取线上维护老师
     *
     * @param id
     * @return
     */
    public AgentTaskSubOnline getSubTaskOnlineById(String id) {
        return agentTaskSubOnlineDao.load(id);
    }

    /**
     * 更新主任务
     *
     * @param agentTaskMain
     */
    public void updateMainTask(AgentTaskMain agentTaskMain) {
        agentTaskMainDao.replace(agentTaskMain);
    }

    /**
     * 更新线上维护老师
     *
     * @param agentTaskSubOnline
     */
    public void updateSubTaskOnline(AgentTaskSubOnline agentTaskSubOnline) {
        agentTaskSubOnlineDao.replace(agentTaskSubOnline);
    }

    /**
     * 检查Excel文件
     *
     * @param workbook
     * @param taskType
     * @return
     */
    public MapMessage checkWorkboook(XSSFWorkbook workbook, AgentTaskType taskType) {
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet == null) {
            return MapMessage.errorMessage();
        }

        List<AgentTaskSubIntoSchool> agentTaskSubIntoSchoolList = new ArrayList<>();
        List<AgentTaskSubOnline> agentTaskSubOnlineList = new ArrayList<>();
        List<String> errorMessage = new ArrayList<>();
        Set<Long> teacherIds = new HashSet();
        Map<String, Long> operatorTeacherIdMap = new HashMap<>();

        Set<Long> allSchoolIds = new HashSet<>();
        Set<Long> allTeacherIds = new HashSet<>();
        int rowNo = 1;
        while (true) {
            XSSFRow row = sheet.getRow(rowNo++);
            if (row == null) {
                break;
            }
            String schoolIdStr = XssfUtils.getStringCellValue(row.getCell(1));
            String teacherIdStr = XssfUtils.getStringCellValue(row.getCell(3));
            Long schoolId = SafeConverter.toLong(schoolIdStr);
            if (schoolId > 0) {
                allSchoolIds.add(schoolId);
            }
            Long teacherId = SafeConverter.toLong(teacherIdStr);
            if (teacherId > 0) {
                allTeacherIds.add(teacherId);
            }
        }

        Map<Long, CrmSchoolSummary> schoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(allSchoolIds);
        List<Long> noCatchSchoolIds = new ArrayList<>();
        Map<Long, Object> schoolObjectMap = new HashMap<>();
        allSchoolIds.forEach(item -> {
            if (schoolSummaryMap.containsKey(item)) {
                CrmSchoolSummary schoolSummary = schoolSummaryMap.get(item);
                if (null != schoolSummary)
                    schoolObjectMap.put(item, schoolSummaryMap);
            } else {
                noCatchSchoolIds.add(item);
            }
        });
        if (CollectionUtils.isNotEmpty(noCatchSchoolIds)) {
            Map<Long, School> schoolMap = raikouSystem.loadSchools(noCatchSchoolIds);
            schoolMap.forEach((k, v) -> {
                if (null != v) {
                    schoolObjectMap.put(k, v);
                }
            });
        }


        Map<Long, CrmTeacherSummary> teacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(allTeacherIds);
        List<Long> noCatchTeacherIds = new ArrayList<>();
        Map<Long, Object> teacherObjectMap = new HashMap<>();
        allTeacherIds.forEach(item -> {
            if (teacherSummaryMap.containsKey(item)) {
                CrmTeacherSummary crmTeacherSummary = teacherSummaryMap.get(item);
                if (null != crmTeacherSummary)
                    teacherObjectMap.put(item, crmTeacherSummary);
            } else {
                noCatchTeacherIds.add(item);
            }
        });
        if (CollectionUtils.isNotEmpty(noCatchTeacherIds)) {
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(noCatchTeacherIds);
            teacherMap.forEach((k, v) -> {
                if (null != v) {
                    teacherObjectMap.put(k, v);
                }
            });
        }


        boolean checkFlag = true;
        rowNo = 1;
        while (true) {
            XSSFRow row = sheet.getRow(rowNo++);
            if (row == null) {
                break;
            }
            String operatorName = SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(0)));
            String schoolIdStr = XssfUtils.getStringCellValue(row.getCell(1));
            String schoolName = SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(2)));
            String teacherIdStr = XssfUtils.getStringCellValue(row.getCell(3));
            String teacherName = SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(4)));
            String comment = SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(5)));

            Long userId = 0l;
            Long schoolId = 0l;
            Long teacherId = 0l;
            //执行人是否为空
            if (StringUtils.isBlank(operatorName)) {
                checkFlag = false;
                errorMessage.add("第" + rowNo + "行，无执行人，请检查后重新上传");
                continue;
            }
            //学校ID是否为空
            if (StringUtils.isBlank(schoolIdStr)) {
                checkFlag = false;
                errorMessage.add("第" + rowNo + "行，无学校ID，请检查后重新上传");
                continue;
            }
            //老师ID是否为空
            if (StringUtils.isBlank(teacherIdStr)) {
                checkFlag = false;
                errorMessage.add("第" + rowNo + "行，无老师ID，请检查后重新上传");
                continue;
            }
            //系统中是否有该执行人
            List<AgentUser> agentUserList = baseOrgService.getUserByRealName(operatorName);
            if (CollectionUtils.isNotEmpty(agentUserList)) {
                userId = agentUserList.stream().map(AgentUser::getId).findFirst().orElse(null);
            } else {
                checkFlag = false;
                errorMessage.add("第" + rowNo + "行，系统中无此执行人，请检查后重新上传");
                continue;
            }
            //系统中是否有该学校ID
            schoolId = SafeConverter.toLong(schoolIdStr);
            if (schoolId > 0) {
                if (!schoolObjectMap.containsKey(schoolId)) {
                    checkFlag = false;
                    errorMessage.add("第" + rowNo + "行，系统中无此学校ID，请检查后重新上传");
                    continue;
                }
            } else {
                checkFlag = false;
                errorMessage.add("第" + rowNo + "行，学校ID类型不正确，请检查后重新上传");
                continue;
            }
            //系统中是否有该老师ID
            teacherId = SafeConverter.toLong(teacherIdStr);
            if (teacherId > 0) {
                if (!teacherObjectMap.containsKey(teacherId)) {
                    checkFlag = false;
                    errorMessage.add("第" + rowNo + "行，系统中无此老师ID，请检查后重新上传");
                    continue;
                }
            } else {
                checkFlag = false;
                errorMessage.add("第" + rowNo + "行，老师ID类型不正确，请检查后重新上传");
                continue;
            }

            if (checkFlag) {
                //过滤掉重复执行人&重复老师ID
                if (operatorTeacherIdMap.containsKey(operatorName) && Objects.equals(operatorTeacherIdMap.get(operatorName), teacherId)) {
                    continue;
                }
                operatorTeacherIdMap.put(operatorName, teacherId);
                //老师ID去重
                teacherIds.add(teacherId);

                if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER) {
                    AgentTaskSubOnline agentTaskSubOnline = new AgentTaskSubOnline();
                    agentTaskSubOnline.setOperatorId(userId);
                    agentTaskSubOnline.setOperatorName(operatorName);
                    agentTaskSubOnline.setSchoolId(schoolId);
                    agentTaskSubOnline.setSchoolName(schoolName);
                    agentTaskSubOnline.setTeacherId(teacherId);
                    agentTaskSubOnline.setTeacherName(teacherName);
                    agentTaskSubOnline.setComment(comment);
                    agentTaskSubOnline.setIsFeedback(false);
                    agentTaskSubOnline.setIsHomework(false);
                    agentTaskSubOnline.setDisabled(false);
                    agentTaskSubOnlineList.add(agentTaskSubOnline);
                } else if (taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER) {
                    AgentTaskSubIntoSchool agentTaskSubIntoSchool = new AgentTaskSubIntoSchool();
                    agentTaskSubIntoSchool.setOperatorId(userId);
                    agentTaskSubIntoSchool.setOperatorName(operatorName);
                    agentTaskSubIntoSchool.setSchoolId(schoolId);
                    agentTaskSubIntoSchool.setSchoolName(schoolName);
                    agentTaskSubIntoSchool.setTeacherId(teacherId);
                    agentTaskSubIntoSchool.setTeacherName(teacherName);
                    agentTaskSubIntoSchool.setComment(comment);
                    agentTaskSubIntoSchool.setIsIntoSchool(false);
                    agentTaskSubIntoSchool.setIsVisitTeacher(false);
                    agentTaskSubIntoSchool.setIsHomework(false);
                    agentTaskSubIntoSchool.setDisabled(false);
                    agentTaskSubIntoSchoolList.add(agentTaskSubIntoSchool);
                }
            }
        }
        if (!checkFlag) {
            return MapMessage.errorMessage().add("errorList", errorMessage);
        }
        if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER && CollectionUtils.isEmpty(agentTaskSubOnlineList)
                || taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER && CollectionUtils.isEmpty(agentTaskSubIntoSchoolList)) {
            errorMessage.add("文件无有效数据！");
            return MapMessage.errorMessage().add("errorList", errorMessage);
        }

        MapMessage message = MapMessage.successMessage();
        if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER) {
            message.add("dataList", agentTaskSubOnlineList);
        } else if (taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER) {
            message.add("dataList", agentTaskSubIntoSchoolList);
        }
        message.add("teacherNum", (teacherIds.size()));
        return message;
    }

    /**
     * 任务导入
     *
     * @param workbook
     * @param taskType
     * @param agentTaskMain
     * @return
     */
    public MapMessage importTask(XSSFWorkbook workbook, AgentTaskType taskType, AgentTaskMain agentTaskMain) {
        //检查excel文件
        MapMessage checkResult = checkWorkboook(workbook, taskType);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        //主任务，设置老师数目
        checkResult.get("rowNo");
        agentTaskMain.setRowNum(SafeConverter.toInt(checkResult.get("teacherNum")));
        agentTaskMainDao.insert(agentTaskMain);

        //线上维护老师
        if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER) {
            List<AgentTaskSubOnline> agentTaskSubOnlineList = (List<AgentTaskSubOnline>) checkResult.get("dataList");
            for (AgentTaskSubOnline agentTaskSubOnline : agentTaskSubOnlineList) {
                agentTaskSubOnline.setMainTaskId(agentTaskMain.getId());
                agentTaskSubOnline.setDisabled(false);
            }
            agentTaskSubOnlineDao.inserts(agentTaskSubOnlineList);

            //进校维护老师
        } else if (taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER) {
            List<AgentTaskSubIntoSchool> agentTaskSubInschoolList = (List<AgentTaskSubIntoSchool>) checkResult.get("dataList");
            for (AgentTaskSubIntoSchool agentTaskSubInschool : agentTaskSubInschoolList) {
                agentTaskSubInschool.setMainTaskId(agentTaskMain.getId());
                agentTaskSubInschool.setDisabled(false);
            }
            agentTaskSubIntoSchoolDao.inserts(agentTaskSubInschoolList);
        }
        return MapMessage.successMessage();
    }

    /**
     * 主任务列表数据转换
     *
     * @param agentTaskMainList
     * @return
     */
    public List<Map<String, Object>> toMainTaskListMap(List<AgentTaskMain> agentTaskMainList) {
        if (CollectionUtils.isEmpty(agentTaskMainList)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        Date currentDate = new Date();
        agentTaskMainList.forEach(item -> {
            Map<String, Object> agentMainTaskMap = new HashMap<>();
            if (null != item) {
                agentMainTaskMap.put("id", item.getId());
                agentMainTaskMap.put("title", item.getTitle());
                agentMainTaskMap.put("userName", item.getUserName());
                agentMainTaskMap.put("createTime", DateUtils.dateToString(item.getCreateTime(), "yyyy-MM-dd HH:mm"));
                agentMainTaskMap.put("endTime", DateUtils.dateToString(item.getEndTime(), "yyyy-MM-dd HH:mm"));
                agentMainTaskMap.put("taskType", null == item.getTaskType() ? "" : item.getTaskType().getValue());
                agentMainTaskMap.put("teacherNum", item.getRowNum());
                agentMainTaskMap.put("comment", item.getComment());
                agentMainTaskMap.put("showUpdateOperator", item.getEndTime().after(currentDate));
            }
            dataList.add(agentMainTaskMap);
        });
        return dataList;
    }

    /**
     * 主任务详情数据转换
     *
     * @param agentTaskMain
     * @return
     */
    public Map<String, Object> toMainTaskMap(AgentTaskMain agentTaskMain) {
        if (null == agentTaskMain) {
            return new HashMap<>();
        }
        Map<String, Object> agentMainTaskMap = new HashMap<>();
        agentMainTaskMap.put("id", agentTaskMain.getId());
        agentMainTaskMap.put("title", agentTaskMain.getTitle());
        agentMainTaskMap.put("taskType", null == agentTaskMain.getTaskType() ? "" : agentTaskMain.getTaskType().getValue());
        agentMainTaskMap.put("endTime", DateUtils.dateToString(agentTaskMain.getEndTime(), "yyyy-MM-dd HH:mm"));
        agentMainTaskMap.put("comment", agentTaskMain.getComment());
        return agentMainTaskMap;
    }

    /**
     * 删除任务
     *
     * @param id
     */
    public void deleteTask(String id) {
        AgentTaskMain agentTaskMain = agentTaskMainDao.load(id);
        if (null != agentTaskMain) {
            AgentTaskType taskType = agentTaskMain.getTaskType();
            //删除线上维护老师数据
            if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER) {
                agentTaskSubOnlineDao.deleteByMainTaskId(id);
                //删除进校维护老师
            } else if (taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER) {
                agentTaskSubIntoSchoolDao.deleteByMainTaskId(id);
            }
            //删除主任务数据
            agentTaskMain.setDisabled(true);
            agentTaskMainDao.replace(agentTaskMain);
        }
    }

    /**
     * 根据条件获取进校维护老师
     *
     * @param mainTaskId
     * @param operatorId
     * @return
     */
    public List<Map<String, Object>> getTaskSubByCondition(String mainTaskId, Long operatorId) {

        List<Map<String, Object>> dataList = new ArrayList<>();
        AgentTaskMain agentTaskMain = agentTaskMainDao.load(mainTaskId);
        if (null == agentTaskMain) {
            return new ArrayList<>();
        }
        AgentTaskType taskType = agentTaskMain.getTaskType();
        //进校维护老师
        if (taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER) {
            List<AgentTaskSubIntoSchool> agentTaskSubIntoSchoolList = agentTaskSubIntoSchoolDao.loadByMainTaskId(mainTaskId);
            agentTaskSubIntoSchoolList = agentTaskSubIntoSchoolList.stream().filter(item -> null != item && Objects.equals(item.getOperatorId(), operatorId)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(agentTaskSubIntoSchoolList)) {
                return new ArrayList<>();
            }
            agentTaskSubIntoSchoolList.forEach(item -> {
                if (null != item) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("id", item.getId());
                    dataMap.put("operatorName", item.getOperatorName());
                    AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(item.getOperatorId()).stream().findFirst().orElse(null);
                    if (null != groupUser) {
                        dataMap.put("groupName", baseOrgService.getGroupById(groupUser.getGroupId()));
                    }
                    dataMap.put("schoolId", item.getSchoolId());
                    dataMap.put("schoolName", item.getSchoolName());
                    dataMap.put("teacherId", item.getTeacherId());
                    dataMap.put("teacherName", item.getTeacherName());
                    dataList.add(dataMap);
                }
            });
            //线上维护老师
        } else if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER) {
            List<AgentTaskSubOnline> agentTaskSubOnlineList = agentTaskSubOnlineDao.loadByMainTaskId(mainTaskId);
            agentTaskSubOnlineList = agentTaskSubOnlineList.stream().filter(item -> null != item && Objects.equals(item.getOperatorId(), operatorId)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(agentTaskSubOnlineList)) {
                return new ArrayList<>();
            }
            agentTaskSubOnlineList.forEach(item -> {
                if (null != item) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("id", item.getId());
                    dataMap.put("operatorName", item.getOperatorName());
                    AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(item.getOperatorId()).stream().findFirst().orElse(null);
                    if (null != groupUser) {
                        dataMap.put("groupName", baseOrgService.getGroupById(groupUser.getGroupId()));
                    }
                    dataMap.put("schoolId", item.getSchoolId());
                    dataMap.put("schoolName", item.getSchoolName());
                    dataMap.put("teacherId", item.getTeacherId());
                    dataMap.put("teacherName", item.getTeacherName());
                    dataList.add(dataMap);
                }
            });
        }
        return dataList;
    }

    /**
     * 修改执行人
     *
     * @param mainTaskId
     * @param ids
     * @param operatorId
     * @param operatorName
     */
    public void updateOperator(String mainTaskId, Collection<String> ids, Long operatorId, String operatorName) {
        AgentTaskMain agentTaskMain = agentTaskMainDao.load(mainTaskId);
        if (null != agentTaskMain) {
            AgentTaskType taskType = agentTaskMain.getTaskType();
            //进校维护老师
            if (taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER) {
                agentTaskSubIntoSchoolDao.updateOperator(ids, operatorId, operatorName);
                //线上维护老师
            } else if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER) {
                agentTaskSubOnlineDao.updateOperator(ids, operatorId, operatorName);
            }
        }
    }

    /**
     * 线上维护老师完成情况
     *
     * @param mainTaskId
     * @return
     */
    public Map<String, Object> getSubTaskOnlineFinishInfo(String mainTaskId) {
        Map<String, Object> dataMap = new HashMap<>();
        if (StringUtils.isBlank(mainTaskId)) {
            return new HashMap<>();
        }
        AgentTaskMain taskMain = agentTaskMainDao.load(mainTaskId);
        if (null == taskMain) {
            return new HashMap<>();
        }
        Date createTime = taskMain.getCreateTime();
        Date endTime = taskMain.getEndTime();
        List<AgentTaskSubOnline> agentTaskSubOnlineList = agentTaskSubOnlineDao.loadByMainTaskId(mainTaskId);
        Set<Long> allTeacherIds = new HashSet<>();
        Set<Long> isFeedbackTeacherIds = new HashSet<>();
        Set<Long> isHomeworkTeacherIds = new HashSet<>();
        agentTaskSubOnlineList.forEach(item -> {
            if (null != item) {
                allTeacherIds.add(item.getTeacherId());
                //已维护老师
                if (null != item.getIsFeedback() && item.getIsFeedback()) {
                    isFeedbackTeacherIds.add(item.getTeacherId());
                }
                //任务期间布置作业
                if (null != item.getIsHomework() && item.getIsHomework()
                        && null != item.getHomeworkTime() && item.getHomeworkTime().compareTo(createTime) >= 0 && item.getHomeworkTime().compareTo(endTime) <= 0) {
                    isHomeworkTeacherIds.add(item.getTeacherId());
                    //未布置作业
                }
            }
        });

        //老师维护率
        double isFeedbackRate = MathUtils.doubleDivide(isFeedbackTeacherIds.size() * 100, allTeacherIds.size());
        //布置作业率
        double isHomeworkRate = MathUtils.doubleDivide(isHomeworkTeacherIds.size() * 100, allTeacherIds.size());
        dataMap.put("teacherNum", allTeacherIds.size());
        dataMap.put("isFeedbackTeacherNum", isFeedbackTeacherIds.size());
        dataMap.put("isHomeworkTeacherNum", isHomeworkTeacherIds.size());
        dataMap.put("isFeedbackRate", isFeedbackRate);
        dataMap.put("isHomeworkRate", isHomeworkRate);
        return dataMap;
    }

    /**
     * 进校维护老师完成情况
     *
     * @param mainTaskId
     * @return
     */
    public Map<String, Object> getSubTaskInschoolFinishInfo(String mainTaskId) {
        Map<String, Object> dataMap = new HashMap<>();
        if (StringUtils.isBlank(mainTaskId)) {
            return new HashMap<>();
        }
        AgentTaskMain taskMain = agentTaskMainDao.load(mainTaskId);
        if (null == taskMain) {
            return new HashMap<>();
        }
        Date createTime = taskMain.getCreateTime();
        Date endTime = taskMain.getEndTime();
        List<AgentTaskSubIntoSchool> agentTaskSubIntoSchoolList = agentTaskSubIntoSchoolDao.loadByMainTaskId(mainTaskId);
        Set<Long> allSchoolIds = new HashSet<>();
        Set<Long> isInSchoolIds = new HashSet<>();
        Set<Long> allTeacherIds = new HashSet<>();
        Set<Long> isVisitTeacherIds = new HashSet<>();
        Set<Long> isHomeworkTeacherIds = new HashSet<>();
        agentTaskSubIntoSchoolList.forEach(item -> {
            if (null != item) {
                allSchoolIds.add(item.getSchoolId());
                allTeacherIds.add(item.getTeacherId());
                //任务期间已进校
                if (null != item.getIsIntoSchool() && item.getIsIntoSchool()
                        && null != item.getIntoSchoolTime() && item.getIntoSchoolTime().compareTo(createTime) >= 0 && item.getIntoSchoolTime().compareTo(endTime) <= 0) {
                    isInSchoolIds.add(item.getSchoolId());
                    //未进校
                }
                //任务期间已拜访老师
                if (null != item.getIsVisitTeacher() && item.getIsVisitTeacher()
                        && null != item.getVisitTeacherTime() && item.getVisitTeacherTime().compareTo(createTime) >= 0 && item.getVisitTeacherTime().compareTo(endTime) <= 0) {
                    isVisitTeacherIds.add(item.getTeacherId());
                    //未拜访老师
                }
                //任务期间已布置作业
                if (null != item.getIsHomework() && item.getIsHomework()
                        && null != item.getHomeworkTime() && item.getHomeworkTime().compareTo(createTime) >= 0 && item.getHomeworkTime().compareTo(endTime) <= 0) {
                    isHomeworkTeacherIds.add(item.getTeacherId());
                    //未布置作业
                }
            }
        });

        //进校率
        double isInSchoolRate = MathUtils.doubleDivide(isInSchoolIds.size() * 100, allSchoolIds.size());
        //拜访率
        double isVisitTeacherRate = MathUtils.doubleDivide(isVisitTeacherIds.size() * 100, allTeacherIds.size());
        //布置率
        double isHomeworkTeacherRate = MathUtils.doubleDivide(isHomeworkTeacherIds.size() * 100, allTeacherIds.size());
        dataMap.put("schoolNum", allSchoolIds.size());
        dataMap.put("teacherNum", allTeacherIds.size());
        dataMap.put("isInSchoolNum", isInSchoolIds.size());
        dataMap.put("isVisitTeacherNum", isVisitTeacherIds.size());
        dataMap.put("isHomeworkTeacherNum", isHomeworkTeacherIds.size());
        dataMap.put("isInSchoolRate", isInSchoolRate);
        dataMap.put("isVisitTeacherRate", isVisitTeacherRate);
        dataMap.put("isHomeworkTeacherRate", isHomeworkTeacherRate);
        return dataMap;
    }

    /**
     * 任务完成情况
     *
     * @param mainTaskId
     * @return
     */
    public Map<String, Object> taskFinishInfo(String mainTaskId) {
        Map<String, Object> dataMap = new HashMap<>();
        AgentTaskMain agentTaskMain = agentTaskMainDao.load(mainTaskId);
        if (null != agentTaskMain) {
            AgentTaskType taskType = agentTaskMain.getTaskType();
            //进校维护老师
            if (taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER) {
                dataMap = getSubTaskInschoolFinishInfo(mainTaskId);
                //线上维护老师
            } else if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER) {
                dataMap = getSubTaskOnlineFinishInfo(mainTaskId);
            }
        }
        return dataMap;
    }

    /**
     * 线上维护老师导出数据
     *
     * @param mainTaskId
     * @return
     */
    public List<AgentTaskSubOnlineExportData> getTaskSubOnlineExportData(String mainTaskId) {
        AgentTaskMain taskMain = agentTaskMainDao.load(mainTaskId);
        if (null == taskMain) {
            return new ArrayList<>();
        }
        Date createTime = taskMain.getCreateTime();
        Date endTime = taskMain.getEndTime();

        List<AgentTaskSubOnlineExportData> dataList = new ArrayList<>();

        List<AgentTaskSubOnline> agentTaskSubOnlineList = agentTaskSubOnlineDao.loadByMainTaskId(mainTaskId);

        Set<Long> teacherIds = new HashSet<>();
        Set<Long> schoolIds = new HashSet<>();
        Set<Long> operatorIds = new HashSet<>();
        agentTaskSubOnlineList.forEach(item -> {
            if (null != item) {
                teacherIds.add(item.getTeacherId());
                schoolIds.add(item.getSchoolId());
                operatorIds.add(item.getOperatorId());
            }
        });

        //获取老师ID与科目对应关系
        Map<Long, Object> teacherSubjectMap = getTeacherSubjectMap(teacherIds);

        //获取学校信息
        Map<Long, CrmSchoolSummary> schoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(schoolIds);

        //根据执行人ids，获取对应的部门等级以及部门信息
        Map<Long, Map<AgentGroupRoleType, AgentGroup>> userRoleGroupMap = getUserRoleGroupMap(operatorIds);

        agentTaskSubOnlineList.forEach(item -> {
            if (null != item) {
                AgentTaskSubOnlineExportData agentTaskSubOnlineExportData = new AgentTaskSubOnlineExportData();

                agentTaskSubOnlineExportData.setOperatorName(item.getOperatorName());

                Map<AgentGroupRoleType, AgentGroup> groupRoleMap = userRoleGroupMap.get(item.getOperatorId());

                if (null != groupRoleMap) {
                    AgentGroup marketingGroup = groupRoleMap.get(AgentGroupRoleType.Marketing);
                    if (marketingGroup != null) {
                        agentTaskSubOnlineExportData.setMarketingName(marketingGroup.getGroupName());
                    }
                    AgentGroup regionGroup = groupRoleMap.get(AgentGroupRoleType.Region);
                    if (regionGroup != null) {
                        agentTaskSubOnlineExportData.setRegionName(regionGroup.getGroupName());
                    }
                    AgentGroup areaGroup = groupRoleMap.get(AgentGroupRoleType.Area);
                    if (areaGroup != null) {
                        agentTaskSubOnlineExportData.setAreaName(areaGroup.getGroupName());
                    }
                    AgentGroup cityGroup = groupRoleMap.get(AgentGroupRoleType.City);
                    if (cityGroup != null) {
                        agentTaskSubOnlineExportData.setCityName(cityGroup.getGroupName());
                    }
                }


                agentTaskSubOnlineExportData.setSchoolId(item.getSchoolId());
                agentTaskSubOnlineExportData.setSchoolName(item.getSchoolName());
                agentTaskSubOnlineExportData.setTeacherId(item.getTeacherId());
                agentTaskSubOnlineExportData.setTeacherName(item.getTeacherName());

                //设置老师科目
                agentTaskSubOnlineExportData.setSubject(ConversionUtils.toString(teacherSubjectMap.get(item.getTeacherId())));

                //设置省、市、区
                CrmSchoolSummary schoolSummary = schoolSummaryMap.get(item.getSchoolId());
                if (schoolSummary != null) {
                    agentTaskSubOnlineExportData.setProvince(schoolSummary.getProvinceName());
                    agentTaskSubOnlineExportData.setCity(schoolSummary.getCityName());
                    agentTaskSubOnlineExportData.setCounty(schoolSummary.getCountyName());
                }

                agentTaskSubOnlineExportData.setFeedbackTime(item.getFeedbackTime());
                agentTaskSubOnlineExportData.setFeedbackType(item.getFeedbackType());
                agentTaskSubOnlineExportData.setFeedbackResult(item.getFeedbackResult());
                if (null != item.getIsHomework() && item.getIsHomework()
                        && null != item.getHomeworkTime() && item.getHomeworkTime().compareTo(createTime) >= 0 && item.getHomeworkTime().compareTo(endTime) <= 0) {
                    agentTaskSubOnlineExportData.setIsHomework(true);
                } else {
                    agentTaskSubOnlineExportData.setIsHomework(false);
                }
                dataList.add(agentTaskSubOnlineExportData);

            }
        });
        return dataList;
    }


    /**
     * 获取老师ID与科目对应关系
     *
     * @param teacherIds
     * @return
     */
    private Map<Long, Object> getTeacherSubjectMap(Collection<Long> teacherIds) {
        Map<Long, Object> teacherSubjectMap = new HashMap<>();
        Map<Long, CrmTeacherSummary> teacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(teacherIds);
        List<Long> noCatchTeacherIds = new ArrayList<>();
        teacherIds.forEach(teacherId -> {
            if (teacherSummaryMap.containsKey(teacherId)) {
                CrmTeacherSummary crmTeacherSummary = teacherSummaryMap.get(teacherId);
                if (null != crmTeacherSummary)
                    teacherSubjectMap.put(teacherId, crmTeacherSummary.getSubjectValue());
            } else {
                noCatchTeacherIds.add(teacherId);
            }
        });
        if (CollectionUtils.isNotEmpty(noCatchTeacherIds)) {
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(noCatchTeacherIds);
            teacherMap.forEach((k, v) -> {
                if (null != v) {
                    teacherSubjectMap.put(k, null != v.getSubject() ? v.getSubject().getValue() : "");
                }
            });
        }
        return teacherSubjectMap;
    }

    /**
     * 根据执行人ids，获取对应的部门等级以及部门信息
     *
     * @param operatorIds
     * @return
     */
    private Map<Long, Map<AgentGroupRoleType, AgentGroup>> getUserRoleGroupMap(Collection<Long> operatorIds) {
        //获取执行人ID与部门关系map
        Map<Long, List<Long>> userGroupsMap = baseOrgService.getUserGroupIdList(operatorIds);

        Map<Long, Map<AgentGroupRoleType, AgentGroup>> userRoleGroupMap = new HashMap<>();

        operatorIds.forEach(p -> {
            //递归设置部门级别
            if (userGroupsMap.containsKey(p)) {
                Map<AgentGroupRoleType, AgentGroup> itemMap = userRoleGroupMap.computeIfAbsent(p, k -> new HashMap<>());
                List<Long> groupIds = userGroupsMap.get(p);
                if (CollectionUtils.isNotEmpty(groupIds)) {
                    Long groupId = groupIds.get(0);
                    List<AgentGroupRoleType> groupRoleTypes = new ArrayList<>();
                    groupRoleTypes.add(AgentGroupRoleType.Marketing);
                    groupRoleTypes.add(AgentGroupRoleType.Region);
                    groupRoleTypes.add(AgentGroupRoleType.Area);
                    groupRoleTypes.add(AgentGroupRoleType.City);

                    groupRoleTypes.forEach(role -> {
                        AgentGroup group = baseOrgService.getParentGroupByRole(groupId, role);
                        if (group != null) {
                            itemMap.put(group.fetchGroupRoleType(), group);
                            userRoleGroupMap.put(p, itemMap);
                        }
                    });
                }
            }
        });
        return userRoleGroupMap;
    }

    /**
     * 进校维护老师导出数据
     *
     * @param mainTaskId
     * @return
     */
    public List<AgentTaskSubIntoSchoolExportData> getTaskSubIntoSchoolExportData(String mainTaskId) {

        AgentTaskMain taskMain = agentTaskMainDao.load(mainTaskId);
        if (null == taskMain) {
            return new ArrayList<>();
        }
        Date createTime = taskMain.getCreateTime();
        Date endTime = taskMain.getEndTime();

        List<AgentTaskSubIntoSchoolExportData> dataList = new ArrayList<>();

        List<AgentTaskSubIntoSchool> agentTaskSubIntoSchoolList = agentTaskSubIntoSchoolDao.loadByMainTaskId(mainTaskId);

        Set<Long> teacherIds = new HashSet<>();
        Set<Long> schoolIds = new HashSet<>();
        Set<Long> operatorIds = new HashSet<>();
        agentTaskSubIntoSchoolList.forEach(item -> {
            if (null != item) {
                teacherIds.add(item.getTeacherId());
                schoolIds.add(item.getSchoolId());
                operatorIds.add(item.getOperatorId());
            }
        });

        //获取老师ID与科目对应关系
        Map<Long, Object> teacherSubjectMap = getTeacherSubjectMap(teacherIds);
        //获取学校信息
        Map<Long, CrmSchoolSummary> schoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(schoolIds);

        //根据执行人ids，获取对应的部门等级以及部门信息
        Map<Long, Map<AgentGroupRoleType, AgentGroup>> userRoleGroupMap = getUserRoleGroupMap(operatorIds);

        agentTaskSubIntoSchoolList.forEach(item -> {
            AgentTaskSubIntoSchoolExportData agentTaskSubInschoolExportData = new AgentTaskSubIntoSchoolExportData();
            if (null != item) {
                agentTaskSubInschoolExportData.setOperatorName(item.getOperatorName());

                Map<AgentGroupRoleType, AgentGroup> groupRoleMap = userRoleGroupMap.get(item.getOperatorId());

                if (null != groupRoleMap) {
                    AgentGroup marketingGroup = groupRoleMap.get(AgentGroupRoleType.Marketing);
                    if (marketingGroup != null) {
                        agentTaskSubInschoolExportData.setMarketingName(marketingGroup.getGroupName());
                    }
                    AgentGroup regionGroup = groupRoleMap.get(AgentGroupRoleType.Region);
                    if (regionGroup != null) {
                        agentTaskSubInschoolExportData.setRegionName(regionGroup.getGroupName());
                    }
                    AgentGroup areaGroup = groupRoleMap.get(AgentGroupRoleType.Area);
                    if (areaGroup != null) {
                        agentTaskSubInschoolExportData.setAreaName(areaGroup.getGroupName());
                    }
                    AgentGroup cityGroup = groupRoleMap.get(AgentGroupRoleType.City);
                    if (cityGroup != null) {
                        agentTaskSubInschoolExportData.setCityName(cityGroup.getGroupName());
                    }
                }

                agentTaskSubInschoolExportData.setSchoolId(item.getSchoolId());
                agentTaskSubInschoolExportData.setSchoolName(item.getSchoolName());
                agentTaskSubInschoolExportData.setTeacherId(item.getTeacherId());
                agentTaskSubInschoolExportData.setTeacherName(item.getTeacherName());

                //设置老师科目
                agentTaskSubInschoolExportData.setSubject(ConversionUtils.toString(teacherSubjectMap.get(item.getTeacherId())));

                //设置省、市、区
                CrmSchoolSummary schoolSummary = schoolSummaryMap.get(item.getSchoolId());
                if (schoolSummary != null) {
                    agentTaskSubInschoolExportData.setProvince(schoolSummary.getProvinceName());
                    agentTaskSubInschoolExportData.setCity(schoolSummary.getCityName());
                    agentTaskSubInschoolExportData.setCounty(schoolSummary.getCountyName());
                }

                if (null != item.getIsIntoSchool() && item.getIsIntoSchool()
                        && null != item.getIntoSchoolTime() && item.getIntoSchoolTime().compareTo(createTime) >= 0 && item.getIntoSchoolTime().compareTo(endTime) <= 0) {
                    agentTaskSubInschoolExportData.setIsIntoSchool(true);
                } else {
                    agentTaskSubInschoolExportData.setIsIntoSchool(false);
                }

                if (null != item.getIsVisitTeacher() && item.getIsVisitTeacher()
                        && null != item.getVisitTeacherTime() && item.getVisitTeacherTime().compareTo(createTime) >= 0 && item.getVisitTeacherTime().compareTo(endTime) <= 0) {
                    agentTaskSubInschoolExportData.setIsVisitTeacher(true);
                } else {
                    agentTaskSubInschoolExportData.setIsVisitTeacher(false);
                }

                if (null != item.getIsHomework() && item.getIsHomework()
                        && null != item.getHomeworkTime() && item.getHomeworkTime().compareTo(createTime) >= 0 && item.getHomeworkTime().compareTo(endTime) <= 0) {
                    agentTaskSubInschoolExportData.setIsHomework(true);
                } else {
                    agentTaskSubInschoolExportData.setIsHomework(false);
                }

                dataList.add(agentTaskSubInschoolExportData);

            }
        });
        return dataList;
    }

    /**
     * 天玑-获取任务列表(个人或者团队)
     *
     * @param userId
     * @param type   1:个人 2：团队
     * @return
     */
    public List<AgentTaskMainVO> getMainTaskList(Long userId, Integer type) {

        List<AgentTaskMain> taskMainList = agentTaskMainDao.loadByDate(null);
        if (CollectionUtils.isEmpty(taskMainList)) {
            return new ArrayList<>();
        }
        Set<String> mainTaskIds = taskMainList.stream().map(AgentTaskMain::getId).collect(Collectors.toSet());

        List<Long> userIds = new ArrayList<>();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);

        //个人
        if (Objects.equals(type, AgentTaskMain.PERSONAL)) {
            userIds.add(userId);
            //团队
        } else if (Objects.equals(type, AgentTaskMain.TEAM) && userRole != AgentRoleType.BusinessDeveloper) {
            List<AgentGroupUser> agentGroupUserList = baseOrgService.getGroupUserByUser(userId);
            if (CollectionUtils.isNotEmpty(agentGroupUserList)) {
                //部门下所有用户
                List<AgentGroupUser> managedUserIdsUsers = baseOrgService.getAllGroupUsersByGroupId(agentGroupUserList.get(0).getGroupId());
                //部门下所有用户ID
                userIds.addAll(managedUserIdsUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList()));
            }
        }
        List<AgentTaskMainVO> dataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userIds)) {
            /*
            进校维护老师
             */
            //主任务子任务对应map
            Map<String, List<AgentTaskSubIntoSchool>> taskSubIntoSchoolMap = agentTaskSubIntoSchoolDao.loadByMainTaskIds(mainTaskIds);
            Map<String, List<AgentTaskSubIntoSchool>> taskSubIntoSchoolMapNew = new HashMap<>();
            taskSubIntoSchoolMap.forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    List<AgentTaskSubIntoSchool> taskSubIntoSchoolList = v.stream().filter(item -> null != item && userIds.contains(item.getOperatorId())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(taskSubIntoSchoolList)) {
                        taskSubIntoSchoolMapNew.put(k, taskSubIntoSchoolList);
                    }
                }
            });

            if (CollectionUtils.isNotEmpty(taskSubIntoSchoolMapNew.keySet())) {
                //主任务map
                Map<String, AgentTaskMain> agentTaskMainMap = agentTaskMainDao.loadByIds(taskSubIntoSchoolMapNew.keySet());
                taskSubIntoSchoolMapNew.forEach((k, v) -> {
                    AgentTaskMainVO taskMainVO = new AgentTaskMainVO();
                    //主任务
                    if (agentTaskMainMap.containsKey(k)) {
                        AgentTaskMain taskMain = agentTaskMainMap.get(k);
                        taskMainVO.setMainTaskId(k);
                        taskMainVO.setEndTime(taskMain.getEndTime());
                        taskMainVO.setTaskType(taskMain.getTaskType());
                        taskMainVO.setTitle(taskMain.getTitle());
                        taskMainVO.setComment(taskMain.getComment());
                    }
                    if (taskSubIntoSchoolMapNew.containsKey(k)) {
                        //主任务下属于该人员的所有子任务
                        List<AgentTaskSubIntoSchool> taskSubIntoSchoolList = taskSubIntoSchoolMapNew.get(k);

                        Set<Long> allSchoolIds = taskSubIntoSchoolList.stream().map(AgentTaskSubIntoSchool::getSchoolId).collect(Collectors.toSet());
                        //主任务下该人员已进校的学校数
                        Set<Long> intoSchoolIds = taskSubIntoSchoolList.stream().filter(item -> null != item && null != item.getIsIntoSchool() && item.getIsIntoSchool()).map(AgentTaskSubIntoSchool::getSchoolId).collect(Collectors.toSet());

                        //已进校数目
                        taskMainVO.setFinishedNum(intoSchoolIds.size());
                        //属于该人员的学校数
                        taskMainVO.setAllNum(allSchoolIds.size());
                        //状态
                        taskMainVO.setStatus(allSchoolIds.size() == intoSchoolIds.size() ? "finished" : "unfinished");
                    }
                    dataList.add(taskMainVO);
                });
            }

            /*
            线上维护老师
             */
            //主任务子任务对应map
            Map<String, List<AgentTaskSubOnline>> taskSubOnlineMap = agentTaskSubOnlineDao.loadByMainTaskIds(mainTaskIds);
            Map<String, List<AgentTaskSubOnline>> taskSubOnlineMapNew = new HashMap<>();
            taskSubOnlineMap.forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    List<AgentTaskSubOnline> taskSubOnlineList = v.stream().filter(item -> null != item && userIds.contains(item.getOperatorId())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(taskSubOnlineList)) {
                        taskSubOnlineMapNew.put(k, taskSubOnlineList);
                    }
                }
            });

            if (CollectionUtils.isNotEmpty(taskSubOnlineMapNew.keySet())) {
                //主任务map
                Map<String, AgentTaskMain> taskMainMap = agentTaskMainDao.loadByIds(taskSubOnlineMapNew.keySet());

                taskSubOnlineMapNew.forEach((k, v) -> {
                    AgentTaskMainVO taskMainVO = new AgentTaskMainVO();
                    //主任务
                    if (taskMainMap.containsKey(k)) {
                        AgentTaskMain taskMain = taskMainMap.get(k);
                        taskMainVO.setMainTaskId(k);
                        taskMainVO.setEndTime(taskMain.getEndTime());
                        taskMainVO.setTaskType(taskMain.getTaskType());
                        taskMainVO.setTitle(taskMain.getTitle());
                        taskMainVO.setComment(taskMain.getComment());
                    }
                    if (taskSubOnlineMapNew.containsKey(k)) {
                        //主任务下属于该人员的所有子任务
                        List<AgentTaskSubOnline> taskSubOnlineListSub = taskSubOnlineMapNew.get(k);

                        Set<Long> allTeacherIds = taskSubOnlineListSub.stream().map(AgentTaskSubOnline::getTeacherId).collect(Collectors.toSet());
                        //主任务下该人员已维护的老师数
                        Set<Long> feedbackTeacherIds = taskSubOnlineListSub.stream().filter(item -> null != item && null != item.getIsFeedback() && item.getIsFeedback()).map(AgentTaskSubOnline::getTeacherId).collect(Collectors.toSet());

                        //已维护老师数目
                        taskMainVO.setFinishedNum(feedbackTeacherIds.size());
                        //属于该人员的老师数
                        taskMainVO.setAllNum(allTeacherIds.size());
                        //状态
                        taskMainVO.setStatus(allTeacherIds.size() == feedbackTeacherIds.size() ? "finished" : "unfinished");
                    }
                    dataList.add(taskMainVO);
                });
            }
        }
        return dataList;
    }

    /**
     * 线上维护老师列表
     *
     * @param mainTaskId
     * @param userId
     * @return
     */
    public Map<String, Object> subTaskOnlineList(String mainTaskId, Long userId) {

        //线上维护老师子任务
        List<AgentTaskSubOnline> taskSubOnlineList = agentTaskSubOnlineDao.loadByMainTaskId(mainTaskId);
        taskSubOnlineList = taskSubOnlineList.stream().filter(item -> null != item && Objects.equals(item.getOperatorId(), userId)).collect(Collectors.toList());

        List<Long> teacherIds = taskSubOnlineList.stream().filter(item -> null != item).map(AgentTaskSubOnline::getTeacherId).collect(Collectors.toList());
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);

        //未维护
        List<Map<String, Object>> unFeedbackMapList = new ArrayList<>();
        List<AgentTaskSubOnline> unFeedbackList = taskSubOnlineList.stream().filter(item -> null != item && (null == item.getIsFeedback() || item.getIsFeedback() == false)).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(unFeedbackList)) {
            unFeedbackList.forEach(item -> {
                Map<String, Object> dataMap = new HashMap<>();
                if (null != item) {
                    dataMap.put("teacherId", item.getTeacherId());
                    dataMap.put("id", item.getId());
                    if (teacherMap.containsKey(item.getTeacherId())) {
                        Teacher teacher = teacherMap.get(item.getTeacherId());
                        dataMap.put("authState", teacher.getAuthenticationState());
                        dataMap.put("subject", teacher.getSubject());
                        dataMap.put("subjectName", teacher.getSubject() == null ? "" : teacher.getSubject().getValue());
                    }
                    dataMap.put("isFeedback", item.getIsFeedback());
                    dataMap.put("teacherName", item.getTeacherName());
                    dataMap.put("schoolName", item.getSchoolName());
                    dataMap.put("feedbackType", item.getFeedbackType() == null ? "" : item.getFeedbackType().getValue());
                    dataMap.put("feedbackResult", item.getFeedbackResult());
                    dataMap.put("isHomework", item.getIsHomework());
                    unFeedbackMapList.add(dataMap);
                }
            });
        }


        //已维护
        List<Map<String, Object>> feedbackMapList = new ArrayList<>();
        List<AgentTaskSubOnline> feedbackList = taskSubOnlineList.stream().filter(item -> null != item && null != item.getIsFeedback() && item.getIsFeedback()).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(feedbackList)) {
            feedbackList.forEach(item -> {
                Map<String, Object> dataMap = new HashMap<>();
                if (null != item) {
                    dataMap.put("id", item.getId());
                    dataMap.put("teacherId", item.getTeacherId());
                    if (teacherMap.containsKey(item.getTeacherId())) {
                        Teacher teacher = teacherMap.get(item.getTeacherId());
                        dataMap.put("authState", teacher.getAuthenticationState());
                        dataMap.put("subject", teacher.getSubject());
                        dataMap.put("subjectName", teacher.getSubject() == null ? "" : teacher.getSubject().getValue());
                    }
                    dataMap.put("isFeedback", item.getIsFeedback());
                    dataMap.put("teacherName", item.getTeacherName());
                    dataMap.put("schoolName", item.getSchoolName());
                    dataMap.put("feedbackType", item.getFeedbackType() == null ? "" : item.getFeedbackType().getValue());
                    dataMap.put("feedbackResult", item.getFeedbackResult());
                    dataMap.put("isHomework", item.getIsHomework());
                    feedbackMapList.add(dataMap);
                }
            });
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("unFeedbackList", unFeedbackMapList);
        dataMap.put("feedbackList", feedbackMapList);
        return dataMap;
    }


    /**
     * 进校维护老师列表
     *
     * @param mainTaskId
     * @param userId
     * @return
     */
    public Map<String, Object> subTaskIntoSchoolList(String mainTaskId, Long userId) {
        //进校维护老师子任务列表
        List<AgentTaskSubIntoSchool> taskSubIntoSchools = agentTaskSubIntoSchoolDao.loadByMainTaskId(mainTaskId);
        taskSubIntoSchools = taskSubIntoSchools.stream().filter(item -> null != item && Objects.equals(item.getOperatorId(), userId)).collect(Collectors.toList());
        //获取老师信息
        List<Long> teacherIds = taskSubIntoSchools.stream().filter(item -> null != item).map(AgentTaskSubIntoSchool::getTeacherId).collect(Collectors.toList());
        Map<Long, Teacher> teacherListMap = teacherLoaderClient.loadTeachers(teacherIds);

        //未进校
        List<AgentTaskSubIntoSchool> unIntoSchoolList = taskSubIntoSchools.stream().filter(item -> null != item && (null == item.getIsIntoSchool() || item.getIsIntoSchool() == false)).collect(Collectors.toList());
        Map<Long, List<AgentTaskSubIntoSchool>> unIntoSchoolMap = unIntoSchoolList.stream().filter(item -> null != item).collect(Collectors.groupingBy(AgentTaskSubIntoSchool::getSchoolId));

        List<Map<String, Object>> unIntoSchoolMapList = new ArrayList<>();
        unIntoSchoolMap.forEach((schoolId, taskSubIntoSchoolList) -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("schoolId", schoolId);
            dataMap.put("schoolName", taskSubIntoSchoolList.get(0).getSchoolName());
            List<Map<String, Object>> teacherList = new ArrayList<>();
            taskSubIntoSchoolList.forEach(taskSubIntoSchool -> {
                Map<String, Object> teacherMap = new HashMap<>();
                if (null != taskSubIntoSchool) {
                    teacherMap.put("teacherId", taskSubIntoSchool.getTeacherId());
                    teacherMap.put("teacherName", taskSubIntoSchool.getTeacherName());
                    teacherMap.put("isVisitTeacher", taskSubIntoSchool.getIsVisitTeacher());
                    teacherMap.put("isHomework", taskSubIntoSchool.getIsHomework());
                    if (teacherListMap.containsKey(taskSubIntoSchool.getTeacherId())) {
                        Teacher teacher = teacherListMap.get(taskSubIntoSchool.getTeacherId());
                        teacherMap.put("authState", teacher.getAuthenticationState());
                        teacherMap.put("subject", teacher.getSubject());
                        teacherMap.put("subjectName", teacher.getSubject() == null ? "" : teacher.getSubject().getValue());
                    }
                    teacherList.add(teacherMap);
                }
            });
            dataMap.put("teacherList", teacherList);
            unIntoSchoolMapList.add(dataMap);
        });

        //已进校
        List<Map<String, Object>> intoSchoolMapList = new ArrayList<>();
        List<AgentTaskSubIntoSchool> intoSchoolList = taskSubIntoSchools.stream().filter(item -> null != item && null != item.getIsIntoSchool() && item.getIsIntoSchool()).collect(Collectors.toList());
        Map<Long, List<AgentTaskSubIntoSchool>> intoSchoolMap = intoSchoolList.stream().filter(item -> null != item).collect(Collectors.groupingBy(AgentTaskSubIntoSchool::getSchoolId));

        intoSchoolMap.forEach((schoolId, taskSubIntoSchoolList) -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("schoolId", schoolId);
            dataMap.put("schoolName", taskSubIntoSchoolList.get(0).getSchoolName());
            dataMap.put("intoSchoolTime", null != taskSubIntoSchoolList.get(0).getIntoSchoolTime() ? DateUtils.dateToString(taskSubIntoSchoolList.get(0).getIntoSchoolTime(), "MM-dd") : "");
            List<Map<String, Object>> teacherList = new ArrayList<>();
            taskSubIntoSchoolList.forEach(taskSubIntoSchool -> {
                Map<String, Object> teacherMap = new HashMap<>();
                if (null != taskSubIntoSchool) {
                    teacherMap.put("teacherId", taskSubIntoSchool.getTeacherId());
                    teacherMap.put("teacherName", taskSubIntoSchool.getTeacherName());
                    teacherMap.put("isVisitTeacher", taskSubIntoSchool.getIsVisitTeacher());
                    teacherMap.put("isHomework", taskSubIntoSchool.getIsHomework());
                    if (teacherListMap.containsKey(taskSubIntoSchool.getTeacherId())) {
                        Teacher teacher = teacherListMap.get(taskSubIntoSchool.getTeacherId());
                        teacherMap.put("authState", teacher.getAuthenticationState());
                        teacherMap.put("subject", teacher.getSubject());
                        teacherMap.put("subjectName", teacher.getSubject() == null ? "" : teacher.getSubject().getValue());
                    }
                    teacherList.add(teacherMap);
                }
            });
            dataMap.put("teacherList", teacherList);
            intoSchoolMapList.add(dataMap);
        });

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("unIntoSchoolList", unIntoSchoolMapList);
        dataMap.put("intoSchoolList", intoSchoolMapList);
        return dataMap;
    }


    /**
     * 检验参数组合
     *
     * @param groupRoleType
     * @param dimension
     * @return
     */
    public boolean judgeGroupDimension(AgentGroupRoleType groupRoleType, Integer dimension) {
        boolean result = false;
        //全国或业务部的情况
        if (groupRoleType == AgentGroupRoleType.Country || groupRoleType == AgentGroupRoleType.BusinessUnit) {
            if (dimension == 1 || dimension == 2 || dimension == 3 || dimension == 4 || dimension == 5) {
                result = true;
            }
            // 大区的情况
        } else if (groupRoleType == AgentGroupRoleType.Region) {
            if (dimension == 1 || dimension == 3 || dimension == 4 || dimension == 5) {
                result = true;
            }
            // 区域的情况
        } else if (groupRoleType == AgentGroupRoleType.Area) {
            if (dimension == 1 || dimension == 4 || dimension == 5) {
                result = true;
            }
            //分区的情况
        } else if (groupRoleType == AgentGroupRoleType.City) {
            if (dimension == 1 || dimension == 5) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 获取该部门dimensions
     *
     * @param groupId
     * @return
     */
    public List<Map<String, Object>> fetchDimensionList(Long groupId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> defaultItem = new HashMap<>();
        defaultItem.put("code", 1);
        defaultItem.put("desc", "默认");
        resultList.add(defaultItem);

        List<AgentGroup> groupList = new ArrayList<>();
        groupList.addAll(baseOrgService.getSubGroupList(groupId));
        Set<AgentGroupRoleType> groupRoleTypes = groupList.stream().map(AgentGroup::fetchGroupRoleType)
                .filter(p -> p == AgentGroupRoleType.Region || p == AgentGroupRoleType.Area || p == AgentGroupRoleType.City)
                .collect(Collectors.toSet());

        if (groupRoleTypes.contains(AgentGroupRoleType.Region)) {  // 业务部或大区
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("code", 2);
            itemMap.put("desc", "大区");
            resultList.add(itemMap);
        }
        if (groupRoleTypes.contains(AgentGroupRoleType.Area)) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("code", 3);
            itemMap.put("desc", "区域");
            resultList.add(itemMap);
        }
        if (groupRoleTypes.contains(AgentGroupRoleType.City)) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("code", 4);
            itemMap.put("desc", "分区");
            resultList.add(itemMap);
        }

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("code", 5);
        itemMap.put("desc", "专员");
        resultList.add(itemMap);


        return resultList;
    }


    /**
     * 专员任务数据统计
     *
     * @param mainTaskId
     * @param groupId
     * @param taskType
     * @return
     */
    public List<Map<String, Object>> subTaskDataUserList(String mainTaskId, Long groupId, AgentTaskType taskType) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        //获取该部门及子部门中指定角色的用户
        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
        AgentGroup group = baseOrgService.getGroupById(groupId);
        List<Long> userList = new ArrayList<>();
        // 如果部门级别是全国 1.过滤出直接子部门中的小学业务部；2.过滤出小学业务部中的市场部；3.获取市场部下面的专员
        if (groupRoleType == AgentGroupRoleType.Country) {
            List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(groupId);
            subGroupList.forEach(p -> {
                if (p.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit && StringUtils.contains(p.getGroupName(), "小学")) {
                    List<AgentGroup> subSubGroupList = baseOrgService.getGroupListByParentId(p.getId());
                    subSubGroupList.forEach(item -> {
                        if (item.fetchGroupRoleType() == AgentGroupRoleType.Marketing) {
                            userList.addAll(baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(item.getId(), AgentRoleType.BusinessDeveloper.getId()));
                        }
                    });
                }
            });
            // 如果部门级别是业务部 1.过滤出直接子部门中的市场部；2.获取市场部下面的专员
        } else if (groupRoleType == AgentGroupRoleType.BusinessUnit && StringUtils.contains(group.getGroupName(), "小学")) {
            List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(groupId);
            AgentGroup targetGroup = subGroupList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).findFirst().orElse(null);
            if (targetGroup != null) {
                userList.addAll(baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(targetGroup.getId(), AgentRoleType.BusinessDeveloper.getId()));
            }
        } else {
            userList.addAll(baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId()));
        }
        Map<Long, AgentUser> userMap = baseOrgService.getUsers(userList).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
        //线上维护老师
        if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER) {
            //执行人ID与子任务列表对应map
            Map<Long, List<AgentTaskSubOnline>> operatorIdTaskSubOnlineMap = agentTaskSubOnlineDao.loadByMainTaskId(mainTaskId)
                    .stream().filter(item -> null != item && userList.contains(item.getOperatorId())).collect(Collectors.groupingBy(AgentTaskSubOnline::getOperatorId));
            userList.forEach(userId -> {
                Map<String, Object> dataMap = new HashMap<>();
                //如果存在该专员
                if (operatorIdTaskSubOnlineMap.containsKey(userId)) {
                    List<AgentTaskSubOnline> taskSubOnlineList = operatorIdTaskSubOnlineMap.get(userId);
                    if (CollectionUtils.isNotEmpty(taskSubOnlineList)) {
                        //老师数
                        int teacherNum = taskSubOnlineList.stream().map(AgentTaskSubOnline::getTeacherId).collect(Collectors.toSet()).size();
                        //已维护老师数
                        int feedbackTeacherNum = taskSubOnlineList.stream().filter(item -> null != item && null != item.getIsFeedback() && item.getIsFeedback()).map(AgentTaskSubOnline::getTeacherId).collect(Collectors.toSet()).size();
                        //布置作业老师数
                        int homeworkTeacherNum = taskSubOnlineList.stream().filter(item -> null != item && null != item.getIsHomework() && item.getIsHomework()).map(AgentTaskSubOnline::getTeacherId).collect(Collectors.toSet()).size();
                        dataMap.put("teacherNum", teacherNum);
                        dataMap.put("feedbackTeacherNum", feedbackTeacherNum);
                        //维护率
                        dataMap.put("feedbackTeacherRate", MathUtils.doubleDivide(feedbackTeacherNum * 100, teacherNum));
                        //布置率
                        dataMap.put("homeworkTeacherRate", MathUtils.doubleDivide(homeworkTeacherNum * 100, teacherNum));
                    }
                } else {
                    dataMap.put("teacherNum", 0);
                    dataMap.put("feedbackTeacherNum", 0);
                    dataMap.put("feedbackTeacherRate", 0);
                    dataMap.put("homeworkTeacherRate", 0);
                }
                //专员ID
                dataMap.put("userId", userId);
                //专员姓名
                AgentUser agentUser = userMap.get(userId);
                if (null != agentUser) {
                    dataMap.put("userName", agentUser.getRealName());
                }
                dataMap.put("clickable", false);
                dataList.add(dataMap);
            });
            //进校维护老师
        } else if (taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER) {
            List<AgentTaskSubIntoSchool> agentTaskSubIntoSchoolList = agentTaskSubIntoSchoolDao.loadByMainTaskId(mainTaskId);
            //执行人ID与子任务列表对应map
            Map<Long, List<AgentTaskSubIntoSchool>> operatorIdTaskSubIntoSchoolMap = agentTaskSubIntoSchoolList
                    .stream().filter(item -> null != item && userList.contains(item.getOperatorId())).collect(Collectors.groupingBy(AgentTaskSubIntoSchool::getOperatorId));
            userList.forEach(userId -> {
                Map<String, Object> dataMap = new HashMap<>();
                //如果存在该专员
                if (operatorIdTaskSubIntoSchoolMap.containsKey(userId)) {
                    List<AgentTaskSubIntoSchool> taskSubIntoSchoolList = operatorIdTaskSubIntoSchoolMap.get(userId);
                    if (CollectionUtils.isNotEmpty(taskSubIntoSchoolList)) {
                        //学校数
                        int schoolNum = taskSubIntoSchoolList.stream().filter(item -> null != item).map(AgentTaskSubIntoSchool::getSchoolId).collect(Collectors.toSet()).size();
                        //进校数
                        int intoSchoolNum = taskSubIntoSchoolList.stream().filter(item -> null != item && null != item.getIsIntoSchool() && item.getIsIntoSchool()).map(AgentTaskSubIntoSchool::getSchoolId).collect(Collectors.toSet()).size();
                        //老师数
                        int teacherNum = taskSubIntoSchoolList.stream().map(AgentTaskSubIntoSchool::getTeacherId).collect(Collectors.toSet()).size();
                        //已拜访老师数
                        int visitTeacherNum = taskSubIntoSchoolList.stream().filter(item -> null != item && null != item.getIsVisitTeacher() && item.getIsVisitTeacher()).map(AgentTaskSubIntoSchool::getTeacherId).collect(Collectors.toSet()).size();
                        //布置作业老师数
                        int homeworkTeacherNum = taskSubIntoSchoolList.stream().filter(item -> null != item && null != item.getIsHomework() && item.getIsHomework()).map(AgentTaskSubIntoSchool::getTeacherId).collect(Collectors.toSet()).size();
                        dataMap.put("schoolNum", schoolNum);
                        //进校率
                        dataMap.put("intoSchoolRate", MathUtils.doubleDivide(intoSchoolNum * 100, schoolNum));
                        dataMap.put("teacherNum", teacherNum);
                        //拜访率
                        dataMap.put("visitTeacherRate", MathUtils.doubleDivide(visitTeacherNum * 100, teacherNum));
                        //布置率
                        dataMap.put("homeworkTeacherRate", MathUtils.doubleDivide(homeworkTeacherNum * 100, teacherNum));
                    }
                } else {
                    dataMap.put("schoolNum", 0);
                    dataMap.put("intoSchoolRate", 0);
                    dataMap.put("teacherNum", 0);
                    dataMap.put("visitTeacherRate", 0);
                    dataMap.put("homeworkTeacherRate", 0);
                }
                //专员ID
                dataMap.put("userId", userId);
                //专员姓名
                AgentUser agentUser = userMap.get(userId);
                if (null != agentUser) {
                    dataMap.put("userName", agentUser.getRealName());
                }
                dataMap.put("clickable", false);
                dataList.add(dataMap);
            });
        }
        return dataList;
    }


    /**
     * 获取符合的部门
     *
     * @param groupId
     * @param dimension
     * @return
     */
    public Collection<Long> fetchGroupList(Long groupId, Integer dimension) {
        List<AgentGroup> groupList = new ArrayList<>();
        //部门级别
        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
        AgentGroup group = baseOrgService.getGroupById(groupId);
        // 默认情况下
        if (dimension == 1) {
            // 获取直接子部门
            List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(groupId);
            // 如果部门级别是全国 1.过滤出直接子部门中的小学业务部；2.过滤出小学业务部中的市场部；3.获取市场部下面的直接子部门
            if (groupRoleType == AgentGroupRoleType.Country) {
                subGroupList.forEach(p -> {
                    if (p.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit && StringUtils.contains(p.getGroupName(), "小学")) {
                        List<AgentGroup> subSubGroupList = baseOrgService.getGroupListByParentId(p.getId());
                        subSubGroupList.forEach(item -> {
                            if (item.fetchGroupRoleType() == AgentGroupRoleType.Marketing) {
                                groupList.addAll(baseOrgService.getGroupListByParentId(item.getId()));
                            }
                        });
                    }
                });
                // 如果部门级别是小学业务部 1.过滤出直接子部门中的市场部；2.获取市场部下面的直接子部门
            } else if (groupRoleType == AgentGroupRoleType.BusinessUnit && StringUtils.contains(group.getGroupName(), "小学")) {
                subGroupList.forEach(p -> {
                    if (p.fetchGroupRoleType() == AgentGroupRoleType.Marketing) {
                        groupList.addAll(baseOrgService.getGroupListByParentId(p.getId()));
                    }
                });
            } else {
                groupList.addAll(subGroupList);
            }
        } else {
            List<AgentGroup> allSubGroupList = new ArrayList<>();
            List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(groupId);
            // 如果部门级别是全国 1.过滤出直接子部门中的小学业务部；2.过滤出小学业务部下的市场部；3.获取市场部下面的所有子部门
            if (groupRoleType == AgentGroupRoleType.Country) {
                subGroupList.forEach(p -> {
                    if (p.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit && StringUtils.contains(p.getGroupName(), "小学")) {
                        List<AgentGroup> subSubGroupList = baseOrgService.getGroupListByParentId(p.getId());
                        subSubGroupList.forEach(item -> {
                            if (item.fetchGroupRoleType() == AgentGroupRoleType.Marketing) {
                                allSubGroupList.addAll(baseOrgService.getSubGroupList(item.getId()));
                            }
                        });
                    }
                });
                // 如果部门级别是小学业务部 1.过滤出直接子部门中的市场部；2.获取市场部下面的所有子部门
            } else if (groupRoleType == AgentGroupRoleType.BusinessUnit && StringUtils.contains(group.getGroupName(), "小学")) {
                subGroupList.forEach(item -> {
                    if (item.fetchGroupRoleType() == AgentGroupRoleType.Marketing) {
                        allSubGroupList.addAll(baseOrgService.getSubGroupList(item.getId()));
                    }
                });
            } else {
                // 获取指定部门下面的所有子部门
                allSubGroupList.addAll(baseOrgService.getSubGroupList(groupId));
            }

            AgentGroupRoleType targetGroupRole = null;
            if (dimension == 2) {
                targetGroupRole = AgentGroupRoleType.Region;
            } else if (dimension == 3) {
                targetGroupRole = AgentGroupRoleType.Area;
            } else if (dimension == 4) {
                targetGroupRole = AgentGroupRoleType.City;
            }
            for (AgentGroup p : allSubGroupList) {
                if (p.fetchGroupRoleType() == targetGroupRole) {
                    groupList.add(p);
                }
            }
        }
        return groupList.stream().map(AgentGroup::getId).collect(Collectors.toSet());
    }

    /**
     * 部门任务数据统计
     *
     * @param mainTaskId
     * @param groupId
     * @param taskType
     * @param dimension
     * @return
     */
    public List<Map<String, Object>> subTaskDataGroupList(String mainTaskId, Long groupId, AgentTaskType taskType, Integer dimension) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        //获取符合条件的部门
        Collection<Long> groupIdList = fetchGroupList(groupId, dimension);
        Map<Long, AgentGroup> groupMap = baseOrgService.getGroupByIds(groupIdList).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
        //多个部门下所有人员
        Set<Long> allUserIds = new HashSet<>();
        //部门与人员关系map
        Map<Long, List<Long>> groupUserIdMap = new HashMap<>();
        groupIdList.forEach(item -> {
            //获取该部门下的所有子部门
            Set<Long> subGroupIds = baseOrgService.getSubGroupList(item).stream().map(AgentGroup::getId).collect(Collectors.toSet());
            //子部门+本部门
            subGroupIds.add(item);
            //获取本部门+所有子部门中的所有人员
            List<Long> userIds = baseOrgService.getGroupUserByGroups(subGroupIds).stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            groupUserIdMap.put(item, userIds);

            allUserIds.addAll(userIds);
        });

        //线上维护老师
        if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER) {
            //执行人ID与子任务列表对应map
            Map<Long, List<AgentTaskSubOnline>> operatorIdTaskSubOnlineMap = agentTaskSubOnlineDao.loadByMainTaskId(mainTaskId)
                    .stream().filter(item -> null != item && allUserIds.contains(item.getOperatorId())).collect(Collectors.groupingBy(AgentTaskSubOnline::getOperatorId));

            groupIdList.forEach(item -> {
                Map<String, Object> dataMap = new HashMap<>();
                int teacherNum = 0;         //老师数
                int feedbackTeacherNum = 0; //已维护老师数
                int homeworkTeacherNum = 0; //布置作业老师数
                if (groupUserIdMap.containsKey(item)) {
                    //获取部门下的人员ids
                    List<Long> userIds = groupUserIdMap.get(item);
                    for (int i = 0; i < userIds.size(); i++) {
                        //获取每个人员的任务
                        if (operatorIdTaskSubOnlineMap.containsKey(userIds.get(i))) {
                            List<AgentTaskSubOnline> taskSubOnlineList = operatorIdTaskSubOnlineMap.get(userIds.get(i));

                            teacherNum += taskSubOnlineList.stream().map(AgentTaskSubOnline::getTeacherId).collect(Collectors.toSet()).size();

                            feedbackTeacherNum += taskSubOnlineList.stream().filter(p -> null != p && null != p.getIsFeedback() && p.getIsFeedback()).map(AgentTaskSubOnline::getTeacherId).collect(Collectors.toSet()).size();

                            homeworkTeacherNum += taskSubOnlineList.stream().filter(p -> null != p && null != p.getIsHomework() && p.getIsHomework()).map(AgentTaskSubOnline::getTeacherId).collect(Collectors.toSet()).size();
                        }
                    }
                }
                //老师数
                dataMap.put("teacherNum", teacherNum);
                //已维护老师数
                dataMap.put("feedbackTeacherNum", feedbackTeacherNum);
                //维护率
                dataMap.put("feedbackTeacherRate", MathUtils.doubleDivide(feedbackTeacherNum * 100, teacherNum));
                //布置率
                dataMap.put("homeworkTeacherRate", MathUtils.doubleDivide(homeworkTeacherNum * 100, teacherNum));

                //部门ID
                dataMap.put("groupId", item);
                //部门名称
                AgentGroup group = groupMap.get(item);
                if (null != group) {
                    dataMap.put("groupName", group.getGroupName());
                }
                dataMap.put("clickable", true);
                dataList.add(dataMap);
            });
            //进校维护老师
        } else if (taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER) {
            List<AgentTaskSubIntoSchool> agentTaskSubIntoSchoolList = agentTaskSubIntoSchoolDao.loadByMainTaskId(mainTaskId);
            //执行人ID与子任务列表对应map
            Map<Long, List<AgentTaskSubIntoSchool>> operatorIdTaskSubIntoSchoolMap = agentTaskSubIntoSchoolList
                    .stream().filter(item -> null != item && allUserIds.contains(item.getOperatorId())).collect(Collectors.groupingBy(AgentTaskSubIntoSchool::getOperatorId));

            groupIdList.forEach(item -> {
                Map<String, Object> dataMap = new HashMap<>();
                int schoolNum = 0;//学校数
                int intoSchoolNum = 0;//进校数
                int teacherNum = 0;         //老师数
                int visitTeacherNum = 0; //已拜访老师数
                int homeworkTeacherNum = 0; //布置作业老师数
                if (groupUserIdMap.containsKey(item)) {
                    //获取部门下的人员ids
                    List<Long> userIds = groupUserIdMap.get(item);
                    for (int i = 0; i < userIds.size(); i++) {
                        //获取每个人员的任务
                        if (operatorIdTaskSubIntoSchoolMap.containsKey(userIds.get(i))) {
                            List<AgentTaskSubIntoSchool> taskSubIntoSchoolList = operatorIdTaskSubIntoSchoolMap.get(userIds.get(i));

                            schoolNum += taskSubIntoSchoolList.stream().filter(p -> null != p).map(AgentTaskSubIntoSchool::getSchoolId).collect(Collectors.toSet()).size();

                            intoSchoolNum += taskSubIntoSchoolList.stream().filter(p -> null != p && null != p.getIsIntoSchool() && p.getIsIntoSchool()).map(AgentTaskSubIntoSchool::getSchoolId).collect(Collectors.toSet()).size();

                            teacherNum += taskSubIntoSchoolList.stream().map(AgentTaskSubIntoSchool::getTeacherId).collect(Collectors.toSet()).size();

                            visitTeacherNum += taskSubIntoSchoolList.stream().filter(p -> null != p && null != p.getIsVisitTeacher() && p.getIsVisitTeacher()).map(AgentTaskSubIntoSchool::getTeacherId).collect(Collectors.toSet()).size();

                            homeworkTeacherNum += taskSubIntoSchoolList.stream().filter(p -> null != p && null != p.getIsHomework() && p.getIsHomework()).map(AgentTaskSubIntoSchool::getTeacherId).collect(Collectors.toSet()).size();
                        }
                    }
                }
                //学校数
                dataMap.put("schoolNum", schoolNum);
                //进校率
                dataMap.put("intoSchoolRate", MathUtils.doubleDivide(intoSchoolNum * 100, schoolNum));
                //老师数
                dataMap.put("teacherNum", teacherNum);
                //老师拜访率
                dataMap.put("visitTeacherRate", MathUtils.doubleDivide(visitTeacherNum * 100, teacherNum));
                //布置率
                dataMap.put("homeworkTeacherRate", MathUtils.doubleDivide(homeworkTeacherNum * 100, teacherNum));

                //部门ID
                dataMap.put("groupId", item);
                //部门名称
                AgentGroup group = groupMap.get(item);
                if (null != group) {
                    dataMap.put("groupName", group.getGroupName());
                }
                dataMap.put("clickable", true);
                dataList.add(dataMap);
            });
        }
        return dataList;
    }

    /**
     * 当前用户未完成任务数量
     *
     * @param userId
     * @return
     */
    public int unFinishedTaskNum(Long userId) {
        List<Long> userIds = new ArrayList<>();
        //获取用户角色
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        //专员
        if (userRole == AgentRoleType.BusinessDeveloper) {
            userIds.add(userId);
            //其他角色
        } else {
            Long groupId = baseOrgService.getGroupUserByUser(userId).stream().map(AgentGroupUser::getGroupId).findFirst().orElse(null);
            //获取该部门下的所有子部门
            Set<Long> subGroupIds = baseOrgService.getSubGroupList(groupId).stream().map(AgentGroup::getId).collect(Collectors.toSet());
            //子部门+本部门
            subGroupIds.add(groupId);
            //获取本部门+所有子部门中的所有人员
            userIds.addAll(baseOrgService.getGroupUserByGroups(subGroupIds).stream().map(AgentGroupUser::getUserId).collect(Collectors.toList()));
        }

        Set<String> mainTaskIds = agentTaskMainDao.loadByDate(null).stream().map(AgentTaskMain::getId).collect(Collectors.toSet());
        /*
        线上维护老师
         */
        List<AgentTaskSubOnline> taskSubOnlineList = new ArrayList<>();
        Map<String, List<AgentTaskSubOnline>> taskSubOnlineMap = agentTaskSubOnlineDao.loadByMainTaskIds(mainTaskIds);
        taskSubOnlineMap.forEach((k, v) -> {
            taskSubOnlineList.addAll(v);
        });
        //包含该执行人、未维护老师任务数量
        int unFinishedSubTaskOnlineNum = taskSubOnlineList
                .stream()
                .filter(item -> null != item && userIds.contains(item.getOperatorId()) && (null == item.getIsFeedback() || item.getIsFeedback() == false))
                .map(AgentTaskSubOnline::getMainTaskId).collect(Collectors.toSet()).size();

        /*
        进校维护老师
         */
        List<AgentTaskSubIntoSchool> taskSubIntoSchoolList = new ArrayList<>();
        Map<String, List<AgentTaskSubIntoSchool>> taskSubIntoSchoolMap = agentTaskSubIntoSchoolDao.loadByMainTaskIds(mainTaskIds);
        taskSubIntoSchoolMap.forEach((k, v) -> {
            taskSubIntoSchoolList.addAll(v);
        });
        //包括该执行人、未进校任务数量
        int unFinishedSubTaskIntoSchoolNum = taskSubIntoSchoolList
                .stream()
                .filter(item -> null != item && userIds.contains(item.getOperatorId()) && (null == item.getIsIntoSchool() || item.getIsIntoSchool() == false || null == item.getIsVisitTeacher() || item.getIsVisitTeacher() == false))
                .map(AgentTaskSubIntoSchool::getMainTaskId).collect(Collectors.toSet()).size();

        return (unFinishedSubTaskOnlineNum + unFinishedSubTaskIntoSchoolNum);
    }


    /**
     * 工作记录，设置任务进校维护老师
     *
     * @param userId
     * @param schoolId
     * @param teacherIds
     */
    public void setSubTaskIntoSchoolAndVisitTeacherForWorkRecord(Long userId, Long schoolId, Collection<Long> teacherIds) {
        //获取主任务
        Set<String> mainTaskIds = agentTaskMainDao.loadByDate(null).stream().filter(item -> null != item).map(AgentTaskMain::getId).collect(Collectors.toSet());
        //根据主任务，获取对应的进校维护老师子任务
        Map<String, List<AgentTaskSubIntoSchool>> taskSubIntoSchoolMap = agentTaskSubIntoSchoolDao.loadByMainTaskIds(mainTaskIds);
        List<AgentTaskSubIntoSchool> taskSubIntoSchoolList = new ArrayList<>();
        taskSubIntoSchoolMap.forEach((k, v) -> {
            taskSubIntoSchoolList.addAll(v);
        });
        //过滤出任务执行人为当前人员、进校为当前学校、没有进校的任务
        Set<String> filterTaskSubIntoSchoolIds = taskSubIntoSchoolList
                .stream()
                .filter(item -> null != item && Objects.equals(item.getOperatorId(), userId) && Objects.equals(item.getSchoolId(), schoolId)
                        && (null == item.getIsIntoSchool() || item.getIsIntoSchool() == false))
                .map(AgentTaskSubIntoSchool::getId).collect(Collectors.toSet());

        //过滤出任务执行人为当前人员、进校为当前学校、老师为当前老师、没有拜访老师的任务
        Set<String> filterTaskSubVisitTeacherIds = taskSubIntoSchoolList
                .stream()
                .filter(item -> null != item && Objects.equals(item.getOperatorId(), userId) && Objects.equals(item.getSchoolId(), schoolId) && teacherIds.contains(item.getTeacherId())
                        && (null == item.getIsVisitTeacher() || item.getIsVisitTeacher() == false))
                .map(AgentTaskSubIntoSchool::getId).collect(Collectors.toSet());

        //设置进校
        if (CollectionUtils.isNotEmpty(filterTaskSubIntoSchoolIds)) {
            agentTaskSubIntoSchoolDao.updateIntoSchool(filterTaskSubIntoSchoolIds);
        }
        //设置拜访老师
        if (CollectionUtils.isNotEmpty(filterTaskSubVisitTeacherIds)) {
            agentTaskSubIntoSchoolDao.updateIsVisitTeacher(filterTaskSubVisitTeacherIds);
        }
    }

    /**
     * 布置作业监听，设置任务老师布置作业
     *
     * @param teacherId
     */
    public void setSubTaskIsHomeworkForListener(Long teacherId) {
        //获取主任务
        Set<String> mainTaskIds = agentTaskMainDao.loadByDate(null).stream().filter(item -> null != item).map(AgentTaskMain::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(mainTaskIds)) {
            //线上维护老师任务
            agentTaskSubOnlineDao.updateIsHomework(mainTaskIds, teacherId);
            //进校维护老师任务
            agentTaskSubIntoSchoolDao.updateIsHomework(mainTaskIds, teacherId);
        }
    }

    /**
     * 首页，判断是否有权限显示维护老师
     *
     * @param userId
     * @return
     */
    public boolean showMaintainTeacher(Long userId) {
        Long groupId = baseOrgService.getGroupUserByUser(userId).stream().map(AgentGroupUser::getGroupId).findFirst().orElse(null);
        AgentGroupRoleType groupRole = baseOrgService.getGroupRole(groupId);
        AgentGroup group = baseOrgService.getGroupById(groupId);
        //如果全国
        if (groupRole == AgentGroupRoleType.Country) {
            return true;
            //如果是小学业务部
        } else if (groupRole == AgentGroupRoleType.BusinessUnit && StringUtils.contains(group.getGroupName(), "小学")) {
            return true;
        } else {
            if (isJuniorMarketing(groupId)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 判断本部门或者多层父级部门是否是小学市场
     *
     * @param groupId
     * @return
     */
    private boolean isJuniorMarketing(Long groupId) {
        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if (null == groupRoleType || null == group) {
            return false;
        }
        //如果是小学市场
        if (groupRoleType == AgentGroupRoleType.Marketing && StringUtils.contains(group.getGroupName(), "小学")) {
            return true;
        }
        Long parentGroupId = group.getParentId();
        if (null == parentGroupId) {
            return false;
        }
        return isJuniorMarketing(parentGroupId);
    }

}
