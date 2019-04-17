package com.voxlearning.utopia.agent.controller.mobile.activity.palace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.bean.group.GroupWithParent;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.activity.AgentActivityService;
import com.voxlearning.utopia.agent.service.activity.palace.PalaceActivityService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.agent.view.activity.ActivityCouponStatisticsView;
import com.voxlearning.utopia.agent.view.activity.palace.PalaceDataView;
import com.voxlearning.utopia.agent.view.activity.palace.PalaceRankingView;
import com.voxlearning.utopia.agent.view.activity.palace.PalaceRecordDataView;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mobile/activity/palace")
public class PalaceActivityController extends AbstractAgentController {

    @Inject
    private PalaceActivityService palaceActivityService;
    @Inject
    private AgentActivityService agentActivityService;
    @Inject
    private AgentGroupSupport agentGroupSupport;
    @Inject
    private AgentGroupUserLoaderClient groupUserLoaderClient;

    @RequestMapping("index.vpage")
    @ResponseBody
    public MapMessage index(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        MapMessage message = MapMessage.successMessage();

        PalaceDataView dataView = palaceActivityService.getUserOverview(activityId, getCurrentUserId());
        message.add("userData", dataView);

        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        if(roleType == AgentRoleType.BusinessDeveloper || roleType == AgentRoleType.CityManager){
            Integer rankingType = 1;
            if(roleType == AgentRoleType.CityManager){
                rankingType = 2;
            }
            List<PalaceRankingView> rankingList = palaceActivityService.getRankingList(activityId,  1, rankingType, 1000, getCurrentUserId());

            // 获取排行榜信息
            Long id;
            if(roleType == AgentRoleType.CityManager){
                List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(getCurrentUserId());
                if(CollectionUtils.isNotEmpty(groupIds)){
                    id = groupIds.get(0);
                }else {
                    id = null;
                }
            }else {
                id = getCurrentUserId();
            }
            if(id != null){
                PalaceRankingView rankingView = rankingList.stream().filter(p -> Objects.equals(p.getId(), id)).findFirst().orElse(null);
                if(rankingView != null){
                    message.add("ranking", rankingView.getRanking());
                }
            }
        }

        Map<String, Object> newUserDataMap = palaceActivityService.getNewUserData(activityId, getCurrentUserId());
        message.putAll(newUserDataMap);
        return message;
    }

    @RequestMapping("record_list.vpage")
    @ResponseBody
    public MapMessage recordList(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        Long userId = getRequestLong("userId");
        if(userId < 1){
            userId = getCurrentUserId();
        }
        List<PalaceRecordDataView> recordList = palaceActivityService.getRecordList(activityId, userId);
        Map<Integer, List<PalaceRecordDataView>> dayDataMap = recordList.stream().collect(Collectors.groupingBy(p -> SafeConverter.toInt(DateUtils.dateToString(p.getBusinessTime(), "yyyyMMdd"))));
        Map<String, List<PalaceRecordDataView>> dataMap = new LinkedHashMap<>();
        if(MapUtils.isNotEmpty(dayDataMap)) {
            List<Integer> dayList = dayDataMap.keySet().stream().sorted((o1, o2) -> (o2 - o1)).collect(Collectors.toList());
            for (Integer day : dayList) {
                String key = DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyy-MM-dd");
                List<PalaceRecordDataView> dataList = dayDataMap.get(day);
                dataList.sort((o1, o2) -> o2.getBusinessTime().compareTo(o1.getBusinessTime()));
                dataMap.put(key, dataList);
            }
        }

        return MapMessage.successMessage().add("dataMap", dataMap);
    }

    @RequestMapping("chart_info.vpage")
    @ResponseBody
    public MapMessage getChartInfo(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }
        Map<String, Integer> dataMap = palaceActivityService.getChartInfo(activityId, getCurrentUserId());
        return MapMessage.successMessage().add("chartData", dataMap);
    }


    //排行榜
    @RequestMapping("ranking_list.vpage")
    @ResponseBody
    public MapMessage rankingList(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }
        Integer dateType = getRequestInt("dateType",1);  // 1 当日 2 累计
        Integer rankingType = getRequestInt("rankingType",1);  //1 专员 2 分区
        List<PalaceRankingView> dataList = palaceActivityService.getRankingList(activityId,  dateType, rankingType, 10, getCurrentUserId());
        return MapMessage.successMessage().add("dataList", dataList);
    }

    @RequestMapping("statistics_data_list.vpage")
    @ResponseBody
    public MapMessage getDataList(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        Long id = getRequestLong("id",0);
        Integer idType = getRequestInt("idType",1);
        Integer dimension = getRequestInt("dimension",1);

        int sumType = getRequestInt("sumType", 1);     // 1：合计  2： 人均

        boolean isMarketing = true;
        if(idType.equals(AgentConstants.INDICATOR_TYPE_GROUP)){
            GroupWithParent groupWithParent = agentGroupSupport.generateGroupWithParent(id);
            while(groupWithParent != null){
                if(StringUtils.contains(groupWithParent.getGroupName(), "一起教育科技")){
                    isMarketing = false;
                    break;
                }
                groupWithParent = groupWithParent.getParent();
            }
        }

        List<PalaceDataView> dataList = new ArrayList<>();

        if(isMarketing){
            dataList = palaceActivityService.getStatisticsDataList(activityId, id, idType, dimension);
        }else {
            AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
            if(roleType == AgentRoleType.Country){
                dataList = palaceActivityService.getOfficeDataList(activityId, id, idType, dimension);
            }else {
                List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(getCurrentUserId());
                if(CollectionUtils.isNotEmpty(groupUserList)){
                    Long targetGroupId = groupUserList.get(0).getGroupId();
                    dataList = palaceActivityService.getOfficeDataList(activityId, targetGroupId, idType, dimension);
                }
            }
        }

        // 人均的情况
        if(sumType == 2){
            palaceActivityService.calAvgDataList(dataList);
        }
        return MapMessage.successMessage().add("dataList", dataList);
    }

    @RequestMapping("get_target_user_coupon_data.vpage")
    @ResponseBody
    public MapMessage getTargetUserCouponData(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        List<AgentGroupUser> groupUserList = groupUserLoaderClient.findAll();
        Map<Long, AgentRoleType> userRoleMap = groupUserList.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, AgentGroupUser::getUserRoleType, (o1, o2) -> o1));

        List<PalaceDataView> dataList = palaceActivityService.getUserDataViewList(activityId, userRoleMap.keySet());

        List<Map<String, Object>> resultList = dataList.stream().map(p -> {
            Map<String, Object> map = BeanMapUtils.tansBean2Map(p);
            map.put("role", userRoleMap.get(p.getId()));
            return map;
        }).collect(Collectors.toList());

        return MapMessage.successMessage().add("dataList", resultList);
    }






}
