package com.voxlearning.utopia.agent.service.mobile.workrecord;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.workrecord.WorkRecordData;
import com.voxlearning.utopia.agent.dao.mongo.AgentWorkRecordStatisticsDao;
import com.voxlearning.utopia.agent.dao.mongo.workload.AgentRecordWorkloadDao;
import com.voxlearning.utopia.agent.persist.entity.AgentWorkRecordStatistics;
import com.voxlearning.utopia.agent.persist.entity.AgentWorkRecordStatisticsRoleData;
import com.voxlearning.utopia.agent.persist.entity.workload.AgentRecordWorkload;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskCenterService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentWorkRecordStatisticsService
 *
 * @author song.wang
 * @date 2018/1/23
 */
@Named
public class AgentWorkRecordStatisticsService extends AbstractAgentService {

    @Inject
    private AgentWorkRecordStatisticsDao agentWorkRecordStatisticsDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject
    private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;
    @Inject
    private AgentRecordWorkloadDao agentRecordWorkloadDao;
    @Inject
    private AgentTaskCenterService agentTaskCenterService;
    @Inject
    private WorkRecordService workRecordService;


    // 生成部门的统计数据
    public void generateGroupStatisticsData(List<AgentGroup> groupList, Date startDate, Date endDate, Integer dateType){

        if(CollectionUtils.isEmpty(groupList) || startDate == null || endDate == null || dateType == null){
            return;
        }

        List<AgentWorkRecordStatistics> dataList = new ArrayList<>();
        DayRange dayRange = DayRange.current();
        WeekRange weekRange = WeekRange.current();
        MonthRange monthRange = MonthRange.current();
        // 开始时间，结束时间不能超过当天早上
        if(!startDate.before(dayRange.getStartDate())){
            startDate = DateUtils.addDays(new Date(), -1);
        }
        if(endDate.getTime() > dayRange.getStartTime()){
            endDate = DayRange.current().getStartDate();
        }

        // 周，月的情况下重置开始时间，避免当周，当月数据不能生成的情况（如果startDate是上周五，今天是周三， startDate加一周后小于endTime, 当周数据就不会生成）
        if(dateType == 2){
            WeekRange tmpWeekRange = WeekRange.newInstance(startDate.getTime());
            startDate = tmpWeekRange.getStartDate();
        }else if(dateType == 3){
            MonthRange tmpMonthRange = MonthRange.newInstance(startDate.getTime());
            startDate = tmpMonthRange.getStartDate();
        }

        while (startDate.before(endDate)) {
            // 判断是否是当前日期
            boolean isCurrentDate = false;
            Date targetDate = startDate;
            if(dateType == 1){
                // 判断是否是当前天
                if(startDate.getTime() >= dayRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addDays(startDate, 1);
            }else if(dateType == 2){
                // 判断是否是当前周
                if(startDate.getTime() >= weekRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addWeeks(startDate, 1);
            }else if(dateType == 3){
                // 判断是否是当前月
                if(startDate.getTime() >= monthRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addMonths(startDate, 1);
            }

            Map<Long, AgentWorkRecordStatistics> groupIntoSchoolStatistics = new HashMap<>();
            if(!isCurrentDate){
                Map<Long, AgentWorkRecordStatistics> dataMap = getGroupRealTimeStatistics(groupList, targetDate, dateType);
                //获取团队专员进校统计情况（部门）
                groupIntoSchoolStatistics = getGroupRealTimeIntoSchoolStatistics(groupList, targetDate, dateType);
                if(MapUtils.isNotEmpty(dataMap)){
                    dataList.addAll(dataMap.values());
                }
            }else {
                // 当前周，当前月的情况下
                if(dateType == 2 || dateType == 3){
                    Map<Long, AgentWorkRecordStatistics> dataMap = getGroupRealTimeStatistics(groupList, targetDate, dateType);
                    //获取团队专员进校统计情况（部门）
                    groupIntoSchoolStatistics = getGroupRealTimeIntoSchoolStatistics(groupList, targetDate, dateType);
                    if(MapUtils.isNotEmpty(dataMap)){
                        dataList.addAll(dataMap.values());
                    }
                }
            }
            //拼装团队专员进校统计情况（部门）
            for (AgentWorkRecordStatistics workRecordStatistics : dataList){
                AgentWorkRecordStatistics intoSchoolStatistics = groupIntoSchoolStatistics.get(workRecordStatistics.getGroupId());
                if (null != intoSchoolStatistics){
                    workRecordStatistics.setPerPersonIntoSchoolCount(intoSchoolStatistics.getPerPersonIntoSchoolCount());
                    workRecordStatistics.setPerPersonVisitTeacherCount(intoSchoolStatistics.getPerPersonVisitTeacherCount());
                    workRecordStatistics.setPerPersonVisitMathTeacherCount(intoSchoolStatistics.getPerPersonVisitMathTeacherCount());
                    workRecordStatistics.setPerPersonVisitEngTeacherCount(intoSchoolStatistics.getPerPersonVisitEngTeacherCount());
                    workRecordStatistics.setPerPersonVisitOtherTeacherCount(intoSchoolStatistics.getPerPersonVisitOtherTeacherCount());
                }
            }

            // 删除现有数据
            Set<Long> groupIds = dataList.stream().map(AgentWorkRecordStatistics::getGroupId).collect(Collectors.toSet());
            agentWorkRecordStatisticsDao.disableData(groupIds, SafeConverter.toInt(DateUtils.dateToString(getStartDate(targetDate, dateType), "yyyyMMdd")), dateType, 1);
            // 插入
            agentWorkRecordStatisticsDao.inserts(dataList);
            dataList.clear();

            if(isCurrentDate){
                break;
            }
        }
    }

    private boolean isCurrentDate(Date date, Integer dateType){
        boolean isCurrentDate = false;
        DayRange dayRange = DayRange.current();
        WeekRange weekRange = WeekRange.current();
        MonthRange monthRange = MonthRange.current();
        if(dateType == 1){
            // 判断是否是当前天
            if(date.getTime() >= dayRange.getStartTime()){
                isCurrentDate = true;
            }
        }else if(dateType == 2){
            // 判断是否是当前周
            if(date.getTime() >= weekRange.getStartTime()){
                isCurrentDate = true;
            }
        }else if(dateType == 3){
            // 判断是否是当前月
            if(date.getTime() >= monthRange.getStartTime()){
                isCurrentDate = true;
            }
        }
        return isCurrentDate;
    }

    public boolean isCurrentDatePub(Date date, Integer dateType){
        return isCurrentDate(date,dateType);
    }

    private Date getStartDate(Date date, Integer dateType){
        Date targetDate = DayRange.current().getStartDate();
        if(dateType == 1){
            targetDate = DayRange.newInstance(date.getTime()).getStartDate();
        }else if(dateType == 2){
            targetDate = WeekRange.newInstance(date.getTime()).getStartDate();
        }else if(dateType == 3){
            targetDate = MonthRange.newInstance(date.getTime()).getStartDate();
        }
        return targetDate;
    }

    public Date getStartDatePub(Date date, Integer dateType){
        return getStartDate(date,dateType);
    }

    private Date getEndDate(Date date, Integer dateType){
        Date targetDate = new Date();
        if(dateType == 1){
            targetDate = DayRange.newInstance(DateUtils.addDays(date, 1).getTime()).getStartDate();
        }else if(dateType == 2){
            targetDate = WeekRange.newInstance(DateUtils.addWeeks(date, 1).getTime()).getStartDate();
        }else if(dateType == 3){
            targetDate = MonthRange.newInstance(DateUtils.addMonths(date, 1).getTime()).getStartDate();
        }
        return targetDate;
    }

    public Date getEndDatePub(Date date, Integer dateType){
        return getEndDate(date,dateType);
    }



    private AgentWorkRecordStatistics generateStatisticsDataForGroup(AgentGroup group, Date date, Integer dateType){
        Date startDate = getStartDate(date, dateType);
        Date endDate = getEndDate(date, dateType);

        List<AgentGroupUser> groupUserList = baseOrgService.getAllGroupUsersByGroupId(group.getId());
        List<AgentRoleType> roleTypeList = new ArrayList<>();
        roleTypeList.add(AgentRoleType.BusinessDeveloper);
        if (group.fetchGroupRoleType() == AgentGroupRoleType.Country){
            roleTypeList.add(AgentRoleType.Region);
            roleTypeList.add(AgentRoleType.AreaManager);
            roleTypeList.add(AgentRoleType.CityManager);
        }else if(group.fetchGroupRoleType() == AgentGroupRoleType.Marketing){
            roleTypeList.add(AgentRoleType.Region);
            if(group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                roleTypeList.add(AgentRoleType.AreaManager);
            }
            roleTypeList.add(AgentRoleType.CityManager);
        }else if(group.fetchGroupRoleType() == AgentGroupRoleType.Region){
            AgentGroup businessUnit = baseOrgService.getParentGroupByRole(group.getId(), AgentGroupRoleType.Marketing);
            if(businessUnit != null && businessUnit.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                roleTypeList.add(AgentRoleType.AreaManager);
            }
            roleTypeList.add(AgentRoleType.CityManager);
        }else if (group.fetchGroupRoleType() == AgentGroupRoleType.Area){
            roleTypeList.add(AgentRoleType.CityManager);
        }

        groupUserList = groupUserList.stream().filter(p -> roleTypeList.contains(p.getUserRoleType())).collect(Collectors.toList());
        AgentWorkRecordStatistics statisticsData = new AgentWorkRecordStatistics(SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd")), dateType, 1, group.getId(), group.getGroupName(), group.getRoleId());
        AgentGroup parentGroup = baseOrgService.getGroupById(group.getParentId());
        if(parentGroup != null){
            statisticsData.setParentGroupId(parentGroup.getId());
            statisticsData.setParentGroupName(parentGroup.getGroupName());
        }

        Map<AgentRoleType, List<Long>> roleUserMap = new HashMap<>();
        List<Long> allUserIdList = new ArrayList<>();
        groupUserList.forEach(p -> {
            List<Long> itemList = roleUserMap.get(p.getUserRoleType());
            if(CollectionUtils.isEmpty(itemList)){
                itemList = new ArrayList<>();
                roleUserMap.put(p.getUserRoleType(), itemList);
            }
            itemList.add(p.getUserId());
            allUserIdList.add(p.getUserId());
        });

        List<WorkRecordData> workRecordDataList = workRecordService.getWorkRecordDataListByUserTypeTime(allUserIdList, null, startDate, endDate);

        Map<Long, List<WorkRecordData>> userRecordMap = workRecordDataList.stream().collect(Collectors.groupingBy(WorkRecordData::getUserId, Collectors.toList()));

        List<Integer> workDayList = getWorkDayList(startDate, endDate);  // 工作日列表

        Map<Integer, AgentWorkRecordStatisticsRoleData> roleDataMap = new HashMap<>();
        Map<Integer, Future<AgentWorkRecordStatisticsRoleData>> futureDataMap = new HashMap<>();
        for(AgentRoleType k : roleUserMap.keySet()){
            List<Long> roleUserList = roleUserMap.get(k);
            Future<AgentWorkRecordStatisticsRoleData> futureData = AlpsThreadPool.getInstance().submit(() -> generateGroupRoleData(roleUserList, userRecordMap, workDayList, dateType));
            futureDataMap.put(k.getId(), futureData);
        }
        if(MapUtils.isNotEmpty(futureDataMap)){
            for(Integer role : futureDataMap.keySet()){
                try {
                    AgentWorkRecordStatisticsRoleData roleData = futureDataMap.get(role).get();
                    if(roleData != null){
                        roleDataMap.put(role, roleData);
                    }
                }catch (Exception e){
                }
            }
        }
        statisticsData.setRoleDataMap(roleDataMap);
        return statisticsData;
    }

    private AgentWorkRecordStatisticsRoleData generateGroupRoleData(List<Long> roleUserList, Map<Long, List<WorkRecordData>> userRecordMap, List<Integer> workDayList, Integer dateType){
        if(CollectionUtils.isEmpty(roleUserList)){
            return null;
        }
        if(workDayList == null){
            workDayList = new ArrayList<>();
        }
        AgentWorkRecordStatisticsRoleData roleData = new AgentWorkRecordStatisticsRoleData();
        roleData.setUserCount(roleUserList.size());

        int fillRecordUserCount = 0;
        if(MapUtils.isNotEmpty(userRecordMap)){
            fillRecordUserCount = ((Long)userRecordMap.keySet().stream().filter(roleUserList::contains).count()).intValue();
        }
        roleData.setFillRecordUserCount(fillRecordUserCount);

        int recordUnreachedUserCount = 0;
        int workDays = workDayList.size();
        //所有工作天数
        List<Integer> recordDayListAll = new ArrayList<>();

        if(dateType == 1){  // 日， 当天未录入
            recordUnreachedUserCount = roleUserList.size() - fillRecordUserCount;
        }else if((dateType == 2 && workDays >= 3) || (dateType == 3 && workDays >= 5)){ // 周,  3天未录入    月,  5天未录入
            int targetDays = dateType == 2 ? 3 : 5;
            for(Long u : roleUserList){
                Set<Integer> recordDayList = new HashSet<>();
                List<WorkRecordData> crmWorkRecordList = new ArrayList<>();
                if(MapUtils.isNotEmpty(userRecordMap) && userRecordMap.containsKey(u)){
                    crmWorkRecordList = userRecordMap.get(u);
                }
                if(CollectionUtils.isNotEmpty(crmWorkRecordList)){
                    recordDayList.addAll(crmWorkRecordList.stream().map(r -> SafeConverter.toInt(DateUtils.dateToString(r.getWorkTime(), "yyyyMMdd"))).collect(Collectors.toList()));
                    recordDayListAll.addAll(recordDayList);
                }
                // 工作日为填写记录的天数
                long unWorkDays = workDayList.stream().filter(p -> !recordDayList.contains(p)).count();
                if(unWorkDays >= targetDays){
                    recordUnreachedUserCount++;
                }
            }
        }
        roleData.setRecordUnreachedUserCount(recordUnreachedUserCount);
        roleData.setPerCapitaWorkload(calPerCapitaWorkload(roleUserList, userRecordMap, workDays));
        //平均工作天数
        roleData.setPerCapitaWorkDayNum(MathUtils.doubleDivide(recordDayListAll.size(),fillRecordUserCount,1));
        //平均陪访工作量
        roleData.setPerCapitalVisitWorkload(calPerCapitaAccompanyVisitWorkload(roleUserList,userRecordMap));
        //计算人均工作
        calPerCapitaWork(roleData,roleUserList,userRecordMap);

        return roleData;
    }

    /**
     * 计算人均工作
     * @param roleData
     * @param roleUserList
     * @param userRecordMap
     */
    public void calPerCapitaWork(AgentWorkRecordStatisticsRoleData roleData, List<Long> roleUserList, Map<Long, List<WorkRecordData>> userRecordMap){
        Set<Long> intoSchoolUserIds = new HashSet<>();
        List<Long> intoSchoolIds = new ArrayList<>();
        List<Long> visitTeaIds = new ArrayList<>();
        List<Long> visitEngTeaIds = new ArrayList<>();
        List<Long> visitMathTeaIds = new ArrayList<>();
        List<Long> visitChiTeaIds = new ArrayList<>();

        Set<Long> accompanyVisitUserList = new HashSet<>();
        int accompanyVisitNum = 0;
        Set<Long> resourceExtensionUserIds = new HashSet<>();
        int resourceExtensionVisitKpNum = 0;
        Set<Long> meetingUserList = new HashSet<>();
        int meetingNum = 0;
        Map<Long,Set<Long>> userSchoolIds = new HashMap<>();
        Map<Long,Set<Long>> userTeacherIds = new HashMap<>();
        Map<Long,Set<Long>> userEngTeacherIds = new HashMap<>();
        Map<Long,Set<Long>> userMathTeacherIds = new HashMap<>();
        Map<Long,Set<Long>> userChiTeacherIds = new HashMap<>();
        for(Long userId : roleUserList) {
            List<WorkRecordData> recordList = userRecordMap.get(userId);
            if(CollectionUtils.isEmpty(recordList)){
                continue;
            }
            Set<Long> schoolIds = userSchoolIds.get(userId);
            if (CollectionUtils.isEmpty(schoolIds)){
                schoolIds = new HashSet<>();
            }

            Set<Long> teacherIds = userTeacherIds.get(userId);
            if (CollectionUtils.isEmpty(teacherIds)){
                teacherIds = new HashSet<>();
            }

            Set<Long> engTeacherIds = userEngTeacherIds.get(userId);
            if (CollectionUtils.isEmpty(engTeacherIds)){
                engTeacherIds = new HashSet<>();
            }

            Set<Long> mathTeacherIds = userMathTeacherIds.get(userId);
            if (CollectionUtils.isEmpty(mathTeacherIds)){
                mathTeacherIds = new HashSet<>();
            }

            Set<Long> chiTeacherIds = userChiTeacherIds.get(userId);
            if (CollectionUtils.isEmpty(chiTeacherIds)){
                chiTeacherIds = new HashSet<>();
            }

            for (WorkRecordData workRecord : recordList) {
                //进校
                if (workRecord.getWorkType() == AgentWorkRecordType.SCHOOL){
                    intoSchoolUserIds.add(workRecord.getUserId());

                    schoolIds.add(workRecord.getSchoolId());

                    //过滤出有科目的拜访老师
                    workRecordService.generateVisitTeacherInfo(workRecord.getVisitUserInfoList(),engTeacherIds,mathTeacherIds,chiTeacherIds,teacherIds);
                    //陪访
                }else if (workRecord.getWorkType() == AgentWorkRecordType.ACCOMPANY){
                    accompanyVisitUserList.add(workRecord.getUserId());
                    accompanyVisitNum ++;
                    //资源拓维
                }else if (workRecord.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION){
                    resourceExtensionUserIds.add(workRecord.getUserId());
                    List<WorkRecordVisitUserInfo> visitUserInfoList = workRecord.getVisitUserInfoList();
                    if (CollectionUtils.isNotEmpty(visitUserInfoList)){
                        //资源拓维仅显示拜访的KP
                        List<WorkRecordVisitUserInfo> visitKpInfoList = visitUserInfoList.stream().filter(p -> p != null && p.getJob() != null && !Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)).collect(Collectors.toList());
                        resourceExtensionVisitKpNum += visitKpInfoList.size();
                    }
                    //组会
                }else if (workRecord.getWorkType() == AgentWorkRecordType.MEETING){
                    meetingUserList.add(workRecord.getUserId());
                    meetingNum ++;
                }
            }
            userSchoolIds.put(userId,schoolIds);
            userTeacherIds.put(userId,teacherIds);
            userEngTeacherIds.put(userId,engTeacherIds);
            userMathTeacherIds.put(userId,mathTeacherIds);
            userChiTeacherIds.put(userId,chiTeacherIds);
        }
        userSchoolIds.values().forEach(intoSchoolIds::addAll);
        userTeacherIds.values().forEach(visitTeaIds::addAll);
        userEngTeacherIds.values().forEach(visitEngTeaIds::addAll);
        userMathTeacherIds.values().forEach(visitMathTeaIds::addAll);
        userChiTeacherIds.values().forEach(visitChiTeaIds::addAll);
        //进校见师人数
        int intoSchoolUserNum = intoSchoolUserIds.size();
        roleData.setPerCapitaIntoSchoolNum(MathUtils.doubleDivide(intoSchoolIds.size(),intoSchoolUserNum,1));
        roleData.setPerCapitaVisitTeaNum(MathUtils.doubleDivide(visitTeaIds.size(),intoSchoolUserNum,1));
        roleData.setPerCapitaVisitChiTeaNum(MathUtils.doubleDivide(visitChiTeaIds.size(),intoSchoolUserNum,1));
        roleData.setPerCapitaVisitMathTeaNum(MathUtils.doubleDivide(visitMathTeaIds.size(),intoSchoolUserNum,1));
        roleData.setPerCapitaVisitEngTeaNum(MathUtils.doubleDivide(visitEngTeaIds.size(),intoSchoolUserNum,1));

        roleData.setPerCapitaAccompanyVisitNum(MathUtils.doubleDivide(accompanyVisitNum,accompanyVisitUserList.size(),1));
        roleData.setPerCapitaVisitResearcherNum(MathUtils.doubleDivide(resourceExtensionVisitKpNum,resourceExtensionUserIds.size(),1));
        roleData.setPerCapitaMeetingNum(MathUtils.doubleDivide(meetingNum,meetingUserList.size(),1));
    }

    // 生成User的统计数据
    public void generateUserStatisticsData(Collection<Long> userIdList, Date startDate, Date endDate, Integer dateType){

        if(CollectionUtils.isEmpty(userIdList) || startDate == null || endDate == null || dateType == null){
            return;
        }

        List<AgentWorkRecordStatistics> dataList = new ArrayList<>();
        DayRange dayRange = DayRange.current();
        WeekRange weekRange = WeekRange.current();
        MonthRange monthRange = MonthRange.current();
        // 开始时间，结束时间不能超过当天早上
        if(!startDate.before(dayRange.getStartDate())){
            startDate = DateUtils.addDays(new Date(), -1);
        }
        if(endDate.getTime() > dayRange.getStartTime()){
            endDate = DayRange.current().getStartDate();
        }

        // 周，月的情况下重置开始时间，避免当周，当月数据不能生成的情况（如果startDate是上周五，今天是周三， startDate加一周后小于endTime, 当周数据就不会生成）
        if(dateType == 2){
            WeekRange tmpWeekRange = WeekRange.newInstance(startDate.getTime());
            startDate = tmpWeekRange.getStartDate();
        }else if(dateType == 3){
            MonthRange tmpMonthRange = MonthRange.newInstance(startDate.getTime());
            startDate = tmpMonthRange.getStartDate();
        }
        while (startDate.before(endDate)) {
            // 判断是否是当前日期
            boolean isCurrentDate = false;
            Date targetDate = startDate;
            if(dateType == 1){
                // 判断是否是当前天
                if(startDate.getTime() >= dayRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addDays(startDate, 1);
            }else if(dateType == 2){
                // 判断是否是当前周
                if(startDate.getTime() >= weekRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addWeeks(startDate, 1);
            }else if(dateType == 3){
                // 判断是否是当前月
                if(startDate.getTime() >= monthRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addMonths(startDate, 1);
            }

            List<AgentWorkRecordStatistics> intoSchoolStatisticsList = new ArrayList<>();
            if(!isCurrentDate){
                dataList.addAll(generateStatisticsDataForUser(userIdList, targetDate, dateType));
                //团队专员进校工作记录统计（专员）
                intoSchoolStatisticsList.addAll(generateIntoSchoolStatisticsDataForUser(userIdList, targetDate, dateType));
            }else {
                // 当前周，当前月的情况下
                if(dateType == 2 || dateType == 3){
                    dataList.addAll(generateStatisticsDataForUser(userIdList, targetDate, dateType));
                    //团队专员进校工作记录统计（专员）
                    intoSchoolStatisticsList.addAll(generateIntoSchoolStatisticsDataForUser(userIdList, targetDate, dateType));
                }
            }
            //拼装团队专员进校工作记录统计
            Map<Long, AgentWorkRecordStatistics> userStatisticsMap = intoSchoolStatisticsList.stream().collect(Collectors.toMap(AgentWorkRecordStatistics::getUserId, Function.identity(), (o1, o2) -> o1));
            dataList.forEach(item -> {
                AgentWorkRecordStatistics intoSchoolStatistics = userStatisticsMap.get(item.getUserId());
                if (null != intoSchoolStatistics){
                    item.setBdIntoSchoolCount(intoSchoolStatistics.getBdIntoSchoolCount());
                    item.setBdVisitTeacherCount(intoSchoolStatistics.getBdVisitTeacherCount());
                    item.setBdVisitMathTeacherCount(intoSchoolStatistics.getBdVisitMathTeacherCount());
                    item.setBdVisitEngTeacherCount(intoSchoolStatistics.getBdVisitEngTeacherCount());
                    item.setBdVisitOtherTeacherCount(intoSchoolStatistics.getBdVisitOtherTeacherCount());
                }
            });

            // 删除现有数据
            Set<Long> userIds = dataList.stream().map(AgentWorkRecordStatistics::getUserId).collect(Collectors.toSet());
            agentWorkRecordStatisticsDao.disableData(userIds, SafeConverter.toInt(DateUtils.dateToString(getStartDate(targetDate, dateType), "yyyyMMdd")), dateType, 2);
            // 插入
            agentWorkRecordStatisticsDao.inserts(dataList);
            dataList.clear();
            if(isCurrentDate){
                break;
            }
        }
    }


    private List<AgentWorkRecordStatistics> generateStatisticsDataForUser(Collection<Long> userIds, Date date, Integer dateType){
        List<AgentWorkRecordStatistics> dataList = new ArrayList<>();

        Date startDate = getStartDate(date, dateType);
        Date endDate = getEndDate(date, dateType);

        List<WorkRecordData> workRecordDataList = workRecordService.getWorkRecordDataListByUserTypeTime(userIds, null, startDate, endDate);
        Map<Long, List<WorkRecordData>> userRecordMap = workRecordDataList.stream().collect(Collectors.groupingBy(WorkRecordData::getUserId, Collectors.toList()));

        Map<Long, AgentGroupUser> userGroupMap = new HashMap<>();
        Map<Long, List<AgentGroupUser>> userGroupListMap = agentGroupUserLoaderClient.findByUserIds(userIds);
        userGroupListMap.forEach((k, v) -> {
            if(CollectionUtils.isNotEmpty(v)){
                userGroupMap.put(k, v.get(0));
            }
        });
        Map<Long, AgentGroup> groupMap = new HashMap<>();
        Set<Long> userGroupIds = userGroupMap.values().stream().map(AgentGroupUser::getGroupId).collect(Collectors.toSet());
        Map<Long, AgentGroup> tem1 = agentGroupLoaderClient.loads(userGroupIds);
        if(MapUtils.isNotEmpty(tem1)){
            groupMap.putAll(tem1);
            Set<Long> parentGroupIds = tem1.values().stream().map(AgentGroup::getParentId).collect(Collectors.toSet());
            groupMap.putAll(agentGroupLoaderClient.loads(parentGroupIds));
        }

        Map<Long, AgentUser> userMap = agentUserLoaderClient.findByIds(userIds);

        List<Integer> workDayList = getWorkDayList(startDate, endDate);  // 工作日列表

        List<Future<AgentWorkRecordStatistics>> futureList = new ArrayList<>();
        userIds.forEach(p -> {
            AgentUser user = userMap.get(p);

            AgentGroupUser groupUser = userGroupMap.get(p);

            if (groupUser != null){
                Long groupId = groupUser.getGroupId();
                AgentGroup group = groupMap.get(groupId);
                if (group != null){
                    AgentGroup parentGroup = groupMap.get(group.getParentId());

            List<WorkRecordData> workRecordList = userRecordMap.get(p);

                    Future<AgentWorkRecordStatistics> futureData = AlpsThreadPool.getInstance().submit(() -> generateUserStatisticsData(user, groupUser.getUserRoleId(), group, parentGroup, workRecordList, workDayList, startDate, dateType));
                    futureList.add(futureData);
                }
            }
        });

        for(Future<AgentWorkRecordStatistics> futureData : futureList){
            try {
                AgentWorkRecordStatistics data = futureData.get();
                if (data != null) {
                    dataList.add(data);
                }
            }catch (Exception e){

            }
        }

        return dataList;
    }

    private AgentWorkRecordStatistics generateUserStatisticsData(AgentUser user, Integer userRole, AgentGroup group, AgentGroup parentGroup, List<WorkRecordData> workRecordList, List<Integer> workDayList, Date startDate, Integer dateType){
        if(user == null || group == null || startDate == null || dateType == null || userRole == null){
            return null;
        }
        if(workDayList == null){
            workDayList = new ArrayList<>();
        }

        AgentWorkRecordStatistics statisticsData = new AgentWorkRecordStatistics(SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd")), dateType, 2, group.getId(), group.getGroupName(), group.getRoleId());
        if(parentGroup != null){
            statisticsData.setParentGroupId(parentGroup.getId());
            statisticsData.setParentGroupName(parentGroup.getGroupName());
        }

        statisticsData.setUserId(user.getId());
        statisticsData.setUserName(user.getRealName());
        statisticsData.setUserRoleId(userRole);

        int userAccompanyVisitNum = 0;
        Double userIntoSchoolWorkload = 0d;     // 进校工作量
        Double userVisitWorkload = 0d;          // 陪访工作量
        Double userMeetingWorkload = 0d;        // 组会&参与组会工作量
        Double userTeachingWorkload = 0d;       // 拜访教研员工作量
        Double userWorkload = 0d;               // 总工作量

        int recordDays = 0;                     // 已工作天数


        Set<Long> userIntoSchoolIds = new HashSet<>();
        Set<Long> userVisitTeacherIds = new HashSet<>();
        Set<Long> userVisitChiTeacherIds = new HashSet<>();
        Set<Long> userVisitMathTeacherIds = new HashSet<>();
        Set<Long> userVisitEngTeacherIds = new HashSet<>();
        Set<Long> userVisitResearcherIds = new HashSet<>();
        Set<String> userMeetings = new HashSet<>();

        if(CollectionUtils.isNotEmpty(workRecordList)){
            List<WorkRecordData> visitRecordList = new ArrayList<>();        //陪访
            List<WorkRecordData> intoSchoolRecordList = new ArrayList<>();   //进校
            List<WorkRecordData> meetingRecordList = new ArrayList<>();      //组会&参与组会
            List<WorkRecordData> teachingRecordList = new ArrayList<>();     //拜访教研员
            workRecordList.forEach(item -> {
                if (item.getWorkType() == AgentWorkRecordType.ACCOMPANY){
                    visitRecordList.add(item);
                }else if (item.getWorkType() == AgentWorkRecordType.SCHOOL){
                    intoSchoolRecordList.add(item);
                }else if (item.getWorkType() == AgentWorkRecordType.MEETING){
                    meetingRecordList.add(item);
                }else if (item.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION){
                    teachingRecordList.add(item);
                }
            });
            //陪访
            userAccompanyVisitNum = visitRecordList.size();
            userVisitWorkload = calWorkload(visitRecordList);
            //进校
            userIntoSchoolWorkload = calWorkload(intoSchoolRecordList);

            if (CollectionUtils.isNotEmpty(intoSchoolRecordList)){
                for (WorkRecordData crmWorkRecord : intoSchoolRecordList){
                    userIntoSchoolIds.add(crmWorkRecord.getSchoolId());
                    //进校见师
                    workRecordService.generateVisitTeacherInfo(crmWorkRecord.getVisitUserInfoList(),userVisitEngTeacherIds,userVisitMathTeacherIds,userVisitChiTeacherIds,userVisitTeacherIds);
                }
            }

            //组会
            userMeetingWorkload = calWorkload(meetingRecordList);
            meetingRecordList.forEach(item -> {
                userMeetings.add(item.getWorkTitle());
            });
            //资源拓维，仅统计拜访的KP数量
            userTeachingWorkload = calWorkload(teachingRecordList);
            for (WorkRecordData crmWorkRecord : teachingRecordList) {
                List<WorkRecordVisitUserInfo> visitedResearcherList = crmWorkRecord.getVisitUserInfoList();
                if (CollectionUtils.isNotEmpty(visitedResearcherList)){
                    visitedResearcherList = visitedResearcherList.stream().filter(p -> !Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)).collect(Collectors.toList());
                    visitedResearcherList.forEach(item -> {
                        userVisitResearcherIds.add(item.getId());
                    });
                }
            }

            // 计算在工作日工作的天数
            Set<Integer> recordDayList = workRecordList.stream().map(w -> SafeConverter.toInt(DateUtils.dateToString(w.getWorkTime(), "yyyyMMdd"))).collect(Collectors.toSet());
            recordDays = workDayList.stream().filter(recordDayList::contains).collect(Collectors.toList()).size();

            // 计算总工作量
            userWorkload = calWorkload(workRecordList);
        }

        statisticsData.setUserIntoSchoolWorkload(userIntoSchoolWorkload);
        statisticsData.setUserVisitWorkload(userVisitWorkload);
        statisticsData.setUserMeetingWorkload(userMeetingWorkload);
        statisticsData.setUserTeachingWorkload(userTeachingWorkload);
        statisticsData.setUserWorkload(userWorkload);
        statisticsData.setUserWorkDays(recordDays);
        statisticsData.setUserNeedWordDays(workDayList.size());

        statisticsData.setUserIntoSchoolNum(userIntoSchoolIds.size());             //进校数
        statisticsData.setUserVisitTeacherNum(userVisitTeacherIds.size());         //见师量
        statisticsData.setUserVisitEngTeacherNum(userVisitEngTeacherIds.size());   //见师量（英语）
        statisticsData.setUserVisitMathTeacherNum(userVisitMathTeacherIds.size()); //见师量（数学）
        statisticsData.setUserVisitChiTeacherNum(userVisitChiTeacherIds.size());   //见师量（语文）
        statisticsData.setUserVisitResearcherNum(userVisitResearcherIds.size());   //拜访教研员数量
        statisticsData.setUserMeetingNum(userMeetings.size());                     //组会数
        statisticsData.setUserAccompanyVisitNum(userAccompanyVisitNum);            //陪访数

        return statisticsData;
    }

    // 计算自然天
    private int calNaturalDays(Date startDate, Date endDate){

        int days;
        Date now = new Date();
        // 当前日期在指定日期之内
        if(DayUtils.judgeDateRange(startDate, endDate, now.getTime())){
            days = ((Long)DateUtils.dayDiff(startDate, now)).intValue();
        }else {
            days = ((Long)DateUtils.dayDiff(startDate, endDate)).intValue();
        }
        return days;
    }

    // 计算工作日天数
    private int calWorkDays(Date startDate, Date endDate){
        return getWorkDayList(startDate, endDate).size();
    }

    private List<Integer> getWorkDayList(Date startDate, Date endDate){
        int start = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        int end = SafeConverter.toInt(DateUtils.dateToString(endDate, "yyyyMMdd"));
        Date now = new Date();
        // 当前日期在指定日期之内
        if(DayUtils.judgeDateRange(startDate, endDate, now.getTime())){
            end = SafeConverter.toInt(DateUtils.dateToString(now, "yyyyMMdd"));
        }
        return DayUtils.getWorkdayList(start, end);
    }

    private double calPerCapitaWorkload(List<Long> userIdList, Map<Long, List<WorkRecordData>> userRecordMap, int days){
        if(CollectionUtils.isEmpty(userIdList)){
            return 0d;
        }
        int userCount = 0;
        double totalT = 0d;
        if(days < 1){
            days = 1;
        }
        List<WorkRecordData> workRecordList = new ArrayList<>();
        for(Long p : userIdList) {
            List<WorkRecordData> recordList = userRecordMap.get(p);
            if(CollectionUtils.isEmpty(recordList)){
                continue;
            }
            workRecordList.addAll(recordList);
            userCount++;
        }
        totalT = calWorkload(workRecordList);
        return MathUtils.doubleDivide(totalT, userCount, 1);
    }

    /**
     * 平均陪同工作量
     * @param userIdList
     * @param userRecordMap
     * @return
     */
    private double calPerCapitaAccompanyVisitWorkload(List<Long> userIdList, Map<Long, List<WorkRecordData>> userRecordMap){
        if(CollectionUtils.isEmpty(userIdList)){
            return 0d;
        }
        int userCount = 0;
        double totalT = 0d;

        List<WorkRecordData> workRecordList = new ArrayList<>();
        for(Long userId : userIdList) {
            List<WorkRecordData> recordList = userRecordMap.get(userId);
            if(CollectionUtils.isEmpty(recordList)){
                continue;
            }
            workRecordList.addAll(recordList.stream().filter(p -> p.getWorkType() == AgentWorkRecordType.ACCOMPANY).collect(Collectors.toList()));
            userCount++;
        }
        totalT = calWorkload(workRecordList);
        return MathUtils.doubleDivide(totalT, userCount, 1);
    }

    /**
     * 工作量T计算方法
     *
     *                                        工作量T计算规则
     *	类别                                                                  	专员       市经理       区域经理      	    大区经理
     *	单科进校（除跨科外进校外的进校）                                      	0.5	        0.5	            0.5            	   0.5
     *	跨科进校（勾选了大于等于2个科目的老师，科目包含：语数英政史地理化生信）	 1	        1	            1	                 1
     *	陪访单科进校	                                                         /	        0.5	            0.5	              0.5
     *	陪访跨科进校	                                                         /      	1	            1	                1
     *	组织校级组会（≥6位老师）	                                             2  	    2	            2	                2
     *	组织省市区级组会（≥30位老师）											 5			5				5					5
     *	参与或陪访校级组会（≥6位老师）											 1			1				1					1
     *	参与或陪访省市区级组会（≥30位老师）                           			 2			2				2					2
     *	拜访教研员																 2			2				2					2
     * @param workRecordList
     * @return
     */
    public double calWorkload(List<WorkRecordData> workRecordList){
        if(CollectionUtils.isEmpty(workRecordList)){
            return 0d;
        }
        double result = 0d;

        List<String> oldRecordIds = new ArrayList<>();                          //旧数据
        Map<AgentWorkRecordType,List<String>> newRecordMap = new HashMap<>();   //新数据
        //分界时间
        Date demarcationDate = DateUtils.stringToDate(WorkRecordData.demarcationDate, DateUtils.FORMAT_SQL_DATETIME);
        workRecordList.forEach(item -> {
            if (item.getWorkTime().before(demarcationDate)){
                oldRecordIds.add(item.getId());
            }else {
                List<String> recordIds = newRecordMap.get(item.getWorkType());
                if (CollectionUtils.isEmpty(recordIds)){
                    recordIds = new ArrayList<>();
                }
                recordIds.add(item.getId());
                newRecordMap.put(item.getWorkType(),recordIds);
            }
        });

        List<AgentRecordWorkload> recordWorkloadList = new ArrayList<>();
        //获取旧工作量数据
        Map<String, AgentRecordWorkload> recordWorkloadMap = agentRecordWorkloadDao.loads(oldRecordIds);
        if(MapUtils.isNotEmpty(recordWorkloadMap)){
            recordWorkloadList.addAll(new ArrayList<>(recordWorkloadMap.values()));
        }
        //获取新工作量数据
        newRecordMap.forEach((k,v) -> {
            Map<String, AgentRecordWorkload> recordWorkLoadMap = agentRecordWorkloadDao.loadByWorkRecordIdsAndType(v,k);
            if (MapUtils.isNotEmpty(recordWorkLoadMap)){
                recordWorkloadList.addAll(new ArrayList<>(recordWorkLoadMap.values()));
            }
        });
        for (AgentRecordWorkload workload : recordWorkloadList){
            result = MathUtils.doubleAdd(result, SafeConverter.toDouble(workload.getWorkload()));
        }

        return result;
    }


    /**
     * 从工作记录中获取学科和老师的映射关系
     * @return
     */
    public Map<Subject,List<Long>> getSubjectTeacherMap(CrmWorkRecord crmWorkRecord){
        Map<Subject,List<Long>> result = new HashMap<>();
        if (null != crmWorkRecord){
            List<CrmTeacherVisitInfo> visitTeacherList = crmWorkRecord.getVisitTeacherList();
            result.putAll(generateSubjectTeacherMap(visitTeacherList));
        }
        return result;
    }

    /**
     * 从进校拜访老师中获取学科和老师的映射关系
     * @param visitTeacherList
     * @return
     */
    public Map<Subject,List<Long>> generateSubjectTeacherMap(List<CrmTeacherVisitInfo> visitTeacherList){
        Map<Subject,List<Long>> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(visitTeacherList)){
            Set<Long> teacherIds = visitTeacherList.stream().filter(CrmTeacherVisitInfo::isRealTeacher).map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toSet());
            result.putAll(generateSubjectTeacherMapByTeacherIds(teacherIds));
        }
        return result;
    }

    public Map<Subject,List<Long>> generateSubjectTeacherMapByTeacherIds(Collection<Long> teacherIds){
        Map<Subject,List<Long>> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(teacherIds)){
            Map<Long, CrmTeacherSummary> teacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(teacherIds);
            teacherSummaryMap.forEach((k,v) -> {
                Subject subject = Subject.ofWithUnknown(v.getSubject());
                if (!result.containsKey(subject)){
                    result.put(subject,new ArrayList<>());
                }
                List<Long> tempIds = result.get(subject);
                tempIds.add(v.getTeacherId());
                result.put(subject,tempIds);
            });
            teacherIds.removeAll(teacherSummaryMap.keySet());
            if (CollectionUtils.isNotEmpty(teacherIds)){
                Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
                teacherMap.forEach((k,v) -> {
                    Subject subject = v.getSubject();
                    if (null == subject){
                        subject = Subject.UNKNOWN;
                    }
                    if (!result.containsKey(subject)){
                        result.put(subject,new ArrayList<>());
                    }
                    List<Long> tempIds = result.get(subject);
                    result.put(subject,tempIds);
                    tempIds.add(v.getId());
                });
            }
        }
        return result;
    }

    /**
     * 获取Group统计信息
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    public Map<Long,AgentWorkRecordStatistics> getGroupStatistics(Collection<Long> groupIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        if (dateType == 1 && isCurrentDate(date, dateType)){
            return getGroupRealTimeStatisticsByIds(groupIds, date, dateType);
        }else {
            return getGroupHistoryStatistics(groupIds, date, dateType);
        }
    }

    /**
     * 获取用户统计
     * @param userIds
     * @param date
     * @param dateType
     * @return
     */
    public Map<Long,AgentWorkRecordStatistics> getUserStatistics(Collection<Long> userIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(userIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        if (dateType == 1 && isCurrentDate(date, dateType)){
            return getUserRealTimeStatistics(userIds, date, dateType);
        }else {
            return getUserHistoryStatistics(userIds, date, dateType);
        }
    }


    /**
     * 获取Group实时统计信息
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    private Map<Long,AgentWorkRecordStatistics> getGroupRealTimeStatisticsByIds(Collection<Long> groupIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }

        List<AgentGroup> agentGroupList = baseOrgService.getGroupByIds(groupIds);
        if (CollectionUtils.isEmpty(agentGroupList)){
            return new HashMap<>();
        }
        return getGroupRealTimeStatistics(agentGroupList, date, dateType);
    }

    private Map<Long, AgentWorkRecordStatistics> getGroupRealTimeStatistics(Collection<AgentGroup> groups, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groups) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Map<Long,AgentWorkRecordStatistics> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(groups)){
            List<Future<AgentWorkRecordStatistics>> futureList = new ArrayList<>();
            groups.forEach(item -> {
                Future<AgentWorkRecordStatistics> futureData = AlpsThreadPool.getInstance().submit(() -> generateStatisticsDataForGroup(item, date, dateType));
                futureList.add(futureData);

            });
            for(Future<AgentWorkRecordStatistics> futureData : futureList){
                try {
                    AgentWorkRecordStatistics data = futureData.get();
                    if(data != null && data.getGroupId() != null){
                        result.putIfAbsent(data.getGroupId(), data);
                    }
                }catch (Exception e){

                }
            }
        }
        return result;

    }

    /**
     * 获取用户实时统计
     * @param userIds
     * @param date
     * @param dateType
     * @return
     */
    private Map<Long, AgentWorkRecordStatistics> getUserRealTimeStatistics(Collection<Long> userIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(userIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        List<AgentWorkRecordStatistics> agentWorkRecordStatistics = generateStatisticsDataForUser(userIds, date, dateType);
        if (CollectionUtils.isNotEmpty(agentWorkRecordStatistics)){
            return agentWorkRecordStatistics.stream().collect(Collectors.toMap(AgentWorkRecordStatistics::getUserId, Function.identity()));
        }
        return new HashMap<>();
    }

    /**
     * 获取group历史统计
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    private Map<Long,AgentWorkRecordStatistics> getGroupHistoryStatistics(Collection<Long> groupIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Date startDate = getStartDate(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        return agentWorkRecordStatisticsDao.getGroupWorkRecordStatistics(groupIds, day, dateType);
    }

    /**
     * 获取用户历史统计
     * @param userIds
     * @param date
     * @param dateType
     * @return
     */
    private Map<Long,AgentWorkRecordStatistics> getUserHistoryStatistics(Collection<Long> userIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(userIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Date startDate = getStartDate(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        return agentWorkRecordStatisticsDao.getUserWorkRecordStatistics(userIds, day, dateType);
    }

    public Map<Long,AgentWorkRecordStatistics> getUserHistoryStatisticsByGroupId(Long groupId, Date date, Integer dateType){
        if (groupId == null || null == date || dateType == null) {
            return new HashMap<>();
        }
        Date startDate = getStartDate(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        return agentWorkRecordStatisticsDao.getUserWorkRecordStatisticsByGroupId(groupId, day, dateType);
    }


    /**
     * 获取Group进校统计信息
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    public Map<Long,AgentWorkRecordStatistics> getGroupIntoSchoolStatistics(Collection<Long> groupIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }

        if (dateType == 1 && isCurrentDate(date, dateType)){
            return getGroupRealTimeIntoSchoolStatisticsByIds(groupIds, date, dateType);
        }else {
            return getGroupHistoryStatistics(groupIds, date, dateType);
        }
    }


    /**
     * 获取Group实时进校统计信息
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    private Map<Long,AgentWorkRecordStatistics> getGroupRealTimeIntoSchoolStatisticsByIds(Collection<Long> groupIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }

        List<AgentGroup> agentGroupList = baseOrgService.getGroupByIds(groupIds);
        if (CollectionUtils.isEmpty(agentGroupList)){
            return new HashMap<>();
        }
        return getGroupRealTimeIntoSchoolStatistics(agentGroupList, date, dateType);
    }

    private Map<Long, AgentWorkRecordStatistics> getGroupRealTimeIntoSchoolStatistics(Collection<AgentGroup> groups, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groups) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Map<Long,AgentWorkRecordStatistics> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(groups)){
            List<Future<AgentWorkRecordStatistics>> futureList = new ArrayList<>();
            groups.forEach(item -> {
                    Future<AgentWorkRecordStatistics> futureData = AlpsThreadPool.getInstance().submit(() -> generateIntoSchoolStatisticsDataForGroup(item, date, dateType));
                    futureList.add(futureData);
            });
            for(Future<AgentWorkRecordStatistics> futureData : futureList){
                try {
                    AgentWorkRecordStatistics data = futureData.get();
                    if(data != null && data.getGroupId() != null){
                        result.putIfAbsent(data.getGroupId(), data);
                    }
                }catch (Exception e){

                }
            }
        }
        return result;

    }


    private AgentWorkRecordStatistics generateIntoSchoolStatisticsDataForGroup(AgentGroup group, Date date, Integer dateType){
        Date startDate = getStartDate(date, dateType);
        Date endDate = getEndDate(date, dateType);

        //获取该部门及子部门下所有的用户
        List<AgentGroupUser> groupUserList = baseOrgService.getAllGroupUsersByGroupId(group.getId());
        //过滤出专员ID
        Set<Long> userIds = groupUserList.stream().filter(p -> p.getUserRoleType() == AgentRoleType.BusinessDeveloper).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        AgentWorkRecordStatistics statisticsData = new AgentWorkRecordStatistics(SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd")), dateType, 1, group.getId(), group.getGroupName(), group.getRoleId());
        AgentGroup parentGroup = baseOrgService.getGroupById(group.getParentId());
        if(parentGroup != null){
            statisticsData.setParentGroupId(parentGroup.getId());
            statisticsData.setParentGroupName(parentGroup.getGroupName());
        }

        List<Long> allVisitTeacherIds = new ArrayList<>();
        List<Long> englishVisitTeacherIds = new ArrayList<>();
        List<Long> mathVisitTeacherIds = new ArrayList<>();
        List<Long> otherVisitTeacherIds = new ArrayList<>();

        //获取该批专员对应进校记录
        List<WorkRecordData> intoSchoolWorkRecordList = workRecordService.getWorkRecordDataListByUserTypeTime(userIds, AgentWorkRecordType.SCHOOL, startDate, endDate);
        Set<Long> userIdList = intoSchoolWorkRecordList.stream().map(WorkRecordData::getUserId).collect(Collectors.toSet());

        intoSchoolWorkRecordList.forEach(item -> {
            List<WorkRecordVisitUserInfo> visitUserInfoList = item.getVisitUserInfoList();
            if (CollectionUtils.isNotEmpty(visitUserInfoList)){
                List<WorkRecordVisitUserInfo> visitTeacherInfoList = visitUserInfoList.stream().filter(p -> Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)).collect(Collectors.toList());
                generateVisitTeacherInfo(visitTeacherInfoList,allVisitTeacherIds,englishVisitTeacherIds,mathVisitTeacherIds,otherVisitTeacherIds);
            }
        });

        Integer userNum = userIdList.size();

        statisticsData.setPerPersonIntoSchoolCount(MathUtils.doubleDivide(intoSchoolWorkRecordList.size(),userNum));    //人均进校数
        statisticsData.setPerPersonVisitTeacherCount(MathUtils.doubleDivide(allVisitTeacherIds.size(),userNum));       //人均见师量
        statisticsData.setPerPersonVisitMathTeacherCount(MathUtils.doubleDivide(mathVisitTeacherIds.size(),userNum));  //人均见师-数学
        statisticsData.setPerPersonVisitEngTeacherCount(MathUtils.doubleDivide(englishVisitTeacherIds.size(),userNum));//人均见师-英语
        statisticsData.setPerPersonVisitOtherTeacherCount(MathUtils.doubleDivide(otherVisitTeacherIds.size(),userNum));//人均见师-其他

        return statisticsData;
    }


    /**
     * 获取用户进校统计信息
     * @param userIds
     * @param date
     * @param dateType
     * @return
     */
    public Map<Long,AgentWorkRecordStatistics> getUserIntoSchoolStatistics(Collection<Long> userIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(userIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        if (dateType == 1 && isCurrentDate(date, dateType)){
            return getUserRealTimeIntoSchoolStatistics(userIds, date, dateType);
        }else {
            return getUserHistoryStatistics(userIds, date, dateType);
        }
    }

    private Map<Long, AgentWorkRecordStatistics> getUserRealTimeIntoSchoolStatistics(Collection<Long> userIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(userIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        List<AgentWorkRecordStatistics> agentWorkRecordStatistics = generateIntoSchoolStatisticsDataForUser(userIds, date, dateType);
        if (CollectionUtils.isNotEmpty(agentWorkRecordStatistics)){
            return agentWorkRecordStatistics.stream().collect(Collectors.toMap(AgentWorkRecordStatistics::getUserId, Function.identity()));
        }
        return new HashMap<>();
    }

    private List<AgentWorkRecordStatistics> generateIntoSchoolStatisticsDataForUser(Collection<Long> userIds, Date date, Integer dateType){
        List<AgentWorkRecordStatistics> dataList = new ArrayList<>();

        Date startDate = getStartDate(date, dateType);
        Date endDate = getEndDate(date, dateType);

//        List<CrmWorkRecord> intoSchoolWorkRecordList = crmWorkRecordLoaderClient.listByWorkersAndType(userIds,CrmWorkRecordType.SCHOOL, startDate, endDate);
        List<List<Long>> splitIds = new ArrayList<>();
        if(userIds.size() > 200){
            int count = userIds.size() / 200 + 1;
            splitIds.addAll(CollectionUtils.splitList(new ArrayList<>(userIds), count));
        }else {
            splitIds.add(new ArrayList<>((userIds)));
        }
        List<Future<List<WorkRecordData>>> futureWorkRecordList = new ArrayList<>();
        for(List<Long> itemList : splitIds){
            futureWorkRecordList.add(AlpsThreadPool.getInstance().submit(() -> workRecordService.getWorkRecordDataListByUserTypeTime(itemList,AgentWorkRecordType.SCHOOL, startDate, endDate)));
        }
        List<WorkRecordData> intoSchoolWorkRecordList = new ArrayList<>();
        for (Future<List<WorkRecordData>> future : futureWorkRecordList){
            try {
                List<WorkRecordData> workRecordList = future.get();
                if (CollectionUtils.isNotEmpty(workRecordList)){
                    intoSchoolWorkRecordList.addAll(workRecordList);
                }
            }catch (Exception e){
                String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                logger.error(methodName + " error", e);
                return dataList;
            }
        }
        Map<Long, List<WorkRecordData>> userWorkRecordMap = intoSchoolWorkRecordList.stream().collect(Collectors.groupingBy(WorkRecordData::getUserId, Collectors.toList()));

        Map<Long, AgentGroupUser> userGroupMap = new HashMap<>();
        Map<Long, List<AgentGroupUser>> userGroupListMap = agentGroupUserLoaderClient.findByUserIds(userIds);
        userGroupListMap.forEach((k, v) -> {
            if(CollectionUtils.isNotEmpty(v)){
                userGroupMap.put(k, v.get(0));
            }
        });
        Map<Long, AgentGroup> groupMap = new HashMap<>();
        Set<Long> userGroupIds = userGroupMap.values().stream().map(AgentGroupUser::getGroupId).collect(Collectors.toSet());
        Map<Long, AgentGroup> tem1 = agentGroupLoaderClient.loads(userGroupIds);
        if(MapUtils.isNotEmpty(tem1)){
            groupMap.putAll(tem1);
            Set<Long> parentGroupIds = tem1.values().stream().map(AgentGroup::getParentId).collect(Collectors.toSet());
            groupMap.putAll(agentGroupLoaderClient.loads(parentGroupIds));
        }

        Map<Long, AgentUser> userMap = agentUserLoaderClient.findByIds(userIds);

        List<Future<AgentWorkRecordStatistics>> futureList = new ArrayList<>();
        userIds.forEach(p -> {
            AgentUser user = userMap.get(p);

            AgentGroupUser groupUser = userGroupMap.get(p);

            Long groupId = groupUser.getGroupId();
            AgentGroup group = groupMap.get(groupId);

            AgentGroup parentGroup = groupMap.get(group.getParentId());

            List<WorkRecordData> workRecordList = userWorkRecordMap.get(p);

            Future<AgentWorkRecordStatistics> futureData = AlpsThreadPool.getInstance().submit(() -> generateUserIntoSchoolStatisticsData(user, groupUser.getUserRoleId(), group, parentGroup, workRecordList, startDate, dateType));
            futureList.add(futureData);
        });

        for(Future<AgentWorkRecordStatistics> futureData : futureList){
            try {
                AgentWorkRecordStatistics data = futureData.get();
                if (data != null) {
                    dataList.add(data);
                }
            }catch (Exception e){

            }
        }

        return dataList;
    }


    private AgentWorkRecordStatistics generateUserIntoSchoolStatisticsData(AgentUser user, Integer userRole, AgentGroup group, AgentGroup parentGroup, List<WorkRecordData> workRecordList, Date startDate, Integer dateType){
        if(user == null || group == null || startDate == null || dateType == null || userRole == null){
            return null;
        }

        AgentWorkRecordStatistics statisticsData = new AgentWorkRecordStatistics(SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd")), dateType, 2, group.getId(), group.getGroupName(), group.getRoleId());
        if(parentGroup != null){
            statisticsData.setParentGroupId(parentGroup.getId());
            statisticsData.setParentGroupName(parentGroup.getGroupName());
        }

        statisticsData.setUserId(user.getId());
        statisticsData.setUserName(user.getRealName());
        statisticsData.setUserRoleId(userRole);

        List<Long> allVisitTeacherIds = new ArrayList<>();
        List<Long> englishVisitTeacherIds = new ArrayList<>();
        List<Long> mathVisitTeacherIds = new ArrayList<>();
        List<Long> otherVisitTeacherIds = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(workRecordList)){
            workRecordList.forEach(item -> {
                List<WorkRecordVisitUserInfo> visitUserInfoList = item.getVisitUserInfoList();
                if (CollectionUtils.isNotEmpty(visitUserInfoList)){
                    List<WorkRecordVisitUserInfo> visitTeacherInfoList = visitUserInfoList.stream().filter(p -> Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)).collect(Collectors.toList());
                    generateVisitTeacherInfo(visitTeacherInfoList,allVisitTeacherIds,englishVisitTeacherIds,mathVisitTeacherIds,otherVisitTeacherIds);
                }
            });
        }

        statisticsData.setBdIntoSchoolCount(workRecordList != null ? workRecordList.size() : 0);                 //进校数
        statisticsData.setBdVisitTeacherCount(allVisitTeacherIds.size());          //见师量
        statisticsData.setBdVisitMathTeacherCount(mathVisitTeacherIds.size());     //数学老师
        statisticsData.setBdVisitEngTeacherCount(englishVisitTeacherIds.size());   //英语老师
        statisticsData.setBdVisitOtherTeacherCount(otherVisitTeacherIds.size());   //其他老师
        return statisticsData;
    }

    public void generateVisitTeacherInfo(List<WorkRecordVisitUserInfo> visitTeacherList,List<Long> allVisitTeacherIds,List<Long> englishVisitTeacherIds,List<Long> mathVisitTeacherIds,List<Long> otherVisitTeacherIds){
        visitTeacherList.forEach(visitTeacher -> {
            //英语
            if (visitTeacher.getSubject() == Subject.ENGLISH){
                englishVisitTeacherIds.add(visitTeacher.getId());
                //数学
            }else if (visitTeacher.getSubject() == Subject.MATH){
                mathVisitTeacherIds.add(visitTeacher.getId());
                //其他
            }else {
                otherVisitTeacherIds.add(visitTeacher.getId());
            }
            allVisitTeacherIds.add(visitTeacher.getId());
        });
    }


    /**
     * 团队专员进校数据转换
     * @param statisticsList
     * @param groupOrUser
     * @return
     */
    public List<Map<String,Object>> intoSchoolStatisticsConvertToMapList(List<AgentWorkRecordStatistics> statisticsList, Integer groupOrUser){
        List<Map<String,Object>> dataList = new ArrayList<>();
        if(CollectionUtils.isEmpty(statisticsList)){
            return dataList;
        }
        statisticsList.forEach(p -> {
            // 部门
            if(groupOrUser == 1){
                Map<String,Object> dataMap = new HashMap<>();
                dataMap.put("groupId",p.getGroupId());
                dataMap.put("groupName",p.getGroupName());
                dataMap.put("perPersonIntoSchoolCount",p.getPerPersonIntoSchoolCount() != null ? p.getPerPersonIntoSchoolCount() : 0);
                dataMap.put("perPersonVisitTeacherCount",p.getPerPersonVisitTeacherCount() != null ? p.getPerPersonVisitTeacherCount() : 0);
                dataMap.put("perPersonVisitMathTeacherCount",p.getPerPersonVisitMathTeacherCount() != null ? p.getPerPersonVisitMathTeacherCount() : 0);
                dataMap.put("perPersonVisitEngTeacherCount",p.getPerPersonVisitEngTeacherCount() != null ? p.getPerPersonVisitEngTeacherCount() : 0);
                dataMap.put("perPersonVisitOtherTeacherCount",p.getPerPersonVisitOtherTeacherCount() != null ? p.getPerPersonVisitOtherTeacherCount() : 0);
                if(p.getGroupId() == null){
                    dataMap.put("clickable",false);
                }else {
                    dataMap.put("clickable",true);
                }
                dataList.add(dataMap);
                // 专员
            }else if(groupOrUser == 2){
                Map<String,Object> dataMap = new HashMap<>();
                dataMap.put("userId",p.getUserId());
                dataMap.put("userName",p.getUserName());
                dataMap.put("bdIntoSchoolCount",p.getBdIntoSchoolCount() != null ? p.getBdIntoSchoolCount() : 0);
                dataMap.put("bdVisitTeacherCount",p.getBdVisitTeacherCount() != null ? p.getBdVisitTeacherCount() : 0);
                dataMap.put("bdVisitMathTeacherCount",p.getBdVisitMathTeacherCount() != null ? p.getBdVisitMathTeacherCount() : 0);
                dataMap.put("bdVisitEngTeacherCount",p.getBdVisitEngTeacherCount() != null ? p.getBdVisitEngTeacherCount() : 0);
                dataMap.put("bdVisitOtherTeacherCount",p.getBdVisitOtherTeacherCount() != null ? p.getBdVisitOtherTeacherCount() : 0);
                dataList.add(dataMap);
            }
        });
        return dataList;
    }


    /**
     * 检验参数组合
     * @param groupRoleType
     * @param dimension
     * @return
     */
    public boolean judgeGroupDimension(AgentGroupRoleType groupRoleType, Integer dimension){
        boolean result = false;
        //全国或业务部的情况
        if (groupRoleType == AgentGroupRoleType.Country || groupRoleType == AgentGroupRoleType.Marketing){
            if(dimension == 1 || dimension == 2 || dimension == 3 || dimension == 4 || dimension == 5){
                result = true;
            }
            // 大区的情况
        } else if(groupRoleType == AgentGroupRoleType.Region){
            if(dimension == 1 || dimension == 3 || dimension == 4 || dimension == 5){
                result = true;
            }
            // 区域的情况
        }else if(groupRoleType == AgentGroupRoleType.Area){
            if(dimension == 1 || dimension == 4 || dimension == 5){
                result = true;
            }
            //分区的情况
        }else if(groupRoleType == AgentGroupRoleType.City){
            if(dimension == 1 || dimension == 5){
                result = true;
            }
        }
        return result;
    }

    /**
     * 从工作记录中获取学科和老师的映射关系NEW
     * @param workRecordData
     * @return
     */
    public Map<Subject,List<Long>> getSubjectTeacherMapNew(WorkRecordData workRecordData) {
        Map<Subject, List<Long>> resultMap = new HashMap<>();
        if (workRecordData != null) {
            List<WorkRecordVisitUserInfo> visitUserInfoList = workRecordData.getVisitUserInfoList();
            if (CollectionUtils.isNotEmpty(visitUserInfoList)){
                List<WorkRecordVisitUserInfo> visitTeacherList = visitUserInfoList.stream().filter(p -> p != null && p.isRealTeacher()).collect(Collectors.toList());
                resultMap.putAll(generateSubjectTeacherMapNew(visitTeacherList));
            }
        }
        return resultMap;
    }
    /**
     * 从进校拜访老师中获取学科和老师的映射关系
     * @param visitTeacherList
     * @return
     */
    public Map<Subject,List<Long>> generateSubjectTeacherMapNew(List<WorkRecordVisitUserInfo> visitTeacherList){
        Map<Subject,List<Long>> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(visitTeacherList)){
            Set<Long> teacherIds = visitTeacherList.stream().map(WorkRecordVisitUserInfo::getId).collect(Collectors.toSet());
            result.putAll(generateSubjectTeacherMapByTeacherIds(teacherIds));
        }
        return result;
    }


}
