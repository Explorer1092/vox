package com.voxlearning.utopia.agent.controller.datareport;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.athena.api.LoadNewSchoolService;
import com.voxlearning.athena.api.LoadTqParentService;
import com.voxlearning.athena.api.LoadTqXTestService;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.datareport.DataReportService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 数据报表
 * Created by yaguang.wang on 2016/10/9.
 */
@Controller
@RequestMapping("/workspace/reportdownload")
@Slf4j
public class DataReportController extends AbstractAgentController {

    @Inject private DataReportService dataReportService;

    @Inject
    private EmailServiceClient emailServiceClient;
    @ImportService(interfaceClass = LoadNewSchoolService.class)
    private LoadNewSchoolService loadNewSchoolService;
    @ImportService(interfaceClass = LoadTqParentService.class)
    private LoadTqParentService loadTqParentService;
    @ImportService(interfaceClass = LoadTqXTestService.class)
    private LoadTqXTestService loadTqXTestService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String date = DateUtils.dateToString(DayUtils.addDay(new Date(), -1), "yyyyMMdd");
        String xtestBrginDate = DateUtils.dateToString(DayUtils.addMonth(new Date(), -1), "yyyyMMdd");
        String xtestEndDate = DateUtils.dateToString(new Date(), "yyyyMMdd");
        //是否是小学
        Boolean isJuniorSchool = false;
        AgentGroup group = baseOrgService.getUserGroups(getCurrentUserId()).stream().findFirst().orElse(null);
        if (group != null && group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
            isJuniorSchool = true;
        }
        model.addAttribute("date", date);
        model.addAttribute("xtestBrginDate", xtestBrginDate);
        model.addAttribute("xtestEndDate", xtestEndDate);
        model.addAttribute("isJuniorSchool", isJuniorSchool);
        return "datareport/data_report";
    }

    @RequestMapping(value = "exportDataReport.vpage", method = RequestMethod.GET)
    @OperationCode("197659a1a5634a79")
    public void exportDataReport(HttpServletRequest request, HttpServletResponse response) {
        AgentGroup group = null;
        Integer mode = getRequestInt("mode", 1);     // 1:online,2:offline
        Long groupId = getRequestLong("groupId");
        Integer schoolType = getRequestInt("schoolType", 1);     // 1:字典表学校，2:非字典表学校

        Integer yesterday = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(new Date(), -1), "yyyyMMdd"));
        Integer day = getRequestInt("day", yesterday);
        Integer dataType = getRequestInt("dataType", 1);         // 1:学校  2:老师   3:班级

        String fileName = day + "{}-"+ (dataType == 1 ? "学校" : (dataType == 2 ? "老师" : "班级")) + (schoolType == 1 ? "" : "非") + "字典表数据报表-"+ (mode == 1 ? "online" : (mode == 2 ? "offline" : "家长"))+".xlsx";
        if(dataType == 1){
            fileName = day + "{}-学校" + (schoolType == 1 ? "" : "非") + "字典表数据报表-"+ (mode == 1 ? "online" : (mode == 2 ? "offline" : "家长"))+".xlsx";
        }else if(dataType == 2 || dataType == 3){
            fileName = day + "{}-"+ (dataType == 2 ? "老师" : "班级") + (schoolType == 1 ? "" : "非") + "字典表数据报表-"+ (mode == 1 ? "online" : (mode == 2 ? "offline" : "家长"))+".csv";
        }

        List<Long> ids = new ArrayList<>();
        Integer idType = 1;              // idType :  1:group, 2:manager, 3:region_code
        AuthCurrentUser currentUser = getCurrentUser();
        List<Integer> schoolLevels = new ArrayList<>();
        if(schoolType == 1){  // 字典表学校
            if(currentUser.isBusinessDeveloper()){
                idType = 2;
                ids.add(currentUser.getUserId());
                fileName = StringUtils.formatMessage(fileName,currentUser.getRealName());
                List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(currentUser.getUserId());
                if(CollectionUtils.isNotEmpty(groupUserList)){
                    group = baseOrgService.getGroupById(groupUserList.get(0).getGroupId());
                    if(group != null){
                        group.fetchServiceTypeList().forEach(p -> {
                            SchoolLevel schoolLevel = p.toSchoolLevel();
                            if(schoolLevel != null){
                                schoolLevels.add(schoolLevel.getLevel());
                            }
                        });
                    }
                }
            }else {
                if(groupId < 1){
                    List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(currentUser.getUserId());
                    if(CollectionUtils.isNotEmpty(groupIds)){
                        groupId = groupIds.get(0);
                    }
                }
                if(groupId > 0){
                    ids.add(groupId);
                    group = baseOrgService.getGroupById(groupId);
                    if(group != null){
                        group.fetchServiceTypeList().forEach(p -> {
                            SchoolLevel schoolLevel = p.toSchoolLevel();
                            if(schoolLevel != null){
                                schoolLevels.add(schoolLevel.getLevel());
                            }
                        });
                    }
                }
            }
        }else if(schoolType == 2){   // 非字典表学校，根据区域查询
            idType = 3;
            if(currentUser.isBusinessDeveloper() || currentUser.isCityManager()){
                List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(currentUser.getUserId());
                if(CollectionUtils.isNotEmpty(groupUserList)){
                    groupId = groupUserList.get(0).getGroupId();
                }
            }
            if(groupId < 1){
                List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(currentUser.getUserId());
                if(CollectionUtils.isNotEmpty(groupIds)){
                    groupId = groupIds.get(0);
                }
            }
            if(groupId != null && groupId > 0) {
                List<Integer> regionCodes = baseOrgService.getGroupRegionCodeList(groupId);
                List<Integer> countyCodes = agentRegionService.getCountyCodes(regionCodes);
                if (CollectionUtils.isNotEmpty(countyCodes)) {
                    countyCodes.forEach(p -> ids.add(SafeConverter.toLong(p)));
                }
                group = baseOrgService.getGroupById(groupId);
                if(group != null){
                    group.fetchServiceTypeList().forEach(p -> {
                        SchoolLevel schoolLevel = p.toSchoolLevel();
                        if(schoolLevel != null){
                            schoolLevels.add(schoolLevel.getLevel());
                        }
                    });
                }
            }
        }

        if(CollectionUtils.isEmpty(ids)){
            try {
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                response.getWriter().write("条件不正确");
                return;
            }catch (Exception e){
                logger.error("error info: ",e);
            }
        }


        MapMessage mapMessage = null;
        if (mode == 1 || mode == 2){
            if(dataType == 1){
                mapMessage = loadNewSchoolService.downloadSchool(mode, schoolType, day, ids, idType, schoolLevels);
            }else if(dataType == 2){
                mapMessage = loadNewSchoolService.downloadTeacherCSV(mode, schoolType, day, ids, idType, schoolLevels);
            }else if(dataType == 3){
                mapMessage = loadNewSchoolService.downloadClassCSV(mode, schoolType, day, ids, idType, schoolLevels);
            }
        }else if (mode == 3){
            if(dataType == 1){
                mapMessage = loadTqParentService.downloadSchoolExcel(ids,idType,schoolLevels,day,schoolType);
            }else if(dataType == 2){
                mapMessage = loadTqParentService.downloadTeacherExcel(ids,idType,schoolLevels,day);
            }else if(dataType == 3){
                mapMessage = loadTqParentService.downloadClassExcel(ids,idType,schoolLevels,day);
            }
        } else if (mode == 4) {
            Set<Integer> days = new HashSet<>();
            Integer xtestBeginDay = getRequestInt("xtestBeginDay", yesterday);
            Integer xtestEndDay = getRequestInt("xtestEndDay", yesterday);
            fileName = xtestBeginDay + "至" + xtestEndDay + "{}-" + (dataType==1 ? "考试":(dataType==2 ? "学校":"班级")) + "字典表数据报表-x测试.csv";

            Date beginDay = DateUtils.stringToDate(String.valueOf(xtestBeginDay), "yyyyMMdd");
            Date endDay = DateUtils.stringToDate(String.valueOf(xtestEndDay), "yyyyMMdd");
            Long beginRange = DayRange.newInstance(beginDay.getTime()).getStartTime();
            Long endRange = DayRange.newInstance(endDay.getTime()).getStartTime();
            days.add(xtestBeginDay);
            while (true) {
                Long currTime = DayRange.newInstance(beginRange).next().getStartTime();
                if (days.size() > 92) {//大于最大日期
                    break;
                }
                if (currTime >= endRange) {//大于最大日期
                    days.add(SafeConverter.toInt(DateUtils.dateToString(new Date(endRange), "yyyyMMdd")));
                    break;
                }

                beginRange = currTime;
                Integer currDay = SafeConverter.toInt(DateUtils.dateToString(new Date(currTime), "yyyyMMdd"));
                days.add(currDay);
            }
            List<Integer> dayList = new ArrayList<>(days);

            switch (dataType) {
                case 1:
                    //设置申请人
                    List<Long> xTestExamIds;
                    if(currentUser.isBusinessDeveloper()){
                        xTestExamIds = Collections.singletonList(currentUser.getUserId());
                    } else {
                        Set<Long> groupIdList = new HashSet<>();
                        groupIdList.add(groupId);
                        List<AgentGroup> agentGroups = baseOrgService.getSubGroupList(groupId);
                        List<Long> temp = Optional.ofNullable(agentGroups).orElse(new ArrayList<>())
                                .stream()
                                .map(AgentGroup::getId)
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(temp)) {
                            groupIdList.addAll(temp);
                        }
                        List<AgentRoleType> userRoles = new ArrayList<>(Arrays.asList(
                                AgentRoleType.BusinessDeveloper,
                                AgentRoleType.CityManager));
                        if (currentUser.isAreaManager()) {
                            userRoles.add(AgentRoleType.AreaManager);
                        } else if (currentUser.isRegionManager()) {
                            userRoles.add(AgentRoleType.Region);
                        } else if (currentUser.isCountryManager()) {
                            userRoles.add(AgentRoleType.Country);
                        }
                        xTestExamIds = baseOrgService.getUserByGroupIdsAndRoles(groupIdList, userRoles);
                    }
                    mapMessage = loadTqXTestService.downloadExamCsv(xTestExamIds, dayList);
                    break;
                case 2:
                    mapMessage = loadTqXTestService.downloadSchoolCsv(ids, idType, schoolLevels, dayList, schoolType);
                    break;
                case 3:
                    mapMessage = loadTqXTestService.downloadClassCsv(ids, idType, schoolLevels, dayList);
                    break;
            }
        }

        if(mapMessage != null && mapMessage.isSuccess() && mapMessage.containsKey("dataByte")){
            try {
                fileName = StringUtils.formatMessage(fileName, group != null && StringUtils.isNotBlank(group.getGroupName()) ? group.getGroupName() : "");
                byte[] bytes = (byte[]) mapMessage.get("dataByte");
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        dataType == 1 ? "application/vnd.ms-excel" : "application/csv;charset=UTF-8",
                        bytes);
            }catch (IOException e){
                logger.error("error info: ",e);
            }
        }else{
            String info = "";
            if(mapMessage != null && !mapMessage.isSuccess()){
                info = mapMessage.getInfo();
            }
            try {
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                response.getWriter().write(info);
                return;
            }catch (Exception e){
                logger.error("error info: ",e);
            }
        }
    }
