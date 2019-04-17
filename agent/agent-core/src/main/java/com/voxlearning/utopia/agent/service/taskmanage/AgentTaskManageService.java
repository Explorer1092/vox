package com.voxlearning.utopia.agent.service.taskmanage;


import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.dao.mongo.taskmanage.AgentMainTaskDao;
import com.voxlearning.utopia.agent.dao.mongo.taskmanage.AgentSubTaskDao;
import com.voxlearning.utopia.agent.persist.entity.memorandum.AgentMemorandum;
import com.voxlearning.utopia.agent.persist.entity.taskmanage.AgentMainTask;
import com.voxlearning.utopia.agent.persist.entity.taskmanage.AgentSubTask;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.memorandum.AgentMemorandumService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.teacher.TeacherBasicInfo;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserSchoolLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 任务管理service
 * @author deliang.che
 * @since  2018-11-13
 */
@Named
public class AgentTaskManageService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private AgentMainTaskDao agentMainTaskDao;
    @Inject
    private AgentSubTaskDao agentSubTaskDao;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentUserSchoolLoaderClient agentUserSchoolLoaderClient;
    @Inject
    private TeacherResourceService teacherResourceService;
    @Inject
    private AgentMemorandumService agentMemorandumService;
    /**
     * 检查Excel文件
     * @param workbook
     * @return
     */
    public MapMessage checkWorkBook(XSSFWorkbook workbook){
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if(sheet == null){
            return MapMessage.errorMessage();
        }

        List<AgentSubTask> subTaskList = new ArrayList<>();
        List<String> errorMessage = new ArrayList<>();
        Set<Long> teacherIds = new HashSet();
        Map<Long,Long> schoolTeacherIdMap = new HashMap<>();

        Set<Long> allSchoolIds = new HashSet<>();
        Set<Long> allTeacherIds = new HashSet<>();
        int rowNo = 1;
        while (true) {
            XSSFRow row = sheet.getRow(rowNo++);
            if (row == null) {
                break;
            }
            String schoolIdStr = XssfUtils.getStringCellValue(row.getCell(0));
            String teacherIdStr = XssfUtils.getStringCellValue(row.getCell(2));
            Long schoolId = SafeConverter.toLong(schoolIdStr);
            if (schoolId > 0){
                allSchoolIds.add(schoolId);
            }
            Long teacherId = SafeConverter.toLong(teacherIdStr);
            if (teacherId > 0){
                allTeacherIds.add(teacherId);
            }
        }

        Map<Long, CrmSchoolSummary> schoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(allSchoolIds);
        List<Long> noCatchSchoolIds = new ArrayList<>();
        Map<Long,Object> schoolObjectMap = new HashMap<>();
        allSchoolIds.forEach(item -> {
            if (schoolSummaryMap.containsKey(item)){
                CrmSchoolSummary schoolSummary = schoolSummaryMap.get(item);
                if (null != schoolSummary)
                    schoolObjectMap.put(item, schoolSummary);
            }else {
                noCatchSchoolIds.add(item);
            }
        });
        if (CollectionUtils.isNotEmpty(noCatchSchoolIds)){
            Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader().loadSchools(noCatchSchoolIds).getUninterruptibly();
            schoolMap.forEach((k,v) -> {
                if (null != v){
                    schoolObjectMap.put(k,v);
                }
            });
        }


        Map<Long, CrmTeacherSummary> teacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(allTeacherIds);
        List<Long> noCatchTeacherIds = new ArrayList<>();
        Map<Long,Object> teacherObjectMap = new HashMap<>();
        allTeacherIds.forEach(item -> {
            if (teacherSummaryMap.containsKey(item)){
                CrmTeacherSummary crmTeacherSummary = teacherSummaryMap.get(item);
                if (null != crmTeacherSummary)
                    teacherObjectMap.put(item, crmTeacherSummary);
            }else {
                noCatchTeacherIds.add(item);
            }
        });
        if (CollectionUtils.isNotEmpty(noCatchTeacherIds)){
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(noCatchTeacherIds);
            teacherMap.forEach((k,v) -> {
                if (null != v){
                    teacherObjectMap.put(k,v);
                }
            });
        }


        boolean checkFlag = true;
        rowNo = 1;
        while (true){
            XSSFRow row = sheet.getRow(rowNo++);
            if(row == null){
                break;
            }
            String schoolIdStr = XssfUtils.getStringCellValue(row.getCell(0));
            String schoolName = SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(1)));
            String teacherIdStr = XssfUtils.getStringCellValue(row.getCell(2));
            String teacherName = SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(3)));

            Long schoolId = 0l;
            Long teacherId = 0l;
            //学校ID是否为空
            if (StringUtils.isBlank(schoolIdStr)){
                checkFlag = false;
                errorMessage.add("第" + rowNo + "行，无学校ID，请检查后重新上传");
                continue;
            }
            //老师ID是否为空
            if (StringUtils.isBlank(teacherIdStr)){
                checkFlag = false;
                errorMessage.add("第" + rowNo + "行，无老师ID，请检查后重新上传");
                continue;
            }
            //系统中是否有该学校ID
            schoolId = SafeConverter.toLong(schoolIdStr);
            if (schoolId > 0){
                Object schoolObj = schoolObjectMap.get(schoolId);
                if (schoolObj == null){
                    checkFlag = false;
                    errorMessage.add("第" + rowNo + "行，系统中无此学校ID，请检查后重新上传");
                    continue;
                }
//                else {
//                    if (schoolObj instanceof CrmSchoolSummary){
//                        CrmSchoolSummary schoolSummary = (CrmSchoolSummary) schoolObj;
//                        if (schoolSummary != null && !Objects.equals(schoolSummary.getSchoolName(), schoolName)){
//                            checkFlag = false;
//                            errorMessage.add("第" + rowNo + "行，系统中无此学校名称，请检查后重新上传");
//                            continue;
//                        }
//                    }else if (schoolObj instanceof School){
//                        School school = (School)schoolObj;
//                        if (school != null && !Objects.equals(school.getCname(), schoolName)){
//                            checkFlag = false;
//                            errorMessage.add("第" + rowNo + "行，系统中无此学校名称，请检查后重新上传");
//                            continue;
//                        }
//                    }
//                }
            }else{
                checkFlag = false;
                errorMessage.add("第" + rowNo + "行，学校ID类型不正确，请检查后重新上传");
                continue;
            }
            //系统中是否有该老师ID
            teacherId = SafeConverter.toLong(teacherIdStr);
            if (teacherId > 0){
                Object teacherObj = teacherObjectMap.get(teacherId);
                if (teacherObj == null){
                    checkFlag = false;
                    errorMessage.add("第" + rowNo + "行，系统中无此老师ID，请检查后重新上传");
                    continue;
                }else {
                    if (teacherObj instanceof CrmTeacherSummary){
                        CrmTeacherSummary teacherSummary = (CrmTeacherSummary)teacherObj;
//                        if (teacherSummary != null && !Objects.equals(teacherSummary.getRealName(), teacherName)){
//                            checkFlag = false;
//                            errorMessage.add("第" + rowNo + "行，系统中无此老师姓名，请检查后重新上传");
//                            continue;
//                        }
                        if (teacherSummary != null && !Objects.equals(teacherSummary.getSchoolId(), schoolId)){
                            checkFlag = false;
                            errorMessage.add("第" + rowNo + "行，系统中老师学校关系不匹配，请检查后重新上传");
                            continue;
                        }
                    }
//                    else if (teacherObj instanceof Teacher){
//                        Teacher teacher = (Teacher)teacherObj;
//                        if (teacher != null && !Objects.equals(teacher.fetchRealname(), teacherName)){
//                            checkFlag = false;
//                            errorMessage.add("第" + rowNo + "行，系统中无此老师姓名，请检查后重新上传");
//                            continue;
//                        }
//                    }
                }
            }else {
                checkFlag = false;
                errorMessage.add("第" + rowNo + "行，老师ID类型不正确，请检查后重新上传");
                continue;
            }

            if (checkFlag){
                //过滤掉重复学校ID&重复老师ID
                if (schoolTeacherIdMap.containsKey(schoolId) && Objects.equals(schoolTeacherIdMap.get(schoolId), teacherId)){
                    continue;
                }
                schoolTeacherIdMap.put(schoolId,teacherId);
                //老师ID去重
                teacherIds.add(teacherId);

                Integer regionCode = null;
                String schoolLevel = "";
                Object schoolObj = schoolObjectMap.get(schoolId);
                if (schoolObj instanceof CrmSchoolSummary){
                    CrmSchoolSummary schoolSummary = (CrmSchoolSummary) schoolObj;
                    if (schoolSummary != null){
                        regionCode = schoolSummary.getCountyCode();
                        schoolLevel = schoolSummary.getSchoolLevel() != null ? schoolSummary.getSchoolLevel().name() : "";
                    }
                }else if (schoolObj instanceof School){
                    School school = (School) schoolObj;
                    if (school != null){
                        regionCode = school.getRegionCode();
                        schoolLevel = SchoolLevel.safeParse(school.getLevel()).name();
                    }
                }

                AgentSubTask subTask = new AgentSubTask();
                subTask.setSchoolId(schoolId);
                subTask.setSchoolName(schoolName);
                subTask.setTeacherId(teacherId);
                subTask.setTeacherName(teacherName);
                subTask.setIfFollowUp(false);
                subTask.setIfHomework(false);
                subTask.setRegionCode(regionCode);
                subTask.setSchoolLevel(schoolLevel);
                subTask.setDisabled(false);
                subTaskList.add(subTask);
            }
        }
        if(!checkFlag){
            return MapMessage.errorMessage().add("errorList", errorMessage);
        }
        if(CollectionUtils.isEmpty(subTaskList)){
            errorMessage.add("文件无有效数据！");
            return MapMessage.errorMessage().add("errorList", errorMessage);
        }

        MapMessage message = MapMessage.successMessage();

        message.add("dataList", subTaskList);
        message.add("teacherNum",(teacherIds.size()));
        return message;
    }

    /**
     * 任务导入
     * @param workbook
     * @param mainTask
     * @return
     */
    public MapMessage importTask(XSSFWorkbook workbook, AgentMainTask mainTask) {
        //检查excel文件
        MapMessage checkResult = checkWorkBook(workbook);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        //主任务，设置老师数目
        mainTask.setTeacherNum(SafeConverter.toInt(checkResult.get("teacherNum")));
        agentMainTaskDao.insert(mainTask);

        List<AgentSubTask> subTaskList = (List<AgentSubTask>) checkResult.get("dataList");
        for (AgentSubTask subTask : subTaskList){
            subTask.setMainTaskId(mainTask.getId());
            subTask.setDisabled(false);
        }
        agentSubTaskDao.inserts(subTaskList);

        return MapMessage.successMessage();
    }

    /**
     * 添加任务
     * @param currentUser
     * @param title
     * @param endTime
     * @param comment
     * @param workbook
     * @return
     */
    public MapMessage addTask(AuthCurrentUser currentUser,String title, Date endTime, String comment,XSSFWorkbook workbook){
        List<AgentMainTask> mainTaskList = agentMainTaskDao.loadByTitle(title);
        if (CollectionUtils.isNotEmpty(mainTaskList)){
            return MapMessage.errorMessage("该标题任务已存在！");
        }
        AgentMainTask mainTask = new AgentMainTask();
        mainTask.setTitle(title);
        mainTask.setEndTime(endTime);
        mainTask.setComment(comment);
        mainTask.setPublisherId(currentUser.getUserId());
        mainTask.setPublisherName(currentUser.getRealName());
        mainTask.setDisabled(false);

        //任务导入
        MapMessage mapMessage = importTask(workbook,mainTask);
        if (!mapMessage.isSuccess()){
            return mapMessage;
        }
        return MapMessage.successMessage().add("teacherNum",mainTask.getTeacherNum());
    }

    /**
     * 任务中心查询主任务列表
     * @return
     */
    public MapMessage mainTaskList(Long userId){
        MapMessage mapMessage = MapMessage.successMessage();
        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        List<AgentMainTask> allMainTasks = agentMainTaskDao.findTaskMainList();
        List<Map<String,Object>> userMainTasks = new ArrayList<>();
        AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
        AgentGroup agentGroup = baseOrgService.getGroupById(groupUser.getGroupId());
        //获取该部门对应的学校阶段
        List<String> schoolLevelList = getSchoolLevel(agentGroup.getServiceType());
        if(roleType == AgentRoleType.BusinessDeveloper){
            List<AgentUserSchool> userSchools = agentUserSchoolLoaderClient.findByUserId(userId);
            List<Long> schoolIds = userSchools.stream().map(AgentUserSchool :: getSchoolId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(schoolIds)){
                if(CollectionUtils.isNotEmpty(allMainTasks)) {
                    allMainTasks.forEach(mt -> {
                        Map<Long,List<AgentSubTask>> subTaskMapList = agentSubTaskDao.findTaskSubBySchoolIds( mt.getId(),schoolIds);
                        List<AgentSubTask> subTaskList = subTaskMapList.values().stream().flatMap(List::stream).collect(Collectors.toList());
                        int totalSubTaskNum = subTaskList.size();
                        if (totalSubTaskNum > 0) {
                            userMainTasks.add(getMainTaskMap(subTaskList, mt, totalSubTaskNum));
                        }
                    });
                }
            }

        }else if(roleType == AgentRoleType.Country || roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager){
            Collection<ExRegion> counties = baseOrgService.getCountyRegionByUserId(userId, roleType);
            Set<Integer> countySet = new HashSet<>();
            counties.forEach(p -> {
                if(p.getCountyCode() > 0){
                    countySet.add(p.getCountyCode());
                }
            });

            if(CollectionUtils.isNotEmpty(countySet)){
//                allMainTasks.forEach(mt ->{
////                    futureList.add(AlpsThreadPool.getInstance().submit(() -> AssemblingMainTask(mt,countySet,schoolLevelList)));
//                    Map<Integer,List<AgentSubTask>> subTaskMapList = agentSubTaskDao.findTaskSubByRegionCodes(mt.getId(),countySet);
//                    List<AgentSubTask> subTaskList = subTaskMapList.values().stream().flatMap(List::stream).collect(Collectors.toList());
//                    subTaskList = subTaskList.stream().filter(p -> schoolLevelList.contains(p.getSchoolLevel()) ).collect(Collectors.toList());
//                    int totalSubTaskNum = subTaskList.size();
//                    if(totalSubTaskNum > 0){
//                        userMainTasks.add(getMainTaskMap(subTaskList,mt,totalSubTaskNum));
//                    }
//
//                });
                Map<String,AgentMainTask> mainTaskMap = allMainTasks.stream().collect(Collectors.toMap(AgentMainTask :: getId,Function.identity()));
                List<Future<List<Map<String,Object>>>> futureList = new ArrayList<>();
                Collection<String> mainIds = mainTaskMap.keySet();
                List<List<String>> idList = splitList(mainIds,5);
                for(List<String> itemList : idList){
                    futureList.add(AlpsThreadPool.getInstance().submit(() -> assemblingMainTask(itemList,countySet,schoolLevelList,mainTaskMap)));
                }
                for(Future<List<Map<String,Object>>> future : futureList) {
                    try {
                        List<Map<String,Object>> item = future.get();
                        if(CollectionUtils.isNotEmpty(item)){
                            userMainTasks.addAll(item);
                        }
                    } catch (Exception e) {
                        logger.error("查询主任务异常",e);
                    }
                }
            }
        }
        List<Map<String,Object>> unFinshList = userMainTasks.stream().filter(p -> !SafeConverter.toBoolean(p.get("endStatus"))).collect(Collectors.toList());

        Collections.sort(unFinshList, (o1, o2) -> (SafeConverter.toDate( o2.get("createTime"))).compareTo(SafeConverter.toDate( o1.get("createTime"))));
        List<Map<String,Object>> finshList = userMainTasks.stream().filter(p -> SafeConverter.toBoolean(p.get("endStatus"))).collect(Collectors.toList());
//        unFinshList.sort(Comparator.comparingLong(d2 -> (SafeConverter.toDate( d2.get("createTime"))).getTime()));
        Collections.sort(finshList, (o1, o2) -> SafeConverter.toDate( o2.get("createTime")).compareTo(SafeConverter.toDate( o1.get("createTime"))));
        List<Map<String,Object>> resultList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(unFinshList)){
            resultList.addAll(unFinshList);
        }
        if(CollectionUtils.isNotEmpty(finshList)){
            resultList.addAll(finshList);
        }
        return mapMessage.add("dataList",resultList);
    }

    private List<Map<String,Object>> assemblingMainTask(List<String> mainIds,Set<Integer> countySet,List<String> schoolLevelList,Map<String,AgentMainTask> taskMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String id : mainIds) {
            AgentMainTask mt = taskMap.get(id);
            if(mt == null){
                continue;
            }
            Map<Integer, List<AgentSubTask>> subTaskMapList = agentSubTaskDao.findTaskSubByRegionCodes(id, countySet);
            List<AgentSubTask> subTaskList = subTaskMapList.values().stream().flatMap(List::stream).collect(Collectors.toList());
            subTaskList = subTaskList.stream().filter(p -> schoolLevelList.contains(p.getSchoolLevel())).collect(Collectors.toList());
            int totalSubTaskNum = subTaskList.size();
            if (totalSubTaskNum > 0) {
                result.add(getMainTaskMap(subTaskList, mt, totalSubTaskNum));
            }
        }
        return result;
    }

    private List<List<String>> splitList(Collection<String> ids, int size){
        List<List<String>> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(ids)){
            return resultList;
        }
        if(size < 1){
            List<String> item = new ArrayList<>(ids);
            resultList.add(item);
            return resultList;
        }
        List<String> idList = new ArrayList<>(ids);
        Map<Integer, List<String>> map = idList.stream().collect(Collectors.groupingBy(p -> idList.indexOf(p) / size, Collectors.toList()));
        map.values().forEach(resultList::add);
        return resultList;
    }
    /**
     * 任务中心查询 查看专员下子任务列表
     * @return
     */
    public MapMessage subTaskList(AgentRoleType currentRoleType,Long userId,String taskId,Long groupId,Long schoolId){
        MapMessage mapMessage = MapMessage.successMessage();
        AgentMainTask agentMainTask = null;
        if (StringUtils.isNotBlank(taskId)){
            agentMainTask = agentMainTaskDao.load(taskId);
            //学校详情，待办任务点击跳转
        }else if (schoolId > 0){
            agentMainTask = getMainTaskBySchoolId(schoolId);
        }
        if(agentMainTask == null || agentMainTask.getDisabled() || agentMainTask.getEndTime().before(new Date())){
            return MapMessage.errorMessage("任务不存在或者已结束，老师列表不可见");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("taskId",agentMainTask.getId());
        map.put("title",agentMainTask.getTitle());
        map.put("createTime",agentMainTask.getCreateTime());
        map.put("endTime",agentMainTask.getEndTime());
        map.put("comment",agentMainTask.getComment());
        mapMessage.add("dataMap",map);
        List<AgentSubTask> subTaskList = new ArrayList<>();
//        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        if (StringUtils.isNotBlank(taskId) && groupId > 0){
            AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
            if((currentRoleType == AgentRoleType.CityManager || groupRoleType == AgentGroupRoleType.City) && userId == 0l){//如果登录人是市经理 或选择区域为分区查看分区数据
//            AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(currentUserId).stream().findFirst().orElse(null);
//            if(groupUser != null){
                Collection<ExRegion> counties = baseOrgService.getCountyRegionByGroupId(groupId);
                Set<Integer> countySet = new HashSet<>();
                counties.forEach(p -> {
                    if(p.getCountyCode() > 0){
                        countySet.add(p.getCountyCode());
                    }
                });
                if(CollectionUtils.isNotEmpty(countySet)){
                    AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
                    //获取该部门对应的学校阶段
                    List<String> schoolLevelList = getSchoolLevel(agentGroup.getServiceType());
                    Map<Integer,List<AgentSubTask>> subTaskMapList = agentSubTaskDao.findTaskSubByRegionCodes(taskId,countySet);
                    List<AgentSubTask> allRegionList = subTaskMapList.values().stream().flatMap(List::stream).collect(Collectors.toList());
                    subTaskList = allRegionList.stream().filter(p -> schoolLevelList.contains(p.getSchoolLevel()) ).collect(Collectors.toList());
                    List<Long> devUserIds = baseOrgService.getGroupBusinessDevelopers(groupId).stream().map(AgentUser :: getId).collect(Collectors.toList());
                    Map<Long, List<AgentUserSchool>> devUserSchools = agentUserSchoolLoaderClient.findByUserIds(devUserIds);
                    Set<Long> devSchoolSet = new HashSet<>();
                    devUserSchools.forEach((k,v)->{
                        if(CollectionUtils.isNotEmpty(v)){
                            devSchoolSet.addAll(v.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toList()));
                        }
                    });
                    subTaskList = subTaskList.stream().filter(s -> !devSchoolSet.contains(s.getSchoolId())).collect(Collectors.toList());
                }
//            }
            }else{
                List<AgentUserSchool> userSchools = agentUserSchoolLoaderClient.findByUserId(userId);
                List<Long> schoolIds = userSchools.stream().map(AgentUserSchool :: getSchoolId).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(schoolIds)){
                    Map<Long,List<AgentSubTask>> subMapList = agentSubTaskDao.findTaskSubBySchoolIds(taskId,schoolIds);
                    subTaskList = subMapList.values().stream().flatMap(List::stream).collect(Collectors.toList());
                }

            }
        }else if (schoolId > 0){
            subTaskList.addAll(agentSubTaskDao.findTaskSubyMainTaskId(agentMainTask.getId()));
            subTaskList = subTaskList.stream().filter(p -> Objects.equals(p.getSchoolId(), schoolId)).collect(Collectors.toList());
        }

        if(CollectionUtils.isNotEmpty(subTaskList)){
            //按照学校排序展示，老师多的学校展示在前面
            List<AgentSubTask> waitList = getToFollowList(subTaskList);
            List<Long> waitTeacherIdList = waitList.stream().map(AgentSubTask::getTeacherId).collect(Collectors.toList());
            List<TeacherBasicInfo> waitTeacherList = teacherResourceService.generateTeacherBasicInfo(waitTeacherIdList, true, true, true,false);
            List<TeacherBasicInfo> sortedWaitTeacherList = sortListBySchoolTeacherNum(waitTeacherList);
            List<Map<String,Object>> waitMapList = createResulltList(agentMainTask.getCreateTime(),agentMainTask.getEndTime(),sortedWaitTeacherList);
            mapMessage.add("waitList",waitMapList);

            //优先展示最近填写备注的老师
            List<AgentSubTask> unusedList = subTaskList.stream().filter( p -> p.getIfFollowUp() && p.getIfHomework() == false).collect(Collectors.toList());
            List<Long> unusedTeacherIdList = unusedList.stream().map(AgentSubTask::getTeacherId).collect(Collectors.toList());
            List<TeacherBasicInfo> unusedTeacherList = teacherResourceService.generateTeacherBasicInfo(unusedTeacherIdList, true, true, true,false);
            List<Map<String,Object>> unusedMapList = createResulltList(agentMainTask.getCreateTime(),agentMainTask.getEndTime(),unusedTeacherList);
            sortListByComtent(unusedMapList);
            mapMessage.add("unusedList",unusedMapList);

            //处理已使用   优先展示已跟进的老师（最近填写备注的显示在最前面），未跟进的按照学校排序展示，老师多的学校展示在前面
            List<Map<String,Object>> usedResult = new ArrayList<>();
            //已跟进已使用
            List<AgentSubTask> usedAndFollowList = subTaskList.stream().filter( p -> p.getIfFollowUp() && p.getIfHomework() == true).collect(Collectors.toList());
            List<Long> usedAndFollowTeacherIdList = usedAndFollowList.stream().map(AgentSubTask::getTeacherId).collect(Collectors.toList());
            List<TeacherBasicInfo> usedAndFollowTeacherList = teacherResourceService.generateTeacherBasicInfo(usedAndFollowTeacherIdList, true, true, true,false);

            List<Map<String,Object>> usedAndFollowMapList = createResulltList(agentMainTask.getCreateTime(),agentMainTask.getEndTime(),usedAndFollowTeacherList);
            List<Map<String,Object>> hasContent = usedAndFollowMapList.stream().filter( p -> {
                String content = SafeConverter.toString(p.get("content"));
                return  StringUtils.isNotBlank(content);
            }).collect(Collectors.toList());
            sortListByComtent(hasContent);
            usedResult.addAll(hasContent);

            //未跟进已使用
            List<AgentSubTask> usedAndUnFollowList = subTaskList.stream().filter( p -> p.getIfFollowUp() == false && p.getIfHomework() == true).collect(Collectors.toList());
            List<Long> usedAndUnFollowTeacherIdList = usedAndUnFollowList.stream().map(AgentSubTask::getTeacherId).collect(Collectors.toList());
            List<TeacherBasicInfo> usedAndUnFollowTeacherList = teacherResourceService.generateTeacherBasicInfo(usedAndUnFollowTeacherIdList, true, true, true,false);
            List<TeacherBasicInfo> sortedUsedAndUnFollowTeacherList = sortListBySchoolTeacherNum(usedAndUnFollowTeacherList);
            List<Map<String,Object>> usedAndUnFollowMapList = createResulltList(agentMainTask.getCreateTime(),agentMainTask.getEndTime(),sortedUsedAndUnFollowTeacherList);
            usedResult.addAll(usedAndUnFollowMapList);
            mapMessage.add("usedList",usedResult);
        }

        return mapMessage;
    }


    private Map<String,Object> getMainTaskMap(List<AgentSubTask> subTaskList, AgentMainTask mt,int totalSubTaskNum){
        int finishNum = getFinishList(subTaskList).size();
        int usedNum =  getUsedList(subTaskList).size();
        Map<String,Object> map = new HashMap<>();
        map.put("taskId",mt.getId());
        map.put("title",mt.getTitle());
        map.put("createTime",mt.getCreateTime());
        map.put("endTime",mt.getEndTime());
        map.put("comment",mt.getComment());
        map.put("taskNum",totalSubTaskNum);
        map.put("finishNum",finishNum);
        map.put("endStatus",new Date().after(mt.getEndTime()));
        map.put("usageRate",MathUtils.doubleDivide(usedNum,totalSubTaskNum,2));
        map.put("responseRate",MathUtils.doubleDivide(finishNum,totalSubTaskNum,2));
        return map;
    }

    /**
     * 主任务列表
     * @return
     */
    public List<Map<String,Object>> mainTaskList(){
        Date currentDate = new Date();
        List<AgentMainTask> mainTaskList = getAllMainTaskList();
        List<AgentMainTask> noEndMainTaskList = new ArrayList<>();
        List<AgentMainTask> endMainTaskList = new ArrayList<>();
        List<AgentMainTask> sortedMainTaskList = new ArrayList<>();

        mainTaskList.forEach(item -> {
            //未结束
            if (item.getEndTime().after(currentDate)){
                noEndMainTaskList.add(item);
                //已结束
            }else {
                endMainTaskList.add(item);
            }
        });
        sortedMainTaskList.addAll(noEndMainTaskList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList()));
        sortedMainTaskList.addAll(endMainTaskList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList()));

        List<Map<String, Object>> dataList = toMainTaskMapList(sortedMainTaskList);
        return dataList;
    }

    /**
     * 获取所有主任务列表
     * @return
     */
    public List<AgentMainTask> getAllMainTaskList(){
//        //查询近半年（6个月）数据
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        calendar.add(Calendar.MONTH, -6);
//        Date lastSixMonthTime = calendar.getTime();
//        return agentMainTaskDao.loadByCreateTime(lastSixMonthTime);
        return agentMainTaskDao.loadByCreateTime(null);
    }

    /**
     * 主任务列表数据转换
     * @param mainTaskList
     * @return
     */
    public List<Map<String ,Object>> toMainTaskMapList(List<AgentMainTask> mainTaskList){
        if (CollectionUtils.isEmpty(mainTaskList)){
            return new ArrayList<>();
        }
        List<Map<String,Object>> dataList = new ArrayList<>();
        Date currentDate = new Date();
        mainTaskList.forEach(item -> {
            Map<String,Object> mainTaskMap = new HashMap<>();
            if (null != item){
                mainTaskMap.put("id",item.getId());
                mainTaskMap.put("createTime", DateUtils.dateToString(item.getCreateTime(),"yyyy-MM-dd"));
                mainTaskMap.put("title",item.getTitle());
                mainTaskMap.put("teacherNum",item.getTeacherNum());
                mainTaskMap.put("endTime",DateUtils.dateToString(item.getEndTime(),"yyyy-MM-dd"));
                mainTaskMap.put("comment",item.getComment());
                mainTaskMap.put("publisherName",item.getPublisherName());
                mainTaskMap.put("ifEnd",item.getEndTime().before(currentDate));
            }
            dataList.add(mainTaskMap);
        });
        return dataList;
    }

    /**
     * 主任务详情
     * @param id
     * @return
     */
    public MapMessage mainTaskDetail(String id){
        AgentMainTask mainTask = agentMainTaskDao.load(id);
        if (mainTask == null){
            return MapMessage.errorMessage("该任务不存在");
        }
        return MapMessage.successMessage().add("dataMap",toMainTaskMap(mainTask));
    }

    /**
     * 主任务详情数据转换
     * @param mainTask
     * @return
     */
    public Map<String,Object> toMainTaskMap(AgentMainTask mainTask){
        if (null == mainTask){
            return new HashMap<>();
        }
        Map<String,Object> mainTaskMap = new HashMap<>();
        mainTaskMap.put("id",mainTask.getId());
        mainTaskMap.put("title",mainTask.getTitle());
        mainTaskMap.put("endTime",DateUtils.dateToString(mainTask.getEndTime(),"yyyy-MM-dd"));
        mainTaskMap.put("comment",mainTask.getComment());
        return mainTaskMap;
    }

    /**
     * 编辑主任务
     * @param id
     * @param title
     * @param endTime
     * @param comment
     * @return
     */
    public MapMessage editMainTask(String id,String title,Date endTime,String comment){
        AgentMainTask mainTask = agentMainTaskDao.load(id);
        if (null == mainTask){
            return MapMessage.errorMessage("该任务不存在！");
        }
        //如果任务已经截止，不可修改截止时间
        Date currentDate = new Date();
        if (mainTask.getEndTime().before(currentDate)){
            return MapMessage.errorMessage("该任务已截止，不可修改截止时间！");
        }

        List<AgentMainTask> mainTaskList = agentMainTaskDao.loadByTitle(title).stream().filter(p -> !Objects.equals(p.getId(), id)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(mainTaskList)){
            return MapMessage.errorMessage("该标题任务已存在！");
        }
        mainTask.setTitle(title);
        if (endTime != null){
            mainTask.setEndTime(endTime);
        }
        mainTask.setComment(comment);
        agentMainTaskDao.replace(mainTask);
        return MapMessage.successMessage();
    }

    /**
     * 删除任务
     * @param id
     * @return
     */
    public MapMessage deleteTask(String id){
        AgentMainTask mainTask = agentMainTaskDao.load(id);
        if (mainTask == null){
            return MapMessage.errorMessage("该任务不存在");
        }
        //删除对应子任务
        agentSubTaskDao.deleteByMainTaskId(id);
        //删除主任务数据
        mainTask.setDisabled(true);
        agentMainTaskDao.replace(mainTask);
        return MapMessage.successMessage();
    }

    private List<TeacherBasicInfo> sortListBySchoolTeacherNum(List<TeacherBasicInfo> teacherList){
        List<TeacherBasicInfo> result = new ArrayList<>();
        if(CollectionUtils.isEmpty(teacherList)){
            return result;
        }
        Map<Long,List<TeacherBasicInfo>> teacherMap = teacherList.stream().collect(Collectors.groupingBy(TeacherBasicInfo ::getSchoolId,Collectors.toList()));
        Map<Long,Integer> teacherNumMap = new LinkedHashMap<>();

        teacherMap.forEach((key,subList) -> teacherNumMap.put(key,subList.size()));
        Map<Long,Integer> teacherTemMap = sortByValue(teacherNumMap);
        teacherTemMap.forEach((k,v) ->{
            result.addAll(teacherMap.get(k));
        });
        return result;
    }
    public static List<Map<String, Object>> sortListByComtent(List<Map<String, Object>> list) {
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                int result = 0;
                Date v1 = SafeConverter.toDate(o1.get("contentCreateTime"));
                Date v2 = SafeConverter.toDate(o2.get("contentCreateTime"));
                if(v1 != null && v2 != null){
                   result = v1.before(v2) ? result = 1 : -1;
                }else if(v1 == null && v2 != null){
                    result = -1;
                }else  if(v1 != null && v2 == null){
                    result = 1;
                }
                return result;
            }
        });
        return list;
    }

    public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                return (e2.getValue()).compareTo(e1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private List<Map<String,Object>> createResulltList(Date startTime,Date endTime ,List<TeacherBasicInfo> teacherBasicInfos){
        if(CollectionUtils.isEmpty(teacherBasicInfos)){
            return Collections.emptyList();
        }
        List<Map<String,Object>> result = new ArrayList<>();
        teacherBasicInfos.forEach(p->{
            Map<String,Object> map =BeanMapUtils.tansBean2Map(p);
            AgentMemorandum agentMemorandum = agentMemorandumService.loadMemorandumByTeacherId(p.getTeacherId(),null,startTime,endTime).stream().findFirst().orElse(null);
           if(agentMemorandum != null){
               map.put("content",agentMemorandum.getContent());
               map.put("contentCreateTime",agentMemorandum.getCreateTime());
           }else {
               map.put("content","");
               map.put("contentCreateTime",null);
           }
            result.add(map);
        });
        return result;
    }

    public Map<String,Object> rangeOrganizationRole(Long groupId, String groupRoleType, String roleType,Long userId){
        Map<String,Object> dataMap = new HashMap<>();
        AgentGroup group = new AgentGroup();
        List<Map<String, Object>> groupRoleTypeList = new ArrayList<>();
        List<Map<String, Object>> roleTypeList = new ArrayList<>();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        //默认
        if (groupId == 0L && StringUtils.isBlank(groupRoleType) && StringUtils.isBlank(roleType)){
            //全国总监
            if (userRole == AgentRoleType.Country){
                //小学市场
                group = baseOrgService.findAllGroups().stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
            }else {
                AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
                group = baseOrgService.getGroupById(groupUser.getGroupId());
            }
        }else {
            group = baseOrgService.getGroupById(groupId);
        }
        //组织
        Map<String, Object> organizationMap = generateOrganization(group,groupRoleType);
        AgentGroupRoleType showGroupRoleType = (AgentGroupRoleType)organizationMap.get("showGroupRoleType");
        groupRoleTypeList = (List<Map<String, Object>>)organizationMap.get("groupRoleTypeList");


        dataMap.put("group",group);
        dataMap.put("groupRoleTypeList",groupRoleTypeList);
        dataMap.put("roleTypeList",roleTypeList);
        return dataMap;
    }

    public Map<String,Object> generateOrganization(AgentGroup group,String groupRoleType){
        AgentGroupRoleType agentGroupRoleType = null;
        if (StringUtils.isNotBlank(groupRoleType)){
            agentGroupRoleType = AgentGroupRoleType.nameOf(groupRoleType);
        }
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String,Object>> groupRoleTypeList = new ArrayList<>();
        AgentGroupRoleType showGroupRoleType = null;
        AgentGroupRoleType currentGroupRoleType = group.fetchGroupRoleType();
        //分区、
        if (currentGroupRoleType == AgentGroupRoleType.City){
            groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",true));
        }else if(currentGroupRoleType == AgentGroupRoleType.Area){ //区域
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,true));
            groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
        }if (currentGroupRoleType == AgentGroupRoleType.Region){//大区
            //小学大区
            if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
                if (agentGroupRoleType == AgentGroupRoleType.City){
                    showGroupRoleType = AgentGroupRoleType.City;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,true));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area,false));
                    groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
                }else {
                    showGroupRoleType = AgentGroupRoleType.Area;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area,true));
                    groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
                }
                //中学大区
            }else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)){
                showGroupRoleType = AgentGroupRoleType.City;
                groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,true));
                groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
            }
            //市场
        }else if (currentGroupRoleType == AgentGroupRoleType.Marketing){
            //小学市场
            if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
                if (agentGroupRoleType == AgentGroupRoleType.City){
                    showGroupRoleType = AgentGroupRoleType.City;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,true));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area,false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region,false));
                    groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
                }else if (agentGroupRoleType == AgentGroupRoleType.Area){
                    showGroupRoleType = AgentGroupRoleType.Area;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area,true));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region,false));
                    groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
                }else {
                    showGroupRoleType = AgentGroupRoleType.Region;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area,false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region,true));
                    groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
                }
                //中学市场
            }else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)){
                if (agentGroupRoleType == AgentGroupRoleType.City){
                    showGroupRoleType = AgentGroupRoleType.City;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,true));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region,false));
                    groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
                }else {
                    showGroupRoleType = AgentGroupRoleType.Region;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region,true));
                    groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
                }
            }
        }
        dataMap.put("groupRoleTypeList",groupRoleTypeList);
        dataMap.put("showGroupRoleType",showGroupRoleType);

        return dataMap;
    }

    public Map<String,Object> generateGroupRoleType(AgentGroupRoleType groupRoleType,Boolean ifShow){
        Map<String,Object> groupRoleTypeMap = new HashMap<>();
        groupRoleTypeMap.put("groupRoleType",groupRoleType);
        groupRoleTypeMap.put("roleName",groupRoleType.getRoleName());
        groupRoleTypeMap.put("show",ifShow);
        return groupRoleTypeMap;
    }
    public Map<String,Object> generateRoleType(AgentRoleType roleType,String roleName,Boolean ifShow){
        Map<String,Object> roleTypeMap = new HashMap<>();
        roleTypeMap.put("groupRoleType",roleType);
        roleTypeMap.put("roleName",roleName);
        roleTypeMap.put("show",ifShow);
        return roleTypeMap;
    }

    /**
     * 未完成任务的数目
     * @param userId
     * @return
     */
    public int unFinishedTaskNum(Long userId){
        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        //获取未结束的任务
        List<AgentMainTask> unEndMainTaskList = agentMainTaskDao.loadUnEndTaskList();

        Set<String> mainTaskIds = new HashSet<>();
        //专员
        if(roleType == AgentRoleType.BusinessDeveloper){
            List<AgentUserSchool> userSchoolList = agentUserSchoolLoaderClient.findByUserId(userId);
            List<Long> schoolIds = userSchoolList.stream().map(AgentUserSchool :: getSchoolId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(schoolIds)){
                if(CollectionUtils.isNotEmpty(unEndMainTaskList)){
                    unEndMainTaskList.forEach(item ->{                    //获取该主任务中，没有完成的子任务
                        Map<Long,List<AgentSubTask>> subMapList = agentSubTaskDao.findTaskSubBySchoolIds(item.getId(),schoolIds);
                        List<AgentSubTask> unfinishedSubTaskList =subMapList.values().stream().flatMap(List::stream).collect(Collectors.toList())
                                .stream().filter(p -> !p.getIfFollowUp() && !p.getIfHomework()).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(unfinishedSubTaskList)){
                            mainTaskIds.add(item.getId());
                        }
                    });
                }
            }
            //市经理及以上
        }else if(roleType == AgentRoleType.Country || roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager){
            //获取人员所在部门负责的区域
            AgentGroup group = baseOrgService.getGroupFirstOne(userId, roleType);
            if (group == null) {
                return mainTaskIds.size();
            }
            Collection<ExRegion> counties = baseOrgService.getCountyRegionByGroupId(group.getId());
            Set<Integer> countySet = new HashSet<>();
            counties.forEach(p -> {
                if(p.getCountyCode() > 0){
                    countySet.add(p.getCountyCode());
                }
            });
            //获取该部门对应的学校阶段
            List<String> schoolLevelList = getSchoolLevel(group.getServiceType());
            if(CollectionUtils.isNotEmpty(countySet)){
                unEndMainTaskList.forEach(item ->{
                    //获取该主任务中，对应学校阶段，没有完成的子任务
                    Map<Integer,List<AgentSubTask>> subMapList = agentSubTaskDao.findTaskSubByRegionCodes(item.getId(),countySet);
                    List<AgentSubTask> unfinishedSubTaskList =subMapList.values().stream().flatMap(List::stream).collect(Collectors.toList())
                            .stream().filter(p -> schoolLevelList.contains(p.getSchoolLevel()) && !p.getIfFollowUp() && !p.getIfHomework()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(unfinishedSubTaskList)){
                        mainTaskIds.add(item.getId());
                    }
                });
            }
        }
        return mainTaskIds.size();
    }

    /**
     * 布置作业监听，设置任务老师布置作业
     * @param teacherId
     */
    public void setSubTaskIfHomeworkForListener(Long teacherId){
        //获取未结束的主任务
        Set<String> mainTaskIds = agentMainTaskDao.loadUnEndTaskList().stream().map(AgentMainTask::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(mainTaskIds)){
            //子任务维护老师使用
            agentSubTaskDao.updateIfHomework(mainTaskIds,teacherId);
        }
    }

    //添加老师备注加更新根据
    public void setSubTaskFollowForListener(Long teacherId){
        //获取未结束的主任务
        Set<String> mainTaskIds = agentMainTaskDao.loadUnEndTaskList().stream().map(AgentMainTask::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(mainTaskIds)){
            //子任务维护老师使用
            agentSubTaskDao.updateFollow(mainTaskIds,teacherId);
        }
    }

    public List<Map<String,Object>> taskStatistic(String taskId ,Long groupId,AgentGroupRoleType groupRoleType,AgentRoleType userRoleType){
        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        if(agentGroup == null){
            return Collections.emptyList();
        }
       List<String> schoolLevelList = getSchoolLevel(agentGroup.getServiceType());
        List<AgentGroup> groupList = new ArrayList<>();
        Map<Long, List<AgentGroupUser>> groupGroupUserMap = new HashMap<>();

        AgentGroupRoleType currentGroupRoleType = baseOrgService.getGroupRole(groupId);
        if (currentGroupRoleType == AgentGroupRoleType.City || (userRoleType != null && userRoleType == AgentRoleType.BusinessDeveloper)){
            groupList.add(baseOrgService.getGroupById(groupId));
            if(userRoleType == AgentRoleType.BusinessDeveloper){
                groupGroupUserMap.putAll(baseOrgService.getAllSubGroupUsersByGroupIdAndRole(groupId,AgentRoleType.BusinessDeveloper.getId()).stream().collect(Collectors.groupingBy(AgentGroupUser::getGroupId)));
            }else{
                groupGroupUserMap.putAll(baseOrgService.getGroupUserByGroups(Collections.singleton(groupId)).stream().collect(Collectors.groupingBy(AgentGroupUser::getGroupId)));
            }
        }else {
            //过滤出指定部门级别的子部门
            List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId).stream().filter(p -> Objects.equals(p.getRoleId(), groupRoleType.getId())).collect(Collectors.toList());
            Set<Long> subGroupIds = subGroupList.stream().map(AgentGroup::getId).collect(Collectors.toSet());
            subGroupIds.forEach(item -> {
                Set<Long> groupIds = new HashSet<>();
                groupIds.addAll(baseOrgService.getSubGroupList(item).stream().map(AgentGroup::getId).collect(Collectors.toSet()));
                groupIds.add(item);
                groupGroupUserMap.put(item,baseOrgService.getGroupUserByGroups(groupIds).stream().collect(Collectors.toList()));
            });
            groupList.addAll(subGroupList);
        }

        Map<Long, AgentGroup> groupMap = groupList.stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
        Set<Long> groupIds = groupMap.keySet();

        Map<Long,List<Long>> groupUsersMap = new HashMap<>();
        List<Long> userIds = new ArrayList<>();
        groupGroupUserMap.forEach((k,v) -> {
            List<Long> userIdList = v.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            groupUsersMap.put(k,userIdList);
            userIds.addAll(userIdList);
        });

        Set<Integer> countySet =getCountyCodeSet(groupId);
        Map<Integer,List<AgentSubTask>> subMapList = agentSubTaskDao.findTaskSubByRegionCodes(taskId,countySet);
        List<AgentSubTask> allList = subMapList.values().stream().flatMap(List::stream).collect(Collectors.toList());
        List<AgentSubTask> groupAllSubTaskList = allList.stream().filter(p -> schoolLevelList.contains(p.getSchoolLevel())).collect(Collectors.toList());//过滤出对应学校阶段的任务//分区
        if(currentGroupRoleType == AgentGroupRoleType.City || (userRoleType != null && userRoleType == AgentRoleType.BusinessDeveloper)){
            return taskStatisticByDev(currentGroupRoleType,userIds,groupAllSubTaskList);
        }else{
            return taskStatisticByGroup(groupIds,groupAllSubTaskList,groupMap,groupUsersMap);
        }
    }

    //按专员统计
    private List<Map<String,Object>> taskStatisticByDev(AgentGroupRoleType agentGroupRoleType,List<Long> userIds,List<AgentSubTask> subTaskList){
        Map<Long,AgentUser> userMap = baseOrgService.getUsers(userIds) .stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
        Map<Long, List<AgentUserSchool>> userSchools = agentUserSchoolLoaderClient.findByUserIds(userIds);
        List<Map<String,Object>> resultList = new ArrayList<>();
        Set<Long> allSchoolIdSet = new HashSet<>();//存全部已分配了的学校id
        userIds.forEach(u->{
            List<AgentSubTask> userSubTaskList = new ArrayList<>();
            AgentUser agentUser = userMap.get(u);
            if(agentUser != null){
                if(CollectionUtils.isNotEmpty(userSchools.get(u))){
                    List<Long> schoolIds = userSchools.get(u).stream().map(AgentUserSchool :: getSchoolId).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(schoolIds)){
                        allSchoolIdSet.addAll(schoolIds);
                    }
                    userSubTaskList = subTaskList.stream().filter(sb-> schoolIds.contains(sb.getSchoolId())).collect(Collectors.toList());
                }
            }
            Map<String,Object> map = toDevStatisticMap(agentUser.getId(),agentUser.getRealName(),userSubTaskList);
            resultList.add(map);
        });
        //选择的部门为分区时 要过滤出为分配的数据
        if(agentGroupRoleType == AgentGroupRoleType.City){
            List<AgentSubTask> unDisSubTaskList = subTaskList.stream().filter(sb-> !allSchoolIdSet.contains(sb.getSchoolId())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(unDisSubTaskList)){
                Map<String,Object> map = toDevStatisticMap(0l,"未分配",unDisSubTaskList);
                resultList.add(map);
            }
        }
        return resultList;
    }

    private Map<String,Object> toDevStatisticMap(Long userId,String userName,List<AgentSubTask> userSubTaskList){
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("userName",userName);
        int userTaskNum = userSubTaskList.size();
        map.put("taskNum",userTaskNum);
        int finishNum = getFinishList(userSubTaskList).size();
        int usedNum =  getUsedList(userSubTaskList).size();
        int toFollowUp = getToFollowList(userSubTaskList).size();
        map.put("tofollowUp",toFollowUp);
        map.put("usageRate",userTaskNum > 0 ? MathUtils.doubleDivide(usedNum,userTaskNum,2) : 0);
        map.put("responseRate",userTaskNum > 0 ? MathUtils.doubleDivide(finishNum,userTaskNum,2) : 0);
        return map;
    }
    //按部门统计
    private List<Map<String,Object>> taskStatisticByGroup(Set<Long> groupIds,List<AgentSubTask> subTaskList,Map<Long, AgentGroup> groupMap,Map<Long,List<Long>> groupUsersMap){

        List<Map<String,Object>> resultList = new ArrayList<>();
        for (Long item : groupIds) {
            List<Long> userIds = groupUsersMap.get(item);
            Map<String, Object> dataMap = new HashMap<>();
            AgentGroup group = groupMap.get(item);
            if (group != null) {
                dataMap.put("groupId", item);
                dataMap.put("groupName", group.getGroupName());
            }else {
                dataMap.put("groupId", 0l);
                dataMap.put("groupName", "");
            }
            Set<Integer> countySet =getCountyCodeSet(item);
            List<AgentSubTask> groupSubList = subTaskList.stream().filter(p-> countySet.contains(p.getRegionCode())).collect(Collectors.toList());
            int userTaskNum = groupSubList.size();
            dataMap.put("taskNum",userTaskNum);
            int finishNum = getFinishList(groupSubList).size();
            int usedNum =  getUsedList(groupSubList).size();
            int toFollowUp = getToFollowList(groupSubList).size();
            dataMap.put("perCapitaSurplus",userIds.size() > 0 ? MathUtils.doubleDivide(toFollowUp,userIds.size(),0) : 0);
            dataMap.put("usageRate",userTaskNum > 0 ? MathUtils.doubleDivide(usedNum,userTaskNum,2) : 0);
            dataMap.put("responseRate",userTaskNum > 0 ? MathUtils.doubleDivide(finishNum,userTaskNum,2) : 0);
            resultList.add(dataMap);
        }
        return resultList;
    }

    private List<String> getSchoolLevel(String serviceType){
        if(StringUtils.isBlank(serviceType)){
            return Collections.emptyList();
        }
        List<String> schoolLevelList = new ArrayList<>();
        String[] arr = serviceType.split(",");
        for (String str : arr){
            AgentServiceType agentServiceType = AgentServiceType.nameOf(str);
            if(agentServiceType != null){
                schoolLevelList.add(agentServiceType.toSchoolLevel().toString());
            }
        }
        return schoolLevelList;
    }

    //过滤已完成的列表
    private List<AgentSubTask> getFinishList(List<AgentSubTask> subTaskList){
        if(CollectionUtils.isEmpty(subTaskList)){
            return Collections.emptyList();
        }
        return subTaskList.stream().filter(p -> p.getIfFollowUp() || p.getIfHomework()).collect(Collectors.toList());
    }
    //过滤已使用的列表
    private List<AgentSubTask> getUsedList(List<AgentSubTask> subTaskList){
        if(CollectionUtils.isEmpty(subTaskList)){
            return Collections.emptyList();
        }
        return subTaskList.stream().filter(AgentSubTask::getIfHomework).collect(Collectors.toList());
    }
    //过滤待跟进的列表
    private List<AgentSubTask> getToFollowList(List<AgentSubTask> subTaskList){
        if(CollectionUtils.isEmpty(subTaskList)){
            return Collections.emptyList();
        }
        return subTaskList.stream().filter( p -> p.getIfFollowUp() == false && p.getIfHomework() == false).collect(Collectors.toList());
    }

    private Set<Integer> getCountyCodeSet(Long groupId){
        Collection<ExRegion> counties = baseOrgService.getCountyRegionByGroupId(groupId);
        Set<Integer> countySet =counties.stream().filter(p -> p.getCountyCode() > 0).map(ExRegion::getCountyCode).collect(Collectors.toSet());
        return countySet;
    }

    /**
     * 获取学校相关的主任务
     * @param schoolId
     * @return
     */
    public AgentMainTask getMainTaskBySchoolId(Long schoolId){
        AgentMainTask agentMainTask = null;
        //当前学校相关的主任务
        List<AgentMainTask> mainTaskList = new ArrayList<>();
        //获取未结束的任务
        List<AgentMainTask> unEndMainTaskList = agentMainTaskDao.loadUnEndTaskList();
        if (CollectionUtils.isNotEmpty(unEndMainTaskList)){
            Map<String, AgentMainTask> mainTaskMap = unEndMainTaskList.stream().collect(Collectors.toMap(AgentMainTask::getId, Function.identity(), (o1, o2) -> o1));
            Map<String,List<AgentSubTask>> mainSubTaskMap = agentSubTaskDao.findSubTaskByMainTaskIds(mainTaskMap.keySet());
            mainSubTaskMap.forEach((k,v) -> {
                //当前学校未完成的子任务
                List<AgentSubTask> unfinishedSubTaskList = v.stream().filter(p -> !p.getIfFollowUp() && !p.getIfHomework() && Objects.equals(p.getSchoolId(), schoolId)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(unfinishedSubTaskList)){
                    mainTaskList.add(mainTaskMap.get(k));
                }
            });
        }
        //若存在多个主任务，返回结束时间最早的主任务
        if (CollectionUtils.isNotEmpty(mainTaskList)){
            agentMainTask = mainTaskList.stream().sorted(Comparator.comparing(AgentMainTask::getEndTime)).findFirst().orElse(null);
        }
        return agentMainTask;
    }
}
