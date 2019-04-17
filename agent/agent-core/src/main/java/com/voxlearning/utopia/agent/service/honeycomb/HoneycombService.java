package com.voxlearning.utopia.agent.service.honeycomb;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.group.GroupWithParent;
import com.voxlearning.utopia.agent.bean.honeycomb.HoneycombFansStatisticsData;
import com.voxlearning.utopia.agent.bean.honeycomb.HoneycombOrderStatisticsData;
import com.voxlearning.utopia.agent.bean.honeycomb.HoneycombPartnerStatisticsData;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombOrderStatistics;
import com.voxlearning.utopia.agent.service.AgentApiAuth;
import com.voxlearning.utopia.agent.service.activity.AgentActivityService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.agent.view.activity.ActivityDataView;
import com.voxlearning.utopia.agent.view.honeycomb.HoneycombDataView;
import com.voxlearning.utopia.agent.view.honeycomb.HoneycombRankingView;
import com.voxlearning.utopia.agent.view.honeycomb.HoneycombTargetUserCount;
import com.voxlearning.utopia.agent.view.honeycomb.HoneycombTargetUserDetail;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class HoneycombService {
    @Inject
    private HoneycombOrderStatisticsService honeycombOrderStatisticsService;
    @Inject
    private AgentActivityService agentActivityService;
    @Inject
    private HoneycombFansStatisticsService honeycombFansStatisticsService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private HoneycombUserService honeycombUserService;
    @Inject
    private HoneycombPartnerStatisticsService partnerStatisticsService;
    @Inject
    private AgentGroupSupport agentGroupSupport;

    public static final Integer DATE_TYPE_DAY = 1;   // 日
    public static final Integer DATE_TYPE_WEEK = 2;     //周
    public static final Integer DATE_TYPE_MONTH = 3;     //月

    private List<List<Integer>> getDayRangeList(Date date, Integer dateType){
        List<List<Integer>> dataList = new ArrayList<>();
        Date tmpDate = date;
        if(!judgeDate(tmpDate, dateType)){
            return dataList;
        }
        int pageSize = 6;
        if(Objects.equals(dateType, DATE_TYPE_DAY)){
            pageSize = 7;
        }else if(Objects.equals(dateType,DATE_TYPE_WEEK)){
            pageSize = 4;
        }else if (Objects.equals(dateType,DATE_TYPE_MONTH)) {
            pageSize = 3;
        }

        for(int i = 0 ; i < pageSize; i++){
            List<Integer> days = getDayList(tmpDate, dateType);
            dataList.add(days);
            if (Objects.equals(dateType, DATE_TYPE_DAY)){
                tmpDate = DateUtils.addDays(tmpDate, -1);
            }else if (Objects.equals(dateType,DATE_TYPE_WEEK)){
                tmpDate = DateUtils.addWeeks(tmpDate, -1);
            }else if (Objects.equals(dateType,DATE_TYPE_MONTH)){
                tmpDate = DateUtils.addMonths(tmpDate, -1);
            }

            if(!judgeDate(tmpDate, dateType)){
                break;
            }
        }
        return dataList;
    }

    private boolean judgeDate(Date date, Integer dateType){
        if (Objects.equals(dateType, DATE_TYPE_DAY)){
            return date.after(DateUtils.addDays(new Date(), -90));
        }else if (Objects.equals(dateType,DATE_TYPE_WEEK)){
            return date.after(DateUtils.addWeeks(new Date(), -30));
        }else if (Objects.equals(dateType,DATE_TYPE_MONTH)){
            return date.after(DateUtils.addMonths(new Date(), -12));
        }
        return false;
    }


    public String showFormatDate(Integer day, Integer dateType){
        if(Objects.equals(dateType, DATE_TYPE_DAY)){
            return String.valueOf(day);
        }else if(Objects.equals(dateType, DATE_TYPE_WEEK)){
            Date startDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
            WeekRange wr = WeekRange.newInstance(startDate.getTime());
            return DateUtils.dateToString(wr.getStartDate(),"MM/dd") + "-" + DateUtils.dateToString(wr.getEndDate(),"MM/dd");
        }else if(Objects.equals(dateType, DATE_TYPE_MONTH)){
            return SafeConverter.toInt(String.valueOf(day).substring(4, 6)) + "月";
        }
        return String.valueOf(day);
    }



    public List<Map<String,Object>> getChartData(Date date, Integer dateType, Long userId){
        List<Map<String,Object>> resultList = new ArrayList<>();

        List<List<Integer>> dayRangeList = getDayRangeList(date, dateType);
        if(CollectionUtils.isEmpty(dayRangeList)){
            return resultList;
        }

        Collection<Long> userIds = agentActivityService.getManagedUsers(userId);
        if(CollectionUtils.isNotEmpty(userIds)){
            dayRangeList.forEach(days -> {
                Map<String, Object> itemData = new HashMap<>();
                int orderNum = 0;
                List<HoneycombOrderStatistics> orderStatisticsList = honeycombOrderStatisticsService.getOrderStatistics(userIds, days);
                if(CollectionUtils.isNotEmpty(orderStatisticsList)){
                    orderNum = orderStatisticsList.stream().map(t -> SafeConverter.toInt(t.getCount())).reduce(0, (x, y) -> x + y);
                }
                itemData.put("orderNum", orderNum);
                Integer day = days.stream().min(Comparator.comparing(Function.identity())).orElse(null);
                itemData.put("startDate", day);
                itemData.put("day", showFormatDate(day, dateType));
                resultList.add(itemData);
            });
        }

        return resultList;
    }


    public HoneycombDataView dataOverview(Date date, Integer dateType, Long userId) {

        List<Integer> days = getDayList(date,dateType);

        Long id = userId;
        Integer idType = AgentConstants.INDICATOR_TYPE_USER;
        HoneycombOrderStatisticsData orderStatisticsData = null;
        HoneycombFansStatisticsData fansStatisticsData = null;
        HoneycombPartnerStatisticsData partnerStatisticsData = null;
        List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        if(CollectionUtils.isNotEmpty(groupIds)) {
            Long groupId = groupIds.get(0);
            id = groupId;
            idType = AgentConstants.INDICATOR_TYPE_GROUP;
            orderStatisticsData = honeycombOrderStatisticsService.getGroupStatisticsData(groupId, days);
            fansStatisticsData = honeycombFansStatisticsService.getGroupStatisticsData(groupId, days);
            partnerStatisticsData = partnerStatisticsService.getGroupStatisticsData(groupId, days);
        }else {
            orderStatisticsData = honeycombOrderStatisticsService.getUserStatisticsData(userId, days);
            fansStatisticsData = honeycombFansStatisticsService.getUserStatisticsData(userId, days);
            partnerStatisticsData = partnerStatisticsService.getUserStatisticsData(userId, days);
        }

        return generateDataView(id, idType, orderStatisticsData, fansStatisticsData, partnerStatisticsData);
    }


    public List<Integer> getDayList(Date date, Integer dateType){
        List<Integer> days = new ArrayList<>();
        if (Objects.equals(dateType, DATE_TYPE_DAY)){
            days.add(SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd")));
        }else if (Objects.equals(dateType,DATE_TYPE_WEEK)){
            WeekRange weekRange = WeekRange.newInstance(date.getTime());
            days.addAll(DayUtils.getEveryDays(SafeConverter.toInt(DateUtils.dateToString(weekRange.getStartDate(), "yyyyMMdd")), SafeConverter.toInt(DateUtils.dateToString(weekRange.getEndDate(), "yyyyMMdd"))));
        }else if (Objects.equals(dateType,DATE_TYPE_MONTH)){
            MonthRange monthRange = MonthRange.newInstance(date.getTime());
            days.addAll(DayUtils.getEveryDays(SafeConverter.toInt(DateUtils.dateToString(monthRange.getStartDate(), "yyyyMMdd")), SafeConverter.toInt(DateUtils.dateToString(monthRange.getEndDate(), "yyyyMMdd"))));
        }
        return days;
    }


    public List<HoneycombDataView> statisticsDataList(String activityId,Date date, Integer dateType, Long id,Integer idType,Integer dimension) {
        List<HoneycombDataView> dataList = new ArrayList<>();
        AgentGroup group = null;
        if(idType.equals(AgentConstants.INDICATOR_TYPE_USER)){
            List<AgentGroup> userGroups = baseOrgService.getUserGroups(id);
            if (CollectionUtils.isNotEmpty(userGroups)){
                group = userGroups.stream().findFirst().orElse(null);
            }
        }else if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
            group = baseOrgService.getGroupById(id);
        }
        if(group == null){
            return dataList;
        }
        List<Integer> days = getDayList(date, dateType);
        if(dimension == 2 || (dimension == 1 && group.fetchGroupRoleType() == AgentGroupRoleType.City)){
            // 专员的情况下
            List<Long> userIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(group.getId(), AgentRoleType.BusinessDeveloper.getId());
            dataList.addAll(generateUserDataView(activityId, userIds, days));
        }else {
            List<AgentGroup> groups;
            if(dimension == 1 ){  // 默认情况下
                if(group.fetchGroupRoleType() != AgentGroupRoleType.City
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Area
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Region
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Marketing
                        ){
                    groups = baseOrgService.getSubGroupList(group.getId()).stream()
                            .filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).collect(Collectors.toList());
                }else {
                    groups = baseOrgService.getGroupListByParentId(group.getId());
                }
            }else {
                AgentGroupRoleType targetGroupRoleType;
                if(dimension == 3){
                    targetGroupRoleType = AgentGroupRoleType.City;
                }else if(dimension == 4){
                    targetGroupRoleType = AgentGroupRoleType.Area;
                }else if(dimension == 5){
                    targetGroupRoleType = AgentGroupRoleType.Region;
                }else {
                    targetGroupRoleType = null;
                }
                groups = baseOrgService.getSubGroupList(group.getId()).stream()
                        .filter(p -> Objects.equals(p.fetchGroupRoleType(), targetGroupRoleType))
                        .collect(Collectors.toList());
            }
            List<Long> groupIds = groups.stream().map(AgentGroup::getId).collect(Collectors.toList());
            dataList.addAll(generateGroupDataView(activityId, groupIds, days));
        }
        return dataList;
    }

    public List<Map<String, Object>> calAvgDataList(List<HoneycombDataView> dataList){
        if(CollectionUtils.isEmpty(dataList)){
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataMapList = new ArrayList<>();
        dataList.forEach(p -> {
            int size = 1;
            if(Objects.equals(p.getIdType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                AgentRoleType targetRole = AgentRoleType.BusinessDeveloper;
                GroupWithParent groupWithParent = agentGroupSupport.generateGroupWithParent(p.getId());
                while(groupWithParent != null){
                    if(StringUtils.contains(groupWithParent.getGroupName(), "渠道")){
                        targetRole = AgentRoleType.CityAgentLimited;
                        break;
                    }
                    groupWithParent = groupWithParent.getParent();
                }
                List<AgentGroupUser> groupUserList = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(p.getId(), targetRole.getId());
                size = groupUserList.size();
            }
            dataMapList.add(p.convertToAverageMap(size, 1));
        });
        return dataMapList;
    }


    public Integer getBeforeDate(Date date, Integer dateType){
        Integer beforeDate = SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd"));
        if (Objects.equals(dateType, DATE_TYPE_DAY)){
            beforeDate = SafeConverter.toInt(DateUtils.dateToString(DayUtils.addDay(date,-1), "yyyyMMdd"));
        }else if (Objects.equals(dateType,DATE_TYPE_WEEK)){
            WeekRange weekRange = WeekRange.newInstance(date.getTime());
            weekRange = weekRange.previous();
            beforeDate = SafeConverter.toInt(DateUtils.dateToString(weekRange.getStartDate(), "yyyyMMdd"));
        }else if (Objects.equals(dateType,DATE_TYPE_MONTH)){
            beforeDate = SafeConverter.toInt(DateUtils.dateToString(DayUtils.getFirstDayOfMonth(DayUtils.addMonth(date,-1)), "yyyyMMdd"));
        }
        return beforeDate;
    }

    public Integer getAfterDate(Date date, Integer dateType){
        Integer afterDate = SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd"));
        if (Objects.equals(dateType, DATE_TYPE_DAY)){
            afterDate = SafeConverter.toInt(DateUtils.dateToString(DayUtils.addDay(date,1), "yyyyMMdd"));
        }else if (Objects.equals(dateType,DATE_TYPE_WEEK)){
            WeekRange weekRange = WeekRange.newInstance(date.getTime());
            weekRange = weekRange.next();
            afterDate = SafeConverter.toInt(DateUtils.dateToString(weekRange.getStartDate(), "yyyyMMdd"));
        }else if (Objects.equals(dateType,DATE_TYPE_MONTH)){
            afterDate = SafeConverter.toInt(DateUtils.dateToString(DayUtils.getFirstDayOfMonth(DayUtils.addMonth(date,1)), "yyyyMMdd"));
        }
        return afterDate;
    }

    public List<HoneycombDataView> generateUserDataView(String activityId,Collection<Long> userIds,Collection<Integer> days){

        //获取粉丝数
        List<HoneycombFansStatisticsData> fansStatisticsDataList = honeycombFansStatisticsService.getUserStatisticsData(userIds, days);
        Map<Long, HoneycombFansStatisticsData> fansStatisticsDataMap = fansStatisticsDataList.stream().collect(Collectors.toMap(HoneycombFansStatisticsData::getId, Function.identity(), (o1, o2) -> o1));

        //订单&异业订单
        List<HoneycombOrderStatisticsData> orderStatisticsDataList = honeycombOrderStatisticsService.getUserStatisticsData(activityId,userIds, days);
        Map<Long, HoneycombOrderStatisticsData> orderStatisticsDataMap = orderStatisticsDataList.stream().collect(Collectors.toMap(HoneycombOrderStatisticsData::getId, Function.identity(), (o1, o2) -> o1));

        // 合作伙伴
        List<HoneycombPartnerStatisticsData> partnerDataList = partnerStatisticsService.getUserStatisticsData(userIds, days);
        Map<Long, HoneycombPartnerStatisticsData> partnerDataMap = partnerDataList.stream().collect(Collectors.toMap(HoneycombPartnerStatisticsData::getId, Function.identity(), (o1, o2) -> o1));

        return generateDataViewList(userIds, AgentConstants.INDICATOR_TYPE_USER, orderStatisticsDataMap, fansStatisticsDataMap, partnerDataMap);
    }

    private List<HoneycombDataView> generateDataViewList(Collection<Long> ids,
                                                         Integer idType,
                                                         Map<Long, HoneycombOrderStatisticsData> orderStatisticsDataMap,
                                                         Map<Long, HoneycombFansStatisticsData> fansStatisticsDataMap,
                                                         Map<Long, HoneycombPartnerStatisticsData> partnerDataMap){
        List<HoneycombDataView> dataList = new ArrayList<>();
        if(CollectionUtils.isEmpty(ids)){
            return dataList;
        }
        ids.forEach(p -> {
            HoneycombDataView dataView = generateDataView(p, idType, orderStatisticsDataMap.get(p), fansStatisticsDataMap.get(p), partnerDataMap.get(p));
            dataList.add(dataView);
        });

        return dataList;
    }

    private HoneycombDataView generateDataView(Long id, Integer idType, HoneycombOrderStatisticsData orderStatisticsData, HoneycombFansStatisticsData fansStatisticsData, HoneycombPartnerStatisticsData partnerStatisticsData){
        HoneycombDataView dataView = new HoneycombDataView();
        dataView.setId(id);
        dataView.setIdType(idType);

        if (orderStatisticsData != null){
            dataView.setOrderNum(SafeConverter.toInt(orderStatisticsData.getTotalCount()));
            dataView.setHorizontalOrderNum(SafeConverter.toInt(orderStatisticsData.getTargetCount()));
            if(StringUtils.isBlank(dataView.getName())){
                dataView.setName(orderStatisticsData.getName());
            }
        }

        if (fansStatisticsData != null){
            dataView.setFansNum(SafeConverter.toInt(fansStatisticsData.getTotalCount()));
            if(StringUtils.isBlank(dataView.getName())){
                dataView.setName(fansStatisticsData.getName());
            }
        }

        if(partnerStatisticsData != null){
            dataView.setHorizontalContractNum(SafeConverter.toInt(partnerStatisticsData.getTotalCount()));
            if(StringUtils.isBlank(dataView.getName())){
                dataView.setName(partnerStatisticsData.getName());
            }
        }

        if(StringUtils.isBlank(dataView.getName())){
            if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
                AgentUser user = baseOrgService.getUser(id);
                if(user != null){
                    dataView.setName(user.getRealName());
                }
            }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
                AgentGroup group = baseOrgService.getGroupById(id);
                if(group != null){
                    dataView.setName(group.getGroupName());
                }
            }
        }
        return dataView;
    }

    public List<HoneycombDataView> generateGroupDataView(String activityId,Collection<Long> groupIds,Collection<Integer> days){
        //获取粉丝数
        List<HoneycombFansStatisticsData> fansStatisticsDataList = honeycombFansStatisticsService.getGroupStatisticsData(groupIds, days);
        Map<Long, HoneycombFansStatisticsData> fansStatisticsDataMap = fansStatisticsDataList.stream().collect(Collectors.toMap(HoneycombFansStatisticsData::getId, Function.identity(), (o1, o2) -> o1));

        //订单&异业订单
        List<HoneycombOrderStatisticsData> orderStatisticsDataList = honeycombOrderStatisticsService.getGroupStatisticsData(activityId,groupIds, days);
        Map<Long, HoneycombOrderStatisticsData> orderStatisticsDataMap = orderStatisticsDataList.stream().collect(Collectors.toMap(HoneycombOrderStatisticsData::getId, Function.identity(), (o1, o2) -> o1));

        // 合作伙伴
        List<HoneycombPartnerStatisticsData> partnerDataList = partnerStatisticsService.getGroupStatisticsData(groupIds, days);
        Map<Long, HoneycombPartnerStatisticsData> partnerDataMap = partnerDataList.stream().collect(Collectors.toMap(HoneycombPartnerStatisticsData::getId, Function.identity(), (o1, o2) -> o1));

        return generateDataViewList(groupIds, AgentConstants.INDICATOR_TYPE_GROUP, orderStatisticsDataMap, fansStatisticsDataMap, partnerDataMap);
    }


    public MapMessage productList(Integer pageNo,Integer pageSize){
        MapMessage mapMessage = MapMessage.successMessage();
        List<Map<String, Object>> dataList = new ArrayList<>();
        Boolean hasNext = false;
        int returnPageNo = 0;
        String domain = honeycombUserService.getDomainUrl();
        String url = "/v1/agent/product_list.vpage";
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("pageNo", SafeConverter.toString(pageNo));
        dataMap.put("pageSize", SafeConverter.toString(pageSize));
        dataMap.put("app_key", "HoneyComb");
        String sig = AgentApiAuth.generateAppKeySig(dataMap, honeycombUserService.getHoneycombSecretKey());

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(domain + url)
                .addParameter("pageNo",SafeConverter.toString(pageNo))
                .addParameter("pageSize",SafeConverter.toString(pageSize))
                .addParameter("app_key", "HoneyComb")
                .addParameter("sig", sig)
                .execute();
        MapMessage resultMap = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
        if (MapUtils.isNotEmpty(resultMap) && resultMap.isSuccess() && resultMap.containsKey("data")) {
            dataList = (List<Map<String, Object>>) resultMap.get("data");
            hasNext = SafeConverter.toBoolean(resultMap.get("hasNext"));
            returnPageNo = SafeConverter.toInt(resultMap.get("pageNo"));
        }
        mapMessage.put("dataList",dataList);
        mapMessage.put("hasNext",hasNext);
        mapMessage.put("pageNo",returnPageNo);
        return mapMessage;
    }


    public List<HoneycombRankingView> getRankingList(Date date, Integer dateType, Integer rankingType, Integer topN){

        List<HoneycombRankingView> rankingDataList = new ArrayList<>();

        List<Integer> dayList = getDayList(date, dateType);

        List<HoneycombOrderStatisticsData> statisticsDataList = new ArrayList<>();
        if(rankingType == 1) { // 专员榜
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByRole(AgentRoleType.BusinessDeveloper.getId());
            List<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            statisticsDataList.addAll(honeycombOrderStatisticsService.getUserStatisticsData(userIds, dayList));
        }else {  // 分区榜
            List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.City);
            List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
            statisticsDataList.addAll(honeycombOrderStatisticsService.getGroupStatisticsData(groupIdList, dayList));
        }

        statisticsDataList = statisticsDataList.stream().filter(p -> SafeConverter.toInt(p.getTotalCount()) > 0).collect(Collectors.toList());

        statisticsDataList.sort((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.getTotalCount()), SafeConverter.toInt(o1.getTotalCount())));

        int ranking = 0;
        int preAmount = 0;
        for(int i = 0; i< statisticsDataList.size(); i++){
            HoneycombOrderStatisticsData data = statisticsDataList.get(i);
            Integer dataValue = data.getTotalCount();
            if(ranking < topN) {
                if (preAmount != dataValue) {
                    preAmount = dataValue;
                    ranking++;
                }
                HoneycombRankingView rankingView = new HoneycombRankingView();
                rankingView.setId(data.getId());
                rankingView.setName(data.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }else {
                if (preAmount != dataValue) {
                    break;
                }
                HoneycombRankingView rankingView = new HoneycombRankingView();
                rankingView.setId(data.getId());
                rankingView.setName(data.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }
        }
        return rankingDataList;
    }



    public List<HoneycombTargetUserCount> getZeroOrderUserCount(String activityId, Collection<Long> groupIds, Date date, Integer dateType){
        List<HoneycombTargetUserCount> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<HoneycombTargetUserCount>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getZeroOrderUserCount(activityId, groupId, date, dateType)));
        }
        for(Future<HoneycombTargetUserCount> future : futureList) {
            try {
                HoneycombTargetUserCount item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;

    }

    public HoneycombTargetUserCount getZeroOrderUserCount(String activityId, Long groupId, Date date, Integer dateType){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }
        HoneycombTargetUserCount data = new HoneycombTargetUserCount();
        data.setGroupId(group.getId());
        data.setGroupName(group.getGroupName());

        List<Long> userIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());
        List<HoneycombOrderStatisticsData> userDataList = honeycombOrderStatisticsService.getUserStatisticsData(activityId, userIds, getDayList(date, dateType));
        long count = userDataList.stream().filter(p -> SafeConverter.toInt(p.getTotalCount()) == 0).count();
        data.setTargetUserCount((int)count);
        return data;
    }

    public List<HoneycombTargetUserDetail> getZeroOrderUserList(String activityId, Long groupId, Date date, Integer dateType){
        List<HoneycombTargetUserDetail> dataList = new ArrayList<>();
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return new ArrayList<>();
        }

        Map<Long, AgentGroup> groupMap = new HashMap<>();
        groupMap.put(groupId, group);

        List<Long> userIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());
        List<HoneycombOrderStatisticsData> userDataList = honeycombOrderStatisticsService.getUserStatisticsData(activityId, userIds, getDayList(date, dateType));
        userDataList.forEach(p -> {
            if(SafeConverter.toInt(p.getTotalCount()) == 0){
                HoneycombTargetUserDetail detail = new HoneycombTargetUserDetail();
                detail.setUserId(p.getId());
                detail.setUserName(p.getName());
                List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(p.getId());
                if(CollectionUtils.isNotEmpty(groupUsers)){
                    AgentGroupUser groupUser = groupUsers.get(0);
                    AgentGroup tempGroup = groupMap.get(groupUser.getGroupId());
                    if(tempGroup == null){
                        tempGroup = baseOrgService.getGroupById(groupUser.getGroupId());
                        groupMap.put(groupUser.getGroupId(), tempGroup);
                    }
                    if(tempGroup != null){
                        detail.setGroupId(tempGroup.getId());
                        detail.setGroupName(tempGroup.getGroupName());
                    }
                    dataList.add(detail);
                }
            }
        });

        return dataList;
    }


    /**
     * 排名信息
     * @param date
     * @param dateType
     * @param userId
     * @return
     */
    public Integer rankingInfo(Date date,Integer dateType,Long userId){
        int ranking = 0;
        int topN = 1000;
        HoneycombRankingView rankingView = null;
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        if (userRole == AgentRoleType.BusinessDeveloper){
            List<HoneycombRankingView> rankingList = getRankingList(date, dateType, 1, topN);
            rankingView = rankingList.stream().filter(p -> Objects.equals(p.getId(), userId)).findFirst().orElse(null);
        }else if (userRole == AgentRoleType.CityManager){
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(userId);
            Long groupId = groupUserList.stream().map(AgentGroupUser::getGroupId).findFirst().orElse(null);
            List<HoneycombRankingView> rankingList = getRankingList(date, dateType, 2, topN);
            rankingView = rankingList.stream().filter(p -> Objects.equals(p.getId(), groupId)).findFirst().orElse(null);
        }
        if (rankingView != null){
            ranking = SafeConverter.toInt(rankingView.getRanking());
        }
        return ranking;
    }


    public List<Long> getSubGroupIds(Long groupId){
        List<Long> groupIds = new ArrayList<>();
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group.fetchGroupRoleType() == AgentGroupRoleType.Country){
            List<Long> marketingGroupIds = baseOrgService.getSubGroupList(groupId).stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).map(AgentGroup::getId).collect(Collectors.toList());
            Map<Long, List<AgentGroup>> subGroupList = baseOrgService.getGroupListByParentIds(marketingGroupIds);
            if(MapUtils.isNotEmpty(subGroupList)){
                groupIds.addAll(subGroupList.values().stream().flatMap(List::stream).map(AgentGroup::getId).collect(Collectors.toSet()));
            }
        }else if(group.fetchGroupRoleType() == AgentGroupRoleType.City){
            groupIds.add(groupId);
        } else {
            List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(groupId);
            if(CollectionUtils.isNotEmpty(subGroupList)){
                groupIds.addAll(subGroupList.stream().map(AgentGroup::getId).collect(Collectors.toSet()));
            }
        }
        return groupIds;
    }

}