//    @RequestMapping(value = "exportDataReport2.vpage", method = RequestMethod.GET)
//    @OperationCode("197659a1a5634a79")
//    public void exportDataReport2(HttpServletResponse response) {
//        String type = getRequestString("modeType");
//        int day = getRequestInt("day",0);
//        Long groupId = getRequestLong("groupId");
//
//        String dataType = requestString("dataType","");
//        AuthCurrentUser currentUser = getCurrentUser();
//        Long userId = null;  // 默认null
//        try {
//            if (day == 0){
//                response.setHeader("Content-type", "text/fhtml;charset=UTF-8");
//                response.getWriter().write("日期不正确");
//                return;
//            }
//            List<Long> schoolIds = new ArrayList<>();
//
//            String nowTime = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME);
//            String fileName = day + "{}-"+dataType+"数据报表"+ nowTime + "-{}"+".xlsx";
//            if (currentUser.isBusinessDeveloper() || currentUser.isCityManager()){
//                schoolIds = baseOrgService.getManagedSchoolList(getCurrentUserId());
//                if (currentUser.isBusinessDeveloper()){
//                    userId = currentUser.getUserId();
//                    groupId = null;
//                    if ("online".equals(type)){
//                        fileName = StringUtils.formatMessage(fileName,currentUser.getRealName(),"online");
//                    }else{
//                        fileName = StringUtils.formatMessage(fileName,currentUser.getRealName(),"offline");
//                    }
//                }
//                if (currentUser.isCityManager()){
//                    List<AgentGroup> groupList = baseOrgService.getUserGroups(currentUser.getUserId());
//                    AgentGroup group = null;
//                    if(CollectionUtils.isNotEmpty(groupList)){
//                        group = groupList.get(0);
//                        groupId = group.getId();
//                    }
//                    if ("online".equals(type)){
//                        fileName = StringUtils.formatMessage(fileName,group == null? "" : group.getGroupName(),"online");
//                    }else{
//                        fileName = StringUtils.formatMessage(fileName,group == null? "" : group.getGroupName(),"offline");
//                    }
//                }
//            } else {
//                AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
//                if(agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City && agentGroup.fetchGroupRoleType() == AgentGroupRoleType.Area){
//                    if (!"school".equals(dataType) && agentGroup.fetchGroupRoleType() != AgentGroupRoleType.City) {
//                        response.setHeader("Content-type", "text/html;charset=UTF-8");
//                        response.getWriter().write("请选择角色为分区的部门");
//                        return;
//                    }
//                    schoolIds = baseOrgService.getManagedSchoolListByGroupId(groupId);
//                    if ("online".equals(type)) {
//                        fileName = StringUtils.formatMessage(fileName, agentGroup.getGroupName(), "online");
//                    } else {
//                        fileName = StringUtils.formatMessage(fileName, agentGroup.getGroupName(), "offline");
//                    }
//                }else {
//                    response.setHeader("Content-type", "text/html;charset=UTF-8");
//                    response.getWriter().write("请选择角色为区域或分区的部门");
//                    return;
//                }
//            }
//
//            SXSSFWorkbook workbook = new SXSSFWorkbook();
//            //online模式数据下载
//            if ("online".equals(type)){
//                if ("school".equals(dataType)){
//                    Map<Long, SchoolOnlineReportData> schoolOnlineReportDataMap = dataReportService.getSchoolOnlineReportData(groupId, userId, day);
//                    /*
//                    1.根据学校所属“地区名称”拼音由A-Z排序
//                     */
//                    List<SchoolOnlineReportData> schoolOnlineReportDataList = schoolOnlineReportDataMap.values().stream()
//                            .sorted(Comparator.comparing(p->p.getCountyName()==null?"":p.getCountyName(),Collator.getInstance(java.util.Locale.CHINA))).collect(Collectors.toList());
//                    generalSchoolSheet(workbook,schoolOnlineReportDataList);
//                }
//                if ("teacher".equals(dataType)){
//                    Map<Long, TeacherOnlineReportData> teacherOnlineReportDataMap = dataReportService.getTeacherOnlineReportData(schoolIds, day);
//                    /*
//                    1.按照学校所属”地区名称”拼音由A-Z排序（主要排序依据）
//                    2.同地区，按照学校ID升序排列（次要排序依据1）
//                    3.同学校，按照老师注册日期由近到远排序（次要排序依据2）
//                     */
//                    List<TeacherOnlineReportData> teacherOnlineReportDataList = new ArrayList<>();
//                    List<TeacherOnlineReportData> teacherOnlineReportDataNotNullList = new ArrayList<>();
//                    List<TeacherOnlineReportData> teacherOnlineReportDataNullList = new ArrayList<>();
//                    teacherOnlineReportDataMap.values().forEach(p -> {
//                        if (null != p.getCountyName() && null != p.getSchoolId()){
//                            teacherOnlineReportDataNotNullList.add(p);
//                        }else{
//                            teacherOnlineReportDataNullList.add(p);
//                        }
//                    });
//                    teacherOnlineReportDataList.addAll(teacherOnlineReportDataNotNullList);
//                    //对“地区名称”不为空的列表排序
//                    Comparator<TeacherOnlineReportData> comparatorRegTime = (teacher1, teacher2) -> (teacher2.getRegTime()==null?DateUtils.stringToDate("1900-01-01 00:00:00"):teacher2.getRegTime()).compareTo(teacher1.getRegTime()==null?DateUtils.stringToDate("1900-01-01 00:00:00"):teacher1.getRegTime());
//                    teacherOnlineReportDataList = teacherOnlineReportDataList.stream()
//                            .sorted(Comparator.comparing(TeacherOnlineReportData::getCountyName,Collator.getInstance(java.util.Locale.CHINA)).thenComparingLong(TeacherOnlineReportData::getSchoolId).thenComparing(comparatorRegTime)).collect(Collectors.toList());
//                    //排序完，拼接上“地区名称”为空的列表
//                    teacherOnlineReportDataList.addAll(teacherOnlineReportDataNullList);
//                    generalTeacherSheet(workbook,teacherOnlineReportDataList);
//                }
////                if ("group".equals(dataType)){
////                    Map<Long, GroupOnlineReportData> groupOnlineReportDataMap = dataReportService.getGroupOnlineReportData(schoolIds, day);
////                    generalGroupSheet(workbook,groupOnlineReportDataMap.values());
////                }
//                if ("class".equals(dataType)){
//                    Map<Long, ClassOnlineReportData> classOnlineReportDataMap = dataReportService.getClassOnlineReportData(schoolIds, day);
//                    /*
//                    1.学校ID升序排列（主要排序依据）
//                    2.同学校，按照年级名称从小到大排列（次要排序依据1）
//                    3.同年级，按照班级名称从小到大排列（次要排序依据2）
//                     */
//                    List<ClassOnlineReportData> classOnlineReportDataList = classOnlineReportDataMap.values().stream()
//                            .filter(p -> p.getSchoolId() != null && p.getClazzLevel() != null && p.getClazzName() != null)
//                            .sorted(Comparator.comparingLong(ClassOnlineReportData::getSchoolId).thenComparingInt(ClassOnlineReportData::getClazzLevel).thenComparingInt(p -> SafeConverter.toInt(p.getClazzName().replaceAll("班", ""), 99))).collect(Collectors.toList());
//                    generalClassSheet(workbook,classOnlineReportDataList);
//                }
//                //offline模式数据下载
//            }else if ("offline".equals(type)){
//                List<SchoolLevel> schoolLevels = new ArrayList<>();
//                schoolLevels.add(SchoolLevel.MIDDLE);
//                schoolLevels.add(SchoolLevel.HIGH);
//                schoolIds = baseOrgService.getSchoolListByLevels(schoolIds,schoolLevels);
//                if ("school".equals(dataType)){
//                    Map<Long, SchoolOfflineReportData> schoolofflineReportDataMap = dataReportService.getSchoolOfflineReportData(schoolIds, day);
//                    /*
//                    1.根据学校所属“地区名称”拼音由A-Z排序
//                     */
//                    List<SchoolOfflineReportData> schoolOfflineReportDataList = schoolofflineReportDataMap.values().stream()
//                            .sorted(Comparator.comparing(p->p.getCountyName()==null?"":p.getCountyName(),Collator.getInstance(java.util.Locale.CHINA))).collect(Collectors.toList());
//                    generalSchoolSheetOffline(workbook,schoolOfflineReportDataList);
//                }
//                if ("teacher".equals(dataType)){
//                    Map<Long, TeacherOfflineReportData> teacherOfflineReportDataMap = dataReportService.getTeacherOfflineReportData(schoolIds, day);
//                     /*
//                    1.按照学校所属”地区名称”拼音由A-Z排序（主要排序依据）
//                    2.同地区，按照学校ID升序排列（次要排序依据1）
//                    3.同学校，按照老师注册日期由近到远排序（次要排序依据2）
//                     */
//                    List<TeacherOfflineReportData> teacherOfflineReportDataList = new ArrayList<>();
//                    List<TeacherOfflineReportData> teacherOfflineReportDataNotNullList = new ArrayList<>();
//                    List<TeacherOfflineReportData> teacherOfflineReportDataNullList = new ArrayList<>();
//                    teacherOfflineReportDataMap.values().forEach(p -> {
//                        if (null != p.getCountyName() && null != p.getSchoolId()){
//                            teacherOfflineReportDataNotNullList.add(p);
//                        }else{
//                            teacherOfflineReportDataNullList.add(p);
//                        }
//                    });
//                    teacherOfflineReportDataList.addAll(teacherOfflineReportDataNotNullList);
//                    //对“地区名称”不为空的列表排序
//                    Comparator<TeacherOfflineReportData> comparatorRegTime = (teacher1, teacher2) -> (teacher2.getRegTime()==null?DateUtils.stringToDate("1900-01-01 00:00:00"):teacher2.getRegTime()).compareTo(teacher1.getRegTime()==null?DateUtils.stringToDate("1900-01-01 00:00:00"):teacher1.getRegTime());
//                    teacherOfflineReportDataList = teacherOfflineReportDataList.stream()
//                            .sorted(Comparator.comparing(TeacherOfflineReportData::getCountyName,Collator.getInstance(java.util.Locale.CHINA)).thenComparingLong(TeacherOfflineReportData::getSchoolId).thenComparing(comparatorRegTime)).collect(Collectors.toList());
//                    //排序完，拼接上“地区名称”为空的列表
//                    teacherOfflineReportDataList.addAll(teacherOfflineReportDataNullList);
//                    generalTeacherSheetOffline(workbook,teacherOfflineReportDataList);
//                }
////                if ("group".equals(dataType)){
////                    Map<Long, GroupOfflineReportData> groupOfflineReportDataMap = dataReportService.getGroupOfflineReportData(schoolIds, day);
////                    generalGroupSheetOffline(workbook,groupOfflineReportDataMap.values());
////                }
//                if ("class".equals(dataType)){
//                    Map<Long, ClassOfflineReportData> classOfflineReportData = dataReportService.getClassOfflineReportData(schoolIds, day);
//                    /*
//                    1.学校ID升序排列（主要排序依据）
//                    2.同学校，按照年级名称从小到大排列（次要排序依据1）
//                    3.同年级，按照班级名称从小到大排列（次要排序依据2）
//                     */
//                    List<ClassOfflineReportData> classOnlineReportDataList = classOfflineReportData.values().stream()
//                            .filter(p -> p.getSchoolId() != null && p.getClazzLevel() != null && p.getClazzName() != null)
//                            .sorted(Comparator.comparingLong(ClassOfflineReportData::getSchoolId).thenComparingInt(ClassOfflineReportData::getClazzLevel).thenComparing(p -> SafeConverter.toInt(p.getClazzName().replaceAll("班", ""), 99))).collect(Collectors.toList());
//                    generalClassSheetOffline(workbook,classOnlineReportDataList);
//                }
//            }
//            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            workbook.write(outStream);
//            outStream.flush();
//            HttpRequestContextUtils.currentRequestContext().downloadFile(
//                    fileName,
//                    "application/vnd.ms-excel",
//                    outStream.toByteArray());
//            outStream.close();
//            workbook.dispose();
//        } catch (Exception ex) {
//            try {
//                response.setHeader("Content-type", "text/html;charset=UTF-8");
//                response.getWriter().write("所查询的数据不存在");
//            } catch (IOException e) {
//                logger.error("error info: ",e);
//                emailServiceClient.createPlainEmail()
//                        .body("error info: "+e)
//                        .subject("查询数据异常【" + RuntimeMode.current().getStageMode() + "】")
//                        .to("song.wang@17zuoye.com;chunlin.yu@17zuoye.com;deliang.che@17zuoye.com")
//                        .send();
//            }
//        }
//    }
//
//    private void generalSchoolSheet(SXSSFWorkbook workbook,Collection<SchoolOnlineReportData> dataList){
//        try {
//            Sheet sheet = workbook.createSheet("学校");
//            sheet.createFreezePane(0, 1, 0, 1);
//
//            Font font = workbook.createFont();
//            font.setFontName("宋体");
//            font.setFontHeightInPoints((short) 10);
//            Row firstRow = sheet.createRow(0);
//
//            CellStyle firstRowStyle = workbook.createCellStyle();
//            CellStyle firstRowStylePink = workbook.createCellStyle();
//            CellStyle firstRowStyleGreen = workbook.createCellStyle();
//            CellStyle firstRowStyleBlue = workbook.createCellStyle();
//            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyle.setFont(font);
//            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            firstRowStylePink.setFillForegroundColor(HSSFColor.ROSE.index);
//            firstRowStylePink.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStylePink.setFont(font);
//            firstRowStylePink.setAlignment(CellStyle.ALIGN_CENTER);
//            firstRowStyleGreen.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
//            firstRowStyleGreen.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyleGreen.setFont(font);
//            firstRowStyleGreen.setAlignment(CellStyle.ALIGN_CENTER);
//            firstRowStyleBlue.setFillForegroundColor(HSSFColor.LIGHT_TURQUOISE.index);
//            firstRowStyleBlue.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyleBlue.setFont(font);
//            firstRowStyleBlue.setAlignment(CellStyle.ALIGN_CENTER);
//            setCellValue(firstRow, 0, firstRowStyle, "日期");
//            setCellValue(firstRow, 1, firstRowStyle, "部门");
//            setCellValue(firstRow, 2, firstRowStyle, "负责人");
//            setCellValue(firstRow, 3, firstRowStyle, "城市");
//            setCellValue(firstRow, 4, firstRowStyle, "地区");
//            setCellValue(firstRow, 5, firstRowStyle, "学校ID");
//            setCellValue(firstRow, 6, firstRowStyle, "学校名称");
//            setCellValue(firstRow, 7, firstRowStyle, "学段");
//            setCellValue(firstRow, 8, firstRowStyle, "学制");
//            setCellValue(firstRow, 9, firstRowStyle, "英语起始年级");
//            setCellValue(firstRow, 10, firstRowStyle, "等级");
//            setCellValue(firstRow, 11, firstRowStyle, "渗透率");
//            setCellValue(firstRow, 12, firstRowStyle, "规模");
//            setCellValue(firstRow, 13, firstRowStyle, "累计注册学生");
//            setCellValue(firstRow, 14, firstRowStyle, "累计认证学生");
//            setCellValue(firstRow, 15, firstRowStyle, "本月注册学生");
//            setCellValue(firstRow, 16, firstRowStyle, "本月认证学生");
//            setCellValue(firstRow, 17, firstRowStylePink, "英语累计新增");
//            setCellValue(firstRow, 18, firstRowStylePink, "英语新增1套");
//            setCellValue(firstRow, 19, firstRowStylePink, "英语新增2套");
//            setCellValue(firstRow, 20, firstRowStylePink, "英语新增3套");
//            setCellValue(firstRow, 21, firstRowStylePink, "英语回流1套");
//            setCellValue(firstRow, 22, firstRowStylePink, "英语回流2套");
//            setCellValue(firstRow, 23, firstRowStylePink, "英语回流3套");
//            setCellValue(firstRow, 24, firstRowStylePink, "英语本月月活");
//            setCellValue(firstRow, 25, firstRowStylePink, "英语上月月活");
//            setCellValue(firstRow, 26, firstRowStyleGreen, "数学累计新增");
//            setCellValue(firstRow, 27, firstRowStyleGreen, "数学新增1套");
//            setCellValue(firstRow, 28, firstRowStyleGreen, "数学新增2套");
//            setCellValue(firstRow, 29, firstRowStyleGreen, "数学新增3套");
//            setCellValue(firstRow, 30, firstRowStyleGreen, "数学回流1套");
//            setCellValue(firstRow, 31, firstRowStyleGreen, "数学回流2套");
//            setCellValue(firstRow, 32, firstRowStyleGreen, "数学回流3套");
//            setCellValue(firstRow, 33, firstRowStyleGreen, "数学本月月活");
//            setCellValue(firstRow, 34, firstRowStyleGreen, "数学上月月活");
//            setCellValue(firstRow, 35, firstRowStyleBlue, "语文累计新增");
//            setCellValue(firstRow, 36, firstRowStyleBlue, "语文新增1套");
//            setCellValue(firstRow, 37, firstRowStyleBlue, "语文新增2套");
//            setCellValue(firstRow, 38, firstRowStyleBlue, "语文新增3套");
//            setCellValue(firstRow, 39, firstRowStyleBlue, "语文回流1套");
//            setCellValue(firstRow, 40, firstRowStyleBlue, "语文回流2套");
//            setCellValue(firstRow, 41, firstRowStyleBlue, "语文回流3套");
//            setCellValue(firstRow, 42, firstRowStyleBlue, "语文本月月活");
//            setCellValue(firstRow, 43, firstRowStyleBlue, "语文上月月活");
//            setCellValue(firstRow, 44, firstRowStyle, "最近拜访日期");
//
//            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
//            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            if (CollectionUtils.isNotEmpty(dataList)) {
//                Integer index = 1;
//                for (SchoolOnlineReportData data : dataList) {
//                    Row row = sheet.createRow(index++);
//                    List<Object> exportAbleData = data.getExportAbleData();
//                    if (CollectionUtils.isNotEmpty(exportAbleData)){
//                        for (int i = 0; i < exportAbleData.size(); i++) {
//                            setCellValue(row,i,cellStyle,exportAbleData.get(i));
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("error info: ",ex);
//            emailServiceClient.createPlainEmail()
//                    .body("error info: "+ex)
//                    .subject("online模式学校数据下载异常【" + RuntimeMode.current().getStageMode() + "】")
//                    .to("song.wang@17zuoye.com;chunlin.yu@17zuoye.com;deliang.che@17zuoye.com")
//                    .send();
//        }
//    }
//
//    private void generalSchoolSheetOffline(SXSSFWorkbook workbook,Collection<SchoolOfflineReportData> dataList){
//        try {
//            Sheet sheet = workbook.createSheet("学校");
//            sheet.createFreezePane(0, 1, 0, 1);
//
//            Font font = workbook.createFont();
//            font.setFontName("宋体");
//            font.setFontHeightInPoints((short) 10);
//            Row firstRow = sheet.createRow(0);
//
//            CellStyle firstRowStyle = workbook.createCellStyle();
//            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyle.setFont(font);
//            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            setCellValue(firstRow, 0, firstRowStyle, "日期");
//            setCellValue(firstRow, 1, firstRowStyle, "部门");
//            setCellValue(firstRow, 2, firstRowStyle, "负责人");
//            setCellValue(firstRow, 3, firstRowStyle, "城市");
//            setCellValue(firstRow, 4, firstRowStyle, "地区");
//            setCellValue(firstRow, 5, firstRowStyle, "学校ID");
//            setCellValue(firstRow, 6, firstRowStyle, "学校名称");
//            setCellValue(firstRow, 7, firstRowStyle, "学段");
//            setCellValue(firstRow, 8, firstRowStyle, "学制");
//            setCellValue(firstRow, 9, firstRowStyle, "等级");
//            setCellValue(firstRow, 10, firstRowStyle, "规模");
//            setCellValue(firstRow, 11, firstRowStyle, "快乐学考号数");
//            setCellValue(firstRow, 12, firstRowStyle, "本月扫描试卷老师数");
//            setCellValue(firstRow, 13, firstRowStyle, "普通扫描≥1次学生数");
//            setCellValue(firstRow, 14, firstRowStyle, "普通扫描≥3次学生数");
//            setCellValue(firstRow, 15, firstRowStyle, "上月普通扫描≥1次学生数");
//            setCellValue(firstRow, 16, firstRowStyle, "上月普通扫描≥3次学生数");
//            setCellValue(firstRow, 17, firstRowStyle, "本月数学大考扫描学生数");
//            setCellValue(firstRow, 18, firstRowStyle, "本月英语大考扫描学生数");
//            setCellValue(firstRow, 19, firstRowStyle, "本月语文大考扫描学生数");
//            setCellValue(firstRow, 20, firstRowStyle, "本月物理大考扫描学生数");
//            setCellValue(firstRow, 21, firstRowStyle, "本月化学大考扫描学生数");
//            setCellValue(firstRow, 22, firstRowStyle, "本月生物大考扫描学生数");
//            setCellValue(firstRow, 23, firstRowStyle, "本月政治大考扫描学生数");
//            setCellValue(firstRow, 24, firstRowStyle, "本月历史大考扫描学生数");
//            setCellValue(firstRow, 25, firstRowStyle, "本月地理大考扫描学生数");
//            setCellValue(firstRow, 26, firstRowStyle, "最近拜访日期");
//
//            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
//            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            if (CollectionUtils.isNotEmpty(dataList)) {
//                Integer index = 1;
//                for (SchoolOfflineReportData data : dataList) {
//                    Row row = sheet.createRow(index++);
//                    List<Object> exportAbleData = data.getExportAbleData();
//                    if (CollectionUtils.isNotEmpty(exportAbleData)){
//                        for (int i = 0; i < exportAbleData.size(); i++) {
//                            setCellValue(row,i,cellStyle,exportAbleData.get(i));
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("error info: ",ex);
//            emailServiceClient.createPlainEmail()
//                    .body("error info: "+ex)
//                    .subject("offline模式学校数据下载异常【" + RuntimeMode.current().getStageMode() + "】")
//                    .to("song.wang@17zuoye.com;chunlin.yu@17zuoye.com;deliang.che@17zuoye.com")
//                    .send();
//        }
//    }
//
//    private void generalTeacherSheet(SXSSFWorkbook workbook,Collection<TeacherOnlineReportData> dataList){
//        try {
//            Sheet sheet = workbook.createSheet("老师");
//            sheet.createFreezePane(0, 1, 0, 1);
//            Font font = workbook.createFont();
//            font.setFontName("宋体");
//            font.setFontHeightInPoints((short) 10);
//            CellStyle firstRowStyle = workbook.createCellStyle();
//            firstRowStyle.setFont(font);
//            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            Row firstRow = sheet.createRow(0);
//            setCellValue(firstRow, 0, firstRowStyle, "日期");
//            setCellValue(firstRow, 1, firstRowStyle, "负责人");
//            setCellValue(firstRow, 2, firstRowStyle, "城市");
//            setCellValue(firstRow, 3, firstRowStyle, "地区");
//            setCellValue(firstRow, 4, firstRowStyle, "学校ID");
//            setCellValue(firstRow, 5, firstRowStyle, "学校名称");
//            setCellValue(firstRow, 6, firstRowStyle, "学段");
//            setCellValue(firstRow, 7, firstRowStyle, "老师ID");
//            setCellValue(firstRow, 8, firstRowStyle, "老师姓名");
//            setCellValue(firstRow, 9, firstRowStyle, "注册日期");
//            setCellValue(firstRow, 10, firstRowStyle, "认证日期");
//            setCellValue(firstRow, 11, firstRowStyle, "是否认证");
//            setCellValue(firstRow, 12, firstRowStyle, "科目");
//            setCellValue(firstRow, 13, firstRowStyle, "带班数量");
//            setCellValue(firstRow, 14, firstRowStyle, "注册学生");
//            setCellValue(firstRow, 15, firstRowStyle, "认证学生");
//            setCellValue(firstRow, 16, firstRowStyle, "最近使用日期");
//            setCellValue(firstRow, 17, firstRowStyle, "本月布置作业");
//            setCellValue(firstRow, 18, firstRowStyle, "累计新增");
//            setCellValue(firstRow, 19, firstRowStyle, "新增1套");
//            setCellValue(firstRow, 20, firstRowStyle, "新增2套");
//            setCellValue(firstRow, 21, firstRowStyle, "新增3套");
//            setCellValue(firstRow, 22, firstRowStyle, "回流1套");
//            setCellValue(firstRow, 23, firstRowStyle, "回流2套");
//            setCellValue(firstRow, 24, firstRowStyle, "回流3套");
//            setCellValue(firstRow, 25, firstRowStyle, "本月月活");
//            setCellValue(firstRow, 26, firstRowStyle, "上月月活");
//            setCellValue(firstRow, 27, firstRowStyle, "是否布置期末作业包");
//            setCellValue(firstRow, 28, firstRowStyle, "是否布置暑假作业");
//
//            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
//            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            if (CollectionUtils.isNotEmpty(dataList)) {
//                Integer index = 1;
//                for (TeacherOnlineReportData data : dataList) {
//                    Row row = sheet.createRow(index++);
//                    List<Object> exportAbleData = data.getExportAbleData();
//                    if (CollectionUtils.isNotEmpty(exportAbleData)){
//                        for (int i = 0; i < exportAbleData.size(); i++) {
//                            setCellValue(row,i,cellStyle,exportAbleData.get(i));
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("error info: ",ex);
//            emailServiceClient.createPlainEmail()
//                    .body("error info: "+ex)
//                    .subject("online模式老师数据下载异常【" + RuntimeMode.current().getStageMode() + "】")
//                    .to("song.wang@17zuoye.com;chunlin.yu@17zuoye.com;deliang.che@17zuoye.com")
//                    .send();
//        }
//    }
//
//    private void generalTeacherSheetOffline(SXSSFWorkbook workbook,Collection<TeacherOfflineReportData> dataList){
//        try {
//            Sheet sheet = workbook.createSheet("老师");
//            sheet.createFreezePane(0, 1, 0, 1);
//            Font font = workbook.createFont();
//            font.setFontName("宋体");
//            font.setFontHeightInPoints((short) 10);
//            CellStyle firstRowStyle = workbook.createCellStyle();
//            firstRowStyle.setFont(font);
//            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            Row firstRow = sheet.createRow(0);
//            setCellValue(firstRow, 0, firstRowStyle, "日期");
//            setCellValue(firstRow, 1, firstRowStyle, "负责人");
//            setCellValue(firstRow, 2, firstRowStyle, "城市");
//            setCellValue(firstRow, 3, firstRowStyle, "地区");
//            setCellValue(firstRow, 4, firstRowStyle, "学校ID");
//            setCellValue(firstRow, 5, firstRowStyle, "学校名称");
//            setCellValue(firstRow, 6, firstRowStyle, "学段");
//            setCellValue(firstRow, 7, firstRowStyle, "等级");
//            setCellValue(firstRow, 8, firstRowStyle, "老师ID");
//            setCellValue(firstRow, 9, firstRowStyle, "老师姓名");
//            setCellValue(firstRow, 10, firstRowStyle, "注册日期");
//            setCellValue(firstRow, 11, firstRowStyle, "科目");
//            setCellValue(firstRow, 12, firstRowStyle, "快乐学考号数");
//            setCellValue(firstRow, 13, firstRowStyle, "本月扫描试卷套数");
//            setCellValue(firstRow, 14, firstRowStyle, "普通扫描≥1次学生数");
//            setCellValue(firstRow, 15, firstRowStyle, "普通扫描≥3次学生数");
//            setCellValue(firstRow, 16, firstRowStyle, "上月普通扫描≥1次学生数");
//            setCellValue(firstRow, 17, firstRowStyle, "上月普通扫描≥3次学生数");
//
//            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
//            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            if (CollectionUtils.isNotEmpty(dataList)) {
//                Integer index = 1;
//                for (TeacherOfflineReportData data : dataList) {
//                    Row row = sheet.createRow(index++);
//                    List<Object> exportAbleData = data.getExportAbleData();
//                    if (CollectionUtils.isNotEmpty(exportAbleData)){
//                        for (int i = 0; i < exportAbleData.size(); i++) {
//                            setCellValue(row,i,cellStyle,exportAbleData.get(i));
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("error info: ",ex);
//            emailServiceClient.createPlainEmail()
//                    .body("error info: "+ex)
//                    .subject("offline模式老师数据下载异常【" + RuntimeMode.current().getStageMode() + "】")
//                    .to("song.wang@17zuoye.com;chunlin.yu@17zuoye.com;deliang.che@17zuoye.com")
//                    .send();
//        }
//    }
//    private void generalGroupSheet(SXSSFWorkbook workbook,Collection<GroupOnlineReportData> dataList){
//        try {
//            Sheet sheet = workbook.createSheet("班组");
//            sheet.createFreezePane(0, 1, 0, 1);
//            Font font = workbook.createFont();
//            font.setFontName("宋体");
//            font.setFontHeightInPoints((short) 10);
//            CellStyle firstRowStyle = workbook.createCellStyle();
//            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyle.setFont(font);
//            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            Row firstRow = sheet.createRow(0);
//            setCellValue(firstRow, 0, firstRowStyle, "日期");
//            setCellValue(firstRow, 1, firstRowStyle, "负责人");
//            setCellValue(firstRow, 2, firstRowStyle, "城市");
//            setCellValue(firstRow, 3, firstRowStyle, "地区");
//            setCellValue(firstRow, 4, firstRowStyle, "学校ID");
//            setCellValue(firstRow, 5, firstRowStyle, "学校名称");
//            setCellValue(firstRow, 6, firstRowStyle, "学段");
//            setCellValue(firstRow, 7, firstRowStyle, "年级");
//            setCellValue(firstRow, 8, firstRowStyle, "班级");
//            setCellValue(firstRow, 9, firstRowStyle, "科目");
//            setCellValue(firstRow, 10, firstRowStyle, "老师ID");
//            setCellValue(firstRow, 11, firstRowStyle, "老师姓名");
//            setCellValue(firstRow, 12, firstRowStyle, "是否认证");
//            setCellValue(firstRow, 13, firstRowStyle, "注册学生");
//            setCellValue(firstRow, 14, firstRowStyle, "认证学生");
//            setCellValue(firstRow, 15, firstRowStyle, "最近使用日期");
//            setCellValue(firstRow, 16, firstRowStyle, "本月布置作业");
//            setCellValue(firstRow, 17, firstRowStyle, "所有学生-1套");
//            setCellValue(firstRow, 18, firstRowStyle, "所有学生-2套");
//            setCellValue(firstRow, 19, firstRowStyle, "科目月活-3套");
//            setCellValue(firstRow, 20, firstRowStyle, "上月月活");
//            setCellValue(firstRow, 21, firstRowStyle, "注册未新增");
//            setCellValue(firstRow, 22, firstRowStyle, "是否布置寒假作业");
//
//
//            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
//            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            if (CollectionUtils.isNotEmpty(dataList)) {
//                Integer index = 1;
//                for (GroupOnlineReportData data : dataList) {
//                    Row row = sheet.createRow(index++);
//                    List<Object> exportAbleData = data.getExportAbleData();
//                    if (CollectionUtils.isNotEmpty(exportAbleData)){
//                        for (int i = 0; i < exportAbleData.size(); i++) {
//                            setCellValue(row,i,cellStyle,exportAbleData.get(i));
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("error info: ",ex);
//            emailServiceClient.createPlainEmail()
//                    .body("error info: "+ex)
//                    .subject("online模式班组数据下载异常【" + RuntimeMode.current().getStageMode() + "】")
//                    .to("song.wang@17zuoye.com;chunlin.yu@17zuoye.com;deliang.che@17zuoye.com")
//                    .send();
//        }
//    }
//
//    private void generalGroupSheetOffline(SXSSFWorkbook workbook,Collection<GroupOfflineReportData> dataList){
//        try {
//            Sheet sheet = workbook.createSheet("班组");
//            sheet.createFreezePane(0, 1, 0, 1);
//            Font font = workbook.createFont();
//            font.setFontName("宋体");
//            font.setFontHeightInPoints((short) 10);
//            CellStyle firstRowStyle = workbook.createCellStyle();
//            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyle.setFont(font);
//            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            Row firstRow = sheet.createRow(0);
//            setCellValue(firstRow, 0, firstRowStyle, "日期");
//            setCellValue(firstRow, 1, firstRowStyle, "负责人");
//            setCellValue(firstRow, 2, firstRowStyle, "城市");
//            setCellValue(firstRow, 3, firstRowStyle, "地区");
//            setCellValue(firstRow, 4, firstRowStyle, "学校ID");
//            setCellValue(firstRow, 5, firstRowStyle, "学校名称");
//            setCellValue(firstRow, 6, firstRowStyle, "学段");
//            setCellValue(firstRow, 7, firstRowStyle, "等级");
//            setCellValue(firstRow, 8, firstRowStyle, "年级");
//            setCellValue(firstRow, 9, firstRowStyle, "班级");
//            setCellValue(firstRow, 10, firstRowStyle, "科目");
//            setCellValue(firstRow, 11, firstRowStyle, "老师ID");
//            setCellValue(firstRow, 12, firstRowStyle, "老师姓名");
//            setCellValue(firstRow, 13, firstRowStyle, "快乐学考号数");
//            setCellValue(firstRow, 14, firstRowStyle, "本月扫描试卷套数");
//            setCellValue(firstRow, 15, firstRowStyle, "普通扫描≥1次学生数");
//            setCellValue(firstRow, 16, firstRowStyle, "普通扫描≥3次学生数");
//            setCellValue(firstRow, 17, firstRowStyle, "上月普通扫描≥1次学生数");
//            setCellValue(firstRow, 18, firstRowStyle, "上月普通扫描≥3次学生数");
//
//            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
//            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            if (CollectionUtils.isNotEmpty(dataList)) {
//                Integer index = 1;
//                for (GroupOfflineReportData data : dataList) {
//                    Row row = sheet.createRow(index++);
//                    List<Object> exportAbleData = data.getExportAbleData();
//                    if (CollectionUtils.isNotEmpty(exportAbleData)){
//                        for (int i = 0; i < exportAbleData.size(); i++) {
//                            setCellValue(row,i,cellStyle,exportAbleData.get(i));
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("error info: ",ex);
//            emailServiceClient.createPlainEmail()
//                    .body("error info: "+ex)
//                    .subject("offline模式班组数据下载异常【" + RuntimeMode.current().getStageMode() + "】")
//                    .to("song.wang@17zuoye.com;chunlin.yu@17zuoye.com;deliang.che@17zuoye.com")
//                    .send();
//        }
//    }
//
//    /**
//     * 班级online数据导出Excel
//     * @param workbook
//     * @param dataList
//     */
//    private void generalClassSheet(SXSSFWorkbook workbook,Collection<ClassOnlineReportData> dataList){
//        try {
//            Sheet sheet = workbook.createSheet("班级");
//            sheet.createFreezePane(0, 1, 0, 1);
//            Font font = workbook.createFont();
//            font.setFontName("宋体");
//            font.setFontHeightInPoints((short) 10);
//            CellStyle firstRowStyle = workbook.createCellStyle();
//            CellStyle firstRowStylePink = workbook.createCellStyle();
//            CellStyle firstRowStyleGreen = workbook.createCellStyle();
//            CellStyle firstRowStyleBlue = workbook.createCellStyle();
//            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyle.setFont(font);
//            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            firstRowStylePink.setFillForegroundColor(HSSFColor.ROSE.index);
//            firstRowStylePink.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStylePink.setFont(font);
//            firstRowStylePink.setAlignment(CellStyle.ALIGN_CENTER);
//            firstRowStyleGreen.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
//            firstRowStyleGreen.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyleGreen.setFont(font);
//            firstRowStyleGreen.setAlignment(CellStyle.ALIGN_CENTER);
//            firstRowStyleBlue.setFillForegroundColor(HSSFColor.LIGHT_TURQUOISE.index);
//            firstRowStyleBlue.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyleBlue.setFont(font);
//            firstRowStyleBlue.setAlignment(CellStyle.ALIGN_CENTER);
//            Row firstRow = sheet.createRow(0);
//            setCellValue(firstRow, 0, firstRowStyle, "日期");
//            setCellValue(firstRow, 1, firstRowStyle, "负责人");
//            setCellValue(firstRow, 2, firstRowStyle, "城市");
//            setCellValue(firstRow, 3, firstRowStyle, "地区");
//            setCellValue(firstRow, 4, firstRowStyle, "学校ID");
//            setCellValue(firstRow, 5, firstRowStyle, "学校名称");
//            setCellValue(firstRow, 6, firstRowStyle, "学段");
//            setCellValue(firstRow, 7, firstRowStyle, "年级");
//            setCellValue(firstRow, 8, firstRowStyle, "班级");
//            setCellValue(firstRow, 9, firstRowStyle, "注册学生");
//            setCellValue(firstRow, 10, firstRowStyle, "认证学生");
//            setCellValue(firstRow, 11, firstRowStyle, "英语老师");
//            setCellValue(firstRow, 12, firstRowStyle, "数学老师");
//            setCellValue(firstRow, 13, firstRowStyle, "语文老师");
//            setCellValue(firstRow, 14, firstRowStyle, "本月布置英语作业");
//            setCellValue(firstRow, 15, firstRowStyle, "本月布置数学作业");
//            setCellValue(firstRow, 16, firstRowStyle, "本月布置语文作业");
//            setCellValue(firstRow, 17, firstRowStylePink, "英语累计新增");
//            setCellValue(firstRow, 18, firstRowStylePink, "英语新增1套");
//            setCellValue(firstRow, 19, firstRowStylePink, "英语新增2套");
//            setCellValue(firstRow, 20, firstRowStylePink, "英语新增3套");
//            setCellValue(firstRow, 21, firstRowStylePink, "英语回流1套");
//            setCellValue(firstRow, 22, firstRowStylePink, "英语回流2套");
//            setCellValue(firstRow, 23, firstRowStylePink, "英语回流3套");
//            setCellValue(firstRow, 24, firstRowStylePink, "英语本月月活");
//            setCellValue(firstRow, 25, firstRowStylePink, "英语上月月活");
//            setCellValue(firstRow, 26, firstRowStyleGreen, "数学累计新增");
//            setCellValue(firstRow, 27, firstRowStyleGreen, "数学新增1套");
//            setCellValue(firstRow, 28, firstRowStyleGreen, "数学新增2套");
//            setCellValue(firstRow, 29, firstRowStyleGreen, "数学新增3套");
//            setCellValue(firstRow, 30, firstRowStyleGreen, "数学回流1套");
//            setCellValue(firstRow, 31, firstRowStyleGreen, "数学回流2套");
//            setCellValue(firstRow, 32, firstRowStyleGreen, "数学回流3套");
//            setCellValue(firstRow, 33, firstRowStyleGreen, "数学本月月活");
//            setCellValue(firstRow, 34, firstRowStyleGreen, "数学上月月活");
//            setCellValue(firstRow, 35, firstRowStyleBlue, "语文累计新增");
//            setCellValue(firstRow, 36, firstRowStyleBlue, "语文新增1套");
//            setCellValue(firstRow, 37, firstRowStyleBlue, "语文新增2套");
//            setCellValue(firstRow, 38, firstRowStyleBlue, "语文新增3套");
//            setCellValue(firstRow, 39, firstRowStyleBlue, "语文回流1套");
//            setCellValue(firstRow, 40, firstRowStyleBlue, "语文回流2套");
//            setCellValue(firstRow, 41, firstRowStyleBlue, "语文回流3套");
//            setCellValue(firstRow, 42, firstRowStyleBlue, "语文本月月活");
//            setCellValue(firstRow, 43, firstRowStyleBlue, "语文上月月活");
//            setCellValue(firstRow, 44, firstRowStyleBlue, "布置期末作业包科目");
//            setCellValue(firstRow, 45, firstRowStyleBlue, "布置暑假作业科目");
//
//            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
//            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            if (CollectionUtils.isNotEmpty(dataList)) {
//                Integer index = 1;
//                for (ClassOnlineReportData data : dataList) {
//                    Row row = sheet.createRow(index++);
//                    List<Object> exportAbleData = data.getExportAbleData();
//                    if (CollectionUtils.isNotEmpty(exportAbleData)){
//                        for (int i = 0; i < exportAbleData.size(); i++) {
//                            setCellValue(row,i,cellStyle,exportAbleData.get(i));
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("error info: ",ex);
//            emailServiceClient.createPlainEmail()
//                    .body("error info: "+ex)
//                    .subject("online模式班级数据下载异常【" + RuntimeMode.current().getStageMode() + "】")
//                    .to("song.wang@17zuoye.com;chunlin.yu@17zuoye.com;deliang.che@17zuoye.com")
//                    .send();
//        }
//    }
//
//    /**
//     * 班级offline数据导出Excel
//     * @param workbook
//     * @param dataList
//     */
//    private void generalClassSheetOffline(SXSSFWorkbook workbook,Collection<ClassOfflineReportData> dataList){
//        try {
//            Sheet sheet = workbook.createSheet("班级");
//            sheet.createFreezePane(0, 1, 0, 1);
//            Font font = workbook.createFont();
//            font.setFontName("宋体");
//            font.setFontHeightInPoints((short) 10);
//            CellStyle firstRowStyle = workbook.createCellStyle();
//            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//            firstRowStyle.setFont(font);
//            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            Row firstRow = sheet.createRow(0);
//            setCellValue(firstRow, 0, firstRowStyle, "日期");
//            setCellValue(firstRow, 1, firstRowStyle, "负责人");
//            setCellValue(firstRow, 2, firstRowStyle, "城市");
//            setCellValue(firstRow, 3, firstRowStyle, "地区");
//            setCellValue(firstRow, 4, firstRowStyle, "学校ID");
//            setCellValue(firstRow, 5, firstRowStyle, "学校名称");
//            setCellValue(firstRow, 6, firstRowStyle, "学段");
//            setCellValue(firstRow, 7, firstRowStyle, "等级");
//            setCellValue(firstRow, 8, firstRowStyle, "年级");
//            setCellValue(firstRow, 9, firstRowStyle, "班级");
//            setCellValue(firstRow, 10, firstRowStyle, "老师");
//            setCellValue(firstRow, 11, firstRowStyle, "快乐学考号数");
//            setCellValue(firstRow, 12, firstRowStyle, "普通扫描≥1次学生数");
//            setCellValue(firstRow, 13, firstRowStyle, "普通扫描≥3次学生数");
//            setCellValue(firstRow, 14, firstRowStyle, "上月普通扫描≥1次学生数");
//            setCellValue(firstRow, 15, firstRowStyle, "上月普通扫描≥3次学生数");
//
//            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
//            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            if (CollectionUtils.isNotEmpty(dataList)) {
//                Integer index = 1;
//                for (ClassOfflineReportData data : dataList) {
//                    Row row = sheet.createRow(index++);
//                    List<Object> exportAbleData = data.getExportAbleData();
//                    if (CollectionUtils.isNotEmpty(exportAbleData)){
//                        for (int i = 0; i < exportAbleData.size(); i++) {
//                            setCellValue(row,i,cellStyle,exportAbleData.get(i));
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("error info: ",ex);
//            emailServiceClient.createPlainEmail()
//                    .body("error info: "+ex)
//                    .subject("offline模式班级数据下载异常【" + RuntimeMode.current().getStageMode() + "】")
//                    .to("song.wang@17zuoye.com;chunlin.yu@17zuoye.com;deliang.che@17zuoye.com")
//                    .send();
//        }
//    }
//    private void setCellValue(Row row, int column, CellStyle style, Object value) {
//        Cell cell = row.getCell(column);
//        if (cell == null) {
//            cell = row.createCell(column);
//        }
//        if (null != style){
//            cell.setCellStyle(style);
//        }
//        String info = value == null ? "" : String.valueOf(value).trim();
//        if (!NumberUtils.isDigits(info)) {
//            cell.setCellValue(info);
//        } else {
//            cell.setCellValue(SafeConverter.toLong(info));
//        }
//    }

    // 选择大区
    @RequestMapping(value = "searchRegion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchRegion() {
        return MapMessage.successMessage().add("resultList", dataReportService.loadDepartmentList(null));
    }

    // 选择城市
    @RequestMapping(value = "searchCity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchCity() {
        Long region = getRequestLong("groupId");
        if (region == 0L) {
            List<AgentRoleType> roles = new ArrayList<>();
            roles.add(AgentRoleType.Region);
            roles.add(AgentRoleType.AreaManager);
            MapMessage msg = getGroupId(getCurrentUserId(), roles);
            if (msg.isSuccess()) {
                region = SafeConverter.toLong(msg.get("groupId"));
            } else {
                return msg;
            }
        }
        String type = getRequestString("type");
        return MapMessage.successMessage().add("resultList", dataReportService.loadDepartmentList(region, type));
    }


    private MapMessage getGroupId(Long userId, Collection<AgentRoleType> roles) {
        List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(userId).stream().filter(p -> roles.contains(p.getUserRoleType())).collect(Collectors.toList());
        Set<Long> groupIds = groupUsers.stream().map(AgentGroupUser::getGroupId).collect(Collectors.toSet());
        List<AgentGroup> groups = baseOrgService.getGroupByIds(groupIds);
        if (CollectionUtils.isEmpty(groups)) {
            return MapMessage.errorMessage("该用户没有大区粒度权限");
        }
        return MapMessage.successMessage().add("groupId", groups.get(0).getId());
    }

    // 选择专员
    @RequestMapping(value = "searchBusinessDeveloper.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchBusinessDeveloper() {
        Long city = getRequestLong("groupId");
        if (city == 0L) {
            MapMessage msg = getGroupId(getCurrentUserId(), Collections.singleton(AgentRoleType.CityManager));
            if (msg.isSuccess()) {
                city = SafeConverter.toLong(msg.get("groupId"));
            } else {
                return msg;
            }
        }
        return MapMessage.successMessage().add("resultList", dataReportService.loadDepartmentUserList(city));
    }
}
