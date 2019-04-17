package com.voxlearning.utopia.agent.service.daily;


import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.SchoolBasicInfo;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOnlineIndicator;
import com.voxlearning.utopia.agent.bean.resource.HomeWorkInfo;
import com.voxlearning.utopia.agent.bean.workrecord.WorkRecordData;
import com.voxlearning.utopia.agent.constants.AgentDailyScoreIndex;
import com.voxlearning.utopia.agent.constants.AgentDailyScoreSubIndex;
import com.voxlearning.utopia.agent.dao.mongo.AgentWorkRecordStatisticsDao;
import com.voxlearning.utopia.agent.dao.mongo.daily.AgentDailyDao;
import com.voxlearning.utopia.agent.dao.mongo.daily.AgentDailyPlanDao;
import com.voxlearning.utopia.agent.dao.mongo.daily.AgentDailyScoreDao;
import com.voxlearning.utopia.agent.dao.mongo.parent.AgentNewRegisterParentDao;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchers;
import com.voxlearning.utopia.agent.persist.entity.AgentWorkRecordStatistics;
import com.voxlearning.utopia.agent.persist.entity.daily.*;
import com.voxlearning.utopia.agent.persist.entity.parent.AgentSchoolNewRegisterParent;
import com.voxlearning.utopia.agent.service.activity.AgentActivityService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentWorkRecordStatisticsService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.WorkRecordDataCompatibilityService;
import com.voxlearning.utopia.agent.service.organization.AgentOuterResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.support.AgentSchoolSupport;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.utils.Pinyin4jUtils;
import com.voxlearning.utopia.agent.view.daily.DailyScoreView;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.mapper.SchoolEsInfo;
import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.WorkRecordVisitUserInfo;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 日报service
 *
 * @author deliang.che
 * @since 2018/9/19
 */
@Named
public class AgentDailyService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AgentDailyPlanDao agentDailyPlanDao;
    @Inject
    private WorkRecordService workRecordService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private SearchService searchService;
    @Inject
    private AgentDailyDao agentDailyDao;
    @Inject
    private AgentWorkRecordStatisticsService agentWorkRecordStatisticsService;
    @Inject
    private AgentWorkRecordStatisticsDao agentWorkRecordStatisticsDao;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private AgentResourceService resourceService;
    @Inject
    private AgentDailyScoreDao agentDailyScoreDao;
    @Inject
    private AgentOuterResourceService agentOuterResourceService;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private PerformanceService performanceService;
    @Inject
    private LoadNewSchoolServiceClient loadNewSchoolServiceClient;
    @Inject
    private SchoolResourceService schoolResourceService;
    @Inject
    private WorkRecordDataCompatibilityService workRecordDataCompatibilityService;
    @Inject
    private AgentActivityService agentActivityService;
    @Inject
    private AgentNewRegisterParentDao newRegisterParentDao;
    @Inject
    private AgentSchoolSupport agentSchoolSupport;

    public static final Integer END_SUBMIT_TIME = 900;      //日报截止提交时间
    public static final Integer START_SUBMIT_TIME = 1800;   //日报开始提交时间
    public static final Integer BE_LATE_SUBMIT_TIME = 2100; //日报迟交时间

    /**
     * 获取日报日期
     *
     * @return
     */
    public Date getDailyDate() {
        Date date = null;
        Integer nowDateInt = SafeConverter.toInt(DateUtils.dateToString(new Date(), "HHmm"));
        //下午18:00之后，返回当天日期
        if (nowDateInt > START_SUBMIT_TIME) {
            date = new Date();
            //早晨09:00之前，返回昨天日期
        } else if (nowDateInt < END_SUBMIT_TIME) {
            date = DateUtils.addDays(new Date(), -1);
        }
        return date;
    }

    /**
     * 创建日报之前
     *
     * @param userId
     * @return
     */
    public MapMessage beforeAddDaily(Long userId) {
        //获取日报日期
        Date dailyDate = getDailyDate();
        if (dailyDate == null) {
            dailyDate = new Date();
        }

        Date startDate = DayRange.newInstance(dailyDate.getTime()).getStartDate();
        Date endDate = DayRange.newInstance(dailyDate.getTime()).getEndDate();

        //获取当天的工作记录
        List<WorkRecordData> workRecordList = workRecordService.getWorkRecordDataListByUserTypeTime(Collections.singleton(userId), null, startDate, endDate);

        // 当天工作记录与前一天日报计划对比信息
        Map<String, Object> compareResultMap = compareWithWorkRecordAndDailyPlan(userId, workRecordList, dailyDate);

        List<WorkRecordVisitUserInfo> visitTeaList = new ArrayList<>();
        List<WorkRecordVisitUserInfo> visitEngTeaList = new ArrayList<>();
        List<WorkRecordVisitUserInfo> visitMathTeaList = new ArrayList<>();
        List<WorkRecordVisitUserInfo> visitOtherTeaList = new ArrayList<>();

        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        Set<Long> intoSchoolWorkUserIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        List<WorkRecordData> intoSchoolWorkRecordList = new ArrayList<>();
        //组装数据
        generateIntoSchoolData(userRole, userIds, intoSchoolWorkUserIds, userId, intoSchoolWorkRecordList, startDate, endDate);
        //拜访老师情况
        visitTeacherInfo(intoSchoolWorkRecordList, visitEngTeaList, visitMathTeaList, visitOtherTeaList, visitTeaList);

        //拜访后布置作业的老师
        Set<Long> homeWorkTeacherIds = new HashSet<>();
        Set<Long> homeWorkEngTeacherIds = new HashSet<>();
        Set<Long> homeWorkMathTeacherIds = new HashSet<>();
        Set<Long> homeWorkOtherTeacherIds = new HashSet<>();


        //获取布置作业信息
        Map<String, Integer> teacherHomeWorkInfoMap = getTeacherHomeWorkInfo(userRole, visitTeaList, visitEngTeaList, visitMathTeaList, visitOtherTeaList,
                homeWorkTeacherIds, homeWorkEngTeacherIds, homeWorkMathTeacherIds, homeWorkOtherTeacherIds,
                dailyDate, intoSchoolWorkUserIds);

        Integer homeWorkUserNum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkUserNum"));
        Integer homeWorkTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkTeacherSum"));
        Integer homeWorkEnglishTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkEnglishTeacherSum"));
        Integer homeWorkMathTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkMathTeacherSum"));
        Integer homeWorkOtherTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkOtherTeacherSum"));


        String otherWorkPlan = "";
        //昨天日报时间
        Integer yesterdayDailyDate = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(dailyDate, -1), "yyyyMMdd"));
        //获取昨日日报计划
        AgentDailyPlan dailyPlan = agentDailyPlanDao.loadByUserIdAndTime(userId, yesterdayDailyDate);
        if (dailyPlan != null) {
            otherWorkPlan = dailyPlan.getOtherWork();
        }

        //日报时间
        String dailyDateStr = DateUtils.dateToString(dailyDate, "yyyy.MM.dd");

        Map<String, Object> userMap = userInfo(userId, startDate, endDate);

        MapMessage mapMessage = MapMessage.successMessage();

        Map<String, Object> dataMap = new HashMap<>();

        int userNum = intoSchoolWorkUserIds.size();
        if (userRole == AgentRoleType.BusinessDeveloper) {
            dataMap.put("allVisitTeacherCount", visitTeaList.size());
            dataMap.put("englishVisitTeacherCount", visitEngTeaList.size());
            dataMap.put("mathVisitTeacherCount", visitMathTeaList.size());
            dataMap.put("otherVisitTeacherCount", visitOtherTeaList.size());

            dataMap.put("homeWorkAllTeacherCount", homeWorkTeacherIds.size());
            dataMap.put("homeWorkEnglishTeacherCount", homeWorkEngTeacherIds.size());
            dataMap.put("homeWorkMathTeacherCount", homeWorkMathTeacherIds.size());
            dataMap.put("homeWorkOtherTeacherCount", homeWorkOtherTeacherIds.size());
        } else {
            dataMap.put("perPersonVisitTeacherCount", MathUtils.doubleDivide(visitTeaList.size(), userNum));       //人均见师量
            dataMap.put("perPersonVisitMathTeacherCount", MathUtils.doubleDivide(visitMathTeaList.size(), userNum));  //人均见师-数学
            dataMap.put("perPersonVisitEngTeacherCount", MathUtils.doubleDivide(visitEngTeaList.size(), userNum));//人均见师-英语
            dataMap.put("perPersonVisitOtherTeacherCount", MathUtils.doubleDivide(visitOtherTeaList.size(), userNum));//人均见师-其他

            dataMap.put("perPersonHomeWorkTeacherCount", MathUtils.doubleDivide(homeWorkTeacherSum, homeWorkUserNum));
            dataMap.put("perPersonHomeWorkEnglishTeacherCount", MathUtils.doubleDivide(homeWorkEnglishTeacherSum, homeWorkUserNum));
            dataMap.put("perPersonHomeWorkMathTeacherCount", MathUtils.doubleDivide(homeWorkMathTeacherSum, homeWorkUserNum));
            dataMap.put("perPersonHomeWorkOtherTeacherCount", MathUtils.doubleDivide(homeWorkOtherTeacherSum, homeWorkUserNum));
        }


        dataMap.put("userAvatar", userMap.get("userAvatar"));
        dataMap.put("userName", userMap.get("userName"));
        dataMap.put("groupName", userMap.get("groupName"));
        dataMap.put("workload", userMap.get("workload"));
        dataMap.put("dailyDate", dailyDateStr);
        dataMap.put("userRole", userRole);


        dataMap.put("otherWorkPlan", otherWorkPlan);

        mapMessage.add("intoSchoolMapList", compareResultMap.get("intoSchoolCompareList"));
        mapMessage.add("meetingMapList", compareResultMap.get("meetingCompareList"));
        mapMessage.add("outerResourceMapList", compareResultMap.get("outerResourceCompareList"));
        mapMessage.add("partnerMapList", compareResultMap.get("partnerCompareList"));
        mapMessage.add("dataMap", dataMap);
        return mapMessage;
    }

    /**
     * 获取老师布置作业情况
     *
     * @param userRole
     * @param visitTeaList
     * @param visitEngTeaList
     * @param visitMathTeaList
     * @param visitOtherTeaList
     * @param homeWorkTeacherIds
     * @param homeWorkEngTeacherIds
     * @param homeWorkMathTeacherIds
     * @param homeWorkOtherTeacherIds
     * @param dailyDate
     * @param userIdList
     * @return
     */
    public Map<String, Integer> getTeacherHomeWorkInfo(AgentRoleType userRole, List<WorkRecordVisitUserInfo> visitTeaList, List<WorkRecordVisitUserInfo> visitEngTeaList, List<WorkRecordVisitUserInfo> visitMathTeaList, List<WorkRecordVisitUserInfo> visitOtherTeaList,
                                                       Set<Long> homeWorkTeacherIds, Set<Long> homeWorkEngTeacherIds, Set<Long> homeWorkMathTeacherIds, Set<Long> homeWorkOtherTeacherIds,
                                                       Date dailyDate, Collection<Long> userIdList) {
        Integer homeWorkUserNum = 0;
        Integer homeWorkTeacherSum = 0;
        Integer homeWorkEnglishTeacherSum = 0;
        Integer homeWorkMathTeacherSum = 0;
        Integer homeWorkOtherTeacherSum = 0;

        //如果是专员，实时获取拜访老师布置作业情况
        if (userRole == AgentRoleType.BusinessDeveloper) {
            //老师与拜访时间map
//            Map<Long, Date> teacherVisitDateMap = visitTeaList.stream().collect(Collectors.toMap(CrmTeacherVisitInfo::getTeacherId, CrmTeacherVisitInfo::getVisitTime, (o1, o2) -> o1));

            Set<Long> teacherIds = visitTeaList.stream().map(WorkRecordVisitUserInfo::getId).collect(Collectors.toSet());

            //老师与班组之间关系
            Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, true);
            Map<Long, List<Long>> teacherGroupIdMap = new HashMap<>();
            teacherGroups.forEach((k, v) -> {
                teacherGroupIdMap.put(k, v.stream().map(GroupTeacherMapper::getId).collect(Collectors.toList()));
            });

            //老师班组与最近一次布置作业时间对应关系
            List<HomeWorkInfo> teacherHomeWorkList = getTeacherHomeWorkList(teacherIds, 2);
            Map<Long, List<HomeWorkInfo>> groupHomeWorkListMap = teacherHomeWorkList.stream().collect(Collectors.groupingBy(HomeWorkInfo::getGroupId));
//            Map<Long,Date> groupHomeWorkDateMap = new HashMap<>();
            Map<Long, HomeWorkInfo> groupHomeWorkMap = new HashMap<>();
            groupHomeWorkListMap.forEach((k, v) -> {
                groupHomeWorkMap.put(k, v.stream().sorted((o1, o2) -> o2.getAssignDate().compareTo(o1.getAssignDate())).findFirst().orElse(null));
            });

            //日报当天开始时间
            Date startDate = DayRange.newInstance(dailyDate.getTime()).getStartDate();
            teacherGroupIdMap.forEach((k, v) -> {
                //拜访老师时间
//                Date visitDate = teacherVisitDateMap.get(k);
                v.forEach(groupId -> {
                    //布置作业时间
                    HomeWorkInfo homeWorkInfo = groupHomeWorkMap.get(groupId);
                    if (homeWorkInfo != null) {
                        Date assignDate = homeWorkInfo.getAssignDate();
                        //如果是日报当天开始时间之后布置作业
                        if (assignDate != null && assignDate.after(startDate)) {
                            homeWorkTeacherIds.add(k);
                        }
                    }
                });
            });

            List<Long> visitEngTeaIds = visitEngTeaList.stream().map(WorkRecordVisitUserInfo::getId).collect(Collectors.toList());
            List<Long> visitMathTeaIds = visitMathTeaList.stream().map(WorkRecordVisitUserInfo::getId).collect(Collectors.toList());
            List<Long> visitOtherTeaIds = visitOtherTeaList.stream().map(WorkRecordVisitUserInfo::getId).collect(Collectors.toList());
            homeWorkTeacherIds.forEach(item -> {
                if (visitEngTeaIds.contains(item)) {
                    homeWorkEngTeacherIds.add(item);
                }
                if (visitMathTeaIds.contains(item)) {
                    homeWorkMathTeacherIds.add(item);
                }
                if (visitOtherTeaIds.contains(item)) {
                    homeWorkOtherTeacherIds.add(item);
                }
            });
            //市经理及以上，计算名下已经填写日报的专员的拜访老师布置作业平均数
        } else {
            Integer dailyDateInt = SafeConverter.toInt(DateUtils.dateToString(dailyDate, "yyyyMMdd"));
            Map<Long, AgentDaily> userDailyMap = agentDailyDao.loadByUserIdsAndTime(userIdList, dailyDateInt);
            if (MapUtils.isNotEmpty(userDailyMap)) {
                for (AgentDaily daily : userDailyMap.values()) {
                    homeWorkUserNum++;
                    homeWorkTeacherSum += daily.getAssignHomeWorkTeaNum() != null ? daily.getAssignHomeWorkTeaNum() : 0;
                    homeWorkEnglishTeacherSum += daily.getAssignHomeWorkEngTeaNum() != null ? daily.getAssignHomeWorkEngTeaNum() : 0;
                    homeWorkMathTeacherSum += daily.getAssignHomeWorkMathTeaNum() != null ? daily.getAssignHomeWorkMathTeaNum() : 0;
                    homeWorkOtherTeacherSum += daily.getAssignHomeWorkOtherTeaNum() != null ? daily.getAssignHomeWorkOtherTeaNum() : 0;
                }
            }
        }
        Map<String, Integer> dataMap = new HashMap<>();
        dataMap.put("homeWorkUserNum", homeWorkUserNum);
        dataMap.put("homeWorkTeacherSum", homeWorkTeacherSum);
        dataMap.put("homeWorkEnglishTeacherSum", homeWorkEnglishTeacherSum);
        dataMap.put("homeWorkMathTeacherSum", homeWorkMathTeacherSum);
        dataMap.put("homeWorkOtherTeacherSum", homeWorkOtherTeacherSum);
        return dataMap;
    }


    public List<HomeWorkInfo> getTeacherHomeWorkList(Collection<Long> teacherIds, Integer homeWorkDay) {
        List<Long> juniorSchoolMathTeacherIds = new ArrayList<>();
        List<Long> juniorSchoolEngTeacherIds = new ArrayList<>();
        List<Long> primarySchoolTeacherIds = new ArrayList<>();

        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        teacherMap.forEach((k, v) -> {
            if (v.getKtwelve() == Ktwelve.JUNIOR_SCHOOL) {
                if (v.getSubject() == Subject.MATH) {
                    juniorSchoolMathTeacherIds.add(k);
                } else {
                    juniorSchoolEngTeacherIds.add(k);
                }
            } else if (v.getKtwelve() == Ktwelve.PRIMARY_SCHOOL) {
                primarySchoolTeacherIds.add(k);
            }
        });
        List<HomeWorkInfo> homeWorkInfoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(juniorSchoolMathTeacherIds)) {
//            homeWorkInfoList.addAll(resourceService.juniorSchoolMathHomeWorkList(juniorSchoolEngTeacherIds,homeWorkDay));
            homeWorkInfoList.addAll(getTeacherHomeWorkLis(juniorSchoolMathTeacherIds));
        }
        if (CollectionUtils.isNotEmpty(juniorSchoolEngTeacherIds)) {
            homeWorkInfoList.addAll(resourceService.juniorSchoolEngHomeWorkList(juniorSchoolEngTeacherIds, homeWorkDay));
        }
        if (CollectionUtils.isNotEmpty(primarySchoolTeacherIds)) {
            homeWorkInfoList.addAll(resourceService.primarySchoolHomeWorkList(primarySchoolTeacherIds, homeWorkDay));
        }
        return homeWorkInfoList;
    }

    public List<HomeWorkInfo> getTeacherHomeWorkLis(Collection<Long> teacherIds) {
        List<HomeWorkInfo> dataList = new ArrayList<>();
        teacherIds.forEach(teacherId -> {
            MapMessage mapMessage = resourceService.getTeacherHwListByTeacherId(teacherId);
            if (mapMessage.isSuccess()) {
                dataList.addAll((List<HomeWorkInfo>) mapMessage.get("homeWorkList"));
            }
        });
        return dataList;
    }

    public Map<String, Object> userInfo(Long userId, Date startDate, Date endDate) {
        //部门名称
        String groupName = "";
        AgentGroup group = baseOrgService.getUserGroups(userId).stream().findFirst().orElse(null);
        if (group != null) {
            groupName = group.getGroupName();
        }

        String userAvatar = ""; //用户头像
        String userName = "";   //用户姓名
        AgentUser user = baseOrgService.getUser(userId);
        if (user != null) {
            userAvatar = user.getAvatar();
            userName = user.getRealName();
        }

        List<WorkRecordData> workRecordDataList = workRecordService.getWorkRecordDataListByUserTypeTime(Collections.singleton(userId), null, startDate, endDate);
        //工作量
        Double workload = agentWorkRecordStatisticsService.calWorkload(workRecordDataList);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("userAvatar", userAvatar);
        dataMap.put("userName", userName);
        dataMap.put("groupName", groupName);
        dataMap.put("workload", workload);
        return dataMap;
    }

    /**
     * 当天工作记录与前一天日报计划对比信息
     *
     * @param userId
     * @param workRecordList
     * @param dailyDate
     * @return
     */
    public Map<String, Object> compareWithWorkRecordAndDailyPlan(Long userId, List<WorkRecordData> workRecordList, Date dailyDate) {


        //获取昨天日报的工作计划
        Integer yesterdayDailyDate = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(dailyDate, -1), "yyyyMMdd"));
        AgentDailyPlan dailyPlan = agentDailyPlanDao.loadByUserIdAndTime(userId, yesterdayDailyDate);
        List<Long> intoSchoolList = new ArrayList<>();
        List<String> meetingList = new ArrayList<>();
        List<Long> partnerList = new ArrayList<>();
        List<Long> researcherList = new ArrayList<>();
        if (dailyPlan != null) {
            intoSchoolList.addAll(dailyPlan.getSchoolIdList());
            meetingList.addAll(dailyPlan.getMeetingNameList());
            researcherList.addAll(dailyPlan.getResearcherIdList());
            partnerList.addAll(dailyPlan.getPartnerIdList());
        }

        List<AgentDailyCompareInfo> intoSchoolCompareList = new ArrayList<>();
        List<AgentDailyCompareInfo> meetingCompareList = new ArrayList<>();
        List<AgentDailyCompareInfo> outerResourceCompareList = new ArrayList<>();
        List<AgentDailyCompareInfo> partnerCompareList = new ArrayList<>();

        //近30天进校记录（不包含当天）
        Date startTime = DayRange.newInstance(DateUtils.addDays(dailyDate, -30).getTime()).getStartDate();
        Date endTime = DayRange.newInstance(DateUtils.addDays(dailyDate, -1).getTime()).getEndDate();
        List<WorkRecordData> intoSchoolWorkRecordList = workRecordService.getWorkRecordDataListByUserTypeTime(Collections.singleton(userId), AgentWorkRecordType.SCHOOL, startTime, endTime);
        Map<Long, List<WorkRecordData>> schoolWorkRecordListMap = intoSchoolWorkRecordList.stream().filter(p -> p.getSchoolId() != null).collect(Collectors.groupingBy(WorkRecordData::getSchoolId));

        workRecordList.forEach(item -> {
            AgentDailyCompareInfo dailyCompareInfo = new AgentDailyCompareInfo();
            dailyCompareInfo.setWorkType(workRecordService.convertWorkRecordType(item.getWorkType()));
            dailyCompareInfo.setWorkRecordId(item.getId());
            //进校
            if (item.getWorkType() == AgentWorkRecordType.SCHOOL) {
                dailyCompareInfo.setContent(item.getSchoolName());
                if (CollectionUtils.isNotEmpty(intoSchoolList)) {
                    //计划内
                    if (intoSchoolList.contains(item.getSchoolId())) {
                        dailyCompareInfo.setStatus("plan");

                        intoSchoolList.remove(item.getSchoolId());
                        //未计划
                    } else {
                        dailyCompareInfo.setStatus("noPlan");
                    }
                    //未计划
                } else {
                    dailyCompareInfo.setStatus("noPlan");
                }
                //频繁拜访：拜访学校近30天拜访过2天及以上（不含今天这次拜访）
                dailyCompareInfo.setFrequentlyVisit(workRecordService.intoSchoolFrequentlyVisit(schoolWorkRecordListMap, item.getSchoolId()));

                intoSchoolCompareList.add(dailyCompareInfo);
            }
            //组会
            if (item.getWorkType() == AgentWorkRecordType.MEETING) {
                dailyCompareInfo.setContent(item.getWorkTitle());
                if (CollectionUtils.isNotEmpty(meetingList)) {
                    //计划内
                    if (meetingList.contains(item.getWorkTitle())) {
                        dailyCompareInfo.setStatus("plan");

                        meetingList.remove(item.getWorkTitle());
                        //未计划
                    } else {
                        dailyCompareInfo.setStatus("noPlan");
                    }
                    //未计划
                } else {
                    dailyCompareInfo.setStatus("noPlan");
                }
                meetingCompareList.add(dailyCompareInfo);
            }
            //资源拓维
            if (item.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION) {
                if (CollectionUtils.isNotEmpty(researcherList)) {
                    boolean isPlan = false;
                    List<WorkRecordVisitUserInfo> visitUserInfoList = item.getVisitUserInfoList();
                    if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
                        List<WorkRecordVisitUserInfo> visitKpInfoList = visitUserInfoList.stream().filter(p -> !Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)).collect(Collectors.toList());
                        for (WorkRecordVisitUserInfo visitKpInfo : visitKpInfoList) {
                            if (researcherList.contains(visitKpInfo.getId())) {
                                isPlan = true;

                                researcherList.remove(visitKpInfo.getId());
                            }
                        }
                    }
                    //计划内
                    if (isPlan) {
                        dailyCompareInfo.setStatus("plan");
                        //未计划
                    } else {
                        dailyCompareInfo.setStatus("noPlan");
                    }
                    //未计划
                } else {
                    dailyCompareInfo.setStatus("noPlan");
                }

                dailyCompareInfo.setContent(StringUtils.join(workRecordService.generateResourceExtensionTitleList(item), "、"));

                outerResourceCompareList.add(dailyCompareInfo);

            }
            //陪同
            if (item.getWorkType() == AgentWorkRecordType.ACCOMPANY) {
                WorkRecordData workRecord = workRecordService.getAccompanyWorkRecord(item);
                if (workRecord != null) {
                    if (CollectionUtils.isNotEmpty(partnerList)) {
                        //计划内
                        if (partnerList.contains(workRecord.getUserId())) {
                            dailyCompareInfo.setStatus("plan");

                            partnerList.remove(workRecord.getUserId());
                            //未计划
                        } else {
                            dailyCompareInfo.setStatus("noPlan");
                        }
                        //未计划
                    } else {
                        dailyCompareInfo.setStatus("noPlan");
                    }

                    //进校
                    if (workRecord.getWorkType() == AgentWorkRecordType.SCHOOL) {
                        //校级会议
                        if (null != workRecord.getVisitSchoolType() && workRecord.getVisitSchoolType() == 1) {
                            //学校名称 + “-校级会议”
                            dailyCompareInfo.setContent("进校-" + workRecord.getSchoolName() + "-校级会议");
                        } else {
                            //学校名称
                            dailyCompareInfo.setContent("进校-" + workRecord.getSchoolName());
                        }
                    }
                    //组会
                    if (workRecord.getWorkType() == AgentWorkRecordType.MEETING) {
                        //组会主题
                        dailyCompareInfo.setContent("组会-" + workRecord.getWorkTitle());
                    }
                    //资源拓维
                    if (workRecord.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION) {
                        //拜访教研员主题
                        dailyCompareInfo.setContent(StringUtils.join(workRecordService.generateResourceExtensionTitleList(workRecord), "、"));
                    }
                }
                partnerCompareList.add(dailyCompareInfo);
            }
        });
        Map<Long, CrmSchoolSummary> schoolSummaryMap = agentSchoolSupport.batchLoadCrmSchoolSummaryAndSchool(intoSchoolList);
        //进校未完成
        intoSchoolList.forEach(item -> {
            AgentDailyCompareInfo dailyCompareInfo = new AgentDailyCompareInfo();
            dailyCompareInfo.setWorkType(CrmWorkRecordType.SCHOOL);
            CrmSchoolSummary schoolSummary = schoolSummaryMap.get(item);
            if (schoolSummary != null) {
                dailyCompareInfo.setContent(schoolSummary.getSchoolName());
            }
            dailyCompareInfo.setStatus("unFinish");
            intoSchoolCompareList.add(dailyCompareInfo);
        });
        //组会未完成
        meetingList.forEach(item -> {
            AgentDailyCompareInfo dailyCompareInfo = new AgentDailyCompareInfo();
            dailyCompareInfo.setWorkType(CrmWorkRecordType.MEETING);
            dailyCompareInfo.setContent(item);
            dailyCompareInfo.setStatus("unFinish");
            meetingCompareList.add(dailyCompareInfo);
        });
        //教研员未完成
        if (CollectionUtils.isNotEmpty(researcherList)) {
            AgentDailyCompareInfo dailyCompareInfo = new AgentDailyCompareInfo();
            dailyCompareInfo.setWorkType(CrmWorkRecordType.TEACHING);
            dailyCompareInfo.setContent(StringUtils.join(workRecordService.generateResourceExtensionTitleByIds(researcherList), "、"));
            dailyCompareInfo.setStatus("unFinish");
            outerResourceCompareList.add(dailyCompareInfo);
        }

        //陪同未完成
        if (CollectionUtils.isNotEmpty(partnerList)) {
            AgentDailyCompareInfo dailyCompareInfo = new AgentDailyCompareInfo();
            dailyCompareInfo.setWorkType(CrmWorkRecordType.VISIT);
            dailyCompareInfo.setContent(StringUtils.join(baseOrgService.getUsers(partnerList).stream().map(AgentUser::getRealName).collect(Collectors.toList()), "、"));
            dailyCompareInfo.setStatus("unFinish");
            partnerCompareList.add(dailyCompareInfo);
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("intoSchoolCompareList", intoSchoolCompareList);
        dataMap.put("meetingCompareList", meetingCompareList);
        dataMap.put("outerResourceCompareList", outerResourceCompareList);
        dataMap.put("partnerCompareList", partnerCompareList);
        return dataMap;
    }

    /**
     * 创建日报进校选择学校
     *
     * @param userId
     * @param searchKey
     * @param scene
     * @param isDefault
     * @return
     */
    public List<SchoolBasicInfo> dailyIntoSchoolSearch(Long userId, String searchKey, Integer scene, Boolean isDefault) {
        List<SchoolBasicInfo> schoolBasicInfoList = new ArrayList<>();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        List<Long> schoolIdList = new ArrayList<>();
        if (!isDefault) {
            Page<SchoolEsInfo> esInfoPage = searchService.searchSchoolPageForScene(userId, searchKey, scene, null, null, 0, 100);
            schoolIdList = esInfoPage.getContent().stream().map(p -> SafeConverter.toLong(p.getId())).collect(Collectors.toList());
            //默认
        } else {
            //专员
            if (userRole == AgentRoleType.BusinessDeveloper) {
                schoolIdList = baseOrgService.getManagedSchoolList(userId);
            }
        }
        if (CollectionUtils.isEmpty(schoolIdList)) {
            return Collections.emptyList();
        }
        Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIdList);
        List<School> schoolList = schoolMap.values().stream()
                .filter(p -> (p.getSchoolAuthenticationState() == AuthenticationState.WAITING || p.getSchoolAuthenticationState() == AuthenticationState.SUCCESS) && p.getSchoolAuthenticationState() != AuthenticationState.FAILURE)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(schoolList)) {
            return Collections.emptyList();
        }
        schoolList.forEach(school -> {
            SchoolBasicInfo schoolInfo = new SchoolBasicInfo();
            schoolInfo.setSchoolId(school.getId());
            schoolInfo.setSchoolName(school.getCname());
            schoolBasicInfoList.add(schoolInfo);
        });
        return schoolBasicInfoList;
    }

    /**
     * 创建日报资源拓维选择上层资源
     *
     * @param userId
     * @param searchName
     * @param isDefault
     * @return
     */
    public Map<String, Object> dailyOuterResourceSearch(Long userId, String searchName, Boolean isDefault) {
        Map<String, Object> dataMap = new HashMap<>();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        if (!isDefault || userRole == AgentRoleType.BusinessDeveloper) {
            dataMap = agentOuterResourceService.getOuterResourceInfoForDaily(userId, searchName);
        }
        return dataMap;
    }


    /**
     * 创建日报选择陪同对象
     *
     * @param currentUser
     * @return
     */
    public List<Map<String, Object>> dailyPartnerSearch(AuthCurrentUser currentUser) {
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(currentUser.getUserId());
        if (CollectionUtils.isEmpty(groupUserList)) {
            return Collections.emptyList();
        }
        List<Long> userIds = new ArrayList<>();
        Long groupId = groupUserList.get(0).getGroupId();
        AgentRoleType userRole = baseOrgService.getUserRole(currentUser.getUserId());
        //专员
        if (userRole == AgentRoleType.BusinessDeveloper) {
            userIds.addAll(baseOrgService.getGroupUsersByRole(groupId, AgentRoleType.BusinessDeveloper));
        } else {
            //部门及其子部门下所有用户
            List<AgentGroupUser> managedGroupUserList = baseOrgService.getAllGroupUsersByGroupId(groupId);
            userIds.addAll(managedGroupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet()));
        }
        //移除自己的ID
        userIds.remove(currentUser.getUserId());

        List<AgentUser> userList = baseOrgService.getUsers(userIds);

        List<Map<String, Object>> dataList = new ArrayList<>();
        //根据陪同对象姓名首字母分组
        Map<String, List<AgentUser>> firstCapitalUserMap = userList.stream().collect(Collectors.groupingBy(p -> Pinyin4jUtils.getFirstCapital(p.getRealName())));
        //陪同对象姓名首字母排序
        List<String> sortedFirstCapital = firstCapitalUserMap.keySet().stream().sorted(Comparator.comparing(item -> item == null ? "" : item, Collator.getInstance(Locale.CHINA))).collect(Collectors.toList());
        sortedFirstCapital.forEach(item -> {
            Map<String, Object> dataMap = new HashMap<>();
            List<Map<String, Object>> partnerMapList = new ArrayList<>();
            List<AgentUser> partnerList = firstCapitalUserMap.get(item);
            if (CollectionUtils.isNotEmpty(partnerList)) {
                partnerList.forEach(partner -> {
                    Map<String, Object> partnerMap = new HashMap<>();
                    partnerMap.put("id", partner.getId());
                    partnerMap.put("workerName", partner.getRealName());
                    partnerMapList.add(partnerMap);
                });
            }
            dataMap.put("firstCapital", item);
            dataMap.put("partnerList", partnerMapList);
            dataList.add(dataMap);
        });
        return dataList;
    }

    /**
     * 创建or编辑日报
     *
     * @param userId
     * @param otherWorkResult
     * @param schoolIdStr
     * @param meetingNameStr
     * @param outerResourceIdStr
     * @param partnerIdStr
     * @param otherWork
     * @return
     */
    public MapMessage addOrEditDaily(String id, Long userId, String otherWorkResult, String schoolIdStr, String meetingNameStr, String outerResourceIdStr, String partnerIdStr, String otherWork) {
        Date dailyDate = null;
        AgentDaily daily = null;
        AgentDailyPlan dailyPlan = null;
        //编辑
        if (StringUtils.isNotBlank(id)) {
            daily = agentDailyDao.load(id);
            dailyPlan = agentDailyPlanDao.loadByDailyId(id);
            if (daily == null || daily.getDailyTime() == null) {
                return MapMessage.errorMessage("该日报不存在！");
            }
            dailyDate = DateUtils.stringToDate(SafeConverter.toString(daily.getDailyTime()), "yyyyMMdd");
            //新增
        } else {
            daily = new AgentDaily();
            dailyPlan = new AgentDailyPlan();
            //获取日报日期
            dailyDate = getDailyDate();
            if (dailyDate == null) {
                return MapMessage.errorMessage("该时间段不可创建日报！");
            }
        }
        Integer dailyDateInt = SafeConverter.toInt(DateUtils.dateToString(dailyDate, "yyyyMMdd"));
        //新增
        if (StringUtils.isBlank(id)) {
            AgentDaily agentDaily = agentDailyDao.loadByUserIdAndTime(userId, dailyDateInt);
            if (agentDaily != null) {
                return MapMessage.errorMessage("当前用户当天已提报过日报！");
            }
        }
        //获取日报提交状态
        Integer dailyStatus = getDailyStatus();

        //获取日报当天的开始截止时间
        Date startDate = DayRange.newInstance(dailyDate.getTime()).getStartDate();
        Date endDate = DayRange.newInstance(dailyDate.getTime()).getEndDate();

        //获取日报当天的工作记录
        List<WorkRecordData> workRecordList = workRecordService.getWorkRecordDataListByUserTypeTime(Collections.singleton(userId), null, startDate, endDate);
        //日报当天工作
        String todayWork = getWorkStrByUserAndDate(workRecordList);

        //组装进校数据
        List<WorkRecordData> intoSchoolWorkRecordList = new ArrayList<>();
        Set<Long> intoSchoolWorkUserIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        generateIntoSchoolData(userRole, userIds, intoSchoolWorkUserIds, userId, intoSchoolWorkRecordList, startDate, endDate);


        /*
        明日计划
         */
        //进校ID列表
        List<Long> planSchoolIdList = convertStrToList(schoolIdStr);
        //拜访教研员ID列表
        List<Long> planResearcherIdList = convertStrToList(outerResourceIdStr);
        //陪同对象ID列表
        List<Long> planPartnerIdList = convertStrToList(partnerIdStr);
        //组会名称列表
        List<String> planMeetingNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(meetingNameStr)) {
            planMeetingNameList.addAll(Arrays.stream(meetingNameStr.split(",")).collect(Collectors.toSet()));
        }
        String tomorrowPlan = getDailyTomorrowPlanInfo(planSchoolIdList, planResearcherIdList, planPartnerIdList, planMeetingNameList);

        List<WorkRecordVisitUserInfo> visitTeaList = new ArrayList<>();
        List<WorkRecordVisitUserInfo> visitEngTeaList = new ArrayList<>();
        List<WorkRecordVisitUserInfo> visitMathTeaList = new ArrayList<>();
        List<WorkRecordVisitUserInfo> visitOtherTeaList = new ArrayList<>();

        //日报当天拜访老师情况
        visitTeacherInfo(intoSchoolWorkRecordList, visitEngTeaList, visitMathTeaList, visitOtherTeaList, visitTeaList);

        //布置作业的老师
        Set<Long> homeWorkTeacherIds = new HashSet<>();
        Set<Long> homeWorkEngTeacherIds = new HashSet<>();
        Set<Long> homeWorkMathTeacherIds = new HashSet<>();
        Set<Long> homeWorkOtherTeacherIds = new HashSet<>();

        //获取老师布置作业情况
        Map<String, Integer> teacherHomeWorkInfoMap = getTeacherHomeWorkInfo(userRole, visitTeaList, visitEngTeaList, visitMathTeaList, visitOtherTeaList,
                homeWorkTeacherIds, homeWorkEngTeacherIds, homeWorkMathTeacherIds, homeWorkOtherTeacherIds,
                dailyDate, intoSchoolWorkUserIds);

        Integer homeWorkUserNum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkUserNum"));
        Integer homeWorkTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkTeacherSum"));
        Integer homeWorkEnglishTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkEnglishTeacherSum"));
        Integer homeWorkMathTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkMathTeacherSum"));
        Integer homeWorkOtherTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkOtherTeacherSum"));


        daily.setUserId(userId);
        daily.setDailyTime(dailyDateInt);
        daily.setWorkload(agentWorkRecordStatisticsService.calWorkload(workRecordList));
        if (userRole == AgentRoleType.BusinessDeveloper) {
            daily.setIntoSchoolNum(intoSchoolWorkRecordList.size());
            daily.setVisitTeaNum(visitTeaList.size());
            daily.setVisitEngTeaNum(visitEngTeaList.size());
            daily.setVisitMathTeaNum(visitMathTeaList.size());
            daily.setVisitOtherTeaNum(visitOtherTeaList.size());

            daily.setAssignHomeWorkTeaNum(homeWorkTeacherIds.size());
            daily.setAssignHomeWorkEngTeaNum(homeWorkEngTeacherIds.size());
            daily.setAssignHomeWorkMathTeaNum(homeWorkMathTeacherIds.size());
            daily.setAssignHomeWorkOtherTeaNum(homeWorkOtherTeacherIds.size());

        } else {
            int intoSchoolUserSize = intoSchoolWorkUserIds.size();
            daily.setPerPersonIntoSchoolNum(MathUtils.doubleDivide(intoSchoolWorkRecordList.size(), intoSchoolUserSize));
            daily.setPerPersonVisitTeaNum(MathUtils.doubleDivide(visitTeaList.size(), intoSchoolUserSize));
            daily.setPerPersonVisitEngTeaNum(MathUtils.doubleDivide(visitEngTeaList.size(), intoSchoolUserSize));
            daily.setPerPersonVisitMathTeaNum(MathUtils.doubleDivide(visitMathTeaList.size(), intoSchoolUserSize));
            daily.setPerPersonVisitOtherTeaNum(MathUtils.doubleDivide(visitOtherTeaList.size(), intoSchoolUserSize));

            daily.setPerPersonAssignHwTeaNum(MathUtils.doubleDivide(homeWorkTeacherSum, homeWorkUserNum));
            daily.setPerPersonAssignHwEngTeaNum(MathUtils.doubleDivide(homeWorkEnglishTeacherSum, homeWorkUserNum));
            daily.setPerPersonAssignHwMathTeaNum(MathUtils.doubleDivide(homeWorkMathTeacherSum, homeWorkUserNum));
            daily.setPerPersonAssignHwOtherTeaNum(MathUtils.doubleDivide(homeWorkOtherTeacherSum, homeWorkUserNum));
        }
        daily.setOtherWorkResult(otherWorkResult);
        daily.setContent(todayWork);
        daily.setStatus(dailyStatus);
        daily.setDisabled(false);


        dailyPlan.setUserId(userId);
        dailyPlan.setDailyTime(dailyDateInt);
        dailyPlan.setSchoolIdList(planSchoolIdList);
        dailyPlan.setMeetingNameList(planMeetingNameList);
        dailyPlan.setResearcherIdList(planResearcherIdList);
        dailyPlan.setPartnerIdList(planPartnerIdList);
        dailyPlan.setOtherWork(otherWork);
        dailyPlan.setContent(tomorrowPlan);
        dailyPlan.setDisabled(false);

        //今天计划数目
        Integer todayPlanNum = planSchoolIdList.size() + planResearcherIdList.size() + planPartnerIdList.size() + planMeetingNameList.size();

        //昨天日报计划
        Integer yesterdayDailyDateInt = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(dailyDate, -1), "yyyyMMdd"));
        AgentDailyPlan yesterdayDailyPlan = agentDailyPlanDao.loadByUserIdAndTime(userId, yesterdayDailyDateInt);
        List<Long> yesterdayPlanSchoolIdList = new ArrayList<>();
        List<String> yesterdayPlanMeetingNameList = new ArrayList<>();
        List<Long> yesterdayPlanResearcherIdList = new ArrayList<>();
        List<Long> yesterdayPlanPartnerIdList = new ArrayList<>();
        if (yesterdayDailyPlan != null) {
            yesterdayPlanSchoolIdList.addAll(yesterdayDailyPlan.getSchoolIdList());
            yesterdayPlanMeetingNameList.addAll(yesterdayDailyPlan.getMeetingNameList());
            yesterdayPlanResearcherIdList.addAll(yesterdayDailyPlan.getResearcherIdList());
            yesterdayPlanPartnerIdList.addAll(yesterdayDailyPlan.getPartnerIdList());
        }

        //昨天计划数目
        Integer yesterdayPlanNum = yesterdayPlanSchoolIdList.size() + yesterdayPlanMeetingNameList.size() + yesterdayPlanResearcherIdList.size() + yesterdayPlanPartnerIdList.size();

        //上层资源
        Set<Long> outerResourceIds = new HashSet<>();
        Integer meetingCount = 0; //出席人数
        //计划完成情况
        Set<Long> finishSchoolIds = new HashSet<>();
        Set<String> finishMeetingNames = new HashSet<>();
        Set<Long> finishResearcherIds = new HashSet<>();
        Set<Long> finishWorkerIds = new HashSet<>();
        for (WorkRecordData workRecord : workRecordList) {
            //进校
            if (workRecord.getWorkType() == AgentWorkRecordType.SCHOOL) {
                if (yesterdayPlanSchoolIdList.contains(workRecord.getSchoolId())) {
                    finishSchoolIds.add(workRecord.getSchoolId());
                }
            }
            //组会
            if (workRecord.getWorkType() == AgentWorkRecordType.MEETING) {
                if (yesterdayPlanMeetingNameList.contains(workRecord.getWorkTitle())) {
                    finishMeetingNames.add(workRecord.getWorkTitle());
                }
                meetingCount += SafeConverter.toInt(workRecord.getMeetingCount());
            }
            //资源拓维
            if (workRecord.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION) {
                List<WorkRecordVisitUserInfo> visitedUserInfoList = workRecord.getVisitUserInfoList();
                for (WorkRecordVisitUserInfo visitUserInfo : visitedUserInfoList) {
                    if (yesterdayPlanResearcherIdList.contains(visitUserInfo.getId())) {
                        finishResearcherIds.add(visitUserInfo.getId());
                    }
                }
            }
            //陪同
            if (workRecord.getWorkType() == AgentWorkRecordType.ACCOMPANY) {
                AccompanyBusinessType businessType = workRecord.getBusinessType();
                if (businessType != null) {
                    WorkRecordData workInfo = workRecordService.getWorkRecordDataByIdAndType(workRecord.getBusinessRecordId(), AgentWorkRecordType.nameOf(businessType.name()));
                    if (workInfo != null) {
                        if (yesterdayPlanPartnerIdList.contains(workInfo.getUserId())) {
                            finishWorkerIds.add(workInfo.getUserId());
                        }
                    }
                }
            }
            //上层资源
            List<WorkRecordVisitUserInfo> visitUserInfoList = workRecord.getVisitUserInfoList();
            if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
                outerResourceIds.addAll(visitUserInfoList.stream().filter(p -> !Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)).map(WorkRecordVisitUserInfo::getId).collect(Collectors.toSet()));
            }
        }
        //已完成计划数目
        Integer finishPlanNum = finishSchoolIds.size() + finishMeetingNames.size() + finishResearcherIds.size() + finishWorkerIds.size();

        //当天工作记录与前一天日报计划对比信息
        Map<String, Object> compareResultMap = compareWithWorkRecordAndDailyPlan(userId, workRecordList, dailyDate);
        daily.setIntoSchoolCompareList((List<AgentDailyCompareInfo>) compareResultMap.get("intoSchoolCompareList"));
        daily.setMeetingCompareList((List<AgentDailyCompareInfo>) compareResultMap.get("meetingCompareList"));
        daily.setResearcherCompareList((List<AgentDailyCompareInfo>) compareResultMap.get("outerResourceCompareList"));
        daily.setPartnerCompareList((List<AgentDailyCompareInfo>) compareResultMap.get("partnerCompareList"));

        //进校ids
        List<Long> intoSchoolIds = intoSchoolWorkRecordList.stream().map(WorkRecordData::getSchoolId).collect(Collectors.toList());
        Double workload = daily.getWorkload();
        Integer meetingCount1 = meetingCount;
        //编辑
        if (StringUtils.isNotBlank(id)) {
            agentDailyDao.replace(daily);
            agentDailyPlanDao.replace(dailyPlan);
            //专员，工作日，计算得分
            if (userRole == AgentRoleType.BusinessDeveloper && DayUtils.isWorkDay(dailyDateInt)) {
                AlpsThreadPool.getInstance().submit(() -> computeScore(false, id, dailyDateInt, userId, workload, visitTeaList.size(),
                        todayPlanNum, yesterdayPlanNum, finishPlanNum, visitTeaList, intoSchoolIds, outerResourceIds.size(), meetingCount1, dailyStatus));
            }
            //新增
        } else {
            agentDailyDao.insert(daily);
            String dailyId = daily.getId();
            dailyPlan.setDailyId(dailyId);
            agentDailyPlanDao.insert(dailyPlan);
            //专员，工作日，计算得分
            if (userRole == AgentRoleType.BusinessDeveloper && DayUtils.isWorkDay(dailyDateInt)) {
                AlpsThreadPool.getInstance().submit(() -> computeScore(true, dailyId, dailyDateInt, userId, workload, visitTeaList.size(),
                        todayPlanNum, yesterdayPlanNum, finishPlanNum, visitTeaList, intoSchoolIds, outerResourceIds.size(), meetingCount1, dailyStatus));
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 日报当天工作
     *
     * @param workRecordList
     * @return
     */
    public String getWorkStrByUserAndDate(List<WorkRecordData> workRecordList) {
        List<String> todayIntoSchoolNameList = new ArrayList<>();
        List<String> todayMeetingList = new ArrayList<>();
        List<String> todayResourceExtensionList = new ArrayList<>();
        Set<String> todayPartnerList = new HashSet<>();

        workRecordList.forEach(item -> {
            //进校
            if (item.getWorkType() == AgentWorkRecordType.SCHOOL) {
                todayIntoSchoolNameList.add(item.getSchoolName());
            }
            //组会
            if (item.getWorkType() == AgentWorkRecordType.MEETING) {
                todayMeetingList.add(item.getWorkTitle());
            }
            //资源拓维
            if (item.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION) {
                List<WorkRecordVisitUserInfo> visitUserInfoList = item.getVisitUserInfoList();
                if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
                    visitUserInfoList.forEach(p -> {
                        todayResourceExtensionList.add(p.getName());
                    });
                }
            }
            //陪同
            if (item.getWorkType() == AgentWorkRecordType.ACCOMPANY) {
                AccompanyBusinessType businessType = item.getBusinessType();
                if (businessType != null) {
                    WorkRecordData workRecord = workRecordService.getWorkRecordDataByIdAndType(item.getBusinessRecordId(), AgentWorkRecordType.nameOf(businessType.name()));
                    if (workRecord != null) {
                        todayPartnerList.add(workRecord.getUserName());
                    }
                }
            }
        });

        String intoSchoolStr = StringUtils.join(todayIntoSchoolNameList, "、");
        String meetingStr = StringUtils.join(todayMeetingList, "、");
        String resourceExtensionStr = StringUtils.join(todayResourceExtensionList, "、");
        String partnerStr = StringUtils.join(todayPartnerList, "、");

        String todayWork = "";
        if (StringUtils.isNotBlank(intoSchoolStr)) {
            todayWork += "进校（" + intoSchoolStr + "）、";
        }
        if (StringUtils.isNotBlank(meetingStr)) {
            todayWork += "组会（" + meetingStr + "）、";
        }
        if (StringUtils.isNotBlank(resourceExtensionStr)) {
            todayWork += "资源拓维" + resourceExtensionStr + "）、";
        }
        if (StringUtils.isNotBlank(partnerStr)) {
            todayWork += "陪同（" + partnerStr + "）、";
        }
        if (StringUtils.isNotBlank(todayWork)) {
            todayWork = StringUtils.substringBeforeLast(todayWork, "、");
        }
        return todayWork;
    }


    /**
     * 日报明日计划
     *
     * @param planSchoolIdList
     * @param planResearcherIdList
     * @param planPartnerIdList
     * @param planMeetingNameList
     * @return
     */
    public String getDailyTomorrowPlanInfo(List<Long> planSchoolIdList, List<Long> planResearcherIdList, List<Long> planPartnerIdList, List<String> planMeetingNameList) {
        List<String> planIntoSchoolList = new ArrayList<>();
        List<String> planResearcherList = new ArrayList<>();
        List<String> planPartnerList = new ArrayList<>();
        //进校
        if (CollectionUtils.isNotEmpty(planSchoolIdList)) {
            Map<Long, School> schoolMap = raikouSystem.loadSchools(planSchoolIdList);
            planIntoSchoolList = schoolMap.values().stream().map(School::getCname).collect(Collectors.toList());
        }
        //拜访教研员
        if (CollectionUtils.isNotEmpty(planResearcherIdList)) {
            planResearcherList.addAll(workRecordService.generateResourceExtensionTitleByIds(planResearcherIdList));
        }
        //陪同对象
        if (CollectionUtils.isNotEmpty(planPartnerIdList)) {
            List<AgentUser> userList = baseOrgService.getUsers(planPartnerIdList);
            planPartnerList = userList.stream().map(AgentUser::getRealName).collect(Collectors.toList());
        }

        String planIntoSchoolStr = StringUtils.join(planIntoSchoolList, "、");
        String planMeetingStr = StringUtils.join(planMeetingNameList, "、");
        String planResearcherStr = StringUtils.join(planResearcherList, "、");
        String planPartnerStr = StringUtils.join(planPartnerList, "、");


        String tomorrowPlan = "";
        if (StringUtils.isNotBlank(planIntoSchoolStr)) {
            tomorrowPlan += "进校（" + planIntoSchoolStr + "）、";
        }
        if (StringUtils.isNotBlank(planMeetingStr)) {
            tomorrowPlan += "组会（" + planMeetingStr + "）、";
        }
        if (StringUtils.isNotBlank(planResearcherStr)) {
            tomorrowPlan += "教研员（" + planResearcherStr + "）、";
        }
        if (StringUtils.isNotBlank(planPartnerStr)) {
            tomorrowPlan += "陪同（" + planPartnerStr + "）、";
        }
        if (StringUtils.isNotBlank(tomorrowPlan)) {
            tomorrowPlan = StringUtils.substringBeforeLast(tomorrowPlan, "、");
        }
        return tomorrowPlan;
    }

    /**
     * 字符串（逗号隔开）转列表
     *
     * @param str
     * @return
     */
    public List<Long> convertStrToList(String str) {
        List<Long> list = new ArrayList<>();
        if (StringUtils.isNotBlank(str)) {
            List<String> strList = Arrays.stream(str.split(",")).collect(Collectors.toList());
            strList.forEach(item -> list.add(SafeConverter.toLong(item)));
        }
        return list;
    }


    /**
     * 日报提示
     *
     * @param userId
     * @return
     */
    public MapMessage dailyPointOut(Long userId) {
        MapMessage mapMessage = MapMessage.successMessage();
        //获取日报日期
        Date dailyDate = getDailyDate();
        if (dailyDate == null) {
            mapMessage.add("status", "canNotCommit");
            return mapMessage;
        }
        Integer dailyDateInt = SafeConverter.toInt(DateUtils.dateToString(dailyDate, "yyyyMMdd"));
        AgentDaily daily = agentDailyDao.loadByUserIdAndTime(userId, dailyDateInt);
        if (daily != null) {
            mapMessage.add("status", "haveCommit");
        } else {
            mapMessage.add("status", "haveNotCommit");
        }
        return mapMessage;
    }


    /**
     * 获取提报提交状态
     *
     * @return
     */
    public Integer getDailyStatus() {
        Integer nowDateInt = SafeConverter.toInt(DateUtils.dateToString(new Date(), "HHmm"));
        //下午18：00-21：00之间提交，按时交
        if (nowDateInt >= START_SUBMIT_TIME && nowDateInt <= BE_LATE_SUBMIT_TIME) {
            return AgentDaily.ON_TIME_SUBMIT;
            //在21：00-次日09：00之前提交，迟交
        } else if (nowDateInt > BE_LATE_SUBMIT_TIME || nowDateInt < END_SUBMIT_TIME) {
            return AgentDaily.BE_LATE_SUBMIT;
        }
        return null;
    }

    /**
     * 部门及角色列表
     *
     * @param currentUser
     * @return
     */
    public List<Map<String, Object>> groupRoleList(AuthCurrentUser currentUser) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (currentUser.isBusinessDeveloper()) {
            return Collections.emptyList();
        }
        List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(currentUser.getUserId());
        if (CollectionUtils.isEmpty(groupUserByUser)) {
            return Collections.emptyList();
        }
        Long groupId = groupUserByUser.get(0).getGroupId();
        List<Long> groupIds = new ArrayList<>();
        if (currentUser.isCountryManager() || currentUser.isProductOperator() || currentUser.isAdmin()) {
            List<AgentGroup> groupList = baseOrgService.getSubGroupList(groupId);
            groupIds.addAll(groupList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).map(AgentGroup::getId).collect(Collectors.toList()));
        } else {
            groupIds.add(groupId);
        }

        List<AgentGroup> groupList = baseOrgService.getGroupByIds(groupIds);
        if (CollectionUtils.isEmpty(groupList)) {
            return Collections.emptyList();
        }
        groupList.forEach(group -> {
            Integer code = 1;
            //小学
            if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                code = 1;
                //中学
            } else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)) {
                code = 2;
            }

            List<AgentRoleType> userRoleTypeList = new ArrayList<>();
            userRoleTypeList.add(AgentRoleType.BusinessDeveloper);
            userRoleTypeList.add(AgentRoleType.CityManager);
            if (group.fetchGroupRoleType() == AgentGroupRoleType.Area) {
                userRoleTypeList.add(AgentRoleType.AreaManager);
            } else if (group.fetchGroupRoleType() == AgentGroupRoleType.Region || group.fetchGroupRoleType() == AgentGroupRoleType.Marketing) {
                if (code == 1) {
                    userRoleTypeList.add(AgentRoleType.AreaManager);
                }
                userRoleTypeList.add(AgentRoleType.Region);
            }

            for (AgentRoleType roleType : userRoleTypeList) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("code", code);
                dataMap.put("groupId", group.getId());
                dataMap.put("userRoleType", roleType);

                //全国总监、管理员、产品运营，默认显示角色：小学大区经理
                if (currentUser.isCountryManager() || currentUser.isAdmin() || currentUser.isProductOperator()) {
                    if (code == 1 && roleType == AgentRoleType.Region) {
                        dataMap.put("defaultShow", true);
                    }
                }
                //大区经理，默认显示角色：小学：区域经理；中学：市经理
                if (currentUser.isRegionManager()) {
                    if (code == 1 && roleType == AgentRoleType.AreaManager) {
                        dataMap.put("defaultShow", true);
                    }
                    if (code == 2 && roleType == AgentRoleType.CityManager) {
                        dataMap.put("defaultShow", true);
                    }
                }
                //区域经理，默认显示角色：市经理
                if (currentUser.isAreaManager() && roleType == AgentRoleType.CityManager) {
                    dataMap.put("defaultShow", true);
                }
                //市经理，默认显示角色：专员
                if (currentUser.isCityManager() && roleType == AgentRoleType.BusinessDeveloper) {
                    dataMap.put("defaultShow", true);
                }

                dataList.add(dataMap);
            }

        });
        return dataList;

    }

    /**
     * 日报列表
     *
     * @param currentUser
     * @param date
     * @param groupId
     * @param userRoleTypeStr
     * @return
     */
    public List<Map<String, Object>> dailyList(AuthCurrentUser currentUser, Date date, Long groupId, String userRoleTypeStr) {
        AgentRoleType userRoleType = null;
        if (StringUtils.isNotBlank(userRoleTypeStr)) {
            userRoleType = AgentRoleType.nameOf(userRoleTypeStr);
        } else {
            userRoleType = AgentRoleType.BusinessDeveloper;
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        Set<Long> groupIds = new HashSet<>();
        //专员、市经理
        if (currentUser.isBusinessDeveloper() || currentUser.isCityManager()) {
            //当前人员所在部门
            AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(currentUser.getUserId()).stream().findFirst().orElse(null);
            Long currentGroupId = groupUser.getGroupId();
            //所属分区内所有专员
            userIds.addAll(baseOrgService.getGroupUsersByRole(currentGroupId, userRoleType));
            groupIds.add(currentGroupId);
        } else {
            groupIds.add(groupId);
            //子部门
            List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId);
            if (CollectionUtils.isNotEmpty(subGroupList)) {
                groupIds.addAll(subGroupList.stream().map(AgentGroup::getId).collect(Collectors.toSet()));
            }
            userIds.addAll(baseOrgService.getUserByGroupIdsAndRole(groupIds, userRoleType));
        }

        Integer dateInt = SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd"));
        //获取日报列表
        Map<Long, AgentDaily> userDailyMap = agentDailyDao.loadByUserIdsAndTime(userIds, dateInt);

        //获取日报计划列表
        Map<Long, AgentDailyPlan> userDailyPlanMap = agentDailyPlanDao.loadByUserIdsAndTime(userIds, dateInt);

        Map<Long, AgentUser> userMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), ((o1, o2) -> o1)));

        Map<Long, List<Long>> userGroupIdListMap = baseOrgService.getUserGroupIdList(userIds);

        Map<Long, AgentGroup> groupMap = baseOrgService.getGroupByIds(groupIds).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), ((o1, o2) -> o1)));

        /*
        1.按照“迟交、按时提交、未提交”的顺序展示
        2.相同状态的，按照提交时间由近到远展示
         */
        List<AgentDaily> onTimeSubmitDailyList = new ArrayList<>(userDailyMap.values()).stream().filter(p -> Objects.equals(p.getStatus(), AgentDaily.ON_TIME_SUBMIT)).sorted(Comparator.comparing(AgentDaily::getUpdateTime).reversed()).collect(Collectors.toList());
        List<AgentDaily> beLateSubmitDailyList = new ArrayList<>(userDailyMap.values()).stream().filter(p -> Objects.equals(p.getStatus(), AgentDaily.BE_LATE_SUBMIT)).sorted(Comparator.comparing(AgentDaily::getUpdateTime).reversed()).collect(Collectors.toList());
        List<AgentDaily> sortDailyList = new ArrayList<>();
        sortDailyList.addAll(beLateSubmitDailyList);
        sortDailyList.addAll(onTimeSubmitDailyList);
        //已提交日报人员
        List<Long> commitUserIds = new ArrayList<>();
        sortDailyList.forEach(item -> {
            commitUserIds.add(item.getUserId());
        });

        //人员日报得分
        Map<Long, AgentDailyScore> userDailyScoreMap = new HashMap<>();
        if (userRoleType == AgentRoleType.BusinessDeveloper) {
            userDailyScoreMap.putAll(agentDailyScoreDao.loadByUserIdsAndTimeAndIndex(commitUserIds, dateInt, AgentDailyScoreIndex.TOTAL_SCORE));
        }


        //未提交日报人员
        List<Long> unCommitUserIds = new ArrayList<>();
        userIds.forEach(item -> {
            if (!commitUserIds.contains(item)) {
                unCommitUserIds.add(item);
            }
        });

        //已提交日报
        List<Map<String, Object>> commitList = commitDailyList(commitUserIds, userMap, userGroupIdListMap, groupMap, userDailyMap, dateInt, userRoleType, userDailyScoreMap, userDailyPlanMap, currentUser);
        //未提交日报
        List<Map<String, Object>> unCommitList = unCommitDailyList(unCommitUserIds, date, userRoleType, userMap, userGroupIdListMap, groupMap, currentUser);

        dataList.addAll(commitList);
        dataList.addAll(unCommitList);
        return dataList;
    }

    /**
     * 已提交日报列表
     *
     * @param commitUserIds
     * @param userMap
     * @param userGroupIdListMap
     * @param groupMap
     * @param userDailyMap
     * @param dateInt
     * @param userRoleType
     * @param userDailyScoreMap
     * @param userDailyPlanMap
     * @param currentUser
     * @return
     */
    public List<Map<String, Object>> commitDailyList(Collection<Long> commitUserIds, Map<Long, AgentUser> userMap, Map<Long, List<Long>> userGroupIdListMap,
                                                     Map<Long, AgentGroup> groupMap, Map<Long, AgentDaily> userDailyMap, Integer dateInt, AgentRoleType userRoleType,
                                                     Map<Long, AgentDailyScore> userDailyScoreMap, Map<Long, AgentDailyPlan> userDailyPlanMap, AuthCurrentUser currentUser) {
        List<Map<String, Object>> commitList = new ArrayList<>();
        for (Long userId : commitUserIds) {
            Map<String, Object> dataMap = new HashMap<>();
            AgentUser user = userMap.get(userId);
            dataMap.put("userId", userId);
            if (user != null) {
                dataMap.put("userAvatar", user.getAvatar());
                dataMap.put("userName", user.getRealName());
            }
            List<Long> groupIdList = userGroupIdListMap.get(userId);
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                AgentGroup group = groupMap.get(groupIdList.get(0));
                if (group != null) {
                    dataMap.put("groupName", group.getGroupName());
                }
            }

            AgentDaily daily = userDailyMap.get(userId);
            if (daily != null) {
                Boolean isNextDay = false;
                dataMap.put("id", daily.getId());
                dataMap.put("todayWork", daily.getContent());
                //判断是否是次日
                Integer updateTimeInt = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(daily.getUpdateTime(), -1), "yyyyMMdd"));
                if (Objects.equals(updateTimeInt, dateInt)) {
                    isNextDay = true;
                }
                dataMap.put("isNextDay", isNextDay);
                dataMap.put("commitTime", DateUtils.dateToString(daily.getUpdateTime(), "HH:mm"));
                dataMap.put("status", daily.getStatus());
                dataMap.put("workload", daily.getWorkload() != null ? daily.getWorkload() : 0);
                if (Objects.equals(userRoleType, AgentRoleType.BusinessDeveloper)) {
                    dataMap.put("intoSchoolNum", daily.getIntoSchoolNum() != null ? daily.getIntoSchoolNum() : 0);
                    dataMap.put("visitTeaCount", daily.getVisitTeaNum() != null ? daily.getVisitTeaNum() : 0);
                    dataMap.put("visitEngTeaCount", daily.getVisitEngTeaNum() != null ? daily.getVisitEngTeaNum() : 0);
                    dataMap.put("visitMathTeaCount", daily.getVisitMathTeaNum() != null ? daily.getVisitMathTeaNum() : 0);
                    dataMap.put("visitOtherTeaCount", daily.getVisitOtherTeaNum() != null ? daily.getVisitOtherTeaNum() : 0);

                    //人员得分
                    AgentDailyScore dailyScore = userDailyScoreMap.get(userId);
                    if (dailyScore != null) {
                        dataMap.put("score", MathUtils.doubleMultiply(dailyScore.getScore(), 1, 1));
                    }
                } else {
                    dataMap.put("perPersonIntoSchoolNum", daily.getPerPersonIntoSchoolNum() != null ? daily.getPerPersonIntoSchoolNum() : 0);
                    dataMap.put("perPersonVisitTeaNum", daily.getPerPersonVisitTeaNum() != null ? daily.getPerPersonVisitTeaNum() : 0);
                    dataMap.put("perPersonVisitEngTeaNum", daily.getPerPersonVisitEngTeaNum() != null ? daily.getPerPersonVisitEngTeaNum() : 0);
                    dataMap.put("perPersonVisitMathTeaNum", daily.getPerPersonVisitMathTeaNum() != null ? daily.getPerPersonVisitMathTeaNum() : 0);
                    dataMap.put("perPersonVisitOtherTeaNum", daily.getPerPersonVisitOtherTeaNum() != null ? daily.getPerPersonVisitOtherTeaNum() : 0);
                }
            }
            AgentDailyPlan dailyPlan = userDailyPlanMap.get(userId);
            if (dailyPlan != null) {
                dataMap.put("tomorrowPlan", dailyPlan.getContent());
            }
            Boolean isOneself = false;
            if (Objects.equals(userId, currentUser.getUserId())) {
                isOneself = true;
            }
            dataMap.put("isOneself", isOneself);
            commitList.add(dataMap);
        }
        return commitList;
    }


    /**
     * 未提交日报列表
     *
     * @param unCommitUserIds
     * @param date
     * @param userRoleType
     * @param userMap
     * @param userGroupIdListMap
     * @param groupMap
     * @param currentUser
     * @return
     */
    public List<Map<String, Object>> unCommitDailyList(List<Long> unCommitUserIds, Date date, AgentRoleType userRoleType, Map<Long, AgentUser> userMap, Map<Long, List<Long>> userGroupIdListMap,
                                                       Map<Long, AgentGroup> groupMap, AuthCurrentUser currentUser) {
        List<Map<String, Object>> unCommitList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(unCommitUserIds)) {
            DayRange dayRange = DayRange.current();
            //获取日报当天的开始截止时间
            Date startDate = DayRange.newInstance(date.getTime()).getStartDate();
            Date endDate = DayRange.newInstance(date.getTime()).getEndDate();
            //如果非当前天，专员的情况，数据从工作记录数据统计表获取
            if (date.getTime() < dayRange.getStartTime() && Objects.equals(userRoleType, AgentRoleType.BusinessDeveloper)) {
                Integer day = SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd"));
                Map<Long, AgentWorkRecordStatistics> userWorkRecordStatistics = agentWorkRecordStatisticsDao.getUserWorkRecordStatistics(unCommitUserIds, day, 1);
                unCommitUserIds.forEach(item -> {
                    AgentWorkRecordStatistics workRecordStatistics = userWorkRecordStatistics.get(item);
                    Map<String, Object> dataMap = new HashMap<>();
                    AgentUser user = userMap.get(item);
                    dataMap.put("userId", item);
                    if (user != null) {
                        dataMap.put("userAvatar", user.getAvatar());
                        dataMap.put("userName", user.getRealName());
                    }
                    List<Long> groupIdList = userGroupIdListMap.get(item);
                    if (CollectionUtils.isNotEmpty(groupIdList)) {
                        AgentGroup group = groupMap.get(groupIdList.get(0));
                        if (group != null) {
                            dataMap.put("groupName", group.getGroupName());
                        }
                    }
                    dataMap.put("status", -1);
                    Double workload = 0D;
                    Integer intoSchoolNum = 0;
                    Integer visitTeaCount = 0;
                    Integer visitEngTeaCount = 0;
                    Integer visitMathTeaCount = 0;
                    Integer visitOtherTeaCount = 0;
                    if (workRecordStatistics != null) {
                        workload = workRecordStatistics.getUserWorkload() != null ? workRecordStatistics.getUserWorkload() : 0D;
                        intoSchoolNum = workRecordStatistics.getBdIntoSchoolCount() != null ? workRecordStatistics.getBdIntoSchoolCount() : 0;
                        visitTeaCount = workRecordStatistics.getBdVisitTeacherCount() != null ? workRecordStatistics.getBdVisitTeacherCount() : 0;
                        visitEngTeaCount = workRecordStatistics.getBdVisitEngTeacherCount() != null ? workRecordStatistics.getBdVisitEngTeacherCount() : 0;
                        visitMathTeaCount = workRecordStatistics.getBdVisitMathTeacherCount() != null ? workRecordStatistics.getBdVisitMathTeacherCount() : 0;
                        visitOtherTeaCount = workRecordStatistics.getBdVisitOtherTeacherCount() != null ? workRecordStatistics.getBdVisitOtherTeacherCount() : 0;
                    }
                    dataMap.put("workload", workload);
                    dataMap.put("intoSchoolNum", intoSchoolNum);
                    dataMap.put("visitTeaCount", visitTeaCount);
                    dataMap.put("visitEngTeaCount", visitEngTeaCount);
                    dataMap.put("visitMathTeaCount", visitMathTeaCount);
                    dataMap.put("visitOtherTeaCount", visitOtherTeaCount);
                    Boolean isOneself = false;
                    if (Objects.equals(item, currentUser.getUserId())) {
                        isOneself = true;
                    }
                    dataMap.put("isOneself", isOneself);
                    unCommitList.add(dataMap);
                });
                //如果是当前天，或者非专员的情况下，数据实时获取
            } else {
                //工作记录
                List<WorkRecordData> workRecordList = workRecordService.getWorkRecordDataListByUserTypeTime(unCommitUserIds, null, startDate, endDate);
                Map<Long, List<WorkRecordData>> userWorkRecordMap = workRecordList.stream().filter(p -> p.getUserId() != null).collect(Collectors.groupingBy(WorkRecordData::getUserId, Collectors.toList()));

                //进校工作记录
                List<WorkRecordData> intoSchoolWorkRecordList = workRecordList.stream().filter(p -> p.getWorkType() == AgentWorkRecordType.SCHOOL).collect(Collectors.toList());
                Map<Long, List<WorkRecordData>> userIntoSchoolWorkRecordMap = intoSchoolWorkRecordList.stream().filter(p -> p.getUserId() != null).collect(Collectors.groupingBy(WorkRecordData::getUserId, Collectors.toList()));

                for (Long userId : unCommitUserIds) {
                    Map<String, Object> dataMap = new HashMap<>();
                    AgentUser user = userMap.get(userId);
                    dataMap.put("userId", userId);
                    if (user != null) {
                        dataMap.put("userAvatar", user.getAvatar());
                        dataMap.put("userName", user.getRealName());
                    }
                    List<Long> groupIdList = userGroupIdListMap.get(userId);
                    if (CollectionUtils.isNotEmpty(groupIdList)) {
                        AgentGroup group = groupMap.get(groupIdList.get(0));
                        if (group != null) {
                            dataMap.put("groupName", group.getGroupName());
                        }
                    }
                    List<WorkRecordVisitUserInfo> allVisitTeacherList = new ArrayList<>();
                    List<WorkRecordVisitUserInfo> englishVisitTeacherList = new ArrayList<>();
                    List<WorkRecordVisitUserInfo> mathVisitTeacherList = new ArrayList<>();
                    List<WorkRecordVisitUserInfo> otherVisitTeacherList = new ArrayList<>();

                    List<WorkRecordData> workRecords = userWorkRecordMap.get(userId);

                    dataMap.put("status", -1);
                    dataMap.put("workload", agentWorkRecordStatisticsService.calWorkload(workRecords));
                    //专员
                    if (Objects.equals(userRoleType, AgentRoleType.BusinessDeveloper)) {

                        List<WorkRecordData> intoSchoolWorkRecords = userIntoSchoolWorkRecordMap.get(userId);

                        visitTeacherInfo(intoSchoolWorkRecords, englishVisitTeacherList, mathVisitTeacherList, otherVisitTeacherList, allVisitTeacherList);

                        dataMap.put("intoSchoolNum", intoSchoolWorkRecords != null ? intoSchoolWorkRecords.size() : 0);
                        dataMap.put("visitTeaCount", allVisitTeacherList.size());
                        dataMap.put("visitEngTeaCount", englishVisitTeacherList.size());
                        dataMap.put("visitMathTeaCount", mathVisitTeacherList.size());
                        dataMap.put("visitOtherTeaCount", otherVisitTeacherList.size());
                    }
                    Boolean isOneself = false;
                    if (Objects.equals(userId, currentUser.getUserId())) {
                        isOneself = true;
                    }
                    dataMap.put("isOneself", isOneself);
                    unCommitList.add(dataMap);
                }
            }

            unCommitList.sort(Comparator.comparing(p -> p.get("userName") != null ? SafeConverter.toString(p.get("userName")) : "", Collator.getInstance(java.util.Locale.CHINA)));
        }
        return unCommitList;
    }

    /**
     * 日报详情
     *
     * @param date
     * @param userId
     * @param currentUserId
     * @param type          1:详情页  2：编辑回显页
     * @return
     */
    public Map<String, Object> dailyDetail(Date date, Long userId, Long currentUserId, Integer type) {
        Map<String, Object> dataMap = new HashMap<>();

        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        Integer dateInt = SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd"));
        AgentDaily daily = agentDailyDao.loadByUserIdAndTime(userId, dateInt);

        Date startDate = DayRange.newInstance(date.getTime()).getStartDate();
        Date endDate = null;
        //详情页
        if (type == 1) {
            if (daily != null) {
                endDate = daily.getUpdateTime();
            } else {
                endDate = DayRange.newInstance(date.getTime()).getEndDate();
            }
            //编辑回显页
        } else if (type == 2) {
            endDate = DayRange.newInstance(date.getTime()).getEndDate();
        }

        List<AgentDailyCompareInfo> intoSchoolCompareList = new ArrayList<>(); //进校对比
        List<AgentDailyCompareInfo> meetingCompareList = new ArrayList<>();    //组会对比
        List<AgentDailyCompareInfo> outerResourceCompareList = new ArrayList<>(); //拜访教研员对比
        List<AgentDailyCompareInfo> partnerCompareList = new ArrayList<>();    //陪同对象对比

        if (daily != null) {

            //日报得分列表
            List<AgentDailyScore> dailyScoreList = agentDailyScoreDao.loadByDailyId(daily.getId());

            dataMap.put("id", daily.getId());
            dataMap.put("commitTime", DateUtils.dateToString(daily.getUpdateTime(), "HH:mm"));
            //详情
            if (type == 1) {
                if (userRole == AgentRoleType.BusinessDeveloper) {
                    dataMap.put("visitTeaNum", daily.getVisitTeaNum() != null ? daily.getVisitTeaNum() : 0);
                    dataMap.put("visitEngTeaNum", daily.getVisitEngTeaNum() != null ? daily.getVisitEngTeaNum() : 0);
                    dataMap.put("visitMathTeaNum", daily.getVisitMathTeaNum() != null ? daily.getVisitMathTeaNum() : 0);
                    dataMap.put("visitOtherTeaNum", daily.getVisitOtherTeaNum() != null ? daily.getVisitOtherTeaNum() : 0);

                    dataMap.put("homeWorkTeaNum", daily.getAssignHomeWorkTeaNum() != null ? daily.getAssignHomeWorkTeaNum() : 0);
                    dataMap.put("homeWorkEngTeaNum", daily.getAssignHomeWorkEngTeaNum() != null ? daily.getAssignHomeWorkEngTeaNum() : 0);
                    dataMap.put("homeWorkMathTeaNum", daily.getAssignHomeWorkMathTeaNum() != null ? daily.getAssignHomeWorkMathTeaNum() : 0);
                    dataMap.put("homeWorkOtherTeaNum", daily.getAssignHomeWorkOtherTeaNum() != null ? daily.getAssignHomeWorkOtherTeaNum() : 0);

                    //日报总得分
                    AgentDailyScore dailyScore = dailyScoreList.stream().filter(p -> p.getIndex() == AgentDailyScoreIndex.TOTAL_SCORE).findFirst().orElse(null);
                    if (dailyScore != null) {
                        dataMap.put("score", dailyScore.getScore() != null ? MathUtils.doubleMultiply(dailyScore.getScore(), 1, 1) : 0.0);
                    }
                } else {
                    dataMap.put("perPersonVisitTeaNum", daily.getPerPersonVisitTeaNum() != null ? daily.getPerPersonVisitTeaNum() : 0);
                    dataMap.put("perPersonVisitEngTeaNum", daily.getPerPersonVisitEngTeaNum() != null ? daily.getPerPersonVisitEngTeaNum() : 0);
                    dataMap.put("perPersonVisitMathTeaNum", daily.getPerPersonVisitMathTeaNum() != null ? daily.getPerPersonVisitMathTeaNum() : 0);
                    dataMap.put("perPersonVisitOtherTeaNum", daily.getPerPersonVisitOtherTeaNum() != null ? daily.getPerPersonVisitOtherTeaNum() : 0);

                    dataMap.put("perPersonHomeWorkTeaNum", daily.getPerPersonAssignHwTeaNum() != null ? daily.getPerPersonAssignHwTeaNum() : 0);
                    dataMap.put("perPersonHomeWorkEngTeaNum", daily.getPerPersonAssignHwEngTeaNum() != null ? daily.getPerPersonAssignHwEngTeaNum() : 0);
                    dataMap.put("perPersonHomeWorkMathTeaNum", daily.getPerPersonAssignHwMathTeaNum() != null ? daily.getPerPersonAssignHwMathTeaNum() : 0);
                    dataMap.put("perPersonHomeWorkOtherTeaNum", daily.getPerPersonAssignHwOtherTeaNum() != null ? daily.getPerPersonAssignHwOtherTeaNum() : 0);
                }

                //获取对比信息
                if (CollectionUtils.isNotEmpty(daily.getIntoSchoolCompareList()) || CollectionUtils.isNotEmpty(daily.getMeetingCompareList())
                        || CollectionUtils.isNotEmpty(daily.getResearcherCompareList()) || CollectionUtils.isNotEmpty(daily.getPartnerCompareList())) {
                    intoSchoolCompareList = daily.getIntoSchoolCompareList();
                    meetingCompareList = daily.getMeetingCompareList();
                    outerResourceCompareList = daily.getResearcherCompareList();
                    partnerCompareList = daily.getPartnerCompareList();
                } else {
                    //获取当天的工作记录
                    List<WorkRecordData> workRecordList = workRecordService.getWorkRecordDataListByUserTypeTime(Collections.singleton(userId), null, startDate, endDate);
                    //当天工作记录与前一天日报计划对比信息
                    Map<String, Object> compareResultMap = compareWithWorkRecordAndDailyPlan(userId, workRecordList, date);
                    intoSchoolCompareList = (List<AgentDailyCompareInfo>) compareResultMap.get("intoSchoolCompareList");
                    meetingCompareList = (List<AgentDailyCompareInfo>) compareResultMap.get("meetingCompareList");
                    outerResourceCompareList = (List<AgentDailyCompareInfo>) compareResultMap.get("outerResourceCompareList");
                    partnerCompareList = (List<AgentDailyCompareInfo>) compareResultMap.get("partnerCompareList");
                }
                //编辑回显
            } else if (type == 2) {
                List<WorkRecordVisitUserInfo> visitTeaList = new ArrayList<>();
                List<WorkRecordVisitUserInfo> visitEngTeaList = new ArrayList<>();
                List<WorkRecordVisitUserInfo> visitMathTeaList = new ArrayList<>();
                List<WorkRecordVisitUserInfo> visitOtherTeaList = new ArrayList<>();

                Set<Long> intoSchoolWorkUserIds = new HashSet<>();
                Set<Long> userIds = new HashSet<>();

                //获取当天的工作记录
                List<WorkRecordData> workRecordList = workRecordService.getWorkRecordDataListByUserTypeTime(Collections.singleton(userId), null, startDate, endDate);

                List<WorkRecordData> intoSchoolWorkRecordDataList = new ArrayList<>();
                generateIntoSchoolData(userRole, userIds, intoSchoolWorkUserIds, userId, intoSchoolWorkRecordDataList, startDate, endDate);

                visitTeacherInfo(intoSchoolWorkRecordDataList, visitEngTeaList, visitMathTeaList, visitOtherTeaList, visitTeaList);

                //拜访后布置作业的老师
                Set<Long> homeWorkTeacherIds = new HashSet<>();
                Set<Long> homeWorkEngTeacherIds = new HashSet<>();
                Set<Long> homeWorkMathTeacherIds = new HashSet<>();
                Set<Long> homeWorkOtherTeacherIds = new HashSet<>();

                //获取布置作业信息
                Map<String, Integer> teacherHomeWorkInfoMap = getTeacherHomeWorkInfo(userRole, visitTeaList, visitEngTeaList, visitMathTeaList, visitOtherTeaList,
                        homeWorkTeacherIds, homeWorkEngTeacherIds, homeWorkMathTeacherIds, homeWorkOtherTeacherIds,
                        date, intoSchoolWorkUserIds);

                Integer homeWorkUserNum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkUserNum"));
                Integer homeWorkTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkTeacherSum"));
                Integer homeWorkEnglishTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkEnglishTeacherSum"));
                Integer homeWorkMathTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkMathTeacherSum"));
                Integer homeWorkOtherTeacherSum = SafeConverter.toInt(teacherHomeWorkInfoMap.get("homeWorkOtherTeacherSum"));
                if (userRole == AgentRoleType.BusinessDeveloper) {
                    dataMap.put("visitTeaNum", visitTeaList.size());
                    dataMap.put("visitEngTeaNum", visitEngTeaList.size());
                    dataMap.put("visitMathTeaNum", visitMathTeaList.size());
                    dataMap.put("visitOtherTeaNum", visitOtherTeaList.size());

                    dataMap.put("homeWorkTeaNum", homeWorkTeacherIds.size());
                    dataMap.put("homeWorkEngTeaNum", homeWorkEngTeacherIds.size());
                    dataMap.put("homeWorkMathTeaNum", homeWorkMathTeacherIds.size());
                    dataMap.put("homeWorkOtherTeaNum", homeWorkOtherTeacherIds.size());
                } else {
                    int userSize = intoSchoolWorkUserIds.size();
                    dataMap.put("perPersonVisitTeaNum", MathUtils.doubleDivide(visitTeaList.size(), userSize));
                    dataMap.put("perPersonVisitEngTeaNum", MathUtils.doubleDivide(visitEngTeaList.size(), userSize));
                    dataMap.put("perPersonVisitMathTeaNum", MathUtils.doubleDivide(visitMathTeaList.size(), userSize));
                    dataMap.put("perPersonVisitOtherTeaNum", MathUtils.doubleDivide(visitOtherTeaList.size(), userSize));

                    dataMap.put("perPersonHomeWorkTeaNum", MathUtils.doubleDivide(homeWorkTeacherSum, homeWorkUserNum));
                    dataMap.put("perPersonHomeWorkEngTeaNum", MathUtils.doubleDivide(homeWorkEnglishTeacherSum, homeWorkUserNum));
                    dataMap.put("perPersonHomeWorkMathTeaNum", MathUtils.doubleDivide(homeWorkMathTeacherSum, homeWorkUserNum));
                    dataMap.put("perPersonHomeWorkOtherTeaNum", MathUtils.doubleDivide(homeWorkOtherTeacherSum, homeWorkUserNum));
                }

                //当天工作记录与前一天日报计划对比信息
                Map<String, Object> compareResultMap = compareWithWorkRecordAndDailyPlan(userId, workRecordList, date);
                intoSchoolCompareList = (List<AgentDailyCompareInfo>) compareResultMap.get("intoSchoolCompareList");
                meetingCompareList = (List<AgentDailyCompareInfo>) compareResultMap.get("meetingCompareList");
                outerResourceCompareList = (List<AgentDailyCompareInfo>) compareResultMap.get("outerResourceCompareList");
                partnerCompareList = (List<AgentDailyCompareInfo>) compareResultMap.get("partnerCompareList");
            }

            dataMap.put("otherWorkResult", daily.getOtherWorkResult());
        }

        dataMap.put("intoSchoolCompareList", intoSchoolCompareList);
        dataMap.put("meetingCompareList", meetingCompareList);
        dataMap.put("outerResourceCompareList", outerResourceCompareList);
        dataMap.put("partnerCompareList", partnerCompareList);

        Map<String, Object> userMap = userInfo(userId, startDate, endDate);
        dataMap.put("userAvatar", userMap.get("userAvatar"));
        dataMap.put("userName", userMap.get("userName"));
        dataMap.put("groupName", userMap.get("groupName"));
        dataMap.put("workload", userMap.get("workload"));

        AgentDailyPlan dailyPlan = agentDailyPlanDao.loadByUserIdAndTime(userId, dateInt);
        List<Map<String, Object>> intoSchoolList = new ArrayList<>();
        if (dailyPlan != null) {
            //进校ID列表
            List<Long> schoolIdList = dailyPlan.getSchoolIdList();
            if (CollectionUtils.isNotEmpty(schoolIdList)) {
                Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIdList);
                schoolIdList.forEach(item -> {
                    School school = schoolMap.get(item);
                    if (school != null) {
                        Map<String, Object> schoolItemMap = new HashMap<>();
                        schoolItemMap.put("schoolId", item);
                        schoolItemMap.put("schoolName", school.getCname());
                        intoSchoolList.add(schoolItemMap);
                    }
                });
            }

            //组会列表
            List<String> meetingNameList = dailyPlan.getMeetingNameList();

            List<Map<String, Object>> outerResourceList = new ArrayList<>();
            //上层资源ID列表
            List<Long> outerResourceIds = dailyPlan.getResearcherIdList();
            if (CollectionUtils.isNotEmpty(outerResourceIds)) {
                Map<Long, Map<String, Object>> resourceInfoMap = agentOuterResourceService.getResourceInfoByIds(outerResourceIds);
                resourceInfoMap.forEach((k, v) -> {
                    outerResourceList.add(v);
                });
            }

            //陪同人ID列表
            List<Long> partnerIdList = dailyPlan.getPartnerIdList();
            List<AgentUser> userList = baseOrgService.getUsers(partnerIdList);

            dataMap.put("intoSchoolList", intoSchoolList);
            dataMap.put("meetingList", meetingNameList);
            dataMap.put("outerResourceList", outerResourceList);
            dataMap.put("partnerList", userList);
            dataMap.put("otherWork", dailyPlan.getOtherWork());

        }
        //判断是否可以编辑
        Boolean canEdit = false;
        Integer todayInt = SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMMdd"));
        Integer yesterdayInt = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(new Date(), -1), "yyyyMMdd"));
        Integer nowDateInt = SafeConverter.toInt(DateUtils.dateToString(new Date(), "HHmm"));
        if (Objects.equals(currentUserId, userId)) {
            //18:00之后，可编辑当天日报
            if (nowDateInt > START_SUBMIT_TIME && Objects.equals(dateInt, todayInt)) {
                canEdit = true;
            }
            //09:00之前，可编辑昨天日报
            if (nowDateInt < END_SUBMIT_TIME && Objects.equals(dateInt, yesterdayInt)) {
                canEdit = true;
            }
        }
        dataMap.put("canEdit", canEdit);
        dataMap.put("userRoleType", userRole);
        return dataMap;
    }


    /**
     * 拜访老师情况
     *
     * @param workRecordList
     * @param visitEngTeaList
     * @param visitMathTeaList
     * @param visitOtherTeaList
     * @param visitTeaList
     */
    public void visitTeacherInfo(List<WorkRecordData> workRecordList, List<WorkRecordVisitUserInfo> visitEngTeaList, List<WorkRecordVisitUserInfo> visitMathTeaList, List<WorkRecordVisitUserInfo> visitOtherTeaList, List<WorkRecordVisitUserInfo> visitTeaList) {
        if (CollectionUtils.isNotEmpty(workRecordList)) {
            //有科目老师
            List<WorkRecordVisitUserInfo> allSubjectTeaList = new ArrayList<>();
            Set<Long> allSubjectTeaIds = new HashSet<>();
            //无科目老师
            List<WorkRecordVisitUserInfo> allNoSubjectTeaList = new ArrayList<>();

            List<WorkRecordVisitUserInfo> allTeaList = new ArrayList<>();
            workRecordList.forEach(item -> {
                //拜访老师统计
                List<WorkRecordVisitUserInfo> visitUserInfoList = item.getVisitUserInfoList();
                if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
                    List<WorkRecordVisitUserInfo> visitTeacherList = visitUserInfoList.stream().filter(p -> p != null && p.getJob() != null && Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)).collect(Collectors.toList());
                    visitTeacherList.forEach(visitTeacher -> {
                        visitTeacher.setVisitTime(item.getWorkTime());
                        //无科目老师
                        if (visitTeacher.getSubject() == null || visitTeacher.getSubject() == Subject.UNKNOWN) {
                            allNoSubjectTeaList.add(visitTeacher);
                        } else {
                            if (!allSubjectTeaIds.contains(visitTeacher.getId())) {
                                allSubjectTeaIds.add(visitTeacher.getId());
                                allSubjectTeaList.add(visitTeacher);
                            }
                        }
                    });
                }
            });
            allTeaList.addAll(allSubjectTeaList);
            allTeaList.addAll(allNoSubjectTeaList);
            allTeaList.forEach(visitTeacher -> {
                //英语
                if (visitTeacher.getSubject() == Subject.ENGLISH) {
                    visitEngTeaList.add(visitTeacher);
                    //数学
                } else if (visitTeacher.getSubject() == Subject.MATH) {
                    visitMathTeaList.add(visitTeacher);
                    //其他
                } else {
                    visitOtherTeaList.add(visitTeacher);
                }
                visitTeaList.add(visitTeacher);
            });
        }
    }

    private String getResearchersAddress(AgentResearchers researchers) {
        Integer level = SafeConverter.toInt(researchers.getLevel());
        if (level == 1) {
            ExRegion region = raikouSystem.loadRegion(SafeConverter.toInt(researchers.getProvince()));
            if (region != null) {
                return region.getProvinceName();
            }
        }
        if (level == 2) {
            ExRegion city = raikouSystem.loadRegion(SafeConverter.toInt(researchers.getCity()));
            if (city != null) {
                return String.format("%s%s", city.getCityName(), city.getCityName());
            }
        }
        if (level == 3) {
            ExRegion country = raikouSystem.loadRegion(SafeConverter.toInt(researchers.getCounty()));
            if (country != null) {
                return String.format("%s%s%s", country.getProvinceName(), country.getCityName(), country.getCountyName());
            }
        }
        return null;
    }

    public Map<String, Object> rangeOrganizationRole(Long groupId, String groupRoleType, String roleType, Long userId) {
        Map<String, Object> dataMap = new HashMap<>();
        AgentGroup group = new AgentGroup();
        List<Map<String, Object>> groupRoleTypeList = new ArrayList<>();
        List<Map<String, Object>> roleTypeList = new ArrayList<>();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        //默认
        if (groupId == 0L && StringUtils.isBlank(groupRoleType) && StringUtils.isBlank(roleType)) {
            //全国总监
            if (userRole == AgentRoleType.Country) {
                //小学市场
                group = baseOrgService.findAllGroups().stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
            } else {
                AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
                group = baseOrgService.getGroupById(groupUser.getGroupId());
            }
        } else {
            group = baseOrgService.getGroupById(groupId);
        }
        //组织
        Map<String, Object> organizationMap = generateOrganization(group, groupRoleType);
        AgentGroupRoleType showGroupRoleType = (AgentGroupRoleType) organizationMap.get("showGroupRoleType");
        groupRoleTypeList = (List<Map<String, Object>>) organizationMap.get("groupRoleTypeList");
        //角色
        roleTypeList = generateRole(group, showGroupRoleType, roleType);

        dataMap.put("group", group);
        dataMap.put("groupRoleTypeList", groupRoleTypeList);
        dataMap.put("roleTypeList", roleTypeList);
        return dataMap;
    }

    public Map<String, Object> generateOrganization(AgentGroup group, String groupRoleType) {
        AgentGroupRoleType agentGroupRoleType = null;
        if (StringUtils.isNotBlank(groupRoleType)) {
            agentGroupRoleType = AgentGroupRoleType.nameOf(groupRoleType);
        }
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> groupRoleTypeList = new ArrayList<>();
        AgentGroupRoleType showGroupRoleType = null;
        AgentGroupRoleType currentGroupRoleType = group.fetchGroupRoleType();
        //分区、区域
        if (currentGroupRoleType == AgentGroupRoleType.City || currentGroupRoleType == AgentGroupRoleType.Area) {
            showGroupRoleType = AgentGroupRoleType.City;
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City, true));
            //大区
        } else if (currentGroupRoleType == AgentGroupRoleType.Region) {
            //小学大区
            if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                if (agentGroupRoleType == AgentGroupRoleType.City) {
                    showGroupRoleType = AgentGroupRoleType.City;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City, true));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area, false));
                } else {
                    showGroupRoleType = AgentGroupRoleType.Area;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City, false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area, true));
                }
                //中学大区
            } else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)) {
                showGroupRoleType = AgentGroupRoleType.City;
                groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City, true));
            }
            //市场
        } else if (currentGroupRoleType == AgentGroupRoleType.Marketing) {
            //小学市场
            if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                if (agentGroupRoleType == AgentGroupRoleType.City) {
                    showGroupRoleType = AgentGroupRoleType.City;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City, true));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area, false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region, false));
                } else if (agentGroupRoleType == AgentGroupRoleType.Area) {
                    showGroupRoleType = AgentGroupRoleType.Area;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City, false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area, true));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region, false));
                } else {
                    showGroupRoleType = AgentGroupRoleType.Region;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City, false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area, false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region, true));
                }
                //中学市场
            } else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)) {
                if (agentGroupRoleType == AgentGroupRoleType.City) {
                    showGroupRoleType = AgentGroupRoleType.City;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City, true));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region, false));
                } else {
                    showGroupRoleType = AgentGroupRoleType.Region;
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City, false));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region, true));
                }
            }
        }
        dataMap.put("groupRoleTypeList", groupRoleTypeList);
        dataMap.put("showGroupRoleType", showGroupRoleType);

        return dataMap;
    }


    public Map<String, Object> generateGroupRoleType(AgentGroupRoleType groupRoleType, Boolean ifShow) {
        Map<String, Object> groupRoleTypeMap = new HashMap<>();
        groupRoleTypeMap.put("groupRoleType", groupRoleType);
        groupRoleTypeMap.put("roleName", groupRoleType.getRoleName());
        groupRoleTypeMap.put("show", ifShow);
        return groupRoleTypeMap;
    }

    public List<Map<String, Object>> generateRole(AgentGroup group, AgentGroupRoleType groupRoleType, String roleType) {
        List<Map<String, Object>> roleTypeList = new ArrayList<>();
        AgentRoleType userRoleType = null;
        if (StringUtils.isNotBlank(roleType)) {
            userRoleType = AgentRoleType.nameOf(roleType);
        }
        //按分区看
        if (groupRoleType == AgentGroupRoleType.City) {
            if (userRoleType == AgentRoleType.CityManager) {
                roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper, false));
                roleTypeList.add(generateRoleType(AgentRoleType.CityManager, true));
            } else {
                roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper, true));
                roleTypeList.add(generateRoleType(AgentRoleType.CityManager, false));
            }
        }
        //按区域看
        if (groupRoleType == AgentGroupRoleType.Area) {
            if (userRoleType == AgentRoleType.AreaManager) {
                roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper, false));
                roleTypeList.add(generateRoleType(AgentRoleType.CityManager, false));
                roleTypeList.add(generateRoleType(AgentRoleType.AreaManager, true));
            } else if (userRoleType == AgentRoleType.CityManager) {
                roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper, false));
                roleTypeList.add(generateRoleType(AgentRoleType.CityManager, true));
                roleTypeList.add(generateRoleType(AgentRoleType.AreaManager, false));
            } else {
                roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper, true));
                roleTypeList.add(generateRoleType(AgentRoleType.CityManager, false));
                roleTypeList.add(generateRoleType(AgentRoleType.AreaManager, false));
            }
        }
        //按大区看
        if (groupRoleType == AgentGroupRoleType.Region) {
            if (userRoleType == AgentRoleType.Region) {
                roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper, false));
                roleTypeList.add(generateRoleType(AgentRoleType.CityManager, false));
                //小学部门
                if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                    roleTypeList.add(generateRoleType(AgentRoleType.AreaManager, false));
                }
                roleTypeList.add(generateRoleType(AgentRoleType.Region, true));
            } else if (userRoleType == AgentRoleType.AreaManager) {
                roleTypeList.add(generateRoleType(AgentRoleType.CityManager, false));
                //小学部门
                if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                    roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper, false));
                    roleTypeList.add(generateRoleType(AgentRoleType.AreaManager, true));
                    //中学部门
                } else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)) {
                    roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper, true));
                }
                roleTypeList.add(generateRoleType(AgentRoleType.Region, false));
            } else if (userRoleType == AgentRoleType.CityManager) {
                roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper, false));
                roleTypeList.add(generateRoleType(AgentRoleType.CityManager, true));
                //小学部门
                if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                    roleTypeList.add(generateRoleType(AgentRoleType.AreaManager, false));
                }
                roleTypeList.add(generateRoleType(AgentRoleType.Region, false));
            } else {
                roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper, true));
                roleTypeList.add(generateRoleType(AgentRoleType.CityManager, false));
                //小学部门
                if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                    roleTypeList.add(generateRoleType(AgentRoleType.AreaManager, false));
                }
                roleTypeList.add(generateRoleType(AgentRoleType.Region, false));
            }
        }
        return roleTypeList;
    }

    public Map<String, Object> generateRoleType(AgentRoleType roleType, Boolean ifShow) {
        Map<String, Object> roleTypeMap = new HashMap<>();
        roleTypeMap.put("roleType", roleType);
        roleTypeMap.put("roleName", roleType.getRoleName());
        roleTypeMap.put("show", ifShow);
        return roleTypeMap;
    }


    public Map<String, Object> currentGroupList(Long groupId, Long userId) {
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        AgentGroup currentGroup = baseOrgService.getUserGroups(userId).stream().findFirst().orElse(null);
        Map<String, Object> dataMap = new HashMap<>();
        if (userRole == null || currentGroup == null || userRole == AgentRoleType.BusinessDeveloper || userRole == AgentRoleType.CityManager) {
            return dataMap;
        }
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if (group == null) {
            return dataMap;
        }
        Boolean canBack = false;
        List<AgentGroup> groupList = new ArrayList<>();
        //如果是全国总监，部门范围为市场
        if (userRole == AgentRoleType.Country && group.fetchGroupRoleType() == AgentGroupRoleType.Marketing) {
            List<AgentGroup> allGroups = baseOrgService.findAllGroups();
            //小学市场部门
            groupList.add(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && (p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL))).findFirst().orElse(null));
            //中学市场部门
            groupList.add(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).findFirst().orElse(null));
        } else {
            //范围部门与当前部门级别相同时，只显示自己部门
            if (currentGroup.fetchGroupRoleType() == group.fetchGroupRoleType()) {
                groupList.add(group);
                //不相同（为当前部门子部门）时，显示范围部门同级别的部门
            } else {
                Long parentId = group.getParentId();
                groupList.addAll(baseOrgService.getGroupListByParentId(parentId));
                canBack = true;
            }
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        groupList.forEach(item -> {
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("id", item.getId());
            groupMap.put("groupName", item.getGroupName());
            if (item.fetchGroupRoleType() == AgentGroupRoleType.City) {
                groupMap.put("clickable", false);
            } else {
                groupMap.put("clickable", true);
            }
            dataList.add(groupMap);
        });
        dataMap.put("dataList", dataList);
        dataMap.put("canBack", canBack);
        return dataMap;
    }

    public List<Map<String, Object>> subGroupList(Long groupId) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        //下级子部门
        List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(groupId);
        subGroupList.forEach(item -> {
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("id", item.getId());
            groupMap.put("groupName", item.getGroupName());
            if (item.fetchGroupRoleType() == AgentGroupRoleType.City) {
                groupMap.put("clickable", false);
            } else {
                groupMap.put("clickable", true);
            }
            dataList.add(groupMap);
        });
        return dataList;
    }

    public Map<String, Object> parentGroupList(Long groupId, Long userId) {
        List<AgentGroup> groupList = new ArrayList<>();
        //当前部门
        AgentGroup currentGroup = baseOrgService.getUserGroups(userId).stream().findFirst().orElse(null);

        Boolean canBack = false;
        //获取父级部门
        AgentGroup parentGroup = baseOrgService.getParentGroup(groupId);
        if (currentGroup.fetchGroupRoleType() == parentGroup.fetchGroupRoleType() || parentGroup.fetchGroupRoleType() == AgentGroupRoleType.Marketing) {
            //如果所在部门是全国
            if (currentGroup.fetchGroupRoleType() == AgentGroupRoleType.Country) {
                groupList.addAll(baseOrgService.findAllGroups().stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing &&
                        (p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).collect(Collectors.toList()));
            } else {
                groupList.add(parentGroup);
            }
        } else {
            Long parentGroupId = parentGroup.getParentId();
            groupList.addAll(baseOrgService.getGroupListByParentId(parentGroupId));
            canBack = true;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("groupList", groupList);
        dataMap.put("canBack", canBack);
        return dataMap;
    }


    /**
     * 日报统计
     *
     * @param date
     * @param groupId
     * @param groupRoleType
     * @param userRoleType
     * @return
     */
    public List<Map<String, Object>> dailyStatistic(Date date, Long groupId, AgentGroupRoleType groupRoleType, AgentRoleType userRoleType) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        List<AgentGroup> groupList = new ArrayList<>();
        Map<Long, List<AgentGroupUser>> groupGroupUserMap = new HashMap<>();

        AgentGroupRoleType currentGroupRoleType = baseOrgService.getGroupRole(groupId);
        //分区
        if (currentGroupRoleType == AgentGroupRoleType.City) {
            groupList.add(baseOrgService.getGroupById(groupId));
            groupGroupUserMap.putAll(baseOrgService.getGroupUserByGroups(Collections.singleton(groupId)).stream().filter(p -> Objects.equals(userRoleType.getId(), p.getUserRoleId())).collect(Collectors.groupingBy(AgentGroupUser::getGroupId)));
        } else {
            //过滤出指定部门级别的子部门
            List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId).stream().filter(p -> Objects.equals(p.getRoleId(), groupRoleType.getId())).collect(Collectors.toList());
            Set<Long> subGroupIds = subGroupList.stream().map(AgentGroup::getId).collect(Collectors.toSet());
            subGroupIds.forEach(item -> {
                Set<Long> groupIds = new HashSet<>();
                groupIds.addAll(baseOrgService.getSubGroupList(item).stream().map(AgentGroup::getId).collect(Collectors.toSet()));
                groupIds.add(item);
                groupGroupUserMap.put(item, baseOrgService.getGroupUserByGroups(groupIds).stream().filter(p -> Objects.equals(userRoleType.getId(), p.getUserRoleId())).collect(Collectors.toList()));
            });
            groupList.addAll(subGroupList);
        }

        Map<Long, AgentGroup> groupMap = groupList.stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
        Set<Long> groupIds = groupMap.keySet();

        Map<Long, List<Long>> groupUsersMap = new HashMap<>();
        List<Long> userIds = new ArrayList<>();
        groupGroupUserMap.forEach((k, v) -> {
            List<Long> userIdList = v.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            groupUsersMap.put(k, userIdList);
            userIds.addAll(userIdList);
        });

        Integer dateInt = SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd"));
        //获取人员与日报对应关系
        Map<Long, AgentDaily> userDailyMap = agentDailyDao.loadByUserIdsAndTime(userIds, dateInt);

        List<AgentDailyScore> dailyScoreList = new ArrayList<>();
        //如果角色为专员
        if (userRoleType == AgentRoleType.BusinessDeveloper) {
            //获取当天、该批人员的日报得分
            Map<Long, AgentDailyScore> dailyScoreMap = agentDailyScoreDao.loadByUserIdsAndTimeAndIndex(userIds, dateInt, AgentDailyScoreIndex.TOTAL_SCORE);
            dailyScoreMap.forEach((k, v) -> {
                dailyScoreList.add(v);
            });
        }

        for (Long item : groupIds) {
            Map<String, Object> dataMap = new HashMap<>();
            AgentGroup group = groupMap.get(item);
            if (group != null) {
                dataMap.put("groupId", item);
                dataMap.put("groupName", group.getGroupName());
            }
            int onTimeCommitNum = 0;    //按时提交
            int beLateCommitNum = 0;    //迟交
            int unCommitNum = 0;        //未提交
            Double totalScore = 0D;     //总得分
            Double averageScore = 0D;   //平均得分
            List<Long> userIdList = groupUsersMap.get(item);
            if (CollectionUtils.isNotEmpty(userIdList)) {
                for (Long userId : userIdList) {
                    AgentDaily daily = userDailyMap.get(userId);
                    if (daily != null) {
                        //按时提交
                        if (daily.getStatus() != null && Objects.equals(daily.getStatus(), AgentDaily.ON_TIME_SUBMIT)) {
                            onTimeCommitNum++;
                            //迟交
                        } else if (daily.getStatus() != null && Objects.equals(daily.getStatus(), AgentDaily.BE_LATE_SUBMIT)) {
                            beLateCommitNum++;
                        }
                        //未提交
                    } else {
                        unCommitNum++;
                    }
                }

                if (userRoleType == AgentRoleType.BusinessDeveloper) {
                    //得分列表
                    List<Double> scoreList = dailyScoreList.stream().filter(p -> userIdList.contains(p.getUserId())).map(AgentDailyScore::getScore).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(scoreList)) {
                        for (Double score : scoreList) {
                            totalScore = MathUtils.doubleAddNoScale(totalScore, score);
                        }
                        //平均得分
                        averageScore = MathUtils.doubleDivide(totalScore, scoreList.size(), 1);
                        dataMap.put("averageScore", averageScore);
                    }
                }
            }
            dataMap.put("onTimeCommitNum", onTimeCommitNum);
            dataMap.put("beLateCommitNum", beLateCommitNum);
            dataMap.put("unCommitNum", unCommitNum);
            dataList.add(dataMap);
        }
        return dataList;
    }

    /**
     * 计算保存日报得分
     *
     * @param isAdd
     * @param dailyId
     * @param dailyTime
     * @param userId
     * @param workload
     * @param visitTeaNum
     * @param todayPlanNum
     * @param yesterdayPlanNum
     * @param finishPlanNum
     * @param visitTeaList
     * @param intoSchoolIds
     * @param outerResourceNum
     * @param meetingCount
     * @param dailyStatus
     */
    public void computeScore(Boolean isAdd, String dailyId, Integer dailyTime, Long userId, Double workload, Integer visitTeaNum,
                             Integer todayPlanNum, Integer yesterdayPlanNum, Integer finishPlanNum, List<WorkRecordVisitUserInfo> visitTeaList,
                             List<Long> intoSchoolIds, Integer outerResourceNum, Integer meetingCount, Integer dailyStatus) {
        AgentGroup group = baseOrgService.getUserGroups(userId).stream().findFirst().orElse(null);
        if (group == null) {
            return;
        }
        Date dailyDate = DateUtils.stringToDate(SafeConverter.toString(dailyTime), "yyyyMMdd");
        AgentDailyScore dailyScore = new AgentDailyScore();
        dailyScore.setDailyId(dailyId);
        dailyScore.setDailyTime(dailyTime);
        dailyScore.setUserId(userId);
        dailyScore.setDisabled(false);
        //新增
        if (isAdd) {
            List<AgentDailyScore> dailyScoreList = new ArrayList<>();
            //计划性指标计算得分
            AgentDailyScore planDailyScore = new AgentDailyScore();
            try {
                BeanUtils.copyProperties(planDailyScore, dailyScore);
            } catch (Exception e) {
                logger.error("AgentDailyScore bean copy error");
            }
            planDailyScore = planComputeScore(planDailyScore, todayPlanNum, yesterdayPlanNum, finishPlanNum);
            dailyScoreList.add(planDailyScore);
            //工作量指标计算得分
            AgentDailyScore workloadDailyScore = new AgentDailyScore();
            try {
                BeanUtils.copyProperties(workloadDailyScore, dailyScore);
            } catch (Exception e) {
                logger.error("AgentDailyScore bean copy error");
            }
            workloadDailyScore = workloadComputeScore(workloadDailyScore, workload);
            dailyScoreList.add(workloadDailyScore);
            //见师量指标计算得分
            AgentDailyScore visitTeaDailyScore = new AgentDailyScore();
            try {
                BeanUtils.copyProperties(visitTeaDailyScore, dailyScore);
            } catch (Exception e) {
                logger.error("AgentDailyScore bean copy error");
            }
            visitTeaDailyScore = visitTeaComputeScore(visitTeaDailyScore, meetingCount, outerResourceNum, visitTeaNum);
            dailyScoreList.add(visitTeaDailyScore);

            //进校-质量指标计算得分
            AgentDailyScore intoSchoolQualityDailyScore = new AgentDailyScore();
            try {
                BeanUtils.copyProperties(intoSchoolQualityDailyScore, dailyScore);
            } catch (Exception e) {
                logger.error("AgentDailyScore bean copy error");
            }
            intoSchoolQualityDailyScore = intoSchoolQualityComputeScore(intoSchoolQualityDailyScore, visitTeaList, intoSchoolIds, dailyDate, userId);
            dailyScoreList.add(intoSchoolQualityDailyScore);

            //日报填写及时性指标计算得分
            AgentDailyScore dailyInTimeDailyScore = new AgentDailyScore();
            try {
                BeanUtils.copyProperties(dailyInTimeDailyScore, dailyScore);
            } catch (Exception e) {
                logger.error("AgentDailyScore bean copy error");
            }
            dailyInTimeDailyScore = dailyInTimeComputeScore(dailyInTimeDailyScore, dailyStatus);
            dailyScoreList.add(dailyInTimeDailyScore);

            agentDailyScoreDao.inserts(dailyScoreList);

            //总得分
            double score = MathUtils.doubleAddNoScale(planDailyScore.getScore(), workloadDailyScore.getScore(), visitTeaDailyScore.getScore(),
                    intoSchoolQualityDailyScore.getScore(), dailyInTimeDailyScore.getScore());
            if (score < 0) {
                score = 0;
            }
            if (score > 10) {
                score = 10;
            }
            AgentDailyScore totalDailyScore = new AgentDailyScore();
            totalDailyScore.setDailyId(dailyId);
            totalDailyScore.setDailyTime(dailyTime);
            totalDailyScore.setUserId(userId);
            totalDailyScore.setDisabled(false);
            totalDailyScore.setIndex(AgentDailyScoreIndex.TOTAL_SCORE);
            totalDailyScore.setWeight(null);
            totalDailyScore.setRatio(null);
            totalDailyScore.setScore(score);
            agentDailyScoreDao.insert(totalDailyScore);
            //编辑
        } else {
            List<AgentDailyScore> addDailyScoreList = new ArrayList<>();

            List<AgentDailyScore> dailyScoreList = agentDailyScoreDao.loadByDailyId(dailyId);
            //计划性指标计算得分
            AgentDailyScore planDailyScore = dailyScoreList.stream().filter(p -> p.getIndex() == AgentDailyScoreIndex.PLAN).findFirst().orElse(null);
            if (planDailyScore != null) {
                planDailyScore = planComputeScore(planDailyScore, todayPlanNum, yesterdayPlanNum, finishPlanNum);
                agentDailyScoreDao.replace(planDailyScore);
            } else {
                planDailyScore = new AgentDailyScore();
                try {
                    BeanUtils.copyProperties(planDailyScore, dailyScore);
                } catch (Exception e) {
                    logger.error("AgentDailyScore bean copy error");
                }
                planDailyScore = planComputeScore(planDailyScore, todayPlanNum, yesterdayPlanNum, finishPlanNum);
                addDailyScoreList.add(planDailyScore);
            }
            //工作量指标计算得分
            AgentDailyScore workloadDailyScore = dailyScoreList.stream().filter(p -> p.getIndex() == AgentDailyScoreIndex.WORKLOAD).findFirst().orElse(null);
            if (workloadDailyScore != null) {
                workloadDailyScore = workloadComputeScore(workloadDailyScore, workload);
                agentDailyScoreDao.replace(workloadDailyScore);
            } else {
                workloadDailyScore = new AgentDailyScore();
                try {
                    BeanUtils.copyProperties(workloadDailyScore, dailyScore);
                } catch (Exception e) {
                    logger.error("AgentDailyScore bean copy error");
                }
                workloadDailyScore = workloadComputeScore(workloadDailyScore, workload);
                addDailyScoreList.add(workloadDailyScore);
            }

            //见师量指标计算得分
            AgentDailyScore visitTeaDailyScore = dailyScoreList.stream().filter(p -> p.getIndex() == AgentDailyScoreIndex.VISIT_TEA).findFirst().orElse(null);
            if (visitTeaDailyScore != null) {
                visitTeaDailyScore = visitTeaComputeScore(visitTeaDailyScore, meetingCount, outerResourceNum, visitTeaNum);
                agentDailyScoreDao.replace(visitTeaDailyScore);
            } else {
                visitTeaDailyScore = new AgentDailyScore();
                try {
                    BeanUtils.copyProperties(visitTeaDailyScore, dailyScore);
                } catch (Exception e) {
                    logger.error("AgentDailyScore bean copy error");
                }
                visitTeaDailyScore = visitTeaComputeScore(visitTeaDailyScore, meetingCount, outerResourceNum, visitTeaNum);
                addDailyScoreList.add(visitTeaDailyScore);
            }

            //进校-质量指标计算得分
            AgentDailyScore intoSchoolQualityDailyScore = dailyScoreList.stream().filter(p -> p.getIndex() == AgentDailyScoreIndex.INTO_SCHOOL_QUALITY).findFirst().orElse(null);
            if (intoSchoolQualityDailyScore != null) {
                intoSchoolQualityDailyScore = intoSchoolQualityComputeScore(intoSchoolQualityDailyScore, visitTeaList, intoSchoolIds, dailyDate, userId);
                agentDailyScoreDao.replace(intoSchoolQualityDailyScore);
            } else {
                intoSchoolQualityDailyScore = new AgentDailyScore();
                try {
                    BeanUtils.copyProperties(intoSchoolQualityDailyScore, dailyScore);
                } catch (Exception e) {
                    logger.error("AgentDailyScore bean copy error");
                }
                intoSchoolQualityDailyScore = intoSchoolQualityComputeScore(intoSchoolQualityDailyScore, visitTeaList, intoSchoolIds, dailyDate, userId);
                addDailyScoreList.add(intoSchoolQualityDailyScore);
            }

            //日报填写及时性指标计算得分
            AgentDailyScore dailyInTimeDailyScore = dailyScoreList.stream().filter(p -> p.getIndex() == AgentDailyScoreIndex.DAILY_IN_TIME).findFirst().orElse(null);
            if (dailyInTimeDailyScore != null) {
                dailyInTimeDailyScore = dailyInTimeComputeScore(dailyInTimeDailyScore, dailyStatus);
                agentDailyScoreDao.replace(dailyInTimeDailyScore);
            } else {
                dailyInTimeDailyScore = new AgentDailyScore();
                try {
                    BeanUtils.copyProperties(dailyInTimeDailyScore, dailyScore);
                } catch (Exception e) {
                    logger.error("AgentDailyScore bean copy error");
                }
                dailyInTimeDailyScore = dailyInTimeComputeScore(dailyInTimeDailyScore, dailyStatus);
                addDailyScoreList.add(dailyInTimeDailyScore);
            }
            if (CollectionUtils.isNotEmpty(addDailyScoreList)) {
                agentDailyScoreDao.inserts(addDailyScoreList);
            }

            //总得分
            double score = MathUtils.doubleAddNoScale(planDailyScore.getScore(), workloadDailyScore.getScore(), visitTeaDailyScore.getScore(),
                    intoSchoolQualityDailyScore.getScore(), dailyInTimeDailyScore.getScore());
            if (score < 0) {
                score = 0;
            }
            if (score > 10) {
                score = 10;
            }
            AgentDailyScore totalDailyScore = dailyScoreList.stream().filter(p -> p.getIndex() == AgentDailyScoreIndex.TOTAL_SCORE).findFirst().orElse(null);
            if (totalDailyScore != null) {
                totalDailyScore.setScore(score);
                agentDailyScoreDao.replace(totalDailyScore);
            } else {
                totalDailyScore = new AgentDailyScore();
                totalDailyScore.setDailyId(dailyId);
                totalDailyScore.setDailyTime(dailyTime);
                totalDailyScore.setUserId(userId);
                totalDailyScore.setDisabled(false);
                totalDailyScore.setIndex(AgentDailyScoreIndex.TOTAL_SCORE);
                totalDailyScore.setWeight(null);
                totalDailyScore.setRatio(null);
                totalDailyScore.setScore(score);
                agentDailyScoreDao.insert(totalDailyScore);
            }
        }
    }

//    /**
//     * 工作量指标计算得分
//     * @param dailyScore
//     * @param group
//     * @param workload
//     * @return
//     */
//    public AgentDailyScore workloadComputeScore(AgentDailyScore dailyScore,AgentGroup group,Double workload){
//        Double weight = 0D; //权重
//        Double ratio = 0D;  //系数
//        //小学
//        if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
//            weight = 1.5;
//            //中学
//        }else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)){
//            weight = 2.0;
//        }
//        if (workload >= 3){
//            ratio = 1.0;
//        }else if (workload >= 2 && workload < 3){
//            ratio = 0.85;
//        }else if (workload >= 1 && workload < 2){
//            ratio = 0.4;
//        }else if (workload < 1){
//            ratio = 0.0;
//        }
//        dailyScore.setIndex(AgentDailyScoreIndex.WORKLOAD);
//        dailyScore.setWeight(weight);
//        dailyScore.setRatio(ratio);
//        dailyScore.setScore(MathUtils.doubleMultiplyNoScale(weight,ratio));
//        return dailyScore;
//    }
//
//
//    /**
//     * 见师量指标计算得分
//     * @param dailyScore
//     * @param group
//     * @param visitTeaNum
//     * @return
//     */
//    public AgentDailyScore visitTeaComputeScore(AgentDailyScore dailyScore,AgentGroup group,Integer visitTeaNum){
//        Double weight = 0D; //权重
//        Double ratio = 0D;  //系数
//        //小学
//        if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
//            weight = 1.5;
//            //中学
//        }else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)){
//            weight = 2.0;
//        }
//        if (visitTeaNum >= 20){
//            ratio = 1.0;
//        }else if (visitTeaNum >= 10 && visitTeaNum < 20){
//            ratio = 0.85;
//        }else if (visitTeaNum >= 5 && visitTeaNum < 10){
//            ratio = 0.4;
//        }else if (visitTeaNum < 5){
//            ratio = 0.0;
//        }
//
//        dailyScore.setIndex(AgentDailyScoreIndex.VISIT_TEA);
//        dailyScore.setWeight(weight);
//        dailyScore.setRatio(ratio);
//        dailyScore.setScore(MathUtils.doubleMultiplyNoScale(weight,ratio));
//        return dailyScore;
//    }
//
//    /**
//     * 拜访数学老师占比指标计算得分
//     * @param dailyScore
//     * @param group
//     * @param visitTeaNum
//     * @param visitMathTeaNum
//     * @return
//     */
//    public AgentDailyScore visitMathTeaScaleComputeScore(AgentDailyScore dailyScore,AgentGroup group,Integer visitTeaNum,Integer visitMathTeaNum){
//        Double weight = 0D; //权重
//        Double ratio = 0D;  //系数
//        //小学
//        if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
//            weight = 1.0;
//            //中学
//        }else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)){
//            weight = 0.0;
//        }
//        Double visitMathTeaScale = MathUtils.doubleDivide(visitMathTeaNum, visitTeaNum);
//        if (visitMathTeaScale >= 0.6){
//            ratio = 1.0;
//        }else if (visitMathTeaScale >= 0.5 && visitMathTeaScale < 0.6){
//            ratio = 0.85;
//        }else if (visitMathTeaScale >= 0.4 && visitMathTeaScale < 0.5){
//            ratio = 0.4;
//        }else if (visitMathTeaScale < 0.4){
//            ratio = 0.0;
//        }
//
//        dailyScore.setIndex(AgentDailyScoreIndex.VISIT_MATH_TEA_SCALE);
//        dailyScore.setWeight(weight);
//        dailyScore.setRatio(ratio);
//        dailyScore.setScore(MathUtils.doubleMultiplyNoScale(weight,ratio));
//        return dailyScore;
//    }
//
//    /**
//     * 是否有计划指标计算得分
//     * @param dailyScore
//     * @param planNum
//     * @return
//     */
//    public AgentDailyScore ifPlanComputeScore(AgentDailyScore dailyScore,Integer planNum){
//        Double weight = 0.5;
//        Double ratio = 0D;
//        if (planNum >= 3){
//            ratio = 1.0;
//        }else if (planNum >= 2 && planNum < 3){
//            ratio = 0.85;
//        }else if (planNum >= 1 && planNum < 2){
//            ratio = 0.4;
//        }else if (planNum == 0){
//            ratio = 0.0;
//        }
//        dailyScore.setIndex(AgentDailyScoreIndex.IF_PLAN);
//        dailyScore.setWeight(weight);
//        dailyScore.setRatio(ratio);
//        dailyScore.setScore(MathUtils.doubleMultiplyNoScale(weight,ratio));
//        return dailyScore;
//    }
//
//    /**
//     * 计划完成率指标计算得分
//     * @param dailyScore
//     * @param planNum
//     * @param finishPlanNum
//     * @param planRatio
//     * @return
//     */
//    public AgentDailyScore planFinishScaleComputeScore(AgentDailyScore dailyScore,Integer planNum, Integer finishPlanNum,Double planRatio){
//        Double weight = 1.5;
//        Double ratio = 0D;
//        if (planNum > 0){
//            //计划全部完成
//            if (Objects.equals(planNum, finishPlanNum)){
//                ratio = 1.0;
//            }
//            //计划部分完成
//            if (finishPlanNum > 0 && finishPlanNum < planNum){
//                ratio = 0.4;
//            }
//            //计划全部未完成
//            if (finishPlanNum == 0){
//                ratio = 0.0;
//            }
//        }else {
//            ratio = 0.0;
//        }
//        dailyScore.setIndex(AgentDailyScoreIndex.PLAN_FINISH_SCALE);
//        dailyScore.setWeight(weight);
//        dailyScore.setRatio(ratio);
//        dailyScore.setScore(MathUtils.doubleMultiplyNoScale(MathUtils.doubleMultiplyNoScale(weight,ratio),planRatio));
//        return dailyScore;
//    }
//
//    /**
//     * 拜访老师使用率指标计算得分
//     * @param dailyScore
//     * @param visitTeaList
//     * @param visitTeaRatio
//     * @return
//     */
//    public AgentDailyScore visitTeaUseScaleComputeScore(AgentDailyScore dailyScore,List<WorkRecordVisitUserInfo> visitTeaList, Double visitTeaRatio){
//        Set<Long> newRegisterTea = new HashSet<>(); //当前新注册老师
//        Set<Long> unAuthTea = new HashSet<>();      //未认证老师使用
//        Set<Long> authTea = new HashSet<>();        //7天以上未使用认证老师使用
//
//        if (CollectionUtils.isNotEmpty(visitTeaList)){
//            Map<Long, WorkRecordVisitUserInfo> visitTeacherMap = visitTeaList.stream().collect(Collectors.toMap(WorkRecordVisitUserInfo::getId, Function.identity(), (o1, o2) -> o1));
//            Set<Long> teacherIds = visitTeacherMap.keySet();
//            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
//
//            //老师与班组之间关系
//            Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, true);
//            Map<Long,List<Long>> teacherGroupIdMap = new HashMap<>();
//            teacherGroups.forEach((k,v) -> {
//                teacherGroupIdMap.put(k,v.stream().map(GroupTeacherMapper::getId).collect(Collectors.toList()));
//            });
//
//            //老师班组与最近八天布置作业时间对应关系
//            List<HomeWorkInfo> teacherHomeWorkList = getTeacherHomeWorkList(teacherIds,8);
//            Map<Long, List<HomeWorkInfo>> groupHomeWorkListMap = teacherHomeWorkList.stream().collect(Collectors.groupingBy(HomeWorkInfo::getGroupId));
//
//            if (CollectionUtils.isNotEmpty(teacherIds)){
//                teacherIds.forEach (teacherId -> {
//                    WorkRecordVisitUserInfo visitTeacherInfo = visitTeacherMap.get(teacherId);
//                    if (visitTeacherInfo != null){
//                        //拜访时间
//                        Date visitTime = visitTeacherInfo.getVisitTime();
//                        Teacher teacher = teacherMap.get(teacherId);
//                        if (teacher != null){
//                            //注册时间
//                            Date createTime = teacher.getCreateTime();
//                            //拜访当天新注册老师
//                            if (DateUtils.isSameDay(createTime,visitTime)){
//                                newRegisterTea.add(teacherId);
//                            }
//                        }
//                        Integer sevenDayNoUse = 0;
//                        Boolean authStatus = false;
//                        List<Long> groupIds = teacherGroupIdMap.get(teacherId);
//                        if (CollectionUtils.isNotEmpty(groupIds)){
//                            for (Long groupId : groupIds){
//                                List<HomeWorkInfo> homeWorkInfoList = groupHomeWorkListMap.get(groupId);
//                                if (CollectionUtils.isNotEmpty(homeWorkInfoList)){
//                                    for (HomeWorkInfo homeWorkInfo : homeWorkInfoList){
//                                        //布置作业时间
//                                        Date assignDate = homeWorkInfo.getAssignDate();
//                                        //拜访当天布置作业
//                                        if (DateUtils.isSameDay(assignDate,visitTime)){
//                                            if (teacher != null){
//                                                //老师认证状态
//                                                Integer authenticationState = teacher.getAuthenticationState();
//                                                //未认证
//                                                if (authenticationState != 1){
//                                                    unAuthTea.add(teacherId);
//                                                    //认证
//                                                }else {
//                                                    authStatus = true;
//                                                    //七天内使用过
//                                                    Date startDate = DateUtils.calculateDateDay(visitTime, -7);
//                                                    if (assignDate.after(startDate) && assignDate.before(visitTime)){
//                                                        sevenDayNoUse ++;
//                                                    }
//
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        //7天以上未使用认证老师使用
//                        if (authStatus && sevenDayNoUse == 0){
//                            authTea.add(teacherId);
//                        }
//                    }
//                });
//            }
//        }
//
//        //拜访老师使用率
//        Double visitTeaUseRatio = MathUtils.doubleDivide(MathUtils.doubleAdd(MathUtils.doubleAdd(newRegisterTea.size(), unAuthTea.size()), authTea.size()), visitTeaList.size());
//
//        Double weight = 3.0;
//        Double ratio = 0D;
//
//        if (visitTeaUseRatio >= 0.5){
//            ratio = 1.0;
//        }else if (visitTeaUseRatio >= 0.2 && visitTeaUseRatio < 0.5){
//            ratio = 0.4;
//        }else if (visitTeaUseRatio < 0.2){
//            ratio = 0.0;
//        }
//        dailyScore.setIndex(AgentDailyScoreIndex.VISIT_TEA_USE_SCALE);
//        dailyScore.setWeight(weight);
//        dailyScore.setRatio(ratio);
//        dailyScore.setScore(MathUtils.doubleMultiplyNoScale(MathUtils.doubleMultiplyNoScale(weight,ratio),visitTeaRatio));
//        return dailyScore;
//    }
//
//    /**
//     * 学校拜访频次指标计算得分
//     * @param dailyScore
//     * @param intoSchoolList
//     * @param userId
//     * @return
//     */
//    public AgentDailyScore visitSchoolComputeScore(AgentDailyScore dailyScore,List<WorkRecordData> intoSchoolList,Long userId){
//        Double weight = 1.0;
//        Double ratio = 0D;
//        if (CollectionUtils.isNotEmpty(intoSchoolList)){
//            //拜访时间
//            Date visitTime = intoSchoolList.get(0).getWorkTime();
//            Date startDate = DayRange.newInstance(DateUtils.addDays(visitTime, -30).getTime()).getStartDate();
//            Date endDate = DayRange.newInstance(DateUtils.addDays(visitTime, -1).getTime()).getEndDate();
//            //该专员近30天拜访学校记录
//            List<WorkRecordData> intoSchoolWorkRecordList = workRecordService.getWorkRecordDataListByUserTypeTime(Collections.singleton(userId), AgentWorkRecordType.SCHOOL,startDate,endDate);
//
//            Map<Long, List<WorkRecordData>> intoSchoolMap = intoSchoolWorkRecordList.stream().collect(Collectors.groupingBy(WorkRecordData::getSchoolId));
//
//            ratio = 1.0;
//            for (WorkRecordData workRecord : intoSchoolList){
//                //频繁拜访：拜访学校近30天拜访过2天及以上（不含今天这次拜访）
//                if (workRecordService.intoSchoolFrequentlyVisit(intoSchoolMap,workRecord.getSchoolId())){
//                    ratio = 0.0;
//                    break;
//                }
//            }
//        }
//        dailyScore.setIndex(AgentDailyScoreIndex.VISIT_SCHOOL_NUM);
//        dailyScore.setWeight(weight);
//        dailyScore.setRatio(ratio);
//        dailyScore.setScore(MathUtils.doubleMultiplyNoScale(weight,ratio));
//        return dailyScore;
//    }

    /**
     * 日报点评雷达图
     *
     * @param date
     * @param userId
     * @return
     */
    public MapMessage dailyCommentsRadarMap(Date date, Long userId) {
        Map<String, Object> dataMap = new HashMap<>();
        Integer dateInt = SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd"));
        AgentDailyScore dailyScore = agentDailyScoreDao.loadByUserIdAndTime(userId, dateInt);
        if (dailyScore == null) {
            return MapMessage.errorMessage("日报得分不存在！");
        }
        int workloadScore = 0;
        int visitTeaScore = 0;
        int visitMathTeaScaleScore = 0;
        int ifPlanScore = 0;
        int planFinishScaleScore = 0;
        int visitTeaUseScaleScore = 0;
        if (dailyScore.getIndex() == AgentDailyScoreIndex.WORKLOAD) {
            workloadScore = computeRadarMapScoreByRatio(dailyScore.getRatio());
        }
        if (dailyScore.getIndex() == AgentDailyScoreIndex.VISIT_TEA) {
            visitTeaScore = computeRadarMapScoreByRatio(dailyScore.getRatio());
        }
        if (dailyScore.getIndex() == AgentDailyScoreIndex.VISIT_MATH_TEA_SCALE) {
            visitMathTeaScaleScore = computeRadarMapScoreByRatio(dailyScore.getRatio());
        }
        if (dailyScore.getIndex() == AgentDailyScoreIndex.IF_PLAN) {
            ifPlanScore = computeRadarMapScoreByRatio(dailyScore.getRatio());
        }
        if (dailyScore.getIndex() == AgentDailyScoreIndex.PLAN_FINISH_SCALE) {
            planFinishScaleScore = computeRadarMapScoreByPlanFinishScale(dailyScore.getScore());
        }
        if (dailyScore.getIndex() == AgentDailyScoreIndex.VISIT_TEA_USE_SCALE) {
            visitTeaUseScaleScore = computeRadarMapScoreByVisitTeaUseScale(dailyScore.getScore());
        }
        AgentGroup group = baseOrgService.getUserGroups(userId).stream().findFirst().orElse(null);
        Boolean juniorSchoolBd = false; //是否小学专员
        //小学
        if (group != null && group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
            dataMap.put("visitMathTeaScaleScore", visitMathTeaScaleScore);
            juniorSchoolBd = true;
        }
        dataMap.put("workloadScore", workloadScore);
        dataMap.put("visitTeaScore", visitTeaScore);
        dataMap.put("ifPlanScore", ifPlanScore);
        dataMap.put("planFinishScaleScore", planFinishScaleScore);
        dataMap.put("visitTeaUseScaleScore", visitTeaUseScaleScore);
        dataMap.put("juniorSchoolBd", juniorSchoolBd);
        return MapMessage.successMessage().add("dataMap", dataMap);
    }

    /**
     * 根据系数计算雷达图分数
     *
     * @param ratio
     * @return
     */
    public int computeRadarMapScoreByRatio(Double ratio) {
        int score = 1;
        if (ratio != null) {
            if (ratio == 1) {
                score = 5;
            } else if (ratio == 0.85) {
                score = 4;
            } else if (ratio == 0.4) {
                score = 2;
            } else if (ratio == 0) {
                score = 1;
            }
        }
        return score;
    }

    /**
     * 根据计划完成率得分计算雷达图分数
     *
     * @param score
     * @return
     */
    public int computeRadarMapScoreByPlanFinishScale(Double score) {
        int resultScore = 1;
        if (score != null) {
            if (score == 1.5) {
                resultScore = 5;
            } else if (score >= 1.0 && score < 1.5) {
                resultScore = 4;
            } else if (score >= 0.5 && score < 1.0) {
                resultScore = 3;
            } else if (score > 0.0 && score < 0.5) {
                resultScore = 2;
            } else if (score == 0.0) {
                resultScore = 1;
            }
        }
        return resultScore;
    }

    /**
     * 根据拜访效果得分计算雷达图分数
     *
     * @param score
     * @return
     */
    public int computeRadarMapScoreByVisitTeaUseScale(Double score) {
        int resultScore = 1;
        if (score != null) {
            if (score == 3.0) {
                resultScore = 5;
            } else if (score >= 2.0 && score < 3.0) {
                resultScore = 4;
            } else if (score >= 1.0 && score < 2.0) {
                resultScore = 3;
            } else if (score > 0.0 && score < 1.0) {
                resultScore = 2;
            } else if (score == 0.0) {
                resultScore = 1;
            }
        }
        return resultScore;
    }

    public void generateIntoSchoolData(AgentRoleType userRole, Set<Long> userIds, Set<Long> intoSchoolWorkUserIds, Long userId, List<WorkRecordData> intoSchoolWorkRecordList, Date startDate, Date endDate) {
        //市场专员
        if (userRole == AgentRoleType.BusinessDeveloper) {
            userIds.add(userId);
        } else {
            AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
            userIds.addAll(baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupUser.getGroupId(), AgentRoleType.BusinessDeveloper.getId()));
        }
        intoSchoolWorkRecordList.addAll(workRecordService.getWorkRecordDataListByUserTypeTime(userIds, AgentWorkRecordType.SCHOOL, startDate, endDate));
        intoSchoolWorkUserIds.addAll(intoSchoolWorkRecordList.stream().map(WorkRecordData::getUserId).collect(Collectors.toSet()));
    }


    /**
     * 计划性指标计算得分
     *
     * @param dailyScore
     * @param todayPlanNum
     * @param yesterdayPlanNum
     * @param finishPlanNum
     * @return
     */
    public AgentDailyScore planComputeScore(AgentDailyScore dailyScore, Integer todayPlanNum, Integer yesterdayPlanNum, Integer finishPlanNum) {
        double plan1Score = MathUtils.doubleMultiplyNoScale(0.25, todayPlanNum);
        double plan2Score = MathUtils.doubleMultiplyNoScale(0.5, finishPlanNum);
        double plan3Score = MathUtils.doubleMultiplyNoScale(-0.25, (yesterdayPlanNum - finishPlanNum));
        double planScore = MathUtils.doubleAddNoScale(plan1Score, plan2Score, plan3Score);
        if (planScore < 0) {
            planScore = 0d;
        }
        if (planScore > 2) {
            planScore = 2d;
        }
        //子指标得分
        List<AgentDailySubScore> dailySubScoreList = new ArrayList<>();
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.PLAN_1, Collections.singletonList(todayPlanNum), plan1Score));
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.PLAN_2, Collections.singletonList(finishPlanNum), plan2Score));
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.PLAN_3, Collections.singletonList(yesterdayPlanNum - finishPlanNum), plan3Score));

        dailyScore.setSubScoreList(dailySubScoreList);
        dailyScore.setIndex(AgentDailyScoreIndex.PLAN);
        dailyScore.setScore(planScore);
        return dailyScore;
    }

    public AgentDailySubScore generateDailySubScore(AgentDailyScoreSubIndex index, List<Integer> statisticsNumList, Double score) {
        AgentDailySubScore dailySubScore = new AgentDailySubScore();
        dailySubScore.setIndex(index);
        dailySubScore.setStatisticsNumList(statisticsNumList);
        dailySubScore.setScore(score);
        return dailySubScore;
    }

    /**
     * 工作量指标计算得分
     *
     * @param dailyScore
     * @param workload
     * @return
     */
    public AgentDailyScore workloadComputeScore(AgentDailyScore dailyScore, Double workload) {
        double score = MathUtils.doubleMultiplyNoScale(0.3, MathUtils.doubleDivide(workload, 0.5, 0, BigDecimal.ROUND_DOWN));
        if (score > 2) {
            score = 2d;
        }
        dailyScore.setIndex(AgentDailyScoreIndex.WORKLOAD);
        dailyScore.setScore(score);
        return dailyScore;
    }

    /**
     * 见师量指标计算得分
     *
     * @param dailyScore
     * @param attendances
     * @param outerResourceNum
     * @param teacherNum
     * @return
     */
    public AgentDailyScore visitTeaComputeScore(AgentDailyScore dailyScore, Integer attendances, Integer outerResourceNum, Integer teacherNum) {
        double visitTea1Score = MathUtils.doubleMultiplyNoScale(0.7, MathUtils.doubleDivide(attendances, 30, 0, BigDecimal.ROUND_DOWN));
        double visitTea2Score = MathUtils.doubleMultiplyNoScale(0.4, outerResourceNum);
        double visitTea3Score = MathUtils.doubleMultiplyNoScale(0.1, teacherNum);
        double score = MathUtils.doubleAddNoScale(visitTea1Score, visitTea2Score, visitTea3Score);
        if (score < 0) {
            score = 0d;
        }
        if (score > 2) {
            score = 2d;
        }
        //子指标得分
        List<AgentDailySubScore> dailySubScoreList = new ArrayList<>();
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.VISIT_TEA_1, Collections.singletonList(attendances), visitTea1Score));
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.VISIT_TEA_2, Collections.singletonList(outerResourceNum), visitTea2Score));
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.VISIT_TEA_3, Collections.singletonList(teacherNum), visitTea3Score));

        dailyScore.setSubScoreList(dailySubScoreList);
        dailyScore.setIndex(AgentDailyScoreIndex.VISIT_TEA);
        dailyScore.setScore(score);
        return dailyScore;
    }

    /**
     * 进校-质量计算得分
     *
     * @param dailyScore
     * @param visitTeaList
     * @param visitSchoolIds
     * @param dailyDate
     * @param userId
     * @return
     */
    public AgentDailyScore intoSchoolQualityComputeScore(AgentDailyScore dailyScore, List<WorkRecordVisitUserInfo> visitTeaList, List<Long> visitSchoolIds, Date dailyDate, Long userId) {
        Set<Long> newRegisterTea = new HashSet<>(); //当前新注册老师
        Set<Long> unAuthTea = new HashSet<>();      //未认证老师使用
        Set<Long> authTea = new HashSet<>();        //7天以上未使用认证老师使用
        Set<Long> beParentTea = new HashSet<>();    //当天转化成家长的老师
        Set<Long> newRegParent = new HashSet<>();    //当天拜访学校产生新家长
        Set<Long> lowSchool = new HashSet<>();      //当天拜访的“单科低渗”学校
        Set<Long> frequentlySchool = new HashSet<>();  //拜访“频繁进校”标签的学校且序号（8、9、10）分值均为0、频繁拜访需剔除掉“直播展位推广”类型的进校

        //组装老师数据
        generateTeacherData(visitTeaList, newRegisterTea, unAuthTea, authTea, beParentTea, dailyDate);

        double score1 = MathUtils.doubleMultiplyNoScale(0.4, newRegisterTea.size());
        double score2 = MathUtils.doubleMultiplyNoScale(0.4, MathUtils.doubleAdd(unAuthTea.size(), authTea.size()));
        double score3 = MathUtils.doubleMultiplyNoScale(0.4, beParentTea.size());


        //拜访学校产生新注册家长
        Map<Long, List<AgentSchoolNewRegisterParent>> schoolParentMap = newRegisterParentDao.loadBySchoolIds(visitSchoolIds);
        schoolParentMap.forEach((k, v) -> {
            v.forEach(item -> {
                if (DateUtils.isSameDay(item.getRegisterTime(), dailyDate)) {
                    newRegParent.add(item.getParentId());
                }
            });
        });
        double score4 = MathUtils.doubleMultiplyNoScale(0.4, MathUtils.doubleDivide(newRegParent.size(), 5, 0, BigDecimal.ROUND_DOWN));

        //当天拜访的“单科低渗”学校
        Integer day = performanceService.lastSuccessDataDay();
        Map<Long, SchoolOnlineIndicator> schoolOnlineIndicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(visitSchoolIds, day);
        visitSchoolIds.forEach(item -> {
            SchoolOnlineIndicator schoolOnlineIndicator = schoolOnlineIndicatorMap.get(item);
            if (schoolOnlineIndicator != null) {
                AgentSchoolPermeabilityType schoolSubjectPermeabilityType = schoolResourceService.getSchoolSubjectPermeabilityType(SafeConverter.toDouble(schoolOnlineIndicator.fetchMonthData().getMaxPenetrateRateSglSubj()));
                if (schoolSubjectPermeabilityType == AgentSchoolPermeabilityType.LOW) {
                    lowSchool.add(item);
                }
            }
        });
        double score5 = MathUtils.doubleMultiplyNoScale(0.3, lowSchool.size());

        /*
        每拜访1所“频繁进校”标签的学校且序号（8、9、10）分值均为0、频繁拜访需剔除掉“直播展位推广”类型的进校
         */
        //近30天进校记录（不包含拜访当天）
        Date startTime = DayRange.newInstance(DateUtils.addDays(dailyDate, -30).getTime()).getStartDate();
        Date endTime = DayRange.newInstance(DateUtils.addDays(dailyDate, -1).getTime()).getEndDate();
        List<WorkRecordData> intoSchoolWorkRecordList = workRecordDataCompatibilityService.getWorkRecordDataListByUserTypeTime(Collections.singletonList(userId), AgentWorkRecordType.SCHOOL, startTime, endTime);
        Map<Long, List<WorkRecordData>> schoolIntoSchoolWorkRecordListMap = intoSchoolWorkRecordList.stream().filter(p -> p.getSchoolId() != null && !Objects.equals(p.getVisitSchoolType(), 3)).collect(Collectors.groupingBy(WorkRecordData::getSchoolId));
        visitSchoolIds.forEach(schoolId -> {
            if (workRecordService.intoSchoolFrequentlyVisit(schoolIntoSchoolWorkRecordListMap, schoolId) && score1 == 0 && score2 == 0 && score3 == 0) {
                frequentlySchool.add(schoolId);
            }
        });

        double score6 = MathUtils.doubleMultiplyNoScale(-0.5, frequentlySchool.size());

        /*
        每产生5个直播订单（家长工具中订单类的活动的"今日订单量"）
         */
        Date startDate = DateUtils.addMonths(new Date(), -6);
        long count = agentActivityService.getActivityOrderList(startDate, userId).stream().filter(p -> DateUtils.isSameDay(p.getCreateTime(), dailyDate)).count();
        double score7 = MathUtils.doubleMultiplyNoScale(0.4, MathUtils.doubleDivide(count, 5, 0, BigDecimal.ROUND_DOWN));


        double score = MathUtils.doubleAddNoScale(score1, score2, score3, score4, score5, score6, score7);
        if (score < 0) {
            score = 0;
        } else if (score > 4) {
            score = 4;
        }

        //子指标得分
        List<AgentDailySubScore> dailySubScoreList = new ArrayList<>();
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_1, Collections.singletonList(newRegisterTea.size()), score1));
        List<Integer> statisticsNumList = new ArrayList<>();
        statisticsNumList.add(unAuthTea.size());
        statisticsNumList.add(authTea.size());
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_2, statisticsNumList, score2));
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_3, Collections.singletonList(beParentTea.size()), score3));
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_4, new ArrayList<>(), score4));
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_5, Collections.singletonList(lowSchool.size()), score5));
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_6, Collections.singletonList(frequentlySchool.size()), score6));
        dailySubScoreList.add(generateDailySubScore(AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_7, new ArrayList<>(), score7));

        dailyScore.setIndex(AgentDailyScoreIndex.INTO_SCHOOL_QUALITY);
        dailyScore.setScore(score);
        dailyScore.setSubScoreList(dailySubScoreList);
        return dailyScore;
    }

    public void generateTeacherData(List<WorkRecordVisitUserInfo> visitTeaList, Set<Long> newRegisterTea, Set<Long> unAuthTea, Set<Long> authTea, Set<Long> beParentTea, Date visitTime) {
        if (CollectionUtils.isNotEmpty(visitTeaList)) {
            Map<Long, WorkRecordVisitUserInfo> visitTeacherMap = visitTeaList.stream().collect(Collectors.toMap(WorkRecordVisitUserInfo::getId, Function.identity(), (o1, o2) -> o1));
            Set<Long> teacherIds = visitTeacherMap.keySet();
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);

            //老师与班组之间关系
            Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, true);
            Map<Long, List<Long>> teacherGroupIdMap = new HashMap<>();
            teacherGroups.forEach((k, v) -> {
                teacherGroupIdMap.put(k, v.stream().map(GroupTeacherMapper::getId).collect(Collectors.toList()));
            });

            //老师班组与最近八天布置作业时间对应关系
            List<HomeWorkInfo> teacherHomeWorkList = getTeacherHomeWorkList(teacherIds, 8);
            Map<Long, List<HomeWorkInfo>> groupHomeWorkListMap = teacherHomeWorkList.stream().collect(Collectors.groupingBy(HomeWorkInfo::getGroupId));

            if (CollectionUtils.isNotEmpty(teacherIds)) {
                teacherIds.forEach(teacherId -> {
                    WorkRecordVisitUserInfo visitTeacherInfo = visitTeacherMap.get(teacherId);
                    if (visitTeacherInfo != null) {
                        //拜访时间
//                        Date visitTime = visitTeacherInfo.getVisitTime();
                        Teacher teacher = teacherMap.get(teacherId);
                        if (teacher != null) {
                            //注册时间
                            Date createTime = teacher.getCreateTime();
                            //拜访当天新注册老师
                            if (DateUtils.isSameDay(createTime, visitTime)) {
                                newRegisterTea.add(teacherId);
                            }
                        }
                        Integer sevenDayNoUse = 0;
                        Boolean authStatus = false;
                        List<Long> groupIds = teacherGroupIdMap.get(teacherId);
                        if (CollectionUtils.isNotEmpty(groupIds)) {
                            for (Long groupId : groupIds) {
                                List<HomeWorkInfo> homeWorkInfoList = groupHomeWorkListMap.get(groupId);
                                if (CollectionUtils.isNotEmpty(homeWorkInfoList)) {
                                    for (HomeWorkInfo homeWorkInfo : homeWorkInfoList) {
                                        //布置作业时间
                                        Date assignDate = homeWorkInfo.getAssignDate();
                                        //拜访当天布置作业
                                        if (DateUtils.isSameDay(assignDate, visitTime)) {
                                            if (teacher != null) {
                                                //老师认证状态
                                                Integer authenticationState = teacher.getAuthenticationState();
                                                //未认证
                                                if (authenticationState != 1) {
                                                    unAuthTea.add(teacherId);
                                                    //认证
                                                } else {
                                                    authStatus = true;
                                                    //七天内使用过
                                                    Date startDate = DateUtils.calculateDateDay(visitTime, -7);
                                                    if (assignDate.after(startDate) && assignDate.before(visitTime)) {
                                                        sevenDayNoUse++;
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //7天以上未使用认证老师使用
                        if (authStatus && sevenDayNoUse == 0) {
                            authTea.add(teacherId);
                        }

                        //当天转化为家长的老师
                        String mobile = sensitiveUserDataServiceClient.loadUserMobile(teacherId);
                        List<User> users = userLoaderClient.loadUsers(mobile, UserType.PARENT);
                        if (CollectionUtils.isNotEmpty(users)) {
                            if (DateUtils.isSameDay(users.get(0).getCreateTime(), visitTime)) {
                                beParentTea.add(teacherId);
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 日报填写及时性计算得分
     *
     * @param dailyScore
     * @param status
     * @return
     */
    public AgentDailyScore dailyInTimeComputeScore(AgentDailyScore dailyScore, Integer status) {
        double score = 0;
        if (Objects.equals(status, AgentDaily.BE_LATE_SUBMIT)) {
            score = -0.5;
        }
        dailyScore.setIndex(AgentDailyScoreIndex.DAILY_IN_TIME);
        dailyScore.setScore(score);
        return dailyScore;
    }


    public DailyScoreView dailyScoreOverview(String dailyId) {
        DailyScoreView dailyScoreView = new DailyScoreView();
        List<AgentDailyScore> dailyScoreList = agentDailyScoreDao.loadByDailyId(dailyId);
        if (CollectionUtils.isEmpty(dailyScoreList)) {
            return dailyScoreView;
        }
        dailyScoreList.forEach(item -> {
            if (item.getIndex() == AgentDailyScoreIndex.TOTAL_SCORE) {
                dailyScoreView.setTotalScore(SafeConverter.toDouble(item.getScore()));
            }
            if (item.getIndex() == AgentDailyScoreIndex.PLAN) {
                dailyScoreView.setPlanScore(SafeConverter.toDouble(item.getScore()));
                List<AgentDailySubScore> subScoreList = item.getSubScoreList();
                if (CollectionUtils.isNotEmpty(subScoreList)) {
                    subScoreList.forEach(p -> {
                        if (p.getIndex() == AgentDailyScoreSubIndex.PLAN_1) {
                            dailyScoreView.setMakePlanScore(SafeConverter.toDouble(p.getScore()));
                        }
                        if (p.getIndex() == AgentDailyScoreSubIndex.PLAN_2) {
                            dailyScoreView.setFinishPlanScore(SafeConverter.toDouble(p.getScore()));
                        }
                    });
                }
            }

            if (item.getIndex() == AgentDailyScoreIndex.WORKLOAD) {
                dailyScoreView.setWorkloadScore(SafeConverter.toDouble(item.getScore()));
            }

            if (item.getIndex() == AgentDailyScoreIndex.VISIT_TEA) {
                dailyScoreView.setVisitTeaScore(SafeConverter.toDouble(item.getScore()));
            }

            if (item.getIndex() == AgentDailyScoreIndex.INTO_SCHOOL_QUALITY) {
                dailyScoreView.setIntoSchoolQualityScore(SafeConverter.toDouble(item.getScore()));
                List<AgentDailySubScore> subScoreList = item.getSubScoreList();
                if (CollectionUtils.isNotEmpty(subScoreList)) {
                    subScoreList.forEach(p -> {
                        if (p.getIndex() == AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_1) {
                            dailyScoreView.setTeaRegTeaAppScore(SafeConverter.toDouble(p.getScore()));
                        }
                        if (p.getIndex() == AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_2) {
                            dailyScoreView.setUnActivityTeaAssignHwScore(SafeConverter.toDouble(p.getScore()));
                        }
                        if (p.getIndex() == AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_3) {
                            dailyScoreView.setTeaRegParentAppScore(SafeConverter.toDouble(p.getScore()));
                        }
                        if (p.getIndex() == AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_4) {
                            dailyScoreView.setNewParentScore(SafeConverter.toDouble(p.getScore()));
                        }
                        if (p.getIndex() == AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_5) {
                            dailyScoreView.setVisitLowPermeateSchoolScore(SafeConverter.toDouble(p.getScore()));
                        }
                        if (p.getIndex() == AgentDailyScoreSubIndex.INTO_SCHOOL_QUALITY_7) {
                            dailyScoreView.setLiveBroadcastOrderScore(SafeConverter.toDouble(p.getScore()));
                        }
                    });
                }
            }

            if (item.getIndex() == AgentDailyScoreIndex.DAILY_IN_TIME) {
                dailyScoreView.setDailyInTimeScore(SafeConverter.toDouble(item.getScore()));
            }

        });

        return dailyScoreView;
    }
}
